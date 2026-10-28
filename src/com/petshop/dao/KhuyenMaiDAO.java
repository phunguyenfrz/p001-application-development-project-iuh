package com.petshop.dao;

import com.petshop.model.KhuyenMai;
import com.petshop.util.DatabaseManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Truy xuất dữ liệu chương trình khuyến mãi trong SQL Server.
 */
public class KhuyenMaiDAO {

    private static final DateTimeFormatter DINH_DANG_NGAY_MA = DateTimeFormatter.ofPattern("yyyyMMdd");

    // Sinh mã khuyến mãi theo định dạng KM-yyyyMMdd-xxxxxx.
    public String taoMaKhuyenMai() {
        String ngay = LocalDate.now().format(DINH_DANG_NGAY_MA);
        String tienTo = "KM-" + ngay + "-";
        String sql = "SELECT TOP 1 maKhuyenMai FROM KhuyenMai WHERE maKhuyenMai LIKE ? ORDER BY maKhuyenMai DESC";

        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, tienTo + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String maCu = rs.getString("maKhuyenMai");
                        String[] phan = maCu.split("-");
                        if (phan.length == 3) {
                            int soThuTu = Integer.parseInt(phan[2]) + 1;
                            return tienTo + String.format("%06d", soThuTu);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi sinh mã khuyến mãi: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Mã khuyến mãi trong cơ sở dữ liệu không đúng định dạng", e);
        }

        return tienTo + "000001";
    }

    // Thêm chương trình khuyến mãi mới.
    public boolean themKhuyenMai(KhuyenMai khuyenMai) {
        String sql = """
                INSERT INTO KhuyenMai (maKhuyenMai, tenChuongTrinh, phanTramGiam, ngayBatDau, ngayKetThuc, trangThai)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, khuyenMai.getMaKhuyenMai());
            ps.setString(2, khuyenMai.getTenChuongTrinh());
            ps.setBigDecimal(3, khuyenMai.getPhanTramGiam());
            ps.setDate(4, Date.valueOf(khuyenMai.getNgayBatDau()));
            ps.setDate(5, Date.valueOf(khuyenMai.getNgayKetThuc()));
            ps.setBoolean(6, khuyenMai.isTrangThai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi thêm khuyến mãi: " + e.getMessage(), e);
        }
    }

    // Cập nhật thông tin chương trình khuyến mãi.
    public boolean capNhatKhuyenMai(KhuyenMai khuyenMai) {
        String sql = """
                UPDATE KhuyenMai
                SET tenChuongTrinh = ?, phanTramGiam = ?, ngayBatDau = ?, ngayKetThuc = ?, trangThai = ?
                WHERE maKhuyenMai = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, khuyenMai.getTenChuongTrinh());
            ps.setBigDecimal(2, khuyenMai.getPhanTramGiam());
            ps.setDate(3, Date.valueOf(khuyenMai.getNgayBatDau()));
            ps.setDate(4, Date.valueOf(khuyenMai.getNgayKetThuc()));
            ps.setBoolean(5, khuyenMai.isTrangThai());
            ps.setString(6, khuyenMai.getMaKhuyenMai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi cập nhật khuyến mãi: " + e.getMessage(), e);
        }
    }

    // Ngừng chương trình bằng xóa mềm.
    public boolean xoaKhuyenMai(String maKhuyenMai) {
        String sql = "UPDATE KhuyenMai SET trangThai = 0 WHERE maKhuyenMai = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKhuyenMai);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi ngừng khuyến mãi: " + e.getMessage(), e);
        }
    }

    // Lấy toàn bộ chương trình khuyến mãi.
    public List<KhuyenMai> layDanhSachKhuyenMai() {
        String sql = "SELECT maKhuyenMai, tenChuongTrinh, phanTramGiam, ngayBatDau, ngayKetThuc, trangThai "
                + "FROM KhuyenMai ORDER BY maKhuyenMai DESC";
        List<KhuyenMai> danhSach = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                danhSach.add(docKhuyenMai(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi lấy danh sách khuyến mãi: " + e.getMessage(), e);
        }
        return danhSach;
    }

    // Tìm khuyến mãi theo mã, tên và trạng thái; giá trị null là không lọc.
    public List<KhuyenMai> timKiemKhuyenMai(String maKhuyenMai, String tenChuongTrinh, Boolean trangThai) {
        StringBuilder sql = new StringBuilder(
                "SELECT maKhuyenMai, tenChuongTrinh, phanTramGiam, ngayBatDau, ngayKetThuc, trangThai "
                        + "FROM KhuyenMai WHERE 1 = 1");
        boolean coLocMa = maKhuyenMai != null && !maKhuyenMai.trim().isEmpty();
        boolean coLocTen = tenChuongTrinh != null && !tenChuongTrinh.trim().isEmpty();
        String dieuKienMa = coLocMa ? "%" + maKhuyenMai.trim() + "%" : null;
        String dieuKienTen = coLocTen ? "%" + tenChuongTrinh.trim() + "%" : null;

        if (coLocMa) {
            sql.append(" AND maKhuyenMai LIKE ?");
        }
        if (coLocTen) {
            sql.append(" AND tenChuongTrinh LIKE ?");
        }
        if (trangThai != null) {
            sql.append(" AND trangThai = ?");
        }
        sql.append(" ORDER BY maKhuyenMai DESC");

        List<KhuyenMai> danhSach = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int viTri = 1;
            if (coLocMa) {
                ps.setString(viTri++, dieuKienMa);
            }
            if (coLocTen) {
                ps.setString(viTri++, dieuKienTen);
            }
            if (trangThai != null) {
                ps.setBoolean(viTri, trangThai);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(docKhuyenMai(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi tìm kiếm khuyến mãi: " + e.getMessage(), e);
        }
        return danhSach;
    }

    // Tìm một khuyến mãi theo mã, trả về null nếu không tồn tại.
    public KhuyenMai layKhuyenMaiTheoMa(String maKhuyenMai) {
        String sql = "SELECT maKhuyenMai, tenChuongTrinh, phanTramGiam, ngayBatDau, ngayKetThuc, trangThai "
                + "FROM KhuyenMai WHERE maKhuyenMai = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKhuyenMai);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return docKhuyenMai(rs);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi lấy khuyến mãi theo mã: " + e.getMessage(), e);
        }
        return null;
    }

    private KhuyenMai docKhuyenMai(ResultSet rs) throws SQLException {
        Date ngayBatDau = rs.getDate("ngayBatDau");
        Date ngayKetThuc = rs.getDate("ngayKetThuc");
        return new KhuyenMai(rs.getString("maKhuyenMai"), rs.getString("tenChuongTrinh"),
                rs.getBigDecimal("phanTramGiam"), ngayBatDau.toLocalDate(), ngayKetThuc.toLocalDate(),
                rs.getBoolean("trangThai"));
    }
}
