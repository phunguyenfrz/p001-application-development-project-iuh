package com.petshop.controller;

import com.petshop.model.TaiKhoan;
import com.petshop.service.XacThucService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller điều khiển luồng nghiệp vụ đăng nhập của hệ thống.
 */
public class DangNhapController {
    private final XacThucService xacThucService;

    // === BỔ SUNG 1: Khai báo các thành phần giao diện từ file FXML (@FXML) ===
    @FXML
    private TextField txtTenDangNhap;

    @FXML
    private PasswordField txtMatKhau;

    public DangNhapController() {
        this.xacThucService = new XacThucService();
    }

    public DangNhapController(XacThucService xacThucService) {
        this.xacThucService = xacThucService;
    }

    /**
     * Xác thực thông tin tài khoản và mật khẩu người dùng.
     */
    public TaiKhoan xacThuc(String tenDangNhap, String matKhau) {
        return xacThucService.dangNhap(tenDangNhap, matKhau);
    }

    // === BỔ SUNG 2: Hàm xử lý sự kiện khi người dùng bấm nút Đăng nhập trên giao diện ===
    @FXML
    private void handleDangNhap() {
        String tenDangNhap = txtTenDangNhap.getText();
        String matKhau = txtMatKhau.getText();

        try {
            // Gọi hàm xác thực sẵn có của bro
            TaiKhoan taiKhoanHienTai = xacThuc(tenDangNhap, matKhau);

            if (taiKhoanHienTai != null) {
                // Đăng nhập thành công -> Chuyển sang màn hình chính
                chuyenSangManHinhChinh();
            }
        } catch (IllegalArgumentException e) {
            // Bắt lỗi do bỏ trống ô nhập hoặc sai thông tin từ Service ném lên
            hienThiCanhBao("Lỗi đăng nhập", e.getMessage());
        } catch (IllegalStateException e) {
            // Bắt lỗi hệ thống / kết nối CSDL
            hienThiCanhBao("Lỗi hệ thống", e.getMessage());
        }
    }

    // === BỔ SUNG 3: Hàm xử lý nút Thoát (nếu trên giao diện có nút thoát) ===
    @FXML
    private void handleThoat() {
        System.exit(0);
    }

    // === BỔ SUNG 4: Hàm chuyển hướng sang ManHinhChinhView khi thành công ===
    private void chuyenSangManHinhChinh() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/petshop/view/ManHinhChinhView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) txtTenDangNhap.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Cửa hàng thú cưng Happy Tails - Trang chủ");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            hienThiCanhBao("Lỗi giao diện", "Không thể mở màn hình chính: " + e.getMessage());
        }
    }

    // === BỔ SUNG 5: Hàm hỗ trợ hiển thị bảng thông báo (Alert) ===
    private void hienThiCanhBao(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}