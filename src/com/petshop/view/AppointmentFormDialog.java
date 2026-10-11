package com.petshop.view;

import com.petshop.controller.AppointmentController;
import com.petshop.controller.AuditController;
import com.petshop.controller.CustomerController;
import com.petshop.model.Customer;
import com.petshop.model.AppointmentData.CustomerOption;
import com.petshop.model.AppointmentData.CreationResult;
import com.petshop.model.AppointmentData.Draft;
import com.petshop.model.AppointmentData.EmployeeOption;
import com.petshop.model.AppointmentData.Lookups;
import com.petshop.model.AppointmentData.PetOption;
import com.petshop.model.AppointmentData.ServiceOption;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

final class AppointmentFormDialog {
    private final AppointmentController controller;
    private final CustomerController customerController;
    private final AuditController auditController;
    private final Runnable savedCallback;
    private final Dialog<Void> dialog = new Dialog<>();
    private final ComboBox<CustomerOption> customer = new ComboBox<>();
    private final ComboBox<PetOption> pet = new ComboBox<>();
    private final ComboBox<EmployeeOption> employee = new ComboBox<>();
    private final DatePicker date = new DatePicker();
    private final TextField time = new TextField();
    private final TextField note = new TextField();
    private final LocalDate originalDate;
    private final ListView<ServiceOption> services = new ListView<>();
    private final Label serviceHint = new Label("Chọn thú cưng để tải dịch vụ và giá theo cân nặng.");
    private final Label formError = new Label();
    private final Map<String, Node> controls = new LinkedHashMap<>();
    private final Map<String, Label> errors = new LinkedHashMap<>();
    private final Map<ServiceOption, BooleanProperty> selectedServices = new LinkedHashMap<>();
    private final AtomicLong serviceRequestSequence = new AtomicLong();
    private boolean saving;
    private boolean saved;
    private boolean lookupsReady;
    private Lookups lookups;
    private String selectAfterLookup;

    AppointmentFormDialog(
            AppointmentController controller,
            CustomerController customerController,
            AuditController auditController,
            LocalDate initialDate,
            Runnable savedCallback) {
        this.controller = Objects.requireNonNull(controller);
        this.customerController = customerController;
        this.auditController = auditController;
        this.savedCallback = savedCallback;
        date.setValue(initialDate == null ? LocalDate.now() : initialDate);
        originalDate = date.getValue();
        time.setPromptText("HH:mm");
        time.setText("09:00");
        note.setPromptText("Ghi chú (không bắt buộc)");
        customer.setConverter(converter());
        pet.setConverter(converter());
        employee.setConverter(converter());
        customer.valueProperty().addListener((observable, oldValue, value) -> {
            pet.getItems().clear();
            pet.setValue(null);
            services.getItems().clear();
            selectedServices.clear();
            serviceHint.setText("Chọn thú cưng để tải dịch vụ và giá theo cân nặng.");
            if (value != null && lookupsReady) {
                fillPets(value.id());
            }
        });
        pet.valueProperty().addListener((observable, oldValue, value) -> {
            services.getItems().clear();
            selectedServices.clear();
            if (value != null) {
                loadServices(value);
            } else {
                serviceHint.setText("Chọn thú cưng để tải dịch vụ và giá theo cân nặng.");
            }
        });
        date.valueProperty().addListener((observable, oldValue, value) -> {
            PetOption selected = pet.getValue();
            if (value != null && selected != null) {
                loadServices(selected);
            }
        });
        configureServiceList();
        loadLookups();
    }

    private void loadLookups() {
        loadLookups(null);
    }

    private void loadLookups(String customerIdToSelect) {
        setFormDisabled(true);
        LocalDate lookupDate = date.getValue();
        Task<Lookups> task = new Task<>() {
            @Override
            protected Lookups call() {
                return controller.loadLookups(lookupDate);
            }
        };
        task.setOnSucceeded(event -> {
            setFormDisabled(false);
            lookups = task.getValue();
            customer.getItems().setAll(lookups.customers());
            employee.getItems().setAll(lookups.employees());
            lookupsReady = true;
            if (customerIdToSelect != null) {
                lookups.customers().stream()
                        .filter(option -> option.id().equals(customerIdToSelect))
                        .findFirst().ifPresent(customer::setValue);
                formError.setText("");
            } else if (lookups.customers().isEmpty()) {
                formError.setText("Chưa có khách hàng để đặt lịch.");
            }
            if (lookups.employees().isEmpty()) {
                formError.setText("Chưa có nhân viên Chăm sóc đang làm.");
            }
        });
        task.setOnFailed(event -> {
            setFormDisabled(false);
            formError.setText(task.getException() == null
                    ? "Không thể tải dữ liệu đặt lịch." : task.getException().getMessage());
        });
        start(task, "appointment-lookups");
        show();
    }

    private Node customerField() {
        Button addCustomer = new Button("Thêm nhanh");
        addCustomer.setDisable(customerController == null || auditController == null);
        addCustomer.setOnAction(event -> openQuickCustomerDialog(addCustomer));
        HBox field = new HBox(8, customer, addCustomer);
        HBox.setHgrow(customer, javafx.scene.layout.Priority.ALWAYS);
        return field;
    }

    private void openQuickCustomerDialog(Button source) {
        TextField name = new TextField();
        TextField phone = new TextField();
        TextField email = new TextField();
        TextField address = new TextField();
        Label error = new Label();
        error.getStyleClass().add("feedback-error");
        VBox content = new VBox(8,
                new Label("Họ và tên"), name,
                new Label("Số điện thoại (10 chữ số)"), phone,
                new Label("Email (không bắt buộc)"), email,
                new Label("Địa chỉ (không bắt buộc)"), address, error);
        Dialog<Void> quickDialog = new Dialog<>();
        quickDialog.setTitle("Thêm nhanh khách hàng");
        ButtonType save = new ButtonType("Thêm", ButtonBar.ButtonData.OK_DONE);
        quickDialog.getDialogPane().getButtonTypes().setAll(save, ButtonType.CANCEL);
        quickDialog.getDialogPane().setContent(content);
        quickDialog.getDialogPane().lookupButton(save).addEventFilter(
                ActionEvent.ACTION, action -> {
                    String id = CustomerView.newCode("KH");
                    Customer created = new Customer(id, name.getText(), phone.getText(),
                            email.getText(), 0, address.getText(), "");
                    if (created.getHoTen().isBlank()) {
                        error.setText("Họ tên khách hàng không được để trống.");
                        action.consume();
                        return;
                    }
                    if (!created.getSoDienThoai().matches("\\d{10}")) {
                        error.setText("Số điện thoại phải gồm đúng 10 chữ số.");
                        action.consume();
                        return;
                    }
                    source.setDisable(true);
                    Task<Void> task = new Task<>() {
                        @Override
                        protected Void call() {
                            customerController.add(created);
                            auditController.log("Thêm", "KhachHang", id, "Thêm nhanh khi đặt lịch");
                            return null;
                        }
                    };
                    task.setOnSucceeded(done -> {
                        source.setDisable(false);
                        quickDialog.close();
                        loadLookups(id);
                    });
                    task.setOnFailed(failed -> {
                        source.setDisable(false);
                        Throwable cause = task.getException();
                        error.setText(cause == null || cause.getMessage() == null
                                ? "Không thể thêm khách hàng." : cause.getMessage());
                    });
                    start(task, "appointment-customer-add");
                    action.consume();
                });
        quickDialog.show();
    }

    private void show() {
        dialog.setTitle("Đặt lịch hẹn");
        dialog.setHeaderText(null);
        ButtonType save = new ButtonType("Đặt lịch", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(save, cancel);
        formError.getStyleClass().add("feedback-error");
        formError.setWrapText(true);
        VBox content = new VBox(8,
                field("Khách hàng", "customer", customerField()),
                field("Thú cưng", "pet", pet),
                field("Nhân viên Chăm sóc", "employee", employee),
                field("Ngày hẹn", "date", date),
                field("Giờ bắt đầu", "time", time),
                new Label("Dịch vụ (có thể chọn nhiều)"),
                serviceHint,
                services,
                field("Ghi chú", "note", note),
                formError);
        content.setPadding(new Insets(4));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setMinWidth(520);
        dialog.getDialogPane().setMinHeight(560);
        dialog.getDialogPane().lookupButton(save).addEventFilter(
                ActionEvent.ACTION, event -> submit(event));
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
        dialog.show();
    }

    private Node field(String label, String key, Node control) {
        Label error = new Label();
        error.getStyleClass().add("field-error");
        errors.put(key, error);
        controls.put(key, control);
        return new VBox(4, new Label(label), control, error);
    }

    private void configureServiceList() {
        services.setCellFactory(CheckBoxListCell.forListView(item ->
                selectedServices.computeIfAbsent(item, ignored -> new SimpleBooleanProperty(false)),
                converter()));
        VBox.setVgrow(services, javafx.scene.layout.Priority.ALWAYS);
    }

    private void fillPets(String customerId) {
        if (lookups != null) {
            pet.getItems().setAll(lookups.pets().stream()
                    .filter(option -> option.customerId().equals(customerId)).toList());
        }
    }

    private void loadServices(PetOption selectedPet) {
        services.getItems().clear();
        selectedServices.clear();
        serviceHint.setText("Đang tải dịch vụ theo cân nặng...");
        LocalDate effectiveDate = date.getValue();
        long requestId = serviceRequestSequence.incrementAndGet();
        Task<List<ServiceOption>> task = new Task<>() {
            @Override
            protected List<ServiceOption> call() {
                return controller.loadServices(effectiveDate, selectedPet.weight());
            }
        };
        task.setOnSucceeded(event -> {
            if (serviceRequestSequence.get() != requestId
                    || !Objects.equals(pet.getValue(), selectedPet)
                    || !Objects.equals(date.getValue(), effectiveDate)) {
                return;
            }
            selectedServices.clear();
            services.getItems().setAll(task.getValue());
            serviceHint.setText(task.getValue().isEmpty()
                    ? "Không có dịch vụ với bảng giá hiệu lực trong ngày đã chọn."
                    : "Giá được xác định theo cân nặng " + selectedPet.weight() + " kg.");
        });
        task.setOnFailed(event -> {
            if (serviceRequestSequence.get() == requestId) {
                serviceHint.setText(task.getException() == null
                        ? "Không thể tải dịch vụ." : task.getException().getMessage());
            }
        });
        start(task, "appointment-services");
    }

    private void submit(ActionEvent event) {
        clearErrors();
        if (!validate()) {
            event.consume();
            return;
        }
        List<String> serviceIds = selectedServices.entrySet().stream()
                .filter(entry -> entry.getValue().get())
                .map(entry -> entry.getKey().id()).toList();
        Draft draft = new Draft(customer.getValue().id(), pet.getValue().id(),
                employee.getValue().id(), date.getValue(),
                LocalTime.parse(time.getText().trim()), note.getText().trim(), serviceIds);
        saving = true;
        setFormDisabled(true);
        Task<CreationResult> task = new Task<>() {
            @Override
            protected CreationResult call() {
                return controller.create(draft);
            }
        };
        task.setOnSucceeded(done -> {
            saving = false;
            saved = true;
            dialog.close();
            savedCallback.run();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(task.getValue().auditLogged()
                    ? "Đặt lịch thành công" : "Lịch hẹn đã lưu");
            alert.setHeaderText("Mã lịch hẹn: " + task.getValue().id());
            alert.setContentText(task.getValue().auditLogged()
                    ? "Trạng thái ban đầu: Đã đặt."
                    : "Lịch đã được lưu nhưng không ghi được nhật ký thao tác.");
            alert.show();
        });
        task.setOnFailed(failed -> {
            saving = false;
            setFormDisabled(false);
            formError.setText(task.getException() == null
                    ? "Không thể đặt lịch hẹn." : task.getException().getMessage());
        });
        start(task, "appointment-create");
        event.consume();
    }

    private boolean validate() {
        boolean valid = true;
        if (customer.getValue() == null) {
            error("customer", "Hãy chọn khách hàng.");
            valid = false;
        }
        if (pet.getValue() == null) {
            error("pet", "Hãy chọn thú cưng.");
            valid = false;
        }
        if (employee.getValue() == null) {
            error("employee", "Hãy chọn nhân viên Chăm sóc.");
            valid = false;
        }
        if (date.getValue() == null || date.getValue().isBefore(LocalDate.now())) {
            error("date", "Ngày hẹn không được ở quá khứ.");
            valid = false;
        }
        try {
            LocalTime.parse(time.getText().trim());
        } catch (RuntimeException exception) {
            error("time", "Giờ hẹn phải có định dạng HH:mm.");
            valid = false;
        }
        if (selectedServices.values().stream().noneMatch(BooleanProperty::get)) {
            serviceHint.setText("Hãy chọn ít nhất một dịch vụ.");
            valid = false;
        }
        return valid;
    }

    private boolean isDirty() {
        return customer.getValue() != null || pet.getValue() != null
                || employee.getValue() != null || !note.getText().isBlank()
                || !Objects.equals(originalDate, date.getValue())
                || !"09:00".equals(time.getText())
                || selectedServices.values().stream().anyMatch(BooleanProperty::get);
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

    private void setFormDisabled(boolean disabled) {
        customer.setDisable(disabled);
        pet.setDisable(disabled);
        employee.setDisable(disabled);
        date.setDisable(disabled);
        time.setDisable(disabled);
        note.setDisable(disabled);
        services.setDisable(disabled);
        dialog.getDialogPane().getButtonTypes().forEach(type -> {
            Node button = dialog.getDialogPane().lookupButton(type);
            if (button != null) {
                button.setDisable(disabled);
            }
        });
    }

    private <T> StringConverter<T> converter() {
        return new StringConverter<>() {
            @Override
            public String toString(T value) {
                return value == null ? "" : value.toString();
            }

            @Override
            public T fromString(String value) {
                return null;
            }
        };
    }

    private void start(Task<?> task, String threadName) {
        Thread worker = new Thread(task, threadName);
        worker.setDaemon(true);
        worker.start();
    }
}
