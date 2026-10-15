package com.petshop.view;

import com.petshop.dao.CustomerDao;
import com.petshop.model.Customer;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.event.EventHandler;

/**
 * Quản lý khách hàng đang hoạt động.
 * Hỗ trợ thêm, sửa, ngừng hoạt động và tìm nhanh khách hàng.
 */
public class CustomerView extends VBox {

    // Đối tượng truy cập dữ liệu khách hàng.
    private final CustomerDao customer_dao = new CustomerDao();

    // Danh sách gốc đọc từ database.
    private final ObservableList<Customer> customer_list =
            FXCollections.observableArrayList();

    // Danh sách hiển thị sau khi lọc theo từ khóa.
    private final FilteredList<Customer> filtered_customers =
            new FilteredList<>(customer_list, customer -> true);

    // Trạng thái đang truy vấn hoặc lưu dữ liệu.
    private final SimpleBooleanProperty busy =
            new SimpleBooleanProperty(false);

    // Bảng hiển thị khách hàng.
    private final TableView<Customer> tblCustomers = new TableView<>();

    // Ô tìm nhanh theo mã, tên, số điện thoại hoặc email.
    private final TextField txtSearch = new TextField();

    // Các nút thao tác khách hàng.
    private final Button btnAdd = new Button("＋ Thêm mới");
    private final Button btnEdit = new Button("Sửa");
    private final Button btnDelete = new Button("Ngừng hoạt động");
    private final Button btnRefresh = new Button("Làm mới");

    // Nhãn thông báo số lượng và trạng thái xử lý.
    private final Label lblStatus = new Label();

    /** Khởi tạo giao diện; không nhận tham số. */
    public CustomerView() {
        setSpacing(16);
        setPadding(new Insets(24, 28, 28, 28));
        setStyle("-fx-background-color: #f3f6f3;");

        // Tiêu đề màn hình khách hàng.
        Label lblTitle = new Label("Khách hàng › Cập nhật khách hàng");
        lblTitle.setStyle(
                "-fx-font-size: 22px; -fx-font-weight: bold;"
        );

        txtSearch.setPromptText("Tìm mã, tên, SĐT, email...");
        txtSearch.setPrefWidth(260);
        txtSearch.getStyleClass().add("search-field");

        // Khung chứa ô tìm kiếm.
        HBox search_box = new HBox(txtSearch);
        search_box.getStyleClass().add("search-box");

        // Khoảng trống đẩy các nút sang bên phải.
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnAdd.getStyleClass().add("btn-action-primary");
        btnEdit.getStyleClass().add("btn-action-secondary");
        btnDelete.getStyleClass().add("btn-action-secondary");
        btnRefresh.getStyleClass().add("btn-action-secondary");

        // Thanh công cụ quản lý khách hàng.
        HBox toolbar = new HBox(
                10, search_box, spacer,
                btnAdd, btnEdit, btnDelete, btnRefresh
        );
        toolbar.setAlignment(Pos.CENTER_LEFT);

        createColumns();
        tblCustomers.setItems(filtered_customers);
        tblCustomers.setPlaceholder(
                new Label("Không có khách hàng phù hợp")
        );

        // Khóa thao tác trong lúc đang làm việc với database.
        btnAdd.disableProperty().bind(busy);
        btnRefresh.disableProperty().bind(busy);
        txtSearch.disableProperty().bind(busy);
        tblCustomers.disableProperty().bind(busy);

        // Sửa và ngừng hoạt động cần có khách hàng được chọn.
        btnEdit.disableProperty().bind(
                busy.or(tblCustomers.getSelectionModel()
                        .selectedItemProperty().isNull())
        );
        btnDelete.disableProperty().bind(
                busy.or(tblCustomers.getSelectionModel()
                        .selectedItemProperty().isNull())
        );

        // Gắn các hành động cho nút.
        btnAdd.setOnAction(event -> showCustomerDialog(null));
        btnEdit.setOnAction(event -> showCustomerDialog(
                tblCustomers.getSelectionModel().getSelectedItem()
        ));
        btnDelete.setOnAction(event -> deactivateCustomer());
        btnRefresh.setOnAction(event -> loadCustomers());
        // Hiển thị phím tắt trên nút Làm mới.
        btnRefresh.setText("Làm mới (F5)");

        // Bộ xử lý F5 trên cửa sổ khi màn khách hàng đang được hiển thị.
        EventHandler<KeyEvent> refresh_handler = event -> {
            if (event.getCode() == KeyCode.F5) {
                // Chỉ tải lại khi thao tác trước đã hoàn tất.
                if (!busy.get()) {
                    loadCustomers();
                }

                event.consume();
            }
        };

        // Đăng ký khi mở màn khách hàng, gỡ khi chuyển sang màn khác.
        sceneProperty().addListener((observable, old_scene, new_scene) -> {
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
        });

        // Lọc lại danh sách khi nội dung tìm kiếm thay đổi.
        txtSearch.textProperty().addListener(
                (observable, old_value, new_value) -> filterCustomers()
        );

        // Khung nội dung chứa thanh công cụ và bảng.
        VBox content_box = new VBox(14, toolbar, tblCustomers);
        content_box.setPadding(new Insets(16));
        content_box.getStyleClass().add("chart-panel");

        VBox.setVgrow(tblCustomers, Priority.ALWAYS);
        VBox.setVgrow(content_box, Priority.ALWAYS);

        getChildren().addAll(lblTitle, content_box, lblStatus);
        loadCustomers();
    }

    /** Tạo các cột của bảng; không nhận tham số. */
    private void createColumns() {
        tblCustomers.getColumns().add(
                createTextColumn(
                        "Mã khách hàng", Customer::getMaKhachHang, 180
                )
        );
        tblCustomers.getColumns().add(
                createTextColumn("Họ tên", Customer::getHoTen, 190)
        );
        tblCustomers.getColumns().add(
                createTextColumn(
                        "Số điện thoại", Customer::getSoDienThoai, 135
                )
        );
        tblCustomers.getColumns().add(
                createTextColumn("Email", Customer::getEmail, 210)
        );
        tblCustomers.getColumns().add(
                createTextColumn("Địa chỉ", Customer::getDiaChi, 220)
        );

        // Cột điểm tích lũy chỉ dùng để xem.
        TableColumn<Customer, Integer> points_column =
                new TableColumn<>("Điểm tích lũy");

        points_column.setCellValueFactory(cell ->
                new ReadOnlyObjectWrapper<>(
                        cell.getValue().getDiemTichLuy()
                )
        );
        points_column.setPrefWidth(115);
        tblCustomers.getColumns().add(points_column);

        tblCustomers.getColumns().add(
                createTextColumn("Ghi chú", Customer::getGhiChu, 200)
        );
    }

    /**
     * Tạo cột văn bản liên kết với thuộc tính khách hàng.
     *
     * @param title Tiêu đề cột.
     * @param getter Hàm lấy dữ liệu từ Customer.
     * @param width Chiều rộng ban đầu.
     * @return Cột đã thiết lập.
     */
    private TableColumn<Customer, String> createTextColumn(
            String title,
            Function<Customer, String> getter,
            double width
    ) {
        // Cột hiển thị dữ liệu văn bản của khách hàng.
        TableColumn<Customer, String> column = new TableColumn<>(title);

        column.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(getter.apply(cell.getValue()))
        );
        column.setPrefWidth(width);
        column.setSortable(false);

        return column;
    }

    /** Đọc lại khách hàng từ database ở luồng nền. */
    private void loadCustomers() {
        busy.set(true);
        lblStatus.setText("Đang tải khách hàng...");

        // Tác vụ truy vấn danh sách, không thao tác giao diện trong call().
        Task<List<Customer>> load_task = new Task<>() {
            @Override
            protected List<Customer> call() throws SQLException {
                return customer_dao.findAll();
            }
        };

        load_task.setOnSucceeded(event -> {
            customer_list.setAll(load_task.getValue());
            busy.set(false);
            filterCustomers();
        });

        load_task.setOnFailed(event -> {
            busy.set(false);
            lblStatus.setText("Không tải được danh sách khách hàng");
            showError(load_task.getException());
        });

        // Luồng truy vấn tự kết thúc cùng ứng dụng.
        Thread worker = new Thread(load_task, "load-customers");
        worker.setDaemon(true);
        worker.start();
    }

    /**
     * Chuẩn hóa từ khóa để tìm không phân biệt hoa thường và dấu.
     *
     * @param value Chuỗi cần chuẩn hóa; có thể null.
     * @return Chuỗi chữ thường không dấu.
     */
    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }

        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    /**
     * Lọc tất cả khách hàng khớp mã, tên, số điện thoại hoặc email.
     * Để trống từ khóa thì hiện toàn bộ danh sách đang hoạt động.
     */
    private void filterCustomers() {
        // Từ khóa đã bỏ dấu và chuyển thành chữ thường.
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
                        + " / " + customer_list.size()
                        + " khách hàng đang hoạt động"
        );
    }

    /**
     * Mở form thêm hoặc sửa khách hàng.
     *
     * @param selected Khách hàng cần sửa; null nếu thêm mới.
     */
    private void showCustomerDialog(Customer selected) {
        // Xác định form đang dùng để thêm hay sửa.
        boolean editing = selected != null;

        // Hộp thoại nhập thông tin khách hàng.
        Dialog<Customer> dialog = new Dialog<>();
        dialog.initOwner(getScene().getWindow());
        dialog.setTitle(editing ? "Sửa khách hàng" : "Thêm khách hàng");
        dialog.setHeaderText(
                editing
                        ? "Mã khách hàng: " + selected.getMaKhachHang()
                        : "Mã được tạo tự động khi lưu"
        );
        dialog.getDialogPane().setPrefWidth(520);

        // Các ô nhập thông tin khách hàng.
        TextField txtName = new TextField();
        TextField txtPhone = new TextField();
        TextField txtEmail = new TextField();
        TextField txtAddress = new TextField();
        TextArea txaNote = new TextArea();

        txaNote.setPrefRowCount(3);
        txaNote.setWrapText(true);
        txtPhone.setPromptText("10 chữ số, bắt đầu bằng 0");
        txtEmail.setPromptText("Có thể để trống");

        // Chỉ cho nhập tối đa 10 chữ số vào ô số điện thoại.
        txtPhone.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("[0-9]{0,10}")
                        ? change : null
        ));

        // Đổ thông tin khách hàng đã chọn vào form sửa.
        if (editing) {
            txtName.setText(selected.getHoTen());
            txtPhone.setText(selected.getSoDienThoai());
            txtEmail.setText(selected.getEmail());
            txtAddress.setText(selected.getDiaChi());
            txaNote.setText(selected.getGhiChu());
        }

        // Nhãn hiển thị lỗi nhập liệu ngay trong form.
        Label lblValidation = new Label();
        lblValidation.setStyle("-fx-text-fill: #b3261e;");
        lblValidation.setWrapText(true);

        // Bố cục các trường thông tin.
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(16));

        // Cột nhãn có đủ chiều rộng để hiển thị tên trường.
        ColumnConstraints label_column = new ColumnConstraints();
        label_column.setMinWidth(120);
        label_column.setPrefWidth(120);

        // Cột nhập liệu chiếm phần chiều rộng còn lại.
        ColumnConstraints input_column = new ColumnConstraints();
        input_column.setMinWidth(240);
        input_column.setHgrow(Priority.ALWAYS);
        input_column.setFillWidth(true);

        // Áp dụng kích thước cho hai cột của form.
        form.getColumnConstraints().setAll(label_column, input_column);

        form.addRow(0, new Label("Họ tên *"), txtName);
        form.addRow(1, new Label("Số điện thoại *"), txtPhone);
        form.addRow(2, new Label("Email"), txtEmail);
        form.addRow(3, new Label("Địa chỉ"), txtAddress);
        form.addRow(4, new Label("Ghi chú"), txaNote);
        form.add(lblValidation, 0, 5, 2, 1);

        GridPane.setHgrow(txtName, Priority.ALWAYS);
        GridPane.setHgrow(txtPhone, Priority.ALWAYS);
        GridPane.setHgrow(txtEmail, Priority.ALWAYS);
        GridPane.setHgrow(txtAddress, Priority.ALWAYS);
        GridPane.setHgrow(txaNote, Priority.ALWAYS);

        dialog.getDialogPane().setContent(form);

        // Loại nút xác nhận lưu thông tin.
        ButtonType save_type = new ButtonType(
                "Lưu", ButtonBar.ButtonData.OK_DONE
        );
        dialog.getDialogPane().getButtonTypes().addAll(
                save_type, ButtonType.CANCEL
        );

        // Nút Lưu thực tế để chặn đóng form khi dữ liệu không hợp lệ.
        Button btnSave = (Button) dialog.getDialogPane()
                .lookupButton(save_type);

        btnSave.addEventFilter(ActionEvent.ACTION, event -> {
            // Thông báo kiểm tra các trường hiện tại.
            String validation_message = validateCustomer(
                    txtName.getText(),
                    txtPhone.getText(),
                    txtEmail.getText(),
                    txtAddress.getText(),
                    txaNote.getText()
            );

            if (validation_message != null) {
                lblValidation.setText(validation_message);
                event.consume();
            }
        });

        // Chuyển dữ liệu form thành Customer khi người dùng bấm Lưu.
        dialog.setResultConverter(button_type -> {
            if (button_type != save_type) {
                return null;
            }

            // Đối tượng mới, không sửa trực tiếp dòng đang hiển thị.
            Customer customer = new Customer(
                    editing ? selected.getMaKhachHang() : null,
                    txtName.getText().trim(),
                    txtPhone.getText().trim(),
                    nullableText(txtEmail.getText()),
                    editing ? selected.getDiemTichLuy() : 0,
                    nullableText(txtAddress.getText()),
                    nullableText(txaNote.getText())
            );

            return customer;
        });

        // Chỉ lưu khi người dùng xác nhận và form hợp lệ.
        dialog.showAndWait().ifPresent(customer -> {
            // Tác vụ thêm hoặc sửa khách hàng.
            Task<Void> save_task = new Task<>() {
                @Override
                protected Void call() throws SQLException {
                    if (editing) {
                        customer_dao.update(customer);
                    } else {
                        customer_dao.insert(customer);
                    }
                    return null;
                }
            };

            runWriteTask(
                    save_task,
                    editing
                            ? "Đã cập nhật khách hàng."
                            : "Đã thêm khách hàng."
            );
        });
    }

    /**
     * Kiểm tra thông tin nhập theo giới hạn bảng KhachHang.
     *
     * @param name Họ tên khách hàng.
     * @param phone Số điện thoại.
     * @param email Địa chỉ email.
     * @param address Địa chỉ liên hệ.
     * @param note Ghi chú khách hàng.
     * @return Thông báo lỗi; null nếu hợp lệ.
     */
    private String validateCustomer(
            String name, String phone, String email,
            String address, String note
    ) {
        if (name.trim().isEmpty() || name.trim().length() > 100) {
            return "Họ tên phải có từ 1 đến 100 ký tự.";
        }

        if (!phone.trim().matches("0[0-9]{9}")) {
            return "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0.";
        }

        if (!email.trim().isEmpty()
                && (email.trim().length() > 100
                || !email.trim().matches(
                        "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
                ))) {
            return "Email không hợp lệ hoặc dài quá 100 ký tự.";
        }

        if (address.trim().length() > 255) {
            return "Địa chỉ không được dài quá 255 ký tự.";
        }

        if (note.trim().length() > 255) {
            return "Ghi chú không được dài quá 255 ký tự.";
        }

        return null;
    }

    /**
     * Chuyển chuỗi trống thành null để lưu các trường không bắt buộc.
     *
     * @param value Chuỗi lấy từ ô nhập.
     * @return Chuỗi đã bỏ khoảng trắng đầu cuối hoặc null.
     */
    private String nullableText(String value) {
        return value == null || value.trim().isEmpty()
                ? null : value.trim();
    }

    /** Xác nhận và ngừng hoạt động khách hàng đang chọn. */
    private void deactivateCustomer() {
        // Khách hàng được chọn trên bảng.
        Customer selected =
                tblCustomers.getSelectionModel().getSelectedItem();

        if (selected == null) {
            return;
        }

        // Hộp thoại xác nhận xóa mềm.
        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Ngừng hoạt động khách hàng "
                        + selected.getHoTen()
                        + " (" + selected.getMaKhachHang() + ")?\n"
                        + "Thông tin và lịch sử trong database được giữ lại.",
                ButtonType.YES,
                ButtonType.NO
        );
        confirmation.initOwner(getScene().getWindow());
        confirmation.setTitle("Ngừng hoạt động khách hàng");
        confirmation.setHeaderText("Xác nhận thao tác");

        // Chỉ thực hiện khi người dùng chọn Yes.
        if (confirmation.showAndWait()
                .orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }

        // Tác vụ cập nhật trạng thái khách hàng.
        Task<Void> delete_task = new Task<>() {
            @Override
            protected Void call() throws SQLException {
                customer_dao.deactivate(selected.getMaKhachHang());
                return null;
            }
        };

        runWriteTask(delete_task, "Đã ngừng hoạt động khách hàng.");
    }

    /**
     * Chạy thao tác ghi dữ liệu ở luồng nền rồi tải lại bảng.
     *
     * @param write_task Tác vụ thêm, sửa hoặc ngừng hoạt động.
     * @param success_message Thông báo khi lưu thành công.
     */
    private void runWriteTask(
            Task<Void> write_task, String success_message
    ) {
        busy.set(true);
        lblStatus.setText("Đang lưu dữ liệu...");

        write_task.setOnSucceeded(event -> {
            // Thông báo xác nhận thao tác thành công.
            Alert information = new Alert(
                    Alert.AlertType.INFORMATION, success_message
            );
            information.initOwner(getScene().getWindow());
            information.setTitle("Thành công");
            information.setHeaderText(null);
            information.showAndWait();

            loadCustomers();
        });

        write_task.setOnFailed(event -> {
            busy.set(false);
            lblStatus.setText("Lưu dữ liệu thất bại");
            showError(write_task.getException());
        });

        // Luồng ghi dữ liệu tự kết thúc cùng ứng dụng.
        Thread worker = new Thread(write_task, "save-customer");
        worker.setDaemon(true);
        worker.start();
    }

    /**
     * Hiển thị lỗi thao tác database.
     *
     * @param error Lỗi do tác vụ truy vấn hoặc lưu dữ liệu trả về.
     */
    private void showError(Throwable error) {
        error.printStackTrace();

        // Thông báo mặc định khi thao tác không thành công.
        String message = "Không thực hiện được thao tác. "
                + "Hãy kiểm tra kết nối và thông báo trong terminal.";

        // Nhận diện lỗi trùng dữ liệu do SQL Server trả về.
        if (error instanceof SQLException) {
            // Lỗi SQL cần đọc mã để hiển thị thông báo phù hợp.
            SQLException sql_error = (SQLException) error;

            if (sql_error.getErrorCode() == 2601
                    || sql_error.getErrorCode() == 2627) {
                message = "Dữ liệu bị trùng. Kiểm tra số điện thoại; "
                        + "khách hàng đã ngừng hoạt động vẫn giữ số này.";
            } else if (sql_error.getErrorCode() == 207) {
                message = "Database thiếu cột hoặc tên cột không đúng. "
                        + "Kiểm tra đã chạy SQL bổ sung trangThai chưa.";
            } else {
                message = sql_error.getMessage();
            }
        }

        // Hộp thoại thông báo lỗi cho người sử dụng.
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.initOwner(getScene().getWindow());
        alert.setTitle("Không thực hiện được thao tác");
        alert.setHeaderText("Thao tác thất bại");
        alert.showAndWait();
    }
}