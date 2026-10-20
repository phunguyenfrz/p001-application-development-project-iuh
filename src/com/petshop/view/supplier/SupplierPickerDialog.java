package com.petshop.view.supplier;

import com.petshop.controller.SupplierController;
import com.petshop.model.Supplier;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Hộp thoại chọn Nhà cung cấp (SupplierPickerDialog).
 * Bàn giao phụ thuộc cho phân hệ Lập đơn hàng nhập (IMP_CREATE).
 * Chỉ hiển thị các nhà cung cấp đang hợp tác (trangThai = true) và hỗ trợ tìm kiếm nhanh.
 */
public class SupplierPickerDialog extends Dialog<Supplier> {

    private final SupplierController supplierController;
    private final ObservableList<Supplier> supplierList = FXCollections.observableArrayList();
    private final AtomicLong searchRequestCounter = new AtomicLong(0);
    private PauseTransition searchDebounce;

    private TextField txtSearch;
    private TableView<Supplier> tblSuppliers;
    private Label lblResultCount;
    private Button btnSelect;
    private Button btnCancel;

    public SupplierPickerDialog(Window owner) {
        this.supplierController = new SupplierController();

        setTitle("Chọn Nhà Cung Cấp - Happy Tails");
        setHeaderText(null);
        initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            initOwner(owner);
        }

        DialogPane pnlDialogPane = getDialogPane();
        pnlDialogPane.getStylesheets().add(
                getClass().getResource("/com/petshop/view/styles.css").toExternalForm()
        );
        pnlDialogPane.setPrefSize(760, 500);
        pnlDialogPane.setStyle("-fx-background-color: #f3f6f3;");

        pnlDialogPane.setContent(buildDialogContent());
        configureDialogButtons(pnlDialogPane);
        filterSuppliers("");
    }

    /**
     * Phương thức tiện ích tĩnh giúp màn hình Lập đơn hàng nhập (IMP_CREATE)
     * mở hộp thoại và nhận về nhà cung cấp được chọn.
     */
    public static Optional<Supplier> showAndPick(Window owner) {
        SupplierPickerDialog dlgPicker = new SupplierPickerDialog(owner);
        return dlgPicker.showAndWait();
    }

    private VBox buildDialogContent() {
        VBox pnlContainer = new VBox(14);
        pnlContainer.setPadding(new Insets(18, 20, 10, 20));

        VBox pnlHeader = new VBox(4);
        Label lblTitle = new Label("Chọn đối tác cung cấp hàng");
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");
        Label lblSubtitle = new Label("Chỉ hiển thị các nhà cung cấp đang hợp tác. Nhấp đúp vào dòng để chọn nhanh.");
        lblSubtitle.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #6e7c70;");
        pnlHeader.getChildren().addAll(lblTitle, lblSubtitle);

        HBox pnlToolbar = new HBox(10);
        pnlToolbar.setAlignment(Pos.CENTER_LEFT);

        searchDebounce = new PauseTransition(Duration.millis(150));
        searchDebounce.setOnFinished(e -> filterSuppliers(txtSearch.getText()));

        txtSearch = new TextField();
        txtSearch.setPromptText("🔍 Tìm theo mã NCC, tên nhà cung cấp, số điện thoại...");
        txtSearch.getStyleClass().add("search-field");
        txtSearch.setPrefWidth(380);
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> searchDebounce.playFromStart());

        HBox pnlSearchBox = new HBox(6, txtSearch);
        pnlSearchBox.getStyleClass().add("search-box");
        HBox.setHgrow(pnlSearchBox, Priority.ALWAYS);

        Button btnRefresh = new Button("🔄 Làm mới");
        btnRefresh.getStyleClass().add("btn-action-secondary");
        btnRefresh.setOnAction(e -> {
            txtSearch.clear();
            filterSuppliers("");
        });

        pnlToolbar.getChildren().addAll(pnlSearchBox, btnRefresh);

        VBox pnlTableCard = new VBox(10);
        pnlTableCard.setPadding(new Insets(12));
        pnlTableCard.getStyleClass().add("chart-panel");
        VBox.setVgrow(pnlTableCard, Priority.ALWAYS);

        tblSuppliers = createSupplierTable();
        VBox.setVgrow(tblSuppliers, Priority.ALWAYS);

        lblResultCount = new Label("Đang tải dữ liệu...");
        lblResultCount.setStyle("-fx-font-size: 12px; -fx-text-fill: #6e7c70;");

        pnlTableCard.getChildren().addAll(tblSuppliers, lblResultCount);

        pnlContainer.getChildren().addAll(pnlHeader, pnlToolbar, pnlTableCard);
        return pnlContainer;
    }

    private TableView<Supplier> createSupplierTable() {
        TableView<Supplier> tblView = new TableView<>(supplierList);
        tblView.setPlaceholder(new Label("Không tìm thấy nhà cung cấp nào đang hợp tác."));
        tblView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Supplier, String> colId = new TableColumn<>("MÃ NCC");
        colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMaNhaCungCap()));
        colId.setPrefWidth(150);
        colId.setMinWidth(140);

        TableColumn<Supplier, String> colName = new TableColumn<>("TÊN NHÀ CUNG CẤP");
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTenNhaCungCap()));
        colName.setPrefWidth(230);

        TableColumn<Supplier, String> colPhone = new TableColumn<>("SỐ ĐIỆN THOẠI");
        colPhone.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSoDienThoai()));
        colPhone.setPrefWidth(115);
        colPhone.setMinWidth(105);

        TableColumn<Supplier, String> colAddress = new TableColumn<>("ĐỊA CHỈ");
        colAddress.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDiaChi()));
        colAddress.setPrefWidth(190);

        tblView.getColumns().addAll(List.of(colId, colName, colPhone, colAddress));

        tblView.setRowFactory(tv -> {
            TableRow<Supplier> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    setResult(row.getItem());
                    close();
                }
            });
            return row;
        });

        return tblView;
    }

    private void configureDialogButtons(DialogPane pnlDialogPane) {
        ButtonType selectButtonType = new ButtonType("✔ Chọn nhà cung cấp", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Hủy (Esc)", ButtonBar.ButtonData.CANCEL_CLOSE);
        pnlDialogPane.getButtonTypes().addAll(selectButtonType, cancelButtonType);

        btnSelect = (Button) pnlDialogPane.lookupButton(selectButtonType);
        btnSelect.getStyleClass().add("btn-action-primary");
        btnSelect.setDisable(true);

        btnCancel = (Button) pnlDialogPane.lookupButton(cancelButtonType);
        btnCancel.getStyleClass().add("btn-action-secondary");

        tblSuppliers.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            btnSelect.setDisable(newSel == null);
        });

        pnlDialogPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                setResult(null);
                close();
                event.consume();
            }
        });

        setResultConverter(dialogButton -> {
            if (dialogButton == selectButtonType) {
                return tblSuppliers.getSelectionModel().getSelectedItem();
            }
            return null;
        });
    }

    public void filterSuppliers(String keyword) {
        final long requestId = searchRequestCounter.incrementAndGet();
        CompletableFuture.supplyAsync(() -> supplierController.searchSuppliers(keyword, true))
                .thenAccept(filtered -> Platform.runLater(() -> {
                    if (requestId != searchRequestCounter.get()) {
                        return;
                    }
                    supplierList.setAll(filtered);
                    lblResultCount.setText("Hiển thị " + supplierList.size() + " nhà cung cấp đang hợp tác");
                    if (!supplierList.isEmpty()) {
                        tblSuppliers.getSelectionModel().selectFirst();
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        lblResultCount.setText("Lỗi khi lọc nhà cung cấp: " + cause.getMessage());
                    });
                    return null;
                });
    }
}

