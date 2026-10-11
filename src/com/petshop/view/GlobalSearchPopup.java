package com.petshop.view;

import com.petshop.controller.GlobalSearchController;
import com.petshop.controller.ScreenNavigator;
import com.petshop.model.Role;
import com.petshop.model.SearchResult;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.geometry.Side;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public final class GlobalSearchPopup {
    private final TextField searchField = new TextField();
    private final ContextMenu popup = new ContextMenu();
    private final PauseTransition debounce = new PauseTransition(Duration.millis(300));
    private final AtomicLong generation = new AtomicLong();
    private final GlobalSearchController controller;
    private final Role role;
    private final ScreenNavigator navigator;

    public GlobalSearchPopup(
            GlobalSearchController controller, Role role, ScreenNavigator navigator) {
        this.controller = controller;
        this.role = role;
        this.navigator = navigator;
        searchField.setPromptText("Tìm kiếm nhanh...");
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((observable, oldValue, query) -> {
            generation.incrementAndGet();
            debounce.stop();
            popup.hide();
            if (query != null && !query.isBlank()) {
                debounce.playFromStart();
            }
        });
        debounce.setOnFinished(event -> search(searchField.getText()));
    }

    public TextField field() {
        return searchField;
    }

    public void focus() {
        searchField.requestFocus();
        searchField.selectAll();
    }

    private void search(String query) {
        String term = query == null ? "" : query.trim();
        if (term.isEmpty()) {
            return;
        }
        long request = generation.get();
        Task<List<SearchResult>> task = new Task<>() {
            @Override
            protected List<SearchResult> call() {
                return controller.search(term, role);
            }
        };
        task.setOnSucceeded(event -> {
            if (request == generation.get()
                    && term.equals(searchField.getText().trim())) {
                showResults(task.getValue());
            }
        });
        task.setOnFailed(event -> {
            if (request == generation.get()) {
                java.util.logging.Logger.getLogger(GlobalSearchPopup.class.getName())
                        .log(java.util.logging.Level.SEVERE, "Global search failed", task.getException());
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Lỗi tìm kiếm");
                alert.setHeaderText("Không thể tìm kiếm dữ liệu.");
                alert.setContentText(task.getException().getMessage());
                alert.show();
            }
        });
        Thread worker = new Thread(task, "global-search");
        worker.setDaemon(true);
        worker.start();
    }

    private void showResults(List<SearchResult> results) {
        popup.getItems().clear();
        if (results.isEmpty()) {
            MenuItem empty = new MenuItem("Không tìm thấy dữ liệu");
            empty.setDisable(true);
            popup.getItems().add(empty);
        } else {
            Map<String, List<SearchResult>> groups = new LinkedHashMap<>();
            for (SearchResult result : results) {
                groups.computeIfAbsent(result.groupName(), ignored -> new ArrayList<>()).add(result);
            }
            boolean first = true;
            for (Map.Entry<String, List<SearchResult>> group : groups.entrySet()) {
                if (!first) {
                    popup.getItems().add(new SeparatorMenuItem());
                }
                first = false;
                MenuItem heading = new MenuItem(group.getKey());
                heading.setDisable(true);
                popup.getItems().add(heading);
                for (SearchResult result : group.getValue()) {
                    MenuItem item = new MenuItem(result.id() + " · " + result.name()
                            + " — " + result.detail());
                    item.setOnAction(event -> {
                        popup.hide();
                        navigator.navigate(result);
                    });
                    popup.getItems().add(item);
                }
            }
        }
        if (searchField.isFocused() && !popup.isShowing()) {
            popup.show(searchField, Side.BOTTOM, 0, 0);
        }
    }
}
