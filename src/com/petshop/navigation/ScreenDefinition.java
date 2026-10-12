package com.petshop.navigation;

import com.petshop.model.Screen;
import com.petshop.view.AppView;

import java.util.function.Supplier;

public record ScreenDefinition(
        Screen screen, String label, MenuGroup group, Supplier<AppView> factory) {
}
