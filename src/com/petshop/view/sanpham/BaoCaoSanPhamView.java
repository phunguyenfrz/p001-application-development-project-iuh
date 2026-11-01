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
import java.util.List;

/**
 * Màn hình báo cáo thống kê danh mục sản phẩm và giá trị tồn kho (PRD_REPORT).
 * Thiết kế giao diện đồng bộ hoàn toàn theo phân hệ Dịch vụ.
 */
public class BaoCaoSanPhamView extends VBox {

    private final SanPhamController sanPhamController = new SanPhamController();

    private final Label lblTongSanPham = new Label("0");
    private final Label lblTongGiaTriKho = new Label("0 đ");
    private final Label lblTongSapHet = new Label("0");

    private final Button btnLamMoi = new Button("Làm mới báo cáo");
    private final TableView<SanPham> tblDanhSach = new TableView<>();

    public BaoCaoSanPhamView() {
        setSpacing(14);
        getStyleClass().add("khung-phan-he");

        VBox pnlTieuDeMan = new VBox(4);
        Label lblTieuDe = new Label("SẢN PHẨM - BÁO CÁO TỒN KHO & DANH MỤC");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Tổng hợp số lượng sản phẩm, giá trị tồn kho và cảnh báo thiếu hụt hàng hóa.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDeMan.getChildren().addAll(lblTieuDe, lblMoTa);

        HBox pnlTheThongKe = taoKhungTheThongKe();
        VBox pnlBang = taoKhungBangChiTiet();
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        getChildren().addAll(pnlTieuDeMan, pnlTheThongKe, pnlBang);

        taiBaoCao();
    }

    private HBox taoKhungTheThongKe() {
        HBox pnlThe = new HBox(14);

        VBox the1 = taoTheKpi("TỔNG SỐ SẢN PHẨM", lblTongSanPham, "Sản phẩm trong danh mục", "the-thong-ke");
        VBox the2 = taoTheKpi("TỔNG GIÁ TRỊ TỒN KHO", lblTongGiaTriKho, "Tính theo giá niêm yết hiện tại", "the-thong-ke-noi-bat");
        VBox the3 = taoTheKpi("SẢN PHẨM CẦN NHẬP THÊM", lblTongSapHet, "Tồn kho dưới ngưỡng an toàn", "the-thong-ke");

        HBox.setHgrow(the1, Priority.ALWAYS);
        HBox.setHgrow(the2, Priority.ALWAYS);
        HBox.setHgrow(the3, Priority.ALWAYS);

        pnlThe.getChildren().addAll(the1, the2, the3);
        return pnlThe;
    }

    private VBox taoTheKpi(String tieuDe, Label lblGiaTri, String phuDe, String styleCard) {
        VBox pnlThe = new VBox(6);
        pnlThe.getStyleClass().add(styleCard);
        pnlThe.setPadding(new Insets(16));

        Label lblTen = new Label(tieuDe);
        lblTen.getStyleClass().add("the-thong-ke-noi-bat".equals(styleCard) ? "tieu-de-thong-ke-trang" : "tieu-de-thong-ke");

        lblGiaTri.getStyleClass().add("the-thong-ke-noi-bat".equals(styleCard) ? "gia-tri-thong-ke-trang" : "gia-tri-thong-ke");

        Label lblPhu = new Label(phuDe);
        lblPhu.getStyleClass().add("the-thong-ke-noi-bat".equals(styleCard) ? "mo-ta-thong-ke-trang" : "mo-ta-thong-ke");

        pnlThe.getChildren().addAll(lblTen, lblGiaTri, lblPhu);
        return pnlThe;
    }

    private VBox taoKhungBangChiTiet() {
        VBox pnlBang = new VBox(10);
        pnlBang.getStyleClass().add("hop-noi-dung");
        pnlBang.setPadding(new Insets(14));

        HBox pnlTieuDeBang = new HBox(10);
        pnlTieuDeBang.getStyleClass().add("thanh-tac-vu");
        pnlTieuDeBang.setAlignment(Pos.CENTER_LEFT);

        Label lblBang = new Label("Chi tiết danh mục và giá trị tồn từng sản phẩm");
        lblBang.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #1e293b;");

        Region pnlSpacer = new Region();
        HBox.setHgrow(pnlSpacer, Priority.ALWAYS);

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> taiBaoCao());

        pnlTieuDeBang.getChildren().addAll(lblBang, pnlSpacer, btnLamMoi);

        TableColumn<SanPham, String> colMa = new TableColumn<>("Mã sản phẩm");
        colMa.setPrefWidth(140);
        colMa.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaSanPham()));

        TableColumn<SanPham, String> colTen = new TableColumn<>("Tên sản phẩm");
        colTen.setPrefWidth(220);
        colTen.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTenSanPham()));

        TableColumn<SanPham, String> colDonVi = new TableColumn<>("Loại / ĐVT");
        colDonVi.setPrefWidth(90);
        colDonVi.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDonViTinh()));

        TableColumn<SanPham, String> colGiaBan = new TableColumn<>("Giá bán");
        colGiaBan.setPrefWidth(120);
        colGiaBan.setStyle("-fx-alignment: CENTER-RIGHT;");
        colGiaBan.setCellValueFactory(cell -> new SimpleStringProperty(
                ThemSanPhamView.dinhDangTien(cell.getValue().getGiaBan().doubleValue())
        ));

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

TableColumn<SanPham, String> colGiaTri = new TableColumn<>("Thành tiền tồn");
colGiaTri.setPrefWidth(140);
colGiaTri.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");
colGiaTri.setCellValueFactory(cell -> {
    BigDecimal thanhTien = cell.getValue().getGiaBan().multiply(BigDecimal.valueOf(cell.getValue().getSoLuongTon()));
    return new SimpleStringProperty(ThemSanPhamView.dinhDangTien(thanhTien.doubleValue()));
});

TableColumn<SanPham, String> colNcc = new TableColumn<>("Nhà cung cấp");
colNcc.setPrefWidth(180);
colNcc.setCellValueFactory(cell -> new SimpleStringProperty(
        cell.getValue().getTenNhaCungCap() != null ? cell.getValue().getTenNhaCungCap() : "Chưa xác định"
));

TableColumn<SanPham, String> colTrangThai = new TableColumn<>("Trạng thái");
colTrangThai.setPrefWidth(130);
colTrangThai.setCellValueFactory(cell -> new SimpleStringProperty(
        cell.getValue().isTrangThai() ? "Đang bán" : "Ngừng bán"
));

tblDanhSach.getColumns().setAll(List.of(
        colMa, colTen, colDonVi, colTon, colNguong, colTinhTrang, colGiaBan, colGiaTri, colNcc, colTrangThai
));
tblDanhSach.getStyleClass().add("bang-du-lieu");
tblDanhSach.setPlaceholder(new Label("Chưa có dữ liệu sản phẩm trong cơ sở dữ liệu."));

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

            lblTongGiaTriKho.setText(ThemSanPhamView.dinhDangTien(tongTienKho.doubleValue()));
            lblTongSapHet.setText(String.valueOf(sapHet));
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Không thể tải báo cáo sản phẩm: " + ex.getMessage());
            alert.showAndWait();
        }
    }
}
