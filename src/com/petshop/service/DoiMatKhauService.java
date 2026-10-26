package com.petshop.service;

import com.petshop.dao.TaiKhoanDAO;
import com.petshop.model.TaiKhoan;
import com.petshop.util.SessionManager;

/**
 * Lớp dịch vụ xử lý nghiệp vụ đổi mật khẩu tài khoản người dùng.
 */
public class DoiMatKhauService {

    private final TaiKhoanDAO taiKhoanDAO;

    public DoiMatKhauService() {
        this.taiKhoanDAO = new TaiKhoanDAO();
    }

    public DoiMatKhauService(TaiKhoanDAO taiKhoanDAO) {
        this.taiKhoanDAO = taiKhoanDAO;
    }

    /**
     * Thực hiện đổi mật khẩu cho tài khoản hiện tại.
     * Kiểm tra hợp lệ các trường: không rỗng, mật khẩu cũ đúng, mật khẩu mới khớp xác nhận.
     */
    public boolean doiMatKhau(String matKhauCu, String matKhauMoi, String xacNhanMatKhau) {
        TaiKhoan taiKhoanHienTai = SessionManager.getTaiKhoanHienTai();
        if (taiKhoanHienTai == null) {
            throw new SecurityException("Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại!");
        }

        if (matKhauCu == null || matKhauCu.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu hiện tại!");
        }

        if (matKhauMoi == null || matKhauMoi.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu mới!");
        }

        if (matKhauMoi.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự!");
        }

        if (xacNhanMatKhau == null || xacNhanMatKhau.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng xác nhận mật khẩu mới!");
        }

        if (!matKhauMoi.equals(xacNhanMatKhau)) {
            throw new IllegalArgumentException("Mật khẩu mới và xác nhận mật khẩu không khớp!");
        }

        if (matKhauMoi.equals(matKhauCu)) {
            throw new IllegalArgumentException("Mật khẩu mới không được trùng với mật khẩu hiện tại!");
        }

        // Kiểm tra mật khẩu cũ bằng cách thử xác thực
        TaiKhoan tkKiemTra = taiKhoanDAO.dangNhap(taiKhoanHienTai.getTenDangNhap(), matKhauCu);
        if (tkKiemTra == null) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác!");
        }

        boolean ketQua = taiKhoanDAO.doiMatKhau(taiKhoanHienTai.getMaTaiKhoan(), matKhauMoi);
        if (!ketQua) {
            throw new IllegalStateException("Không thể cập nhật mật khẩu, vui lòng thử lại sau!");
        }

        return true;
    }
}
