package com.petshop.model;

import java.math.BigDecimal;

/**
 * Thực thể ánh xạ bảng ChiTietHoaDon_DichVu trong cơ sở dữ liệu.
 * Lưu thông tin từng dòng dịch vụ chăm sóc thú cưng thuộc hóa đơn.
 */
public class ChiTietHoaDonDichVu {

    private String maHoaDon;
    private String maDichVu;
    private String tenDichVu;
    private String maThuCung;
    private String tenThuCung;
    private int soLuong; // Mặc định là 1 theo ràng buộc CHECK (soLuong = 1)
    private BigDecimal donGia;

    public ChiTietHoaDonDichVu() {
        this.soLuong = 1;
        this.donGia = BigDecimal.ZERO;
    }

    public ChiTietHoaDonDichVu(String maHoaDon, String maDichVu, int soLuong, BigDecimal donGia) {
        this.maHoaDon = maHoaDon;
        this.maDichVu = maDichVu;
        this.soLuong = 1;
        this.donGia = donGia != null ? donGia : BigDecimal.ZERO;
    }

    public ChiTietHoaDonDichVu(String maHoaDon, String maDichVu, String tenDichVu, String maThuCung, String tenThuCung, BigDecimal donGia) {
        this(maHoaDon, maDichVu, 1, donGia);
        this.tenDichVu = tenDichVu;
        this.maThuCung = maThuCung;
        this.tenThuCung = tenThuCung;
    }

    public BigDecimal getThanhTien() {
        if (donGia == null) return BigDecimal.ZERO;
        return donGia.multiply(BigDecimal.valueOf(soLuong));
    }

    // Getters and Setters
    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }

    public String getMaDichVu() { return maDichVu; }
    public void setMaDichVu(String maDichVu) { this.maDichVu = maDichVu; }

    public String getTenDichVu() { return tenDichVu; }
    public void setTenDichVu(String tenDichVu) { this.tenDichVu = tenDichVu; }

    public String getMaThuCung() { return maThuCung; }
    public void setMaThuCung(String maThuCung) { this.maThuCung = maThuCung; }

    public String getTenThuCung() { return tenThuCung; }
    public void setTenThuCung(String tenThuCung) { this.tenThuCung = tenThuCung; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }
}

