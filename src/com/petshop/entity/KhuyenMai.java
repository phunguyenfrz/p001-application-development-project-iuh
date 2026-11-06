package com.petshop.entity;

import java.time.LocalDate;

/**
 * Thực thể đại diện cho bảng KhuyenMai trong cơ sở dữ liệu.
 * Chuẩn hóa theo Bộ chỉ thị kỹ thuật IUH: sử dụng kiểu nguyên thủy double cho tỷ lệ giảm và tính tiền giảm.
 */
public class KhuyenMai {

    private String maKhuyenMai;
    private String tenChuongTrinh;
    private double phanTramGiam;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private boolean trangThai;

    public KhuyenMai() {
    }

    public KhuyenMai(String maKhuyenMai, String tenChuongTrinh, double phanTramGiam,
                     LocalDate ngayBatDau, LocalDate ngayKetThuc, boolean trangThai) {
        this.maKhuyenMai = maKhuyenMai;
        this.tenChuongTrinh = tenChuongTrinh;
        this.phanTramGiam = phanTramGiam;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.trangThai = trangThai;
    }

    /**
     * Phương thức tính số tiền được giảm trên tổng hóa đơn (thuộc tính dẫn xuất).
     */
    public double tinhTienGiam(double tongTien) {
        if (tongTien <= 0 || phanTramGiam <= 0) {
            return 0.0;
        }
        return tongTien * phanTramGiam / 100.0;
    }

    public String getMaKhuyenMai() {
        return maKhuyenMai;
    }

    public void setMaKhuyenMai(String maKhuyenMai) {
        this.maKhuyenMai = maKhuyenMai;
    }

    public String getTenChuongTrinh() {
        return tenChuongTrinh;
    }

    public String getTenKhuyenMai() {
        return tenChuongTrinh;
    }

    public void setTenChuongTrinh(String tenChuongTrinh) {
        this.tenChuongTrinh = tenChuongTrinh;
    }

    public void setTenKhuyenMai(String tenKhuyenMai) {
        this.tenChuongTrinh = tenKhuyenMai;
    }

    public double getPhanTramGiam() {
        return phanTramGiam;
    }

    public void setPhanTramGiam(double phanTramGiam) {
        this.phanTramGiam = phanTramGiam;
    }

    public LocalDate getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDate ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDate getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDate ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }
}
