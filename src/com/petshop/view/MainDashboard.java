package com.petshop.view;

import com.petshop.model.User;
import com.petshop.util.SessionManager;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainDashboard extends BorderPane {
    private final Stage stage;

    public MainDashboard(Stage stage) {
        this.stage = stage;
        User user = SessionManager.getCurrentUser();
        getStyleClass().add("dashboard");
        setTop(createHeader(user));
        setCenter(createMenu(user));
        setPadding(new Insets(28));
    }

    private VBox createHeader(User user) {
        Label title = new Label("Quản lý cửa hàng thú cưng");
        title.getStyleClass().add("dashboard-title");
        Label account = new Label(user.getFullName() + "  |  " + user.getRole().getDisplayName());
        account.getStyleClass().add("account-label");
        Button logoutButton = new Button("Đăng xuất");
        logoutButton.setOnAction(event -> logout());
        VBox header = new VBox(6, title, account, logoutButton);
        header.getStyleClass().add("dashboard-header");
        return header;
    }

    private GridPane createMenu(User user) {
        GridPane menu = new GridPane();
        menu.setHgap(16);
        menu.setVgap(16);
        menu.setPadding(new Insets(32, 0, 20, 0));
        addModule(menu, "Quản lý sản phẩm", 0, 0);
        addModule(menu, "Quản lý thú cưng", 1, 0);
        addModule(menu, "Bán hàng", 2, 0);
        addModule(menu, "Khách hàng", 0, 1);
        addModule(menu, "Báo cáo thống kê", 1, 1);
        Button employees = addModule(menu, "Quản lý nhân viên", 2, 1);
        employees.setDisable(user.getRole() != User.Role.OWNER);
        return menu;
    }

    private Button addModule(GridPane menu, String text, int column, int row) {
        Button button = new Button(text);
        button.setPrefSize(280, 110);
        button.getStyleClass().add("module-button");
        button.setOnAction(event -> showComingSoon(text));
        menu.add(button, column, row);
        return button;
    }

    private void showComingSoon(String module) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(module);
        alert.setHeaderText(null);
        alert.setContentText("Phân hệ này sẽ được phát triển ở bước tiếp theo.");
        alert.showAndWait();
    }

    private void logout() {
        SessionManager.logout();
        stage.setTitle("PetShop Management | Đăng nhập");
        Scene scene = new Scene(new LoginView(stage), 520, 600);
        scene.getStylesheets().add(getClass().getResource("/com/petshop/view/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.centerOnScreen();
    }
}
