package com.petshop.view.hoadon;

import com.petshop.controller.HoaDonController;
import com.petshop.model.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Giao diện Cập nhật hóa đơn bán lẻ đã thanh toán (HD_CAP_NHAT).
 * Cho phép điều chỉnh hóa đơn đã xuất do nhập nhầm:
 * - Thêm / bớt sản phẩm, tăng / giảm số lượng
 * - Tự động tính chênh lệch:
 *   + Nếu phát sinh thêm tiền: Khách thanh toán thêm (Tiền mặt hoặc Chuyển khoản giống bên Lập hóa đơn).
 *   + Nếu bớt tiền: Cửa hàng hoàn tiền cho khách (bằng Tiền mặt tại quầy).
 * - Cập nhật đồng bộ tồn kho và điểm tích lũy khách hàng.
 */
public class CapNhatHoaDonView extends VBox {

    private static final String CSS_CHUNG = "/com/petshop/view/css/chung.css";
    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 VNĐ");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final HoaDonController controller = new HoaDonController();

    // Dữ liệu nguồn
    private final ObservableList<HoaDon> dsHoaDonDaThanhToan = FXCollections.observableArrayList();
    private final ObservableList<SanPham> dsSanPhamGoc = FXCollections.observableArrayList();
    private final ObservableList<SanPham> dsSanPhamHienThi = FXCollections.observableArrayList();
    private final ObservableList<DongHoaDon> dsDongHang = FXCollections.observableArrayList();

    // Hóa đơn hiện tại đang chỉnh sửa
    private HoaDon hoaDonGoc = null;

    // 1. Thanh tìm kiếm & chọn hóa đơn
    private final TextField txtTimHoaDon = new TextField();
    private final Button btnTimHoaDon = new Button("🔍 Tìm kiếm");
    private final ComboBox<HoaDon> cboHoaDon = new ComboBox<>();

    // 2. Thông tin tóm tắt hóa đơn gốc
    private final Label lblMaHd = new Label("--");
    private final Label lblKhachHang = new Label("--");
    private final Label lblNgayLap = new Label("--");
    private final Label lblThuNgan = new Label("--");
    private final Label lblHinhThucCu = new Label("--");
    private final Label lblTienDaThanhToan = new Label("0 VNĐ");

    // 3. Thanh thêm sản phẩm mới vào đơn
    private final TextField txtTimSanPham = new TextField();
    private final ComboBox<SanPham> cboSanPham = new ComboBox<>();
    private final Label lblDonGiaSp = new Label("0 VNĐ");
    private final Label lblTonKhoSp = new Label("Tồn: 0");
    private final Spinner<Integer> spnSoLuongThem = new Spinner<>(1, 9999, 1);
    private final Button btnThemSanPham = new Button("+ Thêm vào đơn");
    private final Button btnXoaDong = new Button("✕ Xóa dòng");

    // 4. Bảng danh sách mặt hàng
    private final TableView<DongHoaDon> bangChiTiet = new TableView<>();

    // 5. Khu vực tổng kết và chênh lệch thanh toán / hoàn tiền
    private final Label lblTongTienCu = new Label("0 VNĐ");
    private final Label lblTongTienMoi = new Label("0 VNĐ");
    private final Label lblTrangThaiChenhLech = new Label("Không phát sinh chênh lệch (0 VNĐ)");

    // Khung Thanh toán thêm (nếu tổng mới > tổng cũ)
    private final VBox boxThanhToanThem = new VBox(8);
    private final RadioButton radTienMat = new RadioButton("Tiền mặt");
    private final RadioButton radChuyenKhoan = new RadioButton("Chuyển khoản");
    private final ToggleGroup grpPhuongThucThem = new ToggleGroup();
    private final TextField txtTienKhachDua = new TextField();
    private final Button btnDuTien = new Button("Đủ tiền");
    private final Label lblTienThoiLai = new Label("0 VNĐ");
    private final VBox boxCkThem = new VBox(4);
    private final Label lblNoiDungCk = new Label();

    // Khung Hoàn tiền mặt (nếu tổng mới < tổng cũ)
    private final VBox boxHoanTien = new VBox(6);
    private final Label lblSoTienHoanMat = new Label("0 VNĐ");

    // Nút hành động
    private final Button btnLuuCapNhat = new Button("✔ Lưu cập nhật");
    private final Button btnLuuVaIn = new Button("🖨 Cập nhật & In hóa đơn");
    private final Button btnLamMoiForm = new Button("Làm mới form");

    // Biến tính toán chênh lệch
    private BigDecimal tongTienMoiHienTai = BigDecimal.ZERO;
    private BigDecimal tienChenhLech = BigDecimal.ZERO;

    public CapNhatHoaDonView() {
        setSpacing(12);
        setPadding(new Insets(16));
        getStyleClass().add("khung-phan-he");

        khoiTaoGiaoDien();
        dangKySuKien();
        napDuLieuBanDau();
    }

    private void khoiTaoGiaoDien() {
        // 1. Tiêu đề
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("CẬP NHẬT HÓA ĐƠN BÁN LẺ");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Chỉnh sửa hóa đơn đã thanh toán khi có nhầm lẫn: thêm/bớt hàng, tăng/giảm số lượng; hỗ trợ thanh toán thêm hoặc hoàn tiền mặt.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Khung tìm kiếm và chọn hóa đơn
        VBox pnlTimKiem = taoKhungTimKiem();

        // 3. Khung thông tin hóa đơn và thanh chọn sản phẩm
        VBox pnlThongTinVaChonSp = taoKhungThongTinVaChonSp();

        // 4. Bảng chi tiết mặt hàng
        taoBangChiTiet();
        VBox.setVgrow(bangChiTiet, Priority.ALWAYS);

        // 5. Khung thanh toán chênh lệch / hoàn tiền
        HBox pnlTongKetChenhLech = taoKhungTongKetChenhLech();

        getChildren().addAll(pnlTieuDe, pnlTimKiem, pnlThongTinVaChonSp, bangChiTiet, pnlTongKetChenhLech);
    }

    private VBox taoKhungTimKiem() {
        VBox pnl = new VBox(8);
        pnl.getStyleClass().add("hop-noi-dung");
        pnl.setPadding(new Insets(10, 14, 10, 14));

        HBox hangTim = new HBox(10);
        hangTim.setAlignment(Pos.CENTER_LEFT);

        Label lblTim = new Label("Chọn hóa đơn cần sửa:");
        lblTim.setStyle("-fx-font-weight: bold;");

        txtTimHoaDon.setPromptText("Nhập mã HĐ hoặc SĐT khách...");
        txtTimHoaDon.setPrefWidth(220);

        btnTimHoaDon.getStyleClass().add("nut-phu");

        cboHoaDon.setPromptText("-- Chọn hóa đơn đã thanh toán gần đây --");
        cboHoaDon.setPrefWidth(350);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnLamMoiForm.getStyleClass().add("nut-phu");
        btnLamMoiForm.setOnAction(e -> lamMoiForm());

        hangTim.getChildren().addAll(lblTim, txtTimHoaDon, btnTimHoaDon, new Label("hoặc:"), cboHoaDon, spacer, btnLamMoiForm);
        pnl.getChildren().add(hangTim);
        return pnl;
    }

    private VBox taoKhungThongTinVaChonSp() {
        VBox pnl = new VBox(10);
        pnl.getStyleClass().add("hop-noi-dung");
        pnl.setPadding(new Insets(10, 14, 10, 14));

        // Hàng thông tin tóm tắt hóa đơn
        GridPane luoiInfo = new GridPane();
        luoiInfo.setHgap(16);
        luoiInfo.setVgap(4);

        luoiInfo.add(new Label("Mã hóa đơn:"), 0, 0);
        lblMaHd.setStyle("-fx-font-weight: bold; -fx-text-fill: #0369a1;");
        luoiInfo.add(lblMaHd, 1, 0);

        luoiInfo.add(new Label("Khách hàng:"), 2, 0);
        lblKhachHang.setStyle("-fx-font-weight: bold;");
        luoiInfo.add(lblKhachHang, 3, 0);

        luoiInfo.add(new Label("Ngày lập:"), 4, 0);
        luoiInfo.add(lblNgayLap, 5, 0);

        luoiInfo.add(new Label("Thu ngân:"), 0, 1);
        luoiInfo.add(lblThuNgan, 1, 1);

        luoiInfo.add(new Label("Hình thức TT gốc:"), 2, 1);
        luoiInfo.add(lblHinhThucCu, 3, 1);

        luoiInfo.add(new Label("Tiền đã thanh toán:"), 4, 1);
        lblTienDaThanhToan.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #15803d;");
        luoiInfo.add(lblTienDaThanhToan, 5, 1);

        Separator sep = new Separator();

        // Hàng thêm sản phẩm
        HBox hangThemSp = new HBox(8);
        hangThemSp.setAlignment(Pos.CENTER_LEFT);

        Label lblThem = new Label("Thêm mặt hàng:");
        lblThem.setStyle("-fx-font-weight: bold;");

        txtTimSanPham.setPromptText("Tìm kiếm sản phẩm...");
        txtTimSanPham.setPrefWidth(160);

        cboSanPham.setPromptText("-- Chọn sản phẩm --");
        cboSanPham.setPrefWidth(240);

        lblDonGiaSp.setStyle("-fx-font-weight: bold; -fx-min-width: 85px;");
        lblTonKhoSp.setStyle("-fx-text-fill: #15803d; -fx-font-weight: bold; -fx-min-width: 65px;");

        spnSoLuongThem.setPrefWidth(75);
        spnSoLuongThem.setEditable(true);

        btnThemSanPham.getStyleClass().add("nut-chinh");
        btnThemSanPham.setOnAction(e -> xuLyThemSanPham());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnXoaDong.getStyleClass().add("nut-xoa");
        btnXoaDong.setOnAction(e -> xuLyXoaDong());

        hangThemSp.getChildren().addAll(lblThem, txtTimSanPham, cboSanPham, lblDonGiaSp, lblTonKhoSp,
                new Label("SL:"), spnSoLuongThem, btnThemSanPham, spacer, btnXoaDong);

        pnl.getChildren().addAll(luoiInfo, sep, hangThemSp);
        return pnl;
    }

    private void taoBangChiTiet() {
        bangChiTiet.getStyleClass().add("bang-du-lieu");
        bangChiTiet.setItems(dsDongHang);
        bangChiTiet.setPlaceholder(new Label("Vui lòng chọn một hóa đơn đã thanh toán để bắt đầu cập nhật."));

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

        // Cột Số lượng cho phép chỉnh sửa trực tiếp qua Spinner
        TableColumn<DongHoaDon, Integer> colSl = new TableColumn<>("Số lượng");
        colSl.setPrefWidth(120);
        colSl.setStyle("-fx-alignment: center;");
        colSl.setCellFactory(col -> new TableCell<>() {
            private final Spinner<Integer> spinner = new Spinner<>(1, 9999, 1);
            {
                spinner.setEditable(true);
                spinner.setPrefWidth(85);
                spinner.valueProperty().addListener((obs, cu, moi) -> {
                    if (moi != null && getIndex() >= 0 && getIndex() < dsDongHang.size()) {
                        DongHoaDon d = dsDongHang.get(getIndex());
                        if (d.getLoai() == DongHoaDon.LoaiDong.SAN_PHAM && d.getSoLuong() != moi) {
                            d.setSoLuong(moi);
                            bangChiTiet.refresh();
                            tinhToanLaiChenhLech();
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= dsDongHang.size()) {
                    setGraphic(null);
                } else {
                    DongHoaDon d = dsDongHang.get(getIndex());
                    if (d.getLoai() == DongHoaDon.LoaiDong.SAN_PHAM) {
                        spinner.getValueFactory().setValue(d.getSoLuong());
                        setGraphic(spinner);
                    } else {
                        // Dịch vụ cố định số lượng = 1
                        setText("1");
                        setGraphic(null);
                    }
                }
            }
        });

        TableColumn<DongHoaDon, String> colGia = new TableColumn<>("Đơn giá");
        colGia.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().getDonGia())));
        colGia.setPrefWidth(110);
        colGia.setStyle("-fx-alignment: center-right;");

        TableColumn<DongHoaDon, String> colThanhTien = new TableColumn<>("Thành tiền");
        colThanhTien.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().getThanhTien())));
        colThanhTien.setPrefWidth(130);
        colThanhTien.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");

        bangChiTiet.getColumns().addAll(colStt, colLoai, colMa, colTen, colDvt, colSl, colGia, colThanhTien);
    }

    private HBox taoKhungTongKetChenhLech() {
        HBox pnl = new HBox(20);
        pnl.getStyleClass().add("hop-noi-dung");
        pnl.setPadding(new Insets(12, 16, 12, 16));
        pnl.setAlignment(Pos.CENTER_LEFT);

        // Cột 1: So sánh tổng tiền cũ và mới
        VBox cotSoSanh = new VBox(6);
        cotSoSanh.setPrefWidth(280);

        GridPane luoiTien = new GridPane();
        luoiTien.setHgap(12);
        luoiTien.setVgap(4);

        luoiTien.add(new Label("Tiền đã thanh toán (Cũ):"), 0, 0);
        lblTongTienCu.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        luoiTien.add(lblTongTienCu, 1, 0);

        luoiTien.add(new Label("Tổng tiền hóa đơn mới:"), 0, 1);
        lblTongTienMoi.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #0369a1;");
        luoiTien.add(lblTongTienMoi, 1, 1);

        lblTrangThaiChenhLech.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 4 0;");

        cotSoSanh.getChildren().addAll(luoiTien, lblTrangThaiChenhLech);

        Separator sep1 = new Separator(javafx.geometry.Orientation.VERTICAL);

        // Cột 2: Khung động: Thanh toán thêm hoặc Hoàn tiền
        StackPane khungHinhThuc = new StackPane();
        khungHinhThuc.setPrefWidth(380);

        // A. Khung Thanh toán thêm (khi mới > cũ)
        boxThanhToanThem.setStyle("-fx-background-color: #f0fdf4; -fx-padding: 8; -fx-border-color: #bbf7d0; -fx-border-radius: 4; -fx-background-radius: 4;");
        radTienMat.setToggleGroup(grpPhuongThucThem);
        radChuyenKhoan.setToggleGroup(grpPhuongThucThem);
        radTienMat.setSelected(true);
        HBox hangRadioThem = new HBox(12, radTienMat, radChuyenKhoan);

        // Chi tiết tiền mặt thanh toán thêm
        GridPane luoiTmThem = new GridPane();
        luoiTmThem.setHgap(8);
        luoiTmThem.setVgap(4);
        txtTienKhachDua.setPromptText("Nhập tiền khách đưa...");
        txtTienKhachDua.setPrefWidth(140);
        btnDuTien.getStyleClass().add("nut-phu");
        HBox hangDua = new HBox(4, txtTienKhachDua, btnDuTien);
        lblTienThoiLai.setStyle("-fx-font-weight: bold; -fx-text-fill: #15803d;");
        luoiTmThem.add(new Label("Khách đưa:"), 0, 0);
        luoiTmThem.add(hangDua, 1, 0);
        luoiTmThem.add(new Label("Tiền thối:"), 0, 1);
        luoiTmThem.add(lblTienThoiLai, 1, 1);

        // Chi tiết chuyển khoản thêm
        boxCkThem.setStyle("-fx-background-color: #ffffff; -fx-padding: 6; -fx-border-color: #cbd5e1; -fx-border-radius: 4;");
        Label lblStk = new Label("STK: 0909000000 (MB Bank) - PET SHOP HAPPY TAILS");
        lblStk.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #166534;");
        lblNoiDungCk.setStyle("-fx-font-size: 11px; -fx-text-fill: #15803d;");
        boxCkThem.getChildren().addAll(lblStk, lblNoiDungCk);
        boxCkThem.setVisible(false);
        boxCkThem.setManaged(false);

        boxThanhToanThem.getChildren().addAll(new Label("Hình thức thanh toán thêm:"), hangRadioThem, luoiTmThem, boxCkThem);

        // B. Khung Hoàn tiền mặt (khi mới < cũ)
        boxHoanTien.setStyle("-fx-background-color: #fef2f2; -fx-padding: 10; -fx-border-color: #fecaca; -fx-border-radius: 4; -fx-background-radius: 4;");
        Label lblTieuDeHoan = new Label("PHƯƠNG THỨC HOÀN TIỀN:");
        lblTieuDeHoan.setStyle("-fx-font-weight: bold; -fx-text-fill: #991b1b;");
        Label lblGhiChuHoan = new Label("Hoàn trả bằng TIỀN MẶT tại quầy thu ngân.");
        lblGhiChuHoan.setStyle("-fx-font-weight: bold; -fx-text-fill: #dc2626;");
        lblSoTienHoanMat.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #dc2626;");
        boxHoanTien.getChildren().addAll(lblTieuDeHoan, lblGhiChuHoan, new HBox(8, new Label("Số tiền hoàn trả khách:"), lblSoTienHoanMat));

        khungHinhThuc.getChildren().addAll(boxThanhToanThem, boxHoanTien);
        boxThanhToanThem.setVisible(false);
        boxHoanTien.setVisible(false);

        Separator sep2 = new Separator(javafx.geometry.Orientation.VERTICAL);

        // Cột 3: Nút lưu và in
        VBox cotNut = new VBox(8);
        cotNut.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(cotNut, Priority.ALWAYS);

        btnLuuCapNhat.getStyleClass().add("nut-chinh");
        btnLuuCapNhat.setStyle("-fx-font-size: 13px; -fx-padding: 8 16;");
        btnLuuCapNhat.setDisable(true);
        btnLuuCapNhat.setOnAction(e -> xuLyLuuCapNhat(false));

        btnLuuVaIn.getStyleClass().add("nut-chinh");
        btnLuuVaIn.setStyle("-fx-font-size: 13px; -fx-padding: 8 16;");
        btnLuuVaIn.setDisable(true);
        btnLuuVaIn.setOnAction(e -> xuLyLuuCapNhat(true));

        cotNut.getChildren().addAll(btnLuuCapNhat, btnLuuVaIn);

        pnl.getChildren().addAll(cotSoSanh, sep1, khungHinhThuc, sep2, cotNut);
        return pnl;
    }

    private void dangKySuKien() {
        // Tìm kiếm hóa đơn khi gõ hoặc click nút
        btnTimHoaDon.setOnAction(e -> xuLyTimKiemHoaDon());
        txtTimHoaDon.setOnAction(e -> xuLyTimKiemHoaDon());

        // Chọn từ ComboBox hóa đơn gần đây
        cboHoaDon.setOnAction(e -> {
            HoaDon chon = cboHoaDon.getValue();
            if (chon != null) {
                napThongTinHoaDon(chon.getMaHoaDon());
            }
        });

        // Tìm kiếm sản phẩm
        txtTimSanPham.textProperty().addListener((obs, cu, moi) -> locDanhSachSanPham(moi));

        // Khi chọn sản phẩm thì cập nhật giá và tồn kho
        cboSanPham.setOnAction(e -> {
            SanPham sp = cboSanPham.getValue();
            if (sp != null) {
                lblDonGiaSp.setText(DINH_DANG_TIEN.format(sp.getGiaBan()));
                lblTonKhoSp.setText("Tồn: " + sp.getSoLuongTon());
            } else {
                lblDonGiaSp.setText("0 VNĐ");
                lblTonKhoSp.setText("Tồn: 0");
            }
        });

        // Chuyển đổi phương thức thanh toán thêm
        radTienMat.setOnAction(e -> {
            boxCkThem.setVisible(false);
            boxCkThem.setManaged(false);
        });
        radChuyenKhoan.setOnAction(e -> {
            boxCkThem.setVisible(true);
            boxCkThem.setManaged(true);
        });

        // Tiền khách đưa khi thanh toán thêm
        txtTienKhachDua.textProperty().addListener((obs, cu, moi) -> tinhToanTienThoiThem());
        btnDuTien.setOnAction(e -> {
            if (tienChenhLech.compareTo(BigDecimal.ZERO) > 0) {
                txtTienKhachDua.setText(String.valueOf(tienChenhLech.longValue()));
            }
        });
    }

    private void napDuLieuBanDau() {
        // Nạp danh sách hóa đơn đã thanh toán gần đây
        try {
            List<HoaDon> ds = controller.timKiemHoaDon("", null, null, "Tất cả", "Đã thanh toán");
            dsHoaDonDaThanhToan.setAll(ds);
            cboHoaDon.setItems(dsHoaDonDaThanhToan);
            cboHoaDon.setConverter(new javafx.util.StringConverter<>() {
                @Override public String toString(HoaDon hd) {
                    return hd != null ? hd.getMaHoaDon() + " - " + hd.getTenKhachHang() + " (" + DINH_DANG_TIEN.format(hd.getTongTien()) + ")" : "";
                }
                @Override public HoaDon fromString(String s) { return null; }
            });
        } catch (Exception ignored) {}

        // Nạp danh mục sản phẩm
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
    }

    private void locDanhSachSanPham(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            dsSanPhamHienThi.setAll(dsSanPhamGoc);
            return;
        }
        String pattern = tuKhoa.trim().toLowerCase();
        List<SanPham> loc = new ArrayList<>();
        for (SanPham sp : dsSanPhamGoc) {
            boolean khop = sp.getTenSanPham().toLowerCase().contains(pattern)
                    || sp.getMaSanPham().toLowerCase().contains(pattern);
            if (khop) loc.add(sp);
        }
        dsSanPhamHienThi.setAll(loc);
        if (!dsSanPhamHienThi.isEmpty()) {
            cboSanPham.setValue(dsSanPhamHienThi.get(0));
        }
    }

    private void xuLyTimKiemHoaDon() {
        String tuKhoa = txtTimHoaDon.getText() != null ? txtTimHoaDon.getText().trim() : "";
        if (tuKhoa.isEmpty()) {
            hienThiCanhBao("Vui lòng nhập Mã hóa đơn hoặc Số điện thoại khách hàng cần tìm!");
            return;
        }

        try {
            List<HoaDon> ketQua = controller.timKiemHoaDon(tuKhoa, null, null, "Tất cả", "Đã thanh toán");
            if (ketQua.isEmpty()) {
                hienThiCanhBao("Không tìm thấy hóa đơn đã thanh toán nào khớp với từ khóa: " + tuKhoa);
                return;
            }
            napThongTinHoaDon(ketQua.get(0).getMaHoaDon());
        } catch (Exception ex) {
            hienThiLoi("Lỗi khi tìm kiếm hóa đơn: " + ex.getMessage());
        }
    }

    private void napThongTinHoaDon(String maHoaDon) {
        try {
            HoaDon hd = controller.layHoaDonTheoMa(maHoaDon);
            if (hd == null) {
                hienThiCanhBao("Không tìm thấy dữ liệu chi tiết của hóa đơn: " + maHoaDon);
                return;
            }

            if ("Đã hủy".equalsIgnoreCase(hd.getTrangThaiThanhToan())) {
                hienThiCanhBao("Hóa đơn '" + maHoaDon + "' đã bị hủy, không thể chỉnh sửa!");
                return;
            }

            this.hoaDonGoc = hd;

            // Nạp thông tin giao diện
            lblMaHd.setText(hd.getMaHoaDon());
            lblKhachHang.setText((hd.getTenKhachHang() != null ? hd.getTenKhachHang() : "Khách vãng lai")
                    + (hd.getSoDienThoaiKhachHang() != null ? " (" + hd.getSoDienThoaiKhachHang() + ")" : ""));
            lblNgayLap.setText(hd.getNgayLap() != null ? hd.getNgayLap().format(DINH_DANG_NGAY) : "--");
            lblThuNgan.setText(hd.getTenNhanVien() != null ? hd.getTenNhanVien() : hd.getMaNhanVien());
            lblHinhThucCu.setText(hd.getHinhThucThanhToan() != null ? hd.getHinhThucThanhToan() : "Tiền mặt");
            lblTienDaThanhToan.setText(DINH_DANG_TIEN.format(hd.getTongTien()));
            lblTongTienCu.setText(DINH_DANG_TIEN.format(hd.getTongTien()));

            lblNoiDungCk.setText("Nội dung CK: " + hd.getMaHoaDon() + " Tra them");

            // Nạp các dòng mặt hàng
            dsDongHang.clear();
            int stt = 1;
            if (hd.getDanhSachSanPham() != null) {
                for (ChiTietHoaDonSanPham sp : hd.getDanhSachSanPham()) {
                    dsDongHang.add(new DongHoaDon(
                            stt++,
                            DongHoaDon.LoaiDong.SAN_PHAM,
                            sp.getMaSanPham(),
                            sp.getTenSanPham() != null ? sp.getTenSanPham() : sp.getMaSanPham(),
                            sp.getDonViTinh() != null ? sp.getDonViTinh() : "Cái",
                            sp.getSoLuong(),
                            sp.getDonGia(),
                            ""
                    ));
                }
            }

            if (hd.getDanhSachDichVu() != null) {
                for (ChiTietHoaDonDichVu dv : hd.getDanhSachDichVu()) {
                    dsDongHang.add(new DongHoaDon(
                            stt++,
                            DongHoaDon.LoaiDong.DICH_VU,
                            dv.getMaDichVu(),
                            dv.getTenDichVu() != null ? dv.getTenDichVu() : dv.getMaDichVu(),
                            "Lần",
                            1,
                            dv.getDonGia(),
                            "Dịch vụ chăm sóc"
                    ));
                }
            }

            btnLuuCapNhat.setDisable(false);
            btnLuuVaIn.setDisable(false);

            tinhToanLaiChenhLech();
        } catch (Exception ex) {
            hienThiLoi("Lỗi khi nạp hóa đơn: " + ex.getMessage());
        }
    }

    private void xuLyThemSanPham() {
        if (hoaDonGoc == null) {
            hienThiCanhBao("Vui lòng tìm và nạp một hóa đơn đã thanh toán trước khi thêm mặt hàng!");
            return;
        }

        SanPham sp = cboSanPham.getValue();
        if (sp == null) {
            hienThiCanhBao("Vui lòng chọn sản phẩm cần thêm vào hóa đơn!");
            return;
        }

        int soLuongThem = spnSoLuongThem.getValue() != null ? spnSoLuongThem.getValue() : 1;
        if (soLuongThem <= 0) {
            hienThiCanhBao("Số lượng thêm vào phải lớn hơn 0!");
            return;
        }

        // Kiểm tra xem sản phẩm đã có trong bảng chưa
        for (DongHoaDon d : dsDongHang) {
            if (d.getLoai() == DongHoaDon.LoaiDong.SAN_PHAM && d.getMaHang().equalsIgnoreCase(sp.getMaSanPham())) {
                d.setSoLuong(d.getSoLuong() + soLuongThem);
                bangChiTiet.refresh();
                tinhToanLaiChenhLech();
                return;
            }
        }

        // Nếu chưa có thì thêm dòng mới
        DongHoaDon dongMoi = DongHoaDon.tuSanPham(dsDongHang.size() + 1, sp, soLuongThem);
        dsDongHang.add(dongMoi);
        tinhToanLaiChenhLech();
    }

    private void xuLyXoaDong() {
        DongHoaDon chon = bangChiTiet.getSelectionModel().getSelectedItem();
        if (chon == null) {
            hienThiCanhBao("Vui lòng chọn dòng mặt hàng muốn xóa trên bảng!");
            return;
        }

        if (dsDongHang.size() <= 1) {
            hienThiCanhBao("Hóa đơn phải có ít nhất một mặt hàng hoặc dịch vụ!");
            return;
        }

        dsDongHang.remove(chon);
        // Đánh lại STT
        for (int i = 0; i < dsDongHang.size(); i++) {
            dsDongHang.get(i).setStt(i + 1);
        }
        bangChiTiet.refresh();
        tinhToanLaiChenhLech();
    }

    private void tinhToanLaiChenhLech() {
        if (hoaDonGoc == null) return;

        BigDecimal tongGocMoi = BigDecimal.ZERO;
        for (DongHoaDon d : dsDongHang) {
            tongGocMoi = tongGocMoi.add(d.getThanhTien());
        }

        // Áp lại các khoản giảm giá gốc nếu có
        BigDecimal tienGiamKm = hoaDonGoc.getTienGiam();
        BigDecimal tienGiamDiem = hoaDonGoc.getTienGiamTuDiem();
        BigDecimal tongGiam = tienGiamKm.add(tienGiamDiem);

        this.tongTienMoiHienTai = tongGocMoi.subtract(tongGiam).max(BigDecimal.ZERO);
        lblTongTienMoi.setText(DINH_DANG_TIEN.format(tongTienMoiHienTai));

        BigDecimal tongCu = hoaDonGoc.getTongTien();
        this.tienChenhLech = tongTienMoiHienTai.subtract(tongCu);

        if (tienChenhLech.compareTo(BigDecimal.ZERO) > 0) {
            // Khách phải trả thêm
            lblTrangThaiChenhLech.setText("KHÁCH CẦN THANH TOÁN THÊM: +" + DINH_DANG_TIEN.format(tienChenhLech));
            lblTrangThaiChenhLech.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #0369a1;");

            boxThanhToanThem.setVisible(true);
            boxThanhToanThem.setManaged(true);
            boxHoanTien.setVisible(false);
            boxHoanTien.setManaged(false);

            tinhToanTienThoiThem();
        } else if (tienChenhLech.compareTo(BigDecimal.ZERO) < 0) {
            // Cửa hàng hoàn tiền mặt cho khách
            BigDecimal tienHoan = tienChenhLech.abs();
            lblTrangThaiChenhLech.setText("CỬA HÀNG HOÀN TIỀN CHO KHÁCH: " + DINH_DANG_TIEN.format(tienHoan));
            lblTrangThaiChenhLech.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #dc2626;");

            lblSoTienHoanMat.setText(DINH_DANG_TIEN.format(tienHoan));

            boxThanhToanThem.setVisible(false);
            boxThanhToanThem.setManaged(false);
            boxHoanTien.setVisible(true);
            boxHoanTien.setManaged(true);
        } else {
            // Bằng nhau
            lblTrangThaiChenhLech.setText("Không phát sinh chênh lệch (0 VNĐ)");
            lblTrangThaiChenhLech.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #475569;");

            boxThanhToanThem.setVisible(false);
            boxThanhToanThem.setManaged(false);
            boxHoanTien.setVisible(false);
            boxHoanTien.setManaged(false);
        }
    }

    private void tinhToanTienThoiThem() {
        if (tienChenhLech.compareTo(BigDecimal.ZERO) <= 0) {
            lblTienThoiLai.setText("0 VNĐ");
            return;
        }

        String nhap = txtTienKhachDua.getText() != null ? txtTienKhachDua.getText().trim() : "";
        if (nhap.isEmpty()) {
            lblTienThoiLai.setText("0 VNĐ");
            return;
        }

        try {
            BigDecimal dua = new BigDecimal(nhap);
            BigDecimal thoi = dua.subtract(tienChenhLech);
            if (thoi.compareTo(BigDecimal.ZERO) >= 0) {
                lblTienThoiLai.setText(DINH_DANG_TIEN.format(thoi));
                lblTienThoiLai.setStyle("-fx-font-weight: bold; -fx-text-fill: #15803d;");
            } else {
                lblTienThoiLai.setText("Chưa đủ (còn thiếu " + DINH_DANG_TIEN.format(thoi.abs()) + ")");
                lblTienThoiLai.setStyle("-fx-font-weight: bold; -fx-text-fill: #dc2626;");
            }
        } catch (Exception ex) {
            lblTienThoiLai.setText("Số tiền không hợp lệ");
        }
    }

    private void xuLyLuuCapNhat(boolean inHoaDon) {
        if (hoaDonGoc == null) {
            hienThiCanhBao("Chưa có hóa đơn nào được chọn để cập nhật!");
            return;
        }

        if (dsDongHang.isEmpty()) {
            hienThiCanhBao("Hóa đơn phải có ít nhất một mặt hàng hoặc dịch vụ!");
            return;
        }

        // Tạo đối tượng HoaDon mới từ dữ liệu trên bảng
        HoaDon hdMoi = new HoaDon();
        hdMoi.setMaHoaDon(hoaDonGoc.getMaHoaDon());
        hdMoi.setMaKhachHang(hoaDonGoc.getMaKhachHang());
        hdMoi.setTenKhachHang(hoaDonGoc.getTenKhachHang());
        hdMoi.setSoDienThoaiKhachHang(hoaDonGoc.getSoDienThoaiKhachHang());
        hdMoi.setMaNhanVien(hoaDonGoc.getMaNhanVien());
        hdMoi.setTenNhanVien(hoaDonGoc.getTenNhanVien());
        hdMoi.setNgayLap(hoaDonGoc.getNgayLap());
        hdMoi.setGioLap(hoaDonGoc.getGioLap());
        hdMoi.setTrangThaiThanhToan(hoaDonGoc.getTrangThaiThanhToan());
        hdMoi.setMaKhuyenMai(hoaDonGoc.getMaKhuyenMai());
        hdMoi.setTenKhuyenMai(hoaDonGoc.getTenKhuyenMai());
        hdMoi.setTienGiam(hoaDonGoc.getTienGiam());
        hdMoi.setDiemTichLuySuDung(hoaDonGoc.getDiemTichLuySuDung());
        hdMoi.setDiemTichLuyCong(hoaDonGoc.getDiemTichLuyCong());

        // Hình thức thanh toán: nếu có thanh toán thêm thì ghi nhận hình thức trả thêm
        if (tienChenhLech.compareTo(BigDecimal.ZERO) > 0) {
            hdMoi.setHinhThucThanhToan(radChuyenKhoan.isSelected() ? "Chuyển khoản" : "Tiền mặt");
            hdMoi.setTienThanhToanThem(tienChenhLech);
            hdMoi.setTienHoanLai(BigDecimal.ZERO);
        } else if (tienChenhLech.compareTo(BigDecimal.ZERO) < 0) {
            hdMoi.setHinhThucThanhToan(hoaDonGoc.getHinhThucThanhToan());
            hdMoi.setTienThanhToanThem(BigDecimal.ZERO);
            hdMoi.setTienHoanLai(tienChenhLech.abs());
        } else {
            hdMoi.setHinhThucThanhToan(hoaDonGoc.getHinhThucThanhToan());
            hdMoi.setTienThanhToanThem(BigDecimal.ZERO);
            hdMoi.setTienHoanLai(BigDecimal.ZERO);
        }

        hdMoi.setTongTien(tongTienMoiHienTai);

        for (DongHoaDon d : dsDongHang) {
            if (d.getLoai() == DongHoaDon.LoaiDong.SAN_PHAM) {
                hdMoi.getDanhSachSanPham().add(new ChiTietHoaDonSanPham(
                        hdMoi.getMaHoaDon(),
                        d.getMaHang(),
                        d.getTenHang(),
                        d.getDonViTinh(),
                        d.getSoLuong(),
                        d.getDonGia()
                ));
            } else {
                hdMoi.getDanhSachDichVu().add(new ChiTietHoaDonDichVu(
                        hdMoi.getMaHoaDon(),
                        d.getMaHang(),
                        d.getTenHang(),
                        d.getMaThuCung(),
                        d.getTenThuCung(),
                        d.getDonGia()
                ));
            }
        }

        try {
            boolean thanhCong = controller.capNhatHoaDon(hoaDonGoc, hdMoi, hdMoi.getTienThanhToanThem(), hdMoi.getTienHoanLai());
            if (thanhCong) {
                StringBuilder thongBao = new StringBuilder("Đã cập nhật hóa đơn " + hdMoi.getMaHoaDon() + " thành công!\n");
                thongBao.append("Tổng tiền mới: ").append(DINH_DANG_TIEN.format(hdMoi.getTongTien())).append("\n");
                if (hdMoi.getTienThanhToanThem().compareTo(BigDecimal.ZERO) > 0) {
                    thongBao.append("Đã thu thêm: ").append(DINH_DANG_TIEN.format(hdMoi.getTienThanhToanThem()))
                            .append(" (").append(hdMoi.getHinhThucThanhToan()).append(")\n");
                } else if (hdMoi.getTienHoanLai().compareTo(BigDecimal.ZERO) > 0) {
                    thongBao.append("Đã hoàn lại tiền mặt: ").append(DINH_DANG_TIEN.format(hdMoi.getTienHoanLai())).append("\n");
                }

                hienThiThongBao("Cập nhật thành công", thongBao.toString());

                if (inHoaDon) {
                    new InHoaDonDialog(hdMoi, (Stage) getScene().getWindow()).showAndWait();
                }

                lamMoiForm();
            }
        } catch (Exception ex) {
            hienThiLoi("Lỗi khi cập nhật hóa đơn: " + ex.getMessage());
        }
    }

    private void lamMoiForm() {
        hoaDonGoc = null;
        lblMaHd.setText("--");
        lblKhachHang.setText("--");
        lblNgayLap.setText("--");
        lblThuNgan.setText("--");
        lblHinhThucCu.setText("--");
        lblTienDaThanhToan.setText("0 VNĐ");
        lblTongTienCu.setText("0 VNĐ");
        lblTongTienMoi.setText("0 VNĐ");
        lblTrangThaiChenhLech.setText("Không phát sinh chênh lệch (0 VNĐ)");
        lblTrangThaiChenhLech.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #475569;");

        txtTimHoaDon.clear();
        txtTimSanPham.clear();
        txtTienKhachDua.clear();
        lblTienThoiLai.setText("0 VNĐ");

        dsDongHang.clear();
        boxThanhToanThem.setVisible(false);
        boxThanhToanThem.setManaged(false);
        boxHoanTien.setVisible(false);
        boxHoanTien.setManaged(false);

        btnLuuCapNhat.setDisable(true);
        btnLuuVaIn.setDisable(true);

        napDuLieuBanDau();
    }

    private void hienThiThongBao(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, noiDung);
        alert.setHeaderText(tieuDe);
        alert.showAndWait();
    }

    private void hienThiCanhBao(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.WARNING, noiDung);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void hienThiLoi(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.ERROR, noiDung);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}

