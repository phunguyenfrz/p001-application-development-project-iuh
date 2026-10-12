package com.petshop.view;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

public final class SearchResultPanel<T> extends VBox {
    private final Label title = new Label();
    private final Label resultCount = new Label();
    private final TableView<T> table = new TableView<>();
    private final Button editButton = new Button("Sửa");
    private final Button deactivateButton = new Button("Ngừng");
    private final Consumer<T> editAction;
    private final Consumer<T> deactivateAction;

    public SearchResultPanel(
            List<TableColumn<T, ?>> sourceColumns,
            boolean allowEdit,
            boolean allowDeactivate,
            Runnable clearAction,
            Consumer<T> selectionAction,
            Consumer<T> editAction,
            Consumer<T> deactivateAction,
            Consumer<T> readOnlyAction) {
        this(sourceColumns, allowEdit, allowDeactivate, !allowEdit, clearAction,
                selectionAction, editAction, deactivateAction, readOnlyAction);
    }

    public SearchResultPanel(
            List<TableColumn<T, ?>> sourceColumns,
            boolean allowEdit,
            boolean allowDeactivate,
            boolean openDetailsOnDoubleClick,
            Runnable clearAction,
            Consumer<T> selectionAction,
            Consumer<T> editAction,
            Consumer<T> deactivateAction,
            Consumer<T> readOnlyAction) {
        super(8);
        this.editAction = editAction;
        this.deactivateAction = deactivateAction;
        getStyleClass().add("card-panel");

        title.getStyleClass().add("card-title");
        resultCount.getStyleClass().add("card-sub");
        Button clear = new Button("Xóa kết quả");
        clear.getStyleClass().add("sidebar-btn");
        clear.setOnAction(event -> clearAction.run());
        editButton.getStyleClass().add("sidebar-btn");
        editButton.setVisible(allowEdit);
        editButton.setManaged(allowEdit);
        editButton.setOnAction(event -> withSelection(this.editAction));
        deactivateButton.getStyleClass().add("sidebar-btn");
        deactivateButton.setVisible(allowDeactivate);
        deactivateButton.setManaged(allowDeactivate);
        deactivateButton.setOnAction(event -> withSelection(this.deactivateAction));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(8, title, resultCount, spacer, editButton,
                deactivateButton, clear);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Không tìm thấy dữ liệu"));
        table.setPrefHeight(180);
        table.setMaxHeight(320);
        for (TableColumn<T, ?> sourceColumn : sourceColumns) {
            TableColumn<T, Object> column = new TableColumn<>(sourceColumn.getText());
            column.setCellValueFactory(cell -> {
                javafx.beans.value.ObservableValue<?> value =
                        sourceColumn.getCellObservableValue(cell.getValue());
                return new ReadOnlyObjectWrapper<>(value == null ? null : value.getValue());
            });
            column.setSortable(sourceColumn.isSortable());
            column.setPrefWidth(sourceColumn.getPrefWidth());
            table.getColumns().add(column);
        }
        table.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, selected) -> {
                    boolean hasSelection = selected != null;
                    editButton.setDisable(!hasSelection);
                    deactivateButton.setDisable(!hasSelection);
                    if (selected != null) {
                        selectionAction.accept(selected);
                    }
                });
        table.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && openDetailsOnDoubleClick) {
                T selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    readOnlyAction.accept(selected);
                }
            }
        });
        editButton.setDisable(true);
        deactivateButton.setDisable(true);
        getChildren().addAll(header, table);
        setVisible(false);
        setManaged(false);
    }

    public void showResults(String keyword, List<T> results) {
        title.setText("Kết quả tìm kiếm cho \"" + keyword + "\"");
        resultCount.setText("Tìm thấy " + results.size() + " kết quả");
        table.getItems().setAll(FXCollections.observableArrayList(results));
        table.getSelectionModel().clearSelection();
        setVisible(true);
        setManaged(true);
    }

    public void hideResults() {
        table.getItems().clear();
        table.getSelectionModel().clearSelection();
        setVisible(false);
        setManaged(false);
    }

    private void withSelection(Consumer<T> action) {
        T selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            action.accept(selected);
        }
    }
}
