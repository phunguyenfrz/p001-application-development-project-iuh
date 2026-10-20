package com.petshop.view.importorder;

import com.petshop.controller.ImportOrderController;
import com.petshop.model.ImportOrder;
import com.petshop.model.ImportOrderItem;
import com.petshop.model.Product;
import com.petshop.model.Supplier;
import com.petshop.model.User;
import com.petshop.view.product.ProductPickerDialog;
import com.petshop.view.supplier.SupplierPickerDialog;
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

/**
 * Giao diện Quản lý Đơn hàng nhập (ImportOrderView).
 * Hiện thực đầy đủ 4 phân hệ chức năng theo tài liệu OOAD và PRD:
 * - IMP_CREATE: Lập đơn nhập hàng từ Nhà cung cấp.
 * - IMP_RECEIVE / IMP_CONFIRM: Xác nhận nhận hàng và tự động cập nhật tồn kho.
 * - IMP_UPDATE / IMP_CANCEL: Cập nhật thông tin và hủy đơn hàng (xóa mềm).
 * - IMP_SEARCH: Tìm kiếm đơn hàng nhập đa tiêu chí và tra cứu chi tiết.
 *
 * Tuân thủ Definition of Done:
 * - Điều khiển đặt tên tiền tố: btn, txt, lbl, tbl, dtp, cbo.
 * - Tên lớp là danh từ, tên phương thức là động từ; đầy đủ Javadoc/comment.
 * - 1 màn hình chỉ có 1 chức năng duy nhất, không dùng tab chuyển đổi ở trên đầu.
 * - Tất cả nút có setMinWidth(Region.USE_PREF_SIZE) để hiển thị đầy đủ tên, không co rút "...".
 * - Ngày tháng dùng DatePicker, ô số lượng và đơn giá chỉ cho nhập số.
 * - Xóa/hủy đơn hàng nhập thực hiện xóa mềm (chuyển sang trạng thái 'Đã hủy').
 */
public class ImportOrderView extends VBox {

    private final String currentActionId;
    private final User currentUser;
    private final ImportOrderController importOrderController = new ImportOrderController();
    private final ObservableList<ImportOrder> masterOrderList = FXCollections.observableArrayList();
    private final FilteredList<ImportOrder> filteredOrderList = new FilteredList<>(masterOrderList, o -> true);
    private final SimpleBooleanProperty isBusy = new SimpleBooleanProperty(false);
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0 đ");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Các điều khiển chung
    private Label lblTitle;
    private Label lblStatusCount;
    private TableView<ImportOrder> tblOrders;

    // Các điều khiển dành cho màn LẬP ĐƠN NHẬP (IMP_CREATE)
    private Supplier selectedSupplier;
    private TextField txtSupplierCode;
    private TextField txtSupplierName;
    private TextField txtSupplierPhone;
    private TextField txtOrderId;
    private TextField txtEmployeeInfo;
    private DatePicker dtpNgayLap;
    private TableView<ImportOrderItem> tblOrderItems;
    private final ObservableList<ImportOrderItem> currentDraftItems = FXCollections.observableArrayList();
    private Label lblTotalAmount;
    private Button btnPickSupplier;
    private Button btnAddItem;
    private Button btnEditItem;
    private Button btnDeleteItem;
    private Button btnSaveOrder;
    private Button btnResetOrder;

    // Các điều khiển dành cho NHẬN HÀNG, HỦY ĐƠN & TÌM KIẾM
    private TextField txtSearch;
    private ComboBox<String> cboStatusFilter;
    private DatePicker dtpFromDate;
    private DatePicker dtpToDate;
    private Button btnSearch;
    private Button btnRefresh;
    private Button btnViewDetail;
    private Button btnConfirmReceive;
    private Button btnCancelOrder;

    public ImportOrderView(String actionId) {
        this(actionId, null);
    }

    public ImportOrderView(String actionId, User user) {
        this.currentActionId = actionId != null ? actionId : "IMP_CREATE";
        this.currentUser = user;

        getStyleClass().addAll("customer-page", "import-order-page");
        setPadding(new Insets(24, 28, 24, 28));
        setSpacing(18);
        VBox.setVgrow(this, Priority.ALWAYS);

        // Khởi tạo đúng 1 màn hình độc lập tương ứng với chức năng được chọn
        if ("IMP_CREATE".equalsIgnoreCase(currentActionId)) {
            initializeCreateScreen();
        } else if ("IMP_RECEIVE".equalsIgnoreCase(currentActionId) || "IMP_CONFIRM".equalsIgnoreCase(currentActionId)) {
            initializeReceiveScreen();
            taiDanhSachDonHangTuDatabase();
        } else if ("IMP_UPDATE".equalsIgnoreCase(currentActionId) || "IMP_CANCEL".equalsIgnoreCase(currentActionId)) {
            initializeUpdateScreen();
            taiDanhSachDonHangTuDatabase();
        } else {
            initializeSearchScreen();
            taiDanhSachDonHangTuDatabase();
        }
    }

    // =========================================================================
    // 1. MÀN HÌNH LẬP ĐƠN NHẬP HÀNG (IMP_CREATE)
    // =========================================================================
    private void initializeCreateScreen() {
        lblTitle = new Label("Lập đơn hàng nhập");
        lblTitle.getStyleClass().add("customer-title");

        // 1. Khung thông tin chung (Nhà cung cấp & Đơn nhập)
        VBox pnlInfoCard = new VBox(12);
        pnlInfoCard.getStyleClass().add("customer-card");
        pnlInfoCard.setPadding(new Insets(16, 18, 16, 18));

        Label lblSectionInfo = new Label("1. Thông tin chung đơn nhập");
        lblSectionInfo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c6b50;");

        GridPane pnlGrid = new GridPane();
        pnlGrid.setHgap(14);
        pnlGrid.setVgap(12);

        ColumnConstraints c1 = new ColumnConstraints(120);
        ColumnConstraints c2 = new ColumnConstraints(220);
        ColumnConstraints c3 = new ColumnConstraints(130);
        ColumnConstraints c4 = new ColumnConstraints(220);
        ColumnConstraints c5 = new ColumnConstraints();
        c5.setHgrow(Priority.ALWAYS);
        pnlGrid.getColumnConstraints().addAll(c1, c2, c3, c4, c5);

        // Hàng 0: Mã đơn & Ngày lập
        Label lblOrderCode = new Label("Mã đơn nhập:");
        lblOrderCode.setStyle("-fx-font-weight: 600; -fx-text-fill: #4b5a50;");
        txtOrderId = new TextField(importOrderController.generateNextOrderId());
        txtOrderId.setEditable(false);
        txtOrderId.setStyle("-fx-background-color: #f0f2f0; -fx-border-color: #d8e2d8; -fx-border-radius: 6px; -fx-pref-height: 36px; -fx-font-weight: bold;");

        Label lblOrderDate = new Label("Ngày lập (*):");
        lblOrderDate.setStyle("-fx-font-weight: 600; -fx-text-fill: #4b5a50;");
        dtpNgayLap = new DatePicker(LocalDate.now());
        dtpNgayLap.setPrefHeight(36);
        dtpNgayLap.setMaxWidth(Double.MAX_VALUE);
        dtpNgayLap.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 6px;");

        Label lblEmp = new Label("Nhân viên lập:");
        lblEmp.setStyle("-fx-font-weight: 600; -fx-text-fill: #4b5a50;");
        String empCode = currentUser != null ? currentUser.getMaNhanVien() : "NV-20261005-000001";
        String empName = currentUser != null ? currentUser.getFullName() : "Quản trị hệ thống";
        txtEmployeeInfo = new TextField(empName + " (" + empCode + ")");
        txtEmployeeInfo.setEditable(false);
        txtEmployeeInfo.setStyle("-fx-background-color: #f0f2f0; -fx-border-color: #d8e2d8; -fx-border-radius: 6px; -fx-pref-height: 36px;");

        pnlGrid.add(lblOrderCode, 0, 0);
        pnlGrid.add(txtOrderId, 1, 0);
        pnlGrid.add(lblOrderDate, 2, 0);
        pnlGrid.add(dtpNgayLap, 3, 0);

        // Hàng 1: Nhà cung cấp
        Label lblSupplierTag = new Label("Nhà cung cấp (*):");
        lblSupplierTag.setStyle("-fx-font-weight: 600; -fx-text-fill: #4b5a50;");

        txtSupplierName = new TextField();
        txtSupplierName.setPromptText("Chưa chọn nhà cung cấp...");
        txtSupplierName.setEditable(false);
        txtSupplierName.setPrefHeight(36);
        txtSupplierName.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 6px;");

        btnPickSupplier = new Button("📋 Chọn nhà cung cấp");
        btnPickSupplier.getStyleClass().add("customer-secondary");
        btnPickSupplier.setMinWidth(Region.USE_PREF_SIZE);
        btnPickSupplier.setPrefHeight(36);
        btnPickSupplier.setOnAction(e -> xuLyChonNhaCungCap());

        txtSupplierPhone = new TextField();
        txtSupplierPhone.setPromptText("Số điện thoại NCC");
        txtSupplierPhone.setEditable(false);
        txtSupplierPhone.setPrefHeight(36);
        txtSupplierPhone.setStyle("-fx-background-color: #f0f2f0; -fx-border-color: #d8e2d8; -fx-border-radius: 6px;");

        HBox pnlSupplierRow = new HBox(8, txtSupplierName, btnPickSupplier);
        HBox.setHgrow(txtSupplierName, Priority.ALWAYS);

        pnlGrid.add(lblSupplierTag, 0, 1);
        pnlGrid.add(pnlSupplierRow, 1, 1, 2, 1);
        pnlGrid.add(lblEmp, 3, 1);
        pnlGrid.add(txtEmployeeInfo, 4, 1);

        pnlInfoCard.getChildren().addAll(lblSectionInfo, pnlGrid);

        // 2. Khung danh sách hàng hóa trong đơn nhập
        VBox pnlItemsCard = new VBox(12);
        pnlItemsCard.getStyleClass().add("customer-card");
        pnlItemsCard.setPadding(new Insets(16, 18, 16, 18));
        VBox.setVgrow(pnlItemsCard, Priority.ALWAYS);

        HBox pnlItemsToolbar = new HBox(12);
        pnlItemsToolbar.setAlignment(Pos.CENTER_LEFT);

        Label lblSectionItems = new Label("2. Danh sách mặt hàng nhập kho");
        lblSectionItems.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c6b50;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnAddItem = new Button("＋ Thêm sản phẩm");
        btnAddItem.getStyleClass().add("customer-add");
        btnAddItem.setMinWidth(Region.USE_PREF_SIZE);
        btnAddItem.setOnAction(e -> xuLyThemSanPhamVaoDon());

        btnEditItem = new Button("Sửa số lượng / giá");
        btnEditItem.getStyleClass().add("customer-secondary");
        btnEditItem.setMinWidth(Region.USE_PREF_SIZE);
        btnEditItem.setDisable(true);
        btnEditItem.setOnAction(e -> xuLySuaSanPhamTrongDon());

        btnDeleteItem = new Button("Xóa khỏi đơn");
        btnDeleteItem.getStyleClass().add("customer-danger");
        btnDeleteItem.setMinWidth(Region.USE_PREF_SIZE);
        btnDeleteItem.setDisable(true);
        btnDeleteItem.setOnAction(e -> xuLyXoaSanPhamKhoiDon());

        pnlItemsToolbar.getChildren().addAll(lblSectionItems, spacer, btnAddItem, btnEditItem, btnDeleteItem);

        tblOrderItems = buildDraftItemsTable();
        VBox.setVgrow(tblOrderItems, Priority.ALWAYS);

        tblOrderItems.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            boolean hasSelection = (n != null);
            btnEditItem.setDisable(!hasSelection);
            btnDeleteItem.setDisable(!hasSelection);
        });

        // 3. Footer tổng tiền và nút Lưu đơn
        HBox pnlActionFooter = new HBox(16);
        pnlActionFooter.setAlignment(Pos.CENTER_RIGHT);
        pnlActionFooter.setStyle("-fx-padding: 8px 0;");

        Label lblTotalTag = new Label("Tổng tiền đơn nhập:");
        lblTotalTag.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");

        lblTotalAmount = new Label("0 đ");
        lblTotalAmount.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #ff7518;");

        btnResetOrder = new Button("Nhập lại");
        btnResetOrder.getStyleClass().add("customer-secondary");
        btnResetOrder.setMinWidth(Region.USE_PREF_SIZE);
        btnResetOrder.setOnAction(e -> xuLyLamMoiDonDraft());

        btnSaveOrder = new Button("💾 Lưu đơn nhập hàng");
        btnSaveOrder.getStyleClass().add("customer-add");
        btnSaveOrder.setMinWidth(Region.USE_PREF_SIZE);
        btnSaveOrder.setStyle("-fx-font-size: 13.5px; -fx-padding: 10px 24px;");
        btnSaveOrder.setOnAction(e -> xuLyLuuDonNhap());

        pnlActionFooter.getChildren().addAll(lblTotalTag, lblTotalAmount, new Separator(), btnResetOrder, btnSaveOrder);

        pnlItemsCard.getChildren().addAll(pnlItemsToolbar, tblOrderItems, pnlActionFooter);

        getChildren().addAll(lblTitle, pnlInfoCard, pnlItemsCard);
    }

    private TableView<ImportOrderItem> buildDraftItemsTable() {
        TableView<ImportOrderItem> table = new TableView<>();
        table.setItems(currentDraftItems);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ImportOrderItem, Number> colIndex = new TableColumn<>("STT");
        colIndex.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(table.getItems().indexOf(data.getValue()) + 1));
        colIndex.setPrefWidth(50);
        colIndex.setStyle("-fx-alignment: CENTER;");

        TableColumn<ImportOrderItem, String> colCode = new TableColumn<>("Mã sản phẩm");
        colCode.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getMaSanPham()));
        colCode.setPrefWidth(130);

        TableColumn<ImportOrderItem, String> colName = new TableColumn<>("Tên sản phẩm");
        colName.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTenSanPham()));
        colName.setPrefWidth(260);

        TableColumn<ImportOrderItem, String> colUnit = new TableColumn<>("ĐVT");
        colUnit.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getDonViTinh()));
        colUnit.setPrefWidth(70);
        colUnit.setStyle("-fx-alignment: CENTER;");

        TableColumn<ImportOrderItem, String> colQty = new TableColumn<>("Số lượng nhập");
        colQty.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().getSoLuong())));
        colQty.setPrefWidth(110);
        colQty.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        TableColumn<ImportOrderItem, String> colPrice = new TableColumn<>("Đơn giá nhập");
        colPrice.setCellValueFactory(data -> new ReadOnlyStringWrapper(currencyFormat.format(data.getValue().getDonGia())));
        colPrice.setPrefWidth(125);
        colPrice.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<ImportOrderItem, String> colTotal = new TableColumn<>("Thành tiền");
        colTotal.setCellValueFactory(data -> new ReadOnlyStringWrapper(currencyFormat.format(data.getValue().getThanhTien())));
        colTotal.setPrefWidth(140);
        colTotal.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: #2c6b50;");

        table.getColumns().addAll(List.of(colIndex, colCode, colName, colUnit, colQty, colPrice, colTotal));

        table.setRowFactory(tv -> {
            TableRow<ImportOrderItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty() && event.getButton() == MouseButton.PRIMARY) {
                    xuLySuaSanPhamTrongDon();
                }
            });
            return row;
        });

        table.setPlaceholder(new Label("Chưa có mặt hàng nào trong đơn nhập. Bấm '＋ Thêm sản phẩm' để bắt đầu."));
        return table;
    }

    private void tinhLaiTongTienDraft() {
        double total = 0;
        for (ImportOrderItem item : currentDraftItems) {
            total += item.getThanhTien();
        }
        lblTotalAmount.setText(currencyFormat.format(total));
    }

    private void xuLyChonNhaCungCap() {
        Optional<Supplier> picked = SupplierPickerDialog.showAndPick(getScene().getWindow());
        picked.ifPresent(supplier -> {
            this.selectedSupplier = supplier;
            txtSupplierName.setText(supplier.getTenNhaCungCap() + " (" + supplier.getMaNhaCungCap() + ")");
            txtSupplierPhone.setText(supplier.getSoDienThoai());
        });
    }

    private void xuLyThemSanPhamVaoDon() {
        Optional<Product> picked = ProductPickerDialog.showAndPick(getScene().getWindow());
        picked.ifPresent(product -> {
            // Kiểm tra xem sản phẩm đã có trong bảng nháp chưa
            for (ImportOrderItem item : currentDraftItems) {
                if (item.getMaSanPham().equals(product.getMaSanPham())) {
                    thongBaoCanhBao("Sản phẩm đã tồn tại", "Sản phẩm " + product.getTenSanPham() + " đã có trong đơn nhập. Bạn có thể chọn dòng và bấm 'Sửa số lượng / giá'.");
                    tblOrderItems.getSelectionModel().select(item);
                    return;
                }
            }

            // Mở dialog nhập số lượng và đơn giá
            Optional<ImportOrderItem> itemResult = ImportOrderItemDialog.showAndInput(getScene().getWindow(), product, null);
            itemResult.ifPresent(newItem -> {
                newItem.setMaDonHang(txtOrderId.getText());
                currentDraftItems.add(newItem);
                tinhLaiTongTienDraft();
            });
        });
    }

    private void xuLySuaSanPhamTrongDon() {
        ImportOrderItem selected = tblOrderItems.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Optional<ImportOrderItem> updated = ImportOrderItemDialog.showAndInput(getScene().getWindow(), null, selected);
        updated.ifPresent(item -> {
            selected.setSoLuong(item.getSoLuong());
            selected.setDonGia(item.getDonGia());
            tblOrderItems.refresh();
            tinhLaiTongTienDraft();
        });
    }

    private void xuLyXoaSanPhamKhoiDon() {
        ImportOrderItem selected = tblOrderItems.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        currentDraftItems.remove(selected);
        tinhLaiTongTienDraft();
    }

    private void xuLyLamMoiDonDraft() {
        selectedSupplier = null;
        txtSupplierName.clear();
        txtSupplierPhone.clear();
        txtOrderId.setText(importOrderController.generateNextOrderId());
        dtpNgayLap.setValue(LocalDate.now());
        currentDraftItems.clear();
        tinhLaiTongTienDraft();
    }

    private void xuLyLuuDonNhap() {
        if (selectedSupplier == null) {
            thongBaoLoi("Thiếu thông tin", "Vui lòng chọn Nhà cung cấp đối tác trước khi lưu đơn.");
            btnPickSupplier.requestFocus();
            return;
        }

        if (currentDraftItems.isEmpty()) {
            thongBaoLoi("Thiếu mặt hàng", "Đơn hàng nhập phải có ít nhất một mặt hàng sản phẩm.");
            btnAddItem.requestFocus();
            return;
        }

        LocalDate ngayLap = dtpNgayLap.getValue();
        if (ngayLap == null) {
            thongBaoLoi("Thiếu ngày lập", "Vui lòng chọn ngày lập đơn hàng nhập.");
            dtpNgayLap.requestFocus();
            return;
        }

        String empCode = currentUser != null ? currentUser.getMaNhanVien() : "NV-20261005-000001";
        String orderId = txtOrderId.getText().trim();

        ImportOrder order = new ImportOrder();
        order.setMaDonHang(orderId);
        order.setMaNhaCungCap(selectedSupplier.getMaNhaCungCap());
        order.setMaNhanVien(empCode);
        order.setNgayLap(ngayLap);
        order.setTrangThai(ImportOrder.TRANG_THAI_CHO_NHAN);
        order.setItems(new ArrayList<>(currentDraftItems));
        order.tinhTongTien();

        try {
            importOrderController.createImportOrder(order);
            thongBaoThanhCong("Lập đơn nhập thành công",
                    "Đã tạo thành công đơn hàng nhập " + order.getMaDonHang() + " với tổng tiền: " + currencyFormat.format(order.getTongTien()) + ".\nTrạng thái đơn: 'Chờ nhận hàng'.");
            xuLyLamMoiDonDraft();
        } catch (Exception ex) {
            thongBaoLoi("Lỗi khi lưu đơn nhập", ex.getMessage());
        }
    }

    // =========================================================================
    // 2. MÀN HÌNH NHẬN HÀNG & CẬP NHẬT TỒN KHO (IMP_RECEIVE / IMP_CONFIRM)
    // =========================================================================
    private void initializeReceiveScreen() {
        lblTitle = new Label("Xác nhận nhận hàng & Nhập kho");
        lblTitle.getStyleClass().add("customer-title");

        HBox pnlHeader = new HBox(12);
        pnlHeader.setAlignment(Pos.CENTER_LEFT);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        btnConfirmReceive = new Button("✓ Xác nhận nhận hàng");
        btnConfirmReceive.getStyleClass().add("customer-add");
        btnConfirmReceive.setMinWidth(Region.USE_PREF_SIZE);
        btnConfirmReceive.setDisable(true);
        btnConfirmReceive.setOnAction(e -> xuLyXacNhanNhanHang());

        btnViewDetail = new Button("Xem chi tiết đơn");
        btnViewDetail.getStyleClass().add("customer-secondary");
        btnViewDetail.setMinWidth(Region.USE_PREF_SIZE);
        btnViewDetail.setDisable(true);
        btnViewDetail.setOnAction(e -> xuLyXemChiTietDon());

        btnRefresh = new Button("Làm mới");
        btnRefresh.getStyleClass().add("customer-secondary");
        btnRefresh.setMinWidth(Region.USE_PREF_SIZE);
        btnRefresh.setOnAction(e -> taiDanhSachDonHangTuDatabase());

        pnlHeader.getChildren().addAll(lblTitle, headerSpacer, btnConfirmReceive, btnViewDetail, btnRefresh);

        // Thanh tìm kiếm nhanh
        HBox pnlToolbar = new HBox(10);
        pnlToolbar.setAlignment(Pos.CENTER_LEFT);

        txtSearch = new TextField();
        txtSearch.setPromptText("Tìm kiếm theo mã đơn hoặc nhà cung cấp...");
        txtSearch.setPrefHeight(38);
        txtSearch.setPrefWidth(350);
        txtSearch.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8px; -fx-border-color: #d1ded1; -fx-border-radius: 8px; -fx-padding: 0 12px;");

        PauseTransition debounce = new PauseTransition(Duration.millis(150));
        debounce.setOnFinished(e -> apDungBoLocHienThi());
        txtSearch.textProperty().addListener((obs, o, n) -> debounce.playFromStart());

        Label lblNote = new Label("💡 Chỉ các đơn hàng ở trạng thái 'Chờ nhận hàng' mới có thể xác nhận để cộng số lượng vào tồn kho.");
        lblNote.setStyle("-fx-font-size: 12px; -fx-text-fill: #2c6b50; -fx-font-style: italic;");

        pnlToolbar.getChildren().addAll(txtSearch, lblNote);

        tblOrders = buildOrdersTable();
        VBox.setVgrow(tblOrders, Priority.ALWAYS);

        VBox pnlTableCard = createTableCard(tblOrders);

        tblOrders.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            boolean hasSelection = (n != null);
            btnViewDetail.setDisable(!hasSelection);
            btnConfirmReceive.setDisable(!hasSelection || !n.isChoNhanHang());
        });

        getChildren().addAll(pnlHeader, pnlToolbar, pnlTableCard);
    }

    private void xuLyXacNhanNhanHang() {
        ImportOrder selected = tblOrders.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác Nhận Nhận Hàng Nhập Kho");
        confirm.setHeaderText("Xác nhận nhập kho cho đơn hàng: " + selected.getMaDonHang());
        confirm.setContentText("Bạn có chắc chắn đã kiểm nhận đủ hàng hóa từ Nhà cung cấp?\n\nSau khi xác nhận:\n- Trạng thái đơn chuyển sang 'Hoàn tất'.\n- Số lượng của toàn bộ sản phẩm trong đơn sẽ tự động được cộng dồn vào tồn kho cửa hàng.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                importOrderController.confirmReceiveOrder(selected.getMaDonHang());
                thongBaoThanhCong("Nhận hàng thành công", "Đơn hàng " + selected.getMaDonHang() + " đã hoàn tất và số lượng tồn kho đã được cập nhật thành công.");
                taiDanhSachDonHangTuDatabase();
            } catch (Exception ex) {
                thongBaoLoi("Lỗi xác nhận nhận hàng", ex.getMessage());
            }
        }
    }

    // =========================================================================
    // 3. MÀN HÌNH CẬP NHẬT & HỦY ĐƠN HÀNG NHẬP (IMP_UPDATE / IMP_CANCEL)
    // =========================================================================
    private void initializeUpdateScreen() {
        lblTitle = new Label("Cập nhật & Hủy đơn hàng nhập");
        lblTitle.getStyleClass().add("customer-title");

        HBox pnlHeader = new HBox(12);
        pnlHeader.setAlignment(Pos.CENTER_LEFT);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        btnCancelOrder = new Button("✕ Hủy đơn nhập");
        btnCancelOrder.getStyleClass().add("customer-danger");
        btnCancelOrder.setMinWidth(Region.USE_PREF_SIZE);
        btnCancelOrder.setDisable(true);
        btnCancelOrder.setOnAction(e -> xuLyHuyDonNhap());

        btnViewDetail = new Button("Xem chi tiết đơn");
        btnViewDetail.getStyleClass().add("customer-secondary");
        btnViewDetail.setMinWidth(Region.USE_PREF_SIZE);
        btnViewDetail.setDisable(true);
        btnViewDetail.setOnAction(e -> xuLyXemChiTietDon());

        btnRefresh = new Button("Làm mới");
        btnRefresh.getStyleClass().add("customer-secondary");
        btnRefresh.setMinWidth(Region.USE_PREF_SIZE);
        btnRefresh.setOnAction(e -> taiDanhSachDonHangTuDatabase());

        pnlHeader.getChildren().addAll(lblTitle, headerSpacer, btnCancelOrder, btnViewDetail, btnRefresh);

        // Thanh lọc nhanh
        HBox pnlToolbar = new HBox(10);
        pnlToolbar.setAlignment(Pos.CENTER_LEFT);

        txtSearch = new TextField();
        txtSearch.setPromptText("Tìm kiếm đơn theo mã đơn, nhà cung cấp...");
        txtSearch.setPrefHeight(38);
        txtSearch.setPrefWidth(350);
        txtSearch.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8px; -fx-border-color: #d1ded1; -fx-border-radius: 8px; -fx-padding: 0 12px;");

        PauseTransition debounce = new PauseTransition(Duration.millis(150));
        debounce.setOnFinished(e -> apDungBoLocHienThi());
        txtSearch.textProperty().addListener((obs, o, n) -> debounce.playFromStart());

        Label lblNote = new Label("⚠ Thao tác 'Hủy đơn' thực hiện xóa mềm (chuyển sang 'Đã hủy') và chỉ áp dụng cho đơn 'Chờ nhận hàng'.");
        lblNote.setStyle("-fx-font-size: 12px; -fx-text-fill: #dc2626; -fx-font-style: italic;");

        pnlToolbar.getChildren().addAll(txtSearch, lblNote);

        tblOrders = buildOrdersTable();
        VBox.setVgrow(tblOrders, Priority.ALWAYS);

        VBox pnlTableCard = createTableCard(tblOrders);

        tblOrders.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            boolean hasSelection = (n != null);
            btnViewDetail.setDisable(!hasSelection);
            btnCancelOrder.setDisable(!hasSelection || !n.isChoNhanHang());
        });

        getChildren().addAll(pnlHeader, pnlToolbar, pnlTableCard);
    }

    private void xuLyHuyDonNhap() {
        ImportOrder selected = tblOrders.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác Nhận Hủy Đơn Hàng Nhập");
        confirm.setHeaderText("Hủy đơn hàng nhập: " + selected.getMaDonHang());
        confirm.setContentText("Bạn có chắc chắn muốn hủy đơn hàng này không?\n\nThao tác này là xóa mềm: Đơn hàng sẽ được chuyển sang trạng thái 'Đã hủy' và lưu vết lịch sử theo chuẩn OOAD.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                importOrderController.cancelImportOrder(selected.getMaDonHang());
                thongBaoThanhCong("Đã hủy đơn nhập", "Đơn hàng nhập " + selected.getMaDonHang() + " đã được chuyển sang trạng thái 'Đã hủy'.");
                taiDanhSachDonHangTuDatabase();
            } catch (Exception ex) {
                thongBaoLoi("Lỗi khi hủy đơn", ex.getMessage());
            }
        }
    }

    // =========================================================================
    // 4. MÀN HÌNH TÌM KIẾM ĐƠN HÀNG NHẬP (IMP_SEARCH)
    // =========================================================================
    private void initializeSearchScreen() {
        lblTitle = new Label("Tìm kiếm đơn hàng nhập");
        lblTitle.getStyleClass().add("customer-title");

        HBox pnlSearchToolbar = new HBox(10);
        pnlSearchToolbar.setAlignment(Pos.CENTER_LEFT);

        txtSearch = new TextField();
        txtSearch.setPromptText("Mã đơn, tên NCC, nhân viên...");
        txtSearch.setPrefHeight(38);
        txtSearch.setPrefWidth(260);
        txtSearch.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8px; -fx-border-color: #d1ded1; -fx-border-radius: 8px; -fx-padding: 0 12px;");
        txtSearch.setOnAction(e -> apDungBoLocHienThi());

        cboStatusFilter = new ComboBox<>(FXCollections.observableArrayList(
                "Tất cả trạng thái", "Chờ nhận hàng", "Hoàn tất", "Đã hủy"
        ));
        cboStatusFilter.setValue("Tất cả trạng thái");
        cboStatusFilter.setPrefHeight(38);
        cboStatusFilter.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 8px;");
        cboStatusFilter.setOnAction(e -> apDungBoLocHienThi());

        Label lblFrom = new Label("Từ:");
        lblFrom.setStyle("-fx-font-weight: 600; -fx-text-fill: #4b5a50;");
        dtpFromDate = new DatePicker();
        dtpFromDate.setPrefHeight(38);
        dtpFromDate.setPrefWidth(130);
        dtpFromDate.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 8px;");
        dtpFromDate.setOnAction(e -> apDungBoLocHienThi());

        Label lblTo = new Label("Đến:");
        lblTo.setStyle("-fx-font-weight: 600; -fx-text-fill: #4b5a50;");
        dtpToDate = new DatePicker();
        dtpToDate.setPrefHeight(38);
        dtpToDate.setPrefWidth(130);
        dtpToDate.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 8px;");
        dtpToDate.setOnAction(e -> apDungBoLocHienThi());

        btnSearch = new Button("Tìm kiếm");
        btnSearch.getStyleClass().add("customer-save");
        btnSearch.setMinWidth(Region.USE_PREF_SIZE);
        btnSearch.setPrefHeight(38);
        btnSearch.setOnAction(e -> apDungBoLocHienThi());

        btnRefresh = new Button("Làm mới");
        btnRefresh.getStyleClass().add("customer-secondary");
        btnRefresh.setMinWidth(Region.USE_PREF_SIZE);
        btnRefresh.setPrefHeight(38);
        btnRefresh.setOnAction(e -> {
            txtSearch.clear();
            cboStatusFilter.setValue("Tất cả trạng thái");
            dtpFromDate.setValue(null);
            dtpToDate.setValue(null);
            taiDanhSachDonHangTuDatabase();
        });

        btnViewDetail = new Button("Xem chi tiết");
        btnViewDetail.getStyleClass().add("customer-secondary");
        btnViewDetail.setMinWidth(Region.USE_PREF_SIZE);
        btnViewDetail.setPrefHeight(38);
        btnViewDetail.setDisable(true);
        btnViewDetail.setOnAction(e -> xuLyXemChiTietDon());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        pnlSearchToolbar.getChildren().addAll(
                txtSearch, cboStatusFilter,
                lblFrom, dtpFromDate, lblTo, dtpToDate,
                btnSearch, btnRefresh, spacer, btnViewDetail
        );

        tblOrders = buildOrdersTable();
        VBox.setVgrow(tblOrders, Priority.ALWAYS);

        VBox pnlTableCard = createTableCard(tblOrders);

        tblOrders.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            btnViewDetail.setDisable(n == null);
        });

        getChildren().addAll(lblTitle, pnlSearchToolbar, pnlTableCard);
    }

    // =========================================================================
    // DỰNG BẢNG HIỂN THỊ ĐƠN HÀNG NHẬP
    // =========================================================================
    private TableView<ImportOrder> buildOrdersTable() {
        TableView<ImportOrder> table = new TableView<>();
        table.setItems(filteredOrderList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ImportOrder, String> colCode = new TableColumn<>("Mã đơn nhập");
        colCode.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getMaDonHang()));
        colCode.setPrefWidth(140);
        colCode.setStyle("-fx-font-weight: bold; -fx-alignment: CENTER-LEFT;");

        TableColumn<ImportOrder, String> colSupplier = new TableColumn<>("Nhà cung cấp");
        colSupplier.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getTenNhaCungCap() != null ? data.getValue().getTenNhaCungCap() : data.getValue().getMaNhaCungCap()
        ));
        colSupplier.setPrefWidth(240);

        TableColumn<ImportOrder, String> colDate = new TableColumn<>("Ngày lập");
        colDate.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getNgayLap() != null ? data.getValue().getNgayLap().format(dateFormatter) : ""
        ));
        colDate.setPrefWidth(100);
        colDate.setStyle("-fx-alignment: CENTER;");

        TableColumn<ImportOrder, String> colTotal = new TableColumn<>("Tổng tiền (VNĐ)");
        colTotal.setCellValueFactory(data -> new ReadOnlyStringWrapper(currencyFormat.format(data.getValue().getTongTien())));
        colTotal.setPrefWidth(130);
        colTotal.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");

        TableColumn<ImportOrder, String> colEmployee = new TableColumn<>("Nhân viên lập");
        colEmployee.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getTenNhanVien() != null ? data.getValue().getTenNhanVien() : data.getValue().getMaNhanVien()
        ));
        colEmployee.setPrefWidth(160);

        TableColumn<ImportOrder, String> colStatus = new TableColumn<>("Trạng thái");
        colStatus.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTrangThai()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label lbl = new Label(item);
                    lbl.setStyle("-fx-padding: 3px 10px; -fx-background-radius: 10px; -fx-font-weight: bold; -fx-font-size: 11.5px;");
                    if (ImportOrder.TRANG_THAI_HOAN_TAT.equalsIgnoreCase(item)) {
                        lbl.setStyle(lbl.getStyle() + "-fx-background-color: #dcfce7; -fx-text-fill: #15803d;");
                    } else if (ImportOrder.TRANG_THAI_DA_HUY.equalsIgnoreCase(item)) {
                        lbl.setStyle(lbl.getStyle() + "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626;");
                    } else {
                        lbl.setStyle(lbl.getStyle() + "-fx-background-color: #fef3c7; -fx-text-fill: #b45309;");
                    }
                    setGraphic(lbl);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        colStatus.setPrefWidth(125);

        table.getColumns().addAll(List.of(colCode, colSupplier, colDate, colTotal, colEmployee, colStatus));

        table.setRowFactory(tv -> {
            TableRow<ImportOrder> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty() && event.getButton() == MouseButton.PRIMARY) {
                    xuLyXemChiTietDon();
                }
            });
            return row;
        });

        table.setPlaceholder(new Label("Không tìm thấy đơn hàng nhập phù hợp."));
        return table;
    }

    private VBox createTableCard(TableView<?> table) {
        VBox card = new VBox(10);
        card.getStyleClass().add("customer-card");
        card.setPadding(new Insets(16, 18, 16, 18));
        VBox.setVgrow(card, Priority.ALWAYS);

        lblStatusCount = new Label("Đang tải dữ liệu...");
        lblStatusCount.setStyle("-fx-font-size: 12px; -fx-text-fill: #738075; -fx-font-style: italic;");

        card.getChildren().addAll(table, lblStatusCount);
        return card;
    }

    private void xuLyXemChiTietDon() {
        ImportOrder selected = tblOrders.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        // Tải danh sách mặt hàng nếu chưa có
        Optional<ImportOrder> fullOrder = importOrderController.getOrderById(selected.getMaDonHang());
        ImportOrderDetailDialog.showDetails(getScene().getWindow(), fullOrder.orElse(selected));
    }

    private void apDungBoLocHienThi() {
        String keyword = txtSearch != null && txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";
        String statusFilter = cboStatusFilter != null ? cboStatusFilter.getValue() : null;
        LocalDate fromDate = dtpFromDate != null ? dtpFromDate.getValue() : null;
        LocalDate toDate = dtpToDate != null ? dtpToDate.getValue() : null;

        filteredOrderList.setPredicate(order -> {
            // 1. Lọc theo màn hình
            if ("IMP_RECEIVE".equalsIgnoreCase(currentActionId) || "IMP_CONFIRM".equalsIgnoreCase(currentActionId)) {
                // Màn nhận hàng chỉ hiển thị đơn Chờ nhận hàng
                if (!order.isChoNhanHang()) return false;
            }

            // 2. Lọc theo trạng thái
            if (statusFilter != null && !"Tất cả trạng thái".equals(statusFilter) && !"Tất cả".equals(statusFilter)) {
                if (!statusFilter.equalsIgnoreCase(order.getTrangThai())) return false;
            }

            // 3. Lọc theo ngày
            if (fromDate != null && order.getNgayLap() != null && order.getNgayLap().isBefore(fromDate)) {
                return false;
            }
            if (toDate != null && order.getNgayLap() != null && order.getNgayLap().isAfter(toDate)) {
                return false;
            }

            // 4. Lọc theo từ khóa
            if (!keyword.isEmpty()) {
                boolean matchId = order.getMaDonHang() != null && order.getMaDonHang().toLowerCase().contains(keyword);
                boolean matchNcc = order.getTenNhaCungCap() != null && order.getTenNhaCungCap().toLowerCase().contains(keyword);
                boolean matchEmp = order.getTenNhanVien() != null && order.getTenNhanVien().toLowerCase().contains(keyword);
                return matchId || matchNcc || matchEmp;
            }

            return true;
        });

        if (lblStatusCount != null) {
            lblStatusCount.setText(String.format("Hiển thị %d / tổng số %d đơn hàng nhập.", filteredOrderList.size(), masterOrderList.size()));
        }
    }

    private void taiDanhSachDonHangTuDatabase() {
        isBusy.set(true);
        if (lblStatusCount != null) {
            lblStatusCount.setText("Đang tải dữ liệu đơn hàng nhập từ CSDL...");
        }

        CompletableFuture.supplyAsync(importOrderController::getAllImportOrders)
                .thenAccept(orders -> Platform.runLater(() -> {
                    masterOrderList.setAll(orders);
                    isBusy.set(false);
                    apDungBoLocHienThi();
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

    private void thongBaoThanhCong(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void thongBaoCanhBao(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
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

