package com.petshop.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Thực thể lịch hẹn dịch vụ chăm sóc thú cưng của khách hàng.
 */
public class LichHen {

    private String maLichHen;
    private String maKhachHang;
    private String maThuCung;
    private String maNhanVien;
    private LocalDate ngayHen;
    private LocalTime gioHen;
    private String trangThai;
    private String ghiChu;

    // Các trường hỗ trợ hiển thị lên giao diện
    private String tenKhachHang;
    private String soDienThoai;
    private String tenThuCung;
    private String tenNhanVien;
    private List<ChiTietLichHen> danhSachChiTiet;

    public LichHen() {
        this.trangThai = "Đã đặt";
        this.danhSachChiTiet = new ArrayList<>();
    }

    public LichHen(String maLichHen, String maKhachHang, String maThuCung, String maNhanVien,
                   LocalDate ngayHen, LocalTime gioHen, String trangThai, String ghiChu) {
        this.maLichHen = maLichHen;
        this.maKhachHang = maKhachHang;
        this.maThuCung = maThuCung;
        this.maNhanVien = maNhanVien;
        this.ngayHen = ngayHen;
        this.gioHen = gioHen;
        this.trangThai = trangThai != null ? trangThai : "Đã đặt";
        this.ghiChu = ghiChu;
        this.danhSachChiTiet = new ArrayList<>();
    }

    public String getMaLichHen() {
        return maLichHen;
    }

    public void setMaLichHen(String maLichHen) {
        this.maLichHen = maLichHen;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getMaThuCung() {
        return maThuCung;
    }

    public void setMaThuCung(String maThuCung) {
        this.maThuCung = maThuCung;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public LocalDate getNgayHen() {
        return ngayHen;
    }

    public void setNgayHen(LocalDate ngayHen) {
        this.ngayHen = ngayHen;
    }

    public LocalTime getGioHen() {
        return gioHen;
    }

    public void setGioHen(LocalTime gioHen) {
        this.gioHen = gioHen;
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

    public String getTenThuCung() {
        return tenThuCung;
    }

    public void setTenThuCung(String tenThuCung) {
        this.tenThuCung = tenThuCung;
    }

    public String getTenNhanVien() {
        return tenNhanVien;
    }

    public void setTenNhanVien(String tenNhanVien) {
        this.tenNhanVien = tenNhanVien;
    }

    public List<ChiTietLichHen> getDanhSachChiTiet() {
        return danhSachChiTiet;
    }

    public void setDanhSachChiTiet(List<ChiTietLichHen> danhSachChiTiet) {
        this.danhSachChiTiet = danhSachChiTiet != null ? danhSachChiTiet : new ArrayList<>();
    }

    // Tính tổng tiền các dịch vụ đã chọn trong lịch hẹn
    public BigDecimal getTongTien() {
        BigDecimal tong = BigDecimal.ZERO;
        if (danhSachChiTiet != null) {
            for (ChiTietLichHen ct : danhSachChiTiet) {
                if (ct.getDonGia() != null) {
                    tong = tong.add(ct.getDonGia());
                }
            }
        }
        return tong;
    }

    // Lấy chuỗi danh sách tên dịch vụ để hiển thị lên bảng
    public String getChuoiDichVu() {
        if (danhSachChiTiet == null || danhSachChiTiet.isEmpty()) {
            return "Chưa có dịch vụ";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < danhSachChiTiet.size(); i++) {
            ChiTietLichHen ct = danhSachChiTiet.get(i);
            if (ct.getTenDichVu() != null && !ct.getTenDichVu().isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(ct.getTenDichVu());
            }
        }
        return sb.toString();
    }
}

