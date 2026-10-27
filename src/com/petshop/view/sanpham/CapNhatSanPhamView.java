package com.petshop.view.sanpham;

import com.petshop.controller.SanPhamController;
import com.petshop.model.SanPham;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

/**
 * Màn hình Quản lý / Cập nhật Sản phẩm (CapNhatSanPhamView).
 * Cho phép xem danh sách, Thêm mới, Sửa thông tin và Ngừng kinh doanh.
 * Tiền tố điều khiển: lbl, txt, btn, tbl.
 */
public class CapNhatSanPhamView extends VBox {

    private final SanPhamController sanPhamController = new SanPhamController();
    private final DecimalFormat dinhDangTien = new DecimalFormat("#,### đ");

    private final Button btnThemMoi = new Button("Thêm mới");
    private final Button btnCapNhat = new Button("Cập nhật");
    private final Button btnXoaMem = new Button("Ngừng kinh doanh");
    private final Button btnLamMoi = new Button("Làm mới");

    private final TableView<SanPham> tblDanhSach = new TableView<>();

    public CapNhatSanPhamView() {
        setSpacing(14);
        getStyleClass().add("khung-phan-he");

        VBox pnlTieuDeMan = new VBox(4);
        Label lblTieuDe = new Label("SẢN PHẨM - CẬP NHẬT SẢN PHẨM");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Bấm Thêm mới để tạo sản phẩm; chọn một sản phẩm trong bảng để Cập nhật thông tin hoặc Ngừng kinh doanh.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDeMan.getChildren().addAll(lblTieuDe, lblMoTa);

        VBox pnlBang = taoKhungBangDanhSach();
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        getChildren().addAll(pnlTieuDeMan, pnlBang);

        capNhatTrangThaiNut(null);
        taiLaiBang();
    }

    private VBox taoKhungBangDanhSach() {
        VBox pnlKhung = new VBox(10);
        pnlKhung.getStyleClass().add("hop-noi-dung");
        pnlKhung.setPadding(new Insets(14));

        HBox pnlTacVu = new HBox(8);
        pnlTacVu.setAlignment(Pos.CENTER_LEFT);

        btnThemMoi.getStyleClass().add("nut-chinh");
        btnThemMoi.setOnAction(e -> moCuaSoThem());

        btnCapNhat.getStyleClass().add("nut-phu");
        btnCapNhat.setOnAction(e -> moCuaSoCapNhat());

        btnXoaMem.getStyleClass().add("nut-phu");
        btnXoaMem.setOnAction(e -> xuLyXoaMem());

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> taiLaiBang());

        pnlTacVu.getChildren().addAll(btnThemMoi, btnCapNhat, btnXoaMem, btnLamMoi);

        cauHinhCacCotBang();
        pnlKhung.getChildren().addAll(pnlTacVu, tblDanhSach);
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);

        tblDanhSach.getSelectionModel().selectedItemProperty().addListener((obs, cu, moi) -> capNhatTrangThaiNut(moi));

        return pnlKhung;
    }

    private void cauHinhCacCotBang() {
        TableColumn<SanPham, String> colMa = new TableColumn<>("Mã sản phẩm");
        colMa.setPrefWidth(140);
        colMa.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaSanPham()));

        TableColumn<SanPham, String> colTen = new TableColumn<>("Tên sản phẩm");
        colTen.setPrefWidth(220);
        colTen.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTenSanPham()));

        TableColumn<SanPham, String> colMaVach = new TableColumn<>("Mã vạch");
        colMaVach.setPrefWidth(120);
        colMaVach.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaVach()));

        TableColumn<SanPham, String> colGiaBan = new TableColumn<>("Giá bán");
        colGiaBan.setPrefWidth(110);
        colGiaBan.setStyle("-fx-alignment: CENTER-RIGHT;");
        colGiaBan.setCellValueFactory(cell -> new SimpleStringProperty(dinhDangTien.format(cell.getValue().getGiaBan())));

        TableColumn<SanPham, String> colDonVi = new TableColumn<>("Đơn vị");
        colDonVi.setPrefWidth(80);
        colDonVi.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDonViTinh()));

        TableColumn<SanPham, String> colTonKho = new TableColumn<>("Tồn kho");
        colTonKho.setPrefWidth(90);
        colTonKho.setStyle("-fx-alignment: CENTER-RIGHT;");
        colTonKho.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getSoLuongTon())));

        TableColumn<SanPham, String> colNguong = new TableColumn<>("Ngưỡng an toàn");
        colNguong.setPrefWidth(110);
        colNguong.setStyle("-fx-alignment: CENTER-RIGHT;");
        colNguong.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getNguongTonToiThieu())));

        TableColumn<SanPham, String> colNhaCungCap = new TableColumn<>("Nhà cung cấp");
        colNhaCungCap.setPrefWidth(180);
        colNhaCungCap.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getTenNhaCungCap() != null ? cell.getValue().getTenNhaCungCap() : "Chưa xác định"
        ));

        TableColumn<SanPham, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setPrefWidth(130);
        colTrangThai.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().isTrangThai() ? "Đang kinh doanh" : "Ngừng kinh doanh"
        ));

        tblDanhSach.getColumns().setAll(List.of(
                colMa, colTen, colMaVach, colGiaBan, colDonVi, colTonKho, colNguong, colNhaCungCap, colTrangThai
        ));
        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Chưa có dữ liệu sản phẩm."));
    }

    public void taiLaiBang() {
        try {
            List<SanPham> ds = sanPhamController.layDanhSachSanPham();
            tblDanhSach.setItems(FXCollections.observableArrayList(ds));
        } catch (Exception ex) {
            hienThiCanhBao("Lỗi tải dữ liệu", ex.getMessage());
        }
    }

    private void capNhatTrangThaiNut(SanPham sp) {
        boolean coChon = sp != null;
        btnCapNhat.setDisable(!coChon);
        btnXoaMem.setDisable(!coChon || !sp.isTrangThai());
    }

    private void moCuaSoThem() {
        ThemSanPhamView viewThem = new ThemSanPhamView(this::taiLaiBang);
        Stage cuaSo = new Stage();
        cuaSo.setTitle("Happy Tails - Thêm sản phẩm mới");
        cuaSo.initModality(Modality.APPLICATION_MODAL);
        cuaSo.setScene(new Scene(viewThem, 720, 360));
        cuaSo.centerOnScreen();
        cuaSo.show();
    }

    private void moCuaSoCapNhat() {
        SanPham sp = tblDanhSach.getSelectionModel().getSelectedItem();
        if (sp == null) return;

        Dialog<ButtonType> dlgCapNhat = new Dialog<>();
        dlgCapNhat.setTitle("Happy Tails - Cập nhật sản phẩm");
        dlgCapNhat.setHeaderText(null);

        GridPane pnlGrid = new GridPane();
        pnlGrid.setHgap(12);
        pnlGrid.setVgap(10);
        pnlGrid.setPadding(new Insets(20));

        TextField txtTen = new TextField(sp.getTenSanPham());
        TextField txtMaVach = new TextField(sp.getMaVach());
        TextField txtGia = new TextField(sp.getGiaBan().toPlainString());
        TextField txtDonVi = new TextField(sp.getDonViTinh());
        TextField txtNguong = new TextField(String.valueOf(sp.getNguongTonToiThieu()));
        CheckBox chkTrangThai = new CheckBox("Đang kinh doanh");
        chkTrangThai.setSelected(sp.isTrangThai());

        txtGia.textProperty().addListener((obs, cu, moi) -> {
            if (!moi.matches("\\d*")) txtGia.setText(cu);
        });
        txtNguong.textProperty().addListener((obs, cu, moi) -> {
            if (!moi.matches("\\d*")) txtNguong.setText(cu);
        });

        pnlGrid.add(new Label("Mã sản phẩm:"), 0, 0);
        pnlGrid.add(new Label(sp.getMaSanPham()), 1, 0);

        pnlGrid.add(new Label("Tên sản phẩm (*):"), 0, 1);
        pnlGrid.add(txtTen, 1, 1);

        pnlGrid.add(new Label("Mã vạch (*):"), 0, 2);
        pnlGrid.add(txtMaVach, 1, 2);

        pnlGrid.add(new Label("Giá bán (VNĐ) (*):"), 0, 3);
        pnlGrid.add(txtGia, 1, 3);

        pnlGrid.add(new Label("Đơn vị tính (*):"), 0, 4);
        pnlGrid.add(txtDonVi, 1, 4);

        pnlGrid.add(new Label("Ngưỡng an toàn:"), 0, 5);
        pnlGrid.add(txtNguong, 1, 5);

        pnlGrid.add(new Label("Trạng thái:"), 0, 6);
        pnlGrid.add(chkTrangThai, 1, 6);

        dlgCapNhat.getDialogPane().setContent(pnlGrid);
        dlgCapNhat.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> ketQua = dlgCapNhat.showAndWait();
        if (ketQua.isPresent() && ketQua.get() == ButtonType.OK) {
            try {
                sp.setTenSanPham(txtTen.getText().trim());
                sp.setMaVach(txtMaVach.getText().trim());
                sp.setGiaBan(new BigDecimal(txtGia.getText().trim()));
                sp.setDonViTinh(txtDonVi.getText().trim());
                sp.setNguongTonToiThieu(Integer.parseInt(txtNguong.getText().trim()));
                sp.setTrangThai(chkTrangThai.isSelected());

                boolean thanhCong = sanPhamController.capNhatSanPham(sp);
                if (thanhCong) {
                    hienThiThongBao("Thông báo", "Cập nhật sản phẩm thành công!");
                    taiLaiBang();
                }
            } catch (Exception ex) {
                hienThiCanhBao("Lỗi cập nhật", ex.getMessage());
            }
        }
    }

    private void xuLyXoaMem() {
        SanPham sp = tblDanhSach.getSelectionModel().getSelectedItem();
        if (sp == null) return;

        Alert xacNhan = new Alert(Alert.AlertType.CONFIRMATION);
        xacNhan.setTitle("Xác nhận ngừng kinh doanh");
        xacNhan.setHeaderText(null);
        xacNhan.setContentText("Bạn có chắc chắn muốn chuyển sản phẩm \"" + sp.getTenSanPham() + "\" sang trạng thái Ngừng kinh doanh không?");

        Optional<ButtonType> ketQua = xacNhan.showAndWait();
        if (ketQua.isPresent() && ketQua.get() == ButtonType.OK) {
            try {
                boolean thanhCong = sanPhamController.xoaMemSanPham(sp.getMaSanPham());
                if (thanhCong) {
                    hienThiThongBao("Thông báo", "Đã chuyển sản phẩm sang ngừng kinh doanh!");
                    taiLaiBang();
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

