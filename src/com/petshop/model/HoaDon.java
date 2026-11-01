package com.petshop.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Thực thể ánh xạ bảng HoaDon trong cơ sở dữ liệu.
 * Đại diện cho hóa đơn thanh toán bán lẻ sản phẩm và dịch vụ của cửa hàng Happy Tails.
 */
public class HoaDon {

    private String maHoaDon;
    private String maLichHen;
    private String maPhieuDichVu;
    private String maNhanVien;
    private String tenNhanVien;
    private String maKhachHang;
    private String tenKhachHang;
    private String soDienThoaiKhachHang;
    private LocalDate ngayLap;
    private LocalTime gioLap;
    private String hinhThucThanhToan; // "Tiền mặt", "Chuyển khoản", "POS"
    private String trangThaiThanhToan; // "Chưa thanh toán", "Đã thanh toán", "Đã hủy"
    private String maKhuyenMai;
    private String tenKhuyenMai;
    private BigDecimal tienGiam;
    private int diemTichLuySuDung = 0; // Số điểm dùng giảm trừ
    private int diemTichLuyCong = 0;   // Số điểm mới tích lũy được
    private BigDecimal tienGiamTuDiem = BigDecimal.ZERO; // Tiền giảm tương ứng điểm dùng
    private BigDecimal tienKhachDua;
    private BigDecimal tienThoiLai;
    private BigDecimal tienThanhToanThem = BigDecimal.ZERO;
    private BigDecimal tienHoanLai = BigDecimal.ZERO;
    private BigDecimal tongTien; // Thực thu sau giảm giá
    private String ghiChu;

    // Danh sách dòng chi tiết
    private List<ChiTietHoaDonSanPham> danhSachSanPham = new ArrayList<>();
    private List<ChiTietHoaDonDichVu> danhSachDichVu = new ArrayList<>();

    public HoaDon() {
        this.ngayLap = LocalDate.now();
        this.gioLap = LocalTime.now();
        this.hinhThucThanhToan = "Tiền mặt";
        this.trangThaiThanhToan = "Đã thanh toán";
        this.tienGiam = BigDecimal.ZERO;
        this.tongTien = BigDecimal.ZERO;
    }

    public HoaDon(String maHoaDon, String maLichHen, String maPhieuDichVu, String maNhanVien,
                   String maKhachHang, LocalDate ngayLap, String hinhThucThanhToan,
                   String trangThaiThanhToan, String maKhuyenMai, BigDecimal tongTien) {
        this();
        this.maHoaDon = maHoaDon;
        this.maLichHen = maLichHen;
        this.maPhieuDichVu = maPhieuDichVu;
        this.maNhanVien = maNhanVien;
        this.maKhachHang = maKhachHang;
        this.ngayLap = ngayLap != null ? ngayLap : LocalDate.now();
        this.hinhThucThanhToan = hinhThucThanhToan;
        this.trangThaiThanhToan = trangThaiThanhToan != null ? trangThaiThanhToan : "Đã thanh toán";
        this.maKhuyenMai = maKhuyenMai;
        this.tongTien = tongTien != null ? tongTien : BigDecimal.ZERO;
    }

    /**
     * Tính tổng tiền gốc (trước khi trừ khuyến mãi) từ danh sách sản phẩm và dịch vụ.
     */
    public BigDecimal tinhTongTienGoc() {
        BigDecimal tong = BigDecimal.ZERO;
        if (danhSachSanPham != null) {
            for (ChiTietHoaDonSanPham sp : danhSachSanPham) {
                tong = tong.add(sp.getThanhTien());
            }
        }
        if (danhSachDichVu != null) {
            for (ChiTietHoaDonDichVu dv : danhSachDichVu) {
                tong = tong.add(dv.getThanhTien());
            }
        }
        return tong;
    }

    public int getTongSoLuongMatHang() {
        int tong = 0;
        if (danhSachSanPham != null) {
            for (ChiTietHoaDonSanPham sp : danhSachSanPham) tong += sp.getSoLuong();
        }
        if (danhSachDichVu != null) {
            tong += danhSachDichVu.size();
        }
        return tong;
    }

    /**
     * Mô tả chi tiết các khoản ưu đãi (khuyến mãi và điểm tích lũy) áp dụng trên hóa đơn.
     */
    public String getMoTaGiamGia() {
        List<String> ds = new ArrayList<>();
        if (maKhuyenMai != null && !maKhuyenMai.isBlank()) {
            if (tienGiam != null && tienGiam.compareTo(BigDecimal.ZERO) > 0) {
                ds.add(maKhuyenMai + " (-" + String.format("%,d", tienGiam.longValue()) + "đ)");
            } else {
                ds.add(maKhuyenMai);
            }
        }
        if (diemTichLuySuDung > 0) {
            long tienDiem = tienGiamTuDiem != null && tienGiamTuDiem.compareTo(BigDecimal.ZERO) > 0
                    ? tienGiamTuDiem.longValue()
                    : diemTichLuySuDung * 1000L;
            ds.add("Dùng " + diemTichLuySuDung + " điểm (-" + String.format("%,d", tienDiem) + "đ)");
        }
        if (ds.isEmpty()) {
            return "--";
        }
        return String.join(" + ", ds);
    }

    // Getters and Setters
    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }

    public String getMaLichHen() { return maLichHen; }
    public void setMaLichHen(String maLichHen) { this.maLichHen = maLichHen; }

    public String getMaPhieuDichVu() { return maPhieuDichVu; }
    public void setMaPhieuDichVu(String maPhieuDichVu) { this.maPhieuDichVu = maPhieuDichVu; }

    public String getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(String maNhanVien) { this.maNhanVien = maNhanVien; }

    public String getTenNhanVien() { return tenNhanVien; }
    public void setTenNhanVien(String tenNhanVien) { this.tenNhanVien = tenNhanVien; }

    public String getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(String maKhachHang) { this.maKhachHang = maKhachHang; }

    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }

    public String getSoDienThoaiKhachHang() { return soDienThoaiKhachHang; }
    public void setSoDienThoaiKhachHang(String soDienThoaiKhachHang) { this.soDienThoaiKhachHang = soDienThoaiKhachHang; }

    public LocalDate getNgayLap() { return ngayLap; }
    public void setNgayLap(LocalDate ngayLap) { this.ngayLap = ngayLap; }

    public LocalTime getGioLap() { return gioLap; }
    public void setGioLap(LocalTime gioLap) { this.gioLap = gioLap; }

    public String getHinhThucThanhToan() { return hinhThucThanhToan; }
    public void setHinhThucThanhToan(String hinhThucThanhToan) { this.hinhThucThanhToan = hinhThucThanhToan; }

    public String getTrangThaiThanhToan() { return trangThaiThanhToan; }
    public void setTrangThaiThanhToan(String trangThaiThanhToan) { this.trangThaiThanhToan = trangThaiThanhToan; }

    public String getMaKhuyenMai() { return maKhuyenMai; }
    public void setMaKhuyenMai(String maKhuyenMai) { this.maKhuyenMai = maKhuyenMai; }

    public String getTenKhuyenMai() { return tenKhuyenMai; }
    public void setTenKhuyenMai(String tenKhuyenMai) { this.tenKhuyenMai = tenKhuyenMai; }

    public BigDecimal getTienGiam() { return tienGiam != null ? tienGiam : BigDecimal.ZERO; }
    public void setTienGiam(BigDecimal tienGiam) { this.tienGiam = tienGiam; }

    public BigDecimal getTongTien() { return tongTien != null ? tongTien : BigDecimal.ZERO; }
    public void setTongTien(BigDecimal tongTien) { this.tongTien = tongTien; }

    public int getDiemTichLuySuDung() { return diemTichLuySuDung; }
    public void setDiemTichLuySuDung(int diemTichLuySuDung) {
        this.diemTichLuySuDung = diemTichLuySuDung;
        this.tienGiamTuDiem = BigDecimal.valueOf(diemTichLuySuDung * 1000L);
    }

    public int getDiemTichLuyCong() { return diemTichLuyCong; }
    public void setDiemTichLuyCong(int diemTichLuyCong) { this.diemTichLuyCong = diemTichLuyCong; }

    public BigDecimal getTienGiamTuDiem() { return tienGiamTuDiem != null ? tienGiamTuDiem : BigDecimal.ZERO; }
    public void setTienGiamTuDiem(BigDecimal tienGiamTuDiem) { this.tienGiamTuDiem = tienGiamTuDiem; }

    public BigDecimal getTienKhachDua() { return tienKhachDua; }
    public void setTienKhachDua(BigDecimal tienKhachDua) { this.tienKhachDua = tienKhachDua; }

    public BigDecimal getTienThoiLai() { return tienThoiLai; }
    public void setTienThoiLai(BigDecimal tienThoiLai) { this.tienThoiLai = tienThoiLai; }

    public BigDecimal getTienThanhToanThem() { return tienThanhToanThem != null ? tienThanhToanThem : BigDecimal.ZERO; }
    public void setTienThanhToanThem(BigDecimal tienThanhToanThem) { this.tienThanhToanThem = tienThanhToanThem; }

    public BigDecimal getTienHoanLai() { return tienHoanLai != null ? tienHoanLai : BigDecimal.ZERO; }
    public void setTienHoanLai(BigDecimal tienHoanLai) { this.tienHoanLai = tienHoanLai; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public List<ChiTietHoaDonSanPham> getDanhSachSanPham() { return danhSachSanPham; }
    public void setDanhSachSanPham(List<ChiTietHoaDonSanPham> danhSachSanPham) { this.danhSachSanPham = danhSachSanPham; }

    public List<ChiTietHoaDonDichVu> getDanhSachDichVu() { return danhSachDichVu; }
    public void setDanhSachDichVu(List<ChiTietHoaDonDichVu> danhSachDichVu) { this.danhSachDichVu = danhSachDichVu; }
}

