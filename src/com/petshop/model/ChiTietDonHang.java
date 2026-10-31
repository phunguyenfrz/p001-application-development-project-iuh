package com.petshop.model;

import java.math.BigDecimal;

/**
 * Thực thể Chi tiết đơn nhập hàng.
 * Khớp với bảng ChiTietDonHang trong CSDL SQL Server:
 * - maDonHang VARCHAR(20) NOT NULL
 * - maSanPham VARCHAR(20) NOT NULL
 * - soLuong INT NOT NULL CHECK (soLuong > 0)
 * - donGia DECIMAL(18,2) NOT NULL CHECK (donGia >= 0)
 * PRIMARY KEY (maDonHang, maSanPham)
 */
public class ChiTietDonHang {

    private String maDonHang;
    private String maSanPham;
    private int soLuong;
    private BigDecimal donGia;

    // Các trường bổ trợ hiển thị trên bảng
    private String tenSanPham;
    private String donViTinh;

    public ChiTietDonHang() {
        this.soLuong = 1;
        this.donGia = BigDecimal.ZERO;
    }

    public ChiTietDonHang(String maDonHang, String maSanPham, int soLuong, BigDecimal donGia) {
        this.maDonHang = maDonHang;
        this.maSanPham = maSanPham;
        this.soLuong = soLuong;
        this.donGia = donGia != null ? donGia : BigDecimal.ZERO;
    }

    public ChiTietDonHang(String maDonHang, String maSanPham, String tenSanPham, String donViTinh, int soLuong, BigDecimal donGia) {
        this.maDonHang = maDonHang;
        this.maSanPham = maSanPham;
        this.tenSanPham = tenSanPham;
        this.donViTinh = donViTinh;
        this.soLuong = soLuong;
        this.donGia = donGia != null ? donGia : BigDecimal.ZERO;
    }

    public String getMaDonHang() {
        return maDonHang;
    }

    public void setMaDonHang(String maDonHang) {
        this.maDonHang = maDonHang;
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

    public BigDecimal getThanhTien() {
        if (donGia == null) {
            return BigDecimal.ZERO;
        }
        return donGia.multiply(BigDecimal.valueOf(soLuong));
    }

    @Override
    public String toString() {
        return "ChiTietDonHang{" +
                "maDonHang='" + maDonHang + '\'' +
                ", maSanPham='" + maSanPham + '\'' +
                ", tenSanPham='" + tenSanPham + '\'' +
                ", soLuong=" + soLuong +
                ", donGia=" + donGia +
                '}';
    }
}

