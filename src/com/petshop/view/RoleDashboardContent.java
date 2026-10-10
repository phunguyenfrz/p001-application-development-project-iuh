package com.petshop.view;

import com.petshop.controller.NavigationController;
import com.petshop.dao.DashboardDao;
import com.petshop.dao.DashboardDao.AllergyPet;
import com.petshop.dao.DashboardDao.Appointment;
import com.petshop.dao.DashboardDao.BestSeller;
import com.petshop.dao.DashboardDao.PendingOrder;
import com.petshop.dao.DashboardDao.ProductStock;
import com.petshop.dao.DashboardDao.RecentInvoice;
import com.petshop.model.User;
import com.petshop.util.RoleMenuConfig;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RoleDashboardContent extends VBox {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final NavigationController navigationController;
    private final User user;

    public RoleDashboardContent(User user, NavigationController navigationController) {
        super(22);
        this.user = user;
        this.navigationController = navigationController;
        try {
            DashboardDao dao = new DashboardDao();
            switch (user.getRole()) {
                case QUAN_LY -> getChildren().addAll(managerContent(dao.loadManagerDashboard()));
                case BAN_HANG -> getChildren().addAll(salesContent(dao.loadSalesDashboard()));
                case CHAM_SOC -> getChildren().addAll(
                        careContent(dao.loadCareDashboard(user.getMaNhanVien())));
            }
        } catch (IllegalStateException exception) {
            showLoadError(exception);
        }
    }

    private List<Node> managerContent(DashboardDao.ManagerDashboard data) {
        HBox metrics = new HBox(16,
                metric("Doanh thu hôm nay", money(data.revenueToday()), true),
                metric("Số lịch hẹn hôm nay", Integer.toString(data.appointmentsToday()), false),
                metric("Sản phẩm dưới ngưỡng tồn", Integer.toString(data.lowStock().size()), false));
        growChildren(metrics);

        HBox analysis = new HBox(20);
        VBox chart = revenueChart(data.weeklyRevenue(), data.revenueToday());
        VBox stock = stockPanel(data.lowStock());
        stock.setPrefWidth(370);
        HBox.setHgrow(chart, Priority.ALWAYS);
        analysis.getChildren().addAll(chart, stock);

        return List.of(metrics, analysis, bestSellersPanel(data.bestSellers()));
    }

    private List<Node> salesContent(DashboardDao.SalesDashboard data) {
        HBox metrics = new HBox(16,
                metric("Số hóa đơn hôm nay", Integer.toString(data.invoicesToday()), true),
                metric("Doanh thu bán hàng hôm nay", money(data.revenueToday()), false),
                metric("Sản phẩm dưới ngưỡng tồn", Integer.toString(data.lowStock().size()), false),
                metric("Đơn nhập đang chờ nhận hàng",
                        Integer.toString(data.pendingOrderCount()), false));
        growChildren(metrics);

        HBox stockAndOrders = new HBox(20,
                stockPanel(data.lowStock()), pendingOrdersPanel(data.pendingOrders()));
        growChildren(stockAndOrders);
        return List.of(metrics, unpaidAppointmentsPanel(data.unpaidAppointments()),
                stockAndOrders, recentInvoicesPanel(data.recentInvoices()));
    }

    private List<Node> careContent(DashboardDao.CareDashboard data) {
        HBox count = new HBox(16,
                metric("Số lịch chờ xác nhận", Integer.toString(data.awaitingConfirmation()), true));
        growChildren(count);
        return List.of(careAppointmentsPanel(data.appointmentsToday()), count,
                allergyPetsPanel(data.petsWithAllergiesToday()));
    }

    private VBox metric(String title, String value, boolean featured) {
        VBox card = new VBox(8);
        card.getStyleClass().add(featured ? "stat-card-featured" : "stat-card");
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add(featured ? "stat-title-white" : "stat-title");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add(featured ? "stat-value-white" : "stat-value");
        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    private VBox revenueChart(Map<LocalDate, BigDecimal> revenueByDay, BigDecimal revenueToday) {
        VBox panel = panel();
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(3);
        Label title = new Label("Doanh thu tuần này");
        title.getStyleClass().add("card-title");
        BigDecimal total = revenueByDay.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        Label subtitle = new Label("Tổng: " + money(total));
        subtitle.getStyleClass().add("card-sub");
        titleBox.getChildren().addAll(title, subtitle);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label badge = new Label("● Tuần hiện tại");
        badge.getStyleClass().add("status-badge-paid");
        header.getChildren().addAll(titleBox, spacer, badge);

        LocalDate monday = revenueByDay.keySet().stream().min(LocalDate::compareTo)
                .orElse(LocalDate.now().with(java.time.DayOfWeek.MONDAY));
        BigDecimal max = revenueByDay.values().stream().max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        String[] dayLabels = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        HBox chart = new HBox(24);
        chart.setAlignment(Pos.BOTTOM_CENTER);
        chart.setPadding(new Insets(24, 16, 8, 16));
        for (int index = 0; index < dayLabels.length; index++) {
            LocalDate date = monday.plusDays(index);
            BigDecimal value = revenueByDay.getOrDefault(date, BigDecimal.ZERO);
            boolean today = date.equals(LocalDate.now());
            double height = max.signum() == 0 ? 4
                    : Math.max(4, value.doubleValue() / max.doubleValue() * 150);
            VBox barColumn = new VBox(8);
            barColumn.setAlignment(Pos.BOTTOM_CENTER);
            if (today) {
                Label todayValue = new Label(money(revenueToday));
                todayValue.setStyle("-fx-font-size: 10px; -fx-text-fill: #2f4f38; -fx-font-weight: bold;");
                barColumn.getChildren().add(todayValue);
            }
            Rectangle bar = new Rectangle(36, height);
            bar.setArcWidth(8);
            bar.setArcHeight(8);
            bar.setFill(today ? Color.web("#2f4f38") : Color.web("#bdd7c3"));
            Tooltip.install(bar, new Tooltip(dayLabels[index] + " - " + money(value)));
            Label day = new Label(dayLabels[index]);
            day.setStyle(today
                    ? "-fx-font-size: 11.5px; -fx-text-fill: #2f4f38; -fx-font-weight: bold;"
                    : "-fx-font-size: 11.5px; -fx-text-fill: #829485; -fx-font-weight: 600;");
            barColumn.getChildren().addAll(bar, day);
            chart.getChildren().add(barColumn);
            HBox.setHgrow(barColumn, Priority.ALWAYS);
        }
        panel.getChildren().addAll(header, chart);
        return panel;
    }

    private VBox stockPanel(List<ProductStock> products) {
        List<List<String>> rows = products.stream()
                .map(item -> List.of(item.name(), item.quantity() + " / " + item.threshold()))
                .toList();
        return tablePanel("Sản phẩm dưới ngưỡng tồn",
                List.of("Sản phẩm", "Tồn / Ngưỡng"), rows);
    }

    private VBox bestSellersPanel(List<BestSeller> items) {
        List<List<String>> rows = items.stream()
                .map(item -> List.of(item.type(), item.name(), Integer.toString(item.quantity())))
                .toList();
        return tablePanel("Top dịch vụ/sản phẩm bán chạy",
                List.of("Loại", "Tên", "Đã bán"), rows);
    }

    private VBox pendingOrdersPanel(List<PendingOrder> orders) {
        List<List<String>> rows = orders.stream()
                .map(order -> List.of(order.code(), order.supplier(), money(order.total())))
                .toList();
        return tablePanel("Đơn nhập đang chờ nhận hàng",
                List.of("Mã đơn", "Nhà cung cấp", "Tổng tiền"), rows);
    }

    private VBox unpaidAppointmentsPanel(List<Appointment> appointments) {
        VBox panel = panel();
        HBox header = panelHeader("Lịch hẹn hoàn thành chờ thanh toán");
        Button createInvoice = invoiceButton();
        header.getChildren().add(createInvoice);
        TableView<ObservableList<String>> table = dataTable(
                List.of("Mã lịch", "Giờ", "Thú cưng", "Dịch vụ"),
                appointments.stream()
                        .map(item -> List.of(item.code(), time(item.time()), item.pet(), item.services()))
                        .toList());
        TableColumn<ObservableList<String>, String> action = new TableColumn<>("Thao tác");
        action.setCellFactory(column -> new TableCell<>() {
            private final Button button = new Button("Lập hóa đơn");

            {
                button.getStyleClass().add("sidebar-btn");
                button.setOnAction(event -> {
                    ObservableList<String> selected = getTableView().getItems().get(getIndex());
                    navigationController.navigateToAppointmentInvoice(selected.get(0));
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : button);
            }
        });
        table.getColumns().add(action);
        panel.getChildren().addAll(header, table);
        return panel;
    }

    private VBox recentInvoicesPanel(List<RecentInvoice> invoices) {
        VBox panel = panel();
        HBox header = panelHeader("Hóa đơn gần đây");
        header.getChildren().add(invoiceButton());
        List<List<String>> rows = invoices.stream()
                .map(invoice -> List.of(invoice.code(), invoice.date().format(DATE_FORMAT),
                        invoice.customer(), money(invoice.total()), invoice.status()))
                .toList();
        panel.getChildren().addAll(header, dataTable(
                List.of("Mã hóa đơn", "Ngày lập", "Khách hàng", "Tổng tiền", "Trạng thái"), rows));
        return panel;
    }

    private VBox careAppointmentsPanel(List<Appointment> appointments) {
        List<List<String>> rows = appointments.stream()
                .map(item -> List.of(time(item.time()), item.pet(), item.services(), item.status()))
                .toList();
        return tablePanel("Lịch của tôi hôm nay",
                List.of("Giờ", "Thú cưng", "Dịch vụ", "Trạng thái"), rows);
    }

    private VBox allergyPetsPanel(List<AllergyPet> pets) {
        List<List<String>> rows = pets.stream()
                .map(item -> List.of(item.pet(), item.type(), item.allergy(), item.customer()))
                .toList();
        return tablePanel("Thú cưng hôm nay có ghi chú dị ứng/bệnh nền",
                List.of("Thú cưng", "Loài", "Ghi chú", "Chủ nuôi"), rows);
    }

    private VBox tablePanel(String title, List<String> columns, List<List<String>> rows) {
        VBox panel = panel();
        Label heading = new Label(title);
        heading.getStyleClass().add("card-title");
        panel.getChildren().addAll(heading, dataTable(columns, rows));
        return panel;
    }

    private VBox panel() {
        VBox panel = new VBox(12);
        panel.getStyleClass().add("card-panel");
        return panel;
    }

    private HBox panelHeader(String title) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label(title);
        label.getStyleClass().add("card-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(label, spacer);
        return header;
    }

    private TableView<ObservableList<String>> dataTable(
            List<String> columnNames, List<List<String>> rows) {
        TableView<ObservableList<String>> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(Math.min(320, Math.max(125, 38 + rows.size() * 34)));
        for (int index = 0; index < columnNames.size(); index++) {
            int columnIndex = index;
            TableColumn<ObservableList<String>, String> column =
                    new TableColumn<>(columnNames.get(index));
            column.setCellValueFactory(cell -> new SimpleStringProperty(
                    cell.getValue().get(columnIndex)));
            table.getColumns().add(column);
        }
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        for (List<String> row : rows) {
            data.add(FXCollections.observableArrayList(row));
        }
        table.setItems(data);
        table.setPlaceholder(new Label("Không có dữ liệu."));
        return table;
    }

    private Button invoiceButton() {
        Button button = new Button("+ Tạo hóa đơn mới");
        button.getStyleClass().add("btn-create-invoice");
        button.setVisible(RoleMenuConfig.canAccess(
                user.getRole(), RoleMenuConfig.Screen.INVOICE_CREATE));
        button.setManaged(button.isVisible());
        button.setOnAction(event -> navigationController.navigate(
                RoleMenuConfig.Screen.INVOICE_CREATE, "Lập hóa đơn mới"));
        return button;
    }

    private void showLoadError(IllegalStateException exception) {
        Label error = new Label("Không thể tải dữ liệu từ QLCH_HappyTails: "
                + exception.getMessage());
        error.setWrapText(true);
        error.getStyleClass().add("welcome-sub");
        VBox errorPanel = panel();
        errorPanel.getChildren().add(error);
        getChildren().add(errorPanel);
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi tải bảng điều khiển");
            alert.setHeaderText("Không thể kết nối hoặc truy vấn dữ liệu.");
            alert.setContentText(exception.getMessage());
            alert.show();
        });
    }

    private String money(BigDecimal amount) {
        return NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"))
                .format(amount) + " đ";
    }

    private String time(LocalTime value) {
        return value.format(TIME_FORMAT);
    }

    private void growChildren(HBox row) {
        for (Node child : row.getChildren()) {
            HBox.setHgrow(child, Priority.ALWAYS);
        }
    }
}
