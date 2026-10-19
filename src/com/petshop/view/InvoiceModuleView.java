package com.petshop.view;

import com.petshop.dao.AssignedOperationsDao;
import com.petshop.dao.AssignedOperationsDao.CatalogOption;
import com.petshop.dao.AssignedOperationsDao.InvoiceRow;
import com.petshop.dao.AssignedOperationsDao.LookupOption;
import com.petshop.dao.AssignedOperationsDao.PromotionOption;
import com.petshop.model.InvoiceItem;
import com.petshop.model.User;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
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
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/** Invoice search, creation, cancellation, and paid-invoice selection workflows. */
public class InvoiceModuleView extends VBox {

    private static final String UNPAID = "Chưa thanh toán";

    private final AssignedOperationsDao dao = new AssignedOperationsDao();
    private final User user;
    private final TextField txtSearch = new TextField();
    private final Label lblFeedback = new Label();
    private final TableView<InvoiceRow> tblInvoices = new TableView<>();

    public InvoiceModuleView(String actionId, String actionName, User user) {
        this.user = user;
        setSpacing(16);
        setPadding(new Insets(24, 28, 28, 28));
        setStyle("-fx-background-color: #f3f6f3;");

        Label lblTitle = new Label("Hóa đơn  /  " + actionName);
        lblTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");
        Label lblSubtitle = new Label("Lập, tra cứu và hủy hóa đơn theo trạng thái thanh toán.");
        lblSubtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #6e7c70;");
        getChildren().add(new VBox(4, lblTitle, lblSubtitle));

        tblInvoices.getColumns().addAll(
                column("Mã hóa đơn", 185, InvoiceRow::id),
                column("Ngày lập", 115, row -> row.date().toString()),
                column("Khách hàng", 175, InvoiceRow::customer),
                column("Nhân viên", 155, InvoiceRow::employee),
                column("Tổng tiền", 140, row -> money(row.total())),
                column("Trạng thái", 145, InvoiceRow::status),
                column("Thanh toán", 130, row -> value(row.paymentMethod())));
        tblInvoices.setPlaceholder(new Label("Không có hóa đơn phù hợp."));
        VBox.setVgrow(tblInvoices, Priority.ALWAYS);

        txtSearch.setPromptText("Mã hóa đơn, khách hàng hoặc trạng thái");
        txtSearch.getStyleClass().add("search-field");
        txtSearch.setPrefWidth(330);
        txtSearch.setOnAction(event -> loadInvoices());
        Button btnSearch = button("Tìm kiếm", false);
        btnSearch.setOnAction(event -> loadInvoices());
        Button btnRefresh = button("Làm mới", false);
        btnRefresh.setOnAction(event -> {
            txtSearch.clear();
            loadInvoices();
        });
        HBox toolbar = new HBox(8, txtSearch, btnSearch, btnRefresh);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        toolbar.getChildren().add(spacer);
        if ("INV_CREATE".equals(actionId)) {
            Button btnCreateInvoice = button("Lập hóa đơn", true);
            btnCreateInvoice.setOnAction(event -> createInvoice());
            toolbar.getChildren().add(btnCreateInvoice);
        } else if ("INV_CANCEL".equals(actionId)) {
            Button btnCancelInvoice = button("Hủy hóa đơn", true);
            btnCancelInvoice.setOnAction(event -> cancelInvoice());
            toolbar.getChildren().add(btnCancelInvoice);
        }

        VBox card = new VBox(12, toolbar, lblFeedback, tblInvoices);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #e5ebe5; -fx-border-radius: 8px;");
        VBox.setVgrow(card, Priority.ALWAYS);
        getChildren().add(card);
        loadInvoices();
    }

    private void loadInvoices() {
        try {
            tblInvoices.setItems(FXCollections.observableArrayList(dao.findInvoices(txtSearch.getText())));
            lblFeedback.setText("Hiển thị " + tblInvoices.getItems().size() + " hóa đơn.");
        } catch (RuntimeException exception) {
            tblInvoices.getItems().clear();
            lblFeedback.setText(exception.getMessage());
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
        List<PromotionOption> promotions;
        try {
            customers = new ArrayList<>(dao.findCustomerOptions());
            appointments = new ArrayList<>(dao.findAppointmentOptions());
            catalog = dao.findInvoiceCatalog();
            promotions = new ArrayList<>(dao.findActivePromotions(LocalDate.now()));
        } catch (RuntimeException exception) {
            showAlert(Alert.AlertType.ERROR, "Không tải được dữ liệu", exception.getMessage());
            return;
        }
        customers.add(0, new LookupOption("", "Khách vãng lai"));
        appointments.add(0, new LookupOption("", "Không gắn lịch hẹn"));
        promotions.add(0, new PromotionOption("", "Không áp dụng", 0));
        if (catalog.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Chưa có mặt hàng", "Cần có sản phẩm hoặc dịch vụ đang áp dụng giá trước khi lập hóa đơn.");
            return;
        }

        Dialog<InvoiceForm> dlgInvoice = new Dialog<>();
        dlgInvoice.setTitle("Lập hóa đơn");
        dlgInvoice.getDialogPane().setPrefWidth(760);
        ButtonType btnTypeSave = new ButtonType("Lưu hóa đơn", ButtonBar.ButtonData.OK_DONE);
        dlgInvoice.getDialogPane().getButtonTypes().addAll(btnTypeSave, ButtonType.CANCEL);

        ComboBox<LookupOption> cboCustomer = new ComboBox<>(FXCollections.observableArrayList(customers));
        ComboBox<LookupOption> cboAppointment = new ComboBox<>(FXCollections.observableArrayList(appointments));
        cboCustomer.getSelectionModel().selectFirst();
        cboAppointment.getSelectionModel().selectFirst();
        ComboBox<String> cboPaymentMethod = new ComboBox<>(FXCollections.observableArrayList(
                "Tiền mặt", "Chuyển khoản", "POS", UNPAID));
        cboPaymentMethod.getSelectionModel().selectFirst();
        DatePicker dtpInvoiceDate = new DatePicker(LocalDate.now());
        List<PromotionOption> activePromotions = new ArrayList<>(promotions);
        ObjectProperty<PromotionOption> propPromotion = new SimpleObjectProperty<>(promotions.get(0));
        Label lblPromotion = new Label("Khuyến mãi: " + propPromotion.get().name());
        Button btnChoosePromotion = button("Chọn khuyến mãi", false);
        btnChoosePromotion.setOnAction(event -> {
            ChoiceDialog<PromotionOption> dlgPromotion = new ChoiceDialog<>(propPromotion.get(),
                new ArrayList<>(activePromotions));
            dlgPromotion.setTitle("Chọn khuyến mãi đang hiệu lực");
            dlgPromotion.setHeaderText("Khuyến mãi áp dụng cho hóa đơn");
            dlgPromotion.setContentText("Chương trình:");
            dlgPromotion.showAndWait().ifPresent(propPromotion::set);
        });
        TextField txtQuantity = new TextField("1");
        txtQuantity.setPrefWidth(85);
        txtQuantity.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d{0,5}") ? change : null));
        TextField txtBarcode = new TextField();
        txtBarcode.setPromptText("Quét hoặc nhập mã vạch sản phẩm");
        txtBarcode.setTextFormatter(new TextFormatter<String>(change ->
            change.getControlNewText().length() <= 50 ? change : null));

        ObservableList<InvoiceItem> items = FXCollections.observableArrayList();
        TableView<InvoiceItem> tblInvoiceItems = new TableView<>(items);
        tblInvoiceItems.setPrefHeight(190);
        tblInvoiceItems.getColumns().addAll(
                column("Mặt hàng", 285, InvoiceItem::getTenHang),
                column("SL", 65, row -> Integer.toString(row.getSoLuong())),
                column("Đơn giá", 125, row -> money(row.getDonGia())),
                column("Thành tiền", 145, row -> money(row.getThanhTien())));
        tblInvoiceItems.setPlaceholder(new Label("Chưa có mặt hàng trong hóa đơn."));
        Label lblTotal = new Label("Tạm tính: 0 đ | Giảm: 0 đ | Thanh toán: 0 đ");
        lblTotal.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");

        Button btnChooseItem = button("Chọn mặt hàng", false);
        btnChooseItem.setOnAction(event -> chooseCatalogItem(catalog).ifPresent(option ->
                addInvoiceItem(option, txtQuantity, items, lblTotal, propPromotion.get())));
        Button btnRemoveItem = button("Bỏ mặt hàng", false);
        btnRemoveItem.setOnAction(event -> {
            InvoiceItem selected = tblInvoiceItems.getSelectionModel().getSelectedItem();
            if (selected != null) {
                items.remove(selected);
                updateTotal(items, lblTotal, propPromotion.get());
            }
        });
        Button btnScanBarcode = button("Thêm mã vạch", false);
        btnScanBarcode.setOnAction(event -> addProductByBarcode(txtBarcode, txtQuantity, items, lblTotal, propPromotion.get()));
        txtBarcode.setOnAction(event -> btnScanBarcode.fire());
        HBox itemControls = new HBox(8, txtQuantity, btnChooseItem, btnRemoveItem);
        itemControls.setAlignment(Pos.CENTER_LEFT);
        HBox barcodeControls = new HBox(8, txtBarcode, btnScanBarcode);
        HBox.setHgrow(txtBarcode, Priority.ALWAYS);
        HBox promotionControls = new HBox(8, lblPromotion, btnChoosePromotion);
        propPromotion.addListener((observable, oldValue, selected) -> {
            lblPromotion.setText("Khuyến mãi: " + selected.name());
            updateTotal(items, lblTotal, selected);
        });
        dtpInvoiceDate.valueProperty().addListener((observable, oldValue, selectedDate) -> {
            if (selectedDate == null) {
                return;
            }
            String selectedId = propPromotion.get().id();
            try {
                activePromotions.clear();
                activePromotions.addAll(dao.findActivePromotions(selectedDate));
                activePromotions.add(0, new PromotionOption("", "Không áp dụng", 0));
                PromotionOption updatedPromotion = activePromotions.stream()
                        .filter(option -> option.id().equals(selectedId)).findFirst().orElse(activePromotions.get(0));
                propPromotion.set(updatedPromotion);
            } catch (RuntimeException exception) {
                activePromotions.clear();
                activePromotions.add(new PromotionOption("", "Không áp dụng", 0));
                propPromotion.set(activePromotions.get(0));
                showAlert(Alert.AlertType.ERROR, "Không tải được khuyến mãi", exception.getMessage());
            }
        });

        GridPane gridFields = new GridPane();
        gridFields.setHgap(12);
        gridFields.setVgap(10);
        gridFields.addRow(0, new Label("Ngày lập"), dtpInvoiceDate);
        gridFields.addRow(1, new Label("Khách hàng"), cboCustomer);
        gridFields.addRow(2, new Label("Lịch hẹn"), cboAppointment);
        gridFields.addRow(3, new Label("Thanh toán"), cboPaymentMethod);
        gridFields.addRow(4, new Label("Khuyến mãi"), promotionControls);
        gridFields.add(new Label("Mặt hàng"), 0, 5);
        gridFields.add(itemControls, 1, 5);
        gridFields.add(new Label("Mã vạch"), 0, 6);
        gridFields.add(barcodeControls, 1, 6);
        gridFields.add(tblInvoiceItems, 0, 7, 2, 1);
        gridFields.add(lblTotal, 1, 8);
        dlgInvoice.getDialogPane().setContent(gridFields);

        Node btnSave = dlgInvoice.getDialogPane().lookupButton(btnTypeSave);
        btnSave.addEventFilter(ActionEvent.ACTION, event -> {
            boolean duplicateService = items.stream().filter(InvoiceItem::isService)
                    .map(InvoiceItem::getMaDichVu).distinct().count()
                    != items.stream().filter(InvoiceItem::isService).count();
                if (items.isEmpty() || duplicateService || cboPaymentMethod.getValue() == null || dtpInvoiceDate.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Kiểm tra hóa đơn", "Chọn ngày, phương thức thanh toán và thêm mặt hàng hợp lệ.");
                event.consume();
            }
        });
        dlgInvoice.setResultConverter(btnType -> btnType == btnTypeSave
                ? new InvoiceForm(cboCustomer.getValue().id(), cboAppointment.getValue().id(), dtpInvoiceDate.getValue(),
                    propPromotion.get().id(), UNPAID.equals(cboPaymentMethod.getValue()) ? null : cboPaymentMethod.getValue(),
                    List.copyOf(items))
                : null);
        dlgInvoice.showAndWait().ifPresent(form -> {
            try {
                dao.createInvoice(blankToNull(form.customerId()), blankToNull(form.appointmentId()),
                    user.getMaNhanVien(), form.invoiceDate(), blankToNull(form.promotionId()),
                    form.paymentMethod(), form.items());
                loadInvoices();
            } catch (RuntimeException exception) {
                showAlert(Alert.AlertType.ERROR, "Không lập được hóa đơn", exception.getMessage());
            }
        });
    }

    private Optional<CatalogOption> chooseCatalogItem(List<CatalogOption> catalog) {
        Dialog<CatalogOption> dlgCatalog = new Dialog<>();
        dlgCatalog.setTitle("Chọn sản phẩm hoặc dịch vụ");
        ButtonType btnTypeChoose = new ButtonType("Chọn mặt hàng", ButtonBar.ButtonData.OK_DONE);
        dlgCatalog.getDialogPane().getButtonTypes().addAll(btnTypeChoose, ButtonType.CANCEL);

        ComboBox<CatalogOption> cboCatalogItem = new ComboBox<>(FXCollections.observableArrayList(catalog));
        cboCatalogItem.setMaxWidth(Double.MAX_VALUE);
        cboCatalogItem.getSelectionModel().selectFirst();
        GridPane gridFields = new GridPane();
        gridFields.setHgap(12);
        gridFields.setVgap(10);
        gridFields.addRow(0, new Label("Mặt hàng"), cboCatalogItem);
        dlgCatalog.getDialogPane().setContent(gridFields);
        dlgCatalog.setResultConverter(btnType -> btnType == btnTypeChoose ? cboCatalogItem.getValue() : null);
        return dlgCatalog.showAndWait();
    }

    private void addInvoiceItem(CatalogOption option, TextField quantityField,
                                ObservableList<InvoiceItem> items, Label lblTotal, PromotionOption promotion) {
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
        updateTotal(items, lblTotal, promotion);
    }

    private void addProductByBarcode(TextField barcode, TextField quantityField,
                                     ObservableList<InvoiceItem> items, Label lblTotal, PromotionOption promotion) {
        if (barcode.getText().isBlank()) {
            return;
        }
        try {
            Optional<CatalogOption> product = dao.findProductByBarcode(barcode.getText());
            if (product.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Không tìm thấy sản phẩm", "Mã vạch không khớp sản phẩm đang bán.");
                return;
            }
            addInvoiceItem(product.get(), quantityField, items, lblTotal, promotion);
            barcode.clear();
        } catch (RuntimeException exception) {
            showAlert(Alert.AlertType.ERROR, "Không tra được mã vạch", exception.getMessage());
        }
    }

    private void cancelInvoice() {
        InvoiceRow selected = tblInvoices.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.INFORMATION, "Chưa chọn hóa đơn", "Chọn một hóa đơn trong bảng trước.");
            return;
        }
        if (!UNPAID.equals(selected.status())) {
            showAlert(Alert.AlertType.INFORMATION, "Không thể hủy", "Chỉ hóa đơn chưa thanh toán được hủy. Hóa đơn đã thanh toán cần xử lý bằng Phiếu đổi trả.");
            return;
        }
        Alert dlgConfirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Hóa đơn sẽ được chuyển sang trạng thái Đã hủy.", ButtonType.CANCEL, ButtonType.OK);
        dlgConfirm.setHeaderText("Hủy hóa đơn " + selected.id() + "?");
        if (dlgConfirm.showAndWait().filter(ButtonType.OK::equals).isEmpty()) {
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

    private static void updateTotal(List<InvoiceItem> items, Label lblTotal, PromotionOption promotion) {
        double subtotal = items.stream().mapToDouble(InvoiceItem::getThanhTien).sum();
        double discount = subtotal * (promotion == null ? 0 : promotion.percent()) / 100;
        lblTotal.setText("Tạm tính: " + money(subtotal) + " | Giảm: " + money(discount)
                + " | Thanh toán: " + money(subtotal - discount));
    }

    private static <T> TableColumn<T, String> column(String title, double width, Function<T, String> value) {
        TableColumn<T, String> tblColumn = new TableColumn<>(title);
        tblColumn.setPrefWidth(width);
        tblColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return tblColumn;
    }

    private static Button button(String text, boolean primary) {
        Button btn = new Button(text);
        btn.getStyleClass().add(primary ? "btn-action-primary" : "btn-action-secondary");
        return btn;
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

    /** Reusable chooser for the return module to select a previously paid invoice. */
    public static Optional<InvoiceRow> showPaidInvoicePicker() {
        AssignedOperationsDao invoiceDao = new AssignedOperationsDao();
        Dialog<InvoiceRow> dlgPaidInvoice = new Dialog<>();
        dlgPaidInvoice.setTitle("Chọn hóa đơn đã thanh toán");
        ButtonType btnTypeChoose = new ButtonType("Chọn hóa đơn", ButtonBar.ButtonData.OK_DONE);
        dlgPaidInvoice.getDialogPane().getButtonTypes().addAll(btnTypeChoose, ButtonType.CANCEL);

        TextField txtSearch = new TextField();
        txtSearch.setPromptText("Mã hóa đơn, khách hàng hoặc trạng thái");
        TableView<InvoiceRow> tblPaidInvoices = new TableView<>();
        tblPaidInvoices.getColumns().addAll(
                column("Mã hóa đơn", 185, InvoiceRow::id),
                column("Ngày lập", 115, row -> row.date().toString()),
                column("Khách hàng", 175, InvoiceRow::customer),
                column("Tổng tiền", 140, row -> money(row.total())),
                column("Thanh toán", 130, row -> value(row.paymentMethod())));
        tblPaidInvoices.setPlaceholder(new Label("Không có hóa đơn đã thanh toán phù hợp."));
        Runnable loadPaidInvoices = () -> {
            try {
            tblPaidInvoices.setItems(FXCollections.observableArrayList(invoiceDao.findPaidInvoices(txtSearch.getText())));
            } catch (RuntimeException exception) {
            tblPaidInvoices.getItems().clear();
                showAlert(Alert.AlertType.ERROR, "Không tải được hóa đơn", exception.getMessage());
            }
        };
        txtSearch.setOnAction(event -> loadPaidInvoices.run());
        Button btnSearch = button("Tìm kiếm", false);
        btnSearch.setOnAction(event -> loadPaidInvoices.run());
        HBox boxControls = new HBox(8, txtSearch, btnSearch);
        HBox.setHgrow(txtSearch, Priority.ALWAYS);
        VBox boxContent = new VBox(10, boxControls, tblPaidInvoices);
        boxContent.setPrefSize(760, 420);
        dlgPaidInvoice.getDialogPane().setContent(boxContent);
        Node btnChoose = dlgPaidInvoice.getDialogPane().lookupButton(btnTypeChoose);
        btnChoose.setDisable(true);
        tblPaidInvoices.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selected) ->
            btnChoose.setDisable(selected == null));
        loadPaidInvoices.run();
        dlgPaidInvoice.setResultConverter(btnType -> btnType == btnTypeChoose
            ? tblPaidInvoices.getSelectionModel().getSelectedItem() : null);
        return dlgPaidInvoice.showAndWait();
    }

    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message == null ? "Có lỗi không xác định." : message);
        alert.showAndWait();
    }

    private record InvoiceForm(String customerId, String appointmentId, LocalDate invoiceDate,
                               String promotionId, String paymentMethod,
                               List<InvoiceItem> items) {}
}
