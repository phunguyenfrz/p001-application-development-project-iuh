package com.petshop.dao;

import com.petshop.model.Product;
import com.petshop.util.DbConnection;
import com.petshop.util.TextSearch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class ProductDao implements CrudDao<Product, String> {
    private static final String SELECT = """
            SELECT maSanPham, maNhaCungCap, tenSanPham, maVach, giaBan,
                   soLuongTon, donViTinh, trangThai, nguongTonToiThieu
            FROM SanPham
            """;

    @Override
    public List<Product> findAll() {
        return query(SELECT + " ORDER BY tenSanPham", null);
    }

    @Override
    public List<Product> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        String sql = SELECT + " WHERE "
                + TextSearch.likePredicate(keyword, "maSanPham", "tenSanPham", "maVach")
                + """
                ORDER BY tenSanPham
                """;
        return query(sql, keyword);
    }

    @Override
    public void add(Product product) {
        String sql = """
                INSERT INTO SanPham
                    (maSanPham, maNhaCungCap, tenSanPham, maVach, giaBan,
                     soLuongTon, donViTinh, trangThai, nguongTonToiThieu)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        String id = transaction(connection -> {
            String generatedId = nextProductCode(connection);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bind(statement, product, generatedId);
                statement.executeUpdate();
            }
            return generatedId;
        });
        product.setMaSanPham(id);
    }

    @Override
    public void update(Product product) {
        String sql = """
                UPDATE SanPham
                SET maNhaCungCap = ?, tenSanPham = ?, maVach = ?, giaBan = ?,
                    donViTinh = ?, trangThai = ?, nguongTonToiThieu = ?
                WHERE maSanPham = ?
                """;
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, blankToNull(product.getMaNhaCungCap()));
            statement.setString(2, product.getTenSanPham().trim());
            statement.setString(3, blankToNull(product.getMaVach()));
            statement.setBigDecimal(4, product.getGiaBanDecimal());
            statement.setString(5, product.getDonViTinh().trim());
            statement.setBoolean(6, product.isTrangThai());
            statement.setInt(7, product.getNguongTonToiThieu());
            statement.setString(8, product.getMaSanPham());
            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException("Không tìm thấy sản phẩm cần cập nhật.");
            }
        } catch (SQLException exception) {
            throw databaseFailure("Không thể cập nhật sản phẩm. Hãy kiểm tra mã vạch và nhà cung cấp.",
                    exception);
        }
    }

    @Override
    public void deactivate(String id) {
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE SanPham SET trangThai = 0 WHERE maSanPham = ?")) {
            statement.setString(1, id);
            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException("Không tìm thấy sản phẩm cần ngừng.");
            }
        } catch (SQLException exception) {
            throw databaseFailure("Không thể ngừng kinh doanh sản phẩm.", exception);
        }
    }

    private List<Product> query(String sql, String pattern) {
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (pattern != null) {
                TextSearch.bindLikeParameters(statement, 1, pattern, 3);
            }
            try (ResultSet rows = statement.executeQuery()) {
                List<Product> products = new ArrayList<>();
                while (rows.next()) {
                    products.add(new Product(
                            rows.getString("maSanPham"),
                            rows.getString("maNhaCungCap"),
                            rows.getString("tenSanPham"),
                            rows.getString("maVach"),
                            rows.getBigDecimal("giaBan"),
                            rows.getInt("soLuongTon"),
                            rows.getString("donViTinh"),
                            rows.getBoolean("trangThai"),
                            rows.getInt("nguongTonToiThieu")));
                }
                return List.copyOf(products);
            }
        } catch (SQLException exception) {
            throw databaseFailure("Không thể tải danh sách sản phẩm.", exception);
        }
    }

    private void bind(PreparedStatement statement, Product product, String id)
            throws SQLException {
        statement.setString(1, id);
        statement.setString(2, blankToNull(product.getMaNhaCungCap()));
        statement.setString(3, product.getTenSanPham().trim());
        statement.setString(4, blankToNull(product.getMaVach()));
        statement.setBigDecimal(5, product.getGiaBanDecimal());
        statement.setInt(6, product.getSoLuongTon());
        statement.setString(7, product.getDonViTinh().trim());
        statement.setBoolean(8, product.isTrangThai());
        statement.setInt(9, product.getNguongTonToiThieu());
    }

    private String nextProductCode(Connection connection) throws SQLException {
        String day = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String sql = """
                SELECT ISNULL(MAX(TRY_CONVERT(int, RIGHT(maSanPham, 6))), 0)
                FROM SanPham WITH (UPDLOCK, HOLDLOCK)
                WHERE maSanPham LIKE ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "SP-" + day + "-%");
            try (ResultSet row = statement.executeQuery()) {
                row.next();
                int next = row.getInt(1) + 1;
                if (next > 999_999) {
                    throw new IllegalStateException("Đã vượt số mã sản phẩm tối đa trong ngày.");
                }
                return "SP-" + day + "-" + String.format("%06d", next);
            }
        }
    }

    private <T> T transaction(SqlWork<T> work) {
        try (Connection connection = DbConnection.getConnection()) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            try {
                T result = work.run(connection);
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                if (exception instanceof SQLException sqlException) {
                    throw databaseFailure("Không thể thêm sản phẩm. Hãy kiểm tra mã vạch "
                            + "và nhà cung cấp.", sqlException);
                }
                throw exception;
            }
        } catch (SQLException exception) {
            throw databaseFailure("Không thể lưu sản phẩm.", exception);
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private IllegalStateException databaseFailure(String message, SQLException exception) {
        return new IllegalStateException(message, exception);
    }

    @FunctionalInterface
    private interface SqlWork<T> {
        T run(Connection connection) throws SQLException;
    }
}
