package com.petshop.view;

import com.petshop.controller.InventoryController;
import com.petshop.model.InventoryItem;
import com.petshop.util.Formats;
import com.petshop.util.TextSearch;
import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
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

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public final class InventoryView extends VBox implements AppView, SearchResultAware {
    private final InventoryController controller;
    private final TableView<InventoryItem> table = new TableView<>();
    private final TextField search = new TextField();
    private final ComboBox<String> filter = new ComboBox<>();
    private final Label feedback = new Label();
    private final PauseTransition debounce = new PauseTransition(Duration.millis(300));
    private final AtomicLong requestSequence = new AtomicLong();
    private final SearchResultPanel<InventoryItem> searchResults;
    private List<InventoryItem> loadedItems = List.of();
    private Set<String> matchedIds = Set.of();
    private String pendingGlobalId;
    private boolean settingGlobalSearch;
    private String keyword = "";

    public InventoryView(InventoryController controller) {
        super(14);
        this.controller = Objects.requireNonNull(controller);
        setPadding(new Insets(4, 0, 0, 0));
        Label title = new Label("Tồn kho");
        title.getStyleClass().add("welcome-title");
        Label description = new Label("UC010 · Tra cứu số lượng tồn và cảnh báo sản phẩm dưới ngưỡng.");
        description.getStyleClass().add("welcome-sub");
        search.setPromptText("Tìm theo mã, tên, mã vạch hoặc nhà cung cấp...");
        search.getStyleClass().add("search-field");
        Button clearSearch = new Button("×");
        clearSearch.getStyleClass().add("search-clear-button");
        clearSearch.setAccessibleText("Xóa từ khóa tìm kiếm");
        clearSearch.setOnAction(event -> search.clear());
        StackPane searchInput = new StackPane(search, clearSearch);
        StackPane.setAlignment(clearSearch, javafx.geometry.Pos.CENTER_RIGHT);
        search.setPadding(new Insets(8, 38, 8, 12));
        filter.getItems().setAll("Tất cả sản phẩm", "Sắp hết hàng");
        filter.setValue(filter.getItems().get(0));
        filter.valueProperty().addListener((observable, oldValue, value) -> applySearch());
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
        HBox toolbar = new HBox(10, searchInput, filter);
        toolbar.getStyleClass().add("search-box");
        HBox.setHgrow(searchInput, Priority.ALWAYS);
        List<TableColumn<InventoryItem, ?>> inventoryColumns = List.of(
                column("Mã sản phẩm", InventoryItem::productId),
                column("Sản phẩm", InventoryItem::productName),
                column("Mã vạch", item -> display(item.barcode())),
                column("Tồn kho", item -> item.quantityOnHand() + " " + item.unit()),
                column("Ngưỡng tối thiểu", item -> item.minimumQuantity() + " " + item.unit()),
                column("Giá bán", item -> Formats.money(item.salePrice())),
                column("Nhà cung cấp", item -> display(item.supplierName())));
        table.getColumns().setAll(inventoryColumns);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Không có dữ liệu"));
        table.setRowFactory(ignored -> new TableRow<>() {
            @Override
            protected void updateItem(InventoryItem item, boolean empty) {
                super.updateItem(item, empty);
                pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("search-match"),
                        !empty && item != null && matchedIds.contains(item.productId()));
            }
        });
        searchResults = new SearchResultPanel<>(inventoryColumns, false, false,
                () -> search.clear(),
                item -> {
                    table.getSelectionModel().select(item);
                    table.scrollTo(item);
                },
                item -> { },
                item -> { },
                item -> showReadOnly(item));
        feedback.getStyleClass().add("welcome-sub");
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
        return "Tồn kho";
    }

    @Override
    public void showSearchResult(String id) {
        pendingGlobalId = Objects.requireNonNull(id);
        settingGlobalSearch = true;
        search.setText(id);
        settingGlobalSearch = false;
        keyword = id.trim();
        debounce.stop();
        if (!loadedItems.isEmpty()) {
            applySearch();
        }
    }

    @Override
    public void showSearchKeyword(String query) {
        pendingGlobalId = null;
        search.setText(query == null ? "" : query);
    }

    private void refresh() {
        long requestId = requestSequence.incrementAndGet();
        Task<List<InventoryItem>> task = new Task<>() {
            @Override
            protected List<InventoryItem> call() {
                return controller.findAll();
            }
        };
        task.setOnSucceeded(event -> {
            if (requestSequence.get() == requestId) {
                loadedItems = task.getValue();
                applySearch();
            }
        });
        task.setOnFailed(event -> {
            if (requestSequence.get() == requestId) {
                feedback.setText(task.getException() == null
                        ? "Không thể tải dữ liệu tồn kho." : task.getException().getMessage());
            }
        });
        Thread worker = new Thread(task, "inventory-search");
        worker.setDaemon(true);
        worker.start();
    }

    private void applySearch() {
        List<InventoryItem> visible = "Sắp hết hàng".equals(filter.getValue())
                ? loadedItems.stream().filter(InventoryItem::isBelowMinimum).toList()
                : loadedItems;
        table.getItems().setAll(visible);
        if (keyword.isBlank()) {
            matchedIds = Set.of();
            searchResults.hideResults();
        } else {
            List<InventoryItem> matches = visible.stream()
                    .filter(item -> pendingGlobalId != null
                            ? item.productId().equals(pendingGlobalId)
                            : matches(item, keyword))
                    .toList();
            matchedIds = matches.stream().map(InventoryItem::productId)
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
            searchResults.showResults(keyword, matches);
            if (!matches.isEmpty()) {
                table.scrollTo(matches.get(0));
            }
        }
        table.refresh();
        long lowCount = visible.stream().filter(InventoryItem::isBelowMinimum).count();
        feedback.setText(lowCount == 0 ? "Không có sản phẩm dưới ngưỡng tồn."
                : "Có " + lowCount + " sản phẩm đang ở dưới hoặc bằng ngưỡng tồn tối thiểu.");
    }

    private boolean matches(InventoryItem item, String query) {
        return TextSearch.matchesAll(query, item.productId(), item.productName(),
                item.barcode(), item.supplierName());
    }

    private void showReadOnly(InventoryItem item) {
        javafx.scene.control.Alert details = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        details.setTitle("Chi tiết tồn kho");
        details.setHeaderText(item.productName());
        details.setContentText("Mã sản phẩm: " + item.productId()
                + "\nMã vạch: " + display(item.barcode())
                + "\nTồn kho: " + item.quantityOnHand() + " " + item.unit()
                + "\nNgưỡng tối thiểu: " + item.minimumQuantity() + " " + item.unit()
                + "\nNhà cung cấp: " + display(item.supplierName()));
        details.show();
    }

    private TableColumn<InventoryItem, String> column(
            String heading, Function<InventoryItem, String> value) {
        TableColumn<InventoryItem, String> column = new TableColumn<>(heading);
        column.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }

    private String display(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }
}
