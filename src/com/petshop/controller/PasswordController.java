package com.petshop.controller;

public interface PasswordController {
    boolean isStrongPassword(String password);

    boolean changePassword(String accountId, String currentPassword, String newPassword);
}
