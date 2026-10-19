package com.petshop.view.employee;

import com.petshop.controller.EmployeeController;
import com.petshop.model.Employee;
import com.petshop.model.User;
import com.petshop.util.SessionManager;
import com.petshop.view.ModuleListView;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.*;
import javafx.scene.effect.Effect;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;

import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Giao diện cập nhật nhân viên.
 *
 * Chỉ quản lý hồ sơ nhân viên.
 * Cấp tài khoản và phân quyền được thực hiện ở màn riêng.
 */
public class EmployeeView extends VBox {

    // Controller xử lý nghiệp vụ nhân viên.
    private final EmployeeController employee_controller =
            new EmployeeController();

    // Trạng thái đang tải hoặc lưu dữ liệu.
    private final SimpleBooleanProperty busy =
            new SimpleBooleanProperty(false);

    // Bảng danh sách nhân viên.
    private final TableView<Employee> tblEmployees =
            new TableView<>();

    // Các nhãn thông tin.
    private final Label lblCount = new Label();
    private final Label lblSelected = new Label();

    // Các nút thao tác trên danh sách.
    private final Button btnAdd =
            createButton("＋ Thêm nhân viên", "employee-add");

    private final Button btnEdit =
            createButton("Sửa nhân viên", "employee-secondary");

    private final Button btnDeactivate =
            createButton("Ngừng làm việc", "employee-danger");

    private final Button btnRefresh =
            createButton("Làm mới", "employee-secondary");

    // Định dạng ngày trên form.
    private final DateTimeFormatter date_format =
            DateTimeFormatter.ofPattern("dd/MM/uuuu");

    /**
     * Khởi tạo giao diện theo chức năng được chọn.
     *
     * @param action_id Mã chức năng trong MenuRegistry.
     */
    public EmployeeView(String action_id) {
        // Các chức năng khác tạm giữ màn mẫu.
        if (!"EMP_UPDATE".equals(action_id)) {
            String action_name = switch (action_id) {
                case "EMP_SEARCH" -> "Tìm kiếm nhân viên";
                case "EMP_ROLES" -> "Phân quyền";
                case "EMP_ACCOUNT" -> "Tài khoản đăng nhập";
                default -> "Nhân viên";
            };

            ModuleListView placeholder = new ModuleListView(
                    "Nhân viên",
                    action_name,
                    null
            );

            getChildren().add(placeholder);
            VBox.setVgrow(placeholder, Priority.ALWAYS);
            return;
        }

        getStyleClass().add("employee-page");
        setPadding(new Insets(24));
        setSpacing(20);

        Label lblTitle = new Label("Cập nhật nhân viên");
        lblTitle.getStyleClass().add("employee-title");

        Region header_space = new Region();
        HBox.setHgrow(header_space, Priority.ALWAYS);

        HBox header = new HBox(
                10,
                lblTitle,
                header_space,
                btnAdd,
                btnEdit,
                btnDeactivate
        );
        header.setAlignment(Pos.CENTER_LEFT);

        configureTable();

        lblCount.getStyleClass().add("employee-section-title");
        lblSelected.getStyleClass().add("employee-muted");

        Region count_space = new Region();
        HBox.setHgrow(count_space, Priority.ALWAYS);

        HBox count_row = new HBox(
                10,
                lblCount,
                count_space,
                lblSelected,
                btnRefresh
        );
        count_row.setAlignment(Pos.CENTER_LEFT);

        Label lblHint = createWrappedLabel(
                "Chỉ sửa nhân viên đang làm; không sửa chính mình "
                + "hoặc Admin. Chức vụ không tự thay đổi quyền đăng nhập."
        );
        lblHint.getStyleClass().add("employee-muted");

        VBox table_card = new VBox(
                12,
                count_row,
                tblEmployees,
                new Separator(),
                lblHint
        );
        table_card.getStyleClass().add("employee-card");
        table_card.setPadding(new Insets(16));

        VBox.setVgrow(table_card, Priority.ALWAYS);
        VBox.setVgrow(tblEmployees, Priority.ALWAYS);

        getChildren().addAll(header, table_card);

        // Khóa điều khiển khi đang xử lý dữ liệu.
        btnAdd.disableProperty().bind(busy);
        btnRefresh.disableProperty().bind(busy);
        tblEmployees.disableProperty().bind(busy);

        busy.addListener(
                (observable, previous, current) -> updateSelection()
        );

        tblEmployees.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, previous, current) ->
                                updateSelection()
                );

        btnAdd.setOnAction(event -> showForm(null));

        btnEdit.setOnAction(event -> {
            Employee selected = tblEmployees
                    .getSelectionModel()
                    .getSelectedItem();

            if (!employee_controller.canEdit(selected)) {
                return;
            }

            // Danh sách không tải ảnh; đọc hồ sơ đầy đủ trước khi sửa.
            executeBackground(
                    () -> employee_controller.loadEmployee(
                            selected.maNhanVien()
                    ),
                    employee -> {
                        if (employee_controller.canEdit(employee)) {
                            showForm(employee);
                        } else {
                            showError(new IllegalStateException(
                                    "Hồ sơ đã thay đổi. "
                                    + "Hãy làm mới danh sách."
                            ));
                        }
                    },
                    this::showError
            );
        });

        btnDeactivate.setOnAction(event -> showDeactivate());
        btnRefresh.setOnAction(event -> loadEmployees());

        // F5 chỉ được xử lý khi đang tương tác trong trang này.
        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F5 && !busy.get()) {
                loadEmployees();
                event.consume();
            }
        });

        loadEmployees();
    }

    /**
     * Thiết lập các cột của bảng nhân viên.
     */
    private void configureTable() {
        tblEmployees.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );
        tblEmployees.setFixedCellSize(-1);

        tblEmployees.getColumns().add(createColumn(
                "Mã nhân viên",
                Employee::maNhanVien,
                185,
                false
        ));

        // Truyền cả hồ sơ vào ô để ảnh luôn đi cùng đúng nhân viên khi cuộn/sắp xếp.
        TableColumn<Employee, Employee> name_column = new TableColumn<>("Họ tên");
        name_column.setPrefWidth(220);
        name_column.setMinWidth(100);
        name_column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        name_column.setComparator(java.util.Comparator.comparing(
                Employee::hoTen, String.CASE_INSENSITIVE_ORDER));
        name_column.setCellFactory(ignored -> new TableCell<>() {
            @Override
            protected void updateItem(Employee employee, boolean empty) {
                super.updateItem(employee, empty);
                setText(null);
                setGraphic(null);
                if (empty || employee == null) return;

                Label lblInitials = new Label(createInitials(employee.hoTen()));
                lblInitials.getStyleClass().add("employee-avatar");
                Node avatar = lblInitials;
                byte[] photo = employee.anh();
                if (photo != null && photo.length > 0) {
                    try {
                        Image image = createPhotoPreview(photo);
                        ImageView imgAvatar = new ImageView(image);
                        // Cắt vuông ở giữa rồi bo tròn, không kéo méo ảnh.
                        double side = Math.min(image.getWidth(), image.getHeight());
                        imgAvatar.setViewport(new javafx.geometry.Rectangle2D(
                                (image.getWidth() - side) / 2,
                                (image.getHeight() - side) / 2, side, side));
                        imgAvatar.setFitWidth(30);
                        imgAvatar.setFitHeight(30);
                        imgAvatar.setPreserveRatio(false);
                        imgAvatar.setSmooth(true);
                        imgAvatar.setClip(new javafx.scene.shape.Circle(15, 15, 15));
                        avatar = imgAvatar;
                    } catch (IllegalArgumentException error) {
                        // Ảnh cũ bị lỗi vẫn cho xem tên và dùng avatar chữ cái.
                        avatar = lblInitials;
                    }
                }

                Label lblName = createWrappedLabel(employee.hoTen());
                lblName.prefWidthProperty().bind(widthProperty().subtract(54));
                HBox content = new HBox(8, avatar, lblName);
                content.setAlignment(Pos.CENTER_LEFT);
                setGraphic(content);
            }
        });
        tblEmployees.getColumns().add(name_column);

        tblEmployees.getColumns().add(createColumn(
                "Số điện thoại",
                Employee::soDienThoai,
                135,
                false
        ));

        tblEmployees.getColumns().add(createColumn(
                "Tài khoản đăng nhập",
                this::accountText,
                300,
                false
        ));

        tblEmployees.getColumns().add(createColumn(
                "Chức vụ",
                Employee::chucVu,
                115,
                true
        ));

        tblEmployees.getColumns().add(createColumn(
                "Trạng thái",
                Employee::trangThai,
                125,
                true
        ));
    }

    /**
     * Tải danh sách qua Controller ở luồng nền.
     */
    private void loadEmployees() {
        if (busy.get()) {
            return;
        }

        lblCount.setText("Đang tải nhân viên...");
        tblEmployees.setPlaceholder(
                new Label("Đang tải dữ liệu...")
        );

        executeBackground(
                employee_controller::loadEmployees,
                employees -> {
                    tblEmployees.getSelectionModel().clearSelection();

                    tblEmployees.setItems(
                            FXCollections.observableArrayList(employees)
                    );

                    tblEmployees.setPlaceholder(
                            new Label("Chưa có nhân viên")
                    );

                    lblCount.setText(
                            "Danh sách nhân viên · " + employees.size()
                    );

                    updateSelection();
                },
                error -> {
                    lblCount.setText(
                            "Không tải được danh sách. "
                            + "Bấm Làm mới để thử lại."
                    );

                    tblEmployees.setPlaceholder(
                            new Label("Không tải được dữ liệu")
                    );

                    showError(error);
                }
        );
    }

    /**
     * Cập nhật nút thao tác theo nhân viên đang chọn.
     */
    private void updateSelection() {
        Employee selected = tblEmployees
                .getSelectionModel()
                .getSelectedItem();

        boolean editable = !busy.get()
                && employee_controller.canEdit(selected);

        btnEdit.setDisable(!editable);
        btnDeactivate.setDisable(!editable);

        lblSelected.setText(
                selected == null
                        ? "Chưa chọn nhân viên"
                        : "Đã chọn " + selected.hoTen()
        );
    }

    /**
     * Mở form thêm hoặc sửa nhân viên.
     *
     * @param employee null nếu thêm mới; hồ sơ đầy đủ nếu sửa.
     */
    private void showForm(Employee employee) {
        boolean editing = employee != null;

        Dialog<Void> dialog = createDialog(
                editing ? "Sửa nhân viên" : "Thêm nhân viên",
                680
        );

        TextField txtName = new TextField(
                editing ? employee.hoTen() : ""
        );

        TextField txtPhone = new TextField(
                editing ? employee.soDienThoai() : ""
        );

        TextField txtEmail = new TextField(
                editing ? emptyText(employee.email()) : ""
        );

        TextField txtAddress = new TextField(
                editing ? emptyText(employee.diaChi()) : ""
        );

        txtName.setPromptText("Nhập họ tên...");
        txtPhone.setPromptText("Nhập số điện thoại...");
        txtEmail.setPromptText("Nhập email...");
        txtAddress.setPromptText("Nhập địa chỉ...");

        ComboBox<String> cboJob = new ComboBox<>(
                FXCollections.observableArrayList(
                        "Quản lý",
                        "Bán hàng",
                        "Chăm sóc"
                )
        );
        cboJob.setPromptText("Chọn chức vụ...");
        cboJob.setValue(editing ? employee.chucVu() : null);

        ComboBox<String> cboGender = new ComboBox<>(
                FXCollections.observableArrayList("Nam", "Nữ")
        );
        cboGender.setPromptText("Chọn giới tính...");
        cboGender.setValue(
                editing ? (employee.nam() ? "Nam" : "Nữ") : null
        );

        ComboBox<String> cboStatus = new ComboBox<>(
                FXCollections.observableArrayList(
                        "Đang làm",
                        "Đã nghỉ việc"
                )
        );
        cboStatus.setValue(
                editing ? employee.trangThai() : "Đang làm"
        );

        DatePicker dtpBirth = createDatePicker(
                editing ? employee.ngaySinh() : null
        );

        DatePicker dtpStart = createDatePicker(
                editing
                        ? employee.ngayBatDau()
                        : LocalDate.now(
                                ZoneId.of("Asia/Ho_Chi_Minh")
                        )
        );

        // Mảng ngoài giữ tham chiếu để cập nhật trong các event handler.
        byte[][] selected_photo = {
                editing ? employee.anh() : null
        };

        // Ảnh xem trước khi thêm/sửa; thay đổi trên form chưa được lưu vào database.
        ImageView imgPhoto = new ImageView();
        imgPhoto.setFitWidth(120);
        imgPhoto.setFitHeight(90);
        imgPhoto.setPreserveRatio(true);
        imgPhoto.setSmooth(true);
        imgPhoto.setVisible(false);
        imgPhoto.setManaged(false);
        Label lblPhoto = createWrappedLabel(
                "Kéo thả hoặc bấm để chọn ảnh PNG/JPEG (tối đa 2 MB)");
        if (selected_photo[0] != null && selected_photo[0].length > 0) {
            try {
                imgPhoto.setImage(createPhotoPreview(selected_photo[0]));
                imgPhoto.setVisible(true);
                imgPhoto.setManaged(true);
                lblPhoto.setText("Bấm hoặc kéo thả để thay ảnh");
            } catch (IllegalArgumentException error) {
                lblPhoto.setText("Không đọc được ảnh cũ. Bấm để chọn lại.");
            }
        }
        VBox photo_content = new VBox(8, imgPhoto, lblPhoto);
        photo_content.setAlignment(Pos.CENTER);
        StackPane upload_box = new StackPane(photo_content);
        upload_box.getStyleClass().add("employee-upload");
        Label lblError = createWrappedLabel("");
        lblError.getStyleClass().add("employee-error");
        setError(lblError, "");

        // Đọc file ảnh; Service kiểm tra lại định dạng và kích thước.
        Consumer<File> choose_photo = file -> {
            try {
                if (Files.size(file.toPath()) > 2 * 1024 * 1024) {
                    throw new IllegalArgumentException(
                            "Ảnh tối đa 2 MB."
                    );
                }

                byte[] bytes = Files.readAllBytes(file.toPath());

                if (bytes.length == 0
                        || bytes.length > 2 * 1024 * 1024) {

                    throw new IllegalArgumentException(
                            "Ảnh rỗng hoặc vượt quá 2 MB."
                    );
                }

                // Chỉ thay ảnh đã chọn khi ảnh mới đọc được.
                Image preview = createPhotoPreview(bytes);
                selected_photo[0] = bytes;
                imgPhoto.setImage(preview);
                imgPhoto.setVisible(true);
                imgPhoto.setManaged(true);

                lblPhoto.setText(
                        file.getName()
                        + " · "
                        + (bytes.length / 1024)
                        + " KB"
                );

                setError(lblError, "");

            } catch (Exception error) {
                setError(lblError, error.getMessage());
            }
        };

        upload_box.setOnMouseClicked(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Chọn ảnh nhân viên");

            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                            "Ảnh PNG/JPEG",
                            "*.png",
                            "*.jpg",
                            "*.jpeg"
                    )
            );

            File file = chooser.showOpenDialog(
                    dialog.getDialogPane().getScene().getWindow()
            );

            if (file != null) {
                choose_photo.accept(file);
            }
        });

        upload_box.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }

            event.consume();
        });

        upload_box.setOnDragDropped(event -> {
            boolean accepted = event.getDragboard().hasFiles()
                    && event.getDragboard().getFiles().size() == 1;

            if (accepted) {
                choose_photo.accept(
                        event.getDragboard().getFiles().get(0)
                );
            }

            event.setDropCompleted(accepted);
            event.consume();
        });

        // Form hai cột.
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);

        for (int index = 0; index < 2; index++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(50);
            grid.getColumnConstraints().add(column);
        }

        grid.add(
                createField("Họ tên *", txtName),
                0, 0, 2, 1
        );

        grid.add(
                createField("Số điện thoại *", txtPhone),
                0, 1
        );

        grid.add(
                createField("Email *", txtEmail),
                1, 1
        );

        grid.add(
                createField("Địa chỉ *", txtAddress),
                0, 2, 2, 1
        );

        grid.add(
                createField(
                        editing
                                ? "Ảnh * · có thể giữ ảnh hiện tại"
                                : "Ảnh *",
                        upload_box
                ),
                0, 3, 2, 1
        );

        grid.add(createField("Chức vụ *", cboJob), 0, 4);
        grid.add(createField("Ngày sinh *", dtpBirth), 1, 4);

        grid.add(createField("Giới tính *", cboGender), 0, 5);
        grid.add(createField("Ngày bắt đầu *", dtpStart), 1, 5);

        grid.add(
                createField("Trạng thái *", cboStatus),
                0, 6, 2, 1
        );

        VBox form = new VBox(
                12,
                createDialogTitle(
                        editing ? "Sửa nhân viên" : "Thêm nhân viên"
                ),
                new Separator(),
                grid,
                lblError
        );

        setDialogContent(dialog, form, 570);

        ButtonType cancel_type = new ButtonType(
                "Hủy",
                ButtonBar.ButtonData.CANCEL_CLOSE
        );

        ButtonType save_type = new ButtonType(
                "Lưu nhân viên",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                cancel_type,
                save_type
        );

        Button btnSave = (Button) dialog.getDialogPane()
                .lookupButton(save_type);

        Button btnCancel = (Button) dialog.getDialogPane()
                .lookupButton(cancel_type);

        btnSave.getStyleClass().add("employee-primary");
        btnCancel.getStyleClass().add("employee-secondary");

        btnSave.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> {
                    // Không tự đóng form trước khi lưu thành công.
                    event.consume();
                    setError(lblError, "");

                    if (cboGender.getValue() == null) {
                        setError(lblError, "Chọn giới tính.");
                        return;
                    }

                    Employee input = new Employee(
                            editing ? employee.maNhanVien() : null,
                            txtName.getText(),
                            txtPhone.getText(),
                            txtEmail.getText(),
                            txtAddress.getText(),
                            cboJob.getValue(),
                            dtpBirth.getValue(),
                            "Nam".equals(cboGender.getValue()),
                            dtpStart.getValue(),
                            cboStatus.getValue(),
                            selected_photo[0],
                            null,
                            null,
                            null
                    );

                    // Chuyển nghỉ việc trong form cũng cần xác nhận.
                    if (editing
                            && "Đã nghỉ việc".equals(
                                    input.trangThai()
                            )) {

                        Alert confirm = new Alert(
                                Alert.AlertType.CONFIRMATION,
                                "Nhân viên sẽ ngừng làm việc "
                                + "và tài khoản bị khóa. Tiếp tục?",
                                ButtonType.YES,
                                ButtonType.NO
                        );

                        confirm.initOwner(
                                dialog.getDialogPane()
                                        .getScene()
                                        .getWindow()
                        );

                        if (confirm.showAndWait()
                                .orElse(ButtonType.NO)
                                != ButtonType.YES) {
                            return;
                        }
                    }

                    form.setDisable(true);
                    btnSave.setDisable(true);
                    btnCancel.setDisable(true);

                    executeBackground(
                            () -> {
                                if (editing) {
                                    employee_controller
                                            .updateEmployee(input);

                                    return input.maNhanVien();
                                }

                                return employee_controller
                                        .addEmployee(input);
                            },
                            employee_id -> {
                                dialog.close();
                                loadEmployees();
                            },
                            error -> {
                                form.setDisable(false);
                                btnSave.setDisable(false);
                                btnCancel.setDisable(false);

                                setError(
                                        lblError,
                                        error.getMessage()
                                );
                            }
                    );
                }
        );

        showModal(dialog);
    }

    /**
     * Mở hộp xác nhận ngừng làm việc.
     */
    private void showDeactivate() {
        Employee employee = tblEmployees
                .getSelectionModel()
                .getSelectedItem();

        if (busy.get()
                || !employee_controller.canEdit(employee)) {
            return;
        }

        Dialog<Void> dialog = createDialog(
                "Ngừng làm việc",
                530
        );

        Label lblName = new Label(employee.hoTen());
        lblName.getStyleClass().add("employee-section-title");

        VBox summary = new VBox(
                8,
                lblName,
                new Label(employee.maNhanVien()),
                new Label(employee.chucVu()),
                new Label(employee.soDienThoai())
        );
        summary.getStyleClass().add("employee-summary");

        Label lblExplanation = createWrappedLabel(
                "Đặt trạng thái Đã nghỉ việc và khóa tài khoản "
                + "đăng nhập nếu có. Lịch hẹn, hóa đơn và ca trực "
                + "đã lưu được giữ nguyên."
        );

        Label lblError = createWrappedLabel("");
        lblError.getStyleClass().add("employee-error");
        setError(lblError, "");

        VBox content = new VBox(
                14,
                createDialogTitle(
                        "Ngừng làm việc cho nhân viên?"
                ),
                new Separator(),
                summary,
                lblExplanation,
                lblError
        );

        setDialogContent(dialog, content, 270);

        ButtonType cancel_type = new ButtonType(
                "Hủy",
                ButtonBar.ButtonData.CANCEL_CLOSE
        );

        ButtonType confirm_type = new ButtonType(
                "Xác nhận ngừng làm việc",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                cancel_type,
                confirm_type
        );

        Button btnConfirm = (Button) dialog.getDialogPane()
                .lookupButton(confirm_type);

        Button btnCancel = (Button) dialog.getDialogPane()
                .lookupButton(cancel_type);

        btnConfirm.getStyleClass().add("employee-danger");
        btnCancel.getStyleClass().add("employee-secondary");

        btnConfirm.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> {
                    event.consume();

                    btnConfirm.setDisable(true);
                    btnCancel.setDisable(true);

                    executeBackground(
                            () -> {
                                employee_controller.deactivateEmployee(
                                        employee.maNhanVien()
                                );
                                return true;
                            },
                            ignored -> {
                                dialog.close();
                                loadEmployees();
                            },
                            error -> {
                                btnConfirm.setDisable(false);
                                btnCancel.setDisable(false);

                                setError(
                                        lblError,
                                        error.getMessage()
                                );
                            }
                    );
                }
        );

        showModal(dialog);
    }

    /**
     * Tạo nội dung cột tài khoản đăng nhập.
     *
     * @param employee Nhân viên cần hiển thị.
     * @return Tên tài khoản và ghi chú.
     */
    private String accountText(Employee employee) {
        if (employee.tenDangNhap() == null) {
            return "Chưa có tài khoản\n"
                    + "Cấp tại màn Tài khoản đăng nhập";
        }

        User current_user = SessionManager.getCurrentUser();
        String result = employee.tenDangNhap();

        if (current_user != null
                && current_user.getMaNhanVien().equals(
                        employee.maNhanVien()
                )) {

            result += " · Đang đăng nhập";
        }

        if ("Admin".equals(employee.phanQuyen())) {
            result += " · Admin / Toàn quyền";
        } else if (Boolean.FALSE.equals(
                employee.taiKhoanHoatDong()
        )) {
            result += " · Đã khóa";
        }

        return result;
    }

    /**
     * Tạo cột văn bản tự xuống dòng.
     *
     * @param title Tiêu đề cột.
     * @param getter Hàm lấy dữ liệu.
     * @param width Chiều rộng ưu tiên.
     * @param badge Có dùng kiểu nhãn trạng thái hay không.
     * @return Cột dữ liệu.
     */
    private TableColumn<Employee, String> createColumn(
            String title,
            Function<Employee, String> getter,
            double width,
            boolean badge
    ) {
        TableColumn<Employee, String> column =
                new TableColumn<>(title);

        column.setPrefWidth(width);
        column.setMinWidth(80);

        column.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(
                        displayText(getter.apply(cell.getValue()))
                )
        );

        column.setCellFactory(ignored -> new TableCell<>() {
            private final Label lblValue =
                    createWrappedLabel("");

            {
                lblValue.prefWidthProperty().bind(
                        widthProperty().subtract(18)
                );
            }

            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                setText(null);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                lblValue.setText(value);

                // TableCell được tái sử dụng nên phải xóa style cũ.
                lblValue.getStyleClass().removeAll(
                        "employee-badge",
                        "employee-inactive"
                );

                if (badge) {
                    lblValue.getStyleClass().add(
                            "employee-badge"
                    );
                }

                if ("Đã nghỉ việc".equals(value)) {
                    lblValue.getStyleClass().add(
                            "employee-inactive"
                    );
                }

                setGraphic(lblValue);
            }
        });

        return column;
    }

    /**
     * Thực hiện thao tác Controller ở luồng nền.
     *
     * @param operation Công việc cần thực hiện.
     * @param on_success Xử lý thành công trên luồng JavaFX.
     * @param on_failure Xử lý lỗi trên luồng JavaFX.
     * @param <T> Kiểu kết quả.
     */
    private <T> void executeBackground(
            Supplier<T> operation,
            Consumer<T> on_success,
            Consumer<Throwable> on_failure
    ) {
        if (busy.get()) {
            return;
        }

        busy.set(true);

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
            on_failure.accept(task.getException());
        });

        Thread worker = new Thread(
                task,
                "employee-operation"
        );
        worker.setDaemon(true);
        worker.start();
    }

    /**
     * Thiết lập hộp thoại của module nhân viên.
     *
     * @param title Tiêu đề.
     * @param width Chiều rộng ưu tiên.
     * @return Hộp thoại.
     */
    private Dialog<Void> createDialog(
            String title,
            double width
    ) {
        Dialog<Void> dialog = new Dialog<>();

        dialog.setTitle(title);
        dialog.initOwner(getScene().getWindow());
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        dialog.getDialogPane().getStyleClass().add(
                "employee-dialog"
        );

        dialog.getDialogPane().getStylesheets().addAll(
                getScene().getStylesheets()
        );

        dialog.getDialogPane().setPrefWidth(width);

        return dialog;
    }

    /**
     * Đặt nội dung cuộn và giới hạn chiều cao theo màn hình.
     *
     * @param dialog Hộp thoại.
     * @param content Nội dung.
     * @param preferred_height Chiều cao ưu tiên.
     */
    private void setDialogContent(
            Dialog<?> dialog,
            VBox content,
            double preferred_height
    ) {
        ScrollPane scroll = new ScrollPane(content);

        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );
        scroll.getStyleClass().add("employee-scroll");

        javafx.stage.Window owner = dialog.getOwner();

        double screen_height = javafx.stage.Screen
                .getScreensForRectangle(
                        owner.getX(),
                        owner.getY(),
                        owner.getWidth(),
                        owner.getHeight()
                )
                .stream()
                .findFirst()
                .orElse(javafx.stage.Screen.getPrimary())
                .getVisualBounds()
                .getHeight();

        double viewport_height = Math.min(
                preferred_height,
                Math.max(150, screen_height - 170)
        );

        scroll.setPrefViewportHeight(viewport_height);
        scroll.setMinViewportHeight(viewport_height);

        dialog.getDialogPane().setContent(scroll);
    }

    /**
     * Mở hộp thoại và làm mờ nền.
     *
     * @param dialog Hộp thoại cần mở.
     */
    private void showModal(Dialog<?> dialog) {
        Parent root = getScene().getRoot();
        Effect previous_effect = root.getEffect();

        GaussianBlur blur = new GaussianBlur(6);
        blur.setInput(previous_effect);

        root.setEffect(blur);

        try {
            dialog.showAndWait();
        } finally {
            root.setEffect(previous_effect);
        }
    }

    /**
     * Tạo ô chọn ngày theo định dạng Việt Nam.
     *
     * @param value Ngày ban đầu.
     * @return Ô chọn ngày.
     */
    private DatePicker createDatePicker(LocalDate value) {
        DatePicker picker = new DatePicker(value);

        picker.setEditable(false);
        picker.setPromptText("dd/MM/yyyy");
        picker.setMaxWidth(Double.MAX_VALUE);

        picker.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null
                        ? ""
                        : date.format(date_format);
            }

            @Override
            public LocalDate fromString(String value) {
                return value == null || value.isBlank()
                        ? null
                        : LocalDate.parse(value, date_format);
            }
        });

        return picker;
    }

    /**
     * Tạo nhóm nhãn và điều khiển nhập liệu.
     *
     * @param title Tên trường.
     * @param input Điều khiển nhập liệu.
     * @return Khối trường dữ liệu.
     */
    private VBox createField(String title, Node input) {
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("employee-field-label");

        if (input instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }

        VBox field = new VBox(5, lblTitle, input);
        field.setMinWidth(0);

        return field;
    }

    /**
     * Hiển thị hoặc ẩn lỗi trên form.
     *
     * @param label Nhãn thông báo.
     * @param message Nội dung lỗi.
     */
    private void setError(Label label, String message) {
        label.setText(message);

        boolean visible = message != null
                && !message.isBlank();

        label.setVisible(visible);
        label.setManaged(visible);
    }

    /**
     * Hiển thị lỗi ngoài form.
     *
     * @param error Lỗi thao tác.
     */
    private void showError(Throwable error) {
        Alert alert = new Alert(
                Alert.AlertType.ERROR,
                displayText(error.getMessage())
        );

        alert.setHeaderText(
                "Không thực hiện được thao tác nhân viên"
        );

        if (getScene() != null) {
            alert.initOwner(getScene().getWindow());
        }

        alert.showAndWait();
    }

    /**
     * Tạo nút có class CSS riêng.
     *
     * @param title Chữ trên nút.
     * @param style Class CSS.
     * @return Nút thao tác.
     */
    private static Button createButton(
            String title,
            String style
    ) {
        Button button = new Button(title);
        button.getStyleClass().add(style);
        return button;
    }

    /**
     * Tạo nhãn tự xuống dòng.
     *
     * @param value Nội dung.
     * @return Nhãn.
     */
    private Label createWrappedLabel(String value) {
        Label label = new Label(value);

        label.setWrapText(true);
        label.setMinHeight(Region.USE_PREF_SIZE);

        return label;
    }

    /**
     * Tạo tiêu đề hộp thoại.
     *
     * @param value Nội dung.
     * @return Nhãn tiêu đề.
     */
    private Label createDialogTitle(String value) {
        Label label = new Label(value);
        label.getStyleClass().add("employee-dialog-title");
        return label;
    }

    /**
     * Xử lý nội dung chưa có dữ liệu.
     *
     * @param value Giá trị có thể null.
     * @return Nội dung hiển thị.
     */
    private static String displayText(String value) {
        return value == null || value.isBlank()
                ? "Chưa cập nhật"
                : value;
    }

    /**
     * Chuyển null thành chuỗi rỗng cho ô nhập.
     *
     * @param value Giá trị ban đầu.
     * @return Chuỗi không null.
     */
    private static String emptyText(String value) {
        return value == null ? "" : value;
    }

    /**
     * Tạo chữ viết tắt cho avatar.
     *
     * @param name Họ tên nhân viên.
     * @return Chữ đầu của hai từ cuối tên.
     */
    private static String createInitials(String name) {
        if (name == null || name.isBlank()) {
            return "NV";
        }

        String[] parts = name.trim().split("\\s+");

        String result = parts.length < 2
                ? parts[0].substring(0, 1)
                : parts[parts.length - 2].substring(0, 1)
                    + parts[parts.length - 1].substring(0, 1);

        return result.toUpperCase(Locale.ROOT);
    }

    /**
     * Đọc ảnh thu nhỏ cho form và avatar, kiểm tra định dạng và kích thước trước.
     * @param bytes Dữ liệu PNG/JPEG đã lưu hoặc vừa chọn.
     * @return Ảnh dùng để hiển thị, không làm thay đổi ảnh gốc.
     */
    private Image createPhotoPreview(byte[] bytes) {
        if (bytes == null || bytes.length == 0 || bytes.length > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("Ảnh rỗng hoặc vượt quá 2 MB.");
        }
        try (var stream = javax.imageio.ImageIO.createImageInputStream(
                new ByteArrayInputStream(bytes))) {
            if (stream == null) throw new IllegalArgumentException("Không đọc được ảnh.");
            var readers = javax.imageio.ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) throw new IllegalArgumentException("File không phải ảnh hợp lệ.");
            var reader = readers.next();
            try {
                reader.setInput(stream);
                String format = reader.getFormatName();
                if (!format.equalsIgnoreCase("png") && !format.equalsIgnoreCase("jpeg")) {
                    throw new IllegalArgumentException("Chỉ chọn ảnh PNG hoặc JPEG.");
                }
                int width = reader.getWidth(0), height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || width > 4096 || height > 4096) {
                    throw new IllegalArgumentException("Kích thước ảnh tối đa 4096 × 4096.");
                }
            } finally {
                reader.dispose();
            }
            Image image = new Image(new ByteArrayInputStream(bytes), 240, 180, true, true);
            if (image.isError()) throw new IllegalArgumentException("Ảnh bị lỗi hoặc không đọc được.");
            return image;
        } catch (java.io.IOException error) {
            throw new IllegalArgumentException("Không đọc được ảnh.", error);
        }
    }
}
