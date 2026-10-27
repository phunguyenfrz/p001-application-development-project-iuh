package com.petshop.view.khachhang;

import com.petshop.controller.KhachHangController;
import com.petshop.model.KhachHang;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Màn hình phụ (Popup / Modal Dialog) dùng để thêm mới khách hàng vào hệ thống.
 * Cung cấp form nhập liệu có ràng buộc:
 * - Họ tên: Cho phép chữ cái, chữ số (ví dụ: nguyen van 1, a2, b3), chặn ký tự đặc biệt (!@#$%^&*...)
 * - Số điện thoại: Chỉ cho phép nhập đúng 10 chữ số bắt đầu bằng 0.
 */
public class ThemKhachHangView extends VBox {

    private final KhachHangController khachHangController = new KhachHangController();
    private final Runnable khiThemThanhCong;

    // Các trường nhập liệu trên form
    private final TextField txtMaKhachHang = new TextField();
    private final TextField txtHoTen = new TextField();
    private final TextField txtSoDienThoai = new TextField();
    private final TextField txtEmail = new TextField();
    private final TextField txtDiaChi = new TextField();
    private final TextArea txtGhiChu = new TextArea();

    // Các nút bấm điều khiển
    private final Button btnLuu = new Button("Lưu thêm mới");
    private final Button btnLamMoi = new Button("Làm mới");
    private final Button btnDong = new Button("Đóng");

    public ThemKhachHangView() {
        this(null);
    }

    public ThemKhachHangView(Runnable khiThemThanhCong) {
        this.khiThemThanhCong = khiThemThanhCong;

        setSpacing(14);
        setPadding(new Insets(18));
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề màn hình
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("THÊM KHÁCH HÀNG MỚI");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Nhập đầy đủ thông tin khách hàng; họ tên (không chứa ký tự đặc biệt) và số điện thoại (10 số) là bắt buộc.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Khung form nhập liệu
        VBox pnlForm = taoKhungNhapLieu();

        getChildren().addAll(pnlTieuDe, pnlForm);

        // 3. Khởi tạo mã tự động và làm sạch form
        lamMoiForm();
    }

    /**
     * Khởi tạo giao diện lưới (GridPane) chứa các nhãn và ô nhập liệu thông tin khách hàng.
     */
    private VBox taoKhungNhapLieu() {
        VBox pnlKhung = new VBox(14);
        pnlKhung.getStyleClass().add("hop-noi-dung");

        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(12);
        pnlLuoi.setVgap(10);

        // Mã khách hàng sinh tự động, chỉ đọc
        txtMaKhachHang.setDisable(true);
        txtMaKhachHang.getStyleClass().add("o-nhap-ma-tu-dong");

        // Placeholder hướng dẫn người dùng
        txtHoTen.setPromptText("Nhập họ và tên đầy đủ (VD: Nguyễn Văn An, nguyen van 1)");
        txtSoDienThoai.setPromptText("Nhập 10 chữ số bắt đầu bằng 0 (VD: 0912345678)");
        txtEmail.setPromptText("Địa chỉ email (VD: an.nguyen@gmail.com)");
        txtDiaChi.setPromptText("Số nhà, tên đường, phường/xã, quận/huyện...");

        txtGhiChu.setPromptText("Ghi chú về khách hàng (sở thích, lưu ý chăm sóc...)");
        txtGhiChu.setPrefRowCount(3);
        txtGhiChu.setWrapText(true);

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
        // RÀNG BUỘC SỐ ĐIỆN THOẠI: Chỉ cho phép nhập ký tự số và tối đa 10 chữ số
        // =====================================================================
        txtSoDienThoai.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            String chiSo = newVal.replaceAll("[^0-9]", "");
            if (chiSo.length() > 10) {
                chiSo = chiSo.substring(0, 10);
            }
            if (!chiSo.equals(newVal)) {
                txtSoDienThoai.setText(chiSo);
            }
        });

        // Thêm các thành phần vào lưới GridPane
        pnlLuoi.add(new Label("Mã khách hàng:"), 0, 0);
        pnlLuoi.add(txtMaKhachHang, 1, 0);

        pnlLuoi.add(new Label("Họ và tên (*):"), 0, 1);
        pnlLuoi.add(txtHoTen, 1, 1);

        pnlLuoi.add(new Label("Số điện thoại (*):"), 0, 2);
        pnlLuoi.add(txtSoDienThoai, 1, 2);

        pnlLuoi.add(new Label("Email:"), 0, 3);
        pnlLuoi.add(txtEmail, 1, 3);

        pnlLuoi.add(new Label("Địa chỉ:"), 0, 4);
        pnlLuoi.add(txtDiaChi, 1, 4);

        pnlLuoi.add(new Label("Ghi chú:"), 0, 5);
        pnlLuoi.add(txtGhiChu, 1, 5);

        // Thiết lập độ rộng cột nhãn và cột ô nhập
        ColumnConstraints colNhan = new ColumnConstraints();
        colNhan.setMinWidth(140);
        ColumnConstraints colNhap = new ColumnConstraints();
        colNhap.setHgrow(Priority.ALWAYS);
        pnlLuoi.getColumnConstraints().addAll(colNhan, colNhap);

        // Thanh nút chức năng (Đẩy sang bên phải)
        HBox pnlNut = new HBox(10);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        btnLuu.getStyleClass().add("nut-chinh");
        btnLuu.setOnAction(e -> xuLyLuuKhachHang());

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> lamMoiForm());

        btnDong.getStyleClass().add("nut-phu");
        btnDong.setOnAction(e -> dongCuaSo());

        pnlNut.getChildren().addAll(btnLuu, btnLamMoi, btnDong);

        pnlKhung.getChildren().addAll(pnlLuoi, pnlNut);
        return pnlKhung;
    }

    /**
     * Xử lý kiểm tra dữ liệu và lưu khách hàng mới thông qua Controller.
     */
    private void xuLyLuuKhachHang() {
        String hoTen = txtHoTen.getText();
        String sdt = txtSoDienThoai.getText();
        String email = txtEmail.getText();
        String diaChi = txtDiaChi.getText();
        String ghiChu = txtGhiChu.getText();

        // 1. Kiểm tra nhanh tại View
        if (hoTen == null || hoTen.trim().isEmpty()) {
            hienThongBao(Alert.AlertType.WARNING, "Cảnh báo nhập liệu", "Họ và tên khách hàng không được để trống!");
            txtHoTen.requestFocus();
            return;
        }

        if (!hoTen.trim().matches("^[\\p{L}0-9\\s]+$")) {
            hienThongBao(Alert.AlertType.WARNING, "Cảnh báo nhập liệu", "Họ và tên khách hàng không được chứa ký tự đặc biệt (!@#$%^&*...)!");
            txtHoTen.requestFocus();
            return;
        }

        if (sdt == null || sdt.trim().isEmpty()) {
            hienThongBao(Alert.AlertType.WARNING, "Cảnh báo nhập liệu", "Số điện thoại không được để trống!");
            txtSoDienThoai.requestFocus();
            return;
        }

        if (!sdt.trim().matches("^0\\d{9}$")) {
            hienThongBao(Alert.AlertType.WARNING, "Cảnh báo nhập liệu", "Số điện thoại phải gồm đúng 10 chữ số và bắt đầu bằng số 0!");
            txtSoDienThoai.requestFocus();
            return;
        }

        // 2. Đóng gói đối tượng KhachHang
        KhachHang kh = new KhachHang();
        kh.setMaKhachHang(txtMaKhachHang.getText());
        kh.setHoTen(hoTen.trim());
        kh.setSoDienThoai(sdt.trim());
        kh.setEmail(email);
        kh.setDiaChi(diaChi);
        kh.setGhiChu(ghiChu);
        kh.setDiemTichLuy(0);
        kh.setTrangThai(true);

        // 3. Gọi Controller để lưu vào cơ sở dữ liệu
        try {
            boolean thanhCong = khachHangController.themKhachHang(kh);
            if (thanhCong) {
                hienThongBao(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm mới khách hàng: " + kh.getHoTen() + " (" + kh.getMaKhachHang() + ")");
                if (khiThemThanhCong != null) {
                    khiThemThanhCong.run();
                }
                dongCuaSo();
            } else {
                hienThongBao(Alert.AlertType.ERROR, "Thất bại", "Không thể thêm khách hàng vào cơ sở dữ liệu!");
            }
        } catch (IllegalArgumentException | SecurityException ex) {
            hienThongBao(Alert.AlertType.ERROR, "Lỗi nghiệp vụ", ex.getMessage());
        } catch (Exception ex) {
            hienThongBao(Alert.AlertType.ERROR, "Lỗi hệ thống", "Đã xảy ra lỗi: " + ex.getMessage());
        }
    }

    /**
     * Làm mới form và tự động lấy mã khách hàng mới tiếp theo từ cơ sở dữ liệu.
     */
    public void lamMoiForm() {
        txtMaKhachHang.setText(khachHangController.taoMaKhachHangMoi());
        txtHoTen.clear();
        txtSoDienThoai.clear();
        txtEmail.clear();
        txtDiaChi.clear();
        txtGhiChu.clear();
        txtHoTen.requestFocus();
    }

    /**
     * Đóng cửa sổ Stage chứa màn hình này (nếu có).
     */
    private void dongCuaSo() {
        if (getScene() != null && getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
    }

    /**
     * Tiện ích hiển thị hộp thoại thông báo Alert.
     */
    private void hienThongBao(Alert.AlertType loai, String tieuDe, String noiDung) {
        Alert alert = new Alert(loai);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}
