package com.petshop.model;

/**
 * Người dùng đăng nhập (ghép từ bảng TaiKhoan và NhanVien).
 */
public class User {
    private final String maNhanVien;
    private final String maTaiKhoan;
    private final String username;
    private final String fullName;
    private final Role role;

    public User(String maTaiKhoan, String maNhanVien, String username, String fullName, Role role) {
        this.maTaiKhoan = maTaiKhoan;
        this.maNhanVien = maNhanVien;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    public String getMaTaiKhoan() {
        return maTaiKhoan;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }
}
