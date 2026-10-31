package com.petshop.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Thực thể Phân ca làm việc cho nhân viên theo ngày.
 * Tương ứng bảng PhanCa trong cơ sở dữ liệu.
 */
public class PhanCa {

    public static final String TRANG_THAI_DA_PHAN_CONG = "Đã phân công";
    public static final String TRANG_THAI_DA_HUY = "Đã hủy";

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm");

    private String maPhanCa;
    private String maNhanVien;
    private String tenNhanVien;
    private String maCa;
    private String tenCa;
    private LocalTime gioBatDau;
    private LocalTime gioKetThuc;
    private LocalDate ngayLam;
    private String trangThai; // "Đã phân công" hoặc "Đã hủy"

    public PhanCa() {
        this.trangThai = TRANG_THAI_DA_PHAN_CONG;
    }

    public PhanCa(String maPhanCa, String maNhanVien, String maCa, LocalDate ngayLam, String trangThai) {
        this.maPhanCa = maPhanCa;
        this.maNhanVien = maNhanVien;
        this.maCa = maCa;
        this.ngayLam = ngayLam;
        this.trangThai = (trangThai == null || trangThai.isBlank()) ? TRANG_THAI_DA_PHAN_CONG : trangThai;
    }

    public String getMaPhanCa() {
        return maPhanCa;
    }

    public void setMaPhanCa(String maPhanCa) {
        this.maPhanCa = maPhanCa;
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

    public String getMaCa() {
        return maCa;
    }

    public void setMaCa(String maCa) {
        this.maCa = maCa;
    }

    public String getTenCa() {
        return tenCa;
    }

    public void setTenCa(String tenCa) {
        this.tenCa = tenCa;
    }

    public LocalTime getGioBatDau() {
        return gioBatDau;
    }

    public void setGioBatDau(LocalTime gioBatDau) {
        this.gioBatDau = gioBatDau;
    }

    public LocalTime getGioKetThuc() {
        return gioKetThuc;
    }

    public void setGioKetThuc(LocalTime gioKetThuc) {
        this.gioKetThuc = gioKetThuc;
    }

    public LocalDate getNgayLam() {
        return ngayLam;
    }

    public void setNgayLam(LocalDate ngayLam) {
        this.ngayLam = ngayLam;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public boolean isDaPhanCong() {
        return TRANG_THAI_DA_PHAN_CONG.equalsIgnoreCase(trangThai);
    }

    public String getNgayLamDinhDang() {
        return ngayLam != null ? ngayLam.format(DINH_DANG_NGAY) : "";
    }

    public String getKhungGio() {
        if (gioBatDau != null && gioKetThuc != null) {
            return gioBatDau.format(DINH_DANG_GIO) + " - " + gioKetThuc.format(DINH_DANG_GIO);
        }
        return "";
    }

    @Override
    public String toString() {
        return (tenCa != null ? tenCa : maCa) + " - " + (tenNhanVien != null ? tenNhanVien : maNhanVien) + " (" + getNgayLamDinhDang() + ")";
    }
}

