package com.petshop.view.donnhap;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Màn hình lập đơn nhập hàng, chọn nhà cung cấp và thêm sản phẩm vào đơn.
 * Dữ liệu hiện tại là dữ liệu mẫu trong lúc chờ DonHangService.
 */
public class LapDonNhapView extends VBox {

    private static final Locale LOCALE_VI_VN = new Locale("vi", "VN");

    private final Button btnChonNhaCungCap = new Button("Chọn nhà cung cấp");
    private final Button btnThemSanPham = new Button("Thêm sản phẩm");
    private final Button btnXoaDong = new Button("Xóa dòng đã chọn");
    private final Button btnTaoDon = new Button("Tạo đơn nhập");
    private final Label lblNhaCungCap = new Label("Chưa chọn nhà cung cấp");
    private final Label lblTongTien = new Label("0 đ");
    private final TableView<DongDonNhapMau> tblChiTiet = new TableView<>();
    private final ObservableList<DongDonNhapMau> dsChiTiet = FXCollections.observableArrayList();

    private final List<NhaCungCapMau> dsNhaCungCap = taoDanhSachNhaCungCapMau();
    private final List<SanPhamMau> dsSanPham = taoDanhSachSanPhamMau();
    private NhaCungCapMau nhaCungCapDangChon;

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
        colMa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaSanPham()));

        TableColumn<DongDonNhapMau, String> colTen = new TableColumn<>("Tên sản phẩm");
        colTen.setPrefWidth(250);
        colTen.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenSanPham()));

        TableColumn<DongDonNhapMau, String> colSoLuong = new TableColumn<>("Số lượng");
        colSoLuong.setPrefWidth(110);
        colSoLuong.setStyle("-fx-alignment: CENTER-RIGHT;");
        colSoLuong.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getSoLuong())));

        TableColumn<DongDonNhapMau, String> colDonGia = new TableColumn<>("Đơn giá nhập");
        colDonGia.setPrefWidth(150);
        colDonGia.setStyle("-fx-alignment: CENTER-RIGHT;");
        colDonGia.setCellValueFactory(d -> new SimpleStringProperty(dinhDangTien(d.getValue().getDonGia())));

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

    // Mở danh sách nhà cung cấp mẫu và xử lý khi đổi lựa chọn.
    private void xuLyChonNhaCungCap() {
        ChoiceDialog<NhaCungCapMau> dlg = new ChoiceDialog<>(null, dsNhaCungCap);
        dlg.setTitle("Chọn nhà cung cấp");
        dlg.setHeaderText("Chọn nhà cung cấp cho đơn nhập");
        dlg.setContentText("Nhà cung cấp:");
        ganChuDialog(dlg);

        Optional<NhaCungCapMau> ketQua = dlg.showAndWait();
        if (ketQua.isEmpty()) {
            return;
        }

        NhaCungCapMau luaChon = ketQua.get();
        if (nhaCungCapDangChon != null
                && !nhaCungCapDangChon.getMaNhaCungCap().equals(luaChon.getMaNhaCungCap())
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

        DongDonNhapMau dongMoi = moDialogThemSanPham();
        if (dongMoi == null) {
            return;
        }

        for (DongDonNhapMau dongCu : dsChiTiet) {
            if (dongCu.getMaSanPham().equals(dongMoi.getMaSanPham())) {
                dongCu.setSoLuong(dongCu.getSoLuong() + dongMoi.getSoLuong());
                tblChiTiet.refresh();
                capNhatTongTien();
                return;
            }
        }

        dsChiTiet.add(dongMoi);
        capNhatTongTien();
    }

    private DongDonNhapMau moDialogThemSanPham() {
        List<SanPhamMau> sanPhamTheoNhaCungCap = laySanPhamTheoNhaCungCap();
        if (sanPhamTheoNhaCungCap.isEmpty()) {
            hienThiLoi("Nhà cung cấp này chưa có sản phẩm mẫu.");
            return null;
        }

        Dialog<DongDonNhapMau> dlg = new Dialog<>();
        dlg.setTitle("Thêm sản phẩm vào đơn nhập");
        dlg.setHeaderText("Chọn sản phẩm và nhập số lượng cần nhập");
        ganChuDialog(dlg);

        ButtonType nutThem = new ButtonType("Thêm", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(nutThem, ButtonType.CANCEL);
        ComboBox<SanPhamMau> cboSanPham = new ComboBox<>(FXCollections.observableArrayList(sanPhamTheoNhaCungCap));
        cboSanPham.setMaxWidth(Double.MAX_VALUE);
        cboSanPham.setPromptText("Chọn sản phẩm");
        TextField txtSoLuong = new TextField("1");
        txtSoLuong.setPromptText("Số nguyên lớn hơn 0");
        txtSoLuong.textProperty().addListener((obs, cu, moi) -> {
            if (moi != null && !moi.matches("\\d*")) {
                txtSoLuong.setText(cu);
            }
        });
        Label lblThanhTien = new Label("0 đ");
        cboSanPham.valueProperty().addListener((obs, cu, moi) ->
            capNhatThanhTienDuKien(moi, txtSoLuong.getText(), lblThanhTien));
        txtSoLuong.textProperty().addListener((obs, cu, moi) ->
            capNhatThanhTienDuKien(cboSanPham.getValue(), moi, lblThanhTien));

        GridPane pnlNhap = new GridPane();
        pnlNhap.setHgap(10);
        pnlNhap.setVgap(10);
        pnlNhap.setPadding(new Insets(12));
        pnlNhap.add(new Label("Sản phẩm:"), 0, 0);
        pnlNhap.add(cboSanPham, 1, 0);
        pnlNhap.add(new Label("Số lượng:"), 0, 1);
        pnlNhap.add(txtSoLuong, 1, 1);
        pnlNhap.add(new Label("Thành tiền:"), 0, 2);
        pnlNhap.add(lblThanhTien, 1, 2);
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

            SanPhamMau sanPham = cboSanPham.getValue();
            dlg.setResult(new DongDonNhapMau(sanPham, soLuong));
            e.consume();
            dlg.close();
        });

        return dlg.showAndWait().orElse(null);
    }

    private void capNhatThanhTienDuKien(SanPhamMau sanPham, String chuoiSoLuong, Label lblThanhTien) {
        if (sanPham == null || chuoiSoLuong == null || chuoiSoLuong.isEmpty()) {
            lblThanhTien.setText("0 đ");
            return;
        }
        try {
            int soLuong = Integer.parseInt(chuoiSoLuong);
            lblThanhTien.setText(dinhDangTien(sanPham.getDonGiaNhap() * soLuong));
        } catch (NumberFormatException ex) {
            lblThanhTien.setText("Không hợp lệ");
        }
    }

    private List<SanPhamMau> laySanPhamTheoNhaCungCap() {
        List<SanPhamMau> ketQua = new ArrayList<>();
        for (SanPhamMau sanPham : dsSanPham) {
            if (sanPham.getMaNhaCungCap().equals(nhaCungCapDangChon.getMaNhaCungCap())) {
                ketQua.add(sanPham);
            }
        }
        return ketQua;
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
        double tongTien = 0;
        for (DongDonNhapMau dong : dsChiTiet) {
            tongTien += dong.tinhThanhTien();
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

        // TODO: Gọi DonHangService để lưu đơn nhập và các dòng chi tiết.
        hienThiThongBao("Chưa kết nối lưu đơn", "Thông tin hiện là dữ liệu mẫu. TODO: nối DonHangService để lưu đơn nhập.");
    }

    private List<NhaCungCapMau> taoDanhSachNhaCungCapMau() {
        List<NhaCungCapMau> danhSach = new ArrayList<>();
        danhSach.add(new NhaCungCapMau("NCC-20261026-000001", "Nhà cung cấp Thú cưng An Phát"));
        danhSach.add(new NhaCungCapMau("NCC-20261026-000002", "Công ty Thức ăn Happy Pet"));
        return danhSach;
    }

    private List<SanPhamMau> taoDanhSachSanPhamMau() {
        List<SanPhamMau> danhSach = new ArrayList<>();
        danhSach.add(new SanPhamMau("SP-20261026-000001", "Thức ăn hạt cho chó 2kg", "NCC-20261026-000001", 125000));
        danhSach.add(new SanPhamMau("SP-20261026-000002", "Vòng cổ thú cưng", "NCC-20261026-000001", 45000));
        danhSach.add(new SanPhamMau("SP-20261026-000003", "Thức ăn mèo vị cá 1kg", "NCC-20261026-000002", 89000));
        danhSach.add(new SanPhamMau("SP-20261026-000004", "Sữa tắm thú cưng 500ml", "NCC-20261026-000002", 72000));
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

    private String dinhDangTien(double soTien) {
        NumberFormat nf = NumberFormat.getNumberInstance(LOCALE_VI_VN);
        nf.setMaximumFractionDigits(0);
        return nf.format(Math.round(soTien)) + " đ";
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

    private static class NhaCungCapMau {
        private final String maNhaCungCap;
        private final String tenNhaCungCap;

        private NhaCungCapMau(String maNhaCungCap, String tenNhaCungCap) {
            this.maNhaCungCap = maNhaCungCap;
            this.tenNhaCungCap = tenNhaCungCap;
        }

        private String getMaNhaCungCap() {
            return maNhaCungCap;
        }

        @Override
        public String toString() {
            return tenNhaCungCap + " (" + maNhaCungCap + ")";
        }
    }

    private static class SanPhamMau {
        private final String maSanPham;
        private final String tenSanPham;
        private final String maNhaCungCap;
        private final double donGiaNhap;

        private SanPhamMau(String maSanPham, String tenSanPham, String maNhaCungCap, double donGiaNhap) {
            this.maSanPham = maSanPham;
            this.tenSanPham = tenSanPham;
            this.maNhaCungCap = maNhaCungCap;
            this.donGiaNhap = donGiaNhap;
        }

        private String getMaSanPham() {
            return maSanPham;
        }

        private String getTenSanPham() {
            return tenSanPham;
        }

        private String getMaNhaCungCap() {
            return maNhaCungCap;
        }

        private double getDonGiaNhap() {
            return donGiaNhap;
        }

        @Override
        public String toString() {
            return tenSanPham + " - " + dinhDangTien(donGiaNhap);
        }

        private String dinhDangTien(double soTien) {
            NumberFormat nf = NumberFormat.getNumberInstance(LOCALE_VI_VN);
            nf.setMaximumFractionDigits(0);
            return nf.format(Math.round(soTien)) + " đ";
        }
    }

    private static class DongDonNhapMau {
        private final String maSanPham;
        private final String tenSanPham;
        private final double donGia;
        private int soLuong;

        private DongDonNhapMau(SanPhamMau sanPham, int soLuong) {
            this.maSanPham = sanPham.getMaSanPham();
            this.tenSanPham = sanPham.getTenSanPham();
            this.donGia = sanPham.getDonGiaNhap();
            this.soLuong = soLuong;
        }

        private String getMaSanPham() {
            return maSanPham;
        }

        private String getTenSanPham() {
            return tenSanPham;
        }

        private double getDonGia() {
            return donGia;
        }

        private int getSoLuong() {
            return soLuong;
        }

        private void setSoLuong(int soLuong) {
            this.soLuong = soLuong;
        }

        private double tinhThanhTien() {
            return donGia * soLuong;
        }
    }
}
