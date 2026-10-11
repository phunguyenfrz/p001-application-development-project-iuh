package com.petshop.view;

import com.petshop.controller.InventoryController;
import com.petshop.model.InventoryItem;
import com.petshop.util.Formats;
import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
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

public final class InventoryView extends VBox implements AppView {
    private final InventoryController controller;
    private final TableView<InventoryItem> table = new TableView<>();
    private final TextField search = new TextField();
    private final ComboBox<String> filter = new ComboBox<>();
    private final Label feedback = new Label();
    private final PauseTransition debounce = new PauseTransition(Duration.millis(300));
    private final AtomicLong requestSequence = new AtomicLong();
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
        filter.getItems().setAll("Tất cả sản phẩm", "Sắp hết hàng");
        filter.setValue(filter.getItems().get(0));
        filter.valueProperty().addListener((observable, oldValue, value) -> refresh());
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
        HBox toolbar = new HBox(10, search, filter);
        toolbar.getStyleClass().add("search-box");
        HBox.setHgrow(search, Priority.ALWAYS);
        table.getColumns().setAll(
                column("Mã sản phẩm", InventoryItem::productId),
                column("Sản phẩm", InventoryItem::productName),
                column("Mã vạch", item -> display(item.barcode())),
                column("Tồn kho", item -> item.quantityOnHand() + " " + item.unit()),
                column("Ngưỡng tối thiểu", item -> item.minimumQuantity() + " " + item.unit()),
                column("Giá bán", item -> Formats.money(item.salePrice())),
                column("Nhà cung cấp", item -> display(item.supplierName())));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Không có dữ liệu"));
        feedback.getStyleClass().add("welcome-sub");
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
        return "Tồn kho";
    }

    private void refresh() {
        long requestId = requestSequence.incrementAndGet();
        String query = keyword;
        boolean belowMinimum = "Sắp hết hàng".equals(filter.getValue());
        Task<List<InventoryItem>> task = new Task<>() {
            @Override
            protected List<InventoryItem> call() {
                if (belowMinimum && query.isBlank()) {
                    return controller.findBelowMinimum();
                }
                List<InventoryItem> values = query.isBlank()
                        ? controller.findAll() : controller.search(query);
                return belowMinimum
                        ? values.stream().filter(InventoryItem::isBelowMinimum).toList()
                        : values;
            }
        };
        task.setOnSucceeded(event -> {
            if (requestSequence.get() == requestId) {
                List<InventoryItem> items = task.getValue();
                table.getItems().setAll(items);
                table.setPlaceholder(new Label(items.isEmpty() && !query.isBlank()
                        ? "Không tìm thấy dữ liệu" : "Không có dữ liệu"));
                long lowCount = items.stream().filter(InventoryItem::isBelowMinimum).count();
                feedback.setText(lowCount == 0 ? "Không có sản phẩm dưới ngưỡng tồn."
                        : "Có " + lowCount + " sản phẩm đang ở dưới hoặc bằng ngưỡng tồn tối thiểu.");
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
