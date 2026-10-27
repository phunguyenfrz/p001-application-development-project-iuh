package com.petshop.view.sanpham;

import com.petshop.controller.SanPhamController;
import com.petshop.model.SanPham;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ThemSanPhamView extends VBox {

    private static final Locale LOCALE_VI_VN = new Locale("vi", "VN");

    private final SanPhamController sanPhamController;
    private final Runnable khiThemThanhCong;

    private final TextField txtTenSanPham = new TextField();
    private final ComboBox<SanPham.NhaCungCapSimple> cboNhaCungCap = new ComboBox<>();
    private final TextField txtMaVach = new TextField();
    private final TextField txtGiaBan = new TextField();
    private final TextField txtDonViTinh = new TextField();
    private final TextField txtNguongTonToiThieu = new TextField();
    private final TextField txtSoLuongTonBanDau = new TextField();

    private final Button btnLuu = new Button("Lưu sản phẩm");
    private final Button btnLamMoi = new Button("Làm mới");

    public ThemSanPhamView() {
        this(new SanPhamController(), null);
    }

    public ThemSanPhamView(SanPhamController sanPhamController, Runnable khiThemThanhCong) {
        this.sanPhamController = sanPhamController;
        this.khiThemThanhCong = khiThemThanhCong;

        setSpacing(14);
        setPadding(new Insets(18));
        getStyleClass().add("khung-phan-he");

        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("THÊM SẢN PHẨM MỚI");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Nhập thông tin sản phẩm, nhà cung cấp, giá bán và thiết lập tồn kho an toàn.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        VBox pnlForm = taoKhungNhapLieu();
        getChildren().addAll(pnlTieuDe, pnlForm);

        taiDanhSachNhaCungCap();
        datGiaTriMacDinh();
    }

    private VBox taoKhungNhapLieu() {
        VBox pnlKhung = new VBox(14);
        pnlKhung.getStyleClass().add("hop-noi-dung");

        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(12);
        pnlLuoi.setVgap(12);

        txtTenSanPham.setPromptText("Nhập tên sản phẩm (VD: Pate Whiskas Cá Biển 85g)");
        cboNhaCungCap.setPromptText("-- Chọn nhà cung cấp --");
        cboNhaCungCap.setMaxWidth(Double.MAX_VALUE);

        txtMaVach.setPromptText("Nhập mã vạch duy nhất (VD: 893123456701)");
        txtGiaBan.setPromptText("Nhập giá bán (VD: 50000)");
        txtDonViTinh.setPromptText("Nhập đơn vị tính (Gói, Hộp, Chai, Cái...)");
        txtNguongTonToiThieu.setPromptText("Mặc định: 10");
        txtSoLuongTonBanDau.setPromptText("Mặc định: 0");

        chanNhapSo(txtGiaBan, false);
        chanNhapSo(txtNguongTonToiThieu, true);
        chanNhapSo(txtSoLuongTonBanDau, true);

        pnlLuoi.add(new Label("Tên sản phẩm (*):"), 0, 0);
        pnlLuoi.add(txtTenSanPham, 1, 0);

        pnlLuoi.add(new Label("Nhà cung cấp (*):"), 0, 1);
        pnlLuoi.add(cboNhaCungCap, 1, 1);

        pnlLuoi.add(new Label("Mã vạch (*):"), 0, 2);
        pnlLuoi.add(txtMaVach, 1, 2);

        pnlLuoi.add(new Label("Giá bán (VNĐ) (*):"), 0, 3);
        pnlLuoi.add(txtGiaBan, 1, 3);

        pnlLuoi.add(new Label("Đơn vị tính (*):"), 0, 4);
        pnlLuoi.add(txtDonViTinh, 1, 4);

        pnlLuoi.add(new Label("Ngưỡng an toàn tối thiểu:"), 0, 5);
        pnlLuoi.add(txtNguongTonToiThieu, 1, 5);

        pnlLuoi.add(new Label("Số lượng tồn ban đầu:"), 0, 6);
        pnlLuoi.add(txtSoLuongTonBanDau, 1, 6);

        ColumnConstraints colNhan = new ColumnConstraints();
        colNhan.setMinWidth(190);
        ColumnConstraints colNhap = new ColumnConstraints();
        colNhap.setHgrow(Priority.ALWAYS);
        pnlLuoi.getColumnConstraints().addAll(colNhan, colNhap);

        HBox pnlNut = new HBox(10);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);
        btnLamMoi.getStyleClass().add("nut-phu");
        btnLuu.getStyleClass().add("nut-chinh");
        btnLamMoi.setOnAction(e -> datGiaTriMacDinh());
        btnLuu.setOnAction(e -> xuLyThemSanPham());
        pnlNut.getChildren().addAll(btnLamMoi, btnLuu);

        pnlKhung.getChildren().addAll(pnlLuoi, pnlNut);
        return pnlKhung;
    }

    private void chanNhapSo(TextField tf, boolean soNguyen) {
        tf.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) return;
            if (soNguyen) {
                if (!newVal.matches("\\d*")) {
                    tf.setText(oldVal);
                }
            } else {
                if (!newVal.matches("\\d*(\\.\\d*)?")) {
                    tf.setText(oldVal);
                }
            }
        });
    }

    private void taiDanhSachNhaCungCap() {
        try {
            List<SanPham.NhaCungCapSimple> ds = sanPhamController.layDanhSachNhaCungCap();
            cboNhaCungCap.getItems().setAll(ds);
            if (!ds.isEmpty()) {
                cboNhaCungCap.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            hienThiLoi("Không tải được danh sách nhà cung cấp: " + e.getMessage());
        }
    }

    private void datGiaTriMacDinh() {
        txtTenSanPham.clear();
        txtMaVach.clear();
        txtGiaBan.clear();
        txtDonViTinh.clear();
        txtNguongTonToiThieu.setText("10");
        txtSoLuongTonBanDau.setText("0");
        if (!cboNhaCungCap.getItems().isEmpty()) {
            cboNhaCungCap.getSelectionModel().selectFirst();
        }
        txtTenSanPham.requestFocus();
    }

    private void xuLyThemSanPham() {
        String ten = txtTenSanPham.getText() != null ? txtTenSanPham.getText().trim() : "";
        SanPham.NhaCungCapSimple ncc = cboNhaCungCap.getSelectionModel().getSelectedItem();
        String maVach = txtMaVach.getText() != null ? txtMaVach.getText().trim() : "";
        String chuoiGia = txtGiaBan.getText() != null ? txtGiaBan.getText().trim() : "";
        String donVi = txtDonViTinh.getText() != null ? txtDonViTinh.getText().trim() : "";
        String chuoiNguong = txtNguongTonToiThieu.getText() != null ? txtNguongTonToiThieu.getText().trim() : "10";
        String chuoiTon = txtSoLuongTonBanDau.getText() != null ? txtSoLuongTonBanDau.getText().trim() : "0";

        if (ten.isEmpty()) {
            hienThiLoi("Vui lòng nhập tên sản phẩm!");
            txtTenSanPham.requestFocus();
            return;
        }
        if (ncc == null) {
            hienThiLoi("Vui lòng chọn nhà cung cấp!");
            cboNhaCungCap.requestFocus();
            return;
        }
        if (maVach.isEmpty()) {
            hienThiLoi("Vui lòng nhập mã vạch sản phẩm!");
            txtMaVach.requestFocus();
            return;
        }
        if (chuoiGia.isEmpty()) {
            hienThiLoi("Vui lòng nhập giá bán!");
            txtGiaBan.requestFocus();
            return;
        }

        BigDecimal giaBan;
        try {
            giaBan = new BigDecimal(chuoiGia);
            if (giaBan.compareTo(BigDecimal.ZERO) < 0) {
                hienThiLoi("Giá bán phải >= 0!");
                return;
            }
        } catch (NumberFormatException e) {
            hienThiLoi("Giá bán không hợp lệ!");
            return;
        }

        if (donVi.isEmpty()) {
            hienThiLoi("Vui lòng nhập đơn vị tính!");
            txtDonViTinh.requestFocus();
            return;
        }

        int nguongTon = 10;
        try {
            if (!chuoiNguong.isEmpty()) {
                nguongTon = Integer.parseInt(chuoiNguong);
                if (nguongTon < 0) {
                    hienThiLoi("Ngưỡng tồn an toàn phải >= 0!");
                    return;
                }
            }
        } catch (NumberFormatException e) {
            hienThiLoi("Ngưỡng tồn không hợp lệ!");
            return;
        }

        int soLuongTon = 0;
        try {
            if (!chuoiTon.isEmpty()) {
                soLuongTon = Integer.parseInt(chuoiTon);
                if (soLuongTon < 0) {
                    hienThiLoi("Số lượng tồn ban đầu phải >= 0!");
                    return;
                }
            }
        } catch (NumberFormatException e) {
            hienThiLoi("Số lượng tồn ban đầu không hợp lệ!");
            return;
        }

        SanPham sp = new SanPham();
        sp.setTenSanPham(ten);
        sp.setMaNhaCungCap(ncc.maNhaCungCap());
        sp.setMaVach(maVach);
        sp.setGiaBan(giaBan);
        sp.setDonViTinh(donVi);
        sp.setNguongTonToiThieu(nguongTon);
        sp.setSoLuongTon(soLuongTon);
        sp.setTrangThai(true);

        try {
            String maMoi = sanPhamController.themSanPham(sp);
            Alert thongBao = new Alert(Alert.AlertType.INFORMATION);
            thongBao.setTitle("Thành công");
            thongBao.setHeaderText(null);
            thongBao.setContentText("Thêm sản phẩm thành công! Mã sản phẩm: " + maMoi);
            ganChuDialog(thongBao);
            thongBao.showAndWait();

            datGiaTriMacDinh();
            if (khiThemThanhCong != null) {
                khiThemThanhCong.run();
            }
        } catch (RuntimeException e) {
            hienThiLoi(e.getMessage());
        }
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
