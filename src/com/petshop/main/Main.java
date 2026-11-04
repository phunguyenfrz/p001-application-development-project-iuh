package com.petshop.main;

import com.petshop.gui.DangNhapView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Happy Tails - Đăng nhập");
        try {
            var iconStream = getClass().getResourceAsStream("/com/petshop/gui/logo.png");
            if (iconStream != null) {
                stage.getIcons().setAll(new Image(iconStream));
            }
        } catch (Exception ignored) {}

        Scene scene = new Scene(new DangNhapView(stage), 540, 420);
        scene.getStylesheets().addAll(
                getClass().getResource("/com/petshop/gui/css/chung.css").toExternalForm(),
                getClass().getResource("/com/petshop/gui/css/dangnhap.css").toExternalForm()
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
