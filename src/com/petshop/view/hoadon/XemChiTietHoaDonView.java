package com.petshop.view.hoadon;

import com.petshop.controller.HoaDonController;
import com.petshop.model.ChiTietHoaDonDichVu;
import com.petshop.model.ChiTietHoaDonSanPham;
import com.petshop.model.DongHoaDon;
import com.petshop.model.HoaDon;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Cửa sổ xem chi tiết hóa đơn bán lẻ, hỗ trợ in hóa đơn và hủy hóa đơn (INV_CANCEL) hoàn lại tồn kho.
 */
public class XemChiTietHoaDonView extends Stage {

    private static final String CSS_CHUNG = "/com/petshop/view/css/chung.css";
    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 VNĐ");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final HoaDonController controller;
    private HoaDon hoaDon;
    private final Runnable khiCapNhatThanhCong;

    private final Label lblTrangThai = new Label();
    private final Button btnHuyHoaDon = new Button("✕ Hủy hóa đơn");
    private final ObservableList<DongHoaDon> dsDong = FXCollections.observableArrayList();

    public XemChiTietHoaDonView(HoaDon hoaDon, Stage chuSoHuu, Runnable khiCapNhatThanhCong) {
        this.hoaDon = hoaDon;
        this.controller = new HoaDonController();
        this.khiCapNhatThanhCong = khiCapNhatThanhCong;

        initModality(Modality.APPLICATION_MODAL);
        if (chuSoHuu != null) initOwner(chuSoHuu);
        setTitle("Chi tiết hóa đơn - " + hoaDon.getMaHoaDon());

        VBox pnlGoc = new VBox(12);
        pnlGoc.setPadding(new Insets(16));
        pnlGoc.getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("CHI TIẾT HÓA ĐƠN BÁN LẺ");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        Label lblMoTa = new Label("Xem chi tiết các mặt hàng, dịch vụ đã thanh toán và lịch sử giao dịch.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Thẻ thông tin hóa đơn
        GridPane luoiThongTin = new GridPane();
        luoiThongTin.setHgap(16);
        luoiThongTin.setVgap(8);
        luoiThongTin.setPadding(new Insets(12));
        luoiThongTin.getStyleClass().add("hop-noi-dung");

        capNhatNhanTrangThai();

        luoiThongTin.add(new Label("Mã hóa đơn:"), 0, 0);
        Label lblMa = new Label(hoaDon.getMaHoaDon());
        lblMa.setStyle("-fx-font-weight: bold; -fx-text-fill: #0284c7;");
        luoiThongTin.add(lblMa, 1, 0);

        luoiThongTin.add(new Label("Trạng thái:"), 2, 0);
        luoiThongTin.add(lblTrangThai, 3, 0);

        luoiThongTin.add(new Label("Ngày lập:"), 0, 1);
        luoiThongTin.add(new Label(hoaDon.getNgayLap() != null ? hoaDon.getNgayLap().format(DINH_DANG_NGAY) : "--"), 1, 1);

        luoiThongTin.add(new Label("Thu ngân / NV:"), 2, 1);
        luoiThongTin.add(new Label(hoaDon.getTenNhanVien() != null ? hoaDon.getTenNhanVien() : hoaDon.getMaNhanVien()), 3, 1);

        luoiThongTin.add(new Label("Khách hàng:"), 0, 2);
        String tenKh = hoaDon.getTenKhachHang() != null ? hoaDon.getTenKhachHang() : "Khách vãng lai";
        if (hoaDon.getSoDienThoaiKhachHang() != null && !hoaDon.getSoDienThoaiKhachHang().isBlank()) {
            tenKh += " (" + hoaDon.getSoDienThoaiKhachHang() + ")";
        }
        luoiThongTin.add(new Label(tenKh), 1, 2);

        luoiThongTin.add(new Label("Hình thức TT:"), 2, 2);
        luoiThongTin.add(new Label(hoaDon.getHinhThucThanhToan() != null ? hoaDon.getHinhThucThanhToan() : "--"), 3, 2);

        if (hoaDon.getMaPhieuDichVu() != null && !hoaDon.getMaPhieuDichVu().isBlank()) {
            luoiThongTin.add(new Label("Mã phiếu DV:"), 0, 3);
            luoiThongTin.add(new Label(hoaDon.getMaPhieuDichVu()), 1, 3);
        }

        // 3. Bảng danh sách chi tiết
        TableView<DongHoaDon> bangChiTiet = taoBangChiTiet();
        napDuLieuVaoBang();

        // 4. Khung tổng kết tài chính
        GridPane luoiTongKet = new GridPane();
        luoiTongKet.setHgap(16);
        luoiTongKet.setVgap(6);
        luoiTongKet.setAlignment(Pos.CENTER_RIGHT);
        luoiTongKet.setPadding(new Insets(8, 12, 8, 12));

        BigDecimal tongGoc = hoaDon.tinhTongTienGoc();
        int row = 0;
        luoiTongKet.add(new Label("Tổng tiền hàng:"), 0, row);
        luoiTongKet.add(new Label(DINH_DANG_TIEN.format(tongGoc)), 1, row++);

        if (hoaDon.getTienGiam() != null && hoaDon.getTienGiam().compareTo(BigDecimal.ZERO) > 0) {
            String kmStr = "Giảm giá (" + (hoaDon.getMaKhuyenMai() != null ? hoaDon.getMaKhuyenMai() : "KM") + "):";
            luoiTongKet.add(new Label(kmStr), 0, row);
            Label lblGiam = new Label("-" + DINH_DANG_TIEN.format(hoaDon.getTienGiam()));
            lblGiam.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
            luoiTongKet.add(lblGiam, 1, row++);
        }

        if (hoaDon.getDiemTichLuySuDung() > 0) {
            luoiTongKet.add(new Label("Dùng điểm (" + hoaDon.getDiemTichLuySuDung() + " đ):"), 0, row);
            Label lblGiamDiem = new Label("-" + DINH_DANG_TIEN.format(hoaDon.getTienGiamTuDiem()));
            lblGiamDiem.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
            luoiTongKet.add(lblGiamDiem, 1, row++);
        }

        Label lblTieuDeThanhToan = new Label("Tổng thanh toán:");
        lblTieuDeThanhToan.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label lblGiaTriThanhToan = new Label(DINH_DANG_TIEN.format(hoaDon.getTongTien()));
        lblGiaTriThanhToan.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #0284c7;");
        luoiTongKet.add(lblTieuDeThanhToan, 0, row);
        luoiTongKet.add(lblGiaTriThanhToan, 1, row++);

        if (hoaDon.getDiemTichLuyCong() > 0) {
            luoiTongKet.add(new Label("Điểm nhận thêm:"), 0, row);
            Label lblDiemCong = new Label("+" + hoaDon.getDiemTichLuyCong() + " điểm");
            lblDiemCong.setStyle("-fx-font-weight: bold; -fx-text-fill: #15803d;");
            luoiTongKet.add(lblDiemCong, 1, row++);
        }

        // 5. Thanh nút thao tác
        btnHuyHoaDon.getStyleClass().add("nut-xoa");
        btnHuyHoaDon.setDisable("Đã hủy".equalsIgnoreCase(hoaDon.getTrangThaiThanhToan()));
        btnHuyHoaDon.setOnAction(e -> xuLyHuyHoaDon());

        Button btnIn = new Button("🖨 In hóa đơn");
        btnIn.getStyleClass().add("nut-chinh");
        btnIn.setOnAction(e -> new InHoaDonDialog(hoaDon, this).showAndWait());

        Button btnDong = new Button("Đóng");
        btnDong.getStyleClass().add("nut-phu");
        btnDong.setOnAction(e -> close());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox pnlNut = new HBox(10, btnHuyHoaDon, spacer, btnIn, btnDong);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        pnlGoc.getChildren().addAll(pnlTieuDe, luoiThongTin, bangChiTiet, luoiTongKet, pnlNut);

        Scene scene = new Scene(pnlGoc, 750, 620);
        if (XemChiTietHoaDonView.class.getResource(CSS_CHUNG) != null) {
            scene.getStylesheets().add(XemChiTietHoaDonView.class.getResource(CSS_CHUNG).toExternalForm());
        }
        setScene(scene);
        setMinWidth(680);
        setMinHeight(520);
    }

    private TableView<DongHoaDon> taoBangChiTiet() {
        TableView<DongHoaDon> bang = new TableView<>();
        bang.getStyleClass().add("bang-du-lieu");
        bang.setItems(dsDong);
        VBox.setVgrow(bang, Priority.ALWAYS);

        TableColumn<DongHoaDon, Integer> colStt = new TableColumn<>("STT");
        colStt.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getStt()));
        colStt.setPrefWidth(45);
        colStt.setStyle("-fx-alignment: center;");

        TableColumn<DongHoaDon, String> colLoai = new TableColumn<>("Phân loại");
        colLoai.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenLoai()));
        colLoai.setPrefWidth(90);
        colLoai.setStyle("-fx-alignment: center;");

        TableColumn<DongHoaDon, String> colMa = new TableColumn<>("Mã hàng");
        colMa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaHang()));
        colMa.setPrefWidth(120);

        TableColumn<DongHoaDon, String> colTen = new TableColumn<>("Tên mặt hàng / Dịch vụ");
        colTen.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenHang()));
        colTen.setPrefWidth(180);

        TableColumn<DongHoaDon, String> colDvt = new TableColumn<>("ĐVT");
        colDvt.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDonViTinh()));
        colDvt.setPrefWidth(60);
        colDvt.setStyle("-fx-alignment: center;");

        TableColumn<DongHoaDon, Integer> colSl = new TableColumn<>("SL");
        colSl.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSoLuong()));
        colSl.setPrefWidth(50);
        colSl.setStyle("-fx-alignment: center-right;");

        TableColumn<DongHoaDon, String> colGia = new TableColumn<>("Đơn giá");
        colGia.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().getDonGia())));
        colGia.setPrefWidth(100);
        colGia.setStyle("-fx-alignment: center-right;");

        TableColumn<DongHoaDon, String> colThanhTien = new TableColumn<>("Thành tiền");
        colThanhTien.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().getThanhTien())));
        colThanhTien.setPrefWidth(110);
        colThanhTien.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");

        bang.getColumns().addAll(colStt, colLoai, colMa, colTen, colDvt, colSl, colGia, colThanhTien);
        return bang;
    }

    private void napDuLieuVaoBang() {
        dsDong.clear();
        int stt = 1;

        if (hoaDon.getDanhSachSanPham() != null) {
            for (ChiTietHoaDonSanPham sp : hoaDon.getDanhSachSanPham()) {
                dsDong.add(new DongHoaDon(
                        stt++,
                        DongHoaDon.LoaiDong.SAN_PHAM,
                        sp.getMaSanPham(),
                        sp.getTenSanPham() != null ? sp.getTenSanPham() : sp.getMaSanPham(),
                        sp.getDonViTinh() != null ? sp.getDonViTinh() : "Cái",
                        sp.getSoLuong(),
                        sp.getDonGia(),
                        ""
                ));
            }
        }

        if (hoaDon.getDanhSachDichVu() != null) {
            for (ChiTietHoaDonDichVu dv : hoaDon.getDanhSachDichVu()) {
                dsDong.add(new DongHoaDon(
                        stt++,
                        DongHoaDon.LoaiDong.DICH_VU,
                        dv.getMaDichVu(),
                        dv.getTenDichVu() != null ? dv.getTenDichVu() : dv.getMaDichVu(),
                        "Lần",
                        1,
                        dv.getDonGia(),
                        "Dịch vụ chăm sóc"
                ));
            }
        }
    }

    private void xuLyHuyHoaDon() {
        TextInputDialog dlgLyDo = new TextInputDialog("Khách hàng yêu cầu hủy / Sai thông tin");
        dlgLyDo.initOwner(this);
        dlgLyDo.setTitle("Xác nhận hủy hóa đơn");
        dlgLyDo.setHeaderText("Bạn có chắc chắn muốn hủy hóa đơn " + hoaDon.getMaHoaDon() + " không?\n"
                + "Toàn bộ số lượng sản phẩm trong hóa đơn sẽ được hoàn trả lại vào kho hàng!");
        dlgLyDo.setContentText("Nhập lý do hủy hóa đơn:");

        Optional<String> ketQua = dlgLyDo.showAndWait();
        if (ketQua.isEmpty()) return;

        String lyDo = ketQua.get().trim();
        try {
            if (controller.huyHoaDon(hoaDon.getMaHoaDon(), lyDo)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Đã hủy hóa đơn " + hoaDon.getMaHoaDon() + " thành công!\nSố lượng tồn kho sản phẩm đã được hoàn lại đầy đủ.");
                alert.setHeaderText(null);
                alert.showAndWait();

                hoaDon.setTrangThaiThanhToan("Đã hủy");
                capNhatNhanTrangThai();
                btnHuyHoaDon.setDisable(true);

                if (khiCapNhatThanhCong != null) {
                    khiCapNhatThanhCong.run();
                }
            }
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Lỗi khi hủy hóa đơn: " + ex.getMessage());
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    private void capNhatNhanTrangThai() {
        String tt = hoaDon.getTrangThaiThanhToan() != null ? hoaDon.getTrangThaiThanhToan() : "Đã thanh toán";
        lblTrangThai.setText(tt);
        if ("Đã hủy".equalsIgnoreCase(tt)) {
            lblTrangThai.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
        } else {
            lblTrangThai.setStyle("-fx-text-fill: #15803d; -fx-font-weight: bold;");
        }
    }
}

