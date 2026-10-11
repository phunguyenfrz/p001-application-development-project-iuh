package com.petshop.dao;

import com.petshop.util.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class SqlAuditDao implements AuditRepository {
    @Override
    public void insert(
            String accountId, String action, String targetTable, String targetId, String note) {
        String sql = """
                INSERT INTO NhatKyThaoTac
                    (maTaiKhoan, hanhDong, bangDoiTuong, maDoiTuong, ghiChu)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, accountId);
            statement.setString(2, action);
            statement.setString(3, targetTable);
            statement.setString(4, targetId);
            statement.setString(5, note);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể ghi nhật ký thao tác: " + exception.getMessage(), exception);
        }
    }
}
