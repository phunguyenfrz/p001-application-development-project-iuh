package com.petshop.view;

import com.petshop.model.Invoice;
import com.petshop.model.User;
import com.petshop.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Giao diện Bảng điều khiển (Dashboard) chuẩn theo thiết kế Happy Tails.
 */
public class MainDashboard extends BorderPane {

    private final Stage stage;
    private final User user;

    public MainDashboard(Stage stage) {
        this.stage = stage;
        this.user = SessionManager.getCurrentUser();

        getStyleClass().add("dashboard");

        // 1. Sidebar bên trái
        setLeft(createSidebar());

        // 2. Nội dung chính (Topbar + Scrollable Body)
        VBox mainContainer = new VBox();
        HBox.setHgrow(mainContainer, Priority.ALWAYS);

        mainContainer.getChildren().addAll(
                createTopbar(),
                createScrollableBody()
        );

        setCenter(mainContainer);
    }

    // =========================================================================
    // 1. SIDEBAR (CỘT ĐIỀU HƯỚNG TRÁI)
    // =========================================================================
    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");

        // Brand Logo + Tên + Nút thu gọn
        HBox brandBox = new HBox(10);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        brandBox.getStyleClass().add("sidebar-brand-box");

        // Logo tròn màu cam
        StackPane logoCircle = new StackPane();
        Circle circle = new Circle(17, Color.web("#d97736"));
        Group paw = createMiniPawWhite();
        logoCircle.getChildren().addAll(circle, paw);

        VBox brandText = new VBox(1);
        Label title = new Label("Happy Tails");
        title.getStyleClass().add("sidebar-title");
        Label subtitle = new Label("Cửa hàng thú cưng");
        subtitle.getStyleClass().add("sidebar-subtitle");
        brandText.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnCollapse = new Button("«");
        btnCollapse.getStyleClass().add("btn-collapse");

        brandBox.getChildren().addAll(logoCircle, brandText, spacer, btnCollapse);

        // Menu items
        VBox menuList = new VBox(4);
        menuList.setPadding(new Insets(10, 12, 10, 12));

        // Nút "Tổng quan" (Active)
        Button btnOverview = createMenuButton("❖  Tổng quan", true);
        menuList.getChildren().add(btnOverview);

        User.Role role = user != null ? user.getRole() : User.Role.QUAN_LY;

        if (role == User.Role.QUAN_LY) {
            // ScreenFlow Quản lý (02A):
            // - Nhân viên (03): Danh sách nhân viên, Thêm/Sửa nhân viên, Chi tiết nhân viên
            VBox fnEmployees = createNavDropdownItem("Nhân viên",
                    createSubMenuButton("Danh sách nhân viên"),
                    createSubMenuButton("Thêm nhân viên mới")
            );
            // - Ca trực (06): Lịch ca trực, Thêm/Điều chỉnh ca
            VBox fnShifts = createNavDropdownItem("Ca trực",
                    createSubMenuButton("Bảng phân ca trực"),
                    createSubMenuButton("Thêm / Gán ca trực")
            );
            // - Nhà cung cấp (08): Danh sách nhà cung cấp, Thêm/Sửa nhà cung cấp
            VBox fnSuppliers = createNavDropdownItem("Nhà cung cấp",
                    createSubMenuButton("Danh sách đối tác NCC"),
                    createSubMenuButton("Thêm nhà cung cấp")
            );
            // - Dịch vụ (10): Danh mục dịch vụ, Thêm/Sửa gói & bảng giá
            VBox fnServices = createNavDropdownItem("Dịch vụ",
                    createSubMenuButton("Danh mục dịch vụ Spa"),
                    createSubMenuButton("Thêm gói / Bảng giá")
            );
            // - Khuyến mãi (12): Danh sách khuyến mãi, Tạo mã voucher/khuyến mãi
            VBox fnPromos = createNavDropdownItem("Khuyến mãi",
                    createSubMenuButton("Chương trình ưu đãi"),
                    createSubMenuButton("Tạo khuyến mãi mới")
            );
            // - Báo cáo (14): Thống kê doanh thu, Xuất báo cáo
            VBox fnReports = createNavDropdownItem("Báo cáo",
                    createSubMenuButton("Thống kê doanh thu"),
                    createSubMenuButton("Báo cáo dịch vụ & hàng hóa")
            );

            menuList.getChildren().addAll(fnEmployees, fnShifts, fnSuppliers, fnServices, fnPromos, fnReports);

        } else if (role == User.Role.BAN_HANG) {
            // ScreenFlow Bán hàng (02B):
            // - Bán hàng POS (24): Màn hình lập hóa đơn, Tính tiền tại quầy
            VBox fnPos = createNavDropdownItem("Bán hàng (POS)",
                    createSubMenuButton("Lập hóa đơn mới"),
                    createSubMenuButton("Quét mã vạch bán lẻ")
            );
            // - Hóa đơn (25): Danh sách hóa đơn, Tra cứu & Đổi trả
            VBox fnInvoices = createNavDropdownItem("Hóa đơn",
                    createSubMenuButton("Lịch sử hóa đơn"),
                    createSubMenuButton("Phiếu đổi trả hàng")
            );
            // - Khách hàng (21): Danh sách khách hàng, Thêm/Sửa khách hàng, Hồ sơ khách hàng
            VBox fnCustomers = createNavDropdownItem("Khách hàng",
                    createSubMenuButton("Tra cứu khách hàng"),
                    createSubMenuButton("Thêm khách hàng mới")
            );
            // - Sản phẩm (15): Danh mục sản phẩm, Thêm/Sửa sản phẩm
            VBox fnProducts = createNavDropdownItem("Sản phẩm",
                    createSubMenuButton("Danh mục sản phẩm"),
                    createSubMenuButton("Thêm sản phẩm mới")
            );
            // - Tồn kho (17): Tồn kho sản phẩm, Cảnh báo sắp hết hàng
            VBox fnInventory = createNavDropdownItem("Tồn kho",
                    createSubMenuButton("Kiểm kê tồn kho"),
                    createSubMenuButton("Cảnh báo chạm ngưỡng")
            );
            // - Đơn hàng nhập (18): Danh sách đơn nhập, Lập đơn nhập kho
            VBox fnImports = createNavDropdownItem("Đơn hàng nhập",
                    createSubMenuButton("Danh sách đơn nhập"),
                    createSubMenuButton("Lập đơn nhập kho mới")
            );

            menuList.getChildren().addAll(fnPos, fnInvoices, fnCustomers, fnProducts, fnInventory, fnImports);

        } else if (role == User.Role.CHAM_SOC) {
            // ScreenFlow Chăm sóc (02C):
            // - Lịch hẹn (26): Danh sách lịch hẹn, Đặt lịch mới, Chi tiết ca chăm sóc
            VBox fnAppointments = createNavDropdownItem("Lịch hẹn",
                    createSubMenuButton("Lịch chăm sóc hôm nay"),
                    createSubMenuButton("Đặt lịch hẹn mới")
            );
            // - Thú cưng (29): Hồ sơ thú cưng, Thêm/Sửa thú cưng, Lịch sử y tế
            VBox fnPets = createNavDropdownItem("Thú cưng",
                    createSubMenuButton("Hồ sơ thú cưng"),
                    createSubMenuButton("Đăng ký thú cưng mới")
            );
            // - Thanh toán (24): Chuyển ca hoàn thành sang POS
            VBox fnPos = createNavDropdownItem("Thanh toán (POS)",
                    createSubMenuButton("Thanh toán ca hoàn tất")
            );

            menuList.getChildren().addAll(fnAppointments, fnPets, fnPos);
        }

        ScrollPane menuScroll = new ScrollPane(menuList);
        menuScroll.setFitToWidth(true);
        menuScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(menuScroll, Priority.ALWAYS);

        // Footer Branch Card ở đáy sidebar
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

    /**
     * Tạo mục chức năng to ở ngoài Sidebar, khi click sẽ toggle sổ xuống các chức năng con.
     * Mặc định ban đầu ở trạng thái đóng (collapsed).
     */
    private VBox createNavDropdownItem(String title, Button... subButtons) {
        VBox itemBox = new VBox(2);
        itemBox.setPadding(new Insets(2, 0, 2, 0));

        // Thanh chức năng to ở ngoài (Icon tròn rỗng/đầy + Tên chức năng + Mũi tên đóng/mở)
        HBox headerBtn = new HBox(10);
        headerBtn.setAlignment(Pos.CENTER_LEFT);
        headerBtn.getStyleClass().add("sidebar-nav-item");

        Label bullet = new Label("○");
        bullet.getStyleClass().add("sidebar-nav-bullet");

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("sidebar-nav-text");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Ban đầu đóng -> mũi tên trỏ sang phải
        Label lblArrow = new Label("▸");
        lblArrow.getStyleClass().add("sidebar-nav-arrow");

        headerBtn.getChildren().addAll(bullet, lblTitle, spacer, lblArrow);

        // Danh sách các chức năng con bên trong (mặc định đóng, ẩn)
        VBox subMenuList = new VBox(2);
        subMenuList.getChildren().addAll(subButtons);
        subMenuList.setVisible(false);
        subMenuList.setManaged(false);

        // Sự kiện click toggle
        headerBtn.setOnMouseClicked(e -> {
            boolean isVisible = !subMenuList.isVisible();
            subMenuList.setVisible(isVisible);
            subMenuList.setManaged(isVisible);
            lblArrow.setText(isVisible ? "▾" : "▸");
            bullet.setText(isVisible ? "●" : "○");
        });

        itemBox.getChildren().addAll(headerBtn, subMenuList);
        return itemBox;
    }

    private Button createMenuButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add(active ? "sidebar-btn-active" : "sidebar-btn");
        if (!active) {
            btn.setOnAction(e -> showPlaceholder(text));
        }
        return btn;
    }

    private Button createSubMenuButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("sidebar-sub-btn");
        btn.setOnAction(e -> showPlaceholder(text));
        return btn;
    }

    // =========================================================================
    // 2. TOPBAR (THANH TÌM KIẾM + PROFILE)
    // =========================================================================
    private HBox createTopbar() {
        HBox topbar = new HBox(16);
        topbar.getStyleClass().add("topbar");

        // Thanh tìm kiếm nhanh
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

        Label badgeK = new Label("⌘ K");
        badgeK.getStyleClass().add("shortcut-badge");

        searchBox.getChildren().addAll(searchIcon, searchField, badgeK);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Các nút icon phụ trợ: ?, Mail, Bell
        Button btnHelp = createIconButton("?");
        Button btnMail = createIconButton("✉");
        Button btnBell = createIconButton("🔔");

        // Profile Avatar + Thông tin User
        String fullName = (user != null) ? user.getFullName() : "Nhân viên";
        String roleName = (user != null) ? user.getRole().getDisplayName() : "Bán hàng";
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

        // Bấm vào profile để Đăng xuất
        MenuButton profileMenu = new MenuButton();
        profileMenu.setGraphic(new HBox(8, avatarCircle, userDetails));
        profileMenu.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        MenuItem logoutItem = new MenuItem("Đăng xuất");
        logoutItem.setOnAction(e -> logout());
        profileMenu.getItems().add(logoutItem);

        topbar.getChildren().addAll(
                searchBox,
                spacer,
                btnHelp, btnMail, btnBell,
                profileMenu
        );
        return topbar;
    }

    private Button createIconButton(String icon) {
        Button btn = new Button(icon);
        btn.getStyleClass().add("icon-btn");
        return btn;
    }

    // =========================================================================
    // 3. MAIN BODY (WELCOME + STATS + CHARTS + TABLE)
    // =========================================================================
    private ScrollPane createScrollableBody() {
        VBox content = new VBox(22);
        content.getStyleClass().add("dashboard-content");

        // Header: Welcome back, [Tên] + Ngày tháng
        HBox welcomeHeader = new HBox();
        welcomeHeader.setAlignment(Pos.CENTER_LEFT);

        String empName = (user != null) ? user.getFullName() : "Ngọc Linh";
        VBox welcomeTextBox = new VBox(4);
        Label welcomeTitle = new Label("Welcome back, " + empName);
        welcomeTitle.getStyleClass().add("welcome-title");
        Label welcomeSub = new Label("Mọi điều tốt đẹp bắt đầu từ một chiếc đuôi hạnh phúc.");
        welcomeSub.getStyleClass().add("welcome-sub");
        welcomeTextBox.getChildren().addAll(welcomeTitle, welcomeSub);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        // Badge ngày
        String todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Label dateBadge = new Label(todayStr + "  📅");
        dateBadge.getStyleClass().add("date-badge");

        welcomeHeader.getChildren().addAll(welcomeTextBox, headerSpacer, dateBadge);

        // 4 Thẻ Stat Cards
        HBox statsRow = createStatsRow();

        // Khu vực giữa: Biểu đồ doanh thu (trái) + Lịch hẹn sắp tới (phải)
        HBox middleRow = new HBox(20);
        VBox chartPanel = createRevenueChartPanel();
        VBox appointmentPanel = createUpcomingAppointmentsPanel();
        HBox.setHgrow(chartPanel, Priority.ALWAYS);
        appointmentPanel.setPrefWidth(350);
        middleRow.getChildren().addAll(chartPanel, appointmentPanel);

        // Khu vực dưới: Hóa đơn gần đây (Bảng dữ liệu)
        VBox invoicePanel = createRecentInvoicesPanel();

        content.getChildren().addAll(
                welcomeHeader,
                statsRow,
                middleRow,
                invoicePanel
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f3f6f3; -fx-background: #f3f6f3; -fx-border-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        return scrollPane;
    }

    /**
     * Hàng 4 thẻ thống kê
     */
    private HBox createStatsRow() {
        HBox row = new HBox(16);

        // Card 1: Doanh thu hôm nay (Nổi bật màu xanh rêu)
        VBox card1 = new VBox(6);
        card1.getStyleClass().add("stat-card-featured");
        HBox.setHgrow(card1, Priority.ALWAYS);
        Label c1Title = new Label("📈  Doanh thu hôm nay");
        c1Title.getStyleClass().add("stat-title-white");
        Label c1Value = new Label("8.450.000 đ");
        c1Value.getStyleClass().add("stat-value-white");
        Label c1Sub = new Label("↑ +12.5% so với hôm qua");
        c1Sub.getStyleClass().add("stat-sub-white");
        card1.getChildren().addAll(c1Title, c1Value, c1Sub);

        // Card 2: Lịch hẹn hôm nay
        VBox card2 = new VBox(6);
        card2.getStyleClass().add("stat-card");
        HBox.setHgrow(card2, Priority.ALWAYS);
        Label c2Title = new Label("📅  Lịch hẹn hôm nay");
        c2Title.getStyleClass().add("stat-title");
        Label c2Value = new Label("24");
        c2Value.getStyleClass().add("stat-value");
        Label c2Sub = new Label("6 lịch hẹn mới trong giờ qua");
        c2Sub.getStyleClass().add("stat-sub");
        card2.getChildren().addAll(c2Title, c2Value, c2Sub);

        // Card 3: Khách hàng mới
        VBox card3 = new VBox(6);
        card3.getStyleClass().add("stat-card");
        HBox.setHgrow(card3, Priority.ALWAYS);
        Label c3Title = new Label("👥  Khách hàng mới");
        c3Title.getStyleClass().add("stat-title");
        Label c3Value = new Label("18");
        c3Value.getStyleClass().add("stat-value");
        Label c3Sub = new Label("↑ +8% so với trung bình tuần");
        c3Sub.getStyleClass().add("stat-sub");
        card3.getChildren().addAll(c3Title, c3Value, c3Sub);

        // Card 4: Sản phẩm sắp hết hàng
        VBox card4 = new VBox(6);
        card4.getStyleClass().add("stat-card");
        HBox.setHgrow(card4, Priority.ALWAYS);
        Label c4Title = new Label("⚠  Sản phẩm sắp hết hàng");
        c4Title.getStyleClass().add("stat-title");
        Label c4Value = new Label("05");
        c4Value.getStyleClass().add("stat-value-alert");
        Label c4Sub = new Label("Cần nhập thêm hàng ngay");
        c4Sub.getStyleClass().add("stat-sub-alert");
        card4.getChildren().addAll(c4Title, c4Value, c4Sub);

        row.getChildren().addAll(card1, card2, card3, card4);
        return row;
    }

    /**
     * Biểu đồ Doanh thu tuần này (Bar Chart mô phỏng pixel-perfect)
     */
    private VBox createRevenueChartPanel() {
        VBox panel = new VBox(14);
        panel.getStyleClass().add("card-panel");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(3);
        Label title = new Label("Doanh thu tuần này");
        title.getStyleClass().add("card-title");
        Label sub = new Label("Tổng: 54.800.000 đ");
        sub.getStyleClass().add("card-sub");
        titleBox.getChildren().addAll(title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badgeCurrentWeek = new Label("● Tuần hiện tại");
        badgeCurrentWeek.getStyleClass().add("status-badge-paid");

        header.getChildren().addAll(titleBox, spacer, badgeCurrentWeek);

        // Vẽ biểu đồ 7 cột (T2 -> CN)
        HBox chartBox = new HBox(24);
        chartBox.setAlignment(Pos.BOTTOM_CENTER);
        chartBox.setPadding(new Insets(24, 16, 8, 16));

        String[] days = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        double[] heights = {85, 105, 95, 140, 115, 160, 130};

        for (int i = 0; i < days.length; i++) {
            boolean isPeak = (i == 5); // T7 là cột cao nhất nổi bật

            VBox barCol = new VBox(8);
            barCol.setAlignment(Pos.BOTTOM_CENTER);

            if (isPeak) {
                Label tooltip = new Label("● 10.200.000 đ");
                tooltip.setStyle("-fx-font-size: 10px; -fx-text-fill: #2f4f38; -fx-font-weight: bold;");
                barCol.getChildren().add(tooltip);
            }

            Rectangle bar = new Rectangle(36, heights[i]);
            bar.setArcWidth(8);
            bar.setArcHeight(8);
            bar.setFill(isPeak ? Color.web("#2f4f38") : Color.web("#bdd7c3"));

            Label dayLbl = new Label(days[i]);
            dayLbl.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #829485; -fx-font-weight: 600;");

            barCol.getChildren().addAll(bar, dayLbl);
            chartBox.getChildren().add(barCol);
            HBox.setHgrow(barCol, Priority.ALWAYS);
        }

        panel.getChildren().addAll(header, chartBox);
        return panel;
    }

    /**
     * Danh sách Lịch hẹn sắp tới
     */
    private VBox createUpcomingAppointmentsPanel() {
        VBox panel = new VBox(12);
        panel.getStyleClass().add("card-panel");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label title = new Label("Lịch hẹn sắp tới");
        title.getStyleClass().add("card-title");
        Label sub = new Label("Trong ngày hôm nay");
        sub.getStyleClass().add("card-sub");
        titleBox.getChildren().addAll(title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Hyperlink linkAll = new Hyperlink("Xem tất cả");
        linkAll.setStyle("-fx-font-size: 12px; -fx-text-fill: #d97736;");
        linkAll.setOnAction(e -> showPlaceholder("Xem toàn bộ lịch hẹn"));

        header.getChildren().addAll(titleBox, spacer, linkAll);

        // 3 Thẻ lịch hẹn mẫu chuẩn theo thiết kế
        VBox app1 = createAppointmentCard("Bé Lu (Chó)", "Nguyễn Minh Anh • Tắm & Vệ sinh", "NV: Bình An", "09:30");
        VBox app2 = createAppointmentCard("Mimi (Mèo)", "Lê Thu Hà • Cắt tỉa lông", "NV: Mai Phương", "11:00");
        VBox app3 = createAppointmentCard("Bông (Chó)", "Trần Quốc Bảo • Tắm & Vệ sinh", "NV: Bình An", "14:00");

        panel.getChildren().addAll(header, app1, app2, app3);
        return panel;
    }

    private VBox createAppointmentCard(String petName, String serviceDetail, String staff, String time) {
        VBox item = new VBox(4);
        item.getStyleClass().add("appointment-item");

        HBox rowTop = new HBox(8);
        rowTop.setAlignment(Pos.CENTER_LEFT);

        Label calIcon = new Label("📅");
        Label nameLbl = new Label(petName);
        nameLbl.getStyleClass().add("pet-name-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label timeLbl = new Label(time + "  ●");
        timeLbl.getStyleClass().add("appointment-time");

        rowTop.getChildren().addAll(calIcon, nameLbl, spacer, timeLbl);

        Label detailLbl = new Label(serviceDetail + "\n" + staff);
        detailLbl.getStyleClass().add("appointment-details");

        item.getChildren().addAll(rowTop, detailLbl);
        return item;
    }

    /**
     * Bảng Hóa đơn gần đây
     */
    private VBox createRecentInvoicesPanel() {
        VBox panel = new VBox(12);
        panel.getStyleClass().add("card-panel");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Hóa đơn gần đây");
        title.getStyleClass().add("card-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnCreate = new Button("+ Tạo hóa đơn mới");
        btnCreate.getStyleClass().add("btn-create-invoice");
        btnCreate.setOnAction(e -> showPlaceholder("Tạo hóa đơn mới"));

        header.getChildren().addAll(title, spacer, btnCreate);

        // TableView hiển thị hóa đơn
        TableView<InvoiceRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<InvoiceRow, String> colCode = new TableColumn<>("MÃ HĐ");
        colCode.setCellValueFactory(d -> d.getValue().code);

        TableColumn<InvoiceRow, String> colCustomer = new TableColumn<>("KHÁCH HÀNG");
        colCustomer.setCellValueFactory(d -> d.getValue().customer);

        TableColumn<InvoiceRow, String> colPet = new TableColumn<>("THÚ CƯNG");
        colPet.setCellValueFactory(d -> d.getValue().pet);

        TableColumn<InvoiceRow, String> colTotal = new TableColumn<>("TỔNG TIỀN");
        colTotal.setCellValueFactory(d -> d.getValue().total);

        TableColumn<InvoiceRow, String> colStatus = new TableColumn<>("TRẠNG THÁI");
        colStatus.setCellValueFactory(d -> d.getValue().status);
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add(item.contains("Đã thanh toán") ? "status-badge-paid" : "status-badge-pending");
                    setGraphic(badge);
                }
            }
        });

        table.getColumns().addAll(colCode, colCustomer, colPet, colTotal, colStatus);

        ObservableList<InvoiceRow> data = FXCollections.observableArrayList(
                new InvoiceRow("HD-20261005-001", "Nguyễn Thu Trang", "Mochi (Mèo)", "350.000 đ", "Đã thanh toán"),
                new InvoiceRow("HD-20261005-002", "Lê Hoàng Nam", "Bông (Chó)", "180.000 đ", "Đã thanh toán"),
                new InvoiceRow("HD-20261005-003", "Võ Thị Mai", "Lu (Chó)", "220.000 đ", "Chưa thanh toán")
        );
        table.setItems(data);

        panel.getChildren().addAll(header, table);
        return panel;
    }

    private Group createMiniPawWhite() {
        Group group = new Group();
        Color c = Color.WHITE;
        Ellipse pad = new Ellipse(0, 3, 7, 5);
        pad.setFill(c);
        Circle t1 = new Circle(-6, -4, 2.3, c);
        Circle t2 = new Circle(-2, -7, 2.5, c);
        Circle t3 = new Circle(2, -7, 2.5, c);
        Circle t4 = new Circle(6, -4, 2.3, c);
        group.getChildren().addAll(pad, t1, t2, t3, t4);
        return group;
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "HT";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[parts.length - 2].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private void showPlaceholder(String module) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(module);
        alert.setHeaderText(null);
        alert.setContentText("Phân hệ [" + module + "] đang được kết nối với cơ sở dữ liệu.");
        alert.showAndWait();
    }

    private void logout() {
        SessionManager.logout();
        stage.setTitle("Happy Tails | Đăng nhập hệ thống");
        Scene scene = new Scene(new LoginView(stage), 600, 680);
        scene.getStylesheets().add(getClass().getResource("/com/petshop/view/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    public static class InvoiceRow {
        public final SimpleStringProperty code;
        public final SimpleStringProperty customer;
        public final SimpleStringProperty pet;
        public final SimpleStringProperty total;
        public final SimpleStringProperty status;

        public InvoiceRow(String c, String cu, String p, String t, String s) {
            this.code = new SimpleStringProperty(c);
            this.customer = new SimpleStringProperty(cu);
            this.pet = new SimpleStringProperty(p);
            this.total = new SimpleStringProperty(t);
            this.status = new SimpleStringProperty(s);
        }
    }
}
