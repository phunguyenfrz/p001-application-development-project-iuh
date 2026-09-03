package com.petshop.view;

import com.petshop.controller.LoginController;
import com.petshop.model.User;
import com.petshop.util.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginView extends VBox {
    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final LoginController loginController = new LoginController();
    private final Stage stage;
    private final Label errorLabel = new Label();

    public LoginView(Stage stage) {
        this.stage = stage;
        setAlignment(Pos.CENTER);
        setSpacing(16);
        setPadding(new Insets(42));
        getStyleClass().add("login-view");

        Label title = new Label("PETSHOP MANAGEMENT");
        title.getStyleClass().add("app-title");
        Label subtitle = new Label("Đăng nhập hệ thống");
        subtitle.getStyleClass().add("subtitle");
        usernameField.setPromptText("Tên đăng nhập");
        passwordField.setPromptText("Mật khẩu");
        usernameField.setMaxWidth(320);
        passwordField.setMaxWidth(320);

        Button loginButton = new Button("Đăng nhập");
        loginButton.setMaxWidth(320);
        loginButton.getStyleClass().add("primary-button");
        loginButton.setOnAction(event -> login());
        passwordField.setOnAction(event -> login());

        errorLabel.getStyleClass().add("error-label");
        Label hint = new Label("Tài khoản mẫu: owner / owner123 hoặc staff / staff123");
        hint.getStyleClass().add("hint-label");
        getChildren().addAll(title, subtitle, usernameField, passwordField, loginButton, errorLabel, hint);
    }

    private void login() {
        User user = loginController.authenticate(usernameField.getText(), passwordField.getText());
        if (user == null) {
            errorLabel.setText("Tên đăng nhập hoặc mật khẩu không đúng.");
            passwordField.clear();
            passwordField.requestFocus();
            return;
        }
        SessionManager.login(user);
        stage.setTitle("PetShop Management | Trang chính");
        Scene scene = new Scene(new MainDashboard(stage), 1000, 680);
        scene.getStylesheets().add(getClass().getResource("/com/petshop/view/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.centerOnScreen();
    }
}
