package com.petshop.model;

import java.math.BigDecimal;

/**
 * Thực thể chi tiết một dòng sản phẩm hoặc dịch vụ trong hóa đơn giao dịch.
 * Dùng để hiển thị trong popup Chi tiết giao dịch.
 */
public class ChiTietGiaoDich {

    /** Mã sản phẩm hoặc mã dịch vụ */
    private final String maDoiTuong;

    /** Tên sản phẩm hoặc tên dịch vụ */
    private final String tenDoiTuong;

    /** Số lượng sử dụng / mua */
    private final int soLuong;

    /** Đơn giá tại thời điểm lập hóa đơn */
    private final BigDecimal donGia;

    public ChiTietGiaoDich(String maDoiTuong, String tenDoiTuong, int soLuong, BigDecimal donGia) {
        this.maDoiTuong = maDoiTuong;
        this.tenDoiTuong = tenDoiTuong;
        this.soLuong = soLuong;
        this.donGia = donGia != null ? donGia : BigDecimal.ZERO;
    }

    public String getMaDoiTuong() {
        return maDoiTuong;
    }

    public String getTenDoiTuong() {
        return tenDoiTuong;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    /**
     * Tính thành tiền của dòng: Số lượng * Đơn giá.
     */
    public BigDecimal getThanhTien() {
        return donGia.multiply(BigDecimal.valueOf(soLuong));
    }
}

