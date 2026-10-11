package com.petshop.view;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public final class ConfirmDialog {
    private ConfirmDialog() {
    }

    public static boolean confirmDeactivate() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận ngừng");
        alert.setHeaderText("Bạn có chắc muốn ngừng mục đã chọn không?");
        alert.setContentText("Mục đã ngừng có thể được kích hoạt lại.");
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    public static boolean confirmCancelForm() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận hủy");
        alert.setHeaderText("Đóng biểu mẫu và bỏ các thay đổi chưa lưu?");
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        return alert.showAndWait().filter(ButtonType.YES::equals).isPresent();
    }

    public static boolean confirmCancelAppointment() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận hủy lịch hẹn");
        alert.setHeaderText("Chuyển lịch hẹn sang trạng thái Đã hủy?");
        alert.setContentText("Lịch hẹn sẽ được giữ trong hệ thống, không bị xóa.");
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }
}
