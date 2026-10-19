package com.petshop.view;

import com.petshop.model.User;
import com.petshop.view.services.AppointmentModuleView;
import com.petshop.view.services.InvoiceModuleView;
import com.petshop.view.services.ServiceModuleView;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class AssignedModuleView extends VBox {

    public AssignedModuleView(String actionId, String actionName, User user) {
        Node view = switch (actionId.substring(0, 4)) {
            case "SRV_" -> new ServiceModuleView(actionId, actionName);
            case "APT_" -> new AppointmentModuleView(actionId, actionName);
            case "INV_" -> new InvoiceModuleView(actionId, actionName, user);
            default -> throw new IllegalArgumentException("Mã chức năng không được hỗ trợ: " + actionId);
        };
        getChildren().setAll(view);
    }
}