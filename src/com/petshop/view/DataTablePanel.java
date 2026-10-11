package com.petshop.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public final class DataTablePanel extends VBox {
    private final HBox header = new HBox();
    private final TableView<ObservableList<String>> table = new TableView<>();

    public DataTablePanel(String title, List<String> columnNames, List<List<String>> rows) {
        super(12);
        getStyleClass().add("card-panel");
        Label heading = new Label(title);
        heading.getStyleClass().add("card-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(heading, spacer);
        configureTable(columnNames, rows);
        getChildren().addAll(header, table);
    }

    public void addHeaderContent(Node node) {
        header.getChildren().add(node);
    }

    public TableView<ObservableList<String>> table() {
        return table;
    }

    private void configureTable(List<String> names, List<List<String>> rows) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(Math.min(320, Math.max(125, 38 + rows.size() * 34)));
        for (int index = 0; index < names.size(); index++) {
            int columnIndex = index;
            TableColumn<ObservableList<String>, String> column =
                    new TableColumn<>(names.get(index));
            column.setCellValueFactory(cell -> new SimpleStringProperty(
                    cell.getValue().get(columnIndex)));
            table.getColumns().add(column);
        }
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        rows.forEach(row -> data.add(FXCollections.observableArrayList(row)));
        table.setItems(data);
        table.setPlaceholder(new Label("Không có dữ liệu."));
    }
}
