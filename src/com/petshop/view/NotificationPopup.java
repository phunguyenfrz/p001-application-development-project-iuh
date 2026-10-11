package com.petshop.view;

import com.petshop.controller.DashboardController;
import com.petshop.model.User;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class NotificationPopup {
    private final DashboardController controller;
    private final User user;

    public NotificationPopup(DashboardController controller, User user) {
        this.controller = controller;
        this.user = user;
    }

    public void show() {
        Task<List<String>> task = new Task<>() {
            @Override
            protected List<String> call() {
                return controller.loadNotifications(user);
            }
        };
        task.setOnSucceeded(event -> {
            var messages = task.getValue();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thông báo");
            alert.setHeaderText("Thông tin trong ngày");
            alert.setContentText(messages.isEmpty()
                    ? "Không có thông báo mới." : String.join("\n", messages));
            alert.showAndWait();
        });
        task.setOnFailed(event -> {
            Logger.getLogger(NotificationPopup.class.getName()).log(
                    Level.SEVERE, "Không thể tải thông báo.", task.getException());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi thông báo");
            alert.setHeaderText("Không thể tải thông báo.");
            alert.setContentText("Vui lòng kiểm tra kết nối và thử lại sau.");
            alert.show();
        });
        Thread worker = new Thread(task, "notifications-load");
        worker.setDaemon(true);
        worker.start();
    }
}
