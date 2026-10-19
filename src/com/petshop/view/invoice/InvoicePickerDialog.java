package com.petshop.view.invoice;

import com.petshop.controller.InvoiceController;
import com.petshop.model.Invoice;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Window;
import javafx.util.Duration;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Hộp thoại Chọn hóa đơn đã thanh toán (InvoicePickerDialog).
 * Bàn giao phụ thuộc sớm (Hạn 21/10/2026) cho Trần Lê Tuấn Anh phục vụ chức năng Lập phiếu đổi trả (RET_CREATE).
 *
 * Tiêu chuẩn Definition of Done:
 * - Đặt tên điều khiển đúng tiền tố: txt, tbl, lbl, btn.
 * - Chỉ cho phép chọn các hóa đơn có trạng thái "Đã thanh toán" (PAID).
 * - Các nút hiển thị tên đầy đủ không bị co rút "..." (setMinWidth(Region.USE_PREF_SIZE)).
 */
public class InvoicePickerDialog extends Dialog<Invoice> {

    private final InvoiceController invoiceController = new InvoiceController();
    private final ObservableList<Invoice> masterInvoiceList = FXCollections.observableArrayList();
    private final FilteredList<Invoice> filteredInvoiceList = new FilteredList<>(masterInvoiceList, inv -> true);
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0 đ");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Điều khiển tuân thủ tiền tố
    private TextField txtSearch;
    private TableView<Invoice> tblInvoices;
    private Label lblStatusCount;
    private Button btnSelect;
    private Button btnCancel;

    public InvoicePickerDialog(Window owner) {
        setTitle("Chọn Hóa Đơn Đã Thanh Toán - Happy Tails");
        setHeaderText(null);
        initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            initOwner(owner);
        }

        DialogPane pnlDialogPane = getDialogPane();
        pnlDialogPane.getStylesheets().add(
                getClass().getResource("/com/petshop/view/styles.css").toExternalForm()
        );
        pnlDialogPane.setPrefSize(780, 520);
        pnlDialogPane.setStyle("-fx-background-color: #f3f6f3;");

        pnlDialogPane.setContent(buildContent());
        configureButtons(pnlDialogPane);
        loadPaidInvoices();

        Platform.runLater(() -> {
            if (txtSearch != null) {
                txtSearch.requestFocus();
            }
        });
    }

    public static Optional<Invoice> showAndPick(Window owner) {
        InvoicePickerDialog dlg = new InvoicePickerDialog(owner);
        return dlg.showAndWait();
    }

    private VBox buildContent() {
        VBox pnlContainer = new VBox(12);
        pnlContainer.setPadding(new Insets(16, 20, 10, 20));

        // Header
        VBox pnlHeader = new VBox(4);
        Label lblTitle = new Label("Danh sách hóa đơn bán hàng đã thanh toán");
        lblTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");
        Label lblSubtitle = new Label("Tìm và chọn hóa đơn gốc cần thực hiện đổi trả hàng hoặc xem lại chi tiết.");
        lblSubtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #6e7c70;");
        pnlHeader.getChildren().addAll(lblTitle, lblSubtitle);

        // Search Bar
        HBox pnlSearch = new HBox(10);
        pnlSearch.setAlignment(Pos.CENTER_LEFT);
        pnlSearch.setPadding(new Insets(10, 14, 10, 14));
        pnlSearch.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 6px; -fx-background-radius: 6px;");

        Label lblSearchIcon = new Label("🔍");
        lblSearchIcon.setStyle("-fx-font-size: 13px; -fx-text-fill: #829485;");

        txtSearch = new TextField();
        txtSearch.setPromptText("Tìm kiếm theo mã hóa đơn (HD-...), tên khách hàng hoặc SĐT...");
        txtSearch.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-font-size: 13px;");
        HBox.setHgrow(txtSearch, Priority.ALWAYS);

        PauseTransition debounce = new PauseTransition(Duration.millis(150));
        txtSearch.textProperty().addListener((obs, o, n) -> {
            debounce.setOnFinished(e -> applyFilter());
            debounce.playFromStart();
        });

        pnlSearch.getChildren().addAll(lblSearchIcon, txtSearch);

        // Table
        tblInvoices = new TableView<>();
        tblInvoices.setItems(filteredInvoiceList);
        tblInvoices.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblInvoices.setPlaceholder(new Label("Không tìm thấy hóa đơn đã thanh toán phù hợp"));
        VBox.setVgrow(tblInvoices, Priority.ALWAYS);

        TableColumn<Invoice, String> colId = new TableColumn<>("Mã hóa đơn");
        colId.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getMaHoaDon()));
        colId.setPrefWidth(140);
        colId.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-weight: bold;");

        TableColumn<Invoice, String> colDate = new TableColumn<>("Ngày lập");
        colDate.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getNgayLap() != null ? c.getValue().getNgayLap().format(dateFormatter) : "-"
        ));
        colDate.setPrefWidth(100);
        colDate.setStyle("-fx-alignment: CENTER;");

        TableColumn<Invoice, String> colCustomer = new TableColumn<>("Khách hàng");
        colCustomer.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTenKhachHang()));
        colCustomer.setPrefWidth(160);
        colCustomer.setStyle("-fx-font-weight: 600;");

        TableColumn<Invoice, String> colPhone = new TableColumn<>("Số điện thoại");
        colPhone.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getSoDienThoaiKhachHang() != null ? c.getValue().getSoDienThoaiKhachHang() : "-"
        ));
        colPhone.setPrefWidth(120);
        colPhone.setStyle("-fx-alignment: CENTER;");

        TableColumn<Invoice, String> colTotal = new TableColumn<>("Tổng tiền");
        colTotal.setCellValueFactory(c -> new ReadOnlyStringWrapper(currencyFormat.format(c.getValue().getTongTien())));
        colTotal.setPrefWidth(130);
        colTotal.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: #15803d;");

        TableColumn<Invoice, String> colPayment = new TableColumn<>("Thanh toán");
        colPayment.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getHinhThucThanhToan() != null ? c.getValue().getHinhThucThanhToan() : "-"
        ));
        colPayment.setPrefWidth(110);
        colPayment.setStyle("-fx-alignment: CENTER;");

        tblInvoices.getColumns().addAll(List.of(colId, colDate, colCustomer, colPhone, colTotal, colPayment));

        tblInvoices.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                Invoice selected = tblInvoices.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    setResult(selected);
                    close();
                }
            }
        });

        // Status bar
        lblStatusCount = new Label("Đang tải danh sách hóa đơn...");
        lblStatusCount.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #6e7c70;");

        pnlContainer.getChildren().addAll(pnlHeader, pnlSearch, tblInvoices, lblStatusCount);
        return pnlContainer;
    }

    private void configureButtons(DialogPane pnlDialogPane) {
        ButtonType btnTypeSelect = new ButtonType("Chọn hóa đơn", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnTypeCancel = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);

        pnlDialogPane.getButtonTypes().addAll(btnTypeSelect, btnTypeCancel);

        btnSelect = (Button) pnlDialogPane.lookupButton(btnTypeSelect);
        btnCancel = (Button) pnlDialogPane.lookupButton(btnTypeCancel);

        btnSelect.setMinWidth(Region.USE_PREF_SIZE);
        btnCancel.setMinWidth(Region.USE_PREF_SIZE);

        btnSelect.getStyleClass().addAll("customer-add");
        btnCancel.getStyleClass().addAll("customer-secondary");

        btnSelect.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            Invoice selected = tblInvoices.getSelectionModel().getSelectedItem();
            if (selected == null) {
                event.consume();
                Alert alert = new Alert(Alert.AlertType.WARNING, "Vui lòng chọn một hóa đơn từ danh sách.");
                alert.showAndWait();
            }
        });

        setResultConverter(dialogButton -> {
            if (dialogButton == btnTypeSelect) {
                return tblInvoices.getSelectionModel().getSelectedItem();
            }
            return null;
        });
    }

    private void loadPaidInvoices() {
        try {
            List<Invoice> list = invoiceController.getPaidInvoices(null);
            masterInvoiceList.setAll(list);
            updateStatusText();
        } catch (Exception ex) {
            lblStatusCount.setText("Lỗi nạp hóa đơn: " + ex.getMessage());
        }
    }

    private void applyFilter() {
        String keyword = txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";
        filteredInvoiceList.setPredicate(inv -> {
            if (keyword.isEmpty()) return true;
            return (inv.getMaHoaDon() != null && inv.getMaHoaDon().toLowerCase().contains(keyword))
                    || (inv.getTenKhachHang() != null && inv.getTenKhachHang().toLowerCase().contains(keyword))
                    || (inv.getSoDienThoaiKhachHang() != null && inv.getSoDienThoaiKhachHang().contains(keyword));
        });
        updateStatusText();
    }

    private void updateStatusText() {
        lblStatusCount.setText(String.format("Hiển thị %d / %d hóa đơn đã thanh toán",
                filteredInvoiceList.size(), masterInvoiceList.size()));
    }
}

