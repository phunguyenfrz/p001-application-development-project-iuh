package com.petshop.dao;

import com.petshop.model.Pet;
import com.petshop.util.DbConnection;
import com.petshop.util.TextSearch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class PetDao implements CrudDao<Pet, String> {
    private static final String SELECT = """
            SELECT tc.maThuCung, tc.maKhachHang, kh.hoTen AS tenChu,
                   kh.soDienThoai AS dienThoaiChu,
                   tc.tenThuCung, tc.loaiThuCung, tc.giongLoai, tc.canNang,
                   tc.ngaySinh, tc.diUng, tc.gioiTinh, tc.dacDiemNhanDang,
                   tc.benhNen, tc.tinhCach, tc.trangThai
            FROM ThuCung tc
            JOIN KhachHang kh ON kh.maKhachHang = tc.maKhachHang
            """;

    @Override
    public List<Pet> findAll() {
        return query(SELECT + " ORDER BY tc.tenThuCung", null);
    }

    @Override
    public List<Pet> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        String sql = SELECT + " WHERE "
                + TextSearch.likePredicate(keyword, "tc.maThuCung", "tc.tenThuCung",
                        "tc.loaiThuCung", "tc.giongLoai", "kh.hoTen", "kh.soDienThoai")
                + """
                ORDER BY tc.tenThuCung
                """;
        return query(sql, keyword);
    }

    public List<Pet> findByCustomer(String customerId) {
        String sql = SELECT + " WHERE tc.maKhachHang = ? ORDER BY tc.tenThuCung";
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, customerId);
            try (ResultSet rows = statement.executeQuery()) {
                return mapPets(rows);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể tải hồ sơ thú cưng của khách hàng.", exception);
        }
    }

    public List<String> findCareHistory(String petId) {
        String sql = """
                SELECT lh.ngayHen, lh.gioHen, lh.trangThai,
                       COALESCE(STRING_AGG(dv.tenDichVu, N', '), N'') AS dichVu
                FROM LichHen lh
                LEFT JOIN ChiTietLichHen ct ON ct.maLichHen = lh.maLichHen
                LEFT JOIN DichVuChamSoc dv ON dv.maDichVu = ct.maDichVu
                WHERE lh.maThuCung = ? AND lh.trangThai = N'Hoàn thành'
                GROUP BY lh.ngayHen, lh.gioHen, lh.trangThai
                ORDER BY lh.ngayHen DESC, lh.gioHen DESC
                """;
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, petId);
            try (ResultSet rows = statement.executeQuery()) {
                List<String> history = new ArrayList<>();
                while (rows.next()) {
                    history.add(rows.getDate("ngayHen").toLocalDate() + " "
                            + rows.getTime("gioHen").toLocalTime() + " · "
                            + rows.getString("dichVu"));
                }
                return List.copyOf(history);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể tải lịch sử chăm sóc thú cưng.", exception);
        }
    }

    @Override
    public void add(Pet pet) {
        String sql = """
                INSERT INTO ThuCung
                    (maThuCung, maKhachHang, tenThuCung, loaiThuCung, giongLoai,
                     canNang, ngaySinh, diUng, gioiTinh, dacDiemNhanDang,
                     benhNen, tinhCach, trangThai)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)
                """;
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            String petId = pet.getMaThuCung();
            if (petId == null || petId.isBlank()) {
                petId = nextPetId(connection);
                pet.setMaThuCung(petId);
            }
            statement.setString(1, petId);
            bind(statement, pet, 2);
            statement.executeUpdate();
            connection.commit();
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể thêm hồ sơ thú cưng. Hãy kiểm tra mã khách hàng.", exception);
        }
    }

    @Override
    public void update(Pet pet) {
        String sql = """
                UPDATE ThuCung SET maKhachHang = ?, tenThuCung = ?, loaiThuCung = ?,
                    giongLoai = ?, canNang = ?, ngaySinh = ?, diUng = ?, gioiTinh = ?,
                    dacDiemNhanDang = ?, benhNen = ?, tinhCach = ?
                WHERE maThuCung = ?
                """;
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, pet, 1);
            statement.setString(9, nullable(pet.getDacDiemNhanDang()));
            statement.setString(10, nullable(pet.getBenhNen()));
            statement.setString(11, nullable(pet.getTinhCach()));
            statement.setString(12, pet.getMaThuCung());
            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException("Không tìm thấy thú cưng cần cập nhật.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể cập nhật hồ sơ thú cưng.", exception);
        }
    }

    @Override
    public void deactivate(String id) {
        String sql = "UPDATE ThuCung SET trangThai = 0 WHERE maThuCung = ? AND trangThai = 1";
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "Không tìm thấy thú cưng đang hoạt động cần ngừng.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể ngừng hồ sơ thú cưng.", exception);
        }
    }

    private List<Pet> query(String sql, String pattern) {
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (pattern != null) {
                TextSearch.bindLikeParameters(statement, 1, pattern, 6);
            }
            try (ResultSet rows = statement.executeQuery()) {
                return mapPets(rows);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể tải hồ sơ thú cưng.", exception);
        }
    }

    private void bind(PreparedStatement statement, Pet pet, int start) throws SQLException {
        statement.setString(start, pet.getMaKhachHang());
        statement.setString(start + 1, pet.getTenThuCung().trim());
        statement.setString(start + 2, nullable(pet.getLoaiThuCung()));
        statement.setString(start + 3, nullable(pet.getGiongLoai()));
        statement.setBigDecimal(start + 4, java.math.BigDecimal.valueOf(pet.getCanNang()));
        if (pet.getNgaySinh() == null) {
            statement.setNull(start + 5, Types.DATE);
        } else {
            statement.setDate(start + 5, java.sql.Date.valueOf(pet.getNgaySinh()));
        }
        statement.setString(start + 6, nullable(pet.getDiUng()));
        statement.setString(start + 7, nullable(pet.getGioiTinh()));
        statement.setString(start + 8, nullable(pet.getDacDiemNhanDang()));
        statement.setString(start + 9, nullable(pet.getBenhNen()));
        statement.setString(start + 10, nullable(pet.getTinhCach()));
    }

    private List<Pet> mapPets(ResultSet rows) throws SQLException {
        List<Pet> pets = new ArrayList<>();
        while (rows.next()) {
            Pet pet = new Pet(rows.getString("maThuCung"),
                    rows.getString("maKhachHang"), rows.getString("tenThuCung"),
                    rows.getString("loaiThuCung"), rows.getString("giongLoai"),
                    rows.getDouble("canNang"),
                    rows.getDate("ngaySinh") == null ? null
                            : rows.getDate("ngaySinh").toLocalDate(),
                    rows.getString("diUng"), rows.getString("gioiTinh"));
            pet.setTenChu(rows.getString("tenChu"));
            pet.setDienThoaiChu(rows.getString("dienThoaiChu"));
            pet.setDacDiemNhanDang(rows.getString("dacDiemNhanDang"));
            pet.setBenhNen(rows.getString("benhNen"));
            pet.setTinhCach(rows.getString("tinhCach"));
            pet.setActive(rows.getBoolean("trangThai"));
            pets.add(pet);
        }
        return List.copyOf(pets);
    }

    private String nextPetId(Connection connection) throws SQLException {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String sql = """
                SELECT ISNULL(MAX(TRY_CONVERT(int, RIGHT(maThuCung, 6))), 0)
                FROM ThuCung WITH (UPDLOCK, HOLDLOCK)
                WHERE maThuCung LIKE ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "TC-" + date + "-%");
            try (ResultSet row = statement.executeQuery()) {
                row.next();
                return "TC-" + date + "-" + String.format("%06d", row.getInt(1) + 1);
            }
        }
    }

    private String nullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
