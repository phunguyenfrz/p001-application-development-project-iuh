package com.petshop.view;

import com.petshop.controller.AuditController;
import com.petshop.controller.ShiftController;
import com.petshop.model.ShiftAssignment;
import com.petshop.model.User;
import com.petshop.util.Formats;
import com.petshop.util.TextSearch;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
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
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

public final class ShiftScheduleView extends VBox implements AppView, SearchResultAware {
    private record Choices(List<ShiftAssignment.Choice> employees,
                           List<ShiftAssignment.Choice> shifts) {
    }

    private final ShiftController controller;
    private final AuditController audit;
    private final User user;
    private final TableView<ShiftAssignment> table = new TableView<>();
    private final TextField search = new TextField();
    private final DatePicker from = new DatePicker(LocalDate.now()
            .with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)));
    private final DatePicker through = new DatePicker(from.getValue().plusDays(6));
    private final Label feedback = new Label();
    private final PauseTransition debounce = new PauseTransition(Duration.millis(300));
    private final SearchResultPanel<ShiftAssignment> searchResults;
    private List<ShiftAssignment> loadedRows = List.of();
    private Set<String> matchedIds = Set.of();
    private String keyword = "";
    private String pendingGlobalId;
    private boolean settingGlobalSearch;

    public ShiftScheduleView(ShiftController controller, AuditController audit, User user) {
        super(12);
        this.controller = controller;
        this.audit = audit;
        this.user = user;
        getStyleClass().add("module-view");
        Label title = new Label("Phân ca trực");
        title.getStyleClass().add("welcome-title");
        Label description = new Label("UC003 · Xem lịch tuần/tháng; chỉ thêm, sửa hoặc xóa ca chưa diễn ra.");
        description.getStyleClass().add("welcome-sub");
        ComboBox<String> period = new ComboBox<>();
        period.getItems().setAll("Tuần", "Tháng");
        period.setValue("Tuần");
        period.setOnAction(event -> setPeriod(period.getValue()));
        search.setPromptText("Tìm nhân viên, ngày hoặc ca...");
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
        Button load = new Button("Tra cứu");
        load.setOnAction(event -> refresh());
        Button add = new Button("Thêm ca");
        add.setOnAction(event -> edit(null));
        Button edit = new Button("Sửa ca");
        edit.setOnAction(event -> edit(table.getSelectionModel().getSelectedItem()));
        Button delete = new Button("Xóa ca");
        delete.setOnAction(event -> deleteSelected());
        List<TableColumn<ShiftAssignment, ?>> shiftColumns = List.of(
                column("Ngày", value -> Formats.date(value.workDate())),
                column("Ca", ShiftAssignment::shiftName),
                column("Thời gian", value -> Formats.time(value.startsAt())
                        + " - " + Formats.time(value.endsAt())),
                column("Nhân viên", ShiftAssignment::employeeName),
                column("Trạng thái", ShiftAssignment::status));
        table.getColumns().setAll(shiftColumns);
        table.setPlaceholder(new Label("Không có dữ liệu"));
        table.setRowFactory(ignored -> new TableRow<>() {
            @Override
            protected void updateItem(ShiftAssignment item, boolean empty) {
                super.updateItem(item, empty);
                pseudoClassStateChanged(PseudoClass.getPseudoClass("search-match"),
                        !empty && item != null && matchedIds.contains(item.id()));
            }
        });
        searchResults = new SearchResultPanel<>(shiftColumns, true, false,
                () -> search.clear(),
                item -> {
                    table.getSelectionModel().select(item);
                    table.scrollTo(item);
                },
                this::edit,
                item -> { },
                this::showReadOnly);
        HBox toolbar = new HBox(8, period, from, through, load, add, edit, delete);
        toolbar.getChildren().add(0, searchInput);
        HBox.setHgrow(searchInput, Priority.ALWAYS);
        feedback.getStyleClass().add("feedback-error");
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
        return "Phân ca trực";
    }

    @Override
    public void showSearchResult(String id) {
        pendingGlobalId = Objects.requireNonNull(id);
        settingGlobalSearch = true;
        search.setText(id);
        settingGlobalSearch = false;
        keyword = id.trim();
        debounce.stop();
        if (!loadedRows.isEmpty()) {
            applySearch();
        }
    }

    @Override
    public void showSearchKeyword(String query) {
        pendingGlobalId = null;
        search.setText(query == null ? "" : query);
    }

    private void setPeriod(String mode) {
        LocalDate date = from.getValue() == null ? LocalDate.now() : from.getValue();
        if ("Tháng".equals(mode)) {
            from.setValue(date.withDayOfMonth(1));
            through.setValue(date.with(TemporalAdjusters.lastDayOfMonth()));
        } else {
            from.setValue(date.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)));
            through.setValue(from.getValue().plusDays(6));
        }
    }

    private void refresh() {
        LocalDate start = from.getValue();
        LocalDate end = through.getValue();
        runTask(() -> controller.findBetween(start, end), rows -> {
            loadedRows = rows;
            applySearch();
            feedback.setText("");
        });
    }

    private void applySearch() {
        table.getItems().setAll(loadedRows);
        if (keyword.isBlank()) {
            matchedIds = Set.of();
            searchResults.hideResults();
        } else {
            List<ShiftAssignment> matches = loadedRows.stream()
                    .filter(item -> pendingGlobalId != null
                            ? item.id().equals(pendingGlobalId)
                            : matches(item, keyword))
                    .toList();
            matchedIds = matches.stream().map(ShiftAssignment::id)
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
            searchResults.showResults(keyword, matches);
            if (!matches.isEmpty()) {
                table.scrollTo(matches.get(0));
            }
        }
        table.refresh();
    }

    private boolean matches(ShiftAssignment item, String query) {
        return TextSearch.matchesAll(query, item.employeeName(),
                Formats.date(item.workDate()), item.workDate().toString(), item.shiftName());
    }

    private void showReadOnly(ShiftAssignment item) {
        javafx.scene.control.Alert details = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        details.setTitle("Chi tiết ca trực");
        details.setHeaderText(item.employeeName() + " · " + item.shiftName());
        details.setContentText("Ngày: " + Formats.date(item.workDate())
                + "\nThời gian: " + Formats.time(item.startsAt())
                + " - " + Formats.time(item.endsAt())
                + "\nTrạng thái: " + item.status());
        details.show();
    }

    private void edit(ShiftAssignment existing) {
        if (existing != null && existing.workDate().isBefore(LocalDate.now())) {
            showError("Không thể sửa ca đã diễn ra.");
            return;
        }
        runTask(() -> new Choices(controller.activeEmployees(), controller.activeShifts()),
                choices -> openAssignmentDialog(existing, choices));
    }

    private void openAssignmentDialog(ShiftAssignment existing, Choices choices) {
        if (choices.employees().isEmpty() || choices.shifts().isEmpty()) {
            showError("Cần có nhân viên đang làm và ca đang áp dụng trước khi phân ca.");
            return;
        }
        ComboBox<ShiftAssignment.Choice> employee = new ComboBox<>();
        employee.getItems().setAll(choices.employees());
        ComboBox<ShiftAssignment.Choice> shift = new ComboBox<>();
        shift.getItems().setAll(choices.shifts());
        DatePicker date = new DatePicker(existing == null ? LocalDate.now() : existing.workDate());
        if (existing != null) {
            employee.getSelectionModel().select(find(choices.employees(), existing.employeeId()));
            shift.getSelectionModel().select(find(choices.shifts(), existing.shiftId()));
        } else {
            employee.getSelectionModel().selectFirst();
            shift.getSelectionModel().selectFirst();
        }
        javafx.scene.control.Dialog<ButtonType> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle(existing == null ? "Thêm ca trực" : "Sửa ca trực");
        ButtonType save = new ButtonType("Lưu", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(save,
                javafx.scene.control.ButtonType.CANCEL);
        VBox fields = new VBox(8, new Label("Nhân viên"), employee,
                new Label("Ca làm việc"), shift, new Label("Ngày làm"), date);
        dialog.getDialogPane().setContent(fields);
        dialog.getDialogPane().setMinWidth(380);
        dialog.setResultConverter(button -> button == save ? save : null);
        if (dialog.showAndWait().isPresent()) {
            ShiftAssignment.Choice staff = employee.getValue();
            ShiftAssignment.Choice selectedShift = shift.getValue();
            String id = existing == null
                    ? "PC-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                    + "-" + String.format("%06d", java.util.concurrent.ThreadLocalRandom
                            .current().nextInt(1_000_000))
                    : existing.id();
            ShiftAssignment assignment = new ShiftAssignment(id, staff.id(), staff.label(),
                    selectedShift.id(), selectedShift.label(), date.getValue(),
                    existing == null ? java.time.LocalTime.MIDNIGHT : existing.startsAt(),
                    existing == null ? java.time.LocalTime.MIDNIGHT : existing.endsAt(),
                    "Đã phân công");
            runTask(() -> {
                if (existing == null) {
                    controller.add(assignment);
                } else {
                    controller.update(assignment);
                }
                audit.log(existing == null ? "Thêm" : "Sửa", "PhanCa", id,
                        "Tài khoản " + user.getMaTaiKhoan());
                return null;
            }, ignored -> refresh());
        }
    }

    private void deleteSelected() {
        ShiftAssignment selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Hãy chọn ca cần xóa.");
            return;
        }
        if (selected.workDate().isBefore(LocalDate.now())) {
            showError("Chỉ được xóa ca chưa diễn ra.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Xóa ca trực chưa diễn ra đã chọn?");
        confirm.setHeaderText(null);
        if (confirm.showAndWait().filter(javafx.scene.control.ButtonType.OK::equals).isPresent()) {
            runTask(() -> {
                controller.delete(selected);
                audit.log("Xóa", "PhanCa", selected.id(), "Tài khoản " + user.getMaTaiKhoan());
                return null;
            }, ignored -> refresh());
        }
    }

    private ShiftAssignment.Choice find(List<ShiftAssignment.Choice> choices, String id) {
        return choices.stream().filter(choice -> choice.id().equals(id)).findFirst().orElse(null);
    }

    private <T> void runTask(java.util.concurrent.Callable<T> work, java.util.function.Consumer<T> success) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return work.call();
            }
        };
        task.setOnSucceeded(event -> success.accept(task.getValue()));
        task.setOnFailed(event -> showError(task.getException() == null
                ? "Không thể hoàn tất thao tác." : task.getException().getMessage()));
        Thread thread = new Thread(task, "shift-schedule");
        thread.setDaemon(true);
        thread.start();
    }

    private void showError(String message) {
        feedback.setText(message == null || message.isBlank()
                ? "Không thể hoàn tất thao tác." : message);
    }

    private TableColumn<ShiftAssignment, String> column(
            String title, Function<ShiftAssignment, String> value) {
        TableColumn<ShiftAssignment, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }
}
