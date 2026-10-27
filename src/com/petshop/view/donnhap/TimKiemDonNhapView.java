package com.petshop.view.donnhap;

import com.petshop.controller.DonHangController;
import com.petshop.model.DonNhap;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TimKiemDonNhapView extends VBox {

    private static final Locale LOCALE_VI_VN = new Locale("vi", "VN");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DonHangController donHangController;
    private final TextField txtMaDonHang = new TextField();
    private final TextField txtNhaCungCap = new TextField();
    private final DatePicker dpTuNgay = new DatePicker();
    private final DatePicker dpDenNgay = new DatePicker();
    private final TableView<DonNhap> tblKetQua = new TableView<>();
    private final Button btnXemChiTiet = new Button("Xem chi tiết");

    public TimKiemDonNhapView() {
        this(new DonHangController());
    }

    public TimKiemDonNhapView(DonHangController donHangController) {
        this.donHangController = donHangController;
        setSpacing(14);
        setPadding(new Insets(18));
        getStyleClass().add("khung-phan-he");

        Label lblTieuDe = new Label("TÌM KIẾM ĐƠN NHẬP");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Lọc đơn nhập theo mã đơn, nhà cung cấp hoặc ngày lập.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");

        GridPane pnlBoLoc = taoBoLoc();
        tblKetQua.getStyleClass().add("bang-du-lieu");
        tblKetQua.setPlaceholder(new Label("Không tìm thấy đơn nhập phù hợp"));
        tblKetQua.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        btnXemChiTiet.getStyleClass().add("nut-phu");
        btnXemChiTiet.disableProperty().bind(tblKetQua.getSelectionModel().selectedItemProperty().isNull());
        btnXemChiTiet.setOnAction(event -> moChiTiet());
        VBox.setVgrow(tblKetQua, Priority.ALWAYS);
        taoCotKetQua();

        HBox pnlThaoTacKetQua = new HBox(btnXemChiTiet);
        pnlThaoTacKetQua.setAlignment(Pos.CENTER_RIGHT);
        VBox pnlNoiDung = new VBox(12, pnlBoLoc, pnlThaoTacKetQua, tblKetQua);
        pnlNoiDung.getStyleClass().add("hop-noi-dung");
        VBox.setVgrow(pnlNoiDung, Priority.ALWAYS);
        getChildren().addAll(lblTieuDe, lblMoTa, pnlNoiDung);
        timKiem();
    }

    private GridPane taoBoLoc() {
        GridPane pnlBoLoc = new GridPane();
        pnlBoLoc.setHgap(10);
        pnlBoLoc.setVgap(8);
        pnlBoLoc.setPadding(new Insets(4));

        txtMaDonHang.setPromptText("Nhập mã đơn");
        txtNhaCungCap.setPromptText("Nhập tên nhà cung cấp");
        dpTuNgay.setPromptText("Từ ngày");
        dpDenNgay.setPromptText("Đến ngày");
        txtMaDonHang.setMaxWidth(Double.MAX_VALUE);
        txtNhaCungCap.setMaxWidth(Double.MAX_VALUE);
        dpTuNgay.setMaxWidth(Double.MAX_VALUE);
        dpDenNgay.setMaxWidth(Double.MAX_VALUE);

        pnlBoLoc.add(new Label("Mã đơn:"), 0, 0);
        pnlBoLoc.add(txtMaDonHang, 1, 0);
        pnlBoLoc.add(new Label("Nhà cung cấp:"), 2, 0);
        pnlBoLoc.add(txtNhaCungCap, 3, 0);
        pnlBoLoc.add(new Label("Từ ngày:"), 0, 1);
        pnlBoLoc.add(dpTuNgay, 1, 1);
        pnlBoLoc.add(new Label("Đến ngày:"), 2, 1);
        pnlBoLoc.add(dpDenNgay, 3, 1);

        HBox pnlNut = new HBox(8);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);
        Button btnTimKiem = new Button("Tìm kiếm");
        btnTimKiem.getStyleClass().add("nut-chinh");
        Button btnXoaBoLoc = new Button("Xóa bộ lọc");
        btnXoaBoLoc.getStyleClass().add("nut-phu");
        btnTimKiem.setOnAction(event -> timKiem());
        btnXoaBoLoc.setOnAction(event -> {
            txtMaDonHang.clear();
            txtNhaCungCap.clear();
            dpTuNgay.setValue(null);
            dpDenNgay.setValue(null);
            timKiem();
        });
        pnlNut.getChildren().addAll(btnXoaBoLoc, btnTimKiem);
        pnlBoLoc.add(pnlNut, 0, 2, 4, 1);
        return pnlBoLoc;
    }

    private void taoCotKetQua() {
        TableColumn<DonNhap, String> colMa = new TableColumn<>("Mã đơn nhập");
        colMa.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().maDonHang()));
        TableColumn<DonNhap, String> colNhaCungCap = new TableColumn<>("Nhà cung cấp");
        colNhaCungCap.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().tenNhaCungCap()));
        TableColumn<DonNhap, String> colNgayLap = new TableColumn<>("Ngày lập");
        colNgayLap.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().ngayLap().format(DINH_DANG_NGAY)));
        TableColumn<DonNhap, String> colTongTien = new TableColumn<>("Tổng tiền");
        colTongTien.setStyle("-fx-alignment: CENTER-RIGHT;");
        colTongTien.setCellValueFactory(cell -> {
            NumberFormat numberFormat = NumberFormat.getNumberInstance(LOCALE_VI_VN);
            numberFormat.setMaximumFractionDigits(0);
            return new SimpleStringProperty(numberFormat.format(cell.getValue().tongTien()) + " đ");
        });
        tblKetQua.getColumns().addAll(java.util.List.of(colMa, colNhaCungCap, colNgayLap, colTongTien));
    }

    private void timKiem() {
        if (dpTuNgay.getValue() != null && dpDenNgay.getValue() != null
                && dpTuNgay.getValue().isAfter(dpDenNgay.getValue())) {
            hienThiLoi("Ngày bắt đầu không được sau ngày kết thúc.");
            return;
        }
        try {
            tblKetQua.setItems(FXCollections.observableArrayList(donHangController.timKiemDonNhap(
                    txtMaDonHang.getText(), txtNhaCungCap.getText(), dpTuNgay.getValue(), dpDenNgay.getValue())));
        } catch (RuntimeException e) {
            hienThiLoi("Không tìm kiếm được đơn nhập: " + e.getMessage());
        }
    }

    private void moChiTiet() {
        DonNhap donNhap = tblKetQua.getSelectionModel().getSelectedItem();
        if (donNhap == null) {
            return;
        }
        try {
            XemChiTietDonNhapView dialog = new XemChiTietDonNhapView(donNhap,
                    donHangController.layChiTietDonHang(donNhap.maDonHang()));
            if (getScene() != null) {
                dialog.initOwner(getScene().getWindow());
            }
            dialog.showAndWait();
        } catch (RuntimeException e) {
            hienThiLoi("Không tải được chi tiết đơn nhập: " + e.getMessage());
        }
    }

    private void hienThiLoi(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi tìm kiếm");
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        if (getScene() != null) {
            alert.initOwner(getScene().getWindow());
        }
        alert.showAndWait();
    }
}