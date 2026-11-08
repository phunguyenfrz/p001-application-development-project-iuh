package com.petshop.entity;

/**
 * Thực thể chi tiết dịch vụ trong một phiếu tiếp nhận thú cưng làm dịch vụ.
 * Chuẩn hóa OOP với Object Reference (ThuCung, DichVuChamSoc),
 * kiểu nguyên thủy double, thuộc tính dẫn xuất tinhThanhTien() (IUH Directive).
 */
public class ChiTietPhieuDichVu {

    private PhieuDichVu phieuDichVu;
    private ThuCung thuCung;
    private DichVuChamSoc dichVu;
    private double donGia;

    public ChiTietPhieuDichVu() {
        this.donGia = 0.0;
    }

    public ChiTietPhieuDichVu(String maPhieuDichVu, String maThuCung, String tenThuCung,
                              String maDichVu, String tenDichVu, double donGia) {
        setMaPhieuDichVu(maPhieuDichVu);
        setMaThuCung(maThuCung);
        setTenThuCung(tenThuCung);
        setMaDichVu(maDichVu);
        setTenDichVu(tenDichVu);
        this.donGia = donGia;
    }

    // =========================================================================
    // GETTERS & SETTERS (OOP & DELEGATE GETTERS)
    // =========================================================================

    public PhieuDichVu getPhieuDichVu() {
        return phieuDichVu;
    }

    public void setPhieuDichVu(PhieuDichVu phieuDichVu) {
        this.phieuDichVu = phieuDichVu;
    }

    public String getMaPhieuDichVu() {
        return phieuDichVu != null ? phieuDichVu.getMaPhieuDichVu() : null;
    }

    public void setMaPhieuDichVu(String maPhieuDichVu) {
        if (maPhieuDichVu == null) {
            this.phieuDichVu = null;
            return;
        }
        if (this.phieuDichVu == null) {
            this.phieuDichVu = new PhieuDichVu();
        }
        this.phieuDichVu.setMaPhieuDichVu(maPhieuDichVu);
    }

    public ThuCung getThuCung() {
        return thuCung;
    }

    public void setThuCung(ThuCung thuCung) {
        this.thuCung = thuCung;
    }

    public String getMaThuCung() {
        return thuCung != null ? thuCung.getMaThuCung() : null;
    }

    public void setMaThuCung(String maThuCung) {
        if (maThuCung == null) {
            if (this.thuCung != null) {
                this.thuCung.setMaThuCung(null);
            }
            return;
        }
        if (this.thuCung == null) {
            this.thuCung = new ThuCung();
        }
        this.thuCung.setMaThuCung(maThuCung);
    }

    public String getTenThuCung() {
        if (thuCung != null && thuCung.getTenThuCung() != null) {
            return thuCung.getTenThuCung();
        }
        return "";
    }

    public void setTenThuCung(String tenThuCung) {
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
    }

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
        if (dichVu != null && dichVu.getTenDichVu() != null) {
            return dichVu.getTenDichVu();
        }
        return "";
    }

    public void setTenDichVu(String tenDichVu) {
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
