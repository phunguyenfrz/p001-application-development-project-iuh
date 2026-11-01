package com.petshop.view.hoadon;

import com.petshop.model.HoaDon;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.transform.Scale;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Cửa sổ xem trước và in / xuất file PDF Báo cáo thống kê doanh thu.
 * Căn chỉnh kích thước chuẩn khổ giấy A4 (portrait), tự động co giãn vừa vặn
 * không bị tràn viền hay mất cột khi in hoặc xuất ra file PDF qua Microsoft Print to PDF.
 */
public class InBaoCaoDoanhThuDialog extends Stage {

    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 VNĐ");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Chiều rộng chuẩn khổ A4 trừ lề an toàn trong JavaFX (72 DPI)
    private static final double RONG_A4_CHUAN = 540;

    public InBaoCaoDoanhThuDialog(
            String khoangThoiGian,
            String trangThai,
            String tongDoanhThu,
            String doanhThuSanPham,
            String doanhThuDichVu,
            String tongGiamGia,
            String tienMat,
            String chuyenKhoan,
            String hoaDonHoanTat,
            String hoaDonHuy,
            String giaTriTrungBinh,
            List<HoaDon> danhSachHoaDon,
            Stage chuSoHuu) {

        initModality(Modality.APPLICATION_MODAL);
        if (chuSoHuu != null) initOwner(chuSoHuu);
        setTitle("Xuất báo cáo doanh thu");

        VBox noiDungBaoCao = taoNoiDungBaoCao(
                khoangThoiGian, trangThai, tongDoanhThu, doanhThuSanPham, doanhThuDichVu,
                tongGiamGia, tienMat, chuyenKhoan, hoaDonHoanTat, hoaDonHuy,
                giaTriTrungBinh, danhSachHoaDon
        );

        // Khung hiển thị mô phỏng tờ giấy A4 trên nền xám
        StackPane khungToGiay = new StackPane(noiDungBaoCao);
        khungToGiay.setPadding(new Insets(16));
        khungToGiay.setAlignment(Pos.TOP_CENTER);
        khungToGiay.setStyle("-fx-background-color: #cbd5e1;");

        ScrollPane cuon = new ScrollPane(khungToGiay);
        cuon.setFitToWidth(true);
        cuon.setStyle("-fx-background: #cbd5e1; -fx-background-color: #cbd5e1;");

        Button btnIn = new Button("🖨 In / Xuất PDF");
        btnIn.getStyleClass().add("nut-chinh");
        btnIn.setStyle("-fx-font-weight: bold; -fx-padding: 6 16;");
        btnIn.setOnAction(e -> inBaoCao(noiDungBaoCao));

        Button btnDong = new Button("Đóng");
        btnDong.getStyleClass().add("nut-phu");
        btnDong.setOnAction(e -> close());

        HBox nut = new HBox(10, btnIn, btnDong);
        nut.setAlignment(Pos.CENTER_RIGHT);
        nut.setPadding(new Insets(10, 16, 12, 16));
        nut.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-width: 1 0 0 0;");

        BorderPane goc = new BorderPane(cuon, null, null, nut, null);
        setScene(new Scene(goc, 760, 680));
        setMinWidth(620);
        setMinHeight(520);
    }

    private VBox taoNoiDungBaoCao(
            String khoangThoiGian,
            String trangThai,
            String tongDoanhThu,
            String doanhThuSanPham,
            String doanhThuDichVu,
            String tongGiamGia,
            String tienMat,
            String chuyenKhoan,
            String hoaDonHoanTat,
            String hoaDonHuy,
            String giaTriTrungBinh,
            List<HoaDon> danhSachHoaDon) {

        VBox baoCao = new VBox(8);
        baoCao.setPadding(new Insets(16, 20, 16, 20));
        baoCao.setPrefWidth(RONG_A4_CHUAN);
        baoCao.setMaxWidth(RONG_A4_CHUAN);
        baoCao.setStyle("-fx-background-color: #ffffff; -fx-border-color: #94a3b8; -fx-border-width: 1; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 6, 0, 0, 2);");
        baoCao.setAlignment(Pos.TOP_CENTER);

        // 1. Thương hiệu cửa hàng
        Label lblBrand = new Label("🐾 HAPPY TAILS PET SHOP 🐾");
        lblBrand.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0369a1;");

        Label lblDiaChi = new Label("12 Nguyễn Văn Bảo, Phường 4, Gò Vấp, TP.HCM  |  Hotline: 0909.000.000");
        lblDiaChi.setStyle("-fx-font-size: 9.5px; -fx-text-fill: #64748b;");

        Label lblPhanCach1 = taoDuongPhanCach();

        // 2. Tiêu đề báo cáo
        Label tieuDe = new Label("BÁO CÁO THỐNG KÊ DOANH THU");
        tieuDe.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label thongTinLoc = new Label("Thời gian: " + khoangThoiGian + "  |  Trạng thái: "
                + (trangThai == null ? "Tất cả trạng thái" : trangThai));
        thongTinLoc.setStyle("-fx-font-size: 10px; -fx-text-fill: #475569; -fx-font-style: italic;");

        // 3. Khung 8 thẻ chỉ số (GridPane 4 cột x 2 hàng, tổng bề ngang vừa khít 500px)
        GridPane chiSo = new GridPane();
        chiSo.setHgap(6);
        chiSo.setVgap(6);
        chiSo.setAlignment(Pos.CENTER);

        themChiSo(chiSo, 0, 0, "Tổng doanh thu thực", tongDoanhThu, "#0284c7");
        themChiSo(chiSo, 1, 0, "Doanh thu sản phẩm", doanhThuSanPham, "#7c3aed");
        themChiSo(chiSo, 2, 0, "Doanh thu dịch vụ", doanhThuDichVu, "#2563eb");
        themChiSo(chiSo, 3, 0, "Tổng tiền giảm giá", tongGiamGia, "#ea580c");

        themChiSo(chiSo, 0, 1, "Tiền mặt", tienMat, "#16a34a");
        themChiSo(chiSo, 1, 1, "Chuyển khoản", chuyenKhoan, "#0d9488");
        themChiSo(chiSo, 2, 1, "Đơn hoàn tất", hoaDonHoanTat, "#1e293b");
        themChiSo(chiSo, 3, 1, "Đơn đã hủy", hoaDonHuy, "#dc2626");

        // 4. Giá trị đơn trung bình
        Label aov = new Label("Giá trị trung bình / đơn (AOV): " + giaTriTrungBinh);
        aov.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #4338ca; -fx-padding: 2 0;");

        // 5. Bảng danh sách hóa đơn chi tiết
        VBox bang = new VBox(0);
        bang.setAlignment(Pos.TOP_LEFT);
        bang.setStyle("-fx-border-color: #cbd5e1; -fx-border-width: 1;");

        GridPane tieuDeBang = taoHangBang(true);
        bang.getChildren().add(tieuDeBang);

        if (danhSachHoaDon != null) {
            for (HoaDon hoaDon : danhSachHoaDon) {
                bang.getChildren().add(taoHangHoaDon(hoaDon));
            }
        }

        // 6. Thanh chân bảng thống kê số lượng
        HBox hangChanBang = new HBox();
        hangChanBang.setAlignment(Pos.CENTER_LEFT);
        hangChanBang.setPadding(new Insets(4, 2, 4, 2));

        Label chanBang = new Label("Tổng số hóa đơn: " + (danhSachHoaDon == null ? 0 : danhSachHoaDon.size()));
        chanBang.setStyle("-fx-font-weight: bold; -fx-font-size: 10px; -fx-text-fill: #334155;");

        Region spacerChan = new Region();
        HBox.setHgrow(spacerChan, Priority.ALWAYS);

        String gioHienTai = LocalDateTime.now().format(DINH_DANG_GIO);
        Label lblNgayIn = new Label("Ngày xuất: " + gioHienTai);
        lblNgayIn.setStyle("-fx-font-size: 9.5px; -fx-text-fill: #64748b; -fx-font-style: italic;");
        hangChanBang.getChildren().addAll(chanBang, spacerChan, lblNgayIn);

        // 7. Khu vực chữ ký xác nhận
        HBox hangKy = new HBox();
        hangKy.setPadding(new Insets(16, 24, 8, 24));

        VBox boxKyNguoiLap = new VBox(3);
        boxKyNguoiLap.setAlignment(Pos.CENTER);
        Label lblNguoiLap = new Label("Người lập biểu");
        lblNguoiLap.setStyle("-fx-font-weight: bold; -fx-font-size: 10px; -fx-text-fill: #0f172a;");
        Label lblKy1 = new Label("(Ký, ghi rõ họ tên)");
        lblKy1.setStyle("-fx-font-size: 9px; -fx-font-style: italic; -fx-text-fill: #64748b;");
        boxKyNguoiLap.getChildren().addAll(lblNguoiLap, lblKy1);

        Region spacerKy = new Region();
        HBox.setHgrow(spacerKy, Priority.ALWAYS);

        VBox boxKyQuanLy = new VBox(3);
        boxKyQuanLy.setAlignment(Pos.CENTER);
        Label lblQuanLy = new Label("Quản lý cửa hàng");
        lblQuanLy.setStyle("-fx-font-weight: bold; -fx-font-size: 10px; -fx-text-fill: #0f172a;");
        Label lblKy2 = new Label("(Ký, đóng dấu)");
        lblKy2.setStyle("-fx-font-size: 9px; -fx-font-style: italic; -fx-text-fill: #64748b;");
        boxKyQuanLy.getChildren().addAll(lblQuanLy, lblKy2);

        hangKy.getChildren().addAll(boxKyNguoiLap, spacerKy, boxKyQuanLy);

        baoCao.getChildren().addAll(
                lblBrand, lblDiaChi, lblPhanCach1,
                tieuDe, thongTinLoc,
                chiSo, aov,
                bang, hangChanBang,
                hangKy
        );
        return baoCao;
    }

    private void themChiSo(GridPane chiSo, int cot, int hang, String ten, String giaTri, String mauGia) {
        VBox the = new VBox(2);
        the.setPadding(new Insets(5, 6, 5, 6));
        the.setPrefWidth(120);
        the.setMaxWidth(120);
        the.setStyle("-fx-border-color: #cbd5e1; -fx-border-radius: 3; -fx-background-radius: 3; -fx-background-color: #f8fafc;");

        Label nhan = new Label(ten);
        nhan.setStyle("-fx-font-size: 9px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        Label gia = new Label(giaTri);
        gia.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: " + mauGia + ";");

        the.getChildren().addAll(nhan, gia);
        chiSo.add(the, cot, hang);
    }

    private GridPane taoHangBang(boolean laTieuDe) {
        GridPane hang = new GridPane();
        // Tổng chiều rộng các cột: 120 + 68 + 122 + 65 + 55 + 70 = 500px (vừa khít A4)
        double[] rong = {120, 68, 122, 65, 55, 70};
        String[] nhan = {"Mã HĐ", "Ngày", "Khách hàng", "Thanh toán", "Trạng thái", "Thực thu"};
        Pos[] canLe = {Pos.CENTER_LEFT, Pos.CENTER, Pos.CENTER_LEFT, Pos.CENTER, Pos.CENTER, Pos.CENTER_RIGHT};

        for (int i = 0; i < nhan.length; i++) {
            Label label = new Label(nhan[i]);
            label.setPrefWidth(rong[i]);
            label.setMaxWidth(rong[i]);
            label.setAlignment(canLe[i]);
            label.setStyle(laTieuDe
                    ? "-fx-font-weight: bold; -fx-font-size: 9.5px; -fx-background-color: #f1f5f9; -fx-padding: 4 4; -fx-border-color: #cbd5e1; -fx-border-width: 0 0 1 0;"
                    : "-fx-font-size: 9px; -fx-padding: 3 4;");
            hang.add(label, i, 0);
        }
        return hang;
    }

    private GridPane taoHangHoaDon(HoaDon hoaDon) {
        GridPane hang = new GridPane();
        hang.setStyle("-fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
        double[] rong = {120, 68, 122, 65, 55, 70};
        Pos[] canLe = {Pos.CENTER_LEFT, Pos.CENTER, Pos.CENTER_LEFT, Pos.CENTER, Pos.CENTER, Pos.CENTER_RIGHT};

        String ngay = hoaDon.getNgayLap() != null ? hoaDon.getNgayLap().format(DINH_DANG_NGAY) : "";
        String trangThai = hoaDon.getTrangThaiThanhToan() != null ? hoaDon.getTrangThaiThanhToan() : "";
        BigDecimal thucThu = "Đã hủy".equalsIgnoreCase(trangThai) ? BigDecimal.ZERO : hoaDon.getTongTien();

        String tenKhach = hoaDon.getTenKhachHang() != null ? hoaDon.getTenKhachHang() : "Khách vãng lai";
        String sdt = hoaDon.getSoDienThoaiKhachHang();
        if (sdt != null && !sdt.isEmpty() && !"--".equals(sdt)) {
            tenKhach += " (" + sdt + ")";
        }

        String[] giaTri = {
                hoaDon.getMaHoaDon(),
                ngay,
                tenKhach,
                hoaDon.getHinhThucThanhToan() != null ? hoaDon.getHinhThucThanhToan() : "",
                trangThai,
                DINH_DANG_TIEN.format(thucThu)
        };

        for (int i = 0; i < giaTri.length; i++) {
            Label label = new Label(giaTri[i]);
            label.setPrefWidth(rong[i]);
            label.setMaxWidth(rong[i]);
            label.setAlignment(canLe[i]);

            StringBuilder style = new StringBuilder("-fx-font-size: 9px; -fx-padding: 3 4;");
            if (i == 0) {
                style.append(" -fx-font-weight: bold; -fx-text-fill: #0f172a;");
            } else if (i == 4) {
                if ("Đã thanh toán".equalsIgnoreCase(trangThai)) {
                    style.append(" -fx-text-fill: #15803d; -fx-font-weight: bold;");
                } else if ("Đã hủy".equalsIgnoreCase(trangThai)) {
                    style.append(" -fx-text-fill: #dc2626; -fx-font-weight: bold;");
                }
            } else if (i == 5) {
                style.append(" -fx-font-weight: bold;");
            }
            label.setStyle(style.toString());
            hang.add(label, i, 0);
        }
        return hang;
    }

    private Label taoDuongPhanCach() {
        Label lbl = new Label();
        lbl.setMaxWidth(Double.MAX_VALUE);
        lbl.setStyle("-fx-border-color: #cbd5e1; -fx-border-width: 1 0 0 0; -fx-padding: 2 0;");
        return lbl;
    }

    /**
     * Thực hiện in hoặc xuất sang file PDF qua PrinterJob.
     * Tự động tính toán Scale Transform co giãn vừa khít 100% lề giấy in A4,
     * tuyệt đối không để tràn viền hay mất cột bên phải.
     */
    private void inBaoCao(VBox noiDungBaoCao) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(this)) {
            PageLayout pageLayout = job.getJobSettings().getPageLayout();
            double printableWidth = pageLayout.getPrintableWidth();

            noiDungBaoCao.applyCss();
            noiDungBaoCao.layout();
            double nodeWidth = noiDungBaoCao.getBoundsInParent().getWidth();
            if (nodeWidth <= 0) nodeWidth = RONG_A4_CHUAN;

            // Tính tỷ lệ co giãn để vừa khít 100% bề rộng trang in
            double scale = printableWidth / nodeWidth;
            if (scale > 1.0) scale = 1.0;

            Scale scaleTransform = new Scale(scale, scale, 0, 0);
            noiDungBaoCao.getTransforms().add(scaleTransform);

            try {
                boolean thanhCong = job.printPage(pageLayout, noiDungBaoCao);
                if (thanhCong) {
                    job.endJob();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Đã gửi lệnh in / xuất PDF báo cáo thành công!");
                    alert.setHeaderText(null);
                    alert.showAndWait();
                    close();
                } else {
                    job.cancelJob();
                }
            } finally {
                noiDungBaoCao.getTransforms().remove(scaleTransform);
            }
        }
    }
}
