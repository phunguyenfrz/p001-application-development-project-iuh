package com.petshop.view.product;

import com.petshop.controller.ProductController;
import com.petshop.model.Product;
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

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Hộp thoại chọn Sản phẩm kèm ô quét mã vạch (ProductPickerDialog).
 * Bàn giao phụ thuộc cho màn hình Lập hóa đơn (INV_CREATE - Bùi Minh Luân).
 *
 * Chức năng chính:
 * 1. Ô quét mã vạch chuyên dụng: Bắt sự kiện Enter khi máy quét đọc xong, tự động tìm và chọn sản phẩm ngay lập tức.
 * 2. Ô tìm kiếm tức thì: Gõ tên, mã SP hoặc mã vạch với cơ chế debounce 150ms.
 * 3. Bảng danh sách sản phẩm hiển thị giá bán và tình trạng tồn kho.
 * 4. Nhấp đúp vào hàng để chọn nhanh sản phẩm.
 */
public class ProductPickerDialog extends Dialog<Product> {

    private final ProductController productController;
    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private final AtomicLong searchCounter = new AtomicLong(0);
    private PauseTransition searchDebounce;
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0 đ");

    // Các điều khiển tuân thủ quy tắc đặt tên (btn, txt, lbl, tbl, cbo)
    private TextField txtBarcodeScanner;
    private TableView<Product> tblProducts;
    private Label lblResultCount;
    private Label lblScanStatus;
    private Button btnSelect;
    private Button btnCancel;

    public ProductPickerDialog(Window owner) {
        this.productController = new ProductController();

        setTitle("Chọn Sản Phẩm - Happy Tails");
        setHeaderText(null);
        initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            initOwner(owner);
        }

        DialogPane pnlDialogPane = getDialogPane();
        pnlDialogPane.getStylesheets().add(
                getClass().getResource("/com/petshop/view/styles.css").toExternalForm()
        );
        pnlDialogPane.setPrefSize(820, 540);
        pnlDialogPane.setStyle("-fx-background-color: #f3f6f3;");

        pnlDialogPane.setContent(buildDialogContent());
        configureDialogButtons(pnlDialogPane);

        // Nạp danh sách ban đầu
        filterProducts("");

        // Tự động focus vào ô tìm kiếm khi mở hộp thoại
        Platform.runLater(() -> {
            if (txtBarcodeScanner != null) {
                txtBarcodeScanner.requestFocus();
            }
        });
    }

    /**
     * Phương thức tiện ích tĩnh giúp màn Lập hóa đơn (INV_CREATE)
     * mở hộp thoại chọn sản phẩm trực quan.
     *
     * @param owner Cửa sổ cha
     * @return Optional chứa sản phẩm được chọn
     */
    public static Optional<Product> showAndPick(Window owner) {
        ProductPickerDialog dlgPicker = new ProductPickerDialog(owner);
        return dlgPicker.showAndWait();
    }

    /**
     * Tiện ích tra cứu nhanh sản phẩm qua mã vạch (không cần mở popup),
     * dành cho trường hợp cần dùng mã vạch.
     *
     * @param barcode Mã vạch từ máy quét
     * @return Optional chứa sản phẩm nếu tìm thấy
     */
    public static Optional<Product> lookupByBarcode(String barcode) {
        ProductController controller = new ProductController();
        return controller.getProductByBarcode(barcode);
    }

    private VBox buildDialogContent() {
        VBox pnlContainer = new VBox(14);
        pnlContainer.setPadding(new Insets(18, 20, 10, 20));

        // Tiêu đề & Hướng dẫn
        VBox pnlHeader = new VBox(4);
        Label lblTitle = new Label("Tra cứu & Chọn sản phẩm bán hàng");
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");
        Label lblSubtitle = new Label("Nhập tên hoặc mã sản phẩm để tìm kiếm. Nhấp đúp chuột để chọn nhanh.");
        lblSubtitle.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #6e7c70;");
        pnlHeader.getChildren().addAll(lblTitle, lblSubtitle);

        // Thanh công cụ tìm kiếm
        VBox pnlToolbarBox = new VBox(8);
        HBox pnlToolbar = new HBox(10);
        pnlToolbar.setAlignment(Pos.CENTER_LEFT);

        txtBarcodeScanner = new TextField();
        txtBarcodeScanner.setPromptText("🔍 Nhập tên hoặc mã sản phẩm cần tìm...");
        txtBarcodeScanner.setPrefHeight(40);
        txtBarcodeScanner.setStyle(
                "-fx-background-color: #ffffff; -fx-background-radius: 8px; " +
                "-fx-border-color: #2c6b50; -fx-border-radius: 8px; -fx-border-width: 1.5px; " +
                "-fx-font-size: 13.5px; -fx-padding: 0 14px;"
        );
        HBox.setHgrow(txtBarcodeScanner, Priority.ALWAYS);

        // Xử lý khi nhấn ENTER
        txtBarcodeScanner.setOnKeyPressed(this::handleBarcodeKeyPress);

        searchDebounce = new PauseTransition(Duration.millis(150));
        searchDebounce.setOnFinished(e -> filterProducts(txtBarcodeScanner.getText()));
        txtBarcodeScanner.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(oldVal)) {
                searchDebounce.playFromStart();
            }
        });

        Button btnClear = new Button("Xóa tìm kiếm");
        btnClear.setMinWidth(Region.USE_PREF_SIZE);
        btnClear.setPrefHeight(40);
        btnClear.setStyle(
                "-fx-background-color: #ffffff; -fx-border-color: #d1ded1; " +
                "-fx-border-radius: 8px; -fx-background-radius: 8px; -fx-cursor: hand;"
        );
        btnClear.setOnAction(e -> {
            txtBarcodeScanner.clear();
            lblScanStatus.setText("");
            txtBarcodeScanner.requestFocus();
        });

        pnlToolbar.getChildren().addAll(txtBarcodeScanner, btnClear);

        // Dòng trạng thái quét
        lblScanStatus = new Label();
        lblScanStatus.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #2c6b50;");

        pnlToolbarBox.getChildren().addAll(pnlToolbar, lblScanStatus);

        // Bảng danh sách sản phẩm
        tblProducts = buildProductsTable();
        VBox.setVgrow(tblProducts, Priority.ALWAYS);

        // Footer đếm số lượng kết quả
        lblResultCount = new Label("Đang tải danh sách sản phẩm...");
        lblResultCount.setStyle("-fx-font-size: 12px; -fx-text-fill: #738075; -fx-font-style: italic;");

        pnlContainer.getChildren().addAll(pnlHeader, pnlToolbarBox, tblProducts, lblResultCount);
        return pnlContainer;
    }

    private TableView<Product> buildProductsTable() {
        TableView<Product> table = new TableView<>();
        table.setItems(productList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(
                "-fx-background-color: #ffffff; -fx-background-radius: 10px; " +
                "-fx-border-color: #e5ebe5; -fx-border-radius: 10px;"
        );

        // 1. Mã sản phẩm
        TableColumn<Product, String> colId = new TableColumn<>("Mã sản phẩm");
        colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMaSanPham()));
        colId.setPrefWidth(125);
        colId.setStyle("-fx-alignment: CENTER-LEFT;");

        // 2. Tên sản phẩm
        TableColumn<Product, String> colName = new TableColumn<>("Tên sản phẩm");
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTenSanPham()));
        colName.setPrefWidth(240);

        // 3. Mã vạch
        TableColumn<Product, String> colBarcode = new TableColumn<>("Mã vạch");
        colBarcode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMaVach()));
        colBarcode.setPrefWidth(120);
        colBarcode.setStyle("-fx-alignment: CENTER;");

        // 4. Đơn vị tính
        TableColumn<Product, String> colUnit = new TableColumn<>("ĐVT");
        colUnit.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDonViTinh()));
        colUnit.setPrefWidth(60);
        colUnit.setStyle("-fx-alignment: CENTER;");

        // 5. Giá bán
        TableColumn<Product, String> colPrice = new TableColumn<>("Giá bán");
        colPrice.setCellValueFactory(data -> new SimpleStringProperty(currencyFormat.format(data.getValue().getGiaBan())));
        colPrice.setPrefWidth(100);
        colPrice.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");

        // 6. Tồn kho
        TableColumn<Product, String> colStock = new TableColumn<>("Tồn kho");
        colStock.setCellValueFactory(data -> {
            int ton = data.getValue().getSoLuongTon();
            int min = data.getValue().getNguongTonToiThieu();
            if (ton <= 0) {
                return new SimpleStringProperty("Hết hàng (0)");
            } else if (ton <= min) {
                return new SimpleStringProperty("Sắp hết (" + ton + ")");
            } else {
                return new SimpleStringProperty(String.valueOf(ton));
            }
        });
        colStock.setPrefWidth(95);
        colStock.setStyle("-fx-alignment: CENTER;");

        table.getColumns().addAll(List.of(colId, colName, colBarcode, colUnit, colPrice, colStock));

        // Nhấp đúp vào hàng để chọn ngay
        table.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty()) && event.getButton() == MouseButton.PRIMARY) {
                    confirmSelection(row.getItem());
                }
            });
            return row;
        });

        // Theo dõi chọn hàng để bật/tắt nút Chọn
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (btnSelect != null) {
                btnSelect.setDisable(newVal == null);
            }
        });

        return table;
    }

    private void handleBarcodeKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            String query = txtBarcodeScanner.getText() != null ? txtBarcodeScanner.getText().trim() : "";
            if (query.isEmpty()) {
                return;
            }

            // Thử tra cứu khớp mã vạch chính xác trước
            Optional<Product> directMatch = productController.getProductByBarcode(query);
            if (directMatch.isPresent()) {
                Product found = directMatch.get();
                lblScanStatus.setText("✓ Đã nhận diện mã vạch: " + found.getTenSanPham());
                confirmSelection(found);
                return;
            }

            // Nếu chỉ có 1 kết quả duy nhất trong bảng, tự động chọn luôn
            if (productList.size() == 1) {
                confirmSelection(productList.get(0));
            } else {
                lblScanStatus.setText("ℹ Tìm thấy " + productList.size() + " kết quả khớp với từ khóa.");
            }
        }
    }

    private void filterProducts(String keyword) {
        final long requestId = searchCounter.incrementAndGet();
        CompletableFuture.supplyAsync(() -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return productController.getActiveProducts();
            } else {
                return productController.searchProducts(keyword.trim(), true);
            }
        }).thenAccept(results -> Platform.runLater(() -> {
            if (requestId == searchCounter.get()) {
                productList.setAll(results);
                lblResultCount.setText("Tìm thấy " + results.size() + " sản phẩm đang kinh doanh.");
                if (!results.isEmpty()) {
                    tblProducts.getSelectionModel().selectFirst();
                }
            }
        }));
    }

    private void configureDialogButtons(DialogPane pnlDialogPane) {
        ButtonType btnTypeSelect = new ButtonType("Chọn sản phẩm", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnTypeCancel = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);

        pnlDialogPane.getButtonTypes().addAll(btnTypeSelect, btnTypeCancel);

        btnSelect = (Button) pnlDialogPane.lookupButton(btnTypeSelect);
        btnSelect.setMinWidth(Region.USE_PREF_SIZE);
        btnSelect.setStyle(
                "-fx-background-color: #2c6b50; -fx-text-fill: #ffffff; " +
                "-fx-font-weight: bold; -fx-background-radius: 8px; -fx-padding: 8px 20px; -fx-cursor: hand;"
        );
        btnSelect.setDisable(true);

        btnCancel = (Button) pnlDialogPane.lookupButton(btnTypeCancel);
        btnCancel.setMinWidth(Region.USE_PREF_SIZE);
        btnCancel.setStyle(
                "-fx-background-color: #ffffff; -fx-border-color: #d1ded1; " +
                "-fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 8px 18px; -fx-cursor: hand;"
        );

        setResultConverter(dialogButton -> {
            if (dialogButton == btnTypeSelect) {
                return tblProducts.getSelectionModel().getSelectedItem();
            }
            return null;
        });
    }

    private void confirmSelection(Product selectedProduct) {
        if (selectedProduct != null) {
            setResult(selectedProduct);
            close();
        }
    }
}
