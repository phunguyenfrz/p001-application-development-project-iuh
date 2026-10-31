package com.petshop.view.sanpham;

import com.petshop.controller.SanPhamController;
import com.petshop.model.SanPham;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Màn hình tra cứu, tìm kiếm sản phẩm theo từ khóa và trạng thái kinh doanh (PRD_SEARCH).
 * Thiết kế giao diện đồng bộ hoàn toàn theo phân hệ Dịch vụ (TimKiemDichVuView).
 */
public class TimKiemSanPhamView extends VBox {

    private final SanPhamController sanPhamController = new SanPhamController();

    private final TextField txtTuKhoa = new TextField();
    private final ComboBox<String> cboLocTrangThai = new ComboBox<>();
    private final Button btnTimKiem = new Button("Tìm kiếm");
    private final Button btnLamMoi = new Button("Làm mới");
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
        VBox.setVgrow(pnlKhungChinh, Priority.ALWAYS);

        HBox pnlThanhTimKiem = taoThanhTimKiem();
        VBox pnlBang = taoKhungBangDanhSach();
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        pnlKhungChinh.getChildren().addAll(pnlThanhTimKiem, pnlBang);
        getChildren().addAll(pnlTieuDeMan, pnlKhungChinh);

        xuLyTimKiem();
    }

    private HBox taoThanhTimKiem() {
        HBox pnlCongCu = new HBox(10);
        pnlCongCu.getStyleClass().add("thanh-tac-vu");
        pnlCongCu.setAlignment(Pos.CENTER_LEFT);

        Label lblTim = new Label("Từ khóa:");
        txtTuKhoa.setPromptText("Nhập mã hoặc tên sản phẩm...");
        txtTuKhoa.setPrefWidth(240);
        txtTuKhoa.setOnAction(e -> xuLyTimKiem());

        Label lblTrangThai = new Label("Trạng thái:");
        cboLocTrangThai.setItems(FXCollections.observableArrayList(
                "Tất cả", "Đang kinh doanh", "Sắp hết hàng", "Hết hàng", "Ngừng kinh doanh"
        ));
        cboLocTrangThai.setValue("Tất cả");
        cboLocTrangThai.setPrefWidth(150);
        cboLocTrangThai.setOnAction(e -> xuLyTimKiem());

        btnTimKiem.getStyleClass().add("nut-chinh");
        btnTimKiem.setOnAction(e -> xuLyTimKiem());

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> {
            txtTuKhoa.clear();
            cboLocTrangThai.setValue("Tất cả");
            xuLyTimKiem();
        });

        Region pnlKhoangCach = new Region();
        HBox.setHgrow(pnlKhoangCach, Priority.ALWAYS);

        lblKetQua.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");

        pnlCongCu.getChildren().addAll(
                lblTim, txtTuKhoa,
                lblTrangThai, cboLocTrangThai,
                btnTimKiem, btnLamMoi,
                pnlKhoangCach, lblKetQua
        );
        return pnlCongCu;
    }

    private VBox taoKhungBangDanhSach() {
        VBox pnlBang = new VBox(8);

        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Không tìm thấy sản phẩm phù hợp"));
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);

        TableColumn<SanPham, String> colMa = new TableColumn<>("Mã sản phẩm");
        colMa.setPrefWidth(140);
        colMa.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaSanPham()));

        TableColumn<SanPham, String> colTen = new TableColumn<>("Tên sản phẩm");
        colTen.setPrefWidth(220);
        colTen.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTenSanPham()));

        TableColumn<SanPham, String> colLoai = new TableColumn<>("Loại / ĐVT");
        colLoai.setPrefWidth(90);
        colLoai.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDonViTinh()));

        TableColumn<SanPham, String> colTon = new TableColumn<>("Số lượng tồn");
        colTon.setPrefWidth(110);
        colTon.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");
        colTon.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getSoLuongTon())));

        TableColumn<SanPham, String> colNguong = new TableColumn<>("Ngưỡng an toàn");
        colNguong.setPrefWidth(120);
        colNguong.setStyle("-fx-alignment: CENTER-RIGHT;");
        colNguong.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getNguongTonToiThieu())));

        TableColumn<SanPham, String> colTinhTrang = new TableColumn<>("Tình trạng");
        colTinhTrang.setPrefWidth(140);
        colTinhTrang.setCellValueFactory(cell -> {
            if (cell.getValue().getSoLuongTon() == 0) {
                return new SimpleStringProperty("✖ HẾT HÀNG");
            }
            boolean sapHet = cell.getValue().isSapHetHang();
            return new SimpleStringProperty(sapHet ? "⚠ SẮP HẾT HÀNG" : "Ổn định");
        });

        TableColumn<SanPham, String> colGiaBan = new TableColumn<>("Giá bán");
        colGiaBan.setPrefWidth(120);
        colGiaBan.setStyle("-fx-alignment: CENTER-RIGHT;");
        colGiaBan.setCellValueFactory(cell -> new SimpleStringProperty(
                ThemSanPhamView.dinhDangTien(cell.getValue().getGiaBan().doubleValue())
        ));

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
                colMa, colTen, colLoai, colTon, colNguong, colTinhTrang, colGiaBan, colNcc, colTrangThai
        ));

        // Hết hàng và sắp hết hàng đổi màu nền dòng giống bên Theo dõi tồn kho
        tblDanhSach.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(SanPham item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setOpacity(1.0);
                    setStyle("");
                } else if (!item.isTrangThai()) {
                    setOpacity(0.5);
                    setStyle("-fx-background-color: #f1f5f9;");
                } else if (item.getSoLuongTon() == 0) {
                    setOpacity(1.0);
                    setStyle("-fx-background-color: #fee2e2;");
                } else if (item.isSapHetHang()) {
                    setOpacity(1.0);
                    setStyle("-fx-background-color: #fef2f2;");
                } else {
                    setOpacity(1.0);
                    setStyle("");
                }
            }
        });

pnlBang.getChildren().add(tblDanhSach);
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);
        return pnlBang;
    }

    private void xuLyTimKiem() {
        try {
            String tuKhoa = txtTuKhoa.getText().trim();
            String trangThaiChon = cboLocTrangThai.getValue();
            Boolean trangThai = null;
            if ("Đang kinh doanh".equals(trangThaiChon) || "Hết hàng".equals(trangThaiChon)) {
                trangThai = true;
            } else if ("Ngừng kinh doanh".equals(trangThaiChon)) {
                trangThai = false;
            }

            List<SanPham> ds = sanPhamController.timKiemSanPham(tuKhoa, trangThai);
            if ("Hết hàng".equals(trangThaiChon)) {
                ds = ds.stream().filter(sp -> sp.getSoLuongTon() <= 0).toList();
            } else if ("Đang kinh doanh".equals(trangThaiChon)) {
                ds = ds.stream().filter(sp -> sp.getSoLuongTon() > 0).toList();
            }
            tblDanhSach.setItems(FXCollections.observableArrayList(ds));
            lblKetQua.setText("Tổng số: " + ds.size() + " sản phẩm");
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienThiCanhBao("Lỗi tìm kiếm: " + ex.getMessage());
        }
    }

    private void hienThiCanhBao(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}
