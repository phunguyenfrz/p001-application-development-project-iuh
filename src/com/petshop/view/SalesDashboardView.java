package com.petshop.view;

import com.petshop.controller.DashboardController;
import com.petshop.controller.ScreenNavigator;
import com.petshop.model.DashboardData.Appointment;
import com.petshop.model.DashboardData.PendingOrder;
import com.petshop.model.DashboardData.ProductStock;
import com.petshop.model.DashboardData.RecentInvoice;
import com.petshop.model.DashboardData.SalesDashboard;
import com.petshop.model.User;
import com.petshop.util.Formats;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.collections.ObservableList;

import java.util.List;

public final class SalesDashboardView extends VBox implements DashboardView {
    private final ScreenNavigator navigator;

    public SalesDashboardView(
            User user, ScreenNavigator navigator, DashboardController controller) {
        super(22);
        this.navigator = navigator;
        getChildren().add(DashboardWidgets.welcomeHeader(user));
        try {
            compose(controller.loadSalesDashboard());
        } catch (IllegalStateException exception) {
            DashboardWidgets.showLoadError(this, exception);
        }
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
        panel.addHeaderContent(DashboardWidgets.invoiceButton(navigator));
        TableColumn<ObservableList<String>, String> action = new TableColumn<>("Thao tác");
        action.setCellFactory(column -> new TableCell<>() {
            private final Button button = new Button("Lập hóa đơn");

            {
                button.getStyleClass().add("sidebar-btn");
                button.setOnAction(event -> {
                    ObservableList<String> row = getTableView().getItems().get(getIndex());
                    navigator.navigateToAppointmentInvoice(row.get(0));
                });
            }

            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                setGraphic(empty ? null : button);
            }
        });
        panel.table().getColumns().add(action);
        return panel;
    }

    private DataTablePanel recentInvoices(List<RecentInvoice> invoices) {
        DataTablePanel panel = new DataTablePanel("Hóa đơn gần đây",
                List.of("Mã hóa đơn", "Ngày lập", "Khách hàng", "Tổng tiền", "Trạng thái"),
                invoices.stream().map(item -> List.of(item.code(), Formats.date(item.date()),
                        item.customer(), DashboardWidgets.money(item.total()), item.status()))
                        .toList());
        panel.addHeaderContent(DashboardWidgets.invoiceButton(navigator));
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
