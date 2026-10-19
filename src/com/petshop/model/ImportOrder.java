package com.petshop.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Model đại diện cho một đơn hàng nhập từ Nhà cung cấp (bảng DonHang).
 * Tuân thủ quy tắc: tên lớp là danh từ, các trường khớp CSDL và tài liệu OOAD 2.15.
 */
public class ImportOrder {

    public static final String TRANG_THAI_CHO_NHAN = "Chờ nhận hàng";
    public static final String TRANG_THAI_HOAN_TAT = "Hoàn tất";
    public static final String TRANG_THAI_DA_HUY = "Đã hủy";

    private String maDonHang;          // DH-YYYYMMDD-NNNNNN
    private String maNhaCungCap;       // FK NhaCungCap
    private String tenNhaCungCap;      // Thuộc tính mở rộng để hiển thị UI
    private String soDienThoaiNhaCungCap;
    private String diaChiNhaCungCap;
    private String maNhanVien;         // FK NhanVien
    private String tenNhanVien;        // Thuộc tính mở rộng để hiển thị UI
    private LocalDate ngayLap;         // Ngày lập đơn nhập
    private double tongTien;           // Tổng tiền đơn hàng nhập
    private String trangThai;          // 'Chờ nhận hàng', 'Hoàn tất', 'Đã hủy'
    private List<ImportOrderItem> items = new ArrayList<>();

    public ImportOrder() {
        this.ngayLap = LocalDate.now();
        this.trangThai = TRANG_THAI_CHO_NHAN;
        this.tongTien = 0;
    }

    public ImportOrder(String maDonHang, String maNhaCungCap, String maNhanVien, LocalDate ngayLap, double tongTien, String trangThai) {
        this.maDonHang = maDonHang;
        this.maNhaCungCap = maNhaCungCap;
        this.maNhanVien = maNhanVien;
        this.ngayLap = ngayLap != null ? ngayLap : LocalDate.now();
        this.tongTien = tongTien;
        this.trangThai = trangThai != null ? trangThai : TRANG_THAI_CHO_NHAN;
    }

    public String getMaDonHang() {
        return maDonHang;
    }

    public void setMaDonHang(String maDonHang) {
        this.maDonHang = maDonHang;
    }

    public String getMaNhaCungCap() {
        return maNhaCungCap;
    }

    public void setMaNhaCungCap(String maNhaCungCap) {
        this.maNhaCungCap = maNhaCungCap;
    }

    public String getTenNhaCungCap() {
        return tenNhaCungCap;
    }

    public void setTenNhaCungCap(String tenNhaCungCap) {
        this.tenNhaCungCap = tenNhaCungCap;
    }

    public String getSoDienThoaiNhaCungCap() {
        return soDienThoaiNhaCungCap;
    }

    public void setSoDienThoaiNhaCungCap(String soDienThoaiNhaCungCap) {
        this.soDienThoaiNhaCungCap = soDienThoaiNhaCungCap;
    }

    public String getDiaChiNhaCungCap() {
        return diaChiNhaCungCap;
    }

    public void setDiaChiNhaCungCap(String diaChiNhaCungCap) {
        this.diaChiNhaCungCap = diaChiNhaCungCap;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public String getTenNhanVien() {
        return tenNhanVien;
    }

    public void setTenNhanVien(String tenNhanVien) {
        this.tenNhanVien = tenNhanVien;
    }

    public LocalDate getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(LocalDate ngayLap) {
        this.ngayLap = ngayLap;
    }

    public double getTongTien() {
        return tongTien;
    }

    public void setTongTien(double tongTien) {
        this.tongTien = tongTien;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public List<ImportOrderItem> getItems() {
        return items;
    }

    public void setItems(List<ImportOrderItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        tinhTongTien();
    }

    public void addItem(ImportOrderItem item) {
        if (item != null) {
            this.items.add(item);
            tinhTongTien();
        }
    }

    public void removeItem(ImportOrderItem item) {
        if (item != null) {
            this.items.remove(item);
            tinhTongTien();
        }
    }

    /**
     * Tự động tính lại tổng tiền của đơn nhập từ danh sách mặt hàng.
     */
    public void tinhTongTien() {
        double sum = 0;
        if (items != null) {
            for (ImportOrderItem item : items) {
                sum += item.getThanhTien();
            }
        }
        this.tongTien = sum;
    }

    public boolean isChoNhanHang() {
        return TRANG_THAI_CHO_NHAN.equalsIgnoreCase(trangThai);
    }

    public boolean isHoanTat() {
        return TRANG_THAI_HOAN_TAT.equalsIgnoreCase(trangThai);
    }

    public boolean isDaHuy() {
        return TRANG_THAI_DA_HUY.equalsIgnoreCase(trangThai);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImportOrder that = (ImportOrder) o;
        return Objects.equals(maDonHang, that.maDonHang);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maDonHang);
    }

    @Override
    public String toString() {
        return "ImportOrder{" +
                "maDonHang='" + maDonHang + '\'' +
                ", maNhaCungCap='" + maNhaCungCap + '\'' +
                ", tenNhaCungCap='" + tenNhaCungCap + '\'' +
                ", ngayLap=" + ngayLap +
                ", tongTien=" + tongTien +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}

