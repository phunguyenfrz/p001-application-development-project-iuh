package com.petshop.main;

import com.petshop.controller.DashboardController;
import com.petshop.controller.GlobalSearchController;
import com.petshop.controller.AuthenticationController;
import com.petshop.controller.PasswordController;
import com.petshop.controller.SessionController;
import com.petshop.controller.SupplierController;
import com.petshop.controller.AuditController;

public record ApplicationControllers(
        AuthenticationController login,
        DashboardController dashboard,
        GlobalSearchController globalSearch,
        PasswordController password,
        SessionController session,
        SupplierController suppliers,
        AuditController audit) {
}
