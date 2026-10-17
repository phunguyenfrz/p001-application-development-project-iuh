package com.petshop.view;

import com.petshop.controller.SupplierController;
import com.petshop.model.Supplier;
import com.petshop.service.SupplierService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Màn hình Nhà cung cấp tách biệt rõ 2 chức năng độc lập theo đúng chuẩn PTUD:
 * 1. SUP_UPDATE (Cập nhật nhà cung cấp):
 *    - Chỉ phục vụ Thêm mới, Xem/Sửa, Xóa mềm (KHÔNG có thanh tìm kiếm).
 * 2. SUP_SEARCH (Tìm kiếm nhà cung cấp):
 *    - Chỉ phục vụ Tìm kiếm & Lọc tra cứu nhà cung cấp (KHÔNG có nút Thêm, Sửa, Xóa).
 */
public class SupplierView extends StackPane {

    private static final int PAGE_SIZE = 3;

    private final SupplierController supplierController;
    private final boolean isSearchMode;

    // Danh sách dữ liệu gốc sau khi lọc và danh sách hiển thị theo trang
    private final List<Supplier> allFilteredSuppliers = new ArrayList<>();
    private final ObservableList<Supplier> pageSuppliers = FXCollections.observableArrayList();
    private final AtomicLong searchRequestCounter = new AtomicLong(0);
    private PauseTransition searchDebounce;
    private int currentPage = 1;

    // Các điều khiển giao diện tuân thủ chuẩn tiền tố (txt, cbo, btn, tbl, lbl, pnl)
    private VBox pnlMainContent;
    private StackPane pnlModalOverlay;
    private TextField txtSearch;
    private ComboBox<String> cboStatusFilter;
    private Button btnResetSearch;
    private Button btnAddSupplier;
    private TableView<Supplier> tblSuppliers;
    private Label lblPaginationInfo;
    private Button btnPrevPage;
    private Button btnNextPage;
    private HBox pnlPageNumbers;

    // Các điều khiển trong Modal Nhà cung cấp
    private TextField txtTenNhaCungCap;
    private TextField txtSoDienThoai;
    private TextField txtEmail;
    private TextField txtDiaChi;
    private TextField txtMatHangCungCap;
    private Label lblFormError;

    /**
     * Khởi tạo màn hình Nhà cung cấp theo mã chức năng.
     *
     * @param actionId Mã chức năng ("SUP_UPDATE" hoặc "SUP_SEARCH")
     */
    public SupplierView(String actionId) {
        this.supplierController = new SupplierController();
        this.isSearchMode = "SUP_SEARCH".equals(actionId);

        setStyle("-fx-background-color: #f2f2ed;");

        pnlMainContent = new VBox(18);
        pnlMainContent.setPadding(new Insets(24, 28, 28, 28));
        pnlMainContent.setAlignment(Pos.TOP_LEFT);

        buildHeaderSection();
        buildToolbarSection();
        buildTableCardSection();

        // Lớp phủ Modal (09 · Thêm / Sửa NCC) mặc định ẩn
        pnlModalOverlay = new StackPane();
        pnlModalOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.45);");
        pnlModalOverlay.setVisible(false);
        pnlModalOverlay.setManaged(false);
        pnlModalOverlay.setOnMouseClicked(e -> {
            if (e.getTarget() == pnlModalOverlay) {
                closeModalOverlay();
            }
        });

        getChildren().addAll(pnlMainContent, pnlModalOverlay);

        registerKeyboardShortcuts();
        applySearchAndFilter();

        // Nếu mở màn "Tìm kiếm nhà cung cấp" (SUP_SEARCH), tự động đưa con trỏ vào ô tìm kiếm
        if (isSearchMode && txtSearch != null) {
            Platform.runLater(() -> txtSearch.requestFocus());
        }
    }

    /**
     * Dựng phần tiêu đề và đường dẫn (Breadcrumb) theo đúng chức năng đang mở.
     */
    private void buildHeaderSection() {
        VBox pnlHeader = new VBox(4);

        Label lblBreadcrumb = new Label(
                isSearchMode
                        ? "Tổng quan / Nhà cung cấp / Tìm kiếm nhà cung cấp"
                        : "Tổng quan / Nhà cung cấp / Cập nhật nhà cung cấp"
        );
        lblBreadcrumb.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #8a8e87;");

        Label lblTitle = new Label(isSearchMode ? "Tìm kiếm nhà cung cấp" : "Nhà cung cấp");
        lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1b211c;");

        Label lblSubtitle = new Label(
                isSearchMode
                        ? "Tra cứu thông tin đối tác cung cấp sản phẩm theo mã, tên, số điện thoại, địa chỉ."
                        : "Đối tác cung cấp sản phẩm cho Happy Tails."
        );
        lblSubtitle.setStyle("-fx-font-size: 13.5px; -fx-text-fill: #787d76;");

        pnlHeader.getChildren().addAll(lblBreadcrumb, lblTitle, lblSubtitle);
        pnlMainContent.getChildren().add(pnlHeader);
    }

    /**
     * Dựng thanh công cụ tách biệt hoàn toàn giữa 2 màn hình:
     * - Màn Cập nhật (SUP_UPDATE): KHÔNG có thanh tìm kiếm, chỉ có nút "+ Thêm nhà cung cấp".
     * - Màn Tìm kiếm (SUP_SEARCH): CHỈ có thanh tìm kiếm & bộ lọc trạng thái, KHÔNG có nút "+ Thêm nhà cung cấp".
     */
    private void buildToolbarSection() {
        HBox pnlToolbar = new HBox(12);
        pnlToolbar.setAlignment(Pos.CENTER_LEFT);

        if (isSearchMode) {
            // === CHẾ ĐỘ TÌM KIẾM (SUP_SEARCH): Chỉ có công cụ Tìm kiếm & Lọc, KHÔNG có nút Thêm ===
            searchDebounce = new PauseTransition(Duration.millis(150));
            searchDebounce.setOnFinished(e -> applySearchAndFilter());

            Label lblSearchIcon = new Label("🔍");
            lblSearchIcon.setStyle("-fx-text-fill: #8d918a; -fx-font-size: 13px;");

            txtSearch = new TextField();
            txtSearch.setPromptText("Tìm theo tên, mã NCC, số điện thoại, mặt hàng...");
            txtSearch.setStyle(
                    "-fx-background-color: transparent; -fx-border-color: transparent; " +
                    "-fx-font-size: 13px; -fx-text-fill: #1f2420; -fx-prompt-text-fill: #989c94; -fx-padding: 0;"
            );
            txtSearch.setPrefWidth(320);
            txtSearch.textProperty().addListener((obs, oldVal, newVal) -> searchDebounce.playFromStart());

            HBox pnlSearchBox = new HBox(8, lblSearchIcon, txtSearch);
            pnlSearchBox.setAlignment(Pos.CENTER_LEFT);
            pnlSearchBox.setPadding(new Insets(0, 14, 0, 14));
            pnlSearchBox.setPrefHeight(40);
            pnlSearchBox.setMaxHeight(40);
            pnlSearchBox.setStyle(
                    "-fx-background-color: #ffffff; -fx-background-radius: 10px; " +
                    "-fx-border-color: #e5e7e2; -fx-border-radius: 10px; -fx-border-width: 1px;"
            );

            cboStatusFilter = new ComboBox<>();
            cboStatusFilter.getItems().addAll("Tất cả trạng thái", "Đang hợp tác", "Ngừng hoạt động");
            cboStatusFilter.setValue("Tất cả trạng thái");
            cboStatusFilter.setPrefHeight(40);
            cboStatusFilter.setStyle(
                    "-fx-background-color: #ffffff; -fx-background-radius: 10px; " +
                    "-fx-border-color: #e5e7e2; -fx-border-radius: 10px; -fx-font-size: 13px;"
            );
            cboStatusFilter.setOnAction(e -> applySearchAndFilter());

            btnResetSearch = new Button("Đặt lại");
            btnResetSearch.setStyle(
                    "-fx-background-color: #ffffff; -fx-border-color: #dcded8; -fx-border-radius: 10px; " +
                    "-fx-background-radius: 10px; -fx-text-fill: #2b312c; -fx-font-size: 13px; " +
                    "-fx-font-weight: bold; -fx-padding: 0 16px; -fx-pref-height: 40px; -fx-cursor: hand;"
            );
            btnResetSearch.setOnAction(e -> {
                txtSearch.clear();
                cboStatusFilter.setValue("Tất cả trạng thái");
                applySearchAndFilter();
            });

            pnlToolbar.getChildren().addAll(pnlSearchBox, cboStatusFilter, btnResetSearch);
        } else {
            // === CHẾ ĐỘ CẬP NHẬT (SUP_UPDATE): KHÔNG có thanh tìm kiếm, chỉ có nút "+ Thêm nhà cung cấp" ===
            Region pnlSpacer = new Region();
            HBox.setHgrow(pnlSpacer, Priority.ALWAYS);

            btnAddSupplier = new Button("+ Thêm nhà cung cấp");
            btnAddSupplier.setStyle(
                    "-fx-background-color: #3f6148; -fx-text-fill: #ffffff; -fx-font-size: 13px; " +
                    "-fx-font-weight: bold; -fx-padding: 0 20px; -fx-pref-height: 40px; " +
                    "-fx-background-radius: 10px; -fx-cursor: hand;"
            );
            btnAddSupplier.setOnAction(e -> showAddSupplierModal());

            pnlToolbar.getChildren().addAll(pnlSpacer, btnAddSupplier);
        }

        pnlMainContent.getChildren().add(pnlToolbar);
    }

    /**
     * Dựng thẻ trắng chứa bảng danh sách nhà cung cấp và thanh phân trang ở đáy chuẩn Figma.
     * - Ở màn SUP_UPDATE: có cột THAO TÁC (Xem/Sửa và Xóa).
     * - Ở màn SUP_SEARCH: KHÔNG có cột cập nhật/xóa, chỉ hiển thị thông tin tra cứu.
     */
    private void buildTableCardSection() {
        VBox pnlCard = new VBox(12);
        pnlCard.setPadding(new Insets(20, 22, 18, 22));
        pnlCard.setStyle(
                "-fx-background-color: #ffffff; -fx-background-radius: 16px; " +
                "-fx-border-color: #e8e9e4; -fx-border-radius: 16px; -fx-border-width: 1px;"
        );

        tblSuppliers = new TableView<>(pageSuppliers);
        tblSuppliers.getStyleClass().add("figma-supplier-table");
        tblSuppliers.getStylesheets().add(createFigmaTableStylesheet());
        tblSuppliers.setPlaceholder(new Label("Không có dữ liệu nhà cung cấp."));
        tblSuppliers.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tblSuppliers.setFixedCellSize(58);

        // 1. Cột MÃ NCC
        TableColumn<Supplier, String> colId = new TableColumn<>("MÃ NCC");
        colId.setCellValueFactory(data -> new SimpleStringProperty(formatDisplayId(data.getValue().getMaNhaCungCap())));
        colId.setPrefWidth(120);
        colId.setMinWidth(105);
        colId.setSortable(false);

        // 2. Cột TÊN
        TableColumn<Supplier, String> colName = new TableColumn<>("TÊN");
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTenNhaCungCap()));
        colName.setPrefWidth(220);
        colName.setMinWidth(170);
        colName.setSortable(false);

        // 3. Cột SĐT
        TableColumn<Supplier, String> colPhone = new TableColumn<>("SĐT");
        colPhone.setCellValueFactory(data -> new SimpleStringProperty(formatDisplayPhone(data.getValue().getSoDienThoai())));
        colPhone.setPrefWidth(145);
        colPhone.setMinWidth(125);
        colPhone.setSortable(false);

        // 4. Cột ĐỊA CHỈ
        TableColumn<Supplier, String> colAddress = new TableColumn<>("ĐỊA CHỈ");
        colAddress.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDiaChi() != null && !data.getValue().getDiaChi().isBlank()
                        ? data.getValue().getDiaChi() : "—"
        ));
        colAddress.setPrefWidth(280);
        colAddress.setMinWidth(200);
        colAddress.setSortable(false);

        if (isSearchMode) {
            // Ở màn TÌM KIẾM (SUP_SEARCH): Hiển thị thêm cột EMAIL, MẶT HÀNG, TRẠNG THÁI (không có nút Sửa/Xóa)
            TableColumn<Supplier, String> colEmail = new TableColumn<>("EMAIL");
            colEmail.setCellValueFactory(data -> new SimpleStringProperty(
                    data.getValue().getEmail() != null && !data.getValue().getEmail().isBlank()
                            ? data.getValue().getEmail() : "—"
            ));
            colEmail.setPrefWidth(170);
            colEmail.setSortable(false);

            TableColumn<Supplier, String> colItems = new TableColumn<>("MẶT HÀNG");
            colItems.setCellValueFactory(data -> new SimpleStringProperty(
                    data.getValue().getMatHangCungCap() != null && !data.getValue().getMatHangCungCap().isBlank()
                            ? data.getValue().getMatHangCungCap() : "—"
            ));
            colItems.setPrefWidth(190);
            colItems.setSortable(false);

            TableColumn<Supplier, String> colStatus = new TableColumn<>("TRẠNG THÁI");
            colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTenTrangThai()));
            colStatus.setPrefWidth(130);
            colStatus.setMinWidth(120);
            colStatus.setSortable(false);

            tblSuppliers.getColumns().addAll(List.of(colId, colName, colPhone, colEmail, colAddress, colItems, colStatus));
        } else {
            // Ở màn CẬP NHẬT (SUP_UPDATE): Đúng 5 cột Figma (MÃ NCC, TÊN, SĐT, ĐỊA CHỈ, THAO TÁC Xem / Xóa)
            TableColumn<Supplier, Void> colActions = new TableColumn<>("THAO TÁC");
            colActions.setPrefWidth(145);
            colActions.setMinWidth(135);
            colActions.setMaxWidth(160);
            colActions.setSortable(false);
            colActions.setCellFactory(col -> new TableCell<>() {
                private final Button btnRowView = new Button("Xem");
                private final Button btnRowDelete = new Button("Xóa");
                private final HBox pnlActionBox = new HBox(8, btnRowView, btnRowDelete);

                {
                    pnlActionBox.setAlignment(Pos.CENTER_LEFT);

                    btnRowView.setStyle(
                            "-fx-background-color: #ffffff; -fx-border-color: #dcded8; -fx-border-radius: 8px; " +
                            "-fx-background-radius: 8px; -fx-text-fill: #2b312c; -fx-font-size: 12px; " +
                            "-fx-font-weight: bold; -fx-padding: 5px 13px; -fx-cursor: hand;"
                    );
                    btnRowView.setOnAction(e -> {
                        int idx = getIndex();
                        if (idx >= 0 && idx < getTableView().getItems().size()) {
                            Supplier current = getTableView().getItems().get(idx);
                            showDetailSupplierModal(current, current, null);
                        }
                    });

                    btnRowDelete.setStyle(
                            "-fx-background-color: #c94444; -fx-background-radius: 8px; " +
                            "-fx-text-fill: #ffffff; -fx-font-size: 12px; " +
                            "-fx-font-weight: bold; -fx-padding: 5px 13px; -fx-cursor: hand;"
                    );
                    btnRowDelete.setOnAction(e -> {
                        int idx = getIndex();
                        if (idx >= 0 && idx < getTableView().getItems().size()) {
                            Supplier current = getTableView().getItems().get(idx);
                            showDeactivateConfirmModal(current);
                        }
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                        setGraphic(null);
                    } else {
                        setGraphic(pnlActionBox);
                    }
                }
            });

            tblSuppliers.getColumns().addAll(List.of(colId, colName, colPhone, colAddress, colActions));
        }

        // Thanh phân trang ở góc dưới bên phải (Hiển thị 1–3 / 5 kết quả   ‹ 1 2 3 ›)
        HBox pnlFooter = new HBox(14);
        pnlFooter.setAlignment(Pos.CENTER_RIGHT);
        pnlFooter.setPadding(new Insets(6, 4, 0, 4));

        lblPaginationInfo = new Label("Hiển thị 0 kết quả");
        lblPaginationInfo.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #787d76;");

        HBox pnlPagerControls = new HBox(4);
        pnlPagerControls.setAlignment(Pos.CENTER_RIGHT);

        btnPrevPage = new Button("‹");
        btnPrevPage.setStyle(paginationArrowStyle());
        btnPrevPage.setOnAction(e -> changePage(currentPage - 1));

        pnlPageNumbers = new HBox(4);
        pnlPageNumbers.setAlignment(Pos.CENTER);

        btnNextPage = new Button("›");
        btnNextPage.setStyle(paginationArrowStyle());
        btnNextPage.setOnAction(e -> changePage(currentPage + 1));

        pnlPagerControls.getChildren().addAll(btnPrevPage, pnlPageNumbers, btnNextPage);
        pnlFooter.getChildren().addAll(lblPaginationInfo, pnlPagerControls);

        pnlCard.getChildren().addAll(tblSuppliers, pnlFooter);
        pnlMainContent.getChildren().add(pnlCard);
    }

    /**
     * Màn hình 1: Hiển thị Modal "Thêm nhà cung cấp" (dành riêng cho chức năng Thêm mới).
     */
    public void showAddSupplierModal() {
        VBox pnlModalCard = createModalCard(460, "Thêm nhà cung cấp");
        appendSupplierFormFields(pnlModalCard, null);
        pnlModalCard.getChildren().add(createModalActionBar("Hủy", this::closeModalOverlay, "Lưu", "#3f6148", this::handleAddSupplier));

        openModalOverlay(pnlModalCard);
        Platform.runLater(() -> txtTenNhaCungCap.requestFocus());
    }

    /**
     * Màn hình 2: Hiển thị màn hình "Thông tin chi tiết" của nhà cung cấp khi nhấn nút Xem.
     */
    public void showDetailSupplierModal(Supplier originalSupplier, Supplier currentValues, String initialError) {
        if (originalSupplier == null || currentValues == null) {
            return;
        }

        VBox pnlModalCard = createModalCard(460, "Thông tin chi tiết");

        TextField txtMaNCCReadOnly = createModalTextField("", formatDisplayId(originalSupplier.getMaNhaCungCap()));
        txtMaNCCReadOnly.setEditable(false);
        txtMaNCCReadOnly.setStyle(
                "-fx-background-color: #f4f4f0; -fx-background-radius: 8px; " +
                "-fx-border-color: #e2e4de; -fx-border-radius: 8px; -fx-border-width: 1px; " +
                "-fx-padding: 0 12px; -fx-font-size: 13px; -fx-text-fill: #6e736c; -fx-font-weight: bold;"
        );
        pnlModalCard.getChildren().add(createLabeledField("Mã nhà cung cấp", false, txtMaNCCReadOnly));

        appendSupplierFormFields(pnlModalCard, currentValues);
        if (initialError != null && !initialError.isBlank()) {
            showInlineFormError(initialError);
        }

        pnlModalCard.getChildren().add(createModalActionBar(
                "Hủy", this::closeModalOverlay,
                "Lưu", "#3f6148", () -> handleProceedToEditConfirm(originalSupplier)
        ));

        openModalOverlay(pnlModalCard);
        Platform.runLater(() -> {
            if (initialError != null) {
                focusFieldOnDuplicateError(initialError);
            } else {
                txtTenNhaCungCap.requestFocus();
            }
        });
    }

    /**
     * Xử lý nghiệp vụ Thêm mới Nhà cung cấp (kiểm tra Regex và kiểm tra trùng SĐT / Email).
     */
    private void handleAddSupplier() {
        if (!validateFormInputs()) {
            return;
        }
        try {
            String newId = supplierController.generateNewSupplierId();
            Supplier newSupplier = buildSupplierFromForm(newId);
            supplierController.createSupplier(newSupplier);
            applySearchAndFilter();
            showSuccessMessageModal("Đã lưu thành công",
                    "Đã lưu nhà cung cấp \"" + newSupplier.getTenNhaCungCap() +
                    "\" (" + formatDisplayId(newId) + ") thành công!");
        } catch (Exception ex) {
            focusFieldOnDuplicateError(ex.getMessage());
            showInlineFormError(ex.getMessage());
        }
    }

    /**
     * Kiểm tra thông tin thay đổi từ màn hình "Thông tin chi tiết" và chuyển sang màn hình "Xác nhận chỉnh sửa".
     */
    private void handleProceedToEditConfirm(Supplier originalSupplier) {
        if (!validateFormInputs()) {
            return;
        }
        Supplier updatedSupplier = buildSupplierFromForm(originalSupplier.getMaNhaCungCap());
        List<String> changes = collectSupplierChanges(originalSupplier, updatedSupplier);
        if (changes.isEmpty()) {
            showInlineFormError("Bạn chưa thay đổi thông tin nào của nhà cung cấp.");
            return;
        }
        showEditConfirmModal(originalSupplier, updatedSupplier, changes);
    }

    /**
     * Màn hình 3: Hiển thị màn hình "Xác nhận chỉnh sửa" khi người dùng thay đổi thông tin từ màn "Thông tin chi tiết".
     */
    private void showEditConfirmModal(Supplier originalSupplier, Supplier updatedSupplier, List<String> changes) {
        VBox pnlConfirmCard = createModalCard(450, "Xác nhận chỉnh sửa");

        Label lblConfirmDesc = new Label(
                "Bạn có chắc chắn muốn cập nhật các thông tin đã thay đổi cho nhà cung cấp \"" +
                originalSupplier.getTenNhaCungCap() + "\" (" + formatDisplayId(originalSupplier.getMaNhaCungCap()) + ") không?"
        );
        lblConfirmDesc.setWrapText(true);
        lblConfirmDesc.setStyle("-fx-font-size: 13.5px; -fx-text-fill: #5a6058;");

        VBox pnlChangesBox = new VBox(6);
        pnlChangesBox.setPadding(new Insets(10, 12, 10, 12));
        pnlChangesBox.setStyle(
                "-fx-background-color: #f6f7f4; -fx-background-radius: 10px; " +
                "-fx-border-color: #e6e8e3; -fx-border-radius: 10px;"
        );
        for (String diff : changes) {
            Label lblDiff = new Label("• " + diff);
            lblDiff.setWrapText(true);
            lblDiff.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #2b312c;");
            pnlChangesBox.getChildren().add(lblDiff);
        }

        HBox pnlActions = createModalActionBar(
                "Quay lại", () -> showDetailSupplierModal(originalSupplier, updatedSupplier, null),
                "Xác nhận", "#3f6148", () -> {
                    try {
                        supplierController.modifySupplier(updatedSupplier);
                        applySearchAndFilter();
                        showSuccessMessageModal("Đã lưu thành công",
                                "Đã lưu thông tin chỉnh sửa cho nhà cung cấp \"" +
                                updatedSupplier.getTenNhaCungCap() + "\" (" + formatDisplayId(updatedSupplier.getMaNhaCungCap()) + ") thành công!");
                    } catch (Exception ex) {
                        showDetailSupplierModal(originalSupplier, updatedSupplier, ex.getMessage());
                    }
                }
        );

        pnlConfirmCard.getChildren().addAll(lblConfirmDesc, pnlChangesBox, pnlActions);
        openModalOverlay(pnlConfirmCard);
    }

    /**
     * So sánh và trả về danh sách các trường thông tin đã thay đổi giữa dữ liệu gốc và dữ liệu chỉnh sửa.
     */
    private List<String> collectSupplierChanges(Supplier original, Supplier updated) {
        List<String> diffs = new ArrayList<>();
        if (!safeStr(original.getTenNhaCungCap()).equals(safeStr(updated.getTenNhaCungCap()))) {
            diffs.add("Tên NCC: " + safeStr(original.getTenNhaCungCap()) + " → " + safeStr(updated.getTenNhaCungCap()));
        }
        if (!safeStr(original.getSoDienThoai()).equals(safeStr(updated.getSoDienThoai()))) {
            diffs.add("Số điện thoại: " + safeStr(original.getSoDienThoai()) + " → " + safeStr(updated.getSoDienThoai()));
        }
        if (!safeStr(original.getEmail()).equalsIgnoreCase(safeStr(updated.getEmail()))) {
            diffs.add("Email: " + safeStr(original.getEmail()) + " → " + safeStr(updated.getEmail()));
        }
        if (!safeStr(original.getDiaChi()).equals(safeStr(updated.getDiaChi()))) {
            diffs.add("Địa chỉ: " + safeStr(original.getDiaChi()) + " → " + safeStr(updated.getDiaChi()));
        }
        if (!safeStr(original.getMatHangCungCap()).equals(safeStr(updated.getMatHangCungCap()))) {
            diffs.add("Mặt hàng: " + safeStr(original.getMatHangCungCap()) + " → " + safeStr(updated.getMatHangCungCap()));
        }
        return diffs;
    }

    private String safeStr(String s) {
        return s != null ? s.trim() : "";
    }

    /**
     * Kiểm tra các trường nhập trên form theo chuẩn Regex trước khi gửi xuống Controller.
     */
    private boolean validateFormInputs() {
        String name = safeStr(txtTenNhaCungCap.getText());
        String phone = safeStr(txtSoDienThoai.getText()).replaceAll("\\s+", "");
        String email = safeStr(txtEmail.getText());
        String items = safeStr(txtMatHangCungCap.getText());

        if (name.isEmpty()) {
            showInlineFormError("Vui lòng nhập Tên nhà cung cấp (*).");
            txtTenNhaCungCap.requestFocus();
            return false;
        }
        if (!SupplierService.REGEX_PHONE.matcher(phone).matches()) {
            showInlineFormError("Số điện thoại không hợp lệ! Phải bắt đầu bằng số 0 và gồm đúng 10 chữ số (0 + 9 chữ số).");
            txtSoDienThoai.requestFocus();
            return false;
        }
        if (email.isEmpty() || !SupplierService.REGEX_EMAIL.matcher(email).matches()) {
            showInlineFormError("Email không đúng định dạng (ví dụ: contact@domain.com)!");
            txtEmail.requestFocus();
            return false;
        }
        if (items.isEmpty()) {
            showInlineFormError("Vui lòng nhập Mặt hàng cung cấp (*).");
            txtMatHangCungCap.requestFocus();
            return false;
        }
        return true;
    }

    private Supplier buildSupplierFromForm(String supplierId) {
        return new Supplier(
                supplierId,
                safeStr(txtTenNhaCungCap.getText()),
                safeStr(txtSoDienThoai.getText()).replaceAll("\\s+", ""),
                safeStr(txtEmail.getText()),
                safeStr(txtDiaChi.getText()),
                safeStr(txtMatHangCungCap.getText()),
                true
        );
    }

    private void focusFieldOnDuplicateError(String errorMsg) {
        if (errorMsg == null) {
            return;
        }
        String lower = errorMsg.toLowerCase();
        if (lower.contains("số điện thoại") && txtSoDienThoai != null) {
            txtSoDienThoai.requestFocus();
        } else if (lower.contains("email") && txtEmail != null) {
            txtEmail.requestFocus();
        }
    }

    /**
     * Hiển thị hộp thoại xác nhận ngừng hoạt động (xóa mềm) Nhà cung cấp trên nền Overlay.
     */
    private void showDeactivateConfirmModal(Supplier supplier) {
        if (supplier == null) {
            return;
        }

        VBox pnlConfirmCard = createModalCard(420, "Xác nhận ngừng hoạt động");

        Label lblConfirmDesc = new Label(
                "Bạn có chắc chắn muốn ngừng hợp tác (xóa mềm) với nhà cung cấp \"" +
                supplier.getTenNhaCungCap() + "\" (" + formatDisplayId(supplier.getMaNhaCungCap()) + ") không?"
        );
        lblConfirmDesc.setWrapText(true);
        lblConfirmDesc.setStyle("-fx-font-size: 13.5px; -fx-text-fill: #5a6058;");

        HBox pnlActions = createModalActionBar(
                "Hủy", this::closeModalOverlay,
                "Xóa", "#c94444", () -> {
                    try {
                        supplierController.removeSupplierSoftly(supplier.getMaNhaCungCap());
                        applySearchAndFilter();
                        showSuccessMessageModal("Đã xóa thành công",
                                "Đã xóa nhà cung cấp \"" + supplier.getTenNhaCungCap() +
                                "\" (" + formatDisplayId(supplier.getMaNhaCungCap()) + ") thành công!");
                    } catch (Exception ex) {
                        lblConfirmDesc.setText("Lỗi: " + ex.getMessage());
                        lblConfirmDesc.setStyle("-fx-font-size: 13px; -fx-text-fill: #c94444; -fx-font-weight: bold;");
                    }
                }
        );

        pnlConfirmCard.getChildren().addAll(lblConfirmDesc, pnlActions);
        openModalOverlay(pnlConfirmCard);
    }

    /**
     * Hiển thị hộp thoại thông báo hoàn tất ("Đã lưu thành công" hoặc "Đã xóa thành công").
     */
    private void showSuccessMessageModal(String title, String detailMessage) {
        VBox pnlSuccessCard = createModalCard(400, title);

        Label lblMessage = new Label(detailMessage);
        lblMessage.setWrapText(true);
        lblMessage.setStyle("-fx-font-size: 13.5px; -fx-text-fill: #2f5e3d; -fx-font-weight: bold;");

        HBox pnlActions = new HBox();
        pnlActions.setAlignment(Pos.CENTER_RIGHT);
        VBox.setMargin(pnlActions, new Insets(6, 0, 0, 0));

        Button btnCloseSuccess = new Button("Đóng");
        btnCloseSuccess.setStyle(
                "-fx-background-color: #3f6148; -fx-background-radius: 8px; " +
                "-fx-text-fill: #ffffff; -fx-font-size: 13px; " +
                "-fx-font-weight: bold; -fx-padding: 8px 24px; -fx-cursor: hand;"
        );
        btnCloseSuccess.setOnAction(e -> closeModalOverlay());

        pnlActions.getChildren().add(btnCloseSuccess);
        pnlSuccessCard.getChildren().addAll(lblMessage, pnlActions);

        openModalOverlay(pnlSuccessCard);
        Platform.runLater(btnCloseSuccess::requestFocus);
    }

    private VBox createModalCard(double width, String titleText) {
        VBox pnlModalCard = new VBox(14);
        pnlModalCard.setMaxWidth(width);
        pnlModalCard.setPrefWidth(width);
        pnlModalCard.setMaxHeight(Region.USE_PREF_SIZE);
        pnlModalCard.setPadding(new Insets(24, 26, 22, 26));
        pnlModalCard.setStyle(
                "-fx-background-color: #ffffff; -fx-background-radius: 16px; " +
                "-fx-border-color: #e6e8e3; -fx-border-radius: 16px; -fx-border-width: 1px;"
        );
        pnlModalCard.setEffect(new DropShadow(28, 0, 8, Color.rgb(0, 0, 0, 0.18)));

        Label lblModalTitle = new Label(titleText);
        lblModalTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1b211c;");
        VBox.setMargin(lblModalTitle, new Insets(0, 0, 2, 0));
        pnlModalCard.getChildren().add(lblModalTitle);
        return pnlModalCard;
    }

    private void appendSupplierFormFields(VBox pnlModalCard, Supplier values) {
        txtTenNhaCungCap = createModalTextField("Nhập tên nhà cung cấp...", values != null ? values.getTenNhaCungCap() : "");
        txtSoDienThoai = createModalTextField("Nhập số điện thoại...", values != null ? values.getSoDienThoai() : "");
        txtEmail = createModalTextField("Nhập email...", values != null ? values.getEmail() : "");
        txtDiaChi = createModalTextField("Nhập địa chỉ...", values != null ? values.getDiaChi() : "");
        txtMatHangCungCap = createModalTextField("Nhập mặt hàng cung cấp...", values != null ? values.getMatHangCungCap() : "");

        lblFormError = new Label();
        lblFormError.setWrapText(true);
        lblFormError.setVisible(false);
        lblFormError.setManaged(false);
        lblFormError.setStyle(
                "-fx-text-fill: #c94444; -fx-font-size: 12.5px; -fx-font-weight: bold; " +
                "-fx-background-color: #fdf0f0; -fx-padding: 8px 12px; -fx-background-radius: 8px;"
        );

        pnlModalCard.getChildren().addAll(
                createLabeledField("Tên nhà cung cấp", true, txtTenNhaCungCap),
                createLabeledField("Số điện thoại", true, txtSoDienThoai),
                createLabeledField("Email", true, txtEmail),
                createLabeledField("Địa chỉ", false, txtDiaChi),
                createLabeledField("Mặt hàng cung cấp", true, txtMatHangCungCap),
                lblFormError
        );
    }

    private HBox createModalActionBar(String cancelText, Runnable onCancel,
                                      String primaryText, String primaryColor, Runnable onPrimary) {
        HBox pnlModalActions = new HBox(12);
        pnlModalActions.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(pnlModalActions, new Insets(6, 0, 0, 0));

        Button btnCancelModal = new Button(cancelText);
        btnCancelModal.setStyle(
                "-fx-background-color: #ffffff; -fx-border-color: #dcded8; -fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; -fx-text-fill: #2b312c; -fx-font-size: 13px; " +
                "-fx-font-weight: bold; -fx-padding: 8px 18px; -fx-cursor: hand;"
        );
        btnCancelModal.setOnAction(e -> onCancel.run());

        Region pnlActionSpacer = new Region();
        HBox.setHgrow(pnlActionSpacer, Priority.ALWAYS);

        Button btnSaveModal = new Button(primaryText);
        btnSaveModal.setStyle(
                "-fx-background-color: " + primaryColor + "; -fx-background-radius: 8px; " +
                "-fx-text-fill: #ffffff; -fx-font-size: 13px; " +
                "-fx-font-weight: bold; -fx-padding: 8px 22px; -fx-cursor: hand;"
        );
        btnSaveModal.setOnAction(e -> onPrimary.run());

        pnlModalActions.getChildren().addAll(btnCancelModal, pnlActionSpacer, btnSaveModal);
        return pnlModalActions;
    }

    private void openModalOverlay(Region pnlCard) {
        pnlModalOverlay.getChildren().setAll(pnlCard);
        pnlModalOverlay.setVisible(true);
        pnlModalOverlay.setManaged(true);
    }

    /**
     * Đóng lớp phủ Modal.
     */
    private void closeModalOverlay() {
        pnlModalOverlay.setVisible(false);
        pnlModalOverlay.setManaged(false);
        pnlModalOverlay.getChildren().clear();
    }

    /**
     * Hiển thị thông báo lỗi ngay trong form Modal.
     */
    private void showInlineFormError(String message) {
        if (lblFormError != null) {
            lblFormError.setText(message);
            lblFormError.setVisible(true);
            lblFormError.setManaged(true);
        }
    }

    /**
     * Tạo một cụm Nhãn (có dấu * đỏ nếu bắt buộc) + Ô nhập liệu theo chuẩn Figma 09 · Thêm / Sửa NCC.
     */
    private VBox createLabeledField(String labelText, boolean required, TextField textField) {
        VBox pnlField = new VBox(6);

        HBox pnlLabelRow = new HBox(3);
        pnlLabelRow.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label(labelText);
        lblTitle.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #4e534d;");
        pnlLabelRow.getChildren().add(lblTitle);

        if (required) {
            Label lblStar = new Label("*");
            lblStar.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #d14343;");
            pnlLabelRow.getChildren().add(lblStar);
        }

        pnlField.getChildren().addAll(pnlLabelRow, textField);
        return pnlField;
    }

    /**
     * Tạo TextField bo góc tinh tế chuẩn Figma 09 · Thêm / Sửa NCC.
     */
    private TextField createModalTextField(String prompt, String initialValue) {
        TextField txtField = new TextField(initialValue != null ? initialValue : "");
        txtField.setPromptText(prompt);
        txtField.setPrefHeight(40);
        txtField.setStyle(
                "-fx-background-color: #ffffff; -fx-background-radius: 8px; " +
                "-fx-border-color: #e2e4de; -fx-border-radius: 8px; -fx-border-width: 1px; " +
                "-fx-padding: 0 12px; -fx-font-size: 13px; -fx-text-fill: #1f2420; " +
                "-fx-prompt-text-fill: #9fa39b;"
        );
        return txtField;
    }

    /**
     * Đăng ký các phím tắt bàn phím: Ctrl+N (Thêm mới khi ở màn SUP_UPDATE), Esc (Đóng Modal), F5 (Làm mới).
     */
    private void registerKeyboardShortcuts() {
        addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE && pnlModalOverlay.isVisible()) {
                closeModalOverlay();
                event.consume();
            } else if (!isSearchMode && event.isControlDown() && event.getCode() == KeyCode.N && !pnlModalOverlay.isVisible()) {
                showAddSupplierModal();
                event.consume();
            } else if (event.getCode() == KeyCode.F5 && !pnlModalOverlay.isVisible()) {
                applySearchAndFilter();
                event.consume();
            }
        });
    }

    /**
     * Thực hiện tải danh sách (hoặc tìm kiếm khi ở màn SUP_SEARCH) trên luồng nền.
     */
    public void applySearchAndFilter() {
        final String keyword = (isSearchMode && txtSearch != null) ? txtSearch.getText() : "";
        final Boolean activeFilter;
        if (isSearchMode && cboStatusFilter != null) {
            String sel = cboStatusFilter.getValue();
            if ("Đang hợp tác".equals(sel)) {
                activeFilter = true;
            } else if ("Ngừng hoạt động".equals(sel)) {
                activeFilter = false;
            } else {
                activeFilter = null;
            }
        } else {
            // Màn Cập nhật mặc định hiển thị các nhà cung cấp đang hợp tác
            activeFilter = true;
        }

        final long requestId = searchRequestCounter.incrementAndGet();

        CompletableFuture.supplyAsync(() -> supplierController.searchSuppliers(keyword, activeFilter))
                .thenAccept(fetched -> Platform.runLater(() -> {
                    if (requestId != searchRequestCounter.get()) {
                        return;
                    }
                    allFilteredSuppliers.clear();
                    allFilteredSuppliers.addAll(fetched);
                    currentPage = 1;
                    updatePaginationView();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        lblPaginationInfo.setText("Lỗi tải dữ liệu");
                    });
                    return null;
                });
    }

    /**
     * Chuyển trang hiển thị trên bảng dữ liệu.
     */
    private void changePage(int targetPage) {
        int totalPages = Math.max(1, (int) Math.ceil((double) allFilteredSuppliers.size() / PAGE_SIZE));
        if (targetPage >= 1 && targetPage <= totalPages) {
            currentPage = targetPage;
            updatePaginationView();
        }
    }

    /**
     * Cập nhật các dòng hiển thị theo trang hiện tại, chiều cao bảng vừa khít và thanh số trang chuẩn Figma.
     */
    private void updatePaginationView() {
        int totalItems = allFilteredSuppliers.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE));
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }

        int fromIndex = (currentPage - 1) * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, totalItems);

        if (totalItems == 0) {
            pageSuppliers.clear();
            lblPaginationInfo.setText("Hiển thị 0 / 0 kết quả");
        } else {
            pageSuppliers.setAll(allFilteredSuppliers.subList(fromIndex, toIndex));
            lblPaginationInfo.setText(String.format("Hiển thị %d–%d / %d kết quả",
                    fromIndex + 1, toIndex, totalItems));
        }

        // Co giãn chiều cao bảng vừa khít số dòng hiện tại để không có dòng trống kẻ sọc bên dưới
        int visibleRowCount = Math.max(1, pageSuppliers.size());
        double exactHeight = 44 + (visibleRowCount * 58) + 4;
        tblSuppliers.setPrefHeight(exactHeight);
        tblSuppliers.setMinHeight(exactHeight);
        tblSuppliers.setMaxHeight(exactHeight);

        // Vẽ dãy số trang ‹ 1 2 3 › giống hệt Figma
        pnlPageNumbers.getChildren().clear();
        for (int i = 1; i <= totalPages; i++) {
            final int pageNum = i;
            Button btnPage = new Button(String.valueOf(pageNum));
            boolean isActive = (pageNum == currentPage);
            btnPage.setStyle(isActive
                    ? "-fx-background-color: transparent; -fx-text-fill: #1b211c; -fx-font-size: 12.5px; -fx-font-weight: bold; -fx-padding: 2px 6px; -fx-cursor: hand;"
                    : "-fx-background-color: transparent; -fx-text-fill: #8a8e87; -fx-font-size: 12.5px; -fx-padding: 2px 6px; -fx-cursor: hand;");
            btnPage.setOnAction(e -> changePage(pageNum));
            pnlPageNumbers.getChildren().add(btnPage);
        }

        btnPrevPage.setDisable(currentPage <= 1);
        btnNextPage.setDisable(currentPage >= totalPages);
    }

    /**
     * Rút gọn mã NCC hiển thị dạng NCC-001, NCC-002 giống thiết kế Figma (vẫn giữ mã gốc đầy đủ khi lưu CSDL).
     */
    private String formatDisplayId(String rawId) {
        if (rawId == null) {
            return "";
        }
        if (rawId.matches("^NCC-\\d{8}-\\d{6}$")) {
            String lastDigits = rawId.substring(rawId.length() - 3);
            return "NCC-" + lastDigits;
        }
        return rawId;
    }

    /**
     * Định dạng hiển thị số điện thoại thoáng đẹp (ví dụ: 028 3822 8899).
     */
    private String formatDisplayPhone(String rawPhone) {
        if (rawPhone == null) {
            return "";
        }
        String digits = rawPhone.trim();
        if (digits.length() == 10) {
            return digits.substring(0, 3) + " " + digits.substring(3, 7) + " " + digits.substring(7);
        }
        return digits;
    }

    private String paginationArrowStyle() {
        return "-fx-background-color: transparent; -fx-text-fill: #787d76; -fx-font-size: 13px; " +
               "-fx-padding: 2px 6px; -fx-cursor: hand;";
    }

    /**
     * Tạo stylesheet nhúng trực tiếp cho TableView để loại bỏ hoàn toàn đường kẻ dọc,
     * nền sọc xám mặc định của JavaFX và tạo thanh tiêu đề bo góc màu #f4f4f0 chuẩn Figma.
     */
    private String createFigmaTableStylesheet() {
        String css = """
            .figma-supplier-table.table-view {
                -fx-background-color: #ffffff;
                -fx-border-color: transparent;
                -fx-table-cell-border-color: transparent;
                -fx-padding: 0;
            }
            .figma-supplier-table.table-view:focused {
                -fx-background-color: #ffffff;
                -fx-background-insets: 0;
            }
            .figma-supplier-table .column-header-background {
                -fx-background-color: #f4f4f0;
                -fx-background-radius: 8px;
                -fx-min-height: 42px;
                -fx-pref-height: 42px;
                -fx-max-height: 42px;
            }
            .figma-supplier-table .column-header-background .filler {
                -fx-background-color: transparent;
            }
            .figma-supplier-table .column-header {
                -fx-background-color: transparent;
                -fx-border-color: transparent;
                -fx-size: 42px;
                -fx-padding: 0 12px;
            }
            .figma-supplier-table .column-header .label {
                -fx-text-fill: #6e736c;
                -fx-font-size: 11.5px;
                -fx-font-weight: bold;
                -fx-alignment: CENTER-LEFT;
            }
            .figma-supplier-table .table-row-cell {
                -fx-background-color: #ffffff;
                -fx-border-color: transparent transparent #eff0ec transparent;
                -fx-border-width: 0 0 1px 0;
            }
            .figma-supplier-table .table-row-cell:odd {
                -fx-background-color: #ffffff;
            }
            .figma-supplier-table .table-row-cell:selected {
                -fx-background-color: #f7faf7;
            }
            .figma-supplier-table .table-row-cell:empty {
                -fx-background-color: #ffffff;
                -fx-border-color: transparent;
            }
            .figma-supplier-table .table-cell {
                -fx-border-color: transparent;
                -fx-alignment: CENTER-LEFT;
                -fx-padding: 0 12px;
                -fx-text-fill: #1f2420;
                -fx-font-size: 13.5px;
            }
            .figma-supplier-table .scroll-bar:horizontal,
            .figma-supplier-table .scroll-bar:vertical {
                -fx-opacity: 0;
                -fx-pref-width: 0;
                -fx-pref-height: 0;
            }
        """;
        String base64 = Base64.getEncoder().encodeToString(css.getBytes(StandardCharsets.UTF_8));
        return "data:text/css;base64," + base64;
    }
}
