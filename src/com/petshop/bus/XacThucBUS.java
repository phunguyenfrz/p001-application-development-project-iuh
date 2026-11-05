package com.petshop.bus;

import com.petshop.dao.TaiKhoanDAO;
import com.petshop.entity.TaiKhoan;
import com.petshop.util.SessionManager;

/**
 * Lớp dịch vụ xử lý nghiệp vụ xác thực đăng nhập và kiểm tra quyền hạn tài khoản.
 */
public class XacThucBUS {

    private final TaiKhoanDAO taiKhoanDAO;

    public XacThucBUS() {
        this.taiKhoanDAO = new TaiKhoanDAO();
    }

    public XacThucBUS(TaiKhoanDAO taiKhoanDAO) {
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
    public TaiKhoan xacThuc(String tenDangNhap, String matKhau) {
        return dangNhap(tenDangNhap, matKhau);
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

        if (!DanhMucMenu.kiemTraQuyen(tk, maChucNang)) {
            throw new SecurityException("Tài khoản của bạn không có quyền thực hiện chức năng này!");
        }
    }
}

