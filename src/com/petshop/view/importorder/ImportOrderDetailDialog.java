package com.petshop.view.importorder;

import com.petshop.model.ImportOrder;
import com.petshop.model.ImportOrderItem;
import javafx.beans.property.ReadOnlyObjectWrapper;
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
import java.util.List;

/**
 * Hộp thoại hiển thị chi tiết Đơn hàng nhập (ImportOrderDetailDialog).
 * Trình bày chi tiết thông tin đơn, đối tác nhà cung cấp, nhân viên lập và bảng mặt hàng.
 */
public class ImportOrderDetailDialog extends Dialog<Void> {

    private final ImportOrder order;
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0 đ");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ImportOrderDetailDialog(Window owner, ImportOrder order) {
        this.order = order;

        setTitle("Chi Tiết Đơn Hàng Nhập - " + order.getMaDonHang());
        setHeaderText(null);
        initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            initOwner(owner);
        }

        DialogPane pnlDialogPane = getDialogPane();
        pnlDialogPane.getStylesheets().add(
                getClass().getResource("/com/petshop/view/styles.css").toExternalForm()
        );
        pnlDialogPane.setPrefSize(780, 560);
        pnlDialogPane.setStyle("-fx-background-color: #f6f5f1;");

        pnlDialogPane.setContent(buildDialogContent());
        configureDialogButtons(pnlDialogPane);
    }

    public static void showDetails(Window owner, ImportOrder order) {
        ImportOrderDetailDialog dlg = new ImportOrderDetailDialog(owner, order);
        dlg.showAndWait();
    }

    private VBox buildDialogContent() {
        VBox pnlContainer = new VBox(14);
        pnlContainer.setPadding(new Insets(16, 20, 10, 20));

        // 1. Header Card thông tin đơn
        VBox pnlHeaderCard = new VBox(10);
        pnlHeaderCard.setStyle("-fx-background-color: #ffffff; -fx-padding: 14px 18px; -fx-background-radius: 10px; -fx-border-color: #e5ebe5; -fx-border-radius: 10px;");

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label lblCode = new Label("Đơn nhập: " + order.getMaDonHang());
        lblCode.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblBadge = createStatusBadge(order.getTrangThai());
        topRow.getChildren().addAll(lblCode, spacer, lblBadge);

        GridPane pnlGrid = new GridPane();
        pnlGrid.setHgap(16);
        pnlGrid.setVgap(8);

        pnlGrid.add(createLabel("Nhà cung cấp:", true), 0, 0);
        pnlGrid.add(createLabel(order.getTenNhaCungCap() != null ? order.getTenNhaCungCap() : order.getMaNhaCungCap(), false), 1, 0);

        pnlGrid.add(createLabel("Số điện thoại NCC:", true), 2, 0);
        pnlGrid.add(createLabel(order.getSoDienThoaiNhaCungCap() != null ? order.getSoDienThoaiNhaCungCap() : "—", false), 3, 0);

        pnlGrid.add(createLabel("Nhân viên lập:", true), 0, 1);
        pnlGrid.add(createLabel((order.getTenNhanVien() != null ? order.getTenNhanVien() : "") + " (" + order.getMaNhanVien() + ")", false), 1, 1);

        pnlGrid.add(createLabel("Ngày lập đơn:", true), 2, 1);
        pnlGrid.add(createLabel(order.getNgayLap() != null ? order.getNgayLap().format(dateFormatter) : "—", false), 3, 1);

        pnlHeaderCard.getChildren().addAll(topRow, new Separator(), pnlGrid);

        // 2. Bảng các mặt hàng nhập
        TableView<ImportOrderItem> tblItems = buildItemsTable();
        VBox.setVgrow(tblItems, Priority.ALWAYS);

        // 3. Footer tổng tiền
        HBox pnlFooter = new HBox(16);
        pnlFooter.setAlignment(Pos.CENTER_RIGHT);
        pnlFooter.setStyle("-fx-padding: 8px 10px;");

        int totalQty = 0;
        if (order.getItems() != null) {
            for (ImportOrderItem item : order.getItems()) {
                totalQty += item.getSoLuong();
            }
        }

        Label lblQtySummary = new Label("Tổng số lượng: " + totalQty + " món");
        lblQtySummary.setStyle("-fx-font-size: 13px; -fx-text-fill: #556056;");

        Label lblTotalTag = new Label("TỔNG TIỀN:");
        lblTotalTag.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");

        Label lblTotalVal = new Label(currencyFormat.format(order.getTongTien()));
        lblTotalVal.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ff7518;");

        pnlFooter.getChildren().addAll(lblQtySummary, new Label(" | "), lblTotalTag, lblTotalVal);

        pnlContainer.getChildren().addAll(pnlHeaderCard, tblItems, pnlFooter);
        return pnlContainer;
    }

    private TableView<ImportOrderItem> buildItemsTable() {
        TableView<ImportOrderItem> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(order.getItems() != null ? order.getItems() : List.of()));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8px; -fx-border-color: #e5ebe5; -fx-border-radius: 8px;");

        TableColumn<ImportOrderItem, Number> colIndex = new TableColumn<>("STT");
        colIndex.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(table.getItems().indexOf(data.getValue()) + 1));
        colIndex.setPrefWidth(45);
        colIndex.setStyle("-fx-alignment: CENTER;");

        TableColumn<ImportOrderItem, String> colCode = new TableColumn<>("Mã SP");
        colCode.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getMaSanPham()));
        colCode.setPrefWidth(125);

        TableColumn<ImportOrderItem, String> colName = new TableColumn<>("Tên sản phẩm");
        colName.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTenSanPham()));
        colName.setPrefWidth(220);

        TableColumn<ImportOrderItem, String> colUnit = new TableColumn<>("ĐVT");
        colUnit.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getDonViTinh()));
        colUnit.setPrefWidth(65);
        colUnit.setStyle("-fx-alignment: CENTER;");

        TableColumn<ImportOrderItem, String> colQty = new TableColumn<>("Số lượng");
        colQty.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().getSoLuong())));
        colQty.setPrefWidth(80);
        colQty.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        TableColumn<ImportOrderItem, String> colPrice = new TableColumn<>("Đơn giá nhập");
        colPrice.setCellValueFactory(data -> new ReadOnlyStringWrapper(currencyFormat.format(data.getValue().getDonGia())));
        colPrice.setPrefWidth(105);
        colPrice.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<ImportOrderItem, String> colTotal = new TableColumn<>("Thành tiền");
        colTotal.setCellValueFactory(data -> new ReadOnlyStringWrapper(currencyFormat.format(data.getValue().getThanhTien())));
        colTotal.setPrefWidth(120);
        colTotal.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: #2c6b50;");

        table.getColumns().addAll(List.of(colIndex, colCode, colName, colUnit, colQty, colPrice, colTotal));
        return table;
    }

    private Label createStatusBadge(String status) {
        Label lbl = new Label(status != null ? status : "Chờ nhận hàng");
        lbl.setStyle("-fx-padding: 4px 12px; -fx-background-radius: 10px; -fx-font-weight: bold; -fx-font-size: 12px;");

        if (ImportOrder.TRANG_THAI_HOAN_TAT.equalsIgnoreCase(status)) {
            lbl.setStyle(lbl.getStyle() + "-fx-background-color: #dcfce7; -fx-text-fill: #15803d;");
        } else if (ImportOrder.TRANG_THAI_DA_HUY.equalsIgnoreCase(status)) {
            lbl.setStyle(lbl.getStyle() + "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626;");
        } else {
            lbl.setStyle(lbl.getStyle() + "-fx-background-color: #fef3c7; -fx-text-fill: #b45309;");
        }
        return lbl;
    }

    private Label createLabel(String text, boolean isHeader) {
        Label lbl = new Label(text);
        if (isHeader) {
            lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #6e7c70; -fx-font-weight: 600;");
        } else {
            lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #1f2923; -fx-font-weight: 500;");
        }
        return lbl;
    }

    private void configureDialogButtons(DialogPane pnlDialogPane) {
        ButtonType btnTypeClose = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);
        pnlDialogPane.getButtonTypes().add(btnTypeClose);

        Button btnClose = (Button) pnlDialogPane.lookupButton(btnTypeClose);
        btnClose.setMinWidth(Region.USE_PREF_SIZE);
        btnClose.setStyle(
                "-fx-background-color: #ffffff; -fx-border-color: #d1ded1; " +
                "-fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 8px 22px; -fx-cursor: hand;"
        );
    }
}

