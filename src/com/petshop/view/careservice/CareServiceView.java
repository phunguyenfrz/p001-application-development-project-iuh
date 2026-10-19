package com.petshop.view.careservice;

import com.petshop.controller.CareServiceController;
import com.petshop.model.CareService;
import com.petshop.model.ServicePricing;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Giao diện Quản lý Dịch vụ chăm sóc & Bảng giá (CareServiceView).
 * Thiết kế chuẩn kiến trúc IUH: Mỗi màn hình chỉ đảm nhận 1 chức năng duy nhất:
 * - SRV_CRUD / CAR_UPDATE: Cập nhật dịch vụ (Thêm, Sửa, Ngừng cung cấp/Xóa mềm, Kích hoạt lại).
 * - SRV_SEARCH / CAR_SEARCH: Tra cứu dịch vụ chăm sóc đa tiêu chí, xem chi tiết và giá 3 bậc hiện hành.
 * - SRV_PRICING / CAR_PRICING: Quản lý bảng giá dịch vụ theo 3 bậc cân nặng thú cưng (Nhỏ, Vừa, Lớn).
 * - CAR_REPORT: Báo cáo & Thống kê dịch vụ chăm sóc.
 *
 * Tuân thủ Definition of Done:
 * - Điều khiển đặt tên tiền tố: btn, txt, lbl, tbl, cbo.
 * - Tên lớp là danh từ, tên phương thức là động từ; đầy đủ Javadoc/comment.
 * - Tất cả nút có setMinWidth(Region.USE_PREF_SIZE) để hiển thị đầy đủ tên, không bị co rút "...".
 * - Bỏ thanh tab ở đầu trang, mỗi chức năng mở màn hình riêng biệt.
 * - Thao tác "xóa" là ngừng hoạt động (xóa mềm - trangThai = 0), khớp tài liệu OOAD.
 */
public class CareServiceView extends VBox {

    private final String currentActionId;
    private final CareServiceController careServiceController = new CareServiceController();
    private final ObservableList<CareService> masterServiceList = FXCollections.observableArrayList();
    private final FilteredList<CareService> filteredServiceList = new FilteredList<>(masterServiceList, s -> true);
    private final ObservableList<ServicePricing> masterPricingList = FXCollections.observableArrayList();
    private final FilteredList<ServicePricing> filteredPricingList = new FilteredList<>(masterPricingList, p -> true);

    private final SimpleBooleanProperty isBusy = new SimpleBooleanProperty(false);
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0 đ");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Các điều khiển tuân thủ tiền tố
    private TableView<CareService> tblServices;
    private TableView<ServicePricing> tblPrices;
    private TextField txtSearch;
    private ComboBox<String> cboStatusFilter;
    private ComboBox<String> cboSizeFilter;

    // Nút chức năng thanh công cụ
    private Button btnAdd;
    private Button btnEdit;
    private Button btnDeactivate;
    private Button btnActivate;
    private Button btnViewDetail;
    private Button btnSearch;
    private Button btnRefresh;
    private Button btnAddPrice;
    private Button btnEditPrice;

    // Nhãn thông tin
    private Label lblTitle;
    private Label lblStatusCount;

    public CareServiceView() {
        this("SRV_CRUD");
    }

    public CareServiceView(String actionId) {
        this.currentActionId = actionId != null ? actionId : "SRV_CRUD";

        getStyleClass().addAll("customer-page", "careservice-page");
        setPadding(new Insets(24, 28, 24, 28));
        setSpacing(18);
        VBox.setVgrow(this, Priority.ALWAYS);

        // Khởi tạo đúng 1 màn hình duy nhất theo mã chức năng (Không dùng thanh tab)
        if ("SRV_SEARCH".equalsIgnoreCase(currentActionId) || "CAR_SEARCH".equalsIgnoreCase(currentActionId)) {
            initializeSearchScreen();
        } else if ("SRV_PRICING".equalsIgnoreCase(currentActionId) || "CAR_PRICING".equalsIgnoreCase(currentActionId)) {
            initializePricingScreen();
        } else if ("CAR_REPORT".equalsIgnoreCase(currentActionId)) {
            initializeReportScreen();
        } else {
            initializeCrudScreen();
        }

        // Tải dữ liệu từ database
        taiDuLieuTuDatabase();
    }

    // =========================================================================
    // 1. MÀN HÌNH CẬP NHẬT DỊCH VỤ (SRV_CRUD / CAR_UPDATE)
    // =========================================================================
    private void initializeCrudScreen() {
        lblTitle = new Label("Cập nhật dịch vụ chăm sóc");
        lblTitle.getStyleClass().add("customer-title");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        btnAdd = new Button("＋ Thêm dịch vụ");
        btnAdd.getStyleClass().add("customer-add");
        btnAdd.setMinWidth(Region.USE_PREF_SIZE);
        btnAdd.setOnAction(e -> xuLyThemDichVu());

        btnEdit = new Button("Sửa dịch vụ");
        btnEdit.getStyleClass().add("customer-secondary");
        btnEdit.setMinWidth(Region.USE_PREF_SIZE);
        btnEdit.setOnAction(e -> xuLySuaDichVu());

        btnDeactivate = new Button("Ngừng cung cấp");
        btnDeactivate.getStyleClass().add("customer-danger");
        btnDeactivate.setMinWidth(Region.USE_PREF_SIZE);
        btnDeactivate.setOnAction(e -> xuLyNgungCungCap());

        btnActivate = new Button("Kích hoạt lại");
        btnActivate.getStyleClass().add("customer-secondary");
        btnActivate.setMinWidth(Region.USE_PREF_SIZE);
        btnActivate.setOnAction(e -> xuLyKichHoatLai());

        btnRefresh = new Button("Làm mới");
        btnRefresh.getStyleClass().add("customer-secondary");
        btnRefresh.setMinWidth(Region.USE_PREF_SIZE);
        btnRefresh.setOnAction(e -> taiDuLieuTuDatabase());

        HBox pnlHeaderActions = new HBox(10, btnAdd, btnEdit, btnDeactivate, btnActivate, btnRefresh);
        pnlHeaderActions.setAlignment(Pos.CENTER_RIGHT);

        HBox pnlHeader = new HBox(16, lblTitle, headerSpacer, pnlHeaderActions);
        pnlHeader.setAlignment(Pos.CENTER_LEFT);

        // Thanh công cụ tìm kiếm và lọc
        HBox pnlToolbar = buildServiceToolbar();

        // Bảng dữ liệu dịch vụ
        tblServices = buildServiceTable(true);
        VBox.setVgrow(tblServices, Priority.ALWAYS);

        // Thanh trạng thái dưới bảng
        HBox pnlFooter = buildFooterBar();

        getChildren().addAll(pnlHeader, pnlToolbar, tblServices, pnlFooter);
    }

    // =========================================================================
    // 2. MÀN HÌNH TRA CỨU DỊCH VỤ (SRV_SEARCH / CAR_SEARCH)
    // =========================================================================
    private void initializeSearchScreen() {
        lblTitle = new Label("Tra cứu & Tìm kiếm dịch vụ chăm sóc");
        lblTitle.getStyleClass().add("customer-title");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        btnViewDetail = new Button("Xem chi tiết & Giá");
        btnViewDetail.getStyleClass().add("customer-add");
        btnViewDetail.setMinWidth(Region.USE_PREF_SIZE);
        btnViewDetail.setOnAction(e -> xuLyXemChiTiet());

        btnRefresh = new Button("Làm mới danh sách");
        btnRefresh.getStyleClass().add("customer-secondary");
        btnRefresh.setMinWidth(Region.USE_PREF_SIZE);
        btnRefresh.setOnAction(e -> taiDuLieuTuDatabase());

        HBox pnlHeaderActions = new HBox(10, btnViewDetail, btnRefresh);
        pnlHeaderActions.setAlignment(Pos.CENTER_RIGHT);

        HBox pnlHeader = new HBox(16, lblTitle, headerSpacer, pnlHeaderActions);
        pnlHeader.setAlignment(Pos.CENTER_LEFT);

        // Thanh công cụ tìm kiếm
        HBox pnlToolbar = buildServiceToolbar();

        // Bảng dữ liệu dịch vụ (chế độ tra cứu)
        tblServices = buildServiceTable(false);
        VBox.setVgrow(tblServices, Priority.ALWAYS);

        // Thanh trạng thái
        HBox pnlFooter = buildFooterBar();

        getChildren().addAll(pnlHeader, pnlToolbar, tblServices, pnlFooter);
    }

    // =========================================================================
    // 3. MÀN HÌNH BẢNG GIÁ DỊCH VỤ (SRV_PRICING / CAR_PRICING)
    // =========================================================================
    private void initializePricingScreen() {
        lblTitle = new Label("Quản lý Bảng giá dịch vụ theo 3 bậc kích thước");
        lblTitle.getStyleClass().add("customer-title");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        btnAddPrice = new Button("＋ Thiết lập giá mới");
        btnAddPrice.getStyleClass().add("customer-add");
        btnAddPrice.setMinWidth(Region.USE_PREF_SIZE);
        btnAddPrice.setOnAction(e -> xuLyThemBangGia());

        btnEditPrice = new Button("Điều chỉnh giá");
        btnEditPrice.getStyleClass().add("customer-secondary");
        btnEditPrice.setMinWidth(Region.USE_PREF_SIZE);
        btnEditPrice.setOnAction(e -> xuLySuaBangGia());

        btnRefresh = new Button("Làm mới");
        btnRefresh.getStyleClass().add("customer-secondary");
        btnRefresh.setMinWidth(Region.USE_PREF_SIZE);
        btnRefresh.setOnAction(e -> taiDuLieuTuDatabase());

        HBox pnlHeaderActions = new HBox(10, btnAddPrice, btnEditPrice, btnRefresh);
        pnlHeaderActions.setAlignment(Pos.CENTER_RIGHT);

        HBox pnlHeader = new HBox(16, lblTitle, headerSpacer, pnlHeaderActions);
        pnlHeader.setAlignment(Pos.CENTER_LEFT);

        // Thanh công cụ tìm kiếm bảng giá
        HBox pnlToolbar = buildPricingToolbar();

        // Bảng dữ liệu bảng giá
        tblPrices = buildPricingTable();
        VBox.setVgrow(tblPrices, Priority.ALWAYS);

        // Thanh trạng thái
        HBox pnlFooter = buildFooterBar();

        getChildren().addAll(pnlHeader, pnlToolbar, tblPrices, pnlFooter);
    }

    // =========================================================================
    // 4. MÀN HÌNH BÁO CÁO DỊCH VỤ (CAR_REPORT)
    // =========================================================================
    private void initializeReportScreen() {
        lblTitle = new Label("Báo cáo & Thống kê dịch vụ chăm sóc thú cưng");
        lblTitle.getStyleClass().add("customer-title");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        btnRefresh = new Button("Cập nhật số liệu");
        btnRefresh.getStyleClass().add("customer-secondary");
        btnRefresh.setMinWidth(Region.USE_PREF_SIZE);
        btnRefresh.setOnAction(e -> taiDuLieuTuDatabase());

        HBox pnlHeader = new HBox(16, lblTitle, headerSpacer, btnRefresh);
        pnlHeader.setAlignment(Pos.CENTER_LEFT);

        // Thống kê tóm tắt
        HBox pnlStats = buildReportSummaryCards();

        // Bảng danh sách dịch vụ kèm giá 3 bậc
        tblServices = buildServiceTable(false);
        VBox.setVgrow(tblServices, Priority.ALWAYS);

        HBox pnlFooter = buildFooterBar();

        getChildren().addAll(pnlHeader, pnlStats, tblServices, pnlFooter);
    }

    // =========================================================================
    // CÁC THÀNH PHẦN GIAO DIỆN CON (TOOLBAR, TABLE, FOOTER)
    // =========================================================================

    private HBox buildServiceToolbar() {
        HBox pnlToolbar = new HBox(12);
        pnlToolbar.setAlignment(Pos.CENTER_LEFT);
        pnlToolbar.setPadding(new Insets(12, 16, 12, 16));
        pnlToolbar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e5e7eb; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        Label lblSearchIcon = new Label("🔍");
        lblSearchIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: #9ca3af;");

        txtSearch = new TextField();
        txtSearch.setPromptText("Tìm theo mã dịch vụ (DV-...), tên hoặc mô tả...");
        txtSearch.setPrefWidth(320);
        txtSearch.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-font-size: 13px;");
        HBox.setHgrow(txtSearch, Priority.ALWAYS);

        PauseTransition debounce = new PauseTransition(Duration.millis(250));
        txtSearch.textProperty().addListener((obs, o, n) -> {
            debounce.setOnFinished(e -> apDungBoLocDichVu());
            debounce.playFromStart();
        });

        cboStatusFilter = new ComboBox<>(FXCollections.observableArrayList(
                "Tất cả trạng thái",
                "Đang cung cấp",
                "Tạm ngừng"
        ));
        cboStatusFilter.setValue("Tất cả trạng thái");
        cboStatusFilter.setPrefWidth(160);
        cboStatusFilter.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #d1d5db; -fx-border-radius: 6px;");
        cboStatusFilter.valueProperty().addListener((obs, o, n) -> apDungBoLocDichVu());

        btnSearch = new Button("Tìm kiếm");
        btnSearch.getStyleClass().add("customer-secondary");
        btnSearch.setMinWidth(Region.USE_PREF_SIZE);
        btnSearch.setOnAction(e -> apDungBoLocDichVu());

        pnlToolbar.getChildren().addAll(lblSearchIcon, txtSearch, new Separator(javafx.geometry.Orientation.VERTICAL), cboStatusFilter, btnSearch);
        return pnlToolbar;
    }

    private HBox buildPricingToolbar() {
        HBox pnlToolbar = new HBox(12);
        pnlToolbar.setAlignment(Pos.CENTER_LEFT);
        pnlToolbar.setPadding(new Insets(12, 16, 12, 16));
        pnlToolbar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e5e7eb; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        Label lblSearchIcon = new Label("🔍");
        lblSearchIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: #9ca3af;");

        txtSearch = new TextField();
        txtSearch.setPromptText("Tìm theo mã bảng giá, mã hoặc tên dịch vụ...");
        txtSearch.setPrefWidth(320);
        txtSearch.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-font-size: 13px;");
        HBox.setHgrow(txtSearch, Priority.ALWAYS);

        PauseTransition debounce = new PauseTransition(Duration.millis(250));
        txtSearch.textProperty().addListener((obs, o, n) -> {
            debounce.setOnFinished(e -> apDungBoLocBangGia());
            debounce.playFromStart();
        });

        cboSizeFilter = new ComboBox<>(FXCollections.observableArrayList(
                "Tất cả kích thước",
                ServicePricing.BAC_NHO,
                ServicePricing.BAC_VUA,
                ServicePricing.BAC_LON
        ));
        cboSizeFilter.setValue("Tất cả kích thước");
        cboSizeFilter.setPrefWidth(160);
        cboSizeFilter.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #d1d5db; -fx-border-radius: 6px;");
        cboSizeFilter.valueProperty().addListener((obs, o, n) -> apDungBoLocBangGia());

        btnSearch = new Button("Tìm kiếm");
        btnSearch.getStyleClass().add("customer-secondary");
        btnSearch.setMinWidth(Region.USE_PREF_SIZE);
        btnSearch.setOnAction(e -> apDungBoLocBangGia());

        pnlToolbar.getChildren().addAll(lblSearchIcon, txtSearch, new Separator(javafx.geometry.Orientation.VERTICAL), cboSizeFilter, btnSearch);
        return pnlToolbar;
    }

    private TableView<CareService> buildServiceTable(boolean allowEditOnDoubleClick) {
        TableView<CareService> tbl = new TableView<>();
        tbl.setItems(filteredServiceList);
        tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tbl.setPlaceholder(new Label("Không tìm thấy dịch vụ chăm sóc phù hợp"));

        // Cột 1: Mã dịch vụ
        TableColumn<CareService, String> colId = new TableColumn<>("Mã dịch vụ");
        colId.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getMaDichVu()));
        colId.setPrefWidth(140);
        colId.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-weight: bold;");

        // Cột 2: Tên dịch vụ
        TableColumn<CareService, String> colName = new TableColumn<>("Tên dịch vụ chăm sóc");
        colName.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTenDichVu()));
        colName.setPrefWidth(220);
        colName.setStyle("-fx-font-weight: 600;");

        // Cột 3: Thời lượng (phút)
        TableColumn<CareService, String> colDuration = new TableColumn<>("Thời lượng");
        colDuration.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getThoiLuong() + " phút"));
        colDuration.setPrefWidth(100);
        colDuration.setStyle("-fx-alignment: CENTER;");

        // Cột 4: Giá Nhỏ (<5kg)
        TableColumn<CareService, String> colPriceSmall = new TableColumn<>("Giá Nhỏ (<5kg)");
        colPriceSmall.setCellValueFactory(c -> {
            double price = c.getValue().getGiaNho();
            return new ReadOnlyStringWrapper(price > 0 ? currencyFormat.format(price) : "-");
        });
        colPriceSmall.setPrefWidth(125);
        colPriceSmall.setStyle("-fx-alignment: CENTER-RIGHT; -fx-text-fill: #15803d; -fx-font-weight: 600;");

        // Cột 5: Giá Vừa (5-10kg)
        TableColumn<CareService, String> colPriceMed = new TableColumn<>("Giá Vừa (5-10kg)");
        colPriceMed.setCellValueFactory(c -> {
            double price = c.getValue().getGiaVua();
            return new ReadOnlyStringWrapper(price > 0 ? currencyFormat.format(price) : "-");
        });
        colPriceMed.setPrefWidth(125);
        colPriceMed.setStyle("-fx-alignment: CENTER-RIGHT; -fx-text-fill: #1d4ed8; -fx-font-weight: 600;");

        // Cột 6: Giá Lớn (>10kg)
        TableColumn<CareService, String> colPriceLarge = new TableColumn<>("Giá Lớn (>10kg)");
        colPriceLarge.setCellValueFactory(c -> {
            double price = c.getValue().getGiaLon();
            return new ReadOnlyStringWrapper(price > 0 ? currencyFormat.format(price) : "-");
        });
        colPriceLarge.setPrefWidth(125);
        colPriceLarge.setStyle("-fx-alignment: CENTER-RIGHT; -fx-text-fill: #7e22ce; -fx-font-weight: 600;");

        // Cột 7: Trạng thái
        TableColumn<CareService, CareService> colStatus = new TableColumn<>("Trạng thái");
        colStatus.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        colStatus.setPrefWidth(140);
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(CareService item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label lblBadge = new Label(item.isTrangThai() ? "Đang cung cấp" : "Tạm ngừng");
                    lblBadge.setStyle(item.isTrangThai()
                            ? "-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-padding: 3px 10px; -fx-background-radius: 10px; -fx-font-weight: bold; -fx-font-size: 11px;"
                            : "-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-padding: 3px 10px; -fx-background-radius: 10px; -fx-font-weight: bold; -fx-font-size: 11px;");
                    setGraphic(lblBadge);
                    setAlignment(Pos.CENTER);
                    setText(null);
                }
            }
        });

        tbl.getColumns().addAll(List.of(colId, colName, colDuration, colPriceSmall, colPriceMed, colPriceLarge, colStatus));

        // Xử lý sự kiện nhấp đúp dòng
        tbl.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                CareService selected = tbl.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    if (allowEditOnDoubleClick) {
                        xuLySuaDichVu();
                    } else {
                        xuLyXemChiTiet();
                    }
                }
            }
        });

        return tbl;
    }

    private TableView<ServicePricing> buildPricingTable() {
        TableView<ServicePricing> tbl = new TableView<>();
        tbl.setItems(filteredPricingList);
        tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tbl.setPlaceholder(new Label("Không tìm thấy bảng giá dịch vụ phù hợp"));

        // Cột 1: Mã bảng giá
        TableColumn<ServicePricing, String> colPricingId = new TableColumn<>("Mã bảng giá");
        colPricingId.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getMaBangGia()));
        colPricingId.setPrefWidth(140);
        colPricingId.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-weight: bold;");

        // Cột 2: Tên dịch vụ
        TableColumn<ServicePricing, String> colServiceName = new TableColumn<>("Tên dịch vụ chăm sóc");
        colServiceName.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTenDichVu() != null ? c.getValue().getTenDichVu() : c.getValue().getMaDichVu()));
        colServiceName.setPrefWidth(220);
        colServiceName.setStyle("-fx-font-weight: 600;");

        // Cột 3: Bậc kích thước
        TableColumn<ServicePricing, ServicePricing> colSize = new TableColumn<>("Bậc kích thước");
        colSize.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        colSize.setPrefWidth(120);
        colSize.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(ServicePricing item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    String size = item.getPhanLoaiKichThuoc();
                    Label lblTier = new Label(size);
                    String style = switch (size) {
                        case "Nhỏ" -> "-fx-background-color: #dcfce7; -fx-text-fill: #15803d;";
                        case "Vừa" -> "-fx-background-color: #dbeafe; -fx-text-fill: #1d4ed8;";
                        default    -> "-fx-background-color: #f3e8ff; -fx-text-fill: #7e22ce;";
                    };
                    lblTier.setStyle(style + " -fx-padding: 3px 10px; -fx-background-radius: 10px; -fx-font-weight: bold; -fx-font-size: 11px;");
                    setGraphic(lblTier);
                    setAlignment(Pos.CENTER);
                    setText(null);
                }
            }
        });

        // Cột 4: Đơn giá
        TableColumn<ServicePricing, String> colPrice = new TableColumn<>("Đơn giá (VNĐ)");
        colPrice.setCellValueFactory(c -> new ReadOnlyStringWrapper(currencyFormat.format(c.getValue().getGia())));
        colPrice.setPrefWidth(130);
        colPrice.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: #15803d;");

        // Cột 5: Ngày áp dụng
        TableColumn<ServicePricing, String> colEffectiveDate = new TableColumn<>("Ngày áp dụng");
        colEffectiveDate.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getNgayApDung() != null ? c.getValue().getNgayApDung().format(dateFormatter) : "-"
        ));
        colEffectiveDate.setPrefWidth(110);
        colEffectiveDate.setStyle("-fx-alignment: CENTER;");

        // Cột 6: Ngày hết hiệu lực
        TableColumn<ServicePricing, String> colExpiryDate = new TableColumn<>("Ngày hết hiệu lực");
        colExpiryDate.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getNgayHetHieuLuc() != null ? c.getValue().getNgayHetHieuLuc().format(dateFormatter) : "Vô thời hạn"
        ));
        colExpiryDate.setPrefWidth(120);
        colExpiryDate.setStyle("-fx-alignment: CENTER;");

        // Cột 7: Tình trạng hiệu lực
        TableColumn<ServicePricing, ServicePricing> colStatus = new TableColumn<>("Hiệu lực");
        colStatus.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        colStatus.setPrefWidth(110);
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(ServicePricing item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    boolean isValid = item.isConHieuLuc();
                    Label lblBadge = new Label(isValid ? "Còn hiệu lực" : "Hết hiệu lực");
                    lblBadge.setStyle(isValid
                            ? "-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-padding: 3px 8px; -fx-background-radius: 10px; -fx-font-size: 11px; -fx-font-weight: 600;"
                            : "-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-padding: 3px 8px; -fx-background-radius: 10px; -fx-font-size: 11px; -fx-font-weight: 600;");
                    setGraphic(lblBadge);
                    setAlignment(Pos.CENTER);
                    setText(null);
                }
            }
        });

        tbl.getColumns().addAll(List.of(colPricingId, colServiceName, colSize, colPrice, colEffectiveDate, colExpiryDate, colStatus));

        tbl.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                xuLySuaBangGia();
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

    private HBox buildReportSummaryCards() {
        HBox pnlStats = new HBox(16);
        pnlStats.setAlignment(Pos.CENTER_LEFT);

        VBox cardTotal = createSummaryCard("Tổng số dịch vụ", "0", "#eff6ff", "#1d4ed8");
        VBox cardActive = createSummaryCard("Đang mở cung cấp", "0", "#f0fdf4", "#15803d");
        VBox cardPricing = createSummaryCard("Số mức giá thiết lập", "0", "#faf5ff", "#7e22ce");

        HBox.setHgrow(cardTotal, Priority.ALWAYS);
        HBox.setHgrow(cardActive, Priority.ALWAYS);
        HBox.setHgrow(cardPricing, Priority.ALWAYS);

        pnlStats.getChildren().addAll(cardTotal, cardActive, cardPricing);
        return pnlStats;
    }

    private VBox createSummaryCard(String title, String initialValue, String bg, String text) {
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
    // XỬ LÝ SỰ KIỆN NGHIỆP VỤ (BUSINESS LOGIC ACTIONS)
    // =========================================================================

    private void taiDuLieuTuDatabase() {
        if (isBusy.get()) return;
        isBusy.set(true);
        if (lblStatusCount != null) {
            lblStatusCount.setText("Đang đồng bộ dữ liệu từ CSDL...");
        }

        CompletableFuture.runAsync(() -> {
            try {
                List<CareService> services = careServiceController.getAllCareServices();
                List<ServicePricing> pricings = careServiceController.searchServicePricings(null, null);

                Platform.runLater(() -> {
                    masterServiceList.setAll(services);
                    masterPricingList.setAll(pricings);

                    capNhatThongBaoTrangThai();
                    capNhatTheBaoCao();
                    isBusy.set(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    isBusy.set(false);
                    if (lblStatusCount != null) {
                        lblStatusCount.setText("Lỗi nạp dữ liệu: " + ex.getMessage());
                    }
                    hienThiThongBaoLoi("Lỗi tải dữ liệu", ex.getMessage());
                });
            }
        });
    }

    private void apDungBoLocDichVu() {
        String keyword = txtSearch != null && txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";
        String statusFilter = cboStatusFilter != null && cboStatusFilter.getValue() != null ? cboStatusFilter.getValue() : "Tất cả trạng thái";

        filteredServiceList.setPredicate(service -> {
            boolean matchesSearch = true;
            if (!keyword.isEmpty()) {
                matchesSearch = (service.getMaDichVu() != null && service.getMaDichVu().toLowerCase().contains(keyword))
                        || (service.getTenDichVu() != null && service.getTenDichVu().toLowerCase().contains(keyword))
                        || (service.getMoTa() != null && service.getMoTa().toLowerCase().contains(keyword));
            }

            boolean matchesStatus = true;
            if ("Đang cung cấp".equalsIgnoreCase(statusFilter)) {
                matchesStatus = service.isTrangThai();
            } else if ("Tạm ngừng".equalsIgnoreCase(statusFilter)) {
                matchesStatus = !service.isTrangThai();
            }

            return matchesSearch && matchesStatus;
        });

        capNhatThongBaoTrangThai();
    }

    private void apDungBoLocBangGia() {
        String keyword = txtSearch != null && txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";
        String sizeFilter = cboSizeFilter != null && cboSizeFilter.getValue() != null ? cboSizeFilter.getValue() : "Tất cả kích thước";

        filteredPricingList.setPredicate(pricing -> {
            boolean matchesSearch = true;
            if (!keyword.isEmpty()) {
                matchesSearch = (pricing.getMaBangGia() != null && pricing.getMaBangGia().toLowerCase().contains(keyword))
                        || (pricing.getMaDichVu() != null && pricing.getMaDichVu().toLowerCase().contains(keyword))
                        || (pricing.getTenDichVu() != null && pricing.getTenDichVu().toLowerCase().contains(keyword));
            }

            boolean matchesSize = true;
            if (!"Tất cả kích thước".equalsIgnoreCase(sizeFilter) && !"Tất cả".equalsIgnoreCase(sizeFilter)) {
                matchesSize = pricing.getPhanLoaiKichThuoc() != null && pricing.getPhanLoaiKichThuoc().equalsIgnoreCase(sizeFilter);
            }

            return matchesSearch && matchesSize;
        });

        capNhatThongBaoTrangThai();
    }

    private void capNhatThongBaoTrangThai() {
        if (lblStatusCount == null) return;
        if (tblPrices != null && getChildren().contains(tblPrices)) {
            lblStatusCount.setText(String.format("Hiển thị %d / %d mức giá dịch vụ",
                    filteredPricingList.size(), masterPricingList.size()));
        } else {
            lblStatusCount.setText(String.format("Hiển thị %d / %d dịch vụ chăm sóc",
                    filteredServiceList.size(), masterServiceList.size()));
        }
    }

    private void capNhatTheBaoCao() {
        long totalServices = masterServiceList.size();
        long activeServices = masterServiceList.stream().filter(CareService::isTrangThai).count();
        long totalPricing = masterPricingList.size();

        lookupAndSetKpi("kpi_Tổng số dịch vụ", String.valueOf(totalServices));
        lookupAndSetKpi("kpi_Đang mở cung cấp", String.valueOf(activeServices));
        lookupAndSetKpi("kpi_Số mức giá thiết lập", String.valueOf(totalPricing));
    }

    private void lookupAndSetKpi(String id, String text) {
        javafx.scene.Node node = lookup("#" + id);
        if (node instanceof Label label) {
            label.setText(text);
        }
    }

    private void xuLyThemDichVu() {
        Optional<CareService> result = CareServiceEditorDialog.showAndEdit(getScene().getWindow(), null);
        result.ifPresent(service -> {
            hienThiThongBaoThanhCong("Thêm dịch vụ thành công", "Đã lưu dịch vụ: " + service.getTenDichVu());
            taiDuLieuTuDatabase();
        });
    }

    private void xuLySuaDichVu() {
        if (tblServices == null) return;
        CareService selected = tblServices.getSelectionModel().getSelectedItem();
        if (selected == null) {
            hienThiCanhBao("Chưa chọn dịch vụ", "Vui lòng chọn một dịch vụ từ danh sách để thực hiện sửa đổi.");
            return;
        }

        Optional<CareService> result = CareServiceEditorDialog.showAndEdit(getScene().getWindow(), selected);
        result.ifPresent(service -> {
            hienThiThongBaoThanhCong("Cập nhật thành công", "Đã cập nhật dịch vụ: " + service.getTenDichVu());
            taiDuLieuTuDatabase();
        });
    }

    /**
     * Thao tác xóa mềm dịch vụ (chuyển trangThai = 0).
     */
    private void xuLyNgungCungCap() {
        if (tblServices == null) return;
        CareService selected = tblServices.getSelectionModel().getSelectedItem();
        if (selected == null) {
            hienThiCanhBao("Chưa chọn dịch vụ", "Vui lòng chọn dịch vụ muốn ngừng cung cấp.");
            return;
        }

        if (!selected.isTrangThai()) {
            hienThiCanhBao("Dịch vụ đã ngừng cung cấp", "Dịch vụ này hiện đang ở trạng thái ngừng cung cấp.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận ngừng cung cấp - Happy Tails");
        confirm.setHeaderText("Xác nhận ngừng cung cấp dịch vụ: " + selected.getTenDichVu());
        confirm.setContentText("Dịch vụ sẽ chuyển sang trạng thái tạm ngừng (xóa mềm). Bạn có thể kích hoạt lại bất kỳ lúc nào.");

        confirm.getButtonTypes().setAll(
                new ButtonType("Đồng ý", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Hủy bỏ", ButtonBar.ButtonData.CANCEL_CLOSE)
        );

        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isPresent() && choice.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
            try {
                boolean success = careServiceController.deactivateCareService(selected.getMaDichVu());
                if (success) {
                    hienThiThongBaoThanhCong("Ngừng cung cấp thành công", "Đã chuyển trạng thái dịch vụ sang Tạm ngừng.");
                    taiDuLieuTuDatabase();
                }
            } catch (Exception ex) {
                hienThiThongBaoLoi("Lỗi thực hiện", ex.getMessage());
            }
        }
    }

    /**
     * Kích hoạt lại dịch vụ đã tạm ngừng (trangThai = 1).
     */
    private void xuLyKichHoatLai() {
        if (tblServices == null) return;
        CareService selected = tblServices.getSelectionModel().getSelectedItem();
        if (selected == null) {
            hienThiCanhBao("Chưa chọn dịch vụ", "Vui lòng chọn dịch vụ muốn kích hoạt lại.");
            return;
        }

        if (selected.isTrangThai()) {
            hienThiCanhBao("Dịch vụ đang hoạt động", "Dịch vụ này hiện đang mở cung cấp.");
            return;
        }

        try {
            boolean success = careServiceController.activateCareService(selected.getMaDichVu());
            if (success) {
                hienThiThongBaoThanhCong("Kích hoạt thành công", "Dịch vụ đã được mở cung cấp lại cho khách hàng.");
                taiDuLieuTuDatabase();
            }
        } catch (Exception ex) {
            hienThiThongBaoLoi("Lỗi thực hiện", ex.getMessage());
        }
    }

    private void xuLyXemChiTiet() {
        if (tblServices == null) return;
        CareService selected = tblServices.getSelectionModel().getSelectedItem();
        if (selected == null) {
            hienThiCanhBao("Chưa chọn dịch vụ", "Vui lòng chọn một dịch vụ từ danh sách để xem chi tiết.");
            return;
        }

        CareServiceDetailDialog.showDetails(getScene().getWindow(), selected);
    }

    private void xuLyThemBangGia() {
        CareService selectedService = null;
        if (tblServices != null) {
            selectedService = tblServices.getSelectionModel().getSelectedItem();
        }

        Optional<ServicePricing> result = CareServicePricingDialog.showAndEdit(getScene().getWindow(), null, selectedService);
        result.ifPresent(pricing -> {
            hienThiThongBaoThanhCong("Thiết lập giá thành công", "Đã lưu mức giá mới cho bậc: " + pricing.getPhanLoaiKichThuoc());
            taiDuLieuTuDatabase();
        });
    }

    private void xuLySuaBangGia() {
        if (tblPrices == null) return;
        ServicePricing selected = tblPrices.getSelectionModel().getSelectedItem();
        if (selected == null) {
            hienThiCanhBao("Chưa chọn mức giá", "Vui lòng chọn một mức giá từ danh sách để điều chỉnh.");
            return;
        }

        Optional<ServicePricing> result = CareServicePricingDialog.showAndEdit(getScene().getWindow(), selected, null);
        result.ifPresent(pricing -> {
            hienThiThongBaoThanhCong("Cập nhật giá thành công", "Đã lưu mức giá điều chỉnh.");
            taiDuLieuTuDatabase();
        });
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
