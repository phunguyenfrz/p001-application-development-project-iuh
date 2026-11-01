package com.petshop.model;

import java.math.BigDecimal;

/**
 * Thực thể ánh xạ bảng ChiTietHoaDon_SanPham trong cơ sở dữ liệu.
 * Lưu thông tin từng dòng sản phẩm bán ra thuộc hóa đơn.
 */
public class ChiTietHoaDonSanPham {

    private String maHoaDon;
    private String maSanPham;
    private String tenSanPham;
    private String donViTinh;
    private int soLuong;
    private BigDecimal donGia;

    public ChiTietHoaDonSanPham() {
        this.soLuong = 1;
        this.donGia = BigDecimal.ZERO;
    }

    public ChiTietHoaDonSanPham(String maHoaDon, String maSanPham, int soLuong, BigDecimal donGia) {
        this.maHoaDon = maHoaDon;
        this.maSanPham = maSanPham;
        this.soLuong = soLuong;
        this.donGia = donGia != null ? donGia : BigDecimal.ZERO;
    }

    public ChiTietHoaDonSanPham(String maHoaDon, String maSanPham, String tenSanPham, String donViTinh, int soLuong, BigDecimal donGia) {
        this(maHoaDon, maSanPham, soLuong, donGia);
        this.tenSanPham = tenSanPham;
        this.donViTinh = donViTinh;
    }

    public BigDecimal getThanhTien() {
        if (donGia == null) return BigDecimal.ZERO;
        return donGia.multiply(BigDecimal.valueOf(soLuong));
    }

    // Getters and Setters
    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }

    public String getMaSanPham() { return maSanPham; }
    public void setMaSanPham(String maSanPham) { this.maSanPham = maSanPham; }

    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }
}

