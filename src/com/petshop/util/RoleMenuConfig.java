package com.petshop.util;

import com.petshop.model.User;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class RoleMenuConfig {
    public enum Screen {
        OVERVIEW,
        EMPLOYEE_LIST,
        EMPLOYEE_CREATE,
        ACCOUNT_ROLES,
        SHIFT_SCHEDULE,
        SHIFT_ASSIGN,
        SUPPLIERS,
        SERVICE_CATALOG,
        SERVICE_PRICING,
        PROMOTIONS,
        REPORT_REVENUE,
        REPORT_INVENTORY,
        REPORT_BEST_SELLERS,
        INVOICE_CREATE,
        INVOICE_HISTORY,
        CUSTOMER_SEARCH,
        CUSTOMER_CREATE,
        PRODUCT_CATALOG,
        PRODUCT_CREATE,
        INVENTORY_VIEW,
        INVENTORY_ALERTS,
        IMPORT_LIST,
        IMPORT_CREATE,
        APPOINTMENT_TODAY,
        APPOINTMENT_CREATE,
        APPOINTMENT_SEARCH_DATE,
        PET_PROFILES,
        PET_REGISTER
    }

    public record MenuEntry(String label, Screen screen) {
    }

    public record MenuSection(String label, List<MenuEntry> entries) {
        public MenuSection {
            entries = List.copyOf(entries);
        }
    }

    private static final Map<User.Role, List<MenuSection>> MENUS = createMenus();

    private RoleMenuConfig() {
    }

    public static List<MenuSection> getMenu(User.Role role) {
        List<MenuSection> menu = MENUS.get(role);
        if (menu == null) {
            throw new IllegalArgumentException("Vai trò không hợp lệ: " + role);
        }
        return menu;
    }

    public static boolean canAccess(User.Role role, Screen screen) {
        if (role == null || screen == null) {
            return false;
        }
        if (screen == Screen.OVERVIEW) {
            return true;
        }
        return getMenu(role).stream()
                .flatMap(section -> section.entries().stream())
                .anyMatch(entry -> entry.screen() == screen);
    }

    private static Map<User.Role, List<MenuSection>> createMenus() {
        Map<User.Role, List<MenuSection>> menus = new EnumMap<>(User.Role.class);
        menus.put(User.Role.QUAN_LY, List.of(
                section("Nhân viên",
                        entry("Danh sách", Screen.EMPLOYEE_LIST),
                        entry("Thêm mới", Screen.EMPLOYEE_CREATE),
                        entry("Tài khoản & vai trò", Screen.ACCOUNT_ROLES)),
                section("Ca trực",
                        entry("Bảng phân ca", Screen.SHIFT_SCHEDULE),
                        entry("Thêm/Gán ca", Screen.SHIFT_ASSIGN)),
                direct("Nhà cung cấp", Screen.SUPPLIERS),
                section("Dịch vụ",
                        entry("Danh mục", Screen.SERVICE_CATALOG),
                        entry("Bảng giá 3 bậc", Screen.SERVICE_PRICING)),
                direct("Khuyến mãi", Screen.PROMOTIONS),
                section("Báo cáo",
                        entry("Doanh thu", Screen.REPORT_REVENUE),
                        entry("Tồn kho", Screen.REPORT_INVENTORY),
                        entry("Sản phẩm/Dịch vụ bán chạy", Screen.REPORT_BEST_SELLERS))
        ));
        menus.put(User.Role.BAN_HANG, List.of(
                section("Thanh toán",
                        entry("Lập hóa đơn mới", Screen.INVOICE_CREATE),
                        entry("Lịch sử hóa đơn", Screen.INVOICE_HISTORY)),
                section("Khách hàng",
                        entry("Tra cứu", Screen.CUSTOMER_SEARCH),
                        entry("Thêm mới", Screen.CUSTOMER_CREATE)),
                section("Sản phẩm",
                        entry("Danh mục", Screen.PRODUCT_CATALOG),
                        entry("Thêm mới", Screen.PRODUCT_CREATE)),
                section("Tồn kho",
                        entry("Xem tồn kho", Screen.INVENTORY_VIEW),
                        entry("Cảnh báo chạm ngưỡng", Screen.INVENTORY_ALERTS)),
                section("Đơn nhập",
                        entry("Danh sách", Screen.IMPORT_LIST),
                        entry("Lập đơn nhập mới", Screen.IMPORT_CREATE))
        ));
        menus.put(User.Role.CHAM_SOC, List.of(
                section("Lịch hẹn",
                        entry("Lịch hôm nay", Screen.APPOINTMENT_TODAY),
                        entry("Đặt lịch mới", Screen.APPOINTMENT_CREATE),
                        entry("Tìm theo ngày", Screen.APPOINTMENT_SEARCH_DATE)),
                section("Thú cưng",
                        entry("Hồ sơ", Screen.PET_PROFILES),
                        entry("Đăng ký mới", Screen.PET_REGISTER)),
                section("Khách hàng",
                        entry("Tra cứu", Screen.CUSTOMER_SEARCH))
        ));
        return Map.copyOf(menus);
    }

    private static MenuSection direct(String label, Screen screen) {
        return section(label, entry(label, screen));
    }

    private static MenuSection section(String label, MenuEntry... entries) {
        return new MenuSection(label, List.of(entries));
    }

    private static MenuEntry entry(String label, Screen screen) {
        return new MenuEntry(label, screen);
    }
}
