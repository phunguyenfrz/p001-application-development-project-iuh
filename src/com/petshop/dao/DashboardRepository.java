package com.petshop.dao;

import com.petshop.model.DashboardData.Appointment;
import com.petshop.model.DashboardData.CareDashboard;
import com.petshop.model.DashboardData.ManagerDashboard;
import com.petshop.model.DashboardData.ProductStock;
import com.petshop.model.DashboardData.SalesDashboard;

import java.util.List;

public interface DashboardRepository {
    ManagerDashboard loadManagerDashboard();

    SalesDashboard loadSalesDashboard();

    CareDashboard loadCareDashboard(String employeeId);

    List<ProductStock> loadLowStockNotifications();

    int loadTodayAppointmentCount();

    List<Appointment> loadMyAppointmentsTodayNotifications(String employeeId);
}
