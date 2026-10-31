package com.petshop.model;

/**
 * Thực thể đại diện cho nhân viên trong cửa hàng Happy Tails.
 * Tương ứng với bảng NhanVien trong cơ sở dữ liệu.
 */
public class NhanVien {

    private String maNhanVien;
    private String hoTen;
    private String soDienThoai;
    private String chucVu;
    private boolean gioiTinh; // true: Nam, false: Nữ
    private String trangThai; // "Đang làm", "Đã nghỉ việc"

    public NhanVien() {
    }

    public NhanVien(String maNhanVien, String hoTen, String soDienThoai, String chucVu, boolean gioiTinh, String trangThai) {
        this.maNhanVien = maNhanVien;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.chucVu = chucVu;
        this.gioiTinh = gioiTinh;
        this.trangThai = trangThai;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getChucVu() {
        return chucVu;
    }

    public void setChucVu(String chucVu) {
        this.chucVu = chucVu;
    }

    public boolean isGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(boolean gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    @Override
    public String toString() {
        return hoTen + " (" + maNhanVien + " - " + chucVu + ")";
    }
}

