package com.petshop.dao;

import com.petshop.model.Promotion;
import com.petshop.util.DbConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class PromotionDao implements CrudDao<Promotion, String> {
    private static final String SELECT = """
            SELECT maKhuyenMai, tenChuongTrinh, phanTramGiam,
                   ngayBatDau, ngayKetThuc, trangThai
            FROM KhuyenMai
            """;

    @Override
    public List<Promotion> findAll() {
        return query(SELECT + " ORDER BY ngayBatDau DESC, tenChuongTrinh", null);
    }

    @Override
    public List<Promotion> search(String keyword) {
        String sql = SELECT + """
                WHERE maKhuyenMai LIKE ? OR tenChuongTrinh LIKE ?
                ORDER BY ngayBatDau DESC, tenChuongTrinh
                """;
        return query(sql, "%" + (keyword == null ? "" : keyword.trim()) + "%");
    }

    @Override
    public void add(Promotion promotion) {
        execute("""
                INSERT INTO KhuyenMai
                    (maKhuyenMai, tenChuongTrinh, phanTramGiam, ngayBatDau, ngayKetThuc, trangThai)
                VALUES (?, ?, ?, ?, ?, ?)
                """, promotion, true);
    }

    @Override
    public void update(Promotion promotion) {
        execute("""
                UPDATE KhuyenMai SET tenChuongTrinh = ?, phanTramGiam = ?,
                    ngayBatDau = ?, ngayKetThuc = ?, trangThai = ?
                WHERE maKhuyenMai = ?
                """, promotion, false);
    }

    @Override
    public void deactivate(String id) {
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE KhuyenMai SET trangThai = 0 WHERE maKhuyenMai = ?")) {
            statement.setString(1, id);
            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException("Không tìm thấy khuyến mãi cần ngừng.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể ngừng khuyến mãi.", exception);
        }
    }

    private void execute(String sql, Promotion promotion, boolean adding) {
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            if (adding) {
                statement.setString(index++, promotion.id());
            }
            statement.setString(index++, promotion.name().trim());
            statement.setBigDecimal(index++, promotion.discountPercent());
            statement.setDate(index++, Date.valueOf(promotion.startsOn()));
            statement.setDate(index++, Date.valueOf(promotion.endsOn()));
            statement.setBoolean(index++, promotion.active());
            if (!adding) {
                statement.setString(index, promotion.id());
            }
            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException("Không tìm thấy khuyến mãi cần cập nhật.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể lưu khuyến mãi.", exception);
        }
    }

    private List<Promotion> query(String sql, String pattern) {
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (pattern != null) {
                statement.setString(1, pattern);
                statement.setString(2, pattern);
            }
            try (ResultSet rows = statement.executeQuery()) {
                List<Promotion> result = new ArrayList<>();
                while (rows.next()) {
                    result.add(new Promotion(rows.getString("maKhuyenMai"),
                            rows.getString("tenChuongTrinh"),
                            rows.getBigDecimal("phanTramGiam"),
                            rows.getDate("ngayBatDau").toLocalDate(),
                            rows.getDate("ngayKetThuc").toLocalDate(),
                            rows.getBoolean("trangThai")));
                }
                return List.copyOf(result);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể tải chương trình khuyến mãi.", exception);
        }
    }
}
