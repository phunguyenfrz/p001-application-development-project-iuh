package com.petshop.controller;

import com.petshop.model.User;
import com.petshop.util.SessionManager;

import java.util.Objects;

public final class AuditControllerImpl implements AuditController {
    private final com.petshop.util.AuditService auditService;

    public AuditControllerImpl(com.petshop.util.AuditService auditService) {
        this.auditService = Objects.requireNonNull(auditService);
    }

    @Override
    public void log(String action, String targetTable, String targetId, String note) {
        User actor = SessionManager.getCurrentUser();
        if (actor == null) {
            throw new IllegalStateException("Không thể ghi nhật ký khi chưa có phiên đăng nhập.");
        }
        auditService.log(actor, action, targetTable, targetId, note);
    }
}
