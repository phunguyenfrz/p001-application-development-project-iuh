package com.petshop.view.employee;

import com.petshop.controller.EmployeeController;
import com.petshop.model.Employee;
import com.petshop.model.User;
import com.petshop.util.SessionManager;
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
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.List;

/**
 * Màn hình Quản lý Phân quyền nhân viên (EMP_ROLES - Hạn: 22/10/2026).
 * Cho phép thiết lập và cập nhật vai trò phân quyền hệ thống cho nhân viên:
 * Admin, Quản lý, Bán hàng, Chăm sóc kèm ma trận quyền hạn chi tiết.
 *
 * Tiêu chuẩn Definition of Done:
 * - Đặt tên điều khiển đúng tiền tố: txt, cbo, tbl, lbl, btn.
 * - Các nút hiển thị tên đầy đủ không co rút (setMinWidth(Region.USE_PREF_SIZE)).
 * - Tên lớp là danh từ, tên phương thức là động từ, comment đầy đủ.
 */
public class EmployeeRolesView extends VBox {

    private final EmployeeController employeeController = new EmployeeController();
    private final ObservableList<Employee> masterEmployeeList = FXCollections.observableArrayList();
    private final FilteredList<Employee> filteredEmployeeList = new FilteredList<>(masterEmployeeList, e -> true);

    // Điều khiển tuân thủ đúng tiền tố
    private TextField txtSearchRole;
    private ComboBox<String> cboRoleFilter;
    private TableView<Employee> tblRoleEmployees;
    private Label lblStatusCount;

    // Khối thông tin phân quyền bên phải
    private Label lblSelectedName;
    private Label lblSelectedId;
    private Label lblSelectedAccount;
    private Label lblSelectedCurrentRole;
    private ComboBox<String> cboNewRole;
    private VBox pnlRoleDescriptionCard;
    private Label lblRoleDescTitle;
    private Label lblRoleDescContent;
    private Label lblWarning;
    private Button btnSaveRole;
    private Button btnRefreshRoles;

    public EmployeeRolesView() {
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
        Label lblTitle = new Label("QUẢN LÝ PHÂN QUYỀN NHÂN VIÊN");
        lblTitle.getStyleClass().add("employee-title");

        Label lblSubtitle = new Label("Thiết lập và phân bổ vai trò quản trị hệ thống cho nhân viên: Admin, Quản lý, Bán hàng, Chăm sóc.");
        lblSubtitle.getStyleClass().add("employee-muted");
        lblSubtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        pnlHeader.getChildren().addAll(lblTitle, lblSubtitle);

        // 2. Nội dung 2 cột
        HBox pnlBody = new HBox(16);
        VBox.setVgrow(pnlBody, Priority.ALWAYS);

        // --- CỘT TRÁI: BẢNG NHÂN VIÊN & PHÂN QUYỀN ---
        VBox pnlLeftCard = new VBox(12);
        pnlLeftCard.getStyleClass().add("employee-card");
        pnlLeftCard.setPadding(new Insets(16));
        HBox.setHgrow(pnlLeftCard, Priority.ALWAYS);

        HBox pnlSearchRow = new HBox(10);
        pnlSearchRow.setAlignment(Pos.CENTER_LEFT);

        txtSearchRole = new TextField();
        txtSearchRole.setPromptText("🔍 Tìm theo họ tên, mã NV hoặc tài khoản...");
        txtSearchRole.setPrefHeight(36);
        HBox.setHgrow(txtSearchRole, Priority.ALWAYS);

        cboRoleFilter = new ComboBox<>();
        cboRoleFilter.getItems().addAll("Tất cả vai trò", "Admin", "Quản lý", "Bán hàng", "Chăm sóc", "Chưa cấp TK");
        cboRoleFilter.setValue("Tất cả vai trò");
        cboRoleFilter.setPrefHeight(36);

        pnlSearchRow.getChildren().addAll(txtSearchRole, cboRoleFilter);

        tblRoleEmployees = new TableView<>();
        tblRoleEmployees.setItems(filteredEmployeeList);
        tblRoleEmployees.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblRoleEmployees.setPlaceholder(new Label("Không có nhân viên nào."));
        VBox.setVgrow(tblRoleEmployees, Priority.ALWAYS);

        TableColumn<Employee, String> colId = new TableColumn<>("Mã NV");
        colId.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().maNhanVien()));
        colId.setPrefWidth(125);
        colId.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        TableColumn<Employee, String> colName = new TableColumn<>("Họ và tên");
        colName.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().hoTen()));
        colName.setPrefWidth(160);

        TableColumn<Employee, String> colJob = new TableColumn<>("Chức vụ");
        colJob.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().chucVu()));
        colJob.setPrefWidth(95);
        colJob.setStyle("-fx-alignment: CENTER;");

        TableColumn<Employee, String> colUsername = new TableColumn<>("Tài khoản");
        colUsername.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().tenDangNhap() != null ? c.getValue().tenDangNhap() : "-"));
        colUsername.setPrefWidth(110);
        colUsername.setStyle("-fx-alignment: CENTER;");

        TableColumn<Employee, Employee> colRole = new TableColumn<>("Phân quyền hệ thống");
        colRole.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        colRole.setPrefWidth(140);
        colRole.setStyle("-fx-alignment: CENTER;");
        colRole.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Employee item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else if (item.phanQuyen() != null && !item.phanQuyen().isEmpty()) {
                    Label lbl = new Label(item.phanQuyen());
                    lbl.getStyleClass().add(getRoleBadgeClass(item.phanQuyen()));
                    setGraphic(lbl);
                } else {
                    Label lbl = new Label("Chưa cấp TK");
                    lbl.getStyleClass().add("badge-none");
                    setGraphic(lbl);
                }
            }
        });

        TableColumn<Employee, Employee> colAccStatus = new TableColumn<>("Trạng thái");
        colAccStatus.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        colAccStatus.setPrefWidth(110);
        colAccStatus.setStyle("-fx-alignment: CENTER;");
        colAccStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Employee item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else if (item.tenDangNhap() == null) {
                    Label lbl = new Label("Chưa có TK");
                    lbl.getStyleClass().add("badge-none");
                    setGraphic(lbl);
                } else if (Boolean.TRUE.equals(item.taiKhoanHoatDong())) {
                    Label lbl = new Label("Hoạt động");
                    lbl.getStyleClass().add("badge-active");
                    setGraphic(lbl);
                } else {
                    Label lbl = new Label("Bị khóa");
                    lbl.getStyleClass().add("badge-locked");
                    setGraphic(lbl);
                }
            }
        });

        tblRoleEmployees.getColumns().addAll(colId, colName, colJob, colUsername, colRole, colAccStatus);

        lblStatusCount = new Label("Đang tải dữ liệu...");
        lblStatusCount.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        pnlLeftCard.getChildren().addAll(pnlSearchRow, tblRoleEmployees, lblStatusCount);

        // --- CỘT PHẢI: BẢNG ĐIỀU KHIỂN & MA TRẬN PHÂN QUYỀN ---
        VBox pnlRightCard = new VBox(14);
        pnlRightCard.getStyleClass().add("employee-card");
        pnlRightCard.setPadding(new Insets(20));
        pnlRightCard.setPrefWidth(380);
        pnlRightCard.setMinWidth(360);

        Label lblRightTitle = new Label("THIẾT LẬP PHÂN QUYỀN");
        lblRightTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        GridPane gridSelected = new GridPane();
        gridSelected.setHgap(12);
        gridSelected.setVgap(8);

        gridSelected.add(createFieldLabel("Họ và tên:"), 0, 0);
        lblSelectedName = createFieldValue("Vui lòng chọn nhân viên");
        gridSelected.add(lblSelectedName, 1, 0);

        gridSelected.add(createFieldLabel("Mã nhân viên:"), 0, 1);
        lblSelectedId = createFieldValue("-");
        gridSelected.add(lblSelectedId, 1, 1);

        gridSelected.add(createFieldLabel("Tài khoản:"), 0, 2);
        lblSelectedAccount = createFieldValue("-");
        gridSelected.add(lblSelectedAccount, 1, 2);

        gridSelected.add(createFieldLabel("Quyền hiện tại:"), 0, 3);
        lblSelectedCurrentRole = createFieldValue("-");
        gridSelected.add(lblSelectedCurrentRole, 1, 3);

        Label lblSelectNewRole = new Label("CHỌN VAI TRÒ MỚI:");
        lblSelectNewRole.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #334155;");

        cboNewRole = new ComboBox<>();
        cboNewRole.getItems().addAll("Admin", "Quản lý", "Bán hàng", "Chăm sóc");
        cboNewRole.setValue("Bán hàng");
        cboNewRole.setPrefHeight(36);
        cboNewRole.setMaxWidth(Double.MAX_VALUE);
        cboNewRole.setDisable(true);

        // Khối mô tả quyền hạn
        pnlRoleDescriptionCard = new VBox(8);
        pnlRoleDescriptionCard.setPadding(new Insets(14));
        pnlRoleDescriptionCard.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        lblRoleDescTitle = new Label("QUYỀN HẠN CỦA VAI TRÒ BÁN HÀNG:");
        lblRoleDescTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #0369a1;");

        lblRoleDescContent = new Label();
        lblRoleDescContent.setWrapText(true);
        lblRoleDescContent.setStyle("-fx-font-size: 12px; -fx-text-fill: #334155; -fx-line-spacing: 3px;");
        capNhatMoTaVaiTro("Bán hàng");

        pnlRoleDescriptionCard.getChildren().addAll(lblRoleDescTitle, lblRoleDescContent);

        lblWarning = new Label();
        lblWarning.setWrapText(true);
        lblWarning.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #b91c1c; -fx-font-weight: bold;");

        HBox pnlActionButtons = new HBox(10);
        pnlActionButtons.setAlignment(Pos.CENTER_RIGHT);

        btnRefreshRoles = new Button("Làm mới");
        btnRefreshRoles.getStyleClass().addAll("employee-secondary");
        btnRefreshRoles.setMinWidth(Region.USE_PREF_SIZE);
        btnRefreshRoles.setPrefHeight(36);

        btnSaveRole = new Button("💾 Lưu phân quyền");
        btnSaveRole.getStyleClass().addAll("employee-add");
        btnSaveRole.setMinWidth(Region.USE_PREF_SIZE);
        btnSaveRole.setPrefHeight(36);
        btnSaveRole.setDisable(true);

        pnlActionButtons.getChildren().addAll(btnRefreshRoles, btnSaveRole);

        pnlRightCard.getChildren().addAll(
                lblRightTitle, new Separator(), gridSelected, new Separator(),
                lblSelectNewRole, cboNewRole, pnlRoleDescriptionCard, lblWarning,
                new Separator(), pnlActionButtons
        );

        pnlBody.getChildren().addAll(pnlLeftCard, pnlRightCard);
        getChildren().addAll(pnlHeader, pnlBody);
    }

    private void ganSuKien() {
        // Debounce tìm kiếm
        PauseTransition debounce = new PauseTransition(Duration.millis(250));
        txtSearchRole.textProperty().addListener((obs, oldV, newV) -> {
            debounce.setOnFinished(e -> apDungBoLoc());
            debounce.playFromStart();
        });

        cboRoleFilter.valueProperty().addListener((obs, oldV, newV) -> apDungBoLoc());

        // Lắng nghe chọn dòng nhân viên
        tblRoleEmployees.getSelectionModel().selectedItemProperty().addListener((obs, oldV, selected) -> {
            hienThiThongTinNhanVienDuocChon(selected);
        });

        // Lắng nghe thay đổi ComboBox vai trò mới
        cboNewRole.valueProperty().addListener((obs, oldV, newRole) -> {
            capNhatMoTaVaiTro(newRole);
        });

        btnRefreshRoles.setOnAction(e -> napDuLieuTuCoSoDuLieu());
        btnSaveRole.setOnAction(e -> luuPhanQuyen());
    }

    private void hienThiThongTinNhanVienDuocChon(Employee employee) {
        if (employee == null) {
            lblSelectedName.setText("Vui lòng chọn nhân viên");
            lblSelectedId.setText("-");
            lblSelectedAccount.setText("-");
            lblSelectedCurrentRole.setText("-");
            cboNewRole.setDisable(true);
            btnSaveRole.setDisable(true);
            lblWarning.setText("");
            return;
        }

        lblSelectedName.setText(employee.hoTen());
        lblSelectedId.setText(employee.maNhanVien());

        boolean hasAccount = employee.tenDangNhap() != null && !employee.tenDangNhap().isEmpty();
        lblSelectedAccount.setText(hasAccount ? employee.tenDangNhap() : "(Chưa có tài khoản)");
        lblSelectedCurrentRole.setText(hasAccount && employee.phanQuyen() != null ? employee.phanQuyen() : "Chưa cấp");

        if (!hasAccount) {
            cboNewRole.setDisable(true);
            btnSaveRole.setDisable(true);
            lblWarning.setText("⚠️ Nhân viên này chưa được cấp tài khoản đăng nhập. Vui lòng sang màn 'Tài khoản đăng nhập' để cấp tài khoản trước khi phân quyền.");
        } else {
            cboNewRole.setDisable(false);
            cboNewRole.setValue(employee.phanQuyen() != null ? employee.phanQuyen() : "Bán hàng");
            btnSaveRole.setDisable(false);
            lblWarning.setText("");
        }
    }

    private void capNhatMoTaVaiTro(String role) {
        if (role == null) return;
        lblRoleDescTitle.setText("QUYỀN HẠN CỦA VAI TRÒ " + role.toUpperCase() + ":");

        switch (role) {
            case "Admin" -> {
                lblRoleDescTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #7e22ce;");
                lblRoleDescContent.setText("• Toàn quyền quản trị tất cả các mô-đun trong hệ thống Happy Tails.\n• Quản lý Nhân sự, Ca trực, Phân quyền, Tài khoản người dùng.\n• Bán hàng, Kho hàng, Khuyến mãi, Chăm sóc và Báo cáo doanh thu.");
            }
            case "Quản lý" -> {
                lblRoleDescTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1d4ed8;");
                lblRoleDescContent.setText("• Quản lý thông tin Nhân viên và Phân công ca trực.\n• Quản lý Nhà cung cấp, Dịch vụ chăm sóc thú cưng và Khuyến mãi.\n• Xem Báo cáo thống kê doanh thu và Báo cáo tồn kho sản phẩm.");
            }
            case "Bán hàng" -> {
                lblRoleDescTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #0284c7;");
                lblRoleDescContent.setText("• Quản lý hồ sơ Khách hàng và Danh mục Sản phẩm.\n• Quản lý Đơn hàng nhập và Lập hóa đơn bán hàng (`INV_CREATE`).\n• Lập Phiếu đổi trả hàng (`RET_CREATE`) và tra cứu hóa đơn.");
            }
            case "Chăm sóc" -> {
                lblRoleDescTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #b45309;");
                lblRoleDescContent.setText("• Quản lý Lịch hẹn chăm sóc thú cưng (`APPOINTMENT`).\n• Quản lý hồ sơ Thú cưng và Tra cứu lịch sử khách hàng.\n• Tra cứu bảng giá và dịch vụ chăm sóc thú cưng (`CARE_SERVICE`).");
            }
        }
    }

    private void luuPhanQuyen() {
        Employee selected = tblRoleEmployees.getSelectionModel().getSelectedItem();
        if (selected == null) {
            hienThiThongBao(Alert.AlertType.WARNING, "Chưa chọn nhân viên", "Vui lòng chọn nhân viên cần phân quyền.");
            return;
        }

        if (selected.tenDangNhap() == null) {
            hienThiThongBao(Alert.AlertType.WARNING, "Chưa có tài khoản", "Nhân viên này chưa có tài khoản đăng nhập.");
            return;
        }

        String newRole = cboNewRole.getValue();
        if (newRole == null) return;

        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null && currentUser.getMaNhanVien().equals(selected.maNhanVien()) && !"Admin".equalsIgnoreCase(newRole)) {
            hienThiThongBao(Alert.AlertType.ERROR, "Thao tác không được phép", "Bạn không thể tự hạ quyền quản trị Admin của chính mình.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác Nhận Cập Nhật Phân Quyền");
        confirmAlert.setHeaderText("Cập nhật phân quyền cho nhân viên " + selected.hoTen());
        confirmAlert.setContentText("Bạn có chắc chắn muốn thay đổi phân quyền của tài khoản [" + selected.tenDangNhap() + "] sang vai trò [" + newRole + "]?");
        confirmAlert.initOwner(getScene().getWindow());

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        new Thread(() -> {
            try {
                employeeController.updateRole(selected.maNhanVien(), newRole);
                Platform.runLater(() -> {
                    hienThiThongBao(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật phân quyền vai trò [" + newRole + "] cho nhân viên " + selected.hoTen());
                    napDuLieuTuCoSoDuLieu();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    hienThiThongBao(Alert.AlertType.ERROR, "Lỗi phân quyền", ex.getMessage());
                });
            }
        }).start();
    }

    private void apDungBoLoc() {
        String keyword = txtSearchRole.getText() != null ? txtSearchRole.getText().trim().toLowerCase() : "";
        String roleFilter = cboRoleFilter.getValue();

        filteredEmployeeList.setPredicate(emp -> {
            if (emp == null) return false;

            // Lọc theo vai trò
            if (roleFilter != null && !roleFilter.equals("Tất cả vai trò")) {
                if (roleFilter.equals("Chưa cấp TK")) {
                    if (emp.tenDangNhap() != null) return false;
                } else {
                    if (!roleFilter.equalsIgnoreCase(emp.phanQuyen())) return false;
                }
            }

            // Lọc theo từ khóa
            if (keyword.isEmpty()) return true;

            boolean matchId = emp.maNhanVien() != null && emp.maNhanVien().toLowerCase().contains(keyword);
            boolean matchName = emp.hoTen() != null && emp.hoTen().toLowerCase().contains(keyword);
            boolean matchUser = emp.tenDangNhap() != null && emp.tenDangNhap().toLowerCase().contains(keyword);

            return matchId || matchName || matchUser;
        });

        lblStatusCount.setText("Hiển thị " + filteredEmployeeList.size() + " / " + masterEmployeeList.size() + " nhân viên");
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
                        tblRoleEmployees.getSelectionModel().selectFirst();
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    lblStatusCount.setText("Lỗi tải: " + ex.getMessage());
                });
            }
        }).start();
    }

    private void hienThiThongBao(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        if (getScene() != null && getScene().getWindow() != null) {
            alert.initOwner(getScene().getWindow());
        }
        alert.showAndWait();
    }

    private Label createFieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: 500;");
        lbl.setPrefWidth(90);
        return lbl;
    }

    private Label createFieldValue(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #1e293b; -fx-font-weight: bold;");
        return lbl;
    }

    private String getRoleBadgeClass(String phanQuyen) {
        if (phanQuyen == null) return "badge-none";
        return switch (phanQuyen) {
            case "Admin" -> "badge-admin";
            case "Quản lý" -> "badge-manager";
            case "Bán hàng" -> "badge-sale";
            case "Chăm sóc" -> "badge-care";
            default -> "badge-none";
        };
    }
}

