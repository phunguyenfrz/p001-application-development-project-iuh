package com.petshop.view.product;

import com.petshop.controller.ProductController;
import com.petshop.model.Product;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Giao diện Quản lý Sản phẩm & Tồn kho (ProductView).
 * Thiết kế chuẩn kiến trúc IUH: Mỗi màn hình chỉ đảm nhận 1 chức năng duy nhất:
 * - PRD_UPDATE / PRD_CRUD: Cập nhật sản phẩm (Thêm, Sửa, Ngừng kinh doanh/Xóa mềm).
 * - PRD_SEARCH: Tìm kiếm sản phẩm đa tiêu chí, tra cứu chi tiết.
 * - PRD_INVENTORY: Quản lý số lượng tồn kho và ngưỡng an toàn.
 * - PRD_LOW_STOCK / PRD_REPORT: Giám sát và cảnh báo hàng sắp hết / hết hàng.
 *
 * Tuân thủ Definition of Done:
 * - Điều khiển đặt tên tiền tố: btn, txt, lbl, tbl, cbo.
 * - Tên lớp là danh từ, tên phương thức là động từ; đầy đủ Javadoc/comment.
 * - Tất cả nút có setMinWidth(Region.USE_PREF_SIZE) để hiển thị đầy đủ tên, không bị co rút "...".
 * - Bỏ thanh tab ở đầu trang, mỗi chức năng mở màn hình riêng biệt.
 * - Tính năng mã vạch tạm thời được vô hiệu hóa theo yêu cầu thực tế của nhóm.
 */
public class ProductView extends VBox {

    private final String currentActionId;
    private final ProductController productController = new ProductController();
    private final ObservableList<Product> masterProductList = FXCollections.observableArrayList();
    private final FilteredList<Product> filteredProductList = new FilteredList<>(masterProductList, p -> true);
    private final SimpleBooleanProperty isBusy = new SimpleBooleanProperty(false);
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0 đ");

    // Các điều khiển tuân thủ quy tắc tiền tố
    private TableView<Product> tblProducts;
    private TextField txtSearch;
    private ComboBox<String> cboStatusFilter;
    private ComboBox<String> cboStockFilter;

    // Nút chức năng thanh công cụ
    private Button btnAdd;
    private Button btnEdit;
    private Button btnDeactivate;
    private Button btnActivate;
    private Button btnViewDetail;
    private Button btnAdjustStock;
    private Button btnSearch;
    private Button btnRefresh;

    // Nhãn thông tin
    private Label lblTitle;
    private Label lblStatusCount;
    private Label lblWarningBanner;

    public ProductView() {
        this("PRD_UPDATE");
    }

    public ProductView(String actionId) {
        this.currentActionId = actionId != null ? actionId : "PRD_UPDATE";

        getStyleClass().addAll("customer-page", "product-page");
        setPadding(new Insets(24, 28, 24, 28));
        setSpacing(18);
        VBox.setVgrow(this, Priority.ALWAYS);

        // Khởi tạo đúng 1 màn hình duy nhất theo mã chức năng
        if ("PRD_SEARCH".equalsIgnoreCase(currentActionId)) {
            initializeSearchScreen();
        } else if ("PRD_INVENTORY".equalsIgnoreCase(currentActionId)) {
            initializeInventoryScreen();
        } else if ("PRD_LOW_STOCK".equalsIgnoreCase(currentActionId) || "PRD_REPORT".equalsIgnoreCase(currentActionId)) {
            initializeLowStockScreen();
        } else {
            initializeUpdateScreen();
        }

        // Tải dữ liệu từ database
        taiDuLieuTuDatabase();
    }

    // =========================================================================
    // 1. MÀN HÌNH CẬP NHẬT SẢN PHẨM (PRD_UPDATE / PRD_CRUD)
    // =========================================================================
    private void initializeUpdateScreen() {
        lblTitle = new Label("Cập nhật sản phẩm");
        lblTitle.getStyleClass().add("customer-title");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        btnAdd = new Button("＋ Thêm sản phẩm");
        btnAdd.getStyleClass().add("customer-add");
        btnAdd.setMinWidth(Region.USE_PREF_SIZE);
        btnAdd.setOnAction(e -> xuLyThemSanPham());

        btnEdit = new Button("Sửa sản phẩm");
        btnEdit.getStyleClass().add("customer-secondary");
        btnEdit.setMinWidth(Region.USE_PREF_SIZE);
        btnEdit.setOnAction(e -> xuLySuaSanPham());

        btnDeactivate = new Button("Ngừng kinh doanh");
        btnDeactivate.getStyleClass().add("customer-danger");
        btnDeactivate.setMinWidth(Region.USE_PREF_SIZE);
        btnDeactivate.setOnAction(e -> xuLyNgungKinhDoanh());

        btnActivate = new Button("Kinh doanh lại");
        btnActivate.getStyleClass().add("customer-secondary");
        btnActivate.setMinWidth(Region.USE_PREF_SIZE);
        btnActivate.setVisible(false);
        btnActivate.setManaged(false);
        btnActivate.setOnAction(e -> xuLyKichHoatLai());

        HBox pnlHeader = new HBox(12, lblTitle, headerSpacer, btnAdd, btnEdit, btnDeactivate, btnActivate);
        pnlHeader.setAlignment(Pos.CENTER_LEFT);

        // Thanh công cụ lọc nhanh cho trang cập nhật
        HBox pnlToolbar = new HBox(10);
        pnlToolbar.setAlignment(Pos.CENTER_LEFT);

        txtSearch = new TextField();
        txtSearch.setPromptText("Tìm kiếm sản phẩm cần sửa theo mã hoặc tên...");
        txtSearch.setPrefHeight(38);
        txtSearch.setPrefWidth(350);
        txtSearch.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8px; -fx-border-color: #d1ded1; -fx-border-radius: 8px; -fx-padding: 0 12px;");

        PauseTransition debounce = new PauseTransition(Duration.millis(150));
        debounce.setOnFinished(e -> apDungBoLocCapNhat());
        txtSearch.textProperty().addListener((obs, o, n) -> debounce.playFromStart());

        cboStatusFilter = new ComboBox<>(FXCollections.observableArrayList(
                "Tất cả trạng thái", "Đang kinh doanh", "Ngừng kinh doanh"
        ));
        cboStatusFilter.setValue("Tất cả trạng thái");
        cboStatusFilter.setPrefHeight(38);
        cboStatusFilter.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 8px;");
        cboStatusFilter.setOnAction(e -> apDungBoLocCapNhat());

        btnRefresh = new Button("Làm mới");
        btnRefresh.getStyleClass().add("customer-secondary");
        btnRefresh.setMinWidth(Region.USE_PREF_SIZE);
        btnRefresh.setPrefHeight(38);
        btnRefresh.setOnAction(e -> taiDuLieuTuDatabase());

        pnlToolbar.getChildren().addAll(txtSearch, cboStatusFilter, btnRefresh);

        // Bảng danh sách sản phẩm
        tblProducts = buildUpdateTable();
        VBox.setVgrow(tblProducts, Priority.ALWAYS);

        VBox pnlTableCard = createTableCard(tblProducts);

        // Khóa các nút khi chưa chọn dòng
        btnEdit.setDisable(true);
        btnDeactivate.setDisable(true);
        btnActivate.setDisable(true);

        tblProducts.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                btnEdit.setDisable(true);
                btnDeactivate.setDisable(true);
                btnActivate.setDisable(true);
                btnDeactivate.setVisible(true);
                btnDeactivate.setManaged(true);
                btnActivate.setVisible(false);
                btnActivate.setManaged(false);
            } else {
                btnEdit.setDisable(false);
                if (newVal.isTrangThai()) {
                    btnDeactivate.setDisable(false);
                    btnDeactivate.setVisible(true);
                    btnDeactivate.setManaged(true);
                    btnActivate.setVisible(false);
                    btnActivate.setManaged(false);
                } else {
                    btnActivate.setDisable(false);
                    btnActivate.setVisible(true);
                    btnActivate.setManaged(true);
                    btnDeactivate.setVisible(false);
                    btnDeactivate.setManaged(false);
                }
            }
        });

        getChildren().addAll(pnlHeader, pnlToolbar, pnlTableCard);
    }

    private TableView<Product> buildUpdateTable() {
        TableView<Product> table = new TableView<>();
        table.setItems(filteredProductList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Product, String> colId = new TableColumn<>("Mã sản phẩm");
        colId.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getMaSanPham()));
        colId.setPrefWidth(130);

        TableColumn<Product, String> colName = new TableColumn<>("Tên sản phẩm");
        colName.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTenSanPham()));
        colName.setPrefWidth(240);

        TableColumn<Product, String> colUnit = new TableColumn<>("ĐVT");
        colUnit.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getDonViTinh()));
        colUnit.setPrefWidth(70);
        colUnit.setStyle("-fx-alignment: CENTER;");

        TableColumn<Product, String> colPrice = new TableColumn<>("Giá bán");
        colPrice.setCellValueFactory(data -> new ReadOnlyStringWrapper(currencyFormat.format(data.getValue().getGiaBan())));
        colPrice.setPrefWidth(120);
        colPrice.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");

        TableColumn<Product, String> colStock = new TableColumn<>("Tồn kho");
        colStock.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().getSoLuongTon())));
        colStock.setPrefWidth(90);
        colStock.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        TableColumn<Product, String> colSupplier = new TableColumn<>("Nhà cung cấp");
        colSupplier.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getTenNhaCungCap() != null ? data.getValue().getTenNhaCungCap() : "—"
        ));
        colSupplier.setPrefWidth(170);

        TableColumn<Product, String> colStatus = new TableColumn<>("Trạng thái");
        colStatus.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().isTrangThai() ? "Đang bán" : "Ngừng bán"
        ));
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label lbl = new Label(item);
                    lbl.getStyleClass().add("Đang bán".equals(item) ? "product-badge-active" : "product-badge-inactive");
                    setGraphic(lbl);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        colStatus.setPrefWidth(110);

        table.getColumns().addAll(List.of(colId, colName, colUnit, colPrice, colStock, colSupplier, colStatus));

        table.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty() && event.getButton() == MouseButton.PRIMARY) {
                    xuLySuaSanPham();
                }
            });
            return row;
        });

        return table;
    }

    private void apDungBoLocCapNhat() {
        String keyword = txtSearch != null && txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";
        String statusFilter = cboStatusFilter != null ? cboStatusFilter.getValue() : null;

        filteredProductList.setPredicate(p -> {
            if (statusFilter != null) {
                if ("Đang kinh doanh".equals(statusFilter) && !p.isTrangThai()) return false;
                if ("Ngừng kinh doanh".equals(statusFilter) && p.isTrangThai()) return false;
            }
            if (!keyword.isEmpty()) {
                boolean matchId = p.getMaSanPham() != null && p.getMaSanPham().toLowerCase().contains(keyword);
                boolean matchName = p.getTenSanPham() != null && p.getTenSanPham().toLowerCase().contains(keyword);
                boolean matchSupplier = p.getTenNhaCungCap() != null && p.getTenNhaCungCap().toLowerCase().contains(keyword);
                return matchId || matchName || matchSupplier;
            }
            return true;
        });

        capNhatNhanTrangThai();
    }

    // =========================================================================
    // 2. MÀN HÌNH TÌM KIẾM SẢN PHẨM (PRD_SEARCH)
    // =========================================================================
    private void initializeSearchScreen() {
        lblTitle = new Label("Tìm kiếm sản phẩm");
        lblTitle.getStyleClass().add("customer-title");

        HBox pnlSearchToolbar = new HBox(10);
        pnlSearchToolbar.setAlignment(Pos.CENTER_LEFT);

        txtSearch = new TextField();
        txtSearch.setPromptText("Tìm kiếm theo mã sản phẩm, tên hoặc nhà cung cấp...");
        txtSearch.setPrefHeight(38);
        txtSearch.setPrefWidth(350);
        txtSearch.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8px; -fx-border-color: #d1ded1; -fx-border-radius: 8px; -fx-padding: 0 12px;");
        txtSearch.setOnAction(e -> apDungBoLocTimKiem());

        cboStockFilter = new ComboBox<>(FXCollections.observableArrayList(
                "Tất cả hàng tồn", "Còn hàng", "Sắp hết hàng", "Hết hàng"
        ));
        cboStockFilter.setValue("Tất cả hàng tồn");
        cboStockFilter.setPrefHeight(38);
        cboStockFilter.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 8px;");
        cboStockFilter.setOnAction(e -> apDungBoLocTimKiem());

        cboStatusFilter = new ComboBox<>(FXCollections.observableArrayList(
                "Tất cả trạng thái", "Đang kinh doanh", "Ngừng kinh doanh"
        ));
        cboStatusFilter.setValue("Tất cả trạng thái");
        cboStatusFilter.setPrefHeight(38);
        cboStatusFilter.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 8px;");
        cboStatusFilter.setOnAction(e -> apDungBoLocTimKiem());

        btnSearch = new Button("Tìm kiếm");
        btnSearch.getStyleClass().add("customer-save");
        btnSearch.setMinWidth(Region.USE_PREF_SIZE);
        btnSearch.setPrefHeight(38);
        btnSearch.setOnAction(e -> apDungBoLocTimKiem());

        btnRefresh = new Button("Làm mới");
        btnRefresh.getStyleClass().add("customer-secondary");
        btnRefresh.setMinWidth(Region.USE_PREF_SIZE);
        btnRefresh.setPrefHeight(38);
        btnRefresh.setOnAction(e -> {
            txtSearch.clear();
            cboStockFilter.setValue("Tất cả hàng tồn");
            cboStatusFilter.setValue("Tất cả trạng thái");
            taiDuLieuTuDatabase();
        });

        btnViewDetail = new Button("Xem chi tiết");
        btnViewDetail.getStyleClass().add("customer-secondary");
        btnViewDetail.setMinWidth(Region.USE_PREF_SIZE);
        btnViewDetail.setPrefHeight(38);
        btnViewDetail.setDisable(true);
        btnViewDetail.setOnAction(e -> xuLyXemChiTiet());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        pnlSearchToolbar.getChildren().addAll(
                txtSearch, cboStockFilter, cboStatusFilter,
                btnSearch, btnRefresh, spacer, btnViewDetail
        );

        tblProducts = buildSearchTable();
        VBox.setVgrow(tblProducts, Priority.ALWAYS);

        VBox pnlTableCard = createTableCard(tblProducts);

        tblProducts.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            btnViewDetail.setDisable(newVal == null);
        });

        getChildren().addAll(lblTitle, pnlSearchToolbar, pnlTableCard);
    }

    private TableView<Product> buildSearchTable() {
        TableView<Product> table = new TableView<>();
        table.setItems(filteredProductList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Product, String> colId = new TableColumn<>("Mã sản phẩm");
        colId.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getMaSanPham()));
        colId.setPrefWidth(130);

        TableColumn<Product, String> colName = new TableColumn<>("Tên sản phẩm");
        colName.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTenSanPham()));
        colName.setPrefWidth(240);

        TableColumn<Product, String> colUnit = new TableColumn<>("ĐVT");
        colUnit.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getDonViTinh()));
        colUnit.setPrefWidth(70);
        colUnit.setStyle("-fx-alignment: CENTER;");

        TableColumn<Product, String> colPrice = new TableColumn<>("Giá bán");
        colPrice.setCellValueFactory(data -> new ReadOnlyStringWrapper(currencyFormat.format(data.getValue().getGiaBan())));
        colPrice.setPrefWidth(120);
        colPrice.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");

        TableColumn<Product, String> colStock = new TableColumn<>("Tồn kho");
        colStock.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().getSoLuongTon())));
        colStock.setPrefWidth(85);
        colStock.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        TableColumn<Product, String> colStockStatus = new TableColumn<>("Mức tồn");
        colStockStatus.setCellValueFactory(data -> {
            int ton = data.getValue().getSoLuongTon();
            int min = data.getValue().getNguongTonToiThieu();
            if (ton <= 0) return new ReadOnlyStringWrapper("Hết hàng");
            if (ton <= min) return new ReadOnlyStringWrapper("Sắp hết (" + ton + "/" + min + ")");
            return new ReadOnlyStringWrapper("Đủ hàng");
        });
        colStockStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label lbl = new Label(item);
                    if (item.contains("Hết hàng")) {
                        lbl.getStyleClass().add("product-badge-outstock");
                    } else if (item.contains("Sắp hết")) {
                        lbl.getStyleClass().add("product-badge-lowstock");
                    } else {
                        lbl.getStyleClass().add("product-badge-instock");
                    }
                    setGraphic(lbl);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        colStockStatus.setPrefWidth(120);

        TableColumn<Product, String> colStatus = new TableColumn<>("Trạng thái");
        colStatus.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().isTrangThai() ? "Đang bán" : "Ngừng bán"
        ));
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label lbl = new Label(item);
                    lbl.getStyleClass().add("Đang bán".equals(item) ? "product-badge-active" : "product-badge-inactive");
                    setGraphic(lbl);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        colStatus.setPrefWidth(100);

        TableColumn<Product, String> colSupplier = new TableColumn<>("Nhà cung cấp");
        colSupplier.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getTenNhaCungCap() != null ? data.getValue().getTenNhaCungCap() : "—"
        ));
        colSupplier.setPrefWidth(160);

        table.getColumns().addAll(List.of(colId, colName, colUnit, colPrice, colStock, colStockStatus, colStatus, colSupplier));

        table.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty() && event.getButton() == MouseButton.PRIMARY) {
                    xuLyXemChiTiet();
                }
            });
            return row;
        });

        return table;
    }

    private void apDungBoLocTimKiem() {
        String keyword = txtSearch != null && txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";
        String statusFilter = cboStatusFilter != null ? cboStatusFilter.getValue() : null;
        String stockFilter = cboStockFilter != null ? cboStockFilter.getValue() : null;

        filteredProductList.setPredicate(p -> {
            if (statusFilter != null) {
                if ("Đang kinh doanh".equals(statusFilter) && !p.isTrangThai()) return false;
                if ("Ngừng kinh doanh".equals(statusFilter) && p.isTrangThai()) return false;
            }
            if (stockFilter != null) {
                if ("Còn hàng".equals(stockFilter) && p.getSoLuongTon() <= 0) return false;
                if ("Sắp hết hàng".equals(stockFilter) && !p.isSapHetHang()) return false;
                if ("Hết hàng".equals(stockFilter) && p.getSoLuongTon() > 0) return false;
            }
            if (!keyword.isEmpty()) {
                boolean matchId = p.getMaSanPham() != null && p.getMaSanPham().toLowerCase().contains(keyword);
                boolean matchName = p.getTenSanPham() != null && p.getTenSanPham().toLowerCase().contains(keyword);
                boolean matchSupplier = p.getTenNhaCungCap() != null && p.getTenNhaCungCap().toLowerCase().contains(keyword);
                return matchId || matchName || matchSupplier;
            }
            return true;
        });

        capNhatNhanTrangThai();
    }

    // =========================================================================
    // 3. MÀN HÌNH QUẢN LÝ TỒN KHO (PRD_INVENTORY)
    // =========================================================================
    private void initializeInventoryScreen() {
        lblTitle = new Label("Quản lý tồn kho");
        lblTitle.getStyleClass().add("customer-title");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        btnAdjustStock = new Button("Điều chỉnh tồn kho");
        btnAdjustStock.getStyleClass().add("customer-add");
        btnAdjustStock.setMinWidth(Region.USE_PREF_SIZE);
        btnAdjustStock.setDisable(true);
        btnAdjustStock.setOnAction(e -> xuLyDieuChinhTonKho());

        btnRefresh = new Button("Làm mới");
        btnRefresh.getStyleClass().add("customer-secondary");
        btnRefresh.setMinWidth(Region.USE_PREF_SIZE);
        btnRefresh.setOnAction(e -> taiDuLieuTuDatabase());

        HBox pnlHeader = new HBox(12, lblTitle, headerSpacer, btnAdjustStock, btnRefresh);
        pnlHeader.setAlignment(Pos.CENTER_LEFT);

        // Thanh tìm kiếm nhanh sản phẩm kiểm kho
        HBox pnlToolbar = new HBox(10);
        pnlToolbar.setAlignment(Pos.CENTER_LEFT);

        txtSearch = new TextField();
        txtSearch.setPromptText("Tìm sản phẩm kiểm kho theo mã hoặc tên...");
        txtSearch.setPrefHeight(38);
        txtSearch.setPrefWidth(350);
        txtSearch.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8px; -fx-border-color: #d1ded1; -fx-border-radius: 8px; -fx-padding: 0 12px;");

        PauseTransition debounce = new PauseTransition(Duration.millis(150));
        debounce.setOnFinished(e -> apDungBoLocTonKho());
        txtSearch.textProperty().addListener((obs, o, n) -> debounce.playFromStart());

        cboStockFilter = new ComboBox<>(FXCollections.observableArrayList(
                "Tất cả mức tồn", "Đủ hàng an toàn", "Sắp hết hàng (<= ngưỡng)", "Hết hàng (= 0)"
        ));
        cboStockFilter.setValue("Tất cả mức tồn");
        cboStockFilter.setPrefHeight(38);
        cboStockFilter.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 8px;");
        cboStockFilter.setOnAction(e -> apDungBoLocTonKho());

        pnlToolbar.getChildren().addAll(txtSearch, cboStockFilter);

        tblProducts = buildInventoryTable();
        VBox.setVgrow(tblProducts, Priority.ALWAYS);

        VBox pnlTableCard = createTableCard(tblProducts);

        tblProducts.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            btnAdjustStock.setDisable(newVal == null);
        });

        getChildren().addAll(pnlHeader, pnlToolbar, pnlTableCard);
    }

    private TableView<Product> buildInventoryTable() {
        TableView<Product> table = new TableView<>();
        table.setItems(filteredProductList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Product, String> colId = new TableColumn<>("Mã sản phẩm");
        colId.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getMaSanPham()));
        colId.setPrefWidth(130);

        TableColumn<Product, String> colName = new TableColumn<>("Tên sản phẩm");
        colName.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTenSanPham()));
        colName.setPrefWidth(250);

        TableColumn<Product, String> colUnit = new TableColumn<>("ĐVT");
        colUnit.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getDonViTinh()));
        colUnit.setPrefWidth(70);
        colUnit.setStyle("-fx-alignment: CENTER;");

        TableColumn<Product, String> colStock = new TableColumn<>("Tồn kho thực tế");
        colStock.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().getSoLuongTon())));
        colStock.setPrefWidth(120);
        colStock.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-font-size: 13.5px;");

        TableColumn<Product, String> colThreshold = new TableColumn<>("Ngưỡng an toàn");
        colThreshold.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().getNguongTonToiThieu())));
        colThreshold.setPrefWidth(120);
        colThreshold.setStyle("-fx-alignment: CENTER;");

        TableColumn<Product, String> colStockStatus = new TableColumn<>("Tình trạng kho");
        colStockStatus.setCellValueFactory(data -> {
            int ton = data.getValue().getSoLuongTon();
            int min = data.getValue().getNguongTonToiThieu();
            if (ton <= 0) return new ReadOnlyStringWrapper("Hết hàng");
            if (ton <= min) return new ReadOnlyStringWrapper("Sắp hết (" + ton + "/" + min + ")");
            return new ReadOnlyStringWrapper("Đủ hàng");
        });
        colStockStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label lbl = new Label(item);
                    if (item.contains("Hết hàng")) {
                        lbl.getStyleClass().add("product-badge-outstock");
                    } else if (item.contains("Sắp hết")) {
                        lbl.getStyleClass().add("product-badge-lowstock");
                    } else {
                        lbl.getStyleClass().add("product-badge-instock");
                    }
                    setGraphic(lbl);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        colStockStatus.setPrefWidth(130);

        TableColumn<Product, String> colSupplier = new TableColumn<>("Nhà cung cấp");
        colSupplier.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getTenNhaCungCap() != null ? data.getValue().getTenNhaCungCap() : "—"
        ));
        colSupplier.setPrefWidth(180);

        table.getColumns().addAll(List.of(colId, colName, colUnit, colStock, colThreshold, colStockStatus, colSupplier));

        table.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty() && event.getButton() == MouseButton.PRIMARY) {
                    xuLyDieuChinhTonKho();
                }
            });
            return row;
        });

        return table;
    }

    private void apDungBoLocTonKho() {
        String keyword = txtSearch != null && txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";
        String stockFilter = cboStockFilter != null ? cboStockFilter.getValue() : null;

        filteredProductList.setPredicate(p -> {
            if (stockFilter != null) {
                if ("Đủ hàng an toàn".equals(stockFilter) && p.isSapHetHang()) return false;
                if ("Sắp hết hàng (<= ngưỡng)".equals(stockFilter) && !p.isSapHetHang()) return false;
                if ("Hết hàng (= 0)".equals(stockFilter) && p.getSoLuongTon() > 0) return false;
            }
            if (!keyword.isEmpty()) {
                boolean matchId = p.getMaSanPham() != null && p.getMaSanPham().toLowerCase().contains(keyword);
                boolean matchName = p.getTenSanPham() != null && p.getTenSanPham().toLowerCase().contains(keyword);
                return matchId || matchName;
            }
            return true;
        });

        capNhatNhanTrangThai();
    }

    // =========================================================================
    // 4. MÀN HÌNH CẢNH BÁO HÀNG SẮP HẾT (PRD_LOW_STOCK / PRD_REPORT)
    // =========================================================================
    private void initializeLowStockScreen() {
        lblTitle = new Label("Cảnh báo hàng sắp hết & Hết hàng");
        lblTitle.getStyleClass().add("customer-title");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        btnAdjustStock = new Button("Nhập thêm hàng");
        btnAdjustStock.getStyleClass().add("customer-add");
        btnAdjustStock.setMinWidth(Region.USE_PREF_SIZE);
        btnAdjustStock.setDisable(true);
        btnAdjustStock.setOnAction(e -> xuLyDieuChinhTonKho());

        btnRefresh = new Button("Làm mới");
        btnRefresh.getStyleClass().add("customer-secondary");
        btnRefresh.setMinWidth(Region.USE_PREF_SIZE);
        btnRefresh.setOnAction(e -> taiDuLieuTuDatabase());

        HBox pnlHeader = new HBox(12, lblTitle, headerSpacer, btnAdjustStock, btnRefresh);
        pnlHeader.setAlignment(Pos.CENTER_LEFT);

        // Banner cảnh báo nổi bật
        lblWarningBanner = new Label();
        lblWarningBanner.setStyle(
                "-fx-background-color: #fef2f2; -fx-text-fill: #b91c1c; -fx-font-weight: bold; " +
                "-fx-padding: 10px 16px; -fx-background-radius: 8px; -fx-border-color: #fecaca; -fx-border-radius: 8px;"
        );

        tblProducts = buildLowStockTable();
        VBox.setVgrow(tblProducts, Priority.ALWAYS);

        VBox pnlTableCard = createTableCard(tblProducts);

        tblProducts.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            btnAdjustStock.setDisable(newVal == null);
        });

        getChildren().addAll(pnlHeader, lblWarningBanner, pnlTableCard);
    }

    private TableView<Product> buildLowStockTable() {
        TableView<Product> table = new TableView<>();
        table.setItems(filteredProductList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Product, String> colId = new TableColumn<>("Mã sản phẩm");
        colId.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getMaSanPham()));
        colId.setPrefWidth(130);

        TableColumn<Product, String> colName = new TableColumn<>("Tên sản phẩm");
        colName.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTenSanPham()));
        colName.setPrefWidth(260);

        TableColumn<Product, String> colUnit = new TableColumn<>("ĐVT");
        colUnit.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getDonViTinh()));
        colUnit.setPrefWidth(70);
        colUnit.setStyle("-fx-alignment: CENTER;");

        TableColumn<Product, String> colStock = new TableColumn<>("Tồn kho hiện tại");
        colStock.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().getSoLuongTon())));
        colStock.setPrefWidth(120);
        colStock.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #b91c1c;");

        TableColumn<Product, String> colThreshold = new TableColumn<>("Ngưỡng an toàn");
        colThreshold.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().getNguongTonToiThieu())));
        colThreshold.setPrefWidth(120);
        colThreshold.setStyle("-fx-alignment: CENTER;");

        TableColumn<Product, String> colAlert = new TableColumn<>("Mức độ cảnh báo");
        colAlert.setCellValueFactory(data -> {
            int ton = data.getValue().getSoLuongTon();
            return new ReadOnlyStringWrapper(ton <= 0 ? "HẾT HÀNG (0)" : "SẮP HẾT");
        });
        colAlert.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label lbl = new Label(item);
                    lbl.getStyleClass().add(item.contains("HẾT HÀNG") ? "product-badge-outstock" : "product-badge-lowstock");
                    setGraphic(lbl);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        colAlert.setPrefWidth(130);

        TableColumn<Product, String> colSupplier = new TableColumn<>("Nhà cung cấp");
        colSupplier.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getTenNhaCungCap() != null ? data.getValue().getTenNhaCungCap() : "—"
        ));
        colSupplier.setPrefWidth(180);

        table.getColumns().addAll(List.of(colId, colName, colUnit, colStock, colThreshold, colAlert, colSupplier));

        table.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty() && event.getButton() == MouseButton.PRIMARY) {
                    xuLyDieuChinhTonKho();
                }
            });
            return row;
        });

        return table;
    }

    private void apDungBoLocCanhBao() {
        int lowStockCount = 0;
        int outOfStockCount = 0;

        for (Product p : masterProductList) {
            if (p.isSapHetHang()) lowStockCount++;
            if (p.getSoLuongTon() == 0) outOfStockCount++;
        }

        if (lblWarningBanner != null) {
            if (lowStockCount > 0) {
                lblWarningBanner.setText(String.format("⚠ CẢNH BÁO: Đang có %d sản phẩm chạm hoặc dưới mức an toàn (trong đó %d mặt hàng hết tồn kho = 0). Cần bổ sung ngay!", lowStockCount, outOfStockCount));
            } else {
                lblWarningBanner.setText("✓ Kho hàng an toàn: Hiện không có mặt hàng nào dưới ngưỡng tồn an toàn.");
            }
        }

        filteredProductList.setPredicate(Product::isSapHetHang);
        capNhatNhanTrangThai();
    }

    // =========================================================================
    // DỰNG THẺ CHỨA BẢNG VÀ STATUS FOOTER
    // =========================================================================
    private VBox createTableCard(TableView<Product> table) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("customer-card", "product-card");
        card.setPadding(new Insets(16, 18, 16, 18));
        VBox.setVgrow(card, Priority.ALWAYS);

        lblStatusCount = new Label("Đang tải dữ liệu sản phẩm...");
        lblStatusCount.setStyle("-fx-font-size: 12px; -fx-text-fill: #738075; -fx-font-style: italic;");

        card.getChildren().addAll(table, lblStatusCount);
        return card;
    }

    private void capNhatNhanTrangThai() {
        if (lblStatusCount != null) {
            lblStatusCount.setText(String.format("Hiển thị %d / tổng số %d sản phẩm.", filteredProductList.size(), masterProductList.size()));
        }
    }

    // =========================================================================
    // TẢI DỮ LIỆU BẤT ĐỒNG BỘ
    // =========================================================================
    public void taiDuLieuTuDatabase() {
        isBusy.set(true);
        if (lblStatusCount != null) {
            lblStatusCount.setText("Đang tải dữ liệu sản phẩm từ CSDL...");
        }

        CompletableFuture.supplyAsync(productController::getAllProducts)
                .thenAccept(products -> Platform.runLater(() -> {
                    masterProductList.setAll(products);
                    isBusy.set(false);

                    if ("PRD_SEARCH".equalsIgnoreCase(currentActionId)) {
                        apDungBoLocTimKiem();
                    } else if ("PRD_INVENTORY".equalsIgnoreCase(currentActionId)) {
                        apDungBoLocTonKho();
                    } else if ("PRD_LOW_STOCK".equalsIgnoreCase(currentActionId) || "PRD_REPORT".equalsIgnoreCase(currentActionId)) {
                        apDungBoLocCanhBao();
                    } else {
                        apDungBoLocCapNhat();
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        isBusy.set(false);
                        if (lblStatusCount != null) {
                            lblStatusCount.setText("Lỗi khi tải dữ liệu: " + ex.getMessage());
                        }
                    });
                    return null;
                });
    }

    // =========================================================================
    // CÁC HÀM XỬ LÝ SỰ KIỆN THAO TÁC
    // =========================================================================
    private void xuLyThemSanPham() {
        Optional<Product> created = ProductEditorDialog.showAndEdit(getScene().getWindow(), null);
        created.ifPresent(product -> {
            taiDuLieuTuDatabase();
            thongBaoThanhCong("Thêm sản phẩm thành công", "Đã thêm mới sản phẩm " + product.getTenSanPham() + " (" + product.getMaSanPham() + ").");
        });
    }

    private void xuLySuaSanPham() {
        Product selected = tblProducts.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Optional<Product> updated = ProductEditorDialog.showAndEdit(getScene().getWindow(), selected);
        updated.ifPresent(product -> {
            taiDuLieuTuDatabase();
            thongBaoThanhCong("Cập nhật thành công", "Đã cập nhật thông tin sản phẩm " + product.getTenSanPham() + ".");
        });
    }

    private void xuLyNgungKinhDoanh() {
        Product selected = tblProducts.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác Nhận Ngừng Kinh Doanh");
        confirm.setHeaderText("Ngừng kinh doanh sản phẩm: " + selected.getTenSanPham());
        confirm.setContentText("Sản phẩm sẽ được chuyển sang trạng thái 'Ngừng kinh doanh' (xóa mềm). Sản phẩm sẽ không xuất hiện trên màn hình Bán hàng/Lập hóa đơn nhưng lịch sử giao dịch cũ vẫn được bảo lưu.\n\nBạn có chắc chắn muốn ngừng kinh doanh?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                productController.deactivateProduct(selected.getMaSanPham());
                selected.setTrangThai(false);
                tblProducts.refresh();
                if ("PRD_UPDATE".equalsIgnoreCase(currentActionId) || "PRD_CRUD".equalsIgnoreCase(currentActionId)) {
                    apDungBoLocCapNhat();
                }
                thongBaoThanhCong("Đã ngừng kinh doanh", "Sản phẩm " + selected.getTenSanPham() + " đã ngừng kinh doanh thành công.");
            } catch (Exception ex) {
                thongBaoLoi("Lỗi thao tác", ex.getMessage());
            }
        }
    }

    private void xuLyKichHoatLai() {
        Product selected = tblProducts.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            productController.activateProduct(selected.getMaSanPham());
            selected.setTrangThai(true);
            tblProducts.refresh();
            if ("PRD_UPDATE".equalsIgnoreCase(currentActionId) || "PRD_CRUD".equalsIgnoreCase(currentActionId)) {
                apDungBoLocCapNhat();
            }
            thongBaoThanhCong("Kích hoạt thành công", "Sản phẩm " + selected.getTenSanPham() + " đã được mở bán trở lại.");
        } catch (Exception ex) {
            thongBaoLoi("Lỗi thao tác", ex.getMessage());
        }
    }

    private void xuLyXemChiTiet() {
        Product selected = tblProducts.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ProductDetailDialog.showDetails(getScene().getWindow(), selected);
        }
    }

    private void xuLyDieuChinhTonKho() {
        Product selected = tblProducts.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Optional<Boolean> updated = ProductStockDialog.showAndAdjust(getScene().getWindow(), selected);
        updated.ifPresent(success -> {
            if (success) {
                tblProducts.refresh();
                if ("PRD_INVENTORY".equalsIgnoreCase(currentActionId)) {
                    apDungBoLocTonKho();
                } else if ("PRD_LOW_STOCK".equalsIgnoreCase(currentActionId) || "PRD_REPORT".equalsIgnoreCase(currentActionId)) {
                    apDungBoLocCanhBao();
                }
                thongBaoThanhCong("Điều chỉnh tồn kho thành công", "Đã cập nhật số lượng tồn mới: " + selected.getSoLuongTon() + " (" + selected.getDonViTinh() + ").");
            }
        });
    }

    private void thongBaoThanhCong(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void thongBaoLoi(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
