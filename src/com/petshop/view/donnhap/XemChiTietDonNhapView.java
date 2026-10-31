package com.petshop.view.donnhap;

import com.petshop.controller.DonHangController;
import com.petshop.model.ChiTietDonHang;
import com.petshop.model.DonHang;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Dialog hiển thị chi tiết đơn nhập hàng.
 * Bao gồm thông tin đơn hàng và bảng danh sách sản phẩm trong đơn.
 */
public class XemChiTietDonNhapView extends Stage {

    // Định dạng số tiền và ngày tháng
    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final DonHang donHang;
    private final DonHangController donHangController;

    // Bảng chi tiết đơn hàng
    private TableView<ChiTietDonHang> bangChiTiet;

    /**
     * Khởi tạo dialog xem chi tiết đơn nhập.
     *
     * @param donHang đơn hàng cần xem chi tiết
     */
    public XemChiTietDonNhapView(DonHang donHang) {
        this.donHang = donHang;
        this.donHangController = new DonHangController();

        khoiTaoGiaoDien();
        taiDuLieu();
    }

    /**
     * Khởi tạo giao diện dialog.
     */
    private void khoiTaoGiaoDien() {
        // Cấu hình cửa sổ
        setTitle("Chi Tiết Đơn Nhập Hàng");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(true);
        setMinWidth(800);
        setMinHeight(600);

        // Bố cục chính
        VBox boCucChinh = new VBox(15);
        boCucChinh.setPadding(new Insets(20));
        boCucChinh.getStyleClass().add("khung-phan-he");

        // Tiêu đề
        Label tieuDe = new Label("Chi Tiết Đơn Nhập Hàng");
        tieuDe.getStyleClass().add("tieu-de-phan-he");

        // Mô tả
        Label moTa = new Label("Thông tin chi tiết đơn nhập hàng #" + donHang.getMaDonHang());
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
        Scene scene = new Scene(boCucChinh, 850, 600);
        scene.getStylesheets().add(getClass().getResource("/com/petshop/view/css/chung.css").toExternalForm());
        setScene(scene);
    }

    /**
     * Tạo hộp hiển thị thông tin đơn hàng.
     */
    private VBox taoHopThongTinDonHang() {
        VBox hopThongTin = new VBox(10);
        hopThongTin.getStyleClass().add("hop-noi-dung");
        hopThongTin.setPadding(new Insets(15));

        // Hàng 1: Mã đơn hàng và ngày lập
        HBox hang1 = new HBox(20);
        hang1.getChildren().addAll(
                taoNhan("Mã đơn hàng:", donHang.getMaDonHang()),
                taoNhan("Ngày lập:", donHang.getNgayLap() != null
                        ? donHang.getNgayLap().format(DINH_DANG_NGAY) : ""),
                taoNhan("Thời gian:", donHang.getGioLap() != null
                        ? donHang.getGioLap().format(DINH_DANG_GIO) : "")
        );

        // Hàng 2: Nhà cung cấp và nhân viên
        HBox hang2 = new HBox(20);
        hang2.getChildren().addAll(
                taoNhan("Nhà cung cấp:", donHang.getTenNhaCungCap()),
                taoNhan("Nhân viên:", donHang.getTenNhanVien())
        );

        // Hàng 3: Tổng tiền và trạng thái
        HBox hang3 = new HBox(20);
        hang3.getChildren().addAll(
                taoNhan("Tổng tiền:", donHang.getTongTien() != null
                        ? DINH_DANG_TIEN.format(donHang.getTongTien()) + " đ" : "0 đ"),
                taoNhan("Trạng thái:", donHang.getTrangThai())
        );

        hopThongTin.getChildren().addAll(hang1, hang2, hang3);
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
        nhanTieuDe.setStyle("-fx-font-weight: bold;");
        nhanTieuDe.setMinWidth(Region.USE_PREF_SIZE);

        Label nhanGiaTri = new Label(giaTri != null ? giaTri : "");

        hop.getChildren().addAll(nhanTieuDe, nhanGiaTri);
        return hop;
    }

    /**
     * Tạo hộp chứa bảng chi tiết sản phẩm.
     */
    @SuppressWarnings("unchecked")
    private VBox taoHopBangChiTiet() {
        VBox hopBang = new VBox(10);
        VBox.setVgrow(hopBang, Priority.ALWAYS);

        Label tieuDeBang = new Label("Danh sách sản phẩm");
        tieuDeBang.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Khởi tạo bảng
        bangChiTiet = new TableView<>();
        bangChiTiet.getStyleClass().add("bang-du-lieu");
        bangChiTiet.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        bangChiTiet.setPlaceholder(new Label("Không có sản phẩm nào trong đơn hàng"));
        VBox.setVgrow(bangChiTiet, Priority.ALWAYS);

        // Cột STT
        TableColumn<ChiTietDonHang, Number> cotSTT = new TableColumn<>("STT");
        cotSTT.setMinWidth(50);
        cotSTT.setMaxWidth(60);
        cotSTT.setStyle("-fx-alignment: CENTER;");
        cotSTT.setCellValueFactory(cellData -> {
            int index = bangChiTiet.getItems().indexOf(cellData.getValue()) + 1;
            return new ReadOnlyObjectWrapper<>(index);
        });

        // Cột mã sản phẩm
        TableColumn<ChiTietDonHang, String> cotMaSP = new TableColumn<>("Mã SP");
        cotMaSP.setMinWidth(100);
        cotMaSP.setCellValueFactory(new PropertyValueFactory<>("maSanPham"));

        // Cột tên sản phẩm
        TableColumn<ChiTietDonHang, String> cotTenSP = new TableColumn<>("Tên sản phẩm");
        cotTenSP.setMinWidth(200);
        cotTenSP.setCellValueFactory(new PropertyValueFactory<>("tenSanPham"));

        // Cột đơn vị tính
        TableColumn<ChiTietDonHang, String> cotDVT = new TableColumn<>("Đơn vị tính");
        cotDVT.setMinWidth(80);
        cotDVT.setStyle("-fx-alignment: CENTER;");
        cotDVT.setCellValueFactory(new PropertyValueFactory<>("donViTinh"));

        // Cột số lượng
        TableColumn<ChiTietDonHang, Integer> cotSoLuong = new TableColumn<>("Số lượng");
        cotSoLuong.setMinWidth(80);
        cotSoLuong.setStyle("-fx-alignment: CENTER;");
        cotSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuong"));

        // Cột đơn giá
        TableColumn<ChiTietDonHang, BigDecimal> cotDonGia = new TableColumn<>("Đơn giá");
        cotDonGia.setMinWidth(120);
        cotDonGia.setStyle("-fx-alignment: CENTER-RIGHT;");
        cotDonGia.setCellValueFactory(new PropertyValueFactory<>("donGia"));
        cotDonGia.setCellFactory(col -> new TableCell<ChiTietDonHang, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(DINH_DANG_TIEN.format(item));
                }
            }
        });

        // Cột thành tiền
        TableColumn<ChiTietDonHang, BigDecimal> cotThanhTien = new TableColumn<>("Thành tiền");
        cotThanhTien.setMinWidth(120);
        cotThanhTien.setStyle("-fx-alignment: CENTER-RIGHT;");
        cotThanhTien.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getThanhTien()));
        cotThanhTien.setCellFactory(col -> new TableCell<ChiTietDonHang, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(DINH_DANG_TIEN.format(item));
                }
            }
        });

        // Thêm các cột vào bảng
        bangChiTiet.getColumns().addAll(cotSTT, cotMaSP, cotTenSP, cotDVT, cotSoLuong, cotDonGia, cotThanhTien);

        hopBang.getChildren().addAll(tieuDeBang, bangChiTiet);
        return hopBang;
    }

    /**
     * Tạo hộp chứa nút đóng.
     */
    private HBox taoHopNut() {
        HBox hopNut = new HBox();
        hopNut.setAlignment(Pos.CENTER_RIGHT);
        hopNut.setPadding(new Insets(10, 0, 0, 0));

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
        try {
            List<ChiTietDonHang> danhSachChiTiet =
                    donHangController.layChiTietTheoMaDonHang(donHang.getMaDonHang());
            ObservableList<ChiTietDonHang> duLieu = FXCollections.observableArrayList(danhSachChiTiet);
            bangChiTiet.setItems(duLieu);
        } catch (Exception e) {
            bangChiTiet.setPlaceholder(new Label("Lỗi khi tải dữ liệu chi tiết đơn hàng"));
            e.printStackTrace();
        }
    }

    /**
     * Mở dialog xem chi tiết đơn nhập hàng.
     *
     * @param chuSoHuu cửa sổ cha
     * @param donHang  đơn hàng cần xem chi tiết
     */
    public static void moDialog(Window chuSoHuu, DonHang donHang) {
        XemChiTietDonNhapView dialog = new XemChiTietDonNhapView(donHang);
        if (chuSoHuu != null) {
            dialog.initOwner(chuSoHuu);
        }
        dialog.showAndWait();
    }
}
