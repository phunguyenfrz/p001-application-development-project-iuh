package com.petshop.view;

import com.petshop.dao.AssignedOperationsDao;
import com.petshop.dao.AssignedOperationsDao.AppointmentRow;
import com.petshop.dao.AssignedOperationsDao.LookupOption;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class AppointmentModuleView extends VBox {

    private static final List<String> STATUSES = List.of("Đã đặt", "Đã xác nhận", "Đang thực hiện", "Hoàn thành", "Đã hủy");
    private static final Map<String, List<String>> NEXT_STATUSES = Map.of(
            "Đã đặt", List.of("Đã xác nhận", "Đã hủy"),
            "Đã xác nhận", List.of("Đang thực hiện", "Đã hủy"),
            "Đang thực hiện", List.of("Hoàn thành", "Đã hủy"));

    private final AssignedOperationsDao dao = new AssignedOperationsDao();
    private final String actionId;
    private final String actionName;
    private final TextField searchField = new TextField();
    private final Label feedback = new Label();
    private final TableView<AppointmentRow> table = new TableView<>();

    public AppointmentModuleView(String actionId, String actionName) {
        this.actionId = actionId;
        this.actionName = actionName;
        setSpacing(16);
        setPadding(new Insets(24, 28, 28, 28));
        setStyle("-fx-background-color: #f3f6f3;");

        Label title = new Label("Lịch hẹn  /  " + actionName);
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");
        Label subtitle = new Label("Theo dõi lịch chăm sóc, trạng thái và thay đổi thời gian hẹn.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #6e7c70;");
        getChildren().add(new VBox(4, title, subtitle));

        table.getColumns().addAll(
                column("Mã lịch hẹn", 180, AppointmentRow::id),
                column("Khách hàng", 145, AppointmentRow::customer),
                column("Thú cưng", 130, AppointmentRow::pet),
                column("Nhân viên", 145, AppointmentRow::employee),
                column("Ngày hẹn", 110, row -> row.date().toString()),
                column("Giờ hẹn", 90, row -> row.time().format(DateTimeFormatter.ofPattern("HH:mm"))),
                column("Trạng thái", 130, AppointmentRow::status),
                column("Ghi chú", 180, row -> value(row.note())));
        table.setPlaceholder(new Label("Không có lịch hẹn phù hợp."));
        VBox.setVgrow(table, Priority.ALWAYS);

        searchField.setPromptText("Mã lịch, khách hàng, thú cưng hoặc trạng thái");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(330);
        searchField.setOnAction(event -> loadAppointments());
        Button search = button("Tìm kiếm", false);
        search.setOnAction(event -> loadAppointments());
        Button refresh = button("Làm mới", false);
        refresh.setOnAction(event -> {
            searchField.clear();
            loadAppointments();
        });
        HBox toolbar = new HBox(8, searchField, search, refresh);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        toolbar.getChildren().add(spacer);

        if ("APT_CREATE".equals(actionId)) {
            Button create = button("Đặt lịch hẹn", true);
            create.setOnAction(event -> createAppointment());
            toolbar.getChildren().add(create);
        } else if ("APT_STATUS".equals(actionId)) {
            Button updateStatus = button("Cập nhật trạng thái", true);
            updateStatus.setOnAction(event -> updateStatus());
            toolbar.getChildren().add(updateStatus);
        } else if ("APT_RESCHEDULE".equals(actionId)) {
            Button reschedule = button("Dời lịch", true);
            reschedule.setOnAction(event -> rescheduleAppointment());
            Button cancel = button("Hủy lịch", false);
            cancel.setOnAction(event -> cancelAppointment());
            toolbar.getChildren().addAll(reschedule, cancel);
        }

        VBox card = new VBox(12, toolbar, feedback, table);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #e5ebe5; -fx-border-radius: 8px;");
        VBox.setVgrow(card, Priority.ALWAYS);
        getChildren().add(card);
        loadAppointments();
    }

    private void loadAppointments() {
        try {
            table.setItems(FXCollections.observableArrayList(dao.findAppointments(searchField.getText())));
            feedback.setText("Hiển thị " + table.getItems().size() + " lịch hẹn.");
        } catch (RuntimeException exception) {
            table.getItems().clear();
            feedback.setText(exception.getMessage());
        }
    }

    private void createAppointment() {
        List<LookupOption> customers;
        List<LookupOption> employees;
        List<LookupOption> services;
        try {
            customers = dao.findCustomerOptions();
            employees = dao.findEmployeeOptions();
            services = dao.findServiceOptions();
        } catch (RuntimeException exception) {
            showAlert(Alert.AlertType.ERROR, "Không tải được dữ liệu", exception.getMessage());
            return;
        }

        Dialog<AppointmentForm> dialog = new Dialog<>();
        dialog.setTitle("Đặt lịch hẹn");
        ButtonType save = new ButtonType("Đặt lịch", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        ComboBox<LookupOption> customer = new ComboBox<>(FXCollections.observableArrayList(customers));
        ComboBox<LookupOption> pet = new ComboBox<>();
        ComboBox<LookupOption> employee = new ComboBox<>(FXCollections.observableArrayList(employees));
        ComboBox<LookupOption> service = new ComboBox<>(FXCollections.observableArrayList(services));
        customer.setMaxWidth(Double.MAX_VALUE);
        pet.setMaxWidth(Double.MAX_VALUE);
        employee.setMaxWidth(Double.MAX_VALUE);
        service.setMaxWidth(Double.MAX_VALUE);
        customer.valueProperty().addListener((observable, oldValue, selected) -> {
            pet.getItems().clear();
            if (selected != null) {
                try {
                    pet.setItems(FXCollections.observableArrayList(dao.findPetOptions(selected.id())));
                    pet.getSelectionModel().selectFirst();
                } catch (RuntimeException exception) {
                    showAlert(Alert.AlertType.ERROR, "Không tải được thú cưng", exception.getMessage());
                }
            }
        });
        customer.getSelectionModel().selectFirst();
        employee.getSelectionModel().selectFirst();
        service.getSelectionModel().selectFirst();

        DatePicker date = new DatePicker(LocalDate.now());
        TextField time = new TextField("09:00");
        time.setPromptText("HH:mm");
        time.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d{0,2}(:\\d{0,2})?") ? change : null));
        TextField weight = new TextField("4.00");
        weight.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d{0,3}([.,]\\d{0,2})?") ? change : null));
        TextArea note = new TextArea();
        note.setPromptText("Ghi chú (không bắt buộc)");
        note.setPrefRowCount(2);

        GridPane fields = new GridPane();
        fields.setHgap(12);
        fields.setVgap(10);
        fields.addRow(0, new Label("Khách hàng"), customer);
        fields.addRow(1, new Label("Thú cưng"), pet);
        fields.addRow(2, new Label("Nhân viên"), employee);
        fields.addRow(3, new Label("Dịch vụ"), service);
        fields.addRow(4, new Label("Ngày hẹn"), date);
        fields.addRow(5, new Label("Giờ hẹn"), time);
        fields.addRow(6, new Label("Cân nặng (kg)"), weight);
        fields.addRow(7, new Label("Ghi chú"), note);
        dialog.getDialogPane().setContent(fields);
        Node saveButton = dialog.getDialogPane().lookupButton(save);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            boolean invalidTime = !validTime(time.getText());
            boolean invalidWeight = !validWeight(weight.getText());
            if (customer.getValue() == null || pet.getValue() == null || employee.getValue() == null
                    || service.getValue() == null || date.getValue() == null || date.getValue().isBefore(LocalDate.now())
                    || invalidTime || invalidWeight || note.getText().length() > 255) {
                showAlert(Alert.AlertType.WARNING, "Kiểm tra dữ liệu", "Chọn đủ thông tin; ngày, giờ, cân nặng hoặc ghi chú chưa hợp lệ.");
                event.consume();
            }
        });
        dialog.setResultConverter(button -> button == save
                ? new AppointmentForm(customer.getValue(), pet.getValue(), employee.getValue(), service.getValue(),
                        date.getValue(), LocalTime.parse(time.getText()), Double.parseDouble(weight.getText().replace(',', '.')), note.getText())
                : null);
        dialog.showAndWait().ifPresent(form -> {
            try {
                dao.createAppointment(form.customer().id(), form.pet().id(), form.employee().id(), form.service().id(),
                        form.weight(), form.date(), form.time(), form.note());
                loadAppointments();
            } catch (RuntimeException exception) {
                showAlert(Alert.AlertType.ERROR, "Không đặt được lịch", exception.getMessage());
            }
        });
    }

    private void updateStatus() {
        AppointmentRow selected = selectedAppointment();
        if (selected == null) {
            return;
        }
        List<String> allowed = NEXT_STATUSES.getOrDefault(selected.status(), List.of());
        if (allowed.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Không thể cập nhật", "Lịch đã ở trạng thái kết thúc.");
            return;
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(allowed.get(0), allowed);
        dialog.setTitle("Cập nhật trạng thái");
        dialog.setHeaderText(selected.id() + " · " + selected.status());
        dialog.setContentText("Trạng thái mới:");
        dialog.showAndWait().ifPresent(status -> runStatusUpdate(selected, status));
    }

    private void runStatusUpdate(AppointmentRow selected, String status) {
        try {
            dao.updateAppointmentStatus(selected.id(), status);
            loadAppointments();
        } catch (RuntimeException exception) {
            showAlert(Alert.AlertType.ERROR, "Không cập nhật được", exception.getMessage());
        }
    }

    private void rescheduleAppointment() {
        AppointmentRow selected = selectedAppointment();
        if (selected == null) {
            return;
        }
        if (!List.of("Đã đặt", "Đã xác nhận").contains(selected.status())) {
            showAlert(Alert.AlertType.INFORMATION, "Không thể dời lịch", "Chỉ lịch đã đặt hoặc đã xác nhận mới được dời.");
            return;
        }
        Dialog<RescheduleForm> dialog = new Dialog<>();
        dialog.setTitle("Dời lịch hẹn");
        ButtonType save = new ButtonType("Lưu lịch mới", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);
        DatePicker date = new DatePicker(selected.date());
        TextField time = new TextField(selected.time().format(DateTimeFormatter.ofPattern("HH:mm")));
        time.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d{0,2}(:\\d{0,2})?") ? change : null));
        GridPane fields = new GridPane();
        fields.setHgap(12);
        fields.setVgap(10);
        fields.addRow(0, new Label("Ngày mới"), date);
        fields.addRow(1, new Label("Giờ mới (HH:mm)"), time);
        dialog.getDialogPane().setContent(fields);
        dialog.getDialogPane().lookupButton(save).addEventFilter(ActionEvent.ACTION, event -> {
            if (date.getValue() == null || date.getValue().isBefore(LocalDate.now()) || !validTime(time.getText())) {
                showAlert(Alert.AlertType.WARNING, "Kiểm tra dữ liệu", "Chọn ngày không ở quá khứ và giờ theo định dạng HH:mm.");
                event.consume();
            }
        });
        dialog.setResultConverter(button -> button == save
                ? new RescheduleForm(date.getValue(), LocalTime.parse(time.getText())) : null);
        dialog.showAndWait().ifPresent(form -> {
            try {
                dao.rescheduleAppointment(selected.id(), form.date(), form.time());
                loadAppointments();
            } catch (RuntimeException exception) {
                showAlert(Alert.AlertType.ERROR, "Không dời được lịch", exception.getMessage());
            }
        });
    }

    private void cancelAppointment() {
        AppointmentRow selected = selectedAppointment();
        if (selected == null) {
            return;
        }
        if (!List.of("Đã đặt", "Đã xác nhận").contains(selected.status())) {
            showAlert(Alert.AlertType.INFORMATION, "Không thể hủy lịch", "Lịch đang thực hiện hoặc đã kết thúc không thể hủy.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Lịch sẽ được cập nhật trạng thái Đã hủy.",
                ButtonType.CANCEL, ButtonType.OK);
        confirm.setHeaderText("Hủy lịch " + selected.id() + "?");
        if (confirm.showAndWait().filter(ButtonType.OK::equals).isEmpty()) {
            return;
        }
        runStatusUpdate(selected, "Đã hủy");
    }

    private AppointmentRow selectedAppointment() {
        AppointmentRow selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.INFORMATION, "Chưa chọn lịch", "Chọn một lịch hẹn trong bảng trước.");
        }
        return selected;
    }

    private static boolean validTime(String value) {
        try {
            LocalTime.parse(value, DateTimeFormatter.ofPattern("HH:mm"));
            return true;
        } catch (DateTimeParseException exception) {
            return false;
        }
    }

    private static boolean validWeight(String value) {
        try {
            double weight = Double.parseDouble(value.replace(',', '.'));
            return Double.isFinite(weight) && weight > 0 && weight <= 999;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private static <T> TableColumn<T, String> column(String title, double width, Function<T, String> value) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }

    private static Button button(String text, boolean primary) {
        Button button = new Button(text);
        button.getStyleClass().add(primary ? "btn-action-primary" : "btn-action-secondary");
        return button;
    }

    private static String value(String text) {
        return text == null ? "" : text;
    }

    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message == null ? "Có lỗi không xác định." : message);
        alert.showAndWait();
    }

    private record AppointmentForm(LookupOption customer, LookupOption pet, LookupOption employee,
                                   LookupOption service, LocalDate date, LocalTime time, double weight, String note) {}

    private record RescheduleForm(LocalDate date, LocalTime time) {}
}
