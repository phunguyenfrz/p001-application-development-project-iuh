package com.petshop.entity;

import java.util.Objects;

/**
 * Thực thể Sản phẩm (hàng hóa) trong cửa hàng Happy Tails.
 * Khớp bảng SanPham trong CSDL SQL Server.
 * Tuân thủ quy định: sử dụng kiểu double cho tiền tệ.
 */
public class SanPham {

    private String maSanPham;
    private String maNhaCungCap;
    private String tenSanPham;
    private String maVach;
    private double giaNhap;
    private double giaBan;
    private int soLuongTon;
    private int nguongTonToiThieu;
    private String donViTinh;
    private boolean trangThai; // true: đang kinh doanh, false: ngừng kinh doanh (xóa mềm)

    // Tên nhà cung cấp hiển thị trên bảng biểu
    private String tenNhaCungCap;

    public SanPham() {
        this.giaNhap = 0;
        this.giaBan = 0;
        this.soLuongTon = 0;
        this.nguongTonToiThieu = 10;
        this.trangThai = true;
    }

    public SanPham(String maSanPham, String maNhaCungCap, String tenSanPham, String maVach,
                   double giaNhap, double giaBan, int soLuongTon, int nguongTonToiThieu, String donViTinh, boolean trangThai) {
        this.maSanPham = maSanPham;
        this.maNhaCungCap = maNhaCungCap;
        this.tenSanPham = tenSanPham;
        this.maVach = maVach;
        this.giaNhap = giaNhap;
        this.giaBan = giaBan;
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

    public double getGiaNhap() {
        return giaNhap;
    }

    public void setGiaNhap(double giaNhap) {
        this.giaNhap = giaNhap;
    }

    public double getGiaBan() {
        return giaBan;
    }

    public void setGiaBan(double giaBan) {
        this.giaBan = giaBan;
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

    public boolean isConHang() {
        return this.soLuongTon > this.nguongTonToiThieu;
    }

    /**
     * Tình trạng tồn kho theo 3 mức chuẩn (Task Tồn kho):
     * - "Hết hàng": số lượng tồn <= 0
     * - "Sắp hết hàng": 0 < số lượng tồn <= ngưỡng tồn tối thiểu
     * - "Còn hàng": số lượng tồn > ngưỡng tồn tối thiểu
     */
    public String getTinhTrangTonKho() {
        if (isHetHang()) {
            return "Hết hàng";
        }
        if (isSapHetHang()) {
            return "Sắp hết hàng";
        }
        return "Còn hàng";
    }

    /**
     * Trạng thái hiển thị sản phẩm:
     * - Nếu trangThai == false: "Ngừng kinh doanh"
     * - Nếu trangThai == true và soLuongTon <= 0: "Hết hàng"
     * - Nếu trangThai == true và soLuongTon > 0 và <= nguong: "Sắp hết hàng"
     * - Nếu trangThai == true và soLuongTon > nguong: "Đang kinh doanh"
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SanPham sanPham = (SanPham) o;
        return Objects.equals(maSanPham, sanPham.maSanPham);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maSanPham);
    }

    @Override
    public String toString() {
        return "SanPham{" +
                "maSanPham='" + maSanPham + '\'' +
                ", tenSanPham='" + tenSanPham + '\'' +
                ", giaNhap=" + giaNhap +
                ", giaBan=" + giaBan +
                ", soLuongTon=" + soLuongTon +
                ", nguongTonToiThieu=" + nguongTonToiThieu +
                ", donViTinh='" + donViTinh + '\'' +
                ", trangThai=" + trangThai +
                '}';
    }
}
