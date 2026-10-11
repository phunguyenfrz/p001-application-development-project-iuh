package com.petshop.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NhaCungCapTest {
    @Test
    void validatesRequiredFieldsAndPhoneNumber() {
        NhaCungCap invalid = new NhaCungCap("", "", "abc", "", false);
        assertTrue(invalid.validate().size() >= 3);

        NhaCungCap valid = new NhaCungCap(
                "NCC-001", "Nhà cung cấp", "0901234567", "Quận 1", false);
        assertTrue(valid.validate().isEmpty());
    }
}
