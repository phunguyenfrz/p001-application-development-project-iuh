package com.petshop.entity;

/**
 * Thực thể chi tiết dịch vụ trong một lần hẹn chăm sóc thú cưng (ánh xạ từ ChiTietLichHen).
 * Sử dụng kiểu nguyên thủy double theo Chuẩn IUH Directive.
 */
public class ChiTietDichVuLichHen {

    private final DichVuChamSoc dichVu;
    private final double canNangThuCung;
    private final double donGia;

    public ChiTietDichVuLichHen(String tenDichVu, double canNangThuCung, double donGia) {
        this.dichVu = new DichVuChamSoc();
        this.dichVu.setTenDichVu(tenDichVu);
        this.canNangThuCung = canNangThuCung;
        this.donGia = donGia;
    }

    public DichVuChamSoc getDichVu() {
        return dichVu;
    }

    public String getTenDichVu() {
        return dichVu != null ? dichVu.getTenDichVu() : null;
    }

    public double getCanNangThuCung() {
        return canNangThuCung;
    }

    public double getDonGia() {
        return donGia;
    }
}
