package com.petshop.view.promotion;

import com.petshop.controller.PromotionController;
import com.petshop.model.Promotion;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Window;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Hộp thoại Chọn chương trình khuyến mãi đang hiệu lực (PromotionPickerDialog).
 * Bàn giao phụ thuộc từ Trần Lê Tuấn Anh phục vụ cho màn hình Lập hóa đơn (INV_CREATE).
 *
 * Tiêu chuẩn Definition of Done:
 * - Đặt tên điều khiển theo tiền tố: txt, tbl, lbl, btn.
 * - Các nút hiển thị tên đầy đủ (setMinWidth(Region.USE_PREF_SIZE)).
 * - Chỉ cho phép chọn các chương trình khuyến mãi đang hoạt động và trong thời hạn áp dụng.
 */
public class PromotionPickerDialog extends Dialog<Promotion> {

    private final PromotionController promotionController = new PromotionController();
    private final ObservableList<Promotion> masterPromotionList = FXCollections.observableArrayList();
    private final FilteredList<Promotion> filteredPromotionList = new FilteredList<>(masterPromotionList, p -> true);
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Điều khiển tuân thủ tiền tố
    private TextField txtSearch;
    private TableView<Promotion> tblPromotions;
    private Label lblStatusCount;
    private Button btnSelect;
    private Button btnNoPromotion;
    private Button btnCancel;

    public PromotionPickerDialog(Window owner) {
        setTitle("Chọn Khuyến Mãi Áp Dụng - Happy Tails");
        setHeaderText(null);
        initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            initOwner(owner);
        }

        DialogPane pnlDialogPane = getDialogPane();
        pnlDialogPane.getStylesheets().add(
                getClass().getResource("/com/petshop/view/styles.css").toExternalForm()
        );
        pnlDialogPane.setPrefSize(720, 480);
        pnlDialogPane.setStyle("-fx-background-color: #f3f6f3;");

        pnlDialogPane.setContent(buildContent());
        configureButtons(pnlDialogPane);
        loadActivePromotions();

        Platform.runLater(() -> {
            if (txtSearch != null) {
                txtSearch.requestFocus();
            }
        });
    }

    public static Optional<Promotion> showAndPick(Window owner) {
        PromotionPickerDialog dlg = new PromotionPickerDialog(owner);
        return dlg.showAndWait();
    }

    private VBox buildContent() {
        VBox pnlContainer = new VBox(12);
        pnlContainer.setPadding(new Insets(16, 20, 10, 20));

        // Header
        VBox pnlHeader = new VBox(4);
        Label lblTitle = new Label("Danh sách chương trình khuyến mãi đang hiệu lực");
        lblTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");
        Label lblSubtitle = new Label("Chọn chương trình ưu đãi để áp dụng chiết khấu giảm giá vào hóa đơn thanh toán.");
        lblSubtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #6e7c70;");
        pnlHeader.getChildren().addAll(lblTitle, lblSubtitle);

        // Search Bar
        HBox pnlSearch = new HBox(10);
        pnlSearch.setAlignment(Pos.CENTER_LEFT);
        pnlSearch.setPadding(new Insets(10, 14, 10, 14));
        pnlSearch.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 6px; -fx-background-radius: 6px;");

        Label lblSearchIcon = new Label("🔍");
        lblSearchIcon.setStyle("-fx-font-size: 13px; -fx-text-fill: #829485;");

        txtSearch = new TextField();
        txtSearch.setPromptText("Tìm kiếm theo mã khuyến mãi hoặc tên chương trình ưu đãi...");
        txtSearch.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-font-size: 13px;");
        HBox.setHgrow(txtSearch, Priority.ALWAYS);

        PauseTransition debounce = new PauseTransition(Duration.millis(150));
        txtSearch.textProperty().addListener((obs, o, n) -> {
            debounce.setOnFinished(e -> applyFilter());
            debounce.playFromStart();
        });

        pnlSearch.getChildren().addAll(lblSearchIcon, txtSearch);

        // Table
        tblPromotions = new TableView<>();
        tblPromotions.setItems(filteredPromotionList);
        tblPromotions.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblPromotions.setPlaceholder(new Label("Không có chương trình khuyến mãi nào đang áp dụng"));
        VBox.setVgrow(tblPromotions, Priority.ALWAYS);

        TableColumn<Promotion, String> colId = new TableColumn<>("Mã KM");
        colId.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getMaKhuyenMai()));
        colId.setPrefWidth(125);
        colId.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-weight: bold;");

        TableColumn<Promotion, String> colName = new TableColumn<>("Tên chương trình ưu đãi");
        colName.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTenChuongTrinh()));
        colName.setPrefWidth(260);
        colName.setStyle("-fx-font-weight: 600;");

        TableColumn<Promotion, String> colDiscount = new TableColumn<>("Giảm giá");
        colDiscount.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.format("%.0f %%", c.getValue().getPhanTramGiam())));
        colDiscount.setPrefWidth(100);
        colDiscount.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: #dc2626;");

        TableColumn<Promotion, String> colStart = new TableColumn<>("Bắt đầu");
        colStart.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getNgayBatDau() != null ? c.getValue().getNgayBatDau().format(dateFormatter) : "-"
        ));
        colStart.setPrefWidth(105);
        colStart.setStyle("-fx-alignment: CENTER;");

        TableColumn<Promotion, String> colEnd = new TableColumn<>("Hạn áp dụng");
        colEnd.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getNgayKetThuc() != null ? c.getValue().getNgayKetThuc().format(dateFormatter) : "Vô thời hạn"
        ));
        colEnd.setPrefWidth(110);
        colEnd.setStyle("-fx-alignment: CENTER;");

        tblPromotions.getColumns().addAll(List.of(colId, colName, colDiscount, colStart, colEnd));

        tblPromotions.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                Promotion selected = tblPromotions.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    setResult(selected);
                    close();
                }
            }
        });

        // Status bar
        lblStatusCount = new Label("Đang tải danh sách khuyến mãi...");
        lblStatusCount.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #6e7c70;");

        pnlContainer.getChildren().addAll(pnlHeader, pnlSearch, tblPromotions, lblStatusCount);
        return pnlContainer;
    }

    private void configureButtons(DialogPane pnlDialogPane) {
        ButtonType btnTypeSelect = new ButtonType("Chọn khuyến mãi", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnTypeNoPromo = new ButtonType("Không áp dụng", ButtonBar.ButtonData.OTHER);
        ButtonType btnTypeCancel = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);

        pnlDialogPane.getButtonTypes().addAll(btnTypeSelect, btnTypeNoPromo, btnTypeCancel);

        btnSelect = (Button) pnlDialogPane.lookupButton(btnTypeSelect);
        btnNoPromotion = (Button) pnlDialogPane.lookupButton(btnTypeNoPromo);
        btnCancel = (Button) pnlDialogPane.lookupButton(btnTypeCancel);

        btnSelect.setMinWidth(Region.USE_PREF_SIZE);
        btnNoPromotion.setMinWidth(Region.USE_PREF_SIZE);
        btnCancel.setMinWidth(Region.USE_PREF_SIZE);

        btnSelect.getStyleClass().addAll("customer-add");
        btnNoPromotion.getStyleClass().addAll("customer-secondary");
        btnCancel.getStyleClass().addAll("customer-secondary");

        btnSelect.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            Promotion selected = tblPromotions.getSelectionModel().getSelectedItem();
            if (selected == null) {
                event.consume();
                Alert alert = new Alert(Alert.AlertType.WARNING, "Vui lòng chọn một chương trình khuyến mãi từ danh sách.");
                alert.showAndWait();
            }
        });

        setResultConverter(dialogButton -> {
            if (dialogButton == btnTypeSelect) {
                return tblPromotions.getSelectionModel().getSelectedItem();
            }
            if (dialogButton == btnTypeNoPromo) {
                Promotion emptyPromo = new Promotion();
                emptyPromo.setMaKhuyenMai(null);
                emptyPromo.setTenChuongTrinh("Không áp dụng");
                emptyPromo.setPhanTramGiam(0);
                return emptyPromo;
            }
            return null;
        });
    }

    private void loadActivePromotions() {
        try {
            List<Promotion> all = promotionController.layDanhSachKhuyenMai();
            LocalDate today = LocalDate.now();

            List<Promotion> activePromotions = all.stream().filter(p -> {
                if (!p.isTrangThai()) return false;
                if (p.getNgayBatDau() != null && today.isBefore(p.getNgayBatDau())) return false;
                return p.getNgayKetThuc() == null || !today.isAfter(p.getNgayKetThuc());
            }).toList();

            masterPromotionList.setAll(activePromotions);
            updateStatusText();
        } catch (Exception ex) {
            lblStatusCount.setText("Lỗi nạp danh sách khuyến mãi: " + ex.getMessage());
        }
    }

    private void applyFilter() {
        String keyword = txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";
        filteredPromotionList.setPredicate(p -> {
            if (keyword.isEmpty()) return true;
            return (p.getMaKhuyenMai() != null && p.getMaKhuyenMai().toLowerCase().contains(keyword))
                    || (p.getTenChuongTrinh() != null && p.getTenChuongTrinh().toLowerCase().contains(keyword));
        });
        updateStatusText();
    }

    private void updateStatusText() {
        lblStatusCount.setText(String.format("Hiển thị %d / %d chương trình khuyến mãi đang áp dụng",
                filteredPromotionList.size(), masterPromotionList.size()));
    }
}

