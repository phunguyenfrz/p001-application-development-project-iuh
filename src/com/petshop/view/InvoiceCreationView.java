package com.petshop.view;

import com.petshop.controller.InvoiceController;
import com.petshop.controller.InvoiceSavedAuditException;
import com.petshop.model.InvoiceData.AppointmentOption;
import com.petshop.model.InvoiceData.CustomerOption;
import com.petshop.model.InvoiceData.InvoiceDraft;
import com.petshop.model.InvoiceData.InvoiceLine;
import com.petshop.model.InvoiceData.InvoiceReceipt;
import com.petshop.model.InvoiceData.ProductOption;
import com.petshop.util.Formats;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public final class InvoiceCreationView extends VBox implements AppView {
    private final String title;
    private final InvoiceController controller;
    private final String initialAppointmentId;
    private final ComboBox<CustomerOption> customers = new ComboBox<>();
    private final ComboBox<AppointmentOption> appointments = new ComboBox<>();
    private final TextField productSearch = new TextField();
    private final ComboBox<ProductOption> productResults = new ComboBox<>();
    private final Spinner<Integer> quantity = new Spinner<>(1, 100_000, 1);
    private final ComboBox<String> paymentMethod = new ComboBox<>();
    private final TableView<CartRow> cartTable = new TableView<>();
    private final ObservableList<CartRow> cart = FXCollections.observableArrayList();
    private final Label totalLabel = new Label("Tổng tiền: 0 ₫");
    private final Label feedback = new Label();
    private final Button payButton = new Button("Xác nhận thanh toán");
    private final Button exportButton = new Button("In / Xuất PDF");
    private Button removeButton;
    private final PauseTransition searchDebounce = new PauseTransition(Duration.millis(300));
    private final AtomicLong searchSequence = new AtomicLong();
    private InvoiceReceipt receipt;

    public InvoiceCreationView(InvoiceController controller, String appointmentId) {
        super(16);
        this.controller = controller;
        initialAppointmentId = appointmentId;
        title = appointmentId == null ? "Lập hóa đơn" : "Lập hóa đơn | " + appointmentId;
        setPadding(new Insets(4, 0, 0, 0));
        configureCart();
        getChildren().addAll(buildHeader(), buildForm(), cartTable, buildActions());
        loadOptions();
        configureProductSearch();
        appointments.valueProperty().addListener((observable, oldValue, selected) -> {
            if (selected != null) {
                customers.getItems().stream()
                        .filter(customer -> customer.id().equals(selected.customerId()))
                        .findFirst().ifPresent(customers::setValue);
                updateTotal();
            }
        });
    }

    private Node buildHeader() {
        VBox header = new VBox(6);
        Label heading = new Label("Lập hóa đơn");
        heading.getStyleClass().add("welcome-title");
        Label description = new Label(initialAppointmentId == null
                ? "Chọn khách hàng hoặc để trống cho khách vãng lai; tìm sản phẩm bằng tên, mã hoặc mã vạch."
                : "Lịch hẹn đã hoàn thành: " + initialAppointmentId);
        description.getStyleClass().add("welcome-sub");
        header.getChildren().addAll(heading, description);
        return header;
    }

    private Node buildForm() {
        customers.setPromptText("Khách vãng lai hoặc chọn khách hàng");
        appointments.setPromptText("Không gắn lịch hẹn");
        paymentMethod.getItems().setAll("Tiền mặt", "Chuyển khoản", "POS");
        paymentMethod.setValue("Tiền mặt");
        productSearch.setPromptText("Quét/nhập mã vạch hoặc tìm sản phẩm...");
        productSearch.getStyleClass().add("search-field");
        productResults.setPromptText("Chọn sản phẩm tìm thấy");
        HBox productRow = new HBox(8, productSearch, productResults, quantity,
                actionButton("Thêm mặt hàng", this::addProduct));
        productRow.getStyleClass().add("search-box");
        HBox.setHgrow(productSearch, Priority.ALWAYS);
        VBox panel = new VBox(12,
                labeled("Khách hàng", customers),
                labeled("Lịch hẹn đã hoàn thành (không bắt buộc)", appointments),
                labeled("Sản phẩm", productRow),
                labeled("Hình thức thanh toán", paymentMethod));
        panel.getStyleClass().add("card-panel");
        return panel;
    }

    private Node buildActions() {
        payButton.getStyleClass().add("btn-create-invoice");
        payButton.setOnAction(event -> createInvoice());
        exportButton.getStyleClass().add("sidebar-btn");
        exportButton.setDisable(true);
        exportButton.setOnAction(event -> chooseExportPath());
        totalLabel.getStyleClass().add("card-title");
        feedback.getStyleClass().add("feedback-error");
        feedback.setWrapText(true);
        HBox actions = new HBox(12, totalLabel, removeButton, payButton, exportButton);
        actions.setPadding(new Insets(8, 0, 0, 0));
        return new VBox(6, actions, feedback);
    }

    private void configureCart() {
        cartTable.getStyleClass().add("card-panel");
        cartTable.setItems(cart);
        cartTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        cartTable.setPlaceholder(new Label("Chưa có mặt hàng trong hóa đơn."));
        cartTable.getColumns().setAll(
                cartColumn("Mặt hàng", CartRow::name),
                cartColumn("Số lượng", row -> Integer.toString(row.quantity())),
                cartColumn("Đơn giá", row -> Formats.money(row.unitPrice())),
                cartColumn("Thành tiền", row -> Formats.money(row.lineTotal())));
        removeButton = actionButton("Xóa dòng", () -> {
            CartRow selected = cartTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                cart.remove(selected);
                updateTotal();
            }
        });
        removeButton.getStyleClass().add("sidebar-btn");
        VBox.setVgrow(cartTable, Priority.ALWAYS);
    }

    private void configureProductSearch() {
        productSearch.textProperty().addListener((observable, oldValue, value) -> {
            searchDebounce.stop();
            if (value == null || value.isBlank()) {
                productResults.getItems().clear();
            } else {
                searchDebounce.playFromStart();
            }
        });
        searchDebounce.setOnFinished(event -> searchProducts(productSearch.getText()));
        productSearch.setOnAction(event -> searchProducts(productSearch.getText()));
    }

    private void loadOptions() {
        Task<Options> task = new Task<>() {
            @Override
            protected Options call() {
                return new Options(controller.findCustomers(),
                        controller.findCompletedAppointments());
            }
        };
        task.setOnSucceeded(event -> {
            customers.getItems().setAll(task.getValue().customers());
            appointments.getItems().setAll(task.getValue().appointments());
            if (initialAppointmentId != null) {
                appointments.getItems().stream()
                        .filter(item -> item.id().equals(initialAppointmentId))
                        .findFirst().ifPresent(appointments::setValue);
            }
        });
        task.setOnFailed(event -> showFailure("Không thể tải dữ liệu cho hóa đơn.",
                task.getException()));
        runTask(task, "invoice-options-load");
    }

    private void searchProducts(String query) {
        String term = query == null ? "" : query.trim();
        long current = searchSequence.incrementAndGet();
        Task<List<ProductOption>> task = new Task<>() {
            @Override
            protected List<ProductOption> call() {
                return controller.searchProducts(term);
            }
        };
        task.setOnSucceeded(event -> {
            if (searchSequence.get() == current) {
                productResults.getItems().setAll(task.getValue());
                if (!task.getValue().isEmpty()) {
                    productResults.getSelectionModel().selectFirst();
                    productResults.show();
                } else {
                    feedback.setText("Không tìm thấy sản phẩm.");
                }
            }
        });
        task.setOnFailed(event -> {
            if (searchSequence.get() == current) {
                showFailure("Không thể tìm sản phẩm.", task.getException());
            }
        });
        runTask(task, "invoice-product-search");
    }

    private void addProduct() {
        ProductOption selected = productResults.getValue();
        if (selected == null) {
            feedback.setText("Hãy tìm và chọn sản phẩm trước.");
            return;
        }
        int requested = quantity.getValue();
        CartRow existing = cart.stream().filter(row -> row.product().id().equals(selected.id()))
                .findFirst().orElse(null);
        int combined = requested + (existing == null ? 0 : existing.quantity());
        if (combined > selected.stock()) {
            feedback.setText("Số lượng vượt quá tồn kho hiện có (" + selected.stock() + ").");
            return;
        }
        if (existing != null) {
            cart.set(cart.indexOf(existing), new CartRow(selected, combined));
        } else {
            cart.add(new CartRow(selected, requested));
        }
        feedback.setText("");
        updateTotal();
    }

    private void createInvoice() {
        if (cart.isEmpty() && appointments.getValue() == null) {
            feedback.setText("Thêm ít nhất một mặt hàng hoặc chọn lịch hẹn hoàn thành.");
            return;
        }
        List<InvoiceLine> lines = cart.stream()
                .map(row -> new InvoiceLine(row.product().id(), row.quantity())).toList();
        AppointmentOption appointment = appointments.getValue();
        String customerId = customers.getValue() == null
                ? null : customers.getValue().id();
        InvoiceDraft draft = new InvoiceDraft(customerId,
                appointment == null ? null : appointment.id(),
                paymentMethod.getValue(), lines);
        payButton.setDisable(true);
        feedback.setText("Đang ghi nhận thanh toán...");
        Task<InvoiceReceipt> task = new Task<>() {
            @Override
            protected InvoiceReceipt call() {
                return controller.createInvoice(draft);
            }
        };
        task.setOnSucceeded(event -> {
            receipt = task.getValue();
            feedback.getStyleClass().remove("feedback-error");
            feedback.getStyleClass().add("feedback-success");
            feedback.setText("Đã thanh toán và lưu hóa đơn " + receipt.id() + ".");
            payButton.setDisable(true);
            exportButton.setDisable(false);
            showAlert(Alert.AlertType.INFORMATION, "Thanh toán thành công",
                    "Hóa đơn " + receipt.id() + " đã được lưu.");
        });
        task.setOnFailed(event -> {
            payButton.setDisable(false);
            if (task.getException() instanceof InvoiceSavedAuditException saved) {
                receipt = saved.receipt();
                payButton.setDisable(true);
                exportButton.setDisable(false);
                feedback.getStyleClass().remove("feedback-error");
                feedback.getStyleClass().add("feedback-success");
                feedback.setText(saved.getMessage()
                        + " Mã hóa đơn: " + receipt.id() + ". Không thanh toán lại.");
                showAlert(Alert.AlertType.WARNING, "Hóa đơn đã được lưu",
                        saved.getMessage() + " Vui lòng không lập lại giao dịch.");
                return;
            }
            showFailure("Không thể hoàn tất thanh toán.", task.getException());
        });
        runTask(task, "invoice-create");
    }

    private void chooseExportPath() {
        if (receipt == null) {
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Xuất hóa đơn PDF");
        chooser.setInitialFileName(receipt.id() + ".pdf");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Tệp PDF", "*.pdf"));
        Window window = getScene() == null ? null : getScene().getWindow();
        java.io.File file = chooser.showSaveDialog(window);
        if (file == null) {
            return;
        }
        Path target = file.toPath();
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                controller.exportPdf(receipt, target);
                return null;
            }
        };
        task.setOnSucceeded(event ->
                showAlert(Alert.AlertType.INFORMATION, "Xuất hóa đơn", "Đã xuất tệp PDF."));
        task.setOnFailed(event -> showFailure("Không thể xuất hóa đơn PDF.", task.getException()));
        runTask(task, "invoice-pdf-export");
    }

    private void updateTotal() {
        BigDecimal products = cart.stream().map(CartRow::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        AppointmentOption appointment = appointments.getValue();
        BigDecimal services = appointment == null
                ? BigDecimal.ZERO : appointment.serviceTotal();
        totalLabel.setText("Tổng tiền: " + Formats.money(products.add(services)));
    }

    private void showFailure(String friendly, Throwable error) {
        Throwable cause = error == null ? null : error.getCause();
        String details = error == null ? "Không rõ lỗi" : error.getMessage();
        java.util.logging.Logger.getLogger(InvoiceCreationView.class.getName())
                .log(java.util.logging.Level.SEVERE, friendly, error);
        feedback.getStyleClass().remove("feedback-success");
        feedback.getStyleClass().add("feedback-error");
        feedback.setText(friendly + (details == null || details.isBlank()
                ? "" : " " + details));
        if (cause != null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi hóa đơn", friendly);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    private void runTask(Task<?> task, String threadName) {
        Thread worker = new Thread(task, threadName);
        worker.setDaemon(true);
        worker.start();
    }

    private Node labeled(String text, Node control) {
        VBox field = new VBox(4, new Label(text), control);
        return field;
    }

    private Button actionButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setOnAction(event -> action.run());
        return button;
    }

    private TableColumn<CartRow, String> cartColumn(
            String title, java.util.function.Function<CartRow, String> value) {
        TableColumn<CartRow, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell ->
                new javafx.beans.property.ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public String getTitle() {
        return title;
    }

    private record Options(
            List<CustomerOption> customers, List<AppointmentOption> appointments) {
    }

    private record CartRow(ProductOption product, int quantity) {
        String name() {
            return product.name();
        }

        BigDecimal unitPrice() {
            return product.price();
        }

        BigDecimal lineTotal() {
            return unitPrice().multiply(BigDecimal.valueOf(quantity));
        }
    }
}
