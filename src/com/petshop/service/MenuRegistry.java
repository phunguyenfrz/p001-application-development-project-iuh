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

    public static class SubAction {
        private final String id;
        private final String name;
        private final String shortcut;

        public SubAction(String id, String name, String shortcut) {
            this.id = id;
            this.name = name;
            this.shortcut = shortcut;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getShortcut() { return shortcut; }
    }

    public static class ObjectMenu {
        private final String id;
        private final String title;
        private final String iconType;
        private final List<SubAction> subActions;

        public ObjectMenu(String id, String title, String iconType, List<SubAction> subActions) {
            this.id = id;
            this.title = title;
            this.iconType = iconType;
            this.subActions = subActions;
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getIconType() { return iconType; }
        public List<SubAction> getSubActions() { return subActions; }
    }

    // Danh sách menu theo thứ tự hiển thị trên thanh điều hướng; mục Tổng quan được dựng riêng.
    private static final List<ObjectMenu> ALL_MENUS = new ArrayList<>();

    // Bảng quyền tập trung ánh xạ mã chức năng sang các vai trò được phép.
    private static final Map<String, Set<User.Role>> PERMISSION_MAP = new HashMap<>();

    static {
        // 1. Khách hàng
        ALL_MENUS.add(new ObjectMenu("CUSTOMER", "Khách hàng", "CUSTOMER", List.of(
                new SubAction("CUS_CRUD", "Thêm/Sửa/Xóa khách hàng", "Alt+C"),
                new SubAction("CUS_SEARCH", "Tìm kiếm khách hàng", "Ctrl+F"),
                new SubAction("CUS_HISTORY", "Lịch sử mua hàng / sử dụng dịch vụ", null)
        )));

        // 2. Thú cưng
        ALL_MENUS.add(new ObjectMenu("PET", "Thú cưng", "PET", List.of(
                new SubAction("PET_CRUD", "Thêm/Sửa/Xóa thú cưng", "Alt+P"),
                new SubAction("PET_SEARCH", "Tìm kiếm thú cưng", "Ctrl+F"),
                new SubAction("PET_WEIGHT", "Cập nhật sức khỏe / Cân nặng", null),
                new SubAction("PET_HISTORY", "Lịch sử chăm sóc", null)
        )));

        // 3. Lịch hẹn
        ALL_MENUS.add(new ObjectMenu("APPOINTMENT", "Lịch hẹn", "APPOINTMENT", List.of(
                new SubAction("APT_CREATE", "Đặt lịch hẹn mới", null),
                new SubAction("APT_STATUS", "Quản lý trạng thái lịch hẹn", null),
                new SubAction("APT_RESCHEDULE", "Dời / Hủy lịch hẹn", null),
                new SubAction("APT_SEARCH", "Tìm kiếm lịch hẹn", "Ctrl+F")
        )));

        // 4. Nhân viên
        ALL_MENUS.add(new ObjectMenu("EMPLOYEE", "Nhân viên", "EMPLOYEE", List.of(
                new SubAction("EMP_CRUD", "Thêm/Sửa/Ngừng hoạt động nhân viên", "Alt+E"),
                new SubAction("EMP_SEARCH", "Tìm kiếm nhân viên", "Ctrl+F"),
                new SubAction("EMP_ROLES", "Phân quyền tài khoản", null),
                new SubAction("EMP_ACCOUNT", "Quản lý tài khoản (khóa/mở khóa, đổi mật khẩu)", null)
        )));

        // 5. Ca trực
        ALL_MENUS.add(new ObjectMenu("SHIFT", "Ca trực", "SHIFT", List.of(
                new SubAction("SHF_CRUD", "Thêm/Sửa/Xóa ca làm việc", null),
                new SubAction("SHF_ASSIGN", "Phân công ca trực", null),
                new SubAction("SHF_VIEW", "Xem lịch ca trực", null)
        )));

        // 6. Nhà cung cấp
        ALL_MENUS.add(new ObjectMenu("SUPPLIER", "Nhà cung cấp", "SUPPLIER", List.of(
                new SubAction("SUP_CRUD", "Thêm/Sửa/Ngừng nhà cung cấp", null),
                new SubAction("SUP_SEARCH", "Tìm kiếm nhà cung cấp", "Ctrl+F")
        )));

        // 7. Đơn hàng nhập
        ALL_MENUS.add(new ObjectMenu("IMPORT_ORDER", "Đơn hàng nhập", "IMPORT_ORDER", List.of(
                new SubAction("IMP_CREATE", "Lập đơn hàng mới", null),
                new SubAction("IMP_CONFIRM", "Xác nhận nhận hàng", null),
                new SubAction("IMP_CANCEL", "Sửa / Hủy đơn hàng", null),
                new SubAction("IMP_SEARCH", "Tìm kiếm đơn hàng", "Ctrl+F")
        )));

        // 8. Sản phẩm
        ALL_MENUS.add(new ObjectMenu("PRODUCT", "Sản phẩm", "PRODUCT", List.of(
                new SubAction("PRD_CRUD", "Thêm/Sửa/Ngừng kinh doanh sản phẩm", null),
                new SubAction("PRD_SEARCH", "Tìm kiếm sản phẩm", "Ctrl+F"),
                new SubAction("PRD_INVENTORY", "Quản lý tồn kho", null),
                new SubAction("PRD_LOW_STOCK", "Cảnh báo hết hàng", null),
                new SubAction("PRD_REPORT", "Báo cáo tồn kho / sản phẩm bán chạy", null)
        )));

        // 9. Dịch vụ chăm sóc
        ALL_MENUS.add(new ObjectMenu("SERVICE", "Dịch vụ chăm sóc", "SERVICE", List.of(
                new SubAction("SRV_CRUD", "Thêm/Sửa/Ngừng cung cấp dịch vụ", null),
                new SubAction("SRV_SEARCH", "Tìm kiếm dịch vụ", "Ctrl+F"),
                new SubAction("SRV_PRICING", "Bảng giá theo cân nặng", null),
                new SubAction("SRV_REPORT", "Báo cáo dịch vụ bán chạy", null)
        )));

        // 10. Khuyến mãi
        ALL_MENUS.add(new ObjectMenu("PROMOTION", "Khuyến mãi", "PROMOTION", List.of(
                new SubAction("PROMO_CREATE", "Tạo khuyến mãi", null),
                new SubAction("PROMO_EDIT", "Sửa khuyến mãi", null),
                new SubAction("PROMO_CANCEL", "Hủy/Dừng khuyến mãi", null),
                new SubAction("PROMO_SEARCH", "Tìm kiếm khuyến mãi", "Ctrl+F")
        )));

        // 11. Hóa đơn
        ALL_MENUS.add(new ObjectMenu("INVOICE", "Hóa đơn", "INVOICE", List.of(
                new SubAction("INV_CREATE", "Lập hóa đơn (Thanh toán)", null),
                new SubAction("INV_SEARCH", "Tìm kiếm / In hóa đơn", "Ctrl+F"),
                new SubAction("INV_CANCEL", "Hủy hóa đơn", null),
                new SubAction("INV_REVENUE", "Thống kê doanh thu", null)
        )));

        // 12. Phiếu đổi trả
        ALL_MENUS.add(new ObjectMenu("RETURN_NOTE", "Phiếu đổi trả", "RETURN_NOTE", List.of(
                new SubAction("RET_CREATE", "Lập phiếu đổi trả", null),
                new SubAction("RET_CONFIRM", "Xác nhận / Từ chối đổi trả", null),
                new SubAction("RET_SEARCH", "Tìm kiếm phiếu đổi trả", "Ctrl+F")
        )));

        // Khởi tạo quyền cho quản lý, bán hàng và chăm sóc; quản trị viên luôn có toàn quyền.
        for (ObjectMenu menu : ALL_MENUS) {
            for (SubAction action : menu.getSubActions()) {
                Set<User.Role> roles = new HashSet<>();
                roles.add(User.Role.ADMIN);

                // Quản lý được phép dùng các menu quản trị và xem báo cáo được chỉ định.
                if (menu.getId().equals("EMPLOYEE") || menu.getId().equals("SHIFT") ||
                    menu.getId().equals("SUPPLIER") || menu.getId().equals("SERVICE") ||
                    menu.getId().equals("PROMOTION") || action.getId().equals("INV_REVENUE") ||
                    action.getId().equals("PRD_LOW_STOCK") || action.getId().equals("PRD_REPORT")) {
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
                    action.getId().equals("CUS_SEARCH") || action.getId().equals("SRV_SEARCH")) {
                    roles.add(User.Role.CHAM_SOC);
                }

                PERMISSION_MAP.put(action.getId(), roles);
            }
        }
    }

    /**
     * Lấy toàn bộ danh sách menu gốc (dành cho Admin hoặc tham chiếu).
     */
    public static List<ObjectMenu> getAllMenus() {
        return Collections.unmodifiableList(ALL_MENUS);
    }

    /**
     * Lấy danh sách menu sau khi đã lọc theo vai trò của người dùng.
     * Mục cha chỉ xuất hiện nếu có ít nhất 1 chức năng con được phép truy cập.
     */
    public static List<ObjectMenu> getMenusForRole(User.Role role) {
        if (role == null) return Collections.emptyList();
        if (role == User.Role.ADMIN) return getAllMenus();

        List<ObjectMenu> filtered = new ArrayList<>();
        for (ObjectMenu menu : ALL_MENUS) {
            List<SubAction> permittedActions = new ArrayList<>();
            for (SubAction action : menu.getSubActions()) {
                if (hasPermission(role, action.getId())) {
                    permittedActions.add(action);
                }
            }
            if (!permittedActions.isEmpty()) {
                filtered.add(new ObjectMenu(menu.getId(), menu.getTitle(), menu.getIconType(), permittedActions));
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
