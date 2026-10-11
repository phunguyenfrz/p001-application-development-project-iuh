package com.petshop.view;

import com.petshop.controller.ScreenNavigator;
import com.petshop.model.Screen;
import com.petshop.navigation.MenuGroup;
import com.petshop.navigation.MenuModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class SidebarView extends VBox {
    private static final class GroupRow {
        private final MenuGroup group;
        private final VBox submenu;
        private final Label title;
        private final Label arrow;
        private final Label icon;

        private GroupRow(MenuGroup group, VBox submenu, Label title, Label arrow, Label icon) {
            this.group = group;
            this.submenu = submenu;
            this.title = title;
            this.arrow = arrow;
            this.icon = icon;
        }
    }

    private final ScreenNavigator navigator;
    private final Map<Screen, java.util.List<Button>> buttons = new EnumMap<>(Screen.class);
    private final Map<Button, String> labels = new java.util.IdentityHashMap<>();
    private final java.util.List<GroupRow> groups = new java.util.ArrayList<>();
    private final Label brandTitle = new Label("Happy Tails");
    private final Label brandSubtitle = new Label("Cửa hàng thú cưng");
    private boolean collapsed;

    public SidebarView(List<MenuModel.Section> menu, ScreenNavigator navigator) {
        this.navigator = navigator;
        getStyleClass().add("sidebar");
        HBox brand = new HBox(10);
        brand.setAlignment(Pos.CENTER_LEFT);
        brand.getStyleClass().add("sidebar-brand-box");
        StackPane logo = new StackPane();
        Circle logoBackground = new Circle(17);
        logoBackground.getStyleClass().add("paw-logo-background");
        logo.getChildren().addAll(logoBackground, new PawIcon(34,
                PawIcon.ColorVariant.WHITE, PawIcon.Style.FILLED));
        brand.getChildren().addAll(logo,
                new VBox(1, brandTitle, brandSubtitle));
        brandTitle.getStyleClass().add("sidebar-title");
        brandSubtitle.getStyleClass().add("sidebar-subtitle");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button collapse = new Button("«");
        collapse.getStyleClass().add("btn-collapse");
        collapse.setOnAction(event -> setCollapsed(!collapsed));
        brand.getChildren().addAll(spacer, collapse);

        VBox menuList = new VBox(4);
        menuList.setPadding(new Insets(10, 12, 10, 12));
        for (MenuModel.Section section : menu) {
            if (section.group() == null) {
                section.entries().forEach(entry ->
                        menuList.getChildren().add(menuButton(entry.label(), entry.screen())));
            } else {
                menuList.getChildren().add(groupRow(section));
            }
        }
        ScrollPane scroll = new ScrollPane(menuList);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("sidebar-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        getChildren().addAll(brand, scroll);
    }

    public void observe(javafx.beans.property.ReadOnlyObjectProperty<Screen> currentScreen) {
        currentScreen.addListener((observable, oldScreen, newScreen) -> {
            if (newScreen != null) {
                updateSelection(newScreen);
            }
        });
    }

    private VBox groupRow(MenuModel.Section section) {
        VBox wrapper = new VBox(2);
        wrapper.setPadding(new Insets(2, 0, 2, 0));
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("sidebar-nav-item");
        Label icon = new Label(section.group().icon());
        icon.getStyleClass().add("sidebar-nav-bullet");
        Label title = new Label(section.label());
        title.getStyleClass().add("sidebar-nav-text");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label arrow = new Label("▸");
        arrow.getStyleClass().add("sidebar-nav-arrow");
        header.getChildren().addAll(icon, title, spacer, arrow);
        VBox submenu = new VBox(2);
        submenu.setPadding(new Insets(2, 0, 4, 10));
        submenu.setVisible(false);
        submenu.setManaged(false);
        for (MenuModel.Entry entry : section.entries()) {
            submenu.getChildren().add(submenuButton(entry));
        }
        GroupRow row = new GroupRow(section.group(), submenu, title, arrow, icon);
        groups.add(row);
        header.setOnMouseClicked(event -> toggle(row));
        wrapper.getChildren().addAll(header, submenu);
        return wrapper;
    }

    private Button menuButton(String label, Screen screen) {
        Button button = new Button(label);
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add("sidebar-btn");
        addButton(button, screen);
        return button;
    }

    private Button submenuButton(MenuModel.Entry entry) {
        Button button = new Button(entry.label());
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add("sidebar-sub-btn");
        addButton(button, entry.screen());
        return button;
    }

    private void addButton(Button button, Screen screen) {
        buttons.computeIfAbsent(screen, ignored -> new java.util.ArrayList<>()).add(button);
        labels.put(button, button.getText());
        button.setTooltip(new Tooltip(button.getText()));
        button.setOnAction(event -> navigator.navigate(screen));
    }

    private void toggle(GroupRow row) {
        boolean open = !row.submenu.isVisible();
        row.submenu.setVisible(open);
        row.submenu.setManaged(open);
        row.arrow.setText(open ? "▾" : "▸");
    }

    private void updateSelection(Screen selected) {
        buttons.forEach((screen, screenButtons) -> screenButtons.forEach(button -> {
            button.getStyleClass().remove("sidebar-btn-active");
            if (screen == selected) {
                button.getStyleClass().add("sidebar-btn-active");
            }
        }));
        for (GroupRow row : groups) {
            boolean contains = row.submenu.getChildren().stream()
                    .filter(Button.class::isInstance)
                    .map(Button.class::cast)
                    .anyMatch(button -> buttons.getOrDefault(selected, java.util.List.of())
                            .contains(button));
            if (contains && !row.submenu.isVisible()) {
                toggle(row);
            }
        }
    }

    private void setCollapsed(boolean value) {
        collapsed = value;
        double width = collapsed ? 72 : 240;
        setPrefWidth(width);
        setMinWidth(width);
        setMaxWidth(width);
        brandTitle.setVisible(!collapsed);
        brandTitle.setManaged(!collapsed);
        brandSubtitle.setVisible(!collapsed);
        brandSubtitle.setManaged(!collapsed);
        buttons.forEach((screen, screenButtons) -> screenButtons.forEach(button ->
                button.setText(collapsed ? shortIcon(labels.get(button))
                        : labels.get(button))));
        for (GroupRow row : groups) {
            row.title.setVisible(!collapsed);
            row.title.setManaged(!collapsed);
            row.arrow.setVisible(!collapsed);
            row.arrow.setManaged(!collapsed);
            row.icon.setText(collapsed ? row.group.icon() : "○");
        }
    }

    private String shortIcon(String label) {
        return label == null || label.isBlank() ? "•" : label.substring(0, 1);
    }
}
