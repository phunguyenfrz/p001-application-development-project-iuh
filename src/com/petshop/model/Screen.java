package com.petshop.model;

public enum Screen {
    OVERVIEW,
    /** UC002: quản lý nhân viên, tài khoản và gán vai trò. */
    EMPLOYEES,
    /** UC003: quản lý ca làm việc và phân ca. */
    SHIFTS,
    /** UC004: quản lý nhà cung cấp. */
    SUPPLIERS,
    /** UC005: quản lý dịch vụ chăm sóc và bảng giá theo bậc. */
    CARE_SERVICES,
    /** UC006: quản lý khuyến mãi. */
    PROMOTIONS,
    /** UC007: xem báo cáo doanh thu, tồn kho hoặc mặt hàng bán chạy. */
    REPORTS,
    /** UC009: quản lý sản phẩm. */
    PRODUCTS,
    /** UC010: xem tồn kho và cảnh báo tồn thấp. */
    INVENTORY,
    /** UC011: xem và lập đơn nhập hàng. */
    PURCHASE_ORDERS,
    /** UC012: lịch hôm nay, tra cứu ngày và đặt lịch. */
    APPOINTMENTS,
    /** UC013: quản lý hồ sơ thú cưng. */
    PETS,
    /** UC014: quản lý khách hàng; Chăm sóc chỉ tra cứu/thêm nhanh khi đặt lịch. */
    CUSTOMERS,
    /**
     * UC015: lập hóa đơn. Tài liệu SRS hiện có trong workspace chưa xác minh
     * use case này.
     */
    INVOICE_CREATE,
    /**
     * UC015: lịch sử hóa đơn. Tài liệu SRS hiện có trong workspace chưa xác minh
     * use case này.
     */
    INVOICE_HISTORY,
    /**
     * UC015: đổi/trả hàng. Tài liệu SRS hiện có trong workspace chưa xác minh
     * use case này.
     */
    RETURNS
}
