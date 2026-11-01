package com.petshop.view.doitra;

import com.petshop.controller.PhieuDoiTraController;
import com.petshop.model.*;
import com.petshop.util.SessionManager;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Man hinh 1: Lap phieu doi tra (RET_CREATE / DT_TAO).
 * Bo cuc 3 vung, thanh tien trinh, ma tran QL5, 2 the hinh thuc xu ly, thanh tom tat co dinh.
 */
public class LapPhieuDoiTraView extends VBox {

    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 VNĐ");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PhieuDoiTraController controller;

    // Thanh tien trinh
    private final Label lblStep1 = new Label("1. Tìm hóa đơn gốc");
    private final Label lblStep2 = new Label("2. Chọn sản phẩm đổi trả");
    private final Label lblStep3 = new Label("3. Xác nhận & lưu");

    // Vung A - Tim hoa don
    private final TextField txtTimKiemHoaDon = new TextField();
    private final Button btnTimHoaDon = new Button("Tìm hóa đơn");
    private final Label lblBannerLoiHoaDon = new Label();
    private final TableView<HoaDon> tblKetQuaHoaDon = new TableView<>();
    private final ObservableList<HoaDon> dsHoaDonTimKiem = FXCollections.observableArrayList();
    private final VBox pnlThongTinHoaDonChon = new VBox(8);

    // Hoa don dang chon
    private HoaDon hoaDonDangChon = null;
    private boolean hoaDonHopLe = false;

    // Vung B - Bang san pham doi tra
    public static class DongTraHang {
        private final BooleanProperty chon = new SimpleBooleanProperty(false);
        private final String maSanPham;
        private final String tenSanPham;
        private final BigDecimal donGia;
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

        public DongTraHang(String maSanPham, String tenSanPham, BigDecimal donGia,
                           int soLuongDaMua, int soLuongDaTraTruoc, boolean laDichVu) {
            this.maSanPham = maSanPham;
            this.tenSanPham = tenSanPham;
            this.donGia = donGia != null ? donGia : BigDecimal.ZERO;
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
            this.btnDinhKem.setStyle("-fx-font-size: 11px; -fx-padding: 3 8;");

            this.lblThanhTien = new Label("0 VNĐ");
            this.lblThanhTien.setStyle("-fx-font-weight: bold; -fx-text-fill: #1570ef;");

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
        public String getMaSanPham() { return maSanPham; }
        public String getTenSanPham() { return tenSanPham; }
        public BigDecimal getDonGia() { return donGia; }
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

        public BigDecimal tinhThanhTien() {
            if (!isChon() || laDichVu) return BigDecimal.ZERO;
            return donGia.multiply(BigDecimal.valueOf(getSoLuongTra()));
        }
    }

    private final TableView<DongTraHang> tblSanPhamDoiTra = new TableView<>();
    private final ObservableList<DongTraHang> dsSanPhamDoiTra = FXCollections.observableArrayList();

    // Vung C - Hinh thuc xu ly
    private final RadioButton rdoTraHang = new RadioButton("Trả hàng, hoàn tiền");
    private final RadioButton rdoDoiHang = new RadioButton("Đổi sang sản phẩm khác");
    private final ToggleGroup grpHinhThuc = new ToggleGroup();

    // The hoan tien
    private final VBox pnlTheHoanTien = new VBox(10);
    private final ComboBox<String> cboHinhThucHoan = new ComboBox<>(FXCollections.observableArrayList("Tiền mặt", "Chuyển khoản", "Voucher"));
    private final Label lblTienHoanDuKien = new Label("0 VNĐ");

    // The doi hang
    private final VBox pnlTheDoiHang = new VBox(10);
    private final TextField txtTimSanPhamDoi = new TextField();
    private final ComboBox<SanPham> cboSanPhamDoi = new ComboBox<>();
    private final Spinner<Integer> spnSoLuongDoi = new Spinner<>(1, 100, 1);
    private final Button btnThemSanPhamDoi = new Button("+ Thêm hàng đổi");
    private final Label lblCanhBaoTonKho = new Label();

    public static class DongDoiHang {
        private final SanPham sanPham;
        private int soLuong;
        private BigDecimal thanhTien;

        public DongDoiHang(SanPham sanPham, int soLuong) {
            this.sanPham = sanPham;
            this.soLuong = soLuong;
            this.thanhTien = sanPham.getGiaBan().multiply(BigDecimal.valueOf(soLuong));
        }

        public SanPham getSanPham() { return sanPham; }
        public int getSoLuong() { return soLuong; }
        public void setSoLuong(int soLuong) {
            this.soLuong = soLuong;
            this.thanhTien = sanPham.getGiaBan().multiply(BigDecimal.valueOf(soLuong));
        }
        public BigDecimal getThanhTien() { return thanhTien; }
    }

    private final TableView<DongDoiHang> tblSanPhamDoiMoi = new TableView<>();
    private final ObservableList<DongDoiHang> dsSanPhamDoiMoi = FXCollections.observableArrayList();
    private final Label lblDongChenhLech = new Label("Không phát sinh chênh lệch (0 VNĐ)");

    // Vung D - Thanh tom tat co dinh
    private final Label lblTongTienTra = new Label("0 VNĐ");
    private final Label lblTongTienDoi = new Label("0 VNĐ");
    private final Label lblChenhLechCuoi = new Label("0 VNĐ");
    private final Button btnHuy = new Button("Hủy / Làm mới");
    private final Button btnXemLai = new Button("Xem lại");
    private final Button btnLuuPhiu = new Button("✔ Lưu phiếu đổi trả");

    public LapPhieuDoiTraView() {
        this.controller = new PhieuDoiTraController();
        khoiTaoGiaoDien();
        dangKySuKien();
    }

    private void khoiTaoGiaoDien() {
        this.setSpacing(14);
        this.setPadding(new Insets(16));
        this.setStyle("-fx-background-color: #f8fafc;");

        // 1. Thanh tien trinh
        HBox pnlTienTrinh = taoThanhTienTrinh();

        // 2. Vung A - Tim hoa don goc
        VBox vungA = taoVungATimHoaDon();

        // 3. Vung B - Chon san pham doi tra
        VBox vungB = taoVungBChonSanPham();

        // 4. Vung C - Hinh thuc xu ly
        VBox vungC = taoVungCHinhThucXuLy();

        // 5. Vung D - Thanh tom tat co dinh
        HBox vungD = taoVungDTomTatCoDinh();

        ScrollPane scroll = new ScrollPane();
        VBox noiDungCuon = new VBox(14, pnlTienTrinh, vungA, vungB, vungC);
        noiDungCuon.setPadding(new Insets(0, 8, 14, 0));
        scroll.setContent(noiDungCuon);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        this.getChildren().addAll(scroll, vungD);
    }

    private HBox taoThanhTienTrinh() {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10, 16, 10, 16));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px;");

        capNhatMauTienTrinh(1);

        Label muiTen1 = new Label(" ➔ ");
        muiTen1.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold;");
        Label muiTen2 = new Label(" ➔ ");
        muiTen2.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold;");

        box.getChildren().addAll(lblStep1, muiTen1, lblStep2, muiTen2, lblStep3);
        return box;
    }

    private void capNhatMauTienTrinh(int stepHienTai) {
        String styleActive = "-fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: #2563eb; -fx-padding: 6 14; -fx-background-radius: 16px;";
        String styleInActive = "-fx-font-weight: normal; -fx-text-fill: #64748b; -fx-background-color: #f1f5f9; -fx-padding: 6 14; -fx-background-radius: 16px;";

        lblStep1.setStyle(stepHienTai == 1 ? styleActive : styleInActive);
        lblStep2.setStyle(stepHienTai == 2 ? styleActive : styleInActive);
        lblStep3.setStyle(stepHienTai == 3 ? styleActive : styleInActive);
    }

    private VBox taoVungATimHoaDon() {
        VBox card = new VBox(10);
        card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px;");

        Label lblTieuDe = new Label("VÙNG A - TÌM HÓA ĐƠN GỐC CẦN ĐỔI TRẢ");
        lblTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1e293b;");

        HBox hangTim = new HBox(10);
        hangTim.setAlignment(Pos.CENTER_LEFT);
        txtTimKiemHoaDon.setPromptText("Nhập Mã hóa đơn / Số điện thoại / Mã khách hàng...");
        txtTimKiemHoaDon.setPrefWidth(380);
        txtTimKiemHoaDon.setStyle("-fx-padding: 6 10;");

        btnTimHoaDon.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 16; -fx-cursor: hand;");
        hangTim.getChildren().addAll(new Label("Tìm kiếm:"), txtTimKiemHoaDon, btnTimHoaDon);

        lblBannerLoiHoaDon.setManaged(false);
        lblBannerLoiHoaDon.setVisible(false);
        lblBannerLoiHoaDon.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-padding: 8 12; -fx-background-radius: 6px; -fx-font-weight: bold;");

        // Bang ket qua hoa don tim thay
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
        colKhach.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTenKhachHang() != null ? d.getValue().getTenKhachHang() : "Khách vãng lai"));
        colKhach.setPrefWidth(150);

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

        // Khoi thong tin chi tiet hoa don duoc chon
        pnlThongTinHoaDonChon.setManaged(false);
        pnlThongTinHoaDonChon.setVisible(false);
        pnlThongTinHoaDonChon.setPadding(new Insets(10));
        pnlThongTinHoaDonChon.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 6px; -fx-border-color: #cbd5e1; -fx-border-radius: 6px;");

        card.getChildren().addAll(lblTieuDe, hangTim, lblBannerLoiHoaDon, tblKetQuaHoaDon, pnlThongTinHoaDonChon);
        return card;
    }

    private VBox taoVungBChonSanPham() {
        VBox card = new VBox(10);
        card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px;");

        Label lblTieuDe = new Label("VÙNG B - CHỌN SẢN PHẨM ĐỔI TRẢ & TÌNH TRẠNG HÀNG (Ma trận QL5)");
        lblTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1e293b;");

        Label lblGhiChuMaTran = new Label("(*) Lưu ý: 'Không phù hợp' và 'Khác' chỉ chấp nhận khi sản phẩm 'Nguyên vẹn'. Dịch vụ chăm sóc không được đổi trả.");
        lblGhiChuMaTran.setStyle("-fx-font-size: 11px; -fx-text-fill: #e11d48; -fx-font-style: italic;");

        tblSanPhamDoiTra.setPrefHeight(200);
        tblSanPhamDoiTra.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        TableColumn<DongTraHang, Boolean> colChon = new TableColumn<>("Chọn");
        colChon.setCellValueFactory(d -> d.getValue().chonProperty());
        colChon.setCellFactory(col -> new TableCell<DongTraHang, Boolean>() {
            private final CheckBox cb = new CheckBox();
            {
                cb.setOnAction(e -> {
                    DongTraHang d = getTableView().getItems().get(getIndex());
                    if (d.isLaDichVu()) {
                        cb.setSelected(false);
                        hienThongBaoCanhBao("Hóa đơn/dòng dịch vụ không được phép đổi trả theo QL4!");
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

        TableColumn<DongTraHang, String> colGia = new TableColumn<>("Đơn giá");
        colGia.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().getDonGia())));
        colGia.setPrefWidth(95);

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
        card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px;");

        Label lblTieuDe = new Label("VÙNG C - HÌNH THỨC XỬ LÝ ĐỔI TRẢ");
        lblTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1e293b;");

        rdoTraHang.setToggleGroup(grpHinhThuc);
        rdoDoiHang.setToggleGroup(grpHinhThuc);
        rdoTraHang.setSelected(true);

        rdoTraHang.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        rdoDoiHang.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        HBox hangChon = new HBox(30, rdoTraHang, rdoDoiHang);
        hangChon.setAlignment(Pos.CENTER_LEFT);

        // The 1: Hoan tien
        pnlTheHoanTien.setPadding(new Insets(10));
        pnlTheHoanTien.setStyle("-fx-background-color: #f0fdf4; -fx-border-color: #bbf7d0; -fx-border-radius: 6px; -fx-background-radius: 6px;");
        cboHinhThucHoan.setValue("Tiền mặt");
        HBox hangHoan = new HBox(16);
        hangHoan.setAlignment(Pos.CENTER_LEFT);
        lblTienHoanDuKien.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #16a34a;");
        hangHoan.getChildren().addAll(
                new Label("Hình thức hoàn:"), cboHinhThucHoan,
                new Label("Số tiền hoàn trả khách:"), lblTienHoanDuKien
        );
        pnlTheHoanTien.getChildren().add(hangHoan);

        // The 2: Doi hang
        pnlTheDoiHang.setPadding(new Insets(10));
        pnlTheDoiHang.setStyle("-fx-background-color: #eff6ff; -fx-border-color: #bfdbfe; -fx-border-radius: 6px; -fx-background-radius: 6px;");
        pnlTheDoiHang.setManaged(false);
        pnlTheDoiHang.setVisible(false);

        HBox hangThemDoi = new HBox(10);
        hangThemDoi.setAlignment(Pos.CENTER_LEFT);
        txtTimSanPhamDoi.setPromptText("Lọc nhanh sản phẩm...");
        txtTimSanPhamDoi.setPrefWidth(160);

        cboSanPhamDoi.setPromptText("-- Chọn sản phẩm đổi mới --");
        cboSanPhamDoi.setPrefWidth(260);

        spnSoLuongDoi.setPrefWidth(70);
        btnThemSanPhamDoi.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 12;");

        lblCanhBaoTonKho.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px;");

        hangThemDoi.getChildren().addAll(
                new Label("Tìm SP:"), txtTimSanPhamDoi,
                cboSanPhamDoi,
                new Label("SL:"), spnSoLuongDoi,
                btnThemSanPhamDoi, lblCanhBaoTonKho
        );

        tblSanPhamDoiMoi.setPrefHeight(120);
        tblSanPhamDoiMoi.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<DongDoiHang, String> colTenDoi = new TableColumn<>("Sản phẩm đổi");
        colTenDoi.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSanPham().getTenSanPham()));

        TableColumn<DongDoiHang, String> colGiaDoi = new TableColumn<>("Đơn giá");
        colGiaDoi.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().getSanPham().getGiaBan())));

        TableColumn<DongDoiHang, Integer> colSlDoi = new TableColumn<>("SL");
        colSlDoi.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSoLuong()));

        TableColumn<DongDoiHang, String> colTtDoi = new TableColumn<>("Thành tiền");
        colTtDoi.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().getThanhTien())));

        TableColumn<DongDoiHang, Void> colXoaDoi = new TableColumn<>("Thao tác");
        colXoaDoi.setCellFactory(c -> new TableCell<>() {
            private final Button btnXoa = new Button("Xóa");
            {
                btnXoa.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-font-size: 11px;");
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
        HBox bar = new HBox(20);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 16, 12, 16));
        bar.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #cbd5e1; -fx-border-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, -2);");

        lblTongTienTra.setStyle("-fx-font-weight: bold; -fx-text-fill: #dc2626; -fx-font-size: 13px;");
        lblTongTienDoi.setStyle("-fx-font-weight: bold; -fx-text-fill: #2563eb; -fx-font-size: 13px;");
        lblChenhLechCuoi.setStyle("-fx-font-weight: bold; -fx-text-fill: #16a34a; -fx-font-size: 15px;");

        HBox pnlThongKe = new HBox(18);
        pnlThongKe.setAlignment(Pos.CENTER_LEFT);
        pnlThongKe.getChildren().addAll(
                new Label("Tổng hàng trả:"), lblTongTienTra,
                new Label("Tổng hàng đổi:"), lblTongTienDoi,
                new Label("Chênh lệch:"), lblChenhLechCuoi
        );
        HBox.setHgrow(pnlThongKe, Priority.ALWAYS);

        btnHuy.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-padding: 8 16; -fx-font-weight: bold; -fx-cursor: hand;");
        btnXemLai.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0284c7; -fx-padding: 8 16; -fx-font-weight: bold; -fx-cursor: hand;");
        btnLuuPhiu.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white; -fx-padding: 8 20; -fx-font-weight: bold; -fx-cursor: hand;");

        bar.getChildren().addAll(pnlThongKe, btnHuy, btnXemLai, btnLuuPhiu);
        return bar;
    }

    private void dangKySuKien() {
        btnTimHoaDon.setOnAction(e -> timKiemHoaDon());
        txtTimKiemHoaDon.setOnAction(e -> timKiemHoaDon());

        tblKetQuaHoaDon.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
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

        // Chon san pham de doi moi
        napDanhSachSanPhamDoi();

        txtTimSanPhamDoi.textProperty().addListener((obs, oldVal, newVal) -> {
            locDanhSachSanPhamDoi(newVal);
        });

        cboSanPhamDoi.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int ton = newVal.getSoLuongTon();
                if (ton <= 0) {
                    lblCanhBaoTonKho.setText("Sản phẩm đã hết hàng trong kho!");
                    btnThemSanPhamDoi.setDisable(true);
                } else {
                    lblCanhBaoTonKho.setText("Tồn kho: " + ton);
                    btnThemSanPhamDoi.setDisable(false);
                    spnSoLuongDoi.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, ton, 1));
                }
            } else {
                lblCanhBaoTonKho.setText("");
            }
        });

        btnThemSanPhamDoi.setOnAction(e -> themSanPhamDoi());
        btnHuy.setOnAction(e -> lamMoiToanBo());
        btnXemLai.setOnAction(e -> xemLaiTruocKhiLuu());
        btnLuuPhiu.setOnAction(e -> xacNhanVaLuuPhiu());
    }

    private void timKiemHoaDon() {
        String tuKhoa = txtTimKiemHoaDon.getText();
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            hienBannerLoi("Vui lòng nhập Mã hóa đơn, SĐT hoặc Mã khách hàng để tìm kiếm!");
            return;
        }

        lblBannerLoiHoaDon.setVisible(false);
        lblBannerLoiHoaDon.setManaged(false);
        dsHoaDonTimKiem.clear();

        try {
            HoaDon hd = controller.traCuuHoaDonDeDoiTra(tuKhoa.trim());
            if (hd != null) {
                dsHoaDonTimKiem.add(hd);
                tblKetQuaHoaDon.getSelectionModel().select(0);
                capNhatMauTienTrinh(2);
            }
        } catch (Exception ex) {
            hienBannerLoi(ex.getMessage() != null ? ex.getMessage() : "Không tìm thấy hóa đơn gốc!");
            pnlThongTinHoaDonChon.setVisible(false);
            pnlThongTinHoaDonChon.setManaged(false);
            dsSanPhamDoiTra.clear();
        }
    }

    private void hienBannerLoi(String thongBao) {
        lblBannerLoiHoaDon.setText("✖ " + thongBao);
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

        // Header info
        HBox hangKhach = new HBox(20);
        hangKhach.setAlignment(Pos.CENTER_LEFT);
        String tenKhach = hd.getTenKhachHang() != null ? hd.getTenKhachHang() : "Khách vãng lai";
        String sdtKhach = hd.getSoDienThoaiKhachHang() != null ? hd.getSoDienThoaiKhachHang() : "--";
        Label lblKhach = new Label("Khách hàng: " + tenKhach + " (" + sdtKhach + ")");
        lblKhach.setStyle("-fx-font-weight: bold;");

        Label lblNgayMua = new Label("Ngày mua: " + (hd.getNgayLap() != null ? hd.getNgayLap().format(DINH_DANG_NGAY) : ""));
        hangKhach.getChildren().addAll(lblKhach, lblNgayMua);

        // Badge kiem tra
        HBox hangBadge = new HBox(12);
        hangBadge.setAlignment(Pos.CENTER_LEFT);

        Label badgeThanhToan = new Label(daThanhToan ? "✔ Đã thanh toán" : "✖ Chưa thanh toán");
        badgeThanhToan.setStyle(daThanhToan
                ? "-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-padding: 4 10; -fx-background-radius: 12px; -fx-font-weight: bold;"
                : "-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-padding: 4 10; -fx-background-radius: 12px; -fx-font-weight: bold;");

        Label badgeHan = new Label(conHan ? ("✔ Còn hạn đổi trả (còn " + soNgayConLai + " ngày)") : ("✖ Quá hạn đổi trả (" + soNgay + " ngày > 7 ngày)"));
        badgeHan.setStyle(conHan
                ? "-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-padding: 4 10; -fx-background-radius: 12px; -fx-font-weight: bold;"
                : "-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-padding: 4 10; -fx-background-radius: 12px; -fx-font-weight: bold;");

        hangBadge.getChildren().addAll(badgeThanhToan, badgeHan);

        pnlThongTinHoaDonChon.getChildren().addAll(hangKhach, hangBadge);
        pnlThongTinHoaDonChon.setVisible(true);
        pnlThongTinHoaDonChon.setManaged(true);

        // Tai danh sach chi tiet mat hang vao Vung B
        napDanhSachSanPhamTuHoaDon(hd);
    }

    private void napDanhSachSanPhamTuHoaDon(HoaDon hd) {
        dsSanPhamDoiTra.clear();
        if (hd == null) return;

        // 1. Mat hang san pham
        if (hd.getDanhSachSanPham() != null) {
            for (ChiTietHoaDonSanPham ct : hd.getDanhSachSanPham()) {
                String maHang = ct.getMaSanPham();
                String tenHang = ct.getTenSanPham();
                int daTra = 0;
                if (maHang != null) {
                    daTra = controller.laySoLuongDaDoiTra(hd.getMaHoaDon(), maHang);
                }

                DongTraHang dong = new DongTraHang(maHang, tenHang, ct.getDonGia(), ct.getSoLuong(), daTra, false);

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
                        dong.getBtnDinhKem().setText("✔ " + f.getName());
                        dong.getBtnDinhKem().setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-font-size: 11px;");
                    }
                });

                dsSanPhamDoiTra.add(dong);
            }
        }

        // 2. Mat hang dich vu (lam mo, khong cho doi tra)
        if (hd.getDanhSachDichVu() != null) {
            for (ChiTietHoaDonDichVu ct : hd.getDanhSachDichVu()) {
                DongTraHang dong = new DongTraHang(ct.getMaDichVu(), ct.getTenDichVu() + " (Dịch vụ - QL4 không đổi trả)",
                        ct.getDonGia(), ct.getSoLuong(), 0, true);
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
            dong.getCboTinhTrang().setStyle("-fx-border-color: #dc2626;");
            hienThongBaoCanhBao("Quy tắc QL5 không hợp lệ:\n" + ex.getMessage());
        }
    }

    private final List<SanPham> dsSanPhamDoiGoc = new ArrayList<>();

    private void napDanhSachSanPhamDoi() {
        try {
            List<SanPham> sps = controller.layDanhSachSanPhamDangKinhDoanh();
            if (sps != null) {
                dsSanPhamDoiGoc.addAll(sps);
                cboSanPhamDoi.setItems(FXCollections.observableArrayList(sps));
            }
        } catch (Exception ignored) {}
    }

    private void locDanhSachSanPhamDoi(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            cboSanPhamDoi.setItems(FXCollections.observableArrayList(dsSanPhamDoiGoc));
            return;
        }
        String tk = tuKhoa.trim().toLowerCase();
        List<SanPham> loc = new ArrayList<>();
        for (SanPham sp : dsSanPhamDoiGoc) {
            if ((sp.getTenSanPham() != null && sp.getTenSanPham().toLowerCase().contains(tk))
                    || (sp.getMaSanPham() != null && sp.getMaSanPham().toLowerCase().contains(tk))) {
                loc.add(sp);
            }
        }
        cboSanPhamDoi.setItems(FXCollections.observableArrayList(loc));
    }

    private void themSanPhamDoi() {
        SanPham sp = cboSanPhamDoi.getValue();
        if (sp == null) {
            hienThongBaoCanhBao("Vui lòng chọn sản phẩm cần đổi mới!");
            return;
        }
        int sl = spnSoLuongDoi.getValue();
        if (sl > sp.getSoLuongTon()) {
            hienThongBaoCanhBao("Số lượng vượt quá tồn kho hiện tại (" + sp.getSoLuongTon() + ")!");
            return;
        }

        // Kiem tra da co trong bang doi chua
        for (DongDoiHang d : dsSanPhamDoiMoi) {
            if (d.getSanPham().getMaSanPham().equals(sp.getMaSanPham())) {
                d.setSoLuong(d.getSoLuong() + sl);
                tblSanPhamDoiMoi.refresh();
                tinhToanLaiTaiChinh();
                return;
            }
        }

        dsSanPhamDoiMoi.add(new DongDoiHang(sp, sl));
        tinhToanLaiTaiChinh();
    }

    private void tinhToanLaiTaiChinh() {
        BigDecimal tongTra = BigDecimal.ZERO;
        for (DongTraHang d : dsSanPhamDoiTra) {
            if (d.isChon() && !d.isLaDichVu()) {
                tongTra = tongTra.add(d.tinhThanhTien());
            }
        }

        BigDecimal tongDoi = BigDecimal.ZERO;
        if (rdoDoiHang.isSelected()) {
            for (DongDoiHang d : dsSanPhamDoiMoi) {
                tongDoi = tongDoi.add(d.getThanhTien());
            }
        }

        lblTongTienTra.setText(DINH_DANG_TIEN.format(tongTra));
        lblTongTienDoi.setText(DINH_DANG_TIEN.format(tongDoi));
        lblTienHoanDuKien.setText(DINH_DANG_TIEN.format(tongTra));

        BigDecimal chenhLech = tongDoi.subtract(tongTra);
        if (rdoTraHang.isSelected()) {
            lblChenhLechCuoi.setText("Hoàn trả: " + DINH_DANG_TIEN.format(tongTra));
            lblChenhLechCuoi.setStyle("-fx-font-weight: bold; -fx-text-fill: #16a34a; -fx-font-size: 14px;");
        } else {
            if (chenhLech.compareTo(BigDecimal.ZERO) > 0) {
                lblDongChenhLech.setText("Khách cần bù: " + DINH_DANG_TIEN.format(chenhLech));
                lblDongChenhLech.setStyle("-fx-font-weight: bold; -fx-text-fill: #dc2626; -fx-font-size: 13px;");
                lblChenhLechCuoi.setText("Khách bù: " + DINH_DANG_TIEN.format(chenhLech));
                lblChenhLechCuoi.setStyle("-fx-font-weight: bold; -fx-text-fill: #dc2626; -fx-font-size: 14px;");
            } else if (chenhLech.compareTo(BigDecimal.ZERO) < 0) {
                BigDecimal hoan = chenhLech.abs();
                lblDongChenhLech.setText("Hoàn lại khách: " + DINH_DANG_TIEN.format(hoan));
                lblDongChenhLech.setStyle("-fx-font-weight: bold; -fx-text-fill: #16a34a; -fx-font-size: 13px;");
                lblChenhLechCuoi.setText("Hoàn khách: " + DINH_DANG_TIEN.format(hoan));
                lblChenhLechCuoi.setStyle("-fx-font-weight: bold; -fx-text-fill: #16a34a; -fx-font-size: 14px;");
            } else {
                lblDongChenhLech.setText("Không phát sinh chênh lệch (0 VNĐ)");
                lblDongChenhLech.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569; -fx-font-size: 13px;");
                lblChenhLechCuoi.setText("0 VNĐ");
                lblChenhLechCuoi.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569; -fx-font-size: 14px;");
            }
        }
    }

    private void lamMoiToanBo() {
        txtTimKiemHoaDon.clear();
        lblBannerLoiHoaDon.setVisible(false);
        lblBannerLoiHoaDon.setManaged(false);
        tblKetQuaHoaDon.getItems().clear();
        pnlThongTinHoaDonChon.getChildren().clear();
        pnlThongTinHoaDonChon.setVisible(false);
        pnlThongTinHoaDonChon.setManaged(false);
        dsSanPhamDoiTra.clear();
        dsSanPhamDoiMoi.clear();
        rdoTraHang.setSelected(true);
        tinhToanLaiTaiChinh();
        capNhatMauTienTrinh(1);
        hoaDonDangChon = null;
        hoaDonHopLe = false;
    }

    private void xemLaiTruocKhiLuu() {
        if (hoaDonDangChon == null) {
            hienThongBaoCanhBao("Chưa chọn hóa đơn gốc!");
            return;
        }

        capNhatMauTienTrinh(3);

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
        for (DongTraHang d : dsSanPhamDoiTra) {
            if (d.isChon() && !d.isLaDichVu()) {
                if (d.getCboLyDo().getValue() == null || d.getCboTinhTrang().getValue() == null) {
                    hienThongBaoCanhBao("Vui lòng chọn đầy đủ Lý do và Tình trạng cho sản phẩm: " + d.getTenSanPham());
                    return;
                }

                ChiTietPhieuDoiTra ct = new ChiTietPhieuDoiTra();
                ct.setMaSanPham(d.getMaSanPham());
                ct.setTenSanPham(d.getTenSanPham());
                ct.setSoLuong(d.getSoLuongTra());
                ct.setDonGia(d.getDonGia());
                ct.setLyDo(d.getCboLyDo().getValue());
                ct.setTinhTrang(d.getCboTinhTrang().getValue());
                ct.setGhiChu(d.getTxtGhiChu().getText());

                try {
                    controller.kiemTraHopLeChiTiet(ct);
                } catch (IllegalArgumentException ex) {
                    hienThongBaoCanhBao("Lỗi kiểm tra quy tắc ma trận: " + ex.getMessage());
                    return;
                }

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

        // Tinh toan tien hang tra & tong so luong tra
        int tongSoLuongTra = 0;
        BigDecimal tongTra = BigDecimal.ZERO;
        for (DongTraHang d : dsSanPhamDoiTra) {
            if (d.isChon() && !d.isLaDichVu()) {
                tongSoLuongTra += d.getSoLuongTra();
                tongTra = tongTra.add(d.tinhThanhTien());
            }
        }

        BigDecimal tongDoi = BigDecimal.ZERO;
        if (rdoDoiHang.isSelected()) {
            for (DongDoiHang d : dsSanPhamDoiMoi) {
                tongDoi = tongDoi.add(d.getThanhTien());
            }
            // Gan thong tin san pham doi vao dong chi tiet dau tien hoac cac dong tuong ung
            if (!dsSanPhamDoiMoi.isEmpty() && !dsChiTiet.isEmpty()) {
                DongDoiHang doiDau = dsSanPhamDoiMoi.get(0);
                ChiTietPhieuDoiTra ctDau = dsChiTiet.get(0);
                ctDau.setMaSanPhamDoi(doiDau.getSanPham().getMaSanPham());
                ctDau.setTenSanPhamDoi(doiDau.getSanPham().getTenSanPham());
                ctDau.setSoLuongDoi(doiDau.getSoLuong());
                ctDau.setDonGiaDoi(doiDau.getSanPham().getGiaBan());
            }
        }

        // Tao doi tuong PhieuDoiTra
        PhieuDoiTra phieu = new PhieuDoiTra();
        phieu.setMaPhieuDoiTra(controller.sinhMaPhieu());
        phieu.setMaHoaDon(hoaDonDangChon.getMaHoaDon());
        phieu.setNgayLap(LocalDate.now());
        phieu.setMaKhachHang(hoaDonDangChon.getMaKhachHang());
        phieu.setTenKhachHang(hoaDonDangChon.getTenKhachHang());
        phieu.setSoDienThoaiKhachHang(hoaDonDangChon.getSoDienThoaiKhachHang());
        phieu.setSoLuong(tongSoLuongTra);

        // Nhan vien dang nhap hien tai
        TaiKhoan tkHienTai = SessionManager.getTaiKhoanHienTai();
        phieu.setMaNhanVien(tkHienTai != null ? tkHienTai.getMaNhanVien() : "NV-20261001-000001");

        if (rdoTraHang.isSelected()) {
            phieu.setLoaiXuLy("Hoàn tiền");
            phieu.setHinhThucHoan(cboHinhThucHoan.getValue());
            phieu.setTienHoan(tongTra);
            phieu.setTienBu(BigDecimal.ZERO);
        } else {
            phieu.setLoaiXuLy("Đổi hàng");
            phieu.setHinhThucHoan(null);
            BigDecimal chenhLech = tongDoi.subtract(tongTra);
            if (chenhLech.compareTo(BigDecimal.ZERO) > 0) {
                phieu.setTienBu(chenhLech);
                phieu.setTienHoan(BigDecimal.ZERO);
            } else {
                phieu.setTienHoan(chenhLech.abs());
                phieu.setTienBu(BigDecimal.ZERO);
            }
        }

        phieu.setTrangThai("Chờ xử lý");
        phieu.setDanhSachSanPham(dsChiTiet);

        // Hop thoai xac nhan cuoi cung
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
