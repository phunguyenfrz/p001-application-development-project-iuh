package com.petshop.dao;

import com.petshop.model.SanPham;
import com.petshop.util.DatabaseManager;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SanPhamDAO {

    public List<SanPham.NhaCungCapSimple> layDanhSachNhaCungCap() {
        String sql = "SELECT maNhaCungCap, tenNhaCungCap FROM NhaCungCap ORDER BY tenNhaCungCap";
        List<SanPham.NhaCungCapSimple> danhSach = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                danhSach.add(new SanPham.NhaCungCapSimple(
                        rs.getString("maNhaCungCap"),
                        rs.getString("tenNhaCungCap")));
            }
            return danhSach;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi tải danh sách nhà cung cấp: " + e.getMessage(), e);
        }
    }

    public String themSanPham(SanPham sp) {
        LocalDate homNay = LocalDate.now();
        String sqlThem = """
                INSERT INTO SanPham (maSanPham, maNhaCungCap, tenSanPham, maVach, giaBan, soLuongTon, nguongTonToiThieu, donViTinh, trangThai)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            conn.setAutoCommit(false);
            try {
                String maSanPham = taoMaSanPham(conn, homNay);
                try (PreparedStatement stmt = conn.prepareStatement(sqlThem)) {
                    stmt.setString(1, maSanPham);
                    stmt.setString(2, sp.getMaNhaCungCap());
                    stmt.setString(3, sp.getTenSanPham());
                    stmt.setString(4, sp.getMaVach());
                    stmt.setBigDecimal(5, sp.getGiaBan());
                    stmt.setInt(6, sp.getSoLuongTon());
                    stmt.setInt(7, sp.getNguongTonToiThieu());
                    stmt.setString(8, sp.getDonViTinh());
                    stmt.setBoolean(9, sp.isTrangThai());
                    stmt.executeUpdate();
                }
                conn.commit();
                return maSanPham;
            } catch (SQLException | RuntimeException e) {
                rollback(conn, e);
                if (e instanceof SQLException sqlEx) {
                    throw new IllegalStateException("Lỗi lưu sản phẩm: " + sqlEx.getMessage(), sqlEx);
                }
                throw (RuntimeException) e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi kết nối cơ sở dữ liệu: " + e.getMessage(), e);
        }
    }

    private String taoMaSanPham(Connection conn, LocalDate ngayTao) throws SQLException {
        String ngay = ngayTao.format(DateTimeFormatter.BASIC_ISO_DATE);
        String tienTo = "SP-" + ngay + "-";
        String sql = "SELECT MAX(maSanPham) FROM SanPham WITH (UPDLOCK, HOLDLOCK) WHERE maSanPham LIKE ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tienTo + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                String maLonNhat = rs.getString(1);
                int soThuTu = maLonNhat == null ? 1 : Integer.parseInt(maLonNhat.substring(maLonNhat.length() - 6)) + 1;
                if (soThuTu > 999999) {
                    throw new IllegalStateException("Đã vượt quá số lượng mã sản phẩm trong ngày.");
                }
                return String.format("%s%06d", tienTo, soThuTu);
            }
        }
    }

    public boolean capNhatSanPham(SanPham sp) {
        String sql = """
                UPDATE SanPham
                SET maNhaCungCap = ?, tenSanPham = ?, maVach = ?, giaBan = ?, nguongTonToiThieu = ?, donViTinh = ?
                WHERE maSanPham = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sp.getMaNhaCungCap());
            stmt.setString(2, sp.getTenSanPham());
            stmt.setString(3, sp.getMaVach());
            stmt.setBigDecimal(4, sp.getGiaBan());
            stmt.setInt(5, sp.getNguongTonToiThieu());
            stmt.setString(6, sp.getDonViTinh());
            stmt.setString(7, sp.getMaSanPham());
            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi cập nhật sản phẩm: " + e.getMessage(), e);
        }
    }

    public boolean capNhatTrangThaiSanPham(String maSanPham, boolean trangThai) {
        String sql = "UPDATE SanPham SET trangThai = ? WHERE maSanPham = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, trangThai);
            stmt.setString(2, maSanPham);
            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi cập nhật trạng thái kinh doanh sản phẩm: " + e.getMessage(), e);
        }
    }

    public boolean xoaSanPham(String maSanPham) {
        String sql = """
                DELETE FROM SanPham
                WHERE maSanPham = ? AND soLuongTon = 0
                  AND NOT EXISTS (SELECT 1 FROM ChiTietDonHang WHERE maSanPham = ?)
                  AND NOT EXISTS (SELECT 1 FROM ChiTietHoaDon_SanPham WHERE maSanPham = ?)
                """;
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) return false;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, maSanPham);
                stmt.setString(2, maSanPham);
                stmt.setString(3, maSanPham);
                int affected = stmt.executeUpdate();
                if (affected == 1) return true;

                try (PreparedStatement check = conn.prepareStatement("SELECT 1 FROM SanPham WHERE maSanPham = ?")) {
                    check.setString(1, maSanPham);
                    try (ResultSet rs = check.executeQuery()) {
                        if (!rs.next()) return false;
                    }
                }
                throw new IllegalStateException("Không thể xóa sản phẩm còn tồn kho hoặc đã phát sinh đơn nhập/hóa đơn. Hãy dùng chức năng Ngừng kinh doanh.");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 547) {
                throw new IllegalStateException("Không thể xóa sản phẩm đã được sử dụng trong giao dịch. Hãy dùng chức năng Ngừng kinh doanh.", e);
            }
            throw new IllegalStateException("Lỗi xóa sản phẩm: " + e.getMessage(), e);
        }
    }

    public SanPham laySanPhamTheoMa(String maSanPham) {
        String sql = """
                SELECT sp.maSanPham, sp.maNhaCungCap, ncc.tenNhaCungCap, sp.tenSanPham,
                       sp.maVach, sp.giaBan, sp.soLuongTon, sp.nguongTonToiThieu, sp.donViTinh, sp.trangThai
                FROM SanPham sp
                LEFT JOIN NhaCungCap ncc ON ncc.maNhaCungCap = sp.maNhaCungCap
                WHERE sp.maSanPham = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maSanPham);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSanPham(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi lấy thông tin sản phẩm: " + e.getMessage(), e);
        }
    }

    public boolean tonTaiMaVach(String maVach, String maSanPhamHienTai) {
        String sql = "SELECT 1 FROM SanPham WHERE maVach = ?" +
                     (maSanPhamHienTai != null ? " AND maSanPham <> ?" : "");
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maVach);
            if (maSanPhamHienTai != null) {
                stmt.setString(2, maSanPhamHienTai);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi kiểm tra mã vạch: " + e.getMessage(), e);
        }
    }

    public List<SanPham> timKiemSanPham(String tuKhoa, String maNhaCungCap, Boolean chiDangKinhDoanh) {
        StringBuilder sql = new StringBuilder("""
                SELECT sp.maSanPham, sp.maNhaCungCap, ncc.tenNhaCungCap, sp.tenSanPham,
                       sp.maVach, sp.giaBan, sp.soLuongTon, sp.nguongTonToiThieu, sp.donViTinh, sp.trangThai
                FROM SanPham sp
                LEFT JOIN NhaCungCap ncc ON ncc.maNhaCungCap = sp.maNhaCungCap
                WHERE 1 = 1
                """);
        List<Object> params = new ArrayList<>();

        if (tuKhoa != null && !tuKhoa.isBlank()) {
            sql.append(" AND (sp.maSanPham LIKE ? OR sp.tenSanPham LIKE ? OR sp.maVach LIKE ?)");
            String pattern = "%" + tuKhoa.trim() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        if (maNhaCungCap != null && !maNhaCungCap.isBlank()) {
            sql.append(" AND sp.maNhaCungCap = ?");
            params.add(maNhaCungCap.trim());
        }

        if (chiDangKinhDoanh != null) {
            sql.append(" AND sp.trangThai = ?");
            params.add(chiDangKinhDoanh ? 1 : 0);
        }

        sql.append(" ORDER BY sp.trangThai DESC, sp.tenSanPham ASC");

        List<SanPham> danhSach = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(mapResultSetToSanPham(rs));
                }
            }
            return danhSach;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi tìm kiếm sản phẩm: " + e.getMessage(), e);
        }
    }

    public List<SanPham> layDanhSachTonKho(boolean chiDuoiNguong) {
        StringBuilder sql = new StringBuilder("""
                SELECT sp.maSanPham, sp.maNhaCungCap, ncc.tenNhaCungCap, sp.tenSanPham,
                       sp.maVach, sp.giaBan, sp.soLuongTon, sp.nguongTonToiThieu, sp.donViTinh, sp.trangThai
                FROM SanPham sp
                LEFT JOIN NhaCungCap ncc ON ncc.maNhaCungCap = sp.maNhaCungCap
                WHERE sp.trangThai = 1
                """);
        if (chiDuoiNguong) {
            sql.append(" AND sp.soLuongTon <= sp.nguongTonToiThieu");
        }
        sql.append(" ORDER BY sp.soLuongTon ASC, sp.tenSanPham ASC");

        List<SanPham> danhSach = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString());
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                danhSach.add(mapResultSetToSanPham(rs));
            }
            return danhSach;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi tải thông tin tồn kho: " + e.getMessage(), e);
        }
    }

    public boolean capNhatSoLuongTon(String maSanPham, int soLuongThayDoi) {
        String sql = "UPDATE SanPham SET soLuongTon = soLuongTon + ? WHERE maSanPham = ? AND (soLuongTon + ?) >= 0";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, soLuongThayDoi);
            stmt.setString(2, maSanPham);
            stmt.setInt(3, soLuongThayDoi);
            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi cập nhật số lượng tồn: " + e.getMessage(), e);
        }
    }

    private SanPham mapResultSetToSanPham(ResultSet rs) throws SQLException {
        return new SanPham(
                rs.getString("maSanPham"),
                rs.getString("maNhaCungCap"),
                rs.getString("tenNhaCungCap"),
                rs.getString("tenSanPham"),
                rs.getString("maVach"),
                rs.getBigDecimal("giaBan"),
                rs.getInt("soLuongTon"),
                rs.getInt("nguongTonToiThieu"),
                rs.getString("donViTinh"),
                rs.getBoolean("trangThai")
        );
    }

    private void rollback(Connection conn, Exception cause) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            cause.addSuppressed(e);
        }
    }
}
