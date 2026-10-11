package com.petshop.view;

import com.petshop.controller.DashboardController;
import com.petshop.model.User;
import javafx.scene.control.Alert;

public final class NotificationPopup {
    private final DashboardController controller;
    private final User user;

    public NotificationPopup(DashboardController controller, User user) {
        this.controller = controller;
        this.user = user;
    }

    public void show() {
        try {
            var messages = controller.loadNotifications(user);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thông báo");
            alert.setHeaderText("Thông tin trong ngày");
            alert.setContentText(messages.isEmpty()
                    ? "Không có thông báo mới." : String.join("\n", messages));
            alert.showAndWait();
        } catch (IllegalStateException exception) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi thông báo");
            alert.setHeaderText("Không thể tải thông báo.");
            alert.setContentText(exception.getMessage());
            alert.show();
        }
    }
}
