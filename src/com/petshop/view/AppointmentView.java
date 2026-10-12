package com.petshop.view;

import com.petshop.controller.AppointmentController;
import com.petshop.controller.AuditController;
import com.petshop.controller.CustomerController;
import com.petshop.model.AppointmentData.Row;
import com.petshop.util.Formats;
import com.petshop.util.TextSearch;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public final class AppointmentView extends VBox implements AppView, SearchResultAware {
    private final AppointmentController controller;
    private final CustomerController customers;
    private final AuditController audit;
    private final DatePicker selectedDate = new DatePicker(LocalDate.now());
    private final TableView<Row> table = new TableView<>();
    private final TextField search = new TextField();
    private final Label feedback = new Label();
    private final AtomicLong refreshSequence = new AtomicLong();
    private final PauseTransition debounce = new PauseTransition(Duration.millis(300));
    private final SearchResultPanel<Row> searchResults;
    private List<Row> loadedRows = List.of();
    private Set<String> matchedIds = Set.of();
    private String keyword = "";
    private String pendingGlobalId;
    private boolean settingGlobalSearch;

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
        search.setPromptText("Tìm kiếm lịch hẹn...");
        search.getStyleClass().add("search-field");
        Button clearSearch = new Button("×");
        clearSearch.getStyleClass().add("search-clear-button");
        clearSearch.setAccessibleText("Xóa từ khóa tìm kiếm");
        clearSearch.setOnAction(event -> search.clear());
        StackPane searchInput = new StackPane(search, clearSearch);
        StackPane.setAlignment(clearSearch, javafx.geometry.Pos.CENTER_RIGHT);
        search.setPadding(new Insets(8, 38, 8, 12));
        search.textProperty().addListener((observable, oldValue, value) -> {
            keyword = value == null ? "" : value.trim();
            if (!settingGlobalSearch) {
                pendingGlobalId = null;
            }
            debounce.stop();
            if (keyword.isBlank()) {
                applySearch();
            } else {
                debounce.playFromStart();
            }
        });
        debounce.setOnFinished(event -> applySearch());
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
        List<TableColumn<Row, ?>> appointmentColumns = List.of(
                column("Giờ", item -> Formats.time(item.time())),
                column("Mã lịch hẹn", Row::id),
                column("Khách hàng", item -> item.customerName() + " · " + item.customerId()),
                column("Thú cưng", item -> item.petName() + " · " + item.petId()),
                column("Dịch vụ", Row::services),
                column("Nhân viên", item -> item.employeeName() + " · " + item.employeeId()),
                column("Trạng thái", Row::status),
                column("Ghi chú", item -> item.note() == null ? "" : item.note()));
        table.getColumns().setAll(appointmentColumns);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Không có dữ liệu"));
        table.setRowFactory(ignored -> new TableRow<>() {
            @Override
            protected void updateItem(Row item, boolean empty) {
                super.updateItem(item, empty);
                pseudoClassStateChanged(PseudoClass.getPseudoClass("search-match"),
                        !empty && item != null && matchedIds.contains(item.id()));
            }
        });
        searchResults = new SearchResultPanel<>(appointmentColumns, false, false,
                () -> search.clear(),
                item -> {
                    table.getSelectionModel().select(item);
                    table.scrollTo(item);
                },
                item -> { },
                item -> { },
                this::showReadOnly);
        feedback.getStyleClass().add("welcome-sub");
        feedback.setWrapText(true);
        toolbar.getChildren().add(0, searchInput);
        HBox.setHgrow(searchInput, Priority.ALWAYS);
        getChildren().addAll(title, description, toolbar, searchResults, feedback, table);
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

    @Override
    public void showSearchResult(String id) {
        pendingGlobalId = Objects.requireNonNull(id);
        settingGlobalSearch = true;
        search.setText(id);
        settingGlobalSearch = false;
        keyword = id.trim();
        debounce.stop();
        Task<java.util.Optional<Row>> task = new Task<>() {
            @Override
            protected java.util.Optional<Row> call() {
                return controller.findById(id);
            }
        };
        task.setOnSucceeded(event -> task.getValue().ifPresentOrElse(item -> {
            if (!item.date().equals(selectedDate.getValue())) {
                selectedDate.setValue(item.date());
            } else {
                loadedRows = List.of(item);
                applySearch();
            }
        }, this::applySearch));
        task.setOnFailed(event -> showError(task.getException() == null
                ? "Không thể tải lịch hẹn được chọn."
                : task.getException().getMessage()));
        start(task, "appointment-search-result");
    }

    @Override
    public void showSearchKeyword(String query) {
        pendingGlobalId = null;
        search.setText(query == null ? "" : query);
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
            loadedRows = task.getValue();
            applySearch();
            table.setPlaceholder(new Label(loadedRows.isEmpty()
                    ? "Không có lịch hẹn trong ngày đã chọn." : "Không tìm thấy dữ liệu"));
            feedback.setText("Ngày " + Formats.date(date) + " · "
                    + loadedRows.size() + " lịch hẹn");
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

    private void applySearch() {
        table.getItems().setAll(loadedRows);
        if (keyword.isBlank()) {
            matchedIds = Set.of();
            searchResults.hideResults();
        } else {
            List<Row> matches = loadedRows.stream()
                    .filter(item -> pendingGlobalId != null
                            ? item.id().equals(pendingGlobalId)
                            : matches(item, keyword))
                    .toList();
            matchedIds = matches.stream().map(Row::id)
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
            searchResults.showResults(keyword, matches);
            if (!matches.isEmpty()) {
                table.scrollTo(matches.get(0));
            }
        }
        table.refresh();
    }

    private boolean matches(Row item, String query) {
        return TextSearch.matchesAll(query, item.id(), item.customerName(), item.petName());
    }

    private void showReadOnly(Row item) {
        javafx.scene.control.Alert details = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        details.setTitle("Chi tiết lịch hẹn");
        details.setHeaderText(item.id());
        details.setContentText("Khách hàng: " + item.customerName() + " · " + item.customerId()
                + "\nThú cưng: " + item.petName() + " · " + item.petId()
                + "\nNhân viên: " + item.employeeName() + " · " + item.employeeId()
                + "\nNgày giờ: " + Formats.date(item.date()) + " " + Formats.time(item.time())
                + "\nDịch vụ: " + item.services()
                + "\nTrạng thái: " + item.status()
                + "\nGhi chú: " + (item.note() == null ? "" : item.note()));
        details.show();
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
