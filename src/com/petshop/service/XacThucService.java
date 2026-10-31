package com.petshop.service;

import com.petshop.dao.TaiKhoanDAO;
import com.petshop.model.TaiKhoan;
import com.petshop.util.SessionManager;

/**
 * Lớp dịch vụ xử lý nghiệp vụ xác thực đăng nhập và kiểm tra quyền hạn tài khoản.
 */
public class XacThucService {

    private final TaiKhoanDAO taiKhoanDAO;

    public XacThucService() {
        this.taiKhoanDAO = new TaiKhoanDAO();
    }

    public XacThucService(TaiKhoanDAO taiKhoanDAO) {
        this.taiKhoanDAO = taiKhoanDAO;
    }

    /**
     * Xác thực thông tin đăng nhập của người dùng.
     */
    public TaiKhoan dangNhap(String tenDangNhap, String matKhau) {
        if (tenDangNhap == null || tenDangNhap.isBlank() || matKhau == null || matKhau.isEmpty()) {
            return null;
        }

        TaiKhoan tk = taiKhoanDAO.dangNhap(tenDangNhap.trim(), matKhau);
        if (tk != null) {
            SessionManager.dangNhap(tk);
        }
        return tk;
    }

    /**
     * Đăng xuất tài khoản khỏi phiên làm việc.
     */
    public void dangXuat() {
        SessionManager.dangXuat();
    }

    /**
     * Lấy thông tin tài khoản đang đăng nhập hiện tại.
     */
    public TaiKhoan getTaiKhoanHienTai() {
        return SessionManager.getTaiKhoanHienTai();
    }

    /**
     * Kiểm tra tài khoản hiện tại có quyền thực hiện chức năng nghiệp vụ không.
     */
    public boolean coQuyen(String maChucNang) {
        TaiKhoan tk = getTaiKhoanHienTai();
        if (tk == null) {
            return false;
        }
        if (tk.getPhanQuyen() == TaiKhoan.VaiTro.ADMIN) {
            return true;
        }
        return DanhMucMenu.kiemTraQuyen(tk.getPhanQuyen(), maChucNang);
    }

    /**
     * Kiểm tra tài khoản hiện tại có quyền thực hiện chức năng nghiệp vụ không.
     * @throws SecurityException nếu không có quyền hoặc chưa đăng nhập.
     */
    public void kiemTraQuyen(String maChucNang) {
        TaiKhoan tk = getTaiKhoanHienTai();
        if (tk == null) {
            throw new SecurityException("Chưa đăng nhập hệ thống!");
        }

        if (tk.getPhanQuyen() == TaiKhoan.VaiTro.ADMIN) {
            return;
        }

        if (!DanhMucMenu.kiemTraQuyen(tk.getPhanQuyen(), maChucNang)) {
            throw new SecurityException("Tài khoản của bạn không có quyền thực hiện chức năng này!");
        }
    }
}

