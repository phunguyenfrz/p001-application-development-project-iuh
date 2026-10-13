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
        // Tự động kiểm tra và seed tài khoản admin nếu chưa có
        ensureAdminAccount();

        String sql = """
                SELECT nv.maNhanVien, nv.hoTen, tk.tenDangNhap, tk.phanQuyen
                FROM TaiKhoan tk
                JOIN NhanVien nv ON nv.maNhanVien = tk.maNhanVien
                WHERE LOWER(tk.tenDangNhap) = LOWER(?) AND tk.matKhau = ?
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

    private static boolean adminSeeded = false;

    /**
     * Khởi tạo tài khoản admin (admin / admin123) duy nhất một lần nếu chưa tồn tại trong CSDL.
     */
    public synchronized void ensureAdminAccount() {
        if (adminSeeded) return;
        try (Connection conn = DatabaseManager.getConnection()) {
            // Nới lỏng CHECK constraint trên cột phanQuyen nếu đang thiếu N'Admin'
            try (Statement alterStmt = conn.createStatement()) {
                alterStmt.executeUpdate("""
                    IF EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'CK__TaiKhoan__phanQu__5CD6CB2B' OR name LIKE '%TaiKhoan%phanQu%')
                    BEGIN
                        DECLARE @sql NVARCHAR(MAX) = N'';
                        SELECT @sql = @sql + N'ALTER TABLE TaiKhoan DROP CONSTRAINT ' + name + N';'
                        FROM sys.check_constraints
                        WHERE parent_object_id = OBJECT_ID('TaiKhoan') AND definition LIKE '%phanQuyen%';
                        IF LEN(@sql) > 0 EXEC sp_executesql @sql;
                        ALTER TABLE TaiKhoan ADD CONSTRAINT CK_TaiKhoan_PhanQuyen 
                            CHECK (phanQuyen IN (N'Admin', N'Quản lý', N'Bán hàng', N'Chăm sóc'));
                    END
                """);
            } catch (Exception ignored) {}

            // Kiểm tra tài khoản admin đã tồn tại chưa
            String checkSql = "SELECT COUNT(*) FROM TaiKhoan WHERE LOWER(tenDangNhap) = 'admin'";
            try (PreparedStatement ps = conn.prepareStatement(checkSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    adminSeeded = true;
                    return;
                }
            }

            // Tạo NhanVien admin nếu chưa có
            String insertNv = """
                IF NOT EXISTS (SELECT 1 FROM NhanVien WHERE maNhanVien = 'NV-20261005-000000')
                BEGIN
                    INSERT INTO NhanVien (maNhanVien, hoTen, soDienThoai, chucVu, ngaySinh, gioiTinh, trangThai)
                    VALUES ('NV-20261005-000000', N'Quản trị hệ thống', '0909000000', N'Quản lý', '1990-01-01', 1, N'Đang làm');
                END
            """;
            try (Statement st = conn.createStatement()) {
                st.executeUpdate(insertNv);
            }

            // Tạo TaiKhoan admin
            String insertTk = """
                INSERT INTO TaiKhoan (maTaiKhoan, maNhanVien, tenDangNhap, matKhau, phanQuyen, trangThai)
                VALUES ('TK-20261005-000000', 'NV-20261005-000000', 'admin', 'admin123', N'Admin', 1)
            """;
            try (Statement st = conn.createStatement()) {
                st.executeUpdate(insertTk);
            }

            adminSeeded = true;
        } catch (SQLException e) {
            // Không làm sập ứng dụng nếu DB không cho phép DDL, vẫn cho chạy tiếp
            System.err.println("Notice ensureAdminAccount: " + e.getMessage());
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
