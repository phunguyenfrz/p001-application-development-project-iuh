package com.petshop.view;

import com.petshop.model.User;
import com.petshop.view.careservice.CareServiceView;
import com.petshop.view.services.AppointmentModuleView;
import com.petshop.view.services.InvoiceCreateView;
import com.petshop.view.services.InvoiceModuleView;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class AssignedModuleView extends VBox {

    public AssignedModuleView(String actionId, String actionName, User user) {
        Node view = switch (actionId.substring(0, 4)) {
            case "SRV_" -> new CareServiceView(actionId);
            case "APT_" -> new AppointmentModuleView(actionId, actionName);
                case "INV_" -> "INV_CREATE".equals(actionId)
                    ? new InvoiceCreateView(user)
                    : new InvoiceModuleView(actionId, actionName, user);
            default -> throw new IllegalArgumentException("Mã chức năng không được hỗ trợ: " + actionId);
        };
        getChildren().setAll(view);
    }
}