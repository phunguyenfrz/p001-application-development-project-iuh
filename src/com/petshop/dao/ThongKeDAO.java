package com.petshop.dao;

import com.petshop.model.HoatDongGanDay;
import com.petshop.model.ThongKeTongQuan;
import com.petshop.util.DatabaseManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp DAO truy vấn dữ liệu thống kê tổng quan và hoạt động từ CSDL SQL Server.
 */
public class ThongKeDAO {

    /**
     * Lấy các số liệu tổng quan cho thẻ KPI (doanh thu hôm nay, lịch hẹn hôm nay, sản phẩm sắp hết).
     */
    public ThongKeTongQuan layThongKeTongQuan() {
        BigDecimal doanhThu = BigDecimal.ZERO;
        int soLichHen = 0;
        int soSanPhamSapHet = 0;

        // 1. Tính tổng doanh thu hôm nay từ HoaDon (đã thanh toán)
        String sqlDoanhThu = """
                SELECT COALESCE(SUM(tongTien), 0) AS doanhThu
                FROM HoaDon
                WHERE ngayLap = CAST(GETDATE() AS DATE)
                  AND trangThaiThanhToan = N'Đã thanh toán'
                """;

        // 2. Đếm số lịch hẹn hôm nay (chưa bị hủy)
        String sqlLichHen = """
                SELECT COUNT(*) AS soLuong
                FROM LichHen
                WHERE ngayHen = CAST(GETDATE() AS DATE)
                  AND trangThai != N'Đã hủy'
                """;

        // 3. Đếm sản phẩm có tồn kho <= ngưỡng tồn tối thiểu
        String sqlSanPham = """
                SELECT COUNT(*) AS soLuong
                FROM SanPham
                WHERE soLuongTon <= nguongTonToiThieu
                  AND trangThai = 1
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                return new ThongKeTongQuan(doanhThu, soLichHen, soSanPhamSapHet);
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlDoanhThu);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    doanhThu = rs.getBigDecimal("doanhThu");
                    if (doanhThu == null) {
                        doanhThu = BigDecimal.ZERO;
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlLichHen);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    soLichHen = rs.getInt("soLuong");
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlSanPham);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    soSanPhamSapHet = rs.getInt("soLuong");
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi truy vấn thống kê tổng quan: " + e.getMessage(), e);
        }

        return new ThongKeTongQuan(doanhThu, soLichHen, soSanPhamSapHet);
    }

    /**
     * Lấy danh sách lịch hẹn và hoạt động gần đây trong ngày.
     */
    public List<HoatDongGanDay> layHoatDongGanDay() {
        List<HoatDongGanDay> danhSach = new ArrayList<>();
        String sql = """
                SELECT TOP 10 lh.maLichHen, kh.hoTen, tc.tenThuCung, lh.gioHen, lh.trangThai
                FROM LichHen lh
                JOIN KhachHang kh ON kh.maKhachHang = lh.maKhachHang
                JOIN ThuCung tc ON tc.maThuCung = lh.maThuCung
                WHERE lh.ngayHen = CAST(GETDATE() AS DATE)
                ORDER BY lh.gioHen ASC
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                return danhSach;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String khach = rs.getString("hoTen");
                    String thuCung = rs.getString("tenThuCung");
                    String gio = rs.getString("gioHen");
                    if (gio != null && gio.length() >= 5) {
                        gio = gio.substring(0, 5);
                    }
                    String trangThai = rs.getString("trangThai");

                    HoatDongGanDay hd = new HoatDongGanDay(
                            gio,
                            "Lịch hẹn chăm sóc",
                            thuCung + " (" + khach + ") - Trạng thái: " + trangThai
                    );
                    danhSach.add(hd);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi truy vấn hoạt động gần đây: " + e.getMessage(), e);
        }

        return danhSach;
    }
}
