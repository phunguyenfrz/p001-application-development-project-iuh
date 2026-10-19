package com.petshop.model;

import java.util.Objects;

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

    public static InvoiceItem createProductItem(String maSanPham, String tenHang, int soLuong, double donGia) {
        return new InvoiceItem(maSanPham, null, tenHang, soLuong, donGia);
    }

    public static InvoiceItem createServiceItem(String maDichVu, String tenHang, double donGia) {
        return new InvoiceItem(null, maDichVu, tenHang, 1, donGia);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvoiceItem that = (InvoiceItem) o;
        if (maChiTietHD != null && that.maChiTietHD != null) {
            return Objects.equals(maChiTietHD, that.maChiTietHD);
        }
        return Objects.equals(maHoaDon, that.maHoaDon) &&
                Objects.equals(maSanPham, that.maSanPham) &&
                Objects.equals(maDichVu, that.maDichVu);
    }

    @Override
    public int hashCode() {
        if (maChiTietHD != null) {
            return Objects.hash(maChiTietHD);
        }
        return Objects.hash(maHoaDon, maSanPham, maDichVu);
    }

    @Override
    public String toString() {
        return "InvoiceItem{" +
                "maChiTietHD='" + maChiTietHD + '\'' +
                ", tenHang='" + tenHang + '\'' +
                ", soLuong=" + soLuong +
                ", donGia=" + donGia +
                ", thanhTien=" + getThanhTien() +
                '}';
    }
}
