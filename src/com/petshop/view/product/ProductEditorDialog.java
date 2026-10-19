package com.petshop.view.product;

import com.petshop.controller.ProductController;
import com.petshop.controller.SupplierController;
import com.petshop.model.Product;
import com.petshop.model.Supplier;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * Hộp thoại Thêm mới và Chỉnh sửa sản phẩm (ProductEditorDialog).
 *
 * Tuân thủ tiêu chuẩn Definition of Done:
 * - Đặt tên điều khiển theo đúng tiền tố chuẩn: txt, cbo, lbl, btn, chk.
 * - Ô giá bán và số lượng tồn CHỈ CHO NHẬP SỐ (áp dụng TextFormatter).
 * - Kiểm tra dữ liệu hợp lệ (tên, mã vạch duy nhất, đơn vị tính) trước khi lưu.
 */
public class ProductEditorDialog extends Dialog<Product> {

    private final ProductController productController;
    private final SupplierController supplierController;
    private final Product editingProduct;
    private final boolean isCreateMode;

    // Các điều khiển tuân thủ tiền tố
    private TextField txtId;
    private TextField txtName;
    private TextField txtBarcode;
    private TextField txtPrice;
    private TextField txtStock;
    private TextField txtThreshold;
    private ComboBox<String> cboUnit;
    private ComboBox<Supplier> cboSupplier;
    private CheckBox chkActive;
    private Label lblError;
    private Button btnSave;
    private Button btnCancel;

    public ProductEditorDialog(Window owner, Product product) {
        this.productController = new ProductController();
        this.supplierController = new SupplierController();
        this.editingProduct = product;
        this.isCreateMode = (product == null);

        setTitle(isCreateMode ? "Thêm Sản Phẩm Mới - Happy Tails" : "Chỉnh Sửa Sản Phẩm - Happy Tails");
        setHeaderText(null);
        initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            initOwner(owner);
        }

        DialogPane pnlDialogPane = getDialogPane();
        pnlDialogPane.getStylesheets().add(
                getClass().getResource("/com/petshop/view/styles.css").toExternalForm()
        );
        pnlDialogPane.setPrefSize(600, 560);
        pnlDialogPane.setStyle("-fx-background-color: #f3f6f3;");

        pnlDialogPane.setContent(buildDialogContent());
        configureDialogButtons(pnlDialogPane);
        populateData();

        Platform.runLater(() -> {
            if (txtName != null) {
                txtName.requestFocus();
            }
        });
    }

    /**
     * Phương thức tiện ích tĩnh mở hộp thoại Thêm/Sửa sản phẩm.
     */
    public static Optional<Product> showAndEdit(Window owner, Product product) {
        ProductEditorDialog dlg = new ProductEditorDialog(owner, product);
        return dlg.showAndWait();
    }

    private VBox buildDialogContent() {
        VBox pnlContainer = new VBox(14);
        pnlContainer.setPadding(new Insets(16, 20, 10, 20));

        // Tiêu đề form
        VBox pnlHeader = new VBox(4);
        Label lblTitle = new Label(isCreateMode ? "Thêm sản phẩm mới vào danh mục" : "Cập nhật thông tin sản phẩm");
        lblTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");
        Label lblSubtitle = new Label("Vui lòng điền đầy đủ các trường bắt buộc (*). Giá bán và số lượng tồn chỉ cho phép nhập số.");
        lblSubtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #6e7c70;");
        pnlHeader.getChildren().addAll(lblTitle, lblSubtitle);

        // Khung form
        GridPane pnlGrid = new GridPane();
        pnlGrid.setHgap(14);
        pnlGrid.setVgap(12);
        pnlGrid.setStyle(
                "-fx-background-color: #ffffff; -fx-background-radius: 12px; " +
                "-fx-border-color: #e5ebe5; -fx-border-radius: 12px; -fx-padding: 18px;"
        );

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        pnlGrid.getColumnConstraints().addAll(col1, col2);

        // 1. Mã sản phẩm (readonly)
        VBox boxId = new VBox(4);
        Label lblId = new Label("Mã sản phẩm:");
        lblId.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #4b5a50;");
        txtId = new TextField();
        txtId.setEditable(false);
        txtId.setStyle("-fx-background-color: #f7faf7; -fx-border-color: #d8e2d8; -fx-border-radius: 6px; -fx-pref-height: 36px; -fx-font-weight: bold;");
        boxId.getChildren().addAll(lblId, txtId);

        // 2. Nhà cung cấp
        VBox boxSupplier = new VBox(4);
        Label lblSupplier = new Label("Nhà cung cấp đối tác:");
        lblSupplier.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #4b5a50;");
        cboSupplier = new ComboBox<>();
        cboSupplier.setMaxWidth(Double.MAX_VALUE);
        cboSupplier.setPrefHeight(36);
        cboSupplier.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 6px;");
        setupSupplierComboBox();
        boxSupplier.getChildren().addAll(lblSupplier, cboSupplier);

        pnlGrid.add(boxId, 0, 0);
        pnlGrid.add(boxSupplier, 1, 0);

        // 3. Tên sản phẩm
        VBox boxName = new VBox(4);
        Label lblName = new Label("Tên sản phẩm (*):");
        lblName.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #4b5a50;");
        txtName = new TextField();
        txtName.setPromptText("Ví dụ: Hạt Royal Canin Indoor 2kg");
        txtName.setPrefHeight(36);
        txtName.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 6px;");
        boxName.getChildren().addAll(lblName, txtName);
        pnlGrid.add(boxName, 0, 1, 2, 1);

        // 4. Mã vạch Barcode (Đã disable theo yêu cầu thực tế của nhóm)
        VBox boxBarcode = new VBox(4);
        Label lblBarcode = new Label("Mã vạch (Tự động):");
        lblBarcode.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #4b5a50;");
        txtBarcode = new TextField();
        txtBarcode.setDisable(true);
        txtBarcode.setEditable(false);
        txtBarcode.setPromptText("Hệ thống tự động tạo mã vạch");
        txtBarcode.setPrefHeight(36);
        txtBarcode.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #6b7280; -fx-border-color: #d1ded1; -fx-border-radius: 6px;");
        boxBarcode.getChildren().addAll(lblBarcode, txtBarcode);

        // 5. Đơn vị tính
        VBox boxUnit = new VBox(4);
        Label lblUnit = new Label("Đơn vị tính (*):");
        lblUnit.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #4b5a50;");
        cboUnit = new ComboBox<>(FXCollections.observableArrayList(
                "Gói", "Túi", "Hộp", "Chai", "Lon", "Cái", "Chiếc", "Bộ", "Kg"
        ));
        cboUnit.setEditable(true);
        cboUnit.setMaxWidth(Double.MAX_VALUE);
        cboUnit.setPrefHeight(36);
        cboUnit.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 6px;");
        boxUnit.getChildren().addAll(lblUnit, cboUnit);

        pnlGrid.add(boxBarcode, 0, 2);
        pnlGrid.add(boxUnit, 1, 2);

        // 6. Giá bán (CHỈ CHO NHẬP SỐ)
        VBox boxPrice = new VBox(4);
        Label lblPrice = new Label("Giá bán (VNĐ) (*):");
        lblPrice.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #4b5a50;");
        txtPrice = new TextField();
        txtPrice.setPromptText("Chỉ nhập số nguyên, ví dụ 150000");
        txtPrice.setPrefHeight(36);
        txtPrice.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 6px;");
        applyNumericFilter(txtPrice); // Bắt buộc chỉ cho nhập số
        boxPrice.getChildren().addAll(lblPrice, txtPrice);

        // 7. Số lượng tồn kho ban đầu (CHỈ CHO NHẬP SỐ)
        VBox boxStock = new VBox(4);
        Label lblStock = new Label("Số lượng tồn kho (*):");
        lblStock.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #4b5a50;");
        txtStock = new TextField();
        txtStock.setPromptText("Chỉ nhập số, ví dụ 50");
        txtStock.setPrefHeight(36);
        txtStock.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 6px;");
        applyNumericFilter(txtStock); // Bắt buộc chỉ cho nhập số
        boxStock.getChildren().addAll(lblStock, txtStock);

        pnlGrid.add(boxPrice, 0, 3);
        pnlGrid.add(boxStock, 1, 3);

        // 8. Ngưỡng tồn tối thiểu (CHỈ CHO NHẬP SỐ)
        VBox boxThreshold = new VBox(4);
        Label lblThreshold = new Label("Ngưỡng cảnh báo tồn tối thiểu:");
        lblThreshold.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #4b5a50;");
        txtThreshold = new TextField();
        txtThreshold.setPromptText("Mặc định: 10");
        txtThreshold.setPrefHeight(36);
        txtThreshold.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 6px;");
        applyNumericFilter(txtThreshold); // Bắt buộc chỉ cho nhập số
        boxThreshold.getChildren().addAll(lblThreshold, txtThreshold);

        // 9. Trạng thái kinh doanh
        VBox boxStatus = new VBox(10);
        Label lblStatus = new Label("Tình trạng kinh doanh:");
        lblStatus.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #4b5a50;");
        chkActive = new CheckBox("Đang kinh doanh (bán tại cửa hàng)");
        chkActive.setSelected(true);
        chkActive.setStyle("-fx-font-size: 12.5px; -fx-font-weight: 600; -fx-text-fill: #2c6b50;");
        boxStatus.getChildren().addAll(lblStatus, chkActive);

        pnlGrid.add(boxThreshold, 0, 4);
        pnlGrid.add(boxStatus, 1, 4);

        // Thông báo lỗi
        lblError = new Label();
        lblError.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px; -fx-font-weight: 600;");
        lblError.setWrapText(true);

        pnlContainer.getChildren().addAll(pnlHeader, pnlGrid, lblError);
        return pnlContainer;
    }

    /**
     * Quy tắc Definition of Done: Ô giá bán và số lượng tồn chỉ cho nhập số.
     */
    private void applyNumericFilter(TextField textField) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change;
            }
            return null;
        };
        textField.setTextFormatter(new TextFormatter<>(filter));
    }

    private void setupSupplierComboBox() {
        try {
            List<Supplier> suppliers = supplierController.loadActiveSuppliers();
            cboSupplier.setItems(FXCollections.observableArrayList(suppliers));
            cboSupplier.setConverter(new StringConverter<>() {
                @Override
                public String toString(Supplier s) {
                    return s != null ? s.getTenNhaCungCap() + " (" + s.getMaNhaCungCap() + ")" : "";
                }

                @Override
                public Supplier fromString(String string) {
                    return null;
                }
            });
        } catch (Exception ignored) {
        }
    }

    private void populateData() {
        if (isCreateMode) {
            txtId.setText(productController.generateNextProductId());
            txtBarcode.setText("893" + (System.currentTimeMillis() % 10000000000L));
            txtThreshold.setText("10");
            txtStock.setText("0");
            cboUnit.setValue("Gói");
            chkActive.setSelected(true);
        } else {
            txtId.setText(editingProduct.getMaSanPham());
            txtName.setText(editingProduct.getTenSanPham());
            txtBarcode.setText(editingProduct.getMaVach());
            txtPrice.setText(String.valueOf((long) editingProduct.getGiaBan()));
            txtStock.setText(String.valueOf(editingProduct.getSoLuongTon()));
            txtThreshold.setText(String.valueOf(editingProduct.getNguongTonToiThieu()));
            cboUnit.setValue(editingProduct.getDonViTinh());
            chkActive.setSelected(editingProduct.isTrangThai());

            // Chọn nhà cung cấp tương ứng
            if (editingProduct.getMaNhaCungCap() != null) {
                for (Supplier s : cboSupplier.getItems()) {
                    if (s.getMaNhaCungCap().equals(editingProduct.getMaNhaCungCap())) {
                        cboSupplier.setValue(s);
                        break;
                    }
                }
            }
        }
    }

    private void configureDialogButtons(DialogPane pnlDialogPane) {
        ButtonType btnTypeSave = new ButtonType(isCreateMode ? "Thêm sản phẩm" : "Lưu thay đổi", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnTypeCancel = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);

        pnlDialogPane.getButtonTypes().addAll(btnTypeSave, btnTypeCancel);

        btnSave = (Button) pnlDialogPane.lookupButton(btnTypeSave);
        btnSave.setMinWidth(Region.USE_PREF_SIZE);
        btnSave.setStyle(
                "-fx-background-color: #2c6b50; -fx-text-fill: #ffffff; " +
                "-fx-font-weight: bold; -fx-background-radius: 8px; -fx-padding: 8px 20px; -fx-cursor: hand;"
        );

        btnCancel = (Button) pnlDialogPane.lookupButton(btnTypeCancel);
        btnCancel.setMinWidth(Region.USE_PREF_SIZE);
        btnCancel.setStyle(
                "-fx-background-color: #ffffff; -fx-border-color: #d1ded1; " +
                "-fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 8px 18px; -fx-cursor: hand;"
        );

        // Chặn đóng popup nếu dữ liệu không hợp lệ
        btnSave.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!validateAndSave()) {
                event.consume();
            }
        });

        setResultConverter(dialogButton -> {
            if (dialogButton == btnTypeSave) {
                return buildProductFromFields();
            }
            return null;
        });
    }

    private boolean validateAndSave() {
        lblError.setText("");

        String name = txtName.getText() != null ? txtName.getText().trim() : "";
        if (name.isEmpty()) {
            lblError.setText("⚠ Tên sản phẩm không được để trống.");
            txtName.requestFocus();
            return false;
        }

        String barcode = txtBarcode.getText() != null ? txtBarcode.getText().trim() : "";
        if (barcode.isEmpty()) {
            barcode = "893" + (System.currentTimeMillis() % 10000000000L);
            txtBarcode.setText(barcode);
        }

        String unit = cboUnit.getValue() != null ? cboUnit.getValue().trim() : "";
        if (unit.isEmpty()) {
            lblError.setText("⚠ Vui lòng chọn hoặc nhập đơn vị tính.");
            cboUnit.requestFocus();
            return false;
        }

        String priceStr = txtPrice.getText() != null ? txtPrice.getText().trim() : "";
        if (priceStr.isEmpty()) {
            lblError.setText("⚠ Giá bán sản phẩm không được để trống.");
            txtPrice.requestFocus();
            return false;
        }

        String stockStr = txtStock.getText() != null ? txtStock.getText().trim() : "";
        if (stockStr.isEmpty()) {
            lblError.setText("⚠ Số lượng tồn kho không được để trống.");
            txtStock.requestFocus();
            return false;
        }

        try {
            double price = Double.parseDouble(priceStr);
            int stock = Integer.parseInt(stockStr);
            int threshold = 10;
            String thresholdStr = txtThreshold.getText() != null ? txtThreshold.getText().trim() : "";
            if (!thresholdStr.isEmpty()) {
                threshold = Integer.parseInt(thresholdStr);
            }

            Product product = buildProductFromFields();
            product.setGiaBan(price);
            product.setSoLuongTon(stock);
            product.setNguongTonToiThieu(threshold);

            return productController.saveProduct(product, isCreateMode);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            lblError.setText("⚠ " + ex.getMessage());
            return false;
        } catch (Exception ex) {
            lblError.setText("⚠ Lỗi không xác định: " + ex.getMessage());
            return false;
        }
    }

    private Product buildProductFromFields() {
        String id = txtId.getText().trim();
        String name = txtName.getText().trim();
        String barcode = txtBarcode.getText().trim();
        String unit = cboUnit.getValue() != null ? cboUnit.getValue().trim() : "Gói";
        double price = txtPrice.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtPrice.getText().trim());
        int stock = txtStock.getText().trim().isEmpty() ? 0 : Integer.parseInt(txtStock.getText().trim());
        int threshold = txtThreshold.getText().trim().isEmpty() ? 10 : Integer.parseInt(txtThreshold.getText().trim());
        boolean active = chkActive.isSelected();

        Supplier supplier = cboSupplier.getValue();
        String supplierId = supplier != null ? supplier.getMaNhaCungCap() : null;

        Product p = new Product(id, supplierId, name, barcode, price, stock, threshold, unit, active);
        if (supplier != null) {
            p.setTenNhaCungCap(supplier.getTenNhaCungCap());
        }
        return p;
    }
}
