package com.petshop.controller;

import com.petshop.model.DonNhap;
import com.petshop.service.DonHangService;

import java.util.List;

public class DonHangController {

    private final DonHangService donHangService;

    public DonHangController() {
        this(new DonHangService());
    }

    public DonHangController(DonHangService donHangService) {
        this.donHangService = donHangService;
    }

    public List<DonNhap> layDonChoNhanHang() {
        return donHangService.layDonChoNhanHang();
    }

    public List<DonNhap.ChiTiet> layChiTietDonHang(String maDonHang) {
        return donHangService.layChiTietDonHang(maDonHang);
    }

    public boolean xacNhanNhanHang(String maDonHang) {
        return donHangService.xacNhanNhanHang(maDonHang);
    }
}