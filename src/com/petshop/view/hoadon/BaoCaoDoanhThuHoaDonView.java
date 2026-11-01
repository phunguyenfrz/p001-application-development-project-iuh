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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Giao diện Báo cáo thống kê doanh thu bán hàng & dịch vụ (HD_DOANH_THU / INV_REVENUE).
 */
public class BaoCaoDoanhThuHoaDonView extends VBox {

    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 VNĐ");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final HoaDonController controller;

    private final DatePicker dpTuNgay = new DatePicker(LocalDate.now().withDayOfMonth(1));
    private final DatePicker dpDenNgay = new DatePicker(LocalDate.now());

    // Thẻ chỉ số
    private final Label lblTongDoanhThu = new Label("0 VNĐ");
    private final Label lblTienMat = new Label("0 VNĐ");
    private final Label lblChuyenKhoan = new Label("0 VNĐ");
    private final Label lblPos = new Label("0 VNĐ");
    private final Label lblSoHoaDon = new Label("0");
    private final Label lblSoHuy = new Label("0");

    private final ObservableList<HoaDon> dsHoaDon = FXCollections.observableArrayList();
    private final TableView<HoaDon> bangHoaDon = new TableView<>();

    public BaoCaoDoanhThuHoaDonView() {
        this.controller = new HoaDonController();

        setSpacing(12);
        setPadding(new Insets(16));
        getStyleClass().add("khung-phan-he");

        khoiTaoGiaoDien();
        taiBaoCao();
    }

    private void khoiTaoGiaoDien() {
        // Tiêu đề
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("BÁO CÁO THỐNG KÊ DOANH THU");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Tổng hợp doanh thu từ hoạt động bán lẻ sản phẩm và cung cấp dịch vụ chăm sóc thú cưng.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // Bộ lọc thời gian
        HBox pnlBoLoc = new HBox(10);
        pnlBoLoc.setPadding(new Insets(10, 12, 10, 12));
        pnlBoLoc.getStyleClass().add("hop-noi-dung");
        pnlBoLoc.setAlignment(Pos.CENTER_LEFT);

        dpTuNgay.setPrefWidth(140);
        dpDenNgay.setPrefWidth(140);

        Button btnXemBaoCao = new Button("📊 Xem báo cáo");
        btnXemBaoCao.getStyleClass().add("nut-chinh");
        btnXemBaoCao.setOnAction(e -> taiBaoCao());

        pnlBoLoc.getChildren().addAll(new Label("Từ ngày:"), dpTuNgay, new Label("Đến ngày:"), dpDenNgay, btnXemBaoCao);

        // Thẻ chỉ số tổng hợp
        HBox pnlTheChiSo = taoTheChiSo();

        // Bảng dữ liệu chi tiết
        taoBangDuLieu();
        VBox.setVgrow(bangHoaDon, Priority.ALWAYS);

        getChildren().addAll(pnlTieuDe, pnlBoLoc, pnlTheChiSo, bangHoaDon);
    }

    private HBox taoTheChiSo() {
        HBox pnl = new HBox(12);
        pnl.setAlignment(Pos.CENTER);

        VBox the1 = taoThe("TỔNG DOANH THU", lblTongDoanhThu, "#0284c7");
        VBox the2 = taoThe("TIỀN MẶT", lblTienMat, "#16a34a");
        VBox the3 = taoThe("CHUYỂN KHOẢN", lblChuyenKhoan, "#0d9488");
        VBox the5 = taoThe("SỐ HÓA ĐƠN", lblSoHoaDon, "#334155");
        VBox the6 = taoThe("HÓA ĐƠN HỦY", lblSoHuy, "#dc2626");

        HBox.setHgrow(the1, Priority.ALWAYS);
        HBox.setHgrow(the2, Priority.ALWAYS);
        HBox.setHgrow(the3, Priority.ALWAYS);
        HBox.setHgrow(the5, Priority.ALWAYS);
        HBox.setHgrow(the6, Priority.ALWAYS);

        pnl.getChildren().addAll(the1, the2, the3, the5, the6);
        return pnl;
    }

    private VBox taoThe(String tieuDe, Label lblGiaTri, String mauSac) {
        VBox the = new VBox(6);
        the.setPadding(new Insets(12));
        the.getStyleClass().add("hop-noi-dung");
        the.setAlignment(Pos.CENTER);

        Label lbl = new Label(tieuDe);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        lblGiaTri.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + mauSac + ";");

        the.getChildren().addAll(lbl, lblGiaTri);
        return the;
    }

    private void taoBangDuLieu() {
        bangHoaDon.getStyleClass().add("bang-du-lieu");
        bangHoaDon.setItems(dsHoaDon);

        TableColumn<HoaDon, String> colMa = new TableColumn<>("Mã hóa đơn");
        colMa.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getMaHoaDon()));
        colMa.setPrefWidth(160);

        TableColumn<HoaDon, String> colNgay = new TableColumn<>("Ngày lập");
        colNgay.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getNgayLap() != null ? h.getValue().getNgayLap().format(DINH_DANG_NGAY) : ""));
        colNgay.setPrefWidth(100);
        colNgay.setStyle("-fx-alignment: center;");

        TableColumn<HoaDon, String> colKhach = new TableColumn<>("Khách hàng");
        colKhach.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getTenKhachHang() != null ? h.getValue().getTenKhachHang() : "Khách vãng lai"));
        colKhach.setPrefWidth(180);

        TableColumn<HoaDon, String> colNv = new TableColumn<>("Thu ngân");
        colNv.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getTenNhanVien() != null ? h.getValue().getTenNhanVien() : h.getValue().getMaNhanVien()));
        colNv.setPrefWidth(150);

        TableColumn<HoaDon, String> colHt = new TableColumn<>("Hình thức TT");
        colHt.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getHinhThucThanhToan()));
        colHt.setPrefWidth(120);
        colHt.setStyle("-fx-alignment: center;");

        TableColumn<HoaDon, String> colTongTien = new TableColumn<>("Doanh thu");
        colTongTien.setCellValueFactory(h -> new SimpleStringProperty(DINH_DANG_TIEN.format(h.getValue().getTongTien())));
        colTongTien.setPrefWidth(130);
        colTongTien.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");

        TableColumn<HoaDon, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setCellValueFactory(h -> new SimpleStringProperty(h.getValue().getTrangThaiThanhToan()));
        colTrangThai.setPrefWidth(120);
        colTrangThai.setStyle("-fx-alignment: center;");

        bangHoaDon.getColumns().addAll(colMa, colNgay, colKhach, colNv, colHt, colTongTien, colTrangThai);
    }

    private void taiBaoCao() {
        LocalDate tuNgay = dpTuNgay.getValue();
        LocalDate denNgay = dpDenNgay.getValue();

        try {
            Map<String, Object> baoCao = controller.layBaoCaoDoanhThu(tuNgay, denNgay);

            BigDecimal tongDt = (BigDecimal) baoCao.getOrDefault("tongDoanhThu", BigDecimal.ZERO);
            BigDecimal dtTm = (BigDecimal) baoCao.getOrDefault("doanhThuTienMat", BigDecimal.ZERO);
            BigDecimal dtCk = (BigDecimal) baoCao.getOrDefault("doanhThuChuyenKhoan", BigDecimal.ZERO);
            BigDecimal dtPos = (BigDecimal) baoCao.getOrDefault("doanhThuPos", BigDecimal.ZERO);
            int soHd = (int) baoCao.getOrDefault("tongSoHoaDon", 0);
            int soHuy = (int) baoCao.getOrDefault("soHoaDonHuy", 0);

            lblTongDoanhThu.setText(DINH_DANG_TIEN.format(tongDt));
            lblTienMat.setText(DINH_DANG_TIEN.format(dtTm));
            lblChuyenKhoan.setText(DINH_DANG_TIEN.format(dtCk));
            lblPos.setText(DINH_DANG_TIEN.format(dtPos));
            lblSoHoaDon.setText(String.valueOf(soHd));
            lblSoHuy.setText(String.valueOf(soHuy));

            List<HoaDon> ds = controller.timKiemHoaDon("", tuNgay, denNgay, "Tất cả", "Tất cả");
            dsHoaDon.setAll(ds);
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Lỗi khi tải báo cáo doanh thu: " + ex.getMessage());
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }
}

