package com.petshop.controller;

import com.petshop.dao.InMemorySupplierDao;
import com.petshop.model.NhaCungCap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SupplierControllerTest {
    @Test
    void performsCrudOperationsAgainstTheInMemoryDao() {
        SupplierController controller = new SupplierController(new InMemorySupplierDao());
        assertEquals(2, controller.findAll().size());

        NhaCungCap created = new NhaCungCap(
                "NCC-DEMO-003", "Nhà cung cấp mới", "0901234567", "Quận 5", false);
        controller.add(created);
        assertEquals(1, controller.search("mới").size());
        controller.update(new NhaCungCap(
                created.getMaNhaCungCap(), "Nhà cung cấp cập nhật",
                created.getSoDienThoai(), created.getDiaChi(), false));
        assertEquals("Nhà cung cấp cập nhật",
                controller.search("cập nhật").get(0).getTenNhaCungCap());
        controller.deactivate(created.getMaNhaCungCap());
        assertTrue(controller.findAll().stream()
                .filter(item -> item.getMaNhaCungCap().equals(created.getMaNhaCungCap()))
                .findFirst().orElseThrow().isNgungHoatDong());
    }
}
