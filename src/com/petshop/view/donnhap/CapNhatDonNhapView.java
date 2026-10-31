package com.petshop.view.donnhap;

import com.petshop.controller.DonHangController;
import com.petshop.model.DonHang;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Giao diện cập nhật đơn nhập hàng.
 * Cho phép xác nhận nhận hàng, hủy đơn, xem chi tiết đơn nhập.
 */
public class CapNhatDonNhapView extends VBox {

    // === Các thành phần giao diện ===
    private TableView<DonHang> bangDonHang;
    private Button nutXacNhan;
    private Button nutHuyDon;
    private Button nutXemChiTiet;
    private Button nutLamMoi;
    private Label nhanTrangThai;

    // === Controller ===
    private final DonHangController controller;

    // === Định dạng ===
    private final DecimalFormat dinhDangTien = new DecimalFormat("#,##0");
    private final DateTimeFormatter dinhDangNgay = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Khởi tạo giao diện cập nhật đơn nhập hàng.
     */
    public CapNhatDonNhapView() {
        controller = new DonHangController();
        khoiTaoGiaoDien();
        taiDuLieu();
    }

    /**
     * Khởi tạo toàn bộ giao diện.
     */
    private void khoiTaoGiaoDien() {
        getStyleClass().add("khung-phan-he");
        setSpacing(15);
        setPadding(new Insets(20));

        // --- Phần tiêu đề ---
        VBox hopTieuDe = taoPhanTieuDe();

        // --- Phần nút chức năng ---
        HBox hopNut = taoPhanNut();

        // --- Phần bảng dữ liệu ---
        bangDonHang = taoBangDonHang();
        VBox.setVgrow(bangDonHang, Priority.ALWAYS);

        // --- Phần trạng thái ---
        nhanTrangThai = new Label();
        nhanTrangThai.getStyleClass().add("mo-ta-phan-he");

        getChildren().addAll(hopTieuDe, hopNut, bangDonHang, nhanTrangThai);

        // Lắng nghe sự kiện chọn dòng trên bảng để bật/tắt nút
        bangDonHang.getSelectionModel().selectedItemProperty().addListener(
                (obs, dongCu, dongMoi) -> capNhatTrangThaiNut(dongMoi));

        // Mặc định tắt các nút khi chưa chọn dòng
        capNhatTrangThaiNut(null);
    }

    /**
     * Tạo phần tiêu đề của giao diện.
     */
    private VBox taoPhanTieuDe() {
        VBox hop = new VBox(5);

        Label tieuDe = new Label("CẬP NHẬT ĐƠN NHẬP HÀNG");
        tieuDe.getStyleClass().add("tieu-de-phan-he");

        Label moTa = new Label("Xác nhận nhận hàng hoặc hủy các đơn nhập hàng đang chờ xử lý.");
        moTa.getStyleClass().add("mo-ta-phan-he");

        hop.getChildren().addAll(tieuDe, moTa);
        return hop;
    }

    /**
     * Tạo phần nút chức năng.
     */
    private HBox taoPhanNut() {
        HBox hop = new HBox(10);
        hop.setAlignment(Pos.CENTER_LEFT);

        // Nút xác nhận nhận hàng
        nutXacNhan = new Button("Xác nhận nhận hàng");
        nutXacNhan.getStyleClass().add("nut-chinh");
        nutXacNhan.setOnAction(e -> xuLyXacNhanNhanHang());

        // Nút hủy đơn (màu đỏ)
        nutHuyDon = new Button("Hủy đơn");
        nutHuyDon.getStyleClass().add("nut-phu");
        nutHuyDon.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        nutHuyDon.setOnAction(e -> xuLyHuyDon());

        // Nút xem chi tiết
        nutXemChiTiet = new Button("Xem chi tiết");
        nutXemChiTiet.getStyleClass().add("nut-phu");
        nutXemChiTiet.setOnAction(e -> xuLyXemChiTiet());

        // Nút làm mới
        nutLamMoi = new Button("Làm mới");
        nutLamMoi.getStyleClass().add("nut-phu");
        nutLamMoi.setOnAction(e -> taiDuLieu());

        hop.getChildren().addAll(nutXacNhan, nutHuyDon, nutXemChiTiet, nutLamMoi);
        return hop;
    }

    /**
     * Tạo bảng hiển thị danh sách đơn hàng.
     */
    @SuppressWarnings("unchecked")
    private TableView<DonHang> taoBangDonHang() {
        TableView<DonHang> bang = new TableView<>();
        bang.getStyleClass().add("bang-du-lieu");
        bang.setPlaceholder(new Label("Không có đơn nhập hàng nào."));

        // Cột mã đơn hàng
        TableColumn<DonHang, String> cotMa = new TableColumn<>("Mã đơn hàng");
        cotMa.setCellValueFactory(new PropertyValueFactory<>("maDonHang"));
        cotMa.setPrefWidth(120);

        // Cột tên nhà cung cấp
        TableColumn<DonHang, String> cotNCC = new TableColumn<>("Nhà cung cấp");
        cotNCC.setCellValueFactory(new PropertyValueFactory<>("tenNhaCungCap"));
        cotNCC.setPrefWidth(180);

        // Cột tên nhân viên
        TableColumn<DonHang, String> cotNV = new TableColumn<>("Nhân viên");
        cotNV.setCellValueFactory(new PropertyValueFactory<>("tenNhanVien"));
        cotNV.setPrefWidth(150);

        // Cột ngày lập (định dạng dd/MM/yyyy)
        TableColumn<DonHang, String> cotNgayLap = new TableColumn<>("Ngày lập");
        cotNgayLap.setCellValueFactory(cellData -> {
            LocalDate ngay = cellData.getValue().getNgayLap();
            String ngayStr = (ngay != null) ? ngay.format(dinhDangNgay) : "";
            return new SimpleStringProperty(ngayStr);
        });
        cotNgayLap.setPrefWidth(120);

        // Cột tổng tiền (định dạng #,##0)
        TableColumn<DonHang, String> cotTongTien = new TableColumn<>("Tổng tiền");
        cotTongTien.setCellValueFactory(cellData -> {
            BigDecimal tongTien = cellData.getValue().getTongTien();
            String tienStr = (tongTien != null) ? dinhDangTien.format(tongTien) : "0";
            return new SimpleStringProperty(tienStr);
        });
        cotTongTien.setPrefWidth(130);
        cotTongTien.setStyle("-fx-alignment: CENTER-RIGHT;");

        // Cột trạng thái (tô màu theo trạng thái)
        TableColumn<DonHang, String> cotTrangThai = new TableColumn<>("Trạng thái");
        cotTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        cotTrangThai.setPrefWidth(140);
        cotTrangThai.setCellFactory(col -> new TableCell<DonHang, String>() {
            @Override
            protected void updateItem(String trangThai, boolean empty) {
                super.updateItem(trangThai, empty);
                if (empty || trangThai == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(trangThai);
                    setStyle("-fx-font-weight: bold;");
                    if (DonHang.TRANG_THAI_CHO_NHAN.equals(trangThai)) {
                        setTextFill(Color.ORANGE);
                    } else if (DonHang.TRANG_THAI_HOAN_TAT.equals(trangThai)) {
                        setTextFill(Color.GREEN);
                    } else if (DonHang.TRANG_THAI_DA_HUY.equals(trangThai)) {
                        setTextFill(Color.RED);
                    } else {
                        setTextFill(Color.BLACK);
                    }
                }
            }
        });

        bang.getColumns().addAll(cotMa, cotNCC, cotNV, cotNgayLap, cotTongTien, cotTrangThai);
        bang.setRowFactory(tv -> {
            TableRow<DonHang> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    bang.getSelectionModel().select(row.getIndex());
                    xuLyXemChiTiet();
                }
            });
            return row;
        });
        return bang;
    }

    /**
     * Cập nhật trạng thái bật/tắt các nút dựa trên dòng được chọn.
     */
    private void capNhatTrangThaiNut(DonHang donHang) {
        if (donHang == null) {
            // Không có dòng nào được chọn → tắt hết
            nutXacNhan.setDisable(true);
            nutHuyDon.setDisable(true);
            nutXemChiTiet.setDisable(true);
        } else {
            boolean laChoNhanHang = DonHang.TRANG_THAI_CHO_NHAN.equals(donHang.getTrangThai());
            // Chỉ bật xác nhận và hủy khi trạng thái là "Chờ nhận hàng"
            nutXacNhan.setDisable(!laChoNhanHang);
            nutHuyDon.setDisable(!laChoNhanHang);
            // Luôn bật xem chi tiết khi có chọn dòng
            nutXemChiTiet.setDisable(false);
        }
    }

    /**
     * Tải dữ liệu đơn hàng từ controller và hiển thị lên bảng.
     */
    private void taiDuLieu() {
        try {
            List<DonHang> danhSach = controller.layTatCaDonHang();
            ObservableList<DonHang> danhSachObservable = FXCollections.observableArrayList(danhSach);
            bangDonHang.setItems(danhSachObservable);

            // Cập nhật nhãn trạng thái
            long soChoNhan = danhSach.stream()
                    .filter(dh -> DonHang.TRANG_THAI_CHO_NHAN.equals(dh.getTrangThai()))
                    .count();
            nhanTrangThai.setText("Tổng số đơn: " + danhSach.size()
                    + " | Đang chờ nhận hàng: " + soChoNhan);

            // Xóa lựa chọn cũ
            bangDonHang.getSelectionModel().clearSelection();
        } catch (Exception ex) {
            hienThongBaoLoi("Lỗi tải dữ liệu", "Không thể tải danh sách đơn nhập hàng.\n" + ex.getMessage());
        }
    }

    /**
     * Xử lý xác nhận nhận hàng cho đơn được chọn.
     */
    private void xuLyXacNhanNhanHang() {
        DonHang donHang = bangDonHang.getSelectionModel().getSelectedItem();
        if (donHang == null) return;

        // Hiển thị hộp thoại xác nhận
        Alert xacNhan = new Alert(Alert.AlertType.CONFIRMATION);
        xacNhan.setTitle("Xác nhận nhận hàng");
        xacNhan.setHeaderText("Xác nhận nhận hàng cho đơn: " + donHang.getMaDonHang());
        xacNhan.setContentText("Bạn có chắc chắn muốn xác nhận đã nhận hàng cho đơn này?");

        Optional<ButtonType> ketQua = xacNhan.showAndWait();
        if (ketQua.isPresent() && ketQua.get() == ButtonType.OK) {
            try {
                boolean daXacNhan = controller.xacNhanNhanHang(donHang.getMaDonHang());
                if (daXacNhan) {
                    hienThongBaoThanhCong("Thành công", "Đã xác nhận nhận hàng cho đơn: " + donHang.getMaDonHang());
                } else {
                    hienThongBaoLoi("Không thể xác nhận nhận hàng",
                            "Đơn hàng đã được xử lý hoặc có sản phẩm không còn tồn tại. Vui lòng tải lại danh sách.");
                }
                taiDuLieu(); // Tải lại bảng
            } catch (Exception ex) {
                hienThongBaoLoi("Lỗi xác nhận nhận hàng",
                        "Không thể xác nhận nhận hàng.\n" + ex.getMessage());
            }
        }
    }

    /**
     * Xử lý hủy đơn nhập hàng được chọn.
     */
    private void xuLyHuyDon() {
        DonHang donHang = bangDonHang.getSelectionModel().getSelectedItem();
        if (donHang == null) return;

        // Hiển thị hộp thoại xác nhận hủy
        Alert xacNhan = new Alert(Alert.AlertType.CONFIRMATION);
        xacNhan.setTitle("Xác nhận hủy đơn");
        xacNhan.setHeaderText("Hủy đơn nhập hàng: " + donHang.getMaDonHang());
        xacNhan.setContentText("Bạn có chắc chắn muốn hủy đơn nhập hàng này? Thao tác này không thể hoàn tác.");

        Optional<ButtonType> ketQua = xacNhan.showAndWait();
        if (ketQua.isPresent() && ketQua.get() == ButtonType.OK) {
            try {
                boolean daHuy = controller.huyDonNhapHang(donHang.getMaDonHang());
                if (daHuy) {
                    hienThongBaoThanhCong("Thành công", "Đã hủy đơn nhập hàng: " + donHang.getMaDonHang());
                } else {
                    hienThongBaoLoi("Không thể hủy đơn",
                            "Trạng thái đơn hàng đã thay đổi. Vui lòng tải lại danh sách.");
                }
                taiDuLieu(); // Tải lại bảng
            } catch (Exception ex) {
                hienThongBaoLoi("Lỗi hủy đơn",
                        "Không thể hủy đơn nhập hàng.\n" + ex.getMessage());
            }
        }
    }

    /**
     * Xử lý xem chi tiết đơn nhập hàng được chọn.
     */
    private void xuLyXemChiTiet() {
        DonHang donHang = bangDonHang.getSelectionModel().getSelectedItem();
        if (donHang == null) return;

        // Mở dialog xem chi tiết đơn nhập
        XemChiTietDonNhapView.moDialog(getScene().getWindow(), donHang);
    }

    /**
     * Hiển thị thông báo thành công.
     */
    private void hienThongBaoThanhCong(String tieuDe, String noiDung) {
        Alert thongBao = new Alert(Alert.AlertType.INFORMATION);
        thongBao.setTitle(tieuDe);
        thongBao.setHeaderText(null);
        thongBao.setContentText(noiDung);
        thongBao.showAndWait();
    }

    /**
     * Hiển thị thông báo lỗi.
     */
    private void hienThongBaoLoi(String tieuDe, String noiDung) {
        Alert thongBao = new Alert(Alert.AlertType.ERROR);
        thongBao.setTitle(tieuDe);
        thongBao.setHeaderText(null);
        thongBao.setContentText(noiDung);
        thongBao.showAndWait();
    }
}
