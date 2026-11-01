package com.petshop.view.hoadon;

import com.petshop.model.HoaDon;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.Group;
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

    // Chiều rộng chuẩn khổ A4 trừ lề an toàn mặc định của máy in / Microsoft Print to PDF (487pt)
    private static final double RONG_A4_CHUAN = 475;

    private final String khoangThoiGian;
    private final String trangThai;
    private final String tongDoanhThu;
    private final String doanhThuSanPham;
    private final String doanhThuDichVu;
    private final String tongGiamGia;
    private final String tienMat;
    private final String chuyenKhoan;
    private final String hoaDonHoanTat;
    private final String hoaDonHuy;
    private final String giaTriTrungBinh;
    private final List<HoaDon> danhSachHoaDon;

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

        this.khoangThoiGian = khoangThoiGian;
        this.trangThai = trangThai;
        this.tongDoanhThu = tongDoanhThu;
        this.doanhThuSanPham = doanhThuSanPham;
        this.doanhThuDichVu = doanhThuDichVu;
        this.tongGiamGia = tongGiamGia;
        this.tienMat = tienMat;
        this.chuyenKhoan = chuyenKhoan;
        this.hoaDonHoanTat = hoaDonHoanTat;
        this.hoaDonHuy = hoaDonHuy;
        this.giaTriTrungBinh = giaTriTrungBinh;
        this.danhSachHoaDon = danhSachHoaDon;

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
        btnIn.setOnAction(e -> inBaoCao());

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
        baoCao.setPadding(new Insets(14, 12, 14, 12));
        baoCao.setPrefWidth(RONG_A4_CHUAN);
        baoCao.setMaxWidth(RONG_A4_CHUAN);
        baoCao.setMinWidth(RONG_A4_CHUAN);
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

        // 3. Khung 8 thẻ chỉ số (GridPane 4 cột x 2 hàng, tổng bề ngang vừa khít 450px)
        GridPane chiSo = new GridPane();
        chiSo.setHgap(6);
        chiSo.setVgap(6);
        chiSo.setAlignment(Pos.CENTER);
        chiSo.setMinWidth(450);
        chiSo.setPrefWidth(450);
        chiSo.setMaxWidth(450);
        for (int i = 0; i < 4; i++) {
            chiSo.getColumnConstraints().add(new ColumnConstraints(108, 108, 108));
        }

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
        bang.setMinWidth(450);
        bang.setPrefWidth(450);
        bang.setMaxWidth(450);

        GridPane tieuDeBang = taoHangBang(true);
        bang.getChildren().add(tieuDeBang);

        if (danhSachHoaDon != null) {
            for (int i = 0; i < danhSachHoaDon.size(); i++) {
                bang.getChildren().add(taoHangHoaDon(danhSachHoaDon.get(i), i));
            }
        }

        // 6. Thanh chân bảng thống kê số lượng
        HBox hangChanBang = new HBox();
        hangChanBang.setAlignment(Pos.CENTER_LEFT);
        hangChanBang.setPadding(new Insets(4, 2, 4, 2));
        hangChanBang.setMinWidth(450);
        hangChanBang.setPrefWidth(450);
        hangChanBang.setMaxWidth(450);

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
        hangKy.setPadding(new Insets(14, 10, 8, 10));
        hangKy.setMinWidth(450);
        hangKy.setPrefWidth(450);
        hangKy.setMaxWidth(450);

        VBox boxKyNguoiLap = new VBox(3);
        boxKyNguoiLap.setMinWidth(140);
        boxKyNguoiLap.setPrefWidth(140);
        boxKyNguoiLap.setAlignment(Pos.CENTER);
        Label lblNguoiLap = new Label("Người lập biểu");
        lblNguoiLap.setStyle("-fx-font-weight: bold; -fx-font-size: 10px; -fx-text-fill: #0f172a;");
        Label lblKy1 = new Label("(Ký, ghi rõ họ tên)");
        lblKy1.setStyle("-fx-font-size: 9px; -fx-font-style: italic; -fx-text-fill: #64748b;");
        boxKyNguoiLap.getChildren().addAll(lblNguoiLap, lblKy1);

        Region spacerKy = new Region();
        HBox.setHgrow(spacerKy, Priority.ALWAYS);

        VBox boxKyQuanLy = new VBox(3);
        boxKyQuanLy.setMinWidth(140);
        boxKyQuanLy.setPrefWidth(140);
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
        the.setPadding(new Insets(4, 5, 4, 5));
        the.setPrefWidth(108);
        the.setMaxWidth(108);
        the.setMinWidth(108);
        the.setStyle("-fx-border-color: #cbd5e1; -fx-border-radius: 3; -fx-background-radius: 3; -fx-background-color: #f8fafc;");

        Label nhan = new Label(ten);
        nhan.setStyle("-fx-font-size: 8.5px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        Label gia = new Label(giaTri);
        gia.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + mauGia + ";");

        the.getChildren().addAll(nhan, gia);
        chiSo.add(the, cot, hang);
    }

    private GridPane taoHangBang(boolean laTieuDe) {
        GridPane hang = new GridPane();
        hang.setMinWidth(450);
        hang.setPrefWidth(450);
        hang.setMaxWidth(450);
        // Tổng chiều rộng các cột: 95 + 55 + 105 + 58 + 67 + 70 = 450px (vừa khít 100% lề giấy in A4)
        double[] rong = {95, 55, 105, 58, 67, 70};
        String[] nhan = {"Mã HĐ", "Ngày", "Khách hàng", "Thanh toán", "Trạng thái", "Thực thu"};
        Pos[] canLe = {Pos.CENTER_LEFT, Pos.CENTER, Pos.CENTER_LEFT, Pos.CENTER, Pos.CENTER, Pos.CENTER_RIGHT};

        for (int i = 0; i < nhan.length; i++) {
            hang.getColumnConstraints().add(new ColumnConstraints(rong[i], rong[i], rong[i]));
            Label label = new Label(nhan[i]);
            label.setMinWidth(rong[i]);
            label.setPrefWidth(rong[i]);
            label.setMaxWidth(rong[i]);
            label.setAlignment(canLe[i]);
            label.setStyle(laTieuDe
                    ? "-fx-font-weight: bold; -fx-font-size: 9px; -fx-background-color: #f1f5f9; -fx-padding: 4 3; -fx-border-color: #cbd5e1; -fx-border-width: 0 0 1 0;"
                    : "-fx-font-size: 8.5px; -fx-padding: 3 3;");
            hang.add(label, i, 0);
        }
        return hang;
    }

    private GridPane taoHangHoaDon(HoaDon hoaDon, int index) {
        GridPane hang = new GridPane();
        hang.setMinWidth(450);
        hang.setPrefWidth(450);
        hang.setMaxWidth(450);
        String bg = (index % 2 == 1) ? "#f8fafc" : "#ffffff";
        hang.setStyle("-fx-background-color: " + bg + "; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
        double[] rong = {95, 55, 105, 58, 67, 70};
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
            hang.getColumnConstraints().add(new ColumnConstraints(rong[i], rong[i], rong[i]));
            Label label = new Label(giaTri[i]);
            label.setMinWidth(rong[i]);
            label.setPrefWidth(rong[i]);
            label.setMaxWidth(rong[i]);
            label.setAlignment(canLe[i]);

            StringBuilder style = new StringBuilder("-fx-font-size: 8.5px; -fx-padding: 3 3;");
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
     * Sử dụng bản in độc lập trong Group để căn lề tuyệt đối (0, 0),
     * triệt tiêu toàn bộ độ lệch từ cửa sổ xem trước, căn đều hai bên khổ giấy A4.
     */
    private void inBaoCao() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(this)) {
            PageLayout pageLayout = job.getJobSettings().getPageLayout();
            double printableWidth = pageLayout.getPrintableWidth();

            // Tạo một bản in độc lập hoàn toàn, không phụ thuộc vào vị trí trong Stage/ScrollPane
            VBox banIn = taoNoiDungBaoCao(
                    khoangThoiGian, trangThai, tongDoanhThu, doanhThuSanPham, doanhThuDichVu,
                    tongGiamGia, tienMat, chuyenKhoan, hoaDonHoanTat, hoaDonHuy,
                    giaTriTrungBinh, danhSachHoaDon
            );
            // Gỡ bỏ viền xám và hiệu ứng bóng đổ (chỉ dùng cho xem trước trên màn hình)
            banIn.setStyle("-fx-background-color: #ffffff; -fx-border-width: 0;");
            banIn.setEffect(null);

            // Đặt vào Group để layoutX = 0, layoutY = 0 tuyệt đối, căn đều lề trang in chuẩn xác
            Group printRoot = new Group(banIn);

            Scale scaleTransform = null;
            if (printableWidth < RONG_A4_CHUAN) {
                double scale = printableWidth / RONG_A4_CHUAN;
                scaleTransform = new Scale(scale, scale, 0, 0);
                printRoot.getTransforms().add(scaleTransform);
            }

            try {
                boolean thanhCong = job.printPage(pageLayout, printRoot);
                if (thanhCong) {
                    job.endJob();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Đã gửi lệnh in / xuất PDF báo cáo thành công!");
                    alert.setHeaderText(null);
                    alert.showAndWait();
                    close();
                } else {
                    job.cancelJob();
                }
            } catch (Exception ex) {
                job.cancelJob();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Lỗi khi in báo cáo: " + ex.getMessage());
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
