package com.petshop.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PurchaseOrder(
        String id,
        String supplierId,
        String supplierName,
        String employeeId,
        String employeeName,
        LocalDate createdOn,
        BigDecimal total,
        String status,
        List<Line> lines) {
    public PurchaseOrder {
        lines = List.copyOf(lines);
    }

    public record Line(
            String productId,
            String productName,
            int quantity,
            BigDecimal unitCost) {
        public BigDecimal total() {
            return unitCost.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public record Choice(String id, String label, BigDecimal amount) {
        @Override
        public String toString() {
            return label;
        }
    }
}
