package com.petshop.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DbConnection {
    private DbConnection() {
    }

    public static Connection getConnection() {
        Properties config = DbConfig.load();
        try {
            return DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("user"),
                    config.getProperty("password"));
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể kết nối SQL Server QLCH_HappyTails. "
                            + "Hãy kiểm tra SQL Server, cổng kết nối và thông tin trong db.properties.",
                    exception);
        }
    }
}
