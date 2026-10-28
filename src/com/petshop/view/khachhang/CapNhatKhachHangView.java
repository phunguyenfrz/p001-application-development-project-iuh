package com.petshop.view.khachhang;

import com.petshop.controller.KhachHangController;
import com.petshop.model.KhachHang;
import javafx.beans.property.SimpleIntegerProperty;
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

import java.util.List;
import java.util.Optional;

/**
 * Màn hình Quản lý & Cập nhật khách hàng (KH_CAP_NHAT).
 * <p>
 * Luồng hoạt động chính:
 * 1. Hiển thị danh sách khách hàng dạng bảng TableView với các cột thông tin chi tiết.
 * 2. Cung cấp thanh tác vụ gồm các nút: Thêm mới (mở popup ThemKhachHangView), Cập nhật (mở popup chỉnh sửa),
 *    Xóa mềm (chuyển trạng thái sang Ngừng hoạt động / Kích hoạt lại), và Làm mới (tải lại bảng).
 * 3. Hỗ trợ sự kiện nhấp đúp chuột vào bất kỳ dòng nào trên bảng để mở nhanh cửa sổ Cập nhật.
 * 4. Tự động làm mờ các dòng khách hàng có trạng thái "Ngừng hoạt động" để phân biệt trực quan.
 */
public class CapNhatKhachHangView extends VBox {

    private static final String CSS_CHUNG = "/com/petshop/view/css/chung.css";

    private final KhachHangController khachHangController = new KhachHangController();
    private final ObservableList<KhachHang> danhSachHienThi = FXCollections.observableArrayList();

    // Các nút chức năng trên thanh tác vụ
    private final Button btnThemMoi = new Button();
    private final Button btnCapNhat = new Button();
    private final Button btnLamMoi = new Button();

    // Bảng hiển thị danh sách khách hàng
    private final TableView<KhachHang> tblDanhSach = new TableView<>();

    public CapNhatKhachHangView() {
        setSpacing(14);
        setPadding(new Insets(20, 24, 20, 24));
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề phân hệ nghiệp vụ
        VBox pnlTieuDeManHinh = new VBox(4);
        Label lblTieuDe = new Label("QUẢN LÝ KHÁCH HÀNG - CẬP NHẬT THÔNG TIN");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        Label lblMoTa = new Label("Quản lý danh sách khách hàng và cập nhật thông tin cá nhân.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");

        pnlTieuDeManHinh.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Khung bảng dữ liệu kèm thanh tác vụ
        VBox pnlBangDuLieu = taoKhungBangDanhSach();
        VBox.setVgrow(pnlBangDuLieu, Priority.ALWAYS);

        getChildren().addAll(pnlTieuDeManHinh, pnlBangDuLieu);

        // Khởi tạo trạng thái ban đầu của các nút chức năng
        capNhatTrangThaiNut(null);

        // Tải dữ liệu ban đầu từ cơ sở dữ liệu lên bảng
        taiLaiDanhSach();
    }

    /**
     * Khởi tạo thanh tác vụ phía trên bảng danh sách.
     */
    private HBox taoThanhTacVu() {
        HBox pnlThanhNut = new HBox(10);
        pnlThanhNut.getStyleClass().add("thanh-tac-vu");
        pnlThanhNut.setAlignment(Pos.CENTER_LEFT);
        pnlThanhNut.setMaxWidth(Double.MAX_VALUE);

        // Nhãn tiêu đề danh sách nằm bên trái
        Label lblDanhSach = new Label("Danh sách khách hàng");
        lblDanhSach.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #1e293b;");

        // Khoảng trống co giãn để đẩy cụm nút sang bên phải
        Region pnlKhoangTrong = new Region();
        HBox.setHgrow(pnlKhoangTrong, Priority.ALWAYS);

        // Thiết lập kiểu dáng nút từ file chung.css
        btnThemMoi.setText("Thêm mới");
        btnThemMoi.getStyleClass().add("nut-chinh");
        btnThemMoi.setOnAction(e -> moCuaSoThemMoi());

        btnCapNhat.setText("Cập nhật");
        btnCapNhat.getStyleClass().add("nut-phu");
        btnCapNhat.setOnAction(e -> moCuaSoCapNhat());

        btnLamMoi.setText("Làm mới");
        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> taiLaiDanhSach());

        // Ghép nối: [Nhãn bên trái] -> [Khoảng trống giãn] -> [Cụm nút bên phải]
        pnlThanhNut.getChildren().addAll(lblDanhSach, pnlKhoangTrong, btnThemMoi, btnCapNhat, btnLamMoi);
        return pnlThanhNut;
    }

    /**
     * Khởi tạo khung chứa thanh tác vụ và bảng dữ liệu TableView.
     */
    private VBox taoKhungBangDanhSach() {
        VBox pnlKhung = new VBox(10);
        pnlKhung.getStyleClass().add("hop-noi-dung");

        HBox pnlThanhNut = taoThanhTacVu();

        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Chưa có dữ liệu khách hàng nào trong hệ thống"));
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);

        // --- Cột 1: Mã khách hàng ---
        TableColumn<KhachHang, String> colMa = new TableColumn<>("Mã khách hàng");
        colMa.setPrefWidth(160);
        colMa.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMaKhachHang()));
        colMa.setStyle("-fx-alignment: CENTER;");

        // --- Cột 2: Họ và tên ---
        TableColumn<KhachHang, String> colHoTen = new TableColumn<>("Họ và tên");
        colHoTen.setPrefWidth(200);
        colHoTen.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHoTen()));
        colHoTen.setStyle("-fx-alignment: CENTER-LEFT;");

        // --- Cột 3: Số điện thoại ---
        TableColumn<KhachHang, String> colSdt = new TableColumn<>("Số điện thoại");
        colSdt.setPrefWidth(130);
        colSdt.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSoDienThoai()));
        colSdt.setStyle("-fx-alignment: CENTER;");

        // --- Cột 4: Email ---
        TableColumn<KhachHang, String> colEmail = new TableColumn<>("Email");
        colEmail.setPrefWidth(190);
        colEmail.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getEmail() != null ? cellData.getValue().getEmail() : "-"
        ));
        colEmail.setStyle("-fx-alignment: CENTER-LEFT;");

        // --- Cột 5: Điểm tích lũy ---
        TableColumn<KhachHang, Number> colDiem = new TableColumn<>("Điểm tích lũy");
        colDiem.setPrefWidth(120);
        colDiem.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getDiemTichLuy()));
        colDiem.setStyle("-fx-alignment: CENTER-RIGHT;");

        // --- Cột 6: Địa chỉ ---
        TableColumn<KhachHang, String> colDiaChi = new TableColumn<>("Địa chỉ");
        colDiaChi.setPrefWidth(260);
        colDiaChi.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDiaChi() != null ? cellData.getValue().getDiaChi() : "-"
        ));
        colDiaChi.setStyle("-fx-alignment: CENTER-LEFT;");

        tblDanhSach.getColumns().addAll(colMa, colHoTen, colSdt, colEmail, colDiem, colDiaChi);
        tblDanhSach.setItems(danhSachHienThi);

        // Hỗ trợ nhấp đúp chuột vào dòng để mở nhanh cửa sổ Cập nhật
        tblDanhSach.setRowFactory(tv -> {
            TableRow<KhachHang> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    moCuaSoCapNhat();
                }
            });
            return row;
        });

        // Lắng nghe sự kiện chọn dòng trên bảng để kích hoạt / vô hiệu hóa nút Cập nhật
        tblDanhSach.getSelectionModel().selectedItemProperty().addListener((obs, cu, moi) -> capNhatTrangThaiNut(moi));

        pnlKhung.getChildren().addAll(pnlThanhNut, tblDanhSach);
        return pnlKhung;
    }

    /**
     * Bật / tắt các nút chức năng tùy theo trạng thái dòng được chọn trên bảng.
     */
    private void capNhatTrangThaiNut(KhachHang khachHangChon) {
        btnCapNhat.setDisable(khachHangChon == null);
    }

    /**
     * Tải lại danh sách toàn bộ khách hàng từ tầng Controller.
     */
    public void taiLaiDanhSach() {
        try {
            List<KhachHang> danhSach = khachHangController.layDanhSachKhachHang();
            danhSachHienThi.setAll(danhSach);
            tblDanhSach.getSelectionModel().clearSelection();
            capNhatTrangThaiNut(null);
        } catch (Exception e) {
            hienThiLoi("Lỗi tải danh sách khách hàng", e.getMessage());
        }
    }

    /**
     * Mở hộp thoại popup Thêm mới khách hàng (ThemKhachHangView).
     */
    private void moCuaSoThemMoi() {
        ThemKhachHangView.hienThi(getScene() != null ? getScene().getWindow() : null, this::taiLaiDanhSach);
    }

    /**
     * Mở hộp thoại popup Cập nhật thông tin khách hàng đang được chọn trên bảng.
     */
    private void moCuaSoCapNhat() {
        KhachHang khChon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (khChon == null) {
            hienThiCanhBao("Chưa chọn khách hàng", "Vui lòng chọn một khách hàng trên bảng để tiến hành cập nhật!");
            return;
        }

        Stage cuaSo = new Stage();
        cuaSo.initModality(Modality.APPLICATION_MODAL);
        if (getScene() != null && getScene().getWindow() != null) {
            cuaSo.initOwner(getScene().getWindow());
        }
        cuaSo.setTitle("Cập nhật thông tin khách hàng - " + khChon.getMaKhachHang());

        VBox pnlGoc = new VBox(14);
        pnlGoc.setPadding(new Insets(18));
        pnlGoc.getStyleClass().add("khung-phan-he");

        // Tiêu đề form cập nhật
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("CẬP NHẬT THÔNG TIN KHÁCH HÀNG");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblDangSua = new Label("Đang chỉnh sửa: " + khChon.getMaKhachHang() + " - " + khChon.getHoTen());
        lblDangSua.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #0284c7;");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblDangSua);

        // Khung nhập liệu
        VBox pnlForm = new VBox(14);
        pnlForm.getStyleClass().add("hop-noi-dung");

        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(12);
        pnlLuoi.setVgap(10);

        TextField txtMa = new TextField(khChon.getMaKhachHang());
        txtMa.setEditable(false);

        TextField txtHoTen = new TextField(khChon.getHoTen());
        TextField txtSdt = new TextField(khChon.getSoDienThoai());
        TextField txtEmail = new TextField(khChon.getEmail() != null ? khChon.getEmail() : "");
        TextField txtDiaChi = new TextField(khChon.getDiaChi() != null ? khChon.getDiaChi() : "");
        TextArea txtGhiChu = new TextArea(khChon.getGhiChu() != null ? khChon.getGhiChu() : "");
        txtGhiChu.setPrefRowCount(2);
        txtGhiChu.setWrapText(true);

        // --- Ràng buộc dữ liệu thời gian thực trên form sửa ---
        // 1. Họ tên: Chỉ cho phép chữ cái tiếng Việt, số và khoảng trắng
        txtHoTen.textProperty().addListener((obs, cu, moi) -> {
            if (moi != null && !moi.matches("^[\\p{L}0-9\\s]*$")) {
                txtHoTen.setText(cu != null ? cu : "");
            }
        });

        // 2. Số điện thoại: Chỉ cho phép chữ số, tối đa 10 ký tự
        txtSdt.textProperty().addListener((obs, cu, moi) -> {
            if (moi != null) {
                if (!moi.matches("\\d*")) {
                    txtSdt.setText(cu != null ? cu : "");
                } else if (moi.length() > 10) {
                    txtSdt.setText(moi.substring(0, 10));
                }
            }
        });

        pnlLuoi.add(new Label("Mã khách hàng:"), 0, 0);
        pnlLuoi.add(txtMa, 1, 0);
        pnlLuoi.add(new Label("Họ và tên (*):"), 0, 1);
        pnlLuoi.add(txtHoTen, 1, 1);
        pnlLuoi.add(new Label("Số điện thoại (*):"), 0, 2);
        pnlLuoi.add(txtSdt, 1, 2);
        pnlLuoi.add(new Label("Email:"), 0, 3);
        pnlLuoi.add(txtEmail, 1, 3);
        pnlLuoi.add(new Label("Địa chỉ:"), 0, 4);
        pnlLuoi.add(txtDiaChi, 1, 4);
        pnlLuoi.add(new Label("Ghi chú:"), 0, 5);
        pnlLuoi.add(txtGhiChu, 1, 5);

        ColumnConstraints colNhan = new ColumnConstraints(120);
        ColumnConstraints colNhap = new ColumnConstraints();
        colNhap.setHgrow(Priority.ALWAYS);
        pnlLuoi.getColumnConstraints().addAll(colNhan, colNhap);

        // Thanh nút bấm điều khiển trong form sửa
        HBox pnlNutDieuKhien = new HBox(10);
        pnlNutDieuKhien.setAlignment(Pos.CENTER_RIGHT);

        Button btnLuu = new Button("Lưu thay đổi");
        btnLuu.getStyleClass().add("nut-chinh");
        btnLuu.setOnAction(e -> {
            try {
                String ten = txtHoTen.getText();
                String sdt = txtSdt.getText();
                String email = txtEmail.getText();
                String diaChi = txtDiaChi.getText();
                String ghiChu = txtGhiChu.getText();

                KhachHang khCapNhat = new KhachHang();
                khCapNhat.setMaKhachHang(khChon.getMaKhachHang());
                khCapNhat.setHoTen(ten);
                khCapNhat.setSoDienThoai(sdt);
                khCapNhat.setEmail(email);
                khCapNhat.setDiaChi(diaChi);
                khCapNhat.setGhiChu(ghiChu);
                khCapNhat.setDiemTichLuy(khChon.getDiemTichLuy());
                khCapNhat.setTrangThai(true);

                boolean thanhCong = khachHangController.capNhatKhachHang(khCapNhat);
                if (thanhCong) {
                    cuaSo.close();
                    taiLaiDanhSach();
                    hienThiThongBao("Thành công", "Đã cập nhật thông tin khách hàng '" + khChon.getMaKhachHang() + "' thành công!");
                }
            } catch (IllegalArgumentException ex) {
                hienThiCanhBao("Dữ liệu không hợp lệ", ex.getMessage());
            } catch (Exception ex) {
                hienThiLoi("Lỗi khi cập nhật", ex.getMessage());
            }
        });

        Button btnHuy = new Button("Đóng");
        btnHuy.getStyleClass().add("nut-phu");
        btnHuy.setOnAction(e -> cuaSo.close());

        pnlNutDieuKhien.getChildren().addAll(btnLuu, btnHuy);
        pnlForm.getChildren().addAll(pnlLuoi, pnlNutDieuKhien);
        pnlGoc.getChildren().addAll(pnlTieuDe, pnlForm);

        Scene scene = new Scene(pnlGoc, 500, 440);
        if (CapNhatKhachHangView.class.getResource(CSS_CHUNG) != null) {
            scene.getStylesheets().add(CapNhatKhachHangView.class.getResource(CSS_CHUNG).toExternalForm());
        }

        cuaSo.setScene(scene);
        cuaSo.setResizable(false);
        cuaSo.showAndWait();
    }

    private void hienThiThongBao(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
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
