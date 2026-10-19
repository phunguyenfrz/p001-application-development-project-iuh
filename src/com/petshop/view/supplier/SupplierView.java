package com.petshop.view.supplier;

import com.petshop.controller.SupplierController;
import com.petshop.model.Supplier;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Effect;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Giao diện quản lý nhà cung cấp (chuẩn hóa theo đối tượng Khách hàng - CustomerView).
 *
 * SUP_UPDATE: cập nhật nhà cung cấp (Thêm, Sửa, Ngừng hợp tác).
 * SUP_SEARCH: tìm kiếm và xem thông tin nhà cung cấp.
 *
 * Giao diện gọi SupplierController, tuân thủ chặt chẽ kiến trúc n-Tier.
 */
public class SupplierView extends VBox {

    // Trang Cập nhật chỉ hiển thị nhà cung cấp đang hợp tác.
    private boolean update_mode = false;

    // Controller tiếp nhận yêu cầu từ giao diện.
    private final SupplierController supplier_controller = new SupplierController();

    // Danh sách nhà cung cấp gốc được tải từ database.
    private final ObservableList<Supplier> supplier_list = FXCollections.observableArrayList();

    // Danh sách nhà cung cấp sau khi áp dụng bộ lọc.
    private final FilteredList<Supplier> filtered_suppliers = new FilteredList<>(supplier_list, supplier -> true);

    // Trạng thái đang tải hoặc ghi dữ liệu.
    private final SimpleBooleanProperty busy = new SimpleBooleanProperty(false);

    // Bảng hiển thị thông tin nhà cung cấp.
    private final TableView<Supplier> tblSuppliers = new TableView<>();

    // Ô nhập từ khóa trên trang tìm kiếm.
    private final TextField txtSearch = new TextField();

    // Các nút trên trang cập nhật.
    private final Button btnAdd = new Button("＋ Thêm nhà cung cấp");
    private final Button btnEdit = new Button("Sửa nhà cung cấp");
    private final Button btnDelete = new Button("Ngừng hợp tác");

    // Các nút trên trang tìm kiếm.
    private final Button btnSearch = new Button("Tìm kiếm");
    private final Button btnViewInfo = new Button("Xem thông tin");
    private final Button btnRefresh = new Button("Làm mới");

    // Lọc nhà cung cấp theo trạng thái trên trang Tìm kiếm.
    private final ComboBox<String> cboSupplierStatus = new ComboBox<>();

    // Nhãn thông báo số lượng hoặc trạng thái xử lý.
    private final Label lblStatus = new Label();

    // Từ khóa đã xác nhận bằng nút Tìm kiếm hoặc phím Enter.
    private String search_keyword = "";

    /**
     * Khởi tạo màn hình theo chức năng được chọn.
     *
     * @param action_id Mã chức năng từ MenuRegistry ("SUP_UPDATE" hoặc "SUP_SEARCH").
     */
    public SupplierView(String action_id) {
        if ("SUP_SEARCH".equals(action_id)) {
            initializeSearchScreen();
            return;
        }

        initializeUpdateScreen();
    }

    /**
     * Dựng trang cập nhật nhà cung cấp.
     * Trang này có các nút Thêm, Sửa và Ngừng hợp tác.
     */
    private void initializeUpdateScreen() {
        update_mode = true;
        getStyleClass().add("customer-page");
        setSpacing(20);
        setPadding(new Insets(28));

        // Tiêu đề của trang cập nhật.
        Label lblTitle = new Label("Cập nhật nhà cung cấp");
        lblTitle.getStyleClass().add("customer-title");

        // Khoảng trống đẩy nhóm nút sang bên phải.
        Region header_spacer = new Region();
        HBox.setHgrow(header_spacer, Priority.ALWAYS);

        btnAdd.getStyleClass().add("customer-add");
        btnEdit.getStyleClass().add("customer-secondary");
        btnDelete.getStyleClass().add("customer-danger");

        // Hàng tiêu đề và các nút thao tác.
        HBox header = new HBox(
                10,
                lblTitle,
                header_spacer,
                btnAdd,
                btnEdit,
                btnDelete);
        header.setAlignment(Pos.CENTER_LEFT);

        configureTable();

        // Thẻ trắng chứa bảng dữ liệu.
        VBox table_card = createTableCard();

        lblStatus.getStyleClass().add("customer-status");

        // Khóa thao tác trong lúc đang truy vấn hoặc lưu dữ liệu.
        btnAdd.disableProperty().bind(busy);
        tblSuppliers.disableProperty().bind(busy);

        // Chỉ cho sửa hoặc xóa khi có nhà cung cấp được chọn.
        btnEdit.disableProperty().bind(
                busy.or(tblSuppliers.getSelectionModel().selectedItemProperty().isNull()));

        btnDelete.disableProperty().bind(
                busy.or(tblSuppliers.getSelectionModel().selectedItemProperty().isNull()));

        btnAdd.setOnAction(event -> showSupplierDialog(null));
        btnEdit.setOnAction(event -> showSupplierDialog(tblSuppliers.getSelectionModel().getSelectedItem()));
        btnDelete.setOnAction(event -> showDeleteDialog());

        registerRefreshShortcut();

        getChildren().addAll(
                header,
                table_card,
                lblStatus);

        loadSuppliers();
    }

    /**
     * Dựng trang tìm kiếm nhà cung cấp.
     * Tìm khi bấm nút Tìm kiếm hoặc nhấn Enter.
     */
    private void initializeSearchScreen() {
        getStyleClass().addAll(
                "customer-page",
                "customer-search-page");

        setSpacing(20);
        setPadding(new Insets(28));

        // Tiêu đề trang tìm kiếm.
        Label lblTitle = new Label("Tìm kiếm nhà cung cấp");
        lblTitle.getStyleClass().add("customer-title");

        // Nhãn mô tả tiêu chí tìm kiếm.
        Label lblSearch = new Label("Tìm kiếm nhà cung cấp");
        lblSearch.getStyleClass().add("customer-search-label");

        txtSearch.setPromptText("Tìm kiếm theo mã, tên, số điện thoại, email, mặt hàng...");
        txtSearch.setPrefWidth(440);
        txtSearch.setMinWidth(220);
        txtSearch.getStyleClass().add("customer-search");

        btnSearch.getStyleClass().add("customer-save");
        btnViewInfo.getStyleClass().add("customer-secondary");
        btnRefresh.getStyleClass().add("customer-secondary");

        // Khoảng trống đẩy nhóm nút sang bên phải.
        Region search_spacer = new Region();
        HBox.setHgrow(search_spacer, Priority.ALWAYS);

        // Lựa chọn trạng thái, mặc định tra cứu tất cả nhà cung cấp.
        cboSupplierStatus.getItems().addAll("Tất cả", "Đang hợp tác", "Ngừng hoạt động");
        cboSupplierStatus.setValue("Tất cả");
        cboSupplierStatus.setPrefWidth(165);
        cboSupplierStatus.getStyleClass().add("customer-status-filter");

        // Đặt nút Tìm kiếm sát ô nhập theo chuẩn Figma.
        HBox search_box = new HBox(0, txtSearch, btnSearch);
        search_box.setAlignment(Pos.CENTER_LEFT);
        search_box.getStyleClass().add("customer-search-group");
        txtSearch.setPrefWidth(350);
        HBox.setHgrow(txtSearch, Priority.ALWAYS);

        // Xem thông tin, trạng thái và Làm mới nằm bên phải.
        HBox search_bar = new HBox(
                10,
                search_box,
                search_spacer,
                btnViewInfo,
                cboSupplierStatus,
                btnRefresh);
        search_bar.setAlignment(Pos.CENTER_LEFT);

        VBox search_section = new VBox(
                10,
                lblSearch,
                search_bar);

        configureTable();

        VBox table_card = createTableCard();

        lblStatus.getStyleClass().add("customer-status");

        // Không cho thao tác khi đang tải dữ liệu.
        btnSearch.disableProperty().bind(busy);
        cboSupplierStatus.disableProperty().bind(busy);
        btnRefresh.disableProperty().bind(busy);
        txtSearch.disableProperty().bind(busy);
        tblSuppliers.disableProperty().bind(busy);

        // Chỉ cho xem thông tin khi có dòng được chọn.
        btnViewInfo.disableProperty().bind(
                busy.or(tblSuppliers.getSelectionModel().selectedItemProperty().isNull()));

        btnSearch.setOnAction(event -> searchSuppliers());
        txtSearch.setOnAction(event -> searchSuppliers());

        btnViewInfo.setOnAction(event -> showSupplierInfo());

        cboSupplierStatus.setOnAction(event -> {
            tblSuppliers.getSelectionModel().clearSelection();
            filterSuppliers();
        });

        btnRefresh.setOnAction(event -> {
            txtSearch.clear();
            search_keyword = "";
            cboSupplierStatus.setValue("Tất cả");
            tblSuppliers.getSelectionModel().clearSelection();
            loadSuppliers();
        });

        registerRefreshShortcut();

        getChildren().addAll(
                lblTitle,
                search_section,
                table_card,
                lblStatus);

        loadSuppliers();
    }

    /**
     * Thiết lập bảng dữ liệu dùng chung cho hai trang.
     */
    private void configureTable() {
        createColumns();

        tblSuppliers.setItems(filtered_suppliers);
        tblSuppliers.getStyleClass().add("customer-table");
        tblSuppliers.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblSuppliers.setPlaceholder(new Label("Không có nhà cung cấp phù hợp"));

        tblSuppliers.setRowFactory(tv -> {
            TableRow<Supplier> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    if (update_mode) {
                        showSupplierDialog(row.getItem());
                    } else {
                        showSupplierInfo();
                    }
                }
            });
            return row;
        });
    }

    /**
     * Tạo thẻ trắng chứa bảng nhà cung cấp.
     */
    private VBox createTableCard() {
        VBox table_card = new VBox(tblSuppliers);
        table_card.setPadding(new Insets(20));
        table_card.getStyleClass().add("customer-card");

        VBox.setVgrow(tblSuppliers, Priority.ALWAYS);
        VBox.setVgrow(table_card, Priority.ALWAYS);

        return table_card;
    }

    /**
     * Tạo các cột theo đúng cấu trúc của bảng nhà cung cấp.
     */
    private void createColumns() {
        tblSuppliers.getColumns().clear();

        tblSuppliers.getColumns().add(
                createColumn("Mã NCC", Supplier::getMaNhaCungCap, 170));

        tblSuppliers.getColumns().add(
                createColumn("Tên nhà cung cấp", Supplier::getTenNhaCungCap, 220));

        tblSuppliers.getColumns().add(
                createColumn("Số điện thoại", Supplier::getSoDienThoai, 135));

        tblSuppliers.getColumns().add(
                createColumn("Email", Supplier::getEmail, 190));

        tblSuppliers.getColumns().add(
                createColumn("Địa chỉ", Supplier::getDiaChi, 220));

        tblSuppliers.getColumns().add(
                createColumn("Mặt hàng cung cấp", Supplier::getMatHangCungCap, 200));

        if (!update_mode) {
            tblSuppliers.getColumns().add(
                    createColumn("Trạng thái", Supplier::getTenTrangThai, 130));
        }
    }

    /**
     * Tạo cột có nội dung tự xuống dòng tương tự bảng Khách hàng.
     */
    private TableColumn<Supplier, String> createColumn(
            String title,
            Function<Supplier, String> getter,
            double width) {
        TableColumn<Supplier, String> column = new TableColumn<>(title);

        column.setPrefWidth(width);
        column.setMinWidth(85);
        column.setSortable(false);

        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                textOrEmpty(getter.apply(cell.getValue()))));

        column.setCellFactory(table_column -> new TableCell<>() {
            private final Label lblValue = new Label();

            {
                lblValue.setWrapText(true);
                lblValue.getStyleClass().add("customer-cell-text");
                lblValue.prefWidthProperty().bind(widthProperty().subtract(20));
            }

            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                setText(null);

                if (empty) {
                    lblValue.setText("");
                    setGraphic(null);
                } else {
                    lblValue.setText(textOrEmpty(value));
                    setGraphic(lblValue);
                }
            }
        });

        return column;
    }

    /**
     * Đọc lại danh sách nhà cung cấp ở luồng nền.
     */
    private void loadSuppliers() {
        lblStatus.setText("Đang tải nhà cung cấp...");

        executeBackground(
                () -> update_mode
                        ? supplier_controller.loadActiveSuppliers()
                        : supplier_controller.loadSuppliers(),
                suppliers -> {
                    tblSuppliers.getSelectionModel().clearSelection();
                    supplier_list.setAll(suppliers);
                    filterSuppliers();
                },
                error -> {
                    lblStatus.setText("Không tải được dữ liệu");
                    showError(error);
                });
    }

    /**
     * Xác nhận từ khóa và thực hiện tìm kiếm.
     */
    private void searchSuppliers() {
        if (busy.get()) {
            return;
        }

        search_keyword = txtSearch.getText().trim();
        tblSuppliers.getSelectionModel().clearSelection();
        filterSuppliers();
    }

    /**
     * Lọc tất cả nhà cung cấp khớp mã, tên, số điện thoại, email hoặc mặt hàng.
     */
    private void filterSuppliers() {
        String keyword = normalizeText(search_keyword);

        String selected_status = update_mode
                ? "Tất cả" : cboSupplierStatus.getValue();

        filtered_suppliers.setPredicate(supplier -> {
            boolean matches_keyword = keyword.isEmpty()
                    || normalizeText(supplier.getMaNhaCungCap()).contains(keyword)
                    || normalizeText(supplier.getTenNhaCungCap()).contains(keyword)
                    || normalizeText(supplier.getSoDienThoai()).contains(keyword)
                    || normalizeText(supplier.getEmail()).contains(keyword)
                    || normalizeText(supplier.getMatHangCungCap()).contains(keyword)
                    || normalizeText(supplier.getDiaChi()).contains(keyword);

            boolean matches_status = selected_status == null
                    || "Tất cả".equals(selected_status)
                    || ("Đang hợp tác".equals(selected_status) && supplier.isTrangThai())
                    || ("Ngừng hoạt động".equals(selected_status) && !supplier.isTrangThai());

            return matches_keyword && matches_status;
        });

        lblStatus.setText("Hiển thị " + filtered_suppliers.size()
                + " / " + supplier_list.size() + " nhà cung cấp");
    }

    /**
     * Đăng ký phím F5 trên cửa sổ khi mở màn nhà cung cấp.
     */
    private void registerRefreshShortcut() {
        EventHandler<KeyEvent> refresh_handler = event -> {
            if (event.getCode() == KeyCode.F5) {
                if (!busy.get()) {
                    loadSuppliers();
                }
                event.consume();
            }
        };

        sceneProperty().addListener((observable, old_scene, new_scene) -> {
            if (old_scene != null) {
                old_scene.removeEventFilter(KeyEvent.KEY_PRESSED, refresh_handler);
            }
            if (new_scene != null) {
                new_scene.addEventFilter(KeyEvent.KEY_PRESSED, refresh_handler);
            }
        });
    }

    /**
     * Mở form thêm hoặc sửa nhà cung cấp (chuẩn hộp thoại Khách hàng).
     *
     * @param selected Nhà cung cấp cần sửa; null nếu thêm mới.
     */
    private void showSupplierDialog(Supplier selected) {
        boolean editing = selected != null;

        Dialog<ButtonType> dialog = new Dialog<>();
        configureDialog(dialog, 680);

        Label lblTitle = new Label(
                editing ? "Sửa nhà cung cấp" : "Thêm nhà cung cấp");
        lblTitle.getStyleClass().add("customer-dialog-title");

        TextField txtName = new TextField();
        TextField txtPhone = new TextField();
        TextField txtEmail = new TextField();
        TextField txtAddress = new TextField();
        TextField txtGoods = new TextField();

        txtName.setPromptText("Nhập tên nhà cung cấp...");
        txtPhone.setPromptText("Nhập số điện thoại (10 chữ số bắt đầu bằng 0)...");
        txtEmail.setPromptText("Nhập email liên hệ (ví dụ: contact@supplier.com)...");
        txtAddress.setPromptText("Nhập địa chỉ nhà cung cấp...");
        txtGoods.setPromptText("Nhập mặt hàng cung cấp (ví dụ: Thức ăn hạt, Pate, Phụ kiện)...");

        // Giới hạn số điện thoại tối đa 10 chữ số
        txtPhone.setTextFormatter(
                new TextFormatter<String>(change -> change.getControlNewText()
                        .matches("[0-9]{0,10}") ? change : null));

        if (editing) {
            txtName.setText(selected.getTenNhaCungCap());
            txtPhone.setText(selected.getSoDienThoai());
            txtEmail.setText(selected.getEmail());
            txtAddress.setText(selected.getDiaChi());
            txtGoods.setText(selected.getMatHangCungCap());
        }

        Label lblError = new Label();
        lblError.setWrapText(true);
        lblError.getStyleClass().add("customer-error");

        VBox form = new VBox(
                12,
                lblTitle,
                createField("Tên nhà cung cấp *", txtName),
                createField("Số điện thoại *", txtPhone),
                createField("Email *", txtEmail),
                createField("Địa chỉ", txtAddress),
                createField("Mặt hàng cung cấp *", txtGoods),
                lblError);

        dialog.getDialogPane().setContent(form);

        ButtonType cancel_type = new ButtonType(
                "Hủy",
                ButtonBar.ButtonData.CANCEL_CLOSE);

        ButtonType save_type = new ButtonType(
                editing ? "Cập nhật" : "Lưu nhà cung cấp",
                ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes().addAll(
                cancel_type,
                save_type);

        Button btnSave = (Button) dialog.getDialogPane().lookupButton(save_type);
        Button btnCancel = (Button) dialog.getDialogPane().lookupButton(cancel_type);

        btnSave.getStyleClass().add("customer-save");
        btnCancel.getStyleClass().add("customer-secondary");

        btnSave.disableProperty().bind(busy);
        btnCancel.disableProperty().bind(busy);

        dialog.setOnCloseRequest(event -> {
            if (busy.get()) {
                event.consume();
            }
        });

        btnSave.addEventFilter(ActionEvent.ACTION, event -> {
            event.consume();
            lblError.setText("");

            Supplier supplier = new Supplier(
                    editing ? selected.getMaNhaCungCap() : null,
                    txtName.getText(),
                    txtPhone.getText(),
                    txtEmail.getText(),
                    txtAddress.getText(),
                    txtGoods.getText(),
                    editing ? selected.isTrangThai() : true);

            form.setDisable(true);

            executeBackground(
                    () -> {
                        if (editing) {
                            supplier_controller.updateSupplier(supplier);
                        } else {
                            supplier_controller.addSupplier(supplier);
                        }
                        return null;
                    },
                    result -> {
                        dialog.setResult(save_type);
                        dialog.close();
                        loadSuppliers();
                    },
                    error -> {
                        form.setDisable(false);
                        lblError.setText(error.getMessage());
                    });
        });

        showModal(dialog);
    }

    /**
     * Tạo nhóm nhãn và ô nhập.
     */
    private VBox createField(String title, Control control) {
        Label lblField = new Label(title);
        lblField.getStyleClass().add("customer-field-label");

        control.getStyleClass().add("customer-input");
        control.setMaxWidth(Double.MAX_VALUE);

        return new VBox(
                5,
                lblField,
                control);
    }

    /**
     * Mở hộp thông tin nhà cung cấp đang chọn (Xem chi tiết).
     */
    private void showSupplierInfo() {
        Supplier selected = tblSuppliers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        configureDialog(dialog, 600);

        Label lblTitle = new Label("Thông tin nhà cung cấp");
        lblTitle.getStyleClass().add("customer-dialog-title");

        VBox content = new VBox(
                16,
                lblTitle,
                new Separator(),
                createProfileCard(selected),
                createDetailsCard(selected));

        dialog.getDialogPane().setContent(content);

        ButtonType close_type = new ButtonType(
                "Đóng",
                ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().add(close_type);

        Button btnClose = (Button) dialog.getDialogPane().lookupButton(close_type);
        btnClose.getStyleClass().add("customer-secondary");

        showModal(dialog);
    }

    /**
     * Mở hộp xác nhận ngừng hợp tác nhà cung cấp (xóa mềm).
     */
    private void showDeleteDialog() {
        Supplier selected = tblSuppliers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        configureDialog(dialog, 600);

        Label lblTitle = new Label("Ngừng hợp tác nhà cung cấp");
        lblTitle.getStyleClass().add("customer-dialog-title");

        Label lblWarning = new Label(
                "Nhà cung cấp sẽ chuyển sang trạng thái ngừng hoạt động. Thông tin và lịch sử nhập hàng được giữ nguyên.");
        lblWarning.setWrapText(true);
        lblWarning.getStyleClass().add("customer-field-label");

        Label lblError = new Label();
        lblError.setWrapText(true);
        lblError.getStyleClass().add("customer-error");

        VBox content = new VBox(
                14,
                lblTitle,
                lblWarning,
                new Separator(),
                createProfileCard(selected),
                createDetailsCard(selected),
                lblError);

        dialog.getDialogPane().setContent(content);

        ButtonType cancel_type = new ButtonType(
                "Hủy",
                ButtonBar.ButtonData.CANCEL_CLOSE);

        ButtonType delete_type = new ButtonType(
                "Xác nhận ngừng hợp tác",
                ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes().addAll(
                cancel_type,
                delete_type);

        Button btnConfirm = (Button) dialog.getDialogPane().lookupButton(delete_type);
        Button btnCancel = (Button) dialog.getDialogPane().lookupButton(cancel_type);

        btnConfirm.getStyleClass().add("customer-danger");
        btnCancel.getStyleClass().add("customer-secondary");

        btnConfirm.disableProperty().bind(busy);
        btnCancel.disableProperty().bind(busy);

        dialog.setOnCloseRequest(event -> {
            if (busy.get()) {
                event.consume();
            }
        });

        btnConfirm.addEventFilter(ActionEvent.ACTION, event -> {
            event.consume();
            lblError.setText("");

            executeBackground(
                    () -> {
                        supplier_controller.deleteSupplier(selected.getMaNhaCungCap());
                        return null;
                    },
                    result -> {
                        dialog.setResult(delete_type);
                        dialog.close();
                        loadSuppliers();
                    },
                    error -> lblError.setText(error.getMessage()));
        });

        showModal(dialog);
    }

    /**
     * Tạo thẻ avatar và tên nhà cung cấp.
     */
    private HBox createProfileCard(Supplier supplier) {
        Label lblAvatar = new Label(getInitials(supplier.getTenNhaCungCap()));
        lblAvatar.getStyleClass().add("customer-avatar");

        Label lblName = new Label(supplier.getTenNhaCungCap());
        lblName.setStyle("-fx-font-weight: bold;");
        lblName.setWrapText(true);

        Label lblActivityStatus = new Label(
                supplier.isTrangThai() ? "Đang hợp tác" : "Ngừng hoạt động");
        lblActivityStatus.getStyleClass().addAll(
                "customer-activity-badge",
                supplier.isTrangThai()
                        ? "customer-activity-active" : "customer-activity-inactive");

        VBox name_box = new VBox(7, lblName, lblActivityStatus);
        name_box.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(name_box, Priority.ALWAYS);

        HBox profile = new HBox(14, lblAvatar, name_box);
        profile.setAlignment(Pos.CENTER_LEFT);
        profile.getStyleClass().add("customer-info-card");
        return profile;
    }

    /**
     * Tạo thẻ thông tin đối tác theo bố cục lưới 3 cột tương tự Khách hàng.
     */
    private VBox createDetailsCard(Supplier supplier) {
        Label lblDetails = new Label("Thông tin nhà cung cấp");
        lblDetails.setStyle("-fx-font-weight: bold;");

        GridPane details = new GridPane();
        details.setHgap(16);
        details.setVgap(16);

        for (int index = 0; index < 3; index++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / 3);
            details.getColumnConstraints().add(column);
        }

        details.add(createDetail("Mã nhà cung cấp", supplier.getMaNhaCungCap()), 0, 0);
        details.add(createDetail("Số điện thoại", supplier.getSoDienThoai()), 1, 0);
        details.add(createDetail("Email", supplier.getEmail()), 2, 0);
        details.add(createDetail("Địa chỉ", supplier.getDiaChi()), 0, 1);
        details.add(createDetail("Mặt hàng cung cấp", supplier.getMatHangCungCap()), 1, 1);
        details.add(createDetail("Trạng thái", supplier.getTenTrangThai()), 2, 1);

        VBox detail_card = new VBox(
                14,
                lblDetails,
                details);

        detail_card.getStyleClass().add("customer-info-card");
        return detail_card;
    }

    /**
     * Tạo một trường thông tin chỉ đọc.
     */
    private VBox createDetail(String title, String value) {
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("customer-field-label");

        Label lblValue = new Label(value == null || value.isBlank() ? "—" : value);
        lblValue.setWrapText(true);
        lblValue.setMaxWidth(Double.MAX_VALUE);

        return new VBox(
                5,
                lblTitle,
                lblValue);
    }

    /**
     * Thiết lập hộp thoại không có thanh tiêu đề hệ điều hành.
     */
    private void configureDialog(Dialog<?> dialog, double width) {
        dialog.initOwner(getScene().getWindow());
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        dialog.getDialogPane().setPrefWidth(width);
        dialog.getDialogPane().getStyleClass().add("customer-dialog");

        dialog.getDialogPane().getStylesheets().addAll(
                getScene().getStylesheets());
    }

    /**
     * Mở hộp thoại và làm mờ cửa sổ phía sau.
     */
    private <T> Optional<T> showModal(Dialog<T> dialog) {
        Node owner_root = getScene().getRoot();
        Effect previous_effect = owner_root.getEffect();

        GaussianBlur blur = new GaussianBlur(6);
        blur.setInput(previous_effect);

        ColorAdjust dim = new ColorAdjust();
        dim.setBrightness(-0.25);
        dim.setInput(blur);

        owner_root.setEffect(dim);

        try {
            return dialog.showAndWait();
        } finally {
            owner_root.setEffect(previous_effect);
        }
    }

    /**
     * Chạy thao tác dữ liệu trên luồng nền.
     */
    private <T> void executeBackground(
            Callable<T> operation,
            Consumer<T> on_success,
            Consumer<Throwable> on_failure) {
        busy.set(true);

        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return operation.call();
            }
        };

        task.setOnSucceeded(event -> {
            busy.set(false);
            on_success.accept(task.getValue());
        });

        task.setOnFailed(event -> {
            busy.set(false);
            task.getException().printStackTrace();
            on_failure.accept(task.getException());
        });

        Thread worker = new Thread(task, "supplier-operation");
        worker.setDaemon(true);
        worker.start();
    }

    /**
     * Hiển thị thông báo lỗi đọc dữ liệu.
     */
    private void showError(Throwable error) {
        Alert alert = new Alert(
                Alert.AlertType.ERROR,
                error.getMessage());

        alert.initOwner(getScene().getWindow());
        alert.setTitle("Thông báo");
        alert.setHeaderText("Không thực hiện được thao tác");
        alert.showAndWait();
    }

    /**
     * Chuẩn hóa văn bản để tìm kiếm tiếng Việt không dấu.
     */
    private String normalizeText(String value) {
        return Normalizer.normalize(
                textOrEmpty(value),
                Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    /**
     * Chuyển giá trị null thành chuỗi rỗng.
     */
    private String textOrEmpty(String value) {
        return value == null ? "" : value;
    }

    /**
     * Lấy chữ cái đầu của từ đầu và từ cuối trong tên nhà cung cấp để làm avatar.
     */
    private String getInitials(String name) {
        if (name == null || name.isBlank()) {
            return "NCC";
        }
        String[] parts = name.trim().split("\\s+");
        return (parts[0].substring(0, 1)
                + (parts.length > 1
                        ? parts[parts.length - 1].substring(0, 1)
                        : ""))
                .toUpperCase(Locale.ROOT);
    }
}

