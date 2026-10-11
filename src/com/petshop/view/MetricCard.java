package com.petshop.view;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public final class MetricCard extends VBox {
    public MetricCard(String title, String value, boolean featured) {
        super(8);
        getStyleClass().add(featured ? "stat-card-featured" : "stat-card");
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add(featured ? "stat-title-white" : "stat-title");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add(featured ? "stat-value-white" : "stat-value");
        getChildren().addAll(titleLabel, valueLabel);
    }
}
