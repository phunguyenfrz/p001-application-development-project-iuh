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
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginView extends StackPane {

    // Khai báo các điều khiển giao diện theo quy tắc đặt tên tiền tố: txt, pwd, btn, lbl
    private final TextField txtUsername = new TextField();
    private final PasswordField pwdPassword = new PasswordField();
    private final TextField txtPasswordVisible = new TextField(); // Hiển thị khi bật icon con mắt
    private final Button btnLogin = new Button("Đăng nhập");
    private final Button btnTogglePassword = new Button();

    private final Label lblBrandTitle = new Label("Happy Tails");
    private final Label lblBrandSubtitle = new Label("Hệ thống quản lý cửa hàng thú cưng");
    private final Label lblErrorBanner = new Label("Tên đăng nhập hoặc mật khẩu không đúng");
    private final Label lblUsername = new Label("Tên đăng nhập");
    private final Label lblUsernameError = new Label("Kiểm tra lại tên đăng nhập");
    private final Label lblPassword = new Label("Mật khẩu");
    private final Label lblPasswordError = new Label("Mật khẩu không khớp");
    private final Label lblSecurity = new Label("Đăng nhập an toàn dành cho nhân viên");
    private final Label lblFooter = new Label("© 2026 Happy Tails • Phiên bản 1.0");

    // Container banner thông báo lỗi phía trên form
    private final HBox pnlErrorBanner = new HBox();

    // Các thành phần trang trí nền
    private final Circle circleLeft = new Circle(220);
    private final Circle circleRight = new Circle(280);
    private final Group pawWatermark = createPawWatermark();

    private final LoginController loginController = new LoginController();
    private final Stage stage;
    private boolean isPasswordVisible = false;

    public LoginView(Stage stage) {
        this.stage = stage;
        getStyleClass().add("login-view");

        // Tự động mở cửa sổ ở chế độ toàn màn hình (full màn hình) theo yêu cầu & nạp icon cửa sổ
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

        // -------------------------------------------------------------
        // 1. Cấu hình các hình tròn và watermark nền (vẽ nền chuẩn Figma)
        // -------------------------------------------------------------
        circleLeft.setFill(Color.web("#c8ead5", 0.70));
        circleLeft.setManaged(false);

        circleRight.setFill(Color.web("#faedd7", 0.85));
        circleRight.setManaged(false);

        pawWatermark.setManaged(false);

        // -------------------------------------------------------------
        // 2. Thẻ trắng trung tâm (Login Card - Chuẩn Figma: 440px x 426px hug)
        // -------------------------------------------------------------
        VBox pnlCard = new VBox();
        pnlCard.getStyleClass().add("login-card");
        pnlCard.setPrefWidth(440);
        pnlCard.setMaxWidth(440);
        pnlCard.setMinWidth(440);
        pnlCard.setSpacing(16);
        pnlCard.setAlignment(Pos.CENTER);
        StackPane.setAlignment(pnlCard, Pos.CENTER);

        // 2.1. Brand Header (Logo ảnh chuẩn + Tên thương hiệu & Phụ đề)
        Node pnlLogoBadge = createPawBadge();
        lblBrandTitle.getStyleClass().add("brand-title");

        HBox pnlBrandRow = new HBox(12, pnlLogoBadge, lblBrandTitle);
        pnlBrandRow.setAlignment(Pos.CENTER);

        lblBrandSubtitle.getStyleClass().add("brand-subtitle");

        VBox pnlBrandBox = new VBox(6, pnlBrandRow, lblBrandSubtitle);
        pnlBrandBox.setAlignment(Pos.CENTER);
        pnlBrandBox.setPadding(new Insets(0, 0, 4, 0));

        // 2.2. Error Alert Banner (Xuất hiện khi đăng nhập thất bại - Ảnh 3)
        lblErrorBanner.getStyleClass().add("error-banner-text");
        pnlErrorBanner.getStyleClass().add("error-banner");
        pnlErrorBanner.setSpacing(8);
        pnlErrorBanner.setAlignment(Pos.CENTER_LEFT);
        pnlErrorBanner.getChildren().addAll(createAlertIcon(), lblErrorBanner);
        pnlErrorBanner.setVisible(false);
        pnlErrorBanner.setManaged(false);

        // 2.3. Ô nhập tên đăng nhập (Không nhập sẵn, để trống kèm placeholder prompt text)
        lblUsername.getStyleClass().add("field-label");
        txtUsername.getStyleClass().add("login-input");
        txtUsername.setPromptText("Nhập tài khoản...");
        txtUsername.clear();
        txtUsername.setMaxWidth(Double.MAX_VALUE);

        lblUsernameError.getStyleClass().add("field-error-text");
        lblUsernameError.setVisible(false);
        lblUsernameError.setManaged(false);

        VBox pnlUsernameBox = new VBox(5, lblUsername, txtUsername, lblUsernameError);
        pnlUsernameBox.setAlignment(Pos.CENTER_LEFT);

        // 2.4. Ô nhập mật khẩu (có nút bật/tắt hiển thị mật khẩu)
        lblPassword.getStyleClass().add("field-label");

        pwdPassword.getStyleClass().add("login-input");
        pwdPassword.setPromptText("Nhập mật khẩu...");
        pwdPassword.clear();
        pwdPassword.setMaxWidth(Double.MAX_VALUE);

        txtPasswordVisible.getStyleClass().add("login-input");
        txtPasswordVisible.setPromptText("Nhập mật khẩu...");
        txtPasswordVisible.setMaxWidth(Double.MAX_VALUE);
        txtPasswordVisible.setManaged(false);
        txtPasswordVisible.setVisible(false);

        btnTogglePassword.getStyleClass().add("eye-button");
        btnTogglePassword.setGraphic(createEyeIcon(false));

        StackPane pnlPasswordFieldContainer = new StackPane();
        pnlPasswordFieldContainer.getChildren().addAll(pwdPassword, txtPasswordVisible, btnTogglePassword);
        StackPane.setAlignment(btnTogglePassword, Pos.CENTER_RIGHT);
        StackPane.setMargin(btnTogglePassword, new Insets(0, 4, 0, 0));

        lblPasswordError.getStyleClass().add("field-error-text");
        lblPasswordError.setVisible(false);
        lblPasswordError.setManaged(false);

        VBox pnlPasswordBox = new VBox(5, lblPassword, pnlPasswordFieldContainer, lblPasswordError);
        pnlPasswordBox.setAlignment(Pos.CENTER_LEFT);

        // 2.5. Nút Đăng nhập
        btnLogin.getStyleClass().add("btn-login");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setOnAction(event -> login());
        VBox.setMargin(btnLogin, new Insets(4, 0, 0, 0));

        // Hỗ trợ nhấn phím Enter để gửi form đăng nhập
        txtUsername.setOnAction(event -> login());
        pwdPassword.setOnAction(event -> login());
        txtPasswordVisible.setOnAction(event -> login());

        // 2.6. Chú thích bảo mật phía dưới nút đăng nhập
        lblSecurity.getStyleClass().add("security-text");
        HBox pnlSecurityBox = new HBox(6, createShieldIcon(), lblSecurity);
        pnlSecurityBox.setAlignment(Pos.CENTER);
        VBox.setMargin(pnlSecurityBox, new Insets(4, 0, 0, 0));

        // Ráp toàn bộ các khối vào Card trung tâm (440px x 426px hug)
        pnlCard.getChildren().addAll(
                pnlBrandBox,
                pnlErrorBanner,
                pnlUsernameBox,
                pnlPasswordBox,
                btnLogin,
                pnlSecurityBox
        );

        // -------------------------------------------------------------
        // 3. Footer phía đáy màn hình
        // -------------------------------------------------------------
        lblFooter.getStyleClass().add("footer-text");
        StackPane.setAlignment(lblFooter, Pos.BOTTOM_CENTER);
        StackPane.setMargin(lblFooter, new Insets(0, 0, 20, 0));

        // Thêm tất cả vào giao diện StackPane chính
        getChildren().addAll(circleLeft, circleRight, pawWatermark, pnlCard, lblFooter);

        // Cấu hình sự kiện toggle mật khẩu & tự động xóa lỗi khi người dùng nhập liệu
        setupPasswordToggle();
        setupInputListeners();
    }

    /**
     * Bố cục động các thành phần trang trí nền khi cửa sổ co giãn hoặc full màn hình.
     */
    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        double w = getWidth();
        double h = getHeight();

        // Định vị vòng tròn xanh xô thơm bên trái màn hình
        circleLeft.setCenterX(-30);
        circleLeft.setCenterY(h * 0.32);

        // Định vị vòng tròn be đào góc dưới bên phải màn hình
        circleRight.setCenterX(w + 40);
        circleRight.setCenterY(h + 50);

        // Định vị watermark dấu chân góc trên bên phải màn hình
        pawWatermark.setLayoutX(w - 110);
        pawWatermark.setLayoutY(90);
    }

    /**
     * Tạo huy hiệu logo cam chuẩn Happy Tails từ file logo.png.
     */
    private Node createPawBadge() {
        try {
            var stream = getClass().getResourceAsStream("/com/petshop/view/logo.png");
            if (stream != null) {
                ImageView iv = new ImageView(new Image(stream));
                iv.setFitWidth(40);
                iv.setFitHeight(40);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
                return iv;
            }
        } catch (Exception ignored) {}

        StackPane badge = new StackPane();
        badge.getStyleClass().add("brand-badge");
        badge.setMinSize(40, 40);
        badge.setPrefSize(40, 40);
        badge.setMaxSize(40, 40);

        Group pawGroup = new Group();
        Circle toe1 = new Circle(19.5, 9.0, 2.9);
        toe1.setFill(Color.TRANSPARENT);
        toe1.setStroke(Color.WHITE);
        toe1.setStrokeWidth(2.0);

        Circle toe2 = new Circle(26.5, 14.5, 2.9);
        toe2.setFill(Color.TRANSPARENT);
        toe2.setStroke(Color.WHITE);
        toe2.setStrokeWidth(2.0);

        Circle toe3 = new Circle(26.0, 22.5, 2.9);
        toe3.setFill(Color.TRANSPARENT);
        toe3.setStroke(Color.WHITE);
        toe3.setStrokeWidth(2.0);

        SVGPath beanPad = new SVGPath();
        beanPad.setContent("M 11 16 C 16 13, 22 16.5, 21 22.5 C 20 27.5, 15.5 29.5, 12 27.5 C 8 25.5, 6.5 20.5, 8.5 17.5 C 9.5 16.5, 10 16, 11 16 Z");
        beanPad.setFill(Color.TRANSPARENT);
        beanPad.setStroke(Color.WHITE);
        beanPad.setStrokeWidth(2.0);

        pawGroup.getChildren().addAll(beanPad, toe1, toe2, toe3);
        badge.getChildren().add(pawGroup);
        return badge;
    }

    /**
     * Tạo hình mờ watermark dấu chân viền mảnh ở góc trên bên phải màn hình
     * đúng hình dạng nhận diện thương hiệu.
     */
    private Group createPawWatermark() {
        Group group = new Group();
        Color strokeColor = Color.web("#bde4cb");
        double strokeWidth = 2.0;

        Circle toe1 = new Circle(52, 24, 7.8);
        toe1.setFill(Color.TRANSPARENT);
        toe1.setStroke(strokeColor);
        toe1.setStrokeWidth(strokeWidth);

        Circle toe2 = new Circle(71, 39, 7.8);
        toe2.setFill(Color.TRANSPARENT);
        toe2.setStroke(strokeColor);
        toe2.setStrokeWidth(strokeWidth);

        Circle toe3 = new Circle(70, 61, 7.8);
        toe3.setFill(Color.TRANSPARENT);
        toe3.setStroke(strokeColor);
        toe3.setStrokeWidth(strokeWidth);

        SVGPath beanPad = new SVGPath();
        beanPad.setContent("M 30 43 C 43 35, 59 44, 57 60 C 54 74, 42 79, 32 74 C 22 69, 17 55, 23 47 C 26 44, 27 43, 30 43 Z");
        beanPad.setFill(Color.TRANSPARENT);
        beanPad.setStroke(strokeColor);
        beanPad.setStrokeWidth(strokeWidth);

        group.getChildren().addAll(beanPad, toe1, toe2, toe3);
        group.setRotate(12);
        return group;
    }

    /**
     * Tạo biểu tượng SVG khiên bảo mật có dấu tích kiểm.
     */
    private SVGPath createShieldIcon() {
        SVGPath shield = new SVGPath();
        shield.setContent("M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z");
        shield.setFill(Color.web("#6b7280"));
        shield.setScaleX(0.62);
        shield.setScaleY(0.62);
        return shield;
    }

    /**
     * Tạo biểu tượng SVG cảnh báo hình tròn có chấm than (!).
     */
    private SVGPath createAlertIcon() {
        SVGPath alert = new SVGPath();
        alert.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z");
        alert.setFill(Color.web("#ef4444"));
        alert.setScaleX(0.70);
        alert.setScaleY(0.70);
        return alert;
    }

    /**
     * Tạo biểu tượng SVG con mắt để bật/tắt hiển thị mật khẩu.
     */
    private SVGPath createEyeIcon(boolean active) {
        SVGPath eye = new SVGPath();
        eye.setContent("M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z");
        eye.setFill(active ? Color.web("#2d6a4f") : Color.web("#6b7280"));
        eye.setScaleX(0.70);
        eye.setScaleY(0.70);
        return eye;
    }

    /**
     * Tạo icon spinner xoay tròn cho trạng thái "Đang đăng nhập..." (khớp Ảnh 1).
     */
    private Node createSpinnerGraphic() {
        Arc arc = new Arc(0, 0, 7, 7, 45, 270);
        arc.setType(ArcType.OPEN);
        arc.setFill(Color.TRANSPARENT);
        arc.setStroke(Color.WHITE);
        arc.setStrokeWidth(2.2);

        RotateTransition rotate = new RotateTransition(Duration.millis(800), arc);
        rotate.setByAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.play();

        return arc;
    }

    /**
     * Thiết lập đồng bộ và nút bấm ẩn/hiện mật khẩu.
     */
    private void setupPasswordToggle() {
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
                btnTogglePassword.setGraphic(createEyeIcon(true));
                txtPasswordVisible.requestFocus();
                txtPasswordVisible.positionCaret(txtPasswordVisible.getText().length());
            } else {
                pwdPassword.setText(txtPasswordVisible.getText());
                pwdPassword.setVisible(true);
                pwdPassword.setManaged(true);
                txtPasswordVisible.setVisible(false);
                txtPasswordVisible.setManaged(false);
                btnTogglePassword.setGraphic(createEyeIcon(false));
                pwdPassword.requestFocus();
                pwdPassword.positionCaret(pwdPassword.getText().length());
            }
        });
    }

    /**
     * Thiết lập xóa trạng thái lỗi khi người dùng gõ phím.
     */
    private void setupInputListeners() {
        txtUsername.textProperty().addListener((obs, o, n) -> showErrorState(false, null));
        pwdPassword.textProperty().addListener((obs, o, n) -> showErrorState(false, null));
        txtPasswordVisible.textProperty().addListener((obs, o, n) -> showErrorState(false, null));
    }

    private void setLoadingState(boolean loading) {
        if (loading) {
            btnLogin.setText("Đang đăng nhập...");
            btnLogin.setGraphic(createSpinnerGraphic());
            btnLogin.setDisable(true);
            txtUsername.setDisable(true);
            pwdPassword.setDisable(true);
            txtPasswordVisible.setDisable(true);
        } else {
            btnLogin.setText("Đăng nhập");
            btnLogin.setGraphic(null);
            btnLogin.setDisable(false);
            txtUsername.setDisable(false);
            pwdPassword.setDisable(false);
            txtPasswordVisible.setDisable(false);
        }
    }

    /**
     * - Alert banner đỏ ở trên ô nhập.
     * - Viền đỏ ô nhập tên đăng nhập và mật khẩu.
     * - Thông báo phụ màu đỏ dưới từng ô nhập.
     */
    private void showErrorState(boolean hasError, String message) {
        if (hasError) {
            lblErrorBanner.setText(message != null && !message.isBlank() ? message : "Tên đăng nhập hoặc mật khẩu không đúng");
            pnlErrorBanner.setVisible(true);
            pnlErrorBanner.setManaged(true);

            if (!txtUsername.getStyleClass().contains("login-input-error")) {
                txtUsername.getStyleClass().add("login-input-error");
            }
            lblUsernameError.setVisible(true);
            lblUsernameError.setManaged(true);

            if (!pwdPassword.getStyleClass().contains("login-input-error")) {
                pwdPassword.getStyleClass().add("login-input-error");
            }
            if (!txtPasswordVisible.getStyleClass().contains("login-input-error")) {
                txtPasswordVisible.getStyleClass().add("login-input-error");
            }
            lblPasswordError.setVisible(true);
            lblPasswordError.setManaged(true);
        } else {
            pnlErrorBanner.setVisible(false);
            pnlErrorBanner.setManaged(false);

            txtUsername.getStyleClass().remove("login-input-error");
            lblUsernameError.setVisible(false);
            lblUsernameError.setManaged(false);

            pwdPassword.getStyleClass().remove("login-input-error");
            txtPasswordVisible.getStyleClass().remove("login-input-error");
            lblPasswordError.setVisible(false);
            lblPasswordError.setManaged(false);
        }
    }

    /**
     * Xử lý luồng đăng nhập:
     * 1. Hiển thị trạng thái đang đăng nhập (Ảnh 1).
     * 2. Gọi LoginController xác thực tài khoản.
     * 3. Chuyển sang lỗi (Ảnh 3) hoặc chuyển tiếp vào MainDashboard.
     */
    private void login() {
        showErrorState(false, null);
        setLoadingState(true);

        // Hiển thị trạng thái xoay loading mượt mà theo mockup Ảnh 1
        PauseTransition delay = new PauseTransition(Duration.millis(350));
        delay.setOnFinished(event -> performAuthentication());
        delay.play();
    }

    /**
     * Thực hiện kiểm tra tài khoản qua LoginController (giữ nguyên logic gốc).
     */
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
            showErrorState(true, "Tên đăng nhập hoặc mật khẩu không đúng");
            return;
        }

        // Đăng nhập thành công -> chuyển sang Dashboard
        SessionManager.login(user);
        stage.setTitle("Happy Tails | Bảng điều khiển");
        try {
            var iconStream = getClass().getResourceAsStream("/com/petshop/view/logo.png");
            if (iconStream != null) {
                stage.getIcons().setAll(new Image(iconStream));
            }
        } catch (Exception ignored) {}

        // Tính toán kích thước cửa sổ vừa vặn màn hình khả dụng (trừ taskbar)
        javafx.geometry.Rectangle2D visualBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        double targetWidth = Math.min(1280, visualBounds.getWidth() * 0.94);
        double targetHeight = Math.min(760, visualBounds.getHeight() * 0.94);

        Scene scene = new Scene(new MainDashboard(stage), targetWidth, targetHeight);
        scene.getStylesheets().add(getClass().getResource("/com/petshop/view/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setMaximized(true);
    }
}
