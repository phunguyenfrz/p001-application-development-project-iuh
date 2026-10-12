package com.petshop.view;

import com.petshop.main.ApplicationControllers;
import com.petshop.controller.NavigationController;
import com.petshop.model.Screen;
import com.petshop.model.User;
import com.petshop.navigation.MenuModel;
import com.petshop.navigation.ScreenRegistry;
import com.petshop.controller.ScreenNavigator;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public final class MainShell extends BorderPane {
    private final TopbarView topbar;

    public MainShell(
            User user, ApplicationControllers controllers,
            NavigationController navigator, ScreenRegistry registry,
            ContentHost contentHost, Runnable logoutAction) {
        getStyleClass().add("dashboard");
        SidebarView sidebar = new SidebarView(
                new MenuModel(registry).forRole(user.getRole()), navigator);
        sidebar.observe(navigator.currentScreenProperty());
        topbar = new TopbarView(user, controllers, navigator, logoutAction);
        VBox center = new VBox(topbar, contentHost);
        setLeft(sidebar);
        setCenter(center);
        addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.K) {
                topbar.focusSearch();
                event.consume();
            }
        });
    }
}
