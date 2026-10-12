package com.petshop.view;

import com.petshop.controller.AuditController;
import com.petshop.controller.CrudController;
import com.petshop.util.TextSearch;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.css.PseudoClass;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public abstract class CrudListView<T, ID> extends VBox
        implements AppView, SearchResultAware {
    private static final PseudoClass SEARCH_MATCH =
            PseudoClass.getPseudoClass("search-match");

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
    private final SearchResultPanel<T> searchResultPanel;
    private final boolean allowAddEdit;
    private final boolean allowDeactivate;
    private List<T> loadedItems = List.of();
    private Set<ID> matchedIds = Set.of();
    private String keyword = "";
    private String pendingGlobalId;
    private boolean settingGlobalSearch;

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
        this.allowAddEdit = allowAddEdit;
        this.allowDeactivate = allowDeactivate;
        setPadding(new Insets(4, 0, 0, 0));

        Label heading = new Label(title);
        heading.getStyleClass().add("welcome-title");
        Label summary = new Label(description);
        summary.getStyleClass().add("welcome-sub");
        searchField.setPromptText("Tìm kiếm...");
        searchField.getStyleClass().add("search-field");
        Button clearSearch = new Button("×");
        clearSearch.getStyleClass().add("search-clear-button");
        clearSearch.setAccessibleText("Xóa từ khóa tìm kiếm");
        clearSearch.setOnAction(event -> searchField.clear());
        StackPane searchInput = new StackPane(searchField, clearSearch);
        StackPane.setAlignment(clearSearch, javafx.geometry.Pos.CENTER_RIGHT);
        searchField.setPadding(new Insets(8, 38, 8, 12));
        searchField.textProperty().addListener((observable, oldValue, value) -> {
            keyword = value == null ? "" : value.trim();
            if (!settingGlobalSearch) {
                pendingGlobalId = null;
            }
            searchDebounce.stop();
            if (keyword.isBlank()) {
                applySearchAndFilter();
            } else {
                searchDebounce.playFromStart();
            }
        });
        searchDebounce.setOnFinished(event -> applySearchAndFilter());

        filterBox.getItems().setAll(filterOptions());
        if (!filterBox.getItems().isEmpty()) {
            filterBox.setValue(filterBox.getItems().get(0));
        }
        filterBox.valueProperty().addListener(
                (observable, oldValue, value) -> applySearchAndFilter());
        HBox toolbar = new HBox(10, searchInput, filterBox);
        toolbar.getStyleClass().add("search-box");
        HBox.setHgrow(searchInput, Priority.ALWAYS);
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

        List<TableColumn<T, ?>> mainColumns = columns();
        table.getColumns().setAll(mainColumns);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Không có dữ liệu"));
        table.setRowFactory(ignored -> new TableRow<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                pseudoClassStateChanged(SEARCH_MATCH,
                        !empty && item != null && matchedIds.contains(idOf(item)));
            }
        });
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && showDetailsOnDoubleClick()) {
                T selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openReadOnlyDialog(selected);
                }
            }
        });
        searchResultPanel = new SearchResultPanel<>(
                mainColumns,
                allowAddEdit,
                allowDeactivate,
                showDetailsOnDoubleClick(),
                this::clearSearch,
                item -> {
                    table.getSelectionModel().select(item);
                    table.scrollTo(item);
                },
                this::openEditDialog,
                this::deactivateFromSearchResult,
                this::openReadOnlyDialog);
        feedback.getStyleClass().add("welcome-sub");
        feedback.setWrapText(true);
        getChildren().addAll(heading, summary, toolbar, searchResultPanel, feedback, table);
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

    protected boolean auditChangesInController() {
        return false;
    }

    protected boolean showDetailsOnDoubleClick() {
        return false;
    }

    protected void openReadOnlyDialog(T item) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Chi tiết");
        alert.setHeaderText(title);
        alert.setContentText(String.valueOf(item));
        alert.show();
    }

    protected boolean matches(T item, String keyword) {
        String[] fields = new String[table.getColumns().size()];
        for (int index = 0; index < table.getColumns().size(); index++) {
            TableColumn<T, ?> column = table.getColumns().get(index);
            javafx.beans.value.ObservableValue<?> value =
                    column.getCellObservableValue(item);
            fields[index] = value == null || value.getValue() == null
                    ? "" : String.valueOf(value.getValue());
        }
        return TextSearch.matchesAll(keyword, fields);
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

    @Override
    public final void showSearchResult(String id) {
        pendingGlobalId = Objects.requireNonNull(id);
        settingGlobalSearch = true;
        searchField.setText(id);
        settingGlobalSearch = false;
        keyword = id.trim();
        searchDebounce.stop();
        if (!loadedItems.isEmpty()) {
            applySearchAndFilter();
        }
    }

    @Override
    public final void showSearchKeyword(String query) {
        pendingGlobalId = null;
        searchField.setText(query == null ? "" : query);
    }

    protected final void refresh() {
        long id = requestId.incrementAndGet();
        Task<List<T>> task = new Task<>() {
            @Override
            protected List<T> call() {
                return List.copyOf(controller.findAll());
            }
        };
        task.setOnSucceeded(event -> {
            if (requestId.get() == id) {
                loadedItems = task.getValue();
                applySearchAndFilter();
            }
        });
        task.setOnFailed(event -> {
            if (requestId.get() == id) {
                Throwable cause = task.getException();
                showError(cause == null ? "Không thể tải dữ liệu." : cause.getMessage());
            }
        });
        Thread worker = new Thread(task, "crud-list-load");
        worker.setDaemon(true);
        worker.start();
    }

    private void clearSearch() {
        searchField.clear();
    }

    private void applySearchAndFilter() {
        List<T> visibleItems = List.copyOf(filter(loadedItems, selectedFilter()));
        table.getItems().setAll(visibleItems);
        if (keyword.isBlank()) {
            matchedIds = Set.of();
            searchResultPanel.hideResults();
        } else {
            List<T> matchedItems = visibleItems.stream()
                    .filter(item -> pendingGlobalId != null
                            ? Objects.equals(String.valueOf(idOf(item)), pendingGlobalId)
                            : matches(item, keyword))
                    .toList();
            Set<ID> ids = new HashSet<>();
            matchedItems.forEach(item -> ids.add(idOf(item)));
            matchedIds = Set.copyOf(ids);
            searchResultPanel.showResults(keyword, matchedItems);
            if (!matchedItems.isEmpty()) {
                table.scrollTo(matchedItems.get(0));
            }
        }
        table.refresh();
    }

    private String selectedFilter() {
        String selected = filterBox.getValue();
        return selected == null || selected.equals("Tất cả") ? "" : selected;
    }

    private void deactivateFromSearchResult(T item) {
        if (!canDeactivate(item)) {
            showError("Mục đã ngừng hoặc không thể ngừng.");
            return;
        }
        table.getSelectionModel().select(item);
        deactivateSelected();
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
                if (auditChangesInController()) {
                    return true;
                }
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

    @Override
    public final javafx.scene.Node getNode() {
        return this;
    }

    @Override
    public final String getTitle() {
        return title;
    }
}
