package com.petshop.controller;

import com.petshop.dao.UserDao;
import com.petshop.model.User;

public class LoginController {
    private final UserDao userDao = new UserDao();

    /**
     * @return User nếu đăng nhập đúng, null nếu sai tên đăng nhập/mật khẩu.
     * @throws IllegalStateException nếu không kết nối được database.
     */
    public User authenticate(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            return null;
        }
        return userDao.findByCredentials(username.trim().toLowerCase(), password);
    }
}
