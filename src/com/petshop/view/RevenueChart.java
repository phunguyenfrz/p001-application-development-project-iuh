package com.petshop.view;

import com.petshop.util.Formats;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;

public final class RevenueChart extends VBox {
    private static final String[] DAY_LABELS = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};

    public RevenueChart(Map<LocalDate, BigDecimal> revenues, BigDecimal revenueToday) {
        super(12);
        getStyleClass().add("card-panel");
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(3);
        Label title = new Label("Doanh thu tuần này");
        title.getStyleClass().add("card-title");
        BigDecimal total = revenues.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        Label subtitle = new Label("Tổng: " + Formats.money(total));
        subtitle.getStyleClass().add("card-sub");
        titleBox.getChildren().addAll(title, subtitle);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label badge = new Label("● Tuần hiện tại");
        badge.getStyleClass().add("status-badge-paid");
        header.getChildren().addAll(titleBox, spacer, badge);

        LocalDate monday = revenues.keySet().stream().min(LocalDate::compareTo)
                .orElse(LocalDate.now().with(DayOfWeek.MONDAY));
        BigDecimal max = revenues.values().stream().max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        HBox chart = new HBox(24);
        chart.setAlignment(Pos.BOTTOM_CENTER);
        chart.setPadding(new Insets(24, 16, 8, 16));
        for (int index = 0; index < DAY_LABELS.length; index++) {
            LocalDate date = monday.plusDays(index);
            BigDecimal value = revenues.getOrDefault(date, BigDecimal.ZERO);
            boolean today = date.equals(LocalDate.now());
            double height = max.signum() == 0 ? 4
                    : Math.max(4, value.doubleValue() / max.doubleValue() * 150);
            VBox column = new VBox(8);
            column.setAlignment(Pos.BOTTOM_CENTER);
            if (today) {
                Label todayValue = new Label(Formats.money(revenueToday));
                todayValue.getStyleClass().add("revenue-today-value");
                column.getChildren().add(todayValue);
            }
            Rectangle bar = new Rectangle(36, height);
            bar.setArcWidth(8);
            bar.setArcHeight(8);
            bar.getStyleClass().add(today ? "revenue-bar-today" : "revenue-bar");
            Tooltip.install(bar, new Tooltip(DAY_LABELS[index] + " - " + Formats.money(value)));
            Label day = new Label(DAY_LABELS[index]);
            day.getStyleClass().add(today ? "revenue-day-today" : "revenue-day");
            column.getChildren().addAll(bar, day);
            chart.getChildren().add(column);
            HBox.setHgrow(column, Priority.ALWAYS);
        }
        getChildren().addAll(header, chart);
    }
}
