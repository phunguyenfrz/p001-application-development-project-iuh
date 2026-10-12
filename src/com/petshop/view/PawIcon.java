package com.petshop.view;

import javafx.scene.Group;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;

public final class PawIcon extends Group {
    public enum ColorVariant {
        BRAND("paw-brand"),
        WHITE("paw-white");

        private final String styleClass;

        ColorVariant(String styleClass) {
            this.styleClass = styleClass;
        }
    }

    public enum Style {
        OUTLINE("paw-outline"),
        FILLED("paw-filled");

        private final String styleClass;

        Style(String styleClass) {
            this.styleClass = styleClass;
        }
    }

    public PawIcon(double size, ColorVariant color, Style style) {
        getStyleClass().addAll("paw-icon", color.styleClass);
        double scale = size / 34.0;
        setScaleX(scale);
        setScaleY(scale);
        Ellipse pad = new Ellipse(0, 5, 12, 9);
        Circle toe1 = new Circle(-11, -8, 3.8);
        Circle toe2 = new Circle(-4, -13, 4.2);
        Circle toe3 = new Circle(4, -13, 4.2);
        Circle toe4 = new Circle(11, -8, 3.8);
        for (var shape : java.util.List.of(pad, toe1, toe2, toe3, toe4)) {
            shape.getStyleClass().addAll("paw-shape", style.styleClass);
        }
        getChildren().addAll(pad, toe1, toe2, toe3, toe4);
    }
}
