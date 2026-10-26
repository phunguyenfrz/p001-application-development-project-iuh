package com.petshop.dao;

import com.petshop.model.DonNhap;
import com.petshop.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DonHangDAO {

    public List<DonNhap> layDonChoNhanHang() {
        String sql = """
                SELECT dh.maDonHang, ncc.tenNhaCungCap, dh.ngayLap, dh.tongTien
                FROM DonHang dh
                JOIN NhaCungCap ncc ON ncc.maNhaCungCap = dh.maNhaCungCap
                WHERE dh.trangThai = N'Chờ nhận hàng'
                ORDER BY dh.ngayLap DESC, dh.maDonHang DESC
                """;
        List<DonNhap> danhSach = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                danhSach.add(new DonNhap(
                        resultSet.getString("maDonHang"),
                        resultSet.getString("tenNhaCungCap"),
                        resultSet.getDate("ngayLap").toLocalDate(),
                        resultSet.getBigDecimal("tongTien")));
            }
            return danhSach;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi tải danh sách đơn nhập chờ nhận: " + e.getMessage(), e);
        }
    }

    public List<DonNhap.ChiTiet> layChiTietDonHang(String maDonHang) {
        String sql = """
                SELECT ct.maSanPham, sp.tenSanPham, ct.soLuong, ct.donGia
                FROM ChiTietDonHang ct
                JOIN SanPham sp ON sp.maSanPham = ct.maSanPham
                WHERE ct.maDonHang = ?
                ORDER BY sp.tenSanPham
                """;
        List<DonNhap.ChiTiet> danhSach = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maDonHang);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    danhSach.add(new DonNhap.ChiTiet(
                            resultSet.getString("maSanPham"),
                            resultSet.getString("tenSanPham"),
                            resultSet.getInt("soLuong"),
                            resultSet.getBigDecimal("donGia")));
                }
            }
            return danhSach;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi tải chi tiết đơn nhập: " + e.getMessage(), e);
        }
    }

    public boolean xacNhanNhanHang(String maDonHang) {
        String sqlCapNhatTrangThai = """
                UPDATE DonHang
                SET trangThai = N'Hoàn tất'
                WHERE maDonHang = ? AND trangThai = N'Chờ nhận hàng'
                """;
        String sqlLayChiTiet = "SELECT maSanPham, soLuong FROM ChiTietDonHang WHERE maDonHang = ?";
        String sqlCongTonKho = "UPDATE SanPham SET soLuongTon = soLuongTon + ? WHERE maSanPham = ?";

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement updateStatus = connection.prepareStatement(sqlCapNhatTrangThai)) {
                updateStatus.setString(1, maDonHang);
                if (updateStatus.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }

                int soDongChiTiet = 0;
                try (PreparedStatement selectDetails = connection.prepareStatement(sqlLayChiTiet);
                     ResultSet details = bindAndExecute(selectDetails, maDonHang);
                     PreparedStatement updateStock = connection.prepareStatement(sqlCongTonKho)) {
                    while (details.next()) {
                        soDongChiTiet++;
                        updateStock.setInt(1, details.getInt("soLuong"));
                        updateStock.setString(2, details.getString("maSanPham"));
                        if (updateStock.executeUpdate() != 1) {
                            throw new IllegalStateException("Không tìm thấy sản phẩm " + details.getString("maSanPham"));
                        }
                    }
                }
                if (soDongChiTiet == 0) {
                    throw new IllegalStateException("Đơn nhập không có sản phẩm nên không thể nhận hàng.");
                }

                connection.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                rollback(connection, e);
                if (e instanceof SQLException sqlException) {
                    throw new IllegalStateException("Lỗi xác nhận nhận hàng: " + sqlException.getMessage(), sqlException);
                }
                throw (RuntimeException) e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi kết nối khi xác nhận nhận hàng: " + e.getMessage(), e);
        }
    }

    private ResultSet bindAndExecute(PreparedStatement statement, String maDonHang) throws SQLException {
        statement.setString(1, maDonHang);
        return statement.executeQuery();
    }

    private void rollback(Connection connection, Exception cause) {
        try {
            connection.rollback();
        } catch (SQLException rollbackError) {
            cause.addSuppressed(rollbackError);
        }
    }
}