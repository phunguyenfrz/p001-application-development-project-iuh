package com.petshop.view;

import com.petshop.controller.AuditController;
import com.petshop.controller.EmployeeController;
import com.petshop.model.Employee;
import com.petshop.model.Role;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

final class EmployeeFormDialog {
    private final EmployeeController controller;
    private final AuditController auditController;
    private final Employee existing;
    private final Runnable savedCallback;
    private final Dialog<Void> dialog = new Dialog<>();
    private final TextField code = new TextField();
    private final TextField name = new TextField();
    private final TextField phone = new TextField();
    private final TextField username = new TextField();
    private final PasswordField password = new PasswordField();
    private final ComboBox<Role> role = new ComboBox<>();
    private final DatePicker birthDate = new DatePicker();
    private final ComboBox<String> gender = new ComboBox<>();
    private final CheckBox working = new CheckBox("Đang làm");
    private final CheckBox accountActive = new CheckBox("Tài khoản được phép đăng nhập");
    private final Label formError = new Label();
    private final Map<String, Node> controls = new LinkedHashMap<>();
    private final Map<String, Label> errors = new LinkedHashMap<>();
    private final String initialName;
    private final String initialPhone;
    private final String initialUsername;
    private final Role initialRole;
    private final LocalDate initialBirthDate;
    private final String initialGender;
    private final boolean initialWorking;
    private final boolean initialAccountActive;
    private boolean saving;
    private boolean saved;

    EmployeeFormDialog(
            EmployeeController controller,
            AuditController auditController,
            Employee existing,
            Runnable savedCallback) {
        this.controller = Objects.requireNonNull(controller);
        this.auditController = Objects.requireNonNull(auditController);
        this.existing = existing;
        this.savedCallback = savedCallback;
        code.setEditable(false);
        code.setText(existing == null ? "Tự động sinh" : existing.id());
        name.setPromptText("Họ tên");
        phone.setPromptText("10 chữ số");
        username.setPromptText("Tên đăng nhập");
        password.setPromptText(existing == null
                ? "Mật khẩu ban đầu (ít nhất 8 ký tự)" : "Để trống nếu không đổi mật khẩu");
        role.getItems().setAll(Role.values());
        role.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Role value) {
                return value == null ? "" : value.getDisplayName();
            }

            @Override
            public Role fromString(String value) {
                return Role.fromDbValue(value);
            }
        });
        gender.getItems().setAll("Nam", "Nữ");
        gender.setValue("Nam");
        birthDate.setValue(LocalDate.now().minusYears(18));
        working.setSelected(true);
        accountActive.setSelected(true);
        if (existing != null) {
            name.setText(existing.fullName());
            phone.setText(existing.phone());
            username.setText(existing.username() == null ? "" : existing.username());
            role.setValue(existing.role());
            birthDate.setValue(existing.birthDate());
            gender.setValue(existing.male() ? "Nam" : "Nữ");
            working.setSelected(existing.working());
            accountActive.setSelected(existing.accountActive());
        }
        initialName = name.getText();
        initialPhone = phone.getText();
        initialUsername = username.getText();
        initialRole = role.getValue();
        initialBirthDate = birthDate.getValue();
        initialGender = gender.getValue();
        initialWorking = working.isSelected();
        initialAccountActive = accountActive.isSelected();
        show();
    }

    private void show() {
        dialog.setTitle(existing == null ? "Thêm nhân viên" : "Sửa nhân viên");
        dialog.setHeaderText(null);
        ButtonType save = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(save, cancel);
        formError.getStyleClass().add("feedback-error");
        formError.setWrapText(true);
        VBox content = new VBox(8,
                field("Mã nhân viên", "code", code),
                field("Họ và tên", "name", name),
                field("Số điện thoại", "phone", phone),
                field("Tên đăng nhập", "username", username),
                field("Mật khẩu", "password", password),
                field("Vai trò", "role", role),
                field("Ngày sinh", "birthDate", birthDate),
                field("Giới tính", "gender", gender),
                working, accountActive, formError);
        content.setPadding(new Insets(4));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setMinWidth(460);
        dialog.getDialogPane().lookupButton(save).addEventFilter(
                ActionEvent.ACTION, event -> save(event));
        dialog.getDialogPane().lookupButton(cancel).addEventFilter(
                ActionEvent.ACTION, event -> {
                    if (isDirty() && !ConfirmDialog.confirmCancelForm()) {
                        event.consume();
                    }
                });
        dialog.setOnCloseRequest(event -> {
            if (!saved && (saving || isDirty() && !ConfirmDialog.confirmCancelForm())) {
                event.consume();
            }
        });
        dialog.showAndWait();
    }

    private Node field(String title, String key, Node control) {
        Label error = new Label();
        error.getStyleClass().add("field-error");
        errors.put(key, error);
        controls.put(key, control);
        return new VBox(4, new Label(title), control, error);
    }

    private void save(ActionEvent event) {
        clearErrors();
        if (!validate()) {
            event.consume();
            return;
        }
        Employee employee = new Employee(
                existing == null ? "" : existing.id(),
                name.getText().trim(), phone.getText().trim(), role.getValue(),
                birthDate.getValue(), "Nam".equals(gender.getValue()),
                working.isSelected(), username.getText().trim(), accountActive.isSelected());
        String newPassword = password.getText();
        saving = true;
        dialog.getDialogPane().getButtonTypes().forEach(type ->
                dialog.getDialogPane().lookupButton(type).setDisable(true));
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                String employeeId;
                if (existing == null) {
                    employeeId = controller.add(employee, newPassword);
                } else {
                    controller.update(employee, newPassword);
                    employeeId = employee.id();
                }
                try {
                    auditController.log(existing == null ? "Thêm" : "Sửa",
                            "NhanVien", employeeId, "Cập nhật hồ sơ và tài khoản nhân viên");
                    return true;
                } catch (RuntimeException exception) {
                    return false;
                }
            }
        };
        task.setOnSucceeded(done -> {
            saving = false;
            saved = true;
            dialog.close();
            savedCallback.run();
            if (!task.getValue()) {
                warning("Đã lưu hồ sơ nhưng không ghi được nhật ký thao tác.");
            }
        });
        task.setOnFailed(failed -> {
            saving = false;
            dialog.getDialogPane().getButtonTypes().forEach(type ->
                    dialog.getDialogPane().lookupButton(type).setDisable(false));
            formError.setText(task.getException() == null
                    ? "Không thể lưu thông tin nhân viên."
                    : task.getException().getMessage());
        });
        Thread worker = new Thread(task, "employee-save");
        worker.setDaemon(true);
        worker.start();
        event.consume();
    }

    private boolean validate() {
        boolean valid = true;
        if (name.getText().isBlank()) {
            error("name", "Họ tên không được để trống.");
            valid = false;
        }
        if (!phone.getText().trim().matches("\\d{10}")) {
            error("phone", "Số điện thoại phải gồm đúng 10 chữ số.");
            valid = false;
        }
        if (username.getText().isBlank()) {
            error("username", "Tên đăng nhập không được để trống.");
            valid = false;
        }
        if (role.getValue() == null) {
            error("role", "Hãy chọn vai trò.");
            valid = false;
        }
        if (birthDate.getValue() == null || !birthDate.getValue().isBefore(LocalDate.now())) {
            error("birthDate", "Ngày sinh phải trước ngày hiện tại.");
            valid = false;
        }
        if ((existing == null || !password.getText().isBlank())
                && password.getText().length() < 8) {
            error("password", "Mật khẩu phải có ít nhất 8 ký tự.");
            valid = false;
        }
        if (accountActive.isSelected() && !working.isSelected()) {
            error("username", "Nhân viên đã nghỉ việc không thể có tài khoản hoạt động.");
            valid = false;
        }
        return valid;
    }

    private boolean isDirty() {
        return existing == null || !name.getText().equals(initialName)
                || !phone.getText().equals(initialPhone)
                || !username.getText().equals(initialUsername)
                || role.getValue() != initialRole
                || !Objects.equals(birthDate.getValue(), initialBirthDate)
                || !gender.getValue().equals(initialGender)
                || working.isSelected() != initialWorking
                || accountActive.isSelected() != initialAccountActive
                || !password.getText().isBlank();
    }

    private void error(String key, String message) {
        Node control = controls.get(key);
        if (control != null && !control.getStyleClass().contains("field-invalid")) {
            control.getStyleClass().add("field-invalid");
        }
        errors.get(key).setText(message);
    }

    private void clearErrors() {
        formError.setText("");
        controls.values().forEach(node -> node.getStyleClass().remove("field-invalid"));
        errors.values().forEach(label -> label.setText(""));
    }

    private void warning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Không ghi được nhật ký");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}
