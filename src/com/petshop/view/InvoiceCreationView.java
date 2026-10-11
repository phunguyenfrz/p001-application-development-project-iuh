package com.petshop.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class InvoiceCreationView extends VBox implements AppView {
    private final String title;

    public InvoiceCreationView(String appointmentId) {
        super(16);
        title = appointmentId == null ? "Lập hóa đơn" : "Lập hóa đơn | " + appointmentId;
        setPadding(new Insets(4, 0, 0, 0));

        Label description = new Label(appointmentId == null
                ? "Quét mã vạch sản phẩm để thêm vào hóa đơn."
                : "Lịch hẹn: " + appointmentId + " — quét mã vạch sản phẩm để thêm vào hóa đơn.");
        description.getStyleClass().add("welcome-sub");

        TextField barcodeField = new TextField();
        barcodeField.setPromptText("Quét hoặc nhập mã vạch sản phẩm...");
        barcodeField.getStyleClass().add("search-field");
        barcodeField.setPrefWidth(420);

        HBox barcodeBox = new HBox(barcodeField);
        barcodeBox.setAlignment(Pos.CENTER_LEFT);
        barcodeBox.getStyleClass().add("search-box");

        VBox panel = new VBox(16, description, barcodeBox);
        panel.setPadding(new Insets(20));
        panel.getStyleClass().add("card-panel");
        getChildren().add(panel);

        Platform.runLater(barcodeField::requestFocus);
    }

    @Override
    public javafx.scene.Node getNode() {
        return this;
    }

    @Override
    public String getTitle() {
        return title;
    }
}
