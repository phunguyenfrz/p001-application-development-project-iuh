package com.petshop.model;

import java.time.LocalDate;

public record Employee(
        String id,
        String fullName,
        String phone,
        Role role,
        LocalDate birthDate,
        boolean male,
        boolean working,
        String username,
        boolean accountActive) {
}
