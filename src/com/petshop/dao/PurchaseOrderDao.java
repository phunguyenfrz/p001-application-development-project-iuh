package com.petshop.dao;

import com.petshop.model.PurchaseOrder;
import com.petshop.util.DbConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class PurchaseOrderDao implements PurchaseOrderRepository {
    private record Header(String id, String supplierId, String supplierName,
                          String employeeId, String employeeName, java.time.LocalDate createdOn,
                          java.math.BigDecimal total, String status) {
    }

    @Override
    public List<PurchaseOrder> findAll() {
        String sql = """
                SELECT dh.maDonHang, dh.maNhaCungCap, ncc.tenNhaCungCap,
                       dh.maNhanVien, nv.hoTen, dh.ngayLap, dh.tongTien, dh.trangThai
                FROM DonHang dh
                JOIN NhaCungCap ncc ON ncc.maNhaCungCap = dh.maNhaCungCap
                JOIN NhanVien nv ON nv.maNhanVien = dh.maNhanVien
                ORDER BY dh.ngayLap DESC, dh.maDonHang DESC
                """;
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            List<Header> headers = new ArrayList<>();
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    headers.add(new Header(rows.getString("maDonHang"), rows.getString("maNhaCungCap"),
                            rows.getString("tenNhaCungCap"), rows.getString("maNhanVien"),
                            rows.getString("hoTen"), rows.getDate("ngayLap").toLocalDate(),
                            rows.getBigDecimal("tongTien"), rows.getString("trangThai")));
                }
            }
            List<PurchaseOrder> orders = headers.stream().map(header -> new PurchaseOrder(
                    header.id(), header.supplierId(), header.supplierName(), header.employeeId(),
                    header.employeeName(), header.createdOn(), header.total(), header.status(),
                    linesUnchecked(connection, header.id()))).toList();
            return List.copyOf(orders);
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể tải đơn nhập hàng.", exception);
        }
    }

    private List<PurchaseOrder.Line> linesUnchecked(Connection connection, String id) {
        try {
            return lines(connection, id);
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể tải chi tiết đơn nhập " + id + ".", exception);
        }
    }

    @Override
    public List<PurchaseOrder.Choice> suppliers() {
        return choices("SELECT maNhaCungCap, tenNhaCungCap, CAST(0 AS decimal(18,2)) FROM NhaCungCap ORDER BY tenNhaCungCap");
    }

    @Override
    public List<PurchaseOrder.Choice> products() {
        return choices("SELECT maSanPham, tenSanPham, giaBan FROM SanPham WHERE trangThai = 1 ORDER BY tenSanPham");
    }

    @Override
    public void create(PurchaseOrder order) {
        String headerSql = """
                INSERT INTO DonHang (maDonHang, maNhaCungCap, maNhanVien, ngayLap, tongTien, trangThai)
                VALUES (?, ?, ?, ?, ?, N'Chờ nhận hàng')
                """;
        String lineSql = """
                INSERT INTO ChiTietDonHang (maDonHang, maSanPham, soLuong, donGia)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection connection = DbConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement header = connection.prepareStatement(headerSql);
                 PreparedStatement line = connection.prepareStatement(lineSql)) {
                header.setString(1, order.id());
                header.setString(2, order.supplierId());
                header.setString(3, order.employeeId());
                header.setDate(4, Date.valueOf(order.createdOn()));
                header.setBigDecimal(5, order.total());
                header.executeUpdate();
                for (PurchaseOrder.Line item : order.lines()) {
                    line.setString(1, order.id());
                    line.setString(2, item.productId());
                    line.setInt(3, item.quantity());
                    line.setBigDecimal(4, item.unitCost());
                    line.addBatch();
                }
                line.executeBatch();
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể lập đơn nhập hàng.", exception);
        }
    }

    @Override
    public void receive(String id) {
        try (Connection connection = DbConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement status = connection.prepareStatement("""
                    UPDATE DonHang SET trangThai = N'Hoàn tất'
                    WHERE maDonHang = ? AND trangThai = N'Chờ nhận hàng'
                    """);
                 PreparedStatement stock = connection.prepareStatement("""
                    UPDATE sp SET soLuongTon = sp.soLuongTon + line.soLuong
                    FROM SanPham sp
                    JOIN (SELECT maSanPham, SUM(soLuong) AS soLuong
                          FROM ChiTietDonHang WHERE maDonHang = ? GROUP BY maSanPham) line
                      ON line.maSanPham = sp.maSanPham
                    """)) {
                status.setString(1, id);
                if (status.executeUpdate() == 0) {
                    throw new IllegalArgumentException("Đơn không còn ở trạng thái Chờ nhận hàng.");
                }
                stock.setString(1, id);
                stock.executeUpdate();
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể xác nhận nhận hàng.", exception);
        }
    }

    private List<PurchaseOrder.Choice> choices(String sql) {
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            List<PurchaseOrder.Choice> result = new ArrayList<>();
            while (rows.next()) {
                result.add(new PurchaseOrder.Choice(rows.getString(1), rows.getString(2),
                        rows.getBigDecimal(3)));
            }
            return List.copyOf(result);
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể tải lựa chọn đơn nhập.", exception);
        }
    }

    private List<PurchaseOrder.Line> lines(Connection connection, String id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT ct.maSanPham, sp.tenSanPham, ct.soLuong, ct.donGia
                FROM ChiTietDonHang ct JOIN SanPham sp ON sp.maSanPham = ct.maSanPham
                WHERE ct.maDonHang = ?
                ORDER BY sp.tenSanPham
                """)) {
            statement.setString(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                List<PurchaseOrder.Line> result = new ArrayList<>();
                while (rows.next()) {
                    result.add(new PurchaseOrder.Line(rows.getString("maSanPham"),
                            rows.getString("tenSanPham"), rows.getInt("soLuong"),
                            rows.getBigDecimal("donGia")));
                }
                return List.copyOf(result);
            }
        }
    }
}
