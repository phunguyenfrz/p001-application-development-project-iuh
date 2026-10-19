package com.petshop.view.pet;

import com.petshop.controller.PetController;
import com.petshop.model.Pet;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.Effect;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/** Tra cứu hồ sơ thú cưng qua Controller, bao gồm hồ sơ ngừng theo dõi. */
public class PetSearchView extends VBox {
    private final PetController pet_controller = new PetController();
    private final ObservableList<Pet> pet_list = FXCollections.observableArrayList();
    private final TableView<Pet> tblPets = new TableView<>();
    private final TextField txtKeyword = new TextField();
    private final ComboBox<String> cboStatus = new ComboBox<>();
    private final Label lblStatus = new Label();
    private final SimpleBooleanProperty busy = new SimpleBooleanProperty(false);
    private final DateTimeFormatter date_format = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    // Chỉ đổi từ khóa đang áp dụng khi bấm Tìm kiếm hoặc Enter.
    private String submitted_keyword = "";

    /** Khởi tạo trang tìm kiếm và tải dữ liệu ở luồng nền. */
    public PetSearchView() {
        getStyleClass().addAll("pet-page", "pet-search-page");
        setPadding(new Insets(26));
        setSpacing(18);
        Label lblTitle = new Label("Tìm kiếm thú cưng");
        lblTitle.getStyleClass().add("pet-title");
        txtKeyword.setPromptText("Tìm theo mã, tên thú cưng, chủ nuôi hoặc số điện thoại...");
        txtKeyword.setMaxWidth(Double.MAX_VALUE);
        Button btnSearch = new Button("Tìm kiếm");
        btnSearch.getStyleClass().add("pet-search-primary");
        HBox search_group = new HBox(8, txtKeyword, btnSearch);
        HBox.setHgrow(txtKeyword, Priority.ALWAYS);
        VBox search_field = new VBox(6, new Label("Tra cứu hồ sơ thú cưng"), search_group);
        HBox.setHgrow(search_field, Priority.ALWAYS);
        cboStatus.getItems().addAll("Tất cả", "Đang theo dõi", "Ngừng theo dõi");
        cboStatus.setValue("Tất cả");
        cboStatus.setPrefWidth(150);
        VBox status_field = new VBox(6, new Label("Trạng thái"), cboStatus);
        Button btnInfo = new Button("Xem thông tin");
        Button btnRefresh = new Button("Làm mới");
        btnInfo.getStyleClass().add("pet-secondary");
        btnRefresh.getStyleClass().add("pet-secondary");
        HBox toolbar = new HBox(12, search_field, status_field, btnInfo, btnRefresh);
        toolbar.setAlignment(Pos.BOTTOM_LEFT);
        configureTable();
        lblStatus.getStyleClass().add("pet-muted");
        VBox card = new VBox(12, tblPets, lblStatus);
        card.getStyleClass().add("pet-card");
        card.setPadding(new Insets(18));
        VBox.setVgrow(tblPets, Priority.ALWAYS);
        VBox.setVgrow(card, Priority.ALWAYS);
        getChildren().addAll(lblTitle, toolbar, card);

        btnInfo.disableProperty().bind(busy.or(tblPets.getSelectionModel().selectedItemProperty().isNull()));
        btnSearch.disableProperty().bind(busy);
        btnRefresh.disableProperty().bind(busy);
        txtKeyword.disableProperty().bind(busy);
        cboStatus.disableProperty().bind(busy);
        tblPets.disableProperty().bind(busy);
        btnSearch.setOnAction(event -> searchPets());
        txtKeyword.setOnAction(event -> searchPets());
        cboStatus.setOnAction(event -> filterPets());
        btnRefresh.setOnAction(event -> refreshPets());
        btnInfo.setOnAction(event -> showPetInformation());
        tblPets.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !busy.get())
                showPetInformation();
        });
        tblPets.getSelectionModel().selectedItemProperty().addListener((observable, previous, selected) -> {
            if (!busy.get())
                updateSelectionMessage();
        });
        // Chỉ xử lý F5 khi sự kiện nằm trong trang hiện tại.
        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F5 && !busy.get()) {
                refreshPets();
                event.consume();
            }
        });
        loadPets();
    }

    /** Thiết lập các cột và cho hàng tăng chiều cao theo nội dung. */
    private void configureTable() {
        tblPets.getStyleClass().add("pet-table");
        tblPets.setFixedCellSize(-1);
        tblPets.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        addColumn("Mã thú cưng", Pet::getMaThuCung, 180);
        addColumn("Tên thú cưng", Pet::getTenThuCung, 110);
        addColumn("Chủ nuôi", Pet::getTenChuNuoi, 150);
        addColumn("SĐT chủ nuôi", Pet::getSoDienThoaiChuNuoi, 130);
        addColumn("Loài", Pet::getLoaiThuCung, 75);
        addColumn("Giống", Pet::getGiongLoai, 110);
        addColumn("Giới tính", Pet::getGioiTinh, 80);
        addColumn("Ngày sinh", pet -> formatBirthDate(pet), 110);
        addColumn("Cân nặng (kg)", pet -> formatWeight(pet), 100);
        addColumn("Dị ứng", Pet::getDiUng, 180);
    }

    /**
     * Thêm cột văn bản xuống dòng khi nội dung dài.
     *
     * @param title  Tiêu đề cột.
     * @param getter Hàm lấy dữ liệu từ hồ sơ.
     * @param width  Chiều rộng ưu tiên.
     */
    private void addColumn(String title, Function<Pet, String> getter, double width) {
        TableColumn<Pet, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setMinWidth(65);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(displayText(getter.apply(cell.getValue()))));
        column.setCellFactory(ignored -> new TableCell<>() {
            private final Label lblValue = new Label();
            {
                lblValue.setWrapText(true);
                lblValue.setMinHeight(Region.USE_PREF_SIZE);
                lblValue.getStyleClass().add("pet-cell-text");
                lblValue.prefWidthProperty().bind(widthProperty().subtract(16));
            }

            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                setText(null);
                lblValue.setText(empty ? "" : value);
                setGraphic(empty ? null : lblValue);
                setTooltip(empty ? null : new Tooltip(value));
            }
        });
        tblPets.getColumns().add(column);
    }

    /** Áp dụng từ khóa đã nhập, hỗ trợ tìm không phân biệt dấu. */
    private void searchPets() {
        submitted_keyword = normalizeText(txtKeyword.getText());
        filterPets();
    }

    /** Kết hợp từ khóa và trạng thái, không thay đổi dữ liệu gốc. */
    private void filterPets() {
        String status = cboStatus.getValue();
        List<Pet> matches = pet_list.stream().filter(pet -> {
            boolean status_matches = "Tất cả".equals(status)
                    || ("Đang theo dõi".equals(status) && pet.isTrangThai())
                    || ("Ngừng theo dõi".equals(status) && !pet.isTrangThai());
            boolean keyword_matches = List.of(displayText(pet.getMaThuCung()),
                    displayText(pet.getTenThuCung()), displayText(pet.getTenChuNuoi()),
                    displayText(pet.getMaKhachHang()), displayText(pet.getSoDienThoaiChuNuoi()))
                    .stream().anyMatch(value -> normalizeText(value).contains(submitted_keyword));
            return status_matches && keyword_matches;
        }).toList();
        tblPets.getSelectionModel().clearSelection();
        tblPets.setItems(FXCollections.observableArrayList(matches));
        tblPets.setPlaceholder(new Label(pet_list.isEmpty()
                ? "Chưa có hồ sơ thú cưng"
                : "Không tìm thấy thú cưng phù hợp"));
        updateSelectionMessage();
    }

    /** Xóa điều kiện tra cứu và đọc lại dữ liệu từ database. */
    private void refreshPets() {
        txtKeyword.clear();
        submitted_keyword = "";
        cboStatus.setValue("Tất cả");
        loadPets();
    }

    /** Gọi Controller ở luồng nền để không làm đứng giao diện. */
    private void loadPets() {
        if (busy.get())
            return;
        busy.set(true);
        tblPets.getItems().clear();
        lblStatus.setText("Đang tải hồ sơ thú cưng...");
        tblPets.setPlaceholder(new Label("Đang tải dữ liệu..."));
        Task<List<Pet>> task = new Task<>() {
            @Override
            protected List<Pet> call() {
                return pet_controller.loadPets();
            }
        };
        task.setOnSucceeded(event -> {
            pet_list.setAll(task.getValue());
            busy.set(false);
            filterPets();
        });
        task.setOnFailed(event -> {
            busy.set(false);
            pet_list.clear();
            lblStatus.setText("Không tải được hồ sơ. Bấm Làm mới để thử lại.");
            tblPets.setPlaceholder(new Label("Không thể tải dữ liệu"));
            Alert alert = new Alert(Alert.AlertType.ERROR);
            if (getScene() != null)
                alert.initOwner(getScene().getWindow());
            alert.setHeaderText("Không tải được danh sách thú cưng");
            alert.setContentText(displayText(task.getException().getMessage()));
            alert.showAndWait();
        });
        Thread worker = new Thread(task, "pet-search-load");
        worker.setDaemon(true);
        worker.start();
    }

    /** Hiển thị số kết quả hoặc thú cưng đang được chọn. */
    private void updateSelectionMessage() {
        Pet selected = tblPets.getSelectionModel().getSelectedItem();
        lblStatus.setText(selected == null ? "Tìm thấy " + tblPets.getItems().size() + " thú cưng"
                : "Đã chọn " + displayText(selected.getTenThuCung()) + " · " + selected.getMaThuCung());
    }

    /**
     * Hiển thị thông tin thú cưng theo bố cục thẻ và ba cột.
     * Chỉ xem thông tin, không chỉnh sửa dữ liệu.
     */
    private void showPetInformation() {
        Pet pet = tblPets.getSelectionModel().getSelectedItem();

        if (pet == null || busy.get()) {
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(getScene().getWindow());
        dialog.initModality(Modality.WINDOW_MODAL);

        // Bỏ thanh tiêu đề hệ điều hành để giống thiết kế.
        dialog.initStyle(javafx.stage.StageStyle.UNDECORATED);
        dialog.setTitle("Thông tin thú cưng");

        DialogPane pane = dialog.getDialogPane();
        pane.getStyleClass().addAll("pet-dialog", "pet-info-dialog");
        pane.getStylesheets().addAll(getScene().getStylesheets());
        pane.setPrefWidth(650);

        // Tiêu đề và đường phân cách.
        Label lblTitle = new Label("Thông tin thú cưng");
        lblTitle.getStyleClass().add("pet-dialog-title");

        // Thẻ đầu tiên chứa tên và mã thú cưng.
        VBox identity_card = new VBox(
                16,
                createInformationField("Tên thú cưng", pet.getTenThuCung()),
                createInformationField("Mã thú cưng", pet.getMaThuCung()));
        identity_card.getStyleClass().add("pet-info-card");

        // Hàng thông tin chủ nuôi gồm ba cột.
        GridPane owner_grid = createInformationGrid();

        owner_grid.add(
                createInformationField("Chủ nuôi", pet.getTenChuNuoi()),
                0, 0);
        owner_grid.add(
                createInformationField("Mã khách hàng", pet.getMaKhachHang()),
                1, 0);
        owner_grid.add(
                createInformationField(
                        "Số điện thoại",
                        pet.getSoDienThoaiChuNuoi()),
                2, 0);

        // Thông tin thú cưng được chia thành hai hàng, mỗi hàng ba cột.
        GridPane pet_grid = createInformationGrid();

        pet_grid.add(
                createInformationField("Loài", pet.getLoaiThuCung()),
                0, 0);
        pet_grid.add(
                createInformationField("Giống", pet.getGiongLoai()),
                1, 0);
        pet_grid.add(
                createInformationField("Giới tính", pet.getGioiTinh()),
                2, 0);
        pet_grid.add(
                createInformationField("Ngày sinh", formatBirthDate(pet)),
                0, 1);
        pet_grid.add(
                createInformationField(
                        "Cân nặng",
                        formatWeight(pet) + " kg"),
                1, 1);
        pet_grid.add(
                createInformationField("Dị ứng", pet.getDiUng()),
                2, 1);

        // Giữ trạng thái hồ sơ để nhận biết thú cưng đã ngừng theo dõi.
        Label lblTrackingStatus = new Label(
                pet.isTrangThai() ? "Đang theo dõi" : "Ngừng theo dõi");
        lblTrackingStatus.getStyleClass().add(
                pet.isTrangThai()
                        ? "pet-search-active"
                        : "pet-search-inactive");

        VBox content = new VBox(
                14,
                lblTitle,
                new Separator(),
                identity_card,
                owner_grid,
                new Separator(),
                pet_grid);

        // Cho phép cuộn khi màn hình nhỏ hoặc nội dung dài.
        ScrollPane scrInformation = new ScrollPane(content);
        scrInformation.setFitToWidth(true);
        // Chiều cao tự tính theo nội dung, tránh khoảng trắng phía dưới.
        scrInformation.setPrefViewportHeight(Region.USE_COMPUTED_SIZE);
        scrInformation.getStyleClass().add("pet-form-scroll");

        pane.setContent(scrInformation);

        ButtonType close_type = new ButtonType(
                "Đóng",
                ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().add(close_type);
        pane.lookupButton(close_type).getStyleClass().add("pet-secondary");

        // Làm mờ nền và khôi phục khi đóng hộp thoại.
        javafx.scene.Parent owner_root = getScene().getRoot();
        Effect previous_effect = owner_root.getEffect();

        GaussianBlur blur = new GaussianBlur(6);
        blur.setInput(previous_effect);
        owner_root.setEffect(blur);

        try {
            dialog.showAndWait();
        } finally {
            owner_root.setEffect(previous_effect);
        }
    }

    /**
     * Tạo trường thông tin chỉ đọc, hỗ trợ xuống dòng.
     *
     * @param title Tên trường.
     * @param value Nội dung hiển thị.
     * @return Khối gồm nhãn và giá trị.
     */
    private VBox createInformationField(String title, String value) {
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("pet-info-field-title");

        Label lblValue = new Label(displayText(value));
        lblValue.getStyleClass().add("pet-info-field-value");
        lblValue.setWrapText(true);
        lblValue.setMinHeight(Region.USE_PREF_SIZE);
        lblValue.setMaxWidth(Double.MAX_VALUE);

        VBox field = new VBox(6, lblTitle, lblValue);
        field.setMinWidth(0);
        field.setMaxWidth(Double.MAX_VALUE);

        return field;
    }

    /**
     * Tạo lưới ba cột có chiều rộng bằng nhau.
     *
     * @return Lưới hiển thị thông tin.
     */
    private GridPane createInformationGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);

        for (int i = 0; i < 3; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / 3);
            column.setFillWidth(true);
            grid.getColumnConstraints().add(column);
        }

        return grid;
    }

    /**
     * @param pet Hồ sơ cần hiển thị. @return Ngày sinh hoặc dấu gạch nếu chưa có.
     */
    private String formatBirthDate(Pet pet) {
        return pet.getNgaySinh() == null ? "—" : pet.getNgaySinh().format(date_format);
    }

    /** @param pet Hồ sơ cần hiển thị. @return Cân nặng dùng dấu phẩy thập phân. */
    private String formatWeight(Pet pet) {
        return java.math.BigDecimal.valueOf(pet.getCanNang()).stripTrailingZeros().toPlainString().replace('.', ',');
    }

    /** @param value Giá trị có thể trống. @return Nội dung hiển thị an toàn. */
    private static String displayText(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    /**
     * @param value Từ khóa gốc. @return Chuỗi bỏ dấu và không phân biệt hoa thường.
     */
    private static String normalizeText(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").replace('đ', 'd').replace('Đ', 'D')
                .toLowerCase(Locale.ROOT).trim();
    }
}

