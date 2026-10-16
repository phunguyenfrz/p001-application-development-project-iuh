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
        VBox pnlHeaderBox = new VBox(4);
        Label lblTitle = new Label(objectTitle + " › " + actionName);
        lblTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");

        Label lblSubtitle = new Label("Phân hệ quản lý " + objectTitle + " (Sơ đồ hướng đối tượng chuẩn).");
        lblSubtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #6e7c70;");
        pnlHeaderBox.getChildren().addAll(lblTitle, lblSubtitle);

        // 2. Toolbar & Table Container
        VBox pnlCard = new VBox(14);
        pnlCard.setPadding(new Insets(16));
        pnlCard.getStyleClass().add("chart-panel");
        VBox.setVgrow(pnlCard, Priority.ALWAYS);

        HBox pnlToolbar = new HBox(10);
        pnlToolbar.setAlignment(Pos.CENTER_LEFT);

        TextField txtSearch = new TextField();
        txtSearch.setPromptText("Tìm kiếm trong danh sách " + objectTitle + "...");
        txtSearch.getStyleClass().add("search-field");
        txtSearch.setPrefWidth(280);

        HBox pnlSearchBox = new HBox(6, txtSearch);
        pnlSearchBox.getStyleClass().add("search-box");

        Region pnlSpacer = new Region();
        HBox.setHgrow(pnlSpacer, Priority.ALWAYS);

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

        pnlToolbar.getChildren().addAll(pnlSearchBox, pnlSpacer, btnAdd, btnEdit, btnDelete, btnRefresh);

        // Table Placeholder
        TableView<String> tblRecords = new TableView<>();
        Label lblTablePlaceholder = new Label("Chức năng [" + actionName + "] đang được kết nối với cơ sở dữ liệu. (TODO)");
        tblRecords.setPlaceholder(lblTablePlaceholder);
        VBox.setVgrow(tblRecords, Priority.ALWAYS);

        TableColumn<String, String> colIdentifier = new TableColumn<>("Mã định danh");
        colIdentifier.setPrefWidth(140);
        TableColumn<String, String> colMainInfo = new TableColumn<>("Thông tin chính");
        colMainInfo.setPrefWidth(300);
        TableColumn<String, String> colUpdatedAt = new TableColumn<>("Ngày cập nhật");
        colUpdatedAt.setPrefWidth(160);
        TableColumn<String, String> colStatus = new TableColumn<>("Trạng thái");
        colStatus.setPrefWidth(140);

        tblRecords.getColumns().addAll(colIdentifier, colMainInfo, colUpdatedAt, colStatus);

        pnlCard.getChildren().addAll(pnlToolbar, tblRecords);
        getChildren().addAll(pnlHeaderBox, pnlCard);
    }

    private void showDetailDialog(String title) {
        Alert dlgInformation = new Alert(Alert.AlertType.INFORMATION);
        dlgInformation.setTitle(title);
        dlgInformation.setHeaderText(null);
        dlgInformation.setContentText("Hộp thoại [" + title + "] đang được xây dựng theo chuẩn Dialog n-Tier.");
        dlgInformation.showAndWait();
    }
}
