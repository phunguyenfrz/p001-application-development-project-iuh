package com.petshop.view.shift;

import com.petshop.controller.ShiftController;
import com.petshop.model.Shift;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.function.Consumer;

/**
 * Hộp thoại modal ShiftEditorDialog dùng cho 2 nghiệp vụ:
 * 1. Thêm ca trực mới (Modal 1.1A)
 * 2. Cập nhật ca trực đã có (Modal 1.1A · Sửa ca trực)
 * Cung cấp chức năng kiểm tra định dạng giờ tự động và hiển thị cảnh báo lỗi (Modal 1.1B).
 *
 * Tuân thủ quy tắc lập trình (Review 3_OOP và Review 4_Coding Convention):
 * - Tên lớp là danh từ (ShiftEditorDialog), viết theo chuẩn PascalCase.
 * - Tên phương thức là động từ (khoiTao..., ganSuKien..., xuLy...), viết theo chuẩn camelCase.
 * - Quy ước tiền tố điều khiển: btn, txt, lbl, cbo.
 * - Đầy đủ chú thích Javadoc và chú thích một dòng.
 */
public class ShiftEditorDialog extends StackPane {

    public enum CheDo {
        THEM_MOI,
        CAP_NHAT
    }

    private final CheDo cheDoHienTai;
    private final ShiftController shiftController;
    private final Shift shiftHienTai;
    private final Consumer<Shift> onThanhCong;
    private final Runnable onDong;

    // Các thành phần điều khiển giao diện theo đúng tiền tố
    private Label lblTieuDe;
    private Label lblPhuDe;
    private Button btnDong;

    private Label lblMaCa;
    private TextField txtMaCa;

    private Label lblTenCa;
    private TextField txtTenCa;

    private Label lblGioBatDau;
    private TextField txtGioBatDau;
    private Label lblLoiGioBatDau;

    private Label lblGioKetThuc;
    private TextField txtGioKetThuc;
    private Label lblLoiGioKetThuc;

    private Label lblTrangThai;
    private ComboBox<String> cboTrangThai;

    private Label lblBaoLoi;
    private Button btnHuy;
    private Button btnLuuThongTin;

    /**
     * Khởi tạo hộp thoại thêm mới hoặc sửa ca trực.
     *
     * @param cheDo           Chế độ thêm mới hoặc cập nhật
     * @param shiftController Bộ điều khiển ca trực
     * @param shift           Ca trực cần sửa (null nếu thêm mới)
     * @param onThanhCong     Callback khi lưu thành công
     * @param onDong          Callback khi đóng hộp thoại
     */
    public ShiftEditorDialog(CheDo cheDo, ShiftController shiftController,
                             Shift shift, Consumer<Shift> onThanhCong, Runnable onDong) {
        this.cheDoHienTai = cheDo;
        this.shiftController = shiftController;
        this.shiftHienTai = shift;
        this.onThanhCong = onThanhCong;
        this.onDong = onDong;

        getStyleClass().add("modal-overlay");

        khoiTaoGiaoDien();
        ganSuKien();
        napDuLieu();
    }

    /**
     * Dựng toàn bộ cấu trúc giao diện hộp thoại theo đúng mẫu ảnh thiết kế 1.1A và 1.1B.
     */
    private void khoiTaoGiaoDien() {
        VBox pnlCard = new VBox(16);
        pnlCard.getStyleClass().add("modal-card");
        pnlCard.setPrefWidth(540);
        pnlCard.setMaxWidth(540);

        // 1. Header (Tiêu đề + nút đóng X)
        BorderPane pnlHeader = new BorderPane();

        VBox pnlTitleBox = new VBox(3);
        String tieuDeText = (cheDoHienTai == CheDo.THEM_MOI) ? "Thêm ca trực" : "Sửa ca trực";
        String phuDeText = (cheDoHienTai == CheDo.THEM_MOI)
                ? "Tạo ca trực mới và thiết lập thời gian làm việc."
                : "Cập nhật thông tin và thời gian làm việc của ca trực.";

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

        // --- Hàng 1: Mã ca (chỉ đọc có ổ khóa) & Tên ca (*) ---
        lblMaCa = new Label("Mã ca");
        lblMaCa.getStyleClass().add("modal-field-label");

        txtMaCa = new TextField();
        txtMaCa.getStyleClass().add("modal-text-input-readonly");
        txtMaCa.setEditable(false);

        SVGPath lockSvg = taoIconKhoa();
        StackPane pnlMaContainer = new StackPane(txtMaCa, lockSvg);
        StackPane.setAlignment(lockSvg, Pos.CENTER_RIGHT);
        StackPane.setMargin(lockSvg, new Insets(0, 10, 0, 0));

        VBox pnlMaBox = new VBox(6, lblMaCa, pnlMaContainer);

        lblTenCa = taoNhanCoDauSao("Tên ca");
        txtTenCa = new TextField();
        txtTenCa.setPromptText("Nhập tên ca trực");
        txtTenCa.getStyleClass().add("modal-text-input");

        VBox pnlTenBox = new VBox(6, lblTenCa, txtTenCa);

        pnlGrid.add(pnlMaBox, 0, 0);
        pnlGrid.add(pnlTenBox, 1, 0);

        // --- Hàng 2: Giờ bắt đầu (*) & Giờ kết thúc (*) ---
        lblGioBatDau = taoNhanCoDauSao("Giờ bắt đầu");

        txtGioBatDau = new TextField();
        txtGioBatDau.setPromptText("HH:mm");
        txtGioBatDau.getStyleClass().add("modal-text-input-with-icon");
        txtGioBatDau.setStyle("-fx-padding: 0 12px 0 38px;");

        SVGPath clockSvg1 = taoIconDongHo();
        clockSvg1.setMouseTransparent(true);
        StackPane pnlGioBatDauContainer = new StackPane(txtGioBatDau, clockSvg1);
        StackPane.setAlignment(clockSvg1, Pos.CENTER_LEFT);
        StackPane.setMargin(clockSvg1, new Insets(0, 0, 0, 12));

        lblLoiGioBatDau = new Label();
        lblLoiGioBatDau.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11.5px; -fx-font-weight: 500;");
        lblLoiGioBatDau.setVisible(false);
        lblLoiGioBatDau.setManaged(false);

        VBox pnlGioBatDauBox = new VBox(4, lblGioBatDau, pnlGioBatDauContainer, lblLoiGioBatDau);

        lblGioKetThuc = taoNhanCoDauSao("Giờ kết thúc");

        txtGioKetThuc = new TextField();
        txtGioKetThuc.setPromptText("HH:mm");
        txtGioKetThuc.getStyleClass().add("modal-text-input-with-icon");
        txtGioKetThuc.setStyle("-fx-padding: 0 12px 0 38px;");

        SVGPath clockSvg2 = taoIconDongHo();
        clockSvg2.setMouseTransparent(true);
        StackPane pnlGioKetThucContainer = new StackPane(txtGioKetThuc, clockSvg2);
        StackPane.setAlignment(clockSvg2, Pos.CENTER_LEFT);
        StackPane.setMargin(clockSvg2, new Insets(0, 0, 0, 12));

        lblLoiGioKetThuc = new Label();
        lblLoiGioKetThuc.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11.5px; -fx-font-weight: 500;");
        lblLoiGioKetThuc.setVisible(false);
        lblLoiGioKetThuc.setManaged(false);

        VBox pnlGioKetThucBox = new VBox(4, lblGioKetThuc, pnlGioKetThucContainer, lblLoiGioKetThuc);

        pnlGrid.add(pnlGioBatDauBox, 0, 1);
        pnlGrid.add(pnlGioKetThucBox, 1, 1);

        // --- Hàng 3 (Toàn chiều rộng): Trạng thái ---
        lblTrangThai = new Label("Trạng thái");
        lblTrangThai.getStyleClass().add("modal-field-label");

        cboTrangThai = new ComboBox<>();
        cboTrangThai.getItems().addAll("Đang hoạt động", "Sắp bắt đầu", "Đã kết thúc", "Ngừng áp dụng");
        cboTrangThai.setValue("Đang hoạt động");
        cboTrangThai.setMaxWidth(Double.MAX_VALUE);
        cboTrangThai.getStyleClass().add("modal-text-input");

        VBox pnlTrangThaiBox = new VBox(6, lblTrangThai, cboTrangThai);

        // 3. Khối nhắc nhở hướng dẫn (callout-info)
        HBox pnlInfoCallout = new HBox(8);
        pnlInfoCallout.setAlignment(Pos.CENTER_LEFT);
        pnlInfoCallout.getStyleClass().add("callout-info");

        Label lblInfoIcon = new Label("ⓘ");
        lblInfoIcon.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        Label lblInfoText = new Label("Nhập HH:mm (00:00–23:59). Tự thêm dấu \":\" sau 2 số đầu.\nGiờ kết thúc phải lớn hơn giờ bắt đầu. Các trường * là bắt buộc.");
        lblInfoText.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        lblInfoText.setWrapText(true);

        pnlInfoCallout.getChildren().addAll(lblInfoIcon, lblInfoText);

        // Thông báo lỗi tổng quát
        lblBaoLoi = new Label();
        lblBaoLoi.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px; -fx-font-weight: 600;");
        lblBaoLoi.setVisible(false);
        lblBaoLoi.setManaged(false);

        // 4. Thanh nút bấm: Hủy & Lưu thông tin
        HBox pnlActions = new HBox(12);
        pnlActions.setAlignment(Pos.CENTER_RIGHT);

        btnHuy = new Button("Hủy");
        btnHuy.getStyleClass().add("btn-action-secondary");
        btnHuy.setPrefWidth(90);

        btnLuuThongTin = new Button("Lưu thông tin");
        btnLuuThongTin.getStyleClass().add("btn-orange");

        pnlActions.getChildren().addAll(btnHuy, btnLuuThongTin);

        // Ráp toàn bộ vào Card
        pnlCard.getChildren().addAll(
                pnlHeader,
                pnlGrid,
                pnlTrangThaiBox,
                pnlInfoCallout,
                lblBaoLoi,
                pnlActions
        );

        getChildren().add(pnlCard);
    }

    /**
     * Gắn bộ lắng nghe sự kiện tương tác và định dạng giờ tự động.
     */
    private void ganSuKien() {
        btnDong.setOnAction(e -> dongHopThoai());
        btnHuy.setOnAction(e -> dongHopThoai());

        setOnMouseClicked(e -> {
            if (e.getTarget() == this) {
                dongHopThoai();
            }
        });

        // Tự động chèn dấu ":" sau 2 số đầu và kiểm tra lỗi thời gian thực
        thietLapTuDongDinhDangVaKiemTraGio(txtGioBatDau, lblLoiGioBatDau);
        thietLapTuDongDinhDangVaKiemTraGio(txtGioKetThuc, lblLoiGioKetThuc);

        // Nút lưu thông tin
        btnLuuThongTin.setOnAction(e -> xuLyLuuCaTruc());
    }

    /**
     * Nạp dữ liệu ban đầu lên form dựa theo chế độ Thêm mới hoặc Cập nhật.
     */
    private void napDuLieu() {
        if (cheDoHienTai == CheDo.THEM_MOI) {
            String maMoi = shiftController.sinhMaCaMoi();
            txtMaCa.setText(maMoi);
            cboTrangThai.setValue("Đang hoạt động");
        } else if (shiftHienTai != null) {
            txtMaCa.setText(shiftHienTai.getMaCa());
            txtTenCa.setText(shiftHienTai.getTenCa());
            txtGioBatDau.setText(shiftHienTai.layGioBatDauHienThi());
            txtGioKetThuc.setText(shiftHienTai.layGioKetThucHienThi());
            cboTrangThai.setValue(shiftHienTai.getTrangThai());
        }
    }

    /**
     * Thiết lập tự động thêm dấu ":" sau 2 chữ số đầu tiên khi người dùng gõ
     * và thẩm định định dạng HH:mm, đổi viền đỏ và hiển thị thông báo lỗi như mẫu Image 5.
     *
     * @param txtInput Ô nhập liệu giờ
     * @param lblLoi   Nhãn hiển thị dòng lỗi màu đỏ bên dưới
     */
    private void thietLapTuDongDinhDangVaKiemTraGio(TextField txtInput, Label lblLoi) {
        txtInput.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;

            // Xóa chữ cái hoặc ký tự không hợp lệ ngoài số và dấu :
            String cleaned = newVal.replaceAll("[^0-9:]", "");

            // Tự động chèn dấu : sau 2 chữ số đầu nếu người dùng chưa gõ :
            if (cleaned.length() == 2 && !cleaned.contains(":") && oldVal != null && oldVal.length() < 2) {
                cleaned = cleaned + ":";
                txtInput.setText(cleaned);
                txtInput.positionCaret(cleaned.length());
                return;
            }

            // Giới hạn độ dài tối đa 5 ký tự (HH:mm)
            if (cleaned.length() > 5) {
                cleaned = cleaned.substring(0, 5);
                txtInput.setText(cleaned);
                txtInput.positionCaret(cleaned.length());
                return;
            }

            // Kiểm tra tính hợp lệ khi người dùng đã nhập đủ 5 ký tự
            if (cleaned.length() == 5) {
                try {
                    shiftController.kiemTraDinhDangGio(cleaned);
                    // Hợp lệ: xóa cảnh báo đỏ, giữ nguyên lề thụt 38px tránh đè icon đồng hồ
                    txtInput.setStyle("-fx-padding: 0 12px 0 38px; -fx-border-color: #d1d5db; -fx-border-width: 1px;");
                    lblLoi.setText("");
                    lblLoi.setVisible(false);
                    lblLoi.setManaged(false);
                } catch (IllegalArgumentException ex) {
                    // Không hợp lệ: viền đỏ + hiển thị dòng lỗi (Khớp mẫu Image 5), giữ nguyên lề thụt 38px
                    txtInput.setStyle("-fx-padding: 0 12px 0 38px; -fx-border-color: #dc2626; -fx-border-width: 1.5px;");
                    lblLoi.setText(ex.getMessage());
                    lblLoi.setVisible(true);
                    lblLoi.setManaged(true);
                }
            } else {
                txtInput.setStyle("-fx-padding: 0 12px 0 38px; -fx-border-color: #d1d5db; -fx-border-width: 1px;");
                lblLoi.setText("");
                lblLoi.setVisible(false);
                lblLoi.setManaged(false);
            }
        });
    }

    /**
     * Thực hiện kiểm tra tính hợp lệ và ghi nhận lưu ca trực vào CSDL.
     */
    private void xuLyLuuCaTruc() {
        lblBaoLoi.setVisible(false);
        lblBaoLoi.setManaged(false);

        String maCa = txtMaCa.getText().trim();
        String tenCa = txtTenCa.getText().trim();
        String gioBatDauStr = txtGioBatDau.getText().trim();
        String gioKetThucStr = txtGioKetThuc.getText().trim();
        String trangThai = cboTrangThai.getValue();

        // 1. Kiểm tra rỗng tên ca
        if (tenCa.isEmpty()) {
            hienThiLoi("Vui lòng nhập tên ca trực.");
            txtTenCa.requestFocus();
            return;
        }

        // 2. Kiểm tra giờ bắt đầu
        if (gioBatDauStr.isEmpty()) {
            hienThiLoi("Vui lòng nhập giờ bắt đầu.");
            txtGioBatDau.requestFocus();
            return;
        }

        LocalTime gioBatDau;
        try {
            gioBatDau = shiftController.chuyenDoiSangLocalTime(gioBatDauStr);
        } catch (IllegalArgumentException ex) {
            txtGioBatDau.setStyle("-fx-padding: 0 12px 0 38px; -fx-border-color: #dc2626; -fx-border-width: 1.5px;");
            lblLoiGioBatDau.setText(ex.getMessage());
            lblLoiGioBatDau.setVisible(true);
            lblLoiGioBatDau.setManaged(true);
            hienThiLoi(ex.getMessage());
            txtGioBatDau.requestFocus();
            return;
        }

        // 3. Kiểm tra giờ kết thúc
        if (gioKetThucStr.isEmpty()) {
            hienThiLoi("Vui lòng nhập giờ kết thúc.");
            txtGioKetThuc.requestFocus();
            return;
        }

        LocalTime gioKetThuc;
        try {
            gioKetThuc = shiftController.chuyenDoiSangLocalTime(gioKetThucStr);
        } catch (IllegalArgumentException ex) {
            txtGioKetThuc.setStyle("-fx-padding: 0 12px 0 38px; -fx-border-color: #dc2626; -fx-border-width: 1.5px;");
            lblLoiGioKetThuc.setText(ex.getMessage());
            lblLoiGioKetThuc.setVisible(true);
            lblLoiGioKetThuc.setManaged(true);
            hienThiLoi(ex.getMessage());
            txtGioKetThuc.requestFocus();
            return;
        }

        // 4. Ràng buộc giờ kết thúc phải sau giờ bắt đầu
        if (!gioKetThuc.isAfter(gioBatDau)) {
            hienThiLoi("Giờ kết thúc phải lớn hơn giờ bắt đầu.");
            txtGioKetThuc.requestFocus();
            return;
        }

        // Tạo đối tượng ca trực
        Shift doiTuongLuu = new Shift(
                maCa,
                tenCa,
                gioBatDau,
                gioKetThuc,
                trangThai,
                LocalDate.now()
        );

        try {
            if (cheDoHienTai == CheDo.THEM_MOI) {
                shiftController.themCaTruc(doiTuongLuu);
            } else {
                shiftController.capNhatCaTruc(doiTuongLuu);
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
     * Hiển thị thông báo lỗi lên giao diện.
     *
     * @param thongDiep Nội dung lỗi
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
     * @param tieuDe Tên nhãn
     * @return Đối tượng Label
     */
    private Label taoNhanCoDauSao(String tieuDe) {
        Label lbl = new Label(tieuDe + " *");
        lbl.getStyleClass().add("modal-field-label");
        return lbl;
    }

    /**
     * Tạo icon ổ khóa cho trường chỉ đọc.
     */
    private SVGPath taoIconKhoa() {
        SVGPath lockSvg = new SVGPath();
        lockSvg.setContent("M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z");
        lockSvg.setFill(Color.web("#9ca3af"));
        lockSvg.setScaleX(0.55);
        lockSvg.setScaleY(0.55);
        return lockSvg;
    }

    /**
     * Tạo icon đồng hồ thời gian.
     */
    private SVGPath taoIconDongHo() {
        SVGPath clockSvg = new SVGPath();
        clockSvg.setContent("M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z");
        clockSvg.setFill(Color.web("#9ca3af"));
        clockSvg.setScaleX(0.55);
        clockSvg.setScaleY(0.55);
        return clockSvg;
    }
}


