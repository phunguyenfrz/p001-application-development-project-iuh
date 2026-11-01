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
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình Xử lý đổi trả (RET_CONFIRM / DT_XU_LY).
 * Thiết kế giao diện 2 màn hình chuyển đổi linh hoạt tương tự Màn hình Cập nhật hóa đơn:
 * 1. Màn hình bình thường: Bảng danh sách tất cả các phiếu đổi trả + bộ lọc tìm kiếm + nút hành động.
 * 2. Màn hình cập nhật/chi tiết:
 *    - Phiếu CHƯA XÁC NHẬN ("Chờ xử lý"): Cho phép chỉnh sửa thông tin, thêm/xóa sản phẩm, điều chỉnh số lượng, lý do, hướng xử lý QL26, lưu cập nhật hoặc duyệt/từ chối.
 *    - Phiếu ĐÃ XÁC NHẬN ("Đã duyệt" / "Từ chối"): Khóa toàn bộ ở chế độ chỉ đọc (read-only), không thể chỉnh sửa.
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

    // HAI MÀN HÌNH CHÍNH (Chuyển đổi linh hoạt)
    private final VBox pnlManHinhDanhSach = new VBox(12);
    private final VBox pnlManHinhChinhSua = new VBox(12);

    // ==========================================
    // 1. CÁC THÀNH PHẦN MÀN HÌNH DANH SÁCH
    // ==========================================
    private final TextField txtTimKiem = new TextField();
    private final ComboBox<String> cboLocTrangThai = new ComboBox<>();
    private final Button btnChonCapNhat = new Button("✏ Cập nhật / Chi tiết phiếu");
    private final Button btnDuyetNhanh = new Button("✔ Duyệt phiếu");
    private final Button btnTuChoiNhanh = new Button("✖ Từ chối");
    private final Button btnLamMoiDanhSach = new Button("🔄 Làm mới");
    private final TableView<PhieuDoiTra> bangDanhSachPhieu = new TableView<>();
    private final Label lblThongKeChanBang = new Label("Tổng cộng: 0 phiếu đổi trả");

    // ==========================================
    // 2. CÁC THÀNH PHẦN MÀN HÌNH CHỈNH SỬA / CHI TIẾT
    // ==========================================
    private final Button btnQuayLaiDanhSach = new Button("← Quay lại danh sách phiếu");
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
    private final Label lblDonGiaGoc = new Label("0 VNĐ");
    private final Label lblSoLuongMua = new Label("Đã mua: 0 (Đã trả: 0)");
    private final Spinner<Integer> spnSoLuongTraThem = new Spinner<>(1, 9999, 1);
    private final ComboBox<String> cboLyDoThem = new ComboBox<>();
    private final ComboBox<String> cboTinhTrangThem = new ComboBox<>();
    private final ComboBox<String> cboHuongXuLyThem = new ComboBox<>();
    private final TextField txtGhiChuThem = new TextField();
    private final Button btnThemSanPham = new Button("+ Thêm vào phiếu");

    // Khối điều chỉnh dòng mặt hàng đang chọn
    private final HBox boxDieuChinhDong = new HBox(12);
    private final Label lblDongDangChon = new Label("Chưa chọn dòng mặt hàng nào");
    private final Spinner<Integer> spnSoLuongSua = new Spinner<>(1, 9999, 1);
    private final Button btnCapNhatSl = new Button("✔ Cập nhật SL");
    private final Button btnXoaDong = new Button("✕ Xóa mặt hàng");

    // Bảng chi tiết mặt hàng đổi trả
    private final TableView<ChiTietPhieuDoiTra> bangChiTietHangTra = new TableView<>();

    // Tổng kết tài chính
    private final Label lblTongTienHangTra = new Label("0 VNĐ");
    private final Label lblTongTienHoan = new Label("0 VNĐ");
    private final Label lblTongTienBu = new Label("0 VNĐ");

    // Action Bar dưới cùng
    private final HBox pnlActionBarSua = new HBox(12);
    private final Button btnLuuCapNhat = new Button("✔ Lưu cập nhật phiếu");
    private final Button btnInLaiPhieu = new Button("🖨 In lại phiếu");
    private final Button btnHuyBoSua = new Button("Hủy bỏ");

    public XuLyPhieuDoiTraView() {
        setSpacing(12);
        setPadding(new Insets(16));
        setStyle("-fx-background-color: #f8fafc;");

        khoiTaoGiaoDien();
        dangKySuKien();
        taiDanhSachPhieu();
        chuyenManHinh(false);
    }

    private void khoiTaoGiaoDien() {
        // Khởi tạo Màn hình 1: Danh sách
        khoiTaoManHinhDanhSach();

        // Khởi tạo Màn hình 2: Chỉnh sửa / Chi tiết
        khoiTaoManHinhChinhSua();

        getChildren().add(pnlManHinhDanhSach);
    }

    // =========================================================================
    // 1. DỰNG MÀN HÌNH 1: DANH SÁCH TẤT CẢ CÁC PHIẾU
    // =========================================================================
    private void khoiTaoManHinhDanhSach() {
        pnlManHinhDanhSach.setSpacing(12);
        VBox.setVgrow(pnlManHinhDanhSach, Priority.ALWAYS);

        // Header tiêu đề
        VBox pnlTieuDe = new VBox(3);
        Label lblTieuDe = new Label("QUẢN LÝ & XỬ LÝ PHIẾU ĐỔI TRẢ");
        lblTieuDe.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        Label lblMoTa = new Label("Danh sách các yêu cầu đổi trả hàng hóa. Chọn phiếu để điều chỉnh nội dung hoặc xét duyệt.");
        lblMoTa.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // Thanh công cụ lọc & nút thao tác
        HBox pnlThanhCongCu = new HBox(10);
        pnlThanhCongCu.setPadding(new Insets(10, 14, 10, 14));
        pnlThanhCongCu.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px;");
        pnlThanhCongCu.setAlignment(Pos.CENTER_LEFT);

        txtTimKiem.setPromptText("Tìm theo mã phiếu, mã HĐ, tên KH, SĐT...");
        txtTimKiem.setPrefWidth(260);

        cboLocTrangThai.getItems().addAll("Tất cả trạng thái", "Chờ xử lý", "Đã duyệt", "Từ chối");
        cboLocTrangThai.setValue("Tất cả trạng thái");
        cboLocTrangThai.setPrefWidth(140);
        cboLocTrangThai.setOnAction(e -> locDanhSach());

        btnChonCapNhat.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 16; -fx-cursor: hand;");
        btnChonCapNhat.setOnAction(e -> moManHinhChinhSua());

        btnDuyetNhanh.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 14; -fx-cursor: hand;");
        btnDuyetNhanh.setOnAction(e -> xuLyDuyetPhieu(layPhieuDangChon()));

        btnTuChoiNhanh.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 14; -fx-cursor: hand;");
        btnTuChoiNhanh.setOnAction(e -> xuLyTuChoiPhieu(layPhieuDangChon()));

        btnLamMoiDanhSach.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-font-weight: bold; -fx-padding: 6 14; -fx-cursor: hand;");
        btnLamMoiDanhSach.setOnAction(e -> taiDanhSachPhieu());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        pnlThanhCongCu.getChildren().addAll(txtTimKiem, cboLocTrangThai, btnChonCapNhat, btnDuyetNhanh, btnTuChoiNhanh, spacer, btnLamMoiDanhSach);

        // Bảng danh sách phiếu
        taoBangDanhSachPhieu();
        VBox.setVgrow(bangDanhSachPhieu, Priority.ALWAYS);

        // Chân trang
        HBox pnlChanTrang = new HBox(12);
        pnlChanTrang.setPadding(new Insets(6, 12, 6, 12));
        pnlChanTrang.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px;");
        lblThongKeChanBang.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
        pnlChanTrang.getChildren().add(lblThongKeChanBang);

        pnlManHinhDanhSach.getChildren().addAll(pnlTieuDe, pnlThanhCongCu, bangDanhSachPhieu, pnlChanTrang);
    }

    private void taoBangDanhSachPhieu() {
        bangDanhSachPhieu.setItems(dsHienThi);
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
        colKhach.setPrefWidth(180);

        TableColumn<PhieuDoiTra, String> colLoai = new TableColumn<>("Loại xử lý");
        colLoai.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLoaiXuLy() != null ? d.getValue().getLoaiXuLy() : "Hoàn tiền"));
        colLoai.setPrefWidth(110);

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
        colHoan.setStyle("-fx-alignment: center-right; -fx-text-fill: #b91c1c;");

        TableColumn<PhieuDoiTra, String> colBu = new TableColumn<>("Tiền bù");
        colBu.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().getTienBu() != null ? d.getValue().getTienBu() : BigDecimal.ZERO)));
        colBu.setPrefWidth(90);
        colBu.setStyle("-fx-alignment: center-right;");

        TableColumn<PhieuDoiTra, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTrangThai()));
        colTrangThai.setPrefWidth(105);
        colTrangThai.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Chờ xử lý".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold;");
                    } else if ("Đã duyệt".equalsIgnoreCase(item) || "Hoàn thành".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                    } else if ("Từ chối".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        bangDanhSachPhieu.getColumns().addAll(colStt, colMaP, colMaHd, colNgay, colKhach, colLoai, colTongTra, colHoan, colBu, colTrangThai);
    }

    // =========================================================================
    // 2. DỰNG MÀN HÌNH 2: CHỈNH SỬA / CHI TIẾT PHIẾU
    // =========================================================================
    private void khoiTaoManHinhChinhSua() {
        pnlManHinhChinhSua.setSpacing(10);
        VBox.setVgrow(pnlManHinhChinhSua, Priority.ALWAYS);

        // Header trên cùng: Quay lại + Tiêu đề + Badge
        HBox pnlHeaderSua = new HBox(12);
        pnlHeaderSua.setAlignment(Pos.CENTER_LEFT);

        btnQuayLaiDanhSach.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-font-weight: bold; -fx-padding: 6 14; -fx-cursor: hand;");
        btnQuayLaiDanhSach.setOnAction(e -> chuyenManHinh(false));

        lblTieuDeChinhSua.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        lblBadgeTrangThai.setStyle("-fx-padding: 4 12; -fx-background-radius: 12px; -fx-font-weight: bold;");

        Region spacerH = new Region();
        HBox.setHgrow(spacerH, Priority.ALWAYS);

        pnlHeaderSua.getChildren().addAll(btnQuayLaiDanhSach, lblTieuDeChinhSua, lblBadgeTrangThai, spacerH);

        // Banner thông báo trạng thái
        lblBannerTrangThai.setStyle("-fx-padding: 8 14; -fx-background-radius: 6px; -fx-font-weight: bold;");
        lblBannerTrangThai.setMaxWidth(Double.MAX_VALUE);

        // Card thông tin tóm tắt
        VBox cardThongTin = taoCardThongTinTomTat();

        // Khung thêm sản phẩm từ HĐ gốc (chỉ hiện khi Chờ xử lý)
        taoKhungThemSanPham();

        // Khung điều chỉnh dòng đang chọn
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
        card.setPadding(new Insets(10, 14, 10, 14));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px;");

        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(6);

        grid.add(new Label("Mã phiếu:"), 0, 0);
        lblMaPhieu.setStyle("-fx-font-weight: bold; -fx-text-fill: #0284c7;");
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
        lblLoaiXuLyHienTai.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
        grid.add(lblLoaiXuLyHienTai, 5, 1);

        card.getChildren().add(grid);
        return card;
    }

    private void taoKhungThemSanPham() {
        boxThemSanPham.setPadding(new Insets(10, 14, 10, 14));
        boxThemSanPham.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px;");

        Label lblTieuDeBox = new Label("+ THÊM SẢN PHẨM ĐỔI TRẢ TỪ HÓA ĐƠN GỐC:");
        lblTieuDeBox.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #0284c7;");

        HBox hang1 = new HBox(10);
        hang1.setAlignment(Pos.CENTER_LEFT);

        cboSanPhamGoc.setPromptText("-- Chọn sản phẩm đã mua --");
        cboSanPhamGoc.setPrefWidth(260);

        cboLyDoThem.getItems().addAll("Lỗi sản phẩm", "Hết hạn", "Không phù hợp", "Khác");
        cboLyDoThem.setValue("Lỗi sản phẩm");
        cboLyDoThem.setPrefWidth(125);

        cboTinhTrangThem.getItems().addAll("Nguyên vẹn", "Đã mở", "Hư hỏng");
        cboTinhTrangThem.setValue("Nguyên vẹn");
        cboTinhTrangThem.setPrefWidth(110);

        cboHuongXuLyThem.getItems().addAll("Nhập lại kho bán", "Trả nhà cung cấp", "Tiêu hủy");
        cboHuongXuLyThem.setValue("Trả nhà cung cấp");
        cboHuongXuLyThem.setPrefWidth(135);

        spnSoLuongTraThem.setPrefWidth(75);

        btnThemSanPham.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 14; -fx-cursor: hand;");
        btnThemSanPham.setOnAction(e -> xuLyThemSanPhamVaoPhieu());

        hang1.getChildren().addAll(new Label("Sản phẩm:"), cboSanPhamGoc,
                new Label("Lý do:"), cboLyDoThem,
                new Label("Tình trạng:"), cboTinhTrangThem,
                new Label("Hướng xử lý:"), cboHuongXuLyThem,
                new Label("SL:"), spnSoLuongTraThem,
                btnThemSanPham);

        boxThemSanPham.getChildren().addAll(lblTieuDeBox, hang1);
    }

    private void taoKhungDieuChinhDong() {
        boxDieuChinhDong.setPadding(new Insets(8, 14, 8, 14));
        boxDieuChinhDong.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 6px;");
        boxDieuChinhDong.setAlignment(Pos.CENTER_LEFT);

        lblDongDangChon.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        spnSoLuongSua.setPrefWidth(75);
        spnSoLuongSua.setDisable(true);

        btnCapNhatSl.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #334155; -fx-font-weight: bold; -fx-padding: 5 12; -fx-cursor: hand;");
        btnCapNhatSl.setDisable(true);
        btnCapNhatSl.setOnAction(e -> xuLyCapNhatSoLuongDong());

        btnXoaDong.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-padding: 5 12; -fx-cursor: hand;");
        btnXoaDong.setDisable(true);
        btnXoaDong.setOnAction(e -> xuLyXoaDongSanPham());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        boxDieuChinhDong.getChildren().addAll(new Label("Điều chỉnh dòng đang chọn:"), lblDongDangChon,
                new Label("SL mới:"), spnSoLuongSua, btnCapNhatSl, spacer, btnXoaDong);
    }

    private void taoBangChiTietHangTra() {
        bangChiTietHangTra.setItems(dsChiTietSua);
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
        colThanhTien.setStyle("-fx-alignment: center-right; -fx-font-weight: bold; -fx-text-fill: #b91c1c;");

        TableColumn<ChiTietPhieuDoiTra, String> colLyDo = new TableColumn<>("Lý do");
        colLyDo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLyDo()));
        colLyDo.setPrefWidth(100);

        TableColumn<ChiTietPhieuDoiTra, String> colTinhTrang = new TableColumn<>("Tình trạng");
        colTinhTrang.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTinhTrang()));
        colTinhTrang.setPrefWidth(90);

        TableColumn<ChiTietPhieuDoiTra, String> colHuongXuLy = new TableColumn<>("Hướng xử lý (QL26)");
        colHuongXuLy.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHuongXuLy()));
        colHuongXuLy.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> cbo = new ComboBox<>(FXCollections.observableArrayList(
                    "Nhập lại kho bán", "Trả nhà cung cấp", "Tiêu hủy"
            ));
            {
                cbo.setStyle("-fx-font-size: 11px;");
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
                    cbo.setValue(ct.getHuongXuLy());
                    boolean choPhepSua = phieuDangChon != null && "Chờ xử lý".equalsIgnoreCase(phieuDangChon.getTrangThai());
                    cbo.setDisable(!choPhepSua);
                    setGraphic(cbo);
                }
            }
        });
        colHuongXuLy.setPrefWidth(140);

        bangChiTietHangTra.getColumns().addAll(colStt, colMaSp, colTenSp, colSl, colGia, colThanhTien, colLyDo, colTinhTrang, colHuongXuLy);
    }

    private HBox taoKhungTongKetVaAction() {
        HBox box = new HBox(14);
        box.setPadding(new Insets(10, 14, 10, 14));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px;");
        box.setAlignment(Pos.CENTER_LEFT);

        // Khối tổng kết tài chính
        HBox boxTien = new HBox(16);
        boxTien.setAlignment(Pos.CENTER_LEFT);

        lblTongTienHangTra.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        lblTongTienHoan.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #b91c1c;");
        lblTongTienBu.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        boxTien.getChildren().addAll(
                new Label("Tổng hàng trả:"), lblTongTienHangTra,
                new Label("Tiền hoàn lại:"), lblTongTienHoan,
                new Label("Khách bù thêm:"), lblTongTienBu
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Nút hành động
        btnLuuCapNhat.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 7 18; -fx-cursor: hand;");
        btnLuuCapNhat.setOnAction(e -> xuLyLuuCapNhatPhieu());

        btnInLaiPhieu.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 7 16; -fx-cursor: hand;");
        btnInLaiPhieu.setOnAction(e -> moHopThoaiXemVaIn(phieuDangChon));

        btnHuyBoSua.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-font-weight: bold; -fx-padding: 7 16; -fx-cursor: hand;");
        btnHuyBoSua.setOnAction(e -> chuyenManHinh(false));

        pnlActionBarSua.setAlignment(Pos.CENTER_RIGHT);
        pnlActionBarSua.getChildren().addAll(btnLuuCapNhat, btnInLaiPhieu, btnHuyBoSua);

        box.getChildren().addAll(boxTien, spacer, pnlActionBarSua);
        return box;
    }

    private void dangKySuKien() {
        txtTimKiem.textProperty().addListener((obs, cu, moi) -> locDanhSach());

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
            btnCapNhatSl.setDisable(!coChon || !choPhepSua);
            btnXoaDong.setDisable(!coChon || !choPhepSua);

            if (coChon) {
                lblDongDangChon.setText(moi.getTenSanPham() + " (Hiện tại: " + moi.getSoLuong() + ")");
                spnSoLuongSua.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, moi.getSoLuong()));
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

        // Tải đầy đủ chi tiết phiếu từ CSDL
        PhieuDoiTra phieuDayDu = controller.layPhieuDoiTraTheoMa(p.getMaPhieuDoiTra());
        this.phieuDangChon = phieuDayDu != null ? phieuDayDu : p;

        // Tải hóa đơn gốc để lấy danh sách sản phẩm có thể thêm
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
            lblBadgeTrangThai.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #d97706; -fx-padding: 4 12; -fx-background-radius: 12px; -fx-font-weight: bold;");
            lblBannerTrangThai.setText("⚡ Phiếu đang ở trạng thái [Chờ xử lý] - Cho phép điều chỉnh thông tin, thêm/bớt sản phẩm, số lượng, lý do và hướng xử lý.");
            lblBannerTrangThai.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-padding: 8 14; -fx-background-radius: 6px; -fx-font-weight: bold;");

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
            // ĐÃ XÁC NHẬN (Đã duyệt hoặc Từ chối): KHÓA TOÀN BỘ KHÔNG CHO PHÉP SỬA ĐỔI
            if ("Đã duyệt".equalsIgnoreCase(tt) || "Hoàn thành".equalsIgnoreCase(tt)) {
                lblBadgeTrangThai.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-padding: 4 12; -fx-background-radius: 12px; -fx-font-weight: bold;");
                lblBannerTrangThai.setText("🔒 Phiếu đổi trả đã được xác nhận [Đã duyệt] - Dữ liệu đã được khóa an toàn, không thể chỉnh sửa.");
                lblBannerTrangThai.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-padding: 8 14; -fx-background-radius: 6px; -fx-font-weight: bold;");
            } else {
                lblBadgeTrangThai.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-padding: 4 12; -fx-background-radius: 12px; -fx-font-weight: bold;");
                lblBannerTrangThai.setText("🔒 Phiếu đổi trả đã bị [Từ chối] (Lý do: " + (phieuDangChon.getLyDo() != null ? phieuDangChon.getLyDo() : "Không đạt") + ") - Không thể chỉnh sửa.");
                lblBannerTrangThai.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-padding: 8 14; -fx-background-radius: 6px; -fx-font-weight: bold;");
            }

            // Ẩn toàn bộ khung chỉnh sửa
            boxThemSanPham.setVisible(false);
            boxThemSanPham.setManaged(false);
            boxDieuChinhDong.setVisible(false);
            boxDieuChinhDong.setManaged(false);

            btnLuuCapNhat.setVisible(false);
            btnLuuCapNhat.setManaged(false);
            btnHuyBoSua.setText("← Quay lại danh sách");

            btnInLaiPhieu.setVisible(true);
            btnInLaiPhieu.setManaged(true);
        }

        // Nạp danh sách sản phẩm chi tiết của phiếu
        dsChiTietSua.clear();
        if (phieuDangChon.getDanhSachSanPham() != null) {
            for (ChiTietPhieuDoiTra ct : phieuDangChon.getDanhSachSanPham()) {
                // Tạo bản sao để sửa đổi không ảnh hưởng nếu hủy
                ChiTietPhieuDoiTra copy = new ChiTietPhieuDoiTra(
                        ct.getMaPhieuDoiTra(), ct.getMaHoaDon(), ct.getMaSanPham(), ct.getTenSanPham(),
                        ct.getSoLuong(), ct.getDonGia(), ct.getLyDo(), ct.getTinhTrang(),
                        ct.getHuongXuLy(), ct.getGhiChu()
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

        if ("Đổi sang sản phẩm khác".equalsIgnoreCase(phieuDangChon.getLoaiXuLy())) {
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

        // Kiểm tra QL5 ma trận
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

        // Kiểm tra nếu sản phẩm đã có trong bảng sửa
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

    private void xuLyCapNhatSoLuongDong() {
        ChiTietPhieuDoiTra ct = bangChiTietHangTra.getSelectionModel().getSelectedItem();
        if (ct == null) return;

        int slMoi = spnSoLuongSua.getValue();
        if (slMoi <= 0) {
            hienCanhBao("Số lượng đổi trả phải lớn hơn 0!");
            return;
        }

        ct.setSoLuong(slMoi);
        bangChiTietHangTra.refresh();
        tinhToanTongTienChiTiet();
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
            // Cập nhật danh sách mới vào đối tượng phiếu
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

        Alert xacNhan = new Alert(Alert.AlertType.CONFIRMATION);
        xacNhan.setTitle("Xác nhận duyệt phiếu");
        xacNhan.setHeaderText("Duyệt phiếu đổi trả: " + phieu.getMaPhieuDoiTra());
        xacNhan.setContentText("Bạn có chắc chắn muốn duyệt phiếu đổi trả này?\nHệ thống sẽ cập nhật kho hàng và điều chỉnh tài chính hóa đơn/doanh thu ngay lập tức.");

        if (xacNhan.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                // Nếu đang ở màn hình sửa, lưu trước khi duyệt
                if (!dsChiTietSua.isEmpty() && phieuDangChon != null && phieu.getMaPhieuDoiTra().equals(phieuDangChon.getMaPhieuDoiTra())) {
                    phieuDangChon.getDanhSachSanPham().clear();
                    phieuDangChon.getDanhSachSanPham().addAll(dsChiTietSua);
                    controller.capNhatPhieuDoiTra(phieuDangChon);
                }

                boolean kq = controller.duyetPhieuDoiTra(phieu.getMaPhieuDoiTra());
                if (kq) {
                    hienThanhCong("Đã duyệt phiếu đổi trả và đồng bộ kho/doanh thu thành công!");
                    chuyenManHinh(false);
                }
            } catch (Exception ex) {
                hienLoi("Lỗi duyệt phiếu: " + ex.getMessage());
            }
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
