package com.petshop.model;

import java.util.Set;

public enum Role {
    QUAN_LY("Quản lý", true, false, true, Set.of(
            Screen.OVERVIEW, Screen.EMPLOYEES, Screen.SHIFTS, Screen.SUPPLIERS,
            Screen.CARE_SERVICES, Screen.PROMOTIONS, Screen.REPORTS)),
    BAN_HANG("Bán hàng", false, false, true, Set.of(
            Screen.OVERVIEW, Screen.PRODUCTS, Screen.INVENTORY, Screen.PURCHASE_ORDERS,
            Screen.CUSTOMERS, Screen.INVOICE_CREATE,
            Screen.INVOICE_HISTORY, Screen.RETURNS)),
    CHAM_SOC("Chăm sóc", false, true, false, Set.of(
            Screen.OVERVIEW, Screen.APPOINTMENTS, Screen.PETS, Screen.CUSTOMERS));

    private final String displayName;
    private final Set<Screen> allowedScreens;
    private final boolean allAppointmentNotifications;
    private final boolean ownAppointmentNotifications;
    private final boolean lowStockNotifications;

    Role(String displayName, boolean allAppointmentNotifications,
         boolean ownAppointmentNotifications, boolean lowStockNotifications,
         Set<Screen> allowedScreens) {
        this.displayName = displayName;
        this.allowedScreens = Set.copyOf(allowedScreens);
        this.allAppointmentNotifications = allAppointmentNotifications;
        this.ownAppointmentNotifications = ownAppointmentNotifications;
        this.lowStockNotifications = lowStockNotifications;
    }

    public boolean canAccess(Screen screen) {
        return screen != null && allowedScreens.contains(screen);
    }

    public Set<Screen> getAllowedScreens() {
        return allowedScreens;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getHelpText() {
        return switch (this) {
            case QUAN_LY -> "Quản lý nhân sự, danh mục, khuyến mãi và báo cáo.";
            case BAN_HANG -> "Quản lý sản phẩm, tồn kho, đơn nhập, khách hàng và bán hàng.";
            case CHAM_SOC -> "Quản lý lịch hẹn, thú cưng và tra cứu khách hàng.";
        };
    }

    public boolean receivesAllAppointmentNotifications() {
        return allAppointmentNotifications;
    }

    public boolean receivesOwnAppointmentNotifications() {
        return ownAppointmentNotifications;
    }

    public boolean receivesLowStockNotifications() {
        return lowStockNotifications;
    }

    public static Role fromDbValue(String value) {
        for (Role role : values()) {
            if (role.displayName.equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Phân quyền không hợp lệ: " + value);
    }

    public static Role fromDisplayName(String value) {
        return fromDbValue(value);
    }
}
