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
        dialog.showAndWait();
    }

    private void savePassword(
            Dialog<ButtonType> dialog, ActionEvent event, Label error,
            PasswordField current, PasswordField next) {
        try {
            if (!controller.changePassword(user.getMaTaiKhoan(), current.getText(), next.getText())) {
                reject(event, error, "Mật khẩu hiện tại không chính xác.");
                return;
            }
            information("Mật khẩu đã được cập nhật.");
        } catch (PasswordChangedAuditException exception) {
            error(exception.getMessage());
        } catch (IllegalStateException exception) {
            reject(event, error, exception.getMessage());
        }
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
