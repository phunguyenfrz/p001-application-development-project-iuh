package com.petshop.dao;

import com.petshop.model.ChiTietDonHang;
import com.petshop.model.DonHang;
import com.petshop.util.DatabaseManager;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp DAO truy xuất và thao tác dữ liệu bảng DonHang & ChiTietDonHang.
 * Tuân thủ quy ước mã đơn: DH-yyyyMMdd-xxxxxx.
 */
public class DonHangDAO {

    private static final DateTimeFormatter DINH_DANG_NGAY_MA = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Tự động sinh mã đơn nhập hàng theo định dạng DH-yyyyMMdd-xxxxxx.
     */
    public String taoMaDonHang() {
        String ngay = LocalDate.now().format(DINH_DANG_NGAY_MA);
        String tienTo = "DH-" + ngay + "-";
        String sql = "SELECT TOP 1 maDonHang FROM DonHang WHERE maDonHang LIKE ? ORDER BY maDonHang DESC";

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                return tienTo + "000001";
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, tienTo + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String maCu = rs.getString("maDonHang");
                        String[] phan = maCu.split("-");
                        if (phan.length == 3) {
                            int stt = Integer.parseInt(phan[2]) + 1;
                            return tienTo + String.format("%06d", stt);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi sinh mã đơn hàng: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Mã đơn hàng trong cơ sở dữ liệu không đúng định dạng", e);
        }

        return tienTo + "000001";
    }

    /**
     * Thêm mới một đơn nhập hàng cùng danh sách chi tiết (trong cùng transaction).
     */
    public boolean themDonHang(DonHang donHang, List<ChiTietDonHang> danhSachChiTiet) {
        String sqlDonHang = """
                INSERT INTO DonHang (maDonHang, maNhaCungCap, maNhanVien, ngayLap, tongTien, trangThai)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        String sqlChiTiet = """
                INSERT INTO ChiTietDonHang (maDonHang, maSanPham, soLuong, donGia)
                VALUES (?, ?, ?, ?)
                """;

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            if (conn == null) {
                return false;
            }
            conn.setAutoCommit(false);

            try (PreparedStatement psDH = conn.prepareStatement(sqlDonHang)) {
                psDH.setString(1, donHang.getMaDonHang());
                psDH.setString(2, donHang.getMaNhaCungCap());
                psDH.setString(3, donHang.getMaNhanVien());
                psDH.setDate(4, Date.valueOf(donHang.getNgayLap()));
                psDH.setBigDecimal(5, donHang.getTongTien() != null ? donHang.getTongTien() : BigDecimal.ZERO);
                psDH.setNString(6, donHang.getTrangThai() != null ? donHang.getTrangThai() : DonHang.TRANG_THAI_CHO_NHAN);

                int rowsDH = psDH.executeUpdate();
                if (rowsDH <= 0) {
                    conn.rollback();
                    return false;
                }
            }

            try (PreparedStatement psCT = conn.prepareStatement(sqlChiTiet)) {
                for (ChiTietDonHang ct : danhSachChiTiet) {
                    psCT.setString(1, donHang.getMaDonHang());
                    psCT.setString(2, ct.getMaSanPham());
                    psCT.setInt(3, ct.getSoLuong());
                    psCT.setBigDecimal(4, ct.getDonGia() != null ? ct.getDonGia() : BigDecimal.ZERO);
                    psCT.addBatch();
                }
                psCT.executeBatch();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Tìm đơn hàng theo mã đơn.
     */
    public DonHang timTheoMa(String maDonHang) {
        String sql = """
                SELECT dh.*, ncc.tenNhaCungCap, nv.hoTen AS tenNhanVien
                FROM DonHang dh
                LEFT JOIN NhaCungCap ncc ON dh.maNhaCungCap = ncc.maNhaCungCap
                LEFT JOIN NhanVien nv ON dh.maNhanVien = nv.maNhanVien
                WHERE dh.maDonHang = ?
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) return null;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maDonHang);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return anhXaDonHang(rs);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy danh sách chi tiết của một đơn hàng.
     */
    public List<ChiTietDonHang> layChiTietTheoMaDonHang(String maDonHang) {
        List<ChiTietDonHang> list = new ArrayList<>();
        String sql = """
                SELECT ct.*, sp.tenSanPham, sp.donViTinh
                FROM ChiTietDonHang ct
                LEFT JOIN SanPham sp ON ct.maSanPham = sp.maSanPham
                WHERE ct.maDonHang = ?
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) return list;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maDonHang);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ChiTietDonHang ct = new ChiTietDonHang();
                        ct.setMaDonHang(rs.getString("maDonHang"));
                        ct.setMaSanPham(rs.getString("maSanPham"));
                        ct.setSoLuong(rs.getInt("soLuong"));
                        ct.setDonGia(rs.getBigDecimal("donGia"));
                        ct.setTenSanPham(rs.getNString("tenSanPham"));
                        ct.setDonViTinh(rs.getNString("donViTinh"));
                        list.add(ct);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy tất cả đơn hàng sắp xếp theo ngày lập giảm dần.
     */
    public List<DonHang> layTatCaDonHang() {
        return timKiemDonHang(null, null, null, null, null);
    }

    /**
     * Tìm kiếm đơn hàng theo bộ lọc đa tiêu chí.
     */
    public List<DonHang> timKiemDonHang(String keyword, String maNhaCungCap, LocalDate tuNgay, LocalDate denNgay, String trangThai) {
        List<DonHang> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT dh.*, ncc.tenNhaCungCap, nv.hoTen AS tenNhanVien
                FROM DonHang dh
                LEFT JOIN NhaCungCap ncc ON dh.maNhaCungCap = ncc.maNhaCungCap
                LEFT JOIN NhanVien nv ON dh.maNhanVien = nv.maNhanVien
                WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (dh.maDonHang LIKE ? OR ncc.tenNhaCungCap LIKE ? OR nv.hoTen LIKE ?)");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (maNhaCungCap != null && !maNhaCungCap.trim().isEmpty()) {
            sql.append(" AND dh.maNhaCungCap = ?");
            params.add(maNhaCungCap.trim());
        }

        if (tuNgay != null) {
            sql.append(" AND dh.ngayLap >= ?");
            params.add(Date.valueOf(tuNgay));
        }

        if (denNgay != null) {
            sql.append(" AND dh.ngayLap <= ?");
            params.add(Date.valueOf(denNgay));
        }

        if (trangThai != null && !trangThai.trim().isEmpty() && !trangThai.equalsIgnoreCase("Tất cả")) {
            sql.append(" AND dh.trangThai = ?");
            params.add(trangThai.trim());
        }

        sql.append(" ORDER BY dh.ngayLap DESC, dh.maDonHang DESC");

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) return list;
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(anhXaDonHang(rs));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Cập nhật trạng thái đơn hàng (sử dụng khi hủy đơn).
     */
    public boolean capNhatTrangThai(String maDonHang, String trangThaiHienTai, String trangThaiMoi) {
        String sql = "UPDATE DonHang SET trangThai = ? WHERE maDonHang = ? AND trangThai = ?";
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) return false;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setNString(1, trangThaiMoi);
                ps.setString(2, maDonHang);
                ps.setNString(3, trangThaiHienTai);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xác nhận nhận hàng trong transaction:
     * 1. Cập nhật trạng thái đơn thành 'Hoàn tất' (chỉ thực hiện nếu trạng thái hiện tại là 'Chờ nhận hàng').
     * 2. Cộng số lượng tồn kho trong bảng SanPham tương ứng từng sản phẩm trong chi tiết đơn.
     */
    public boolean xacNhanNhanHangVaCongTonKho(String maDonHang) {
        String sqlUpdateDonHang = "UPDATE DonHang SET trangThai = ? WHERE maDonHang = ? AND trangThai = ?";
        String sqlGetChiTiet = "SELECT maSanPham, soLuong FROM ChiTietDonHang WHERE maDonHang = ?";
        String sqlUpdateTonKho = "UPDATE SanPham SET soLuongTon = soLuongTon + ? WHERE maSanPham = ?";

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            if (conn == null) return false;
            conn.setAutoCommit(false);

            // 1. Lấy chi tiết trước để đơn rỗng không thể chuyển sang hoàn tất.
            List<ChiTietDonHang> chiTietList = new ArrayList<>();
            try (PreparedStatement psCT = conn.prepareStatement(sqlGetChiTiet)) {
                psCT.setString(1, maDonHang);
                try (ResultSet rsCT = psCT.executeQuery()) {
                    while (rsCT.next()) {
                        ChiTietDonHang ct = new ChiTietDonHang();
                        ct.setMaSanPham(rsCT.getString("maSanPham"));
                        ct.setSoLuong(rsCT.getInt("soLuong"));
                        chiTietList.add(ct);
                    }
                }
            }
            if (chiTietList.isEmpty()) {
                conn.rollback();
                return false;
            }

            // 2. Chuyển trạng thái có điều kiện để chỉ một lần xác nhận thành công.
            try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateDonHang)) {
                psUpdate.setNString(1, DonHang.TRANG_THAI_HOAN_TAT);
                psUpdate.setString(2, maDonHang);
                psUpdate.setNString(3, DonHang.TRANG_THAI_CHO_NHAN);
                if (psUpdate.executeUpdate() != 1) {
                    conn.rollback();
                    return false;
                }
            }

            // 3. Cộng tồn kho; thiếu sản phẩm sẽ rollback cả trạng thái đơn.
            try (PreparedStatement psTonKho = conn.prepareStatement(sqlUpdateTonKho)) {
                for (ChiTietDonHang ct : chiTietList) {
                    psTonKho.setInt(1, ct.getSoLuong());
                    psTonKho.setString(2, ct.getMaSanPham());
                    if (psTonKho.executeUpdate() != 1) {
                        conn.rollback();
                        return false;
                    }
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private DonHang anhXaDonHang(ResultSet rs) throws SQLException {
        DonHang dh = new DonHang();
        dh.setMaDonHang(rs.getString("maDonHang"));
        dh.setMaNhaCungCap(rs.getString("maNhaCungCap"));
        dh.setMaNhanVien(rs.getString("maNhanVien"));
        Date ngayLap = rs.getDate("ngayLap");
        if (ngayLap != null) {
            dh.setNgayLap(ngayLap.toLocalDate());
        }
        dh.setTongTien(rs.getBigDecimal("tongTien"));
        dh.setTrangThai(rs.getNString("trangThai"));

        try {
            dh.setTenNhaCungCap(rs.getNString("tenNhaCungCap"));
        } catch (SQLException ignored) {}

        try {
            dh.setTenNhanVien(rs.getNString("tenNhanVien"));
        } catch (SQLException ignored) {}

        return dh;
    }
}

