package com.petshop.navigation;

import com.petshop.model.Role;
import com.petshop.model.Screen;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScreenRegistryTest {
    @Test
    void registersEveryScreenAndBuildsRoleSpecificMenus() {
        ScreenRegistry registry = new ScreenRegistry();
        assertEquals(Screen.values().length, registry.definitions().size());
        MenuModel menu = new MenuModel(registry);
        List<MenuModel.Section> full = menu.fullMenu();
        List<MenuModel.Section> manager = menu.forRole(Role.QUAN_LY);
        List<MenuModel.Section> sales = menu.forRole(Role.BAN_HANG);
        List<MenuModel.Section> care = menu.forRole(Role.CHAM_SOC);
        assertEquals(8, full.size());
        assertEquals("Tổng quan", full.get(0).entries().get(0).label());
        assertEquals("Nhân viên & Ca trực", manager.get(1).label());
        assertEquals(8, manager.size());
        assertTrue(sales.stream().anyMatch(section -> section.label().equals("Sản phẩm & Kho")));
        assertTrue(care.stream().flatMap(section -> section.entries().stream())
                .anyMatch(entry -> entry.screen() == Screen.APPOINTMENTS));
        assertTrue(care.stream().flatMap(section -> section.entries().stream())
                .noneMatch(entry -> entry.screen() == Screen.INVOICE_CREATE));
        assertTrue(sales.stream().flatMap(section -> section.entries().stream())
                .noneMatch(entry -> Set.of(Screen.EMPLOYEES, Screen.SHIFTS, Screen.SUPPLIERS,
                        Screen.PROMOTIONS, Screen.REVENUE_REPORT, Screen.INVENTORY_REPORT,
                        Screen.BEST_SELLERS_REPORT).contains(entry.screen())));
        assertEquals(1, care.stream().filter(section -> section.group() == null).count());
        assertEquals(2, care.stream().filter(
                section -> section.label().equals("Lịch hẹn")).findFirst()
                .orElseThrow().entries().size());
        assertEquals(3, manager.stream().filter(
                section -> section.label().equals("Báo cáo")).findFirst()
                .orElseThrow().entries().size());
    }
}
