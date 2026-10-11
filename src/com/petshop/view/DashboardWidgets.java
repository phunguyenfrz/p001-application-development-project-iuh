package com.petshop.view;

import com.petshop.model.User;
import com.petshop.util.Formats;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DashboardWidgets {
    private static final Logger LOGGER = Logger.getLogger(DashboardWidgets.class.getName());

    private DashboardWidgets() {
    }

    public static Node welcomeHeader(User user) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        VBox welcome = new VBox(4);
        Label title = new Label("Chào mừng trở lại, " + user.getFullName());
        title.getStyleClass().add("welcome-title");
        Label subtitle = new Label("Mọi điều tốt đẹp bắt đầu từ một chiếc đuôi hạnh phúc.");
        subtitle.getStyleClass().add("welcome-sub");
        welcome.getChildren().addAll(title, subtitle);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label date = new Label(Formats.date(LocalDate.now()) + "  📅");
        date.getStyleClass().add("date-badge");
        header.getChildren().addAll(welcome, spacer, date);
        return header;
    }

    public static String money(BigDecimal value) {
        return Formats.money(value);
    }

    public static String time(LocalTime value) {
        return Formats.time(value);
    }

    public static void grow(HBox row) {
        for (Node child : row.getChildren()) {
            HBox.setHgrow(child, Priority.ALWAYS);
        }
    }

    public static void showLoadError(VBox parent, IllegalStateException exception) {
        LOGGER.log(Level.SEVERE, "Không thể tải dữ liệu tổng quan.", exception);
        Label error = new Label("Hiện chưa thể tải dữ liệu tổng quan. Vui lòng thử lại sau.");
        error.setWrapText(true);
        error.getStyleClass().add("welcome-sub");
        VBox panel = new VBox(error);
        panel.getStyleClass().add("card-panel");
        parent.getChildren().add(panel);
    }
}
