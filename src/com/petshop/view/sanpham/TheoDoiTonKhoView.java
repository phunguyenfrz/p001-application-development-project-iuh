package com.petshop.view.sanpham;

import com.petshop.controller.SanPhamController;
import com.petshop.model.SanPham;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TheoDoiTonKhoView extends VBox {

    private static final Locale LOCALE_VI_VN = new Locale("vi", "VN");

    private final SanPhamController sanPhamController;

    private final Label lblTongSoMatHang = new Label("0");
    private final Label lblTongSoLuongTon = new Label("0");
    private final Label lblCanNhapHang = new Label("0");

    private final CheckBox chkDuoiNguongAnToan = new CheckBox("Chỉ hiển thị sản phẩm dưới ngưỡng an toàn (Cần nhập hàng)");
    private final TextField txtTimKiem = new TextField();
    private final Button btnLamMoi = new Button("Làm mới dữ liệu");

    private final TableView<SanPham> tblTonKho = new TableView<>();

    public TheoDoiTonKhoView() {
        this(new SanPhamController());
    }

    public TheoDoiTonKhoView(SanPhamController sanPhamController) {
        this.sanPhamController = sanPhamController;

        setSpacing(14);
        setPadding(new Insets(18));
        getStyleClass().add("khung-phan-he");

        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("THEO DÕI TỒN KHO & BÁO CÁO SẢN PHẨM");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Giám sát mức độ tồn kho, cảnh báo các mặt hàng dưới ngưỡng an toàn và thống kê tổng hợp.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        HBox pnlTheThongKe = taoCacTheThongKe();

        VBox pnlKhungChinh = new VBox(12);
        pnlKhungChinh.getStyleClass().add("hop-noi-dung");
        VBox.setVgrow(pnlKhungChinh, Priority.ALWAYS);

        HBox pnlTacVu = taoThanhTacVu();
        VBox pnlBang = taoKhungBang();
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        pnlKhungChinh.getChildren().addAll(pnlTacVu, pnlBang);
        getChildren().addAll(pnlTieuDe, pnlTheThongKe, pnlKhungChinh);

        taiDuLieu();
    }

    private HBox taoCacTheThongKe() {
        HBox pnlThe = new HBox(16);
        pnlThe.setAlignment(Pos.CENTER_LEFT);

        VBox theMatHang = taoHopThongKe("TỔNG MÃ HÀNG", lblTongSoMatHang, "#1976d2");
        VBox theTonKho = taoHopThongKe("TỔNG TỒN KHO", lblTongSoLuongTon, "#2e7d32");
        VBox theCanNhap = taoHopThongKe("CẦN NHẬP BỔ SUNG", lblCanNhapHang, "#c62828");

        HBox.setHgrow(theMatHang, Priority.ALWAYS);
        HBox.setHgrow(theTonKho, Priority.ALWAYS);
        HBox.setHgrow(theCanNhap, Priority.ALWAYS);

        pnlThe.getChildren().addAll(theMatHang, theTonKho, theCanNhap);
        return pnlThe;
    }

    private VBox taoHopThongKe(String tieuDe, Label lblGiaTri, String mauSac) {
        VBox box = new VBox(6);
        box.setPadding(new Insets(14, 18, 14, 18));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #e0e0e0; -fx-border-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 4, 0, 0, 2);");

        Label lblNhan = new Label(tieuDe);
        lblNhan.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: #666;");

        lblGiaTri.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + mauSac + ";");

        box.getChildren().addAll(lblNhan, lblGiaTri);
        return box;
    }

    private HBox taoThanhTacVu() {
        HBox pnlTacVu = new HBox(14);
        pnlTacVu.setAlignment(Pos.CENTER_LEFT);

        txtTimKiem.setPromptText("Lọc nhanh theo mã hoặc tên...");
        txtTimKiem.setPrefWidth(260);
        txtTimKiem.textProperty().addListener((obs, oldV, newV) -> taiDuLieu());

        chkDuoiNguongAnToan.setStyle("-fx-font-weight: bold; -fx-text-fill: #c62828;");
        chkDuoiNguongAnToan.setOnAction(e -> taiDuLieu());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> {
            txtTimKiem.clear();
            chkDuoiNguongAnToan.setSelected(false);
            taiDuLieu();
        });

        pnlTacVu.getChildren().addAll(txtTimKiem, chkDuoiNguongAnToan, spacer, btnLamMoi);
        return pnlTacVu;
    }

    private VBox taoKhungBang() {
        VBox pnlBang = new VBox(8);

        tblTonKho.getStyleClass().add("bang-du-lieu");
        tblTonKho.setPlaceholder(new Label("Không có dữ liệu tồn kho"));
        VBox.setVgrow(tblTonKho, Priority.ALWAYS);

        TableColumn<SanPham, String> colMa = new TableColumn<>("Mã sản phẩm");
        colMa.setPrefWidth(160);
        colMa.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMaSanPham()));

        TableColumn<SanPham, String> colTen = new TableColumn<>("Tên sản phẩm");
        colTen.setPrefWidth(240);
        colTen.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTenSanPham()));

        TableColumn<SanPham, String> colNcc = new TableColumn<>("Nhà cung cấp");
        colNcc.setPrefWidth(200);
        colNcc.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTenNhaCungCap() != null ? c.getValue().getTenNhaCungCap() : c.getValue().getMaNhaCungCap()));

        TableColumn<SanPham, String> colDvt = new TableColumn<>("ĐVT");
        colDvt.setPrefWidth(80);
        colDvt.setStyle("-fx-alignment: CENTER;");
        colDvt.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDonViTinh()));

        TableColumn<SanPham, String> colTon = new TableColumn<>("Số lượng tồn");
        colTon.setPrefWidth(110);
        colTon.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");
        colTon.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getSoLuongTon())));

        TableColumn<SanPham, String> colNguong = new TableColumn<>("Ngưỡng tối thiểu");
        colNguong.setPrefWidth(130);
        colNguong.setStyle("-fx-alignment: CENTER-RIGHT;");
        colNguong.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getNguongTonToiThieu())));

        TableColumn<SanPham, String> colTrangThaiTon = new TableColumn<>("Đánh giá tồn kho");
        colTrangThaiTon.setPrefWidth(170);
        colTrangThaiTon.setStyle("-fx-alignment: CENTER;");
        colTrangThaiTon.setCellValueFactory(c -> {
            boolean duoiNguong = c.getValue().isDuoiNguongAnToan();
            return new SimpleStringProperty(duoiNguong ? "Cảnh báo thiếu hàng" : "An toàn");
        });

        tblTonKho.getColumns().addAll(List.of(colMa, colTen, colNcc, colDvt, colTon, colNguong, colTrangThaiTon));

        tblTonKho.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(SanPham item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (item.isDuoiNguongAnToan()) {
                    setStyle("-fx-background-color: #ffebee; -fx-font-weight: bold;");
                } else {
                    setStyle("");
                }
            }
        });

        pnlBang.getChildren().add(tblTonKho);
        return pnlBang;
    }

    public void taiDuLieu() {
        try {
            boolean chiDuoiNguong = chkDuoiNguongAnToan.isSelected();
            List<SanPham> tatCaDangBan = sanPhamController.layDanhSachTonKho(false);

            int tongMatHang = tatCaDangBan.size();
            int tongTon = 0;
            int soLuongCanNhap = 0;
            for (SanPham sp : tatCaDangBan) {
                tongTon += sp.getSoLuongTon();
                if (sp.isDuoiNguongAnToan()) {
                    soLuongCanNhap++;
                }
            }

            lblTongSoMatHang.setText(String.format("%,d", tongMatHang));
            lblTongSoLuongTon.setText(String.format("%,d", tongTon));
            lblCanNhapHang.setText(String.format("%,d", soLuongCanNhap));

            String tuKhoa = txtTimKiem.getText() != null ? txtTimKiem.getText().trim().toLowerCase() : "";
            List<SanPham> danhSachHienThi = (chiDuoiNguong ? sanPhamController.layDanhSachTonKho(true) : tatCaDangBan)
                    .stream()
                    .filter(sp -> tuKhoa.isEmpty()
                            || (sp.getMaSanPham() != null && sp.getMaSanPham().toLowerCase().contains(tuKhoa))
                            || (sp.getTenSanPham() != null && sp.getTenSanPham().toLowerCase().contains(tuKhoa))
                            || (sp.getMaVach() != null && sp.getMaVach().toLowerCase().contains(tuKhoa)))
                    .toList();

            tblTonKho.setItems(FXCollections.observableArrayList(danhSachHienThi));
        } catch (Exception e) {
            hienThiLoi("Lỗi tải thông tin tồn kho: " + e.getMessage());
        }
    }

    private void hienThiLoi(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Thông báo lỗi");
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        if (getScene() != null && getScene().getWindow() != null) {
            alert.initOwner(getScene().getWindow());
        }
        alert.showAndWait();
    }
}
