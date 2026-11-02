package com.petshop.view.doitra;

import com.petshop.model.ChiTietPhieuDoiTra;
import com.petshop.model.PhieuDoiTra;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
 * Thiết kế đồng bộ hoàn toàn với màn hình Cập nhật / Chi tiết phiếu đổi trả:
 * - Card thông tin tóm tắt phiếu & hóa đơn gốc.
 * - Card danh sách mặt hàng đổi trả.
 * - Card hình thức xử lý đổi trả (hoàn tiền hoặc bảng hàng đổi mới).
 * - Thanh tổng kết tài chính cố định ở dưới.
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

        VBox root = new VBox(12);
        root.setPadding(new Insets(16));
        root.getStyleClass().add("khung-phan-he");

        if (phieu == null) {
            root.getChildren().add(new Label("Không có dữ liệu phiếu đổi trả!"));
            this.setScene(new Scene(root, 400, 200));
            return;
        }

        // Header trên cùng: Tiêu đề + Badge trạng thái
        HBox pnlHeader = new HBox(12);
        pnlHeader.setAlignment(Pos.CENTER_LEFT);

        Label lblTieuDe = new Label("CHI TIẾT PHIẾU ĐỔI TRẢ");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        Label lblTrangThai = new Label(phieu.getTrangThai());
        String tt = phieu.getTrangThai();
        if ("Đã duyệt".equalsIgnoreCase(tt) || "Hoàn thành".equalsIgnoreCase(tt)) {
            lblTrangThai.setStyle("-fx-padding: 3 10; -fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-background-radius: 4px; -fx-font-weight: bold;");
        } else if ("Từ chối".equalsIgnoreCase(tt)) {
            lblTrangThai.setStyle("-fx-padding: 3 10; -fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-background-radius: 4px; -fx-font-weight: bold;");
        } else {
            lblTrangThai.setStyle("-fx-padding: 3 10; -fx-background-color: #fef9c3; -fx-text-fill: #854d0e; -fx-background-radius: 4px; -fx-font-weight: bold;");
        }

        Region spacerH = new Region();
        HBox.setHgrow(spacerH, Priority.ALWAYS);

        pnlHeader.getChildren().addAll(lblTieuDe, lblTrangThai, spacerH);

        // Card 1: Thông tin tóm tắt phiếu & hóa đơn
        VBox cardThongTin = new VBox(8);
        cardThongTin.getStyleClass().add("hop-noi-dung");

        Label lblTieuDeCard1 = new Label("THÔNG TIN PHIẾU ĐỔI TRẢ & HÓA ĐƠN GỐC");
        lblTieuDeCard1.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px;");

        GridPane gridInfo = new GridPane();
        gridInfo.setHgap(24);
        gridInfo.setVgap(6);
        gridInfo.add(new Label("Mã phiếu:"), 0, 0);
        Label lblMa = new Label(phieu.getMaPhieuDoiTra());
        lblMa.setStyle("-fx-font-weight: bold;");
        gridInfo.add(lblMa, 1, 0);

        gridInfo.add(new Label("Hóa đơn gốc:"), 2, 0);
        Label lblHd = new Label(phieu.getMaHoaDon() != null ? phieu.getMaHoaDon() : "--");
        lblHd.setStyle("-fx-font-weight: bold;");
        gridInfo.add(lblHd, 3, 0);

        gridInfo.add(new Label("Ngày lập:"), 0, 1);
        gridInfo.add(new Label(phieu.getNgayLap() != null ? phieu.getNgayLap().format(DINH_DANG_NGAY) : "--"), 1, 1);

        gridInfo.add(new Label("Khách hàng:"), 2, 1);
        gridInfo.add(new Label(phieu.getTenKhachHang() != null ? phieu.getTenKhachHang() : "Khách lẻ"), 3, 1);

        gridInfo.add(new Label("Số điện thoại:"), 0, 2);
        gridInfo.add(new Label(phieu.getSoDienThoaiKhachHang() != null ? phieu.getSoDienThoaiKhachHang() : "--"), 1, 2);

        gridInfo.add(new Label("Nhân viên lập:"), 2, 2);
        gridInfo.add(new Label(phieu.getMaNhanVien() != null ? phieu.getMaNhanVien() : "--"), 3, 2);

        gridInfo.add(new Label("Hình thức xử lý:"), 0, 3);
        Label lblLoaiXl = new Label(phieu.getLoaiXuLy() != null ? phieu.getLoaiXuLy() : "--");
        lblLoaiXl.setStyle("-fx-font-weight: bold;");
        gridInfo.add(lblLoaiXl, 1, 3);

        cardThongTin.getChildren().addAll(lblTieuDeCard1, gridInfo);

        // Card 2: Danh sách mặt hàng đổi trả
        VBox cardBangTra = new VBox(8);
        cardBangTra.getStyleClass().add("hop-noi-dung");

        Label lblTieuDeCard2 = new Label("DANH SÁCH MẶT HÀNG ĐỔI TRẢ");
        lblTieuDeCard2.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px;");

        TableView<ChiTietPhieuDoiTra> tblTra = new TableView<>();
        tblTra.getStyleClass().add("bang-du-lieu");
        tblTra.setPrefHeight(150);
        tblTra.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ChiTietPhieuDoiTra, String> colMaSp = new TableColumn<>("Mã SP");
        colMaSp.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaSanPham()));
        colMaSp.setPrefWidth(110);

        TableColumn<ChiTietPhieuDoiTra, String> colTenSp = new TableColumn<>("Tên sản phẩm");
        colTenSp.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenSanPham()));
        colTenSp.setPrefWidth(170);

        TableColumn<ChiTietPhieuDoiTra, String> colGiaTra = new TableColumn<>("Đơn giá trả");
        colGiaTra.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().getDonGia())));
        colGiaTra.setStyle("-fx-alignment: center-right;");
        colGiaTra.setPrefWidth(95);

        TableColumn<ChiTietPhieuDoiTra, Integer> colSlTra = new TableColumn<>("SL trả");
        colSlTra.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSoLuong()));
        colSlTra.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");
        colSlTra.setPrefWidth(70);

        TableColumn<ChiTietPhieuDoiTra, String> colThanhTien = new TableColumn<>("Thành tiền hoàn");
        colThanhTien.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().tinhThanhTienTra())));
        colThanhTien.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");
        colThanhTien.setPrefWidth(115);

        TableColumn<ChiTietPhieuDoiTra, String> colLyDo = new TableColumn<>("Lý do");
        colLyDo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLyDo()));
        colLyDo.setPrefWidth(120);

        TableColumn<ChiTietPhieuDoiTra, String> colTinhTrang = new TableColumn<>("Tình trạng");
        colTinhTrang.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTinhTrang()));
        colTinhTrang.setPrefWidth(110);

        TableColumn<ChiTietPhieuDoiTra, String> colGhiChu = new TableColumn<>("Ghi chú / Mô tả lỗi");
        colGhiChu.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGhiChu() != null ? d.getValue().getGhiChu() : ""));
        colGhiChu.setPrefWidth(140);

        tblTra.getColumns().addAll(colMaSp, colTenSp, colGiaTra, colSlTra, colThanhTien, colLyDo, colTinhTrang, colGhiChu);
        if (phieu.getDanhSachSanPham() != null) {
            tblTra.setItems(FXCollections.observableArrayList(phieu.getDanhSachSanPham()));
        }

        cardBangTra.getChildren().addAll(lblTieuDeCard2, tblTra);

        // Card 3: Hình thức xử lý đổi trả
        VBox cardHinhThuc = new VBox(12);
        cardHinhThuc.getStyleClass().add("hop-noi-dung");

        Label lblTieuDeCard3 = new Label("HÌNH THỨC XỬ LÝ ĐỔI TRẢ");
        lblTieuDeCard3.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px;");

        RadioButton rdoTra = new RadioButton("Trả hàng, hoàn tiền");
        RadioButton rdoDoi = new RadioButton("Đổi sang sản phẩm khác");
        ToggleGroup grp = new ToggleGroup();
        rdoTra.setToggleGroup(grp);
        rdoDoi.setToggleGroup(grp);
        rdoTra.setDisable(true);
        rdoDoi.setDisable(true);
        rdoTra.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1f2937;");
        rdoDoi.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1f2937;");

        HBox hangChon = new HBox(30, rdoTra, rdoDoi);
        hangChon.setAlignment(Pos.CENTER_LEFT);

        BigDecimal tongHangTra = phieu.tinhTongTienHangTra();
        BigDecimal tongHangDoi = BigDecimal.ZERO;
        boolean laDoiHang = "Đổi hàng".equalsIgnoreCase(phieu.getLoaiXuLy());

        VBox pnlTheNoiDung = new VBox(10);
        pnlTheNoiDung.setPadding(new Insets(10));
        pnlTheNoiDung.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 4px; -fx-background-radius: 4px;");

        if (laDoiHang) {
            rdoDoi.setSelected(true);

            TableView<ChiTietPhieuDoiTra> tblDoi = new TableView<>();
            tblDoi.getStyleClass().add("bang-du-lieu");
            tblDoi.setPrefHeight(120);
            tblDoi.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            TableColumn<ChiTietPhieuDoiTra, String> colTenDoi = new TableColumn<>("Sản phẩm đổi mới");
            colTenDoi.setCellValueFactory(d -> {
                String ten = d.getValue().getTenSanPhamDoi();
                return new SimpleStringProperty(ten != null && !ten.isBlank() ? ten : d.getValue().getMaSanPhamDoi());
            });
            colTenDoi.setPrefWidth(220);

            TableColumn<ChiTietPhieuDoiTra, String> colGiaDoi = new TableColumn<>("Đơn giá");
            colGiaDoi.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().getDonGiaDoi())));
            colGiaDoi.setStyle("-fx-alignment: center-right;");
            colGiaDoi.setPrefWidth(110);

            TableColumn<ChiTietPhieuDoiTra, Integer> colSlDoi = new TableColumn<>("SL");
            colSlDoi.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSoLuongDoi()));
            colSlDoi.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");
            colSlDoi.setPrefWidth(70);

            TableColumn<ChiTietPhieuDoiTra, String> colTtDoi = new TableColumn<>("Thành tiền");
            colTtDoi.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().tinhThanhTienDoi())));
            colTtDoi.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");
            colTtDoi.setPrefWidth(120);

            tblDoi.getColumns().addAll(colTenDoi, colGiaDoi, colSlDoi, colTtDoi);

            ObservableList<ChiTietPhieuDoiTra> dsDoi = FXCollections.observableArrayList();
            if (phieu.getDanhSachSanPham() != null) {
                for (ChiTietPhieuDoiTra ct : phieu.getDanhSachSanPham()) {
                    if (ct.getMaSanPhamDoi() != null && !ct.getMaSanPhamDoi().isBlank()) {
                        tongHangDoi = tongHangDoi.add(ct.tinhThanhTienDoi());
                        dsDoi.add(ct);
                    }
                }
            }
            tblDoi.setItems(dsDoi);

            Label lblKetLuanChenhLech = new Label();
            lblKetLuanChenhLech.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
            BigDecimal tienBu = phieu.getTienBu() != null ? phieu.getTienBu() : BigDecimal.ZERO;
            BigDecimal tienHoan = phieu.getTienHoan() != null ? phieu.getTienHoan() : BigDecimal.ZERO;

            if (tienBu.compareTo(BigDecimal.ZERO) > 0) {
                lblKetLuanChenhLech.setText("Kết quả đổi trả: Khách bù thêm " + DINH_DANG_TIEN.format(tienBu));
                lblKetLuanChenhLech.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #0284c7;");
            } else if (tienHoan.compareTo(BigDecimal.ZERO) > 0) {
                lblKetLuanChenhLech.setText("Kết quả đổi trả: Hoàn lại cho khách " + DINH_DANG_TIEN.format(tienHoan));
                lblKetLuanChenhLech.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #dc2626;");
            } else {
                lblKetLuanChenhLech.setText("Kết quả đổi trả: Đổi ngang giá (Chênh lệch: 0 VNĐ)");
                lblKetLuanChenhLech.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #16a34a;");
            }

            pnlTheNoiDung.getChildren().addAll(tblDoi, lblKetLuanChenhLech);
        } else {
            rdoTra.setSelected(true);
            HBox hangHoan = new HBox(16);
            hangHoan.setAlignment(Pos.CENTER_LEFT);
            Label lblTienHoan = new Label(DINH_DANG_TIEN.format(phieu.getTienHoan() != null ? phieu.getTienHoan() : tongHangTra));
            lblTienHoan.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #b91c1c;");
            hangHoan.getChildren().addAll(
                    new Label("Hình thức hoàn:"), new Label("Tiền mặt"),
                    new Label("Số tiền hoàn trả khách:"), lblTienHoan
            );
            pnlTheNoiDung.getChildren().add(hangHoan);
        }

        cardHinhThuc.getChildren().addAll(lblTieuDeCard3, hangChon, pnlTheNoiDung);

        // Thanh tổng kết tài chính cố định ở dưới
        HBox barTomTat = new HBox(20);
        barTomTat.setPadding(new Insets(10, 14, 10, 14));
        barTomTat.setAlignment(Pos.CENTER_LEFT);
        barTomTat.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0;");

        Label lblTongTra = new Label("Tổng hàng trả: " + DINH_DANG_TIEN.format(tongHangTra));
        lblTongTra.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold;");

        Label lblTongDoi = new Label("Tổng hàng đổi: " + DINH_DANG_TIEN.format(tongHangDoi));
        lblTongDoi.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold;");

        BigDecimal tienBu = phieu.getTienBu() != null ? phieu.getTienBu() : BigDecimal.ZERO;
        BigDecimal tienHoan = phieu.getTienHoan() != null ? phieu.getTienHoan() : BigDecimal.ZERO;
        Label lblChenhLech = new Label();
        lblChenhLech.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        if (tienBu.compareTo(BigDecimal.ZERO) > 0) {
            lblChenhLech.setText("Khách bù thêm: " + DINH_DANG_TIEN.format(tienBu));
            lblChenhLech.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0284c7;");
        } else if (tienHoan.compareTo(BigDecimal.ZERO) > 0) {
            lblChenhLech.setText("Tiền hoàn lại: " + DINH_DANG_TIEN.format(tienHoan));
            lblChenhLech.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dc2626;");
        } else {
            lblChenhLech.setText("Chênh lệch: 0 VNĐ");
            lblChenhLech.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #16a34a;");
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

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

        barTomTat.getChildren().addAll(lblTongTra, lblTongDoi, lblChenhLech, spacer, btnIn, btnDong);

        VBox noiDungCuon = new VBox(12, cardThongTin, cardBangTra, cardHinhThuc);
        noiDungCuon.setPadding(new Insets(2, 2, 10, 2));

        ScrollPane scroll = new ScrollPane(noiDungCuon);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(pnlHeader, scroll, barTomTat);

        Scene scene = new Scene(root, 960, 640);
        try {
            scene.getStylesheets().add(getClass().getResource(CSS_CHUNG).toExternalForm());
        } catch (Exception ignored) {}
        this.setScene(scene);
    }
}
