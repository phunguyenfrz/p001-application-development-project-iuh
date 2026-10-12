package com.petshop.view;

import com.petshop.controller.DashboardController;
import com.petshop.model.DashboardData.Appointment;
import com.petshop.model.DashboardData.PendingOrder;
import com.petshop.model.DashboardData.ProductStock;
import com.petshop.model.DashboardData.RecentInvoice;
import com.petshop.model.DashboardData.SalesDashboard;
import com.petshop.model.User;
import com.petshop.util.Formats;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public final class SalesDashboardView extends VBox implements DashboardView {
    public SalesDashboardView(User user, DashboardController controller) {
        super(22);
        getChildren().add(DashboardWidgets.welcomeHeader(user));
        Task<SalesDashboard> task = new Task<>() {
            @Override
            protected SalesDashboard call() {
                return controller.loadSalesDashboard();
            }
        };
        task.setOnSucceeded(event -> compose(task.getValue()));
        task.setOnFailed(event -> DashboardWidgets.showLoadError(this,
                new IllegalStateException("Không thể tải dữ liệu tổng quan.",
                        task.getException())));
        Thread worker = new Thread(task, "sales-dashboard-load");
        worker.setDaemon(true);
        worker.start();
    }

    private void compose(SalesDashboard data) {
        HBox metrics = new HBox(16,
                new MetricCard("Số hóa đơn hôm nay", Integer.toString(data.invoicesToday()), true),
                new MetricCard("Doanh thu bán hàng hôm nay",
                        DashboardWidgets.money(data.revenueToday()), false),
                new MetricCard("Sản phẩm dưới ngưỡng tồn",
                        Integer.toString(data.lowStock().size()), false),
                new MetricCard("Đơn nhập đang chờ nhận hàng",
                        Integer.toString(data.pendingOrderCount()), false));
        DashboardWidgets.grow(metrics);
        HBox stockAndOrders = new HBox(20,
                stockPanel(data.lowStock()), pendingOrders(data.pendingOrders()));
        DashboardWidgets.grow(stockAndOrders);
        getChildren().addAll(metrics, unpaidAppointments(data.unpaidAppointments()),
                stockAndOrders, recentInvoices(data.recentInvoices()));
    }

    private DataTablePanel stockPanel(List<ProductStock> products) {
        return new DataTablePanel("Sản phẩm dưới ngưỡng tồn",
                List.of("Sản phẩm", "Tồn / Ngưỡng"), products.stream()
                .map(item -> List.of(item.name(), item.quantity() + " / " + item.threshold()))
                .toList());
    }

    private DataTablePanel pendingOrders(List<PendingOrder> orders) {
        return new DataTablePanel("Đơn nhập đang chờ nhận hàng",
                List.of("Mã đơn", "Nhà cung cấp", "Tổng tiền"), orders.stream()
                .map(item -> List.of(item.code(), item.supplier(),
                        DashboardWidgets.money(item.total())))
                .toList());
    }

    private DataTablePanel unpaidAppointments(List<Appointment> appointments) {
        DataTablePanel panel = new DataTablePanel("Lịch hẹn hoàn thành chờ thanh toán",
                List.of("Mã lịch", "Giờ", "Thú cưng", "Dịch vụ"), appointments.stream()
                .map(item -> List.of(item.code(), DashboardWidgets.time(item.time()),
                        item.pet(), item.services())).toList());
        return panel;
    }

    private DataTablePanel recentInvoices(List<RecentInvoice> invoices) {
        DataTablePanel panel = new DataTablePanel("Hóa đơn gần đây",
                List.of("Mã hóa đơn", "Ngày lập", "Khách hàng", "Tổng tiền", "Trạng thái"),
                invoices.stream().map(item -> List.of(item.code(), Formats.date(item.date()),
                        item.customer(), DashboardWidgets.money(item.total()), item.status()))
                        .toList());
        return panel;
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
