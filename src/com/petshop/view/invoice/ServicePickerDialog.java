package com.petshop.view.invoice;

import com.petshop.controller.CareServiceController;
import com.petshop.controller.PetController;
import com.petshop.model.CareService;
import com.petshop.model.Customer;
import com.petshop.model.InvoiceItem;
import com.petshop.model.Pet;
import com.petshop.model.ServicePricing;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Window;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Hộp thoại chọn Dịch vụ chăm sóc thú cưng và tính giá theo số cân nặng.
 * Tuân thủ tiêu chuẩn:
 * - Tên lớp là danh từ (ServicePickerDialog).
 * - Tên điều khiển có tiền tố đúng chuẩn: cbo, txt, rad, lbl, btn.
 * - Tự động đồng bộ phân cấp giá 3 bậc: Nhỏ (< 5kg), Vừa (5 - 10kg), Lớn (> 10kg).
 * - Xử lý thông minh: nếu khách có thú cưng đăng ký thì chọn từ danh sách; nếu khách vãng lai
 *   thì cho nhập trực tiếp tên (tùy chọn) và cân nặng mà không hiển thị dropdown rỗng/gượng ép.
 */
public class ServicePickerDialog extends Dialog<InvoiceItem> {

    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0 'đ'");
    private final CareServiceController careServiceController = new CareServiceController();
    private final PetController petController = new PetController();

    private ComboBox<CareService> cboCareService;
    private ComboBox<PetWrapper> cboCustomerPet;
    private TextField txtPetName;
    private TextField txtPetWeight;
    private RadioButton radTierSmall;
    private RadioButton radTierMedium;
    private RadioButton radTierLarge;
    private ToggleGroup grpSizeTier;
    private Label lblCalculatedPrice;
    private Label lblTierExplanation;
    private Label lblServiceDuration;
    private Label lblPriceBadgeSmall;
    private Label lblPriceBadgeMedium;
    private Label lblPriceBadgeLarge;

    private final Customer selectedCustomer;
    private final List<CareService> activeServices;
    private final List<Pet> customerPets = new ArrayList<>();
    private final boolean hasRegisteredPets;

    /**
     * Vỏ bọc hiển thị thú cưng của khách hàng hoặc tùy chọn thú cưng khác.
     */
    private static class PetWrapper {
        private final Pet pet;
        private final String customLabel;

        public PetWrapper(Pet pet) {
            this.pet = pet;
            this.customLabel = null;
        }

        public PetWrapper(String customLabel) {
            this.pet = null;
            this.customLabel = customLabel;
        }

        public Pet getPet() {
            return pet;
        }

        public boolean isCustom() {
            return pet == null;
        }

        @Override
        public String toString() {
            if (pet != null) {
                String breed = (pet.getGiongLoai() != null && !pet.getGiongLoai().isBlank())
                        ? " (" + pet.getGiongLoai() + " - " + pet.getCanNang() + " kg)"
                        : " (" + pet.getCanNang() + " kg)";
                return "🐾 " + pet.getTenThuCung() + breed;
            }
            return customLabel != null ? customLabel : "＋ Thú cưng khác / Chưa đăng ký";
        }
    }

    public ServicePickerDialog(Window owner, Customer customer, List<CareService> activeServices) {
        this.selectedCustomer = customer;
        this.activeServices = (activeServices != null && !activeServices.isEmpty())
                ? activeServices
                : careServiceController.getActiveCareServices();

        // Nạp thú cưng của khách nếu đã chọn khách
        if (selectedCustomer != null && selectedCustomer.getMaKhachHang() != null) {
            try {
                List<Pet> loaded = petController.loadPetsByCustomerId(selectedCustomer.getMaKhachHang());
                if (loaded != null && !loaded.isEmpty()) {
                    customerPets.addAll(loaded);
                }
            } catch (Exception ex) {
                System.err.println("Lỗi nạp thú cưng của khách: " + ex.getMessage());
            }
        }
        this.hasRegisteredPets = !customerPets.isEmpty();

        initOwner(owner);
        setTitle("Chọn Dịch Vụ & Tính Giá Theo Cân Nặng - Happy Tails");
        setHeaderText(null);

        DialogPane pnlDialogPane = getDialogPane();
        pnlDialogPane.setContent(buildDialogContent());

        ButtonType btnTypeSave = new ButtonType("Thêm vào hóa đơn", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnTypeCancel = new ButtonType("Hủy bỏ", ButtonBar.ButtonData.CANCEL_CLOSE);
        pnlDialogPane.getButtonTypes().addAll(btnTypeSave, btnTypeCancel);

        Button btnSave = (Button) pnlDialogPane.lookupButton(btnTypeSave);
        btnSave.getStyleClass().add("customer-add");
        btnSave.setStyle("-fx-background-color: #15803d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 18px; -fx-background-radius: 6px;");

        Button btnCancel = (Button) pnlDialogPane.lookupButton(btnTypeCancel);
        btnCancel.getStyleClass().add("customer-secondary");

        // Validate khi nhấn Thêm
        btnSave.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            CareService selectedSrv = cboCareService.getValue();
            if (selectedSrv == null) {
                hienThiCanhBao("Chưa chọn dịch vụ", "Vui lòng chọn dịch vụ chăm sóc thú cưng.");
                event.consume();
                return;
            }

            double weight = layCanNangHopLe();
            if (weight <= 0) {
                hienThiCanhBao("Cân nặng không hợp lệ", "Vui lòng nhập cân nặng thú cưng lớn hơn 0 kg.");
                txtPetWeight.requestFocus();
                event.consume();
                return;
            }

            double price = tinhDonGiaTheoLuaChon();
            if (price <= 0) {
                hienThiCanhBao("Chưa có giá dịch vụ", "Dịch vụ [" + selectedSrv.getTenDichVu() + "] hiện chưa có bảng giá cho bậc cân nặng này.");
                event.consume();
            }
        });

        setResultConverter(btnType -> {
            if (btnType == btnTypeSave) {
                return taoDongDichVu();
            }
            return null;
        });

        // Tự động khởi tạo giá trị đầu tiên
        if (!this.activeServices.isEmpty()) {
            cboCareService.setValue(this.activeServices.get(0));
        }

        capNhatDonGia();
    }

    public static InvoiceItem showDialog(Window owner, Customer customer, List<CareService> services) {
        ServicePickerDialog dlg = new ServicePickerDialog(owner, customer, services);
        return dlg.showAndWait().orElse(null);
    }

    private VBox buildDialogContent() {
        VBox pnlRoot = new VBox(14);
        pnlRoot.setPrefWidth(540);
        pnlRoot.setPadding(new Insets(16));
        pnlRoot.setStyle("-fx-background-color: #ffffff;");

        // 1. Chọn Dịch vụ chăm sóc
        VBox pnlServiceSection = new VBox(6);
        Label lblServiceTitle = new Label("Dịch vụ chăm sóc (*):");
        lblServiceTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12.5px; -fx-text-fill: #374151;");

        cboCareService = new ComboBox<>(FXCollections.observableArrayList(activeServices));
        cboCareService.setMaxWidth(Double.MAX_VALUE);
        cboCareService.setStyle("-fx-pref-height: 36px; -fx-font-size: 13px;");
        cboCareService.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(CareService item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getTenDichVu() + " (" + item.getThoiLuong() + " phút)");
            }
        });
        cboCareService.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(CareService item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getTenDichVu() + " (" + item.getThoiLuong() + " phút)");
            }
        });

        lblServiceDuration = new Label("Thời lượng: 30 phút");
        lblServiceDuration.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #6b7280;");

        cboCareService.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                lblServiceDuration.setText("Thời lượng: " + newVal.getThoiLuong() + " phút | Mô tả: " +
                        (newVal.getMoTa() != null ? newVal.getMoTa() : "Dịch vụ chăm sóc chuẩn Happy Tails"));
                capNhatHienThiBangGiaCuaDichVu(newVal);
                capNhatDonGia();
            }
        });
        pnlServiceSection.getChildren().addAll(lblServiceTitle, cboCareService, lblServiceDuration);

        // 2. Thông tin thú cưng & Cân nặng (Tự động thích ứng nếu là khách vãng lai hoặc khách có thú cưng)
        VBox pnlPetSection = new VBox(8);
        pnlPetSection.setPadding(new Insets(12));
        pnlPetSection.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #e5e7eb; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        Label lblPetSectionTitle = new Label("THÔNG TIN THÚ CƯNG & CÂN NẶNG");
        lblPetSectionTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");

        GridPane gridPet = new GridPane();
        gridPet.setHgap(12);
        gridPet.setVgap(10);

        Label lblWeight = new Label("Cân nặng (kg) (*):");
        lblWeight.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #4b5563;");

        txtPetWeight = new TextField();
        txtPetWeight.setPromptText("Nhập cân nặng (ví dụ: 4.5)");
        txtPetWeight.setStyle("-fx-pref-height: 34px; -fx-font-size: 13px; -fx-font-weight: bold;");
        applyDecimalFilter(txtPetWeight);

        txtPetName = new TextField();
        txtPetName.setPromptText("Tên hoặc giống thú cưng (tùy chọn)...");
        txtPetName.setStyle("-fx-pref-height: 34px; -fx-font-size: 12.5px;");

        int currentRow = 0;

        if (hasRegisteredPets) {
            // Trường hợp: Khách hàng có thú cưng đã đăng ký trong hệ thống
            Label lblPetSelect = new Label("Thú cưng đã đăng ký:");
            lblPetSelect.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #4b5563;");

            cboCustomerPet = new ComboBox<>();
            cboCustomerPet.setMaxWidth(Double.MAX_VALUE);
            cboCustomerPet.setStyle("-fx-pref-height: 34px;");

            for (Pet p : customerPets) {
                cboCustomerPet.getItems().add(new PetWrapper(p));
            }
            cboCustomerPet.getItems().add(new PetWrapper("＋ Thú cưng khác / Chưa đăng ký"));

            Label lblCustomPetName = new Label("Tên thú cưng:");
            lblCustomPetName.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #4b5563;");

            // Ẩn/hiện ô nhập tên tùy theo lựa chọn
            lblCustomPetName.setVisible(false);
            lblCustomPetName.setManaged(false);
            txtPetName.setVisible(false);
            txtPetName.setManaged(false);

            cboCustomerPet.valueProperty().addListener((obs, oldVal, chosen) -> {
                if (chosen != null && !chosen.isCustom() && chosen.getPet() != null) {
                    double w = chosen.getPet().getCanNang();
                    txtPetWeight.setText(w > 0 ? String.valueOf(w) : "4.0");
                    lblCustomPetName.setVisible(false);
                    lblCustomPetName.setManaged(false);
                    txtPetName.setVisible(false);
                    txtPetName.setManaged(false);
                } else {
                    // Chọn "Thú cưng khác"
                    txtPetWeight.clear();
                    lblCustomPetName.setVisible(true);
                    lblCustomPetName.setManaged(true);
                    txtPetName.setVisible(true);
                    txtPetName.setManaged(true);
                    txtPetName.requestFocus();
                }
            });

            cboCustomerPet.getSelectionModel().selectFirst();

            gridPet.add(lblPetSelect, 0, currentRow);
            gridPet.add(cboCustomerPet, 1, currentRow++);

            gridPet.add(lblCustomPetName, 0, currentRow);
            gridPet.add(txtPetName, 1, currentRow++);

            gridPet.add(lblWeight, 0, currentRow);
            gridPet.add(txtPetWeight, 1, currentRow++);
        } else {
            // Trường hợp: Khách vãng lai HOẶC khách chưa có thú cưng đăng ký
            // Hiển thị trực tiếp ô nhập tên thú cưng (tùy chọn) và ô cân nặng, không hiện ComboBox
            Label lblPetName = new Label("Tên thú cưng:");
            lblPetName.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #4b5563;");

            gridPet.add(lblPetName, 0, currentRow);
            gridPet.add(txtPetName, 1, currentRow++);

            gridPet.add(lblWeight, 0, currentRow);
            gridPet.add(txtPetWeight, 1, currentRow++);

            // Mặc định gợi ý cân nặng ban đầu để tính giá ngay
            txtPetWeight.setText("4.0");
        }

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(140);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        gridPet.getColumnConstraints().addAll(col1, col2);

        // Lắng nghe khi thay đổi cân nặng
        txtPetWeight.textProperty().addListener((obs, oldVal, newVal) -> {
            dongBoBacTheoCanNang();
            capNhatDonGia();
        });

        pnlPetSection.getChildren().addAll(lblPetSectionTitle, gridPet);

        // 3. 3 Bậc kích thước trực quan
        VBox pnlTiersSection = new VBox(8);
        Label lblTierTitle = new Label("Bậc kích thước & Đơn giá áp dụng:");
        lblTierTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12.5px; -fx-text-fill: #374151;");

        grpSizeTier = new ToggleGroup();

        radTierSmall = new RadioButton("Nhỏ (< 5 kg)");
        radTierSmall.setToggleGroup(grpSizeTier);
        radTierSmall.setStyle("-fx-font-size: 12.5px; -fx-font-weight: 600;");

        radTierMedium = new RadioButton("Vừa (5 - 10 kg)");
        radTierMedium.setToggleGroup(grpSizeTier);
        radTierMedium.setStyle("-fx-font-size: 12.5px; -fx-font-weight: 600;");

        radTierLarge = new RadioButton("Lớn (> 10 kg)");
        radTierLarge.setToggleGroup(grpSizeTier);
        radTierLarge.setStyle("-fx-font-size: 12.5px; -fx-font-weight: 600;");

        lblPriceBadgeSmall = createBadgeLabel("0 đ");
        lblPriceBadgeMedium = createBadgeLabel("0 đ");
        lblPriceBadgeLarge = createBadgeLabel("0 đ");

        HBox rowTierSmall = createTierRow(radTierSmall, lblPriceBadgeSmall);
        HBox rowTierMedium = createTierRow(radTierMedium, lblPriceBadgeMedium);
        HBox rowTierLarge = createTierRow(radTierLarge, lblPriceBadgeLarge);

        // Cho phép click chọn trực tiếp radio button để tự gợi ý số cân nặng tương ứng
        radTierSmall.setOnAction(e -> {
            if (layCanNangHopLe() >= 5.0 || layCanNangHopLe() <= 0) txtPetWeight.setText("3.5");
            capNhatDonGia();
        });
        radTierMedium.setOnAction(e -> {
            double w = layCanNangHopLe();
            if (w < 5.0 || w > 10.0) txtPetWeight.setText("7.0");
            capNhatDonGia();
        });
        radTierLarge.setOnAction(e -> {
            if (layCanNangHopLe() <= 10.0) txtPetWeight.setText("12.0");
            capNhatDonGia();
        });

        VBox pnlTierCards = new VBox(6, rowTierSmall, rowTierMedium, rowTierLarge);
        pnlTiersSection.getChildren().addAll(lblTierTitle, pnlTierCards);

        // 4. Khối kết quả đơn giá áp dụng
        VBox pnlResultBox = new VBox(4);
        pnlResultBox.setAlignment(Pos.CENTER);
        pnlResultBox.setPadding(new Insets(12));
        pnlResultBox.setStyle("-fx-background-color: #ecfdf5; -fx-border-color: #10b981; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        Label lblResultTitle = new Label("ĐƠN GIÁ ÁP DỤNG");
        lblResultTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #047857; -fx-letter-spacing: 1px;");

        lblCalculatedPrice = new Label("0 đ");
        lblCalculatedPrice.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #065f46;");

        lblTierExplanation = new Label("Áp dụng bậc: Nhỏ (< 5kg)");
        lblTierExplanation.setStyle("-fx-font-size: 12px; -fx-text-fill: #047857;");

        pnlResultBox.getChildren().addAll(lblResultTitle, lblCalculatedPrice, lblTierExplanation);

        pnlRoot.getChildren().addAll(pnlServiceSection, pnlPetSection, pnlTiersSection, pnlResultBox);
        return pnlRoot;
    }

    private HBox createTierRow(RadioButton rad, Label lblBadge) {
        HBox row = new HBox(12, rad, lblBadge);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 12, 6, 12));
        row.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e5e7eb; -fx-border-radius: 6px; -fx-background-radius: 6px;");
        HBox.setHgrow(rad, Priority.ALWAYS);
        return row;
    }

    private Label createBadgeLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-background-color: #f3f4f6; -fx-padding: 3px 10px; -fx-background-radius: 12px; -fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        return lbl;
    }

    private void capNhatHienThiBangGiaCuaDichVu(CareService service) {
        if (service == null) return;

        double pSmall = service.getGiaNho() > 0 ? service.getGiaNho() : careServiceController.getPriceForSize(service.getMaDichVu(), ServicePricing.BAC_NHO, null);
        double pMed = service.getGiaVua() > 0 ? service.getGiaVua() : careServiceController.getPriceForSize(service.getMaDichVu(), ServicePricing.BAC_VUA, null);
        double pLarge = service.getGiaLon() > 0 ? service.getGiaLon() : careServiceController.getPriceForSize(service.getMaDichVu(), ServicePricing.BAC_LON, null);

        lblPriceBadgeSmall.setText(pSmall > 0 ? currencyFormat.format(pSmall) : "Chưa có giá");
        lblPriceBadgeMedium.setText(pMed > 0 ? currencyFormat.format(pMed) : "Chưa có giá");
        lblPriceBadgeLarge.setText(pLarge > 0 ? currencyFormat.format(pLarge) : "Chưa có giá");
    }

    private void dongBoBacTheoCanNang() {
        double weight = layCanNangHopLe();
        if (weight <= 0) return;

        if (weight < 5.0) {
            radTierSmall.setSelected(true);
        } else if (weight <= 10.0) {
            radTierMedium.setSelected(true);
        } else {
            radTierLarge.setSelected(true);
        }
    }

    private void capNhatDonGia() {
        CareService service = cboCareService.getValue();
        if (service == null) {
            lblCalculatedPrice.setText("0 đ");
            return;
        }

        capNhatHienThiBangGiaCuaDichVu(service);

        double price = tinhDonGiaTheoLuaChon();
        lblCalculatedPrice.setText(currencyFormat.format(price));

        String sizeBracket = layTenBacKichThuocHienTai();
        double weight = layCanNangHopLe();
        lblTierExplanation.setText("Áp dụng: Bậc " + sizeBracket + " (" + weight + " kg) · " + service.getTenDichVu());
    }

    private double tinhDonGiaTheoLuaChon() {
        CareService service = cboCareService.getValue();
        if (service == null) return 0;

        String size = layTenBacKichThuocHienTai();
        double price = 0;

        if (ServicePricing.BAC_NHO.equals(size)) {
            price = service.getGiaNho();
        } else if (ServicePricing.BAC_VUA.equals(size)) {
            price = service.getGiaVua();
        } else if (ServicePricing.BAC_LON.equals(size)) {
            price = service.getGiaLon();
        }

        if (price <= 0) {
            price = careServiceController.getPriceForSize(service.getMaDichVu(), size, null);
        }

        return price;
    }

    private String layTenBacKichThuocHienTai() {
        if (radTierLarge.isSelected()) return ServicePricing.BAC_LON;
        if (radTierMedium.isSelected()) return ServicePricing.BAC_VUA;
        return ServicePricing.BAC_NHO;
    }

    private double layCanNangHopLe() {
        String text = txtPetWeight.getText();
        if (text == null || text.trim().isEmpty()) return 0;
        try {
            return Double.parseDouble(text.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private InvoiceItem taoDongDichVu() {
        CareService srv = cboCareService.getValue();
        double weight = layCanNangHopLe();
        String size = layTenBacKichThuocHienTai();
        double price = tinhDonGiaTheoLuaChon();

        String petName = null;
        if (hasRegisteredPets && cboCustomerPet != null && cboCustomerPet.getValue() != null) {
            PetWrapper pw = cboCustomerPet.getValue();
            if (!pw.isCustom() && pw.getPet() != null) {
                petName = pw.getPet().getTenThuCung();
            } else if (txtPetName != null && !txtPetName.getText().trim().isEmpty()) {
                petName = txtPetName.getText().trim();
            }
        } else if (txtPetName != null && !txtPetName.getText().trim().isEmpty()) {
            petName = txtPetName.getText().trim();
        }

        StringBuilder sb = new StringBuilder(srv.getTenDichVu());
        sb.append(" (");
        if (petName != null && !petName.isBlank()) {
            sb.append(petName).append(" · ");
        }
        sb.append(weight).append("kg - Bậc ").append(size).append(")");

        return InvoiceItem.createServiceItem(srv.getMaDichVu(), sb.toString(), price);
    }

    private void applyDecimalFilter(TextField textField) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*([.,]\\d*)?")) {
                return change;
            }
            return null;
        };
        textField.setTextFormatter(new TextFormatter<>(filter));
    }

    private void hienThiCanhBao(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner(getDialogPane().getScene().getWindow());
        alert.showAndWait();
    }
}
