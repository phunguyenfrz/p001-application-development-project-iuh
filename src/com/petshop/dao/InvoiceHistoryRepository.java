package com.petshop.dao;

import com.petshop.model.InvoiceSummary;

import java.time.LocalDate;
import java.util.List;

public interface InvoiceHistoryRepository {
    List<InvoiceSummary> search(String keyword, LocalDate from, LocalDate through);
}
