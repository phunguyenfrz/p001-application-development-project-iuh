package com.petshop.view.donnhap;

import com.petshop.controller.DonNhapHangController;
import com.petshop.model.ChiTietDonNhap;
import com.petshop.model.DonNhapHang;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Dialog hiển thị chi tiết đơn nhập hàng.
 * Bao gồm thông tin đơn hàng, thông tin truy vết (người hủy, lý do hủy / người nhận, ngày giờ nhận)
 * và bảng danh sách sản phẩm trong đơn.
 */
public class XemChiTietDonNhapView extends Stage {

    // Định dạng số tiền và ngày tháng
    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DINH_DANG_NGAY_GIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private DonNhapHang donHang;
    private final DonNhapHangController donHangController;

    // Bảng chi tiết đơn hàng
    private TableView<ChiTietDonNhap> bangChiTiet;

    /**
     * Khởi tạo dialog xem chi tiết đơn nhập.
     *
     * @param donHang đơn hàng cần xem chi tiết
     */
    public XemChiTietDonNhapView(DonNhapHang donHang) {
        this.donHangController = new DonNhapHangController();
        // Luôn nạp lại đơn hàng từ CSDL để đảm bảo đầy đủ lý do hủy, người hủy, người nhận mới nhất
        DonNhapHang dhMoi = (donHang != null && donHang.getMaDonHang() != null)
                ? this.donHangController.timTheoMa(donHang.getMaDonHang())
                : null;
        this.donHang = (dhMoi != null) ? dhMoi : donHang;

        khoiTaoGiaoDien();
        taiDuLieu();
    }

    /**
     * Khởi tạo giao diện dialog.
     */
    private void khoiTaoGiaoDien() {
        setTitle("Chi Tiết Đơn Nhập Hàng #" + (donHang != null ? donHang.getMaDonHang() : ""));
        initModality(Modality.APPLICATION_MODAL);
        setResizable(true);
        setMinWidth(850);
        setMinHeight(600);

        // Bố cục chính
        VBox boCucChinh = new VBox(14);
        boCucChinh.setPadding(new Insets(18));
        boCucChinh.getStyleClass().add("khung-phan-he");

        // Tiêu đề
        Label tieuDe = new Label("CHI TIẾT ĐƠN NHẬP HÀNG");
        tieuDe.getStyleClass().add("tieu-de-phan-he");

        // Mô tả
        Label moTa = new Label("Thông tin chi tiết và danh sách sản phẩm của đơn nhập #" + (donHang != null ? donHang.getMaDonHang() : ""));
        moTa.getStyleClass().add("mo-ta-phan-he");

        // Phần thông tin đơn hàng
        VBox hopThongTin = taoHopThongTinDonHang();

        // Phần bảng chi tiết sản phẩm
        VBox hopBang = taoHopBangChiTiet();
        VBox.setVgrow(hopBang, Priority.ALWAYS);

        // Nút đóng
        HBox hopNut = taoHopNut();

        boCucChinh.getChildren().addAll(tieuDe, moTa, hopThongTin, hopBang, hopNut);

        // Tạo scene và áp dụng CSS
        Scene scene = new Scene(boCucChinh, 880, 620);
        if (getClass().getResource("/com/petshop/view/css/chung.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/com/petshop/view/css/chung.css").toExternalForm());
        }
        setScene(scene);
    }

    /**
     * Tạo hộp hiển thị thông tin đơn hàng và truy vết nhận/hủy.
     */
    private VBox taoHopThongTinDonHang() {
        VBox hopThongTin = new VBox(10);
        hopThongTin.getStyleClass().add("hop-noi-dung");
        hopThongTin.setPadding(new Insets(14));

        if (donHang == null) {
            hopThongTin.getChildren().add(new Label("Không có thông tin đơn hàng."));
            return hopThongTin;
        }

        // Hàng 1: Mã đơn hàng, Ngày lập, Thời gian lập
        HBox hang1 = new HBox(20);
        hang1.getChildren().addAll(
                taoNhan("Mã đơn hàng:", donHang.getMaDonHang()),
                taoNhan("Ngày lập:", donHang.getNgayLap() != null
                        ? donHang.getNgayLap().format(DINH_DANG_NGAY) : ""),
                taoNhan("Thời gian:", donHang.getGioLap() != null
                        ? donHang.getGioLap().format(DINH_DANG_GIO) : "")
        );

        // Hàng 2: Nhà cung cấp và nhân viên lập
        HBox hang2 = new HBox(20);
        hang2.getChildren().addAll(
                taoNhan("Nhà cung cấp:", donHang.getTenNhaCungCap() != null ? donHang.getTenNhaCungCap() : donHang.getMaNhaCungCap()),
                taoNhan("Nhân viên lập:", donHang.getTenNhanVien() != null ? donHang.getTenNhanVien() : donHang.getMaNhanVien())
        );

        // Hàng 3: Tổng tiền đặt và Trạng thái
        HBox hang3 = new HBox(20);
        hang3.getChildren().addAll(
                taoNhan("Tổng tiền đặt:", donHang.getTongTien() != null
                        ? DINH_DANG_TIEN.format(donHang.getTongTien()) + " đ" : "0 đ"),
                taoNhanTrangThai("Trạng thái:", donHang.getTrangThai())
        );

        hopThongTin.getChildren().addAll(hang1, hang2, hang3);

        // Nếu trạng thái là "Đã hủy" -> hiển thị Người hủy và Lý do hủy nổi bật
        if (DonNhapHang.TRANG_THAI_DA_HUY.equals(donHang.getTrangThai())) {
            HBox hangHuy = new HBox(20);
            hangHuy.setStyle("-fx-padding: 10; -fx-background-radius: 6; -fx-border-color: #e2e8f0; -fx-border-radius: 6;");

            String nguoiHuy = donHang.getTenNhanVienHuy() != null ? donHang.getTenNhanVienHuy()
                    : (donHang.getMaNhanVienHuy() != null ? donHang.getMaNhanVienHuy() : "Không xác định");
            String lyDo = donHang.getLyDoHuy() != null && !donHang.getLyDoHuy().isBlank()
                    ? donHang.getLyDoHuy() : "Chưa cập nhật lý do hủy";

            Label lblLyDoTieuDe = new Label("Lý do hủy:");
            lblLyDoTieuDe.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

            Label lblLyDoNoiDung = new Label(lyDo);
            lblLyDoNoiDung.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
            lblLyDoNoiDung.setWrapText(true);

            HBox hopLyDo = new HBox(6, lblLyDoTieuDe, lblLyDoNoiDung);
            HBox.setHgrow(hopLyDo, Priority.ALWAYS);

            hangHuy.getChildren().addAll(
                    taoNhan("Người hủy:", nguoiHuy),
                    hopLyDo
            );
            hopThongTin.getChildren().add(hangHuy);
        }

        // Nếu trạng thái là "Hoàn tất" -> hiển thị Người nhận, Thời gian nhận, Tổng tiền thực nhận
        if (DonNhapHang.TRANG_THAI_HOAN_TAT.equals(donHang.getTrangThai())) {
            HBox hangNhan = new HBox(20);
            hangNhan.setStyle("-fx-padding: 10; -fx-background-radius: 6; -fx-border-color: #e2e8f0; -fx-border-radius: 6;");

            String nguoiNhan = donHang.getTenNhanVienNhan() != null ? donHang.getTenNhanVienNhan()
                    : (donHang.getMaNhanVienNhan() != null ? donHang.getMaNhanVienNhan() : "Không xác định");
            String gioNhan = donHang.getNgayGioNhan() != null ? donHang.getNgayGioNhan().format(DINH_DANG_NGAY_GIO) : "Chưa ghi nhận";
            BigDecimal ttTN = donHang.getTongTienThucNhan() != null ? donHang.getTongTienThucNhan() : donHang.getTongTien();

            hangNhan.getChildren().addAll(
                    taoNhan("Người nhận hàng:", nguoiNhan),
                    taoNhan("Thời gian nhận:", gioNhan),
                    taoNhan("Tổng tiền thực nhận:", DINH_DANG_TIEN.format(ttTN) + " đ")
            );
            hopThongTin.getChildren().add(hangNhan);

            // Nếu có ghi chú xử lý thiếu hàng (lưu trong lyDoHuy)
            if (donHang.getLyDoHuy() != null && !donHang.getLyDoHuy().isBlank()) {
                HBox hangGhiChu = new HBox(10);
                hangGhiChu.setStyle("-fx-padding: 8; -fx-background-radius: 6; -fx-border-color: #e2e8f0; -fx-border-radius: 6;");
                Label lblGhiChu = new Label("Ghi chú / Xử lý thiếu: " + donHang.getLyDoHuy());
                lblGhiChu.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
                hangGhiChu.getChildren().add(lblGhiChu);
                hopThongTin.getChildren().add(hangGhiChu);
            }
        }

        return hopThongTin;
    }

    /**
     * Tạo cặp nhãn tiêu đề - giá trị.
     */
    private HBox taoNhan(String tieuDe, String giaTri) {
        HBox hop = new HBox(5);
        hop.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(hop, Priority.ALWAYS);

        Label nhanTieuDe = new Label(tieuDe);
        nhanTieuDe.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
        nhanTieuDe.setMinWidth(Region.USE_PREF_SIZE);

        Label nhanGiaTri = new Label(giaTri != null ? giaTri : "");
        nhanGiaTri.setStyle("-fx-text-fill: #2c3e50;");

        hop.getChildren().addAll(nhanTieuDe, nhanGiaTri);
        return hop;
    }

    /**
     * Tạo cặp nhãn tiêu đề - trạng thái có tô màu tương ứng.
     */
    private HBox taoNhanTrangThai(String tieuDe, String trangThai) {
        HBox hop = new HBox(5);
        hop.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(hop, Priority.ALWAYS);

        Label nhanTieuDe = new Label(tieuDe);
        nhanTieuDe.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
        nhanTieuDe.setMinWidth(Region.USE_PREF_SIZE);

        Label nhanGiaTri = new Label(trangThai != null ? trangThai : "");
        String mauTrangThai = switch (trangThai != null ? trangThai : "") {
            case "Chờ nhận hàng" -> "#D2691E";
            case "Hoàn tất" -> "#228B22";
            case "Đã hủy" -> "#DC143C";
            default -> "#2c3e50";
        };
        nhanGiaTri.setStyle("-fx-font-weight: bold; -fx-text-fill: " + mauTrangThai + ";");

        hop.getChildren().addAll(nhanTieuDe, nhanGiaTri);
        return hop;
    }

    /**
     * Tạo hộp chứa bảng chi tiết sản phẩm.
     */
    @SuppressWarnings("unchecked")
    private VBox taoHopBangChiTiet() {
        VBox hopBang = new VBox(8);
        VBox.setVgrow(hopBang, Priority.ALWAYS);

        Label tieuDeBang = new Label("Danh sách sản phẩm trong đơn nhập");
        tieuDeBang.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");

        // Khởi tạo bảng
        bangChiTiet = new TableView<>();
        bangChiTiet.getStyleClass().add("bang-du-lieu");
        bangChiTiet.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        bangChiTiet.setPlaceholder(new Label("Không có sản phẩm nào trong đơn hàng"));
        VBox.setVgrow(bangChiTiet, Priority.ALWAYS);

        // Cột STT
        TableColumn<ChiTietDonNhap, Number> cotSTT = new TableColumn<>("STT");
        cotSTT.setMinWidth(45);
        cotSTT.setMaxWidth(60);
        cotSTT.setStyle("-fx-alignment: CENTER;");
        cotSTT.setCellValueFactory(cellData -> {
            int index = bangChiTiet.getItems().indexOf(cellData.getValue()) + 1;
            return new ReadOnlyObjectWrapper<>(index);
        });

        // Cột mã sản phẩm
        TableColumn<ChiTietDonNhap, String> cotMaSP = new TableColumn<>("Mã SP");
        cotMaSP.setMinWidth(110);
        cotMaSP.setCellValueFactory(new PropertyValueFactory<>("maSanPham"));

        // Cột tên sản phẩm
        TableColumn<ChiTietDonNhap, String> cotTenSP = new TableColumn<>("Tên sản phẩm");
        cotTenSP.setMinWidth(200);
        cotTenSP.setCellValueFactory(cellData -> {
            String ten = cellData.getValue().getTenSanPham();
            return new SimpleStringProperty(ten != null ? ten : cellData.getValue().getMaSanPham());
        });

        // Cột đơn vị tính
        TableColumn<ChiTietDonNhap, String> cotDVT = new TableColumn<>("ĐVT");
        cotDVT.setMinWidth(70);
        cotDVT.setMaxWidth(90);
        cotDVT.setStyle("-fx-alignment: CENTER;");
        cotDVT.setCellValueFactory(new PropertyValueFactory<>("donViTinh"));

        // Cột số lượng đặt
        TableColumn<ChiTietDonNhap, Integer> cotSoLuong = new TableColumn<>("SL Đặt");
        cotSoLuong.setMinWidth(80);
        cotSoLuong.setStyle("-fx-alignment: CENTER-RIGHT;");
        cotSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuong"));

        // Cột số lượng thực nhận
        TableColumn<ChiTietDonNhap, String> cotThucNhan = new TableColumn<>("SL Thực nhận");
        cotThucNhan.setMinWidth(90);
        cotThucNhan.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");
        cotThucNhan.setCellValueFactory(cellData -> {
            Integer tn = cellData.getValue().getSoLuongThucNhan();
            return new SimpleStringProperty(tn != null ? String.valueOf(tn) : "-");
        });

        // Cột chênh lệch (nếu giao thiếu)
        TableColumn<ChiTietDonNhap, String> cotChenhLech = new TableColumn<>("Chênh lệch");
        cotChenhLech.setMinWidth(95);
        cotChenhLech.setCellValueFactory(cellData -> {
            Integer tn = cellData.getValue().getSoLuongThucNhan();
            int dat = cellData.getValue().getSoLuong();
            if (tn == null) return new SimpleStringProperty("-");
            int diff = tn - dat;
            if (diff < 0) {
                return new SimpleStringProperty(diff + " (Thiếu)");
            }
            return new SimpleStringProperty("0");
        });
        cotChenhLech.setStyle("-fx-alignment: CENTER-RIGHT;");

        // Cột đơn giá
        TableColumn<ChiTietDonNhap, String> cotDonGia = new TableColumn<>("Đơn giá (đ)");
        cotDonGia.setMinWidth(110);
        cotDonGia.setStyle("-fx-alignment: CENTER-RIGHT;");
        cotDonGia.setCellValueFactory(cellData -> {
            BigDecimal donGia = cellData.getValue().getDonGia();
            return new SimpleStringProperty(donGia != null ? DINH_DANG_TIEN.format(donGia) : "0");
        });

        // Cột thành tiền
        TableColumn<ChiTietDonNhap, String> cotThanhTien = new TableColumn<>("Thành tiền (đ)");
        cotThanhTien.setMinWidth(120);
        cotThanhTien.setStyle("-fx-alignment: CENTER-RIGHT;");
        cotThanhTien.setCellValueFactory(cellData -> {
            BigDecimal tt = cellData.getValue().getThanhTien();
            return new SimpleStringProperty(tt != null ? DINH_DANG_TIEN.format(tt) : "0");
        });

        // Thêm các cột vào bảng
        bangChiTiet.getColumns().addAll(cotSTT, cotMaSP, cotTenSP, cotDVT, cotSoLuong, cotThucNhan, cotChenhLech, cotDonGia, cotThanhTien);

        hopBang.getChildren().addAll(tieuDeBang, bangChiTiet);
        return hopBang;
    }

    /**
     * Tạo hộp chứa nút đóng.
     */
    private HBox taoHopNut() {
        HBox hopNut = new HBox();
        hopNut.setAlignment(Pos.CENTER_RIGHT);
        hopNut.setPadding(new Insets(6, 0, 0, 0));

        Button nutDong = new Button("Đóng");
        nutDong.getStyleClass().add("nut-phu");
        nutDong.setOnAction(e -> close());

        hopNut.getChildren().add(nutDong);
        return hopNut;
    }

    /**
     * Tải dữ liệu chi tiết đơn hàng từ controller.
     */
    private void taiDuLieu() {
        if (donHang == null || donHang.getMaDonHang() == null) return;
        try {
            List<ChiTietDonNhap> danhSachChiTiet =
                    donHangController.layChiTietTheoMaDonHang(donHang.getMaDonHang());
            ObservableList<ChiTietDonNhap> duLieu = FXCollections.observableArrayList(danhSachChiTiet);
            bangChiTiet.setItems(duLieu);
        } catch (Exception e) {
            bangChiTiet.setPlaceholder(new Label("Lỗi khi tải dữ liệu chi tiết đơn hàng: " + e.getMessage()));
        }
    }

    /**
     * Mở dialog xem chi tiết đơn nhập hàng.
     *
     * @param chuSoHuu cửa sổ cha
     * @param donHang  đơn hàng cần xem chi tiết
     */
    public static void moDialog(Window chuSoHuu, DonNhapHang donHang) {
        XemChiTietDonNhapView dialog = new XemChiTietDonNhapView(donHang);
        if (chuSoHuu != null) {
            dialog.initOwner(chuSoHuu);
        }
        dialog.showAndWait();
    }
}
