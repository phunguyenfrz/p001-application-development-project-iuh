package com.petshop.view;

import javafx.scene.Node;

public interface AppView {
    Node getNode();

    String getTitle();

    default void onShow() {
    }

    default void onHide() {
    }
}
