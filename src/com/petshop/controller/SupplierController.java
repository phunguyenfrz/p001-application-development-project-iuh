package com.petshop.controller;

import com.petshop.model.Supplier;
import com.petshop.service.SupplierService;

import java.util.List;

/**
 * Lớp Controller điều phối các thao tác từ giao diện Nhà cung cấp (SupplierView, SupplierPickerDialog)
 * xuống tầng nghiệp vụ (SupplierService) theo kiến trúc n-Tier.
 */
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController() {
        this.supplierService = new SupplierService();
    }

    /**
     * Lấy toàn bộ danh sách nhà cung cấp.
     */
    public List<Supplier> loadSuppliers() {
        return supplierService.searchSuppliers("", null);
    }

    /**
     * Lấy danh sách nhà cung cấp đang hợp tác (trangThai = 1) cho trang Cập nhật.
     */
    public List<Supplier> loadActiveSuppliers() {
        return supplierService.searchSuppliers("", true);
    }

    /**
     * Thêm mới nhà cung cấp.
     */
    public void addSupplier(Supplier supplier) {
        supplierService.addSupplier(supplier);
    }

    /**
     * Cập nhật thông tin nhà cung cấp.
     */
    public void updateSupplier(Supplier supplier) {
        supplierService.updateSupplier(supplier);
    }

    /**
     * Ngừng hợp tác nhà cung cấp bằng xóa mềm.
     */
    public void deleteSupplier(String supplierId) {
        supplierService.deactivateSupplier(supplierId);
    }

    /**
     * Tìm kiếm hoặc tải danh sách nhà cung cấp theo từ khóa và bộ lọc trạng thái.
     */
    public List<Supplier> searchSuppliers(String keyword, Boolean activeFilter) {
        return supplierService.searchSuppliers(keyword, activeFilter);
    }

    /**
     * Sinh mã nhà cung cấp tiếp theo theo định dạng NCC-YYYYMMDD-NNNNNN.
     */
    public String generateNewSupplierId() {
        return supplierService.generateNextSupplierId();
    }

    /**
     * Thêm mới nhà cung cấp (tương thích).
     */
    public void createSupplier(Supplier supplier) {
        addSupplier(supplier);
    }

    /**
     * Cập nhật thông tin nhà cung cấp (tương thích).
     */
    public void modifySupplier(Supplier supplier) {
        updateSupplier(supplier);
    }

    /**
     * Xóa mềm nhà cung cấp (tương thích).
     */
    public void removeSupplierSoftly(String maNhaCungCap) {
        deleteSupplier(maNhaCungCap);
    }
}
