package com.petshop.view.customer;

import com.petshop.controller.CustomerController;
import com.petshop.controller.CustomerTransactionController;
import com.petshop.model.Customer;
import com.petshop.model.CustomerTransaction;
import com.petshop.model.CustomerTransactionItem;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javafx.animation.PauseTransition;
import javafx.geometry.Side;
import javafx.util.Duration;

/**
 * Giao diện lịch sử giao dịch khách hàng.
 * Hỗ trợ chọn khách hàng, lọc hóa đơn và xem chi tiết giao dịch.
 * Chỉ đọc dữ liệu thông qua các Controller.
 */
public class CustomerTransactionView extends VBox {

    // Controller lấy danh sách khách hàng.
    private final CustomerController customer_controller = new CustomerController();

    // Controller xử lý lịch sử giao dịch.
    private final CustomerTransactionController transaction_controller = new CustomerTransactionController();

    // Danh sách hóa đơn hiện đang hiển thị.
    private final ObservableList<CustomerTransaction> transaction_list = FXCollections.observableArrayList();

    // Trạng thái đang thực hiện truy vấn.
    private final SimpleBooleanProperty busy = new SimpleBooleanProperty(false);

    // Khách hàng đang được chọn để xem lịch sử.
    private Customer selected_customer;

    // Danh sách khách hàng lưu tạm để gợi ý khi nhập.
    private List<Customer> suggestion_customers = List.of();

    // Popup hiển thị gợi ý ngay dưới ô tìm kiếm.
    private final ContextMenu customer_suggestions = new ContextMenu();

    // Đợi người dùng ngừng gõ một chút trước khi cập nhật gợi ý.
    private final PauseTransition suggestion_delay = new PauseTransition(Duration.millis(200));

    // Trạng thái tải danh sách dùng cho gợi ý.
    private boolean suggestions_loaded = false;
    private boolean suggestions_loading = false;
    // Bộ lọc đã được áp dụng thành công.
    private LocalDate applied_from_date;
    private LocalDate applied_to_date;
    private String applied_status = "Tất cả";

    // Định dạng ngày hiển thị.
    private final DateTimeFormatter date_format = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    // Định dạng tiền Việt Nam.
    private final NumberFormat money_format = NumberFormat.getNumberInstance(
            Locale.forLanguageTag("vi-VN"));

    // Các điều khiển tìm khách hàng.
    private final TextField txtCustomerSearch = new TextField();
    private final Button btnSearch = new Button("Tìm kiếm");
    private final Button btnRefresh = new Button("Làm mới");
    private final Button btnChangeCustomer = new Button("Đổi khách hàng");

    // Danh sách chọn khi nhiều khách hàng khớp từ khóa.
    private final ListView<Customer> lstCustomers = new ListView<>();
    private final Button btnChooseCustomer = new Button("Chọn khách hàng");
    private final VBox pnlCustomerResults = new VBox(8);

    // Thẻ thông tin khách hàng đã chọn.
    private final HBox pnlCustomerProfile = new HBox(18);

    // Các điều khiển lọc hóa đơn.
    private final DatePicker dtpFromDate = new DatePicker();
    private final DatePicker dtpToDate = new DatePicker();
    private final ComboBox<String> cboPaymentStatus = new ComboBox<>();
    private final Button btnApply = new Button("Áp dụng");
    private final Button btnClearFilters = new Button("Xóa bộ lọc");

    // Bảng hóa đơn và nút xem chi tiết.
    private final TableView<CustomerTransaction> tblTransactions = new TableView<>();

    private final Button btnViewDetails = new Button("Xem chi tiết");

    // Các nhãn thông báo và tổng hợp.
    private final Label lblSearchMessage = new Label();
    private final Label lblFilterMessage = new Label();
    private final Label lblResultCount = new Label("Chưa chọn khách hàng");
    private final Label lblPaidCaption = new Label("Tổng tiền đã thanh toán");
    private final Label lblPaidTotal = new Label("—");

    // Khung thông báo khi bảng trống hoặc xảy ra lỗi.
    private final Label lblTableMessage = new Label();
    private final Button btnRetry = new Button("Thử lại");
    private final VBox pnlTablePlaceholder = new VBox(10);

    /**
     * Khởi tạo toàn bộ màn lịch sử giao dịch.
     */
    public CustomerTransactionView() {
        getStyleClass().add("customer-history-page");
        setPadding(new Insets(24, 28, 24, 28));
        setSpacing(14);

        money_format.setMinimumFractionDigits(0);
        money_format.setMaximumFractionDigits(2);

        // Tiêu đề trang.
        Label lblTitle = new Label("Lịch sử giao dịch khách hàng");
        lblTitle.getStyleClass().add("history-title");

        // Khu vực tìm và chọn khách hàng.
        VBox search_section = createCustomerSearch();

        configureCustomerProfile();

        // Hàng bộ lọc ngày và trạng thái.
        HBox filter_bar = createFilterBar();

        lblFilterMessage.getStyleClass().add("history-muted");
        lblFilterMessage.setWrapText(true);
        resetFilterMessage();

        // Thẻ chứa bảng hóa đơn và phần tổng hợp.
        VBox transaction_card = createTransactionCard();

        VBox.setVgrow(transaction_card, Priority.ALWAYS);

        bindControls();
        registerActions();
        registerRefreshShortcut();
        // Thiết lập gợi ý khi nhập tên, mã hoặc số điện thoại.
        registerCustomerSuggestions();

        getChildren().addAll(
                lblTitle,
                search_section,
                pnlCustomerProfile,
                filter_bar,
                lblFilterMessage,
                transaction_card);

        showTableMessage(
                "Chọn khách hàng để xem lịch sử giao dịch.",
                false);
    }

    /**
     * Tạo khu vực tìm kiếm và danh sách chọn khách hàng.
     *
     * @return Khung tìm khách hàng.
     */
    private VBox createCustomerSearch() {
        // Nhãn phía trên ô tìm kiếm.
        Label lblSearch = new Label("Tìm kiếm khách hàng");
        lblSearch.getStyleClass().add("history-section-title");

        txtCustomerSearch.setPromptText(
                "Tìm khách hàng theo mã, họ tên, số điện thoại...");
        txtCustomerSearch.setPrefWidth(520);
        txtCustomerSearch.getStyleClass().add("history-input");

        btnSearch.getStyleClass().add("history-primary");
        btnRefresh.getStyleClass().add("history-secondary");

        // Hàng nhập từ khóa và các nút tìm kiếm.
        HBox search_bar = new HBox(
                8,
                txtCustomerSearch,
                btnSearch,
                btnRefresh);
        search_bar.setAlignment(Pos.CENTER_LEFT);

        lblSearchMessage.setWrapText(true);
        lblSearchMessage.getStyleClass().add("history-muted");

        // Giữ đủ chiều cao để hiển thị các khách hàng tìm được.
        lstCustomers.setMinHeight(120);
        lstCustomers.setPrefHeight(145);
        lstCustomers.setMaxHeight(145);

        // Không cho khung kết quả bị ép thấp hơn nội dung bên trong.
        pnlCustomerResults.setMinHeight(Region.USE_PREF_SIZE);

        // Hiển thị tên, mã và số điện thoại để phân biệt người trùng tên.
        lstCustomers.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Customer customer, boolean empty) {
                super.updateItem(customer, empty);

                if (empty || customer == null) {
                    setText(null);
                } else {
                    setText(
                            customer.getHoTen()
                                    + "  |  "
                                    + customer.getMaKhachHang()
                                    + "  |  "
                                    + customer.getSoDienThoai());
                }
            }
        });

        btnChooseCustomer.getStyleClass().add("history-primary");

        pnlCustomerResults.getChildren().addAll(
                new Label("Chọn khách hàng trong kết quả:"),
                lstCustomers,
                btnChooseCustomer);
        pnlCustomerResults.getStyleClass().add("history-card");

        setShown(pnlCustomerResults, false);

        return new VBox(
                6,
                lblSearch,
                search_bar,
                lblSearchMessage);
    }

    /**
     * Thiết lập thẻ khách hàng đã chọn.
     */
    private void configureCustomerProfile() {
        pnlCustomerProfile.setAlignment(Pos.CENTER_LEFT);
        pnlCustomerProfile.getStyleClass().add("history-profile");

        btnChangeCustomer.getStyleClass().add("history-secondary");

        setShown(pnlCustomerProfile, false);
    }

    /**
     * Tạo hàng lọc ngày và trạng thái thanh toán.
     *
     * @return Hàng điều khiển bộ lọc.
     */
    private HBox createFilterBar() {
        configureDatePicker(dtpFromDate);
        configureDatePicker(dtpToDate);

        cboPaymentStatus.getItems().addAll(
                "Tất cả",
                "Chưa thanh toán",
                "Đã thanh toán",
                "Đã hủy");
        cboPaymentStatus.setValue("Tất cả");
        cboPaymentStatus.setPrefWidth(190);
        cboPaymentStatus.getStyleClass().add("history-input");

        btnApply.getStyleClass().add("history-primary");
        btnClearFilters.getStyleClass().add("history-secondary");

        // Các ô lọc có nhãn ở phía trên.
        HBox filter_bar = new HBox(
                10,
                createField("Từ ngày", dtpFromDate),
                createField("Đến ngày", dtpToDate),
                createField("Trạng thái thanh toán", cboPaymentStatus),
                btnApply,
                btnClearFilters);
        filter_bar.setAlignment(Pos.BOTTOM_LEFT);

        return filter_bar;
    }

    /**
     * Thiết lập DatePicker có lịch chọn ngày.
     *
     * @param date_picker Điều khiển ngày cần thiết lập.
     */
    private void configureDatePicker(DatePicker date_picker) {
        date_picker.setPromptText("dd/MM/yyyy");
        date_picker.setPrefWidth(160);
        date_picker.setEditable(false);
        date_picker.getStyleClass().add("history-input");

        date_picker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate value) {
                return value == null ? "" : value.format(date_format);
            }

            @Override
            public LocalDate fromString(String value) {
                return value == null || value.isBlank()
                        ? null
                        : LocalDate.parse(value, date_format);
            }
        });
    }

    /**
     * Tạo nhóm nhãn và điều khiển.
     *
     * @param title   Nội dung nhãn.
     * @param control Điều khiển cần hiển thị.
     * @return Khung chứa nhãn và điều khiển.
     */
    private VBox createField(String title, Control control) {
        // Nhãn mô tả trường.
        Label lblField = new Label(title);
        lblField.getStyleClass().add("history-field-label");

        return new VBox(5, lblField, control);
    }

    /**
     * Tạo bảng hóa đơn và phần tổng hợp.
     *
     * @return Thẻ chứa danh sách hóa đơn.
     */
    private VBox createTransactionCard() {
        // Tiêu đề bảng và mô tả thứ tự.
        Label lblTitle = new Label("Danh sách hóa đơn");
        lblTitle.getStyleClass().add("history-section-title");

        Label lblSubtitle = new Label("Ngày lập mới nhất trước");
        lblSubtitle.getStyleClass().add("history-muted");

        // Khoảng trống đẩy nút Xem chi tiết sang phải.
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnViewDetails.getStyleClass().add("history-primary");

        // Hàng tiêu đề của thẻ.
        HBox card_header = new HBox(
                10,
                new VBox(3, lblTitle, lblSubtitle),
                spacer,
                btnViewDetails);
        card_header.setAlignment(Pos.CENTER_LEFT);

        tblTransactions.setItems(transaction_list);
        tblTransactions.getStyleClass().add("history-table");
        tblTransactions.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);

        tblTransactions.getColumns().add(
                createColumn(
                        "Mã hóa đơn",
                        CustomerTransaction::getMaHoaDon,
                        190));
        tblTransactions.getColumns().add(
                createColumn(
                        "Ngày lập",
                        transaction -> transaction.getNgayLap()
                                .format(date_format),
                        110));
        tblTransactions.getColumns().add(
                createColumn(
                        "Nhân viên lập",
                        CustomerTransaction::getTenNhanVien,
                        160));
        tblTransactions.getColumns().add(
                createColumn(
                        "Hình thức thanh toán",
                        transaction -> displayText(
                                transaction.getHinhThucThanhToan()),
                        170));
        tblTransactions.getColumns().add(
                createColumn(
                        "Tổng tiền",
                        transaction -> formatMoney(
                                transaction.getTongTien()),
                        135));

        // Cột trạng thái hiển thị dưới dạng badge.
        TableColumn<CustomerTransaction, String> status_column = createColumn(
                "Trạng thái thanh toán",
                CustomerTransaction::getTrangThaiThanhToan,
                170);

        status_column.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                setText(null);
                setGraphic(null);

                if (!empty && status != null) {
                    setGraphic(createStatusBadge(status));
                }
            }
        });

        tblTransactions.getColumns().add(status_column);

        lblTableMessage.setWrapText(true);
        lblTableMessage.getStyleClass().add("history-muted");

        btnRetry.getStyleClass().add("history-primary");

        pnlTablePlaceholder.setAlignment(Pos.CENTER);
        pnlTablePlaceholder.getChildren().addAll(
                lblTableMessage,
                btnRetry);

        tblTransactions.setPlaceholder(pnlTablePlaceholder);

        // Phần tổng hợp bên trái.
        Label lblCountCaption = new Label("Trong kết quả đang hiển thị");
        lblCountCaption.getStyleClass().add("history-muted");

        VBox count_box = new VBox(
                4,
                lblCountCaption,
                lblResultCount);

        // Phần tổng tiền bên phải.
        lblPaidCaption.getStyleClass().add("history-muted");
        lblPaidTotal.getStyleClass().add("history-total");

        VBox total_box = new VBox(
                4,
                lblPaidCaption,
                lblPaidTotal);
        total_box.setAlignment(Pos.CENTER_RIGHT);

        // Khoảng trống giữa hai phần tổng hợp.
        Region footer_spacer = new Region();
        HBox.setHgrow(footer_spacer, Priority.ALWAYS);

        HBox footer = new HBox(
                count_box,
                footer_spacer,
                total_box);
        footer.setAlignment(Pos.CENTER_LEFT);

        VBox transaction_card = new VBox(
                12,
                card_header,
                tblTransactions,
                new Separator(),
                footer);
        transaction_card.getStyleClass().add("history-card");

        VBox.setVgrow(tblTransactions, Priority.ALWAYS);

        return transaction_card;
    }

    /**
     * Tạo cột văn bản cho bảng.
     *
     * @param title  Tiêu đề cột.
     * @param getter Hàm lấy dữ liệu.
     * @param width  Chiều rộng ưu tiên.
     * @param <T>    Kiểu đối tượng của một dòng.
     * @return Cột văn bản đã thiết lập.
     */
    private <T> TableColumn<T, String> createColumn(
            String title,
            Function<T, String> getter,
            double width) {
        // Cột đang được tạo.
        TableColumn<T, String> column = new TableColumn<>(title);

        column.setPrefWidth(width);
        column.setMinWidth(90);
        column.setSortable(false);

        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                getter.apply(cell.getValue())));

        return column;
    }

    /**
     * Thiết lập trạng thái bật, tắt của các điều khiển.
     */
    private void bindControls() {
        // Điều kiện chưa chọn khách hàng hoặc đang tải dữ liệu.
        SimpleBooleanProperty no_customer = new SimpleBooleanProperty(true);

        pnlCustomerProfile.visibleProperty().addListener(
                (observable, old_value, visible) -> no_customer.set(!visible));

        btnSearch.disableProperty().bind(busy);
        btnRefresh.disableProperty().bind(busy);
        txtCustomerSearch.disableProperty().bind(busy);
        lstCustomers.disableProperty().bind(busy);
        btnChangeCustomer.disableProperty().bind(busy);

        btnChooseCustomer.disableProperty().bind(
                busy.or(
                        lstCustomers.getSelectionModel()
                                .selectedItemProperty().isNull()));

        dtpFromDate.disableProperty().bind(busy.or(no_customer));
        dtpToDate.disableProperty().bind(busy.or(no_customer));
        cboPaymentStatus.disableProperty().bind(busy.or(no_customer));
        btnApply.disableProperty().bind(busy.or(no_customer));
        btnClearFilters.disableProperty().bind(busy.or(no_customer));

        tblTransactions.disableProperty().bind(busy);

        btnViewDetails.disableProperty().bind(
                busy.or(
                        tblTransactions.getSelectionModel()
                                .selectedItemProperty().isNull()));

        btnRetry.disableProperty().bind(busy);
    }

    /**
     * Gắn hành động cho các điều khiển.
     */
    private void registerActions() {
        btnSearch.setOnAction(event -> searchCustomers());
        txtCustomerSearch.setOnAction(event -> searchCustomers());

        btnChooseCustomer.setOnAction(event -> chooseCustomer());

        lstCustomers.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                chooseCustomer();
            }
        });

        lstCustomers.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                chooseCustomer();
                event.consume();
            }
        });

        btnChangeCustomer.setOnAction(event -> resetScreen());

        btnRefresh.setOnAction(event -> {
            if (selected_customer != null) {
                reloadTransactions();
            } else {
                searchCustomers();
            }
        });

        btnApply.setOnAction(event -> applyFilters());

        btnClearFilters.setOnAction(event -> {
            clearFilterValues();
            loadTransactions(null, null, "Tất cả");
        });

        btnRetry.setOnAction(event -> reloadTransactions());

        btnViewDetails.setOnAction(event -> showTransactionDetails());
    }

    /**
     * Tải lại danh sách khách hàng và hiển thị gợi ý.
     * Được gọi khi bấm Tìm kiếm hoặc nhấn Enter.
     */
    private void searchCustomers() {
        if (busy.get()) {
            return;
        }

        txtCustomerSearch.requestFocus();
        loadCustomerSuggestions();
    }

    /**
     * Đăng ký gợi ý khách hàng khi nội dung ô tìm kiếm thay đổi.
     */
    private void registerCustomerSuggestions() {
        customer_suggestions.getStyleClass().add(
                "history-customer-suggestions");

        // Chờ 200 ms sau lần gõ gần nhất.
        suggestion_delay.setOnFinished(event -> {
            if (suggestions_loaded) {
                showCustomerSuggestions();
            } else {
                loadCustomerSuggestions();
            }
        });

        txtCustomerSearch.textProperty().addListener(
                (observable, old_value, new_value) -> {
                    suggestion_delay.stop();
                    customer_suggestions.hide();

                    // Không hiện toàn bộ khách hàng khi ô đang trống.
                    if (new_value == null || new_value.isBlank()) {
                        lblSearchMessage.setText("");
                        return;
                    }

                    if (!busy.get()) {
                        suggestion_delay.playFromStart();
                    }
                });

        // Đóng gợi ý khi bắt đầu tải lịch sử hoặc chi tiết hóa đơn.
        busy.addListener((observable, old_value, loading) -> {
            if (loading) {
                suggestion_delay.stop();
                customer_suggestions.hide();
            }
        });

        // Không để popup còn hiển thị khi chuyển sang màn khác.
        sceneProperty().addListener(
                (observable, old_scene, new_scene) -> {
                    if (new_scene == null) {
                        suggestion_delay.stop();
                        customer_suggestions.hide();
                    }
                });
    }

    /**
     * Tải danh sách khách hàng qua Controller.
     * Không khóa ô nhập trong khi tải để người dùng tiếp tục gõ.
     */
    private void loadCustomerSuggestions() {
        // Không tạo nhiều truy vấn gợi ý cùng lúc.
        if (suggestions_loading) {
            return;
        }

        suggestions_loading = true;
        lblSearchMessage.setText("Đang tải gợi ý...");

        // Tác vụ chỉ đọc dữ liệu ở luồng nền.
        Task<List<Customer>> suggestion_task = new Task<>() {
            @Override
            protected List<Customer> call() {
                return customer_controller.loadCustomers();
            }
        };

        suggestion_task.setOnSucceeded(event -> {
            suggestion_customers = List.copyOf(
                    suggestion_task.getValue());

            suggestions_loaded = true;
            suggestions_loading = false;

            // Lọc bằng nội dung hiện tại, không dùng từ khóa cũ lúc bắt đầu tải.
            if (getScene() != null
                    && txtCustomerSearch.isFocused()
                    && !busy.get()) {
                showCustomerSuggestions();
            }
        });

        suggestion_task.setOnFailed(event -> {
            suggestions_loading = false;
            customer_suggestions.hide();

            suggestion_task.getException().printStackTrace();

            if (getScene() != null && !busy.get()) {
                lblSearchMessage.setText(
                        "Không tải được gợi ý. Bấm Tìm kiếm để thử lại.");
            }
        });

        // Luồng tải dữ liệu phục vụ gợi ý khách hàng.
        Thread worker = new Thread(
                suggestion_task,
                "customer-history-suggestions");

        worker.setDaemon(true);
        worker.start();
    }

    /**
     * Hiển thị các khách hàng khớp từ khóa bên dưới ô tìm kiếm.
     * Tối đa 8 gợi ý mỗi lần để popup không quá dài.
     */
    private void showCustomerSuggestions() {
        // Từ khóa không phân biệt dấu và chữ hoa, chữ thường.
        String keyword = normalizeText(
                txtCustomerSearch.getText());

        customer_suggestions.hide();
        customer_suggestions.getItems().clear();

        if (keyword.isEmpty()
                || busy.get()
                || getScene() == null
                || !txtCustomerSearch.isFocused()) {
            return;
        }

        // Mọi khách hàng khớp mã, họ tên hoặc số điện thoại.
        List<Customer> matches = suggestion_customers.stream()
                .filter(customer -> normalizeText(customer.getMaKhachHang())
                        .contains(keyword)
                        || normalizeText(customer.getHoTen())
                                .contains(keyword)
                        || normalizeText(customer.getSoDienThoai())
                                .contains(keyword))
                .toList();

        if (matches.isEmpty()) {
            lblSearchMessage.setText(
                    "Không có khách hàng khớp từ khóa.");
            return;
        }

        // Giới hạn số dòng hiển thị trong popup.
        int visible_count = Math.min(matches.size(), 8);

        for (int index = 0; index < visible_count; index++) {
            // Khách hàng tương ứng với một dòng gợi ý.
            Customer customer = matches.get(index);

            // Hiện cả mã và SĐT để phân biệt người trùng tên.
            MenuItem suggestion_item = new MenuItem(
                    customer.getHoTen()
                            + "  |  "
                            + customer.getMaKhachHang()
                            + "  |  "
                            + customer.getSoDienThoai());

            suggestion_item.setOnAction(event -> selectSuggestedCustomer(customer));

            customer_suggestions.getItems().add(suggestion_item);
        }

        if (matches.size() > visible_count) {
            // Nhắc nhập cụ thể hơn khi có quá nhiều người khớp.
            MenuItem remaining_item = new MenuItem(
                    "Còn " + (matches.size() - visible_count)
                            + " kết quả. Nhập thêm để thu hẹp.");
            remaining_item.setDisable(true);

            customer_suggestions.getItems().add(remaining_item);
        }

        lblSearchMessage.setText(
                "Tìm thấy " + matches.size()
                        + " khách hàng. Bấm một gợi ý để xem lịch sử.");

        customer_suggestions.setPrefWidth(
                txtCustomerSearch.getWidth());

        // Popup xuất hiện ngay dưới ô tìm kiếm, không đẩy bảng xuống.
        customer_suggestions.show(
                txtCustomerSearch,
                Side.BOTTOM,
                0,
                3);
    }

    /**
     * Chọn trực tiếp khách hàng từ popup và tải lịch sử.
     *
     * @param customer Khách hàng người dùng đã chọn.
     */
    private void selectSuggestedCustomer(Customer customer) {
        if (busy.get()) {
            return;
        }

        suggestion_delay.stop();
        customer_suggestions.hide();

        selected_customer = customer;
        lblSearchMessage.setText("");

        setShown(pnlCustomerResults, false);

        updateCustomerProfile();
        clearFilterValues();

        loadTransactions(null, null, "Tất cả");
    }

    /**
     * Chọn khách hàng từ danh sách kết quả.
     */
    private void chooseCustomer() {
        if (busy.get()) {
            return;
        }

        // Khách hàng người dùng đã chọn.
        Customer customer = lstCustomers.getSelectionModel().getSelectedItem();

        if (customer == null) {
            return;
        }

        selected_customer = customer;

        setShown(pnlCustomerResults, false);
        lblSearchMessage.setText("");

        updateCustomerProfile();
        clearFilterValues();

        loadTransactions(null, null, "Tất cả");
    }

    /**
     * Hiển thị thông tin ngắn của khách hàng đã chọn.
     */
    private void updateCustomerProfile() {
        // Avatar chữ viết tắt của họ tên.
        Label lblAvatar = new Label(
                getInitials(selected_customer.getHoTen()));
        lblAvatar.getStyleClass().add("history-avatar");

        // Tên và mã khách hàng.
        VBox name_box = new VBox(
                4,
                new Label(selected_customer.getHoTen()),
                mutedLabel(selected_customer.getMaKhachHang()));

        // Thông tin điện thoại.
        VBox phone_box = new VBox(
                4,
                mutedLabel("Số điện thoại"),
                new Label(selected_customer.getSoDienThoai()));

        // Thông tin email.
        VBox email_box = new VBox(
                4,
                mutedLabel("Email"),
                new Label(displayText(selected_customer.getEmail())));

        // Khoảng trống đẩy nút Đổi khách hàng sang phải.
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        pnlCustomerProfile.getChildren().setAll(
                lblAvatar,
                name_box,
                phone_box,
                email_box,
                spacer,
                btnChangeCustomer);

        setShown(pnlCustomerProfile, true);
    }

    /**
     * Áp dụng bộ lọc được chọn trên giao diện.
     */
    private void applyFilters() {
        // Giá trị ngày hiện tại của hai DatePicker.
        LocalDate from_date = dtpFromDate.getValue();
        LocalDate to_date = dtpToDate.getValue();

        /*
         * Kiểm tra tại View để hiển thị lỗi ngay cạnh bộ lọc.
         * Service vẫn kiểm tra lại trước khi truy vấn database.
         */
        if (from_date != null
                && to_date != null
                && from_date.isAfter(to_date)) {

            lblFilterMessage.setText(
                    "Từ ngày phải nhỏ hơn hoặc bằng Đến ngày. "
                            + "Bộ lọc mới chưa được áp dụng.");
            lblFilterMessage.getStyleClass().add("history-error");
            return;
        }

        resetFilterMessage();

        loadTransactions(
                from_date,
                to_date,
                cboPaymentStatus.getValue());
    }

    /**
     * Tải lịch sử của khách hàng theo bộ lọc.
     *
     * @param from_date      Ngày bắt đầu hoặc null.
     * @param to_date        Ngày kết thúc hoặc null.
     * @param payment_status Trạng thái thanh toán hoặc Tất cả.
     */
    private void loadTransactions(
            LocalDate from_date,
            LocalDate to_date,
            String payment_status) {
        if (selected_customer == null || busy.get()) {
            return;
        }

        // Mã khách hàng cố định cho lần truy vấn này.
        String customer_id = selected_customer.getMaKhachHang();

        transaction_list.clear();
        lblResultCount.setText("Đang tải dữ liệu");
        lblPaidTotal.setText("—");
        lblPaidCaption.setText("Tổng tiền đã thanh toán");

        showTableMessage("Đang tải dữ liệu...", false);

        executeBackground(
                () -> transaction_controller.loadTransactions(
                        customer_id,
                        from_date,
                        to_date,
                        payment_status),
                transactions -> {
                    applied_from_date = from_date;
                    applied_to_date = to_date;
                    applied_status = payment_status;

                    transaction_list.setAll(transactions);

                    updateSummary();

                    // Phân biệt kết quả trống do bộ lọc hay chưa có giao dịch.
                    boolean has_filter = from_date != null
                            || to_date != null
                            || !"Tất cả".equals(payment_status);

                    showTableMessage(
                            has_filter
                                    ? "Không có giao dịch khớp bộ lọc."
                                    : "Khách hàng chưa có giao dịch.",
                            false);
                },
                error -> {
                    /*
                     * Ghi nhớ bộ lọc của lần tải lỗi để nút Thử lại
                     * thực hiện đúng yêu cầu vừa thất bại.
                     */
                    applied_from_date = from_date;
                    applied_to_date = to_date;
                    applied_status = payment_status;

                    lblResultCount.setText("Chưa tải được kết quả");
                    lblPaidTotal.setText("—");

                    showTableMessage(error.getMessage(), true);
                });
    }

    /**
     * Tải lại lịch sử theo bộ lọc đã áp dụng hoặc lần tải gần nhất.
     */
    private void reloadTransactions() {
        loadTransactions(
                applied_from_date,
                applied_to_date,
                applied_status);
    }

    /**
     * Cập nhật số hóa đơn và tổng tiền đã thanh toán.
     */
    private void updateSummary() {
        // Tổng tiền và số hóa đơn đã thanh toán trong kết quả hiện tại.
        BigDecimal paid_total = transaction_controller.calculatePaidTotal(
                transaction_list);

        int paid_count = transaction_controller.countPaidTransactions(
                transaction_list);

        lblResultCount.setText(transaction_list.size() + " hóa đơn");

        lblPaidCaption.setText(
                "Tổng tiền đã thanh toán · " + paid_count + " hóa đơn");

        lblPaidTotal.setText(formatMoney(paid_total));
    }

    /**
     * Đưa các điều khiển bộ lọc về trạng thái ban đầu.
     */
    private void clearFilterValues() {
        dtpFromDate.setValue(null);
        dtpToDate.setValue(null);
        cboPaymentStatus.setValue("Tất cả");

        applied_from_date = null;
        applied_to_date = null;
        applied_status = "Tất cả";

        resetFilterMessage();
    }

    /**
     * Hiển thị lời nhắc mặc định dưới hàng bộ lọc.
     */
    private void resetFilterMessage() {
        lblFilterMessage.getStyleClass().removeAll("history-error");
        lblFilterMessage.setText(
                "Để trống ngày để xem lịch sử không giới hạn khoảng ngày.");
    }

    /**
     * Trở về trạng thái chưa chọn khách hàng.
     */
    private void resetScreen() {
        selected_customer = null;

        txtCustomerSearch.clear();
        lstCustomers.getItems().clear();
        transaction_list.clear();

        setShown(pnlCustomerResults, false);
        setShown(pnlCustomerProfile, false);

        lblSearchMessage.setText("");
        clearFilterValues();

        lblResultCount.setText("Chưa chọn khách hàng");
        lblPaidCaption.setText("Tổng tiền đã thanh toán");
        lblPaidTotal.setText("—");

        showTableMessage(
                "Chọn khách hàng để xem lịch sử giao dịch.",
                false);

        txtCustomerSearch.requestFocus();
    }

    /**
     * Hiển thị thông báo trong vùng bảng trống.
     *
     * @param message Nội dung thông báo.
     * @param retry   Có hiển thị nút Thử lại hay không.
     */
    private void showTableMessage(String message, boolean retry) {
        lblTableMessage.setText(message);
        setShown(btnRetry, retry);
    }

    /**
     * Tải sản phẩm, dịch vụ và mở hộp chi tiết giao dịch.
     */
    private void showTransactionDetails() {
        // Hóa đơn đang được chọn trên bảng.
        CustomerTransaction selected = tblTransactions.getSelectionModel().getSelectedItem();

        if (selected == null || busy.get()) {
            return;
        }

        executeBackground(
                () -> {
                    // Các sản phẩm và dịch vụ của hóa đơn được chọn.
                    List<CustomerTransactionItem> products = transaction_controller.loadProducts(
                            selected.getMaKhachHang(),
                            selected.getMaHoaDon());

                    List<CustomerTransactionItem> services = transaction_controller.loadServices(
                            selected.getMaKhachHang(),
                            selected.getMaHoaDon());

                    return new TransactionDetails(products, services);
                },
                details -> openDetailsDialog(selected, details),
                error -> showError(error.getMessage()));
    }

    /**
     * Dựng hộp chi tiết giao dịch theo mẫu.
     *
     * @param transaction Hóa đơn được chọn.
     * @param details     Các dòng sản phẩm và dịch vụ đã tải.
     */
    private void openDetailsDialog(
            CustomerTransaction transaction,
            TransactionDetails details) {
        // Hộp thoại chi tiết giao dịch.
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(getScene().getWindow());
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        dialog.getDialogPane().getStyleClass().add("history-dialog");
        dialog.getDialogPane().setPrefWidth(760);
        dialog.getDialogPane().getStylesheets().addAll(
                getScene().getStylesheets());

        // Tiêu đề hộp thoại.
        Label lblTitle = new Label("Chi tiết giao dịch");
        lblTitle.getStyleClass().add("history-section-title");

        // Lưới thông tin đầu hóa đơn.
        GridPane information = new GridPane();
        information.setHgap(18);
        information.setVgap(16);
        information.getStyleClass().add("history-profile");

        for (int index = 0; index < 3; index++) {
            // Chia đều chiều rộng cho ba cột.
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / 3);
            information.getColumnConstraints().add(column);
        }

        information.add(
                detailField("Mã hóa đơn", transaction.getMaHoaDon()),
                0, 0);
        information.add(
                detailField(
                        "Ngày lập",
                        transaction.getNgayLap().format(date_format)),
                1, 0);
        information.add(
                detailField("Khách hàng", transaction.getTenKhachHang()),
                2, 0);
        information.add(
                detailField("Nhân viên lập", transaction.getTenNhanVien()),
                0, 1);
        information.add(
                detailField(
                        "Hình thức thanh toán",
                        displayText(transaction.getHinhThucThanhToan())),
                1, 1);
        information.add(
                new VBox(
                        5,
                        mutedLabel("Trạng thái thanh toán"),
                        createStatusBadge(
                                transaction.getTrangThaiThanhToan())),
                2, 1);

        // Nội dung có thể cuộn khi hóa đơn có nhiều dòng.
        VBox detail_content = new VBox(
                16,
                information,
                createItemSection("Sản phẩm", details.products()),
                createItemSection("Dịch vụ", details.services()));

        ScrollPane scrDetails = new ScrollPane(detail_content);
        scrDetails.setFitToWidth(true);
        scrDetails.setPrefViewportHeight(350);
        scrDetails.getStyleClass().add("history-scroll");

        // Tổng tiền cuối hộp lấy từ giá trị thực tế đã lưu.
        Label lblTotalTitle = new Label("Tổng thanh toán thực tế");
        lblTotalTitle.getStyleClass().add("history-section-title");

        Label lblTotalNote = mutedLabel(
                "Theo tổng tiền thực tế đã lưu của hóa đơn.");

        Label lblTotal = new Label(
                formatMoney(transaction.getTongTien()));
        lblTotal.getStyleClass().add("history-total");

        Region total_spacer = new Region();
        HBox.setHgrow(total_spacer, Priority.ALWAYS);

        HBox total_row = new HBox(
                new VBox(4, lblTotalTitle, lblTotalNote),
                total_spacer,
                lblTotal);
        total_row.setAlignment(Pos.CENTER_LEFT);

        dialog.getDialogPane().setContent(
                new VBox(
                        14,
                        lblTitle,
                        new Separator(),
                        scrDetails,
                        new Separator(),
                        total_row));

        // Hộp chi tiết chỉ có nút Đóng.
        ButtonType close_type = new ButtonType(
                "Đóng",
                ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(close_type);

        Button btnClose = (Button) dialog.getDialogPane()
                .lookupButton(close_type);
        btnClose.getStyleClass().add("history-secondary");

        showModal(dialog);
    }

    /**
     * Tạo khu vực sản phẩm hoặc dịch vụ trong hộp chi tiết.
     *
     * @param title Tiêu đề nhóm.
     * @param items Các dòng chi tiết.
     * @return Khung chứa tiêu đề và bảng.
     */
    private VBox createItemSection(
            String title,
            List<CustomerTransactionItem> items) {
        // Tiêu đề nhóm chi tiết.
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("history-section-title");

        // Bảng chi tiết sản phẩm hoặc dịch vụ.
        TableView<CustomerTransactionItem> tblItems = new TableView<>();
        tblItems.getStyleClass().add("history-table");
        tblItems.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);

        tblItems.getColumns().add(
                createColumn(
                        "Tên " + title.toLowerCase(
                                Locale.forLanguageTag("vi-VN")),
                        CustomerTransactionItem::getTenDoiTuong,
                        290));
        tblItems.getColumns().add(
                createColumn(
                        "Số lượng",
                        item -> String.valueOf(item.getSoLuong()),
                        90));
        tblItems.getColumns().add(
                createColumn(
                        "Đơn giá",
                        item -> formatMoney(item.getDonGia()),
                        140));
        tblItems.getColumns().add(
                createColumn(
                        "Thành tiền",
                        item -> formatMoney(item.getThanhTien()),
                        150));

        tblItems.getItems().setAll(items);
        tblItems.setFixedCellSize(40);
        tblItems.setPrefHeight(
                items.isEmpty() ? 100 : 42 + items.size() * 40);
        tblItems.setMinHeight(100);
        tblItems.setMaxHeight(Region.USE_PREF_SIZE);

        tblItems.setPlaceholder(
                new Label("Không có " + title.toLowerCase(
                        Locale.forLanguageTag("vi-VN")) + " trong hóa đơn."));

        return new VBox(8, lblTitle, tblItems);
    }

    /**
     * Tạo nhóm tên trường và giá trị chỉ đọc.
     *
     * @param title Tên trường.
     * @param value Nội dung trường.
     * @return Khung thông tin.
     */
    private VBox detailField(String title, String value) {
        // Giá trị thông tin có thể xuống dòng khi dài.
        Label lblValue = new Label(displayText(value));
        lblValue.setWrapText(true);
        lblValue.setMaxWidth(Double.MAX_VALUE);

        return new VBox(5, mutedLabel(title), lblValue);
    }

    /**
     * Tạo badge trạng thái thanh toán.
     *
     * @param status Trạng thái của hóa đơn.
     * @return Nhãn trạng thái có màu tương ứng.
     */
    private Label createStatusBadge(String status) {
        // Nhãn trạng thái được tạo riêng cho mỗi vị trí hiển thị.
        Label lblStatus = new Label(status);
        lblStatus.getStyleClass().add("history-badge");

        if ("Đã thanh toán".equals(status)) {
            lblStatus.getStyleClass().add("history-paid");
        } else if ("Đã hủy".equals(status)) {
            lblStatus.getStyleClass().add("history-cancelled");
        } else {
            lblStatus.getStyleClass().add("history-pending");
        }

        return lblStatus;
    }

    /**
     * Hiển thị hộp thoại và làm mờ nền chính.
     *
     * @param dialog Hộp thoại cần hiển thị.
     */
    private void showModal(Dialog<?> dialog) {
        // Lưu hiệu ứng cũ để khôi phục khi đóng hộp thoại.
        Node owner_root = getScene().getRoot();
        Effect previous_effect = owner_root.getEffect();

        GaussianBlur blur = new GaussianBlur(6);
        blur.setInput(previous_effect);

        ColorAdjust dim = new ColorAdjust();
        dim.setBrightness(-0.25);
        dim.setInput(blur);

        owner_root.setEffect(dim);

        try {
            dialog.showAndWait();
        } finally {
            owner_root.setEffect(previous_effect);
        }
    }

    /**
     * Chạy thao tác Controller ở luồng nền.
     *
     * @param operation  Thao tác cần thực hiện.
     * @param on_success Xử lý khi thành công.
     * @param on_failure Xử lý khi thất bại.
     * @param <T>        Kiểu kết quả của thao tác.
     */
    private <T> void executeBackground(
            Supplier<T> operation,
            Consumer<T> on_success,
            Consumer<Throwable> on_failure) {
        busy.set(true);

        // Tác vụ truy vấn, không cập nhật giao diện trong call().
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

        // Luồng nền tự kết thúc khi ứng dụng đóng.
        Thread worker = new Thread(task, "customer-history-operation");
        worker.setDaemon(true);
        worker.start();
    }

    /**
     * Đăng ký F5 khi màn lịch sử được gắn vào cửa sổ.
     */
    private void registerRefreshShortcut() {
        // Bộ xử lý tải lại dữ liệu bằng bàn phím.
        EventHandler<KeyEvent> refresh_handler = event -> {
            if (event.getCode() == KeyCode.F5) {
                if (!busy.get() && selected_customer != null) {
                    reloadTransactions();
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
     * Hiển thị thông báo lỗi.
     *
     * @param message Nội dung lỗi.
     */
    private void showError(String message) {
        // Hộp thoại lỗi khi tải chi tiết thất bại.
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.initOwner(getScene().getWindow());
        alert.setTitle("Thông báo");
        alert.setHeaderText("Không tải được chi tiết giao dịch");
        alert.showAndWait();
    }

    /**
     * Tạo nhãn chữ phụ.
     *
     * @param text Nội dung nhãn.
     * @return Nhãn có kiểu chữ phụ.
     */
    private Label mutedLabel(String text) {
        Label lblText = new Label(text);
        lblText.getStyleClass().add("history-muted");
        return lblText;
    }

    /**
     * Định dạng tiền Việt Nam.
     *
     * @param amount Số tiền cần hiển thị.
     * @return Chuỗi tiền có đơn vị đồng.
     */
    private String formatMoney(BigDecimal amount) {
        return money_format.format(amount) + " ₫";
    }

    /**
     * Hiển thị dấu gạch khi chưa có thông tin.
     *
     * @param value Giá trị cần hiển thị.
     * @return Giá trị hoặc dấu gạch.
     */
    private String displayText(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    /**
     * Chuẩn hóa từ khóa không phân biệt dấu và hoa thường.
     *
     * @param value Chuỗi cần chuẩn hóa.
     * @return Chuỗi chữ thường không dấu.
     */
    private String normalizeText(String value) {
        return Normalizer.normalize(
                value == null ? "" : value,
                Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    /**
     * Lấy chữ viết tắt của họ tên.
     *
     * @param name Họ tên khách hàng.
     * @return Chữ cái đầu của từ đầu và từ cuối.
     */
    private String getInitials(String name) {
        if (name == null || name.isBlank()) {
            return "KH";
        }

        // Các từ trong họ tên.
        String[] parts = name.trim().split("\\s+");

        return (parts[0].substring(0, 1)
                + (parts.length > 1
                        ? parts[parts.length - 1].substring(0, 1)
                        : ""))
                .toUpperCase(Locale.ROOT);
    }

    /**
     * Đồng thời thay đổi trạng thái hiển thị và chiếm diện tích.
     *
     * @param node  Thành phần giao diện.
     * @param shown Có hiển thị hay không.
     */
    private void setShown(Node node, boolean shown) {
        node.setVisible(shown);
        node.setManaged(shown);
    }

    /**
     * Kết quả tải chi tiết hóa đơn.
     *
     * @param products Danh sách sản phẩm.
     * @param services Danh sách dịch vụ.
     */
    private record TransactionDetails(
            List<CustomerTransactionItem> products,
            List<CustomerTransactionItem> services) {
    }
}
