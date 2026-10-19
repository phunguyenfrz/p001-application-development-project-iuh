package com.petshop.view;

import com.petshop.controller.PromotionController;
import com.petshop.model.Promotion;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Hộp thoại modal PromotionEditorDialog dùng cho 2 nghiệp vụ:
 * 1. Thêm khuyến mãi mới (Modal 2.1A)
 * 2. Cập nhật khuyến mãi đã có (Modal 2.1A · Sửa khuyến mãi)
 *
 * Tuân thủ quy ước đặt tên:
 * - Tiền tố điều khiển: btn, txt, lbl, dtp, cbo.
 * - Tên lớp là danh từ (PromotionEditorDialog), tên phương thức là động từ.
 */
public class PromotionEditorDialog extends StackPane {

    public enum CheDo {
        THEM_MOI,
        CAP_NHAT
    }

    private final CheDo cheDoHienTai;
    private final PromotionController promotionController;
    private final Promotion promotionHienTai;
    private final Consumer<Promotion> onThanhCong;
    private final Runnable onDong;

    // Các thành phần điều khiển giao diện theo đúng tiền tố
    private Label lblTieuDe;
    private Label lblPhuDe;
    private Button btnDong;

    private Label lblMaKhuyenMai;
    private TextField txtMaKhuyenMai;

    private Label lblTenChuongTrinh;
    private TextField txtTenChuongTrinh;

    private Label lblPhanTramGiam;
    private TextField txtPhanTramGiam;

    private Label lblTrangThai;
    private ComboBox<String> cboTrangThai;

    private Label lblNgayBatDau;
    private DatePicker dtpNgayBatDau;

    private Label lblNgayKetThuc;
    private DatePicker dtpNgayKetThuc;

    private Label lblGhiChu;
    private Label lblBaoLoi;

    private Button btnHuy;
    private Button btnLuuKhuyenMai;

    // Lớp overlay nội bộ để hiển thị hộp thoại xác nhận chỉnh sửa bên trên form
    private StackPane pnlInnerOverlay;

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Khởi tạo hộp thoại thêm hoặc sửa khuyến mãi.
     *
     * @param cheDo               Chế độ thêm mới hoặc cập nhật
     * @param promotionController Bộ điều khiển khuyến mãi
     * @param promotion           Khuyến mãi cần sửa (nếu là cập nhật) hoặc null (nếu thêm mới)
     * @param onThanhCong         Callback thực hiện khi lưu thành công
     * @param onDong              Callback khi đóng hộp thoại
     */
    public PromotionEditorDialog(CheDo cheDo, PromotionController promotionController,
                                 Promotion promotion, Consumer<Promotion> onThanhCong, Runnable onDong) {
        this.cheDoHienTai = cheDo;
        this.promotionController = promotionController;
        this.promotionHienTai = promotion;
        this.onThanhCong = onThanhCong;
        this.onDong = onDong;

        getStyleClass().add("modal-overlay");

        khoiTaoGiaoDien();
        ganSuKien();
        napDuLieu();
    }

    /**
     * Dựng toàn bộ cấu trúc giao diện hộp thoại theo đúng mẫu ảnh thiết kế.
     */
    private void khoiTaoGiaoDien() {
        VBox pnlCard = new VBox(18);
        pnlCard.getStyleClass().add("modal-card");

        // 1. Header (Tiêu đề + nút đóng X)
        BorderPane pnlHeader = new BorderPane();

        VBox pnlTitleBox = new VBox(3);
        String tieuDeText = (cheDoHienTai == CheDo.THEM_MOI) ? "Thêm khuyến mãi" : "Sửa khuyến mãi";
        String phuDeText = (cheDoHienTai == CheDo.THEM_MOI)
                ? "Thiết lập chương trình ưu đãi và thời gian áp dụng."
                : "Cập nhật chương trình ưu đãi và thời gian áp dụng.";

        lblTieuDe = new Label(tieuDeText);
        lblTieuDe.getStyleClass().add("modal-header-title");

        lblPhuDe = new Label(phuDeText);
        lblPhuDe.getStyleClass().add("modal-header-sub");

        pnlTitleBox.getChildren().addAll(lblTieuDe, lblPhuDe);

        btnDong = new Button("✕");
        btnDong.getStyleClass().add("modal-close-button");

        pnlHeader.setLeft(pnlTitleBox);
        pnlHeader.setRight(btnDong);

        // 2. Form Grid nhập liệu 2 cột
        GridPane pnlGrid = new GridPane();
        pnlGrid.setHgap(16);
        pnlGrid.setVgap(14);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        pnlGrid.getColumnConstraints().addAll(col1, col2);

        // --- Hàng 1: Mã khuyến mãi (readonly) & Tên chương trình (*) ---
        lblMaKhuyenMai = new Label("Mã khuyến mãi");
        lblMaKhuyenMai.getStyleClass().add("modal-field-label");

        txtMaKhuyenMai = new TextField();
        txtMaKhuyenMai.getStyleClass().add("modal-text-input-readonly");
        txtMaKhuyenMai.setEditable(false);

        // Icon ổ khóa biểu thị trường chỉ đọc
        SVGPath lockSvg = new SVGPath();
        lockSvg.setContent("M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z");
        lockSvg.setFill(Color.web("#9ca3af"));
        lockSvg.setScaleX(0.55);
        lockSvg.setScaleY(0.55);

        StackPane pnlMaContainer = new StackPane();
        pnlMaContainer.getChildren().addAll(txtMaKhuyenMai, lockSvg);
        StackPane.setAlignment(lockSvg, Pos.CENTER_RIGHT);
        StackPane.setMargin(lockSvg, new Insets(0, 10, 0, 0));

        VBox pnlMaBox = new VBox(6, lblMaKhuyenMai, pnlMaContainer);

        lblTenChuongTrinh = taoNhanCoDauSao("Tên chương trình");
        txtTenChuongTrinh = new TextField();
        txtTenChuongTrinh.setPromptText("Nhập tên chương trình");
        txtTenChuongTrinh.getStyleClass().add("modal-text-input");
        VBox pnlTenBox = new VBox(6, lblTenChuongTrinh, txtTenChuongTrinh);

        pnlGrid.add(pnlMaBox, 0, 0);
        pnlGrid.add(pnlTenBox, 1, 0);

        // --- Hàng 2: Phần trăm giảm (*) & Trạng thái ---
        lblPhanTramGiam = taoNhanCoDauSao("Phần trăm giảm");

        txtPhanTramGiam = new TextField();
        txtPhanTramGiam.setPromptText("Nhập số từ 1 đến 100");
        txtPhanTramGiam.getStyleClass().add("modal-text-input");

        // RÀNG BUỘC: Ô số chỉ cho phép nhập số
        UnaryOperator<TextFormatter.Change> chiChoPhepNhapSo = change -> {
            String noiDungMoi = change.getControlNewText();
            if (noiDungMoi.matches("([1-9][0-9]?|100)?")) {
                return change;
            }
            return null;
        };
        txtPhanTramGiam.setTextFormatter(new TextFormatter<>(chiChoPhepNhapSo));

        Label lblKyHieuPhanTram = new Label("%");
        lblKyHieuPhanTram.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 13px; -fx-font-weight: bold;");

        StackPane pnlPhanTramContainer = new StackPane();
        pnlPhanTramContainer.getChildren().addAll(txtPhanTramGiam, lblKyHieuPhanTram);
        StackPane.setAlignment(lblKyHieuPhanTram, Pos.CENTER_RIGHT);
        StackPane.setMargin(lblKyHieuPhanTram, new Insets(0, 12, 0, 0));

        VBox pnlPhanTramBox = new VBox(6, lblPhanTramGiam, pnlPhanTramContainer);

        lblTrangThai = new Label("Trạng thái");
        lblTrangThai.getStyleClass().add("modal-field-label");

        cboTrangThai = new ComboBox<>();
        cboTrangThai.getItems().addAll("Đang áp dụng", "Sắp áp dụng", "Ngừng áp dụng");
        cboTrangThai.setPromptText("Chọn trạng thái");
        cboTrangThai.setMaxWidth(Double.MAX_VALUE);
        cboTrangThai.getStyleClass().add("modal-text-input");

        VBox pnlTrangThaiBox = new VBox(6, lblTrangThai, cboTrangThai);

        pnlGrid.add(pnlPhanTramBox, 0, 1);
        pnlGrid.add(pnlTrangThaiBox, 1, 1);

        // --- Hàng 3: Ngày bắt đầu & Ngày kết thúc ---
        lblNgayBatDau = new Label("Ngày bắt đầu");
        lblNgayBatDau.getStyleClass().add("modal-field-label");

        dtpNgayBatDau = new DatePicker();
        dtpNgayBatDau.setPromptText("dd/mm/yyyy");
        dtpNgayBatDau.setMaxWidth(Double.MAX_VALUE);
        caiDatDinhDangDatePicker(dtpNgayBatDau);

        VBox pnlBatDauBox = new VBox(6, lblNgayBatDau, dtpNgayBatDau);

        lblNgayKetThuc = new Label("Ngày kết thúc");
        lblNgayKetThuc.getStyleClass().add("modal-field-label");

        dtpNgayKetThuc = new DatePicker();
        dtpNgayKetThuc.setPromptText("dd/mm/yyyy");
        dtpNgayKetThuc.setMaxWidth(Double.MAX_VALUE);
        caiDatDinhDangDatePicker(dtpNgayKetThuc);

        VBox pnlKetThucBox = new VBox(6, lblNgayKetThuc, dtpNgayKetThuc);

        pnlGrid.add(pnlBatDauBox, 0, 2);
        pnlGrid.add(pnlKetThucBox, 1, 2);

        // 3. Khối thông báo hướng dẫn (Info Callout Box)
        HBox pnlInfoCallout = new HBox(8);
        pnlInfoCallout.setAlignment(Pos.CENTER_LEFT);
        pnlInfoCallout.getStyleClass().add("callout-info");

        Label lblInfoIcon = new Label("ⓘ");
        lblInfoIcon.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        lblGhiChu = new Label("Chỉ nhập số cho phần trăm giảm. Ngày kết thúc không được trước ngày bắt đầu.");
        lblGhiChu.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        lblGhiChu.setWrapText(true);

        pnlInfoCallout.getChildren().addAll(lblInfoIcon, lblGhiChu);

        // Thông báo lỗi nếu kiểm tra tính hợp lệ không đạt
        lblBaoLoi = new Label();
        lblBaoLoi.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px; -fx-font-weight: 600;");
        lblBaoLoi.setWrapText(true);
        lblBaoLoi.setVisible(false);
        lblBaoLoi.setManaged(false);

        // 4. Thanh nút bấm phía dưới (Hủy & Lưu khuyến mãi)
        HBox pnlActions = new HBox(12);
        pnlActions.setAlignment(Pos.CENTER_RIGHT);

        btnHuy = new Button("Hủy");
        btnHuy.getStyleClass().add("btn-action-secondary");
        btnHuy.setPrefWidth(90);

        btnLuuKhuyenMai = new Button("Lưu khuyến mãi");
        btnLuuKhuyenMai.getStyleClass().add("btn-orange");

        pnlActions.getChildren().addAll(btnHuy, btnLuuKhuyenMai);

        // Gắn vào Card
        pnlCard.getChildren().addAll(
                pnlHeader,
                pnlGrid,
                pnlInfoCallout,
                lblBaoLoi,
                pnlActions
        );

        // Lớp phủ nội bộ dùng để hiển thị hộp thoại xác nhận chỉnh sửa
        pnlInnerOverlay = new StackPane();
        pnlInnerOverlay.setVisible(false);
        pnlInnerOverlay.setManaged(false);
        pnlInnerOverlay.setStyle("-fx-background-color: rgba(15, 23, 42, 0.45);");

        getChildren().addAll(pnlCard, pnlInnerOverlay);
    }

    /**
     * Gắn các bộ lắng nghe sự kiện tương tác người dùng.
     */
    private void ganSuKien() {
        // Đóng hộp thoại
        btnDong.setOnAction(e -> dongHopThoai());
        btnHuy.setOnAction(e -> dongHopThoai());

        // Bấm ra ngoài nền đen để đóng
        setOnMouseClicked(e -> {
            if (e.getTarget() == this) {
                dongHopThoai();
            }
        });

        // Nút lưu khuyến mãi
        btnLuuKhuyenMai.setOnAction(e -> xuLyLuuKhuyenMai());
    }

    /**
     * Nạp dữ liệu ban đầu lên form dựa theo chế độ Thêm mới hoặc Cập nhật.
     */
    private void napDuLieu() {
        if (cheDoHienTai == CheDo.THEM_MOI) {
            // Tự động sinh mã tiếp theo
            String maMoi = promotionController.sinhMaKhuyenMaiMoi();
            txtMaKhuyenMai.setText(maMoi);
            cboTrangThai.setValue("Đang áp dụng");
            dtpNgayBatDau.setValue(LocalDate.now());
            dtpNgayKetThuc.setValue(LocalDate.now().plusMonths(1));
        } else if (promotionHienTai != null) {
            // Nạp dữ liệu hiện tại lên form sửa
            txtMaKhuyenMai.setText(promotionHienTai.getMaKhuyenMai());
            txtTenChuongTrinh.setText(promotionHienTai.getTenChuongTrinh());
            txtPhanTramGiam.setText(String.valueOf((int) promotionHienTai.getPhanTramGiam()));

            cboTrangThai.setValue(promotionHienTai.layTrangThaiHienThi());
            dtpNgayBatDau.setValue(promotionHienTai.getNgayBatDau());
            dtpNgayKetThuc.setValue(promotionHienTai.getNgayKetThuc());
        }
    }

    /**
     * Thực hiện kiểm tra nghiệp vụ và lưu thông tin khuyến mãi.
     * - Chế độ THEM_MOI: Lưu trực tiếp vào CSDL sau khi kiểm tra hợp lệ.
     * - Chế độ CAP_NHAT: Hiển thị hộp thoại Xác nhận chỉnh sửa (so sánh trước/sau) rồi mới lưu.
     */
    private void xuLyLuuKhuyenMai() {
        lblBaoLoi.setVisible(false);
        lblBaoLoi.setManaged(false);

        String maKhuyenMai = txtMaKhuyenMai.getText().trim();
        String tenChuongTrinh = txtTenChuongTrinh.getText().trim();
        String phanTramStr = txtPhanTramGiam.getText().trim();
        String trangThaiStr = cboTrangThai.getValue();
        LocalDate ngayBatDau = dtpNgayBatDau.getValue();
        LocalDate ngayKetThuc = dtpNgayKetThuc.getValue();

        // 1. Kiểm tra rỗng tên chương trình
        if (tenChuongTrinh.isEmpty()) {
            hienThiLoi("Vui lòng nhập tên chương trình khuyến mãi.");
            txtTenChuongTrinh.requestFocus();
            return;
        }

        // 2. Kiểm tra phần trăm giảm
        if (phanTramStr.isEmpty()) {
            hienThiLoi("Vui lòng nhập phần trăm giảm giá (từ 1 đến 100).");
            txtPhanTramGiam.requestFocus();
            return;
        }

        double phanTramGiam;
        try {
            phanTramGiam = Double.parseDouble(phanTramStr);
            if (phanTramGiam <= 0 || phanTramGiam > 100) {
                hienThiLoi("Phần trăm giảm phải là số từ 1 đến 100.");
                txtPhanTramGiam.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            hienThiLoi("Phần trăm giảm không đúng định dạng số hợp lệ.");
            txtPhanTramGiam.requestFocus();
            return;
        }

        // 3. Kiểm tra ngày bắt đầu và kết thúc
        if (ngayBatDau == null) {
            hienThiLoi("Vui lòng chọn ngày bắt đầu.");
            dtpNgayBatDau.requestFocus();
            return;
        }

        if (ngayKetThuc == null) {
            hienThiLoi("Vui lòng chọn ngày kết thúc.");
            dtpNgayKetThuc.requestFocus();
            return;
        }

        if (ngayKetThuc.isBefore(ngayBatDau)) {
            hienThiLoi("Ngày kết thúc không được trước ngày bắt đầu.");
            dtpNgayKetThuc.requestFocus();
            return;
        }

        // 4. Xác định cờ boolean trangThai
        boolean trangThai = !"Ngừng áp dụng".equals(trangThaiStr);

        // Đối tượng dữ liệu mới đã được người dùng nhập
        Promotion doiTuongLuu = new Promotion(
                maKhuyenMai,
                tenChuongTrinh,
                phanTramGiam,
                ngayBatDau,
                ngayKetThuc,
                trangThai,
                LocalDate.now()
        );

        if (cheDoHienTai == CheDo.THEM_MOI) {
            // Chế độ thêm mới: lưu ngay không cần xác nhận
            thucHienLuuVaoCSDL(doiTuongLuu);
        } else {
            // Chế độ cập nhật: hiển thị hộp thoại Xác nhận chỉnh sửa trước
            hienThiXacNhanChinhSua(doiTuongLuu);
        }
    }

    /**
     * Hiển thị hộp thoại xác nhận chỉnh sửa (PromotionConfirmEditDialog)
     * so sánh dữ liệu cũ và mới trước khi lưu vào CSDL.
     *
     * @param doiTuongMoi Dữ liệu mới người dùng vừa nhập
     */
    private void hienThiXacNhanChinhSua(Promotion doiTuongMoi) {
        PromotionConfirmEditDialog confirmDialog = new PromotionConfirmEditDialog(
                promotionHienTai,
                doiTuongMoi,
                // Callback "Xác nhận": thực hiện lưu
                () -> {
                    dongInnerOverlay();
                    thucHienLuuVaoCSDL(doiTuongMoi);
                },
                // Callback "Quay lại": đóng hộp thoại xác nhận, quay về form chỉnh sửa
                this::dongInnerOverlay
        );

        // Hiển thị hộp thoại xác nhận trên lớp phủ nội bộ
        pnlInnerOverlay.getChildren().setAll(confirmDialog);
        pnlInnerOverlay.setVisible(true);
        pnlInnerOverlay.setManaged(true);
    }

    /**
     * Đóng lớp phủ nội bộ và xóa hộp thoại xác nhận.
     */
    private void dongInnerOverlay() {
        pnlInnerOverlay.getChildren().clear();
        pnlInnerOverlay.setVisible(false);
        pnlInnerOverlay.setManaged(false);
    }

    /**
     * Thực hiện lưu dữ liệu vào CSDL qua Controller.
     *
     * @param doiTuongLuu Dữ liệu Promotion cần ghi xuống
     */
    private void thucHienLuuVaoCSDL(Promotion doiTuongLuu) {
        try {
            if (cheDoHienTai == CheDo.THEM_MOI) {
                promotionController.themKhuyenMai(doiTuongLuu);
            } else {
                promotionController.capNhatKhuyenMai(doiTuongLuu);
            }

            if (onThanhCong != null) {
                onThanhCong.accept(doiTuongLuu);
            }
            dongHopThoai();
        } catch (Exception ex) {
            hienThiLoi(ex.getMessage());
        }
    }

    /**
     * Hiển thị thông điệp lỗi cho người dùng.
     *
     * @param thongDiep Lời nhắn lỗi
     */
    private void hienThiLoi(String thongDiep) {
        lblBaoLoi.setText("⚠ " + thongDiep);
        lblBaoLoi.setVisible(true);
        lblBaoLoi.setManaged(true);
    }

    /**
     * Đóng hộp thoại modal.
     */
    private void dongHopThoai() {
        if (onDong != null) {
            onDong.run();
        }
    }

    /**
     * Tạo nhãn trường có dấu hoa thị màu đỏ bắt buộc nhập.
     *
     * @param tieuDe Tên trường
     * @return Label đã cấu hình
     */
    private Label taoNhanCoDauSao(String tieuDe) {
        Label lbl = new Label(tieuDe + " *");
        lbl.getStyleClass().add("modal-field-label");
        return lbl;
    }

    /**
     * Thiết lập định dạng chuẩn dd/MM/yyyy cho DatePicker.
     *
     * @param dtp DatePicker cần áp dụng bộ chuyển đổi
     */
    private void caiDatDinhDangDatePicker(DatePicker dtp) {
        dtp.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return (date != null) ? DINH_DANG_NGAY.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.trim().isEmpty()) {
                    try {
                        return LocalDate.parse(string.trim(), DINH_DANG_NGAY);
                    } catch (Exception e) {
                        return null;
                    }
                }
                return null;
            }
        });
    }
}

