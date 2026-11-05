package com.petshop.entity;

import java.util.Objects;

/**
 * Thực thể Chi tiết đơn nhập hàng.
 * Khớp với bảng ChiTietDonNhap trong CSDL SQL Server:
 * - maDonHang VARCHAR(20) NOT NULL
 * - maSanPham VARCHAR(20) NOT NULL
 * - soLuong INT NOT NULL CHECK (soLuong > 0)
 * - soLuongThucNhan INT NULL CHECK (soLuongThucNhan >= 0)
 * - donGia DECIMAL(18,2) NOT NULL CHECK (donGia >= 0)
 * - PRIMARY KEY (maDonHang, maSanPham)
 *
 * Tuân thủ quy chuẩn kỹ thuật:
 * - Dùng Object Reference: SanPham sanPham
 * - Tiền tệ dùng kiểu số thực double (không dùng BigDecimal)
 * - Thuộc tính dẫn xuất (thanhTien) được tính bằng hàm tinhThanhTien()
 */
public class ChiTietDonNhap {

    private String maDonHang;
    private SanPham sanPham;
    private int soLuong;
    private Integer soLuongThucNhan;
    private double donGia;

    public ChiTietDonNhap() {
        this.sanPham = new SanPham();
        this.soLuong = 1;
        this.soLuongThucNhan = 1;
        this.donGia = 0.0;
    }

    public ChiTietDonNhap(String maDonHang, SanPham sanPham, int soLuong, double donGia) {
        this.maDonHang = maDonHang;
        this.sanPham = sanPham != null ? sanPham : new SanPham();
        this.soLuong = soLuong;
        this.soLuongThucNhan = soLuong;
        this.donGia = Math.max(0.0, donGia);
    }

    public ChiTietDonNhap(String maDonHang, String maSanPham, int soLuong, double donGia) {
        this.maDonHang = maDonHang;
        this.sanPham = new SanPham();
        this.sanPham.setMaSanPham(maSanPham);
        this.soLuong = soLuong;
        this.soLuongThucNhan = soLuong;
        this.donGia = Math.max(0.0, donGia);
    }

    public ChiTietDonNhap(String maDonHang, String maSanPham, String tenSanPham, String donViTinh, int soLuong, double donGia) {
        this(maDonHang, maSanPham, soLuong, donGia);
        this.sanPham.setTenSanPham(tenSanPham);
        this.sanPham.setDonViTinh(donViTinh);
    }

    public ChiTietDonNhap(String maDonHang, String maSanPham, String tenSanPham, String donViTinh, int soLuong, Integer soLuongThucNhan, double donGia) {
        this(maDonHang, maSanPham, tenSanPham, donViTinh, soLuong, donGia);
        this.soLuongThucNhan = soLuongThucNhan != null ? soLuongThucNhan : soLuong;
    }

    public String getMaDonHang() {
        return maDonHang;
    }

    public void setMaDonHang(String maDonHang) {
        this.maDonHang = maDonHang;
    }

    public String getMaDonNhap() {
        return maDonHang;
    }

    public void setMaDonNhap(String maDonNhap) {
        this.maDonHang = maDonNhap;
    }

    public SanPham getSanPham() {
        return sanPham;
    }

    public void setSanPham(SanPham sanPham) {
        this.sanPham = sanPham != null ? sanPham : new SanPham();
    }

    public String getMaSanPham() {
        return sanPham != null ? sanPham.getMaSanPham() : null;
    }

    public void setMaSanPham(String maSanPham) {
        if (this.sanPham == null) {
            this.sanPham = new SanPham();
        }
        this.sanPham.setMaSanPham(maSanPham);
    }

    public String getTenSanPham() {
        return sanPham != null ? sanPham.getTenSanPham() : null;
    }

    public void setTenSanPham(String tenSanPham) {
        if (this.sanPham == null) {
            this.sanPham = new SanPham();
        }
        this.sanPham.setTenSanPham(tenSanPham);
    }

    public String getDonViTinh() {
        return sanPham != null ? sanPham.getDonViTinh() : null;
    }

    public void setDonViTinh(String donViTinh) {
        if (this.sanPham == null) {
            this.sanPham = new SanPham();
        }
        this.sanPham.setDonViTinh(donViTinh);
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
        if (this.soLuongThucNhan == null) {
            this.soLuongThucNhan = soLuong;
        }
    }

    public Integer getSoLuongThucNhan() {
        return soLuongThucNhan != null ? soLuongThucNhan : soLuong;
    }

    public void setSoLuongThucNhan(Integer soLuongThucNhan) {
        this.soLuongThucNhan = soLuongThucNhan;
    }

    public double getDonGia() {
        return donGia;
    }

    public void setDonGia(double donGia) {
        this.donGia = Math.max(0.0, donGia);
    }

    /**
     * Thành tiền theo số lượng đặt = soLuong * donGia (thuộc tính dẫn xuất).
     */
    public double tinhThanhTien() {
        if (soLuong <= 0 || donGia <= 0.0) {
            return 0.0;
        }
        return soLuong * donGia;
    }

    /**
     * Thành tiền thực nhận = soLuongThucNhan * donGia (thuộc tính dẫn xuất).
     */
    public double tinhThanhTienThucNhan() {
        int thucNhan = getSoLuongThucNhan();
        if (thucNhan <= 0 || donGia <= 0.0) {
            return 0.0;
        }
        return thucNhan * donGia;
    }

    /**
     * Getter ủy quyền tương thích TableView PropertyValueFactory("thanhTien").
     */
    public double getThanhTien() {
        return tinhThanhTien();
    }

    /**
     * Getter ủy quyền tương thích TableView PropertyValueFactory("thanhTienThucNhan").
     */
    public double getThanhTienThucNhan() {
        return tinhThanhTienThucNhan();
    }

    @Override
    public String toString() {
        return "ChiTietDonNhap{" +
                "maDonHang='" + maDonHang + '\'' +
                ", maSanPham='" + getMaSanPham() + '\'' +
                ", tenSanPham='" + getTenSanPham() + '\'' +
                ", soLuong=" + soLuong +
                ", soLuongThucNhan=" + soLuongThucNhan +
                ", donGia=" + donGia +
                ", thanhTien=" + tinhThanhTien() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChiTietDonNhap that = (ChiTietDonNhap) o;
        return Objects.equals(maDonHang, that.maDonHang) &&
                Objects.equals(getMaSanPham(), that.getMaSanPham());
    }

    @Override
    public int hashCode() {
        return Objects.hash(maDonHang, getMaSanPham());
    }
}
