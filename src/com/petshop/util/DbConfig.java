package com.petshop.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public final class DbConfig {
    private static final String RESOURCE_NAME = "/db.properties";

    private DbConfig() {
    }

    static Properties load() {
        Properties properties = new Properties();
        try (InputStream input = DbConfig.class.getResourceAsStream(RESOURCE_NAME)) {
            if (input == null) {
                throw new IllegalStateException(
                        "Thiếu cấu hình cơ sở dữ liệu db.properties trên classpath. "
                                + "Hãy tạo src/db.properties theo db.properties.example.");
            }
            properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("Không thể đọc cấu hình cơ sở dữ liệu db.properties.", exception);
        }

        require(properties, "url");
        require(properties, "user");
        require(properties, "password");
        return properties;
    }

    public static String getBranchName() {
        try (InputStream input = DbConfig.class.getResourceAsStream(RESOURCE_NAME)) {
            if (input == null) {
                return "Chi nhánh Quận 1";
            }
            Properties properties = new Properties();
            properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
            String branch = properties.getProperty("branch");
            return branch == null || branch.isBlank() ? "Chi nhánh Quận 1" : branch.trim();
        } catch (IOException exception) {
            throw new IllegalStateException("Không thể đọc tên chi nhánh từ db.properties.", exception);
        }
    }

    private static void require(Properties properties, String key) {
        if (properties.getProperty(key) == null || properties.getProperty(key).isBlank()) {
            throw new IllegalStateException(
                    "Thiếu giá trị '" + key + "' trong cấu hình cơ sở dữ liệu db.properties.");
        }
    }
}
