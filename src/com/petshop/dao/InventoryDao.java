package com.petshop.dao;

import com.petshop.model.InventoryItem;
import com.petshop.util.DbConnection;
import com.petshop.util.TextSearch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class InventoryDao implements InventoryRepository {
    private static final String SELECT = """
            SELECT sp.maSanPham, sp.tenSanPham, sp.maVach, sp.soLuongTon,
                   sp.donViTinh, sp.nguongTonToiThieu, sp.giaBan,
                   ncc.tenNhaCungCap
            FROM SanPham sp
            LEFT JOIN NhaCungCap ncc ON ncc.maNhaCungCap = sp.maNhaCungCap
            WHERE sp.trangThai = 1
            """;

    @Override
    public List<InventoryItem> findAll() {
        return query(SELECT + " ORDER BY sp.tenSanPham", null);
    }

    @Override
    public List<InventoryItem> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        String sql = SELECT + " AND "
                + TextSearch.likePredicate(keyword, "sp.maSanPham", "sp.tenSanPham",
                        "sp.maVach", "ncc.tenNhaCungCap")
                + """
                ORDER BY sp.tenSanPham
                """;
        return query(sql, keyword);
    }

    @Override
    public List<InventoryItem> findBelowMinimum() {
        return query(SELECT + """
                  AND sp.soLuongTon <= sp.nguongTonToiThieu
                ORDER BY sp.soLuongTon, sp.tenSanPham
                """, null);
    }

    private List<InventoryItem> query(String sql, String pattern) {
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (pattern != null) {
                TextSearch.bindLikeParameters(statement, 1, pattern, 4);
            }
            try (ResultSet rows = statement.executeQuery()) {
                List<InventoryItem> items = new ArrayList<>();
                while (rows.next()) {
                    items.add(new InventoryItem(
                            rows.getString("maSanPham"),
                            rows.getString("tenSanPham"),
                            rows.getString("maVach"),
                            rows.getInt("soLuongTon"),
                            rows.getString("donViTinh"),
                            rows.getInt("nguongTonToiThieu"),
                            rows.getBigDecimal("giaBan"),
                            rows.getString("tenNhaCungCap")));
                }
                return List.copyOf(items);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể tải dữ liệu tồn kho.", exception);
        }
    }
}
