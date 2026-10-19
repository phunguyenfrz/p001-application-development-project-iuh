package com.petshop.model;

/**
 * Model đại diện cho một dòng trong hóa đơn (bảng ChiTietHoaDon).
 * Mỗi dòng là sản phẩm HOẶC dịch vụ (đúng một trong hai).
 */
public class InvoiceItem {
    private String maChiTietHD;   // CT-YYYYMMDD-NNNNNN
    private String maHoaDon;
    private String maSanPham;     // null nếu là dịch vụ
    private String maDichVu;      // null nếu là sản phẩm
    private String tenHang;       // tên hiển thị (không lưu DB)
    private int soLuong = 1;
    private double donGia;

    public InvoiceItem() {}

    public InvoiceItem(String maSanPham, String maDichVu, String tenHang, int soLuong, double donGia) {
        this.maSanPham = maSanPham;
        this.maDichVu = maDichVu;
        this.tenHang = tenHang;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    public String getMaChiTietHD()                      { return maChiTietHD; }
    public void setMaChiTietHD(String maChiTietHD)      { this.maChiTietHD = maChiTietHD; }

    public String getMaHoaDon()                 { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon)    { this.maHoaDon = maHoaDon; }

    public String getMaSanPham()                    { return maSanPham; }
    public void setMaSanPham(String maSanPham)      { this.maSanPham = maSanPham; }

    public String getMaDichVu()                 { return maDichVu; }
    public void setMaDichVu(String maDichVu)    { this.maDichVu = maDichVu; }

    public String getTenHang()              { return tenHang; }
    public void setTenHang(String tenHang)  { this.tenHang = tenHang; }

    public int getSoLuong()                 { return soLuong; }
    public void setSoLuong(int soLuong)     { this.soLuong = soLuong; }

    public double getDonGia()               { return donGia; }
    public void setDonGia(double donGia)    { this.donGia = donGia; }

    public boolean isService() { return maDichVu != null; }

    public double getThanhTien() { return donGia * soLuong; }
}
