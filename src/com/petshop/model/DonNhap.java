package com.petshop.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DonNhap(String maDonHang, String tenNhaCungCap, LocalDate ngayLap, BigDecimal tongTien) {

    public record ChiTiet(String maSanPham, String tenSanPham, int soLuong, BigDecimal donGia) {
    }
}