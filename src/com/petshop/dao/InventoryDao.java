package com.petshop.dao;

import com.petshop.model.InventoryItem;
import com.petshop.util.DbConnection;

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
        String sql = SELECT + """
                  AND (sp.maSanPham LIKE ? OR sp.tenSanPham LIKE ? OR sp.maVach LIKE ?
                       OR ncc.tenNhaCungCap LIKE ?)
                ORDER BY sp.tenSanPham
                """;
        String pattern = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        return query(sql, pattern);
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
                for (int parameter = 1; parameter <= 4; parameter++) {
                    statement.setString(parameter, pattern);
                }
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
