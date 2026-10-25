package com.petshop.controller;

import com.petshop.model.TaiKhoan;
import com.petshop.service.XacThucService;

/**
 * Controller điều khiển luồng nghiệp vụ đăng nhập của hệ thống.
 */
public class DangNhapController {
    private final XacThucService xacThucService;

    public DangNhapController() {
        this.xacThucService = new XacThucService();
    }

    public DangNhapController(XacThucService xacThucService) {
        this.xacThucService = xacThucService;
    }

    /**
     * Xác thực thông tin tài khoản và mật khẩu người dùng.
     *
     * @param tenDangNhap Tên đăng nhập
     * @param matKhau Mật khẩu
     * @return TaiKhoan nếu đăng nhập thành công, null nếu không đúng thông tin.
     * @throws IllegalStateException nếu không thể kết nối tới cơ sở dữ liệu.
     */
    public TaiKhoan xacThuc(String tenDangNhap, String matKhau) {
        return xacThucService.dangNhap(tenDangNhap, matKhau);
    }
}

