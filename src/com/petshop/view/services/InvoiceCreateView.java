package com.petshop.view.services;

import com.petshop.dao.InvoiceDao;
import com.petshop.model.Invoice;
import com.petshop.model.InvoiceItem;
import com.petshop.model.Product;
import com.petshop.model.User;
import com.petshop.service.InvoiceService;
import com.petshop.view.product.ProductPickerDialog;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Màn lập hóa đơn và thu tiền. */
public class InvoiceCreateView extends BorderPane {

    private final User user;
    private final InvoiceService invoiceService = new InvoiceService();
    private final Invoice invoice = new Invoice();
    private final ObservableList<InvoiceItem> invoiceItems = FXCollections.observableArrayList();

    private final ComboBox<InvoiceDao.CustomerOption> cboCustomer = new ComboBox<>();
    private final ComboBox<InvoiceDao.PetOption> cboPet = new ComboBox<>();
    private final ComboBox<InvoiceDao.PromotionOption> cboPromotion = new ComboBox<>();
    private final DatePicker dtpInvoiceDate = new DatePicker(LocalDate.now());
    private final TextField txtManualDiscount = new TextField("0");
    private final TextField txtBarcode = new TextField();
    private final TextField txtCashReceived = new TextField("0");
    private final TableView<InvoiceItem> tblInvoiceItems = new TableView<>(invoiceItems);
    private final Label lblAppointmentBadge = new Label();
    private final Label lblSubtotal = new Label("0 đ");
    private final Label lblPromotionDiscount = new Label("0 đ");
    private final Label lblTax = new Label("0 đ");
    private final Label lblTotal = new Label("0 đ");
    private final Label lblChange = new Label("0 đ");
    private final Label lblPaymentPermission = new Label();
    private final Label lblItemCount = new Label("Chưa có mặt hàng");
    private final Button btnChooseProduct = new Button("Chọn sản phẩm");
    private final Button btnAddBarcode = new Button("Thêm mã vạch");
    private final Button btnChooseService = new Button("Thêm dịch vụ");
    private final Button btnSavePending = new Button("Lưu chờ thanh toán");
    private final Button btnPayAndCreate = new Button("Thanh toán & lập hóa đơn");
    private final RadioButton rdoCash = new RadioButton("Tiền mặt");
    private final RadioButton rdoTransfer = new RadioButton("Chuyển khoản");
    private final CheckBox chkPrintInvoice = new CheckBox("In hóa đơn sau khi thanh toán");
    private final VBox pnlCashReceived = new VBox(6);
    private String appointmentId;
    private boolean isLoadingCustomer;
    private double currentTotal;

    public InvoiceCreateView(User user) {
        this(user, null);
    }

    /** Cho phép màn lịch hẹn mở hóa đơn với các dịch vụ đã đặt được khóa sẵn. */
    public InvoiceCreateView(User user, String appointmentId) {
        this.user = user;
        this.appointmentId = appointmentId;
        setPadding(new Insets(20, 24, 24, 24));
        getStyleClass().add("invoice-create-page");
        buildView();
        loadCustomers();
        loadPromotions(dtpInvoiceDate.getValue());
        if (appointmentId != null && !appointmentId.isBlank()) {
            loadAppointment(appointmentId);
        }
    }

    private void buildView() {
        VBox pnlPage = new VBox(16);
        HBox pnlTitleRow = new HBox(12);
        pnlTitleRow.setAlignment(Pos.CENTER_LEFT);
        Label lblTitle = new Label("Lập hóa đơn");
        lblTitle.getStyleClass().add("invoice-page-title");
        Region pnlTitleSpacer = new Region();
        HBox.setHgrow(pnlTitleSpacer, Priority.ALWAYS);
        lblAppointmentBadge.getStyleClass().add("invoice-appointment-badge");
        lblAppointmentBadge.setVisible(false);
        lblAppointmentBadge.setManaged(false);
        pnlTitleRow.getChildren().addAll(lblTitle, pnlTitleSpacer, lblAppointmentBadge);

        HBox pnlColumns = new HBox(16, createMainColumn(), createPaymentSummary());
        Node nodeMainColumn = pnlColumns.getChildren().get(0);
        Node nodeSummaryColumn = pnlColumns.getChildren().get(1);
        HBox.setHgrow(nodeMainColumn, Priority.ALWAYS);
        ((Region) nodeMainColumn).setMinWidth(0);
        ((Region) nodeSummaryColumn).setMinWidth(285);
        ((Region) nodeSummaryColumn).setPrefWidth(310);

        pnlPage.getChildren().addAll(pnlTitleRow, pnlColumns);
        setCenter(pnlPage);
        configureListeners();
        refreshTotals();
    }

    private VBox createMainColumn() {
        VBox pnlMainColumn = new VBox(14);
        pnlMainColumn.setFillWidth(true);
        pnlMainColumn.getChildren().addAll(createCustomerCard(), createItemsCard(), createNoteCard());
        return pnlMainColumn;
    }

    private VBox createCustomerCard() {
        Label lblCardTitle = new Label("Thông tin khách hàng");
        lblCardTitle.getStyleClass().add("invoice-card-title");
        Label lblCustomerField = fieldLabel("Tìm khách hàng *");
        cboCustomer.setPromptText("Chọn khách hàng");
        cboCustomer.setMaxWidth(Double.MAX_VALUE);
        cboCustomer.getStyleClass().add("invoice-combo");

        Label lblPetField = fieldLabel("Thú cưng");
        cboPet.setPromptText("Chọn thú cưng của khách");
        cboPet.setMaxWidth(Double.MAX_VALUE);
        cboPet.setDisable(true);
        cboPet.getStyleClass().add("invoice-combo");
        Label lblInvoiceDateField = fieldLabel("Ngày lập *");
        dtpInvoiceDate.getStyleClass().add("invoice-date-picker");

        GridPane pnlFields = new GridPane();
        pnlFields.setHgap(12);
        pnlFields.setVgap(6);
        ColumnConstraints colLabel = new ColumnConstraints();
        colLabel.setPercentWidth(100);
        pnlFields.getColumnConstraints().add(colLabel);
        pnlFields.add(lblCustomerField, 0, 0);
        pnlFields.add(cboCustomer, 0, 1);
        pnlFields.add(lblPetField, 0, 2);
        pnlFields.add(cboPet, 0, 3);
        pnlFields.add(lblInvoiceDateField, 0, 4);
        pnlFields.add(dtpInvoiceDate, 0, 5);
        return createCard("invoice-card", lblCardTitle, pnlFields);
    }

    private VBox createItemsCard() {
        Label lblCardTitle = new Label("Sản phẩm & dịch vụ");
        lblCardTitle.getStyleClass().add("invoice-card-title");
        Label lblSubtitle = new Label("Thêm mặt hàng hoặc dịch vụ vào hóa đơn.");
        lblSubtitle.getStyleClass().add("invoice-muted");

        txtBarcode.setPromptText("Quét hoặc nhập mã vạch sản phẩm");
        txtBarcode.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().length() <= 50 ? change : null));
        txtBarcode.getStyleClass().add("invoice-input");
        btnChooseProduct.getStyleClass().addAll("btn-action-secondary", "invoice-product-action");
        btnAddBarcode.getStyleClass().addAll("btn-action-secondary", "invoice-product-action");
        btnChooseService.getStyleClass().add("btn-action-secondary");
        btnChooseProduct.setOnAction(event -> chooseProduct());
        btnAddBarcode.setOnAction(event -> addProductByBarcode());
        btnChooseService.setOnAction(event -> chooseService());
        txtBarcode.setOnAction(event -> addProductByBarcode());

        HBox pnlItemActions = new HBox(8, txtBarcode, btnAddBarcode, btnChooseProduct, btnChooseService);
        pnlItemActions.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(txtBarcode, Priority.ALWAYS);

        configureItemTable();
        lblItemCount.getStyleClass().add("invoice-muted");

        VBox pnlItemsCard = createCard("invoice-card", lblCardTitle, lblSubtitle, pnlItemActions,
                tblInvoiceItems, lblItemCount);
        VBox.setVgrow(tblInvoiceItems, Priority.ALWAYS);

        if (!invoiceService.canAddProducts(user)) {
            btnChooseProduct.setVisible(false);
            btnChooseProduct.setManaged(false);
            btnAddBarcode.setVisible(false);
            btnAddBarcode.setManaged(false);
            txtBarcode.setVisible(false);
            txtBarcode.setManaged(false);
        }
        return pnlItemsCard;
    }

    private void configureItemTable() {
        tblInvoiceItems.getStyleClass().add("invoice-item-table");
        tblInvoiceItems.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tblInvoiceItems.setPlaceholder(new Label("Chưa có sản phẩm hoặc dịch vụ."));
        tblInvoiceItems.getColumns().add(textColumn("Sản phẩm / dịch vụ", 0.46, InvoiceItem::getTenHienThi));
        tblInvoiceItems.getColumns().add(quantityColumn());
        tblInvoiceItems.getColumns().add(textColumn("Đơn giá", 0.17, item -> money(item.getDonGia())));
        tblInvoiceItems.getColumns().add(textColumn("Thành tiền", 0.18, item -> money(item.thanhTien())));
        tblInvoiceItems.getColumns().add(removeColumn());
    }

    private TableColumn<InvoiceItem, String> textColumn(String title, double width,
                                                        java.util.function.Function<InvoiceItem, String> value) {
        TableColumn<InvoiceItem, String> tblColumn = new TableColumn<>(title);
        tblColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        tblColumn.setMinWidth(75);
        tblColumn.setPrefWidth(width * 680);
        return tblColumn;
    }

    private TableColumn<InvoiceItem, InvoiceItem> quantityColumn() {
        TableColumn<InvoiceItem, InvoiceItem> tblColumn = new TableColumn<>("Số lượng");
        tblColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        tblColumn.setPrefWidth(130);
        tblColumn.setCellFactory(column -> new TableCell<>() {
            private final Button btnDecrease = new Button("−");
            private final Label lblQuantity = new Label();
            private final Button btnIncrease = new Button("+");
            private final HBox pnlStepper = new HBox(6, btnDecrease, lblQuantity, btnIncrease);

            {
                btnDecrease.getStyleClass().add("invoice-stepper-button");
                btnIncrease.getStyleClass().add("invoice-stepper-button");
                pnlStepper.setAlignment(Pos.CENTER);
                btnDecrease.setOnAction(event -> changeQuantity(-1));
                btnIncrease.setOnAction(event -> changeQuantity(1));
            }

            private void changeQuantity(int amount) {
                InvoiceItem item = getTableRow() == null ? null : getTableRow().getItem();
                if (item == null || item.isKhoa() || item.isService()) return;
                item.setSoLuong(Math.max(1, item.getSoLuong() + amount));
                tblInvoiceItems.refresh();
                refreshTotals();
            }

            @Override
            protected void updateItem(InvoiceItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                lblQuantity.setText(Integer.toString(item.getSoLuong()));
                boolean canChangeQuantity = !item.isKhoa() && !item.isService();
                btnDecrease.setDisable(!canChangeQuantity || item.getSoLuong() <= 1);
                btnIncrease.setDisable(!canChangeQuantity);
                setGraphic(pnlStepper);
            }
        });
        return tblColumn;
    }

    private TableColumn<InvoiceItem, InvoiceItem> removeColumn() {
        TableColumn<InvoiceItem, InvoiceItem> tblColumn = new TableColumn<>("");
        tblColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        tblColumn.setPrefWidth(42);
        tblColumn.setCellFactory(column -> new TableCell<>() {
            private final Button btnRemove = new Button("×");

            {
                btnRemove.getStyleClass().add("invoice-remove-button");
                btnRemove.setOnAction(event -> {
                    InvoiceItem item = getTableRow() == null ? null : getTableRow().getItem();
                    if (item == null || item.isKhoa()) return;
                    invoiceItems.remove(item);
                    refreshTotals();
                });
            }

            @Override
            protected void updateItem(InvoiceItem item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null || item.isKhoa() ? null : btnRemove);
            }
        });
        return tblColumn;
    }

    private VBox createPaymentSummary() {
        VBox pnlSummary = new VBox(12);
        pnlSummary.getStyleClass().add("invoice-card");
        pnlSummary.setMinWidth(285);

        Label lblSummaryTitle = new Label("Tổng kết thanh toán");
        lblSummaryTitle.getStyleClass().add("invoice-card-title");
        Label lblInvoiceDate = new Label("Hóa đơn mới · " + dtpInvoiceDate.getValue().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblInvoiceDate.getStyleClass().add("invoice-muted");

        Label lblSubtotalCaption = new Label("Tạm tính");
        Label lblManualDiscountCaption = new Label("Giảm giá hóa đơn");
        Label lblPromotionCaption = new Label("Giảm khuyến mãi");
        Label lblTaxCaption = new Label("Thuế GTGT (0%)");
        Label lblTotalCaption = new Label("Tổng thanh toán");
        for (Label lbl : List.of(lblSubtotalCaption, lblManualDiscountCaption, lblPromotionCaption, lblTaxCaption)) {
            lbl.getStyleClass().add("invoice-summary-caption");
        }

        HBox pnlSubtotal = valueRow(lblSubtotalCaption, lblSubtotal);
        txtManualDiscount.setPromptText("0");
        txtManualDiscount.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d{0,12}") ? change : null));
        txtManualDiscount.getStyleClass().add("invoice-input");
        HBox pnlManualDiscount = new HBox(8, lblManualDiscountCaption, txtManualDiscount);
        pnlManualDiscount.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(lblManualDiscountCaption, Priority.ALWAYS);

        cboPromotion.setPromptText("Chọn mã khuyến mãi");
        cboPromotion.setMaxWidth(Double.MAX_VALUE);
        cboPromotion.getStyleClass().add("invoice-combo");
        Label lblPromotionField = fieldLabel("Mã khuyến mãi");
        HBox pnlPromotionDiscount = valueRow(lblPromotionCaption, lblPromotionDiscount);
        HBox pnlTax = valueRow(lblTaxCaption, lblTax);
        Region pnlDivider = new Region();
        pnlDivider.getStyleClass().add("invoice-summary-divider");

        lblTotalCaption.getStyleClass().add("invoice-total-caption");
        lblTotal.getStyleClass().add("invoice-total-value");
        VBox pnlTotalBox = new VBox(4, lblTotalCaption, lblTotal);
        pnlTotalBox.getStyleClass().add("invoice-total-box");

        Label lblPaymentMethod = fieldLabel("Phương thức thanh toán");
        ToggleGroup grpPaymentMethod = new ToggleGroup();
        rdoCash.setToggleGroup(grpPaymentMethod);
        rdoTransfer.setToggleGroup(grpPaymentMethod);
        rdoCash.setSelected(true);
        rdoCash.getStyleClass().add("invoice-payment-option");
        rdoTransfer.getStyleClass().add("invoice-payment-option");
        HBox pnlMethods = new HBox(8, rdoCash, rdoTransfer);
        pnlMethods.setAlignment(Pos.CENTER_LEFT);

        Label lblCashReceived = fieldLabel("Tiền khách đưa");
        txtCashReceived.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d{0,14}") ? change : null));
        txtCashReceived.getStyleClass().add("invoice-input");
        pnlCashReceived.getChildren().addAll(lblCashReceived, txtCashReceived);
        HBox pnlChange = valueRow(new Label("Tiền thừa trả khách"), lblChange);
        lblChange.getStyleClass().add("invoice-change-value");

        chkPrintInvoice.getStyleClass().add("invoice-print-option");
        lblPaymentPermission.getStyleClass().add("invoice-permission-warning");
        lblPaymentPermission.setWrapText(true);
        lblPaymentPermission.setVisible(false);
        lblPaymentPermission.setManaged(false);

        btnSavePending.getStyleClass().addAll("btn-action-secondary", "invoice-secondary-button");
        btnPayAndCreate.getStyleClass().add("invoice-primary-button");
        btnSavePending.setMaxWidth(Double.MAX_VALUE);
        btnPayAndCreate.setMaxWidth(Double.MAX_VALUE);
        btnSavePending.setOnAction(event -> savePending());
        btnPayAndCreate.setOnAction(event -> payInvoice());
        HBox pnlActions = new HBox(8, btnSavePending, btnPayAndCreate);
        pnlActions.setAlignment(Pos.CENTER_RIGHT);

        pnlSummary.getChildren().addAll(lblSummaryTitle, lblInvoiceDate,
                pnlSubtotal, pnlManualDiscount, lblPromotionField, cboPromotion,
                pnlPromotionDiscount, pnlTax, pnlDivider, pnlTotalBox,
                lblPaymentMethod, pnlMethods, pnlCashReceived, pnlChange,
                chkPrintInvoice, lblPaymentPermission, pnlActions);
        return pnlSummary;
    }

    private VBox createNoteCard() {
        Label lblTitle = new Label("Thông tin bổ sung");
        lblTitle.getStyleClass().add("invoice-card-title");
        TextField txtNote = new TextField();
        txtNote.setPromptText("Ghi chú hóa đơn");
        txtNote.getStyleClass().add("invoice-input");
        txtNote.textProperty().addListener((observable, oldValue, newValue) -> invoice.setGhiChu(newValue));
        return createCard("invoice-card", lblTitle, txtNote);
    }

    private void configureListeners() {
        cboCustomer.valueProperty().addListener((observable, oldValue, customer) -> {
            if (isLoadingCustomer) return;
            invoice.setMaKhachHang(customer == null ? null : customer.id());
            invoiceItems.removeIf(item -> item.isService() && !item.isKhoa());
            cboPet.getItems().clear();
            cboPet.getSelectionModel().clearSelection();
            cboPet.setDisable(customer == null);
            if (customer != null) {
                try {
                    cboPet.setItems(FXCollections.observableArrayList(invoiceService.findPets(customer.id())));
                } catch (RuntimeException exception) {
                    showAlert(Alert.AlertType.ERROR, "Không tải được thú cưng", exception.getMessage());
                }
            }
            invoice.setMaThuCung(null);
            refreshTotals();
        });
        cboPet.valueProperty().addListener((observable, oldValue, pet) -> {
            invoice.setMaThuCung(pet == null ? null : pet.id());
        });
        cboPromotion.valueProperty().addListener((observable, oldValue, promotion) -> refreshTotals());
        txtManualDiscount.textProperty().addListener((observable, oldValue, value) -> refreshTotals());
        txtCashReceived.textProperty().addListener((observable, oldValue, value) -> refreshCashChange());
        rdoCash.selectedProperty().addListener((observable, oldValue, selected) -> updatePaymentControls());
        rdoTransfer.selectedProperty().addListener((observable, oldValue, selected) -> updatePaymentControls());
        dtpInvoiceDate.valueProperty().addListener((observable, oldValue, date) -> {
            if (date != null) loadPromotions(date);
        });
        invoiceItems.addListener((javafx.collections.ListChangeListener<InvoiceItem>) change -> refreshTotals());
    }

    private void loadCustomers() {
        try {
            cboCustomer.setItems(FXCollections.observableArrayList(invoiceService.findCustomers("")));
        } catch (RuntimeException exception) {
            showAlert(Alert.AlertType.ERROR, "Không tải được khách hàng", exception.getMessage());
        }
    }

    private void loadPromotions(LocalDate date) {
        if (date == null) return;
        InvoiceDao.PromotionOption selected = cboPromotion.getValue();
        try {
            cboPromotion.setItems(FXCollections.observableArrayList(invoiceService.findActivePromotions(date)));
            if (selected != null) {
                cboPromotion.getItems().stream().filter(option -> option.id().equals(selected.id()))
                        .findFirst().ifPresent(cboPromotion::setValue);
            }
        } catch (RuntimeException exception) {
            cboPromotion.getItems().clear();
            showAlert(Alert.AlertType.ERROR, "Không tải được khuyến mãi", exception.getMessage());
        }
        refreshTotals();
    }

    private void loadAppointment(String selectedAppointmentId) {
        try {
            InvoiceDao.AppointmentDraft draft = invoiceService.findAppointmentDraft(selectedAppointmentId);
            appointmentId = draft.appointmentId();
            invoice.setMaLichHen(appointmentId);
            lblAppointmentBadge.setText("Từ lịch hẹn " + appointmentId);
            lblAppointmentBadge.setVisible(true);
            lblAppointmentBadge.setManaged(true);
            isLoadingCustomer = true;
            cboCustomer.getItems().removeIf(option -> option.id().equals(draft.customer().id()));
            cboCustomer.getItems().add(0, draft.customer());
            cboCustomer.setValue(draft.customer());
            cboCustomer.setDisable(true);
            invoice.setMaKhachHang(draft.customer().id());
            cboPet.setItems(FXCollections.observableArrayList(invoiceService.findPets(draft.customer().id())));
            cboPet.getItems().removeIf(option -> option.id().equals(draft.pet().id()));
            cboPet.getItems().add(0, draft.pet());
            cboPet.setValue(draft.pet());
            cboPet.setDisable(true);
            invoice.setMaThuCung(draft.pet().id());
            invoiceItems.setAll(draft.items());
            isLoadingCustomer = false;
            tblInvoiceItems.refresh();
            refreshTotals();
        } catch (RuntimeException exception) {
            isLoadingCustomer = false;
            showAlert(Alert.AlertType.ERROR, "Không tải được lịch hẹn", exception.getMessage());
        }
    }

    private void chooseProduct() {
        if (!invoiceService.canAddProducts(user)) return;
        Window owner = getScene() == null ? null : getScene().getWindow();
        ProductPickerDialog.showAndPick(owner).ifPresent(this::addProduct);
    }

    private void addProductByBarcode() {
        if (!invoiceService.canAddProducts(user) || txtBarcode.getText().isBlank()) return;
        try {
            Optional<Product> product = ProductPickerDialog.lookupByBarcode(txtBarcode.getText().trim());
            if (product.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Không tìm thấy sản phẩm", "Mã vạch không khớp sản phẩm đang bán.");
                return;
            }
            addProduct(product.get());
            txtBarcode.clear();
        } catch (RuntimeException exception) {
            showAlert(Alert.AlertType.ERROR, "Không tra được mã vạch", exception.getMessage());
        }
    }

    private void addProduct(Product product) {
        Optional<InvoiceItem> existing = invoiceItems.stream()
                .filter(item -> item.getLoaiDong() == InvoiceItem.LineType.SAN_PHAM
                        && product.getMaSanPham().equals(item.getMaSanPham()))
                .findFirst();
        if (existing.isPresent()) {
            InvoiceItem item = existing.get();
            item.setSoLuong(item.getSoLuong() + 1);
            tblInvoiceItems.refresh();
        } else {
            invoiceItems.add(new InvoiceItem(product.getMaSanPham(), null,
                    product.getTenSanPham(), 1, product.getGiaBan()));
        }
        refreshTotals();
    }

    private void chooseService() {
        InvoiceDao.PetOption selectedPet = cboPet.getValue();
        if (selectedPet == null) {
            showAlert(Alert.AlertType.INFORMATION, "Chưa chọn thú cưng", "Chọn khách hàng và thú cưng trước khi thêm dịch vụ.");
            return;
        }
        try {
            List<InvoiceDao.ServiceOption> options = invoiceService.findServicesForPet(
                    selectedPet.weight(), dtpInvoiceDate.getValue());
            if (options.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Chưa có dịch vụ", "Không có dịch vụ phù hợp với cân nặng thú cưng vào ngày đã chọn.");
                return;
            }
            javafx.scene.control.ChoiceDialog<InvoiceDao.ServiceOption> dlgService =
                    new javafx.scene.control.ChoiceDialog<>(options.get(0), options);
            dlgService.setTitle("Chọn dịch vụ");
            dlgService.setHeaderText("Dịch vụ theo bậc cân nặng " + petSize(selectedPet.weight()));
            dlgService.setContentText("Dịch vụ:");
            dlgService.showAndWait().ifPresent(this::addService);
        } catch (RuntimeException exception) {
            showAlert(Alert.AlertType.ERROR, "Không tải được dịch vụ", exception.getMessage());
        }
    }

    private void addService(InvoiceDao.ServiceOption service) {
        boolean alreadyAdded = invoiceItems.stream().anyMatch(item -> item.isService()
                && service.id().equals(item.getMaDichVu()));
        if (alreadyAdded) {
            showAlert(Alert.AlertType.INFORMATION, "Dịch vụ đã có", "Dịch vụ này đã nằm trong hóa đơn.");
            return;
        }
        invoiceItems.add(new InvoiceItem(null, service.id(), service.name() + " · " + service.size(),
                1, service.price()));
        refreshTotals();
    }

    private void refreshTotals() {
        double subtotal = invoiceItems.stream().mapToDouble(InvoiceItem::thanhTien).sum();
        double manualDiscount = parseAmount(txtManualDiscount.getText());
        double promotionPercent = cboPromotion.getValue() == null ? 0 : cboPromotion.getValue().percent();
        double promotionDiscount = invoiceService.calculatePromotionDiscount(subtotal, promotionPercent);
        double tax = 0;
        invoice.setItems(new ArrayList<>(invoiceItems));
        invoice.setGiamGia(Math.min(manualDiscount, subtotal));
        invoice.setGiamTheoKhuyenMai(Math.min(promotionDiscount, Math.max(0, subtotal - invoice.getGiamGia())));
        invoice.setThue(tax);
        lblSubtotal.setText(money(subtotal));
        lblPromotionDiscount.setText("−" + money(invoice.getGiamTheoKhuyenMai()));
        lblTax.setText(money(tax));
        currentTotal = invoice.tinhTongThanhToan();
        lblTotal.setText(money(currentTotal));
        lblItemCount.setText(invoiceItems.size() + " mặt hàng · Tổng số lượng: "
                + invoiceItems.stream().mapToInt(InvoiceItem::getSoLuong).sum());
        boolean cannotCollect = !invoiceService.canCollect(invoice, user) && invoice.coSanPham();
        lblPaymentPermission.setText("Hóa đơn có sản phẩm chỉ nhân viên Bán hàng hoặc Admin được thu tiền.");
        lblPaymentPermission.setVisible(cannotCollect);
        lblPaymentPermission.setManaged(cannotCollect);
        btnPayAndCreate.setDisable(invoiceItems.isEmpty() || cannotCollect);
        refreshCashChange();
    }

    private void refreshCashChange() {
        double change = Math.max(0, parseAmount(txtCashReceived.getText()) - currentTotal);
        lblChange.setText(money(change));
        boolean isCash = rdoCash.isSelected();
        pnlCashReceived.setVisible(isCash);
        pnlCashReceived.setManaged(isCash);
    }

    private void updatePaymentControls() {
        refreshCashChange();
    }

    private void savePending() {
        try {
            Invoice invoiceToSave = buildInvoice();
            String invoiceId = invoiceService.savePending(invoiceToSave);
            lblAppointmentBadge.setText("Đã lưu chờ thanh toán · " + invoiceId);
            lblAppointmentBadge.setVisible(true);
            lblAppointmentBadge.setManaged(true);
            showAlert(Alert.AlertType.INFORMATION, "Đã lưu hóa đơn", "Hóa đơn " + invoiceId + " đang chờ thanh toán.");
        } catch (RuntimeException exception) {
            showAlert(Alert.AlertType.WARNING, "Không lưu được hóa đơn", exception.getMessage());
        }
    }

    private void payInvoice() {
        try {
            Invoice invoiceToSave = buildInvoice();
            String paymentMethod = rdoCash.isSelected() ? "Tiền mặt" : "Chuyển khoản";
            String invoiceId = invoiceService.pay(invoiceToSave, user, paymentMethod,
                    parseAmount(txtCashReceived.getText()));
            showAlert(Alert.AlertType.INFORMATION, "Thanh toán thành công",
                    "Đã lập và thanh toán hóa đơn " + invoiceId + ".");
        } catch (RuntimeException exception) {
            showAlert(Alert.AlertType.WARNING, "Không thanh toán được", exception.getMessage());
        }
    }

    private Invoice buildInvoice() {
        Invoice invoiceToSave = new Invoice();
        invoiceToSave.setMaNhanVien(user == null ? null : user.getMaNhanVien());
        invoiceToSave.setMaKhachHang(cboCustomer.getValue() == null ? null : cboCustomer.getValue().id());
        invoiceToSave.setMaThuCung(cboPet.getValue() == null ? null : cboPet.getValue().id());
        invoiceToSave.setMaLichHen(appointmentId);
        invoiceToSave.setNgayLap(dtpInvoiceDate.getValue());
        invoiceToSave.setMaKhuyenMai(cboPromotion.getValue() == null ? null : cboPromotion.getValue().id());
        invoiceToSave.setGiamGia(Math.min(parseAmount(txtManualDiscount.getText()),
                invoiceItems.stream().mapToDouble(InvoiceItem::thanhTien).sum()));
        invoiceToSave.setGiamTheoKhuyenMai(invoice.getGiamTheoKhuyenMai());
        invoiceToSave.setThue(invoice.getThue());
        invoiceToSave.setGhiChu(getNote());
        invoiceToSave.setItems(new ArrayList<>(invoiceItems));
        return invoiceToSave;
    }

    private String getNote() { return invoice.getGhiChu(); }

    private static HBox valueRow(Node label, Node value) {
        HBox pnlRow = new HBox(8, label, new Region(), value);
        pnlRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(pnlRow.getChildren().get(1), Priority.ALWAYS);
        return pnlRow;
    }

    private static VBox createCard(String styleClass, Node... children) {
        VBox pnlCard = new VBox(12, children);
        pnlCard.getStyleClass().add(styleClass);
        return pnlCard;
    }

    private static Label fieldLabel(String text) {
        Label lblField = new Label(text);
        lblField.getStyleClass().add("invoice-field-label");
        return lblField;
    }

    private static String petSize(double weight) {
        return weight < 5 ? "Nhỏ" : weight <= 10 ? "Vừa" : "Lớn";
    }

    private static double parseAmount(String text) {
        if (text == null || text.isBlank()) return 0;
        try {
            return Double.parseDouble(text.replace(",", "").trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private static String money(double amount) {
        return String.format("%,.0f đ", amount);
    }

    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message == null ? "Có lỗi không xác định." : message);
        alert.showAndWait();
    }
}