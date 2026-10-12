package com.petshop.dao;

import com.petshop.model.NhaCungCap;
import com.petshop.util.TextSearch;

import java.util.ArrayList;
import java.util.List;

public final class InMemorySupplierDao implements CrudDao<NhaCungCap, String> {
    private final List<NhaCungCap> suppliers = new ArrayList<>(List.of(
            new NhaCungCap("NCC-DEMO-001", "Công ty Thức ăn Thú cưng", "0901234567",
                    "Quận 1, TP. Hồ Chí Minh", false),
            new NhaCungCap("NCC-DEMO-002", "Phụ kiện Happy Pets", "0912345678",
                    "Quận 3, TP. Hồ Chí Minh", false)));

    @Override
    public List<NhaCungCap> findAll() {
        return List.copyOf(suppliers);
    }

    @Override
    public List<NhaCungCap> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return suppliers.stream()
                .filter(supplier -> TextSearch.matchesAll(keyword,
                        supplier.getMaNhaCungCap(), supplier.getTenNhaCungCap(),
                        supplier.getSoDienThoai()))
                .toList();
    }

    @Override
    public void add(NhaCungCap supplier) {
        if (suppliers.stream().anyMatch(current ->
                current.getMaNhaCungCap().equals(supplier.getMaNhaCungCap()))) {
            throw new IllegalArgumentException("Mã nhà cung cấp đã tồn tại.");
        }
        suppliers.add(supplier);
    }

    @Override
    public void update(NhaCungCap supplier) {
        int index = indexOf(supplier.getMaNhaCungCap());
        if (index < 0) {
            throw new IllegalArgumentException("Không tìm thấy nhà cung cấp cần cập nhật.");
        }
        suppliers.set(index, supplier);
    }

    @Override
    public void deactivate(String id) {
        int index = indexOf(id);
        if (index < 0) {
            throw new IllegalArgumentException("Không tìm thấy nhà cung cấp cần ngừng.");
        }
        suppliers.set(index, suppliers.get(index).ngungHoatDong());
    }

    private int indexOf(String id) {
        for (int index = 0; index < suppliers.size(); index++) {
            if (suppliers.get(index).getMaNhaCungCap().equals(id)) {
                return index;
            }
        }
        return -1;
    }
}
