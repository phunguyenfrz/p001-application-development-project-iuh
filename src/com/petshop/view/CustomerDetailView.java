package com.petshop.view;

import com.petshop.controller.CustomerController;
import com.petshop.controller.PetController;
import com.petshop.model.Customer;
import com.petshop.model.Pet;
import com.petshop.model.Permission;
import com.petshop.model.Role;
import com.petshop.model.Screen;
import com.petshop.navigation.ScreenRegistry;
import com.petshop.util.SessionManager;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Objects;

public final class CustomerDetailView {
    private CustomerDetailView() {
    }

    public static void show(
            Customer customer, CustomerController customers, PetController pets) {
        Objects.requireNonNull(customer);
        ListView<Pet> petList = new ListView<>();
        petList.setCellFactory(ignored -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Pet pet, boolean empty) {
                super.updateItem(pet, empty);
                setText(empty || pet == null ? null : pet.getTenThuCung()
                        + " · " + pet.getLoaiThuCung() + " · "
                        + pet.getCanNang() + " kg");
            }
        });
        petList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && petList.getSelectionModel().getSelectedItem() != null) {
                PetDetailView.show(petList.getSelectionModel().getSelectedItem(), pets);
            }
        });
        petList.setPlaceholder(new Label("Khách hàng chưa có hồ sơ thú cưng."));
        Button addPet = new Button("Thêm thú cưng");
        Role role = SessionManager.getCurrentUser() == null
                ? null : SessionManager.getCurrentUser().getRole();
        boolean canCreatePet = ScreenRegistry.hasPermission(
                Screen.PETS, role, Permission.CREATE);
        addPet.setVisible(canCreatePet);
        addPet.setManaged(canCreatePet);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Chi tiết khách hàng");
        dialog.setHeaderText(customer.getHoTen());
        Button close = new Button("Đóng");
        close.setOnAction(event -> dialog.close());
        addPet.setOnAction(event -> {
            PetFormDialog.show(pets, customers, null, customer.getMaKhachHang());
            loadPets(customer, pets, petList);
        });
        VBox details = new VBox(8,
                new Label("Mã khách hàng: " + value(customer.getMaKhachHang())),
                new Label("Số điện thoại: " + value(customer.getSoDienThoai())),
                new Label("Email: " + value(customer.getEmail())),
                new Label("Điểm tích lũy: " + customer.getDiemTichLuy()),
                new Label("Địa chỉ: " + value(customer.getDiaChi())),
                new Label("Ghi chú: " + value(customer.getGhiChu())),
                new Label("Thú cưng"), petList,
                new HBox(8, addPet, close));
        dialog.getDialogPane().setContent(details);
        dialog.getDialogPane().setPrefWidth(560);
        loadPets(customer, pets, petList);
        dialog.show();
    }

    private static void loadPets(Customer customer, PetController pets, ListView<Pet> petList) {
        Task<List<Pet>> task = new Task<>() {
            @Override
            protected List<Pet> call() {
                return pets.findByCustomer(customer.getMaKhachHang());
            }
        };
        task.setOnSucceeded(event -> petList.getItems().setAll(task.getValue()));
        task.setOnFailed(event -> petList.setPlaceholder(new Label(
                task.getException() == null ? "Không thể tải hồ sơ thú cưng."
                        : task.getException().getMessage())));
        Thread worker = new Thread(task, "customer-pet-list");
        worker.setDaemon(true);
        worker.start();
    }

    private static String value(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }
}
