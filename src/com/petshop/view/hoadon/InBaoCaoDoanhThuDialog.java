package com.petshop.view.hoadon;

import com.petshop.model.HoaDon;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

public class InBaoCaoDoanhThuDialog extends Stage {

    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 VNĐ");

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
        ScrollPane cuon = new ScrollPane(noiDungBaoCao);
        cuon.setFitToWidth(true);
        cuon.setStyle("-fx-background: #e2e8f0; -fx-background-color: #e2e8f0;");

        Button btnIn = new Button("🖨 In báo cáo");
        btnIn.getStyleClass().add("nut-chinh");
        btnIn.setOnAction(e -> inBaoCao(noiDungBaoCao));

        Button btnDong = new Button("Đóng");
        btnDong.getStyleClass().add("nut-phu");
        btnDong.setOnAction(e -> close());

        HBox nut = new HBox(10, btnIn, btnDong);
        nut.setAlignment(Pos.CENTER_RIGHT);
        nut.setPadding(new Insets(10, 16, 12, 16));
        nut.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-width: 1 0 0 0;");

        BorderPane goc = new BorderPane(cuon, null, null, nut, null);
        setScene(new Scene(goc, 900, 680));
        setMinWidth(760);
        setMinHeight(560);
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
        VBox baoCao = new VBox(10);
        baoCao.setPadding(new Insets(24));
        baoCao.setMaxWidth(820);
        baoCao.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cbd5e1;");
        baoCao.setAlignment(Pos.TOP_CENTER);

        Label tieuDe = new Label("BÁO CÁO THỐNG KÊ DOANH THU");
        tieuDe.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        Label thongTinLoc = new Label("Thời gian: " + khoangThoiGian + "    |    Trạng thái: "
                + (trangThai == null ? "Tất cả trạng thái" : trangThai));
        thongTinLoc.setStyle("-fx-text-fill: #475569;");

        GridPane chiSo = new GridPane();
        chiSo.setHgap(12);
        chiSo.setVgap(8);
        chiSo.setAlignment(Pos.CENTER);
        themChiSo(chiSo, 0, 0, "Tổng doanh thu thực thu", tongDoanhThu);
        themChiSo(chiSo, 1, 0, "Doanh thu sản phẩm", doanhThuSanPham);
        themChiSo(chiSo, 2, 0, "Doanh thu dịch vụ", doanhThuDichVu);
        themChiSo(chiSo, 3, 0, "Tổng tiền giảm giá", tongGiamGia);
        themChiSo(chiSo, 0, 1, "Tiền mặt", tienMat);
        themChiSo(chiSo, 1, 1, "Chuyển khoản", chuyenKhoan);
        themChiSo(chiSo, 2, 1, "Đơn hoàn tất", hoaDonHoanTat);
        themChiSo(chiSo, 3, 1, "Đơn đã hủy", hoaDonHuy);

        Label aov = new Label("Giá trị trung bình / đơn: " + giaTriTrungBinh);
        aov.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #4338ca;");

        VBox bang = new VBox(4);
        bang.setAlignment(Pos.TOP_LEFT);
        GridPane tieuDeBang = taoHangBang(true);
        bang.getChildren().add(tieuDeBang);
        if (danhSachHoaDon != null) {
            for (HoaDon hoaDon : danhSachHoaDon) {
                bang.getChildren().add(taoHangHoaDon(hoaDon));
            }
        }

        Label chanBang = new Label("Tổng số hóa đơn: " + (danhSachHoaDon == null ? 0 : danhSachHoaDon.size()));
        chanBang.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");

        baoCao.getChildren().addAll(tieuDe, thongTinLoc, chiSo, aov, bang, chanBang);
        return baoCao;
    }

    private void themChiSo(GridPane chiSo, int cot, int hang, String ten, String giaTri) {
        VBox the = new VBox(3);
        the.setPadding(new Insets(8));
        the.setPrefWidth(180);
        the.setStyle("-fx-border-color: #cbd5e1; -fx-border-radius: 4; -fx-background-radius: 4;");
        Label nhan = new Label(ten);
        nhan.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748b;");
        Label gia = new Label(giaTri);
        gia.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0369a1;");
        the.getChildren().addAll(nhan, gia);
        chiSo.add(the, cot, hang);
    }

    private GridPane taoHangBang(boolean laTieuDe) {
        GridPane hang = new GridPane();
        hang.setHgap(6);
        double[] rong = {90, 90, 140, 100, 110, 110};
        String[] nhan = {"Mã HĐ", "Ngày", "Khách hàng", "Thanh toán", "Trạng thái", "Thực thu"};
        for (int i = 0; i < nhan.length; i++) {
            Label label = new Label(nhan[i]);
            label.setPrefWidth(rong[i]);
            label.setStyle(laTieuDe
                    ? "-fx-font-weight: bold; -fx-background-color: #e2e8f0; -fx-padding: 5;"
                    : "-fx-padding: 4;");
            hang.add(label, i, 0);
        }
        if (!laTieuDe) {
            hang.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");
        }
        return hang;
    }

    private GridPane taoHangHoaDon(HoaDon hoaDon) {
        GridPane hang = new GridPane();
        hang.setHgap(6);
        hang.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");
        double[] rong = {90, 90, 140, 100, 110, 110};
        String ngay = hoaDon.getNgayLap() != null ? hoaDon.getNgayLap().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
        String trangThai = hoaDon.getTrangThaiThanhToan();
        BigDecimal thucThu = "Đã hủy".equalsIgnoreCase(trangThai) ? BigDecimal.ZERO : hoaDon.getTongTien();
        String[] giaTri = {
                hoaDon.getMaHoaDon(),
                ngay,
                hoaDon.getTenKhachHang() != null ? hoaDon.getTenKhachHang() : "Khách vãng lai",
                hoaDon.getHinhThucThanhToan(),
                trangThai,
                DINH_DANG_TIEN.format(thucThu)
        };
        for (int i = 0; i < giaTri.length; i++) {
            Label label = new Label(giaTri[i] != null ? giaTri[i] : "");
            label.setPrefWidth(rong[i]);
            label.setStyle("-fx-font-size: 10px; -fx-padding: 4;");
            hang.add(label, i, 0);
        }
        return hang;
    }

    private void inBaoCao(VBox noiDungBaoCao) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(this)) {
            noiDungBaoCao.setMaxWidth(Region.USE_PREF_SIZE);
            if (job.printPage(noiDungBaoCao)) {
                job.endJob();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Đã gửi lệnh in báo cáo thành công!");
                alert.setHeaderText(null);
                alert.showAndWait();
                close();
            } else {
                job.cancelJob();
            }
        }
    }
}
