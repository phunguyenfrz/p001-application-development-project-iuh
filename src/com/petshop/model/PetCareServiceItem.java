package com.petshop.model;

import java.math.BigDecimal;

/**
 * Dịch vụ đã lưu trong chi tiết lịch hẹn.
 * @param tenDichVu Tên dịch vụ.
 * @param canNangThuCung Cân nặng ghi nhận tại lần sử dụng dịch vụ.
 * @param donGia Đơn giá đã lưu tại lần sử dụng dịch vụ.
 */
public record PetCareServiceItem(
        String tenDichVu, BigDecimal canNangThuCung, BigDecimal donGia) {
}
