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
    private final Button btnXoaMem = new Button();
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

        Label lblMoTa = new Label("Quản lý danh sách khách hàng, cập nhật thông tin cá nhân và quản lý trạng thái hoạt động.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");

        pnlTieuDeManHinh.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Khung bảng dữ liệu kèm thanh tác vụ
        VBox pnlBangDuLieu = taoKhungBangDanhSach();
        VBox.setVgrow(pnlBangDuLieu, Priority.ALWAYS);

        getChildren().addAll(pnlTieuDeManHinh, pnlBangDuLieu);

        // Khởi tạo trạng thái ban đầu của các nút chức năng (vô hiệu hóa các nút cần chọn dòng)
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

        btnXoaMem.setText("Xóa");
        btnXoaMem.getStyleClass().add("nut-xoa");
        btnXoaMem.setOnAction(e -> xuLyXoaMemHoacKichHoat());

        btnLamMoi.setText("Làm mới");
        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> taiLaiDanhSach());

        // Ghép nối: [Nhãn bên trái] -> [Khoảng trống giãn] -> [Cụm nút bên phải]
        pnlThanhNut.getChildren().addAll(lblDanhSach, pnlKhoangTrong, btnThemMoi, btnCapNhat, btnXoaMem, btnLamMoi);
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
        colMa.setPrefWidth(150);
        colMa.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMaKhachHang()));
        colMa.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

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
        colDiem.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");

        // --- Cột 6: Địa chỉ ---
        TableColumn<KhachHang, String> colDiaChi = new TableColumn<>("Địa chỉ");
        colDiaChi.setPrefWidth(220);
        colDiaChi.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDiaChi() != null ? cellData.getValue().getDiaChi() : "-"
        ));
        colDiaChi.setStyle("-fx-alignment: CENTER-LEFT;");

        // --- Cột 7: Trạng thái (Hiển thị Badge xanh / đỏ qua CSS chung) ---
        TableColumn<KhachHang, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setPrefWidth(140);
        colTrangThai.setStyle("-fx-alignment: CENTER;");
        colTrangThai.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().isTrangThai() ? "Đang hoạt động" : "Ngừng hoạt động"
        ));
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
                    if ("Đang hoạt động".equalsIgnoreCase(item)) {
                        badge.getStyleClass().add("nhan-trang-thai-hoat-dong");
                    } else {
                        badge.getStyleClass().add("nhan-trang-thai-khoa");
                    }
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        tblDanhSach.getColumns().addAll(colMa, colHoTen, colSdt, colEmail, colDiem, colDiaChi, colTrangThai);
        tblDanhSach.setItems(danhSachHienThi);

        // =====================================================================
        // XỬ LÝ LÀM MỜ DÒNG NGỪNG HOẠT ĐỘNG & HỖ TRỢ NHẤP ĐÚP CHUỘT ĐỂ SỬA NHANH
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
            // Nhấp đúp chuột vào dòng để mở cửa sổ Cập nhật
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    moCuaSoCapNhat();
                }
            });
            return row;
        });

        // Lắng nghe sự kiện chọn dòng trên bảng để kích hoạt / vô hiệu hóa các nút tương ứng
        tblDanhSach.getSelectionModel().selectedItemProperty().addListener((obs, cu, moi) -> capNhatTrangThaiNut(moi));

        pnlKhung.getChildren().addAll(pnlThanhNut, tblDanhSach);
        return pnlKhung;
    }

    /**
     * Bật / tắt các nút chức năng tùy theo trạng thái dòng được chọn trên bảng.
     */
    private void capNhatTrangThaiNut(KhachHang khachHangChon) {
        if (khachHangChon == null) {
            btnCapNhat.setDisable(true);
            btnXoaMem.setDisable(true);
            btnXoaMem.setText("Xóa");
            btnXoaMem.getStyleClass().clear();
            btnXoaMem.getStyleClass().addAll("button", "nut-xoa");
        } else {
            btnCapNhat.setDisable(false);
            btnXoaMem.setDisable(false);

            // Nếu khách hàng đang hoạt động: hiển thị nút "Xóa" (màu đỏ)
            // Nếu khách hàng đã ngừng hoạt động: hiển thị nút "Kích hoạt lại" (màu xanh chính)
            btnXoaMem.getStyleClass().clear();
            btnXoaMem.getStyleClass().add("button");
            if (khachHangChon.isTrangThai()) {
                btnXoaMem.setText("Xóa");
                btnXoaMem.getStyleClass().add("nut-xoa");
            } else {
                btnXoaMem.setText("Kích hoạt lại");
                btnXoaMem.getStyleClass().add("nut-chinh");
            }
        }
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
        txtMa.setDisable(true);
        txtMa.setStyle("-fx-opacity: 0.9; -fx-font-weight: bold; -fx-background-color: #f1f5f9;");

        TextField txtHoTen = new TextField(khChon.getHoTen());
        TextField txtSdt = new TextField(khChon.getSoDienThoai());
        TextField txtEmail = new TextField(khChon.getEmail() != null ? khChon.getEmail() : "");
        TextField txtDiaChi = new TextField(khChon.getDiaChi() != null ? khChon.getDiaChi() : "");
        TextArea txtGhiChu = new TextArea(khChon.getGhiChu() != null ? khChon.getGhiChu() : "");
        txtGhiChu.setPrefRowCount(2);
        txtGhiChu.setWrapText(true);

        ComboBox<String> cboTrangThai = new ComboBox<>();
        cboTrangThai.setItems(FXCollections.observableArrayList("Đang hoạt động", "Ngừng hoạt động"));
        cboTrangThai.setValue(khChon.isTrangThai() ? "Đang hoạt động" : "Ngừng hoạt động");
        cboTrangThai.setPrefWidth(200);

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
        pnlLuoi.add(new Label("Trạng thái:"), 0, 5);
        pnlLuoi.add(cboTrangThai, 1, 5);
        pnlLuoi.add(new Label("Ghi chú:"), 0, 6);
        pnlLuoi.add(txtGhiChu, 1, 6);

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
                boolean trangThai = "Đang hoạt động".equalsIgnoreCase(cboTrangThai.getValue());

                KhachHang khCapNhat = new KhachHang();
                khCapNhat.setMaKhachHang(khChon.getMaKhachHang());
                khCapNhat.setHoTen(ten);
                khCapNhat.setSoDienThoai(sdt);
                khCapNhat.setEmail(email);
                khCapNhat.setDiaChi(diaChi);
                khCapNhat.setGhiChu(ghiChu);
                khCapNhat.setDiemTichLuy(khChon.getDiemTichLuy());
                khCapNhat.setTrangThai(trangThai);

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

        Scene scene = new Scene(pnlGoc, 500, 480);
        if (CapNhatKhachHangView.class.getResource(CSS_CHUNG) != null) {
            scene.getStylesheets().add(CapNhatKhachHangView.class.getResource(CSS_CHUNG).toExternalForm());
        }

        cuaSo.setScene(scene);
        cuaSo.setResizable(false);
        cuaSo.showAndWait();
    }

    /**
     * Xử lý hành động xóa mềm (ngừng hoạt động) hoặc kích hoạt lại khách hàng.
     */
    private void xuLyXoaMemHoacKichHoat() {
        KhachHang khChon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (khChon == null) {
            return;
        }

        if (khChon.isTrangThai()) {
            Alert xacNhan = new Alert(Alert.AlertType.CONFIRMATION);
            xacNhan.setTitle("Xác nhận ngưng hoạt động");
            xacNhan.setHeaderText("Ngưng hoạt động khách hàng: " + khChon.getHoTen());
            xacNhan.setContentText("Bạn có chắc chắn muốn ngưng hoạt động khách hàng '" + khChon.getMaKhachHang() +
                    "' không?\nKhách hàng này sẽ được làm mờ trên bảng.");

            Optional<ButtonType> ketQua = xacNhan.showAndWait();
            if (ketQua.isPresent() && ketQua.get() == ButtonType.OK) {
                try {
                    boolean ok = khachHangController.xoaMemKhachHang(khChon.getMaKhachHang());
                    if (ok) {
                        taiLaiDanhSach();
                        hienThiThongBao("Thành công", "Đã chuyển trạng thái khách hàng sang ngừng hoạt động!");
                    }
                } catch (Exception ex) {
                    hienThiLoi("Lỗi thực hiện", ex.getMessage());
                }
            }
        } else {
            Alert xacNhan = new Alert(Alert.AlertType.CONFIRMATION);
            xacNhan.setTitle("Xác nhận kích hoạt lại");
            xacNhan.setHeaderText("Kích hoạt lại khách hàng: " + khChon.getHoTen());
            xacNhan.setContentText("Bạn có muốn kích hoạt lại khách hàng '" + khChon.getMaKhachHang() + "' không?");

            Optional<ButtonType> ketQua = xacNhan.showAndWait();
            if (ketQua.isPresent() && ketQua.get() == ButtonType.OK) {
                try {
                    boolean ok = khachHangController.kichHoatLaiKhachHang(khChon.getMaKhachHang());
                    if (ok) {
                        taiLaiDanhSach();
                        hienThiThongBao("Thành công", "Khách hàng đã được kích hoạt lại thành công!");
                    }
                } catch (Exception ex) {
                    hienThiLoi("Lỗi thực hiện", ex.getMessage());
                }
            }
        }
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
