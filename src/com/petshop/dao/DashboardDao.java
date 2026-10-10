package com.petshop.dao;

import com.petshop.util.DatabaseManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardDao {
    public record ProductStock(String code, String name, int quantity, int threshold) {
    }

    public record BestSeller(String type, String name, int quantity) {
    }

    public record Appointment(String code, LocalTime time, String pet, String services, String status) {
    }

    public record AllergyPet(String pet, String type, String allergy, String customer) {
    }

    public record PendingOrder(String code, String supplier, BigDecimal total) {
    }

    public record RecentInvoice(String code, LocalDate date, String customer,
                                BigDecimal total, String status) {
    }

    public record ManagerDashboard(BigDecimal revenueToday, Map<LocalDate, BigDecimal> weeklyRevenue,
                                   int appointmentsToday, List<ProductStock> lowStock,
                                   List<BestSeller> bestSellers) {
    }

    public record SalesDashboard(int invoicesToday, BigDecimal revenueToday, List<ProductStock> lowStock,
                                 int pendingOrderCount,
                                 List<PendingOrder> pendingOrders, List<Appointment> unpaidAppointments,
                                 List<RecentInvoice> recentInvoices) {
    }

    public record CareDashboard(List<Appointment> appointmentsToday, int awaitingConfirmation,
                                List<AllergyPet> petsWithAllergiesToday) {
    }

    public ManagerDashboard loadManagerDashboard() {
        BigDecimal revenueToday = queryRevenueToday();
        Map<LocalDate, BigDecimal> weeklyRevenue = queryWeeklyRevenue();
        weeklyRevenue.put(LocalDate.now(), revenueToday);
        return new ManagerDashboard(
                revenueToday,
                weeklyRevenue,
                queryAppointmentCount(),
                queryLowStock(),
                queryBestSellers());
    }

    public SalesDashboard loadSalesDashboard() {
        return new SalesDashboard(
                queryInvoiceCountToday(),
                queryRevenueToday(),
                queryLowStock(),
                queryPendingOrderCount(),
                queryPendingOrders(),
                queryUnpaidCompletedAppointments(),
                queryRecentInvoices());
    }

    public CareDashboard loadCareDashboard(String employeeId) {
        return new CareDashboard(
                queryMyAppointmentsToday(employeeId),
                queryAppointmentsAwaitingConfirmation(employeeId),
                queryPetsWithAllergiesToday(employeeId));
    }

    private BigDecimal queryRevenueToday() {
        String sql = """
                SELECT COALESCE(SUM(tongTien), 0) AS total
                FROM HoaDon
                WHERE ngayLap = CAST(GETDATE() AS DATE)
                  AND trangThaiThanhToan = N'Đã thanh toán'
                """;
        return querySingleDecimal(sql);
    }

    private Map<LocalDate, BigDecimal> queryWeeklyRevenue() {
        String sql = """
                DECLARE @today DATE = CAST(GETDATE() AS DATE);
                DECLARE @monday DATE = DATEADD(day,
                    -(DATEDIFF(day, CONVERT(DATE, '19000101'), @today) % 7), @today);
                WITH weekDays AS (
                    SELECT DATEADD(day, dayOffset, @monday) AS businessDate
                    FROM (VALUES (0), (1), (2), (3), (4), (5), (6)) dayOffsets(dayOffset)
                )
                SELECT weekDays.businessDate, COALESCE(SUM(hd.tongTien), 0) AS total
                FROM weekDays
                LEFT JOIN HoaDon hd
                  ON hd.ngayLap = weekDays.businessDate
                 AND hd.trangThaiThanhToan = N'Đã thanh toán'
                GROUP BY weekDays.businessDate
                ORDER BY weekDays.businessDate
                """;
        Map<LocalDate, BigDecimal> result = new LinkedHashMap<>();
        query(sql, statement -> {
        }, rs -> result.put(rs.getDate("businessDate").toLocalDate(), rs.getBigDecimal("total")));
        return result;
    }

    private int queryAppointmentCount() {
        return querySingleInt("""
                SELECT COUNT(*) AS total
                FROM LichHen
                WHERE ngayHen = CAST(GETDATE() AS DATE)
                  AND trangThai <> N'Đã hủy'
                """, statement -> {
        });
    }

    private int queryInvoiceCountToday() {
        return querySingleInt("""
                SELECT COUNT(*) AS total
                FROM HoaDon
                WHERE ngayLap = CAST(GETDATE() AS DATE)
                  AND trangThaiThanhToan <> N'Đã hủy'
                """, statement -> {
        });
    }

    private int queryAppointmentsAwaitingConfirmation(String employeeId) {
        return querySingleInt("""
                SELECT COUNT(*) AS total
                FROM LichHen
                WHERE ngayHen = CAST(GETDATE() AS DATE)
                  AND trangThai = N'Đã đặt'
                  AND maNhanVien = ?
                """, statement -> statement.setString(1, employeeId));
    }

    private List<ProductStock> queryLowStock() {
        String sql = """
                SELECT maSanPham, tenSanPham, soLuongTon, nguongTonToiThieu
                FROM SanPham
                WHERE soLuongTon <= nguongTonToiThieu
                ORDER BY soLuongTon, tenSanPham
                """;
        List<ProductStock> result = new ArrayList<>();
        query(sql, statement -> {
        }, rs -> result.add(new ProductStock(
                rs.getString("maSanPham"),
                rs.getString("tenSanPham"),
                rs.getInt("soLuongTon"),
                rs.getInt("nguongTonToiThieu"))));
        return result;
    }

    private List<BestSeller> queryBestSellers() {
        String sql = """
                SELECT TOP (5) itemType, itemName, SUM(quantity) AS quantity
                FROM (
                    SELECT N'Sản phẩm' AS itemType, sp.tenSanPham AS itemName, ct.soLuong AS quantity
                    FROM ChiTietHoaDon_SanPham ct
                    JOIN HoaDon hd ON hd.maHoaDon = ct.maHoaDon
                    JOIN SanPham sp ON sp.maSanPham = ct.maSanPham
                    WHERE hd.trangThaiThanhToan = N'Đã thanh toán'
                    UNION ALL
                    SELECT N'Dịch vụ', dv.tenDichVu, ct.soLuong
                    FROM ChiTietHoaDon_DichVu ct
                    JOIN HoaDon hd ON hd.maHoaDon = ct.maHoaDon
                    JOIN DichVuChamSoc dv ON dv.maDichVu = ct.maDichVu
                    WHERE hd.trangThaiThanhToan = N'Đã thanh toán'
                ) soldItems
                GROUP BY itemType, itemName
                ORDER BY SUM(quantity) DESC, itemName
                """;
        List<BestSeller> result = new ArrayList<>();
        query(sql, statement -> {
        }, rs -> result.add(new BestSeller(
                rs.getString("itemType"), rs.getString("itemName"), rs.getInt("quantity"))));
        return result;
    }

    private List<PendingOrder> queryPendingOrders() {
        String sql = """
                SELECT TOP (10) dh.maDonHang, ncc.tenNhaCungCap, dh.tongTien
                FROM DonHang dh
                JOIN NhaCungCap ncc ON ncc.maNhaCungCap = dh.maNhaCungCap
                WHERE dh.trangThai = N'Chờ nhận hàng'
                ORDER BY dh.ngayLap DESC, dh.maDonHang DESC
                """;
        List<PendingOrder> result = new ArrayList<>();
        query(sql, statement -> {
        }, rs -> result.add(new PendingOrder(
                rs.getString("maDonHang"),
                rs.getString("tenNhaCungCap"),
                rs.getBigDecimal("tongTien"))));
        return result;
    }

    private int queryPendingOrderCount() {
        return querySingleInt("""
                SELECT COUNT(*) AS total
                FROM DonHang
                WHERE trangThai = N'Chờ nhận hàng'
                """, statement -> {
        });
    }

    private List<Appointment> queryUnpaidCompletedAppointments() {
        String sql = """
                SELECT TOP (10) lh.maLichHen, lh.gioHen, tc.tenThuCung,
                       COALESCE(dv.services, N'') AS services, lh.trangThai
                FROM LichHen lh
                JOIN ThuCung tc ON tc.maThuCung = lh.maThuCung
                OUTER APPLY (
                    SELECT STRING_AGG(dv.tenDichVu, N', ') AS services
                    FROM ChiTietLichHen ctlh
                    JOIN DichVuChamSoc dv ON dv.maDichVu = ctlh.maDichVu
                    WHERE ctlh.maLichHen = lh.maLichHen
                ) dv
                WHERE lh.ngayHen = CAST(GETDATE() AS DATE)
                  AND lh.trangThai = N'Hoàn thành'
                  AND NOT EXISTS (
                      SELECT 1 FROM HoaDon hd WHERE hd.maLichHen = lh.maLichHen
                  )
                ORDER BY lh.gioHen
                """;
        List<Appointment> result = new ArrayList<>();
        query(sql, statement -> {
        }, rs -> result.add(mapAppointment(rs)));
        return result;
    }

    private List<RecentInvoice> queryRecentInvoices() {
        String sql = """
                SELECT TOP (10) hd.maHoaDon, hd.ngayLap,
                       COALESCE(kh.hoTen, N'Khách vãng lai') AS customerName,
                       hd.tongTien, hd.trangThaiThanhToan
                FROM HoaDon hd
                LEFT JOIN KhachHang kh ON kh.maKhachHang = hd.maKhachHang
                ORDER BY hd.ngayLap DESC, hd.maHoaDon DESC
                """;
        List<RecentInvoice> result = new ArrayList<>();
        query(sql, statement -> {
        }, rs -> result.add(new RecentInvoice(
                rs.getString("maHoaDon"),
                rs.getDate("ngayLap").toLocalDate(),
                rs.getString("customerName"),
                rs.getBigDecimal("tongTien"),
                rs.getString("trangThaiThanhToan"))));
        return result;
    }

    private List<Appointment> queryMyAppointmentsToday(String employeeId) {
        String sql = """
                SELECT lh.maLichHen, lh.gioHen, tc.tenThuCung,
                       COALESCE(dv.services, N'') AS services, lh.trangThai
                FROM LichHen lh
                JOIN ThuCung tc ON tc.maThuCung = lh.maThuCung
                OUTER APPLY (
                    SELECT STRING_AGG(dv.tenDichVu, N', ') AS services
                    FROM ChiTietLichHen ctlh
                    JOIN DichVuChamSoc dv ON dv.maDichVu = ctlh.maDichVu
                    WHERE ctlh.maLichHen = lh.maLichHen
                ) dv
                WHERE lh.ngayHen = CAST(GETDATE() AS DATE)
                  AND lh.maNhanVien = ?
                ORDER BY lh.gioHen
                """;
        List<Appointment> result = new ArrayList<>();
        query(sql, statement -> statement.setString(1, employeeId),
                rs -> result.add(mapAppointment(rs)));
        return result;
    }

    private List<AllergyPet> queryPetsWithAllergiesToday(String employeeId) {
        String sql = """
                SELECT DISTINCT tc.tenThuCung, COALESCE(tc.loaiThuCung, N'Không rõ') AS loaiThuCung,
                       tc.diUng, kh.hoTen
                FROM LichHen lh
                JOIN ThuCung tc ON tc.maThuCung = lh.maThuCung
                JOIN KhachHang kh ON kh.maKhachHang = lh.maKhachHang
                WHERE lh.ngayHen = CAST(GETDATE() AS DATE)
                  AND lh.maNhanVien = ?
                  AND NULLIF(LTRIM(RTRIM(tc.diUng)), N'') IS NOT NULL
                  AND tc.diUng <> N'Không có'
                ORDER BY tc.tenThuCung
                """;
        List<AllergyPet> result = new ArrayList<>();
        query(sql, statement -> statement.setString(1, employeeId),
                rs -> result.add(new AllergyPet(
                        rs.getString("tenThuCung"),
                        rs.getString("loaiThuCung"),
                        rs.getString("diUng"),
                        rs.getString("hoTen"))));
        return result;
    }

    private BigDecimal querySingleDecimal(String sql) {
        final BigDecimal[] value = {BigDecimal.ZERO};
        query(sql, statement -> {
        }, rs -> value[0] = rs.getBigDecimal("total"));
        return value[0];
    }

    private int querySingleInt(String sql, StatementBinder binder) {
        final int[] value = {0};
        query(sql, binder, rs -> value[0] = rs.getInt("total"));
        return value[0];
    }

    private Appointment mapAppointment(ResultSet rs) throws SQLException {
        return new Appointment(
                rs.getString("maLichHen"),
                rs.getTime("gioHen").toLocalTime(),
                rs.getString("tenThuCung"),
                rs.getString("services"),
                rs.getString("trangThai"));
    }

    private void query(String sql, StatementBinder binder, RowMapper mapper) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    mapper.map(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải dữ liệu bảng điều khiển: " + e.getMessage(), e);
        }
    }

    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }

    @FunctionalInterface
    private interface RowMapper {
        void map(ResultSet resultSet) throws SQLException;
    }
}
