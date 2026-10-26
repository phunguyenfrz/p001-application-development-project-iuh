package com.petshop.model;

import java.time.LocalDate;

/**
 * Thực thể Bảng giá dịch vụ theo bậc kích thước thú cưng (Nhỏ, Vừa, Lớn), ánh xạ bảng BangGiaDichVu.
 */
public class BangGiaDichVu {

    // Hằng số dùng chung cho ngưỡng cân nặng và nhãn hiển thị của 3 bậc giá
    public static final double NGUONG_CAN_NANG_NHO = 5.0;
    public static final double NGUONG_CAN_NANG_VUA = 10.0;

    public static final String KHOANG_CAN_NANG_NHO = "dưới 5 kg";
    public static final String KHOANG_CAN_NANG_VUA = "5 - 10 kg";
    public static final String KHOANG_CAN_NANG_LON = "trên 10 kg";

    public static final String NHAN_GIA_NHO = "Giá bậc Nhỏ (" + KHOANG_CAN_NANG_NHO + ")";
    public static final String NHAN_GIA_VUA = "Giá bậc Vừa (" + KHOANG_CAN_NANG_VUA + ")";
    public static final String NHAN_GIA_LON = "Giá bậc Lớn (" + KHOANG_CAN_NANG_LON + ")";

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

    // Quy đổi cân nặng (kg) sang bậc kích thước dựa trên hằng số ngưỡng dùng chung
    public static String xacDinhKichThuoc(double canNang) {
        if (canNang < NGUONG_CAN_NANG_NHO) {
            return "Nhỏ";
        }
        if (canNang <= NGUONG_CAN_NANG_VUA) {
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
