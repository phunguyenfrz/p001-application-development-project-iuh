package com.petshop.entity;

import java.time.LocalDate;

/**
 * Thực thể biểu diễn một lần hẹn chăm sóc dịch vụ của thú cưng (ánh xạ từ bảng LichHen).
 */
public class LichSuChamSoc {

    private final String maLichHen;
    private final ThuCung thuCung;
    private final KhachHang khachHang;
    private final NhanVien nhanVien;
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
        this.thuCung = new ThuCung();
        this.thuCung.setMaThuCung(maThuCung);
        this.thuCung.setTenThuCung(tenThuCung);
        this.khachHang = new KhachHang();
        this.khachHang.setMaKhachHang(maKhachHang);
        this.khachHang.setHoTen(tenKhachHang);
        this.nhanVien = new NhanVien();
        this.nhanVien.setMaNhanVien(maNhanVien);
        this.nhanVien.setHoTen(tenNhanVien);
        this.ngayHen = ngayHen;
        this.gioHen = gioHen;
        this.trangThai = trangThai != null ? trangThai : "Hoàn thành";
        this.ghiChu = ghiChu != null ? ghiChu : "";
    }

    public String getMaLichHen() {
        return maLichHen;
    }

    public ThuCung getThuCung() {
        return thuCung;
    }

    public String getMaThuCung() {
        return thuCung != null ? thuCung.getMaThuCung() : null;
    }

    public String getTenThuCung() {
        return thuCung != null ? thuCung.getTenThuCung() : null;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public String getMaKhachHang() {
        return khachHang != null ? khachHang.getMaKhachHang() : null;
    }

    public String getTenKhachHang() {
        return khachHang != null ? khachHang.getHoTen() : null;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public String getMaNhanVien() {
        return nhanVien != null ? nhanVien.getMaNhanVien() : null;
    }

    public String getTenNhanVien() {
        return nhanVien != null ? nhanVien.getHoTen() : null;
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

