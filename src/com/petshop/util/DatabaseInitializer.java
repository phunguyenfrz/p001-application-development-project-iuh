package com.petshop.util;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Kiểm tra kết nối database khi ứng dụng khởi động.
 * Schema và dữ liệu mẫu được tạo bằng script QLCH_HappyTails_Database_Full.sql
 * (chạy trên SQL Server trước khi mở ứng dụng).
 */
public final class DatabaseInitializer {

    private DatabaseInitializer() {}

    /**
     * Thử mở kết nối tới database.
     * @return true nếu kết nối thành công.
     */
    public static boolean initialize() {
        try (Connection conn = DatabaseManager.getConnection()) {
            return conn.isValid(3);
        } catch (SQLException e) {
            System.err.println("[DatabaseInitializer] Không kết nối được database: " + e.getMessage());
            return false;
        }
    }
}
