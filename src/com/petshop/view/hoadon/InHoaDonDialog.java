package com.petshop.view.hoadon;

import com.petshop.model.ChiTietHoaDonDichVu;
import com.petshop.model.ChiTietHoaDonSanPham;
import com.petshop.model.HoaDon;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.transform.Scale;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

/**
 * Hộp thoại hiển thị và in hóa đơn bán lẻ chuẩn khổ A6 / K80 (kiểu Bách Hóa Xanh / WinMart).
 * Thiết kế nhỏ gọn, hiện đại, tối ưu cho máy in bill nhiệt hoặc in khổ A6/A4.
 */
public class InHoaDonDialog extends Stage {

    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0đ");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm");

    // Khổ A6 chuẩn (~105mm x 148mm = 297pt x 420pt), bề ngang 280pt vừa khít A6 & bill nhiệt 80mm
    private static final double RONG_HOA_DON = 280;
    private static final double RONG_NOI_DUNG = 250;

    private final HoaDon hoaDon;

    public InHoaDonDialog(HoaDon hoaDon, Stage chuSoHuu) {
        this.hoaDon = hoaDon;

        initModality(Modality.APPLICATION_MODAL);
        if (chuSoHuu != null) initOwner(chuSoHuu);
        setTitle("Hóa đơn bán lẻ A6 - " + hoaDon.getMaHoaDon());

        VBox pnlHoaDonGiay = taoNoiDungHoaDon(hoaDon, false);

        StackPane khungToGiay = new StackPane(pnlHoaDonGiay);
        khungToGiay.setPadding(new Insets(14));
        khungToGiay.setAlignment(Pos.TOP_CENTER);
        khungToGiay.setStyle("-fx-background-color: #cbd5e1;");

        ScrollPane spCuon = new ScrollPane(khungToGiay);
        spCuon.setFitToWidth(true);
        spCuon.setStyle("-fx-background: #cbd5e1; -fx-background-color: #cbd5e1;");

        Button btnIn = new Button("🖨 In hóa đơn");
        btnIn.getStyleClass().add("nut-chinh");
        btnIn.setStyle("-fx-font-weight: bold; -fx-padding: 6 16;");
        btnIn.setOnAction(e -> inHoaDon());

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

        Scene scene = new Scene(pnlGoc, 390, 650);
        setScene(scene);
        setMinWidth(360);
        setMinHeight(520);
    }

    private VBox taoNoiDungHoaDon(HoaDon hd, boolean laBanIn) {
        VBox pnl = new VBox(6);
        pnl.setPadding(new Insets(14, 15, 14, 15));
        pnl.setMinWidth(RONG_HOA_DON);
        pnl.setPrefWidth(RONG_HOA_DON);
        pnl.setMaxWidth(RONG_HOA_DON);

        if (laBanIn) {
            pnl.setStyle("-fx-background-color: #ffffff; -fx-border-width: 0;");
        } else {
            pnl.setStyle("-fx-background-color: #ffffff; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.12), 5, 0, 0, 2); -fx-border-color: #cbd5e1; -fx-border-width: 1;");
        }
        pnl.setAlignment(Pos.TOP_CENTER);

        // 1. Header thương hiệu (kiểu siêu thị / Bách Hóa Xanh)
        Label lblBrand = new Label("🐾 HAPPY TAILS PET SHOP 🐾");
        lblBrand.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold; -fx-text-fill: #0369a1;");

        Label lblSlogan = new Label("Chăm sóc trọn vẹn - Yêu thương thú cưng");
        lblSlogan.setStyle("-fx-font-size: 9px; -fx-text-fill: #64748b; -fx-font-style: italic;");

        Label lblDiaChi = new Label("Đ/C: 12 Nguyễn Văn Bảo, P.4, Q.Gò Vấp, TP.HCM");
        lblDiaChi.setStyle("-fx-font-size: 9px; -fx-text-fill: #475569;");
        Label lblHotline = new Label("Hotline: 0909.000.000");
        lblHotline.setStyle("-fx-font-size: 9px; -fx-text-fill: #475569; -fx-font-weight: bold;");

        Region lblPhanCach1 = taoDuongPhanCach();

        // 2. Tiêu đề và mã hóa đơn
        Label lblTieuDe = new Label("HÓA ĐƠN BÁN LẺ");
        lblTieuDe.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label lblMaHd = new Label("Số: " + hd.getMaHoaDon());
        lblMaHd.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #0284c7;");

        // 3. Thông tin chung
        GridPane luoiThongTin = new GridPane();
        luoiThongTin.setHgap(6);
        luoiThongTin.setVgap(3);
        luoiThongTin.setMinWidth(RONG_NOI_DUNG);
        luoiThongTin.setPrefWidth(RONG_NOI_DUNG);
        luoiThongTin.setMaxWidth(RONG_NOI_DUNG);
        luoiThongTin.getColumnConstraints().addAll(
                new ColumnConstraints(68),
                new ColumnConstraints(176)
        );

        String thoiGian = "--";
        if (hd.getNgayLap() != null) {
            thoiGian = hd.getNgayLap().format(DINH_DANG_NGAY);
            if (hd.getGioLap() != null) {
                thoiGian += " " + hd.getGioLap().format(DINH_DANG_GIO);
            }
        }
        luoiThongTin.add(taoLabelNhe("Thời gian:"), 0, 0);
        Label lblThoiGian = new Label(thoiGian);
        lblThoiGian.setStyle("-fx-font-size: 9.5px; -fx-text-fill: #1e293b;");
        luoiThongTin.add(lblThoiGian, 1, 0);

        luoiThongTin.add(taoLabelNhe("Thu ngân:"), 0, 1);
        Label lblNv = new Label(hd.getTenNhanVien() != null ? hd.getTenNhanVien() : hd.getMaNhanVien());
        lblNv.setStyle("-fx-font-size: 9.5px; -fx-text-fill: #1e293b;");
        luoiThongTin.add(lblNv, 1, 1);

        luoiThongTin.add(taoLabelNhe("Khách hàng:"), 0, 2);
        String kh = hd.getTenKhachHang() != null ? hd.getTenKhachHang() : "Khách vãng lai";
        if (hd.getSoDienThoaiKhachHang() != null && !hd.getSoDienThoaiKhachHang().isBlank() && !"--".equals(hd.getSoDienThoaiKhachHang())) {
            kh += " (" + hd.getSoDienThoaiKhachHang() + ")";
        }
        Label lblKh = new Label(kh);
        lblKh.setStyle("-fx-font-size: 9.5px; -fx-text-fill: #1e293b;");
        luoiThongTin.add(lblKh, 1, 2);

        if (hd.getMaPhieuDichVu() != null && !hd.getMaPhieuDichVu().isBlank()) {
            luoiThongTin.add(taoLabelNhe("Phiếu DV:"), 0, 3);
            Label lblPdv = new Label(hd.getMaPhieuDichVu());
            lblPdv.setStyle("-fx-font-size: 9.5px; -fx-text-fill: #1e293b;");
            luoiThongTin.add(lblPdv, 1, 3);
        }

        Region lblPhanCach2 = taoDuongPhanCach();

        // 4. Bảng danh sách mặt hàng
        VBox pnlBang = new VBox(3);
        pnlBang.setAlignment(Pos.TOP_LEFT);
        pnlBang.setMinWidth(RONG_NOI_DUNG);
        pnlBang.setPrefWidth(RONG_NOI_DUNG);
        pnlBang.setMaxWidth(RONG_NOI_DUNG);

        pnlBang.getChildren().add(taoHangTieuDeBang());

        // Mặt hàng sản phẩm
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

        // Dịch vụ chăm sóc
        if (hd.getDanhSachDichVu() != null) {
            for (ChiTietHoaDonDichVu dv : hd.getDanhSachDichVu()) {
                String ten = (dv.getTenDichVu() != null ? dv.getTenDichVu() : dv.getMaDichVu()) + " [DV]";
                pnlBang.getChildren().add(taoDongBang(ten, 1, dv.getDonGia(), dv.getThanhTien()));
            }
        }

        Region lblPhanCach3 = taoDuongPhanCach();

        // 5. Tổng kết thanh toán
        GridPane luoiTongKet = new GridPane();
        luoiTongKet.setHgap(6);
        luoiTongKet.setVgap(3);
        luoiTongKet.setAlignment(Pos.CENTER_RIGHT);
        luoiTongKet.setMinWidth(RONG_NOI_DUNG);
        luoiTongKet.setPrefWidth(RONG_NOI_DUNG);
        luoiTongKet.setMaxWidth(RONG_NOI_DUNG);
        luoiTongKet.getColumnConstraints().addAll(
                new ColumnConstraints(140),
                new ColumnConstraints(104)
        );

        int row = 0;
        BigDecimal tongGoc = hd.getTongTienGoc();
        luoiTongKet.add(taoLabelNhe("Tổng tiền hàng:"), 0, row);
        Label lblTongGoc = new Label(DINH_DANG_TIEN.format(tongGoc));
        lblTongGoc.setAlignment(Pos.CENTER_RIGHT);
        lblTongGoc.setMaxWidth(Double.MAX_VALUE);
        lblTongGoc.setStyle("-fx-font-size: 9.5px; -fx-text-fill: #1e293b;");
        luoiTongKet.add(lblTongGoc, 1, row++);

        if (hd.getTienGiam() != null && hd.getTienGiam().compareTo(BigDecimal.ZERO) > 0) {
            String tenKm = hd.getTenKhuyenMai() != null && !hd.getTenKhuyenMai().isBlank()
                    ? hd.getTenKhuyenMai()
                    : (hd.getMaKhuyenMai() != null ? hd.getMaKhuyenMai() : "KM");
            luoiTongKet.add(taoLabelNhe("Giảm giá (" + tenKm + "):"), 0, row);
            Label lblGiam = new Label("-" + DINH_DANG_TIEN.format(hd.getTienGiam()));
            lblGiam.setAlignment(Pos.CENTER_RIGHT);
            lblGiam.setMaxWidth(Double.MAX_VALUE);
            lblGiam.setStyle("-fx-font-size: 9.5px; -fx-text-fill: #dc2626;");
            luoiTongKet.add(lblGiam, 1, row++);
        }

        if (hd.getDiemTichLuySuDung() > 0) {
            luoiTongKet.add(taoLabelNhe("Dùng điểm (" + hd.getDiemTichLuySuDung() + "đ):"), 0, row);
            Label lblGiamDiem = new Label("-" + DINH_DANG_TIEN.format(hd.getTienGiamTuDiem()));
            lblGiamDiem.setAlignment(Pos.CENTER_RIGHT);
            lblGiamDiem.setMaxWidth(Double.MAX_VALUE);
            lblGiamDiem.setStyle("-fx-font-size: 9.5px; -fx-text-fill: #dc2626;");
            luoiTongKet.add(lblGiamDiem, 1, row++);
        }

        Label lblNhanThanhToan = new Label("THANH TOÁN:");
        lblNhanThanhToan.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        luoiTongKet.add(lblNhanThanhToan, 0, row);

        Label lblThanhToan = new Label(DINH_DANG_TIEN.format(hd.getTongTien()));
        lblThanhToan.setAlignment(Pos.CENTER_RIGHT);
        lblThanhToan.setMaxWidth(Double.MAX_VALUE);
        lblThanhToan.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0284c7;");
        luoiTongKet.add(lblThanhToan, 1, row++);

        luoiTongKet.add(taoLabelNhe("Phương thức:"), 0, row);
        String ht = hd.getHinhThucThanhToan() != null ? hd.getHinhThucThanhToan() : "Tiền mặt";
        Label lblHt = new Label(ht);
        lblHt.setAlignment(Pos.CENTER_RIGHT);
        lblHt.setMaxWidth(Double.MAX_VALUE);
        lblHt.setStyle("-fx-font-size: 9.5px; -fx-text-fill: #1e293b;");
        luoiTongKet.add(lblHt, 1, row++);

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
            Label lblDua = new Label(DINH_DANG_TIEN.format(tienDua));
            lblDua.setAlignment(Pos.CENTER_RIGHT);
            lblDua.setMaxWidth(Double.MAX_VALUE);
            lblDua.setStyle("-fx-font-size: 9.5px;");
            luoiTongKet.add(lblDua, 1, row++);

            luoiTongKet.add(taoLabelNhe("Tiền thối lại:"), 0, row);
            Label lblThoi = new Label(DINH_DANG_TIEN.format(tienThoi));
            lblThoi.setAlignment(Pos.CENTER_RIGHT);
            lblThoi.setMaxWidth(Double.MAX_VALUE);
            lblThoi.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold; -fx-text-fill: #15803d;");
            luoiTongKet.add(lblThoi, 1, row++);
        }

        if (hd.getDiemTichLuyCong() > 0) {
            luoiTongKet.add(taoLabelNhe("Điểm tích lũy mới:"), 0, row);
            Label lblDiemMoi = new Label("+" + hd.getDiemTichLuyCong() + " điểm");
            lblDiemMoi.setAlignment(Pos.CENTER_RIGHT);
            lblDiemMoi.setMaxWidth(Double.MAX_VALUE);
            lblDiemMoi.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold; -fx-text-fill: #0284c7;");
            luoiTongKet.add(lblDiemMoi, 1, row++);
        }

        if (hd.getTienThanhToanThem().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tongHoaDonGoc = hd.getTongTien().subtract(hd.getTienThanhToanThem());
            luoiTongKet.add(taoLabelNhe("Đã thanh toán trước:"), 0, row);
            Label lblGoc = new Label(DINH_DANG_TIEN.format(tongHoaDonGoc));
            lblGoc.setAlignment(Pos.CENTER_RIGHT);
            lblGoc.setMaxWidth(Double.MAX_VALUE);
            luoiTongKet.add(lblGoc, 1, row++);

            luoiTongKet.add(taoLabelNhe("Khách thanh toán thêm:"), 0, row);
            Label lblThem = new Label("+" + DINH_DANG_TIEN.format(hd.getTienThanhToanThem()));
            lblThem.setAlignment(Pos.CENTER_RIGHT);
            lblThem.setMaxWidth(Double.MAX_VALUE);
            lblThem.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold; -fx-text-fill: #0284c7;");
            luoiTongKet.add(lblThem, 1, row++);
        } else if (hd.getTienHoanLai().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tongHoaDonGoc = hd.getTongTien().add(hd.getTienHoanLai());
            luoiTongKet.add(taoLabelNhe("Đã thanh toán trước:"), 0, row);
            Label lblGoc = new Label(DINH_DANG_TIEN.format(tongHoaDonGoc));
            lblGoc.setAlignment(Pos.CENTER_RIGHT);
            lblGoc.setMaxWidth(Double.MAX_VALUE);
            luoiTongKet.add(lblGoc, 1, row++);

            luoiTongKet.add(taoLabelNhe("Hoàn lại tiền mặt:"), 0, row);
            Label lblHoan = new Label("-" + DINH_DANG_TIEN.format(hd.getTienHoanLai()));
            lblHoan.setAlignment(Pos.CENTER_RIGHT);
            lblHoan.setMaxWidth(Double.MAX_VALUE);
            lblHoan.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold; -fx-text-fill: #dc2626;");
            luoiTongKet.add(lblHoan, 1, row++);
        }

        Region lblPhanCach4 = taoDuongPhanCach();

        // 6. Footer thông điệp và quy định siêu thị
        Label lblLoiChuc = new Label("Cảm ơn Quý khách & Hẹn gặp lại!");
        lblLoiChuc.setStyle("-fx-font-weight: bold; -fx-font-size: 10.5px; -fx-text-fill: #0f172a;");

        Label lblKiemTra = new Label("Quý khách vui lòng kiểm tra hóa đơn trước khi rời quầy.");
        lblKiemTra.setStyle("-fx-font-size: 8.5px; -fx-text-fill: #64748b; -fx-font-style: italic;");

        Label lblDoiTra = new Label("Đổi trả trong vòng 03 ngày (kèm hóa đơn và hàng nguyên tem).");
        lblDoiTra.setStyle("-fx-font-size: 8.5px; -fx-text-fill: #64748b; -fx-font-style: italic;");

        Label lblPassWifi = new Label("Wifi: HappyTails_Free  |  Pass: happytails");
        lblPassWifi.setStyle("-fx-font-size: 8.5px; -fx-text-fill: #475569;");

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
                lblLoiChuc, lblKiemTra, lblDoiTra, lblPassWifi
        );

        return pnl;
    }

    private GridPane taoHangTieuDeBang() {
        GridPane hang = new GridPane();
        hang.setMinWidth(RONG_NOI_DUNG);
        hang.setPrefWidth(RONG_NOI_DUNG);
        hang.setMaxWidth(RONG_NOI_DUNG);
        hang.getColumnConstraints().addAll(
                new ColumnConstraints(116),
                new ColumnConstraints(22),
                new ColumnConstraints(52),
                new ColumnConstraints(60)
        );

        Label h1 = new Label("Tên hàng");
        h1.setStyle("-fx-font-weight: bold; -fx-font-size: 9.5px; -fx-text-fill: #334155;");

        Label h2 = new Label("SL");
        h2.setAlignment(Pos.CENTER_RIGHT);
        h2.setMaxWidth(Double.MAX_VALUE);
        h2.setStyle("-fx-font-weight: bold; -fx-font-size: 9.5px; -fx-text-fill: #334155;");

        Label h3 = new Label("Đ.Giá");
        h3.setAlignment(Pos.CENTER_RIGHT);
        h3.setMaxWidth(Double.MAX_VALUE);
        h3.setStyle("-fx-font-weight: bold; -fx-font-size: 9.5px; -fx-text-fill: #334155;");

        Label h4 = new Label("T.Tiền");
        h4.setAlignment(Pos.CENTER_RIGHT);
        h4.setMaxWidth(Double.MAX_VALUE);
        h4.setStyle("-fx-font-weight: bold; -fx-font-size: 9.5px; -fx-text-fill: #334155;");

        hang.add(h1, 0, 0);
        hang.add(h2, 1, 0);
        hang.add(h3, 2, 0);
        hang.add(h4, 3, 0);
        return hang;
    }

    private GridPane taoDongBang(String ten, int sl, BigDecimal donGia, BigDecimal thanhTien) {
        GridPane hang = new GridPane();
        hang.setMinWidth(RONG_NOI_DUNG);
        hang.setPrefWidth(RONG_NOI_DUNG);
        hang.setMaxWidth(RONG_NOI_DUNG);
        hang.getColumnConstraints().addAll(
                new ColumnConstraints(116),
                new ColumnConstraints(22),
                new ColumnConstraints(52),
                new ColumnConstraints(60)
        );

        Label lblTen = new Label(ten);
        lblTen.setWrapText(true);
        lblTen.setStyle("-fx-font-size: 9px; -fx-text-fill: #1e293b;");

        Label lblSl = new Label(String.valueOf(sl));
        lblSl.setAlignment(Pos.CENTER_RIGHT);
        lblSl.setMaxWidth(Double.MAX_VALUE);
        lblSl.setStyle("-fx-font-size: 9px; -fx-text-fill: #334155;");

        Label lblDg = new Label(DINH_DANG_TIEN.format(donGia));
        lblDg.setAlignment(Pos.CENTER_RIGHT);
        lblDg.setMaxWidth(Double.MAX_VALUE);
        lblDg.setStyle("-fx-font-size: 9px; -fx-text-fill: #334155;");

        Label lblTt = new Label(DINH_DANG_TIEN.format(thanhTien));
        lblTt.setAlignment(Pos.CENTER_RIGHT);
        lblTt.setMaxWidth(Double.MAX_VALUE);
        lblTt.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        hang.add(lblTen, 0, 0);
        hang.add(lblSl, 1, 0);
        hang.add(lblDg, 2, 0);
        hang.add(lblTt, 3, 0);
        return hang;
    }

    private Label taoLabelNhe(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 9.5px; -fx-text-fill: #64748b;");
        return lbl;
    }

    private Region taoDuongPhanCach() {
        Region line = new Region();
        line.setMinWidth(RONG_NOI_DUNG);
        line.setPrefWidth(RONG_NOI_DUNG);
        line.setMaxWidth(RONG_NOI_DUNG);
        line.setStyle("-fx-border-color: #cbd5e1; -fx-border-style: dashed; -fx-border-width: 1 0 0 0; -fx-padding: 2 0;");
        return line;
    }

    /**
     * In hóa đơn ra máy in bill nhiệt K80 hoặc máy in khổ A6 / A4.
     * Căn giữa trang in tự động nếu in trên khổ giấy lớn (A4), vừa khít khổ A6 và giấy cuộn 80mm.
     */
    private void inHoaDon() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(this)) {
            PageLayout pageLayout = job.getJobSettings().getPageLayout();
            double printableWidth = pageLayout.getPrintableWidth();

            VBox banIn = taoNoiDungHoaDon(this.hoaDon, true);

            // Căn giữa hóa đơn trên trang in nếu khổ giấy in lớn hơn 280pt
            double offsetX = 0;
            if (printableWidth > RONG_HOA_DON) {
                offsetX = (printableWidth - RONG_HOA_DON) / 2.0;
            }
            banIn.setLayoutX(offsetX);
            banIn.setLayoutY(0);

            Group printRoot = new Group(banIn);

            Scale scaleTransform = null;
            if (printableWidth < RONG_HOA_DON) {
                double scale = printableWidth / RONG_HOA_DON;
                scaleTransform = new Scale(scale, scale, 0, 0);
                printRoot.getTransforms().add(scaleTransform);
            }

            try {
                boolean success = job.printPage(pageLayout, printRoot);
                if (success) {
                    job.endJob();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Đã gửi lệnh in hóa đơn thành công!");
                    alert.setHeaderText(null);
                    alert.showAndWait();
                    close();
                } else {
                    job.cancelJob();
                }
            } catch (Exception ex) {
                job.cancelJob();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Lỗi khi in hóa đơn: " + ex.getMessage());
                alert.setHeaderText(null);
                alert.showAndWait();
            } finally {
                if (scaleTransform != null) {
                    printRoot.getTransforms().remove(scaleTransform);
                }
            }
        }
    }
}
