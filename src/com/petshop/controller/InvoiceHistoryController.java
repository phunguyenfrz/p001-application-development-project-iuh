package com.petshop.controller;

import com.petshop.dao.InvoiceHistoryRepository;
import com.petshop.model.InvoiceSummary;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class InvoiceHistoryController {
    private final InvoiceHistoryRepository repository;

    public InvoiceHistoryController(InvoiceHistoryRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public List<InvoiceSummary> search(String keyword, LocalDate from, LocalDate through) {
        if (from == null || through == null || through.isBefore(from)) {
            throw new IllegalArgumentException("Khoảng ngày tra cứu không hợp lệ.");
        }
        return repository.search(keyword, from, through);
    }
}
