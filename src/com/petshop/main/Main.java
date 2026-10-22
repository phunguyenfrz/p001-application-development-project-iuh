package com.petshop.main;

import com.petshop.view.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Happy Tails - Đăng nhập hệ thống");
        try {
            var iconStream = getClass().getResourceAsStream("/com/petshop/view/logo.png");
            if (iconStream != null) {
                stage.getIcons().setAll(new Image(iconStream));
            }
        } catch (Exception ignored) {}

        Scene scene = new Scene(new LoginView(stage), 520, 400);
        scene.getStylesheets().addAll(
                getClass().getResource("/com/petshop/view/css/base.css").toExternalForm(),
                getClass().getResource("/com/petshop/view/css/login.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
