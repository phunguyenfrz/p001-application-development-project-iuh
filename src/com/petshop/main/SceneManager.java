package com.petshop.main;

import com.petshop.controller.AuthenticationController;
import com.petshop.controller.NavigationController;
import com.petshop.controller.ScreenNavigator;
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
import com.petshop.view.ManagerDashboardView;
import com.petshop.view.SalesDashboardView;
import com.petshop.view.SupplierView;
import com.petshop.util.SessionManager;
import com.petshop.view.LoginView;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
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
        AtomicReference<ScreenNavigator> navigatorReference = new AtomicReference<>();
        EnumMap<Role, Supplier<DashboardView>> dashboards = new EnumMap<>(Role.class);
        dashboards.put(Role.QUAN_LY, () -> new ManagerDashboardView(
                user, navigatorReference.get(), controllers.dashboard()));
        dashboards.put(Role.BAN_HANG, () -> new SalesDashboardView(
                user, navigatorReference.get(), controllers.dashboard()));
        dashboards.put(Role.CHAM_SOC, () -> new CareDashboardView(
                user, navigatorReference.get(), controllers.dashboard()));
        ScreenRegistry registry = new ScreenRegistry(
                Map.of(Screen.SUPPLIERS,
                        () -> new SupplierView(controllers.suppliers(), controllers.audit())),
                InvoiceCreationView::new, dashboards, user.getRole());
        NavigationController navigator = new NavigationController(contentHost, registry, user);
        navigatorReference.set(navigator);
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
        if (user != null) {
            try {
                controllers.session().logout(user);
            } catch (IllegalStateException exception) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.WARNING);
                alert.setTitle("Không ghi được nhật ký");
                alert.setHeaderText(null);
                alert.setContentText(exception.getMessage());
                alert.show();
            }
        }
        showLogin();
    }
}
