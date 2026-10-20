package com.petshop.view.product;

import com.petshop.model.Product;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.text.DecimalFormat;

/**
 * Hộp thoại Xem chi tiết sản phẩm (ProductDetailDialog).
 * Phục vụ cho chức năng Tìm kiếm (PRD_SEARCH) và xem nhanh thông tin hàng hóa.
 */
public class ProductDetailDialog extends Dialog<Void> {

    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0 đ");

    public ProductDetailDialog(Window owner, Product product) {
        setTitle("Chi Tiết Sản Phẩm - " + product.getTenSanPham());
        setHeaderText(null);
        initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            initOwner(owner);
        }

        DialogPane pnlDialogPane = getDialogPane();
        pnlDialogPane.getStylesheets().add(
                getClass().getResource("/com/petshop/view/styles.css").toExternalForm()
        );
        pnlDialogPane.setPrefSize(520, 480);
        pnlDialogPane.setStyle("-fx-background-color: #f3f6f3;");

        pnlDialogPane.setContent(buildDialogContent(product));
        configureDialogButtons(pnlDialogPane);
    }

    public static void showDetails(Window owner, Product product) {
        if (product != null) {
            ProductDetailDialog dlg = new ProductDetailDialog(owner, product);
            dlg.showAndWait();
        }
    }

    private VBox buildDialogContent(Product product) {
        VBox pnlContainer = new VBox(16);
        pnlContainer.setPadding(new Insets(16, 20, 10, 20));

        // Header
        VBox pnlHeader = new VBox(4);
        Label lblTitle = new Label(product.getTenSanPham());
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");
        lblTitle.setWrapText(true);

        Label lblCode = new Label("Mã định danh: " + product.getMaSanPham() + " · Mã vạch: " + product.getMaVach());
        lblCode.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #6e7c70;");
        pnlHeader.getChildren().addAll(lblTitle, lblCode);

        // Khung chi tiết
        VBox pnlCard = new VBox(12);
        pnlCard.setStyle(
                "-fx-background-color: #ffffff; -fx-background-radius: 12px; " +
                "-fx-border-color: #e5ebe5; -fx-border-radius: 12px; -fx-padding: 18px;"
        );

        pnlCard.getChildren().addAll(
                createDetailRow("Mã sản phẩm:", product.getMaSanPham()),
                createDetailRow("Tên sản phẩm:", product.getTenSanPham()),
                createDetailRow("Mã vạch (Barcode):", product.getMaVach()),
                createDetailRow("Đơn vị tính:", product.getDonViTinh()),
                createDetailRow("Giá bán niêm yết:", currencyFormat.format(product.getGiaBan())),
                createDetailRow("Số lượng tồn kho:", product.getSoLuongTon() + " " + product.getDonViTinh()),
                createDetailRow("Ngưỡng tồn tối thiểu:", product.getNguongTonToiThieu() + " " + product.getDonViTinh()),
                createDetailRow("Nhà cung cấp:", (product.getTenNhaCungCap() != null && !product.getTenNhaCungCap().isEmpty())
                        ? product.getTenNhaCungCap()
                        : (product.getMaNhaCungCap() != null ? product.getMaNhaCungCap() : "Chưa xác định")),
                createStatusRow(product)
        );

        pnlContainer.getChildren().addAll(pnlHeader, pnlCard);
        return pnlContainer;
    }

    private HBox createDetailRow(String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label lbl = new Label(label);
        lbl.setPrefWidth(160);
        lbl.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #6e7c70; -fx-font-weight: 600;");

        Label val = new Label(value != null ? value : "—");
        val.setStyle("-fx-font-size: 13px; -fx-text-fill: #1f2923; -fx-font-weight: bold;");
        val.setWrapText(true);
        HBox.setHgrow(val, Priority.ALWAYS);

        row.getChildren().addAll(lbl, val);
        return row;
    }

    private HBox createStatusRow(Product product) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label lbl = new Label("Trạng thái kinh doanh:");
        lbl.setPrefWidth(160);
        lbl.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #6e7c70; -fx-font-weight: 600;");

        Label badge = new Label(product.isTrangThai() ? "● Đang kinh doanh" : "○ Ngừng kinh doanh");
        badge.setStyle(product.isTrangThai()
                ? "-fx-background-color: #d8f3df; -fx-text-fill: #1e7e34; -fx-font-weight: bold; -fx-padding: 4px 10px; -fx-background-radius: 10px; -fx-font-size: 11.5px;"
                : "-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-font-weight: bold; -fx-padding: 4px 10px; -fx-background-radius: 10px; -fx-font-size: 11.5px;");

        row.getChildren().addAll(lbl, badge);
        return row;
    }

    private void configureDialogButtons(DialogPane pnlDialogPane) {
        ButtonType btnTypeClose = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);
        pnlDialogPane.getButtonTypes().add(btnTypeClose);

        Button btnClose = (Button) pnlDialogPane.lookupButton(btnTypeClose);
        btnClose.setMinWidth(Region.USE_PREF_SIZE);
        btnClose.setStyle(
                "-fx-background-color: #ffffff; -fx-border-color: #d1ded1; " +
                "-fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 8px 20px; -fx-cursor: hand;"
        );
    }
}

