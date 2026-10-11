package com.petshop.main;

import com.petshop.controller.DashboardController;
import com.petshop.controller.GlobalSearchController;
import com.petshop.controller.AuthenticationController;
import com.petshop.controller.PasswordController;
import com.petshop.controller.SessionController;
import com.petshop.controller.SupplierController;
import com.petshop.controller.AuditController;
import com.petshop.controller.InvoiceController;
import com.petshop.controller.ReportController;
import com.petshop.controller.EmployeeController;
import com.petshop.controller.InventoryController;
import com.petshop.controller.ProductController;
import com.petshop.controller.AppointmentController;
import com.petshop.controller.CustomerController;
import com.petshop.controller.PetController;
import com.petshop.controller.CareServiceController;
import com.petshop.controller.PromotionController;
import com.petshop.controller.ShiftController;
import com.petshop.controller.PurchaseOrderController;
import com.petshop.controller.InvoiceHistoryController;

public record ApplicationControllers(
        AuthenticationController login,
        DashboardController dashboard,
        GlobalSearchController globalSearch,
        PasswordController password,
        SessionController session,
        SupplierController suppliers,
        AuditController audit,
        InvoiceController invoice,
        ReportController report,
        EmployeeController employees,
        InventoryController inventory,
        ProductController products,
        AppointmentController appointments,
        CustomerController customers,
        PetController pets,
        CareServiceController careServices,
        PromotionController promotions,
        ShiftController shifts,
        PurchaseOrderController purchaseOrders,
        InvoiceHistoryController invoiceHistory) {
}
