package com.petshop.dao;

import com.petshop.util.DbConnection;
import com.petshop.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PasswordDao implements PasswordRepository {
    public boolean changePassword(String accountId, String currentPassword, String newPassword) {
        String selectSql = "SELECT matKhau FROM TaiKhoan WHERE maTaiKhoan = ? AND trangThai = 1";
        String updateSql = "UPDATE TaiKhoan SET matKhau = ? WHERE maTaiKhoan = ? AND matKhau = ?";
        try (Connection connection = DbConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String currentHash;
                try (PreparedStatement select = connection.prepareStatement(selectSql)) {
                    select.setString(1, accountId);
                    try (ResultSet result = select.executeQuery()) {
                        if (!result.next()) {
                            connection.rollback();
                            return false;
                        }
                        currentHash = result.getString("matKhau");
                    }
                }
                if (!PasswordUtil.verify(currentPassword, currentHash)) {
                    connection.rollback();
                    return false;
                }
                try (PreparedStatement update = connection.prepareStatement(updateSql)) {
                    update.setString(1, PasswordUtil.hash(newPassword));
                    update.setString(2, accountId);
                    update.setString(3, currentHash);
                    if (update.executeUpdate() != 1) {
                        connection.rollback();
                        return false;
                    }
                }
                connection.commit();
                return true;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể cập nhật mật khẩu.", exception);
        }
    }
}
