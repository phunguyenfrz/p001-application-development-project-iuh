package com.petshop.view;

import com.petshop.dao.AssignedOperationsDao;
import com.petshop.dao.AssignedOperationsDao.CatalogOption;
import com.petshop.dao.AssignedOperationsDao.InvoiceRow;
import com.petshop.dao.AssignedOperationsDao.LookupOption;
import com.petshop.model.InvoiceItem;
import com.petshop.model.User;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class InvoiceModuleView extends VBox {

    private static final String UNPAID = "Chưa thanh toán";

    private final AssignedOperationsDao dao = new AssignedOperationsDao();
    private final String actionId;
    private final String actionName;
    private final User user;
    private final TextField searchField = new TextField();
    private final Label feedback = new Label();
    private final TableView<InvoiceRow> table = new TableView<>();

    public InvoiceModuleView(String actionId, String actionName, User user) {
        this.actionId = actionId;
        this.actionName = actionName;
        this.user = user;
        setSpacing(16);
        setPadding(new Insets(24, 28, 28, 28));
        setStyle("-fx-background-color: #f3f6f3;");

        Label title = new Label("Hóa đơn  /  " + actionName);
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");
        Label subtitle = new Label("Lập, tra cứu và hủy hóa đơn theo trạng thái thanh toán.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #6e7c70;");
        getChildren().add(new VBox(4, title, subtitle));

        table.getColumns().addAll(
                column("Mã hóa đơn", 185, InvoiceRow::id),
                column("Ngày lập", 115, row -> row.date().toString()),
                column("Khách hàng", 175, InvoiceRow::customer),
                column("Nhân viên", 155, InvoiceRow::employee),
                column("Tổng tiền", 140, row -> money(row.total())),
                column("Trạng thái", 145, InvoiceRow::status),
                column("Thanh toán", 130, row -> value(row.paymentMethod())));
        table.setPlaceholder(new Label("Không có hóa đơn phù hợp."));
        VBox.setVgrow(table, Priority.ALWAYS);

        searchField.setPromptText("Mã hóa đơn, khách hàng hoặc trạng thái");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(330);
        searchField.setOnAction(event -> loadInvoices());
        Button search = button("Tìm kiếm", false);
        search.setOnAction(event -> loadInvoices());
        Button refresh = button("Làm mới", false);
        refresh.setOnAction(event -> {
            searchField.clear();
            loadInvoices();
        });
        HBox toolbar = new HBox(8, searchField, search, refresh);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        toolbar.getChildren().add(spacer);
        if ("INV_CREATE".equals(actionId)) {
            Button create = button("Lập hóa đơn", true);
            create.setOnAction(event -> createInvoice());
            toolbar.getChildren().add(create);
        } else if ("INV_CANCEL".equals(actionId)) {
            Button cancel = button("Hủy hóa đơn", true);
            cancel.setOnAction(event -> cancelInvoice());
            toolbar.getChildren().add(cancel);
        }

        VBox card = new VBox(12, toolbar, feedback, table);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #e5ebe5; -fx-border-radius: 8px;");
        VBox.setVgrow(card, Priority.ALWAYS);
        getChildren().add(card);
        loadInvoices();
    }

    private void loadInvoices() {
        try {
            table.setItems(FXCollections.observableArrayList(dao.findInvoices(searchField.getText())));
            feedback.setText("Hiển thị " + table.getItems().size() + " hóa đơn.");
        } catch (RuntimeException exception) {
            table.getItems().clear();
            feedback.setText(exception.getMessage());
        }
    }

    private void createInvoice() {
        if (user == null || user.getMaNhanVien() == null) {
            showAlert(Alert.AlertType.ERROR, "Không lập được hóa đơn", "Không xác định được nhân viên đang đăng nhập.");
            return;
        }

        List<LookupOption> customers;
        List<LookupOption> appointments;
        List<CatalogOption> catalog;
        try {
            customers = new ArrayList<>(dao.findCustomerOptions());
            appointments = new ArrayList<>(dao.findAppointmentOptions());
            catalog = dao.findInvoiceCatalog();
        } catch (RuntimeException exception) {
            showAlert(Alert.AlertType.ERROR, "Không tải được dữ liệu", exception.getMessage());
            return;
        }
        customers.add(0, new LookupOption("", "Khách vãng lai"));
        appointments.add(0, new LookupOption("", "Không gắn lịch hẹn"));
        if (catalog.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Chưa có mặt hàng", "Cần có sản phẩm hoặc dịch vụ đang áp dụng giá trước khi lập hóa đơn.");
            return;
        }

        Dialog<InvoiceForm> dialog = new Dialog<>();
        dialog.setTitle("Lập hóa đơn");
        dialog.getDialogPane().setPrefWidth(760);
        ButtonType save = new ButtonType("Lưu hóa đơn", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        ComboBox<LookupOption> customer = new ComboBox<>(FXCollections.observableArrayList(customers));
        ComboBox<LookupOption> appointment = new ComboBox<>(FXCollections.observableArrayList(appointments));
        customer.getSelectionModel().selectFirst();
        appointment.getSelectionModel().selectFirst();
        ComboBox<String> payment = new ComboBox<>(FXCollections.observableArrayList(
                "Tiền mặt", "Chuyển khoản", "POS", UNPAID));
        payment.getSelectionModel().selectFirst();
        ComboBox<CatalogOption> itemPicker = new ComboBox<>(FXCollections.observableArrayList(catalog));
        itemPicker.setMaxWidth(Double.MAX_VALUE);
        itemPicker.getSelectionModel().selectFirst();
        TextField quantity = new TextField("1");
        quantity.setPrefWidth(85);
        quantity.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d{0,5}") ? change : null));
        itemPicker.valueProperty().addListener((observable, oldValue, item) -> {
            quantity.setText("1");
            quantity.setDisable(item != null && item.service());
        });

        ObservableList<InvoiceItem> items = FXCollections.observableArrayList();
        TableView<InvoiceItem> itemTable = new TableView<>(items);
        itemTable.setPrefHeight(190);
        itemTable.getColumns().addAll(
                column("Mặt hàng", 285, InvoiceItem::getTenHang),
                column("SL", 65, row -> Integer.toString(row.getSoLuong())),
                column("Đơn giá", 125, row -> money(row.getDonGia())),
                column("Thành tiền", 145, row -> money(row.getThanhTien())));
        itemTable.setPlaceholder(new Label("Chưa có mặt hàng trong hóa đơn."));
        Label total = new Label("Tổng cộng: 0 đ");
        total.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");

        Button addItem = button("Thêm mặt hàng", false);
        addItem.setOnAction(event -> addInvoiceItem(itemPicker, quantity, items, total));
        Button removeItem = button("Bỏ mặt hàng", false);
        removeItem.setOnAction(event -> {
            InvoiceItem selected = itemTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                items.remove(selected);
                updateTotal(items, total);
            }
        });
        HBox itemControls = new HBox(8, itemPicker, quantity, addItem, removeItem);
        itemControls.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(itemPicker, Priority.ALWAYS);

        GridPane fields = new GridPane();
        fields.setHgap(12);
        fields.setVgap(10);
        fields.addRow(0, new Label("Khách hàng"), customer);
        fields.addRow(1, new Label("Lịch hẹn"), appointment);
        fields.addRow(2, new Label("Thanh toán"), payment);
        fields.add(new Label("Mặt hàng"), 0, 3);
        fields.add(itemControls, 1, 3);
        fields.add(itemTable, 0, 4, 2, 1);
        fields.add(total, 1, 5);
        dialog.getDialogPane().setContent(fields);

        Node saveButton = dialog.getDialogPane().lookupButton(save);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            boolean duplicateService = items.stream().filter(InvoiceItem::isService)
                    .map(InvoiceItem::getMaDichVu).distinct().count()
                    != items.stream().filter(InvoiceItem::isService).count();
                    if (items.isEmpty() || duplicateService || payment.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Kiểm tra hóa đơn", "Thêm ít nhất một mặt hàng; mỗi dịch vụ chỉ được tính một lần.");
                event.consume();
            }
        });
        dialog.setResultConverter(button -> button == save
                ? new InvoiceForm(customer.getValue().id(), appointment.getValue().id(),
                        UNPAID.equals(payment.getValue()) ? null : payment.getValue(), List.copyOf(items))
                : null);
        dialog.showAndWait().ifPresent(form -> {
            try {
                dao.createInvoice(blankToNull(form.customerId()), blankToNull(form.appointmentId()),
                        user.getMaNhanVien(), form.paymentMethod(), form.items());
                loadInvoices();
            } catch (RuntimeException exception) {
                showAlert(Alert.AlertType.ERROR, "Không lập được hóa đơn", exception.getMessage());
            }
        });
    }

    private void addInvoiceItem(ComboBox<CatalogOption> picker, TextField quantityField,
                                ObservableList<InvoiceItem> items, Label total) {
        CatalogOption option = picker.getValue();
        if (option == null) {
            return;
        }
        int count = 1;
        if (!option.service()) {
            try {
                count = Integer.parseInt(quantityField.getText());
                if (count < 1) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException exception) {
                showAlert(Alert.AlertType.WARNING, "Số lượng không hợp lệ", "Số lượng sản phẩm phải là số nguyên lớn hơn 0.");
                return;
            }
        }

        Optional<InvoiceItem> existing = items.stream().filter(item -> option.service()
                ? item.isService() && option.id().equals(item.getMaDichVu())
                : !item.isService() && option.id().equals(item.getMaSanPham())).findFirst();
        if (option.service() && existing.isPresent()) {
            showAlert(Alert.AlertType.INFORMATION, "Dịch vụ đã có", "Mỗi dịch vụ chỉ được thêm một lần vào hóa đơn.");
            return;
        }
        if (existing.isPresent()) {
            existing.get().setSoLuong(existing.get().getSoLuong() + count);
            items.set(items.indexOf(existing.get()), existing.get());
        } else {
            items.add(new InvoiceItem(option.service() ? null : option.id(), option.service() ? option.id() : null,
                    option.name(), count, option.price()));
        }
        updateTotal(items, total);
    }

    private void cancelInvoice() {
        InvoiceRow selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.INFORMATION, "Chưa chọn hóa đơn", "Chọn một hóa đơn trong bảng trước.");
            return;
        }
        if (!UNPAID.equals(selected.status())) {
            showAlert(Alert.AlertType.INFORMATION, "Không thể hủy", "Chỉ hóa đơn chưa thanh toán được hủy. Hóa đơn đã thanh toán cần xử lý bằng Phiếu đổi trả.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Hóa đơn sẽ được chuyển sang trạng thái Đã hủy.", ButtonType.CANCEL, ButtonType.OK);
        confirm.setHeaderText("Hủy hóa đơn " + selected.id() + "?");
        if (confirm.showAndWait().filter(ButtonType.OK::equals).isEmpty()) {
            return;
        }
        try {
            if (!dao.cancelInvoice(selected.id())) {
                showAlert(Alert.AlertType.WARNING, "Không hủy được", "Hóa đơn đã thay đổi trạng thái hoặc không còn tồn tại.");
            }
            loadInvoices();
        } catch (RuntimeException exception) {
            showAlert(Alert.AlertType.ERROR, "Không hủy được", exception.getMessage());
        }
    }

    private static void updateTotal(List<InvoiceItem> items, Label total) {
        total.setText("Tổng cộng: " + money(items.stream().mapToDouble(InvoiceItem::getThanhTien).sum()));
    }

    private static <T> TableColumn<T, String> column(String title, double width, Function<T, String> value) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }

    private static Button button(String text, boolean primary) {
        Button button = new Button(text);
        button.getStyleClass().add(primary ? "btn-action-primary" : "btn-action-secondary");
        return button;
    }

    private static String money(double amount) {
        return String.format("%,.0f đ", amount);
    }

    private static String value(String text) {
        return text == null ? "" : text;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message == null ? "Có lỗi không xác định." : message);
        alert.showAndWait();
    }

    private record InvoiceForm(String customerId, String appointmentId, String paymentMethod,
                               List<InvoiceItem> items) {}
}
