package com.petshop.view;

import com.petshop.controller.DangNhapController;
import com.petshop.model.TaiKhoan;
import com.petshop.util.SessionManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Màn hình Đăng nhập hệ thống (DangNhapView).
 * Hỗ trợ inline validation tách riêng cho từng trường, viền đỏ và thông báo chi tiết.
 */
public class DangNhapView extends StackPane {

    private final Label lblTieuDe = new Label("CỬA HÀNG THÚ CƯNG HAPPY TAILS");
    private final Label lblTieuDePhu = new Label("HỆ THỐNG QUẢN LÝ CỬA HÀNG");
    private final Label lblTenDangNhap = new Label("Tên đăng nhập:");
    private final Label lblMatKhau = new Label("Mật khẩu:");

    // Tách riêng 2 nhãn báo lỗi cho từng ô nhập liệu
    private final Label lblLoiTenDangNhap = new Label();
    private final Label lblLoiMatKhau = new Label();

    private final TextField txtTenDangNhap = new TextField();
    private final PasswordField pwdMatKhau = new PasswordField();

    private final Button btnDangNhap = new Button("Đăng nhập");
    private final Button btnThoat = new Button("Thoát");

    private final DangNhapController dangNhapController = new DangNhapController();
    private final Stage stage;

    public DangNhapView(Stage stage) {
        this.stage = stage;
        getStyleClass().add("man-hinh-dang-nhap");

        VBox pnlHopDangNhap = new VBox(14);
        pnlHopDangNhap.getStyleClass().add("hop-dang-nhap");
        pnlHopDangNhap.setAlignment(Pos.CENTER);

        // Header: Logo và Tiêu đề
        VBox pnlTieuDe = new VBox(6);
        pnlTieuDe.setAlignment(Pos.CENTER);

        try {
            var luongIcon = getClass().getResourceAsStream("/com/petshop/view/logo.png");
            if (luongIcon != null) {
                ImageView imgLogo = new ImageView(new Image(luongIcon));
                imgLogo.setFitWidth(48);
                imgLogo.setFitHeight(48);
                imgLogo.setPreserveRatio(true);
                pnlTieuDe.getChildren().add(imgLogo);
            }
        } catch (Exception ignored) {}

        lblTieuDe.getStyleClass().add("tieu-de-chinh");
        lblTieuDePhu.getStyleClass().add("tieu-de-phu");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblTieuDePhu);

        // Cấu hình style ban đầu cho các nhãn lỗi inline (mặc định ẩn)
        lblLoiTenDangNhap.getStyleClass().add("thong-bao-loi-inline");
        lblLoiTenDangNhap.setVisible(false);
        lblLoiTenDangNhap.setManaged(false);

        lblLoiMatKhau.getStyleClass().add("thong-bao-loi-inline");
        lblLoiMatKhau.setVisible(false);
        lblLoiMatKhau.setManaged(false);

        // Lưới nhập liệu
        GridPane pnlLuoiNhap = new GridPane();
        pnlLuoiNhap.setHgap(12);
        pnlLuoiNhap.setVgap(6); // Khoảng cách giữa các hàng để nhãn lỗi hiển thị sát bên dưới
        pnlLuoiNhap.setAlignment(Pos.CENTER);

        lblTenDangNhap.getStyleClass().add("nhan-nhap-lieu");
        lblMatKhau.getStyleClass().add("nhan-nhap-lieu");

        txtTenDangNhap.setPromptText("Nhập tên đăng nhập");
        txtTenDangNhap.setPrefWidth(240);

        pwdMatKhau.setPromptText("Nhập mật khẩu");
        pwdMatKhau.setPrefWidth(240);

        // Tự động xóa lỗi khi người dùng bắt đầu gõ lại phím
        txtTenDangNhap.textProperty().addListener((obs, o, n) -> xoaLoiTenDangNhap());
        pwdMatKhau.textProperty().addListener((obs, o, n) -> xoaLoiMatKhau());

        // Nhấn phím Enter để đăng nhập nhanh
        txtTenDangNhap.setOnAction(e -> xuLyDangNhap());
        pwdMatKhau.setOnAction(e -> xuLyDangNhap());

        // Sắp xếp bố cục GridPane: Ô nhập nằm hàng chẵn, dòng lỗi nằm ngay hàng lẻ bên dưới
        pnlLuoiNhap.add(lblTenDangNhap, 0, 0);
        pnlLuoiNhap.add(txtTenDangNhap, 1, 0);
        pnlLuoiNhap.add(lblLoiTenDangNhap, 1, 1); // Dòng lỗi nằm dưới tên đăng nhập

        pnlLuoiNhap.add(lblMatKhau, 0, 2);
        pnlLuoiNhap.add(pwdMatKhau, 1, 2);
        pnlLuoiNhap.add(lblLoiMatKhau, 1, 3); // Dòng lỗi nằm dưới mật khẩu

        // Hàng nút thao tác
        HBox pnlNutThaoTac = new HBox(12);
        pnlNutThaoTac.setAlignment(Pos.CENTER_RIGHT);

        btnDangNhap.getStyleClass().add("nut-chinh");
        btnDangNhap.setPrefWidth(110);
        btnDangNhap.setOnAction(e -> xuLyDangNhap());

        btnThoat.getStyleClass().add("nut-phu");
        btnThoat.setPrefWidth(85);
        btnThoat.setOnAction(e -> Platform.exit());

        pnlNutThaoTac.getChildren().addAll(btnThoat, btnDangNhap);

        // Ráp toàn bộ vào hộp đăng nhập
        pnlHopDangNhap.getChildren().addAll(
                pnlTieuDe,
                pnlLuoiNhap,
                pnlNutThaoTac
        );

        // Chân trang
        Label lblChanTrang = new Label("Khoa Công nghệ Thông tin - Trường Đại học Công nghiệp TP.HCM");
        lblChanTrang.getStyleClass().add("dong-chan-trang");
        StackPane.setAlignment(lblChanTrang, Pos.BOTTOM_CENTER);
        StackPane.setMargin(lblChanTrang, new Insets(0, 0, 14, 0));

        getChildren().addAll(pnlHopDangNhap, lblChanTrang);
    }

    /** Xóa lỗi và reset viền cho ô Tên đăng nhập */
    private void xoaLoiTenDangNhap() {
        txtTenDangNhap.setStyle("");
        if (lblLoiTenDangNhap.isVisible()) {
            lblLoiTenDangNhap.setVisible(false);
            lblLoiTenDangNhap.setManaged(false);
            lblLoiTenDangNhap.setText("");
        }
    }

    /** Xóa lỗi và reset viền cho ô Mật khẩu */
    private void xoaLoiMatKhau() {
        pwdMatKhau.setStyle("");
        if (lblLoiMatKhau.isVisible()) {
            lblLoiMatKhau.setVisible(false);
            lblLoiMatKhau.setManaged(false);
            lblLoiMatKhau.setText("");
        }
    }

    /** Hiển thị viền đỏ và thông báo lỗi dưới ô Tên đăng nhập */
    private void hienLoiTenDangNhap(String thongBao) {
        txtTenDangNhap.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 1.5px; -fx-border-radius: 4px;");
        lblLoiTenDangNhap.setText(thongBao);
        lblLoiTenDangNhap.setVisible(true);
        lblLoiTenDangNhap.setManaged(true);
    }

    /** Hiển thị viền đỏ và thông báo lỗi dưới ô Mật khẩu */
    private void hienLoiMatKhau(String thongBao) {
        pwdMatKhau.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 1.5px; -fx-border-radius: 4px;");
        lblLoiMatKhau.setText(thongBao);
        lblLoiMatKhau.setVisible(true);
        lblLoiMatKhau.setManaged(true);
    }

    /** Xử lý xác thực tài khoản với các trạng thái Inline Validation & Alert */
    private void xuLyDangNhap() {
        xoaLoiTenDangNhap();
        xoaLoiMatKhau();

        String tenDangNhap = txtTenDangNhap.getText() != null ? txtTenDangNhap.getText().trim() : "";
        String matKhau = pwdMatKhau.getText() != null ? pwdMatKhau.getText() : "";

        boolean coLoi = false;
        
        // Trạng thái Inline Validation khi để trống
        if (tenDangNhap.isEmpty()) {
            hienLoiTenDangNhap("Vui lòng nhập tên đăng nhập");
            txtTenDangNhap.requestFocus();
            coLoi = true;
        }

        if (matKhau.isEmpty()) {
            hienLoiMatKhau("Mật khẩu không được để trống");
            if (!coLoi) {
                pwdMatKhau.requestFocus();
            }
            coLoi = true;
        }

        if (coLoi) return;

        TaiKhoan taiKhoan;
        try {
            taiKhoan = dangNhapController.xacThuc(tenDangNhap, matKhau);
        } catch (IllegalStateException ex) {
            hienThiCanhBao("Lỗi hệ thống", "Lỗi kết nối cơ sở dữ liệu SQL Server!");
            return;
        }

        // Sai thông tin đăng nhập
        if (taiKhoan == null) {
            hienThiCanhBao("Lỗi đăng nhập", "Tên đăng nhập hoặc mật khẩu không chính xác");
            pwdMatKhau.clear();
            pwdMatKhau.requestFocus();
            return;
        }

        // Tài khoản bị khóa / vô hiệu hóa
        if (!taiKhoan.isTrangThai()) {
            hienThiCanhBao("Lỗi đăng nhập", "Tài khoản không khả dụng hoặc đã bị khóa");
            return;
        }

        // Đăng nhập thành công -> Lưu phiên và chuyển hướng màn hình chính
        SessionManager.dangNhap(taiKhoan);

        stage.setTitle("Happy Tails - Quản lý cửa hàng thú cưng");
        Scene scene = new Scene(new ManHinhChinhView(stage), 1280, 760);
        scene.getStylesheets().addAll(
                getClass().getResource("/com/petshop/view/css/chung.css").toExternalForm(),
                getClass().getResource("/com/petshop/view/css/manhinhchinh.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMaximized(true);
    }

    private void hienThiCanhBao(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}