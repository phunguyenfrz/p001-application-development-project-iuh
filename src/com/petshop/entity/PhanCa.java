package com.petshop.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Thực thể Phân công ca trực cho nhân viên (bảng PhanCa).
 * Chuẩn hóa OOP:
 * - Khóa ngoại sử dụng Object Reference: NhanVien nhanVien, Ca ca.
 * - Giữ lại các Delegate Getter/Setter (getMaNhanVien, getTenNhanVien, getMaCa, getTenCa...) để tương thích TableView.
 */
public class PhanCa {

    public static final String TRANG_THAI_DA_PHAN_CONG = "Đã phân công";
    public static final String TRANG_THAI_DA_HUY = "Đã hủy";

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm");

    private String maPhanCa;
    private NhanVien nhanVien;
    private Ca ca;
    private LocalDate ngayLam;
    private String trangThai;

    public PhanCa() {
        this.trangThai = TRANG_THAI_DA_PHAN_CONG;
    }

    public PhanCa(String maPhanCa, NhanVien nhanVien, Ca ca, LocalDate ngayLam, String trangThai) {
        this.maPhanCa = maPhanCa;
        this.nhanVien = nhanVien;
        this.ca = ca;
        this.ngayLam = ngayLam;
        this.trangThai = trangThai;
    }

    public String getMaPhanCa() {
        return maPhanCa;
    }

    public void setMaPhanCa(String maPhanCa) {
        this.maPhanCa = maPhanCa;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public String getMaNhanVien() {
        return nhanVien != null ? nhanVien.getMaNhanVien() : null;
    }

    public void setMaNhanVien(String maNhanVien) {
        if (maNhanVien == null) {
            this.nhanVien = null;
            return;
        }
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

    public Ca getCa() {
        return ca;
    }

    public void setCa(Ca ca) {
        this.ca = ca;
    }

    public String getMaCa() {
        return ca != null ? ca.getMaCa() : null;
    }

    public void setMaCa(String maCa) {
        if (maCa == null) {
            this.ca = null;
            return;
        }
        if (this.ca == null) {
            this.ca = new Ca();
        }
        this.ca.setMaCa(maCa);
    }

    public String getTenCa() {
        return ca != null ? ca.getTenCa() : null;
    }

    public void setTenCa(String tenCa) {
        if (this.ca == null) {
            this.ca = new Ca();
        }
        this.ca.setTenCa(tenCa);
    }

    public LocalTime getGioBatDau() {
        return ca != null ? ca.getGioBatDau() : null;
    }

    public void setGioBatDau(LocalTime gioBatDau) {
        if (this.ca == null) {
            this.ca = new Ca();
        }
        this.ca.setGioBatDau(gioBatDau);
    }

    public LocalTime getGioKetThuc() {
        return ca != null ? ca.getGioKetThuc() : null;
    }

    public void setGioKetThuc(LocalTime gioKetThuc) {
        if (this.ca == null) {
            this.ca = new Ca();
        }
        this.ca.setGioKetThuc(gioKetThuc);
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

    public String getNgayLamDinhDang() {
        return ngayLam != null ? ngayLam.format(DINH_DANG_NGAY) : "";
    }

    public String getKhungGio() {
        LocalTime bd = getGioBatDau();
        LocalTime kt = getGioKetThuc();
        if (bd == null || kt == null) return "";
        return bd.format(DINH_DANG_GIO) + " - " + kt.format(DINH_DANG_GIO);
    }

    public boolean isDaPhanCong() {
        return TRANG_THAI_DA_PHAN_CONG.equalsIgnoreCase(trangThai);
    }
}
