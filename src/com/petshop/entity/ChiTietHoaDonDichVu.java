package com.petshop.entity;

/**
 * Thực thể ánh xạ bảng ChiTietHoaDon_DichVu trong cơ sở dữ liệu.
 * Lưu thông tin từng dòng dịch vụ chăm sóc thú cưng thuộc hóa đơn.
 * Tuân thủ Điều 1 và Điều 2 IUH: Kiểu double, phương thức tinhThanhTien().
 */
public class ChiTietHoaDonDichVu {

    private String maHoaDon;
    private DichVuChamSoc dichVu;
    private ThuCung thuCung;
    private int soLuong; // Mặc định là 1 theo ràng buộc CHECK (soLuong = 1)
    private double donGia;

    public ChiTietHoaDonDichVu() {
        this.soLuong = 1;
        this.donGia = 0.0;
    }

    public ChiTietHoaDonDichVu(String maHoaDon, String maDichVu, String tenDichVu, String maThuCung, String tenThuCung, double donGia) {
        this();
        this.maHoaDon = maHoaDon;
        setMaDichVu(maDichVu);
        setTenDichVu(tenDichVu);
        setMaThuCung(maThuCung);
        setTenThuCung(tenThuCung);
        this.donGia = donGia;
    }

    /**
     * Phương thức tự tính thành tiền dẫn xuất: soLuong * donGia (Điều 2 IUH).
     */
    public double tinhThanhTien() {
        return this.soLuong * this.donGia;
    }

    public double getThanhTien() {
        return tinhThanhTien();
    }

    // Getters and Setters
    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }

    public DichVuChamSoc getDichVu() { return dichVu; }
    public void setDichVu(DichVuChamSoc dichVu) {
        this.dichVu = dichVu;
    }

    public ThuCung getThuCung() { return thuCung; }
    public void setThuCung(ThuCung thuCung) {
        this.thuCung = thuCung;
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
        return dichVu != null ? dichVu.getTenDichVu() : null;
    }
    public void setTenDichVu(String tenDichVu) {
        if (this.dichVu == null) {
            this.dichVu = new DichVuChamSoc();
        }
        this.dichVu.setTenDichVu(tenDichVu);
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
        return thuCung != null ? thuCung.getTenThuCung() : null;
    }
    public void setTenThuCung(String tenThuCung) {
        if (this.thuCung == null) {
            this.thuCung = new ThuCung();
        }
        this.thuCung.setTenThuCung(tenThuCung);
    }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }
}
