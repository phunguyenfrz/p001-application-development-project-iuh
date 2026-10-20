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
import javafx.stage.Modality;
import javafx.util.Duration;

import java.util.List;

/**
 * Màn hình Quản lý Tài khoản đăng nhập (EMP_ACCOUNT - Hạn: 22/10/2026).
 * Cho phép cấp tài khoản mới, khóa / mở khóa tài khoản và đặt lại mật khẩu cho nhân viên.
 *
 * Tiêu chuẩn Definition of Done:
 * - Đặt tên điều khiển đúng tiền tố: txt, cbo, tbl, lbl, btn.
 * - Các nút hiển thị tên đầy đủ không co rút (setMinWidth(Region.USE_PREF_SIZE)).
 * - Tên lớp là danh từ, tên phương thức là động từ, comment đầy đủ.
 */
public class EmployeeAccountView extends VBox {

    private final EmployeeController employeeController = new EmployeeController();
    private final ObservableList<Employee> masterEmployeeList = FXCollections.observableArrayList();
    private final FilteredList<Employee> filteredEmployeeList = new FilteredList<>(masterEmployeeList, e -> true);

    // Điều khiển tuân thủ đúng tiền tố
    private TextField txtSearchAccount;
    private ComboBox<String> cboAccountStatusFilter;
    private Button btnCreateAccount;
    private Button btnToggleStatus;
    private Button btnResetPassword;
    private Button btnRefreshAccounts;

    private TableView<Employee> tblAccounts;
    private Label lblStatusCount;

    public EmployeeAccountView() {
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
        Label lblTitle = new Label("QUẢN LÝ TÀI KHOẢN ĐĂNG NHẬP");
        lblTitle.getStyleClass().add("employee-title");

        Label lblSubtitle = new Label("Cấp tài khoản mới cho nhân viên, quản lý trạng thái khóa/mở khóa và đặt lại mật khẩu truy cập.");
        lblSubtitle.getStyleClass().add("employee-muted");
        lblSubtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        pnlHeader.getChildren().addAll(lblTitle, lblSubtitle);

        // 2. Action Toolbar Card
        VBox pnlActionCard = new VBox(12);
        pnlActionCard.getStyleClass().add("employee-card");
        pnlActionCard.setPadding(new Insets(16));

        HBox pnlToolbarRow = new HBox(12);
        pnlToolbarRow.setAlignment(Pos.CENTER_LEFT);

        txtSearchAccount = new TextField();
        txtSearchAccount.setPromptText("🔍 Tìm theo tên đăng nhập, họ tên hoặc mã NV...");
        txtSearchAccount.setPrefHeight(36);
        HBox.setHgrow(txtSearchAccount, Priority.ALWAYS);

        cboAccountStatusFilter = new ComboBox<>();
        cboAccountStatusFilter.getItems().addAll("Tất cả trạng thái", "Đang hoạt động", "Đang bị khóa", "Chưa cấp tài khoản");
        cboAccountStatusFilter.setValue("Tất cả trạng thái");
        cboAccountStatusFilter.setPrefHeight(36);

        btnCreateAccount = new Button("＋ Cấp tài khoản mới");
        btnCreateAccount.getStyleClass().addAll("employee-add");
        btnCreateAccount.setMinWidth(Region.USE_PREF_SIZE);
        btnCreateAccount.setPrefHeight(36);

        btnToggleStatus = new Button("🔒 Khóa / Mở khóa");
        btnToggleStatus.getStyleClass().addAll("employee-secondary");
        btnToggleStatus.setMinWidth(Region.USE_PREF_SIZE);
        btnToggleStatus.setPrefHeight(36);
        btnToggleStatus.setDisable(true);

        btnResetPassword = new Button("🔑 Đặt lại mật khẩu");
        btnResetPassword.getStyleClass().addAll("employee-secondary");
        btnResetPassword.setMinWidth(Region.USE_PREF_SIZE);
        btnResetPassword.setPrefHeight(36);
        btnResetPassword.setDisable(true);

        btnRefreshAccounts = new Button("Làm mới");
        btnRefreshAccounts.getStyleClass().addAll("employee-secondary");
        btnRefreshAccounts.setMinWidth(Region.USE_PREF_SIZE);
        btnRefreshAccounts.setPrefHeight(36);

        pnlToolbarRow.getChildren().addAll(
                txtSearchAccount, cboAccountStatusFilter,
                btnCreateAccount, btnToggleStatus, btnResetPassword, btnRefreshAccounts
        );

        pnlActionCard.getChildren().add(pnlToolbarRow);

        // 3. Table Card
        VBox pnlTableCard = new VBox(12);
        pnlTableCard.getStyleClass().add("employee-card");
        pnlTableCard.setPadding(new Insets(16));
        VBox.setVgrow(pnlTableCard, Priority.ALWAYS);

        tblAccounts = new TableView<>();
        tblAccounts.setItems(filteredEmployeeList);
        tblAccounts.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblAccounts.setPlaceholder(new Label("Không tìm thấy tài khoản nào."));
        VBox.setVgrow(tblAccounts, Priority.ALWAYS);

        TableColumn<Employee, String> colEmpId = new TableColumn<>("Mã NV");
        colEmpId.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().maNhanVien()));
        colEmpId.setPrefWidth(140);
        colEmpId.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        TableColumn<Employee, String> colEmpName = new TableColumn<>("Họ và tên");
        colEmpName.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().hoTen()));
        colEmpName.setPrefWidth(190);

        TableColumn<Employee, String> colJob = new TableColumn<>("Chức vụ");
        colJob.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().chucVu()));
        colJob.setPrefWidth(110);
        colJob.setStyle("-fx-alignment: CENTER;");

        TableColumn<Employee, String> colUsername = new TableColumn<>("Tên đăng nhập");
        colUsername.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().tenDangNhap() != null ? c.getValue().tenDangNhap() : "(Chưa cấp)"));
        colUsername.setPrefWidth(140);
        colUsername.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        TableColumn<Employee, Employee> colRole = new TableColumn<>("Quyền truy cập");
        colRole.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        colRole.setPrefWidth(130);
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
                    Label lbl = new Label("Không có");
                    lbl.getStyleClass().add("badge-none");
                    setGraphic(lbl);
                }
            }
        });

        TableColumn<Employee, Employee> colStatus = new TableColumn<>("Trạng thái tài khoản");
        colStatus.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        colStatus.setPrefWidth(140);
        colStatus.setStyle("-fx-alignment: CENTER;");
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Employee item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else if (item.tenDangNhap() == null) {
                    Label lbl = new Label("Chưa cấp tài khoản");
                    lbl.getStyleClass().add("badge-none");
                    setGraphic(lbl);
                } else if (Boolean.TRUE.equals(item.taiKhoanHoatDong())) {
                    Label lbl = new Label("Đang hoạt động");
                    lbl.getStyleClass().add("badge-active");
                    setGraphic(lbl);
                } else {
                    Label lbl = new Label("Đang bị khóa");
                    lbl.getStyleClass().add("badge-locked");
                    setGraphic(lbl);
                }
            }
        });

        tblAccounts.getColumns().addAll(colEmpId, colEmpName, colJob, colUsername, colRole, colStatus);

        lblStatusCount = new Label("Đang tải danh sách tài khoản...");
        lblStatusCount.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        pnlTableCard.getChildren().addAll(tblAccounts, lblStatusCount);

        getChildren().addAll(pnlHeader, pnlActionCard, pnlTableCard);
    }

    private void ganSuKien() {
        PauseTransition debounce = new PauseTransition(Duration.millis(250));
        txtSearchAccount.textProperty().addListener((obs, oldV, newV) -> {
            debounce.setOnFinished(e -> apDungBoLoc());
            debounce.playFromStart();
        });

        cboAccountStatusFilter.valueProperty().addListener((obs, oldV, newV) -> apDungBoLoc());

        // Lắng nghe chọn dòng để cập nhật trạng thái nút
        tblAccounts.getSelectionModel().selectedItemProperty().addListener((obs, oldV, selected) -> {
            boolean hasAccount = selected != null && selected.tenDangNhap() != null && !selected.tenDangNhap().isEmpty();
            btnToggleStatus.setDisable(!hasAccount);
            btnResetPassword.setDisable(!hasAccount);

            if (hasAccount) {
                if (Boolean.TRUE.equals(selected.taiKhoanHoatDong())) {
                    btnToggleStatus.setText("🔒 Khóa tài khoản");
                } else {
                    btnToggleStatus.setText("🔓 Mở khóa tài khoản");
                }
            } else {
                btnToggleStatus.setText("🔒 Khóa / Mở khóa");
            }
        });

        btnCreateAccount.setOnAction(e -> hienThiHopThoaiCapTaiKhoan());
        btnToggleStatus.setOnAction(e -> xuLyKhoaMoKhoaTaiKhoan());
        btnResetPassword.setOnAction(e -> xuLyDatLaiMatKhau());
        btnRefreshAccounts.setOnAction(e -> napDuLieuTuCoSoDuLieu());
    }

    private void hienThiHopThoaiCapTaiKhoan() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Cấp Tài Khoản Mới Cho Nhân Viên");
        dialog.setHeaderText(null);
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (getScene() != null && getScene().getWindow() != null) {
            dialog.initOwner(getScene().getWindow());
        }

        DialogPane pnlPane = dialog.getDialogPane();
        pnlPane.getStylesheets().add(getClass().getResource("/com/petshop/view/styles.css").toExternalForm());
        pnlPane.setPrefWidth(480);

        VBox pnlContent = new VBox(12);
        pnlContent.setPadding(new Insets(16));

        Label lblDlgTitle = new Label("CẤP TÀI KHOẢN ĐĂNG NHẬP MỚI");
        lblDlgTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // Lọc danh sách nhân viên đang làm mà chưa có tài khoản
        List<Employee> unassignedEmployees = masterEmployeeList.stream()
                .filter(e -> "Đang làm".equals(e.trangThai()) && (e.tenDangNhap() == null || e.tenDangNhap().isEmpty()))
                .toList();

        if (unassignedEmployees.isEmpty()) {
            pnlContent.getChildren().addAll(
                    lblDlgTitle,
                    new Label("Tất cả nhân viên đang làm việc đều đã được cấp tài khoản đăng nhập."),
                    new Separator()
            );
            pnlPane.setContent(pnlContent);
            pnlPane.getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();
            return;
        }

        ComboBox<Employee> cboSelectEmployee = new ComboBox<>(FXCollections.observableArrayList(unassignedEmployees));
        cboSelectEmployee.setPromptText("Chọn nhân viên cần cấp tài khoản...");
        cboSelectEmployee.setMaxWidth(Double.MAX_VALUE);
        cboSelectEmployee.setPrefHeight(36);
        cboSelectEmployee.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Employee item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.hoTen() + " (" + item.maNhanVien() + " - " + item.chucVu() + ")");
            }
        });
        cboSelectEmployee.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Employee item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.hoTen() + " (" + item.maNhanVien() + " - " + item.chucVu() + ")");
            }
        });

        TextField txtNewUsername = new TextField();
        txtNewUsername.setPromptText("Nhập tên đăng nhập (ví dụ: luan.bm)...");
        txtNewUsername.setPrefHeight(36);

        PasswordField txtNewPassword = new PasswordField();
        txtNewPassword.setText("123456"); // Mật khẩu mặc định tiện ích
        txtNewPassword.setPrefHeight(36);

        ComboBox<String> cboRole = new ComboBox<>();
        cboRole.getItems().addAll("Admin", "Quản lý", "Bán hàng", "Chăm sóc");
        cboRole.setValue("Bán hàng");
        cboRole.setMaxWidth(Double.MAX_VALUE);
        cboRole.setPrefHeight(36);

        // Tự động gợi ý username và vai trò khi chọn nhân viên
        cboSelectEmployee.valueProperty().addListener((obs, oldV, emp) -> {
            if (emp != null) {
                String rawName = emp.hoTen().toLowerCase().trim();
                String[] parts = rawName.split("\\s+");
                String suggested = parts[parts.length - 1];
                if (parts.length > 1) {
                    suggested += "." + parts[0];
                }
                txtNewUsername.setText(suggested);

                if ("Quản lý".equalsIgnoreCase(emp.chucVu())) {
                    cboRole.setValue("Quản lý");
                } else if ("Chăm sóc".equalsIgnoreCase(emp.chucVu())) {
                    cboRole.setValue("Chăm sóc");
                } else {
                    cboRole.setValue("Bán hàng");
                }
            }
        });

        cboSelectEmployee.getSelectionModel().selectFirst();

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #b91c1c; -fx-font-size: 12px;");

        GridPane gridForm = new GridPane();
        gridForm.setHgap(10);
        gridForm.setVgap(12);

        gridForm.add(new Label("Nhân viên:"), 0, 0);
        gridForm.add(cboSelectEmployee, 1, 0);

        gridForm.add(new Label("Tên đăng nhập:"), 0, 1);
        gridForm.add(txtNewUsername, 1, 1);

        gridForm.add(new Label("Mật khẩu ban đầu:"), 0, 2);
        gridForm.add(txtNewPassword, 1, 2);

        gridForm.add(new Label("Vai trò phân quyền:"), 0, 3);
        gridForm.add(cboRole, 1, 3);

        pnlContent.getChildren().addAll(lblDlgTitle, new Separator(), gridForm, lblError);
        pnlPane.setContent(pnlContent);

        ButtonType btnTypeCreate = new ButtonType("Cấp tài khoản", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnTypeCancel = new ButtonType("Hủy bỏ", ButtonBar.ButtonData.CANCEL_CLOSE);
        pnlPane.getButtonTypes().addAll(btnTypeCreate, btnTypeCancel);

        Button btnDoCreate = (Button) pnlPane.lookupButton(btnTypeCreate);
        btnDoCreate.getStyleClass().addAll("employee-add");
        btnDoCreate.setMinWidth(Region.USE_PREF_SIZE);

        btnDoCreate.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            Employee selectedEmp = cboSelectEmployee.getValue();
            String username = txtNewUsername.getText() != null ? txtNewUsername.getText().trim() : "";
            String password = txtNewPassword.getText() != null ? txtNewPassword.getText() : "";
            String role = cboRole.getValue();

            if (selectedEmp == null) {
                lblError.setText("Vui lòng chọn nhân viên cần cấp tài khoản.");
                event.consume();
                return;
            }
            if (username.isEmpty() || !username.matches("^[a-zA-Z0-9_.]{3,50}$")) {
                lblError.setText("Tên đăng nhập từ 3 đến 50 ký tự, không chứa dấu cách hoặc ký tự đặc biệt.");
                event.consume();
                return;
            }
            if (password.length() < 6) {
                lblError.setText("Mật khẩu ban đầu phải có ít nhất 6 ký tự.");
                event.consume();
                return;
            }

            try {
                employeeController.createAccount(selectedEmp.maNhanVien(), username, password, role);
                hienThiThongBao(Alert.AlertType.INFORMATION, "Thành công",
                        "Đã cấp tài khoản [" + username + "] thành công cho nhân viên " + selectedEmp.hoTen() + "!");
                napDuLieuTuCoSoDuLieu();
            } catch (Exception ex) {
                lblError.setText("Lỗi cấp tài khoản: " + ex.getMessage());
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    private void xuLyKhoaMoKhoaTaiKhoan() {
        Employee selected = tblAccounts.getSelectionModel().getSelectedItem();
        if (selected == null || selected.tenDangNhap() == null) {
            hienThiThongBao(Alert.AlertType.WARNING, "Chưa chọn tài khoản", "Vui lòng chọn một tài khoản từ danh sách.");
            return;
        }

        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null && currentUser.getMaNhanVien().equals(selected.maNhanVien())) {
            hienThiThongBao(Alert.AlertType.ERROR, "Thao tác không được phép", "Bạn không thể tự khóa tài khoản của chính mình.");
            return;
        }

        boolean isCurrentlyActive = Boolean.TRUE.equals(selected.taiKhoanHoatDong());
        String actionName = isCurrentlyActive ? "KHÓA" : "MỞ KHÓA";

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác Nhận " + actionName + " Tài Khoản");
        confirmAlert.setHeaderText(actionName + " tài khoản [" + selected.tenDangNhap() + "]");
        confirmAlert.setContentText("Bạn có chắc chắn muốn " + actionName.toLowerCase() + " tài khoản của nhân viên " + selected.hoTen() + "?");
        if (getScene() != null && getScene().getWindow() != null) {
            confirmAlert.initOwner(getScene().getWindow());
        }

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        new Thread(() -> {
            try {
                employeeController.toggleAccountStatus(selected.maNhanVien(), !isCurrentlyActive);
                Platform.runLater(() -> {
                    hienThiThongBao(Alert.AlertType.INFORMATION, "Thành công",
                            "Đã " + actionName.toLowerCase() + " tài khoản [" + selected.tenDangNhap() + "] thành công.");
                    napDuLieuTuCoSoDuLieu();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    hienThiThongBao(Alert.AlertType.ERROR, "Lỗi thực hiện", ex.getMessage());
                });
            }
        }).start();
    }

    private void xuLyDatLaiMatKhau() {
        Employee selected = tblAccounts.getSelectionModel().getSelectedItem();
        if (selected == null || selected.tenDangNhap() == null) {
            hienThiThongBao(Alert.AlertType.WARNING, "Chưa chọn tài khoản", "Vui lòng chọn một tài khoản cần đặt lại mật khẩu.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Đặt Lại Mật Khẩu");
        dialog.setHeaderText(null);
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (getScene() != null && getScene().getWindow() != null) {
            dialog.initOwner(getScene().getWindow());
        }

        DialogPane pnlPane = dialog.getDialogPane();
        pnlPane.getStylesheets().add(getClass().getResource("/com/petshop/view/styles.css").toExternalForm());
        pnlPane.setPrefWidth(420);

        VBox pnlContent = new VBox(12);
        pnlContent.setPadding(new Insets(16));

        Label lblDlgTitle = new Label("ĐẶT LẠI MẬT KHẨU TÀI KHOẢN");
        lblDlgTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label lblTargetUser = new Label("Tài khoản: " + selected.tenDangNhap() + " (" + selected.hoTen() + ")");
        lblTargetUser.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #475569; -fx-font-weight: bold;");

        PasswordField txtNewPass = new PasswordField();
        txtNewPass.setPromptText("Nhập mật khẩu mới (ít nhất 6 ký tự)...");
        txtNewPass.setPrefHeight(36);

        PasswordField txtConfirmPass = new PasswordField();
        txtConfirmPass.setPromptText("Xác nhận lại mật khẩu mới...");
        txtConfirmPass.setPrefHeight(36);

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #b91c1c; -fx-font-size: 12px;");

        GridPane gridForm = new GridPane();
        gridForm.setHgap(10);
        gridForm.setVgap(12);

        gridForm.add(new Label("Mật khẩu mới:"), 0, 0);
        gridForm.add(txtNewPass, 1, 0);

        gridForm.add(new Label("Xác nhận MK:"), 0, 1);
        gridForm.add(txtConfirmPass, 1, 1);

        pnlContent.getChildren().addAll(lblDlgTitle, lblTargetUser, new Separator(), gridForm, lblError);
        pnlPane.setContent(pnlContent);

        ButtonType btnTypeConfirm = new ButtonType("Xác nhận", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnTypeCancel = new ButtonType("Hủy bỏ", ButtonBar.ButtonData.CANCEL_CLOSE);
        pnlPane.getButtonTypes().addAll(btnTypeConfirm, btnTypeCancel);

        Button btnDoReset = (Button) pnlPane.lookupButton(btnTypeConfirm);
        btnDoReset.getStyleClass().addAll("employee-add");
        btnDoReset.setMinWidth(Region.USE_PREF_SIZE);

        btnDoReset.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String p1 = txtNewPass.getText();
            String p2 = txtConfirmPass.getText();

            if (p1 == null || p1.length() < 6) {
                lblError.setText("Mật khẩu mới phải có ít nhất 6 ký tự.");
                event.consume();
                return;
            }
            if (!p1.equals(p2)) {
                lblError.setText("Mật khẩu xác nhận không trùng khớp.");
                event.consume();
                return;
            }

            try {
                employeeController.resetPassword(selected.maNhanVien(), p1);
                hienThiThongBao(Alert.AlertType.INFORMATION, "Thành công",
                        "Đã đặt lại mật khẩu cho tài khoản [" + selected.tenDangNhap() + "] thành công.");
            } catch (Exception ex) {
                lblError.setText("Lỗi đặt lại mật khẩu: " + ex.getMessage());
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    private void apDungBoLoc() {
        String keyword = txtSearchAccount.getText() != null ? txtSearchAccount.getText().trim().toLowerCase() : "";
        String statusFilter = cboAccountStatusFilter.getValue();

        filteredEmployeeList.setPredicate(emp -> {
            if (emp == null) return false;

            // 1. Lọc theo trạng thái tài khoản
            if (statusFilter != null && !statusFilter.equals("Tất cả trạng thái")) {
                boolean hasAccount = emp.tenDangNhap() != null && !emp.tenDangNhap().isEmpty();
                if (statusFilter.equals("Chưa cấp tài khoản")) {
                    if (hasAccount) return false;
                } else if (statusFilter.equals("Đang hoạt động")) {
                    if (!hasAccount || !Boolean.TRUE.equals(emp.taiKhoanHoatDong())) return false;
                } else if (statusFilter.equals("Đang bị khóa")) {
                    if (!hasAccount || Boolean.TRUE.equals(emp.taiKhoanHoatDong())) return false;
                }
            }

            // 2. Lọc theo từ khóa
            if (keyword.isEmpty()) return true;

            boolean matchId = emp.maNhanVien() != null && emp.maNhanVien().toLowerCase().contains(keyword);
            boolean matchName = emp.hoTen() != null && emp.hoTen().toLowerCase().contains(keyword);
            boolean matchUser = emp.tenDangNhap() != null && emp.tenDangNhap().toLowerCase().contains(keyword);

            return matchId || matchName || matchUser;
        });

        lblStatusCount.setText("Hiển thị " + filteredEmployeeList.size() + " / " + masterEmployeeList.size() + " tài khoản nhân viên");
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
                        tblAccounts.getSelectionModel().selectFirst();
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

