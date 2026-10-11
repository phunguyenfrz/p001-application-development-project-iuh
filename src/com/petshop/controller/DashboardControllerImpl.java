package com.petshop.controller;

import com.petshop.dao.DashboardRepository;
import com.petshop.model.DashboardData.Appointment;
import com.petshop.model.DashboardData.CareDashboard;
import com.petshop.model.DashboardData.ManagerDashboard;
import com.petshop.model.DashboardData.ProductStock;
import com.petshop.model.DashboardData.SalesDashboard;
import com.petshop.model.User;

import java.util.List;
import java.util.Objects;

public final class DashboardControllerImpl implements DashboardController {
    private final DashboardRepository dashboardDao;

    public DashboardControllerImpl(DashboardRepository dashboardDao) {
        this.dashboardDao = Objects.requireNonNull(dashboardDao);
    }

    @Override
    public ManagerDashboard loadManagerDashboard() {
        return dashboardDao.loadManagerDashboard();
    }

    @Override
    public SalesDashboard loadSalesDashboard() {
        return dashboardDao.loadSalesDashboard();
    }

    @Override
    public CareDashboard loadCareDashboard(String employeeId) {
        return dashboardDao.loadCareDashboard(employeeId);
    }

    @Override
    public List<ProductStock> loadLowStockNotifications() {
        return dashboardDao.loadLowStockNotifications();
    }

    @Override
    public int loadTodayAppointmentCount() {
        return dashboardDao.loadTodayAppointmentCount();
    }

    @Override
    public List<Appointment> loadMyAppointmentsTodayNotifications(String employeeId) {
        return dashboardDao.loadMyAppointmentsTodayNotifications(employeeId);
    }

    @Override
    public List<String> loadNotifications(User user) {
        Objects.requireNonNull(user, "Phiên đăng nhập không hợp lệ.");
        java.util.ArrayList<String> messages = new java.util.ArrayList<>();
        if (user.getRole().receivesLowStockNotifications()) {
            List<ProductStock> products = dashboardDao.loadLowStockNotifications();
            messages.add("Sản phẩm dưới ngưỡng tồn: " + products.size());
            for (ProductStock product : products) {
                messages.add("• " + product.name() + " (" + product.quantity()
                        + "/" + product.threshold() + ")");
            }
        }
        if (user.getRole().receivesAllAppointmentNotifications()) {
            messages.add("Lịch hẹn hôm nay: " + dashboardDao.loadTodayAppointmentCount());
        }
        if (user.getRole().receivesOwnAppointmentNotifications()) {
            List<Appointment> appointments =
                    dashboardDao.loadMyAppointmentsTodayNotifications(user.getMaNhanVien());
            messages.add("Lịch hẹn hôm nay của bạn: " + appointments.size());
            for (Appointment appointment : appointments) {
                messages.add("• " + appointment.time() + " — " + appointment.pet()
                        + " (" + appointment.status() + ")");
            }
        }
        return List.copyOf(messages);
    }
}
