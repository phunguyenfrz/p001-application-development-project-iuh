package com.petshop.navigation;

import com.petshop.model.Role;
import com.petshop.model.Screen;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScreenRegistryTest {
    @Test
    void registersEveryScreenAndBuildsRoleSpecificMenus() {
        ScreenRegistry registry = new ScreenRegistry();
        assertEquals(Screen.values().length, registry.definitions().size());
        MenuModel manager = new MenuModel(registry, Role.QUAN_LY);
        MenuModel sales = new MenuModel(registry, Role.BAN_HANG);
        MenuModel care = new MenuModel(registry, Role.CHAM_SOC);
        assertEquals("Tài khoản & Nhân sự", manager.sections().get(0).label());
        assertTrue(manager.sections().stream().anyMatch(
                section -> section.label().equals("Thống kê & báo cáo")));
        assertTrue(sales.sections().stream().anyMatch(section -> section.label().equals("Sản phẩm & Kho")));
        assertTrue(care.sections().stream().flatMap(section -> section.entries().stream())
                .anyMatch(entry -> entry.screen() == Screen.APPOINTMENTS));
        assertTrue(care.sections().stream().flatMap(section -> section.entries().stream())
                .noneMatch(entry -> entry.screen() == Screen.INVOICE_CREATE));
        assertEquals(List.of(Screen.EMPLOYEES, Screen.SHIFTS),
                manager.sections().get(0).entries().stream().map(MenuModel.Entry::screen).toList());
    }
}
