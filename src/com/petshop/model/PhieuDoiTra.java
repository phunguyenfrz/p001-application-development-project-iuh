package com.petshop.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Thực thể biểu diễn Phiếu đổi trả hàng hóa (PhieuDoiTra).
 */
public class PhieuDoiTra {

    private String maPhieuDoiTra;
    private String maHoaDon;
    private String maNhanVien;
    private String tenNhanVien;
    private String maKhachHang;
    private String tenKhachHang;
    private String soDienThoaiKhachHang;
    private LocalDate ngayLap;
    private String lyDo; // Lý do chính: "Lỗi sản phẩm", "Hết hạn", "Không phù hợp", "Khác"
    private int soLuong; // Tổng số lượng hàng trả
    private String loaiXuLy; // "Trả hàng, hoàn tiền" hoặc "Đổi sang sản phẩm khác"
    private String hinhThucHoan; // "Tiền mặt", "Chuyển khoản", "Voucher"
    private BigDecimal tienHoan; // Tiền hoàn lại cho khách
    private BigDecimal tienBu; // Tiền khách bù thêm nếu đổi hàng giá cao hơn
    private String trangThai; // "Chờ xử lý", "Hoàn thành", "Đã hoàn tiền", "Từ chối"
    private String ghiChu;

    // Danh sách mặt hàng chi tiết trong phiếu đổi trả
    private List<ChiTietPhieuDoiTra> danhSachSanPham = new ArrayList<>();

    public PhieuDoiTra() {
        this.ngayLap = LocalDate.now();
        this.soLuong = 0;
        this.loaiXuLy = "Trả hàng, hoàn tiền";
        this.hinhThucHoan = "Tiền mặt";
        this.tienHoan = BigDecimal.ZERO;
        this.tienBu = BigDecimal.ZERO;
        this.trangThai = "Chờ xử lý";
    }

    public PhieuDoiTra(String maPhieuDoiTra, String maHoaDon, String maNhanVien, String maKhachHang,
                       LocalDate ngayLap, String lyDo, int soLuong, String loaiXuLy,
                       BigDecimal tienHoan, BigDecimal tienBu, String trangThai) {
        this();
        this.maPhieuDoiTra = maPhieuDoiTra;
        this.maHoaDon = maHoaDon;
        this.maNhanVien = maNhanVien;
        this.maKhachHang = maKhachHang;
        this.ngayLap = ngayLap != null ? ngayLap : LocalDate.now();
        this.lyDo = lyDo;
        this.soLuong = soLuong;
        this.loaiXuLy = loaiXuLy != null ? loaiXuLy : "Trả hàng, hoàn tiền";
        this.tienHoan = tienHoan != null ? tienHoan : BigDecimal.ZERO;
        this.tienBu = tienBu != null ? tienBu : BigDecimal.ZERO;
        this.trangThai = trangThai != null ? trangThai : "Chờ xử lý";
    }

    // Tính tổng giá trị các mặt hàng khách trả lại
    public BigDecimal tinhTongTienHangTra() {
        BigDecimal tong = BigDecimal.ZERO;
        if (danhSachSanPham != null && !danhSachSanPham.isEmpty()) {
            for (ChiTietPhieuDoiTra ct : danhSachSanPham) {
                tong = tong.add(ct.tinhThanhTienTra());
            }
            if (tong.compareTo(BigDecimal.ZERO) > 0) {
                return tong;
            }
        }
        if (tienHoan != null && tienHoan.compareTo(BigDecimal.ZERO) > 0) {
            return tienHoan;
        }
        return tong;
    }

    // Tính tổng giá trị các mặt hàng khách đổi lấy mới
    public BigDecimal tinhTongTienHangDoi() {
        BigDecimal tong = BigDecimal.ZERO;
        if (danhSachSanPham != null) {
            for (ChiTietPhieuDoiTra ct : danhSachSanPham) {
                tong = tong.add(ct.tinhThanhTienDoi());
            }
        }
        return tong;
    }

    public String getMaPhieuDoiTra() {
        return maPhieuDoiTra;
    }

    public void setMaPhieuDoiTra(String maPhieuDoiTra) {
        this.maPhieuDoiTra = maPhieuDoiTra;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
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

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
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

    public LocalDate getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(LocalDate ngayLap) {
        this.ngayLap = ngayLap;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public String getLoaiXuLy() {
        return loaiXuLy;
    }

    public void setLoaiXuLy(String loaiXuLy) {
        this.loaiXuLy = loaiXuLy;
    }

    public String getHinhThucHoan() {
        return hinhThucHoan;
    }

    public void setHinhThucHoan(String hinhThucHoan) {
        this.hinhThucHoan = hinhThucHoan;
    }

    public BigDecimal getTienHoan() {
        return tienHoan != null ? tienHoan : BigDecimal.ZERO;
    }

    public void setTienHoan(BigDecimal tienHoan) {
        this.tienHoan = tienHoan != null ? tienHoan : BigDecimal.ZERO;
    }

    public BigDecimal getTienBu() {
        return tienBu != null ? tienBu : BigDecimal.ZERO;
    }

    public void setTienBu(BigDecimal tienBu) {
        this.tienBu = tienBu != null ? tienBu : BigDecimal.ZERO;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public List<ChiTietPhieuDoiTra> getDanhSachSanPham() {
        return danhSachSanPham;
    }

    public void setDanhSachSanPham(List<ChiTietPhieuDoiTra> danhSachSanPham) {
        this.danhSachSanPham = danhSachSanPham != null ? danhSachSanPham : new ArrayList<>();
    }

    public void themChiTiet(ChiTietPhieuDoiTra ct) {
        if (this.danhSachSanPham == null) {
            this.danhSachSanPham = new ArrayList<>();
        }
        if (ct != null) {
            this.danhSachSanPham.add(ct);
        }
    }
}

