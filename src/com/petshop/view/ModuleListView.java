package com.petshop.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Màn hình danh sách chuẩn cho một đối tượng nghiệp vụ (ModuleListView).
 * Bố cục chuẩn ứng dụng Desktop: Tiêu đề, Thanh công cụ (Thêm, Sửa, Xóa, Làm mới) và Bảng dữ liệu.
 */
public class ModuleListView extends VBox {

    public ModuleListView(String objectTitle, String actionName, String shortcut) {
        setSpacing(14);
        setPadding(new Insets(20, 24, 20, 24));
        setStyle("-fx-background-color: #f4f6f8;");

        // 1. Tiêu đề màn hình
        VBox pnlHeaderBox = new VBox(4);
        Label lblTitle = new Label(objectTitle.toUpperCase() + " - " + actionName.toUpperCase());
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label lblSubtitle = new Label("Phân hệ quản lý " + objectTitle + " (Hệ thống Happy Tails).");
        lblSubtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        pnlHeaderBox.getChildren().addAll(lblTitle, lblSubtitle);

        // 2. Khung chức năng & Bảng dữ liệu
        VBox pnlCard = new VBox(12);
        pnlCard.setPadding(new Insets(14));
        pnlCard.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-radius: 4px; -fx-background-radius: 4px;");
        VBox.setVgrow(pnlCard, Priority.ALWAYS);

        HBox pnlToolbar = new HBox(10);
        pnlToolbar.setAlignment(Pos.CENTER_LEFT);

        TextField txtSearch = new TextField();
        txtSearch.setPromptText("Tìm kiếm trong danh sách " + objectTitle + "...");
        txtSearch.setPrefWidth(260);

        Region pnlSpacer = new Region();
        HBox.setHgrow(pnlSpacer, Priority.ALWAYS);

        Button btnAdd = new Button("Thêm mới");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> showDetailDialog("Thêm mới " + objectTitle));

        Button btnEdit = new Button("Cập nhật");
        btnEdit.getStyleClass().add("btn-secondary");
        btnEdit.setOnAction(e -> showDetailDialog("Cập nhật " + objectTitle));

        Button btnDelete = new Button("Xóa");
        btnDelete.getStyleClass().add("btn-danger");
        btnDelete.setOnAction(e -> showDetailDialog("Xóa " + objectTitle));

        Button btnRefresh = new Button("Làm mới");
        btnRefresh.getStyleClass().add("btn-secondary");

        pnlToolbar.getChildren().addAll(txtSearch, pnlSpacer, btnAdd, btnEdit, btnDelete, btnRefresh);

        // Bảng dữ liệu (TableView)
        TableView<String> tblRecords = new TableView<>();
        Label lblTablePlaceholder = new Label("Chức năng [" + actionName + "] đang được kết nối với cơ sở dữ liệu.");
        lblTablePlaceholder.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
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

        tblRecords.getColumns().addAll(java.util.List.of(colIdentifier, colMainInfo, colUpdatedAt, colStatus));

        pnlCard.getChildren().addAll(pnlToolbar, tblRecords);
        getChildren().addAll(pnlHeaderBox, pnlCard);
    }

    private void showDetailDialog(String title) {
        Alert dlgInformation = new Alert(Alert.AlertType.INFORMATION);
        dlgInformation.setTitle(title);
        dlgInformation.setHeaderText(null);
        dlgInformation.setContentText("Hộp thoại [" + title + "] đang được xây dựng theo chuẩn Kiến trúc đa tầng.");
        dlgInformation.showAndWait();
    }
}
