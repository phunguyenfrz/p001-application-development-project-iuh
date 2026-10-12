package com.petshop.view;

import com.petshop.controller.AuditController;
import com.petshop.controller.CareServiceController;
import com.petshop.model.CareServiceData;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class CareServiceFormDialog {
    private CareServiceFormDialog() {
    }

    static void show(
            CareServiceController controller, AuditController audit, CareServiceData existing) {
        TextField id = new TextField(existing == null
                ? CustomerView.newCode("DV") : existing.id());
        id.setEditable(false);
        TextField name = new TextField(existing == null ? "" : existing.name());
        TextField description = new TextField(
                existing == null || existing.description() == null ? "" : existing.description());
        TextField duration = new TextField(existing == null ? "30"
                : String.valueOf(existing.durationMinutes()));
        TextField small = new TextField(existing == null ? "" : text(existing.smallPrice()));
        TextField medium = new TextField(existing == null ? "" : text(existing.mediumPrice()));
        TextField large = new TextField(existing == null ? "" : text(existing.largePrice()));
        TextField effectiveDate = new TextField(existing == null ? LocalDate.now().toString()
                : existing.effectiveDate().toString());
        CheckBox active = new CheckBox("Đang áp dụng");
        active.setSelected(existing == null || existing.active());
        Map<String, Node> controls = new LinkedHashMap<>();
        Map<String, Label> errors = new LinkedHashMap<>();
        VBox content = new VBox(8,
                field("Mã dịch vụ", "id", id, controls, errors),
                field("Tên dịch vụ", "name", name, controls, errors),
                field("Mô tả", "description", description, controls, errors),
                field("Thời lượng (phút)", "duration", duration, controls, errors),
                field("Giá nhỏ (<5 kg)", "small", small, controls, errors),
                field("Giá vừa (5–10 kg)", "medium", medium, controls, errors),
                field("Giá lớn (>10 kg)", "large", large, controls, errors),
                field("Ngày áp dụng giá (yyyy-MM-dd)", "effectiveDate",
                        effectiveDate, controls, errors), active);
        new CrudFormDialog<>(
                existing == null ? "Thêm dịch vụ chăm sóc" : "Sửa dịch vụ chăm sóc",
                content, controls, errors,
                () -> new CareServiceData(id.getText(), name.getText().trim(),
                        description.getText().trim(),
                        Integer.parseInt(duration.getText().trim()),
                        new BigDecimal(small.getText().trim().replace(',', '.')),
                        new BigDecimal(medium.getText().trim().replace(',', '.')),
                        new BigDecimal(large.getText().trim().replace(',', '.')),
                        LocalDate.parse(effectiveDate.getText().trim()), active.isSelected()),
                service -> validate(service),
                () -> existing == null || !name.getText().equals(existing.name())
                        || !description.getText().equals(existing.description())
                        || !duration.getText().equals(String.valueOf(existing.durationMinutes()))
                        || !small.getText().equals(text(existing.smallPrice()))
                        || !medium.getText().equals(text(existing.mediumPrice()))
                        || !large.getText().equals(text(existing.largePrice()))
                        || !effectiveDate.getText().equals(existing.effectiveDate().toString())
                        || active.isSelected() != existing.active(),
                service -> {
                    if (existing == null) {
                        controller.add(service);
                    } else {
                        controller.update(service);
                    }
                    audit.log(existing == null ? "Thêm" : "Sửa",
                            "DichVuChamSoc", service.id(), null);
                }).show();
    }

    private static List<CrudFormDialog.FieldError> validate(CareServiceData service) {
        java.util.ArrayList<CrudFormDialog.FieldError> result = new java.util.ArrayList<>();
        if (service.name().isBlank()) {
            result.add(new CrudFormDialog.FieldError("name", "Tên dịch vụ là bắt buộc."));
        }
        if (service.durationMinutes() <= 0) {
            result.add(new CrudFormDialog.FieldError("duration", "Thời lượng phải lớn hơn 0."));
        }
        validatePrice(result, "small", service.smallPrice());
        validatePrice(result, "medium", service.mediumPrice());
        validatePrice(result, "large", service.largePrice());
        if (service.effectiveDate().isBefore(LocalDate.now())) {
            result.add(new CrudFormDialog.FieldError("effectiveDate",
                    "Ngày áp dụng không được ở quá khứ."));
        }
        return List.copyOf(result);
    }

    private static void validatePrice(
            List<CrudFormDialog.FieldError> errors, String key, BigDecimal price) {
        if (price.signum() < 0 || price.scale() > 2 || price.precision() > 18) {
            errors.add(new CrudFormDialog.FieldError(key,
                    "Giá phải không âm và tối đa 2 chữ số thập phân."));
        }
    }

    private static Node field(String title, String key, TextField control,
                              Map<String, Node> controls, Map<String, Label> errors) {
        Label error = new Label();
        error.getStyleClass().add("field-error");
        controls.put(key, control);
        errors.put(key, error);
        return new VBox(4, new Label(title), control, error);
    }

    private static String text(BigDecimal value) {
        return value == null ? "0" : value.toPlainString();
    }
}
