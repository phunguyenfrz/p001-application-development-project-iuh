package com.petshop.view;

import com.petshop.controller.ScreenNavigator;
import com.petshop.model.Screen;
import com.petshop.model.User;
import com.petshop.util.Formats;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;

public final class DashboardWidgets {
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

    public static Node invoiceButton(ScreenNavigator navigator) {
        javafx.scene.control.Button button =
                new javafx.scene.control.Button("+ Tạo hóa đơn mới");
        button.getStyleClass().add("btn-create-invoice");
        button.setOnAction(event ->
                navigator.navigate(Screen.INVOICE_CREATE, "Lập hóa đơn mới"));
        return button;
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
        Label error = new Label("Không thể tải dữ liệu từ QLCH_HappyTails: "
                + exception.getMessage());
        error.setWrapText(true);
        error.getStyleClass().add("welcome-sub");
        VBox panel = new VBox(error);
        panel.getStyleClass().add("card-panel");
        parent.getChildren().add(panel);
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi tải bảng điều khiển");
            alert.setHeaderText("Không thể kết nối hoặc truy vấn dữ liệu.");
            alert.setContentText(exception.getMessage());
            alert.show();
        });
    }
}
