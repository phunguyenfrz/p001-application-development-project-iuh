package com.petshop.view.doitra;

import com.petshop.model.ChiTietPhieuDoiTra;
import com.petshop.model.PhieuDoiTra;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

/**
 * Hop thoai Modal: Xem chi tiet phieu doi tra (chi doc).
 */
public class XemChiTietPhieuDoiTraView extends Stage {

    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 VNĐ");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public XemChiTietPhieuDoiTraView(PhieuDoiTra phieu) {
        this.initModality(Modality.APPLICATION_MODAL);
        this.setTitle("Chi tiết phiếu đổi trả - " + (phieu != null ? phieu.getMaPhieuDoiTra() : ""));
        khoiTaoGiaoDien(phieu);
    }

    private void khoiTaoGiaoDien(PhieuDoiTra phieuDauVao) {
        PhieuDoiTra phieuTam = phieuDauVao;
        if (phieuTam != null && (phieuTam.getDanhSachSanPham() == null || phieuTam.getDanhSachSanPham().isEmpty())) {
            try {
                com.petshop.dao.PhieuDoiTraDAO dao = new com.petshop.dao.PhieuDoiTraDAO();
                PhieuDoiTra phieuDayDu = dao.layPhieuDoiTraTheoMa(phieuTam.getMaPhieuDoiTra());
                if (phieuDayDu != null && phieuDayDu.getDanhSachSanPham() != null && !phieuDayDu.getDanhSachSanPham().isEmpty()) {
                    phieuTam = phieuDayDu;
                }
            } catch (Exception ignored) {}
        }

        final PhieuDoiTra phieu = phieuTam;

        VBox root = new VBox(14);
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color: #f8fafc;");

        if (phieu == null) {
            root.getChildren().add(new Label("Không có dữ liệu phiếu đổi trả!"));
            this.setScene(new Scene(root, 400, 200));
            return;
        }

        // 1. Header card
        VBox cardHeader = new VBox(8);
        cardHeader.setPadding(new Insets(12));
        cardHeader.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px;");

        HBox hangMa = new HBox(14);
        hangMa.setAlignment(Pos.CENTER_LEFT);
        Label lblMa = new Label("MÃ PHIẾU: " + phieu.getMaPhieuDoiTra());
        lblMa.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label lblTrangThai = new Label(phieu.getTrangThai());
        lblTrangThai.setStyle("-fx-padding: 4 12; -fx-background-radius: 12px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: #2563eb;");
        if ("Hoàn thành".equalsIgnoreCase(phieu.getTrangThai())) lblTrangThai.setStyle("-fx-padding: 4 12; -fx-background-radius: 12px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: #16a34a;");
        else if ("Từ chối".equalsIgnoreCase(phieu.getTrangThai())) lblTrangThai.setStyle("-fx-padding: 4 12; -fx-background-radius: 12px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: #dc2626;");
        else if ("Chờ xử lý".equalsIgnoreCase(phieu.getTrangThai())) lblTrangThai.setStyle("-fx-padding: 4 12; -fx-background-radius: 12px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: #d97706;");

        hangMa.getChildren().addAll(lblMa, lblTrangThai);

        GridPane gridInfo = new GridPane();
        gridInfo.setHgap(20);
        gridInfo.setVgap(6);
        gridInfo.add(new Label("• Hóa đơn gốc: " + phieu.getMaHoaDon()), 0, 0);
        gridInfo.add(new Label("• Ngày lập: " + (phieu.getNgayLap() != null ? phieu.getNgayLap().format(DINH_DANG_NGAY) : "--")), 1, 0);
        gridInfo.add(new Label("• Khách hàng: " + (phieu.getTenKhachHang() != null ? phieu.getTenKhachHang() : "Khách lẻ")), 0, 1);
        gridInfo.add(new Label("• Số điện thoại: " + (phieu.getSoDienThoaiKhachHang() != null ? phieu.getSoDienThoaiKhachHang() : "--")), 1, 1);
        gridInfo.add(new Label("• Loại xử lý: " + (phieu.getLoaiXuLy() != null ? phieu.getLoaiXuLy() : "--")), 0, 2);
        gridInfo.add(new Label("• Nhân viên lập: " + (phieu.getMaNhanVien() != null ? phieu.getMaNhanVien() : "--")), 1, 2);

        cardHeader.getChildren().addAll(hangMa, gridInfo);

        // 2. Bang chi tiet hang tra
        VBox cardBang = new VBox(8);
        cardBang.setPadding(new Insets(12));
        cardBang.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px;");

        Label lblTieuDeBang = new Label("CHI TIẾT MẶT HÀNG ĐỔI TRẢ & HƯỚNG XỬ LÝ (QL26)");
        lblTieuDeBang.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #334155;");

        TableView<ChiTietPhieuDoiTra> tbl = new TableView<>();
        tbl.setPrefHeight(200);
        tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ChiTietPhieuDoiTra, String> colTen = new TableColumn<>("Sản phẩm");
        colTen.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenSanPham()));

        TableColumn<ChiTietPhieuDoiTra, Integer> colSL = new TableColumn<>("SL");
        colSL.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSoLuong()));
        colSL.setPrefWidth(45);

        TableColumn<ChiTietPhieuDoiTra, String> colGia = new TableColumn<>("Đơn giá");
        colGia.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().getDonGia())));

        TableColumn<ChiTietPhieuDoiTra, String> colLyDo = new TableColumn<>("Lý do");
        colLyDo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLyDo()));

        TableColumn<ChiTietPhieuDoiTra, String> colTinhTrang = new TableColumn<>("Tình trạng");
        colTinhTrang.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTinhTrang()));

        TableColumn<ChiTietPhieuDoiTra, String> colHuongXuLy = new TableColumn<>("Hướng xử lý");
        colHuongXuLy.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHuongXuLy()));

        TableColumn<ChiTietPhieuDoiTra, String> colThanhTien = new TableColumn<>("Thành tiền hoàn");
        colThanhTien.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().tinhThanhTienTra())));

        TableColumn<ChiTietPhieuDoiTra, String> colGhiChu = new TableColumn<>("Ghi chú");
        colGhiChu.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGhiChu()));

        tbl.getColumns().addAll(colTen, colSL, colGia, colLyDo, colTinhTrang, colHuongXuLy, colThanhTien, colGhiChu);
        if (phieu.getDanhSachSanPham() != null) {
            tbl.setItems(FXCollections.observableArrayList(phieu.getDanhSachSanPham()));
        }

        // Tong tien
        HBox hangTongTien = new HBox(12);
        hangTongTien.setAlignment(Pos.CENTER_RIGHT);
        BigDecimal tongTienTra = phieu.tinhTongTienHangTra();
        if (tongTienTra.compareTo(BigDecimal.ZERO) == 0 && phieu.getTienHoan() != null && phieu.getTienHoan().compareTo(BigDecimal.ZERO) > 0) {
            tongTienTra = phieu.getTienHoan();
        }
        Label lblTongTien = new Label("Tổng tiền hàng trả: " + DINH_DANG_TIEN.format(tongTienTra));
        lblTongTien.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dc2626;");
        hangTongTien.getChildren().add(lblTongTien);

        cardBang.getChildren().addAll(lblTieuDeBang, tbl, hangTongTien);

        // 3. Footer buttons
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button btnIn = new Button("🖨 In lại phiếu");
        btnIn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 18; -fx-cursor: hand;");
        btnIn.setOnAction(e -> {
            Alert al = new Alert(Alert.AlertType.INFORMATION);
            al.setTitle("In phiếu");
            al.setHeaderText(null);
            al.setContentText("Đã gửi lệnh in cho phiếu: " + phieu.getMaPhieuDoiTra());
            al.showAndWait();
        });

        Button btnDong = new Button("Đóng");
        btnDong.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-padding: 8 18; -fx-cursor: hand;");
        btnDong.setOnAction(e -> this.close());

        footer.getChildren().addAll(btnIn, btnDong);

        root.getChildren().addAll(cardHeader, cardBang, footer);

        Scene scene = new Scene(root, 780, 520);
        this.setScene(scene);
    }
}

