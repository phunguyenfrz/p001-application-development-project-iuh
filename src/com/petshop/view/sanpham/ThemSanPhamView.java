package com.petshop.view.sanpham;

import com.petshop.controller.SanPhamController;
import com.petshop.model.NhaCungCap;
import com.petshop.model.SanPham;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Màn hình nhỏ (popup) dùng để thêm mới sản phẩm vào kho hàng.
 * Thiết kế giao diện đồng bộ hoàn toàn theo cách bố trí của phân hệ Dịch vụ.
 */
public class ThemSanPhamView extends VBox {

    private static final Locale LOCALE_VI_VN = new Locale("vi", "VN");

    private final SanPhamController sanPhamController = new SanPhamController();
    private final Runnable khiThemThanhCong;

    private final TextField txtMaSanPham = new TextField();
    private final TextField txtTenSanPham = new TextField();
    private final ComboBox<String> cboLoaiSanPham = new ComboBox<>();
    private final ComboBox<NhaCungCap> cboNhaCungCap = new ComboBox<>();
    private final TextField txtGiaBan = new TextField();
    private final TextField txtSoLuongTon = new TextField();
    private final TextField txtNguongTon = new TextField();

    private final Button btnThem = new Button("Lưu thêm mới");
    private final Button btnLamMoi = new Button("Làm mới");
    private final Button btnDong = new Button("Đóng");

    public ThemSanPhamView() {
        this(null);
    }

    public ThemSanPhamView(Runnable khiThemThanhCong) {
        this.khiThemThanhCong = khiThemThanhCong;
        setSpacing(14);
        setPadding(new Insets(18));
        getStyleClass().add("khung-phan-he");

        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("THÊM SẢN PHẨM MỚI");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Nhập thông tin chi tiết sản phẩm để thêm vào kho hàng.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        VBox pnlForm = taoKhungNhapLieu();
        getChildren().addAll(pnlTieuDe, pnlForm);

        xoaTrangForm();
    }

    private VBox taoKhungNhapLieu() {
        VBox pnlKhung = new VBox(14);
        pnlKhung.getStyleClass().add("hop-noi-dung");

        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(12);
        pnlLuoi.setVgap(10);

        txtMaSanPham.setPromptText("Tự động sinh (SP-yyyyMMdd-xxxxxx)");
        txtMaSanPham.setEditable(false);
        txtMaSanPham.setStyle("-fx-background-color: #f1f5f9;");

        txtTenSanPham.setPromptText("Nhập tên sản phẩm (VD: Hạt Royal Canin Indoor 2kg)");

        cboLoaiSanPham.getItems().addAll("Lọ", "Gói", "Hộp", "Chai", "Túi", "Cái");
        cboLoaiSanPham.setValue("Lọ");
        cboLoaiSanPham.setMaxWidth(Double.MAX_VALUE);

        cboNhaCungCap.setPromptText("-- Chọn nhà cung cấp (tùy chọn) --");
        cboNhaCungCap.setMaxWidth(Double.MAX_VALUE);

        txtGiaBan.setPromptText("Nhập giá bán (VNĐ)");
        txtSoLuongTon.setPromptText("Nhập số lượng tồn ban đầu (mặc định 0)");
        txtNguongTon.setPromptText("Nhập ngưỡng an toàn (mặc định 10)");

        chanNhapSoNguyen(txtSoLuongTon);
        chanNhapSoNguyen(txtNguongTon);
        ganDinhDangTienChoO(txtGiaBan);

        pnlLuoi.add(new Label("Mã sản phẩm:"), 0, 0);
        pnlLuoi.add(txtMaSanPham, 1, 0);

        pnlLuoi.add(new Label("Tên sản phẩm (*):"), 0, 1);
        pnlLuoi.add(txtTenSanPham, 1, 1);

        pnlLuoi.add(new Label("Loại sản phẩm (*):"), 0, 2);
        pnlLuoi.add(cboLoaiSanPham, 1, 2);

        pnlLuoi.add(new Label("Nhà cung cấp:"), 0, 3);
        pnlLuoi.add(cboNhaCungCap, 1, 3);

        pnlLuoi.add(new Label("Giá bán (VNĐ) (*):"), 0, 4);
        pnlLuoi.add(txtGiaBan, 1, 4);

        pnlLuoi.add(new Label("Số lượng tồn ban đầu:"), 0, 5);
        pnlLuoi.add(txtSoLuongTon, 1, 5);

        pnlLuoi.add(new Label("Ngưỡng an toàn (*):"), 0, 6);
        pnlLuoi.add(txtNguongTon, 1, 6);

        ColumnConstraints colNhan = new ColumnConstraints();
        colNhan.setMinWidth(170);
        ColumnConstraints colNhap = new ColumnConstraints();
        colNhap.setHgrow(Priority.ALWAYS);
        pnlLuoi.getColumnConstraints().addAll(colNhan, colNhap);

        HBox pnlNut = new HBox(10);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        btnThem.getStyleClass().add("nut-chinh");
        btnThem.setOnAction(e -> xuLyThem());

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> xoaTrangForm());

        btnDong.getStyleClass().add("nut-phu");
        btnDong.setOnAction(e -> dongCuaSo());

        pnlNut.getChildren().addAll(btnLamMoi, btnDong, btnThem);
        pnlKhung.getChildren().addAll(pnlLuoi, pnlNut);
        return pnlKhung;
    }

    private void chanNhapSoNguyen(TextField txt) {
        txt.textProperty().addListener((obs, cu, moi) -> {
            if (moi != null && !moi.matches("\\d*")) {
                txt.setText(cu);
            }
        });
    }

    private void ganDinhDangTienChoO(TextField txt) {
        txt.textProperty().addListener((obs, cu, moi) -> {
            if (moi != null && !moi.matches("[\\d.\\sđ]*")) {
                txt.setText(cu);
            }
        });
        txt.focusedProperty().addListener((obs, cu, dangChon) -> {
            if (!dangChon) {
                dinhDangLaiO(txt);
            }
        });
    }

    private void dinhDangLaiO(TextField txt) {
        String chuoi = boKyTuDinhDangTien(txt.getText());
        if (chuoi.isEmpty()) {
            return;
        }
        try {
            double soTien = Double.parseDouble(chuoi);
            if (soTien >= 0) {
                txt.setText(dinhDangTien(soTien));
            }
        } catch (NumberFormatException ignored) {
        }
    }

    public static String dinhDangTien(double soTien) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(LOCALE_VI_VN);
        return nf.format(soTien);
    }

    public static String boKyTuDinhDangTien(String chuoi) {
        if (chuoi == null) return "";
        return chuoi.replaceAll("[^\\d]", "").trim();
    }

    private void xuLyThem() {
        try {
            SanPham sp = docFormSanPham();
            boolean thanhCong = sanPhamController.themSanPham(sp);
            if (thanhCong) {
                hienThiThongBao("Thêm sản phẩm mới thành công!");
                if (khiThemThanhCong != null) {
                    khiThemThanhCong.run();
                }
                dongCuaSo();
            }
        } catch (IllegalArgumentException | SecurityException ex) {
            hienThiCanhBao("Dữ liệu không hợp lệ: " + ex.getMessage());
        } catch (IllegalStateException ex) {
            hienThiLoi("Lỗi cơ sở dữ liệu: " + ex.getMessage());
        }
    }

    private SanPham docFormSanPham() {
        String ten = txtTenSanPham.getText() == null ? "" : txtTenSanPham.getText().trim();
        if (ten.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập tên sản phẩm!");
        }

        String loaiSp = cboLoaiSanPham.getValue();
        if (loaiSp == null || loaiSp.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn loại sản phẩm!");
        }

        String chuoiGia = boKyTuDinhDangTien(txtGiaBan.getText());
        if (chuoiGia.isEmpty()) {
            throw new IllegalArgumentException("Giá bán không được để trống!");
        }
        BigDecimal gia = new BigDecimal(chuoiGia);
        if (gia.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá bán phải lớn hơn hoặc bằng 0!");
        }

        int ton = 0;
        if (txtSoLuongTon.getText() != null && !txtSoLuongTon.getText().trim().isEmpty()) {
            ton = Integer.parseInt(txtSoLuongTon.getText().trim());
        }

        int nguong = 10;
        if (txtNguongTon.getText() != null && !txtNguongTon.getText().trim().isEmpty()) {
            nguong = Integer.parseInt(txtNguongTon.getText().trim());
        }

        String maSp = txtMaSanPham.getText().trim();
        String maVach = "893" + maSp.replace("SP-", "").replace("-", "");
        NhaCungCap ncc = cboNhaCungCap.getValue();

        SanPham sp = new SanPham();
        sp.setMaSanPham(maSp);
        sp.setTenSanPham(ten);
        sp.setMaVach(maVach);
        sp.setGiaBan(gia);
        sp.setSoLuongTon(ton);
        sp.setNguongTonToiThieu(nguong);
        sp.setDonViTinh(loaiSp);
        sp.setMaNhaCungCap(ncc != null ? ncc.getMaNhaCungCap() : null);
        sp.setTrangThai(true);

        return sp;
    }

    private void xoaTrangForm() {
        txtMaSanPham.setText(sanPhamController.taoMaSanPham());
        txtTenSanPham.clear();
        cboLoaiSanPham.setValue("Lọ");
        txtGiaBan.clear();
        txtSoLuongTon.setText("0");
        txtNguongTon.setText("10");
        napDanhSachNhaCungCap();
        txtTenSanPham.requestFocus();
    }

    private void napDanhSachNhaCungCap() {
        try {
            List<NhaCungCap> dsNcc = sanPhamController.layDanhSachNhaCungCap();
            cboNhaCungCap.getItems().setAll(dsNcc);
            cboNhaCungCap.setValue(null);
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            cboNhaCungCap.getItems().clear();
        }
    }

    private void dongCuaSo() {
        if (getScene() != null && getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
    }

    private void hienThiThongBao(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }

    private void hienThiCanhBao(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Dữ liệu không hợp lệ");
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }

    private void hienThiLoi(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}
