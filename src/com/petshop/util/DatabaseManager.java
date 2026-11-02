package com.petshop.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
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

    private static volatile HikariDataSource dataSource;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseManager::closePool, "hikari-shutdown-hook"));
    }

    private DatabaseManager() {}

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private static HikariDataSource getDataSource() {
        HikariDataSource ds = dataSource;
        if (ds == null || ds.isClosed()) {
            synchronized (DatabaseManager.class) {
                ds = dataSource;
                if (ds == null || ds.isClosed()) {
                    HikariConfig config = new HikariConfig();
                    config.setPoolName("HappyTails-HikariPool");
                    config.setJdbcUrl(env("DB_URL", DEFAULT_URL));
                    config.setUsername(env("DB_USER", DEFAULT_USER));
                    config.setPassword(env("DB_PASSWORD", DEFAULT_PASSWORD));

                    config.setMaximumPoolSize(10);
                    config.setMinimumIdle(2);
                    config.setConnectionTimeout(5_000);
                    config.setIdleTimeout(300_000);
                    config.setMaxLifetime(1_800_000);
                    config.setInitializationFailTimeout(-1);

                    // Bật bộ đệm PreparedStatement trong mssql-jdbc driver
                    config.addDataSourceProperty("disableStatementPooling", "false");
                    config.addDataSourceProperty("statementPoolingCacheSize", "30");

                    ds = new HikariDataSource(config);
                    dataSource = ds;
                }
            }
        }
        return ds;
    }

    /**
     * Mượn một Connection từ Connection Pool.
     * Gọi trong try-with-resources để tự động trả kết nối về Pool.
     */
    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    private static synchronized void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
        }
    }
}
