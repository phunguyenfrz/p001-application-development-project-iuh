package com.petshop.model;

import java.math.BigDecimal;

/**
 * Thực thể Chi tiết đơn nhập hàng.
 * Khớp với bảng ChiTietDonNhap trong CSDL SQL Server:
 * - maDonHang VARCHAR(20) NOT NULL
 * - maSanPham VARCHAR(20) NOT NULL
 * - soLuong INT NOT NULL CHECK (soLuong > 0)
 * - soLuongThucNhan INT NULL CHECK (soLuongThucNhan >= 0)
 * - donGia DECIMAL(18,2) NOT NULL CHECK (donGia >= 0)
 * - PRIMARY KEY (maDonHang, maSanPham)
 */
public class ChiTietDonNhap {

    private String maDonHang;
    private String maSanPham;
    private int soLuong;
    private Integer soLuongThucNhan;
    private BigDecimal donGia;

    // Các trường hiển thị bổ trợ (JOIN view)
    private String tenSanPham;
    private String donViTinh;

    public ChiTietDonNhap() {
        this.soLuong = 1;
        this.soLuongThucNhan = 1;
        this.donGia = BigDecimal.ZERO;
    }

    public ChiTietDonNhap(String maDonHang, String maSanPham, int soLuong, BigDecimal donGia) {
        this.maDonHang = maDonHang;
        this.maSanPham = maSanPham;
        this.soLuong = soLuong;
        this.soLuongThucNhan = soLuong;
        this.donGia = donGia != null ? donGia : BigDecimal.ZERO;
    }

    public ChiTietDonNhap(String maDonHang, String maSanPham, String tenSanPham, String donViTinh, int soLuong, BigDecimal donGia) {
        this.maDonHang = maDonHang;
        this.maSanPham = maSanPham;
        this.tenSanPham = tenSanPham;
        this.donViTinh = donViTinh;
        this.soLuong = soLuong;
        this.soLuongThucNhan = soLuong;
        this.donGia = donGia != null ? donGia : BigDecimal.ZERO;
    }

    public ChiTietDonNhap(String maDonHang, String maSanPham, String tenSanPham, String donViTinh, int soLuong, Integer soLuongThucNhan, BigDecimal donGia) {
        this.maDonHang = maDonHang;
        this.maSanPham = maSanPham;
        this.tenSanPham = tenSanPham;
        this.donViTinh = donViTinh;
        this.soLuong = soLuong;
        this.soLuongThucNhan = soLuongThucNhan != null ? soLuongThucNhan : soLuong;
        this.donGia = donGia != null ? donGia : BigDecimal.ZERO;
    }

    public String getMaDonHang() {
        return maDonHang;
    }

    public void setMaDonHang(String maDonHang) {
        this.maDonHang = maDonHang;
    }

    public String getMaDonNhap() {
        return maDonHang;
    }

    public void setMaDonNhap(String maDonNhap) {
        this.maDonHang = maDonNhap;
    }

    public String getMaSanPham() {
        return maSanPham;
    }

    public void setMaSanPham(String maSanPham) {
        this.maSanPham = maSanPham;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
        if (this.soLuongThucNhan == null) {
            this.soLuongThucNhan = soLuong;
        }
    }

    public Integer getSoLuongThucNhan() {
        return soLuongThucNhan != null ? soLuongThucNhan : soLuong;
    }

    public void setSoLuongThucNhan(Integer soLuongThucNhan) {
        this.soLuongThucNhan = soLuongThucNhan;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia != null ? donGia : BigDecimal.ZERO;
    }

    public String getTenSanPham() {
        return tenSanPham;
    }

    public void setTenSanPham(String tenSanPham) {
        this.tenSanPham = tenSanPham;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    /**
     * Thành tiền theo số lượng đặt = soLuong * donGia.
     */
    public BigDecimal getThanhTien() {
        if (donGia == null || soLuong <= 0) {
            return BigDecimal.ZERO;
        }
        return donGia.multiply(BigDecimal.valueOf(soLuong));
    }

    /**
     * Thành tiền thực nhận = soLuongThucNhan * donGia.
     */
    public BigDecimal getThanhTienThucNhan() {
        int thucNhan = getSoLuongThucNhan();
        if (donGia == null || thucNhan <= 0) {
            return BigDecimal.ZERO;
        }
        return donGia.multiply(BigDecimal.valueOf(thucNhan));
    }

    @Override
    public String toString() {
        return "ChiTietDonNhap{" +
                "maDonHang='" + maDonHang + '\'' +
                ", maSanPham='" + maSanPham + '\'' +
                ", tenSanPham='" + tenSanPham + '\'' +
                ", soLuong=" + soLuong +
                ", soLuongThucNhan=" + soLuongThucNhan +
                ", donGia=" + donGia +
                ", thanhTien=" + getThanhTien() +
                ", thanhTienThucNhan=" + getThanhTienThucNhan() +
                '}';
    }
}
