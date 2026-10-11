package com.petshop.main;

import com.petshop.controller.AuthenticationController;
import com.petshop.controller.NavigationController;
import com.petshop.dao.InMemorySupplierDao;
import com.petshop.model.Role;
import com.petshop.model.User;
import com.petshop.model.Screen;
import com.petshop.navigation.ScreenRegistry;
import com.petshop.view.CareDashboardView;
import com.petshop.view.ContentHost;
import com.petshop.view.DashboardView;
import com.petshop.view.InvoiceCreationView;
import com.petshop.view.MainShell;
import com.petshop.view.EmployeeView;
import com.petshop.view.InventoryView;
import com.petshop.view.ProductView;
import com.petshop.view.AppointmentView;
import com.petshop.view.CustomerView;
import com.petshop.view.PetView;
import com.petshop.view.CareServiceView;
import com.petshop.view.ManagerDashboardView;
import com.petshop.view.ReportView;
import com.petshop.view.SalesDashboardView;
import com.petshop.view.SupplierView;
import com.petshop.view.PromotionView;
import com.petshop.view.ShiftScheduleView;
import com.petshop.view.PurchaseOrderView;
import com.petshop.view.InvoiceHistoryView;
import com.petshop.util.SessionManager;
import com.petshop.view.LoginView;
import javafx.scene.Scene;
import javafx.concurrent.Task;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public final class SceneManager {
    private static final String STYLESHEET = "/com/petshop/view/styles.css";
    private static final double LOGIN_WIDTH = 600;
    private static final double LOGIN_HEIGHT = 680;
    private static final double DASHBOARD_WIDTH = 1220;
    private static final double DASHBOARD_HEIGHT = 800;

    private final Stage stage;
    private final ApplicationControllers controllers;

    public SceneManager(Stage stage, ApplicationControllers controllers) {
        this.stage = Objects.requireNonNull(stage);
        this.controllers = Objects.requireNonNull(controllers);
    }

    public void showLogin() {
        SessionManager.logout();
        stage.setTitle("Happy Tails | Đăng nhập hệ thống");
        stage.setMinWidth(480);
        stage.setMinHeight(620);
        stage.setScene(createScene(new LoginView(controllers.login(), this::showMain),
                LOGIN_WIDTH, LOGIN_HEIGHT));
        stage.centerOnScreen();
        stage.show();
    }

    public void showMain(User user) {
        Objects.requireNonNull(user, "Tài khoản đăng nhập không được để trống.");
        SessionManager.login(user);
        ContentHost contentHost = new ContentHost();
        EnumMap<Role, Supplier<DashboardView>> dashboards = new EnumMap<>(Role.class);
        dashboards.put(Role.QUAN_LY, () -> new ManagerDashboardView(
                user, controllers.dashboard()));
        dashboards.put(Role.BAN_HANG, () -> new SalesDashboardView(
                user, controllers.dashboard()));
        dashboards.put(Role.CHAM_SOC, () -> new CareDashboardView(
                user, controllers.dashboard()));
        ScreenRegistry registry = new ScreenRegistry(
                Map.ofEntries(
                        Map.entry(Screen.EMPLOYEES,
                                () -> new EmployeeView(controllers.employees(), controllers.audit())),
                        Map.entry(Screen.INVENTORY,
                                () -> new InventoryView(controllers.inventory())),
                        Map.entry(Screen.PRODUCTS,
                                () -> new ProductView(controllers.products(), controllers.audit())),
                        Map.entry(Screen.APPOINTMENTS,
                                () -> new AppointmentView(controllers.appointments(),
                                        controllers.customers(), controllers.audit())),
                        Map.entry(Screen.CUSTOMERS,
                                () -> new CustomerView(controllers.customers(), controllers.audit(),
                                        user.getRole() == Role.BAN_HANG)),
                        Map.entry(Screen.PETS,
                                () -> new PetView(controllers.pets(), controllers.audit())),
                        Map.entry(Screen.CARE_SERVICES,
                                () -> new CareServiceView(controllers.careServices(), controllers.audit())),
                        Map.entry(Screen.SUPPLIERS,
                                () -> new SupplierView(controllers.suppliers(), controllers.audit())),
                        Map.entry(Screen.PROMOTIONS,
                                () -> new PromotionView(controllers.promotions(), controllers.audit())),
                        Map.entry(Screen.SHIFTS, () -> new ShiftScheduleView(
                                controllers.shifts(), controllers.audit(), user)),
                        Map.entry(Screen.PURCHASE_ORDERS, () -> new PurchaseOrderView(
                                controllers.purchaseOrders(), user)),
                        Map.entry(Screen.INVOICE_HISTORY,
                                () -> new InvoiceHistoryView(controllers.invoiceHistory())),
                        Map.entry(Screen.INVOICE_CREATE,
                                () -> new InvoiceCreationView(controllers.invoice(), null)),
                        Map.entry(Screen.REVENUE_REPORT,
                                () -> new ReportView.Revenue("Doanh thu", controllers.report())),
                        Map.entry(Screen.INVENTORY_REPORT,
                                () -> new ReportView.Inventory("Tồn kho", controllers.report())),
                        Map.entry(Screen.BEST_SELLERS_REPORT,
                                () -> new ReportView.BestSellers("Bán chạy", controllers.report()))),
                appointmentId -> new InvoiceCreationView(
                        controllers.invoice(), appointmentId),
                dashboards, user.getRole());
        NavigationController navigator = new NavigationController(contentHost, registry, user);
        MainShell shell = new MainShell(user, controllers, navigator, registry,
                contentHost, this::logout);
        navigator.navigate(Screen.OVERVIEW);
        stage.setTitle("Happy Tails | Bảng điều khiển");
        stage.setMinWidth(1100);
        stage.setMinHeight(750);
        stage.setScene(createScene(shell,
                DASHBOARD_WIDTH, DASHBOARD_HEIGHT));
        stage.centerOnScreen();
    }

    private Scene createScene(javafx.scene.Parent root, double width, double height) {
        Scene scene = new Scene(root, width, height);
        var stylesheet = SceneManager.class.getResource(STYLESHEET);
        if (stylesheet == null) {
            throw new IllegalStateException("Không tìm thấy stylesheet giao diện: " + STYLESHEET);
        }
        scene.getStylesheets().add(stylesheet.toExternalForm());
        return scene;
    }

    private void logout() {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            showLogin();
            return;
        }
        stage.getScene().getRoot().setDisable(true);
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                controllers.session().logout(user);
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            stage.getScene().getRoot().setDisable(false);
            showLogin();
        });
        task.setOnFailed(event -> {
            Throwable failure = task.getException();
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Không ghi được nhật ký");
            alert.setHeaderText("Phiên đăng nhập đã kết thúc.");
            alert.setContentText("Không thể lưu nhật ký đăng xuất. Vui lòng báo quản trị viên.");
            alert.show();
            java.util.logging.Logger.getLogger(SceneManager.class.getName()).log(
                    java.util.logging.Level.SEVERE, "Không thể ghi nhật ký đăng xuất.", failure);
            stage.getScene().getRoot().setDisable(false);
            showLogin();
        });
        Thread worker = new Thread(task, "logout-audit");
        worker.setDaemon(true);
        worker.start();
    }
}
