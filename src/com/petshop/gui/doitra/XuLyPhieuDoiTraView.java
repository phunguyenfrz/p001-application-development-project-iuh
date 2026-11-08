package com.petshop.gui.doitra;

import com.petshop.bus.PhieuDoiTraBUS;
import com.petshop.entity.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Màn hình Xử lý đổi trả (RET_CONFIRM / DT_XU_LY).
 * Tuân thủ 100% Bộ chỉ thị kỹ thuật IUH:
 * - Kiểu tiền tệ: double (0% BigDecimal).
 * - Không nạp toàn bộ sản phẩm dưới CSDL lên ComboBox trên RAM; sử dụng ô quét mã vạch / tìm kiếm trực tiếp dưới DB.
 * - Khi duyệt phiếu: tự động hoàn tồn kho (soLuongTon + ?) và trừ điểm tích lũy của khách hàng.
 */
public class XuLyPhieuDoiTraView extends VBox {

    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 VNĐ");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PhieuDoiTraBUS controller = new PhieuDoiTraBUS();

    private final ObservableList<PhieuDoiTra> dsTatCaPhieu = FXCollections.observableArrayList();
    private final ObservableList<PhieuDoiTra> dsHienThi = FXCollections.observableArrayList();
    private final ObservableList<ChiTietPhieuDoiTra> dsChiTietTra = FXCollections.observableArrayList();

    private PhieuDoiTra phieuDangChon = null;
    private HoaDon hoaDonGocCuaPhieu = null;

    private final VBox pnlManHinhDanhSach = new VBox(12);
    private final VBox pnlManHinhChinhSua = new VBox(12);

    // 1. MÀN HÌNH DANH SÁCH
    private final TextField txtTimKiem = new TextField();
    private final ComboBox<String> cboLocTrangThai = new ComboBox<>();
    private final ComboBox<String> cboXuLySanPhamNhanh = new ComboBox<>();
    private final Button btnChonCapNhat = new Button("Cập nhật / Chi tiết phiếu");
    private final Button btnDuyetNhanh = new Button("Duyệt phiếu");
    private final Button btnTuChoiNhanh = new Button("Từ chối");
    private final Button btnLamMoiDanhSach = new Button("Làm mới");
    private final TableView<PhieuDoiTra> bangDanhSachPhieu = new TableView<>();
    private final Label lblThongKeChanBang = new Label("Tổng cộng: 0 phiếu đổi trả");

    // 2. MÀN HÌNH CHỈNH SỬA / CHI TIẾT
    private final Button btnQuayLaiDanhSach = new Button("Quay lại danh sách phiếu");
    private final Label lblTieuDeChinhSua = new Label("CHI TIẾT PHIẾU ĐỔI TRẢ");
    private final Label lblBadgeTrangThai = new Label();
    private final Label lblBannerTrangThai = new Label();

    private final Label lblMaPhieu = new Label("--");
    private final Label lblMaHoaDon = new Label("--");
    private final Label lblNgayLap = new Label("--");
    private final Label lblKhachHang = new Label("--");
    private final Label lblNhanVienLap = new Label("--");
    private final Label lblLoaiXuLyHienTai = new Label("--");

    private final TableView<ChiTietPhieuDoiTra> bangSanPhamDoiTra = new TableView<>();
    private final VBox boxThemSanPhamGoc = new VBox(8);
    private final ComboBox<ChiTietHoaDonSanPham> cboSanPhamGoc = new ComboBox<>();
    private final Spinner<Integer> spnSoLuongTraThem = new Spinner<>(1, 9999, 1);
    private final ComboBox<String> cboLyDoThem = new ComboBox<>();
    private final ComboBox<String> cboTinhTrangThem = new ComboBox<>();
    private final TextField txtGhiChuThem = new TextField();
    private final Button btnThemSanPham = new Button("+ Thêm vào phiếu");

    private final RadioButton rdoTraHang = new RadioButton("Trả hàng, hoàn tiền");
    private final RadioButton rdoDoiHang = new RadioButton("Đổi sang sản phẩm khác");
    private final ToggleGroup grpHinhThuc = new ToggleGroup();

    private final VBox pnlTheHoanTien = new VBox(10);
    private final ComboBox<String> cboHinhThucHoan = new ComboBox<>(FXCollections.observableArrayList("Tiền mặt", "Chuyển khoản", "Voucher"));
    private final Label lblTienHoanDuKien = new Label("0 VNĐ");

    private final VBox pnlTheDoiHang = new VBox(10);
    private final HBox hangThemDoi = new HBox(10);
    private final TextField txtMaVachSanPham = new TextField();
    private final Spinner<Integer> spnSoLuongDoi = new Spinner<>(1, 100, 1);
    private final Button btnThemSanPhamDoi = new Button("+ Thêm hàng đổi");
    private final Label lblCanhBaoTonKho = new Label();

    public static class DongDoiHang {
        private final SanPham sanPham;
        private final ChiTietPhieuDoiTra chiTietDoi;

        public DongDoiHang(SanPham sanPham, int soLuong) {
            this.sanPham = sanPham;
            this.chiTietDoi = new ChiTietPhieuDoiTra();
            this.chiTietDoi.setSanPhamDoi(sanPham);
            this.chiTietDoi.setDonGiaDoi(sanPham != null ? sanPham.getGiaBan() : 0.0);
            this.chiTietDoi.setSoLuongDoi(soLuong);
        }

        public SanPham getSanPham() { return sanPham; }
        public int getSoLuong() { return chiTietDoi.getSoLuongDoi(); }
        public void setSoLuong(int soLuong) {
            this.chiTietDoi.setSoLuongDoi(soLuong);
        }
        public double getDonGia() { return chiTietDoi.getDonGiaDoi(); }
        public double getThanhTien() { return chiTietDoi.tinhThanhTienDoi(); }
    }

    private final TableView<DongDoiHang> tblSanPhamDoiMoi = new TableView<>();
    private final ObservableList<DongDoiHang> dsSanPhamDoiMoi = FXCollections.observableArrayList();
    private final Label lblDongChenhLech = new Label("Không phát sinh chênh lệch (0 VNĐ)");

    private final Label lblTongTienTra = new Label("0 VNĐ");
    private final Label lblTongTienDoi = new Label("0 VNĐ");
    private final Label lblChenhLechCuoi = new Label("0 VNĐ");
    private final Button btnLuuCapNhat = new Button("Lưu cập nhật phiếu");
    private final Button btnInLaiPhieu = new Button("In lại phiếu");
    private final Button btnHuyBoSua = new Button("Quay lại danh sách");

    public XuLyPhieuDoiTraView() {
        setSpacing(14);
        getStyleClass().add("khung-phan-he");

        khoiTaoGiaoDien();
        dangKySuKien();
        taiDanhSachPhieu();
        chuyenManHinh(false);
    }

    private void khoiTaoGiaoDien() {
        khoiTaoManHinhDanhSach();
        khoiTaoManHinhChinhSua();
        getChildren().add(pnlManHinhDanhSach);
    }

    public static String chuanHoaHuongXuLyHienThi(String huongXuLy, String tinhTrang) {
        if (huongXuLy != null && !huongXuLy.isBlank()) {
            if ("Thu lại vào kho".equalsIgnoreCase(huongXuLy.trim())
                    || "Nhập lại kho bán".equalsIgnoreCase(huongXuLy.trim())) {
                return "Thu lại vào kho";
            }
            if ("Hủy bỏ".equalsIgnoreCase(huongXuLy.trim())
                    || "Tiêu hủy".equalsIgnoreCase(huongXuLy.trim())
                    || "Trả nhà cung cấp".equalsIgnoreCase(huongXuLy.trim())) {
                return "Hủy bỏ";
            }
        }
        return "Nguyên vẹn".equalsIgnoreCase(tinhTrang) ? "Thu lại vào kho" : "Hủy bỏ";
    }

    private void khoiTaoManHinhDanhSach() {
        pnlManHinhDanhSach.setSpacing(14);
        VBox.setVgrow(pnlManHinhDanhSach, Priority.ALWAYS);

        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("PHIẾU ĐỔI TRẢ - XỬ LÝ ĐỔI TRẢ");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Danh sách phiếu đổi trả hàng hóa. Khi duyệt phiếu, hệ thống tự động hoàn tồn kho và trừ điểm tích lũy của khách hàng.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        VBox pnlKhungBang = new VBox(10);
        pnlKhungBang.getStyleClass().add("hop-noi-dung");
        VBox.setVgrow(pnlKhungBang, Priority.ALWAYS);

        HBox pnlThanhCongCu = new HBox(10);
        pnlThanhCongCu.getStyleClass().add("thanh-tac-vu");
        pnlThanhCongCu.setAlignment(Pos.CENTER_LEFT);

        txtTimKiem.setPromptText("Tìm mã phiếu, mã HĐ, tên KH, SĐT...");
        txtTimKiem.setPrefWidth(230);

        cboLocTrangThai.getItems().addAll("Tất cả trạng thái", "Chờ xử lý", "Đã duyệt", "Từ chối");
        cboLocTrangThai.setValue("Tất cả trạng thái");
        cboLocTrangThai.setPrefWidth(135);
        cboLocTrangThai.setOnAction(e -> locDanhSach());

        cboXuLySanPhamNhanh.getItems().addAll("Thu lại vào kho", "Hủy bỏ");
        cboXuLySanPhamNhanh.setValue("Thu lại vào kho");
        cboXuLySanPhamNhanh.setPrefWidth(145);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnChonCapNhat.getStyleClass().add("nut-phu");
        btnChonCapNhat.setOnAction(e -> moManHinhChinhSua());

        btnDuyetNhanh.getStyleClass().add("nut-chinh");
        btnDuyetNhanh.setOnAction(e -> xuLyDuyetPhieu(layPhieuDangChon()));

        btnTuChoiNhanh.getStyleClass().add("nut-xoa");
        btnTuChoiNhanh.setOnAction(e -> xuLyTuChoiPhieu(layPhieuDangChon()));

        btnLamMoiDanhSach.getStyleClass().add("nut-phu");
        btnLamMoiDanhSach.setOnAction(e -> taiDanhSachPhieu());

        pnlThanhCongCu.getChildren().addAll(
                txtTimKiem,
                cboLocTrangThai,
                new Label("Xử lý SP hoàn:"), cboXuLySanPhamNhanh,
                spacer,
                btnChonCapNhat, btnDuyetNhanh, btnTuChoiNhanh, btnLamMoiDanhSach
        );

        taoBangDanhSachPhieu();
        VBox.setVgrow(bangDanhSachPhieu, Priority.ALWAYS);

        HBox pnlChanTrang = new HBox(12);
        pnlChanTrang.setPadding(new Insets(4, 4, 0, 4));
        lblThongKeChanBang.setStyle("-fx-font-weight: bold;");
        pnlChanTrang.getChildren().add(lblThongKeChanBang);

        pnlKhungBang.getChildren().addAll(pnlThanhCongCu, bangDanhSachPhieu, pnlChanTrang);
        pnlManHinhDanhSach.getChildren().addAll(pnlTieuDe, pnlKhungBang);
    }

    private void taoBangDanhSachPhieu() {
        bangDanhSachPhieu.setItems(dsHienThi);
        bangDanhSachPhieu.getStyleClass().add("bang-du-lieu");
        bangDanhSachPhieu.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        bangDanhSachPhieu.setPlaceholder(new Label("Không có phiếu đổi trả nào phù hợp."));

        TableColumn<PhieuDoiTra, Integer> colStt = new TableColumn<>("STT");
        colStt.setMaxWidth(45);
        colStt.setStyle("-fx-alignment: center;");
        colStt.setCellValueFactory(c -> new SimpleObjectProperty<>(dsHienThi.indexOf(c.getValue()) + 1));

        TableColumn<PhieuDoiTra, String> colMaP = new TableColumn<>("Mã phiếu");
        colMaP.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaPhieuDoiTra()));
        colMaP.setPrefWidth(125);
        colMaP.setStyle("-fx-font-weight: bold;");

        TableColumn<PhieuDoiTra, String> colMaHd = new TableColumn<>("Hóa đơn gốc");
        colMaHd.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaHoaDon()));
        colMaHd.setPrefWidth(125);

        TableColumn<PhieuDoiTra, String> colNgay = new TableColumn<>("Ngày lập");
        colNgay.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNgayLap() != null ? d.getValue().getNgayLap().format(DINH_DANG_NGAY) : ""));
        colNgay.setPrefWidth(90);

        TableColumn<PhieuDoiTra, String> colKhach = new TableColumn<>("Khách hàng");
        colKhach.setCellValueFactory(d -> {
            String ten = d.getValue().getTenKhachHang() != null ? d.getValue().getTenKhachHang() : "Khách lẻ";
            String sdt = d.getValue().getSoDienThoaiKhachHang();
            return new SimpleStringProperty(sdt != null && !sdt.isEmpty() ? ten + " (" + sdt + ")" : ten);
        });
        colKhach.setPrefWidth(160);

        TableColumn<PhieuDoiTra, String> colLoai = new TableColumn<>("Loại xử lý");
        colLoai.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLoaiXuLy() != null ? d.getValue().getLoaiXuLy() : "Hoàn tiền"));
        colLoai.setPrefWidth(95);

        TableColumn<PhieuDoiTra, String> colSpTra = new TableColumn<>("SP hoàn trả");
        colSpTra.setCellValueFactory(d -> {
            PhieuDoiTra p = d.getValue();
            if (p.getDanhSachSanPham() != null && !p.getDanhSachSanPham().isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (ChiTietPhieuDoiTra ct : p.getDanhSachSanPham()) {
                    if (ct.getSoLuong() > 0) {
                        if (!sb.isEmpty()) sb.append(", ");
                        String ten = ct.getTenSanPham() != null ? ct.getTenSanPham() : ct.getMaSanPham();
                        sb.append(ten).append(" (x").append(ct.getSoLuong()).append(")");
                    }
                }
                if (!sb.isEmpty()) return new SimpleStringProperty(sb.toString());
            }
            return new SimpleStringProperty("-");
        });
        colSpTra.setPrefWidth(180);

        TableColumn<PhieuDoiTra, String> colSpDoi = new TableColumn<>("SP đổi mới");
        colSpDoi.setCellValueFactory(d -> {
            PhieuDoiTra p = d.getValue();
            if (p.getDanhSachSanPham() != null) {
                StringBuilder sb = new StringBuilder();
                for (ChiTietPhieuDoiTra ct : p.getDanhSachSanPham()) {
                    if (ct.getMaSanPhamDoi() != null && !ct.getMaSanPhamDoi().isBlank() && ct.getSoLuongDoi() > 0) {
                        if (!sb.isEmpty()) sb.append(", ");
                        String ten = ct.getTenSanPhamDoi() != null ? ct.getTenSanPhamDoi() : ct.getMaSanPhamDoi();
                        sb.append(ten).append(" (x").append(ct.getSoLuongDoi()).append(")");
                    }
                }
                if (!sb.isEmpty()) return new SimpleStringProperty(sb.toString());
            }
            return new SimpleStringProperty("-");
        });
        colSpDoi.setPrefWidth(140);

        TableColumn<PhieuDoiTra, String> colXuLySp = new TableColumn<>("Xử lý SP hoàn");
        colXuLySp.setCellValueFactory(d -> {
            PhieuDoiTra p = d.getValue();
            String tt = p.getTrangThai();
            if ("Đã duyệt".equalsIgnoreCase(tt) || "Hoàn thành".equalsIgnoreCase(tt)) {
                if (p.getDanhSachSanPham() != null && !p.getDanhSachSanPham().isEmpty()) {
                    ChiTietPhieuDoiTra ct0 = p.getDanhSachSanPham().get(0);
                    return new SimpleStringProperty(chuanHoaHuongXuLyHienThi(ct0.getHuongXuLy(), ct0.getTinhTrang()));
                }
                return new SimpleStringProperty("Thu lại vào kho");
            }
            return new SimpleStringProperty("-");
        });
        colXuLySp.setPrefWidth(115);

        TableColumn<PhieuDoiTra, String> colTongTra = new TableColumn<>("Tổng hàng trả");
        colTongTra.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().tinhTongTienHangTra())));
        colTongTra.setPrefWidth(105);
        colTongTra.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");

        TableColumn<PhieuDoiTra, String> colHoan = new TableColumn<>("Tiền hoàn");
        colHoan.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().tinhTienHoan())));
        colHoan.setPrefWidth(95);
        colHoan.setStyle("-fx-alignment: center-right;");

        TableColumn<PhieuDoiTra, String> colBu = new TableColumn<>("Tiền bù");
        colBu.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().tinhTienBu())));
        colBu.setPrefWidth(90);
        colBu.setStyle("-fx-alignment: center-right;");

        TableColumn<PhieuDoiTra, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTrangThai()));
        colTrangThai.setPrefWidth(95);
        colTrangThai.setStyle("-fx-alignment: center; -fx-font-weight: bold;");

        bangDanhSachPhieu.getColumns().addAll(colStt, colMaP, colMaHd, colNgay, colKhach, colLoai, colSpTra, colSpDoi, colXuLySp, colTongTra, colHoan, colBu, colTrangThai);
    }

    private void khoiTaoManHinhChinhSua() {
        pnlManHinhChinhSua.setSpacing(10);
        VBox.setVgrow(pnlManHinhChinhSua, Priority.ALWAYS);

        HBox pnlHeaderSua = new HBox(12);
        pnlHeaderSua.setAlignment(Pos.CENTER_LEFT);

        btnQuayLaiDanhSach.getStyleClass().add("nut-phu");
        btnQuayLaiDanhSach.setOnAction(e -> chuyenManHinh(false));

        lblTieuDeChinhSua.getStyleClass().add("tieu-de-phan-he");
        lblBadgeTrangThai.setStyle("-fx-padding: 3 10; -fx-background-color: #e2e8f0; -fx-background-radius: 4px; -fx-font-weight: bold;");

        Region spacerH = new Region();
        HBox.setHgrow(spacerH, Priority.ALWAYS);

        pnlHeaderSua.getChildren().addAll(btnQuayLaiDanhSach, lblTieuDeChinhSua, lblBadgeTrangThai, spacerH);

        lblBannerTrangThai.getStyleClass().add("mo-ta-phan-he");
        lblBannerTrangThai.setMaxWidth(Double.MAX_VALUE);

        VBox cardThongTin = taoCardThongTinTomTat();
        VBox cardSanPhamTra = taoVungSanPhamTra();
        VBox cardHinhThucXuLy = taoVungHinhThucXuLy();
        HBox barTomTat = taoVungTomTatCoDinh();

        ScrollPane scroll = new ScrollPane();
        VBox noiDungCuon = new VBox(12, cardThongTin, cardSanPhamTra, cardHinhThucXuLy);
        noiDungCuon.setPadding(new Insets(0, 4, 10, 0));
        scroll.setContent(noiDungCuon);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #f4f6f8;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        pnlManHinhChinhSua.getChildren().addAll(pnlHeaderSua, lblBannerTrangThai, scroll, barTomTat);
    }

    private VBox taoCardThongTinTomTat() {
        VBox card = new VBox(6);
        card.getStyleClass().add("hop-noi-dung");

        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(6);

        grid.add(new Label("Mã phiếu:"), 0, 0);
        lblMaPhieu.setStyle("-fx-font-weight: bold;");
        grid.add(lblMaPhieu, 1, 0);

        grid.add(new Label("Hóa đơn gốc:"), 2, 0);
        lblMaHoaDon.setStyle("-fx-font-weight: bold;");
        grid.add(lblMaHoaDon, 3, 0);

        grid.add(new Label("Ngày lập:"), 4, 0);
        grid.add(lblNgayLap, 5, 0);

        grid.add(new Label("Khách hàng:"), 0, 1);
        lblKhachHang.setStyle("-fx-font-weight: bold;");
        grid.add(lblKhachHang, 1, 1);

        grid.add(new Label("Nhân viên lập:"), 2, 1);
        grid.add(lblNhanVienLap, 3, 1);

        grid.add(new Label("Hình thức xử lý:"), 4, 1);
        lblLoaiXuLyHienTai.setStyle("-fx-font-weight: bold;");
        grid.add(lblLoaiXuLyHienTai, 5, 1);

        card.getChildren().add(grid);
        return card;
    }

    private VBox taoVungSanPhamTra() {
        VBox card = new VBox(10);
        card.getStyleClass().add("hop-noi-dung");

        Label lblTieuDe = new Label("DANH SÁCH SẢN PHẨM ĐỔI TRẢ & TÌNH TRẠNG HÀNG");
        lblTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px;");

        bangSanPhamDoiTra.setItems(dsChiTietTra);
        bangSanPhamDoiTra.getStyleClass().add("bang-du-lieu");
        bangSanPhamDoiTra.setPrefHeight(180);
        bangSanPhamDoiTra.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        bangSanPhamDoiTra.setPlaceholder(new Label("Chưa có mặt hàng đổi trả nào trong phiếu."));

        TableColumn<ChiTietPhieuDoiTra, Integer> colStt = new TableColumn<>("STT");
        colStt.setMaxWidth(45);
        colStt.setStyle("-fx-alignment: center;");
        colStt.setCellValueFactory(c -> new SimpleObjectProperty<>(dsChiTietTra.indexOf(c.getValue()) + 1));

        TableColumn<ChiTietPhieuDoiTra, String> colTen = new TableColumn<>("Sản phẩm");
        colTen.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenSanPham()));
        colTen.setPrefWidth(180);

        TableColumn<ChiTietPhieuDoiTra, String> colGia = new TableColumn<>("Đơn giá");
        colGia.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().getDonGia())));
        colGia.setPrefWidth(95);
        colGia.setStyle("-fx-alignment: center-right;");

        TableColumn<ChiTietPhieuDoiTra, Integer> colSlTra = new TableColumn<>("SL đổi trả");
        colSlTra.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSoLuong()));
        colSlTra.setCellFactory(col -> new TableCell<>() {
            private final Spinner<Integer> spn = new Spinner<>(1, 9999, 1);
            private boolean dangCapNhat = false;
            {
                spn.setPrefWidth(75);
                spn.valueProperty().addListener((obs, cu, moi) -> {
                    if (dangCapNhat || moi == null) return;
                    int idx = getIndex();
                    if (getTableView() != null && idx >= 0 && idx < getTableView().getItems().size()) {
                        ChiTietPhieuDoiTra ct = getTableView().getItems().get(idx);
                        if (ct.getSoLuong() != moi) {
                            ct.setSoLuong(moi);
                            tinhToanLaiTaiChinh();
                            bangSanPhamDoiTra.refresh();
                        }
                    }
                });
            }
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                int idx = getIndex();
                if (empty || getTableView() == null || idx < 0 || idx >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    ChiTietPhieuDoiTra ct = getTableView().getItems().get(idx);
                    boolean choPhepSua = phieuDangChon != null && "Chờ xử lý".equalsIgnoreCase(phieuDangChon.getTrangThai());
                    spn.setDisable(!choPhepSua);
                    dangCapNhat = true;
                    try {
                        if (spn.getValueFactory() == null) {
                            spn.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, Math.max(1, ct.getSoLuong())));
                        } else {
                            spn.getValueFactory().setValue(Math.max(1, ct.getSoLuong()));
                        }
                    } finally {
                        dangCapNhat = false;
                    }
                    setGraphic(spn);
                }
            }
        });
        colSlTra.setPrefWidth(85);

        TableColumn<ChiTietPhieuDoiTra, String> colLyDo = new TableColumn<>("Lý do");
        colLyDo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLyDo()));
        colLyDo.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> cbo = new ComboBox<>(FXCollections.observableArrayList("Lỗi sản phẩm", "Hết hạn", "Không phù hợp", "Khác"));
            private boolean dangCapNhat = false;
            {
                cbo.setPrefWidth(140);
                cbo.setOnAction(e -> {
                    if (dangCapNhat) return;
                    int idx = getIndex();
                    if (getTableView() != null && idx >= 0 && idx < getTableView().getItems().size()) {
                        getTableView().getItems().get(idx).setLyDo(cbo.getValue());
                    }
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int idx = getIndex();
                if (empty || getTableView() == null || idx < 0 || idx >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    ChiTietPhieuDoiTra ct = getTableView().getItems().get(idx);
                    dangCapNhat = true;
                    try {
                        cbo.setValue(ct.getLyDo());
                    } finally {
                        dangCapNhat = false;
                    }
                    boolean choPhepSua = phieuDangChon != null && "Chờ xử lý".equalsIgnoreCase(phieuDangChon.getTrangThai());
                    cbo.setDisable(!choPhepSua);
                    setGraphic(cbo);
                }
            }
        });
        colLyDo.setPrefWidth(140);

        TableColumn<ChiTietPhieuDoiTra, String> colTinhTrang = new TableColumn<>("Tình trạng");
        colTinhTrang.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTinhTrang()));
        colTinhTrang.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> cbo = new ComboBox<>(FXCollections.observableArrayList("Nguyên vẹn", "Đã mở", "Hư hỏng"));
            private boolean dangCapNhat = false;
            {
                cbo.setPrefWidth(130);
                cbo.setOnAction(e -> {
                    if (dangCapNhat) return;
                    int idx = getIndex();
                    if (getTableView() != null && idx >= 0 && idx < getTableView().getItems().size()) {
                        getTableView().getItems().get(idx).setTinhTrang(cbo.getValue());
                    }
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int idx = getIndex();
                if (empty || getTableView() == null || idx < 0 || idx >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    ChiTietPhieuDoiTra ct = getTableView().getItems().get(idx);
                    dangCapNhat = true;
                    try {
                        cbo.setValue(ct.getTinhTrang());
                    } finally {
                        dangCapNhat = false;
                    }
                    boolean choPhepSua = phieuDangChon != null && "Chờ xử lý".equalsIgnoreCase(phieuDangChon.getTrangThai());
                    cbo.setDisable(!choPhepSua);
                    setGraphic(cbo);
                }
            }
        });
        colTinhTrang.setPrefWidth(130);

        TableColumn<ChiTietPhieuDoiTra, String> colGhiChu = new TableColumn<>("Ghi chú / Mô tả lỗi");
        colGhiChu.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGhiChu()));
        colGhiChu.setCellFactory(col -> new TableCell<>() {
            private final TextField txt = new TextField();
            private boolean dangCapNhat = false;
            {
                txt.textProperty().addListener((obs, cu, moi) -> {
                    if (dangCapNhat) return;
                    int idx = getIndex();
                    if (getTableView() != null && idx >= 0 && idx < getTableView().getItems().size()) {
                        getTableView().getItems().get(idx).setGhiChu(moi);
                    }
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                int idx = getIndex();
                if (empty || getTableView() == null || idx < 0 || idx >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    ChiTietPhieuDoiTra ct = getTableView().getItems().get(idx);
                    dangCapNhat = true;
                    try {
                        txt.setText(ct.getGhiChu() != null ? ct.getGhiChu() : "");
                    } finally {
                        dangCapNhat = false;
                    }
                    boolean choPhepSua = phieuDangChon != null && "Chờ xử lý".equalsIgnoreCase(phieuDangChon.getTrangThai());
                    txt.setDisable(!choPhepSua);
                    setGraphic(txt);
                }
            }
        });
        colGhiChu.setPrefWidth(160);

        TableColumn<ChiTietPhieuDoiTra, String> colThanhTien = new TableColumn<>("Thành tiền hoàn");
        colThanhTien.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().tinhThanhTienTra())));
        colThanhTien.setPrefWidth(110);
        colThanhTien.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");

        TableColumn<ChiTietPhieuDoiTra, Void> colXoa = new TableColumn<>("Thao tác");
        colXoa.setCellFactory(col -> new TableCell<>() {
            private final Button btnXoa = new Button("Xóa");
            {
                btnXoa.getStyleClass().add("nut-xoa");
                btnXoa.setStyle("-fx-font-size: 11px; -fx-padding: 2 10;");
                btnXoa.setOnAction(e -> {
                    if (dsChiTietTra.size() <= 1) {
                        hienCanhBao("Phiếu đổi trả phải có ít nhất một mặt hàng! Không thể xóa hết.");
                        return;
                    }
                    ChiTietPhieuDoiTra ct = getTableView().getItems().get(getIndex());
                    dsChiTietTra.remove(ct);
                    tinhToanLaiTaiChinh();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                boolean choPhepSua = phieuDangChon != null && "Chờ xử lý".equalsIgnoreCase(phieuDangChon.getTrangThai());
                setGraphic((empty || !choPhepSua) ? null : btnXoa);
            }
        });
        colXoa.setPrefWidth(65);

        bangSanPhamDoiTra.getColumns().addAll(colStt, colTen, colGia, colSlTra, colLyDo, colTinhTrang, colGhiChu, colThanhTien, colXoa);

        taoKhungThemSanPhamGoc();

        card.getChildren().addAll(lblTieuDe, bangSanPhamDoiTra, boxThemSanPhamGoc);
        return card;
    }

    private void taoKhungThemSanPhamGoc() {
        boxThemSanPhamGoc.getStyleClass().add("hop-noi-dung");
        HBox hang1 = new HBox(10);
        hang1.setAlignment(Pos.CENTER_LEFT);

        cboSanPhamGoc.setPromptText("-- Chọn sản phẩm từ HĐ gốc --");
        cboSanPhamGoc.setPrefWidth(240);

        cboLyDoThem.getItems().addAll("Lỗi sản phẩm", "Hết hạn", "Không phù hợp", "Khác");
        cboLyDoThem.setValue("Lỗi sản phẩm");
        cboLyDoThem.setPrefWidth(125);

        cboTinhTrangThem.getItems().addAll("Nguyên vẹn", "Đã mở", "Hư hỏng");
        cboTinhTrangThem.setValue("Nguyên vẹn");
        cboTinhTrangThem.setPrefWidth(110);

        spnSoLuongTraThem.setPrefWidth(75);
        btnThemSanPham.getStyleClass().add("nut-chinh");
        btnThemSanPham.setOnAction(e -> xuLyThemSanPhamGocVaoPhieu());

        hang1.getChildren().addAll(
                new Label("Thêm mặt hàng HĐ:"), cboSanPhamGoc,
                new Label("Lý do:"), cboLyDoThem,
                new Label("Tình trạng:"), cboTinhTrangThem,
                new Label("SL:"), spnSoLuongTraThem,
                btnThemSanPham
        );

        boxThemSanPhamGoc.getChildren().add(hang1);
    }

    private VBox taoVungHinhThucXuLy() {
        VBox card = new VBox(12);
        card.getStyleClass().add("hop-noi-dung");

        Label lblTieuDe = new Label("HÌNH THỨC XỬ LÝ ĐỔI TRẢ");
        lblTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px;");

        rdoTraHang.setToggleGroup(grpHinhThuc);
        rdoDoiHang.setToggleGroup(grpHinhThuc);
        rdoTraHang.setSelected(true);

        rdoTraHang.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1f2937;");
        rdoDoiHang.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1f2937;");

        HBox hangChon = new HBox(30, rdoTraHang, rdoDoiHang);
        hangChon.setAlignment(Pos.CENTER_LEFT);

        pnlTheHoanTien.setPadding(new Insets(10));
        pnlTheHoanTien.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 4px; -fx-background-radius: 4px;");
        cboHinhThucHoan.setValue("Tiền mặt");
        HBox hangHoan = new HBox(16);
        hangHoan.setAlignment(Pos.CENTER_LEFT);
        lblTienHoanDuKien.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        hangHoan.getChildren().addAll(
                new Label("Hình thức hoàn:"), cboHinhThucHoan,
                new Label("Số tiền hoàn trả khách:"), lblTienHoanDuKien
        );
        pnlTheHoanTien.getChildren().add(hangHoan);

        pnlTheDoiHang.setPadding(new Insets(10));
        pnlTheDoiHang.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 4px; -fx-background-radius: 4px;");
        pnlTheDoiHang.setManaged(false);
        pnlTheDoiHang.setVisible(false);

        hangThemDoi.setAlignment(Pos.CENTER_LEFT);
        txtMaVachSanPham.setPromptText("Quét mã vạch / nhập mã hoặc tên SP đổi rồi nhấn Enter...");
        txtMaVachSanPham.setPrefWidth(340);
        HBox.setHgrow(txtMaVachSanPham, Priority.ALWAYS);

        spnSoLuongDoi.setPrefWidth(70);
        btnThemSanPhamDoi.getStyleClass().add("nut-chinh");
        lblCanhBaoTonKho.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold;");

        hangThemDoi.getChildren().addAll(
                new Label("Mã vạch / Mã SP đổi:"), txtMaVachSanPham,
                new Label("SL:"), spnSoLuongDoi,
                btnThemSanPhamDoi, lblCanhBaoTonKho
        );

        tblSanPhamDoiMoi.getStyleClass().add("bang-du-lieu");
        tblSanPhamDoiMoi.setPrefHeight(130);
        tblSanPhamDoiMoi.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<DongDoiHang, String> colTenDoi = new TableColumn<>("Sản phẩm đổi");
        colTenDoi.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSanPham().getTenSanPham()));

        TableColumn<DongDoiHang, String> colGiaDoi = new TableColumn<>("Đơn giá");
        colGiaDoi.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().getDonGia())));
        colGiaDoi.setStyle("-fx-alignment: center-right;");

        TableColumn<DongDoiHang, Integer> colSlDoi = new TableColumn<>("SL");
        colSlDoi.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSoLuong()));
        colSlDoi.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");

        TableColumn<DongDoiHang, String> colTtDoi = new TableColumn<>("Thành tiền");
        colTtDoi.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().getThanhTien())));
        colTtDoi.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");

        TableColumn<DongDoiHang, Void> colXoaDoi = new TableColumn<>("Thao tác");
        colXoaDoi.setCellFactory(c -> new TableCell<>() {
            private final Button btnXoa = new Button("Xóa");
            {
                btnXoa.getStyleClass().add("nut-xoa");
                btnXoa.setStyle("-fx-font-size: 11px; -fx-padding: 2 10;");
                btnXoa.setOnAction(e -> {
                    DongDoiHang d = getTableView().getItems().get(getIndex());
                    dsSanPhamDoiMoi.remove(d);
                    tinhToanLaiTaiChinh();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                boolean choPhepSua = phieuDangChon != null && "Chờ xử lý".equalsIgnoreCase(phieuDangChon.getTrangThai());
                setGraphic((empty || !choPhepSua) ? null : btnXoa);
            }
        });
        colXoaDoi.setPrefWidth(65);

        tblSanPhamDoiMoi.getColumns().addAll(colTenDoi, colGiaDoi, colSlDoi, colTtDoi, colXoaDoi);
        tblSanPhamDoiMoi.setItems(dsSanPhamDoiMoi);

        lblDongChenhLech.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        pnlTheDoiHang.getChildren().addAll(hangThemDoi, tblSanPhamDoiMoi, lblDongChenhLech);

        card.getChildren().addAll(lblTieuDe, hangChon, pnlTheHoanTien, pnlTheDoiHang);
        return card;
    }

    private HBox taoVungTomTatCoDinh() {
        HBox bar = new HBox(16);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("hop-noi-dung");

        lblTongTienTra.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        lblTongTienDoi.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        lblChenhLechCuoi.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        HBox pnlThongKe = new HBox(18);
        pnlThongKe.setAlignment(Pos.CENTER_LEFT);
        pnlThongKe.getChildren().addAll(
                new Label("Tổng hàng trả:"), lblTongTienTra,
                new Label("Tổng hàng đổi:"), lblTongTienDoi,
                new Label("Chênh lệch:"), lblChenhLechCuoi
        );
        HBox.setHgrow(pnlThongKe, Priority.ALWAYS);

        btnHuyBoSua.getStyleClass().add("nut-phu");
        btnInLaiPhieu.getStyleClass().add("nut-chinh");
        btnLuuCapNhat.getStyleClass().add("nut-chinh");

        bar.getChildren().addAll(pnlThongKe, btnInLaiPhieu, btnHuyBoSua, btnLuuCapNhat);
        return bar;
    }

    private void dangKySuKien() {
        txtTimKiem.textProperty().addListener((obs, cu, moi) -> locDanhSach());

        bangDanhSachPhieu.getSelectionModel().selectedItemProperty().addListener((obs, cu, moi) -> {
            if (moi != null) {
                boolean choXuLy = "Chờ xử lý".equalsIgnoreCase(moi.getTrangThai());
                cboXuLySanPhamNhanh.setDisable(!choXuLy);
                btnDuyetNhanh.setDisable(!choXuLy);
                btnTuChoiNhanh.setDisable(!choXuLy);

                if (moi.getDanhSachSanPham() != null && !moi.getDanhSachSanPham().isEmpty()) {
                    ChiTietPhieuDoiTra ct0 = moi.getDanhSachSanPham().get(0);
                    cboXuLySanPhamNhanh.setValue(chuanHoaHuongXuLyHienThi(ct0.getHuongXuLy(), ct0.getTinhTrang()));
                }
            }
        });

        bangDanhSachPhieu.setRowFactory(tv -> {
            TableRow<PhieuDoiTra> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    moManHinhChinhSua();
                }
            });
            return row;
        });

        grpHinhThuc.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            boolean laTraHang = rdoTraHang.isSelected();
            pnlTheHoanTien.setVisible(laTraHang);
            pnlTheHoanTien.setManaged(laTraHang);
            pnlTheDoiHang.setVisible(!laTraHang);
            pnlTheDoiHang.setManaged(!laTraHang);
            tinhToanLaiTaiChinh();
        });

        txtMaVachSanPham.setOnAction(e -> themSanPhamDoi());
        btnThemSanPhamDoi.setOnAction(e -> themSanPhamDoi());
        btnLuuCapNhat.setOnAction(e -> xuLyLuuCapNhatPhieu());
        btnInLaiPhieu.setOnAction(e -> moHopThoaiXemVaIn(phieuDangChon));
        btnHuyBoSua.setOnAction(e -> chuyenManHinh(false));
    }

    private void themSanPhamDoi() {
        String maHoacTuKhoa = txtMaVachSanPham.getText();
        try {
            SanPham sp = controller.timSanPhamTheoMaVachHoacTuKhoa(maHoacTuKhoa);
            int sl = spnSoLuongDoi.getValue() != null ? spnSoLuongDoi.getValue() : 1;

            for (DongDoiHang d : dsSanPhamDoiMoi) {
                if (d.getSanPham().getMaSanPham().equalsIgnoreCase(sp.getMaSanPham())) {
                    int slMoi = d.getSoLuong() + 1;
                    if (slMoi > sp.getSoLuongTon()) {
                        hienCanhBao("Số lượng vượt quá tồn kho hiện tại (" + sp.getSoLuongTon() + ")!");
                        return;
                    }
                    d.setSoLuong(slMoi);
                    txtMaVachSanPham.clear();
                    tblSanPhamDoiMoi.refresh();
                    tinhToanLaiTaiChinh();
                    return;
                }
            }

            if (sl > sp.getSoLuongTon()) {
                hienCanhBao("Số lượng vượt quá tồn kho hiện tại (" + sp.getSoLuongTon() + ")!");
                return;
            }

            dsSanPhamDoiMoi.add(new DongDoiHang(sp, sl));
            lblCanhBaoTonKho.setText("Đã thêm: " + sp.getTenSanPham() + " (Tồn: " + sp.getSoLuongTon() + ")");
            txtMaVachSanPham.clear();
            tinhToanLaiTaiChinh();
        } catch (IllegalArgumentException ex) {
            hienCanhBao(ex.getMessage());
        }
    }

    private PhieuDoiTra taoPhieuTamDeTinhToan() {
        PhieuDoiTra phieuTam = new PhieuDoiTra();
        phieuTam.setLoaiXuLy(rdoTraHang.isSelected() ? "Hoàn tiền" : "Đổi hàng");
        List<ChiTietPhieuDoiTra> dsCt = new ArrayList<>();

        for (ChiTietPhieuDoiTra d : dsChiTietTra) {
            ChiTietPhieuDoiTra ct = new ChiTietPhieuDoiTra();
            ct.setMaSanPham(d.getMaSanPham());
            ct.setTenSanPham(d.getTenSanPham());
            ct.setDonGia(d.getDonGia());
            ct.setSoLuong(d.getSoLuong());
            dsCt.add(ct);
        }

        if (rdoDoiHang.isSelected() && !dsSanPhamDoiMoi.isEmpty()) {
            for (int i = 0; i < dsSanPhamDoiMoi.size(); i++) {
                DongDoiHang doi = dsSanPhamDoiMoi.get(i);
                ChiTietPhieuDoiTra target;
                if (i < dsCt.size()) {
                    target = dsCt.get(i);
                } else {
                    target = new ChiTietPhieuDoiTra();
                    target.setSoLuong(0);
                    target.setDonGia(0.0);
                    dsCt.add(target);
                }
                target.setSanPhamDoi(doi.getSanPham());
                target.setDonGiaDoi(doi.getDonGia());
                target.setSoLuongDoi(doi.getSoLuong());
            }
        }

        phieuTam.setDanhSachSanPham(dsCt);
        return phieuTam;
    }

    private void tinhToanLaiTaiChinh() {
        PhieuDoiTra phieuTam = taoPhieuTamDeTinhToan();
        double tongTra = phieuTam.tinhTongTienHangTra();
        double tongDoi = phieuTam.tinhTongTienHangDoi();
        double tienHoan = phieuTam.tinhTienHoan();
        double tienBu = phieuTam.tinhTienBu();

        lblTongTienTra.setText(DINH_DANG_TIEN.format(tongTra));
        lblTongTienDoi.setText(DINH_DANG_TIEN.format(tongDoi));
        lblTienHoanDuKien.setText(DINH_DANG_TIEN.format(tienHoan));

        if (rdoTraHang.isSelected()) {
            lblChenhLechCuoi.setText("Hoàn trả: " + DINH_DANG_TIEN.format(tienHoan));
        } else {
            if (tienBu > 0) {
                lblDongChenhLech.setText("Khách cần bù: " + DINH_DANG_TIEN.format(tienBu));
                lblChenhLechCuoi.setText("Khách bù: " + DINH_DANG_TIEN.format(tienBu));
            } else if (tienHoan > 0) {
                lblDongChenhLech.setText("Hoàn lại khách: " + DINH_DANG_TIEN.format(tienHoan));
                lblChenhLechCuoi.setText("Hoàn khách: " + DINH_DANG_TIEN.format(tienHoan));
            } else {
                lblDongChenhLech.setText("Không phát sinh chênh lệch (0 VNĐ)");
                lblChenhLechCuoi.setText("0 VNĐ");
            }
        }
    }

    public void taiDanhSachPhieu() {
        try {
            dsTatCaPhieu.clear();
            List<PhieuDoiTra> ds = controller.layTatCaPhieuDoiTra();
            if (ds != null) {
                dsTatCaPhieu.addAll(ds);
            }
            locDanhSach();
        } catch (Exception ex) {
            hienLoi("Lỗi khi tải danh sách phiếu đổi trả: " + ex.getMessage());
        }
    }

    private void locDanhSach() {
        String tuKhoa = txtTimKiem.getText() != null ? txtTimKiem.getText().trim().toLowerCase() : "";
        String trangThaiLoc = cboLocTrangThai.getValue();
        if ("Tất cả trạng thái".equals(trangThaiLoc)) trangThaiLoc = null;

        dsHienThi.clear();
        int demChoXuLy = 0;
        int demDaDuyet = 0;

        for (PhieuDoiTra p : dsTatCaPhieu) {
            String tt = p.getTrangThai();
            if ("Chờ xử lý".equalsIgnoreCase(tt)) demChoXuLy++;
            else if ("Đã duyệt".equalsIgnoreCase(tt) || "Hoàn thành".equalsIgnoreCase(tt)) demDaDuyet++;

            boolean hopTrangThai = (trangThaiLoc == null)
                    || ("Đã duyệt".equalsIgnoreCase(trangThaiLoc) && ("Đã duyệt".equalsIgnoreCase(tt) || "Hoàn thành".equalsIgnoreCase(tt)))
                    || trangThaiLoc.equalsIgnoreCase(tt);

            boolean hopTuKhoa = tuKhoa.isEmpty()
                    || (p.getMaPhieuDoiTra() != null && p.getMaPhieuDoiTra().toLowerCase().contains(tuKhoa))
                    || (p.getMaHoaDon() != null && p.getMaHoaDon().toLowerCase().contains(tuKhoa))
                    || (p.getTenKhachHang() != null && p.getTenKhachHang().toLowerCase().contains(tuKhoa))
                    || (p.getSoDienThoaiKhachHang() != null && p.getSoDienThoaiKhachHang().contains(tuKhoa));

            if (hopTrangThai && hopTuKhoa) {
                dsHienThi.add(p);
            }
        }

        lblThongKeChanBang.setText(String.format("Tổng cộng: %d phiếu (%d chờ xử lý, %d đã duyệt)",
                dsTatCaPhieu.size(), demChoXuLy, demDaDuyet));
    }

    private PhieuDoiTra layPhieuDangChon() {
        return bangDanhSachPhieu.getSelectionModel().getSelectedItem();
    }

    private void chuyenManHinh(boolean sangManHinhSua) {
        getChildren().clear();
        if (sangManHinhSua) {
            getChildren().add(pnlManHinhChinhSua);
        } else {
            getChildren().add(pnlManHinhDanhSach);
            taiDanhSachPhieu();
        }
    }

    private void moManHinhChinhSua() {
        PhieuDoiTra p = layPhieuDangChon();
        if (p == null) {
            hienCanhBao("Vui lòng chọn một phiếu đổi trả trong bảng danh sách để cập nhật/xem chi tiết!");
            return;
        }

        PhieuDoiTra phieuDayDu = controller.layPhieuDoiTraTheoMa(p.getMaPhieuDoiTra());
        this.phieuDangChon = phieuDayDu != null ? phieuDayDu : p;

        try {
            this.hoaDonGocCuaPhieu = controller.traCuuHoaDonDeDoiTra(phieuDangChon.getMaHoaDon());
        } catch (Exception ignored) {
            this.hoaDonGocCuaPhieu = null;
        }

        hienThiChiTietVaoForm();
        chuyenManHinh(true);
    }

    private void hienThiChiTietVaoForm() {
        lblMaPhieu.setText(phieuDangChon.getMaPhieuDoiTra());
        lblMaHoaDon.setText(phieuDangChon.getMaHoaDon());
        lblNgayLap.setText(phieuDangChon.getNgayLap() != null ? phieuDangChon.getNgayLap().format(DINH_DANG_NGAY) : "--");
        lblKhachHang.setText((phieuDangChon.getTenKhachHang() != null ? phieuDangChon.getTenKhachHang() : "Khách lẻ")
                + " (" + (phieuDangChon.getSoDienThoaiKhachHang() != null ? phieuDangChon.getSoDienThoaiKhachHang() : "--") + ")");
        lblNhanVienLap.setText(phieuDangChon.getMaNhanVien() != null ? phieuDangChon.getMaNhanVien() : "--");

        String tt = phieuDangChon.getTrangThai() != null ? phieuDangChon.getTrangThai() : "Chờ xử lý";
        lblBadgeTrangThai.setText(tt);

        boolean choPhepSua = "Chờ xử lý".equalsIgnoreCase(tt);

        if (choPhepSua) {
            lblBannerTrangThai.setText("Phiếu đang ở trạng thái [Chờ xử lý] - Cho phép điều chỉnh thông tin hàng trả, hàng đổi mới và hình thức xử lý.");
            boxThemSanPhamGoc.setVisible(true);
            boxThemSanPhamGoc.setManaged(true);
            hangThemDoi.setVisible(true);
            hangThemDoi.setManaged(true);
            rdoTraHang.setDisable(false);
            rdoDoiHang.setDisable(false);
            btnLuuCapNhat.setVisible(true);
            btnLuuCapNhat.setManaged(true);
            btnHuyBoSua.setText("Hủy bỏ");
            btnInLaiPhieu.setVisible(false);
            btnInLaiPhieu.setManaged(false);
            napComboBoxSanPhamGoc();
        } else {
            if ("Đã duyệt".equalsIgnoreCase(tt) || "Hoàn thành".equalsIgnoreCase(tt)) {
                lblBannerTrangThai.setText("Phiếu đổi trả đã được xác nhận [Đã duyệt] - Dữ liệu đã được khóa, không thể chỉnh sửa.");
            } else {
                lblBannerTrangThai.setText("Phiếu đổi trả đã bị [Từ chối] (Lý do: " + (phieuDangChon.getLyDo() != null ? phieuDangChon.getLyDo() : "Không đạt") + ") - Không thể chỉnh sửa.");
            }
            boxThemSanPhamGoc.setVisible(false);
            boxThemSanPhamGoc.setManaged(false);
            hangThemDoi.setVisible(false);
            hangThemDoi.setManaged(false);
            rdoTraHang.setDisable(true);
            rdoDoiHang.setDisable(true);
            btnLuuCapNhat.setVisible(false);
            btnLuuCapNhat.setManaged(false);
            btnHuyBoSua.setText("Quay lại danh sách");
            btnInLaiPhieu.setVisible(true);
            btnInLaiPhieu.setManaged(true);
        }

        boolean laDoi = phieuDangChon.getLoaiXuLy() != null
                && (phieuDangChon.getLoaiXuLy().toLowerCase().contains("đổi") || phieuDangChon.getLoaiXuLy().toLowerCase().contains("doi"));
        lblLoaiXuLyHienTai.setText(laDoi ? "Đổi sang sản phẩm khác" : "Hoàn tiền");

        if (laDoi) {
            rdoDoiHang.setSelected(true);
        } else {
            rdoTraHang.setSelected(true);
        }

        dsChiTietTra.clear();
        if (phieuDangChon.getDanhSachSanPham() != null) {
            for (ChiTietPhieuDoiTra ct : phieuDangChon.getDanhSachSanPham()) {
                if (ct.getMaSanPham() != null && !ct.getMaSanPham().isBlank() && ct.getSoLuong() > 0) {
                    ChiTietPhieuDoiTra copy = new ChiTietPhieuDoiTra(
                            ct.getMaPhieuDoiTra(), ct.getMaHoaDon(), ct.getMaSanPham(), ct.getTenSanPham(),
                            ct.getSoLuong(), ct.getDonGia(), ct.getLyDo(), ct.getTinhTrang(),
                            ct.getHuongXuLy(), ct.getGhiChu()
                    );
                    copy.setMaSanPhamDoi(ct.getMaSanPhamDoi());
                    copy.setTenSanPhamDoi(ct.getTenSanPhamDoi());
                    copy.setSoLuongDoi(ct.getSoLuongDoi());
                    copy.setDonGiaDoi(ct.getDonGiaDoi());
                    dsChiTietTra.add(copy);
                }
            }
        }

        dsSanPhamDoiMoi.clear();
        if (phieuDangChon.getDanhSachSanPham() != null) {
            for (ChiTietPhieuDoiTra ct : phieuDangChon.getDanhSachSanPham()) {
                if (ct.getMaSanPhamDoi() != null && !ct.getMaSanPhamDoi().isBlank() && ct.getSoLuongDoi() > 0) {
                    SanPham spDoi = new SanPham();
                    spDoi.setMaSanPham(ct.getMaSanPhamDoi());
                    spDoi.setTenSanPham(ct.getTenSanPhamDoi() != null ? ct.getTenSanPhamDoi() : ct.getMaSanPhamDoi());
                    spDoi.setGiaBan(ct.getDonGiaDoi());
                    spDoi.setSoLuongTon(9999);
                    dsSanPhamDoiMoi.add(new DongDoiHang(spDoi, ct.getSoLuongDoi()));
                }
            }
        }

        bangSanPhamDoiTra.refresh();
        tblSanPhamDoiMoi.refresh();
        tinhToanLaiTaiChinh();
    }

    private void napComboBoxSanPhamGoc() {
        cboSanPhamGoc.getItems().clear();
        if (hoaDonGocCuaPhieu != null && hoaDonGocCuaPhieu.getDanhSachSanPham() != null) {
            cboSanPhamGoc.getItems().addAll(hoaDonGocCuaPhieu.getDanhSachSanPham());
            cboSanPhamGoc.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(ChiTietHoaDonSanPham item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getTenSanPham() + " (SL mua: " + item.getSoLuong() + ")");
                }
            });
            cboSanPhamGoc.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(ChiTietHoaDonSanPham item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getTenSanPham() + " (SL mua: " + item.getSoLuong() + ")");
                }
            });
            if (!cboSanPhamGoc.getItems().isEmpty()) {
                cboSanPhamGoc.setValue(cboSanPhamGoc.getItems().get(0));
            }
        }
    }

    private void xuLyThemSanPhamGocVaoPhieu() {
        ChiTietHoaDonSanPham spGoc = cboSanPhamGoc.getValue();
        if (spGoc == null) {
            hienCanhBao("Vui lòng chọn một sản phẩm từ hóa đơn gốc!");
            return;
        }

        int slThem = spnSoLuongTraThem.getValue();
        String lyDo = cboLyDoThem.getValue();
        String tinhTrang = cboTinhTrangThem.getValue();

        for (ChiTietPhieuDoiTra ct : dsChiTietTra) {
            if (ct.getMaSanPham().equalsIgnoreCase(spGoc.getMaSanPham())) {
                ct.setSoLuong(ct.getSoLuong() + slThem);
                ct.setLyDo(lyDo);
                ct.setTinhTrang(tinhTrang);
                bangSanPhamDoiTra.refresh();
                tinhToanLaiTaiChinh();
                return;
            }
        }

        double donGiaGoc = spGoc.getDonGia();
        ChiTietPhieuDoiTra dongMoi = new ChiTietPhieuDoiTra(
                phieuDangChon.getMaPhieuDoiTra(), phieuDangChon.getMaHoaDon(),
                spGoc.getMaSanPham(), spGoc.getTenSanPham(), slThem, donGiaGoc,
                lyDo, tinhTrang, null, txtGhiChuThem.getText()
        );
        dsChiTietTra.add(dongMoi);
        tinhToanLaiTaiChinh();
    }

    private void xuLyLuuCapNhatPhieu() {
        if (phieuDangChon == null) return;
        if (!"Chờ xử lý".equalsIgnoreCase(phieuDangChon.getTrangThai())) {
            hienCanhBao("Phiếu đã duyệt/xác nhận, không thể chỉnh sửa!");
            return;
        }

        if (dsChiTietTra.isEmpty()) {
            hienCanhBao("Phiếu đổi trả phải có ít nhất một mặt hàng!");
            return;
        }

        if (rdoDoiHang.isSelected() && dsSanPhamDoiMoi.isEmpty()) {
            hienCanhBao("Bạn đã chọn hình thức 'Đổi sang sản phẩm khác' nhưng chưa thêm sản phẩm đổi nào!");
            return;
        }

        int tongSoLuongTra = 0;
        List<ChiTietPhieuDoiTra> dsLuu = new ArrayList<>();
        for (ChiTietPhieuDoiTra ct : dsChiTietTra) {
            tongSoLuongTra += ct.getSoLuong();
            ct.setSanPhamDoi(null);
            ct.setSoLuongDoi(0);
            ct.setDonGiaDoi(0.0);
            dsLuu.add(ct);
        }

        if (rdoDoiHang.isSelected() && !dsSanPhamDoiMoi.isEmpty() && !dsLuu.isEmpty()) {
            int soMatHangTra = dsLuu.size();
            for (int i = 0; i < dsSanPhamDoiMoi.size(); i++) {
                DongDoiHang doi = dsSanPhamDoiMoi.get(i);
                if (i < soMatHangTra) {
                    ChiTietPhieuDoiTra ctTarget = dsLuu.get(i);
                    ctTarget.setSanPhamDoi(doi.getSanPham());
                    ctTarget.setSoLuongDoi(doi.getSoLuong());
                    ctTarget.setDonGiaDoi(doi.getDonGia());
                } else {
                    ChiTietPhieuDoiTra ctPhu = new ChiTietPhieuDoiTra();
                    ctPhu.setMaPhieuDoiTra(phieuDangChon.getMaPhieuDoiTra());
                    ctPhu.setMaHoaDon(phieuDangChon.getMaHoaDon());
                    ctPhu.setSoLuong(0);
                    ctPhu.setDonGia(0.0);
                    ctPhu.setSanPhamDoi(doi.getSanPham());
                    ctPhu.setSoLuongDoi(doi.getSoLuong());
                    ctPhu.setDonGiaDoi(doi.getDonGia());
                    dsLuu.add(ctPhu);
                }
            }
        }

        phieuDangChon.setSoLuong(tongSoLuongTra);
        if (rdoTraHang.isSelected()) {
            phieuDangChon.setLoaiXuLy("Hoàn tiền");
            phieuDangChon.setHinhThucHoan(cboHinhThucHoan.getValue());
        } else {
            phieuDangChon.setLoaiXuLy("Đổi hàng");
            phieuDangChon.setHinhThucHoan(null);
        }

        try {
            phieuDangChon.getDanhSachSanPham().clear();
            phieuDangChon.getDanhSachSanPham().addAll(dsLuu);

            boolean kq = controller.capNhatPhieuDoiTra(phieuDangChon);
            if (kq) {
                hienThanhCong("Đã lưu cập nhật phiếu đổi trả thành công (" + dsChiTietTra.size() + " mặt hàng hoàn trả)!");
                chuyenManHinh(false);
            }
        } catch (Exception ex) {
            hienLoi("Lỗi khi lưu cập nhật phiếu: " + ex.getMessage());
        }
    }

    private void xuLyDuyetPhieu(PhieuDoiTra phieu) {
        if (phieu == null) {
            hienCanhBao("Vui lòng chọn một phiếu đổi trả để duyệt!");
            return;
        }

        PhieuDoiTra phieuDayDu = controller.layPhieuDoiTraTheoMa(phieu.getMaPhieuDoiTra());
        if (phieuDayDu != null) {
            phieu = phieuDayDu;
        }

        if (!"Chờ xử lý".equalsIgnoreCase(phieu.getTrangThai())) {
            hienCanhBao("Phiếu đổi trả đã ở trạng thái [" + phieu.getTrangThai() + "], không thể duyệt lại!");
            return;
        }

        String xuLySpChon = cboXuLySanPhamNhanh.getValue() != null ? cboXuLySanPhamNhanh.getValue() : "Thu lại vào kho";
        int diemTruDuKien = controller.tinhSoDiemTichLuyCanTru(phieu);

        StringBuilder dsSpHoan = new StringBuilder();
        int demSp = 0;
        if (phieu.getDanhSachSanPham() != null) {
            for (ChiTietPhieuDoiTra ct : phieu.getDanhSachSanPham()) {
                if (ct.getSoLuong() > 0) {
                    demSp++;
                    dsSpHoan.append("  + ")
                            .append(ct.getTenSanPham() != null ? ct.getTenSanPham() : ct.getMaSanPham())
                            .append(" (SL hoàn: ").append(ct.getSoLuong()).append(")\n");
                }
            }
        }

        Alert xacNhan = new Alert(Alert.AlertType.CONFIRMATION);
        xacNhan.setTitle("Xác nhận duyệt phiếu & Xử lý sản phẩm hoàn");
        xacNhan.setHeaderText("Duyệt phiếu đổi trả: " + phieu.getMaPhieuDoiTra() + " (" + demSp + " mặt hàng)");
        xacNhan.setContentText("Danh sách mặt hàng khách hoàn trả:\n" + dsSpHoan
                + "\nChọn hướng xử lý đối với toàn bộ " + demSp + " sản phẩm khách hoàn trả:\n"
                + "• Thu lại vào kho: Cộng lại số lượng từng sản phẩm hoàn vào tồn kho (soLuongTon + ?).\n"
                + "• Hủy bỏ: Hủy sản phẩm hoàn, không cộng lại vào tồn kho.\n"
                + "• Điểm tích lũy khách hàng tự động trừ khi duyệt: " + diemTruDuKien + " điểm.\n\n"
                + "(Mặc định đang chọn trên thanh công cụ: " + xuLySpChon + ")");

        ButtonType btnThuLaiKho = new ButtonType("Duyệt & Thu lại vào kho");
        ButtonType btnHuyBoSp = new ButtonType("Duyệt & Hủy bỏ SP");
        ButtonType btnHuy = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);
        xacNhan.getButtonTypes().setAll(btnThuLaiKho, btnHuyBoSp, btnHuy);

        Optional<ButtonType> ketQua = xacNhan.showAndWait();
        if (ketQua.isEmpty() || ketQua.get() == btnHuy) {
            return;
        }

        String huongXuLyQuyetDinh = (ketQua.get() == btnThuLaiKho) ? "Thu lại vào kho" : "Hủy bỏ";

        try {
            boolean kq = controller.duyetPhieuDoiTra(phieu.getMaPhieuDoiTra(), huongXuLyQuyetDinh);
            if (kq) {
                hienThanhCong("Đã duyệt phiếu đổi trả thành công!\n"
                        + "• Đã xử lý (" + huongXuLyQuyetDinh + ") cho " + demSp + " mặt hàng:\n" + dsSpHoan
                        + "• Đã trừ điểm tích lũy khách hàng: " + diemTruDuKien + " điểm.");
                chuyenManHinh(false);
            }
        } catch (Exception ex) {
            hienLoi("Lỗi duyệt phiếu: " + ex.getMessage());
        }
    }

    private void xuLyTuChoiPhieu(PhieuDoiTra phieu) {
        if (phieu == null) {
            hienCanhBao("Vui lòng chọn một phiếu đổi trả để từ chối!");
            return;
        }

        if (!"Chờ xử lý".equalsIgnoreCase(phieu.getTrangThai())) {
            hienCanhBao("Phiếu đổi trả đã ở trạng thái [" + phieu.getTrangThai() + "], không thể từ chối!");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Từ chối phiếu đổi trả");
        dialog.setHeaderText("Nhập lý do từ chối phiếu " + phieu.getMaPhieuDoiTra() + ":");
        dialog.setContentText("Lý do từ chối:");

        dialog.showAndWait().ifPresent(lyDo -> {
            if (lyDo.trim().isEmpty()) {
                hienCanhBao("Bắt buộc phải nhập lý do từ chối!");
                return;
            }
            try {
                boolean kq = controller.tuChoiPhieuDoiTra(phieu.getMaPhieuDoiTra(), lyDo.trim());
                if (kq) {
                    hienThanhCong("Đã từ chối phiếu đổi trả!");
                    chuyenManHinh(false);
                }
            } catch (Exception ex) {
                hienLoi("Lỗi từ chối phiếu: " + ex.getMessage());
            }
        });
    }

    private void moHopThoaiXemVaIn(PhieuDoiTra phieu) {
        if (phieu == null) return;
        new XemChiTietPhieuDoiTraView(phieu).showAndWait();
    }

    private void hienThanhCong(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void hienCanhBao(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void hienLoi(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
