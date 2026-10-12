package com.petshop.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class InvoiceData {
    private InvoiceData() {
    }

    public record CustomerOption(String id, String name, String phone) {
        @Override
        public String toString() {
            return name + " · " + phone;
        }
    }

    public record AppointmentOption(
            String id, String customerId, String customerName, String petName,
            LocalDate date, BigDecimal serviceTotal) {
        @Override
        public String toString() {
            return id + " · " + petName + " · " + date;
        }
    }

    public record ProductOption(
            String id, String name, String barcode, BigDecimal price, int stock) {
        @Override
        public String toString() {
            return name + " · " + price + " · Tồn: " + stock;
        }
    }

    public record InvoiceLine(String productId, int quantity) {
    }

    public record InvoiceDraft(
            String customerId,
            String appointmentId,
            String paymentMethod,
            List<InvoiceLine> lines) {
        public InvoiceDraft {
            lines = List.copyOf(lines);
        }
    }

    public record ReceiptLine(
            String description, int quantity, BigDecimal unitPrice, BigDecimal amount) {
    }

    public record InvoiceReceipt(
            String id,
            LocalDateTime createdAt,
            String customerName,
            String paymentMethod,
            BigDecimal total,
            List<ReceiptLine> lines) {
        public InvoiceReceipt {
            lines = List.copyOf(lines);
        }
    }
}
