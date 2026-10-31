package com.petshop.model;

import java.math.BigDecimal;

/**
 * Thực thể chi tiết dịch vụ trong một phiếu tiếp nhận thú cưng làm dịch vụ.
 */
public class ChiTietPhieuDichVu {

    private String maPhieuDichVu;
    private String maThuCung;
    private String maDichVu;
    private BigDecimal donGia;

    // Các trường hỗ trợ hiển thị trên giao diện (lấy qua JOIN)
    private String tenThuCung;
    private String tenDichVu;

    public ChiTietPhieuDichVu() {
        this.donGia = BigDecimal.ZERO;
    }

    public ChiTietPhieuDichVu(String maPhieuDichVu, String maThuCung, String maDichVu, BigDecimal donGia) {
        this.maPhieuDichVu = maPhieuDichVu;
        this.maThuCung = maThuCung;
        this.maDichVu = maDichVu;
        this.donGia = donGia != null ? donGia : BigDecimal.ZERO;
    }

    public ChiTietPhieuDichVu(String maPhieuDichVu, String maThuCung, String tenThuCung,
                             String maDichVu, String tenDichVu, BigDecimal donGia) {
        this.maPhieuDichVu = maPhieuDichVu;
        this.maThuCung = maThuCung;
        this.tenThuCung = tenThuCung;
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.donGia = donGia != null ? donGia : BigDecimal.ZERO;
    }

    public String getMaPhieuDichVu() {
        return maPhieuDichVu;
    }

    public void setMaPhieuDichVu(String maPhieuDichVu) {
        this.maPhieuDichVu = maPhieuDichVu;
    }

    public String getMaThuCung() {
        return maThuCung;
    }

    public void setMaThuCung(String maThuCung) {
        this.maThuCung = maThuCung;
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(String maDichVu) {
        this.maDichVu = maDichVu;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    public String getTenThuCung() {
        return tenThuCung;
    }

    public void setTenThuCung(String tenThuCung) {
        this.tenThuCung = tenThuCung;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public void setTenDichVu(String tenDichVu) {
        this.tenDichVu = tenDichVu;
    }
}

