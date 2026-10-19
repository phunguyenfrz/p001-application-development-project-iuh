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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    private final StackPane pnlContentArea;

    // Quản lý Accordion (chỉ mở 1 đối tượng tại 1 thời điểm)
    private final List<VBox> allSubMenus = new ArrayList<>();
    private final List<Label> allArrows = new ArrayList<>();
    private final List<Label> allBullets = new ArrayList<>();

    // Thu gọn sidebar
    private boolean isSidebarCollapsed = false;
    private StackPane pnlSidebarContainer;

    // Highlight mục đang chọn
    private Node currentActiveItemNode;

    private TextField txtSearch;

    public MainDashboard(Stage stage) {
        this.stage = stage;
        this.authService = new AuthService();
        this.user = authService.getCurrentUser();

        getStyleClass().add("dashboard");

        // 1. Sidebar bên trái
        this.pnlSidebarContainer = new StackPane();
        renderSidebar();
        setLeft(pnlSidebarContainer);

        // 2. Nội dung chính
        VBox pnlMainContainer = new VBox();
        HBox.setHgrow(pnlMainContainer, Priority.ALWAYS);

        this.pnlContentArea = new StackPane();
        VBox.setVgrow(pnlContentArea, Priority.ALWAYS);

        // Mặc định nạp màn hình Tổng quan
        showOverview();

        pnlMainContainer.getChildren().addAll(
                createTopbar(),
                pnlContentArea
        );

        setCenter(pnlMainContainer);

        // Đăng ký phím tắt Ctrl + K để focus vào ô tìm kiếm nhanh
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
                    if (event.isControlDown() && event.getCode() == javafx.scene.input.KeyCode.K) {
                        if (txtSearch != null) {
                            txtSearch.requestFocus();
                            txtSearch.selectAll();
                            event.consume();
                        }
                    }
                });
            }
        });
    }

    private void renderSidebar() {
        pnlSidebarContainer.getChildren().clear();
        if (isSidebarCollapsed) {
            pnlSidebarContainer.getChildren().add(createCollapsedSidebar());
        } else {
            pnlSidebarContainer.getChildren().add(createExpandedSidebar());
        }
    }

    // =========================================================================
    // 1. SIDEBAR MỞ RỘNG (EXPANDED) - ACCORDION HƯỚNG ĐỐI TƯỢNG
    // =========================================================================
    private VBox createExpandedSidebar() {
        VBox pnlExpandedSidebar = new VBox();
        pnlExpandedSidebar.getStyleClass().add("sidebar");

        allSubMenus.clear();
        allArrows.clear();
        allBullets.clear();

        // Brand Logo + Nút thu gọn
        HBox pnlBrandBox = new HBox(10);
        pnlBrandBox.setAlignment(Pos.CENTER_LEFT);
        pnlBrandBox.getStyleClass().add("sidebar-brand-box");

        StackPane pnlLogoBadge = new StackPane();
        pnlLogoBadge.getChildren().add(createBrandLogo(34));
        pnlLogoBadge.setOnMouseClicked(e -> showOverview());
        pnlLogoBadge.setStyle("-fx-cursor: hand;");

        VBox pnlBrandText = new VBox(1);
        Label lblTitle = new Label("Happy Tails");
        lblTitle.getStyleClass().add("sidebar-title");
        lblTitle.setOnMouseClicked(e -> showOverview());
        lblTitle.setStyle("-fx-cursor: hand;");

        Label lblSubtitle = new Label("Cửa hàng thú cưng");
        lblSubtitle.getStyleClass().add("sidebar-subtitle");
        pnlBrandText.getChildren().addAll(lblTitle, lblSubtitle);

        Region pnlBrandSpacer = new Region();
        HBox.setHgrow(pnlBrandSpacer, Priority.ALWAYS);

        Button btnCollapse = new Button("«");
        btnCollapse.getStyleClass().add("btn-collapse");
        btnCollapse.setTooltip(new Tooltip("Thu gọn thanh điều hướng"));
        btnCollapse.setOnAction(e -> {
            isSidebarCollapsed = true;
            renderSidebar();
        });

        pnlBrandBox.getChildren().addAll(pnlLogoBadge, pnlBrandText, pnlBrandSpacer, btnCollapse);

        // Danh sách Menu
        VBox pnlMenuList = new VBox(4);
        pnlMenuList.setPadding(new Insets(10, 12, 10, 12));

        // Nút "Tổng quan" (Trang chủ)
        Button mnuOverview = new Button("❖  Tổng quan");
        mnuOverview.setMaxWidth(Double.MAX_VALUE);
        mnuOverview.getStyleClass().add("sidebar-btn-active");
        mnuOverview.setTooltip(new Tooltip("Trang chủ bảng điều khiển"));
        this.currentActiveItemNode = mnuOverview;
        mnuOverview.setOnAction(e -> {
            setActiveNode(mnuOverview);
            collapseAllAccordions();
            showOverview();
        });
        pnlMenuList.getChildren().add(mnuOverview);

        // Dựng 11 Menu Đối Tượng từ MenuRegistry
        User.Role role = (user != null) ? user.getRole() : User.Role.ADMIN;
        List<MenuRegistry.MenuGroup> objectMenus = MenuRegistry.getMenusForRole(role);

        for (MenuRegistry.MenuGroup menu : objectMenus) {
            VBox pnlAccordionItem = createAccordionItem(menu);
            pnlMenuList.getChildren().add(pnlAccordionItem);
        }

        ScrollPane scrMenu = new ScrollPane(pnlMenuList);
        scrMenu.setFitToWidth(true);
        scrMenu.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scrMenu, Priority.ALWAYS);

        // Footer Branch Card
        VBox pnlFooterBox = new VBox();
        pnlFooterBox.setPadding(new Insets(12, 12, 16, 12));

        HBox pnlBranchCard = new HBox(10);
        pnlBranchCard.setAlignment(Pos.CENTER_LEFT);
        pnlBranchCard.getStyleClass().add("branch-card");

        StackPane pnlPinIcon = new StackPane();
        Circle pinBg = new Circle(14, Color.web("#e1efe1"));
        SVGPath pinSvg = new SVGPath();
        pinSvg.setContent("M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z");
        pinSvg.setFill(Color.web("#3e634c"));
        pinSvg.setScaleX(0.55);
        pinSvg.setScaleY(0.55);
        pnlPinIcon.getChildren().addAll(pinBg, pinSvg);

        VBox pnlBranchInfo = new VBox(2);
        Label lblBranchName = new Label("Chi nhánh Quận 1");
        lblBranchName.getStyleClass().add("branch-title");
        Label lblBranchCity = new Label("TP. HCM");
        lblBranchCity.getStyleClass().add("branch-sub");
        pnlBranchInfo.getChildren().addAll(lblBranchName, lblBranchCity);

        pnlBranchCard.getChildren().addAll(pnlPinIcon, pnlBranchInfo);
        pnlFooterBox.getChildren().add(pnlBranchCard);

        pnlExpandedSidebar.getChildren().addAll(pnlBrandBox, scrMenu, pnlFooterBox);
        return pnlExpandedSidebar;
    }

    private VBox createAccordionItem(MenuRegistry.MenuGroup menu) {
        VBox pnlAccordionItem = new VBox(2);
        pnlAccordionItem.setPadding(new Insets(2, 0, 2, 0));

        HBox mnuGroupHeader = new HBox(8);
        mnuGroupHeader.setAlignment(Pos.CENTER_LEFT);
        mnuGroupHeader.getStyleClass().add("sidebar-nav-item");

        Label lblBullet = new Label("○");
        lblBullet.getStyleClass().add("sidebar-nav-bullet");
        allBullets.add(lblBullet);

        Label lblTitle = new Label(menu.getTitle());
        lblTitle.getStyleClass().add("sidebar-nav-text");
        lblTitle.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(lblTitle, Priority.ALWAYS);

        Tooltip tooltip = new Tooltip("Đối tượng: " + menu.getTitle());
        Tooltip.install(mnuGroupHeader, tooltip);

        Label lblArrow = new Label("▸");
        lblArrow.getStyleClass().add("sidebar-nav-arrow");
        allArrows.add(lblArrow);

        mnuGroupHeader.getChildren().addAll(lblBullet, lblTitle, lblArrow);

        // Danh sách chức năng con
        VBox pnlSubMenuList = new VBox(2);
        for (MenuRegistry.MenuAction action : menu.getActions()) {
            Button mnuAction = new Button(action.getName());
            mnuAction.setMaxWidth(Double.MAX_VALUE);
            mnuAction.getStyleClass().add("sidebar-sub-btn");
            if (action.getShortcut() != null) {
                mnuAction.setTooltip(new Tooltip(action.getName() + " (" + action.getShortcut() + ")"));
            } else {
                mnuAction.setTooltip(new Tooltip(action.getName()));
            }

            mnuAction.setOnAction(e -> {
                setActiveNode(mnuAction);
                openModuleScreen(menu, action);
            });
            pnlSubMenuList.getChildren().add(mnuAction);
        }

        pnlSubMenuList.setVisible(false);
        pnlSubMenuList.setManaged(false);
        allSubMenus.add(pnlSubMenuList);

        // Accordion: chỉ mở 1 đối tượng tại một thời điểm
        mnuGroupHeader.setOnMouseClicked(e -> {
            boolean isCurrentlyOpen = pnlSubMenuList.isVisible();
            collapseAllAccordions();
            if (!isCurrentlyOpen) {
                pnlSubMenuList.setVisible(true);
                pnlSubMenuList.setManaged(true);
                lblArrow.setText("▾");
                lblBullet.setText("●");
            }
        });

        pnlAccordionItem.getChildren().addAll(mnuGroupHeader, pnlSubMenuList);
        return pnlAccordionItem;
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
        VBox pnlCollapsedSidebar = new VBox();
        pnlCollapsedSidebar.getStyleClass().add("sidebar-collapsed");
        pnlCollapsedSidebar.setAlignment(Pos.TOP_CENTER);

        VBox pnlTopBox = new VBox(10);
        pnlTopBox.setAlignment(Pos.CENTER);
        pnlTopBox.setPadding(new Insets(16, 0, 12, 0));

        StackPane pnlLogoBadge = new StackPane();
        pnlLogoBadge.getChildren().add(createBrandLogo(34));
        pnlLogoBadge.setOnMouseClicked(e -> showOverview());
        pnlLogoBadge.setStyle("-fx-cursor: hand;");

        Button btnExpand = new Button("»");
        btnExpand.getStyleClass().add("btn-collapse");
        btnExpand.setTooltip(new Tooltip("Mở rộng thanh điều hướng"));
        btnExpand.setOnAction(e -> {
            isSidebarCollapsed = false;
            renderSidebar();
        });

        pnlTopBox.getChildren().addAll(pnlLogoBadge, btnExpand);

        VBox pnlMenuList = new VBox(6);
        pnlMenuList.setAlignment(Pos.TOP_CENTER);
        pnlMenuList.setPadding(new Insets(8, 0, 8, 0));

        // Nút tổng quan thu gọn
        Button mnuOverviewCollapsed = new Button("❖");
        mnuOverviewCollapsed.getStyleClass().add("sidebar-collapsed-btn");
        mnuOverviewCollapsed.setTooltip(new Tooltip("Tổng quan"));
        mnuOverviewCollapsed.setOnAction(e -> {
            setActiveNode(mnuOverviewCollapsed);
            showOverview();
        });
        pnlMenuList.getChildren().add(mnuOverviewCollapsed);

        User.Role role = (user != null) ? user.getRole() : User.Role.ADMIN;
        List<MenuRegistry.MenuGroup> objectMenus = MenuRegistry.getMenusForRole(role);

        for (MenuRegistry.MenuGroup menu : objectMenus) {
            Button mnuGroup = new Button(menu.getTitle().substring(0, 1));
            mnuGroup.getStyleClass().add("sidebar-collapsed-btn");
            mnuGroup.setTooltip(new Tooltip("Đối tượng: " + menu.getTitle()));
            mnuGroup.setOnAction(e -> {
                // Click vào đối tượng ở chế độ thu gọn -> tự bung mở sidebar để chọn chức năng con
                isSidebarCollapsed = false;
                renderSidebar();
            });
            pnlMenuList.getChildren().add(mnuGroup);
        }

        ScrollPane scrMenu = new ScrollPane(pnlMenuList);
        scrMenu.setFitToWidth(true);
        scrMenu.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scrMenu, Priority.ALWAYS);

        pnlCollapsedSidebar.getChildren().addAll(pnlTopBox, scrMenu);
        return pnlCollapsedSidebar;
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
        HBox pnlTopbar = new HBox(16);
        pnlTopbar.getStyleClass().add("topbar");

        HBox pnlSearchBox = new HBox(8);
        pnlSearchBox.setAlignment(Pos.CENTER_LEFT);
        pnlSearchBox.getStyleClass().add("search-box");

        SVGPath searchIcon = new SVGPath();
        searchIcon.setContent("M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z");
        searchIcon.setFill(Color.web("#9ca89e"));
        searchIcon.setScaleX(0.7);
        searchIcon.setScaleY(0.7);

        this.txtSearch = new TextField();
        txtSearch.setPromptText("Tìm kiếm nhanh (Ctrl + K)...");
        txtSearch.getStyleClass().add("search-field");

        Label lblShortcut = new Label("Ctrl K");
        lblShortcut.getStyleClass().add("shortcut-badge");

        pnlSearchBox.getChildren().addAll(searchIcon, txtSearch, lblShortcut);

        Region pnlSpacer = new Region();
        HBox.setHgrow(pnlSpacer, Priority.ALWAYS);

        // Profile Avatar + Thông tin User
        String fullName = (user != null) ? user.getFullName() : "Admin";
        String roleName = (user != null) ? user.getRole().getDisplayName() : "Admin";
        String initials = getInitials(fullName);

        StackPane pnlAvatarCircle = new StackPane();
        Circle avBg = new Circle(18, Color.web("#dcf0dc"));
        Label lblAvatar = new Label(initials);
        lblAvatar.getStyleClass().add("user-badge-avatar");
        pnlAvatarCircle.getChildren().addAll(avBg, lblAvatar);

        VBox pnlUserDetails = new VBox(2);
        Label lblUserName = new Label(fullName);
        lblUserName.getStyleClass().add("topbar-user-name");
        Label lblUserRole = new Label(roleName);
        lblUserRole.getStyleClass().add("topbar-user-role");
        pnlUserDetails.getChildren().addAll(lblUserName, lblUserRole);

        // Menu tài khoản: Đổi mật khẩu & Đăng xuất
        MenuButton mnuProfile = new MenuButton();
        HBox pnlProfileGraphic = new HBox(8, pnlAvatarCircle, pnlUserDetails);
        mnuProfile.setGraphic(pnlProfileGraphic);
        mnuProfile.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

        MenuItem mnuChangePassword = new MenuItem("🔑 Đổi mật khẩu");
        mnuChangePassword.setOnAction(e -> showChangePasswordDialog());

        MenuItem mnuLogout = new MenuItem("🚪 Đăng xuất");
        mnuLogout.setOnAction(e -> logout());

        SeparatorMenuItem mnuSeparator = new SeparatorMenuItem();
        mnuProfile.getItems().addAll(mnuChangePassword, mnuSeparator, mnuLogout);

        pnlTopbar.getChildren().addAll(
                pnlSearchBox,
                pnlSpacer,
                mnuProfile
        );
        return pnlTopbar;
    }

    // =========================================================================
    // 4. ĐIỀU HƯỚNG MÀN HÌNH MODULE & MÀN HÌNH TỔNG QUAN
    // =========================================================================
    private void openModuleScreen(MenuRegistry.MenuGroup menu, MenuRegistry.MenuAction action) {
        // Kiểm tra phân quyền ở tầng Service trước khi mở màn hình
        authService.checkPermission(action.getId());

        // Mỗi thành viên khi hoàn thành View riêng chỉ sửa vào đúng nhánh case của đối tượng mình phụ trách:
        Node targetView = switch (menu.getId()) {
            // --- Nhóm 1: Khách hàng & Thú cưng & Lịch hẹn ---
            case "CUSTOMER"     -> new ModuleListView(menu.getTitle(), action.getName(), action.getShortcut());
            case "PET"          -> new ModuleListView(menu.getTitle(), action.getName(), action.getShortcut());
            case "APPOINTMENT"  -> new ModuleListView(menu.getTitle(), action.getName(), action.getShortcut());

            // --- Nhóm 2: Nhân viên & Ca trực & Dịch vụ chăm sóc ---
            case "EMPLOYEE"     -> new ModuleListView(menu.getTitle(), action.getName(), action.getShortcut());
            case "SHIFT"        -> new ModuleListView(menu.getTitle(), action.getName(), action.getShortcut());
            case "CARE_SERVICE" -> new ModuleListView(menu.getTitle(), action.getName(), action.getShortcut());

            // --- Nhóm 3: Nhà cung cấp & Đơn hàng nhập & Sản phẩm ---
            case "SUPPLIER"     -> new ModuleListView(menu.getTitle(), action.getName(), action.getShortcut());
            case "IMPORT_ORDER" -> new ModuleListView(menu.getTitle(), action.getName(), action.getShortcut());
            case "PRODUCT"      -> new ModuleListView(menu.getTitle(), action.getName(), action.getShortcut());

            // --- Nhóm 4: Khuyến mãi & Hóa đơn & Phiếu đổi trả ---
            case "PROMOTION"    -> new ModuleListView(menu.getTitle(), action.getName(), action.getShortcut());
            case "INVOICE"      -> new ModuleListView(menu.getTitle(), action.getName(), action.getShortcut());
            case "RETURN_NOTE"  -> new ModuleListView(menu.getTitle(), action.getName(), action.getShortcut());

            default -> new ModuleListView(menu.getTitle(), action.getName(), action.getShortcut());
        };

        pnlContentArea.getChildren().setAll(targetView);
    }

    private void showOverview() {
        pnlContentArea.getChildren().setAll(createScrollableOverview());
    }

    private ScrollPane createScrollableOverview() {
        VBox pnlOverviewContent = new VBox(22);
        pnlOverviewContent.getStyleClass().add("dashboard-content");

        HBox pnlWelcomeHeader = new HBox();
        pnlWelcomeHeader.setAlignment(Pos.CENTER_LEFT);

        String empName = (user != null) ? user.getFullName() : "Quản trị viên";
        VBox pnlWelcomeTextBox = new VBox(4);
        Label lblWelcomeTitle = new Label("Xin chào, " + empName);
        lblWelcomeTitle.getStyleClass().add("welcome-title");
        Label lblWelcomeSubtitle = new Label("Hệ thống quản lý cửa hàng thú cưng Happy Tails theo mô hình Hướng Đối Tượng.");
        lblWelcomeSubtitle.getStyleClass().add("welcome-sub");
        pnlWelcomeTextBox.getChildren().addAll(lblWelcomeTitle, lblWelcomeSubtitle);

        Region pnlHeaderSpacer = new Region();
        HBox.setHgrow(pnlHeaderSpacer, Priority.ALWAYS);

        String todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Label lblDate = new Label(todayStr + "  📅");
        lblDate.getStyleClass().add("date-badge");

        pnlWelcomeHeader.getChildren().addAll(pnlWelcomeTextBox, pnlHeaderSpacer, lblDate);

        // Thẻ KPI
        HBox pnlStatsRow = createStatsRow();

        // Hoạt động gần đây
        VBox pnlSummaryPanel = new VBox(14);
        pnlSummaryPanel.getStyleClass().add("chart-panel");
        Label lblRecentActivityTitle = new Label("Hoạt động gần đây");
        lblRecentActivityTitle.getStyleClass().add("panel-title");
        Label lblRecentActivitySubtitle = new Label("Chưa có hoạt động hôm nay.");
        lblRecentActivitySubtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #7d8f82;");
        pnlSummaryPanel.getChildren().addAll(lblRecentActivityTitle, lblRecentActivitySubtitle);

        pnlOverviewContent.getChildren().addAll(pnlWelcomeHeader, pnlStatsRow, pnlSummaryPanel);

        ScrollPane scrOverview = new ScrollPane(pnlOverviewContent);
        scrOverview.setFitToWidth(true);
        scrOverview.setStyle("-fx-background-color: #f3f6f3; -fx-background: #f3f6f3; -fx-border-color: transparent;");
        VBox.setVgrow(scrOverview, Priority.ALWAYS);
        return scrOverview;
    }

    private HBox createStatsRow() {
        HBox pnlStatsCards = new HBox(16);

        // Thẻ 1: Doanh thu hôm nay
        VBox pnlRevenueCard = new VBox(6);
        pnlRevenueCard.getStyleClass().add("stat-card-featured");
        HBox.setHgrow(pnlRevenueCard, Priority.ALWAYS);
        Label lblRevenueTitle = new Label("📈  Doanh thu hôm nay");
        lblRevenueTitle.getStyleClass().add("stat-title-white");
        Label lblRevenueValue = new Label("0 đ");
        lblRevenueValue.getStyleClass().add("stat-value-white");
        Label lblRevenueDescription = new Label("Tổng thanh toán trong ngày");
        lblRevenueDescription.getStyleClass().add("stat-sub-white");
        pnlRevenueCard.getChildren().addAll(lblRevenueTitle, lblRevenueValue, lblRevenueDescription);

        // Thẻ 2: Lịch hẹn
        VBox pnlAppointmentCard = new VBox(6);
        pnlAppointmentCard.getStyleClass().add("stat-card");
        HBox.setHgrow(pnlAppointmentCard, Priority.ALWAYS);
        Label lblAppointmentTitle = new Label("📅  Lịch hẹn hôm nay");
        lblAppointmentTitle.getStyleClass().add("stat-title");
        Label lblAppointmentValue = new Label("0");
        lblAppointmentValue.getStyleClass().add("stat-value");
        Label lblAppointmentDescription = new Label("Lịch hẹn cần phục vụ");
        lblAppointmentDescription.getStyleClass().add("stat-sub");
        pnlAppointmentCard.getChildren().addAll(lblAppointmentTitle, lblAppointmentValue, lblAppointmentDescription);

        // Thẻ 3: Hết hàng (hiển thị '0' thay vì '00')
        VBox pnlStockCard = new VBox(6);
        pnlStockCard.getStyleClass().add("stat-card");
        HBox.setHgrow(pnlStockCard, Priority.ALWAYS);
        Label lblStockTitle = new Label("⚠  Sản phẩm sắp hết hàng");
        lblStockTitle.getStyleClass().add("stat-title");
        Label lblStockValue = new Label("0");
        lblStockValue.getStyleClass().add("stat-value");
        Label lblStockDescription = new Label("Tồn kho dưới ngưỡng an toàn");
        lblStockDescription.getStyleClass().add("stat-sub");
        pnlStockCard.getChildren().addAll(lblStockTitle, lblStockValue, lblStockDescription);

        pnlStatsCards.getChildren().addAll(pnlRevenueCard, pnlAppointmentCard, pnlStockCard);
        return pnlStatsCards;
    }

    // =========================================================================
    // 5. TIỆN ÍCH TÀI KHOẢN
    // =========================================================================
    private void showChangePasswordDialog() {
        Dialog<ButtonType> dlgChangePassword = new Dialog<>();
        dlgChangePassword.setTitle("Đổi mật khẩu");
        dlgChangePassword.setHeaderText("Cập nhật mật khẩu tài khoản");

        VBox pnlPasswordForm = new VBox(10);
        pnlPasswordForm.setPadding(new Insets(20));

        PasswordField pwdCurrent = new PasswordField();
        pwdCurrent.setPromptText("Mật khẩu hiện tại");

        PasswordField pwdNew = new PasswordField();
        pwdNew.setPromptText("Mật khẩu mới");

        PasswordField pwdConfirm = new PasswordField();
        pwdConfirm.setPromptText("Xác nhận mật khẩu mới");

        Label lblCurrentPassword = new Label("Mật khẩu hiện tại:");
        Label lblNewPassword = new Label("Mật khẩu mới:");
        Label lblConfirmPassword = new Label("Xác nhận mật khẩu:");
        pnlPasswordForm.getChildren().addAll(
                lblCurrentPassword, pwdCurrent,
                lblNewPassword, pwdNew,
                lblConfirmPassword, pwdConfirm
        );

        dlgChangePassword.getDialogPane().setContent(pnlPasswordForm);
        dlgChangePassword.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dlgChangePassword.showAndWait();
    }

    private void logout() {
        authService.logout();
        stage.setTitle("Happy Tails | Đăng nhập hệ thống");
        Scene scene = new Scene(new LoginView(stage), 600, 680);
        scene.getStylesheets().add(getClass().getResource("/com/petshop/view/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    /**
     * Tạo icon logo Happy Tails từ file logo.png chuẩn nhận diện thương hiệu.
     */
    private Node createBrandLogo(double size) {
        try {
            var stream = getClass().getResourceAsStream("/com/petshop/view/logo.png");
            if (stream != null) {
                ImageView iv = new ImageView(new Image(stream));
                iv.setFitWidth(size);
                iv.setFitHeight(size);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
                return iv;
            }
        } catch (Exception ignored) {}

        StackPane badge = new StackPane();
        badge.setStyle("-fx-background-color: #f97316; -fx-background-radius: 8px;");
        badge.setMinSize(size, size);
        badge.setPrefSize(size, size);
        badge.setMaxSize(size, size);
        return badge;
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "HT";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[parts.length - 2].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}
