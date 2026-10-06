package com.petshop.controller;

import com.petshop.model.User;

import java.util.Map;

public class LoginController {
    private final Map<String, Account> accounts = Map.of(
            "owner", new Account("owner123", new User("owner", "Chủ cửa hàng", User.Role.OWNER)),
            "staff", new Account("staff123", new User("staff", "Nhân viên bán hàng", User.Role.STAFF))
    );

    public User authenticate(String username, String password) {
        Account account = accounts.get(username.trim().toLowerCase());
        if (account != null && account.password().equals(password)) {
            return account.user();
        }
        return null;
    }

    private record Account(String password, User user) {
    }
}
