package com.petshop.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ReportView extends BorderPane {
    public ReportView(String reportName, Runnable showDashboard) {
        getStyleClass().add("dashboard");

        Button backButton = new Button("←  Tổng quan");
        backButton.getStyleClass().add("sidebar-btn");
        backButton.setOnAction(event -> showDashboard.run());

        HBox header = new HBox(backButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("topbar");
        setTop(header);

        Label title = new Label("Báo cáo " + reportName);
        title.getStyleClass().add("welcome-title");

        Label notice = new Label(
                "Dữ liệu báo cáo chưa được kết nối. Chức năng xuất sẽ khả dụng khi có dữ liệu.");
        notice.getStyleClass().add("welcome-sub");

        Button exportPdfButton = new Button("Xuất PDF");
        Button exportExcelButton = new Button("Xuất Excel");
        exportPdfButton.getStyleClass().add("sidebar-btn");
        exportExcelButton.getStyleClass().add("sidebar-btn");
        exportPdfButton.setDisable(true);
        exportExcelButton.setDisable(true);

        HBox exportButtons = new HBox(10, exportPdfButton, exportExcelButton);

        VBox content = new VBox(16, title, notice, exportButtons);
        content.setPadding(new Insets(28));
        content.getStyleClass().add("dashboard-content");
        setCenter(content);
    }
}
