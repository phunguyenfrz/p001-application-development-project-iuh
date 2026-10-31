package com.petshop.model;

import java.math.BigDecimal;

/**
 * Chi tiết dịch vụ trong một lịch hẹn chăm sóc thú cưng.
 */
public class ChiTietLichHen {

    private String maLichHen;
    private String maDichVu;
    private String tenDichVu;
    private BigDecimal canNangThuCung;
    private BigDecimal donGia;

    public ChiTietLichHen() {
        this.canNangThuCung = BigDecimal.ZERO;
        this.donGia = BigDecimal.ZERO;
    }

    public ChiTietLichHen(String maLichHen, String maDichVu, BigDecimal canNangThuCung, BigDecimal donGia) {
        this.maLichHen = maLichHen;
        this.maDichVu = maDichVu;
        this.canNangThuCung = canNangThuCung;
        this.donGia = donGia;
    }

    public ChiTietLichHen(String maLichHen, String maDichVu, String tenDichVu, BigDecimal canNangThuCung, BigDecimal donGia) {
        this.maLichHen = maLichHen;
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.canNangThuCung = canNangThuCung;
        this.donGia = donGia;
    }

    public String getMaLichHen() {
        return maLichHen;
    }

    public void setMaLichHen(String maLichHen) {
        this.maLichHen = maLichHen;
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(String maDichVu) {
        this.maDichVu = maDichVu;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public void setTenDichVu(String tenDichVu) {
        this.tenDichVu = tenDichVu;
    }

    public BigDecimal getCanNangThuCung() {
        return canNangThuCung;
    }

    public void setCanNangThuCung(BigDecimal canNangThuCung) {
        this.canNangThuCung = canNangThuCung;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }
}

