package com.petshop.dao;

import com.petshop.model.InvoiceData.AppointmentOption;
import com.petshop.model.InvoiceData.CustomerOption;
import com.petshop.model.InvoiceData.InvoiceDraft;
import com.petshop.model.InvoiceData.InvoiceReceipt;
import com.petshop.model.InvoiceData.ProductOption;

import java.util.List;

public interface InvoiceRepository {
    List<CustomerOption> findCustomers();

    List<ProductOption> searchProducts(String keyword);

    List<AppointmentOption> findCompletedAppointments();

    InvoiceReceipt create(InvoiceDraft draft, String employeeId);
}
