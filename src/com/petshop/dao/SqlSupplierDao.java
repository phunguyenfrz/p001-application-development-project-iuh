package com.petshop.dao;

import com.petshop.model.NhaCungCap;
import com.petshop.util.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class SqlSupplierDao implements CrudDao<NhaCungCap, String> {
    private static final String SELECT = """
            SELECT maNhaCungCap, tenNhaCungCap, soDienThoai, diaChi
            FROM NhaCungCap
            """;

    @Override
    public List<NhaCungCap> findAll() {
        return query(SELECT + " ORDER BY tenNhaCungCap", null);
    }

    @Override
    public List<NhaCungCap> search(String keyword) {
        String sql = SELECT + """
                WHERE maNhaCungCap LIKE ? OR tenNhaCungCap LIKE ? OR soDienThoai LIKE ?
                ORDER BY tenNhaCungCap
                """;
        return query(sql, "%" + (keyword == null ? "" : keyword.trim()) + "%");
    }

    @Override
    public void add(NhaCungCap supplier) {
        String sql = """
                INSERT INTO NhaCungCap (maNhaCungCap, tenNhaCungCap, soDienThoai, diaChi)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, supplier.getMaNhaCungCap());
                statement.setString(2, supplier.getTenNhaCungCap().trim());
                statement.setString(3, supplier.getSoDienThoai().trim());
                statement.setString(4, nullable(supplier.getDiaChi()));
                statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể thêm nhà cung cấp. Hãy kiểm tra số điện thoại.", exception);
        }
    }

    @Override
    public void update(NhaCungCap supplier) {
        String sql = """
                UPDATE NhaCungCap SET tenNhaCungCap = ?, soDienThoai = ?, diaChi = ?
                WHERE maNhaCungCap = ?
                """;
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, supplier.getTenNhaCungCap().trim());
            statement.setString(2, supplier.getSoDienThoai().trim());
            statement.setString(3, nullable(supplier.getDiaChi()));
            statement.setString(4, supplier.getMaNhaCungCap());
            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException("Không tìm thấy nhà cung cấp cần cập nhật.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể cập nhật nhà cung cấp. Hãy kiểm tra số điện thoại.", exception);
        }
    }

    @Override
    public void deactivate(String id) {
        throw new UnsupportedOperationException(
                "Cơ sở dữ liệu chưa có trạng thái ngừng cho nhà cung cấp.");
    }

    private List<NhaCungCap> query(String sql, String pattern) {
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (pattern != null) {
                for (int index = 1; index <= 3; index++) {
                    statement.setString(index, pattern);
                }
            }
            try (ResultSet rows = statement.executeQuery()) {
                List<NhaCungCap> suppliers = new ArrayList<>();
                while (rows.next()) {
                    suppliers.add(new NhaCungCap(rows.getString("maNhaCungCap"),
                            rows.getString("tenNhaCungCap"), rows.getString("soDienThoai"),
                            rows.getString("diaChi"), false));
                }
                return List.copyOf(suppliers);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể tải nhà cung cấp.", exception);
        }
    }

    private String nullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
