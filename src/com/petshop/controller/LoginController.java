package com.petshop.controller;

import com.petshop.model.User;
import com.petshop.service.AuthService;

public class LoginController {
    private final AuthService authService;

    public LoginController() {
        this.authService = new AuthService();
    }

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * @return User nếu đăng nhập đúng, null nếu sai tên đăng nhập/mật khẩu.
     * @throws IllegalStateException nếu không kết nối được database.
     */
    public User authenticate(String username, String password) {
        return authService.login(username, password);
    }
}

