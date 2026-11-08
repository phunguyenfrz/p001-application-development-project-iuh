package com.petshop.entity;

import java.util.Objects;

/**
 * Thực thể Sản phẩm (hàng hóa) trong cửa hàng Happy Tails.
 * Khớp bảng SanPham trong CSDL SQL Server.
 * Tuân thủ quy định: sử dụng kiểu double cho tiền tệ và tham chiếu đối tượng NhaCungCap.
 */
public class SanPham {

    private String maSanPham;
    private NhaCungCap nhaCungCap;
    private String tenSanPham;
    private String maVach;
    private double giaNhap;
    private double giaBan;
    private int soLuongTon;
    private int nguongTonToiThieu;
    private String donViTinh;
    private boolean trangThai; // true: đang kinh doanh, false: ngừng kinh doanh (xóa mềm)

    public SanPham() {
        this.nhaCungCap = new NhaCungCap();
        this.giaNhap = 0;
        this.giaBan = 0;
        this.soLuongTon = 0;
        this.nguongTonToiThieu = 10;
        this.trangThai = true;
    }

    public SanPham(String maSanPham, NhaCungCap nhaCungCap, String tenSanPham, String maVach,
                   double giaNhap, double giaBan, int soLuongTon, int nguongTonToiThieu, String donViTinh, boolean trangThai) {
        this.maSanPham = maSanPham;
        this.nhaCungCap = nhaCungCap != null ? nhaCungCap : new NhaCungCap();
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

    public NhaCungCap getNhaCungCap() {
        return nhaCungCap;
    }

    public void setNhaCungCap(NhaCungCap nhaCungCap) {
        this.nhaCungCap = nhaCungCap;
    }

    public String getMaNhaCungCap() {
        return nhaCungCap != null ? nhaCungCap.getMaNhaCungCap() : null;
    }

    public void setMaNhaCungCap(String maNhaCungCap) {
        if (this.nhaCungCap == null) {
            this.nhaCungCap = new NhaCungCap();
        }
        this.nhaCungCap.setMaNhaCungCap(maNhaCungCap);
    }

    public String getTenNhaCungCap() {
        return nhaCungCap != null ? nhaCungCap.getTenNhaCungCap() : null;
    }

    public void setTenNhaCungCap(String tenNhaCungCap) {
        if (this.nhaCungCap == null) {
            this.nhaCungCap = new NhaCungCap();
        }
        this.nhaCungCap.setTenNhaCungCap(tenNhaCungCap);
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
                ", nhaCungCap=" + getMaNhaCungCap() +
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
