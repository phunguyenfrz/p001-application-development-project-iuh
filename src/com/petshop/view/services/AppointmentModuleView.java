package com.petshop.view.services;

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
import java.util.function.Function;

/** Appointment creation, lifecycle, rescheduling, and search workflows. */
public class AppointmentModuleView extends VBox {

    // End states intentionally have no outgoing transitions.
    private static final Map<String, List<String>> NEXT_STATUSES = Map.of(
            "Đã đặt", List.of("Đã xác nhận", "Đã hủy"),
            "Đã xác nhận", List.of("Đang thực hiện", "Đã hủy"),
            "Đang thực hiện", List.of("Hoàn thành", "Đã hủy"));

    private final AssignedOperationsDao dao = new AssignedOperationsDao();
    private final TextField txtSearch = new TextField();
    private final Label lblFeedback = new Label();
    private final TableView<AppointmentRow> tblAppointments = new TableView<>();

    public AppointmentModuleView(String actionId, String actionName) {
        setSpacing(16);
        setPadding(new Insets(24, 28, 28, 28));
        setStyle("-fx-background-color: #f3f6f3;");

        Label lblTitle = new Label("Lịch hẹn  /  " + actionName);
        lblTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");
        Label lblSubtitle = new Label("Theo dõi lịch chăm sóc, trạng thái và thay đổi thời gian hẹn.");
        lblSubtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #6e7c70;");
        getChildren().add(new VBox(4, lblTitle, lblSubtitle));

        tblAppointments.getColumns().addAll(
                column("Mã lịch hẹn", 180, AppointmentRow::id),
                column("Khách hàng", 145, AppointmentRow::customer),
                column("Thú cưng", 130, AppointmentRow::pet),
                column("Nhân viên", 145, AppointmentRow::employee),
                column("Ngày hẹn", 110, row -> row.date().toString()),
                column("Giờ hẹn", 90, row -> row.time().format(DateTimeFormatter.ofPattern("HH:mm"))),
                column("Trạng thái", 130, AppointmentRow::status),
                column("Ghi chú", 180, row -> value(row.note())));
        tblAppointments.setPlaceholder(new Label("Không có lịch hẹn phù hợp."));
        VBox.setVgrow(tblAppointments, Priority.ALWAYS);

        txtSearch.setPromptText("Mã lịch, khách hàng, thú cưng hoặc trạng thái");
        txtSearch.getStyleClass().add("search-field");
        txtSearch.setPrefWidth(330);
        txtSearch.setOnAction(event -> loadAppointments());
        Button btnSearch = button("Tìm kiếm", false);
        btnSearch.setOnAction(event -> loadAppointments());
        Button btnRefresh = button("Làm mới", false);
        btnRefresh.setOnAction(event -> {
            txtSearch.clear();
            loadAppointments();
        });
        HBox toolbar = new HBox(8, txtSearch, btnSearch, btnRefresh);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        toolbar.getChildren().add(spacer);

        if ("APT_CREATE".equals(actionId)) {
            Button btnCreateAppointment = button("Đặt lịch hẹn", true);
            btnCreateAppointment.setOnAction(event -> createAppointment());
            toolbar.getChildren().add(btnCreateAppointment);
        } else if ("APT_STATUS".equals(actionId)) {
            Button btnUpdateStatus = button("Cập nhật trạng thái", true);
            btnUpdateStatus.setOnAction(event -> updateStatus());
            toolbar.getChildren().add(btnUpdateStatus);
        } else if ("APT_RESCHEDULE".equals(actionId)) {
            Button btnReschedule = button("Dời lịch", true);
            btnReschedule.setOnAction(event -> rescheduleAppointment());
            Button btnCancelAppointment = button("Hủy lịch", false);
            btnCancelAppointment.setOnAction(event -> cancelAppointment());
            toolbar.getChildren().addAll(btnReschedule, btnCancelAppointment);
        }

        VBox card = new VBox(12, toolbar, lblFeedback, tblAppointments);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #e5ebe5; -fx-border-radius: 8px;");
        VBox.setVgrow(card, Priority.ALWAYS);
        getChildren().add(card);
        loadAppointments();
    }

    private void loadAppointments() {
        try {
            tblAppointments.setItems(FXCollections.observableArrayList(dao.findAppointments(txtSearch.getText())));
            lblFeedback.setText("Hiển thị " + tblAppointments.getItems().size() + " lịch hẹn.");
        } catch (RuntimeException exception) {
            tblAppointments.getItems().clear();
            lblFeedback.setText(exception.getMessage());
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

        Dialog<AppointmentForm> dlgAppointment = new Dialog<>();
        dlgAppointment.setTitle("Đặt lịch hẹn");
        ButtonType btnTypeSave = new ButtonType("Đặt lịch", ButtonBar.ButtonData.OK_DONE);
        dlgAppointment.getDialogPane().getButtonTypes().addAll(btnTypeSave, ButtonType.CANCEL);

        ComboBox<LookupOption> cboCustomer = new ComboBox<>(FXCollections.observableArrayList(customers));
        ComboBox<LookupOption> cboPet = new ComboBox<>();
        ComboBox<LookupOption> cboEmployee = new ComboBox<>(FXCollections.observableArrayList(employees));
        ComboBox<LookupOption> cboService = new ComboBox<>(FXCollections.observableArrayList(services));
        cboCustomer.setMaxWidth(Double.MAX_VALUE);
        cboPet.setMaxWidth(Double.MAX_VALUE);
        cboEmployee.setMaxWidth(Double.MAX_VALUE);
        cboService.setMaxWidth(Double.MAX_VALUE);
        cboCustomer.valueProperty().addListener((observable, oldValue, selected) -> {
            cboPet.getItems().clear();
            if (selected != null) {
                try {
                    cboPet.setItems(FXCollections.observableArrayList(dao.findPetOptions(selected.id())));
                    cboPet.getSelectionModel().selectFirst();
                } catch (RuntimeException exception) {
                    showAlert(Alert.AlertType.ERROR, "Không tải được thú cưng", exception.getMessage());
                }
            }
        });
        cboCustomer.getSelectionModel().selectFirst();
        cboEmployee.getSelectionModel().selectFirst();
        cboService.getSelectionModel().selectFirst();

        DatePicker dtpDate = new DatePicker(LocalDate.now());
        TextField txtTime = new TextField("09:00");
        txtTime.setPromptText("HH:mm");
        txtTime.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d{0,2}(:\\d{0,2})?") ? change : null));
        TextField txtWeight = new TextField("4.00");
        txtWeight.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d{0,3}([.,]\\d{0,2})?") ? change : null));
        TextArea txtNote = new TextArea();
        txtNote.setPromptText("Ghi chú (không bắt buộc)");
        txtNote.setPrefRowCount(2);

        GridPane gridFields = new GridPane();
        gridFields.setHgap(12);
        gridFields.setVgap(10);
        gridFields.addRow(0, new Label("Khách hàng"), cboCustomer);
        gridFields.addRow(1, new Label("Thú cưng"), cboPet);
        gridFields.addRow(2, new Label("Nhân viên"), cboEmployee);
        gridFields.addRow(3, new Label("Dịch vụ"), cboService);
        gridFields.addRow(4, new Label("Ngày hẹn"), dtpDate);
        gridFields.addRow(5, new Label("Giờ hẹn"), txtTime);
        gridFields.addRow(6, new Label("Cân nặng (kg)"), txtWeight);
        gridFields.addRow(7, new Label("Ghi chú"), txtNote);
        dlgAppointment.getDialogPane().setContent(gridFields);
        Node btnSave = dlgAppointment.getDialogPane().lookupButton(btnTypeSave);
        btnSave.addEventFilter(ActionEvent.ACTION, event -> {
            boolean invalidTime = !validTime(txtTime.getText());
            boolean invalidWeight = !validWeight(txtWeight.getText());
            if (cboCustomer.getValue() == null || cboPet.getValue() == null || cboEmployee.getValue() == null
                || cboService.getValue() == null || dtpDate.getValue() == null || dtpDate.getValue().isBefore(LocalDate.now())
                || invalidTime || invalidWeight || txtNote.getText().length() > 255) {
                showAlert(Alert.AlertType.WARNING, "Kiểm tra dữ liệu", "Chọn đủ thông tin; ngày, giờ, cân nặng hoặc ghi chú chưa hợp lệ.");
                event.consume();
            }
        });
        dlgAppointment.setResultConverter(btnType -> btnType == btnTypeSave
            ? new AppointmentForm(cboCustomer.getValue(), cboPet.getValue(), cboEmployee.getValue(), cboService.getValue(),
                dtpDate.getValue(), LocalTime.parse(txtTime.getText()), Double.parseDouble(txtWeight.getText().replace(',', '.')), txtNote.getText())
                : null);
        dlgAppointment.showAndWait().ifPresent(form -> {
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
        ChoiceDialog<String> dlgStatus = new ChoiceDialog<>(allowed.get(0), allowed);
        dlgStatus.setTitle("Cập nhật trạng thái");
        dlgStatus.setHeaderText(selected.id() + " · " + selected.status());
        dlgStatus.setContentText("Trạng thái mới:");
        dlgStatus.showAndWait().ifPresent(status -> runStatusUpdate(selected, status));
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
        Dialog<RescheduleForm> dlgReschedule = new Dialog<>();
        dlgReschedule.setTitle("Dời lịch hẹn");
        ButtonType btnTypeSave = new ButtonType("Lưu lịch mới", ButtonBar.ButtonData.OK_DONE);
        dlgReschedule.getDialogPane().getButtonTypes().addAll(btnTypeSave, ButtonType.CANCEL);
        DatePicker dtpDate = new DatePicker(selected.date());
        TextField txtTime = new TextField(selected.time().format(DateTimeFormatter.ofPattern("HH:mm")));
        txtTime.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d{0,2}(:\\d{0,2})?") ? change : null));
        GridPane gridFields = new GridPane();
        gridFields.setHgap(12);
        gridFields.setVgap(10);
        gridFields.addRow(0, new Label("Ngày mới"), dtpDate);
        gridFields.addRow(1, new Label("Giờ mới (HH:mm)"), txtTime);
        dlgReschedule.getDialogPane().setContent(gridFields);
        dlgReschedule.getDialogPane().lookupButton(btnTypeSave).addEventFilter(ActionEvent.ACTION, event -> {
            if (dtpDate.getValue() == null || dtpDate.getValue().isBefore(LocalDate.now()) || !validTime(txtTime.getText())) {
                showAlert(Alert.AlertType.WARNING, "Kiểm tra dữ liệu", "Chọn ngày không ở quá khứ và giờ theo định dạng HH:mm.");
                event.consume();
            }
        });
        dlgReschedule.setResultConverter(btnType -> btnType == btnTypeSave
            ? new RescheduleForm(dtpDate.getValue(), LocalTime.parse(txtTime.getText())) : null);
        dlgReschedule.showAndWait().ifPresent(form -> {
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
        AppointmentRow selected = tblAppointments.getSelectionModel().getSelectedItem();
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
        TableColumn<T, String> tblColumn = new TableColumn<>(title);
        tblColumn.setPrefWidth(width);
        tblColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return tblColumn;
    }

    private static Button button(String text, boolean primary) {
        Button btn = new Button(text);
        btn.getStyleClass().add(primary ? "btn-action-primary" : "btn-action-secondary");
        return btn;
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

