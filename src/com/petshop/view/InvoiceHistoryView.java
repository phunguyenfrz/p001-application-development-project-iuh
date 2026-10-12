package com.petshop.view;

import com.petshop.controller.InvoiceHistoryController;
import com.petshop.model.InvoiceSummary;
import com.petshop.util.Formats;
import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public final class InvoiceHistoryView extends VBox implements AppView {
    private final InvoiceHistoryController controller;
    private final TextField search = new TextField();
    private final DatePicker from = new DatePicker(LocalDate.now().withDayOfMonth(1));
    private final DatePicker through = new DatePicker(LocalDate.now());
    private final TableView<InvoiceSummary> invoices = new TableView<>();
    private final TableView<InvoiceSummary.Line> details = new TableView<>();
    private final Label feedback = new Label();
    private final PauseTransition debounce = new PauseTransition(Duration.millis(300));
    private final AtomicLong request = new AtomicLong();

    public InvoiceHistoryView(InvoiceHistoryController controller) {
        super(12);
        this.controller = controller;
        getStyleClass().add("module-view");
        Label title = new Label("Lịch sử hóa đơn");
        title.getStyleClass().add("welcome-title");
        Label description = new Label("UC015 · Tra cứu hóa đơn và xem chi tiết mặt hàng.");
        description.getStyleClass().add("welcome-sub");
        search.setPromptText("Tìm mã hóa đơn, khách hàng, nhân viên hoặc số điện thoại");
        search.getStyleClass().add("search-field");
        invoices.getColumns().setAll(
                column("Mã hóa đơn", InvoiceSummary::id),
                column("Ngày lập", value -> Formats.dateTime(value.createdAt())),
                column("Khách hàng", InvoiceSummary::customerName),
                column("Nhân viên", InvoiceSummary::employeeName),
                column("Thanh toán", value -> value.paymentMethod() == null ? "-" : value.paymentMethod()),
                column("Trạng thái", InvoiceSummary::paymentStatus),
                column("Tổng tiền", value -> Formats.money(value.total())));
        invoices.setPlaceholder(new Label("Không có dữ liệu"));
        details.getColumns().setAll(
                column("Loại", InvoiceSummary.Line::category),
                column("Mặt hàng", InvoiceSummary.Line::description),
                column("Số lượng", value -> String.valueOf(value.quantity())),
                column("Đơn giá", value -> Formats.money(value.unitPrice())),
                column("Thành tiền", value -> Formats.money(value.amount())));
        details.setPlaceholder(new Label("Chọn hóa đơn để xem chi tiết"));
        invoices.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, value) -> details.getItems().setAll(
                        value == null ? List.of() : value.lines()));
        search.textProperty().addListener((observable, oldValue, value) -> debounce.playFromStart());
        debounce.setOnFinished(event -> load());
        from.valueProperty().addListener((observable, oldValue, value) -> load());
        through.valueProperty().addListener((observable, oldValue, value) -> load());
        feedback.getStyleClass().add("feedback-error");
        VBox.setVgrow(invoices, Priority.ALWAYS);
        VBox.setVgrow(details, Priority.SOMETIMES);
        getChildren().addAll(title, description,
                new HBox(8, search, from, through), feedback, invoices,
                new Label("Chi tiết hóa đơn"), details);
        load();
    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public String getTitle() {
        return "Lịch sử hóa đơn";
    }

    private void load() {
        long current = request.incrementAndGet();
        String keyword = search.getText();
        LocalDate start = from.getValue();
        LocalDate end = through.getValue();
        if (start == null || end == null || end.isBefore(start)) {
            feedback.setText("Khoảng ngày tra cứu không hợp lệ.");
            return;
        }
        Task<List<InvoiceSummary>> task = new Task<>() {
            @Override
            protected List<InvoiceSummary> call() {
                return controller.search(keyword, start, end);
            }
        };
        task.setOnSucceeded(event -> {
            if (request.get() == current) {
                invoices.getItems().setAll(task.getValue());
                feedback.setText(task.getValue().isEmpty() ? "Không tìm thấy dữ liệu." : "");
            }
        });
        task.setOnFailed(event -> {
            if (request.get() == current) {
                feedback.setText(task.getException() == null ? "Không thể tải hóa đơn."
                        : task.getException().getMessage());
            }
        });
        Thread worker = new Thread(task, "invoice-history-search");
        worker.setDaemon(true);
        worker.start();
    }

    private <T> TableColumn<T, String> column(String title, Function<T, String> value) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }
}
