package com.petshop.view.careservice;

import com.petshop.controller.CareServiceController;
import com.petshop.model.CareService;
import com.petshop.model.ServicePricing;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * Hộp thoại Thiết lập & Điều chỉnh Bảng giá dịch vụ (CareServicePricingDialog).
 *
 * Tuân thủ Definition of Done:
 * - Đặt tên điều khiển đúng tiền tố: txt, cbo, dtp, lbl, btn.
 * - Ô đơn giá CHỈ CHO NHẬP SỐ (áp dụng TextFormatter).
 * - Ngày áp dụng và ngày hết hạn dùng DatePicker.
 * - Các nút hiển thị đầy đủ tên không co rút "...".
 */
public class CareServicePricingDialog extends Dialog<ServicePricing> {

    private final CareServiceController careServiceController;
    private final ServicePricing editingPricing;
    private final boolean isCreateMode;
    private final CareService presetService;

    // Các điều khiển tuân thủ tiền tố
    private TextField txtPricingId;
    private ComboBox<CareService> cboService;
    private ComboBox<String> cboSizeTier;
    private TextField txtPrice;
    private DatePicker dtpEffectiveDate;
    private DatePicker dtpExpiryDate;
    private Label lblError;
    private Button btnSave;
    private Button btnCancel;

    public CareServicePricingDialog(Window owner, ServicePricing pricing, CareService presetService) {
        this.careServiceController = new CareServiceController();
        this.editingPricing = pricing;
        this.isCreateMode = (pricing == null);
        this.presetService = presetService;

        setTitle(isCreateMode ? "Thiết Lập Giá Dịch Vụ Mới - Happy Tails" : "Điều Chỉnh Bảng Giá Dịch Vụ - Happy Tails");
        setHeaderText(null);
        initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            initOwner(owner);
        }

        DialogPane pnlDialogPane = getDialogPane();
        pnlDialogPane.getStylesheets().add(
                getClass().getResource("/com/petshop/view/styles.css").toExternalForm()
        );
        pnlDialogPane.setPrefSize(540, 520);
        pnlDialogPane.setStyle("-fx-background-color: #f3f6f3;");

        pnlDialogPane.setContent(buildDialogContent());
        configureDialogButtons(pnlDialogPane);
        populateData();

        Platform.runLater(() -> {
            if (txtPrice != null) {
                txtPrice.requestFocus();
            }
        });
    }

    public static Optional<ServicePricing> showAndEdit(Window owner, ServicePricing pricing, CareService presetService) {
        CareServicePricingDialog dlg = new CareServicePricingDialog(owner, pricing, presetService);
        return dlg.showAndWait();
    }

    private VBox buildDialogContent() {
        VBox pnlContainer = new VBox(14);
        pnlContainer.setPadding(new Insets(16, 20, 10, 20));

        // Tiêu đề form
        VBox pnlHeader = new VBox(4);
        Label lblTitle = new Label(isCreateMode ? "Thiết lập bảng giá dịch vụ theo bậc kích thước" : "Điều chỉnh thông tin mức giá dịch vụ");
        lblTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");
        Label lblSubtitle = new Label("Đơn giá chỉ cho phép nhập số. Chọn bậc kích cỡ và khoảng ngày áp dụng.");
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

        // 1. Mã bảng giá (readonly)
        VBox boxPricingId = new VBox(4);
        Label lblPricingId = new Label("Mã bảng giá:");
        lblPricingId.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #4b5a50;");
        txtPricingId = new TextField();
        txtPricingId.setEditable(false);
        txtPricingId.setStyle("-fx-background-color: #f7faf7; -fx-border-color: #d8e2d8; -fx-border-radius: 6px; -fx-pref-height: 36px; -fx-font-weight: bold;");
        boxPricingId.getChildren().addAll(lblPricingId, txtPricingId);

        // 2. Dịch vụ áp dụng
        VBox boxService = new VBox(4);
        Label lblService = new Label("Dịch vụ chăm sóc (*):");
        lblService.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #4b5a50;");
        cboService = new ComboBox<>();
        cboService.setMaxWidth(Double.MAX_VALUE);
        cboService.setPrefHeight(36);
        cboService.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 6px;");
        setupServiceComboBox();
        boxService.getChildren().addAll(lblService, cboService);

        pnlGrid.add(boxPricingId, 0, 0);
        pnlGrid.add(boxService, 1, 0);

        // 3. Phân loại kích thước thú cưng
        VBox boxSize = new VBox(4);
        Label lblSize = new Label("Bậc kích thước thú cưng (*):");
        lblSize.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #4b5a50;");
        cboSizeTier = new ComboBox<>(FXCollections.observableArrayList(
                ServicePricing.BAC_NHO,  // Nhỏ (<5kg)
                ServicePricing.BAC_VUA,  // Vừa (5-10kg)
                ServicePricing.BAC_LON   // Lớn (>10kg)
        ));
        cboSizeTier.setMaxWidth(Double.MAX_VALUE);
        cboSizeTier.setPrefHeight(36);
        cboSizeTier.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 6px;");
        boxSize.getChildren().addAll(lblSize, cboSizeTier);

        // 4. Đơn giá (CHỈ CHO NHẬP SỐ)
        VBox boxPrice = new VBox(4);
        Label lblPrice = new Label("Đơn giá (VNĐ) (*):");
        lblPrice.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #4b5a50;");
        txtPrice = new TextField();
        txtPrice.setPromptText("Ví dụ: 120000");
        txtPrice.setPrefHeight(36);
        txtPrice.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 6px;");
        applyNumericFilter(txtPrice); // Bắt buộc chỉ cho nhập số
        boxPrice.getChildren().addAll(lblPrice, txtPrice);

        pnlGrid.add(boxSize, 0, 1);
        pnlGrid.add(boxPrice, 1, 1);

        // 5. Ngày áp dụng (DatePicker)
        VBox boxEffectiveDate = new VBox(4);
        Label lblEffectiveDate = new Label("Ngày bắt đầu áp dụng (*):");
        lblEffectiveDate.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #4b5a50;");
        dtpEffectiveDate = new DatePicker(LocalDate.now());
        dtpEffectiveDate.setMaxWidth(Double.MAX_VALUE);
        dtpEffectiveDate.setPrefHeight(36);
        dtpEffectiveDate.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 6px;");
        boxEffectiveDate.getChildren().addAll(lblEffectiveDate, dtpEffectiveDate);

        // 6. Ngày hết hiệu lực (DatePicker, nullable)
        VBox boxExpiryDate = new VBox(4);
        Label lblExpiryDate = new Label("Ngày hết hiệu lực (để trống nếu vô thời hạn):");
        lblExpiryDate.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #4b5a50;");
        dtpExpiryDate = new DatePicker();
        dtpExpiryDate.setMaxWidth(Double.MAX_VALUE);
        dtpExpiryDate.setPrefHeight(36);
        dtpExpiryDate.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1ded1; -fx-border-radius: 6px;");
        boxExpiryDate.getChildren().addAll(lblExpiryDate, dtpExpiryDate);

        pnlGrid.add(boxEffectiveDate, 0, 2);
        pnlGrid.add(boxExpiryDate, 1, 2);

        // Thông báo lỗi
        lblError = new Label();
        lblError.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px; -fx-font-weight: 600;");
        lblError.setWrapText(true);

        pnlContainer.getChildren().addAll(pnlHeader, pnlGrid, lblError);
        return pnlContainer;
    }

    /**
     * Quy tắc Definition of Done: Ô đơn giá chỉ cho phép nhập số nguyên.
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

    private void setupServiceComboBox() {
        List<CareService> services = careServiceController.getActiveCareServices();
        cboService.setItems(FXCollections.observableArrayList(services));
        cboService.setConverter(new StringConverter<>() {
            @Override
            public String toString(CareService s) {
                return s != null ? s.getTenDichVu() + " (" + s.getMaDichVu() + ")" : "";
            }

            @Override
            public CareService fromString(String string) {
                return null;
            }
        });
    }

    private void configureDialogButtons(DialogPane pnlDialogPane) {
        ButtonType btnTypeSave = new ButtonType("Lưu bảng giá", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnTypeCancel = new ButtonType("Hủy bỏ", ButtonBar.ButtonData.CANCEL_CLOSE);

        pnlDialogPane.getButtonTypes().addAll(btnTypeSave, btnTypeCancel);

        btnSave = (Button) pnlDialogPane.lookupButton(btnTypeSave);
        btnCancel = (Button) pnlDialogPane.lookupButton(btnTypeCancel);

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
                return buildPricingFromFields();
            }
            return null;
        });
    }

    private void populateData() {
        if (isCreateMode) {
            txtPricingId.setText(careServiceController.generateNextPricingId());
            cboSizeTier.setValue(ServicePricing.BAC_NHO);
            dtpEffectiveDate.setValue(LocalDate.now());

            if (presetService != null) {
                for (CareService s : cboService.getItems()) {
                    if (s.getMaDichVu().equals(presetService.getMaDichVu())) {
                        cboService.setValue(s);
                        cboService.setDisable(true);
                        break;
                    }
                }
            } else if (!cboService.getItems().isEmpty()) {
                cboService.setValue(cboService.getItems().get(0));
            }
        } else {
            txtPricingId.setText(editingPricing.getMaBangGia());
            cboSizeTier.setValue(editingPricing.getPhanLoaiKichThuoc());
            txtPrice.setText(String.format("%.0f", editingPricing.getGia()));
            dtpEffectiveDate.setValue(editingPricing.getNgayApDung());
            dtpExpiryDate.setValue(editingPricing.getNgayHetHieuLuc());

            for (CareService s : cboService.getItems()) {
                if (s.getMaDichVu().equals(editingPricing.getMaDichVu())) {
                    cboService.setValue(s);
                    break;
                }
            }
        }
    }

    private boolean validateAndSave() {
        lblError.setText("");

        CareService selectedService = cboService.getValue();
        if (selectedService == null) {
            lblError.setText("⚠ Vui lòng chọn dịch vụ áp dụng bảng giá.");
            cboService.requestFocus();
            return false;
        }

        String size = cboSizeTier.getValue();
        if (size == null || size.trim().isEmpty()) {
            lblError.setText("⚠ Vui lòng chọn bậc kích thước thú cưng.");
            cboSizeTier.requestFocus();
            return false;
        }

        String priceStr = txtPrice.getText() != null ? txtPrice.getText().trim() : "";
        if (priceStr.isEmpty()) {
            lblError.setText("⚠ Đơn giá không được để trống.");
            txtPrice.requestFocus();
            return false;
        }

        LocalDate effDate = dtpEffectiveDate.getValue();
        if (effDate == null) {
            lblError.setText("⚠ Vui lòng chọn ngày bắt đầu áp dụng.");
            dtpEffectiveDate.requestFocus();
            return false;
        }

        LocalDate expDate = dtpExpiryDate.getValue();
        if (expDate != null && expDate.isBefore(effDate)) {
            lblError.setText("⚠ Ngày hết hiệu lực phải lớn hơn hoặc bằng ngày bắt đầu áp dụng.");
            dtpExpiryDate.requestFocus();
            return false;
        }

        try {
            double price = Double.parseDouble(priceStr);
            if (price < 0) {
                lblError.setText("⚠ Đơn giá phải lớn hơn hoặc bằng 0.");
                txtPrice.requestFocus();
                return false;
            }

            ServicePricing pricing = buildPricingFromFields();
            return careServiceController.saveServicePricing(pricing, isCreateMode);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            lblError.setText("⚠ " + ex.getMessage());
            return false;
        } catch (Exception ex) {
            lblError.setText("⚠ Lỗi không xác định: " + ex.getMessage());
            return false;
        }
    }

    private ServicePricing buildPricingFromFields() {
        String id = txtPricingId.getText().trim();
        CareService service = cboService.getValue();
        String serviceId = service != null ? service.getMaDichVu() : "";
        String serviceName = service != null ? service.getTenDichVu() : "";
        double price = txtPrice.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtPrice.getText().trim());
        String size = cboSizeTier.getValue();
        LocalDate effDate = dtpEffectiveDate.getValue();
        LocalDate expDate = dtpExpiryDate.getValue();

        ServicePricing p = new ServicePricing(id, serviceId, price, size, effDate, expDate);
        p.setTenDichVu(serviceName);
        return p;
    }
}

