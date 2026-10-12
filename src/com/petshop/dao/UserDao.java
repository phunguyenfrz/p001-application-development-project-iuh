package com.petshop.dao;

import com.petshop.model.User;
import com.petshop.model.Role;
import com.petshop.util.DbConnection;
import com.petshop.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO xử lý truy vấn database cho bảng TaiKhoan / NhanVien.
 * TODO: Implement các phương thức còn lại bên dưới.
 */
public class UserDao implements UserRepository {

    /**
     * Tìm user theo username và xác minh password đã băm — dùng cho đăng nhập.
     * Chỉ chấp nhận tài khoản đang hoạt động của nhân viên đang làm việc.
     *
     * @return User nếu hợp lệ, null nếu sai thông tin.
     * @throws IllegalStateException nếu không truy vấn được database.
     */
    public User findByCredentials(String username, String password) {
        String sql = """
                SELECT tk.maTaiKhoan, nv.maNhanVien, nv.hoTen, tk.tenDangNhap,
                       tk.matKhau, tk.phanQuyen
                FROM TaiKhoan tk
                JOIN NhanVien nv ON nv.maNhanVien = tk.maNhanVien
                WHERE tk.tenDangNhap = ?
                  AND tk.trangThai = 1 AND nv.trangThai = N'Đang làm'
                """;
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && PasswordUtil.verify(password, rs.getString("matKhau"))) {
                    Role role;
                    try {
                        role = Role.fromDbValue(rs.getString("phanQuyen"));
                    } catch (IllegalArgumentException exception) {
                        return null;
                    }
                    return new User(
                            rs.getString("maTaiKhoan"),
                            rs.getString("maNhanVien"),
                            rs.getString("tenDangNhap"),
                            rs.getString("hoTen"),
                            role);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể kiểm tra thông tin đăng nhập.", e);
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
