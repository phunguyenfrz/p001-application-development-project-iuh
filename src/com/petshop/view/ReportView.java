package com.petshop.view;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ReportView extends VBox implements AppView {
    private final String title;

    public ReportView(String reportName) {
        super(16);
        title = reportName;
        Label notice = new Label(
                "Dữ liệu báo cáo chưa được kết nối. Chức năng xuất sẽ khả dụng khi có dữ liệu.");
        notice.getStyleClass().add("welcome-sub");
        VBox panel = new VBox(12, notice);
        panel.getStyleClass().add("card-panel");
        getChildren().add(panel);
    }

    @Override
    public javafx.scene.Node getNode() {
        return this;
    }

    @Override
    public String getTitle() {
        return title;
    }
}
