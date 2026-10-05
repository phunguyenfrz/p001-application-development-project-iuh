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

    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final TextField passwordTextField = new TextField(); // Khi bấm hiện mật khẩu
    private final LoginController loginController = new LoginController();
    private final Stage stage;
    private final Label errorLabel = new Label();
    private boolean isPasswordVisible = false;

    public LoginView(Stage stage) {
        this.stage = stage;
        getStyleClass().add("login-view");

        // Khối Card trắng ở giữa
        VBox card = new VBox();
        card.getStyleClass().add("login-card");
        card.setSpacing(14);
        card.setAlignment(Pos.CENTER);

        // 1. Logo & Tiêu đề
        Group pawLogo = createPawIcon();
        Label title = new Label("Happy Tails");
        title.getStyleClass().add("brand-title");

        Label subtitle = new Label("Hệ thống quản lý cửa hàng thú cưng");
        subtitle.getStyleClass().add("brand-subtitle");

        VBox brandBox = new VBox(6, pawLogo, title, subtitle);
        brandBox.setAlignment(Pos.CENTER);
        brandBox.setPadding(new Insets(0, 0, 10, 0));

        // 2. Ô nhập tên đăng nhập
        Label userLabel = new Label("Tên đăng nhập");
        userLabel.getStyleClass().add("field-label");
        usernameField.setPromptText("Nhập thông tin...");
        usernameField.getStyleClass().add("login-input");
        usernameField.setMaxWidth(Double.MAX_VALUE);

        VBox userBox = new VBox(6, userLabel, usernameField);
        userBox.setAlignment(Pos.CENTER_LEFT);

        // 3. Ô nhập mật khẩu (có nút ẩn/hiện con mắt)
        Label passLabel = new Label("Mật khẩu");
        passLabel.getStyleClass().add("field-label");

        passwordField.setPromptText("Nhập thông tin...");
        passwordField.getStyleClass().add("login-input");
        passwordField.setMaxWidth(Double.MAX_VALUE);

        passwordTextField.setPromptText("Nhập thông tin...");
        passwordTextField.getStyleClass().add("login-input");
        passwordTextField.setMaxWidth(Double.MAX_VALUE);
        passwordTextField.setManaged(false);
        passwordTextField.setVisible(false);

        // Nút con mắt
        Button eyeButton = createEyeButton();

        StackPane passFieldContainer = new StackPane();
        passFieldContainer.getChildren().addAll(passwordField, passwordTextField, eyeButton);
        StackPane.setAlignment(eyeButton, Pos.CENTER_RIGHT);
        StackPane.setMargin(eyeButton, new Insets(0, 4, 0, 0));

        VBox passBox = new VBox(6, passLabel, passFieldContainer);
        passBox.setAlignment(Pos.CENTER_LEFT);

        // 4. Nút Đăng nhập
        Button loginButton = new Button("Đăng nhập");
        loginButton.getStyleClass().add("btn-login");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setOnAction(event -> login());

        // Hỗ trợ nhấn Enter để submit
        usernameField.setOnAction(event -> login());
        passwordField.setOnAction(event -> login());
        passwordTextField.setOnAction(event -> login());

        // 5. Thông báo lỗi
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setWrapText(true);
        errorLabel.setMinHeight(20);

        // 6. Footer
        Label footer = new Label("HAPPY TAILS • Chi nhánh Quận 1");
        footer.getStyleClass().add("footer-text");
        VBox.setMargin(footer, new Insets(12, 0, 0, 0));

        // Ráp toàn bộ vào Card
        card.getChildren().addAll(
                brandBox,
                userBox,
                passBox,
                loginButton,
                errorLabel,
                footer
        );

        getChildren().add(card);
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
        Button btn = new Button();
        btn.getStyleClass().add("eye-button");

        // SVG icon con mắt viền xám nhạt
        SVGPath eyeIcon = new SVGPath();
        eyeIcon.setContent("M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z");
        eyeIcon.setFill(Color.web("#9ca89e"));
        eyeIcon.setScaleX(0.75);
        eyeIcon.setScaleY(0.75);

        btn.setGraphic(eyeIcon);

        // Đồng bộ dữ liệu 2 ô nhập
        passwordField.textProperty().addListener((obs, oldText, newText) -> {
            if (!isPasswordVisible) passwordTextField.setText(newText);
        });
        passwordTextField.textProperty().addListener((obs, oldText, newText) -> {
            if (isPasswordVisible) passwordField.setText(newText);
        });

        btn.setOnAction(e -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                passwordTextField.setText(passwordField.getText());
                passwordTextField.setVisible(true);
                passwordTextField.setManaged(true);
                passwordField.setVisible(false);
                passwordField.setManaged(false);
                eyeIcon.setFill(Color.web("#436850")); // Màu đậm khi đang mở
                passwordTextField.requestFocus();
                passwordTextField.positionCaret(passwordTextField.getText().length());
            } else {
                passwordField.setText(passwordTextField.getText());
                passwordField.setVisible(true);
                passwordField.setManaged(true);
                passwordTextField.setVisible(false);
                passwordTextField.setManaged(false);
                eyeIcon.setFill(Color.web("#9ca89e"));
                passwordField.requestFocus();
                passwordField.positionCaret(passwordField.getText().length());
            }
        });

        return btn;
    }

    private void login() {
        String username = usernameField.getText();
        String password = isPasswordVisible ? passwordTextField.getText() : passwordField.getText();

        errorLabel.setText("");

        User user;
        try {
            user = loginController.authenticate(username, password);
        } catch (IllegalStateException e) {
            errorLabel.setText("Không thể kết nối cơ sở dữ liệu SQL Server!");
            return;
        }

        if (user == null) {
            errorLabel.setText("Tên đăng nhập hoặc mật khẩu không chính xác.");
            passwordField.clear();
            passwordTextField.clear();
            return;
        }

        // Đăng nhập thành công -> chuyển sang Dashboard
        SessionManager.login(user);
        stage.setTitle("Happy Tails | Bảng điều khiển");
        Scene scene = new Scene(new MainDashboard(stage), 1220, 800);
        scene.getStylesheets().add(getClass().getResource("/com/petshop/view/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setMinWidth(1100);
        stage.setMinHeight(750);
        stage.centerOnScreen();
    }
}
