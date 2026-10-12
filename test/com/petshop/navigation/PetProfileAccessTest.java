package com.petshop.navigation;

import com.petshop.model.Permission;
import com.petshop.model.Role;
import com.petshop.model.Screen;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PetProfileAccessTest {
    @Test
    void exposesPetProfilesToAllRolesInTheBusinessGroup() {
        MenuModel menu = new MenuModel(new ScreenRegistry());
        MenuModel.Section section = menu.fullMenu().stream()
                .filter(candidate -> candidate.group() == MenuGroup.CUSTOMERS_AND_PETS)
                .findFirst().orElseThrow();

        assertEquals(List.of(Screen.CUSTOMERS, Screen.PETS),
                section.entries().stream().map(MenuModel.Entry::screen).toList());
        assertTrue(menu.forRole(Role.BAN_HANG).stream()
                .flatMap(candidate -> candidate.entries().stream())
                .anyMatch(entry -> entry.screen() == Screen.PETS));
        assertTrue(menu.forRole(Role.CHAM_SOC).stream()
                .flatMap(candidate -> candidate.entries().stream())
                .anyMatch(entry -> entry.screen() == Screen.PETS));
        assertTrue(menu.forRole(Role.QUAN_LY).stream()
                .flatMap(candidate -> candidate.entries().stream())
                .anyMatch(entry -> entry.screen() == Screen.PETS));
    }

    @Test
    void grantsPetProfileWritePermissionsOnlyToSales() {
        assertEquals(java.util.Set.of(Permission.VIEW),
                ScreenRegistry.permissionsFor(Screen.PETS, Role.QUAN_LY));
        assertEquals(java.util.Set.of(Permission.VIEW, Permission.CREATE,
                        Permission.UPDATE, Permission.DEACTIVATE),
                ScreenRegistry.permissionsFor(Screen.PETS, Role.BAN_HANG));
        assertEquals(java.util.Set.of(Permission.VIEW),
                ScreenRegistry.permissionsFor(Screen.PETS, Role.CHAM_SOC));
    }
}
