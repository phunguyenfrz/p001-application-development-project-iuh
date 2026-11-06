package com.petshop.entity;

import java.util.Objects;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Thực thể Đơn nhập hàng.
 * Khớp với bảng DonNhapHang trong CSDL SQL Server:
 * - maDonHang VARCHAR(20) PRIMARY KEY (DH-YYYYMMDD-XXXXXX)
 * - maNhaCungCap VARCHAR(20) NOT NULL
 * - maNhanVien VARCHAR(20) NOT NULL
 * - ngayLap DATE NOT NULL, gioLap TIME NOT NULL
 * - tongTien DECIMAL(18,2) NOT NULL DEFAULT 0
 * - tongTienThucNhan DECIMAL(18,2) NULL
 * - trangThai NVARCHAR(30) NOT NULL (Chờ nhận hàng, Hoàn tất, Đã hủy)
 * - maNhanVienNhan VARCHAR(20) NULL, ngayGioNhan DATETIME2 NULL
 * - maNhanVienHuy VARCHAR(20) NULL, lyDoHuy NVARCHAR(255) NULL
 *
 * Tuân thủ quy chuẩn kỹ thuật:
 * - Object Reference: nhaCungCap, nhanVien, nhanVienNhan, nhanVienHuy
 * - Tiền tệ dùng kiểu số thực double
 * - Thuộc tính dẫn xuất tongTien không lưu trữ thành biến thành viên,
 *   tự tính toán qua phương thức tinhTongTien() từ danhSachChiTiet
 */
public class DonNhapHang {

    public static final String TRANG_THAI_CHO_NHAN = "Chờ nhận hàng";
    public static final String TRANG_THAI_HOAN_TAT = "Hoàn tất";
    public static final String TRANG_THAI_DA_HUY = "Đã hủy";

    private String maDonHang;
    private NhaCungCap nhaCungCap;
    private NhanVien nhanVien;
    private LocalDate ngayLap;
    private LocalTime gioLap;
    private String trangThai;

    // Truy vết người nhận và thời gian nhận (Object Reference)
    private NhanVien nhanVienNhan;
    private LocalDateTime ngayGioNhan;

    // Truy vết người hủy và lý do hủy (Object Reference)
    private NhanVien nhanVienHuy;
    private String lyDoHuy;

    // Danh sách dòng chi tiết và chuỗi tên mặt hàng hiển thị
    private double tongTien;
    private double tongTienThucNhan;
    private String tenHang;
    private List<ChiTietDonNhap> danhSachChiTiet = new ArrayList<>();

    public DonNhapHang() {
        this.nhaCungCap = new NhaCungCap();
        this.nhanVien = new NhanVien();
        this.ngayLap = LocalDate.now();
        this.gioLap = LocalTime.now().withNano(0);
        this.trangThai = TRANG_THAI_CHO_NHAN;
    }

    public DonNhapHang(String maDonHang, NhaCungCap nhaCungCap, NhanVien nhanVien, LocalDate ngayLap, String trangThai) {
        this.maDonHang = maDonHang;
        this.nhaCungCap = nhaCungCap != null ? nhaCungCap : new NhaCungCap();
        this.nhanVien = nhanVien != null ? nhanVien : new NhanVien();
        this.ngayLap = ngayLap != null ? ngayLap : LocalDate.now();
        this.gioLap = LocalTime.now().withNano(0);
        this.trangThai = trangThai != null ? trangThai : TRANG_THAI_CHO_NHAN;
    }

    public DonNhapHang(String maDonHang, String maNhaCungCap, String maNhanVien, LocalDate ngayLap, String trangThai) {
        this.maDonHang = maDonHang;
        this.nhaCungCap = new NhaCungCap(maNhaCungCap, null);
        this.nhanVien = new NhanVien(maNhanVien, null, null, null, true, "Đang làm");
        this.ngayLap = ngayLap != null ? ngayLap : LocalDate.now();
        this.gioLap = LocalTime.now().withNano(0);
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

    // --- Object Reference: NhaCungCap ---
    public NhaCungCap getNhaCungCap() {
        return nhaCungCap;
    }

    public void setNhaCungCap(NhaCungCap nhaCungCap) {
        this.nhaCungCap = nhaCungCap != null ? nhaCungCap : new NhaCungCap();
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

    // --- Object Reference: NhanVien ---
    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien != null ? nhanVien : new NhanVien();
    }

    public String getMaNhanVien() {
        return nhanVien != null ? nhanVien.getMaNhanVien() : null;
    }

    public void setMaNhanVien(String maNhanVien) {
        if (this.nhanVien == null) {
            this.nhanVien = new NhanVien();
        }
        this.nhanVien.setMaNhanVien(maNhanVien);
    }

    public String getTenNhanVien() {
        return nhanVien != null ? nhanVien.getHoTen() : null;
    }

    public void setTenNhanVien(String tenNhanVien) {
        if (this.nhanVien == null) {
            this.nhanVien = new NhanVien();
        }
        this.nhanVien.setHoTen(tenNhanVien);
    }

    // --- Object Reference: NhanVienNhan ---
    public NhanVien getNhanVienNhan() {
        return nhanVienNhan;
    }

    public void setNhanVienNhan(NhanVien nhanVienNhan) {
        this.nhanVienNhan = nhanVienNhan;
    }

    public String getMaNhanVienNhan() {
        return nhanVienNhan != null ? nhanVienNhan.getMaNhanVien() : null;
    }

    public void setMaNhanVienNhan(String maNhanVienNhan) {
        if (maNhanVienNhan == null || maNhanVienNhan.isBlank()) {
            this.nhanVienNhan = null;
        } else {
            if (this.nhanVienNhan == null) {
                this.nhanVienNhan = new NhanVien();
            }
            this.nhanVienNhan.setMaNhanVien(maNhanVienNhan);
        }
    }

    public String getTenNhanVienNhan() {
        return nhanVienNhan != null ? nhanVienNhan.getHoTen() : null;
    }

    public void setTenNhanVienNhan(String tenNhanVienNhan) {
        if (this.nhanVienNhan != null) {
            this.nhanVienNhan.setHoTen(tenNhanVienNhan);
        }
    }

    // --- Object Reference: NhanVienHuy ---
    public NhanVien getNhanVienHuy() {
        return nhanVienHuy;
    }

    public void setNhanVienHuy(NhanVien nhanVienHuy) {
        this.nhanVienHuy = nhanVienHuy;
    }

    public String getMaNhanVienHuy() {
        return nhanVienHuy != null ? nhanVienHuy.getMaNhanVien() : null;
    }

    public void setMaNhanVienHuy(String maNhanVienHuy) {
        if (maNhanVienHuy == null || maNhanVienHuy.isBlank()) {
            this.nhanVienHuy = null;
        } else {
            if (this.nhanVienHuy == null) {
                this.nhanVienHuy = new NhanVien();
            }
            this.nhanVienHuy.setMaNhanVien(maNhanVienHuy);
        }
    }

    public String getTenNhanVienHuy() {
        return nhanVienHuy != null ? nhanVienHuy.getHoTen() : null;
    }

    public void setTenNhanVienHuy(String tenNhanVienHuy) {
        if (this.nhanVienHuy != null) {
            this.nhanVienHuy.setHoTen(tenNhanVienHuy);
        }
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

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getNgayGioNhan() {
        return ngayGioNhan;
    }

    public void setNgayGioNhan(LocalDateTime ngayGioNhan) {
        this.ngayGioNhan = ngayGioNhan;
    }

    public String getLyDoHuy() {
        return lyDoHuy;
    }

    public void setLyDoHuy(String lyDoHuy) {
        this.lyDoHuy = lyDoHuy;
    }

    public List<ChiTietDonNhap> getDanhSachChiTiet() {
        return danhSachChiTiet;
    }

    public void setDanhSachChiTiet(List<ChiTietDonNhap> danhSachChiTiet) {
        this.danhSachChiTiet = danhSachChiTiet != null ? danhSachChiTiet : new ArrayList<>();
    }

    public String getTenHang() {
        return tenHang;
    }

    public void setTenHang(String tenHang) {
        this.tenHang = tenHang;
    }

    /**
     * Phương thức tự tính toán tổng tiền đặt theo danh sách chi tiết (Điều 2.3).
     * Tuyệt đối không lưu biến thành viên tongTien.
     */
    public double tinhTongTien() {
        if (danhSachChiTiet != null && !danhSachChiTiet.isEmpty()) {
            double sum = 0.0;
            for (ChiTietDonNhap ct : danhSachChiTiet) {
                sum += ct.tinhThanhTien();
            }
            return sum;
        }
        return tongTien;
    }

    /**
     * Getter ủy quyền tương thích TableView PropertyValueFactory("tongTien").
     */
    public double getTongTien() {
        return tinhTongTien();
    }

    public void setTongTien(double tongTien) {
        this.tongTien = tongTien;
    }

    /**
     * Phương thức tự tính toán tổng tiền thực nhận theo danh sách chi tiết (Điều 2.3).
     */
    public double tinhTongTienThucNhan() {
        if (danhSachChiTiet != null && !danhSachChiTiet.isEmpty()) {
            double sum = 0.0;
            for (ChiTietDonNhap ct : danhSachChiTiet) {
                sum += ct.tinhThanhTienThucNhan();
            }
            if (sum > 0.0) return sum;
        }
        return tongTienThucNhan;
    }

    /**
     * Getter ủy quyền tương thích TableView PropertyValueFactory("tongTienThucNhan").
     */
    public double getTongTienThucNhan() {
        return tinhTongTienThucNhan();
    }

    public void setTongTienThucNhan(double tongTienThucNhan) {
        this.tongTienThucNhan = tongTienThucNhan;
    }

    @Override
    public String toString() {
        return "DonNhapHang{" +
                "maDonHang='" + maDonHang + '\'' +
                ", maNhaCungCap='" + getMaNhaCungCap() + '\'' +
                ", maNhanVien='" + getMaNhanVien() + '\'' +
                ", ngayLap=" + ngayLap +
                ", gioLap=" + gioLap +
                ", tongTien=" + tinhTongTien() +
                ", tongTienThucNhan=" + tinhTongTienThucNhan() +
                ", trangThai='" + trangThai + '\'' +
                ", maNhanVienNhan='" + getMaNhanVienNhan() + '\'' +
                ", ngayGioNhan=" + ngayGioNhan +
                ", maNhanVienHuy='" + getMaNhanVienHuy() + '\'' +
                ", lyDoHuy='" + lyDoHuy + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DonNhapHang that = (DonNhapHang) o;
        return Objects.equals(maDonHang, that.maDonHang);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maDonHang);
    }
}
