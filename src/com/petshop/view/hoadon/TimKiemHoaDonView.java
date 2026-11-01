package com.petshop.view.hoadon;

import com.petshop.controller.HoaDonController;
import com.petshop.model.HoaDon;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Giao diện Tra cứu và quản lý lịch sử hóa đơn bán lẻ (INV_SEARCH / HD_TIM_KIEM).
 * Cho phép lọc hóa đơn theo khoảng ngày, hình thức thanh toán, trạng thái, xem chi tiết và in hóa đơn.
 */
public class TimKiemHoaDonView extends VBox {

    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 VNĐ");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final HoaDonController controller;
    private final ObservableList<HoaDon> dsHoaDon = FXCollections.observableArrayList();

    // Điều khiển tìm kiếm
    private final TextField txtTuKhoa = new TextField();
    private final DatePicker dpTuNgay = new DatePicker(LocalDate.now().minusMonths(1));
    private final DatePicker dpDenNgay = new DatePicker(LocalDate.now());
    private final ComboBox<String> cboHinhThuc = new ComboBox<>();
    private final ComboBox<String> cboTrangThai = new ComboBox<>();
    private final TableView<HoaDon> bangHoaDon = new TableView<>();

    // Thẻ tổng kết
    private final Label lblTongSoHoaDon = new Label("0");
    private final Label lblTongDoanhThu = new Label("0 VNĐ");
    private final Label lblTongDaHuy = new Label("0");
    private final Button btnXemChiTiet = new Button("Xem chi tiết");

    public TimKiemHoaDonView() {
        this.controller = new HoaDonController();

        setSpacing(12);
        setPadding(new Insets(16));
        getStyleClass().add("khung-phan-he");

        khoiTaoGiaoDien();
        dangKySuKien();
        napDanhSachHoaDon();
    }

    private void khoiTaoGiaoDien() {
        // 1. Tiêu đề
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("TRA CỨU & QUẢN LÝ HÓA ĐƠN BÁN LẺ");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Tra cứu danh sách hóa đơn theo ngày, trạng thái thanh toán, xem chi tiết và hủy hóa đơn nếu cần.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Thanh bộ lọc
        VBox pnlBoLoc = taoThanhBoLoc();

        // 3. Bảng dữ liệu
        taoBangDuLieu();
        VBox.setVgrow(bangHoaDon, Priority.ALWAYS);

        // 4. Thanh tổng kết dưới đáy
        HBox pnlDay = taoThanhTongKet();

        getChildren().addAll(pnlTieuDe, pnlBoLoc, bangHoaDon, pnlDay);
    }

    private VBox taoThanhBoLoc() {
        VBox pnl = new VBox(10);
        pnl.setPadding(new Insets(12));
        pnl.getStyleClass().add("hop-noi-dung");

        // Hàng 1: Từ khóa + Khoảng ngày + Nút lọc ngày nhanh
        HBox hang1 = new HBox(10);
        hang1.setAlignment(Pos.CENTER_LEFT);

        txtTuKhoa.setPromptText("🔍 Nhập mã HĐ, tên/SĐT khách hàng, nhân viên lập...");
        txtTuKhoa.setPrefWidth(320);

        Label lblTu = new Label("Từ ngày:");
        caiDatDinhDangNgay(dpTuNgay);
        dpTuNgay.setPrefWidth(130);

        Label lblDen = new Label("Đến ngày:");
        caiDatDinhDangNgay(dpDenNgay);
        dpDenNgay.setPrefWidth(130);

        // Nút chọn khoảng ngày nhanh
        Button btnHomNay = new Button("Hôm nay");
        btnHomNay.setStyle("-fx-font-size: 11px; -fx-padding: 3 8;");
        btnHomNay.setOnAction(e -> {
            dpTuNgay.setValue(LocalDate.now());
            dpDenNgay.setValue(LocalDate.now());
        });

        Button btn7Ngay = new Button("7 ngày qua");
        btn7Ngay.setStyle("-fx-font-size: 11px; -fx-padding: 3 8;");
        btn7Ngay.setOnAction(e -> {
            dpTuNgay.setValue(LocalDate.now().minusDays(7));
            dpDenNgay.setValue(LocalDate.now());
        });

        Button btnThangNay = new Button("Tháng này");
        btnThangNay.setStyle("-fx-font-size: 11px; -fx-padding: 3 8;");
        btnThangNay.setOnAction(e -> {
            dpTuNgay.setValue(LocalDate.now().withDayOfMonth(1));
            dpDenNgay.setValue(LocalDate.now());
        });

        Button btnTatCa = new Button("Tất cả ngày");
        btnTatCa.setStyle("-fx-font-size: 11px; -fx-padding: 3 8;");
        btnTatCa.setOnAction(e -> {
            dpTuNgay.setValue(null);
            dpDenNgay.setValue(null);
        });

        hang1.getChildren().addAll(txtTuKhoa, lblTu, dpTuNgay, lblDen, dpDenNgay, btnHomNay, btn7Ngay, btnThangNay, btnTatCa);

        // Hàng 2: Hình thức thanh toán + Trạng thái + Nút tìm kiếm & Làm mới
        HBox hang2 = new HBox(12);
        hang2.setAlignment(Pos.CENTER_LEFT);

        Label lblHt = new Label("Hình thức TT:");
        cboHinhThuc.getItems().addAll("Tất cả", "Tiền mặt", "Chuyển khoản");
        cboHinhThuc.setValue("Tất cả");
        cboHinhThuc.setPrefWidth(130);

        Label lblTt = new Label("Trạng thái:");
        cboTrangThai.getItems().addAll("Tất cả", "Đã thanh toán", "Đã hủy");
        cboTrangThai.setValue("Tất cả");
        cboTrangThai.setPrefWidth(130);

        Button btnTimKiem = new Button("🔍 Tìm kiếm");
        btnTimKiem.getStyleClass().add("nut-chinh");
        btnTimKiem.setOnAction(e -> napDanhSachHoaDon());

        Button btnLamMoi = new Button("🔄 Làm mới");
        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> {
            txtTuKhoa.clear();
            dpTuNgay.setValue(LocalDate.now().minusMonths(1));
            dpDenNgay.setValue(LocalDate.now());
            cboHinhThuc.setValue("Tất cả");
            cboTrangThai.setValue("Tất cả");
            napDanhSachHoaDon();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnXemChiTiet.getStyleClass().add("nut-phu");
        btnXemChiTiet.setDisable(true);
        btnXemChiTiet.setOnAction(e -> {
            HoaDon chon = bangHoaDon.getSelectionModel().getSelectedItem();
            if (chon != null) {
                moXemChiTiet(chon);
            }
        });

        hang2.getChildren().addAll(lblHt, cboHinhThuc, lblTt, cboTrangThai, btnTimKiem, btnLamMoi, spacer, btnXemChiTiet);

        pnl.getChildren().addAll(hang1, hang2);
        return pnl;
    }

    private void dangKySuKien() {
        // Tìm kiếm thời gian thực khi gõ từ khóa
        txtTuKhoa.textProperty().addListener((obs, cu, moi) -> napDanhSachHoaDon());
        txtTuKhoa.setOnAction(e -> napDanhSachHoaDon());

        // Lọc khi thay đổi bộ chọn
        cboHinhThuc.setOnAction(e -> napDanhSachHoaDon());
        cboTrangThai.setOnAction(e -> napDanhSachHoaDon());
        dpTuNgay.valueProperty().addListener((obs, cu, moi) -> napDanhSachHoaDon());
        dpDenNgay.valueProperty().addListener((obs, cu, moi) -> napDanhSachHoaDon());
    }

    private void taoBangDuLieu() {
        bangHoaDon.getStyleClass().add("bang-du-lieu");
        bangHoaDon.setItems(dsHoaDon);
        bangHoaDon.setPlaceholder(new Label("Không tìm thấy hóa đơn nào phù hợp với điều kiện tìm kiếm."));

        TableColumn<HoaDon, Integer> colStt = new TableColumn<>("STT");
        colStt.setPrefWidth(45);
        colStt.setStyle("-fx-alignment: center;");
        colStt.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });

        TableColumn<HoaDon, String> colMa = new TableColumn<>("Mã hóa đơn");
        colMa.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getMaHoaDon()));
        colMa.setPrefWidth(160);
        colMa.setStyle("-fx-alignment: center;");

        TableColumn<HoaDon, String> colNgay = new TableColumn<>("Ngày lập");
        colNgay.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getNgayLap() != null ? h.getValue().getNgayLap().format(DINH_DANG_NGAY) : ""));
        colNgay.setPrefWidth(100);
        colNgay.setStyle("-fx-alignment: center;");

        TableColumn<HoaDon, String> colKhach = new TableColumn<>("Khách hàng");
        colKhach.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getTenKhachHang() != null ? h.getValue().getTenKhachHang() : "Khách vãng lai"));
        colKhach.setPrefWidth(170);

        TableColumn<HoaDon, String> colSdt = new TableColumn<>("Số điện thoại");
        colSdt.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getSoDienThoaiKhachHang() != null ? h.getValue().getSoDienThoaiKhachHang() : "--"));
        colSdt.setPrefWidth(120);
        colSdt.setStyle("-fx-alignment: center;");

        TableColumn<HoaDon, String> colNv = new TableColumn<>("Thu ngân");
        colNv.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getTenNhanVien() != null ? h.getValue().getTenNhanVien() : h.getValue().getMaNhanVien()));
        colNv.setPrefWidth(150);

        TableColumn<HoaDon, String> colHt = new TableColumn<>("Hình thức TT");
        colHt.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getHinhThucThanhToan() != null ? h.getValue().getHinhThucThanhToan() : ""));
        colHt.setPrefWidth(110);
        colHt.setStyle("-fx-alignment: center;");

        TableColumn<HoaDon, String> colGiaGoc = new TableColumn<>("Giá gốc");
        colGiaGoc.setCellValueFactory(h -> new SimpleStringProperty(DINH_DANG_TIEN.format(h.getValue().getTongTienGoc())));
        colGiaGoc.setPrefWidth(120);
        colGiaGoc.setStyle("-fx-alignment: center-right; -fx-text-fill: #475569;");

        TableColumn<HoaDon, String> colGiamGia = new TableColumn<>("Ưu đãi / Giảm giá");
        colGiamGia.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getMoTaGiamGia()));
        colGiamGia.setPrefWidth(200);
        colGiamGia.setStyle("-fx-alignment: center;");

        TableColumn<HoaDon, String> colTongTien = new TableColumn<>("Tổng thanh toán");
        colTongTien.setCellValueFactory(h -> new SimpleStringProperty(DINH_DANG_TIEN.format(h.getValue().getTongTien())));
        colTongTien.setPrefWidth(130);
        colTongTien.setStyle("-fx-alignment: center-right;");

        TableColumn<HoaDon, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getTrangThaiThanhToan() != null ? h.getValue().getTrangThaiThanhToan() : ""));
        colTrangThai.setPrefWidth(120);
        colTrangThai.setStyle("-fx-alignment: center;");

        bangHoaDon.getColumns().addAll(colStt, colMa, colNgay, colKhach, colSdt, colNv, colHt, colGiaGoc, colGiamGia, colTongTien, colTrangThai);

        // Bật / tắt nút xem chi tiết khi chọn dòng
        bangHoaDon.getSelectionModel().selectedItemProperty().addListener((obs, cu, moi) -> {
            btnXemChiTiet.setDisable(moi == null);
        });

        // Double click xem chi tiết
        bangHoaDon.setRowFactory(tv -> {
            TableRow<HoaDon> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    moXemChiTiet(row.getItem());
                }
            });
            return row;
        });
    }

    private HBox taoThanhTongKet() {
        HBox pnl = new HBox(16);
        pnl.setAlignment(Pos.CENTER_LEFT);
        pnl.setPadding(new Insets(4, 0, 4, 0));

        // Chip 1: Tổng số đơn
        HBox chip1 = new HBox(6, new Label("Tổng số HĐ:"), lblTongSoHoaDon);
        chip1.setAlignment(Pos.CENTER_LEFT);
        lblTongSoHoaDon.setStyle("-fx-font-weight: bold;");

        // Chip 2: Doanh thu thực thu
        HBox chip2 = new HBox(6, new Label("Doanh thu thực thu:"), lblTongDoanhThu);
        chip2.setAlignment(Pos.CENTER_LEFT);
        lblTongDoanhThu.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        // Chip 3: Hóa đơn đã hủy
        HBox chip3 = new HBox(6, new Label("Đã hủy:"), lblTongDaHuy);
        chip3.setAlignment(Pos.CENTER_LEFT);
        lblTongDaHuy.setStyle("-fx-font-weight: bold;");

        pnl.getChildren().addAll(chip1, new Separator(javafx.geometry.Orientation.VERTICAL),
                chip2, new Separator(javafx.geometry.Orientation.VERTICAL), chip3);
        return pnl;
    }

    private void moXemChiTiet(HoaDon hd) {
        if (hd == null) return;
        try {
            HoaDon hdChiTiet = controller.layHoaDonTheoMa(hd.getMaHoaDon());
            if (hdChiTiet != null) {
                new XemChiTietHoaDonView(hdChiTiet, (Stage) getScene().getWindow(), this::napDanhSachHoaDon).showAndWait();
            }
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Lỗi khi tải chi tiết hóa đơn: " + ex.getMessage());
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    public void napDanhSachHoaDon() {
        String tuKhoa = txtTuKhoa.getText() != null ? txtTuKhoa.getText().trim() : "";
        LocalDate tuNgay = dpTuNgay.getValue();
        LocalDate denNgay = dpDenNgay.getValue();
        String hinhThuc = cboHinhThuc.getValue();
        String trangThai = cboTrangThai.getValue();

        if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
            // Không thực hiện query nếu khoảng ngày chưa hợp lệ
            return;
        }

        try {
            List<HoaDon> kq = controller.timKiemHoaDon(tuKhoa, tuNgay, denNgay, hinhThuc, trangThai);
            dsHoaDon.setAll(kq);

            long tongTien = 0;
            int soDonDaHuy = 0;
            for (HoaDon h : kq) {
                if ("Đã thanh toán".equalsIgnoreCase(h.getTrangThaiThanhToan())) {
                    if (h.getTongTien() != null) {
                        tongTien += h.getTongTien().longValue();
                    }
                } else if ("Đã hủy".equalsIgnoreCase(h.getTrangThaiThanhToan())) {
                    soDonDaHuy++;
                }
            }

            lblTongSoHoaDon.setText(kq.size() + " hóa đơn");
            lblTongDoanhThu.setText(DINH_DANG_TIEN.format(tongTien));
            lblTongDaHuy.setText(soDonDaHuy + " hóa đơn");
        } catch (Exception ex) {
            // Tránh văng popup liên tục khi đang gõ
            System.err.println("Lỗi tra cứu hóa đơn: " + ex.getMessage());
        }
    }

    private void caiDatDinhDangNgay(DatePicker dp) {
        dp.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(LocalDate date) {
                return date != null ? DINH_DANG_NGAY.format(date) : "";
            }
            @Override public LocalDate fromString(String string) {
                if (string != null && !string.trim().isEmpty()) {
                    try {
                        return LocalDate.parse(string.trim(), DINH_DANG_NGAY);
                    } catch (Exception ignored) {}
                }
                return null;
            }
        });
    }
}
