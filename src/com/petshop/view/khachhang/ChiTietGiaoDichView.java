package com.petshop.view.khachhang;

import com.petshop.controller.KhachHangController;
import com.petshop.model.ChiTietGiaoDich;
import com.petshop.model.LichSuGiaoDich;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Hộp thoại (Modal Dialog) hiển thị thông tin chi tiết của một hóa đơn giao dịch.
 * Bao gồm:
 * - Thông tin hóa đơn (Mã HĐ, Ngày lập, Khách hàng, Nhân viên, Hình thức TT, Trạng thái TT).
 * - Bảng danh sách sản phẩm trong hóa đơn.
 * - Bảng danh sách dịch vụ chăm sóc trong hóa đơn.
 * - Tổng thanh toán thực tế đã lưu của hóa đơn.
 */
public class ChiTietGiaoDichView extends VBox {

    private static final String CSS_CHUNG = "/com/petshop/view/css/chung.css";
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 'đ'");

    private final KhachHangController khachHangController = new KhachHangController();
    private final LichSuGiaoDich hoaDon;

    // Bảng danh sách sản phẩm và dịch vụ
    private final TableView<ChiTietGiaoDich> tblSanPham = new TableView<>();
    private final TableView<ChiTietGiaoDich> tblDichVu = new TableView<>();

    // Nút đóng
    private final Button btnDong = new Button("Đóng");

    public ChiTietGiaoDichView(LichSuGiaoDich hoaDon) {
        this.hoaDon = hoaDon;

        setSpacing(14);
        setPadding(new Insets(18));
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề hộp thoại
        Label lblTieuDe = new Label("Chi tiết giao dịch");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        // 2. Thẻ thông tin hóa đơn (3 cột)
        HBox pnlThongTin = taoTheThongTinHoaDon();

        // 3. Khung bảng sản phẩm
        VBox pnlSanPham = taoBangSanPham();

        // 4. Khung bảng dịch vụ
        VBox pnlDichVu = taoBangDichVu();

        // 5. Thanh tổng kết thanh toán và nút đóng
        VBox pnlTongKet = taoThanhTongKetVaNutDong();

        getChildren().addAll(lblTieuDe, pnlThongTin, pnlSanPham, pnlDichVu, pnlTongKet);

        // Nạp dữ liệu sản phẩm và dịch vụ
        taiDuLieuChiTiet();
    }

    /**
     * Tạo thẻ hiển thị thông tin tổng quan của hóa đơn chia làm 3 cột.
     */
    private HBox taoTheThongTinHoaDon() {
        HBox pnlThe = new HBox(24);
        pnlThe.getStyleClass().add("hop-noi-dung");
        pnlThe.setAlignment(Pos.CENTER_LEFT);

        // Cột 1: Mã hóa đơn & Nhân viên lập
        VBox col1 = new VBox(10);
        col1.setMinWidth(190);
        VBox boxMaHD = taoDongThongTin("Mã hóa đơn", hoaDon.getMaHoaDon());
        VBox boxNV = taoDongThongTin("Nhân viên lập", hoaDon.getTenNhanVien() != null ? hoaDon.getTenNhanVien() : "—");
        col1.getChildren().addAll(boxMaHD, boxNV);

        // Cột 2: Ngày lập & Hình thức thanh toán
        VBox col2 = new VBox(10);
        col2.setMinWidth(170);
        String ngayLapText = hoaDon.getNgayLap() != null ? hoaDon.getNgayLap().format(DINH_DANG_NGAY) : "—";
        VBox boxNgayLap = taoDongThongTin("Ngày lập", ngayLapText);
        String htttText = hoaDon.getHinhThucThanhToan() != null && !hoaDon.getHinhThucThanhToan().isBlank()
                ? hoaDon.getHinhThucThanhToan() : "—";
        VBox boxHTTT = taoDongThongTin("Hình thức thanh toán", htttText);
        col2.getChildren().addAll(boxNgayLap, boxHTTT);

        // Cột 3: Khách hàng & Trạng thái thanh toán
        VBox col3 = new VBox(10);
        HBox.setHgrow(col3, Priority.ALWAYS);
        VBox boxKH = taoDongThongTin("Khách hàng", hoaDon.getTenKhachHang() != null ? hoaDon.getTenKhachHang() : "—");

        VBox boxTT = new VBox(4);
        Label lblTieuDeTT = new Label("Trạng thái thanh toán");
        lblTieuDeTT.getStyleClass().add("nhan-chu-thich-nho");
        Label lblGiaTriTT = new Label(hoaDon.getTrangThaiThanhToan());
        ganStyleBadgeTrangThai(lblGiaTriTT, hoaDon.getTrangThaiThanhToan());
        boxTT.getChildren().addAll(lblTieuDeTT, lblGiaTriTT);
        col3.getChildren().addAll(boxKH, boxTT);

        pnlThe.getChildren().addAll(col1, col2, col3);
        return pnlThe;
    }

    /**
     * Tạo một cụm gồm nhãn tiêu đề nhỏ bên trên và giá trị in đậm bên dưới.
     */
    private VBox taoDongThongTin(String tieuDe, String giaTri) {
        VBox box = new VBox(4);
        Label lblTieuDe = new Label(tieuDe);
        lblTieuDe.getStyleClass().add("nhan-chu-thich-nho");

        Label lblGiaTri = new Label(giaTri);
        lblGiaTri.getStyleClass().add("nhan-gia-tri-thong-tin");
        box.getChildren().addAll(lblTieuDe, lblGiaTri);
        return box;
    }

    /**
     * Gán style class badge trạng thái thanh toán.
     */
    private void ganStyleBadgeTrangThai(Label lblBadge, String trangThai) {
        if ("Đã thanh toán".equalsIgnoreCase(trangThai)) {
            lblBadge.getStyleClass().add("nhan-trang-thai-hoat-dong");
        } else if ("Chưa thanh toán".equalsIgnoreCase(trangThai)) {
            lblBadge.getStyleClass().add("nhan-chua-thanh-toan");
        } else {
            lblBadge.getStyleClass().add("nhan-da-huy");
        }
    }

    /**
     * Tạo phần hiển thị bảng danh sách sản phẩm.
     */
    private VBox taoBangSanPham() {
        VBox pnlKhung = new VBox(8);

        Label lblTieuDe = new Label("Sản phẩm");
        lblTieuDe.getStyleClass().add("tieu-de-bang");

        cauHinhBangChiTiet(tblSanPham, "sản phẩm");
        pnlKhung.getChildren().addAll(lblTieuDe, tblSanPham);
        return pnlKhung;
    }

    /**
     * Tạo phần hiển thị bảng danh sách dịch vụ chăm sóc.
     */
    private VBox taoBangDichVu() {
        VBox pnlKhung = new VBox(8);

        Label lblTieuDe = new Label("Dịch vụ");
        lblTieuDe.getStyleClass().add("tieu-de-bang");

        cauHinhBangChiTiet(tblDichVu, "dịch vụ");
        pnlKhung.getChildren().addAll(lblTieuDe, tblDichVu);
        return pnlKhung;
    }

    /**
     * Thiết lập cấu hình các cột chung cho bảng chi tiết sản phẩm / dịch vụ.
     */
    private void cauHinhBangChiTiet(TableView<ChiTietGiaoDich> bang, String loaiDoiTuong) {
        bang.getStyleClass().add("bang-du-lieu");
        bang.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        bang.setPrefHeight(130);
        bang.setPlaceholder(new Label("Không có " + loaiDoiTuong + " nào trong hóa đơn này."));

        TableColumn<ChiTietGiaoDich, String> colTen = new TableColumn<>("Tên " + loaiDoiTuong);
        colTen.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTenDoiTuong()));
        colTen.getStyleClass().add("cot-can-trai");

        TableColumn<ChiTietGiaoDich, String> colSoLuong = new TableColumn<>("Số lượng");
        colSoLuong.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getSoLuong())));
        colSoLuong.getStyleClass().add("cot-can-giua");
        colSoLuong.setPrefWidth(90);
        colSoLuong.setMaxWidth(110);

        TableColumn<ChiTietGiaoDich, String> colDonGia = new TableColumn<>("Đơn giá");
        colDonGia.setCellValueFactory(cell -> new SimpleStringProperty(DINH_DANG_TIEN.format(cell.getValue().getDonGia())));
        colDonGia.getStyleClass().add("cot-can-phai");
        colDonGia.setPrefWidth(130);

        TableColumn<ChiTietGiaoDich, String> colThanhTien = new TableColumn<>("Thành tiền");
        colThanhTien.setCellValueFactory(cell -> new SimpleStringProperty(DINH_DANG_TIEN.format(cell.getValue().getThanhTien())));
        colThanhTien.getStyleClass().addAll("cot-can-phai", "cot-in-dam");
        colThanhTien.setPrefWidth(140);

        bang.getColumns().addAll(colTen, colSoLuong, colDonGia, colThanhTien);
    }

    /**
     * Tạo thanh tổng kết thanh toán thực tế và nút Đóng.
     */
    private VBox taoThanhTongKetVaNutDong() {
        VBox pnlDuoi = new VBox(12);

        // Hàng tổng kết tiền
        HBox pnlTongKet = new HBox(16);
        pnlTongKet.setAlignment(Pos.CENTER_LEFT);

        VBox boxNhanTong = new VBox(3);
        Label lblTongTieuDe = new Label("Tổng thanh toán thực tế");
        lblTongTieuDe.getStyleClass().add("tieu-de-bang");
        Label lblTongMoTa = new Label("Theo tổng tiền thực tế đã lưu của hóa đơn.");
        lblTongMoTa.getStyleClass().add("nhan-chu-thich-nho");
        boxNhanTong.getChildren().addAll(lblTongTieuDe, lblTongMoTa);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblTongTien = new Label(DINH_DANG_TIEN.format(hoaDon.getTongTien()));
        lblTongTien.getStyleClass().addAll("nhan-tong-tien-noi-bat");

        pnlTongKet.getChildren().addAll(boxNhanTong, spacer, lblTongTien);

        // Hàng nút Đóng căn phải
        HBox pnlNut = new HBox();
        pnlNut.setAlignment(Pos.CENTER_RIGHT);
        btnDong.getStyleClass().add("nut-phu");
        btnDong.setOnAction(e -> dongCuaSo());
        pnlNut.getChildren().add(btnDong);

        pnlDuoi.getChildren().addAll(pnlTongKet, pnlNut);
        return pnlDuoi;
    }

    /**
     * Tải dữ liệu danh sách sản phẩm và dịch vụ của hóa đơn từ cơ sở dữ liệu.
     */
    private void taiDuLieuChiTiet() {
        if (hoaDon == null) return;
        List<ChiTietGiaoDich> dsSP = khachHangController.laySanPhamHoaDon(hoaDon.getMaKhachHang(), hoaDon.getMaHoaDon());
        tblSanPham.setItems(FXCollections.observableArrayList(dsSP));

        List<ChiTietGiaoDich> dsDV = khachHangController.layDichVuHoaDon(hoaDon.getMaKhachHang(), hoaDon.getMaHoaDon());
        tblDichVu.setItems(FXCollections.observableArrayList(dsDV));
    }

    /**
     * Đóng cửa sổ Stage modal.
     */
    private void dongCuaSo() {
        if (getScene() != null && getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
    }

    /**
     * Phương thức tĩnh hiển thị nhanh hộp thoại Chi tiết giao dịch dạng Modal Dialog.
     *
     * @param hoaDon Hóa đơn giao dịch cần xem
     * @param cuaSoCha Cửa sổ cha sở hữu modal
     */
    public static void hienThi(LichSuGiaoDich hoaDon, Window cuaSoCha) {
        if (hoaDon == null) {
            return;
        }

        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        if (cuaSoCha != null) {
            dlg.initOwner(cuaSoCha);
        }
        dlg.setTitle("Chi tiết giao dịch - " + hoaDon.getMaHoaDon());

        ChiTietGiaoDichView pnlNoiDung = new ChiTietGiaoDichView(hoaDon);
        Scene scene = new Scene(pnlNoiDung, 720, 580);

        if (ChiTietGiaoDichView.class.getResource(CSS_CHUNG) != null) {
            scene.getStylesheets().add(ChiTietGiaoDichView.class.getResource(CSS_CHUNG).toExternalForm());
        }

        dlg.setScene(scene);
        dlg.setResizable(false);
        dlg.showAndWait();
    }
}

