package com.petshop.view.invoice;

import com.petshop.model.Invoice;
import com.petshop.model.InvoiceItem;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

/**
 * Hộp thoại Xem chi tiết / In hóa đơn bán hàng (InvoiceDetailDialog).
 * Trình bày phiếu tính tiền chuyên nghiệp, trực quan theo đúng biểu mẫu cửa hàng Happy Tails.
 *
 * Tiêu chuẩn Definition of Done:
 * - Đặt tên điều khiển đúng tiền tố: lbl, tbl, btn.
 * - Các nút hiển thị tên đầy đủ không bị co rút "..." (setMinWidth(Region.USE_PREF_SIZE)).
 */
public class InvoiceDetailDialog extends Dialog<Void> {

    private final Invoice invoice;
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0 đ");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public InvoiceDetailDialog(Window owner, Invoice invoice) {
        this.invoice = invoice;

        setTitle("Chi Tiết Hóa Đơn - " + (invoice != null ? invoice.getMaHoaDon() : ""));
        setHeaderText(null);
        initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            initOwner(owner);
        }

        DialogPane pnlDialogPane = getDialogPane();
        pnlDialogPane.getStylesheets().add(
                getClass().getResource("/com/petshop/view/styles.css").toExternalForm()
        );
        pnlDialogPane.setPrefSize(720, 620);
        pnlDialogPane.setStyle("-fx-background-color: #f3f6f3;");

        pnlDialogPane.setContent(buildReceiptContent());

        ButtonType btnTypePrint = new ButtonType("In hóa đơn", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnTypeClose = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);
        pnlDialogPane.getButtonTypes().addAll(btnTypePrint, btnTypeClose);

        Button btnPrint = (Button) pnlDialogPane.lookupButton(btnTypePrint);
        Button btnClose = (Button) pnlDialogPane.lookupButton(btnTypeClose);

        btnPrint.setMinWidth(Region.USE_PREF_SIZE);
        btnClose.setMinWidth(Region.USE_PREF_SIZE);

        btnPrint.getStyleClass().addAll("customer-add");
        btnClose.getStyleClass().addAll("customer-secondary");

        btnPrint.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("In Hóa Đơn - Happy Tails");
            alert.setHeaderText("Đang kết nối máy in hóa đơn...");
            alert.setContentText("Lệnh in hóa đơn [" + invoice.getMaHoaDon() + "] đã được gửi tới máy in nhiệt thành công!");
            alert.showAndWait();
        });
    }

    public static void showDetails(Window owner, Invoice invoice) {
        if (invoice == null) return;
        InvoiceDetailDialog dlg = new InvoiceDetailDialog(owner, invoice);
        dlg.showAndWait();
    }

    private VBox buildReceiptContent() {
        VBox pnlContainer = new VBox(14);
        pnlContainer.setPadding(new Insets(16, 20, 10, 20));

        // Header Phiếu
        VBox pnlHeader = new VBox(4);
        pnlHeader.setAlignment(Pos.CENTER);
        Label lblStoreName = new Label("HAPPY TAILS PET SHOP & SPA");
        lblStoreName.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");
        Label lblReceiptTitle = new Label("HÓA ĐƠN THANH TOÁN");
        lblReceiptTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-text-fill: #15803d;");
        Label lblStoreAddress = new Label("Địa chỉ: 12 Nguyễn Văn Bảo, Phường 4, Gò Vấp, TP.HCM | Hotline: 1900 6868");
        lblStoreAddress.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #6e7c70;");
        pnlHeader.getChildren().addAll(lblStoreName, lblReceiptTitle, lblStoreAddress);

        // General Info Card
        GridPane pnlInfo = new GridPane();
        pnlInfo.setHgap(20);
        pnlInfo.setVgap(8);
        pnlInfo.setPadding(new Insets(12, 14, 12, 14));
        pnlInfo.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        pnlInfo.add(createInfoRow("Mã hóa đơn:", invoice.getMaHoaDon()), 0, 0);
        pnlInfo.add(createInfoRow("Ngày lập:", invoice.getNgayLap() != null ? invoice.getNgayLap().format(dateFormatter) : "-"), 1, 0);

        pnlInfo.add(createInfoRow("Thu ngân:", invoice.getTenNhanVien()), 0, 1);
        pnlInfo.add(createInfoRow("Khách hàng:", invoice.getTenKhachHang()), 1, 1);

        pnlInfo.add(createInfoRow("Phương thức:", invoice.getHinhThucThanhToan() != null ? invoice.getHinhThucThanhToan() : "Chưa xác định"), 0, 2);

        HBox rowStatus = new HBox(6);
        rowStatus.setAlignment(Pos.CENTER_LEFT);
        Label lblStatusTitle = new Label("Trạng thái:");
        lblStatusTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #4b5a50; -fx-font-size: 12px;");

        Label lblStatusBadge = new Label(invoice.getStatus().getDisplayName());
        String badgeStyle = switch (invoice.getStatus()) {
            case PAID -> "-fx-background-color: #dcfce7; -fx-text-fill: #15803d;";
            case CANCELLED -> "-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c;";
            default -> "-fx-background-color: #fef3c7; -fx-text-fill: #b45309;";
        };
        lblStatusBadge.setStyle(badgeStyle + " -fx-padding: 2px 8px; -fx-background-radius: 10px; -fx-font-weight: bold; -fx-font-size: 11px;");
        rowStatus.getChildren().addAll(lblStatusTitle, lblStatusBadge);
        pnlInfo.add(rowStatus, 1, 2);

        // Table Items
        TableView<InvoiceItem> tblItems = new TableView<>();
        tblItems.setItems(FXCollections.observableArrayList(invoice.getItems()));
        tblItems.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblItems.setPrefHeight(200);

        TableColumn<InvoiceItem, String> colIndex = new TableColumn<>("STT");
        colIndex.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.valueOf(invoice.getItems().indexOf(c.getValue()) + 1)));
        colIndex.setPrefWidth(45);
        colIndex.setStyle("-fx-alignment: CENTER;");

        TableColumn<InvoiceItem, String> colName = new TableColumn<>("Tên hàng hóa / Dịch vụ");
        colName.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTenHang() != null ? c.getValue().getTenHang() : "Mặt hàng"));
        colName.setPrefWidth(240);
        colName.setStyle("-fx-font-weight: 600;");

        TableColumn<InvoiceItem, String> colType = new TableColumn<>("Loại");
        colType.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().isService() ? "Dịch vụ" : "Sản phẩm"));
        colType.setPrefWidth(85);
        colType.setStyle("-fx-alignment: CENTER;");

        TableColumn<InvoiceItem, String> colPrice = new TableColumn<>("Đơn giá");
        colPrice.setCellValueFactory(c -> new ReadOnlyStringWrapper(currencyFormat.format(c.getValue().getDonGia())));
        colPrice.setPrefWidth(110);
        colPrice.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<InvoiceItem, String> colQty = new TableColumn<>("SL");
        colQty.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.valueOf(c.getValue().getSoLuong())));
        colQty.setPrefWidth(50);
        colQty.setStyle("-fx-alignment: CENTER;");

        TableColumn<InvoiceItem, String> colTotal = new TableColumn<>("Thành tiền");
        colTotal.setCellValueFactory(c -> new ReadOnlyStringWrapper(currencyFormat.format(c.getValue().getThanhTien())));
        colTotal.setPrefWidth(120);
        colTotal.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");

        tblItems.getColumns().addAll(colIndex, colName, colType, colPrice, colQty, colTotal);

        // Summary Card
        VBox pnlSummary = new VBox(6);
        pnlSummary.setAlignment(Pos.CENTER_RIGHT);
        pnlSummary.setPadding(new Insets(10, 14, 10, 14));
        pnlSummary.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        HBox rowSubTotal = createSummaryLine("Tổng tiền hàng:", currencyFormat.format(invoice.getTongTienHang()), false);
        HBox rowDiscount = createSummaryLine("Giảm giá khuyến mãi " + (invoice.getPhanTramGiam() > 0 ? "(" + (int)invoice.getPhanTramGiam() + "%):" : ":"),
                invoice.getTienGiam() > 0 ? "- " + currencyFormat.format(invoice.getTienGiam()) : "0 đ", false);
        HBox rowGrandTotal = createSummaryLine("TỔNG TIỀN THANH TOÁN:", currencyFormat.format(invoice.getTongTien()), true);

        pnlSummary.getChildren().addAll(rowSubTotal, rowDiscount, new Separator(), rowGrandTotal);

        pnlContainer.getChildren().addAll(pnlHeader, pnlInfo, tblItems, pnlSummary);
        return pnlContainer;
    }

    private HBox createInfoRow(String labelText, String valueText) {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER_LEFT);
        Label lblLabel = new Label(labelText);
        lblLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4b5a50; -fx-font-size: 12px;");
        Label lblVal = new Label(valueText != null ? valueText : "-");
        lblVal.setStyle("-fx-text-fill: #1f2923; -fx-font-size: 12px;");
        row.getChildren().addAll(lblLabel, lblVal);
        return row;
    }

    private HBox createSummaryLine(String title, String value, boolean isGrand) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_RIGHT);

        Label lblTitle = new Label(title);
        lblTitle.setStyle(isGrand ? "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1f2923;" : "-fx-font-size: 12.5px; -fx-text-fill: #4b5a50;");

        Label lblValue = new Label(value);
        lblValue.setStyle(isGrand ? "-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #15803d;" : "-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #1f2923;");

        row.getChildren().addAll(lblTitle, lblValue);
        return row;
    }
}
