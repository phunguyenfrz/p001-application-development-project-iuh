package com.petshop.dao;

import com.petshop.model.ChiTietDonNhap;
import com.petshop.model.DonNhapHang;
import com.petshop.util.DatabaseManager;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp DAO truy xuất và thao tác dữ liệu bảng DonNhapHang & ChiTietDonNhap.
 * Tuân thủ quy ước mã đơn mới: PN-yyyyMMdd-xxxxxx (vẫn hỗ trợ đọc mã cũ DH-yyyyMMdd-xxxxxx).
 */
public class DonNhapHangDAO {

    private static final DateTimeFormatter DINH_DANG_NGAY_MA = DateTimeFormatter.ofPattern("yyyyMMdd");

    // Cache tên bảng để tương thích ngược nếu DB chưa chạy migration script
    private static volatile String tenBangDonNhap = null;
    private static volatile String tenBangChiTiet = null;
    private static volatile Boolean coCotGioLap = null;

    private static String getTenBangDonNhap(Connection conn) {
        if (tenBangDonNhap != null) return tenBangDonNhap;
        synchronized (DonNhapHangDAO.class) {
            if (tenBangDonNhap != null) return tenBangDonNhap;
            try (ResultSet rs = conn.getMetaData().getTables(null, null, "DonNhapHang", null)) {
                if (rs.next()) {
                    tenBangDonNhap = "DonNhapHang";
                } else {
                    tenBangDonNhap = "DonHang";
                }
            } catch (SQLException e) {
                tenBangDonNhap = "DonNhapHang";
            }
            return tenBangDonNhap;
        }
    }

    private static String getTenBangChiTiet(Connection conn) {
        if (tenBangChiTiet != null) return tenBangChiTiet;
        synchronized (DonNhapHangDAO.class) {
            if (tenBangChiTiet != null) return tenBangChiTiet;
            try (ResultSet rs = conn.getMetaData().getTables(null, null, "ChiTietDonNhap", null)) {
                if (rs.next()) {
                    tenBangChiTiet = "ChiTietDonNhap";
                } else {
                    tenBangChiTiet = "ChiTietDonHang";
                }
            } catch (SQLException e) {
                tenBangChiTiet = "ChiTietDonNhap";
            }
            return tenBangChiTiet;
        }
    }

    private static boolean coCotGioLap(Connection conn) {
        if (coCotGioLap != null) return coCotGioLap;
        synchronized (DonNhapHangDAO.class) {
            if (coCotGioLap != null) return coCotGioLap;
            String bang = getTenBangDonNhap(conn);
            try (ResultSet rs = conn.getMetaData().getColumns(null, null, bang, "gioLap")) {
                coCotGioLap = rs.next();
            } catch (SQLException e) {
                coCotGioLap = false;
            }
            return coCotGioLap;
        }
    }

    /**
     * Tự động sinh mã đơn nhập hàng theo định dạng mới PN-yyyyMMdd-xxxxxx.
     */
    public String taoMaDonNhapHang() {
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                String ngay = LocalDate.now().format(DINH_DANG_NGAY_MA);
                return "PN-" + ngay + "-000001";
            }
            return taoMaDonNhapHang(conn);
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi sinh mã đơn nhập hàng: " + e.getMessage(), e);
        }
    }

    /**
     * Tự động sinh mã đơn nhập hàng trong một connection sẵn có.
     */
    public String taoMaDonNhapHang(Connection conn) throws SQLException {
        String ngay = LocalDate.now().format(DINH_DANG_NGAY_MA);
        String tienToMoi = "PN-" + ngay + "-";
        String tienToCu = "DH-" + ngay + "-";
        String bang = getTenBangDonNhap(conn);
        String sql = "SELECT TOP 1 maDonHang FROM " + bang + " WHERE maDonHang LIKE ? OR maDonHang LIKE ? ORDER BY maDonHang DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tienToMoi + "%");
            ps.setString(2, tienToCu + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String maCu = rs.getString("maDonHang");
                    String[] phan = maCu.split("-");
                    if (phan.length == 3) {
                        try {
                            int stt = Integer.parseInt(phan[2]) + 1;
                            return tienToMoi + String.format("%06d", stt);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        }
        return tienToMoi + "000001";
    }

    /**
     * Thêm mới một đơn nhập hàng cùng danh sách chi tiết (trong cùng transaction).
     */
    public boolean themDonNhapHang(DonNhapHang donHang, List<ChiTietDonNhap> danhSachChiTiet) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            if (conn == null) {
                throw new IllegalStateException("Lỗi khi thêm đơn nhập: không thể kết nối cơ sở dữ liệu");
            }
            conn.setAutoCommit(false);

            String bangDH = getTenBangDonNhap(conn);
            String bangCT = getTenBangChiTiet(conn);
            boolean coGio = coCotGioLap(conn);

            String sqlDonHang = coGio ?
                    "INSERT INTO " + bangDH + " (maDonHang, maNhaCungCap, maNhanVien, ngayLap, gioLap, tongTien, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?)" :
                    "INSERT INTO " + bangDH + " (maDonHang, maNhaCungCap, maNhanVien, ngayLap, tongTien, trangThai) VALUES (?, ?, ?, ?, ?, ?)";

            String sqlChiTiet = "INSERT INTO " + bangCT + " (maDonHang, maSanPham, soLuong, donGia) VALUES (?, ?, ?, ?)";

            kiemTraNhaCungCapDangHoatDong(conn, donHang.getMaNhaCungCap());

            try (PreparedStatement psDH = conn.prepareStatement(sqlDonHang)) {
                psDH.setString(1, donHang.getMaDonHang());
                psDH.setString(2, donHang.getMaNhaCungCap());
                psDH.setString(3, donHang.getMaNhanVien());
                psDH.setDate(4, Date.valueOf(donHang.getNgayLap()));
                if (coGio) {
                    psDH.setTime(5, Time.valueOf(donHang.getGioLap() != null ? donHang.getGioLap() : LocalTime.now().withNano(0)));
                    psDH.setBigDecimal(6, donHang.getTongTien() != null ? donHang.getTongTien() : BigDecimal.ZERO);
                    psDH.setNString(7, donHang.getTrangThai() != null ? donHang.getTrangThai() : DonNhapHang.TRANG_THAI_CHO_NHAN);
                } else {
                    psDH.setBigDecimal(5, donHang.getTongTien() != null ? donHang.getTongTien() : BigDecimal.ZERO);
                    psDH.setNString(6, donHang.getTrangThai() != null ? donHang.getTrangThai() : DonNhapHang.TRANG_THAI_CHO_NHAN);
                }

                int rowsDH = psDH.executeUpdate();
                if (rowsDH <= 0) {
                    conn.rollback();
                    throw new IllegalStateException("Lỗi khi thêm đơn nhập: không thể chèn dữ liệu");
                }
            }

            try (PreparedStatement psCT = conn.prepareStatement(sqlChiTiet)) {
                for (ChiTietDonNhap ct : danhSachChiTiet) {
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
                } catch (SQLException ignored) {}
            }
            throw new IllegalStateException("Lỗi khi thêm đơn nhập hàng: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Lưu nhiều đơn nhập theo từng nhà cung cấp trong một transaction.
     */
    public boolean themNhieuDonNhapHang(List<DonNhapHang> danhSachDonHang) {
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                throw new IllegalStateException("Không thể kết nối cơ sở dữ liệu.");
            }
            conn.setAutoCommit(false);
            try {
                String bangDH = getTenBangDonNhap(conn);
                String bangCT = getTenBangChiTiet(conn);
                boolean coGio = coCotGioLap(conn);

                String sqlDonHang = coGio ?
                        "INSERT INTO " + bangDH + " (maDonHang, maNhaCungCap, maNhanVien, ngayLap, gioLap, tongTien, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?)" :
                        "INSERT INTO " + bangDH + " (maDonHang, maNhaCungCap, maNhanVien, ngayLap, tongTien, trangThai) VALUES (?, ?, ?, ?, ?, ?)";

                String sqlChiTiet = "INSERT INTO " + bangCT + " (maDonHang, maSanPham, soLuong, donGia) VALUES (?, ?, ?, ?)";

                for (DonNhapHang donHang : danhSachDonHang) {
                    kiemTraNhaCungCapDangHoatDong(conn, donHang.getMaNhaCungCap());
                    donHang.setMaDonHang(taoMaDonNhapHang(conn));

                    try (PreparedStatement ps = conn.prepareStatement(sqlDonHang)) {
                        ps.setString(1, donHang.getMaDonHang());
                        ps.setString(2, donHang.getMaNhaCungCap());
                        ps.setString(3, donHang.getMaNhanVien());
                        ps.setDate(4, Date.valueOf(donHang.getNgayLap()));
                        if (coGio) {
                            ps.setTime(5, Time.valueOf(donHang.getGioLap() != null ? donHang.getGioLap() : LocalTime.now().withNano(0)));
                            ps.setBigDecimal(6, donHang.getTongTien());
                            ps.setNString(7, donHang.getTrangThai());
                        } else {
                            ps.setBigDecimal(5, donHang.getTongTien());
                            ps.setNString(6, donHang.getTrangThai());
                        }
                        ps.executeUpdate();
                    }

                    try (PreparedStatement ps = conn.prepareStatement(sqlChiTiet)) {
                        for (ChiTietDonNhap chiTiet : donHang.getDanhSachChiTiet()) {
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
        } catch (SQLException ex) {
            throw new IllegalStateException("Lỗi cơ sở dữ liệu: " + ex.getMessage(), ex);
        }
    }

    /**
     * Tìm đơn nhập hàng theo mã.
     */
    public DonNhapHang timTheoMa(String maDonHang) {
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                throw new IllegalStateException("Lỗi khi tìm đơn nhập: không thể kết nối cơ sở dữ liệu");
            }
            String bangDH = getTenBangDonNhap(conn);
            String sql = "SELECT dh.*, ncc.tenNhaCungCap, nv.hoTen AS tenNhanVien " +
                    "FROM " + bangDH + " dh " +
                    "LEFT JOIN NhaCungCap ncc ON dh.maNhaCungCap = ncc.maNhaCungCap " +
                    "LEFT JOIN NhanVien nv ON dh.maNhanVien = nv.maNhanVien " +
                    "WHERE dh.maDonHang = ?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maDonHang);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return anhXaDonNhapHang(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi tìm đơn nhập theo mã: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Lấy danh sách chi tiết của một đơn nhập hàng.
     */
    public List<ChiTietDonNhap> layChiTietTheoMaDonHang(String maDonHang) {
        List<ChiTietDonNhap> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                throw new IllegalStateException("Lỗi khi lấy chi tiết đơn nhập: không thể kết nối cơ sở dữ liệu");
            }
            String bangCT = getTenBangChiTiet(conn);
            String sql = "SELECT ct.*, sp.tenSanPham, sp.donViTinh " +
                    "FROM " + bangCT + " ct " +
                    "LEFT JOIN SanPham sp ON ct.maSanPham = sp.maSanPham " +
                    "WHERE ct.maDonHang = ?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maDonHang);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ChiTietDonNhap ct = new ChiTietDonNhap();
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
            throw new IllegalStateException("Lỗi khi lấy chi tiết đơn nhập: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Lấy tất cả đơn nhập hàng sắp xếp theo ngày lập giảm dần.
     */
    public List<DonNhapHang> layTatCaDonHang() {
        return timKiemDonHang(null, null, null, null, null);
    }

    /**
     * Tìm kiếm đơn nhập hàng theo bộ lọc đa tiêu chí.
     */
    public List<DonNhapHang> timKiemDonHang(String keyword, String maNhaCungCap, LocalDate tuNgay, LocalDate denNgay, String trangThai) {
        List<DonNhapHang> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                throw new IllegalStateException("Lỗi khi tìm kiếm đơn nhập: không thể kết nối cơ sở dữ liệu");
            }
            String bangDH = getTenBangDonNhap(conn);
            StringBuilder sql = new StringBuilder(
                    "SELECT dh.*, ncc.tenNhaCungCap, nv.hoTen AS tenNhanVien " +
                    "FROM " + bangDH + " dh " +
                    "LEFT JOIN NhaCungCap ncc ON dh.maNhaCungCap = ncc.maNhaCungCap " +
                    "LEFT JOIN NhanVien nv ON dh.maNhanVien = nv.maNhanVien " +
                    "WHERE 1=1"
            );

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

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(anhXaDonNhapHang(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi tìm kiếm đơn nhập hàng: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Cập nhật trạng thái đơn hàng (dùng khi hủy đơn).
     */
    public boolean capNhatTrangThai(String maDonHang, String trangThaiHienTai, String trangThaiMoi) {
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                throw new IllegalStateException("Lỗi khi cập nhật trạng thái đơn nhập: không thể kết nối cơ sở dữ liệu");
            }
            String bangDH = getTenBangDonNhap(conn);
            String sql = "UPDATE " + bangDH + " SET trangThai = ? WHERE maDonHang = ? AND trangThai = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setNString(1, trangThaiMoi);
                ps.setString(2, maDonHang);
                ps.setNString(3, trangThaiHienTai);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi cập nhật trạng thái đơn nhập: " + e.getMessage(), e);
        }
    }

    /**
     * Xác nhận nhận hàng trong transaction:
     * 1. Cập nhật trạng thái đơn thành 'Hoàn tất' (chỉ thực hiện nếu trạng thái hiện tại là 'Chờ nhận hàng').
     * 2. Cộng số lượng tồn kho trong bảng SanPham tương ứng từng sản phẩm trong chi tiết đơn.
     */
    public boolean xacNhanNhanHangVaCongTonKho(String maDonHang) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            if (conn == null) {
                throw new IllegalStateException("Lỗi khi xác nhận nhận hàng: không thể kết nối cơ sở dữ liệu");
            }
            conn.setAutoCommit(false);

            String bangDH = getTenBangDonNhap(conn);
            String bangCT = getTenBangChiTiet(conn);

            String sqlUpdateDonHang = "UPDATE " + bangDH + " SET trangThai = ? WHERE maDonHang = ? AND trangThai = ?";
            String sqlGetChiTiet = "SELECT maSanPham, soLuong FROM " + bangCT + " WHERE maDonHang = ?";
            String sqlUpdateTonKho = "UPDATE SanPham SET soLuongTon = soLuongTon + ? WHERE maSanPham = ?";

            // 1. Lấy chi tiết trước
            List<ChiTietDonNhap> chiTietList = new ArrayList<>();
            try (PreparedStatement psCT = conn.prepareStatement(sqlGetChiTiet)) {
                psCT.setString(1, maDonHang);
                try (ResultSet rsCT = psCT.executeQuery()) {
                    while (rsCT.next()) {
                        ChiTietDonNhap ct = new ChiTietDonNhap();
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

            // 2. Chuyển trạng thái có điều kiện (WHERE trangThai = 'Chờ nhận hàng')
            try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateDonHang)) {
                psUpdate.setNString(1, DonNhapHang.TRANG_THAI_HOAN_TAT);
                psUpdate.setString(2, maDonHang);
                psUpdate.setNString(3, DonNhapHang.TRANG_THAI_CHO_NHAN);
                if (psUpdate.executeUpdate() != 1) {
                    conn.rollback();
                    throw new IllegalStateException("Lỗi khi xác nhận nhận hàng: đơn hàng không ở trạng thái 'Chờ nhận hàng' hoặc đã được xác nhận trước đó");
                }
            }

            // 3. Cộng tồn kho
            try (PreparedStatement psTonKho = conn.prepareStatement(sqlUpdateTonKho)) {
                for (ChiTietDonNhap ct : chiTietList) {
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
                } catch (SQLException ignored) {}
            }
            throw new IllegalStateException("Lỗi khi xác nhận nhận hàng: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    private void kiemTraNhaCungCapDangHoatDong(Connection conn, String maNhaCungCap) throws SQLException {
        String sql = "SELECT trangThai FROM NhaCungCap WHERE maNhaCungCap = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNhaCungCap);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Nhà cung cấp " + maNhaCungCap + " không tồn tại.");
                }
                if (!rs.getBoolean("trangThai")) {
                    throw new IllegalStateException("Nhà cung cấp " + maNhaCungCap + " đã ngừng hợp tác.");
                }
            }
        }
    }

    private DonNhapHang anhXaDonNhapHang(ResultSet rs) throws SQLException {
        DonNhapHang dh = new DonNhapHang();
        dh.setMaDonHang(rs.getString("maDonHang"));
        dh.setMaNhaCungCap(rs.getString("maNhaCungCap"));
        dh.setMaNhanVien(rs.getString("maNhanVien"));
        Date ngayLap = rs.getDate("ngayLap");
        if (ngayLap != null) {
            dh.setNgayLap(ngayLap.toLocalDate());
        }
        try {
            Time gioLap = rs.getTime("gioLap");
            dh.setGioLap(gioLap != null ? gioLap.toLocalTime() : LocalTime.MIDNIGHT);
        } catch (SQLException ex) {
            dh.setGioLap(LocalTime.MIDNIGHT);
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

