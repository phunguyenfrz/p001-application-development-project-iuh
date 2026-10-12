package com.petshop.controller;

import com.petshop.model.Role;
import com.petshop.model.Screen;
import com.petshop.model.User;
import com.petshop.model.SearchResult;
import com.petshop.navigation.ScreenRegistry;
import com.petshop.view.AppView;
import com.petshop.view.ContentSink;
import com.petshop.view.SearchResultAware;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.Alert;

import java.util.Objects;

public final class NavigationController implements ScreenNavigator {
    private final ContentSink contentSink;
    private final ScreenRegistry registry;
    private final Role role;
    private AppView currentView;
    private final ReadOnlyObjectWrapper<Screen> currentScreen =
            new ReadOnlyObjectWrapper<>(this, "currentScreen");

    public NavigationController(ContentSink contentSink, ScreenRegistry registry, User user) {
        this.contentSink = Objects.requireNonNull(contentSink);
        this.registry = Objects.requireNonNull(registry);
        this.role = Objects.requireNonNull(user).getRole();
    }

    @Override
    public boolean navigate(Screen screen) {
        return navigate(screen, screen == null ? "" : registry.definition(screen).label());
    }

    @Override
    public boolean navigate(Screen screen, String title) {
        if (!authorize(screen, title)) {
            return false;
        }
        return show(screen, registry.create(screen), title);
    }

    @Override
    public boolean navigate(SearchResult result) {
        Objects.requireNonNull(result, "Kết quả tìm kiếm không được để trống.");
        if (!authorize(result.screen(), result.name())) {
            return false;
        }
        AppView view = registry.create(result.screen());
        if (view instanceof SearchResultAware resultAware) {
            resultAware.showSearchResult(result.id());
        }
        return show(result.screen(), view, result.name());
    }

    @Override
    public boolean navigateSearch(Screen screen, String keyword) {
        if (!authorize(screen, screen == null ? "" : registry.definition(screen).label())) {
            return false;
        }
        AppView view = registry.create(screen);
        if (view instanceof SearchResultAware resultAware) {
            resultAware.showSearchKeyword(keyword);
        }
        return show(screen, view, registry.definition(screen).label());
    }

    @Override
    public boolean navigateToAppointmentInvoice(String appointmentId) {
        Screen screen = Screen.INVOICE_CREATE;
        if (!authorize(screen, "Lập hóa đơn")) {
            return false;
        }
        return show(screen, registry.createAppointmentInvoice(appointmentId),
                appointmentId == null ? "Lập hóa đơn" : "Lập hóa đơn | " + appointmentId);
    }

    @Override
    public ReadOnlyObjectProperty<Screen> currentScreenProperty() {
        return currentScreen.getReadOnlyProperty();
    }

    public Screen getCurrentScreen() {
        return currentScreen.get();
    }

    private boolean show(Screen screen, AppView nextView, String title) {
        Objects.requireNonNull(nextView, "Factory màn hình trả về null.");
        if (currentView != null) {
            currentView.onHide();
        }
        contentSink.setContent(Objects.requireNonNull(nextView.getNode(),
                        "AppView không trả về Node."), title == null || title.isBlank()
                ? nextView.getTitle() : title);
        currentView = nextView;
        currentScreen.set(screen);
        currentView.onShow();
        return true;
    }

    private boolean authorize(Screen screen, String title) {
        if (screen != null && role.canAccess(screen)) {
            return true;
        }
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Không đủ quyền");
        alert.setHeaderText("Truy cập bị từ chối");
        alert.setContentText(screen == null
                ? "Màn hình yêu cầu không hợp lệ."
                : "Vai trò \"" + role.getDisplayName()
                        + "\" không có quyền truy cập: " + title + ".");
        alert.showAndWait();
        return false;
    }
}
