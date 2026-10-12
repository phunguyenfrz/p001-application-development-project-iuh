package com.petshop.view;

import com.petshop.controller.PetController;
import com.petshop.model.Pet;
import javafx.concurrent.Task;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.util.Objects;

public final class PetDetailView {
    private PetDetailView() {
    }

    public static void show(Pet pet, PetController controller) {
        Objects.requireNonNull(pet);
        Objects.requireNonNull(controller);
        Label owner = new Label("Chủ nuôi: " + value(pet.getTenChu())
                + " · " + value(pet.getDienThoaiChu()));
        Label identity = new Label("Đặc điểm nhận dạng: " + value(pet.getDacDiemNhanDang()));
        Label allergies = new Label("Dị ứng: " + value(pet.getDiUng()));
        Label conditions = new Label("Bệnh nền: " + value(pet.getBenhNen()));
        Label personality = new Label("Tính cách: " + value(pet.getTinhCach()));
        ListView<String> history = new ListView<>();
        history.setPlaceholder(new Label("Chưa có lịch sử chăm sóc."));
        VBox content = new VBox(8,
                new Label("Mã thú cưng: " + value(pet.getMaThuCung())),
                new Label("Tên: " + value(pet.getTenThuCung())),
                new Label("Loài / Giống: " + value(pet.getLoaiThuCung())
                        + " / " + value(pet.getGiongLoai())),
                new Label("Giới tính: " + value(pet.getGioiTinh())),
                new Label("Ngày sinh: " + value(pet.getNgaySinh())),
                new Label("Cân nặng: " + pet.getCanNang() + " kg"),
                owner, identity, allergies, conditions, personality,
                new Label("Lịch sử chăm sóc"), history);
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Chi tiết hồ sơ thú cưng");
        dialog.setHeaderText(pet.getTenThuCung());
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
        dialog.getDialogPane().setPrefWidth(560);

        Task<java.util.List<String>> task = new Task<>() {
            @Override
            protected java.util.List<String> call() {
                return controller.findCareHistory(pet.getMaThuCung());
            }
        };
        task.setOnSucceeded(event -> history.getItems().setAll(task.getValue()));
        task.setOnFailed(event -> history.setPlaceholder(new Label(
                task.getException() == null ? "Không thể tải lịch sử chăm sóc."
                        : task.getException().getMessage())));
        Thread worker = new Thread(task, "pet-care-history");
        worker.setDaemon(true);
        worker.start();
        dialog.show();
    }

    private static String value(Object value) {
        return value == null ? "—" : value.toString();
    }
}
