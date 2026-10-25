package com.petshop.model;

import java.time.LocalDate;

/**
 * Thực thể Bảng giá dịch vụ theo bậc kích thước thú cưng (Nhỏ, Vừa, Lớn), ánh xạ bảng BangGiaDichVu.
 */
public class BangGiaDichVu {

    private String maBangGia;
    private String maDichVu;
    private double gia;
    private String phanLoaiKichThuoc; // "Nhỏ", "Vừa", "Lớn"
    private LocalDate ngayApDung;
    private LocalDate ngayHetHieuLuc;

    public BangGiaDichVu() {
    }

    public BangGiaDichVu(String maBangGia, String maDichVu, double gia,
                         String phanLoaiKichThuoc, LocalDate ngayApDung, LocalDate ngayHetHieuLuc) {
        this.maBangGia = maBangGia;
        this.maDichVu = maDichVu;
        this.gia = gia;
        this.phanLoaiKichThuoc = phanLoaiKichThuoc;
        this.ngayApDung = ngayApDung;
        this.ngayHetHieuLuc = ngayHetHieuLuc;
    }

    // Quy đổi cân nặng (kg) sang bậc kích thước: < 5kg là Nhỏ, 5-10kg là Vừa, > 10kg là Lớn
    public static String xacDinhKichThuoc(double canNang) {
        if (canNang < 5.0) {
            return "Nhỏ";
        }
        if (canNang <= 10.0) {
            return "Vừa";
        }
        return "Lớn";
    }

    public String getMaBangGia() {
        return maBangGia;
    }

    public void setMaBangGia(String maBangGia) {
        this.maBangGia = maBangGia;
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(String maDichVu) {
        this.maDichVu = maDichVu;
    }

    public double getGia() {
        return gia;
    }

    public void setGia(double gia) {
        this.gia = gia;
    }

    public String getPhanLoaiKichThuoc() {
        return phanLoaiKichThuoc;
    }

    public void setPhanLoaiKichThuoc(String phanLoaiKichThuoc) {
        this.phanLoaiKichThuoc = phanLoaiKichThuoc;
    }

    public LocalDate getNgayApDung() {
        return ngayApDung;
    }

    public void setNgayApDung(LocalDate ngayApDung) {
        this.ngayApDung = ngayApDung;
    }

    public LocalDate getNgayHetHieuLuc() {
        return ngayHetHieuLuc;
    }

    public void setNgayHetHieuLuc(LocalDate ngayHetHieuLuc) {
        this.ngayHetHieuLuc = ngayHetHieuLuc;
    }
}

