package com.petshop.controller;

import com.petshop.service.DoiMatKhauService;

/**
 * Lớp điều khiển xử lý luồng đổi mật khẩu giữa View và Service.
 */
public class DoiMatKhauController {

    private final DoiMatKhauService doiMatKhauService;

    public DoiMatKhauController() {
        this.doiMatKhauService = new DoiMatKhauService();
    }

    public DoiMatKhauController(DoiMatKhauService doiMatKhauService) {
        this.doiMatKhauService = doiMatKhauService;
    }

    /**
     * Tiếp nhận yêu cầu đổi mật khẩu từ giao diện và chuyển xuống Service.
     */
    public boolean doiMatKhau(String matKhauCu, String matKhauMoi, String xacNhanMatKhau) {
        return doiMatKhauService.doiMatKhau(matKhauCu, matKhauMoi, xacNhanMatKhau);
    }
}

