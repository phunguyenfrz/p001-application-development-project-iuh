package com.petshop.entity;

import java.time.LocalDate;

/**
 * Thực thể Lịch sử giao dịch của khách hàng (ánh xạ thông qua Object Reference HoaDon).
 * Tuân thủ 100% Điều 1 & Điều 2 IUH:
 * - Không lưu biến thành viên tongTien, không truyền tongTien vào constructor.
 * - Toàn bộ thông tin và tổng tiền được ủy quyền/tính toán trực tiếp từ đối tượng HoaDon.
 */
public class LichSuGiaoDich {

    /** Tham chiếu đối tượng Hóa đơn của giao dịch */
    private final HoaDon hoaDon;

    public LichSuGiaoDich(HoaDon hoaDon) {
        this.hoaDon = hoaDon != null ? hoaDon : new HoaDon();
    }

    public LichSuGiaoDich(String maHoaDon, String maKhachHang, String tenKhachHang,
                          LocalDate ngayLap, String tenNhanVien, String hinhThucThanhToan,
                          String trangThaiThanhToan) {
        this.hoaDon = new HoaDon();
        this.hoaDon.setMaHoaDon(maHoaDon);
        this.hoaDon.setMaKhachHang(maKhachHang);
        this.hoaDon.setTenKhachHang(tenKhachHang);
        this.hoaDon.setNgayLap(ngayLap);
        this.hoaDon.setTenNhanVien(tenNhanVien);
        this.hoaDon.setHinhThucThanhToan(hinhThucThanhToan);
        this.hoaDon.setTrangThaiThanhToan(trangThaiThanhToan);
    }

    public HoaDon getHoaDon() {
        return hoaDon;
    }

    public String getMaHoaDon() {
        return hoaDon != null ? hoaDon.getMaHoaDon() : null;
    }

    public KhachHang getKhachHang() {
        return hoaDon != null ? hoaDon.getKhachHang() : null;
    }

    public String getMaKhachHang() {
        return hoaDon != null ? hoaDon.getMaKhachHang() : null;
    }

    public String getTenKhachHang() {
        return hoaDon != null ? hoaDon.getTenKhachHang() : null;
    }

    public LocalDate getNgayLap() {
        return hoaDon != null ? hoaDon.getNgayLap() : null;
    }

    public NhanVien getNhanVien() {
        return hoaDon != null ? hoaDon.getNhanVien() : null;
    }

    public String getTenNhanVien() {
        return hoaDon != null ? hoaDon.getTenNhanVien() : null;
    }

    public String getHinhThucThanhToan() {
        return hoaDon != null ? hoaDon.getHinhThucThanhToan() : null;
    }

    public String getTrangThaiThanhToan() {
        return hoaDon != null ? hoaDon.getTrangThaiThanhToan() : null;
    }

    /**
     * Phương thức tự tính toán tổng tiền thanh toán từ đối tượng HoaDon (Điều 2 IUH).
     */
    public double tinhTongTien() {
        return hoaDon != null ? hoaDon.tinhTongThanhToan() : 0.0;
    }

    public double getTongTien() {
        return tinhTongTien();
    }
}

