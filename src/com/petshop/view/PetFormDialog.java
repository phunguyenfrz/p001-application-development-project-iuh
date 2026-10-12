package com.petshop.view;

import com.petshop.controller.CustomerController;
import com.petshop.controller.PetController;
import com.petshop.model.Customer;
import com.petshop.model.Pet;
import com.petshop.util.TextSearch;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class PetFormDialog {
    private PetFormDialog() {
    }

    static void show(
            PetController controller,
            CustomerController customers,
            Pet existing,
            String preselectedCustomerId) {
        TextField code = new TextField(existing == null
                ? "Tự động sinh" : existing.getMaThuCung());
        code.setEditable(false);
        code.setDisable(existing != null);
        ComboBox<Customer> owner = new ComboBox<>();
        owner.setEditable(true);
        owner.setPromptText("Tìm theo tên hoặc số điện thoại");
        owner.setConverter(new StringConverter<>() {
            @Override
            public String toString(Customer customer) {
                return customer == null ? "" : customer.getHoTen()
                        + " · " + customer.getSoDienThoai();
            }

            @Override
            public Customer fromString(String value) {
                return owner.getItems().stream()
                        .filter(customer -> toString(customer).equals(value))
                        .findFirst().orElse(null);
            }
        });
        TextField name = new TextField(existing == null ? "" : safe(existing.getTenThuCung()));
        TextField type = new TextField(existing == null ? "" : safe(existing.getLoaiThuCung()));
        TextField breed = new TextField(existing == null ? "" : safe(existing.getGiongLoai()));
        TextField gender = new TextField(existing == null ? "" : safe(existing.getGioiTinh()));
        DatePicker birth = new DatePicker(existing == null ? null : existing.getNgaySinh());
        TextField weight = new TextField(existing == null ? "" : String.valueOf(existing.getCanNang()));
        TextField identification = new TextField(
                existing == null ? "" : safe(existing.getDacDiemNhanDang()));
        TextField allergies = new TextField(existing == null ? "" : safe(existing.getDiUng()));
        TextField conditions = new TextField(existing == null ? "" : safe(existing.getBenhNen()));
        TextField personality = new TextField(existing == null ? "" : safe(existing.getTinhCach()));
        Map<String, Node> controls = new LinkedHashMap<>();
        Map<String, Label> errors = new LinkedHashMap<>();
        VBox content = new VBox(8,
                field("Mã thú cưng", "code", code, controls, errors),
                field("Chủ nuôi", "owner", owner, controls, errors),
                field("Tên thú cưng", "name", name, controls, errors),
                field("Loài", "type", type, controls, errors),
                field("Giống", "breed", breed, controls, errors),
                field("Giới tính", "gender", gender, controls, errors),
                field("Ngày sinh", "birth", birth, controls, errors),
                field("Cân nặng (kg)", "weight", weight, controls, errors),
                field("Đặc điểm nhận dạng", "identification", identification, controls, errors),
                field("Dị ứng", "allergies", allergies, controls, errors),
                field("Bệnh nền", "conditions", conditions, controls, errors),
                field("Tính cách", "personality", personality, controls, errors));

        List<Customer> allCustomers = new ArrayList<>();
        owner.getEditor().textProperty().addListener((observable, oldValue, value) -> {
            if (owner.getValue() != null
                    && !Objects.equals(owner.getConverter().toString(owner.getValue()), value)) {
                owner.setValue(null);
            }
            if (owner.getValue() == null) {
                String query = value == null ? "" : value.trim();
                List<Customer> filtered = query.isEmpty() ? allCustomers
                        : allCustomers.stream().filter(customer ->
                                TextSearch.matchesAll(query,
                                        customer.getHoTen(), customer.getSoDienThoai()))
                                .toList();
                owner.getItems().setAll(filtered);
                if (!filtered.isEmpty()) {
                    owner.show();
                }
            }
        });

        CrudFormDialog<Pet> form = new CrudFormDialog<>(
                existing == null ? "Thêm hồ sơ thú cưng" : "Sửa hồ sơ thú cưng",
                content, controls, errors,
                () -> {
                    Customer selectedOwner = owner.getValue();
                    Pet pet = new Pet(existing == null ? null : existing.getMaThuCung(),
                            selectedOwner == null ? "" : selectedOwner.getMaKhachHang(),
                            name.getText().trim(), type.getText().trim(), breed.getText().trim(),
                            parseWeight(weight.getText()), birth.getValue(),
                            allergies.getText().trim(), gender.getText().trim());
                    pet.setDacDiemNhanDang(identification.getText().trim());
                    pet.setBenhNen(conditions.getText().trim());
                    pet.setTinhCach(personality.getText().trim());
                    if (existing != null) {
                        pet.setActive(existing.isActive());
                    }
                    return pet;
                },
                PetFormDialog::validate,
                () -> existing == null
                        || !Objects.equals(owner.getValue() == null ? null
                                : owner.getValue().getMaKhachHang(), existing.getMaKhachHang())
                        || !name.getText().equals(safe(existing.getTenThuCung()))
                        || !type.getText().equals(safe(existing.getLoaiThuCung()))
                        || !breed.getText().equals(safe(existing.getGiongLoai()))
                        || !gender.getText().equals(safe(existing.getGioiTinh()))
                        || !Objects.equals(birth.getValue(), existing.getNgaySinh())
                        || !weight.getText().equals(String.valueOf(existing.getCanNang()))
                        || !identification.getText().equals(safe(existing.getDacDiemNhanDang()))
                        || !allergies.getText().equals(safe(existing.getDiUng()))
                        || !conditions.getText().equals(safe(existing.getBenhNen()))
                        || !personality.getText().equals(safe(existing.getTinhCach())),
                pet -> {
                    if (existing == null) {
                        controller.add(pet);
                    } else {
                        controller.update(pet);
                    }
                });

        form.setSaveEnabled(false);
        Task<List<Customer>> loadCustomers = new Task<>() {
            @Override
            protected List<Customer> call() {
                return customers.findAll();
            }
        };
        loadCustomers.setOnSucceeded(event -> {
            allCustomers.addAll(loadCustomers.getValue());
            owner.getItems().setAll(allCustomers);
            String selectedId = existing == null ? preselectedCustomerId
                    : existing.getMaKhachHang();
            if (selectedId != null) {
                allCustomers.stream()
                        .filter(customer -> customer.getMaKhachHang().equals(selectedId))
                        .findFirst().ifPresent(owner::setValue);
            }
            form.setSaveEnabled(true);
        });
        loadCustomers.setOnFailed(event -> {
            Label loadError = new Label(loadCustomers.getException() == null
                    ? "Không thể tải danh sách khách hàng."
                    : loadCustomers.getException().getMessage());
            loadError.getStyleClass().add("feedback-error");
            content.getChildren().add(loadError);
        });
        Thread worker = new Thread(loadCustomers, "pet-customer-lookup");
        worker.setDaemon(true);
        worker.start();
        form.show();
    }

    private static List<CrudFormDialog.FieldError> validate(Pet pet) {
        List<CrudFormDialog.FieldError> errors = new ArrayList<>();
        if (pet.getMaKhachHang().isBlank()) {
            errors.add(new CrudFormDialog.FieldError("owner", "Chọn chủ nuôi từ danh sách."));
        }
        if (pet.getTenThuCung().isBlank()) {
            errors.add(new CrudFormDialog.FieldError("name", "Tên thú cưng là bắt buộc."));
        }
        if (pet.getNgaySinh() != null && pet.getNgaySinh().isAfter(LocalDate.now())) {
            errors.add(new CrudFormDialog.FieldError("birth", "Ngày sinh không được ở tương lai."));
        }
        if (!Double.isFinite(pet.getCanNang()) || pet.getCanNang() <= 0
                || pet.getCanNang() > 999.99) {
            errors.add(new CrudFormDialog.FieldError("weight",
                    "Cân nặng phải là số thực lớn hơn 0 và tối đa 999,99 kg."));
        }
        validateLength(errors, "type", pet.getLoaiThuCung(), 50, "Loài");
        validateLength(errors, "breed", pet.getGiongLoai(), 100, "Giống");
        validateLength(errors, "identification", pet.getDacDiemNhanDang(), 255,
                "Đặc điểm nhận dạng");
        validateLength(errors, "allergies", pet.getDiUng(), 255, "Dị ứng");
        validateLength(errors, "conditions", pet.getBenhNen(), 255, "Bệnh nền");
        validateLength(errors, "personality", pet.getTinhCach(), 255, "Tính cách");
        return List.copyOf(errors);
    }

    private static void validateLength(
            List<CrudFormDialog.FieldError> errors,
            String field,
            String value,
            int maxLength,
            String label) {
        if (value != null && value.length() > maxLength) {
            errors.add(new CrudFormDialog.FieldError(
                    field, label + " không được vượt quá " + maxLength + " ký tự."));
        }
    }

    private static double parseWeight(String value) {
        try {
            return Double.parseDouble(value.trim().replace(',', '.'));
        } catch (NumberFormatException exception) {
            return Double.NaN;
        }
    }

    private static Node field(
            String label, String key, Node control,
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
