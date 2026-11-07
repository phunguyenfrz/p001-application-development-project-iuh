package com.petshop.entity;

/**
 * Chi tiết dịch vụ trong một lịch hẹn chăm sóc thú cưng.
 * Chuẩn hóa OOP với Object Reference DichVuChamSoc, kiểu nguyên thủy double theo Chuẩn IUH.
 */
public class ChiTietLichHen {

    private String maLichHen;
    private DichVuChamSoc dichVu;
    private String maDichVuTam;
    private String tenDichVuTam;
    private ThuCung thuCung;
    private String tenThuCungTam;
    private double canNangThuCung;
    private double donGia;

    public ChiTietLichHen() {
        this.canNangThuCung = 0.0;
        this.donGia = 0.0;
    }

    public ChiTietLichHen(String maLichHen, String maDichVu, double canNangThuCung, double donGia) {
        this.maLichHen = maLichHen;
        this.maDichVuTam = maDichVu;
        this.canNangThuCung = canNangThuCung;
        this.donGia = donGia;
    }

    public ChiTietLichHen(String maLichHen, String maDichVu, String tenDichVu, double canNangThuCung, double donGia) {
        this.maLichHen = maLichHen;
        this.maDichVuTam = maDichVu;
        this.tenDichVuTam = tenDichVu;
        this.canNangThuCung = canNangThuCung;
        this.donGia = donGia;
    }

    public ChiTietLichHen(String maLichHen, DichVuChamSoc dichVu, double canNangThuCung, double donGia) {
        this.maLichHen = maLichHen;
        this.dichVu = dichVu;
        if (dichVu != null) {
            this.maDichVuTam = dichVu.getMaDichVu();
            this.tenDichVuTam = dichVu.getTenDichVu();
        }
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
        if (dichVu != null) {
            this.maDichVuTam = dichVu.getMaDichVu();
            this.tenDichVuTam = dichVu.getTenDichVu();
        }
    }

    // Delegate Getters / Setters để TableView không bị gãy
    public String getMaDichVu() {
        return dichVu != null ? dichVu.getMaDichVu() : maDichVuTam;
    }

    public void setMaDichVu(String maDichVu) {
        this.maDichVuTam = maDichVu;
        if (this.dichVu == null) {
            this.dichVu = new DichVuChamSoc();
        }
        this.dichVu.setMaDichVu(maDichVu);
    }

    public String getTenDichVu() {
        return dichVu != null ? dichVu.getTenDichVu() : (tenDichVuTam != null ? tenDichVuTam : "");
    }

    public void setTenDichVu(String tenDichVu) {
        this.tenDichVuTam = tenDichVu;
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

    // Phương thức tự tính thành tiền (Thuộc tính dẫn xuất - Điều 2)
    
    public ThuCung getThuCung() {
        return thuCung;
    }

    public void setThuCung(ThuCung thuCung) {
        this.thuCung = thuCung;
        if (thuCung != null) {
            this.tenThuCungTam = thuCung.getTenThuCung();
        }
    }

    public String getTenThuCung() {
        return thuCung != null ? thuCung.getTenThuCung() : (tenThuCungTam != null ? tenThuCungTam : "");
    }

    public void setTenThuCung(String tenThuCung) {
        this.tenThuCungTam = tenThuCung;
        if (this.thuCung == null) {
            this.thuCung = new ThuCung();
        }
        this.thuCung.setTenThuCung(tenThuCung);
    }

    public String getMaThuCung() {
        return thuCung != null ? thuCung.getMaThuCung() : null;
    }

    public double tinhThanhTien() {
        return donGia;
    }
}
