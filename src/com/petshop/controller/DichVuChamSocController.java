package com.petshop.controller;

import com.petshop.model.BangGiaDichVu;
import com.petshop.model.DichVuChamSoc;
import com.petshop.service.DichVuChamSocService;

import java.util.List;

/**
 * Điều phối các thao tác từ giao diện Dịch vụ chăm sóc xuống tầng DichVuChamSocService.
 */
public class DichVuChamSocController {

    private final DichVuChamSocService dichVuChamSocService;

    public DichVuChamSocController() {
        this.dichVuChamSocService = new DichVuChamSocService();
    }

    public DichVuChamSocController(DichVuChamSocService dichVuChamSocService) {
        this.dichVuChamSocService = dichVuChamSocService;
    }

    public boolean themDichVu(DichVuChamSoc dichVu) {
        return dichVuChamSocService.themDichVu(dichVu);
    }

    public boolean capNhatDichVu(DichVuChamSoc dichVu) {
        return dichVuChamSocService.capNhatDichVu(dichVu);
    }

    public boolean xoaDichVu(String maDichVu) {
        return dichVuChamSocService.xoaDichVu(maDichVu);
    }

    public List<DichVuChamSoc> layDanhSachDichVu() {
        return dichVuChamSocService.layDanhSachDichVu();
    }

    public DichVuChamSoc layDichVuTheoMa(String maDichVu) {
        return dichVuChamSocService.layDichVuTheoMa(maDichVu);
    }

    public List<DichVuChamSoc> timKiemDichVu(String tuKhoa) {
        return dichVuChamSocService.timKiemDichVu(tuKhoa);
    }

    public String taoMaDichVu() {
        return dichVuChamSocService.taoMaDichVu();
    }

    public List<BangGiaDichVu> layBangGiaTheoDichVu(String maDichVu) {
        return dichVuChamSocService.layBangGiaTheoDichVu(maDichVu);
    }

    // Hàm cung cấp cho #68 (Lịch hẹn), #69 (Phiếu dịch vụ), #71 (Hóa đơn)
    public double traGiaTheoCanNang(String maDichVu, double canNang) {
        return dichVuChamSocService.traGiaTheoCanNang(maDichVu, canNang);
    }
}

