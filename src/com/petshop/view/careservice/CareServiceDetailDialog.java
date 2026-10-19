package com.petshop.view.careservice;

import com.petshop.model.CareService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.text.DecimalFormat;

/**
 * Hộp thoại Xem chi tiết dịch vụ chăm sóc và bảng giá 3 bậc hiện hành (CareServiceDetailDialog).
 *
 * Tuân thủ Definition of Done:
 * - Đặt tên điều khiển đúng tiền tố: lbl, btn.
 * - Hiển thị trực quan thông tin dịch vụ và 3 bậc giá: Nhỏ (<5kg), Vừa (5-10kg), Lớn (>10kg).
 * - Nút bấm hiển thị đầy đủ tên không bị co rút "...".
 */
public class CareServiceDetailDialog extends Dialog<Void> {

    private final CareService service;
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0 đ");

    public CareServiceDetailDialog(Window owner, CareService service) {
        this.service = service;

        setTitle("Chi Tiết Dịch Vụ Chăm Sóc - " + (service != null ? service.getTenDichVu() : ""));
        setHeaderText(null);
        initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            initOwner(owner);
        }

        DialogPane pnlDialogPane = getDialogPane();
        pnlDialogPane.getStylesheets().add(
                getClass().getResource("/com/petshop/view/styles.css").toExternalForm()
        );
        pnlDialogPane.setPrefSize(560, 480);
        pnlDialogPane.setStyle("-fx-background-color: #f3f6f3;");

        pnlDialogPane.setContent(buildContent());

        ButtonType btnTypeClose = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);
        pnlDialogPane.getButtonTypes().add(btnTypeClose);

        Button btnClose = (Button) pnlDialogPane.lookupButton(btnTypeClose);
        btnClose.setMinWidth(Region.USE_PREF_SIZE);
        btnClose.getStyleClass().addAll("customer-secondary");
    }

    public static void showDetails(Window owner, CareService service) {
        if (service == null) return;
        CareServiceDetailDialog dlg = new CareServiceDetailDialog(owner, service);
        dlg.showAndWait();
    }

    private VBox buildContent() {
        VBox pnlContainer = new VBox(16);
        pnlContainer.setPadding(new Insets(16, 20, 10, 20));

        // Header
        VBox pnlHeader = new VBox(4);
        Label lblTitle = new Label(service.getTenDichVu());
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");
        Label lblSubtitle = new Label("Mã dịch vụ: " + service.getMaDichVu());
        lblSubtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #6e7c70;");
        pnlHeader.getChildren().addAll(lblTitle, lblSubtitle);

        // General info card
        VBox pnlInfoCard = new VBox(10);
        pnlInfoCard.setPadding(new Insets(14));
        pnlInfoCard.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        HBox rowStatus = new HBox(12);
        rowStatus.setAlignment(Pos.CENTER_LEFT);

        Label lblDurationTag = new Label("⏱ Thời lượng: " + service.getThoiLuong() + " phút");
        lblDurationTag.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0369a1; -fx-padding: 4px 10px; -fx-background-radius: 12px; -fx-font-weight: 600; -fx-font-size: 12px;");

        Label lblStatusTag = new Label(service.isTrangThai() ? "● Đang cung cấp" : "● Tạm ngừng");
        if (service.isTrangThai()) {
            lblStatusTag.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-padding: 4px 10px; -fx-background-radius: 12px; -fx-font-weight: 600; -fx-font-size: 12px;");
        } else {
            lblStatusTag.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-padding: 4px 10px; -fx-background-radius: 12px; -fx-font-weight: 600; -fx-font-size: 12px;");
        }
        rowStatus.getChildren().addAll(lblDurationTag, lblStatusTag);

        Label lblDescTitle = new Label("Mô tả / Quy trình thực hiện:");
        lblDescTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151; -fx-font-size: 12.5px;");

        Label lblDescContent = new Label(service.getMoTa() != null && !service.getMoTa().trim().isEmpty() ? service.getMoTa() : "Chưa có mô tả chi tiết cho dịch vụ này.");
        lblDescContent.setWrapText(true);
        lblDescContent.setStyle("-fx-text-fill: #4b5563; -fx-font-size: 13px; -fx-line-spacing: 3px;");

        pnlInfoCard.getChildren().addAll(rowStatus, lblDescTitle, lblDescContent);

        // 3-Tier Pricing Card
        VBox pnlPricingSection = new VBox(8);
        Label lblPricingTitle = new Label("BẢNG GIÁ HIỆN HÀNH THEO 3 BẬC CÂN NẶNG");
        lblPricingTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");

        HBox pnlPricingGrid = new HBox(12);
        pnlPricingGrid.getChildren().addAll(
                createPriceCard("Nhỏ (< 5kg)", service.getGiaNho(), "#f0fdf4", "#166534", "#bbf7d0"),
                createPriceCard("Vừa (5 - 10kg)", service.getGiaVua(), "#eff6ff", "#1e40af", "#bfdbfe"),
                createPriceCard("Lớn (> 10kg)", service.getGiaLon(), "#faf5ff", "#6b21a8", "#e9d5ff")
        );

        pnlPricingSection.getChildren().addAll(lblPricingTitle, pnlPricingGrid);

        pnlContainer.getChildren().addAll(pnlHeader, pnlInfoCard, pnlPricingSection);
        return pnlContainer;
    }

    private VBox createPriceCard(String tierTitle, double price, String bgColor, String textColor, String borderColor) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(12, 10, 12, 10));
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setStyle(String.format(
                "-fx-background-color: %s; -fx-border-color: %s; -fx-border-radius: 8px; -fx-background-radius: 8px;",
                bgColor, borderColor
        ));

        Label lblTier = new Label(tierTitle);
        lblTier.setStyle(String.format("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: %s;", textColor));

        Label lblPrice = new Label(price > 0 ? currencyFormat.format(price) : "Chưa thiết lập");
        lblPrice.setStyle(String.format("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: %s;", textColor));

        card.getChildren().addAll(lblTier, lblPrice);
        return card;
    }
}

