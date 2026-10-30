package com.petshop.view.nhanvien;

import com.petshop.controller.NhanVienController;
import com.petshop.model.NhanVien;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Màn hình Tìm kiếm & Tra cứu nhân viên (NV_TIM_KIEM).
 * Thiết kế và luồng hoạt động đồng bộ hoàn toàn với TimKiemKhachHangView:
 * 1. Tra cứu linh hoạt theo từ khóa: Mã nhân viên, Họ tên, Số điện thoại.
 * 2. Lọc tự động theo thời gian thực (real-time) ngay khi người dùng gõ từ khóa.
 * 3. Hỗ trợ xem chi tiết hồ sơ nhân sự (XemChiTietNhanVienView):
 *    - Bấm nút "Xem chi tiết" trên thanh tác vụ khi chọn dòng.
 *    - Nhấp đúp chuột vào bất kỳ dòng nhân viên nào để mở popup xem chi tiết.
 */
public class TimKiemNhanVienView extends VBox {

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final NhanVienController controller = new NhanVienController();
    private final ObservableList<NhanVien> danhSachHienThi = FXCollections.observableArrayList();

    // Các thành phần tìm kiếm và lọc
    private final TextField txtTuKhoa = new TextField();
    private final Button btnTimKiem = new Button("Tìm kiếm");
    private final Button btnLamMoi = new Button("Làm mới");
    private final Button btnXemChiTiet = new Button("Xem chi tiết");
    private final Label lblTongSo = new Label("Tổng số: 0 nhân viên");

    // Bảng dữ liệu hiển thị kết quả
    private final TableView<NhanVien> tblDanhSach = new TableView<>();

    public TimKiemNhanVienView() {
        setSpacing(14);
        setPadding(new Insets(20, 24, 20, 24));
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề màn hình
        VBox pnlTieuDeManHinh = new VBox(4);
        Label lblTieuDe = new Label("TÌM KIẾM & TRA CỨU NHÂN VIÊN");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        Label lblMoTa = new Label("Tra cứu nhanh hồ sơ nhân viên theo mã định danh, họ tên hoặc số điện thoại.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDeManHinh.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Khung nội dung chính gồm thanh công cụ và bảng kết quả
        VBox pnlKhungChinh = new VBox(12);
        pnlKhungChinh.getStyleClass().add("hop-noi-dung");
        VBox.setVgrow(pnlKhungChinh, Priority.ALWAYS);

        HBox pnlCongCu = taoThanhTimKiem();
        VBox pnlBang = taoKhungBangDanhSach();
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        pnlKhungChinh.getChildren().addAll(pnlCongCu, pnlBang);
        getChildren().addAll(pnlTieuDeManHinh, pnlKhungChinh);

        // Vô hiệu hóa nút xem chi tiết khi chưa chọn dòng
        btnXemChiTiet.setDisable(true);

        // Nạp dữ liệu ban đầu
        xuLyTimKiem();
    }

    /**
     * Khởi tạo thanh công cụ tìm kiếm:
     * - Ô nhập từ khóa (mã, tên, SĐT)
     * - Nút Tìm kiếm, Làm mới và Nút Xem chi tiết (khi chọn dòng)
     */
    private HBox taoThanhTimKiem() {
        HBox pnlCongCu = new HBox(10);
        pnlCongCu.getStyleClass().add("thanh-tac-vu");
        pnlCongCu.setAlignment(Pos.CENTER_LEFT);
        pnlCongCu.setMaxWidth(Double.MAX_VALUE);

        // Ô nhập từ khóa tra cứu
        Label lblTuKhoa = new Label("Từ khóa:");
        txtTuKhoa.setPromptText("Nhập mã, tên hoặc số điện thoại nhân viên...");
        txtTuKhoa.setPrefWidth(350);
        txtTuKhoa.setOnAction(e -> xuLyTimKiem());
        // Lọc tự động thời gian thực khi người dùng gõ từ khóa
        txtTuKhoa.textProperty().addListener((obs, oldVal, newVal) -> xuLyTimKiem());

        // Nút Tìm kiếm
        btnTimKiem.getStyleClass().add("nut-chinh");
        btnTimKiem.setOnAction(e -> xuLyTimKiem());

        // Nút Làm mới
        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> lamMoiTimKiem());

        // Khoảng trống đẩy nút xem chi tiết và nhãn tổng số sang bên phải
        Region pnlKhoangTrong = new Region();
        HBox.setHgrow(pnlKhoangTrong, Priority.ALWAYS);

        // Nút xem chi tiết khi chọn một nhân viên trên bảng
        btnXemChiTiet.getStyleClass().add("nut-phu");
        btnXemChiTiet.setOnAction(e -> moXemChiTietNhanVien());

        lblTongSo.setStyle("-fx-font-weight: 600; -fx-text-fill: #475569; -fx-font-size: 13px;");

        pnlCongCu.getChildren().addAll(
                lblTuKhoa, txtTuKhoa,
                btnTimKiem,
                btnLamMoi,
                pnlKhoangTrong,
                btnXemChiTiet, lblTongSo
        );

        return pnlCongCu;
    }

    /**
     * Khởi tạo bảng dữ liệu TableView hiển thị kết quả tìm kiếm nhân viên.
     */
    private VBox taoKhungBangDanhSach() {
        VBox pnlKhungBang = new VBox(8);

        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Không tìm thấy nhân viên nào phù hợp với điều kiện tra cứu"));
        tblDanhSach.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);

        // --- Cột 1: Mã nhân viên ---
        TableColumn<NhanVien, String> colMa = new TableColumn<>("Mã nhân viên");
        colMa.setPrefWidth(160);
        colMa.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMaNhanVien()));
        colMa.setStyle("-fx-alignment: CENTER;");

        // --- Cột 2: Họ và tên ---
        TableColumn<NhanVien, String> colHoTen = new TableColumn<>("Họ và tên");
        colHoTen.setPrefWidth(200);
        colHoTen.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHoTen()));
        colHoTen.setStyle("-fx-alignment: CENTER-LEFT;");

        // --- Cột 3: Số điện thoại ---
        TableColumn<NhanVien, String> colSdt = new TableColumn<>("Số điện thoại");
        colSdt.setPrefWidth(130);
        colSdt.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSoDienThoai()));
        colSdt.setStyle("-fx-alignment: CENTER;");

        // --- Cột 4: Giới tính ---
        TableColumn<NhanVien, String> colGioiTinh = new TableColumn<>("Giới tính");
        colGioiTinh.setPrefWidth(90);
        colGioiTinh.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTenGioiTinh()));
        colGioiTinh.setStyle("-fx-alignment: CENTER;");

        // --- Cột 5: Ngày sinh ---
        TableColumn<NhanVien, String> colNgaySinh = new TableColumn<>("Ngày sinh");
        colNgaySinh.setPrefWidth(110);
        colNgaySinh.setCellValueFactory(cellData -> {
            LocalDate ns = cellData.getValue().getNgaySinh();
            return new SimpleStringProperty(ns != null ? ns.format(DINH_DANG_NGAY) : "");
        });
        colNgaySinh.setStyle("-fx-alignment: CENTER;");

        // --- Cột 6: Tuổi ---
        TableColumn<NhanVien, Number> colTuoi = new TableColumn<>("Tuổi");
        colTuoi.setPrefWidth(70);
        colTuoi.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getTuoi()));
        colTuoi.setStyle("-fx-alignment: CENTER;");

        // --- Cột 7: Chức vụ ---
        TableColumn<NhanVien, String> colChucVu = new TableColumn<>("Chức vụ");
        colChucVu.setPrefWidth(120);
        colChucVu.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getChucVu()));
        colChucVu.setStyle("-fx-alignment: CENTER;");

        // --- Cột 8: Trạng thái ---
        TableColumn<NhanVien, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setPrefWidth(130);
        colTrangThai.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTrangThai()));
        colTrangThai.setStyle("-fx-alignment: CENTER;");

        tblDanhSach.getColumns().addAll(colMa, colHoTen, colSdt, colGioiTinh, colNgaySinh, colTuoi, colChucVu, colTrangThai);
        tblDanhSach.setItems(danhSachHienThi);

        // Hiệu ứng làm mờ dòng nhân viên đã nghỉ việc + Double click mở xem chi tiết
        tblDanhSach.setRowFactory(tv -> {
            TableRow<NhanVien> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldVal, nv) -> {
                if (nv != null && !nv.isDangLam()) {
                    row.setOpacity(0.55);
                } else {
                    row.setOpacity(1.0);
                }
            });

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    moXemChiTietNhanVien();
                }
            });
            return row;
        });

        // Lắng nghe sự kiện chọn dòng trên bảng để kích hoạt / vô hiệu hóa nút Xem chi tiết
        tblDanhSach.getSelectionModel().selectedItemProperty().addListener((obs, cu, moi) -> {
            btnXemChiTiet.setDisable(moi == null);
        });

        pnlKhungBang.getChildren().add(tblDanhSach);
        return pnlKhungBang;
    }

    /**
     * Thực thi truy vấn tìm kiếm dữ liệu từ NhanVienController và cập nhật lên TableView.
     */
    private void xuLyTimKiem() {
        String tuKhoa = txtTuKhoa.getText() != null ? txtTuKhoa.getText().trim() : "";

        try {
            List<NhanVien> ketQua = controller.timKiemNhanVien(tuKhoa, null, null);
            danhSachHienThi.setAll(ketQua);
            lblTongSo.setText("Tổng số: " + ketQua.size() + " nhân viên");
            tblDanhSach.getSelectionModel().clearSelection();
            btnXemChiTiet.setDisable(true);
        } catch (Exception e) {
            lblTongSo.setText("Lỗi tìm kiếm");
            hienThiLoi("Lỗi tìm kiếm nhân viên", e.getMessage());
        }
    }

    /**
     * Đặt lại bộ lọc tìm kiếm và tải lại toàn bộ danh sách nhân viên ban đầu.
     */
    private void lamMoiTimKiem() {
        txtTuKhoa.clear();
        xuLyTimKiem();
    }

    /**
     * Mở hộp thoại popup Xem chi tiết nhân viên (XemChiTietNhanVienView).
     */
    private void moXemChiTietNhanVien() {
        NhanVien nvChon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (nvChon == null) {
            hienThiCanhBao("Chưa chọn nhân viên", "Vui lòng chọn một nhân viên trên bảng để xem chi tiết!");
            return;
        }

        XemChiTietNhanVienView.hienThi(getScene() != null ? getScene().getWindow() : null, nvChon);
    }

    private void hienThiCanhBao(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }

    private void hienThiLoi(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}

