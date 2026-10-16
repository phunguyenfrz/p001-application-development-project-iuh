package com.petshop.service;

import com.petshop.model.User;

import java.util.*;

/**
 * Danh mục 12 đối tượng trên thanh điều hướng: Khách hàng, Thú cưng, Lịch hẹn, Nhân viên,
 * Ca trực, Nhà cung cấp, Đơn hàng nhập, Sản phẩm, Dịch vụ chăm sóc, Khuyến mãi, Hóa đơn,
 * Phiếu đổi trả.
 * Khai báo mã, tên, biểu tượng, phím tắt, chức năng con và ma trận phân quyền.
 */
public class MenuRegistry {

    public static class MenuAction {
        private final String id;
        private final String name;
        private final String shortcut;

        public MenuAction(String id, String name, String shortcut) {
            this.id = id;
            this.name = name;
            this.shortcut = shortcut;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getShortcut() { return shortcut; }
    }

    public static class MenuGroup {
        private final String id;
        private final String title;
        private final String iconType;
        private final List<MenuAction> actions;

        public MenuGroup(String id, String title, String iconType, List<MenuAction> actions) {
            this.id = id;
            this.title = title;
            this.iconType = iconType;
            this.actions = actions;
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getIconType() { return iconType; }
        public List<MenuAction> getActions() { return actions; }
    }

    // Danh sách menu theo thứ tự hiển thị trên thanh điều hướng; mục Tổng quan được dựng riêng.
    private static final List<MenuGroup> ALL_MENUS = new ArrayList<>();

    // Bảng quyền tập trung ánh xạ mã chức năng sang các vai trò được phép.
    private static final Map<String, Set<User.Role>> PERMISSION_MAP = new HashMap<>();

    static {
        // 1. Khách hàng
        ALL_MENUS.add(new MenuGroup("CUSTOMER", "Khách hàng", "CUSTOMER", List.of(
                new MenuAction("CUS_UPDATE", "Cập nhật khách hàng", "Alt+C"),
                new MenuAction("CUS_SEARCH", "Tìm kiếm khách hàng", "Ctrl+F"),
                new MenuAction("CUS_HISTORY", "Lịch sử giao dịch", null)
        )));

        // 2. Thú cưng
        ALL_MENUS.add(new MenuGroup("PET", "Thú cưng", "PET", List.of(
                new MenuAction("PET_UPDATE", "Cập nhật thú cưng", "Alt+P"),
                new MenuAction("PET_SEARCH", "Tìm kiếm thú cưng", "Ctrl+F"),
                new MenuAction("PET_CARE", "Lịch sử chăm sóc", null)
        )));

        // 3. Lịch hẹn
        ALL_MENUS.add(new MenuGroup("APPOINTMENT", "Lịch hẹn", "APPOINTMENT", List.of(
                new MenuAction("APT_CREATE", "Đặt lịch hẹn", null),
                new MenuAction("APT_UPDATE", "Cập nhật lịch hẹn", null),
                new MenuAction("APT_SEARCH", "Tìm kiếm lịch hẹn", "Ctrl+F")
        )));

        // 4. Nhân viên
        ALL_MENUS.add(new MenuGroup("EMPLOYEE", "Nhân viên", "EMPLOYEE", List.of(
                new MenuAction("EMP_UPDATE", "Cập nhật nhân viên", "Alt+E"),
                new MenuAction("EMP_SEARCH", "Tìm kiếm nhân viên", "Ctrl+F"),
                new MenuAction("EMP_ROLES", "Phân quyền", null),
                new MenuAction("EMP_ACCOUNT", "Tài khoản đăng nhập", null)
        )));

        // 5. Ca trực
        ALL_MENUS.add(new MenuGroup("SHIFT", "Ca trực", "SHIFT", List.of(
                new MenuAction("SHF_UPDATE", "Cập nhật ca trực", null),
                new MenuAction("SHF_ASSIGN", "Phân công ca trực", null),
                new MenuAction("SHF_VIEW", "Lịch ca trực", null)
        )));

        // 6. Nhà cung cấp
        ALL_MENUS.add(new MenuGroup("SUPPLIER", "Nhà cung cấp", "SUPPLIER", List.of(
                new MenuAction("SUP_UPDATE", "Cập nhật nhà cung cấp", null),
                new MenuAction("SUP_SEARCH", "Tìm kiếm nhà cung cấp", "Ctrl+F")
        )));

        // 7. Đơn hàng nhập
        ALL_MENUS.add(new MenuGroup("IMPORT_ORDER", "Đơn hàng nhập", "IMPORT_ORDER", List.of(
                new MenuAction("IMP_CREATE", "Lập đơn nhập", null),
                new MenuAction("IMP_RECEIVE", "Nhận hàng", null),
                new MenuAction("IMP_UPDATE", "Cập nhật đơn nhập", null),
                new MenuAction("IMP_SEARCH", "Tìm kiếm đơn nhập", "Ctrl+F")
        )));

        // 8. Sản phẩm
        ALL_MENUS.add(new MenuGroup("PRODUCT", "Sản phẩm", "PRODUCT", List.of(
                new MenuAction("PRD_UPDATE", "Cập nhật sản phẩm", null),
                new MenuAction("PRD_SEARCH", "Tìm kiếm sản phẩm", "Ctrl+F"),
                new MenuAction("PRD_INVENTORY", "Tồn kho", null),
                new MenuAction("PRD_REPORT", "Báo cáo sản phẩm", null)
        )));

        // 9. Dịch vụ chăm sóc
        ALL_MENUS.add(new MenuGroup("CARE_SERVICE", "Dịch vụ chăm sóc", "CARE_SERVICE", List.of(
                new MenuAction("CAR_UPDATE", "Cập nhật dịch vụ", null),
                new MenuAction("CAR_SEARCH", "Tìm kiếm dịch vụ", "Ctrl+F"),
                new MenuAction("CAR_PRICING", "Bảng giá dịch vụ", null),
                new MenuAction("CAR_REPORT", "Báo cáo dịch vụ", null)
        )));

        // 10. Khuyến mãi
        ALL_MENUS.add(new MenuGroup("PROMOTION", "Khuyến mãi", "PROMOTION", List.of(
                new MenuAction("PRM_UPDATE", "Cập nhật khuyến mãi", null),
                new MenuAction("PRM_SEARCH", "Tìm kiếm khuyến mãi", "Ctrl+F")
        )));

        // 11. Hóa đơn
        ALL_MENUS.add(new MenuGroup("INVOICE", "Hóa đơn", "INVOICE", List.of(
                new MenuAction("INV_CREATE", "Lập hóa đơn", null),
                new MenuAction("INV_SEARCH", "Tìm kiếm hóa đơn", "Ctrl+F"),
                new MenuAction("INV_UPDATE", "Cập nhật hóa đơn", null),
                new MenuAction("INV_REVENUE", "Báo cáo doanh thu", null)
        )));

        // 12. Phiếu đổi trả
        ALL_MENUS.add(new MenuGroup("RETURN_NOTE", "Phiếu đổi trả", "RETURN_NOTE", List.of(
                new MenuAction("RET_CREATE", "Lập phiếu đổi trả", null),
                new MenuAction("RET_PROCESS", "Xử lý đổi trả", null),
                new MenuAction("RET_SEARCH", "Tìm kiếm phiếu đổi trả", "Ctrl+F")
        )));

        // Khởi tạo quyền cho quản lý, bán hàng và chăm sóc; quản trị viên luôn có toàn quyền.
        for (MenuGroup menu : ALL_MENUS) {
            for (MenuAction action : menu.getActions()) {
                Set<User.Role> roles = new HashSet<>();
                roles.add(User.Role.ADMIN);

                // Quản lý được phép dùng các menu quản trị và xem báo cáo được chỉ định.
                if (menu.getId().equals("EMPLOYEE") || menu.getId().equals("SHIFT") ||
                    menu.getId().equals("SUPPLIER") || menu.getId().equals("CARE_SERVICE") ||
                    menu.getId().equals("PROMOTION") || action.getId().equals("INV_REVENUE") ||
                    action.getId().equals("PRD_INVENTORY") || action.getId().equals("PRD_REPORT")) {
                    roles.add(User.Role.QUAN_LY);
                }

                // Bán hàng được phép dùng các menu nghiệp vụ bán hàng, trừ báo cáo doanh thu.
                if (menu.getId().equals("CUSTOMER") || menu.getId().equals("PRODUCT") ||
                    menu.getId().equals("IMPORT_ORDER") || menu.getId().equals("RETURN_NOTE") ||
                    (menu.getId().equals("INVOICE") && !action.getId().equals("INV_REVENUE"))) {
                    roles.add(User.Role.BAN_HANG);
                }

                // Chăm sóc được phép dùng lịch hẹn, thú cưng và tra cứu khách hàng, dịch vụ.
                if (menu.getId().equals("APPOINTMENT") || menu.getId().equals("PET") ||
                    action.getId().equals("CUS_SEARCH") || action.getId().equals("CAR_SEARCH")) {
                    roles.add(User.Role.CHAM_SOC);
                }

                PERMISSION_MAP.put(action.getId(), roles);
            }
        }
    }

    /**
     * Lấy toàn bộ danh sách menu gốc (dành cho Admin hoặc tham chiếu).
     */
    public static List<MenuGroup> getAllMenus() {
        return Collections.unmodifiableList(ALL_MENUS);
    }

    /**
     * Lấy danh sách menu sau khi đã lọc theo vai trò của người dùng.
     * Mục cha chỉ xuất hiện nếu có ít nhất 1 chức năng con được phép truy cập.
     */
    public static List<MenuGroup> getMenusForRole(User.Role role) {
        if (role == null) return Collections.emptyList();
        if (role == User.Role.ADMIN) return getAllMenus();

        List<MenuGroup> filtered = new ArrayList<>();
        for (MenuGroup menu : ALL_MENUS) {
            List<MenuAction> permittedActions = new ArrayList<>();
            for (MenuAction action : menu.getActions()) {
                if (hasPermission(role, action.getId())) {
                    permittedActions.add(action);
                }
            }
            if (!permittedActions.isEmpty()) {
                filtered.add(new MenuGroup(menu.getId(), menu.getTitle(), menu.getIconType(), permittedActions));
            }
        }
        return filtered;
    }

    /**
     * Kiểm tra vai trò có quyền thực hiện chức năng con không.
     */
    public static boolean hasPermission(User.Role role, String actionId) {
        if (role == User.Role.ADMIN) return true;
        Set<User.Role> permitted = PERMISSION_MAP.get(actionId);
        return permitted != null && permitted.contains(role);
    }
}
