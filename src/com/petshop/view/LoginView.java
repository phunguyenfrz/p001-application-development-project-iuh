package com.petshop.view;

import com.petshop.controller.LoginController;
import com.petshop.model.User;
import com.petshop.util.SessionManager;
import javafx.application.Platform;
import javafx.geometry.HPos;
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
 * Màn hình Đăng nhập hệ thống (LoginView).
 * Thiết kế chuẩn ứng dụng Desktop theo quy định môn học Phát triển ứng dụng (IUH).
 * Quy ước tiền tố controls: lbl, txt, pwd, btn.
 */
public class LoginView extends StackPane {

    // Khai báo điều khiển giao diện theo quy tắc tiền tố: lbl, txt, pwd, btn
    private final Label lblTitle = new Label("CỬA HÀNG THÚ CƯNG HAPPY TAILS");
    private final Label lblSubtitle = new Label("HỆ THỐNG QUẢN LÝ CỬA HÀNG");
    private final Label lblUsername = new Label("Tên đăng nhập:");
    private final Label lblPassword = new Label("Mật khẩu:");
    private final Label lblErrorMessage = new Label();

    private final TextField txtUsername = new TextField();
    private final PasswordField pwdPassword = new PasswordField();

    private final Button btnLogin = new Button("Đăng nhập");
    private final Button btnExit = new Button("Thoát");

    private final LoginController loginController = new LoginController();
    private final Stage stage;

    public LoginView(Stage stage) {
        this.stage = stage;
        getStyleClass().add("login-root");

        // 1. Khung hộp đăng nhập trung tâm
        VBox pnlLoginBox = new VBox(16);
        pnlLoginBox.getStyleClass().add("login-box");
        pnlLoginBox.setAlignment(Pos.CENTER);

        // Header: Logo cửa hàng và tiêu đề
        VBox pnlHeader = new VBox(6);
        pnlHeader.setAlignment(Pos.CENTER);

        try {
            var iconStream = getClass().getResourceAsStream("/com/petshop/view/logo.png");
            if (iconStream != null) {
                ImageView imgLogo = new ImageView(new Image(iconStream));
                imgLogo.setFitWidth(48);
                imgLogo.setFitHeight(48);
                imgLogo.setPreserveRatio(true);
                pnlHeader.getChildren().add(imgLogo);
            }
        } catch (Exception ignored) {}

        lblTitle.getStyleClass().add("login-system-title");
        lblSubtitle.getStyleClass().add("login-subtitle");
        pnlHeader.getChildren().addAll(lblTitle, lblSubtitle);

        // Dòng thông báo lỗi (mặc định ẩn)
        lblErrorMessage.getStyleClass().add("login-error-text");
        lblErrorMessage.setVisible(false);
        lblErrorMessage.setManaged(false);

        // Lưới nhập liệu: Label bên trái, TextField/PasswordField bên phải thẳng hàng
        GridPane pnlFormGrid = new GridPane();
        pnlFormGrid.setHgap(12);
        pnlFormGrid.setVgap(14);
        pnlFormGrid.setAlignment(Pos.CENTER);

        lblUsername.getStyleClass().add("login-form-label");
        lblPassword.getStyleClass().add("login-form-label");

        txtUsername.setPromptText("Nhập tên đăng nhập");
        txtUsername.setPrefWidth(240);

        pwdPassword.setPromptText("Nhập mật khẩu");
        pwdPassword.setPrefWidth(240);

        // Xóa thông báo lỗi khi người dùng bắt đầu gõ phím
        txtUsername.textProperty().addListener((obs, o, n) -> anThongBaoLoi());
        pwdPassword.textProperty().addListener((obs, o, n) -> anThongBaoLoi());

        // Nhấn phím Enter ở bất kỳ ô nào để thực hiện đăng nhập
        txtUsername.setOnAction(e -> xuLyDangNhap());
        pwdPassword.setOnAction(e -> xuLyDangNhap());

        pnlFormGrid.add(lblUsername, 0, 0);
        pnlFormGrid.add(txtUsername, 1, 0);
        pnlFormGrid.add(lblPassword, 0, 1);
        pnlFormGrid.add(pwdPassword, 1, 1);

        // Hàng nút thao tác: Đăng nhập & Thoát
        HBox pnlActionButtons = new HBox(12);
        pnlActionButtons.setAlignment(Pos.CENTER_RIGHT);

        btnLogin.getStyleClass().add("btn-primary");
        btnLogin.setPrefWidth(110);
        btnLogin.setOnAction(e -> xuLyDangNhap());

        btnExit.getStyleClass().add("btn-secondary");
        btnExit.setPrefWidth(85);
        btnExit.setOnAction(e -> Platform.exit());

        pnlActionButtons.getChildren().addAll(btnExit, btnLogin);

        // Ráp các phần tử vào hộp đăng nhập
        pnlLoginBox.getChildren().addAll(
                pnlHeader,
                lblErrorMessage,
                pnlFormGrid,
                pnlActionButtons
        );

        // 2. Dòng thông tin khoa/trường phía đáy màn hình
        Label lblFooter = new Label("Khoa Công nghệ Thông tin - Trường Đại học Công nghiệp TP.HCM");
        lblFooter.getStyleClass().add("login-footer-text");
        StackPane.setAlignment(lblFooter, Pos.BOTTOM_CENTER);
        StackPane.setMargin(lblFooter, new Insets(0, 0, 14, 0));

        getChildren().addAll(pnlLoginBox, lblFooter);
    }

    /**
     * Ẩn thông báo lỗi khi người dùng thay đổi thông tin nhập
     */
    private void anThongBaoLoi() {
        if (lblErrorMessage.isVisible()) {
            lblErrorMessage.setVisible(false);
            lblErrorMessage.setManaged(false);
            lblErrorMessage.setText("");
        }
    }

    /**
     * Hiển thị thông báo lỗi lên giao diện
     */
    private void hienThiLoi(String thongBao) {
        lblErrorMessage.setText(thongBao);
        lblErrorMessage.setVisible(true);
        lblErrorMessage.setManaged(true);
    }

    /**
     * Xử lý xác thực tài khoản và chuyển hướng vào Dashboard
     */
    private void xuLyDangNhap() {
        String username = txtUsername.getText() != null ? txtUsername.getText().trim() : "";
        String password = pwdPassword.getText() != null ? pwdPassword.getText() : "";

        if (username.isEmpty()) {
            hienThiLoi("Vui lòng nhập tên đăng nhập!");
            txtUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            hienThiLoi("Vui lòng nhập mật khẩu!");
            pwdPassword.requestFocus();
            return;
        }

        User user;
        try {
            user = loginController.authenticate(username, password);
        } catch (IllegalStateException ex) {
            hienThiLoi("Lỗi kết nối cơ sở dữ liệu SQL Server!");
            return;
        }

        if (user == null) {
            hienThiLoi("Tên đăng nhập hoặc mật khẩu không chính xác!");
            pwdPassword.clear();
            pwdPassword.requestFocus();
            return;
        }

        // Lưu thông tin phiên đăng nhập
        SessionManager.login(user);

        // Chuyển sang màn hình Bảng điều khiển chính (MainDashboard)
        stage.setTitle("Happy Tails | Hệ thống quản lý cửa hàng thú cưng");
        Scene scene = new Scene(new MainDashboard(stage), 1280, 760);
        scene.getStylesheets().addAll(
                getClass().getResource("/com/petshop/view/css/base.css").toExternalForm(),
                getClass().getResource("/com/petshop/view/css/dashboard.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMaximized(true);
    }
}
