package com.petshop.view;

import com.petshop.controller.AppointmentController;
import com.petshop.controller.AuditController;
import com.petshop.controller.CustomerController;
import com.petshop.model.AppointmentData.Row;
import com.petshop.util.Formats;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public final class AppointmentView extends VBox implements AppView {
    private final AppointmentController controller;
    private final CustomerController customers;
    private final AuditController audit;
    private final DatePicker selectedDate = new DatePicker(LocalDate.now());
    private final TableView<Row> table = new TableView<>();
    private final Label feedback = new Label();
    private final AtomicLong refreshSequence = new AtomicLong();

    public AppointmentView(AppointmentController controller) {
        this(controller, null, null);
    }

    public AppointmentView(AppointmentController controller, CustomerController customers,
                           AuditController audit) {
        super(14);
        this.controller = Objects.requireNonNull(controller);
        this.customers = customers;
        this.audit = audit;
        setPadding(new Insets(4, 0, 0, 0));
        Label title = new Label("Lịch hẹn");
        title.getStyleClass().add("welcome-title");
        Label description = new Label(
                "UC012 · Tra cứu theo ngày, đặt lịch và quản lý trạng thái thực hiện.");
        description.getStyleClass().add("welcome-sub");
        selectedDate.valueProperty().addListener((observable, oldValue, value) -> {
            if (value != null) {
                refresh();
            }
        });
        Button create = new Button("Đặt lịch hẹn");
        create.getStyleClass().add("btn-create-invoice");
        create.setOnAction(event -> new AppointmentFormDialog(
                controller, customers, audit, selectedDate.getValue(), this::refresh));
        Button advance = new Button("Chuyển trạng thái");
        advance.getStyleClass().add("sidebar-btn");
        advance.setOnAction(event -> advanceSelected());
        Button cancel = new Button("Hủy lịch");
        cancel.getStyleClass().add("sidebar-btn");
        cancel.setOnAction(event -> cancelSelected());
        HBox toolbar = new HBox(10, new Label("Ngày hẹn"), selectedDate, create, advance, cancel);
        toolbar.getStyleClass().add("search-box");
        table.getColumns().setAll(
                column("Giờ", item -> Formats.time(item.time())),
                column("Mã lịch hẹn", Row::id),
                column("Khách hàng", item -> item.customerName() + " · " + item.customerId()),
                column("Thú cưng", item -> item.petName() + " · " + item.petId()),
                column("Dịch vụ", Row::services),
                column("Nhân viên", item -> item.employeeName() + " · " + item.employeeId()),
                column("Trạng thái", Row::status),
                column("Ghi chú", item -> item.note() == null ? "" : item.note()));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Không có dữ liệu"));
        feedback.getStyleClass().add("welcome-sub");
        feedback.setWrapText(true);
        getChildren().addAll(title, description, toolbar, feedback, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        refresh();
    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public String getTitle() {
        return "Lịch hẹn";
    }

    private void refresh() {
        LocalDate date = selectedDate.getValue();
        if (date == null) {
            return;
        }
        long sequence = refreshSequence.incrementAndGet();
        table.setDisable(true);
        Task<List<Row>> task = new Task<>() {
            @Override
            protected List<Row> call() {
                return controller.findByDate(date);
            }
        };
        task.setOnSucceeded(event -> {
            if (refreshSequence.get() != sequence) {
                return;
            }
            table.setDisable(false);
            table.getItems().setAll(task.getValue());
            table.setPlaceholder(new Label(task.getValue().isEmpty()
                    ? "Không có lịch hẹn trong ngày đã chọn." : "Không tìm thấy dữ liệu"));
            feedback.setText("Ngày " + Formats.date(date) + " · "
                    + task.getValue().size() + " lịch hẹn");
        });
        task.setOnFailed(event -> {
            if (refreshSequence.get() != sequence) {
                return;
            }
            table.setDisable(false);
            showError(task.getException() == null
                    ? "Không thể tải lịch hẹn." : task.getException().getMessage());
        });
        start(task, "appointment-list");
    }

    private void advanceSelected() {
        Row selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Hãy chọn lịch hẹn cần cập nhật.");
            return;
        }
        runStatusChange(selected, false);
    }

    private void cancelSelected() {
        Row selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Hãy chọn lịch hẹn cần hủy.");
            return;
        }
        if (!ConfirmDialog.confirmCancelAppointment()) {
            return;
        }
        runStatusChange(selected, true);
    }

    private void runStatusChange(Row selected, boolean cancel) {
        table.setDisable(true);
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                if (cancel) {
                    controller.cancel(selected);
                } else {
                    controller.advanceStatus(selected);
                }
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            table.setDisable(false);
            feedback.setText(cancel
                    ? "Đã chuyển lịch hẹn sang trạng thái Đã hủy."
                    : "Đã chuyển lịch hẹn sang trạng thái tiếp theo.");
            refresh();
        });
        task.setOnFailed(event -> {
            table.setDisable(false);
            showError(task.getException() == null
                    ? "Không thể cập nhật lịch hẹn." : task.getException().getMessage());
        });
        start(task, "appointment-status");
    }

    private TableColumn<Row, String> column(
            String title, Function<Row, String> value) {
        TableColumn<Row, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }

    private void showError(String message) {
        feedback.getStyleClass().remove("feedback-success");
        if (!feedback.getStyleClass().contains("feedback-error")) {
            feedback.getStyleClass().add("feedback-error");
        }
        feedback.setText(message == null || message.isBlank()
                ? "Không thể hoàn tất thao tác." : message);
    }

    private void start(Task<?> task, String threadName) {
        Thread worker = new Thread(task, threadName);
        worker.setDaemon(true);
        worker.start();
    }
}
