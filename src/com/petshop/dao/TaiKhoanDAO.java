package com.petshop.dao;

import com.petshop.entity.NhanVien;
import com.petshop.entity.TaiKhoan;
import com.petshop.util.DatabaseManager;
import com.petshop.util.HashUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp DAO truy xuất dữ liệu bảng TaiKhoan và NhanVien trong CSDL SQL Server.
 * Tuân thủ Điều 3 và Điều 5 IUH:
 * - Đăng nhập bằng Mã nhân viên (maNhanVien) hoặc tên đăng nhập.
 * - Mật khẩu được băm bằng thuật toán SHA-256 qua HashUtil.sha256().
 * - Tự động nạp Object Reference NhanVien vào TaiKhoan phục vụ SessionManager.
 */
public class TaiKhoanDAO {

    private static boolean adminDaKhoiTao = false;

    /**
     * Xác thực thông tin đăng nhập từ CSDL theo Mã nhân viên (hoặc Tên đăng nhập).
     * Mật khẩu được kiểm tra theo chuẩn mã hóa SHA-256.
     *
     * @param maNhanVienHoacTenDangNhap Mã nhân viên (ví dụ NV-20261005-000000) hoặc tên đăng nhập
     * @param matKhau Mật khẩu thuần người dùng nhập
     * @return TaiKhoan kèm Object Reference NhanVien nếu hợp lệ, null nếu sai thông tin hoặc bị khóa.
     */
    public TaiKhoan dangNhap(String maNhanVienHoacTenDangNhap, String matKhau) {
        khoiTaoTaiKhoanAdmin();

        if (maNhanVienHoacTenDangNhap == null || maNhanVienHoacTenDangNhap.isBlank() || matKhau == null) {
            return null;
        }

        String input = maNhanVienHoacTenDangNhap.trim();
        String passHash = HashUtil.sha256(matKhau);

        String sql = """
                SELECT tk.maTaiKhoan, tk.maNhanVien, tk.tenDangNhap, tk.matKhau, tk.phanQuyen, tk.trangThai,
                       nv.hoTen, nv.soDienThoai, nv.chucVu, nv.ngaySinh, nv.gioiTinh, nv.trangThai AS trangThaiNhanVien
                FROM TaiKhoan tk
                JOIN NhanVien nv ON nv.maNhanVien = tk.maNhanVien
                WHERE (tk.maNhanVien = ? OR LOWER(tk.tenDangNhap) = LOWER(?))
                  AND tk.trangThai = 1 AND nv.trangThai = N'Đang làm'
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, input);
            ps.setString(2, input);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String dbPass = rs.getString("matKhau");
                    boolean passHopLe = false;

                    // 1. So khớp mật khẩu băm SHA-256
                    if (passHash.equalsIgnoreCase(dbPass)) {
                        passHopLe = true;
                    } 
                    // 2. Tự động tương thích ngược dữ liệu demo cũ chưa băm và nâng cấp lên SHA-256
                    else if (matKhau.equals(dbPass)) {
                        passHopLe = true;
                        nangCapMatKhauSha256(rs.getString("maTaiKhoan"), passHash);
                    }

                    if (passHopLe) {
                        NhanVien nv = new NhanVien(
                                rs.getString("maNhanVien"),
                                rs.getString("hoTen"),
                                rs.getString("soDienThoai"),
                                rs.getString("chucVu"),
                                rs.getDate("ngaySinh") != null ? rs.getDate("ngaySinh").toLocalDate() : null,
                                rs.getBoolean("gioiTinh"),
                                rs.getString("trangThaiNhanVien")
                        );

                        TaiKhoan tk = new TaiKhoan(
                                rs.getString("maNhanVien"),
                                rs.getString("tenDangNhap"),
                                rs.getString("hoTen"),
                                TaiKhoan.VaiTro.tuTenHienThi(rs.getString("phanQuyen"))
                        );
                        tk.setMaTaiKhoan(rs.getString("maTaiKhoan"));
                        tk.setTrangThai(rs.getBoolean("trangThai"));
                        tk.setNhanVien(nv); // Gán Object Reference chuẩn Điều 2 & 5 IUH

                        return tk;
                    }
                }
                return null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi kết nối hoặc truy vấn cơ sở dữ liệu: " + e.getMessage(), e);
        }
    }

    /**
     * Nâng cấp mật khẩu dạng văn bản thuần sang chuỗi băm SHA-256 trong CSDL.
     */
    private void nangCapMatKhauSha256(String maTaiKhoan, String hashSha256) {
        String sql = "UPDATE TaiKhoan SET matKhau = ? WHERE maTaiKhoan = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashSha256);
            ps.setString(2, maTaiKhoan);
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    /**
     * Khôi phục mật khẩu (Quên mật khẩu): Xác thực Mã NV và Số điện thoại khớp trong CSDL
     * rồi cấp lại mật khẩu mới được băm bằng SHA-256 (Điều 5 IUH).
     */
    public boolean khoiPhucMatKhau(String maNhanVien, String soDienThoai, String matKhauMoi) {
        if (maNhanVien == null || soDienThoai == null || matKhauMoi == null) {
            return false;
        }

        String sqlKiemTra = """
                SELECT tk.maTaiKhoan
                FROM TaiKhoan tk
                JOIN NhanVien nv ON nv.maNhanVien = tk.maNhanVien
                WHERE tk.maNhanVien = ? AND nv.soDienThoai = ?
                  AND tk.trangThai = 1 AND nv.trangThai = N'Đang làm'
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            String maTaiKhoan = null;
            try (PreparedStatement ps = conn.prepareStatement(sqlKiemTra)) {
                ps.setString(1, maNhanVien.trim());
                ps.setString(2, soDienThoai.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        maTaiKhoan = rs.getString("maTaiKhoan");
                    }
                }
            }

            if (maTaiKhoan != null) {
                String hashMoi = HashUtil.sha256(matKhauMoi);
                String sqlUpdate = "UPDATE TaiKhoan SET matKhau = ? WHERE maTaiKhoan = ?";
                try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate)) {
                    psUpdate.setString(1, hashMoi);
                    psUpdate.setString(2, maTaiKhoan);
                    return psUpdate.executeUpdate() > 0;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi khôi phục mật khẩu: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật mật khẩu mới cho tài khoản (tự động băm SHA-256).
     */
    public boolean doiMatKhau(String maTaiKhoan, String matKhauMoi) {
        String sql = "UPDATE TaiKhoan SET matKhau = ? WHERE maTaiKhoan = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, HashUtil.sha256(matKhauMoi));
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
     * Reset mật khẩu tài khoản về mật khẩu mặc định (tự động băm SHA-256).
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
     * Đảm bảo tài khoản quản trị mặc định (admin / admin123) tồn tại trong CSDL với mật khẩu đã băm SHA-256.
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

            // Tạo tài khoản admin với mật khẩu băm SHA-256 (admin123 -> SHA-256)
            String hashAdmin = HashUtil.sha256("admin123");
            String insertTk = """
                IF NOT EXISTS (SELECT 1 FROM TaiKhoan WHERE tenDangNhap = 'admin')
                BEGIN
                    INSERT INTO TaiKhoan (maTaiKhoan, maNhanVien, tenDangNhap, matKhau, phanQuyen, trangThai)
                    VALUES ('TK-20261005-000000', 'NV-20261005-000000', 'admin', ?, N'Admin', 1);
                END
            """;
            try (PreparedStatement ps = conn.prepareStatement(insertTk)) {
                ps.setString(1, hashAdmin);
                ps.executeUpdate();
            }

            adminDaKhoiTao = true;
        } catch (Exception e) {
            // Ghi nhận lỗi nhẹ nếu CSDL chưa sẵn sàng
        }
    }
}
