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
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                String tienTo = "DH-" + LocalDate.now().format(DINH_DANG_NGAY_MA) + "-";
                return tienTo + "000001";
            }
            return taoMaDonHang(conn);
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi sinh mã đơn hàng: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Mã đơn hàng trong cơ sở dữ liệu không đúng định dạng", e);
        }

    }

    private String taoMaDonHang(Connection conn) throws SQLException {
        String tienTo = "DH-" + LocalDate.now().format(DINH_DANG_NGAY_MA) + "-";
        String sql = "SELECT TOP 1 maDonHang FROM DonHang WHERE maDonHang LIKE ? ORDER BY maDonHang DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tienTo + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String[] phan = rs.getString("maDonHang").split("-");
                    if (phan.length == 3) {
                        return tienTo + String.format("%06d", Integer.parseInt(phan[2]) + 1);
                    }
                }
            }
        }
        return tienTo + "000001";
    }

    /** Lưu nhiều đơn nhập theo từng nhà cung cấp trong một transaction. */
    public boolean themNhieuDonHang(List<DonHang> danhSachDonHang) {
        String sqlDonHang = """
                INSERT INTO DonHang (maDonHang, maNhaCungCap, maNhanVien, ngayLap, tongTien, trangThai)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        String sqlChiTiet = """
                INSERT INTO ChiTietDonHang (maDonHang, maSanPham, soLuong, donGia)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                throw new IllegalStateException("Không thể kết nối cơ sở dữ liệu.");
            }
            conn.setAutoCommit(false);
            try {
                for (DonHang donHang : danhSachDonHang) {
                    kiemTraNhaCungCapDangHoatDong(conn, donHang.getMaNhaCungCap());
                    donHang.setMaDonHang(taoMaDonHang(conn));

                    try (PreparedStatement ps = conn.prepareStatement(sqlDonHang)) {
                        ps.setString(1, donHang.getMaDonHang());
                        ps.setString(2, donHang.getMaNhaCungCap());
                        ps.setString(3, donHang.getMaNhanVien());
                        ps.setDate(4, Date.valueOf(donHang.getNgayLap()));
                        ps.setBigDecimal(5, donHang.getTongTien());
                        ps.setNString(6, donHang.getTrangThai());
                        ps.executeUpdate();
                    }

                    try (PreparedStatement ps = conn.prepareStatement(sqlChiTiet)) {
                        for (ChiTietDonHang chiTiet : donHang.getDanhSachChiTiet()) {
                            kiemTraSanPhamCuaNhaCungCap(conn, donHang.getMaNhaCungCap(), chiTiet.getMaSanPham());
                            ps.setString(1, donHang.getMaDonHang());
                            ps.setString(2, chiTiet.getMaSanPham());
                            ps.setInt(3, chiTiet.getSoLuong());
                            ps.setBigDecimal(4, chiTiet.getDonGia());
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }
                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                if (ex instanceof IllegalStateException ise) throw ise;
                throw new IllegalStateException("Không thể lưu các đơn nhập: " + ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể lưu các đơn nhập: " + e.getMessage(), e);
        }
    }

    private void kiemTraNhaCungCapDangHoatDong(Connection conn, String maNhaCungCap) throws SQLException {
        String sql = "SELECT trangThai FROM NhaCungCap WHERE maNhaCungCap = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNhaCungCap);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Nhà cung cấp " + maNhaCungCap + " không còn tồn tại.");
                }
                if (!rs.getBoolean("trangThai")) {
                    throw new IllegalStateException("Nhà cung cấp " + maNhaCungCap + " đã ngừng hợp tác.");
                }
            }
        }
    }

    private void kiemTraSanPhamCuaNhaCungCap(Connection conn, String maNhaCungCap, String maSanPham) throws SQLException {
        String sql = """
                SELECT sp.maNhaCungCap, sp.trangThai AS trangThaiSanPham, ncc.trangThai AS trangThaiNhaCungCap
                FROM SanPham sp
                LEFT JOIN NhaCungCap ncc ON ncc.maNhaCungCap = sp.maNhaCungCap
                WHERE sp.maSanPham = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSanPham);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Sản phẩm " + maSanPham + " không còn tồn tại.");
                }
                if (!rs.getBoolean("trangThaiSanPham")) {
                    throw new IllegalStateException("Sản phẩm " + maSanPham + " đã ngừng kinh doanh.");
                }
                if (!maNhaCungCap.equals(rs.getString("maNhaCungCap"))) {
                    throw new IllegalStateException("Sản phẩm " + maSanPham + " không còn thuộc nhà cung cấp đã chọn.");
                }
                if (!rs.getBoolean("trangThaiNhaCungCap")) {
                    throw new IllegalStateException("Nhà cung cấp của sản phẩm " + maSanPham + " đã ngừng hợp tác.");
                }
            }
        }
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
                throw new IllegalStateException("Lỗi khi thêm đơn hàng: không thể kết nối cơ sở dữ liệu");
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
                    throw new IllegalStateException("Lỗi khi thêm đơn hàng: không thể chèn dữ liệu đơn hàng");
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
                    // Lỗi rollback được bao gồm trong thông báo chính
                }
            }
            throw new IllegalStateException("Lỗi khi thêm đơn hàng: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    // Lỗi đóng kết nối không ảnh hưởng kết quả
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
            if (conn == null) {
                throw new IllegalStateException("Lỗi khi tìm đơn hàng: không thể kết nối cơ sở dữ liệu");
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maDonHang);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return anhXaDonHang(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi tìm đơn hàng theo mã: " + e.getMessage(), e);
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
            if (conn == null) {
                throw new IllegalStateException("Lỗi khi lấy chi tiết đơn hàng: không thể kết nối cơ sở dữ liệu");
            }
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
            throw new IllegalStateException("Lỗi khi lấy chi tiết đơn hàng: " + e.getMessage(), e);
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
            if (conn == null) {
                throw new IllegalStateException("Lỗi khi tìm kiếm đơn hàng: không thể kết nối cơ sở dữ liệu");
            }
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
            throw new IllegalStateException("Lỗi khi tìm kiếm đơn hàng: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Cập nhật trạng thái đơn hàng (sử dụng khi hủy đơn).
     */
    public boolean capNhatTrangThai(String maDonHang, String trangThaiHienTai, String trangThaiMoi) {
        String sql = "UPDATE DonHang SET trangThai = ? WHERE maDonHang = ? AND trangThai = ?";
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                throw new IllegalStateException("Lỗi khi cập nhật trạng thái đơn hàng: không thể kết nối cơ sở dữ liệu");
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setNString(1, trangThaiMoi);
                ps.setString(2, maDonHang);
                ps.setNString(3, trangThaiHienTai);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi cập nhật trạng thái đơn hàng: " + e.getMessage(), e);
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
            if (conn == null) {
                throw new IllegalStateException("Lỗi khi xác nhận nhận hàng: không thể kết nối cơ sở dữ liệu");
            }
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
                throw new IllegalStateException("Lỗi khi xác nhận nhận hàng: đơn hàng không có chi tiết sản phẩm");
            }

            // 2. Chuyển trạng thái có điều kiện để chỉ một lần xác nhận thành công.
            try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateDonHang)) {
                psUpdate.setNString(1, DonHang.TRANG_THAI_HOAN_TAT);
                psUpdate.setString(2, maDonHang);
                psUpdate.setNString(3, DonHang.TRANG_THAI_CHO_NHAN);
                if (psUpdate.executeUpdate() != 1) {
                    conn.rollback();
                    throw new IllegalStateException("Lỗi khi xác nhận nhận hàng: đơn hàng không ở trạng thái 'Chờ nhận hàng' hoặc đã được xác nhận trước đó");
                }
            }

            // 3. Cộng tồn kho; thiếu sản phẩm sẽ rollback cả trạng thái đơn.
            try (PreparedStatement psTonKho = conn.prepareStatement(sqlUpdateTonKho)) {
                for (ChiTietDonHang ct : chiTietList) {
                    psTonKho.setInt(1, ct.getSoLuong());
                    psTonKho.setString(2, ct.getMaSanPham());
                    if (psTonKho.executeUpdate() != 1) {
                        conn.rollback();
                        throw new IllegalStateException("Lỗi khi xác nhận nhận hàng: không tìm thấy sản phẩm " + ct.getMaSanPham() + " để cộng tồn kho");
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
                    // Lỗi rollback được bao gồm trong thông báo chính
                }
            }
            throw new IllegalStateException("Lỗi khi xác nhận nhận hàng: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    // Lỗi đóng kết nối không ảnh hưởng kết quả
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
