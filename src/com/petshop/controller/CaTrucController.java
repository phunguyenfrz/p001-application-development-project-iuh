package com.petshop.controller;

import com.petshop.model.Ca;
import com.petshop.model.NhanVien;
import com.petshop.model.PhanCa;
import com.petshop.service.CaTrucService;

import java.time.LocalDate;
import java.util.List;

/**
 * Điều phối thao tác giữa các màn hình giao diện Ca trực và tầng CaTrucService.
 */
public class CaTrucController {

    private final CaTrucService caTrucService;

    public CaTrucController() {
        this.caTrucService = new CaTrucService();
    }

    public CaTrucController(CaTrucService caTrucService) {
        this.caTrucService = caTrucService;
    }

    public String taoMaCa() {
        return caTrucService.taoMaCa();
    }

    public boolean themCa(Ca ca) {
        return caTrucService.themCa(ca);
    }

    public boolean themCaLamViec(Ca ca) {
        return caTrucService.themCaLamViec(ca);
    }

    public boolean capNhatCa(Ca ca) {
        return caTrucService.capNhatCa(ca);
    }

    public boolean suaCaLamViec(Ca ca) {
        return caTrucService.suaCaLamViec(ca);
    }

    public boolean xoaMemCa(String maCa) {
        return caTrucService.xoaMemCa(maCa);
    }

    public boolean xoaCaLamViec(String maCa) {
        return caTrucService.xoaCaLamViec(maCa);
    }

    public List<Ca> layDanhSachCa() {
        return caTrucService.layDanhSachCa();
    }

    public List<Ca> layDanhSachCaDangApDung() {
        return caTrucService.layDanhSachCaDangApDung();
    }

    public Ca layCaTheoMa(String maCa) {
        return caTrucService.layCaTheoMa(maCa);
    }

    public String taoMaPhanCa() {
        return caTrucService.taoMaPhanCa();
    }

    public boolean phanCa(PhanCa phanCa) {
        return caTrucService.phanCa(phanCa);
    }

    public boolean themPhanCa(PhanCa phanCa) {
        return caTrucService.themPhanCa(phanCa);
    }

    public boolean suaPhanCa(PhanCa phanCa) {
        return caTrucService.suaPhanCa(phanCa);
    }

    public boolean huyPhanCa(String maPhanCa) {
        return caTrucService.huyPhanCa(maPhanCa);
    }

    public boolean kiemTraTrungCa(String maNhanVien, String maCa, LocalDate ngayLam) {
        return caTrucService.kiemTraTrungCa(maNhanVien, maCa, ngayLam);
    }

    public List<PhanCa> xemLichTheoNhanVien(String maNhanVien) {
        return caTrucService.xemLichTheoNhanVien(maNhanVien);
    }

    public List<PhanCa> xemLichTheoNgay(LocalDate ngay) {
        return caTrucService.xemLichTheoNgay(ngay);
    }

    public List<PhanCa> layDanhSachPhanCa() {
        return caTrucService.layDanhSachPhanCa();
    }

    public List<PhanCa> layPhanCaTheoNgay(LocalDate ngay) {
        return caTrucService.layPhanCaTheoNgay(ngay);
    }

    public List<PhanCa> timKiemPhanCa(LocalDate ngay, String maNhanVien, String maCa, String trangThai) {
        return caTrucService.timKiemPhanCa(ngay, maNhanVien, maCa, trangThai);
    }

    public List<NhanVien> layDanhSachNhanVienDangLamViec() {
        return caTrucService.layDanhSachNhanVienDangLamViec();
    }
}

