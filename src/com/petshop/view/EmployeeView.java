package com.petshop.view;

import com.petshop.controller.AuditController;
import com.petshop.controller.EmployeeController;
import com.petshop.model.Employee;
import com.petshop.model.Role;
import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public final class EmployeeView extends VBox implements AppView {
    private final EmployeeController controller;
    private final AuditController auditController;
    private final TableView<Employee> table = new TableView<>();
    private final TextField search = new TextField();
    private final Label feedback = new Label();
    private final PauseTransition debounce = new PauseTransition(Duration.millis(300));
    private final AtomicLong requestSequence = new AtomicLong();
    private String keyword = "";

    public EmployeeView(EmployeeController controller, AuditController auditController) {
        super(14);
        this.controller = Objects.requireNonNull(controller);
        this.auditController = Objects.requireNonNull(auditController);
        setPadding(new Insets(4, 0, 0, 0));
        Label title = new Label("Nhân viên");
        title.getStyleClass().add("welcome-title");
        Label description = new Label(
                "UC002 · Quản lý hồ sơ nhân viên, tài khoản đăng nhập và vai trò.");
        description.getStyleClass().add("welcome-sub");
        search.setPromptText("Tìm theo mã, họ tên, số điện thoại hoặc tài khoản...");
        search.getStyleClass().add("search-field");
        search.textProperty().addListener((observable, oldValue, value) -> {
            keyword = value == null ? "" : value.trim();
            debounce.stop();
            if (keyword.isBlank()) {
                refresh();
            } else {
                debounce.playFromStart();
            }
        });
        debounce.setOnFinished(event -> refresh());
        HBox toolbar = new HBox(10, search);
        toolbar.getStyleClass().add("search-box");
        HBox.setHgrow(search, Priority.ALWAYS);
        Button add = action("Thêm nhân viên", "btn-create-invoice");
        add.setOnAction(event -> openForm(null));
        Button edit = action("Sửa", "sidebar-btn");
        edit.setOnAction(event -> {
            Employee selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Hãy chọn nhân viên cần sửa.");
            } else {
                openForm(selected);
            }
        });
        Button deactivate = action("Ngừng", "sidebar-btn");
        deactivate.setOnAction(event -> deactivateSelected());
        toolbar.getChildren().addAll(add, edit, deactivate);
        table.getColumns().setAll(
                column("Mã", Employee::id),
                column("Họ tên", Employee::fullName),
                column("Số điện thoại", Employee::phone),
                column("Chức vụ", employee -> employee.role().getDisplayName()),
                column("Tài khoản", employee -> employee.username() == null
                        ? "Chưa cấp" : employee.username()),
                column("Trạng thái", employee -> employee.working()
                        ? "Đang làm" : "Đã nghỉ việc"));
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
        return "Nhân viên";
    }

    private void openForm(Employee existing) {
        new EmployeeFormDialog(controller, auditController, existing, this::refresh);
    }

    private void deactivateSelected() {
        Employee selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Hãy chọn nhân viên cần ngừng.");
            return;
        }
        if (!selected.working()) {
            showError("Nhân viên này đã nghỉ việc.");
            return;
        }
        if (!ConfirmDialog.confirmDeactivate()) {
            return;
        }
        setDisable(true);
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                controller.deactivate(selected.id());
                try {
                    auditController.log("Ngừng", "NhanVien", selected.id(), null);
                    return true;
                } catch (RuntimeException exception) {
                    return false;
                }
            }
        };
        task.setOnSucceeded(event -> {
            setDisable(false);
            refresh();
            if (task.getValue()) {
                showSuccess("Đã ngừng nhân viên và tài khoản đăng nhập.");
            } else {
                showError("Nhân viên đã được ngừng nhưng không ghi được nhật ký thao tác.");
            }
        });
        task.setOnFailed(event -> {
            setDisable(false);
            showError(message(task.getException(), "Không thể ngừng nhân viên."));
        });
        start(task, "employee-deactivate");
    }

    private void refresh() {
        long requestId = requestSequence.incrementAndGet();
        String query = keyword;
        Task<List<Employee>> task = new Task<>() {
            @Override
            protected List<Employee> call() {
                return query.isBlank() ? controller.findAll() : controller.search(query);
            }
        };
        task.setOnSucceeded(event -> {
            if (requestSequence.get() == requestId) {
                table.getItems().setAll(task.getValue());
                table.setPlaceholder(new Label(query.isBlank()
                        ? "Không có dữ liệu" : "Không tìm thấy dữ liệu"));
            }
        });
        task.setOnFailed(event -> {
            if (requestSequence.get() == requestId) {
                showError(message(task.getException(), "Không thể tải danh sách nhân viên."));
            }
        });
        start(task, "employee-search");
    }

    private Button action(String text, String style) {
        Button button = new Button(text);
        button.getStyleClass().add(style);
        return button;
    }

    private TableColumn<Employee, String> column(
            String heading, Function<Employee, String> value) {
        TableColumn<Employee, String> column = new TableColumn<>(heading);
        column.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }

    private void showSuccess(String text) {
        feedback.getStyleClass().remove("feedback-error");
        if (!feedback.getStyleClass().contains("feedback-success")) {
            feedback.getStyleClass().add("feedback-success");
        }
        feedback.setText(text);
    }

    private void showError(String text) {
        feedback.getStyleClass().remove("feedback-success");
        if (!feedback.getStyleClass().contains("feedback-error")) {
            feedback.getStyleClass().add("feedback-error");
        }
        feedback.setText(text);
    }

    private String message(Throwable cause, String fallback) {
        return cause == null || cause.getMessage() == null
                ? fallback : cause.getMessage();
    }

    private void start(Task<?> task, String threadName) {
        Thread worker = new Thread(task, threadName);
        worker.setDaemon(true);
        worker.start();
    }
}
