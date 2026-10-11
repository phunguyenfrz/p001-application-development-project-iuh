package com.petshop.controller;

import com.petshop.model.Screen;
import com.petshop.model.SearchResult;
import javafx.beans.property.ReadOnlyObjectProperty;

public interface ScreenNavigator {
    boolean navigate(Screen screen);

    boolean navigate(Screen screen, String title);

    boolean navigate(SearchResult result);

    boolean navigateToAppointmentInvoice(String appointmentId);

    ReadOnlyObjectProperty<Screen> currentScreenProperty();
}
