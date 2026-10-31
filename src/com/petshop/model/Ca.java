package com.petshop.model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Thực thể Ca trực làm việc trong cửa hàng.
 * Tương ứng bảng Ca trong cơ sở dữ liệu.
 */
public class Ca {

    public static final String TRANG_THAI_AP_DUNG = "Áp dụng";
    public static final String TRANG_THAI_NGUNG = "Ngừng áp dụng";

    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm");

    private String maCa;
    private String tenCa;
    private LocalTime gioBatDau;
    private LocalTime gioKetThuc;
    private String trangThai; // "Áp dụng" hoặc "Ngừng áp dụng"

    public Ca() {
        this.trangThai = TRANG_THAI_AP_DUNG;
    }

    public Ca(String maCa, String tenCa, LocalTime gioBatDau, LocalTime gioKetThuc, String trangThai) {
        this.maCa = maCa;
        this.tenCa = tenCa;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
        this.trangThai = (trangThai == null || trangThai.isBlank()) ? TRANG_THAI_AP_DUNG : trangThai;
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

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public boolean isDangApDung() {
        return TRANG_THAI_AP_DUNG.equalsIgnoreCase(trangThai);
    }

    public String getGioBatDauDinhDang() {
        return gioBatDau != null ? gioBatDau.format(DINH_DANG_GIO) : "";
    }

    public String getGioKetThucDinhDang() {
        return gioKetThuc != null ? gioKetThuc.format(DINH_DANG_GIO) : "";
    }

    public String getKhungGio() {
        return getGioBatDauDinhDang() + " - " + getGioKetThucDinhDang();
    }

    @Override
    public String toString() {
        return tenCa + " (" + getKhungGio() + ")";
    }
}

