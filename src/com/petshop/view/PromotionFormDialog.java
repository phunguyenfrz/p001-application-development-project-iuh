package com.petshop.view;

import com.petshop.controller.AuditController;
import com.petshop.controller.PromotionController;
import com.petshop.model.Promotion;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

final class PromotionFormDialog {
    private PromotionFormDialog() {
    }

    static void show(PromotionController controller, AuditController audit, Promotion existing) {
        boolean adding = existing == null;
        String id = adding ? "PROM-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + String.format("%06d", java.util.concurrent.ThreadLocalRandom.current()
                        .nextInt(1_000_000))
                : existing.id();
        TextField code = new TextField(adding ? "Tự động sinh · " + id : id);
        code.setEditable(false);
        TextField name = new TextField(adding ? "" : existing.name());
        TextField discount = new TextField(adding ? "" : existing.discountPercent().toPlainString());
        DatePicker start = new DatePicker(adding ? LocalDate.now() : existing.startsOn());
        DatePicker end = new DatePicker(adding ? LocalDate.now() : existing.endsOn());
        CheckBox active = new CheckBox("Đang áp dụng");
        active.setSelected(adding || existing.active());
        GridPane fields = new GridPane();
        fields.setHgap(10);
        fields.setVgap(8);
        addRow(fields, 0, "Mã chương trình", code);
        addRow(fields, 1, "Tên chương trình", name);
        addRow(fields, 2, "Phần trăm giảm", discount);
        addRow(fields, 3, "Ngày bắt đầu", start);
        addRow(fields, 4, "Ngày kết thúc", end);
        fields.add(active, 1, 5);
        Map<String, Node> controls = Map.of(
                "name", name, "discount", discount, "start", start, "end", end);
        Map<String, Label> errors = new LinkedHashMap<>();
        errors.put("name", new Label());
        errors.put("discount", new Label());
        errors.put("start", new Label());
        errors.put("end", new Label());
        VBox content = new VBox(3, fields, errors.get("name"), errors.get("discount"),
                errors.get("start"), errors.get("end"));
        new CrudFormDialog<>(
                adding ? "Thêm khuyến mãi" : "Cập nhật khuyến mãi",
                content, controls, errors,
                () -> new Promotion(id, name.getText(), parseDiscount(discount.getText()),
                        start.getValue(), end.getValue(), active.isSelected()),
                PromotionFormDialog::validate,
                () -> !name.getText().equals(adding ? "" : existing.name())
                        || !discount.getText().equals(adding ? "" : existing.discountPercent().toPlainString())
                        || !java.util.Objects.equals(start.getValue(),
                                adding ? LocalDate.now() : existing.startsOn())
                        || !java.util.Objects.equals(end.getValue(),
                                adding ? LocalDate.now() : existing.endsOn())
                        || active.isSelected() != (adding || existing.active()),
                value -> {
                    if (adding) {
                        controller.add(value);
                    } else {
                        controller.update(value);
                    }
                    audit.log(adding ? "Thêm" : "Sửa", "KhuyenMai", value.id(), null);
                }).show();
    }

    private static java.util.List<CrudFormDialog.FieldError> validate(Promotion value) {
        java.util.List<CrudFormDialog.FieldError> errors = new java.util.ArrayList<>();
        if (value.name() == null || value.name().isBlank() || value.name().length() > 150) {
            errors.add(new CrudFormDialog.FieldError("name",
                    "Tên chương trình là bắt buộc, tối đa 150 ký tự."));
        }
        if (value.discountPercent() == null || value.discountPercent().signum() <= 0
                || value.discountPercent().compareTo(BigDecimal.valueOf(100)) > 0
                || value.discountPercent().scale() > 2) {
            errors.add(new CrudFormDialog.FieldError("discount", "Mức giảm phải từ trên 0 đến 100%."));
        }
        if (value.startsOn() == null) {
            errors.add(new CrudFormDialog.FieldError("start", "Vui lòng chọn ngày bắt đầu."));
        }
        if (value.endsOn() == null || value.startsOn() == null
                || value.endsOn().isBefore(value.startsOn())) {
            errors.add(new CrudFormDialog.FieldError("end", "Ngày kết thúc phải sau ngày bắt đầu."));
        }
        return errors;
    }

    private static BigDecimal parseDiscount(String value) {
        try {
            return new BigDecimal(value.trim());
        } catch (RuntimeException exception) {
            return BigDecimal.valueOf(-1);
        }
    }

    private static void addRow(GridPane grid, int row, String label, Node field) {
        grid.add(new Label(label), 0, row);
        grid.add(field, 1, row);
    }
}
