package com.petshop.view;

import com.petshop.controller.DashboardController;
import com.petshop.model.DashboardData.BestSeller;
import com.petshop.model.DashboardData.ManagerDashboard;
import com.petshop.model.DashboardData.ProductStock;
import com.petshop.model.User;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public final class ManagerDashboardView extends VBox implements DashboardView {
    public ManagerDashboardView(User user, DashboardController controller) {
        super(22);
        getChildren().add(DashboardWidgets.welcomeHeader(user));
        Task<ManagerDashboard> task = new Task<>() {
            @Override
            protected ManagerDashboard call() {
                return controller.loadManagerDashboard();
            }
        };
        task.setOnSucceeded(event -> compose(task.getValue()));
        task.setOnFailed(event -> DashboardWidgets.showLoadError(this,
                new IllegalStateException("Không thể tải dữ liệu tổng quan.",
                        task.getException())));
        Thread worker = new Thread(task, "manager-dashboard-load");
        worker.setDaemon(true);
        worker.start();
    }

    private void compose(ManagerDashboard data) {
        HBox metrics = new HBox(16,
                new MetricCard("Doanh thu hôm nay",
                        DashboardWidgets.money(data.revenueToday()), true),
                new MetricCard("Số lịch hẹn hôm nay",
                        Integer.toString(data.appointmentsToday()), false),
                new MetricCard("Sản phẩm dưới ngưỡng tồn",
                        Integer.toString(data.lowStock().size()), false));
        DashboardWidgets.grow(metrics);
        HBox analysis = new HBox(20);
        RevenueChart chart = new RevenueChart(data.weeklyRevenue(), data.revenueToday());
        DataTablePanel stock = stockPanel(data.lowStock());
        stock.setPrefWidth(370);
        HBox.setHgrow(chart, Priority.ALWAYS);
        analysis.getChildren().addAll(chart, stock);
        getChildren().addAll(metrics, analysis, bestSellers(data.bestSellers()));
    }

    private DataTablePanel stockPanel(List<ProductStock> products) {
        List<List<String>> rows = products.stream()
                .map(item -> List.of(item.name(), item.quantity() + " / " + item.threshold()))
                .toList();
        return new DataTablePanel("Sản phẩm dưới ngưỡng tồn",
                List.of("Sản phẩm", "Tồn / Ngưỡng"), rows);
    }

    private DataTablePanel bestSellers(List<BestSeller> items) {
        List<List<String>> rows = items.stream()
                .map(item -> List.of(item.type(), item.name(), Integer.toString(item.quantity())))
                .toList();
        return new DataTablePanel("Top dịch vụ/sản phẩm bán chạy",
                List.of("Loại", "Tên", "Đã bán"), rows);
    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public String getTitle() {
        return "Tổng quan";
    }
}
