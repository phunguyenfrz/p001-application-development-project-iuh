package com.petshop.view;

import com.petshop.controller.PetCareController;
import com.petshop.controller.PetController;
import com.petshop.model.*;
import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.Effect;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** Lịch sử chăm sóc: chọn thú cưng, lọc lịch hẹn, xem chi tiết và cập nhật hồ sơ. */
public class PetCareHistoryView extends VBox {
    private final PetController pet_controller = new PetController();
    private final PetCareController care_controller = new PetCareController();
    private final SimpleBooleanProperty busy = new SimpleBooleanProperty(false);
    private final TextField txtSearch = new TextField();
    private final ContextMenu pet_suggestions = new ContextMenu();
    private final DatePicker dtpFrom = new DatePicker();
    private final DatePicker dtpTo = new DatePicker();
    private final ComboBox<String> cboStatus = new ComboBox<>();
    private final TableView<PetCareVisit> tblHistory = new TableView<>();
    private final VBox profile_card = new VBox(7);
    private final Label lblSearchMessage = new Label();
    private final Label lblFilterError = new Label();
    private final Label lblCount = new Label("Chưa chọn thú cưng");
    private final Label lblListTitle = new Label("Danh sách lịch hẹn");
    private final Button btnUpdate = new Button("Cập nhật thông tin chăm sóc");
    private List<Pet> all_pets = List.of();
    private Pet selected_pet;
    private PetCareProfile selected_profile;
    private final DateTimeFormatter date_format = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private final DateTimeFormatter time_format = DateTimeFormatter.ofPattern("HH:mm");

    /** Khởi tạo bố cục và tải danh sách thú cưng qua Controller. */
    public PetCareHistoryView() {
        getStyleClass().addAll("pet-page", "pet-care-page");
        setPadding(new Insets(24));
        setSpacing(12);
        Label lblTitle = new Label("Lịch sử chăm sóc");
        lblTitle.getStyleClass().add("pet-title");
        txtSearch.setPromptText("Tìm theo mã, tên thú cưng hoặc chủ nuôi...");
        txtSearch.setPrefWidth(560);
        Button btnSearch = button("Tìm kiếm", "care-primary");
        HBox search_row = new HBox(8, txtSearch, btnSearch);
        search_row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(txtSearch, Priority.ALWAYS);
        search_row.setMaxWidth(720);
        VBox search_box = new VBox(5, new Label("Tìm kiếm thú cưng"), search_row, lblSearchMessage);
        lblSearchMessage.getStyleClass().add("pet-muted");
        profile_card.getStyleClass().addAll("care-profile-card", "care-profile-compact");
        profile_card.setVisible(false);
        profile_card.setManaged(false);
        cboStatus.getItems().addAll("Tất cả", "Đã đặt", "Đã xác nhận", "Đang thực hiện", "Hoàn thành", "Đã hủy");
        cboStatus.setValue("Tất cả");
        cboStatus.setPrefWidth(170);
        // Dùng lịch chọn ngày để tránh nhập sai định dạng.
        configureDatePicker(dtpFrom);
        configureDatePicker(dtpTo);
        Button btnApply = button("Áp dụng", "care-primary");
        Button btnClear = button("Xóa bộ lọc", "pet-secondary");
        HBox filters = new HBox(12,
                field("Từ ngày", dtpFrom), field("Đến ngày", dtpTo),
                field("Trạng thái lịch hẹn", cboStatus), btnApply, btnClear);
        filters.setAlignment(Pos.BOTTOM_LEFT);
        Label lblHint = new Label("Để trống ngày để xem lịch sử không giới hạn khoảng ngày.");
        lblHint.getStyleClass().add("pet-muted");
        lblFilterError.getStyleClass().add("care-error");
        setMessage(lblFilterError, "");
        configureTable();
        Button btnDetails = button("Xem chi tiết", "care-primary");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label lblSort = new Label("Ngày và giờ hẹn mới nhất trước");
        lblSort.getStyleClass().add("pet-muted");
        HBox list_header = new HBox(12, new VBox(4, lblListTitle, lblSort), spacer, btnDetails);
        list_header.setAlignment(Pos.CENTER_LEFT);
        Label lblSummary = new Label("Trong kết quả đang hiển thị");
        lblSummary.getStyleClass().add("pet-muted");
        VBox list_card = new VBox(10, list_header, tblHistory, new Separator(), lblSummary, lblCount);
        list_card.getStyleClass().add("pet-card");
        list_card.setPadding(new Insets(14));
        VBox.setVgrow(tblHistory, Priority.ALWAYS);
        VBox.setVgrow(list_card, Priority.ALWAYS);
        getChildren().addAll(lblTitle, search_box, profile_card, filters, lblHint, lblFilterError, list_card);
        txtSearch.disableProperty().bind(busy);
        btnSearch.disableProperty().bind(busy);
        filters.disableProperty().bind(busy);
        tblHistory.disableProperty().bind(busy);
        btnDetails.disableProperty().bind(busy.or(tblHistory.getSelectionModel().selectedItemProperty().isNull()));
        btnUpdate.disableProperty().bind(busy);
        btnSearch.setOnAction(event -> showSuggestions());
        txtSearch.setOnAction(event -> showSuggestions());
        btnApply.setOnAction(event -> loadHistory());
        btnClear.setOnAction(event -> {
            dtpFrom.setValue(null);
            dtpTo.setValue(null);
            cboStatus.setValue("Tất cả");
            loadHistory();
        });
        btnDetails.setOnAction(event -> showDetails());
        btnUpdate.setOnAction(event -> showProfileForm());
        tblHistory.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !busy.get()) showDetails();
        });
        PauseTransition debounce = new PauseTransition(Duration.millis(200));
        debounce.setOnFinished(event -> showSuggestions());
        txtSearch.textProperty().addListener((observable, previous, current) -> {
            pet_suggestions.hide();
            if (txtSearch.isFocused()) debounce.playFromStart();
        });
        sceneProperty().addListener((observable, previous, current) -> {
            if (current == null) { debounce.stop(); pet_suggestions.hide(); }
        });
        pet_suggestions.getStyleClass().add("care-pet-suggestions");
        lblSearchMessage.setText("Đang tải danh sách thú cưng...");
        runBackground(pet_controller::loadPets, pets -> {
            all_pets = List.copyOf(pets);
            lblSearchMessage.setText("Nhập từ khóa và chọn thú cưng trong danh sách gợi ý.");
        }, error -> {
            lblSearchMessage.setText("Không tải được thú cưng. Bấm Thử lại để tải lại.");
            btnSearch.setText("Thử lại");
            btnSearch.setOnAction(event -> {
                runBackground(pet_controller::loadPets, pets -> {
                    all_pets = List.copyOf(pets);
                    btnSearch.setText("Tìm kiếm");
                    btnSearch.setOnAction(search_event -> showSuggestions());
                    lblSearchMessage.setText("Đã tải danh sách thú cưng. Nhập từ khóa để tìm.");
                }, this::showError);
            });
            showError(error);
        });
    }

    /** Tạo các cột của danh sách lịch hẹn. */
    private void configureTable() {
        tblHistory.getStyleClass().add("pet-table");
        tblHistory.setFixedCellSize(-1);
        tblHistory.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblHistory.setPlaceholder(new Label("Chọn thú cưng để xem lịch sử"));
        tblHistory.getColumns().add(column("Mã lịch hẹn", PetCareVisit::maLichHen, 190));
        tblHistory.getColumns().add(column("Ngày hẹn", visit -> visit.ngayHen().format(date_format), 110));
        tblHistory.getColumns().add(column("Giờ hẹn", visit -> visit.gioHen().format(time_format), 90));
        tblHistory.getColumns().add(column("Nhân viên phụ trách", PetCareVisit::tenNhanVien, 170));
        tblHistory.getColumns().add(column("Trạng thái", PetCareVisit::trangThai, 120));
        tblHistory.getColumns().add(column("Ghi chú", PetCareVisit::ghiChu, 240));
    }

    /** Tìm không dấu, bao gồm hồ sơ đã ngừng theo dõi để giữ khả năng tra cứu lịch sử. */
    private void showSuggestions() {
        if (busy.get() || getScene() == null) return;
        pet_suggestions.hide();
        pet_suggestions.getItems().clear();
        String keyword = normalize(txtSearch.getText());
        if (keyword.isEmpty()) {
            lblSearchMessage.setText("Nhập mã, tên thú cưng, tên chủ nuôi hoặc số điện thoại.");
            return;
        }
        List<Pet> matches = all_pets.stream().filter(pet ->
                normalize(pet.getMaThuCung()).contains(keyword)
                || normalize(pet.getTenThuCung()).contains(keyword)
                || normalize(pet.getTenChuNuoi()).contains(keyword)
                || normalize(pet.getSoDienThoaiChuNuoi()).contains(keyword)).toList();
        lblSearchMessage.setText(matches.isEmpty() ? "Không tìm thấy thú cưng phù hợp."
                : "Tìm thấy " + matches.size() + " thú cưng. Chọn hồ sơ cần xem.");
        for (Pet pet : matches.stream().limit(10).toList()) {
            GridPane row = new GridPane();
            row.setHgap(12);
            double[] widths = {34, 20, 46};
            for (double width : widths) {
                ColumnConstraints constraint = new ColumnConstraints();
                constraint.setPercentWidth(width);
                row.getColumnConstraints().add(constraint);
            }
            row.setPrefWidth(Math.max(350, txtSearch.getWidth() - 35));
            row.add(wrapped(text(pet.getMaThuCung())), 0, 0);
            row.add(wrapped(text(pet.getTenThuCung())), 1, 0);
            row.add(wrapped(text(pet.getTenChuNuoi()) + " · " + text(pet.getSoDienThoaiChuNuoi())), 2, 0);
            CustomMenuItem item = new CustomMenuItem(row, true);
            item.setOnAction(event -> selectPet(pet));
            pet_suggestions.getItems().add(item);
        }
        if (!matches.isEmpty()) pet_suggestions.show(txtSearch, Side.BOTTOM, 0, 2);
    }

    /** @param pet Hồ sơ chọn từ kết quả tìm kiếm. */
    private void selectPet(Pet pet) {
        pet_suggestions.hide();
        selected_pet = pet;
        selected_profile = null;
        txtSearch.clear();
        profile_card.getChildren().clear();
        profile_card.setVisible(false);
        profile_card.setManaged(false);
        dtpFrom.setValue(null);
        dtpTo.setValue(null);
        cboStatus.setValue("Tất cả");
        loadHistory();
    }

    /** Tải hồ sơ và lịch hẹn bằng bộ lọc, giữ kết quả cũ nếu khoảng ngày sai. */
    private void loadHistory() {
        if (busy.get()) return;
        setMessage(lblFilterError, "");
        if (selected_pet == null) {
            setMessage(lblFilterError, "Chọn thú cưng trước khi áp dụng bộ lọc.");
            return;
        }
        LocalDate from_date = dtpFrom.getValue();
        LocalDate to_date = dtpTo.getValue();
        if (from_date != null && to_date != null && from_date.isAfter(to_date)) {
            setMessage(lblFilterError, "Từ ngày phải nhỏ hơn hoặc bằng Đến ngày. Bộ lọc chưa được áp dụng.");
            return;
        }
        String pet_id = selected_pet.getMaThuCung();
        String status = cboStatus.getValue();
        tblHistory.getItems().clear();
        tblHistory.setPlaceholder(new Label("Đang tải dữ liệu..."));
        lblCount.setText("Đang tải...");
        runBackground(() -> new CareData(care_controller.loadProfile(pet_id),
                care_controller.loadVisits(pet_id, from_date, to_date, status)), result -> {
            selected_profile = result.profile();
            renderProfile();
            tblHistory.setItems(FXCollections.observableArrayList(result.visits()));
            tblHistory.setPlaceholder(new Label("Không có lịch hẹn phù hợp với bộ lọc"));
            lblListTitle.setText("Hoàn thành".equals(status) ? "Các lần chăm sóc đã hoàn thành" : "Danh sách lịch hẹn");
            lblCount.setText(result.visits().size() + " lịch hẹn" + ("Hoàn thành".equals(status) ? " hoàn thành" : ""));
            lblSearchMessage.setText("Đang xem: " + selected_pet.getTenThuCung() + " · " + pet_id);
        }, error -> {
            selected_profile = null;
            profile_card.setVisible(false);
            profile_card.setManaged(false);
            lblCount.setText("Chưa tải được kết quả");
            tblHistory.setPlaceholder(new Label("Không tải được lịch sử. Bấm Áp dụng để thử lại."));
            showError(error);
        });
    }

    /** Hiển thị thẻ thông tin hiện tại; không cho sửa hồ sơ đã ngừng theo dõi. */
    private void renderProfile() {
        Label lblName = new Label(selected_pet.getTenThuCung());
        lblName.getStyleClass().add("care-pet-name");
        Label lblId = new Label(selected_pet.getMaThuCung() +
                (selected_profile.dangTheoDoi() ? "" : " · Ngừng theo dõi"));
        lblId.getStyleClass().add("pet-muted");
        Button btnChange = button("Đổi thú cưng", "pet-secondary");
        btnChange.disableProperty().bind(busy);
        btnChange.setOnAction(event -> {
            selected_pet = null;
            selected_profile = null;
            profile_card.setVisible(false);
            profile_card.setManaged(false);
            tblHistory.getItems().clear();
            tblHistory.setPlaceholder(new Label("Chọn thú cưng để xem lịch sử"));
            lblCount.setText("Chưa chọn thú cưng");
            lblSearchMessage.setText("Nhập từ khóa để chọn thú cưng khác.");
            setMessage(lblFilterError, "");
            txtSearch.clear();
            txtSearch.requestFocus();
        });
        btnUpdate.setVisible(selected_profile.dangTheoDoi());
        btnUpdate.setManaged(selected_profile.dangTheoDoi());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(12, new VBox(4, lblName, lblId), spacer, btnUpdate, btnChange);
        header.setAlignment(Pos.CENTER_LEFT);
        btnUpdate.getStyleClass().remove("care-primary");
        btnUpdate.getStyleClass().add("care-primary");
        // Năm nhóm trên một hàng như Figma; nhóm dị ứng có thêm chiều rộng.
        GridPane facts = new GridPane();
        facts.setHgap(16);
        facts.setVgap(8);
        for (double width : new double[] {18, 25, 14, 16, 27}) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(width);
            column.setFillWidth(true);
            facts.getColumnConstraints().add(column);
        }
        facts.add(field("Chủ nuôi", wrapped(text(selected_pet.getTenChuNuoi()))), 0, 0);
        facts.add(field("Loài · Giống · Giới tính", wrapped(
                text(selected_pet.getLoaiThuCung()) + " · "
                + text(selected_pet.getGiongLoai()) + " · "
                + text(selected_pet.getGioiTinh()))), 1, 0);
        facts.add(field("Ngày sinh", wrapped(selected_pet.getNgaySinh() == null
                ? "Chưa ghi nhận" : selected_pet.getNgaySinh().format(date_format))), 2, 0);
        facts.add(field("Cân nặng hiện tại", wrapped(
                decimal(selected_profile.canNang()) + " kg")), 3, 0);
        facts.add(field("Dị ứng đã ghi nhận", wrapped(text(selected_profile.diUng()))), 4, 0);
        for (Node node : facts.getChildren()) {
            GridPane.setValignment(node, javafx.geometry.VPos.TOP);
        }
        profile_card.getChildren().setAll(header, new Separator(), facts);
        profile_card.setVisible(true);
        profile_card.setManaged(true);
    }

    /** Mở form cập nhật riêng cân nặng và dị ứng hiện tại. */
    private void showProfileForm() {
        if (busy.get() || selected_profile == null || !selected_profile.dangTheoDoi()) return;
        String pet_id = selected_pet.getMaThuCung();
        Dialog<Void> dialog = dialog("Cập nhật thông tin chăm sóc", 520);
        TextField txtWeight = new TextField(decimal(selected_profile.canNang()));
        TextField txtAllergy = new TextField(selected_profile.diUng() == null ? "" : selected_profile.diUng());
        Label lblError = new Label();
        lblError.getStyleClass().add("care-error");
        setMessage(lblError, "");
        Label lblHint = wrapped("Dị ứng để trống là chưa ghi nhận, không có nghĩa là không dị ứng. Cập nhật hồ sơ hiện tại không thay đổi cân nặng và đơn giá của các lần chăm sóc trước.");
        lblHint.getStyleClass().add("pet-muted");
        VBox form = new VBox(12, title("Cập nhật thông tin chăm sóc"), new Separator(),
                new Label(selected_pet.getTenThuCung() + " · " + pet_id),
                field("Cân nặng (kg) *", txtWeight),
                field("Dị ứng", txtAllergy), lblHint, lblError);
        setDialogContent(dialog, form, 310);
        ButtonType cancel_type = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType save_type = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(cancel_type, save_type);
        Button btnSave = (Button) dialog.getDialogPane().lookupButton(save_type);
        Button btnCancel = (Button) dialog.getDialogPane().lookupButton(cancel_type);
        btnSave.getStyleClass().add("pet-primary");
        btnCancel.getStyleClass().add("pet-secondary");
        btnSave.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            event.consume();
            String weight = txtWeight.getText();
            String allergy = txtAllergy.getText();
            form.setDisable(true);
            btnSave.setDisable(true);
            btnCancel.setDisable(true);
            setMessage(lblError, "");
            runBackground(() -> {
                care_controller.updateProfile(pet_id, weight, allergy);
                return true;
            }, ignored -> { dialog.close(); loadHistory(); }, error -> {
                form.setDisable(false);
                btnSave.setDisable(false);
                btnCancel.setDisable(false);
                setMessage(lblError, error.getMessage());
            });
        });
        showModal(dialog);
    }

    /** Tải các dịch vụ của dòng lịch hẹn được chọn rồi mở chi tiết. */
    private void showDetails() {
        PetCareVisit visit = tblHistory.getSelectionModel().getSelectedItem();
        if (visit == null || busy.get()) return;
        runBackground(() -> care_controller.loadServices(visit.maLichHen(), visit.maThuCung()), items -> {
            Dialog<Void> dialog = dialog("Chi tiết lần chăm sóc", 720);
            GridPane info = new GridPane();
            info.setHgap(18);
            info.setVgap(14);
            for (int i = 0; i < 3; i++) {
                ColumnConstraints constraint = new ColumnConstraints();
                constraint.setPercentWidth(100.0 / 3);
                info.getColumnConstraints().add(constraint);
            }
            info.add(field("Mã lịch hẹn", wrapped(visit.maLichHen())), 0, 0);
            info.add(field("Ngày hẹn", new Label(visit.ngayHen().format(date_format))), 1, 0);
            info.add(field("Giờ hẹn", new Label(visit.gioHen().format(time_format))), 2, 0);
            info.add(field("Thú cưng", wrapped(text(visit.tenThuCung()) + " · " + visit.maThuCung())), 0, 1);
            info.add(field("Chủ nuôi", wrapped(text(visit.tenChuNuoi()))), 1, 1);
            info.add(field("Nhân viên phụ trách", wrapped(text(visit.tenNhanVien()))), 2, 1);
            info.add(field("Trạng thái", wrapped(visit.trangThai())), 0, 2);
            info.add(field("Ghi chú lịch hẹn", wrapped(text(visit.ghiChu()))), 1, 2, 2, 1);
            info.getStyleClass().add("care-detail-card");
            TableView<PetCareServiceItem> tblServices = new TableView<>();
            tblServices.getStyleClass().addAll("pet-table", "care-service-table");
            tblServices.setFixedCellSize(-1);
            tblServices.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            tblServices.getColumns().add(column("Tên dịch vụ", PetCareServiceItem::tenDichVu, 310));
            tblServices.getColumns().add(column("Cân nặng ghi nhận (kg)", item -> decimal(item.canNangThuCung()), 190));
            tblServices.getColumns().add(column("Đơn giá", item -> money(item.donGia()), 130));
            tblServices.setItems(FXCollections.observableArrayList(items));
            tblServices.setPlaceholder(new Label("Chưa có dịch vụ được ghi nhận cho lịch hẹn này"));
            tblServices.setPrefHeight(Math.min(280, Math.max(120, 48 + items.size() * 54)));
            Label hint = wrapped("Cân nặng và đơn giá được lưu tại lần sử dụng dịch vụ này, không thay bằng giá trị hiện tại. Đơn giá không phải tổng thanh toán thực tế.");
            hint.getStyleClass().add("pet-muted");
            VBox content = new VBox(16, title("Chi tiết lần chăm sóc"), new Separator(), info,
                    new Label("Dịch vụ đã sử dụng"), tblServices,
                    field("Ghi chú lịch hẹn", wrapped(text(visit.ghiChu()))), hint);
            setDialogContent(dialog, content, 520);
            ButtonType close_type = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().add(close_type);
            dialog.getDialogPane().lookupButton(close_type).getStyleClass().add("pet-secondary");
            showModal(dialog);
        }, this::showError);
    }

    /**
     * @param operation Công việc ở luồng nền.
     * @param on_success Xử lý kết quả trên luồng JavaFX.
     * @param on_failure Xử lý lỗi trên luồng JavaFX.
     * @param <T> Kiểu dữ liệu kết quả.
     */
    private <T> void runBackground(Supplier<T> operation, Consumer<T> on_success, Consumer<Throwable> on_failure) {
        if (busy.get()) return;
        busy.set(true);
        Task<T> task = new Task<>() {
            @Override protected T call() { return operation.get(); }
        };
        task.setOnSucceeded(event -> { busy.set(false); on_success.accept(task.getValue()); });
        task.setOnFailed(event -> { busy.set(false); on_failure.accept(task.getException()); });
        Thread worker = new Thread(task, "pet-care-operation");
        worker.setDaemon(true);
        worker.start();
    }

    /** @param name Tiêu đề. @param width Chiều rộng. @return Hộp thoại có style của module. */
    private Dialog<Void> dialog(String name, double width) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(name);
        dialog.initOwner(getScene().getWindow());
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.getDialogPane().getStylesheets().addAll(getScene().getStylesheets());
        dialog.getDialogPane().getStyleClass().addAll("pet-dialog", "care-dialog");
        dialog.getDialogPane().setPrefWidth(width);
        return dialog;
    }

    /** @param dialog Hộp thoại. @param content Nội dung. @param max_height Chiều cao tối đa. */
    private void setDialogContent(Dialog<?> dialog, VBox content, double max_height) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("pet-form-scroll");
        // Controls chưa áp dụng CSS ở thời điểm này; prefHeight() có thể trả về
        // chiều cao quá nhỏ. Cấp vùng hiển thị rõ ràng và chỉ cuộn khi cần.
        javafx.stage.Window owner = dialog.getOwner();
        javafx.geometry.Rectangle2D screen_bounds = javafx.stage.Screen
                .getScreensForRectangle(owner.getX(), owner.getY(),
                        owner.getWidth(), owner.getHeight())
                .stream().findFirst().orElse(javafx.stage.Screen.getPrimary())
                .getVisualBounds();
        double viewport_height = Math.min(max_height,
                Math.max(120, screen_bounds.getHeight() - 180));
        scroll.setPrefViewportHeight(viewport_height);
        scroll.setMinViewportHeight(viewport_height);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        dialog.getDialogPane().setContent(scroll);
    }

    /** @param dialog Hộp thoại cần mở với nền mờ. */
    private void showModal(Dialog<?> dialog) {
        Parent root = getScene().getRoot();
        Effect previous = root.getEffect();
        GaussianBlur blur = new GaussianBlur(6);
        blur.setInput(previous);
        root.setEffect(blur);
        try { dialog.showAndWait(); }
        finally { root.setEffect(previous); }
    }

    /** @param error Lỗi cần thông báo. */
    private void showError(Throwable error) {
        Alert alert = new Alert(Alert.AlertType.ERROR, text(error.getMessage()), ButtonType.OK);
        if (getScene() != null) alert.initOwner(getScene().getWindow());
        alert.setHeaderText("Không thực hiện được thao tác chăm sóc");
        alert.showAndWait();
    }

    /** @param picker Ô chọn ngày cần định dạng. */
    private void configureDatePicker(DatePicker picker) {
        picker.setEditable(false);
        picker.setPromptText("dd/MM/yyyy");
        picker.setPrefWidth(150);
        picker.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(LocalDate value) { return value == null ? "" : value.format(date_format); }
            @Override public LocalDate fromString(String value) { return value == null || value.isBlank() ? null : LocalDate.parse(value, date_format); }
        });
    }

    /** @param caption Chữ trên nút. @param style Class CSS. @return Nút thao tác. */
    private Button button(String caption, String style) {
        Button button = new Button(caption);
        button.getStyleClass().add(style);
        return button;
    }

    /** @param caption Tên trường. @param value Điều khiển chứa giá trị. @return Khối thông tin. */
    private VBox field(String caption, Node value) {
        Label label = new Label(caption);
        label.getStyleClass().add("care-field-label");
        VBox field = new VBox(5, label, value);
        field.setMinWidth(0);
        return field;
    }

    /** @param value Nội dung. @return Nhãn cho phép xuống dòng. */
    private Label wrapped(String value) {
        Label label = new Label(value);
        label.setWrapText(true);
        label.setMinHeight(Region.USE_PREF_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    /** @param value Tiêu đề hộp thoại. @return Nhãn tiêu đề. */
    private Label title(String value) {
        Label label = new Label(value);
        label.getStyleClass().add("pet-dialog-title");
        return label;
    }

    /** @param label Nhãn thông báo. @param value Nội dung rỗng để ẩn. */
    private void setMessage(Label label, String value) {
        label.setText(value);
        label.setVisible(value != null && !value.isBlank());
        label.setManaged(label.isVisible());
    }

    /**
     * @param caption Tên cột.
     * @param getter Hàm lấy giá trị.
     * @param width Chiều rộng ưu tiên.
     * @param <T> Kiểu dữ liệu của dòng.
     * @return Cột có nội dung xuống dòng.
     */
    private <T> TableColumn<T, String> column(String caption, Function<T, String> getter, double width) {
        TableColumn<T, String> column = new TableColumn<>(caption);
        column.setPrefWidth(width);
        column.setMinWidth(70);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(text(getter.apply(cell.getValue()))));
        column.setCellFactory(ignored -> new TableCell<>() {
            private final Label label = wrapped("");
            {
                label.getStyleClass().add("pet-cell-text");
                label.prefWidthProperty().bind(widthProperty().subtract(16));
            }
            @Override protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                setText(null);
                label.setText(empty ? "" : value);
                setGraphic(empty ? null : label);
            }
        });
        return column;
    }

    /** @param value Chuỗi đầu vào. @return Chuỗi tìm kiếm không dấu. */
    private static String normalize(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").replace('đ', 'd').replace('Đ', 'D')
                .toLowerCase(Locale.ROOT).trim();
    }

    /** @param value Nội dung có thể trống. @return Nội dung hiển thị. */
    private static String text(String value) { return value == null || value.isBlank() ? "Chưa ghi nhận" : value; }

    /** @param value Số thập phân. @return Số dùng dấu phẩy thập phân. */
    private static String decimal(BigDecimal value) { return value.stripTrailingZeros().toPlainString().replace('.', ','); }

    /** @param value Tiền đã lưu trong chi tiết. @return Số tiền định dạng Việt Nam. */
    private static String money(BigDecimal value) {
        java.text.NumberFormat format = java.text.NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        format.setMaximumFractionDigits(2);
        return format.format(value) + " đ";
    }

    /** Kết quả tải cùng hồ sơ hiện tại và lịch hẹn để cập nhật giao diện một lần. */
    private record CareData(PetCareProfile profile, List<PetCareVisit> visits) { }
}
