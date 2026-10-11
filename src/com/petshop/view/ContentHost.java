package com.petshop.view;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import com.petshop.model.Screen;
import com.petshop.navigation.ScreenRegistry;
import javafx.beans.property.ReadOnlyObjectProperty;

public final class ContentHost extends VBox implements ContentSink {
    private final ScrollPane scrollPane = new ScrollPane();
    private final VBox content = new VBox(18);
    private final Label breadcrumb = new Label();

    public ContentHost() {
        getStyleClass().add("content-host");
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("content-scroll");
        breadcrumb.getStyleClass().add("card-title");
        content.getStyleClass().add("dashboard-content");
        content.getChildren().add(breadcrumb);
        scrollPane.setContent(content);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        getChildren().add(scrollPane);
    }

    @Override
    public void setContent(Node node, String title) {
        breadcrumb.setText(title);
        if (content.getChildren().size() > 1) {
            content.getChildren().remove(1, content.getChildren().size());
        }
        content.getChildren().add(node);
    }

    public void observe(
            ReadOnlyObjectProperty<Screen> currentScreen, ScreenRegistry registry) {
        currentScreen.addListener((observable, oldScreen, newScreen) -> {
            if (newScreen != null) {
                breadcrumb.setText(registry.definition(newScreen).label());
            }
        });
    }
}
