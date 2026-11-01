package com.petshop.view.hoadon;

import com.petshop.controller.HoaDonController;
import com.petshop.model.*;
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
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Giao diện Lập hóa đơn và thanh toán bán lẻ POS (INV_CREATE / HD_TAO).
 * - Tra cứu khách hàng theo SĐT (giống bên Lập phiếu dịch vụ): có thì tự động nạp, chưa có thì mở popup thêm mới.
 * - Quy tắc điểm tích lũy:
 *   + Hóa đơn < 1 triệu: 100k = 5 điểm; hóa đơn >= 1 triệu: 100k = 10 điểm.
 *   + 1 điểm tích lũy = giảm 1.000 VNĐ. Có ô chọn thanh toán kết hợp điểm tích lũy.
 *   + Khách vãng lai không tính điểm và không dùng điểm; tạo khách hàng mới mới tính điểm.
 * - Tìm kiếm sản phẩm nhanh theo mã/tên.
 * - Phương thức thanh toán: Tiền mặt và Chuyển khoản (đã bỏ POS).
 * - In hóa đơn hiển thị đầy đủ Tiền mặt, Tiền khách đưa, Tiền thối lại và Điểm tích lũy.
 */
public class LapHoaDonThanhToanView extends VBox {

    private static final String CSS_CHUNG = "/com/petshop/view/css/chung.css";
    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 VNĐ");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final HoaDonController controller;

    // Dữ liệu nguồn
    private final ObservableList<DongHoaDon> dsDongHang = FXCollections.observableArrayList();
    private final List<SanPham> dsSanPhamGoc = new ArrayList<>();
    private final ObservableList<SanPham> dsSanPhamHienThi = FXCollections.observableArrayList();
    private final List<KhachHang> dsKhachHangGoc = new ArrayList<>();
    private final ObservableList<KhachHang> dsKhachHangHienThi = FXCollections.observableArrayList();
    private final ObservableList<KhuyenMai> dsKhuyenMai = FXCollections.observableArrayList();

    // 1. Thông tin quầy & Hóa đơn
    private final TextField txtMaHoaDon = new TextField();
    private final DatePicker dpNgayLap = new DatePicker(LocalDate.now());
    private final TextField txtThuNgan = new TextField();

    // Tra cứu khách hàng theo SĐT (giống Lập phiếu dịch vụ)
    private final TextField txtTimKhach = new TextField();
    private final Button btnKiemTraSdt = new Button("Tra cứu SĐT");
    private final ComboBox<KhachHang> cboKhachHang = new ComboBox<>();
    private final Label lblThongTinDiemKhach = new Label("Khách vãng lai (không tích điểm)");
    private String maPhieuDichVuDaNap = null;
    private String maLichHenDaNap = null;
    private boolean dangDongBo = false;

    // 2. Tìm kiếm & chọn sản phẩm bán lẻ
    private final TextField txtTimSanPham = new TextField();
    private final ComboBox<SanPham> cboSanPham = new ComboBox<>();
    private final Label lblDonGiaSp = new Label("0 VNĐ");
    private final Label lblTonKhoSp = new Label("Tồn: 0");
    private final Spinner<Integer> spnSoLuongSp = new Spinner<>(1, 9999, 1);
    private final Button btnThemSp = new Button("+ Thêm sản phẩm");

    // 3. Bảng chi tiết bán hàng
    private final TableView<DongHoaDon> bangChiTiet = new TableView<>();

    // 4. Tóm tắt tiền tệ & Khuyến mãi & Điểm tích lũy
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
    private final Button btnThanhToanVaIn = new Button("🖨 Thanh toán & In");
    private final Button btnThanhToan = new Button("✔ Thanh toán");

    // Biến tính toán
    private BigDecimal tongTienHangHienTai = BigDecimal.ZERO;
    private BigDecimal tienGiamKMHienTai = BigDecimal.ZERO;
    private BigDecimal tienGiamDiemHienTai = BigDecimal.ZERO;
    private BigDecimal tongThanhToanHienTai = BigDecimal.ZERO;
    private int diemCongMoiHienTai = 0;
    private boolean dangCapNhatTien = false;

    public LapHoaDonThanhToanView() {
        this.controller = new HoaDonController();

        setSpacing(10);
        setPadding(new Insets(14));
        getStyleClass().add("khung-phan-he");

        khoiTaoGiaoDien();
        dangKySuKien();
        napDuLieuBanDau();
    }

    private void khoiTaoGiaoDien() {
        // Tiêu đề
        VBox pnlTieuDe = new VBox(3);
        Label lblTieuDe = new Label("LẬP HÓA ĐƠN & THANH TOÁN BÁN LẺ");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Thanh toán bán lẻ, nạp phiếu dịch vụ, tích điểm & đổi điểm tích lũy, in hóa đơn chuyên nghiệp.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // Khung thông tin quầy & Khách hàng
        VBox pnlThongTin = taoKhungThongTinQuay();

        // Khung chọn sản phẩm
        VBox pnlChonHang = taoKhungChonSanPham();

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

        txtThuNgan.setEditable(false);
        txtThuNgan.setPrefWidth(170);

        // Hàng 1: SĐT tra cứu (giống Lập phiếu dịch vụ)
        txtTimKhach.setPromptText("SĐT (10 số)...");
        txtTimKhach.setPrefWidth(150);
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

        cboKhachHang.setPrefWidth(210);
        cboKhachHang.setPromptText("-- Khách vãng lai --");

        Button btnBoChonKhach = new Button("✕ Bỏ");
        btnBoChonKhach.getStyleClass().add("nut-phu");
        btnBoChonKhach.setStyle("-fx-font-size: 11px;");
        btnBoChonKhach.setOnAction(e -> {
            txtTimKhach.clear();
            cboKhachHang.setValue(null);
            lblThongTinDiemKhach.setText("Khách vãng lai");
            lblThongTinDiemKhach.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic;");
            chkDungDiem.setSelected(false);
            chkDungDiem.setDisable(true);
            capNhatTongTienThanhToan();
        });

        Button btnNapPhieuDv = new Button("📥 Nạp từ Phiếu dịch vụ");
        btnNapPhieuDv.setStyle("-fx-background-color: #0284c7; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-cursor: hand;");
        btnNapPhieuDv.setOnAction(e -> moCuaSoChonPhieuDichVu());

        lblThongTinDiemKhach.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic;");

        Region spacerKh = new Region();
        HBox.setHgrow(spacerKh, Priority.ALWAYS);

        HBox hangKhachHang = new HBox(6, txtTimKhach, btnKiemTraSdt, cboKhachHang, btnBoChonKhach, lblThongTinDiemKhach, spacerKh, btnNapPhieuDv);
        hangKhachHang.setAlignment(Pos.CENTER_LEFT);

        // Grid bố trí
        luoi.add(new Label("Số HĐ:"), 0, 0);
        luoi.add(txtMaHoaDon, 1, 0);
        luoi.add(new Label("Ngày lập:"), 2, 0);
        luoi.add(dpNgayLap, 3, 0);
        luoi.add(new Label("Thu ngân:"), 4, 0);
        luoi.add(txtThuNgan, 5, 0);

        luoi.add(new Label("Khách hàng:"), 0, 1);
        luoi.add(hangKhachHang, 1, 1, 5, 1);

        pnl.getChildren().add(luoi);
        return pnl;
    }

    private VBox taoKhungChonSanPham() {
        VBox pnl = new VBox(6);
        pnl.setPadding(new Insets(8, 12, 8, 12));
        pnl.getStyleClass().add("hop-noi-dung");

        HBox hangChonSp = new HBox(10);
        hangChonSp.setAlignment(Pos.CENTER_LEFT);

        Label lblChonSp = new Label("Tìm & chọn sản phẩm:");
        lblChonSp.setStyle("-fx-font-weight: bold;");

        // Ô tìm kiếm sản phẩm nhanh
        txtTimSanPham.setPromptText("🔍 Gõ mã hoặc tên SP...");
        txtTimSanPham.setPrefWidth(180);

        cboSanPham.setPromptText("-- Chọn sản phẩm bán lẻ --");
        cboSanPham.setPrefWidth(260);

        lblDonGiaSp.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a; -fx-min-width: 90px;");
        lblTonKhoSp.setStyle("-fx-text-fill: #15803d; -fx-font-weight: bold; -fx-min-width: 65px;");

        spnSoLuongSp.setPrefWidth(75);
        spnSoLuongSp.setEditable(true);

        btnThemSp.getStyleClass().add("nut-chinh");
        btnThemSp.setOnAction(e -> themSanPhamVaoBang());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnXoaDong = new Button("✕ Xóa dòng");
        btnXoaDong.getStyleClass().add("nut-xoa");
        btnXoaDong.setOnAction(e -> xoaDongDangChon());

        hangChonSp.getChildren().addAll(lblChonSp, txtTimSanPham, cboSanPham, lblDonGiaSp, lblTonKhoSp, new Label("SL:"), spnSoLuongSp, btnThemSp, spacer, btnXoaDong);
        pnl.getChildren().add(hangChonSp);
        return pnl;
    }

    private void taoBangChiTiet() {
        bangChiTiet.getStyleClass().add("bang-du-lieu");
        bangChiTiet.setItems(dsDongHang);
        bangChiTiet.setPlaceholder(new Label("Chưa có mặt hàng hoặc dịch vụ nào trong hóa đơn. Vui lòng chọn sản phẩm hoặc nạp từ phiếu dịch vụ."));

        TableColumn<DongHoaDon, Integer> colStt = new TableColumn<>("STT");
        colStt.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getStt()));
        colStt.setPrefWidth(45);
        colStt.setStyle("-fx-alignment: center;");

        TableColumn<DongHoaDon, String> colLoai = new TableColumn<>("Phân loại");
        colLoai.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenLoai()));
        colLoai.setPrefWidth(90);
        colLoai.setStyle("-fx-alignment: center;");

        TableColumn<DongHoaDon, String> colMa = new TableColumn<>("Mã hàng");
        colMa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaHang()));
        colMa.setPrefWidth(120);

        TableColumn<DongHoaDon, String> colTen = new TableColumn<>("Tên mặt hàng / Dịch vụ");
        colTen.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenHang()));
        colTen.setPrefWidth(220);

        TableColumn<DongHoaDon, String> colDvt = new TableColumn<>("ĐVT");
        colDvt.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDonViTinh()));
        colDvt.setPrefWidth(60);
        colDvt.setStyle("-fx-alignment: center;");

        TableColumn<DongHoaDon, Integer> colSl = new TableColumn<>("Số lượng");
        colSl.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSoLuong()));
        colSl.setPrefWidth(70);
        colSl.setStyle("-fx-alignment: center-right;");

        TableColumn<DongHoaDon, String> colGia = new TableColumn<>("Đơn giá");
        colGia.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().getDonGia())));
        colGia.setPrefWidth(110);
        colGia.setStyle("-fx-alignment: center-right;");

        TableColumn<DongHoaDon, String> colThanhTien = new TableColumn<>("Thành tiền");
        colThanhTien.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().getThanhTien())));
        colThanhTien.setPrefWidth(130);
        colThanhTien.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");

        TableColumn<DongHoaDon, String> colGhiChu = new TableColumn<>("Ghi chú");
        colGhiChu.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGhiChu()));
        colGhiChu.setPrefWidth(150);

        bangChiTiet.getColumns().addAll(colStt, colLoai, colMa, colTen, colDvt, colSl, colGia, colThanhTien, colGhiChu);
    }

    private HBox taoKhungThanhToanDay() {
        HBox pnl = new HBox(14);
        pnl.setPadding(new Insets(10, 12, 10, 12));
        pnl.getStyleClass().add("hop-noi-dung");
        pnl.setAlignment(Pos.CENTER_LEFT);

        // Cột 1: Tiền hàng, Khuyến mãi, Điểm tích lũy, Tổng thanh toán
        VBox cotTien = new VBox(4);
        cotTien.setMinWidth(420);
        cotTien.setPrefWidth(420);

        GridPane luoiTien = new GridPane();
        luoiTien.setHgap(8);
        luoiTien.setVgap(4);

        luoiTien.add(new Label("Tiền hàng:"), 0, 0);
        lblTongTienHang.setStyle("-fx-font-weight: bold;");
        luoiTien.add(lblTongTienHang, 1, 0);

        cboKhuyenMai.setPromptText("-- Không áp dụng --");
        cboKhuyenMai.setPrefWidth(210);
        luoiTien.add(new Label("Khuyến mãi:"), 0, 1);
        luoiTien.add(cboKhuyenMai, 1, 1);

        luoiTien.add(new Label("Tiền giảm KM:"), 0, 2);
        lblTienGiamGiaKM.setStyle("-fx-font-weight: bold; -fx-text-fill: #dc2626;");
        luoiTien.add(lblTienGiamGiaKM, 1, 2);

        // Thanh toán kết hợp điểm tích lũy
        chkDungDiem.setDisable(true); // Chỉ bật khi có khách hàng
        chkDungDiem.setStyle("-fx-font-size: 11px;");

        spnDiemDung.setPrefWidth(85);
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
        btnDungToiDaDiem.setDisable(true);

        HBox hangDiem = new HBox(4, spnDiemDung, btnDungToiDaDiem, lblTienGiamTuDiem);
        hangDiem.setAlignment(Pos.CENTER_LEFT);
        lblTienGiamTuDiem.setStyle("-fx-font-weight: bold; -fx-text-fill: #dc2626;");

        luoiTien.add(chkDungDiem, 0, 3, 2, 1);
        luoiTien.add(new Label("Điểm sử dụng:"), 0, 4);
        luoiTien.add(hangDiem, 1, 4);

        Label lblTieuDeTt = new Label("THANH TOÁN:");
        lblTieuDeTt.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        lblTongThanhToan.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0284c7;");
        luoiTien.add(lblTieuDeTt, 0, 5);
        luoiTien.add(lblTongThanhToan, 1, 5);

        lblDiemCongDuKien.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-style: italic;");
        luoiTien.add(lblDiemCongDuKien, 0, 6, 2, 1);

        cotTien.getChildren().add(luoiTien);

        // Cột 2: Hình thức thanh toán (chỉ Tiền mặt và Chuyển khoản, ĐÃ BỎ POS)
        VBox cotHinhThuc = new VBox(6);
        cotHinhThuc.setPrefWidth(300);

        radTienMat.setToggleGroup(grpPhuongThuc);
        radChuyenKhoan.setToggleGroup(grpPhuongThuc);
        radTienMat.setSelected(true);

        HBox hangRadio = new HBox(16, radTienMat, radChuyenKhoan);
        hangRadio.setAlignment(Pos.CENTER_LEFT);

        // Box Tiền mặt
        GridPane luoiTienMat = new GridPane();
        luoiTienMat.setHgap(8);
        luoiTienMat.setVgap(4);
        txtTienKhachDua.setPromptText("Nhập tiền khách đưa");
        txtTienKhachDua.setPrefWidth(120);
        txtTienKhachDua.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d*") ? change : null));

        Button btnDuTien = new Button("Đủ tiền");
        btnDuTien.setStyle("-fx-font-size: 11px; -fx-padding: 2 6;");
        btnDuTien.setOnAction(e -> {
            txtTienKhachDua.setText(String.valueOf(tongThanhToanHienTai.longValue()));
            tinhToanTienThoi();
        });

        HBox hangKhachDua = new HBox(4, txtTienKhachDua, btnDuTien);
        hangKhachDua.setAlignment(Pos.CENTER_LEFT);

        lblTienThoiLai.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #15803d;");

        luoiTienMat.add(new Label("Tiền khách đưa:"), 0, 0);
        luoiTienMat.add(hangKhachDua, 1, 0);
        luoiTienMat.add(new Label("Tiền thối lại:"), 0, 1);
        luoiTienMat.add(lblTienThoiLai, 1, 1);

        boxTienMat.getChildren().add(luoiTienMat);

        // Box Chuyển khoản
        boxChuyenKhoan.setStyle("-fx-background-color: #f0fdf4; -fx-padding: 8; -fx-border-color: #bbf7d0; -fx-border-radius: 4; -fx-background-radius: 4;");
        Label lblTk = new Label("STK: 0909000000 (MB Bank) - PET SHOP HAPPY TAILS");
        lblTk.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #166534;");
        lblNoiDungCk.setStyle("-fx-font-size: 11px; -fx-text-fill: #15803d;");
        lblNoiDungCk.setText("Nội dung CK: " + txtMaHoaDon.getText());
        boxChuyenKhoan.getChildren().addAll(lblTk, lblNoiDungCk);
        boxChuyenKhoan.setVisible(false);
        boxChuyenKhoan.setManaged(false);

        cotHinhThuc.getChildren().addAll(new Label("Hình thức thanh toán:"), hangRadio, boxTienMat, boxChuyenKhoan);

        // Cột 3: Nút bấm
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox cotNut = new VBox(8);
        cotNut.setAlignment(Pos.CENTER_RIGHT);

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> lamMoiToanBoForm());

        btnThanhToanVaIn.getStyleClass().add("nut-chinh");
        btnThanhToanVaIn.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-pref-height: 38px;");
        btnThanhToanVaIn.setOnAction(e -> xuLyThanhToan(true));

        btnThanhToan.getStyleClass().add("nut-chinh");
        btnThanhToan.setStyle("-fx-background-color: #16a34a; -fx-font-weight: bold; -fx-font-size: 13px; -fx-pref-height: 38px;");
        btnThanhToan.setOnAction(e -> xuLyThanhToan(false));

        HBox hangNut = new HBox(8, btnLamMoi, btnThanhToan, btnThanhToanVaIn);
        hangNut.setAlignment(Pos.CENTER_RIGHT);
        cotNut.getChildren().add(hangNut);

        pnl.getChildren().addAll(cotTien, new Separator(javafx.geometry.Orientation.VERTICAL), cotHinhThuc, spacer, cotNut);
        return pnl;
    }

    private void dangKySuKien() {
        // 1. Tìm kiếm khách hàng theo SĐT (khi gõ Enter hoặc bấm nút Kiểm tra)
        txtTimKhach.setOnAction(e -> xuLyKiemTraSoDienThoai());
        btnKiemTraSdt.setOnAction(e -> xuLyKiemTraSoDienThoai());

        // Tự động kiểm tra khách hàng khi gõ đủ 10 số hoặc làm mới khi xóa
        txtTimKhach.textProperty().addListener((obs, cu, moi) -> {
            if (dangDongBo || moi == null) return;
            if (moi.trim().isEmpty()) {
                cboKhachHang.setValue(null);
                lblThongTinDiemKhach.setText("Khách vãng lai");
                lblThongTinDiemKhach.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic;");
                chkDungDiem.setSelected(false);
                chkDungDiem.setDisable(true);
                capNhatTongTienThanhToan();
            } else if (moi.length() == 10) {
                KhachHang khKhop = null;
                for (KhachHang kh : dsKhachHangGoc) {
                    if (moi.equals(kh.getSoDienThoai())) {
                        khKhop = kh;
                        break;
                    }
                }
                if (khKhop != null) {
                    dongBoThongTinKhachHang(khKhop);
                }
            }
        });

        // Thay đổi chọn khách hàng trên ComboBox
        cboKhachHang.setOnAction(e -> {
            if (dangDongBo) return;
            KhachHang kh = cboKhachHang.getValue();
            dongBoThongTinKhachHang(kh);
        });

        // 2. Tìm kiếm sản phẩm nhanh theo mã hoặc tên
        txtTimSanPham.textProperty().addListener((obs, cu, moi) -> {
            String tuKhoa = moi != null ? moi.trim().toLowerCase() : "";
            if (tuKhoa.isEmpty()) {
                dsSanPhamHienThi.setAll(dsSanPhamGoc);
            } else {
                List<SanPham> loc = dsSanPhamGoc.stream()
                        .filter(s -> (s.getTenSanPham() != null && s.getTenSanPham().toLowerCase().contains(tuKhoa))
                                || (s.getMaSanPham() != null && s.getMaSanPham().toLowerCase().contains(tuKhoa)))
                        .toList();
                dsSanPhamHienThi.setAll(loc);
                if (!loc.isEmpty()) {
                    cboSanPham.setValue(loc.get(0));
                }
            }
        });

        // Khi chọn sản phẩm trong ComboBox
        cboSanPham.setOnAction(e -> {
            SanPham sp = cboSanPham.getValue();
            if (sp != null) {
                lblDonGiaSp.setText(DINH_DANG_TIEN.format(sp.getGiaBan()));
                int tonKho = controller.traCuuTonKho(sp.getMaSanPham());
                lblTonKhoSp.setText("Tồn: " + tonKho);
                if (tonKho <= 0) {
                    lblTonKhoSp.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                } else {
                    lblTonKhoSp.setStyle("-fx-text-fill: #15803d; -fx-font-weight: bold;");
                }
            } else {
                lblDonGiaSp.setText("0 VNĐ");
                lblTonKhoSp.setText("Tồn: 0");
            }
        });

        // 3. Khuyến mãi
        cboKhuyenMai.setOnAction(e -> capNhatTongTienThanhToan());

        // 4. Điểm tích lũy
        chkDungDiem.setOnAction(e -> {
            boolean dung = chkDungDiem.isSelected();
            spnDiemDung.setDisable(!dung);
            btnDungToiDaDiem.setDisable(!dung);
            if (!dung) {
                spnDiemDung.getValueFactory().setValue(0);
            } else {
                dungToiDaDiem();
            }
            capNhatTongTienThanhToan();
        });

        spnDiemDung.valueProperty().addListener((obs, cu, moi) -> capNhatTongTienThanhToan());
        btnDungToiDaDiem.setOnAction(e -> dungToiDaDiem());

        // 5. Hình thức thanh toán
        grpPhuongThuc.selectedToggleProperty().addListener((obs, cu, moi) -> {
            if (radTienMat.isSelected()) {
                boxTienMat.setVisible(true);
                boxTienMat.setManaged(true);
                boxChuyenKhoan.setVisible(false);
                boxChuyenKhoan.setManaged(false);
            } else {
                boxTienMat.setVisible(false);
                boxTienMat.setManaged(false);
                boxChuyenKhoan.setVisible(true);
                boxChuyenKhoan.setManaged(true);
                lblNoiDungCk.setText("Nội dung CK: " + txtMaHoaDon.getText() + " " + (txtTimKhach.getText() != null ? txtTimKhach.getText().trim() : ""));
            }
        });

        // Nhập tiền khách đưa
        txtTienKhachDua.textProperty().addListener((obs, cu, moi) -> tinhToanTienThoi());
    }

    private void napDuLieuBanDau() {
        txtMaHoaDon.setText(controller.taoMaHoaDonMoi());
        lblNoiDungCk.setText("Nội dung CK: " + txtMaHoaDon.getText());

        var tkHienTai = SessionManager.getTaiKhoanHienTai();
        if (tkHienTai != null) {
            txtThuNgan.setText(tkHienTai.getTenDangNhap() + " (" + tkHienTai.getMaNhanVien() + ")");
        } else {
            txtThuNgan.setText("admin (NV-20261005-000001)");
        }

        // Khách hàng
        try {
            dsKhachHangGoc.clear();
            dsKhachHangGoc.addAll(controller.layDanhSachKhachHang());
            dsKhachHangHienThi.setAll(dsKhachHangGoc);
            cboKhachHang.setItems(dsKhachHangHienThi);
            cboKhachHang.setConverter(new javafx.util.StringConverter<>() {
                @Override public String toString(KhachHang kh) {
                    return kh != null ? kh.getHoTen() + " (" + kh.getSoDienThoai() + ") - Điểm: " + kh.getDiemTichLuy() : "-- Khách vãng lai --";
                }
                @Override public KhachHang fromString(String s) { return null; }
            });
        } catch (Exception ignored) {}

        // Sản phẩm
        try {
            dsSanPhamGoc.clear();
            dsSanPhamGoc.addAll(controller.layDanhSachSanPham());
            dsSanPhamHienThi.setAll(dsSanPhamGoc);
            cboSanPham.setItems(dsSanPhamHienThi);
            cboSanPham.setConverter(new javafx.util.StringConverter<>() {
                @Override public String toString(SanPham sp) {
                    return sp != null ? sp.getTenSanPham() + " [" + sp.getMaSanPham() + "]" : "";
                }
                @Override public SanPham fromString(String s) { return null; }
            });
        } catch (Exception ignored) {}

        // Khuyến mãi
        try {
            dsKhuyenMai.clear();
            dsKhuyenMai.add(null); // Ô trống: Không áp dụng khuyến mãi
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

    // =========================================================================
    // NGHIỆP VỤ TRA CỨU KHÁCH HÀNG & THÊM MỚI (GIỐNG LẬP PHIẾU DỊCH VỤ)
    // =========================================================================

    private void xuLyKiemTraSoDienThoai() {
        String sdt = txtTimKhach.getText() != null ? txtTimKhach.getText().trim() : "";
        if (sdt.isEmpty()) {
            hienThiCanhBao("Vui lòng nhập số điện thoại khách hàng cần tra cứu!");
            txtTimKhach.requestFocus();
            return;
        }

        KhachHang khKhop = null;
        for (KhachHang kh : dsKhachHangGoc) {
            if (sdt.equals(kh.getSoDienThoai())) {
                khKhop = kh;
                break;
            }
        }

        if (khKhop != null) {
            dongBoThongTinKhachHang(khKhop);
            cboKhachHang.requestFocus();
        } else {
            if (sdt.matches("^0[0-9]{9}$")) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Khách hàng mới");
                alert.setHeaderText("Khách hàng chưa có trong hệ thống!");
                alert.setContentText("Số điện thoại '" + sdt + "' chưa từng mua hàng/dịch vụ tại cửa hàng.\nHệ thống sẽ mở hộp thoại thêm nhanh khách hàng mới.");
                alert.showAndWait();
                moCuaSoThemKhachNhanh(sdt);
            } else {
                hienThiCanhBao("Số điện thoại phải gồm đúng 10 chữ số và bắt đầu bằng số 0 (VD: 0912345678)!");
                txtTimKhach.requestFocus();
            }
        }
    }

    private void dongBoThongTinKhachHang(KhachHang kh) {
        if (kh == null) {
            lblThongTinDiemKhach.setText("Khách vãng lai");
            lblThongTinDiemKhach.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic;");
            chkDungDiem.setSelected(false);
            chkDungDiem.setDisable(true);
            spnDiemDung.setDisable(true);
            btnDungToiDaDiem.setDisable(true);
            capNhatTongTienThanhToan();
            return;
        }

        dangDongBo = true;
        try {
            cboKhachHang.setValue(kh);
            if (kh.getSoDienThoai() != null && !kh.getSoDienThoai().equals(txtTimKhach.getText())) {
                txtTimKhach.setText(kh.getSoDienThoai());
            }
        } finally {
            dangDongBo = false;
        }

        lblThongTinDiemKhach.setText("Điểm: " + kh.getDiemTichLuy() + " đ");
        lblThongTinDiemKhach.setStyle("-fx-text-fill: #15803d; -fx-font-weight: bold;");

        // Cập nhật cấu hình spinner dùng điểm
        int diemKhachCo = kh.getDiemTichLuy();
        if (diemKhachCo > 0) {
            chkDungDiem.setDisable(false);
            chkDungDiem.setText("Dùng điểm tích lũy (Có: " + diemKhachCo + " đ)");
            if (spnDiemDung.getValueFactory() instanceof SpinnerValueFactory.IntegerSpinnerValueFactory fac) {
                fac.setMax(diemKhachCo);
                fac.setValue(0);
            } else {
                spnDiemDung.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, diemKhachCo, 0));
            }
        } else {
            chkDungDiem.setSelected(false);
            chkDungDiem.setDisable(true);
            chkDungDiem.setText("Dùng điểm tích lũy (Có: 0 đ)");
            if (spnDiemDung.getValueFactory() instanceof SpinnerValueFactory.IntegerSpinnerValueFactory fac) {
                fac.setMax(0);
                fac.setValue(0);
            } else {
                spnDiemDung.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0, 0));
            }
            spnDiemDung.setDisable(true);
            btnDungToiDaDiem.setDisable(true);
        }

        capNhatTongTienThanhToan();
    }

    private void moCuaSoThemKhachNhanh(String sdtBanDau) {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        if (getScene() != null && getScene().getWindow() != null) dlg.initOwner(getScene().getWindow());
        dlg.setTitle("Thêm nhanh khách hàng mới");

        VBox pnlGoc = new VBox(12);
        pnlGoc.setPadding(new Insets(16));
        pnlGoc.getStyleClass().add("khung-phan-he");

        Label lblTieuDe = new Label("THÊM NHANH KHÁCH HÀNG MỚI");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        GridPane luoi = new GridPane();
        luoi.setHgap(12);
        luoi.setVgap(10);

        TextField txtHoTenMoi = new TextField();
        txtHoTenMoi.setPromptText("Họ và tên khách hàng (*)");

        String sdtBanDauHopLe = sdtBanDau != null ? sdtBanDau.replaceAll("[^0-9]", "") : "";
        if (sdtBanDauHopLe.length() > 10) {
            sdtBanDauHopLe = sdtBanDauHopLe.substring(0, 10);
        }
        TextField txtSdtMoi = new TextField(sdtBanDauHopLe);
        txtSdtMoi.setPromptText("Số điện thoại (10 chữ số) (*)");
        txtSdtMoi.setTextFormatter(new TextFormatter<String>(change -> {
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

        TextField txtDiaChiMoi = new TextField();
        txtDiaChiMoi.setPromptText("Địa chỉ khách hàng (tùy chọn)");

        luoi.add(new Label("Họ và tên (*):"), 0, 0); luoi.add(txtHoTenMoi, 1, 0);
        luoi.add(new Label("Số điện thoại (*):"), 0, 1); luoi.add(txtSdtMoi, 1, 1);
        luoi.add(new Label("Địa chỉ:"), 0, 2); luoi.add(txtDiaChiMoi, 1, 2);

        Button btnLuuKhach = new Button("Lưu khách hàng");
        btnLuuKhach.getStyleClass().add("nut-chinh");
        Button btnHuyDlg = new Button("Hủy");
        btnHuyDlg.getStyleClass().add("nut-phu");
        btnHuyDlg.setOnAction(e -> dlg.close());

        btnLuuKhach.setOnAction(e -> {
            String ten = txtHoTenMoi.getText() != null ? txtHoTenMoi.getText().trim() : "";
            String sdt = txtSdtMoi.getText() != null ? txtSdtMoi.getText().trim() : "";

            if (ten.isEmpty() || !sdt.matches("^0[0-9]{9}$")) {
                hienThiCanhBao("Họ tên không được rỗng và Số điện thoại phải đúng 10 chữ số bắt đầu bằng 0!");
                return;
            }

            try {
                com.petshop.service.KhachHangService khService = new com.petshop.service.KhachHangService();
                String maKhMoi = khService.taoMaKhachHangMoi();
                KhachHang khMoi = new KhachHang(maKhMoi, ten, sdt, null, 0, txtDiaChiMoi.getText(), "Khách mua tại quầy", true);

                if (khService.themKhachHang(khMoi)) {
                    dsKhachHangGoc.add(0, khMoi);
                    dsKhachHangHienThi.setAll(dsKhachHangGoc);
                    dongBoThongTinKhachHang(khMoi);

                    dlg.close();
                    hienThiThongBao("Thành công", "Đã thêm mới khách hàng '" + ten + "' (" + sdt + ") thành công!\nTừ đơn hàng này khách hàng sẽ bắt đầu được tích lũy điểm.");
                }
            } catch (Exception ex) {
                hienThiLoi("Lỗi khi thêm khách hàng: " + ex.getMessage());
            }
        });

        HBox pnlNut = new HBox(8, btnLuuKhach, btnHuyDlg);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        pnlGoc.getChildren().addAll(lblTieuDe, luoi, pnlNut);
        Scene scene = new Scene(pnlGoc, 450, 230);
        if (LapHoaDonThanhToanView.class.getResource(CSS_CHUNG) != null) {
            scene.getStylesheets().add(LapHoaDonThanhToanView.class.getResource(CSS_CHUNG).toExternalForm());
        }
        dlg.setScene(scene);
        dlg.showAndWait();
    }

    // =========================================================================
    // NGHIỆP VỤ BÁN HÀNG & TÍNH TIỀN & ĐIỂM TÍCH LŨY
    // =========================================================================

    private void themSanPhamVaoBang() {
        SanPham sp = cboSanPham.getValue();
        if (sp == null) {
            hienThiCanhBao("Vui lòng chọn sản phẩm cần thêm!");
            return;
        }

        int sl = spnSoLuongSp.getValue();
        int tonKho = controller.traCuuTonKho(sp.getMaSanPham());

        int slDaChon = 0;
        for (DongHoaDon d : dsDongHang) {
            if (d.getLoai() == DongHoaDon.LoaiDong.SAN_PHAM && sp.getMaSanPham().equals(d.getMaHang())) {
                slDaChon += d.getSoLuong();
            }
        }

        if (slDaChon + sl > tonKho) {
            hienThiCanhBao("Số lượng yêu cầu (" + (slDaChon + sl) + ") vượt quá tồn kho khả dụng (" + tonKho + ") của sản phẩm '" + sp.getTenSanPham() + "'!");
            return;
        }

        boolean daCo = false;
        for (DongHoaDon d : dsDongHang) {
            if (d.getLoai() == DongHoaDon.LoaiDong.SAN_PHAM && sp.getMaSanPham().equals(d.getMaHang())) {
                d.setSoLuong(d.getSoLuong() + sl);
                daCo = true;
                break;
            }
        }

        if (!daCo) {
            DongHoaDon dongMoi = DongHoaDon.tuSanPham(dsDongHang.size() + 1, sp, sl);
            dsDongHang.add(dongMoi);
        }

        bangChiTiet.refresh();
        capNhatTongTienThanhToan();
    }

    private void xoaDongDangChon() {
        DongHoaDon dongChon = bangChiTiet.getSelectionModel().getSelectedItem();
        if (dongChon == null) {
            hienThiCanhBao("Vui lòng chọn một dòng trên bảng để xóa!");
            return;
        }

        dsDongHang.remove(dongChon);
        for (int i = 0; i < dsDongHang.size(); i++) {
            dsDongHang.get(i).setStt(i + 1);
        }
        bangChiTiet.refresh();
        capNhatTongTienThanhToan();
    }

    private void dungToiDaDiem() {
        KhachHang kh = cboKhachHang.getValue();
        if (kh == null || kh.getDiemTichLuy() <= 0) return;

        BigDecimal tienSauKm = tongTienHangHienTai.subtract(tienGiamKMHienTai).max(BigDecimal.ZERO);
        // Làm tròn lên để có thể dùng điểm lấn át toàn bộ tiền thanh toán về 0đ
        int diemToiDaCoTheDungTheoTien = (int) Math.ceil(tienSauKm.doubleValue() / 1000.0);
        int diemDung = Math.min(kh.getDiemTichLuy(), diemToiDaCoTheDungTheoTien);

        if (spnDiemDung.getValueFactory() instanceof SpinnerValueFactory.IntegerSpinnerValueFactory fac) {
            fac.setMax(kh.getDiemTichLuy());
            fac.setValue(diemDung);
        } else {
            spnDiemDung.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, kh.getDiemTichLuy(), diemDung));
        }
        capNhatTongTienThanhToan();
    }

    private BigDecimal tinhTienGiamKhuyenMaiNhanh(KhuyenMai km, BigDecimal tongTien) {
        if (km == null || tongTien == null || tongTien.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        LocalDate homNay = LocalDate.now();
        if (!km.isTrangThai() || (km.getNgayBatDau() != null && homNay.isBefore(km.getNgayBatDau()))
                || (km.getNgayKetThuc() != null && homNay.isAfter(km.getNgayKetThuc()))) {
            return BigDecimal.ZERO;
        }
        if (km.getPhanTramGiam() != null) {
            return tongTien.multiply(km.getPhanTramGiam()).divide(new BigDecimal("100"));
        }
        return BigDecimal.ZERO;
    }

    private void capNhatTongTienThanhToan() {
        if (dangCapNhatTien) return;
        dangCapNhatTien = true;
        try {
            tongTienHangHienTai = BigDecimal.ZERO;
            for (DongHoaDon d : dsDongHang) {
                tongTienHangHienTai = tongTienHangHienTai.add(d.getThanhTien());
            }
            lblTongTienHang.setText(DINH_DANG_TIEN.format(tongTienHangHienTai));

            // 1. Khuyến mãi (tính nhanh in-memory, tránh nghẽn truy vấn SQL trên luồng UI)
            KhuyenMai km = cboKhuyenMai.getValue();
            tienGiamKMHienTai = tinhTienGiamKhuyenMaiNhanh(km, tongTienHangHienTai);
            if (tienGiamKMHienTai.compareTo(BigDecimal.ZERO) > 0) {
                lblTienGiamGiaKM.setText("- " + DINH_DANG_TIEN.format(tienGiamKMHienTai));
            } else {
                lblTienGiamGiaKM.setText("- 0 VNĐ");
            }

            BigDecimal tienSauKm = tongTienHangHienTai.subtract(tienGiamKMHienTai).max(BigDecimal.ZERO);

            // 2. Điểm tích lũy (1đ = 1.000 VNĐ, có thể lấn át toàn bộ tiền về 0đ)
            KhachHang kh = cboKhachHang.getValue();
            boolean laKhachHang = (kh != null);

            if (laKhachHang && chkDungDiem.isSelected() && spnDiemDung.getValue() != null && spnDiemDung.getValue() > 0) {
                int diemDung = spnDiemDung.getValue();
                // Làm tròn lên để có thể dùng điểm lấn át toàn bộ tiền thanh toán về 0đ
                int diemToiDaCoTheDung = (int) Math.ceil(tienSauKm.doubleValue() / 1000.0);
                int diemToiDaHopLe = Math.min(kh.getDiemTichLuy(), diemToiDaCoTheDung);
                if (diemDung > diemToiDaHopLe) {
                    diemDung = diemToiDaHopLe;
                    if (spnDiemDung.getValueFactory() != null) {
                        spnDiemDung.getValueFactory().setValue(diemDung);
                    }
                }
                BigDecimal giaTriDiem = BigDecimal.valueOf(diemDung * 1000L);
                tienGiamDiemHienTai = tienSauKm.min(giaTriDiem);
                lblTienGiamTuDiem.setText("- " + DINH_DANG_TIEN.format(tienGiamDiemHienTai));
            } else {
                tienGiamDiemHienTai = BigDecimal.ZERO;
                lblTienGiamTuDiem.setText("- 0 VNĐ");
            }

            // 3. Tổng thanh toán thực thu
            tongThanhToanHienTai = tienSauKm.subtract(tienGiamDiemHienTai).max(BigDecimal.ZERO);
            lblTongThanhToan.setText(DINH_DANG_TIEN.format(tongThanhToanHienTai));

            // 4. Tính điểm tích lũy cộng mới từ đơn này:
            // Rule: Hóa đơn < 1tr: 100k = 5 điểm; Hóa đơn >= 1tr: 100k = 10 điểm
            // Khách vãng lai: 0 điểm
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
            BigDecimal tienDua = new BigDecimal(str);
            BigDecimal tienThoi = tienDua.subtract(tongThanhToanHienTai);
            if (tienThoi.compareTo(BigDecimal.ZERO) >= 0) {
                lblTienThoiLai.setText(DINH_DANG_TIEN.format(tienThoi));
                lblTienThoiLai.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #15803d;");
            } else {
                lblTienThoiLai.setText("Còn thiếu: " + DINH_DANG_TIEN.format(tienThoi.abs()));
                lblTienThoiLai.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #dc2626;");
            }
        } catch (Exception ex) {
            lblTienThoiLai.setText("Số tiền không hợp lệ");
            lblTienThoiLai.setStyle("-fx-text-fill: #dc2626;");
        }
    }

    private void moCuaSoChonPhieuDichVu() {
        List<PhieuDichVu> dsPhieu = controller.layDanhSachPhieuDichVuChuaThanhToan();
        if (dsPhieu.isEmpty()) {
            hienThiThongBao("Thông báo", "Hiện không có phiếu dịch vụ nào đang chờ thanh toán!");
            return;
        }

        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Chọn phiếu dịch vụ để nạp vào hóa đơn");

        VBox pnl = new VBox(10);
        pnl.setPadding(new Insets(16));
        pnl.getStyleClass().add("khung-phan-he");

        Label lblHeader = new Label("DANH SÁCH PHIẾU DỊCH VỤ CHỜ THANH TOÁN");
        lblHeader.getStyleClass().add("tieu-de-phan-he");

        TableView<PhieuDichVu> bangPhieu = new TableView<>(FXCollections.observableArrayList(dsPhieu));
        bangPhieu.getStyleClass().add("bang-du-lieu");
        VBox.setVgrow(bangPhieu, Priority.ALWAYS);

        TableColumn<PhieuDichVu, String> cMa = new TableColumn<>("Mã phiếu");
        cMa.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getMaPhieuDichVu()));
        cMa.setPrefWidth(140);

        TableColumn<PhieuDichVu, String> cKh = new TableColumn<>("Khách hàng");
        cKh.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getTenKhachHang()));
        cKh.setPrefWidth(160);

        TableColumn<PhieuDichVu, String> cSdt = new TableColumn<>("SĐT");
        cSdt.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getSoDienThoai()));
        cSdt.setPrefWidth(100);

        TableColumn<PhieuDichVu, String> cNgay = new TableColumn<>("Ngày tiếp nhận");
        cNgay.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getNgayTiepNhan() != null ? p.getValue().getNgayTiepNhan().format(DINH_DANG_NGAY) : ""));
        cNgay.setPrefWidth(110);
        cNgay.setStyle("-fx-alignment: center;");

        TableColumn<PhieuDichVu, String> cTrangThai = new TableColumn<>("Trạng thái");
        cTrangThai.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getTrangThai()));
        cTrangThai.setPrefWidth(110);
        cTrangThai.setStyle("-fx-alignment: center;");

        bangPhieu.getColumns().addAll(cMa, cKh, cSdt, cNgay, cTrangThai);

        Button btnChon = new Button("Chọn nạp phiếu");
        btnChon.getStyleClass().add("nut-chinh");
        Button btnHuy = new Button("Đóng");
        btnHuy.getStyleClass().add("nut-phu");
        btnHuy.setOnAction(e -> dlg.close());

        btnChon.setOnAction(e -> {
            PhieuDichVu pChon = bangPhieu.getSelectionModel().getSelectedItem();
            if (pChon == null) {
                hienThiCanhBao("Vui lòng chọn một phiếu dịch vụ!");
                return;
            }

            try {
                PhieuDichVu phieuChiTiet = controller.layPhieuDichVuDeThanhToan(pChon.getMaPhieuDichVu());
                maPhieuDichVuDaNap = phieuChiTiet.getMaPhieuDichVu();
                maLichHenDaNap = phieuChiTiet.getMaLichHen();

                // Nạp khách hàng
                for (KhachHang kh : dsKhachHangGoc) {
                    if (kh.getMaKhachHang().equals(phieuChiTiet.getMaKhachHang())) {
                        dongBoThongTinKhachHang(kh);
                        break;
                    }
                }

                // Nạp danh sách dịch vụ vào bảng
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

                bangChiTiet.refresh();
                capNhatTongTienThanhToan();
                dlg.close();
                hienThiThongBao("Thành công", "Đã nạp toàn bộ dịch vụ từ phiếu " + pChon.getMaPhieuDichVu() + " vào hóa đơn!");
            } catch (Exception ex) {
                hienThiLoi("Lỗi nạp phiếu dịch vụ: " + ex.getMessage());
            }
        });

        HBox pnlNut = new HBox(8, btnChon, btnHuy);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        pnl.getChildren().addAll(lblHeader, bangPhieu, pnlNut);
        Scene scene = new Scene(pnl, 650, 420);
        if (LapHoaDonThanhToanView.class.getResource(CSS_CHUNG) != null) {
            scene.getStylesheets().add(LapHoaDonThanhToanView.class.getResource(CSS_CHUNG).toExternalForm());
        }
        dlg.setScene(scene);
        dlg.showAndWait();
    }

    private void xuLyThanhToan(boolean inHoaDonSauThanhToan) {
        if (dsDongHang.isEmpty()) {
            hienThiCanhBao("Hóa đơn chưa có sản phẩm hoặc dịch vụ nào để thanh toán!");
            return;
        }

        BigDecimal tienDua = BigDecimal.ZERO;
        BigDecimal tienThoi = BigDecimal.ZERO;

        // Kiểm tra tiền khách đưa nếu là tiền mặt
        if (radTienMat.isSelected()) {
            String strDua = txtTienKhachDua.getText() != null ? txtTienKhachDua.getText().trim() : "";
            if (strDua.isEmpty()) {
                if (tongThanhToanHienTai.compareTo(BigDecimal.ZERO) == 0) {
                    tienDua = BigDecimal.ZERO;
                } else {
                    hienThiCanhBao("Vui lòng nhập số tiền khách đưa!");
                    txtTienKhachDua.requestFocus();
                    return;
                }
            } else {
                try {
                    tienDua = new BigDecimal(strDua);
                    if (tienDua.compareTo(tongThanhToanHienTai) < 0) {
                        hienThiCanhBao("Số tiền khách đưa không đủ để thanh toán hóa đơn!");
                        txtTienKhachDua.requestFocus();
                        return;
                    }
                } catch (Exception ex) {
                    hienThiCanhBao("Số tiền khách đưa không hợp lệ!");
                    txtTienKhachDua.requestFocus();
                    return;
                }
            }
            tienThoi = tienDua.subtract(tongThanhToanHienTai);
        }

        // Tạo đối tượng HoaDon
        HoaDon hd = new HoaDon();
        hd.setMaHoaDon(txtMaHoaDon.getText().trim());
        hd.setNgayLap(dpNgayLap.getValue());
        hd.setMaPhieuDichVu(maPhieuDichVuDaNap);
        hd.setMaLichHen(maLichHenDaNap);

        var tk = SessionManager.getTaiKhoanHienTai();
        hd.setMaNhanVien(tk != null ? tk.getMaNhanVien() : "NV-20261005-000001");
        hd.setTenNhanVien(tk != null ? tk.getTenDangNhap() : "Quản trị hệ thống");

        KhachHang kh = cboKhachHang.getValue();
        if (kh != null) {
            hd.setMaKhachHang(kh.getMaKhachHang());
            hd.setTenKhachHang(kh.getHoTen());
            hd.setSoDienThoaiKhachHang(kh.getSoDienThoai());
        }

        hd.setHinhThucThanhToan(radTienMat.isSelected() ? "Tiền mặt" : "Chuyển khoản");
        hd.setTrangThaiThanhToan("Đã thanh toán");

        KhuyenMai km = cboKhuyenMai.getValue();
        if (km != null) {
            hd.setMaKhuyenMai(km.getMaKhuyenMai());
            hd.setTenKhuyenMai(km.getTenChuongTrinh());
        }

        hd.setTienGiam(tienGiamKMHienTai);

        // Thiết lập điểm tích lũy
        if (kh != null && chkDungDiem.isSelected() && spnDiemDung.getValue() != null && spnDiemDung.getValue() > 0) {
            hd.setDiemTichLuySuDung(spnDiemDung.getValue());
        } else {
            hd.setDiemTichLuySuDung(0);
        }

        hd.setTongTien(tongThanhToanHienTai);
        hd.setDiemTichLuyCong(diemCongMoiHienTai);

        hd.setTienKhachDua(tienDua);
        hd.setTienThoiLai(tienThoi);

        // Đổ sản phẩm và dịch vụ
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
        maPhieuDichVuDaNap = null;
        maLichHenDaNap = null;

        txtTimKhach.clear();
        cboKhachHang.setValue(null);
        lblThongTinDiemKhach.setText("Khách vãng lai");
        lblThongTinDiemKhach.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic;");

        txtTimSanPham.clear();
        cboSanPham.setValue(null);
        spnSoLuongSp.getValueFactory().setValue(1);
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

        try {
            dsKhachHangGoc.clear();
            dsKhachHangGoc.addAll(controller.layDanhSachKhachHang());
            dsKhachHangHienThi.setAll(dsKhachHangGoc);

            dsSanPhamGoc.clear();
            dsSanPhamGoc.addAll(controller.layDanhSachSanPham());
            dsSanPhamHienThi.setAll(dsSanPhamGoc);
        } catch (Exception ignored) {}
    }

    private void hienThiThongBao(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, noiDung);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void hienThiCanhBao(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.WARNING, noiDung);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void hienThiLoi(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.ERROR, noiDung);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
