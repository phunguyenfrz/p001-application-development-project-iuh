package com.petshop.dao;

import com.petshop.entity.ThongKeTongQuan;
import com.petshop.util.DatabaseManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Truy xuất các chỉ số thống kê nhanh và dữ liệu biểu đồ cho màn hình Tổng quan (Dashboard).
 * Tuân thủ Bộ chỉ thị kỹ thuật IUH: tiền tệ sử dụng kiểu nguyên thủy double (0% BigDecimal).
 */
public class ThongKeDAO {

    private static final DateTimeFormatter DINH_DANG_NGAY_NGAN = DateTimeFormatter.ofPattern("dd/MM");

    public ThongKeTongQuan layThongKeHomNay() {
        ThongKeTongQuan thongKe = new ThongKeTongQuan();
        LocalDate homNay = LocalDate.now();

        String sqlHoaDon = """
                SELECT COUNT(*) AS soHoaDon,
                       ISNULL(SUM(tongTien), 0) AS tongDoanhThu
                FROM HoaDon
                WHERE ngayLap = ?
                  AND trangThaiThanhToan = N'Đã thanh toán'
                """;

        String sqlDoiTra = """
                SELECT ISNULL(SUM(tienHoan), 0) AS tongTienHoan,
                       ISNULL(SUM(tienBu), 0) AS tongTienBu
                FROM PhieuDoiTra
                WHERE ngayLap = ?
                  AND trangThai IN (N'Đã duyệt', N'Hoàn thành')
                """;

        String sqlLichHen = """
                SELECT COUNT(*) AS tongLichHen,
                       SUM(CASE WHEN trangThai IN (N'Đã đặt', N'Đã xác nhận', N'Đang thực hiện') THEN 1 ELSE 0 END) AS choXuLy
                FROM LichHen
                WHERE ngayHen = ?
                  AND trangThai <> N'Đã hủy'
                """;

        String sqlTonKho = """
                SELECT COUNT(*) AS soSapHet
                FROM SanPham
                WHERE trangThai = 1
                  AND soLuongTon <= nguongTonToiThieu
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            double doanhThuHomNay = 0.0;

            try (PreparedStatement ps = conn.prepareStatement(sqlHoaDon)) {
                ps.setDate(1, Date.valueOf(homNay));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        thongKe.setSoHoaDonHomNay(rs.getInt("soHoaDon"));
                        doanhThuHomNay = rs.getDouble("tongDoanhThu");
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlDoiTra)) {
                ps.setDate(1, Date.valueOf(homNay));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        double tienHoan = rs.getDouble("tongTienHoan");
                        double tienBu = rs.getDouble("tongTienBu");
                        doanhThuHomNay = Math.max(0.0, doanhThuHomNay - tienHoan + tienBu);
                    }
                }
            } catch (SQLException ignored) {
                // Bỏ qua nếu bảng PhieuDoiTra chưa có cột tienHoan/tienBu
            }

            thongKe.setDoanhThuHomNay(doanhThuHomNay);

            try (PreparedStatement ps = conn.prepareStatement(sqlLichHen)) {
                ps.setDate(1, Date.valueOf(homNay));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        thongKe.setTongSoLichHenHomNay(rs.getInt("tongLichHen"));
                        thongKe.setSoLichHenChoXuLy(rs.getInt("choXuLy"));
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlTonKho);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    thongKe.setSoSanPhamSapHet(rs.getInt("soSapHet"));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải dữ liệu thống kê tổng quan từ cơ sở dữ liệu", e);
        }

        return thongKe;
    }

    /**
     * Lấy thống kê doanh thu thuần 7 ngày gần nhất để vẽ biểu đồ cột (BarChart).
     * Key: Ngày định dạng "dd/MM", Value: Doanh thu (double).
     */
    public Map<String, Double> layDoanhThu7NgayGanNhat() {
        Map<String, Double> ketQua = new LinkedHashMap<>();
        LocalDate homNay = LocalDate.now();
        LocalDate tuNgay = homNay.minusDays(6);

        for (int i = 0; i < 7; i++) {
            LocalDate ngay = tuNgay.plusDays(i);
            ketQua.put(ngay.format(DINH_DANG_NGAY_NGAN), 0.0);
        }

        String sqlHoaDon = """
                SELECT ngayLap, ISNULL(SUM(tongTien), 0) AS doanhThu
                FROM HoaDon
                WHERE ngayLap BETWEEN ? AND ?
                  AND trangThaiThanhToan = N'Đã thanh toán'
                GROUP BY ngayLap
                """;

        String sqlDoiTra = """
                SELECT ngayLap,
                       ISNULL(SUM(tienHoan), 0) AS tongHoan,
                       ISNULL(SUM(tienBu), 0) AS tongBu
                FROM PhieuDoiTra
                WHERE ngayLap BETWEEN ? AND ?
                  AND trangThai IN (N'Đã duyệt', N'Hoàn thành')
                GROUP BY ngayLap
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) return ketQua;

            try (PreparedStatement ps = conn.prepareStatement(sqlHoaDon)) {
                ps.setDate(1, Date.valueOf(tuNgay));
                ps.setDate(2, Date.valueOf(homNay));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Date d = rs.getDate("ngayLap");
                        if (d != null) {
                            String nhanNgay = d.toLocalDate().format(DINH_DANG_NGAY_NGAN);
                            ketQua.put(nhanNgay, rs.getDouble("doanhThu"));
                        }
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlDoiTra)) {
                ps.setDate(1, Date.valueOf(tuNgay));
                ps.setDate(2, Date.valueOf(homNay));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Date d = rs.getDate("ngayLap");
                        if (d != null) {
                            String nhanNgay = d.toLocalDate().format(DINH_DANG_NGAY_NGAN);
                            double hienTai = ketQua.getOrDefault(nhanNgay, 0.0);
                            double dieuChinh = hienTai - rs.getDouble("tongHoan") + rs.getDouble("tongBu");
                            ketQua.put(nhanNgay, Math.max(0.0, dieuChinh));
                        }
                    }
                }
            } catch (SQLException ignored) {}
        } catch (SQLException ignored) {}

        return ketQua;
    }

    /**
     * Lấy cơ cấu doanh thu Bán hàng (Sản phẩm) vs Dịch vụ Spa để vẽ biểu đồ tròn (PieChart).
     */
    public Map<String, Double> layCoCauDoanhThuBanHangVaDichVu() {
        Map<String, Double> coCau = new LinkedHashMap<>();
        double doanhThuBanHang = 0.0;
        double doanhThuDichVu = 0.0;

        String sqlBanHang = """
                SELECT ISNULL(SUM(ct.soLuong * ct.donGia), 0) AS tongBanHang
                FROM ChiTietHoaDon_SanPham ct
                JOIN HoaDon hd ON ct.maHoaDon = hd.maHoaDon
                WHERE hd.trangThaiThanhToan = N'Đã thanh toán'
                """;

        String sqlDichVu = """
                SELECT ISNULL(SUM(ct.soLuong * ct.donGia), 0) AS tongDichVu
                FROM ChiTietHoaDon_DichVu ct
                JOIN HoaDon hd ON ct.maHoaDon = hd.maHoaDon
                WHERE hd.trangThaiThanhToan = N'Đã thanh toán'
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn != null) {
                try (PreparedStatement ps = conn.prepareStatement(sqlBanHang);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        doanhThuBanHang = rs.getDouble("tongBanHang");
                    }
                }
                try (PreparedStatement ps = conn.prepareStatement(sqlDichVu);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        doanhThuDichVu = rs.getDouble("tongDichVu");
                    }
                }
            }
        } catch (SQLException ignored) {}

        coCau.put("Bán hàng", Math.max(0.0, doanhThuBanHang));
        coCau.put("Dịch vụ Spa", Math.max(0.0, doanhThuDichVu));
        return coCau;
    }
}
