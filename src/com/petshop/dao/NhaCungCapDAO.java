package com.petshop.dao;

import com.petshop.model.NhaCungCap;
import com.petshop.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp DAO truy xuất dữ liệu bảng NhaCungCap trong CSDL SQL Server.
 * Tuân thủ phong cách thiết kế của KhuyenMaiDAO và Quy ước #60.
 */
public class NhaCungCapDAO {

    private static final DateTimeFormatter DINH_DANG_NGAY_MA = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Sinh mã nhà cung cấp theo định dạng NCC-yyyyMMdd-xxxxxx.
     */
    public String taoMaNhaCungCap() {
        String ngay = LocalDate.now().format(DINH_DANG_NGAY_MA);
        String tienTo = "NCC-" + ngay + "-";
        String sql = "SELECT TOP 1 maNhaCungCap FROM NhaCungCap WHERE maNhaCungCap LIKE ? ORDER BY maNhaCungCap DESC";

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                return tienTo + "000001";
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, tienTo + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String maCu = rs.getString("maNhaCungCap");
                        String[] phan = maCu.split("-");
                        if (phan.length == 3) {
                            int soThuTu = Integer.parseInt(phan[2]) + 1;
                            return tienTo + String.format("%06d", soThuTu);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi sinh mã nhà cung cấp: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Mã nhà cung cấp trong cơ sở dữ liệu không đúng định dạng", e);
        }

        return tienTo + "000001";
    }

    /**
     * Thêm nhà cung cấp mới.
     */
    public boolean themNhaCungCap(NhaCungCap nhaCungCap) {
        String sql = """
                INSERT INTO NhaCungCap (maNhaCungCap, tenNhaCungCap, soDienThoai, diaChi, email, trangThai)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) return false;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, nhaCungCap.getMaNhaCungCap());
                ps.setString(2, nhaCungCap.getTenNhaCungCap());
                ps.setString(3, nhaCungCap.getSoDienThoai());
                ps.setString(4, nhaCungCap.getDiaChi());
                ps.setString(5, nhaCungCap.getEmail());
                ps.setBoolean(6, nhaCungCap.isTrangThai());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi thêm nhà cung cấp: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật thông tin nhà cung cấp theo mã.
     */
    public boolean capNhatNhaCungCap(NhaCungCap nhaCungCap) {
        String sql = """
                UPDATE NhaCungCap
                SET tenNhaCungCap = ?, soDienThoai = ?, diaChi = ?, email = ?, trangThai = ?
                WHERE maNhaCungCap = ?
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) return false;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, nhaCungCap.getTenNhaCungCap());
                ps.setString(2, nhaCungCap.getSoDienThoai());
                ps.setString(3, nhaCungCap.getDiaChi());
                ps.setString(4, nhaCungCap.getEmail());
                ps.setBoolean(5, nhaCungCap.isTrangThai());
                ps.setString(6, nhaCungCap.getMaNhaCungCap());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi cập nhật nhà cung cấp: " + e.getMessage(), e);
        }
    }

    /** Cập nhật trạng thái hợp tác mà không thay đổi trạng thái sản phẩm đã liên kết. */
    public boolean capNhatTrangThaiNhaCungCap(String maNhaCungCap, boolean trangThai) {
        String sql = "UPDATE NhaCungCap SET trangThai = ? WHERE maNhaCungCap = ?";
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) return false;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBoolean(1, trangThai);
                ps.setString(2, maNhaCungCap);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi cập nhật trạng thái nhà cung cấp: " + e.getMessage(), e);
        }
    }

    /** Xóa nhà cung cấp chỉ khi chưa liên kết sản phẩm hoặc phát sinh đơn nhập. */
    public boolean xoaNhaCungCap(String maNhaCungCap) {
        String sql = """
                DELETE FROM NhaCungCap
                WHERE maNhaCungCap = ?
                  AND NOT EXISTS (SELECT 1 FROM SanPham WHERE maNhaCungCap = ?)
                  AND NOT EXISTS (SELECT 1 FROM DonHang WHERE maNhaCungCap = ?)
                """;
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) return false;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maNhaCungCap);
                ps.setString(2, maNhaCungCap);
                ps.setString(3, maNhaCungCap);
                int affected = ps.executeUpdate();
                if (affected == 0) {
                    try (PreparedStatement check = conn.prepareStatement("SELECT 1 FROM NhaCungCap WHERE maNhaCungCap = ?")) {
                        check.setString(1, maNhaCungCap);
                        try (ResultSet rs = check.executeQuery()) {
                            if (!rs.next()) return false;
                            throw new IllegalStateException("Không thể xóa nhà cung cấp đã liên kết sản phẩm hoặc phát sinh đơn nhập. Hãy dùng chức năng Ngừng hợp tác.");
                        }
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 547) {
                throw new IllegalStateException("Không thể xóa nhà cung cấp đang được dữ liệu khác sử dụng. Hãy dùng chức năng Ngừng hợp tác.", e);
            }
            throw new IllegalStateException("Lỗi khi xóa nhà cung cấp: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy danh sách nhà cung cấp đang hoạt động (trangThai = 1).
     */
    public List<NhaCungCap> layDanhSachNhaCungCap() {
        List<NhaCungCap> danhSach = new ArrayList<>();
        String sql = "SELECT maNhaCungCap, tenNhaCungCap, soDienThoai, diaChi, email, trangThai "
                + "FROM NhaCungCap WHERE trangThai = 1 ORDER BY tenNhaCungCap ASC";

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) return danhSach;

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(docNhaCungCap(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi lấy danh sách nhà cung cấp: " + e.getMessage(), e);
        }

        return danhSach;
    }

    /**
     * Lấy toàn bộ danh sách nhà cung cấp kể cả đã ngừng hoạt động (phục vụ quản lý).
     */
    public List<NhaCungCap> layTatCaNhaCungCap() {
        List<NhaCungCap> danhSach = new ArrayList<>();
        String sql = "SELECT maNhaCungCap, tenNhaCungCap, soDienThoai, diaChi, email, trangThai "
                + "FROM NhaCungCap ORDER BY tenNhaCungCap ASC";

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) return danhSach;

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(docNhaCungCap(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi lấy tất cả nhà cung cấp: " + e.getMessage(), e);
        }

        return danhSach;
    }

    /**
     * Tìm kiếm nhà cung cấp theo từ khóa (khớp mã, tên, SĐT hoặc email), chỉ lấy NCC đang hoạt động (trangThai = 1).
     */
    public List<NhaCungCap> timKiemNhaCungCap(String tuKhoa) {
        List<NhaCungCap> danhSach = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT maNhaCungCap, tenNhaCungCap, soDienThoai, diaChi, email, trangThai "
                        + "FROM NhaCungCap WHERE trangThai = 1");

        boolean coTuKhoa = tuKhoa != null && !tuKhoa.trim().isEmpty();
        if (coTuKhoa) {
            sql.append(" AND (maNhaCungCap LIKE ? OR tenNhaCungCap LIKE ? OR soDienThoai LIKE ? OR email LIKE ?)");
        }
        sql.append(" ORDER BY tenNhaCungCap ASC");

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) return danhSach;

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                if (coTuKhoa) {
                    String pattern = "%" + tuKhoa.trim() + "%";
                    ps.setString(1, pattern);
                    ps.setString(2, pattern);
                    ps.setString(3, pattern);
                    ps.setString(4, pattern);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        danhSach.add(docNhaCungCap(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi tìm kiếm nhà cung cấp: " + e.getMessage(), e);
        }

        return danhSach;
    }

    /**
     * Lấy thông tin nhà cung cấp theo mã.
     */
    public NhaCungCap layTheoMa(String maNhaCungCap) {
        String sql = "SELECT maNhaCungCap, tenNhaCungCap, soDienThoai, diaChi, email, trangThai FROM NhaCungCap WHERE maNhaCungCap = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) return null;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maNhaCungCap);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return docNhaCungCap(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi lấy thông tin nhà cung cấp: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Kiểm tra số điện thoại có bị trùng với nhà cung cấp khác hay không.
     * @param soDienThoai Số điện thoại cần kiểm tra
     * @param maLoaiTru Mã nhà cung cấp bỏ qua (khi cập nhật), có thể null khi thêm mới
     */
    public boolean kiemTraTrungSoDienThoai(String soDienThoai, String maLoaiTru) {
        if (soDienThoai == null || soDienThoai.trim().isEmpty()) {
            return false;
        }

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM NhaCungCap WHERE soDienThoai = ?");
        boolean coLoaiTru = maLoaiTru != null && !maLoaiTru.trim().isEmpty();
        if (coLoaiTru) {
            sql.append(" AND maNhaCungCap <> ?");
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) return false;

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                ps.setString(1, soDienThoai.trim());
                if (coLoaiTru) {
                    ps.setString(2, maLoaiTru.trim());
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi kiểm tra trùng số điện thoại: " + e.getMessage(), e);
        }

        return false;
    }

    /**
     * Hàm ánh xạ dữ liệu từ ResultSet sang đối tượng NhaCungCap.
     */
    private NhaCungCap docNhaCungCap(ResultSet rs) throws SQLException {
        return new NhaCungCap(
                rs.getString("maNhaCungCap"),
                rs.getString("tenNhaCungCap"),
                rs.getString("soDienThoai"),
                rs.getString("diaChi"),
                rs.getString("email"),
                rs.getBoolean("trangThai")
        );
    }
}
