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

public class TimKiemSanPhamView extends VBox {

    private static final Locale LOCALE_VI_VN = new Locale("vi", "VN");

    private final SanPhamController sanPhamController;

    private final TextField txtTuKhoa = new TextField();
    private final ComboBox<SanPham.NhaCungCapSimple> cboNhaCungCap = new ComboBox<>();
    private final ComboBox<String> cboTrangThai = new ComboBox<>();
    private final Button btnTimKiem = new Button("Tìm kiếm");
    private final Button btnLamMoi = new Button("Làm mới");
    private final Label lblKetQua = new Label("Tổng số: 0 sản phẩm");

    private final TableView<SanPham> tblDanhSach = new TableView<>();

    public TimKiemSanPhamView() {
        this(new SanPhamController());
    }

    public TimKiemSanPhamView(SanPhamController sanPhamController) {
        this.sanPhamController = sanPhamController;

        setSpacing(14);
        setPadding(new Insets(18));
        getStyleClass().add("khung-phan-he");

        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("TÌM KIẾM SẢN PHẨM");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Tra cứu sản phẩm theo mã, tên, mã vạch, nhà cung cấp và trạng thái kinh doanh.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        VBox pnlKhungChinh = new VBox(12);
        pnlKhungChinh.getStyleClass().add("hop-noi-dung");
        VBox.setVgrow(pnlKhungChinh, Priority.ALWAYS);

        HBox pnlBoLoc = taoThanhBoLoc();
        VBox pnlBang = taoKhungBang();
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        pnlKhungChinh.getChildren().addAll(pnlBoLoc, pnlBang);
        getChildren().addAll(pnlTieuDe, pnlKhungChinh);

        taiDanhSachNhaCungCap();
        taiDanhSach();
    }

    private HBox taoThanhBoLoc() {
        HBox pnlLoc = new HBox(10);
        pnlLoc.getStyleClass().add("thanh-tac-vu");
        pnlLoc.setAlignment(Pos.CENTER_LEFT);

        txtTuKhoa.setPromptText("Mã SP, tên SP, mã vạch...");
        txtTuKhoa.setPrefWidth(220);
        txtTuKhoa.setOnAction(e -> taiDanhSach());

        cboNhaCungCap.setPromptText("Tất cả NCC");
        cboNhaCungCap.setPrefWidth(200);
        cboNhaCungCap.setOnAction(e -> taiDanhSach());

        cboTrangThai.setItems(FXCollections.observableArrayList("Tất cả trạng thái", "Đang kinh doanh", "Ngừng kinh doanh"));
        cboTrangThai.setValue("Tất cả trạng thái");
        cboTrangThai.setOnAction(e -> taiDanhSach());

        btnTimKiem.getStyleClass().add("nut-chinh");
        btnTimKiem.setOnAction(e -> taiDanhSach());

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> {
            txtTuKhoa.clear();
            cboNhaCungCap.setValue(null);
            cboTrangThai.setValue("Tất cả trạng thái");
            taiDanhSach();
        });

        pnlLoc.getChildren().addAll(
                new Label("Từ khóa:"), txtTuKhoa,
                new Label("NCC:"), cboNhaCungCap,
                new Label("Trạng thái:"), cboTrangThai,
                btnTimKiem, btnLamMoi
        );
        return pnlLoc;
    }

    private VBox taoKhungBang() {
        VBox pnlKhung = new VBox(8);

        HBox pnlTacVuDuoi = new HBox(10);
        pnlTacVuDuoi.setAlignment(Pos.CENTER_LEFT);
        lblKetQua.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        pnlTacVuDuoi.getChildren().addAll(lblKetQua, spacer);

        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Không tìm thấy sản phẩm nào"));
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);

        TableColumn<SanPham, String> colMa = new TableColumn<>("Mã sản phẩm");
        colMa.setPrefWidth(160);
        colMa.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMaSanPham()));

        TableColumn<SanPham, String> colTen = new TableColumn<>("Tên sản phẩm");
        colTen.setPrefWidth(220);
        colTen.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTenSanPham()));

        TableColumn<SanPham, String> colMaVach = new TableColumn<>("Mã vạch");
        colMaVach.setPrefWidth(130);
        colMaVach.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMaVach()));

        TableColumn<SanPham, String> colNcc = new TableColumn<>("Nhà cung cấp");
        colNcc.setPrefWidth(200);
        colNcc.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTenNhaCungCap() != null ? c.getValue().getTenNhaCungCap() : c.getValue().getMaNhaCungCap()));

        TableColumn<SanPham, String> colGia = new TableColumn<>("Giá bán");
        colGia.setPrefWidth(120);
        colGia.setStyle("-fx-alignment: CENTER-RIGHT;");
        colGia.setCellValueFactory(c -> new SimpleStringProperty(dinhDangTien(c.getValue().getGiaBan())));

        TableColumn<SanPham, String> colDvt = new TableColumn<>("ĐVT");
        colDvt.setPrefWidth(80);
        colDvt.setStyle("-fx-alignment: CENTER;");
        colDvt.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDonViTinh()));

        TableColumn<SanPham, String> colTon = new TableColumn<>("Tồn kho");
        colTon.setPrefWidth(90);
        colTon.setStyle("-fx-alignment: CENTER-RIGHT;");
        colTon.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getSoLuongTon())));

        TableColumn<SanPham, String> colNguong = new TableColumn<>("Ngưỡng an toàn");
        colNguong.setPrefWidth(120);
        colNguong.setStyle("-fx-alignment: CENTER-RIGHT;");
        colNguong.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getNguongTonToiThieu())));

        TableColumn<SanPham, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setPrefWidth(130);
        colTrangThai.setStyle("-fx-alignment: CENTER;");
        colTrangThai.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isTrangThai() ? "Đang kinh doanh" : "Ngừng kinh doanh"));

        tblDanhSach.getColumns().addAll(List.of(colMa, colTen, colMaVach, colNcc, colGia, colDvt, colTon, colNguong, colTrangThai));

        tblDanhSach.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(SanPham item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (!item.isTrangThai()) {
                    setStyle("-fx-opacity: 0.55;");
                } else {
                    setStyle("");
                }
            }
        });

        pnlKhung.getChildren().addAll(pnlTacVuDuoi, tblDanhSach);
        return pnlKhung;
    }

    private void taiDanhSachNhaCungCap() {
        try {
            List<SanPham.NhaCungCapSimple> ds = sanPhamController.layDanhSachNhaCungCap();
            cboNhaCungCap.getItems().setAll(ds);
        } catch (Exception ignored) {
        }
    }

    public void taiDanhSach() {
        try {
            String tuKhoa = txtTuKhoa.getText() != null ? txtTuKhoa.getText().trim() : "";
            SanPham.NhaCungCapSimple ncc = cboNhaCungCap.getValue();
            String maNcc = ncc != null ? ncc.maNhaCungCap() : null;

            Boolean chiKinhDoanh = null;
            if ("Đang kinh doanh".equals(cboTrangThai.getValue())) {
                chiKinhDoanh = true;
            } else if ("Ngừng kinh doanh".equals(cboTrangThai.getValue())) {
                chiKinhDoanh = false;
            }

            List<SanPham> ketQua = sanPhamController.timKiemSanPham(tuKhoa, maNcc, chiKinhDoanh);
            tblDanhSach.setItems(FXCollections.observableArrayList(ketQua));
            lblKetQua.setText("Tổng số: " + ketQua.size() + " sản phẩm");
        } catch (Exception e) {
            hienThiLoi("Lỗi tìm kiếm sản phẩm: " + e.getMessage());
        }
    }

    private String dinhDangTien(BigDecimal soTien) {
        if (soTien == null) return "0 đ";
        NumberFormat nf = NumberFormat.getNumberInstance(LOCALE_VI_VN);
        nf.setMaximumFractionDigits(0);
        return nf.format(soTien) + " đ";
    }

    private void hienThiLoi(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Thông báo lỗi");
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        ganChuDialog(alert);
        alert.showAndWait();
    }

    private void ganChuDialog(Alert alert) {
        if (getScene() != null && getScene().getWindow() != null) {
            alert.initOwner(getScene().getWindow());
        }
    }
}
