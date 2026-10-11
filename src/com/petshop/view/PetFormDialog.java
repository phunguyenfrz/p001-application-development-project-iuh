package com.petshop.view;

import com.petshop.controller.AuditController;
import com.petshop.controller.PetController;
import com.petshop.model.Pet;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class PetFormDialog {
    private PetFormDialog() {
    }

    static void show(PetController controller, AuditController audit, Pet existing) {
        TextField code = new TextField(existing == null
                ? CustomerView.newCode("TC") : existing.getMaThuCung());
        code.setEditable(false);
        TextField customer = new TextField(existing == null ? "" : existing.getMaKhachHang());
        TextField name = new TextField(existing == null ? "" : existing.getTenThuCung());
        TextField type = new TextField(existing == null ? "" : safe(existing.getLoaiThuCung()));
        TextField breed = new TextField(existing == null ? "" : safe(existing.getGiongLoai()));
        TextField weight = new TextField(existing == null ? "" : String.valueOf(existing.getCanNang()));
        DatePicker birth = new DatePicker(existing == null ? null : existing.getNgaySinh());
        TextField allergies = new TextField(existing == null ? "" : safe(existing.getDiUng()));
        TextField gender = new TextField(existing == null ? "" : safe(existing.getGioiTinh()));
        Map<String, Node> controls = new LinkedHashMap<>();
        Map<String, Label> errors = new LinkedHashMap<>();
        VBox content = new VBox(8,
                field("Mã thú cưng", "code", code, controls, errors),
                field("Mã khách hàng", "customer", customer, controls, errors),
                field("Tên thú cưng", "name", name, controls, errors),
                field("Loài", "type", type, controls, errors),
                field("Giống", "breed", breed, controls, errors),
                field("Cân nặng (kg)", "weight", weight, controls, errors),
                field("Ngày sinh", "birth", birth, controls, errors),
                field("Dị ứng", "allergies", allergies, controls, errors),
                field("Giới tính", "gender", gender, controls, errors));
        new CrudFormDialog<>(
                existing == null ? "Thêm hồ sơ thú cưng" : "Sửa hồ sơ thú cưng",
                content, controls, errors,
                () -> new Pet(code.getText(), customer.getText().trim(), name.getText().trim(),
                        type.getText().trim(), breed.getText().trim(),
                        Double.parseDouble(weight.getText().trim().replace(',', '.')),
                        birth.getValue(), allergies.getText().trim(), gender.getText().trim()),
                pet -> validate(pet),
                () -> existing == null || !customer.getText().equals(existing.getMaKhachHang())
                        || !name.getText().equals(existing.getTenThuCung())
                        || !type.getText().equals(safe(existing.getLoaiThuCung()))
                        || !breed.getText().equals(safe(existing.getGiongLoai()))
                        || !weight.getText().equals(String.valueOf(existing.getCanNang()))
                        || !java.util.Objects.equals(birth.getValue(), existing.getNgaySinh())
                        || !allergies.getText().equals(safe(existing.getDiUng()))
                        || !gender.getText().equals(safe(existing.getGioiTinh())),
                pet -> {
                    if (existing == null) {
                        controller.add(pet);
                    } else {
                        controller.update(pet);
                    }
                    audit.log(existing == null ? "Thêm" : "Sửa",
                            "ThuCung", pet.getMaThuCung(), null);
                }).show();
    }

    private static List<CrudFormDialog.FieldError> validate(Pet pet) {
        java.util.ArrayList<CrudFormDialog.FieldError> result = new java.util.ArrayList<>();
        if (pet.getMaKhachHang().isBlank()) {
            result.add(new CrudFormDialog.FieldError("customer", "Mã khách hàng là bắt buộc."));
        }
        if (pet.getTenThuCung().isBlank()) {
            result.add(new CrudFormDialog.FieldError("name", "Tên thú cưng là bắt buộc."));
        }
        if (!Double.isFinite(pet.getCanNang()) || pet.getCanNang() <= 0
                || pet.getCanNang() > 999.99) {
            result.add(new CrudFormDialog.FieldError("weight",
                    "Cân nặng phải lớn hơn 0 và tối đa 999,99 kg."));
        }
        if (pet.getLoaiThuCung() != null && pet.getLoaiThuCung().length() > 50) {
            result.add(new CrudFormDialog.FieldError("type", "Loài quá 50 ký tự."));
        }
        if (pet.getGiongLoai() != null && pet.getGiongLoai().length() > 100) {
            result.add(new CrudFormDialog.FieldError("breed", "Giống quá 100 ký tự."));
        }
        if (pet.getDiUng() != null && pet.getDiUng().length() > 255) {
            result.add(new CrudFormDialog.FieldError("allergies", "Thông tin dị ứng quá 255 ký tự."));
        }
        return List.copyOf(result);
    }

    private static Node field(String label, String key, Node control,
                              Map<String, Node> controls, Map<String, Label> errors) {
        Label error = new Label();
        error.getStyleClass().add("field-error");
        controls.put(key, control);
        errors.put(key, error);
        return new VBox(4, new Label(label), control, error);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
