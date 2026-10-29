package com.petshop.model;

import java.time.LocalDate;

/**
 * Thực thể biểu diễn một lần hẹn chăm sóc dịch vụ của thú cưng (ánh xạ từ bảng LichHen).
 */
public class LichSuChamSoc {

    private final String maLichHen;
    private final String maThuCung;
    private final String tenThuCung;
    private final String maKhachHang;
    private final String tenKhachHang;
    private final String maNhanVien;
    private final String tenNhanVien;
    private final LocalDate ngayHen;
    private final String gioHen;
    private final String trangThai;
    private final String ghiChu;

    public LichSuChamSoc(String maLichHen, String maThuCung, String tenThuCung,
                         String maKhachHang, String tenKhachHang,
                         String maNhanVien, String tenNhanVien,
                         LocalDate ngayHen, String gioHen,
                         String trangThai, String ghiChu) {
        this.maLichHen = maLichHen;
        this.maThuCung = maThuCung;
        this.tenThuCung = tenThuCung;
        this.maKhachHang = maKhachHang;
        this.tenKhachHang = tenKhachHang;
        this.maNhanVien = maNhanVien;
        this.tenNhanVien = tenNhanVien;
        this.ngayHen = ngayHen;
        this.gioHen = gioHen;
        this.trangThai = trangThai != null ? trangThai : "Hoàn thành";
        this.ghiChu = ghiChu != null ? ghiChu : "";
    }

    public String getMaLichHen() {
        return maLichHen;
    }

    public String getMaThuCung() {
        return maThuCung;
    }

    public String getTenThuCung() {
        return tenThuCung;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public String getTenNhanVien() {
        return tenNhanVien;
    }

    public LocalDate getNgayHen() {
        return ngayHen;
    }

    public String getGioHen() {
        return gioHen;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }
}

