package com.petshop.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CareServiceData(
        String id,
        String name,
        String description,
        int durationMinutes,
        BigDecimal smallPrice,
        BigDecimal mediumPrice,
        BigDecimal largePrice,
        LocalDate effectiveDate,
        boolean active) {
}
