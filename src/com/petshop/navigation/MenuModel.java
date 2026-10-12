package com.petshop.navigation;

import com.petshop.model.Role;
import com.petshop.model.Screen;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class MenuModel {
    public record Entry(String label, Screen screen, Set<Role> visibleTo) {
        public Entry {
            visibleTo = Set.copyOf(visibleTo);
        }

        public boolean isVisibleTo(Role role) {
            return visibleTo.contains(role);
        }
    }

    public record Section(MenuGroup group, List<Entry> entries) {
        public Section {
            entries = List.copyOf(entries);
        }

        public String label() {
            return group == null ? entries.get(0).label() : group.label();
        }
    }

    private final List<Section> fullMenu;
    private final ScreenRegistry registry;

    public MenuModel(ScreenRegistry registry) {
        this.registry = registry;
        List<Section> sections = new ArrayList<>();
        sections.add(new Section(null, List.of(
                entry("Tổng quan", Screen.OVERVIEW, Role.values()))));
        addSection(sections, MenuGroup.EMPLOYEES_AND_SHIFTS,
                entry("Danh sách nhân viên", Screen.EMPLOYEES, Role.QUAN_LY),
                entry("Phân ca trực", Screen.SHIFTS, Role.QUAN_LY));
        addSection(sections, MenuGroup.CUSTOMERS_AND_PETS,
                entry("Khách hàng", Screen.CUSTOMERS, Role.BAN_HANG, Role.CHAM_SOC),
                entry("Hồ sơ thú cưng", Screen.PETS, Role.BAN_HANG, Role.CHAM_SOC));
        addSection(sections, MenuGroup.CARE_SERVICES,
                entry("Danh mục dịch vụ", Screen.CARE_SERVICES, Role.QUAN_LY));
        addSection(sections, MenuGroup.APPOINTMENTS,
                entry("Danh sách lịch hẹn", Screen.APPOINTMENTS, Role.CHAM_SOC),
                entry("Đặt lịch hẹn", Screen.APPOINTMENTS, Role.CHAM_SOC));
        addSection(sections, MenuGroup.PRODUCTS_AND_STOCK,
                entry("Sản phẩm", Screen.PRODUCTS, Role.BAN_HANG),
                entry("Tồn kho", Screen.INVENTORY, Role.BAN_HANG),
                entry("Nhà cung cấp", Screen.SUPPLIERS, Role.QUAN_LY),
                entry("Đơn nhập hàng", Screen.PURCHASE_ORDERS, Role.BAN_HANG));
        addSection(sections, MenuGroup.SALES,
                entry("Lập hóa đơn", Screen.INVOICE_CREATE, Role.BAN_HANG),
                entry("Khuyến mãi", Screen.PROMOTIONS, Role.QUAN_LY));
        addSection(sections, MenuGroup.REPORTS,
                entry("Doanh thu", Screen.REVENUE_REPORT, Role.QUAN_LY),
                entry("Tồn kho", Screen.INVENTORY_REPORT, Role.QUAN_LY),
                entry("Bán chạy", Screen.BEST_SELLERS_REPORT, Role.QUAN_LY));
        fullMenu = List.copyOf(sections);
    }

    public List<Section> fullMenu() {
        return fullMenu;
    }

    public List<Section> forRole(Role role) {
        List<Section> filtered = new ArrayList<>();
        for (Section section : fullMenu()) {
            List<Entry> entries = section.entries().stream()
                    .filter(entry -> entry.isVisibleTo(role))
                    .filter(entry -> role.canAccess(entry.screen()))
                    .toList();
            if (!entries.isEmpty()) {
                filtered.add(new Section(section.group(), entries));
            }
        }
        return List.copyOf(filtered);
    }

    private Entry entry(String label, Screen screen, Role... roles) {
        registry.definition(screen);
        Set<Role> visibleRoles = EnumSet.of(Role.QUAN_LY, roles);
        return new Entry(label, screen, visibleRoles);
    }

    private void addSection(
            List<Section> sections, MenuGroup group, Entry... entries) {
        sections.add(new Section(group, List.of(entries)));
    }
}
