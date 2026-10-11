package com.petshop.controller;

import com.petshop.model.User;
import com.petshop.util.SessionManager;

import java.util.Objects;

public final class SessionControllerImpl implements SessionController {
    private final com.petshop.util.AuditService auditService;

    public SessionControllerImpl(com.petshop.util.AuditService auditService) {
        this.auditService = Objects.requireNonNull(auditService);
    }

    @Override
    public void logout(User user) {
        Objects.requireNonNull(user, "Tài khoản đăng xuất không được để trống.");
        try {
            auditService.log(user, "Đăng xuất", "TaiKhoan", user.getMaTaiKhoan(), null);
        } finally {
            SessionManager.logout();
        }
    }
}
