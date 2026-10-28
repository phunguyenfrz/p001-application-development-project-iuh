package com.petshop.view.khachhang;

import com.petshop.controller.KhachHangController;
import com.petshop.model.KhachHang;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Hộp thoại (Modal Dialog) Thêm mới khách hàng vào hệ thống.
 * Ràng buộc dữ liệu:
 * - Họ tên: Cho phép chữ cái (kể cả tiếng Việt), chữ số và khoảng trắng (VD: nguyen van 1, a2);
 *   chặn hoàn toàn ký tự đặc biệt (!@#$%^&*...).
 * - Số điện thoại: Ràng buộc nhập số thời gian thực, đúng 10 chữ số và bắt đầu bằng số 0.
 * - Mã khách hàng: Tự động sinh theo mẫu chuẩn KH-yyyyMMdd-xxxxxx và ở chế độ chỉ đọc.
 */
public class ThemKhachHangView extends VBox {

    private static final String CSS_CHUNG = "/com/petshop/view/css/chung.css";

    private final KhachHangController khachHangController = new KhachHangController();
    private final Runnable callbackSauKhiLuu;

    // Các trường nhập liệu
    private final TextField txtMaKhachHang = new TextField();
    private final TextField txtHoTen = new TextField();
    private final TextField txtSoDienThoai = new TextField();
    private final TextField txtEmail = new TextField();
    private final TextField txtDiaChi = new TextField();
    private final TextArea txtGhiChu = new TextArea();

    // Các nút chức năng trong form
    private final Button btnLuu = new Button("Lưu khách hàng");
    private final Button btnLamMoi = new Button("Làm mới");
    private final Button btnDong = new Button("Đóng");

    public ThemKhachHangView(Runnable callbackSauKhiLuu) {
        this.callbackSauKhiLuu = callbackSauKhiLuu;

        setSpacing(14);
        setPadding(new Insets(18));
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề hộp thoại
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("THÊM MỚI KHÁCH HÀNG");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        Label lblMoTa = new Label("Nhập đầy đủ thông tin khách hàng. Các trường đánh dấu (*) là bắt buộc.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Khung form nhập liệu
        VBox pnlForm = taoKhungNhapLieu();

        // 3. Thanh nút bấm
        HBox pnlNut = taoThanhNutBam();

        getChildren().addAll(pnlTieuDe, pnlForm, pnlNut);

        // Khởi tạo mã tự động ban đầu
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
        txtMaKhachHang.setEditable(false);

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
            if (!newVal.matches("\\d*")) {
                txtSoDienThoai.setText(newVal.replaceAll("[^\\d]", ""));
            } else if (newVal.length() > 10) {
                txtSoDienThoai.setText(newVal.substring(0, 10));
            }
        });

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

        ColumnConstraints colNhan = new ColumnConstraints(120);
        ColumnConstraints colNhap = new ColumnConstraints();
        colNhap.setHgrow(Priority.ALWAYS);
        pnlLuoi.getColumnConstraints().addAll(colNhan, colNhap);

        pnlKhung.getChildren().add(pnlLuoi);
        return pnlKhung;
    }

    /**
     * Tạo thanh nút chức năng (Lưu, Làm mới, Đóng) ở chân hộp thoại.
     */
    private HBox taoThanhNutBam() {
        HBox pnlNut = new HBox(10);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        btnLuu.getStyleClass().add("nut-chinh");
        btnLuu.setOnAction(e -> xuLyLuuKhachHang());

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> lamMoiForm());

        btnDong.getStyleClass().add("nut-phu");
        btnDong.setOnAction(e -> dongCuaSo());

        pnlNut.getChildren().addAll(btnLuu, btnLamMoi, btnDong);
        return pnlNut;
    }

    /**
     * Xử lý lưu khách hàng mới vào cơ sở dữ liệu.
     */
    private void xuLyLuuKhachHang() {
        try {
            String hoTen = txtHoTen.getText();
            String sdt = txtSoDienThoai.getText();
            String email = txtEmail.getText();
            String diaChi = txtDiaChi.getText();
            String ghiChu = txtGhiChu.getText();

            KhachHang khMoi = new KhachHang();
            khMoi.setMaKhachHang(txtMaKhachHang.getText());
            khMoi.setHoTen(hoTen);
            khMoi.setSoDienThoai(sdt);
            khMoi.setEmail(email);
            khMoi.setDiaChi(diaChi);
            khMoi.setGhiChu(ghiChu);
            khMoi.setDiemTichLuy(0);
            khMoi.setTrangThai(true);

            boolean thanhCong = khachHangController.themKhachHang(khMoi);
            if (thanhCong) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thành công");
                alert.setHeaderText(null);
                alert.setContentText("Đã thêm mới khách hàng '" + khMoi.getHoTen() + "' thành công!");
                alert.showAndWait();

                if (callbackSauKhiLuu != null) {
                    callbackSauKhiLuu.run();
                }
                dongCuaSo();
            }
        } catch (IllegalArgumentException ex) {
            hienThiCanhBao("Dữ liệu không hợp lệ", ex.getMessage());
        } catch (Exception ex) {
            hienThiLoi("Lỗi hệ thống", ex.getMessage());
        }
    }

    /**
     * Xóa trắng các trường nhập liệu và tự động sinh mã khách hàng mới.
     */
    private void lamMoiForm() {
        try {
            txtMaKhachHang.setText(khachHangController.taoMaKhachHangMoi());
        } catch (Exception e) {
            txtMaKhachHang.setText("KH-" + System.currentTimeMillis());
        }
        txtHoTen.clear();
        txtSoDienThoai.clear();
        txtEmail.clear();
        txtDiaChi.clear();
        txtGhiChu.clear();
        txtHoTen.requestFocus();
    }

    /**
     * Đóng cửa sổ Stage chứa form này.
     */
    private void dongCuaSo() {
        if (getScene() != null && getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
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

    /**
     * Phương thức tĩnh tiện ích mở nhanh hộp thoại Thêm mới khách hàng dạng modal dialog.
     */
    public static void hienThi(Window cuaSoCha, Runnable callbackSauKhiLuu) {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        if (cuaSoCha != null) {
            dlg.initOwner(cuaSoCha);
        }
        dlg.setTitle("Thêm mới khách hàng - Happy Tails");

        ThemKhachHangView pnlNoiDung = new ThemKhachHangView(callbackSauKhiLuu);
        Scene scene = new Scene(pnlNoiDung, 520, 480);

        if (ThemKhachHangView.class.getResource(CSS_CHUNG) != null) {
            scene.getStylesheets().add(ThemKhachHangView.class.getResource(CSS_CHUNG).toExternalForm());
        }

        dlg.setScene(scene);
        dlg.setResizable(false);
        dlg.showAndWait();
    }
}
