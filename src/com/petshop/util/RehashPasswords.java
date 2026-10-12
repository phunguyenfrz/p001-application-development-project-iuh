package com.petshop.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class RehashPasswords {
    private record AccountPassword(String id, String password) {
    }

    private RehashPasswords() {
    }

    public static void main(String[] args) {
        int updated = rehashLegacyPasswords();
        System.out.println("Đã băm lại mật khẩu cho " + updated + " tài khoản.");
    }

    private static int rehashLegacyPasswords() {
        String selectSql = """
                SELECT maTaiKhoan, matKhau
                FROM TaiKhoan
                WHERE matKhau NOT LIKE '$2%'
                """;
        String updateSql = "UPDATE TaiKhoan SET matKhau = ? WHERE maTaiKhoan = ?";
        try (Connection connection = DbConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                List<AccountPassword> accounts = new ArrayList<>();
                try (PreparedStatement select = connection.prepareStatement(selectSql);
                     ResultSet results = select.executeQuery()) {
                    while (results.next()) {
                        accounts.add(new AccountPassword(
                                results.getString("maTaiKhoan"), results.getString("matKhau")));
                    }
                }

                try (PreparedStatement update = connection.prepareStatement(updateSql)) {
                    for (AccountPassword account : accounts) {
                        update.setString(1, PasswordUtil.hash(account.password()));
                        update.setString(2, account.id());
                        update.addBatch();
                    }
                    update.executeBatch();
                }
                connection.commit();
                return accounts.size();
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể băm lại mật khẩu tài khoản trong QLCH_HappyTails.", exception);
        }
    }
}
