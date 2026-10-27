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

/**
 * Màn hình theo dõi số lượng tồn kho và cảnh báo dưới ngưỡng an toàn (PRD_INVENTORY, PRD_LOW_STOCK).
 * Tiền tố điều khiển: lbl, txt, btn, tbl, chk.
 */
public class TheoDoiTonKhoView extends VBox {

    private final SanPhamController sanPhamController = new SanPhamController();
    private final DecimalFormat dinhDangTien = new DecimalFormat("#,### đ");

    private final CheckBox chkChiLocSapHet = new CheckBox("Chỉ hiển thị sản phẩm sắp hết hàng (tồn <= ngưỡng)");
    private final Button btnLamMoi = new Button("Làm mới dữ liệu");
    private final Label lblThongKeTong = new Label();

    private final TableView<SanPham> tblDanhSach = new TableView<>();

    public TheoDoiTonKhoView() {
        setSpacing(14);
        getStyleClass().add("khung-phan-he");

        VBox pnlTieuDeMan = new VBox(4);
        Label lblTieuDe = new Label("SẢN PHẨM - THEO DÕI TỒN KHO");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Kiểm soát số lượng tồn kho theo thời gian thực và phát hiện kịp thời các sản phẩm sắp hết hàng.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDeMan.getChildren().addAll(lblTieuDe, lblMoTa);

        VBox pnlKhungChinh = new VBox(12);
        pnlKhungChinh.getStyleClass().add("hop-noi-dung");
        pnlKhungChinh.setPadding(new Insets(14));
        VBox.setVgrow(pnlKhungChinh, Priority.ALWAYS);

        HBox pnlThanhCongCu = taoThanhCongCu();
        VBox pnlBang = taoKhungBang();
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        pnlKhungChinh.getChildren().addAll(pnlThanhCongCu, pnlBang);
        getChildren().addAll(pnlTieuDeMan, pnlKhungChinh);

        taiDuLieu();
    }

    private HBox taoThanhCongCu() {
        HBox pnlThanh = new HBox(12);
        pnlThanh.setAlignment(Pos.CENTER_LEFT);

        chkChiLocSapHet.setStyle("-fx-font-weight: bold; -fx-text-fill: #b91c1c;");
        chkChiLocSapHet.setOnAction(e -> taiDuLieu());

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> taiDuLieu());

        Region pnlKhoangCach = new Region();
        HBox.setHgrow(pnlKhoangCach, Priority.ALWAYS);

        lblThongKeTong.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");

        pnlThanh.getChildren().addAll(chkChiLocSapHet, btnLamMoi, pnlKhoangCach, lblThongKeTong);
        return pnlThanh;
    }

    private VBox taoKhungBang() {
        VBox pnlBang = new VBox();

        TableColumn<SanPham, String> colMa = new TableColumn<>("Mã sản phẩm");
        colMa.setPrefWidth(140);
        colMa.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaSanPham()));

        TableColumn<SanPham, String> colTen = new TableColumn<>("Tên sản phẩm");
        colTen.setPrefWidth(240);
        colTen.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTenSanPham()));

        TableColumn<SanPham, String> colDonVi = new TableColumn<>("Đơn vị");
        colDonVi.setPrefWidth(80);
        colDonVi.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDonViTinh()));

        TableColumn<SanPham, String> colTonKho = new TableColumn<>("Số lượng tồn");
        colTonKho.setPrefWidth(110);
        colTonKho.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");
        colTonKho.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getSoLuongTon())));

        TableColumn<SanPham, String> colNguong = new TableColumn<>("Ngưỡng an toàn");
        colNguong.setPrefWidth(120);
        colNguong.setStyle("-fx-alignment: CENTER-RIGHT;");
        colNguong.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getNguongTonToiThieu())));

        TableColumn<SanPham, String> colTinhTrang = new TableColumn<>("Tình trạng");
        colTinhTrang.setPrefWidth(140);
        colTinhTrang.setCellValueFactory(cell -> {
            boolean sapHet = cell.getValue().isSapHetHang();
            return new SimpleStringProperty(sapHet ? "⚠ SẮP HẾT HÀNG" : "Ổn định");
        });

        TableColumn<SanPham, String> colGiaBan = new TableColumn<>("Giá bán");
        colGiaBan.setPrefWidth(110);
        colGiaBan.setStyle("-fx-alignment: CENTER-RIGHT;");
        colGiaBan.setCellValueFactory(cell -> new SimpleStringProperty(dinhDangTien.format(cell.getValue().getGiaBan())));

        TableColumn<SanPham, String> colNcc = new TableColumn<>("Nhà cung cấp");
        colNcc.setPrefWidth(180);
        colNcc.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getTenNhaCungCap() != null ? cell.getValue().getTenNhaCungCap() : "Chưa xác định"
        ));

        tblDanhSach.getColumns().setAll(List.of(
                colMa, colTen, colDonVi, colTonKho, colNguong, colTinhTrang, colGiaBan, colNcc
        ));
        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Không có sản phẩm nào."));

        // Highlight màu đỏ cho hàng có tồn kho dưới ngưỡng an toàn
        tblDanhSach.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(SanPham item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (item.isSapHetHang()) {
                    setStyle("-fx-background-color: #fef2f2;");
                } else {
                    setStyle("");
                }
            }
        });

        pnlBang.getChildren().add(tblDanhSach);
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);
        return pnlBang;
    }

    public void taiDuLieu() {
        try {
            List<SanPham> ds;
            if (chkChiLocSapHet.isSelected()) {
                ds = sanPhamController.layDanhSachSanPhamSapHet();
            } else {
                ds = sanPhamController.layDanhSachSanPham();
            }

            tblDanhSach.setItems(FXCollections.observableArrayList(ds));

            long soSapHet = ds.stream().filter(SanPham::isSapHetHang).count();
            lblThongKeTong.setText("Tổng sản phẩm: " + ds.size() + " | Sắp hết hàng: " + soSapHet);
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Không thể tải dữ liệu tồn kho: " + ex.getMessage());
            alert.showAndWait();
        }
    }
}
