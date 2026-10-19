package com.petshop.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Model đại diện cho một hóa đơn bán hàng (bảng HoaDon).
 */
public class Invoice {

    /** Trạng thái thanh toán — giá trị DB khớp CHECK của cột trangThaiThanhToan. */
    public enum Status {
        PENDING("Chờ thanh toán"),
        PAID("Đã thanh toán"),
        CANCELLED("Đã hủy");

        private final String displayName;

        Status(String displayName) { this.displayName = displayName; }

        public String getDisplayName() { return displayName; }

        public static Status fromDisplayName(String value) {
            for (Status s : values()) {
                if (s.displayName.equals(value)
                        || (s == PENDING && "Chưa thanh toán".equals(value))) return s;
            }
            throw new IllegalArgumentException("Trạng thái hóa đơn không hợp lệ: " + value);
        }
    }

    private String maHoaDon;          // HD-YYYYMMDD-NNNNNN
    private String maLichHen;         // nullable
    private String maNhanVien;
    private String maNhanVienThu;     // người thực hiện thu tiền
    private String maKhachHang;       // nullable (khách vãng lai)
    private String maThuCung;         // bắt buộc nếu hóa đơn có dịch vụ
    private LocalDate ngayLap;
    private String hinhThucThanhToan; // Tiền mặt / Chuyển khoản / POS
    private Status status = Status.PENDING;
    private String maKhuyenMai;       // nullable
    private double giamGia;
    private double giamTheoKhuyenMai;
    private double thue;
    private String ghiChu;
    private LocalDateTime ngayHuy;
    private String nguoiHuy;
    private String lyDoHuy;
    private List<InvoiceItem> items = new ArrayList<>();

    public Invoice() {}

    public String getMaHoaDon()                 { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon)    { this.maHoaDon = maHoaDon; }

    public String getMaLichHen()                { return maLichHen; }
    public void setMaLichHen(String maLichHen)  { this.maLichHen = maLichHen; }

    public String getMaNhanVien()                   { return maNhanVien; }
    public void setMaNhanVien(String maNhanVien)    { this.maNhanVien = maNhanVien; }

    public String getMaNhanVienThu() { return maNhanVienThu; }
    public void setMaNhanVienThu(String maNhanVienThu) { this.maNhanVienThu = maNhanVienThu; }

    public String getMaKhachHang()                      { return maKhachHang; }
    public void setMaKhachHang(String maKhachHang)      { this.maKhachHang = maKhachHang; }

    public String getMaThuCung() { return maThuCung; }
    public void setMaThuCung(String maThuCung) { this.maThuCung = maThuCung; }

    public LocalDate getNgayLap()                   { return ngayLap; }
    public void setNgayLap(LocalDate ngayLap)       { this.ngayLap = ngayLap; }

    public String getHinhThucThanhToan()                            { return hinhThucThanhToan; }
    public void setHinhThucThanhToan(String hinhThucThanhToan)      { this.hinhThucThanhToan = hinhThucThanhToan; }

    public Status getStatus()               { return status; }
    public void setStatus(Status status)    { this.status = status; }

    public String getMaKhuyenMai()                  { return maKhuyenMai; }
    public void setMaKhuyenMai(String maKhuyenMai)  { this.maKhuyenMai = maKhuyenMai; }

    public double getTienGiam() { return giamGia; }
    public void setTienGiam(double tienGiam) { setGiamGia(tienGiam); }

    public double getGiamGia() { return giamGia; }
    public void setGiamGia(double giamGia) {
        if (giamGia < 0) throw new IllegalArgumentException("Giảm giá nhập tay không được âm.");
        this.giamGia = giamGia;
    }

    public double getGiamTheoKhuyenMai() { return giamTheoKhuyenMai; }
    public void setGiamTheoKhuyenMai(double giamTheoKhuyenMai) {
        if (giamTheoKhuyenMai < 0) throw new IllegalArgumentException("Giảm theo khuyến mãi không được âm.");
        this.giamTheoKhuyenMai = giamTheoKhuyenMai;
    }

    public double getThue() { return thue; }
    public void setThue(double thue) {
        if (thue < 0) throw new IllegalArgumentException("Thuế không được âm.");
        this.thue = thue;
    }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public LocalDateTime getNgayHuy() { return ngayHuy; }
    public void setNgayHuy(LocalDateTime ngayHuy) { this.ngayHuy = ngayHuy; }

    public String getNguoiHuy() { return nguoiHuy; }
    public void setNguoiHuy(String nguoiHuy) { this.nguoiHuy = nguoiHuy; }

    public String getLyDoHuy() { return lyDoHuy; }
    public void setLyDoHuy(String lyDoHuy) { this.lyDoHuy = lyDoHuy; }

    public List<InvoiceItem> getItems()             { return items; }
    public void setItems(List<InvoiceItem> items)   { this.items = items; }

    public void themDong(InvoiceItem item) {
        if (item == null) throw new IllegalArgumentException("Dòng hóa đơn không hợp lệ.");
        if (status != Status.PENDING) throw new IllegalStateException("Chỉ sửa được hóa đơn chờ thanh toán.");
        items.add(item);
    }

    public void xoaDong(InvoiceItem item) {
        if (item == null || !items.contains(item)) return;
        if (item.isKhoa()) throw new IllegalStateException("Không thể xóa dịch vụ gắn với lịch hẹn.");
        if (status != Status.PENDING) throw new IllegalStateException("Chỉ sửa được hóa đơn chờ thanh toán.");
        items.remove(item);
    }

    /** Tổng tiền các dòng trước giảm giá và thuế. */
    public double tinhTamTinh() {
        return items.stream().mapToDouble(InvoiceItem::thanhTien).sum();
    }

    public double getTongTienHang() {
        return tinhTamTinh();
    }

    public boolean coSanPham() {
        return items.stream().anyMatch(item -> item.getLoaiDong() == InvoiceItem.LineType.SAN_PHAM);
    }

    /** Tổng thanh toán = tạm tính - giảm tay - giảm khuyến mãi + thuế. */
    public double tinhTongThanhToan() {
        return Math.max(0, tinhTamTinh() - giamGia - giamTheoKhuyenMai + thue);
    }

    public double getTongTien() {
        return tinhTongThanhToan();
    }

    public void luuChoThanhToan() {
        if (status != Status.PENDING) throw new IllegalStateException("Chỉ hóa đơn chờ thanh toán mới được lưu chờ.");
        validateDraft();
        status = Status.PENDING;
        hinhThucThanhToan = null;
        maNhanVienThu = null;
    }

    public void thanhToan(User nguoiThu, String hinhThucThanhToan, double tienKhachDua) {
        if (status != Status.PENDING) throw new IllegalStateException("Hóa đơn không ở trạng thái chờ thanh toán.");
        validateDraft();
        if (!coTheThuTien(nguoiThu)) {
            throw new IllegalStateException("Vai trò hiện tại không được thu tiền cho hóa đơn có sản phẩm.");
        }
        if (hinhThucThanhToan == null || hinhThucThanhToan.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn phương thức thanh toán.");
        }
        if ("Tiền mặt".equals(hinhThucThanhToan) && tienKhachDua < tinhTongThanhToan()) {
            throw new IllegalArgumentException("Tiền khách đưa chưa đủ thanh toán.");
        }
        this.maNhanVienThu = nguoiThu == null ? null : nguoiThu.getMaNhanVien();
        this.hinhThucThanhToan = hinhThucThanhToan;
        this.status = Status.PAID;
    }

    public void huy(User nguoiHuy, String lyDo) {
        if (status == Status.CANCELLED) throw new IllegalStateException("Hóa đơn đã hủy trước đó.");
        if (status != Status.PENDING && status != Status.PAID) {
            throw new IllegalStateException("Trạng thái hóa đơn không cho phép hủy.");
        }
        if (nguoiHuy == null || nguoiHuy.getMaNhanVien() == null) {
            throw new IllegalArgumentException("Không xác định được nhân viên hủy hóa đơn.");
        }
        if (lyDo == null || lyDo.isBlank()) throw new IllegalArgumentException("Vui lòng nhập lý do hủy.");
        this.nguoiHuy = nguoiHuy.getMaNhanVien();
        this.ngayHuy = LocalDateTime.now();
        this.lyDoHuy = lyDo.trim();
        this.status = Status.CANCELLED;
    }

    private void validateDraft() {
        if (maKhachHang == null || maKhachHang.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn khách hàng.");
        }
        if (items.isEmpty()) throw new IllegalArgumentException("Hóa đơn cần ít nhất một dòng chi tiết.");
        if (items.stream().anyMatch(InvoiceItem::isService) && (maThuCung == null || maThuCung.isBlank())) {
            throw new IllegalArgumentException("Hóa đơn có dịch vụ cần chọn thú cưng.");
        }
    }

    private boolean coTheThuTien(User nguoiThu) {
        User.Role role = nguoiThu == null ? User.Role.ADMIN : nguoiThu.getRole();
        if (role == User.Role.ADMIN || role == User.Role.BAN_HANG) return true;
        return !coSanPham() && (role == User.Role.CHAM_SOC || role == User.Role.QUAN_LY);
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
