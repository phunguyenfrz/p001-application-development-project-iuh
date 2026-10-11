package com.petshop.navigation;

import com.petshop.model.Role;
import com.petshop.model.Screen;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class MenuModel {
    public record Entry(String label, Screen screen) {
    }

    public record Section(MenuGroup group, List<Entry> entries) {
        public Section {
            entries = List.copyOf(entries);
        }

        public String label() {
            return group.label();
        }
    }

    private final List<Section> sections;

    public MenuModel(ScreenRegistry registry, Role role) {
        Map<MenuGroup, List<Entry>> grouped = new EnumMap<>(MenuGroup.class);
        for (MenuGroup group : MenuGroup.values()) {
            grouped.put(group, new ArrayList<>());
        }
        for (ScreenDefinition definition : registry.definitions().values()) {
            if (definition.group() != null && role.canAccess(definition.screen())) {
                grouped.get(definition.group()).add(
                        new Entry(definition.label(), definition.screen()));
            }
        }
        List<Section> result = new ArrayList<>();
        grouped.forEach((group, entries) -> {
            if (!entries.isEmpty()) {
                result.add(new Section(group, entries));
            }
        });
        sections = List.copyOf(result);
    }

    public List<Section> sections() {
        return sections;
    }
}
