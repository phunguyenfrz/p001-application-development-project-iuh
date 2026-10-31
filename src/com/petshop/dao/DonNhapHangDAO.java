package com.petshop.dao;

import com.petshop.model.ChiTietDonNhap;
import com.petshop.model.DonNhapHang;
import com.petshop.util.DatabaseManager;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lớp DAO truy xuất và thao tác dữ liệu bảng DonNhapHang & ChiTietDonNhap.
 * Tương thích ngược an toàn cho cả CSDL đã hoặc chưa chạy migration script.
 */
public class DonNhapHangDAO {

    private static final DateTimeFormatter DINH_DANG_NGAY_MA = DateTimeFormatter.ofPattern("yyyyMMdd");

    // Cache kiểm tra schema để tối ưu hiệu năng và tránh lỗi thiếu cột
    private static volatile String tenBangDonNhap = null;
    private static volatile String tenBangChiTiet = null;
    private static volatile Boolean coCotGioLap = null;
    private static volatile Boolean coCotThucNhan = null;
    private static volatile Boolean coCotTruyVet = null;

    private static boolean kiemTraBangTonTai(Connection conn, String tenBang) {
        String sql = "SELECT OBJECT_ID(?, 'U')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenBang);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getObject(1) != null;
                }
            }
        } catch (SQLException ignored) {}
        try (ResultSet rs = conn.getMetaData().getTables(null, null, tenBang, null)) {
            return rs.next();
        } catch (SQLException ignored) {}
        return false;
    }

    private static String getTenBangDonNhap(Connection conn) {
        if (tenBangDonNhap != null) return tenBangDonNhap;
        synchronized (DonNhapHangDAO.class) {
            if (tenBangDonNhap != null) return tenBangDonNhap;
            if (kiemTraBangTonTai(conn, "DonNhapHang")) {
                tenBangDonNhap = "DonNhapHang";
            } else {
                tenBangDonNhap = "DonHang";
            }
            return tenBangDonNhap;
        }
    }

    private static String getTenBangChiTiet(Connection conn) {
        if (tenBangChiTiet != null) return tenBangChiTiet;
        synchronized (DonNhapHangDAO.class) {
            if (tenBangChiTiet != null) return tenBangChiTiet;
            if (kiemTraBangTonTai(conn, "ChiTietDonNhap")) {
                tenBangChiTiet = "ChiTietDonNhap";
            } else {
                tenBangChiTiet = "ChiTietDonHang";
            }
            return tenBangChiTiet;
        }
    }

    private static volatile boolean daDamBaoCauTruc = false;

    private static void damBaoCauTrucBang(Connection conn) {
        if (daDamBaoCauTruc) return;
        synchronized (DonNhapHangDAO.class) {
            if (daDamBaoCauTruc) return;
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("IF COL_LENGTH('dbo.DonHang', 'gioLap') IS NULL ALTER TABLE dbo.DonHang ADD gioLap TIME(0) NULL;");
                st.executeUpdate("IF COL_LENGTH('dbo.DonHang', 'tongTienThucNhan') IS NULL ALTER TABLE dbo.DonHang ADD tongTienThucNhan DECIMAL(18,2) NULL;");
                st.executeUpdate("IF COL_LENGTH('dbo.DonHang', 'maNhanVienNhan') IS NULL ALTER TABLE dbo.DonHang ADD maNhanVienNhan VARCHAR(20) NULL;");
                st.executeUpdate("IF COL_LENGTH('dbo.DonHang', 'ngayGioNhan') IS NULL ALTER TABLE dbo.DonHang ADD ngayGioNhan DATETIME2 NULL;");
                st.executeUpdate("IF COL_LENGTH('dbo.DonHang', 'maNhanVienHuy') IS NULL ALTER TABLE dbo.DonHang ADD maNhanVienHuy VARCHAR(20) NULL;");
                st.executeUpdate("IF COL_LENGTH('dbo.DonHang', 'lyDoHuy') IS NULL ALTER TABLE dbo.DonHang ADD lyDoHuy NVARCHAR(255) NULL;");
                st.executeUpdate("IF COL_LENGTH('dbo.ChiTietDonHang', 'soLuongThucNhan') IS NULL ALTER TABLE dbo.ChiTietDonHang ADD soLuongThucNhan INT NULL;");
                st.executeUpdate("UPDATE dbo.ChiTietDonHang SET soLuongThucNhan = soLuong WHERE soLuongThucNhan IS NULL;");
                daDamBaoCauTruc = true;
                coCotGioLap = true;
                coCotThucNhan = true;
                coCotTruyVet = true;
            } catch (Exception ignored) {}
        }
    }

    private static boolean kiemTraCotTonTai(Connection conn, String tenBang, String tenCot) {
        damBaoCauTrucBang(conn);
        String sql = "SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND COLUMN_NAME = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenBang);
            ps.setString(2, tenCot);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        } catch (SQLException ignored) {}
        String sqlLen = "SELECT COL_LENGTH(?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlLen)) {
            ps.setString(1, tenBang);
            ps.setString(2, tenCot);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getObject(1) != null;
                }
            }
        } catch (SQLException ignored) {}
        return false;
    }

    private static boolean coCotGioLap(Connection conn) {
        if (coCotGioLap != null) return coCotGioLap;
        synchronized (DonNhapHangDAO.class) {
            if (coCotGioLap != null) return coCotGioLap;
            String bang = getTenBangDonNhap(conn);
            coCotGioLap = kiemTraCotTonTai(conn, bang, "gioLap");
            return coCotGioLap;
        }
    }

    private static boolean coCotThucNhan(Connection conn) {
        if (coCotThucNhan != null) return coCotThucNhan;
        synchronized (DonNhapHangDAO.class) {
            if (coCotThucNhan != null) return coCotThucNhan;
            String bang = getTenBangChiTiet(conn);
            coCotThucNhan = kiemTraCotTonTai(conn, bang, "soLuongThucNhan");
            return coCotThucNhan;
        }
    }

    private static boolean coCotTruyVet(Connection conn) {
        if (coCotTruyVet != null) return coCotTruyVet;
        synchronized (DonNhapHangDAO.class) {
            if (coCotTruyVet != null) return coCotTruyVet;
            String bang = getTenBangDonNhap(conn);
            coCotTruyVet = kiemTraCotTonTai(conn, bang, "maNhanVienNhan");
            return coCotTruyVet;
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
     * Tự động sinh mã đơn nhập hàng trong một connection sẵn có (mã mới luôn mang tiền tố PN-).
     */
    public String taoMaDonNhapHang(Connection conn) throws SQLException {
        String ngay = LocalDate.now().format(DINH_DANG_NGAY_MA);
        String tienToMoi = "PN-" + ngay + "-";
        String tienToCu = "DH-" + ngay + "-";
        String bang = getTenBangDonNhap(conn);
        String sql = "SELECT maDonHang FROM " + bang + " WHERE maDonHang LIKE ? OR maDonHang LIKE ?";

        int maxStt = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tienToMoi + "%");
            ps.setString(2, tienToCu + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ma = rs.getString("maDonHang");
                    if (ma != null) {
                        String[] phan = ma.split("-");
                        if (phan.length == 3) {
                            try {
                                int stt = Integer.parseInt(phan[2]);
                                if (stt > maxStt) {
                                    maxStt = stt;
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }
            }
        }
        return tienToMoi + String.format("%06d", maxStt + 1);
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
            boolean coTN = coCotThucNhan(conn);

            String sqlDonHang = coGio ?
                    "INSERT INTO " + bangDH + " (maDonHang, maNhaCungCap, maNhanVien, ngayLap, gioLap, tongTien, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?)" :
                    "INSERT INTO " + bangDH + " (maDonHang, maNhaCungCap, maNhanVien, ngayLap, tongTien, trangThai) VALUES (?, ?, ?, ?, ?, ?)";

            String sqlChiTiet = coTN ?
                    "INSERT INTO " + bangCT + " (maDonHang, maSanPham, soLuong, soLuongThucNhan, donGia) VALUES (?, ?, ?, ?, ?)" :
                    "INSERT INTO " + bangCT + " (maDonHang, maSanPham, soLuong, donGia) VALUES (?, ?, ?, ?)";

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
                    if (coTN) {
                        psCT.setInt(4, ct.getSoLuongThucNhan() != null ? ct.getSoLuongThucNhan() : ct.getSoLuong());
                        psCT.setBigDecimal(5, ct.getDonGia() != null ? ct.getDonGia() : BigDecimal.ZERO);
                    } else {
                        psCT.setBigDecimal(4, ct.getDonGia() != null ? ct.getDonGia() : BigDecimal.ZERO);
                    }
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
                boolean coTN = coCotThucNhan(conn);

                String sqlDonHang = coGio ?
                        "INSERT INTO " + bangDH + " (maDonHang, maNhaCungCap, maNhanVien, ngayLap, gioLap, tongTien, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?)" :
                        "INSERT INTO " + bangDH + " (maDonHang, maNhaCungCap, maNhanVien, ngayLap, tongTien, trangThai) VALUES (?, ?, ?, ?, ?, ?)";

                String sqlChiTiet = coTN ?
                        "INSERT INTO " + bangCT + " (maDonHang, maSanPham, soLuong, soLuongThucNhan, donGia) VALUES (?, ?, ?, ?, ?)" :
                        "INSERT INTO " + bangCT + " (maDonHang, maSanPham, soLuong, donGia) VALUES (?, ?, ?, ?)";

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
                            if (coTN) {
                                ps.setInt(4, chiTiet.getSoLuongThucNhan() != null ? chiTiet.getSoLuongThucNhan() : chiTiet.getSoLuong());
                                ps.setBigDecimal(5, chiTiet.getDonGia());
                            } else {
                                ps.setBigDecimal(4, chiTiet.getDonGia());
                            }
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
                        DonNhapHang dh = anhXaDonNhapHang(rs);
                        // Bổ sung tên nhân viên nhận/hủy an toàn nếu có
                        if (dh.getMaNhanVienNhan() != null) {
                            dh.setTenNhanVienNhan(layTenNhanVien(conn, dh.getMaNhanVienNhan()));
                        }
                        if (dh.getMaNhanVienHuy() != null) {
                            dh.setTenNhanVienHuy(layTenNhanVien(conn, dh.getMaNhanVienHuy()));
                        }
                        napTenHangChoDanhSach(conn, List.of(dh));
                        return dh;
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
            boolean coTN = coCotThucNhan(conn);

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
                        int soLuong = rs.getInt("soLuong");
                        ct.setSoLuong(soLuong);
                        if (coTN) {
                            int thucNhan = rs.getInt("soLuongThucNhan");
                            ct.setSoLuongThucNhan(rs.wasNull() ? soLuong : thucNhan);
                        } else {
                            ct.setSoLuongThucNhan(soLuong);
                        }
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
     * Tìm kiếm đơn nhập hàng theo bộ lọc đa tiêu chí (hỗ trợ tìm kiếm theo tên hàng).
     */
    public List<DonNhapHang> timKiemDonHang(String keyword, String maNhaCungCap, LocalDate tuNgay, LocalDate denNgay, String trangThai) {
        return timKiemDonHang(keyword, maNhaCungCap, tuNgay, denNgay, trangThai, null);
    }

    /**
     * Tìm kiếm đơn nhập hàng theo bộ lọc đa tiêu chí:
     * - keyword: mã đơn hàng, tên nhà cung cấp, tên nhân viên, hoặc tên mặt hàng trong đơn
     * - tenHang: tìm theo tên sản phẩm có trong chi tiết đơn nhập
     * - maNhaCungCap: mã NCC
     * - tuNgay, denNgay: khoảng ngày lập
     * - trangThai: Chờ nhận hàng, Hoàn tất, Đã hủy
     */
    public List<DonNhapHang> timKiemDonHang(String keyword, String maNhaCungCap, LocalDate tuNgay, LocalDate denNgay, String trangThai, String tenHang) {
        List<DonNhapHang> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                throw new IllegalStateException("Lỗi khi tìm kiếm đơn nhập: không thể kết nối cơ sở dữ liệu");
            }
            damBaoCauTrucBang(conn);
            String bangDH = getTenBangDonNhap(conn);
            String bangCT = getTenBangChiTiet(conn);

            StringBuilder sql = new StringBuilder(
                    "SELECT dh.*, ncc.tenNhaCungCap, nv.hoTen AS tenNhanVien " +
                    "FROM " + bangDH + " dh " +
                    "LEFT JOIN NhaCungCap ncc ON dh.maNhaCungCap = ncc.maNhaCungCap " +
                    "LEFT JOIN NhanVien nv ON dh.maNhanVien = nv.maNhanVien " +
                    "WHERE 1=1"
            );

            List<Object> params = new ArrayList<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.trim() + "%";
                sql.append(" AND (dh.maDonHang LIKE ? OR ncc.tenNhaCungCap LIKE ? OR nv.hoTen LIKE ? " +
                        "OR EXISTS (SELECT 1 FROM " + bangCT + " ct JOIN SanPham sp ON ct.maSanPham = sp.maSanPham " +
                        "WHERE ct.maDonHang = dh.maDonHang AND (sp.tenSanPham LIKE ? OR sp.maSanPham LIKE ?)))");
                params.add(kw);
                params.add(kw);
                params.add(kw);
                params.add(kw);
                params.add(kw);
            }

            if (tenHang != null && !tenHang.trim().isEmpty()) {
                String th = "%" + tenHang.trim() + "%";
                sql.append(" AND EXISTS (SELECT 1 FROM " + bangCT + " ct JOIN SanPham sp ON ct.maSanPham = sp.maSanPham " +
                        "WHERE ct.maDonHang = dh.maDonHang AND (sp.tenSanPham LIKE ? OR sp.maSanPham LIKE ?))");
                params.add(th);
                params.add(th);
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

            if (coCotGioLap(conn)) {
                sql.append(" ORDER BY dh.ngayLap DESC, dh.gioLap DESC, dh.maDonHang DESC");
            } else {
                sql.append(" ORDER BY dh.ngayLap DESC, dh.maDonHang DESC");
            }

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
            napTenHangChoDanhSach(conn, list);
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi tìm kiếm đơn nhập hàng: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Cập nhật trạng thái đơn hàng (dùng khi hủy đơn với lý do và người hủy).
     */
    public boolean capNhatTrangThaiHuy(String maDonHang, String maNhanVienHuy, String lyDoHuy) {
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                throw new IllegalStateException("Lỗi khi cập nhật trạng thái đơn nhập: không thể kết nối cơ sở dữ liệu");
            }
            String bangDH = getTenBangDonNhap(conn);
            boolean coTruyVet = coCotTruyVet(conn);

            String sql = coTruyVet ?
                    "UPDATE " + bangDH + " SET trangThai = ?, maNhanVienHuy = ?, lyDoHuy = ? WHERE maDonHang = ? AND trangThai = ?" :
                    "UPDATE " + bangDH + " SET trangThai = ? WHERE maDonHang = ? AND trangThai = ?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setNString(1, DonNhapHang.TRANG_THAI_DA_HUY);
                if (coTruyVet) {
                    ps.setString(2, maNhanVienHuy);
                    ps.setNString(3, lyDoHuy != null ? lyDoHuy.trim() : "");
                    ps.setString(4, maDonHang);
                    ps.setNString(5, DonNhapHang.TRANG_THAI_CHO_NHAN);
                } else {
                    ps.setString(2, maDonHang);
                    ps.setNString(3, DonNhapHang.TRANG_THAI_CHO_NHAN);
                }
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi hủy đơn nhập hàng: " + e.getMessage(), e);
        }
    }

    /**
     * Xác nhận nhận hàng trong transaction:
     * 1. Cập nhật trạng thái đơn thành 'Hoàn tất' (chỉ thực hiện nếu trạng thái hiện tại là 'Chờ nhận hàng').
     * 2. Ghi nhận người nhận, ngày giờ nhận, tổng tiền thực nhận (nếu schema hỗ trợ).
     * 3. Cập nhật soLuongThucNhan cho từng dòng chi tiết (nếu schema hỗ trợ).
     * 4. Cộng số lượng tồn kho theo soLuongThucNhan (dòng có soLuongThucNhan = 0 thì bỏ qua update SanPham, không rollback).
     */
    /**
     * Xác nhận nhận hàng và cộng tồn kho (tương thích ngược).
     */
    public boolean xacNhanNhanHangVaCongTonKho(String maDonHang, String maNhanVienNhan,
                                                LocalDateTime ngayGioNhan, BigDecimal tongTienThucNhan,
                                                List<ChiTietDonNhap> chiTietCapNhat) {
        String ketQua = xacNhanNhanHangThieuVaTaoDonBu(maDonHang, maNhanVienNhan, ngayGioNhan, tongTienThucNhan,
                null, chiTietCapNhat, null);
        return ketQua != null;
    }

    /**
     * Xác nhận nhận hàng có xử lý giao thiếu (Phương án 3):
     * - Cập nhật trạng thái 'Hoàn tất' cho đơn hiện tại với số lượng thực nhận và tổng tiền thực nhận.
     * - Cộng tồn kho chỉ đúng số lượng thực nhận.
     * - Lưu ghi chú/lý do chênh lệch vào lyDoHuy (nếu đóng đơn hoặc tách đơn bù).
     * - Nếu có donNhapBu != null: tạo mới đơn nhập bù cho phần thiếu trong cùng 1 transaction.
     *
     * @return Mã đơn bù (nếu tạo) hoặc mã đơn gốc (nếu không tạo đơn bù)
     */
    public String xacNhanNhanHangThieuVaTaoDonBu(String maDonHang, String maNhanVienNhan,
                                                LocalDateTime ngayGioNhan, BigDecimal tongTienThucNhan,
                                                String ghiChuChenhLech,
                                                List<ChiTietDonNhap> chiTietCapNhat,
                                                DonNhapHang donNhapBu) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            if (conn == null) {
                throw new IllegalStateException("Lỗi khi xác nhận nhận hàng: không thể kết nối cơ sở dữ liệu");
            }
            conn.setAutoCommit(false);

            String bangDH = getTenBangDonNhap(conn);
            String bangCT = getTenBangChiTiet(conn);
            boolean coTN = coCotThucNhan(conn);
            boolean coTruyVet = coCotTruyVet(conn);

            // 1. Kiểm tra danh sách chi tiết
            if (chiTietCapNhat == null || chiTietCapNhat.isEmpty()) {
                conn.rollback();
                throw new IllegalStateException("Đơn hàng không có chi tiết sản phẩm để xác nhận nhận hàng.");
            }

            // 2. Chuyển trạng thái có điều kiện (WHERE trangThai = 'Chờ nhận hàng')
            String sqlUpdateDonHang = coTruyVet ?
                    "UPDATE " + bangDH + " SET trangThai = ?, maNhanVienNhan = ?, ngayGioNhan = ?, tongTienThucNhan = ?, lyDoHuy = ? WHERE maDonHang = ? AND trangThai = ?" :
                    "UPDATE " + bangDH + " SET trangThai = ? WHERE maDonHang = ? AND trangThai = ?";

            try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateDonHang)) {
                psUpdate.setNString(1, DonNhapHang.TRANG_THAI_HOAN_TAT);
                if (coTruyVet) {
                    psUpdate.setString(2, maNhanVienNhan);
                    psUpdate.setTimestamp(3, ngayGioNhan != null ? Timestamp.valueOf(ngayGioNhan) : Timestamp.valueOf(LocalDateTime.now()));
                    psUpdate.setBigDecimal(4, tongTienThucNhan != null ? tongTienThucNhan : BigDecimal.ZERO);
                    psUpdate.setNString(5, ghiChuChenhLech);
                    psUpdate.setString(6, maDonHang);
                    psUpdate.setNString(7, DonNhapHang.TRANG_THAI_CHO_NHAN);
                } else {
                    psUpdate.setString(2, maDonHang);
                    psUpdate.setNString(3, DonNhapHang.TRANG_THAI_CHO_NHAN);
                }

                if (psUpdate.executeUpdate() != 1) {
                    conn.rollback();
                    throw new IllegalStateException("Đơn hàng không ở trạng thái 'Chờ nhận hàng' hoặc đã được xác nhận trước đó.");
                }
            }

            // 3. Cập nhật soLuongThucNhan vào ChiTietDonNhap (nếu có cột)
            if (coTN) {
                String sqlUpdateCT = "UPDATE " + bangCT + " SET soLuongThucNhan = ? WHERE maDonHang = ? AND maSanPham = ?";
                try (PreparedStatement psCT = conn.prepareStatement(sqlUpdateCT)) {
                    for (ChiTietDonNhap ct : chiTietCapNhat) {
                        psCT.setInt(1, ct.getSoLuongThucNhan());
                        psCT.setString(2, maDonHang);
                        psCT.setString(3, ct.getMaSanPham());
                        psCT.addBatch();
                    }
                    psCT.executeBatch();
                }
            }

            // 4. Cộng tồn kho SanPham theo soLuongThucNhan
            // Dòng có soLuongThucNhan = 0 thì BỎ QUA câu UPDATE, KHÔNG rollback
            String sqlUpdateTonKho = "UPDATE SanPham SET soLuongTon = soLuongTon + ? WHERE maSanPham = ?";
            try (PreparedStatement psTonKho = conn.prepareStatement(sqlUpdateTonKho)) {
                for (ChiTietDonNhap ct : chiTietCapNhat) {
                    int thucNhan = ct.getSoLuongThucNhan();
                    if (thucNhan > 0) {
                        psTonKho.setInt(1, thucNhan);
                        psTonKho.setString(2, ct.getMaSanPham());
                        if (psTonKho.executeUpdate() != 1) {
                            conn.rollback();
                            throw new IllegalStateException("Không tìm thấy sản phẩm " + ct.getMaSanPham() + " để cộng tồn kho.");
                        }
                    }
                }
            }

            // 5. Tự động tách đơn nhập bù (Backorder) nếu có yêu cầu
            String maDonBu = null;
            if (donNhapBu != null && donNhapBu.getDanhSachChiTiet() != null && !donNhapBu.getDanhSachChiTiet().isEmpty()) {
                maDonBu = taoMaDonNhapHang(conn);
                donNhapBu.setMaDonHang(maDonBu);
                boolean coGio = coCotGioLap(conn);

                String sqlInsertBu = coGio ?
                        "INSERT INTO " + bangDH + " (maDonHang, maNhaCungCap, maNhanVien, ngayLap, gioLap, tongTien, trangThai, lyDoHuy) VALUES (?, ?, ?, ?, ?, ?, ?, ?)" :
                        "INSERT INTO " + bangDH + " (maDonHang, maNhaCungCap, maNhanVien, ngayLap, tongTien, trangThai, lyDoHuy) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement psBu = conn.prepareStatement(sqlInsertBu)) {
                    psBu.setString(1, maDonBu);
                    psBu.setString(2, donNhapBu.getMaNhaCungCap());
                    psBu.setString(3, donNhapBu.getMaNhanVien());
                    psBu.setDate(4, Date.valueOf(donNhapBu.getNgayLap() != null ? donNhapBu.getNgayLap() : LocalDate.now()));
                    if (coGio) {
                        psBu.setTime(5, Time.valueOf(donNhapBu.getGioLap() != null ? donNhapBu.getGioLap() : LocalTime.now().withNano(0)));
                        psBu.setBigDecimal(6, donNhapBu.getTongTien() != null ? donNhapBu.getTongTien() : BigDecimal.ZERO);
                        psBu.setNString(7, DonNhapHang.TRANG_THAI_CHO_NHAN);
                        psBu.setNString(8, donNhapBu.getLyDoHuy());
                    } else {
                        psBu.setBigDecimal(5, donNhapBu.getTongTien() != null ? donNhapBu.getTongTien() : BigDecimal.ZERO);
                        psBu.setNString(6, DonNhapHang.TRANG_THAI_CHO_NHAN);
                        psBu.setNString(7, donNhapBu.getLyDoHuy());
                    }
                    psBu.executeUpdate();
                }

                String sqlCTBu = coTN ?
                        "INSERT INTO " + bangCT + " (maDonHang, maSanPham, soLuong, soLuongThucNhan, donGia) VALUES (?, ?, ?, ?, ?)" :
                        "INSERT INTO " + bangCT + " (maDonHang, maSanPham, soLuong, donGia) VALUES (?, ?, ?, ?)";
                try (PreparedStatement psCTBu = conn.prepareStatement(sqlCTBu)) {
                    for (ChiTietDonNhap ct : donNhapBu.getDanhSachChiTiet()) {
                        psCTBu.setString(1, maDonBu);
                        psCTBu.setString(2, ct.getMaSanPham());
                        psCTBu.setInt(3, ct.getSoLuong());
                        if (coTN) {
                            psCTBu.setInt(4, ct.getSoLuong());
                            psCTBu.setBigDecimal(5, ct.getDonGia() != null ? ct.getDonGia() : BigDecimal.ZERO);
                        } else {
                            psCTBu.setBigDecimal(4, ct.getDonGia() != null ? ct.getDonGia() : BigDecimal.ZERO);
                        }
                        psCTBu.addBatch();
                    }
                    psCTBu.executeBatch();
                }
            }

            conn.commit();
            return maDonBu != null ? maDonBu : maDonHang;
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

    private String layTenNhanVien(Connection conn, String maNhanVien) {
        if (maNhanVien == null || maNhanVien.isBlank()) return null;
        String sql = "SELECT hoTen FROM NhanVien WHERE maNhanVien = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNhanVien.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getNString("hoTen");
                }
            }
        } catch (SQLException ignored) {}
        return maNhanVien;
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

    private void napTenHangChoDanhSach(Connection conn, List<DonNhapHang> list) {
        if (list == null || list.isEmpty()) return;
        String bangCT = getTenBangChiTiet(conn);
        Map<String, List<String>> mapTenHang = new HashMap<>();
        for (int i = 0; i < list.size(); i += 50) {
            List<DonNhapHang> sub = list.subList(i, Math.min(i + 50, list.size()));
            String placeholders = String.join(",", Collections.nCopies(sub.size(), "?"));
            String sql = "SELECT ct.maDonHang, sp.tenSanPham, ct.soLuong " +
                    "FROM " + bangCT + " ct " +
                    "LEFT JOIN SanPham sp ON ct.maSanPham = sp.maSanPham " +
                    "WHERE ct.maDonHang IN (" + placeholders + ")";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int j = 0; j < sub.size(); j++) {
                    ps.setString(j + 1, sub.get(j).getMaDonHang());
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String maDH = rs.getString("maDonHang");
                        String tenSP = rs.getNString("tenSanPham");
                        int sl = rs.getInt("soLuong");
                        String hienThi = (tenSP != null ? tenSP : "SP") + " (x" + sl + ")";
                        mapTenHang.computeIfAbsent(maDH, k -> new ArrayList<>()).add(hienThi);
                    }
                }
            } catch (SQLException ignored) {}
        }
        for (DonNhapHang dh : list) {
            List<String> ds = mapTenHang.get(dh.getMaDonHang());
            if (ds != null && !ds.isEmpty()) {
                dh.setTenHang(String.join(", ", ds));
            } else {
                dh.setTenHang("");
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
            dh.setTongTienThucNhan(rs.getBigDecimal("tongTienThucNhan"));
        } catch (SQLException ignored) {}

        try {
            dh.setMaNhanVienNhan(rs.getString("maNhanVienNhan"));
            Timestamp tsNhan = rs.getTimestamp("ngayGioNhan");
            if (tsNhan != null) {
                dh.setNgayGioNhan(tsNhan.toLocalDateTime());
            }
        } catch (SQLException ignored) {}

        try {
            dh.setMaNhanVienHuy(rs.getString("maNhanVienHuy"));
            dh.setLyDoHuy(rs.getNString("lyDoHuy"));
        } catch (SQLException ignored) {}

        try {
            dh.setTenNhaCungCap(rs.getNString("tenNhaCungCap"));
        } catch (SQLException ignored) {}

        try {
            dh.setTenNhanVien(rs.getNString("tenNhanVien"));
        } catch (SQLException ignored) {}

        return dh;
    }
}
