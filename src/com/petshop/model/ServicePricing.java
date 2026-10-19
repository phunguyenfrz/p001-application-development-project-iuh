package com.petshop.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Model đại diện cho Bảng giá dịch vụ theo 3 bậc kích thước thú cưng (bảng BangGiaDichVu).
 * Tuân thủ tiêu chuẩn: tên lớp là danh từ, các trường khớp CSDL và tài liệu OOAD 2.11.
 */
public class ServicePricing {

    public static final String BAC_NHO = "Nhỏ";   // < 5kg
    public static final String BAC_VUA = "Vừa";   // 5 - 10kg
    public static final String BAC_LON = "Lớn";   // > 10kg

    private String maBangGia;           // BG-YYYYMMDD-NNNNNN
    private String maDichVu;            // FK DichVuChamSoc
    private String tenDichVu;           // Thuộc tính mở rộng để hiển thị UI
    private double gia;                 // CHECK (gia >= 0)
    private String phanLoaiKichThuoc;   // 'Nhỏ', 'Vừa', 'Lớn'
    private LocalDate ngayApDung;
    private LocalDate ngayHetHieuLuc;   // Nullable

    public ServicePricing() {
        this.phanLoaiKichThuoc = BAC_NHO;
        this.ngayApDung = LocalDate.now();
    }

    public ServicePricing(String maBangGia, String maDichVu, double gia, String phanLoaiKichThuoc, LocalDate ngayApDung, LocalDate ngayHetHieuLuc) {
        this.maBangGia = maBangGia;
        this.maDichVu = maDichVu;
        this.gia = gia;
        this.phanLoaiKichThuoc = phanLoaiKichThuoc;
        this.ngayApDung = ngayApDung != null ? ngayApDung : LocalDate.now();
        this.ngayHetHieuLuc = ngayHetHieuLuc;
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

    public String getTenDichVu() {
        return tenDichVu;
    }

    public void setTenDichVu(String tenDichVu) {
        this.tenDichVu = tenDichVu;
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

    public boolean isConHieuLuc() {
        LocalDate today = LocalDate.now();
        if (ngayApDung != null && today.isBefore(ngayApDung)) return false;
        return ngayHetHieuLuc == null || !today.isAfter(ngayHetHieuLuc);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServicePricing that = (ServicePricing) o;
        return Objects.equals(maBangGia, that.maBangGia);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maBangGia);
    }

    @Override
    public String toString() {
        return "ServicePricing{" +
                "maBangGia='" + maBangGia + '\'' +
                ", tenDichVu='" + tenDichVu + '\'' +
                ", phanLoaiKichThuoc='" + phanLoaiKichThuoc + '\'' +
                ", gia=" + gia +
                ", ngayApDung=" + ngayApDung +
                '}';
    }
}

