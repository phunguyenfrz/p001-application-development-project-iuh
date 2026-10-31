package com.petshop.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Thực thể chương trình khuyến mãi áp dụng cho tổng tiền hóa đơn.
 */
public class KhuyenMai {

    private String maKhuyenMai;
    private String tenChuongTrinh;
    private BigDecimal phanTramGiam;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private boolean trangThai;

    public KhuyenMai() {
        this.trangThai = true;
    }

    public KhuyenMai(String maKhuyenMai, String tenChuongTrinh, BigDecimal phanTramGiam,
                     LocalDate ngayBatDau, LocalDate ngayKetThuc, boolean trangThai) {
        this.maKhuyenMai = maKhuyenMai;
        this.tenChuongTrinh = tenChuongTrinh;
        this.phanTramGiam = phanTramGiam;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.trangThai = trangThai;
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

    public void setTenChuongTrinh(String tenChuongTrinh) {
        this.tenChuongTrinh = tenChuongTrinh;
    }

    public BigDecimal getPhanTramGiam() {
        return phanTramGiam;
    }

    public void setPhanTramGiam(BigDecimal phanTramGiam) {
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
