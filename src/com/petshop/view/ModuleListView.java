package com.petshop.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Màn hình danh sách chuẩn cho một đối tượng nghiệp vụ (Module List View).
 * Cung cấp Header, Thanh công cụ (Thêm/Sửa/Xóa/Tìm kiếm/Làm mới) và Bảng dữ liệu.
 */
public class ModuleListView extends VBox {

    public ModuleListView(String objectTitle, String actionName, String shortcut) {
        setSpacing(16);
        setPadding(new Insets(24, 28, 28, 28));
        setStyle("-fx-background-color: #f3f6f3;");

        // 1. Header
        VBox headerBox = new VBox(4);
        Label lblTitle = new Label(objectTitle + " › " + actionName);
        lblTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");

        Label lblSub = new Label("Phân hệ quản lý " + objectTitle + " (Sơ đồ hướng đối tượng chuẩn).");
        lblSub.setStyle("-fx-font-size: 13px; -fx-text-fill: #6e7c70;");
        headerBox.getChildren().addAll(lblTitle, lblSub);

        // 2. Toolbar & Table Container
        VBox card = new VBox(14);
        card.setPadding(new Insets(16));
        card.getStyleClass().add("chart-panel");
        VBox.setVgrow(card, Priority.ALWAYS);

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Tìm kiếm trong danh sách " + objectTitle + "...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(280);

        HBox searchBox = new HBox(6, searchField);
        searchBox.getStyleClass().add("search-box");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnAdd = new Button("➕ Thêm mới");
        btnAdd.getStyleClass().add("btn-action-primary");
        btnAdd.setOnAction(e -> showDetailDialog("Thêm mới " + objectTitle));

        Button btnEdit = new Button("✏ Sửa");
        btnEdit.getStyleClass().add("btn-action-secondary");
        btnEdit.setOnAction(e -> showDetailDialog("Cập nhật " + objectTitle));

        Button btnDelete = new Button("🗑 Xóa");
        btnDelete.getStyleClass().add("btn-action-secondary");
        btnDelete.setOnAction(e -> showDetailDialog("Xóa " + objectTitle));

        Button btnRefresh = new Button("🔄 Làm mới (F5)");
        btnRefresh.getStyleClass().add("btn-action-secondary");

        toolbar.getChildren().addAll(searchBox, spacer, btnAdd, btnEdit, btnDelete, btnRefresh);

        // Table Placeholder
        TableView<String> table = new TableView<>();
        table.setPlaceholder(new Label("Chức năng [" + actionName + "] đang được kết nối với cơ sở dữ liệu. (TODO)"));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<String, String> c1 = new TableColumn<>("Mã định danh");
        c1.setPrefWidth(140);
        TableColumn<String, String> c2 = new TableColumn<>("Thông tin chính");
        c2.setPrefWidth(300);
        TableColumn<String, String> c3 = new TableColumn<>("Ngày cập nhật");
        c3.setPrefWidth(160);
        TableColumn<String, String> c4 = new TableColumn<>("Trạng thái");
        c4.setPrefWidth(140);

        table.getColumns().addAll(c1, c2, c3, c4);

        card.getChildren().addAll(toolbar, table);
        getChildren().addAll(headerBox, card);
    }

    private void showDetailDialog(String title) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText("Hộp thoại [" + title + "] đang được xây dựng theo chuẩn Dialog n-Tier.");
        alert.showAndWait();
    }
}

