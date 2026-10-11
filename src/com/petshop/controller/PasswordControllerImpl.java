package com.petshop.controller;

import com.petshop.dao.PasswordRepository;
import com.petshop.model.User;
import com.petshop.util.AuditService;

import java.util.Objects;

public final class PasswordControllerImpl implements PasswordController {
    private final PasswordRepository passwordDao;
    private final AuditService auditService;

    public PasswordControllerImpl(PasswordRepository passwordDao, AuditService auditService) {
        this.passwordDao = Objects.requireNonNull(passwordDao);
        this.auditService = Objects.requireNonNull(auditService);
    }

    @Override
    public boolean isStrongPassword(String password) {
        if (password == null || password.codePointCount(0, password.length()) < 8) {
            return false;
        }
        boolean hasLetter = password.codePoints().anyMatch(Character::isLetter);
        boolean hasDigit = password.codePoints().anyMatch(Character::isDigit);
        return hasLetter && hasDigit;
    }

    @Override
    public boolean changePassword(String accountId, String currentPassword, String newPassword) {
        boolean changed = passwordDao.changePassword(accountId, currentPassword, newPassword);
        User actor = com.petshop.util.SessionManager.getCurrentUser();
        if (changed && actor != null) {
            try {
                auditService.log(actor, "Đổi mật khẩu", "TaiKhoan", accountId, null);
            } catch (IllegalStateException exception) {
                throw new PasswordChangedAuditException(
                        "Mật khẩu đã thay đổi nhưng không thể ghi nhật ký thao tác: "
                                + exception.getMessage(), exception);
            }
        }
        return changed;
    }
}
