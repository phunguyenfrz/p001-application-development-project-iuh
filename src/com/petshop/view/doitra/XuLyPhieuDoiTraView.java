package com.petshop.view.doitra;

import com.petshop.controller.PhieuDoiTraController;
import com.petshop.model.ChiTietHoaDonSanPham;
import com.petshop.model.ChiTietPhieuDoiTra;
import com.petshop.model.HoaDon;
import com.petshop.model.PhieuDoiTra;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Màn hình Xử lý đổi trả (RET_CONFIRM / DT_XU_LY).
 * Sử dụng bộ màu mặc định chuẩn chung.css đồng bộ với các phân hệ Dịch vụ, Hóa đơn.
 * Tích hợp chức năng xử lý sản phẩm khi hoàn: "Thu lại vào kho" hoặc "Hủy bỏ".
 */
public class XuLyPhieuDoiTraView extends VBox {

    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 VNĐ");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PhieuDoiTraController controller = new PhieuDoiTraController();

    // Dữ liệu nguồn
    private final ObservableList<PhieuDoiTra> dsTatCaPhieu = FXCollections.observableArrayList();
    private final ObservableList<PhieuDoiTra> dsHienThi = FXCollections.observableArrayList();
    private final ObservableList<ChiTietPhieuDoiTra> dsChiTietSua = FXCollections.observableArrayList();

    // Đối tượng phiếu đang xử lý/chỉnh sửa
    private PhieuDoiTra phieuDangChon = null;
    private HoaDon hoaDonGocCuaPhieu = null;

    // Hai màn hình chính chuyển đổi linh hoạt
    private final VBox pnlManHinhDanhSach = new VBox(12);
    private final VBox pnlManHinhChinhSua = new VBox(12);

    // ==========================================
    // 1. CÁC THÀNH PHẦN MÀN HÌNH DANH SÁCH
    // ==========================================
    private final TextField txtTimKiem = new TextField();
    private final ComboBox<String> cboLocTrangThai = new ComboBox<>();
    private final ComboBox<String> cboXuLySanPhamNhanh = new ComboBox<>();
    private final Button btnChonCapNhat = new Button("Cập nhật / Chi tiết phiếu");
    private final Button btnDuyetNhanh = new Button("Duyệt phiếu");
    private final Button btnTuChoiNhanh = new Button("Từ chối");
    private final Button btnLamMoiDanhSach = new Button("Làm mới");
    private final TableView<PhieuDoiTra> bangDanhSachPhieu = new TableView<>();
    private final Label lblThongKeChanBang = new Label("Tổng cộng: 0 phiếu đổi trả");

    // ==========================================
    // 2. CÁC THÀNH PHẦN MÀN HÌNH CHỈNH SỬA / CHI TIẾT
    // ==========================================
    private final Button btnQuayLaiDanhSach = new Button("Quay lại danh sách phiếu");
    private final Label lblTieuDeChinhSua = new Label("CHI TIẾT PHIẾU ĐỔI TRẢ");
    private final Label lblBadgeTrangThai = new Label();
    private final Label lblBannerTrangThai = new Label();

    // Khối tóm tắt thông tin phiếu & HĐ gốc
    private final Label lblMaPhieu = new Label("--");
    private final Label lblMaHoaDon = new Label("--");
    private final Label lblNgayLap = new Label("--");
    private final Label lblKhachHang = new Label("--");
    private final Label lblNhanVienLap = new Label("--");
    private final Label lblLoaiXuLyHienTai = new Label("--");

    // Khối thêm sản phẩm từ hóa đơn gốc (chỉ hiện khi Chờ xử lý)
    private final VBox boxThemSanPham = new VBox(8);
    private final ComboBox<ChiTietHoaDonSanPham> cboSanPhamGoc = new ComboBox<>();
    private final Spinner<Integer> spnSoLuongTraThem = new Spinner<>(1, 9999, 1);
    private final ComboBox<String> cboLyDoThem = new ComboBox<>();
    private final ComboBox<String> cboTinhTrangThem = new ComboBox<>();
    private final ComboBox<String> cboHuongXuLyThem = new ComboBox<>();
    private final TextField txtGhiChuThem = new TextField();
    private final Button btnThemSanPham = new Button("+ Thêm vào phiếu");

    // Khối điều chỉnh dòng mặt hàng đang chọn & xử lý sản phẩm khi hoàn
    private final HBox boxDieuChinhDong = new HBox(10);
    private final Label lblDongDangChon = new Label("Chưa chọn dòng mặt hàng nào");
    private final Spinner<Integer> spnSoLuongSua = new Spinner<>(1, 9999, 1);
    private final ComboBox<String> cboHuongXuLySua = new ComboBox<>();
    private final Button btnCapNhatSl = new Button("Cập nhật dòng");
    private final Button btnThuLaiKhoTatCa = new Button("Thu lại vào kho (Tất cả)");
    private final Button btnHuyBoSpTatCa = new Button("Hủy bỏ SP (Tất cả)");
    private final Button btnXoaDong = new Button("Xóa mặt hàng");

    // Bảng chi tiết mặt hàng đổi trả
    private final TableView<ChiTietPhieuDoiTra> bangChiTietHangTra = new TableView<>();

    // Tổng kết tài chính
    private final Label lblTongTienHangTra = new Label("0 VNĐ");
    private final Label lblTongTienHoan = new Label("0 VNĐ");
    private final Label lblTongTienBu = new Label("0 VNĐ");

    // Action Bar dưới cùng (chỉ Lưu cập nhật hoặc Hủy bỏ)
    private final HBox pnlActionBarSua = new HBox(10);
    private final Button btnLuuCapNhat = new Button("Lưu cập nhật phiếu");
    private final Button btnInLaiPhieu = new Button("In lại phiếu");
    private final Button btnHuyBoSua = new Button("Hủy bỏ");

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

    /**
     * Chuẩn hóa giá trị hướng xử lý sản phẩm khi hoàn về 2 lựa chọn chuẩn:
     * "Thu lại vào kho" hoặc "Hủy bỏ".
     */
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

    // =========================================================================
    // 1. DỰNG MÀN HÌNH 1: DANH SÁCH TẤT CẢ CÁC PHIẾU
    // =========================================================================
    private void khoiTaoManHinhDanhSach() {
        pnlManHinhDanhSach.setSpacing(14);
        VBox.setVgrow(pnlManHinhDanhSach, Priority.ALWAYS);

        // Header tiêu đề
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("PHIẾU ĐỔI TRẢ - XỬ LÝ ĐỔI TRẢ");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Danh sách phiếu đổi trả hàng hóa. Chọn phiếu để cập nhật chi tiết, xử lý sản phẩm hoàn (Thu lại vào kho / Hủy bỏ), duyệt hoặc từ chối.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // Khung bảng và thanh công cụ
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
        colKhach.setPrefWidth(170);

        TableColumn<PhieuDoiTra, String> colLoai = new TableColumn<>("Loại xử lý");
        colLoai.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLoaiXuLy() != null ? d.getValue().getLoaiXuLy() : "Hoàn tiền"));
        colLoai.setPrefWidth(100);

        TableColumn<PhieuDoiTra, String> colXuLySp = new TableColumn<>("Xử lý SP hoàn");
        colXuLySp.setCellValueFactory(d -> {
            PhieuDoiTra p = d.getValue();
            if (p.getDanhSachSanPham() != null && !p.getDanhSachSanPham().isEmpty()) {
                ChiTietPhieuDoiTra ct0 = p.getDanhSachSanPham().get(0);
                return new SimpleStringProperty(chuanHoaHuongXuLyHienThi(ct0.getHuongXuLy(), ct0.getTinhTrang()));
            }
            return new SimpleStringProperty("Thu lại vào kho");
        });
        colXuLySp.setPrefWidth(120);

        TableColumn<PhieuDoiTra, String> colTongTra = new TableColumn<>("Tổng hàng trả");
        colTongTra.setCellValueFactory(d -> {
            BigDecimal tong = d.getValue().tinhTongTienHangTra();
            if (tong.compareTo(BigDecimal.ZERO) == 0 && d.getValue().getTienHoan() != null && d.getValue().getTienHoan().compareTo(BigDecimal.ZERO) > 0) {
                tong = d.getValue().getTienHoan();
            }
            return new SimpleStringProperty(DINH_DANG_TIEN.format(tong));
        });
        colTongTra.setPrefWidth(110);
        colTongTra.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");

        TableColumn<PhieuDoiTra, String> colHoan = new TableColumn<>("Tiền hoàn");
        colHoan.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().getTienHoan() != null ? d.getValue().getTienHoan() : BigDecimal.ZERO)));
        colHoan.setPrefWidth(100);
        colHoan.setStyle("-fx-alignment: center-right;");

        TableColumn<PhieuDoiTra, String> colBu = new TableColumn<>("Tiền bù");
        colBu.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().getTienBu() != null ? d.getValue().getTienBu() : BigDecimal.ZERO)));
        colBu.setPrefWidth(90);
        colBu.setStyle("-fx-alignment: center-right;");

        TableColumn<PhieuDoiTra, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTrangThai()));
        colTrangThai.setPrefWidth(100);
        colTrangThai.setStyle("-fx-alignment: center; -fx-font-weight: bold;");

        bangDanhSachPhieu.getColumns().addAll(colStt, colMaP, colMaHd, colNgay, colKhach, colLoai, colXuLySp, colTongTra, colHoan, colBu, colTrangThai);
    }

    // =========================================================================
    // 2. DỰNG MÀN HÌNH 2: CHỈNH SỬA / CHI TIẾT PHIẾU
    // =========================================================================
    private void khoiTaoManHinhChinhSua() {
        pnlManHinhChinhSua.setSpacing(10);
        VBox.setVgrow(pnlManHinhChinhSua, Priority.ALWAYS);

        // Header trên cùng: Quay lại + Tiêu đề + Trạng thái
        HBox pnlHeaderSua = new HBox(12);
        pnlHeaderSua.setAlignment(Pos.CENTER_LEFT);

        btnQuayLaiDanhSach.getStyleClass().add("nut-phu");
        btnQuayLaiDanhSach.setOnAction(e -> chuyenManHinh(false));

        lblTieuDeChinhSua.getStyleClass().add("tieu-de-phan-he");
        lblBadgeTrangThai.setStyle("-fx-padding: 3 10; -fx-background-color: #e2e8f0; -fx-background-radius: 4px; -fx-font-weight: bold;");

        Region spacerH = new Region();
        HBox.setHgrow(spacerH, Priority.ALWAYS);

        pnlHeaderSua.getChildren().addAll(btnQuayLaiDanhSach, lblTieuDeChinhSua, lblBadgeTrangThai, spacerH);

        // Dòng mô tả trạng thái
        lblBannerTrangThai.getStyleClass().add("mo-ta-phan-he");
        lblBannerTrangThai.setMaxWidth(Double.MAX_VALUE);

        // Card thông tin tóm tắt
        VBox cardThongTin = taoCardThongTinTomTat();

        // Khung thêm sản phẩm từ HĐ gốc (chỉ hiện khi Chờ xử lý)
        taoKhungThemSanPham();

        // Khung điều chỉnh dòng đang chọn & xử lý sản phẩm khi hoàn
        taoKhungDieuChinhDong();

        // Bảng chi tiết mặt hàng
        taoBangChiTietHangTra();
        VBox.setVgrow(bangChiTietHangTra, Priority.ALWAYS);

        // Khung tài chính & Action bar
        HBox pnlTongKetVaAction = taoKhungTongKetVaAction();

        pnlManHinhChinhSua.getChildren().addAll(pnlHeaderSua, lblBannerTrangThai, cardThongTin,
                boxThemSanPham, boxDieuChinhDong, bangChiTietHangTra, pnlTongKetVaAction);
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

    private void taoKhungThemSanPham() {
        boxThemSanPham.getStyleClass().add("hop-noi-dung");

        Label lblTieuDeBox = new Label("+ THÊM SẢN PHẨM ĐỔI TRẢ TỪ HÓA ĐƠN GỐC:");
        lblTieuDeBox.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        HBox hang1 = new HBox(10);
        hang1.setAlignment(Pos.CENTER_LEFT);

        cboSanPhamGoc.setPromptText("-- Chọn sản phẩm đã mua --");
        cboSanPhamGoc.setPrefWidth(250);

        cboLyDoThem.getItems().addAll("Lỗi sản phẩm", "Hết hạn", "Không phù hợp", "Khác");
        cboLyDoThem.setValue("Lỗi sản phẩm");
        cboLyDoThem.setPrefWidth(125);

        cboTinhTrangThem.getItems().addAll("Nguyên vẹn", "Đã mở", "Hư hỏng");
        cboTinhTrangThem.setValue("Nguyên vẹn");
        cboTinhTrangThem.setPrefWidth(110);

        cboHuongXuLyThem.getItems().addAll("Thu lại vào kho", "Hủy bỏ");
        cboHuongXuLyThem.setValue("Thu lại vào kho");
        cboHuongXuLyThem.setPrefWidth(140);

        spnSoLuongTraThem.setPrefWidth(75);

        btnThemSanPham.getStyleClass().add("nut-chinh");
        btnThemSanPham.setOnAction(e -> xuLyThemSanPhamVaoPhieu());

        hang1.getChildren().addAll(
                new Label("Sản phẩm:"), cboSanPhamGoc,
                new Label("Lý do:"), cboLyDoThem,
                new Label("Tình trạng:"), cboTinhTrangThem,
                new Label("Xử lý SP hoàn:"), cboHuongXuLyThem,
                new Label("SL:"), spnSoLuongTraThem,
                btnThemSanPham
        );

        boxThemSanPham.getChildren().addAll(lblTieuDeBox, hang1);
    }

    private void taoKhungDieuChinhDong() {
        boxDieuChinhDong.getStyleClass().add("hop-noi-dung");
        boxDieuChinhDong.setAlignment(Pos.CENTER_LEFT);

        lblDongDangChon.setStyle("-fx-font-weight: bold;");
        spnSoLuongSua.setPrefWidth(70);
        spnSoLuongSua.setDisable(true);

        cboHuongXuLySua.getItems().addAll("Thu lại vào kho", "Hủy bỏ");
        cboHuongXuLySua.setValue("Thu lại vào kho");
        cboHuongXuLySua.setPrefWidth(140);
        cboHuongXuLySua.setDisable(true);

        btnCapNhatSl.getStyleClass().add("nut-chinh");
        btnCapNhatSl.setDisable(true);
        btnCapNhatSl.setOnAction(e -> xuLyCapNhatDong());

        btnThuLaiKhoTatCa.getStyleClass().add("nut-phu");
        btnThuLaiKhoTatCa.setOnAction(e -> apDungHuongXuLyTatCa("Thu lại vào kho"));

        btnHuyBoSpTatCa.getStyleClass().add("nut-phu");
        btnHuyBoSpTatCa.setOnAction(e -> apDungHuongXuLyTatCa("Hủy bỏ"));

        btnXoaDong.getStyleClass().add("nut-xoa");
        btnXoaDong.setDisable(true);
        btnXoaDong.setOnAction(e -> xuLyXoaDongSanPham());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        boxDieuChinhDong.getChildren().addAll(
                new Label("Dòng chọn:"), lblDongDangChon,
                new Label("SL:"), spnSoLuongSua,
                new Label("Xử lý SP hoàn:"), cboHuongXuLySua,
                btnCapNhatSl,
                spacer,
                btnThuLaiKhoTatCa, btnHuyBoSpTatCa, btnXoaDong
        );
    }

    private void taoBangChiTietHangTra() {
        bangChiTietHangTra.setItems(dsChiTietSua);
        bangChiTietHangTra.getStyleClass().add("bang-du-lieu");
        bangChiTietHangTra.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        bangChiTietHangTra.setPlaceholder(new Label("Chưa có mặt hàng đổi trả nào trong phiếu."));

        TableColumn<ChiTietPhieuDoiTra, Integer> colStt = new TableColumn<>("STT");
        colStt.setMaxWidth(45);
        colStt.setStyle("-fx-alignment: center;");
        colStt.setCellValueFactory(c -> new SimpleObjectProperty<>(dsChiTietSua.indexOf(c.getValue()) + 1));

        TableColumn<ChiTietPhieuDoiTra, String> colMaSp = new TableColumn<>("Mã SP");
        colMaSp.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaSanPham()));
        colMaSp.setPrefWidth(120);

        TableColumn<ChiTietPhieuDoiTra, String> colTenSp = new TableColumn<>("Tên sản phẩm");
        colTenSp.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenSanPham()));
        colTenSp.setPrefWidth(220);

        TableColumn<ChiTietPhieuDoiTra, Integer> colSl = new TableColumn<>("Số lượng");
        colSl.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSoLuong()));
        colSl.setPrefWidth(70);
        colSl.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");

        TableColumn<ChiTietPhieuDoiTra, String> colGia = new TableColumn<>("Đơn giá");
        colGia.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().getDonGia())));
        colGia.setPrefWidth(95);
        colGia.setStyle("-fx-alignment: center-right;");

        TableColumn<ChiTietPhieuDoiTra, String> colThanhTien = new TableColumn<>("Thành tiền hoàn");
        colThanhTien.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().tinhThanhTienTra())));
        colThanhTien.setPrefWidth(110);
        colThanhTien.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");

        TableColumn<ChiTietPhieuDoiTra, String> colLyDo = new TableColumn<>("Lý do");
        colLyDo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLyDo()));
        colLyDo.setPrefWidth(100);

        TableColumn<ChiTietPhieuDoiTra, String> colTinhTrang = new TableColumn<>("Tình trạng");
        colTinhTrang.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTinhTrang()));
        colTinhTrang.setPrefWidth(90);

        TableColumn<ChiTietPhieuDoiTra, String> colHuongXuLy = new TableColumn<>("Xử lý SP khi hoàn");
        colHuongXuLy.setCellValueFactory(d -> new SimpleStringProperty(
                chuanHoaHuongXuLyHienThi(d.getValue().getHuongXuLy(), d.getValue().getTinhTrang())
        ));
        colHuongXuLy.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> cbo = new ComboBox<>(FXCollections.observableArrayList(
                    "Thu lại vào kho", "Hủy bỏ"
            ));
            {
                cbo.setPrefWidth(135);
                cbo.setOnAction(e -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        ChiTietPhieuDoiTra ct = getTableRow().getItem();
                        ct.setHuongXuLy(cbo.getValue());
                    }
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    ChiTietPhieuDoiTra ct = getTableRow().getItem();
                    String giaTriChuan = chuanHoaHuongXuLyHienThi(ct.getHuongXuLy(), ct.getTinhTrang());
                    ct.setHuongXuLy(giaTriChuan);
                    cbo.setValue(giaTriChuan);
                    boolean choPhepSua = phieuDangChon != null && "Chờ xử lý".equalsIgnoreCase(phieuDangChon.getTrangThai());
                    cbo.setDisable(!choPhepSua);
                    setGraphic(cbo);
                }
            }
        });
        colHuongXuLy.setPrefWidth(150);

        bangChiTietHangTra.getColumns().addAll(colStt, colMaSp, colTenSp, colSl, colGia, colThanhTien, colLyDo, colTinhTrang, colHuongXuLy);
    }

    private HBox taoKhungTongKetVaAction() {
        HBox box = new HBox(14);
        box.getStyleClass().add("hop-noi-dung");
        box.setAlignment(Pos.CENTER_LEFT);

        HBox boxTien = new HBox(16);
        boxTien.setAlignment(Pos.CENTER_LEFT);

        lblTongTienHangTra.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        lblTongTienHoan.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        lblTongTienBu.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        boxTien.getChildren().addAll(
                new Label("Tổng hàng trả:"), lblTongTienHangTra,
                new Label("Tiền hoàn lại:"), lblTongTienHoan,
                new Label("Khách bù thêm:"), lblTongTienBu
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnLuuCapNhat.getStyleClass().add("nut-chinh");
        btnLuuCapNhat.setOnAction(e -> xuLyLuuCapNhatPhieu());

        btnInLaiPhieu.getStyleClass().add("nut-chinh");
        btnInLaiPhieu.setOnAction(e -> moHopThoaiXemVaIn(phieuDangChon));

        btnHuyBoSua.getStyleClass().add("nut-phu");
        btnHuyBoSua.setOnAction(e -> chuyenManHinh(false));

        pnlActionBarSua.setAlignment(Pos.CENTER_RIGHT);
        pnlActionBarSua.getChildren().addAll(btnLuuCapNhat, btnInLaiPhieu, btnHuyBoSua);

        box.getChildren().addAll(boxTien, spacer, pnlActionBarSua);
        return box;
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

        bangChiTietHangTra.getSelectionModel().selectedItemProperty().addListener((obs, cu, moi) -> {
            boolean coChon = moi != null;
            boolean choPhepSua = phieuDangChon != null && "Chờ xử lý".equalsIgnoreCase(phieuDangChon.getTrangThai());

            spnSoLuongSua.setDisable(!coChon || !choPhepSua);
            cboHuongXuLySua.setDisable(!coChon || !choPhepSua);
            btnCapNhatSl.setDisable(!coChon || !choPhepSua);
            btnXoaDong.setDisable(!coChon || !choPhepSua);

            if (coChon) {
                lblDongDangChon.setText(moi.getTenSanPham() + " (SL: " + moi.getSoLuong() + ")");
                spnSoLuongSua.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, moi.getSoLuong()));
                cboHuongXuLySua.setValue(chuanHoaHuongXuLyHienThi(moi.getHuongXuLy(), moi.getTinhTrang()));
            } else {
                lblDongDangChon.setText("Chưa chọn dòng mặt hàng nào");
            }
        });
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
        lblLoaiXuLyHienTai.setText(phieuDangChon.getLoaiXuLy() != null ? phieuDangChon.getLoaiXuLy() : "Hoàn tiền");

        String tt = phieuDangChon.getTrangThai() != null ? phieuDangChon.getTrangThai() : "Chờ xử lý";
        lblBadgeTrangThai.setText(tt);

        boolean choPhepSua = "Chờ xử lý".equalsIgnoreCase(tt);

        if (choPhepSua) {
            lblBannerTrangThai.setText("Phiếu đang ở trạng thái [Chờ xử lý] - Cho phép điều chỉnh thông tin, số lượng, lý do và xử lý sản phẩm khi hoàn (Thu lại vào kho / Hủy bỏ).");

            boxThemSanPham.setVisible(true);
            boxThemSanPham.setManaged(true);
            boxDieuChinhDong.setVisible(true);
            boxDieuChinhDong.setManaged(true);

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

            boxThemSanPham.setVisible(false);
            boxThemSanPham.setManaged(false);
            boxDieuChinhDong.setVisible(false);
            boxDieuChinhDong.setManaged(false);

            btnLuuCapNhat.setVisible(false);
            btnLuuCapNhat.setManaged(false);
            btnHuyBoSua.setText("Quay lại danh sách");

            btnInLaiPhieu.setVisible(true);
            btnInLaiPhieu.setManaged(true);
        }

        dsChiTietSua.clear();
        if (phieuDangChon.getDanhSachSanPham() != null) {
            for (ChiTietPhieuDoiTra ct : phieuDangChon.getDanhSachSanPham()) {
                String huongChuan = chuanHoaHuongXuLyHienThi(ct.getHuongXuLy(), ct.getTinhTrang());
                ChiTietPhieuDoiTra copy = new ChiTietPhieuDoiTra(
                        ct.getMaPhieuDoiTra(), ct.getMaHoaDon(), ct.getMaSanPham(), ct.getTenSanPham(),
                        ct.getSoLuong(), ct.getDonGia(), ct.getLyDo(), ct.getTinhTrang(),
                        huongChuan, ct.getGhiChu()
                );
                copy.setMaSanPhamDoi(ct.getMaSanPhamDoi());
                copy.setTenSanPhamDoi(ct.getTenSanPhamDoi());
                copy.setSoLuongDoi(ct.getSoLuongDoi());
                copy.setDonGiaDoi(ct.getDonGiaDoi());
                dsChiTietSua.add(copy);
            }
        }

        bangChiTietHangTra.refresh();
        tinhToanTongTienChiTiet();
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

    private void tinhToanTongTienChiTiet() {
        BigDecimal tongHangTra = BigDecimal.ZERO;
        for (ChiTietPhieuDoiTra ct : dsChiTietSua) {
            tongHangTra = tongHangTra.add(ct.tinhThanhTienTra());
        }

        lblTongTienHangTra.setText(DINH_DANG_TIEN.format(tongHangTra));

        if ("Đổi sang sản phẩm khác".equalsIgnoreCase(phieuDangChon.getLoaiXuLy())
                || "Đổi hàng".equalsIgnoreCase(phieuDangChon.getLoaiXuLy())) {
            BigDecimal tongHangDoi = BigDecimal.ZERO;
            for (ChiTietPhieuDoiTra ct : dsChiTietSua) {
                tongHangDoi = tongHangDoi.add(ct.tinhThanhTienDoi());
            }
            BigDecimal chenhLech = tongHangDoi.subtract(tongHangTra);
            if (chenhLech.compareTo(BigDecimal.ZERO) > 0) {
                lblTongTienBu.setText(DINH_DANG_TIEN.format(chenhLech));
                lblTongTienHoan.setText("0 VNĐ");
            } else {
                lblTongTienHoan.setText(DINH_DANG_TIEN.format(chenhLech.abs()));
                lblTongTienBu.setText("0 VNĐ");
            }
        } else {
            lblTongTienHoan.setText(DINH_DANG_TIEN.format(tongHangTra));
            lblTongTienBu.setText("0 VNĐ");
        }
    }

    private void xuLyThemSanPhamVaoPhieu() {
        ChiTietHoaDonSanPham spGoc = cboSanPhamGoc.getValue();
        if (spGoc == null) {
            hienCanhBao("Vui lòng chọn một sản phẩm từ hóa đơn gốc!");
            return;
        }

        int slThem = spnSoLuongTraThem.getValue();
        String lyDo = cboLyDoThem.getValue();
        String tinhTrang = cboTinhTrangThem.getValue();
        String huongXuLy = cboHuongXuLyThem.getValue();

        try {
            controller.kiemTraHopLeChiTiet(new ChiTietPhieuDoiTra(
                    phieuDangChon.getMaPhieuDoiTra(), phieuDangChon.getMaHoaDon(),
                    spGoc.getMaSanPham(), spGoc.getTenSanPham(), slThem, spGoc.getDonGia(),
                    lyDo, tinhTrang, huongXuLy, txtGhiChuThem.getText()
            ));
        } catch (IllegalArgumentException ex) {
            hienLoi(ex.getMessage());
            return;
        }

        for (ChiTietPhieuDoiTra ct : dsChiTietSua) {
            if (ct.getMaSanPham().equalsIgnoreCase(spGoc.getMaSanPham())) {
                ct.setSoLuong(ct.getSoLuong() + slThem);
                ct.setLyDo(lyDo);
                ct.setTinhTrang(tinhTrang);
                ct.setHuongXuLy(huongXuLy);
                bangChiTietHangTra.refresh();
                tinhToanTongTienChiTiet();
                return;
            }
        }

        ChiTietPhieuDoiTra dongMoi = new ChiTietPhieuDoiTra(
                phieuDangChon.getMaPhieuDoiTra(), phieuDangChon.getMaHoaDon(),
                spGoc.getMaSanPham(), spGoc.getTenSanPham(), slThem, spGoc.getDonGia(),
                lyDo, tinhTrang, huongXuLy, txtGhiChuThem.getText()
        );
        dsChiTietSua.add(dongMoi);
        tinhToanTongTienChiTiet();
    }

    private void xuLyCapNhatDong() {
        ChiTietPhieuDoiTra ct = bangChiTietHangTra.getSelectionModel().getSelectedItem();
        if (ct == null) return;

        int slMoi = spnSoLuongSua.getValue();
        if (slMoi <= 0) {
            hienCanhBao("Số lượng đổi trả phải lớn hơn 0!");
            return;
        }

        ct.setSoLuong(slMoi);
        if (cboHuongXuLySua.getValue() != null) {
            ct.setHuongXuLy(cboHuongXuLySua.getValue());
        }
        bangChiTietHangTra.refresh();
        tinhToanTongTienChiTiet();
    }

    private void apDungHuongXuLyTatCa(String huongXuLy) {
        if (phieuDangChon == null || !"Chờ xử lý".equalsIgnoreCase(phieuDangChon.getTrangThai())) {
            return;
        }
        for (ChiTietPhieuDoiTra ct : dsChiTietSua) {
            ct.setHuongXuLy(huongXuLy);
        }
        cboHuongXuLySua.setValue(huongXuLy);
        bangChiTietHangTra.refresh();
    }

    private void xuLyXoaDongSanPham() {
        ChiTietPhieuDoiTra ct = bangChiTietHangTra.getSelectionModel().getSelectedItem();
        if (ct == null) return;

        if (dsChiTietSua.size() <= 1) {
            hienCanhBao("Phiếu đổi trả phải có ít nhất một mặt hàng! Không thể xóa hết.");
            return;
        }

        dsChiTietSua.remove(ct);
        tinhToanTongTienChiTiet();
    }

    private void xuLyLuuCapNhatPhieu() {
        if (phieuDangChon == null) return;
        if (!"Chờ xử lý".equalsIgnoreCase(phieuDangChon.getTrangThai())) {
            hienCanhBao("Phiếu đã duyệt/xác nhận, không thể chỉnh sửa!");
            return;
        }

        if (dsChiTietSua.isEmpty()) {
            hienCanhBao("Phiếu đổi trả phải có ít nhất một mặt hàng!");
            return;
        }

        try {
            phieuDangChon.getDanhSachSanPham().clear();
            phieuDangChon.getDanhSachSanPham().addAll(dsChiTietSua);

            boolean kq = controller.capNhatPhieuDoiTra(phieuDangChon);
            if (kq) {
                hienThanhCong("Đã lưu cập nhật phiếu đổi trả thành công!");
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

        if (!"Chờ xử lý".equalsIgnoreCase(phieu.getTrangThai())) {
            hienCanhBao("Phiếu đổi trả đã ở trạng thái [" + phieu.getTrangThai() + "], không thể duyệt lại!");
            return;
        }

        String xuLySpChon = cboXuLySanPhamNhanh.getValue() != null ? cboXuLySanPhamNhanh.getValue() : "Thu lại vào kho";

        Alert xacNhan = new Alert(Alert.AlertType.CONFIRMATION);
        xacNhan.setTitle("Xác nhận duyệt phiếu & Xử lý sản phẩm hoàn");
        xacNhan.setHeaderText("Duyệt phiếu đổi trả: " + phieu.getMaPhieuDoiTra());
        xacNhan.setContentText("Chọn hướng xử lý đối với sản phẩm khách hoàn trả:\n"
                + "• Thu lại vào kho: Cộng lại số lượng sản phẩm hoàn vào tồn kho.\n"
                + "• Hủy bỏ: Hủy sản phẩm hoàn, không cộng lại vào tồn kho.\n\n"
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
                hienThanhCong("Đã duyệt phiếu đổi trả thành công!\nHướng xử lý sản phẩm hoàn: " + huongXuLyQuyetDinh);
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
