package com.petshop.model;

import java.math.BigDecimal;

/**
 * Thực thể chi tiết dịch vụ trong một lần hẹn chăm sóc thú cưng (ánh xạ từ ChiTietLichHen).
 */
public class ChiTietDichVuLichHen {

    private final String tenDichVu;
    private final BigDecimal canNangThuCung;
    private final BigDecimal donGia;

    public ChiTietDichVuLichHen(String tenDichVu, BigDecimal canNangThuCung, BigDecimal donGia) {
        this.tenDichVu = tenDichVu;
        this.canNangThuCung = canNangThuCung != null ? canNangThuCung : BigDecimal.ZERO;
        this.donGia = donGia != null ? donGia : BigDecimal.ZERO;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public BigDecimal getCanNangThuCung() {
        return canNangThuCung;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }
}

