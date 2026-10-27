package com.petshop.model;

/**
 * Mục hoạt động gần đây hiển thị trên màn hình Tổng quan.
 */
public class HoatDongGanDay {
    private String thoiGian;
    private String loaiHoatDong;
    private String chiTiet;

    public HoatDongGanDay() {}

    public HoatDongGanDay(String thoiGian, String loaiHoatDong, String chiTiet) {
        this.thoiGian = thoiGian;
        this.loaiHoatDong = loaiHoatDong;
        this.chiTiet = chiTiet;
    }

    public String getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(String thoiGian) {
        this.thoiGian = thoiGian;
    }

    public String getLoaiHoatDong() {
        return loaiHoatDong;
    }

    public void setLoaiHoatDong(String loaiHoatDong) {
        this.loaiHoatDong = loaiHoatDong;
    }

    public String getChiTiet() {
        return chiTiet;
    }

    public void setChiTiet(String chiTiet) {
        this.chiTiet = chiTiet;
    }
}
