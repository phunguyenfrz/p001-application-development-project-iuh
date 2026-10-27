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
import java.text.DecimalFormat;
import java.util.List;

/**
 * Màn hình Báo cáo tổng hợp Sản phẩm & Tồn kho (PRD_REPORT).
 * Tiền tố điều khiển: lbl, btn, tbl.
 */
public class BaoCaoSanPhamView extends VBox {

    private final SanPhamController sanPhamController = new SanPhamController();
    private final DecimalFormat dinhDangTien = new DecimalFormat("#,### đ");

    private final Label lblTongSanPham = new Label("0");
    private final Label lblTongGiaTriKho = new Label("0 đ");
    private final Label lblTongSapHet = new Label("0");

    private final TableView<SanPham> tblDanhSach = new TableView<>();
    private final Button btnLamMoi = new Button("🔄 Cập nhật báo cáo");

    public BaoCaoSanPhamView() {
        setSpacing(14);
        getStyleClass().add("khung-phan-he");

        VBox pnlTieuDeMan = new VBox(4);
        Label lblTieuDe = new Label("SẢN PHẨM - BÁO CÁO THỐNG KÊ");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Thống kê tổng giá trị tồn kho, số lượng sản phẩm đang kinh doanh và cảnh báo tồn an toàn.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDeMan.getChildren().addAll(lblTieuDe, lblMoTa);

        VBox pnlKhungChinh = new VBox(14);
        pnlKhungChinh.getStyleClass().add("hop-noi-dung");
        pnlKhungChinh.setPadding(new Insets(16));
        VBox.setVgrow(pnlKhungChinh, Priority.ALWAYS);

        HBox pnlTheThongKe = taoCacTheThongKe();
        VBox pnlBang = taoKhungBang();
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        pnlKhungChinh.getChildren().addAll(pnlTheThongKe, pnlBang);
        getChildren().addAll(pnlTieuDeMan, pnlKhungChinh);

        taiBaoCao();
    }

    private HBox taoCacTheThongKe() {
        HBox pnlCacThe = new HBox(14);

        VBox the1 = taoTheThongKe("📦 Tổng mặt hàng", lblTongSanPham, "Số sản phẩm đang quản lý", false);
        VBox the2 = taoTheThongKe("💰 Tổng giá trị tồn kho", lblTongGiaTriKho, "Tính theo giá niêm yết bán lẻ", true);
        VBox the3 = taoTheThongKe("⚠️ Sản phẩm sắp hết hàng", lblTongSapHet, "Tồn kho dưới ngưỡng an toàn", false);

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> taiBaoCao());

        HBox.setHgrow(the1, Priority.ALWAYS);
        HBox.setHgrow(the2, Priority.ALWAYS);
        HBox.setHgrow(the3, Priority.ALWAYS);

        pnlCacThe.getChildren().addAll(the1, the2, the3);
        return pnlCacThe;
    }

    private VBox taoTheThongKe(String tieuDe, Label lblGiaTri, String moTa, boolean noiBat) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(14, 16, 14, 16));
        if (noiBat) {
            card.getStyleClass().add("the-thong-ke-noi-bat");
            Label lblT = new Label(tieuDe);
            lblT.getStyleClass().add("tieu-de-thong-ke-trang");
            lblGiaTri.getStyleClass().add("gia-tri-thong-ke-trang");
            Label lblM = new Label(moTa);
            lblM.getStyleClass().add("mo-ta-thong-ke-trang");
            card.getChildren().addAll(lblT, lblGiaTri, lblM);
        } else {
            card.getStyleClass().add("the-thong-ke");
            Label lblT = new Label(tieuDe);
            lblT.getStyleClass().add("tieu-de-thong-ke");
            lblGiaTri.getStyleClass().add("gia-tri-thong-ke");
            Label lblM = new Label(moTa);
            lblM.getStyleClass().add("mo-ta-thong-ke");
            card.getChildren().addAll(lblT, lblGiaTri, lblM);
        }
        return card;
    }

    private VBox taoKhungBang() {
        VBox pnlBang = new VBox(10);

        HBox pnlTieuDeBang = new HBox(10);
        pnlTieuDeBang.setAlignment(Pos.CENTER_LEFT);
        Label lblBangTieuDe = new Label("Chi tiết danh mục sản phẩm và tồn kho");
        lblBangTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        pnlTieuDeBang.getChildren().addAll(lblBangTieuDe, spacer, btnLamMoi);

        TableColumn<SanPham, String> colMa = new TableColumn<>("Mã SP");
        colMa.setPrefWidth(130);
        colMa.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaSanPham()));

        TableColumn<SanPham, String> colTen = new TableColumn<>("Tên sản phẩm");
        colTen.setPrefWidth(220);
        colTen.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTenSanPham()));

        TableColumn<SanPham, String> colDonVi = new TableColumn<>("ĐVT");
        colDonVi.setPrefWidth(70);
        colDonVi.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDonViTinh()));

        TableColumn<SanPham, String> colGiaBan = new TableColumn<>("Giá bán");
        colGiaBan.setPrefWidth(110);
        colGiaBan.setStyle("-fx-alignment: CENTER-RIGHT;");
        colGiaBan.setCellValueFactory(cell -> new SimpleStringProperty(dinhDangTien.format(cell.getValue().getGiaBan())));

        TableColumn<SanPham, String> colTon = new TableColumn<>("SL Tồn");
        colTon.setPrefWidth(80);
        colTon.setStyle("-fx-alignment: CENTER-RIGHT;");
        colTon.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getSoLuongTon())));

        TableColumn<SanPham, String> colGiaTri = new TableColumn<>("Thành tiền tồn");
        colGiaTri.setPrefWidth(130);
        colGiaTri.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");
        colGiaTri.setCellValueFactory(cell -> {
            BigDecimal thanhTien = cell.getValue().getGiaBan().multiply(BigDecimal.valueOf(cell.getValue().getSoLuongTon()));
            return new SimpleStringProperty(dinhDangTien.format(thanhTien));
        });

        TableColumn<SanPham, String> colNcc = new TableColumn<>("Nhà cung cấp");
        colNcc.setPrefWidth(180);
        colNcc.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getTenNhaCungCap() != null ? cell.getValue().getTenNhaCungCap() : "Chưa xác định"
        ));

        TableColumn<SanPham, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setPrefWidth(120);
        colTrangThai.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().isTrangThai() ? "Đang bán" : "Ngừng bán"
        ));

        tblDanhSach.getColumns().setAll(List.of(colMa, colTen, colDonVi, colGiaBan, colTon, colGiaTri, colNcc, colTrangThai));
        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Chưa có dữ liệu sản phẩm."));

        pnlBang.getChildren().addAll(pnlTieuDeBang, tblDanhSach);
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);
        return pnlBang;
    }

    public void taiBaoCao() {
        try {
            List<SanPham> ds = sanPhamController.layDanhSachSanPham();
            tblDanhSach.setItems(FXCollections.observableArrayList(ds));

            lblTongSanPham.setText(String.valueOf(ds.size()));

            BigDecimal tongTienKho = BigDecimal.ZERO;
            long sapHet = 0;
            for (SanPham sp : ds) {
                if (sp.isTrangThai()) {
                    BigDecimal giaTriSp = sp.getGiaBan().multiply(BigDecimal.valueOf(sp.getSoLuongTon()));
                    tongTienKho = tongTienKho.add(giaTriSp);
                    if (sp.isSapHetHang()) {
                        sapHet++;
                    }
                }
            }

            lblTongGiaTriKho.setText(dinhDangTien.format(tongTienKho));
            lblTongSapHet.setText(String.valueOf(sapHet));
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Không thể tải báo cáo sản phẩm: " + ex.getMessage());
            alert.showAndWait();
        }
    }
}

