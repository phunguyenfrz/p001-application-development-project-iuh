package com.petshop.entity;

/**
 * Thực thể chi tiết dịch vụ trong một lần hẹn chăm sóc thú cưng (ánh xạ từ ChiTietLichHen).
 * Sử dụng kiểu nguyên thủy double theo Chuẩn IUH Directive.
 */
public class ChiTietDichVuLichHen {

    private final String tenDichVu;
    private final double canNangThuCung;
    private final double donGia;

    public ChiTietDichVuLichHen(String tenDichVu, double canNangThuCung, double donGia) {
        this.tenDichVu = tenDichVu;
        this.canNangThuCung = canNangThuCung;
        this.donGia = donGia;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public double getCanNangThuCung() {
        return canNangThuCung;
    }

    public double getDonGia() {
        return donGia;
    }
}
