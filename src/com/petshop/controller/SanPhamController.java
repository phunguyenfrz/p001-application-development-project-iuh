package com.petshop.controller;

import com.petshop.model.NhaCungCap;
import com.petshop.model.SanPham;
import com.petshop.service.SanPhamService;

import java.util.List;

/**
 * Điều phối dữ liệu giữa tầng View và Service cho phân hệ Sản phẩm.
 */
public class SanPhamController {

    private final SanPhamService sanPhamService;

    public SanPhamController() {
        this.sanPhamService = new SanPhamService();
    }

    public SanPhamController(SanPhamService sanPhamService) {
        this.sanPhamService = sanPhamService;
    }

    public boolean themSanPham(SanPham sp) {
        return sanPhamService.themSanPham(sp);
    }

    public boolean capNhatSanPham(SanPham sp) {
        return sanPhamService.capNhatSanPham(sp);
    }

    public boolean xoaSanPham(String maSanPham) {
        return sanPhamService.xoaSanPham(maSanPham);
    }

    public boolean capNhatTrangThaiSanPham(String maSanPham, boolean trangThai) {
        return sanPhamService.capNhatTrangThaiSanPham(maSanPham, trangThai);
    }

    public List<SanPham> layDanhSachSanPham() {
        return sanPhamService.layDanhSachSanPham();
    }

    public List<SanPham> layTheoNhaCungCap(String maNhaCungCap) {
        return sanPhamService.layTheoNhaCungCap(maNhaCungCap);
    }

    public SanPham layTheoMa(String maSanPham) {
        return sanPhamService.layTheoMa(maSanPham);
    }

    public List<SanPham> timKiemSanPham(String tuKhoa, Boolean trangThai) {
        return sanPhamService.timKiemSanPham(tuKhoa, trangThai);
    }

    public List<SanPham> layDanhSachSanPhamSapHet() {
        return sanPhamService.layDanhSachSanPhamSapHet();
    }

    public int traCuuTonKho(String maSanPham) {
        return sanPhamService.traCuuTonKho(maSanPham);
    }

    public boolean capNhatSoLuongTon(String maSanPham, int soLuongThayDoi) {
        return sanPhamService.capNhatSoLuongTon(maSanPham, soLuongThayDoi);
    }

    public String taoMaSanPham() {
        return sanPhamService.taoMaSanPham();
    }

    public List<NhaCungCap> layDanhSachNhaCungCap() {
        return sanPhamService.layDanhSachNhaCungCap();
    }
}
