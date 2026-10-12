package com.petshop.view;

import com.petshop.model.Screen;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class PlaceholderView extends VBox implements AppView, SearchResultAware {
    private final String title;
    private final Label selectedRecord = new Label();

    public PlaceholderView(Screen screen, String title) {
        super(12);
        this.title = title;
        setPadding(new Insets(4, 0, 0, 0));

        Label heading = new Label(title);
        heading.getStyleClass().add("welcome-title");
        Label description = new Label(
                "Use case của phân hệ này chưa được triển khai. Các chức năng dự kiến:");
        description.getStyleClass().add("welcome-sub");
        Label features = new Label("• Tra cứu và lọc dữ liệu\n"
                + "• Xem chi tiết và cập nhật thông tin\n"
                + "• Thực hiện các thao tác nghiệp vụ của phân hệ");
        features.setWrapText(true);
        features.getStyleClass().add("welcome-sub");
        selectedRecord.getStyleClass().add("welcome-sub");
        selectedRecord.setVisible(false);
        selectedRecord.setManaged(false);
        VBox panel = new VBox(12, heading, description, features, selectedRecord);
        panel.getStyleClass().add("card-panel");
        getChildren().add(panel);
    }

    @Override
    public javafx.scene.Node getNode() {
        return this;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void showSearchResult(String id) {
        selectedRecord.setText("Đã chọn bản ghi: " + id);
        selectedRecord.setVisible(true);
        selectedRecord.setManaged(true);
    }
}
