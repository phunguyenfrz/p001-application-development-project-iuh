package com.petshop.view;

import com.petshop.model.ReturnNoteItem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.text.DecimalFormat;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Hộp thoại modal ReturnItemDialog: Thêm chi tiết đổi trả (Mẫu 3.1A).
 * Cho phép người dùng chọn sản phẩm từ hóa đơn đã mua, nhập số lượng cần hoàn trả,
 * xem trước đơn giá và tổng số tiền hoàn.
 *
 * Tiêu chuẩn tuân thủ:
 * - Đặt tên điều khiển đúng tiền tố: btn, txt, lbl, cbo.
 * - Tên lớp là danh từ (ReturnItemDialog), tên phương thức là động từ.
 * - Ô số chỉ cho phép nhập số (TextFormatter).
 * - Có bình luận 1 dòng và nhiều dòng chuẩn Javadoc.
 */
public class ReturnItemDialog extends StackPane {

    private final String maHoaDon;
    private final List<ReturnNoteItem> danhSachSanPhamHoaDon;
    private final Consumer<ReturnNoteItem> onThemChiTiet;
    private final Runnable onDong;

    // Các thành phần giao diện theo đúng tiền tố
    private Label lblTieuDe;
    private Label lblPhuDe;
    private Button btnDong;

    private Label lblSanPham;
    private ComboBox<ReturnNoteItem> cboSanPham;

    private Label lblSoLuong;
    private TextField txtSoLuong;
    private Label lblSoLuongGhiChu;

    private Label lblDonGia;
    private TextField txtDonGia;

    private Label lblThanhTienTieuDe;
    private Label lblThanhTienGiaTri;

    private Label lblBaoLoi;
    private Button btnHuy;
    private Button btnThemVaoPhieu;

    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 'đ'");

    /**
     * Khởi tạo hộp thoại thêm chi tiết đổi trả.
     *
     * @param maHoaDon                Mã hóa đơn gốc
     * @param danhSachSanPhamHoaDon   Danh sách các sản phẩm có trong hóa đơn
     * @param onThemChiTiet           Callback khi người dùng nhấn "Thêm vào phiếu"
     * @param onDong                  Callback khi đóng hộp thoại
     */
    public ReturnItemDialog(String maHoaDon, List<ReturnNoteItem> danhSachSanPhamHoaDon,
                            Consumer<ReturnNoteItem> onThemChiTiet, Runnable onDong) {
        this.maHoaDon = (maHoaDon != null) ? maHoaDon : "";
        this.danhSachSanPhamHoaDon = (danhSachSanPhamHoaDon != null) ? danhSachSanPhamHoaDon : List.of();
        this.onThemChiTiet = onThemChiTiet;
        this.onDong = onDong;

        getStyleClass().add("modal-overlay");

        khoiTaoGiaoDien();
        ganSuKien();
        napDuLieuBanDau();
    }

    /**
     * Xây dựng cấu trúc giao diện theo đúng mẫu 3.1A.
     */
    private void khoiTaoGiaoDien() {
        VBox pnlCard = new VBox(16);
        pnlCard.getStyleClass().add("modal-card");
        pnlCard.setPrefWidth(480);
        pnlCard.setMaxWidth(480);

        // 1. Header (Tiêu đề + nút đóng X)
        BorderPane pnlHeader = new BorderPane();

        VBox pnlTitleBox = new VBox(3);
        lblTieuDe = new Label("Thêm chi tiết đổi trả");
        lblTieuDe.getStyleClass().add("modal-header-title");

        lblPhuDe = new Label("Chọn sản phẩm từ hóa đơn " + maHoaDon + ".");
        lblPhuDe.getStyleClass().add("modal-header-sub");
        pnlTitleBox.getChildren().addAll(lblTieuDe, lblPhuDe);

        btnDong = new Button("✕");
        btnDong.getStyleClass().add("modal-close-button");

        pnlHeader.setLeft(pnlTitleBox);
        pnlHeader.setRight(btnDong);

        // 2. Chọn sản phẩm
        lblSanPham = new Label("Sản phẩm *");
        lblSanPham.getStyleClass().add("modal-field-label");

        cboSanPham = new ComboBox<>();
        cboSanPham.setMaxWidth(Double.MAX_VALUE);
        cboSanPham.getStyleClass().add("modal-text-input");
        cboSanPham.setPromptText("Chọn sản phẩm cần đổi trả");

        // Tùy biến hiển thị tên sản phẩm trong ComboBox
        cboSanPham.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ReturnNoteItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTenSanPham() + " (Đã mua: " + item.getSoLuong() + ")");
            }
        });
        cboSanPham.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ReturnNoteItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTenSanPham());
            }
        });

        VBox pnlSanPhamBox = new VBox(6, lblSanPham, cboSanPham);

        // 3. Hàng 2 cột: Số lượng đổi trả & Đơn giá
        GridPane pnlGrid = new GridPane();
        pnlGrid.setHgap(14);
        pnlGrid.setVgap(10);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        pnlGrid.getColumnConstraints().addAll(col1, col2);

        // Cột 1: Số lượng đổi trả (*)
        lblSoLuong = new Label("Số lượng đổi trả *");
        lblSoLuong.getStyleClass().add("modal-field-label");

        txtSoLuong = new TextField("1");
        txtSoLuong.getStyleClass().add("modal-text-input");

        // RÀNG BUỘC: Ô số chỉ cho phép nhập số
        UnaryOperator<TextFormatter.Change> boLocChiNhapSo = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("[0-9]*")) {
                return change;
            }
            return null;
        };
        txtSoLuong.setTextFormatter(new TextFormatter<>(boLocChiNhapSo));

        lblSoLuongGhiChu = new Label("Chỉ nhập số. Tối đa 1 sản phẩm theo hóa đơn.");
        lblSoLuongGhiChu.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #9ca3af;");

        VBox pnlSoLuongBox = new VBox(4, lblSoLuong, txtSoLuong, lblSoLuongGhiChu);

        // Cột 2: Đơn giá (readonly + icon ổ khóa)
        lblDonGia = new Label("Đơn giá");
        lblDonGia.getStyleClass().add("modal-field-label");

        txtDonGia = new TextField("0 đ");
        txtDonGia.getStyleClass().add("modal-text-input-readonly");
        txtDonGia.setEditable(false);

        SVGPath lockSvg = new SVGPath();
        lockSvg.setContent("M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z");
        lockSvg.setFill(Color.web("#9ca3af"));
        lockSvg.setScaleX(0.55);
        lockSvg.setScaleY(0.55);

        StackPane pnlDonGiaContainer = new StackPane(txtDonGia, lockSvg);
        StackPane.setAlignment(lockSvg, Pos.CENTER_RIGHT);
        StackPane.setMargin(lockSvg, new Insets(0, 10, 0, 0));

        VBox pnlDonGiaBox = new VBox(4, lblDonGia, pnlDonGiaContainer);

        pnlGrid.add(pnlSoLuongBox, 0, 0);
        pnlGrid.add(pnlDonGiaBox, 1, 0);

        // 4. Khối thẻ xanh làm nổi bật "Thành tiền hoàn"
        HBox pnlHighlightThanhTien = new HBox();
        pnlHighlightThanhTien.setAlignment(Pos.CENTER_LEFT);
        pnlHighlightThanhTien.getStyleClass().add("highlight-card-green");

        lblThanhTienTieuDe = new Label("Thành tiền hoàn");
        lblThanhTienTieuDe.setStyle("-fx-font-size: 13.5px; -fx-font-weight: 600; -fx-text-fill: #2f4f38;");

        Region pnlThanhTienSpacer = new Region();
        HBox.setHgrow(pnlThanhTienSpacer, Priority.ALWAYS);

        lblThanhTienGiaTri = new Label("0 đ");
        lblThanhTienGiaTri.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2f4f38;");

        pnlHighlightThanhTien.getChildren().addAll(lblThanhTienTieuDe, pnlThanhTienSpacer, lblThanhTienGiaTri);

        // 5. Khối thông tin ghi chú
        HBox pnlCalloutInfo = new HBox(8);
        pnlCalloutInfo.setAlignment(Pos.CENTER_LEFT);
        pnlCalloutInfo.getStyleClass().add("callout-info");

        Label lblInfoIcon = new Label("ⓘ");
        lblInfoIcon.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        Label lblInfoText = new Label("Đơn giá được lấy từ hóa đơn gốc và không thể chỉnh sửa.");
        lblInfoText.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        pnlCalloutInfo.getChildren().addAll(lblInfoIcon, lblInfoText);

        // Thông báo lỗi
        lblBaoLoi = new Label();
        lblBaoLoi.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px; -fx-font-weight: 600;");
        lblBaoLoi.setVisible(false);
        lblBaoLoi.setManaged(false);

        // 6. Thanh nút bấm
        HBox pnlActions = new HBox(12);
        pnlActions.setAlignment(Pos.CENTER_RIGHT);

        btnHuy = new Button("Hủy");
        btnHuy.getStyleClass().add("btn-action-secondary");
        btnHuy.setPrefWidth(90);

        btnThemVaoPhieu = new Button("+ Thêm vào phiếu");
        btnThemVaoPhieu.getStyleClass().add("btn-orange");

        pnlActions.getChildren().addAll(btnHuy, btnThemVaoPhieu);

        // Ráp vào Card
        pnlCard.getChildren().addAll(
                pnlHeader,
                pnlSanPhamBox,
                pnlGrid,
                pnlHighlightThanhTien,
                pnlCalloutInfo,
                lblBaoLoi,
                pnlActions
        );

        getChildren().add(pnlCard);
    }

    /**
     * Gắn bộ lắng nghe sự kiện.
     */
    private void ganSuKien() {
        btnDong.setOnAction(e -> dongHopThoai());
        btnHuy.setOnAction(e -> dongHopThoai());

        setOnMouseClicked(e -> {
            if (e.getTarget() == this) {
                dongHopThoai();
            }
        });

        // Khi đổi sản phẩm chọn
        cboSanPham.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtDonGia.setText(DINH_DANG_TIEN.format(newVal.getDonGia()));
                lblSoLuongGhiChu.setText("Chỉ nhập số. Tối đa " + newVal.getSoLuong() + " sản phẩm theo hóa đơn.");
                capNhatThanhTien();
            }
        });

        // Khi thay đổi số lượng nhập
        txtSoLuong.textProperty().addListener((obs, oldVal, newVal) -> capNhatThanhTien());

        // Bấm thêm vào phiếu
        btnThemVaoPhieu.setOnAction(e -> xuLyThemVaoPhieu());
    }

    /**
     * Nạp danh sách sản phẩm ban đầu vào ComboBox.
     */
    private void napDuLieuBanDau() {
        cboSanPham.getItems().setAll(danhSachSanPhamHoaDon);
        if (!danhSachSanPhamHoaDon.isEmpty()) {
            cboSanPham.setValue(danhSachSanPhamHoaDon.get(0));
        }
    }

    /**
     * Tính toán và hiển thị thành tiền hoàn dự kiến.
     */
    private void capNhatThanhTien() {
        ReturnNoteItem selected = cboSanPham.getValue();
        if (selected == null) {
            lblThanhTienGiaTri.setText("0 đ");
            return;
        }

        int sl = 0;
        try {
            sl = Integer.parseInt(txtSoLuong.getText().trim());
        } catch (Exception ignored) {}

        double thanhTien = selected.getDonGia() * sl;
        lblThanhTienGiaTri.setText(DINH_DANG_TIEN.format(thanhTien));
    }

    /**
     * Xử lý xác thực và thêm sản phẩm vào phiếu đổi trả.
     */
    private void xuLyThemVaoPhieu() {
        lblBaoLoi.setVisible(false);
        lblBaoLoi.setManaged(false);

        ReturnNoteItem selected = cboSanPham.getValue();
        if (selected == null) {
            hienThiLoi("Vui lòng chọn sản phẩm cần đổi trả.");
            return;
        }

        String slStr = txtSoLuong.getText().trim();
        if (slStr.isEmpty()) {
            hienThiLoi("Vui lòng nhập số lượng đổi trả.");
            txtSoLuong.requestFocus();
            return;
        }

        int soLuongDoiTra;
        try {
            soLuongDoiTra = Integer.parseInt(slStr);
            if (soLuongDoiTra <= 0) {
                hienThiLoi("Số lượng đổi trả phải lớn hơn 0.");
                txtSoLuong.requestFocus();
                return;
            }
            if (soLuongDoiTra > selected.getSoLuong()) {
                hienThiLoi("Số lượng đổi trả không được vượt quá số lượng đã mua (" + selected.getSoLuong() + ").");
                txtSoLuong.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            hienThiLoi("Số lượng nhập không hợp lệ.");
            txtSoLuong.requestFocus();
            return;
        }

        // Tạo đối tượng chi tiết trả về
        ReturnNoteItem ketQua = new ReturnNoteItem(
                null,
                maHoaDon,
                selected.getMaSanPham(),
                selected.getTenSanPham(),
                soLuongDoiTra,
                selected.getDonGia()
        );

        if (onThemChiTiet != null) {
            onThemChiTiet.accept(ketQua);
        }
        dongHopThoai();
    }

    /**
     * Hiển thị thông điệp cảnh báo lỗi.
     *
     * @param thongDiep Nội dung lỗi
     */
    private void hienThiLoi(String thongDiep) {
        lblBaoLoi.setText("⚠ " + thongDiep);
        lblBaoLoi.setVisible(true);
        lblBaoLoi.setManaged(true);
    }

    /**
     * Đóng hộp thoại.
     */
    private void dongHopThoai() {
        if (onDong != null) {
            onDong.run();
        }
    }
}

