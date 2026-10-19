package com.petshop.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Model đại diện cho một hóa đơn bán hàng (bảng HoaDon).
 * Tuân thủ tiêu chuẩn: tên lớp là danh từ, các trường khớp CSDL và tài liệu OOAD 2.18.
 */
public class Invoice {

    /** Trạng thái thanh toán — giá trị DB khớp CHECK của cột trangThaiThanhToan. */
    public enum Status {
        PENDING("Chưa thanh toán"),
        PAID("Đã thanh toán"),
        CANCELLED("Đã hủy");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static Status fromDisplayName(String value) {
            if (value == null) return PENDING;
            String normalized = value.trim();
            for (Status s : values()) {
                if (s.displayName.equalsIgnoreCase(normalized) || s.name().equalsIgnoreCase(normalized)) {
                    return s;
                }
            }
            return PENDING;
        }
    }

    private String maHoaDon;          // HD-YYYYMMDD-NNNNNN
    private String maLichHen;         // Nullable
    private String maNhanVien;
    private String maKhachHang;       // Nullable (khách vãng lai)
    private LocalDate ngayLap;
    private String hinhThucThanhToan; // Tiền mặt / Chuyển khoản / POS
    private Status status = Status.PENDING;
    private String maKhuyenMai;       // Nullable
    private double tienGiam;
    private Double tongTienDb;        // Lưu tổng tiền từ DB nếu có
    private List<InvoiceItem> items = new ArrayList<>();

    // Các thuộc tính mở rộng hỗ trợ hiển thị UI nhanh
    private String tenKhachHang;
    private String soDienThoaiKhachHang;
    private String tenNhanVien;
    private String tenKhuyenMai;
    private double phanTramGiam;

    public Invoice() {
        this.ngayLap = LocalDate.now();
    }

    public Invoice(String maHoaDon, String maNhanVien, String maKhachHang, LocalDate ngayLap,
                   String hinhThucThanhToan, Status status, String maKhuyenMai, double tienGiam) {
        this.maHoaDon = maHoaDon;
        this.maNhanVien = maNhanVien;
        this.maKhachHang = maKhachHang;
        this.ngayLap = ngayLap != null ? ngayLap : LocalDate.now();
        this.hinhThucThanhToan = hinhThucThanhToan;
        this.status = status != null ? status : Status.PENDING;
        this.maKhuyenMai = maKhuyenMai;
        this.tienGiam = tienGiam;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public String getMaLichHen() {
        return maLichHen;
    }

    public void setMaLichHen(String maLichHen) {
        this.maLichHen = maLichHen;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public LocalDate getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(LocalDate ngayLap) {
        this.ngayLap = ngayLap;
    }

    public String getHinhThucThanhToan() {
        return hinhThucThanhToan;
    }

    public void setHinhThucThanhToan(String hinhThucThanhToan) {
        this.hinhThucThanhToan = hinhThucThanhToan;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMaKhuyenMai() {
        return maKhuyenMai;
    }

    public void setMaKhuyenMai(String maKhuyenMai) {
        this.maKhuyenMai = maKhuyenMai;
    }

    public double getTienGiam() {
        return tienGiam;
    }

    public void setTienGiam(double tienGiam) {
        this.tienGiam = tienGiam;
    }

    public List<InvoiceItem> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItem> items) {
        this.items = items;
    }

    public String getTenKhachHang() {
        return tenKhachHang != null ? tenKhachHang : (maKhachHang != null ? maKhachHang : "Khách vãng lai");
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getSoDienThoaiKhachHang() {
        return soDienThoaiKhachHang;
    }

    public void setSoDienThoaiKhachHang(String soDienThoaiKhachHang) {
        this.soDienThoaiKhachHang = soDienThoaiKhachHang;
    }

    public String getTenNhanVien() {
        return tenNhanVien != null ? tenNhanVien : maNhanVien;
    }

    public void setTenNhanVien(String tenNhanVien) {
        this.tenNhanVien = tenNhanVien;
    }

    public String getTenKhuyenMai() {
        return tenKhuyenMai;
    }

    public void setTenKhuyenMai(String tenKhuyenMai) {
        this.tenKhuyenMai = tenKhuyenMai;
    }

    public double getPhanTramGiam() {
        return phanTramGiam;
    }

    public void setPhanTramGiam(double phanTramGiam) {
        this.phanTramGiam = phanTramGiam;
    }

    public void setTongTien(double tongTien) {
        this.tongTienDb = tongTien;
    }

    /** Tổng tiền hàng trước giảm giá. */
    public double getTongTienHang() {
        return items.stream().mapToDouble(InvoiceItem::getThanhTien).sum();
    }

    /** Tổng tiền phải thanh toán = tổng hàng - tiền giảm (khớp cột tongTien trong DB). */
    public double getTongTien() {
        if (tongTienDb != null && items.isEmpty()) {
            return tongTienDb;
        }
        return Math.max(0, getTongTienHang() - tienGiam);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invoice invoice = (Invoice) o;
        return Objects.equals(maHoaDon, invoice.maHoaDon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maHoaDon);
    }

    @Override
    public String toString() {
        return "Invoice{ma='" + maHoaDon + "', status=" + status + ", tongTien=" + getTongTien() + "}";
    }
}
