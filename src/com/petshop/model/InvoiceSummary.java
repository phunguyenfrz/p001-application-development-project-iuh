package com.petshop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record InvoiceSummary(
        String id,
        LocalDateTime createdAt,
        String employeeName,
        String customerName,
        String paymentMethod,
        String paymentStatus,
        BigDecimal total,
        List<Line> lines) {
    public InvoiceSummary {
        lines = List.copyOf(lines);
    }

    public record Line(String category, String description, int quantity,
                       BigDecimal unitPrice, BigDecimal amount) {
    }
}
