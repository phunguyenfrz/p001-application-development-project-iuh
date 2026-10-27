package com.petshop.controller;

import com.petshop.model.DonNhap;
import com.petshop.service.DonHangService;

import java.time.LocalDate;
import java.util.List;

public class DonHangController {

    private final DonHangService donHangService;

    public DonHangController() {
        this(new DonHangService());
    }

    public DonHangController(DonHangService donHangService) {
        this.donHangService = donHangService;
    }

    public List<DonNhap.NhaCungCap> layDanhSachNhaCungCap() {
        return donHangService.layDanhSachNhaCungCap();
    }

    public List<DonNhap.SanPham> layDanhSachSanPham(String maNhaCungCap) {
        return donHangService.layDanhSachSanPham(maNhaCungCap);
    }

    public String taoDonNhap(String maNhaCungCap, List<DonNhap.ChiTiet> chiTiet) {
        return donHangService.taoDonNhap(maNhaCungCap, chiTiet);
    }

    public List<DonNhap> layDonChoNhanHang() {
        return donHangService.layDonChoNhanHang();
    }

    public List<DonNhap> timKiemDonNhap(String maDonHang, String tenNhaCungCap,
                                       LocalDate tuNgay, LocalDate denNgay) {
        return donHangService.timKiemDonNhap(maDonHang, tenNhaCungCap, tuNgay, denNgay);
    }

    public List<DonNhap.ChiTiet> layChiTietDonHang(String maDonHang) {
        return donHangService.layChiTietDonHang(maDonHang);
    }

    public boolean xacNhanNhanHang(String maDonHang) {
        return donHangService.xacNhanNhanHang(maDonHang);
    }

    public boolean huyDonNhap(String maDonHang) {
        return donHangService.huyDonNhap(maDonHang);
    }
}