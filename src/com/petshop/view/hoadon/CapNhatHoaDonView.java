package com.petshop.view.hoadon;

import com.petshop.controller.HoaDonController;
import com.petshop.model.*;
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
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Giao diện Quản lý & Cập nhật hóa đơn bán lẻ (HD_CAP_NHAT).
 * Theo chuẩn UX hệ thống:
 * 1. Ở màn hình bình thường: Chỉ hiển thị danh sách tất cả các hóa đơn trong hệ thống.
 * 2. Khi nhấn nút "Cập nhật hóa đơn": Chuyển sang giao diện điều chỉnh chi tiết hóa đơn (thêm/bớt hàng, tăng/giảm SL, thanh toán thêm/hoàn tiền).
 * 3. Khi nhấn nút "Xóa hóa đơn": Hiển thị cửa sổ chi tiết hóa đơn với 2 nút [Xóa hóa đơn] và [Hủy bỏ] để xác nhận.
 */
public class CapNhatHoaDonView extends VBox {

    private static final String CSS_CHUNG = "/com/petshop/view/css/chung.css";
    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 VNĐ");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm");

    private final HoaDonController controller = new HoaDonController();

    // Dữ liệu nguồn
    private final ObservableList<HoaDon> dsTatCaHoaDon = FXCollections.observableArrayList();
    private final ObservableList<SanPham> dsSanPhamGoc = FXCollections.observableArrayList();
    private final ObservableList<SanPham> dsSanPhamHienThi = FXCollections.observableArrayList();
    private final ObservableList<DongHoaDon> dsDongHang = FXCollections.observableArrayList();

    // Hóa đơn hiện tại đang chỉnh sửa
    private HoaDon hoaDonGoc = null;

    // HAI MÀN HÌNH CHÍNH (Chuyển đổi linh hoạt)
    private final VBox pnlManHinhDanhSach = new VBox(12);
    private final VBox pnlManHinhChinhSua = new VBox(12);

    // ==========================================
    // 1. CÁC THÀNH PHẦN MÀN HÌNH DANH SÁCH
    // ==========================================
    private final Label lblTongSoHoaDon = new Label("Tổng cộng: 0 hóa đơn");
    private final Button btnChonCapNhat = new Button("✏ Cập nhật hóa đơn");
    private final Button btnXoaHoaDon = new Button("✕ Xóa hóa đơn");
    private final Button btnTaiLaiDanhSach = new Button("🔄 Làm mới");
    private final TableView<HoaDon> bangTatCaHoaDon = new TableView<>();

    // ==========================================
    // 2. CÁC THÀNH PHẦN MÀN HÌNH CHỈNH SỬA
    // ==========================================
    private final Label lblTieuDeSua = new Label("CẬP NHẬT CHI TIẾT HÓA ĐƠN");
    private final Button btnQuayLaiDanhSach = new Button("← Quay lại danh sách hóa đơn");

    // Thông tin tóm tắt hóa đơn gốc
    private final Label lblMaHd = new Label("--");
    private final Label lblKhachHang = new Label("--");
    private final Label lblNgayLap = new Label("--");
    private final Label lblThuNgan = new Label("--");
    private final Label lblHinhThucCu = new Label("--");
    private final Label lblTienDaThanhToan = new Label("0 VNĐ");

    // Thêm sản phẩm mới vào đơn
    private final TextField txtTimSanPham = new TextField();
    private final ComboBox<SanPham> cboSanPham = new ComboBox<>();
    private final Label lblDonGiaSp = new Label("0 VNĐ");
    private final Label lblTonKhoSp = new Label("Tồn: 0");
    private final Spinner<Integer> spnSoLuongThem = new Spinner<>(1, 9999, 1);
    private final Button btnThemSanPham = new Button("+ Thêm vào đơn");

    // Điều chỉnh số lượng dòng đang chọn
    private final Label lblDongDangChon = new Label("Chưa chọn dòng mặt hàng nào");
    private final Spinner<Integer> spnSoLuongSua = new Spinner<>(1, 9999, 1);
    private final Button btnCapNhatSlDong = new Button("✔ Cập nhật SL");
    private final Button btnXoaDong = new Button("✕ Xóa mặt hàng");

    // Bảng chi tiết mặt hàng của hóa đơn đang sửa
    private final TableView<DongHoaDon> bangChiTiet = new TableView<>();

    // Tổng kết và chênh lệch thanh toán / hoàn tiền
    private final Label lblTongTienCu = new Label("0 VNĐ");
    private final Label lblTongTienMoi = new Label("0 VNĐ");
    private final Label lblTrangThaiChenhLech = new Label("Không phát sinh chênh lệch (0 VNĐ)");

    // Khung Thanh toán thêm (khi mới > cũ)
    private final VBox boxThanhToanThem = new VBox(8);
    private final RadioButton radTienMat = new RadioButton("Tiền mặt");
    private final RadioButton radChuyenKhoan = new RadioButton("Chuyển khoản");
    private final ToggleGroup grpPhuongThucThem = new ToggleGroup();
    private final TextField txtTienKhachDua = new TextField();
    private final Button btnDuTien = new Button("Đủ tiền");
    private final Label lblTienThoiLai = new Label("0 VNĐ");
    private final VBox boxCkThem = new VBox(4);
    private final Label lblNoiDungCk = new Label();

    // Khung Hoàn tiền mặt (khi mới < cũ)
    private final VBox boxHoanTien = new VBox(6);
    private final Label lblSoTienHoanMat = new Label("0 VNĐ");

    // Nút hành động
    private final Button btnLuuCapNhat = new Button("✔ Lưu cập nhật");
    private final Button btnLuuVaIn = new Button("🖨 Cập nhật & In hóa đơn");
    private final Button btnHuyChinhSua = new Button("Hủy bỏ");

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

        // Mặc định hiển thị màn hình danh sách, ẩn màn hình chỉnh sửa
        hienThiManHinhDanhSach();
    }

    private void khoiTaoGiaoDien() {
        // 1. Xây dựng Màn hình Danh sách
        taoGiaoDienManHinhDanhSach();
        VBox.setVgrow(pnlManHinhDanhSach, Priority.ALWAYS);

        // 2. Xây dựng Màn hình Chỉnh sửa
        taoGiaoDienManHinhChinhSua();
        VBox.setVgrow(pnlManHinhChinhSua, Priority.ALWAYS);

        getChildren().addAll(pnlManHinhDanhSach, pnlManHinhChinhSua);
    }

    /**
     * Giao diện màn hình bình thường: Chỉ hiển thị danh sách tất cả các hóa đơn.
     */
    private void taoGiaoDienManHinhDanhSach() {
        // Tiêu đề
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("QUẢN LÝ & CẬP NHẬT HÓA ĐƠN");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Danh sách toàn bộ hóa đơn trong hệ thống. Nhấp chọn một hóa đơn để Cập nhật hoặc Xóa.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // Khung bảng danh sách và thanh tác vụ
        VBox pnlKhungBang = new VBox(10);
        pnlKhungBang.getStyleClass().add("hop-noi-dung");
        pnlKhungBang.setPadding(new Insets(12, 14, 12, 14));
        VBox.setVgrow(pnlKhungBang, Priority.ALWAYS);

        HBox hangTacVu = new HBox(10);
        hangTacVu.setAlignment(Pos.CENTER_LEFT);

        Label lblTieuDeBang = new Label("DANH SÁCH TẤT CẢ HÓA ĐƠN");
        lblTieuDeBang.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #1e293b;");

        lblTongSoHoaDon.setStyle("-fx-font-weight: bold; -fx-text-fill: #0284c7;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnChonCapNhat.getStyleClass().add("nut-chinh");
        btnChonCapNhat.setDisable(true);
        btnChonCapNhat.setOnAction(e -> xuLyChuyenSangManHinhChinhSua());

        btnXoaHoaDon.getStyleClass().add("nut-xoa");
        btnXoaHoaDon.setDisable(true);
        btnXoaHoaDon.setOnAction(e -> xuLyMoDialogXoaHoaDon());

        btnTaiLaiDanhSach.getStyleClass().add("nut-phu");
        btnTaiLaiDanhSach.setOnAction(e -> taiTatCaHoaDon());

        hangTacVu.getChildren().addAll(lblTieuDeBang, new Separator(javafx.geometry.Orientation.VERTICAL),
                lblTongSoHoaDon, spacer, btnChonCapNhat, btnXoaHoaDon, btnTaiLaiDanhSach);

        // Bảng danh sách hóa đơn
        bangTatCaHoaDon.getStyleClass().add("bang-du-lieu");
        bangTatCaHoaDon.setItems(dsTatCaHoaDon);
        VBox.setVgrow(bangTatCaHoaDon, Priority.ALWAYS);
        bangTatCaHoaDon.setPlaceholder(new Label("Không có hóa đơn nào trong hệ thống."));

        TableColumn<HoaDon, Integer> colStt = new TableColumn<>("STT");
        colStt.setPrefWidth(50);
        colStt.setStyle("-fx-alignment: center;");
        colStt.setCellValueFactory(cellData -> {
            int index = dsTatCaHoaDon.indexOf(cellData.getValue()) + 1;
            return new SimpleObjectProperty<>(index);
        });

        TableColumn<HoaDon, String> colMaHd = new TableColumn<>("Mã hóa đơn");
        colMaHd.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getMaHoaDon()));
        colMaHd.setPrefWidth(140);
        colMaHd.setStyle("-fx-font-weight: bold; -fx-alignment: center;");

        TableColumn<HoaDon, String> colNgay = new TableColumn<>("Ngày lập");
        colNgay.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getNgayLap() != null ? h.getValue().getNgayLap().format(DINH_DANG_NGAY) : ""));
        colNgay.setPrefWidth(100);
        colNgay.setStyle("-fx-alignment: center;");

        TableColumn<HoaDon, String> colGio = new TableColumn<>("Giờ lập");
        colGio.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getGioLap() != null ? h.getValue().getGioLap().format(DINH_DANG_GIO) : ""));
        colGio.setPrefWidth(75);
        colGio.setStyle("-fx-alignment: center;");

        TableColumn<HoaDon, String> colKhach = new TableColumn<>("Khách hàng");
        colKhach.setCellValueFactory(h -> {
            String ten = h.getValue().getTenKhachHang() != null ? h.getValue().getTenKhachHang() : "Khách vãng lai";
            String sdt = h.getValue().getSoDienThoaiKhachHang();
            return new SimpleStringProperty(sdt != null && !sdt.isEmpty() ? ten + " (" + sdt + ")" : ten);
        });
        colKhach.setPrefWidth(220);

        TableColumn<HoaDon, String> colThuNgan = new TableColumn<>("Thu ngân");
        colThuNgan.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getTenNhanVien() != null ? h.getValue().getTenNhanVien() : h.getValue().getMaNhanVien()));
        colThuNgan.setPrefWidth(140);

        TableColumn<HoaDon, String> colHt = new TableColumn<>("Hình thức TT");
        colHt.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getHinhThucThanhToan()));
        colHt.setPrefWidth(110);
        colHt.setStyle("-fx-alignment: center;");

        TableColumn<HoaDon, String> colTongTien = new TableColumn<>("Tổng tiền");
        colTongTien.setCellValueFactory(h -> new SimpleStringProperty(DINH_DANG_TIEN.format(h.getValue().getTongTien())));
        colTongTien.setPrefWidth(140);
        colTongTien.setStyle("-fx-alignment: center-right; -fx-font-weight: bold; -fx-text-fill: #15803d;");

        TableColumn<HoaDon, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getTrangThaiThanhToan()));
        colTrangThai.setPrefWidth(120);
        colTrangThai.setStyle("-fx-alignment: center;");

        bangTatCaHoaDon.getColumns().addAll(colStt, colMaHd, colNgay, colGio, colKhach, colThuNgan, colHt, colTongTien, colTrangThai);

        // Sự kiện chọn dòng và double click
        bangTatCaHoaDon.getSelectionModel().selectedItemProperty().addListener((obs, cu, moi) -> {
            boolean coChon = moi != null;
            btnChonCapNhat.setDisable(!coChon);
            btnXoaHoaDon.setDisable(!coChon || "Đã hủy".equalsIgnoreCase(moi.getTrangThaiThanhToan()));
        });

        bangTatCaHoaDon.setRowFactory(tv -> {
            TableRow<HoaDon> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    xuLyChuyenSangManHinhChinhSua();
                }
            });
            return row;
        });

        pnlKhungBang.getChildren().addAll(hangTacVu, bangTatCaHoaDon);
        pnlManHinhDanhSach.getChildren().addAll(pnlTieuDe, pnlKhungBang);
    }

    /**
     * Giao diện màn hình chỉnh sửa: Chỉ hiển thị khi bấm nút "Cập nhật hóa đơn".
     */
    private void taoGiaoDienManHinhChinhSua() {
        // Thanh tiêu đề và nút quay lại
        HBox hangTieuDeSua = new HBox(12);
        hangTieuDeSua.setAlignment(Pos.CENTER_LEFT);

        btnQuayLaiDanhSach.getStyleClass().add("nut-phu");
        btnQuayLaiDanhSach.setOnAction(e -> hienThiManHinhDanhSach());

        lblTieuDeSua.getStyleClass().add("tieu-de-phan-he");
        lblTieuDeSua.setStyle("-fx-font-size: 15px; -fx-text-fill: #0369a1;");

        hangTieuDeSua.getChildren().addAll(btnQuayLaiDanhSach, new Separator(javafx.geometry.Orientation.VERTICAL), lblTieuDeSua);

        // Khung thông tin hóa đơn và thanh chọn sản phẩm
        VBox pnlThongTinVaChonSp = taoKhungThongTinVaChonSp();

        // Bảng chi tiết mặt hàng
        taoBangChiTiet();
        VBox.setVgrow(bangChiTiet, Priority.ALWAYS);
        bangChiTiet.setPrefHeight(180);

        // Khung thanh toán chênh lệch / hoàn tiền
        HBox pnlTongKetChenhLech = taoKhungTongKetChenhLech();

        pnlManHinhChinhSua.getChildren().addAll(hangTieuDeSua, pnlThongTinVaChonSp, bangChiTiet, pnlTongKetChenhLech);
    }

    private VBox taoKhungThongTinVaChonSp() {
        VBox pnl = new VBox(10);
        pnl.getStyleClass().add("hop-noi-dung");
        pnl.setPadding(new Insets(10, 14, 10, 14));

        // Hàng 1: Thông tin tóm tắt hóa đơn đang chọn
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

        // Hàng 2: Thêm sản phẩm mới vào đơn
        HBox hangThemSp = new HBox(8);
        hangThemSp.setAlignment(Pos.CENTER_LEFT);

        Label lblThem = new Label("+ Thêm sản phẩm:");
        lblThem.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f766e;");

        txtTimSanPham.setPromptText("Lọc tìm sản phẩm...");
        txtTimSanPham.setPrefWidth(160);

        cboSanPham.setPromptText("-- Chọn sản phẩm --");
        cboSanPham.setPrefWidth(240);

        lblDonGiaSp.setStyle("-fx-font-weight: bold; -fx-min-width: 85px;");
        lblTonKhoSp.setStyle("-fx-text-fill: #15803d; -fx-font-weight: bold; -fx-min-width: 65px;");

        spnSoLuongThem.setPrefWidth(75);
        spnSoLuongThem.setEditable(true);

        btnThemSanPham.getStyleClass().add("nut-chinh");
        btnThemSanPham.setOnAction(e -> xuLyThemSanPham());

        hangThemSp.getChildren().addAll(lblThem, txtTimSanPham, cboSanPham, lblDonGiaSp, lblTonKhoSp,
                new Label("SL:"), spnSoLuongThem, btnThemSanPham);

        // Hàng 3: Điều chỉnh số lượng dòng mặt hàng đang chọn trên bảng chi tiết
        HBox hangDieuChinh = new HBox(10);
        hangDieuChinh.setAlignment(Pos.CENTER_LEFT);
        hangDieuChinh.setStyle("-fx-background-color: #f8fafc; -fx-padding: 6 10; -fx-border-color: #e2e8f0; -fx-border-radius: 4; -fx-background-radius: 4;");

        Label lblTieuDeDc = new Label("Điều chỉnh dòng đang chọn:");
        lblTieuDeDc.setStyle("-fx-font-weight: bold;");

        lblDongDangChon.setStyle("-fx-text-fill: #0369a1; -fx-font-weight: bold;");
        lblDongDangChon.setPrefWidth(260);

        spnSoLuongSua.setPrefWidth(80);
        spnSoLuongSua.setEditable(true);
        spnSoLuongSua.setDisable(true);

        btnCapNhatSlDong.getStyleClass().add("nut-phu");
        btnCapNhatSlDong.setDisable(true);
        btnCapNhatSlDong.setOnAction(e -> xuLyCapNhatSlDongDangChon());

        Region spacerHang3 = new Region();
        HBox.setHgrow(spacerHang3, Priority.ALWAYS);

        btnXoaDong.getStyleClass().add("nut-xoa");
        btnXoaDong.setDisable(true);
        btnXoaDong.setOnAction(e -> xuLyXoaDong());

        hangDieuChinh.getChildren().addAll(lblTieuDeDc, lblDongDangChon, new Label("SL mới:"),
                spnSoLuongSua, btnCapNhatSlDong, spacerHang3, btnXoaDong);

        pnl.getChildren().addAll(luoiInfo, sep, hangThemSp, hangDieuChinh);
        return pnl;
    }

    private void taoBangChiTiet() {
        bangChiTiet.getStyleClass().add("bang-du-lieu");
        bangChiTiet.setItems(dsDongHang);
        bangChiTiet.setPlaceholder(new Label("Hóa đơn chưa có mặt hàng nào."));

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

        // Cột Số lượng: với Sản phẩm có nút [-] [SL] [+], với Dịch vụ cố định 1
        TableColumn<DongHoaDon, Integer> colSl = new TableColumn<>("Số lượng");
        colSl.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSoLuong()));
        colSl.setPrefWidth(130);
        colSl.setStyle("-fx-alignment: center;");
        colSl.setCellFactory(col -> new TableCell<>() {
            private final Button btnGiam = new Button("-");
            private final Label lblSl = new Label("1");
            private final Button btnTang = new Button("+");
            private final HBox boxControls = new HBox(4, btnGiam, lblSl, btnTang);

            {
                boxControls.setAlignment(Pos.CENTER);
                btnGiam.setStyle("-fx-font-size: 11px; -fx-padding: 2 7; -fx-font-weight: bold;");
                btnTang.setStyle("-fx-font-size: 11px; -fx-padding: 2 7; -fx-font-weight: bold;");
                lblSl.setStyle("-fx-font-weight: bold; -fx-min-width: 28px; -fx-alignment: center;");

                btnGiam.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx >= 0 && idx < dsDongHang.size()) {
                        DongHoaDon d = dsDongHang.get(idx);
                        if (d.getLoai() == DongHoaDon.LoaiDong.SAN_PHAM && d.getSoLuong() > 1) {
                            d.setSoLuong(d.getSoLuong() - 1);
                            bangChiTiet.refresh();
                            capNhatThanhDieuChinhSl(d);
                            tinhToanLaiChenhLech();
                        }
                    }
                });

                btnTang.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx >= 0 && idx < dsDongHang.size()) {
                        DongHoaDon d = dsDongHang.get(idx);
                        if (d.getLoai() == DongHoaDon.LoaiDong.SAN_PHAM) {
                            d.setSoLuong(d.getSoLuong() + 1);
                            bangChiTiet.refresh();
                            capNhatThanhDieuChinhSl(d);
                            tinhToanLaiChenhLech();
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= dsDongHang.size()) {
                    setText(null);
                    setGraphic(null);
                } else {
                    DongHoaDon d = dsDongHang.get(getIndex());
                    if (d.getLoai() == DongHoaDon.LoaiDong.SAN_PHAM) {
                        setText(null);
                        lblSl.setText(String.valueOf(d.getSoLuong()));
                        btnGiam.setDisable(d.getSoLuong() <= 1);
                        setGraphic(boxControls);
                    } else {
                        // Dịch vụ cố định 1
                        setGraphic(null);
                        setText("1");
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

        // Lắng nghe chọn dòng trong bảng chi tiết để cập nhật thanh điều chỉnh
        bangChiTiet.getSelectionModel().selectedItemProperty().addListener((obs, cu, moi) -> capNhatThanhDieuChinhSl(moi));
    }

    private void capNhatThanhDieuChinhSl(DongHoaDon dong) {
        if (dong == null) {
            lblDongDangChon.setText("Chưa chọn dòng mặt hàng nào");
            spnSoLuongSua.setDisable(true);
            btnCapNhatSlDong.setDisable(true);
            btnXoaDong.setDisable(true);
        } else {
            btnXoaDong.setDisable(false);
            if (dong.getLoai() == DongHoaDon.LoaiDong.SAN_PHAM) {
                lblDongDangChon.setText(dong.getTenHang() + " (SL: " + dong.getSoLuong() + ")");
                spnSoLuongSua.getValueFactory().setValue(dong.getSoLuong());
                spnSoLuongSua.setDisable(false);
                btnCapNhatSlDong.setDisable(false);
            } else {
                lblDongDangChon.setText(dong.getTenHang() + " [Dịch vụ - cố định 1 lần]");
                spnSoLuongSua.getValueFactory().setValue(1);
                spnSoLuongSua.setDisable(true);
                btnCapNhatSlDong.setDisable(true);
            }
        }
    }

    private void xuLyCapNhatSlDongDangChon() {
        DongHoaDon dong = bangChiTiet.getSelectionModel().getSelectedItem();
        if (dong == null || dong.getLoai() != DongHoaDon.LoaiDong.SAN_PHAM) {
            return;
        }

        int slMoi = spnSoLuongSua.getValue() != null ? spnSoLuongSua.getValue() : 1;
        if (slMoi <= 0) {
            hienThiCanhBao("Số lượng phải lớn hơn 0!");
            return;
        }

        dong.setSoLuong(slMoi);
        bangChiTiet.refresh();
        capNhatThanhDieuChinhSl(dong);
        tinhToanLaiChenhLech();
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

        btnHuyChinhSua.getStyleClass().add("nut-phu");
        btnHuyChinhSua.setOnAction(e -> hienThiManHinhDanhSach());

        cotNut.getChildren().addAll(btnLuuCapNhat, btnLuuVaIn, btnHuyChinhSua);

        pnl.getChildren().addAll(cotSoSanh, sep1, khungHinhThuc, sep2, cotNut);
        return pnl;
    }

    private void dangKySuKien() {
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
        // 1. Tải toàn bộ danh sách hóa đơn
        taiTatCaHoaDon();

        // 2. Nạp danh mục sản phẩm
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

    /**
     * Tải và hiển thị toàn bộ hóa đơn hiện có trong cơ sở dữ liệu.
     */
    private void taiTatCaHoaDon() {
        try {
            List<HoaDon> ds = controller.layTatCaHoaDon();
            dsTatCaHoaDon.setAll(ds);
            lblTongSoHoaDon.setText("Tổng cộng: " + ds.size() + " hóa đơn");
        } catch (Exception ex) {
            hienThiLoi("Lỗi khi tải danh sách hóa đơn: " + ex.getMessage());
        }
    }

    /**
     * Hiển thị màn hình danh sách, ẩn màn hình chỉnh sửa.
     */
    private void hienThiManHinhDanhSach() {
        pnlManHinhDanhSach.setVisible(true);
        pnlManHinhDanhSach.setManaged(true);

        pnlManHinhChinhSua.setVisible(false);
        pnlManHinhChinhSua.setManaged(false);
    }

    /**
     * Hiển thị màn hình chỉnh sửa, ẩn màn hình danh sách.
     */
    private void hienThiManHinhChinhSua() {
        pnlManHinhDanhSach.setVisible(false);
        pnlManHinhDanhSach.setManaged(false);

        pnlManHinhChinhSua.setVisible(true);
        pnlManHinhChinhSua.setManaged(true);
    }

    private void xuLyChuyenSangManHinhChinhSua() {
        HoaDon chon = bangTatCaHoaDon.getSelectionModel().getSelectedItem();
        if (chon == null) {
            hienThiCanhBao("Vui lòng chọn một hóa đơn từ bảng danh sách để cập nhật!");
            return;
        }

        if ("Đã hủy".equalsIgnoreCase(chon.getTrangThaiThanhToan())) {
            hienThiCanhBao("Hóa đơn '" + chon.getMaHoaDon() + "' đã bị hủy, không thể chỉnh sửa!");
            return;
        }

        napThongTinHoaDon(chon.getMaHoaDon());
        hienThiManHinhChinhSua();
    }

    /**
     * Mở hộp thoại xác nhận xóa hóa đơn: hiển thị đầy đủ thông tin chi tiết và 2 nút Xóa - Hủy.
     */
    private void xuLyMoDialogXoaHoaDon() {
        HoaDon chon = bangTatCaHoaDon.getSelectionModel().getSelectedItem();
        if (chon == null) {
            hienThiCanhBao("Vui lòng chọn một hóa đơn từ bảng danh sách để xóa!");
            return;
        }

        if ("Đã hủy".equalsIgnoreCase(chon.getTrangThaiThanhToan())) {
            hienThiCanhBao("Hóa đơn '" + chon.getMaHoaDon() + "' đã bị hủy trước đó!");
            return;
        }

        // Tải chi tiết hóa đơn đầy đủ từ DB
        HoaDon hdChiTiet = controller.layHoaDonTheoMa(chon.getMaHoaDon());
        if (hdChiTiet == null) {
            hienThiLoi("Không thể nạp dữ liệu chi tiết của hóa đơn: " + chon.getMaHoaDon());
            return;
        }

        Stage cuaSoMe = (Stage) getScene().getWindow();
        XacNhanXoaHoaDonDialog dialog = new XacNhanXoaHoaDonDialog(hdChiTiet, cuaSoMe, () -> {
            taiTatCaHoaDon();
        });
        dialog.showAndWait();
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

    private void napThongTinHoaDon(String maHoaDon) {
        try {
            HoaDon hd = controller.layHoaDonTheoMa(maHoaDon);
            if (hd == null) {
                hienThiCanhBao("Không tìm thấy dữ liệu chi tiết của hóa đơn: " + maHoaDon);
                return;
            }

            this.hoaDonGoc = hd;
            lblTieuDeSua.setText("CẬP NHẬT CHI TIẾT HÓA ĐƠN: " + hd.getMaHoaDon());

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

            capNhatThanhDieuChinhSl(null);
            tinhToanLaiChenhLech();
        } catch (Exception ex) {
            hienThiLoi("Lỗi khi nạp hóa đơn: " + ex.getMessage());
        }
    }

    private void xuLyThemSanPham() {
        if (hoaDonGoc == null) {
            hienThiCanhBao("Vui lòng chọn một hóa đơn từ bảng danh sách bên trên trước khi thêm mặt hàng!");
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
                capNhatThanhDieuChinhSl(d);
                tinhToanLaiChenhLech();
                return;
            }
        }

        // Nếu chưa có thì thêm dòng mới
        DongHoaDon dongMoi = DongHoaDon.tuSanPham(dsDongHang.size() + 1, sp, soLuongThem);
        dsDongHang.add(dongMoi);
        bangChiTiet.getSelectionModel().select(dongMoi);
        capNhatThanhDieuChinhSl(dongMoi);
        tinhToanLaiChenhLech();
    }

    private void xuLyXoaDong() {
        DongHoaDon chon = bangChiTiet.getSelectionModel().getSelectedItem();
        if (chon == null) {
            hienThiCanhBao("Vui lòng chọn dòng mặt hàng muốn xóa trên bảng chi tiết!");
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
        capNhatThanhDieuChinhSl(null);
        tinhToanLaiChenhLech();
    }

    private void tinhToanLaiChenhLech() {
        if (hoaDonGoc == null) return;

        BigDecimal tongGocMoi = BigDecimal.ZERO;
        for (DongHoaDon d : dsDongHang) {
            tongGocMoi = tongGocMoi.add(d.getThanhTien());
        }

        // Áp lại các khoản giảm giá gốc nếu có
        BigDecimal tienGiamKm = hoaDonGoc.getTienGiam() != null ? hoaDonGoc.getTienGiam() : BigDecimal.ZERO;
        BigDecimal tienGiamDiem = hoaDonGoc.getTienGiamTuDiem() != null ? hoaDonGoc.getTienGiamTuDiem() : BigDecimal.ZERO;
        BigDecimal tongGiam = tienGiamKm.add(tienGiamDiem);

        this.tongTienMoiHienTai = tongGocMoi.subtract(tongGiam).max(BigDecimal.ZERO);
        lblTongTienMoi.setText(DINH_DANG_TIEN.format(tongTienMoiHienTai));

        BigDecimal tongCu = hoaDonGoc.getTongTien() != null ? hoaDonGoc.getTongTien() : BigDecimal.ZERO;
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

                // Quay lại màn hình danh sách và tải lại dữ liệu mới nhất
                taiTatCaHoaDon();
                hienThiManHinhDanhSach();
            }
        } catch (Exception ex) {
            hienThiLoi("Lỗi khi cập nhật hóa đơn: " + ex.getMessage());
        }
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

    /**
     * Hộp thoại xem chi tiết hóa đơn phục vụ chức năng Xác nhận Xóa / Hủy hóa đơn.
     * Cung cấp 2 nút [✕ Xóa hóa đơn] và [Hủy bỏ].
     */
    public static class XacNhanXoaHoaDonDialog extends Stage {
        private final HoaDon hoaDon;
        private final HoaDonController controller = new HoaDonController();
        private final Runnable khiXoaThanhCong;

        public XacNhanXoaHoaDonDialog(HoaDon hoaDon, Stage chuSoHuu, Runnable khiXoaThanhCong) {
            this.hoaDon = hoaDon;
            this.khiXoaThanhCong = khiXoaThanhCong;

            initModality(Modality.APPLICATION_MODAL);
            if (chuSoHuu != null) initOwner(chuSoHuu);
            setTitle("Xác nhận xóa / hủy hóa đơn - " + hoaDon.getMaHoaDon());

            VBox pnlGoc = new VBox(12);
            pnlGoc.setPadding(new Insets(16));
            pnlGoc.getStyleClass().add("khung-phan-he");

            // 1. Tiêu đề
            Label lblTieuDe = new Label("XÁC NHẬN XÓA / HỦY HÓA ĐƠN");
            lblTieuDe.getStyleClass().add("tieu-de-phan-he");
            lblTieuDe.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 15px;");

            Label lblCanhBao = new Label("Vui lòng kiểm tra kỹ thông tin chi tiết hóa đơn bên dưới trước khi xác nhận xóa / hủy.");
            lblCanhBao.setStyle("-fx-font-style: italic; -fx-text-fill: #64748b;");

            // 2. Thẻ thông tin hóa đơn
            GridPane luoiInfo = new GridPane();
            luoiInfo.setHgap(16);
            luoiInfo.setVgap(6);
            luoiInfo.setPadding(new Insets(10, 14, 10, 14));
            luoiInfo.getStyleClass().add("hop-noi-dung");

            luoiInfo.add(new Label("Mã hóa đơn:"), 0, 0);
            Label lblMa = new Label(hoaDon.getMaHoaDon());
            lblMa.setStyle("-fx-font-weight: bold; -fx-text-fill: #0284c7;");
            luoiInfo.add(lblMa, 1, 0);

            luoiInfo.add(new Label("Trạng thái:"), 2, 0);
            Label lblTt = new Label(hoaDon.getTrangThaiThanhToan());
            lblTt.setStyle("-fx-font-weight: bold; -fx-text-fill: #15803d;");
            luoiInfo.add(lblTt, 3, 0);

            luoiInfo.add(new Label("Ngày & Giờ lập:"), 0, 1);
            String ngayGio = (hoaDon.getNgayLap() != null ? hoaDon.getNgayLap().format(DINH_DANG_NGAY) : "--")
                    + " " + (hoaDon.getGioLap() != null ? hoaDon.getGioLap().format(DINH_DANG_GIO) : "");
            luoiInfo.add(new Label(ngayGio), 1, 1);

            luoiInfo.add(new Label("Thu ngân:"), 2, 1);
            luoiInfo.add(new Label(hoaDon.getTenNhanVien() != null ? hoaDon.getTenNhanVien() : hoaDon.getMaNhanVien()), 3, 1);

            luoiInfo.add(new Label("Khách hàng:"), 0, 2);
            String tenKh = (hoaDon.getTenKhachHang() != null ? hoaDon.getTenKhachHang() : "Khách vãng lai")
                    + (hoaDon.getSoDienThoaiKhachHang() != null ? " (" + hoaDon.getSoDienThoaiKhachHang() + ")" : "");
            luoiInfo.add(new Label(tenKh), 1, 2);

            luoiInfo.add(new Label("Hình thức TT:"), 2, 2);
            luoiInfo.add(new Label(hoaDon.getHinhThucThanhToan() != null ? hoaDon.getHinhThucThanhToan() : "--"), 3, 2);

            // 3. Bảng chi tiết mặt hàng
            ObservableList<DongHoaDon> dsDong = FXCollections.observableArrayList();
            int stt = 1;
            if (hoaDon.getDanhSachSanPham() != null) {
                for (ChiTietHoaDonSanPham sp : hoaDon.getDanhSachSanPham()) {
                    dsDong.add(new DongHoaDon(stt++, DongHoaDon.LoaiDong.SAN_PHAM, sp.getMaSanPham(),
                            sp.getTenSanPham() != null ? sp.getTenSanPham() : sp.getMaSanPham(),
                            sp.getDonViTinh() != null ? sp.getDonViTinh() : "Cái",
                            sp.getSoLuong(), sp.getDonGia(), ""));
                }
            }
            if (hoaDon.getDanhSachDichVu() != null) {
                for (ChiTietHoaDonDichVu dv : hoaDon.getDanhSachDichVu()) {
                    dsDong.add(new DongHoaDon(stt++, DongHoaDon.LoaiDong.DICH_VU, dv.getMaDichVu(),
                            dv.getTenDichVu() != null ? dv.getTenDichVu() : dv.getMaDichVu(),
                            "Lần", 1, dv.getDonGia(), ""));
                }
            }

            TableView<DongHoaDon> bang = new TableView<>(dsDong);
            bang.getStyleClass().add("bang-du-lieu");
            VBox.setVgrow(bang, Priority.ALWAYS);

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
            colTen.setPrefWidth(200);

            TableColumn<DongHoaDon, String> colDvt = new TableColumn<>("ĐVT");
            colDvt.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDonViTinh()));
            colDvt.setPrefWidth(60);
            colDvt.setStyle("-fx-alignment: center;");

            TableColumn<DongHoaDon, Integer> colSl = new TableColumn<>("SL");
            colSl.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSoLuong()));
            colSl.setPrefWidth(50);
            colSl.setStyle("-fx-alignment: center-right;");

            TableColumn<DongHoaDon, String> colGia = new TableColumn<>("Đơn giá");
            colGia.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().getDonGia())));
            colGia.setPrefWidth(100);
            colGia.setStyle("-fx-alignment: center-right;");

            TableColumn<DongHoaDon, String> colTien = new TableColumn<>("Thành tiền");
            colTien.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().getThanhTien())));
            colTien.setPrefWidth(110);
            colTien.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");

            bang.getColumns().addAll(colStt, colLoai, colMa, colTen, colDvt, colSl, colGia, colTien);

            // 4. Khung tổng kết và chú thích hoàn trả
            HBox hangTongKet = new HBox(20);
            hangTongKet.setAlignment(Pos.CENTER_LEFT);
            hangTongKet.setPadding(new Insets(8, 12, 8, 12));
            hangTongKet.getStyleClass().add("hop-noi-dung");

            Label lblGhiChu = new Label("⚠ Khi xóa, hệ thống sẽ hoàn trả số lượng sản phẩm vào kho và trừ điểm tích lũy của khách hàng.");
            lblGhiChu.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 11.5px;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label lblTong = new Label("Tổng tiền thanh toán: " + DINH_DANG_TIEN.format(hoaDon.getTongTien()));
            lblTong.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0369a1;");

            hangTongKet.getChildren().addAll(lblGhiChu, spacer, lblTong);

            // 5. Thanh nút: [Xóa hóa đơn] và [Hủy bỏ]
            HBox hangNut = new HBox(12);
            hangNut.setAlignment(Pos.CENTER_RIGHT);

            Button btnXacNhanXoa = new Button("✕ Xóa hóa đơn");
            btnXacNhanXoa.getStyleClass().add("nut-xoa");
            btnXacNhanXoa.setStyle("-fx-font-size: 13px; -fx-padding: 8 16;");
            btnXacNhanXoa.setOnAction(e -> {
                try {
                    boolean ok = controller.huyHoaDon(hoaDon.getMaHoaDon(), "Xóa hóa đơn từ giao diện quản lý");
                    if (ok) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Đã xóa / hủy hóa đơn " + hoaDon.getMaHoaDon()
                                + " thành công!\nToàn bộ tồn kho sản phẩm và điểm tích lũy của khách hàng đã được hoàn trả.");
                        alert.setHeaderText(null);
                        alert.showAndWait();

                        if (khiXoaThanhCong != null) {
                            khiXoaThanhCong.run();
                        }
                        close();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Không thể xóa hóa đơn: Thao tác thất bại.");
                        alert.setHeaderText(null);
                        alert.showAndWait();
                    }
                } catch (Exception ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Lỗi khi xóa hóa đơn: " + ex.getMessage());
                    alert.setHeaderText(null);
                    alert.showAndWait();
                }
            });

            Button btnHuyBo = new Button("Hủy bỏ");
            btnHuyBo.getStyleClass().add("nut-phu");
            btnHuyBo.setStyle("-fx-font-size: 13px; -fx-padding: 8 16;");
            btnHuyBo.setOnAction(e -> close());

            hangNut.getChildren().addAll(btnXacNhanXoa, btnHuyBo);

            pnlGoc.getChildren().addAll(lblTieuDe, lblCanhBao, luoiInfo, bang, hangTongKet, hangNut);

            Scene scene = new Scene(pnlGoc, 780, 560);
            if (CapNhatHoaDonView.class.getResource(CSS_CHUNG) != null) {
                scene.getStylesheets().add(CapNhatHoaDonView.class.getResource(CSS_CHUNG).toExternalForm());
            }
            setScene(scene);
            setResizable(false);
        }
    }
}
