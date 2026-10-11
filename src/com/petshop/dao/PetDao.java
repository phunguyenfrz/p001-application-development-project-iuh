package com.petshop.dao;

import com.petshop.model.Pet;
import com.petshop.util.DbConnection;

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
                   tc.tenThuCung, tc.loaiThuCung, tc.giongLoai, tc.canNang,
                   tc.ngaySinh, tc.diUng, tc.gioiTinh
            FROM ThuCung tc
            JOIN KhachHang kh ON kh.maKhachHang = tc.maKhachHang
            """;

    @Override
    public List<Pet> findAll() {
        return query(SELECT + " ORDER BY tc.tenThuCung", null);
    }

    @Override
    public List<Pet> search(String keyword) {
        String sql = SELECT + """
                WHERE tc.maThuCung LIKE ? OR tc.tenThuCung LIKE ?
                   OR tc.maKhachHang LIKE ? OR kh.hoTen LIKE ?
                ORDER BY tc.tenThuCung
                """;
        return query(sql, "%" + (keyword == null ? "" : keyword.trim()) + "%");
    }

    @Override
    public void add(Pet pet) {
        String sql = """
                INSERT INTO ThuCung
                    (maThuCung, maKhachHang, tenThuCung, loaiThuCung, giongLoai,
                     canNang, ngaySinh, diUng, gioiTinh)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pet.getMaThuCung());
            bind(statement, pet, 2);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể thêm hồ sơ thú cưng. Hãy kiểm tra mã khách hàng.", exception);
        }
    }

    @Override
    public void update(Pet pet) {
        String sql = """
                UPDATE ThuCung SET maKhachHang = ?, tenThuCung = ?, loaiThuCung = ?,
                    giongLoai = ?, canNang = ?, ngaySinh = ?, diUng = ?, gioiTinh = ?
                WHERE maThuCung = ?
                """;
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, pet, 1);
            statement.setString(9, pet.getMaThuCung());
            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException("Không tìm thấy thú cưng cần cập nhật.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể cập nhật hồ sơ thú cưng.", exception);
        }
    }

    @Override
    public void deactivate(String id) {
        throw new UnsupportedOperationException(
                "Schema hiện tại không có trạng thái ngừng cho hồ sơ thú cưng.");
    }

    private List<Pet> query(String sql, String pattern) {
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (pattern != null) {
                for (int i = 1; i <= 4; i++) {
                    statement.setString(i, pattern);
                }
            }
            try (ResultSet rows = statement.executeQuery()) {
                List<Pet> pets = new ArrayList<>();
                while (rows.next()) {
                    Pet pet = new Pet(rows.getString("maThuCung"),
                            rows.getString("maKhachHang"), rows.getString("tenThuCung"),
                            rows.getString("loaiThuCung"), rows.getString("giongLoai"),
                            rows.getDouble("canNang"),
                            rows.getDate("ngaySinh") == null ? null
                                    : rows.getDate("ngaySinh").toLocalDate(),
                            rows.getString("diUng"), rows.getString("gioiTinh"));
                    pets.add(pet);
                }
                return List.copyOf(pets);
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
    }

    private String nullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
