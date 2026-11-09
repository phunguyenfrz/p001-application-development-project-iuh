package com.petshop.gui.sanpham;

import com.petshop.bus.SanPhamBUS;
import com.petshop.entity.SanPham;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Màn hình theo dõi số lượng tồn kho và cảnh báo dưới ngưỡng an toàn (SP_TON_KHO).
 * Phân loại chính xác 3 trạng thái: "Còn hàng", "Sắp hết hàng" (dưới ngưỡng an toàn), "Hết hàng".
 * Thiết kế giao diện đồng bộ hoàn toàn theo phân hệ Dịch vụ.
 */
public class TheoDoiTonKhoView extends VBox {

    private final SanPhamBUS sanPhamController = new SanPhamBUS();

    private final TextField txtTimKiem = new TextField();
    private final ComboBox<String> cboLocTinhTrang = new ComboBox<>();
    private final Button btnLamMoi = new Button("Làm mới dữ liệu");
    private final Label lblThongKeTong = new Label();

    private final TableView<SanPham> tblDanhSach = new TableView<>();
    private List<SanPham> danhSachGoc = new ArrayList<>();

    public TheoDoiTonKhoView() {
        this(false);
    }

    public TheoDoiTonKhoView(boolean chiLocSapHet) {
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
        VBox.setVgrow(pnlKhungChinh, Priority.ALWAYS);

        HBox pnlThanhCongCu = taoThanhCongCu();
        VBox pnlBang = taoKhungBang();
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        pnlKhungChinh.getChildren().addAll(pnlThanhCongCu, pnlBang);
        getChildren().addAll(pnlTieuDeMan, pnlKhungChinh);

        if (chiLocSapHet) {
            cboLocTinhTrang.setValue("Cần nhập (Hết + Sắp hết)");
        }
        taiDuLieu();
    }

    public void locHangSapHet() {
        cboLocTinhTrang.setValue("Cần nhập (Hết + Sắp hết)");
        locDuLieu();
    }

    private HBox taoThanhCongCu() {
        HBox pnlThanh = new HBox(12);
        pnlThanh.getStyleClass().add("thanh-tac-vu");
        pnlThanh.setAlignment(Pos.CENTER_LEFT);

        txtTimKiem.setPromptText("Tìm mã, tên sản phẩm...");
        txtTimKiem.setPrefWidth(220);
        txtTimKiem.textProperty().addListener((obs, oldVal, newVal) -> locDuLieu());

        cboLocTinhTrang.setItems(FXCollections.observableArrayList(
                "Tất cả tình trạng",
                "Còn hàng",
                "Sắp hết hàng",
                "Hết hàng",
                "Cần nhập (Hết + Sắp hết)"
        ));
        cboLocTinhTrang.setValue("Tất cả tình trạng");
        cboLocTinhTrang.setOnAction(e -> locDuLieu());

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> taiDuLieu());

        Region pnlKhoangCach = new Region();
        HBox.setHgrow(pnlKhoangCach, Priority.ALWAYS);

        lblThongKeTong.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");

        Label lblLoc = new Label("Lọc tình trạng:");
        lblLoc.setStyle("-fx-font-weight: bold;");

        pnlThanh.getChildren().addAll(
                txtTimKiem,
                lblLoc,
                cboLocTinhTrang,
                btnLamMoi,
                pnlKhoangCach,
                lblThongKeTong
        );
        return pnlThanh;
    }

    private VBox taoKhungBang() {
        VBox pnlBang = new VBox(8);

        TableColumn<SanPham, String> colMa = new TableColumn<>("Mã sản phẩm");
        colMa.setPrefWidth(140);
        colMa.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaSanPham()));

        TableColumn<SanPham, String> colTen = new TableColumn<>("Tên sản phẩm");
        colTen.setPrefWidth(240);
        colTen.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTenSanPham()));

        TableColumn<SanPham, String> colDonVi = new TableColumn<>("Loại / ĐVT");
        colDonVi.setPrefWidth(90);
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
        colTinhTrang.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTinhTrangTonKho()));
        colTinhTrang.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Hết hàng".equals(item)) {
                        setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    } else if ("Sắp hết hàng".equals(item)) {
                        setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    } else {
                        setStyle("-fx-text-fill: #15803d; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    }
                }
            }
        });

        TableColumn<SanPham, String> colGiaNhap = new TableColumn<>("Giá nhập");
        colGiaNhap.setPrefWidth(120);
        colGiaNhap.setStyle("-fx-alignment: CENTER-RIGHT;");
        colGiaNhap.setCellValueFactory(cell -> new SimpleStringProperty(
                ThemSanPhamView.dinhDangTien(cell.getValue().getGiaNhap())
        ));

        TableColumn<SanPham, String> colGiaBan = new TableColumn<>("Giá bán");
        colGiaBan.setPrefWidth(120);
        colGiaBan.setStyle("-fx-alignment: CENTER-RIGHT;");
        colGiaBan.setCellValueFactory(cell -> new SimpleStringProperty(
                ThemSanPhamView.dinhDangTien(cell.getValue().getGiaBan())
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
                colMa, colTen, colDonVi, colTonKho, colNguong, colTinhTrang, colGiaNhap, colGiaBan, colNcc, colTrangThai
        ));
        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Không có sản phẩm nào trong danh sách."));

        // Hết hàng có nền đỏ nhạt; sắp hết hàng có nền vàng nhạt; ngừng kinh doanh mờ
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
                } else if (item.isHetHang()) {
                    setOpacity(1.0);
                    setStyle("-fx-background-color: #fee2e2;");
                } else if (item.isSapHetHang()) {
                    setOpacity(1.0);
                    setStyle("-fx-background-color: #fef3c7;");
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

    public void taiDuLieu() {
        try {
            danhSachGoc = sanPhamController.layDanhSachSanPham();
            locDuLieu();
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Không thể tải dữ liệu tồn kho: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    private void locDuLieu() {
        if (danhSachGoc == null) return;

        String tuKhoa = txtTimKiem.getText() != null ? txtTimKiem.getText().trim().toLowerCase() : "";
        String luaChon = cboLocTinhTrang.getValue() != null ? cboLocTinhTrang.getValue() : "Tất cả tình trạng";

        List<SanPham> ketQua = danhSachGoc.stream().filter(sp -> {
            if (!tuKhoa.isEmpty()) {
                boolean hopMa = sp.getMaSanPham() != null && sp.getMaSanPham().toLowerCase().contains(tuKhoa);
                boolean hopTen = sp.getTenSanPham() != null && sp.getTenSanPham().toLowerCase().contains(tuKhoa);
                if (!hopMa && !hopTen) {
                    return false;
                }
            }

            return switch (luaChon) {
                case "Còn hàng" -> sp.isConHang();
                case "Sắp hết hàng" -> sp.isSapHetHang();
                case "Hết hàng" -> sp.isHetHang();
                case "Cần nhập (Hết + Sắp hết)" -> sp.isHetHang() || sp.isSapHetHang();
                default -> true;
            };
        }).collect(Collectors.toList());

        tblDanhSach.setItems(FXCollections.observableArrayList(ketQua));

        long soHetHang = danhSachGoc.stream().filter(sp -> sp.isTrangThai() && sp.isHetHang()).count();
        long soSapHet = danhSachGoc.stream().filter(sp -> sp.isTrangThai() && sp.isSapHetHang()).count();
        long soConHang = danhSachGoc.stream().filter(sp -> sp.isTrangThai() && sp.isConHang()).count();

        lblThongKeTong.setText("Tổng SP: " + danhSachGoc.size()
                + " | Còn hàng: " + soConHang
                + " | Sắp hết hàng: " + soSapHet
                + " | Hết hàng: " + soHetHang);
    }
}
