package com.petshop.dao;

import com.petshop.model.TaiKhoan;
import com.petshop.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp DAO truy xuất dữ liệu bảng TaiKhoan và NhanVien trong CSDL SQL Server.
 */
public class TaiKhoanDAO {

    private static boolean adminDaKhoiTao = false;

    /**
     * Xác thực thông tin đăng nhập từ CSDL.
     * Chỉ chấp nhận tài khoản đang hoạt động (trangThai = 1) của nhân viên đang làm việc.
     *
     * @param tenDangNhap Tên tài khoản
     * @param matKhau Mật khẩu
     * @return TaiKhoan nếu đăng nhập hợp lệ, null nếu sai thông tin hoặc bị khóa.
     */
    public TaiKhoan dangNhap(String tenDangNhap, String matKhau) {
        khoiTaoTaiKhoanAdmin();

        String sql = """
                SELECT tk.maTaiKhoan, tk.maNhanVien, tk.tenDangNhap, tk.phanQuyen, tk.trangThai, nv.hoTen
                FROM TaiKhoan tk
                JOIN NhanVien nv ON nv.maNhanVien = tk.maNhanVien
                WHERE LOWER(tk.tenDangNhap) = LOWER(?) AND tk.matKhau = ?
                  AND tk.trangThai = 1 AND nv.trangThai = N'Đang làm'
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDangNhap);
            ps.setString(2, matKhau);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TaiKhoan tk = new TaiKhoan(
                            rs.getString("maNhanVien"),
                            rs.getString("tenDangNhap"),
                            rs.getString("hoTen"),
                            TaiKhoan.VaiTro.tuTenHienThi(rs.getString("phanQuyen"))
                    );
                    tk.setMaTaiKhoan(rs.getString("maTaiKhoan"));
                    tk.setTrangThai(rs.getBoolean("trangThai"));
                    return tk;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi kết nối hoặc truy vấn cơ sở dữ liệu: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật mật khẩu mới cho tài khoản.
     */
    public boolean doiMatKhau(String maTaiKhoan, String matKhauMoi) {
        String sql = "UPDATE TaiKhoan SET matKhau = ? WHERE maTaiKhoan = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matKhauMoi);
            ps.setString(2, maTaiKhoan);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Khóa tài khoản đăng nhập (trangThai = 0).
     */
    public boolean khoaTaiKhoan(String maTaiKhoan) {
        String sql = "UPDATE TaiKhoan SET trangThai = 0 WHERE maTaiKhoan = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maTaiKhoan);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Mở khóa tài khoản đăng nhập (trangThai = 1).
     */
    public boolean moKhoaTaiKhoan(String maTaiKhoan) {
        String sql = "UPDATE TaiKhoan SET trangThai = 1 WHERE maTaiKhoan = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maTaiKhoan);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật phân quyền tài khoản (Admin, Quản lý, Bán hàng, Chăm sóc).
     */
    public boolean capNhatQuyen(String maTaiKhoan, String quyenMoi) {
        String sql = "UPDATE TaiKhoan SET phanQuyen = ? WHERE maTaiKhoan = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, quyenMoi);
            ps.setString(2, maTaiKhoan);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Reset mật khẩu tài khoản về mật khẩu mặc định.
     */
    public boolean resetMatKhau(String maTaiKhoan, String matKhauMacDinh) {
        return doiMatKhau(maTaiKhoan, matKhauMacDinh);
    }

    /**
     * Lấy toàn bộ danh sách tài khoản kèm tên nhân viên.
     */
    public List<TaiKhoan> layDanhSachTaiKhoan() {
        List<TaiKhoan> danhSach = new ArrayList<>();
        String sql = """
                SELECT tk.maTaiKhoan, tk.maNhanVien, tk.tenDangNhap, tk.phanQuyen, tk.trangThai, nv.hoTen
                FROM TaiKhoan tk
                JOIN NhanVien nv ON nv.maNhanVien = tk.maNhanVien
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                TaiKhoan tk = new TaiKhoan(
                        rs.getString("maNhanVien"),
                        rs.getString("tenDangNhap"),
                        rs.getString("hoTen"),
                        TaiKhoan.VaiTro.tuTenHienThi(rs.getString("phanQuyen"))
                );
                tk.setMaTaiKhoan(rs.getString("maTaiKhoan"));
                tk.setTrangThai(rs.getBoolean("trangThai"));
                danhSach.add(tk);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return danhSach;
    }

    /**
     * Đảm bảo tài khoản quản trị mặc định (admin / admin123) tồn tại trong CSDL.
     */
    public synchronized void khoiTaoTaiKhoanAdmin() {
        if (adminDaKhoiTao) return;
        try (Connection conn = DatabaseManager.getConnection()) {
            // Kiểm tra tài khoản admin đã có chưa
            String checkSql = "SELECT COUNT(*) FROM TaiKhoan WHERE LOWER(tenDangNhap) = 'admin'";
            try (PreparedStatement ps = conn.prepareStatement(checkSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    adminDaKhoiTao = true;
                    return;
                }
            }

            // Tạo nhân viên admin nếu chưa có
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

            // Tạo tài khoản admin
            String insertTk = """
                IF NOT EXISTS (SELECT 1 FROM TaiKhoan WHERE tenDangNhap = 'admin')
                BEGIN
                    INSERT INTO TaiKhoan (maTaiKhoan, maNhanVien, tenDangNhap, matKhau, phanQuyen, trangThai)
                    VALUES ('TK-20261005-000000', 'NV-20261005-000000', 'admin', 'admin123', N'Admin', 1);
                END
            """;
            try (Statement st = conn.createStatement()) {
                st.executeUpdate(insertTk);
            }

            adminDaKhoiTao = true;
        } catch (Exception e) {
            // Ghi nhận lỗi nhẹ nếu CSDL chưa sẵn sàng
        }
    }
}

