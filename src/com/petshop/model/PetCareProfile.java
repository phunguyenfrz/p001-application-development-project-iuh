package com.petshop.model;

import java.math.BigDecimal;

/**
 * Thông tin chăm sóc hiện tại, tách biệt dữ liệu các lịch hẹn cũ.
 * @param canNang Cân nặng hiện tại.
 * @param diUng Dị ứng hiện tại, có thể chưa ghi nhận.
 * @param dangTheoDoi Hồ sơ có đang được theo dõi hay không.
 */
public record PetCareProfile(
        BigDecimal canNang,
        String diUng, boolean dangTheoDoi) {
}
