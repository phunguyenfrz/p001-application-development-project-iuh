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
 * Hỗ trợ xác thực đăng nhập qua Mã nhân viên, mật khẩu băm SHA-256 (IUH Chỉ thị 5),
 * và nghiệp vụ xác minh cấp lại mật khẩu (Quên mật khẩu).
 */
public class TaiKhoanDAO {

    private static boolean adminDaKhoiTao = false;

    /**
     * Xác thực thông tin đăng nhập từ CSDL.
     * Cho phép đăng nhập bằng Mã nhân viên (maNhanVien) hoặc Tên đăng nhập (tenDangNhap).
     * Mật khẩu được kiểm tra qua băm SHA-256 (và fallback plain text tương thích ngược).
     * Chỉ chấp nhận tài khoản đang hoạt động (trangThai = 1) của nhân viên đang làm việc.
     *
     * @param maNhanVienHoacTenDangNhap Mã nhân viên hoặc Tên tài khoản
     * @param matKhau Mật khẩu gốc chưa băm
     * @return TaiKhoan nếu đăng nhập hợp lệ, null nếu sai thông tin hoặc bị khóa.
     */
    public TaiKhoan dangNhap(String maNhanVienHoacTenDangNhap, String matKhau) {
        khoiTaoTaiKhoanAdmin();

        String matKhauHash = HashUtil.sha256(matKhau);
        String sql = """
                SELECT tk.maTaiKhoan, tk.maNhanVien, tk.tenDangNhap, tk.matKhau, tk.phanQuyen, tk.trangThai,
                       nv.hoTen, nv.chucVu, nv.soDienThoai
                FROM TaiKhoan tk
                JOIN NhanVien nv ON nv.maNhanVien = tk.maNhanVien
                WHERE (LOWER(tk.maNhanVien) = LOWER(?) OR LOWER(tk.tenDangNhap) = LOWER(?))
                  AND (tk.matKhau = ? OR tk.matKhau = ?)
                  AND tk.trangThai = 1 AND nv.trangThai = N'Đang làm'
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNhanVienHoacTenDangNhap);
            ps.setString(2, maNhanVienHoacTenDangNhap);
            ps.setString(3, matKhauHash);
            ps.setString(4, matKhau);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String matKhauHienTai = rs.getString("matKhau");
                    String maTaiKhoan = rs.getString("maTaiKhoan");

                    // Tự động nâng cấp mật khẩu sang SHA-256 nếu trong DB vẫn là dạng thô
                    if (matKhau.equals(matKhauHienTai) && !matKhauHash.equals(matKhauHienTai)) {
                        doiMatKhau(maTaiKhoan, matKhauHash);
                    }

                    NhanVien nv = new NhanVien();
                    nv.setMaNhanVien(rs.getString("maNhanVien"));
                    nv.setHoTen(rs.getString("hoTen"));
                    nv.setChucVu(rs.getString("chucVu"));
                    nv.setSoDienThoai(rs.getString("soDienThoai"));
                    nv.setTrangThai("Đang làm");

                    TaiKhoan tk = new TaiKhoan(
                            rs.getString("maNhanVien"),
                            rs.getString("tenDangNhap"),
                            rs.getString("hoTen"),
                            TaiKhoan.VaiTro.tuTenHienThi(rs.getString("phanQuyen"))
                    );
                    tk.setMaTaiKhoan(maTaiKhoan);
                    tk.setTrangThai(rs.getBoolean("trangThai"));
                    tk.setNhanVien(nv); // Object Reference
                    return tk;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi kết nối hoặc truy vấn cơ sở dữ liệu: " + e.getMessage(), e);
        }
    }

    /**
     * Kiểm tra thông tin xác thực cho chức năng Quên mật khẩu.
     * Yêu cầu Mã nhân viên và Số điện thoại phải khớp với hồ sơ nhân viên đang làm việc, tài khoản còn hoạt động.
     */
    public boolean kiemTraThongTinQuenMatKhau(String maNhanVien, String soDienThoai) {
        String sql = """
                SELECT 1
                FROM NhanVien nv
                JOIN TaiKhoan tk ON tk.maNhanVien = nv.maNhanVien
                WHERE (LOWER(nv.maNhanVien) = LOWER(?) OR LOWER(tk.tenDangNhap) = LOWER(?))
                  AND nv.soDienThoai = ?
                  AND nv.trangThai = N'Đang làm'
                  AND tk.trangThai = 1
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNhanVien);
            ps.setString(2, maNhanVien);
            ps.setString(3, soDienThoai);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi kiểm tra thông tin quên mật khẩu: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật mật khẩu mới (đã băm SHA-256) cho nhân viên theo mã nhân viên.
     */
    public boolean datLaiMatKhauTheoMaNhanVien(String maNhanVien, String matKhauMoiHash) {
        String sql = "UPDATE TaiKhoan SET matKhau = ? WHERE LOWER(maNhanVien) = LOWER(?) OR LOWER(tenDangNhap) = LOWER(?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matKhauMoiHash);
            ps.setString(2, maNhanVien);
            ps.setString(3, maNhanVien);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi cập nhật lại mật khẩu: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật mật khẩu mới cho tài khoản theo mã tài khoản.
     */
    public boolean doiMatKhau(String maTaiKhoan, String matKhauMoiHash) {
        String sql = "UPDATE TaiKhoan SET matKhau = ? WHERE maTaiKhoan = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matKhauMoiHash);
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
     * Reset mật khẩu tài khoản về mật khẩu mặc định (băm SHA-256).
     */
    public boolean resetMatKhau(String maTaiKhoan, String matKhauMacDinhHash) {
        return doiMatKhau(maTaiKhoan, matKhauMacDinhHash);
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
     * Đảm bảo tài khoản quản trị mặc định (admin hoặc 260000 / admin123 hash SHA-256) tồn tại trong CSDL.
     */
    public synchronized void khoiTaoTaiKhoanAdmin() {
        if (adminDaKhoiTao) return;
        try (Connection conn = DatabaseManager.getConnection()) {
            // Kiểm tra tài khoản admin đã có chưa
            String checkSql = "SELECT COUNT(*) FROM TaiKhoan WHERE LOWER(tenDangNhap) = 'admin' OR maNhanVien = '260000'";
            try (PreparedStatement ps = conn.prepareStatement(checkSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    adminDaKhoiTao = true;
                    return;
                }
            }

            // Tạo nhân viên admin nếu chưa có
            String insertNv = """
                IF NOT EXISTS (SELECT 1 FROM NhanVien WHERE maNhanVien IN ('260000', 'NV-20261005-000000'))
                BEGIN
                    INSERT INTO NhanVien (maNhanVien, hoTen, soDienThoai, chucVu, ngaySinh, gioiTinh, trangThai)
                    VALUES ('260000', N'Quản trị hệ thống', '0909000000', N'Quản lý', '1990-01-01', 1, N'Đang làm');
                END
            """;
            try (Statement st = conn.createStatement()) {
                st.executeUpdate(insertNv);
            }

            // Tạo tài khoản admin với mật khẩu băm SHA-256 (admin123)
            String hashAdmin = HashUtil.sha256("admin123");
            String insertTk = String.format("""
                IF NOT EXISTS (SELECT 1 FROM TaiKhoan WHERE tenDangNhap IN ('admin', '260000'))
                BEGIN
                    INSERT INTO TaiKhoan (maTaiKhoan, maNhanVien, tenDangNhap, matKhau, phanQuyen, trangThai)
                    VALUES ('TK-20261005-000000', '260000', '260000', '%s', N'Admin', 1);
                END
            """, hashAdmin);
            try (Statement st = conn.createStatement()) {
                st.executeUpdate(insertTk);
            }

            adminDaKhoiTao = true;
        } catch (Exception e) {
            // Ghi nhận lỗi nhẹ nếu CSDL chưa sẵn sàng
        }
    }
}
