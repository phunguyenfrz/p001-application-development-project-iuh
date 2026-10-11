package com.petshop.view;

import com.petshop.controller.AuditController;
import com.petshop.controller.ProductController;
import com.petshop.model.Product;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

final class ProductFormDialog {
    private final ProductController controller;
    private final AuditController auditController;
    private final Product existing;
    private final Dialog<Void> dialog = new Dialog<>();
    private final TextField code = new TextField();
    private final TextField supplierId = new TextField();
    private final TextField name = new TextField();
    private final TextField barcode = new TextField();
    private final TextField price = new TextField();
    private final TextField quantity = new TextField();
    private final TextField unit = new TextField();
    private final TextField minimum = new TextField();
    private final CheckBox active = new CheckBox("Đang kinh doanh");
    private final Label formError = new Label();
    private final Map<String, Node> controls = new LinkedHashMap<>();
    private final Map<String, Label> errors = new LinkedHashMap<>();
    private boolean saving;
    private boolean saved;
    private String initialState;

    ProductFormDialog(
            ProductController controller,
            AuditController auditController,
            Product existing) {
        this.controller = Objects.requireNonNull(controller);
        this.auditController = Objects.requireNonNull(auditController);
        this.existing = existing;
        code.setEditable(false);
        code.setText(existing == null ? "Tự động sinh" : existing.getMaSanPham());
        if (existing != null) {
            supplierId.setText(existing.getMaNhaCungCap() == null
                    ? "" : existing.getMaNhaCungCap());
            name.setText(existing.getTenSanPham());
            barcode.setText(existing.getMaVach() == null ? "" : existing.getMaVach());
            price.setText(existing.getGiaBanDecimal().toPlainString());
            quantity.setText(String.valueOf(existing.getSoLuongTon()));
            quantity.setEditable(false);
            unit.setText(existing.getDonViTinh());
            minimum.setText(String.valueOf(existing.getNguongTonToiThieu()));
            active.setSelected(existing.isTrangThai());
        } else {
            quantity.setText("0");
            minimum.setText("10");
            active.setSelected(true);
        }
        quantity.setEditable(false);
        supplierId.setPromptText("Mã nhà cung cấp (không bắt buộc)");
        name.setPromptText("Tên sản phẩm");
        barcode.setPromptText("Mã vạch");
        price.setPromptText("Giá bán");
        unit.setPromptText("Đơn vị tính");
        initialState = state();
        show();
    }

    private void show() {
        dialog.setTitle(existing == null ? "Thêm sản phẩm" : "Sửa sản phẩm");
        dialog.setHeaderText(null);
        ButtonType save = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(save, cancel);
        formError.getStyleClass().add("feedback-error");
        formError.setWrapText(true);
        VBox content = new VBox(8,
                field("Mã sản phẩm", "code", code),
                field("Mã nhà cung cấp", "supplier", supplierId),
                field("Tên sản phẩm", "name", name),
                field("Mã vạch", "barcode", barcode),
                field("Giá bán", "price", price),
                field("Tồn kho (chỉ đọc; cập nhật qua nhập hàng)", "quantity", quantity),
                field("Đơn vị tính", "unit", unit),
                field("Ngưỡng tồn tối thiểu", "minimum", minimum),
                active, formError);
        content.setPadding(new Insets(4));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setMinWidth(440);
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

    private Node field(String label, String key, Node control) {
        Label error = new Label();
        error.getStyleClass().add("field-error");
        errors.put(key, error);
        controls.put(key, control);
        return new VBox(4, new Label(label), control, error);
    }

    private void save(ActionEvent event) {
        clearErrors();
        if (!validateInputs()) {
            event.consume();
            return;
        }
        Product product = extract();
        saving = true;
        dialog.getDialogPane().getButtonTypes().forEach(type ->
                dialog.getDialogPane().lookupButton(type).setDisable(true));
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                if (existing == null) {
                    controller.add(product);
                } else {
                    product.setMaSanPham(existing.getMaSanPham());
                    controller.update(product);
                }
                try {
                    auditController.log(existing == null ? "Thêm" : "Sửa",
                            "SanPham", product.getMaSanPham(), null);
                    return true;
                } catch (RuntimeException exception) {
                    return false;
                }
            }
        };
        task.setOnSucceeded(done -> {
            saved = true;
            saving = false;
            dialog.close();
            if (!task.getValue()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Không ghi được nhật ký");
                alert.setHeaderText("Sản phẩm đã được lưu.");
                alert.setContentText("Không thể ghi nhật ký thao tác.");
                alert.show();
            }
        });
        task.setOnFailed(failed -> {
            saving = false;
            dialog.getDialogPane().getButtonTypes().forEach(type ->
                    dialog.getDialogPane().lookupButton(type).setDisable(false));
            formError.setText(task.getException() == null
                    ? "Không thể lưu sản phẩm." : task.getException().getMessage());
        });
        Thread worker = new Thread(task, "product-save");
        worker.setDaemon(true);
        worker.start();
        event.consume();
    }

    private Product extract() {
        BigDecimal salePrice = new BigDecimal(price.getText().trim().replace(',', '.'));
        int currentQuantity = Integer.parseInt(quantity.getText().trim());
        int minimumQuantity = Integer.parseInt(minimum.getText().trim());
        return new Product(existing == null ? "" : existing.getMaSanPham(),
                supplierId.getText().trim(), name.getText().trim(), barcode.getText().trim(),
                salePrice, currentQuantity, unit.getText().trim(), active.isSelected(),
                minimumQuantity);
    }

    private boolean validateInputs() {
        boolean valid = true;
        if (name.getText().isBlank()) {
            error("name", "Tên sản phẩm không được để trống.");
            valid = false;
        }
        if (unit.getText().isBlank()) {
            error("unit", "Đơn vị tính không được để trống.");
            valid = false;
        }
        try {
            BigDecimal value = new BigDecimal(price.getText().trim().replace(',', '.'));
            if (value.signum() < 0 || value.scale() > 2 || value.precision() > 18) {
                error("price", "Giá bán phải từ 0 trở lên, tối đa 2 chữ số thập phân.");
                valid = false;
            }
        } catch (NumberFormatException exception) {
            error("price", "Hãy nhập giá bán hợp lệ.");
            valid = false;
        }
        valid &= validateNonNegativeInteger(quantity, "quantity",
                "Hãy nhập tồn kho ban đầu là số nguyên không âm.");
        valid &= validateNonNegativeInteger(minimum, "minimum",
                "Hãy nhập ngưỡng tồn là số nguyên không âm.");
        if (barcode.getText().length() > 50) {
            error("barcode", "Mã vạch không được vượt quá 50 ký tự.");
            valid = false;
        }
        if (supplierId.getText().length() > 30) {
            error("supplier", "Mã nhà cung cấp không được vượt quá 30 ký tự.");
            valid = false;
        }
        return valid;
    }

    private boolean validateNonNegativeInteger(
            TextField field, String key, String message) {
        try {
            return Integer.parseInt(field.getText().trim()) >= 0
                    || showError(key, message);
        } catch (NumberFormatException exception) {
            return showError(key, message);
        }
    }

    private boolean showError(String key, String message) {
        error(key, message);
        return false;
    }

    private boolean isDirty() {
        return existing == null || !state().equals(initialState);
    }

    private String state() {
        return String.join("|", supplierId.getText(), name.getText(), barcode.getText(),
                price.getText(), quantity.getText(), unit.getText(), minimum.getText(),
                Boolean.toString(active.isSelected()));
    }

    private void error(String key, String message) {
        Node control = controls.get(key);
        if (!control.getStyleClass().contains("field-invalid")) {
            control.getStyleClass().add("field-invalid");
        }
        errors.get(key).setText(message);
    }

    private void clearErrors() {
        formError.setText("");
        controls.values().forEach(control ->
                control.getStyleClass().remove("field-invalid"));
        errors.values().forEach(label -> label.setText(""));
    }
}
