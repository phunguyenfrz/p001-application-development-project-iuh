package com.petshop.view;

import com.petshop.controller.DoiMatKhauController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Hộp thoại Đổi mật khẩu tài khoản (DoiMatKhauView).
 * Dạng Dialog độc lập, quy ước tiền tố: lbl, pwd, btn.
 */
public class DoiMatKhauView extends Dialog<Boolean> {

    private final Label lblTieuDe = new Label("ĐỔI MẬT KHẨU TÀI KHOẢN");
    private final Label lblMoTa = new Label("Nhập mật khẩu hiện tại và mật khẩu mới để cập nhật.");

    private final Label lblMatKhauCu = new Label("Mật khẩu hiện tại (*):");
    private final Label lblMatKhauMoi = new Label("Mật khẩu mới (*):");
    private final Label lblXacNhanMatKhau = new Label("Xác nhận mật khẩu (*):");
    private final Label lblThongBaoLoi = new Label();

    private final PasswordField pwdMatKhauCu = new PasswordField();
    private final PasswordField pwdMatKhauMoi = new PasswordField();
    private final PasswordField pwdXacNhanMatKhau = new PasswordField();

    private final Button btnLuu = new Button("Lưu thay đổi");
    private final Button btnHuy = new Button("Hủy bỏ");

    private final DoiMatKhauController doiMatKhauController = new DoiMatKhauController();

    public DoiMatKhauView() {
        setTitle("Happy Tails - Đổi mật khẩu");
        setHeaderText(null);

        // Nút trong DialogPane
        DialogPane dialogPane = getDialogPane();
        try {
            var cssStream = getClass().getResource("/com/petshop/view/css/chung.css");
            if (cssStream != null) {
                dialogPane.getStylesheets().add(cssStream.toExternalForm());
            }
        } catch (Exception ignored) {}

        dialogPane.getButtonTypes().addAll(ButtonType.CLOSE);
        // Ẩn thanh button mặc định để dùng nút tùy chỉnh
        Node nutDongMacDinh = dialogPane.lookupButton(ButtonType.CLOSE);
        if (nutDongMacDinh != null) {
            nutDongMacDinh.setVisible(false);
            nutDongMacDinh.setManaged(false);
        }

        // Bố cục chính
        VBox pnlChinh = new VBox(16);
        pnlChinh.setPadding(new Insets(24, 28, 20, 28));
        pnlChinh.setPrefWidth(420);
        pnlChinh.setStyle("-fx-background-color: #ffffff;");

        // Header
        VBox pnlHeader = new VBox(4);
        lblTieuDe.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");
        lblMoTa.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #667085;");
        pnlHeader.getChildren().addAll(lblTieuDe, lblMoTa);

        // Form nhập dữ liệu
        GridPane pnlForm = new GridPane();
        pnlForm.setHgap(10);
        pnlForm.setVgap(12);

        pwdMatKhauCu.setPromptText("Nhập mật khẩu hiện tại...");
        pwdMatKhauCu.setPrefHeight(36);

        pwdMatKhauMoi.setPromptText("Nhập mật khẩu mới (tối thiểu 6 ký tự)...");
        pwdMatKhauMoi.setPrefHeight(36);

        pwdXacNhanMatKhau.setPromptText("Nhập lại mật khẩu mới...");
        pwdXacNhanMatKhau.setPrefHeight(36);

        pnlForm.add(lblMatKhauCu, 0, 0);
        pnlForm.add(pwdMatKhauCu, 0, 1);

        pnlForm.add(lblMatKhauMoi, 0, 2);
        pnlForm.add(pwdMatKhauMoi, 0, 3);

        pnlForm.add(lblXacNhanMatKhau, 0, 4);
        pnlForm.add(pwdXacNhanMatKhau, 0, 5);

        // Thông báo lỗi
        lblThongBaoLoi.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px;");
        lblThongBaoLoi.setWrapText(true);
        lblThongBaoLoi.setVisible(false);

        // Thanh nút bấm
        HBox pnlNut = new HBox(10);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);
        pnlNut.setPadding(new Insets(8, 0, 0, 0));

        btnHuy.getStyleClass().add("nut-phu");
        btnHuy.setPrefHeight(36);
        btnHuy.setOnAction(e -> {
            setResult(false);
            close();
        });

        btnLuu.getStyleClass().add("nut-chinh");
        btnLuu.setPrefHeight(36);
        btnLuu.setOnAction(e -> xuLyDoiMatKhau());

        pnlNut.getChildren().addAll(btnHuy, btnLuu);

        pnlChinh.getChildren().addAll(pnlHeader, pnlForm, lblThongBaoLoi, pnlNut);
        dialogPane.setContent(pnlChinh);

        // Phím Enter ở các ô mật khẩu để lưu
        pwdMatKhauCu.setOnAction(e -> pwdMatKhauMoi.requestFocus());
        pwdMatKhauMoi.setOnAction(e -> pwdXacNhanMatKhau.requestFocus());
        pwdXacNhanMatKhau.setOnAction(e -> xuLyDoiMatKhau());
    }

    private void xuLyDoiMatKhau() {
        lblThongBaoLoi.setVisible(false);
        String matKhauCu = pwdMatKhauCu.getText();
        String matKhauMoi = pwdMatKhauMoi.getText();
        String xacNhan = pwdXacNhanMatKhau.getText();

        try {
            boolean thanhCong = doiMatKhauController.doiMatKhau(matKhauCu, matKhauMoi, xacNhan);
            if (thanhCong) {
                Alert thongBao = new Alert(Alert.AlertType.INFORMATION);
                thongBao.setTitle("Thông báo");
                thongBao.setHeaderText(null);
                thongBao.setContentText("Đổi mật khẩu thành công!");
                thongBao.showAndWait();

                setResult(true);
                close();
            }
        } catch (IllegalArgumentException | SecurityException ex) {
            hienThiLoi(ex.getMessage());
        } catch (IllegalStateException ex) {
            hienThiLoi("Lỗi cơ sở dữ liệu: " + ex.getMessage());
        } catch (Exception ex) {
            hienThiLoi("Đã xảy ra lỗi không xác định: " + ex.getMessage());
        }
    }

    private void hienThiLoi(String noiDung) {
        lblThongBaoLoi.setText(noiDung);
        lblThongBaoLoi.setVisible(true);
    }
}
