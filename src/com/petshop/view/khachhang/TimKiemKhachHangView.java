package com.petshop.view.khachhang;

import com.petshop.controller.KhachHangController;
import com.petshop.model.KhachHang;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Màn hình Tìm kiếm & Tra cứu khách hàng (KH_TIM_KIEM).
 * <p>
 * Tính năng chính:
 * 1. Tra cứu linh hoạt theo từ khóa: Mã khách hàng, Họ tên, Số điện thoại và Email.
 * 2. Lọc khách hàng theo trạng thái hoạt động: "Tất cả", "Đang hoạt động", "Ngừng hoạt động".
 * 3. Hỗ trợ xem thông tin chi tiết khách hàng và danh sách thú cưng (XemChiTietKhachHangView):
 *    - Bấm nút "Xem chi tiết" trên thanh tác vụ khi chọn dòng.
 *    - Nhấp đúp chuột vào bất kỳ dòng khách hàng nào để mở hộp thoại XemChiTietKhachHangView.
 */
public class TimKiemKhachHangView extends VBox {

    private final KhachHangController khachHangController = new KhachHangController();
    private final ObservableList<KhachHang> danhSachHienThi = FXCollections.observableArrayList();

    // Các thành phần tìm kiếm và lọc
    private final TextField txtTuKhoa = new TextField();
    private final ComboBox<String> cboLocTrangThai = new ComboBox<>();
    private final Button btnTimKiem = new Button("Tìm kiếm");
    private final Button btnLamMoi = new Button("Làm mới");
    private final Button btnXemChiTiet = new Button("Xem chi tiết");
    private final Label lblTongSo = new Label("Tổng số: 0 khách hàng");

    // Bảng dữ liệu hiển thị kết quả
    private final TableView<KhachHang> tblDanhSach = new TableView<>();

    public TimKiemKhachHangView() {
        setSpacing(14);
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề màn hình
        VBox pnlTieuDeManHinh = new VBox(4);
        Label lblTieuDe = new Label("TÌM KIẾM & TRA CỨU KHÁCH HÀNG");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        Label lblMoTa = new Label("Tra cứu nhanh thông tin khách hàng theo mã, tên, SĐT, email hoặc lọc theo trạng thái hoạt động.");
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
     * - Ô nhập từ khóa (mã, tên, SĐT, email)
     * - Bộ lọc trạng thái (Tất cả, Đang hoạt động, Ngừng hoạt động)
     * - Nút Tìm kiếm, Làm mới và Nút Xem chi tiết (khi chọn dòng)
     */
    private HBox taoThanhTimKiem() {
        HBox pnlCongCu = new HBox(10);
        pnlCongCu.getStyleClass().add("thanh-tac-vu");
        pnlCongCu.setAlignment(Pos.CENTER_LEFT);
        pnlCongCu.setMaxWidth(Double.MAX_VALUE);

        // Ô nhập từ khóa tra cứu
        Label lblTuKhoa = new Label("Từ khóa:");
        txtTuKhoa.setPromptText("Nhập mã, tên, SĐT hoặc email khách hàng...");
        txtTuKhoa.setPrefWidth(290);
        txtTuKhoa.setOnAction(e -> xuLyTimKiem());
        // Lọc tự động thời gian thực khi người dùng gõ từ khóa
        txtTuKhoa.textProperty().addListener((obs, oldVal, newVal) -> xuLyTimKiem());

        // ComboBox lọc trạng thái hoạt động
        Label lblTrangThai = new Label("Trạng thái:");
        cboLocTrangThai.setItems(FXCollections.observableArrayList(
                "Tất cả", "Đang hoạt động", "Ngừng hoạt động"
        ));
        cboLocTrangThai.setValue("Tất cả");
        cboLocTrangThai.setOnAction(e -> xuLyTimKiem());

        // Nút Tìm kiếm
        btnTimKiem.getStyleClass().add("nut-chinh");
        btnTimKiem.setOnAction(e -> xuLyTimKiem());

        // Nút Làm mới
        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> lamMoiTimKiem());

        // Khoảng trống đẩy nút xem chi tiết và nhãn tổng số sang bên phải
        Region pnlKhoangTrong = new Region();
        HBox.setHgrow(pnlKhoangTrong, Priority.ALWAYS);

        // Nút xem chi tiết khi chọn một khách hàng trên bảng
        btnXemChiTiet.getStyleClass().add("nut-phu");
        btnXemChiTiet.setOnAction(e -> moXemChiTietKhachHang());

        lblTongSo.setStyle("-fx-font-weight: 600; -fx-text-fill: #475569; -fx-font-size: 13px;");

        pnlCongCu.getChildren().addAll(
                lblTuKhoa, txtTuKhoa,
                btnTimKiem,
                lblTrangThai,
                cboLocTrangThai,
                btnLamMoi,
                pnlKhoangTrong,
                btnXemChiTiet, lblTongSo
        );

        return pnlCongCu;
    }

    /**
     * Khởi tạo bảng dữ liệu TableView hiển thị kết quả tìm kiếm khách hàng.
     */
    private VBox taoKhungBangDanhSach() {
        VBox pnlKhungBang = new VBox(8);

        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Không tìm thấy khách hàng nào phù hợp với điều kiện tra cứu"));
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);

        // --- Cột 1: Mã khách hàng ---
        TableColumn<KhachHang, String> colMa = new TableColumn<>("Mã khách hàng");
        colMa.setPrefWidth(150);
        colMa.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMaKhachHang()));
        colMa.setStyle("-fx-alignment: CENTER;");

        // --- Cột 2: Họ và tên ---
        TableColumn<KhachHang, String> colHoTen = new TableColumn<>("Họ và tên");
        colHoTen.setPrefWidth(180);
        colHoTen.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHoTen()));
        colHoTen.setStyle("-fx-alignment: CENTER-LEFT;");

        // --- Cột 3: Số điện thoại ---
        TableColumn<KhachHang, String> colSdt = new TableColumn<>("Số điện thoại");
        colSdt.setPrefWidth(120);
        colSdt.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSoDienThoai()));
        colSdt.setStyle("-fx-alignment: CENTER;");

        // --- Cột 4: Email ---
        TableColumn<KhachHang, String> colEmail = new TableColumn<>("Email");
        colEmail.setPrefWidth(170);
        colEmail.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getEmail() != null ? cellData.getValue().getEmail() : "-"
        ));
        colEmail.setStyle("-fx-alignment: CENTER-LEFT;");

        // --- Cột 5: Điểm tích lũy ---
        TableColumn<KhachHang, Number> colDiem = new TableColumn<>("Điểm tích lũy");
        colDiem.setPrefWidth(110);
        colDiem.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getDiemTichLuy()));
        colDiem.setStyle("-fx-alignment: CENTER-RIGHT;");

        // --- Cột 6: Địa chỉ ---
        TableColumn<KhachHang, String> colDiaChi = new TableColumn<>("Địa chỉ");
        colDiaChi.setPrefWidth(220);
        colDiaChi.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDiaChi() != null ? cellData.getValue().getDiaChi() : "-"
        ));
        colDiaChi.setStyle("-fx-alignment: CENTER-LEFT;");

        // --- Cột 7: Trạng thái (Hiển thị văn bản thuần giống bên dịch vụ) ---
        TableColumn<KhachHang, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setPrefWidth(140);
        colTrangThai.setStyle("-fx-alignment: CENTER;");
        colTrangThai.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().isTrangThai() ? "Đang hoạt động" : "Ngừng hoạt động"
        ));

        tblDanhSach.getColumns().addAll(colMa, colHoTen, colSdt, colEmail, colDiem, colDiaChi, colTrangThai);
        tblDanhSach.setItems(danhSachHienThi);

        // =====================================================================
        // XỬ LÝ LÀM MỜ DÒNG NGỪNG HOẠT ĐỘNG VÀ SỰ KIỆN NHẤP ĐÚP CHUỘT
        // =====================================================================
        tblDanhSach.setRowFactory(tv -> {
            TableRow<KhachHang> row = new TableRow<>() {
                @Override
                protected void updateItem(KhachHang khachHang, boolean empty) {
                    super.updateItem(khachHang, empty);
                    if (empty || khachHang == null) {
                        setOpacity(1.0);
                    } else {
                        setOpacity(khachHang.isTrangThai() ? 1.0 : 0.55);
                    }
                }
            };

            // Sự kiện nhấp đúp chuột (2 lần) để xem thông tin chi tiết
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    moXemChiTietKhachHang();
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
     * Thực thi truy vấn tìm kiếm dữ liệu từ KhachHangController và cập nhật lên TableView.
     */
    private void xuLyTimKiem() {
        String tuKhoa = txtTuKhoa.getText() != null ? txtTuKhoa.getText().trim() : "";
        String trangThai = cboLocTrangThai.getValue();

        try {
            List<KhachHang> ketQua = khachHangController.timKiemKhachHang(tuKhoa, trangThai);
            danhSachHienThi.setAll(ketQua);
            lblTongSo.setText("Tổng số: " + ketQua.size() + " khách hàng");
            btnXemChiTiet.setDisable(true);
        } catch (Exception e) {
            lblTongSo.setText("Tổng số: 0 khách hàng");
        }
    }

    /**
     * Mở hộp thoại Xem chi tiết khách hàng và danh sách thú cưng (XemChiTietKhachHangView).
     */
    private void moXemChiTietKhachHang() {
        KhachHang khChon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (khChon == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Cảnh báo");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng chọn một khách hàng trong danh sách để xem chi tiết!");
            alert.showAndWait();
            return;
        }

        XemChiTietKhachHangView.hienThi(khChon, getScene() != null ? getScene().getWindow() : null);
    }

    /**
     * Làm mới toàn bộ điều kiện lọc và tải lại bảng.
     */
    private void lamMoiTimKiem() {
        txtTuKhoa.clear();
        cboLocTrangThai.setValue("Tất cả");
        xuLyTimKiem();
        txtTuKhoa.requestFocus();
    }
}
