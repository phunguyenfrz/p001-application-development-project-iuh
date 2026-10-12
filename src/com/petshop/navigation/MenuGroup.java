package com.petshop.navigation;

public enum MenuGroup {
    EMPLOYEES_AND_SHIFTS("Nhân viên & Ca trực", "♙"),
    CUSTOMERS_AND_PETS("Khách hàng & Thú cưng", "♡"),
    CARE_SERVICES("Dịch vụ chăm sóc", "✿"),
    APPOINTMENTS("Lịch hẹn", "◷"),
    PRODUCTS_AND_STOCK("Sản phẩm & Kho", "◈"),
    SALES("Bán hàng", "＄"),
    REPORTS("Báo cáo", "▥");

    private final String label;
    private final String icon;

    MenuGroup(String label, String icon) {
        this.label = label;
        this.icon = icon;
    }

    public String label() {
        return label;
    }

    public String icon() {
        return icon;
    }
}
