package com.petshop.util;

import com.petshop.model.TaiKhoan;

/**
 * Quản lý phiên làm việc của người dùng sau khi đăng nhập thành công.
 */
public final class SessionManager {
    private static TaiKhoan taiKhoanHienTai;

    private SessionManager() {
    }

    public static void dangNhap(TaiKhoan taiKhoan) {
        taiKhoanHienTai = taiKhoan;
    }

    public static TaiKhoan getTaiKhoanHienTai() {
        return taiKhoanHienTai;
    }

    public static boolean daDangNhap() {
        return taiKhoanHienTai != null;
    }

    public static void dangXuat() {
        taiKhoanHienTai = null;
    }
}
