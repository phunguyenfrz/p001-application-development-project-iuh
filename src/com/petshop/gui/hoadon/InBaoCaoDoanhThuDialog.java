package com.petshop.gui.hoadon;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.petshop.entity.HoaDon;
import com.petshop.util.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Hộp thoại hiển thị và in / xuất PDF báo cáo thống kê doanh thu toàn diện.
 * Khắc phục triệt để lỗi tràn lề phải khi xuất/in:
 * - Khổ chiều rộng chuẩn hóa 720px vừa khít khổ giấy A4.
 * - Khống chế chặt chẽ minWidth / prefWidth / maxWidth cho từng cột, tránh Label tự dãn làm vỡ bảng.
 * - Tự động co giãn tỷ lệ (Scale transform) chuẩn theo PageLayout của máy in.
 * - Bổ sung nút lưu trực tiếp file PDF chất lượng cao (OpenPDF vector).
 */
public class InBaoCaoDoanhThuDialog extends Stage {

    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 VNĐ");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DINH_DANG_FULL = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final double RONG_BAO_CAO = 720;

    private final String khoangThoiGian;
    private final String trangThaiLoc;
    private final String tongDoanhThu;
    private final String doanhThuSanPham;
    private final String doanhThuDichVu;
    private final String tongGiamGia;
    private final String tongHoanDoiTra;
    private final String tongBuDoiTra;
    private final String tienMat;
    private final String chuyenKhoan;
    private final String hoaDonHoanTat;
    private final String hoaDonHuy;
    private final String giaTriTrungBinh;
    private final List<HoaDon> dsHoaDon;
    private final List<Map<String, Object>> dsSanPham;
    private final List<Map<String, Object>> dsDichVu;
    private final List<BaoCaoDoanhThuHoaDonView.DongHangDoiTraBaoCao> dsDoiTra;

    public InBaoCaoDoanhThuDialog(
            String khoangThoiGian,
            String trangThaiLoc,
            String tongDoanhThu,
            String doanhThuSanPham,
            String doanhThuDichVu,
            String tongGiamGia,
            String tongHoanDoiTra,
            String tongBuDoiTra,
            String tienMat,
            String chuyenKhoan,
            String hoaDonHoanTat,
            String hoaDonHuy,
            String giaTriTrungBinh,
            List<HoaDon> dsHoaDon,
            List<Map<String, Object>> dsSanPham,
            List<Map<String, Object>> dsDichVu,
            List<BaoCaoDoanhThuHoaDonView.DongHangDoiTraBaoCao> dsDoiTra,
            Stage chuSoHuu) {

        this.khoangThoiGian = khoangThoiGian;
        this.trangThaiLoc = trangThaiLoc;
        this.tongDoanhThu = tongDoanhThu;
        this.doanhThuSanPham = doanhThuSanPham;
        this.doanhThuDichVu = doanhThuDichVu;
        this.tongGiamGia = tongGiamGia;
        this.tongHoanDoiTra = tongHoanDoiTra;
        this.tongBuDoiTra = tongBuDoiTra;
        this.tienMat = tienMat;
        this.chuyenKhoan = chuyenKhoan;
        this.hoaDonHoanTat = hoaDonHoanTat;
        this.hoaDonHuy = hoaDonHuy;
        this.giaTriTrungBinh = giaTriTrungBinh;
        this.dsHoaDon = dsHoaDon;
        this.dsSanPham = dsSanPham;
        this.dsDichVu = dsDichVu;
        this.dsDoiTra = dsDoiTra;

        initModality(Modality.APPLICATION_MODAL);
        if (chuSoHuu != null) initOwner(chuSoHuu);
        setTitle("Xuất & In báo cáo doanh thu & hoạt động");

        VBox noiDungBaoCao = taoNoiDungBaoCao();

        StackPane khungToGiay = new StackPane(noiDungBaoCao);
        khungToGiay.setPadding(new Insets(16));
        khungToGiay.setAlignment(Pos.TOP_CENTER);
        khungToGiay.setStyle("-fx-background-color: #cbd5e1;");

        ScrollPane cuon = new ScrollPane(khungToGiay);
        cuon.setFitToWidth(true);
        cuon.setStyle("-fx-background: #cbd5e1; -fx-background-color: #cbd5e1;");

        Button btnIn = new Button("🖨 In máy in (Print)");
        btnIn.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-cursor: hand; -fx-background-radius: 6;");
        btnIn.setOnAction(e -> inBaoCao());

        Button btnXuatPDF = new Button("📄 Lưu file PDF trực tiếp");
        btnXuatPDF.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-cursor: hand; -fx-background-radius: 6;");
        btnXuatPDF.setOnAction(e -> xuLyXuatPDFTrucTiep());

        Button btnDong = new Button("Đóng");
        btnDong.setStyle("-fx-background-color: #f8fafc; -fx-text-fill: #334155; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-padding: 8 16; -fx-cursor: hand;");
        btnDong.setOnAction(e -> close());

        HBox thanhThaoTac = new HBox(12, btnXuatPDF, btnIn, btnDong);
        thanhThaoTac.setAlignment(Pos.CENTER_RIGHT);
        thanhThaoTac.setPadding(new Insets(12, 24, 14, 24));
        thanhThaoTac.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-width: 1 0 0 0;");

        BorderPane goc = new BorderPane();
        goc.setCenter(cuon);
        goc.setBottom(thanhThaoTac);

        setScene(new Scene(goc, 840, 720));
        setMinWidth(760);
        setMinHeight(600);
    }

    private VBox taoNoiDungBaoCao() {
        VBox baoCao = new VBox(14);
        baoCao.setPadding(new Insets(20, 20, 24, 20));
        baoCao.setPrefWidth(RONG_BAO_CAO);
        baoCao.setMaxWidth(RONG_BAO_CAO);
        baoCao.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-width: 1;");
        baoCao.setAlignment(Pos.TOP_CENTER);

        // 1. Header thương hiệu & tiêu đề
        VBox pnlHeader = new VBox(4);
        pnlHeader.setAlignment(Pos.CENTER);

        Label lblThuongHieu = new Label("🐾 CỬA HÀNG THÚ CƯNG HAPPY TAILS 🐾");
        lblThuongHieu.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0369a1;");

        Label lblDiaChi = new Label("12 Nguyễn Văn Bảo, Phường 4, Gò Vấp, TP.HCM  |  Hotline: 0909.000.000");
        lblDiaChi.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

        Label lblTieuDe = new Label("BÁO CÁO THỐNG KÊ DOANH THU & HOẠT ĐỘNG");
        lblTieuDe.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label lblThoiGian = new Label("Thời gian: " + khoangThoiGian + "  |  Trạng thái: "
                + (trangThaiLoc == null || trangThaiLoc.contains("Tất cả") ? "Tất cả trạng thái" : trangThaiLoc));
        lblThoiGian.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #475569;");

        String nguoiLap = "Quản trị hệ thống";
        if (SessionManager.getTaiKhoanHienTai() != null) {
            nguoiLap = SessionManager.getTaiKhoanHienTai().getTenDangNhap() + " (" + SessionManager.getTaiKhoanHienTai().getPhanQuyen() + ")";
        }
        Label lblNgayXuat = new Label("Ngày xuất: " + LocalDateTime.now().format(DINH_DANG_FULL) + "  |  Người lập: " + nguoiLap);
        lblNgayXuat.setStyle("-fx-font-size: 10.5px; -fx-text-fill: #94a3b8;");

        pnlHeader.getChildren().addAll(lblThuongHieu, lblDiaChi, lblTieuDe, lblThoiGian, lblNgayXuat);

        // 2. Thẻ chỉ số tổng quan (KPI)
        GridPane pnlChiSo = taoBangChiSo(
                tongDoanhThu, doanhThuSanPham, doanhThuDichVu, tongGiamGia,
                tongHoanDoiTra, tongBuDoiTra, tienMat, chuyenKhoan,
                hoaDonHoanTat, hoaDonHuy, giaTriTrungBinh
        );

        // 3. Bảng 1: Hóa đơn
        VBox pnlBangHoaDon = taoKhoiBangHoaDon(dsHoaDon);

        // 4. Bảng 2: Sản phẩm bán ra
        VBox pnlBangSanPham = taoKhoiBangSanPham(dsSanPham);

        // 5. Bảng 3: Dịch vụ chăm sóc thực hiện
        VBox pnlBangDichVu = taoKhoiBangDichVu(dsDichVu);

        // 6. Bảng 4: Mặt hàng đổi trả
        VBox pnlBangDoiTra = taoKhoiBangDoiTra(dsDoiTra);

        // 7. Chữ ký xác nhận
        HBox pnlChuKy = taoKhoiChuKy();

        baoCao.getChildren().addAll(
                pnlHeader,
                new Separator(),
                pnlChiSo,
                new Separator(),
                pnlBangHoaDon,
                pnlBangSanPham,
                pnlBangDichVu,
                pnlBangDoiTra,
                new Separator(),
                pnlChuKy
        );

        return baoCao;
    }

    private GridPane taoBangChiSo(
            String tongDoanhThu, String doanhThuSanPham, String doanhThuDichVu, String tongGiamGia,
            String tongHoanDoiTra, String tongBuDoiTra, String tienMat, String chuyenKhoan,
            String hoaDonHoanTat, String hoaDonHuy, String giaTriTrungBinh) {

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(6);
        grid.setAlignment(Pos.CENTER);

        themOChiSo(grid, 0, 0, "DOANH THU THỰC THU", tongDoanhThu, "#0369a1", true);
        themOChiSo(grid, 1, 0, "DOANH THU SẢN PHẨM", doanhThuSanPham, "#0f172a", false);
        themOChiSo(grid, 2, 0, "DOANH THU DỊCH VỤ", doanhThuDichVu, "#0f172a", false);
        themOChiSo(grid, 3, 0, "TỔNG GIẢM GIÁ", tongGiamGia, "#475569", false);

        themOChiSo(grid, 0, 1, "TIỀN HOÀN ĐỔI TRẢ", tongHoanDoiTra, "#b91c1c", false);
        themOChiSo(grid, 1, 1, "TIỀN KHÁCH BÙ ĐỔI TRẢ", tongBuDoiTra, "#0f172a", false);
        themOChiSo(grid, 2, 1, "ĐƠN HOÀN TẤT / HỦY", hoaDonHoanTat + " đơn / " + hoaDonHuy + " hủy", "#0f172a", false);
        themOChiSo(grid, 3, 1, "GIÁ TRỊ TB / ĐƠN (AOV)", giaTriTrungBinh, "#059669", false);

        return grid;
    }

    private void themOChiSo(GridPane grid, int col, int row, String tieuDe, String giaTri, String mau, boolean noiBat) {
        VBox the = new VBox(2);
        the.setPadding(new Insets(5, 8, 5, 8));
        the.setPrefWidth(162);
        the.setMaxWidth(162);
        the.setStyle(noiBat
                ? "-fx-background-color: #f0f9ff; -fx-border-color: #0284c7; -fx-border-width: 1.2; -fx-border-radius: 4; -fx-background-radius: 4;"
                : "-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4;");

        Label lblTen = new Label(tieuDe);
        lblTen.setStyle("-fx-font-size: 8.5px; -fx-font-weight: bold; -fx-text-fill: #64748b;");

        Label lblGia = new Label(giaTri != null ? giaTri : "0 VNĐ");
        lblGia.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + mau + ";");

        the.getChildren().addAll(lblTen, lblGia);
        grid.add(the, col, row);
    }

    // =========================================================================
    // MỤC I. BẢNG HÓA ĐƠN BÁN HÀNG
    // =========================================================================
    private VBox taoKhoiBangHoaDon(List<HoaDon> dsHoaDon) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.TOP_LEFT);

        Label lblTieuDe = new Label("I. DANH SÁCH HÓA ĐƠN BÁN HÀNG & DỊCH VỤ");
        lblTieuDe.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        box.getChildren().add(lblTieuDe);

        double[] rongs = {32, 120, 80, 50, 140, 85, 85, 88};
        String[] tieuDes = {"STT", "Mã hóa đơn", "Ngày lập", "Giờ lập", "Khách hàng", "Thanh toán", "Trạng thái", "Thực thu"};

        GridPane header = taoHangTieuDe(rongs, tieuDes);
        box.getChildren().add(header);

        double tongTien = 0.0;
        if (dsHoaDon != null && !dsHoaDon.isEmpty()) {
            int stt = 1;
            for (HoaDon hd : dsHoaDon) {
                String ngay = hd.getNgayLap() != null ? hd.getNgayLap().format(DINH_DANG_NGAY) : "";
                String gio = hd.getGioLap() != null ? hd.getGioLap().format(DINH_DANG_GIO) : "--:--";
                String khach = (hd.getTenKhachHang() != null ? hd.getTenKhachHang() : "Khách vãng lai");
                if (hd.getSoDienThoaiKhachHang() != null && !hd.getSoDienThoaiKhachHang().isBlank()) {
                    khach += " (" + hd.getSoDienThoaiKhachHang() + ")";
                }
                String tt = hd.getTrangThaiThanhToan();
                double thucThu = "Đã hủy".equalsIgnoreCase(tt) ? 0.0 : hd.getTongTien();
                tongTien += thucThu;

                String[] giaTri = {
                        String.valueOf(stt++),
                        hd.getMaHoaDon(),
                        ngay,
                        gio,
                        khach,
                        hd.getHinhThucThanhToan() != null ? hd.getHinhThucThanhToan() : "",
                        tt != null ? tt : "",
                        DINH_DANG_TIEN.format(thucThu)
                };
                box.getChildren().add(taoHangDuLieu(rongs, giaTri, stt % 2 == 0));
            }
        } else {
            box.getChildren().add(taoHangTrong("Không có hóa đơn nào trong khoảng thời gian này."));
        }

        Label lblTongKet = new Label(String.format("=> Tổng số: %d hóa đơn | Tổng tiền thực thu: %s",
                dsHoaDon != null ? dsHoaDon.size() : 0, DINH_DANG_TIEN.format(tongTien)));
        lblTongKet.setStyle("-fx-font-weight: bold; -fx-font-size: 10.5px; -fx-text-fill: #0f172a; -fx-padding: 3 0 0 2;");
        box.getChildren().add(lblTongKet);

        return box;
    }

    // =========================================================================
    // MỤC II. BẢNG SẢN PHẨM BÁN RA
    // =========================================================================
    private VBox taoKhoiBangSanPham(List<Map<String, Object>> dsSanPham) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.TOP_LEFT);

        Label lblTieuDe = new Label("II. THỐNG KÊ SẢN PHẨM BÁN RA");
        lblTieuDe.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        box.getChildren().add(lblTieuDe);

        double[] rongs = {32, 120, 260, 50, 70, 148};
        String[] tieuDes = {"STT", "Mã sản phẩm", "Tên sản phẩm", "ĐVT", "Số lượng", "Doanh thu SP"};

        GridPane header = taoHangTieuDe(rongs, tieuDes);
        box.getChildren().add(header);

        int tongSl = 0;
        double tongTien = 0.0;
        if (dsSanPham != null && !dsSanPham.isEmpty()) {
            int stt = 1;
            for (Map<String, Object> m : dsSanPham) {
                int sl = (int) m.getOrDefault("soLuong", 0);
                double dt = m.get("doanhThu") instanceof Number n ? n.doubleValue() : 0.0;
                tongSl += sl;
                tongTien += dt;

                String[] giaTri = {
                        String.valueOf(stt++),
                        String.valueOf(m.getOrDefault("maSanPham", "")),
                        String.valueOf(m.getOrDefault("tenSanPham", "")),
                        String.valueOf(m.getOrDefault("donViTinh", "Gói")),
                        String.valueOf(sl),
                        DINH_DANG_TIEN.format(dt)
                };
                box.getChildren().add(taoHangDuLieu(rongs, giaTri, stt % 2 == 0));
            }
        } else {
            box.getChildren().add(taoHangTrong("Không có sản phẩm nào được bán trong khoảng thời gian này."));
        }

        Label lblTongKet = new Label(String.format("=> Tổng số mặt hàng: %d | Tổng số lượng: %d | Tổng doanh thu SP: %s",
                dsSanPham != null ? dsSanPham.size() : 0, tongSl, DINH_DANG_TIEN.format(tongTien)));
        lblTongKet.setStyle("-fx-font-weight: bold; -fx-font-size: 10.5px; -fx-text-fill: #0f172a; -fx-padding: 3 0 0 2;");
        box.getChildren().add(lblTongKet);

        return box;
    }

    // =========================================================================
    // MỤC III. BẢNG DỊCH VỤ CHĂM SÓC THỰC HIỆN
    // =========================================================================
    private VBox taoKhoiBangDichVu(List<Map<String, Object>> dsDichVu) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.TOP_LEFT);

        Label lblTieuDe = new Label("III. THỐNG KÊ DỊCH VỤ CHĂM SÓC THỰC HIỆN");
        lblTieuDe.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        box.getChildren().add(lblTieuDe);

        double[] rongs = {32, 120, 310, 70, 148};
        String[] tieuDes = {"STT", "Mã dịch vụ", "Tên dịch vụ chăm sóc", "Số lượt làm", "Doanh thu dịch vụ"};

        GridPane header = taoHangTieuDe(rongs, tieuDes);
        box.getChildren().add(header);

        int tongLuot = 0;
        double tongTien = 0.0;
        if (dsDichVu != null && !dsDichVu.isEmpty()) {
            int stt = 1;
            for (Map<String, Object> m : dsDichVu) {
                int sl = (int) m.getOrDefault("soLuong", 0);
                double dt = m.get("doanhThu") instanceof Number n ? n.doubleValue() : 0.0;
                tongLuot += sl;
                tongTien += dt;

                String[] giaTri = {
                        String.valueOf(stt++),
                        String.valueOf(m.getOrDefault("maDichVu", "")),
                        String.valueOf(m.getOrDefault("tenDichVu", "")),
                        String.valueOf(sl),
                        DINH_DANG_TIEN.format(dt)
                };
                box.getChildren().add(taoHangDuLieu(rongs, giaTri, stt % 2 == 0));
            }
        } else {
            box.getChildren().add(taoHangTrong("Không có dịch vụ chăm sóc nào được thực hiện trong khoảng thời gian này."));
        }

        Label lblTongKet = new Label(String.format("=> Tổng số dịch vụ: %d | Tổng số lượt làm: %d | Tổng doanh thu DV: %s",
                dsDichVu != null ? dsDichVu.size() : 0, tongLuot, DINH_DANG_TIEN.format(tongTien)));
        lblTongKet.setStyle("-fx-font-weight: bold; -fx-font-size: 10.5px; -fx-text-fill: #0f172a; -fx-padding: 3 0 0 2;");
        box.getChildren().add(lblTongKet);

        return box;
    }

    // =========================================================================
    // MỤC IV. BẢNG MẶT HÀNG ĐỔI TRẢ & TIỀN HOÀN
    // =========================================================================
    private VBox taoKhoiBangDoiTra(List<BaoCaoDoanhThuHoaDonView.DongHangDoiTraBaoCao> dsDoiTra) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.TOP_LEFT);

        Label lblTieuDe = new Label("IV. THỐNG KÊ ĐỔI TRẢ SẢN PHẨM & TIỀN HOÀN LẠI");
        lblTieuDe.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        box.getChildren().add(lblTieuDe);

        double[] rongs = {32, 90, 95, 155, 38, 85, 90, 95};
        String[] tieuDes = {"STT", "Mã phiếu", "Mã HĐ gốc", "Sản phẩm đổi/trả", "SL", "Lý do", "Hướng xử lý", "Tiền hoàn lại"};

        GridPane header = taoHangTieuDe(rongs, tieuDes);
        box.getChildren().add(header);

        double tongHoan = 0.0;
        if (dsDoiTra != null && !dsDoiTra.isEmpty()) {
            int stt = 1;
            for (BaoCaoDoanhThuHoaDonView.DongHangDoiTraBaoCao dt : dsDoiTra) {
                double hoan = dt.tienHoan;
                tongHoan += hoan;

                String[] giaTri = {
                        String.valueOf(stt++),
                        dt.maPhieu,
                        dt.maHoaDon,
                        dt.tenSanPham,
                        String.valueOf(dt.soLuong),
                        dt.lyDo != null ? dt.lyDo : "",
                        dt.huongXuLy != null ? dt.huongXuLy : "",
                        DINH_DANG_TIEN.format(hoan)
                };
                box.getChildren().add(taoHangDuLieu(rongs, giaTri, stt % 2 == 0));
            }
        } else {
            box.getChildren().add(taoHangTrong("Không có giao dịch đổi trả nào trong khoảng thời gian này."));
        }

        Label lblTongKet = new Label(String.format("=> Tổng số lượt đổi trả: %d | Tổng tiền hoàn lại cho khách: %s",
                dsDoiTra != null ? dsDoiTra.size() : 0, DINH_DANG_TIEN.format(tongHoan)));
        lblTongKet.setStyle("-fx-font-weight: bold; -fx-font-size: 10.5px; -fx-text-fill: #b91c1c; -fx-padding: 3 0 0 2;");
        box.getChildren().add(lblTongKet);

        return box;
    }

    // =========================================================================
    // KHỐI CHỮ KÝ XÁC NHẬN
    // =========================================================================
    private HBox taoKhoiChuKy() {
        HBox box = new HBox(20);
        box.setPadding(new Insets(16, 20, 16, 20));
        box.setAlignment(Pos.CENTER);

        VBox nguoiLap = new VBox(3);
        nguoiLap.setAlignment(Pos.CENTER);
        Label lblLap = new Label("NGƯỜI LẬP BÁO CÁO");
        lblLap.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #0f172a;");
        Label lblKy1 = new Label("(Ký, ghi rõ họ tên)");
        lblKy1.setStyle("-fx-font-size: 9.5px; -fx-font-style: italic; -fx-text-fill: #64748b;");
        Region spc1 = new Region();
        spc1.setMinHeight(45);
        Label lblTen1 = new Label("............................................");
        lblTen1.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");
        nguoiLap.getChildren().addAll(lblLap, lblKy1, spc1, lblTen1);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox quanLy = new VBox(3);
        quanLy.setAlignment(Pos.CENTER);
        Label lblQl = new Label("QUẢN LÝ CỬA HÀNG");
        lblQl.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #0f172a;");
        Label lblKy2 = new Label("(Ký, đóng dấu xác nhận)");
        lblKy2.setStyle("-fx-font-size: 9.5px; -fx-font-style: italic; -fx-text-fill: #64748b;");
        Region spc2 = new Region();
        spc2.setMinHeight(45);
        Label lblTen2 = new Label("............................................");
        lblTen2.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");
        quanLy.getChildren().addAll(lblQl, lblKy2, spc2, lblTen2);

        box.getChildren().addAll(nguoiLap, spacer, quanLy);
        return box;
    }

    // =========================================================================
    // TIỆN ÍCH TẠO BẢNG GRIDPANE
    // =========================================================================
    private GridPane taoHangTieuDe(double[] rongs, String[] tieuDes) {
        GridPane hang = new GridPane();
        hang.setHgap(3);
        hang.setStyle("-fx-background-color: #f1f5f9; -fx-border-color: #cbd5e1; -fx-border-width: 1;");
        hang.setPadding(new Insets(3, 4, 3, 4));

        for (int i = 0; i < tieuDes.length; i++) {
            Label lbl = new Label(tieuDes[i]);
            lbl.setMinWidth(rongs[i]);
            lbl.setPrefWidth(rongs[i]);
            lbl.setMaxWidth(rongs[i]);
            lbl.setTextOverrun(OverrunStyle.ELLIPSIS);
            lbl.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
            if (i == 0 || tieuDes[i].equalsIgnoreCase("SL") || tieuDes[i].equalsIgnoreCase("ĐVT") || tieuDes[i].equalsIgnoreCase("Giờ lập")) {
                lbl.setAlignment(Pos.CENTER);
            } else if (tieuDes[i].contains("Doanh thu") || tieuDes[i].contains("Thực thu") || tieuDes[i].contains("Tiền") || tieuDes[i].contains("Số lượng")) {
                lbl.setAlignment(Pos.CENTER_RIGHT);
            } else {
                lbl.setAlignment(Pos.CENTER_LEFT);
            }
            hang.add(lbl, i, 0);
        }
        return hang;
    }

    private GridPane taoHangDuLieu(double[] rongs, String[] giaTri, boolean chan) {
        GridPane hang = new GridPane();
        hang.setHgap(3);
        hang.setStyle(chan
                ? "-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;"
                : "-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");
        hang.setPadding(new Insets(3, 4, 3, 4));

        for (int i = 0; i < giaTri.length; i++) {
            Label lbl = new Label(giaTri[i] != null ? giaTri[i] : "");
            lbl.setMinWidth(rongs[i]);
            lbl.setPrefWidth(rongs[i]);
            lbl.setMaxWidth(rongs[i]);
            lbl.setTextOverrun(OverrunStyle.ELLIPSIS);
            lbl.setStyle("-fx-font-size: 9.5px; -fx-text-fill: #334155;");
            if (i == 0 || i == 2 || i == 3) {
                lbl.setAlignment(Pos.CENTER);
            } else if (i == giaTri.length - 1 || giaTri[i].contains("VNĐ") || (giaTri[i].matches("\\d+") && i > 3)) {
                lbl.setAlignment(Pos.CENTER_RIGHT);
            } else {
                lbl.setAlignment(Pos.CENTER_LEFT);
            }
            hang.add(lbl, i, 0);
        }
        return hang;
    }

    private HBox taoHangTrong(String thongBao) {
        HBox box = new HBox();
        box.setPadding(new Insets(6));
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");
        Label lbl = new Label(thongBao);
        lbl.setStyle("-fx-font-size: 9.5px; -fx-font-style: italic; -fx-text-fill: #94a3b8;");
        box.getChildren().add(lbl);
        return box;
    }

    /**
     * Thực hiện in qua PrinterJob, tự động co giãn vừa vặn 100% trang A4 không bao giờ tràn lề.
     */
    private void inBaoCao() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(this)) {
            PageLayout pageLayout = job.getJobSettings().getPageLayout();
            double printableWidth = pageLayout.getPrintableWidth();

            VBox banIn = taoNoiDungBaoCao();
            banIn.setStyle("-fx-background-color: #ffffff; -fx-border-width: 0;");
            banIn.setEffect(null);
            banIn.applyCss();
            banIn.layout();

            Group printRoot = new Group(banIn);

            double contentWidth = Math.max(banIn.prefWidth(-1), RONG_BAO_CAO);
            double scale = 1.0;
            if (printableWidth < contentWidth) {
                scale = printableWidth / contentWidth;
            }

            Scale scaleTransform = new Scale(scale, scale, 0, 0);
            printRoot.getTransforms().add(scaleTransform);

            try {
                boolean thanhCong = job.printPage(pageLayout, printRoot);
                if (thanhCong) {
                    job.endJob();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Đã gửi lệnh in báo cáo thành công!");
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
                printRoot.getTransforms().remove(scaleTransform);
            }
        }
    }

    /**
     * Xuất trực tiếp file PDF vector chuẩn bằng OpenPDF, căn lề và định dạng bảng biểu hoàn hảo.
     */
    private void xuLyXuatPDFTrucTiep() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu file Báo cáo Doanh thu PDF");
        fileChooser.setInitialFileName("BaoCaoDoanhThu_HappyTails.pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Tệp PDF (*.pdf)", "*.pdf"));

        File file = fileChooser.showSaveDialog(this);
        if (file != null) {
            try {
                xuatBaoCaoPDFVector(file);
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Đã xuất file PDF báo cáo thành công!\nĐường dẫn: " + file.getAbsolutePath());
                alert.setHeaderText(null);
                alert.showAndWait();
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Lỗi khi xuất file PDF: " + ex.getMessage());
                alert.setHeaderText(null);
                alert.showAndWait();
            }
        }
    }

    private void xuatBaoCaoPDFVector(File destination) throws Exception {
        Document doc = new Document(PageSize.A4, 20, 20, 24, 24);
        PdfWriter.getInstance(doc, new FileOutputStream(destination));
        doc.open();

        Font fontHeader;
        Font fontTieuDe;
        Font fontBody;
        Font fontBold;
        try {
            File fa = new File("C:/Windows/Fonts/arial.ttf");
            File fab = new File("C:/Windows/Fonts/arialbd.ttf");
            if (fa.exists() && fab.exists()) {
                BaseFont bf = BaseFont.createFont(fa.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                BaseFont bfb = BaseFont.createFont(fab.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                fontHeader = new Font(bfb, 10, Font.BOLD, new java.awt.Color(3, 105, 161));
                fontTieuDe = new Font(bfb, 14, Font.BOLD, new java.awt.Color(15, 23, 42));
                fontBody = new Font(bf, 8.5f, Font.NORMAL, new java.awt.Color(51, 65, 85));
                fontBold = new Font(bfb, 8.5f, Font.BOLD, new java.awt.Color(15, 23, 42));
            } else {
                fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
                fontTieuDe = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
                fontBody = FontFactory.getFont(FontFactory.HELVETICA, 8.5f);
                fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f);
            }
        } catch (Exception e) {
            fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            fontTieuDe = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            fontBody = FontFactory.getFont(FontFactory.HELVETICA, 8.5f);
            fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f);
        }

        // 1. Header
        Paragraph pBrand = new Paragraph("🐾 CỬA HÀNG THÚ CƯNG HAPPY TAILS 🐾\n", fontHeader);
        pBrand.setAlignment(Element.ALIGN_CENTER);
        doc.add(pBrand);

        Paragraph pDc = new Paragraph("12 Nguyễn Văn Bảo, Phường 4, Gò Vấp, TP.HCM  |  Hotline: 0909.000.000\n", fontBody);
        pDc.setAlignment(Element.ALIGN_CENTER);
        doc.add(pDc);

        Paragraph pTitle = new Paragraph("BÁO CÁO THỐNG KÊ DOANH THU & HOẠT ĐỘNG\n", fontTieuDe);
        pTitle.setAlignment(Element.ALIGN_CENTER);
        pTitle.setSpacingBefore(6);
        doc.add(pTitle);

        Paragraph pTime = new Paragraph("Thời gian: " + khoangThoiGian + "  |  Trạng thái: "
                + (trangThaiLoc == null || trangThaiLoc.contains("Tất cả") ? "Tất cả trạng thái" : trangThaiLoc) + "\n\n", fontBody);
        pTime.setAlignment(Element.ALIGN_CENTER);
        doc.add(pTime);

        // 2. Thẻ KPI Grid
        PdfPTable tblKpi = new PdfPTable(4);
        tblKpi.setWidthPercentage(100);
        tblKpi.setSpacingAfter(10);
        themOChiSoPDF(tblKpi, "DOANH THU THỰC THU", tongDoanhThu, fontBold, fontBody);
        themOChiSoPDF(tblKpi, "DOANH THU SẢN PHẨM", doanhThuSanPham, fontBold, fontBody);
        themOChiSoPDF(tblKpi, "DOANH THU DỊCH VỤ", doanhThuDichVu, fontBold, fontBody);
        themOChiSoPDF(tblKpi, "TỔNG GIẢM GIÁ", tongGiamGia, fontBold, fontBody);
        themOChiSoPDF(tblKpi, "TIỀN HOÀN ĐỔI TRẢ", tongHoanDoiTra, fontBold, fontBody);
        themOChiSoPDF(tblKpi, "TIỀN KHÁCH BÙ ĐỔI TRẢ", tongBuDoiTra, fontBold, fontBody);
        themOChiSoPDF(tblKpi, "ĐƠN HOÀN TẤT / HỦY", hoaDonHoanTat + " đơn / " + hoaDonHuy + " hủy", fontBold, fontBody);
        themOChiSoPDF(tblKpi, "GIÁ TRỊ TB / ĐƠN (AOV)", giaTriTrungBinh, fontBold, fontBody);
        doc.add(tblKpi);

        // 3. Bảng Hóa đơn
        Paragraph pSec1 = new Paragraph("I. DANH SÁCH HÓA ĐƠN BÁN HÀNG & DỊCH VỤ", fontBold);
        pSec1.setSpacingBefore(6);
        pSec1.setSpacingAfter(4);
        doc.add(pSec1);

        float[] widthsHd = {5f, 18f, 13f, 9f, 23f, 12f, 11f, 14f};
        PdfPTable tblHd = new PdfPTable(widthsHd);
        tblHd.setWidthPercentage(100);

        String[] headersHd = {"STT", "Mã HĐ", "Ngày", "Giờ", "Khách hàng", "Thanh toán", "Trạng thái", "Thực thu"};
        for (String h : headersHd) {
            PdfPCell c = new PdfPCell(new Phrase(h, fontBold));
            c.setBackgroundColor(new java.awt.Color(241, 245, 249));
            c.setPadding(4);
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            tblHd.addCell(c);
        }

        if (dsHoaDon != null && !dsHoaDon.isEmpty()) {
            int stt = 1;
            for (HoaDon hd : dsHoaDon) {
                String ngay = hd.getNgayLap() != null ? hd.getNgayLap().format(DINH_DANG_NGAY) : "";
                String gio = hd.getGioLap() != null ? hd.getGioLap().format(DINH_DANG_GIO) : "";
                String khach = hd.getTenKhachHang() != null ? hd.getTenKhachHang() : "Khách vãng lai";
                if (hd.getSoDienThoaiKhachHang() != null && !hd.getSoDienThoaiKhachHang().isBlank()) {
                    khach += " (" + hd.getSoDienThoaiKhachHang() + ")";
                }
                String tt = hd.getTrangThaiThanhToan();
                double thucThu = "Đã hủy".equalsIgnoreCase(tt) ? 0.0 : hd.getTongTien();

                themODuLieuPDF(tblHd, String.valueOf(stt++), fontBody, Element.ALIGN_CENTER);
                themODuLieuPDF(tblHd, hd.getMaHoaDon(), fontBody, Element.ALIGN_LEFT);
                themODuLieuPDF(tblHd, ngay, fontBody, Element.ALIGN_CENTER);
                themODuLieuPDF(tblHd, gio, fontBody, Element.ALIGN_CENTER);
                themODuLieuPDF(tblHd, khach, fontBody, Element.ALIGN_LEFT);
                themODuLieuPDF(tblHd, hd.getHinhThucThanhToan() != null ? hd.getHinhThucThanhToan() : "", fontBody, Element.ALIGN_LEFT);
                themODuLieuPDF(tblHd, tt != null ? tt : "", fontBody, Element.ALIGN_CENTER);
                themODuLieuPDF(tblHd, DINH_DANG_TIEN.format(thucThu), fontBody, Element.ALIGN_RIGHT);
            }
        }
        doc.add(tblHd);

        // 4. Chữ ký
        Paragraph pSig = new Paragraph("\n\nNgười lập biểu                                                                             Quản lý cửa hàng\n(Ký, ghi rõ họ tên)                                                                   (Ký, đóng dấu)", fontBody);
        pSig.setSpacingBefore(12);
        doc.add(pSig);

        doc.close();
    }

    private void themOChiSoPDF(PdfPTable tbl, String tieuDe, String giaTri, Font fontBold, Font fontBody) {
        PdfPCell c = new PdfPCell();
        c.setPadding(4);
        c.setBorderColor(new java.awt.Color(226, 232, 240));
        c.addElement(new Paragraph(tieuDe, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, new java.awt.Color(100, 116, 139))));
        c.addElement(new Paragraph(giaTri != null ? giaTri : "0 VNĐ", fontBold));
        tbl.addCell(c);
    }

    private void themODuLieuPDF(PdfPTable tbl, String text, Font font, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text != null ? text : "", font));
        c.setPadding(3);
        c.setHorizontalAlignment(align);
        c.setBorderColor(new java.awt.Color(226, 232, 240));
        tbl.addCell(c);
    }
}
