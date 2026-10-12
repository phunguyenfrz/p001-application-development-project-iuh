package com.petshop.util;

import com.petshop.model.User;
import com.petshop.model.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {
    @Test
    void hashesAndVerifiesPassword() {
        String hash = PasswordUtil.hash("correct horse battery staple");

        assertTrue(hash.startsWith("$2"));
        assertTrue(PasswordUtil.verify("correct horse battery staple", hash));
        assertFalse(PasswordUtil.verify("wrong password", hash));
    }

    @Test
    void rejectsEmptyPasswordsAndNonBcryptHashes() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hash(""));
        assertFalse(PasswordUtil.verify("password", "plain-text-password"));
    }

    @Test
    void roleNamesMatchOnlySupportedDatabaseValues() {
        assertEquals(Role.QUAN_LY, Role.fromDbValue("Quản lý"));
        assertEquals(Role.BAN_HANG, Role.fromDbValue("Bán hàng"));
        assertEquals(Role.CHAM_SOC, Role.fromDbValue("Chăm sóc"));
        assertThrows(IllegalArgumentException.class,
                () -> Role.fromDbValue("Thu ngân"));
    }
}
