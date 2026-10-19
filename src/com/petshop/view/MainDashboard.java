package com.petshop.view;

import com.petshop.model.User;
import com.petshop.service.AuthService;
import com.petshop.service.MenuRegistry;
import com.petshop.view.customer.CustomerView;
import com.petshop.view.employee.EmployeeView;
import com.petshop.view.pet.PetView;
import com.petshop.view.product.ProductView;
import com.petshop.view.promotion.PromotionSearchView;
import com.petshop.view.promotion.PromotionUpdateView;
import com.petshop.view.returnnote.ReturnNoteCreateView;
import com.petshop.view.returnnote.ReturnNoteProcessView;
import com.petshop.view.shift.ShiftAssignmentView;
import com.petshop.view.shift.ShiftCalendarView;
import com.petshop.view.shift.ShiftUpdateView;
import com.petshop.view.supplier.SupplierView;
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
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
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

    // Thu gọn sidebar
    private boolean isSidebarCollapsed = false;
    private StackPane pnlSidebarContainer;

    // Highlight mục đang chọn
    private Node currentActiveItemNode;
    private Button overviewButton;
    private String activeMenuId;
    private String activeActionId;
    private Label lblBreadcrumb;
    private HBox pnlSearchBox;
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
                pnlContentArea);

        setCenter(pnlMainContainer);

        // Giữ phím tắt tìm nhanh của demo; ô tìm kiếm chỉ hiện khi người dùng gọi.
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
                    if (event.isControlDown() && event.getCode() == javafx.scene.input.KeyCode.K) {
                        pnlSearchBox.setVisible(true);
                        pnlSearchBox.setManaged(true);
                        txtSearch.requestFocus();
                        txtSearch.selectAll();
                        event.consume();
                    } else if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE
                            && pnlSearchBox.isVisible()) {
                        pnlSearchBox.setVisible(false);
                        pnlSearchBox.setManaged(false);
                        event.consume();
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

        currentActiveItemNode = null;
        allSubMenus.clear();
        allArrows.clear();

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
        Button btnOverview = new Button("Tổng quan");
        btnOverview.setGraphic(createMenuIcon("OVERVIEW", 18));
        btnOverview.setGraphicTextGap(10);
        overviewButton = btnOverview;
        btnOverview.setMaxWidth(Double.MAX_VALUE);
        btnOverview.getStyleClass().add(activeActionId == null ? "sidebar-btn-active" : "sidebar-btn");
        btnOverview.setTooltip(new Tooltip("Trang chủ bảng điều khiển"));
        if (activeActionId == null) {
            currentActiveItemNode = btnOverview;
        }
        btnOverview.setOnAction(e -> {
            collapseAllAccordions();
            showOverview();
        });
        pnlMenuList.getChildren().add(btnOverview);

        // Dựng 11 Menu Đối Tượng từ MenuRegistry
        User.Role role = (user != null) ? user.getRole() : User.Role.ADMIN;
        List<MenuRegistry.MenuGroup> objectMenus = MenuRegistry.getMenusForRole(role);

        for (MenuRegistry.MenuGroup menu : objectMenus) {
            VBox pnlAccordionItem = createAccordionItem(menu);
            pnlMenuList.getChildren().add(pnlAccordionItem);
        }

        ScrollPane scrMenu = new ScrollPane(pnlMenuList);
        scrMenu.setFitToWidth(true);
        scrMenu.setStyle(
                "-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scrMenu, Priority.ALWAYS);

        pnlExpandedSidebar.getChildren().addAll(pnlBrandBox, scrMenu);
        return pnlExpandedSidebar;
    }

    private VBox createAccordionItem(MenuRegistry.MenuGroup menu) {
        VBox pnlAccordionItem = new VBox(2);
        pnlAccordionItem.setPadding(new Insets(2, 0, 2, 0));

        HBox mnuGroupHeader = new HBox(8);
        mnuGroupHeader.setAlignment(Pos.CENTER_LEFT);
        mnuGroupHeader.getStyleClass().add("sidebar-nav-item");

        Node menuIcon = createMenuIcon(menu.getIconType(), 18);

        Label lblTitle = new Label(menu.getTitle());
        lblTitle.getStyleClass().add("sidebar-nav-text");
        lblTitle.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(lblTitle, Priority.ALWAYS);

        Tooltip tooltip = new Tooltip("Đối tượng: " + menu.getTitle());
        Tooltip.install(mnuGroupHeader, tooltip);

        Label lblArrow = new Label("▸");
        lblArrow.getStyleClass().add("sidebar-nav-arrow");
        allArrows.add(lblArrow);

        mnuGroupHeader.getChildren().addAll(menuIcon, lblTitle, lblArrow);

        // Danh sách chức năng con
        VBox pnlSubMenuList = new VBox(2);
        boolean hasActiveAction = false;
        for (MenuRegistry.MenuAction action : menu.getActions()) {
            Button btnAction = new Button(action.getName());
            btnAction.setMaxWidth(Double.MAX_VALUE);
            btnAction.getStyleClass().add("sidebar-sub-btn");
            if (action.getId().equals(activeActionId)) {
                btnAction.getStyleClass().remove("sidebar-sub-btn");
                btnAction.getStyleClass().add("sidebar-sub-btn-active");
                currentActiveItemNode = btnAction;
                hasActiveAction = true;
            }
            if (action.getShortcut() != null) {
                btnAction.setTooltip(new Tooltip(action.getName() + " (" + action.getShortcut() + ")"));
            } else {
                btnAction.setTooltip(new Tooltip(action.getName()));
            }

            btnAction.setOnAction(e -> {
                activeMenuId = menu.getId();
                activeActionId = action.getId();
                setActiveNode(btnAction);
                openModuleScreen(menu, action);
            });
            pnlSubMenuList.getChildren().add(btnAction);
        }

        pnlSubMenuList.setVisible(hasActiveAction);
        pnlSubMenuList.setManaged(hasActiveAction);
        if (hasActiveAction) {
            lblArrow.setText("▾");
        }
        allSubMenus.add(pnlSubMenuList);

        // Accordion: chỉ mở 1 đối tượng tại một thời điểm
        mnuGroupHeader.setOnMouseClicked(e -> {
            boolean isCurrentlyOpen = pnlSubMenuList.isVisible();
            collapseAllAccordions();
            if (!isCurrentlyOpen) {
                pnlSubMenuList.setVisible(true);
                pnlSubMenuList.setManaged(true);
                lblArrow.setText("▾");
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
        }
    }

    // =========================================================================
    // 2. SIDEBAR THU GỌN (COLLAPSED)
    // =========================================================================
    private VBox createCollapsedSidebar() {
        VBox pnlCollapsedSidebar = new VBox();
        pnlCollapsedSidebar.getStyleClass().add("sidebar-collapsed");
        pnlCollapsedSidebar.setAlignment(Pos.TOP_CENTER);
        currentActiveItemNode = null;

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
        Button btnOverviewCollapsed = new Button();
        btnOverviewCollapsed.setGraphic(createMenuIcon("OVERVIEW", 20));
        overviewButton = btnOverviewCollapsed;
        btnOverviewCollapsed.getStyleClass().add(activeActionId == null
            ? "sidebar-collapsed-btn-active" : "sidebar-collapsed-btn");
        if (activeActionId == null) {
            currentActiveItemNode = btnOverviewCollapsed;
        }
        btnOverviewCollapsed.setTooltip(new Tooltip("Tổng quan"));
        btnOverviewCollapsed.setOnAction(e -> {
            showOverview();
        });
        pnlMenuList.getChildren().add(btnOverviewCollapsed);

        User.Role role = (user != null) ? user.getRole() : User.Role.ADMIN;
        List<MenuRegistry.MenuGroup> objectMenus = MenuRegistry.getMenusForRole(role);

        for (MenuRegistry.MenuGroup menu : objectMenus) {
            Button btnGroup = new Button();
            btnGroup.setGraphic(createMenuIcon(menu.getIconType(), 20));
            boolean isActiveGroup = menu.getId().equals(activeMenuId);
            btnGroup.getStyleClass().add(isActiveGroup
                    ? "sidebar-collapsed-btn-active" : "sidebar-collapsed-btn");
            if (isActiveGroup) {
                currentActiveItemNode = btnGroup;
            }
            btnGroup.setTooltip(new Tooltip("Đối tượng: " + menu.getTitle()));
            btnGroup.setOnAction(e -> {
                // Click vào đối tượng ở chế độ thu gọn -> tự bung mở sidebar để chọn chức năng
                // con
                isSidebarCollapsed = false;
                renderSidebar();
            });
            pnlMenuList.getChildren().add(btnGroup);
        }

        ScrollPane scrMenu = new ScrollPane(pnlMenuList);
        scrMenu.setFitToWidth(true);
        scrMenu.setStyle(
                "-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scrMenu, Priority.ALWAYS);

        pnlCollapsedSidebar.getChildren().addAll(pnlTopBox, scrMenu);
        return pnlCollapsedSidebar;
    }

    private void setActiveNode(Node node) {
        if (currentActiveItemNode != null) {
            var previousClasses = currentActiveItemNode.getStyleClass();
            boolean wasCollapsedButton = previousClasses.contains("sidebar-collapsed-btn")
                    || previousClasses.contains("sidebar-collapsed-btn-active");
            boolean wasOverviewButton = previousClasses.contains("sidebar-btn")
                    || previousClasses.contains("sidebar-btn-active");
            previousClasses.removeAll("sidebar-btn-active", "sidebar-sub-btn-active", "sidebar-collapsed-btn-active");
            previousClasses.removeAll("sidebar-btn", "sidebar-sub-btn", "sidebar-collapsed-btn");
            if (wasCollapsedButton) {
                previousClasses.add("sidebar-collapsed-btn");
            } else if (wasOverviewButton) {
                previousClasses.add("sidebar-btn");
            } else {
                previousClasses.add("sidebar-sub-btn");
            }
        }
        this.currentActiveItemNode = node;
        var activeClasses = node.getStyleClass();
        activeClasses.removeAll("sidebar-btn", "sidebar-sub-btn", "sidebar-collapsed-btn",
                "sidebar-btn-active", "sidebar-sub-btn-active", "sidebar-collapsed-btn-active");
        if (node == overviewButton) {
            activeClasses.add(isSidebarCollapsed ? "sidebar-collapsed-btn-active" : "sidebar-btn-active");
        } else if (isSidebarCollapsed) {
            activeClasses.add("sidebar-collapsed-btn-active");
        } else {
            activeClasses.add("sidebar-sub-btn-active");
        }
    }

    // =========================================================================
    // 3. TOPBAR (HEADER DÙNG CHUNG)
    // =========================================================================
    private HBox createTopbar() {
        HBox pnlTopbar = new HBox(16);
        pnlTopbar.getStyleClass().add("topbar");

        String fullName = (user != null) ? user.getFullName() : "Admin";
        String roleName = getCurrentRoleName();

        lblBreadcrumb = new Label(roleName + " / Tổng quan");
        lblBreadcrumb.getStyleClass().add("topbar-breadcrumb");

    // Ô tìm nhanh giữ nguyên chức năng demo nhưng ẩn để khớp topbar mẫu.
    pnlSearchBox = new HBox(8);
    pnlSearchBox.setAlignment(Pos.CENTER_LEFT);
    pnlSearchBox.getStyleClass().add("search-box");

        SVGPath searchIcon = new SVGPath();
        searchIcon.setContent(
                "M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z");
        searchIcon.setFill(Color.web("#9ca89e"));
        searchIcon.setScaleX(0.7);
        searchIcon.setScaleY(0.7);

        txtSearch = new TextField();
        txtSearch.setPromptText("Tìm kiếm nhanh (Ctrl + K)...");
        txtSearch.getStyleClass().add("search-field");

        Label lblShortcut = new Label("Ctrl K");
        lblShortcut.getStyleClass().add("shortcut-badge");

        pnlSearchBox.getChildren().addAll(searchIcon, txtSearch, lblShortcut);
        pnlSearchBox.setVisible(false);
        pnlSearchBox.setManaged(false);

        Region pnlSpacer = new Region();
        HBox.setHgrow(pnlSpacer, Priority.ALWAYS);

        // Profile Avatar + Thông tin User
        String initials = getInitials(fullName);

        StackPane pnlAvatarCircle = new StackPane();
        Circle avBg = new Circle(18, Color.web("#f97316"));
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
        MenuButton btnProfile = new MenuButton();
        btnProfile.getStyleClass().add("topbar-profile");
        HBox pnlProfileGraphic = new HBox(8, pnlAvatarCircle, pnlUserDetails);
        btnProfile.setGraphic(pnlProfileGraphic);
        btnProfile.setTooltip(new Tooltip("Tài khoản và đăng xuất"));

        MenuItem mnuChangePassword = new MenuItem("🔑 Đổi mật khẩu");
        mnuChangePassword.setOnAction(e -> showChangePasswordDialog());

        MenuItem mnuLogout = new MenuItem("🚪 Đăng xuất");
        mnuLogout.setOnAction(e -> logout());

        SeparatorMenuItem mnuSeparator = new SeparatorMenuItem();
        btnProfile.getItems().addAll(mnuChangePassword, mnuSeparator, mnuLogout);

        pnlTopbar.getChildren().addAll(
            lblBreadcrumb,
                pnlSearchBox,
                pnlSpacer,
                btnProfile);
        return pnlTopbar;
    }

    // =========================================================================
    // 4. ĐIỀU HƯỚNG MÀN HÌNH MODULE & MÀN HÌNH TỔNG QUAN
    // =========================================================================
    private void openModuleScreen(MenuRegistry.MenuGroup menu, MenuRegistry.MenuAction action) {
        // Kiểm tra phân quyền ở tầng Service trước khi mở màn hình
        authService.checkPermission(action.getId());

        // Mỗi thành viên khi hoàn thành View riêng chỉ sửa vào đúng nhánh case của đối
        // tượng mình phụ trách:
        Node targetView = switch (menu.getId()) {
            // --- Nhóm 1: Khách hàng & Thú cưng & Lịch hẹn ---
            case "CUSTOMER" -> new CustomerView(action.getId());
            case "PET" -> new PetView(action.getId());
            case "APPOINTMENT" -> new AssignedModuleView(action.getId(), action.getName(), user);

            // --- Nhóm 2: Nhân viên & Ca trực & Dịch vụ chăm sóc ---
            // Mở giao diện nhân viên theo chức năng được chọn.
            case "EMPLOYEE" -> new EmployeeView(action.getId());
            case "SHIFT" -> switch (action.getId()) {
                case "SHF_UPDATE" -> new ShiftUpdateView();
                case "SHF_ASSIGN" -> new ShiftAssignmentView();
                case "SHF_VIEW"   -> new ShiftCalendarView();
                default -> new ModuleListView(menu.getTitle(), action.getName(), action.getShortcut());
            };
            case "CARE_SERVICE" -> new AssignedModuleView(action.getId(), action.getName(), user);

            // --- Nhóm 3: Nhà cung cấp & Đơn hàng nhập & Sản phẩm ---
            case "SUPPLIER" -> new SupplierView(action.getId());
            case "IMPORT_ORDER" -> new ModuleListView(menu.getTitle(), action.getName(), action.getShortcut());
            case "PRODUCT" -> new ProductView(action.getId());

            // --- Nhóm 4: Khuyến mãi & Hóa đơn & Phiếu đổi trả ---
            case "PROMOTION" -> "PRM_SEARCH".equals(action.getId())
                    ? new PromotionSearchView()
                    : new PromotionUpdateView();
            case "INVOICE" -> new AssignedModuleView(action.getId(), action.getName(), user);
            case "RETURN_NOTE" -> "RET_CREATE".equals(action.getId())
                    ? new ReturnNoteCreateView()
                    : new ReturnNoteProcessView();

            default -> new ModuleListView(menu.getTitle(), action.getName(), action.getShortcut());
        };

        lblBreadcrumb.setText(getCurrentRoleName() + " / " + menu.getTitle() + " / " + action.getName());
        pnlContentArea.getChildren().setAll(targetView);
    }

    private void showOverview() {
        activeMenuId = null;
        activeActionId = null;
        if (lblBreadcrumb != null) {
            lblBreadcrumb.setText(getCurrentRoleName() + " / Tổng quan");
        }
        if (overviewButton != null) {
            setActiveNode(overviewButton);
        }
        pnlContentArea.getChildren().setAll(createScrollableOverview());
    }

    private String getCurrentRoleName() {
        return (user != null && user.getRole() != null) ? user.getRole().getDisplayName() : "Admin";
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
        Label lblWelcomeSubtitle = new Label(
                "Hệ thống quản lý cửa hàng thú cưng Happy Tails theo mô hình Hướng Đối Tượng.");
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
                lblConfirmPassword, pwdConfirm);

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

    private Node createMenuIcon(String iconType, double size) {
        String path = switch (iconType) {
            case "OVERVIEW" -> "M3 3h7v7H3z M14 3h7v7h-7z M14 14h7v7h-7z M3 14h7v7H3z";
            case "CUSTOMER" -> "M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1.1-1.1a5.5 5.5 0 0 0-7.8 7.8l1.1 1.1L12 21l7.8-7.5 1.1-1.1a5.5 5.5 0 0 0-.1-7.8z";
            case "PET" -> "M8 10a2 2 0 1 0 0-4 2 2 0 0 0 0 4z M16 10a2 2 0 1 0 0-4 2 2 0 0 0 0 4z M5 15a2 2 0 1 0 0-4 2 2 0 0 0 0 4z M19 15a2 2 0 1 0 0-4 2 2 0 0 0 0 4z M12 12c-3 0-6 4-6 6.5A2.5 2.5 0 0 0 8.5 21c1.3 0 2.2-.8 3.5-.8s2.2.8 3.5.8a2.5 2.5 0 0 0 2.5-2.5C18 16 15 12 12 12z";
            case "APPOINTMENT", "SHIFT" -> "M8 2v4 M16 2v4 M3 10h18 M5 4h14a2 2 0 0 1 2 2v14H3V6a2 2 0 0 1 2-2z M8 14h3 M8 18h3";
            case "EMPLOYEE" -> "M16 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2 M10 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z M20 21v-2a4 4 0 0 0-3-3.87 M16 3.13a4 4 0 0 1 0 7.75";
            case "SUPPLIER", "IMPORT_ORDER" -> "m12 3 9 5-9 5-9-5 9-5z M3 8v8l9 5 9-5V8 M12 13v8 M7.5 5.5l9 5";
            case "PRODUCT" -> "M3 3h2l2.4 12.4A2 2 0 0 0 9.4 17H19a2 2 0 0 0 2-1.6L23 8H6 M10 21a1 1 0 1 0 0-2 1 1 0 0 0 0 2z M18 21a1 1 0 1 0 0-2 1 1 0 0 0 0 2z";
            case "CARE_SERVICE" -> "M6 3a3 3 0 0 0 0 6h2l10 12a2 2 0 0 0 3-3L11 8 M8 9l3-3 M18 5l-6 6";
            case "PROMOTION" -> "M20.6 13.4 12 22l-9-9V3h10l7.6 7.6a2 2 0 0 1 0 2.8z M7.5 7.5h.01";
            case "INVOICE" -> "M6 2h12v20l-3-2-3 2-3-2-3 2V2z M9 7h6 M9 11h6 M9 15h3";
            case "RETURN_NOTE" -> "M3 3v18h18 M8 16v-5 M13 16V6 M18 16v-8";
            default -> "M4 4h16v16H4z M8 8h8v8H8z";
        };

        SVGPath vector = new SVGPath();
        vector.setContent(path);
        vector.setFill(Color.TRANSPARENT);
        vector.setStroke(Color.web("#98a2b3"));
        vector.setStrokeWidth(1.8);
        vector.setStrokeLineCap(StrokeLineCap.ROUND);
        vector.setStrokeLineJoin(StrokeLineJoin.ROUND);
        vector.setScaleX(size / 24.0);
        vector.setScaleY(size / 24.0);

        StackPane icon = new StackPane(vector);
        icon.setMinSize(size, size);
        icon.setPrefSize(size, size);
        icon.setMaxSize(size, size);
        icon.getStyleClass().add("sidebar-nav-icon");
        return icon;
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
        if (name == null || name.isBlank())
            return "HT";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1)
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[parts.length - 2].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}
