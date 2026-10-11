package com.petshop.view;

import com.petshop.controller.AuditController;
import com.petshop.controller.CrudController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Objects;

public abstract class CrudView<T, ID> extends VBox implements AppView {
    private final String title;
    private final CrudController<T, ID> controller;
    private final AuditController auditController;
    private final String targetTable;
    private final TableView<T> table = new TableView<>();
    private final ObservableList<T> rows = FXCollections.observableArrayList();
    private final TextField searchField = new TextField();
    private final ComboBox<String> filterBox = new ComboBox<>();
    private final VBox formPanel = new VBox(12);
    private final Label feedback = new Label();
    private String keyword = "";
    private T editingItem;

    protected CrudView(
            String title,
            String description,
            String targetTable,
            CrudController<T, ID> controller,
            AuditController auditController) {
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
        configureToolbar();

        table.getColumns().setAll(columns());
        table.setItems(rows);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Không có dữ liệu"));

        formPanel.getStyleClass().add("card-panel");
        formPanel.setVisible(false);
        formPanel.setManaged(false);
        BorderPane workspace = new BorderPane();
        workspace.setCenter(table);
        workspace.setRight(formPanel);
        BorderPane.setMargin(formPanel, new Insets(0, 0, 0, 16));
        VBox.setVgrow(workspace, Priority.ALWAYS);
        feedback.getStyleClass().add("welcome-sub");
        feedback.setWrapText(true);
        getChildren().addAll(heading, summary, toolbar, feedback, workspace);
        refresh();
    }

    private HBox toolbar;

    private void configureToolbar() {
        searchField.setPromptText("Tìm kiếm...");
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((observable, oldValue, value) -> {
            keyword = value == null ? "" : value.trim();
            refresh();
            onSearch(keyword);
        });
        filterBox.getItems().setAll(filterOptions());
        if (!filterBox.getItems().isEmpty()) {
            filterBox.setValue(filterBox.getItems().get(0));
        }
        filterBox.valueProperty().addListener((observable, oldValue, value) -> {
            refresh();
            onSearch(keyword);
        });
        Button add = new Button("Thêm");
        add.getStyleClass().add("btn-create-invoice");
        add.setOnAction(event -> openForm(null));
        Button edit = new Button("Sửa");
        edit.getStyleClass().add("sidebar-btn");
        edit.setOnAction(event -> {
            T selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Hãy chọn một dòng cần sửa.");
            } else {
                openForm(selected);
            }
        });
        Button deactivate = new Button("Ngừng");
        deactivate.getStyleClass().add("sidebar-btn");
        deactivate.setOnAction(event -> deactivateSelected());
        toolbar = new HBox(10, searchField, filterBox, add, edit, deactivate);
        toolbar.getStyleClass().add("search-box");
        HBox.setHgrow(searchField, Priority.ALWAYS);
    }

    private void openForm(T existing) {
        editingItem = existing;
        formPanel.getChildren().clear();
        Label formTitle = new Label(existing == null ? "Thêm mới" : "Cập nhật");
        formTitle.getStyleClass().add("card-title");
        Node form = createForm(existing);
        Button save = new Button("Lưu");
        save.getStyleClass().add("btn-create-invoice");
        save.setOnAction(event -> saveForm());
        Button cancel = new Button("Hủy");
        cancel.getStyleClass().add("sidebar-btn");
        cancel.setOnAction(event -> cancelForm());
        HBox actions = new HBox(8, save, cancel);
        formPanel.getChildren().addAll(formTitle, form, actions);
        formPanel.setVisible(true);
        formPanel.setManaged(true);
        feedback.setText("");
    }

    private void saveForm() {
        T item = extractFromForm();
        List<String> errors = validate(item);
        if (!errors.isEmpty()) {
            showError(String.join("\n", errors));
            return;
        }
        boolean adding = editingItem == null;
        try {
            if (adding) {
                controller.add(item);
            } else {
                controller.update(item);
            }
        } catch (IllegalArgumentException | IllegalStateException exception) {
            showError(exception.getMessage());
            return;
        }
        refresh();
        closeForm();
        showSuccess(adding ? "Đã thêm dữ liệu." : "Đã cập nhật dữ liệu.");
        try {
            auditAction(adding ? "Thêm" : "Sửa", item);
        } catch (IllegalStateException exception) {
            showError("Đã lưu dữ liệu nhưng không ghi được nhật ký: " + exception.getMessage());
        }
    }

    private void cancelForm() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận hủy");
        alert.setHeaderText("Đóng biểu mẫu và bỏ các thay đổi chưa lưu?");
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().filter(ButtonType.YES::equals).isPresent()) {
            closeForm();
        }
    }

    private void closeForm() {
        editingItem = null;
        formPanel.getChildren().clear();
        formPanel.setVisible(false);
        formPanel.setManaged(false);
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
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận ngừng");
        alert.setHeaderText("Bạn có chắc muốn ngừng mục đã chọn không?");
        alert.setContentText("Bạn có thể không hoàn tác được thao tác này.");
        if (alert.showAndWait().filter(ButtonType.OK::equals).isEmpty()) {
            return;
        }
        try {
            controller.deactivate(idOf(selected));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            showError(exception.getMessage());
            return;
        }
        refresh();
        showSuccess("Đã ngừng dữ liệu.");
        try {
            auditAction("Ngừng", selected);
        } catch (IllegalStateException exception) {
            showError("Đã ngừng dữ liệu nhưng không ghi được nhật ký: " + exception.getMessage());
        }
    }

    private void refresh() {
        List<T> found = keyword.isBlank() ? controller.findAll() : controller.search(keyword);
        List<T> filtered = filter(found, selectedFilter());
        rows.setAll(filtered);
        table.setPlaceholder(new Label(keyword.isBlank() && selectedFilter().isBlank()
                ? "Không có dữ liệu" : "Không tìm thấy dữ liệu"));
    }

    private String selectedFilter() {
        String selected = filterBox.getValue();
        return selected == null || selected.equals("Tất cả") ? "" : selected;
    }

    protected List<String> filterOptions() {
        return List.of("Tất cả");
    }

    protected List<T> filter(List<T> values, String selectedFilter) {
        return values;
    }

    protected void onSearch(String keyword) {
    }

    protected abstract List<TableColumn<T, ?>> columns();

    protected abstract Node createForm(T existing);

    protected abstract List<String> validate(T item);

    protected abstract T extractFromForm();

    protected abstract ID idOf(T item);

    protected abstract boolean canDeactivate(T item);

    protected void auditAction(String action, T item) {
        auditController.log(action, targetTable, String.valueOf(idOf(item)), null);
    }

    protected final TableView<T> table() {
        return table;
    }

    protected final void showSuccess(String message) {
        feedback.getStyleClass().remove("feedback-error");
        if (!feedback.getStyleClass().contains("feedback-success")) {
            feedback.getStyleClass().add("feedback-success");
        }
        feedback.setText(message);
    }

    protected final void showError(String message) {
        feedback.getStyleClass().remove("feedback-success");
        if (!feedback.getStyleClass().contains("feedback-error")) {
            feedback.getStyleClass().add("feedback-error");
        }
        feedback.setText(message == null || message.isBlank() ? "Đã xảy ra lỗi." : message);
    }

    @Override
    public final Node getNode() {
        return this;
    }

    @Override
    public final String getTitle() {
        return title;
    }
}
