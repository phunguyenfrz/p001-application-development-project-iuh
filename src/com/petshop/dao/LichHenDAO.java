package com.petshop.dao;

import com.petshop.entity.ChiTietLichHen;
import com.petshop.entity.KhachHang;
import com.petshop.entity.LichHen;
import com.petshop.entity.NhanVien;
import com.petshop.entity.ThuCung;
import com.petshop.util.DatabaseManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
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
                VALUES (?, ?, ?, ?, ?, CAST(? AS TIME), ?, ?)
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
                    psLh.setString(6, lichHen.getGioHen().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
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
                            if (ct.getCanNangThuCung() > 0) {
                                psCt.setDouble(3, ct.getCanNangThuCung());
                            } else {
                                psCt.setNull(3, Types.DOUBLE);
                            }
                            psCt.setDouble(4, ct.getDonGia());
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
                SET maKhachHang = ?, maThuCung = ?, maNhanVien = ?, ngayHen = ?, gioHen = CAST(? AS TIME), ghiChu = ?
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
                    psLh.setString(5, lichHen.getGioHen().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
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
                                if (ct.getCanNangThuCung() > 0) {
                                    psThem.setDouble(3, ct.getCanNangThuCung());
                                } else {
                                    psThem.setNull(3, Types.DOUBLE);
                                }
                                psThem.setDouble(4, ct.getDonGia());
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

    // Cập nhật trạng thái lịch hẹn (Đã đặt, Đã xác nhận, Đã tiếp nhận, Đang thực hiện, Hoàn thành, Đã hủy)
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

    // Tự động hủy lịch hẹn kèm ghi chú lý do (áp dụng khi đến trễ > 2h hoặc đến sớm > 2h cần tiếp nhận mới)
    public boolean huyLichHenTuDong(String maLichHen, String lyDoHuy) {
        String sql = """
                UPDATE LichHen
                SET trangThai = N'Đã hủy',
                    ghiChu = CASE 
                        WHEN ghiChu IS NULL OR LTRIM(RTRIM(ghiChu)) = '' THEN ?
                        ELSE ghiChu + ' | ' + ?
                    END
                WHERE maLichHen = ? AND trangThai IN (N'Đã đặt', N'Đã xác nhận')
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, lyDoHuy);
            ps.setString(2, lyDoHuy);
            ps.setString(3, maLichHen);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi tự động hủy lịch hẹn: " + e.getMessage(), e);
        }
    }

    // Lấy toàn bộ danh sách lịch hẹn
    public List<LichHen> layDanhSachLichHen() {
        String sql = """
                SELECT lh.maLichHen, lh.maKhachHang, kh.hoTen AS tenKhachHang, kh.soDienThoai,
                       lh.maThuCung, tc.tenThuCung, lh.maNhanVien, nv.hoTen AS tenNhanVien,
                       lh.ngayHen, lh.gioHen, lh.trangThai, lh.ghiChu
                FROM LichHen lh
                LEFT JOIN KhachHang kh ON kh.maKhachHang = lh.maKhachHang
                LEFT JOIN ThuCung tc ON tc.maThuCung = lh.maThuCung
                LEFT JOIN NhanVien nv ON nv.maNhanVien = lh.maNhanVien
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

        for (LichHen lh : danhSach) {
            lh.setDanhSachChiTiet(layChiTietLichHen(lh.getMaLichHen()));
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
                LEFT JOIN KhachHang kh ON kh.maKhachHang = lh.maKhachHang
                LEFT JOIN ThuCung tc ON tc.maThuCung = lh.maThuCung
                LEFT JOIN NhanVien nv ON nv.maNhanVien = lh.maNhanVien
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
                SELECT ct.maLichHen, ct.maDichVu, dv.tenDichVu, ct.canNangThuCung, ct.donGia,
                       tc.maThuCung, tc.tenThuCung
                FROM ChiTietLichHen ct
                LEFT JOIN DichVuChamSoc dv ON dv.maDichVu = ct.maDichVu
                LEFT JOIN LichHen lh ON lh.maLichHen = ct.maLichHen
                LEFT JOIN ThuCung tc ON tc.maThuCung = lh.maThuCung
                WHERE ct.maLichHen = ?
                """;
        List<ChiTietLichHen> danhSach = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLichHen);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tenDv = rs.getString("tenDichVu");
                    double canNang = rs.getDouble("canNangThuCung");
                    double donGia = rs.getDouble("donGia");
                    ChiTietLichHen ct = new ChiTietLichHen(
                            rs.getString("maLichHen"),
                            rs.getString("maDichVu"),
                            tenDv != null ? tenDv : rs.getString("maDichVu"),
                            canNang,
                            donGia
                    );
                    String tenTc = rs.getString("tenThuCung");
                    if (tenTc != null) {
                        ct.setTenThuCung(tenTc);
                    }
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
        return timKiemLichHen(tuKhoaKhach, tuNgay, denNgay, trangThai, null);
    }

    // Tìm kiếm lịch hẹn theo từ khóa khách hàng, khoảng ngày, trạng thái và nhân viên phụ trách
    public List<LichHen> timKiemLichHen(String tuKhoaKhach, LocalDate tuNgay, LocalDate denNgay, String trangThai, String maNhanVien) {
        StringBuilder sql = new StringBuilder("""
                SELECT lh.maLichHen, lh.maKhachHang, kh.hoTen AS tenKhachHang, kh.soDienThoai,
                       lh.maThuCung, tc.tenThuCung, lh.maNhanVien, nv.hoTen AS tenNhanVien,
                       lh.ngayHen, lh.gioHen, lh.trangThai, lh.ghiChu
                FROM LichHen lh
                LEFT JOIN KhachHang kh ON kh.maKhachHang = lh.maKhachHang
                LEFT JOIN ThuCung tc ON tc.maThuCung = lh.maThuCung
                LEFT JOIN NhanVien nv ON nv.maNhanVien = lh.maNhanVien
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

        if (maNhanVien != null && !maNhanVien.trim().isEmpty() && !"Tất cả".equalsIgnoreCase(maNhanVien.trim())) {
            sql.append(" AND lh.maNhanVien = ?");
            params.add(maNhanVien.trim());
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

        for (LichHen lh : danhSach) {
            lh.setDanhSachChiTiet(layChiTietLichHen(lh.getMaLichHen()));
        }

        return danhSach;
    }

    // Kiểm tra trùng lịch hẹn trong cùng ngày và khung giờ (bỏ qua lịch đã hủy hoặc đã hoàn thành)
    public boolean kiemTraTrungLich(LocalDate ngayHen, LocalTime gioHen, String maLichHenBoQua) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*) FROM LichHen
                WHERE ngayHen = ? AND gioHen = CAST(? AS TIME)
                  AND trangThai NOT IN (N'Đã hủy', N'Hoàn thành')
                """);

        if (maLichHenBoQua != null && !maLichHenBoQua.trim().isEmpty()) {
            sql.append(" AND maLichHen != ?");
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setDate(1, Date.valueOf(ngayHen));
            ps.setString(2, gioHen.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            if (maLichHenBoQua != null && !maLichHenBoQua.trim().isEmpty()) {
                ps.setString(3, maLichHenBoQua.trim());
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
        LocalDate ngayHen = (ngay != null) ? ngay.toLocalDate() : null;

        LocalTime gioHen = null;
        try {
            Time gio = rs.getTime("gioHen");
            if (gio != null) {
                gioHen = gio.toLocalTime();
            }
        } catch (Exception ignored) {}

        if (gioHen == null) {
            String gioStr = rs.getString("gioHen");
            if (gioStr != null && !gioStr.trim().isEmpty()) {
                try {
                    gioHen = LocalTime.parse(gioStr.split("\\.")[0]);
                } catch (Exception ignored) {}
            }
        }

        String maKh = rs.getString("maKhachHang");
        String tenKh = rs.getString("tenKhachHang");
        String sdt = rs.getString("soDienThoai");

        String maTc = rs.getString("maThuCung");
        String tenTc = rs.getString("tenThuCung");

        String maNv = rs.getString("maNhanVien");
        String tenNv = rs.getString("tenNhanVien");

        KhachHang kh = null;
        if (maKh != null) {
            kh = new KhachHang();
            kh.setMaKhachHang(maKh);
            kh.setHoTen(tenKh != null ? tenKh : maKh);
            kh.setSoDienThoai(sdt != null ? sdt : "");
        }

        ThuCung tc = null;
        if (maTc != null) {
            tc = new ThuCung();
            tc.setMaThuCung(maTc);
            tc.setTenThuCung(tenTc != null ? tenTc : maTc);
            if (kh != null) {
                tc.setKhachHang(kh);
            }
        }

        NhanVien nv = null;
        if (maNv != null) {
            nv = new NhanVien();
            nv.setMaNhanVien(maNv);
            nv.setHoTen(tenNv != null ? tenNv : maNv);
        }

        LichHen lh = new LichHen(
                rs.getString("maLichHen"),
                kh,
                tc,
                nv,
                ngayHen,
                gioHen,
                rs.getString("trangThai"),
                rs.getString("ghiChu")
        );
        lh.setTenKhachHang(tenKh != null ? tenKh : maKh);
        lh.setSoDienThoai(sdt != null ? sdt : "");
        lh.setTenThuCung(tenTc != null ? tenTc : maTc);
        lh.setTenNhanVien(tenNv != null ? tenNv : maNv);
        return lh;
    }
}
