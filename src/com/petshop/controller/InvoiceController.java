package com.petshop.controller;

import com.petshop.model.Invoice;
import com.petshop.service.InvoiceService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controller điều phối giữa Giao diện người dùng (View) và Nghiệp vụ Hóa đơn (Service).
 * Đảm bảo các View không truy vấn CSDL hay xử lý nghiệp vụ trực tiếp.
 */
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController() {
        this(new InvoiceService());
    }

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    public String generateNextInvoiceId() {
        return invoiceService.generateNextInvoiceId();
    }

    public boolean createInvoice(Invoice invoice) {
        return invoiceService.createInvoice(invoice);
    }

    public boolean cancelInvoice(String maHoaDon) {
        return invoiceService.cancelInvoice(maHoaDon);
    }

    public List<Invoice> searchInvoices(String keyword, String statusFilter, LocalDate fromDate, LocalDate toDate) {
        return invoiceService.searchInvoices(keyword, statusFilter, fromDate, toDate);
    }

    public List<Invoice> getAllInvoices() {
        return invoiceService.getAllInvoices();
    }

    public Optional<Invoice> getInvoiceById(String maHoaDon) {
        return invoiceService.getInvoiceById(maHoaDon);
    }

    public List<Invoice> getPaidInvoices(String keyword) {
        return invoiceService.getPaidInvoices(keyword);
    }

    public List<com.petshop.dao.InvoiceDao.AppointmentBillingItem> getUnbilledAppointments(String keyword) {
        return invoiceService.getUnbilledAppointments(keyword);
    }
}

