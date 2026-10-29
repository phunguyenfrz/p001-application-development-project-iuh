package com.petshop.dao;

import com.petshop.model.ChiTietLichHen;
import com.petshop.model.LichHen;
import com.petshop.util.DatabaseManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp DAO phụ trách truy xuất dữ liệu lịch hẹn (LichHen, ChiTietLichHen) trong SQL Server.
 */
public class LichHenDAO {

    private static final DateTimeFormatter DINH_DANG_NGAY_MA = DateTimeFormatter.ofPattern("yyyyMMdd");

    // Tự động sinh mã lịch hẹn theo định dạng LH-yyyyMMdd-xxxxxx
    public String taoMaLichHen() {
        String ngay = LocalDate.now().format(DINH_DANG_NGAY_MA);
        String tienTo = "LH-" + ngay + "-";
        String sql = "SELECT TOP 1 maLichHen FROM LichHen WHERE maLichHen LIKE ? ORDER BY maLichHen DESC";

        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, tienTo + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String maCu = rs.getString("maLichHen");
                        String[] phan = maCu.split("-");
                        if (phan.length == 3) {
                            int soThuTu = Integer.parseInt(phan[2]) + 1;
                            return tienTo + String.format("%06d", soThuTu);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi sinh mã lịch hẹn: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Mã lịch hẹn trong cơ sở dữ liệu không đúng định dạng", e);
        }

        return tienTo + "000001";
    }

    // Thêm lịch hẹn mới cùng danh sách dịch vụ chi tiết (dùng transaction)
    public boolean themLichHen(LichHen lichHen, List<ChiTietLichHen> danhSachChiTiet) {
        String sqlLichHen = """
                INSERT INTO LichHen (maLichHen, maKhachHang, maThuCung, maNhanVien, ngayHen, gioHen, trangThai, ghiChu)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        String sqlChiTiet = """
                INSERT INTO ChiTietLichHen (maLichHen, maDichVu, canNangThuCung, donGia)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Thêm vào bảng LichHen
                try (PreparedStatement psLh = conn.prepareStatement(sqlLichHen)) {
                    psLh.setString(1, lichHen.getMaLichHen());
                    psLh.setString(2, lichHen.getMaKhachHang());
                    psLh.setString(3, lichHen.getMaThuCung());
                    psLh.setString(4, lichHen.getMaNhanVien());
                    psLh.setDate(5, Date.valueOf(lichHen.getNgayHen()));
                    psLh.setTime(6, Time.valueOf(lichHen.getGioHen()));
                    psLh.setString(7, lichHen.getTrangThai());
                    psLh.setString(8, lichHen.getGhiChu());
                    psLh.executeUpdate();
                }

                // 2. Thêm các dòng dịch vụ vào ChiTietLichHen
                if (danhSachChiTiet != null && !danhSachChiTiet.isEmpty()) {
                    try (PreparedStatement psCt = conn.prepareStatement(sqlChiTiet)) {
                        for (ChiTietLichHen ct : danhSachChiTiet) {
                            psCt.setString(1, lichHen.getMaLichHen());
                            psCt.setString(2, ct.getMaDichVu());
                            psCt.setBigDecimal(3, ct.getCanNangThuCung());
                            psCt.setBigDecimal(4, ct.getDonGia());
                            psCt.executeUpdate();
                        }
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi thêm lịch hẹn: " + e.getMessage(), e);
        }
    }

    // Cập nhật ngày giờ, ghi chú và danh sách dịch vụ của lịch hẹn
    public boolean capNhatLichHen(LichHen lichHen, List<ChiTietLichHen> danhSachChiTiet) {
        String sqlLichHen = """
                UPDATE LichHen
                SET maKhachHang = ?, maThuCung = ?, maNhanVien = ?, ngayHen = ?, gioHen = ?, ghiChu = ?
                WHERE maLichHen = ?
                """;
        String sqlXoaChiTiet = "DELETE FROM ChiTietLichHen WHERE maLichHen = ?";
        String sqlThemChiTiet = """
                INSERT INTO ChiTietLichHen (maLichHen, maDichVu, canNangThuCung, donGia)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Cập nhật thông tin chính của LichHen
                try (PreparedStatement psLh = conn.prepareStatement(sqlLichHen)) {
                    psLh.setString(1, lichHen.getMaKhachHang());
                    psLh.setString(2, lichHen.getMaThuCung());
                    psLh.setString(3, lichHen.getMaNhanVien());
                    psLh.setDate(4, Date.valueOf(lichHen.getNgayHen()));
                    psLh.setTime(5, Time.valueOf(lichHen.getGioHen()));
                    psLh.setString(6, lichHen.getGhiChu());
                    psLh.setString(7, lichHen.getMaLichHen());
                    psLh.executeUpdate();
                }

                // 2. Nếu có cập nhật danh sách dịch vụ: xóa chi tiết cũ và nạp lại chi tiết mới
                if (danhSachChiTiet != null) {
                    try (PreparedStatement psXoa = conn.prepareStatement(sqlXoaChiTiet)) {
                        psXoa.setString(1, lichHen.getMaLichHen());
                        psXoa.executeUpdate();
                    }

                    if (!danhSachChiTiet.isEmpty()) {
                        try (PreparedStatement psThem = conn.prepareStatement(sqlThemChiTiet)) {
                            for (ChiTietLichHen ct : danhSachChiTiet) {
                                psThem.setString(1, lichHen.getMaLichHen());
                                psThem.setString(2, ct.getMaDichVu());
                                psThem.setBigDecimal(3, ct.getCanNangThuCung());
                                psThem.setBigDecimal(4, ct.getDonGia());
                                psThem.executeUpdate();
                            }
                        }
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi cập nhật lịch hẹn: " + e.getMessage(), e);
        }
    }

    // Cập nhật trạng thái lịch hẹn (Đã đặt, Đã xác nhận, Đang thực hiện, Hoàn thành, Đã hủy)
    public boolean capNhatTrangThai(String maLichHen, String trangThaiMoi) {
        String sql = "UPDATE LichHen SET trangThai = ? WHERE maLichHen = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThaiMoi);
            ps.setString(2, maLichHen);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi cập nhật trạng thái lịch hẹn: " + e.getMessage(), e);
        }
    }

    // Hủy lịch hẹn (chuyển trạng thái sang 'Đã hủy')
    public boolean xoaLichHen(String maLichHen) {
        return capNhatTrangThai(maLichHen, "Đã hủy");
    }

    // Lấy toàn bộ danh sách lịch hẹn
    public List<LichHen> layDanhSachLichHen() {
        String sql = """
                SELECT lh.maLichHen, lh.maKhachHang, kh.hoTen AS tenKhachHang, kh.soDienThoai,
                       lh.maThuCung, tc.tenThuCung, lh.maNhanVien, nv.hoTen AS tenNhanVien,
                       lh.ngayHen, lh.gioHen, lh.trangThai, lh.ghiChu
                FROM LichHen lh
                JOIN KhachHang kh ON kh.maKhachHang = lh.maKhachHang
                JOIN ThuCung tc ON tc.maThuCung = lh.maThuCung
                JOIN NhanVien nv ON nv.maNhanVien = lh.maNhanVien
                ORDER BY lh.ngayHen DESC, lh.gioHen DESC
                """;
        List<LichHen> danhSach = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LichHen lh = docLichHen(rs);
                danhSach.add(lh);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi lấy danh sách lịch hẹn: " + e.getMessage(), e);
        }

        return danhSach;
    }

    // Tìm một lịch hẹn theo mã định danh duy nhất
    public LichHen layLichHenTheoMa(String maLichHen) {
        String sql = """
                SELECT lh.maLichHen, lh.maKhachHang, kh.hoTen AS tenKhachHang, kh.soDienThoai,
                       lh.maThuCung, tc.tenThuCung, lh.maNhanVien, nv.hoTen AS tenNhanVien,
                       lh.ngayHen, lh.gioHen, lh.trangThai, lh.ghiChu
                FROM LichHen lh
                JOIN KhachHang kh ON kh.maKhachHang = lh.maKhachHang
                JOIN ThuCung tc ON tc.maThuCung = lh.maThuCung
                JOIN NhanVien nv ON nv.maNhanVien = lh.maNhanVien
                WHERE lh.maLichHen = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLichHen);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LichHen lh = docLichHen(rs);
                    lh.setDanhSachChiTiet(layChiTietLichHen(maLichHen));
                    return lh;
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi tìm lịch hẹn theo mã: " + e.getMessage(), e);
        }

        return null;
    }

    // Lấy danh sách dịch vụ chi tiết của một lịch hẹn
    public List<ChiTietLichHen> layChiTietLichHen(String maLichHen) {
        String sql = """
                SELECT ct.maLichHen, ct.maDichVu, dv.tenDichVu, ct.canNangThuCung, ct.donGia
                FROM ChiTietLichHen ct
                JOIN DichVuChamSoc dv ON dv.maDichVu = ct.maDichVu
                WHERE ct.maLichHen = ?
                """;
        List<ChiTietLichHen> danhSach = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLichHen);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChiTietLichHen ct = new ChiTietLichHen(
                            rs.getString("maLichHen"),
                            rs.getString("maDichVu"),
                            rs.getString("tenDichVu"),
                            rs.getBigDecimal("canNangThuCung"),
                            rs.getBigDecimal("donGia")
                    );
                    danhSach.add(ct);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi lấy chi tiết dịch vụ lịch hẹn: " + e.getMessage(), e);
        }

        return danhSach;
    }

    // Tìm kiếm lịch hẹn theo từ khóa khách hàng, khoảng ngày và trạng thái
    public List<LichHen> timKiemLichHen(String tuKhoaKhach, LocalDate tuNgay, LocalDate denNgay, String trangThai) {
        StringBuilder sql = new StringBuilder("""
                SELECT lh.maLichHen, lh.maKhachHang, kh.hoTen AS tenKhachHang, kh.soDienThoai,
                       lh.maThuCung, tc.tenThuCung, lh.maNhanVien, nv.hoTen AS tenNhanVien,
                       lh.ngayHen, lh.gioHen, lh.trangThai, lh.ghiChu
                FROM LichHen lh
                JOIN KhachHang kh ON kh.maKhachHang = lh.maKhachHang
                JOIN ThuCung tc ON tc.maThuCung = lh.maThuCung
                JOIN NhanVien nv ON nv.maNhanVien = lh.maNhanVien
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (tuKhoaKhach != null && !tuKhoaKhach.trim().isEmpty()) {
            String tuKhoa = "%" + tuKhoaKhach.trim() + "%";
            sql.append(" AND (kh.hoTen LIKE ? OR kh.soDienThoai LIKE ? OR kh.maKhachHang LIKE ? OR tc.tenThuCung LIKE ? OR lh.maLichHen LIKE ?)");
            params.add(tuKhoa);
            params.add(tuKhoa);
            params.add(tuKhoa);
            params.add(tuKhoa);
            params.add(tuKhoa);
        }

        if (tuNgay != null) {
            sql.append(" AND lh.ngayHen >= ?");
            params.add(Date.valueOf(tuNgay));
        }

        if (denNgay != null) {
            sql.append(" AND lh.ngayHen <= ?");
            params.add(Date.valueOf(denNgay));
        }

        if (trangThai != null && !trangThai.trim().isEmpty() && !"Tất cả".equalsIgnoreCase(trangThai.trim())) {
            sql.append(" AND lh.trangThai = ?");
            params.add(trangThai.trim());
        }

        sql.append(" ORDER BY lh.ngayHen DESC, lh.gioHen DESC");

        List<LichHen> danhSach = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(docLichHen(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi tìm kiếm lịch hẹn: " + e.getMessage(), e);
        }

        return danhSach;
    }

    // Kiểm tra trùng lịch hẹn: cùng thú cưng hoặc cùng nhân viên trong một khung giờ
    public boolean kiemTraTrungLich(String maNhanVien, String maThuCung, LocalDate ngayHen, LocalTime gioHen, String maLichHenBoQua) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*) FROM LichHen
                WHERE ngayHen = ? AND gioHen = ? AND trangThai != N'Đã hủy'
                  AND (maThuCung = ? OR maNhanVien = ?)
                """);

        if (maLichHenBoQua != null && !maLichHenBoQua.trim().isEmpty()) {
            sql.append(" AND maLichHen != ?");
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setDate(1, Date.valueOf(ngayHen));
            ps.setTime(2, Time.valueOf(gioHen));
            ps.setString(3, maThuCung);
            ps.setString(4, maNhanVien);
            if (maLichHenBoQua != null && !maLichHenBoQua.trim().isEmpty()) {
                ps.setString(5, maLichHenBoQua.trim());
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi kiểm tra trùng lịch hẹn: " + e.getMessage(), e);
        }

        return false;
    }

    // Lấy danh sách nhân viên đang làm việc để phân công lịch hẹn
    public List<String[]> layDanhSachNhanVienChamSoc() {
        String sql = "SELECT maNhanVien, hoTen, chucVu FROM NhanVien WHERE trangThai = N'Đang làm' ORDER BY hoTen";
        List<String[]> danhSach = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                danhSach.add(new String[]{
                        rs.getString("maNhanVien"),
                        rs.getString("hoTen"),
                        rs.getString("chucVu")
                });
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi lấy danh sách nhân viên: " + e.getMessage(), e);
        }

        return danhSach;
    }

    // Ánh xạ ResultSet thành đối tượng LichHen
    private LichHen docLichHen(ResultSet rs) throws SQLException {
        Date ngay = rs.getDate("ngayHen");
        Time gio = rs.getTime("gioHen");
        LocalDate ngayHen = (ngay != null) ? ngay.toLocalDate() : null;
        LocalTime gioHen = (gio != null) ? gio.toLocalTime() : null;

        LichHen lh = new LichHen(
                rs.getString("maLichHen"),
                rs.getString("maKhachHang"),
                rs.getString("maThuCung"),
                rs.getString("maNhanVien"),
                ngayHen,
                gioHen,
                rs.getString("trangThai"),
                rs.getString("ghiChu")
        );
        lh.setTenKhachHang(rs.getString("tenKhachHang"));
        lh.setSoDienThoai(rs.getString("soDienThoai"));
        lh.setTenThuCung(rs.getString("tenThuCung"));
        lh.setTenNhanVien(rs.getString("tenNhanVien"));
        return lh;
    }
}

