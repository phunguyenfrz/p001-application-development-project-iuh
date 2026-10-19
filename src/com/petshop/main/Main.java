package com.petshop.main;

import com.petshop.view.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        stage.setTitle("Happy Tails | Đăng nhập hệ thống");
        stage.setMinWidth(480);
        stage.setMinHeight(620);
        Scene scene = new Scene(new LoginView(stage), 600, 680);
        scene.getStylesheets().add(getClass().getResource("/com/petshop/view/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
