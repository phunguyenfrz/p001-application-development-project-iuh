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
    private String maDichVu;
    private String tenDichVu;
    private String maThuCung;
    private String tenThuCung;
    private int soLuong; // Mặc định là 1 theo ràng buộc CHECK (soLuong = 1)
    private double donGia;

    public ChiTietHoaDonDichVu() {
        this.soLuong = 1;
        this.donGia = 0.0;
    }

    public ChiTietHoaDonDichVu(DichVuChamSoc dichVu, ThuCung thuCung, double donGia) {
        this();
        this.dichVu = dichVu;
        this.thuCung = thuCung;
        this.donGia = donGia;
        if (dichVu != null) {
            this.maDichVu = dichVu.getMaDichVu();
            this.tenDichVu = dichVu.getTenDichVu();
        }
        if (thuCung != null) {
            this.maThuCung = thuCung.getMaThuCung();
            this.tenThuCung = thuCung.getTenThuCung();
        }
    }

    public ChiTietHoaDonDichVu(String maHoaDon, DichVuChamSoc dichVu, ThuCung thuCung, double donGia) {
        this(dichVu, thuCung, donGia);
        this.maHoaDon = maHoaDon;
    }

    public ChiTietHoaDonDichVu(String maHoaDon, String maDichVu, int soLuong, double donGia) {
        this.maHoaDon = maHoaDon;
        this.maDichVu = maDichVu;
        this.soLuong = 1;
        this.donGia = donGia;
    }

    public ChiTietHoaDonDichVu(String maHoaDon, String maDichVu, String tenDichVu, String maThuCung, String tenThuCung, double donGia) {
        this(maHoaDon, maDichVu, 1, donGia);
        this.tenDichVu = tenDichVu;
        this.maThuCung = maThuCung;
        this.tenThuCung = tenThuCung;
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
        if (dichVu != null) {
            this.maDichVu = dichVu.getMaDichVu();
            if (dichVu.getTenDichVu() != null) {
                this.tenDichVu = dichVu.getTenDichVu();
            }
        }
    }

    public ThuCung getThuCung() { return thuCung; }
    public void setThuCung(ThuCung thuCung) {
        this.thuCung = thuCung;
        if (thuCung != null) {
            this.maThuCung = thuCung.getMaThuCung();
            if (thuCung.getTenThuCung() != null) {
                this.tenThuCung = thuCung.getTenThuCung();
            }
        }
    }

    public String getMaDichVu() {
        if (maDichVu != null) return maDichVu;
        return dichVu != null ? dichVu.getMaDichVu() : null;
    }
    public void setMaDichVu(String maDichVu) {
        this.maDichVu = maDichVu;
        if (dichVu != null && (dichVu.getMaDichVu() == null || !dichVu.getMaDichVu().equals(maDichVu))) {
            dichVu.setMaDichVu(maDichVu);
        }
    }

    public String getTenDichVu() {
        if (tenDichVu != null) return tenDichVu;
        return dichVu != null ? dichVu.getTenDichVu() : null;
    }
    public void setTenDichVu(String tenDichVu) {
        this.tenDichVu = tenDichVu;
        if (dichVu != null) {
            dichVu.setTenDichVu(tenDichVu);
        }
    }

    public String getMaThuCung() {
        if (maThuCung != null) return maThuCung;
        return thuCung != null ? thuCung.getMaThuCung() : null;
    }
    public void setMaThuCung(String maThuCung) {
        this.maThuCung = maThuCung;
        if (thuCung != null && (thuCung.getMaThuCung() == null || !thuCung.getMaThuCung().equals(maThuCung))) {
            thuCung.setMaThuCung(maThuCung);
        }
    }

    public String getTenThuCung() {
        if (tenThuCung != null) return tenThuCung;
        return thuCung != null ? thuCung.getTenThuCung() : null;
    }
    public void setTenThuCung(String tenThuCung) {
        this.tenThuCung = tenThuCung;
        if (thuCung != null) {
            thuCung.setTenThuCung(tenThuCung);
        }
    }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }
}
