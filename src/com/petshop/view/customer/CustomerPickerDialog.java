package com.petshop.view.customer;

import com.petshop.controller.CustomerController;
import com.petshop.model.Customer;
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
 * Hộp thoại Chọn khách hàng (CustomerPickerDialog).
 * Hỗ trợ tìm kiếm nhanh theo mã, họ tên hoặc số điện thoại, chọn khách hàng hoặc khách vãng lai.
 *
 * Tuân thủ Definition of Done:
 * - Đặt tên điều khiển theo tiền tố: txt, tbl, lbl, btn.
 * - Các nút hiển thị tên đầy đủ (setMinWidth(Region.USE_PREF_SIZE)).
 */
public class CustomerPickerDialog extends Dialog<Customer> {

    private final CustomerController customerController = new CustomerController();
    private final ObservableList<Customer> masterCustomerList = FXCollections.observableArrayList();
    private final FilteredList<Customer> filteredCustomerList = new FilteredList<>(masterCustomerList, c -> true);

    // Điều khiển tuân thủ tiền tố
    private TextField txtSearch;
    private TableView<Customer> tblCustomers;
    private Label lblStatusCount;
    private Button btnSelect;
    private Button btnWalkIn;
    private Button btnCancel;

    public CustomerPickerDialog(Window owner) {
        setTitle("Chọn Khách Hàng - Happy Tails");
        setHeaderText(null);
        initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            initOwner(owner);
        }

        DialogPane pnlDialogPane = getDialogPane();
        pnlDialogPane.getStylesheets().add(
                getClass().getResource("/com/petshop/view/styles.css").toExternalForm()
        );
        pnlDialogPane.setPrefSize(680, 500);
        pnlDialogPane.setStyle("-fx-background-color: #f3f6f3;");

        pnlDialogPane.setContent(buildContent());
        configureButtons(pnlDialogPane);
        loadCustomers();

        Platform.runLater(() -> {
            if (txtSearch != null) {
                txtSearch.requestFocus();
            }
        });
    }

    public static Optional<Customer> showAndPick(Window owner) {
        CustomerPickerDialog dlg = new CustomerPickerDialog(owner);
        return dlg.showAndWait();
    }

    private VBox buildContent() {
        VBox pnlContainer = new VBox(12);
        pnlContainer.setPadding(new Insets(16, 20, 10, 20));

        // Header
        VBox pnlHeader = new VBox(4);
        Label lblTitle = new Label("Tra cứu và chọn thông tin khách hàng");
        lblTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");
        Label lblSubtitle = new Label("Nhập tên, số điện thoại hoặc mã để tìm nhanh. Nhấp đúp vào dòng để chọn.");
        lblSubtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #6e7c70;");
        pnlHeader.getChildren().addAll(lblTitle, lblSubtitle);

        // Search Bar
        HBox pnlSearch = new HBox(10);
        pnlSearch.setAlignment(Pos.CENTER_LEFT);
        pnlSearch.setPadding(new Insets(10, 14, 10, 14));
        pnlSearch.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 6px; -fx-background-radius: 6px;");

        Label lblSearchIcon = new Label("🔍");
        lblSearchIcon.setStyle("-fx-font-size: 13px; -fx-text-fill: #829485;");

        txtSearch = new TextField();
        txtSearch.setPromptText("Tìm kiếm theo tên khách hàng, số điện thoại hoặc mã KH...");
        txtSearch.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-font-size: 13px;");
        HBox.setHgrow(txtSearch, Priority.ALWAYS);

        PauseTransition debounce = new PauseTransition(Duration.millis(150));
        txtSearch.textProperty().addListener((obs, o, n) -> {
            debounce.setOnFinished(e -> applyFilter());
            debounce.playFromStart();
        });

        pnlSearch.getChildren().addAll(lblSearchIcon, txtSearch);

        // Table
        tblCustomers = new TableView<>();
        tblCustomers.setItems(filteredCustomerList);
        tblCustomers.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblCustomers.setPlaceholder(new Label("Không tìm thấy khách hàng phù hợp"));
        VBox.setVgrow(tblCustomers, Priority.ALWAYS);

        TableColumn<Customer, String> colId = new TableColumn<>("Mã KH");
        colId.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getMaKhachHang()));
        colId.setPrefWidth(120);
        colId.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-weight: bold;");

        TableColumn<Customer, String> colName = new TableColumn<>("Họ và tên");
        colName.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getHoTen()));
        colName.setPrefWidth(180);
        colName.setStyle("-fx-font-weight: 600;");

        TableColumn<Customer, String> colPhone = new TableColumn<>("Số điện thoại");
        colPhone.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getSoDienThoai()));
        colPhone.setPrefWidth(130);
        colPhone.setStyle("-fx-alignment: CENTER;");

        TableColumn<Customer, String> colPoints = new TableColumn<>("Điểm tích lũy");
        colPoints.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.valueOf(c.getValue().getDiemTichLuy())));
        colPoints.setPrefWidth(100);
        colPoints.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: #15803d;");

        tblCustomers.getColumns().addAll(List.of(colId, colName, colPhone, colPoints));

        tblCustomers.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                Customer selected = tblCustomers.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    setResult(selected);
                    close();
                }
            }
        });

        // Status bar
        lblStatusCount = new Label("Đang tải danh sách...");
        lblStatusCount.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #6e7c70;");

        pnlContainer.getChildren().addAll(pnlHeader, pnlSearch, tblCustomers, lblStatusCount);
        return pnlContainer;
    }

    private void configureButtons(DialogPane pnlDialogPane) {
        ButtonType btnTypeSelect = new ButtonType("Chọn khách hàng", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnTypeWalkIn = new ButtonType("Khách vãng lai", ButtonBar.ButtonData.OTHER);
        ButtonType btnTypeCancel = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);

        pnlDialogPane.getButtonTypes().addAll(btnTypeSelect, btnTypeWalkIn, btnTypeCancel);

        btnSelect = (Button) pnlDialogPane.lookupButton(btnTypeSelect);
        btnWalkIn = (Button) pnlDialogPane.lookupButton(btnTypeWalkIn);
        btnCancel = (Button) pnlDialogPane.lookupButton(btnTypeCancel);

        btnSelect.setMinWidth(Region.USE_PREF_SIZE);
        btnWalkIn.setMinWidth(Region.USE_PREF_SIZE);
        btnCancel.setMinWidth(Region.USE_PREF_SIZE);

        btnSelect.getStyleClass().addAll("customer-add");
        btnWalkIn.getStyleClass().addAll("customer-secondary");
        btnCancel.getStyleClass().addAll("customer-secondary");

        btnSelect.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            Customer selected = tblCustomers.getSelectionModel().getSelectedItem();
            if (selected == null) {
                event.consume();
                Alert alert = new Alert(Alert.AlertType.WARNING, "Vui lòng chọn một khách hàng từ danh sách.");
                alert.showAndWait();
            }
        });

        setResultConverter(dialogButton -> {
            if (dialogButton == btnTypeSelect) {
                return tblCustomers.getSelectionModel().getSelectedItem();
            }
            if (dialogButton == btnTypeWalkIn) {
                Customer walkIn = new Customer();
                walkIn.setMaKhachHang(null);
                walkIn.setHoTen("Khách vãng lai");
                walkIn.setSoDienThoai("");
                return walkIn;
            }
            return null;
        });
    }

    private void loadCustomers() {
        try {
            List<Customer> list = customerController.loadActiveCustomers();
            masterCustomerList.setAll(list);
            updateStatusText();
        } catch (Exception ex) {
            lblStatusCount.setText("Lỗi nạp danh sách: " + ex.getMessage());
        }
    }

    private void applyFilter() {
        String keyword = txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";
        filteredCustomerList.setPredicate(c -> {
            if (keyword.isEmpty()) return true;
            return (c.getMaKhachHang() != null && c.getMaKhachHang().toLowerCase().contains(keyword))
                    || (c.getHoTen() != null && c.getHoTen().toLowerCase().contains(keyword))
                    || (c.getSoDienThoai() != null && c.getSoDienThoai().contains(keyword));
        });
        updateStatusText();
    }

    private void updateStatusText() {
        lblStatusCount.setText(String.format("Hiển thị %d / %d khách hàng",
                filteredCustomerList.size(), masterCustomerList.size()));
    }
}

