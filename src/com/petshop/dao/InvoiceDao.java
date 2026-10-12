package com.petshop.dao;

import com.petshop.model.InvoiceData.AppointmentOption;
import com.petshop.model.InvoiceData.CustomerOption;
import com.petshop.model.InvoiceData.InvoiceDraft;
import com.petshop.model.InvoiceData.InvoiceLine;
import com.petshop.model.InvoiceData.InvoiceReceipt;
import com.petshop.model.InvoiceData.ProductOption;
import com.petshop.model.InvoiceData.ReceiptLine;
import com.petshop.util.DbConnection;
import com.petshop.util.TextSearch;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class InvoiceDao implements InvoiceRepository {
    @Override
    public List<CustomerOption> findCustomers() {
        String sql = """
                SELECT maKhachHang, hoTen, soDienThoai
                FROM KhachHang
                ORDER BY hoTen
                """;
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            List<CustomerOption> customers = new ArrayList<>();
            while (result.next()) {
                customers.add(new CustomerOption(result.getString(1),
                        result.getString(2), result.getString(3)));
            }
            return List.copyOf(customers);
        } catch (SQLException exception) {
            throw databaseError("Không thể tải danh sách khách hàng.", exception);
        }
    }

    @Override
    public List<ProductOption> searchProducts(String keyword) {
        String query = keyword == null ? "" : keyword.trim();
        String sql = """
                SELECT """ + (query.isEmpty() ? "TOP (30) " : "") + """
                maSanPham, tenSanPham, maVach, giaBan, soLuongTon
                FROM SanPham
                WHERE trangThai = 1 AND soLuongTon > 0
                """
                + (query.isEmpty() ? "" : " AND " + TextSearch.likePredicate(query,
                        "maSanPham", "maVach", "tenSanPham"))
                + """
                ORDER BY CASE WHEN maVach = ? THEN 0 ELSE 1 END, tenSanPham
                """;
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (!query.isEmpty()) {
                TextSearch.bindLikeParameters(statement, 1, query, 3);
            }
            statement.setString(TextSearch.termCount(query) * 3 + 1, query);
            try (ResultSet result = statement.executeQuery()) {
                List<ProductOption> products = new ArrayList<>();
                while (result.next()) {
                    products.add(new ProductOption(result.getString(1), result.getString(2),
                            result.getString(3), result.getBigDecimal(4), result.getInt(5)));
                }
                return List.copyOf(products);
            }
        } catch (SQLException exception) {
            throw databaseError("Không thể tìm sản phẩm.", exception);
        }
    }

    @Override
    public List<AppointmentOption> findCompletedAppointments() {
        String sql = """
                SELECT lh.maLichHen, lh.maKhachHang, kh.hoTen, tc.tenThuCung, lh.ngayHen,
                       COALESCE(SUM(ct.donGia), 0) AS serviceTotal
                FROM LichHen lh
                JOIN KhachHang kh ON kh.maKhachHang = lh.maKhachHang
                JOIN ThuCung tc ON tc.maThuCung = lh.maThuCung
                LEFT JOIN ChiTietLichHen ct ON ct.maLichHen = lh.maLichHen
                WHERE lh.trangThai = N'Hoàn thành'
                  AND NOT EXISTS (SELECT 1 FROM HoaDon hd WHERE hd.maLichHen = lh.maLichHen)
                GROUP BY lh.maLichHen, lh.maKhachHang, kh.hoTen, tc.tenThuCung, lh.ngayHen
                ORDER BY lh.ngayHen DESC, lh.gioHen DESC
                """;
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            List<AppointmentOption> appointments = new ArrayList<>();
            while (result.next()) {
                appointments.add(new AppointmentOption(result.getString(1), result.getString(2),
                        result.getString(3), result.getString(4),
                        result.getDate(5).toLocalDate(), result.getBigDecimal(6)));
            }
            return List.copyOf(appointments);
        } catch (SQLException exception) {
            throw databaseError("Không thể tải lịch hẹn đã hoàn thành.", exception);
        }
    }

    @Override
    public InvoiceReceipt create(InvoiceDraft draft, String employeeId) {
        Objects.requireNonNull(draft, "Thông tin hóa đơn không được để trống.");
        validateDraft(draft);
        try (Connection connection = DbConnection.getConnection()) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            try {
                AppointmentCustomer appointment = resolveAppointment(connection, draft);
                String customerId = appointment == null
                        ? draft.customerId() : appointment.customerId();
                String invoiceId = nextInvoiceId(connection);
                List<ReceiptLine> receiptLines = new ArrayList<>();
                BigDecimal total = BigDecimal.ZERO;
                if (appointment != null) {
                    List<ReceiptLine> services = appointmentServices(connection, appointment.id());
                    receiptLines.addAll(services);
                    total = total.add(services.stream().map(ReceiptLine::amount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                }
                List<PricedLine> products = lockAndPriceProducts(connection, draft.lines());
                for (PricedLine line : products) {
                    BigDecimal amount = line.price().multiply(BigDecimal.valueOf(line.quantity()));
                    receiptLines.add(new ReceiptLine(line.name(), line.quantity(),
                            line.price(), amount));
                    total = total.add(amount);
                }
                LocalDateTime createdAt = LocalDateTime.now();
                insertInvoice(connection, invoiceId, appointment == null
                                ? null : appointment.id(),
                        employeeId, customerId, draft.paymentMethod(), createdAt, total);
                insertProductLines(connection, invoiceId, products);
                if (appointment != null) {
                    copyAppointmentServices(connection, invoiceId, appointment.id());
                }
                String customerName = customerId == null ? "Khách vãng lai"
                        : customerName(connection, customerId);
                connection.commit();
                return new InvoiceReceipt(invoiceId, createdAt, customerName,
                        draft.paymentMethod(), total, receiptLines);
            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            }
        } catch (SQLException exception) {
            throw databaseError("Không thể lập hóa đơn. Dữ liệu chưa được lưu.", exception);
        }
    }

    private void validateDraft(InvoiceDraft draft) {
        if (draft.lines().isEmpty() && draft.appointmentId() == null) {
            throw new IllegalArgumentException("Hóa đơn cần có ít nhất một mặt hàng hoặc lịch hẹn.");
        }
        if (!List.of("Tiền mặt", "Chuyển khoản", "POS").contains(draft.paymentMethod())) {
            throw new IllegalArgumentException("Hình thức thanh toán không hợp lệ.");
        }
        if (draft.lines().stream().anyMatch(line -> line.quantity() < 1)) {
            throw new IllegalArgumentException("Số lượng mỗi mặt hàng phải lớn hơn 0.");
        }
        if (draft.lines().stream().map(InvoiceLine::productId).distinct().count()
                != draft.lines().size()) {
            throw new IllegalArgumentException("Mỗi sản phẩm chỉ được thêm vào hóa đơn một lần.");
        }
    }

    private AppointmentCustomer resolveAppointment(Connection connection, InvoiceDraft draft)
            throws SQLException {
        if (draft.appointmentId() == null) {
            return null;
        }
        String sql = """
                SELECT maLichHen, maKhachHang
                FROM LichHen WITH (UPDLOCK, HOLDLOCK)
                WHERE maLichHen = ? AND trangThai = N'Hoàn thành'
                  AND NOT EXISTS (SELECT 1 FROM HoaDon WHERE maLichHen = ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, draft.appointmentId());
            statement.setString(2, draft.appointmentId());
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalArgumentException(
                            "Lịch hẹn không ở trạng thái Hoàn thành hoặc đã được lập hóa đơn.");
                }
                String customerId = result.getString(2);
                if (draft.customerId() != null && !draft.customerId().equals(customerId)) {
                    throw new IllegalArgumentException(
                            "Khách hàng không khớp với khách hàng của lịch hẹn.");
                }
                return new AppointmentCustomer(result.getString(1), customerId);
            }
        }
    }

    private String nextInvoiceId(Connection connection) throws SQLException {
        String prefix = "HD-" + LocalDate.now().toString().replace("-", "") + "-";
        String sql = """
                SELECT ISNULL(MAX(TRY_CONVERT(int, RIGHT(maHoaDon, 6))), 0)
                FROM HoaDon WITH (UPDLOCK, HOLDLOCK)
                WHERE maHoaDon LIKE ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, prefix + "%");
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                int next = result.getInt(1) + 1;
                if (next > 999_999) {
                    throw new IllegalStateException("Đã hết số hóa đơn trong ngày.");
                }
                return prefix + "%06d".formatted(next);
            }
        }
    }

    private List<PricedLine> lockAndPriceProducts(
            Connection connection, List<InvoiceLine> requested) throws SQLException {
        String sql = """
                SELECT tenSanPham, giaBan, soLuongTon, trangThai
                FROM SanPham WITH (UPDLOCK, ROWLOCK)
                WHERE maSanPham = ?
                """;
        List<PricedLine> products = new ArrayList<>();
        for (InvoiceLine line : requested) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, line.productId());
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        throw new IllegalArgumentException("Không tìm thấy sản phẩm: "
                                + line.productId());
                    }
                    if (!result.getBoolean("trangThai")
                            || result.getInt("soLuongTon") < line.quantity()) {
                        throw new IllegalArgumentException("Sản phẩm không hoạt động hoặc không đủ tồn: "
                                + result.getString("tenSanPham"));
                    }
                    products.add(new PricedLine(line.productId(), result.getString("tenSanPham"),
                            result.getBigDecimal("giaBan"), line.quantity()));
                }
            }
        }
        return List.copyOf(products);
    }

    private List<ReceiptLine> appointmentServices(Connection connection, String appointmentId)
            throws SQLException {
        String sql = """
                SELECT dv.tenDichVu, ct.donGia
                FROM ChiTietLichHen ct
                JOIN DichVuChamSoc dv ON dv.maDichVu = ct.maDichVu
                WHERE ct.maLichHen = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, appointmentId);
            try (ResultSet result = statement.executeQuery()) {
                List<ReceiptLine> lines = new ArrayList<>();
                while (result.next()) {
                    BigDecimal price = result.getBigDecimal(2);
                    lines.add(new ReceiptLine(result.getString(1), 1, price, price));
                }
                return List.copyOf(lines);
            }
        }
    }

    private void insertInvoice(
            Connection connection, String id, String appointmentId, String employeeId,
            String customerId, String paymentMethod, LocalDateTime createdAt, BigDecimal total)
            throws SQLException {
        String sql = """
                INSERT INTO HoaDon
                    (maHoaDon, maLichHen, maNhanVien, maKhachHang, ngayLap,
                     hinhThucThanhToan, trangThaiThanhToan, maKhuyenMai, tienGiam, tongTien)
                VALUES (?, ?, ?, ?, ?, ?, N'Đã thanh toán', NULL, 0, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            if (appointmentId == null) {
                statement.setNull(2, Types.VARCHAR);
            } else {
                statement.setString(2, appointmentId);
            }
            statement.setString(3, employeeId);
            if (customerId == null) {
                statement.setNull(4, Types.VARCHAR);
            } else {
                statement.setString(4, customerId);
            }
            statement.setTimestamp(5, java.sql.Timestamp.valueOf(createdAt));
            statement.setNString(6, paymentMethod);
            statement.setBigDecimal(7, total);
            statement.executeUpdate();
        }
    }

    private void insertProductLines(
            Connection connection, String invoiceId, List<PricedLine> products)
            throws SQLException {
        String detailSql = """
                INSERT INTO ChiTietHoaDon_SanPham(maHoaDon, maSanPham, soLuong, donGia)
                VALUES (?, ?, ?, ?)
                """;
        String stockSql = """
                UPDATE SanPham SET soLuongTon = soLuongTon - ?
                WHERE maSanPham = ? AND trangThai = 1 AND soLuongTon >= ?
                """;
        try (PreparedStatement detail = connection.prepareStatement(detailSql);
             PreparedStatement stock = connection.prepareStatement(stockSql)) {
            for (PricedLine line : products) {
                detail.setString(1, invoiceId);
                detail.setString(2, line.productId());
                detail.setInt(3, line.quantity());
                detail.setBigDecimal(4, line.price());
                detail.addBatch();
                stock.setInt(1, line.quantity());
                stock.setString(2, line.productId());
                stock.setInt(3, line.quantity());
                if (stock.executeUpdate() != 1) {
                    throw new IllegalStateException(
                            "Tồn kho vừa thay đổi; hãy tải lại sản phẩm và thử lại.");
                }
            }
            detail.executeBatch();
        }
    }

    private void copyAppointmentServices(
            Connection connection, String invoiceId, String appointmentId) throws SQLException {
        String sql = """
                INSERT INTO ChiTietHoaDon_DichVu(maHoaDon, maDichVu, soLuong, donGia)
                SELECT ?, maDichVu, 1, donGia
                FROM ChiTietLichHen WHERE maLichHen = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, invoiceId);
            statement.setString(2, appointmentId);
            statement.executeUpdate();
        }
    }

    private String customerName(Connection connection, String customerId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT hoTen FROM KhachHang WHERE maKhachHang = ?")) {
            statement.setString(1, customerId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalStateException("Không tìm thấy khách hàng của hóa đơn.");
                }
                return result.getString(1);
            }
        }
    }

    private void rollback(Connection connection, Exception original) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            original.addSuppressed(rollbackException);
        }
    }

    private IllegalStateException databaseError(String message, SQLException exception) {
        return new IllegalStateException(message, exception);
    }

    private record AppointmentCustomer(String id, String customerId) {
    }

    private record PricedLine(String productId, String name, BigDecimal price, int quantity) {
    }
}
