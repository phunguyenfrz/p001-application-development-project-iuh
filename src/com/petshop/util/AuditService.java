package com.petshop.util;

import com.petshop.model.User;
import com.petshop.dao.AuditRepository;

import java.util.Objects;

public final class AuditService {
    private final AuditRepository auditRepository;

    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = Objects.requireNonNull(auditRepository);
    }

    public void log(
            User actor, String action, String targetTable, String targetId, String note) {
        Objects.requireNonNull(actor, "Tài khoản thực hiện thao tác không được để trống.");
        auditRepository.insert(actor.getMaTaiKhoan(), action, targetTable, targetId, note);
    }
}
