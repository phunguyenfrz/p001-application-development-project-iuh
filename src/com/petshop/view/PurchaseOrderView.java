package com.petshop.view;

import com.petshop.controller.PurchaseOrderController;
import com.petshop.model.PurchaseOrder;
import com.petshop.model.User;
import com.petshop.util.Formats;
import com.petshop.util.TextSearch;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public final class PurchaseOrderView extends VBox implements AppView, SearchResultAware {
    private record Choices(List<PurchaseOrder.Choice> suppliers, List<PurchaseOrder.Choice> products) {
    }

    private final PurchaseOrderController controller;
    private final User user;
    private final TableView<PurchaseOrder> orders = new TableView<>();
    private final TableView<PurchaseOrder.Line> lines = new TableView<>();
    private final TextField search = new TextField();
    private final Label feedback = new Label();
    private final PauseTransition debounce = new PauseTransition(Duration.millis(300));
    private final SearchResultPanel<PurchaseOrder> searchResults;
    private List<PurchaseOrder> loadedOrders = List.of();
    private Set<String> matchedIds = Set.of();
    private String keyword = "";
    private String pendingGlobalId;
    private boolean settingGlobalSearch;

    public PurchaseOrderView(PurchaseOrderController controller, User user) {
        super(12);
        this.controller = controller;
        this.user = user;
        getStyleClass().add("module-view");
        Label title = new Label("Đơn nhập hàng");
        title.getStyleClass().add("welcome-title");
        Label description = new Label("UC011 · Lập đơn nhập và xác nhận riêng khi hàng đã nhận.");
        description.getStyleClass().add("welcome-sub");
        search.setPromptText("Tìm mã đơn, nhà cung cấp, nhân viên hoặc sản phẩm...");
        search.getStyleClass().add("search-field");
        Button clearSearch = new Button("×");
        clearSearch.getStyleClass().add("search-clear-button");
        clearSearch.setAccessibleText("Xóa từ khóa tìm kiếm");
        clearSearch.setOnAction(event -> search.clear());
        StackPane searchInput = new StackPane(search, clearSearch);
        StackPane.setAlignment(clearSearch, javafx.geometry.Pos.CENTER_RIGHT);
        search.setPadding(new javafx.geometry.Insets(8, 38, 8, 12));
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
        Button create = new Button("Lập đơn nhập");
        create.setOnAction(event -> prepareCreate());
        Button receive = new Button("Xác nhận nhận hàng");
        receive.setOnAction(event -> receiveSelected());
        Button refresh = new Button("Tải lại");
        refresh.setOnAction(event -> reload());
        List<TableColumn<PurchaseOrder, ?>> orderColumns = List.of(
                column("Mã đơn", PurchaseOrder::id),
                column("Ngày lập", value -> Formats.date(value.createdOn())),
                column("Nhà cung cấp", PurchaseOrder::supplierName),
                column("Nhân viên", PurchaseOrder::employeeName),
                column("Tổng tiền", value -> Formats.money(value.total())),
                column("Trạng thái", PurchaseOrder::status));
        orders.getColumns().setAll(orderColumns);
        orders.setPlaceholder(new Label("Không có dữ liệu"));
        orders.setRowFactory(ignored -> new TableRow<>() {
            @Override
            protected void updateItem(PurchaseOrder item, boolean empty) {
                super.updateItem(item, empty);
                pseudoClassStateChanged(PseudoClass.getPseudoClass("search-match"),
                        !empty && item != null && matchedIds.contains(item.id()));
            }
        });
        searchResults = new SearchResultPanel<>(orderColumns, false, false,
                () -> search.clear(),
                item -> {
                    orders.getSelectionModel().select(item);
                    orders.scrollTo(item);
                },
                item -> { },
                item -> { },
                this::showReadOnly);
        lines.getColumns().setAll(
                column("Sản phẩm", PurchaseOrder.Line::productName),
                column("Số lượng", value -> String.valueOf(value.quantity())),
                column("Đơn giá", value -> Formats.money(value.unitCost())),
                column("Thành tiền", value -> Formats.money(value.total())));
        lines.setPlaceholder(new Label("Chọn đơn hàng để xem chi tiết"));
        orders.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, value) -> lines.getItems().setAll(
                        value == null ? List.of() : value.lines()));
        HBox toolbar = new HBox(8, searchInput, create, receive, refresh);
        HBox.setHgrow(searchInput, Priority.ALWAYS);
        feedback.getStyleClass().add("feedback-error");
        VBox.setVgrow(orders, Priority.ALWAYS);
        getChildren().addAll(title, description, toolbar, searchResults, feedback, orders,
                new Label("Chi tiết đơn nhập"), lines);
        VBox.setVgrow(lines, Priority.SOMETIMES);
        reload();
    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public String getTitle() {
        return "Đơn nhập hàng";
    }

    @Override
    public void showSearchResult(String id) {
        pendingGlobalId = Objects.requireNonNull(id);
        settingGlobalSearch = true;
        search.setText(id);
        settingGlobalSearch = false;
        keyword = id.trim();
        debounce.stop();
        if (!loadedOrders.isEmpty()) {
            applySearch();
        }
    }

    @Override
    public void showSearchKeyword(String query) {
        pendingGlobalId = null;
        search.setText(query == null ? "" : query);
    }

    private void reload() {
        runTask(controller::findAll, values -> {
            loadedOrders = values;
            applySearch();
            feedback.setText("");
        });
    }

    private void applySearch() {
        orders.getItems().setAll(loadedOrders);
        if (keyword.isBlank()) {
            matchedIds = Set.of();
            searchResults.hideResults();
        } else {
            List<PurchaseOrder> matches = loadedOrders.stream()
                    .filter(item -> pendingGlobalId != null
                            ? item.id().equals(pendingGlobalId)
                            : matches(item, keyword))
                    .toList();
            matchedIds = matches.stream().map(PurchaseOrder::id)
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
            searchResults.showResults(keyword, matches);
            if (!matches.isEmpty()) {
                orders.scrollTo(matches.get(0));
            }
        }
        orders.refresh();
    }

    private boolean matches(PurchaseOrder item, String query) {
        String itemFields = item.lines().stream()
                .map(line -> line.productId() + " " + line.productName())
                .collect(java.util.stream.Collectors.joining(" "));
        return TextSearch.matchesAll(query, item.id(), item.supplierName(), itemFields);
    }

    private void showReadOnly(PurchaseOrder item) {
        Alert details = new Alert(Alert.AlertType.INFORMATION);
        details.setTitle("Chi tiết đơn nhập");
        details.setHeaderText(item.id());
        details.setContentText("Nhà cung cấp: " + item.supplierName()
                + "\nNhân viên: " + item.employeeName()
                + "\nNgày lập: " + Formats.date(item.createdOn())
                + "\nTổng tiền: " + Formats.money(item.total())
                + "\nTrạng thái: " + item.status()
                + "\nMặt hàng: " + item.lines().stream()
                        .map(line -> line.productName() + " × " + line.quantity())
                        .collect(java.util.stream.Collectors.joining(", ")));
        details.show();
    }

    private void prepareCreate() {
        runTask(() -> new Choices(controller.suppliers(), controller.products()),
                choices -> openCreateDialog(choices));
    }

    private void openCreateDialog(Choices choices) {
        if (choices.suppliers().isEmpty() || choices.products().isEmpty()) {
            showError("Cần có nhà cung cấp và sản phẩm đang kinh doanh trước khi lập đơn.");
            return;
        }
        ComboBox<PurchaseOrder.Choice> supplier = new ComboBox<>();
        supplier.getItems().setAll(choices.suppliers());
        supplier.getSelectionModel().selectFirst();
        ComboBox<PurchaseOrder.Choice> product = new ComboBox<>();
        product.getItems().setAll(choices.products());
        product.getSelectionModel().selectFirst();
        TextField quantity = new TextField("1");
        TextField unitCost = new TextField(product.getValue().amount().toPlainString());
        product.setOnAction(event -> {
            if (product.getValue() != null) {
                unitCost.setText(product.getValue().amount().toPlainString());
            }
        });
        TableView<PurchaseOrder.Line> lineTable = new TableView<>();
        lineTable.getColumns().setAll(
                column("Sản phẩm", PurchaseOrder.Line::productName),
                column("Số lượng", line -> String.valueOf(line.quantity())),
                column("Đơn giá", line -> Formats.money(line.unitCost())),
                column("Thành tiền", line -> Formats.money(line.total())));
        lineTable.setPlaceholder(new Label("Thêm ít nhất một mặt hàng"));
        Button addLine = new Button("Thêm mặt hàng");
        addLine.setOnAction(event -> {
            try {
                int amount = Integer.parseInt(quantity.getText().trim());
                BigDecimal cost = new BigDecimal(unitCost.getText().trim());
                if (amount <= 0 || cost.signum() < 0 || cost.scale() > 2) {
                    throw new IllegalArgumentException("Số lượng phải lớn hơn 0; đơn giá không âm.");
                }
                PurchaseOrder.Choice selected = product.getValue();
                PurchaseOrder.Line current = lineTable.getItems().stream()
                        .filter(item -> item.productId().equals(selected.id()))
                        .findFirst().orElse(null);
                if (current != null) {
                    lineTable.getItems().remove(current);
                    amount += current.quantity();
                }
                lineTable.getItems().add(new PurchaseOrder.Line(selected.id(),
                        selected.label(), amount, cost));
            } catch (NumberFormatException exception) {
                showError("Số lượng và đơn giá phải là số hợp lệ.");
            } catch (IllegalArgumentException exception) {
                showError(exception.getMessage());
            }
        });
        GridPane fields = new GridPane();
        fields.setHgap(8);
        fields.setVgap(8);
        fields.addRow(0, new Label("Nhà cung cấp"), supplier);
        fields.addRow(1, new Label("Sản phẩm"), product);
        fields.addRow(2, new Label("Số lượng"), quantity);
        fields.addRow(3, new Label("Đơn giá nhập"), unitCost);
        VBox content = new VBox(8, fields, addLine, lineTable);
        javafx.scene.control.Dialog<javafx.scene.control.ButtonType> dialog =
                new javafx.scene.control.Dialog<>();
        dialog.setTitle("Lập đơn nhập hàng");
        javafx.scene.control.ButtonType save = new javafx.scene.control.ButtonType(
                "Tạo đơn", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(save,
                javafx.scene.control.ButtonType.CANCEL);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setMinWidth(650);
        dialog.setResultConverter(button -> button == save ? save : null);
        if (dialog.showAndWait().isPresent()) {
            if (lineTable.getItems().isEmpty()) {
                showError("Đơn nhập phải có ít nhất một mặt hàng.");
                return;
            }
            PurchaseOrder.Choice selectedSupplier = supplier.getValue();
            List<PurchaseOrder.Line> detail = new ArrayList<>(lineTable.getItems());
            String id = "DH-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                    + "-" + String.format("%06d", java.util.concurrent.ThreadLocalRandom.current()
                    .nextInt(1_000_000));
            PurchaseOrder order = new PurchaseOrder(id, selectedSupplier.id(),
                    selectedSupplier.label(), user.getMaNhanVien(), user.getFullName(),
                    LocalDate.now(), BigDecimal.ZERO, "Chờ nhận hàng", detail);
            runTask(() -> {
                controller.create(order);
                return null;
            }, ignored -> reload());
        }
    }

    private void receiveSelected() {
        PurchaseOrder order = orders.getSelectionModel().getSelectedItem();
        if (order == null) {
            showError("Hãy chọn đơn cần xác nhận nhận hàng.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Xác nhận đã nhận đủ hàng cho đơn " + order.id() + "?");
        confirm.setHeaderText(null);
        if (confirm.showAndWait().filter(javafx.scene.control.ButtonType.OK::equals).isPresent()) {
            runTask(() -> {
                controller.receive(order);
                return null;
            }, ignored -> reload());
        }
    }

    private <T> void runTask(java.util.concurrent.Callable<T> operation,
                             java.util.function.Consumer<T> success) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return operation.call();
            }
        };
        task.setOnSucceeded(event -> success.accept(task.getValue()));
        task.setOnFailed(event -> showError(task.getException() == null
                ? "Không thể hoàn tất thao tác." : task.getException().getMessage()));
        Thread worker = new Thread(task, "purchase-order");
        worker.setDaemon(true);
        worker.start();
    }

    private void showError(String message) {
        feedback.setText(message == null || message.isBlank()
                ? "Không thể hoàn tất thao tác." : message);
    }

    private <T> TableColumn<T, String> column(String heading, Function<T, String> value) {
        TableColumn<T, String> column = new TableColumn<>(heading);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }
}
