package com.petshop.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Thực thể Đơn nhập hàng.
 * Khớp với bảng DonNhapHang trong CSDL SQL Server:
 * - maDonHang VARCHAR(20) PRIMARY KEY (PN-YYYYMMDD-XXXXXX hoặc DH-YYYYMMDD-XXXXXX)
 * - maNhaCungCap VARCHAR(20) NOT NULL
 * - maNhanVien VARCHAR(20) NOT NULL
 * - ngayLap DATE NOT NULL, gioLap TIME NOT NULL
 * - tongTien DECIMAL(18,2) NOT NULL DEFAULT 0
 * - trangThai NVARCHAR(30) NOT NULL (Chờ nhận hàng, Hoàn tất, Đã hủy)
 */
public class DonNhapHang {

    public static final String TRANG_THAI_CHO_NHAN = "Chờ nhận hàng";
    public static final String TRANG_THAI_HOAN_TAT = "Hoàn tất";
    public static final String TRANG_THAI_DA_HUY = "Đã hủy";

    private String maDonHang;
    private String maNhaCungCap;
    private String maNhanVien;
    private LocalDate ngayLap;
    private LocalTime gioLap;
    private BigDecimal tongTien;
    private String trangThai;

    // Các trường hiển thị bổ trợ (JOIN view)
    private String tenNhaCungCap;
    private String tenNhanVien;
    private List<ChiTietDonNhap> danhSachChiTiet = new ArrayList<>();

    public DonNhapHang() {
        this.ngayLap = LocalDate.now();
        this.gioLap = LocalTime.now().withNano(0);
        this.tongTien = BigDecimal.ZERO;
        this.trangThai = TRANG_THAI_CHO_NHAN;
    }

    public DonNhapHang(String maDonHang, String maNhaCungCap, String maNhanVien, LocalDate ngayLap, BigDecimal tongTien, String trangThai) {
        this.maDonHang = maDonHang;
        this.maNhaCungCap = maNhaCungCap;
        this.maNhanVien = maNhanVien;
        this.ngayLap = ngayLap != null ? ngayLap : LocalDate.now();
        this.gioLap = LocalTime.now().withNano(0);
        this.tongTien = tongTien != null ? tongTien : BigDecimal.ZERO;
        this.trangThai = trangThai != null ? trangThai : TRANG_THAI_CHO_NHAN;
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

    public String getMaNhaCungCap() {
        return maNhaCungCap;
    }

    public void setMaNhaCungCap(String maNhaCungCap) {
        this.maNhaCungCap = maNhaCungCap;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public LocalDate getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(LocalDate ngayLap) {
        this.ngayLap = ngayLap;
    }

    public LocalTime getGioLap() {
        return gioLap;
    }

    public void setGioLap(LocalTime gioLap) {
        this.gioLap = gioLap;
    }

    public BigDecimal getTongTien() {
        return tongTien;
    }

    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien != null ? tongTien : BigDecimal.ZERO;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getTenNhaCungCap() {
        return tenNhaCungCap;
    }

    public void setTenNhaCungCap(String tenNhaCungCap) {
        this.tenNhaCungCap = tenNhaCungCap;
    }

    public String getTenNhanVien() {
        return tenNhanVien;
    }

    public void setTenNhanVien(String tenNhanVien) {
        this.tenNhanVien = tenNhanVien;
    }

    public List<ChiTietDonNhap> getDanhSachChiTiet() {
        return danhSachChiTiet;
    }

    public void setDanhSachChiTiet(List<ChiTietDonNhap> danhSachChiTiet) {
        this.danhSachChiTiet = danhSachChiTiet != null ? danhSachChiTiet : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "DonNhapHang{" +
                "maDonHang='" + maDonHang + '\'' +
                ", maNhaCungCap='" + maNhaCungCap + '\'' +
                ", maNhanVien='" + maNhanVien + '\'' +
                ", ngayLap=" + ngayLap +
                ", gioLap=" + gioLap +
                ", tongTien=" + tongTien +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}

