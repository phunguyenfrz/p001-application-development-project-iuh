package com.petshop.view.donnhap;

import com.petshop.controller.DonHangController;
import com.petshop.model.DonNhap;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Màn hình lập đơn nhập hàng, chọn nhà cung cấp và thêm sản phẩm vào đơn.
 * Dữ liệu hiện tại là dữ liệu mẫu trong lúc chờ DonHangService.
 */
public class LapDonNhapView extends VBox {

    private static final Locale LOCALE_VI_VN = new Locale("vi", "VN");

    private final DonHangController controller = new DonHangController();
    private final Button btnChonNhaCungCap = new Button("Chọn nhà cung cấp");
    private final Button btnThemSanPham = new Button("Thêm sản phẩm");
    private final Button btnXoaDong = new Button("Xóa dòng đã chọn");
    private final Button btnTaoDon = new Button("Tạo đơn nhập");
    private final Label lblNhaCungCap = new Label("Chưa chọn nhà cung cấp");
    private final Label lblTongTien = new Label("0 đ");
    private final TableView<DongDonNhapMau> tblChiTiet = new TableView<>();
    private final ObservableList<DongDonNhapMau> dsChiTiet = FXCollections.observableArrayList();

    private DonNhap.NhaCungCap nhaCungCapDangChon;

    public LapDonNhapView() {
        setSpacing(14);
        setPadding(new Insets(18));
        getStyleClass().add("khung-phan-he");

        VBox pnlTieuDe = taoTieuDe();
        VBox pnlDonNhap = taoKhungDonNhap();
        getChildren().addAll(pnlTieuDe, pnlDonNhap);
        VBox.setVgrow(pnlDonNhap, Priority.ALWAYS);
    }

    private VBox taoTieuDe() {
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("LẬP ĐƠN NHẬP HÀNG");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Chọn nhà cung cấp, thêm sản phẩm và kiểm tra tổng tiền đơn nhập.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);
        return pnlTieuDe;
    }

    private VBox taoKhungDonNhap() {
        VBox pnlDonNhap = new VBox(12);
        pnlDonNhap.getStyleClass().add("hop-noi-dung");
        VBox.setVgrow(pnlDonNhap, Priority.ALWAYS);

        HBox pnlNhaCungCap = taoKhungNhaCungCap();
        VBox pnlChiTiet = taoKhungChiTiet();
        HBox pnlTongTien = taoKhungTongTien();

        pnlDonNhap.getChildren().addAll(pnlNhaCungCap, pnlChiTiet, pnlTongTien);
        VBox.setVgrow(pnlChiTiet, Priority.ALWAYS);
        return pnlDonNhap;
    }

    private HBox taoKhungNhaCungCap() {
        HBox pnlNhaCungCap = new HBox(10);
        pnlNhaCungCap.getStyleClass().add("thanh-tac-vu");
        pnlNhaCungCap.setAlignment(Pos.CENTER_LEFT);

        Label lblTieuDeNhaCungCap = new Label("Nhà cung cấp:");
        lblNhaCungCap.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(lblNhaCungCap, Priority.ALWAYS);

        btnChonNhaCungCap.getStyleClass().add("nut-phu");
        btnChonNhaCungCap.setOnAction(e -> xuLyChonNhaCungCap());
        pnlNhaCungCap.getChildren().addAll(lblTieuDeNhaCungCap, lblNhaCungCap, btnChonNhaCungCap);
        return pnlNhaCungCap;
    }

    private VBox taoKhungChiTiet() {
        VBox pnlChiTiet = new VBox(10);
        HBox pnlNutChiTiet = new HBox(8);
        pnlNutChiTiet.setAlignment(Pos.CENTER_LEFT);

        btnThemSanPham.getStyleClass().add("nut-chinh");
        btnThemSanPham.setOnAction(e -> xuLyThemSanPham());
        btnXoaDong.getStyleClass().add("nut-phu");
        btnXoaDong.setOnAction(e -> xuLyXoaDong());
        pnlNutChiTiet.getChildren().addAll(btnThemSanPham, btnXoaDong);

        tblChiTiet.getStyleClass().add("bang-du-lieu");
        tblChiTiet.setItems(dsChiTiet);
        tblChiTiet.setPlaceholder(new Label("Chưa có sản phẩm trong đơn nhập"));
        tblChiTiet.setPrefHeight(320);
        VBox.setVgrow(tblChiTiet, Priority.ALWAYS);
        taoCotChiTiet();

        pnlChiTiet.getChildren().addAll(pnlNutChiTiet, tblChiTiet);
        return pnlChiTiet;
    }

    private void taoCotChiTiet() {
        TableColumn<DongDonNhapMau, String> colMa = new TableColumn<>("Mã sản phẩm");
        colMa.setPrefWidth(175);
        colMa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().sanPham.maSanPham()));

        TableColumn<DongDonNhapMau, String> colTen = new TableColumn<>("Tên sản phẩm");
        colTen.setPrefWidth(250);
        colTen.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().sanPham.tenSanPham()));

        TableColumn<DongDonNhapMau, String> colSoLuong = new TableColumn<>("Số lượng");
        colSoLuong.setPrefWidth(110);
        colSoLuong.setStyle("-fx-alignment: CENTER-RIGHT;");
        colSoLuong.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getSoLuong())));

        TableColumn<DongDonNhapMau, String> colDonGia = new TableColumn<>("Đơn giá nhập");
        colDonGia.setPrefWidth(150);
        colDonGia.setStyle("-fx-alignment: CENTER-RIGHT;");
        colDonGia.setCellValueFactory(d -> new SimpleStringProperty(dinhDangTien(d.getValue().donGia)));

        TableColumn<DongDonNhapMau, String> colThanhTien = new TableColumn<>("Thành tiền");
        colThanhTien.setPrefWidth(160);
        colThanhTien.setStyle("-fx-alignment: CENTER-RIGHT;");
        colThanhTien.setCellValueFactory(d -> new SimpleStringProperty(dinhDangTien(d.getValue().tinhThanhTien())));

        tblChiTiet.getColumns().addAll(colMa, colTen, colSoLuong, colDonGia, colThanhTien);
    }

    private HBox taoKhungTongTien() {
        HBox pnlTongTien = new HBox(10);
        pnlTongTien.setAlignment(Pos.CENTER_RIGHT);

        Label lblTieuDeTongTien = new Label("Tổng tiền:");
        lblTongTien.getStyleClass().add("tieu-de-phan-he");
        btnTaoDon.getStyleClass().add("nut-chinh");
        btnTaoDon.setOnAction(e -> xuLyTaoDon());
        pnlTongTien.getChildren().addAll(lblTieuDeTongTien, lblTongTien, btnTaoDon);
        return pnlTongTien;
    }

    // Mở danh sách nhà cung cấp và xử lý khi đổi lựa chọn.
    private void xuLyChonNhaCungCap() {
        List<DonNhap.NhaCungCap> danhSach;
        try {
            danhSach = controller.layDanhSachNhaCungCap();
        } catch (RuntimeException ex) {
            hienThiLoi(ex.getMessage());
            return;
        }
        if (danhSach.isEmpty()) {
            hienThiLoi("Chưa có nhà cung cấp trong hệ thống.");
            return;
        }

        ChoiceDialog<DonNhap.NhaCungCap> dlg = new ChoiceDialog<>(null, danhSach);
        dlg.setTitle("Chọn nhà cung cấp");
        dlg.setHeaderText("Chọn nhà cung cấp cho đơn nhập");
        dlg.setContentText("Nhà cung cấp:");
        ganChuDialog(dlg);

        Optional<DonNhap.NhaCungCap> ketQua = dlg.showAndWait();
        if (ketQua.isEmpty()) {
            return;
        }

        DonNhap.NhaCungCap luaChon = ketQua.get();
        if (nhaCungCapDangChon != null
            && !nhaCungCapDangChon.maNhaCungCap().equals(luaChon.maNhaCungCap())
                && !dsChiTiet.isEmpty()) {
            Alert xacNhan = new Alert(Alert.AlertType.CONFIRMATION);
            xacNhan.setTitle("Đổi nhà cung cấp");
            xacNhan.setHeaderText(null);
            xacNhan.setContentText("Đổi nhà cung cấp sẽ xóa các sản phẩm hiện có. Bạn có muốn tiếp tục?");
            ganChuDialog(xacNhan);
            if (xacNhan.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }
            dsChiTiet.clear();
            capNhatTongTien();
        }

        nhaCungCapDangChon = luaChon;
        lblNhaCungCap.setText(luaChon.toString());
    }

    // Mở dialog chọn sản phẩm và nhập số lượng.
    private void xuLyThemSanPham() {
        if (nhaCungCapDangChon == null) {
            hienThiLoi("Vui lòng chọn nhà cung cấp trước khi thêm sản phẩm!");
            return;
        }

        DongDonNhapMau dongMoi;
        try {
            dongMoi = moDialogThemSanPham();
        } catch (RuntimeException ex) {
            hienThiLoi(ex.getMessage());
            return;
        }
        if (dongMoi == null) {
            return;
        }

        for (DongDonNhapMau dongCu : dsChiTiet) {
            if (dongCu.sanPham.maSanPham().equals(dongMoi.sanPham.maSanPham())) {
                dongCu.soLuong += dongMoi.soLuong;
                dongCu.donGia = dongMoi.donGia;
                tblChiTiet.refresh();
                capNhatTongTien();
                return;
            }
        }

        dsChiTiet.add(dongMoi);
        capNhatTongTien();
    }

    private DongDonNhapMau moDialogThemSanPham() {
        List<DonNhap.SanPham> sanPhamTheoNhaCungCap =
                controller.layDanhSachSanPham(nhaCungCapDangChon.maNhaCungCap());
        if (sanPhamTheoNhaCungCap.isEmpty()) {
            hienThiLoi("Nhà cung cấp này chưa có sản phẩm hoạt động.");
            return null;
        }

        Dialog<DongDonNhapMau> dlg = new Dialog<>();
        dlg.setTitle("Thêm sản phẩm vào đơn nhập");
        dlg.setHeaderText("Chọn sản phẩm và nhập số lượng cần nhập");
        ganChuDialog(dlg);

        ButtonType nutThem = new ButtonType("Thêm", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(nutThem, ButtonType.CANCEL);
        ComboBox<DonNhap.SanPham> cboSanPham = new ComboBox<>(FXCollections.observableArrayList(sanPhamTheoNhaCungCap));
        cboSanPham.setMaxWidth(Double.MAX_VALUE);
        cboSanPham.setPromptText("Chọn sản phẩm");
        TextField txtSoLuong = new TextField("1");
        txtSoLuong.setPromptText("Số nguyên lớn hơn 0");
        txtSoLuong.textProperty().addListener((obs, cu, moi) -> {
            if (moi != null && !moi.matches("\\d*")) {
                txtSoLuong.setText(cu);
            }
        });
        TextField txtDonGia = new TextField();
        txtDonGia.setPromptText("Giá nhập, VND");
        txtDonGia.textProperty().addListener((obs, cu, moi) -> {
            if (moi != null && !moi.matches("\\d*(\\.\\d{0,2})?")) {
                txtDonGia.setText(cu);
            }
        });
        Label lblThanhTien = new Label("0 đ");
        cboSanPham.valueProperty().addListener((obs, cu, moi) ->
            capNhatThanhTienDuKien(txtSoLuong.getText(), txtDonGia.getText(), lblThanhTien));
        txtSoLuong.textProperty().addListener((obs, cu, moi) ->
            capNhatThanhTienDuKien(moi, txtDonGia.getText(), lblThanhTien));
        txtDonGia.textProperty().addListener((obs, cu, moi) ->
            capNhatThanhTienDuKien(txtSoLuong.getText(), moi, lblThanhTien));

        GridPane pnlNhap = new GridPane();
        pnlNhap.setHgap(10);
        pnlNhap.setVgap(10);
        pnlNhap.setPadding(new Insets(12));
        pnlNhap.add(new Label("Sản phẩm:"), 0, 0);
        pnlNhap.add(cboSanPham, 1, 0);
        pnlNhap.add(new Label("Số lượng:"), 0, 1);
        pnlNhap.add(txtSoLuong, 1, 1);
        pnlNhap.add(new Label("Đơn giá nhập:"), 0, 2);
        pnlNhap.add(txtDonGia, 1, 2);
        pnlNhap.add(new Label("Thành tiền:"), 0, 3);
        pnlNhap.add(lblThanhTien, 1, 3);
        dlg.getDialogPane().setContent(pnlNhap);
        dlg.setResultConverter(button -> null);

        Node nutThemDialog = dlg.getDialogPane().lookupButton(nutThem);
        nutThemDialog.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            if (cboSanPham.getValue() == null) {
                e.consume();
                hienThiLoi("Vui lòng chọn sản phẩm!");
                return;
            }
            int soLuong;
            try {
                soLuong = Integer.parseInt(txtSoLuong.getText());
                if (soLuong <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                e.consume();
                hienThiLoi("Số lượng phải là số nguyên lớn hơn 0!");
                return;
            }

            BigDecimal donGia;
            try {
                donGia = new BigDecimal(txtDonGia.getText().trim());
                if (donGia.signum() < 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                e.consume();
                hienThiLoi("Đơn giá nhập phải là số không âm, tối đa 2 chữ số thập phân.");
                return;
            }

            dlg.setResult(new DongDonNhapMau(cboSanPham.getValue(), soLuong, donGia));
            e.consume();
            dlg.close();
        });

        return dlg.showAndWait().orElse(null);
    }

    private void capNhatThanhTienDuKien(String chuoiSoLuong, String chuoiDonGia, Label lblThanhTien) {
        if (chuoiSoLuong == null || chuoiSoLuong.isEmpty() || chuoiDonGia == null || chuoiDonGia.isEmpty()) {
            lblThanhTien.setText("0 đ");
            return;
        }
        try {
            int soLuong = Integer.parseInt(chuoiSoLuong);
            BigDecimal donGia = new BigDecimal(chuoiDonGia);
            lblThanhTien.setText(dinhDangTien(donGia.multiply(BigDecimal.valueOf(soLuong))));
        } catch (NumberFormatException ex) {
            lblThanhTien.setText("Không hợp lệ");
        }
    }

    private void xuLyXoaDong() {
        DongDonNhapMau dongChon = tblChiTiet.getSelectionModel().getSelectedItem();
        if (dongChon == null) {
            hienThiLoi("Vui lòng chọn sản phẩm cần xóa khỏi đơn!");
            return;
        }
        dsChiTiet.remove(dongChon);
        capNhatTongTien();
    }

    private void capNhatTongTien() {
        BigDecimal tongTien = BigDecimal.ZERO;
        for (DongDonNhapMau dong : dsChiTiet) {
            tongTien = tongTien.add(dong.tinhThanhTien());
        }
        lblTongTien.setText(dinhDangTien(tongTien));
    }

    private void xuLyTaoDon() {
        if (nhaCungCapDangChon == null) {
            hienThiLoi("Vui lòng chọn nhà cung cấp!");
            return;
        }
        if (dsChiTiet.isEmpty()) {
            hienThiLoi("Vui lòng thêm ít nhất một sản phẩm vào đơn nhập!");
            return;
        }

        try {
            List<DonNhap.ChiTiet> chiTiet = dsChiTiet.stream()
                    .map(dong -> new DonNhap.ChiTiet(dong.sanPham.maSanPham(), dong.sanPham.tenSanPham(),
                            dong.soLuong, dong.donGia))
                    .toList();
            String maDonHang = controller.taoDonNhap(nhaCungCapDangChon.maNhaCungCap(), chiTiet);
            hienThiThongBao("Tạo đơn thành công", "Đơn nhập " + maDonHang + " đã được lưu và đang chờ nhận hàng.");
            dsChiTiet.clear();
            nhaCungCapDangChon = null;
            lblNhaCungCap.setText("Chưa chọn nhà cung cấp");
            capNhatTongTien();
        } catch (RuntimeException ex) {
            hienThiLoi(ex.getMessage());
        }
        return danhSach;
    }

    private void ganChuDialog(Dialog<?> dlg) {
        if (getScene() != null) {
            dlg.initOwner(getScene().getWindow());
        }
    }

    private void ganChuDialog(Alert dlg) {
        if (getScene() != null) {
            dlg.initOwner(getScene().getWindow());
        }
    }

    private String dinhDangTien(BigDecimal soTien) {
        NumberFormat nf = NumberFormat.getNumberInstance(LOCALE_VI_VN);
        nf.setMaximumFractionDigits(2);
        return nf.format(soTien) + " đ";
    }

    private void hienThiThongBao(String tieuDe, String noiDung) {
        Alert dlg = new Alert(Alert.AlertType.INFORMATION);
        dlg.setTitle(tieuDe);
        dlg.setHeaderText(null);
        dlg.setContentText(noiDung);
        ganChuDialog(dlg);
        dlg.showAndWait();
    }

    private void hienThiLoi(String noiDung) {
        Alert dlg = new Alert(Alert.AlertType.ERROR);
        dlg.setTitle("Thông báo lỗi");
        dlg.setHeaderText(null);
        dlg.setContentText(noiDung);
        ganChuDialog(dlg);
        dlg.showAndWait();
    }

    private static class DongDonNhapMau {
        private final DonNhap.SanPham sanPham;
        private BigDecimal donGia;
        private int soLuong;

        private DongDonNhapMau(DonNhap.SanPham sanPham, int soLuong, BigDecimal donGia) {
            this.sanPham = sanPham;
            this.soLuong = soLuong;
            this.donGia = donGia;
        }

        private BigDecimal tinhThanhTien() {
            return donGia.multiply(BigDecimal.valueOf(soLuong));
        }
    }
}
