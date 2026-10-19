package com.petshop.controller;

import com.petshop.model.ImportOrder;
import com.petshop.service.ImportOrderService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Lớp Controller điều phối các thao tác từ giao diện Đơn hàng nhập
 * xuống tầng nghiệp vụ (ImportOrderService) theo kiến trúc n-Tier.
 */
public class ImportOrderController {

    private final ImportOrderService importOrderService;

    public ImportOrderController() {
        this.importOrderService = new ImportOrderService();
    }

    public ImportOrderController(ImportOrderService service) {
        this.importOrderService = service != null ? service : new ImportOrderService();
    }

    /**
     * Sinh mã đơn hàng nhập tiếp theo: DH-YYYYMMDD-NNNNNN.
     */
    public String generateNextOrderId() {
        return importOrderService.generateNextOrderId();
    }

    /**
     * Lập đơn hàng nhập mới (IMP_CREATE).
     */
    public void createImportOrder(ImportOrder order) {
        importOrderService.createImportOrder(order);
    }

    /**
     * Xác nhận nhận hàng và cập nhật tồn kho (IMP_CONFIRM / IMP_RECEIVE).
     */
    public void confirmReceiveOrder(String maDonHang) {
        importOrderService.confirmReceiveOrder(maDonHang);
    }

    /**
     * Hủy đơn hàng nhập (xóa mềm - IMP_CANCEL).
     */
    public void cancelImportOrder(String maDonHang) {
        importOrderService.cancelImportOrder(maDonHang);
    }

    /**
     * Cập nhật thông tin đơn hàng nhập (IMP_UPDATE).
     */
    public void updateImportOrder(ImportOrder order) {
        importOrderService.updateImportOrder(order);
    }

    /**
     * Tìm kiếm và lọc danh sách đơn hàng nhập (IMP_SEARCH).
     */
    public List<ImportOrder> searchImportOrders(String keyword, String status, LocalDate fromDate, LocalDate toDate) {
        return importOrderService.searchImportOrders(keyword, status, fromDate, toDate);
    }

    /**
     * Lấy danh sách toàn bộ đơn hàng nhập.
     */
    public List<ImportOrder> getAllImportOrders() {
        return importOrderService.searchImportOrders("", null, null, null);
    }

    /**
     * Lấy thông tin chi tiết một đơn hàng nhập.
     */
    public Optional<ImportOrder> getOrderById(String maDonHang) {
        return importOrderService.getOrderById(maDonHang);
    }
}

