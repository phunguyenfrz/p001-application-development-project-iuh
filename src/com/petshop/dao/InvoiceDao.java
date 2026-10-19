package com.petshop.dao;

import com.petshop.model.Invoice;
import com.petshop.model.InvoiceItem;
import com.petshop.util.DatabaseManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Truy cập dữ liệu cho luồng lập và thanh toán hóa đơn. */
public class InvoiceDao {

    private static final DateTimeFormatter ID_DATE = DateTimeFormatter.BASIC_ISO_DATE;

    public record CustomerOption(String id, String name, String phone) {
        @Override
        public String toString() {
            return name + " · " + phone;
        }
    }

    public record PetOption(String id, String customerId, String name, String kind, double weight) {
        @Override
        public String toString() {
            return name + (kind == null || kind.isBlank() ? "" : " · " + kind);
        }
    }

    public record ServiceOption(String id, String name, String size, double price) {
        @Override
        public String toString() {
            return name + " · " + String.format("%,.0f đ", price);
        }
    }

    public record PromotionOption(String id, String name, double percent) {
        @Override
        public String toString() {
            return name + " · giảm " + String.format("%.0f%%", percent);
        }
    }

    public record AppointmentDraft(String appointmentId, CustomerOption customer,
                                   PetOption pet, List<InvoiceItem> items) {}

    public List<CustomerOption> findCustomers(String keyword) {
        String sql = """
                SELECT maKhachHang, hoTen, soDienThoai
                FROM KhachHang
                WHERE trangThai = 1 AND (maKhachHang LIKE ? OR hoTen LIKE ? OR soDienThoai LIKE ?)
                ORDER BY hoTen
                """;
        String pattern = pattern(keyword);
        List<CustomerOption> rows = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(new CustomerOption(result.getString("maKhachHang"),
                            result.getString("hoTen"), result.getString("soDienThoai")));
                }
            }
            return rows;
        } catch (SQLException exception) {
            throw databaseError("Không tải được khách hàng", exception);
        }
    }

    public List<PetOption> findPets(String customerId) {
        String sql = """
                SELECT maThuCung, maKhachHang, tenThuCung, loaiThuCung, canNang
                FROM ThuCung
                WHERE maKhachHang = ? AND trangThai = 1
                ORDER BY tenThuCung
                """;
        List<PetOption> rows = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, customerId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(new PetOption(result.getString("maThuCung"), result.getString("maKhachHang"),
                            result.getString("tenThuCung"), result.getString("loaiThuCung"),
                            result.getDouble("canNang")));
                }
            }
            return rows;
        } catch (SQLException exception) {
            throw databaseError("Không tải được thú cưng của khách hàng", exception);
        }
    }

    public List<PromotionOption> findActivePromotions(LocalDate date) {
        String sql = """
                SELECT maKhuyenMai, tenChuongTrinh, phanTramGiam
                FROM KhuyenMai
                WHERE trangThai = 1 AND ngayBatDau <= ? AND ngayKetThuc >= ?
                ORDER BY tenChuongTrinh
                """;
        List<PromotionOption> rows = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(date));
            statement.setDate(2, Date.valueOf(date));
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(new PromotionOption(result.getString("maKhuyenMai"),
                            result.getString("tenChuongTrinh"), result.getDouble("phanTramGiam")));
                }
            }
            return rows;
        } catch (SQLException exception) {
            throw databaseError("Không tải được khuyến mãi đang hiệu lực", exception);
        }
    }

    public List<ServiceOption> findServicesForPet(double weight, LocalDate date) {
        String petSize = weight < 5 ? "Nhỏ" : weight <= 10 ? "Vừa" : "Lớn";
        String sql = """
                SELECT dv.maDichVu, dv.tenDichVu, bg.phanLoaiKichThuoc, bg.gia
                FROM DichVuChamSoc dv
                JOIN BangGiaDichVu bg ON bg.maDichVu = dv.maDichVu
                WHERE dv.trangThai = 1 AND bg.phanLoaiKichThuoc = ?
                  AND bg.ngayApDung <= ?
                  AND (bg.ngayHetHieuLuc IS NULL OR bg.ngayHetHieuLuc >= ?)
                ORDER BY dv.tenDichVu
                """;
        List<ServiceOption> rows = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, petSize);
            statement.setDate(2, Date.valueOf(date));
            statement.setDate(3, Date.valueOf(date));
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(new ServiceOption(result.getString("maDichVu"), result.getString("tenDichVu"),
                            result.getString("phanLoaiKichThuoc"), result.getDouble("gia")));
                }
            }
            return rows;
        } catch (SQLException exception) {
            throw databaseError("Không tải được dịch vụ và bảng giá", exception);
        }
    }

    public AppointmentDraft findAppointmentDraft(String appointmentId) {
        String sql = """
                SELECT lh.maLichHen, kh.maKhachHang, kh.hoTen, kh.soDienThoai,
                       tc.maThuCung, tc.tenThuCung, tc.loaiThuCung, tc.canNang
                FROM LichHen lh
                JOIN KhachHang kh ON kh.maKhachHang = lh.maKhachHang
                JOIN ThuCung tc ON tc.maThuCung = lh.maThuCung AND tc.maKhachHang = lh.maKhachHang
                WHERE lh.maLichHen = ? AND lh.trangThai IN (N'Đã đến', N'Đang thực hiện', N'Hoàn thành')
                """;
        try (Connection connection = DatabaseManager.getConnection()) {
            CustomerOption customer;
            PetOption pet;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, appointmentId);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        throw new IllegalArgumentException("Không tìm thấy lịch hẹn có thể lập hóa đơn.");
                    }
                    customer = new CustomerOption(result.getString("maKhachHang"), result.getString("hoTen"),
                            result.getString("soDienThoai"));
                    pet = new PetOption(result.getString("maThuCung"), result.getString("maKhachHang"),
                            result.getString("tenThuCung"), result.getString("loaiThuCung"),
                            result.getDouble("canNang"));
                }
            }

            List<InvoiceItem> items = new ArrayList<>();
            String serviceSql = """
                    SELECT ct.maDichVu, dv.tenDichVu, ct.donGia
                    FROM ChiTietLichHen ct
                    JOIN DichVuChamSoc dv ON dv.maDichVu = ct.maDichVu
                    WHERE ct.maLichHen = ?
                    ORDER BY dv.tenDichVu
                    """;
            try (PreparedStatement statement = connection.prepareStatement(serviceSql)) {
                statement.setString(1, appointmentId);
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        items.add(new InvoiceItem(null, result.getString("maDichVu"),
                                result.getString("tenDichVu"), 1, result.getDouble("donGia"), true));
                    }
                }
            }
            if (items.isEmpty()) throw new IllegalArgumentException("Lịch hẹn chưa có dịch vụ để đưa vào hóa đơn.");
            return new AppointmentDraft(appointmentId, customer, pet, List.copyOf(items));
        } catch (SQLException exception) {
            throw databaseError("Không tải được thông tin lịch hẹn", exception);
        }
    }

    public String save(Invoice invoice) {
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (invoice.getStatus() == Invoice.Status.PAID) {
                    decreaseInventory(connection, invoice.getItems());
                }
                String invoiceId = nextInvoiceId(connection);
                insertInvoice(connection, invoiceId, invoice);
                insertInvoiceItems(connection, invoiceId, invoice.getItems());
                if (invoice.getStatus() == Invoice.Status.PAID && invoice.getMaLichHen() != null) {
                    updateAppointmentStatus(connection, invoice.getMaLichHen());
                }
                connection.commit();
                return invoiceId;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                if (exception instanceof SQLException sqlException) {
                    throw databaseError("Không lưu được hóa đơn", sqlException);
                }
                throw exception;
            }
        } catch (SQLException exception) {
            throw databaseError("Không thể kết nối để lưu hóa đơn", exception);
        }
    }

    private void decreaseInventory(Connection connection, List<InvoiceItem> items) throws SQLException {
        String sql = """
                UPDATE SanPham
                SET soLuongTon = soLuongTon - ?
                WHERE maSanPham = ? AND trangThai = 1 AND soLuongTon >= ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (InvoiceItem item : items) {
                if (item.getLoaiDong() != InvoiceItem.LineType.SAN_PHAM) continue;
                statement.setInt(1, item.getSoLuong());
                statement.setString(2, item.getMaSanPham());
                statement.setInt(3, item.getSoLuong());
                if (statement.executeUpdate() != 1) {
                    throw new IllegalStateException("Sản phẩm " + item.getTenHienThi()
                            + " đã ngừng bán hoặc không đủ tồn kho.");
                }
            }
        }
    }

    private void insertInvoice(Connection connection, String invoiceId, Invoice invoice) throws SQLException {
        String sql = """
                INSERT INTO HoaDon (maHoaDon, maLichHen, maNhanVien, maKhachHang, maThuCung,
                    ngayLap, hinhThucThanhToan, trangThaiThanhToan, maKhuyenMai, tongTien,
                    maNhanVienThu, giamGia, giamTheoKhuyenMai, thue, ghiChu, ngayThanhToan)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, invoiceId);
            setNullableString(statement, 2, invoice.getMaLichHen());
            statement.setString(3, invoice.getMaNhanVien());
            statement.setString(4, invoice.getMaKhachHang());
            setNullableString(statement, 5, invoice.getMaThuCung());
            statement.setDate(6, Date.valueOf(invoice.getNgayLap()));
            setNullableString(statement, 7, invoice.getHinhThucThanhToan());
            statement.setString(8, dbStatus(invoice.getStatus()));
            setNullableString(statement, 9, invoice.getMaKhuyenMai());
            statement.setBigDecimal(10, java.math.BigDecimal.valueOf(invoice.tinhTongThanhToan()));
            setNullableString(statement, 11, invoice.getMaNhanVienThu());
            statement.setBigDecimal(12, java.math.BigDecimal.valueOf(invoice.getGiamGia()));
            statement.setBigDecimal(13, java.math.BigDecimal.valueOf(invoice.getGiamTheoKhuyenMai()));
            statement.setBigDecimal(14, java.math.BigDecimal.valueOf(invoice.getThue()));
            setNullableString(statement, 15, invoice.getGhiChu());
            if (invoice.getStatus() == Invoice.Status.PAID) {
                statement.setTimestamp(16, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            } else {
                statement.setNull(16, Types.TIMESTAMP);
            }
            statement.executeUpdate();
        }
    }

    private void insertInvoiceItems(Connection connection, String invoiceId, List<InvoiceItem> items)
            throws SQLException {
        String productSql = """
                INSERT INTO ChiTietHoaDon_SanPham (maHoaDon, maSanPham, soLuong, donGia, tenHienThi, khoa)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        String serviceSql = """
                INSERT INTO ChiTietHoaDon_DichVu (maHoaDon, maDichVu, soLuong, donGia, tenHienThi, khoa)
                VALUES (?, ?, 1, ?, ?, ?)
                """;
        for (InvoiceItem item : items) {
            String sql = item.getLoaiDong() == InvoiceItem.LineType.SAN_PHAM ? productSql : serviceSql;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, invoiceId);
                if (item.getLoaiDong() == InvoiceItem.LineType.SAN_PHAM) {
                    statement.setString(2, item.getMaSanPham());
                    statement.setInt(3, item.getSoLuong());
                    statement.setBigDecimal(4, java.math.BigDecimal.valueOf(item.getDonGia()));
                    statement.setString(5, item.getTenHienThi());
                    statement.setBoolean(6, item.isKhoa());
                } else {
                    statement.setString(2, item.getMaDichVu());
                    statement.setBigDecimal(3, java.math.BigDecimal.valueOf(item.getDonGia()));
                    statement.setString(4, item.getTenHienThi());
                    statement.setBoolean(5, item.isKhoa());
                }
                statement.executeUpdate();
            }
        }
    }

    private void updateAppointmentStatus(Connection connection, String appointmentId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE LichHen SET trangThai = N'Đã thanh toán' WHERE maLichHen = ?")) {
            statement.setString(1, appointmentId);
            if (statement.executeUpdate() != 1) throw new IllegalStateException("Không cập nhật được lịch hẹn.");
        }
    }

    private String nextInvoiceId(Connection connection) throws SQLException {
        String datePart = LocalDate.now().format(ID_DATE);
        String sql = "SELECT ISNULL(MAX(TRY_CONVERT(INT, RIGHT(maHoaDon, 6))), 0) "
                + "FROM HoaDon WITH (UPDLOCK, HOLDLOCK) WHERE maHoaDon LIKE ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "HD-" + datePart + "-%");
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return "HD-" + datePart + "-" + String.format("%06d", result.getInt(1) + 1);
            }
        }
    }

    private static String dbStatus(Invoice.Status status) {
        return switch (status) {
            case PENDING -> "Chưa thanh toán";
            case PAID -> "Đã thanh toán";
            case CANCELLED -> "Đã hủy";
        };
    }

    private static String pattern(String filter) {
        String value = filter == null ? "" : filter.trim();
        return "%" + value.replace("[", "[[]").replace("%", "[%]").replace("_", "[_]") + "%";
    }

    private static void setNullableString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) statement.setNull(index, Types.VARCHAR);
        else statement.setString(index, value.trim());
    }

    private static IllegalStateException databaseError(String message, SQLException exception) {
        return new IllegalStateException(message + ": " + exception.getMessage(), exception);
    }
}