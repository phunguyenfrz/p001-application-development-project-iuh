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
 * Lớp truy xuất dữ liệu (Data Access Object - DAO) cho phân hệ Khách hàng trong SQL Server.
 * Tuân thủ nghiêm ngặt Quy ước #60:
 * - Không nuốt ngoại lệ SQLException, chuyển sang IllegalStateException có ngữ cảnh rõ ràng.
 * - Không tự ý chạy DDL runtime (ALTER TABLE).
 * - Đánh dấu TODO cho các truy vấn bảng chéo phân hệ (Hóa đơn).
 */
public class KhachHangDAO {

    /** Định dạng ngày tạo mã theo chuẩn hệ thống: yyyyMMdd */
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    public KhachHangDAO() {
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
            throw new IllegalStateException("Lỗi khi lấy danh sách khách hàng: " + e.getMessage(), e);
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
            throw new IllegalStateException("Lỗi khi lấy danh sách khách hàng đang hoạt động: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Tìm khách hàng theo mã định danh duy nhất.
     */
    public KhachHang timTheoMa(String maKhachHang) {
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
            throw new IllegalStateException("Lỗi khi tìm khách hàng theo mã " + maKhachHang + ": " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Tìm khách hàng theo số điện thoại chính xác.
     */
    public KhachHang timTheoSoDienThoai(String soDienThoai) {
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
            throw new IllegalStateException("Lỗi khi tìm khách hàng theo số điện thoại " + soDienThoai + ": " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Kiểm tra số điện thoại có bị trùng với khách hàng khác hay không.
     */
    public boolean kiemTraTrungSoDienThoai(String soDienThoai, String maKhachHangHienTai) {
        if (soDienThoai == null || soDienThoai.trim().isEmpty()) {
            return false;
        }

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM KhachHang WHERE soDienThoai = ?");
        if (maKhachHangHienTai != null && !maKhachHangHienTai.trim().isEmpty()) {
            sql.append(" AND maKhachHang <> ?");
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            ps.setString(1, soDienThoai.trim());
            if (maKhachHangHienTai != null && !maKhachHangHienTai.trim().isEmpty()) {
                ps.setString(2, maKhachHangHienTai.trim());
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi kiểm tra trùng số điện thoại: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Tìm kiếm khách hàng theo từ khóa và bộ lọc trạng thái.
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

        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            String pattern = "%" + tuKhoa.trim() + "%";
            sql.append(" AND (maKhachHang LIKE ? OR hoTen LIKE ? OR soDienThoai LIKE ? OR email LIKE ?)");
            thamSo.add(pattern);
            thamSo.add(pattern);
            thamSo.add(pattern);
            thamSo.add(pattern);
        }

        if (locTrangThai != null && !locTrangThai.trim().isEmpty()) {
            if ("Đang hoạt động".equalsIgnoreCase(locTrangThai.trim())) {
                sql.append(" AND ISNULL(trangThai, 1) = 1");
            } else if ("Ngừng hoạt động".equalsIgnoreCase(locTrangThai.trim())) {
                sql.append(" AND ISNULL(trangThai, 1) = 0");
            }
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
            throw new IllegalStateException("Lỗi khi tìm kiếm khách hàng: " + e.getMessage(), e);
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
            String email = (khachHang.getEmail() != null && !khachHang.getEmail().trim().isEmpty())
                    ? khachHang.getEmail().trim()
                    : null;
            ps.setString(4, email);
            ps.setInt(5, khachHang.getDiemTichLuy());
            ps.setString(6, khachHang.getDiaChi());
            ps.setString(7, khachHang.getGhiChu());
            ps.setBoolean(8, khachHang.isTrangThai());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi thêm mới khách hàng: " + e.getMessage(), e);
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
            String email = (khachHang.getEmail() != null && !khachHang.getEmail().trim().isEmpty())
                    ? khachHang.getEmail().trim()
                    : null;
            ps.setString(3, email);
            ps.setInt(4, khachHang.getDiemTichLuy());
            ps.setString(5, khachHang.getDiaChi());
            ps.setString(6, khachHang.getGhiChu());
            ps.setBoolean(7, khachHang.isTrangThai());
            ps.setString(8, khachHang.getMaKhachHang());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi cập nhật thông tin khách hàng: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa mềm khách hàng (chuyển trangThai = 0).
     */
    public boolean xoaMemKhachHang(String maKhachHang) {
        String sql = "UPDATE KhachHang SET trangThai = 0 WHERE maKhachHang = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maKhachHang);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi xóa mềm khách hàng " + maKhachHang + ": " + e.getMessage(), e);
        }
    }

    /**
     * Kích hoạt lại khách hàng đã ngừng hoạt động (chuyển trangThai = 1).
     */
    public boolean kichHoatLaiKhachHang(String maKhachHang) {
        String sql = "UPDATE KhachHang SET trangThai = 1 WHERE maKhachHang = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maKhachHang);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi kích hoạt lại khách hàng " + maKhachHang + ": " + e.getMessage(), e);
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
            throw new IllegalStateException("Lỗi khi sinh mã khách hàng tự động: " + e.getMessage(), e);
        }

        return tienTo + String.format("%06d", soThuTu);
    }

    // =========================================================================
    // CÁC PHƯƠNG THỨC TRUY XUẤT LỊCH SỬ GIAO DỊCH & CHI TIẾT HÓA ĐƠN
    // TODO: nối HoaDonService khi phân hệ Hóa đơn merge theo Quy ước #60 (Mục 7.3)
    // =========================================================================

    /**
     * Lấy danh sách lịch sử hóa đơn giao dịch của khách hàng.
     * // TODO: nối HoaDonService khi phân hệ Hóa đơn merge theo Quy ước #60 (Mục 7.3)
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
            throw new IllegalStateException("Lỗi khi lấy lịch sử giao dịch khách hàng: " + e.getMessage(), e);
        }

        return danhSach;
    }

    /**
     * Lấy danh sách sản phẩm trong một hóa đơn của khách hàng.
     * // TODO: nối HoaDonService khi phân hệ Hóa đơn merge theo Quy ước #60 (Mục 7.3)
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
            throw new IllegalStateException("Lỗi khi lấy chi tiết sản phẩm hóa đơn: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Lấy danh sách dịch vụ chăm sóc trong một hóa đơn của khách hàng.
     * // TODO: nối HoaDonService khi phân hệ Hóa đơn merge theo Quy ước #60 (Mục 7.3)
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
            throw new IllegalStateException("Lỗi khi lấy chi tiết dịch vụ hóa đơn: " + e.getMessage(), e);
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
