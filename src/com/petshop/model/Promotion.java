package com.petshop.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Promotion(
        String id,
        String name,
        BigDecimal discountPercent,
        LocalDate startsOn,
        LocalDate endsOn,
        boolean active) {
    public boolean isEffectiveOn(LocalDate date) {
        return active && date != null && !date.isBefore(startsOn) && !date.isAfter(endsOn);
    }
}
