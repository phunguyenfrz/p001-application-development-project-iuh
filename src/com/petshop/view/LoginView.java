package com.petshop.view;

import com.petshop.controller.AuthenticationController;
import com.petshop.model.User;
import com.petshop.util.DbConfig;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

/**
 * Giao diện đăng nhập chuẩn thiết kế Happy Tails.
 */
public class LoginView extends StackPane {

    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final TextField passwordTextField = new TextField(); // Khi bấm hiện mật khẩu
    private final AuthenticationController loginController;
    private final LoginListener loginListener;
    private final Label errorLabel = new Label();
    private boolean isPasswordVisible = false;

    public LoginView(AuthenticationController loginController, LoginListener loginListener) {
        this.loginController = loginController;
        this.loginListener = loginListener;
        getStyleClass().add("login-view");

        // Khối Card trắng ở giữa
        VBox card = new VBox();
        card.getStyleClass().add("login-card");
        card.setSpacing(14);
        card.setAlignment(Pos.CENTER);

        // 1. Logo & Tiêu đề
        PawIcon pawLogo = new PawIcon(34,
                PawIcon.ColorVariant.BRAND, PawIcon.Style.OUTLINE);
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
        Label footer = new Label("HAPPY TAILS • " + DbConfig.getBranchName());
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
     * Tạo nút con mắt để bật/tắt hiển thị mật khẩu.
     */
    private Button createEyeButton() {
        Button btn = new Button();
        btn.getStyleClass().add("eye-button");

        // SVG icon con mắt viền xám nhạt
        SVGPath eyeIcon = new SVGPath();
        eyeIcon.setContent("M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z");
        eyeIcon.getStyleClass().add("login-eye-icon");
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
                eyeIcon.getStyleClass().add("login-eye-icon-open");
                passwordTextField.requestFocus();
                passwordTextField.positionCaret(passwordTextField.getText().length());
            } else {
                passwordField.setText(passwordTextField.getText());
                passwordField.setVisible(true);
                passwordField.setManaged(true);
                passwordTextField.setVisible(false);
                passwordTextField.setManaged(false);
                eyeIcon.getStyleClass().remove("login-eye-icon-open");
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
            errorLabel.setText(e.getMessage());
            return;
        }

        if (user == null) {
            errorLabel.setText("Tên đăng nhập hoặc mật khẩu không chính xác.");
            passwordField.clear();
            passwordTextField.clear();
            return;
        }

        loginListener.onLoginSuccess(user);
    }
}
