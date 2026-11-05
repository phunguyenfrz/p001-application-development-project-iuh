package com.petshop.gui.doitra;

import com.petshop.bus.PhieuDoiTraBUS;
import com.petshop.entity.ChiTietPhieuDoiTra;
import com.petshop.entity.PhieuDoiTra;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Màn hình 3: Tìm kiếm & Tra cứu phiếu đổi trả (RET_SEARCH / DT_TIM_KIEM).
 * Tuân thủ Bộ chỉ thị kỹ thuật IUH: 100% tiền tệ dùng kiểu nguyên thủy double.
 */
public class TimKiemPhieuDoiTraView extends VBox {

    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 VNĐ");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PhieuDoiTraBUS controller;

    private final VBox pnlManHinhDanhSach = new VBox(14);
    private final VBox pnlManHinhChiTiet = new VBox(12);

    // 1. MÀN HÌNH DANH SÁCH
    private final TextField txtTuKhoa = new TextField();
    private final DatePicker dpTuNgay = new DatePicker();
    private final DatePicker dpDenNgay = new DatePicker();
    private final ComboBox<String> cboTrangThai = new ComboBox<>(FXCollections.observableArrayList(
            "Tất cả trạng thái", "Chờ xử lý", "Đã duyệt", "Từ chối"
    ));
    private final ComboBox<String> cboLoaiXuLy = new ComboBox<>(FXCollections.observableArrayList(
            "Tất cả loại", "Hoàn tiền", "Đổi hàng"
    ));
    private final Button btnTimKiem = new Button("Tìm kiếm");
    private final Button btnDatLai = new Button("Đặt lại");
    private final Button btnXuatExcel = new Button("Xuất file");

    private final Label lblKpiTongSoPhieu = new Label("0");
    private final Label lblKpiTongTienHoan = new Label("0 VNĐ");
    private final Label lblKpiTongTienBu = new Label("0 VNĐ");
    private final Label lblKpiTyLeHoanThanh = new Label("0%");

    private final TableView<PhieuDoiTra> tblKetQua = new TableView<>();
    private final ObservableList<PhieuDoiTra> dsKetQua = FXCollections.observableArrayList();

    // 2. MÀN HÌNH XEM CHI TIẾT
    private PhieuDoiTra phieuDangXem = null;

    private final Button btnQuayLaiDanhSach = new Button("← Quay lại danh sách phiếu");
    private final Label lblTieuDeChiTiet = new Label("CHI TIẾT PHIẾU ĐỔI TRẢ");
    private final Label lblBadgeTrangThai = new Label();
    private final Label lblBannerTrangThai = new Label();

    private final Label lblMaPhieu = new Label();
    private final Label lblMaHoaDon = new Label();
    private final Label lblNgayLap = new Label();
    private final Label lblKhachHang = new Label();
    private final Label lblSoDienThoai = new Label();
    private final Label lblNhanVien = new Label();
    private final Label lblHinhThucXuLy = new Label();

    private final TableView<ChiTietPhieuDoiTra> tblSanPhamTra = new TableView<>();
    private final ObservableList<ChiTietPhieuDoiTra> dsSanPhamTra = FXCollections.observableArrayList();

    private final ToggleGroup grpHinhThuc = new ToggleGroup();
    private final RadioButton rdoTraHang = new RadioButton("Trả hàng, hoàn tiền");
    private final RadioButton rdoDoiHang = new RadioButton("Đổi sang sản phẩm khác");
    private final VBox pnlTheHoanTien = new VBox(8);
    private final Label lblHinhThucHoan = new Label("Tiền mặt");
    private final Label lblTienHoanDuKien = new Label("0 VNĐ");

    private final VBox pnlTheDoiHang = new VBox(10);
    private final TableView<DongDoiHang> tblSanPhamDoiMoi = new TableView<>();
    private final ObservableList<DongDoiHang> dsSanPhamDoiMoi = FXCollections.observableArrayList();
    private final Label lblDongChenhLech = new Label();

    private final Label lblTongTienHangTra = new Label("Tổng hàng trả: 0 VNĐ");
    private final Label lblTongTienHangDoi = new Label("Tổng hàng đổi: 0 VNĐ");
    private final Label lblChenhLechTaiChinh = new Label("Chênh lệch: 0 VNĐ");
    private final Button btnInLaiPhieu = new Button("In lại phiếu");
    private final Button btnDongChiTiet = new Button("Quay lại");

    public TimKiemPhieuDoiTraView() {
        this.controller = new PhieuDoiTraBUS();
        khoiTaoGiaoDien();
        dangKySuKien();
        napDuLieuMacDinh();
    }

    private void khoiTaoGiaoDien() {
        this.setSpacing(14);
        this.getStyleClass().add("khung-phan-he");

        khoiTaoManHinhDanhSach();
        khoiTaoManHinhChiTiet();

        this.getChildren().add(pnlManHinhDanhSach);
    }

    private void khoiTaoManHinhDanhSach() {
        pnlManHinhDanhSach.setSpacing(14);
        VBox.setVgrow(pnlManHinhDanhSach, Priority.ALWAYS);

        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("PHIẾU ĐỔI TRẢ - TÌM KIẾM PHIẾU ĐỔI TRẢ");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Tra cứu phiếu đổi trả theo mã phiếu, hóa đơn gốc, khách hàng, thời gian và trạng thái xử lý.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        VBox cardBoLoc = taoCardBoLoc();
        HBox pnlKpi = taoPnlKpi();
        VBox cardBang = taoCardBangKetQua();
        VBox.setVgrow(cardBang, Priority.ALWAYS);

        pnlManHinhDanhSach.getChildren().addAll(pnlTieuDe, cardBoLoc, pnlKpi, cardBang);
    }

    private VBox taoCardBoLoc() {
        VBox card = new VBox(10);
        card.getStyleClass().add("hop-noi-dung");

        Label lblTieuDe = new Label("BỘ LỌC TRA CỨU PHIẾU ĐỔI TRẢ");
        lblTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px;");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(10);

        txtTuKhoa.setPromptText("Mã phiếu, Mã HĐ, Tên KH, SĐT...");
        txtTuKhoa.setPrefWidth(220);

        dpTuNgay.setPromptText("Từ ngày");
        dpTuNgay.setPrefWidth(140);
        dpDenNgay.setPromptText("Đến ngày");
        dpDenNgay.setPrefWidth(140);

        cboTrangThai.setValue("Tất cả trạng thái");
        cboTrangThai.setPrefWidth(160);

        cboLoaiXuLy.setValue("Tất cả loại");
        cboLoaiXuLy.setPrefWidth(140);

        grid.add(new Label("Từ khóa:"), 0, 0);
        grid.add(txtTuKhoa, 1, 0);
        grid.add(new Label("Từ ngày:"), 2, 0);
        grid.add(dpTuNgay, 3, 0);
        grid.add(new Label("Đến ngày:"), 4, 0);
        grid.add(dpDenNgay, 5, 0);

        grid.add(new Label("Trạng thái:"), 0, 1);
        grid.add(cboTrangThai, 1, 1);
        grid.add(new Label("Loại xử lý:"), 2, 1);
        grid.add(cboLoaiXuLy, 3, 1);

        HBox hangNut = new HBox(10);
        hangNut.setAlignment(Pos.CENTER_LEFT);
        btnTimKiem.getStyleClass().add("nut-chinh");
        btnDatLai.getStyleClass().add("nut-phu");
        btnXuatExcel.getStyleClass().add("nut-phu");
        hangNut.getChildren().addAll(btnTimKiem, btnDatLai, btnXuatExcel);

        grid.add(hangNut, 4, 1, 2, 1);

        card.getChildren().addAll(lblTieuDe, grid);
        return card;
    }

    private HBox taoPnlKpi() {
        HBox bar = new HBox(14);
        bar.setAlignment(Pos.CENTER_LEFT);

        VBox kpi1 = taoTheKpi("TỔNG PHIẾU ĐỔI TRẢ", lblKpiTongSoPhieu, "#2563eb");
        VBox kpi2 = taoTheKpi("TỔNG TIỀN HOÀN", lblKpiTongTienHoan, "#dc2626");
        VBox kpi3 = taoTheKpi("TỔNG TIỀN BÙ", lblKpiTongTienBu, "#0284c7");
        VBox kpi4 = taoTheKpi("TỶ LỆ DUYỆT XONG", lblKpiTyLeHoanThanh, "#16a34a");

        HBox.setHgrow(kpi1, Priority.ALWAYS);
        HBox.setHgrow(kpi2, Priority.ALWAYS);
        HBox.setHgrow(kpi3, Priority.ALWAYS);
        HBox.setHgrow(kpi4, Priority.ALWAYS);

        bar.getChildren().addAll(kpi1, kpi2, kpi3, kpi4);
        return bar;
    }

    private VBox taoTheKpi(String tieuDe, Label lblGiaTri, String mauSac) {
        VBox box = new VBox(4);
        box.getStyleClass().add("hop-noi-dung");
        box.setPadding(new Insets(10, 14, 10, 14));

        Label lbl = new Label(tieuDe);
        lbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #64748b;");

        lblGiaTri.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + mauSac + ";");

        box.getChildren().addAll(lbl, lblGiaTri);
        return box;
    }

    private VBox taoCardBangKetQua() {
        VBox card = new VBox(8);
        card.getStyleClass().add("hop-noi-dung");

        Label lblTieuDe = new Label("DANH SÁCH KẾT QUẢ TRA CỨU");
        lblTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px;");

        tblKetQua.getStyleClass().add("bang-du-lieu");
        tblKetQua.setPlaceholder(new Label("Không tìm thấy phiếu đổi trả nào phù hợp với bộ lọc."));
        tblKetQua.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tblKetQua, Priority.ALWAYS);

        TableColumn<PhieuDoiTra, String> colMa = new TableColumn<>("Mã phiếu");
        colMa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaPhieuDoiTra()));
        colMa.setPrefWidth(130);

        TableColumn<PhieuDoiTra, String> colMaHD = new TableColumn<>("Hóa đơn gốc");
        colMaHD.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaHoaDon()));
        colMaHD.setPrefWidth(125);

        TableColumn<PhieuDoiTra, String> colNgay = new TableColumn<>("Ngày lập");
        colNgay.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getNgayLap() != null ? d.getValue().getNgayLap().format(DINH_DANG_NGAY) : "--"));
        colNgay.setPrefWidth(90);

        TableColumn<PhieuDoiTra, String> colKhach = new TableColumn<>("Khách hàng");
        colKhach.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTenKhachHang() != null ? d.getValue().getTenKhachHang() : "Khách lẻ"));
        colKhach.setPrefWidth(120);

        TableColumn<PhieuDoiTra, String> colSdt = new TableColumn<>("SĐT");
        colSdt.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getSoDienThoaiKhachHang() != null ? d.getValue().getSoDienThoaiKhachHang() : "--"));
        colSdt.setPrefWidth(95);

        TableColumn<PhieuDoiTra, String> colLoai = new TableColumn<>("Loại xử lý");
        colLoai.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLoaiXuLy()));
        colLoai.setPrefWidth(85);

        TableColumn<PhieuDoiTra, String> colSpDoi = new TableColumn<>("SP đổi mới");
        colSpDoi.setCellValueFactory(d -> {
            PhieuDoiTra p = d.getValue();
            if (p.getDanhSachSanPham() != null) {
                for (ChiTietPhieuDoiTra ct : p.getDanhSachSanPham()) {
                    if (ct.getMaSanPhamDoi() != null && !ct.getMaSanPhamDoi().isBlank()) {
                        String ten = ct.getTenSanPhamDoi() != null ? ct.getTenSanPhamDoi() : ct.getMaSanPhamDoi();
                        return new SimpleStringProperty(ten + " (x" + ct.getSoLuongDoi() + ")");
                    }
                }
            }
            return new SimpleStringProperty("-");
        });
        colSpDoi.setPrefWidth(140);

        TableColumn<PhieuDoiTra, String> colTongTien = new TableColumn<>("Tổng hàng trả");
        colTongTien.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().tinhTongTienHangTra())));
        colTongTien.setPrefWidth(95);

        TableColumn<PhieuDoiTra, String> colTienHoan = new TableColumn<>("Tiền hoàn");
        colTienHoan.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().tinhTienHoan())));
        colTienHoan.setPrefWidth(95);

        TableColumn<PhieuDoiTra, String> colTienBu = new TableColumn<>("Tiền bù");
        colTienBu.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().tinhTienBu())));
        colTienBu.setPrefWidth(90);

        TableColumn<PhieuDoiTra, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTrangThai()));
        colTrangThai.setPrefWidth(90);
        colTrangThai.setStyle("-fx-alignment: center; -fx-font-weight: bold;");

        TableColumn<PhieuDoiTra, Void> colThaoTac = new TableColumn<>("Thao tác");
        colThaoTac.setCellFactory(col -> new TableCell<>() {
            private final Button btnXem = new Button("Xem");
            {
                btnXem.getStyleClass().add("nut-phu");
                btnXem.setStyle("-fx-font-size: 11px; -fx-padding: 2 10;");
                btnXem.setOnAction(e -> {
                    PhieuDoiTra p = getTableView().getItems().get(getIndex());
                    moManHinhChiTiet(p);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnXem);
            }
        });
        colThaoTac.setPrefWidth(70);

        tblKetQua.getColumns().addAll(colMa, colMaHD, colNgay, colKhach, colSdt, colLoai, colSpDoi, colTongTien, colTienHoan, colTienBu, colTrangThai, colThaoTac);
        tblKetQua.setItems(dsKetQua);

        card.getChildren().addAll(lblTieuDe, tblKetQua);
        return card;
    }

    private void khoiTaoManHinhChiTiet() {
        pnlManHinhChiTiet.setSpacing(10);
        VBox.setVgrow(pnlManHinhChiTiet, Priority.ALWAYS);

        HBox pnlHeader = new HBox(12);
        pnlHeader.setAlignment(Pos.CENTER_LEFT);

        btnQuayLaiDanhSach.getStyleClass().add("nut-phu");
        btnQuayLaiDanhSach.setOnAction(e -> chuyenManHinh(false));

        lblTieuDeChiTiet.getStyleClass().add("tieu-de-phan-he");
        lblBadgeTrangThai.setStyle("-fx-padding: 3 10; -fx-background-color: #e2e8f0; -fx-background-radius: 4px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnInTrenHeader = new Button("In lại phiếu");
        btnInTrenHeader.getStyleClass().add("nut-chinh");
        btnInTrenHeader.setOnAction(e -> thucHienInPhieu());

        pnlHeader.getChildren().addAll(btnQuayLaiDanhSach, lblTieuDeChiTiet, lblBadgeTrangThai, spacer, btnInTrenHeader);

        lblBannerTrangThai.setPadding(new Insets(6, 12, 6, 12));
        lblBannerTrangThai.setStyle("-fx-background-color: #f1f5f9; -fx-border-color: #cbd5e1; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-font-style: italic;");
        lblBannerTrangThai.setMaxWidth(Double.MAX_VALUE);

        VBox noiDungCuon = new VBox(12);
        noiDungCuon.setPadding(new Insets(2, 2, 10, 2));

        VBox cardThongTin = taoCardThongTinTomTat();
        VBox cardHangTra = taoCardDanhSachSanPhamTra();
        VBox cardHinhThuc = taoVungHinhThucXuLy();

        noiDungCuon.getChildren().addAll(cardThongTin, cardHangTra, cardHinhThuc);

        ScrollPane scroll = new ScrollPane(noiDungCuon);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        HBox barTomTat = taoBarTomTatTaiChinh();

        pnlManHinhChiTiet.getChildren().addAll(pnlHeader, lblBannerTrangThai, scroll, barTomTat);
    }

    private VBox taoCardThongTinTomTat() {
        VBox card = new VBox(8);
        card.getStyleClass().add("hop-noi-dung");

        Label lblTieuDe = new Label("THÔNG TIN PHIẾU ĐỔI TRẢ & HÓA ĐƠN GỐC");
        lblTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px;");

        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(6);

        grid.add(new Label("Mã phiếu:"), 0, 0);
        lblMaPhieu.setStyle("-fx-font-weight: bold;");
        grid.add(lblMaPhieu, 1, 0);

        grid.add(new Label("Hóa đơn gốc:"), 2, 0);
        lblMaHoaDon.setStyle("-fx-font-weight: bold;");
        grid.add(lblMaHoaDon, 3, 0);

        grid.add(new Label("Ngày lập:"), 0, 1);
        grid.add(lblNgayLap, 1, 1);

        grid.add(new Label("Khách hàng:"), 2, 1);
        grid.add(lblKhachHang, 3, 1);

        grid.add(new Label("Số điện thoại:"), 0, 2);
        grid.add(lblSoDienThoai, 1, 2);

        grid.add(new Label("Nhân viên lập:"), 2, 2);
        grid.add(lblNhanVien, 3, 2);

        grid.add(new Label("Hình thức xử lý:"), 0, 3);
        lblHinhThucXuLy.setStyle("-fx-font-weight: bold;");
        grid.add(lblHinhThucXuLy, 1, 3);

        card.getChildren().addAll(lblTieuDe, grid);
        return card;
    }

    private VBox taoCardDanhSachSanPhamTra() {
        VBox card = new VBox(8);
        card.getStyleClass().add("hop-noi-dung");

        Label lblTieuDe = new Label("DANH SÁCH MẶT HÀNG ĐỔI TRẢ");
        lblTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px;");

        tblSanPhamTra.getStyleClass().add("bang-du-lieu");
        tblSanPhamTra.setPrefHeight(150);
        tblSanPhamTra.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ChiTietPhieuDoiTra, Integer> colStt = new TableColumn<>("STT");
        colStt.setMaxWidth(45);
        colStt.setStyle("-fx-alignment: center;");
        colStt.setCellValueFactory(c -> new SimpleObjectProperty<>(dsSanPhamTra.indexOf(c.getValue()) + 1));

        TableColumn<ChiTietPhieuDoiTra, String> colMaSp = new TableColumn<>("Mã SP");
        colMaSp.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaSanPham()));
        colMaSp.setPrefWidth(110);

        TableColumn<ChiTietPhieuDoiTra, String> colTenSp = new TableColumn<>("Tên sản phẩm");
        colTenSp.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenSanPham()));
        colTenSp.setPrefWidth(170);

        TableColumn<ChiTietPhieuDoiTra, String> colGiaTra = new TableColumn<>("Đơn giá trả");
        colGiaTra.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().getDonGia())));
        colGiaTra.setStyle("-fx-alignment: center-right;");
        colGiaTra.setPrefWidth(95);

        TableColumn<ChiTietPhieuDoiTra, Integer> colSlTra = new TableColumn<>("SL trả");
        colSlTra.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSoLuong()));
        colSlTra.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");
        colSlTra.setPrefWidth(70);

        TableColumn<ChiTietPhieuDoiTra, String> colThanhTien = new TableColumn<>("Thành tiền hoàn");
        colThanhTien.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().tinhThanhTienTra())));
        colThanhTien.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");
        colThanhTien.setPrefWidth(115);

        TableColumn<ChiTietPhieuDoiTra, String> colLyDo = new TableColumn<>("Lý do");
        colLyDo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLyDo()));
        colLyDo.setPrefWidth(120);

        TableColumn<ChiTietPhieuDoiTra, String> colTinhTrang = new TableColumn<>("Tình trạng");
        colTinhTrang.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTinhTrang()));
        colTinhTrang.setPrefWidth(110);

        TableColumn<ChiTietPhieuDoiTra, String> colGhiChu = new TableColumn<>("Ghi chú / Mô tả lỗi");
        colGhiChu.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGhiChu() != null ? d.getValue().getGhiChu() : ""));
        colGhiChu.setPrefWidth(140);

        tblSanPhamTra.getColumns().addAll(colStt, colMaSp, colTenSp, colGiaTra, colSlTra, colThanhTien, colLyDo, colTinhTrang, colGhiChu);
        tblSanPhamTra.setItems(dsSanPhamTra);

        card.getChildren().addAll(lblTieuDe, tblSanPhamTra);
        return card;
    }

    private VBox taoVungHinhThucXuLy() {
        VBox card = new VBox(12);
        card.getStyleClass().add("hop-noi-dung");

        Label lblTieuDe = new Label("HÌNH THỨC XỬ LÝ ĐỔI TRẢ");
        lblTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px;");

        rdoTraHang.setToggleGroup(grpHinhThuc);
        rdoDoiHang.setToggleGroup(grpHinhThuc);
        rdoTraHang.setDisable(true);
        rdoDoiHang.setDisable(true);

        rdoTraHang.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1f2937;");
        rdoDoiHang.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1f2937;");

        HBox hangChon = new HBox(30, rdoTraHang, rdoDoiHang);
        hangChon.setAlignment(Pos.CENTER_LEFT);

        pnlTheHoanTien.setPadding(new Insets(10));
        pnlTheHoanTien.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 4px; -fx-background-radius: 4px;");
        HBox hangHoan = new HBox(16);
        hangHoan.setAlignment(Pos.CENTER_LEFT);
        lblTienHoanDuKien.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #b91c1c;");
        hangHoan.getChildren().addAll(
                new Label("Hình thức hoàn:"), lblHinhThucHoan,
                new Label("Số tiền hoàn trả khách:"), lblTienHoanDuKien
        );
        pnlTheHoanTien.getChildren().add(hangHoan);

        pnlTheDoiHang.setPadding(new Insets(10));
        pnlTheDoiHang.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 4px; -fx-background-radius: 4px;");

        tblSanPhamDoiMoi.getStyleClass().add("bang-du-lieu");
        tblSanPhamDoiMoi.setPrefHeight(130);
        tblSanPhamDoiMoi.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<DongDoiHang, String> colTenDoi = new TableColumn<>("Sản phẩm đổi");
        colTenDoi.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenSanPham()));
        colTenDoi.setPrefWidth(220);

        TableColumn<DongDoiHang, String> colGiaDoi = new TableColumn<>("Đơn giá");
        colGiaDoi.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().getDonGia())));
        colGiaDoi.setStyle("-fx-alignment: center-right;");
        colGiaDoi.setPrefWidth(110);

        TableColumn<DongDoiHang, Integer> colSlDoi = new TableColumn<>("SL");
        colSlDoi.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSoLuong()));
        colSlDoi.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");
        colSlDoi.setPrefWidth(70);

        TableColumn<DongDoiHang, String> colTtDoi = new TableColumn<>("Thành tiền");
        colTtDoi.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().getThanhTien())));
        colTtDoi.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");
        colTtDoi.setPrefWidth(120);

        tblSanPhamDoiMoi.getColumns().addAll(colTenDoi, colGiaDoi, colSlDoi, colTtDoi);
        tblSanPhamDoiMoi.setItems(dsSanPhamDoiMoi);

        lblDongChenhLech.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        pnlTheDoiHang.getChildren().addAll(tblSanPhamDoiMoi, lblDongChenhLech);

        card.getChildren().addAll(lblTieuDe, hangChon, pnlTheHoanTien, pnlTheDoiHang);
        return card;
    }

    private HBox taoBarTomTatTaiChinh() {
        HBox bar = new HBox(20);
        bar.setPadding(new Insets(10, 14, 10, 14));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0;");

        lblTongTienHangTra.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold;");
        lblTongTienHangDoi.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold;");
        lblChenhLechTaiChinh.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnInLaiPhieu.getStyleClass().add("nut-chinh");
        btnInLaiPhieu.setOnAction(e -> thucHienInPhieu());

        btnDongChiTiet.getStyleClass().add("nut-phu");
        btnDongChiTiet.setOnAction(e -> chuyenManHinh(false));

        bar.getChildren().addAll(lblTongTienHangTra, lblTongTienHangDoi, lblChenhLechTaiChinh, spacer, btnInLaiPhieu, btnDongChiTiet);
        return bar;
    }

    private void chuyenManHinh(boolean sangChiTiet) {
        this.getChildren().clear();
        if (sangChiTiet) {
            this.getChildren().add(pnlManHinhChiTiet);
        } else {
            this.getChildren().add(pnlManHinhDanhSach);
        }
    }

    private void moManHinhChiTiet(PhieuDoiTra phieuDauVao) {
        if (phieuDauVao == null) return;

        PhieuDoiTra phieu = phieuDauVao;
        if (phieu.getDanhSachSanPham() == null || phieu.getDanhSachSanPham().isEmpty()) {
            try {
                PhieuDoiTra dayDu = controller.layPhieuDoiTraTheoMa(phieu.getMaPhieuDoiTra());
                if (dayDu != null && dayDu.getDanhSachSanPham() != null && !dayDu.getDanhSachSanPham().isEmpty()) {
                    phieu = dayDu;
                }
            } catch (Exception ignored) {}
        }

        this.phieuDangXem = phieu;

        lblMaPhieu.setText(phieu.getMaPhieuDoiTra());
        lblMaHoaDon.setText(phieu.getMaHoaDon() != null ? phieu.getMaHoaDon() : "--");
        lblNgayLap.setText(phieu.getNgayLap() != null ? phieu.getNgayLap().format(DINH_DANG_NGAY) : "--");
        lblKhachHang.setText(phieu.getTenKhachHang() != null ? phieu.getTenKhachHang() : "Khách lẻ");
        lblSoDienThoai.setText(phieu.getSoDienThoaiKhachHang() != null ? phieu.getSoDienThoaiKhachHang() : "--");
        lblNhanVien.setText(phieu.getMaNhanVien() != null ? phieu.getMaNhanVien() : "--");
        lblHinhThucXuLy.setText(phieu.getLoaiXuLy() != null ? phieu.getLoaiXuLy() : "Hoàn tiền");

        String tt = phieu.getTrangThai();
        lblBadgeTrangThai.setText(tt != null ? tt : "Chờ xử lý");
        if ("Đã duyệt".equalsIgnoreCase(tt) || "Hoàn thành".equalsIgnoreCase(tt)) {
            lblBadgeTrangThai.setStyle("-fx-padding: 3 10; -fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-background-radius: 4px; -fx-font-weight: bold;");
            lblBannerTrangThai.setText("Phiếu đổi trả đã được xác nhận [Đã duyệt] - Dữ liệu đã được khóa, chỉ xem chi tiết.");
        } else if ("Từ chối".equalsIgnoreCase(tt)) {
            lblBadgeTrangThai.setStyle("-fx-padding: 3 10; -fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-background-radius: 4px; -fx-font-weight: bold;");
            lblBannerTrangThai.setText("Phiếu đổi trả này đã bị [Từ chối] tiếp nhận.");
        } else {
            lblBadgeTrangThai.setStyle("-fx-padding: 3 10; -fx-background-color: #fef9c3; -fx-text-fill: #854d0e; -fx-background-radius: 4px; -fx-font-weight: bold;");
            lblBannerTrangThai.setText("Phiếu đổi trả đang [Chờ xử lý] duyệt từ người có thẩm quyền.");
        }

        dsSanPhamTra.clear();
        if (phieu.getDanhSachSanPham() != null) {
            dsSanPhamTra.addAll(phieu.getDanhSachSanPham());
        }

        dsSanPhamDoiMoi.clear();
        double tongHangTra = phieu.tinhTongTienHangTra();
        double tongHangDoi = phieu.tinhTongTienHangDoi();
        double tienHoan = phieu.tinhTienHoan();
        double tienBu = phieu.tinhTienBu();

        boolean laDoiHang = "Đổi hàng".equalsIgnoreCase(phieu.getLoaiXuLy());
        if (laDoiHang) {
            rdoDoiHang.setSelected(true);
            pnlTheHoanTien.setVisible(false);
            pnlTheHoanTien.setManaged(false);
            pnlTheDoiHang.setVisible(true);
            pnlTheDoiHang.setManaged(true);

            if (phieu.getDanhSachSanPham() != null) {
                for (ChiTietPhieuDoiTra ct : phieu.getDanhSachSanPham()) {
                    if (ct.getMaSanPhamDoi() != null && !ct.getMaSanPhamDoi().isBlank()) {
                        String tenDoi = ct.getTenSanPhamDoi() != null ? ct.getTenSanPhamDoi() : ct.getMaSanPhamDoi();
                        dsSanPhamDoiMoi.add(new DongDoiHang(tenDoi, ct.getDonGiaDoi(), ct.getSoLuongDoi(), ct.tinhThanhTienDoi()));
                    }
                }
            }

            if (tienBu > 0) {
                lblDongChenhLech.setText("Kết quả đổi trả: Khách bù thêm " + DINH_DANG_TIEN.format(tienBu));
                lblDongChenhLech.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0284c7;");
                lblChenhLechTaiChinh.setText("Khách bù thêm: " + DINH_DANG_TIEN.format(tienBu));
                lblChenhLechTaiChinh.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0284c7;");
            } else if (tienHoan > 0) {
                lblDongChenhLech.setText("Kết quả đổi trả: Hoàn lại cho khách " + DINH_DANG_TIEN.format(tienHoan));
                lblDongChenhLech.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #dc2626;");
                lblChenhLechTaiChinh.setText("Tiền hoàn lại: " + DINH_DANG_TIEN.format(tienHoan));
                lblChenhLechTaiChinh.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dc2626;");
            } else {
                lblDongChenhLech.setText("Kết quả đổi trả: Đổi ngang giá (Chênh lệch: 0 VNĐ)");
                lblDongChenhLech.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #16a34a;");
                lblChenhLechTaiChinh.setText("Chênh lệch: 0 VNĐ");
                lblChenhLechTaiChinh.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #16a34a;");
            }
        } else {
            rdoTraHang.setSelected(true);
            pnlTheHoanTien.setVisible(true);
            pnlTheHoanTien.setManaged(true);
            pnlTheDoiHang.setVisible(false);
            pnlTheDoiHang.setManaged(false);

            lblTienHoanDuKien.setText(DINH_DANG_TIEN.format(tienHoan));
            lblChenhLechTaiChinh.setText("Tiền hoàn lại: " + DINH_DANG_TIEN.format(tienHoan));
            lblChenhLechTaiChinh.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dc2626;");
        }

        lblTongTienHangTra.setText("Tổng hàng trả: " + DINH_DANG_TIEN.format(tongHangTra));
        lblTongTienHangDoi.setText("Tổng hàng đổi: " + DINH_DANG_TIEN.format(tongHangDoi));

        tblSanPhamTra.refresh();
        tblSanPhamDoiMoi.refresh();

        chuyenManHinh(true);
    }

    private void thucHienInPhieu() {
        if (phieuDangXem == null) return;
        Alert al = new Alert(Alert.AlertType.INFORMATION);
        al.setTitle("In phiếu đổi trả");
        al.setHeaderText(null);
        al.setContentText("Đã gửi lệnh in cho phiếu: " + phieuDangXem.getMaPhieuDoiTra());
        al.showAndWait();
    }

    private void dangKySuKien() {
        btnTimKiem.setOnAction(e -> thucHienTimKiem());
        txtTuKhoa.setOnAction(e -> thucHienTimKiem());

        btnDatLai.setOnAction(e -> {
            txtTuKhoa.clear();
            dpTuNgay.setValue(null);
            dpDenNgay.setValue(null);
            cboTrangThai.setValue("Tất cả trạng thái");
            cboLoaiXuLy.setValue("Tất cả loại");
            thucHienTimKiem();
        });

        btnXuatExcel.setOnAction(e -> xuatDuLieuExcel());

        tblKetQua.setRowFactory(tv -> {
            TableRow<PhieuDoiTra> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    moManHinhChiTiet(row.getItem());
                }
            });
            return row;
        });
    }

    private void napDuLieuMacDinh() {
        thucHienTimKiem();
    }

    private void thucHienTimKiem() {
        String tuKhoa = txtTuKhoa.getText();
        LocalDate tuNgay = dpTuNgay.getValue();
        LocalDate denNgay = dpDenNgay.getValue();

        String tt = cboTrangThai.getValue();
        String trangThaiLoc = ("Tất cả trạng thái".equals(tt) || tt == null) ? null : tt;

        String loai = cboLoaiXuLy.getValue();
        String loaiXuLyLoc = ("Tất cả loại".equals(loai) || loai == null) ? null : loai;

        try {
            List<PhieuDoiTra> ds = controller.timKiemPhieuDoiTra(tuKhoa, tuNgay, denNgay, trangThaiLoc, loaiXuLyLoc);
            dsKetQua.clear();
            if (ds != null) {
                dsKetQua.addAll(ds);
            }
            capNhatKpi();
        } catch (Exception ex) {
            Alert al = new Alert(Alert.AlertType.ERROR);
            al.setTitle("Lỗi");
            al.setContentText("Lỗi tìm kiếm phiếu đổi trả: " + ex.getMessage());
            al.showAndWait();
        }
    }

    private void capNhatKpi() {
        int tongPhieu = dsKetQua.size();
        double tongHoan = 0.0;
        double tongBu = 0.0;
        int demHoanThanh = 0;

        for (PhieuDoiTra p : dsKetQua) {
            tongHoan += p.tinhTienHoan();
            tongBu += p.tinhTienBu();
            if ("Đã duyệt".equalsIgnoreCase(p.getTrangThai()) || "Hoàn thành".equalsIgnoreCase(p.getTrangThai())) {
                demHoanThanh++;
            }
        }

        lblKpiTongSoPhieu.setText(String.valueOf(tongPhieu));
        lblKpiTongTienHoan.setText(DINH_DANG_TIEN.format(tongHoan));
        lblKpiTongTienBu.setText(DINH_DANG_TIEN.format(tongBu));

        if (tongPhieu > 0) {
            double tyLe = (demHoanThanh * 100.0) / tongPhieu;
            lblKpiTyLeHoanThanh.setText(String.format("%.1f%%", tyLe));
        } else {
            lblKpiTyLeHoanThanh.setText("0%");
        }
    }

    private void xuatDuLieuExcel() {
        if (dsKetQua.isEmpty()) {
            Alert al = new Alert(Alert.AlertType.WARNING);
            al.setTitle("Xuất file");
            al.setHeaderText(null);
            al.setContentText("Không có dữ liệu phiếu đổi trả nào để xuất!");
            al.showAndWait();
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu danh sách phiếu đổi trả");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV (*.csv)", "*.csv"));
        fc.setInitialFileName("BaoCao_PhieuDoiTra_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv");

        File file = fc.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try (PrintWriter pw = new PrintWriter(file, StandardCharsets.UTF_8)) {
                pw.write('\ufeff');
                pw.println("Mã phiếu,Mã hóa đơn,Ngày lập,Khách hàng,Số điện thoại,Loại xử lý,Tổng tiền hàng trả,Tiền hoàn,Tiền bù,Trạng thái");
                for (PhieuDoiTra p : dsKetQua) {
                    pw.println(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%.0f\",\"%.0f\",\"%.0f\",\"%s\"",
                            p.getMaPhieuDoiTra(),
                            p.getMaHoaDon() != null ? p.getMaHoaDon() : "",
                            p.getNgayLap() != null ? p.getNgayLap().format(DINH_DANG_NGAY) : "",
                            p.getTenKhachHang() != null ? p.getTenKhachHang().replace("\"", "\"\"") : "",
                            p.getSoDienThoaiKhachHang() != null ? p.getSoDienThoaiKhachHang() : "",
                            p.getLoaiXuLy() != null ? p.getLoaiXuLy() : "",
                            p.tinhTongTienHangTra(),
                            p.tinhTienHoan(),
                            p.tinhTienBu(),
                            p.getTrangThai() != null ? p.getTrangThai() : ""
                    ));
                }
                Alert al = new Alert(Alert.AlertType.INFORMATION);
                al.setTitle("Xuất file thành công");
                al.setHeaderText(null);
                al.setContentText("Đã xuất " + dsKetQua.size() + " dòng ra tệp: " + file.getAbsolutePath());
                al.showAndWait();
            } catch (Exception ex) {
                Alert al = new Alert(Alert.AlertType.ERROR);
                al.setTitle("Lỗi");
                al.setContentText("Lỗi xuất file: " + ex.getMessage());
                al.showAndWait();
            }
        }
    }

    public static class DongDoiHang {
        private final String tenSanPham;
        private final double donGia;
        private final int soLuong;
        private final double thanhTien;

        public DongDoiHang(String tenSanPham, double donGia, int soLuong, double thanhTien) {
            this.tenSanPham = tenSanPham;
            this.donGia = donGia;
            this.soLuong = soLuong;
            this.thanhTien = thanhTien > 0 ? thanhTien : (donGia * soLuong);
        }

        public String getTenSanPham() { return tenSanPham; }
        public double getDonGia() { return donGia; }
        public int getSoLuong() { return soLuong; }
        public double getThanhTien() { return thanhTien; }
    }
}
