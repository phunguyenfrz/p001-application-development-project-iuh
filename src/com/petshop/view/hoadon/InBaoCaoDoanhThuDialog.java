package com.petshop.view.hoadon;

import com.petshop.model.HoaDon;
import com.petshop.util.SessionManager;
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
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.transform.Scale;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Hộp thoại hiển thị và in / xuất PDF báo cáo thống kê doanh thu toàn diện.
 * Kết hợp:
 * - Bố cục 4 bảng kê chi tiết xếp dọc (I. Hóa đơn, II. Sản phẩm bán ra, III. Dịch vụ chăm sóc, IV. Đổi trả & Tiền hoàn)
 * - Khối chỉ số KPI đồng bộ và chữ ký người lập / quản lý
 * - Thuật toán tự động co giãn tỷ lệ (Scale transform + Group) vừa khít khổ giấy A4 khi in hoặc xuất PDF.
 */
public class InBaoCaoDoanhThuDialog extends Stage {

    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 VNĐ");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DINH_DANG_FULL = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final double RONG_BAO_CAO = 880;

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

        Button btnIn = new Button("🖨 In / Xuất PDF");
        btnIn.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-cursor: hand; -fx-background-radius: 6;");
        btnIn.setOnAction(e -> inBaoCao());

        Button btnDong = new Button("Đóng");
        btnDong.setStyle("-fx-background-color: #f8fafc; -fx-text-fill: #334155; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-padding: 8 18; -fx-cursor: hand;");
        btnDong.setOnAction(e -> close());

        HBox thanhThaoTac = new HBox(12, btnIn, btnDong);
        thanhThaoTac.setAlignment(Pos.CENTER_RIGHT);
        thanhThaoTac.setPadding(new Insets(12, 24, 14, 24));
        thanhThaoTac.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-width: 1 0 0 0;");

        BorderPane goc = new BorderPane();
        goc.setCenter(cuon);
        goc.setBottom(thanhThaoTac);

        setScene(new Scene(goc, 960, 720));
        setMinWidth(800);
        setMinHeight(600);
    }

    private VBox taoNoiDungBaoCao() {
        VBox baoCao = new VBox(16);
        baoCao.setPadding(new Insets(28, 36, 36, 36));
        baoCao.setPrefWidth(RONG_BAO_CAO);
        baoCao.setMaxWidth(RONG_BAO_CAO);
        baoCao.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-width: 1;");
        baoCao.setAlignment(Pos.TOP_CENTER);

        // 1. Header thương hiệu & tiêu đề
        VBox pnlHeader = new VBox(4);
        pnlHeader.setAlignment(Pos.CENTER);

        Label lblThuongHieu = new Label("CỬA HÀNG THÚ CƯNG HAPPY TAILS");
        lblThuongHieu.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0369a1;");

        Label lblDiaChi = new Label("12 Nguyễn Văn Bảo, Phường 4, Gò Vấp, TP.HCM  |  Hotline: 0909.000.000");
        lblDiaChi.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

        Label lblTieuDe = new Label("BÁO CÁO THỐNG KÊ DOANH THU & HOẠT ĐỘNG");
        lblTieuDe.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label lblThoiGian = new Label("Thời gian báo cáo: " + khoangThoiGian + "  |  Trạng thái: "
                + (trangThaiLoc == null || trangThaiLoc.contains("Tất cả") ? "Tất cả trạng thái" : trangThaiLoc));
        lblThoiGian.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");

        String nguoiLap = "Quản trị hệ thống";
        if (SessionManager.getTaiKhoanHienTai() != null) {
            nguoiLap = SessionManager.getTaiKhoanHienTai().getTenDangNhap() + " (" + SessionManager.getTaiKhoanHienTai().getPhanQuyen() + ")";
        }
        Label lblNgayXuat = new Label("Ngày xuất: " + LocalDateTime.now().format(DINH_DANG_FULL) + "  |  Người lập: " + nguoiLap);
        lblNgayXuat.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

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
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setAlignment(Pos.CENTER);

        themOChiSo(grid, 0, 0, "DOANH THU THỰC THU", tongDoanhThu, "#0f172a", true);
        themOChiSo(grid, 1, 0, "DOANH THU SẢN PHẨM", doanhThuSanPham, "#0f172a", false);
        themOChiSo(grid, 2, 0, "DOANH THU DỊCH VỤ", doanhThuDichVu, "#0f172a", false);
        themOChiSo(grid, 3, 0, "TỔNG GIẢM GIÁ", tongGiamGia, "#475569", false);

        themOChiSo(grid, 0, 1, "TIỀN HOÀN ĐỔI TRẢ", tongHoanDoiTra, "#b91c1c", false);
        themOChiSo(grid, 1, 1, "TIỀN KHÁCH BÙ ĐỔI TRẢ", tongBuDoiTra, "#0f172a", false);
        themOChiSo(grid, 2, 1, "ĐƠN HOÀN TẤT / HỦY", hoaDonHoanTat + " đơn / " + hoaDonHuy + " hủy", "#0f172a", false);
        themOChiSo(grid, 3, 1, "GIÁ TRỊ TB / ĐƠN (AOV)", giaTriTrungBinh, "#0369a1", false);

        return grid;
    }

    private void themOChiSo(GridPane grid, int col, int row, String tieuDe, String giaTri, String mau, boolean noiBat) {
        VBox the = new VBox(2);
        the.setPadding(new Insets(6, 10, 6, 10));
        the.setPrefWidth(200);
        the.setStyle(noiBat
                ? "-fx-background-color: #f8fafc; -fx-border-color: #0f172a; -fx-border-width: 1.5; -fx-border-radius: 4; -fx-background-radius: 4;"
                : "-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4;");

        Label lblTen = new Label(tieuDe);
        lblTen.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: #64748b;");

        Label lblGia = new Label(giaTri != null ? giaTri : "0 VNĐ");
        lblGia.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + mau + ";");

        the.getChildren().addAll(lblTen, lblGia);
        grid.add(the, col, row);
    }

    // =========================================================================
    // MỤC I. BẢNG HÓA ĐƠN BÁN HÀNG
    // =========================================================================
    private VBox taoKhoiBangHoaDon(List<HoaDon> dsHoaDon) {
        VBox box = new VBox(6);
        box.setAlignment(Pos.TOP_LEFT);

        Label lblTieuDe = new Label("I. DANH SÁCH HÓA ĐƠN BÁN HÀNG & DỊCH VỤ");
        lblTieuDe.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        box.getChildren().add(lblTieuDe);

        double[] rongs = {40, 130, 90, 80, 150, 110, 100, 110};
        String[] tieuDes = {"STT", "Mã hóa đơn", "Ngày lập", "Giờ lập", "Khách hàng", "Thanh toán", "Trạng thái", "Thực thu"};

        GridPane header = taoHangTieuDe(rongs, tieuDes);
        box.getChildren().add(header);

        BigDecimal tongTien = BigDecimal.ZERO;
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
                BigDecimal thucThu = "Đã hủy".equalsIgnoreCase(tt) ? BigDecimal.ZERO : (hd.getTongTien() != null ? hd.getTongTien() : BigDecimal.ZERO);
                tongTien = tongTien.add(thucThu);

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
        lblTongKet.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #0f172a; -fx-padding: 4 0 0 4;");
        box.getChildren().add(lblTongKet);

        return box;
    }

    // =========================================================================
    // MỤC II. BẢNG SẢN PHẨM BÁN RA
    // =========================================================================
    private VBox taoKhoiBangSanPham(List<Map<String, Object>> dsSanPham) {
        VBox box = new VBox(6);
        box.setAlignment(Pos.TOP_LEFT);

        Label lblTieuDe = new Label("II. THỐNG KÊ SẢN PHẨM BÁN RA");
        lblTieuDe.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        box.getChildren().add(lblTieuDe);

        double[] rongs = {40, 130, 280, 80, 110, 170};
        String[] tieuDes = {"STT", "Mã sản phẩm", "Tên sản phẩm", "ĐVT", "Số lượng bán", "Doanh thu sản phẩm"};

        GridPane header = taoHangTieuDe(rongs, tieuDes);
        box.getChildren().add(header);

        int tongSl = 0;
        BigDecimal tongTien = BigDecimal.ZERO;
        if (dsSanPham != null && !dsSanPham.isEmpty()) {
            int stt = 1;
            for (Map<String, Object> m : dsSanPham) {
                int sl = (int) m.getOrDefault("soLuong", 0);
                BigDecimal dt = (BigDecimal) m.getOrDefault("doanhThu", BigDecimal.ZERO);
                tongSl += sl;
                tongTien = tongTien.add(dt);

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

        Label lblTongKet = new Label(String.format("=> Tổng số mặt hàng: %d | Tổng số lượng bán: %d | Tổng doanh thu SP: %s",
                dsSanPham != null ? dsSanPham.size() : 0, tongSl, DINH_DANG_TIEN.format(tongTien)));
        lblTongKet.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #0f172a; -fx-padding: 4 0 0 4;");
        box.getChildren().add(lblTongKet);

        return box;
    }

    // =========================================================================
    // MỤC III. BẢNG DỊCH VỤ CHĂM SÓC THỰC HIỆN
    // =========================================================================
    private VBox taoKhoiBangDichVu(List<Map<String, Object>> dsDichVu) {
        VBox box = new VBox(6);
        box.setAlignment(Pos.TOP_LEFT);

        Label lblTieuDe = new Label("III. THỐNG KÊ DỊCH VỤ CHĂM SÓC THỰC HIỆN");
        lblTieuDe.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        box.getChildren().add(lblTieuDe);

        double[] rongs = {40, 130, 360, 110, 170};
        String[] tieuDes = {"STT", "Mã dịch vụ", "Tên dịch vụ chăm sóc", "Số lượt làm", "Doanh thu dịch vụ"};

        GridPane header = taoHangTieuDe(rongs, tieuDes);
        box.getChildren().add(header);

        int tongLuot = 0;
        BigDecimal tongTien = BigDecimal.ZERO;
        if (dsDichVu != null && !dsDichVu.isEmpty()) {
            int stt = 1;
            for (Map<String, Object> m : dsDichVu) {
                int sl = (int) m.getOrDefault("soLuong", 0);
                BigDecimal dt = (BigDecimal) m.getOrDefault("doanhThu", BigDecimal.ZERO);
                tongLuot += sl;
                tongTien = tongTien.add(dt);

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
        lblTongKet.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #0f172a; -fx-padding: 4 0 0 4;");
        box.getChildren().add(lblTongKet);

        return box;
    }

    // =========================================================================
    // MỤC IV. BẢNG MẶT HÀNG ĐỔI TRẢ & TIỀN HOÀN
    // =========================================================================
    private VBox taoKhoiBangDoiTra(List<BaoCaoDoanhThuHoaDonView.DongHangDoiTraBaoCao> dsDoiTra) {
        VBox box = new VBox(6);
        box.setAlignment(Pos.TOP_LEFT);

        Label lblTieuDe = new Label("IV. THỐNG KÊ ĐỔI TRẢ SẢN PHẨM & TIỀN HOÀN LẠI");
        lblTieuDe.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        box.getChildren().add(lblTieuDe);

        double[] rongs = {40, 110, 110, 180, 45, 95, 110, 120};
        String[] tieuDes = {"STT", "Mã phiếu", "Mã HĐ gốc", "Sản phẩm đổi/trả", "SL", "Lý do", "Hướng xử lý", "Tiền hoàn lại"};

        GridPane header = taoHangTieuDe(rongs, tieuDes);
        box.getChildren().add(header);

        BigDecimal tongHoan = BigDecimal.ZERO;
        if (dsDoiTra != null && !dsDoiTra.isEmpty()) {
            int stt = 1;
            for (BaoCaoDoanhThuHoaDonView.DongHangDoiTraBaoCao dt : dsDoiTra) {
                BigDecimal hoan = dt.tienHoan != null ? dt.tienHoan : BigDecimal.ZERO;
                tongHoan = tongHoan.add(hoan);

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
        lblTongKet.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #b91c1c; -fx-padding: 4 0 0 4;");
        box.getChildren().add(lblTongKet);

        return box;
    }

    // =========================================================================
    // KHỐI CHỮ KÝ XÁC NHẬN
    // =========================================================================
    private HBox taoKhoiChuKy() {
        HBox box = new HBox(40);
        box.setPadding(new Insets(20, 30, 20, 30));
        box.setAlignment(Pos.CENTER);

        VBox nguoiLap = new VBox(4);
        nguoiLap.setAlignment(Pos.CENTER);
        Label lblLap = new Label("NGƯỜI LẬP BÁO CÁO");
        lblLap.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #0f172a;");
        Label lblKy1 = new Label("(Ký, ghi rõ họ tên)");
        lblKy1.setStyle("-fx-font-size: 10px; -fx-font-style: italic; -fx-text-fill: #64748b;");
        Region spc1 = new Region();
        spc1.setMinHeight(50);
        Label lblTen1 = new Label("............................................");
        lblTen1.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        nguoiLap.getChildren().addAll(lblLap, lblKy1, spc1, lblTen1);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox quanLy = new VBox(4);
        quanLy.setAlignment(Pos.CENTER);
        Label lblQl = new Label("QUẢN LÝ CỬA HÀNG");
        lblQl.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #0f172a;");
        Label lblKy2 = new Label("(Ký, đóng dấu xác nhận)");
        lblKy2.setStyle("-fx-font-size: 10px; -fx-font-style: italic; -fx-text-fill: #64748b;");
        Region spc2 = new Region();
        spc2.setMinHeight(50);
        Label lblTen2 = new Label("............................................");
        lblTen2.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        quanLy.getChildren().addAll(lblQl, lblKy2, spc2, lblTen2);

        box.getChildren().addAll(nguoiLap, spacer, quanLy);
        return box;
    }

    // =========================================================================
    // TIỆN ÍCH TẠO BẢNG GRIDPANE
    // =========================================================================
    private GridPane taoHangTieuDe(double[] rongs, String[] tieuDes) {
        GridPane hang = new GridPane();
        hang.setHgap(4);
        hang.setStyle("-fx-background-color: #f1f5f9; -fx-border-color: #cbd5e1; -fx-border-width: 1;");
        hang.setPadding(new Insets(4, 6, 4, 6));

        for (int i = 0; i < tieuDes.length; i++) {
            Label lbl = new Label(tieuDes[i]);
            lbl.setPrefWidth(rongs[i]);
            lbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
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
        hang.setHgap(4);
        hang.setStyle(chan
                ? "-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;"
                : "-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");
        hang.setPadding(new Insets(4, 6, 4, 6));

        for (int i = 0; i < giaTri.length; i++) {
            Label lbl = new Label(giaTri[i] != null ? giaTri[i] : "");
            lbl.setPrefWidth(rongs[i]);
            lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #334155;");
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
        box.setPadding(new Insets(8));
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");
        Label lbl = new Label(thongBao);
        lbl.setStyle("-fx-font-size: 10px; -fx-font-style: italic; -fx-text-fill: #94a3b8;");
        box.getChildren().add(lbl);
        return box;
    }

    /**
     * Thực hiện in hoặc xuất sang file PDF qua PrinterJob.
     * Sử dụng bản in độc lập trong Group kết hợp Scale transform để co giãn vừa khít 100% khổ giấy A4.
     */
    private void inBaoCao() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(this)) {
            PageLayout pageLayout = job.getJobSettings().getPageLayout();
            double printableWidth = pageLayout.getPrintableWidth();

            VBox banIn = taoNoiDungBaoCao();
            banIn.setStyle("-fx-background-color: #ffffff; -fx-border-width: 0;");
            banIn.setEffect(null);

            Group printRoot = new Group(banIn);

            Scale scaleTransform = null;
            if (printableWidth < RONG_BAO_CAO) {
                double scale = printableWidth / RONG_BAO_CAO;
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
