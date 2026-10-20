package com.petshop.view.invoice;

import com.petshop.controller.InvoiceController;
import com.petshop.dao.InvoiceDao.AppointmentBillingItem;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Window;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Hộp thoại chọn Lịch hẹn dịch vụ chưa thanh toán để đưa vào hóa đơn.
 * Tuân thủ tiêu chuẩn:
 * - Tên lớp là danh từ (AppointmentPickerDialog).
 * - Tên điều khiển có tiền tố đúng chuẩn: txt, tbl, col, btn, lbl.
 * - Cho phép tra cứu nhanh lịch hẹn của khách hàng và tự động đồng bộ giá dịch vụ + cân nặng.
 */
public class AppointmentPickerDialog extends Dialog<AppointmentBillingItem> {

    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0 'đ'");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final InvoiceController invoiceController = new InvoiceController();

    private TextField txtSearch;
    private TableView<AppointmentBillingItem> tblAppointments;
    private ObservableList<AppointmentBillingItem> appointmentList = FXCollections.observableArrayList();
    private Label lblResultCount;

    public AppointmentPickerDialog(Window owner) {
        initOwner(owner);
        setTitle("Chọn Lịch Hẹn Chăm Sóc - Happy Tails");
        setHeaderText(null);

        DialogPane pnlDialogPane = getDialogPane();
        pnlDialogPane.setContent(buildContent());

        ButtonType btnTypeSelect = new ButtonType("Áp dụng vào hóa đơn", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnTypeCancel = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);
        pnlDialogPane.getButtonTypes().addAll(btnTypeSelect, btnTypeCancel);

        Button btnSelect = (Button) pnlDialogPane.lookupButton(btnTypeSelect);
        btnSelect.getStyleClass().add("customer-add");
        btnSelect.setStyle("-fx-background-color: #15803d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 18px; -fx-background-radius: 6px;");

        Button btnCancel = (Button) pnlDialogPane.lookupButton(btnTypeCancel);
        btnCancel.getStyleClass().add("customer-secondary");

        btnSelect.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            AppointmentBillingItem chosen = tblAppointments.getSelectionModel().getSelectedItem();
            if (chosen == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Chưa chọn lịch hẹn");
                alert.setHeaderText(null);
                alert.setContentText("Vui lòng chọn một lịch hẹn trong danh sách để đưa vào hóa đơn.");
                alert.initOwner(getDialogPane().getScene().getWindow());
                alert.showAndWait();
                event.consume();
            }
        });

        setResultConverter(btnType -> {
            if (btnType == btnTypeSelect) {
                return tblAppointments.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        taiDuLieuLichHen();
    }

    public static AppointmentBillingItem showDialog(Window owner) {
        AppointmentPickerDialog dlg = new AppointmentPickerDialog(owner);
        return dlg.showAndWait().orElse(null);
    }

    private VBox buildContent() {
        VBox pnlRoot = new VBox(14);
        pnlRoot.setPrefWidth(820);
        pnlRoot.setPrefHeight(480);
        pnlRoot.setPadding(new Insets(16));
        pnlRoot.setStyle("-fx-background-color: #ffffff;");

        // Header info
        VBox pnlBanner = new VBox(4);
        pnlBanner.setPadding(new Insets(10, 14, 10, 14));
        pnlBanner.setStyle("-fx-background-color: #eff6ff; -fx-border-color: #bfdbfe; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        Label lblBannerTitle = new Label("📅 DANH SÁCH LỊCH HẸN CHỜ THANH TOÁN");
        lblBannerTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #1d4ed8; -fx-font-size: 13px;");
        Label lblBannerSub = new Label("Chọn lịch hẹn đã được phục vụ để lập hóa đơn thanh toán cho khách hàng.");
        lblBannerSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #4b5563;");
        pnlBanner.getChildren().addAll(lblBannerTitle, lblBannerSub);

        // Thanh tìm kiếm
        HBox pnlSearchBar = new HBox(10);
        pnlSearchBar.setAlignment(Pos.CENTER_LEFT);

        txtSearch = new TextField();
        txtSearch.setPromptText("Tìm kiếm theo mã LH, tên khách, số điện thoại, tên thú cưng, dịch vụ...");
        txtSearch.setStyle("-fx-pref-height: 36px; -fx-font-size: 12.5px; -fx-background-color: #f9fafb; -fx-border-color: #d1d5db; -fx-border-radius: 6px;");
        HBox.setHgrow(txtSearch, Priority.ALWAYS);
        txtSearch.textProperty().addListener((obs, o, n) -> taiDuLieuLichHen());

        Button btnRefresh = new Button("🔄 Làm mới");
        btnRefresh.setStyle("-fx-pref-height: 36px; -fx-background-color: #ffffff; -fx-border-color: #d1d5db; -fx-border-radius: 6px; -fx-cursor: hand; -fx-font-weight: 600;");
        btnRefresh.setOnAction(e -> {
            txtSearch.clear();
            taiDuLieuLichHen();
        });

        pnlSearchBar.getChildren().addAll(txtSearch, btnRefresh);

        // Bảng lịch hẹn
        tblAppointments = new TableView<>();
        tblAppointments.setItems(appointmentList);
        tblAppointments.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblAppointments.setPlaceholder(new Label("Không có lịch hẹn nào đang chờ thanh toán."));
        VBox.setVgrow(tblAppointments, Priority.ALWAYS);

        TableColumn<AppointmentBillingItem, String> colId = new TableColumn<>("Mã lịch hẹn");
        colId.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getMaLichHen()));
        colId.setPrefWidth(130);
        colId.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-weight: bold;");

        TableColumn<AppointmentBillingItem, String> colCustomer = new TableColumn<>("Khách hàng");
        colCustomer.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTenKhachHang() + "\n" + c.getValue().getSoDienThoaiKhachHang()));
        colCustomer.setPrefWidth(160);

        TableColumn<AppointmentBillingItem, String> colPet = new TableColumn<>("Thú cưng");
        colPet.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTenThuCung() + " (" + c.getValue().getCanNang() + " kg)"));
        colPet.setPrefWidth(130);

        TableColumn<AppointmentBillingItem, String> colService = new TableColumn<>("Dịch vụ");
        colService.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTenDichVu()));
        colService.setPrefWidth(160);
        colService.setStyle("-fx-font-weight: 600;");

        TableColumn<AppointmentBillingItem, String> colPrice = new TableColumn<>("Đơn giá");
        colPrice.setCellValueFactory(c -> new ReadOnlyStringWrapper(currencyFormat.format(c.getValue().getDonGia())));
        colPrice.setPrefWidth(100);
        colPrice.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: #15803d;");

        TableColumn<AppointmentBillingItem, String> colDateTime = new TableColumn<>("Thời gian hẹn");
        colDateTime.setCellValueFactory(c -> {
            String d = c.getValue().getNgayHen() != null ? c.getValue().getNgayHen().format(dateFormatter) : "";
            String t = c.getValue().getGioHen() != null ? c.getValue().getGioHen().format(timeFormatter) : "";
            return new ReadOnlyStringWrapper(t + " - " + d);
        });
        colDateTime.setPrefWidth(120);
        colDateTime.setStyle("-fx-alignment: CENTER;");

        TableColumn<AppointmentBillingItem, String> colStatus = new TableColumn<>("Trạng thái");
        colStatus.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTrangThai()));
        colStatus.setPrefWidth(100);
        colStatus.setStyle("-fx-alignment: CENTER;");

        tblAppointments.getColumns().addAll(List.of(colId, colCustomer, colPet, colService, colPrice, colDateTime, colStatus));

        // Footer kết quả
        lblResultCount = new Label("Đang nạp dữ liệu...");
        lblResultCount.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #6b7280;");

        pnlRoot.getChildren().addAll(pnlBanner, pnlSearchBar, tblAppointments, lblResultCount);
        return pnlRoot;
    }

    private void taiDuLieuLichHen() {
        String keyword = txtSearch != null ? txtSearch.getText().trim() : "";
        List<AppointmentBillingItem> list = invoiceController.getUnbilledAppointments(keyword);
        appointmentList.setAll(list);
        if (lblResultCount != null) {
            lblResultCount.setText("Tìm thấy " + list.size() + " lịch hẹn chờ thanh toán.");
        }
    }
}

