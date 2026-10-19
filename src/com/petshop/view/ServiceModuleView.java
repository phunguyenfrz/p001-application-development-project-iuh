package com.petshop.view;

import com.petshop.dao.AssignedOperationsDao;
import com.petshop.dao.AssignedOperationsDao.LookupOption;
import com.petshop.dao.AssignedOperationsDao.ServicePriceRow;
import com.petshop.dao.AssignedOperationsDao.ServiceRow;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.beans.property.ReadOnlyStringWrapper;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Function;

/** Service catalog, service pricing, and search workflows. */
public class ServiceModuleView extends VBox {

    private final AssignedOperationsDao dao = new AssignedOperationsDao();
    private final String actionId;
    private final String actionName;
    private final TextField txtSearch = new TextField();
    private final Label lblFeedback = new Label();
    private final TableView<ServiceRow> tblServices = new TableView<>();
    private final TableView<ServicePriceRow> tblPrices = new TableView<>();

    public ServiceModuleView(String actionId, String actionName) {
        this.actionId = actionId;
        this.actionName = actionName;
        setSpacing(16);
        setPadding(new Insets(24, 28, 28, 28));
        setStyle("-fx-background-color: #f3f6f3;");
        getChildren().add(createHeader());
        if ("SRV_PRICING".equals(actionId)) {
            buildPricingView();
        } else {
            buildServiceView();
        }
    }

    private VBox createHeader() {
        Label lblTitle = new Label("Dịch vụ chăm sóc  /  " + actionName);
        lblTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");
        Label lblSubtitle = new Label("Quản lý dịch vụ, trạng thái cung cấp và giá theo kích thước thú cưng.");
        lblSubtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #6e7c70;");
        return new VBox(4, lblTitle, lblSubtitle);
    }

    private void buildServiceView() {
        tblServices.getColumns().addAll(
                column("Mã dịch vụ", 180, ServiceRow::id),
                column("Tên dịch vụ", 190, ServiceRow::name),
                column("Mô tả", 260, row -> value(row.description())),
                column("Thời lượng (phút)", 135, row -> Integer.toString(row.duration())),
                column("Trạng thái", 130, row -> row.active() ? "Đang cung cấp" : "Tạm ngừng"));
        tblServices.setPlaceholder(new Label("Không có dịch vụ phù hợp."));
        VBox.setVgrow(tblServices, Priority.ALWAYS);

        Button btnSearch = button("Tìm kiếm", false);
        btnSearch.setOnAction(event -> loadServices());
        txtSearch.setPromptText("Tên hoặc mã dịch vụ");
        txtSearch.getStyleClass().add("search-field");
        txtSearch.setPrefWidth(260);
        txtSearch.setOnAction(event -> loadServices());

        Button btnRefresh = button("Làm mới", false);
        btnRefresh.setOnAction(event -> {
            txtSearch.clear();
            loadServices();
        });

        HBox toolbar = new HBox(8, txtSearch, btnSearch, btnRefresh);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        if (!"SRV_SEARCH".equals(actionId)) {
            Button btnAddService = button("Thêm dịch vụ", true);
            btnAddService.setOnAction(event -> editService(null));
            Button btnEditService = button("Sửa", false);
            Button btnDeactivateService = button("Ngừng cung cấp", false);
            btnEditService.setDisable(true);
            btnDeactivateService.setDisable(true);
            tblServices.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selected) -> {
                btnEditService.setDisable(selected == null);
                btnDeactivateService.setDisable(selected == null || !selected.active());
            });
            btnEditService.setOnAction(event -> editService(tblServices.getSelectionModel().getSelectedItem()));
            btnDeactivateService.setOnAction(event -> deactivateSelectedService());
            toolbar.getChildren().addAll(spacer, btnAddService, btnEditService, btnDeactivateService);
        }

        getChildren().add(createCard(toolbar, lblFeedback, tblServices));
        loadServices();
    }

    private void buildPricingView() {
        tblPrices.getColumns().addAll(
                column("Mã bảng giá", 170, ServicePriceRow::id),
                column("Dịch vụ", 210, ServicePriceRow::serviceName),
                column("Bậc cân nặng", 120, ServicePriceRow::size),
                column("Giá", 130, row -> money(row.price())),
                column("Ngày áp dụng", 125, row -> row.effectiveDate().toString()),
                column("Ngày hết hạn", 125, row -> row.expiryDate() == null ? "Không thời hạn" : row.expiryDate().toString()));
        tblPrices.setPlaceholder(new Label("Chưa có bảng giá phù hợp."));
        VBox.setVgrow(tblPrices, Priority.ALWAYS);

        txtSearch.setPromptText("Tên hoặc mã dịch vụ");
        txtSearch.getStyleClass().add("search-field");
        txtSearch.setPrefWidth(260);
        txtSearch.setOnAction(event -> loadPrices());
        Button btnSearch = button("Tìm kiếm", false);
        btnSearch.setOnAction(event -> loadPrices());
        Button btnRefresh = button("Làm mới", false);
        btnRefresh.setOnAction(event -> {
            txtSearch.clear();
            loadPrices();
        });
        Button btnAddPrice = button("Thêm mức giá", true);
        btnAddPrice.setOnAction(event -> addPrice());
        HBox toolbar = new HBox(8, txtSearch, btnSearch, btnRefresh);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        toolbar.getChildren().addAll(spacer, btnAddPrice);

        getChildren().add(createCard(toolbar, lblFeedback, tblPrices));
        loadPrices();
    }

    private void loadServices() {
        try {
            tblServices.setItems(FXCollections.observableArrayList(dao.findServices(txtSearch.getText())));
            lblFeedback.setText("Hiển thị " + tblServices.getItems().size() + " dịch vụ.");
        } catch (RuntimeException exception) {
            tblServices.getItems().clear();
            lblFeedback.setText(exception.getMessage());
        }
    }

    private void loadPrices() {
        try {
            tblPrices.setItems(FXCollections.observableArrayList(dao.findServicePrices(txtSearch.getText())));
            lblFeedback.setText("Hiển thị " + tblPrices.getItems().size() + " mức giá.");
        } catch (RuntimeException exception) {
            tblPrices.getItems().clear();
            lblFeedback.setText(exception.getMessage());
        }
    }

    private void editService(ServiceRow current) {
        Dialog<ServiceForm> dlgService = new Dialog<>();
        dlgService.setTitle(current == null ? "Thêm dịch vụ" : "Cập nhật dịch vụ");
        ButtonType btnTypeSave = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dlgService.getDialogPane().getButtonTypes().addAll(btnTypeSave, ButtonType.CANCEL);

        TextField txtName = new TextField(current == null ? "" : current.name());
        txtName.setPromptText("Tên dịch vụ");
        TextArea txtDescription = new TextArea(current == null ? "" : value(current.description()));
        txtDescription.setPromptText("Mô tả");
        txtDescription.setPrefRowCount(3);
        TextField txtDuration = new TextField(current == null ? "30" : Integer.toString(current.duration()));
        txtDuration.setTextFormatter(new TextFormatter<String>(change ->
            change.getControlNewText().matches("\\d{0,3}") ? change : null));
        CheckBox chkActive = new CheckBox("Đang cung cấp");
        chkActive.setSelected(current == null || current.active());

        GridPane gridFields = new GridPane();
        gridFields.setHgap(12);
        gridFields.setVgap(10);
        gridFields.addRow(0, new Label("Tên dịch vụ"), txtName);
        gridFields.addRow(1, new Label("Mô tả"), txtDescription);
        gridFields.addRow(2, new Label("Thời lượng (phút)"), txtDuration);
        gridFields.add(chkActive, 1, 3);
        dlgService.getDialogPane().setContent(gridFields);
        Node btnSave = dlgService.getDialogPane().lookupButton(btnTypeSave);
        btnSave.addEventFilter(ActionEvent.ACTION, event -> {
            if (txtName.getText().isBlank() || txtName.getText().length() > 100
                    || txtDescription.getText().length() > 255 || txtDuration.getText().isBlank()
                    || Integer.parseInt(txtDuration.getText()) < 1) {
                showAlert(Alert.AlertType.WARNING, "Kiểm tra dữ liệu", "Tên, mô tả hoặc thời lượng chưa hợp lệ.");
                event.consume();
            }
        });
        dlgService.setResultConverter(btnType -> btnType == btnTypeSave
            ? new ServiceForm(txtName.getText(), txtDescription.getText(), Integer.parseInt(txtDuration.getText()), chkActive.isSelected())
                : null);
        Optional<ServiceForm> result = dlgService.showAndWait();
        result.ifPresent(form -> runSaveService(current, form));
    }

    private void runSaveService(ServiceRow current, ServiceForm form) {
        try {
            dao.saveService(current == null ? null : current.id(), form.name(), form.description(), form.duration(), form.active());
            loadServices();
        } catch (RuntimeException exception) {
            showAlert(Alert.AlertType.ERROR, "Không lưu được dịch vụ", exception.getMessage());
        }
    }

    private void deactivateSelectedService() {
        ServiceRow selected = tblServices.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Dịch vụ sẽ được chuyển sang trạng thái tạm ngừng, không bị xóa khỏi dữ liệu.",
                ButtonType.CANCEL, ButtonType.OK);
        confirm.setHeaderText("Ngừng cung cấp dịch vụ " + selected.name() + "?");
        if (confirm.showAndWait().filter(ButtonType.OK::equals).isEmpty()) {
            return;
        }
        try {
            dao.deactivateService(selected.id());
            loadServices();
        } catch (RuntimeException exception) {
            showAlert(Alert.AlertType.ERROR, "Không cập nhật được", exception.getMessage());
        }
    }

    private void addPrice() {
        Dialog<PriceForm> dlgPrice = new Dialog<>();
        dlgPrice.setTitle("Thêm giá dịch vụ");
        ButtonType btnTypeSave = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dlgPrice.getDialogPane().getButtonTypes().addAll(btnTypeSave, ButtonType.CANCEL);

        ComboBox<LookupOption> cboService = new ComboBox<>();
        ComboBox<String> cboSize = new ComboBox<>(FXCollections.observableArrayList("Nhỏ", "Vừa", "Lớn"));
        cboSize.getSelectionModel().selectFirst();
        TextField txtPrice = new TextField();
        txtPrice.setPromptText("Ví dụ: 80000");
        txtPrice.setTextFormatter(new TextFormatter<String>(change ->
            change.getControlNewText().matches("\\d{0,16}(\\.\\d{0,2})?") ? change : null));
        DatePicker dtpEffective = new DatePicker(LocalDate.now());
        DatePicker dtpExpiry = new DatePicker();
        try {
            cboService.setItems(FXCollections.observableArrayList(dao.findServiceOptions()));
        } catch (RuntimeException exception) {
            showAlert(Alert.AlertType.ERROR, "Không tải được dịch vụ", exception.getMessage());
            return;
        }
        cboService.getSelectionModel().selectFirst();

        GridPane gridFields = new GridPane();
        gridFields.setHgap(12);
        gridFields.setVgap(10);
        gridFields.addRow(0, new Label("Dịch vụ"), cboService);
        gridFields.addRow(1, new Label("Bậc cân nặng"), cboSize);
        gridFields.addRow(2, new Label("Giá (VND)"), txtPrice);
        gridFields.addRow(3, new Label("Ngày áp dụng"), dtpEffective);
        gridFields.addRow(4, new Label("Ngày hết hạn"), dtpExpiry);
        dlgPrice.getDialogPane().setContent(gridFields);
        Node btnSave = dlgPrice.getDialogPane().lookupButton(btnTypeSave);
        btnSave.addEventFilter(ActionEvent.ACTION, event -> {
            boolean invalidPrice = txtPrice.getText().isBlank() || Double.parseDouble(txtPrice.getText()) < 0;
            boolean invalidExpiry = dtpExpiry.getValue() != null && dtpExpiry.getValue().isBefore(dtpEffective.getValue());
            if (cboService.getValue() == null || dtpEffective.getValue() == null || invalidPrice || invalidExpiry) {
                showAlert(Alert.AlertType.WARNING, "Kiểm tra dữ liệu", "Chọn dịch vụ, giá và khoảng hiệu lực hợp lệ.");
                event.consume();
            }
        });
        dlgPrice.setResultConverter(btnType -> btnType == btnTypeSave
            ? new PriceForm(cboService.getValue(), cboSize.getValue(), Double.parseDouble(txtPrice.getText()),
                dtpEffective.getValue(), dtpExpiry.getValue())
                : null);
        dlgPrice.showAndWait().ifPresent(form -> {
            try {
                dao.saveServicePrice(form.service().id(), form.size(), form.price(), form.effectiveDate(), form.expiryDate());
                loadPrices();
            } catch (RuntimeException exception) {
                showAlert(Alert.AlertType.ERROR, "Không lưu được giá", exception.getMessage());
            }
        });
    }

    private static <T> TableColumn<T, String> column(String title, double width, Function<T, String> value) {
        TableColumn<T, String> tblColumn = new TableColumn<>(title);
        tblColumn.setPrefWidth(width);
        tblColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return tblColumn;
    }

    private static VBox createCard(Node... content) {
        VBox card = new VBox(12, content);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #e5ebe5; -fx-border-radius: 8px;");
        VBox.setVgrow(card, Priority.ALWAYS);
        return card;
    }

    private static Button button(String text, boolean primary) {
        Button btn = new Button(text);
        btn.getStyleClass().add(primary ? "btn-action-primary" : "btn-action-secondary");
        return btn;
    }

    private static String money(double amount) {
        return String.format("%,.0f đ", amount);
    }

    private static String value(String text) {
        return text == null ? "" : text;
    }

    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message == null ? "Có lỗi không xác định." : message);
        alert.showAndWait();
    }

    private record ServiceForm(String name, String description, int duration, boolean active) {}

    private record PriceForm(LookupOption service, String size, double price,
                             LocalDate effectiveDate, LocalDate expiryDate) {}
}
