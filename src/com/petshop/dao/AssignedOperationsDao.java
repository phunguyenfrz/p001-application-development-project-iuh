package com.petshop.dao;

import com.petshop.model.InvoiceItem;
import com.petshop.util.DatabaseManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class AssignedOperationsDao {

    private static final DateTimeFormatter ID_DATE = DateTimeFormatter.BASIC_ISO_DATE;

    public record ServiceRow(String id, String name, String description, int duration, boolean active) {}

    public record ServicePriceRow(String id, String serviceId, String serviceName, String size,
                                  double price, LocalDate effectiveDate, LocalDate expiryDate) {}

    public record LookupOption(String id, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    public record AppointmentRow(String id, String customer, String pet, String employee,
                                 LocalDate date, LocalTime time, String status, String note) {}

    public record InvoiceRow(String id, LocalDate date, String customer, String employee,
                             double total, String status, String paymentMethod) {}

    public record CatalogOption(String id, String name, boolean service, String size, double price) {
        @Override
        public String toString() {
            return name + " | " + String.format("%,.0f đ", price);
        }
    }

    public List<ServiceRow> findServices(String filter) {
        String sql = """
                SELECT maDichVu, tenDichVu, moTa, thoiLuong, trangThai
                FROM DichVuChamSoc
                WHERE maDichVu LIKE ? OR tenDichVu LIKE ? OR ISNULL(moTa, N'') LIKE ?
                ORDER BY tenDichVu
                """;
        String pattern = pattern(filter);
        List<ServiceRow> rows = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(new ServiceRow(result.getString("maDichVu"), result.getString("tenDichVu"),
                            result.getString("moTa"), result.getInt("thoiLuong"), result.getBoolean("trangThai")));
                }
            }
            return rows;
        } catch (SQLException exception) {
            throw databaseError("Không tải được danh sách dịch vụ", exception);
        }
    }

    public void saveService(String id, String name, String description, int duration, boolean active) {
        String sql = id == null
                ? "INSERT INTO DichVuChamSoc (maDichVu, tenDichVu, moTa, thoiLuong, trangThai) VALUES (?, ?, ?, ?, ?)"
                : "UPDATE DichVuChamSoc SET tenDichVu = ?, moTa = ?, thoiLuong = ?, trangThai = ? WHERE maDichVu = ?";
        try (Connection connection = DatabaseManager.getConnection()) {
            String serviceId = id == null ? nextId(connection, "DichVuChamSoc", "maDichVu", "DV") : id;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                if (id == null) {
                    statement.setString(1, serviceId);
                    statement.setString(2, name.trim());
                    statement.setString(3, blankToNull(description));
                    statement.setInt(4, duration);
                    statement.setBoolean(5, active);
                } else {
                    statement.setString(1, name.trim());
                    statement.setString(2, blankToNull(description));
                    statement.setInt(3, duration);
                    statement.setBoolean(4, active);
                    statement.setString(5, id);
                }
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw databaseError("Không lưu được dịch vụ", exception);
        }
    }

    public void deactivateService(String id) {
        updateBoolean("UPDATE DichVuChamSoc SET trangThai = 0 WHERE maDichVu = ?", id,
                "Không ngừng cung cấp được dịch vụ");
    }

    public List<ServicePriceRow> findServicePrices(String filter) {
        String sql = """
                SELECT bg.maBangGia, bg.maDichVu, dv.tenDichVu, bg.phanLoaiKichThuoc,
                       bg.gia, bg.ngayApDung, bg.ngayHetHieuLuc
                FROM BangGiaDichVu bg
                JOIN DichVuChamSoc dv ON dv.maDichVu = bg.maDichVu
                WHERE bg.maDichVu LIKE ? OR dv.tenDichVu LIKE ?
                ORDER BY dv.tenDichVu, bg.phanLoaiKichThuoc, bg.ngayApDung DESC
                """;
        String pattern = pattern(filter);
        List<ServicePriceRow> rows = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Date expiry = result.getDate("ngayHetHieuLuc");
                    rows.add(new ServicePriceRow(result.getString("maBangGia"), result.getString("maDichVu"),
                            result.getString("tenDichVu"), result.getString("phanLoaiKichThuoc"),
                            result.getDouble("gia"), result.getDate("ngayApDung").toLocalDate(),
                            expiry == null ? null : expiry.toLocalDate()));
                }
            }
            return rows;
        } catch (SQLException exception) {
            throw databaseError("Không tải được bảng giá dịch vụ", exception);
        }
    }

    public List<LookupOption> findServiceOptions() {
        return findOptions("SELECT maDichVu, tenDichVu FROM DichVuChamSoc WHERE trangThai = 1 ORDER BY tenDichVu",
                "maDichVu", "tenDichVu");
    }

    public void saveServicePrice(String serviceId, String size, double price,
                                 LocalDate effectiveDate, LocalDate expiryDate) {
        String sql = """
                INSERT INTO BangGiaDichVu
                    (maBangGia, maDichVu, gia, phanLoaiKichThuoc, ngayApDung, ngayHetHieuLuc)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseManager.getConnection()) {
            String id = nextId(connection, "BangGiaDichVu", "maBangGia", "BG");
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, id);
                statement.setString(2, serviceId);
                statement.setDouble(3, price);
                statement.setString(4, size);
                statement.setDate(5, Date.valueOf(effectiveDate));
                if (expiryDate == null) {
                    statement.setNull(6, Types.DATE);
                } else {
                    statement.setDate(6, Date.valueOf(expiryDate));
                }
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw databaseError("Không lưu được giá dịch vụ", exception);
        }
    }

    public List<AppointmentRow> findAppointments(String filter) {
        String sql = """
                SELECT lh.maLichHen, kh.hoTen AS tenKhach, tc.tenThuCung, nv.hoTen AS tenNhanVien,
                       lh.ngayHen, lh.gioHen, lh.trangThai, lh.ghiChu
                FROM LichHen lh
                JOIN KhachHang kh ON kh.maKhachHang = lh.maKhachHang
                JOIN ThuCung tc ON tc.maThuCung = lh.maThuCung AND tc.maKhachHang = lh.maKhachHang
                JOIN NhanVien nv ON nv.maNhanVien = lh.maNhanVien
                WHERE lh.maLichHen LIKE ? OR kh.hoTen LIKE ? OR kh.maKhachHang LIKE ?
                   OR tc.tenThuCung LIKE ? OR tc.maThuCung LIKE ? OR lh.trangThai LIKE ?
                ORDER BY lh.ngayHen DESC, lh.gioHen DESC
                """;
        String pattern = pattern(filter);
        List<AppointmentRow> rows = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 1; index <= 6; index++) {
                statement.setString(index, pattern);
            }
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(new AppointmentRow(result.getString("maLichHen"), result.getString("tenKhach"),
                            result.getString("tenThuCung"), result.getString("tenNhanVien"),
                            result.getDate("ngayHen").toLocalDate(), result.getTime("gioHen").toLocalTime(),
                            result.getString("trangThai"), result.getString("ghiChu")));
                }
            }
            return rows;
        } catch (SQLException exception) {
            throw databaseError("Không tải được danh sách lịch hẹn", exception);
        }
    }

    public List<LookupOption> findCustomerOptions() {
        return findOptions("SELECT maKhachHang, CONCAT(hoTen, N' · ', maKhachHang) AS label FROM KhachHang ORDER BY hoTen",
                "maKhachHang", "label");
    }

    public List<LookupOption> findPetOptions(String customerId) {
        String sql = "SELECT maThuCung, CONCAT(tenThuCung, N' · ', maThuCung) AS label FROM ThuCung WHERE maKhachHang = ? ORDER BY tenThuCung";
        List<LookupOption> rows = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, customerId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(new LookupOption(result.getString("maThuCung"), result.getString("label")));
                }
            }
            return rows;
        } catch (SQLException exception) {
            throw databaseError("Không tải được danh sách thú cưng", exception);
        }
    }

    public List<LookupOption> findEmployeeOptions() {
        return findOptions("SELECT maNhanVien, CONCAT(hoTen, N' · ', maNhanVien) AS label FROM NhanVien WHERE trangThai = N'Đang làm' ORDER BY hoTen",
                "maNhanVien", "label");
    }

    public List<LookupOption> findAppointmentOptions() {
        return findOptions("SELECT TOP 200 lh.maLichHen, CONCAT(lh.maLichHen, N' · ', kh.hoTen) AS label "
                        + "FROM LichHen lh JOIN KhachHang kh ON kh.maKhachHang = lh.maKhachHang "
                        + "WHERE lh.trangThai <> N'Đã hủy' ORDER BY lh.ngayHen DESC, lh.gioHen DESC",
                "maLichHen", "label");
    }

    public void createAppointment(String customerId, String petId, String employeeId, String serviceId,
                                  double petWeight, LocalDate date, LocalTime time, String note) {
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String size = petWeight < 5 ? "Nhỏ" : petWeight <= 10 ? "Vừa" : "Lớn";
                double price = findCurrentServicePrice(connection, serviceId, size, date);
                String id = nextId(connection, "LichHen", "maLichHen", "LH");
                try (PreparedStatement appointment = connection.prepareStatement("""
                        INSERT INTO LichHen
                            (maLichHen, maKhachHang, maThuCung, maNhanVien, ngayHen, gioHen, trangThai, ghiChu)
                        VALUES (?, ?, ?, ?, ?, ?, N'Đã đặt', ?)
                        """)) {
                    appointment.setString(1, id);
                    appointment.setString(2, customerId);
                    appointment.setString(3, petId);
                    appointment.setString(4, employeeId);
                    appointment.setDate(5, Date.valueOf(date));
                    appointment.setTime(6, Time.valueOf(time));
                    appointment.setString(7, blankToNull(note));
                    appointment.executeUpdate();
                }
                try (PreparedStatement detail = connection.prepareStatement("""
                        INSERT INTO ChiTietLichHen (maLichHen, maDichVu, canNangThuCung, donGia)
                        VALUES (?, ?, ?, ?)
                        """)) {
                    detail.setString(1, id);
                    detail.setString(2, serviceId);
                    detail.setDouble(3, petWeight);
                    detail.setDouble(4, price);
                    detail.executeUpdate();
                }
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                if (exception instanceof SQLException sqlException) {
                    throw sqlException;
                }
                throw exception;
            }
        } catch (SQLException exception) {
            throw databaseError("Không đặt được lịch hẹn", exception);
        }
    }

    public void updateAppointmentStatus(String id, String status) {
        updateText("UPDATE LichHen SET trangThai = ? WHERE maLichHen = ?", status, id,
                "Không cập nhật được trạng thái lịch hẹn");
    }

    public void rescheduleAppointment(String id, LocalDate date, LocalTime time) {
        String sql = "UPDATE LichHen SET ngayHen = ?, gioHen = ? WHERE maLichHen = ? AND trangThai <> N'Đã hủy'";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(date));
            statement.setTime(2, Time.valueOf(time));
            statement.setString(3, id);
            if (statement.executeUpdate() == 0) {
                throw new IllegalStateException("Không tìm thấy lịch hẹn hoặc lịch đã bị hủy.");
            }
        } catch (SQLException exception) {
            throw databaseError("Không dời được lịch hẹn", exception);
        }
    }

    public List<InvoiceRow> findInvoices(String filter) {
        String sql = """
                SELECT hd.maHoaDon, hd.ngayLap, ISNULL(kh.hoTen, N'Khách vãng lai') AS tenKhach,
                       nv.hoTen AS tenNhanVien, hd.tongTien, hd.trangThaiThanhToan, hd.hinhThucThanhToan
                FROM HoaDon hd
                LEFT JOIN KhachHang kh ON kh.maKhachHang = hd.maKhachHang
                JOIN NhanVien nv ON nv.maNhanVien = hd.maNhanVien
                WHERE hd.maHoaDon LIKE ? OR ISNULL(kh.hoTen, N'Khách vãng lai') LIKE ?
                   OR hd.trangThaiThanhToan LIKE ?
                ORDER BY hd.ngayLap DESC, hd.maHoaDon DESC
                """;
        String pattern = pattern(filter);
        List<InvoiceRow> rows = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Date paymentDate = result.getDate("ngayLap");
                    rows.add(new InvoiceRow(result.getString("maHoaDon"), paymentDate.toLocalDate(),
                            result.getString("tenKhach"), result.getString("tenNhanVien"),
                            result.getDouble("tongTien"), result.getString("trangThaiThanhToan"),
                            result.getString("hinhThucThanhToan")));
                }
            }
            return rows;
        } catch (SQLException exception) {
            throw databaseError("Không tải được danh sách hóa đơn", exception);
        }
    }

    public List<CatalogOption> findInvoiceCatalog() {
        String sql = """
                SELECT maSanPham AS id, tenSanPham AS ten, CAST(0 AS BIT) AS laDichVu,
                       CAST(NULL AS NVARCHAR(20)) AS kichThuoc, giaBan AS gia
                FROM SanPham
                WHERE trangThai = 1
                UNION ALL
                SELECT dv.maDichVu AS id, CONCAT(dv.tenDichVu, N' · ', bg.phanLoaiKichThuoc) AS ten,
                       CAST(1 AS BIT) AS laDichVu, bg.phanLoaiKichThuoc AS kichThuoc, bg.gia
                FROM BangGiaDichVu bg
                JOIN DichVuChamSoc dv ON dv.maDichVu = bg.maDichVu
                WHERE dv.trangThai = 1 AND bg.ngayApDung <= CAST(GETDATE() AS DATE)
                  AND (bg.ngayHetHieuLuc IS NULL OR bg.ngayHetHieuLuc >= CAST(GETDATE() AS DATE))
                ORDER BY ten
                """;
        List<CatalogOption> rows = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                rows.add(new CatalogOption(result.getString("id"), result.getString("ten"),
                        result.getBoolean("laDichVu"), result.getString("kichThuoc"), result.getDouble("gia")));
            }
            return rows;
        } catch (SQLException exception) {
            throw databaseError("Không tải được danh mục bán hàng", exception);
        }
    }

    public void createInvoice(String customerId, String appointmentId, String employeeId,
                              String paymentMethod, List<InvoiceItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Hóa đơn cần ít nhất một mặt hàng.");
        }
        double total = items.stream().mapToDouble(InvoiceItem::getThanhTien).sum();
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (appointmentId != null && !appointmentId.isBlank()) {
                    try (PreparedStatement appointment = connection.prepareStatement(
                            "SELECT maKhachHang FROM LichHen WHERE maLichHen = ?")) {
                        appointment.setString(1, appointmentId);
                        try (ResultSet result = appointment.executeQuery()) {
                            if (!result.next()) {
                                throw new IllegalStateException("Không tìm thấy lịch hẹn đã chọn.");
                            }
                            String appointmentCustomerId = result.getString("maKhachHang");
                            if (customerId == null || customerId.isBlank()) {
                                customerId = appointmentCustomerId;
                            } else if (!customerId.equals(appointmentCustomerId)) {
                                throw new IllegalStateException("Khách hàng không khớp với khách của lịch hẹn.");
                            }
                        }
                    }
                }
                String id = nextId(connection, "HoaDon", "maHoaDon", "HD");
                String paymentStatus = paymentMethod == null ? "Chưa thanh toán" : "Đã thanh toán";
                try (PreparedStatement invoice = connection.prepareStatement("""
                        INSERT INTO HoaDon
                            (maHoaDon, maLichHen, maNhanVien, maKhachHang, ngayLap,
                             hinhThucThanhToan, trangThaiThanhToan, maKhuyenMai, tongTien)
                    VALUES (?, ?, ?, ?, ?, ?, ?, NULL, ?)
                        """)) {
                    invoice.setString(1, id);
                    setNullableString(invoice, 2, appointmentId);
                    invoice.setString(3, employeeId);
                    setNullableString(invoice, 4, customerId);
                    invoice.setDate(5, Date.valueOf(LocalDate.now()));
                    setNullableString(invoice, 6, paymentMethod);
                    invoice.setString(7, paymentStatus);
                    invoice.setDouble(8, total);
                    invoice.executeUpdate();
                }
                for (InvoiceItem item : items) {
                    if (!item.isService() && paymentMethod != null) {
                        try (PreparedStatement stock = connection.prepareStatement("""
                                UPDATE SanPham
                                SET soLuongTon = soLuongTon - ?
                                WHERE maSanPham = ? AND trangThai = 1 AND soLuongTon >= ?
                                """)) {
                            stock.setInt(1, item.getSoLuong());
                            stock.setString(2, item.getMaSanPham());
                            stock.setInt(3, item.getSoLuong());
                            if (stock.executeUpdate() != 1) {
                                throw new IllegalStateException("Sản phẩm " + item.getTenHang()
                                        + " đã ngừng bán hoặc không đủ tồn kho.");
                            }
                        }
                    }
                    if (item.isService()) {
                        try (PreparedStatement detail = connection.prepareStatement("""
                                INSERT INTO ChiTietHoaDon_DichVu (maHoaDon, maDichVu, soLuong, donGia)
                                VALUES (?, ?, 1, ?)
                                """)) {
                            detail.setString(1, id);
                            detail.setString(2, item.getMaDichVu());
                            detail.setDouble(3, item.getDonGia());
                            detail.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement detail = connection.prepareStatement("""
                                INSERT INTO ChiTietHoaDon_SanPham (maHoaDon, maSanPham, soLuong, donGia)
                                VALUES (?, ?, ?, ?)
                                """)) {
                            detail.setString(1, id);
                            detail.setString(2, item.getMaSanPham());
                            detail.setInt(3, item.getSoLuong());
                            detail.setDouble(4, item.getDonGia());
                            detail.executeUpdate();
                        }
                    }
                }
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                if (exception instanceof SQLException sqlException) {
                    throw sqlException;
                }
                throw exception;
            }
        } catch (SQLException exception) {
            throw databaseError("Không lập được hóa đơn", exception);
        }
    }

    public boolean cancelInvoice(String id) {
        String sql = "UPDATE HoaDon SET trangThaiThanhToan = N'Đã hủy', hinhThucThanhToan = NULL "
                + "WHERE maHoaDon = ? AND trangThaiThanhToan = N'Chưa thanh toán'";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException exception) {
            throw databaseError("Không hủy được hóa đơn", exception);
        }
    }

    private List<LookupOption> findOptions(String sql, String idColumn, String labelColumn) {
        List<LookupOption> rows = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                rows.add(new LookupOption(result.getString(idColumn), result.getString(labelColumn)));
            }
            return rows;
        } catch (SQLException exception) {
            throw databaseError("Không tải được dữ liệu lựa chọn", exception);
        }
    }

    private double findCurrentServicePrice(Connection connection, String serviceId, String size, LocalDate date)
            throws SQLException {
        String sql = """
                SELECT TOP 1 gia FROM BangGiaDichVu
                WHERE maDichVu = ? AND phanLoaiKichThuoc = ? AND ngayApDung <= ?
                  AND (ngayHetHieuLuc IS NULL OR ngayHetHieuLuc >= ?)
                ORDER BY ngayApDung DESC
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, serviceId);
            statement.setString(2, size);
            statement.setDate(3, Date.valueOf(date));
            statement.setDate(4, Date.valueOf(date));
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalStateException("Dịch vụ chưa có giá áp dụng cho cỡ " + size + " vào ngày đã chọn.");
                }
                return result.getDouble(1);
            }
        }
    }

    private String nextId(Connection connection, String table, String column, String prefix) throws SQLException {
        String date = LocalDate.now().format(ID_DATE);
        String sql = "SELECT ISNULL(MAX(TRY_CONVERT(INT, RIGHT(" + column + ", 6))), 0) FROM " + table
                + " WHERE " + column + " LIKE ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, prefix + "-" + date + "-%");
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                int sequence = result.getInt(1) + 1;
                return prefix + "-" + date + "-" + String.format("%06d", sequence);
            }
        }
    }

    private void updateBoolean(String sql, String id, String errorMessage) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            if (statement.executeUpdate() == 0) {
                throw new IllegalStateException("Không tìm thấy bản ghi cần cập nhật.");
            }
        } catch (SQLException exception) {
            throw databaseError(errorMessage, exception);
        }
    }

    private void updateText(String sql, String value, String id, String errorMessage) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            statement.setString(2, id);
            if (statement.executeUpdate() == 0) {
                throw new IllegalStateException("Không tìm thấy bản ghi cần cập nhật.");
            }
        } catch (SQLException exception) {
            throw databaseError(errorMessage, exception);
        }
    }

    private static String pattern(String filter) {
        String value = filter == null ? "" : filter.trim();
        return "%" + value.replace("[", "[[]").replace("%", "[%]").replace("_", "[_]") + "%";
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static void setNullableString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, value);
        }
    }

    private static IllegalStateException databaseError(String message, SQLException exception) {
        return new IllegalStateException(message + ": " + exception.getMessage(), exception);
    }
}
