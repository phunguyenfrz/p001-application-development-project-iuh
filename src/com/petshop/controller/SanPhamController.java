package com.petshop.controller;

import com.petshop.model.SanPham;
import com.petshop.service.SanPhamService;

import java.util.List;

public class SanPhamController {

    private final SanPhamService sanPhamService;

    public SanPhamController() {
        this(new SanPhamService());
    }

    public SanPhamController(SanPhamService sanPhamService) {
        this.sanPhamService = sanPhamService;
    }

    public List<SanPham.NhaCungCapSimple> layDanhSachNhaCungCap() {
        return sanPhamService.layDanhSachNhaCungCap();
    }

    public String themSanPham(SanPham sp) {
        return sanPhamService.themSanPham(sp);
    }

    public boolean capNhatSanPham(SanPham sp) {
        return sanPhamService.capNhatSanPham(sp);
    }

    public boolean xoaMemSanPham(String maSanPham) {
        return sanPhamService.xoaMemSanPham(maSanPham);
    }

    public SanPham laySanPhamTheoMa(String maSanPham) {
        return sanPhamService.laySanPhamTheoMa(maSanPham);
    }

    public List<SanPham> timKiemSanPham(String tuKhoa, String maNhaCungCap, Boolean chiDangKinhDoanh) {
        return sanPhamService.timKiemSanPham(tuKhoa, maNhaCungCap, chiDangKinhDoanh);
    }

    public List<SanPham> layDanhSachTonKho(boolean chiDuoiNguong) {
        return sanPhamService.layDanhSachTonKho(chiDuoiNguong);
    }

    public List<SanPham> layBaoCaoSanPham() {
        return sanPhamService.layBaoCaoSanPham();
    }

    public boolean capNhatSoLuongTon(String maSanPham, int soLuongThayDoi) {
        return sanPhamService.capNhatSoLuongTon(maSanPham, soLuongThayDoi);
    }
}
