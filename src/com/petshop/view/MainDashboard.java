package com.petshop.view;

import com.petshop.model.User;
import com.petshop.service.AuthService;
import com.petshop.service.MenuRegistry;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Giao diện Bảng điều khiển (Dashboard) chuẩn Hướng Đối Tượng của Happy Tails.
 * Sidebar được dựng tự động 100% từ MenuRegistry theo đúng 11 mục đối tượng.
 */
public class MainDashboard extends BorderPane {

    private final Stage stage;
    private final User user;
    private final AuthService authService;

    // Vùng nội dung chính (Container Swapping)
    private final StackPane contentArea;

    // Quản lý Accordion (chỉ mở 1 đối tượng tại 1 thời điểm)
    private final List<VBox> allSubMenus = new ArrayList<>();
    private final List<Label> allArrows = new ArrayList<>();
    private final List<Label> allBullets = new ArrayList<>();

    // Thu gọn sidebar
    private boolean isSidebarCollapsed = false;
    private StackPane sidebarContainer;

    // Highlight mục đang chọn
    private Node currentActiveItemNode;

    private TextField searchField;

    public MainDashboard(Stage stage) {
        this.stage = stage;
        this.authService = new AuthService();
        this.user = authService.getCurrentUser();

        getStyleClass().add("dashboard");

        // 1. Sidebar bên trái
        this.sidebarContainer = new StackPane();
        renderSidebar();
        setLeft(sidebarContainer);

        // 2. Nội dung chính
        VBox mainContainer = new VBox();
        HBox.setHgrow(mainContainer, Priority.ALWAYS);

        this.contentArea = new StackPane();
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        // Mặc định nạp màn hình Tổng quan
        showOverview();

        mainContainer.getChildren().addAll(
                createTopbar(),
                contentArea
        );

        setCenter(mainContainer);

        // Đăng ký phím tắt Ctrl + K để focus vào ô tìm kiếm nhanh
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
                    if (event.isControlDown() && event.getCode() == javafx.scene.input.KeyCode.K) {
                        if (searchField != null) {
                            searchField.requestFocus();
                            searchField.selectAll();
                            event.consume();
                        }
                    }
                });
            }
        });
    }

    private void renderSidebar() {
        sidebarContainer.getChildren().clear();
        if (isSidebarCollapsed) {
            sidebarContainer.getChildren().add(createCollapsedSidebar());
        } else {
            sidebarContainer.getChildren().add(createExpandedSidebar());
        }
    }

    // =========================================================================
    // 1. SIDEBAR MỞ RỘNG (EXPANDED) - ACCORDION HƯỚNG ĐỐI TƯỢNG
    // =========================================================================
    private VBox createExpandedSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");

        allSubMenus.clear();
        allArrows.clear();
        allBullets.clear();

        // Brand Logo + Nút thu gọn
        HBox brandBox = new HBox(10);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        brandBox.getStyleClass().add("sidebar-brand-box");

        StackPane logoCircle = new StackPane();
        Circle circle = new Circle(17, Color.web("#d97736"));
        Group paw = createMiniPawWhite();
        logoCircle.getChildren().addAll(circle, paw);
        logoCircle.setOnMouseClicked(e -> showOverview());
        logoCircle.setStyle("-fx-cursor: hand;");

        VBox brandText = new VBox(1);
        Label title = new Label("Happy Tails");
        title.getStyleClass().add("sidebar-title");
        title.setOnMouseClicked(e -> showOverview());
        title.setStyle("-fx-cursor: hand;");

        Label subtitle = new Label("Cửa hàng thú cưng");
        subtitle.getStyleClass().add("sidebar-subtitle");
        brandText.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnCollapse = new Button("«");
        btnCollapse.getStyleClass().add("btn-collapse");
        btnCollapse.setTooltip(new Tooltip("Thu gọn thanh điều hướng"));
        btnCollapse.setOnAction(e -> {
            isSidebarCollapsed = true;
            renderSidebar();
        });

        brandBox.getChildren().addAll(logoCircle, brandText, spacer, btnCollapse);

        // Danh sách Menu
        VBox menuList = new VBox(4);
        menuList.setPadding(new Insets(10, 12, 10, 12));

        // Nút "Tổng quan" (Trang chủ)
        Button btnOverview = new Button("❖  Tổng quan");
        btnOverview.setMaxWidth(Double.MAX_VALUE);
        btnOverview.getStyleClass().add("sidebar-btn-active");
        btnOverview.setTooltip(new Tooltip("Trang chủ bảng điều khiển"));
        this.currentActiveItemNode = btnOverview;
        btnOverview.setOnAction(e -> {
            setActiveNode(btnOverview);
            collapseAllAccordions();
            showOverview();
        });
        menuList.getChildren().add(btnOverview);

        // Dựng 11 Menu Đối Tượng từ MenuRegistry
        User.Role role = (user != null) ? user.getRole() : User.Role.ADMIN;
        List<MenuRegistry.ObjectMenu> objectMenus = MenuRegistry.getMenusForRole(role);

        for (MenuRegistry.ObjectMenu menu : objectMenus) {
            VBox accordionItem = createAccordionItem(menu);
            menuList.getChildren().add(accordionItem);
        }

        ScrollPane menuScroll = new ScrollPane(menuList);
        menuScroll.setFitToWidth(true);
        menuScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(menuScroll, Priority.ALWAYS);

        // Footer Branch Card
        VBox footerBox = new VBox();
        footerBox.setPadding(new Insets(12, 12, 16, 12));

        HBox branchCard = new HBox(10);
        branchCard.setAlignment(Pos.CENTER_LEFT);
        branchCard.getStyleClass().add("branch-card");

        StackPane pinIcon = new StackPane();
        Circle pinBg = new Circle(14, Color.web("#e1efe1"));
        SVGPath pinSvg = new SVGPath();
        pinSvg.setContent("M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z");
        pinSvg.setFill(Color.web("#3e634c"));
        pinSvg.setScaleX(0.55);
        pinSvg.setScaleY(0.55);
        pinIcon.getChildren().addAll(pinBg, pinSvg);

        VBox branchInfo = new VBox(2);
        Label branchName = new Label("Chi nhánh Quận 1");
        branchName.getStyleClass().add("branch-title");
        Label branchCity = new Label("TP. HCM");
        branchCity.getStyleClass().add("branch-sub");
        branchInfo.getChildren().addAll(branchName, branchCity);

        branchCard.getChildren().addAll(pinIcon, branchInfo);
        footerBox.getChildren().add(branchCard);

        sidebar.getChildren().addAll(brandBox, menuScroll, footerBox);
        return sidebar;
    }

    private VBox createAccordionItem(MenuRegistry.ObjectMenu menu) {
        VBox itemBox = new VBox(2);
        itemBox.setPadding(new Insets(2, 0, 2, 0));

        HBox headerBtn = new HBox(8);
        headerBtn.setAlignment(Pos.CENTER_LEFT);
        headerBtn.getStyleClass().add("sidebar-nav-item");

        Label bullet = new Label("○");
        bullet.getStyleClass().add("sidebar-nav-bullet");
        allBullets.add(bullet);

        Label lblTitle = new Label(menu.getTitle());
        lblTitle.getStyleClass().add("sidebar-nav-text");
        lblTitle.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(lblTitle, Priority.ALWAYS);

        Tooltip tooltip = new Tooltip("Đối tượng: " + menu.getTitle());
        Tooltip.install(headerBtn, tooltip);

        Label lblArrow = new Label("▸");
        lblArrow.getStyleClass().add("sidebar-nav-arrow");
        allArrows.add(lblArrow);

        headerBtn.getChildren().addAll(bullet, lblTitle, lblArrow);

        // Danh sách chức năng con
        VBox subMenuList = new VBox(2);
        for (MenuRegistry.SubAction action : menu.getSubActions()) {
            Button subBtn = new Button(action.getName());
            subBtn.setMaxWidth(Double.MAX_VALUE);
            subBtn.getStyleClass().add("sidebar-sub-btn");
            if (action.getShortcut() != null) {
                subBtn.setTooltip(new Tooltip(action.getName() + " (" + action.getShortcut() + ")"));
            } else {
                subBtn.setTooltip(new Tooltip(action.getName()));
            }

            subBtn.setOnAction(e -> {
                setActiveNode(subBtn);
                contentArea.getChildren().setAll(new ModuleListView(menu.getTitle(), action.getName(), action.getShortcut()));
            });
            subMenuList.getChildren().add(subBtn);
        }

        subMenuList.setVisible(false);
        subMenuList.setManaged(false);
        allSubMenus.add(subMenuList);

        // Accordion: chỉ mở 1 đối tượng tại một thời điểm
        headerBtn.setOnMouseClicked(e -> {
            boolean isCurrentlyOpen = subMenuList.isVisible();
            collapseAllAccordions();
            if (!isCurrentlyOpen) {
                subMenuList.setVisible(true);
                subMenuList.setManaged(true);
                lblArrow.setText("▾");
                bullet.setText("●");
            }
        });

        itemBox.getChildren().addAll(headerBtn, subMenuList);
        return itemBox;
    }

    private void collapseAllAccordions() {
        for (int i = 0; i < allSubMenus.size(); i++) {
            allSubMenus.get(i).setVisible(false);
            allSubMenus.get(i).setManaged(false);
            if (i < allArrows.size()) allArrows.get(i).setText("▸");
            if (i < allBullets.size()) allBullets.get(i).setText("○");
        }
    }

    // =========================================================================
    // 2. SIDEBAR THU GỌN (COLLAPSED)
    // =========================================================================
    private VBox createCollapsedSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar-collapsed");
        sidebar.setAlignment(Pos.TOP_CENTER);

        VBox topBox = new VBox(10);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(16, 0, 12, 0));

        StackPane logoCircle = new StackPane();
        Circle circle = new Circle(17, Color.web("#d97736"));
        Group paw = createMiniPawWhite();
        logoCircle.getChildren().addAll(circle, paw);
        logoCircle.setOnMouseClicked(e -> showOverview());
        logoCircle.setStyle("-fx-cursor: hand;");

        Button btnExpand = new Button("»");
        btnExpand.getStyleClass().add("btn-collapse");
        btnExpand.setTooltip(new Tooltip("Mở rộng thanh điều hướng"));
        btnExpand.setOnAction(e -> {
            isSidebarCollapsed = false;
            renderSidebar();
        });

        topBox.getChildren().addAll(logoCircle, btnExpand);

        VBox menuList = new VBox(6);
        menuList.setAlignment(Pos.TOP_CENTER);
        menuList.setPadding(new Insets(8, 0, 8, 0));

        // Nút tổng quan thu gọn
        Button btnHome = new Button("❖");
        btnHome.getStyleClass().add("sidebar-collapsed-btn");
        btnHome.setTooltip(new Tooltip("Tổng quan"));
        btnHome.setOnAction(e -> {
            setActiveNode(btnHome);
            showOverview();
        });
        menuList.getChildren().add(btnHome);

        User.Role role = (user != null) ? user.getRole() : User.Role.ADMIN;
        List<MenuRegistry.ObjectMenu> objectMenus = MenuRegistry.getMenusForRole(role);

        for (MenuRegistry.ObjectMenu menu : objectMenus) {
            Button btnObj = new Button(menu.getTitle().substring(0, 1));
            btnObj.getStyleClass().add("sidebar-collapsed-btn");
            btnObj.setTooltip(new Tooltip("Đối tượng: " + menu.getTitle()));
            btnObj.setOnAction(e -> {
                // Click vào đối tượng ở chế độ thu gọn -> tự bung mở sidebar để chọn chức năng con
                isSidebarCollapsed = false;
                renderSidebar();
            });
            menuList.getChildren().add(btnObj);
        }

        ScrollPane menuScroll = new ScrollPane(menuList);
        menuScroll.setFitToWidth(true);
        menuScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(menuScroll, Priority.ALWAYS);

        sidebar.getChildren().addAll(topBox, menuScroll);
        return sidebar;
    }

    private void setActiveNode(Node node) {
        if (currentActiveItemNode != null) {
            currentActiveItemNode.getStyleClass().remove("sidebar-btn-active");
            currentActiveItemNode.getStyleClass().remove("sidebar-sub-btn-active");
            currentActiveItemNode.getStyleClass().remove("sidebar-collapsed-btn-active");
            if (currentActiveItemNode instanceof Button) {
                if (((Button) currentActiveItemNode).getText().contains("Tổng quan")) {
                    currentActiveItemNode.getStyleClass().add("sidebar-btn");
                } else {
                    currentActiveItemNode.getStyleClass().add("sidebar-sub-btn");
                }
            }
        }
        this.currentActiveItemNode = node;
        if (node instanceof Button) {
            if (((Button) node).getText().contains("Tổng quan")) {
                node.getStyleClass().remove("sidebar-btn");
                node.getStyleClass().add("sidebar-btn-active");
            } else if (((Button) node).getStyleClass().contains("sidebar-collapsed-btn")) {
                node.getStyleClass().add("sidebar-collapsed-btn-active");
            } else {
                node.getStyleClass().remove("sidebar-sub-btn");
                node.getStyleClass().add("sidebar-sub-btn-active");
            }
        }
    }

    // =========================================================================
    // 3. TOPBAR (HEADER DÙNG CHUNG)
    // =========================================================================
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

        this.searchField = new TextField();
        searchField.setPromptText("Tìm kiếm nhanh (Ctrl + K)...");
        searchField.getStyleClass().add("search-field");

        Label badgeK = new Label("Ctrl K");
        badgeK.getStyleClass().add("shortcut-badge");

        searchBox.getChildren().addAll(searchIcon, searchField, badgeK);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Profile Avatar + Thông tin User
        String fullName = (user != null) ? user.getFullName() : "Admin";
        String roleName = (user != null) ? user.getRole().getDisplayName() : "Admin";
        String initials = getInitials(fullName);

        StackPane avatarCircle = new StackPane();
        Circle avBg = new Circle(18, Color.web("#dcf0dc"));
        Label avText = new Label(initials);
        avText.getStyleClass().add("user-badge-avatar");
        avatarCircle.getChildren().addAll(avBg, avText);

        VBox userDetails = new VBox(2);
        Label userNameLbl = new Label(fullName);
        userNameLbl.getStyleClass().add("topbar-user-name");
        Label userRoleLbl = new Label(roleName);
        userRoleLbl.getStyleClass().add("topbar-user-role");
        userDetails.getChildren().addAll(userNameLbl, userRoleLbl);

        // Menu tài khoản: Đổi mật khẩu & Đăng xuất
        MenuButton profileMenu = new MenuButton();
        profileMenu.setGraphic(new HBox(8, avatarCircle, userDetails));
        profileMenu.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

        MenuItem changePassItem = new MenuItem("🔑 Đổi mật khẩu");
        changePassItem.setOnAction(e -> showChangePasswordDialog());

        MenuItem logoutItem = new MenuItem("🚪 Đăng xuất");
        logoutItem.setOnAction(e -> logout());

        profileMenu.getItems().addAll(changePassItem, new SeparatorMenuItem(), logoutItem);

        topbar.getChildren().addAll(
                searchBox,
                spacer,
                profileMenu
        );
        return topbar;
    }

    // =========================================================================
    // 4. MÀN HÌNH TỔNG QUAN (OVERVIEW)
    // =========================================================================
    private void showOverview() {
        contentArea.getChildren().setAll(createScrollableOverview());
    }

    private ScrollPane createScrollableOverview() {
        VBox content = new VBox(22);
        content.getStyleClass().add("dashboard-content");

        HBox welcomeHeader = new HBox();
        welcomeHeader.setAlignment(Pos.CENTER_LEFT);

        String empName = (user != null) ? user.getFullName() : "Quản trị viên";
        VBox welcomeTextBox = new VBox(4);
        Label welcomeTitle = new Label("Xin chào, " + empName);
        welcomeTitle.getStyleClass().add("welcome-title");
        Label welcomeSub = new Label("Hệ thống quản lý cửa hàng thú cưng Happy Tails theo mô hình Hướng Đối Tượng.");
        welcomeSub.getStyleClass().add("welcome-sub");
        welcomeTextBox.getChildren().addAll(welcomeTitle, welcomeSub);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        String todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Label dateBadge = new Label(todayStr + "  📅");
        dateBadge.getStyleClass().add("date-badge");

        welcomeHeader.getChildren().addAll(welcomeTextBox, headerSpacer, dateBadge);

        // Thẻ KPI
        HBox statsRow = createStatsRow();

        // Hoạt động gần đây
        VBox summaryPanel = new VBox(14);
        summaryPanel.getStyleClass().add("chart-panel");
        Label title = new Label("Hoạt động gần đây");
        title.getStyleClass().add("panel-title");
        Label sub = new Label("Chưa có hoạt động hôm nay.");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #7d8f82;");
        summaryPanel.getChildren().addAll(title, sub);

        content.getChildren().addAll(welcomeHeader, statsRow, summaryPanel);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f3f6f3; -fx-background: #f3f6f3; -fx-border-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        return scrollPane;
    }

    private HBox createStatsRow() {
        HBox row = new HBox(16);

        // Thẻ 1: Doanh thu hôm nay
        VBox c1 = new VBox(6);
        c1.getStyleClass().add("stat-card-featured");
        HBox.setHgrow(c1, Priority.ALWAYS);
        Label t1 = new Label("📈  Doanh thu hôm nay");
        t1.getStyleClass().add("stat-title-white");
        Label v1 = new Label("0 đ");
        v1.getStyleClass().add("stat-value-white");
        Label d1 = new Label("Tổng thanh toán trong ngày");
        d1.getStyleClass().add("stat-sub-white");
        c1.getChildren().addAll(t1, v1, d1);

        // Thẻ 2: Lịch hẹn
        VBox c2 = new VBox(6);
        c2.getStyleClass().add("stat-card");
        HBox.setHgrow(c2, Priority.ALWAYS);
        Label t2 = new Label("📅  Lịch hẹn hôm nay");
        t2.getStyleClass().add("stat-title");
        Label v2 = new Label("0");
        v2.getStyleClass().add("stat-value");
        Label d2 = new Label("Lịch hẹn cần phục vụ");
        d2.getStyleClass().add("stat-sub");
        c2.getChildren().addAll(t2, v2, d2);

        // Thẻ 3: Hết hàng (hiển thị '0' thay vì '00')
        VBox c3 = new VBox(6);
        c3.getStyleClass().add("stat-card");
        HBox.setHgrow(c3, Priority.ALWAYS);
        Label t3 = new Label("⚠  Sản phẩm sắp hết hàng");
        t3.getStyleClass().add("stat-title");
        Label v3 = new Label("0");
        v3.getStyleClass().add("stat-value");
        Label d3 = new Label("Tồn kho dưới ngưỡng an toàn");
        d3.getStyleClass().add("stat-sub");
        c3.getChildren().addAll(t3, v3, d3);

        row.getChildren().addAll(c1, c2, c3);
        return row;
    }

    // =========================================================================
    // 5. TIỆN ÍCH TÀI KHOẢN
    // =========================================================================
    private void showChangePasswordDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Đổi mật khẩu");
        dialog.setHeaderText("Cập nhật mật khẩu tài khoản");

        VBox form = new VBox(10);
        form.setPadding(new Insets(20));

        PasswordField oldPass = new PasswordField();
        oldPass.setPromptText("Mật khẩu hiện tại");

        PasswordField newPass = new PasswordField();
        newPass.setPromptText("Mật khẩu mới");

        PasswordField confirmPass = new PasswordField();
        confirmPass.setPromptText("Xác nhận mật khẩu mới");

        form.getChildren().addAll(
                new Label("Mật khẩu hiện tại:"), oldPass,
                new Label("Mật khẩu mới:"), newPass,
                new Label("Xác nhận mật khẩu:"), confirmPass
        );

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait();
    }

    private void logout() {
        authService.logout();
        stage.setTitle("Happy Tails | Đăng nhập hệ thống");
        Scene scene = new Scene(new LoginView(stage), 600, 680);
        scene.getStylesheets().add(getClass().getResource("/com/petshop/view/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    private Group createMiniPawWhite() {
        Group group = new Group();
        Color c = Color.WHITE;
        Ellipse pad = new Ellipse(0, 3, 6, 4.5);
        pad.setFill(c);
        Circle t1 = new Circle(-5.5, -4, 1.8, c);
        Circle t2 = new Circle(-2, -6.5, 2.0, c);
        Circle t3 = new Circle(2, -6.5, 2.0, c);
        Circle t4 = new Circle(5.5, -4, 1.8, c);
        group.getChildren().addAll(pad, t1, t2, t3, t4);
        return group;
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "HT";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[parts.length - 2].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}
