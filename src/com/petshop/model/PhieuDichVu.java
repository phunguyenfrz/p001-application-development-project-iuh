package com.petshop.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Thực thể phiếu tiếp nhận và theo dõi dịch vụ thú cưng tại quầy.
 */
public class PhieuDichVu {

    private String maPhieuDichVu;
    private String maKhachHang;
    private String maNhanVien;
    private String maLichHen;
    private LocalDate ngayTiepNhan;
    private LocalTime gioTiepNhan;
    private String trangThai; // "Đang chờ", "Đang thực hiện", "Hoàn thành", "Đã hủy"
    private String ghiChu;

    // Các trường hỗ trợ hiển thị trên giao diện (lấy qua JOIN)
    private String tenKhachHang;
    private String soDienThoai;
    private String tenNhanVien;
    private List<ChiTietPhieuDichVu> danhSachChiTiet;

    public PhieuDichVu() {
        this.trangThai = "Đang chờ";
        this.ngayTiepNhan = LocalDate.now();
        this.gioTiepNhan = LocalTime.now();
        this.danhSachChiTiet = new ArrayList<>();
    }

    public PhieuDichVu(String maPhieuDichVu, String maKhachHang, String maNhanVien, String maLichHen,
                       LocalDate ngayTiepNhan, LocalTime gioTiepNhan, String trangThai, String ghiChu) {
        this.maPhieuDichVu = maPhieuDichVu;
        this.maKhachHang = maKhachHang;
        this.maNhanVien = maNhanVien;
        this.maLichHen = maLichHen;
        this.ngayTiepNhan = ngayTiepNhan != null ? ngayTiepNhan : LocalDate.now();
        this.gioTiepNhan = gioTiepNhan != null ? gioTiepNhan : LocalTime.now();
        this.trangThai = trangThai != null ? trangThai : "Đang chờ";
        this.ghiChu = ghiChu;
        this.danhSachChiTiet = new ArrayList<>();
    }

    // Tính tổng tiền các dịch vụ có trong phiếu
    public BigDecimal getTongTien() {
        BigDecimal tong = BigDecimal.ZERO;
        if (danhSachChiTiet != null) {
            for (ChiTietPhieuDichVu ct : danhSachChiTiet) {
                if (ct.getDonGia() != null) {
                    tong = tong.add(ct.getDonGia());
                }
            }
        }
        return tong;
    }

    public String getMaPhieuDichVu() {
        return maPhieuDichVu;
    }

    public void setMaPhieuDichVu(String maPhieuDichVu) {
        this.maPhieuDichVu = maPhieuDichVu;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public String getMaLichHen() {
        return maLichHen;
    }

    public void setMaLichHen(String maLichHen) {
        this.maLichHen = maLichHen;
    }

    public LocalDate getNgayTiepNhan() {
        return ngayTiepNhan;
    }

    public void setNgayTiepNhan(LocalDate ngayTiepNhan) {
        this.ngayTiepNhan = ngayTiepNhan;
    }

    public LocalTime getGioTiepNhan() {
        return gioTiepNhan;
    }

    public void setGioTiepNhan(LocalTime gioTiepNhan) {
        this.gioTiepNhan = gioTiepNhan;
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

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getTenNhanVien() {
        return tenNhanVien;
    }

    public void setTenNhanVien(String tenNhanVien) {
        this.tenNhanVien = tenNhanVien;
    }

    public List<ChiTietPhieuDichVu> getDanhSachChiTiet() {
        return danhSachChiTiet;
    }

    public void setDanhSachChiTiet(List<ChiTietPhieuDichVu> danhSachChiTiet) {
        this.danhSachChiTiet = danhSachChiTiet;
    }
}

