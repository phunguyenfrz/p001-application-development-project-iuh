package com.petshop.entity;

/**
 * Thực thể chi tiết dịch vụ trong một phiếu tiếp nhận thú cưng làm dịch vụ.
 * Chuẩn hóa OOP với Object Reference (ThuCung, DichVuChamSoc),
 * kiểu nguyên thủy double, thuộc tính dẫn xuất tinhThanhTien() (IUH Directive).
 */
public class ChiTietPhieuDichVu {

    private String maPhieuDichVu;
    private ThuCung thuCung;
    private DichVuChamSoc dichVu;
    private String maThuCungTam;
    private String tenThuCungTam;
    private String maDichVuTam;
    private String tenDichVuTam;
    private double donGia;

    public ChiTietPhieuDichVu() {
        this.donGia = 0.0;
    }

    public ChiTietPhieuDichVu(String maPhieuDichVu, String maThuCung, String maDichVu, double donGia) {
        this.maPhieuDichVu = maPhieuDichVu;
        this.maThuCungTam = maThuCung;
        this.maDichVuTam = maDichVu;
        this.donGia = donGia;
    }

    public ChiTietPhieuDichVu(String maPhieuDichVu, String maThuCung, String tenThuCung,
                              String maDichVu, String tenDichVu, double donGia) {
        this.maPhieuDichVu = maPhieuDichVu;
        this.maThuCungTam = maThuCung;
        this.tenThuCungTam = tenThuCung;
        this.maDichVuTam = maDichVu;
        this.tenDichVuTam = tenDichVu;
        this.donGia = donGia;
    }

    public ChiTietPhieuDichVu(String maPhieuDichVu, ThuCung thuCung, DichVuChamSoc dichVu, double donGia) {
        this.maPhieuDichVu = maPhieuDichVu;
        this.thuCung = thuCung;
        this.dichVu = dichVu;
        if (thuCung != null) {
            this.maThuCungTam = thuCung.getMaThuCung();
            this.tenThuCungTam = thuCung.getTenThuCung();
        }
        if (dichVu != null) {
            this.maDichVuTam = dichVu.getMaDichVu();
            this.tenDichVuTam = dichVu.getTenDichVu();
        }
        this.donGia = donGia;
    }

    // =========================================================================
    // GETTERS & SETTERS (OOP & DELEGATE GETTERS)
    // =========================================================================

    public String getMaPhieuDichVu() {
        return maPhieuDichVu;
    }

    public void setMaPhieuDichVu(String maPhieuDichVu) {
        this.maPhieuDichVu = maPhieuDichVu;
    }

    public ThuCung getThuCung() {
        return thuCung;
    }

    public void setThuCung(ThuCung thuCung) {
        this.thuCung = thuCung;
        if (thuCung != null) {
            this.maThuCungTam = thuCung.getMaThuCung();
            this.tenThuCungTam = thuCung.getTenThuCung();
        }
    }

    public String getMaThuCung() {
        return thuCung != null ? thuCung.getMaThuCung() : maThuCungTam;
    }

    public void setMaThuCung(String maThuCung) {
        this.maThuCungTam = maThuCung;
        if (this.thuCung == null) {
            this.thuCung = new ThuCung();
        }
        this.thuCung.setMaThuCung(maThuCung);
    }

    public String getTenThuCung() {
        if (thuCung != null && thuCung.getTenThuCung() != null) {
            return thuCung.getTenThuCung();
        }
        return tenThuCungTam != null ? tenThuCungTam : "";
    }

    public void setTenThuCung(String tenThuCung) {
        this.tenThuCungTam = tenThuCung;
        if (this.thuCung == null) {
            this.thuCung = new ThuCung();
        }
        this.thuCung.setTenThuCung(tenThuCung);
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
        if (dichVu != null && dichVu.getTenDichVu() != null) {
            return dichVu.getTenDichVu();
        }
        return tenDichVuTam != null ? tenDichVuTam : "";
    }

    public void setTenDichVu(String tenDichVu) {
        this.tenDichVuTam = tenDichVu;
        if (this.dichVu == null) {
            this.dichVu = new DichVuChamSoc();
        }
        this.dichVu.setTenDichVu(tenDichVu);
    }

    public double getDonGia() {
        return donGia;
    }

    public void setDonGia(double donGia) {
        this.donGia = donGia;
    }

    // Phương thức tự tính thành tiền (Thuộc tính dẫn xuất - Điều 2)
    public double tinhThanhTien() {
        return donGia;
    }
}
