package com.petshop.view.donnhap;

import com.petshop.controller.DonNhapHangController;
import com.petshop.model.DonNhapHang;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Màn hình tìm kiếm đơn nhập hàng (IMP_SEARCH).
 * Cho phép tìm theo từ khóa (mã đơn hàng, tên nhà cung cấp),
 * khoảng ngày, trạng thái. Hỗ trợ xem chi tiết đơn nhập.
 * Tuân thủ phong cách giao diện TimKiemNhaCungCapView.
 */
public class TimKiemDonNhapView extends VBox {

    // Định dạng số tiền và ngày tháng
    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final DonNhapHangController controller = new DonNhapHangController();
    private final ObservableList<DonNhapHang> danhSachHienThi = FXCollections.observableArrayList();

    // Các thành phần bộ lọc tìm kiếm
    private final TextField txtTuKhoa = new TextField();
    private final DatePicker dpTuNgay = new DatePicker();
    private final DatePicker dpDenNgay = new DatePicker();
    private final ComboBox<String> cboTrangThai = new ComboBox<>();

    // Bảng kết quả và nhãn số lượng
    private final TableView<DonNhapHang> tblDanhSach = new TableView<>();
    private final Button btnTim = new Button("Tìm kiếm");
    private final Button btnXemChiTiet = new Button("Xem chi tiết");
    private final Label lblKetQua = new Label("Tổng số: 0 đơn nhập hàng");

    public TimKiemDonNhapView() {
        setSpacing(14);
        getStyleClass().add("khung-phan-he");

        // Tiêu đề
        Label lblTieuDe = new Label("TÌM KIẾM ĐƠN NHẬP HÀNG");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        // Mô tả
        Label lblMoTa = new Label("Tìm kiếm đơn nhập hàng theo mã đơn hàng, tên nhà cung cấp, khoảng ngày và trạng thái.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");

        getChildren().addAll(lblTieuDe, lblMoTa, taoKhungChinh());
        tblDanhSach.setItems(danhSachHienThi);
        capNhatTrangThaiNut(null);
        taiLaiBang();
    }

    /**
     * Tạo khung nội dung chính gồm bộ lọc, bảng dữ liệu và nhãn kết quả.
     */
    private VBox taoKhungChinh() {
        VBox khung = new VBox(12);
        khung.getStyleClass().add("hop-noi-dung");
        VBox.setVgrow(khung, Priority.ALWAYS);

        // === Hàng 1: Từ khóa + Trạng thái ===
        HBox congCu1 = new HBox(10);
        congCu1.setAlignment(Pos.CENTER_LEFT);

        txtTuKhoa.setPromptText("Nhập mã đơn hàng hoặc tên nhà cung cấp...");
        txtTuKhoa.setPrefWidth(300);
        txtTuKhoa.setOnAction(e -> xuLyTimKiem());

        // Danh sách trạng thái
        cboTrangThai.setItems(FXCollections.observableArrayList(
                "Tất cả", "Chờ nhận hàng", "Hoàn tất", "Đã hủy"
        ));
        cboTrangThai.setValue("Tất cả");
        cboTrangThai.setPrefWidth(160);

        congCu1.getChildren().addAll(
                new Label("Từ khóa:"), txtTuKhoa,
                new Label("Trạng thái:"), cboTrangThai
        );

        // === Hàng 2: Khoảng ngày + Nút tìm kiếm + Nút làm mới ===
        HBox congCu2 = new HBox(10);
        congCu2.setAlignment(Pos.CENTER_LEFT);

        dpTuNgay.setPromptText("Từ ngày");
        dpTuNgay.setPrefWidth(150);
        dpDenNgay.setPromptText("Đến ngày");
        dpDenNgay.setPrefWidth(150);

        btnTim.getStyleClass().add("nut-chinh");
        btnTim.setOnAction(e -> xuLyTimKiem());

        Button btnLamMoi = new Button("Làm mới");
        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> {
            txtTuKhoa.clear();
            dpTuNgay.setValue(null);
            dpDenNgay.setValue(null);
            cboTrangThai.setValue("Tất cả");
            taiLaiBang();
        });

        // Khoảng trống đẩy nút "Xem chi tiết" sang phải
        Region khoangTrong = new Region();
        HBox.setHgrow(khoangTrong, Priority.ALWAYS);

        btnXemChiTiet.getStyleClass().add("nut-phu");
        btnXemChiTiet.setOnAction(e -> moChiTietDonNhap());

        congCu2.getChildren().addAll(
                new Label("Từ ngày:"), dpTuNgay,
                new Label("Đến ngày:"), dpDenNgay,
                btnTim, btnLamMoi,
                khoangTrong, btnXemChiTiet
        );

        // === Bảng dữ liệu ===
        taoCotBang();
        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Không tìm thấy đơn nhập hàng phù hợp"));
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);

        // Lắng nghe chọn dòng để cập nhật nút
        tblDanhSach.getSelectionModel().selectedItemProperty().addListener(
                (obs, cu, moi) -> capNhatTrangThaiNut(moi)
        );

        // Nhấn đúp mở chi tiết
        tblDanhSach.setRowFactory(tv -> {
            TableRow<DonNhapHang> dong = new TableRow<>();
            dong.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !dong.isEmpty()) {
                    moChiTietDonNhap();
                }
            });
            return dong;
        });

        khung.getChildren().addAll(congCu1, congCu2, tblDanhSach, lblKetQua);
        return khung;
    }

    /**
     * Tạo các cột cho bảng đơn nhập hàng.
     */
    @SuppressWarnings("unchecked")
    private void taoCotBang() {
        // Cột mã đơn hàng
        TableColumn<DonNhapHang, String> colMaDon = new TableColumn<>("Mã đơn hàng");
        colMaDon.setPrefWidth(160);
        colMaDon.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaDonHang()));

        // Cột tên nhà cung cấp
        TableColumn<DonNhapHang, String> colNCC = new TableColumn<>("Nhà cung cấp");
        colNCC.setPrefWidth(200);
        colNCC.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTenNhaCungCap() != null ? d.getValue().getTenNhaCungCap() : ""
        ));

        // Cột tên nhân viên
        TableColumn<DonNhapHang, String> colNhanVien = new TableColumn<>("Nhân viên");
        colNhanVien.setPrefWidth(160);
        colNhanVien.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTenNhanVien() != null ? d.getValue().getTenNhanVien() : ""
        ));

        // Cột ngày lập (dd/MM/yyyy)
        TableColumn<DonNhapHang, String> colNgayLap = new TableColumn<>("Ngày lập");
        colNgayLap.setPrefWidth(110);

        TableColumn<DonNhapHang, String> colGioLap = new TableColumn<>("Thời gian");
        colGioLap.setPrefWidth(100);
        colGioLap.setCellValueFactory(d -> {
            LocalTime gio = d.getValue().getGioLap();
            return new SimpleStringProperty(gio != null ? gio.format(DINH_DANG_GIO) : "");
        });
        colNgayLap.setCellValueFactory(d -> {
            LocalDate ngay = d.getValue().getNgayLap();
            return new SimpleStringProperty(ngay != null ? ngay.format(DINH_DANG_NGAY) : "");
        });

        // Cột tổng tiền (định dạng #,##0)
        TableColumn<DonNhapHang, String> colTongTien = new TableColumn<>("Tổng tiền");
        colTongTien.setPrefWidth(130);
        colTongTien.setStyle("-fx-alignment: CENTER-RIGHT;");
        colTongTien.setCellValueFactory(d -> {
            BigDecimal tien = d.getValue().getTongTien();
            return new SimpleStringProperty(tien != null ? DINH_DANG_TIEN.format(tien) : "0");
        });

        // Cột trạng thái (có tô màu)
        TableColumn<DonNhapHang, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setPrefWidth(130);
        colTrangThai.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTrangThai() != null ? d.getValue().getTrangThai() : ""
        ));
        colTrangThai.setCellFactory(col -> new TableCell<DonNhapHang, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    // Tô màu theo trạng thái
                    switch (item) {
                        case "Chờ nhận hàng":
                            setStyle("-fx-text-fill: #D2691E; -fx-font-weight: bold;"); // Nâu cam
                            break;
                        case "Hoàn tất":
                            setStyle("-fx-text-fill: #228B22; -fx-font-weight: bold;"); // Xanh lá
                            break;
                        case "Đã hủy":
                            setStyle("-fx-text-fill: #DC143C; -fx-font-weight: bold;"); // Đỏ
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });

        tblDanhSach.getColumns().setAll(List.of(
                colMaDon, colNCC, colNhanVien, colNgayLap, colGioLap, colTongTien, colTrangThai
        ));
    }

    /**
     * Cập nhật trạng thái nút "Xem chi tiết" theo dòng được chọn.
     */
    private void capNhatTrangThaiNut(DonNhapHang donHang) {
        btnXemChiTiet.setDisable(donHang == null);
    }

    /**
     * Xử lý tìm kiếm đơn nhập hàng theo bộ lọc.
     */
    private void xuLyTimKiem() {
        try {
            String tuKhoa = txtTuKhoa.getText();
            LocalDate tuNgay = dpTuNgay.getValue();
            LocalDate denNgay = dpDenNgay.getValue();
            String trangThai = cboTrangThai.getValue();

            // Nếu chọn "Tất cả" thì truyền null để không lọc trạng thái
            if ("Tất cả".equals(trangThai)) {
                trangThai = null;
            }

            // Gọi controller tìm kiếm (maNhaCungCap truyền null vì từ khóa đã bao gồm tên NCC)
            List<DonNhapHang> ketQua = controller.timKiemDonHang(tuKhoa, null, tuNgay, denNgay, trangThai);
            danhSachHienThi.setAll(ketQua);
            tblDanhSach.getSelectionModel().clearSelection();
            tblDanhSach.refresh();
            lblKetQua.setText("Tổng số: " + ketQua.size() + " đơn nhập hàng");
            capNhatTrangThaiNut(null);
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienThiLoi(ex.getMessage());
        }
    }

    /**
     * Tải lại toàn bộ danh sách đơn nhập hàng.
     */
    private void taiLaiBang() {
        try {
            List<DonNhapHang> danhSach = controller.timKiemDonHang(null, null, null, null, null);
            danhSachHienThi.setAll(danhSach);
            tblDanhSach.getSelectionModel().clearSelection();
            tblDanhSach.refresh();
            lblKetQua.setText("Tổng số: " + danhSach.size() + " đơn nhập hàng");
            capNhatTrangThaiNut(null);
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienThiLoi(ex.getMessage());
        }
    }

    /**
     * Mở dialog xem chi tiết đơn nhập hàng đang được chọn.
     */
    private void moChiTietDonNhap() {
        DonNhapHang chon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (chon == null) {
            hienThiLoi("Vui lòng chọn đơn nhập hàng cần xem chi tiết!");
            return;
        }

        // Mở dialog chi tiết
        XemChiTietDonNhapView.moDialog(getScene().getWindow(), chon);
    }

    /**
     * Hiển thị thông báo lỗi.
     */
    private void hienThiLoi(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Thông báo lỗi");
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}
