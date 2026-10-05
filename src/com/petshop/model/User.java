package com.petshop.model;

/**
 * Người dùng đăng nhập (ghép từ bảng TaiKhoan và NhanVien).
 */
public class User {
    /** Vai trò — displayName khớp giá trị cột phanQuyen trong DB. */
    public enum Role {
        QUAN_LY("Quản lý"),
        BAN_HANG("Bán hàng"),
        CHAM_SOC("Chăm sóc"),
        THU_NGAN("Thu ngân");

        private final String displayName;

        Role(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static Role fromDisplayName(String value) {
            for (Role r : values()) {
                if (r.displayName.equals(value)) return r;
            }
            throw new IllegalArgumentException("Phân quyền không hợp lệ: " + value);
        }
    }

    private final String maNhanVien;
    private final String username;
    private final String fullName;
    private final Role role;

    public User(String maNhanVien, String username, String fullName, Role role) {
        this.maNhanVien = maNhanVien;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
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
