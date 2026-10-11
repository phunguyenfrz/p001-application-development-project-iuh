package com.petshop.navigation;

public enum MenuGroup {
    ACCOUNTS("Tài khoản & Nhân sự", "♙"),
    CATALOG("Danh mục", "▤"),
    PRODUCTS_AND_STOCK("Sản phẩm & Kho", "◈"),
    CUSTOMERS_AND_PETS("Khách hàng & Thú cưng", "♡"),
    SALES("Bán hàng", "＄"),
    REPORTS("Thống kê & báo cáo", "▥");

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
