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
 * Màn hình Cập nhật khách hàng (KH_CAP_NHAT).
 * Bố cục chuẩn phân hệ nghiệp vụ:
 * - Phía trên: Tiêu đề màn hình và dòng mô tả
 * - Khung nội dung chính: Bên trái là nhãn "Danh sách khách hàng", bên phải là cụm nút Thao tác [Thêm mới] [Cập nhật] [Xóa] [Làm mới]
 * - Ràng buộc nhập liệu: Cho phép họ tên chứa chữ cái và số (nguyen van 1, a2, b3), chặn ký tự đặc biệt (!@#$%^&*...)
 * - Bảng danh sách TableView: Tự động làm mờ dòng ngừng hoạt động và hỗ trợ nhấp đúp chuột để sửa nhanh.
 */
public class CapNhatKhachHangView extends VBox {

    /** Đường dẫn tài nguyên file CSS dùng chung của hệ thống */
    private static final String CSS_CHUNG = "/com/petshop/view/css/chung.css";

    private final KhachHangController khachHangController = new KhachHangController();
    private final ObservableList<KhachHang> danhSachHienThi = FXCollections.observableArrayList();

    // Các nút trên thanh tác vụ (bố trí ở góc phải)
    private final Button btnThemMoi = new Button("Thêm mới");
    private final Button btnCapNhat = new Button("Cập nhật");
    private final Button btnXoaMem = new Button("Xóa");
    private final Button btnLamMoi = new Button("Làm mới");

    // Bảng dữ liệu hiển thị danh sách khách hàng
    private final TableView<KhachHang> tblDanhSach = new TableView<>();

    public CapNhatKhachHangView() {
        setSpacing(14);
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề phân hệ
        VBox pnlTieuDeMan = new VBox(4);
        Label lblTieuDe = new Label("KHÁCH HÀNG - CẬP NHẬT KHÁCH HÀNG");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Bấm Thêm mới để thêm khách hàng; chọn một dòng trong bảng để Cập nhật hoặc Xóa.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDeMan.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Khung bảng danh sách chứa thanh nút tác vụ và TableView
        VBox pnlBang = taoKhungBangDanhSach();
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        getChildren().addAll(pnlTieuDeMan, pnlBang);

        // 3. Khởi tạo trạng thái nút và nạp dữ liệu ban đầu
        capNhatTrangThaiNut(null);
        taiLaiDanhSach();
    }

    /**
     * Tạo thanh tác vụ phía trên bảng:
     * - Bên trái: Nhãn "Danh sách khách hàng"
     * - Khoảng trống giãn cách: Đẩy toàn bộ nút sang bên phải
     * - Bên phải: Cụm nút Thao tác [Thêm mới] [Cập nhật] [Xóa] [Làm mới]
     */
    private HBox taoThanhTacVu() {
        HBox pnlThanhNut = new HBox(10);
        pnlThanhNut.getStyleClass().add("thanh-tac-vu");
        pnlThanhNut.setAlignment(Pos.CENTER_LEFT);
        pnlThanhNut.setMaxWidth(Double.MAX_VALUE);

        // Nhãn tiêu đề danh sách nằm bên trái
        Label lblDanhSach = new Label("Danh sách khách hàng");
        lblDanhSach.getStyleClass().add("tieu-de-bang");

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
        colEmail.setPrefWidth(170);
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

        // --- Cột 7: Trạng thái (Hiển thị Badge xanh / đỏ qua CSS chung) ---
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
        // XỬ LÝ LÀM MỜ DÒNG NGỪNG HOẠT ĐỘNG & HỖ TRỢ NHẤP ĐÚP CHUỘT ĐỂ SỬA NHANH
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
     * Cập nhật trạng thái kích hoạt của nút Cập nhật và Xóa dựa vào dòng đang chọn.
     */
    private void capNhatTrangThaiNut(KhachHang khachHangChon) {
        if (khachHangChon == null) {
            btnCapNhat.setDisable(true);
            btnXoaMem.setDisable(true);
            btnXoaMem.setText("Xóa");
            btnXoaMem.getStyleClass().remove("nut-chinh");
            if (!btnXoaMem.getStyleClass().contains("nut-xoa")) {
                btnXoaMem.getStyleClass().add("nut-xoa");
            }
        } else {
            btnCapNhat.setDisable(false);
            btnXoaMem.setDisable(false);

            // Nếu khách hàng đang hoạt động -> Nút đỏ "Xóa" (xóa mềm)
            // Nếu khách hàng đã ngừng hoạt động -> Nút xanh "Kích hoạt lại"
            if (khachHangChon.isTrangThai()) {
                btnXoaMem.setText("Xóa");
                btnXoaMem.getStyleClass().remove("nut-chinh");
                if (!btnXoaMem.getStyleClass().contains("nut-xoa")) {
                    btnXoaMem.getStyleClass().add("nut-xoa");
                }
            } else {
                btnXoaMem.setText("Kích hoạt lại");
                btnXoaMem.getStyleClass().remove("nut-xoa");
                if (!btnXoaMem.getStyleClass().contains("nut-chinh")) {
                    btnXoaMem.getStyleClass().add("nut-chinh");
                }
            }
        }
    }

    /**
     * Mở hộp thoại popup Thêm mới khách hàng.
     */
    private void moCuaSoThemMoi() {
        Stage dlgThem = new Stage();
        dlgThem.initModality(Modality.APPLICATION_MODAL);
        if (getScene() != null && getScene().getWindow() != null) {
            dlgThem.initOwner(getScene().getWindow());
        }
        dlgThem.setTitle("Thêm khách hàng mới");

        // Nhúng ThemKhachHangView vào popup kèm callback nạp lại bảng sau khi thêm thành công
        ThemKhachHangView pnlThem = new ThemKhachHangView(this::taiLaiDanhSach);
        Scene scene = new Scene(pnlThem, 520, 480);
        ganCssChung(scene);

        dlgThem.setScene(scene);
        dlgThem.setResizable(false);
        dlgThem.showAndWait();
    }

    /**
     * Mở hộp thoại popup Cập nhật thông tin cho khách hàng đang chọn trên bảng.
     */
    private void moCuaSoCapNhat() {
        KhachHang khChon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (khChon == null) {
            hienThongBao(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một khách hàng trong bảng để cập nhật!");
            return;
        }

        Stage dlgCapNhat = new Stage();
        dlgCapNhat.initModality(Modality.APPLICATION_MODAL);
        if (getScene() != null && getScene().getWindow() != null) {
            dlgCapNhat.initOwner(getScene().getWindow());
        }
        dlgCapNhat.setTitle("Cập nhật thông tin khách hàng - " + khChon.getMaKhachHang());

        VBox pnlNoiDung = taoNoiDungCuaSoCapNhat(khChon, dlgCapNhat);
        Scene scene = new Scene(pnlNoiDung, 520, 520);
        ganCssChung(scene);

        dlgCapNhat.setScene(scene);
        dlgCapNhat.setResizable(false);
        dlgCapNhat.showAndWait();
    }

    /**
     * Dựng giao diện form chỉnh sửa thông tin khách hàng trong cửa sổ popup Cập nhật.
     */
    private VBox taoNoiDungCuaSoCapNhat(KhachHang khChon, Stage dlg) {
        VBox pnlGoc = new VBox(14);
        pnlGoc.setPadding(new Insets(18));
        pnlGoc.getStyleClass().add("khung-phan-he");

        // Tiêu đề form cập nhật
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("CẬP NHẬT THÔNG TIN KHÁCH HÀNG");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblDangSua = new Label("Đang chỉnh sửa: " + khChon.getMaKhachHang() + " - " + khChon.getHoTen());
        lblDangSua.getStyleClass().add("nhan-dang-chon");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblDangSua);

        // Khung nhập liệu
        VBox pnlForm = new VBox(14);
        pnlForm.getStyleClass().add("hop-noi-dung");

        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(12);
        pnlLuoi.setVgap(10);

        TextField txtMa = new TextField(khChon.getMaKhachHang());
        txtMa.setDisable(true);
        txtMa.getStyleClass().add("o-nhap-ma-tu-dong");

        TextField txtHoTen = new TextField(khChon.getHoTen());
        TextField txtSdt = new TextField(khChon.getSoDienThoai());
        TextField txtEmail = new TextField(khChon.getEmail() != null ? khChon.getEmail() : "");
        TextField txtDiaChi = new TextField(khChon.getDiaChi() != null ? khChon.getDiaChi() : "");
        TextField txtDiem = new TextField(String.valueOf(khChon.getDiemTichLuy()));
        TextArea txtGhiChu = new TextArea(khChon.getGhiChu() != null ? khChon.getGhiChu() : "");
        txtGhiChu.setPrefRowCount(3);
        txtGhiChu.setWrapText(true);

        ComboBox<String> cboTrangThai = new ComboBox<>(FXCollections.observableArrayList(
                "Đang hoạt động", "Ngừng hoạt động"
        ));
        cboTrangThai.setValue(khChon.isTrangThai() ? "Đang hoạt động" : "Ngừng hoạt động");
        cboTrangThai.setMaxWidth(Double.MAX_VALUE);

        // =====================================================================
        // RÀNG BUỘC HỌ TÊN: Cho phép chữ cái, chữ số, khoảng trắng; chặn ký tự đặc biệt (!@#$%^&*...)
        // =====================================================================
        txtHoTen.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            String locKyTu = newVal.replaceAll("[^\\p{L}0-9\\s]", "");
            if (!locKyTu.equals(newVal)) {
                txtHoTen.setText(locKyTu);
            }
        });

        // =====================================================================
        // RÀNG BUỘC SỐ ĐIỆN THOẠI: Chỉ cho nhập số, tối đa 10 ký tự
        // =====================================================================
        txtSdt.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            String chiSo = newVal.replaceAll("[^0-9]", "");
            if (chiSo.length() > 10) {
                chiSo = chiSo.substring(0, 10);
            }
            if (!chiSo.equals(newVal)) {
                txtSdt.setText(chiSo);
            }
        });

        // Ràng buộc điểm tích lũy: Chỉ cho nhập số nguyên không âm
        txtDiem.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            String chiSo = newVal.replaceAll("[^0-9]", "");
            if (!chiSo.equals(newVal)) {
                txtDiem.setText(chiSo);
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

        pnlLuoi.add(new Label("Điểm tích lũy:"), 0, 4);
        pnlLuoi.add(txtDiem, 1, 4);

        pnlLuoi.add(new Label("Trạng thái:"), 0, 5);
        pnlLuoi.add(cboTrangThai, 1, 5);

        pnlLuoi.add(new Label("Địa chỉ:"), 0, 6);
        pnlLuoi.add(txtDiaChi, 1, 6);

        pnlLuoi.add(new Label("Ghi chú:"), 0, 7);
        pnlLuoi.add(txtGhiChu, 1, 7);

        ColumnConstraints colNhan = new ColumnConstraints();
        colNhan.setMinWidth(140);
        ColumnConstraints colNhap = new ColumnConstraints();
        colNhap.setHgrow(Priority.ALWAYS);
        pnlLuoi.getColumnConstraints().addAll(colNhan, colNhap);

        // Thanh nút bấm Lưu / Hủy (Đẩy sang bên phải)
        HBox pnlNut = new HBox(10);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        Button btnLuu = new Button("Lưu cập nhật");
        btnLuu.getStyleClass().add("nut-chinh");
        btnLuu.setOnAction(e -> {
            String hoTenMoi = txtHoTen.getText();
            String sdtMoi = txtSdt.getText();

            if (hoTenMoi == null || hoTenMoi.trim().isEmpty()) {
                hienThongBao(Alert.AlertType.WARNING, "Cảnh báo", "Họ và tên không được để trống!");
                txtHoTen.requestFocus();
                return;
            }

            if (!hoTenMoi.trim().matches("^[\\p{L}0-9\\s]+$")) {
                hienThongBao(Alert.AlertType.WARNING, "Cảnh báo", "Họ và tên không được chứa ký tự đặc biệt (!@#$%^&*...)!");
                txtHoTen.requestFocus();
                return;
            }

            if (sdtMoi == null || !sdtMoi.trim().matches("^0\\d{9}$")) {
                hienThongBao(Alert.AlertType.WARNING, "Cảnh báo", "Số điện thoại phải gồm đúng 10 chữ số và bắt đầu bằng số 0!");
                txtSdt.requestFocus();
                return;
            }

            int diem = 0;
            try {
                if (!txtDiem.getText().trim().isEmpty()) {
                    diem = Integer.parseInt(txtDiem.getText().trim());
                }
            } catch (NumberFormatException ignored) {}

            // Cập nhật thông tin vào đối tượng
            khChon.setHoTen(hoTenMoi.trim());
            khChon.setSoDienThoai(sdtMoi.trim());
            khChon.setEmail(txtEmail.getText());
            khChon.setDiemTichLuy(diem);
            khChon.setDiaChi(txtDiaChi.getText());
            khChon.setGhiChu(txtGhiChu.getText());
            khChon.setTrangThai("Đang hoạt động".equals(cboTrangThai.getValue()));

            try {
                boolean kq = khachHangController.capNhatKhachHang(khChon);
                if (kq) {
                    hienThongBao(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật thông tin khách hàng thành công!");
                    dlg.close();
                    taiLaiDanhSach();
                } else {
                    hienThongBao(Alert.AlertType.ERROR, "Thất bại", "Không thể lưu cập nhật vào cơ sở dữ liệu!");
                }
            } catch (IllegalArgumentException | SecurityException ex) {
                hienThongBao(Alert.AlertType.ERROR, "Lỗi nghiệp vụ", ex.getMessage());
            } catch (Exception ex) {
                hienThongBao(Alert.AlertType.ERROR, "Lỗi hệ thống", ex.getMessage());
            }
        });

        Button btnHuy = new Button("Hủy bỏ");
        btnHuy.getStyleClass().add("nut-phu");
        btnHuy.setOnAction(e -> dlg.close());

        pnlNut.getChildren().addAll(btnLuu, btnHuy);
        pnlForm.getChildren().addAll(pnlLuoi, pnlNut);

        pnlGoc.getChildren().addAll(pnlTieuDe, pnlForm);
        return pnlGoc;
    }

    /**
     * Xử lý chuyển trạng thái khách hàng sang Ngừng hoạt động (xóa mềm) hoặc Kích hoạt lại.
     */
    private void xuLyXoaMemHoacKichHoat() {
        KhachHang khChon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (khChon == null) {
            hienThongBao(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một khách hàng cần thao tác!");
            return;
        }

        if (khChon.isTrangThai()) {
            // Đang hoạt động -> Hỏi xác nhận ngưng hoạt động
            Alert xacNhan = new Alert(Alert.AlertType.CONFIRMATION);
            xacNhan.setTitle("Xác nhận xóa");
            xacNhan.setHeaderText("Ngưng hoạt động khách hàng: " + khChon.getHoTen() + " (" + khChon.getMaKhachHang() + ")?");
            xacNhan.setContentText("Khách hàng này sẽ được chuyển sang trạng thái 'Ngừng hoạt động' (xóa mềm) và dòng hiển thị sẽ được làm mờ.");

            Optional<ButtonType> kq = xacNhan.showAndWait();
            if (kq.isPresent() && kq.get() == ButtonType.OK) {
                try {
                    boolean thanhCong = khachHangController.xoaMemKhachHang(khChon.getMaKhachHang());
                    if (thanhCong) {
                        hienThongBao(Alert.AlertType.INFORMATION, "Thành công", "Đã ngưng hoạt động khách hàng thành công!");
                        taiLaiDanhSach();
                    } else {
                        hienThongBao(Alert.AlertType.ERROR, "Thất bại", "Không thể cập nhật trạng thái khách hàng!");
                    }
                } catch (Exception ex) {
                    hienThongBao(Alert.AlertType.ERROR, "Lỗi", ex.getMessage());
                }
            }
        } else {
            // Đang ngừng hoạt động -> Hỏi xác nhận kích hoạt lại
            Alert xacNhan = new Alert(Alert.AlertType.CONFIRMATION);
            xacNhan.setTitle("Xác nhận kích hoạt lại");
            xacNhan.setHeaderText("Kích hoạt lại khách hàng: " + khChon.getHoTen() + " (" + khChon.getMaKhachHang() + ")?");
            xacNhan.setContentText("Khách hàng sẽ được mở lại trạng thái 'Đang hoạt động' để tiếp tục giao dịch.");

            Optional<ButtonType> kq = xacNhan.showAndWait();
            if (kq.isPresent() && kq.get() == ButtonType.OK) {
                try {
                    boolean thanhCong = khachHangController.kichHoatLaiKhachHang(khChon.getMaKhachHang());
                    if (thanhCong) {
                        hienThongBao(Alert.AlertType.INFORMATION, "Thành công", "Đã kích hoạt lại khách hàng thành công!");
                        taiLaiDanhSach();
                    } else {
                        hienThongBao(Alert.AlertType.ERROR, "Thất bại", "Không thể kích hoạt lại khách hàng!");
                    }
                } catch (Exception ex) {
                    hienThongBao(Alert.AlertType.ERROR, "Lỗi", ex.getMessage());
                }
            }
        }
    }

    /**
     * Nạp lại toàn bộ danh sách khách hàng từ cơ sở dữ liệu vào bảng hiển thị.
     */
    public void taiLaiDanhSach() {
        try {
            List<KhachHang> ds = khachHangController.layDanhSachKhachHang();
            danhSachHienThi.setAll(ds);
            tblDanhSach.refresh();
        } catch (Exception e) {
            hienThongBao(Alert.AlertType.ERROR, "Lỗi nạp dữ liệu", "Không thể tải danh sách khách hàng: " + e.getMessage());
        }
    }

    /**
     * Gắn file stylesheet CSS chung cho Scene của cửa sổ con.
     */
    private void ganCssChung(Scene scene) {
        if (scene != null && getClass().getResource(CSS_CHUNG) != null) {
            scene.getStylesheets().add(getClass().getResource(CSS_CHUNG).toExternalForm());
        }
    }

    /**
     * Tiện ích hiển thị hộp thoại thông báo.
     */
    private void hienThongBao(Alert.AlertType loai, String tieuDe, String noiDung) {
        Alert alert = new Alert(loai);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}
