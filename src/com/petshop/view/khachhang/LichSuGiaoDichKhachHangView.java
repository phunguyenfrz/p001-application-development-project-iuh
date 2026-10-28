package com.petshop.view.khachhang;

import com.petshop.controller.KhachHangController;
import com.petshop.model.KhachHang;
import com.petshop.model.LichSuGiaoDich;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Màn hình Lịch sử giao dịch khách hàng (KH_LICH_SU).
 * <p>
 * Luồng nghiệp vụ:
 * 1. Cho phép tìm kiếm và chọn khách hàng qua mã, họ tên hoặc số điện thoại.
 *    Hỗ trợ bảng gợi ý tự động (Autocomplete Popup) thả xuống ngay bên dưới ô nhập khi gõ phím.
 * 2. Hiển thị thẻ thông tin tóm tắt của khách hàng được chọn (Avatar viết tắt, Mã, Họ tên, SĐT, Email).
 * 3. Bộ lọc thời gian (Từ ngày, Đến ngày) và bộ lọc Trạng thái thanh toán (Tất cả, Đã thanh toán, Chưa thanh toán, Đã hủy).
 * 4. Bảng danh sách các hóa đơn giao dịch được sắp xếp theo ngày lập mới nhất trước.
 * 5. Nhấp đúp chuột hoặc bấm "Xem chi tiết" trên dòng hóa đơn để mở hộp thoại chi tiết (ChiTietGiaoDichView).
 * 6. Thanh tổng kết dưới đáy: hiển thị tổng số hóa đơn đang lọc và tổng tiền thực tế đã thanh toán.
 */
public class LichSuGiaoDichKhachHangView extends VBox {

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 'đ'");

    private final KhachHangController khachHangController = new KhachHangController();
    private final ObservableList<LichSuGiaoDich> danhSachHoaDon = FXCollections.observableArrayList();

    // Khách hàng hiện tại đang được chọn để xem lịch sử
    private KhachHang khachHangHienTai = null;

    // Cờ đánh dấu khi vừa chọn khách hàng từ danh sách gợi ý để tránh trigger tìm kiếm lặp
    private boolean dangChonTuGoiY = false;

    // Các thành phần tìm kiếm khách hàng
    private final TextField txtTimKhachHang = new TextField();
    private final Button btnTimKhachHang = new Button("Tìm kiếm");
    private final Button btnLamMoiKhachHang = new Button("Làm mới");

    // Popup bảng gợi ý khách hàng thả xuống ngay bên dưới ô nhập
    private final Popup popupGoiY = new Popup();
    private final ListView<KhachHang> lstGoiY = new ListView<>();

    // Thẻ hiển thị khách hàng đang chọn
    private final HBox theKhachHang = new HBox(16);
    private final Label lblAvatar = new Label("KH");
    private final Label lblTenKhachHang = new Label("Chưa chọn khách hàng");
    private final Label lblMaKhachHang = new Label("—");
    private final Label lblSoDienThoai = new Label("—");
    private final Label lblEmail = new Label("—");
    private final Button btnDoiKhachHang = new Button("Đổi khách hàng");

    // Bộ lọc hóa đơn
    private final DatePicker dpTuNgay = new DatePicker();
    private final DatePicker dpDenNgay = new DatePicker();
    private final ComboBox<String> cboTrangThaiThanhToan = new ComboBox<>();
    private final Button btnApDung = new Button("Áp dụng");
    private final Button btnXoaBoLoc = new Button("Xóa bộ lọc");

    // Bảng danh sách hóa đơn
    private final TableView<LichSuGiaoDich> tblHoaDon = new TableView<>();
    private final Button btnXemChiTiet = new Button("Xem chi tiết");

    // Thanh tổng kết số liệu
    private final Label lblKetQuaHienThi = new Label("Trong kết quả đang hiển thị:\n0 hóa đơn");
    private final Label lblTongTienThanhToan = new Label("0 đ");
    private final Label lblTongTienThanhToanTieuDe = new Label("Tổng tiền đã thanh toán • 0 hóa đơn");

    public LichSuGiaoDichKhachHangView() {
        setSpacing(16);
        setPadding(new Insets(20, 24, 20, 24));
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề màn hình
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("Lịch sử giao dịch khách hàng");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Tra cứu hóa đơn mua sắm và sử dụng dịch vụ của từng khách hàng");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Khu vực tìm kiếm khách hàng
        VBox pnlKhuVucTimKH = taoKhuVucTimKiemKhachHang();

        // 3. Khởi tạo Popup danh sách gợi ý khách hàng
        khoiTaoPopupGoiY();

        // 4. Thẻ thông tin khách hàng đang chọn
        cauHinhTheKhachHang();

        // 5. Khu vực bộ lọc hóa đơn
        VBox pnlKhuVucBoLoc = taoKhuVucBoLocHoaDon();

        // 6. Khu vực bảng danh sách hóa đơn
        VBox pnlBangHoaDon = taoKhuVucBangHoaDon();

        // 7. Thanh tổng kết dưới đáy bảng
        HBox pnlTongKet = taoThanhTongKet();

        getChildren().addAll(pnlTieuDe, pnlKhuVucTimKH, theKhachHang, pnlKhuVucBoLoc, pnlBangHoaDon, pnlTongKet);

        // Đăng ký sự kiện tương tác
        dangKySuKien();

        // Nạp mặc định khách hàng đầu tiên trong hệ thống nếu có
        taiKhachHangMacDinh();
    }

    /**
     * Tạo khu vực thanh công cụ tra cứu khách hàng.
     */
    private VBox taoKhuVucTimKiemKhachHang() {
        VBox pnlKhung = new VBox(6);

        Label lblNhan = new Label("Tìm kiếm khách hàng");
        lblNhan.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #1e293b;");

        HBox pnlThanh = new HBox(10);
        pnlThanh.setAlignment(Pos.CENTER_LEFT);

        txtTimKhachHang.setPromptText("Tìm khách hàng theo mã, họ tên, số điện thoại...");
        HBox.setHgrow(txtTimKhachHang, Priority.ALWAYS);

        btnTimKhachHang.getStyleClass().add("nut-chinh");
        btnLamMoiKhachHang.getStyleClass().add("nut-phu");

        pnlThanh.getChildren().addAll(txtTimKhachHang, btnTimKhachHang, btnLamMoiKhachHang);
        pnlKhung.getChildren().addAll(lblNhan, pnlThanh);
        return pnlKhung;
    }

    /**
     * Khởi tạo Popup bảng gợi ý khách hàng (Autocomplete).
     */
    private void khoiTaoPopupGoiY() {
        popupGoiY.setAutoHide(true);
        popupGoiY.setHideOnEscape(true);

        lstGoiY.setStyle("-fx-background-color: #ffffff; -fx-border-color: #0284c7; -fx-border-width: 1.5px; -fx-border-radius: 4px;");

        // Tùy biến hiển thị từng dòng khách hàng trong danh sách gợi ý
        lstGoiY.setCellFactory(lv -> new ListCell<>() {
            private final HBox pnlDong = new HBox(12);
            private final Label lblAvatarItem = new Label();
            private final Label lblTenItem = new Label();
            private final Label lblMaItem = new Label();
            private final Label lblSdtItem = new Label();
            private final Label lblTrangThaiItem = new Label();

            {
                pnlDong.setAlignment(Pos.CENTER_LEFT);
                lblAvatarItem.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-font-weight: bold; -fx-font-size: 12px; -fx-alignment: center; -fx-background-radius: 50%; -fx-min-width: 32px; -fx-min-height: 32px;");

                VBox boxThongTin = new VBox(2);
                lblTenItem.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #0f172a;");
                lblMaItem.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
                boxThongTin.getChildren().addAll(lblTenItem, lblMaItem);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                VBox boxPhu = new VBox(2);
                boxPhu.setAlignment(Pos.CENTER_RIGHT);
                lblSdtItem.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #334155;");
                boxPhu.getChildren().addAll(lblSdtItem, lblTrangThaiItem);

                pnlDong.getChildren().addAll(lblAvatarItem, boxThongTin, spacer, boxPhu);
            }

            @Override
            protected void updateItem(KhachHang kh, boolean empty) {
                super.updateItem(kh, empty);
                if (empty || kh == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    lblAvatarItem.setText(layKyTuVietTat(kh.getHoTen()));
                    lblTenItem.setText(kh.getHoTen());
                    lblMaItem.setText(kh.getMaKhachHang());

                    String sdt = (kh.getSoDienThoai() != null && !kh.getSoDienThoai().isBlank())
                            ? kh.getSoDienThoai() : "—";
                    lblSdtItem.setText(sdt);

                    boolean hoatDong = kh.isTrangThai();
                    lblTrangThaiItem.setText(hoatDong ? "Đang hoạt động" : "Ngừng hoạt động");
                    lblTrangThaiItem.getStyleClass().clear();
                    if (hoatDong) {
                        lblTrangThaiItem.getStyleClass().add("nhan-trang-thai-hoat-dong");
                    } else {
                        lblTrangThaiItem.getStyleClass().add("nhan-trang-thai-khoa");
                    }

                    setGraphic(pnlDong);
                }
            }
        });

        popupGoiY.getContent().add(lstGoiY);
    }

    /**
     * Cấu hình thẻ thông tin khách hàng đang được chọn.
     */
    private void cauHinhTheKhachHang() {
        theKhachHang.getStyleClass().add("hop-noi-dung");
        theKhachHang.setAlignment(Pos.CENTER_LEFT);

        // Avatar viết tắt
        lblAvatar.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-font-weight: bold; -fx-font-size: 15px; -fx-alignment: center; -fx-background-radius: 50%; -fx-min-width: 44px; -fx-min-height: 44px;");

        // Tên và mã khách hàng
        VBox boxTenMa = new VBox(3);
        boxTenMa.setMinWidth(180);
        lblTenKhachHang.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        lblMaKhachHang.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #64748b;");
        boxTenMa.getChildren().addAll(lblTenKhachHang, lblMaKhachHang);

        // Số điện thoại
        VBox boxSDT = new VBox(3);
        boxSDT.setMinWidth(140);
        Label lblTieuDeSDT = new Label("Số điện thoại");
        lblTieuDeSDT.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #64748b;");
        lblSoDienThoai.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");
        boxSDT.getChildren().addAll(lblTieuDeSDT, lblSoDienThoai);

        // Email
        VBox boxEmail = new VBox(3);
        boxEmail.setMinWidth(200);
        Label lblTieuDeEmail = new Label("Email");
        lblTieuDeEmail.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #64748b;");
        lblEmail.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");
        boxEmail.getChildren().addAll(lblTieuDeEmail, lblEmail);

        // Khoảng đệm đẩy nút đổi khách hàng sang phải
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnDoiKhachHang.getStyleClass().add("nut-phu");

        theKhachHang.getChildren().addAll(lblAvatar, boxTenMa, boxSDT, boxEmail, spacer, btnDoiKhachHang);
    }

    /**
     * Tạo khu vực bộ lọc ngày lập và trạng thái thanh toán của hóa đơn.
     */
    private VBox taoKhuVucBoLocHoaDon() {
        VBox pnlKhung = new VBox(6);

        HBox pnlBoLoc = new HBox(14);
        pnlBoLoc.setAlignment(Pos.BOTTOM_LEFT);

        // Bộ chuyển đổi định dạng ngày dd/MM/yyyy cho DatePicker
        StringConverter<LocalDate> converterNgay = new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? DINH_DANG_NGAY.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.trim().isEmpty()) {
                    try {
                        return LocalDate.parse(string.trim(), DINH_DANG_NGAY);
                    } catch (Exception e) {
                        return null;
                    }
                }
                return null;
            }
        };

        // Từ ngày
        VBox boxTuNgay = new VBox(4);
        Label lblTuNgay = new Label("Từ ngày");
        lblTuNgay.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #64748b;");
        dpTuNgay.setConverter(converterNgay);
        dpTuNgay.setPromptText("dd/MM/yyyy");
        dpTuNgay.setPrefWidth(140);
        boxTuNgay.getChildren().addAll(lblTuNgay, dpTuNgay);

        // Đến ngày
        VBox boxDenNgay = new VBox(4);
        Label lblDenNgay = new Label("Đến ngày");
        lblDenNgay.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #64748b;");
        dpDenNgay.setConverter(converterNgay);
        dpDenNgay.setPromptText("dd/MM/yyyy");
        dpDenNgay.setPrefWidth(140);
        boxDenNgay.getChildren().addAll(lblDenNgay, dpDenNgay);

        // Trạng thái thanh toán
        VBox boxTrangThai = new VBox(4);
        Label lblTrangThai = new Label("Trạng thái thanh toán");
        lblTrangThai.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #64748b;");
        cboTrangThaiThanhToan.getItems().addAll("Tất cả", "Chưa thanh toán", "Đã thanh toán", "Đã hủy");
        cboTrangThaiThanhToan.setValue("Tất cả");
        cboTrangThaiThanhToan.setPrefWidth(160);
        boxTrangThai.getChildren().addAll(lblTrangThai, cboTrangThaiThanhToan);

        // Nút bấm
        btnApDung.getStyleClass().add("nut-chinh");
        btnXoaBoLoc.getStyleClass().add("nut-phu");

        pnlBoLoc.getChildren().addAll(boxTuNgay, boxDenNgay, boxTrangThai, btnApDung, btnXoaBoLoc);

        Label lblGhiChuNgay = new Label("Để trống ngày để xem lịch sử không giới hạn khoảng ngày.");
        lblGhiChuNgay.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #64748b;");

        pnlKhung.getChildren().addAll(pnlBoLoc, lblGhiChuNgay);
        return pnlKhung;
    }

    /**
     * Tạo khu vực bảng hiển thị danh sách hóa đơn.
     */
    private VBox taoKhuVucBangHoaDon() {
        VBox pnlKhung = new VBox(8);
        VBox.setVgrow(pnlKhung, Priority.ALWAYS);

        // Thanh tiêu đề phía trên bảng
        HBox pnlThanhTren = new HBox();
        pnlThanhTren.setAlignment(Pos.CENTER_LEFT);

        VBox boxTieuDeBang = new VBox(2);
        Label lblTieuDeBang = new Label("Danh sách hóa đơn");
        lblTieuDeBang.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #1e293b;");
        Label lblSapXep = new Label("Ngày lập mới nhất trước");
        lblSapXep.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #64748b;");
        boxTieuDeBang.getChildren().addAll(lblTieuDeBang, lblSapXep);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnXemChiTiet.getStyleClass().add("nut-chinh");

        pnlThanhTren.getChildren().addAll(boxTieuDeBang, spacer, btnXemChiTiet);

        // Bảng TableView
        tblHoaDon.getStyleClass().add("bang-du-lieu");
        tblHoaDon.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblHoaDon.setPlaceholder(new Label("Không có hóa đơn giao dịch nào phù hợp với điều kiện tìm kiếm."));
        VBox.setVgrow(tblHoaDon, Priority.ALWAYS);

        TableColumn<LichSuGiaoDich, String> colMaHD = new TableColumn<>("Mã hóa đơn");
        colMaHD.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaHoaDon()));
        colMaHD.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-weight: bold;");
        colMaHD.setPrefWidth(160);

        TableColumn<LichSuGiaoDich, String> colNgayLap = new TableColumn<>("Ngày lập");
        colNgayLap.setCellValueFactory(cell -> {
            LocalDate nl = cell.getValue().getNgayLap();
            return new SimpleStringProperty(nl != null ? nl.format(DINH_DANG_NGAY) : "—");
        });
        colNgayLap.setStyle("-fx-alignment: CENTER;");
        colNgayLap.setPrefWidth(110);

        TableColumn<LichSuGiaoDich, String> colNhanVien = new TableColumn<>("Nhân viên lập");
        colNhanVien.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getTenNhanVien() != null ? cell.getValue().getTenNhanVien() : "—"));
        colNhanVien.setStyle("-fx-alignment: CENTER-LEFT;");
        colNhanVien.setPrefWidth(140);

        TableColumn<LichSuGiaoDich, String> colHTTT = new TableColumn<>("Hình thức thanh toán");
        colHTTT.setCellValueFactory(cell -> {
            String httt = cell.getValue().getHinhThucThanhToan();
            return new SimpleStringProperty(httt != null && !httt.isBlank() ? httt : "—");
        });
        colHTTT.setStyle("-fx-alignment: CENTER-LEFT;");
        colHTTT.setPrefWidth(140);

        TableColumn<LichSuGiaoDich, String> colTongTien = new TableColumn<>("Tổng tiền");
        colTongTien.setCellValueFactory(cell -> new SimpleStringProperty(DINH_DANG_TIEN.format(cell.getValue().getTongTien())));
        colTongTien.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");
        colTongTien.setPrefWidth(130);

        // Cột trạng thái thanh toán hiển thị Badge màu
        TableColumn<LichSuGiaoDich, String> colTrangThai = new TableColumn<>("Trạng thái thanh toán");
        colTrangThai.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTrangThaiThanhToan()));
        colTrangThai.setStyle("-fx-alignment: CENTER;");
        colTrangThai.setPrefWidth(140);
        colTrangThai.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    badge.setText(item);
                    badge.getStyleClass().clear();
                    if ("Đã thanh toán".equalsIgnoreCase(item)) {
                        badge.getStyleClass().add("nhan-trang-thai-hoat-dong");
                    } else if ("Chưa thanh toán".equalsIgnoreCase(item)) {
                        badge.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #d97706; -fx-padding: 3px 8px; -fx-background-radius: 4px; -fx-font-size: 11.5px; -fx-font-weight: 600;");
                    } else {
                        badge.getStyleClass().add("nhan-trang-thai-khoa");
                    }
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        tblHoaDon.getColumns().addAll(colMaHD, colNgayLap, colNhanVien, colHTTT, colTongTien, colTrangThai);
        tblHoaDon.setItems(danhSachHoaDon);

        pnlKhung.getChildren().addAll(pnlThanhTren, tblHoaDon);
        return pnlKhung;
    }

    /**
     * Tạo thanh tổng kết số lượng hóa đơn và tổng tiền đã thanh toán.
     */
    private HBox taoThanhTongKet() {
        HBox pnlTongKet = new HBox();
        pnlTongKet.getStyleClass().add("hop-noi-dung");
        pnlTongKet.setAlignment(Pos.CENTER_LEFT);

        lblKetQuaHienThi.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #64748b;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox boxTongTien = new VBox(2);
        boxTongTien.setAlignment(Pos.CENTER_RIGHT);
        lblTongTienThanhToanTieuDe.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #64748b;");
        lblTongTienThanhToan.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        boxTongTien.getChildren().addAll(lblTongTienThanhToanTieuDe, lblTongTienThanhToan);

        pnlTongKet.getChildren().addAll(lblKetQuaHienThi, spacer, boxTongTien);
        return pnlTongKet;
    }

    /**
     * Đăng ký các sự kiện tương tác trên giao diện.
     */
    private void dangKySuKien() {
        // Lắng nghe ký tự người dùng nhập vào ô tìm kiếm để gợi ý khách hàng thời gian thực
        txtTimKhachHang.textProperty().addListener((obs, oldVal, newVal) -> {
            if (dangChonTuGoiY) {
                dangChonTuGoiY = false;
                return;
            }
            if (newVal == null || newVal.trim().isEmpty()) {
                popupGoiY.hide();
                return;
            }
            String tuKhoa = newVal.trim();
            try {
                List<KhachHang> ketQua = khachHangController.timKiemKhachHang(tuKhoa, "Tất cả");
                if (ketQua.isEmpty()) {
                    popupGoiY.hide();
                } else {
                    lstGoiY.getItems().setAll(ketQua);
                    hienThiPopupGoiY();
                }
            } catch (Exception ignored) {
                popupGoiY.hide();
            }
        });

        // Bấm chuột vào ô tìm kiếm nếu đã có ký tự thì mở lại gợi ý
        txtTimKhachHang.setOnMouseClicked(e -> {
            String text = txtTimKhachHang.getText();
            if (text != null && !text.trim().isEmpty() && !popupGoiY.isShowing()) {
                try {
                    List<KhachHang> ketQua = khachHangController.timKiemKhachHang(text.trim(), "Tất cả");
                    if (!ketQua.isEmpty()) {
                        lstGoiY.getItems().setAll(ketQua);
                        hienThiPopupGoiY();
                    }
                } catch (Exception ignored) {}
            }
        });

        // Điều hướng phím: nhấn phím Mũi tên xuống để nhảy xuống danh sách gợi ý
        txtTimKhachHang.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DOWN && popupGoiY.isShowing()) {
                lstGoiY.requestFocus();
                lstGoiY.getSelectionModel().selectFirst();
                e.consume();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                popupGoiY.hide();
            }
        });

        // Xử lý chọn khách hàng từ danh sách gợi ý bằng chuột
        lstGoiY.setOnMouseClicked(e -> {
            KhachHang kh = lstGoiY.getSelectionModel().getSelectedItem();
            if (kh != null) {
                chonKhachHangTuGoiY(kh);
            }
        });

        // Xử lý chọn khách hàng từ danh sách gợi ý bằng phím Enter
        lstGoiY.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                KhachHang kh = lstGoiY.getSelectionModel().getSelectedItem();
                if (kh != null) {
                    chonKhachHangTuGoiY(kh);
                }
            } else if (e.getCode() == KeyCode.ESCAPE) {
                popupGoiY.hide();
                txtTimKhachHang.requestFocus();
            }
        });

        // Tự động ẩn popup gợi ý khi rời tiêu điểm khỏi ô nhập và bảng gợi ý
        txtTimKhachHang.focusedProperty().addListener((obs, oldV, isFocused) -> {
            if (!isFocused && !lstGoiY.isFocused()) {
                popupGoiY.hide();
            }
        });

        // Tìm kiếm khách hàng khi nhấn Enter hoặc bấm nút Tìm kiếm
        btnTimKhachHang.setOnAction(e -> thucHienTimKiemKhachHang());
        txtTimKhachHang.setOnAction(e -> thucHienTimKiemKhachHang());

        // Làm mới tìm kiếm khách hàng
        btnLamMoiKhachHang.setOnAction(e -> {
            popupGoiY.hide();
            txtTimKhachHang.clear();
            taiKhachHangMacDinh();
        });

        // Đổi khách hàng: focus vào ô tìm kiếm, chọn toàn bộ văn bản và mở gợi ý
        btnDoiKhachHang.setOnAction(e -> {
            txtTimKhachHang.requestFocus();
            txtTimKhachHang.selectAll();
            String text = txtTimKhachHang.getText();
            if (text != null && !text.trim().isEmpty()) {
                try {
                    List<KhachHang> ketQua = khachHangController.timKiemKhachHang(text.trim(), "Tất cả");
                    if (!ketQua.isEmpty()) {
                        lstGoiY.getItems().setAll(ketQua);
                        hienThiPopupGoiY();
                    }
                } catch (Exception ignored) {}
            }
        });

        // Áp dụng bộ lọc hóa đơn
        btnApDung.setOnAction(e -> taiDanhSachHoaDon());

        // Xóa bộ lọc hóa đơn
        btnXoaBoLoc.setOnAction(e -> {
            dpTuNgay.setValue(null);
            dpDenNgay.setValue(null);
            cboTrangThaiThanhToan.setValue("Tất cả");
            taiDanhSachHoaDon();
        });

        // Xem chi tiết hóa đơn
        btnXemChiTiet.setOnAction(e -> moChiTietGiaoDich());

        // Nhấp đúp chuột vào một dòng hóa đơn để mở hộp thoại chi tiết
        tblHoaDon.setRowFactory(tv -> {
            TableRow<LichSuGiaoDich> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    moChiTietGiaoDich();
                }
            });
            return row;
        });
    }

    /**
     * Hiển thị bảng danh sách gợi ý khách hàng ngay phía dưới ô nhập liệu tìm kiếm.
     */
    private void hienThiPopupGoiY() {
        if (lstGoiY.getItems().isEmpty()) {
            popupGoiY.hide();
            return;
        }

        if (txtTimKhachHang.getScene() == null || txtTimKhachHang.getScene().getWindow() == null) {
            return;
        }

        javafx.geometry.Point2D diem = txtTimKhachHang.localToScreen(0, txtTimKhachHang.getHeight());
        if (diem != null) {
            double chieuRong = txtTimKhachHang.getWidth();
            if (chieuRong <= 0) {
                chieuRong = 450;
            }
            lstGoiY.setPrefWidth(chieuRong);
            lstGoiY.setMaxWidth(chieuRong);

            int soDong = Math.min(lstGoiY.getItems().size(), 5);
            lstGoiY.setPrefHeight(soDong * 52.0 + 4);

            if (!popupGoiY.isShowing()) {
                popupGoiY.show(txtTimKhachHang.getScene().getWindow(), diem.getX(), diem.getY() + 3);
            } else {
                popupGoiY.setX(diem.getX());
                popupGoiY.setY(diem.getY() + 3);
            }
        }
    }

    /**
     * Xử lý khi người dùng chọn một khách hàng từ bảng gợi ý.
     */
    private void chonKhachHangTuGoiY(KhachHang kh) {
        dangChonTuGoiY = true;
        chonKhachHang(kh);
        txtTimKhachHang.setText(kh.getHoTen());
        popupGoiY.hide();
        txtTimKhachHang.requestFocus();
    }

    /**
     * Tìm khách hàng theo từ khóa người dùng nhập vào.
     */
    private void thucHienTimKiemKhachHang() {
        popupGoiY.hide();
        String tuKhoa = txtTimKhachHang.getText() != null ? txtTimKhachHang.getText().trim() : "";
        if (tuKhoa.isEmpty()) {
            taiKhachHangMacDinh();
            return;
        }

        try {
            List<KhachHang> ketQua = khachHangController.timKiemKhachHang(tuKhoa, "Tất cả");
            if (ketQua.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Không tìm thấy khách hàng nào với thông tin: \"" + tuKhoa + "\"", ButtonType.OK);
                alert.setTitle("Thông báo");
                alert.setHeaderText(null);
                alert.showAndWait();
            } else {
                // Chọn khách hàng đầu tiên trong kết quả tìm được
                chonKhachHang(ketQua.get(0));
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Lỗi tìm kiếm: " + e.getMessage(), ButtonType.OK);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    /**
     * Nạp khách hàng đầu tiên trong hệ thống để người dùng tiện theo dõi ngay khi mở màn hình.
     */
    private void taiKhachHangMacDinh() {
        try {
            List<KhachHang> danhSach = khachHangController.layDanhSachKhachHang();
            if (!danhSach.isEmpty()) {
                chonKhachHang(danhSach.get(0));
            } else {
                datRongThongTinKhachHang();
            }
        } catch (Exception e) {
            datRongThongTinKhachHang();
        }
    }

    private void datRongThongTinKhachHang() {
        khachHangHienTai = null;
        lblAvatar.setText("--");
        lblTenKhachHang.setText("Không có khách hàng trong hệ thống");
        lblMaKhachHang.setText("—");
        lblSoDienThoai.setText("—");
        lblEmail.setText("—");
        danhSachHoaDon.clear();
        capNhatThanhTongKet();
    }

    /**
     * Thiết lập khách hàng được chọn và nạp danh sách hóa đơn giao dịch tương ứng.
     */
    public void chonKhachHang(KhachHang kh) {
        if (kh == null) return;
        this.khachHangHienTai = kh;

        lblAvatar.setText(layKyTuVietTat(kh.getHoTen()));
        lblTenKhachHang.setText(kh.getHoTen());
        lblMaKhachHang.setText(kh.getMaKhachHang());
        lblSoDienThoai.setText(kh.getSoDienThoai() != null ? kh.getSoDienThoai() : "—");
        lblEmail.setText(kh.getEmail() != null && !kh.getEmail().isBlank() ? kh.getEmail() : "—");

        // Tải danh sách hóa đơn của khách hàng này
        taiDanhSachHoaDon();
    }

    /**
     * Trích xuất các chữ cái viết tắt đại diện cho tên khách hàng (VD: "Nguyễn Minh Thư" -> "MT").
     */
    private String layKyTuVietTat(String hoTen) {
        if (hoTen == null || hoTen.trim().isEmpty()) return "KH";
        String[] tu = hoTen.trim().split("\\s+");
        if (tu.length == 1) {
            return tu[0].substring(0, Math.min(2, tu[0].length())).toUpperCase();
        }
        String kyTuDau = tu[tu.length - 2].substring(0, 1).toUpperCase();
        String kyTuCuoi = tu[tu.length - 1].substring(0, 1).toUpperCase();
        return kyTuDau + kyTuCuoi;
    }

    /**
     * Tải danh sách hóa đơn từ cơ sở dữ liệu dựa trên khách hàng và các bộ lọc hiện tại.
     */
    private void taiDanhSachHoaDon() {
        if (khachHangHienTai == null) {
            danhSachHoaDon.clear();
            capNhatThanhTongKet();
            return;
        }

        LocalDate tuNgay = dpTuNgay.getValue();
        LocalDate denNgay = dpDenNgay.getValue();
        String trangThai = cboTrangThaiThanhToan.getValue();

        try {
            List<LichSuGiaoDich> ds = khachHangController.layLichSuGiaoDich(
                    khachHangHienTai.getMaKhachHang(),
                    tuNgay,
                    denNgay,
                    trangThai
            );
            danhSachHoaDon.setAll(ds);
        } catch (Exception e) {
            danhSachHoaDon.clear();
        }
        capNhatThanhTongKet();
    }

    /**
     * Cập nhật các chỉ số tổng hợp dưới chân bảng.
     */
    private void capNhatThanhTongKet() {
        int soHoaDon = danhSachHoaDon.size();
        lblKetQuaHienThi.setText("Trong kết quả đang hiển thị:\n" + soHoaDon + " hóa đơn");

        // Tính tổng tiền các hóa đơn có trạng thái "Đã thanh toán"
        int soHoaDonDaThanhToan = 0;
        BigDecimal tongTienDaTT = BigDecimal.ZERO;

        for (LichSuGiaoDich hd : danhSachHoaDon) {
            if ("Đã thanh toán".equalsIgnoreCase(hd.getTrangThaiThanhToan())) {
                soHoaDonDaThanhToan++;
                tongTienDaTT = tongTienDaTT.add(hd.getTongTien());
            }
        }

        lblTongTienThanhToanTieuDe.setText("Tổng tiền đã thanh toán • " + soHoaDonDaThanhToan + " hóa đơn");
        lblTongTienThanhToan.setText(DINH_DANG_TIEN.format(tongTienDaTT));
    }

    /**
     * Mở hộp thoại Chi tiết giao dịch cho hóa đơn đang được chọn.
     */
    private void moChiTietGiaoDich() {
        LichSuGiaoDich hd = tblHoaDon.getSelectionModel().getSelectedItem();
        if (hd == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Vui lòng chọn một hóa đơn từ bảng để xem chi tiết!", ButtonType.OK);
            alert.setTitle("Thông báo");
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }

        ChiTietGiaoDichView.hienThi(hd, getScene() != null ? getScene().getWindow() : null);
    }
}
