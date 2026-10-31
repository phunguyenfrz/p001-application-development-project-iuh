package com.petshop.controller;

import com.petshop.model.KhuyenMai;
import com.petshop.service.KhuyenMaiService;

import java.math.BigDecimal;
import java.util.List;

/**
 * Điều phối thao tác khuyến mãi từ giao diện xuống KhuyenMaiService.
 */
public class KhuyenMaiController {

    private final KhuyenMaiService khuyenMaiService;

    public KhuyenMaiController() {
        this.khuyenMaiService = new KhuyenMaiService();
    }

    public KhuyenMaiController(KhuyenMaiService khuyenMaiService) {
        this.khuyenMaiService = khuyenMaiService;
    }

    public boolean themKhuyenMai(KhuyenMai khuyenMai) {
        return khuyenMaiService.themKhuyenMai(khuyenMai);
    }

    public boolean capNhatKhuyenMai(KhuyenMai khuyenMai) {
        return khuyenMaiService.capNhatKhuyenMai(khuyenMai);
    }

    public boolean xoaKhuyenMai(String maKhuyenMai) {
        return khuyenMaiService.xoaKhuyenMai(maKhuyenMai);
    }

    public List<KhuyenMai> layDanhSachKhuyenMai() {
        return khuyenMaiService.layDanhSachKhuyenMai();
    }

    public KhuyenMai layKhuyenMaiTheoMa(String maKhuyenMai) {
        return khuyenMaiService.layKhuyenMaiTheoMa(maKhuyenMai);
    }

    public List<KhuyenMai> timKiemKhuyenMai(String maKhuyenMai, String tenChuongTrinh, Boolean trangThai) {
        return khuyenMaiService.timKiemKhuyenMai(maKhuyenMai, tenChuongTrinh, trangThai);
    }

    // Hàm cung cấp cho #71 Hóa đơn: boolean kiemTraKhuyenMaiHopLe(String maKhuyenMai).
    public boolean kiemTraKhuyenMaiHopLe(String maKhuyenMai) {
        return khuyenMaiService.kiemTraKhuyenMaiHopLe(maKhuyenMai);
    }

    public BigDecimal tinhSoTienGiam(String maKhuyenMai, BigDecimal tongHoaDon) {
        return khuyenMaiService.tinhSoTienGiam(maKhuyenMai, tongHoaDon);
    }
}
