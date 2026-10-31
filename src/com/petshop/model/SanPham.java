package com.petshop.model;

import java.math.BigDecimal;

/**
 * Thực thể Sản phẩm (hàng hóa) trong cửa hàng Happy Tails.
 * Khớp bảng SanPham trong CSDL SQL Server.
 */
public class SanPham {

    private String maSanPham;
    private String maNhaCungCap;
    private String tenSanPham;
    private String maVach;
    private BigDecimal giaBan;
    private int soLuongTon;
    private int nguongTonToiThieu;
    private String donViTinh;
    private boolean trangThai; // true: đang kinh doanh, false: ngừng kinh doanh (xóa mềm)

    // Tên nhà cung cấp hiển thị trên bảng biểu
    private String tenNhaCungCap;

    public SanPham() {
        this.giaBan = BigDecimal.ZERO;
        this.soLuongTon = 0;
        this.nguongTonToiThieu = 10;
        this.trangThai = true;
    }

    public SanPham(String maSanPham, String maNhaCungCap, String tenSanPham, String maVach,
                   BigDecimal giaBan, int soLuongTon, int nguongTonToiThieu, String donViTinh, boolean trangThai) {
        this.maSanPham = maSanPham;
        this.maNhaCungCap = maNhaCungCap;
        this.tenSanPham = tenSanPham;
        this.maVach = maVach;
        this.giaBan = giaBan != null ? giaBan : BigDecimal.ZERO;
        this.soLuongTon = soLuongTon;
        this.nguongTonToiThieu = nguongTonToiThieu;
        this.donViTinh = donViTinh;
        this.trangThai = trangThai;
    }

    public String getMaSanPham() {
        return maSanPham;
    }

    public void setMaSanPham(String maSanPham) {
        this.maSanPham = maSanPham;
    }

    public String getMaNhaCungCap() {
        return maNhaCungCap;
    }

    public void setMaNhaCungCap(String maNhaCungCap) {
        this.maNhaCungCap = maNhaCungCap;
    }

    public String getTenSanPham() {
        return tenSanPham;
    }

    public void setTenSanPham(String tenSanPham) {
        this.tenSanPham = tenSanPham;
    }

    public String getMaVach() {
        return maVach;
    }

    public void setMaVach(String maVach) {
        this.maVach = maVach;
    }

    public BigDecimal getGiaBan() {
        return giaBan;
    }

    public void setGiaBan(BigDecimal giaBan) {
        this.giaBan = giaBan != null ? giaBan : BigDecimal.ZERO;
    }

    public int getSoLuongTon() {
        return soLuongTon;
    }

    public void setSoLuongTon(int soLuongTon) {
        this.soLuongTon = soLuongTon;
    }

    public int getNguongTonToiThieu() {
        return nguongTonToiThieu;
    }

    public void setNguongTonToiThieu(int nguongTonToiThieu) {
        this.nguongTonToiThieu = nguongTonToiThieu;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }

    public String getTenNhaCungCap() {
        return tenNhaCungCap;
    }

    public void setTenNhaCungCap(String tenNhaCungCap) {
        this.tenNhaCungCap = tenNhaCungCap;
    }

    public boolean isSapHetHang() {
        return this.soLuongTon > 0 && this.soLuongTon <= this.nguongTonToiThieu;
    }

    public boolean isHetHang() {
        return this.soLuongTon <= 0;
    }

    /**
     * Trạng thái hiển thị sản phẩm:
     * - Nếu trangThai == false: "Ngừng kinh doanh"
     * - Nếu trangThai == true và soLuongTon <= 0: "Hết hàng"
     * - Nếu trangThai == true và soLuongTon > 0: "Đang kinh doanh"
     */
    public String getTrangThaiHienThi() {
        if (!this.trangThai) {
            return "Ngừng kinh doanh";
        }
        if (this.soLuongTon <= 0) {
            return "Hết hàng";
        }
        if (isSapHetHang()) {
            return "Sắp hết hàng";
        }
        return "Đang kinh doanh";
    }
}

