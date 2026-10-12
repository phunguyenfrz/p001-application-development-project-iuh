package com.petshop.model;

import java.math.BigDecimal;

/**
 * Model đại diện cho một sản phẩm trong cửa hàng (bảng SanPham).
 */
public class Product {
    private String maSanPham;     // SP-YYYYMMDD-NNNNNN
    private String maNhaCungCap;
    private String tenSanPham;
    private String maVach;
    private BigDecimal giaBan = BigDecimal.ZERO;
    private int soLuongTon;
    private String donViTinh;
    private boolean trangThai;    // true: đang bán
    private int nguongTonToiThieu = 10;

    public Product() {}

    public Product(String maSanPham, String maNhaCungCap, String tenSanPham, String maVach,
                   double giaBan, int soLuongTon, String donViTinh, boolean trangThai) {
        this(maSanPham, maNhaCungCap, tenSanPham, maVach, BigDecimal.valueOf(giaBan),
                soLuongTon, donViTinh, trangThai, 10);
    }

    public Product(String maSanPham, String maNhaCungCap, String tenSanPham, String maVach,
                   BigDecimal giaBan, int soLuongTon, String donViTinh, boolean trangThai,
                   int nguongTonToiThieu) {
        this.maSanPham = maSanPham;
        this.maNhaCungCap = maNhaCungCap;
        this.tenSanPham = tenSanPham;
        this.maVach = maVach;
        this.giaBan = giaBan == null ? BigDecimal.ZERO : giaBan;
        this.soLuongTon = soLuongTon;
        this.donViTinh = donViTinh;
        this.trangThai = trangThai;
        this.nguongTonToiThieu = nguongTonToiThieu;
    }

    public String getMaSanPham()                    { return maSanPham; }
    public void setMaSanPham(String maSanPham)      { this.maSanPham = maSanPham; }

    public String getMaNhaCungCap()                     { return maNhaCungCap; }
    public void setMaNhaCungCap(String maNhaCungCap)    { this.maNhaCungCap = maNhaCungCap; }

    public String getTenSanPham()                   { return tenSanPham; }
    public void setTenSanPham(String tenSanPham)    { this.tenSanPham = tenSanPham; }

    public String getMaVach()               { return maVach; }
    public void setMaVach(String maVach)    { this.maVach = maVach; }

    public double getGiaBan()               { return giaBan.doubleValue(); }
    public void setGiaBan(double giaBan)    { this.giaBan = BigDecimal.valueOf(giaBan); }
    public BigDecimal getGiaBanDecimal()    { return giaBan; }
    public void setGiaBan(BigDecimal giaBan) { this.giaBan = giaBan; }

    public int getSoLuongTon()                  { return soLuongTon; }
    public void setSoLuongTon(int soLuongTon)   { this.soLuongTon = soLuongTon; }

    public String getDonViTinh()                { return donViTinh; }
    public void setDonViTinh(String donViTinh)  { this.donViTinh = donViTinh; }

    public boolean isTrangThai()                { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }
    public int getNguongTonToiThieu()           { return nguongTonToiThieu; }
    public void setNguongTonToiThieu(int value) { this.nguongTonToiThieu = value; }

    @Override
    public String toString() {
        return "Product{ma='" + maSanPham + "', ten='" + tenSanPham + "', giaBan=" + giaBan
                + ", ton=" + soLuongTon + "}";
    }
}
