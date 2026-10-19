package com.petshop.model;

import java.util.Objects;

/**
 * Model đại diện cho Dịch vụ chăm sóc thú cưng (bảng DichVuChamSoc).
 * Tuân thủ tiêu chuẩn: tên lớp là danh từ, các trường khớp CSDL và tài liệu OOAD 2.10.
 */
public class CareService {

    private String maDichVu;        // DV-YYYYMMDD-NNNNNN
    private String tenDichVu;
    private String moTa;
    private int thoiLuong;          // CHECK (thoiLuong > 0) - tính theo phút
    private boolean trangThai;      // 1: Đang cung cấp, 0: Tạm ngừng (xóa mềm)

    // Các thuộc tính mở rộng hiển thị giá hiện hành theo 3 bậc cân nặng
    private double giaNho;          // Nhỏ (<5kg)
    private double giaVua;          // Vừa (5-10kg)
    private double giaLon;          // Lớn (>10kg)

    public CareService() {
        this.thoiLuong = 30;
        this.trangThai = true;
    }

    public CareService(String maDichVu, String tenDichVu, String moTa, int thoiLuong, boolean trangThai) {
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.moTa = moTa;
        this.thoiLuong = thoiLuong;
        this.trangThai = trangThai;
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

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public int getThoiLuong() {
        return thoiLuong;
    }

    public void setThoiLuong(int thoiLuong) {
        this.thoiLuong = thoiLuong;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }

    public double getGiaNho() {
        return giaNho;
    }

    public void setGiaNho(double giaNho) {
        this.giaNho = giaNho;
    }

    public double getGiaVua() {
        return giaVua;
    }

    public void setGiaVua(double giaVua) {
        this.giaVua = giaVua;
    }

    public double getGiaLon() {
        return giaLon;
    }

    public void setGiaLon(double giaLon) {
        this.giaLon = giaLon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CareService that = (CareService) o;
        return Objects.equals(maDichVu, that.maDichVu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maDichVu);
    }

    @Override
    public String toString() {
        return tenDichVu != null ? tenDichVu : maDichVu;
    }
}

