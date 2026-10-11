package com.petshop.controller;

import com.petshop.model.DashboardData.AllergyPet;
import com.petshop.model.DashboardData.Appointment;
import com.petshop.model.DashboardData.CareDashboard;
import com.petshop.model.DashboardData.ManagerDashboard;
import com.petshop.model.DashboardData.ProductStock;
import com.petshop.model.DashboardData.SalesDashboard;
import com.petshop.model.User;

import java.util.List;

public interface DashboardController {
    ManagerDashboard loadManagerDashboard();

    SalesDashboard loadSalesDashboard();

    CareDashboard loadCareDashboard(String employeeId);

    List<ProductStock> loadLowStockNotifications();

    int loadTodayAppointmentCount();

    List<Appointment> loadMyAppointmentsTodayNotifications(String employeeId);

    List<String> loadNotifications(User user);
}
