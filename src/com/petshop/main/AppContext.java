package com.petshop.main;

import com.petshop.controller.AuditControllerImpl;
import com.petshop.controller.DashboardControllerImpl;
import com.petshop.controller.GlobalSearchControllerImpl;
import com.petshop.controller.LoginController;
import com.petshop.controller.InvoiceControllerImpl;
import com.petshop.controller.ReportControllerImpl;
import com.petshop.controller.PasswordControllerImpl;
import com.petshop.controller.SessionControllerImpl;
import com.petshop.controller.SupplierController;
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
import com.petshop.dao.DashboardDao;
import com.petshop.dao.EmployeeDao;
import com.petshop.dao.InventoryDao;
import com.petshop.dao.ProductDao;
import com.petshop.dao.AppointmentDao;
import com.petshop.dao.CustomerDao;
import com.petshop.dao.PetDao;
import com.petshop.dao.CareServiceDao;
import com.petshop.dao.PromotionDao;
import com.petshop.dao.ShiftDao;
import com.petshop.dao.PurchaseOrderDao;
import com.petshop.dao.InvoiceHistoryDao;
import com.petshop.dao.SqlSupplierDao;
import com.petshop.dao.InvoiceDao;
import com.petshop.dao.ReportDao;
import com.petshop.dao.PasswordDao;
import com.petshop.dao.SqlAuditDao;
import com.petshop.dao.UserDao;
import com.petshop.search.AppointmentSearchProvider;
import com.petshop.search.CustomerSearchProvider;
import com.petshop.search.GlobalSearchService;
import com.petshop.search.PetSearchProvider;
import com.petshop.search.ProductSearchProvider;
import com.petshop.search.ServiceSearchProvider;
import com.petshop.util.AuditService;
import javafx.stage.Stage;

import java.util.List;

public final class AppContext {
    private final SceneManager sceneManager;

    public AppContext(Stage stage) {
        AuditService auditService = new AuditService(new SqlAuditDao());
        AuditControllerImpl auditController = new AuditControllerImpl(auditService);
        ApplicationControllers controllers = new ApplicationControllers(
                new LoginController(new UserDao(), auditService),
                new DashboardControllerImpl(new DashboardDao()),
                new GlobalSearchControllerImpl(new GlobalSearchService(List.of(
                        new CustomerSearchProvider(),
                        new PetSearchProvider(),
                        new ProductSearchProvider(),
                        new ServiceSearchProvider(),
                        new AppointmentSearchProvider()))),
                new PasswordControllerImpl(new PasswordDao(), auditService),
                new SessionControllerImpl(auditService),
                new SupplierController(new SqlSupplierDao()),
                auditController,
                new InvoiceControllerImpl(new InvoiceDao(), auditController),
                new ReportControllerImpl(new ReportDao()),
                new EmployeeController(new EmployeeDao()),
                new InventoryController(new InventoryDao()),
                new ProductController(new ProductDao()),
                new AppointmentController(new AppointmentDao(), auditController),
                new CustomerController(new CustomerDao()),
                new PetController(new PetDao()),
                new CareServiceController(new CareServiceDao()),
                new PromotionController(new PromotionDao()),
                new ShiftController(new ShiftDao()),
                new PurchaseOrderController(new PurchaseOrderDao(), auditController),
                new InvoiceHistoryController(new InvoiceHistoryDao()));
        sceneManager = new SceneManager(stage, controllers);
    }

    public SceneManager sceneManager() {
        return sceneManager;
    }
}
