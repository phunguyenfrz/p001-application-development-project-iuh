package com.petshop.model;

import java.math.BigDecimal;

/**
 * Một dòng sản phẩm hoặc dịch vụ trong chi tiết giao dịch.
 * Dữ liệu dùng để hiển thị, không cho chỉnh sửa.
 */
public class CustomerTransactionItem {

    // Mã sản phẩm hoặc mã dịch vụ.
    private final String maDoiTuong;

    // Tên sản phẩm hoặc tên dịch vụ.
    private final String tenDoiTuong;

    // Số lượng đã được lưu trong chi tiết hóa đơn.
    private final int soLuong;

    // Đơn giá tại thời điểm lập hóa đơn.
    private final BigDecimal donGia;

    /**
     * Khởi tạo dòng chi tiết giao dịch.
     *
     * @param maDoiTuong Mã sản phẩm hoặc dịch vụ.
     * @param tenDoiTuong Tên sản phẩm hoặc dịch vụ.
     * @param soLuong Số lượng trong chi tiết hóa đơn.
     * @param donGia Đơn giá trong chi tiết hóa đơn.
     */
    public CustomerTransactionItem(
            String maDoiTuong,
            String tenDoiTuong,
            int soLuong,
            BigDecimal donGia
    ) {
        this.maDoiTuong = maDoiTuong;
        this.tenDoiTuong = tenDoiTuong;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    /** @return Mã sản phẩm hoặc dịch vụ. */
    public String getMaDoiTuong() {
        return maDoiTuong;
    }

    /** @return Tên sản phẩm hoặc dịch vụ. */
    public String getTenDoiTuong() {
        return tenDoiTuong;
    }

    /** @return Số lượng trong chi tiết hóa đơn. */
    public int getSoLuong() {
        return soLuong;
    }

    /** @return Đơn giá tại thời điểm lập hóa đơn. */
    public BigDecimal getDonGia() {
        return donGia;
    }

    /**
     * Tính thành tiền của riêng dòng chi tiết.
     *
     * @return Số lượng nhân với đơn giá.
     */
    public BigDecimal getThanhTien() {
        return donGia.multiply(BigDecimal.valueOf(soLuong));
    }
}