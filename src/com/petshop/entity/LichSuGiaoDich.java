package com.petshop.entity;

import java.time.LocalDate;

/**
 * Thực thể Lịch sử giao dịch của khách hàng (ánh xạ thông tin hóa đơn từ bảng HoaDon).
 * Dùng để hiển thị danh sách hóa đơn mua sắm sản phẩm và sử dụng dịch vụ của khách hàng.
 * Tuân thủ 100% Điều 1 IUH: Sử dụng kiểu nguyên thủy double cho tiền tệ (0% BigDecimal).
 */
public class LichSuGiaoDich {

    /** Mã định danh hóa đơn (HD-yyyyMMdd-xxxxxx) */
    private final String maHoaDon;

    /** Tham chiếu đối tượng Khách hàng sở hữu giao dịch */
    private final KhachHang khachHang;

    /** Ngày lập hóa đơn */
    private final LocalDate ngayLap;

    /** Tham chiếu đối tượng Nhân viên phụ trách lập hóa đơn */
    private final NhanVien nhanVien;

    /** Hình thức thanh toán: Tiền mặt, Chuyển khoản, POS */
    private final String hinhThucThanhToan;

    /** Trạng thái thanh toán: Đã thanh toán, Chưa thanh toán, Đã hủy */
    private final String trangThaiThanhToan;

    /** Tổng thanh toán thực tế của hóa đơn (double nguyên thủy) */
    private final double tongTien;

    public LichSuGiaoDich(String maHoaDon, String maKhachHang, String tenKhachHang,
                          LocalDate ngayLap, String tenNhanVien, String hinhThucThanhToan,
                          String trangThaiThanhToan, double tongTien) {
        this.maHoaDon = maHoaDon;
        this.khachHang = new KhachHang();
        this.khachHang.setMaKhachHang(maKhachHang);
        this.khachHang.setHoTen(tenKhachHang);
        this.ngayLap = ngayLap;
        this.nhanVien = new NhanVien();
        this.nhanVien.setHoTen(tenNhanVien);
        this.hinhThucThanhToan = hinhThucThanhToan;
        this.trangThaiThanhToan = trangThaiThanhToan;
        this.tongTien = Math.max(0.0, tongTien);
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public String getMaKhachHang() {
        return khachHang != null ? khachHang.getMaKhachHang() : null;
    }

    public String getTenKhachHang() {
        return khachHang != null ? khachHang.getHoTen() : null;
    }

    public LocalDate getNgayLap() {
        return ngayLap;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public String getTenNhanVien() {
        return nhanVien != null ? nhanVien.getHoTen() : null;
    }

    public String getHinhThucThanhToan() {
        return hinhThucThanhToan;
    }

    public String getTrangThaiThanhToan() {
        return trangThaiThanhToan;
    }

    public double getTongTien() {
        return tongTien;
    }
}

