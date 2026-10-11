package com.petshop.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DashboardData {
    private DashboardData() {
    }

    public record ProductStock(String code, String name, int quantity, int threshold) {
    }

    public record BestSeller(String type, String name, int quantity) {
    }

    public record Appointment(String code, LocalTime time, String pet, String services, String status) {
    }

    public record AllergyPet(String pet, String type, String allergy, String customer) {
    }

    public record PendingOrder(String code, String supplier, BigDecimal total) {
    }

    public record RecentInvoice(String code, LocalDate date, String customer,
                                BigDecimal total, String status) {
    }

    public record ManagerDashboard(
            BigDecimal revenueToday,
            Map<LocalDate, BigDecimal> weeklyRevenue,
            int appointmentsToday,
            List<ProductStock> lowStock,
            List<BestSeller> bestSellers) {
        public ManagerDashboard {
            weeklyRevenue = Collections.unmodifiableMap(new LinkedHashMap<>(weeklyRevenue));
            lowStock = List.copyOf(lowStock);
            bestSellers = List.copyOf(bestSellers);
        }
    }

    public record SalesDashboard(
            int invoicesToday,
            BigDecimal revenueToday,
            List<ProductStock> lowStock,
            int pendingOrderCount,
            List<PendingOrder> pendingOrders,
            List<Appointment> unpaidAppointments,
            List<RecentInvoice> recentInvoices) {
        public SalesDashboard {
            lowStock = List.copyOf(lowStock);
            pendingOrders = List.copyOf(pendingOrders);
            unpaidAppointments = List.copyOf(unpaidAppointments);
            recentInvoices = List.copyOf(recentInvoices);
        }
    }

    public record CareDashboard(
            List<Appointment> appointmentsToday,
            int awaitingConfirmation,
            List<AllergyPet> petsWithAllergiesToday) {
        public CareDashboard {
            appointmentsToday = List.copyOf(appointmentsToday);
            petsWithAllergiesToday = List.copyOf(petsWithAllergiesToday);
        }
    }
}
