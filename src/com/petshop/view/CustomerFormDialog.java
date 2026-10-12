package com.petshop.view;

import com.petshop.controller.AuditController;
import com.petshop.controller.CustomerController;
import com.petshop.model.Customer;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class CustomerFormDialog {
    private CustomerFormDialog() {
    }

    static void show(
            CustomerController controller, AuditController audit, Customer existing) {
        TextField code = new TextField(existing == null
                ? CustomerView.newCode("KH") : existing.getMaKhachHang());
        code.setEditable(false);
        code.setPromptText("Tự động sinh");
        TextField name = new TextField(existing == null ? "" : existing.getHoTen());
        TextField phone = new TextField(existing == null ? "" : existing.getSoDienThoai());
        TextField email = new TextField(existing == null ? "" : safe(existing.getEmail()));
        TextField address = new TextField(existing == null ? "" : safe(existing.getDiaChi()));
        TextField note = new TextField(existing == null ? "" : safe(existing.getGhiChu()));
        Map<String, Node> controls = new LinkedHashMap<>();
        Map<String, Label> errors = new LinkedHashMap<>();
        VBox content = new VBox(8,
                field("Mã khách hàng", "code", code, controls, errors),
                field("Họ và tên", "name", name, controls, errors),
                field("Số điện thoại", "phone", phone, controls, errors),
                field("Email", "email", email, controls, errors),
                field("Địa chỉ", "address", address, controls, errors),
                field("Ghi chú", "note", note, controls, errors),
                new Label("Điểm tích lũy chỉ được thay đổi từ nghiệp vụ bán hàng."));
        Customer edited = existing;
        new CrudFormDialog<>(
                existing == null ? "Thêm khách hàng" : "Sửa khách hàng",
                content, controls, errors,
                () -> new Customer(code.getText(), name.getText(), phone.getText(),
                        email.getText(), existing == null ? 0 : existing.getDiemTichLuy(),
                        address.getText(), note.getText()),
                customer -> validate(customer),
                () -> existing == null || !name.getText().equals(existing.getHoTen())
                        || !phone.getText().equals(existing.getSoDienThoai())
                        || !email.getText().equals(safe(existing.getEmail()))
                        || !address.getText().equals(safe(existing.getDiaChi()))
                        || !note.getText().equals(safe(existing.getGhiChu())),
                customer -> {
                    if (edited == null) {
                        controller.add(customer);
                    } else {
                        controller.update(customer);
                    }
                    audit.log(edited == null ? "Thêm" : "Sửa",
                            "KhachHang", customer.getMaKhachHang(), null);
                }).show();
    }

    private static List<CrudFormDialog.FieldError> validate(Customer customer) {
        java.util.ArrayList<CrudFormDialog.FieldError> result = new java.util.ArrayList<>();
        if (customer.getHoTen().isBlank()) {
            result.add(new CrudFormDialog.FieldError("name",
                    "Họ tên khách hàng không được để trống."));
        }
        if (!customer.getSoDienThoai().matches("\\d{10}")) {
            result.add(new CrudFormDialog.FieldError("phone",
                    "Số điện thoại phải gồm đúng 10 chữ số."));
        }
        if (customer.getEmail() != null && !customer.getEmail().isBlank()
                && !customer.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            result.add(new CrudFormDialog.FieldError("email", "Địa chỉ email không hợp lệ."));
        }
        if (customer.getEmail() != null && customer.getEmail().length() > 100) {
            result.add(new CrudFormDialog.FieldError("email", "Email quá 100 ký tự."));
        }
        if (customer.getDiaChi() != null && customer.getDiaChi().length() > 255) {
            result.add(new CrudFormDialog.FieldError("address", "Địa chỉ quá 255 ký tự."));
        }
        if (customer.getGhiChu() != null && customer.getGhiChu().length() > 255) {
            result.add(new CrudFormDialog.FieldError("note", "Ghi chú quá 255 ký tự."));
        }
        return List.copyOf(result);
    }

    private static Node field(String label, String key, TextField control,
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
