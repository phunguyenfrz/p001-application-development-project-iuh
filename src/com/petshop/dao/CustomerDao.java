package com.petshop.dao;

import com.petshop.model.Customer;
import com.petshop.util.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class CustomerDao implements CrudDao<Customer, String> {
    private static final String SELECT = """
            SELECT maKhachHang, hoTen, soDienThoai, email, diemTichLuy, diaChi, ghiChu
            FROM KhachHang
            """;

    @Override
    public List<Customer> findAll() {
        return query(SELECT + " ORDER BY hoTen", null);
    }

    @Override
    public List<Customer> search(String keyword) {
        String sql = SELECT + """
                WHERE maKhachHang LIKE ? OR hoTen LIKE ? OR soDienThoai LIKE ?
                ORDER BY hoTen
                """;
        return query(sql, "%" + (keyword == null ? "" : keyword.trim()) + "%");
    }

    @Override
    public void add(Customer customer) {
        String sql = """
                INSERT INTO KhachHang
                    (maKhachHang, hoTen, soDienThoai, email, diemTichLuy, diaChi, ghiChu)
                VALUES (?, ?, ?, ?, 0, ?, ?)
                """;
        String code = customer.getMaKhachHang();
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);
            statement.setString(2, customer.getHoTen().trim());
            statement.setString(3, customer.getSoDienThoai().trim());
            statement.setString(4, nullable(customer.getEmail()));
            statement.setString(5, nullable(customer.getDiaChi()));
            statement.setString(6, nullable(customer.getGhiChu()));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể thêm khách hàng. Hãy kiểm tra số điện thoại đã tồn tại chưa.",
                    exception);
        }
    }

    @Override
    public void update(Customer customer) {
        String sql = """
                UPDATE KhachHang SET hoTen = ?, soDienThoai = ?, email = ?,
                    diaChi = ?, ghiChu = ?
                WHERE maKhachHang = ?
                """;
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, customer.getHoTen().trim());
            statement.setString(2, customer.getSoDienThoai().trim());
            statement.setString(3, nullable(customer.getEmail()));
            statement.setString(4, nullable(customer.getDiaChi()));
            statement.setString(5, nullable(customer.getGhiChu()));
            statement.setString(6, customer.getMaKhachHang());
            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException("Không tìm thấy khách hàng cần cập nhật.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể cập nhật khách hàng.", exception);
        }
    }

    @Override
    public void deactivate(String id) {
        throw new UnsupportedOperationException(
                "Schema hiện tại không có trạng thái ngừng cho khách hàng.");
    }

    private List<Customer> query(String sql, String pattern) {
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (pattern != null) {
                for (int i = 1; i <= 3; i++) {
                    statement.setString(i, pattern);
                }
            }
            try (ResultSet rows = statement.executeQuery()) {
                List<Customer> result = new ArrayList<>();
                while (rows.next()) {
                    result.add(new Customer(rows.getString("maKhachHang"),
                            rows.getString("hoTen"), rows.getString("soDienThoai"),
                            rows.getString("email"), rows.getInt("diemTichLuy"),
                            rows.getString("diaChi"), rows.getString("ghiChu")));
                }
                return List.copyOf(result);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể tải dữ liệu khách hàng.", exception);
        }
    }

    private String nullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
