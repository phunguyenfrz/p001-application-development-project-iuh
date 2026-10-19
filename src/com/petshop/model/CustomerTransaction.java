package com.petshop.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Thông tin một hóa đơn trong lịch sử giao dịch khách hàng.
 * Chỉ phục vụ tra cứu, không dùng để lập hoặc sửa hóa đơn.
 */
public class CustomerTransaction {

    // Mã hóa đơn trong bảng HoaDon.
    private final String maHoaDon;

    // Mã khách hàng sở hữu giao dịch.
    private final String maKhachHang;

    // Họ tên khách hàng để hiển thị trong hộp chi tiết.
    private final String tenKhachHang;

    // Ngày lập hóa đơn.
    private final LocalDate ngayLap;

    // Họ tên nhân viên lập hóa đơn.
    private final String tenNhanVien;

    // Hình thức thanh toán; có thể null nếu chưa có thông tin.
    private final String hinhThucThanhToan;

    // Trạng thái: Chưa thanh toán, Đã thanh toán hoặc Đã hủy.
    private final String trangThaiThanhToan;

    // Tổng thanh toán thực tế được lưu trong HoaDon.tongTien.
    private final BigDecimal tongTien;

    /**
     * Khởi tạo một giao dịch từ dữ liệu truy vấn.
     *
     * @param maHoaDon Mã hóa đơn.
     * @param maKhachHang Mã khách hàng.
     * @param tenKhachHang Họ tên khách hàng.
     * @param ngayLap Ngày lập hóa đơn.
     * @param tenNhanVien Họ tên nhân viên lập.
     * @param hinhThucThanhToan Hình thức thanh toán.
     * @param trangThaiThanhToan Trạng thái thanh toán.
     * @param tongTien Tổng thanh toán thực tế của hóa đơn.
     */
    public CustomerTransaction(
            String maHoaDon,
            String maKhachHang,
            String tenKhachHang,
            LocalDate ngayLap,
            String tenNhanVien,
            String hinhThucThanhToan,
            String trangThaiThanhToan,
            BigDecimal tongTien
    ) {
        this.maHoaDon = maHoaDon;
        this.maKhachHang = maKhachHang;
        this.tenKhachHang = tenKhachHang;
        this.ngayLap = ngayLap;
        this.tenNhanVien = tenNhanVien;
        this.hinhThucThanhToan = hinhThucThanhToan;
        this.trangThaiThanhToan = trangThaiThanhToan;
        this.tongTien = tongTien;
    }

    /** @return Mã hóa đơn. */
    public String getMaHoaDon() {
        return maHoaDon;
    }

    /** @return Mã khách hàng của giao dịch. */
    public String getMaKhachHang() {
        return maKhachHang;
    }

    /** @return Họ tên khách hàng. */
    public String getTenKhachHang() {
        return tenKhachHang;
    }

    /** @return Ngày lập hóa đơn. */
    public LocalDate getNgayLap() {
        return ngayLap;
    }

    /** @return Họ tên nhân viên lập hóa đơn. */
    public String getTenNhanVien() {
        return tenNhanVien;
    }

    /** @return Hình thức thanh toán hoặc null. */
    public String getHinhThucThanhToan() {
        return hinhThucThanhToan;
    }

    /** @return Trạng thái thanh toán của hóa đơn. */
    public String getTrangThaiThanhToan() {
        return trangThaiThanhToan;
    }

    /** @return Tổng thanh toán thực tế từ database. */
    public BigDecimal getTongTien() {
        return tongTien;
    }
}