package com.petshop.dao;

import com.petshop.model.AppointmentData.CustomerOption;
import com.petshop.model.AppointmentData.Draft;
import com.petshop.model.AppointmentData.EmployeeOption;
import com.petshop.model.AppointmentData.Lookups;
import com.petshop.model.AppointmentData.PetOption;
import com.petshop.model.AppointmentData.Row;
import com.petshop.model.AppointmentData.ServiceOption;
import com.petshop.util.DbConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class AppointmentDao implements AppointmentRepository {
    @Override
    public Optional<Row> findById(String appointmentId) {
        String sql = """
                SELECT lh.maLichHen, lh.maKhachHang, kh.hoTen AS tenKhachHang,
                       lh.maThuCung, tc.tenThuCung, lh.maNhanVien, nv.hoTen AS tenNhanVien,
                       lh.ngayHen, lh.gioHen, lh.trangThai, lh.ghiChu,
                       COALESCE(STRING_AGG(dv.tenDichVu, N', '), N'') AS dichVu
                FROM LichHen lh
                JOIN KhachHang kh ON kh.maKhachHang = lh.maKhachHang
                JOIN ThuCung tc ON tc.maThuCung = lh.maThuCung
                JOIN NhanVien nv ON nv.maNhanVien = lh.maNhanVien
                LEFT JOIN ChiTietLichHen ctlh ON ctlh.maLichHen = lh.maLichHen
                LEFT JOIN DichVuChamSoc dv ON dv.maDichVu = ctlh.maDichVu
                WHERE lh.maLichHen = ?
                GROUP BY lh.maLichHen, lh.maKhachHang, kh.hoTen, lh.maThuCung,
                         tc.tenThuCung, lh.maNhanVien, nv.hoTen, lh.ngayHen,
                         lh.gioHen, lh.trangThai, lh.ghiChu
                """;
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, appointmentId);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Row(rows.getString("maLichHen"),
                        rows.getString("maKhachHang"), rows.getString("tenKhachHang"),
                        rows.getString("maThuCung"), rows.getString("tenThuCung"),
                        rows.getString("maNhanVien"), rows.getString("tenNhanVien"),
                        rows.getDate("ngayHen").toLocalDate(),
                        rows.getTime("gioHen").toLocalTime(),
                        rows.getString("trangThai"), rows.getString("ghiChu"),
                        rows.getString("dichVu")));
            }
        } catch (SQLException exception) {
            throw databaseFailure("Không thể tải lịch hẹn được chọn.", exception);
        }
    }

    @Override
    public List<Row> findByDate(LocalDate date) {
        String sql = """
                SELECT lh.maLichHen, lh.maKhachHang, kh.hoTen AS tenKhachHang,
                       lh.maThuCung, tc.tenThuCung, lh.maNhanVien, nv.hoTen AS tenNhanVien,
                       lh.ngayHen, lh.gioHen, lh.trangThai, lh.ghiChu,
                       COALESCE(STRING_AGG(dv.tenDichVu, N', '), N'') AS dichVu
                FROM LichHen lh
                JOIN KhachHang kh ON kh.maKhachHang = lh.maKhachHang
                JOIN ThuCung tc ON tc.maThuCung = lh.maThuCung
                JOIN NhanVien nv ON nv.maNhanVien = lh.maNhanVien
                LEFT JOIN ChiTietLichHen ctlh ON ctlh.maLichHen = lh.maLichHen
                LEFT JOIN DichVuChamSoc dv ON dv.maDichVu = ctlh.maDichVu
                WHERE lh.ngayHen = ?
                GROUP BY lh.maLichHen, lh.maKhachHang, kh.hoTen, lh.maThuCung,
                         tc.tenThuCung, lh.maNhanVien, nv.hoTen, lh.ngayHen,
                         lh.gioHen, lh.trangThai, lh.ghiChu
                ORDER BY lh.gioHen, kh.hoTen
                """;
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(date));
            try (ResultSet rows = statement.executeQuery()) {
                List<Row> appointments = new ArrayList<>();
                while (rows.next()) {
                    appointments.add(new Row(rows.getString("maLichHen"),
                            rows.getString("maKhachHang"), rows.getString("tenKhachHang"),
                            rows.getString("maThuCung"), rows.getString("tenThuCung"),
                            rows.getString("maNhanVien"), rows.getString("tenNhanVien"),
                            rows.getDate("ngayHen").toLocalDate(),
                            rows.getTime("gioHen").toLocalTime(),
                            rows.getString("trangThai"), rows.getString("ghiChu"),
                            rows.getString("dichVu")));
                }
                return List.copyOf(appointments);
            }
        } catch (SQLException exception) {
            throw databaseFailure("Không thể tải danh sách lịch hẹn.", exception);
        }
    }

    @Override
    public Lookups loadLookups(LocalDate date) {
        try (Connection connection = DbConnection.getConnection()) {
            return new Lookups(loadCustomers(connection), loadPets(connection),
                    loadEmployees(connection));
        } catch (SQLException exception) {
            throw databaseFailure("Không thể tải dữ liệu để đặt lịch.", exception);
        }
    }

    @Override
    public List<ServiceOption> loadServices(LocalDate date, BigDecimal petWeight) {
        try (Connection connection = DbConnection.getConnection()) {
            return loadServices(connection, date, petWeight);
        } catch (SQLException exception) {
            throw databaseFailure("Không thể tải dịch vụ và bảng giá hiện hành.", exception);
        }
    }

    @Override
    public String create(Draft draft) {
        try (Connection connection = DbConnection.getConnection()) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            try {
                BigDecimal weight = petWeight(connection, draft.customerId(), draft.petId());
                verifyCareEmployee(connection, draft.employeeId());
                List<ServiceOption> services = selectedServices(connection, draft.serviceIds(),
                        weight, draft.date());
                int duration = services.stream().mapToInt(ServiceOption::durationMinutes).sum();
                if (duration <= 0) {
                    throw new IllegalArgumentException(
                            "Thời lượng dịch vụ phải lớn hơn 0 phút.");
                }
                ensureNoOverlap(connection, draft.employeeId(), draft.date(), draft.time(), duration);
                String id = nextAppointmentId(connection, draft.date());
                insertAppointment(connection, draft, id);
                insertDetails(connection, id, weight, services);
                connection.commit();
                return id;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                if (exception instanceof SQLException sqlException) {
                    throw databaseFailure("Không thể đặt lịch. Hãy kiểm tra dữ liệu đã chọn.", sqlException);
                }
                throw exception;
            }
        } catch (SQLException exception) {
            throw databaseFailure("Không thể đặt lịch hẹn.", exception);
        }
    }

    @Override
    public void updateStatus(String appointmentId, String expectedStatus, String status) {
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE LichHen SET trangThai = ? WHERE maLichHen = ? AND trangThai = ?")) {
            statement.setString(1, status);
            statement.setString(2, appointmentId);
            statement.setString(3, expectedStatus);
            if (statement.executeUpdate() == 0) {
                throw new IllegalStateException(
                        "Trạng thái lịch hẹn đã thay đổi. Hãy tải lại danh sách.");
            }
        } catch (SQLException exception) {
            throw databaseFailure("Không thể đổi trạng thái lịch hẹn.", exception);
        }
    }

    private List<CustomerOption> loadCustomers(Connection connection) throws SQLException {
        String sql = "SELECT maKhachHang, hoTen, soDienThoai FROM KhachHang ORDER BY hoTen";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            List<CustomerOption> values = new ArrayList<>();
            while (rows.next()) {
                values.add(new CustomerOption(rows.getString(1), rows.getString(2),
                        rows.getString(3)));
            }
            return values;
        }
    }

    private List<PetOption> loadPets(Connection connection) throws SQLException {
        String sql = """
                SELECT maThuCung, maKhachHang, tenThuCung, canNang
                FROM ThuCung WHERE trangThai = 1 ORDER BY tenThuCung
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            List<PetOption> values = new ArrayList<>();
            while (rows.next()) {
                values.add(new PetOption(rows.getString(1), rows.getString(2),
                        rows.getString(3), rows.getBigDecimal(4)));
            }
            return values;
        }
    }

    private List<EmployeeOption> loadEmployees(Connection connection) throws SQLException {
        String sql = """
                SELECT maNhanVien, hoTen FROM NhanVien
                WHERE trangThai = N'Đang làm' AND chucVu = N'Chăm sóc'
                ORDER BY hoTen
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            List<EmployeeOption> values = new ArrayList<>();
            while (rows.next()) {
                values.add(new EmployeeOption(rows.getString(1), rows.getString(2)));
            }
            return values;
        }
    }

    private List<ServiceOption> loadServices(
            Connection connection, LocalDate date, BigDecimal petWeight)
            throws SQLException {
        String tier = weightTier(petWeight);
        String sql = """
                SELECT dv.maDichVu, dv.tenDichVu, dv.thoiLuong,
                       (SELECT TOP (1) bg.gia FROM BangGiaDichVu bg
                        WHERE bg.maDichVu = dv.maDichVu AND bg.phanLoaiKichThuoc = ?
                          AND bg.ngayApDung <= ? AND (bg.ngayHetHieuLuc IS NULL OR bg.ngayHetHieuLuc >= ?)
                        ORDER BY bg.ngayApDung DESC) AS gia
                FROM DichVuChamSoc dv
                WHERE dv.trangThai = 1
                ORDER BY dv.tenDichVu
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tier);
            statement.setDate(2, Date.valueOf(date));
            statement.setDate(3, Date.valueOf(date));
            try (ResultSet rows = statement.executeQuery()) {
                List<ServiceOption> values = new ArrayList<>();
                while (rows.next()) {
                    BigDecimal price = rows.getBigDecimal("gia");
                    if (price != null) {
                        values.add(new ServiceOption(rows.getString("maDichVu"),
                                rows.getString("tenDichVu"), rows.getInt("thoiLuong"), price));
                    }
                }
                return values;
            }
        }
    }

    private BigDecimal petWeight(Connection connection, String customerId, String petId)
            throws SQLException {
        String sql = """
                SELECT canNang FROM ThuCung
                WHERE maThuCung = ? AND maKhachHang = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, petId);
            statement.setString(2, customerId);
            try (ResultSet row = statement.executeQuery()) {
                if (!row.next()) {
                    throw new IllegalArgumentException("Thú cưng không thuộc khách hàng đã chọn.");
                }
                return row.getBigDecimal(1);
            }
        }
    }

    private void verifyCareEmployee(Connection connection, String employeeId)
            throws SQLException {
        String sql = """
                SELECT 1 FROM NhanVien
                WHERE maNhanVien = ? AND trangThai = N'Đang làm' AND chucVu = N'Chăm sóc'
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, employeeId);
            try (ResultSet row = statement.executeQuery()) {
                if (!row.next()) {
                    throw new IllegalArgumentException(
                            "Nhân viên được chọn không còn làm công việc Chăm sóc.");
                }
            }
        }
    }

    private List<ServiceOption> selectedServices(
            Connection connection, List<String> serviceIds, BigDecimal weight, LocalDate date)
            throws SQLException {
        Set<String> uniqueIds = new HashSet<>(serviceIds);
        if (uniqueIds.size() != serviceIds.size()) {
            throw new IllegalArgumentException("Không thể chọn trùng dịch vụ.");
        }
        String sql = """
                SELECT dv.maDichVu, dv.tenDichVu, dv.thoiLuong,
                       (SELECT TOP (1) bg.gia FROM BangGiaDichVu bg
                        WHERE bg.maDichVu = dv.maDichVu AND bg.phanLoaiKichThuoc = ?
                          AND bg.ngayApDung <= ? AND (bg.ngayHetHieuLuc IS NULL OR bg.ngayHetHieuLuc >= ?)
                        ORDER BY bg.ngayApDung DESC) AS gia
                FROM DichVuChamSoc dv
                WHERE dv.trangThai = 1 AND dv.maDichVu = ?
                """;
        List<ServiceOption> selected = new ArrayList<>();
        for (String serviceId : uniqueIds) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, weightTier(weight));
                statement.setDate(2, Date.valueOf(date));
                statement.setDate(3, Date.valueOf(date));
                statement.setString(4, serviceId);
                try (ResultSet row = statement.executeQuery()) {
                    if (!row.next() || row.getBigDecimal("gia") == null) {
                        throw new IllegalArgumentException("Không có giá hiệu lực cho một dịch vụ đã chọn.");
                    }
                    selected.add(new ServiceOption(row.getString(1), row.getString(2),
                            row.getInt(3), row.getBigDecimal(4)));
                }
            }
        }
        return selected;
    }

    private void ensureNoOverlap(
            Connection connection, String employeeId, LocalDate date, LocalTime start, int duration)
            throws SQLException {
        String sql = """
                SELECT lh.gioHen, COALESCE(SUM(dv.thoiLuong), 0) AS duration
                FROM LichHen lh WITH (UPDLOCK, HOLDLOCK)
                LEFT JOIN ChiTietLichHen ct ON ct.maLichHen = lh.maLichHen
                LEFT JOIN DichVuChamSoc dv ON dv.maDichVu = ct.maDichVu
                WHERE lh.maNhanVien = ? AND lh.ngayHen = ? AND lh.trangThai <> N'Đã hủy'
                GROUP BY lh.maLichHen, lh.gioHen
                """;
        LocalTime newEnd = start.plusMinutes(duration);
        if (newEnd.isBefore(start)) {
            throw new IllegalArgumentException("Khung giờ lịch hẹn không được vượt qua nửa đêm.");
        }
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, employeeId);
            statement.setDate(2, Date.valueOf(date));
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    LocalTime existingStart = rows.getTime("gioHen").toLocalTime();
                    LocalTime existingEnd = existingStart.plusMinutes(
                            Math.max(1, rows.getInt("duration")));
                    if (existingEnd.isBefore(existingStart)) {
                        existingEnd = LocalTime.MAX;
                    }
                    if (start.isBefore(existingEnd) && existingStart.isBefore(newEnd)) {
                        throw new IllegalArgumentException(
                                "Nhân viên đã có lịch hẹn trùng khung giờ.");
                    }
                }
            }
        }
    }

    private String nextAppointmentId(Connection connection, LocalDate date) throws SQLException {
        String day = date.format(DateTimeFormatter.BASIC_ISO_DATE);
        String sql = """
                SELECT ISNULL(MAX(TRY_CONVERT(int, RIGHT(maLichHen, 6))), 0)
                FROM LichHen WITH (UPDLOCK, HOLDLOCK)
                WHERE maLichHen LIKE ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "LH-" + day + "-%");
            try (ResultSet row = statement.executeQuery()) {
                row.next();
                return "LH-" + day + "-" + String.format("%06d", row.getInt(1) + 1);
            }
        }
    }

    private void insertAppointment(Connection connection, Draft draft, String id)
            throws SQLException {
        String sql = """
                INSERT INTO LichHen
                    (maLichHen, maKhachHang, maThuCung, maNhanVien, ngayHen, gioHen, trangThai, ghiChu)
                VALUES (?, ?, ?, ?, ?, ?, N'Đã đặt', ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            statement.setString(2, draft.customerId());
            statement.setString(3, draft.petId());
            statement.setString(4, draft.employeeId());
            statement.setDate(5, Date.valueOf(draft.date()));
            statement.setTime(6, Time.valueOf(draft.time()));
            statement.setString(7, draft.note());
            statement.executeUpdate();
        }
    }

    private void insertDetails(
            Connection connection, String appointmentId, BigDecimal weight,
            List<ServiceOption> services) throws SQLException {
        String sql = """
                INSERT INTO ChiTietLichHen (maLichHen, maDichVu, canNangThuCung, donGia)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (ServiceOption service : services) {
                statement.setString(1, appointmentId);
                statement.setString(2, service.id());
                statement.setBigDecimal(3, weight);
                statement.setBigDecimal(4, service.price());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private String weightTier(BigDecimal weight) {
        if (weight.compareTo(BigDecimal.valueOf(5)) < 0) {
            return "Nhỏ";
        }
        if (weight.compareTo(BigDecimal.TEN) <= 0) {
            return "Vừa";
        }
        return "Lớn";
    }

    private IllegalStateException databaseFailure(String message, SQLException cause) {
        return new IllegalStateException(message, cause);
    }
}
