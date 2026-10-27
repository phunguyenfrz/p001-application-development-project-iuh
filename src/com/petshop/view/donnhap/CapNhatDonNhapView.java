package com.petshop.view.donnhap;

import com.petshop.controller.DonHangController;
import com.petshop.model.DonNhap;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CapNhatDonNhapView extends VBox {

    private static final Locale LOCALE_VI_VN = new Locale("vi", "VN");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DonHangController donHangController;
    private final TableView<DonNhap> tblDonNhap = new TableView<>();
    private final Button btnXemChiTiet = new Button("Xem chi tiết");
    private final Button btnXacNhan = new Button("Xác nhận nhận hàng");

    public CapNhatDonNhapView() {
        this(new DonHangController());
    }

    public CapNhatDonNhapView(DonHangController donHangController) {
        this.donHangController = donHangController;
        setSpacing(14);
        setPadding(new Insets(18));
        getStyleClass().add("khung-phan-he");

        Label lblTieuDe = new Label("XÁC NHẬN NHẬN HÀNG");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Chọn đơn chờ nhận để cập nhật tồn kho.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");

        HBox pnlCongCu = new HBox(8);
        pnlCongCu.setAlignment(Pos.CENTER_RIGHT);
        Button btnLamMoi = new Button("Làm mới");
        btnLamMoi.getStyleClass().add("nut-phu");
        btnXemChiTiet.getStyleClass().add("nut-phu");
        btnXacNhan.getStyleClass().add("nut-chinh");
        btnXemChiTiet.disableProperty().bind(tblDonNhap.getSelectionModel().selectedItemProperty().isNull());
        btnXacNhan.disableProperty().bind(tblDonNhap.getSelectionModel().selectedItemProperty().isNull());
        btnLamMoi.setOnAction(event -> taiDanhSach());
        btnXemChiTiet.setOnAction(event -> moChiTiet());
        btnXacNhan.setOnAction(event -> xacNhanNhanHang());
        pnlCongCu.getChildren().addAll(btnLamMoi, btnXemChiTiet, btnXacNhan);

        tblDonNhap.getStyleClass().add("bang-du-lieu");
        tblDonNhap.setPlaceholder(new Label("Không có đơn nhập đang chờ nhận hàng"));
        tblDonNhap.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(tblDonNhap, Priority.ALWAYS);
        taoCotDonNhap();

        VBox pnlNoiDung = new VBox(12, pnlCongCu, tblDonNhap);
        pnlNoiDung.getStyleClass().add("hop-noi-dung");
        VBox.setVgrow(pnlNoiDung, Priority.ALWAYS);
        getChildren().addAll(lblTieuDe, lblMoTa, pnlNoiDung);
        taiDanhSach();
    }

    private void taoCotDonNhap() {
        TableColumn<DonNhap, String> colMa = new TableColumn<>("Mã đơn nhập");
        colMa.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().maDonHang()));
        TableColumn<DonNhap, String> colNhaCungCap = new TableColumn<>("Nhà cung cấp");
        colNhaCungCap.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().tenNhaCungCap()));
        TableColumn<DonNhap, String> colNgayLap = new TableColumn<>("Ngày lập");
        colNgayLap.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().ngayLap().format(DINH_DANG_NGAY)));
        TableColumn<DonNhap, String> colTongTien = new TableColumn<>("Tổng tiền");
        colTongTien.setStyle("-fx-alignment: CENTER-RIGHT;");
        colTongTien.setCellValueFactory(cell -> new SimpleStringProperty(dinhDangTien(cell.getValue().tongTien())));
        tblDonNhap.getColumns().addAll(java.util.List.of(colMa, colNhaCungCap, colNgayLap, colTongTien));
    }

    private void taiDanhSach() {
        try {
            tblDonNhap.setItems(FXCollections.observableArrayList(donHangController.layDonChoNhanHang()));
        } catch (RuntimeException e) {
            hienThiLoi("Không tải được danh sách đơn nhập: " + e.getMessage());
        }
    }

    private void moChiTiet() {
        DonNhap donNhap = tblDonNhap.getSelectionModel().getSelectedItem();
        if (donNhap == null) {
            return;
        }
        try {
            XemChiTietDonNhapView dialog = new XemChiTietDonNhapView(donNhap,
                    donHangController.layChiTietDonHang(donNhap.maDonHang()));
            ganChuDialog(dialog);
            dialog.showAndWait();
        } catch (RuntimeException e) {
            hienThiLoi("Không tải được chi tiết đơn nhập: " + e.getMessage());
        }
    }

    private void xacNhanNhanHang() {
        DonNhap donNhap = tblDonNhap.getSelectionModel().getSelectedItem();
        if (donNhap == null) {
            return;
        }
        Alert xacNhan = new Alert(Alert.AlertType.CONFIRMATION);
        xacNhan.setTitle("Xác nhận nhận hàng");
        xacNhan.setHeaderText("Nhận đơn " + donNhap.maDonHang() + "?");
        xacNhan.setContentText("Tồn kho sẽ được cộng theo số lượng của từng sản phẩm.");
        ganChuDialog(xacNhan);
        if (xacNhan.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            if (!donHangController.xacNhanNhanHang(donNhap.maDonHang())) {
                hienThiLoi("Đơn này không còn ở trạng thái chờ nhận hàng. Hãy làm mới danh sách.");
                taiDanhSach();
                return;
            }
            new Alert(Alert.AlertType.INFORMATION, "Đã xác nhận nhận hàng và cập nhật tồn kho.").showAndWait();
            taiDanhSach();
        } catch (RuntimeException e) {
            hienThiLoi("Không thể xác nhận nhận hàng: " + e.getMessage());
        }
    }

    private String dinhDangTien(BigDecimal soTien) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(LOCALE_VI_VN);
        numberFormat.setMaximumFractionDigits(0);
        return numberFormat.format(soTien) + " đ";
    }

    private void hienThiLoi(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        ganChuDialog(alert);
        alert.showAndWait();
    }

    private void ganChuDialog(Dialog<?> dialog) {
        if (getScene() != null) {
            dialog.initOwner(getScene().getWindow());
        }
    }
}