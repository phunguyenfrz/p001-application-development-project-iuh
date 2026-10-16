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
 * Giao diện cập nhật khách hàng.
 * Gọi CustomerController để đọc, thêm, sửa và xóa dữ liệu.
 */
public class CustomerView extends VBox {

    // Controller tiếp nhận yêu cầu từ giao diện.
    private final CustomerController customer_controller =
            new CustomerController();

    // Danh sách gốc lấy từ database.
    private final ObservableList<Customer> customer_list =
            FXCollections.observableArrayList();

    // Danh sách sau khi áp dụng từ khóa tìm kiếm.
    private final FilteredList<Customer> filtered_customers =
            new FilteredList<>(customer_list, customer -> true);

    // Trạng thái đang đọc hoặc lưu dữ liệu.
    private final SimpleBooleanProperty busy =
            new SimpleBooleanProperty(false);

    // Các điều khiển của màn hình.
    private final TableView<Customer> tblCustomers = new TableView<>();
    private final TextField txtSearch = new TextField();
    private final Button btnAdd = new Button("＋ Thêm khách hàng");
    private final Button btnEdit = new Button("Sửa khách hàng");
    private final Button btnDelete = new Button("Xóa");
    private final Button btnRefresh = new Button("Làm mới (F5)");
    private final Label lblStatus = new Label();

    /**
     * Khởi tạo màn hình theo chức năng được chọn.
     *
     * @param action_id Mã chức năng từ MenuRegistry.
     */
    
    public CustomerView(String action_id) {
        /*
         * Các chức năng tìm kiếm riêng và lịch sử chưa triển khai.
         * Tiếp tục dùng màn mẫu cho hai chức năng đó.
         */
        // In thông tin để xác định màn hình đang được mở.
        System.out.println("MO CUSTOMER VIEW MOI: " + action_id);
        if (!"CUS_UPDATE".equals(action_id)) {
            // Tên chức năng được hiển thị trên màn mẫu.
            String action_name = "CUS_SEARCH".equals(action_id)
                    ? "Tìm kiếm khách hàng"
                    : "Lịch sử giao dịch";

            // Màn mẫu hiện có của nhóm.
            ModuleListView placeholder = new ModuleListView(
                    "Khách hàng", action_name, null
            );

            VBox.setVgrow(placeholder, Priority.ALWAYS);
            getChildren().add(placeholder);
            return;
        }

        getStyleClass().add("customer-page");
        setSpacing(20);
        setPadding(new Insets(28));

        // Tiêu đề và nhóm nút trên cùng.
        Label lblTitle = new Label("Cập nhật khách hàng");
        lblTitle.getStyleClass().add("customer-title");

        // Khoảng trống đẩy nhóm nút sang phải.
        Region header_spacer = new Region();
        HBox.setHgrow(header_spacer, Priority.ALWAYS);

        btnAdd.getStyleClass().add("customer-add");
        btnEdit.getStyleClass().add("customer-secondary");
        btnDelete.getStyleClass().add("customer-danger");
        btnRefresh.getStyleClass().add("customer-secondary");

        // Thanh tiêu đề theo bố cục Figma.
        HBox header = new HBox(
                10, lblTitle, header_spacer,
                btnAdd, btnEdit, btnDelete
        );
        header.setAlignment(Pos.CENTER_LEFT);

        txtSearch.setPromptText("Tìm mã, họ tên, số điện thoại, email...");
        txtSearch.setPrefWidth(330);
        txtSearch.getStyleClass().add("customer-search");

        // Khoảng trống giữa ô tìm kiếm và nút Làm mới.
        Region search_spacer = new Region();
        HBox.setHgrow(search_spacer, Priority.ALWAYS);

        // Giữ tìm nhanh và Làm mới theo yêu cầu trước đó.
        HBox search_bar = new HBox(
                12, txtSearch, search_spacer, btnRefresh
        );
        search_bar.setAlignment(Pos.CENTER_LEFT);

        createColumns();

        tblCustomers.setItems(filtered_customers);
        tblCustomers.getStyleClass().add("customer-table");
        tblCustomers.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );
        tblCustomers.setPlaceholder(
                new Label("Không có khách hàng phù hợp")
        );

        // Thẻ trắng chứa bảng khách hàng.
        VBox table_card = new VBox(tblCustomers);
        table_card.setPadding(new Insets(20));
        table_card.getStyleClass().add("customer-card");

        VBox.setVgrow(tblCustomers, Priority.ALWAYS);
        VBox.setVgrow(table_card, Priority.ALWAYS);

        lblStatus.getStyleClass().add("customer-status");

        // Khóa các thao tác trong lúc truy vấn hoặc ghi database.
        btnAdd.disableProperty().bind(busy);
        btnRefresh.disableProperty().bind(busy);
        txtSearch.disableProperty().bind(busy);
        tblCustomers.disableProperty().bind(busy);

        // Chỉ cho sửa hoặc xóa khi có khách hàng được chọn.
        btnEdit.disableProperty().bind(
                busy.or(tblCustomers.getSelectionModel()
                        .selectedItemProperty().isNull())
        );
        btnDelete.disableProperty().bind(
                busy.or(tblCustomers.getSelectionModel()
                        .selectedItemProperty().isNull())
        );

        btnAdd.setOnAction(event -> showCustomerDialog(null));

        btnEdit.setOnAction(event -> showCustomerDialog(
                tblCustomers.getSelectionModel().getSelectedItem()
        ));

        btnDelete.setOnAction(event -> showDeleteDialog());
        btnRefresh.setOnAction(event -> loadCustomers());

        txtSearch.textProperty().addListener(
                (observable, old_value, new_value) -> filterCustomers()
        );

        registerRefreshShortcut();

        getChildren().addAll(
        header, table_card, lblStatus
        );

        loadCustomers();
    }

    /** Tạo các cột theo thứ tự trong mẫu Figma. */
    private void createColumns() {
        tblCustomers.getColumns().add(
                createColumn("Mã KH", Customer::getMaKhachHang, 170)
        );
        tblCustomers.getColumns().add(
                createColumn("Họ tên", Customer::getHoTen, 160)
        );
        tblCustomers.getColumns().add(
                createColumn("Email", Customer::getEmail, 190)
        );
        tblCustomers.getColumns().add(
                createColumn("Số điện thoại", Customer::getSoDienThoai, 135)
        );
        tblCustomers.getColumns().add(
                createColumn("Địa chỉ", Customer::getDiaChi, 220)
        );
        tblCustomers.getColumns().add(
                createColumn(
                        "Điểm tích lũy",
                        customer -> String.valueOf(
                                customer.getDiemTichLuy()
                        ),
                        105
                )
        );
        tblCustomers.getColumns().add(
                createColumn("Ghi chú", Customer::getGhiChu, 200)
        );
    }

    /**
     * Tạo cột có nội dung tự xuống dòng khi cần.
     *
     * @param title Tiêu đề cột.
     * @param getter Hàm lấy giá trị từ Customer.
     * @param width Chiều rộng ưu tiên.
     * @return Cột hiển thị văn bản.
     */
    private TableColumn<Customer, String> createColumn(
            String title,
            Function<Customer, String> getter,
            double width
    ) {
        // Cột dữ liệu đang được tạo.
        TableColumn<Customer, String> column =
                new TableColumn<>(title);

        column.setPrefWidth(width);
        column.setMinWidth(85);
        column.setSortable(false);

        column.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(
                        textOrEmpty(getter.apply(cell.getValue()))
                )
        );

        column.setCellFactory(table_column -> new TableCell<>() {
            // Nhãn hiển thị nội dung trong ô.
            private final Label lblValue = new Label();

            {
                lblValue.setWrapText(true);
                lblValue.getStyleClass().add("customer-cell-text");
                lblValue.prefWidthProperty().bind(
                        widthProperty().subtract(20)
                );
            }

            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                setText(null);

                // Dọn nội dung khi ô được tái sử dụng cho dòng trống.
                if (empty) {
                    setGraphic(null);
                } else {
                    lblValue.setText(textOrEmpty(value));
                    setGraphic(lblValue);
                }
            }
        });

        return column;
    }

    /** Đọc lại danh sách khách hàng ở luồng nền. */
    private void loadCustomers() {
        lblStatus.setText("Đang tải khách hàng...");

        executeBackground(
                () -> customer_controller.loadCustomers(),
                customers -> {
                    customer_list.setAll(customers);
                    filterCustomers();
                },
                error -> {
                    lblStatus.setText("Không tải được dữ liệu");
                    showError(error);
                }
        );
    }

    /** Lọc tất cả khách hàng có trường thông tin khớp từ khóa. */
    private void filterCustomers() {
        // Từ khóa không phân biệt hoa thường và dấu tiếng Việt.
        String keyword = normalizeText(txtSearch.getText());

        filtered_customers.setPredicate(customer ->
                keyword.isEmpty()
                        || normalizeText(customer.getMaKhachHang())
                                .contains(keyword)
                        || normalizeText(customer.getHoTen())
                                .contains(keyword)
                        || normalizeText(customer.getSoDienThoai())
                                .contains(keyword)
                        || normalizeText(customer.getEmail())
                                .contains(keyword)
        );

        lblStatus.setText(
                "Hiển thị " + filtered_customers.size()
                        + " / " + customer_list.size() + " khách hàng"
        );
    }

    /** Đăng ký F5 khi mở màn hình và gỡ khi rời màn hình. */
    private void registerRefreshShortcut() {
        // Bộ xử lý phím tắt dùng trên Scene của cửa sổ chính.
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
                                KeyEvent.KEY_PRESSED, refresh_handler
                        );
                    }

                    if (new_scene != null) {
                        new_scene.addEventFilter(
                                KeyEvent.KEY_PRESSED, refresh_handler
                        );
                    }
                }
        );
    }

    /**
     * Mở form thêm hoặc sửa theo bố cục một cột.
     *
     * @param selected Khách hàng cần sửa; null nếu thêm mới.
     */
    private void showCustomerDialog(Customer selected) {
        // Phân biệt thao tác thêm và sửa.
        boolean editing = selected != null;

        // Hộp thoại form nhập liệu.
        Dialog<ButtonType> dialog = new Dialog<>();
        configureDialog(dialog, 680);

        // Tiêu đề form.
        Label lblTitle = new Label(
                editing ? "Sửa khách hàng" : "Thêm khách hàng"
        );
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

        // Chỉ nhận tối đa 10 chữ số.
        txtPhone.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("[0-9]{0,10}")
                        ? change : null
        ));

        // Điền dữ liệu cũ khi mở form sửa.
        if (editing) {
            txtName.setText(selected.getHoTen());
            txtPhone.setText(selected.getSoDienThoai());
            txtEmail.setText(selected.getEmail());
            txtAddress.setText(selected.getDiaChi());
            txtNote.setText(selected.getGhiChu());
        }

        // Lỗi nhập liệu hoặc lỗi lưu hiển thị ngay trong form.
        Label lblError = new Label();
        lblError.setWrapText(true);
        lblError.getStyleClass().add("customer-error");

        // Form một cột theo Figma mới.
        VBox form = new VBox(
                12,
                lblTitle,
                createField("Họ tên *", txtName),
                createField("Số điện thoại *", txtPhone),
                createField("Email", txtEmail),
                createField("Địa chỉ", txtAddress),
                createField("Ghi chú", txtNote),
                lblError
        );

        dialog.getDialogPane().setContent(form);

        // Các nút của form.
        ButtonType cancel_type = new ButtonType(
                "Hủy", ButtonBar.ButtonData.CANCEL_CLOSE
        );
        ButtonType save_type = new ButtonType(
                editing ? "Cập nhật" : "Lưu khách hàng",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                cancel_type, save_type
        );

        // Nút xác nhận và nút hủy thực tế trong DialogPane.
        Button btnSave = (Button) dialog.getDialogPane()
                .lookupButton(save_type);
        Button btnCancel = (Button) dialog.getDialogPane()
                .lookupButton(cancel_type);

        btnSave.getStyleClass().add("customer-save");
        btnCancel.getStyleClass().add("customer-secondary");

        btnSave.disableProperty().bind(busy);
        btnCancel.disableProperty().bind(busy);

        // Không cho đóng form trong lúc đang lưu.
        dialog.setOnCloseRequest(event -> {
            if (busy.get()) {
                event.consume();
            }
        });

        btnSave.addEventFilter(ActionEvent.ACTION, event -> {
            // Giữ form mở đến khi lưu database thành công.
            event.consume();
            lblError.setText("");

            // Tạo bản dữ liệu mới, không sửa trực tiếp dòng đang chọn.
            Customer customer = new Customer(
                    editing ? selected.getMaKhachHang() : null,
                    txtName.getText(),
                    txtPhone.getText(),
                    txtEmail.getText(),
                    editing ? selected.getDiemTichLuy() : 0,
                    txtAddress.getText(),
                    txtNote.getText()
            );

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
                        // Giữ dữ liệu đã nhập để người dùng sửa và thử lại.
                        form.setDisable(false);
                        lblError.setText(error.getMessage());
                    }
            );
        });

        showModal(dialog);
    }

    /**
     * Tạo nhóm nhãn và ô nhập liệu.
     *
     * @param title Nội dung nhãn.
     * @param control Điều khiển nhập liệu.
     * @return Khung chứa nhãn phía trên và ô nhập phía dưới.
     */
    private VBox createField(String title, Control control) {
        // Nhãn mô tả ô nhập.
        Label lblField = new Label(title);
        lblField.getStyleClass().add("customer-field-label");

        control.getStyleClass().add("customer-input");
        control.setMaxWidth(Double.MAX_VALUE);

        return new VBox(5, lblField, control);
    }

    /** Mở hộp xác nhận xóa và hiển thị thông tin khách hàng. */
    private void showDeleteDialog() {
        // Khách hàng được chọn trên bảng.
        Customer selected =
                tblCustomers.getSelectionModel().getSelectedItem();

        if (selected == null) {
            return;
        }

        // Hộp thoại xác nhận xóa hẳn.
        Dialog<ButtonType> dialog = new Dialog<>();
        configureDialog(dialog, 600);

        // Tiêu đề và lời nhắc của hộp thoại.
        Label lblTitle = new Label("Xóa khách hàng");
        lblTitle.getStyleClass().add("customer-dialog-title");

        Label lblWarning = new Label(
                "Hành động này không thể hoàn tác."
        );
        lblWarning.getStyleClass().add("customer-field-label");

        // Avatar lấy chữ cái đầu của tên.
        Label lblAvatar = new Label(
                getInitials(selected.getHoTen())
        );
        lblAvatar.getStyleClass().add("customer-avatar");

        Label lblName = new Label(selected.getHoTen());
        lblName.setStyle("-fx-font-weight: bold;");

        // Thẻ thông tin tên khách hàng.
        HBox profile = new HBox(14, lblAvatar, lblName);
        profile.setAlignment(Pos.CENTER_LEFT);
        profile.getStyleClass().add("customer-info-card");

        // Tiêu đề khu vực thông tin cá nhân.
        Label lblDetails = new Label("Thông tin cá nhân");
        lblDetails.setStyle("-fx-font-weight: bold;");

        // Lưới thông tin gồm ba cột, hai hàng.
        GridPane details = new GridPane();
        details.setHgap(16);
        details.setVgap(14);

        for (int index = 0; index < 3; index++) {
            // Mỗi cột chiếm một phần ba chiều rộng.
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / 3);
            details.getColumnConstraints().add(column);
        }

        details.add(
                createDetail("Mã khách hàng", selected.getMaKhachHang()),
                0, 0
        );
        details.add(
                createDetail("Số điện thoại", selected.getSoDienThoai()),
                1, 0
        );
        details.add(
                createDetail("Email", selected.getEmail()),
                2, 0
        );
        details.add(
                createDetail("Địa chỉ", selected.getDiaChi()),
                0, 1
        );
        details.add(
                createDetail(
                        "Điểm tích lũy",
                        String.valueOf(selected.getDiemTichLuy())
                ),
                1, 1
        );
        details.add(
                createDetail("Ghi chú", selected.getGhiChu()),
                2, 1
        );

        // Thẻ chứa toàn bộ thông tin chi tiết.
        VBox detail_card = new VBox(14, lblDetails, details);
        detail_card.getStyleClass().add("customer-info-card");

        // Nhãn báo lỗi nếu khách hàng không được phép xóa.
        Label lblError = new Label();
        lblError.setWrapText(true);
        lblError.getStyleClass().add("customer-error");

        // Nội dung hộp xác nhận.
        VBox content = new VBox(
                14, lblTitle, lblWarning,
                new Separator(), profile, detail_card, lblError
        );

        dialog.getDialogPane().setContent(content);

        // Nút hủy và xác nhận xóa.
        ButtonType cancel_type = new ButtonType(
                "Hủy", ButtonBar.ButtonData.CANCEL_CLOSE
        );
        ButtonType delete_type = new ButtonType(
                "Xác nhận xóa", ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                cancel_type, delete_type
        );

        // Các nút thực tế của hộp thoại.
        Button btnConfirm = (Button) dialog.getDialogPane()
                .lookupButton(delete_type);
        Button btnCancel = (Button) dialog.getDialogPane()
                .lookupButton(cancel_type);

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
                        customer_controller.deleteCustomer(
                                selected.getMaKhachHang()
                        );
                        return null;
                    },
                    result -> {
                        dialog.setResult(delete_type);
                        dialog.close();
                        loadCustomers();
                    },
                    error -> lblError.setText(error.getMessage())
            );
        });

        showModal(dialog);
    }

    /**
     * Tạo một mục thông tin trong hộp xác nhận xóa.
     *
     * @param title Tên trường thông tin.
     * @param value Giá trị cần hiển thị.
     * @return Khung chứa tên và giá trị.
     */
    private VBox createDetail(String title, String value) {
        // Nhãn tên trường thông tin.
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("customer-field-label");

        // Nhãn nội dung, tự xuống dòng khi dài.
        Label lblValue = new Label(
                value == null || value.isBlank() ? "—" : value
        );
        lblValue.setWrapText(true);
        lblValue.setMaxWidth(Double.MAX_VALUE);

        return new VBox(5, lblTitle, lblValue);
    }

    /**
     * Thiết lập hộp thoại không có thanh tiêu đề hệ điều hành.
     *
     * @param dialog Hộp thoại cần thiết lập.
     * @param width Chiều rộng ưu tiên của hộp thoại.
     */
    private void configureDialog(Dialog<?> dialog, double width) {
        dialog.initOwner(getScene().getWindow());
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        dialog.getDialogPane().setPrefWidth(width);
        dialog.getDialogPane().getStyleClass()
                .add("customer-dialog");

        // Dùng lại stylesheet đang được cửa sổ chính sử dụng.
        dialog.getDialogPane().getStylesheets().addAll(
                getScene().getStylesheets()
        );
    }

    /**
     * Hiển thị hộp thoại và làm mờ nền phía sau.
     *
     * @param dialog Hộp thoại cần mở.
     * @param <T> Kiểu kết quả của hộp thoại.
     * @return Kết quả khi đóng hộp thoại.
     */
    private <T> Optional<T> showModal(Dialog<T> dialog) {
        // Gốc giao diện chính và hiệu ứng đang có trước khi mở form.
        Node owner_root = getScene().getRoot();
        Effect previous_effect = owner_root.getEffect();

        // Làm mờ và giảm độ sáng cửa sổ phía sau.
        GaussianBlur blur = new GaussianBlur(6);
        blur.setInput(previous_effect);

        ColorAdjust dim = new ColorAdjust();
        dim.setBrightness(-0.25);
        dim.setInput(blur);

        owner_root.setEffect(dim);

        try {
            return dialog.showAndWait();
        } finally {
            // Khôi phục giao diện khi đóng form.
            owner_root.setEffect(previous_effect);
        }
    }

    /**
     * Chạy thao tác Controller ở luồng nền.
     *
     * @param operation Thao tác cần thực hiện.
     * @param on_success Xử lý khi thành công trên luồng giao diện.
     * @param on_failure Xử lý khi thất bại trên luồng giao diện.
     * @param <T> Kiểu kết quả của thao tác.
     */
    private <T> void executeBackground(
            Supplier<T> operation,
            Consumer<T> on_success,
            Consumer<Throwable> on_failure
    ) {
        busy.set(true);

        // Tác vụ thực hiện thao tác dữ liệu ngoài luồng giao diện.
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

        // Luồng nền kết thúc cùng ứng dụng.
        Thread worker = new Thread(task, "customer-operation");
        worker.setDaemon(true);
        worker.start();
    }

    /**
     * Hiển thị lỗi đọc dữ liệu.
     *
     * @param error Lỗi cần thông báo.
     */
    private void showError(Throwable error) {
        // Hộp thoại lỗi dành cho thao tác ngoài form.
        Alert alert = new Alert(
                Alert.AlertType.ERROR, error.getMessage()
        );
        alert.initOwner(getScene().getWindow());
        alert.setTitle("Thông báo");
        alert.setHeaderText("Không thực hiện được thao tác");
        alert.showAndWait();
    }

    /**
     * Chuẩn hóa chuỗi để tìm kiếm không phân biệt dấu và hoa thường.
     *
     * @param value Chuỗi cần chuẩn hóa.
     * @return Chuỗi chữ thường không dấu.
     */
    private String normalizeText(String value) {
        return Normalizer.normalize(
                        textOrEmpty(value), Normalizer.Form.NFD
                )
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    /**
     * Chuyển null thành chuỗi rỗng.
     *
     * @param value Giá trị cần kiểm tra.
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

        // Các từ trong họ tên đã bỏ khoảng trắng đầu cuối.
        String[] parts = name.trim().split("\\s+");

        return (
                parts[0].substring(0, 1)
                        + (parts.length > 1
                        ? parts[parts.length - 1].substring(0, 1)
                        : "")
        ).toUpperCase(Locale.ROOT);
    }
}