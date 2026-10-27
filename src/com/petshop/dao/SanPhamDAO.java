package com.petshop.dao;

import com.petshop.model.SanPham;
import com.petshop.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp DAO truy xuất và thao tác dữ liệu bảng SanPham trong CSDL SQL Server.
 */
public class SanPhamDAO {

    /**
     * Tự động sinh mã sản phẩm theo định dạng SP-yyyyMMdd-xxxxxx.
     */
    public String taoMaSanPham() {
        String ngay = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String tienTo = "SP-" + ngay + "-";
        String sql = "SELECT TOP 1 maSanPham FROM SanPham WHERE maSanPham LIKE ? ORDER BY maSanPham DESC";

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                return tienTo + "000001";
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, tienTo + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String maCu = rs.getString("maSanPham");
                        String[] phan = maCu.split("-");
                        if (phan.length == 3) {
                            int stt = Integer.parseInt(phan[2]) + 1;
                            return tienTo + String.format("%06d", stt);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi sinh mã sản phẩm: " + e.getMessage(), e);
        }

        return tienTo + "000001";
    }

    /**
     * Thêm sản phẩm mới vào CSDL.
     */
    public boolean themSanPham(SanPham sp) {
        String sql = """
                INSERT INTO SanPham (maSanPham, maNhaCungCap, tenSanPham, maVach, giaBan, soLuongTon, nguongTonToiThieu, donViTinh, trangThai)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sp.getMaSanPham());
            if (sp.getMaNhaCungCap() != null && !sp.getMaNhaCungCap().isBlank()) {
                ps.setString(2, sp.getMaNhaCungCap());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setString(3, sp.getTenSanPham());
            ps.setString(4, sp.getMaVach());
            ps.setBigDecimal(5, sp.getGiaBan());
            ps.setInt(6, sp.getSoLuongTon());
            ps.setInt(7, sp.getNguongTonToiThieu());
            ps.setString(8, sp.getDonViTinh());
            ps.setBoolean(9, sp.isTrangThai());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi thêm sản phẩm vào CSDL: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật thông tin sản phẩm.
     */
    public boolean capNhatSanPham(SanPham sp) {
        String sql = """
                UPDATE SanPham
                SET maNhaCungCap = ?, tenSanPham = ?, maVach = ?, giaBan = ?,
                    soLuongTon = ?, nguongTonToiThieu = ?, donViTinh = ?, trangThai = ?
                WHERE maSanPham = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (sp.getMaNhaCungCap() != null && !sp.getMaNhaCungCap().isBlank()) {
                ps.setString(1, sp.getMaNhaCungCap());
            } else {
                ps.setNull(1, Types.VARCHAR);
            }
            ps.setString(2, sp.getTenSanPham());
            ps.setString(3, sp.getMaVach());
            ps.setBigDecimal(4, sp.getGiaBan());
            ps.setInt(5, sp.getSoLuongTon());
            ps.setInt(6, sp.getNguongTonToiThieu());
            ps.setString(7, sp.getDonViTinh());
            ps.setBoolean(8, sp.isTrangThai());
            ps.setString(9, sp.getMaSanPham());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi cập nhật sản phẩm: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa mềm sản phẩm (chuyển trạng thái = false - ngừng kinh doanh).
     */
    public boolean xoaMemSanPham(String maSanPham) {
        String sql = "UPDATE SanPham SET trangThai = 0 WHERE maSanPham = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSanPham);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi xóa mềm sản phẩm: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy toàn bộ danh sách sản phẩm kèm tên nhà cung cấp.
     */
    public List<SanPham> layDanhSachSanPham() {
        List<SanPham> danhSach = new ArrayList<>();
        String sql = """
                SELECT sp.maSanPham, sp.maNhaCungCap, sp.tenSanPham, sp.maVach, sp.giaBan,
                       sp.soLuongTon, sp.nguongTonToiThieu, sp.donViTinh, sp.trangThai,
                       ncc.tenNhaCungCap
                FROM SanPham sp
                LEFT JOIN NhaCungCap ncc ON ncc.maNhaCungCap = sp.maNhaCungCap
                ORDER BY sp.maSanPham DESC
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) return danhSach;

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(docDongSanPham(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi lấy danh sách sản phẩm: " + e.getMessage(), e);
        }

        return danhSach;
    }

    /**
     * Lấy sản phẩm theo mã.
     */
    public SanPham layTheoMa(String maSanPham) {
        String sql = """
                SELECT sp.maSanPham, sp.maNhaCungCap, sp.tenSanPham, sp.maVach, sp.giaBan,
                       sp.soLuongTon, sp.nguongTonToiThieu, sp.donViTinh, sp.trangThai,
                       ncc.tenNhaCungCap
                FROM SanPham sp
                LEFT JOIN NhaCungCap ncc ON ncc.maNhaCungCap = sp.maNhaCungCap
                WHERE sp.maSanPham = ?
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) return null;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maSanPham);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return docDongSanPham(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi lấy thông tin sản phẩm: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Tìm kiếm sản phẩm theo từ khóa (mã, tên hoặc mã vạch).
     */
    public List<SanPham> timKiemSanPham(String tuKhoa, Boolean trangThai) {
        List<SanPham> danhSach = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT sp.maSanPham, sp.maNhaCungCap, sp.tenSanPham, sp.maVach, sp.giaBan,
                       sp.soLuongTon, sp.nguongTonToiThieu, sp.donViTinh, sp.trangThai,
                       ncc.tenNhaCungCap
                FROM SanPham sp
                LEFT JOIN NhaCungCap ncc ON ncc.maNhaCungCap = sp.maNhaCungCap
                WHERE 1 = 1
                """);

        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            sql.append(" AND (LOWER(sp.maSanPham) LIKE LOWER(?) OR LOWER(sp.tenSanPham) LIKE LOWER(?) OR LOWER(sp.maVach) LIKE LOWER(?))");
        }
        if (trangThai != null) {
            sql.append(" AND sp.trangThai = ?");
        }
        sql.append(" ORDER BY sp.maSanPham DESC");

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) return danhSach;

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int paramIdx = 1;
                if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
                    String pattern = "%" + tuKhoa.trim() + "%";
                    ps.setString(paramIdx++, pattern);
                    ps.setString(paramIdx++, pattern);
                    ps.setString(paramIdx++, pattern);
                }
                if (trangThai != null) {
                    ps.setBoolean(paramIdx++, trangThai);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        danhSach.add(docDongSanPham(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi tìm kiếm sản phẩm: " + e.getMessage(), e);
        }

        return danhSach;
    }

    /**
     * Lấy danh sách sản phẩm dưới ngưỡng tồn an toàn (phục vụ PRD_LOW_STOCK và Issue #72).
     */
    public List<SanPham> layDanhSachSanPhamSapHet() {
        List<SanPham> danhSach = new ArrayList<>();
        String sql = """
                SELECT sp.maSanPham, sp.maNhaCungCap, sp.tenSanPham, sp.maVach, sp.giaBan,
                       sp.soLuongTon, sp.nguongTonToiThieu, sp.donViTinh, sp.trangThai,
                       ncc.tenNhaCungCap
                FROM SanPham sp
                LEFT JOIN NhaCungCap ncc ON ncc.maNhaCungCap = sp.maNhaCungCap
                WHERE sp.soLuongTon <= sp.nguongTonToiThieu AND sp.trangThai = 1
                ORDER BY sp.soLuongTon ASC
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) return danhSach;

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(docDongSanPham(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi lấy danh sách sản phẩm sắp hết: " + e.getMessage(), e);
        }

        return danhSach;
    }

    /**
     * Tra cứu số lượng tồn kho theo mã sản phẩm (phục vụ Issue #71 Hóa đơn).
     */
    public int traCuuTonKho(String maSanPham) {
        String sql = "SELECT soLuongTon FROM SanPham WHERE maSanPham = ?";
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) return 0;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maSanPham);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("soLuongTon");
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi tra cứu tồn kho: " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Cập nhật số lượng tồn kho (cộng thêm hoặc trừ bớt, phục vụ Issue #71 và #67).
     * @param maSanPham Mã sản phẩm
     * @param soLuongThayDoi Số lượng thay đổi (dương là cộng thêm tồn khi nhập, âm là trừ tồn khi bán)
     */
    public boolean capNhatSoLuongTon(String maSanPham, int soLuongThayDoi) {
        String sql = "UPDATE SanPham SET soLuongTon = soLuongTon + ? WHERE maSanPham = ? AND (soLuongTon + ? >= 0)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, soLuongThayDoi);
            ps.setString(2, maSanPham);
            ps.setInt(3, soLuongThayDoi);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi cập nhật số lượng tồn kho: " + e.getMessage(), e);
        }
    }

    private SanPham docDongSanPham(ResultSet rs) throws SQLException {
        SanPham sp = new SanPham(
                rs.getString("maSanPham"),
                rs.getString("maNhaCungCap"),
                rs.getString("tenSanPham"),
                rs.getString("maVach"),
                rs.getBigDecimal("giaBan"),
                rs.getInt("soLuongTon"),
                rs.getInt("nguongTonToiThieu"),
                rs.getString("donViTinh"),
                rs.getBoolean("trangThai")
        );
        sp.setTenNhaCungCap(rs.getString("tenNhaCungCap"));
        return sp;
    }
}

