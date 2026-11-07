package com.petshop.gui.hoadon;

import com.petshop.bus.HoaDonBUS;
import com.petshop.bus.KhachHangBUS;
import com.petshop.entity.*;
import com.petshop.util.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Giao diện Lập hóa đơn và thanh toán bán lẻ POS (com.petshop.gui).
 * Tuân thủ tuyệt đối Bộ chỉ thị kỹ thuật IUH:
 * - Điều 1: Tiền tệ dùng 100% kiểu double nguyên thủy, tuyệt đối không dùng BigDecimal.
 * - Điều 2: Khóa ngoại dùng Object Reference (KhachHang, NhanVien, KhuyenMai, PhieuDichVu).
 *           Thuộc tính dẫn xuất tongTien, thanhTien... được tính tự động từ entity và BUS.
 * - Điều 3: Kiến trúc 3 tầng: GUI chỉ bắt sự kiện và hiển thị, không viết SQL, gọi qua BUS.
 * - Điều 4: CẤM nạp toàn bộ CSDL lên RAM qua ComboBox. Quầy POS dùng ô quét mã vạch
 *           txtMaVachSanPham + Enter, tra cứu khách hàng theo SĐT trực tiếp dưới DB kèm
 *           nút "+ Thêm mới nhanh" và "Khách vãng lai". Khóa cứng giá bán.
 * - Điều 5: Thu ngân tự động lấy từ SessionManager.getTaiKhoanHienTai().getNhanVien(),
 *           không cho phép chọn thu ngân qua ComboBox.
 * - Điều 6: Validate SĐT 10 số, ném IllegalArgumentException khi sai nghiệp vụ.
 */
public class LapHoaDonThanhToanView extends VBox {

    private static final String CSS_CHUNG = "/com/petshop/gui/css/chung.css";
    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 VNĐ");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final HoaDonBUS controller;
    private final KhachHangBUS khachHangBUS;

    // Dữ liệu nguồn giỏ hàng và khuyến mãi
    private final ObservableList<DongHoaDon> dsDongHang = FXCollections.observableArrayList();
    private final ObservableList<KhuyenMai> dsKhuyenMai = FXCollections.observableArrayList();

    // Đối tượng nghiệp vụ đang chọn (Điều 2 IUH)
    private KhachHang khachHangHienTai = null;
    private NhanVien nhanVienHienTai = null;

    // 1. Thông tin quầy & Hóa đơn
    private final TextField txtMaHoaDon = new TextField();
    private final DatePicker dpNgayLap = new DatePicker(LocalDate.now());

    // Tra cứu khách hàng theo SĐT trực tiếp dưới DB (Điều 4 IUH)
    private final TextField txtTimKhach = new TextField();
    private final Button btnKiemTraSdt = new Button("Tra cứu SĐT (F2)");
    private final Label lblTenKhachHang = new Label("Khách vãng lai");
    private final Button btnKhachVangLai = new Button("✕ Bỏ chọn");

    // Thông tin Phiếu dịch vụ nạp tự động hoặc chọn thủ công
    private String maPhieuDichVuDaNap = null;
    private String maLichHenDaNap = null;
    private String trangThaiPhieuDichVuDaNap = null;

    // Hộp hiển thị thông tin Phiếu dịch vụ trong ngày
    private final HBox boxPhieuDichVu = new HBox(10);
    private final Label lblIconPhieu = new Label("📋 Phiếu dịch vụ trong ngày:");
    private final Label lblMaPhieuDv = new Label();
    private final Label lblTrangThaiPhieuDv = new Label();
    private final Label lblCanhBaoThanhToan = new Label();
    private final Button btnBoPhieuDv = new Button("✕ Bỏ phiếu DV");

    // 2. Ô tìm kiếm & quét mã vạch sản phẩm POS
    private final TextField txtMaVachSanPham = new TextField();
    private final Button btnQuetMaVach = new Button("Thêm SP (Enter)");
    private final Popup popupGoiYSP = new Popup();
    private final ListView<SanPham> lstGoiYSP = new ListView<>();
    private boolean dangChonGoiY = false;

    // Quản lý đơn hàng tạm (Hold Orders - xử lý khách thiếu tiền / đổi ý)
    private final ObservableList<DonHangTam> dsDonTam = FXCollections.observableArrayList();
    private int demDonTam = 1;
    private final Button btnLuuTam = new Button("⏸ Lưu tạm (F6)");
    private final Button btnDonTam = new Button("📂 Đơn tạm (0)");

    // 3. Bảng chi tiết bán hàng
    private final TableView<DongHoaDon> bangChiTiet = new TableView<>();

    // 4. Tóm tắt tiền tệ & Khuyến mãi & Điểm tích lũy (double nguyên thủy - Điều 1)
    private final Label lblTongTienHang = new Label("0 VNĐ");
    private final ComboBox<KhuyenMai> cboKhuyenMai = new ComboBox<>();
    private final Label lblTienGiamGiaKM = new Label("- 0 VNĐ");

    // Điểm tích lũy
    private final CheckBox chkDungDiem = new CheckBox("Dùng điểm tích lũy (1đ = 1.000đ)");
    private final Spinner<Integer> spnDiemDung = new Spinner<>(0, 0, 0);
    private final Button btnDungToiDaDiem = new Button("Dùng tối đa");
    private final Label lblTienGiamTuDiem = new Label("- 0 VNĐ");
    private final Label lblDiemCongDuKien = new Label("Khách vãng lai (không tích điểm)");

    private final Label lblTongThanhToan = new Label("0 VNĐ");

    // 5. Phương thức thanh toán (chỉ Tiền mặt và Chuyển khoản)
    private final RadioButton radTienMat = new RadioButton("Tiền mặt");
    private final RadioButton radChuyenKhoan = new RadioButton("Chuyển khoản");
    private final ToggleGroup grpPhuongThuc = new ToggleGroup();

    // Box Tiền mặt
    private final VBox boxTienMat = new VBox(6);
    private final TextField txtTienKhachDua = new TextField();
    private final Label lblTienThoiLai = new Label("0 VNĐ");

    // Box Chuyển khoản
    private final VBox boxChuyenKhoan = new VBox(6);
    private final Label lblNoiDungCk = new Label();

    // Nút hành động
    private final Button btnLamMoi = new Button("Làm mới (F5)");
    private final Button btnThanhToanVaIn = new Button("🖨 Thanh toán & In (F9)");
    private final Button btnThanhToan = new Button("✔ Thanh toán");

    // Biến tính toán (Điều 1 IUH: 100% double nguyên thủy)
    private double tongTienHangHienTai = 0.0;
    private double tienGiamKMHienTai = 0.0;
    private double tienGiamDiemHienTai = 0.0;
    private double tongThanhToanHienTai = 0.0;
    private int diemCongMoiHienTai = 0;
    private boolean dangCapNhatTien = false;

    public LapHoaDonThanhToanView() {
        this.controller = new HoaDonBUS();
        this.khachHangBUS = new KhachHangBUS();

        setSpacing(10);
        setPadding(new Insets(14));
        getStyleClass().add("khung-phan-he");

        khoiTaoGiaoDien();
        dangKySuKien();
        napDuLieuBanDau();
        caiDatPhimTat();
    }

    private void khoiTaoGiaoDien() {
        // Tiêu đề
        VBox pnlTieuDe = new VBox(3);
        Label lblTieuDe = new Label("LẬP HÓA ĐƠN & THANH TOÁN BÁN LẺ (POS)");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Bán hàng siêu tốc: Quét mã vạch SP (F4), tra cứu SĐT khách (F2), Lưu tạm đơn (F6), Đơn tạm (F7), thanh toán & in bill A6 (F9).");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // Khung thông tin quầy & Khách hàng
        VBox pnlThongTin = taoKhungThongTinQuay();

        // Khởi tạo popup autocomplete gợi ý sản phẩm
        khoiTaoPopupGoiYSP();

        // Khung quét mã vạch POS
        VBox pnlChonHang = taoKhungQuetMaVachPos();

        // Bảng chi tiết
        taoBangChiTiet();
        VBox.setVgrow(bangChiTiet, Priority.ALWAYS);

        // Khung thanh toán dưới đáy
        HBox pnlThanhToan = taoKhungThanhToanDay();

        getChildren().addAll(pnlTieuDe, pnlThongTin, pnlChonHang, bangChiTiet, pnlThanhToan);
    }

    private VBox taoKhungThongTinQuay() {
        VBox pnl = new VBox(8);
        pnl.setPadding(new Insets(10, 12, 10, 12));
        pnl.getStyleClass().add("hop-noi-dung");

        GridPane luoi = new GridPane();
        luoi.setHgap(10);
        luoi.setVgap(8);

        txtMaHoaDon.setEditable(false);
        txtMaHoaDon.setPrefWidth(170);
        txtMaHoaDon.setStyle("-fx-font-weight: bold; -fx-text-fill: #0284c7;");

        dpNgayLap.setPrefWidth(130);
        dpNgayLap.setEditable(false);
        dpNgayLap.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(LocalDate d) { return d != null ? DINH_DANG_NGAY.format(d) : ""; }
            @Override public LocalDate fromString(String s) { return null; }
        });

        // Tra cứu khách hàng trực tiếp dưới DB theo SĐT (Điều 4 IUH)
        txtTimKhach.setPromptText("Nhập SĐT (10 số)...");
        txtTimKhach.setPrefWidth(140);
        txtTimKhach.setTextFormatter(new TextFormatter<String>(change -> {
            String textMoi = change.getControlNewText();
            if (textMoi.matches("\\d{0,10}")) {
                return change;
            }
            String chiSo = textMoi.replaceAll("[^0-9]", "");
            if (chiSo.length() > 10) {
                chiSo = chiSo.substring(0, 10);
            }
            change.setText(chiSo);
            change.setRange(0, change.getControlText().length());
            change.setCaretPosition(chiSo.length());
            change.setAnchor(chiSo.length());
            return change;
        }));

        btnKiemTraSdt.getStyleClass().add("nut-phu");
        btnKiemTraSdt.setStyle("-fx-font-weight: bold;");

        lblTenKhachHang.setStyle("-fx-font-weight: bold; -fx-text-fill: #0369a1;");

        btnKhachVangLai.getStyleClass().add("nut-phu");
        btnKhachVangLai.setStyle("-fx-font-size: 11px;");
        btnKhachVangLai.setTooltip(new Tooltip("Chuyển về khách vãng lai"));
        btnKhachVangLai.setOnAction(e -> datKhachVangLai());

        Button btnNapPhieuDv = new Button("📥 Nạp từ Phiếu dịch vụ");
        btnNapPhieuDv.setStyle("-fx-background-color: #0284c7; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-cursor: hand;");
        btnNapPhieuDv.setOnAction(e -> moCuaSoChonPhieuDichVu());

        Region spacerKh = new Region();
        HBox.setHgrow(spacerKh, Priority.ALWAYS);

        HBox hangKhachHang = new HBox(8, txtTimKhach, btnKiemTraSdt, lblTenKhachHang, btnKhachVangLai, spacerKh, btnNapPhieuDv);
        hangKhachHang.setAlignment(Pos.CENTER_LEFT);

        // Bố trí Grid gọn gàng, loại bỏ hoàn toàn ô Thu ngân và nút Thêm khách mới
        luoi.add(new Label("Số HĐ:"), 0, 0);
        luoi.add(txtMaHoaDon, 1, 0);
        luoi.add(new Label("Ngày lập:"), 2, 0);
        luoi.add(dpNgayLap, 3, 0);

        luoi.add(new Label("Khách hàng:"), 0, 1);
        luoi.add(hangKhachHang, 1, 1, 3, 1);

        // Hộp thông tin Phiếu dịch vụ tự động nạp
        boxPhieuDichVu.setAlignment(Pos.CENTER_LEFT);
        boxPhieuDichVu.setPadding(new Insets(6, 12, 6, 12));
        boxPhieuDichVu.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 4; -fx-background-radius: 4;");
        lblIconPhieu.setStyle("-fx-font-weight: bold; -fx-text-fill: #0369a1;");
        lblMaPhieuDv.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #0f172a;");
        btnBoPhieuDv.getStyleClass().add("nut-phu");
        btnBoPhieuDv.setStyle("-fx-font-size: 11px;");
        btnBoPhieuDv.setOnAction(e -> xoaPhieuDichVuDaNap(true));

        Region spacerPdv = new Region();
        HBox.setHgrow(spacerPdv, Priority.ALWAYS);

        boxPhieuDichVu.getChildren().addAll(lblIconPhieu, lblMaPhieuDv, lblTrangThaiPhieuDv, lblCanhBaoThanhToan, spacerPdv, btnBoPhieuDv);
        boxPhieuDichVu.setVisible(false);
        boxPhieuDichVu.setManaged(false);

        pnl.getChildren().addAll(luoi, boxPhieuDichVu);
        return pnl;
    }

    private VBox taoKhungQuetMaVachPos() {
        VBox pnl = new VBox(6);
        pnl.setPadding(new Insets(8, 12, 8, 12));
        pnl.getStyleClass().add("hop-noi-dung");

        HBox hangQuet = new HBox(10);
        hangQuet.setAlignment(Pos.CENTER_LEFT);

        Label lblPos = new Label("Sản phẩm (F4):");
        lblPos.setStyle("-fx-font-weight: bold; -fx-text-fill: #0369a1;");

        txtMaVachSanPham.setPromptText("🔍 Quét mã vạch barcode hoặc gõ mã / tên sản phẩm...");
        txtMaVachSanPham.setPrefWidth(420);
        HBox.setHgrow(txtMaVachSanPham, Priority.ALWAYS);
        txtMaVachSanPham.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        btnQuetMaVach.getStyleClass().add("nut-chinh");
        btnQuetMaVach.setText("Thêm (Enter)");
        btnQuetMaVach.setOnAction(e -> xuLyQuetMaVachHoacChonGoiY());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnLuuTam.getStyleClass().add("nut-phu");
        btnLuuTam.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: #b45309; -fx-background-color: #fef3c7; -fx-border-color: #f59e0b; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
        btnLuuTam.setOnAction(e -> xuLyLuuTamDonHang());

        btnDonTam.getStyleClass().add("nut-phu");
        btnDonTam.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: #0369a1; -fx-background-color: #e0f2fe; -fx-border-color: #38bdf8; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
        btnDonTam.setOnAction(e -> moCuaSoDonHangTam());

        Button btnXoaDong = new Button("✕ Xóa dòng");
        btnXoaDong.getStyleClass().add("nut-xoa");
        btnXoaDong.setOnAction(e -> xoaDongDangChon());

        Button btnXoaTatCa = new Button("✕ Xóa hết giỏ");
        btnXoaTatCa.getStyleClass().add("nut-phu");
        btnXoaTatCa.setStyle("-fx-font-size: 11px; -fx-text-fill: #dc2626;");
        btnXoaTatCa.setOnAction(e -> {
            if (!dsDongHang.isEmpty()) {
                dsDongHang.clear();
                capNhatTongTienThanhToan();
                txtMaVachSanPham.requestFocus();
            }
        });

        hangQuet.getChildren().addAll(lblPos, txtMaVachSanPham, btnQuetMaVach, spacer, btnLuuTam, btnDonTam, btnXoaDong, btnXoaTatCa);
        pnl.getChildren().add(hangQuet);
        return pnl;
    }

    private void taoBangChiTiet() {
        bangChiTiet.getStyleClass().add("bang-du-lieu");
        bangChiTiet.setItems(dsDongHang);
        bangChiTiet.setPlaceholder(new Label("Giỏ hàng đang trống. Bắn mã vạch SP (Enter) hoặc nạp từ Phiếu dịch vụ."));

        TableColumn<DongHoaDon, Integer> colStt = new TableColumn<>("STT");
        colStt.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getStt()));
        colStt.setPrefWidth(45);
        colStt.setStyle("-fx-alignment: center;");

        TableColumn<DongHoaDon, String> colLoai = new TableColumn<>("Phân loại");
        colLoai.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenLoai()));
        colLoai.setPrefWidth(85);
        colLoai.setStyle("-fx-alignment: center;");

        TableColumn<DongHoaDon, String> colMa = new TableColumn<>("Mã hàng");
        colMa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaHang()));
        colMa.setPrefWidth(120);

        TableColumn<DongHoaDon, String> colTen = new TableColumn<>("Tên mặt hàng / Dịch vụ");
        colTen.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenHang()));
        colTen.setPrefWidth(240);

        TableColumn<DongHoaDon, String> colDvt = new TableColumn<>("ĐVT");
        colDvt.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDonViTinh()));
        colDvt.setPrefWidth(60);
        colDvt.setStyle("-fx-alignment: center;");

        // Cột số lượng cho phép điều chỉnh qua Spinner (nếu là sản phẩm)
        TableColumn<DongHoaDon, Integer> colSl = new TableColumn<>("Số lượng");
        colSl.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSoLuong()));
        colSl.setPrefWidth(90);
        colSl.setStyle("-fx-alignment: center;");
        colSl.setCellFactory(col -> new TableCell<>() {
            private final Spinner<Integer> spinner = new Spinner<>(1, 9999, 1);
            {
                spinner.setEditable(true);
                spinner.setPrefWidth(80);
                spinner.valueProperty().addListener((obs, cu, moi) -> {
                    if (moi != null && getTableRow() != null && getTableRow().getItem() != null) {
                        DongHoaDon item = getTableRow().getItem();
                        if (item.getLoai() == DongHoaDon.LoaiDong.SAN_PHAM) {
                            int tonKho = controller.traCuuTonKho(item.getMaHang());
                            if (moi > tonKho) {
                                hienThiCanhBao("Số lượng " + moi + " vượt quá tồn kho hiện tại (" + tonKho + ")!");
                                Platform.runLater(() -> spinner.getValueFactory().setValue(cu != null ? cu : 1));
                                return;
                            }
                        }
                        item.setSoLuong(moi);
                        bangChiTiet.refresh();
                        capNhatTongTienThanhToan();
                    }
                });
            }

            @Override
            protected void updateItem(Integer sl, boolean empty) {
                super.updateItem(sl, empty);
                if (empty || sl == null) {
                    setGraphic(null);
                } else {
                    DongHoaDon item = getTableRow() != null ? getTableRow().getItem() : null;
                    if (item != null && item.getLoai() == DongHoaDon.LoaiDong.DICH_VU) {
                        setText("1");
                        setGraphic(null);
                    } else {
                        setText(null);
                        spinner.getValueFactory().setValue(sl);
                        setGraphic(spinner);
                    }
                }
            }
        });

        // Đơn giá KHÓA CỨNG (Điều 4 IUH)
        TableColumn<DongHoaDon, String> colGia = new TableColumn<>("Đơn giá (Khóa)");
        colGia.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().getDonGia())));
        colGia.setPrefWidth(120);
        colGia.setStyle("-fx-alignment: center-right; -fx-text-fill: #334155;");

        // Thành tiền tính theo tinhThanhTien() (Điều 2 IUH)
        TableColumn<DongHoaDon, String> colThanhTien = new TableColumn<>("Thành tiền");
        colThanhTien.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().tinhThanhTien())));
        colThanhTien.setPrefWidth(130);
        colThanhTien.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");

        TableColumn<DongHoaDon, String> colGhiChu = new TableColumn<>("Ghi chú");
        colGhiChu.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGhiChu()));
        colGhiChu.setPrefWidth(140);

        bangChiTiet.getColumns().addAll(colStt, colLoai, colMa, colTen, colDvt, colSl, colGia, colThanhTien, colGhiChu);
    }

    private HBox taoKhungThanhToanDay() {
        HBox pnl = new HBox(12);
        pnl.setPadding(new Insets(10, 14, 10, 14));
        pnl.getStyleClass().add("hop-noi-dung");
        pnl.setAlignment(Pos.CENTER_LEFT);

        // =====================================================================
        // CỘT 1: TIỀN HÀNG, KHUYẾN MÃI, ĐIỂM TÍCH LŨY, TỔNG THANH TOÁN
        // =====================================================================
        VBox cotTien = new VBox(4);
        cotTien.setMinWidth(280);
        cotTien.setPrefWidth(290);

        GridPane luoiTien = new GridPane();
        luoiTien.setHgap(8);
        luoiTien.setVgap(4);

        ColumnConstraints colTienNhan = new ColumnConstraints();
        colTienNhan.setMinWidth(90);
        colTienNhan.setPrefWidth(95);
        ColumnConstraints colTienGiaTri = new ColumnConstraints();
        colTienGiaTri.setHgrow(Priority.ALWAYS);
        luoiTien.getColumnConstraints().addAll(colTienNhan, colTienGiaTri);

        Label lblNhanTienHang = new Label("Tiền hàng:");
        lblNhanTienHang.setMinWidth(Region.USE_PREF_SIZE);
        lblTongTienHang.setStyle("-fx-font-weight: bold;");
        lblTongTienHang.setMinWidth(Region.USE_PREF_SIZE);
        luoiTien.add(lblNhanTienHang, 0, 0);
        luoiTien.add(lblTongTienHang, 1, 0);

        Label lblNhanKm = new Label("Khuyến mãi:");
        lblNhanKm.setMinWidth(Region.USE_PREF_SIZE);
        cboKhuyenMai.setPromptText("-- Không áp dụng --");
        cboKhuyenMai.setPrefWidth(180);
        cboKhuyenMai.setMaxWidth(Double.MAX_VALUE);
        luoiTien.add(lblNhanKm, 0, 1);
        luoiTien.add(cboKhuyenMai, 1, 1);

        Label lblNhanGiamKm = new Label("Tiền giảm KM:");
        lblNhanGiamKm.setMinWidth(Region.USE_PREF_SIZE);
        lblTienGiamGiaKM.setStyle("-fx-font-weight: bold; -fx-text-fill: #dc2626;");
        lblTienGiamGiaKM.setMinWidth(Region.USE_PREF_SIZE);
        luoiTien.add(lblNhanGiamKm, 0, 2);
        luoiTien.add(lblTienGiamGiaKM, 1, 2);

        // Thanh toán kết hợp điểm tích lũy
        chkDungDiem.setDisable(true);
        chkDungDiem.setStyle("-fx-font-size: 11px;");
        chkDungDiem.setMinWidth(Region.USE_PREF_SIZE);

        spnDiemDung.setPrefWidth(75);
        spnDiemDung.setMinWidth(70);
        spnDiemDung.setEditable(true);
        spnDiemDung.setDisable(true);
        spnDiemDung.getEditor().textProperty().addListener((obs, cu, moi) -> {
            if (moi != null && !moi.matches("\\d*")) {
                spnDiemDung.getEditor().setText(cu != null ? cu : "");
            }
        });
        spnDiemDung.getEditor().setOnAction(e -> {
            try {
                String txt = spnDiemDung.getEditor().getText();
                if (txt != null && !txt.isBlank()) {
                    int val = Integer.parseInt(txt.trim());
                    if (spnDiemDung.getValueFactory() != null) {
                        spnDiemDung.getValueFactory().setValue(val);
                    }
                }
            } catch (Exception ignored) {}
        });

        btnDungToiDaDiem.setStyle("-fx-font-size: 11px; -fx-padding: 2 6;");
        btnDungToiDaDiem.setMinWidth(Region.USE_PREF_SIZE);
        btnDungToiDaDiem.setDisable(true);

        HBox hangDiem = new HBox(4, spnDiemDung, btnDungToiDaDiem, lblTienGiamTuDiem);
        hangDiem.setAlignment(Pos.CENTER_LEFT);
        lblTienGiamTuDiem.setStyle("-fx-font-weight: bold; -fx-text-fill: #dc2626;");
        lblTienGiamTuDiem.setMinWidth(Region.USE_PREF_SIZE);

        Label lblNhanDiemDung = new Label("Điểm sử dụng:");
        lblNhanDiemDung.setMinWidth(Region.USE_PREF_SIZE);

        luoiTien.add(chkDungDiem, 0, 3, 2, 1);
        luoiTien.add(lblNhanDiemDung, 0, 4);
        luoiTien.add(hangDiem, 1, 4);

        Label lblTieuDeTt = new Label("THANH TOÁN:");
        lblTieuDeTt.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        lblTieuDeTt.setMinWidth(Region.USE_PREF_SIZE);
        lblTongThanhToan.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0284c7;");
        lblTongThanhToan.setMinWidth(Region.USE_PREF_SIZE);
        luoiTien.add(lblTieuDeTt, 0, 5);
        luoiTien.add(lblTongThanhToan, 1, 5);

        lblDiemCongDuKien.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-style: italic;");
        luoiTien.add(lblDiemCongDuKien, 0, 6, 2, 1);

        cotTien.getChildren().add(luoiTien);

        // =====================================================================
        // CỘT 2: HÌNH THỨC THANH TOÁN (TIỀN MẶT / CHUYỂN KHOẢN)
        // =====================================================================
        VBox cotHinhThuc = new VBox(6);
        cotHinhThuc.setMinWidth(265);
        cotHinhThuc.setPrefWidth(280);

        Label lblNhanHinhThuc = new Label("Hình thức thanh toán:");
        lblNhanHinhThuc.setStyle("-fx-font-weight: 600; -fx-text-fill: #334155;");
        lblNhanHinhThuc.setMinWidth(Region.USE_PREF_SIZE);

        radTienMat.setToggleGroup(grpPhuongThuc);
        radChuyenKhoan.setToggleGroup(grpPhuongThuc);
        radTienMat.setSelected(true);
        radTienMat.setMinWidth(Region.USE_PREF_SIZE);
        radChuyenKhoan.setMinWidth(Region.USE_PREF_SIZE);

        HBox hangRadio = new HBox(16, radTienMat, radChuyenKhoan);
        hangRadio.setAlignment(Pos.CENTER_LEFT);

        // Box Tiền mặt
        GridPane luoiTienMat = new GridPane();
        luoiTienMat.setHgap(8);
        luoiTienMat.setVgap(4);

        ColumnConstraints colMatNhan = new ColumnConstraints();
        colMatNhan.setMinWidth(95);
        colMatNhan.setPrefWidth(100);
        ColumnConstraints colMatNhap = new ColumnConstraints();
        colMatNhap.setHgrow(Priority.ALWAYS);
        luoiTienMat.getColumnConstraints().addAll(colMatNhan, colMatNhap);

        txtTienKhachDua.setPromptText("Nhập tiền đưa...");
        txtTienKhachDua.setPrefWidth(110);
        txtTienKhachDua.setMinWidth(90);
        txtTienKhachDua.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d*") ? change : null));

        Button btnDuTien = new Button("Đủ tiền");
        btnDuTien.setStyle("-fx-font-size: 11px; -fx-padding: 3 8;");
        btnDuTien.setMinWidth(Region.USE_PREF_SIZE);
        btnDuTien.setOnAction(e -> {
            txtTienKhachDua.setText(String.valueOf((long) tongThanhToanHienTai));
            tinhToanTienThoi();
        });

        HBox hangKhachDua = new HBox(4, txtTienKhachDua, btnDuTien);
        hangKhachDua.setAlignment(Pos.CENTER_LEFT);

        Label lblNhanTienDua = new Label("Tiền khách đưa:");
        lblNhanTienDua.setMinWidth(Region.USE_PREF_SIZE);

        Label lblNhanTienThoi = new Label("Tiền thối lại:");
        lblNhanTienThoi.setMinWidth(Region.USE_PREF_SIZE);

        lblTienThoiLai.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #15803d;");
        lblTienThoiLai.setMinWidth(Region.USE_PREF_SIZE);

        luoiTienMat.add(lblNhanTienDua, 0, 0);
        luoiTienMat.add(hangKhachDua, 1, 0);
        luoiTienMat.add(lblNhanTienThoi, 0, 1);
        luoiTienMat.add(lblTienThoiLai, 1, 1);

        boxTienMat.getChildren().add(luoiTienMat);

        // Box Chuyển khoản
        boxChuyenKhoan.setStyle("-fx-background-color: #f0fdf4; -fx-padding: 8; -fx-border-color: #bbf7d0; -fx-border-radius: 4; -fx-background-radius: 4;");
        Label lblTk = new Label("STK: 0909000000 (MB Bank) - PET SHOP HAPPY TAILS");
        lblTk.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #166534;");
        lblTk.setMinWidth(Region.USE_PREF_SIZE);
        lblNoiDungCk.setStyle("-fx-font-size: 11px; -fx-text-fill: #15803d;");
        lblNoiDungCk.setText("Nội dung CK: " + txtMaHoaDon.getText());
        boxChuyenKhoan.getChildren().addAll(lblTk, lblNoiDungCk);
        boxChuyenKhoan.setVisible(false);
        boxChuyenKhoan.setManaged(false);

        cotHinhThuc.getChildren().addAll(lblNhanHinhThuc, hangRadio, boxTienMat, boxChuyenKhoan);

        // =====================================================================
        // CỘT 3: CÁC NÚT THAO TÁC (THANH TOÁN & IN F9, THANH TOÁN, LÀM MỚI F5)
        // =====================================================================
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox cotNut = new VBox(8);
        cotNut.setAlignment(Pos.CENTER_RIGHT);
        cotNut.setMinWidth(270);
        cotNut.setPrefWidth(280);

        btnThanhToanVaIn.getStyleClass().add("nut-chinh");
        btnThanhToanVaIn.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-pref-height: 42px; -fx-cursor: hand;");
        btnThanhToanVaIn.setMaxWidth(Double.MAX_VALUE);
        btnThanhToanVaIn.setMinWidth(Region.USE_PREF_SIZE);
        btnThanhToanVaIn.setOnAction(e -> xuLyThanhToan(true));

        btnThanhToan.getStyleClass().add("nut-chinh");
        btnThanhToan.setStyle("-fx-background-color: #16a34a; -fx-font-weight: bold; -fx-font-size: 12.5px; -fx-pref-height: 34px; -fx-cursor: hand;");
        btnThanhToan.setMinWidth(Region.USE_PREF_SIZE);
        btnThanhToan.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnThanhToan, Priority.ALWAYS);
        btnThanhToan.setOnAction(e -> xuLyThanhToan(false));

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setStyle("-fx-font-size: 12px; -fx-pref-height: 34px; -fx-cursor: hand;");
        btnLamMoi.setMinWidth(Region.USE_PREF_SIZE);
        btnLamMoi.setOnAction(e -> lamMoiToanBoForm());

        HBox hangNutPhu = new HBox(8, btnLamMoi, btnThanhToan);
        hangNutPhu.setAlignment(Pos.CENTER_RIGHT);

        cotNut.getChildren().addAll(btnThanhToanVaIn, hangNutPhu);

        pnl.getChildren().addAll(
                cotTien,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                cotHinhThuc,
                spacer,
                cotNut
        );
        return pnl;
    }

    private void dangKySuKien() {
        // Quét mã vạch sản phẩm POS và tìm kiếm autocomplete theo tên / mã
        txtMaVachSanPham.textProperty().addListener((obs, cu, moi) -> {
            if (dangChonGoiY) return;
            if (moi == null || moi.trim().length() < 1) {
                popupGoiYSP.hide();
                return;
            }
            capNhatGoiYSanPham(moi.trim());
        });

        txtMaVachSanPham.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DOWN && popupGoiYSP.isShowing()) {
                lstGoiYSP.requestFocus();
                lstGoiYSP.getSelectionModel().selectFirst();
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                popupGoiYSP.hide();
                event.consume();
            }
        });

        txtMaVachSanPham.setOnAction(e -> xuLyQuetMaVachHoacChonGoiY());

        // Tìm kiếm khách hàng theo SĐT (Enter)
        txtTimKhach.setOnAction(e -> xuLyKiemTraSoDienThoai());
        btnKiemTraSdt.setOnAction(e -> xuLyKiemTraSoDienThoai());

        // Tự động kiểm tra khách hàng khi gõ đủ 10 số
        txtTimKhach.textProperty().addListener((obs, cu, moi) -> {
            if (moi == null) return;
            if (moi.trim().isEmpty()) {
                datKhachVangLai();
            } else if (moi.length() == 10 && moi.matches("^0\\d{9}$")) {
                xuLyKiemTraSoDienThoai();
            }
        });

        // Chọn khuyến mãi
        cboKhuyenMai.setOnAction(e -> capNhatTongTienThanhToan());

        // Sử dụng điểm tích lũy
        chkDungDiem.setOnAction(e -> {
            boolean dung = chkDungDiem.isSelected();
            spnDiemDung.setDisable(!dung);
            btnDungToiDaDiem.setDisable(!dung);
            if (!dung) {
                if (spnDiemDung.getValueFactory() != null) {
                    spnDiemDung.getValueFactory().setValue(0);
                }
            } else {
                dungToiDaDiem();
            }
            capNhatTongTienThanhToan();
        });

        spnDiemDung.valueProperty().addListener((obs, cu, moi) -> capNhatTongTienThanhToan());
        btnDungToiDaDiem.setOnAction(e -> dungToiDaDiem());

        // Chuyển đổi phương thức thanh toán
        radTienMat.setOnAction(e -> {
            boxTienMat.setVisible(true);
            boxTienMat.setManaged(true);
            boxChuyenKhoan.setVisible(false);
            boxChuyenKhoan.setManaged(false);
            tinhToanTienThoi();
        });

        radChuyenKhoan.setOnAction(e -> {
            boxTienMat.setVisible(false);
            boxTienMat.setManaged(false);
            boxChuyenKhoan.setVisible(true);
            boxChuyenKhoan.setManaged(true);
            lblTienThoiLai.setText("0 VNĐ");
        });

        txtTienKhachDua.textProperty().addListener((obs, cu, moi) -> tinhToanTienThoi());
    }

    private void napDuLieuBanDau() {
        txtMaHoaDon.setText(controller.taoMaHoaDonMoi());
        lblNoiDungCk.setText("Nội dung CK: " + txtMaHoaDon.getText());

        // Nhân viên thu ngân tự động lấy từ phiên đăng nhập (Điều 5 IUH)
        TaiKhoan tk = SessionManager.getTaiKhoanHienTai();
        if (tk != null && tk.getNhanVien() != null) {
            this.nhanVienHienTai = tk.getNhanVien();
        }

        // Khuyến mãi
        try {
            dsKhuyenMai.clear();
            dsKhuyenMai.add(null); // Ô trống: Không áp dụng
            dsKhuyenMai.addAll(controller.layDanhSachKhuyenMai());
            cboKhuyenMai.setItems(dsKhuyenMai);
            cboKhuyenMai.setConverter(new javafx.util.StringConverter<>() {
                @Override public String toString(KhuyenMai km) {
                    return km != null ? km.getTenChuongTrinh() + " (" + km.getMaKhuyenMai() + ")" : "-- Không áp dụng --";
                }
                @Override public KhuyenMai fromString(String s) { return null; }
            });
            cboKhuyenMai.setValue(null);
        } catch (Exception ignored) {}
    }

    private void caiDatPhimTat() {
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.F4) {
                        txtMaVachSanPham.requestFocus();
                        txtMaVachSanPham.selectAll();
                        event.consume();
                    } else if (event.getCode() == KeyCode.F2) {
                        txtTimKhach.requestFocus();
                        txtTimKhach.selectAll();
                        event.consume();
                    } else if (event.getCode() == KeyCode.F5) {
                        lamMoiToanBoForm();
                        event.consume();
                    } else if (event.getCode() == KeyCode.F6) {
                        xuLyLuuTamDonHang();
                        event.consume();
                    } else if (event.getCode() == KeyCode.F7) {
                        moCuaSoDonHangTam();
                        event.consume();
                    } else if (event.getCode() == KeyCode.F9) {
                        xuLyThanhToan(true);
                        event.consume();
                    }
                });
            }
        });
    }

    // =========================================================================
    // POS: QUÉT MÃ VẠCH & TÌM KIẾM SẢN PHẨM AUTOCOMPLETE (Điều 4 IUH)
    // =========================================================================

    private void khoiTaoPopupGoiYSP() {
        popupGoiYSP.setAutoHide(true);
        lstGoiYSP.setPrefWidth(580);
        lstGoiYSP.setPrefHeight(230);
        lstGoiYSP.getStyleClass().add("bang-du-lieu");

        lstGoiYSP.setCellFactory(param -> new ListCell<>() {
            private final HBox pnlDong = new HBox(10);
            private final Label lblMa = new Label();
            private final Label lblTen = new Label();
            private final Region spacer = new Region();
            private final Label lblTon = new Label();
            private final Label lblGia = new Label();

            {
                pnlDong.setAlignment(Pos.CENTER_LEFT);
                pnlDong.setPadding(new Insets(5, 10, 5, 10));

                lblMa.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0369a1; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 2 6; -fx-background-radius: 4; -fx-min-width: 125px; -fx-alignment: center;");
                lblTen.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #0f172a;");
                HBox.setHgrow(spacer, Priority.ALWAYS);
                lblTon.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #059669; -fx-min-width: 85px; -fx-alignment: center-right;");
                lblGia.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #b91c1c; -fx-min-width: 110px; -fx-alignment: center-right;");

                pnlDong.getChildren().addAll(lblMa, lblTen, spacer, lblTon, lblGia);
            }

            @Override
            protected void updateItem(SanPham sp, boolean empty) {
                super.updateItem(sp, empty);
                if (empty || sp == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    lblMa.setText(sp.getMaSanPham());
                    lblTen.setText(sp.getTenSanPham());

                    int ton = sp.getSoLuongTon();
                    String dvt = sp.getDonViTinh() != null ? sp.getDonViTinh() : "SP";
                    if (ton <= 0) {
                        lblTon.setText("Hết hàng");
                        lblTon.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: #dc2626; -fx-min-width: 85px; -fx-alignment: center-right;");
                    } else if (sp.isSapHetHang()) {
                        lblTon.setText("Còn " + ton + " " + dvt);
                        lblTon.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: #d97706; -fx-min-width: 85px; -fx-alignment: center-right;");
                    } else {
                        lblTon.setText("Tồn: " + ton + " " + dvt);
                        lblTon.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #059669; -fx-min-width: 85px; -fx-alignment: center-right;");
                    }

                    double gia = sp.getGiaBan();
                    lblGia.setText(DINH_DANG_TIEN.format(gia));

                    setGraphic(pnlDong);
                }
            }
        });

        lstGoiYSP.setOnMouseClicked(event -> {
            SanPham spChon = lstGoiYSP.getSelectionModel().getSelectedItem();
            if (spChon != null) {
                chonSanPhamVaoGio(spChon);
            }
        });

        lstGoiYSP.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                SanPham spChon = lstGoiYSP.getSelectionModel().getSelectedItem();
                if (spChon != null) {
                    chonSanPhamVaoGio(spChon);
                }
            } else if (event.getCode() == KeyCode.ESCAPE) {
                popupGoiYSP.hide();
                txtMaVachSanPham.requestFocus();
            }
        });

        popupGoiYSP.getContent().add(lstGoiYSP);
    }

    private void capNhatGoiYSanPham(String tuKhoa) {
        try {
            List<SanPham> ketQua = controller.timKiemSanPhamGoiY(tuKhoa);
            if (ketQua.isEmpty()) {
                popupGoiYSP.hide();
                return;
            }

            lstGoiYSP.setItems(FXCollections.observableArrayList(ketQua));
            if (!popupGoiYSP.isShowing() && txtMaVachSanPham.getScene() != null && txtMaVachSanPham.getScene().getWindow() != null) {
                var bounds = txtMaVachSanPham.localToScreen(txtMaVachSanPham.getBoundsInLocal());
                if (bounds != null) {
                    popupGoiYSP.show(txtMaVachSanPham, bounds.getMinX(), bounds.getMaxY() + 4);
                }
            }
        } catch (Exception ignored) {
            popupGoiYSP.hide();
        }
    }

    private void xuLyQuetMaVachHoacChonGoiY() {
        String input = txtMaVachSanPham.getText() != null ? txtMaVachSanPham.getText().trim() : "";
        if (input.isEmpty()) return;

        // 1. Thử tìm chính xác theo mã hoặc mã vạch (dành cho súng quét mã vạch / Barcode to PC)
        SanPham sp = controller.timSanPhamTheoMaHoacMaVach(input);
        if (sp != null) {
            chonSanPhamVaoGio(sp);
            return;
        }

        // 2. Nếu popup gợi ý đang hiển thị, lấy sản phẩm đang chọn hoặc đầu tiên
        if (popupGoiYSP.isShowing() && !lstGoiYSP.getItems().isEmpty()) {
            SanPham spChon = lstGoiYSP.getSelectionModel().getSelectedItem();
            if (spChon == null) {
                spChon = lstGoiYSP.getItems().get(0);
            }
            chonSanPhamVaoGio(spChon);
            return;
        }

        // 3. Nếu chưa hiện popup, thử tìm kiếm nhanh theo tên
        List<SanPham> ds = controller.timKiemSanPhamGoiY(input);
        if (!ds.isEmpty()) {
            chonSanPhamVaoGio(ds.get(0));
            return;
        }

        try { java.awt.Toolkit.getDefaultToolkit().beep(); } catch (Exception ignored) {}
        popupGoiYSP.hide();
        hienThiCanhBao("Không tìm thấy sản phẩm nào có mã, mã vạch hoặc tên gần giống: '" + input + "'!");
        txtMaVachSanPham.selectAll();
    }

    private void chonSanPhamVaoGio(SanPham sp) {
        if (sp == null) return;
        try {
            int tonKho = controller.traCuuTonKho(sp.getMaSanPham());
            if (tonKho <= 0) {
                try { java.awt.Toolkit.getDefaultToolkit().beep(); } catch (Exception ignored) {}
                popupGoiYSP.hide();
                hienThiCanhBao("Sản phẩm '" + sp.getTenSanPham() + "' hiện đã hết hàng trong kho!");
                txtMaVachSanPham.selectAll();
                txtMaVachSanPham.requestFocus();
                return;
            }

            DongHoaDon dongDaCo = null;
            for (DongHoaDon d : dsDongHang) {
                if (d.getLoai() == DongHoaDon.LoaiDong.SAN_PHAM && d.getMaHang().equalsIgnoreCase(sp.getMaSanPham())) {
                    dongDaCo = d;
                    break;
                }
            }

            if (dongDaCo != null) {
                if (dongDaCo.getSoLuong() + 1 > tonKho) {
                    try { java.awt.Toolkit.getDefaultToolkit().beep(); } catch (Exception ignored) {}
                    popupGoiYSP.hide();
                    hienThiCanhBao("Không thể tăng số lượng. Sản phẩm '" + sp.getTenSanPham() + "' chỉ còn " + tonKho + " " + sp.getDonViTinh() + " trong kho!");
                    txtMaVachSanPham.selectAll();
                    txtMaVachSanPham.requestFocus();
                    return;
                }
                dongDaCo.setSoLuong(dongDaCo.getSoLuong() + 1);
                bangChiTiet.refresh();
            } else {
                DongHoaDon dongMoi = DongHoaDon.tuSanPham(dsDongHang.size() + 1, sp, 1);
                dsDongHang.add(dongMoi);
            }

            for (int i = 0; i < dsDongHang.size(); i++) {
                dsDongHang.get(i).setStt(i + 1);
            }

            dangChonGoiY = true;
            txtMaVachSanPham.clear();
            dangChonGoiY = false;
            popupGoiYSP.hide();
            txtMaVachSanPham.requestFocus();
            capNhatTongTienThanhToan();
        } catch (Exception ex) {
            hienThiLoi("Lỗi thêm sản phẩm: " + ex.getMessage());
        }
    }

    private void xoaDongDangChon() {
        DongHoaDon dong = bangChiTiet.getSelectionModel().getSelectedItem();
        if (dong == null) {
            hienThiCanhBao("Vui lòng chọn dòng cần xóa trong bảng chi tiết!");
            return;
        }

        if (dong.getLoai() == DongHoaDon.LoaiDong.DICH_VU) {
            xoaPhieuDichVuDaNap(true);
        } else {
            dsDongHang.remove(dong);
            for (int i = 0; i < dsDongHang.size(); i++) {
                dsDongHang.get(i).setStt(i + 1);
            }
            bangChiTiet.refresh();
            capNhatTongTienThanhToan();
        }
        txtMaVachSanPham.requestFocus();
    }

    // =========================================================================
    // TRA CỨU KHÁCH HÀNG THEO SĐT TRỰC TIẾP DƯỚI DB (Điều 4 & 6 IUH)
    // =========================================================================

    private void xuLyKiemTraSoDienThoai() {
        String sdt = txtTimKhach.getText() != null ? txtTimKhach.getText().trim() : "";
        if (sdt.isEmpty()) {
            hienThiCanhBao("Vui lòng nhập số điện thoại khách hàng cần tra cứu!");
            txtTimKhach.requestFocus();
            return;
        }

        if (!sdt.matches("^0\\d{9}$")) {
            hienThiCanhBao("Số điện thoại không hợp lệ! Bắt buộc gồm 10 chữ số và bắt đầu bằng số 0 (VD: 0912345678).");
            txtTimKhach.requestFocus();
            return;
        }

        try {
            // Tra cứu trực tiếp DB: WHERE soDienThoai = ? (Điều 4 IUH)
            KhachHang kh = controller.timKhachHangTheoSoDienThoai(sdt);
            if (kh != null) {
                dongBoThongTinKhachHang(kh);
                kiemTraVaGoiYPhieuDichVuTrongNgay(kh);
                txtMaVachSanPham.requestFocus();
            } else {
                hienThiCanhBao("Số điện thoại '" + sdt + "' chưa có trong hệ thống!\nNếu muốn thêm khách hàng mới, vui lòng chuyển sang phân hệ 'Khách hàng' để tạo.");
            }
        } catch (Exception ex) {
            hienThiLoi("Lỗi tra cứu khách hàng: " + ex.getMessage());
        }
    }

    private void dongBoThongTinKhachHang(KhachHang kh) {
        this.khachHangHienTai = kh;
        if (kh == null) {
            datKhachVangLai();
            return;
        }

        lblTenKhachHang.setText(kh.getHoTen() + " (" + kh.getSoDienThoai() + ") - Điểm: " + kh.getDiemTichLuy() + "đ");
        lblTenKhachHang.setStyle("-fx-font-weight: bold; -fx-text-fill: #15803d;");

        // Cập nhật cấu hình spinner dùng điểm
        int diemKhachCo = kh.getDiemTichLuy();
        if (diemKhachCo > 0) {
            chkDungDiem.setDisable(false);
            chkDungDiem.setText("Dùng điểm tích lũy (Có: " + diemKhachCo + " đ)");
            spnDiemDung.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, diemKhachCo, 0));
        } else {
            chkDungDiem.setSelected(false);
            chkDungDiem.setDisable(true);
            chkDungDiem.setText("Dùng điểm tích lũy (Có: 0 đ)");
            spnDiemDung.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0, 0));
            spnDiemDung.setDisable(true);
            btnDungToiDaDiem.setDisable(true);
        }

        capNhatTongTienThanhToan();
    }

    private void datKhachVangLai() {
        this.khachHangHienTai = null;
        txtTimKhach.clear();
        lblTenKhachHang.setText("Khách vãng lai");
        lblTenKhachHang.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic;");
        chkDungDiem.setSelected(false);
        chkDungDiem.setDisable(true);
        chkDungDiem.setText("Dùng điểm tích lũy (1đ = 1.000đ)");
        spnDiemDung.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0, 0));
        spnDiemDung.setDisable(true);
        btnDungToiDaDiem.setDisable(true);
        xoaPhieuDichVuDaNap(true);
        capNhatTongTienThanhToan();
    }

    private void dungToiDaDiem() {
        if (khachHangHienTai == null || !chkDungDiem.isSelected()) return;
        int diemKhachCo = khachHangHienTai.getDiemTichLuy();
        double tienSauKm = Math.max(0.0, tongTienHangHienTai - tienGiamKMHienTai);
        int diemToiDaCoTheDung = (int) Math.ceil(tienSauKm / 1000.0);
        int diemDung = Math.min(diemKhachCo, diemToiDaCoTheDung);

        if (spnDiemDung.getValueFactory() != null) {
            spnDiemDung.getValueFactory().setValue(diemDung);
        }
        capNhatTongTienThanhToan();
    }

    private void kiemTraVaGoiYPhieuDichVuTrongNgay(KhachHang kh) {
        if (kh == null || kh.getMaKhachHang() == null) return;
        try {
            List<PhieuDichVu> dsPhieu = controller.layPhieuDichVuTrongNgayCuaKhachHang(kh.getMaKhachHang(), LocalDate.now());
            if (dsPhieu != null && !dsPhieu.isEmpty()) {
                PhieuDichVu phieuGoiY = dsPhieu.get(0);
                napPhieuDichVuVaoHoaDon(phieuGoiY);

                Platform.runLater(() -> {
                    String thongBao = "Khách hàng '" + kh.getHoTen() + "' có Phiếu dịch vụ #" + phieuGoiY.getMaPhieuDichVu()
                            + " trong ngày hôm nay đã được nạp tự động vào giỏ hàng.";
                    if (!"Hoàn thành".equalsIgnoreCase(phieuGoiY.getTrangThai())) {
                        thongBao += "\n\nLƯU Ý: Phiếu dịch vụ chưa ở trạng thái 'Hoàn thành' nên hệ thống tạm khóa thanh toán!";
                    }
                    hienThiThongBao("Đã nạp phiếu dịch vụ", thongBao);
                });
            } else {
                xoaPhieuDichVuDaNap(false);
            }
        } catch (Exception ignored) {}
    }

    private void napPhieuDichVuVaoHoaDon(PhieuDichVu pChon) {
        if (pChon == null) return;
        try {
            PhieuDichVu phieuChiTiet = controller.layPhieuDichVuDeThanhToan(pChon.getMaPhieuDichVu());
            this.maPhieuDichVuDaNap = phieuChiTiet.getMaPhieuDichVu();
            this.maLichHenDaNap = phieuChiTiet.getMaLichHen();
            this.trangThaiPhieuDichVuDaNap = phieuChiTiet.getTrangThai();

            // Xóa các dòng dịch vụ cũ nếu trước đó từng nạp
            dsDongHang.removeIf(d -> d.getLoai() == DongHoaDon.LoaiDong.DICH_VU);

            if (phieuChiTiet.getDanhSachChiTiet() != null) {
                for (ChiTietPhieuDichVu ct : phieuChiTiet.getDanhSachChiTiet()) {
                    DongHoaDon d = DongHoaDon.tuDichVu(
                            dsDongHang.size() + 1,
                            ct.getMaDichVu(),
                            ct.getTenDichVu() != null ? ct.getTenDichVu() : ct.getMaDichVu(),
                            ct.getMaThuCung(),
                            ct.getTenThuCung(),
                            ct.getDonGia()
                    );
                    dsDongHang.add(d);
                }
            }

            for (int i = 0; i < dsDongHang.size(); i++) {
                dsDongHang.get(i).setStt(i + 1);
            }
            bangChiTiet.refresh();

            lblMaPhieuDv.setText(phieuChiTiet.getMaPhieuDichVu());
            String tt = phieuChiTiet.getTrangThai() != null ? phieuChiTiet.getTrangThai() : "Đang chờ";
            lblTrangThaiPhieuDv.setText("[" + tt + "]");

            boolean daHoanThanh = "Hoàn thành".equalsIgnoreCase(tt);
            if (daHoanThanh) {
                lblTrangThaiPhieuDv.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 4;");
                lblCanhBaoThanhToan.setText("✔ Phiếu DV đã hoàn thành - Sẵn sàng thanh toán");
                lblCanhBaoThanhToan.setStyle("-fx-text-fill: #15803d; -fx-font-weight: bold;");
                btnThanhToan.setDisable(false);
                btnThanhToanVaIn.setDisable(false);
            } else {
                lblTrangThaiPhieuDv.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 4;");
                lblCanhBaoThanhToan.setText("⚠ Phiếu dịch vụ chưa 'Hoàn thành' (" + tt + ") - Cần hoàn thành trước khi thanh toán!");
                lblCanhBaoThanhToan.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                btnThanhToan.setDisable(true);
                btnThanhToanVaIn.setDisable(true);
            }

            boxPhieuDichVu.setVisible(true);
            boxPhieuDichVu.setManaged(true);
            capNhatTongTienThanhToan();
        } catch (Exception ex) {
            hienThiLoi("Lỗi nạp phiếu dịch vụ: " + ex.getMessage());
        }
    }

    private void xoaPhieuDichVuDaNap(boolean capNhatBang) {
        maPhieuDichVuDaNap = null;
        maLichHenDaNap = null;
        trangThaiPhieuDichVuDaNap = null;

        boxPhieuDichVu.setVisible(false);
        boxPhieuDichVu.setManaged(false);

        if (capNhatBang) {
            dsDongHang.removeIf(d -> d.getLoai() == DongHoaDon.LoaiDong.DICH_VU);
            for (int i = 0; i < dsDongHang.size(); i++) {
                dsDongHang.get(i).setStt(i + 1);
            }
            bangChiTiet.refresh();
        }

        btnThanhToan.setDisable(false);
        btnThanhToanVaIn.setDisable(false);
        capNhatTongTienThanhToan();
    }

    private void moCuaSoChonPhieuDichVu() {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        if (getScene() != null && getScene().getWindow() != null) dlg.initOwner(getScene().getWindow());
        dlg.setTitle("Chọn phiếu dịch vụ thanh toán");

        VBox pnlGoc = new VBox(10);
        pnlGoc.setPadding(new Insets(14));
        pnlGoc.getStyleClass().add("khung-phan-he");

        Label lblTieuDe = new Label("DANH SÁCH PHIẾU DỊCH VỤ CHỜ THANH TOÁN");
        lblTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0369a1;");

        // Ô tìm kiếm nhanh theo SĐT / Mã phiếu / Tên khách
        TextField txtTimKiemPhieu = new TextField();
        txtTimKiemPhieu.setPromptText("Tìm kiếm theo SĐT, Tên khách hoặc Mã phiếu...");
        HBox.setHgrow(txtTimKiemPhieu, Priority.ALWAYS);
        HBox pnlTim = new HBox(8, new Label("Tìm kiếm:"), txtTimKiemPhieu);
        pnlTim.setAlignment(Pos.CENTER_LEFT);

        List<PhieuDichVu> dsGoc = new ArrayList<>();
        try {
            dsGoc = controller.layDanhSachPhieuDichVuChuaThanhToan();
        } catch (Exception ex) {
            hienThiLoi("Lỗi nạp danh sách phiếu dịch vụ: " + ex.getMessage());
        }

        ObservableList<PhieuDichVu> dsHienThi = FXCollections.observableArrayList(dsGoc);
        final List<PhieuDichVu> dsPhieuGocCuoi = dsGoc;
        txtTimKiemPhieu.textProperty().addListener((obs, cu, moi) -> {
            if (moi == null || moi.trim().isEmpty()) {
                dsHienThi.setAll(dsPhieuGocCuoi);
            } else {
                String tuKhoa = moi.trim().toLowerCase();
                dsHienThi.setAll(dsPhieuGocCuoi.stream().filter(p ->
                        (p.getMaPhieuDichVu() != null && p.getMaPhieuDichVu().toLowerCase().contains(tuKhoa)) ||
                        (p.getTenKhachHang() != null && p.getTenKhachHang().toLowerCase().contains(tuKhoa)) ||
                        (p.getSoDienThoai() != null && p.getSoDienThoai().contains(tuKhoa))
                ).toList());
            }
        });

        TableView<PhieuDichVu> bang = new TableView<>(dsHienThi);
        bang.getStyleClass().add("bang-du-lieu");
        bang.setPlaceholder(new Label("Hiện không có phiếu dịch vụ nào chờ thanh toán."));
        VBox.setVgrow(bang, Priority.ALWAYS);

        TableColumn<PhieuDichVu, String> colMa = new TableColumn<>("Mã phiếu");
        colMa.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getMaPhieuDichVu()));
        colMa.setPrefWidth(130);

        TableColumn<PhieuDichVu, String> colKhach = new TableColumn<>("Khách hàng");
        colKhach.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getTenKhachHang() != null ? p.getValue().getTenKhachHang() : p.getValue().getMaKhachHang()));
        colKhach.setPrefWidth(160);

        TableColumn<PhieuDichVu, String> colSdt = new TableColumn<>("SĐT");
        colSdt.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getSoDienThoai() != null ? p.getValue().getSoDienThoai() : ""));
        colSdt.setPrefWidth(110);
        colSdt.setStyle("-fx-alignment: center;");

        TableColumn<PhieuDichVu, String> colNgay = new TableColumn<>("Ngày lập");
        colNgay.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getNgayLap() != null ? p.getValue().getNgayLap().format(DINH_DANG_NGAY) : ""));
        colNgay.setPrefWidth(100);
        colNgay.setStyle("-fx-alignment: center;");

        TableColumn<PhieuDichVu, String> colTt = new TableColumn<>("Trạng thái");
        colTt.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getTrangThai()));
        colTt.setPrefWidth(110);
        colTt.setStyle("-fx-alignment: center;");

        bang.getColumns().addAll(colMa, colKhach, colSdt, colNgay, colTt);

        Button btnChon = new Button("Chọn phiếu này");
        btnChon.getStyleClass().add("nut-chinh");
        btnChon.setOnAction(e -> {
            PhieuDichVu p = bang.getSelectionModel().getSelectedItem();
            if (p != null) {
                // Chặn nạp trùng 1 phiếu dịch vụ vào 2 hóa đơn khác nhau
                if (new com.petshop.bus.PhieuDichVuBUS().kiemTraPhieuDichVuDaCoHoaDon(p.getMaPhieuDichVu())) {
                    hienThiCanhBao("Phiếu dịch vụ '" + p.getMaPhieuDichVu() + "' đã được lập hóa đơn thanh toán trước đó!");
                    return;
                }

                if (p.getMaKhachHang() != null && !p.getMaKhachHang().isBlank()) {
                    try {
                        KhachHang kh = khachHangBUS.layTheoMa(p.getMaKhachHang());
                        if (kh != null) {
                            dongBoThongTinKhachHang(kh);
                            txtTimKhach.setText(kh.getSoDienThoai());
                        }
                    } catch (Exception ignored) {}
                }
                napPhieuDichVuVaoHoaDon(p);
                dlg.close();
            } else {
                hienThiCanhBao("Vui lòng chọn một phiếu dịch vụ trong danh sách!");
            }
        });

        Button btnHuy = new Button("Đóng");
        btnHuy.getStyleClass().add("nut-phu");
        btnHuy.setOnAction(e -> dlg.close());

        HBox pnlNut = new HBox(8, btnChon, btnHuy);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        pnlGoc.getChildren().addAll(lblTieuDe, pnlTim, bang, pnlNut);
        Scene sc = new Scene(pnlGoc, 650, 420);
        if (LapHoaDonThanhToanView.class.getResource(CSS_CHUNG) != null) {
            sc.getStylesheets().add(LapHoaDonThanhToanView.class.getResource(CSS_CHUNG).toExternalForm());
        }
        dlg.setScene(sc);
        dlg.showAndWait();
    }

    /**
     * Lưu tạm giỏ hàng hiện tại vào hàng đợi đơn tạm (Hold Order).
     * Giải quyết tình huống khách chưa đủ tiền / đổi ý / cần thời gian chuyển khoản mà không làm nghẽn quầy POS.
     */
    private void xuLyLuuTamDonHang() {
        if (dsDongHang.isEmpty()) {
            hienThiCanhBao("Giỏ hàng đang trống, không có mặt hàng nào để lưu tạm!");
            return;
        }

        String maDon = "DT-" + String.format("%02d", demDonTam++);
        String tenKh = (khachHangHienTai != null) ? khachHangHienTai.getHoTen() : "Khách vãng lai";
        String sdt = (khachHangHienTai != null) ? khachHangHienTai.getSoDienThoai() : "";
        String maKm = (cboKhuyenMai.getValue() != null) ? cboKhuyenMai.getValue().getMaKhuyenMai() : null;
        int diem = (chkDungDiem.isSelected() && spnDiemDung.getValue() != null) ? spnDiemDung.getValue() : 0;
        String ghiChu = (khachHangHienTai != null ? "Khách: " + khachHangHienTai.getHoTen() : "Khách vãng lai");

        DonHangTam donTam = new DonHangTam(
                maDon,
                khachHangHienTai,
                tenKh,
                sdt,
                new ArrayList<>(dsDongHang),
                tongTienHangHienTai,
                tongThanhToanHienTai,
                maKm,
                diem,
                ghiChu,
                maPhieuDichVuDaNap
        );

        dsDonTam.add(donTam);
        capNhatNhanNutDonTam();

        // Làm mới form để sẵn sàng phục vụ khách tiếp theo
        lamMoiToanBoForm();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Lưu tạm đơn thành công");
        alert.setHeaderText("Đã lưu tạm đơn hàng [" + maDon + "]!");
        alert.setContentText("Toàn bộ " + donTam.getSoLuongMatHang() + " mặt hàng của '" + tenKh + "' đã được đưa vào danh sách chờ.\n"
                + "Quầy POS sẵn sàng phục vụ khách tiếp theo.\n"
                + "Khi khách quay lại, nhấn 'Đơn tạm (" + dsDonTam.size() + ")' hoặc phím F7 để khôi phục ngay.");
        alert.showAndWait();
    }

    private void capNhatNhanNutDonTam() {
        int sl = dsDonTam.size();
        btnDonTam.setText("📂 Đơn tạm (" + sl + ")");
        if (sl > 0) {
            btnDonTam.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-background-color: #0284c7; -fx-border-color: #0369a1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
        } else {
            btnDonTam.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: #0369a1; -fx-background-color: #e0f2fe; -fx-border-color: #38bdf8; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
        }
    }

    /**
     * Mở cửa sổ danh sách đơn hàng lưu tạm (Hold Orders) và hỗ trợ tải lại từ hóa đơn cũ / đã hủy.
     */
    public void moCuaSoDonHangTam() {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.initOwner(getScene() != null ? getScene().getWindow() : null);
        dlg.setTitle("Đơn hàng lưu tạm & Khôi phục hóa đơn POS - Happy Tails");

        VBox root = new VBox(12);
        root.setPadding(new Insets(16));
        root.getStyleClass().add("khung-phan-he");

        Label lblTieuDe = new Label("DANH SÁCH ĐƠN HÀNG LƯU TẠM (HOLD ORDERS)");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        Label lblHuongDan = new Label("Chọn đơn hàng tạm trong danh sách rồi nhấn 'Khôi phục vào POS' (hoặc click đúp) khi khách quay lại thanh toán.");
        lblHuongDan.getStyleClass().add("mo-ta-phan-he");

        // Bảng danh sách đơn hàng tạm
        TableView<DonHangTam> bangDonTam = new TableView<>();
        bangDonTam.getStyleClass().add("bang-du-lieu");
        bangDonTam.setItems(dsDonTam);
        bangDonTam.setPlaceholder(new Label("Hiện không có đơn hàng nào đang lưu tạm."));
        bangDonTam.setPrefHeight(260);

        TableColumn<DonHangTam, String> colMa = new TableColumn<>("Mã đơn tạm");
        colMa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaDonTam()));
        colMa.setPrefWidth(90);

        TableColumn<DonHangTam, String> colGio = new TableColumn<>("Giờ lưu");
        colGio.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getThoiGianLuuStr()));
        colGio.setPrefWidth(85);

        TableColumn<DonHangTam, String> colKhach = new TableColumn<>("Khách hàng");
        colKhach.setCellValueFactory(d -> {
            String sdt = d.getValue().getSoDienThoai();
            String ten = d.getValue().getTenKhachHang();
            return new SimpleStringProperty(ten + (sdt != null && !sdt.isBlank() ? " (" + sdt + ")" : ""));
        });
        colKhach.setPrefWidth(180);

        TableColumn<DonHangTam, Integer> colSoMon = new TableColumn<>("Số món");
        colSoMon.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSoLuongMatHang()));
        colSoMon.setPrefWidth(70);

        TableColumn<DonHangTam, String> colTongTien = new TableColumn<>("Tổng thanh toán");
        colTongTien.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().getTongThanhToan()) + " VNĐ"));
        colTongTien.setPrefWidth(130);
        colTongTien.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        TableColumn<DonHangTam, String> colGhiChu = new TableColumn<>("Ghi chú");
        colGhiChu.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGhiChu() != null ? d.getValue().getGhiChu() : ""));
        colGhiChu.setPrefWidth(140);

        bangDonTam.getColumns().addAll(colMa, colGio, colKhach, colSoMon, colTongTien, colGhiChu);

        // Hàng nút thao tác
        HBox pnlNut = new HBox(10);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        Button btnXoaDon = new Button("✕ Xóa đơn này");
        btnXoaDon.getStyleClass().add("nut-xoa");
        btnXoaDon.setDisable(true);

        Button btnXoaTatCa = new Button("✕ Xóa tất cả đơn tạm");
        btnXoaTatCa.getStyleClass().add("nut-phu");
        btnXoaTatCa.setStyle("-fx-text-fill: #dc2626;");
        btnXoaTatCa.setDisable(dsDonTam.isEmpty());

        Button btnDong = new Button("Đóng");
        btnDong.getStyleClass().add("nut-phu");
        btnDong.setOnAction(e -> dlg.close());

        Button btnKhoiPhuc = new Button("🛒 Khôi phục vào POS");
        btnKhoiPhuc.getStyleClass().add("nut-chinh");
        btnKhoiPhuc.setStyle("-fx-background-color: #16a34a; -fx-font-weight: bold;");
        btnKhoiPhuc.setDisable(true);

        bangDonTam.getSelectionModel().selectedItemProperty().addListener((obs, cu, moi) -> {
            boolean coChon = (moi != null);
            btnKhoiPhuc.setDisable(!coChon);
            btnXoaDon.setDisable(!coChon);
        });

        Runnable thucHienKhoiPhuc = () -> {
            DonHangTam chon = bangDonTam.getSelectionModel().getSelectedItem();
            if (chon == null) {
                hienThiCanhBao("Vui lòng chọn một đơn tạm để khôi phục!");
                return;
            }

            if (!dsDongHang.isEmpty()) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Xác nhận giỏ hàng");
                confirm.setHeaderText("Quầy POS đang có " + dsDongHang.size() + " mặt hàng.");
                confirm.setContentText("Bạn có muốn lưu tạm đơn hiện tại trước khi khôi phục đơn [" + chon.getMaDonTam() + "] không?");
                ButtonType btnLuu = new ButtonType("Lưu tạm đơn hiện tại");
                ButtonType btnBo = new ButtonType("Bỏ qua & Ghi đè");
                ButtonType btnHuy = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
                confirm.getButtonTypes().setAll(btnLuu, btnBo, btnHuy);

                Optional<ButtonType> luaChon = confirm.showAndWait();
                if (luaChon.isEmpty() || luaChon.get() == btnHuy) return;
                if (luaChon.get() == btnLuu) {
                    xuLyLuuTamDonHang();
                }
            }

            khoiPhucDonTam(chon);
            dsDonTam.remove(chon);
            capNhatNhanNutDonTam();
            dlg.close();

            txtMaVachSanPham.requestFocus();
        };

        btnKhoiPhuc.setOnAction(e -> thucHienKhoiPhuc.run());

        bangDonTam.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && bangDonTam.getSelectionModel().getSelectedItem() != null) {
                thucHienKhoiPhuc.run();
            }
        });

        btnXoaDon.setOnAction(e -> {
            DonHangTam chon = bangDonTam.getSelectionModel().getSelectedItem();
            if (chon != null) {
                dsDonTam.remove(chon);
                capNhatNhanNutDonTam();
                btnXoaTatCa.setDisable(dsDonTam.isEmpty());
            }
        });

        btnXoaTatCa.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc chắn muốn xóa toàn bộ " + dsDonTam.size() + " đơn tạm không?", ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText(null);
            if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                dsDonTam.clear();
                capNhatNhanNutDonTam();
                btnXoaTatCa.setDisable(true);
            }
        });

        Region spacerNut = new Region();
        HBox.setHgrow(spacerNut, Priority.ALWAYS);

        pnlNut.getChildren().addAll(btnXoaDon, btnXoaTatCa, spacerNut, btnDong, btnKhoiPhuc);

        root.getChildren().addAll(lblTieuDe, lblHuongDan, bangDonTam, pnlNut);

        Scene scene = new Scene(root, 720, 420);
        try {
            scene.getStylesheets().add(getClass().getResource(CSS_CHUNG).toExternalForm());
        } catch (Exception ignored) {}

        dlg.setScene(scene);
        dlg.showAndWait();
    }

    private void khoiPhucDonTam(DonHangTam donTam) {
        if (donTam == null) return;

        lamMoiToanBoForm();

        // 1. Phục hồi khách hàng
        if (donTam.getKhachHang() != null) {
            dongBoThongTinKhachHang(donTam.getKhachHang());
            txtTimKhach.setText(donTam.getKhachHang().getSoDienThoai());
        } else {
            datKhachVangLai();
        }

        // 2. Phục hồi mặt hàng
        for (DongHoaDon d : donTam.getDsMatHang()) {
            DongHoaDon copy = new DongHoaDon(
                    dsDongHang.size() + 1,
                    d.getLoai(),
                    d.getMaHang(),
                    d.getTenHang(),
                    d.getDonViTinh(),
                    d.getSoLuong(),
                    d.getDonGia(),
                    d.getGhiChu()
            );
            copy.setMaThuCung(d.getMaThuCung());
            copy.setTenThuCung(d.getTenThuCung());
            dsDongHang.add(copy);
        }

        // 3. Phục hồi Phiếu dịch vụ nếu có
        if (donTam.getMaPhieuDichVu() != null && !donTam.getMaPhieuDichVu().isBlank()) {
            try {
                PhieuDichVu p = controller.layPhieuDichVuDeThanhToan(donTam.getMaPhieuDichVu());
                if (p != null) {
                    napPhieuDichVuVaoHoaDon(p);
                }
            } catch (Exception ignored) {}
        }

        // 4. Phục hồi Khuyến mãi
        if (donTam.getMaKhuyenMai() != null) {
            for (KhuyenMai km : cboKhuyenMai.getItems()) {
                if (km != null && donTam.getMaKhuyenMai().equalsIgnoreCase(km.getMaKhuyenMai())) {
                    cboKhuyenMai.setValue(km);
                    break;
                }
            }
        }

        bangChiTiet.refresh();
        capNhatTongTienThanhToan();
    }

    public void napLaiHoaDonVaoPOS(HoaDon hd) {
        if (hd == null) return;

        lamMoiToanBoForm();

        // 1. Phục hồi thông tin khách hàng
        if (hd.getKhachHang() != null) {
            dongBoThongTinKhachHang(hd.getKhachHang());
            txtTimKhach.setText(hd.getKhachHang().getSoDienThoai());
        } else if (hd.getSoDienThoaiKhachHang() != null && !hd.getSoDienThoaiKhachHang().isBlank()) {
            KhachHang kh = controller.timKhachHangTheoSoDienThoai(hd.getSoDienThoaiKhachHang().trim());
            if (kh != null) {
                dongBoThongTinKhachHang(kh);
                txtTimKhach.setText(kh.getSoDienThoai());
            } else {
                datKhachVangLai();
            }
        } else {
            datKhachVangLai();
        }

        // 2. Nạp sản phẩm
        if (hd.getDanhSachSanPham() != null) {
            for (ChiTietHoaDonSanPham sp : hd.getDanhSachSanPham()) {
                DongHoaDon dong = new DongHoaDon(
                        dsDongHang.size() + 1,
                        DongHoaDon.LoaiDong.SAN_PHAM,
                        sp.getMaSanPham(),
                        sp.getTenSanPham() != null ? sp.getTenSanPham() : sp.getMaSanPham(),
                        sp.getDonViTinh() != null ? sp.getDonViTinh() : "Cái",
                        sp.getSoLuong(),
                        sp.getDonGia(),
                        ""
                );
                dsDongHang.add(dong);
            }
        }

        // 3. Nạp dịch vụ
        if (hd.getDanhSachDichVu() != null) {
            for (ChiTietHoaDonDichVu dv : hd.getDanhSachDichVu()) {
                DongHoaDon dong = new DongHoaDon(
                        dsDongHang.size() + 1,
                        DongHoaDon.LoaiDong.DICH_VU,
                        dv.getMaDichVu(),
                        dv.getTenDichVu() != null ? dv.getTenDichVu() : dv.getMaDichVu(),
                        "Lần",
                        dv.getSoLuong(),
                        dv.getDonGia(),
                        dv.getTenThuCung() != null ? "Thú cưng: " + dv.getTenThuCung() : ""
                );
                dong.setMaThuCung(dv.getMaThuCung());
                dong.setTenThuCung(dv.getTenThuCung());
                dsDongHang.add(dong);
            }
        }

        // 4. Khuyến mãi nếu có
        if (hd.getMaKhuyenMai() != null) {
            for (KhuyenMai km : cboKhuyenMai.getItems()) {
                if (km != null && hd.getMaKhuyenMai().equalsIgnoreCase(km.getMaKhuyenMai())) {
                    cboKhuyenMai.setValue(km);
                    break;
                }
            }
        }

        bangChiTiet.refresh();
        capNhatTongTienThanhToan();
    }

    // =========================================================================
    // TÍNH TOÁN TIỀN TỆ & ĐIỂM TÍCH LŨY (Điều 1 & 2 IUH: 100% double)
    // =========================================================================


    private void capNhatTongTienThanhToan() {
        if (dangCapNhatTien) return;
        dangCapNhatTien = true;
        try {
            // Tổng tiền hàng gốc tính qua tinhThanhTien() của từng dòng (Điều 2)
            tongTienHangHienTai = 0.0;
            for (DongHoaDon d : dsDongHang) {
                tongTienHangHienTai += d.tinhThanhTien();
            }
            lblTongTienHang.setText(DINH_DANG_TIEN.format(tongTienHangHienTai));

            // 1. Khuyến mãi (qua BUS)
            KhuyenMai km = cboKhuyenMai.getValue();
            if (km != null && tongTienHangHienTai > 0.0) {
                try {
                    tienGiamKMHienTai = controller.tinhSoTienGiam(km.getMaKhuyenMai(), tongTienHangHienTai);
                } catch (Exception ignored) {
                    tienGiamKMHienTai = 0.0;
                }
            } else {
                tienGiamKMHienTai = 0.0;
            }

            if (tienGiamKMHienTai > 0.0) {
                lblTienGiamGiaKM.setText("- " + DINH_DANG_TIEN.format(tienGiamKMHienTai));
            } else {
                lblTienGiamGiaKM.setText("- 0 VNĐ");
            }

            double tienSauKm = Math.max(0.0, tongTienHangHienTai - tienGiamKMHienTai);

            // 2. Điểm tích lũy (1đ = 1.000 VNĐ)
            boolean laKhachHang = (khachHangHienTai != null);
            if (laKhachHang && chkDungDiem.isSelected() && spnDiemDung.getValue() != null && spnDiemDung.getValue() > 0) {
                int diemDung = spnDiemDung.getValue();
                int diemToiDaCoTheDung = (int) Math.ceil(tienSauKm / 1000.0);
                int diemToiDaHopLe = Math.min(khachHangHienTai.getDiemTichLuy(), diemToiDaCoTheDung);
                if (diemDung > diemToiDaHopLe) {
                    diemDung = diemToiDaHopLe;
                    if (spnDiemDung.getValueFactory() != null) {
                        spnDiemDung.getValueFactory().setValue(diemDung);
                    }
                }
                double giaTriDiem = diemDung * 1000.0;
                tienGiamDiemHienTai = Math.min(tienSauKm, giaTriDiem);
                lblTienGiamTuDiem.setText("- " + DINH_DANG_TIEN.format(tienGiamDiemHienTai));
            } else {
                tienGiamDiemHienTai = 0.0;
                lblTienGiamTuDiem.setText("- 0 VNĐ");
            }

            // 3. Tổng thanh toán thực thu
            tongThanhToanHienTai = Math.max(0.0, tienSauKm - tienGiamDiemHienTai);
            lblTongThanhToan.setText(DINH_DANG_TIEN.format(tongThanhToanHienTai));

            // 4. Tính điểm tích lũy cộng mới
            if (laKhachHang) {
                diemCongMoiHienTai = controller.tinhDiemTichLuy(tongThanhToanHienTai, true);
                lblDiemCongDuKien.setText("Điểm nhận thêm: +" + diemCongMoiHienTai + " điểm");
                lblDiemCongDuKien.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #15803d;");
            } else {
                diemCongMoiHienTai = 0;
                lblDiemCongDuKien.setText("Khách vãng lai (không tích điểm)");
                lblDiemCongDuKien.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-style: italic;");
            }

            tinhToanTienThoi();
        } finally {
            dangCapNhatTien = false;
        }
    }

    private void tinhToanTienThoi() {
        if (!radTienMat.isSelected()) {
            lblTienThoiLai.setText("0 VNĐ");
            return;
        }

        String str = txtTienKhachDua.getText() != null ? txtTienKhachDua.getText().trim() : "";
        if (str.isEmpty()) {
            lblTienThoiLai.setText("0 VNĐ");
            return;
        }

        try {
            double tienDua = Double.parseDouble(str);
            double tienThoi = tienDua - tongThanhToanHienTai;
            if (tienThoi >= 0.0) {
                lblTienThoiLai.setText(DINH_DANG_TIEN.format(tienThoi));
                lblTienThoiLai.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #15803d;");
            } else {
                lblTienThoiLai.setText("Thiếu " + DINH_DANG_TIEN.format(-tienThoi));
                lblTienThoiLai.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #dc2626;");
            }
        } catch (NumberFormatException e) {
            lblTienThoiLai.setText("0 VNĐ");
        }
    }

    // =========================================================================
    // THANH TOÁN & IN HÓA ĐƠN
    // =========================================================================

    private void xuLyThanhToan(boolean inHoaDonSauThanhToan) {
        if (dsDongHang.isEmpty()) {
            hienThiCanhBao("Hóa đơn chưa có bất kỳ sản phẩm hoặc dịch vụ nào!");
            txtMaVachSanPham.requestFocus();
            return;
        }

        if (maPhieuDichVuDaNap != null && !maPhieuDichVuDaNap.isBlank()) {
            if (!"Hoàn thành".equalsIgnoreCase(trangThaiPhieuDichVuDaNap)) {
                hienThiCanhBao("Phiếu dịch vụ '" + maPhieuDichVuDaNap + "' chưa hoàn thành. Không thể thanh toán!");
                return;
            }
        }

        double tienDua = 0.0;
        double tienThoi = 0.0;

        if (radTienMat.isSelected()) {
            String strDua = txtTienKhachDua.getText() != null ? txtTienKhachDua.getText().trim() : "";
            if (strDua.isEmpty()) {
                if (tongThanhToanHienTai == 0.0) {
                    tienDua = 0.0;
                } else {
                    hienThiCanhBao("Vui lòng nhập số tiền khách đưa!");
                    txtTienKhachDua.requestFocus();
                    return;
                }
            } else {
                try {
                    tienDua = Double.parseDouble(strDua);
                    if (tienDua < tongThanhToanHienTai) {
                        hienThiCanhBao("Số tiền khách đưa không đủ để thanh toán!");
                        txtTienKhachDua.requestFocus();
                        return;
                    }
                } catch (NumberFormatException ex) {
                    hienThiCanhBao("Số tiền khách đưa không hợp lệ!");
                    txtTienKhachDua.requestFocus();
                    return;
                }
            }
            tienThoi = Math.max(0.0, tienDua - tongThanhToanHienTai);
        }

        // Tạo Entity HoaDon với Object References (Điều 2 & 5 IUH)
        HoaDon hd = new HoaDon();
        hd.setMaHoaDon(txtMaHoaDon.getText().trim());
        hd.setNgayLap(dpNgayLap.getValue());
        hd.setMaPhieuDichVu(maPhieuDichVuDaNap);
        hd.setMaLichHen(maLichHenDaNap);

        // Nhân viên thu ngân từ SessionManager
        hd.setNhanVien(nhanVienHienTai);
        if (nhanVienHienTai == null) {
            TaiKhoan tk = SessionManager.getTaiKhoanHienTai();
            hd.setMaNhanVien(tk != null ? tk.getMaNhanVien() : "NV-20261005-000001");
            hd.setTenNhanVien(tk != null ? tk.getTenDangNhap() : "Quản trị hệ thống");
        }

        // Khách hàng Object Reference
        hd.setKhachHang(khachHangHienTai);

        hd.setHinhThucThanhToan(radTienMat.isSelected() ? "Tiền mặt" : "Chuyển khoản");
        hd.setTrangThaiThanhToan("Đã thanh toán");

        // Khuyến mãi Object Reference
        KhuyenMai km = cboKhuyenMai.getValue();
        hd.setKhuyenMai(km);

        hd.setTienGiam(tienGiamKMHienTai);

        // Thiết lập điểm tích lũy
        if (khachHangHienTai != null && chkDungDiem.isSelected() && spnDiemDung.getValue() != null && spnDiemDung.getValue() > 0) {
            hd.setDiemTichLuySuDung(spnDiemDung.getValue());
            hd.setTienGiamTuDiem(tienGiamDiemHienTai);
        } else {
            hd.setDiemTichLuySuDung(0);
            hd.setTienGiamTuDiem(0.0);
        }

        hd.setDiemTichLuyCong(diemCongMoiHienTai);
        hd.setTienKhachDua(tienDua);
        hd.setTienThoiLai(tienThoi);

        // Nạp danh sách chi tiết (Điều 1: donGia kiểu double)
        for (DongHoaDon d : dsDongHang) {
            if (d.getLoai() == DongHoaDon.LoaiDong.SAN_PHAM) {
                hd.getDanhSachSanPham().add(new ChiTietHoaDonSanPham(
                        hd.getMaHoaDon(),
                        d.getMaHang(),
                        d.getTenHang(),
                        d.getDonViTinh(),
                        d.getSoLuong(),
                        d.getDonGia()
                ));
            } else {
                hd.getDanhSachDichVu().add(new ChiTietHoaDonDichVu(
                        hd.getMaHoaDon(),
                        d.getMaHang(),
                        d.getTenHang(),
                        d.getMaThuCung(),
                        d.getTenThuCung(),
                        d.getDonGia()
                ));
            }
        }

        try {
            if (controller.lapHoaDon(hd)) {
                StringBuilder msg = new StringBuilder("Đã thanh toán hóa đơn " + hd.getMaHoaDon() + " thành công!\n");
                msg.append("Tổng thanh toán: ").append(DINH_DANG_TIEN.format(hd.getTongTien())).append("\n");

                if (hd.getDiemTichLuySuDung() > 0) {
                    msg.append("Đã giảm trừ điểm: ").append(hd.getDiemTichLuySuDung()).append(" điểm (-").append(DINH_DANG_TIEN.format(hd.getTienGiamTuDiem())).append(")\n");
                }
                if (hd.getDiemTichLuyCong() > 0) {
                    msg.append("Điểm tích lũy nhận được: +").append(hd.getDiemTichLuyCong()).append(" điểm\n");
                }

                hienThiThongBao("Thanh toán thành công", msg.toString());

                if (inHoaDonSauThanhToan) {
                    new InHoaDonDialog(hd, (Stage) getScene().getWindow()).showAndWait();
                }

                lamMoiToanBoForm();
            }
        } catch (Exception ex) {
            hienThiLoi("Lỗi khi thanh toán hóa đơn: " + ex.getMessage());
        }
    }

    private void lamMoiToanBoForm() {
        txtMaHoaDon.setText(controller.taoMaHoaDonMoi());
        lblNoiDungCk.setText("Nội dung CK: " + txtMaHoaDon.getText());
        xoaPhieuDichVuDaNap(false);

        datKhachVangLai();

        txtMaVachSanPham.clear();
        cboKhuyenMai.setValue(null);

        chkDungDiem.setSelected(false);
        chkDungDiem.setDisable(true);
        spnDiemDung.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0, 0));
        spnDiemDung.setDisable(true);
        btnDungToiDaDiem.setDisable(true);

        txtTienKhachDua.clear();
        lblTienThoiLai.setText("0 VNĐ");

        dsDongHang.clear();
        capNhatTongTienThanhToan();

        Platform.runLater(txtMaVachSanPham::requestFocus);
    }

    private void hienThiCanhBao(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void hienThiThongBao(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setTitle(title != null ? title : "Thông báo");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void hienThiLoi(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    /**
     * Lớp lưu trữ thông tin đơn hàng tạm (Hold Order) trong phiên làm việc POS.
     * Tuân thủ Điều 1 và Điều 2 IUH: Kiểu double nguyên thủy, tham chiếu đối tượng.
     */
    public static class DonHangTam {
        private final String maDonTam;
        private final LocalTime thoiGianLuu;
        private final KhachHang khachHang;
        private final String tenKhachHang;
        private final String soDienThoai;
        private final List<DongHoaDon> dsMatHang;
        private final double tongTienHang;
        private final double tongThanhToan;
        private final String maKhuyenMai;
        private final int diemDung;
        private final String ghiChu;
        private final String maPhieuDichVu;

        public DonHangTam(String maDonTam, KhachHang khachHang, String tenKhachHang, String soDienThoai,
                          List<DongHoaDon> dsMatHang, double tongTienHang, double tongThanhToan,
                          String maKhuyenMai, int diemDung, String ghiChu, String maPhieuDichVu) {
            this.maDonTam = maDonTam;
            this.thoiGianLuu = LocalTime.now();
            this.khachHang = khachHang;
            this.tenKhachHang = tenKhachHang;
            this.soDienThoai = soDienThoai;
            this.dsMatHang = new ArrayList<>();
            for (DongHoaDon d : dsMatHang) {
                DongHoaDon copy = new DongHoaDon(
                        d.getStt(),
                        d.getLoai(),
                        d.getMaHang(),
                        d.getTenHang(),
                        d.getDonViTinh(),
                        d.getSoLuong(),
                        d.getDonGia(),
                        d.getGhiChu()
                );
                copy.setMaThuCung(d.getMaThuCung());
                copy.setTenThuCung(d.getTenThuCung());
                this.dsMatHang.add(copy);
            }
            this.tongTienHang = tongTienHang;
            this.tongThanhToan = tongThanhToan;
            this.maKhuyenMai = maKhuyenMai;
            this.diemDung = diemDung;
            this.ghiChu = ghiChu;
            this.maPhieuDichVu = maPhieuDichVu;
        }

        public String getMaDonTam() { return maDonTam; }
        public LocalTime getThoiGianLuu() { return thoiGianLuu; }
        public String getThoiGianLuuStr() { return thoiGianLuu.format(DateTimeFormatter.ofPattern("HH:mm:ss")); }
        public KhachHang getKhachHang() { return khachHang; }
        public String getTenKhachHang() { return tenKhachHang; }
        public String getSoDienThoai() { return soDienThoai; }
        public List<DongHoaDon> getDsMatHang() { return dsMatHang; }
        public int getSoLuongMatHang() { return dsMatHang.size(); }
        public double getTongTienHang() { return tongTienHang; }
        public double getTongThanhToan() { return tongThanhToan; }
        public String getMaKhuyenMai() { return maKhuyenMai; }
        public int getDiemDung() { return diemDung; }
        public String getGhiChu() { return ghiChu; }
        public String getMaPhieuDichVu() { return maPhieuDichVu; }
    }
}
