package com.petshop.view;

import com.petshop.controller.AuditController;
import com.petshop.controller.CrudController;
import com.petshop.model.NhaCungCap;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.concurrent.Task;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

final class SupplierFormDialog {
    private final CrudController<NhaCungCap, String> controller;
    private final AuditController auditController;
    private final NhaCungCap existing;
    private final TextField codeField = new TextField();
    private final TextField nameField = new TextField();
    private final TextField phoneField = new TextField();
    private final TextField addressField = new TextField();
    private final CheckBox activeCheckBox = new CheckBox("Đang hoạt động");
    private final Map<String, Node> controls = new LinkedHashMap<>();
    private final Map<String, Label> errorLabels = new LinkedHashMap<>();
    private final String initialName;
    private final String initialPhone;
    private final String initialAddress;
    private final boolean initialActive;

    SupplierFormDialog(
            CrudController<NhaCungCap, String> controller,
            AuditController auditController,
            NhaCungCap existing) {
        this.controller = controller;
        this.auditController = auditController;
        this.existing = existing;
        if (existing == null) {
            codeField.setText(generateCode());

            initialName = "";
            initialPhone = "";
            initialAddress = "";
            initialActive = true;
        } else {
            codeField.setText(existing.getMaNhaCungCap());
            nameField.setText(existing.getTenNhaCungCap());
            phoneField.setText(existing.getSoDienThoai());
            addressField.setText(existing.getDiaChi());
            activeCheckBox.setSelected(!existing.isNgungHoatDong());
            initialName = nameField.getText();
            initialPhone = phoneField.getText();
            initialAddress = addressField.getText();
            initialActive = activeCheckBox.isSelected();
        }
        codeField.setEditable(false);
        codeField.setPromptText(existing == null ? "Tự động sinh" : "");
        nameField.setPromptText("Tên nhà cung cấp");
        phoneField.setPromptText("Tối đa 15 chữ số");
        activeCheckBox.setVisible(false);
        activeCheckBox.setManaged(false);
        addressField.setPromptText("Địa chỉ");
        Node content = formContent();
        CrudFormDialog<NhaCungCap> dialog = new CrudFormDialog<>(
                existing == null ? "Thêm nhà cung cấp" : "Sửa nhà cung cấp",
                content, controls, errorLabels, this::extract, this::validate,
                this::isDirty, this::save);
        dialog.show();
    }

    private Node formContent() {
        VBox content = new VBox(8,
                field("Mã nhà cung cấp", "code", codeField),
                field("Tên nhà cung cấp", "name", nameField),
                field("Số điện thoại", "phone", phoneField),
                field("Địa chỉ", "address", addressField),
                activeCheckBox);
        content.setPadding(new Insets(4));
        return content;
    }

    private Node field(String title, String key, TextField control) {
        Label label = new Label(title);
        Label error = new Label();
        error.getStyleClass().add("field-error");
        errorLabels.put(key, error);
        controls.put(key, control);
        return new VBox(4, label, control, error);
    }

    private NhaCungCap extract() {
        return new NhaCungCap(codeField.getText(), nameField.getText(),
                phoneField.getText(), addressField.getText(),
                !activeCheckBox.isSelected());
    }

    private List<CrudFormDialog.FieldError> validate(NhaCungCap supplier) {
        List<CrudFormDialog.FieldError> errors = new java.util.ArrayList<>();
        if (supplier.getTenNhaCungCap().isBlank()) {
            errors.add(new CrudFormDialog.FieldError("name",
                    "Tên nhà cung cấp không được để trống."));
        }
        if (!supplier.getSoDienThoai().isBlank()
                && !supplier.getSoDienThoai().matches("\\d{1,15}")) {
            errors.add(new CrudFormDialog.FieldError("phone",
                    "Số điện thoại nhà cung cấp phải có tối đa 15 chữ số."));
        }
        return List.copyOf(errors);
    }

    private boolean isDirty() {
        return !nameField.getText().equals(initialName)
                || !phoneField.getText().equals(initialPhone)
                || !addressField.getText().equals(initialAddress)
                || activeCheckBox.isSelected() != initialActive;
    }

    private void save(NhaCungCap supplier) {
        boolean adding = existing == null;
        if (adding) {
            controller.add(supplier);
        } else {
            controller.update(supplier);
        }
        Task<Void> auditTask = new Task<>() {
            @Override
            protected Void call() {
                auditController.log(adding ? "Thêm" : "Sửa", "NhaCungCap",
                        supplier.getMaNhaCungCap(), null);
                return null;
            }
        };
        auditTask.setOnFailed(event -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Không ghi được nhật ký");
            alert.setHeaderText("Dữ liệu đã được lưu.");
            Throwable failure = auditTask.getException();
            alert.setContentText(failure == null
                    ? "Không rõ lỗi." : failure.getMessage());
            alert.show();
        });
        Thread auditWorker = new Thread(auditTask, "supplier-audit-log");
        auditWorker.setDaemon(true);
        auditWorker.start();
    }

    private String generateCode() {
        int suffix = ThreadLocalRandom.current().nextInt(100_000, 1_000_000);
        return "NCC-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + suffix;
    }
}
