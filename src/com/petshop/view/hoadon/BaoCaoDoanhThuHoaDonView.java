package com.petshop.view.hoadon;

import com.petshop.controller.HoaDonController;
import com.petshop.model.HoaDon;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;

/**
 * Giao diện Báo cáo thống kê doanh thu bán lẻ sản phẩm & dịch vụ (HD_DOANH_THU / INV_REVENUE).
 * Hiển thị đầy đủ các chỉ số kinh doanh:
 * - Tổng doanh thu thực thu, Doanh thu sản phẩm, Doanh thu dịch vụ, Tổng giảm giá
 * - Doanh thu theo phương thức thanh toán (Tiền mặt, Chuyển khoản, POS)
 * - Số hóa đơn thành công, số đơn hủy, giá trị đơn trung bình (AOV)
 * - Bộ lọc khoảng thời gian nhanh (Hôm nay, 7 ngày qua, Tháng này, Tháng trước, Năm nay, Tùy chọn)
 * - Hiển thị toàn bộ hóa đơn ở tất cả trạng thái (Đã thanh toán, Đã hủy)
 */
public class BaoCaoDoanhThuHoaDonView extends VBox {

    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 VNĐ");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm");

    private final HoaDonController controller;

    // Bộ lọc thời gian và trạng thái
    private final ComboBox<String> cboMocThoiGian = new ComboBox<>();
    private final DatePicker dpTuNgay = new DatePicker(LocalDate.now().withDayOfMonth(1));
    private final DatePicker dpDenNgay = new DatePicker(LocalDate.now());
    private final ComboBox<String> cboLocTrangThai = new ComboBox<>();
    private final Button btnXemBaoCao = new Button("📊 Xuất báo cáo");
    private final Button btnLamMoi = new Button("🔄 Làm mới");

    // Thẻ chỉ số kinh doanh (KPI Cards)
    private final Label lblTongDoanhThu = new Label("0 VNĐ");
    private final Label lblDoanhThuSanPham = new Label("0 VNĐ");
    private final Label lblDoanhThuDichVu = new Label("0 VNĐ");
    private final Label lblTongGiamGia = new Label("0 VNĐ");

    private final Label lblTienMat = new Label("0 VNĐ");
    private final Label lblChuyenKhoan = new Label("0 VNĐ");
    private final Label lblSoHoaDonThanhCong = new Label("0");
    private final Label lblSoHoaDonHuy = new Label("0");
    private final Label lblGiaTriTrungBinhDon = new Label("0 VNĐ");

    // Bảng dữ liệu chi tiết
    private final ObservableList<HoaDon> dsHoaDon = FXCollections.observableArrayList();
    private final TableView<HoaDon> bangHoaDon = new TableView<>();
    private final Label lblThongKeChanBang = new Label("Hiển thị 0 hóa đơn");

    private boolean dangCapNhatMocThoiGianTuDong = false;

    public BaoCaoDoanhThuHoaDonView() {
        this.controller = new HoaDonController();

        setSpacing(12);
        setPadding(new Insets(16));
        getStyleClass().add("khung-phan-he");

        khoiTaoGiaoDien();
        dangKySuKien();
        taiBaoCao();
    }

    private void khoiTaoGiaoDien() {
        // 1. Tiêu đề
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("BÁO CÁO THỐNG KÊ DOANH THU");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Tổng hợp số liệu doanh thu từ hoạt động bán lẻ sản phẩm và cung cấp dịch vụ chăm sóc thú cưng.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Bộ lọc thời gian & trạng thái
        HBox pnlBoLoc = taoKhungBoLoc();

        // 3. Khung các thẻ chỉ số KPI
        VBox pnlTheChiSo = taoCacTheChiSo();

        // 4. Bảng dữ liệu chi tiết hóa đơn
        taoBangDuLieu();
        VBox.setVgrow(bangHoaDon, Priority.ALWAYS);

        // 5. Thanh chân trang tóm tắt
        HBox pnlChanTrang = taoChanTrang();

        getChildren().addAll(pnlTieuDe, pnlBoLoc, pnlTheChiSo, bangHoaDon, pnlChanTrang);
    }

    private HBox taoKhungBoLoc() {
        HBox pnl = new HBox(10);
        pnl.setPadding(new Insets(10, 14, 10, 14));
        pnl.getStyleClass().add("hop-noi-dung");
        pnl.setAlignment(Pos.CENTER_LEFT);

        Label lblMoc = new Label("Khoảng thời gian:");
        lblMoc.setStyle("-fx-font-weight: bold;");

        cboMocThoiGian.getItems().addAll("Hôm nay", "Hôm qua", "7 ngày gần đây", "Tháng này", "Tháng trước", "Năm nay", "Tùy chọn");
        cboMocThoiGian.setValue("Tháng này");
        cboMocThoiGian.setPrefWidth(135);

        Label lblTu = new Label("Từ ngày:");
        dpTuNgay.setPrefWidth(125);

        Label lblDen = new Label("Đến ngày:");
        dpDenNgay.setPrefWidth(125);

        Label lblTrangThai = new Label("Trạng thái:");
        lblTrangThai.setStyle("-fx-font-weight: bold;");
        cboLocTrangThai.getItems().addAll("Tất cả trạng thái", "Đã thanh toán", "Đã hủy");
        cboLocTrangThai.setValue("Tất cả trạng thái");
        cboLocTrangThai.setPrefWidth(140);
        cboLocTrangThai.setOnAction(e -> taiBaoCao());

        btnXemBaoCao.getStyleClass().add("nut-chinh");
        btnXemBaoCao.setOnAction(e -> moXuatBaoCao());

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> {
            cboMocThoiGian.setValue("Tháng này");
            cboLocTrangThai.setValue("Tất cả trạng thái");
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

        // Hàng 1: Nguồn thu và Giảm giá
        HBox hang1 = new HBox(10);
        hang1.setAlignment(Pos.CENTER);

        VBox theTongDt = taoThe("TỔNG DOANH THU THỰC THU", lblTongDoanhThu, "#0284c7", "Sau khi đã trừ khuyến mãi và điểm");
        VBox theSp = taoThe("DOANH THU SẢN PHẨM", lblDoanhThuSanPham, "#7c3aed", "Thu từ bán lẻ phụ kiện & thức ăn");
        VBox theDv = taoThe("DOANH THU DỊCH VỤ", lblDoanhThuDichVu, "#2563eb", "Thu từ tắm, spa & khám bệnh");
        VBox theGiam = taoThe("TỔNG TIỀN GIẢM GIÁ", lblTongGiamGia, "#ea580c", "Khuyến mãi voucher + Điểm tích lũy");

        HBox.setHgrow(theTongDt, Priority.ALWAYS);
        HBox.setHgrow(theSp, Priority.ALWAYS);
        HBox.setHgrow(theDv, Priority.ALWAYS);
        HBox.setHgrow(theGiam, Priority.ALWAYS);

        hang1.getChildren().addAll(theTongDt, theSp, theDv, theGiam);

        // Hàng 2: Phương thức thanh toán & Đơn hàng
        HBox hang2 = new HBox(10);
        hang2.setAlignment(Pos.CENTER);

        VBox theTm = taoThe("TIỀN MẶT", lblTienMat, "#16a34a", "Thanh toán trực tiếp tại quầy");
        VBox theCk = taoThe("CHUYỂN KHOẢN", lblChuyenKhoan, "#0d9488", "Qua mã QR / Tài khoản ngân hàng");
        VBox theTc = taoThe("ĐƠN HOÀN TẤT", lblSoHoaDonThanhCong, "#1e293b", "Hóa đơn thanh toán thành công");
        VBox theHuy = taoThe("ĐƠN ĐÃ HỦY", lblSoHoaDonHuy, "#dc2626", "Hóa đơn đã bị thu hồi / hủy");
        VBox theAov = taoThe("GIÁ TRỊ TB / ĐƠN (AOV)", lblGiaTriTrungBinhDon, "#4338ca", "Doanh thu bình quân mỗi đơn");

        HBox.setHgrow(theTm, Priority.ALWAYS);
        HBox.setHgrow(theCk, Priority.ALWAYS);
        HBox.setHgrow(theTc, Priority.ALWAYS);
        HBox.setHgrow(theHuy, Priority.ALWAYS);
        HBox.setHgrow(theAov, Priority.ALWAYS);

        hang2.getChildren().addAll(theTm, theCk, theTc, theHuy, theAov);

        box.getChildren().addAll(hang1, hang2);
        return box;
    }

    private VBox taoThe(String tieuDe, Label lblGiaTri, String mauSac, String chuThich) {
        VBox the = new VBox(4);
        the.setPadding(new Insets(10, 12, 10, 12));
        the.getStyleClass().add("hop-noi-dung");
        the.setAlignment(Pos.CENTER_LEFT);

        Label lbl = new Label(tieuDe);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        lblGiaTri.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + mauSac + ";");

        Label lblChuThich = new Label(chuThich);
        lblChuThich.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");

        the.getChildren().addAll(lbl, lblGiaTri, lblChuThich);
        return the;
    }

    private void taoBangDuLieu() {
        bangHoaDon.getStyleClass().add("bang-du-lieu");
        bangHoaDon.setItems(dsHoaDon);
        bangHoaDon.setPlaceholder(new Label("Không có dữ liệu hóa đơn nào trong khoảng thời gian đã chọn."));

        TableColumn<HoaDon, Integer> colStt = new TableColumn<>("STT");
        colStt.setPrefWidth(45);
        colStt.setStyle("-fx-alignment: center;");
        colStt.setCellValueFactory(cellData -> {
            int index = dsHoaDon.indexOf(cellData.getValue()) + 1;
            return new SimpleObjectProperty<>(index);
        });

        TableColumn<HoaDon, String> colMa = new TableColumn<>("Mã hóa đơn");
        colMa.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getMaHoaDon()));
        colMa.setPrefWidth(130);
        colMa.setStyle("-fx-font-weight: bold; -fx-alignment: center;");

        TableColumn<HoaDon, String> colNgay = new TableColumn<>("Ngày lập");
        colNgay.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getNgayLap() != null ? h.getValue().getNgayLap().format(DINH_DANG_NGAY) : ""));
        colNgay.setPrefWidth(95);
        colNgay.setStyle("-fx-alignment: center;");

        TableColumn<HoaDon, String> colGio = new TableColumn<>("Giờ lập");
        colGio.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getGioLap() != null ? h.getValue().getGioLap().format(DINH_DANG_GIO) : ""));
        colGio.setPrefWidth(70);
        colGio.setStyle("-fx-alignment: center;");

        TableColumn<HoaDon, String> colKhach = new TableColumn<>("Khách hàng");
        colKhach.setCellValueFactory(h -> {
            String ten = h.getValue().getTenKhachHang() != null ? h.getValue().getTenKhachHang() : "Khách vãng lai";
            String sdt = h.getValue().getSoDienThoaiKhachHang();
            return new SimpleStringProperty(sdt != null && !sdt.isEmpty() ? ten + " (" + sdt + ")" : ten);
        });
        colKhach.setPrefWidth(190);

        TableColumn<HoaDon, String> colNv = new TableColumn<>("Thu ngân");
        colNv.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getTenNhanVien() != null ? h.getValue().getTenNhanVien() : h.getValue().getMaNhanVien()));
        colNv.setPrefWidth(130);

        TableColumn<HoaDon, String> colHt = new TableColumn<>("Hình thức TT");
        colHt.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getHinhThucThanhToan()));
        colHt.setPrefWidth(100);
        colHt.setStyle("-fx-alignment: center;");

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
        colGiamGia.setPrefWidth(110);
        colGiamGia.setStyle("-fx-alignment: center-right; -fx-text-fill: #ea580c;");

        TableColumn<HoaDon, String> colTongTien = new TableColumn<>("Doanh thu thực");
        colTongTien.setCellValueFactory(h -> {
            if ("Đã hủy".equalsIgnoreCase(h.getValue().getTrangThaiThanhToan())) {
                return new SimpleStringProperty("0 VNĐ (Đã hủy)");
            }
            return new SimpleStringProperty(DINH_DANG_TIEN.format(h.getValue().getTongTien()));
        });
        colTongTien.setPrefWidth(130);
        colTongTien.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");

        // Cột Trạng thái có màu sắc phân biệt rõ ràng
        TableColumn<HoaDon, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getTrangThaiThanhToan()));
        colTrangThai.setPrefWidth(120);
        colTrangThai.setStyle("-fx-alignment: center;");
        colTrangThai.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-alignment: center;");
                } else {
                    setText(item);
                    if ("Đã thanh toán".equalsIgnoreCase(item)) {
                        setStyle("-fx-alignment: center; -fx-text-fill: #15803d; -fx-font-weight: bold;");
                    } else if ("Đã hủy".equalsIgnoreCase(item)) {
                        setStyle("-fx-alignment: center; -fx-text-fill: #dc2626; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-alignment: center; -fx-text-fill: #ea580c; -fx-font-weight: bold;");
                    }
                }
            }
        });

        bangHoaDon.getColumns().addAll(colStt, colMa, colNgay, colGio, colKhach, colNv, colHt, colGiaGoc, colGiamGia, colTongTien, colTrangThai);
    }

    private HBox taoChanTrang() {
        HBox pnl = new HBox(12);
        pnl.setPadding(new Insets(6, 12, 6, 12));
        pnl.setAlignment(Pos.CENTER_LEFT);
        pnl.getStyleClass().add("hop-noi-dung");

        lblThongKeChanBang.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");

        pnl.getChildren().add(lblThongKeChanBang);
        return pnl;
    }

    private void dangKySuKien() {
        // Khi chọn mốc thời gian nhanh trong ComboBox
        cboMocThoiGian.setOnAction(e -> {
            if (!dangCapNhatMocThoiGianTuDong) {
                String chon = cboMocThoiGian.getValue();
                if (chon != null && !"Tùy chọn".equals(chon)) {
                    apDungMocThoiGian(chon);
                    taiBaoCao();
                }
            }
        });

        // Khi người dùng đổi DatePicker thủ công
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

    private void danhDauTuyChon() {
        if (!dangCapNhatMocThoiGianTuDong) {
            cboMocThoiGian.setValue("Tùy chọn");
        }
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
            if (trangThaiLoc == null || "Tất cả trạng thái".equalsIgnoreCase(trangThaiLoc)) {
                trangThaiLoc = "Tất cả";
            }
            Map<String, Object> baoCao = controller.layBaoCaoDoanhThu(tuNgay, denNgay, trangThaiLoc);

            BigDecimal tongDt = (BigDecimal) baoCao.getOrDefault("tongDoanhThu", BigDecimal.ZERO);
            BigDecimal dtSp = (BigDecimal) baoCao.getOrDefault("doanhThuSanPham", BigDecimal.ZERO);
            BigDecimal dtDv = (BigDecimal) baoCao.getOrDefault("doanhThuDichVu", BigDecimal.ZERO);
            BigDecimal tongGiam = (BigDecimal) baoCao.getOrDefault("tongGiamGia", BigDecimal.ZERO);

            BigDecimal dtTm = (BigDecimal) baoCao.getOrDefault("doanhThuTienMat", BigDecimal.ZERO);
            BigDecimal dtCk = (BigDecimal) baoCao.getOrDefault("doanhThuChuyenKhoan", BigDecimal.ZERO);
            int soHd = (int) baoCao.getOrDefault("tongSoHoaDon", 0);
            int soHuy = (int) baoCao.getOrDefault("soHoaDonHuy", 0);
            int soTc = (int) baoCao.getOrDefault("soHoaDonThanhCong", Math.max(0, soHd - soHuy));
            BigDecimal aov = (BigDecimal) baoCao.getOrDefault("giaTriTrungBinhDon", BigDecimal.ZERO);

            lblTongDoanhThu.setText(DINH_DANG_TIEN.format(tongDt));
            lblDoanhThuSanPham.setText(DINH_DANG_TIEN.format(dtSp));
            lblDoanhThuDichVu.setText(DINH_DANG_TIEN.format(dtDv));
            lblTongGiamGia.setText(DINH_DANG_TIEN.format(tongGiam));

            lblTienMat.setText(DINH_DANG_TIEN.format(dtTm));
            lblChuyenKhoan.setText(DINH_DANG_TIEN.format(dtCk));
            lblSoHoaDonThanhCong.setText(String.valueOf(soTc));
            lblSoHoaDonHuy.setText(String.valueOf(soHuy));
            lblGiaTriTrungBinhDon.setText(DINH_DANG_TIEN.format(aov));

            // Xác định trạng thái lọc: Mặc định là "Tất cả" để lấy toàn bộ đơn hàng (cả Đã thanh toán và Đã hủy)
            List<HoaDon> ds = controller.timKiemHoaDon("", tuNgay, denNgay, "Tất cả", trangThaiLoc);
            dsHoaDon.setAll(ds);

            // Đếm số đơn theo trạng thái trong danh sách hiện tại
            long demThanhCong = ds.stream().filter(h -> "Đã thanh toán".equalsIgnoreCase(h.getTrangThaiThanhToan())).count();
            long demHuy = ds.stream().filter(h -> "Đã hủy".equalsIgnoreCase(h.getTrangThaiThanhToan())).count();

            BigDecimal tongGocThuc = dtSp.add(dtDv);
            lblThongKeChanBang.setText("Hiển thị: " + ds.size() + " hóa đơn (" + demThanhCong + " hoàn tất, " + demHuy + " đã hủy) | Tổng giá gốc: " + DINH_DANG_TIEN.format(tongGocThuc) + " | Tổng giảm giá: " + DINH_DANG_TIEN.format(tongGiam) + " | Tổng thực thu: " + DINH_DANG_TIEN.format(tongDt));
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
                lblTienMat.getText(),
                lblChuyenKhoan.getText(),
                lblSoHoaDonThanhCong.getText(),
                lblSoHoaDonHuy.getText(),
                lblGiaTriTrungBinhDon.getText(),
                dsHoaDon,
                chuSoHuu
        ).showAndWait();
    }
}
