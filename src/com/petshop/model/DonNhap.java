package com.petshop.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DonNhap(String maDonHang, String tenNhaCungCap, LocalDate ngayLap, BigDecimal tongTien) {

    public record NhaCungCap(String maNhaCungCap, String tenNhaCungCap) {
        @Override
        public String toString() {
            return tenNhaCungCap + " (" + maNhaCungCap + ")";
        }
    }

    public record SanPham(String maSanPham, String tenSanPham, String maNhaCungCap) {
        @Override
        public String toString() {
            return tenSanPham + " (" + maSanPham + ")";
        }
    }

    public record ChiTiet(String maSanPham, String tenSanPham, int soLuong, BigDecimal donGia) {
    }
}