package com.petshop.view;

import com.petshop.controller.PurchaseOrderController;
import com.petshop.model.PurchaseOrder;
import com.petshop.model.User;
import com.petshop.util.Formats;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public final class PurchaseOrderView extends VBox implements AppView {
    private record Choices(List<PurchaseOrder.Choice> suppliers, List<PurchaseOrder.Choice> products) {
    }

    private final PurchaseOrderController controller;
    private final User user;
    private final TableView<PurchaseOrder> orders = new TableView<>();
    private final TableView<PurchaseOrder.Line> lines = new TableView<>();
    private final Label feedback = new Label();

    public PurchaseOrderView(PurchaseOrderController controller, User user) {
        super(12);
        this.controller = controller;
        this.user = user;
        getStyleClass().add("module-view");
        Label title = new Label("Đơn nhập hàng");
        title.getStyleClass().add("welcome-title");
        Label description = new Label("UC011 · Lập đơn nhập và xác nhận riêng khi hàng đã nhận.");
        description.getStyleClass().add("welcome-sub");
        Button create = new Button("Lập đơn nhập");
        create.setOnAction(event -> prepareCreate());
        Button receive = new Button("Xác nhận nhận hàng");
        receive.setOnAction(event -> receiveSelected());
        Button refresh = new Button("Tải lại");
        refresh.setOnAction(event -> reload());
        orders.getColumns().setAll(
                column("Mã đơn", PurchaseOrder::id),
                column("Ngày lập", value -> Formats.date(value.createdOn())),
                column("Nhà cung cấp", PurchaseOrder::supplierName),
                column("Nhân viên", PurchaseOrder::employeeName),
                column("Tổng tiền", value -> Formats.money(value.total())),
                column("Trạng thái", PurchaseOrder::status));
        orders.setPlaceholder(new Label("Không có dữ liệu"));
        lines.getColumns().setAll(
                column("Sản phẩm", PurchaseOrder.Line::productName),
                column("Số lượng", value -> String.valueOf(value.quantity())),
                column("Đơn giá", value -> Formats.money(value.unitCost())),
                column("Thành tiền", value -> Formats.money(value.total())));
        lines.setPlaceholder(new Label("Chọn đơn hàng để xem chi tiết"));
        orders.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, value) -> lines.getItems().setAll(
                        value == null ? List.of() : value.lines()));
        HBox toolbar = new HBox(8, create, receive, refresh);
        feedback.getStyleClass().add("feedback-error");
        VBox.setVgrow(orders, Priority.ALWAYS);
        getChildren().addAll(title, description, toolbar, feedback, orders,
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

    private void reload() {
        runTask(controller::findAll, values -> {
            orders.getItems().setAll(values);
            feedback.setText("");
        });
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
