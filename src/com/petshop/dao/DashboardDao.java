package com.petshop.dao;

import com.petshop.util.DatabaseManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object phục vụ màn hình Tổng quan (Dashboard Overview).
 * Truy vấn thống kê thời gian thực từ cơ sở dữ liệu:
 * - Doanh thu hôm nay (HoaDon có trangThaiThanhToan = N'Đã thanh toán')
 * - Số lịch hẹn hôm nay (LichHen có ngayHen = hôm nay và trangThai <> N'Đã hủy')
 * - Số sản phẩm sắp hết hàng (soLuongTon <= nguongTonToiThieu)
 * - Danh sách hoạt động giao dịch gần đây (Hóa đơn thanh toán & Lịch hẹn dịch vụ)
 */
public class DashboardDao {

    public record DashboardStats(
            double todayRevenue,
            int todayAppointments,
            int lowStockProducts
    ) {}

    public record RecentActivity(
            String type,            // "INVOICE" hoặc "APPOINTMENT"
            String code,            // Mã hóa đơn hoặc mã lịch hẹn
            LocalDate date,
            LocalTime time,
            double amount,
            String status,
            String customerName,
            String employeeName
    ) {}

    /**
     * Lấy các chỉ số thống kê KPI hôm nay từ CSDL theo thời gian thực.
     */
    public DashboardStats getTodayStats() {
        double revenue = 0.0;
        int appointments = 0;
        int lowStock = 0;

        // 1. Doanh thu hôm nay: Tổng tiền các hóa đơn 'Đã thanh toán' trong ngày hiện tại
        String sqlRevenue = """
                SELECT ISNULL(SUM(tongTien), 0)
                FROM HoaDon
                WHERE CAST(ngayLap AS DATE) = CAST(GETDATE() AS DATE)
                  AND trangThaiThanhToan = N'Đã thanh toán'
                """;

        // 2. Lịch hẹn hôm nay: Số lịch hẹn ngày hôm nay chưa bị hủy
        String sqlAppointments = """
                SELECT COUNT(*)
                FROM LichHen
                WHERE CAST(ngayHen AS DATE) = CAST(GETDATE() AS DATE)
                  AND trangThai <> N'Đã hủy'
                """;

        // 3. Sản phẩm sắp hết hàng: Tồn kho <= ngưỡng an toàn và đang kinh doanh
        String sqlLowStock = """
                SELECT COUNT(*)
                FROM SanPham
                WHERE soLuongTon <= nguongTonToiThieu
                  AND trangThai = 1
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sqlRevenue);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    revenue = rs.getDouble(1);
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlAppointments);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    appointments = rs.getInt(1);
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlLowStock);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lowStock = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn số liệu Dashboard: " + e.getMessage());
        }

        return new DashboardStats(revenue, appointments, lowStock);
    }

    /**
     * Lấy danh sách các hoạt động / giao dịch gần đây nhất trong hệ thống.
     */
    public List<RecentActivity> getRecentActivities(int limit) {
        List<RecentActivity> list = new ArrayList<>();

        String sql = """
                SELECT TOP (?) * FROM (
                    SELECT 'INVOICE' AS loaiHoatDong,
                           hd.maHoaDon AS maGiaoDich,
                           hd.ngayLap AS ngayThucHien,
                           CAST('00:00:00' AS TIME) AS gioThucHien,
                           hd.tongTien AS soTien,
                           hd.trangThaiThanhToan AS trangThai,
                           ISNULL(kh.hoTen, N'Khách lẻ vãng lai') AS tenKhachHang,
                           nv.hoTen AS tenNhanVien
                    FROM HoaDon hd
                    JOIN NhanVien nv ON nv.maNhanVien = hd.maNhanVien
                    LEFT JOIN KhachHang kh ON kh.maKhachHang = hd.maKhachHang

                    UNION ALL

                    SELECT 'APPOINTMENT' AS loaiHoatDong,
                           lh.maLichHen AS maGiaoDich,
                           lh.ngayHen AS ngayThucHien,
                           lh.gioHen AS gioThucHien,
                           0.0 AS soTien,
                           lh.trangThai,
                           kh.hoTen AS tenKhachHang,
                           nv.hoTen AS tenNhanVien
                    FROM LichHen lh
                    JOIN NhanVien nv ON nv.maNhanVien = lh.maNhanVien
                    JOIN KhachHang kh ON kh.maKhachHang = lh.maKhachHang
                ) AS HoatDongGop
                ORDER BY maGiaoDich DESC
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 6);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date d = rs.getDate("ngayThucHien");
                    java.sql.Time t = rs.getTime("gioThucHien");
                    list.add(new RecentActivity(
                            rs.getString("loaiHoatDong"),
                            rs.getString("maGiaoDich"),
                            d != null ? d.toLocalDate() : LocalDate.now(),
                            t != null ? t.toLocalTime() : LocalTime.MIN,
                            rs.getDouble("soTien"),
                            rs.getString("trangThai"),
                            rs.getString("tenKhachHang"),
                            rs.getString("tenNhanVien")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn hoạt động gần đây: " + e.getMessage());
        }

        return list;
    }
}

