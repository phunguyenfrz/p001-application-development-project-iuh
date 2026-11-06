package com.petshop.gui.doitra;

import com.petshop.bus.PhieuDoiTraBUS;
import com.petshop.entity.*;
import com.petshop.util.SessionManager;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình 1: Lập phiếu đổi trả (RET_CREATE / DT_TAO).
 * Tuân thủ 100% Bộ chỉ thị kỹ thuật IUH:
 * - Kiểu tiền tệ: double (0% BigDecimal).
 * - Không nạp toàn bộ CSDL lên ComboBox trên RAM: tra cứu hóa đơn trực tiếp theo SĐT/Mã HĐ dưới DB;
 *   quét/nhập mã sản phẩm đổi mới bằng txtMaVachSanPham + phím Enter bắn vào giỏ hàng đổi (trùng +1 SL, khóa cứng giá bán).
 * - Nhân viên lập phiếu tự động lấy từ SessionManager.getTaiKhoanHienTai().getNhanVien().
 * - Tính toán tiền tệ ủy quyền hoàn toàn cho các phương thức dẫn xuất của Entity (tinhThanhTienTra, tinhTienHoan, tinhTienBu).
 */
public class LapPhieuDoiTraView extends VBox {

    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 VNĐ");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PhieuDoiTraBUS controller;

    // Tìm hóa đơn gốc trực tiếp dưới DB (không dùng ComboBox nạp toàn bộ CSDL)
    private final TextField txtTimKiemHoaDon = new TextField();
    private final Button btnTimHoaDon = new Button("Tra cứu hóa đơn");
    private final Label lblBannerLoiHoaDon = new Label();
    private final TableView<HoaDon> tblKetQuaHoaDon = new TableView<>();
    private final ObservableList<HoaDon> dsHoaDonTimKiem = FXCollections.observableArrayList();
    private final VBox pnlThongTinHoaDonChon = new VBox(8);

    // Hóa đơn đang chọn
    private HoaDon hoaDonDangChon = null;
    private boolean hoaDonHopLe = false;

    // Bảng sản phẩm đổi trả
    public static class DongTraHang {
        private final BooleanProperty chon = new SimpleBooleanProperty(false);
        private final ChiTietPhieuDoiTra chiTiet;
        private final int soLuongDaMua;
        private final int soLuongDaTraTruoc;
        private final Spinner<Integer> spnSoLuongTra;
        private final ComboBox<String> cboLyDo;
        private final ComboBox<String> cboTinhTrang;
        private final TextField txtGhiChu;
        private final Button btnDinhKem;
        private File fileBangChung = null;
        private final Label lblThanhTien;
        private final boolean laDichVu;

        public DongTraHang(String maSanPham, String tenSanPham, double donGia,
                           int soLuongDaMua, int soLuongDaTraTruoc, boolean laDichVu) {
            this.chiTiet = new ChiTietPhieuDoiTra();
            this.chiTiet.setMaSanPham(maSanPham);
            this.chiTiet.setTenSanPham(tenSanPham);
            this.chiTiet.setDonGia(donGia);
            this.chiTiet.setSoLuong(1);

            this.soLuongDaMua = soLuongDaMua;
            this.soLuongDaTraTruoc = soLuongDaTraTruoc;
            this.laDichVu = laDichVu;

            int soLuongToiDa = Math.max(0, soLuongDaMua - soLuongDaTraTruoc);
            this.spnSoLuongTra = new Spinner<>(soLuongToiDa > 0 ? 1 : 0, Math.max(1, soLuongToiDa), 1);
            this.spnSoLuongTra.setEditable(true);
            this.spnSoLuongTra.setPrefWidth(75);

            this.cboLyDo = new ComboBox<>(FXCollections.observableArrayList(
                    "Lỗi sản phẩm", "Hết hạn", "Không phù hợp", "Khác"
            ));
            this.cboLyDo.setPromptText("-- Chọn lý do --");
            this.cboLyDo.setPrefWidth(140);

            this.cboTinhTrang = new ComboBox<>(FXCollections.observableArrayList(
                    "Nguyên vẹn", "Đã mở", "Hư hỏng"
            ));
            this.cboTinhTrang.setPromptText("-- Tình trạng --");
            this.cboTinhTrang.setPrefWidth(130);

            this.txtGhiChu = new TextField();
            this.txtGhiChu.setPromptText("Ghi chú / mô tả lỗi...");
            this.txtGhiChu.setPrefWidth(160);

            this.btnDinhKem = new Button("Ảnh minh chứng");
            this.btnDinhKem.getStyleClass().add("nut-phu");
            this.btnDinhKem.setStyle("-fx-font-size: 11px; -fx-padding: 3 8;");

            this.lblThanhTien = new Label("0 VNĐ");
            this.lblThanhTien.setStyle("-fx-font-weight: bold;");

            if (laDichVu || soLuongToiDa <= 0) {
                this.chon.set(false);
                this.spnSoLuongTra.setDisable(true);
                this.cboLyDo.setDisable(true);
                this.cboTinhTrang.setDisable(true);
                this.txtGhiChu.setDisable(true);
                this.btnDinhKem.setDisable(true);
            }
        }



        public BooleanProperty chonProperty() { return chon; }
        public boolean isChon() { return chon.get(); }
        public void setChon(boolean val) { chon.set(val); }
        public String getMaSanPham() { return chiTiet.getMaSanPham(); }
        public String getTenSanPham() { return chiTiet.getTenSanPham(); }
        public double getDonGia() { return chiTiet.getDonGia(); }
        public int getSoLuongDaMua() { return soLuongDaMua; }
        public int getSoLuongDaTraTruoc() { return soLuongDaTraTruoc; }
        public int getSoLuongTra() { return spnSoLuongTra.getValue(); }
        public Spinner<Integer> getSpnSoLuongTra() { return spnSoLuongTra; }
        public ComboBox<String> getCboLyDo() { return cboLyDo; }
        public ComboBox<String> getCboTinhTrang() { return cboTinhTrang; }
        public TextField getTxtGhiChu() { return txtGhiChu; }
        public Button getBtnDinhKem() { return btnDinhKem; }
        public File getFileBangChung() { return fileBangChung; }
        public void setFileBangChung(File f) { this.fileBangChung = f; }
        public Label getLblThanhTien() { return lblThanhTien; }
        public boolean isLaDichVu() { return laDichVu; }

        public double tinhThanhTien() {
            if (!isChon() || laDichVu) return 0.0;
            chiTiet.setSoLuong(getSoLuongTra());
            return chiTiet.tinhThanhTienTra();
        }
    }

    private final TableView<DongTraHang> tblSanPhamDoiTra = new TableView<>();
    private final ObservableList<DongTraHang> dsSanPhamDoiTra = FXCollections.observableArrayList();

    // Hình thức xử lý
    private final RadioButton rdoTraHang = new RadioButton("Trả hàng, hoàn tiền");
    private final RadioButton rdoDoiHang = new RadioButton("Đổi sang sản phẩm khác");
    private final ToggleGroup grpHinhThuc = new ToggleGroup();

    // Thẻ hoàn tiền
    private final VBox pnlTheHoanTien = new VBox(10);
    private final ComboBox<String> cboHinhThucHoan = new ComboBox<>(FXCollections.observableArrayList("Tiền mặt", "Chuyển khoản", "Voucher"));
    private final Label lblTienHoanDuKien = new Label("0 VNĐ");

    // Thẻ đổi hàng: Ô quét mã vạch sản phẩm txtMaVachSanPham + phím Enter bắn vào giỏ hàng đổi
    private final VBox pnlTheDoiHang = new VBox(10);
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
            this.chiTietDoi.setDonGiaDoi(sanPham.getGiaBan() != null ? sanPham.getGiaBan().doubleValue() : 0.0);
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

    // Thanh tóm tắt cố định
    private final Label lblTongTienTra = new Label("0 VNĐ");
    private final Label lblTongTienDoi = new Label("0 VNĐ");
    private final Label lblChenhLechCuoi = new Label("0 VNĐ");
    private final Button btnHuy = new Button("Làm mới");
    private final Button btnXemLai = new Button("Xem lại");
    private final Button btnLuuPhiu = new Button("Lưu phiếu đổi trả");

    public LapPhieuDoiTraView() {
        this.controller = new PhieuDoiTraBUS();
        khoiTaoGiaoDien();
        dangKySuKien();
        napDanhSachHoaDon7NgayMacDinh();
    }

    private void khoiTaoGiaoDien() {
        this.setSpacing(14);
        this.getStyleClass().add("khung-phan-he");

        VBox pnlTieuDeMan = new VBox(4);
        Label lblTieuDe = new Label("PHIẾU ĐỔI TRẢ - LẬP PHIẾU ĐỔI TRẢ");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Tra cứu hóa đơn gốc (trong 7 ngày, đã thanh toán), chọn sản phẩm cần đổi trả và xác nhận hoàn tiền hoặc đổi sản phẩm mới.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDeMan.getChildren().addAll(lblTieuDe, lblMoTa);

        VBox vungA = taoVungATimHoaDon();
        VBox vungB = taoVungBChonSanPham();
        VBox vungC = taoVungCHinhThucXuLy();
        HBox vungD = taoVungDTomTatCoDinh();

        ScrollPane scroll = new ScrollPane();
        VBox noiDungCuon = new VBox(14, vungA, vungB, vungC);
        noiDungCuon.setPadding(new Insets(0, 4, 10, 0));
        scroll.setContent(noiDungCuon);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #f4f6f8;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        this.getChildren().addAll(pnlTieuDeMan, scroll, vungD);
    }

    private VBox taoVungATimHoaDon() {
        VBox card = new VBox(10);
        card.getStyleClass().add("hop-noi-dung");

        Label lblTieuDe = new Label("TÌM HÓA ĐƠN GỐC CẦN ĐỔI TRẢ (TRONG 7 NGÀY GẦN NHẤT)");
        lblTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px;");

        HBox hangTim = new HBox(10);
        hangTim.setAlignment(Pos.CENTER_LEFT);
        txtTimKiemHoaDon.setPromptText("Nhập Số điện thoại khách hàng (10 số) hoặc Mã hóa đơn rồi nhấn Enter...");
        txtTimKiemHoaDon.setPrefWidth(420);
        HBox.setHgrow(txtTimKiemHoaDon, Priority.ALWAYS);

        btnTimHoaDon.getStyleClass().add("nut-chinh");
        Button btnHien7Ngay = new Button("Hiện HĐ 7 ngày gần nhất");
        btnHien7Ngay.getStyleClass().add("nut-phu");
        btnHien7Ngay.setOnAction(e -> napDanhSachHoaDon7NgayMacDinh());

        hangTim.getChildren().addAll(
                new Label("Tra cứu HĐ / SĐT:"), txtTimKiemHoaDon, btnTimHoaDon, btnHien7Ngay
        );

        lblBannerLoiHoaDon.setManaged(false);
        lblBannerLoiHoaDon.setVisible(false);
        lblBannerLoiHoaDon.setStyle("-fx-font-weight: bold;");

        tblKetQuaHoaDon.getStyleClass().add("bang-du-lieu");
        tblKetQuaHoaDon.setPrefHeight(130);
        tblKetQuaHoaDon.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<HoaDon, String> colMaHD = new TableColumn<>("Mã HĐ");
        colMaHD.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaHoaDon()));
        colMaHD.setPrefWidth(140);

        TableColumn<HoaDon, String> colNgayLap = new TableColumn<>("Ngày mua");
        colNgayLap.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getNgayLap() != null ? d.getValue().getNgayLap().format(DINH_DANG_NGAY) : ""));
        colNgayLap.setPrefWidth(100);

        TableColumn<HoaDon, String> colKhach = new TableColumn<>("Khách hàng");
        colKhach.setCellValueFactory(d -> {
            HoaDon hd = d.getValue();
            String ten = hd.getTenKhachHang() != null ? hd.getTenKhachHang() : "Khách vãng lai";
            String sdt = hd.getSoDienThoaiKhachHang() != null && !hd.getSoDienThoaiKhachHang().isBlank()
                    ? " (" + hd.getSoDienThoaiKhachHang() + ")" : "";
            return new SimpleStringProperty(ten + sdt);
        });
        colKhach.setPrefWidth(170);

        TableColumn<HoaDon, String> colTongTien = new TableColumn<>("Tổng tiền");
        colTongTien.setCellValueFactory(d -> new SimpleStringProperty(
                DINH_DANG_TIEN.format(d.getValue().getTongTien())));
        colTongTien.setPrefWidth(110);

        TableColumn<HoaDon, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTrangThaiThanhToan()));
        colTrangThai.setPrefWidth(110);

        TableColumn<HoaDon, String> colChiNhanh = new TableColumn<>("Chi nhánh");
        colChiNhanh.setCellValueFactory(d -> new SimpleStringProperty("Chi nhánh chính"));
        colChiNhanh.setPrefWidth(110);

        tblKetQuaHoaDon.getColumns().addAll(colMaHD, colNgayLap, colKhach, colTongTien, colTrangThai, colChiNhanh);
        tblKetQuaHoaDon.setItems(dsHoaDonTimKiem);

        pnlThongTinHoaDonChon.setManaged(false);
        pnlThongTinHoaDonChon.setVisible(false);
        pnlThongTinHoaDonChon.setPadding(new Insets(10));
        pnlThongTinHoaDonChon.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 4px; -fx-background-radius: 4px;");

        card.getChildren().addAll(lblTieuDe, hangTim, lblBannerLoiHoaDon, tblKetQuaHoaDon, pnlThongTinHoaDonChon);
        return card;
    }


    private VBox taoVungBChonSanPham() {
        VBox card = new VBox(10);
        card.getStyleClass().add("hop-noi-dung");

        Label lblTieuDe = new Label("CHỌN SẢN PHẨM ĐỔI TRẢ & TÌNH TRẠNG HÀNG");
        lblTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px;");

        Label lblGhiChuMaTran = new Label("(*) Lưu ý: Chỉ đổi trả sản phẩm thuộc hóa đơn đã thanh toán trong 7 ngày. Dịch vụ chăm sóc không được đổi trả.");
        lblGhiChuMaTran.getStyleClass().add("mo-ta-phan-he");

        tblSanPhamDoiTra.getStyleClass().add("bang-du-lieu");
        tblSanPhamDoiTra.setPrefHeight(200);
        tblSanPhamDoiTra.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<DongTraHang, Boolean> colChon = new TableColumn<>("Chọn");
        colChon.setCellValueFactory(d -> d.getValue().chonProperty());
        colChon.setCellFactory(col -> new TableCell<DongTraHang, Boolean>() {
            private final CheckBox cb = new CheckBox();
            {
                cb.setOnAction(e -> {
                    DongTraHang d = getTableView().getItems().get(getIndex());
                    if (d.isLaDichVu()) {
                        cb.setSelected(false);
                        hienThongBaoCanhBao("Dòng dịch vụ chăm sóc thú cưng không được phép đổi trả!");
                        return;
                    }
                    d.setChon(cb.isSelected());
                    kiemTraLaiMaTran(d);
                    tinhToanLaiTaiChinh();
                });
            }
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    DongTraHang d = getTableRow().getItem();
                    cb.setSelected(d.isChon());
                    cb.setDisable(d.isLaDichVu() || (d.getSoLuongDaMua() - d.getSoLuongDaTraTruoc()) <= 0);
                    setGraphic(cb);
                }
            }
        });
        colChon.setPrefWidth(55);

        TableColumn<DongTraHang, String> colTen = new TableColumn<>("Sản phẩm");
        colTen.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenSanPham()));
        colTen.setPrefWidth(180);

        TableColumn<DongTraHang, String> colGia = new TableColumn<>("Đơn giá (khóa cứng)");
        colGia.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().getDonGia())));
        colGia.setPrefWidth(110);

        TableColumn<DongTraHang, Integer> colDaMua = new TableColumn<>("SL đã mua");
        colDaMua.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSoLuongDaMua()));
        colDaMua.setPrefWidth(80);

        TableColumn<DongTraHang, Integer> colDaTra = new TableColumn<>("SL đã trả");
        colDaTra.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSoLuongDaTraTruoc()));
        colDaTra.setPrefWidth(75);

        TableColumn<DongTraHang, Spinner<Integer>> colSlTra = new TableColumn<>("SL đổi trả");
        colSlTra.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSpnSoLuongTra()));
        colSlTra.setPrefWidth(85);

        TableColumn<DongTraHang, ComboBox<String>> colLyDo = new TableColumn<>("Lý do");
        colLyDo.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getCboLyDo()));
        colLyDo.setPrefWidth(150);

        TableColumn<DongTraHang, ComboBox<String>> colTinhTrang = new TableColumn<>("Tình trạng");
        colTinhTrang.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getCboTinhTrang()));
        colTinhTrang.setPrefWidth(140);

        TableColumn<DongTraHang, TextField> colGhiChu = new TableColumn<>("Ghi chú / Mô tả lỗi");
        colGhiChu.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getTxtGhiChu()));
        colGhiChu.setPrefWidth(170);

        TableColumn<DongTraHang, Button> colAnh = new TableColumn<>("Ảnh chứng cứ");
        colAnh.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getBtnDinhKem()));
        colAnh.setPrefWidth(110);

        TableColumn<DongTraHang, Label> colThanhTien = new TableColumn<>("Thành tiền hoàn");
        colThanhTien.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getLblThanhTien()));
        colThanhTien.setPrefWidth(120);

        tblSanPhamDoiTra.getColumns().addAll(colChon, colTen, colGia, colDaMua, colDaTra, colSlTra,
                colLyDo, colTinhTrang, colGhiChu, colAnh, colThanhTien);
        tblSanPhamDoiTra.setItems(dsSanPhamDoiTra);

        card.getChildren().addAll(lblTieuDe, lblGhiChuMaTran, tblSanPhamDoiTra);
        return card;
    }

    private VBox taoVungCHinhThucXuLy() {
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

        HBox hangThemDoi = new HBox(10);
        hangThemDoi.setAlignment(Pos.CENTER_LEFT);
        txtMaVachSanPham.setPromptText("Quét mã vạch / nhập mã hoặc tên SP đổi rồi nhấn Enter...");
        txtMaVachSanPham.setPrefWidth(360);
        HBox.setHgrow(txtMaVachSanPham, Priority.ALWAYS);

        spnSoLuongDoi.setPrefWidth(75);
        btnThemSanPhamDoi.getStyleClass().add("nut-chinh");
        lblCanhBaoTonKho.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold;");

        hangThemDoi.getChildren().addAll(
                new Label("Mã vạch / Mã SP đổi:"), txtMaVachSanPham,
                new Label("SL:"), spnSoLuongDoi,
                btnThemSanPhamDoi, lblCanhBaoTonKho
        );

        tblSanPhamDoiMoi.getStyleClass().add("bang-du-lieu");
        tblSanPhamDoiMoi.setPrefHeight(120);
        tblSanPhamDoiMoi.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<DongDoiHang, String> colTenDoi = new TableColumn<>("Sản phẩm đổi");
        colTenDoi.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSanPham().getTenSanPham()));

        TableColumn<DongDoiHang, String> colGiaDoi = new TableColumn<>("Đơn giá (khóa cứng)");
        colGiaDoi.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().getDonGia())));

        TableColumn<DongDoiHang, Integer> colSlDoi = new TableColumn<>("SL");
        colSlDoi.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSoLuong()));

        TableColumn<DongDoiHang, String> colTtDoi = new TableColumn<>("Thành tiền");
        colTtDoi.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().getThanhTien())));

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
                setGraphic(empty ? null : btnXoa);
            }
        });

        tblSanPhamDoiMoi.getColumns().addAll(colTenDoi, colGiaDoi, colSlDoi, colTtDoi, colXoaDoi);
        tblSanPhamDoiMoi.setItems(dsSanPhamDoiMoi);

        lblDongChenhLech.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        pnlTheDoiHang.getChildren().addAll(hangThemDoi, tblSanPhamDoiMoi, lblDongChenhLech);

        card.getChildren().addAll(lblTieuDe, hangChon, pnlTheHoanTien, pnlTheDoiHang);
        return card;
    }

    private HBox taoVungDTomTatCoDinh() {
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

        btnHuy.getStyleClass().add("nut-phu");
        btnXemLai.getStyleClass().add("nut-phu");
        btnLuuPhiu.getStyleClass().add("nut-chinh");

        bar.getChildren().addAll(pnlThongKe, btnHuy, btnXemLai, btnLuuPhiu);
        return bar;
    }

    private void dangKySuKien() {
        btnTimHoaDon.setOnAction(e -> timKiemHoaDon());
        txtTimKiemHoaDon.setOnAction(e -> timKiemHoaDon());

        tblKetQuaHoaDon.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                lblBannerLoiHoaDon.setVisible(false);
                lblBannerLoiHoaDon.setManaged(false);
                chonHoaDonGoc(newVal);
            }
        });

        grpHinhThuc.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            boolean laTraHang = rdoTraHang.isSelected();
            pnlTheHoanTien.setVisible(laTraHang);
            pnlTheHoanTien.setManaged(laTraHang);
            pnlTheDoiHang.setVisible(!laTraHang);
            pnlTheDoiHang.setManaged(!laTraHang);
            tinhToanLaiTaiChinh();
        });

        // Quét mã vạch sản phẩm đổi + phím Enter bắn vào giỏ hàng đổi, trùng thì +1 SL
        txtMaVachSanPham.setOnAction(e -> themSanPhamDoi());
        btnThemSanPhamDoi.setOnAction(e -> themSanPhamDoi());

        btnHuy.setOnAction(e -> lamMoiToanBo());
        btnXemLai.setOnAction(e -> xemLaiTruocKhiLuu());
        btnLuuPhiu.setOnAction(e -> xacNhanVaLuuPhiu());
    }

    private void napDanhSachHoaDon7NgayMacDinh() {
        try {
            List<HoaDon> ds7Ngay = controller.layDanhSachHoaDonTrong7Ngay();
            dsHoaDonTimKiem.setAll(ds7Ngay);
            tblKetQuaHoaDon.getSelectionModel().clearSelection();
        } catch (Exception ignored) {}
    }

    private void timKiemHoaDon() {
        String tuKhoa = txtTimKiemHoaDon.getText();
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            napDanhSachHoaDon7NgayMacDinh();
            return;
        }

        lblBannerLoiHoaDon.setVisible(false);
        lblBannerLoiHoaDon.setManaged(false);
        dsHoaDonTimKiem.clear();

        try {
            List<HoaDon> dsHoaDon = controller.traCuuDanhSachHoaDonDeDoiTra(tuKhoa.trim());
            dsHoaDonTimKiem.setAll(dsHoaDon);

            if (dsHoaDon.size() == 1) {
                HoaDon hdDuyNhat = dsHoaDon.get(0);
                tblKetQuaHoaDon.getSelectionModel().select(0);
                chonHoaDonGoc(hdDuyNhat);
            } else if (dsHoaDon.size() > 1) {
                tblKetQuaHoaDon.getSelectionModel().clearSelection();
                hoaDonDangChon = null;
                hoaDonHopLe = false;
                pnlThongTinHoaDonChon.setVisible(false);
                pnlThongTinHoaDonChon.setManaged(false);
                dsSanPhamDoiTra.clear();
                tinhToanLaiTaiChinh();
                hienBannerLoi("Tìm thấy " + dsHoaDon.size() + " hóa đơn hợp lệ trong 7 ngày. Vui lòng nhấp chọn 1 hóa đơn trong bảng!");
            }
        } catch (Exception ex) {
            hienBannerLoi(ex.getMessage() != null ? ex.getMessage() : "Không tìm thấy hóa đơn gốc!");
            pnlThongTinHoaDonChon.setVisible(false);
            pnlThongTinHoaDonChon.setManaged(false);
            dsSanPhamDoiTra.clear();
            tinhToanLaiTaiChinh();
        }
    }

    private void hienBannerLoi(String thongBao) {
        lblBannerLoiHoaDon.setText("Lưu ý: " + thongBao);
        lblBannerLoiHoaDon.setVisible(true);
        lblBannerLoiHoaDon.setManaged(true);
    }

    private void chonHoaDonGoc(HoaDon hd) {
        this.hoaDonDangChon = hd;
        pnlThongTinHoaDonChon.getChildren().clear();

        long soNgay = ChronoUnit.DAYS.between(hd.getNgayLap(), LocalDate.now());
        long soNgayConLai = 7 - soNgay;
        boolean daThanhToan = "Đã thanh toán".equalsIgnoreCase(hd.getTrangThaiThanhToan());
        boolean conHan = soNgay >= 0 && soNgay <= 7;

        this.hoaDonHopLe = daThanhToan && conHan;

        HBox hangKhach = new HBox(20);
        hangKhach.setAlignment(Pos.CENTER_LEFT);
        String tenKhach = hd.getTenKhachHang() != null ? hd.getTenKhachHang() : "Khách vãng lai";
        String sdtKhach = hd.getSoDienThoaiKhachHang() != null ? hd.getSoDienThoaiKhachHang() : "--";
        Label lblKhach = new Label("Khách hàng: " + tenKhach + " (" + sdtKhach + ")");
        lblKhach.setStyle("-fx-font-weight: bold;");

        TaiKhoan tk = SessionManager.getTaiKhoanHienTai();
        String tenNv = (tk != null && tk.getHoTenNhanVien() != null) ? tk.getHoTenNhanVien() : "Nhân viên hiện tại";
        Label lblNhanVien = new Label("Nhân viên lập: " + tenNv);

        Label lblNgayMua = new Label("Ngày mua: " + (hd.getNgayLap() != null ? hd.getNgayLap().format(DINH_DANG_NGAY) : ""));
        Label lblTrangThai = new Label("Trạng thái: " + (daThanhToan ? "Đã thanh toán" : "Chưa thanh toán"));
        Label lblHan = new Label(conHan ? ("Thời hạn: Còn " + soNgayConLai + " ngày đổi trả") : ("Thời hạn: Quá hạn (" + soNgay + " ngày)"));
        lblHan.setStyle("-fx-font-weight: bold;");

        hangKhach.getChildren().addAll(lblKhach, lblNhanVien, lblNgayMua, lblTrangThai, lblHan);

        pnlThongTinHoaDonChon.getChildren().add(hangKhach);
        pnlThongTinHoaDonChon.setVisible(true);
        pnlThongTinHoaDonChon.setManaged(true);

        napDanhSachSanPhamTuHoaDon(hd);
        tinhToanLaiTaiChinh();
    }

    private void napDanhSachSanPhamTuHoaDon(HoaDon hd) {
        dsSanPhamDoiTra.clear();
        if (hd == null) return;

        if (hd.getDanhSachSanPham() != null) {
            for (ChiTietHoaDonSanPham ct : hd.getDanhSachSanPham()) {
                String maHang = ct.getMaSanPham();
                String tenHang = ct.getTenSanPham();
                int daTra = 0;
                if (maHang != null) {
                    daTra = controller.laySoLuongDaDoiTra(hd.getMaHoaDon(), maHang);
                }

                double donGiaSp = ct.getDonGia();
                DongTraHang dong = new DongTraHang(maHang, tenHang, donGiaSp, ct.getSoLuong(), daTra, false);

                dong.getSpnSoLuongTra().valueProperty().addListener((obs, o, n) -> {
                    dong.getLblThanhTien().setText(DINH_DANG_TIEN.format(dong.tinhThanhTien()));
                    tinhToanLaiTaiChinh();
                });

                dong.getCboLyDo().valueProperty().addListener((obs, o, n) -> kiemTraLaiMaTran(dong));
                dong.getCboTinhTrang().valueProperty().addListener((obs, o, n) -> kiemTraLaiMaTran(dong));
                dong.getTxtGhiChu().textProperty().addListener((obs, o, n) -> kiemTraLaiMaTran(dong));

                dong.getBtnDinhKem().setOnAction(e -> {
                    FileChooser fc = new FileChooser();
                    fc.setTitle("Chọn ảnh minh chứng lỗi/hư hỏng");
                    fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Ảnh", "*.png", "*.jpg", "*.jpeg"));
                    File f = fc.showOpenDialog(getScene().getWindow());
                    if (f != null) {
                        dong.setFileBangChung(f);
                        dong.getBtnDinhKem().setText(f.getName());
                    }
                });

                dsSanPhamDoiTra.add(dong);
            }
        }

        if (hd.getDanhSachDichVu() != null) {
            for (ChiTietHoaDonDichVu ct : hd.getDanhSachDichVu()) {
                double donGiaDv = ct.getDonGia();
                DongTraHang dong = new DongTraHang(ct.getMaDichVu(), ct.getTenDichVu() + " (Dịch vụ - không đổi trả)",
                        donGiaDv, ct.getSoLuong(), 0, true);
                dsSanPhamDoiTra.add(dong);
            }
        }
    }

    private void kiemTraLaiMaTran(DongTraHang dong) {
        if (!dong.isChon() || dong.isLaDichVu()) return;

        String lyDo = dong.getCboLyDo().getValue();
        String tinhTrang = dong.getCboTinhTrang().getValue();
        String ghiChu = dong.getTxtGhiChu().getText();

        if (lyDo == null || tinhTrang == null) return;

        try {
            ChiTietPhieuDoiTra testCt = new ChiTietPhieuDoiTra();
            testCt.setLyDo(lyDo);
            testCt.setTinhTrang(tinhTrang);
            testCt.setGhiChu(ghiChu);
            controller.kiemTraHopLeChiTiet(testCt);

            dong.getCboLyDo().setStyle("");
            dong.getCboTinhTrang().setStyle("");
            dong.getLblThanhTien().setText(DINH_DANG_TIEN.format(dong.tinhThanhTien()));
        } catch (IllegalArgumentException ex) {
            hienThongBaoCanhBao("Quy tắc đổi trả không hợp lệ:\n" + ex.getMessage());
        }
    }

    private void themSanPhamDoi() {
        String maHoacTuKhoa = txtMaVachSanPham.getText();
        try {
            SanPham sp = controller.timSanPhamTheoMaVachHoacTuKhoa(maHoacTuKhoa);
            int slThem = spnSoLuongDoi.getValue() != null ? spnSoLuongDoi.getValue() : 1;

            for (DongDoiHang d : dsSanPhamDoiMoi) {
                if (d.getSanPham().getMaSanPham().equalsIgnoreCase(sp.getMaSanPham())) {
                    int slMoi = d.getSoLuong() + 1;
                    if (slMoi > sp.getSoLuongTon()) {
                        hienThongBaoCanhBao("Số lượng đổi vượt quá tồn kho hiện tại (" + sp.getSoLuongTon() + ")!");
                        return;
                    }
                    d.setSoLuong(slMoi);
                    lblCanhBaoTonKho.setText("Đã +1 '" + sp.getTenSanPham() + "' (Tồn: " + sp.getSoLuongTon() + ")");
                    txtMaVachSanPham.clear();
                    tblSanPhamDoiMoi.refresh();
                    tinhToanLaiTaiChinh();
                    return;
                }
            }

            if (slThem > sp.getSoLuongTon()) {
                hienThongBaoCanhBao("Sản phẩm '" + sp.getTenSanPham() + "' chỉ còn tồn kho " + sp.getSoLuongTon() + "!");
                return;
            }

            dsSanPhamDoiMoi.add(new DongDoiHang(sp, slThem));
            lblCanhBaoTonKho.setText("Đã thêm: " + sp.getTenSanPham() + " (Tồn: " + sp.getSoLuongTon() + ")");
            txtMaVachSanPham.clear();
            tinhToanLaiTaiChinh();
        } catch (IllegalArgumentException ex) {
            hienThongBaoCanhBao(ex.getMessage());
        }
    }

    /**
     * Dựng đối tượng PhieuDoiTra tạm thời để ủy quyền việc tính toán tiền hoàn / tiền bù cho Entity.
     */
    private PhieuDoiTra taoPhieuTamDeTinhToan() {
        PhieuDoiTra phieuTam = new PhieuDoiTra();
        phieuTam.setLoaiXuLy(rdoTraHang.isSelected() ? "Hoàn tiền" : "Đổi hàng");
        List<ChiTietPhieuDoiTra> dsCt = new ArrayList<>();

        for (DongTraHang d : dsSanPhamDoiTra) {
            if (d.isChon() && !d.isLaDichVu()) {
                ChiTietPhieuDoiTra ct = new ChiTietPhieuDoiTra();
                ct.setMaSanPham(d.getMaSanPham());
                ct.setTenSanPham(d.getTenSanPham());
                ct.setDonGia(d.getDonGia());
                ct.setSoLuong(d.getSoLuongTra());
                dsCt.add(ct);
            }
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

    private void lamMoiToanBo() {
        txtTimKiemHoaDon.clear();
        txtMaVachSanPham.clear();
        lblCanhBaoTonKho.setText("");
        lblBannerLoiHoaDon.setVisible(false);
        lblBannerLoiHoaDon.setManaged(false);
        pnlThongTinHoaDonChon.getChildren().clear();
        pnlThongTinHoaDonChon.setVisible(false);
        pnlThongTinHoaDonChon.setManaged(false);
        dsSanPhamDoiTra.clear();
        dsSanPhamDoiMoi.clear();
        rdoTraHang.setSelected(true);
        tinhToanLaiTaiChinh();
        hoaDonDangChon = null;
        hoaDonHopLe = false;
        napDanhSachHoaDon7NgayMacDinh();
    }

    private void xemLaiTruocKhiLuu() {
        if (hoaDonDangChon == null) {
            hienThongBaoCanhBao("Chưa chọn hóa đơn gốc!");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("TÓM TẮT PHIẾU ĐỔI TRẢ:\n\n");
        sb.append("- Hóa đơn gốc: ").append(hoaDonDangChon.getMaHoaDon()).append("\n");
        sb.append("- Khách hàng: ").append(hoaDonDangChon.getTenKhachHang() != null ? hoaDonDangChon.getTenKhachHang() : "Khách vãng lai").append("\n");
        sb.append("- Hình thức xử lý: ").append(rdoTraHang.isSelected() ? "Trả hàng, hoàn tiền" : "Đổi sang sản phẩm khác").append("\n\n");

        sb.append("MẶT HÀNG TRẢ LẠI:\n");
        int demTra = 0;
        for (DongTraHang d : dsSanPhamDoiTra) {
            if (d.isChon() && !d.isLaDichVu()) {
                demTra++;
                sb.append(String.format("  + %s | SL: %d | Lý do: %s | Tình trạng: %s | Tiền: %s\n",
                        d.getTenSanPham(), d.getSoLuongTra(), d.getCboLyDo().getValue(), d.getCboTinhTrang().getValue(),
                        DINH_DANG_TIEN.format(d.tinhThanhTien())));
            }
        }
        if (demTra == 0) {
            sb.append("  (Chưa chọn mặt hàng nào)\n");
        }

        if (rdoDoiHang.isSelected()) {
            sb.append("\nMẶT HÀNG ĐỔI MỚI:\n");
            for (DongDoiHang d : dsSanPhamDoiMoi) {
                sb.append(String.format("  + %s | SL: %d | Tiền: %s\n",
                        d.getSanPham().getTenSanPham(), d.getSoLuong(), DINH_DANG_TIEN.format(d.getThanhTien())));
            }
        }

        sb.append("\nTỔNG KẾT TÀI CHÍNH:\n");
        sb.append("  - Tổng giá trị hàng trả: ").append(lblTongTienTra.getText()).append("\n");
        sb.append("  - Tổng giá trị hàng đổi: ").append(lblTongTienDoi.getText()).append("\n");
        sb.append("  - Kết quả: ").append(lblChenhLechCuoi.getText()).append("\n");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Happy Tails - Xem lại phiếu đổi trả");
        alert.setHeaderText("Kiểm tra lại thông tin trước khi lưu chính thức");
        alert.setContentText(sb.toString());
        alert.getDialogPane().setPrefWidth(550);
        alert.showAndWait();
    }

    private void xacNhanVaLuuPhiu() {
        if (hoaDonDangChon == null) {
            hienThongBaoCanhBao("Vui lòng tìm và chọn hóa đơn gốc trước khi lập phiếu!");
            return;
        }

        if (!hoaDonHopLe) {
            hienThongBaoCanhBao("Hóa đơn gốc không đạt điều kiện đổi trả (chưa thanh toán hoặc quá thời hạn 7 ngày)!");
            return;
        }

        List<ChiTietPhieuDoiTra> dsChiTiet = new ArrayList<>();
        int tongSoLuongTra = 0;

        for (DongTraHang d : dsSanPhamDoiTra) {
            if (d.isChon() && !d.isLaDichVu()) {
                if (d.getCboLyDo().getValue() == null || d.getCboTinhTrang().getValue() == null) {
                    hienThongBaoCanhBao("Vui lòng chọn đầy đủ Lý do và Tình trạng cho sản phẩm: " + d.getTenSanPham());
                    return;
                }

                ChiTietPhieuDoiTra ct = new ChiTietPhieuDoiTra();
                ct.setHoaDon(hoaDonDangChon);
                ct.setMaSanPham(d.getMaSanPham());
                ct.setTenSanPham(d.getTenSanPham());
                ct.setSoLuong(d.getSoLuongTra());
                ct.setDonGia(d.getDonGia());
                ct.setLyDo(d.getCboLyDo().getValue());
                ct.setTinhTrang(d.getCboTinhTrang().getValue());
                ct.setGhiChu(d.getTxtGhiChu().getText());
                ct.setHuongXuLy("Nguyên vẹn".equalsIgnoreCase(ct.getTinhTrang()) ? "Thu lại vào kho" : "Hủy bỏ");

                try {
                    controller.kiemTraHopLeChiTiet(ct);
                } catch (IllegalArgumentException ex) {
                    hienThongBaoCanhBao("Lỗi kiểm tra quy tắc đổi trả: " + ex.getMessage());
                    return;
                }

                tongSoLuongTra += d.getSoLuongTra();
                dsChiTiet.add(ct);
            }
        }

        if (dsChiTiet.isEmpty()) {
            hienThongBaoCanhBao("Vui lòng chọn ít nhất 1 sản phẩm hợp lệ cần đổi trả!");
            return;
        }

        if (rdoDoiHang.isSelected() && dsSanPhamDoiMoi.isEmpty()) {
            hienThongBaoCanhBao("Bạn đã chọn hình thức 'Đổi sang sản phẩm khác' nhưng chưa thêm sản phẩm đổi nào!");
            return;
        }

        if (rdoDoiHang.isSelected() && !dsSanPhamDoiMoi.isEmpty()) {
            DongDoiHang doiDau = dsSanPhamDoiMoi.get(0);
            ChiTietPhieuDoiTra ctDau = dsChiTiet.get(0);
            ctDau.setSanPhamDoi(doiDau.getSanPham());
            ctDau.setSoLuongDoi(doiDau.getSoLuong());
            ctDau.setDonGiaDoi(doiDau.getDonGia());
        }

        PhieuDoiTra phieu = new PhieuDoiTra();
        phieu.setMaPhieuDoiTra(controller.sinhMaPhieu());
        phieu.setHoaDon(hoaDonDangChon);
        phieu.setNgayLap(LocalDate.now());
        phieu.setMaKhachHang(hoaDonDangChon.getMaKhachHang());
        phieu.setTenKhachHang(hoaDonDangChon.getTenKhachHang());
        phieu.setSoDienThoaiKhachHang(hoaDonDangChon.getSoDienThoaiKhachHang());
        phieu.setSoLuong(tongSoLuongTra);

        // Tự động lấy Nhân viên từ SessionManager.getTaiKhoanHienTai().getNhanVien()
        TaiKhoan tkHienTai = SessionManager.getTaiKhoanHienTai();
        if (tkHienTai != null && tkHienTai.getNhanVien() != null) {
            phieu.setNhanVien(tkHienTai.getNhanVien());
        } else if (tkHienTai != null) {
            phieu.setMaNhanVien(tkHienTai.getMaNhanVien());
            phieu.setTenNhanVien(tkHienTai.getHoTenNhanVien());
        } else {
            phieu.setMaNhanVien("NV-20261001-000001");
        }

        if (rdoTraHang.isSelected()) {
            phieu.setLoaiXuLy("Hoàn tiền");
            phieu.setHinhThucHoan(cboHinhThucHoan.getValue());
        } else {
            phieu.setLoaiXuLy("Đổi hàng");
            phieu.setHinhThucHoan(null);
        }

        phieu.setTrangThai("Chờ xử lý");
        phieu.setDanhSachSanPham(dsChiTiet);

        Alert xacNhan = new Alert(Alert.AlertType.CONFIRMATION);
        xacNhan.setTitle("Xác nhận lưu phiếu đổi trả");
        xacNhan.setHeaderText("Xác nhận lưu phiếu đổi trả với khách hàng?");
        xacNhan.setContentText(String.format("Mã phiếu mới: %s\nHóa đơn gốc: %s\nSố mặt hàng trả: %d\nKết quả tài chính: %s",
                phieu.getMaPhieuDoiTra(), phieu.getMaHoaDon(), dsChiTiet.size(), lblChenhLechCuoi.getText()));

        if (xacNhan.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                boolean thanhCong = controller.lapPhieuDoiTra(phieu);
                if (thanhCong) {
                    hienHopThoaiThanhCong(phieu);
                } else {
                    hienThongBaoCanhBao("Không thể lưu phiếu đổi trả vào hệ thống!");
                }
            } catch (Exception ex) {
                hienThongBaoCanhBao("Lỗi khi lưu phiếu đổi trả: " + ex.getMessage());
            }
        }
    }

    private void hienHopThoaiThanhCong(PhieuDoiTra phieu) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setHeaderText("LẬP PHIẾU ĐỔI TRẢ THÀNH CÔNG!");
        alert.setContentText("Mã phiếu đổi trả: " + phieu.getMaPhieuDoiTra() + "\nTrạng thái: " + phieu.getTrangThai());

        ButtonType btnIn = new ButtonType("In phiếu đổi trả");
        ButtonType btnMoi = new ButtonType("Lập phiếu mới", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(btnIn, btnMoi);

        alert.showAndWait().ifPresent(type -> {
            if (type == btnIn) {
                inPhieuDoiTra(phieu);
            }
            lamMoiToanBo();
        });
    }

    private void inPhieuDoiTra(PhieuDoiTra phieu) {
        Alert dialogIn = new Alert(Alert.AlertType.INFORMATION);
        dialogIn.setTitle("Happy Tails - In phiếu đổi trả");
        dialogIn.setHeaderText("Lệnh in gửi thành công đến máy in nhiệt POS");
        dialogIn.setContentText("Đã in phiếu đổi trả: " + phieu.getMaPhieuDoiTra());
        dialogIn.showAndWait();
    }

    private void hienThongBaoCanhBao(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}
