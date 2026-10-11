package com.petshop.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleTest {
    @Test
    void mapsDatabaseValuesAndRejectsUnknownRole() {
        assertEquals(Role.QUAN_LY, Role.fromDbValue("Quản lý"));
        assertEquals(Role.BAN_HANG, Role.fromDbValue("Bán hàng"));
        assertEquals(Role.CHAM_SOC, Role.fromDbValue("Chăm sóc"));
        assertThrows(IllegalArgumentException.class, () -> Role.fromDbValue("Thu ngân"));
        assertEquals(3, Role.values().length);
    }

    @Test
    void enforcesRoleScreenMatrix() {
        assertTrue(Role.QUAN_LY.canAccess(Screen.EMPLOYEES));
        assertTrue(Role.QUAN_LY.canAccess(Screen.REPORTS));
        assertFalse(Role.QUAN_LY.canAccess(Screen.PRODUCTS));
        assertTrue(Role.BAN_HANG.canAccess(Screen.PRODUCTS));
        assertTrue(Role.BAN_HANG.canAccess(Screen.RETURNS));
        assertFalse(Role.BAN_HANG.canAccess(Screen.APPOINTMENTS));
        assertTrue(Role.CHAM_SOC.canAccess(Screen.APPOINTMENTS));
        assertTrue(Role.CHAM_SOC.canAccess(Screen.CUSTOMERS));
        assertFalse(Role.CHAM_SOC.canAccess(Screen.INVOICE_CREATE));
    }
}
