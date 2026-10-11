package com.petshop.view;

import com.petshop.controller.DashboardController;
import com.petshop.controller.ScreenNavigator;
import com.petshop.model.DashboardData.AllergyPet;
import com.petshop.model.DashboardData.Appointment;
import com.petshop.model.DashboardData.CareDashboard;
import com.petshop.model.User;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public final class CareDashboardView extends VBox implements DashboardView {
    public CareDashboardView(
            User user, ScreenNavigator navigator, DashboardController controller) {
        super(22);
        getChildren().add(DashboardWidgets.welcomeHeader(user));
        try {
            compose(controller.loadCareDashboard(user.getMaNhanVien()));
        } catch (IllegalStateException exception) {
            DashboardWidgets.showLoadError(this, exception);
        }
    }

    private void compose(CareDashboard data) {
        HBox count = new HBox(16, new MetricCard("Số lịch chờ xác nhận",
                Integer.toString(data.awaitingConfirmation()), true));
        getChildren().addAll(appointments(data.appointmentsToday()), count,
                allergyPets(data.petsWithAllergiesToday()));
    }

    private DataTablePanel appointments(List<Appointment> appointments) {
        return new DataTablePanel("Lịch của tôi hôm nay",
                List.of("Giờ", "Thú cưng", "Dịch vụ", "Trạng thái"),
                appointments.stream().map(item -> List.of(DashboardWidgets.time(item.time()),
                        item.pet(), item.services(), item.status())).toList());
    }

    private DataTablePanel allergyPets(List<AllergyPet> pets) {
        return new DataTablePanel("Thú cưng hôm nay có ghi chú dị ứng/bệnh nền",
                List.of("Thú cưng", "Loài", "Ghi chú", "Chủ nuôi"),
                pets.stream().map(item -> List.of(item.pet(), item.type(),
                        item.allergy(), item.customer())).toList());
    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public String getTitle() {
        return "Tổng quan";
    }
}
