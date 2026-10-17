package com.petshop.view;

import com.petshop.controller.CustomerController;
import com.petshop.model.Customer;

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
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Giao diện quản lý khách hàng.
 *
 * CUS_UPDATE: cập nhật khách hàng.
 * CUS_SEARCH: tìm kiếm và xem thông tin khách hàng.
 * CUS_HISTORY: tạm sử dụng màn hình mẫu.
 *
 * Giao diện gọi CustomerController, không truy vấn SQL trực tiếp.
 */
public class CustomerView extends VBox {

        // Trang Cập nhật chỉ hiển thị khách hàng đang hoạt động.
        private boolean update_mode = false;

        // Controller tiếp nhận yêu cầu từ giao diện.
        private final CustomerController customer_controller = new CustomerController();

        // Danh sách khách hàng gốc được tải từ database.
        private final ObservableList<Customer> customer_list = FXCollections.observableArrayList();

        // Danh sách khách hàng sau khi áp dụng bộ lọc.
        private final FilteredList<Customer> filtered_customers = new FilteredList<>(customer_list, customer -> true);

        // Trạng thái đang tải hoặc ghi dữ liệu.
        private final SimpleBooleanProperty busy = new SimpleBooleanProperty(false);

        // Bảng hiển thị thông tin khách hàng.
        private final TableView<Customer> tblCustomers = new TableView<>();

        // Ô nhập từ khóa trên trang tìm kiếm.
        private final TextField txtSearch = new TextField();

        // Các nút trên trang cập nhật.
        private final Button btnAdd = new Button("＋ Thêm khách hàng");

        private final Button btnEdit = new Button("Sửa khách hàng");

        private final Button btnDelete = new Button("Ngừng hoạt động");

        // Các nút trên trang tìm kiếm.
        private final Button btnSearch = new Button("Tìm kiếm");

        private final Button btnViewInfo = new Button("Xem thông tin");

        private final Button btnRefresh = new Button("Làm mới");

        // Lọc khách hàng theo trạng thái trên trang Tìm kiếm.
        private final ComboBox<String> cboCustomerStatus = new ComboBox<>();

        // Nhãn thông báo số lượng hoặc trạng thái xử lý.
        private final Label lblStatus = new Label();

        // Từ khóa đã xác nhận bằng nút Tìm kiếm hoặc phím Enter.
        private String search_keyword = "";

        /**
         * Khởi tạo màn hình theo chức năng được chọn.
         *
         * @param action_id Mã chức năng từ MenuRegistry.
         */
        public CustomerView(String action_id) {
                // Mở trang tìm kiếm khách hàng.
                if ("CUS_SEARCH".equals(action_id)) {
                        initializeSearchScreen();
                        return;
                }

                // Chức năng chưa triển khai tiếp tục dùng màn hình mẫu.
                if (!"CUS_UPDATE".equals(action_id)) {
                        // Màn lịch sử giao dịch sẽ được triển khai sau.
                        ModuleListView placeholder = new ModuleListView(
                                        "Khách hàng",
                                        "Lịch sử giao dịch",
                                        null);

                        VBox.setVgrow(placeholder, Priority.ALWAYS);
                        getChildren().add(placeholder);
                        return;
                }

                initializeUpdateScreen();
        }

        /**
         * Dựng trang cập nhật khách hàng.
         * Trang này có các nút Thêm, Sửa và Ngừng hoạt động.
         */
        private void initializeUpdateScreen() {
                update_mode = true;
                getStyleClass().add("customer-page");
                setSpacing(20);
                setPadding(new Insets(28));

                // Tiêu đề của trang cập nhật.
                Label lblTitle = new Label("Cập nhật khách hàng");
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
                tblCustomers.disableProperty().bind(busy);

                // Chỉ cho sửa hoặc xóa khi có khách hàng được chọn.
                btnEdit.disableProperty().bind(
                                busy.or(
                                                tblCustomers.getSelectionModel()
                                                                .selectedItemProperty()
                                                                .isNull()));

                btnDelete.disableProperty().bind(
                                busy.or(
                                                tblCustomers.getSelectionModel()
                                                                .selectedItemProperty()
                                                                .isNull()));

                btnAdd.setOnAction(event -> showCustomerDialog(null));

                btnEdit.setOnAction(event -> showCustomerDialog(
                                tblCustomers.getSelectionModel().getSelectedItem()));

                btnDelete.setOnAction(event -> showDeleteDialog());

                registerRefreshShortcut();

                getChildren().addAll(
                                header,
                                table_card,
                                lblStatus);

                loadCustomers();
        }

        /**
         * Dựng trang tìm kiếm khách hàng.
         * Tìm khi bấm nút Tìm kiếm hoặc nhấn Enter.
         */
        private void initializeSearchScreen() {
                getStyleClass().addAll(
                                "customer-page",
                                "customer-search-page");

                setSpacing(20);
                setPadding(new Insets(28));

                // Tiêu đề trang tìm kiếm.
                Label lblTitle = new Label("Tìm kiếm khách hàng");
                lblTitle.getStyleClass().add("customer-title");

                // Nhãn mô tả tiêu chí tìm kiếm.
                Label lblSearch = new Label("Tìm kiếm khách hàng");
                lblSearch.getStyleClass().add("customer-search-label");

                txtSearch.setPromptText(
                                "Tìm kiếm theo mã, tên, số điện thoại, email...");
                txtSearch.setPrefWidth(440);
                txtSearch.setMinWidth(220);
                txtSearch.getStyleClass().add("customer-search");

                btnSearch.getStyleClass().add("customer-save");
                btnViewInfo.getStyleClass().add("customer-secondary");
                btnRefresh.getStyleClass().add("customer-secondary");

                // Khoảng trống đẩy nhóm nút sang bên phải.
                Region search_spacer = new Region();
                HBox.setHgrow(search_spacer, Priority.ALWAYS);

                // Ba lựa chọn trạng thái, mặc định tra cứu tất cả khách hàng.
                cboCustomerStatus.getItems().addAll(
                                "Tất cả", "Đang hoạt động", "Ngừng hoạt động");
                cboCustomerStatus.setValue("Tất cả");
                cboCustomerStatus.setPrefWidth(165);
                cboCustomerStatus.getStyleClass().add("customer-status-filter");

                // Đặt nút Tìm kiếm sát ô nhập theo mẫu Figma.
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
                                cboCustomerStatus,
                                btnRefresh);
                search_bar.setAlignment(Pos.CENTER_LEFT);

                // Khu vực nhập tiêu chí tìm kiếm.
                VBox search_section = new VBox(
                                10,
                                lblSearch,
                                search_bar);

                configureTable();

                // Thẻ trắng chứa bảng kết quả.
                VBox table_card = createTableCard();

                lblStatus.getStyleClass().add("customer-status");

                // Không cho thao tác khi đang tải dữ liệu.
                btnSearch.disableProperty().bind(busy);
                cboCustomerStatus.disableProperty().bind(busy);
                btnRefresh.disableProperty().bind(busy);
                txtSearch.disableProperty().bind(busy);
                tblCustomers.disableProperty().bind(busy);

                // Chỉ cho xem thông tin khi có dòng được chọn.
                btnViewInfo.disableProperty().bind(
                                busy.or(
                                                tblCustomers.getSelectionModel()
                                                                .selectedItemProperty()
                                                                .isNull()));

                // Nút Tìm kiếm và Enter có cùng hành vi.
                btnSearch.setOnAction(event -> searchCustomers());
                txtSearch.setOnAction(event -> searchCustomers());

                btnViewInfo.setOnAction(event -> showCustomerInfo());

                // Đổi trạng thái sẽ lọc ngay trên từ khóa đã tìm trước đó.
                cboCustomerStatus.setOnAction(event -> {
                        tblCustomers.getSelectionModel().clearSelection();
                        filterCustomers();
                });

                // Làm mới sẽ xóa bộ lọc và tải lại dữ liệu.
                btnRefresh.setOnAction(event -> {
                        txtSearch.clear();
                        search_keyword = "";
                        cboCustomerStatus.setValue("Tất cả");

                        tblCustomers.getSelectionModel().clearSelection();

                        loadCustomers();
                });

                registerRefreshShortcut();

                getChildren().addAll(
                                lblTitle,
                                search_section,
                                table_card,
                                lblStatus);

                loadCustomers();
        }

        /**
         * Thiết lập bảng dữ liệu dùng chung cho hai trang.
         */
        private void configureTable() {
                createColumns();

                tblCustomers.setItems(filtered_customers);
                tblCustomers.getStyleClass().add("customer-table");

                tblCustomers.setColumnResizePolicy(
                                TableView.CONSTRAINED_RESIZE_POLICY);

                tblCustomers.setPlaceholder(
                                new Label("Không có khách hàng phù hợp"));
        }

        /**
         * Tạo thẻ trắng chứa bảng khách hàng.
         *
         * @return Khung chứa bảng đã được thiết lập.
         */
        private VBox createTableCard() {
                // Khung hiển thị bảng dữ liệu.
                VBox table_card = new VBox(tblCustomers);

                table_card.setPadding(new Insets(20));
                table_card.getStyleClass().add("customer-card");

                VBox.setVgrow(tblCustomers, Priority.ALWAYS);
                VBox.setVgrow(table_card, Priority.ALWAYS);

                return table_card;
        }

        /**
         * Tạo các cột theo thứ tự trong mẫu Figma.
         */
        private void createColumns() {
                tblCustomers.getColumns().clear();

                tblCustomers.getColumns().add(
                                createColumn(
                                                "Mã KH",
                                                Customer::getMaKhachHang,
                                                170));

                tblCustomers.getColumns().add(
                                createColumn(
                                                "Họ tên",
                                                Customer::getHoTen,
                                                160));

                tblCustomers.getColumns().add(
                                createColumn(
                                                "Email",
                                                Customer::getEmail,
                                                190));

                tblCustomers.getColumns().add(
                                createColumn(
                                                "Số điện thoại",
                                                Customer::getSoDienThoai,
                                                135));

                tblCustomers.getColumns().add(
                                createColumn(
                                                "Địa chỉ",
                                                Customer::getDiaChi,
                                                220));

                tblCustomers.getColumns().add(
                                createColumn(
                                                "Điểm tích lũy",
                                                customer -> String.valueOf(
                                                                customer.getDiemTichLuy()),
                                                105));

                tblCustomers.getColumns().add(
                                createColumn(
                                                "Ghi chú",
                                                Customer::getGhiChu,
                                                200));
        }

        /**
         * Tạo cột có nội dung tự xuống dòng.
         *
         * @param title  Tiêu đề cột.
         * @param getter Hàm lấy dữ liệu từ Customer.
         * @param width  Chiều rộng ưu tiên.
         * @return Cột hiển thị văn bản.
         */
        private TableColumn<Customer, String> createColumn(
                        String title,
                        Function<Customer, String> getter,
                        double width) {
                // Cột dữ liệu đang được tạo.
                TableColumn<Customer, String> column = new TableColumn<>(title);

                column.setPrefWidth(width);
                column.setMinWidth(85);
                column.setSortable(false);

                column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                                textOrEmpty(
                                                getter.apply(cell.getValue()))));

                column.setCellFactory(table_column -> new TableCell<>() {

                        // Nhãn hiển thị nội dung trong ô.
                        private final Label lblValue = new Label();

                        {
                                lblValue.setWrapText(true);
                                lblValue.getStyleClass().add("customer-cell-text");

                                lblValue.prefWidthProperty().bind(
                                                widthProperty().subtract(20));
                        }

                        @Override
                        protected void updateItem(String value, boolean empty) {
                                super.updateItem(value, empty);
                                setText(null);

                                // Xóa nội dung khi ô được tái sử dụng cho dòng trống.
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
         * Đọc lại danh sách khách hàng ở luồng nền.
         * Giữ nguyên từ khóa đã được xác nhận.
         */
        private void loadCustomers() {
                lblStatus.setText("Đang tải khách hàng...");

                executeBackground(
                                () -> update_mode
                                        ? customer_controller.loadActiveCustomers()
                                        : customer_controller.loadCustomers(),
                                customers -> {
                                        tblCustomers.getSelectionModel().clearSelection();

                                        customer_list.setAll(customers);
                                        filterCustomers();
                                },
                                error -> {
                                        lblStatus.setText("Không tải được dữ liệu");
                                        showError(error);
                                });
        }

        /**
         * Xác nhận từ khóa và thực hiện tìm kiếm.
         * Từ khóa rỗng sẽ hiển thị toàn bộ danh sách.
         */
        private void searchCustomers() {
                // Không tìm trong lúc danh sách đang được tải.
                if (busy.get()) {
                        return;
                }

                search_keyword = txtSearch.getText().trim();

                tblCustomers.getSelectionModel().clearSelection();

                filterCustomers();
        }

        /**
         * Lọc tất cả khách hàng khớp mã, tên, số điện thoại hoặc email.
         * Không phân biệt chữ hoa, chữ thường và dấu tiếng Việt.
         */
        private void filterCustomers() {
                // Từ khóa chỉ thay đổi sau khi bấm Tìm kiếm hoặc Enter.
                String keyword = normalizeText(search_keyword);

                // Trang Cập nhật không áp dụng bộ lọc của trang Tìm kiếm.
                String selected_status = update_mode
                                ? "Tất cả" : cboCustomerStatus.getValue();

                filtered_customers.setPredicate(customer -> {
                        // Kiểm tra thông tin khớp từ khóa, không phân biệt dấu.
                        boolean matches_keyword = keyword.isEmpty()
                                        || normalizeText(customer.getMaKhachHang()).contains(keyword)
                                        || normalizeText(customer.getHoTen()).contains(keyword)
                                        || normalizeText(customer.getSoDienThoai()).contains(keyword)
                                        || normalizeText(customer.getEmail()).contains(keyword);

                        // Kết hợp từ khóa VÀ trạng thái, không lọc từng điều kiện riêng.
                        boolean matches_status = selected_status == null
                                        || "Tất cả".equals(selected_status)
                                        || ("Đang hoạt động".equals(selected_status)
                                                && customer.isTrangThai())
                                        || ("Ngừng hoạt động".equals(selected_status)
                                                && !customer.isTrangThai());

                        return matches_keyword && matches_status;
                });

                lblStatus.setText("Hiển thị " + filtered_customers.size()
                                + " / " + customer_list.size() + " khách hàng");
        }

        /**
         * Đăng ký phím F5 trên cửa sổ khi mở màn khách hàng.
         * Gỡ xử lý phím khi rời màn hình.
         */
        private void registerRefreshShortcut() {
                // Bộ xử lý phím tắt tải lại dữ liệu.
                EventHandler<KeyEvent> refresh_handler = event -> {
                        if (event.getCode() == KeyCode.F5) {
                                if (!busy.get()) {
                                        loadCustomers();
                                }

                                event.consume();
                        }
                };

                sceneProperty().addListener(
                                (observable, old_scene, new_scene) -> {
                                        if (old_scene != null) {
                                                old_scene.removeEventFilter(
                                                                KeyEvent.KEY_PRESSED,
                                                                refresh_handler);
                                        }

                                        if (new_scene != null) {
                                                new_scene.addEventFilter(
                                                                KeyEvent.KEY_PRESSED,
                                                                refresh_handler);
                                        }
                                });
        }

        /**
         * Mở form thêm hoặc sửa khách hàng.
         *
         * @param selected Khách hàng cần sửa; null nếu thêm mới.
         */
        private void showCustomerDialog(Customer selected) {
                // Xác định form thêm hay sửa.
                boolean editing = selected != null;

                // Hộp thoại nhập liệu.
                Dialog<ButtonType> dialog = new Dialog<>();
                configureDialog(dialog, 680);

                // Tiêu đề của form.
                Label lblTitle = new Label(
                                editing ? "Sửa khách hàng" : "Thêm khách hàng");
                lblTitle.getStyleClass().add("customer-dialog-title");

                // Các ô nhập thông tin khách hàng.
                TextField txtName = new TextField();
                TextField txtPhone = new TextField();
                TextField txtEmail = new TextField();
                TextField txtAddress = new TextField();
                TextField txtNote = new TextField();

                txtName.setPromptText("Nhập họ tên...");
                txtPhone.setPromptText("Nhập số điện thoại...");
                txtEmail.setPromptText("Nhập email...");
                txtAddress.setPromptText("Nhập địa chỉ...");
                txtNote.setPromptText("Nhập ghi chú...");

                // Chỉ cho nhập tối đa 10 chữ số.
                txtPhone.setTextFormatter(
                                new TextFormatter<String>(change -> change.getControlNewText()
                                                .matches("[0-9]{0,10}")
                                                                ? change
                                                                : null));

                // Đổ dữ liệu cũ vào form khi sửa.
                if (editing) {
                        txtName.setText(selected.getHoTen());
                        txtPhone.setText(selected.getSoDienThoai());
                        txtEmail.setText(selected.getEmail());
                        txtAddress.setText(selected.getDiaChi());
                        txtNote.setText(selected.getGhiChu());
                }

                // Nhãn lỗi nhập liệu hoặc lỗi lưu database.
                Label lblError = new Label();
                lblError.setWrapText(true);
                lblError.getStyleClass().add("customer-error");

                // Form một cột theo mẫu.
                VBox form = new VBox(
                                12,
                                lblTitle,
                                createField("Họ tên *", txtName),
                                createField("Số điện thoại *", txtPhone),
                                createField("Email", txtEmail),
                                createField("Địa chỉ", txtAddress),
                                createField("Ghi chú", txtNote),
                                lblError);

                dialog.getDialogPane().setContent(form);

                // Loại nút Hủy và Lưu.
                ButtonType cancel_type = new ButtonType(
                                "Hủy",
                                ButtonBar.ButtonData.CANCEL_CLOSE);

                ButtonType save_type = new ButtonType(
                                editing ? "Cập nhật" : "Lưu khách hàng",
                                ButtonBar.ButtonData.OK_DONE);

                dialog.getDialogPane().getButtonTypes().addAll(
                                cancel_type,
                                save_type);

                // Các nút thực tế trong DialogPane.
                Button btnSave = (Button) dialog.getDialogPane()
                                .lookupButton(save_type);

                Button btnCancel = (Button) dialog.getDialogPane()
                                .lookupButton(cancel_type);

                btnSave.getStyleClass().add("customer-save");
                btnCancel.getStyleClass().add("customer-secondary");

                btnSave.disableProperty().bind(busy);
                btnCancel.disableProperty().bind(busy);

                // Không cho đóng form khi đang lưu dữ liệu.
                dialog.setOnCloseRequest(event -> {
                        if (busy.get()) {
                                event.consume();
                        }
                });

                btnSave.addEventFilter(ActionEvent.ACTION, event -> {
                        // Không đóng form trước khi database lưu thành công.
                        event.consume();
                        lblError.setText("");

                        // Tạo bản dữ liệu mới từ các ô nhập.
                        Customer customer = new Customer(
                                        editing ? selected.getMaKhachHang() : null,
                                        txtName.getText(),
                                        txtPhone.getText(),
                                        txtEmail.getText(),
                                        editing ? selected.getDiemTichLuy() : 0,
                                        txtAddress.getText(),
                                        txtNote.getText());

                        form.setDisable(true);

                        executeBackground(
                                        () -> {
                                                if (editing) {
                                                        customer_controller.updateCustomer(customer);
                                                } else {
                                                        customer_controller.addCustomer(customer);
                                                }

                                                return null;
                                        },
                                        result -> {
                                                dialog.setResult(save_type);
                                                dialog.close();

                                                loadCustomers();
                                        },
                                        error -> {
                                                // Giữ nguyên dữ liệu để người dùng sửa và thử lại.
                                                form.setDisable(false);
                                                lblError.setText(error.getMessage());
                                        });
                });

                showModal(dialog);
        }

        /**
         * Tạo nhóm nhãn và ô nhập.
         *
         * @param title   Nội dung nhãn.
         * @param control Điều khiển nhập liệu.
         * @return Khung nhãn phía trên và ô nhập phía dưới.
         */
        private VBox createField(String title, Control control) {
                // Nhãn mô tả trường nhập.
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
         * Mở hộp thông tin khách hàng đang chọn.
         * Chỉ cho xem, không cho sửa dữ liệu trong hộp thoại này.
         */
        private void showCustomerInfo() {
                // Khách hàng được chọn trong bảng.
                Customer selected = tblCustomers.getSelectionModel().getSelectedItem();

                if (selected == null) {
                        return;
                }

                // Hộp thoại thông tin khách hàng.
                Dialog<ButtonType> dialog = new Dialog<>();
                configureDialog(dialog, 600);

                // Tiêu đề hộp thoại.
                Label lblTitle = new Label("Thông tin khách hàng");
                lblTitle.getStyleClass().add("customer-dialog-title");

                // Nội dung thông tin dùng chung với hộp xác nhận xóa.
                VBox content = new VBox(
                                16,
                                lblTitle,
                                new Separator(),
                                createProfileCard(selected),
                                createDetailsCard(selected));

                dialog.getDialogPane().setContent(content);

                // Hộp thông tin chỉ có nút Đóng.
                ButtonType close_type = new ButtonType(
                                "Đóng",
                                ButtonBar.ButtonData.CANCEL_CLOSE);

                dialog.getDialogPane().getButtonTypes().add(close_type);

                // Nút Đóng thực tế để áp dụng CSS.
                Button btnClose = (Button) dialog.getDialogPane()
                                .lookupButton(close_type);

                btnClose.getStyleClass().add("customer-secondary");

                showModal(dialog);
        }

        /**
         * Mở hộp xác nhận ngừng hoạt động khách hàng.
         * Chỉ ngừng hoạt động sau khi người dùng xác nhận.
         */
        private void showDeleteDialog() {
                // Khách hàng được chọn trong bảng.
                Customer selected = tblCustomers.getSelectionModel().getSelectedItem();

                if (selected == null) {
                        return;
                }

                // Hộp thoại xác nhận ngừng hoạt động bằng xóa mềm.
                Dialog<ButtonType> dialog = new Dialog<>();
                configureDialog(dialog, 600);

                // Tiêu đề hộp thoại.
                Label lblTitle = new Label("Ngừng hoạt động khách hàng");
                lblTitle.getStyleClass().add("customer-dialog-title");

                // Cảnh báo về hành động xóa.
                Label lblWarning = new Label(
                                "Khách hàng sẽ ngừng hoạt động. Thông tin và lịch sử giao dịch được giữ nguyên.");
                lblWarning.setWrapText(true);
                lblWarning.getStyleClass().add("customer-field-label");

                // Nhãn báo lỗi nếu database không cho phép xóa.
                Label lblError = new Label();
                lblError.setWrapText(true);
                lblError.getStyleClass().add("customer-error");

                // Nội dung xác nhận và thông tin khách hàng.
                VBox content = new VBox(
                                14,
                                lblTitle,
                                lblWarning,
                                new Separator(),
                                createProfileCard(selected),
                                createDetailsCard(selected),
                                lblError);

                dialog.getDialogPane().setContent(content);

                // Loại nút Hủy và Xác nhận xóa.
                ButtonType cancel_type = new ButtonType(
                                "Hủy",
                                ButtonBar.ButtonData.CANCEL_CLOSE);

                ButtonType delete_type = new ButtonType(
                                "Xác nhận ngừng hoạt động",
                                ButtonBar.ButtonData.OK_DONE);

                dialog.getDialogPane().getButtonTypes().addAll(
                                cancel_type,
                                delete_type);

                // Các nút thực tế của hộp thoại.
                Button btnConfirm = (Button) dialog.getDialogPane()
                                .lookupButton(delete_type);

                Button btnCancel = (Button) dialog.getDialogPane()
                                .lookupButton(cancel_type);

                btnConfirm.getStyleClass().add("customer-danger");
                btnCancel.getStyleClass().add("customer-secondary");

                btnConfirm.disableProperty().bind(busy);
                btnCancel.disableProperty().bind(busy);

                // Không cho đóng hộp thoại khi đang cập nhật trạng thái.
                dialog.setOnCloseRequest(event -> {
                        if (busy.get()) {
                                event.consume();
                        }
                });

                btnConfirm.addEventFilter(ActionEvent.ACTION, event -> {
                        // Chỉ đóng hộp thoại khi ngừng hoạt động thành công.
                        event.consume();
                        lblError.setText("");

                        executeBackground(
                                        () -> {
                                                customer_controller.deleteCustomer(
                                                                selected.getMaKhachHang());

                                                return null;
                                        },
                                        result -> {
                                                dialog.setResult(delete_type);
                                                dialog.close();

                                                loadCustomers();
                                        },
                                        error -> lblError.setText(error.getMessage()));
                });

                showModal(dialog);
        }

        /**
         * Tạo thẻ avatar và tên khách hàng.
         *
         * @param customer Khách hàng cần hiển thị.
         * @return Thẻ chứa avatar và họ tên.
         */
        private HBox createProfileCard(Customer customer) {
                // Avatar lấy chữ viết tắt từ họ tên.
                Label lblAvatar = new Label(getInitials(customer.getHoTen()));
                lblAvatar.getStyleClass().add("customer-avatar");

                // Tên khách hàng và trạng thái được lấy từ dữ liệu SQL.
                Label lblName = new Label(customer.getHoTen());
                lblName.setStyle("-fx-font-weight: bold;");
                lblName.setWrapText(true);

                Label lblActivityStatus = new Label(
                                customer.isTrangThai() ? "Đang hoạt động" : "Ngừng hoạt động");
                lblActivityStatus.getStyleClass().addAll(
                                "customer-activity-badge",
                                customer.isTrangThai()
                                        ? "customer-activity-active" : "customer-activity-inactive");

                // Badge dưới tên, dùng chung cho hộp thông tin và xác nhận ngừng hoạt động.
                VBox name_box = new VBox(7, lblName, lblActivityStatus);
                name_box.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(name_box, Priority.ALWAYS);

                HBox profile = new HBox(14, lblAvatar, name_box);
                profile.setAlignment(Pos.CENTER_LEFT);
                profile.getStyleClass().add("customer-info-card");
                return profile;
        }

        /**
         * Tạo thẻ thông tin cá nhân theo bố cục ba cột.
         *
         * @param customer Khách hàng cần hiển thị.
         * @return Thẻ chứa thông tin cá nhân.
         */
        private VBox createDetailsCard(Customer customer) {
                // Tiêu đề khu vực thông tin.
                Label lblDetails = new Label("Thông tin cá nhân");
                lblDetails.setStyle("-fx-font-weight: bold;");

                // Lưới thông tin gồm ba cột và hai hàng.
                GridPane details = new GridPane();
                details.setHgap(16);
                details.setVgap(16);

                for (int index = 0; index < 3; index++) {
                        // Mỗi cột chiếm một phần ba chiều rộng.
                        ColumnConstraints column = new ColumnConstraints();
                        column.setPercentWidth(100.0 / 3);

                        details.getColumnConstraints().add(column);
                }

                details.add(
                                createDetail(
                                                "Mã khách hàng",
                                                customer.getMaKhachHang()),
                                0,
                                0);

                details.add(
                                createDetail(
                                                "Số điện thoại",
                                                customer.getSoDienThoai()),
                                1,
                                0);

                details.add(
                                createDetail(
                                                "Email",
                                                customer.getEmail()),
                                2,
                                0);

                details.add(
                                createDetail(
                                                "Địa chỉ",
                                                customer.getDiaChi()),
                                0,
                                1);

                details.add(
                                createDetail(
                                                "Điểm tích lũy",
                                                String.valueOf(customer.getDiemTichLuy())),
                                1,
                                1);

                details.add(
                                createDetail(
                                                "Ghi chú",
                                                customer.getGhiChu()),
                                2,
                                1);

                // Thẻ chứa tiêu đề và lưới thông tin.
                VBox detail_card = new VBox(
                                14,
                                lblDetails,
                                details);

                detail_card.getStyleClass().add("customer-info-card");

                return detail_card;
        }

        /**
         * Tạo một trường thông tin chỉ đọc.
         *
         * @param title Tên trường thông tin.
         * @param value Giá trị hiển thị.
         * @return Khung chứa tên trường và giá trị.
         */
        private VBox createDetail(String title, String value) {
                // Nhãn mô tả trường thông tin.
                Label lblTitle = new Label(title);
                lblTitle.getStyleClass().add("customer-field-label");

                // Giá trị hiển thị; dùng dấu gạch nếu chưa có thông tin.
                Label lblValue = new Label(
                                value == null || value.isBlank() ? "—" : value);

                lblValue.setWrapText(true);
                lblValue.setMaxWidth(Double.MAX_VALUE);

                return new VBox(
                                5,
                                lblTitle,
                                lblValue);
        }

        /**
         * Thiết lập hộp thoại không có thanh tiêu đề hệ điều hành.
         *
         * @param dialog Hộp thoại cần thiết lập.
         * @param width  Chiều rộng ưu tiên.
         */
        private void configureDialog(Dialog<?> dialog, double width) {
                dialog.initOwner(getScene().getWindow());
                dialog.initModality(Modality.WINDOW_MODAL);
                dialog.initStyle(StageStyle.UNDECORATED);

                dialog.getDialogPane().setPrefWidth(width);
                dialog.getDialogPane().getStyleClass()
                                .add("customer-dialog");

                // Dùng stylesheet của cửa sổ chính.
                dialog.getDialogPane().getStylesheets().addAll(
                                getScene().getStylesheets());
        }

        /**
         * Mở hộp thoại và làm mờ cửa sổ phía sau.
         *
         * @param dialog Hộp thoại cần mở.
         * @param <T>    Kiểu kết quả của hộp thoại.
         * @return Kết quả khi hộp thoại đóng.
         */
        private <T> Optional<T> showModal(Dialog<T> dialog) {
                // Gốc giao diện chính.
                Node owner_root = getScene().getRoot();

                // Hiệu ứng đang có trước khi mở hộp thoại.
                Effect previous_effect = owner_root.getEffect();

                // Hiệu ứng làm mờ nền.
                GaussianBlur blur = new GaussianBlur(6);
                blur.setInput(previous_effect);

                // Hiệu ứng giảm độ sáng của nền.
                ColorAdjust dim = new ColorAdjust();
                dim.setBrightness(-0.25);
                dim.setInput(blur);

                owner_root.setEffect(dim);

                try {
                        return dialog.showAndWait();
                } finally {
                        // Khôi phục hiệu ứng cũ sau khi đóng hộp thoại.
                        owner_root.setEffect(previous_effect);
                }
        }

        /**
         * Chạy thao tác dữ liệu trên luồng nền.
         *
         * @param operation  Thao tác gọi Controller.
         * @param on_success Xử lý kết quả trên luồng giao diện.
         * @param on_failure Xử lý lỗi trên luồng giao diện.
         * @param <T>        Kiểu dữ liệu trả về.
         */
        private <T> void executeBackground(
                        Supplier<T> operation,
                        Consumer<T> on_success,
                        Consumer<Throwable> on_failure) {
                busy.set(true);

                // Tác vụ thực hiện ngoài luồng giao diện.
                Task<T> task = new Task<>() {
                        @Override
                        protected T call() {
                                return operation.get();
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

                // Luồng nền tự kết thúc cùng ứng dụng.
                Thread worker = new Thread(
                                task,
                                "customer-operation");

                worker.setDaemon(true);
                worker.start();
        }

        /**
         * Hiển thị thông báo lỗi đọc dữ liệu.
         *
         * @param error Lỗi cần thông báo.
         */
        private void showError(Throwable error) {
                // Thông báo lỗi dành cho thao tác ngoài form.
                Alert alert = new Alert(
                                Alert.AlertType.ERROR,
                                error.getMessage());

                alert.initOwner(getScene().getWindow());
                alert.setTitle("Thông báo");
                alert.setHeaderText("Không thực hiện được thao tác");

                alert.showAndWait();
        }

        /**
         * Chuẩn hóa văn bản để tìm kiếm.
         *
         * @param value Văn bản cần chuẩn hóa.
         * @return Chuỗi chữ thường, không dấu, bỏ khoảng trắng đầu cuối.
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
         *
         * @param value Chuỗi cần kiểm tra.
         * @return Chuỗi ban đầu hoặc chuỗi rỗng.
         */
        private String textOrEmpty(String value) {
                return value == null ? "" : value;
        }

        /**
         * Lấy chữ cái đầu của từ đầu và từ cuối trong họ tên.
         *
         * @param name Họ tên khách hàng.
         * @return Chữ viết tắt dùng làm avatar.
         */
        private String getInitials(String name) {
                if (name == null || name.isBlank()) {
                        return "KH";
                }

                // Các từ trong họ tên sau khi chuẩn hóa khoảng trắng.
                String[] parts = name.trim().split("\\s+");

                return (parts[0].substring(0, 1)
                                + (parts.length > 1
                                                ? parts[parts.length - 1].substring(0, 1)
                                                : ""))
                                .toUpperCase(Locale.ROOT);
        }
}