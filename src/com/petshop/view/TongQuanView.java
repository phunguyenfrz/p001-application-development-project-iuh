package com.petshop.view;

import com.petshop.controller.TongQuanController;
import com.petshop.model.HoatDongGanDay;
import com.petshop.model.TaiKhoan;
import com.petshop.model.ThongKeTongQuan;
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
 * Hiển thị số liệu thật từ CSDL SQL Server về doanh thu, lịch hẹn và cảnh báo tồn kho.
 * Bảng Hoạt động trong ngày được cố định cột (không co giãn kéo ra kéo vô) và chia tỉ lệ đều đặn.
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
    private final DecimalFormat dinhDangTien = new DecimalFormat("#,##0 đ");

    public TongQuanView() {
        setFitToWidth(true);
        setStyle("-fx-background-color: #f8fafc; -fx-background: #f8fafc; -fx-border-color: transparent;");

        VBox pnlNoiDung = new VBox(20);
        pnlNoiDung.getStyleClass().add("noi-dung-tong-quan");
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

        TaiKhoan tk = tongQuanController.layTaiKhoanHienTai();
        String tenNhanVien = (tk != null && tk.getHoTenNhanVien() != null) ? tk.getHoTenNhanVien() : "Quản trị hệ thống";
        lblXinChao.setText("Xin chào, " + tenNhanVien);
        lblXinChao.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        lblMoTa.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        VBox pnlLoiChao = new VBox(4, lblXinChao, lblMoTa);

        Region pnlKhoangCach = new Region();
        HBox.setHgrow(pnlKhoangCach, Priority.ALWAYS);

        String ngayChuoi = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        lblNgayHienTai.setText("📅 " + ngayChuoi);
        lblNgayHienTai.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-padding: 6 12; -fx-font-weight: bold; -fx-text-fill: #334155;");

        btnLamMoi.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-padding: 6 14; -fx-font-weight: bold; -fx-text-fill: #334155; -fx-cursor: hand;");
        btnLamMoi.setOnAction(e -> taiDuLieu());

        pnlHeader.getChildren().addAll(pnlLoiChao, pnlKhoangCach, btnLamMoi, lblNgayHienTai);
        return pnlHeader;
    }

    private HBox taoCacTheThongKe() {
        HBox pnlCacThe = new HBox(16);

        // Thẻ 1: Doanh thu hôm nay (Nổi bật)
        VBox pnlTheDoanhThu = new VBox(6);
        pnlTheDoanhThu.setStyle("-fx-background-color: #0284c7; -fx-background-radius: 8px; -fx-padding: 16;");
        HBox.setHgrow(pnlTheDoanhThu, Priority.ALWAYS);

        Label lblDoanhThuTieuDe = new Label("📈  Doanh thu hôm nay");
        lblDoanhThuTieuDe.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #e0f2fe;");

        lblDoanhThuGiaTri.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label lblDoanhThuMoTa = new Label("Tổng thanh toán thực tế trong ngày");
        lblDoanhThuMoTa.setStyle("-fx-font-size: 11px; -fx-text-fill: #bae6fd;");

        pnlTheDoanhThu.getChildren().addAll(lblDoanhThuTieuDe, lblDoanhThuGiaTri, lblDoanhThuMoTa);

        // Thẻ 2: Lịch hẹn hôm nay
        VBox pnlTheLichHen = new VBox(6);
        pnlTheLichHen.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-padding: 16;");
        HBox.setHgrow(pnlTheLichHen, Priority.ALWAYS);

        Label lblLichHenTieuDe = new Label("📅  Lịch hẹn hôm nay");
        lblLichHenTieuDe.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #64748b;");

        lblLichHenGiaTri.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label lblLichHenMoTa = new Label("Lịch hẹn dịch vụ chăm sóc");
        lblLichHenMoTa.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        pnlTheLichHen.getChildren().addAll(lblLichHenTieuDe, lblLichHenGiaTri, lblLichHenMoTa);

        // Thẻ 3: Sản phẩm hết hàng hoặc cần nhập thêm
        VBox pnlTheTonKho = new VBox(6);
        pnlTheTonKho.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-padding: 16;");
        HBox.setHgrow(pnlTheTonKho, Priority.ALWAYS);

        Label lblTonKhoTieuDe = new Label("⚠️  Sản phẩm cần nhập thêm");
        lblTonKhoTieuDe.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #64748b;");

        lblTonKhoGiaTri.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label lblTonKhoMoTa = new Label("Hết hàng hoặc tồn dưới ngưỡng an toàn");
        lblTonKhoMoTa.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        pnlTheTonKho.getChildren().addAll(lblTonKhoTieuDe, lblTonKhoGiaTri, lblTonKhoMoTa);

        pnlCacThe.getChildren().addAll(pnlTheDoanhThu, pnlTheLichHen, pnlTheTonKho);
        return pnlCacThe;
    }

    private VBox taoBangHoatDong() {
        VBox pnlKhungBang = new VBox(12);
        pnlKhungBang.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px;");
        pnlKhungBang.setPadding(new Insets(18, 20, 20, 20));

        Label lblTieuDeBang = new Label("Lịch hẹn & Hoạt động trong ngày");
        lblTieuDeBang.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label lblMoTaBang = new Label("Tổng hợp toàn bộ các lịch hẹn dịch vụ, hóa đơn thanh toán và phiếu đổi trả phát sinh trong ngày.");
        lblMoTaBang.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        // Cột 1: Thời gian (Fix cứng không kéo ra vô)
        TableColumn<HoatDongGanDay, String> colThoiGian = new TableColumn<>("Thời gian");
        colThoiGian.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getThoiGian()));
        colThoiGian.setStyle("-fx-alignment: center;");
        colThoiGian.setResizable(false);
        colThoiGian.setMinWidth(110);
        colThoiGian.setMaxWidth(130);

        // Cột 2: Loại hoạt động (Fix cứng không kéo ra vô)
        TableColumn<HoatDongGanDay, String> colLoai = new TableColumn<>("Phân hệ / Hoạt động");
        colLoai.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getLoaiHoatDong()));
        colLoai.setStyle("-fx-alignment: center; -fx-font-weight: bold;");
        colLoai.setResizable(false);
        colLoai.setMinWidth(170);
        colLoai.setMaxWidth(190);
        colLoai.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    if (item.contains("Lịch hẹn")) {
                        setStyle("-fx-text-fill: #0284c7; -fx-font-weight: bold; -fx-alignment: center;");
                    } else if (item.contains("Hóa đơn")) {
                        setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold; -fx-alignment: center;");
                    } else if (item.contains("đổi trả")) {
                        setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: bold; -fx-alignment: center;");
                    } else {
                        setStyle("-fx-text-fill: #334155; -fx-alignment: center;");
                    }
                }
            }
        });

        // Cột 3: Khách hàng / Thú cưng (Fix cứng)
        TableColumn<HoatDongGanDay, String> colDoiTuong = new TableColumn<>("Khách hàng / Thú cưng");
        colDoiTuong.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDoiTuong()));
        colDoiTuong.setResizable(false);
        colDoiTuong.setMinWidth(200);

        // Cột 4: Chi tiết nghiệp vụ (Tự động lấp đầy phần còn lại)
        TableColumn<HoatDongGanDay, String> colChiTiet = new TableColumn<>("Chi tiết giao dịch");
        colChiTiet.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getChiTiet()));
        colChiTiet.setResizable(false);
        colChiTiet.setMinWidth(280);

        // Cột 5: Trạng thái (Fix cứng không kéo ra vô)
        TableColumn<HoatDongGanDay, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTrangThai()));
        colTrangThai.setStyle("-fx-alignment: center; -fx-font-weight: bold;");
        colTrangThai.setResizable(false);
        colTrangThai.setMinWidth(130);
        colTrangThai.setMaxWidth(160);
        colTrangThai.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    if (item.contains("Đã thanh toán") || item.contains("Đã duyệt") || item.contains("Hoàn thành")) {
                        setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold; -fx-alignment: center;");
                    } else if (item.contains("Đang chờ") || item.contains("Chờ xử lý")) {
                        setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold; -fx-alignment: center;");
                    } else if (item.contains("hủy") || item.contains("Từ chối")) {
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-alignment: center;");
                    } else {
                        setStyle("-fx-text-fill: #334155; -fx-alignment: center;");
                    }
                }
            }
        });

        tblHoatDong.getColumns().setAll(List.of(colThoiGian, colLoai, colDoiTuong, colChiTiet, colTrangThai));
        tblHoatDong.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblHoatDong.setPlaceholder(new Label("Chưa có phát sinh hoạt động nào trong ngày hôm nay."));
        tblHoatDong.setPrefHeight(340);

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
