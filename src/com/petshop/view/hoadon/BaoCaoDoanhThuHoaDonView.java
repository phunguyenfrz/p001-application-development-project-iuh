package com.petshop.view.hoadon;

import com.petshop.controller.HoaDonController;
import com.petshop.controller.PhieuDoiTraController;
import com.petshop.model.ChiTietPhieuDoiTra;
import com.petshop.model.HoaDon;
import com.petshop.model.PhieuDoiTra;
import javafx.application.Platform;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;

/**
 * Usecase riêng: Báo cáo thống kê toàn diện (TK_DOANH_THU).
 * Tối giản màu sắc (phong cách chuyên nghiệp, thanh lịch), tích hợp:
 * - Thẻ KPI: Doanh thu thực thu, Doanh thu SP, Doanh thu DV, Giảm giá, Tiền hoàn đổi trả, Tiền khách bù, Đơn hoàn tất, Đơn hủy, AOV
 * - Tab 1: Hóa đơn thanh toán (bổ sung cột Giá gốc & Giảm giá)
 * - Tab 2: Sản phẩm bán ra trong ngày/tuần/tháng
 * - Tab 3: Dịch vụ chăm sóc thực hiện trong ngày/tuần/tháng
 * - Tab 4: Báo cáo sản phẩm đổi trả & Tổng tiền hoàn
 */
public class BaoCaoDoanhThuHoaDonView extends VBox {

    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 VNĐ");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm");

    private final HoaDonController hoaDonController = new HoaDonController();
    private final PhieuDoiTraController phieuDoiTraController = new PhieuDoiTraController();

    // Bộ lọc thời gian và trạng thái
    private final ComboBox<String> cboMocThoiGian = new ComboBox<>();
    private final DatePicker dpTuNgay = new DatePicker(LocalDate.now().withDayOfMonth(1));
    private final DatePicker dpDenNgay = new DatePicker(LocalDate.now());
    private final ComboBox<String> cboLocTrangThai = new ComboBox<>();
    private final Button btnXemBaoCao = new Button("📊 Xuất báo cáo");
    private final Button btnLamMoi = new Button("🔄 Làm mới");

    // Thẻ chỉ số kinh doanh (KPI Cards) - Tone màu tối giản thanh lịch (#0f172a, #334155)
    private final Label lblTongDoanhThu = new Label("0 VNĐ");
    private final Label lblDoanhThuSanPham = new Label("0 VNĐ");
    private final Label lblDoanhThuDichVu = new Label("0 VNĐ");
    private final Label lblTongGiamGia = new Label("0 VNĐ");

    private final Label lblTongHoanDoiTra = new Label("0 VNĐ");
    private final Label lblTongBuDoiTra = new Label("0 VNĐ");
    private final Label lblTienMat = new Label("0 VNĐ");
    private final Label lblChuyenKhoan = new Label("0 VNĐ");
    private final Label lblSoHoaDonThanhCong = new Label("0");
    private final Label lblSoHoaDonHuy = new Label("0");
    private final Label lblGiaTriTrungBinhDon = new Label("0 VNĐ");

    // Dữ liệu cho 4 Tab
    private final ObservableList<HoaDon> dsHoaDon = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> dsSanPhamBan = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> dsDichVuLam = FXCollections.observableArrayList();
    private final ObservableList<DongHangDoiTraBaoCao> dsHangDoiTra = FXCollections.observableArrayList();

    private final TableView<HoaDon> bangHoaDon = new TableView<>();
    private final TableView<Map<String, Object>> bangSanPham = new TableView<>();
    private final TableView<Map<String, Object>> bangDichVu = new TableView<>();
    private final TableView<DongHangDoiTraBaoCao> bangDoiTra = new TableView<>();

    private final Label lblThongKeChanBang = new Label("Hiển thị 0 hóa đơn");
    private boolean dangCapNhatMocThoiGianTuDong = false;

    public BaoCaoDoanhThuHoaDonView() {
        setSpacing(12);
        setPadding(new Insets(16));
        setStyle("-fx-background-color: #f8fafc;");

        khoiTaoGiaoDien();
        dangKySuKien();
        taiBaoCao();
    }

    private void khoiTaoGiaoDien() {
        // 1. Tiêu đề
        VBox pnlTieuDe = new VBox(3);
        Label lblTieuDe = new Label("BÁO CÁO THỐNG KÊ DOANH THU & HOẠT ĐỘNG");
        lblTieuDe.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        Label lblMoTa = new Label("Tổng hợp toàn diện doanh thu bán lẻ, dịch vụ chăm sóc thú cưng và tình hình đổi trả sản phẩm.");
        lblMoTa.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Bộ lọc thời gian & trạng thái
        HBox pnlBoLoc = taoKhungBoLoc();

        // 3. Khung các thẻ chỉ số KPI (Tone màu tối giản thanh lịch, bỏ sặc sỡ)
        VBox pnlTheChiSo = taoCacTheChiSo();

        // 4. TabPane đa năng hiển thị chi tiết các danh mục
        TabPane tabPane = taoTabPaneChiTiet();
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        // 5. Thanh chân trang tóm tắt
        HBox pnlChanTrang = taoChanTrang();

        getChildren().addAll(pnlTieuDe, pnlBoLoc, pnlTheChiSo, tabPane, pnlChanTrang);
    }

    private HBox taoKhungBoLoc() {
        HBox pnl = new HBox(10);
        pnl.setPadding(new Insets(10, 14, 10, 14));
        pnl.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px;");
        pnl.setAlignment(Pos.CENTER_LEFT);

        Label lblMoc = new Label("Khoảng thời gian:");
        lblMoc.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");

        cboMocThoiGian.getItems().addAll("Hôm nay", "Hôm qua", "7 ngày gần đây", "Tháng này", "Tháng trước", "Năm nay", "Tùy chọn");
        cboMocThoiGian.setValue("Tháng này");
        cboMocThoiGian.setPrefWidth(135);

        Label lblTu = new Label("Từ ngày:");
        dpTuNgay.setPrefWidth(125);

        Label lblDen = new Label("Đến ngày:");
        dpDenNgay.setPrefWidth(125);

        Label lblTrangThai = new Label("Trạng thái HĐ:");
        lblTrangThai.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        cboLocTrangThai.getItems().addAll("Tất cả", "Đã thanh toán", "Đã hủy");
        cboLocTrangThai.setValue("Tất cả");
        cboLocTrangThai.setPrefWidth(140);
        cboLocTrangThai.setOnAction(e -> taiBaoCao());

        btnXemBaoCao.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 14; -fx-cursor: hand;");
        btnXemBaoCao.setOnAction(e -> moXuatBaoCao());

        btnLamMoi.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-font-weight: bold; -fx-padding: 6 14; -fx-cursor: hand;");
        btnLamMoi.setOnAction(e -> {
            cboMocThoiGian.setValue("Tháng này");
            cboLocTrangThai.setValue("Tất cả");
            apDungMocThoiGian("Tháng này");
            taiBaoCao();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        pnl.getChildren().addAll(lblMoc, cboMocThoiGian, new Separator(javafx.geometry.Orientation.VERTICAL),
                lblTu, dpTuNgay, lblDen, dpDenNgay, new Separator(javafx.geometry.Orientation.VERTICAL),
                lblTrangThai, cboLocTrangThai, btnXemBaoCao, spacer, btnLamMoi);
        return pnl;
    }

    private VBox taoCacTheChiSo() {
        VBox box = new VBox(8);

        // Hàng 1: Nguồn thu chính & Giảm giá (Tone màu tối giản: #0f172a, #334155)
        HBox hang1 = new HBox(10);
        hang1.setAlignment(Pos.CENTER);

        VBox theTongDt = taoThe("DOANH THU THỰC THU", lblTongDoanhThu, "#0f172a", "Đã trừ giảm giá & điều chỉnh đổi trả");
        VBox theSp = taoThe("DOANH THU SẢN PHẨM", lblDoanhThuSanPham, "#0f172a", "Bán lẻ thức ăn & phụ kiện");
        VBox theDv = taoThe("DOANH THU DỊCH VỤ", lblDoanhThuDichVu, "#0f172a", "Dịch vụ spa & chăm sóc thú cưng");
        VBox theGiam = taoThe("TỔNG TIỀN GIẢM GIÁ", lblTongGiamGia, "#475569", "Khuyến mãi + Điểm tích lũy");

        HBox.setHgrow(theTongDt, Priority.ALWAYS);
        HBox.setHgrow(theSp, Priority.ALWAYS);
        HBox.setHgrow(theDv, Priority.ALWAYS);
        HBox.setHgrow(theGiam, Priority.ALWAYS);

        hang1.getChildren().addAll(theTongDt, theSp, theDv, theGiam);

        // Hàng 2: Tình hình đổi trả, phương thức thanh toán & đơn hàng
        HBox hang2 = new HBox(10);
        hang2.setAlignment(Pos.CENTER);

        VBox theHoanDt = taoThe("TIỀN HOÀN ĐỔI TRẢ", lblTongHoanDoiTra, "#b91c1c", "Hoàn lại cho khách từ hàng đổi trả");
        VBox theBuDt = taoThe("TIỀN KHÁCH BÙ ĐỔI TRẢ", lblTongBuDoiTra, "#0f172a", "Khách bù thêm khi đổi hàng");
        VBox theTc = taoThe("ĐƠN HOÀN TẤT", lblSoHoaDonThanhCong, "#0f172a", "Hóa đơn thanh toán thành công");
        VBox theHuy = taoThe("ĐƠN ĐÃ HỦY", lblSoHoaDonHuy, "#b91c1c", "Hóa đơn đã bị thu hồi / hủy");
        VBox theAov = taoThe("GIÁ TRỊ TB / ĐƠN (AOV)", lblGiaTriTrungBinhDon, "#0f172a", "Doanh thu bình quân mỗi đơn");

        HBox.setHgrow(theHoanDt, Priority.ALWAYS);
        HBox.setHgrow(theBuDt, Priority.ALWAYS);
        HBox.setHgrow(theTc, Priority.ALWAYS);
        HBox.setHgrow(theHuy, Priority.ALWAYS);
        HBox.setHgrow(theAov, Priority.ALWAYS);

        hang2.getChildren().addAll(theHoanDt, theBuDt, theTc, theHuy, theAov);

        box.getChildren().addAll(hang1, hang2);
        return box;
    }

    private VBox taoThe(String tieuDe, Label lblGiaTri, String mauSac, String chuThich) {
        VBox the = new VBox(3);
        the.setPadding(new Insets(8, 12, 8, 12));
        the.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px;");
        the.setAlignment(Pos.CENTER_LEFT);

        Label lbl = new Label(tieuDe);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        lblGiaTri.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + mauSac + ";");

        Label lblChuThich = new Label(chuThich);
        lblChuThich.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");

        the.getChildren().addAll(lbl, lblGiaTri, lblChuThich);
        return the;
    }

    private TabPane taoTabPaneChiTiet() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px;");

        // Tab 1: Hóa đơn
        taoBangHoaDon();
        Tab tab1 = new Tab("📋 Danh sách Hóa đơn", bangHoaDon);

        // Tab 2: Sản phẩm bán ra
        taoBangSanPham();
        Tab tab2 = new Tab("📦 Sản phẩm bán ra", bangSanPham);

        // Tab 3: Dịch vụ chăm sóc
        taoBangDichVu();
        Tab tab3 = new Tab("✂️ Dịch vụ thực hiện", bangDichVu);

        // Tab 4: Sản phẩm đổi trả
        taoBangDoiTra();
        Tab tab4 = new Tab("🔄 Hàng đổi trả & Tiền hoàn", bangDoiTra);

        tabPane.getTabs().addAll(tab1, tab2, tab3, tab4);
        return tabPane;
    }

    private void taoBangHoaDon() {
        bangHoaDon.setItems(dsHoaDon);
        bangHoaDon.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        bangHoaDon.setPlaceholder(new Label("Không có dữ liệu hóa đơn trong khoảng thời gian đã chọn."));

        TableColumn<HoaDon, Integer> colStt = new TableColumn<>("STT");
        colStt.setMaxWidth(45);
        colStt.setStyle("-fx-alignment: center;");
        colStt.setCellValueFactory(c -> new SimpleObjectProperty<>(dsHoaDon.indexOf(c.getValue()) + 1));

        TableColumn<HoaDon, String> colMa = new TableColumn<>("Mã hóa đơn");
        colMa.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getMaHoaDon()));
        colMa.setPrefWidth(125);
        colMa.setStyle("-fx-font-weight: bold;");

        TableColumn<HoaDon, String> colNgay = new TableColumn<>("Ngày lập");
        colNgay.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getNgayLap() != null ? h.getValue().getNgayLap().format(DINH_DANG_NGAY) : ""));
        colNgay.setPrefWidth(90);

        TableColumn<HoaDon, String> colGio = new TableColumn<>("Giờ lập");
        colGio.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getGioLap() != null ? h.getValue().getGioLap().format(DINH_DANG_GIO) : ""));
        colGio.setPrefWidth(65);

        TableColumn<HoaDon, String> colKhach = new TableColumn<>("Khách hàng");
        colKhach.setCellValueFactory(h -> {
            String ten = h.getValue().getTenKhachHang() != null ? h.getValue().getTenKhachHang() : "Khách vãng lai";
            String sdt = h.getValue().getSoDienThoaiKhachHang();
            return new SimpleStringProperty(sdt != null && !sdt.isEmpty() ? ten + " (" + sdt + ")" : ten);
        });
        colKhach.setPrefWidth(170);

        TableColumn<HoaDon, String> colNv = new TableColumn<>("Thu ngân");
        colNv.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getTenNhanVien() != null ? h.getValue().getTenNhanVien() : h.getValue().getMaNhanVien()));
        colNv.setPrefWidth(115);

        TableColumn<HoaDon, String> colHt = new TableColumn<>("Hình thức TT");
        colHt.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getHinhThucThanhToan()));
        colHt.setPrefWidth(95);

        TableColumn<HoaDon, String> colGiaGoc = new TableColumn<>("Giá gốc");
        colGiaGoc.setCellValueFactory(h -> {
            BigDecimal giaGoc = h.getValue().getTongTienGoc();
            return new SimpleStringProperty(DINH_DANG_TIEN.format(giaGoc != null ? giaGoc : BigDecimal.ZERO));
        });
        colGiaGoc.setPrefWidth(110);
        colGiaGoc.setStyle("-fx-alignment: center-right; -fx-text-fill: #475569;");

        TableColumn<HoaDon, String> colGiamGia = new TableColumn<>("Giảm giá");
        colGiamGia.setCellValueFactory(h -> {
            if ("Đã hủy".equalsIgnoreCase(h.getValue().getTrangThaiThanhToan())) {
                return new SimpleStringProperty("0 VNĐ");
            }
            BigDecimal tongGiam = h.getValue().getTongTienGiam();
            return new SimpleStringProperty(DINH_DANG_TIEN.format(tongGiam != null ? tongGiam : BigDecimal.ZERO));
        });
        colGiamGia.setPrefWidth(100);
        colGiamGia.setStyle("-fx-alignment: center-right;");

        TableColumn<HoaDon, String> colTongTien = new TableColumn<>("Doanh thu thực");
        colTongTien.setCellValueFactory(h -> {
            if ("Đã hủy".equalsIgnoreCase(h.getValue().getTrangThaiThanhToan())) {
                return new SimpleStringProperty("0 VNĐ (Đã hủy)");
            }
            return new SimpleStringProperty(DINH_DANG_TIEN.format(h.getValue().getTongTien()));
        });
        colTongTien.setPrefWidth(120);
        colTongTien.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");

        TableColumn<HoaDon, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getTrangThaiThanhToan()));
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
                    if ("Đã thanh toán".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #15803d; -fx-font-weight: bold;");
                    } else if ("Đã hủy".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold;");
                    }
                }
            }
        });

        bangHoaDon.getColumns().addAll(colStt, colMa, colNgay, colGio, colKhach, colNv, colHt, colGiaGoc, colGiamGia, colTongTien, colTrangThai);
    }

    private void taoBangSanPham() {
        bangSanPham.setItems(dsSanPhamBan);
        bangSanPham.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        bangSanPham.setPlaceholder(new Label("Không có dữ liệu sản phẩm nào được bán trong khoảng thời gian đã chọn."));

        TableColumn<Map<String, Object>, Integer> colStt = new TableColumn<>("STT");
        colStt.setMaxWidth(45);
        colStt.setStyle("-fx-alignment: center;");
        colStt.setCellValueFactory(c -> new SimpleObjectProperty<>(dsSanPhamBan.indexOf(c.getValue()) + 1));

        TableColumn<Map<String, Object>, String> colMa = new TableColumn<>("Mã sản phẩm");
        colMa.setCellValueFactory(d -> new SimpleStringProperty((String) d.getValue().get("maSanPham")));
        colMa.setPrefWidth(120);

        TableColumn<Map<String, Object>, String> colTen = new TableColumn<>("Tên sản phẩm");
        colTen.setCellValueFactory(d -> new SimpleStringProperty((String) d.getValue().get("tenSanPham")));
        colTen.setPrefWidth(260);

        TableColumn<Map<String, Object>, String> colDvt = new TableColumn<>("ĐVT");
        colDvt.setCellValueFactory(d -> new SimpleStringProperty((String) d.getValue().get("donViTinh")));
        colDvt.setPrefWidth(70);

        TableColumn<Map<String, Object>, String> colSl = new TableColumn<>("Số lượng bán");
        colSl.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("soLuong"))));
        colSl.setPrefWidth(90);
        colSl.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");

        TableColumn<Map<String, Object>, String> colDt = new TableColumn<>("Tổng doanh thu");
        colDt.setCellValueFactory(d -> {
            BigDecimal dt = (BigDecimal) d.getValue().get("doanhThu");
            return new SimpleStringProperty(DINH_DANG_TIEN.format(dt != null ? dt : BigDecimal.ZERO));
        });
        colDt.setPrefWidth(130);
        colDt.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");

        bangSanPham.getColumns().addAll(colStt, colMa, colTen, colDvt, colSl, colDt);
    }

    private void taoBangDichVu() {
        bangDichVu.setItems(dsDichVuLam);
        bangDichVu.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        bangDichVu.setPlaceholder(new Label("Không có dữ liệu dịch vụ nào được thực hiện trong khoảng thời gian đã chọn."));

        TableColumn<Map<String, Object>, Integer> colStt = new TableColumn<>("STT");
        colStt.setMaxWidth(45);
        colStt.setStyle("-fx-alignment: center;");
        colStt.setCellValueFactory(c -> new SimpleObjectProperty<>(dsDichVuLam.indexOf(c.getValue()) + 1));

        TableColumn<Map<String, Object>, String> colMa = new TableColumn<>("Mã dịch vụ");
        colMa.setCellValueFactory(d -> new SimpleStringProperty((String) d.getValue().get("maDichVu")));
        colMa.setPrefWidth(120);

        TableColumn<Map<String, Object>, String> colTen = new TableColumn<>("Tên dịch vụ chăm sóc");
        colTen.setCellValueFactory(d -> new SimpleStringProperty((String) d.getValue().get("tenDichVu")));
        colTen.setPrefWidth(280);

        TableColumn<Map<String, Object>, String> colSl = new TableColumn<>("Số lượt thực hiện");
        colSl.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("soLuong"))));
        colSl.setPrefWidth(110);
        colSl.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");

        TableColumn<Map<String, Object>, String> colDt = new TableColumn<>("Tổng doanh thu dịch vụ");
        colDt.setCellValueFactory(d -> {
            BigDecimal dt = (BigDecimal) d.getValue().get("doanhThu");
            return new SimpleStringProperty(DINH_DANG_TIEN.format(dt != null ? dt : BigDecimal.ZERO));
        });
        colDt.setPrefWidth(140);
        colDt.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");

        bangDichVu.getColumns().addAll(colStt, colMa, colTen, colSl, colDt);
    }

    private void taoBangDoiTra() {
        bangDoiTra.setItems(dsHangDoiTra);
        bangDoiTra.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        bangDoiTra.setPlaceholder(new Label("Không có sản phẩm nào được đổi trả trong khoảng thời gian đã chọn."));

        TableColumn<DongHangDoiTraBaoCao, Integer> colStt = new TableColumn<>("STT");
        colStt.setMaxWidth(45);
        colStt.setStyle("-fx-alignment: center;");
        colStt.setCellValueFactory(c -> new SimpleObjectProperty<>(dsHangDoiTra.indexOf(c.getValue()) + 1));

        TableColumn<DongHangDoiTraBaoCao, String> colMaP = new TableColumn<>("Mã phiếu");
        colMaP.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().maPhieu));
        colMaP.setPrefWidth(110);

        TableColumn<DongHangDoiTraBaoCao, String> colMaHd = new TableColumn<>("Mã HĐ gốc");
        colMaHd.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().maHoaDon));
        colMaHd.setPrefWidth(110);

        TableColumn<DongHangDoiTraBaoCao, String> colTenSp = new TableColumn<>("Sản phẩm đổi/trả");
        colTenSp.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().tenSanPham));
        colTenSp.setPrefWidth(170);

        TableColumn<DongHangDoiTraBaoCao, Integer> colSl = new TableColumn<>("SL");
        colSl.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().soLuong));
        colSl.setMaxWidth(50);
        colSl.setStyle("-fx-alignment: center-right;");

        TableColumn<DongHangDoiTraBaoCao, String> colLyDo = new TableColumn<>("Lý do");
        colLyDo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().lyDo));
        colLyDo.setPrefWidth(95);

        TableColumn<DongHangDoiTraBaoCao, String> colHuong = new TableColumn<>("Hướng xử lý (QL26)");
        colHuong.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().huongXuLy));
        colHuong.setPrefWidth(110);

        TableColumn<DongHangDoiTraBaoCao, String> colHoan = new TableColumn<>("Tiền hoàn lại");
        colHoan.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().tienHoan)));
        colHoan.setPrefWidth(95);
        colHoan.setStyle("-fx-alignment: center-right; -fx-text-fill: #b91c1c;");

        TableColumn<DongHangDoiTraBaoCao, String> colBu = new TableColumn<>("Khách bù");
        colBu.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().tienBu)));
        colBu.setPrefWidth(85);
        colBu.setStyle("-fx-alignment: center-right;");

        TableColumn<DongHangDoiTraBaoCao, String> colTt = new TableColumn<>("Trạng thái");
        colTt.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().trangThai));
        colTt.setPrefWidth(90);
        colTt.setStyle("-fx-alignment: center; -fx-font-weight: bold;");

        bangDoiTra.getColumns().addAll(colStt, colMaP, colMaHd, colTenSp, colSl, colLyDo, colHuong, colHoan, colBu, colTt);
    }

    private HBox taoChanTrang() {
        HBox pnl = new HBox(12);
        pnl.setPadding(new Insets(6, 12, 6, 12));
        pnl.setAlignment(Pos.CENTER_LEFT);
        pnl.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px;");

        lblThongKeChanBang.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
        pnl.getChildren().add(lblThongKeChanBang);
        return pnl;
    }

    private void dangKySuKien() {
        cboMocThoiGian.setOnAction(e -> {
            if (!dangCapNhatMocThoiGianTuDong) {
                String chon = cboMocThoiGian.getValue();
                if (chon != null && !"Tùy chọn".equals(chon)) {
                    apDungMocThoiGian(chon);
                    taiBaoCao();
                }
            }
        });

        dpTuNgay.valueProperty().addListener((obs, cu, moi) -> thayDoiNgayThuCong());
        dpDenNgay.valueProperty().addListener((obs, cu, moi) -> thayDoiNgayThuCong());
    }

    private void thayDoiNgayThuCong() {
        if (dangCapNhatMocThoiGianTuDong) return;
        cboMocThoiGian.setValue("Tùy chọn");
        Platform.runLater(() -> {
            if (!dangCapNhatMocThoiGianTuDong) {
                taiBaoCao();
            }
        });
    }

    private void apDungMocThoiGian(String moc) {
        dangCapNhatMocThoiGianTuDong = true;
        LocalDate homNay = LocalDate.now();

        switch (moc) {
            case "Hôm nay":
                dpTuNgay.setValue(homNay);
                dpDenNgay.setValue(homNay);
                break;
            case "Hôm qua":
                dpTuNgay.setValue(homNay.minusDays(1));
                dpDenNgay.setValue(homNay.minusDays(1));
                break;
            case "7 ngày gần đây":
                dpTuNgay.setValue(homNay.minusDays(6));
                dpDenNgay.setValue(homNay);
                break;
            case "Tháng này":
                dpTuNgay.setValue(homNay.with(TemporalAdjusters.firstDayOfMonth()));
                dpDenNgay.setValue(homNay);
                break;
            case "Tháng trước":
                LocalDate thangTruoc = homNay.minusMonths(1);
                dpTuNgay.setValue(thangTruoc.with(TemporalAdjusters.firstDayOfMonth()));
                dpDenNgay.setValue(thangTruoc.with(TemporalAdjusters.lastDayOfMonth()));
                break;
            case "Năm nay":
                dpTuNgay.setValue(homNay.with(TemporalAdjusters.firstDayOfYear()));
                dpDenNgay.setValue(homNay);
                break;
            default:
                break;
        }
        dangCapNhatMocThoiGianTuDong = false;
    }

    private void taiBaoCao() {
        LocalDate tuNgay = dpTuNgay.getValue();
        LocalDate denNgay = dpDenNgay.getValue();

        if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Từ ngày không được lớn hơn Đến ngày!");
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }

        try {
            String trangThaiLoc = cboLocTrangThai.getValue();
            if (trangThaiLoc == null || trangThaiLoc.contains("Tất cả") || trangThaiLoc.isBlank()) {
                trangThaiLoc = "Tất cả";
            }
            Map<String, Object> baoCao = hoaDonController.layBaoCaoDoanhThu(tuNgay, denNgay, trangThaiLoc);

            BigDecimal tongDt = (BigDecimal) baoCao.getOrDefault("tongDoanhThu", BigDecimal.ZERO);
            BigDecimal dtSp = (BigDecimal) baoCao.getOrDefault("doanhThuSanPham", BigDecimal.ZERO);
            BigDecimal dtDv = (BigDecimal) baoCao.getOrDefault("doanhThuDichVu", BigDecimal.ZERO);
            BigDecimal tongGiam = (BigDecimal) baoCao.getOrDefault("tongGiamGia", BigDecimal.ZERO);

            BigDecimal tongHoanDt = (BigDecimal) baoCao.getOrDefault("tongTienHoanDoiTra", BigDecimal.ZERO);
            BigDecimal tongBuDt = (BigDecimal) baoCao.getOrDefault("tongTienBuDoiTra", BigDecimal.ZERO);

            BigDecimal dtTm = (BigDecimal) baoCao.getOrDefault("doanhThuTienMat", BigDecimal.ZERO);
            BigDecimal dtCk = (BigDecimal) baoCao.getOrDefault("doanhThuChuyenKhoan", BigDecimal.ZERO);
            int soHd = (int) baoCao.getOrDefault("tongSoHoaDon", 0);
            int soHuy = (int) baoCao.getOrDefault("soHoaDonHuy", 0);
            int soTc = (int) baoCao.getOrDefault("soHoaDonThanhCong", Math.max(0, soHd - soHuy));
            BigDecimal aov = (BigDecimal) baoCao.getOrDefault("giaTriTrungBinhDon", BigDecimal.ZERO);

            // Cập nhật các thẻ KPI
            lblTongDoanhThu.setText(DINH_DANG_TIEN.format(tongDt));
            lblDoanhThuSanPham.setText(DINH_DANG_TIEN.format(dtSp));
            lblDoanhThuDichVu.setText(DINH_DANG_TIEN.format(dtDv));
            lblTongGiamGia.setText(DINH_DANG_TIEN.format(tongGiam));

            lblTongHoanDoiTra.setText(DINH_DANG_TIEN.format(tongHoanDt));
            lblTongBuDoiTra.setText(DINH_DANG_TIEN.format(tongBuDt));
            lblTienMat.setText(DINH_DANG_TIEN.format(dtTm));
            lblChuyenKhoan.setText(DINH_DANG_TIEN.format(dtCk));
            lblSoHoaDonThanhCong.setText(String.valueOf(soTc));
            lblSoHoaDonHuy.setText(String.valueOf(soHuy));
            lblGiaTriTrungBinhDon.setText(DINH_DANG_TIEN.format(aov));

            // 1. Nạp danh sách Hóa đơn
            List<HoaDon> ds = hoaDonController.timKiemHoaDon("", tuNgay, denNgay, "Tất cả", trangThaiLoc);
            dsHoaDon.setAll(ds);

            // 2. Nạp danh sách Sản phẩm bán ra
            List<Map<String, Object>> dsSp = hoaDonController.layBaoCaoSanPhamBanRa(tuNgay, denNgay);
            dsSanPhamBan.setAll(dsSp);

            // 3. Nạp danh sách Dịch vụ thực hiện
            List<Map<String, Object>> dsDv = hoaDonController.layBaoCaoDichVuThucHien(tuNgay, denNgay);
            dsDichVuLam.setAll(dsDv);

            // 4. Nạp danh sách Sản phẩm đổi trả
            dsHangDoiTra.clear();
            List<PhieuDoiTra> dsPdt = phieuDoiTraController.timKiemPhieuDoiTra("", tuNgay, denNgay, "Tất cả", "Tất cả");
            if (dsPdt != null) {
                for (PhieuDoiTra p : dsPdt) {
                    if (p.getDanhSachSanPham() != null && !p.getDanhSachSanPham().isEmpty()) {
                        for (ChiTietPhieuDoiTra ct : p.getDanhSachSanPham()) {
                            dsHangDoiTra.add(new DongHangDoiTraBaoCao(
                                    p.getMaPhieuDoiTra(),
                                    p.getMaHoaDon(),
                                    ct.getTenSanPham(),
                                    ct.getSoLuong(),
                                    ct.getLyDo(),
                                    ct.getHuongXuLy(),
                                    ct.tinhThanhTienTra(),
                                    p.getTienBu() != null ? p.getTienBu() : BigDecimal.ZERO,
                                    p.getTrangThai()
                            ));
                        }
                    } else {
                        // Dòng tóm tắt nếu không có chi tiết
                        dsHangDoiTra.add(new DongHangDoiTraBaoCao(
                                p.getMaPhieuDoiTra(),
                                p.getMaHoaDon(),
                                "Mặt hàng theo HĐ",
                                p.getSoLuong(),
                                p.getLyDo(),
                                "Đang cập nhật",
                                p.getTienHoan() != null ? p.getTienHoan() : BigDecimal.ZERO,
                                p.getTienBu() != null ? p.getTienBu() : BigDecimal.ZERO,
                                p.getTrangThai()
                        ));
                    }
                }
            }

            long demThanhCong = ds.stream().filter(h -> "Đã thanh toán".equalsIgnoreCase(h.getTrangThaiThanhToan())).count();
            long demHuy = ds.stream().filter(h -> "Đã hủy".equalsIgnoreCase(h.getTrangThaiThanhToan())).count();
            BigDecimal tongGocThuc = dtSp.add(dtDv);
            lblThongKeChanBang.setText(String.format("Hiển thị: %d hóa đơn (%d hoàn tất, %d đã hủy) | %d SP bán ra | %d dịch vụ | %d lượt đổi trả | Tổng giá gốc: %s | Tổng thực thu: %s",
                    ds.size(), demThanhCong, demHuy, dsSp.size(), dsDv.size(), dsHangDoiTra.size(), DINH_DANG_TIEN.format(tongGocThuc), DINH_DANG_TIEN.format(tongDt)));

        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Lỗi khi tải báo cáo doanh thu: " + ex.getMessage());
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    public void moXuatBaoCao() {
        taiBaoCao();
        String khoangThoiGian = dpTuNgay.getValue() != null && dpDenNgay.getValue() != null
                ? dpTuNgay.getValue().format(DINH_DANG_NGAY) + " - " + dpDenNgay.getValue().format(DINH_DANG_NGAY)
                : "";
        Stage chuSoHuu = getScene() != null ? (Stage) getScene().getWindow() : null;
        new InBaoCaoDoanhThuDialog(
                khoangThoiGian,
                cboLocTrangThai.getValue(),
                lblTongDoanhThu.getText(),
                lblDoanhThuSanPham.getText(),
                lblDoanhThuDichVu.getText(),
                lblTongGiamGia.getText(),
                lblTongHoanDoiTra.getText(),
                lblTongBuDoiTra.getText(),
                lblTienMat.getText(),
                lblChuyenKhoan.getText(),
                lblSoHoaDonThanhCong.getText(),
                lblSoHoaDonHuy.getText(),
                lblGiaTriTrungBinhDon.getText(),
                new java.util.ArrayList<>(dsHoaDon),
                new java.util.ArrayList<>(dsSanPhamBan),
                new java.util.ArrayList<>(dsDichVuLam),
                new java.util.ArrayList<>(dsHangDoiTra),
                chuSoHuu
        ).showAndWait();
    }

    /**
     * DTO hiển thị dữ liệu bảng hàng đổi trả trong báo cáo doanh thu.
     */
    public static class DongHangDoiTraBaoCao {
        public final String maPhieu;
        public final String maHoaDon;
        public final String tenSanPham;
        public final int soLuong;
        public final String lyDo;
        public final String huongXuLy;
        public final BigDecimal tienHoan;
        public final BigDecimal tienBu;
        public final String trangThai;

        public DongHangDoiTraBaoCao(String maPhieu, String maHoaDon, String tenSanPham, int soLuong,
                                   String lyDo, String huongXuLy, BigDecimal tienHoan, BigDecimal tienBu, String trangThai) {
            this.maPhieu = maPhieu;
            this.maHoaDon = maHoaDon;
            this.tenSanPham = tenSanPham;
            this.soLuong = soLuong;
            this.lyDo = lyDo;
            this.huongXuLy = huongXuLy;
            this.tienHoan = tienHoan != null ? tienHoan : BigDecimal.ZERO;
            this.tienBu = tienBu != null ? tienBu : BigDecimal.ZERO;
            this.trangThai = trangThai;
        }
    }
}
