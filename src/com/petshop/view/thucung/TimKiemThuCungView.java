package com.petshop.view.thucung;

import com.petshop.controller.ThuCungController;
import com.petshop.model.ThuCung;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Màn hình Tra cứu & Tìm kiếm thú cưng (TC_TIM_KIEM).
 * Cho phép tra cứu nhanh hồ sơ thú cưng theo mã, tên bé, giống loài, hoặc theo tên/SĐT của chủ nuôi.
 * Hỗ trợ xem chi tiết hồ sơ và nhật ký chăm sóc dịch vụ của thú cưng.
 */
public class TimKiemThuCungView extends VBox {

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ThuCungController thuCungController = new ThuCungController();
    private final ObservableList<ThuCung> danhSachHienThi = FXCollections.observableArrayList();

    // Các thành phần tìm kiếm và lọc
    private final TextField txtTuKhoa = new TextField();
    private final ComboBox<String> cboLocLoai = new ComboBox<>();
    private final Button btnTimKiem = new Button("Tìm kiếm");
    private final Button btnLamMoi = new Button("Làm mới");
    private final Button btnXemChiTiet = new Button("Xem chi tiết");
    private final Label lblTongSo = new Label("Tổng số: 0 thú cưng");

    // Bảng dữ liệu hiển thị kết quả
    private final TableView<ThuCung> tblDanhSach = new TableView<>();

    public TimKiemThuCungView() {
        setSpacing(14);
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề màn hình
        VBox pnlTieuDeManHinh = new VBox(4);
        Label lblTieuDe = new Label("TÌM KIẾM & TRA CỨU THÚ CƯNG");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        Label lblMoTa = new Label("Tra cứu nhanh hồ sơ thú cưng theo mã, tên thú cưng, giống loài hoặc thông tin chủ nuôi.");
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
     * - Ô nhập từ khóa (mã, tên thú cưng, giống loài, tên hoặc SĐT chủ nuôi)
     * - Bộ lọc loài thú cưng (Tất cả, Chó, Mèo, Thỏ, Hamster, Chim, Khác)
     * - Nút Tìm kiếm, Làm mới và Nút Xem chi tiết (khi chọn dòng)
     */
    private HBox taoThanhTimKiem() {
        HBox pnlCongCu = new HBox(10);
        pnlCongCu.getStyleClass().add("thanh-tac-vu");
        pnlCongCu.setAlignment(Pos.CENTER_LEFT);
        pnlCongCu.setMaxWidth(Double.MAX_VALUE);

        // Ô nhập từ khóa tra cứu
        Label lblTuKhoa = new Label("Từ khóa:");
        txtTuKhoa.setPromptText("Nhập mã, tên bé, giống loài, tên hoặc SĐT chủ nuôi...");
        txtTuKhoa.setPrefWidth(320);
        txtTuKhoa.setOnAction(e -> xuLyTimKiem());
        // Lọc tự động thời gian thực khi người dùng gõ từ khóa
        txtTuKhoa.textProperty().addListener((obs, oldVal, newVal) -> xuLyTimKiem());

        // ComboBox lọc loài thú cưng
        Label lblLoai = new Label("Loài:");
        cboLocLoai.setItems(FXCollections.observableArrayList(
                "Tất cả", "Chó", "Mèo", "Thỏ", "Hamster", "Chim", "Khác"
        ));
        cboLocLoai.setValue("Tất cả");
        cboLocLoai.setOnAction(e -> xuLyTimKiem());

        // Nút Tìm kiếm
        btnTimKiem.getStyleClass().add("nut-chinh");
        btnTimKiem.setOnAction(e -> xuLyTimKiem());

        // Nút Làm mới
        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> lamMoiTimKiem());

        // Khoảng trống đẩy nút xem chi tiết và nhãn tổng số sang bên phải
        Region pnlKhoangTrong = new Region();
        HBox.setHgrow(pnlKhoangTrong, Priority.ALWAYS);

        // Nút xem chi tiết khi chọn một thú cưng trên bảng
        btnXemChiTiet.getStyleClass().add("nut-phu");
        btnXemChiTiet.setOnAction(e -> moXemChiTietThuCung());

        lblTongSo.setStyle("-fx-font-weight: 600; -fx-text-fill: #475569; -fx-font-size: 13px;");

        pnlCongCu.getChildren().addAll(
                lblTuKhoa, txtTuKhoa,
                lblLoai, cboLocLoai,
                btnTimKiem,
                btnLamMoi,
                pnlKhoangTrong,
                btnXemChiTiet, lblTongSo
        );

        return pnlCongCu;
    }

    /**
     * Khởi tạo bảng dữ liệu TableView hiển thị kết quả tìm kiếm thú cưng.
     */
    private VBox taoKhungBangDanhSach() {
        VBox pnlKhungBang = new VBox(8);

        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Không tìm thấy thú cưng nào phù hợp với điều kiện tra cứu"));
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);

        // --- Cột 1: Mã thú cưng ---
        TableColumn<ThuCung, String> colMa = new TableColumn<>("Mã thú cưng");
        colMa.setPrefWidth(150);
        colMa.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaThuCung()));
        colMa.setStyle("-fx-alignment: CENTER;");

        // --- Cột 2: Tên thú cưng ---
        TableColumn<ThuCung, String> colTen = new TableColumn<>("Tên thú cưng");
        colTen.setPrefWidth(150);
        colTen.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTenThuCung()));
        colTen.setStyle("-fx-alignment: CENTER-LEFT;");

        // --- Cột 3: Loài ---
        TableColumn<ThuCung, String> colLoai = new TableColumn<>("Loài");
        colLoai.setPrefWidth(100);
        colLoai.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getLoaiThuCung() != null ? cell.getValue().getLoaiThuCung() : "-"
        ));
        colLoai.setStyle("-fx-alignment: CENTER;");

        // --- Cột 4: Giống loài ---
        TableColumn<ThuCung, String> colGiong = new TableColumn<>("Giống loài");
        colGiong.setPrefWidth(160);
        colGiong.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getGiongLoai() != null ? cell.getValue().getGiongLoai() : "-"
        ));
        colGiong.setStyle("-fx-alignment: CENTER-LEFT;");

        // --- Cột 5: Giới tính ---
        TableColumn<ThuCung, String> colGioiTinh = new TableColumn<>("Giới tính");
        colGioiTinh.setPrefWidth(85);
        colGioiTinh.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getGioiTinh() != null ? cell.getValue().getGioiTinh() : "-"
        ));
        colGioiTinh.setStyle("-fx-alignment: CENTER;");

        // --- Cột 6: Cân nặng ---
        TableColumn<ThuCung, String> colCanNang = new TableColumn<>("Cân nặng");
        colCanNang.setPrefWidth(110);
        colCanNang.setCellValueFactory(cell -> {
            BigDecimal cn = cell.getValue().getCanNang();
            return new SimpleStringProperty(cn != null ? String.format("%.2f kg", cn) : "-");
        });
        colCanNang.setStyle("-fx-alignment: CENTER-RIGHT;");

        // --- Cột 7: Ngày sinh ---
        TableColumn<ThuCung, String> colNgaySinh = new TableColumn<>("Ngày sinh");
        colNgaySinh.setPrefWidth(115);
        colNgaySinh.setCellValueFactory(cell -> {
            LocalDate ns = cell.getValue().getNgaySinh();
            return new SimpleStringProperty(ns != null ? ns.format(DINH_DANG_NGAY) : "-");
        });
        colNgaySinh.setStyle("-fx-alignment: CENTER;");

        // --- Cột 8: Tên chủ nuôi ---
        TableColumn<ThuCung, String> colTenChuNuoi = new TableColumn<>("Tên chủ nuôi");
        colTenChuNuoi.setPrefWidth(160);
        colTenChuNuoi.setCellValueFactory(cell -> {
            ThuCung tc = cell.getValue();
            if (tc.getTenKhachHang() != null && !tc.getTenKhachHang().isEmpty()) {
                return new SimpleStringProperty(tc.getTenKhachHang());
            }
            return new SimpleStringProperty(tc.getMaKhachHang() != null ? tc.getMaKhachHang() : "-");
        });
        colTenChuNuoi.setStyle("-fx-alignment: CENTER-LEFT;");

        // --- Cột 9: SĐT chủ nuôi ---
        TableColumn<ThuCung, String> colSdtChuNuoi = new TableColumn<>("SĐT chủ nuôi");
        colSdtChuNuoi.setPrefWidth(120);
        colSdtChuNuoi.setCellValueFactory(cell -> {
            ThuCung tc = cell.getValue();
            return new SimpleStringProperty(
                    tc.getSoDienThoaiKhachHang() != null ? tc.getSoDienThoaiKhachHang() : "-"
            );
        });
        colSdtChuNuoi.setStyle("-fx-alignment: CENTER;");

        // --- Cột 10: Dị ứng / Ghi chú ---
        TableColumn<ThuCung, String> colDiUng = new TableColumn<>("Dị ứng / Ghi chú");
        colDiUng.setMinWidth(180);
        colDiUng.prefWidthProperty().bind(
                tblDanhSach.widthProperty().subtract(150 + 150 + 100 + 150 + 80 + 105 + 110 + 160 + 120 + 20)
        );
        colDiUng.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getDiUng() != null ? cell.getValue().getDiUng() : "-"
        ));
        colDiUng.setStyle("-fx-alignment: CENTER-LEFT;");

        tblDanhSach.getColumns().addAll(List.of(
                colMa, colTen, colLoai, colGiong, colGioiTinh, colCanNang, colNgaySinh, colTenChuNuoi, colSdtChuNuoi, colDiUng
        ));
        tblDanhSach.setItems(danhSachHienThi);

        // Sự kiện nhấp đúp chuột để xem chi tiết thú cưng
        tblDanhSach.setRowFactory(tv -> {
            TableRow<ThuCung> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    moXemChiTietThuCung();
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
     * Thực thi truy vấn tìm kiếm dữ liệu từ ThuCungController và cập nhật lên TableView.
     */
    private void xuLyTimKiem() {
        String tuKhoa = txtTuKhoa.getText() != null ? txtTuKhoa.getText().trim() : "";
        String loai = cboLocLoai.getValue();

        try {
            List<ThuCung> ketQua = thuCungController.timKiemThuCung(tuKhoa, loai);
            danhSachHienThi.setAll(ketQua);
            lblTongSo.setText("Tổng số: " + ketQua.size() + " thú cưng");
            btnXemChiTiet.setDisable(true);
        } catch (Exception e) {
            lblTongSo.setText("Tổng số: 0 thú cưng");
        }
    }

    /**
     * Mở hộp thoại Xem chi tiết thú cưng (XemChiTietThuCungView).
     */
    private void moXemChiTietThuCung() {
        ThuCung tcChon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (tcChon == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Cảnh báo");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng chọn một thú cưng trong danh sách để xem chi tiết!");
            alert.showAndWait();
            return;
        }

        XemChiTietThuCungView.hienThi(tcChon, getScene() != null ? getScene().getWindow() : null);
    }

    /**
     * Làm mới toàn bộ điều kiện lọc và tải lại bảng.
     */
    private void lamMoiTimKiem() {
        txtTuKhoa.clear();
        cboLocLoai.setValue("Tất cả");
        xuLyTimKiem();
        txtTuKhoa.requestFocus();
    }
}

