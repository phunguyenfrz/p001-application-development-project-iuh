package com.petshop.controller;

import com.petshop.dao.PetDao;
import com.petshop.model.Pet;
import com.petshop.model.Role;
import com.petshop.model.User;
import com.petshop.util.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PetControllerTest {
    @AfterEach
    void clearSession() {
        SessionManager.logout();
    }

    @Test
    void refusesDirectPetWritesForCareRoleBeforeCallingDao() {
        SessionManager.login(new User("TK-1", "NV-1", "care", "Care User", Role.CHAM_SOC));
        PetController controller = new PetController(new PetDao(), (action, table, id, note) -> {
        });

        assertThrows(IllegalStateException.class, () -> controller.add(new Pet()));
        assertThrows(IllegalStateException.class, () -> controller.update(new Pet()));
        assertThrows(IllegalStateException.class, () -> controller.deactivate("TC-1"));
    }

    @Test
    void rejectsNonPositivePetWeightBeforePersisting() {
        SessionManager.login(new User("TK-2", "NV-2", "sales", "Sales User", Role.BAN_HANG));
        PetController controller = new PetController(new PetDao(), (action, table, id, note) -> {
        });
        Pet pet = new Pet(null, "KH-1", "Milo", "Chó", "Poodle",
                0, null, null, null);

        assertThrows(IllegalArgumentException.class, () -> controller.add(pet));
    }
}
