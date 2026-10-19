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
     * Thêm mới nhà cung cấp.
     */
    public void createSupplier(Supplier supplier) {
        supplierService.addSupplier(supplier);
    }

    /**
     * Cập nhật thông tin nhà cung cấp.
     */
    public void modifySupplier(Supplier supplier) {
        supplierService.updateSupplier(supplier);
    }

    /**
     * Xóa mềm nhà cung cấp (chuyển trạng thái sang Ngừng hoạt động).
     */
    public void removeSupplierSoftly(String maNhaCungCap) {
        supplierService.deactivateSupplier(maNhaCungCap);
    }
}
