package com.petshop.model;

import java.util.Objects;

/**
 * Model đại diện cho một dòng trong hóa đơn (bảng ChiTietHoaDon).
 * Mỗi dòng là sản phẩm HOẶC dịch vụ (đúng một trong hai).
 */
public class InvoiceItem {
    public enum LineType {
        SAN_PHAM,
        DICH_VU
    }

    private String maChiTietHD;   // CT-YYYYMMDD-NNNNNN
    private String maHoaDon;
    private String maSanPham;     // null nếu là dịch vụ
    private String maDichVu;      // null nếu là sản phẩm
    private String tenHang;       // tên hiển thị (không lưu DB)
    private int soLuong = 1;
    private double donGia;
    private boolean khoa;         // true nếu dòng dịch vụ đến từ lịch hẹn

    public InvoiceItem() {}

    public InvoiceItem(String maSanPham, String maDichVu, String tenHang, int soLuong, double donGia) {
        this.maSanPham = maSanPham;
        this.maDichVu = maDichVu;
        this.tenHang = tenHang;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    public InvoiceItem(String maSanPham, String maDichVu, String tenHang, int soLuong,
                       double donGia, boolean khoa) {
        this(maSanPham, maDichVu, tenHang, soLuong, donGia);
        this.khoa = khoa;
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

    public String getTenHienThi() { return tenHang; }
    public void setTenHienThi(String tenHienThi) { this.tenHang = tenHienThi; }

    public int getSoLuong()                 { return soLuong; }
    public void setSoLuong(int soLuong) {
        if (khoa && soLuong != this.soLuong) {
            throw new IllegalStateException("Không thể sửa số lượng dịch vụ từ lịch hẹn.");
        }
        if (soLuong < 1) throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
        this.soLuong = soLuong;
    }

    public double getDonGia()               { return donGia; }
    public void setDonGia(double donGia) {
        if (khoa && Double.compare(donGia, this.donGia) != 0) {
            throw new IllegalStateException("Không thể sửa giá dịch vụ từ lịch hẹn.");
        }
        if (donGia < 0) throw new IllegalArgumentException("Đơn giá không được âm.");
        this.donGia = donGia;
    }

    public boolean isService() { return maDichVu != null; }

    public LineType getLoaiDong() { return isService() ? LineType.DICH_VU : LineType.SAN_PHAM; }

    public boolean isKhoa() { return khoa; }

    public void setKhoa(boolean khoa) { this.khoa = khoa; }

    public double getThanhTien() { return thanhTien(); }

    public double thanhTien() { return donGia * soLuong; }

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
