package com.petshop.view.invoice;

import com.petshop.controller.CareServiceController;
import com.petshop.controller.InvoiceController;
import com.petshop.controller.ProductController;
import com.petshop.model.*;
import com.petshop.view.customer.CustomerPickerDialog;
import com.petshop.view.product.ProductPickerDialog;
import com.petshop.view.promotion.PromotionPickerDialog;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

/**
 * Giao diện Quản lý Hóa đơn (InvoiceView).
 * Hiện thực đầy đủ các phân hệ chức năng theo tài liệu OOAD và PRD:
 * - INV_CREATE: Lập hóa đơn bán hàng & thanh toán (Hạn: 22/10/2026).
 * - INV_SEARCH: Tìm kiếm & tra cứu hóa đơn bán hàng đa tiêu chí (Hạn: 21/10/2026).
 * - INV_CANCEL / INV_UPDATE: Hủy hóa đơn bán hàng (Xóa mềm & hoàn kho, Hạn: 23/10/2026).
 * - INV_REVENUE: Báo cáo & thống kê doanh thu hóa đơn.
 *
 * Tuân thủ Definition of Done:
 * - Đặt tên điều khiển đúng tiền tố: btn, txt, lbl, tbl, dtp, cbo.
 * - Tên lớp là danh từ, tên phương thức là động từ; có comment đầy đủ.
 * - Một màn hình chỉ có một chức năng duy nhất, bỏ hoàn toàn thanh tab ở trên đầu.
 * - Ô số chỉ cho nhập số; ngày tháng dùng DatePicker.
 * - Thao tác "hủy" hóa đơn là xóa mềm (chuyển sang 'Đã hủy'), tự động hoàn kho sản phẩm.
 * - Nút bấm hiển thị tên đầy đủ không bị co rút "..." (setMinWidth(Region.USE_PREF_SIZE)).
 */
public class InvoiceView extends VBox {

    private final String currentActionId;
    private final User currentUser;
    private final InvoiceController invoiceController = new InvoiceController();
    private final ProductController productController = new ProductController();
    private final CareServiceController careServiceController = new CareServiceController();

    private final ObservableList<Invoice> masterInvoiceList = FXCollections.observableArrayList();
    private final FilteredList<Invoice> filteredInvoiceList = new FilteredList<>(masterInvoiceList, inv -> true);
    private final ObservableList<InvoiceItem> currentCartItems = FXCollections.observableArrayList();

    private final SimpleBooleanProperty isBusy = new SimpleBooleanProperty(false);
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0 đ");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Các điều khiển chung
    private Label lblTitle;
    private Label lblStatusCount;
    private TableView<Invoice> tblInvoices;

    // Các điều khiển dành cho màn LẬP HÓA ĐƠN (INV_CREATE)
    private TextField txtInvoiceId;
    private TextField txtCashier;
    private DatePicker dtpNgayLap;
    private Customer selectedCustomer;
    private TextField txtCustomerName;
    private TextField txtCustomerPhone;
    private Button btnPickCustomer;
    private Button btnWalkInCustomer;

    private Promotion selectedPromotion;
    private TextField txtPromotionInfo;
    private Button btnPickPromotion;
    private Button btnClearPromotion;

    private TextField txtBarcode;
    private Button btnPickProduct;
    private Button btnPickService;
    private Button btnDeleteItem;
    private Button btnClearItems;
    private TableView<InvoiceItem> tblCartItems;

    private Label lblSubTotal;
    private Label lblDiscountAmount;
    private Label lblGrandTotal;
    private ComboBox<String> cboPaymentMethod;
    private TextField txtCustomerGivenMoney;
    private Label lblChangeMoney;
    private Button btnPayAndPrint;
    private Button btnSaveDraft;
    private Button btnReset;

    // Các điều khiển dành cho TÌM KIẾM, HỦY & BÁO CÁO
    private TextField txtSearch;
    private ComboBox<String> cboStatusFilter;
    private DatePicker dtpFromDate;
    private DatePicker dtpToDate;
    private Button btnSearch;
    private Button btnRefresh;
    private Button btnViewDetail;
    private Button btnCancelInvoice;

    public InvoiceView(String actionId) {
        this(actionId, null);
    }

    public InvoiceView(String actionId, User user) {
        this.currentActionId = actionId != null ? actionId : "INV_CREATE";
        this.currentUser = user;

        getStyleClass().addAll("customer-page", "invoice-page");
        setPadding(new Insets(24, 28, 24, 28));
        setSpacing(18);
        VBox.setVgrow(this, Priority.ALWAYS);

        // Khởi tạo đúng 1 màn hình duy nhất theo mã chức năng (Bỏ tab trên đầu)
        if ("INV_SEARCH".equalsIgnoreCase(currentActionId)) {
            initializeSearchScreen();
        } else if ("INV_CANCEL".equalsIgnoreCase(currentActionId) || "INV_UPDATE".equalsIgnoreCase(currentActionId)) {
            initializeCancelScreen();
        } else if ("INV_REVENUE".equalsIgnoreCase(currentActionId)) {
            initializeRevenueScreen();
        } else {
            initializeCreateScreen();
        }

        // Tải dữ liệu từ database
        taiDuLieuTuDatabase();
    }

    // =========================================================================
    // 1. MÀN HÌNH LẬP HÓA ĐƠN BÁN HÀNG (INV_CREATE)
    // =========================================================================
    private void initializeCreateScreen() {
        lblTitle = new Label("Lập hóa đơn bán hàng & thanh toán");
        lblTitle.getStyleClass().add("customer-title");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        btnReset = new Button("Làm mới đơn");
        btnReset.getStyleClass().add("customer-secondary");
        btnReset.setMinWidth(Region.USE_PREF_SIZE);
        btnReset.setOnAction(e -> lamMoiDonHang());

        HBox pnlHeader = new HBox(16, lblTitle, headerSpacer, btnReset);
        pnlHeader.setAlignment(Pos.CENTER_LEFT);

        // Chia bố cục 2 cột: Cột trái (Giỏ hàng SP & DV), Cột phải (Thanh toán & KH)
        HBox pnlMainLayout = new HBox(18);
        VBox.setVgrow(pnlMainLayout, Priority.ALWAYS);

        VBox pnlLeftCol = buildCartPanel();
        HBox.setHgrow(pnlLeftCol, Priority.ALWAYS);

        VBox pnlRightCol = buildCheckoutPanel();
        pnlRightCol.setPrefWidth(380);
        pnlRightCol.setMinWidth(360);

        pnlMainLayout.getChildren().addAll(pnlLeftCol, pnlRightCol);

        getChildren().addAll(pnlHeader, pnlMainLayout);
    }

    private VBox buildCartPanel() {
        VBox pnlCart = new VBox(12);
        pnlCart.setPadding(new Insets(16));
        pnlCart.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e5e7eb; -fx-border-radius: 12px; -fx-background-radius: 12px;");
        VBox.setVgrow(pnlCart, Priority.ALWAYS);

        // Thanh công cụ thêm hàng hóa & quét mã vạch
        HBox pnlAddBar = new HBox(10);
        pnlAddBar.setAlignment(Pos.CENTER_LEFT);

        btnPickProduct = new Button("＋ Chọn sản phẩm");
        btnPickProduct.getStyleClass().add("customer-add");
        btnPickProduct.setMinWidth(Region.USE_PREF_SIZE);
        btnPickProduct.setOnAction(e -> xuLyChonSanPham());

        btnPickService = new Button("＋ Chọn dịch vụ");
        btnPickService.getStyleClass().add("customer-secondary");
        btnPickService.setMinWidth(Region.USE_PREF_SIZE);
        btnPickService.setOnAction(e -> xuLyChonDichVu());

        Label lblBarcodeIcon = new Label("Quét mã / Mã SP:");
        lblBarcodeIcon.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #4b5563;");

        txtBarcode = new TextField();
        txtBarcode.setPromptText("Nhập mã SP / Quét barcode rồi Enter...");
        txtBarcode.setPrefWidth(220);
        txtBarcode.setPrefHeight(34);
        txtBarcode.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #d1d5db; -fx-border-radius: 6px; -fx-font-size: 12.5px;");
        txtBarcode.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                xuLyQuetMaVach();
            }
        });

        Region barSpacer = new Region();
        HBox.setHgrow(barSpacer, Priority.ALWAYS);

        btnDeleteItem = new Button("Xóa dòng");
        btnDeleteItem.getStyleClass().add("customer-danger");
        btnDeleteItem.setMinWidth(Region.USE_PREF_SIZE);
        btnDeleteItem.setOnAction(e -> xuLyXoaDong());

        btnClearItems = new Button("Xóa tất cả");
        btnClearItems.getStyleClass().add("customer-secondary");
        btnClearItems.setMinWidth(Region.USE_PREF_SIZE);
        btnClearItems.setOnAction(e -> xuLyXoaTatCa());

        pnlAddBar.getChildren().addAll(btnPickProduct, btnPickService, new Separator(javafx.geometry.Orientation.VERTICAL),
                lblBarcodeIcon, txtBarcode, barSpacer, btnDeleteItem, btnClearItems);

        // Bảng danh sách mặt hàng đã chọn
        tblCartItems = new TableView<>();
        tblCartItems.setItems(currentCartItems);
        tblCartItems.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblCartItems.setPlaceholder(new Label("Chưa có mặt hàng nào. Vui lòng bấm 'Chọn sản phẩm' hoặc quét mã vạch."));
        VBox.setVgrow(tblCartItems, Priority.ALWAYS);

        TableColumn<InvoiceItem, String> colIndex = new TableColumn<>("STT");
        colIndex.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.valueOf(currentCartItems.indexOf(c.getValue()) + 1)));
        colIndex.setPrefWidth(45);
        colIndex.setStyle("-fx-alignment: CENTER;");

        TableColumn<InvoiceItem, String> colCode = new TableColumn<>("Mã SP / DV");
        colCode.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().isService() ? c.getValue().getMaDichVu() : c.getValue().getMaSanPham()));
        colCode.setPrefWidth(125);
        colCode.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-weight: bold;");

        TableColumn<InvoiceItem, String> colName = new TableColumn<>("Tên mặt hàng / Dịch vụ");
        colName.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTenHang()));
        colName.setPrefWidth(220);
        colName.setStyle("-fx-font-weight: 600;");

        TableColumn<InvoiceItem, String> colType = new TableColumn<>("Phân loại");
        colType.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().isService() ? "Dịch vụ" : "Sản phẩm"));
        colType.setPrefWidth(85);
        colType.setStyle("-fx-alignment: CENTER;");

        TableColumn<InvoiceItem, String> colPrice = new TableColumn<>("Đơn giá");
        colPrice.setCellValueFactory(c -> new ReadOnlyStringWrapper(currencyFormat.format(c.getValue().getDonGia())));
        colPrice.setPrefWidth(110);
        colPrice.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: 600; -fx-text-fill: #15803d;");

        // Cột số lượng cho phép điều chỉnh
        TableColumn<InvoiceItem, Integer> colQty = new TableColumn<>("Số lượng");
        colQty.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getSoLuong()));
        colQty.setPrefWidth(80);
        colQty.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        TableColumn<InvoiceItem, String> colTotal = new TableColumn<>("Thành tiền");
        colTotal.setCellValueFactory(c -> new ReadOnlyStringWrapper(currencyFormat.format(c.getValue().getThanhTien())));
        colTotal.setPrefWidth(120);
        colTotal.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: #1f2923;");

        tblCartItems.getColumns().addAll(List.of(colIndex, colCode, colName, colType, colPrice, colQty, colTotal));

        pnlCart.getChildren().addAll(pnlAddBar, tblCartItems);
        return pnlCart;
    }

    private VBox buildCheckoutPanel() {
        VBox pnlCheckout = new VBox(12);
        pnlCheckout.setPadding(new Insets(16));
        pnlCheckout.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e5e7eb; -fx-border-radius: 12px; -fx-background-radius: 12px;");

        Label lblSectionTitle = new Label("THÔNG TIN THANH TOÁN");
        lblSectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");

        // 1. Mã hóa đơn & Thu ngân
        GridPane pnlMeta = new GridPane();
        pnlMeta.setHgap(10);
        pnlMeta.setVgap(8);

        Label lblId = new Label("Mã hóa đơn:");
        lblId.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #4b5563;");
        txtInvoiceId = new TextField();
        txtInvoiceId.setEditable(false);
        txtInvoiceId.setText(invoiceController.generateNextInvoiceId());
        txtInvoiceId.setStyle("-fx-background-color: #f3f4f6; -fx-font-weight: bold; -fx-pref-height: 32px; -fx-border-color: #d1d5db; -fx-border-radius: 6px;");

        Label lblCashierTitle = new Label("Thu ngân:");
        lblCashierTitle.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #4b5563;");
        txtCashier = new TextField();
        txtCashier.setEditable(false);
        txtCashier.setText(currentUser != null ? currentUser.getFullName() : "Admin Quản trị");
        txtCashier.setStyle("-fx-background-color: #f3f4f6; -fx-pref-height: 32px; -fx-border-color: #d1d5db; -fx-border-radius: 6px;");

        Label lblDateTitle = new Label("Ngày lập (*):");
        lblDateTitle.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #4b5563;");
        dtpNgayLap = new DatePicker(LocalDate.now());
        dtpNgayLap.setMaxWidth(Double.MAX_VALUE);
        dtpNgayLap.setStyle("-fx-pref-height: 32px;");

        pnlMeta.add(lblId, 0, 0);
        pnlMeta.add(txtInvoiceId, 1, 0);
        pnlMeta.add(lblCashierTitle, 0, 1);
        pnlMeta.add(txtCashier, 1, 1);
        pnlMeta.add(lblDateTitle, 0, 2);
        pnlMeta.add(dtpNgayLap, 1, 2);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPrefWidth(90);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        pnlMeta.getColumnConstraints().addAll(c1, c2);

        // 2. Thông tin khách hàng
        VBox pnlCustomerBox = new VBox(6);
        pnlCustomerBox.setPadding(new Insets(10));
        pnlCustomerBox.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #e5e7eb; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        HBox rowCustHeader = new HBox(8);
        rowCustHeader.setAlignment(Pos.CENTER_LEFT);
        Label lblCustSection = new Label("Khách hàng:");
        lblCustSection.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        Region custSpacer = new Region();
        HBox.setHgrow(custSpacer, Priority.ALWAYS);

        btnPickCustomer = new Button("Chọn KH");
        btnPickCustomer.getStyleClass().add("customer-secondary");
        btnPickCustomer.setMinWidth(Region.USE_PREF_SIZE);
        btnPickCustomer.setOnAction(e -> xuLyChonKhachHang());

        btnWalkInCustomer = new Button("Vãng lai");
        btnWalkInCustomer.getStyleClass().add("customer-secondary");
        btnWalkInCustomer.setMinWidth(Region.USE_PREF_SIZE);
        btnWalkInCustomer.setOnAction(e -> datKhachVangLai());

        rowCustHeader.getChildren().addAll(lblCustSection, custSpacer, btnPickCustomer, btnWalkInCustomer);

        txtCustomerName = new TextField("Khách vãng lai");
        txtCustomerName.setEditable(false);
        txtCustomerName.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1d5db; -fx-border-radius: 6px; -fx-pref-height: 32px; -fx-font-weight: 600;");

        pnlCustomerBox.getChildren().addAll(rowCustHeader, txtCustomerName);

        // 3. Khuyến mãi áp dụng
        VBox pnlPromoBox = new VBox(6);
        pnlPromoBox.setPadding(new Insets(10));
        pnlPromoBox.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #e5e7eb; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        HBox rowPromoHeader = new HBox(8);
        rowPromoHeader.setAlignment(Pos.CENTER_LEFT);
        Label lblPromoSection = new Label("Khuyến mãi:");
        lblPromoSection.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        Region promoSpacer = new Region();
        HBox.setHgrow(promoSpacer, Priority.ALWAYS);

        btnPickPromotion = new Button("Chọn KM");
        btnPickPromotion.getStyleClass().add("customer-secondary");
        btnPickPromotion.setMinWidth(Region.USE_PREF_SIZE);
        btnPickPromotion.setOnAction(e -> xuLyChonKhuyenMai());

        btnClearPromotion = new Button("Hủy KM");
        btnClearPromotion.getStyleClass().add("customer-secondary");
        btnClearPromotion.setMinWidth(Region.USE_PREF_SIZE);
        btnClearPromotion.setOnAction(e -> huyKhuyenMai());

        rowPromoHeader.getChildren().addAll(lblPromoSection, promoSpacer, btnPickPromotion, btnClearPromotion);

        txtPromotionInfo = new TextField("Không áp dụng");
        txtPromotionInfo.setEditable(false);
        txtPromotionInfo.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1d5db; -fx-border-radius: 6px; -fx-pref-height: 32px;");

        pnlPromoBox.getChildren().addAll(rowPromoHeader, txtPromotionInfo);

        // 4. Tổng kết số tiền & Thanh toán
        VBox pnlCalculation = new VBox(8);
        pnlCalculation.setPadding(new Insets(12));
        pnlCalculation.setStyle("-fx-background-color: #f0fdf4; -fx-border-color: #bbf7d0; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        lblSubTotal = new Label("0 đ");
        HBox rowSubTotal = createCalcRow("Tổng tiền hàng:", lblSubTotal, false);

        lblDiscountAmount = new Label("- 0 đ");
        lblDiscountAmount.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
        HBox rowDiscount = createCalcRow("Chiết khấu giảm giá:", lblDiscountAmount, false);

        lblGrandTotal = new Label("0 đ");
        lblGrandTotal.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #15803d;");
        HBox rowGrandTotal = createCalcRow("TỔNG THANH TOÁN:", lblGrandTotal, true);

        pnlCalculation.getChildren().addAll(rowSubTotal, rowDiscount, new Separator(), rowGrandTotal);

        // 5. Hình thức & Tiền khách đưa
        GridPane pnlPaymentInputs = new GridPane();
        pnlPaymentInputs.setHgap(10);
        pnlPaymentInputs.setVgap(8);

        Label lblMethod = new Label("Hình thức:");
        lblMethod.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #4b5563;");
        cboPaymentMethod = new ComboBox<>(FXCollections.observableArrayList("Tiền mặt", "Chuyển khoản", "POS"));
        cboPaymentMethod.setValue("Tiền mặt");
        cboPaymentMethod.setMaxWidth(Double.MAX_VALUE);
        cboPaymentMethod.setStyle("-fx-pref-height: 34px;");

        Label lblGiven = new Label("Tiền khách đưa:");
        lblGiven.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #4b5563;");
        txtCustomerGivenMoney = new TextField();
        txtCustomerGivenMoney.setPromptText("Nhập số tiền...");
        txtCustomerGivenMoney.setStyle("-fx-pref-height: 34px; -fx-font-size: 13px; -fx-font-weight: bold;");
        applyNumericFilter(txtCustomerGivenMoney); // Quy tắc DoD: chỉ cho nhập số
        txtCustomerGivenMoney.textProperty().addListener((obs, o, n) -> tinhTienThoiLai());

        Label lblChangeTitle = new Label("Tiền thối lại:");
        lblChangeTitle.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #4b5563;");
        lblChangeMoney = new Label("0 đ");
        lblChangeMoney.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2563eb;");

        pnlPaymentInputs.add(lblMethod, 0, 0);
        pnlPaymentInputs.add(cboPaymentMethod, 1, 0);
        pnlPaymentInputs.add(lblGiven, 0, 1);
        pnlPaymentInputs.add(txtCustomerGivenMoney, 1, 1);
        pnlPaymentInputs.add(lblChangeTitle, 0, 2);
        pnlPaymentInputs.add(lblChangeMoney, 1, 2);

        ColumnConstraints cc1 = new ColumnConstraints();
        cc1.setPrefWidth(95);
        ColumnConstraints cc2 = new ColumnConstraints();
        cc2.setHgrow(Priority.ALWAYS);
        pnlPaymentInputs.getColumnConstraints().addAll(cc1, cc2);

        // 6. Nút hành động chính
        VBox pnlActionButtons = new VBox(8);

        btnPayAndPrint = new Button("✓ Thanh toán & In hóa đơn");
        btnPayAndPrint.getStyleClass().add("customer-add");
        btnPayAndPrint.setMaxWidth(Double.MAX_VALUE);
        btnPayAndPrint.setPrefHeight(42);
        btnPayAndPrint.setMinWidth(Region.USE_PREF_SIZE);
        btnPayAndPrint.setOnAction(e -> xuLyThanhToanHoaDon(true));

        btnSaveDraft = new Button("Lưu tạm (Chưa thanh toán)");
        btnSaveDraft.getStyleClass().add("customer-secondary");
        btnSaveDraft.setMaxWidth(Double.MAX_VALUE);
        btnSaveDraft.setPrefHeight(36);
        btnSaveDraft.setMinWidth(Region.USE_PREF_SIZE);
        btnSaveDraft.setOnAction(e -> xuLyThanhToanHoaDon(false));

        pnlActionButtons.getChildren().addAll(btnPayAndPrint, btnSaveDraft);

        pnlCheckout.getChildren().addAll(lblSectionTitle, pnlMeta, pnlCustomerBox, pnlPromoBox,
                pnlCalculation, pnlPaymentInputs, pnlActionButtons);
        return pnlCheckout;
    }

    private HBox createCalcRow(String title, Label lblValue, boolean isGrand) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_RIGHT);

        Label lblT = new Label(title);
        lblT.setStyle(isGrand ? "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1f2923;" : "-fx-font-size: 12px; -fx-text-fill: #4b5563;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(lblT, spacer, lblValue);
        return row;
    }

    // =========================================================================
    // 2. MÀN HÌNH TRA CỨU HÓA ĐƠN (INV_SEARCH)
    // =========================================================================
    private void initializeSearchScreen() {
        lblTitle = new Label("Tra cứu & Tìm kiếm hóa đơn bán hàng");
        lblTitle.getStyleClass().add("customer-title");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        btnViewDetail = new Button("Xem chi tiết & In");
        btnViewDetail.getStyleClass().add("customer-add");
        btnViewDetail.setMinWidth(Region.USE_PREF_SIZE);
        btnViewDetail.setOnAction(e -> xuLyXemChiTiet());

        btnCancelInvoice = new Button("Hủy hóa đơn");
        btnCancelInvoice.getStyleClass().add("customer-danger");
        btnCancelInvoice.setMinWidth(Region.USE_PREF_SIZE);
        btnCancelInvoice.setOnAction(e -> xuLyHuyHoaDon());

        btnRefresh = new Button("Làm mới danh sách");
        btnRefresh.getStyleClass().add("customer-secondary");
        btnRefresh.setMinWidth(Region.USE_PREF_SIZE);
        btnRefresh.setOnAction(e -> taiDuLieuTuDatabase());

        HBox pnlHeaderActions = new HBox(10, btnViewDetail, btnCancelInvoice, btnRefresh);
        pnlHeaderActions.setAlignment(Pos.CENTER_RIGHT);

        HBox pnlHeader = new HBox(16, lblTitle, headerSpacer, pnlHeaderActions);
        pnlHeader.setAlignment(Pos.CENTER_LEFT);

        // Thanh công cụ tìm kiếm hóa đơn
        HBox pnlToolbar = buildSearchToolbar();

        // Bảng dữ liệu hóa đơn
        tblInvoices = buildInvoiceTable();
        VBox.setVgrow(tblInvoices, Priority.ALWAYS);

        // Thanh trạng thái chân trang
        HBox pnlFooter = buildFooterBar();

        getChildren().addAll(pnlHeader, pnlToolbar, tblInvoices, pnlFooter);
    }

    // =========================================================================
    // 3. MÀN HÌNH HỦY HÓA ĐƠN (INV_CANCEL / INV_UPDATE)
    // =========================================================================
    private void initializeCancelScreen() {
        lblTitle = new Label("Quản lý Hủy hóa đơn bán hàng (Xóa mềm)");
        lblTitle.getStyleClass().add("customer-title");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        btnCancelInvoice = new Button("Xác nhận hủy hóa đơn");
        btnCancelInvoice.getStyleClass().add("customer-danger");
        btnCancelInvoice.setMinWidth(Region.USE_PREF_SIZE);
        btnCancelInvoice.setOnAction(e -> xuLyHuyHoaDon());

        btnViewDetail = new Button("Xem chi tiết");
        btnViewDetail.getStyleClass().add("customer-secondary");
        btnViewDetail.setMinWidth(Region.USE_PREF_SIZE);
        btnViewDetail.setOnAction(e -> xuLyXemChiTiet());

        btnRefresh = new Button("Làm mới");
        btnRefresh.getStyleClass().add("customer-secondary");
        btnRefresh.setMinWidth(Region.USE_PREF_SIZE);
        btnRefresh.setOnAction(e -> taiDuLieuTuDatabase());

        HBox pnlHeaderActions = new HBox(10, btnCancelInvoice, btnViewDetail, btnRefresh);
        pnlHeaderActions.setAlignment(Pos.CENTER_RIGHT);

        HBox pnlHeader = new HBox(16, lblTitle, headerSpacer, pnlHeaderActions);
        pnlHeader.setAlignment(Pos.CENTER_LEFT);

        // Thanh công cụ tìm kiếm
        HBox pnlToolbar = buildSearchToolbar();

        // Bảng hóa đơn
        tblInvoices = buildInvoiceTable();
        VBox.setVgrow(tblInvoices, Priority.ALWAYS);

        // Chân trang
        HBox pnlFooter = buildFooterBar();

        getChildren().addAll(pnlHeader, pnlToolbar, tblInvoices, pnlFooter);
    }

    // =========================================================================
    // 4. MÀN HÌNH BÁO CÁO DOANH THU (INV_REVENUE)
    // =========================================================================
    private void initializeRevenueScreen() {
        lblTitle = new Label("Báo cáo & Thống kê doanh thu hóa đơn bán hàng");
        lblTitle.getStyleClass().add("customer-title");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        btnRefresh = new Button("Cập nhật số liệu");
        btnRefresh.getStyleClass().add("customer-secondary");
        btnRefresh.setMinWidth(Region.USE_PREF_SIZE);
        btnRefresh.setOnAction(e -> taiDuLieuTuDatabase());

        HBox pnlHeader = new HBox(16, lblTitle, headerSpacer, btnRefresh);
        pnlHeader.setAlignment(Pos.CENTER_LEFT);

        // Thẻ KPI tổng kết doanh thu
        HBox pnlStats = buildRevenueKpiCards();

        // Thanh công cụ lọc
        HBox pnlToolbar = buildSearchToolbar();

        // Bảng hóa đơn
        tblInvoices = buildInvoiceTable();
        VBox.setVgrow(tblInvoices, Priority.ALWAYS);

        HBox pnlFooter = buildFooterBar();

        getChildren().addAll(pnlHeader, pnlStats, pnlToolbar, tblInvoices, pnlFooter);
    }

    // =========================================================================
    // CÁC THÀNH PHẦN CON: TOOLBAR, TABLEVIEW, KPI CARDS
    // =========================================================================
    private HBox buildSearchToolbar() {
        HBox pnlToolbar = new HBox(10);
        pnlToolbar.setAlignment(Pos.CENTER_LEFT);
        pnlToolbar.setPadding(new Insets(12, 16, 12, 16));
        pnlToolbar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e5e7eb; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        Label lblSearchIcon = new Label("🔍");
        lblSearchIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: #9ca3af;");

        txtSearch = new TextField();
        txtSearch.setPromptText("Tìm theo mã hóa đơn, tên/SĐT khách hàng, nhân viên...");
        txtSearch.setPrefWidth(260);
        txtSearch.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-font-size: 13px;");
        HBox.setHgrow(txtSearch, Priority.ALWAYS);

        PauseTransition debounce = new PauseTransition(Duration.millis(250));
        txtSearch.textProperty().addListener((obs, o, n) -> {
            debounce.setOnFinished(e -> apDungBoLocHoaDon());
            debounce.playFromStart();
        });

        cboStatusFilter = new ComboBox<>(FXCollections.observableArrayList(
                "Tất cả trạng thái",
                "Đã thanh toán",
                "Chưa thanh toán",
                "Đã hủy"
        ));
        cboStatusFilter.setValue("Tất cả trạng thái");
        cboStatusFilter.setPrefWidth(150);
        cboStatusFilter.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #d1d5db; -fx-border-radius: 6px;");
        cboStatusFilter.valueProperty().addListener((obs, o, n) -> apDungBoLocHoaDon());

        Label lblFrom = new Label("Từ ngày:");
        lblFrom.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #4b5563; -fx-font-weight: 600;");
        dtpFromDate = new DatePicker();
        dtpFromDate.setPrefWidth(125);
        dtpFromDate.valueProperty().addListener((obs, o, n) -> apDungBoLocHoaDon());

        Label lblTo = new Label("Đến:");
        lblTo.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #4b5563; -fx-font-weight: 600;");
        dtpToDate = new DatePicker();
        dtpToDate.setPrefWidth(125);
        dtpToDate.valueProperty().addListener((obs, o, n) -> apDungBoLocHoaDon());

        btnSearch = new Button("Tìm kiếm");
        btnSearch.getStyleClass().add("customer-secondary");
        btnSearch.setMinWidth(Region.USE_PREF_SIZE);
        btnSearch.setOnAction(e -> apDungBoLocHoaDon());

        pnlToolbar.getChildren().addAll(lblSearchIcon, txtSearch, new Separator(javafx.geometry.Orientation.VERTICAL),
                cboStatusFilter, lblFrom, dtpFromDate, lblTo, dtpToDate, btnSearch);
        return pnlToolbar;
    }

    private TableView<Invoice> buildInvoiceTable() {
        TableView<Invoice> tbl = new TableView<>();
        tbl.setItems(filteredInvoiceList);
        tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tbl.setPlaceholder(new Label("Không tìm thấy hóa đơn phù hợp"));

        TableColumn<Invoice, String> colId = new TableColumn<>("Mã hóa đơn");
        colId.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getMaHoaDon()));
        colId.setPrefWidth(140);
        colId.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-weight: bold;");

        TableColumn<Invoice, String> colDate = new TableColumn<>("Ngày lập");
        colDate.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getNgayLap() != null ? c.getValue().getNgayLap().format(dateFormatter) : "-"
        ));
        colDate.setPrefWidth(100);
        colDate.setStyle("-fx-alignment: CENTER;");

        TableColumn<Invoice, String> colCustomer = new TableColumn<>("Khách hàng");
        colCustomer.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTenKhachHang()));
        colCustomer.setPrefWidth(160);
        colCustomer.setStyle("-fx-font-weight: 600;");

        TableColumn<Invoice, String> colPhone = new TableColumn<>("Số điện thoại");
        colPhone.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getSoDienThoaiKhachHang() != null ? c.getValue().getSoDienThoaiKhachHang() : "-"
        ));
        colPhone.setPrefWidth(120);
        colPhone.setStyle("-fx-alignment: CENTER;");

        TableColumn<Invoice, String> colCashier = new TableColumn<>("Thu ngân");
        colCashier.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTenNhanVien()));
        colCashier.setPrefWidth(140);

        TableColumn<Invoice, String> colPayment = new TableColumn<>("Thanh toán");
        colPayment.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getHinhThucThanhToan() != null ? c.getValue().getHinhThucThanhToan() : "-"
        ));
        colPayment.setPrefWidth(110);
        colPayment.setStyle("-fx-alignment: CENTER;");

        TableColumn<Invoice, String> colTotal = new TableColumn<>("Tổng tiền (VNĐ)");
        colTotal.setCellValueFactory(c -> new ReadOnlyStringWrapper(currencyFormat.format(c.getValue().getTongTien())));
        colTotal.setPrefWidth(130);
        colTotal.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: #15803d;");

        TableColumn<Invoice, Invoice> colStatus = new TableColumn<>("Trạng thái");
        colStatus.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        colStatus.setPrefWidth(125);
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Invoice item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label lblBadge = new Label(item.getStatus().getDisplayName());
                    String style = switch (item.getStatus()) {
                        case PAID -> "-fx-background-color: #dcfce7; -fx-text-fill: #15803d;";
                        case CANCELLED -> "-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c;";
                        default -> "-fx-background-color: #fef3c7; -fx-text-fill: #b45309;";
                    };
                    lblBadge.setStyle(style + " -fx-padding: 3px 10px; -fx-background-radius: 10px; -fx-font-weight: bold; -fx-font-size: 11px;");
                    setGraphic(lblBadge);
                    setAlignment(Pos.CENTER);
                    setText(null);
                }
            }
        });

        tbl.getColumns().addAll(List.of(colId, colDate, colCustomer, colPhone, colCashier, colPayment, colTotal, colStatus));

        tbl.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                xuLyXemChiTiet();
            }
        });

        return tbl;
    }

    private HBox buildFooterBar() {
        HBox pnlFooter = new HBox();
        pnlFooter.setAlignment(Pos.CENTER_LEFT);
        pnlFooter.setPadding(new Insets(6, 4, 6, 4));

        lblStatusCount = new Label("Đang tải dữ liệu...");
        lblStatusCount.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        pnlFooter.getChildren().add(lblStatusCount);
        return pnlFooter;
    }

    private HBox buildRevenueKpiCards() {
        HBox pnlStats = new HBox(16);
        pnlStats.setAlignment(Pos.CENTER_LEFT);

        VBox cardTotalRev = createKpiCard("Tổng doanh thu thực tế", "0 đ", "#f0fdf4", "#15803d");
        VBox cardPaidCount = createKpiCard("Hóa đơn đã thanh toán", "0", "#eff6ff", "#1d4ed8");
        VBox cardCancelCount = createKpiCard("Hóa đơn đã hủy", "0", "#fee2e2", "#b91c1c");

        HBox.setHgrow(cardTotalRev, Priority.ALWAYS);
        HBox.setHgrow(cardPaidCount, Priority.ALWAYS);
        HBox.setHgrow(cardCancelCount, Priority.ALWAYS);

        pnlStats.getChildren().addAll(cardTotalRev, cardPaidCount, cardCancelCount);
        return pnlStats;
    }

    private VBox createKpiCard(String title, String initialValue, String bg, String text) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(16));
        card.setStyle(String.format("-fx-background-color: %s; -fx-border-radius: 8px; -fx-background-radius: 8px;", bg));

        Label lblCardTitle = new Label(title);
        lblCardTitle.setStyle(String.format("-fx-font-size: 12.5px; -fx-font-weight: 600; -fx-text-fill: %s;", text));

        Label lblCardVal = new Label(initialValue);
        lblCardVal.setId("kpi_" + title);
        lblCardVal.setStyle(String.format("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: %s;", text));

        card.getChildren().addAll(lblCardTitle, lblCardVal);
        return card;
    }

    // =========================================================================
    // XỬ LÝ NGHIỆP VỤ (BUSINESS ACTIONS)
    // =========================================================================

    private void taiDuLieuTuDatabase() {
        if (isBusy.get()) return;
        isBusy.set(true);
        if (lblStatusCount != null) {
            lblStatusCount.setText("Đang tải dữ liệu hóa đơn...");
        }

        CompletableFuture.runAsync(() -> {
            try {
                List<Invoice> list = invoiceController.getAllInvoices();
                Platform.runLater(() -> {
                    masterInvoiceList.setAll(list);
                    capNhatTrangThaiVaKpi();
                    isBusy.set(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    isBusy.set(false);
                    if (lblStatusCount != null) {
                        lblStatusCount.setText("Lỗi nạp dữ liệu: " + ex.getMessage());
                    }
                });
            }
        });
    }

    private void apDungBoLocHoaDon() {
        String keyword = txtSearch != null && txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";
        String statusFilter = cboStatusFilter != null && cboStatusFilter.getValue() != null ? cboStatusFilter.getValue() : "Tất cả trạng thái";
        LocalDate fromDate = dtpFromDate != null ? dtpFromDate.getValue() : null;
        LocalDate toDate = dtpToDate != null ? dtpToDate.getValue() : null;

        filteredInvoiceList.setPredicate(inv -> {
            boolean matchKey = true;
            if (!keyword.isEmpty()) {
                matchKey = (inv.getMaHoaDon() != null && inv.getMaHoaDon().toLowerCase().contains(keyword))
                        || (inv.getTenKhachHang() != null && inv.getTenKhachHang().toLowerCase().contains(keyword))
                        || (inv.getSoDienThoaiKhachHang() != null && inv.getSoDienThoaiKhachHang().contains(keyword))
                        || (inv.getTenNhanVien() != null && inv.getTenNhanVien().toLowerCase().contains(keyword));
            }

            boolean matchStatus = true;
            if (!"Tất cả trạng thái".equalsIgnoreCase(statusFilter) && !"Tất cả".equalsIgnoreCase(statusFilter)) {
                matchStatus = inv.getStatus().getDisplayName().equalsIgnoreCase(statusFilter);
            }

            boolean matchDate = true;
            if (fromDate != null && inv.getNgayLap() != null && inv.getNgayLap().isBefore(fromDate)) {
                matchDate = false;
            }
            if (toDate != null && inv.getNgayLap() != null && inv.getNgayLap().isAfter(toDate)) {
                matchDate = false;
            }

            return matchKey && matchStatus && matchDate;
        });

        capNhatTrangThaiVaKpi();
    }

    private void capNhatTrangThaiVaKpi() {
        if (lblStatusCount != null) {
            lblStatusCount.setText(String.format("Hiển thị %d / %d hóa đơn bán hàng",
                    filteredInvoiceList.size(), masterInvoiceList.size()));
        }

        // Cập nhật thẻ KPI
        double totalRevenue = masterInvoiceList.stream()
                .filter(i -> i.getStatus() == Invoice.Status.PAID)
                .mapToDouble(Invoice::getTongTien).sum();
        long paidCount = masterInvoiceList.stream().filter(i -> i.getStatus() == Invoice.Status.PAID).count();
        long cancelCount = masterInvoiceList.stream().filter(i -> i.getStatus() == Invoice.Status.CANCELLED).count();

        lookupAndSetKpi("kpi_Tổng doanh thu thực tế", currencyFormat.format(totalRevenue));
        lookupAndSetKpi("kpi_Hóa đơn đã thanh toán", String.valueOf(paidCount));
        lookupAndSetKpi("kpi_Hóa đơn đã hủy", String.valueOf(cancelCount));
    }

    private void lookupAndSetKpi(String id, String text) {
        javafx.scene.Node node = lookup("#" + id);
        if (node instanceof Label label) {
            label.setText(text);
        }
    }

    // =========================================================================
    // XỬ LÝ LẬP HÓA ĐƠN (INV_CREATE ACTIONS)
    // =========================================================================

    private void xuLyChonSanPham() {
        Optional<Product> pOpt = ProductPickerDialog.showAndPick(getScene().getWindow());
        pOpt.ifPresent(p -> themSanPhamVaoGio(p, 1));
    }

    private void xuLyQuetMaVach() {
        if (txtBarcode == null) return;
        String code = txtBarcode.getText() != null ? txtBarcode.getText().trim() : "";
        if (code.isEmpty()) return;

        // Tìm theo mã vạch trước, nếu không thấy thì tìm theo mã sản phẩm
        Optional<Product> pOpt = productController.getProductByBarcode(code);
        if (pOpt.isEmpty()) {
            pOpt = productController.getProductById(code);
        }

        if (pOpt.isPresent()) {
            themSanPhamVaoGio(pOpt.get(), 1);
            txtBarcode.clear();
        } else {
            hienThiCanhBao("Không tìm thấy sản phẩm", "Không tìm thấy sản phẩm có mã vạch hoặc mã SP: " + code);
            txtBarcode.selectAll();
        }
    }

    private void themSanPhamVaoGio(Product p, int qty) {
        // Kiểm tra xem sản phẩm đã có trong giỏ chưa
        for (InvoiceItem item : currentCartItems) {
            if (!item.isService() && p.getMaSanPham().equals(item.getMaSanPham())) {
                int newQty = item.getSoLuong() + qty;
                if (newQty > p.getSoLuongTon()) {
                    hienThiCanhBao("Tồn kho không đủ", "Sản phẩm [" + p.getTenSanPham() +
                            "] chỉ còn tồn " + p.getSoLuongTon() + ", không thể thêm quá số lượng này.");
                    return;
                }
                item.setSoLuong(newQty);
                tblCartItems.refresh();
                tinhToanTongTien();
                return;
            }
        }

        if (p.getSoLuongTon() < qty) {
            hienThiCanhBao("Tồn kho không đủ", "Sản phẩm [" + p.getTenSanPham() +
                    "] chỉ còn tồn " + p.getSoLuongTon() + ", không đủ bán.");
            return;
        }

        InvoiceItem newItem = InvoiceItem.createProductItem(p.getMaSanPham(), p.getTenSanPham(), qty, p.getGiaBan());
        currentCartItems.add(newItem);
        tinhToanTongTien();
    }

    private void xuLyChonDichVu() {
        List<CareService> activeServices = careServiceController.getActiveCareServices();
        if (activeServices.isEmpty()) {
            hienThiCanhBao("Dịch vụ trống", "Hiện không có dịch vụ chăm sóc nào đang mở cung cấp.");
            return;
        }

        ChoiceDialog<CareService> dlg = new ChoiceDialog<>(activeServices.get(0), activeServices);
        dlg.setTitle("Chọn Dịch Vụ Chăm Sóc - Happy Tails");
        dlg.setHeaderText("Chọn dịch vụ chăm sóc thú cưng đưa vào hóa đơn");
        dlg.setContentText("Dịch vụ:");

        Optional<CareService> chosen = dlg.showAndWait();
        chosen.ifPresent(service -> {
            // Giá dịch vụ mặc định lấy theo giá bậc Nhỏ nếu có, hoặc giá lớn hơn 0 đầu tiên
            double price = service.getGiaNho() > 0 ? service.getGiaNho() :
                    (service.getGiaVua() > 0 ? service.getGiaVua() : service.getGiaLon());

            InvoiceItem srvItem = InvoiceItem.createServiceItem(service.getMaDichVu(), service.getTenDichVu(), price);
            currentCartItems.add(srvItem);
            tinhToanTongTien();
        });
    }

    private void xuLyXoaDong() {
        InvoiceItem selected = tblCartItems.getSelectionModel().getSelectedItem();
        if (selected != null) {
            currentCartItems.remove(selected);
            tinhToanTongTien();
        } else {
            hienThiCanhBao("Chưa chọn dòng", "Vui lòng chọn dòng mặt hàng muốn xóa khỏi giỏ.");
        }
    }

    private void xuLyXoaTatCa() {
        if (!currentCartItems.isEmpty()) {
            currentCartItems.clear();
            tinhToanTongTien();
        }
    }

    private void xuLyChonKhachHang() {
        Optional<Customer> cOpt = CustomerPickerDialog.showAndPick(getScene().getWindow());
        cOpt.ifPresent(c -> {
            this.selectedCustomer = c;
            txtCustomerName.setText(c.getHoTen() + (c.getSoDienThoai() != null && !c.getSoDienThoai().isEmpty() ? " (" + c.getSoDienThoai() + ")" : ""));
        });
    }

    private void datKhachVangLai() {
        this.selectedCustomer = null;
        txtCustomerName.setText("Khách vãng lai");
    }

    private void xuLyChonKhuyenMai() {
        Optional<Promotion> pOpt = PromotionPickerDialog.showAndPick(getScene().getWindow());
        pOpt.ifPresent(p -> {
            if (p.getMaKhuyenMai() == null) {
                huyKhuyenMai();
            } else {
                this.selectedPromotion = p;
                txtPromotionInfo.setText(p.getTenChuongTrinh() + " (-" + (int) p.getPhanTramGiam() + "%)");
                tinhToanTongTien();
            }
        });
    }

    private void huyKhuyenMai() {
        this.selectedPromotion = null;
        txtPromotionInfo.setText("Không áp dụng");
        tinhToanTongTien();
    }

    private void tinhToanTongTien() {
        double subTotal = currentCartItems.stream().mapToDouble(InvoiceItem::getThanhTien).sum();
        double discount = 0;

        if (selectedPromotion != null && selectedPromotion.getPhanTramGiam() > 0) {
            discount = subTotal * (selectedPromotion.getPhanTramGiam() / 100.0);
        }

        double grandTotal = Math.max(0, subTotal - discount);

        lblSubTotal.setText(currencyFormat.format(subTotal));
        lblDiscountAmount.setText("- " + currencyFormat.format(discount));
        lblGrandTotal.setText(currencyFormat.format(grandTotal));

        tinhTienThoiLai();
    }

    private void tinhTienThoiLai() {
        if (txtCustomerGivenMoney == null || lblChangeMoney == null) return;
        String text = txtCustomerGivenMoney.getText() != null ? txtCustomerGivenMoney.getText().trim() : "";
        if (text.isEmpty()) {
            lblChangeMoney.setText("0 đ");
            return;
        }

        try {
            double given = Double.parseDouble(text);
            double subTotal = currentCartItems.stream().mapToDouble(InvoiceItem::getThanhTien).sum();
            double discount = (selectedPromotion != null) ? subTotal * (selectedPromotion.getPhanTramGiam() / 100.0) : 0;
            double grandTotal = Math.max(0, subTotal - discount);

            double change = given - grandTotal;
            if (change >= 0) {
                lblChangeMoney.setText(currencyFormat.format(change));
                lblChangeMoney.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #15803d;");
            } else {
                lblChangeMoney.setText("Còn thiếu: " + currencyFormat.format(Math.abs(change)));
                lblChangeMoney.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dc2626;");
            }
        } catch (NumberFormatException ignored) {
            lblChangeMoney.setText("0 đ");
        }
    }

    private void xuLyThanhToanHoaDon(boolean isPaid) {
        if (currentCartItems.isEmpty()) {
            hienThiCanhBao("Chưa có mặt hàng", "Hóa đơn phải có ít nhất 1 mặt hàng để thanh toán.");
            return;
        }

        double subTotal = currentCartItems.stream().mapToDouble(InvoiceItem::getThanhTien).sum();
        double discount = (selectedPromotion != null) ? subTotal * (selectedPromotion.getPhanTramGiam() / 100.0) : 0;
        double grandTotal = Math.max(0, subTotal - discount);

        if (isPaid) {
            String givenText = txtCustomerGivenMoney.getText() != null ? txtCustomerGivenMoney.getText().trim() : "";
            if (!givenText.isEmpty()) {
                double given = Double.parseDouble(givenText);
                if (given < grandTotal) {
                    hienThiCanhBao("Tiền khách đưa chưa đủ", "Tiền khách đưa nhỏ hơn tổng tiền phải thanh toán.");
                    txtCustomerGivenMoney.requestFocus();
                    return;
                }
            }
        }

        try {
            Invoice inv = new Invoice();
            inv.setMaHoaDon(txtInvoiceId.getText().trim());
            inv.setMaNhanVien(currentUser != null ? currentUser.getMaNhanVien() : "NV-20261019-000001");
            inv.setTenNhanVien(txtCashier.getText().trim());
            inv.setMaKhachHang(selectedCustomer != null ? selectedCustomer.getMaKhachHang() : null);
            inv.setTenKhachHang(selectedCustomer != null ? selectedCustomer.getHoTen() : "Khách vãng lai");
            inv.setNgayLap(dtpNgayLap.getValue() != null ? dtpNgayLap.getValue() : LocalDate.now());
            inv.setHinhThucThanhToan(cboPaymentMethod.getValue());
            inv.setStatus(isPaid ? Invoice.Status.PAID : Invoice.Status.PENDING);
            inv.setMaKhuyenMai(selectedPromotion != null ? selectedPromotion.getMaKhuyenMai() : null);
            inv.setTenKhuyenMai(selectedPromotion != null ? selectedPromotion.getTenChuongTrinh() : null);
            inv.setPhanTramGiam(selectedPromotion != null ? selectedPromotion.getPhanTramGiam() : 0);
            inv.setTienGiam(discount);
            inv.setTongTien(grandTotal);
            inv.setItems(new ArrayList<>(currentCartItems));

            boolean success = invoiceController.createInvoice(inv);
            if (success) {
                hienThiThongBaoThanhCong("Thanh toán thành công",
                        (isPaid ? "Đã lập và hoàn tất thanh toán hóa đơn: " : "Đã lưu tạm hóa đơn: ") + inv.getMaHoaDon());

                // Mở hóa đơn xem trước và in
                if (isPaid) {
                    InvoiceDetailDialog.showDetails(getScene().getWindow(), inv);
                }

                lamMoiDonHang();
                taiDuLieuTuDatabase();
            }
        } catch (Exception ex) {
            hienThiThongBaoLoi("Lỗi lưu hóa đơn", ex.getMessage());
        }
    }

    private void lamMoiDonHang() {
        txtInvoiceId.setText(invoiceController.generateNextInvoiceId());
        currentCartItems.clear();
        selectedCustomer = null;
        txtCustomerName.setText("Khách vãng lai");
        selectedPromotion = null;
        txtPromotionInfo.setText("Không áp dụng");
        cboPaymentMethod.setValue("Tiền mặt");
        txtCustomerGivenMoney.clear();
        lblChangeMoney.setText("0 đ");
        dtpNgayLap.setValue(LocalDate.now());
        tinhToanTongTien();
    }

    // =========================================================================
    // XỬ LÝ TRA CỨU & HỦY HÓA ĐƠN (INV_SEARCH / INV_CANCEL ACTIONS)
    // =========================================================================

    private void xuLyXemChiTiet() {
        if (tblInvoices == null) return;
        Invoice selected = tblInvoices.getSelectionModel().getSelectedItem();
        if (selected == null) {
            hienThiCanhBao("Chưa chọn hóa đơn", "Vui lòng chọn một hóa đơn từ bảng để xem chi tiết.");
            return;
        }

        // Tải chi tiết các mặt hàng của hóa đơn
        Optional<Invoice> fullOpt = invoiceController.getInvoiceById(selected.getMaHoaDon());
        if (fullOpt.isPresent()) {
            InvoiceDetailDialog.showDetails(getScene().getWindow(), fullOpt.get());
        } else {
            hienThiCanhBao("Lỗi tra cứu", "Không tìm thấy chi tiết cho hóa đơn [" + selected.getMaHoaDon() + "].");
        }
    }

    /**
     * Hủy hóa đơn (Xóa mềm - trangThaiThanhToan = 'Đã hủy').
     * Khớp 100% tài liệu OOAD và tự động hoàn trả số lượng tồn kho sản phẩm.
     */
    private void xuLyHuyHoaDon() {
        if (tblInvoices == null) return;
        Invoice selected = tblInvoices.getSelectionModel().getSelectedItem();
        if (selected == null) {
            hienThiCanhBao("Chưa chọn hóa đơn", "Vui lòng chọn một hóa đơn từ bảng để thực hiện hủy.");
            return;
        }

        if (selected.getStatus() == Invoice.Status.CANCELLED) {
            hienThiCanhBao("Hóa đơn đã hủy", "Hóa đơn [" + selected.getMaHoaDon() + "] đã ở trạng thái Đã hủy trước đó.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận hủy hóa đơn - Happy Tails");
        confirm.setHeaderText("Xác nhận hủy hóa đơn bán hàng: " + selected.getMaHoaDon());
        confirm.setContentText("Hóa đơn sẽ chuyển sang trạng thái 'Đã hủy' (xóa mềm). Toàn bộ số lượng sản phẩm trong hóa đơn sẽ được tự động hoàn lại vào kho hàng. Bạn có chắc chắn muốn hủy không?");

        confirm.getButtonTypes().setAll(
                new ButtonType("Xác nhận hủy", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Bỏ qua", ButtonBar.ButtonData.CANCEL_CLOSE)
        );

        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isPresent() && choice.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
            try {
                boolean success = invoiceController.cancelInvoice(selected.getMaHoaDon());
                if (success) {
                    hienThiThongBaoThanhCong("Hủy hóa đơn thành công",
                            "Hóa đơn [" + selected.getMaHoaDon() + "] đã được hủy và tồn kho đã được hoàn lại thành công.");
                    taiDuLieuTuDatabase();
                }
            } catch (Exception ex) {
                hienThiThongBaoLoi("Lỗi hủy hóa đơn", ex.getMessage());
            }
        }
    }

    /**
     * Tiêu chuẩn Definition of Done: Ô số chỉ cho nhập số.
     */
    private void applyNumericFilter(TextField textField) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change;
            }
            return null;
        };
        textField.setTextFormatter(new TextFormatter<>(filter));
    }

    // =========================================================================
    // HỘP THOẠI THÔNG BÁO TIỆN ÍCH
    // =========================================================================

    private void hienThiThongBaoThanhCong(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo - Happy Tails");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void hienThiCanhBao(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo - Happy Tails");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void hienThiThongBaoLoi(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi hệ thống - Happy Tails");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

