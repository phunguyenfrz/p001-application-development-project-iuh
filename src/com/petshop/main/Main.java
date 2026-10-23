package com.petshop.main;

import com.petshop.view.DangNhapView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Happy Tails - Đăng nhập");
        try {
            var iconStream = getClass().getResourceAsStream("/com/petshop/view/logo.png");
            if (iconStream != null) {
                stage.getIcons().setAll(new Image(iconStream));
            }
        } catch (Exception ignored) {}

        Scene scene = new Scene(new DangNhapView(stage), 520, 400);
        scene.getStylesheets().addAll(
                getClass().getResource("/com/petshop/view/css/chung.css").toExternalForm(),
                getClass().getResource("/com/petshop/view/css/dangnhap.css").toExternalForm()
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
