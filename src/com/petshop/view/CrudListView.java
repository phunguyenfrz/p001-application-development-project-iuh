package com.petshop.view;

import com.petshop.controller.AuditController;
import com.petshop.controller.CrudController;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public abstract class CrudListView<T, ID> extends VBox implements AppView {
    private final String title;
    private final String targetTable;
    private final CrudController<T, ID> controller;
    private final AuditController auditController;
    private final TableView<T> table = new TableView<>();
    private final TextField searchField = new TextField();
    private final ComboBox<String> filterBox = new ComboBox<>();
    private final Label feedback = new Label();
    private final PauseTransition searchDebounce = new PauseTransition(Duration.millis(300));
    private final AtomicLong requestId = new AtomicLong();
    private String keyword = "";

    protected CrudListView(
            String title,
            String description,
            String targetTable,
            CrudController<T, ID> controller,
            AuditController auditController,
            boolean allowAddEditDeactivate) {
        this(title, description, targetTable, controller, auditController,
                allowAddEditDeactivate, allowAddEditDeactivate);
    }

    protected CrudListView(
            String title,
            String description,
            String targetTable,
            CrudController<T, ID> controller,
            AuditController auditController,
            boolean allowAddEdit,
            boolean allowDeactivate) {
        super(14);
        this.title = Objects.requireNonNull(title);
        this.targetTable = Objects.requireNonNull(targetTable);
        this.controller = Objects.requireNonNull(controller);
        this.auditController = Objects.requireNonNull(auditController);
        setPadding(new Insets(4, 0, 0, 0));
        Label heading = new Label(title);
        heading.getStyleClass().add("welcome-title");
        Label summary = new Label(description);
        summary.getStyleClass().add("welcome-sub");
        searchField.setPromptText("Tìm kiếm...");
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((observable, oldValue, value) -> {
            keyword = value == null ? "" : value.trim();
            searchDebounce.stop();
            if (keyword.isBlank()) {
                refresh();
            } else {
                searchDebounce.playFromStart();
            }
        });
        searchDebounce.setOnFinished(event -> refresh());
        filterBox.getItems().setAll(filterOptions());
        if (!filterBox.getItems().isEmpty()) {
            filterBox.setValue(filterBox.getItems().get(0));
        }
        filterBox.valueProperty().addListener((observable, oldValue, value) -> refresh());
        javafx.scene.layout.HBox toolbar = new javafx.scene.layout.HBox(10, searchField, filterBox);
        toolbar.getStyleClass().add("search-box");
        javafx.scene.layout.HBox.setHgrow(searchField, Priority.ALWAYS);
        if (allowAddEdit) {
            Button add = new Button("Thêm");
            add.getStyleClass().add("btn-create-invoice");
            add.setOnAction(event -> openAddDialog());
            Button edit = new Button("Sửa");
            edit.getStyleClass().add("sidebar-btn");
            edit.setOnAction(event -> {
                T selected = table.getSelectionModel().getSelectedItem();
                if (selected == null) {
                    showError("Hãy chọn một dòng cần sửa.");
                } else {
                    openEditDialog(selected);
                }
            });
            toolbar.getChildren().addAll(add, edit);
            if (allowDeactivate) {
                Button deactivate = new Button("Ngừng");
                deactivate.getStyleClass().add("sidebar-btn");
                deactivate.setOnAction(event -> deactivateSelected());
                toolbar.getChildren().add(deactivate);
            }
        }
        table.getColumns().setAll(columns());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Không có dữ liệu"));
        feedback.getStyleClass().add("welcome-sub");
        feedback.setWrapText(true);
        getChildren().addAll(heading, summary, toolbar, feedback, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        refresh();
    }

    protected abstract List<TableColumn<T, ?>> columns();

    protected abstract void openAddDialog();

    protected abstract void openEditDialog(T item);

    protected abstract ID idOf(T item);

    protected boolean canDeactivate(T item) {
        return true;
    }

    protected List<String> filterOptions() {
        return List.of("Tất cả");
    }

    protected List<T> filter(List<T> values, String selectedFilter) {
        return values;
    }

    protected void showSuccess(String message) {
        feedback.getStyleClass().remove("feedback-error");
        if (!feedback.getStyleClass().contains("feedback-success")) {
            feedback.getStyleClass().add("feedback-success");
        }
        feedback.setText(message);
    }

    protected void showError(String message) {
        feedback.getStyleClass().remove("feedback-success");
        if (!feedback.getStyleClass().contains("feedback-error")) {
            feedback.getStyleClass().add("feedback-error");
        }
        feedback.setText(message == null || message.isBlank() ? "Đã xảy ra lỗi." : message);
    }

    protected final void refresh() {
        long id = requestId.incrementAndGet();
        String query = keyword;
        String filter = selectedFilter();
        Task<List<T>> task = new Task<>() {
            @Override
            protected List<T> call() {
                List<T> found = query.isBlank()
                        ? controller.findAll() : controller.search(query);
                return List.copyOf(filter(found, filter));
            }
        };
        task.setOnSucceeded(event -> {
            if (requestId.get() == id) {
                table.getItems().setAll(task.getValue());
                table.setPlaceholder(new Label(query.isBlank() && filter.isBlank()
                        ? "Không có dữ liệu" : "Không tìm thấy dữ liệu"));
            }
        });
        task.setOnFailed(event -> {
            if (requestId.get() == id) {
                Throwable cause = task.getException();
                showError(cause == null ? "Không thể tải dữ liệu."
                        : cause.getMessage());
            }
        });
        Thread worker = new Thread(task, "crud-list-search");
        worker.setDaemon(true);
        worker.start();
    }

    private void deactivateSelected() {
        T selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Hãy chọn một dòng cần ngừng.");
            return;
        }
        if (!canDeactivate(selected)) {
            showError("Mục đã ngừng hoặc không thể ngừng.");
            return;
        }
        if (!ConfirmDialog.confirmDeactivate()) {
            return;
        }
        ID selectedId = idOf(selected);
        setDisable(true);
        Task<Boolean> deactivateTask = new Task<>() {
            @Override
            protected Boolean call() {
                controller.deactivate(selectedId);
                try {
                    auditController.log("Ngừng", targetTable, String.valueOf(selectedId), null);
                    return true;
                } catch (RuntimeException exception) {
                    return false;
                }
            }
        };
        deactivateTask.setOnSucceeded(event -> {
            setDisable(false);
            refresh();
            showSuccess(deactivateTask.getValue()
                    ? "Đã ngừng dữ liệu."
                    : "Đã ngừng dữ liệu nhưng không ghi được nhật ký thao tác.");
        });
        deactivateTask.setOnFailed(event -> {
            setDisable(false);
            Throwable failure = deactivateTask.getException();
            showError(failure == null ? "Không thể ngừng dữ liệu." : failure.getMessage());
        });
        Thread worker = new Thread(deactivateTask, "crud-deactivate");
        worker.setDaemon(true);
        worker.start();
    }

    private String selectedFilter() {
        String selected = filterBox.getValue();
        return selected == null || selected.equals("Tất cả") ? "" : selected;
    }

    @Override
    public final javafx.scene.Node getNode() {
        return this;
    }

    @Override
    public final String getTitle() {
        return title;
    }
}
