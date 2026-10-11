package com.petshop.main;

import com.petshop.controller.AuditControllerImpl;
import com.petshop.controller.DashboardControllerImpl;
import com.petshop.controller.GlobalSearchControllerImpl;
import com.petshop.controller.LoginController;
import com.petshop.controller.PasswordControllerImpl;
import com.petshop.controller.SessionControllerImpl;
import com.petshop.controller.SupplierController;
import com.petshop.dao.DashboardDao;
import com.petshop.dao.InMemorySupplierDao;
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
                new SupplierController(new InMemorySupplierDao()),
                new AuditControllerImpl(auditService));
        sceneManager = new SceneManager(stage, controllers);
    }

    public SceneManager sceneManager() {
        return sceneManager;
    }
}
