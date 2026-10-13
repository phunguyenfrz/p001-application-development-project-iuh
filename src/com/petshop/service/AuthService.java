package com.petshop.service;

import com.petshop.dao.UserDao;
import com.petshop.model.User;
import com.petshop.util.SessionManager;

/**
 * Service xác thực n-Tier và quản lý phiên người dùng.
 */
public class AuthService {

    private final UserDao userDao;

    public AuthService() {
        this.userDao = new UserDao();
    }

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            return null;
        }

        User user = userDao.findByCredentials(username.trim(), password);
        if (user != null) {
            SessionManager.login(user);
        }
        return user;
    }

    public void logout() {
        SessionManager.logout();
    }

    public User getCurrentUser() {
        return SessionManager.getCurrentUser();
    }

    /**
     * Kiểm tra quyền thực hiện chức năng nghiệp vụ của người dùng hiện tại.
     * @throws SecurityException nếu người dùng không có quyền.
     */
    public void checkPermission(String actionId) {
        User user = getCurrentUser();
        if (user == null) {
            throw new SecurityException("Chưa đăng nhập hệ thống!");
        }
        if (!MenuRegistry.hasPermission(user.getRole(), actionId)) {
            throw new SecurityException("Tài khoản [" + user.getUsername() + "] với vai trò [" +
                    user.getRole().getDisplayName() + "] không có quyền thực hiện chức năng [" + actionId + "]!");
        }
    }
}

