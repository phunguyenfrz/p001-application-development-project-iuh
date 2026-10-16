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

public class ServiceModuleView extends VBox {

    private final AssignedOperationsDao dao = new AssignedOperationsDao();
    private final String actionId;
    private final String actionName;
    private final TextField searchField = new TextField();
    private final Label feedback = new Label();
    private final TableView<ServiceRow> serviceTable = new TableView<>();
    private final TableView<ServicePriceRow> priceTable = new TableView<>();

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
        Label title = new Label("Dịch vụ chăm sóc  /  " + actionName);
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");
        Label subtitle = new Label("Quản lý dịch vụ, trạng thái cung cấp và giá theo kích thước thú cưng.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #6e7c70;");
        return new VBox(4, title, subtitle);
    }

    private void buildServiceView() {
        serviceTable.getColumns().addAll(
                column("Mã dịch vụ", 180, ServiceRow::id),
                column("Tên dịch vụ", 190, ServiceRow::name),
                column("Mô tả", 260, row -> value(row.description())),
                column("Thời lượng (phút)", 135, row -> Integer.toString(row.duration())),
                column("Trạng thái", 130, row -> row.active() ? "Đang cung cấp" : "Tạm ngừng"));
        serviceTable.setPlaceholder(new Label("Không có dịch vụ phù hợp."));
        VBox.setVgrow(serviceTable, Priority.ALWAYS);

        Button search = button("Tìm kiếm", false);
        search.setOnAction(event -> loadServices());
        searchField.setPromptText("Tên hoặc mã dịch vụ");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(260);
        searchField.setOnAction(event -> loadServices());

        Button refresh = button("Làm mới", false);
        refresh.setOnAction(event -> {
            searchField.clear();
            loadServices();
        });

        HBox toolbar = new HBox(8, searchField, search, refresh);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        if (!"SRV_SEARCH".equals(actionId)) {
            Button add = button("Thêm dịch vụ", true);
            add.setOnAction(event -> editService(null));
            Button edit = button("Sửa", false);
            Button deactivate = button("Ngừng cung cấp", false);
            edit.setDisable(true);
            deactivate.setDisable(true);
            serviceTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selected) -> {
                edit.setDisable(selected == null);
                deactivate.setDisable(selected == null || !selected.active());
            });
            edit.setOnAction(event -> editService(serviceTable.getSelectionModel().getSelectedItem()));
            deactivate.setOnAction(event -> deactivateSelectedService());
            toolbar.getChildren().addAll(spacer, add, edit, deactivate);
        }

        getChildren().add(createCard(toolbar, feedback, serviceTable));
        loadServices();
    }

    private void buildPricingView() {
        priceTable.getColumns().addAll(
                column("Mã bảng giá", 170, ServicePriceRow::id),
                column("Dịch vụ", 210, ServicePriceRow::serviceName),
                column("Bậc cân nặng", 120, ServicePriceRow::size),
                column("Giá", 130, row -> money(row.price())),
                column("Ngày áp dụng", 125, row -> row.effectiveDate().toString()),
                column("Ngày hết hạn", 125, row -> row.expiryDate() == null ? "Không thời hạn" : row.expiryDate().toString()));
        priceTable.setPlaceholder(new Label("Chưa có bảng giá phù hợp."));
        VBox.setVgrow(priceTable, Priority.ALWAYS);

        searchField.setPromptText("Tên hoặc mã dịch vụ");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(260);
        searchField.setOnAction(event -> loadPrices());
        Button search = button("Tìm kiếm", false);
        search.setOnAction(event -> loadPrices());
        Button refresh = button("Làm mới", false);
        refresh.setOnAction(event -> {
            searchField.clear();
            loadPrices();
        });
        Button add = button("Thêm mức giá", true);
        add.setOnAction(event -> addPrice());
        HBox toolbar = new HBox(8, searchField, search, refresh);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        toolbar.getChildren().addAll(spacer, add);

        getChildren().add(createCard(toolbar, feedback, priceTable));
        loadPrices();
    }

    private void loadServices() {
        try {
            serviceTable.setItems(FXCollections.observableArrayList(dao.findServices(searchField.getText())));
            feedback.setText("Hiển thị " + serviceTable.getItems().size() + " dịch vụ.");
        } catch (RuntimeException exception) {
            serviceTable.getItems().clear();
            feedback.setText(exception.getMessage());
        }
    }

    private void loadPrices() {
        try {
            priceTable.setItems(FXCollections.observableArrayList(dao.findServicePrices(searchField.getText())));
            feedback.setText("Hiển thị " + priceTable.getItems().size() + " mức giá.");
        } catch (RuntimeException exception) {
            priceTable.getItems().clear();
            feedback.setText(exception.getMessage());
        }
    }

    private void editService(ServiceRow current) {
        Dialog<ServiceForm> dialog = new Dialog<>();
        dialog.setTitle(current == null ? "Thêm dịch vụ" : "Cập nhật dịch vụ");
        ButtonType save = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        TextField name = new TextField(current == null ? "" : current.name());
        name.setPromptText("Tên dịch vụ");
        TextArea description = new TextArea(current == null ? "" : value(current.description()));
        description.setPromptText("Mô tả");
        description.setPrefRowCount(3);
        TextField duration = new TextField(current == null ? "30" : Integer.toString(current.duration()));
        duration.setTextFormatter(new TextFormatter<String>(change ->
            change.getControlNewText().matches("\\d{0,3}") ? change : null));
        CheckBox active = new CheckBox("Đang cung cấp");
        active.setSelected(current == null || current.active());

        GridPane fields = new GridPane();
        fields.setHgap(12);
        fields.setVgap(10);
        fields.addRow(0, new Label("Tên dịch vụ"), name);
        fields.addRow(1, new Label("Mô tả"), description);
        fields.addRow(2, new Label("Thời lượng (phút)"), duration);
        fields.add(active, 1, 3);
        dialog.getDialogPane().setContent(fields);
        Node saveButton = dialog.getDialogPane().lookupButton(save);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (name.getText().isBlank() || name.getText().length() > 100
                    || description.getText().length() > 255 || duration.getText().isBlank()
                    || Integer.parseInt(duration.getText()) < 1) {
                showAlert(Alert.AlertType.WARNING, "Kiểm tra dữ liệu", "Tên, mô tả hoặc thời lượng chưa hợp lệ.");
                event.consume();
            }
        });
        dialog.setResultConverter(button -> button == save
                ? new ServiceForm(name.getText(), description.getText(), Integer.parseInt(duration.getText()), active.isSelected())
                : null);
        Optional<ServiceForm> result = dialog.showAndWait();
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
        ServiceRow selected = serviceTable.getSelectionModel().getSelectedItem();
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
        Dialog<PriceForm> dialog = new Dialog<>();
        dialog.setTitle("Thêm giá dịch vụ");
        ButtonType save = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        ComboBox<LookupOption> service = new ComboBox<>();
        ComboBox<String> size = new ComboBox<>(FXCollections.observableArrayList("Nhỏ", "Vừa", "Lớn"));
        size.getSelectionModel().selectFirst();
        TextField price = new TextField();
        price.setPromptText("Ví dụ: 80000");
        price.setTextFormatter(new TextFormatter<String>(change ->
            change.getControlNewText().matches("\\d{0,16}(\\.\\d{0,2})?") ? change : null));
        DatePicker effective = new DatePicker(LocalDate.now());
        DatePicker expiry = new DatePicker();
        try {
            service.setItems(FXCollections.observableArrayList(dao.findServiceOptions()));
        } catch (RuntimeException exception) {
            showAlert(Alert.AlertType.ERROR, "Không tải được dịch vụ", exception.getMessage());
            return;
        }
        service.getSelectionModel().selectFirst();

        GridPane fields = new GridPane();
        fields.setHgap(12);
        fields.setVgap(10);
        fields.addRow(0, new Label("Dịch vụ"), service);
        fields.addRow(1, new Label("Bậc cân nặng"), size);
        fields.addRow(2, new Label("Giá (VND)"), price);
        fields.addRow(3, new Label("Ngày áp dụng"), effective);
        fields.addRow(4, new Label("Ngày hết hạn"), expiry);
        dialog.getDialogPane().setContent(fields);
        Node saveButton = dialog.getDialogPane().lookupButton(save);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            boolean invalidPrice = price.getText().isBlank() || Double.parseDouble(price.getText()) < 0;
            boolean invalidExpiry = expiry.getValue() != null && expiry.getValue().isBefore(effective.getValue());
            if (service.getValue() == null || effective.getValue() == null || invalidPrice || invalidExpiry) {
                showAlert(Alert.AlertType.WARNING, "Kiểm tra dữ liệu", "Chọn dịch vụ, giá và khoảng hiệu lực hợp lệ.");
                event.consume();
            }
        });
        dialog.setResultConverter(button -> button == save
                ? new PriceForm(service.getValue(), size.getValue(), Double.parseDouble(price.getText()),
                        effective.getValue(), expiry.getValue())
                : null);
        dialog.showAndWait().ifPresent(form -> {
            try {
                dao.saveServicePrice(form.service().id(), form.size(), form.price(), form.effectiveDate(), form.expiryDate());
                loadPrices();
            } catch (RuntimeException exception) {
                showAlert(Alert.AlertType.ERROR, "Không lưu được giá", exception.getMessage());
            }
        });
    }

    private static <T> TableColumn<T, String> column(String title, double width, Function<T, String> value) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }

    private static VBox createCard(Node... content) {
        VBox card = new VBox(12, content);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #e5ebe5; -fx-border-radius: 8px;");
        VBox.setVgrow(card, Priority.ALWAYS);
        return card;
    }

    private static Button button(String text, boolean primary) {
        Button button = new Button(text);
        button.getStyleClass().add(primary ? "btn-action-primary" : "btn-action-secondary");
        return button;
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
