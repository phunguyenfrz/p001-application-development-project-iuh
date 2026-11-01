package com.petshop.view;

import com.petshop.controller.TongQuanController;
import com.petshop.model.HoatDongGanDay;
import com.petshop.model.TaiKhoan;
import com.petshop.model.ThongKeTongQuan;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Màn hình Tổng quan Dashboard (TongQuanView).
 * Sử dụng bộ màu mặc định chuẩn chung.css đồng bộ với toàn hệ thống.
 * Bảng Lịch hẹn & Hoạt động trong ngày được cố định cột (không kéo ra vô) và tự động dàn đều 100% chiều rộng màn hình.
 */
public class TongQuanView extends ScrollPane {

    private final Label lblXinChao = new Label();
    private final Label lblMoTa = new Label("Hệ thống quản lý cửa hàng thú cưng Happy Tails theo mô hình Hướng Đối Tượng.");
    private final Label lblNgayHienTai = new Label();

    private final Label lblDoanhThuGiaTri = new Label("0 đ");
    private final Label lblLichHenGiaTri = new Label("0");
    private final Label lblTonKhoGiaTri = new Label("0");

    private final TableView<HoatDongGanDay> tblHoatDong = new TableView<>();
    private final Button btnLamMoi = new Button("Làm mới dữ liệu");

    private final TongQuanController tongQuanController = new TongQuanController();
    private final DecimalFormat dinhDangTien = new DecimalFormat("#,##0 đ");

    public TongQuanView() {
        setFitToWidth(true);
        setFitToHeight(true);
        setStyle("-fx-background-color: #f4f6f8; -fx-background: #f4f6f8; -fx-border-color: transparent;");

        VBox pnlNoiDung = new VBox(16);
        pnlNoiDung.getStyleClass().add("khung-phan-he");

        // 1. Header lời chào và ngày làm việc
        HBox pnlHeader = taoPhanHeader();

        // 2. Các thẻ thống kê KPI (Màu mặc định chuẩn chung.css)
        HBox pnlTheThongKe = taoCacTheThongKe();

        // 3. Khối danh sách hoạt động / lịch hẹn hôm nay
        VBox pnlBangHoatDong = taoBangHoatDong();
        VBox.setVgrow(pnlBangHoatDong, Priority.ALWAYS);

        pnlNoiDung.getChildren().addAll(pnlHeader, pnlTheThongKe, pnlBangHoatDong);
        setContent(pnlNoiDung);

        // Tải dữ liệu ban đầu
        taiDuLieu();
    }

    private HBox taoPhanHeader() {
        HBox pnlHeader = new HBox(16);
        pnlHeader.setAlignment(Pos.CENTER_LEFT);

        TaiKhoan tk = tongQuanController.layTaiKhoanHienTai();
        String tenNhanVien = (tk != null && tk.getHoTenNhanVien() != null) ? tk.getHoTenNhanVien() : "Quản trị hệ thống";
        lblXinChao.setText("Xin chào, " + tenNhanVien);
        lblXinChao.getStyleClass().add("tieu-de-phan-he");

        lblMoTa.getStyleClass().add("mo-ta-phan-he");

        VBox pnlLoiChao = new VBox(4, lblXinChao, lblMoTa);

        Region pnlKhoangCach = new Region();
        HBox.setHgrow(pnlKhoangCach, Priority.ALWAYS);

        String ngayChuoi = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        lblNgayHienTai.setText("Ngày: " + ngayChuoi);
        lblNgayHienTai.getStyleClass().add("hop-noi-dung");
        lblNgayHienTai.setStyle("-fx-padding: 6 12; -fx-font-weight: bold;");

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> taiDuLieu());

        pnlHeader.getChildren().addAll(pnlLoiChao, pnlKhoangCach, btnLamMoi, lblNgayHienTai);
        return pnlHeader;
    }

    private HBox taoCacTheThongKe() {
        HBox pnlCacThe = new HBox(16);

        // Thẻ 1: Doanh thu hôm nay
        VBox pnlTheDoanhThu = taoTheKpiMacDinh(
                "DOANH THU HÔM NAY",
                lblDoanhThuGiaTri,
                "Tổng thanh toán thực tế trong ngày (đã trừ hoàn đổi trả)"
        );

        // Thẻ 2: Lịch hẹn hôm nay
        VBox pnlTheLichHen = taoTheKpiMacDinh(
                "LỊCH HẸN HÔM NAY",
                lblLichHenGiaTri,
                "Lịch hẹn dịch vụ chăm sóc"
        );

        // Thẻ 3: Sản phẩm cần nhập thêm
        VBox pnlTheTonKho = taoTheKpiMacDinh(
                "SẢN PHẨM CẦN NHẬP THÊM",
                lblTonKhoGiaTri,
                "Hết hàng hoặc tồn dưới ngưỡng an toàn"
        );

        pnlCacThe.getChildren().addAll(pnlTheDoanhThu, pnlTheLichHen, pnlTheTonKho);
        return pnlCacThe;
    }

    private VBox taoTheKpiMacDinh(String tieuDe, Label lblGiaTri, String moTa) {
        VBox card = new VBox(6);
        card.getStyleClass().add("hop-noi-dung");
        card.setPadding(new Insets(16));
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setMaxWidth(Double.MAX_VALUE);

        Label lblTieuDe = new Label(tieuDe);
        lblTieuDe.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #64748b;");

        lblGiaTri.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label lblMoTa = new Label(moTa);
        lblMoTa.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #64748b;");

        card.getChildren().addAll(lblTieuDe, lblGiaTri, lblMoTa);
        return card;
    }

    private VBox taoBangHoatDong() {
        VBox pnlKhungBang = new VBox(10);
        pnlKhungBang.getStyleClass().add("hop-noi-dung");
        pnlKhungBang.setPadding(new Insets(16, 18, 18, 18));

        Label lblTieuDeBang = new Label("Lịch hẹn & Hoạt động trong ngày");
        lblTieuDeBang.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        Label lblMoTaBang = new Label("Tổng hợp toàn bộ các lịch hẹn dịch vụ, hóa đơn thanh toán và phiếu đổi trả phát sinh trong ngày.");
        lblMoTaBang.getStyleClass().add("mo-ta-phan-he");

        tblHoatDong.getStyleClass().add("bang-du-lieu");
        tblHoatDong.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(tblHoatDong, Priority.ALWAYS);

        // Tính chiều rộng khả dụng của bảng để chia đều 100% full màn hình và cố định không cho kéo cột
        DoubleBinding chieuRongKhaDung = tblHoatDong.widthProperty().subtract(18);

        // Cột 1: Thời gian (12%)
        TableColumn<HoatDongGanDay, String> colThoiGian = new TableColumn<>("Thời gian");
        colThoiGian.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getThoiGian()));
        colThoiGian.setStyle("-fx-alignment: center;");
        colThoiGian.setResizable(false);
        colThoiGian.setReorderable(false);
        colThoiGian.prefWidthProperty().bind(chieuRongKhaDung.multiply(0.12));

        // Cột 2: Phân hệ / Hoạt động (18%)
        TableColumn<HoatDongGanDay, String> colLoai = new TableColumn<>("Phân hệ / Hoạt động");
        colLoai.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getLoaiHoatDong()));
        colLoai.setStyle("-fx-alignment: center; -fx-font-weight: bold;");
        colLoai.setResizable(false);
        colLoai.setReorderable(false);
        colLoai.prefWidthProperty().bind(chieuRongKhaDung.multiply(0.18));

        // Cột 3: Khách hàng / Thú cưng (22%)
        TableColumn<HoatDongGanDay, String> colDoiTuong = new TableColumn<>("Khách hàng / Thú cưng");
        colDoiTuong.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDoiTuong()));
        colDoiTuong.setResizable(false);
        colDoiTuong.setReorderable(false);
        colDoiTuong.prefWidthProperty().bind(chieuRongKhaDung.multiply(0.22));

        // Cột 4: Chi tiết giao dịch (33%)
        TableColumn<HoatDongGanDay, String> colChiTiet = new TableColumn<>("Chi tiết giao dịch");
        colChiTiet.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getChiTiet()));
        colChiTiet.setResizable(false);
        colChiTiet.setReorderable(false);
        colChiTiet.prefWidthProperty().bind(chieuRongKhaDung.multiply(0.33));

        // Cột 5: Trạng thái (15%)
        TableColumn<HoatDongGanDay, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTrangThai()));
        colTrangThai.setStyle("-fx-alignment: center; -fx-font-weight: bold;");
        colTrangThai.setResizable(false);
        colTrangThai.setReorderable(false);
        colTrangThai.prefWidthProperty().bind(chieuRongKhaDung.multiply(0.15));

        tblHoatDong.getColumns().setAll(List.of(colThoiGian, colLoai, colDoiTuong, colChiTiet, colTrangThai));
        tblHoatDong.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        tblHoatDong.setPlaceholder(new Label("Chưa có phát sinh hoạt động nào trong ngày hôm nay."));
        tblHoatDong.setPrefHeight(440);

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

        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienThiCanhBao("Lỗi tải dữ liệu", "Không thể lấy số liệu thống kê: " + ex.getMessage());
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
