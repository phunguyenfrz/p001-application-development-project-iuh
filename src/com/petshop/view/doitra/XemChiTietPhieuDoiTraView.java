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
 * Hộp thoại Modal: Xem chi tiết phiếu đổi trả (chỉ đọc).
 * Sử dụng bộ màu mặc định chuẩn chung.css.
 */
public class XemChiTietPhieuDoiTraView extends Stage {

    private static final String CSS_CHUNG = "/com/petshop/view/css/chung.css";
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
        root.getStyleClass().add("khung-phan-he");

        if (phieu == null) {
            root.getChildren().add(new Label("Không có dữ liệu phiếu đổi trả!"));
            this.setScene(new Scene(root, 400, 200));
            return;
        }

        // 1. Header card
        VBox cardHeader = new VBox(8);
        cardHeader.getStyleClass().add("hop-noi-dung");

        HBox hangMa = new HBox(14);
        hangMa.setAlignment(Pos.CENTER_LEFT);
        Label lblMa = new Label("MÃ PHIẾU: " + phieu.getMaPhieuDoiTra());
        lblMa.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label lblTrangThai = new Label(phieu.getTrangThai());
        lblTrangThai.setStyle("-fx-padding: 3 10; -fx-background-color: #e2e8f0; -fx-background-radius: 4px; -fx-font-weight: bold;");

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

        // 2. Bảng chi tiết hàng trả
        VBox cardBang = new VBox(8);
        cardBang.getStyleClass().add("hop-noi-dung");

        Label lblTieuDeBang = new Label("CHI TIẾT MẶT HÀNG ĐỔI TRẢ & XỬ LÝ SẢN PHẨM HOÀN");
        lblTieuDeBang.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        TableView<ChiTietPhieuDoiTra> tbl = new TableView<>();
        tbl.getStyleClass().add("bang-du-lieu");
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

        TableColumn<ChiTietPhieuDoiTra, String> colHuongXuLy = new TableColumn<>("Xử lý SP hoàn");
        colHuongXuLy.setCellValueFactory(d -> new SimpleStringProperty(
                XuLyPhieuDoiTraView.chuanHoaHuongXuLyHienThi(d.getValue().getHuongXuLy(), d.getValue().getTinhTrang())
        ));

        TableColumn<ChiTietPhieuDoiTra, String> colThanhTien = new TableColumn<>("Thành tiền hoàn");
        colThanhTien.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().tinhThanhTienTra())));

        TableColumn<ChiTietPhieuDoiTra, String> colGhiChu = new TableColumn<>("Ghi chú");
        colGhiChu.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGhiChu()));

        tbl.getColumns().addAll(colTen, colSL, colGia, colLyDo, colTinhTrang, colHuongXuLy, colThanhTien, colGhiChu);
        if (phieu.getDanhSachSanPham() != null) {
            tbl.setItems(FXCollections.observableArrayList(phieu.getDanhSachSanPham()));
        }

        HBox hangTongTien = new HBox(12);
        hangTongTien.setAlignment(Pos.CENTER_RIGHT);
        BigDecimal tongTienTra = phieu.tinhTongTienHangTra();
        if (tongTienTra.compareTo(BigDecimal.ZERO) == 0 && phieu.getTienHoan() != null && phieu.getTienHoan().compareTo(BigDecimal.ZERO) > 0) {
            tongTienTra = phieu.getTienHoan();
        }
        Label lblTongTien = new Label("Tổng tiền hàng trả: " + DINH_DANG_TIEN.format(tongTienTra));
        lblTongTien.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        hangTongTien.getChildren().add(lblTongTien);

        cardBang.getChildren().addAll(lblTieuDeBang, tbl, hangTongTien);

        // 3. Footer buttons
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button btnIn = new Button("In lại phiếu");
        btnIn.getStyleClass().add("nut-chinh");
        btnIn.setOnAction(e -> {
            Alert al = new Alert(Alert.AlertType.INFORMATION);
            al.setTitle("In phiếu");
            al.setHeaderText(null);
            al.setContentText("Đã gửi lệnh in cho phiếu: " + phieu.getMaPhieuDoiTra());
            al.showAndWait();
        });

        Button btnDong = new Button("Đóng");
        btnDong.getStyleClass().add("nut-phu");
        btnDong.setOnAction(e -> this.close());

        footer.getChildren().addAll(btnIn, btnDong);

        root.getChildren().addAll(cardHeader, cardBang, footer);

        Scene scene = new Scene(root, 780, 520);
        if (getClass().getResource(CSS_CHUNG) != null) {
            scene.getStylesheets().add(getClass().getResource(CSS_CHUNG).toExternalForm());
        }
        this.setScene(scene);
    }
}
