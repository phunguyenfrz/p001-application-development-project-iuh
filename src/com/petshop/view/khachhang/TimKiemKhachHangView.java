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
 * Màn hình Tìm kiếm khách hàng (KH_TIM_KIEM).
 * Hỗ trợ tra cứu linh hoạt qua mã, họ tên, số điện thoại, email và bộ lọc trạng thái hoạt động.
 * Tính năng xem chi tiết:
 * - Nút "Xem chi tiết" trên thanh tác vụ (kích hoạt khi chọn 1 khách hàng trên bảng)
 * - Nhấp đúp chuột vào bất kỳ dòng khách hàng nào để mở hộp thoại XemThongTinKhachHangView.
 */
public class TimKiemKhachHangView extends VBox {

    private final KhachHangController khachHangController = new KhachHangController();
    private final ObservableList<KhachHang> danhSachHienThi = FXCollections.observableArrayList();

    // Các thành phần trên thanh công cụ tìm kiếm
    private final TextField txtTuKhoa = new TextField();
    private final ComboBox<String> cboLocTrangThai = new ComboBox<>();
    private final Button btnTimKiem = new Button("Tìm kiếm");
    private final Button btnLamMoi = new Button("Làm mới");
    private final Button btnXemChiTiet = new Button("Xem chi tiết");
    private final Label lblTongSo = new Label("Tổng số: 0 khách hàng");

    // Bảng hiển thị danh sách kết quả
    private final TableView<KhachHang> tblDanhSach = new TableView<>();

    public TimKiemKhachHangView() {
        setSpacing(14);
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề phân hệ
        VBox pnlTieuDeMan = new VBox(4);
        Label lblTieuDe = new Label("KHÁCH HÀNG - TÌM KIẾM KHÁCH HÀNG");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Tra cứu danh mục khách hàng theo mã, họ tên, số điện thoại hoặc email và lọc theo trạng thái hoạt động.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDeMan.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Khung nội dung chính bao gồm thanh công cụ và bảng danh sách
        VBox pnlKhungChinh = new VBox(12);
        pnlKhungChinh.getStyleClass().add("hop-noi-dung");
        VBox.setVgrow(pnlKhungChinh, Priority.ALWAYS);

        HBox pnlThanhCongCu = taoThanhTimKiem();
        VBox pnlBang = taoKhungBangDanhSach();
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        pnlKhungChinh.getChildren().addAll(pnlThanhCongCu, pnlBang);
        getChildren().addAll(pnlTieuDeMan, pnlKhungChinh);

        // 3. Nạp dữ liệu ban đầu
        btnXemChiTiet.setDisable(true);
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

        lblTongSo.getStyleClass().add("nhan-tong-so");

        pnlCongCu.getChildren().addAll(
                lblTuKhoa, txtTuKhoa,
                 btnTimKiem
                , lblTrangThai,
                 cboLocTrangThai
                , btnLamMoi,
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
        colMa.getStyleClass().addAll("cot-can-giua", "cot-in-dam");

        // --- Cột 2: Họ và tên ---
        TableColumn<KhachHang, String> colHoTen = new TableColumn<>("Họ và tên");
        colHoTen.setPrefWidth(180);
        colHoTen.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHoTen()));
        colHoTen.getStyleClass().add("cot-can-trai");

        // --- Cột 3: Số điện thoại ---
        TableColumn<KhachHang, String> colSdt = new TableColumn<>("Số điện thoại");
        colSdt.setPrefWidth(120);
        colSdt.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSoDienThoai()));
        colSdt.getStyleClass().add("cot-can-giua");

        // --- Cột 4: Email ---
        TableColumn<KhachHang, String> colEmail = new TableColumn<>("Email");
        colEmail.setPrefWidth(180);
        colEmail.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getEmail() != null ? cellData.getValue().getEmail() : "-"
        ));
        colEmail.getStyleClass().add("cot-can-trai");

        // --- Cột 5: Điểm tích lũy ---
        TableColumn<KhachHang, Number> colDiem = new TableColumn<>("Điểm tích lũy");
        colDiem.setPrefWidth(110);
        colDiem.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getDiemTichLuy()));
        colDiem.getStyleClass().addAll("cot-can-phai", "cot-in-dam");

        // --- Cột 6: Địa chỉ ---
        TableColumn<KhachHang, String> colDiaChi = new TableColumn<>("Địa chỉ");
        colDiaChi.setPrefWidth(220);
        colDiaChi.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDiaChi() != null ? cellData.getValue().getDiaChi() : "-"
        ));
        colDiaChi.getStyleClass().add("cot-can-trai");

        // --- Cột 7: Trạng thái (Badge xanh / đỏ) ---
        TableColumn<KhachHang, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setPrefWidth(140);
        colTrangThai.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().isTrangThai() ? "Đang hoạt động" : "Ngừng hoạt động"
        ));
        colTrangThai.setCellFactory(col -> new TableCell<KhachHang, String>() {
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
                    if ("Đang hoạt động".equals(item)) {
                        badge.getStyleClass().add("nhan-trang-thai-hoat-dong");
                    } else {
                        badge.getStyleClass().add("nhan-trang-thai-khoa");
                    }
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                    setText(null);
                }
            }
        });

        tblDanhSach.getColumns().addAll(colMa, colHoTen, colSdt, colEmail, colDiem, colDiaChi, colTrangThai);
        tblDanhSach.setItems(danhSachHienThi);

        // =====================================================================
        // ROW FACTORY: Làm mờ dòng ngừng hoạt động & Nhấp đúp chuột xem chi tiết
        // =====================================================================
        tblDanhSach.setRowFactory(tv -> {
            TableRow<KhachHang> row = new TableRow<>() {
                @Override
                protected void updateItem(KhachHang khachHang, boolean empty) {
                    super.updateItem(khachHang, empty);
                    getStyleClass().remove("dong-ngung-hoat-dong");
                    if (!empty && khachHang != null && !khachHang.isTrangThai()) {
                        getStyleClass().add("dong-ngung-hoat-dong");
                    }
                }
            };

            // Sự kiện nhấp đúp chuột vào bất kỳ dòng nào để xem chi tiết thông tin
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    moXemChiTietKhachHang();
                }
            });
            return row;
        });

        // Kích hoạt / vô hiệu hóa nút "Xem chi tiết" theo dòng đang chọn
        tblDanhSach.getSelectionModel().selectedItemProperty().addListener((obs, cu, moi) -> {
            btnXemChiTiet.setDisable(moi == null);
        });

        pnlKhungBang.getChildren().add(tblDanhSach);
        return pnlKhungBang;
    }

    /**
     * Xử lý tìm kiếm theo từ khóa và trạng thái được chọn trên thanh công cụ.
     */
    private void xuLyTimKiem() {
        String tuKhoa = txtTuKhoa.getText();
        String locTrangThai = cboLocTrangThai.getValue();

        try {
            List<KhachHang> ketQua = khachHangController.timKiemKhachHang(tuKhoa, locTrangThai);
            danhSachHienThi.setAll(ketQua);
            lblTongSo.setText("Tổng số: " + ketQua.size() + " khách hàng");
            tblDanhSach.refresh();
        } catch (Exception ex) {
            System.err.println("[TimKiemKhachHangView] Lỗi tìm kiếm: " + ex.getMessage());
        }
    }

    /**
     * Mở hộp thoại Xem thông tin chi tiết của khách hàng đang chọn trên bảng.
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

        XemThongTinKhachHangView.hienThi(khChon, getScene() != null ? getScene().getWindow() : null);
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

