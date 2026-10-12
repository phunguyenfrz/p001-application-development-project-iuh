package com.petshop.dao;

import com.petshop.model.InvoiceSummary;
import com.petshop.util.DbConnection;
import com.petshop.util.TextSearch;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class InvoiceHistoryDao implements InvoiceHistoryRepository {
    private record Header(String id, java.time.LocalDateTime createdAt, String employeeName,
                          String customerName, String paymentMethod, String paymentStatus,
                          java.math.BigDecimal total) {
    }

    @Override
    public List<InvoiceSummary> search(String keyword, java.time.LocalDate from,
                                       java.time.LocalDate through) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        String sql = """
                SELECT hd.maHoaDon, hd.ngayLap, nv.hoTen AS tenNhanVien,
                       COALESCE(kh.hoTen, N'Khách vãng lai') AS tenKhachHang,
                       hd.hinhThucThanhToan, hd.trangThaiThanhToan, hd.tongTien
                FROM HoaDon hd
                JOIN NhanVien nv ON nv.maNhanVien = hd.maNhanVien
                LEFT JOIN KhachHang kh ON kh.maKhachHang = hd.maKhachHang
                WHERE CAST(hd.ngayLap AS date) BETWEEN ? AND ? AND
                """ + TextSearch.likePredicate(keyword, "hd.maHoaDon", "nv.hoTen",
                "kh.hoTen", "kh.soDienThoai") + """
                ORDER BY hd.ngayLap DESC
                """;
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(from));
            statement.setDate(2, Date.valueOf(through));
            TextSearch.bindLikeParameters(statement, 3, keyword, 4);
            List<Header> headers = new ArrayList<>();
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    headers.add(new Header(rows.getString("maHoaDon"),
                            rows.getTimestamp("ngayLap").toLocalDateTime(),
                            rows.getString("tenNhanVien"), rows.getString("tenKhachHang"),
                            rows.getString("hinhThucThanhToan"),
                            rows.getString("trangThaiThanhToan"),
                            rows.getBigDecimal("tongTien")));
                }
            }
            List<InvoiceSummary> invoices = headers.stream()
                    .map(header -> new InvoiceSummary(header.id(), header.createdAt(),
                            header.employeeName(), header.customerName(), header.paymentMethod(),
                            header.paymentStatus(), header.total(), findLinesUnchecked(connection, header.id())))
                    .toList();
            return List.copyOf(invoices);
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể tra cứu lịch sử hóa đơn.", exception);
        }
    }

    private List<InvoiceSummary.Line> findLinesUnchecked(Connection connection, String id) {
        try {
            return findLines(connection, id);
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể tải chi tiết hóa đơn " + id + ".", exception);
        }
    }

    private List<InvoiceSummary.Line> findLines(Connection connection, String id)
            throws SQLException {
        String sql = """
                SELECT category, description, quantity, unitPrice
                FROM (
                    SELECT N'Sản phẩm' AS category, sp.tenSanPham AS description,
                           ct.soLuong AS quantity, ct.donGia AS unitPrice
                    FROM ChiTietHoaDon_SanPham ct
                    JOIN SanPham sp ON sp.maSanPham = ct.maSanPham
                    WHERE ct.maHoaDon = ?
                    UNION ALL
                    SELECT N'Dịch vụ', dv.tenDichVu, ct.soLuong, ct.donGia
                    FROM ChiTietHoaDon_DichVu ct
                    JOIN DichVuChamSoc dv ON dv.maDichVu = ct.maDichVu
                    WHERE ct.maHoaDon = ?
                ) detail
                ORDER BY category, description
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            statement.setString(2, id);
            try (ResultSet rows = statement.executeQuery()) {
                List<InvoiceSummary.Line> lines = new ArrayList<>();
                while (rows.next()) {
                    int quantity = rows.getInt("quantity");
                    java.math.BigDecimal unitPrice = rows.getBigDecimal("unitPrice");
                    lines.add(new InvoiceSummary.Line(rows.getString("category"),
                            rows.getString("description"), quantity, unitPrice,
                            unitPrice.multiply(java.math.BigDecimal.valueOf(quantity))));
                }
                return List.copyOf(lines);
            }
        }
    }
}
