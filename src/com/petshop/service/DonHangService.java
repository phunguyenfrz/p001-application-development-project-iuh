package com.petshop.service;

import com.petshop.dao.DonHangDAO;
import com.petshop.model.DonNhap;

import java.util.List;

public class DonHangService {

    private final DonHangDAO donHangDAO;
    private final XacThucService xacThucService;

    public DonHangService() {
        this(new DonHangDAO(), new XacThucService());
    }

    public DonHangService(DonHangDAO donHangDAO, XacThucService xacThucService) {
        this.donHangDAO = donHangDAO;
        this.xacThucService = xacThucService;
    }

    public List<DonNhap> layDonChoNhanHang() {
        xacThucService.kiemTraQuyen("DN_NHAN_HANG");
        return donHangDAO.layDonChoNhanHang();
    }

    public List<DonNhap.ChiTiet> layChiTietDonHang(String maDonHang) {
        xacThucService.kiemTraQuyen("DN_NHAN_HANG");
        if (maDonHang == null || maDonHang.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn đơn nhập.");
        }
        return donHangDAO.layChiTietDonHang(maDonHang.trim());
    }

    public boolean xacNhanNhanHang(String maDonHang) {
        xacThucService.kiemTraQuyen("DN_NHAN_HANG");
        if (maDonHang == null || maDonHang.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn đơn nhập cần xác nhận.");
        }
        return donHangDAO.xacNhanNhanHang(maDonHang.trim());
    }
}