package com.petshop.view.nhacungcap;

import com.petshop.controller.NhaCungCapController;
import com.petshop.model.NhaCungCap;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Màn hình nhập thêm nhà cung cấp mới.
 * Tuân thủ phong cách giao diện ThemKhuyenMaiView.
 */
public class ThemNhaCungCapView extends VBox {

    private final NhaCungCapController controller = new NhaCungCapController();
    private final Runnable khiThemThanhCong;

    private final TextField txtTenNhaCungCap = new TextField();
    private final TextField txtSoDienThoai = new TextField();
    private final TextField txtEmail = new TextField();
    private final TextField txtDiaChi = new TextField();
    private final Button btnThem = new Button("Lưu thêm mới");

    public ThemNhaCungCapView() {
        this(null);
    }

    public ThemNhaCungCapView(Runnable khiThemThanhCong) {
        this.khiThemThanhCong = khiThemThanhCong;
        setSpacing(14);
        setPadding(new Insets(18));
        getStyleClass().add("khung-phan-he");

        Label lblTieuDe = new Label("THÊM NHÀ CUNG CẤP");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Nhập thông tin đối tác cung cấp hàng hóa cho cửa hàng thú cưng.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");

        getChildren().addAll(lblTieuDe, lblMoTa, taoKhungNhapLieu());

        // Bộ lọc chỉ cho nhập ký tự số vào ô SĐT
        txtSoDienThoai.textProperty().addListener((obs, cu, moi) -> {
            if (moi == null) return;
            if (!moi.matches("\\d*")) {
                txtSoDienThoai.setText(moi.replaceAll("[^\\d]", ""));
            } else if (moi.length() > 10) {
                txtSoDienThoai.setText(moi.substring(0, 10));
            }
        });
    }

    private VBox taoKhungNhapLieu() {
        VBox khung = new VBox(14);
        khung.getStyleClass().add("hop-noi-dung");

        GridPane luoi = new GridPane();
        luoi.setHgap(12);
        luoi.setVgap(10);

        txtTenNhaCungCap.setPromptText("Ví dụ: Công ty TNHH Thức Ăn Pet VN");
        txtSoDienThoai.setPromptText("10 chữ số bắt đầu bằng 0 (ví dụ: 0912345678)");
        txtEmail.setPromptText("Ví dụ: contact@petvn.com (tùy chọn)");
        txtDiaChi.setPromptText("Địa chỉ văn phòng / kho hàng (tùy chọn)");

        luoi.add(new Label("Tên nhà cung cấp (*):"), 0, 0);
        luoi.add(txtTenNhaCungCap, 1, 0);
        luoi.add(new Label("Số điện thoại (*):"), 0, 1);
        luoi.add(txtSoDienThoai, 1, 1);
        luoi.add(new Label("Email:"), 0, 2);
        luoi.add(txtEmail, 1, 2);
        luoi.add(new Label("Địa chỉ:"), 0, 3);
        luoi.add(txtDiaChi, 1, 3);

        GridPane.setHgrow(txtTenNhaCungCap, Priority.ALWAYS);
        GridPane.setHgrow(txtSoDienThoai, Priority.ALWAYS);
        GridPane.setHgrow(txtEmail, Priority.ALWAYS);
        GridPane.setHgrow(txtDiaChi, Priority.ALWAYS);

        HBox nut = new HBox(10);
        nut.setAlignment(Pos.CENTER_RIGHT);
        Button btnLamMoi = new Button("Làm mới");
        Button btnDong = new Button("Đóng");

        btnThem.getStyleClass().add("nut-chinh");
        btnLamMoi.getStyleClass().add("nut-phu");
        btnDong.getStyleClass().add("nut-phu");

        btnThem.setOnAction(e -> xuLyThem());
        btnLamMoi.setOnAction(e -> xoaTrangForm());
        btnDong.setOnAction(e -> dongCuaSo());

        nut.getChildren().addAll(btnLamMoi, btnDong, btnThem);
        khung.getChildren().addAll(luoi, nut);
        return khung;
    }

    private NhaCungCap docForm() {
        String ten = txtTenNhaCungCap.getText() == null ? "" : txtTenNhaCungCap.getText().trim();
        String sdt = txtSoDienThoai.getText() == null ? "" : txtSoDienThoai.getText().trim();
        String email = txtEmail.getText() == null ? null : txtEmail.getText().trim();
        String diaChi = txtDiaChi.getText() == null ? null : txtDiaChi.getText().trim();

        return new NhaCungCap(null, ten, sdt, diaChi, email, true);
    }

    private void xuLyThem() {
        try {
            NhaCungCap ncc = docForm();
            if (controller.themNhaCungCap(ncc)) {
                hienThiThongBao("Thành công", "Đã thêm nhà cung cấp: " + ncc.getTenNhaCungCap());
                if (khiThemThanhCong != null) {
                    khiThemThanhCong.run();
                }
                dongCuaSo();
            } else {
                hienThiLoi("Không thể thêm nhà cung cấp vào cơ sở dữ liệu!");
            }
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienThiLoi(ex.getMessage());
        }
    }

    private void xoaTrangForm() {
        txtTenNhaCungCap.clear();
        txtSoDienThoai.clear();
        txtEmail.clear();
        txtDiaChi.clear();
        txtTenNhaCungCap.requestFocus();
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

    private void hienThiLoi(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Thông báo lỗi");
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}

