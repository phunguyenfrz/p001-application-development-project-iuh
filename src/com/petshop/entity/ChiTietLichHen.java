package com.petshop.entity;

/**
 * Chi tiết dịch vụ trong một lịch hẹn chăm sóc thú cưng.
 * Chuẩn hóa OOP với Object Reference DichVuChamSoc, kiểu nguyên thủy double theo Chuẩn IUH.
 */
public class ChiTietLichHen {

    private String maLichHen;
    private DichVuChamSoc dichVu;
    private ThuCung thuCung;
    private double canNangThuCung;
    private double donGia;

    public ChiTietLichHen() {
        this.canNangThuCung = 0.0;
        this.donGia = 0.0;
    }

    public ChiTietLichHen(String maLichHen, String maDichVu, String tenDichVu, double canNangThuCung, double donGia) {
        this.maLichHen = maLichHen;
        setMaDichVu(maDichVu);
        setTenDichVu(tenDichVu);
        this.canNangThuCung = canNangThuCung;
        this.donGia = donGia;
    }

    public String getMaLichHen() {
        return maLichHen;
    }

    public void setMaLichHen(String maLichHen) {
        this.maLichHen = maLichHen;
    }

    public DichVuChamSoc getDichVu() {
        return dichVu;
    }

    public void setDichVu(DichVuChamSoc dichVu) {
        this.dichVu = dichVu;
    }

    // Delegate Getters / Setters để TableView không bị gãy
    public String getMaDichVu() {
        return dichVu != null ? dichVu.getMaDichVu() : null;
    }

    public void setMaDichVu(String maDichVu) {
        if (maDichVu == null) {
            if (this.dichVu != null) {
                this.dichVu.setMaDichVu(null);
            }
            return;
        }
        if (this.dichVu == null) {
            this.dichVu = new DichVuChamSoc();
        }
        this.dichVu.setMaDichVu(maDichVu);
    }

    public String getTenDichVu() {
        return (dichVu != null && dichVu.getTenDichVu() != null) ? dichVu.getTenDichVu() : "";
    }

    public void setTenDichVu(String tenDichVu) {
        if (this.dichVu == null) {
            this.dichVu = new DichVuChamSoc();
        }
        this.dichVu.setTenDichVu(tenDichVu);
    }

    public double getCanNangThuCung() {
        return canNangThuCung;
    }

    public void setCanNangThuCung(double canNangThuCung) {
        this.canNangThuCung = canNangThuCung;
    }

    public double getDonGia() {
        return donGia;
    }

    public void setDonGia(double donGia) {
        this.donGia = donGia;
    }

    public ThuCung getThuCung() {
        return thuCung;
    }

    public void setThuCung(ThuCung thuCung) {
        this.thuCung = thuCung;
    }

    public String getTenThuCung() {
        return (thuCung != null && thuCung.getTenThuCung() != null) ? thuCung.getTenThuCung() : "";
    }

    public void setTenThuCung(String tenThuCung) {
        if (this.thuCung == null) {
            this.thuCung = new ThuCung();
        }
        this.thuCung.setTenThuCung(tenThuCung);
    }

    public String getMaThuCung() {
        return thuCung != null ? thuCung.getMaThuCung() : null;
    }
}
