package com.petshop.entity;

/**
 * Thực thể ánh xạ bảng ChiTietHoaDon_DichVu trong cơ sở dữ liệu.
 * Lưu thông tin từng dòng dịch vụ chăm sóc thú cưng thuộc hóa đơn.
 * Tuân thủ Điều 1 và Điều 2 IUH: Kiểu double, phương thức tinhThanhTien().
 */
public class ChiTietHoaDonDichVu {

    private String maHoaDon;
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

    public String getMaDichVu() { return maDichVu; }
    public void setMaDichVu(String maDichVu) { this.maDichVu = maDichVu; }

    public String getTenDichVu() { return tenDichVu; }
    public void setTenDichVu(String tenDichVu) { this.tenDichVu = tenDichVu; }

    public String getMaThuCung() { return maThuCung; }
    public void setMaThuCung(String maThuCung) { this.maThuCung = maThuCung; }

    public String getTenThuCung() { return tenThuCung; }
    public void setTenThuCung(String tenThuCung) { this.tenThuCung = tenThuCung; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }
}
