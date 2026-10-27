package com.petshop.dao;

import com.petshop.model.ChiTietGiaoDich;
import com.petshop.model.KhachHang;
import com.petshop.model.LichSuGiaoDich;
import com.petshop.util.DatabaseManager;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp truy xuất dữ liệu (Data Access Object - DAO) cho phân hệ Khách hàng và Lịch sử giao dịch trong SQL Server.
 * Thực hiện các thao tác:
 * - Quản lý thông tin khách hàng (CRUD, xóa mềm, tìm kiếm)
 * - Tra cứu lịch sử hóa đơn giao dịch của khách hàng
 * - Tra cứu chi tiết sản phẩm và dịch vụ trong từng hóa đơn
 */
public class KhachHangDAO {

    /** Định dạng ngày tạo mã theo chuẩn hệ thống: yyyyMMdd */
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** Biến cờ kiểm tra cột trangThai đã được đảm bảo tồn tại trong cơ sở dữ liệu hay chưa */
    private static volatile boolean daKiemTraCotTrangThai = false;

    public KhachHangDAO() {
        damBaoCotTrangThaiTonTai();
    }

    /**
     * Tự động bổ sung cột trangThai vào bảng KhachHang nếu CSDL chưa có (hỗ trợ tính năng xóa mềm).
     */
    private synchronized void damBaoCotTrangThaiTonTai() {
        if (daKiemTraCotTrangThai) {
            return;
        }
        String sqlKiemTra = """
                IF COL_LENGTH('dbo.KhachHang', 'trangThai') IS NULL
                BEGIN
                    ALTER TABLE dbo.KhachHang
                    ADD trangThai BIT NOT NULL
                        CONSTRAINT DF_KhachHang_TrangThai DEFAULT (1) WITH VALUES;
                END
                """;
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlKiemTra);
            daKiemTraCotTrangThai = true;
        } catch (SQLException e) {
            System.err.println("[KhachHangDAO] Kiểm tra cột trangThai: " + e.getMessage());
        }
    }

    /**
     * Lấy toàn bộ danh sách khách hàng trong cơ sở dữ liệu, sắp xếp giảm dần theo mã.
     */
    public List<KhachHang> layDanhSachKhachHang() {
        List<KhachHang> danhSach = new ArrayList<>();
        String sql = """
                SELECT maKhachHang, hoTen, soDienThoai, email, diemTichLuy, diaChi, ghiChu,
                       ISNULL(trangThai, 1) AS trangThai
                FROM KhachHang
                ORDER BY maKhachHang DESC
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                danhSach.add(anhXaDongKhachHang(rs));
            }
        } catch (SQLException e) {
            System.err.println("[KhachHangDAO] Lỗi khi lấy danh sách khách hàng: " + e.getMessage());
        }
        return danhSach;
    }

    /**
     * Lấy danh sách khách hàng đang hoạt động (trangThai = 1).
     */
    public List<KhachHang> layDanhSachKhachHangDangHoatDong() {
        List<KhachHang> danhSach = new ArrayList<>();
        String sql = """
                SELECT maKhachHang, hoTen, soDienThoai, email, diemTichLuy, diaChi, ghiChu,
                       ISNULL(trangThai, 1) AS trangThai
                FROM KhachHang
                WHERE ISNULL(trangThai, 1) = 1
                ORDER BY hoTen ASC
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                danhSach.add(anhXaDongKhachHang(rs));
            }
        } catch (SQLException e) {
            System.err.println("[KhachHangDAO] Lỗi khi lấy danh sách khách hàng hoạt động: " + e.getMessage());
        }
        return danhSach;
    }

    /**
     * Tra cứu thông tin khách hàng theo mã định danh.
     */
    public KhachHang layKhachHangTheoMa(String maKhachHang) {
        if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
            return null;
        }

        String sql = """
                SELECT maKhachHang, hoTen, soDienThoai, email, diemTichLuy, diaChi, ghiChu,
                       ISNULL(trangThai, 1) AS trangThai
                FROM KhachHang
                WHERE maKhachHang = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maKhachHang.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return anhXaDongKhachHang(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[KhachHangDAO] Lỗi tra cứu khách hàng theo mã: " + e.getMessage());
        }
        return null;
    }

    /**
     * Tra cứu thông tin khách hàng theo số điện thoại (phục vụ kiểm tra trùng lặp).
     */
    public KhachHang layKhachHangTheoSoDienThoai(String soDienThoai) {
        if (soDienThoai == null || soDienThoai.trim().isEmpty()) {
            return null;
        }

        String sql = """
                SELECT maKhachHang, hoTen, soDienThoai, email, diemTichLuy, diaChi, ghiChu,
                       ISNULL(trangThai, 1) AS trangThai
                FROM KhachHang
                WHERE soDienThoai = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, soDienThoai.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return anhXaDongKhachHang(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[KhachHangDAO] Lỗi tra cứu khách hàng theo SĐT: " + e.getMessage());
        }
        return null;
    }

    /**
     * Tìm kiếm và lọc danh sách khách hàng linh hoạt:
     * - Tìm kiếm theo từ khóa qua 4 tiêu chí: mã khách hàng, họ tên, số điện thoại hoặc email.
     * - Lọc theo trạng thái hoạt động: "Tất cả", "Đang hoạt động", "Ngừng hoạt động".
     */
    public List<KhachHang> timKiemKhachHang(String tuKhoa, String locTrangThai) {
        List<KhachHang> danhSach = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT maKhachHang, hoTen, soDienThoai, email, diemTichLuy, diaChi, ghiChu,
                       ISNULL(trangThai, 1) AS trangThai
                FROM KhachHang
                WHERE 1 = 1
                """);

        List<Object> thamSo = new ArrayList<>();

        if ("Đang hoạt động".equalsIgnoreCase(locTrangThai)) {
            sql.append(" AND ISNULL(trangThai, 1) = 1");
        } else if ("Ngừng hoạt động".equalsIgnoreCase(locTrangThai)) {
            sql.append(" AND ISNULL(trangThai, 1) = 0");
        }

        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            String giaTriTim = "%" + tuKhoa.trim() + "%";
            sql.append(" AND (maKhachHang LIKE ? OR hoTen LIKE ? OR soDienThoai LIKE ? OR email LIKE ?)");
            thamSo.add(giaTriTim);
            thamSo.add(giaTriTim);
            thamSo.add(giaTriTim);
            thamSo.add(giaTriTim);
        }

        sql.append(" ORDER BY maKhachHang DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < thamSo.size(); i++) {
                ps.setObject(i + 1, thamSo.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(anhXaDongKhachHang(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[KhachHangDAO] Lỗi khi tìm kiếm khách hàng: " + e.getMessage());
        }

        return danhSach;
    }

    /**
     * Thêm mới một khách hàng vào cơ sở dữ liệu.
     */
    public boolean themKhachHang(KhachHang khachHang) {
        String sql = """
                INSERT INTO KhachHang (maKhachHang, hoTen, soDienThoai, email, diemTichLuy, diaChi, ghiChu, trangThai)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, khachHang.getMaKhachHang());
            ps.setString(2, khachHang.getHoTen());
            ps.setString(3, khachHang.getSoDienThoai());
            ps.setString(4, khachHang.getEmail());
            ps.setInt(5, khachHang.getDiemTichLuy());
            ps.setString(6, khachHang.getDiaChi());
            ps.setString(7, khachHang.getGhiChu());
            ps.setBoolean(8, khachHang.isTrangThai());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[KhachHangDAO] Lỗi khi thêm khách hàng: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cập nhật thông tin khách hàng hiện có.
     */
    public boolean capNhatKhachHang(KhachHang khachHang) {
        String sql = """
                UPDATE KhachHang
                SET hoTen = ?,
                    soDienThoai = ?,
                    email = ?,
                    diemTichLuy = ?,
                    diaChi = ?,
                    ghiChu = ?,
                    trangThai = ?
                WHERE maKhachHang = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, khachHang.getHoTen());
            ps.setString(2, khachHang.getSoDienThoai());
            ps.setString(3, khachHang.getEmail());
            ps.setInt(4, khachHang.getDiemTichLuy());
            ps.setString(5, khachHang.getDiaChi());
            ps.setString(6, khachHang.getGhiChu());
            ps.setBoolean(7, khachHang.isTrangThai());
            ps.setString(8, khachHang.getMaKhachHang());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[KhachHangDAO] Lỗi khi cập nhật khách hàng: " + e.getMessage());
            return false;
        }
    }

    /**
     * Xóa mềm khách hàng (trangThai = 0).
     */
    public boolean xoaMemKhachHang(String maKhachHang) {
        String sql = "UPDATE KhachHang SET trangThai = 0 WHERE maKhachHang = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maKhachHang);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[KhachHangDAO] Lỗi khi xóa mềm khách hàng: " + e.getMessage());
            return false;
        }
    }

    /**
     * Kích hoạt lại khách hàng đã ngừng hoạt động (trangThai = 1).
     */
    public boolean kichHoatLaiKhachHang(String maKhachHang) {
        String sql = "UPDATE KhachHang SET trangThai = 1 WHERE maKhachHang = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maKhachHang);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[KhachHangDAO] Lỗi khi kích hoạt lại khách hàng: " + e.getMessage());
            return false;
        }
    }

    /**
     * Tự động sinh mã khách hàng mới theo mẫu: KH-yyyyMMdd-xxxxxx.
     */
    public String taoMaKhachHang() {
        String ngayChuoi = LocalDate.now().format(DINH_DANG_NGAY);
        String tienTo = "KH-" + ngayChuoi + "-";

        String sql = """
                SELECT MAX(CAST(RIGHT(maKhachHang, 6) AS INT)) AS soCuoi
                FROM KhachHang
                WHERE maKhachHang LIKE ?
                """;

        int soThuTu = 1;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tienTo + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int maxSo = rs.getInt("soCuoi");
                    if (!rs.wasNull()) {
                        soThuTu = maxSo + 1;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[KhachHangDAO] Lỗi khi tạo mã khách hàng: " + e.getMessage());
        }

        return tienTo + String.format("%06d", soThuTu);
    }

    // =========================================================================
    // CÁC PHƯƠNG THỨC TRUY XUẤT LỊCH SỬ GIAO DỊCH & CHI TIẾT HÓA ĐƠN
    // =========================================================================

    /**
     * Lấy danh sách lịch sử hóa đơn giao dịch của khách hàng theo bộ lọc:
     * - Từ ngày đến ngày (nếu có)
     * - Trạng thái thanh toán (Tất cả, Đã thanh toán, Chưa thanh toán, Đã hủy)
     *
     * @param maKhachHang Mã khách hàng cần xem lịch sử
     * @param tuNgay Ngày bắt đầu (null nếu không lọc)
     * @param denNgay Ngày kết thúc (null nếu không lọc)
     * @param trangThaiThanhToan Trạng thái thanh toán ("Tất cả" hoặc null nếu không lọc)
     * @return Danh sách LichSuGiaoDich sắp xếp ngày lập mới nhất lên đầu
     */
    public List<LichSuGiaoDich> layLichSuGiaoDich(String maKhachHang, LocalDate tuNgay, LocalDate denNgay, String trangThaiThanhToan) {
        List<LichSuGiaoDich> danhSach = new ArrayList<>();
        if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
            return danhSach;
        }

        StringBuilder sql = new StringBuilder("""
                SELECT hd.maHoaDon,
                       hd.maKhachHang,
                       kh.hoTen AS tenKhachHang,
                       hd.ngayLap,
                       nv.hoTen AS tenNhanVien,
                       hd.hinhThucThanhToan,
                       hd.trangThaiThanhToan,
                       hd.tongTien
                FROM dbo.HoaDon hd
                JOIN dbo.KhachHang kh ON kh.maKhachHang = hd.maKhachHang
                JOIN dbo.NhanVien nv ON nv.maNhanVien = hd.maNhanVien
                WHERE hd.maKhachHang = ?
                """);

        List<Object> params = new ArrayList<>();
        params.add(maKhachHang.trim());

        if (tuNgay != null) {
            sql.append(" AND hd.ngayLap >= ?");
            params.add(Date.valueOf(tuNgay));
        }

        if (denNgay != null) {
            sql.append(" AND hd.ngayLap <= ?");
            params.add(Date.valueOf(denNgay));
        }

        if (trangThaiThanhToan != null && !trangThaiThanhToan.trim().isEmpty() && !"Tất cả".equalsIgnoreCase(trangThaiThanhToan)) {
            sql.append(" AND hd.trangThaiThanhToan = ?");
            params.add(trangThaiThanhToan.trim());
        }

        sql.append(" ORDER BY hd.ngayLap DESC, hd.maHoaDon DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date d = rs.getDate("ngayLap");
                    LocalDate ngayLap = d != null ? d.toLocalDate() : null;

                    LichSuGiaoDich gd = new LichSuGiaoDich(
                            rs.getString("maHoaDon"),
                            rs.getString("maKhachHang"),
                            rs.getString("tenKhachHang"),
                            ngayLap,
                            rs.getString("tenNhanVien"),
                            rs.getString("hinhThucThanhToan"),
                            rs.getString("trangThaiThanhToan"),
                            rs.getBigDecimal("tongTien")
                    );
                    danhSach.add(gd);
                }
            }
        } catch (SQLException e) {
            System.err.println("[KhachHangDAO] Lỗi lấy lịch sử giao dịch: " + e.getMessage());
        }

        return danhSach;
    }

    /**
     * Lấy danh sách sản phẩm trong một hóa đơn của khách hàng.
     */
    public List<ChiTietGiaoDich> laySanPhamHoaDon(String maKhachHang, String maHoaDon) {
        List<ChiTietGiaoDich> danhSach = new ArrayList<>();
        String sql = """
                SELECT sp.maSanPham AS maDoiTuong,
                       sp.tenSanPham AS tenDoiTuong,
                       ct.soLuong,
                       ct.donGia
                FROM dbo.ChiTietHoaDon_SanPham ct
                JOIN dbo.SanPham sp ON sp.maSanPham = ct.maSanPham
                JOIN dbo.HoaDon hd ON hd.maHoaDon = ct.maHoaDon
                WHERE hd.maKhachHang = ? AND hd.maHoaDon = ?
                ORDER BY sp.tenSanPham, sp.maSanPham
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maKhachHang);
            ps.setString(2, maHoaDon);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(new ChiTietGiaoDich(
                            rs.getString("maDoiTuong"),
                            rs.getString("tenDoiTuong"),
                            rs.getInt("soLuong"),
                            rs.getBigDecimal("donGia")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[KhachHangDAO] Lỗi lấy chi tiết sản phẩm hóa đơn: " + e.getMessage());
        }
        return danhSach;
    }

    /**
     * Lấy danh sách dịch vụ chăm sóc trong một hóa đơn của khách hàng.
     */
    public List<ChiTietGiaoDich> layDichVuHoaDon(String maKhachHang, String maHoaDon) {
        List<ChiTietGiaoDich> danhSach = new ArrayList<>();
        String sql = """
                SELECT dv.maDichVu AS maDoiTuong,
                       dv.tenDichVu AS tenDoiTuong,
                       ct.soLuong,
                       ct.donGia
                FROM dbo.ChiTietHoaDon_DichVu ct
                JOIN dbo.DichVuChamSoc dv ON dv.maDichVu = ct.maDichVu
                JOIN dbo.HoaDon hd ON hd.maHoaDon = ct.maHoaDon
                WHERE hd.maKhachHang = ? AND hd.maHoaDon = ?
                ORDER BY dv.tenDichVu, dv.maDichVu
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maKhachHang);
            ps.setString(2, maHoaDon);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(new ChiTietGiaoDich(
                            rs.getString("maDoiTuong"),
                            rs.getString("tenDoiTuong"),
                            rs.getInt("soLuong"),
                            rs.getBigDecimal("donGia")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[KhachHangDAO] Lỗi lấy chi tiết dịch vụ hóa đơn: " + e.getMessage());
        }
        return danhSach;
    }

    /**
     * Ánh xạ một dòng dữ liệu từ ResultSet sang đối tượng KhachHang.
     */
    private KhachHang anhXaDongKhachHang(ResultSet rs) throws SQLException {
        KhachHang kh = new KhachHang();
        kh.setMaKhachHang(rs.getString("maKhachHang"));
        kh.setHoTen(rs.getString("hoTen"));
        kh.setSoDienThoai(rs.getString("soDienThoai"));
        kh.setEmail(rs.getString("email"));
        kh.setDiemTichLuy(rs.getInt("diemTichLuy"));
        kh.setDiaChi(rs.getString("diaChi"));
        kh.setGhiChu(rs.getString("ghiChu"));

        boolean trangThai = true;
        try {
            trangThai = rs.getBoolean("trangThai");
            if (rs.wasNull()) {
                trangThai = true;
            }
        } catch (SQLException ignored) {
            trangThai = true;
        }
        kh.setTrangThai(trangThai);

        return kh;
    }
}
