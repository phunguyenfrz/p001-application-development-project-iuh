package com.petshop.view.hoadon;

import com.petshop.model.ChiTietHoaDonDichVu;
import com.petshop.model.ChiTietHoaDonSanPham;
import com.petshop.model.HoaDon;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

/**
 * Hộp thoại hiển thị bản in hóa đơn bán lẻ chuẩn khổ nhiệt K80 / A5 của cửa hàng Happy Tails.
 * Hỗ trợ in trực tiếp ra máy in qua JavaFX PrinterJob.
 */
public class InHoaDonDialog extends Stage {

    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 VNĐ");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public InHoaDonDialog(HoaDon hoaDon, Stage chuSoHuu) {
        initModality(Modality.APPLICATION_MODAL);
        if (chuSoHuu != null) initOwner(chuSoHuu);
        setTitle("In hóa đơn - " + hoaDon.getMaHoaDon());

        VBox pnlHoaDonGiay = taoNoiDungHoaDon(hoaDon);

        ScrollPane spCuon = new ScrollPane(pnlHoaDonGiay);
        spCuon.setFitToWidth(true);
        spCuon.setStyle("-fx-background: #e2e8f0; -fx-background-color: #e2e8f0;");

        Button btnIn = new Button("🖨 In hóa đơn");
        btnIn.getStyleClass().add("nut-chinh");
        btnIn.setOnAction(e -> {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(this)) {
                boolean success = job.printPage(pnlHoaDonGiay);
                if (success) {
                    job.endJob();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Đã gửi lệnh in hóa đơn thành công!");
                    alert.setHeaderText(null);
                    alert.showAndWait();
                    close();
                }
            }
        });

        Button btnDong = new Button("Đóng");
        btnDong.getStyleClass().add("nut-phu");
        btnDong.setOnAction(e -> close());

        HBox pnlNut = new HBox(10, btnIn, btnDong);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);
        pnlNut.setPadding(new Insets(10, 16, 12, 16));
        pnlNut.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-width: 1 0 0 0;");

        BorderPane pnlGoc = new BorderPane();
        pnlGoc.setCenter(spCuon);
        pnlGoc.setBottom(pnlNut);

        Scene scene = new Scene(pnlGoc, 480, 680);
        setScene(scene);
        setMinWidth(460);
        setMinHeight(550);
    }

    private VBox taoNoiDungHoaDon(HoaDon hd) {
        VBox pnl = new VBox(8);
        pnl.setPadding(new Insets(24));
        pnl.setMaxWidth(400);
        pnl.setStyle("-fx-background-color: #ffffff; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 6, 0, 0, 2); -fx-border-color: #cbd5e1; -fx-border-width: 1;");
        pnl.setAlignment(Pos.TOP_CENTER);

        // Header thương hiệu
        Label lblBrand = new Label("🐾 HAPPY TAILS PET SHOP 🐾");
        lblBrand.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0369a1;");

        Label lblSlogan = new Label("Chăm sóc trọn vẹn - Yêu thương thú cưng");
        lblSlogan.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-style: italic;");

        Label lblDiaChi = new Label("12 Nguyễn Văn Bảo, Phường 4, Gò Vấp, TP.HCM");
        lblDiaChi.setStyle("-fx-font-size: 11px; -fx-text-fill: #475569;");
        Label lblHotline = new Label("Hotline: 0909.000.000 - 0987.654.321");
        lblHotline.setStyle("-fx-font-size: 11px; -fx-text-fill: #475569;");

        Label lblPhanCach1 = taoDuongPhanCach();

        Label lblTieuDe = new Label("HÓA ĐƠN BÁN LẺ");
        lblTieuDe.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label lblMaHd = new Label("Số HĐ: " + hd.getMaHoaDon());
        lblMaHd.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        // Thông tin chung
        GridPane luoiThongTin = new GridPane();
        luoiThongTin.setHgap(8);
        luoiThongTin.setVgap(4);

        luoiThongTin.add(taoLabelNhe("Ngày lập:"), 0, 0);
        luoiThongTin.add(new Label(hd.getNgayLap() != null ? hd.getNgayLap().format(DINH_DANG_NGAY) : "--"), 1, 0);

        luoiThongTin.add(taoLabelNhe("Thu ngân:"), 0, 1);
        luoiThongTin.add(new Label(hd.getTenNhanVien() != null ? hd.getTenNhanVien() : hd.getMaNhanVien()), 1, 1);

        luoiThongTin.add(taoLabelNhe("Khách hàng:"), 0, 2);
        String kh = hd.getTenKhachHang() != null ? hd.getTenKhachHang() : "Khách vãng lai";
        if (hd.getSoDienThoaiKhachHang() != null && !hd.getSoDienThoaiKhachHang().isBlank()) {
            kh += " (" + hd.getSoDienThoaiKhachHang() + ")";
        }
        luoiThongTin.add(new Label(kh), 1, 2);

        if (hd.getMaPhieuDichVu() != null && !hd.getMaPhieuDichVu().isBlank()) {
            luoiThongTin.add(taoLabelNhe("Phiếu dịch vụ:"), 0, 3);
            luoiThongTin.add(new Label(hd.getMaPhieuDichVu()), 1, 3);
        }

        Label lblPhanCach2 = taoDuongPhanCach();

        // Bảng danh sách mặt hàng
        VBox pnlBang = new VBox(6);
        pnlBang.setAlignment(Pos.TOP_LEFT);

        // Header bảng
        HBox hangTieuDe = new HBox(4);
        Label h1 = new Label("Mặt hàng");
        Label h2 = new Label("SL");
        Label h3 = new Label("Đ.Giá");
        Label h4 = new Label("T.Tiền");

        h1.setPrefWidth(150); h1.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        h2.setPrefWidth(30);  h2.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-alignment: center-right;");
        h3.setPrefWidth(80);  h3.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-alignment: center-right;");
        h4.setPrefWidth(90);  h4.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-alignment: center-right;");
        hangTieuDe.getChildren().addAll(h1, h2, h3, h4);
        pnlBang.getChildren().add(hangTieuDe);

        // Sản phẩm
        if (hd.getDanhSachSanPham() != null) {
            for (ChiTietHoaDonSanPham sp : hd.getDanhSachSanPham()) {
                pnlBang.getChildren().add(taoDongBang(
                        sp.getTenSanPham() != null ? sp.getTenSanPham() : sp.getMaSanPham(),
                        sp.getSoLuong(),
                        sp.getDonGia(),
                        sp.getThanhTien()
                ));
            }
        }

        // Dịch vụ
        if (hd.getDanhSachDichVu() != null) {
            for (ChiTietHoaDonDichVu dv : hd.getDanhSachDichVu()) {
                String ten = (dv.getTenDichVu() != null ? dv.getTenDichVu() : dv.getMaDichVu()) + " [DV]";
                pnlBang.getChildren().add(taoDongBang(ten, 1, dv.getDonGia(), dv.getThanhTien()));
            }
        }

        Label lblPhanCach3 = taoDuongPhanCach();

        // Tổng kết thanh toán
        GridPane luoiTongKet = new GridPane();
        luoiTongKet.setHgap(8);
        luoiTongKet.setVgap(4);
        luoiTongKet.setAlignment(Pos.CENTER_RIGHT);

        int row = 0;
        BigDecimal tongGoc = hd.tinhTongTienGoc();
        luoiTongKet.add(taoLabelNhe("Tổng tiền hàng:"), 0, row);
        Label lblTongGoc = new Label(DINH_DANG_TIEN.format(tongGoc));
        lblTongGoc.setStyle("-fx-alignment: center-right;");
        luoiTongKet.add(lblTongGoc, 1, row++);

        if (hd.getTienGiam() != null && hd.getTienGiam().compareTo(BigDecimal.ZERO) > 0) {
            luoiTongKet.add(taoLabelNhe("Giảm giá (" + (hd.getMaKhuyenMai() != null ? hd.getMaKhuyenMai() : "KM") + "):"), 0, row);
            Label lblGiam = new Label("-" + DINH_DANG_TIEN.format(hd.getTienGiam()));
            lblGiam.setStyle("-fx-alignment: center-right; -fx-text-fill: #dc2626;");
            luoiTongKet.add(lblGiam, 1, row++);
        }

        if (hd.getDiemTichLuySuDung() > 0) {
            luoiTongKet.add(taoLabelNhe("Dùng điểm (" + hd.getDiemTichLuySuDung() + " đ):"), 0, row);
            Label lblGiamDiem = new Label("-" + DINH_DANG_TIEN.format(hd.getTienGiamTuDiem()));
            lblGiamDiem.setStyle("-fx-alignment: center-right; -fx-text-fill: #dc2626;");
            luoiTongKet.add(lblGiamDiem, 1, row++);
        }

        luoiTongKet.add(new Label("THANH TOÁN:"), 0, row);
        Label lblThanhToan = new Label(DINH_DANG_TIEN.format(hd.getTongTien()));
        lblThanhToan.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0369a1; -fx-alignment: center-right;");
        luoiTongKet.add(lblThanhToan, 1, row++);

        luoiTongKet.add(taoLabelNhe("Phương thức:"), 0, row);
        String ht = hd.getHinhThucThanhToan() != null ? hd.getHinhThucThanhToan() : "Tiền mặt";
        luoiTongKet.add(new Label(ht), 1, row++);

        if ("Tiền mặt".equalsIgnoreCase(ht)) {
            BigDecimal tienThanhToanThem = hd.getTienThanhToanThem();
            BigDecimal tienHoanLai = hd.getTienHoanLai();
            boolean coThanhToanThem = tienThanhToanThem.compareTo(BigDecimal.ZERO) > 0;
            boolean coHoanTien = tienHoanLai.compareTo(BigDecimal.ZERO) > 0;

            BigDecimal tienDua = hd.getTienKhachDua() != null ? hd.getTienKhachDua() : hd.getTongTien();
            BigDecimal tienThoi;
            if (coThanhToanThem) {
                tienThoi = hd.getTienThoiLai() != null
                        ? hd.getTienThoiLai()
                        : tienDua.subtract(tienThanhToanThem).max(BigDecimal.ZERO);
            } else {
                BigDecimal tongSoSanh = coHoanTien ? hd.getTongTien().add(tienHoanLai) : hd.getTongTien();
                tienDua = coHoanTien ? tongSoSanh : tienDua;
                tienThoi = hd.getTienThoiLai() != null
                        ? hd.getTienThoiLai()
                        : tienDua.subtract(tongSoSanh).max(BigDecimal.ZERO);
            }

            luoiTongKet.add(taoLabelNhe("Tiền khách đưa:"), 0, row);
            luoiTongKet.add(new Label(DINH_DANG_TIEN.format(tienDua)), 1, row++);

            luoiTongKet.add(taoLabelNhe("Tiền thối lại:"), 0, row);
            Label lblThoi = new Label(DINH_DANG_TIEN.format(tienThoi));
            lblThoi.setStyle("-fx-font-weight: bold; -fx-text-fill: #15803d;");
            luoiTongKet.add(lblThoi, 1, row++);
        }

        if (hd.getDiemTichLuyCong() > 0) {
            luoiTongKet.add(taoLabelNhe("Điểm tích lũy cộng mới:"), 0, row);
            Label lblDiemMoi = new Label("+" + hd.getDiemTichLuyCong() + " điểm");
            lblDiemMoi.setStyle("-fx-font-weight: bold; -fx-text-fill: #0284c7;");
            luoiTongKet.add(lblDiemMoi, 1, row++);
        }

        if (hd.getTienThanhToanThem().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tongHoaDonGoc = hd.getTongTien().subtract(hd.getTienThanhToanThem());
            luoiTongKet.add(taoLabelNhe("Đã thanh toán hóa đơn gốc:"), 0, row);
            luoiTongKet.add(new Label(DINH_DANG_TIEN.format(tongHoaDonGoc)), 1, row++);
            luoiTongKet.add(taoLabelNhe("Khách cần bù thêm:"), 0, row);
            Label lblThem = new Label("+" + DINH_DANG_TIEN.format(hd.getTienThanhToanThem()));
            lblThem.setStyle("-fx-font-weight: bold; -fx-text-fill: #0369a1;");
            luoiTongKet.add(lblThem, 1, row++);
        } else if (hd.getTienHoanLai().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tongHoaDonGoc = hd.getTongTien().add(hd.getTienHoanLai());
            luoiTongKet.add(taoLabelNhe("Đã thanh toán hóa đơn gốc:"), 0, row);
            luoiTongKet.add(new Label(DINH_DANG_TIEN.format(tongHoaDonGoc)), 1, row++);
            luoiTongKet.add(taoLabelNhe("Tiền thừa, cửa hàng hoàn lại:"), 0, row);
            Label lblHoan = new Label("-" + DINH_DANG_TIEN.format(hd.getTienHoanLai()));
            lblHoan.setStyle("-fx-font-weight: bold; -fx-text-fill: #dc2626;");
            luoiTongKet.add(lblHoan, 1, row++);
        }

        Label lblPhanCach4 = taoDuongPhanCach();

        // Footer cảm ơn
        Label lblLoiChuc = new Label("Cảm ơn Quý khách & Hẹn gặp lại!");
        lblLoiChuc.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #1e293b;");
        Label lblPassWifi = new Label("Wifi: HappyTails_Free / Pass: happytails");
        lblPassWifi.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748b;");

        pnl.getChildren().addAll(
                lblBrand, lblSlogan, lblDiaChi, lblHotline,
                lblPhanCach1,
                lblTieuDe, lblMaHd,
                luoiThongTin,
                lblPhanCach2,
                pnlBang,
                lblPhanCach3,
                luoiTongKet,
                lblPhanCach4,
                lblLoiChuc, lblPassWifi
        );

        return pnl;
    }

    private HBox taoDongBang(String ten, int sl, BigDecimal donGia, BigDecimal thanhTien) {
        HBox hang = new HBox(4);
        hang.setAlignment(Pos.CENTER_LEFT);

        Label lblTen = new Label(ten);
        lblTen.setPrefWidth(150);
        lblTen.setWrapText(true);
        lblTen.setStyle("-fx-font-size: 11px;");

        Label lblSl = new Label(String.valueOf(sl));
        lblSl.setPrefWidth(30);
        lblSl.setStyle("-fx-font-size: 11px; -fx-alignment: center-right;");

        Label lblDg = new Label(DINH_DANG_TIEN.format(donGia));
        lblDg.setPrefWidth(80);
        lblDg.setStyle("-fx-font-size: 11px; -fx-alignment: center-right;");

        Label lblTt = new Label(DINH_DANG_TIEN.format(thanhTien));
        lblTt.setPrefWidth(90);
        lblTt.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-alignment: center-right;");

        hang.getChildren().addAll(lblTen, lblSl, lblDg, lblTt);
        return hang;
    }

    private Label taoLabelNhe(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        return lbl;
    }

    private Label taoDuongPhanCach() {
        Label lbl = new Label("-------------------------------------------------------------");
        lbl.setStyle("-fx-text-fill: #94a3b8; -fx-alignment: center;");
        return lbl;
    }
}
