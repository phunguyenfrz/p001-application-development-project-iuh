package com.petshop.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormatsTest {
    @Test
    void formatsVietnameseMoneyAndDateTime() {
        assertTrue(Formats.money(new BigDecimal("1234")).contains("₫"));
        assertEquals("02/01/2025", Formats.date(LocalDate.of(2025, 1, 2)));
        assertEquals("02/01/2025 03:04",
                Formats.dateTime(LocalDateTime.of(2025, 1, 2, 3, 4)));
        assertEquals("02/01/2025 03:04",
                Formats.dateTime(Instant.parse("2025-01-01T20:04:00Z")));
    }
}
