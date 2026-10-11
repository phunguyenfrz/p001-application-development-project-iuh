package com.petshop.view;

import com.petshop.main.ApplicationControllers;
import com.petshop.model.User;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public final class TopbarView extends HBox {
    private final GlobalSearchPopup searchPopup;

    public TopbarView(
            User user, ApplicationControllers controllers,
            com.petshop.controller.ScreenNavigator navigator, Runnable logoutAction) {
        super(16);
        getStyleClass().add("topbar");
        searchPopup = new GlobalSearchPopup(controllers.globalSearch(), user.getRole(), navigator);
        HBox search = new HBox(8, new Label("⌕"), searchPopup.field(), new Label("Ctrl K"));
        search.setAlignment(Pos.CENTER_LEFT);
        search.getStyleClass().add("search-box");
        searchPopup.field().getStyleClass().add("search-field");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button help = iconButton("?", "Trợ giúp");
        help.setOnAction(event -> showHelp(user));
        Button notifications = iconButton("🔔", "Thông báo");
        notifications.setOnAction(event ->
                new NotificationPopup(controllers.dashboard(), user).show());
        Label avatar = new Label(initials(user.getFullName()));
        avatar.getStyleClass().add("user-badge-avatar");
        MenuButton profile = new MenuButton();
        profile.setGraphic(new HBox(8, avatar, new Label(user.getFullName())));
        profile.getStyleClass().add("profile-menu");
        MenuItem changePassword = new MenuItem("Đổi mật khẩu");
        changePassword.setOnAction(event ->
                new ChangePasswordDialog(controllers.password(), user).show());
        MenuItem logout = new MenuItem("Đăng xuất");
        logout.setOnAction(event -> logoutAction.run());
        profile.getItems().addAll(changePassword, logout);
        getChildren().addAll(search, spacer, help, notifications, profile);
    }

    public void focusSearch() {
        searchPopup.focus();
    }

    private Button iconButton(String text, String tooltip) {
        Button button = new Button(text);
        button.getStyleClass().add("icon-btn");
        button.setTooltip(new Tooltip(tooltip));
        return button;
    }

    private void showHelp(User user) {
        Alert help = new Alert(Alert.AlertType.INFORMATION);
        help.setTitle("Trợ giúp Happy Tails");
        help.setHeaderText("Hướng dẫn theo vai trò: " + user.getRole().getDisplayName());
        help.setContentText(user.getRole().getHelpText()
                + "\n\nDùng ô tìm kiếm hoặc nhấn Ctrl+K để tra cứu dữ liệu được phép xem.");
        help.showAndWait();
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) {
            return "HT";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return ("" + parts[parts.length - 2].charAt(0)
                + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}
