package com.petshop.view.employee;

import com.petshop.controller.EmployeeController;
import com.petshop.model.Employee;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Màn hình Tìm kiếm nhân viên (EMP_SEARCH - Hạn: 19/10/2026).
 * Cho phép tra cứu đa tiêu chí: mã nhân viên, họ tên, số điện thoại, email, chức vụ,
 * trạng thái làm việc và trạng thái tài khoản hệ thống.
 *
 * Tiêu chuẩn Definition of Done:
 * - Đặt tên điều khiển đúng tiền tố: txt, cbo, tbl, lbl, btn.
 * - Các nút hiển thị tên đầy đủ không co rút (setMinWidth(Region.USE_PREF_SIZE)).
 * - Tên lớp là danh từ, tên phương thức là động từ, comment đầy đủ.
 */
public class EmployeeSearchView extends VBox {

    private final EmployeeController employeeController = new EmployeeController();
    private final ObservableList<Employee> masterEmployeeList = FXCollections.observableArrayList();
    private final FilteredList<Employee> filteredEmployeeList = new FilteredList<>(masterEmployeeList, e -> true);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Điều khiển tuân thủ đúng tiền tố
    private TextField txtSearch;
    private ComboBox<String> cboRoleFilter;
    private ComboBox<String> cboStatusFilter;
    private ComboBox<String> cboAccountFilter;
    private Button btnSearch;
    private Button btnRefresh;
    private Button btnViewDetail;

    private TableView<Employee> tblEmployees;
    private Label lblStatusCount;

    public EmployeeSearchView() {
        getStyleClass().add("employee-page");
        setPadding(new Insets(24));
        setSpacing(18);
        VBox.setVgrow(this, Priority.ALWAYS);

        khoiTaoGiaoDien();
        ganSuKien();
        napDuLieuTuCoSoDuLieu();
    }

    private void khoiTaoGiaoDien() {
        // 1. Header Box
        VBox pnlHeader = new VBox(4);
        Label lblTitle = new Label("TÌM KIẾM NHÂN VIÊN");
        lblTitle.getStyleClass().add("employee-title");

        Label lblSubtitle = new Label("Tra cứu thông tin hồ sơ nhân viên, chức vụ, trạng thái làm việc và tài khoản hệ thống.");
        lblSubtitle.getStyleClass().add("employee-muted");
        lblSubtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        pnlHeader.getChildren().addAll(lblTitle, lblSubtitle);

        // 2. Filter Card
        VBox pnlFilterCard = new VBox(12);
        pnlFilterCard.getStyleClass().add("employee-card");
        pnlFilterCard.setPadding(new Insets(16));

        Label lblFilterTitle = new Label("BỘ LỌC TÌM KIẾM ĐA TIÊU CHÍ");
        lblFilterTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #334155;");

        HBox pnlFilterRow = new HBox(12);
        pnlFilterRow.setAlignment(Pos.CENTER_LEFT);

        txtSearch = new TextField();
        txtSearch.setPromptText("🔍 Nhập mã nhân viên, họ tên, SĐT hoặc email...");
        txtSearch.setPrefHeight(36);
        HBox.setHgrow(txtSearch, Priority.ALWAYS);

        cboRoleFilter = new ComboBox<>();
        cboRoleFilter.getItems().addAll("Tất cả chức vụ", "Quản lý", "Bán hàng", "Chăm sóc");
        cboRoleFilter.setValue("Tất cả chức vụ");
        cboRoleFilter.setPrefHeight(36);

        cboStatusFilter = new ComboBox<>();
        cboStatusFilter.getItems().addAll("Tất cả trạng thái", "Đang làm", "Đã nghỉ việc");
        cboStatusFilter.setValue("Tất cả trạng thái");
        cboStatusFilter.setPrefHeight(36);

        cboAccountFilter = new ComboBox<>();
        cboAccountFilter.getItems().addAll("Tất cả tài khoản", "Đã có tài khoản", "Chưa cấp tài khoản");
        cboAccountFilter.setValue("Tất cả tài khoản");
        cboAccountFilter.setPrefHeight(36);

        btnSearch = new Button("Tìm kiếm");
        btnSearch.getStyleClass().addAll("employee-add");
        btnSearch.setMinWidth(Region.USE_PREF_SIZE);
        btnSearch.setPrefHeight(36);

        btnRefresh = new Button("Làm mới");
        btnRefresh.getStyleClass().addAll("employee-secondary");
        btnRefresh.setMinWidth(Region.USE_PREF_SIZE);
        btnRefresh.setPrefHeight(36);

        btnViewDetail = new Button("👁 Xem chi tiết");
        btnViewDetail.getStyleClass().addAll("employee-secondary");
        btnViewDetail.setMinWidth(Region.USE_PREF_SIZE);
        btnViewDetail.setPrefHeight(36);
        btnViewDetail.setDisable(true);

        pnlFilterRow.getChildren().addAll(
                txtSearch, cboRoleFilter, cboStatusFilter, cboAccountFilter,
                btnSearch, btnRefresh, btnViewDetail
        );

        pnlFilterCard.getChildren().addAll(lblFilterTitle, pnlFilterRow);

        // 3. Table Card
        VBox pnlTableCard = new VBox(12);
        pnlTableCard.getStyleClass().add("employee-card");
        pnlTableCard.setPadding(new Insets(16));
        VBox.setVgrow(pnlTableCard, Priority.ALWAYS);

        tblEmployees = new TableView<>();
        tblEmployees.setItems(filteredEmployeeList);
        tblEmployees.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblEmployees.setPlaceholder(new Label("Không tìm thấy nhân viên nào phù hợp."));
        VBox.setVgrow(tblEmployees, Priority.ALWAYS);

        // Cột 1: Mã NV
        TableColumn<Employee, String> colId = new TableColumn<>("Mã NV");
        colId.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().maNhanVien()));
        colId.setPrefWidth(140);
        colId.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        // Cột 2: Ảnh & Họ tên
        TableColumn<Employee, Employee> colName = new TableColumn<>("Họ và tên");
        colName.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        colName.setPrefWidth(200);
        colName.setCellFactory(col -> new TableCell<>() {
            private final ImageView imgAvatar = new ImageView();
            private final Label lblEmpName = new Label();
            private final HBox pnlCellBox = new HBox(10, imgAvatar, lblEmpName);

            {
                imgAvatar.setFitWidth(32);
                imgAvatar.setFitHeight(32);
                imgAvatar.setPreserveRatio(true);
                imgAvatar.setSmooth(true);
                Circle clip = new Circle(16, 16, 16);
                imgAvatar.setClip(clip);
                pnlCellBox.setAlignment(Pos.CENTER_LEFT);
                lblEmpName.setStyle("-fx-font-weight: bold;");
            }

            @Override
            protected void updateItem(Employee item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    lblEmpName.setText(item.hoTen());
                    if (item.anh() != null && item.anh().length > 0) {
                        try {
                            imgAvatar.setImage(new Image(new ByteArrayInputStream(item.anh())));
                        } catch (Exception ex) {
                            setDefaultLogo();
                        }
                    } else {
                        setDefaultLogo();
                    }
                    setGraphic(pnlCellBox);
                }
            }

            private void setDefaultLogo() {
                try {
                    imgAvatar.setImage(new Image(getClass().getResourceAsStream("/com/petshop/view/logo.png")));
                } catch (Exception ignored) {}
            }
        });

        // Cột 3: Chức vụ
        TableColumn<Employee, String> colJob = new TableColumn<>("Chức vụ");
        colJob.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().chucVu()));
        colJob.setPrefWidth(100);
        colJob.setStyle("-fx-alignment: CENTER;");

        // Cột 4: Giới tính
        TableColumn<Employee, String> colGender = new TableColumn<>("Giới tính");
        colGender.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().nam() ? "Nam" : "Nữ"));
        colGender.setPrefWidth(70);
        colGender.setStyle("-fx-alignment: CENTER;");

        // Cột 5: Ngày sinh
        TableColumn<Employee, String> colBirth = new TableColumn<>("Ngày sinh");
        colBirth.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().ngaySinh() != null ? DATE_FORMATTER.format(c.getValue().ngaySinh()) : ""));
        colBirth.setPrefWidth(95);
        colBirth.setStyle("-fx-alignment: CENTER;");

        // Cột 6: SĐT
        TableColumn<Employee, String> colPhone = new TableColumn<>("Số điện thoại");
        colPhone.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().soDienThoai()));
        colPhone.setPrefWidth(110);
        colPhone.setStyle("-fx-alignment: CENTER;");

        // Cột 7: Email
        TableColumn<Employee, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().email() != null ? c.getValue().email() : ""));
        colEmail.setPrefWidth(150);

        // Cột 8: Ngày vào làm
        TableColumn<Employee, String> colStart = new TableColumn<>("Ngày vào làm");
        colStart.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().ngayBatDau() != null ? DATE_FORMATTER.format(c.getValue().ngayBatDau()) : ""));
        colStart.setPrefWidth(95);
        colStart.setStyle("-fx-alignment: CENTER;");

        // Cột 9: Trạng thái làm việc
        TableColumn<Employee, Employee> colStatus = new TableColumn<>("Trạng thái");
        colStatus.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        colStatus.setPrefWidth(110);
        colStatus.setStyle("-fx-alignment: CENTER;");
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Employee item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label lbl = new Label(item.trangThai());
                    lbl.getStyleClass().add("Đang làm".equals(item.trangThai()) ? "badge-active" : "badge-locked");
                    setGraphic(lbl);
                }
            }
        });

        // Cột 10: Tài khoản & Phân quyền
        TableColumn<Employee, Employee> colAccount = new TableColumn<>("Tài khoản / Quyền");
        colAccount.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        colAccount.setPrefWidth(140);
        colAccount.setStyle("-fx-alignment: CENTER;");
        colAccount.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Employee item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else if (item.tenDangNhap() != null && !item.tenDangNhap().isEmpty()) {
                    Label lbl = new Label(item.tenDangNhap() + " (" + (item.phanQuyen() != null ? item.phanQuyen() : "") + ")");
                    lbl.getStyleClass().add(Boolean.TRUE.equals(item.taiKhoanHoatDong()) ? "badge-admin" : "badge-locked");
                    setGraphic(lbl);
                } else {
                    Label lbl = new Label("Chưa cấp");
                    lbl.getStyleClass().add("badge-none");
                    setGraphic(lbl);
                }
            }
        });

        tblEmployees.getColumns().addAll(colId, colName, colJob, colGender, colBirth, colPhone, colEmail, colStart, colStatus, colAccount);

        // Footer count
        lblStatusCount = new Label("Đang tải danh sách nhân viên...");
        lblStatusCount.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        pnlTableCard.getChildren().addAll(tblEmployees, lblStatusCount);

        getChildren().addAll(pnlHeader, pnlFilterCard, pnlTableCard);
    }

    private void ganSuKien() {
        // Tìm kiếm tự động với debounce 250ms
        PauseTransition debounce = new PauseTransition(Duration.millis(250));
        txtSearch.textProperty().addListener((obs, oldV, newV) -> {
            debounce.setOnFinished(e -> apDungBoLoc());
            debounce.playFromStart();
        });

        cboRoleFilter.valueProperty().addListener((obs, oldV, newV) -> apDungBoLoc());
        cboStatusFilter.valueProperty().addListener((obs, oldV, newV) -> apDungBoLoc());
        cboAccountFilter.valueProperty().addListener((obs, oldV, newV) -> apDungBoLoc());

        btnSearch.setOnAction(e -> apDungBoLoc());
        btnRefresh.setOnAction(e -> napDuLieuTuCoSoDuLieu());

        // Bật/tắt nút xem chi tiết theo dòng được chọn
        tblEmployees.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            btnViewDetail.setDisable(newV == null);
        });

        btnViewDetail.setOnAction(e -> {
            Employee selected = tblEmployees.getSelectionModel().getSelectedItem();
            if (selected != null) {
                hienThiChiTietHoSo(selected);
            }
        });

        // Nháy đúp chuột để mở xem chi tiết
        tblEmployees.setRowFactory(tv -> {
            TableRow<Employee> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    hienThiChiTietHoSo(row.getItem());
                }
            });
            return row;
        });
    }

    private void hienThiChiTietHoSo(Employee employee) {
        EmployeeDetailDialog.show(employee, getScene() != null ? getScene().getWindow() : null);
    }

    private void apDungBoLoc() {
        String keyword = txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";
        String selectedRole = cboRoleFilter.getValue();
        String selectedStatus = cboStatusFilter.getValue();
        String selectedAccount = cboAccountFilter.getValue();

        filteredEmployeeList.setPredicate(emp -> {
            if (emp == null) return false;

            // 1. Lọc chức vụ
            if (selectedRole != null && !selectedRole.equals("Tất cả chức vụ")) {
                if (!selectedRole.equalsIgnoreCase(emp.chucVu())) {
                    return false;
                }
            }

            // 2. Lọc trạng thái làm việc
            if (selectedStatus != null && !selectedStatus.equals("Tất cả trạng thái")) {
                if (!selectedStatus.equalsIgnoreCase(emp.trangThai())) {
                    return false;
                }
            }

            // 3. Lọc tài khoản
            if (selectedAccount != null && !selectedAccount.equals("Tất cả tài khoản")) {
                boolean hasAccount = emp.tenDangNhap() != null && !emp.tenDangNhap().isEmpty();
                if (selectedAccount.equals("Đã có tài khoản") && !hasAccount) {
                    return false;
                }
                if (selectedAccount.equals("Chưa cấp tài khoản") && hasAccount) {
                    return false;
                }
            }

            // 4. Lọc từ khóa
            if (keyword.isEmpty()) return true;

            boolean matchId = emp.maNhanVien() != null && emp.maNhanVien().toLowerCase().contains(keyword);
            boolean matchName = emp.hoTen() != null && emp.hoTen().toLowerCase().contains(keyword);
            boolean matchPhone = emp.soDienThoai() != null && emp.soDienThoai().contains(keyword);
            boolean matchEmail = emp.email() != null && emp.email().toLowerCase().contains(keyword);

            return matchId || matchName || matchPhone || matchEmail;
        });

        lblStatusCount.setText("Tìm thấy " + filteredEmployeeList.size() + " / " + masterEmployeeList.size() + " nhân viên trong hệ thống");
    }

    private void napDuLieuTuCoSoDuLieu() {
        lblStatusCount.setText("Đang tải dữ liệu từ CSDL...");
        new Thread(() -> {
            try {
                List<Employee> list = employeeController.loadEmployees();
                Platform.runLater(() -> {
                    masterEmployeeList.setAll(list);
                    apDungBoLoc();
                    if (!filteredEmployeeList.isEmpty()) {
                        tblEmployees.getSelectionModel().selectFirst();
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    lblStatusCount.setText("Lỗi tải dữ liệu: " + ex.getMessage());
                });
            }
        }).start();
    }
}

