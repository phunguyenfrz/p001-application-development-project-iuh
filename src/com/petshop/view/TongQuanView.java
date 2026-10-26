package com.petshop.view;

import com.petshop.controller.TongQuanController;
import com.petshop.model.HoatDongGanDay;
import com.petshop.model.TaiKhoan;
import com.petshop.model.ThongKeTongQuan;
import com.petshop.util.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Màn hình Tổng quan Dashboard (TongQuanView).
 * Hiển thị số liệu thật từ CSDL SQL Server về doanh thu, lịch hẹn và cảnh báo tồn kho.
 * Tiền tố điều khiển: lbl, btn, tbl.
 */
public class TongQuanView extends ScrollPane {

    private final Label lblXinChao = new Label();
    private final Label lblMoTa = new Label("Hệ thống quản lý cửa hàng thú cưng Happy Tails theo mô hình Hướng Đối Tượng.");
    private final Label lblNgayHienTai = new Label();

    private final Label lblDoanhThuGiaTri = new Label("0 đ");
    private final Label lblLichHenGiaTri = new Label("0");
    private final Label lblTonKhoGiaTri = new Label("0");

    private final TableView<HoatDongGanDay> tblHoatDong = new TableView<>();
    private final Button btnLamMoi = new Button("🔄 Làm mới dữ liệu");

    private final TongQuanController tongQuanController = new TongQuanController();
    private final DecimalFormat dinhDangTien = new DecimalFormat("#,### đ");

    public TongQuanView() {
        setFitToWidth(true);
        setStyle("-fx-background-color: #f3f6f3; -fx-background: #f3f6f3; -fx-border-color: transparent;");

        VBox pnlNoiDung = new VBox(20);
        pnlNoiDung.getStyleClass().add("dashboard-content");
        pnlNoiDung.setPadding(new Insets(24, 28, 36, 28));

        // 1. Header lời chào và ngày làm việc
        HBox pnlHeader = taoPhanHeader();

        // 2. Các thẻ thống kê KPI
        HBox pnlTheThongKe = taoCacTheThongKe();

        // 3. Khối danh sách hoạt động / lịch hẹn hôm nay
        VBox pnlBangHoatDong = taoBangHoatDong();

        pnlNoiDung.getChildren().addAll(pnlHeader, pnlTheThongKe, pnlBangHoatDong);
        setContent(pnlNoiDung);

        // Tải dữ liệu ban đầu
        taiDuLieu();
    }

    private HBox taoPhanHeader() {
        HBox pnlHeader = new HBox(16);
        pnlHeader.setAlignment(Pos.CENTER_LEFT);

        TaiKhoan tk = SessionManager.getTaiKhoanHienTai();
        String tenNhanVien = (tk != null && tk.getHoTenNhanVien() != null) ? tk.getHoTenNhanVien() : "Quản trị viên";
        lblXinChao.setText("Xin chào, " + tenNhanVien);
        lblXinChao.getStyleClass().add("welcome-title");

        lblMoTa.getStyleClass().add("welcome-sub");

        VBox pnlLoiChao = new VBox(4, lblXinChao, lblMoTa);

        Region pnlKhoangCach = new Region();
        HBox.setHgrow(pnlKhoangCach, Priority.ALWAYS);

        String ngayChuoi = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        lblNgayHienTai.setText("📅 " + ngayChuoi);
        lblNgayHienTai.getStyleClass().add("date-badge");

        btnLamMoi.getStyleClass().add("btn-action-secondary");
        btnLamMoi.setOnAction(e -> taiDuLieu());

        pnlHeader.getChildren().addAll(pnlLoiChao, pnlKhoangCach, btnLamMoi, lblNgayHienTai);
        return pnlHeader;
    }

    private HBox taoCacTheThongKe() {
        HBox pnlCacThe = new HBox(16);

        // Thẻ 1: Doanh thu hôm nay
        VBox pnlTheDoanhThu = new VBox(6);
        pnlTheDoanhThu.getStyleClass().add("stat-card-featured");
        HBox.setHgrow(pnlTheDoanhThu, Priority.ALWAYS);

        Label lblDoanhThuTieuDe = new Label("📈  Doanh thu hôm nay");
        lblDoanhThuTieuDe.getStyleClass().add("stat-title-white");

        lblDoanhThuGiaTri.getStyleClass().add("stat-value-white");

        Label lblDoanhThuMoTa = new Label("Tổng thanh toán trong ngày");
        lblDoanhThuMoTa.getStyleClass().add("stat-sub-white");

        pnlTheDoanhThu.getChildren().addAll(lblDoanhThuTieuDe, lblDoanhThuGiaTri, lblDoanhThuMoTa);

        // Thẻ 2: Lịch hẹn hôm nay
        VBox pnlTheLichHen = new VBox(6);
        pnlTheLichHen.getStyleClass().add("stat-card");
        HBox.setHgrow(pnlTheLichHen, Priority.ALWAYS);

        Label lblLichHenTieuDe = new Label("📅  Lịch hẹn hôm nay");
        lblLichHenTieuDe.getStyleClass().add("stat-title");

        lblLichHenGiaTri.getStyleClass().add("stat-value");

        Label lblLichHenMoTa = new Label("Lịch hẹn dịch vụ chăm sóc");
        lblLichHenMoTa.getStyleClass().add("stat-sub");

        pnlTheLichHen.getChildren().addAll(lblLichHenTieuDe, lblLichHenGiaTri, lblLichHenMoTa);

        // Thẻ 3: Sản phẩm sắp hết hàng
        VBox pnlTheTonKho = new VBox(6);
        pnlTheTonKho.getStyleClass().add("stat-card");
        HBox.setHgrow(pnlTheTonKho, Priority.ALWAYS);

        Label lblTonKhoTieuDe = new Label("⚠️  Sản phẩm sắp hết hàng");
        lblTonKhoTieuDe.getStyleClass().add("stat-title");

        lblTonKhoGiaTri.getStyleClass().add("stat-value");

        Label lblTonKhoMoTa = new Label("Tồn kho dưới ngưỡng an toàn");
        lblTonKhoMoTa.getStyleClass().add("stat-sub");

        pnlTheTonKho.getChildren().addAll(lblTonKhoTieuDe, lblTonKhoGiaTri, lblTonKhoMoTa);

        pnlCacThe.getChildren().addAll(pnlTheDoanhThu, pnlTheLichHen, pnlTheTonKho);
        return pnlCacThe;
    }

    private VBox taoBangHoatDong() {
        VBox pnlKhungBang = new VBox(12);
        pnlKhungBang.getStyleClass().add("card-panel");
        pnlKhungBang.setPadding(new Insets(18, 20, 20, 20));

        Label lblTieuDeBang = new Label("Lịch hẹn & Hoạt động trong ngày");
        lblTieuDeBang.getStyleClass().add("card-title");

        Label lblMoTaBang = new Label("Danh sách các lịch hẹn cần phục vụ trong ngày làm việc hiện tại.");
        lblMoTaBang.getStyleClass().add("card-sub");

        TableColumn<HoatDongGanDay, String> colThoiGian = new TableColumn<>("Thời gian");
        colThoiGian.setCellValueFactory(new PropertyValueFactory<>("thoiGian"));
        colThoiGian.setPrefWidth(120);

        TableColumn<HoatDongGanDay, String> colTieuDe = new TableColumn<>("Nội dung công việc");
        colTieuDe.setCellValueFactory(new PropertyValueFactory<>("tieuDe"));
        colTieuDe.setPrefWidth(420);

        TableColumn<HoatDongGanDay, String> colMoTa = new TableColumn<>("Chi tiết / Trạng thái");
        colMoTa.setCellValueFactory(new PropertyValueFactory<>("moTa"));
        colMoTa.setPrefWidth(260);

        tblHoatDong.getColumns().setAll(List.of(colThoiGian, colTieuDe, colMoTa));
        tblHoatDong.setPlaceholder(new Label("Chưa có lịch hẹn nào trong ngày hôm nay."));
        tblHoatDong.setPrefHeight(260);

        pnlKhungBang.getChildren().addAll(lblTieuDeBang, lblMoTaBang, tblHoatDong);
        return pnlKhungBang;
    }

    /**
     * Nạp dữ liệu thống kê từ Controller và cập nhật lên giao diện.
     */
    public void taiDuLieu() {
        try {
            ThongKeTongQuan thongKe = tongQuanController.layThongKeTongQuan();
            if (thongKe != null) {
                lblDoanhThuGiaTri.setText(dinhDangTien.format(thongKe.getDoanhThuHomNay()));
                lblLichHenGiaTri.setText(String.valueOf(thongKe.getSoLichHenHomNay()));
                lblTonKhoGiaTri.setText(String.valueOf(thongKe.getSoSanPhamSapHet()));
            }

            List<HoatDongGanDay> dsHoatDong = tongQuanController.layHoatDongGanDay();
            tblHoatDong.getItems().setAll(dsHoatDong);

        } catch (IllegalStateException ex) {
            hienThiCanhBao("Lỗi tải dữ liệu", "Không thể lấy số liệu thống kê: " + ex.getMessage());
        } catch (Exception ex) {
            hienThiCanhBao("Lỗi", "Đã xảy ra lỗi khi tải dữ liệu: " + ex.getMessage());
        }
    }

    private void hienThiCanhBao(String tieuDe, String noiDung) {
        Alert thongBao = new Alert(Alert.AlertType.WARNING);
        thongBao.setTitle(tieuDe);
        thongBao.setHeaderText(null);
        thongBao.setContentText(noiDung);
        thongBao.showAndWait();
    }
}

