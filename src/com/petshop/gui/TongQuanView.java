package com.petshop.gui;

import com.petshop.bus.TongQuanBUS;
import com.petshop.bus.XacThucBUS;
import com.petshop.entity.TaiKhoan;
import com.petshop.entity.ThongKeTongQuan;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Màn hình Tổng quan (Dashboard) hiển thị các thẻ KPI tương tác và biểu đồ trực quan.
 * - Click thẻ "DOANH THU" -> Mở màn Thống kê doanh thu (BaoCaoDoanhThuHoaDonView).
 * - Click thẻ "LỊCH HẸN"  -> Mở màn Tìm kiếm lịch hẹn (lọc hôm nay).
 * - Click thẻ "TỒN KHO"   -> Mở màn Theo dõi tồn kho (lọc hàng sắp hết).
 * - Biểu đồ cột (BarChart): Thống kê doanh thu 7 ngày gần nhất.
 * - Biểu đồ tròn (PieChart): Cơ cấu doanh thu Bán hàng vs Dịch vụ Spa.
 */
public class TongQuanView extends VBox {

    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0");

    private final TongQuanBUS tongQuanService;
    private Runnable xuLyClickDoanhThu;
    private Runnable xuLyClickLichHen;
    private Runnable xuLyClickTonKho;

    public TongQuanView() {
        this(null, null, null);
    }

    public TongQuanView(Runnable xuLyClickDoanhThu, Runnable xuLyClickLichHen, Runnable xuLyClickTonKho) {
        this.tongQuanService = new TongQuanBUS();
        this.xuLyClickDoanhThu = xuLyClickDoanhThu;
        this.xuLyClickLichHen = xuLyClickLichHen;
        this.xuLyClickTonKho = xuLyClickTonKho;

        getStyleClass().add("khung-phan-he");
        setSpacing(18);
        setPadding(new Insets(20, 24, 20, 24));

        TaiKhoan tk = new XacThucBUS().getTaiKhoanHienTai();
        String hoTen = (tk != null && tk.getHoTenNhanVien() != null) ? tk.getHoTenNhanVien() : "Nhân viên";
        String tenVaiTro = (tk != null && tk.getPhanQuyen() != null) ? tk.getPhanQuyen().getTenHienThi() : "Quản trị viên";
        String ngayHomNay = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        ThongKeTongQuan thongKe = taiThongKeAnToan();

        // 1. Khung chào mừng
        HBox pnlChaoMung = new HBox(16);
        pnlChaoMung.setAlignment(Pos.CENTER_LEFT);
        pnlChaoMung.setPadding(new Insets(18, 22, 18, 22));
        pnlChaoMung.setStyle("-fx-background-color: white; -fx-background-radius: 10px; -fx-border-color: #eaecf0; -fx-border-radius: 10px;");

        VBox pnlThongTinChao = new VBox(5);
        Label lblChao = new Label("Xin chào, " + hoTen + "!");
        lblChao.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #101828;");
        Label lblPhuDe = new Label("Vai trò hiện tại: " + tenVaiTro + "  •  Ngày làm việc: " + ngayHomNay);
        lblPhuDe.setStyle("-fx-font-size: 13px; -fx-text-fill: #667085;");
        pnlThongTinChao.getChildren().addAll(lblChao, lblPhuDe);

        Region pnlKhoangTrong = new Region();
        HBox.setHgrow(pnlKhoangTrong, Priority.ALWAYS);

        Label lblTrangThai = new Label("Hệ thống sẵn sàng");
        lblTrangThai.setStyle("-fx-background-color: #ecfdf3; -fx-text-fill: #027a48; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 6 14 6 14; -fx-background-radius: 16px;");

        pnlChaoMung.getChildren().addAll(pnlThongTinChao, pnlKhoangTrong, lblTrangThai);

        // 2. Hàng 3 thẻ KPI tương tác (Click mở màn hình tương ứng)
        HBox pnlThongKe = new HBox(16);
        String chuoiDoanhThu = DINH_DANG_TIEN.format(thongKe.getDoanhThuHomNay()) + " ₫";
        String chuoiPhuDoanhThu = thongKe.getSoHoaDonHomNay() + " hóa đơn đã thanh toán (Nhấn để xem báo cáo)";
        String chuoiLichHen = thongKe.getTongSoLichHenHomNay() + " lịch";
        String chuoiPhuLichHen = thongKe.getSoLichHenChoXuLy() + " lịch đang chờ xử lý (Nhấn để tra cứu hôm nay)";
        String chuoiTonKho = thongKe.getSoSanPhamSapHet() + " mặt hàng";
        String chuoiPhuTonKho = "Dưới ngưỡng tồn tối thiểu (Nhấn để kiểm tra kho)";

        VBox theDoanhThu = taoTheChiSo(
                "DOANH THU HÔM NAY",
                chuoiDoanhThu,
                chuoiPhuDoanhThu,
                "#f97316",
                "Nhấn để mở màn hình Thống kê doanh thu hóa đơn",
                () -> kichHoatDieuHuongDoanhThu()
        );
        VBox theLichHen = taoTheChiSo(
                "LỊCH HẸN HÔM NAY",
                chuoiLichHen,
                chuoiPhuLichHen,
                "#2563eb",
                "Nhấn để mở màn hình Tìm kiếm lịch hẹn hôm nay",
                () -> kichHoatDieuHuongLichHen()
        );
        VBox theTonKho = taoTheChiSo(
                "SẢN PHẨM CẦN NHẬP THÊM",
                chuoiTonKho,
                chuoiPhuTonKho,
                "#dc2626",
                "Nhấn để mở màn hình Theo dõi tồn kho (lọc hàng sắp hết)",
                () -> kichHoatDieuHuongTonKho()
        );

        pnlThongKe.getChildren().addAll(theDoanhThu, theLichHen, theTonKho);

        // 3. Hàng Biểu đồ trực quan (BarChart 7 ngày & PieChart cơ cấu doanh thu)
        HBox pnlBieuDo = taoKhuVucBieuDo();
        VBox.setVgrow(pnlBieuDo, Priority.ALWAYS);

        VBox noiDungChinh = new VBox(16, pnlChaoMung, pnlThongKe, pnlBieuDo);
        ScrollPane scroll = new ScrollPane(noiDungChinh);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #f4f6f8; -fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().add(scroll);
    }

    private HBox taoKhuVucBieuDo() {
        HBox khungBieuDo = new HBox(16);
        khungBieuDo.setAlignment(Pos.CENTER);

        VBox cardBarChart = taoBieuDoDoanhThu7Ngay();
        VBox cardPieChart = taoBieuDoCoCauDoanhThu();

        HBox.setHgrow(cardBarChart, Priority.ALWAYS);
        HBox.setHgrow(cardPieChart, Priority.ALWAYS);
        cardBarChart.setMaxWidth(Double.MAX_VALUE);
        cardPieChart.setMaxWidth(Double.MAX_VALUE);

        khungBieuDo.getChildren().addAll(cardBarChart, cardPieChart);
        return khungBieuDo;
    }

    /**
     * Biểu đồ cột (BarChart): Thống kê doanh thu 7 ngày gần nhất theo đúng lịch.
     */
    private VBox taoBieuDoDoanhThu7Ngay() {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16, 18, 16, 18));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10px; -fx-border-color: #eaecf0; -fx-border-radius: 10px;");

        LocalDate homNay = LocalDate.now();
        LocalDate tuNgay = homNay.minusDays(6);
        DateTimeFormatter fmtDayDu = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        Label lblTieuDe = new Label("BIỂU ĐỒ DOANH THU 7 NGÀY GẦN NHẤT (" + tuNgay.format(fmtDayDu) + " - " + homNay.format(fmtDayDu) + ")");
        lblTieuDe.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold; -fx-text-fill: #101828;");

        CategoryAxis trucNgay = new CategoryAxis();
        trucNgay.setLabel("Ngày");
        trucNgay.setTickLabelFill(Color.web("#1e293b"));
        trucNgay.setTickLabelFont(Font.font("Segoe UI", FontWeight.BOLD, 11.5));
        trucNgay.setTickLabelRotation(0);
        trucNgay.setTickLabelGap(6);
        trucNgay.setTickMarkVisible(true);
        trucNgay.setTickLabelsVisible(true);
        trucNgay.setAnimated(false);

        NumberAxis trucTien = new NumberAxis();
        trucTien.setLabel("Doanh thu (VNĐ)");
        trucTien.setTickLabelFill(Color.web("#475569"));
        trucTien.setTickLabelFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
        trucTien.setTickMarkVisible(true);
        trucTien.setTickLabelsVisible(true);
        trucTien.setAnimated(false);
        trucTien.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                if (object == null) return "0";
                return DINH_DANG_TIEN.format(object.doubleValue());
            }

            @Override
            public Number fromString(String string) {
                return 0;
            }
        });

        BarChart<String, Number> barChart = new BarChart<>(trucNgay, trucTien);
        barChart.setLegendVisible(false);
        barChart.setAnimated(false);
        barChart.setCategoryGap(18);
        barChart.setBarGap(4);
        barChart.setPrefHeight(290);
        VBox.setVgrow(barChart, Priority.ALWAYS);

        XYChart.Series<String, Number> chuoiDuLieu = new XYChart.Series<>();
        chuoiDuLieu.setName("Doanh thu thuần");

        List<String> danhSachNhanNgay = new ArrayList<>();
        try {
            Map<String, Double> duLieu7Ngay = tongQuanService.layDoanhThu7NgayGanNhat();
            for (Map.Entry<String, Double> muc : duLieu7Ngay.entrySet()) {
                String nhanNgay = muc.getKey();
                double giaTri = muc.getValue() != null ? muc.getValue() : 0.0;
                danhSachNhanNgay.add(nhanNgay);

                XYChart.Data<String, Number> diemDuLieu = new XYChart.Data<>(nhanNgay, giaTri);
                diemDuLieu.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        Tooltip tp = new Tooltip("Ngày " + nhanNgay.replace("\n", " - ")
                                + "\nDoanh thu: " + DINH_DANG_TIEN.format(giaTri) + " ₫");
                        Tooltip.install(newNode, tp);
                    }
                });
                chuoiDuLieu.getData().add(diemDuLieu);
            }
        } catch (Exception ignored) {}

        if (!danhSachNhanNgay.isEmpty()) {
            trucNgay.setCategories(FXCollections.observableArrayList(danhSachNhanNgay));
        }
        barChart.getData().add(chuoiDuLieu);

        card.getChildren().addAll(lblTieuDe, barChart);
        return card;
    }

    /**
     * Biểu đồ tròn (PieChart): Cơ cấu doanh thu Bán hàng vs Dịch vụ Spa.
     */
    private VBox taoBieuDoCoCauDoanhThu() {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16, 18, 16, 18));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10px; -fx-border-color: #eaecf0; -fx-border-radius: 10px;");

        Label lblTieuDe = new Label("CƠ CẤU DOANH THU: BÁN HÀNG VS DỊCH VỤ SPA");
        lblTieuDe.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold; -fx-text-fill: #101828;");

        ObservableList<PieChart.Data> duLieuPie = FXCollections.observableArrayList();
        double dtBanHang = 0.0;
        double dtDichVu = 0.0;

        try {
            Map<String, Double> coCau = tongQuanService.layCoCauDoanhThuBanHangVaDichVu();
            dtBanHang = coCau.getOrDefault("Bán hàng", 0.0);
            dtDichVu = coCau.getOrDefault("Dịch vụ Spa", 0.0);
        } catch (Exception ignored) {}

        double tongCoCau = dtBanHang + dtDichVu;
        if (tongCoCau <= 0) {
            duLieuPie.add(new PieChart.Data("Bán hàng (0 ₫)", 1));
            duLieuPie.add(new PieChart.Data("Dịch vụ Spa (0 ₫)", 1));
        } else {
            double ptBanHang = (dtBanHang * 100.0) / tongCoCau;
            double ptDichVu = (dtDichVu * 100.0) / tongCoCau;
            duLieuPie.add(new PieChart.Data(
                    String.format("Bán hàng (%.1f%% - %s ₫)", ptBanHang, DINH_DANG_TIEN.format(dtBanHang)),
                    dtBanHang));
            duLieuPie.add(new PieChart.Data(
                    String.format("Dịch vụ Spa (%.1f%% - %s ₫)", ptDichVu, DINH_DANG_TIEN.format(dtDichVu)),
                    dtDichVu));
        }

        PieChart pieChart = new PieChart(duLieuPie);
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(true);
        pieChart.setAnimated(false);
        pieChart.setPrefHeight(270);
        VBox.setVgrow(pieChart, Priority.ALWAYS);

        card.getChildren().addAll(lblTieuDe, pieChart);
        return card;
    }

    private ThongKeTongQuan taiThongKeAnToan() {
        try {
            return tongQuanService.layThongKeHomNay();
        } catch (Exception e) {
            return new ThongKeTongQuan();
        }
    }

    private VBox taoTheChiSo(String tieuDe, String giaTri, String ghiChu, String mauNhan,
                             String goiY, Runnable suKienClick) {
        VBox the = new VBox(8);
        the.setPadding(new Insets(18, 20, 18, 20));
        String styleMacDinh = "-fx-background-color: white; -fx-background-radius: 10px; -fx-border-color: #eaecf0; -fx-border-radius: 10px; -fx-cursor: hand;";
        String styleHover = "-fx-background-color: #f8fafc; -fx-background-radius: 10px; -fx-border-color: " + mauNhan + "; -fx-border-width: 1.5px; -fx-border-radius: 10px; -fx-cursor: hand;";

        the.setStyle(styleMacDinh);
        the.setCursor(Cursor.HAND);
        HBox.setHgrow(the, Priority.ALWAYS);
        the.setMaxWidth(Double.MAX_VALUE);

        if (goiY != null && !goiY.isBlank()) {
            Tooltip.install(the, new Tooltip(goiY));
        }

        the.setOnMouseEntered(e -> the.setStyle(styleHover));
        the.setOnMouseExited(e -> the.setStyle(styleMacDinh));
        the.setOnMouseClicked(e -> {
            if (suKienClick != null) {
                suKienClick.run();
            }
        });

        Label lblTieuDe = new Label(tieuDe);
        lblTieuDe.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #667085;");

        Label lblGiaTri = new Label(giaTri);
        lblGiaTri.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + mauNhan + ";");

        Label lblGhiChu = new Label(ghiChu);
        lblGhiChu.setStyle("-fx-font-size: 12px; -fx-text-fill: #98a2b3;");

        the.getChildren().addAll(lblTieuDe, lblGiaTri, lblGhiChu);
        return the;
    }

    private void kichHoatDieuHuongDoanhThu() {
        if (xuLyClickDoanhThu != null) {
            xuLyClickDoanhThu.run();
            return;
        }
        ManHinhChinhView mhc = timManHinhChinh();
        if (mhc != null) {
            mhc.moManHinhThongKeDoanhThuTuDashboard();
        }
    }

    private void kichHoatDieuHuongLichHen() {
        if (xuLyClickLichHen != null) {
            xuLyClickLichHen.run();
            return;
        }
        ManHinhChinhView mhc = timManHinhChinh();
        if (mhc != null) {
            mhc.moManHinhTimKiemLichHenHomNayTuDashboard();
        }
    }

    private void kichHoatDieuHuongTonKho() {
        if (xuLyClickTonKho != null) {
            xuLyClickTonKho.run();
            return;
        }
        ManHinhChinhView mhc = timManHinhChinh();
        if (mhc != null) {
            mhc.moManHinhTheoDoiTonKhoSapHetTuDashboard();
        }
    }

    private ManHinhChinhView timManHinhChinh() {
        Node hienTai = this.getParent();
        while (hienTai != null) {
            if (hienTai instanceof ManHinhChinhView mhc) {
                return mhc;
            }
            hienTai = hienTai.getParent();
        }
        return null;
    }
}
