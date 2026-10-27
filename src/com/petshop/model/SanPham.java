package com.petshop.model;

import java.math.BigDecimal;

public class SanPham {

    private String maSanPham;
    private String maNhaCungCap;
    private String tenNhaCungCap;
    private String tenSanPham;
    private String maVach;
    private BigDecimal giaBan;
    private int soLuongTon;
    private int nguongTonToiThieu;
    private String donViTinh;
    private boolean trangThai;

    public SanPham() {
    }

    public SanPham(String maSanPham, String maNhaCungCap, String tenSanPham, String maVach,
                   BigDecimal giaBan, int soLuongTon, int nguongTonToiThieu, String donViTinh, boolean trangThai) {
        this(maSanPham, maNhaCungCap, null, tenSanPham, maVach, giaBan, soLuongTon, nguongTonToiThieu, donViTinh, trangThai);
    }

    public SanPham(String maSanPham, String maNhaCungCap, String tenNhaCungCap, String tenSanPham,
                   String maVach, BigDecimal giaBan, int soLuongTon, int nguongTonToiThieu,
                   String donViTinh, boolean trangThai) {
        this.maSanPham = maSanPham;
        this.maNhaCungCap = maNhaCungCap;
        this.tenNhaCungCap = tenNhaCungCap;
        this.tenSanPham = tenSanPham;
        this.maVach = maVach;
        this.giaBan = giaBan;
        this.soLuongTon = soLuongTon;
        this.nguongTonToiThieu = nguongTonToiThieu;
        this.donViTinh = donViTinh;
        this.trangThai = trangThai;
    }

    public String getMaSanPham() {
        return maSanPham;
    }

    public void setMaSanPham(String maSanPham) {
        this.maSanPham = maSanPham;
    }

    public String getMaNhaCungCap() {
        return maNhaCungCap;
    }

    public void setMaNhaCungCap(String maNhaCungCap) {
        this.maNhaCungCap = maNhaCungCap;
    }

    public String getTenNhaCungCap() {
        return tenNhaCungCap;
    }

    public void setTenNhaCungCap(String tenNhaCungCap) {
        this.tenNhaCungCap = tenNhaCungCap;
    }

    public String getTenSanPham() {
        return tenSanPham;
    }

    public void setTenSanPham(String tenSanPham) {
        this.tenSanPham = tenSanPham;
    }

    public String getMaVach() {
        return maVach;
    }

    public void setMaVach(String maVach) {
        this.maVach = maVach;
    }

    public BigDecimal getGiaBan() {
        return giaBan;
    }

    public void setGiaBan(BigDecimal giaBan) {
        this.giaBan = giaBan;
    }

    public int getSoLuongTon() {
        return soLuongTon;
    }

    public void setSoLuongTon(int soLuongTon) {
        this.soLuongTon = soLuongTon;
    }

    public int getNguongTonToiThieu() {
        return nguongTonToiThieu;
    }

    public void setNguongTonToiThieu(int nguongTonToiThieu) {
        this.nguongTonToiThieu = nguongTonToiThieu;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }

    public boolean isDuoiNguongAnToan() {
        return soLuongTon <= nguongTonToiThieu;
    }

    public static record NhaCungCapSimple(String maNhaCungCap, String tenNhaCungCap) {
        @Override
        public String toString() {
            return tenNhaCungCap + " (" + maNhaCungCap + ")";
        }
    }
}
