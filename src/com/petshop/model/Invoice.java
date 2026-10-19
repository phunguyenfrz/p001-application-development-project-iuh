package com.petshop.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Model đại diện cho một hóa đơn bán hàng (bảng HoaDon).
 */
public class Invoice {

    /** Trạng thái thanh toán — giá trị DB khớp CHECK của cột trangThaiThanhToan. */
    public enum Status {
        PENDING("Chưa thanh toán"),
        PAID("Đã thanh toán"),
        CANCELLED("Đã hủy");

        private final String displayName;

        Status(String displayName) { this.displayName = displayName; }

        public String getDisplayName() { return displayName; }

        public static Status fromDisplayName(String value) {
            for (Status s : values()) {
                if (s.displayName.equals(value)) return s;
            }
            throw new IllegalArgumentException("Trạng thái hóa đơn không hợp lệ: " + value);
        }
    }

    private String maHoaDon;          // HD-YYYYMMDD-NNNNNN
    private String maLichHen;         // nullable
    private String maNhanVien;
    private String maKhachHang;       // nullable (khách vãng lai)
    private LocalDate ngayLap;
    private String hinhThucThanhToan; // Tiền mặt / Chuyển khoản / POS
    private Status status = Status.PENDING;
    private String maKhuyenMai;       // nullable
    private double tienGiam;
    private List<InvoiceItem> items = new ArrayList<>();

    public Invoice() {}

    public String getMaHoaDon()                 { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon)    { this.maHoaDon = maHoaDon; }

    public String getMaLichHen()                { return maLichHen; }
    public void setMaLichHen(String maLichHen)  { this.maLichHen = maLichHen; }

    public String getMaNhanVien()                   { return maNhanVien; }
    public void setMaNhanVien(String maNhanVien)    { this.maNhanVien = maNhanVien; }

    public String getMaKhachHang()                      { return maKhachHang; }
    public void setMaKhachHang(String maKhachHang)      { this.maKhachHang = maKhachHang; }

    public LocalDate getNgayLap()                   { return ngayLap; }
    public void setNgayLap(LocalDate ngayLap)       { this.ngayLap = ngayLap; }

    public String getHinhThucThanhToan()                            { return hinhThucThanhToan; }
    public void setHinhThucThanhToan(String hinhThucThanhToan)      { this.hinhThucThanhToan = hinhThucThanhToan; }

    public Status getStatus()               { return status; }
    public void setStatus(Status status)    { this.status = status; }

    public String getMaKhuyenMai()                  { return maKhuyenMai; }
    public void setMaKhuyenMai(String maKhuyenMai)  { this.maKhuyenMai = maKhuyenMai; }

    public double getTienGiam()                 { return tienGiam; }
    public void setTienGiam(double tienGiam)    { this.tienGiam = tienGiam; }

    public List<InvoiceItem> getItems()             { return items; }
    public void setItems(List<InvoiceItem> items)   { this.items = items; }

    /** Tổng tiền hàng trước giảm giá. */
    public double getTongTienHang() {
        return items.stream().mapToDouble(InvoiceItem::getThanhTien).sum();
    }

    /** Tổng tiền phải thanh toán = tổng hàng - tiền giảm (khớp cột tongTien trong DB). */
    public double getTongTien() {
        return Math.max(0, getTongTienHang() - tienGiam);
    }

    @Override
    public String toString() {
        return "Invoice{ma='" + maHoaDon + "', status=" + status + ", tongTien=" + getTongTien() + "}";
    }
}
