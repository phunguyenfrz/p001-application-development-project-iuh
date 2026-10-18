package com.petshop.view;

import com.petshop.controller.CustomerController;
import com.petshop.controller.PetController;
import com.petshop.model.Customer;
import com.petshop.model.Pet;

import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
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
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Giao diện cập nhật hồ sơ thú cưng.
 * Gọi Controller để thêm, sửa và ngừng theo dõi.
 */
public class PetView extends VBox {

    // Controller xử lý hồ sơ thú cưng.
    private final PetController pet_controller = new PetController();

    // Controller lấy khách hàng đang hoạt động để chọn chủ nuôi.
    private final CustomerController customer_controller =
            new CustomerController();

    // Danh sách thú cưng đang được theo dõi.
    private final ObservableList<Pet> pet_list =
            FXCollections.observableArrayList();

    // Trạng thái đang tải hoặc lưu dữ liệu.
    private final SimpleBooleanProperty busy =
            new SimpleBooleanProperty(false);

    // Định dạng ngày hiển thị trên bảng và form.
    private final DateTimeFormatter date_format =
            DateTimeFormatter.ofPattern("dd/MM/uuuu");

    // Các điều khiển của trang cập nhật.
    private final TableView<Pet> tblPets = new TableView<>();
    private final Button btnAdd = new Button("＋ Thêm thú cưng");
    private final Button btnEdit = new Button("Sửa thú cưng");
    private final Button btnDeactivate = new Button("Ngừng theo dõi");
    private final Label lblStatus = new Label();

    /**
     * Khởi tạo màn hình theo chức năng được chọn.
     *
     * @param action_id Mã chức năng từ MenuRegistry.
     */
    public PetView(String action_id) {
        // Trang tìm kiếm dùng giao diện tra cứu riêng, giữ nguyên trang cập nhật.
        if ("PET_SEARCH".equals(action_id)) {
            PetSearchView search_view = new PetSearchView();
            VBox.setVgrow(search_view, Priority.ALWAYS);
            getChildren().add(search_view);
            return;
        }

        // Các trang khác sẽ được triển khai ở bước sau.
        if (!"PET_UPDATE".equals(action_id)) {
            // Tên chức năng của màn mẫu.
            String action_name = "PET_SEARCH".equals(action_id)
                    ? "Tìm kiếm thú cưng"
                    : "Lịch sử chăm sóc";

            // Giữ màn mẫu cho chức năng chưa triển khai.
            ModuleListView placeholder = new ModuleListView(
                    "Thú cưng", action_name, null
            );

            VBox.setVgrow(placeholder, Priority.ALWAYS);
            getChildren().add(placeholder);
            return;
        }

        getStyleClass().add("pet-page");
        setPadding(new Insets(26));
        setSpacing(18);

        // Tiêu đề trang.
        Label lblTitle = new Label("Cập nhật thú cưng");
        lblTitle.getStyleClass().add("pet-title");

        // Khoảng trống đẩy các nút sang phải.
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnAdd.getStyleClass().add("pet-add");
        btnEdit.getStyleClass().add("pet-secondary");
        btnDeactivate.getStyleClass().add("pet-danger");

        // Hàng tiêu đề và thao tác.
        HBox header = new HBox(
                10, lblTitle, spacer,
                btnAdd, btnEdit, btnDeactivate
        );
        header.setAlignment(Pos.CENTER_LEFT);

        configureTable();

        lblStatus.getStyleClass().add("pet-muted");

        // Thẻ trắng chứa bảng và thông tin dòng đang chọn.
        VBox table_card = new VBox(12, tblPets, lblStatus);
        table_card.setPadding(new Insets(18));
        table_card.getStyleClass().add("pet-card");

        VBox.setVgrow(tblPets, Priority.ALWAYS);
        VBox.setVgrow(table_card, Priority.ALWAYS);

        btnAdd.disableProperty().bind(busy);
        tblPets.disableProperty().bind(busy);

        // Chỉ sửa hoặc ngừng theo dõi khi có hồ sơ được chọn.
        btnEdit.disableProperty().bind(
                busy.or(tblPets.getSelectionModel()
                        .selectedItemProperty().isNull())
        );
        btnDeactivate.disableProperty().bind(
                busy.or(tblPets.getSelectionModel()
                        .selectedItemProperty().isNull())
        );

        btnAdd.setOnAction(event -> showPetDialog(null));

        btnEdit.setOnAction(event -> showPetDialog(
                tblPets.getSelectionModel().getSelectedItem()
        ));

        btnDeactivate.setOnAction(event -> showDeactivateDialog());

        tblPets.getSelectionModel().selectedItemProperty().addListener(
                (observable, old_value, selected) -> {
                    if (!busy.get()) {
                        updateSelectionMessage(selected);
                    }
                }
        );

        registerRefreshShortcut();

        getChildren().addAll(header, table_card);
        loadPets();
    }

    /** Thiết lập bảng thú cưng theo thứ tự trong Figma. */
    private void configureTable() {
        tblPets.setItems(pet_list);
        tblPets.getStyleClass().add("pet-table");
        tblPets.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );
        tblPets.setPlaceholder(
                new Label("Chưa có thú cưng đang được theo dõi")
        );

        tblPets.getColumns().add(
                createColumn("Mã thú cưng", Pet::getMaThuCung, 185)
        );
        tblPets.getColumns().add(
                createColumn("Tên thú cưng", Pet::getTenThuCung, 120)
        );
        tblPets.getColumns().add(
                createColumn("Chủ nuôi", Pet::getTenChuNuoi, 160)
        );
        tblPets.getColumns().add(
                createColumn(
                        "SĐT chủ nuôi",
                        Pet::getSoDienThoaiChuNuoi,
                        135
                )
        );
        tblPets.getColumns().add(
                createColumn("Loài", Pet::getLoaiThuCung, 85)
        );
        tblPets.getColumns().add(
                createColumn("Giống", Pet::getGiongLoai, 110)
        );
        tblPets.getColumns().add(
                createColumn("Giới tính", Pet::getGioiTinh, 85)
        );
        tblPets.getColumns().add(
                createColumn(
                        "Ngày sinh",
                        pet -> pet.getNgaySinh() == null
                                ? "—"
                                : pet.getNgaySinh().format(date_format),
                        110
                )
        );
        tblPets.getColumns().add(
                createColumn(
                        "Cân nặng (kg)",
                        pet -> formatWeight(pet.getCanNang()),
                        110
                )
        );
        tblPets.getColumns().add(
                createColumn("Dị ứng", Pet::getDiUng, 190)
        );
    }

    /**
     * Tạo cột bảng có nội dung xuống dòng.
     *
     * @param title Tiêu đề cột.
     * @param getter Hàm lấy dữ liệu.
     * @param width Chiều rộng ưu tiên.
     * @return Cột dữ liệu thú cưng.
     */
    private TableColumn<Pet, String> createColumn(
            String title,
            Function<Pet, String> getter,
            double width
    ) {
        // Cột đang được thiết lập.
        TableColumn<Pet, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setMinWidth(75);
        column.setSortable(false);

        column.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(
                        displayText(getter.apply(cell.getValue()))
                )
        );

        column.setCellFactory(table_column -> new TableCell<>() {
            // Nhãn hiển thị nội dung của ô.
            private final Label lblValue = new Label();

            {
                lblValue.setWrapText(true);
                lblValue.getStyleClass().add("pet-cell-text");
                lblValue.prefWidthProperty().bind(
                        widthProperty().subtract(16)
                );
            }

            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                setText(null);

                if (empty) {
                    lblValue.setText("");
                    setGraphic(null);
                    setTooltip(null);
                } else {
                    lblValue.setText(value);
                    setGraphic(lblValue);
                    setTooltip(new Tooltip(value));
                }
            }
        });

        return column;
    }

    /** Tải lại các hồ sơ đang theo dõi từ Controller. */
    private void loadPets() {
        lblStatus.setText("Đang tải hồ sơ thú cưng...");

        executeBackground(
                () -> pet_controller.loadActivePets(),
                pets -> {
                    tblPets.getSelectionModel().clearSelection();
                    pet_list.setAll(pets);
                    updateSelectionMessage(null);
                },
                error -> {
                    lblStatus.setText("Không tải được hồ sơ thú cưng");
                    showError(error.getMessage());
                }
        );
    }

    /**
     * Cập nhật thông tin dưới bảng khi chọn dòng.
     *
     * @param selected Thú cưng đang chọn hoặc null.
     */
    private void updateSelectionMessage(Pet selected) {
        lblStatus.setText(
                selected == null
                        ? "Có " + pet_list.size()
                                + " thú cưng đang được theo dõi"
                        : "Đã chọn " + selected.getTenThuCung()
                                + " · " + selected.getMaThuCung()
        );
    }

    /**
     * Mở form thêm hoặc sửa thú cưng.
     *
     * @param selected Hồ sơ cần sửa; null nếu thêm mới.
     */
    private void showPetDialog(Pet selected) {
        // Phân biệt form thêm và sửa.
        boolean editing = selected != null;

        // Chủ nuôi phải được chọn từ gợi ý, không chỉ nhập tên.
        SimpleObjectProperty<Customer> selected_owner =
                new SimpleObjectProperty<>();

        // Danh sách chủ nuôi dùng để lọc gợi ý.
        ObservableList<Customer> owner_list =
                FXCollections.observableArrayList();

        // Ngăn việc điền tên sau khi chọn làm mất lựa chọn chủ nuôi.
        boolean[] setting_owner = {false};

        // Popup gợi ý chủ nuôi.
        ContextMenu owner_suggestions = new ContextMenu();
        owner_suggestions.getStyleClass().add("pet-owner-suggestions");

        // Hộp thoại form.
        Dialog<ButtonType> dialog = new Dialog<>();
        configureDialog(dialog, 800);

        // Các điều khiển nhập liệu.
        Label lblTitle = new Label(
                editing ? "Sửa thú cưng" : "Thêm thú cưng"
        );
        lblTitle.getStyleClass().add("pet-dialog-title");

        TextField txtPetId = new TextField(
                editing ? selected.getMaThuCung() : "Tự động tạo khi lưu"
        );
        txtPetId.setEditable(false);

        TextField txtOwner = new TextField();
        txtOwner.setPromptText("Tìm chủ nuôi theo mã, tên hoặc SĐT...");

        Label lblOwnerInfo = new Label("Chưa chọn chủ nuôi");
        lblOwnerInfo.setWrapText(true);
        lblOwnerInfo.getStyleClass().add("pet-owner-info");

        TextField txtName = new TextField();
        txtName.setPromptText("Nhập tên thú cưng...");

        ComboBox<String> cboSpecies = new ComboBox<>();
        cboSpecies.getItems().addAll("Chó", "Mèo", "Khác");
        cboSpecies.setEditable(true);
        cboSpecies.setPromptText("Chọn hoặc nhập loài...");

        TextField txtBreed = new TextField();
        txtBreed.setPromptText("Nhập giống...");

        ComboBox<String> cboGender = new ComboBox<>();
        cboGender.getItems().addAll("Đực", "Cái");
        cboGender.setPromptText("Chọn giới tính...");

        DatePicker dtpBirthDate = new DatePicker();
        configureBirthDate(dtpBirthDate);

        TextField txtWeight = new TextField();
        txtWeight.setPromptText("Ví dụ: 5,2");

        TextField txtAllergy = new TextField();
        txtAllergy.setPromptText("Nhập dị ứng đã ghi nhận (không bắt buộc)");

        // Các nhãn lỗi ngay dưới trường tương ứng.
        Label lblOwnerError = createErrorLabel();
        Label lblNameError = createErrorLabel();
        Label lblBirthError = createErrorLabel();
        Label lblWeightError = createErrorLabel();
        Label lblFormError = createErrorLabel();

        // Điền dữ liệu cũ khi mở form sửa.
        if (editing) {
            txtName.setText(selected.getTenThuCung());
            cboSpecies.setValue(selected.getLoaiThuCung());
            cboSpecies.getEditor().setText(
                    textOrEmpty(selected.getLoaiThuCung())
            );
            txtBreed.setText(selected.getGiongLoai());
            cboGender.setValue(selected.getGioiTinh());
            dtpBirthDate.setValue(selected.getNgaySinh());
            txtWeight.setText(formatWeight(selected.getCanNang()));
            txtAllergy.setText(selected.getDiUng());

            // Giữ chủ nuôi hiện tại kể cả khi khách đã ngừng hoạt động.
            Customer current_owner = new Customer();
            current_owner.setMaKhachHang(selected.getMaKhachHang());
            current_owner.setHoTen(selected.getTenChuNuoi());
            current_owner.setSoDienThoai(
                    selected.getSoDienThoaiChuNuoi()
            );

            selected_owner.set(current_owner);
            txtOwner.setText(selected.getTenChuNuoi());
            lblOwnerInfo.setText(ownerDescription(current_owner));
        }

        // Hai cột có chiều rộng bằng nhau.
        GridPane fields = new GridPane();
        fields.setHgap(14);
        fields.setVgap(12);

        for (int index = 0; index < 2; index++) {
            // Cấu hình chiều rộng của một cột form.
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(50);
            column.setHgrow(Priority.ALWAYS);
            fields.getColumnConstraints().add(column);
        }

        fields.add(
                createField("Tên thú cưng *", txtName, lblNameError),
                0, 0
        );
        fields.add(
                createField("Loài", cboSpecies, null),
                1, 0
        );
        fields.add(
                createField("Giống", txtBreed, null),
                0, 1
        );
        fields.add(
                createField("Giới tính", cboGender, null),
                1, 1
        );
        fields.add(
                createField("Ngày sinh", dtpBirthDate, lblBirthError),
                0, 2
        );

        // Đơn vị kg hiển thị cạnh ô cân nặng.
        Label lblKg = new Label("kg");
        lblKg.getStyleClass().add("pet-muted");

        HBox weight_box = new HBox(8, txtWeight, lblKg);
        weight_box.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(txtWeight, Priority.ALWAYS);

        fields.add(
                createField("Cân nặng *", weight_box, lblWeightError),
                1, 2
        );

        // Form một phần toàn chiều rộng, một phần hai cột.
        VBox form = new VBox(
                12,
                lblTitle,
                new Separator()
        );

        if (editing) {
            form.getChildren().add(
                    createField("Mã thú cưng", txtPetId, null)
            );
        }

        form.getChildren().addAll(
                createField("Chọn chủ nuôi *", txtOwner, lblOwnerError),
                lblOwnerInfo,
                fields,
                createField("Dị ứng", txtAllergy, null),
                lblFormError
        );

        // Cho cuộn form trên màn hình có chiều cao nhỏ.
        ScrollPane scrForm = new ScrollPane(form);
        scrForm.setFitToWidth(true);
        scrForm.setPrefViewportHeight(470);
        scrForm.getStyleClass().add("pet-form-scroll");

        dialog.getDialogPane().setContent(scrForm);

        // Các nút của form.
        ButtonType cancel_type = new ButtonType(
                "Hủy", ButtonBar.ButtonData.CANCEL_CLOSE
        );
        ButtonType save_type = new ButtonType(
                editing ? "Cập nhật" : "Lưu thú cưng",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                cancel_type, save_type
        );

        Button btnSave = (Button) dialog.getDialogPane()
                .lookupButton(save_type);
        Button btnCancel = (Button) dialog.getDialogPane()
                .lookupButton(cancel_type);

        btnSave.getStyleClass().add("pet-primary");
        btnCancel.getStyleClass().add("pet-secondary");

        btnSave.disableProperty().bind(busy);
        btnCancel.disableProperty().bind(busy);
        form.disableProperty().bind(busy);

        /*
         * Tạo lại gợi ý từ danh sách đã tải.
         * Không truy vấn SQL theo từng ký tự người dùng nhập.
         */
        Runnable show_owner_suggestions = () -> {
            owner_suggestions.hide();
            owner_suggestions.getItems().clear();

            // Từ khóa hiện tại không phân biệt dấu hoặc chữ hoa.
            String keyword = normalizeText(txtOwner.getText());

            if (keyword.isEmpty()
                    || !txtOwner.isFocused()
                    || busy.get()) {
                return;
            }

            // Tất cả chủ nuôi phù hợp với từ khóa.
            List<Customer> matches = owner_list.stream()
                    .filter(owner ->
                            normalizeText(owner.getMaKhachHang())
                                    .contains(keyword)
                                    || normalizeText(owner.getHoTen())
                                            .contains(keyword)
                                    || normalizeText(owner.getSoDienThoai())
                                            .contains(keyword)
                    )
                    .toList();

            for (Customer owner : matches.stream().limit(8).toList()) {
                // Một lựa chọn bao gồm mã, tên và SĐT.
                MenuItem item = new MenuItem(ownerDescription(owner));

                item.setOnAction(event -> {
                    setting_owner[0] = true;
                    try {
                        txtOwner.setText(owner.getHoTen());
                        selected_owner.set(owner);
                        lblOwnerInfo.setText(ownerDescription(owner));
                        lblOwnerError.setText("");
                    } finally {
                        setting_owner[0] = false;
                    }

                    owner_suggestions.hide();
                });

                owner_suggestions.getItems().add(item);
            }

            if (matches.isEmpty()) {
                MenuItem empty_item = new MenuItem(
                        "Không có chủ nuôi đang hoạt động phù hợp."
                );
                empty_item.setDisable(true);
                owner_suggestions.getItems().add(empty_item);
            } else if (matches.size() > 8) {
                MenuItem more_item = new MenuItem(
                        "Nhập thêm để thu hẹp " + matches.size() + " kết quả."
                );
                more_item.setDisable(true);
                owner_suggestions.getItems().add(more_item);
            }

            owner_suggestions.setPrefWidth(txtOwner.getWidth());
            owner_suggestions.show(txtOwner, Side.BOTTOM, 0, 3);
        };

        // Chờ người dùng ngừng gõ trong 200 ms.
        PauseTransition owner_delay =
                new PauseTransition(Duration.millis(200));

        owner_delay.setOnFinished(event ->
                show_owner_suggestions.run()
        );

        txtOwner.textProperty().addListener(
                (observable, old_value, new_value) -> {
                    if (setting_owner[0]) {
                        return;
                    }

                    // Nhập tên khác phải chọn lại hồ sơ, không giữ mã cũ.
                    selected_owner.set(null);
                    lblOwnerInfo.setText("Chưa chọn chủ nuôi");
                    owner_suggestions.hide();
                    owner_delay.stop();
                    owner_delay.playFromStart();
                }
        );

        txtOwner.setOnAction(event -> show_owner_suggestions.run());

        txtOwner.setOnMouseClicked(event -> {
            if (!txtOwner.getText().isBlank()) {
                show_owner_suggestions.run();
            }
        });

        // Tải danh sách chủ nuôi khi form xuất hiện.
        dialog.setOnShown(event -> executeBackground(
                () -> customer_controller.loadActiveCustomers(),
                owners -> {
                    owner_list.setAll(owners);
                    if (txtOwner.isFocused()
                            && selected_owner.get() == null) {
                        show_owner_suggestions.run();
                    }
                },
                error -> lblFormError.setText(error.getMessage())
        ));

        // Không đóng form giữa chừng khi đang thực hiện truy vấn.
        dialog.setOnCloseRequest(event -> {
            if (busy.get()) {
                event.consume();
            }
        });

        dialog.setOnHidden(event -> {
            owner_delay.stop();
            owner_suggestions.hide();
        });

        btnSave.addEventFilter(ActionEvent.ACTION, event -> {
            // Giữ form đến khi Service và database xác nhận thành công.
            event.consume();

            owner_delay.stop();
            owner_suggestions.hide();

            lblOwnerError.setText("");
            lblNameError.setText("");
            lblBirthError.setText("");
            lblWeightError.setText("");
            lblFormError.setText("");

            // Kết quả kiểm tra các lỗi cơ bản ngay tại form.
            boolean valid = true;

            if (selected_owner.get() == null) {
                lblOwnerError.setText(
                        "Chọn khách hàng từ danh sách để liên kết hồ sơ."
                );
                valid = false;
            }

            if (txtName.getText().trim().isEmpty()) {
                lblNameError.setText("Vui lòng nhập tên thú cưng.");
                valid = false;
            }

            // Ngày hiện tại theo múi giờ Việt Nam.
            LocalDate today = LocalDate.now(
                    ZoneId.of("Asia/Ho_Chi_Minh")
            );

            if (dtpBirthDate.getValue() != null
                    && dtpBirthDate.getValue().isAfter(today)) {
                lblBirthError.setText("Ngày sinh không ở tương lai.");
                valid = false;
            }

            // Chấp nhận dấu phẩy hoặc dấu chấm thập phân.
            String weight_text = txtWeight.getText().trim()
                    .replace(',', '.');

            double weight = 0;

            if (!weight_text.matches("[0-9]+(?:\\.[0-9]{1,2})?")) {
                lblWeightError.setText(
                        "Nhập số dương, tối đa 2 chữ số thập phân."
                );
                valid = false;
            } else {
                weight = Double.parseDouble(weight_text);

                if (!Double.isFinite(weight)
                        || weight <= 0
                        || weight > 999.99) {
                    lblWeightError.setText(
                            "Cân nặng phải lớn hơn 0 và tối đa 999,99 kg."
                    );
                    valid = false;
                }
            }

            if (!valid) {
                return;
            }

            // Đối tượng mới để không sửa trực tiếp dòng đang hiển thị.
            Pet pet = new Pet(
                    editing ? selected.getMaThuCung() : null,
                    selected_owner.get().getMaKhachHang(),
                    txtName.getText(),
                    cboSpecies.getEditor().getText(),
                    txtBreed.getText(),
                    weight,
                    dtpBirthDate.getValue(),
                    txtAllergy.getText(),
                    cboGender.getValue()
            );

            executeBackground(
                    () -> {
                        if (editing) {
                            pet_controller.updatePet(pet);
                        } else {
                            pet_controller.addPet(pet);
                        }
                        return null;
                    },
                    result -> {
                        dialog.setResult(save_type);
                        dialog.close();
                        loadPets();
                    },
                    error -> lblFormError.setText(error.getMessage())
            );
        });

        showModal(dialog);
    }

    /**
     * Tạo nhóm nhãn, điều khiển và lỗi của một trường.
     *
     * @param title Nội dung nhãn.
     * @param control Điều khiển hoặc khung nhập liệu.
     * @param error Nhãn lỗi hoặc null nếu không có.
     * @return Nhóm trường nhập.
     */
    private VBox createField(
            String title,
            Node control,
            Label error
    ) {
        // Nhãn mô tả trường nhập.
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("pet-field-label");

        if (control instanceof Control) {
            Control input_control = (Control) control;
            input_control.getStyleClass().add("pet-input");
            input_control.setMaxWidth(Double.MAX_VALUE);
        }

        // Khung chứa một trường thông tin.
        VBox field = new VBox(5, lblTitle, control);

        if (error != null) {
            field.getChildren().add(error);
        }

        return field;
    }

    /**
     * Tạo nhãn lỗi chỉ chiếm diện tích khi có nội dung.
     *
     * @return Nhãn lỗi của form.
     */
    private Label createErrorLabel() {
        // Nhãn thông báo lỗi tại trường nhập.
        Label lblError = new Label();
        lblError.setWrapText(true);
        lblError.getStyleClass().add("pet-error");
        lblError.visibleProperty().bind(
                lblError.textProperty().isNotEmpty()
        );
        lblError.managedProperty().bind(
                lblError.visibleProperty()
        );
        return lblError;
    }

    /**
     * Thiết lập lịch chọn ngày sinh.
     *
     * @param date_picker Điều khiển chọn ngày.
     */
    private void configureBirthDate(DatePicker date_picker) {
        date_picker.setPromptText("dd/MM/yyyy");
        date_picker.setEditable(false);

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

        // Không cho chọn ngày sinh trong tương lai.
        date_picker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                setDisable(
                        empty || date == null
                                || date.isAfter(LocalDate.now(
                                        ZoneId.of("Asia/Ho_Chi_Minh")
                                ))
                );
            }
        });
    }

    /** Hiển thị hộp xác nhận ngừng theo dõi hồ sơ. */
    private void showDeactivateDialog() {
        // Thú cưng đang được chọn.
        Pet selected = tblPets.getSelectionModel().getSelectedItem();

        if (selected == null) {
            return;
        }

        // Hộp xác nhận ngừng theo dõi.
        Dialog<ButtonType> dialog = new Dialog<>();
        configureDialog(dialog, 530);

        Label lblTitle = new Label("Ngừng theo dõi thú cưng?");
        lblTitle.getStyleClass().add("pet-dialog-title");

        Label lblPet = new Label(
                selected.getTenThuCung()
                        + " · " + selected.getMaThuCung()
        );
        lblPet.setWrapText(true);

        Label lblOwner = new Label(
                "Chủ nuôi: " + displayText(selected.getTenChuNuoi())
        );
        lblOwner.setWrapText(true);

        Label lblWarning = new Label(
                "Hồ sơ sẽ ngừng được theo dõi. "
                        + "Lịch sử chăm sóc được giữ nguyên."
        );
        lblWarning.setWrapText(true);

        Label lblError = createErrorLabel();

        dialog.getDialogPane().setContent(
                new VBox(
                        14,
                        lblTitle,
                        new Separator(),
                        lblPet,
                        lblOwner,
                        lblWarning,
                        lblError
                )
        );

        // Nút hủy và xác nhận ngừng theo dõi.
        ButtonType cancel_type = new ButtonType(
                "Hủy", ButtonBar.ButtonData.CANCEL_CLOSE
        );
        ButtonType confirm_type = new ButtonType(
                "Xác nhận ngừng theo dõi",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                cancel_type, confirm_type
        );

        Button btnConfirm = (Button) dialog.getDialogPane()
                .lookupButton(confirm_type);
        Button btnCancel = (Button) dialog.getDialogPane()
                .lookupButton(cancel_type);

        btnConfirm.getStyleClass().add("pet-danger");
        btnCancel.getStyleClass().add("pet-secondary");

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
                        pet_controller.deactivatePet(
                                selected.getMaThuCung()
                        );
                        return null;
                    },
                    result -> {
                        dialog.setResult(confirm_type);
                        dialog.close();
                        loadPets();
                    },
                    error -> lblError.setText(error.getMessage())
            );
        });

        showModal(dialog);
    }

    /**
     * Thiết lập kiểu hộp thoại của module thú cưng.
     *
     * @param dialog Hộp thoại cần thiết lập.
     * @param width Chiều rộng ưu tiên.
     */
    private void configureDialog(Dialog<?> dialog, double width) {
        dialog.initOwner(getScene().getWindow());
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        dialog.getDialogPane().getStyleClass().add("pet-dialog");
        dialog.getDialogPane().setPrefWidth(width);
        dialog.getDialogPane().getStylesheets().addAll(
                getScene().getStylesheets()
        );
    }

    /**
     * Mở hộp thoại và làm mờ nền chính.
     *
     * @param dialog Hộp thoại cần mở.
     */
    private void showModal(Dialog<?> dialog) {
        // Lưu hiệu ứng cũ để khôi phục sau khi đóng.
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
     * @param operation Thao tác đọc hoặc ghi dữ liệu.
     * @param on_success Xử lý khi thành công.
     * @param on_failure Xử lý khi thất bại.
     * @param <T> Kiểu kết quả.
     */
    private <T> void executeBackground(
            Supplier<T> operation,
            Consumer<T> on_success,
            Consumer<Throwable> on_failure
    ) {
        busy.set(true);

        // Không truy cập các điều khiển giao diện trong call().
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

        // Luồng xử lý tự kết thúc cùng ứng dụng.
        Thread worker = new Thread(task, "pet-operation");
        worker.setDaemon(true);
        worker.start();
    }

    /** Đăng ký F5 khi mở trang, gỡ khi rời trang. */
    private void registerRefreshShortcut() {
        // Bộ xử lý tải lại danh sách bằng F5.
        EventHandler<KeyEvent> refresh_handler = event -> {
            if (event.getCode() == KeyCode.F5) {
                if (!busy.get()) {
                    loadPets();
                }
                event.consume();
            }
        };

        sceneProperty().addListener(
                (observable, old_scene, new_scene) -> {
                    if (old_scene != null) {
                        old_scene.removeEventFilter(
                                KeyEvent.KEY_PRESSED,
                                refresh_handler
                        );
                    }

                    if (new_scene != null) {
                        new_scene.addEventFilter(
                                KeyEvent.KEY_PRESSED,
                                refresh_handler
                        );
                    }
                }
        );
    }

    /**
     * Hiển thị lỗi ngoài form.
     *
     * @param message Nội dung lỗi.
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.initOwner(getScene().getWindow());
        alert.setTitle("Thông báo");
        alert.setHeaderText("Không thực hiện được thao tác");
        alert.showAndWait();
    }

    /**
     * Tạo mô tả chủ nuôi trong gợi ý.
     *
     * @param owner Khách hàng cần hiển thị.
     * @return Tên, mã và số điện thoại.
     */
    private String ownerDescription(Customer owner) {
        return displayText(owner.getHoTen())
                + " · " + owner.getMaKhachHang()
                + " · " + displayText(owner.getSoDienThoai());
    }

    /**
     * Định dạng cân nặng bằng dấu phẩy thập phân.
     *
     * @param weight Cân nặng cần hiển thị.
     * @return Chuỗi cân nặng không có số 0 thừa ở cuối.
     */
    private String formatWeight(double weight) {
        return java.math.BigDecimal.valueOf(weight)
                .stripTrailingZeros()
                .toPlainString()
                .replace('.', ',');
    }

    /**
     * Chuẩn hóa từ khóa tìm kiếm.
     *
     * @param value Chuỗi cần chuẩn hóa.
     * @return Chuỗi chữ thường không dấu.
     */
    private String normalizeText(String value) {
        return Normalizer.normalize(
                        textOrEmpty(value),
                        Normalizer.Form.NFD
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
     * @param value Chuỗi đầu vào.
     * @return Chuỗi ban đầu hoặc chuỗi rỗng.
     */
    private String textOrEmpty(String value) {
        return value == null ? "" : value;
    }

    /**
     * Hiển thị dấu gạch cho trường chưa có thông tin.
     *
     * @param value Chuỗi đầu vào.
     * @return Giá trị ban đầu hoặc dấu gạch.
     */
    private String displayText(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }
}