package com.petshop.model;

/**
 * Mục hoạt động gần đây hiển thị trên màn hình Tổng quan Dashboard.
 */
public class HoatDongGanDay {
    private String thoiGian;
    private String loaiHoatDong;
    private String doiTuong;
    private String chiTiet;
    private String trangThai;

    public HoatDongGanDay() {}

    public HoatDongGanDay(String thoiGian, String loaiHoatDong, String chiTiet) {
        this.thoiGian = thoiGian;
        this.loaiHoatDong = loaiHoatDong;
        this.chiTiet = chiTiet;
        this.doiTuong = "--";
        this.trangThai = "Bình thường";
    }

    public HoatDongGanDay(String thoiGian, String loaiHoatDong, String doiTuong, String chiTiet, String trangThai) {
        this.thoiGian = thoiGian;
        this.loaiHoatDong = loaiHoatDong;
        this.doiTuong = doiTuong;
        this.chiTiet = chiTiet;
        this.trangThai = trangThai;
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

    public String getDoiTuong() {
        return doiTuong != null ? doiTuong : "--";
    }

    public void setDoiTuong(String doiTuong) {
        this.doiTuong = doiTuong;
    }

    public String getChiTiet() {
        return chiTiet;
    }

    public void setChiTiet(String chiTiet) {
        this.chiTiet = chiTiet;
    }

    public String getTrangThai() {
        return trangThai != null ? trangThai : "Bình thường";
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
