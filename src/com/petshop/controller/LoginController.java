package com.petshop.controller;

import com.petshop.dao.UserRepository;
import com.petshop.model.User;
import com.petshop.util.AuditService;

public class LoginController implements AuthenticationController {
    private final UserRepository userDao;
    private final com.petshop.util.AuditService auditService;

    public LoginController(UserRepository userDao, com.petshop.util.AuditService auditService) {
        this.userDao = java.util.Objects.requireNonNull(userDao);
        this.auditService = java.util.Objects.requireNonNull(auditService);
    }

    /**
     * @return User nếu đăng nhập đúng, null nếu sai tên đăng nhập/mật khẩu.
     * @throws IllegalStateException nếu không kết nối được database.
     */
    @Override
    public User authenticate(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            return null;
        }
        User user = userDao.findByCredentials(username.trim().toLowerCase(), password);
        if (user != null) {
            auditService.log(user, "Đăng nhập", "TaiKhoan", user.getMaTaiKhoan(), null);
        }
        return user;
    }
}
