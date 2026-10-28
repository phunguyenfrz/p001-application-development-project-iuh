package com.petshop.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Thực thể Lịch sử giao dịch của khách hàng (ánh xạ thông tin hóa đơn từ bảng HoaDon).
 * Dùng để hiển thị danh sách hóa đơn mua sắm sản phẩm và sử dụng dịch vụ của khách hàng.
 */
public class LichSuGiaoDich {

    /** Mã định danh hóa đơn (HD-yyyyMMdd-xxxxxx) */
    private final String maHoaDon;

    /** Mã khách hàng sở hữu giao dịch */
    private final String maKhachHang;

    /** Họ tên khách hàng */
    private final String tenKhachHang;

    /** Ngày lập hóa đơn */
    private final LocalDate ngayLap;

    /** Tên nhân viên phụ trách lập hóa đơn */
    private final String tenNhanVien;

    /** Hình thức thanh toán: Tiền mặt, Chuyển khoản, POS */
    private final String hinhThucThanhToan;

    /** Trạng thái thanh toán: Đã thanh toán, Chưa thanh toán, Đã hủy */
    private final String trangThaiThanhToan;

    /** Tổng thanh toán thực tế của hóa đơn */
    private final BigDecimal tongTien;

    public LichSuGiaoDich(String maHoaDon, String maKhachHang, String tenKhachHang,
                          LocalDate ngayLap, String tenNhanVien, String hinhThucThanhToan,
                          String trangThaiThanhToan, BigDecimal tongTien) {
        this.maHoaDon = maHoaDon;
        this.maKhachHang = maKhachHang;
        this.tenKhachHang = tenKhachHang;
        this.ngayLap = ngayLap;
        this.tenNhanVien = tenNhanVien;
        this.hinhThucThanhToan = hinhThucThanhToan;
        this.trangThaiThanhToan = trangThaiThanhToan;
        this.tongTien = tongTien != null ? tongTien : BigDecimal.ZERO;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public LocalDate getNgayLap() {
        return ngayLap;
    }

    public String getTenNhanVien() {
        return tenNhanVien;
    }

    public String getHinhThucThanhToan() {
        return hinhThucThanhToan;
    }

    public String getTrangThaiThanhToan() {
        return trangThaiThanhToan;
    }

    public BigDecimal getTongTien() {
        return tongTien;
    }
}

