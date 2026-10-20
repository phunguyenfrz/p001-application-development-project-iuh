package com.petshop.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Quản lý kết nối đến cơ sở dữ liệu SQL Server (QLCH_HappyTails) qua TCP/IP.
 * Có thể ghi đè cấu hình bằng biến môi trường:
 * DB_URL, DB_USER, DB_PASSWORD.
 *
 * Lưu ý: driver mssql-jdbc không hỗ trợ named pipe nên không nối được LocalDB
 * trực tiếp; cần SQL Server/SQL Express đã bật TCP/IP (cổng 1433).
 */
public final class DatabaseManager {

    private static final String DEFAULT_URL =
            "jdbc:sqlserver://localhost:1433;databaseName=QLCH_HappyTails;encrypt=true;trustServerCertificate=true";
    private static final String DEFAULT_USER = "petshop";
    private static final String DEFAULT_PASSWORD = "Petshop@123";

    private DatabaseManager() {}

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private static volatile boolean schemaMigrated = false;

    /**
     * Lấy một Connection mới từ database.
     * Gọi trong try-with-resources để tự động đóng.
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(
                env("DB_URL", DEFAULT_URL),
                env("DB_USER", DEFAULT_USER),
                env("DB_PASSWORD", DEFAULT_PASSWORD));
        if (!schemaMigrated) {
            synchronized (DatabaseManager.class) {
                if (!schemaMigrated) {
                    schemaMigrated = true;
                    DatabaseInitializer.ensureMigrations(conn);
                }
            }
        }
        return conn;
    }
}
