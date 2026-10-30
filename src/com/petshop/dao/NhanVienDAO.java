package com.petshop.dao;

import com.petshop.model.NhanVien;
import com.petshop.model.TaiKhoan;
import com.petshop.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp DAO phụ trách truy xuất dữ liệu nhân viên và tài khoản liên kết trong CSDL SQL Server.
 */
public class NhanVienDAO {

    private static final DateTimeFormatter DINH_DANG_NGAY_MA = DateTimeFormatter.ofPattern("yyyyMMdd");

    // Tự động sinh mã nhân viên mới theo định dạng: NV-yyyyMMdd-xxxxxx
    public String taoMaNhanVien() {
        String ngay = LocalDate.now().format(DINH_DANG_NGAY_MA);
        String tienTo = "NV-" + ngay + "-";
        String sql = "SELECT TOP 1 maNhanVien FROM NhanVien WHERE maNhanVien LIKE ? ORDER BY maNhanVien DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tienTo + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String maCu = rs.getString("maNhanVien");
                    String[] phan = maCu.split("-");
                    if (phan.length == 3) {
                        int soThuTu = Integer.parseInt(phan[2]) + 1;
                        return tienTo + String.format("%06d", soThuTu);
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi sinh mã nhân viên: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Mã nhân viên trong CSDL không đúng định dạng", e);
        }

        return tienTo + "000001";
    }

    // Tự động sinh mã tài khoản mới theo định dạng: TK-yyyyMMdd-xxxxxx
    public String taoMaTaiKhoan() {
        String ngay = LocalDate.now().format(DINH_DANG_NGAY_MA);
        String tienTo = "TK-" + ngay + "-";
        String sql = "SELECT TOP 1 maTaiKhoan FROM TaiKhoan WHERE maTaiKhoan LIKE ? ORDER BY maTaiKhoan DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tienTo + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String maCu = rs.getString("maTaiKhoan");
                    String[] phan = maCu.split("-");
                    if (phan.length == 3) {
                        int soThuTu = Integer.parseInt(phan[2]) + 1;
                        return tienTo + String.format("%06d", soThuTu);
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi sinh mã tài khoản: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Mã tài khoản trong CSDL không đúng định dạng", e);
        }

        return tienTo + "000001";
    }

    // Lấy toàn bộ danh sách nhân viên kèm thông tin tài khoản
    public List<NhanVien> layDanhSachNhanVien() {
        return timKiemNhanVien(null, null, null);
    }

    // Tìm kiếm và lọc danh sách nhân viên theo từ khóa (Mã/Tên/SĐT), Chức vụ và Trạng thái
    public List<NhanVien> timKiemNhanVien(String tuKhoa, String chucVu, String trangThai) {
        StringBuilder sql = new StringBuilder("""
                SELECT nv.maNhanVien, nv.hoTen, nv.soDienThoai, nv.chucVu, nv.ngaySinh, nv.gioiTinh, nv.trangThai,
                       tk.maTaiKhoan, tk.tenDangNhap, tk.phanQuyen, tk.trangThai AS trangThaiTaiKhoan
                FROM NhanVien nv
                LEFT JOIN TaiKhoan tk ON tk.maNhanVien = nv.maNhanVien
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            String pattern = "%" + tuKhoa.trim() + "%";
            sql.append(" AND (nv.maNhanVien LIKE ? OR nv.hoTen LIKE ? OR nv.soDienThoai LIKE ? OR tk.tenDangNhap LIKE ?)");
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        if (chucVu != null && !chucVu.trim().isEmpty() && !"Tất cả".equalsIgnoreCase(chucVu.trim())) {
            sql.append(" AND nv.chucVu = ?");
            params.add(chucVu.trim());
        }

        if (trangThai != null && !trangThai.trim().isEmpty() && !"Tất cả".equalsIgnoreCase(trangThai.trim())) {
            sql.append(" AND nv.trangThai = ?");
            params.add(trangThai.trim());
        }

        sql.append(" ORDER BY nv.trangThai ASC, nv.maNhanVien DESC");

        List<NhanVien> danhSach = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(docNhanVien(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi tìm kiếm nhân viên: " + e.getMessage(), e);
        }
        return danhSach;
    }

    // Lấy thông tin một nhân viên theo mã
    public NhanVien layNhanVienTheoMa(String maNhanVien) {
        String sql = """
                SELECT nv.maNhanVien, nv.hoTen, nv.soDienThoai, nv.chucVu, nv.ngaySinh, nv.gioiTinh, nv.trangThai,
                       tk.maTaiKhoan, tk.tenDangNhap, tk.phanQuyen, tk.trangThai AS trangThaiTaiKhoan
                FROM NhanVien nv
                LEFT JOIN TaiKhoan tk ON tk.maNhanVien = nv.maNhanVien
                WHERE nv.maNhanVien = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNhanVien);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return docNhanVien(rs);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi lấy thông tin nhân viên theo mã: " + e.getMessage(), e);
        }
        return null;
    }

    // Kiểm tra trùng lặp số điện thoại (bỏ qua mã nhân viên đang cập nhật nếu có)
    public boolean kiemTraTrungSoDienThoai(String soDienThoai, String maNhanVienBoQua) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM NhanVien WHERE soDienThoai = ?");
        if (maNhanVienBoQua != null && !maNhanVienBoQua.trim().isEmpty()) {
            sql.append(" AND maNhanVien != ?");
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, soDienThoai);
            if (maNhanVienBoQua != null && !maNhanVienBoQua.trim().isEmpty()) {
                ps.setString(2, maNhanVienBoQua.trim());
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi kiểm tra số điện thoại: " + e.getMessage(), e);
        }
        return false;
    }

    // Cập nhật thông tin nhân viên (họ tên, SĐT, chức vụ, ngày sinh, giới tính)
    public boolean capNhatNhanVien(NhanVien nv) {
        String sql = """
                UPDATE NhanVien
                SET hoTen = ?, soDienThoai = ?, chucVu = ?, ngaySinh = ?, gioiTinh = ?, trangThai = ?
                WHERE maNhanVien = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nv.getHoTen());
            ps.setString(2, nv.getSoDienThoai());
            ps.setString(3, nv.getChucVu());
            ps.setDate(4, Date.valueOf(nv.getNgaySinh()));
            ps.setBoolean(5, nv.isGioiTinh());
            ps.setString(6, nv.getTrangThai());
            ps.setString(7, nv.getMaNhanVien());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi cập nhật thông tin nhân viên: " + e.getMessage(), e);
        }
    }

    // Cập nhật phân quyền và trạng thái khóa/mở khóa tài khoản liên kết (EMP_ROLES, EMP_ACCOUNT)
    public boolean capNhatTaiKhoan(String maNhanVien, String vaiTroMoi, boolean trangThaiHoatDong) {
        String sqlCheck = "SELECT maTaiKhoan FROM TaiKhoan WHERE maNhanVien = ?";
        String sqlUpdate = "UPDATE TaiKhoan SET phanQuyen = ?, trangThai = ? WHERE maNhanVien = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {
                psCheck.setString(1, maNhanVien);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        try (PreparedStatement psUp = conn.prepareStatement(sqlUpdate)) {
                            psUp.setString(1, vaiTroMoi);
                            psUp.setBoolean(2, trangThaiHoatDong);
                            psUp.setString(3, maNhanVien);
                            return psUp.executeUpdate() > 0;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi cập nhật tài khoản nhân viên: " + e.getMessage(), e);
        }
        return false;
    }

    // Khóa / Mở khóa tài khoản theo mã nhân viên
    public boolean doiTrangThaiTaiKhoan(String maNhanVien, boolean trangThaiMoi) {
        String sql = "UPDATE TaiKhoan SET trangThai = ? WHERE maNhanVien = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, trangThaiMoi);
            ps.setString(2, maNhanVien);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi đổi trạng thái tài khoản: " + e.getMessage(), e);
        }
    }

    // Xóa mềm nhân viên: Đổi trạng thái sang "Đã nghỉ việc" và tự động khóa tài khoản
    public boolean ngungLamViec(String maNhanVien) {
        String sqlNv = "UPDATE NhanVien SET trangThai = N'Đã nghỉ việc' WHERE maNhanVien = ?";
        String sqlTk = "UPDATE TaiKhoan SET trangThai = 0 WHERE maNhanVien = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement psNv = conn.prepareStatement(sqlNv)) {
                    psNv.setString(1, maNhanVien);
                    psNv.executeUpdate();
                }

                try (PreparedStatement psTk = conn.prepareStatement(sqlTk)) {
                    psTk.setString(1, maNhanVien);
                    psTk.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi ngừng làm việc nhân viên: " + e.getMessage(), e);
        }
    }

    // Khôi phục làm việc: Đổi trạng thái nhân viên sang "Đang làm" (tùy chọn mở lại tài khoản)
    public boolean khoiPhucLamViec(String maNhanVien, boolean moLaiTaiKhoan) {
        String sqlNv = "UPDATE NhanVien SET trangThai = N'Đang làm' WHERE maNhanVien = ?";
        String sqlTk = "UPDATE TaiKhoan SET trangThai = 1 WHERE maNhanVien = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement psNv = conn.prepareStatement(sqlNv)) {
                    psNv.setString(1, maNhanVien);
                    psNv.executeUpdate();
                }

                if (moLaiTaiKhoan) {
                    try (PreparedStatement psTk = conn.prepareStatement(sqlTk)) {
                        psTk.setString(1, maNhanVien);
                        psTk.executeUpdate();
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi khôi phục trạng thái nhân viên: " + e.getMessage(), e);
        }
    }

    // Thêm nhân viên mới (chỉ thêm hồ sơ nhân sự)
    public boolean themNhanVien(NhanVien nv) {
        return themNhanVien(nv, null);
    }

    // Thêm nhân viên mới kèm tài khoản (Transaction an toàn chuẩn bị cho ThemNhanVienView)
    public boolean themNhanVien(NhanVien nv, TaiKhoan tk) {
        String sqlNv = """
                INSERT INTO NhanVien (maNhanVien, hoTen, soDienThoai, chucVu, ngaySinh, gioiTinh, trangThai)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        String sqlTk = """
                INSERT INTO TaiKhoan (maTaiKhoan, maNhanVien, tenDangNhap, matKhau, phanQuyen, trangThai)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement psNv = conn.prepareStatement(sqlNv)) {
                    psNv.setString(1, nv.getMaNhanVien());
                    psNv.setString(2, nv.getHoTen());
                    psNv.setString(3, nv.getSoDienThoai());
                    psNv.setString(4, nv.getChucVu());
                    psNv.setDate(5, Date.valueOf(nv.getNgaySinh()));
                    psNv.setBoolean(6, nv.isGioiTinh());
                    psNv.setString(7, nv.getTrangThai());
                    psNv.executeUpdate();
                }

                if (tk != null) {
                    try (PreparedStatement psTk = conn.prepareStatement(sqlTk)) {
                        psTk.setString(1, tk.getMaTaiKhoan());
                        psTk.setString(2, nv.getMaNhanVien());
                        psTk.setString(3, tk.getTenDangNhap());
                        psTk.setString(4, tk.getMatKhau());
                        psTk.setString(5, tk.getPhanQuyen().getTenHienThi());
                        psTk.setBoolean(6, tk.isTrangThai());
                        psTk.executeUpdate();
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi thêm nhân viên mới: " + e.getMessage(), e);
        }
    }

    // Tạo tài khoản mới cho nhân viên đã có trong hệ thống
    public boolean themTaiKhoanChoNhanVien(String maNhanVien, String tenDangNhap, String matKhau, String phanQuyen) {
        String maTk = taoMaTaiKhoan();
        String sql = """
                INSERT INTO TaiKhoan (maTaiKhoan, maNhanVien, tenDangNhap, matKhau, phanQuyen, trangThai)
                VALUES (?, ?, ?, ?, ?, 1)
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maTk);
            ps.setString(2, maNhanVien);
            ps.setString(3, tenDangNhap);
            ps.setString(4, matKhau);
            ps.setString(5, phanQuyen);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi tạo tài khoản đăng nhập: " + e.getMessage(), e);
        }
    }

    // Đặt lại mật khẩu tài khoản
    public boolean datLaiMatKhau(String maTaiKhoan, String matKhauMoi) {
        String sql = "UPDATE TaiKhoan SET matKhau = ? WHERE maTaiKhoan = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matKhauMoi);
            ps.setString(2, maTaiKhoan);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi đặt lại mật khẩu: " + e.getMessage(), e);
        }
    }

    // Xóa tài khoản đăng nhập
    public boolean xoaTaiKhoan(String maTaiKhoan) {
        String sqlCheck = "SELECT tenDangNhap FROM TaiKhoan WHERE maTaiKhoan = ?";
        String sqlDelete = "DELETE FROM TaiKhoan WHERE maTaiKhoan = ?";
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {
                psCheck.setString(1, maTaiKhoan);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        String user = rs.getString("tenDangNhap");
                        if ("admin".equalsIgnoreCase(user)) {
                            throw new IllegalArgumentException("Không được phép xóa tài khoản Quản trị viên (Admin)!");
                        }
                    }
                }
            }
            try (PreparedStatement psDel = conn.prepareStatement(sqlDelete)) {
                psDel.setString(1, maTaiKhoan);
                return psDel.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi xóa tài khoản: " + e.getMessage(), e);
        }
    }

    // Kiểm tra tên đăng nhập đã tồn tại chưa
    public boolean kiemTraTenDangNhapTonTai(String tenDangNhap) {
        String sql = "SELECT COUNT(*) FROM TaiKhoan WHERE LOWER(tenDangNhap) = LOWER(?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDangNhap);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    // Helper map ResultSet sang đối tượng NhanVien
    private NhanVien docNhanVien(ResultSet rs) throws SQLException {
        Date d = rs.getDate("ngaySinh");
        LocalDate ngaySinh = d != null ? d.toLocalDate() : null;

        NhanVien nv = new NhanVien(
                rs.getString("maNhanVien"),
                rs.getString("hoTen"),
                rs.getString("soDienThoai"),
                rs.getString("chucVu"),
                ngaySinh,
                rs.getBoolean("gioiTinh"),
                rs.getString("trangThai")
        );

        nv.setMaTaiKhoan(rs.getString("maTaiKhoan"));
        nv.setTenDangNhap(rs.getString("tenDangNhap"));
        nv.setPhanQuyen(rs.getString("phanQuyen"));

        Object ttTkObj = rs.getObject("trangThaiTaiKhoan");
        if (ttTkObj != null) {
            nv.setTrangThaiTaiKhoan(rs.getBoolean("trangThaiTaiKhoan"));
        } else {
            nv.setTrangThaiTaiKhoan(null);
        }

        return nv;
    }
}

