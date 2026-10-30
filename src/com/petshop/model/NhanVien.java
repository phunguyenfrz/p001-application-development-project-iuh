package com.petshop.model;

import java.time.LocalDate;
import java.time.Period;

/**
 * Lớp thực thể Nhân viên (khớp với bảng NhanVien trong CSDL SQL Server).
 * Quản lý thông tin hồ sơ nhân sự, chức vụ, tình trạng làm việc và tài khoản hệ thống liên kết.
 */
public class NhanVien {

    private String maNhanVien;
    private String hoTen;
    private String soDienThoai;
    private String chucVu; // "Quản lý", "Bán hàng", "Chăm sóc"
    private LocalDate ngaySinh;
    private boolean gioiTinh; // true: Nam, false: Nữ
    private String trangThai; // "Đang làm", "Đã nghỉ việc"

    // Thông tin tài khoản đăng nhập liên kết (bảng TaiKhoan)
    private String maTaiKhoan;
    private String tenDangNhap;
    private String phanQuyen; // "Quản lý", "Bán hàng", "Chăm sóc", "Admin"
    private Boolean trangThaiTaiKhoan; // true: Hoạt động, false: Bị khóa

    public NhanVien() {
        this.trangThai = "Đang làm";
        this.gioiTinh = true;
    }

    public NhanVien(String maNhanVien, String hoTen, String soDienThoai, String chucVu,
                    LocalDate ngaySinh, boolean gioiTinh, String trangThai) {
        this.maNhanVien = maNhanVien;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.chucVu = chucVu;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.trangThai = (trangThai != null && !trangThai.isBlank()) ? trangThai : "Đang làm";
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getChucVu() {
        return chucVu;
    }

    public void setChucVu(String chucVu) {
        this.chucVu = chucVu;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public boolean isGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(boolean gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public String getTenGioiTinh() {
        return gioiTinh ? "Nam" : "Nữ";
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public boolean isDangLam() {
        return "Đang làm".equalsIgnoreCase(this.trangThai);
    }

    public int getTuoi() {
        if (ngaySinh == null) return 0;
        return Period.between(ngaySinh, LocalDate.now()).getYears();
    }

    // Getter / Setter cho thông tin tài khoản liên kết
    public String getMaTaiKhoan() {
        return maTaiKhoan;
    }

    public void setMaTaiKhoan(String maTaiKhoan) {
        this.maTaiKhoan = maTaiKhoan;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    public String getPhanQuyen() {
        return phanQuyen;
    }

    public void setPhanQuyen(String phanQuyen) {
        this.phanQuyen = phanQuyen;
    }

    public Boolean getTrangThaiTaiKhoan() {
        return trangThaiTaiKhoan;
    }

    public void setTrangThaiTaiKhoan(Boolean trangThaiTaiKhoan) {
        this.trangThaiTaiKhoan = trangThaiTaiKhoan;
    }

    public boolean isTaiKhoanHoatDong() {
        return Boolean.TRUE.equals(this.trangThaiTaiKhoan);
    }

    @Override
    public String toString() {
        return hoTen + " (" + maNhanVien + " - " + chucVu + ")";
    }
}

