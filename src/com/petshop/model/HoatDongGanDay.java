package com.petshop.model;

/**
 * Mục hoạt động gần đây hiển thị trên màn hình Tổng quan.
 */
public class HoatDongGanDay {
    private String tieuDe;
    private String thoiGian;
    private String moTa;

    public HoatDongGanDay() {}

    public HoatDongGanDay(String tieuDe, String thoiGian, String moTa) {
        this.tieuDe = tieuDe;
        this.thoiGian = thoiGian;
        this.moTa = moTa;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(String thoiGian) {
        this.thoiGian = thoiGian;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }
}

