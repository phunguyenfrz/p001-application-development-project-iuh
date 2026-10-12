package com.petshop.view;

import com.petshop.controller.PasswordChangedAuditException;
import com.petshop.controller.PasswordController;
import com.petshop.model.User;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.concurrent.Task;

public final class ChangePasswordDialog {
    private final PasswordController controller;
    private final User user;

    public ChangePasswordDialog(PasswordController controller, User user) {
        this.controller = controller;
        this.user = user;
    }

    public void show() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Đổi mật khẩu");
        ButtonType saveType = new ButtonType("Đổi mật khẩu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);
        PasswordField current = new PasswordField();
        current.setPromptText("Mật khẩu hiện tại");
        PasswordField next = new PasswordField();
        next.setPromptText("Mật khẩu mới (tối thiểu 8 ký tự, gồm chữ và số)");
        PasswordField confirm = new PasswordField();
        confirm.setPromptText("Xác nhận mật khẩu mới");
        Label error = new Label();
        error.getStyleClass().add("error-label");
        VBox form = new VBox(10, current, next, confirm, error);
        form.setPadding(new Insets(8));
        dialog.getDialogPane().setContent(form);
        Button save = (Button) dialog.getDialogPane().lookupButton(saveType);
        save.addEventFilter(ActionEvent.ACTION, event -> {
            if (current.getText().isEmpty()) {
                reject(event, error, "Hãy nhập mật khẩu hiện tại.");
            } else if (!controller.isStrongPassword(next.getText())) {
                reject(event, error, "Mật khẩu mới cần từ 8 ký tự, có ít nhất một chữ và một số.");
            } else if (!next.getText().equals(confirm.getText())) {
                reject(event, error, "Mật khẩu xác nhận không khớp.");
            } else {
                savePassword(dialog, event, error, current, next);
            }
        });
        if (dialog.showAndWait().filter(saveType::equals).isPresent()) {
            information("Mật khẩu đã được cập nhật.");
        }
    }

    private void savePassword(
            Dialog<ButtonType> dialog, ActionEvent event, Label error,
            PasswordField current, PasswordField next) {
        event.consume();
        Button save = (Button) dialog.getDialogPane().lookupButton(
                dialog.getDialogPane().getButtonTypes().get(0));
        save.setDisable(true);
        error.setText("Đang cập nhật mật khẩu...");
        String oldPassword = current.getText();
        String newPassword = next.getText();
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                return controller.changePassword(
                        user.getMaTaiKhoan(), oldPassword, newPassword);
            }
        };
        task.setOnSucceeded(done -> {
            if (!task.getValue()) {
                save.setDisable(false);
                error.setText("Mật khẩu hiện tại không chính xác.");
                return;
            }
            dialog.setResult(dialog.getDialogPane().getButtonTypes().get(0));
            dialog.close();
        });
        task.setOnFailed(done -> {
            Throwable failure = task.getException();
            if (failure instanceof PasswordChangedAuditException auditFailure) {
                dialog.setResult(dialog.getDialogPane().getButtonTypes().get(0));
                dialog.close();
                error(auditFailure.getMessage());
                return;
            }
            save.setDisable(false);
            error.setText(failure == null
                    ? "Không thể cập nhật mật khẩu." : failure.getMessage());
        });
        Thread worker = new Thread(task, "password-change");
        worker.setDaemon(true);
        worker.start();
    }

    private void reject(ActionEvent event, Label label, String message) {
        event.consume();
        label.setText(message);
    }

    private void information(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.setTitle("Thành công");
        alert.setHeaderText(null);
        alert.show();
    }

    private void error(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setTitle("Lỗi ghi nhật ký");
        alert.setHeaderText("Mật khẩu đã được đổi nhưng không ghi được nhật ký.");
        alert.show();
    }
}
