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
import java.util.List;

/**
 * Màn hình nhỏ (popup/dialog) dùng để thêm mới sản phẩm.
 * Tiền tố điều khiển: lbl, txt, btn, cbo.
 */
public class ThemSanPhamView extends VBox {

    private final SanPhamController sanPhamController = new SanPhamController();
    private final Runnable khiThemThanhCong;

    private final TextField txtMaSanPham = new TextField();
    private final TextField txtTenSanPham = new TextField();
    private final TextField txtMaVach = new TextField();
    private final TextField txtGiaBan = new TextField();
    private final TextField txtSoLuongTon = new TextField();
    private final TextField txtNguongTon = new TextField();
    private final TextField txtDonViTinh = new TextField();
    private final ComboBox<NhaCungCap> cboNhaCungCap = new ComboBox<>();

    private final Button btnLuu = new Button("Lưu sản phẩm");
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

        GridPane pnlForm = taoKhungNhapLieu();
        HBox pnlNut = taoThanhNutBam();

        getChildren().addAll(pnlTieuDe, pnlForm, pnlNut);
        lamMoiForm();
    }

    private GridPane taoKhungNhapLieu() {
        GridPane pnlGrid = new GridPane();
        pnlGrid.setHgap(14);
        pnlGrid.setVgap(12);
        pnlGrid.getStyleClass().add("hop-noi-dung");
        pnlGrid.setPadding(new Insets(16));

        txtMaSanPham.setPromptText("Tự động sinh (SP-yyyyMMdd-xxxxxx)");
        txtMaSanPham.setEditable(false);
        txtMaSanPham.setStyle("-fx-background-color: #f1f5f9;");

        cboNhaCungCap.setPromptText("-- Chọn nhà cung cấp --");
        cboNhaCungCap.setMaxWidth(Double.MAX_VALUE);

        txtTenSanPham.setPromptText("Nhập tên sản phẩm (*)...");
        txtMaVach.setPromptText("Nhập mã vạch (*)...");
        txtGiaBan.setPromptText("Nhập giá bán (*, chỉ số)...");
        txtSoLuongTon.setPromptText("Số lượng tồn ban đầu (mặc định 0)...");
        txtNguongTon.setPromptText("Ngưỡng tồn an toàn (mặc định 10)...");
        txtDonViTinh.setPromptText("Cái, Gói, Hộp, Chai (*)...");

        // Chỉ cho phép nhập số ở ô giá bán, số lượng, ngưỡng tồn
        datBoLocChiSo(txtGiaBan);
        datBoLocChiSo(txtSoLuongTon);
        datBoLocChiSo(txtNguongTon);

        pnlGrid.add(new Label("Mã sản phẩm:"), 0, 0);
        pnlGrid.add(txtMaSanPham, 1, 0);

        pnlGrid.add(new Label("Nhà cung cấp:"), 2, 0);
        pnlGrid.add(cboNhaCungCap, 3, 0);

        pnlGrid.add(new Label("Tên sản phẩm (*):"), 0, 1);
        pnlGrid.add(txtTenSanPham, 1, 1);

        pnlGrid.add(new Label("Mã vạch (*):"), 2, 1);
        pnlGrid.add(txtMaVach, 3, 1);

        pnlGrid.add(new Label("Giá bán (VNĐ) (*):"), 0, 2);
        pnlGrid.add(txtGiaBan, 1, 2);

        pnlGrid.add(new Label("Đơn vị tính (*):"), 2, 2);
        pnlGrid.add(txtDonViTinh, 3, 2);

        pnlGrid.add(new Label("Số lượng tồn:"), 0, 3);
        pnlGrid.add(txtSoLuongTon, 1, 3);

        pnlGrid.add(new Label("Ngưỡng an toàn:"), 2, 3);
        pnlGrid.add(txtNguongTon, 3, 3);

        ColumnConstraints col1 = new ColumnConstraints(120);
        ColumnConstraints col2 = new ColumnConstraints(220);
        ColumnConstraints col3 = new ColumnConstraints(120);
        ColumnConstraints col4 = new ColumnConstraints(220);
        pnlGrid.getColumnConstraints().addAll(col1, col2, col3, col4);

        return pnlGrid;
    }

    private HBox taoThanhNutBam() {
        HBox pnlNut = new HBox(10);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);
        pnlNut.setPadding(new Insets(10, 0, 0, 0));

        btnLuu.getStyleClass().add("nut-chinh");
        btnLuu.setOnAction(e -> xuLyLuuSanPham());

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> lamMoiForm());

        btnDong.getStyleClass().add("nut-phu");
        btnDong.setOnAction(e -> dongCuaSo());

        pnlNut.getChildren().addAll(btnLamMoi, btnDong, btnLuu);
        return pnlNut;
    }

    private void xuLyLuuSanPham() {
        try {
            SanPham sp = docDuLieuTuForm();
            boolean thanhCong = sanPhamController.themSanPham(sp);
            if (thanhCong) {
                hienThiThongBao("Thông báo", "Thêm sản phẩm mới thành công!");
                if (khiThemThanhCong != null) {
                    khiThemThanhCong.run();
                }
                dongCuaSo();
            }
        } catch (IllegalArgumentException | SecurityException ex) {
            hienThiCanhBao("Dữ liệu không hợp lệ", ex.getMessage());
        } catch (IllegalStateException ex) {
            hienThiLoi("Lỗi cơ sở dữ liệu", ex.getMessage());
        }
    }

    private SanPham docDuLieuTuForm() {
        String ten = txtTenSanPham.getText().trim();
        String maVach = txtMaVach.getText().trim();
        String donVi = txtDonViTinh.getText().trim();
        NhaCungCap ncc = cboNhaCungCap.getValue();

        BigDecimal gia = BigDecimal.ZERO;
        if (!txtGiaBan.getText().trim().isEmpty()) {
            gia = new BigDecimal(txtGiaBan.getText().trim());
        }

        int ton = 0;
        if (!txtSoLuongTon.getText().trim().isEmpty()) {
            ton = Integer.parseInt(txtSoLuongTon.getText().trim());
        }

        int nguong = 10;
        if (!txtNguongTon.getText().trim().isEmpty()) {
            nguong = Integer.parseInt(txtNguongTon.getText().trim());
        }

        SanPham sp = new SanPham();
        sp.setMaSanPham(txtMaSanPham.getText().trim());
        sp.setTenSanPham(ten);
        sp.setMaVach(maVach);
        sp.setGiaBan(gia);
        sp.setSoLuongTon(ton);
        sp.setNguongTonToiThieu(nguong);
        sp.setDonViTinh(donVi);
        sp.setMaNhaCungCap(ncc != null ? ncc.getMaNhaCungCap() : null);
        sp.setTrangThai(true);

        return sp;
    }

    private void lamMoiForm() {
        txtMaSanPham.setText(sanPhamController.taoMaSanPham());
        txtTenSanPham.clear();
        txtMaVach.clear();
        txtGiaBan.clear();
        txtSoLuongTon.setText("0");
        txtNguongTon.setText("10");
        txtDonViTinh.clear();
        napDanhSachNhaCungCap();
        txtTenSanPham.requestFocus();
    }

    private void napDanhSachNhaCungCap() {
        try {
            List<NhaCungCap> dsNcc = sanPhamController.layDanhSachNhaCungCap();
            cboNhaCungCap.getItems().setAll(dsNcc);
            cboNhaCungCap.setValue(null);
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            // Khi không kết nối được CSDL, vẫn cho phép form hoạt động
            cboNhaCungCap.getItems().clear();
        }
    }

    private void datBoLocChiSo(TextField txt) {
        txt.textProperty().addListener((obs, cu, moi) -> {
            if (!moi.matches("\\d*")) {
                txt.setText(cu);
            }
        });
    }

    private void dongCuaSo() {
        if (getScene() != null && getScene().getWindow() instanceof Stage stage) {
            stage.close();
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

    private void hienThiLoi(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}
