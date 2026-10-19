package com.petshop.view;

import com.petshop.controller.ShiftAssignmentController;
import com.petshop.model.Shift;
import com.petshop.model.ShiftAssignment;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Hộp thoại modal ShiftAssignmentEditorDialog:
 * - 4.2A · Thêm phân công ca trực (Image 2)
 * - 4.2A · Cập nhật phân công (Image 3)
 * - Hỗ trợ chọn ngày làm việc bằng DatePicker (Image 4)
 *
 * Tiêu chuẩn tuân thủ (Review 3_OOP và Review 4_Coding Convention):
 * - Tên lớp là danh từ (ShiftAssignmentEditorDialog), viết theo chuẩn PascalCase.
 * - Tên phương thức là động từ (khoiTao..., ganSuKien..., xuLy...), viết theo chuẩn camelCase.
 * - Tiền tố điều khiển: btn, txt, lbl, cbo, dtp.
 * - Đầy đủ chú thích Javadoc và chú thích từng khối xử lý.
 */
public class ShiftAssignmentEditorDialog extends StackPane {

    public enum CheDo {
        THEM_MOI,
        CAP_NHAT
    }

    private final CheDo cheDoHienTai;
    private final ShiftAssignmentController controller;
    private final ShiftAssignment assignmentHienTai;
    private final Consumer<ShiftAssignment> onThanhCong;
    private final Runnable onDong;

    // Các thành phần điều khiển giao diện theo đúng tiền tố chuẩn
    private Label lblTieuDe;
    private Label lblPhuDe;
    private Button btnDong;

    private Label lblMaPhanCa;
    private TextField txtMaPhanCa;

    private Label lblNhanVien;
    private ComboBox<Map<String, String>> cboNhanVien;

    private Label lblCaTruc;
    private ComboBox<Shift> cboCaTruc;

    private Label lblNgayLam;
    private DatePicker dtpNgayLam;

    private Label lblTrangThai;
    private ComboBox<String> cboTrangThai;

    private Label lblBaoLoi;
    private Button btnHuy;
    private Button btnLuuPhanCong;

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Khởi tạo hộp thoại thêm hoặc sửa phân công ca trực.
     *
     * @param cheDo       Chế độ thêm mới hoặc cập nhật
     * @param controller  Bộ điều khiển phân ca
     * @param assignment  Đối tượng phân ca (null nếu thêm mới)
     * @param onThanhCong Callback thực thi khi lưu thành công
     * @param onDong      Callback thực thi khi đóng modal
     */
    public ShiftAssignmentEditorDialog(CheDo cheDo, ShiftAssignmentController controller,
                                       ShiftAssignment assignment,
                                       Consumer<ShiftAssignment> onThanhCong, Runnable onDong) {
        this.cheDoHienTai = cheDo;
        this.controller = controller;
        this.assignmentHienTai = assignment;
        this.onThanhCong = onThanhCong;
        this.onDong = onDong;

        getStyleClass().add("modal-overlay");

        khoiTaoGiaoDien();
        ganSuKien();
        napDuLieu();
    }

    /**
     * Dựng toàn bộ cấu trúc giao diện hộp thoại theo thiết kế Modal 4.2A.
     */
    private void khoiTaoGiaoDien() {
        VBox pnlCard = new VBox(16);
        pnlCard.getStyleClass().add("modal-card");
        pnlCard.setPrefWidth(540);
        pnlCard.setMaxWidth(540);

        // 1. Tiêu đề và nút đóng (X)
        HBox pnlHeader = new HBox();
        pnlHeader.setAlignment(Pos.CENTER_LEFT);

        VBox pnlTitleBox = new VBox(3);
        lblTieuDe = new Label(cheDoHienTai == CheDo.THEM_MOI ? "Thêm phân công ca trực" : "Cập nhật phân công");
        lblTieuDe.getStyleClass().add("modal-header-title");

        lblPhuDe = new Label(cheDoHienTai == CheDo.THEM_MOI
                ? "Chọn nhân viên, ca trực và ngày làm việc để tạo phân công mới."
                : "Điều chỉnh thông tin phân công cho ca làm việc chưa diễn ra.");
        lblPhuDe.getStyleClass().add("modal-header-sub");
        pnlTitleBox.getChildren().addAll(lblTieuDe, lblPhuDe);

        Region pnlHeaderSpacer = new Region();
        HBox.setHgrow(pnlHeaderSpacer, Priority.ALWAYS);

        btnDong = new Button("✕");
        btnDong.getStyleClass().add("modal-close-button");

        pnlHeader.getChildren().addAll(pnlTitleBox, pnlHeaderSpacer, btnDong);

        // 2. Lưới trường nhập liệu (GridPane 2 cột)
        GridPane pnlGrid = new GridPane();
        pnlGrid.setHgap(16);
        pnlGrid.setVgap(14);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        pnlGrid.getColumnConstraints().addAll(col1, col2);

        // --- Hàng 0: Mã phân ca & Nhân viên (*) ---
        lblMaPhanCa = new Label("Mã phân ca");
        lblMaPhanCa.getStyleClass().add("modal-field-label");

        txtMaPhanCa = new TextField();
        txtMaPhanCa.setEditable(false);
        txtMaPhanCa.getStyleClass().add("modal-text-input-readonly");

        // Biểu tượng ổ khóa cho ô chỉ đọc
        SVGPath lockSvg = new SVGPath();
        lockSvg.setContent("M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z");
        lockSvg.setFill(Color.web("#9ca3af"));
        lockSvg.setScaleX(0.65);
        lockSvg.setScaleY(0.65);

        StackPane pnlMaContainer = new StackPane(txtMaPhanCa, lockSvg);
        StackPane.setAlignment(lockSvg, Pos.CENTER_RIGHT);
        StackPane.setMargin(lockSvg, new Insets(0, 10, 0, 0));

        VBox pnlMaBox = new VBox(6, lblMaPhanCa, pnlMaContainer);

        lblNhanVien = taoNhanCoDauSao("Nhân viên");
        cboNhanVien = new ComboBox<>();
        cboNhanVien.setPromptText("Chọn nhân viên");
        cboNhanVien.setMaxWidth(Double.MAX_VALUE);
        cboNhanVien.getStyleClass().add("modal-text-input");

        // Bộ chuyển đổi hiển thị tên nhân viên trong ComboBox
        cboNhanVien.setConverter(new StringConverter<>() {
            @Override
            public String toString(Map<String, String> item) {
                return (item != null) ? item.get("hoTen") : "";
            }

            @Override
            public Map<String, String> fromString(String string) {
                return null;
            }
        });

        VBox pnlNhanVienBox = new VBox(6, lblNhanVien, cboNhanVien);

        pnlGrid.add(pnlMaBox, 0, 0);
        pnlGrid.add(pnlNhanVienBox, 1, 0);

        // --- Hàng 1: Ca trực (*) & Ngày làm (*) ---
        lblCaTruc = taoNhanCoDauSao("Ca trực");
        cboCaTruc = new ComboBox<>();
        cboCaTruc.setPromptText("Chọn ca trực");
        cboCaTruc.setMaxWidth(Double.MAX_VALUE);
        cboCaTruc.getStyleClass().add("modal-text-input");

        // Bộ chuyển đổi hiển thị tên ca và khung giờ trong ComboBox (VD: Ca sáng · 08:00–16:00)
        cboCaTruc.setConverter(new StringConverter<>() {
            @Override
            public String toString(Shift item) {
                if (item == null) return "";
                return item.getTenCa() + " · " + item.layGioBatDauHienThi() + "–" + item.layGioKetThucHienThi();
            }

            @Override
            public Shift fromString(String string) {
                return null;
            }
        });

        VBox pnlCaTrucBox = new VBox(6, lblCaTruc, cboCaTruc);

        lblNgayLam = taoNhanCoDauSao("Ngày làm");
        dtpNgayLam = new DatePicker();
        dtpNgayLam.setPromptText("dd/mm/yyyy");
        dtpNgayLam.setMaxWidth(Double.MAX_VALUE);
        dtpNgayLam.getStyleClass().add("modal-text-input");

        // Định dạng hiển thị dd/MM/yyyy cho DatePicker
        dtpNgayLam.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return (date != null) ? date.format(DINH_DANG_NGAY) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.trim().isEmpty()) {
                    try {
                        return LocalDate.parse(string.trim(), DINH_DANG_NGAY);
                    } catch (Exception ignored) {}
                }
                return null;
            }
        });

        // Khóa các ngày trong quá khứ theo quy tắc "Chỉ phân công từ hôm nay" (Image 4)
        dtpNgayLam.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && item.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #9ca3af;");
                }
            }
        });

        VBox pnlNgayLamBox = new VBox(6, lblNgayLam, dtpNgayLam);

        pnlGrid.add(pnlCaTrucBox, 0, 1);
        pnlGrid.add(pnlNgayLamBox, 1, 1);

        // --- Hàng 2 (Cột 0): Trạng thái phân công ---
        lblTrangThai = new Label("Trạng thái phân công");
        lblTrangThai.getStyleClass().add("modal-field-label");

        cboTrangThai = new ComboBox<>();
        cboTrangThai.getItems().addAll("Đã phân công", "Đã hủy");
        cboTrangThai.setValue("Đã phân công");
        cboTrangThai.setMaxWidth(Double.MAX_VALUE);
        cboTrangThai.getStyleClass().add("modal-text-input");

        VBox pnlTrangThaiBox = new VBox(6, lblTrangThai, cboTrangThai);
        pnlGrid.add(pnlTrangThaiBox, 0, 2);

        // 3. Khối thông báo hướng dẫn (callout-info)
        HBox pnlInfoCallout = new HBox(8);
        pnlInfoCallout.setAlignment(Pos.CENTER_LEFT);
        pnlInfoCallout.getStyleClass().add("callout-info");

        Label lblInfoIcon = new Label("ⓘ");
        lblInfoIcon.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        Label lblInfoText = new Label("Chỉ phân công từ hôm nay. Các trường có dấu * là bắt buộc.");
        lblInfoText.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        pnlInfoCallout.getChildren().addAll(lblInfoIcon, lblInfoText);

        // Thông báo lỗi
        lblBaoLoi = new Label();
        lblBaoLoi.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px; -fx-font-weight: 600;");
        lblBaoLoi.setVisible(false);
        lblBaoLoi.setManaged(false);

        // 4. Thanh nút bấm chân trang: Hủy & Lưu phân công (Cam)
        HBox pnlActions = new HBox(12);
        pnlActions.setAlignment(Pos.CENTER_RIGHT);

        btnHuy = new Button("Hủy");
        btnHuy.getStyleClass().add("btn-action-secondary");
        btnHuy.setPrefWidth(90);

        btnLuuPhanCong = new Button("Lưu phân công");
        btnLuuPhanCong.getStyleClass().add("btn-orange");

        pnlActions.getChildren().addAll(btnHuy, btnLuuPhanCong);

        pnlCard.getChildren().addAll(
                pnlHeader,
                pnlGrid,
                pnlInfoCallout,
                lblBaoLoi,
                pnlActions
        );

        getChildren().add(pnlCard);
    }

    /**
     * Tạo nhãn với dấu sao đỏ (*) đánh dấu trường bắt buộc.
     */
    private Label taoNhanCoDauSao(String tenNhan) {
        Label lbl = new Label(tenNhan + " *");
        lbl.getStyleClass().add("modal-field-label");
        return lbl;
    }

    /**
     * Gắn bộ lắng nghe sự kiện tương tác.
     */
    private void ganSuKien() {
        btnDong.setOnAction(e -> dongHopThoai());
        btnHuy.setOnAction(e -> dongHopThoai());

        setOnMouseClicked(e -> {
            if (e.getTarget() == this) {
                dongHopThoai();
            }
        });

        btnLuuPhanCong.setOnAction(e -> xuLyLuuPhanCong());
    }

    /**
     * Nạp dữ liệu danh sách nhân viên, ca trực và điền dữ liệu ban đầu.
     */
    private void napDuLieu() {
        // Nạp danh sách nhân viên
        List<Map<String, String>> dsNhanVien = controller.layDanhSachNhanVien();
        cboNhanVien.getItems().setAll(dsNhanVien);

        // Nạp danh sách ca trực
        List<Shift> dsCa = controller.layDanhSachCaTruc();
        cboCaTruc.getItems().setAll(dsCa);

        if (cheDoHienTai == CheDo.THEM_MOI) {
            String maMoi = controller.sinhMaPhanCaTuDong();
            txtMaPhanCa.setText(maMoi);
            dtpNgayLam.setValue(LocalDate.now());
            cboTrangThai.setValue("Đã phân công");
        } else if (assignmentHienTai != null) {
            txtMaPhanCa.setText(assignmentHienTai.getMaPhanCa());
            dtpNgayLam.setValue(assignmentHienTai.getNgayLam());
            cboTrangThai.setValue(assignmentHienTai.getTrangThai());

            // Chọn nhân viên tương ứng
            for (Map<String, String> emp : dsNhanVien) {
                if (emp.get("maNhanVien").equals(assignmentHienTai.getMaNhanVien())) {
                    cboNhanVien.setValue(emp);
                    break;
                }
            }

            // Chọn ca trực tương ứng
            for (Shift s : dsCa) {
                if (s.getMaCa().equals(assignmentHienTai.getMaCa())) {
                    cboCaTruc.setValue(s);
                    break;
                }
            }
        }
    }

    /**
     * Thực hiện kiểm tra tính hợp lệ và ghi nhận lưu phân ca vào CSDL.
     */
    private void xuLyLuuPhanCong() {
        lblBaoLoi.setVisible(false);
        lblBaoLoi.setManaged(false);

        String maPhanCa = txtMaPhanCa.getText().trim();
        Map<String, String> nhanVienDaChon = cboNhanVien.getValue();
        Shift caDaChon = cboCaTruc.getValue();
        LocalDate ngayLam = dtpNgayLam.getValue();
        String trangThai = cboTrangThai.getValue();

        // 1. Kiểm tra nhân viên
        if (nhanVienDaChon == null) {
            hienThiLoi("Vui lòng chọn nhân viên.");
            cboNhanVien.requestFocus();
            return;
        }

        // 2. Kiểm tra ca trực
        if (caDaChon == null) {
            hienThiLoi("Vui lòng chọn ca trực.");
            cboCaTruc.requestFocus();
            return;
        }

        // 3. Kiểm tra ngày làm
        if (ngayLam == null) {
            hienThiLoi("Vui lòng chọn ngày làm việc.");
            dtpNgayLam.requestFocus();
            return;
        }

        // 4. Ràng buộc "Chỉ phân công từ hôm nay"
        if (ngayLam.isBefore(LocalDate.now())) {
            hienThiLoi("Chỉ phân công từ hôm nay trở đi (" + LocalDate.now().format(DINH_DANG_NGAY) + ").");
            dtpNgayLam.requestFocus();
            return;
        }

        ShiftAssignment doiTuongLuu = new ShiftAssignment(
                maPhanCa,
                nhanVienDaChon.get("maNhanVien"),
                nhanVienDaChon.get("hoTen"),
                caDaChon.getMaCa(),
                caDaChon.getTenCa(),
                caDaChon.getGioBatDau(),
                caDaChon.getGioKetThuc(),
                ngayLam,
                trangThai
        );

        try {
            if (cheDoHienTai == CheDo.THEM_MOI) {
                controller.themPhanCa(doiTuongLuu);
            } else {
                controller.capNhatPhanCa(doiTuongLuu);
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
     * Hiển thị cảnh báo lỗi lên modal.
     *
     * @param thongDiep Nội dung lỗi
     */
    private void hienThiLoi(String thongDiep) {
        lblBaoLoi.setText("⚠ " + thongDiep);
        lblBaoLoi.setVisible(true);
        lblBaoLoi.setManaged(true);
    }

    /**
     * Đóng modal dialog.
     */
    private void dongHopThoai() {
        if (onDong != null) {
            onDong.run();
        }
    }
}

