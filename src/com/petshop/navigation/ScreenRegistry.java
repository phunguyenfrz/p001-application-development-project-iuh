package com.petshop.navigation;

import com.petshop.model.Role;
import com.petshop.model.Screen;
import com.petshop.model.Permission;
import com.petshop.view.AppView;
import com.petshop.view.DashboardView;
import com.petshop.view.ModuleViewFactory;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ScreenRegistry {
    private final Map<Screen, ScreenDefinition> definitions;
    private final Function<String, AppView> appointmentInvoiceFactory;

    public ScreenRegistry() {
        this(Map.of(), appointmentId ->
                ModuleViewFactory.create(Screen.INVOICE_CREATE, "Lập hóa đơn"),
                Map.of(), null);
    }

    public ScreenRegistry(
            Map<Screen, Supplier<AppView>> factories,
            Function<String, AppView> appointmentInvoiceFactory) {
        this(factories, appointmentInvoiceFactory, Map.of(), null);
    }

    public ScreenRegistry(
            Map<Screen, Supplier<AppView>> factories,
            Function<String, AppView> appointmentInvoiceFactory,
            Map<Role, Supplier<DashboardView>> dashboardFactories,
            Role currentRole) {
        Objects.requireNonNull(factories, "Danh sách factory không được để trống.");
        Objects.requireNonNull(dashboardFactories, "Danh sách dashboard factory không được để trống.");
        this.appointmentInvoiceFactory = Objects.requireNonNull(
                appointmentInvoiceFactory, "Factory hóa đơn lịch hẹn không được để trống.");
        Map<Screen, ScreenDefinition> registered = new LinkedHashMap<>();
        Supplier<DashboardView> dashboardFactory = currentRole == null
                ? null : dashboardFactories.get(currentRole);

        register(registered, factories, Screen.OVERVIEW, "Tổng quan", null,
                dashboardFactory == null ? null : () -> dashboardFactory.get());
        register(registered, factories, Screen.EMPLOYEES, "Danh sách nhân viên",
                MenuGroup.EMPLOYEES_AND_SHIFTS);
        register(registered, factories, Screen.SHIFTS, "Phân ca trực",
                MenuGroup.EMPLOYEES_AND_SHIFTS);
        register(registered, factories, Screen.SUPPLIERS, "Nhà cung cấp",
                MenuGroup.PRODUCTS_AND_STOCK);
        register(registered, factories, Screen.CARE_SERVICES, "Danh mục dịch vụ",
                MenuGroup.CARE_SERVICES);
        register(registered, factories, Screen.PROMOTIONS, "Khuyến mãi", MenuGroup.SALES);
        register(registered, factories, Screen.REVENUE_REPORT, "Doanh thu", MenuGroup.REPORTS);
        register(registered, factories, Screen.INVENTORY_REPORT, "Tồn kho", MenuGroup.REPORTS);
        register(registered, factories, Screen.BEST_SELLERS_REPORT, "Bán chạy", MenuGroup.REPORTS);
        register(registered, factories, Screen.PRODUCTS, "Sản phẩm", MenuGroup.PRODUCTS_AND_STOCK);
        register(registered, factories, Screen.INVENTORY, "Tồn kho", MenuGroup.PRODUCTS_AND_STOCK);
        register(registered, factories, Screen.PURCHASE_ORDERS, "Đơn nhập", MenuGroup.PRODUCTS_AND_STOCK);
        register(registered, factories, Screen.CUSTOMERS, "Khách hàng",
                MenuGroup.CUSTOMERS_AND_PETS);
        register(registered, factories, Screen.PETS, "Hồ sơ thú cưng",
                MenuGroup.CUSTOMERS_AND_PETS);
        register(registered, factories, Screen.APPOINTMENTS, "Danh sách lịch hẹn",
                MenuGroup.APPOINTMENTS);
        register(registered, factories, Screen.INVOICE_CREATE, "Lập hóa đơn", MenuGroup.SALES);
        register(registered, factories, Screen.INVOICE_HISTORY, "Lịch sử hóa đơn", MenuGroup.SALES);
        register(registered, factories, Screen.RETURNS, "Đổi/trả hàng", MenuGroup.SALES);

        if (registered.size() != Screen.values().length) {
            throw new IllegalStateException("ScreenRegistry chưa đăng ký đủ các màn hình.");
        }
        definitions = Collections.unmodifiableMap(new LinkedHashMap<>(registered));
    }

    public ScreenDefinition definition(Screen screen) {
        ScreenDefinition definition = definitions.get(screen);
        if (definition == null) {
            throw new IllegalArgumentException("Màn hình chưa được đăng ký: " + screen);
        }
        return definition;
    }

    public Map<Screen, ScreenDefinition> definitions() {
        return definitions;
    }

    public static Set<Permission> permissionsFor(Screen screen, Role role) {
        if (screen == null || role == null) {
            return Set.of();
        }
        if (screen == Screen.PETS) {
            return switch (role) {
                case QUAN_LY, CHAM_SOC -> Set.of(Permission.VIEW);
                case BAN_HANG -> EnumSet.allOf(Permission.class);
            };
        }
        return role != null && role.canAccess(screen) ? Set.of(Permission.VIEW) : Set.of();
    }

    public static boolean hasPermission(Screen screen, Role role, Permission permission) {
        return permissionsFor(screen, role).contains(permission);
    }

    public AppView create(Screen screen) {
        return definition(screen).factory().get();
    }

    public AppView createAppointmentInvoice(String appointmentId) {
        return appointmentInvoiceFactory.apply(appointmentId);
    }

    private void register(
            Map<Screen, ScreenDefinition> registry,
            Map<Screen, Supplier<AppView>> factories,
            Screen screen,
            String label,
            MenuGroup group) {
        register(registry, factories, screen, label, group, null);
    }

    private void register(
            Map<Screen, ScreenDefinition> registry,
            Map<Screen, Supplier<AppView>> factories,
            Screen screen,
            String label,
            MenuGroup group,
            Supplier<AppView> registeredFactory) {
        Supplier<AppView> factory = registeredFactory != null
                ? registeredFactory : factories.getOrDefault(screen,
                () -> defaultView(screen, label));
        Objects.requireNonNull(factory, "Factory màn hình không được để trống: " + screen);
        if (registry.putIfAbsent(screen, new ScreenDefinition(screen, label, group, factory)) != null) {
            throw new IllegalStateException("Màn hình đã được đăng ký: " + screen);
        }
    }

    private AppView defaultView(Screen screen, String label) {
        return ModuleViewFactory.create(screen, label);
    }
}
