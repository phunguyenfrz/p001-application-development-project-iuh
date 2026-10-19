package com.petshop.model;

import java.util.Objects;

/**
 * Model đại diện cho một mặt hàng trong đơn hàng nhập (bảng ChiTietDonHang).
 * Tuân thủ quy tắc: tên lớp là danh từ, các trường khớp CSDL và tài liệu OOAD 2.16.
 */
public class ImportOrderItem {

    private String maDonHang;
    private String maSanPham;
    private String tenSanPham;
    private String donViTinh;
    private int soLuong;        // CHECK (soLuong > 0)
    private double donGia;      // CHECK (donGia >= 0)
    private double thanhTien;   // soLuong * donGia

    public ImportOrderItem() {
    }

    public ImportOrderItem(String maDonHang, String maSanPham, String tenSanPham, String donViTinh, int soLuong, double donGia) {
        this.maDonHang = maDonHang;
        this.maSanPham = maSanPham;
        this.tenSanPham = tenSanPham;
        this.donViTinh = donViTinh;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = soLuong * donGia;
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

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
        this.thanhTien = this.soLuong * this.donGia;
    }

    public double getDonGia() {
        return donGia;
    }

    public void setDonGia(double donGia) {
        this.donGia = donGia;
        this.thanhTien = this.soLuong * this.donGia;
    }

    public double getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(double thanhTien) {
        this.thanhTien = thanhTien;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImportOrderItem that = (ImportOrderItem) o;
        return Objects.equals(maDonHang, that.maDonHang) && Objects.equals(maSanPham, that.maSanPham);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maDonHang, maSanPham);
    }

    @Override
    public String toString() {
        return "ImportOrderItem{" +
                "maSanPham='" + maSanPham + '\'' +
                ", tenSanPham='" + tenSanPham + '\'' +
                ", soLuong=" + soLuong +
                ", donGia=" + donGia +
                ", thanhTien=" + thanhTien +
                '}';
    }
}

