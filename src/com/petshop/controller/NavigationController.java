package com.petshop.controller;

import com.petshop.model.User;
import com.petshop.util.RoleMenuConfig;
import com.petshop.util.SessionManager;
import com.petshop.view.InvoiceCreationView;
import com.petshop.view.ReportView;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class NavigationController {
    private final Stage stage;
    private final Runnable showDashboard;

    public NavigationController(Stage stage, Runnable showDashboard) {
        this.stage = stage;
        this.showDashboard = showDashboard;
    }

    public boolean navigate(RoleMenuConfig.Screen screen, String label) {
        if (!authorize(screen, label)) {
            return false;
        }

        if (screen == RoleMenuConfig.Screen.OVERVIEW) {
            showDashboard.run();
        } else if (screen == RoleMenuConfig.Screen.INVOICE_CREATE) {
            openInvoice(null, label);
        } else if (isReportScreen(screen)) {
            Scene scene = new Scene(new ReportView(label, showDashboard), 1220, 800);
            scene.getStylesheets().add(
                    getClass().getResource("/com/petshop/view/styles.css").toExternalForm());
            stage.setTitle("Happy Tails | Báo cáo " + label);
            stage.setScene(scene);
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(label);
            alert.setHeaderText(null);
            alert.setContentText("Phân hệ [" + label + "] đang được kết nối với cơ sở dữ liệu.");
            alert.showAndWait();
        }
        return true;
    }

    public boolean navigateToAppointmentInvoice(String appointmentId) {
        if (!authorize(RoleMenuConfig.Screen.INVOICE_CREATE, "Lập hóa đơn")) {
            return false;
        }
        openInvoice(appointmentId, "Lập hóa đơn");
        return true;
    }

    private boolean authorize(RoleMenuConfig.Screen screen, String label) {
        User user = SessionManager.getCurrentUser();
        if (user != null && RoleMenuConfig.canAccess(user.getRole(), screen)) {
            return true;
        }
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Không đủ quyền");
        alert.setHeaderText("Truy cập bị từ chối");
        alert.setContentText(user == null
                ? "Vui lòng đăng nhập trước khi mở màn hình này."
                : "Vai trò \"" + user.getRole().getDisplayName()
                        + "\" không có quyền truy cập: " + label + ".");
        alert.showAndWait();
        return false;
    }

    private void openInvoice(String appointmentId, String title) {
        Scene scene = new Scene(
                new InvoiceCreationView(showDashboard, appointmentId), 1220, 800);
        scene.getStylesheets().add(
                getClass().getResource("/com/petshop/view/styles.css").toExternalForm());
        stage.setTitle("Happy Tails | " + title);
        stage.setScene(scene);
    }

    private boolean isReportScreen(RoleMenuConfig.Screen screen) {
        return screen == RoleMenuConfig.Screen.REPORT_REVENUE
                || screen == RoleMenuConfig.Screen.REPORT_INVENTORY
                || screen == RoleMenuConfig.Screen.REPORT_BEST_SELLERS;
    }
}
