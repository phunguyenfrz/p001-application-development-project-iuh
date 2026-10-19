package com.petshop.view;

import com.petshop.controller.LoginController;
import com.petshop.model.User;
import com.petshop.util.SessionManager;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Giao diện đăng nhập chuẩn thiết kế Happy Tails.
 */
public class LoginView extends StackPane {

    private final TextField txtUsername = new TextField();
    private final PasswordField pwdPassword = new PasswordField();
    private final TextField txtPasswordVisible = new TextField(); // Khi bấm hiện mật khẩu
    private final LoginController loginController = new LoginController();
    private final Stage stage;
    private final Button btnLogin = new Button("Đăng nhập");
    private final Label lblError = new Label("Tên đăng nhập hoặc mật khẩu không đúng");
    private final Label lblUsernameError = new Label("Kiểm tra lại tên đăng nhập");
    private final Label lblPasswordError = new Label("Mật khẩu không khớp");
    private final HBox pnlErrorBanner = new HBox();
    private boolean isPasswordVisible = false;
    private final Circle circleLeft = new Circle(220);
    private final Circle circleRight = new Circle(280);
    private final Group pawWatermark = createPawIcon();

    public LoginView(Stage stage) {
        this.stage = stage;
        getStyleClass().add("login-view");

        // Giữ chế độ toàn màn hình và icon cửa sổ của giao diện Happy Tails.
        if (stage != null) {
            stage.setMaximized(true);
            Platform.runLater(() -> stage.setMaximized(true));
            try {
                var iconStream = getClass().getResourceAsStream("/com/petshop/view/logo.png");
                if (iconStream != null) {
                    stage.getIcons().setAll(new Image(iconStream));
                }
            } catch (Exception ignored) {}
        }

        circleLeft.setFill(Color.web("#c8ead5", 0.70));
        circleLeft.setManaged(false);
        circleRight.setFill(Color.web("#faedd7", 0.85));
        circleRight.setManaged(false);
        pawWatermark.setManaged(false);

        // Khối Card trắng ở giữa
        VBox pnlCard = new VBox();
        pnlCard.getStyleClass().add("login-card");
        pnlCard.setPrefWidth(440);
        pnlCard.setMinWidth(440);
        pnlCard.setMaxWidth(440);
        pnlCard.setPrefHeight(426);
        pnlCard.setMinHeight(426);
        pnlCard.setMaxHeight(426);
        pnlCard.setSpacing(14);
        pnlCard.setAlignment(Pos.CENTER);

        // 1. Logo & Tiêu đề
        Node pnlLogoBadge = createPawBadge();
        Label lblTitle = new Label("Happy Tails");
        lblTitle.getStyleClass().add("brand-title");

        Label lblSubtitle = new Label("Hệ thống quản lý cửa hàng thú cưng");
        lblSubtitle.getStyleClass().add("brand-subtitle");

        HBox pnlBrandRow = new HBox(12, pnlLogoBadge, lblTitle);
        pnlBrandRow.setAlignment(Pos.CENTER);
        VBox pnlBrandBox = new VBox(6, pnlBrandRow, lblSubtitle);
        pnlBrandBox.setAlignment(Pos.CENTER);
        pnlBrandBox.setPadding(new Insets(0, 0, 4, 0));

        // 2. Ô nhập tên đăng nhập
        Label lblUsername = new Label("Tên đăng nhập");
        lblUsername.getStyleClass().add("field-label");
        txtUsername.setPromptText("Nhập thông tin...");
        txtUsername.getStyleClass().add("login-input");
        txtUsername.setMaxWidth(Double.MAX_VALUE);
        lblUsernameError.getStyleClass().add("field-error-text");
        lblUsernameError.setVisible(false);
        lblUsernameError.setManaged(false);

        VBox pnlUsernameBox = new VBox(5, lblUsername, txtUsername, lblUsernameError);
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
        lblPasswordError.getStyleClass().add("field-error-text");
        lblPasswordError.setVisible(false);
        lblPasswordError.setManaged(false);
        pnlPasswordBox.getChildren().add(lblPasswordError);

        // 4. Nút Đăng nhập
        btnLogin.getStyleClass().add("btn-login");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setOnAction(event -> login());

        // Hỗ trợ nhấn Enter để submit
        txtUsername.setOnAction(event -> login());
        pwdPassword.setOnAction(event -> login());
        txtPasswordVisible.setOnAction(event -> login());

        // 5. Banner lỗi đăng nhập.
        lblError.getStyleClass().add("error-banner-text");
        lblError.setWrapText(true);
        pnlErrorBanner.getStyleClass().add("error-banner");
        pnlErrorBanner.setSpacing(8);
        pnlErrorBanner.setAlignment(Pos.CENTER_LEFT);
        pnlErrorBanner.getChildren().addAll(createAlertIcon(), lblError);
        pnlErrorBanner.setVisible(false);
        pnlErrorBanner.setManaged(false);

        // 6. Chú thích bảo mật
        Label lblSecurity = new Label("Đăng nhập an toàn dành cho nhân viên");
        lblSecurity.getStyleClass().add("security-text");
        HBox pnlSecurityBox = new HBox(6, createShieldIcon(), lblSecurity);
        pnlSecurityBox.setAlignment(Pos.CENTER);

        // Footer trang được đặt dưới card.
        Label lblFooter = new Label("© 2026 Happy Tails • Phiên bản 1.0");
        lblFooter.getStyleClass().add("footer-text");
        StackPane.setAlignment(lblFooter, Pos.BOTTOM_CENTER);
        StackPane.setMargin(lblFooter, new Insets(0, 0, 20, 0));

        // Ráp toàn bộ vào Card
        pnlCard.getChildren().addAll(
                pnlBrandBox,
                pnlErrorBanner,
                pnlUsernameBox,
                pnlPasswordBox,
                btnLogin,
                pnlSecurityBox
        );

        getChildren().addAll(circleLeft, circleRight, pawWatermark, pnlCard, lblFooter);
            txtUsername.textProperty().addListener((obs, oldText, newText) -> showErrorState(false, null));
            pwdPassword.textProperty().addListener((obs, oldText, newText) -> showErrorState(false, null));
            txtPasswordVisible.textProperty().addListener((obs, oldText, newText) -> showErrorState(false, null));
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        double width = getWidth();
        double height = getHeight();
        circleLeft.setCenterX(-30);
        circleLeft.setCenterY(height * 0.32);
        circleRight.setCenterX(width + 40);
        circleRight.setCenterY(height + 50);
        pawWatermark.setLayoutX(width - 110);
        pawWatermark.setLayoutY(90);
    }

    private Node createPawBadge() {
        try {
            var logoStream = getClass().getResourceAsStream("/com/petshop/view/logo.png");
            if (logoStream != null) {
                ImageView imgLogo = new ImageView(new Image(logoStream));
                imgLogo.setFitWidth(40);
                imgLogo.setFitHeight(40);
                imgLogo.setPreserveRatio(true);
                imgLogo.setSmooth(true);
                return imgLogo;
            }
        } catch (Exception ignored) {}
        return createPawIcon();
    }

    private Node createShieldIcon() {
        SVGPath iconShield = new SVGPath();
        iconShield.setContent("M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z");
        iconShield.setFill(Color.web("#6b7280"));
        iconShield.setScaleX(0.62);
        iconShield.setScaleY(0.62);
        return iconShield;
    }

    private Node createAlertIcon() {
        SVGPath iconAlert = new SVGPath();
        iconAlert.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z");
        iconAlert.setFill(Color.web("#ef4444"));
        iconAlert.setScaleX(0.7);
        iconAlert.setScaleY(0.7);
        return iconAlert;
    }

    private Node createSpinnerGraphic() {
        Arc arcSpinner = new Arc(0, 0, 7, 7, 45, 270);
        arcSpinner.setType(ArcType.OPEN);
        arcSpinner.setFill(Color.TRANSPARENT);
        arcSpinner.setStroke(Color.WHITE);
        arcSpinner.setStrokeWidth(2.2);
        RotateTransition animationSpinner = new RotateTransition(Duration.millis(800), arcSpinner);
        animationSpinner.setByAngle(360);
        animationSpinner.setCycleCount(Animation.INDEFINITE);
        animationSpinner.setInterpolator(Interpolator.LINEAR);
        animationSpinner.play();
        return arcSpinner;
    }

    private void setLoadingState(boolean isLoading) {
        btnLogin.setText(isLoading ? "Đang đăng nhập..." : "Đăng nhập");
        btnLogin.setGraphic(isLoading ? createSpinnerGraphic() : null);
        btnLogin.setDisable(isLoading);
        txtUsername.setDisable(isLoading);
        pwdPassword.setDisable(isLoading);
        txtPasswordVisible.setDisable(isLoading);
    }

    private void showErrorState(boolean hasError, String message) {
        if (hasError) {
            lblError.setText(message != null && !message.isBlank()
                    ? message : "Tên đăng nhập hoặc mật khẩu không đúng");
            pnlErrorBanner.setVisible(true);
            pnlErrorBanner.setManaged(true);
            txtUsername.getStyleClass().add("login-input-error");
            pwdPassword.getStyleClass().add("login-input-error");
            txtPasswordVisible.getStyleClass().add("login-input-error");
            lblUsernameError.setVisible(true);
            lblUsernameError.setManaged(true);
            lblPasswordError.setVisible(true);
            lblPasswordError.setManaged(true);
            return;
        }

        pnlErrorBanner.setVisible(false);
        pnlErrorBanner.setManaged(false);
        txtUsername.getStyleClass().remove("login-input-error");
        pwdPassword.getStyleClass().remove("login-input-error");
        txtPasswordVisible.getStyleClass().remove("login-input-error");
        lblUsernameError.setVisible(false);
        lblUsernameError.setManaged(false);
        lblPasswordError.setVisible(false);
        lblPasswordError.setManaged(false);
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
        showErrorState(false, null);
        setLoadingState(true);
        PauseTransition delayLogin = new PauseTransition(Duration.millis(350));
        delayLogin.setOnFinished(event -> performAuthentication());
        delayLogin.play();
    }

    private void performAuthentication() {
        String username = txtUsername.getText();
        String password = isPasswordVisible ? txtPasswordVisible.getText() : pwdPassword.getText();

        User user;
        try {
            user = loginController.authenticate(username, password);
        } catch (IllegalStateException e) {
            setLoadingState(false);
            showErrorState(true, "Không thể kết nối cơ sở dữ liệu SQL Server!");
            return;
        }

        if (user == null) {
            setLoadingState(false);
            showErrorState(true, "Tên đăng nhập hoặc mật khẩu không chính xác.");
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
