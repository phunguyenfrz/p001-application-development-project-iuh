package com.petshop.view.product;

import com.petshop.controller.ProductController;
import com.petshop.model.Product;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * Hộp thoại Điều chỉnh tồn kho và Ngưỡng an toàn (ProductStockDialog).
 * Phục vụ nghiệp vụ Tồn kho (PRD_INVENTORY) và Hàng sắp hết (PRD_LOW_STOCK).
 */
public class ProductStockDialog extends Dialog<Boolean> {

    private final ProductController productController;
    private final Product product;

    private TextField txtCurrentStock;
    private TextField txtNewStock;
    private TextField txtThreshold;
    private Label lblError;
    private Button btnSave;
    private Button btnCancel;

    public ProductStockDialog(Window owner, Product product) {
        this.productController = new ProductController();
        this.product = product;

        setTitle("Điều Chỉnh Tồn Kho - " + product.getTenSanPham());
        setHeaderText(null);
        initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            initOwner(owner);
        }

        DialogPane pnlDialogPane = getDialogPane();
        pnlDialogPane.getStylesheets().add(
                getClass().getResource("/com/petshop/view/styles.css").toExternalForm()
        );
        pnlDialogPane.setPrefSize(480, 420);
        pnlDialogPane.setStyle("-fx-background-color: #f3f6f3;");

        pnlDialogPane.setContent(buildDialogContent());
        configureDialogButtons(pnlDialogPane);
    }

    public static Optional<Boolean> showAndAdjust(Window owner, Product product) {
        if (product != null) {
            ProductStockDialog dlg = new ProductStockDialog(owner, product);
            return dlg.showAndWait();
        }
        return Optional.empty();
    }

    private VBox buildDialogContent() {
        VBox pnlContainer = new VBox(14);
        pnlContainer.setPadding(new Insets(16, 20, 10, 20));

        VBox pnlHeader = new VBox(4);
        Label lblTitle = new Label("Cập nhật số lượng tồn kho");
        lblTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");
        Label lblSub = new Label("Sản phẩm: " + product.getTenSanPham() + " (" + product.getDonViTinh() + ")");
        lblSub.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #6e7c70;");
        pnlHeader.getChildren().addAll(lblTitle, lblSub);

        VBox pnlForm = new VBox(12);
        pnlForm.setStyle(
                "-fx-background-color: #ffffff; -fx-background-radius: 12px; " +
                "-fx-border-color: #e5ebe5; -fx-border-radius: 12px; -fx-padding: 18px;"
        );

        // Tồn kho hiện tại
        VBox boxCurrent = new VBox(4);
        Label lblCur = new Label("Số lượng tồn hiện tại:");
        lblCur.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #6e7c70;");
        txtCurrentStock = new TextField(String.valueOf(product.getSoLuongTon()));
        txtCurrentStock.setEditable(false);
        txtCurrentStock.setStyle("-fx-background-color: #f7faf7; -fx-font-weight: bold; -fx-pref-height: 36px; -fx-border-color: #d8e2d8; -fx-border-radius: 6px;");
        boxCurrent.getChildren().addAll(lblCur, txtCurrentStock);

        // Tồn kho mới
        VBox boxNew = new VBox(4);
        Label lblNew = new Label("Số lượng tồn mới (*):");
        lblNew.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #2c6b50;");
        txtNewStock = new TextField(String.valueOf(product.getSoLuongTon()));
        txtNewStock.setPrefHeight(36);
        txtNewStock.setStyle("-fx-background-color: #ffffff; -fx-border-color: #2c6b50; -fx-border-radius: 6px; -fx-font-weight: bold;");
        applyNumericFilter(txtNewStock); // Chỉ cho nhập số
        boxNew.getChildren().addAll(lblNew, txtNewStock);

        // Các nút cộng nhanh
        HBox boxQuickAdd = new HBox(8);
        boxQuickAdd.setAlignment(Pos.CENTER_LEFT);
        Label lblQuick = new Label("Cộng thêm:");
        lblQuick.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #6e7c70;");

        Button btnAdd10 = createQuickButton("+10", 10);
        Button btnAdd50 = createQuickButton("+50", 50);
        Button btnAdd100 = createQuickButton("+100", 100);
        boxQuickAdd.getChildren().addAll(lblQuick, btnAdd10, btnAdd50, btnAdd100);

        // Ngưỡng cảnh báo tồn tối thiểu
        VBox boxThreshold = new VBox(4);
        Label lblThresh = new Label("Ngưỡng tồn tối thiểu an toàn (*):");
        lblThresh.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #6e7c70;");
        txtThreshold = new TextField(String.valueOf(product.getNguongTonToiThieu()));
        txtThreshold.setPrefHeight(36);
        txtThreshold.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 6px;");
        applyNumericFilter(txtThreshold); // Chỉ cho nhập số
        boxThreshold.getChildren().addAll(lblThresh, txtThreshold);

        pnlForm.getChildren().addAll(boxCurrent, boxNew, boxQuickAdd, boxThreshold);

        lblError = new Label();
        lblError.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px; -fx-font-weight: 600;");

        pnlContainer.getChildren().addAll(pnlHeader, pnlForm, lblError);
        return pnlContainer;
    }

    private Button createQuickButton(String text, int amount) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color: #f0f7f3; -fx-border-color: #c4ded0; " +
                "-fx-border-radius: 6px; -fx-background-radius: 6px; -fx-font-size: 11.5px; -fx-cursor: hand;"
        );
        btn.setMinWidth(Region.USE_PREF_SIZE);
        btn.setOnAction(e -> {
            try {
                int cur = txtNewStock.getText().isEmpty() ? 0 : Integer.parseInt(txtNewStock.getText().trim());
                txtNewStock.setText(String.valueOf(cur + amount));
            } catch (Exception ignored) {
            }
        });
        return btn;
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
        ButtonType btnTypeSave = new ButtonType("Lưu tồn kho", ButtonBar.ButtonData.OK_DONE);
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

        btnSave.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!validateAndUpdate()) {
                event.consume();
            }
        });

        setResultConverter(dialogButton -> dialogButton == btnTypeSave);
    }

    private boolean validateAndUpdate() {
        lblError.setText("");
        String stockStr = txtNewStock.getText() != null ? txtNewStock.getText().trim() : "";
        if (stockStr.isEmpty()) {
            lblError.setText("⚠ Vui lòng nhập số lượng tồn mới.");
            txtNewStock.requestFocus();
            return false;
        }

        String threshStr = txtThreshold.getText() != null ? txtThreshold.getText().trim() : "";
        if (threshStr.isEmpty()) {
            lblError.setText("⚠ Vui lòng nhập ngưỡng tồn an toàn.");
            txtThreshold.requestFocus();
            return false;
        }

        try {
            int newStock = Integer.parseInt(stockStr);
            int newThresh = Integer.parseInt(threshStr);

            productController.updateStock(product.getMaSanPham(), newStock);
            productController.updateThreshold(product.getMaSanPham(), newThresh);

            product.setSoLuongTon(newStock);
            product.setNguongTonToiThieu(newThresh);
            return true;
        } catch (Exception ex) {
            lblError.setText("⚠ " + ex.getMessage());
            return false;
        }
    }
}

