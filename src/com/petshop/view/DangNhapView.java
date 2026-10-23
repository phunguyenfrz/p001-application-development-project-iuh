package com.petshop.view;

import com.petshop.controller.DangNhapController;
import com.petshop.model.TaiKhoan;
import com.petshop.util.SessionManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
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
 * Thiết kế chuẩn ứng dụng Desktop theo quy định môn học Phát triển ứng dụng (IUH).
 * Quy ước tiền tố controls: lbl, txt, pwd, btn.
 */
public class DangNhapView extends StackPane {

    // Khai báo điều khiển giao diện theo quy tắc tiền tố: lbl, txt, pwd, btn
    private final Label lblTieuDe = new Label("CỬA HÀNG THÚ CƯNG HAPPY TAILS");
    private final Label lblTieuDePhu = new Label("HỆ THỐNG QUẢN LÝ CỬA HÀNG");
    private final Label lblTenDangNhap = new Label("Tên đăng nhập:");
    private final Label lblMatKhau = new Label("Mật khẩu:");
    private final Label lblThongBaoLoi = new Label();

    private final TextField txtTenDangNhap = new TextField();
    private final PasswordField pwdMatKhau = new PasswordField();

    private final Button btnDangNhap = new Button("Đăng nhập");
    private final Button btnThoat = new Button("Thoát");

    private final DangNhapController dangNhapController = new DangNhapController();
    private final Stage stage;

    public DangNhapView(Stage stage) {
        this.stage = stage;
        getStyleClass().add("man-hinh-dang-nhap");

        // 1. Khung hộp đăng nhập trung tâm
        VBox pnlHopDangNhap = new VBox(16);
        pnlHopDangNhap.getStyleClass().add("hop-dang-nhap");
        pnlHopDangNhap.setAlignment(Pos.CENTER);

        // Header: Logo cửa hàng và tiêu đề
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

        // Dòng thông báo lỗi (mặc định ẩn)
        lblThongBaoLoi.getStyleClass().add("thong-bao-loi");
        lblThongBaoLoi.setVisible(false);
        lblThongBaoLoi.setManaged(false);

        // Lưới nhập liệu: Label bên trái, TextField/PasswordField bên phải thẳng hàng
        GridPane pnlLuoiNhap = new GridPane();
        pnlLuoiNhap.setHgap(12);
        pnlLuoiNhap.setVgap(14);
        pnlLuoiNhap.setAlignment(Pos.CENTER);

        lblTenDangNhap.getStyleClass().add("nhan-nhap-lieu");
        lblMatKhau.getStyleClass().add("nhan-nhap-lieu");

        txtTenDangNhap.setPromptText("Nhập tên đăng nhập");
        txtTenDangNhap.setPrefWidth(240);

        pwdMatKhau.setPromptText("Nhập mật khẩu");
        pwdMatKhau.setPrefWidth(240);

        // Xóa thông báo lỗi khi người dùng bắt đầu gõ phím
        txtTenDangNhap.textProperty().addListener((obs, o, n) -> anThongBaoLoi());
        pwdMatKhau.textProperty().addListener((obs, o, n) -> anThongBaoLoi());

        // Nhấn phím Enter ở bất kỳ ô nào để thực hiện đăng nhập
        txtTenDangNhap.setOnAction(e -> xuLyDangNhap());
        pwdMatKhau.setOnAction(e -> xuLyDangNhap());

        pnlLuoiNhap.add(lblTenDangNhap, 0, 0);
        pnlLuoiNhap.add(txtTenDangNhap, 1, 0);
        pnlLuoiNhap.add(lblMatKhau, 0, 1);
        pnlLuoiNhap.add(pwdMatKhau, 1, 1);

        // Hàng nút thao tác: Đăng nhập & Thoát
        HBox pnlNutThaoTac = new HBox(12);
        pnlNutThaoTac.setAlignment(Pos.CENTER_RIGHT);

        btnDangNhap.getStyleClass().add("nut-chinh");
        btnDangNhap.setPrefWidth(110);
        btnDangNhap.setOnAction(e -> xuLyDangNhap());

        btnThoat.getStyleClass().add("nut-phu");
        btnThoat.setPrefWidth(85);
        btnThoat.setOnAction(e -> Platform.exit());

        pnlNutThaoTac.getChildren().addAll(btnThoat, btnDangNhap);

        // Ráp các phần tử vào hộp đăng nhập
        pnlHopDangNhap.getChildren().addAll(
                pnlTieuDe,
                lblThongBaoLoi,
                pnlLuoiNhap,
                pnlNutThaoTac
        );

        // 2. Dòng thông tin khoa/trường phía đáy màn hình
        Label lblChanTrang = new Label("Khoa Công nghệ Thông tin - Trường Đại học Công nghiệp TP.HCM");
        lblChanTrang.getStyleClass().add("dong-chan-trang");
        StackPane.setAlignment(lblChanTrang, Pos.BOTTOM_CENTER);
        StackPane.setMargin(lblChanTrang, new Insets(0, 0, 14, 0));

        getChildren().addAll(pnlHopDangNhap, lblChanTrang);
    }

    /**
     * Ẩn thông báo lỗi khi người dùng thay đổi thông tin nhập
     */
    private void anThongBaoLoi() {
        if (lblThongBaoLoi.isVisible()) {
            lblThongBaoLoi.setVisible(false);
            lblThongBaoLoi.setManaged(false);
            lblThongBaoLoi.setText("");
        }
    }

    /**
     * Hiển thị thông báo lỗi lên giao diện
     */
    private void hienThiLoi(String thongBao) {
        lblThongBaoLoi.setText(thongBao);
        lblThongBaoLoi.setVisible(true);
        lblThongBaoLoi.setManaged(true);
    }

    /**
     * Xử lý xác thực tài khoản và chuyển hướng vào màn hình chính
     */
    private void xuLyDangNhap() {
        String tenDangNhap = txtTenDangNhap.getText() != null ? txtTenDangNhap.getText().trim() : "";
        String matKhau = pwdMatKhau.getText() != null ? pwdMatKhau.getText() : "";

        if (tenDangNhap.isEmpty()) {
            hienThiLoi("Vui lòng nhập tên đăng nhập!");
            txtTenDangNhap.requestFocus();
            return;
        }

        if (matKhau.isEmpty()) {
            hienThiLoi("Vui lòng nhập mật khẩu!");
            pwdMatKhau.requestFocus();
            return;
        }

        TaiKhoan taiKhoan;
        try {
            taiKhoan = dangNhapController.xacThuc(tenDangNhap, matKhau);
        } catch (IllegalStateException ex) {
            hienThiLoi("Lỗi kết nối cơ sở dữ liệu SQL Server!");
            return;
        }

        if (taiKhoan == null) {
            hienThiLoi("Tên đăng nhập hoặc mật khẩu không chính xác!");
            pwdMatKhau.clear();
            pwdMatKhau.requestFocus();
            return;
        }

        // Lưu thông tin phiên đăng nhập
        SessionManager.dangNhap(taiKhoan);

        // Chuyển sang màn hình chính (ManHinhChinhView)
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
}
