package com.petshop.view;

import com.petshop.controller.LoginController;
import com.petshop.model.User;
import com.petshop.util.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

/**
 * Giao diện đăng nhập chuẩn thiết kế Happy Tails.
 */
public class LoginView extends StackPane {

    private final TextField txtUsername = new TextField();
    private final PasswordField pwdPassword = new PasswordField();
    private final TextField txtPasswordVisible = new TextField(); // Khi bấm hiện mật khẩu
    private final LoginController loginController = new LoginController();
    private final Stage stage;
    private final Label lblError = new Label();
    private boolean isPasswordVisible = false;

    public LoginView(Stage stage) {
        this.stage = stage;
        getStyleClass().add("login-view");

        // Khối Card trắng ở giữa
        VBox pnlCard = new VBox();
        pnlCard.getStyleClass().add("login-card");
        pnlCard.setSpacing(14);
        pnlCard.setAlignment(Pos.CENTER);

        // 1. Logo & Tiêu đề
        Group pawLogo = createPawIcon();
        Label lblTitle = new Label("Happy Tails");
        lblTitle.getStyleClass().add("brand-title");

        Label lblSubtitle = new Label("Hệ thống quản lý cửa hàng thú cưng");
        lblSubtitle.getStyleClass().add("brand-subtitle");

        VBox pnlBrandBox = new VBox(6, pawLogo, lblTitle, lblSubtitle);
        pnlBrandBox.setAlignment(Pos.CENTER);
        pnlBrandBox.setPadding(new Insets(0, 0, 10, 0));

        // 2. Ô nhập tên đăng nhập
        Label lblUsername = new Label("Tên đăng nhập");
        lblUsername.getStyleClass().add("field-label");
        txtUsername.setPromptText("Nhập thông tin...");
        txtUsername.getStyleClass().add("login-input");
        txtUsername.setMaxWidth(Double.MAX_VALUE);

        VBox pnlUsernameBox = new VBox(6, lblUsername, txtUsername);
        pnlUsernameBox.setAlignment(Pos.CENTER_LEFT);

        // 3. Ô nhập mật khẩu (có nút ẩn/hiện con mắt)
        Label lblPassword = new Label("Mật khẩu");
        lblPassword.getStyleClass().add("field-label");

        pwdPassword.setPromptText("Nhập thông tin...");
        pwdPassword.getStyleClass().add("login-input");
        pwdPassword.setMaxWidth(Double.MAX_VALUE);

        txtPasswordVisible.setPromptText("Nhập thông tin...");
        txtPasswordVisible.getStyleClass().add("login-input");
        txtPasswordVisible.setMaxWidth(Double.MAX_VALUE);
        txtPasswordVisible.setManaged(false);
        txtPasswordVisible.setVisible(false);

        // Nút con mắt
        Button btnTogglePassword = createEyeButton();

        StackPane pnlPasswordFieldContainer = new StackPane();
        pnlPasswordFieldContainer.getChildren().addAll(pwdPassword, txtPasswordVisible, btnTogglePassword);
        StackPane.setAlignment(btnTogglePassword, Pos.CENTER_RIGHT);
        StackPane.setMargin(btnTogglePassword, new Insets(0, 4, 0, 0));

        VBox pnlPasswordBox = new VBox(6, lblPassword, pnlPasswordFieldContainer);
        pnlPasswordBox.setAlignment(Pos.CENTER_LEFT);

        // 4. Nút Đăng nhập
        Button btnLogin = new Button("Đăng nhập");
        btnLogin.getStyleClass().add("btn-login");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setOnAction(event -> login());

        // Hỗ trợ nhấn Enter để submit
        txtUsername.setOnAction(event -> login());
        pwdPassword.setOnAction(event -> login());
        txtPasswordVisible.setOnAction(event -> login());

        // 5. Thông báo lỗi
        lblError.getStyleClass().add("error-label");
        lblError.setWrapText(true);
        lblError.setMinHeight(20);

        // 6. Footer
        Label lblFooter = new Label("HAPPY TAILS • Chi nhánh Quận 1");
        lblFooter.getStyleClass().add("footer-text");
        VBox.setMargin(lblFooter, new Insets(12, 0, 0, 0));

        // Ráp toàn bộ vào Card
        pnlCard.getChildren().addAll(
                pnlBrandBox,
                pnlUsernameBox,
                pnlPasswordBox,
                btnLogin,
                lblError,
                lblFooter
        );

        getChildren().add(pnlCard);
        setPadding(new Insets(24));
    }

    /**
     * Tạo icon dấu chân thú cưng viền màu cam/nâu cam như thiết kế.
     */
    private Group createPawIcon() {
        Group group = new Group();

        Color strokeColor = Color.web("#d97736");
        double strokeWidth = 2.4;

        // Đệm chính (Main pad)
        Ellipse mainPad = new Ellipse(0, 5, 12, 9);
        mainPad.setFill(Color.TRANSPARENT);
        mainPad.setStroke(strokeColor);
        mainPad.setStrokeWidth(strokeWidth);

        // 4 ngón chân (Toes)
        Circle toe1 = new Circle(-11, -8, 3.8);
        toe1.setFill(Color.TRANSPARENT);
        toe1.setStroke(strokeColor);
        toe1.setStrokeWidth(strokeWidth);

        Circle toe2 = new Circle(-4, -13, 4.2);
        toe2.setFill(Color.TRANSPARENT);
        toe2.setStroke(strokeColor);
        toe2.setStrokeWidth(strokeWidth);

        Circle toe3 = new Circle(4, -13, 4.2);
        toe3.setFill(Color.TRANSPARENT);
        toe3.setStroke(strokeColor);
        toe3.setStrokeWidth(strokeWidth);

        Circle toe4 = new Circle(11, -8, 3.8);
        toe4.setFill(Color.TRANSPARENT);
        toe4.setStroke(strokeColor);
        toe4.setStrokeWidth(strokeWidth);

        group.getChildren().addAll(mainPad, toe1, toe2, toe3, toe4);
        return group;
    }

    /**
     * Tạo nút con mắt để bật/tắt hiển thị mật khẩu.
     */
    private Button createEyeButton() {
        Button btnTogglePassword = new Button();
        btnTogglePassword.getStyleClass().add("eye-button");

        // SVG icon con mắt viền xám nhạt
        SVGPath eyeIcon = new SVGPath();
        eyeIcon.setContent("M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z");
        eyeIcon.setFill(Color.web("#9ca89e"));
        eyeIcon.setScaleX(0.75);
        eyeIcon.setScaleY(0.75);

        btnTogglePassword.setGraphic(eyeIcon);

        // Đồng bộ dữ liệu 2 ô nhập
        pwdPassword.textProperty().addListener((obs, oldText, newText) -> {
            if (!isPasswordVisible) txtPasswordVisible.setText(newText);
        });
        txtPasswordVisible.textProperty().addListener((obs, oldText, newText) -> {
            if (isPasswordVisible) pwdPassword.setText(newText);
        });

        btnTogglePassword.setOnAction(e -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                txtPasswordVisible.setText(pwdPassword.getText());
                txtPasswordVisible.setVisible(true);
                txtPasswordVisible.setManaged(true);
                pwdPassword.setVisible(false);
                pwdPassword.setManaged(false);
                eyeIcon.setFill(Color.web("#436850")); // Màu đậm khi đang mở
                txtPasswordVisible.requestFocus();
                txtPasswordVisible.positionCaret(txtPasswordVisible.getText().length());
            } else {
                pwdPassword.setText(txtPasswordVisible.getText());
                pwdPassword.setVisible(true);
                pwdPassword.setManaged(true);
                txtPasswordVisible.setVisible(false);
                txtPasswordVisible.setManaged(false);
                eyeIcon.setFill(Color.web("#9ca89e"));
                pwdPassword.requestFocus();
                pwdPassword.positionCaret(pwdPassword.getText().length());
            }
        });

        return btnTogglePassword;
    }

    private void login() {
        String username = txtUsername.getText();
        String password = isPasswordVisible ? txtPasswordVisible.getText() : pwdPassword.getText();

        lblError.setText("");

        User user;
        try {
            user = loginController.authenticate(username, password);
        } catch (IllegalStateException e) {
            lblError.setText("Không thể kết nối cơ sở dữ liệu SQL Server!");
            return;
        }

        if (user == null) {
            lblError.setText("Tên đăng nhập hoặc mật khẩu không chính xác.");
            pwdPassword.clear();
            txtPasswordVisible.clear();
            return;
        }

        // Đăng nhập thành công -> chuyển sang Dashboard
        SessionManager.login(user);
        stage.setTitle("Happy Tails | Bảng điều khiển");

        // Tính toán kích thước cửa sổ vừa vặn màn hình khả dụng (trừ taskbar)
        javafx.geometry.Rectangle2D visualBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        double targetWidth = Math.min(1280, visualBounds.getWidth() * 0.94);
        double targetHeight = Math.min(760, visualBounds.getHeight() * 0.94);

        Scene scene = new Scene(new MainDashboard(stage), targetWidth, targetHeight);
        scene.getStylesheets().add(getClass().getResource("/com/petshop/view/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setMinWidth(1024);
        stage.setMinHeight(640);
        stage.centerOnScreen();
    }
}
