package com.petshop.view.careservice;

import com.petshop.controller.CareServiceController;
import com.petshop.model.CareService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * Hộp thoại Thêm mới và Chỉnh sửa dịch vụ chăm sóc (CareServiceEditorDialog).
 *
 * Tuân thủ Definition of Done:
 * - Đặt tên điều khiển theo đúng tiền tố: txt, lbl, btn, chk.
 * - Ô thời lượng CHỈ CHO NHẬP SỐ (áp dụng TextFormatter).
 * - Kiểm tra dữ liệu hợp lệ trước khi lưu.
 * - Tất cả nút hiển thị đầy đủ tên (setMinWidth(Region.USE_PREF_SIZE)).
 */
public class CareServiceEditorDialog extends Dialog<CareService> {

    private final CareServiceController careServiceController;
    private final CareService editingService;
    private final boolean isCreateMode;

    // Các điều khiển tuân thủ tiền tố
    private TextField txtId;
    private TextField txtName;
    private TextField txtDuration;
    private TextArea txtDescription;
    private CheckBox chkActive;
    private Label lblError;
    private Button btnSave;
    private Button btnCancel;

    public CareServiceEditorDialog(Window owner, CareService service) {
        this.careServiceController = new CareServiceController();
        this.editingService = service;
        this.isCreateMode = (service == null);

        setTitle(isCreateMode ? "Thêm Dịch Vụ Mới - Happy Tails" : "Chỉnh Sửa Dịch Vụ - Happy Tails");
        setHeaderText(null);
        initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            initOwner(owner);
        }

        DialogPane pnlDialogPane = getDialogPane();
        pnlDialogPane.getStylesheets().add(
                getClass().getResource("/com/petshop/view/styles.css").toExternalForm()
        );
        pnlDialogPane.setPrefSize(520, 500);
        pnlDialogPane.setStyle("-fx-background-color: #f3f6f3;");

        pnlDialogPane.setContent(buildDialogContent());
        configureDialogButtons(pnlDialogPane);
        populateData();

        Platform.runLater(() -> {
            if (txtName != null) {
                txtName.requestFocus();
            }
        });
    }

    /**
     * Phương thức tiện ích tĩnh mở hộp thoại Thêm/Sửa dịch vụ.
     */
    public static Optional<CareService> showAndEdit(Window owner, CareService service) {
        CareServiceEditorDialog dlg = new CareServiceEditorDialog(owner, service);
        return dlg.showAndWait();
    }

    private VBox buildDialogContent() {
        VBox pnlContainer = new VBox(14);
        pnlContainer.setPadding(new Insets(16, 20, 10, 20));

        // Tiêu đề form
        VBox pnlHeader = new VBox(4);
        Label lblTitle = new Label(isCreateMode ? "Thêm dịch vụ chăm sóc thú cưng" : "Cập nhật dịch vụ chăm sóc");
        lblTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");
        Label lblSubtitle = new Label("Điền đầy đủ thông tin dịch vụ (*). Thời lượng chỉ cho phép nhập số nguyên.");
        lblSubtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #6e7c70;");
        pnlHeader.getChildren().addAll(lblTitle, lblSubtitle);

        GridPane pnlGrid = new GridPane();
        pnlGrid.setHgap(14);
        pnlGrid.setVgap(14);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        pnlGrid.getColumnConstraints().addAll(col1, col2);

        // 1. Mã dịch vụ (readonly)
        VBox boxId = new VBox(4);
        Label lblId = new Label("Mã dịch vụ:");
        lblId.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #4b5a50;");
        txtId = new TextField();
        txtId.setEditable(false);
        txtId.setStyle("-fx-background-color: #f7faf7; -fx-border-color: #d8e2d8; -fx-border-radius: 6px; -fx-pref-height: 36px; -fx-font-weight: bold;");
        boxId.getChildren().addAll(lblId, txtId);

        // 2. Thời lượng (CHỈ CHO NHẬP SỐ)
        VBox boxDuration = new VBox(4);
        Label lblDuration = new Label("Thời lượng (phút) (*):");
        lblDuration.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #4b5a50;");
        txtDuration = new TextField();
        txtDuration.setPromptText("Ví dụ: 30, 45, 60");
        txtDuration.setPrefHeight(36);
        txtDuration.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 6px;");
        applyNumericFilter(txtDuration); // Quy tắc DoD: chỉ cho nhập số
        boxDuration.getChildren().addAll(lblDuration, txtDuration);

        pnlGrid.add(boxId, 0, 0);
        pnlGrid.add(boxDuration, 1, 0);

        // 3. Tên dịch vụ
        VBox boxName = new VBox(4);
        Label lblName = new Label("Tên dịch vụ chăm sóc (*):");
        lblName.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #4b5a50;");
        txtName = new TextField();
        txtName.setPromptText("Ví dụ: Tắm sấy vệ sinh toàn diện");
        txtName.setPrefHeight(36);
        txtName.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 6px;");
        boxName.getChildren().addAll(lblName, txtName);
        pnlGrid.add(boxName, 0, 1, 2, 1);

        // 4. Mô tả dịch vụ
        VBox boxDescription = new VBox(4);
        Label lblDescription = new Label("Mô tả chi tiết / Quy trình thực hiện:");
        lblDescription.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #4b5a50;");
        txtDescription = new TextArea();
        txtDescription.setPromptText("Mô tả các bước thực hiện, trang thiết bị sử dụng, lưu ý đối với thú cưng...");
        txtDescription.setPrefRowCount(4);
        txtDescription.setWrapText(true);
        txtDescription.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 6px;");
        boxDescription.getChildren().addAll(lblDescription, txtDescription);
        pnlGrid.add(boxDescription, 0, 2, 2, 1);

        // 5. Trạng thái cung cấp
        VBox boxStatus = new VBox(6);
        Label lblStatus = new Label("Trạng thái hoạt động:");
        lblStatus.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #4b5a50;");
        chkActive = new CheckBox("Đang mở cung cấp dịch vụ cho khách hàng");
        chkActive.setSelected(true);
        chkActive.setStyle("-fx-font-size: 12.5px; -fx-font-weight: 600; -fx-text-fill: #2c6b50;");
        boxStatus.getChildren().addAll(lblStatus, chkActive);
        pnlGrid.add(boxStatus, 0, 3, 2, 1);

        // Thông báo lỗi
        lblError = new Label();
        lblError.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px; -fx-font-weight: 600;");
        lblError.setWrapText(true);

        pnlContainer.getChildren().addAll(pnlHeader, pnlGrid, lblError);
        return pnlContainer;
    }

    /**
     * Quy tắc Definition of Done: Ô thời lượng chỉ cho phép nhập số nguyên.
     */
    private void applyNumericFilter(TextField textField) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change;
            }
            return null;
        };
        textField.setTextFormatter(new TextFormatter<>(filter));
    }

    private void configureDialogButtons(DialogPane pnlDialogPane) {
        ButtonType btnTypeSave = new ButtonType("Lưu thông tin", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnTypeCancel = new ButtonType("Hủy bỏ", ButtonBar.ButtonData.CANCEL_CLOSE);

        pnlDialogPane.getButtonTypes().addAll(btnTypeSave, btnTypeCancel);

        btnSave = (Button) pnlDialogPane.lookupButton(btnTypeSave);
        btnCancel = (Button) pnlDialogPane.lookupButton(btnTypeCancel);

        // Bắt buộc hiển thị tên đầy đủ không co rút "..."
        btnSave.setMinWidth(Region.USE_PREF_SIZE);
        btnCancel.setMinWidth(Region.USE_PREF_SIZE);

        btnSave.getStyleClass().addAll("customer-add");
        btnCancel.getStyleClass().addAll("customer-secondary");

        btnSave.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!validateAndSave()) {
                event.consume();
            }
        });

        setResultConverter(dialogButton -> {
            if (dialogButton == btnTypeSave) {
                return buildServiceFromFields();
            }
            return null;
        });
    }

    private void populateData() {
        if (isCreateMode) {
            txtId.setText(careServiceController.generateNextServiceId());
            txtDuration.setText("30");
            chkActive.setSelected(true);
        } else {
            txtId.setText(editingService.getMaDichVu());
            txtName.setText(editingService.getTenDichVu());
            txtDuration.setText(String.valueOf(editingService.getThoiLuong()));
            txtDescription.setText(editingService.getMoTa() != null ? editingService.getMoTa() : "");
            chkActive.setSelected(editingService.isTrangThai());
        }
    }

    private boolean validateAndSave() {
        lblError.setText("");

        String name = txtName.getText() != null ? txtName.getText().trim() : "";
        if (name.isEmpty()) {
            lblError.setText("⚠ Tên dịch vụ chăm sóc không được để trống.");
            txtName.requestFocus();
            return false;
        }

        String durationStr = txtDuration.getText() != null ? txtDuration.getText().trim() : "";
        if (durationStr.isEmpty()) {
            lblError.setText("⚠ Thời lượng dịch vụ không được để trống.");
            txtDuration.requestFocus();
            return false;
        }

        try {
            int duration = Integer.parseInt(durationStr);
            if (duration <= 0) {
                lblError.setText("⚠ Thời lượng dịch vụ phải lớn hơn 0 phút.");
                txtDuration.requestFocus();
                return false;
            }

            CareService service = buildServiceFromFields();
            return careServiceController.saveCareService(service, isCreateMode);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            lblError.setText("⚠ " + ex.getMessage());
            return false;
        } catch (Exception ex) {
            lblError.setText("⚠ Lỗi không xác định: " + ex.getMessage());
            return false;
        }
    }

    private CareService buildServiceFromFields() {
        String id = txtId.getText().trim();
        String name = txtName.getText().trim();
        int duration = txtDuration.getText().trim().isEmpty() ? 30 : Integer.parseInt(txtDuration.getText().trim());
        String description = txtDescription.getText() != null ? txtDescription.getText().trim() : "";
        boolean active = chkActive.isSelected();

        return new CareService(id, name, description, duration, active);
    }
}

