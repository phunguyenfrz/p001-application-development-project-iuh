package com.petshop.model;

import java.util.Objects;

/**
 * Người dùng đăng nhập (ghép từ bảng TaiKhoan và NhanVien).
 */
public class User {
    /** Vai trò — displayName khớp giá trị cột phanQuyen trong DB. */
    public enum Role {
        ADMIN("Admin"),
        QUAN_LY("Quản lý"),
        BAN_HANG("Bán hàng"),
        CHAM_SOC("Chăm sóc");

        private final String displayName;

        Role(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static Role fromDisplayName(String value) {
            String normalizedValue = value == null ? "" : value.trim();
            for (Role r : values()) {
                if (r.displayName.equalsIgnoreCase(normalizedValue)
                        || r.name().equalsIgnoreCase(normalizedValue)) {
                    return r;
                }
            }
            throw new IllegalArgumentException("Vai trò không hợp lệ: " + value);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role=" + role +
                '}';
    }
}
