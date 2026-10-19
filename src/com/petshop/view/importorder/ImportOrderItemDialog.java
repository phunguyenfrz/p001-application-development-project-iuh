package com.petshop.view.importorder;

import com.petshop.model.ImportOrderItem;
import com.petshop.model.Product;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.text.DecimalFormat;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * Hộp thoại nhập số lượng và đơn giá nhập cho mặt hàng (ImportOrderItemDialog).
 * Tuân thủ Definition of Done:
 * - Tên lớp là danh từ.
 * - Ô số lượng và đơn giá chỉ cho nhập số (Number only).
 * - Tên điều khiển có tiền tố chuẩn (btn, txt, lbl).
 * - Các nút hiển thị đầy đủ tên, không bị co rút "...".
 */
public class ImportOrderItemDialog extends Dialog<ImportOrderItem> {

    private final Product product;
    private final ImportOrderItem existingItem;
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0 đ");

    private TextField txtProductName;
    private TextField txtUnit;
    private TextField txtQuantity;
    private TextField txtUnitPrice;
    private Label lblSubTotal;
    private Label lblError;

    private Button btnSave;
    private Button btnCancel;

    public ImportOrderItemDialog(Window owner, Product product, ImportOrderItem existingItem) {
        this.product = product;
        this.existingItem = existingItem;

        setTitle(existingItem == null ? "Nhập Số Lượng & Đơn Giá Nhập" : "Chỉnh Sửa Mặt Hàng Nhập");
        setHeaderText(null);
        initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            initOwner(owner);
        }

        DialogPane pnlDialogPane = getDialogPane();
        pnlDialogPane.getStylesheets().add(
                getClass().getResource("/com/petshop/view/styles.css").toExternalForm()
        );
        pnlDialogPane.setPrefWidth(480);
        pnlDialogPane.setStyle("-fx-background-color: #f6f5f1;");

        pnlDialogPane.setContent(buildDialogContent());
        configureDialogButtons(pnlDialogPane);

        populateData();

        Platform.runLater(() -> {
            if (txtQuantity != null) {
                txtQuantity.requestFocus();
                txtQuantity.selectAll();
            }
        });
    }

    public static Optional<ImportOrderItem> showAndInput(Window owner, Product product, ImportOrderItem existingItem) {
        ImportOrderItemDialog dlg = new ImportOrderItemDialog(owner, product, existingItem);
        return dlg.showAndWait();
    }

    private VBox buildDialogContent() {
        VBox pnlContainer = new VBox(14);
        pnlContainer.setPadding(new Insets(16, 18, 10, 18));

        Label lblTitle = new Label("Thông tin hàng hóa nhập kho");
        lblTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");

        GridPane pnlGrid = new GridPane();
        pnlGrid.setHgap(12);
        pnlGrid.setVgap(14);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(130);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        pnlGrid.getColumnConstraints().addAll(col1, col2);

        // 1. Tên sản phẩm
        Label lblName = new Label("Sản phẩm:");
        lblName.setStyle("-fx-font-weight: 600; -fx-text-fill: #4b5a50;");
        txtProductName = new TextField();
        txtProductName.setEditable(false);
        txtProductName.setStyle("-fx-background-color: #f0f2f0; -fx-border-color: #d8e2d8; -fx-border-radius: 6px; -fx-font-weight: bold; -fx-pref-height: 36px;");
        pnlGrid.add(lblName, 0, 0);
        pnlGrid.add(txtProductName, 1, 0);

        // 2. Đơn vị tính
        Label lblUnit = new Label("Đơn vị tính:");
        lblUnit.setStyle("-fx-font-weight: 600; -fx-text-fill: #4b5a50;");
        txtUnit = new TextField();
        txtUnit.setEditable(false);
        txtUnit.setStyle("-fx-background-color: #f0f2f0; -fx-border-color: #d8e2d8; -fx-border-radius: 6px; -fx-pref-height: 36px;");
        pnlGrid.add(lblUnit, 0, 1);
        pnlGrid.add(txtUnit, 1, 1);

        // 3. Số lượng nhập (Chỉ cho nhập số)
        Label lblQuantity = new Label("Số lượng nhập (*):");
        lblQuantity.setStyle("-fx-font-weight: 600; -fx-text-fill: #4b5a50;");
        txtQuantity = new TextField();
        txtQuantity.setPrefHeight(36);
        txtQuantity.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 6px; -fx-font-weight: bold;");
        applyNumericFilter(txtQuantity);
        txtQuantity.textProperty().addListener((obs, o, n) -> capNhatThanhTien());
        pnlGrid.add(lblQuantity, 0, 2);
        pnlGrid.add(txtQuantity, 1, 2);

        // 4. Đơn giá nhập (Chỉ cho nhập số)
        Label lblPrice = new Label("Đơn giá nhập (*):");
        lblPrice.setStyle("-fx-font-weight: 600; -fx-text-fill: #4b5a50;");
        txtUnitPrice = new TextField();
        txtUnitPrice.setPrefHeight(36);
        txtUnitPrice.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 6px; -fx-font-weight: bold;");
        applyNumericFilter(txtUnitPrice);
        txtUnitPrice.textProperty().addListener((obs, o, n) -> capNhatThanhTien());
        pnlGrid.add(lblPrice, 0, 3);
        pnlGrid.add(txtUnitPrice, 1, 3);

        // 5. Thành tiền tạm tính
        Label lblTotalTag = new Label("Thành tiền:");
        lblTotalTag.setStyle("-fx-font-weight: 600; -fx-text-fill: #4b5a50;");
        lblSubTotal = new Label("0 đ");
        lblSubTotal.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #ff7518;");
        pnlGrid.add(lblTotalTag, 0, 4);
        pnlGrid.add(lblSubTotal, 1, 4);

        lblError = new Label();
        lblError.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px; -fx-font-style: italic;");

        pnlContainer.getChildren().addAll(lblTitle, pnlGrid, lblError);
        return pnlContainer;
    }

    private void populateData() {
        if (existingItem != null) {
            txtProductName.setText(existingItem.getTenSanPham() + " (" + existingItem.getMaSanPham() + ")");
            txtUnit.setText(existingItem.getDonViTinh());
            txtQuantity.setText(String.valueOf(existingItem.getSoLuong()));
            txtUnitPrice.setText(String.valueOf((long) existingItem.getDonGia()));
        } else if (product != null) {
            txtProductName.setText(product.getTenSanPham() + " (" + product.getMaSanPham() + ")");
            txtUnit.setText(product.getDonViTinh());
            txtQuantity.setText("1");
            // Gợi ý giá vốn bằng 70% giá bán nếu chưa có giá nhập
            long estimatedCost = (long) (product.getGiaBan() * 0.7);
            txtUnitPrice.setText(String.valueOf(estimatedCost > 0 ? estimatedCost : 50000));
        }
        capNhatThanhTien();
    }

    private void capNhatThanhTien() {
        try {
            int qty = txtQuantity.getText().isEmpty() ? 0 : Integer.parseInt(txtQuantity.getText().trim());
            double price = txtUnitPrice.getText().isEmpty() ? 0 : Double.parseDouble(txtUnitPrice.getText().trim());
            lblSubTotal.setText(currencyFormat.format(qty * price));
        } catch (Exception ignored) {
            lblSubTotal.setText("0 đ");
        }
    }

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

    private void configureDialogButtons(DialogPane pnlDialogPane) {
        ButtonType btnTypeSave = new ButtonType("Xác nhận", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnTypeCancel = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);

        pnlDialogPane.getButtonTypes().addAll(btnTypeSave, btnTypeCancel);

        btnSave = (Button) pnlDialogPane.lookupButton(btnTypeSave);
        btnSave.setMinWidth(Region.USE_PREF_SIZE);
        btnSave.setStyle(
                "-fx-background-color: #2c6b50; -fx-text-fill: #ffffff; " +
                "-fx-font-weight: bold; -fx-background-radius: 8px; -fx-padding: 8px 22px; -fx-cursor: hand;"
        );

        btnCancel = (Button) pnlDialogPane.lookupButton(btnTypeCancel);
        btnCancel.setMinWidth(Region.USE_PREF_SIZE);
        btnCancel.setStyle(
                "-fx-background-color: #ffffff; -fx-border-color: #d1ded1; " +
                "-fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 8px 18px; -fx-cursor: hand;"
        );

        btnSave.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!validateAndBuild()) {
                event.consume();
            }
        });

        setResultConverter(dialogButton -> {
            if (dialogButton == btnTypeSave) {
                return buildResultItem();
            }
            return null;
        });
    }

    private boolean validateAndBuild() {
        lblError.setText("");

        String qtyStr = txtQuantity.getText() != null ? txtQuantity.getText().trim() : "";
        if (qtyStr.isEmpty()) {
            lblError.setText("⚠ Vui lòng nhập số lượng.");
            txtQuantity.requestFocus();
            return false;
        }

        int qty;
        try {
            qty = Integer.parseInt(qtyStr);
            if (qty <= 0) {
                lblError.setText("⚠ Số lượng nhập phải lớn hơn 0.");
                txtQuantity.requestFocus();
                return false;
            }
        } catch (NumberFormatException ex) {
            lblError.setText("⚠ Số lượng không hợp lệ.");
            txtQuantity.requestFocus();
            return false;
        }

        String priceStr = txtUnitPrice.getText() != null ? txtUnitPrice.getText().trim() : "";
        if (priceStr.isEmpty()) {
            lblError.setText("⚠ Vui lòng nhập đơn giá nhập.");
            txtUnitPrice.requestFocus();
            return false;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 0) {
                lblError.setText("⚠ Đơn giá nhập không được nhỏ hơn 0.");
                txtUnitPrice.requestFocus();
                return false;
            }
        } catch (NumberFormatException ex) {
            lblError.setText("⚠ Đơn giá không hợp lệ.");
            txtUnitPrice.requestFocus();
            return false;
        }

        return true;
    }

    private ImportOrderItem buildResultItem() {
        int qty = Integer.parseInt(txtQuantity.getText().trim());
        double price = Double.parseDouble(txtUnitPrice.getText().trim());

        String spId = product != null ? product.getMaSanPham() : existingItem.getMaSanPham();
        String spName = product != null ? product.getTenSanPham() : existingItem.getTenSanPham();
        String spUnit = product != null ? product.getDonViTinh() : existingItem.getDonViTinh();
        String orderId = existingItem != null ? existingItem.getMaDonHang() : null;

        return new ImportOrderItem(orderId, spId, spName, spUnit, qty, price);
    }
}

