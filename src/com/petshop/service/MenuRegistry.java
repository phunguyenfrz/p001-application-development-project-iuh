package com.petshop.service;

import com.petshop.model.User;

import java.util.*;

/**
 * Registry tập trung toàn bộ danh mục Menu Hướng Đối Tượng (11 đối tượng).
 * Khai báo id, nhãn, icon, phím tắt, danh sách chức năng con và phân quyền RBAC.
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

    // 11 Mục menu đối tượng theo đúng thứ tự nghiệp vụ
    private static final List<ObjectMenu> ALL_MENUS = new ArrayList<>();

    // Bảng quyền RBAC tập trung: ActionID -> Set<User.Role>
    private static final Map<String, Set<User.Role>> PERMISSION_MAP = new HashMap<>();

    static {
        // 1. Nhân viên
        ALL_MENUS.add(new ObjectMenu("EMPLOYEE", "Nhân viên", "EMPLOYEE", List.of(
                new SubAction("EMP_CRUD", "Thêm/Sửa/Xóa nhân viên", "Ctrl+E"),
                new SubAction("EMP_SEARCH", "Tìm kiếm nhân viên", "Ctrl+F"),
                new SubAction("EMP_SHIFTS", "Quản lý ca làm việc", null),
                new SubAction("EMP_ASSIGN", "Phân công ca trực", null),
                new SubAction("EMP_ROLES", "Phân quyền tài khoản", null)
        )));

        // 2. Khách hàng
        ALL_MENUS.add(new ObjectMenu("CUSTOMER", "Khách hàng", "CUSTOMER", List.of(
                new SubAction("CUS_CRUD", "Thêm/Sửa/Xóa khách hàng", "Ctrl+C"),
                new SubAction("CUS_SEARCH", "Tìm kiếm khách hàng", "Ctrl+F"),
                new SubAction("CUS_HISTORY", "Xem lịch sử mua hàng", null)
        )));

        // 3. Thú cưng
        ALL_MENUS.add(new ObjectMenu("PET", "Thú cưng", "PET", List.of(
                new SubAction("PET_CRUD", "Thêm/Sửa/Xóa thú cưng", "Ctrl+P"),
                new SubAction("PET_SEARCH", "Tìm kiếm thú cưng", "Ctrl+F"),
                new SubAction("PET_WEIGHT", "Cập nhật cân nặng/dị ứng", null),
                new SubAction("PET_HISTORY", "Xem lịch sử chăm sóc", null)
        )));

        // 4. Nhà cung cấp
        ALL_MENUS.add(new ObjectMenu("SUPPLIER", "Nhà cung cấp", "SUPPLIER", List.of(
                new SubAction("SUP_CRUD", "Thêm/Sửa/Xóa nhà cung cấp", null),
                new SubAction("SUP_SEARCH", "Tìm kiếm nhà cung cấp", "Ctrl+F")
        )));

        // 5. Sản phẩm
        ALL_MENUS.add(new ObjectMenu("PRODUCT", "Sản phẩm", "PRODUCT", List.of(
                new SubAction("PRD_CRUD", "Thêm/Sửa/Xóa sản phẩm", null),
                new SubAction("PRD_SEARCH", "Tìm kiếm sản phẩm", "Ctrl+F"),
                new SubAction("PRD_INVENTORY", "Kiểm kê tồn kho", null),
                new SubAction("PRD_UPDATE_STOCK", "Cập nhật tồn kho", null),
                new SubAction("PRD_LOW_STOCK", "Cảnh báo hết hàng", null)
        )));

        // 6. Dịch vụ chăm sóc
        ALL_MENUS.add(new ObjectMenu("SERVICE", "Dịch vụ chăm sóc", "SERVICE", List.of(
                new SubAction("SRV_CRUD", "Thêm/Sửa/Xóa dịch vụ", null),
                new SubAction("SRV_SEARCH", "Tìm kiếm dịch vụ", "Ctrl+F"),
                new SubAction("SRV_PRICING", "Bảng giá theo cân nặng", null)
        )));

        // 7. Khuyến mãi
        ALL_MENUS.add(new ObjectMenu("PROMOTION", "Khuyến mãi", "PROMOTION", List.of(
                new SubAction("PROMO_CREATE", "Tạo khuyến mãi", null),
                new SubAction("PROMO_EDIT", "Sửa khuyến mãi", null),
                new SubAction("PROMO_CANCEL", "Hủy/Dừng khuyến mãi", null),
                new SubAction("PROMO_SEARCH", "Tìm kiếm khuyến mãi", "Ctrl+F")
        )));

        // 8. Đơn nhập hàng
        ALL_MENUS.add(new ObjectMenu("IMPORT_ORDER", "Đơn nhập hàng", "IMPORT_ORDER", List.of(
                new SubAction("IMP_CREATE", "Lập đơn nhập", null),
                new SubAction("IMP_CONFIRM", "Xác nhận đã nhận hàng", null),
                new SubAction("IMP_CANCEL", "Hủy đơn nhập", null),
                new SubAction("IMP_SEARCH", "Tìm kiếm đơn nhập", "Ctrl+F")
        )));

        // 9. Lịch hẹn
        ALL_MENUS.add(new ObjectMenu("APPOINTMENT", "Lịch hẹn", "APPOINTMENT", List.of(
                new SubAction("APT_CREATE", "Đặt lịch mới (kiểm tra trùng)", null),
                new SubAction("APT_STATUS", "Cập nhật trạng thái", null),
                new SubAction("APT_RESCHEDULE", "Đổi lịch hẹn", null),
                new SubAction("APT_CANCEL", "Hủy lịch hẹn", null),
                new SubAction("APT_SEARCH", "Tìm kiếm lịch hẹn", "Ctrl+F")
        )));

        // 10. Hóa đơn
        ALL_MENUS.add(new ObjectMenu("INVOICE", "Hóa đơn", "INVOICE", List.of(
                new SubAction("INV_CREATE", "Lập hóa đơn (quét mã vạch/lịch hẹn)", null),
                new SubAction("INV_DISCOUNT", "Áp dụng khuyến mãi", null),
                new SubAction("INV_PRINT", "In hóa đơn", null),
                new SubAction("INV_CANCEL", "Hủy hóa đơn", null),
                new SubAction("INV_SEARCH", "Tìm kiếm hóa đơn", "Ctrl+F"),
                new SubAction("INV_REVENUE", "Thống kê doanh thu (Xuất Excel/PDF)", null)
        )));

        // 11. Phiếu đổi trả
        ALL_MENUS.add(new ObjectMenu("RETURN_NOTE", "Phiếu đổi trả", "RETURN_NOTE", List.of(
                new SubAction("RET_CREATE", "Lập phiếu đổi trả", null),
                new SubAction("RET_SEARCH", "Tìm kiếm phiếu đổi trả", "Ctrl+F")
        )));

        // Ma trận quyền: ADMIN có toàn quyền
        // Cấu hình quyền chi tiết cho từng vai trò
        for (ObjectMenu menu : ALL_MENUS) {
            for (SubAction action : menu.getSubActions()) {
                Set<User.Role> roles = new HashSet<>();
                roles.add(User.Role.ADMIN); // Admin luôn có quyền

                // Phân quyền cho QUAN_LY
                if (menu.getId().equals("EMPLOYEE") || menu.getId().equals("SUPPLIER") ||
                    menu.getId().equals("SERVICE") || menu.getId().equals("PROMOTION") ||
                    action.getId().equals("INV_REVENUE") || action.getId().equals("PRD_LOW_STOCK")) {
                    roles.add(User.Role.QUAN_LY);
                }

                // Phân quyền cho BAN_HANG
                if (menu.getId().equals("CUSTOMER") || menu.getId().equals("PRODUCT") ||
                    menu.getId().equals("IMPORT_ORDER") || menu.getId().equals("RETURN_NOTE") ||
                    (menu.getId().equals("INVOICE") && !action.getId().equals("INV_REVENUE"))) {
                    roles.add(User.Role.BAN_HANG);
                }

                // Phân quyền cho CHAM_SOC
                if (menu.getId().equals("APPOINTMENT") || menu.getId().equals("PET")) {
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

