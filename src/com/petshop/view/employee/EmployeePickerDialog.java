package com.petshop.view.employee;

import com.petshop.controller.EmployeeController;
import com.petshop.model.Employee;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.List;
import java.util.Optional;

/**
 * Hộp thoại Chọn nhân viên (EmployeePickerDialog).
 * Bàn giao phụ thuộc cho Trần Lê Tuấn Anh (dùng trong màn Phân công ca trực - Hạn 17/10/2026).
 *
 * Tiêu chuẩn Definition of Done:
 * - Đặt tên điều khiển đúng tiền tố: txt, cbo, tbl, lbl, btn.
 * - Các nút hiển thị tên đầy đủ không co rút (setMinWidth(Region.USE_PREF_SIZE)).
 * - Tên lớp là danh từ, tên phương thức là động từ, chú thích đầy đủ.
 */
public class EmployeePickerDialog extends Dialog<Employee> {

    private final EmployeeController employeeController = new EmployeeController();
    private final ObservableList<Employee> masterEmployeeList = FXCollections.observableArrayList();
    private final FilteredList<Employee> filteredEmployeeList = new FilteredList<>(masterEmployeeList, e -> true);

    // Điều khiển tuân thủ đúng tiền tố
    private TextField txtSearch;
    private ComboBox<String> cboChucVu;
    private TableView<Employee> tblEmployees;
    private Label lblStatusCount;
    private Button btnSelect;
    private Button btnCancel;

    public EmployeePickerDialog(Window owner) {
        setTitle("Chọn Nhân Viên - Happy Tails");
        setHeaderText(null);
        initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            initOwner(owner);
        }

        DialogPane pnlDialogPane = getDialogPane();
        pnlDialogPane.getStylesheets().add(
                getClass().getResource("/com/petshop/view/styles.css").toExternalForm()
        );
        pnlDialogPane.setPrefSize(720, 520);
        pnlDialogPane.setStyle("-fx-background-color: #f7f9f6;");

        pnlDialogPane.setContent(buildContent());
        configureButtons(pnlDialogPane);
        loadEmployees();

        Platform.runLater(() -> {
            if (txtSearch != null) {
                txtSearch.requestFocus();
            }
        });
    }

    /**
     * Phương thức tiện ích tĩnh mở hộp thoại chọn nhân viên.
     *
     * @param owner Cửa sổ cha
     * @return Optional chứa nhân viên được chọn hoặc rỗng nếu người dùng hủy
     */
    public static Optional<Employee> showAndPick(Window owner) {
        EmployeePickerDialog dlg = new EmployeePickerDialog(owner);
        return dlg.showAndWait();
    }

    /**
     * Tên alias tương thích cho Tuấn Anh gọi trực tiếp.
     */
    public static Optional<Employee> showAndWait(Window owner) {
        return showAndPick(owner);
    }

    private VBox buildContent() {
        VBox pnlContainer = new VBox(12);
        pnlContainer.setPadding(new Insets(16, 20, 10, 20));

        // 1. Tiêu đề
        Label lblTitle = new Label("DANH SÁCH NHÂN VIÊN");
        lblTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");

        Label lblSubTitle = new Label("Tìm kiếm và chọn nhân viên đang làm việc để gán ca trực hoặc phân công nhiệm vụ.");
        lblSubTitle.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #6e7c70;");

        // 2. Thanh tìm kiếm và bộ lọc chức vụ
        HBox pnlFilterBar = new HBox(12);
        pnlFilterBar.setAlignment(Pos.CENTER_LEFT);

        txtSearch = new TextField();
        txtSearch.setPromptText("🔍 Nhập mã nhân viên, họ tên hoặc số điện thoại...");
        txtSearch.setPrefHeight(36);
        HBox.setHgrow(txtSearch, Priority.ALWAYS);

        // Debounce 250ms cho việc tìm kiếm mượt mà
        PauseTransition debounce = new PauseTransition(Duration.millis(250));
        txtSearch.textProperty().addListener((obs, oldV, newV) -> {
            debounce.setOnFinished(e -> applyFilter());
            debounce.playFromStart();
        });

        cboChucVu = new ComboBox<>();
        cboChucVu.getItems().addAll("Tất cả chức vụ", "Quản lý", "Bán hàng", "Chăm sóc");
        cboChucVu.setValue("Tất cả chức vụ");
        cboChucVu.setPrefHeight(36);
        cboChucVu.valueProperty().addListener((obs, oldV, newV) -> applyFilter());

        pnlFilterBar.getChildren().addAll(txtSearch, cboChucVu);

        // 3. Bảng danh sách nhân viên
        tblEmployees = new TableView<>();
        tblEmployees.setItems(filteredEmployeeList);
        tblEmployees.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblEmployees.setPlaceholder(new Label("Không tìm thấy nhân viên nào phù hợp."));
        VBox.setVgrow(tblEmployees, Priority.ALWAYS);

        TableColumn<Employee, String> colId = new TableColumn<>("Mã NV");
        colId.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().maNhanVien()));
        colId.setPrefWidth(140);
        colId.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        TableColumn<Employee, String> colName = new TableColumn<>("Họ và tên");
        colName.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().hoTen()));
        colName.setPrefWidth(180);

        TableColumn<Employee, String> colRole = new TableColumn<>("Chức vụ");
        colRole.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().chucVu()));
        colRole.setPrefWidth(110);
        colRole.setStyle("-fx-alignment: CENTER;");

        TableColumn<Employee, String> colPhone = new TableColumn<>("Số điện thoại");
        colPhone.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().soDienThoai()));
        colPhone.setPrefWidth(110);
        colPhone.setStyle("-fx-alignment: CENTER;");

        TableColumn<Employee, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().email() != null ? c.getValue().email() : ""));
        colEmail.setPrefWidth(150);

        TableColumn<Employee, String> colStatus = new TableColumn<>("Trạng thái");
        colStatus.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().trangThai()));
        colStatus.setPrefWidth(100);
        colStatus.setStyle("-fx-alignment: CENTER;");

        tblEmployees.getColumns().addAll(colId, colName, colRole, colPhone, colEmail, colStatus);

        // Nháy đúp chuột để chọn ngay
        tblEmployees.setRowFactory(tv -> {
            TableRow<Employee> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    confirmSelection();
                }
            });
            return row;
        });

        // 4. Thanh đếm trạng thái dưới chân bảng
        lblStatusCount = new Label("Đang tải danh sách...");
        lblStatusCount.setStyle("-fx-font-size: 12px; -fx-text-fill: #6e7c70;");

        pnlContainer.getChildren().addAll(lblTitle, lblSubTitle, pnlFilterBar, tblEmployees, lblStatusCount);
        return pnlContainer;
    }

    private void configureButtons(DialogPane pnlDialogPane) {
        ButtonType btnTypeSelect = new ButtonType("Chọn nhân viên", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnTypeCancel = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);

        pnlDialogPane.getButtonTypes().addAll(btnTypeSelect, btnTypeCancel);

        btnSelect = (Button) pnlDialogPane.lookupButton(btnTypeSelect);
        btnCancel = (Button) pnlDialogPane.lookupButton(btnTypeCancel);

        btnSelect.getStyleClass().addAll("customer-add");
        btnCancel.getStyleClass().addAll("customer-secondary");

        btnSelect.setMinWidth(Region.USE_PREF_SIZE);
        btnCancel.setMinWidth(Region.USE_PREF_SIZE);

        btnSelect.setDisable(true);
        tblEmployees.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            btnSelect.setDisable(newV == null);
        });

        setResultConverter(buttonType -> {
            if (buttonType == btnTypeSelect) {
                return tblEmployees.getSelectionModel().getSelectedItem();
            }
            return null;
        });
    }

    private void confirmSelection() {
        Employee selected = tblEmployees.getSelectionModel().getSelectedItem();
        if (selected != null) {
            setResult(selected);
            close();
        }
    }

    private void applyFilter() {
        String keyword = txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";
        String selectedRole = cboChucVu.getValue();

        filteredEmployeeList.setPredicate(emp -> {
            if (emp == null) return false;

            // Lọc theo chức vụ
            if (selectedRole != null && !selectedRole.equals("Tất cả chức vụ")) {
                if (!selectedRole.equalsIgnoreCase(emp.chucVu())) {
                    return false;
                }
            }

            // Lọc theo từ khóa
            if (keyword.isEmpty()) return true;

            boolean matchId = emp.maNhanVien() != null && emp.maNhanVien().toLowerCase().contains(keyword);
            boolean matchName = emp.hoTen() != null && emp.hoTen().toLowerCase().contains(keyword);
            boolean matchPhone = emp.soDienThoai() != null && emp.soDienThoai().contains(keyword);
            boolean matchEmail = emp.email() != null && emp.email().toLowerCase().contains(keyword);

            return matchId || matchName || matchPhone || matchEmail;
        });

        lblStatusCount.setText("Tìm thấy " + filteredEmployeeList.size() + " / " + masterEmployeeList.size() + " nhân viên đang làm việc");
    }

    private void loadEmployees() {
        new Thread(() -> {
            try {
                List<Employee> list = employeeController.loadActiveEmployees();
                Platform.runLater(() -> {
                    masterEmployeeList.setAll(list);
                    applyFilter();
                    if (!filteredEmployeeList.isEmpty()) {
                        tblEmployees.getSelectionModel().selectFirst();
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    lblStatusCount.setText("Lỗi nạp dữ liệu: " + ex.getMessage());
                });
            }
        }).start();
    }
}

