package com.petshop.dao;

import com.petshop.model.HoatDongGanDay;
import com.petshop.model.ThongKeTongQuan;
import com.petshop.util.DatabaseManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp DAO truy vấn dữ liệu thống kê tổng quan và hoạt động từ CSDL SQL Server.
 */
public class ThongKeDAO {

    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 VNĐ");

    /**
     * Lấy các số liệu tổng quan cho thẻ KPI:
     * - Doanh thu hôm nay (đã trừ tiền hoàn đổi trả trong ngày và cộng bù)
     * - Lịch hẹn hôm nay
     * - Sản phẩm cần nhập thêm (tồn <= ngưỡng tối thiểu)
     */
    public ThongKeTongQuan layThongKeTongQuan() {
        BigDecimal doanhThu = BigDecimal.ZERO;
        int soLichHen = 0;
        int soSanPhamSapHet = 0;

        // 1. Tính tổng doanh thu hôm nay từ HoaDon trừ đi tiền hoàn đổi trả trong ngày
        String sqlDoanhThu = """
                SELECT COALESCE(
                    (SELECT SUM(tongTien) FROM dbo.HoaDon WHERE ngayLap = CAST(GETDATE() AS DATE) AND trangThaiThanhToan = N'Đã thanh toán'), 0
                ) - COALESCE(
                    (SELECT SUM(tienHoan) FROM dbo.PhieuDoiTra WHERE ngayLap = CAST(GETDATE() AS DATE) AND trangThai IN (N'Đã duyệt', N'Hoàn thành')), 0
                ) + COALESCE(
                    (SELECT SUM(tienBu) FROM dbo.PhieuDoiTra WHERE ngayLap = CAST(GETDATE() AS DATE) AND trangThai IN (N'Đã duyệt', N'Hoàn thành')), 0
                ) AS doanhThu
                """;

        // 2. Đếm số lịch hẹn hôm nay (chưa bị hủy)
        String sqlLichHen = """
                SELECT COUNT(*) AS soLuong
                FROM dbo.LichHen
                WHERE ngayHen = CAST(GETDATE() AS DATE)
                  AND trangThai != N'Đã hủy'
                """;

        // 3. Đếm sản phẩm có tồn kho <= ngưỡng tồn tối thiểu
        String sqlSanPham = """
                SELECT COUNT(*) AS soLuong
                FROM dbo.SanPham
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
                    if (doanhThu == null || doanhThu.compareTo(BigDecimal.ZERO) < 0) {
                        doanhThu = BigDecimal.ZERO;
                    }
                }
            } catch (Exception ex) {
                // Fallback nếu chưa có cột tienHoan/tienBu
                try (PreparedStatement psFallback = conn.prepareStatement(
                        "SELECT ISNULL(SUM(tongTien), 0) AS doanhThu FROM dbo.HoaDon WHERE ngayLap = CAST(GETDATE() AS DATE) AND trangThaiThanhToan = N'Đã thanh toán'")) {
                    try (ResultSet rsFb = psFallback.executeQuery()) {
                        if (rsFb.next()) doanhThu = rsFb.getBigDecimal("doanhThu");
                    }
                } catch (Exception ignored) {}
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
     * Lấy danh sách lịch hẹn và hoạt động phát sinh trong ngày:
     * - Lịch hẹn chăm sóc thú cưng
     * - Hóa đơn thanh toán phát sinh trong ngày
     * - Phiếu đổi trả hàng phát sinh trong ngày
     */
    public List<HoatDongGanDay> layHoatDongGanDay() {
        List<HoatDongGanDay> danhSach = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                return danhSach;
            }

            // 1. Lịch hẹn dịch vụ hôm nay
            String sqlLichHen = """
                    SELECT TOP 10
                           lh.maLichHen,
                           COALESCE(CONVERT(VARCHAR(5), lh.gioHen, 108), 'Hôm nay') AS thoiGian,
                           N'Lịch hẹn dịch vụ' AS loaiHoatDong,
                           kh.hoTen AS tenKhach,
                           tc.tenThuCung,
                           lh.trangThai AS trangThai
                    FROM dbo.LichHen lh
                    LEFT JOIN dbo.KhachHang kh ON kh.maKhachHang = lh.maKhachHang
                    LEFT JOIN dbo.ThuCung tc ON tc.maThuCung = lh.maThuCung
                    WHERE lh.ngayHen = CAST(GETDATE() AS DATE)
                    ORDER BY lh.gioHen ASC
                    """;
            try (PreparedStatement ps = conn.prepareStatement(sqlLichHen);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tg = rs.getString("thoiGian");
                    String khach = rs.getString("tenKhach");
                    String pet = rs.getString("tenThuCung");
                    String maLh = rs.getString("maLichHen");
                    String tt = rs.getString("trangThai");

                    String doiTuong = pet != null ? pet + (khach != null ? " (" + khach + ")" : "") : (khach != null ? khach : "Khách hàng");
                    String chiTiet = "Lịch hẹn " + maLh;

                    danhSach.add(new HoatDongGanDay(
                            tg != null ? tg : "Hôm nay",
                            "Lịch hẹn dịch vụ",
                            doiTuong,
                            chiTiet,
                            tt != null ? tt : "Đang chờ"
                    ));
                }
            } catch (Exception ignored) {}

            // 2. Hóa đơn thanh toán hôm nay
            String sqlHoaDon = """
                    SELECT TOP 10
                           hd.maHoaDon,
                           hd.hinhThucThanhToan,
                           hd.tongTien,
                           hd.trangThaiThanhToan,
                           kh.hoTen AS tenKhach,
                           kh.soDienThoai
                    FROM dbo.HoaDon hd
                    LEFT JOIN dbo.KhachHang kh ON kh.maKhachHang = hd.maKhachHang
                    WHERE hd.ngayLap = CAST(GETDATE() AS DATE)
                    ORDER BY hd.maHoaDon DESC
                    """;
            try (PreparedStatement ps = conn.prepareStatement(sqlHoaDon);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maHd = rs.getString("maHoaDon");
                    String ht = rs.getString("hinhThucThanhToan");
                    BigDecimal tien = rs.getBigDecimal("tongTien");
                    String tt = rs.getString("trangThaiThanhToan");
                    String khach = rs.getString("tenKhach");
                    String sdt = rs.getString("soDienThoai");

                    String doiTuong = khach != null ? khach + (sdt != null ? " (" + sdt + ")" : "") : "Khách vãng lai";
                    String tienChuoi = tien != null ? DINH_DANG_TIEN.format(tien) : "0 VNĐ";
                    String chiTiet = maHd + (ht != null ? " - " + ht : "") + " (" + tienChuoi + ")";

                    danhSach.add(new HoatDongGanDay(
                            "Hôm nay",
                            "Hóa đơn thanh toán",
                            doiTuong,
                            chiTiet,
                            tt != null ? tt : "Đã thanh toán"
                    ));
                }
            } catch (Exception ignored) {}

            // 3. Phiếu đổi trả hàng hôm nay
            String sqlDoiTra = """
                    SELECT TOP 10
                           pdt.maPhieuDoiTra,
                           pdt.maHoaDon,
                           pdt.lyDo,
                           pdt.tienHoan,
                           pdt.tienBu,
                           pdt.loaiXuLy,
                           pdt.trangThai,
                           kh.hoTen AS tenKhach,
                           kh.soDienThoai
                    FROM dbo.PhieuDoiTra pdt
                    LEFT JOIN dbo.KhachHang kh ON kh.maKhachHang = pdt.maKhachHang
                    WHERE pdt.ngayLap = CAST(GETDATE() AS DATE)
                    ORDER BY pdt.maPhieuDoiTra DESC
                    """;
            try (PreparedStatement ps = conn.prepareStatement(sqlDoiTra);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maPhieu = rs.getString("maPhieuDoiTra");
                    String maHd = rs.getString("maHoaDon");
                    String lyDo = rs.getString("lyDo");
                    BigDecimal tienHoan = rs.getBigDecimal("tienHoan");
                    BigDecimal tienBu = rs.getBigDecimal("tienBu");
                    String tt = rs.getString("trangThai");
                    String khach = rs.getString("tenKhach");
                    String sdt = rs.getString("soDienThoai");

                    String doiTuong = khach != null ? khach + (sdt != null ? " (" + sdt + ")" : "") : "Khách hàng";
                    if (lyDo == null || lyDo.isBlank()) lyDo = "Đổi trả hàng";

                    String chiTiet = maPhieu + " (HĐ: " + (maHd != null ? maHd : "--") + ") - " + lyDo;
                    if (tienHoan != null && tienHoan.compareTo(BigDecimal.ZERO) > 0) {
                        chiTiet += " [Hoàn: " + DINH_DANG_TIEN.format(tienHoan) + "]";
                    } else if (tienBu != null && tienBu.compareTo(BigDecimal.ZERO) > 0) {
                        chiTiet += " [Khách bù: " + DINH_DANG_TIEN.format(tienBu) + "]";
                    }

                    danhSach.add(new HoatDongGanDay(
                            "Hôm nay",
                            "Phiếu đổi trả",
                            doiTuong,
                            chiTiet,
                            tt != null ? tt : "Đã duyệt"
                    ));
                }
            } catch (Exception ignored) {}

        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi truy vấn hoạt động gần đây: " + e.getMessage(), e);
        }

        return danhSach;
    }
}
