package com.petshop.controller;

import com.petshop.model.InvoiceData.AppointmentOption;
import com.petshop.model.InvoiceData.CustomerOption;
import com.petshop.model.InvoiceData.InvoiceDraft;
import com.petshop.model.InvoiceData.InvoiceReceipt;
import com.petshop.model.InvoiceData.ProductOption;

import java.nio.file.Path;
import java.util.List;

public interface InvoiceController {
    List<CustomerOption> findCustomers();

    List<ProductOption> searchProducts(String keyword);

    List<AppointmentOption> findCompletedAppointments();

    InvoiceReceipt createInvoice(InvoiceDraft draft);

    void exportPdf(InvoiceReceipt receipt, Path target);
}
