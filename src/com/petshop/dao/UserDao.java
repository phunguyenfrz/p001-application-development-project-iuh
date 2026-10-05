package com.petshop.dao;

import com.petshop.model.User;
import com.petshop.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO xử lý truy vấn database cho bảng TaiKhoan / NhanVien.
 * TODO: Implement các phương thức còn lại bên dưới.
 */
public class UserDao {

    /**
     * Tìm user theo username và password — dùng cho đăng nhập.
     * Chỉ chấp nhận tài khoản đang hoạt động của nhân viên đang làm việc.
     * TODO: Băm mật khẩu (BCrypt) thay vì so sánh chuỗi thô.
     *
     * @return User nếu hợp lệ, null nếu sai thông tin.
     * @throws IllegalStateException nếu không truy vấn được database.
     */
    public User findByCredentials(String username, String password) {
        String sql = """
                SELECT nv.maNhanVien, nv.hoTen, tk.tenDangNhap, tk.phanQuyen
                FROM TaiKhoan tk
                JOIN NhanVien nv ON nv.maNhanVien = tk.maNhanVien
                WHERE tk.tenDangNhap = ? AND tk.matKhau = ?
                  AND tk.trangThai = 1 AND nv.trangThai = N'Đang làm'
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getString("maNhanVien"),
                            rs.getString("tenDangNhap"),
                            rs.getString("hoTen"),
                            User.Role.fromDisplayName(rs.getString("phanQuyen")));
                }
                return null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không truy vấn được database: " + e.getMessage(), e);
        }
    }

    /** Lấy tất cả nhân viên. */
    public List<User> findAll() {
        // TODO: Implement
        return new ArrayList<>();
    }

    /** Thêm user mới. */
    public void save(User user, String password) {
        // TODO: Implement
    }

    /** Cập nhật thông tin user. */
    public void update(User user) {
        // TODO: Implement
    }

    /** Xóa user theo username. */
    public void delete(String username) {
        // TODO: Implement
    }
}
