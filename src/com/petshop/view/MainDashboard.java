package com.petshop.view;

import com.petshop.controller.NavigationController;
import com.petshop.model.User;
import com.petshop.util.RoleMenuConfig;
import com.petshop.util.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MainDashboard extends BorderPane {
    private final Stage stage;
    private final User user;
    private final NavigationController navigationController;

    public MainDashboard(Stage stage) {
        this.stage = stage;
        this.user = SessionManager.getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("Cần đăng nhập để mở bảng điều khiển.");
        }
        this.navigationController = new NavigationController(stage, this::showDashboard);

        getStyleClass().add("dashboard");
        setLeft(createSidebar());

        VBox mainContainer = new VBox();
        mainContainer.getChildren().addAll(createTopbar(), createScrollableBody());
        setCenter(mainContainer);
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");

        HBox brandBox = new HBox(10);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        brandBox.getStyleClass().add("sidebar-brand-box");

        StackPane logoCircle = new StackPane();
        Circle circle = new Circle(17, Color.web("#d97736"));
        logoCircle.getChildren().addAll(circle, createMiniPawWhite());

        VBox brandText = new VBox(1);
        Label title = new Label("Happy Tails");
        title.getStyleClass().add("sidebar-title");
        Label subtitle = new Label("Cửa hàng thú cưng");
        subtitle.getStyleClass().add("sidebar-subtitle");
        brandText.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button collapse = new Button("«");
        collapse.getStyleClass().add("btn-collapse");
        brandBox.getChildren().addAll(logoCircle, brandText, spacer, collapse);

        VBox menuList = new VBox(4);
        menuList.setPadding(new Insets(10, 12, 10, 12));
        menuList.getChildren().add(createMenuButton(
                "❖  Tổng quan", true, RoleMenuConfig.Screen.OVERVIEW));
        for (RoleMenuConfig.MenuSection section : RoleMenuConfig.getMenu(user.getRole())) {
            if (section.entries().size() == 1
                    && section.entries().get(0).label().equals(section.label())) {
                RoleMenuConfig.MenuEntry entry = section.entries().get(0);
                menuList.getChildren().add(createMenuButton(entry.label(), false, entry.screen()));
            } else {
                menuList.getChildren().add(createNavDropdownItem(section));
            }
        }

        ScrollPane menuScroll = new ScrollPane(menuList);
        menuScroll.setFitToWidth(true);
        menuScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(menuScroll, Priority.ALWAYS);
        sidebar.getChildren().addAll(brandBox, menuScroll);
        return sidebar;
    }

    private VBox createNavDropdownItem(RoleMenuConfig.MenuSection section) {
        VBox itemBox = new VBox(2);
        itemBox.setPadding(new Insets(2, 0, 2, 0));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("sidebar-nav-item");

        Label bullet = new Label("○");
        bullet.getStyleClass().add("sidebar-nav-bullet");
        Label title = new Label(section.label());
        title.getStyleClass().add("sidebar-nav-text");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label arrow = new Label("▸");
        arrow.getStyleClass().add("sidebar-nav-arrow");
        header.getChildren().addAll(bullet, title, spacer, arrow);

        VBox submenu = new VBox(2);
        for (RoleMenuConfig.MenuEntry entry : section.entries()) {
            submenu.getChildren().add(createSubMenuButton(entry));
        }
        submenu.setVisible(false);
        submenu.setManaged(false);
        header.setOnMouseClicked(event -> {
            boolean visible = !submenu.isVisible();
            submenu.setVisible(visible);
            submenu.setManaged(visible);
            arrow.setText(visible ? "▾" : "▸");
            bullet.setText(visible ? "●" : "○");
        });

        itemBox.getChildren().addAll(header, submenu);
        return itemBox;
    }

    private Button createMenuButton(String label, boolean active, RoleMenuConfig.Screen screen) {
        Button button = new Button(label);
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add(active ? "sidebar-btn-active" : "sidebar-btn");
        button.setOnAction(event -> navigationController.navigate(screen, label.trim()));
        return button;
    }

    private Button createSubMenuButton(RoleMenuConfig.MenuEntry entry) {
        Button button = new Button(entry.label());
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add("sidebar-sub-btn");
        button.setOnAction(event -> navigationController.navigate(entry.screen(), entry.label()));
        return button;
    }

    private HBox createTopbar() {
        HBox topbar = new HBox(16);
        topbar.getStyleClass().add("topbar");

        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.getStyleClass().add("search-box");
        SVGPath searchIcon = new SVGPath();
        searchIcon.setContent("M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z");
        searchIcon.setFill(Color.web("#9ca89e"));
        searchIcon.setScaleX(0.7);
        searchIcon.setScaleY(0.7);
        TextField searchField = new TextField();
        searchField.setPromptText("Tìm kiếm nhanh...");
        searchField.getStyleClass().add("search-field");
        Label shortcut = new Label("⌘ K");
        shortcut.getStyleClass().add("shortcut-badge");
        searchBox.getChildren().addAll(searchIcon, searchField, shortcut);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button help = createIconButton("?");
        Button mail = createIconButton("✉");
        Button bell = createIconButton("🔔");

        String fullName = user.getFullName();
        StackPane avatar = new StackPane();
        avatar.getChildren().addAll(
                new Circle(18, Color.web("#dcf0dc")),
                new Label(getInitials(fullName)));
        avatar.getChildren().get(1).getStyleClass().add("user-badge-avatar");

        VBox userDetails = new VBox(2);
        Label name = new Label(fullName);
        name.getStyleClass().add("topbar-user-name");
        Label role = new Label(user.getRole().getDisplayName());
        role.getStyleClass().add("topbar-user-role");
        userDetails.getChildren().addAll(name, role);

        MenuButton profile = new MenuButton();
        profile.setGraphic(new HBox(8, avatar, userDetails));
        profile.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        MenuItem logout = new MenuItem("Đăng xuất");
        logout.setOnAction(event -> logout());
        profile.getItems().add(logout);
        topbar.getChildren().addAll(searchBox, spacer, help, mail, bell, profile);
        return topbar;
    }

    private Button createIconButton(String icon) {
        Button button = new Button(icon);
        button.getStyleClass().add("icon-btn");
        return button;
    }

    private ScrollPane createScrollableBody() {
        VBox content = new VBox(22);
        content.getStyleClass().add("dashboard-content");

        HBox welcomeHeader = new HBox();
        welcomeHeader.setAlignment(Pos.CENTER_LEFT);
        VBox welcomeText = new VBox(4);
        Label welcomeTitle = new Label("Welcome back, " + user.getFullName());
        welcomeTitle.getStyleClass().add("welcome-title");
        Label welcomeSubtitle = new Label(
                "Mọi điều tốt đẹp bắt đầu từ một chiếc đuôi hạnh phúc.");
        welcomeSubtitle.getStyleClass().add("welcome-sub");
        welcomeText.getChildren().addAll(welcomeTitle, welcomeSubtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label date = new Label(LocalDate.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "  📅");
        date.getStyleClass().add("date-badge");
        welcomeHeader.getChildren().addAll(welcomeText, spacer, date);
        content.getChildren().addAll(welcomeHeader,
                new RoleDashboardContent(user, navigationController));

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f3f6f3; -fx-background: #f3f6f3; -fx-border-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        return scrollPane;
    }

    private Group createMiniPawWhite() {
        Group paw = new Group();
        Ellipse pad = new Ellipse(0, 3, 7, 5);
        pad.setFill(Color.WHITE);
        Circle toe1 = new Circle(-6, -4, 2.3, Color.WHITE);
        Circle toe2 = new Circle(-2, -7, 2.5, Color.WHITE);
        Circle toe3 = new Circle(2, -7, 2.5, Color.WHITE);
        Circle toe4 = new Circle(6, -4, 2.3, Color.WHITE);
        paw.getChildren().addAll(pad, toe1, toe2, toe3, toe4);
        return paw;
    }

    private String getInitials(String name) {
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

    private void showDashboard() {
        stage.setTitle("Happy Tails | Bảng điều khiển");
        Scene scene = new Scene(new MainDashboard(stage), 1220, 800);
        scene.getStylesheets().add(
                getClass().getResource("/com/petshop/view/styles.css").toExternalForm());
        stage.setScene(scene);
    }

    private void logout() {
        SessionManager.logout();
        stage.setTitle("Happy Tails | Đăng nhập hệ thống");
        Scene scene = new Scene(new LoginView(stage), 600, 680);
        scene.getStylesheets().add(
                getClass().getResource("/com/petshop/view/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.centerOnScreen();
    }
}
