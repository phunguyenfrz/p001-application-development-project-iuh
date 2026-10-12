package com.petshop.dao;

public interface PasswordRepository {
    boolean changePassword(String accountId, String currentPassword, String newPassword);
}
