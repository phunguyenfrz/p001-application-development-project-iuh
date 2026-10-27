package com.petshop.view.sanpham;

import com.petshop.controller.SanPhamController;
import com.petshop.model.SanPham;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

/**
 * Màn hình tra cứu, tìm kiếm sản phẩm và xóa mềm (PRD_SEARCH).
 * Tiền tố điều khiển: lbl, txt, btn, tbl, cbo.
 */
public class TimKiemSanPhamView extends VBox {

    private final SanPhamController sanPhamController = new SanPhamController();
    private final DecimalFormat dinhDangTien = new DecimalFormat("#,### đ");

    private final TextField txtTuKhoa = new TextField();
    private final ComboBox<String> cboLocTrangThai = new ComboBox<>();
    private final Button btnTimKiem = new Button("Tìm kiếm");
    private final Button btnLamMoi = new Button("Làm mới");
    private final Button btnXoaMem = new Button("Ngừng kinh doanh");
    private final Label lblKetQua = new Label("Tổng số: 0 sản phẩm");

    private final TableView<SanPham> tblDanhSach = new TableView<>();

    public TimKiemSanPhamView() {
        setSpacing(14);
        getStyleClass().add("khung-phan-he");

        VBox pnlTieuDeMan = new VBox(4);
        Label lblTieuDe = new Label("SẢN PHẨM - TÌM KIẾM SẢN PHẨM");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Tra cứu danh mục sản phẩm theo mã, tên hoặc mã vạch và lọc theo trạng thái kinh doanh.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDeMan.getChildren().addAll(lblTieuDe, lblMoTa);

        VBox pnlKhungChinh = new VBox(12);
        pnlKhungChinh.getStyleClass().add("hop-noi-dung");
        pnlKhungChinh.setPadding(new Insets(14));
        VBox.setVgrow(pnlKhungChinh, Priority.ALWAYS);

        HBox pnlThanhTimKiem = taoThanhTimKiem();
        VBox pnlBang = taoKhungBang();
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        pnlKhungChinh.getChildren().addAll(pnlThanhTimKiem, pnlBang);
        getChildren().addAll(pnlTieuDeMan, pnlKhungChinh);

        btnXoaMem.setDisable(true);
        tblDanhSach.getSelectionModel().selectedItemProperty().addListener((obs, cu, moi) -> {
            btnXoaMem.setDisable(moi == null || !moi.isTrangThai());
        });

        xuLyTimKiem();
    }

    private HBox taoThanhTimKiem() {
        HBox pnlThanh = new HBox(10);
        pnlThanh.setAlignment(Pos.CENTER_LEFT);

        txtTuKhoa.setPromptText("Nhập mã, tên hoặc mã vạch...");
        txtTuKhoa.setPrefWidth(240);

        cboLocTrangThai.getItems().addAll("Tất cả trạng thái", "Đang kinh doanh", "Ngừng kinh doanh");
        cboLocTrangThai.setValue("Tất cả trạng thái");
        cboLocTrangThai.setPrefWidth(160);

        btnTimKiem.getStyleClass().add("nut-chinh");
        btnTimKiem.setOnAction(e -> xuLyTimKiem());
        txtTuKhoa.setOnAction(e -> xuLyTimKiem());

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> {
            txtTuKhoa.clear();
            cboLocTrangThai.setValue("Tất cả trạng thái");
            xuLyTimKiem();
        });

        btnXoaMem.getStyleClass().add("nut-phu");
        btnXoaMem.setOnAction(e -> xuLyXoaMem());

        Region pnlKhoangCach = new Region();
        HBox.setHgrow(pnlKhoangCach, Priority.ALWAYS);

        lblKetQua.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");

        pnlThanh.getChildren().addAll(
                new Label("Từ khóa:"), txtTuKhoa,
                new Label("Trạng thái:"), cboLocTrangThai,
                btnTimKiem, btnLamMoi, btnXoaMem,
                pnlKhoangCach, lblKetQua
        );
        return pnlThanh;
    }

    private VBox taoKhungBang() {
        VBox pnlBang = new VBox();

        TableColumn<SanPham, String> colMa = new TableColumn<>("Mã sản phẩm");
        colMa.setPrefWidth(140);
        colMa.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaSanPham()));

        TableColumn<SanPham, String> colTen = new TableColumn<>("Tên sản phẩm");
        colTen.setPrefWidth(230);
        colTen.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTenSanPham()));

        TableColumn<SanPham, String> colMaVach = new TableColumn<>("Mã vạch");
        colMaVach.setPrefWidth(120);
        colMaVach.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaVach()));

        TableColumn<SanPham, String> colGiaBan = new TableColumn<>("Giá bán");
        colGiaBan.setPrefWidth(120);
        colGiaBan.setStyle("-fx-alignment: CENTER-RIGHT;");
        colGiaBan.setCellValueFactory(cell -> new SimpleStringProperty(dinhDangTien.format(cell.getValue().getGiaBan())));

        TableColumn<SanPham, String> colDonVi = new TableColumn<>("Đơn vị");
        colDonVi.setPrefWidth(80);
        colDonVi.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDonViTinh()));

        TableColumn<SanPham, String> colTon = new TableColumn<>("Tồn kho");
        colTon.setPrefWidth(90);
        colTon.setStyle("-fx-alignment: CENTER-RIGHT;");
        colTon.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getSoLuongTon())));

        TableColumn<SanPham, String> colNguong = new TableColumn<>("Ngưỡng an toàn");
        colNguong.setPrefWidth(110);
        colNguong.setStyle("-fx-alignment: CENTER-RIGHT;");
        colNguong.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getNguongTonToiThieu())));

        TableColumn<SanPham, String> colNcc = new TableColumn<>("Nhà cung cấp");
        colNcc.setPrefWidth(180);
        colNcc.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getTenNhaCungCap() != null ? cell.getValue().getTenNhaCungCap() : "Chưa xác định"
        ));

        TableColumn<SanPham, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setPrefWidth(130);
        colTrangThai.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().isTrangThai() ? "Đang kinh doanh" : "Ngừng kinh doanh"
        ));

        tblDanhSach.getColumns().setAll(List.of(
                colMa, colTen, colMaVach, colGiaBan, colDonVi, colTon, colNguong, colNcc, colTrangThai
        ));
        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Không tìm thấy sản phẩm phù hợp."));

        pnlBang.getChildren().add(tblDanhSach);
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);
        return pnlBang;
    }

    private void xuLyTimKiem() {
        try {
            String tuKhoa = txtTuKhoa.getText().trim();
            String trangThaiChon = cboLocTrangThai.getValue();
            Boolean trangThai = null;
            if ("Đang kinh doanh".equals(trangThaiChon)) {
                trangThai = true;
            } else if ("Ngừng kinh doanh".equals(trangThaiChon)) {
                trangThai = false;
            }

            List<SanPham> ds = sanPhamController.timKiemSanPham(tuKhoa, trangThai);
            tblDanhSach.setItems(FXCollections.observableArrayList(ds));
            lblKetQua.setText("Tổng số: " + ds.size() + " sản phẩm");
        } catch (Exception ex) {
            hienThiCanhBao("Lỗi tìm kiếm", ex.getMessage());
        }
    }

    private void xuLyXoaMem() {
        SanPham sp = tblDanhSach.getSelectionModel().getSelectedItem();
        if (sp == null) return;

        Alert xacNhan = new Alert(Alert.AlertType.CONFIRMATION);
        xacNhan.setTitle("Xác nhận ngừng kinh doanh");
        xacNhan.setHeaderText(null);
        xacNhan.setContentText("Bạn có chắc muốn chuyển sản phẩm \"" + sp.getTenSanPham() + "\" sang Ngừng kinh doanh không?");

        Optional<ButtonType> ketQua = xacNhan.showAndWait();
        if (ketQua.isPresent() && ketQua.get() == ButtonType.OK) {
            try {
                boolean thanhCong = sanPhamController.xoaMemSanPham(sp.getMaSanPham());
                if (thanhCong) {
                    hienThiThongBao("Thông báo", "Đã ngừng kinh doanh sản phẩm!");
                    xuLyTimKiem();
                }
            } catch (Exception ex) {
                hienThiCanhBao("Lỗi", ex.getMessage());
            }
        }
    }

    private void hienThiThongBao(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }

    private void hienThiCanhBao(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}

