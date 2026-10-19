package com.petshop.model;

import java.util.Objects;

/**
 * Model đại diện cho một sản phẩm trong cửa hàng (bảng SanPham).
 */
public class Product {
    private String maSanPham;     // SP-YYYYMMDD-NNNNNN
    private String maNhaCungCap;
    private String tenSanPham;
    private String maVach;
    private double giaBan;
    private int soLuongTon;
    private int nguongTonToiThieu = 10;
    private String donViTinh;
    private boolean trangThai;    // true: đang bán

    public Product() {}

    public Product(String maSanPham, String maNhaCungCap, String tenSanPham, String maVach,
                   double giaBan, int soLuongTon, String donViTinh, boolean trangThai) {
        this(maSanPham, maNhaCungCap, tenSanPham, maVach, giaBan, soLuongTon, 10, donViTinh, trangThai);
    }

    public Product(String maSanPham, String maNhaCungCap, String tenSanPham, String maVach,
                   double giaBan, int soLuongTon, int nguongTonToiThieu, String donViTinh, boolean trangThai) {
        this.maSanPham = maSanPham;
        this.maNhaCungCap = maNhaCungCap;
        this.tenSanPham = tenSanPham;
        this.maVach = maVach;
        this.giaBan = giaBan;
        this.soLuongTon = soLuongTon;
        this.nguongTonToiThieu = nguongTonToiThieu;
        this.donViTinh = donViTinh;
        this.trangThai = trangThai;
    }

    public String getMaSanPham()                    { return maSanPham; }
    public void setMaSanPham(String maSanPham)      { this.maSanPham = maSanPham; }

    public String getMaNhaCungCap()                     { return maNhaCungCap; }
    public void setMaNhaCungCap(String maNhaCungCap)    { this.maNhaCungCap = maNhaCungCap; }

    public String getTenSanPham()                   { return tenSanPham; }
    public void setTenSanPham(String tenSanPham)    { this.tenSanPham = tenSanPham; }

    public String getMaVach()               { return maVach; }
    public void setMaVach(String maVach)    { this.maVach = maVach; }

    public double getGiaBan()               { return giaBan; }
    public void setGiaBan(double giaBan)    { this.giaBan = giaBan; }

    public int getSoLuongTon()                  { return soLuongTon; }
    public void setSoLuongTon(int soLuongTon)   { this.soLuongTon = soLuongTon; }

    public int getNguongTonToiThieu()                       { return nguongTonToiThieu; }
    public void setNguongTonToiThieu(int nguongTonToiThieu) { this.nguongTonToiThieu = nguongTonToiThieu; }

    /** Kiểm tra sản phẩm có đang ở mức sắp hết hàng hay không. */
    public boolean isSapHetHang() {
        return soLuongTon <= nguongTonToiThieu;
    }

    public String getDonViTinh()                { return donViTinh; }
    public void setDonViTinh(String donViTinh)  { this.donViTinh = donViTinh; }

    public boolean isTrangThai()                { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(maSanPham, product.maSanPham);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maSanPham);
    }

    @Override
    public String toString() {
        return "Product{ma='" + maSanPham + "', ten='" + tenSanPham + "', giaBan=" + giaBan
                + ", ton=" + soLuongTon + ", nguongToiThieu=" + nguongTonToiThieu + "}";
    }
}
