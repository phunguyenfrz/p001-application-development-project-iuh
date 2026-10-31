package com.petshop.dao;

import com.petshop.model.ChiTietPhieuDichVu;
import com.petshop.model.LichHen;
import com.petshop.model.PhieuDichVu;
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
 * Lớp DAO phụ trách truy xuất dữ liệu phiếu dịch vụ và chi tiết phiếu trong SQL Server.
 */
public class PhieuDichVuDAO {

    private static final DateTimeFormatter DINH_DANG_NGAY_MA = DateTimeFormatter.ofPattern("yyyyMMdd");

    // Tự động sinh mã phiếu dịch vụ theo định dạng PDV-yyyyMMdd-xxxxxx
    public String taoMaPhieuDichVu() {
        String ngay = LocalDate.now().format(DINH_DANG_NGAY_MA);
        String tienTo = "PDV-" + ngay + "-";
        String sql = "SELECT TOP 1 maPhieuDichVu FROM PhieuDichVu WHERE maPhieuDichVu LIKE ? ORDER BY maPhieuDichVu DESC";

        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, tienTo + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String maCu = rs.getString("maPhieuDichVu");
                        String[] phan = maCu.split("-");
                        if (phan.length == 3) {
                            int soThuTu = Integer.parseInt(phan[2]) + 1;
                            return tienTo + String.format("%06d", soThuTu);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi sinh mã phiếu dịch vụ: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Mã phiếu dịch vụ trong cơ sở dữ liệu không đúng định dạng", e);
        }

        return tienTo + "000001";
    }

    // Thêm phiếu dịch vụ mới kèm các dòng chi tiết dịch vụ (dùng transaction)
    public boolean themPhieuDichVu(PhieuDichVu phieu, List<ChiTietPhieuDichVu> danhSachChiTiet) {
        String sqlPhieu = """
                INSERT INTO PhieuDichVu (maPhieuDichVu, maKhachHang, maNhanVien, maLichHen, ngayTiepNhan, gioTiepNhan, trangThai, ghiChu)
                VALUES (?, ?, ?, ?, ?, CAST(? AS TIME), ?, ?)
                """;
        String sqlChiTiet = """
                INSERT INTO ChiTietPhieuDichVu (maPhieuDichVu, maThuCung, maDichVu, donGia)
                VALUES (?, ?, ?, ?)
                """;
        String sqlCapNhatLichHen = "UPDATE LichHen SET trangThai = N'Hoàn thành' WHERE maLichHen = ? AND trangThai != N'Đã hủy'";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Thêm vào bảng PhieuDichVu
                try (PreparedStatement psPhieu = conn.prepareStatement(sqlPhieu)) {
                    psPhieu.setString(1, phieu.getMaPhieuDichVu());
                    psPhieu.setString(2, phieu.getMaKhachHang());
                    psPhieu.setString(3, phieu.getMaNhanVien());
                    psPhieu.setString(4, phieu.getMaLichHen() != null && !phieu.getMaLichHen().trim().isEmpty() ? phieu.getMaLichHen().trim() : null);
                    psPhieu.setDate(5, Date.valueOf(phieu.getNgayTiepNhan()));
                    psPhieu.setString(6, phieu.getGioTiepNhan().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    psPhieu.setString(7, phieu.getTrangThai());
                    psPhieu.setString(8, phieu.getGhiChu());
                    psPhieu.executeUpdate();
                }

                // 2. Thêm các dòng dịch vụ vào ChiTietPhieuDichVu
                if (danhSachChiTiet != null && !danhSachChiTiet.isEmpty()) {
                    try (PreparedStatement psCt = conn.prepareStatement(sqlChiTiet)) {
                        for (ChiTietPhieuDichVu ct : danhSachChiTiet) {
                            psCt.setString(1, phieu.getMaPhieuDichVu());
                            psCt.setString(2, ct.getMaThuCung());
                            psCt.setString(3, ct.getMaDichVu());
                            psCt.setBigDecimal(4, ct.getDonGia());
                            psCt.executeUpdate();
                        }
                    }
                }

                // 3. Nếu lập từ lịch hẹn: cập nhật trạng thái lịch hẹn sang 'Đang thực hiện'
                if (phieu.getMaLichHen() != null && !phieu.getMaLichHen().trim().isEmpty()) {
                    try (PreparedStatement psLh = conn.prepareStatement(sqlCapNhatLichHen)) {
                        psLh.setString(1, phieu.getMaLichHen().trim());
                        psLh.executeUpdate();
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
            throw new IllegalStateException("Lỗi khi thêm phiếu dịch vụ: " + e.getMessage(), e);
        }
    }

    // Cập nhật thông tin phiếu và danh sách dịch vụ chi tiết (dùng transaction)
    public boolean capNhatPhieuDichVu(PhieuDichVu phieu, List<ChiTietPhieuDichVu> danhSachChiTiet) {
        String sqlPhieu = """
                UPDATE PhieuDichVu
                SET maKhachHang = ?, maNhanVien = ?, maLichHen = ?, ngayTiepNhan = ?,
                    gioTiepNhan = CAST(? AS TIME), trangThai = ?, ghiChu = ?
                WHERE maPhieuDichVu = ?
                """;
        String sqlXoaChiTiet = "DELETE FROM ChiTietPhieuDichVu WHERE maPhieuDichVu = ?";
        String sqlThemChiTiet = """
                INSERT INTO ChiTietPhieuDichVu (maPhieuDichVu, maThuCung, maDichVu, donGia)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Cập nhật bảng PhieuDichVu
                try (PreparedStatement psPhieu = conn.prepareStatement(sqlPhieu)) {
                    psPhieu.setString(1, phieu.getMaKhachHang());
                    psPhieu.setString(2, phieu.getMaNhanVien());
                    psPhieu.setString(3, phieu.getMaLichHen() != null && !phieu.getMaLichHen().trim().isEmpty() ? phieu.getMaLichHen().trim() : null);
                    psPhieu.setDate(4, Date.valueOf(phieu.getNgayTiepNhan()));
                    psPhieu.setString(5, phieu.getGioTiepNhan().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    psPhieu.setString(6, phieu.getTrangThai());
                    psPhieu.setString(7, phieu.getGhiChu());
                    psPhieu.setString(8, phieu.getMaPhieuDichVu());
                    psPhieu.executeUpdate();
                }

                // 2. Cập nhật chi tiết: xóa cũ và nạp lại
                if (danhSachChiTiet != null) {
                    try (PreparedStatement psXoa = conn.prepareStatement(sqlXoaChiTiet)) {
                        psXoa.setString(1, phieu.getMaPhieuDichVu());
                        psXoa.executeUpdate();
                    }

                    if (!danhSachChiTiet.isEmpty()) {
                        try (PreparedStatement psThem = conn.prepareStatement(sqlThemChiTiet)) {
                            for (ChiTietPhieuDichVu ct : danhSachChiTiet) {
                                psThem.setString(1, phieu.getMaPhieuDichVu());
                                psThem.setString(2, ct.getMaThuCung());
                                psThem.setString(3, ct.getMaDichVu());
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
            throw new IllegalStateException("Lỗi khi cập nhật phiếu dịch vụ: " + e.getMessage(), e);
        }
    }

    // Cập nhật trạng thái tiến độ phiếu dịch vụ
    public boolean capNhatTrangThai(String maPhieuDichVu, String trangThaiMoi) {
        String sql = "UPDATE PhieuDichVu SET trangThai = ? WHERE maPhieuDichVu = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThaiMoi);
            ps.setString(2, maPhieuDichVu);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi cập nhật trạng thái phiếu dịch vụ: " + e.getMessage(), e);
        }
    }

    // Xóa mềm phiếu dịch vụ (chuyển trạng thái sang 'Đã hủy' và lưu lý do)
    public boolean xoaMem(String maPhieuDichVu, String lyDoHuy) {
        String sql = """
                UPDATE PhieuDichVu
                SET trangThai = N'Đã hủy',
                    ghiChu = CASE 
                        WHEN ghiChu IS NULL OR LEN(LTRIM(RTRIM(ghiChu))) = 0 THEN ?
                        ELSE ghiChu + N' | Lý do hủy: ' + ?
                    END
                WHERE maPhieuDichVu = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String lyDo = (lyDoHuy != null && !lyDoHuy.trim().isEmpty()) ? "Lý do hủy: " + lyDoHuy.trim() : "Lý do hủy: Hủy phiếu tại quầy";
            String lyDoKem = (lyDoHuy != null && !lyDoHuy.trim().isEmpty()) ? lyDoHuy.trim() : "Hủy phiếu tại quầy";
            ps.setString(1, lyDo);
            ps.setString(2, lyDoKem);
            ps.setString(3, maPhieuDichVu);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi xóa mềm phiếu dịch vụ: " + e.getMessage(), e);
        }
    }

    // Lấy toàn bộ danh sách phiếu dịch vụ
    public List<PhieuDichVu> layDanhSachPhieuDichVu() {
        String sql = """
                SELECT pdv.maPhieuDichVu, pdv.maKhachHang, kh.hoTen AS tenKhachHang, kh.soDienThoai,
                       pdv.maNhanVien, nv.hoTen AS tenNhanVien, pdv.maLichHen,
                       pdv.ngayTiepNhan, pdv.gioTiepNhan, pdv.trangThai, pdv.ghiChu
                FROM PhieuDichVu pdv
                LEFT JOIN KhachHang kh ON kh.maKhachHang = pdv.maKhachHang
                LEFT JOIN NhanVien nv ON nv.maNhanVien = pdv.maNhanVien
                ORDER BY pdv.ngayTiepNhan DESC, pdv.gioTiepNhan DESC
                """;
        List<PhieuDichVu> danhSach = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                danhSach.add(docPhieuDichVu(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi lấy danh sách phiếu dịch vụ: " + e.getMessage(), e);
        }

        for (PhieuDichVu pdv : danhSach) {
            pdv.setDanhSachChiTiet(layChiTietPhieuDichVu(pdv.getMaPhieuDichVu()));
        }

        return danhSach;
    }

    // Lấy một phiếu dịch vụ theo mã duy nhất
    public PhieuDichVu layPhieuDichVuTheoMa(String maPhieuDichVu) {
        String sql = """
                SELECT pdv.maPhieuDichVu, pdv.maKhachHang, kh.hoTen AS tenKhachHang, kh.soDienThoai,
                       pdv.maNhanVien, nv.hoTen AS tenNhanVien, pdv.maLichHen,
                       pdv.ngayTiepNhan, pdv.gioTiepNhan, pdv.trangThai, pdv.ghiChu
                FROM PhieuDichVu pdv
                LEFT JOIN KhachHang kh ON kh.maKhachHang = pdv.maKhachHang
                LEFT JOIN NhanVien nv ON nv.maNhanVien = pdv.maNhanVien
                WHERE pdv.maPhieuDichVu = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhieuDichVu);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PhieuDichVu pdv = docPhieuDichVu(rs);
                    pdv.setDanhSachChiTiet(layChiTietPhieuDichVu(maPhieuDichVu));
                    return pdv;
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi tìm phiếu dịch vụ theo mã: " + e.getMessage(), e);
        }

        return null;
    }

    // Lấy danh sách chi tiết dịch vụ của một phiếu
    public List<ChiTietPhieuDichVu> layChiTietPhieuDichVu(String maPhieuDichVu) {
        String sql = """
                SELECT ct.maPhieuDichVu, ct.maThuCung, tc.tenThuCung,
                       ct.maDichVu, dv.tenDichVu, ct.donGia
                FROM ChiTietPhieuDichVu ct
                LEFT JOIN ThuCung tc ON tc.maThuCung = ct.maThuCung
                LEFT JOIN DichVuChamSoc dv ON dv.maDichVu = ct.maDichVu
                WHERE ct.maPhieuDichVu = ?
                """;
        List<ChiTietPhieuDichVu> danhSach = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhieuDichVu);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tenTc = rs.getString("tenThuCung");
                    String tenDv = rs.getString("tenDichVu");
                    ChiTietPhieuDichVu ct = new ChiTietPhieuDichVu(
                            rs.getString("maPhieuDichVu"),
                            rs.getString("maThuCung"),
                            tenTc != null ? tenTc : rs.getString("maThuCung"),
                            rs.getString("maDichVu"),
                            tenDv != null ? tenDv : rs.getString("maDichVu"),
                            rs.getBigDecimal("donGia")
                    );
                    danhSach.add(ct);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi lấy chi tiết phiếu dịch vụ: " + e.getMessage(), e);
        }

        return danhSach;
    }

    // Tìm kiếm phiếu dịch vụ theo từ khóa khách hàng/mã phiếu, khoảng ngày và trạng thái
    public List<PhieuDichVu> timKiemPhieuDichVu(String tuKhoa, LocalDate tuNgay, LocalDate denNgay, String trangThai) {
        StringBuilder sql = new StringBuilder("""
                SELECT pdv.maPhieuDichVu, pdv.maKhachHang, kh.hoTen AS tenKhachHang, kh.soDienThoai,
                       pdv.maNhanVien, nv.hoTen AS tenNhanVien, pdv.maLichHen,
                       pdv.ngayTiepNhan, pdv.gioTiepNhan, pdv.trangThai, pdv.ghiChu
                FROM PhieuDichVu pdv
                LEFT JOIN KhachHang kh ON kh.maKhachHang = pdv.maKhachHang
                LEFT JOIN NhanVien nv ON nv.maNhanVien = pdv.maNhanVien
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            String tk = "%" + tuKhoa.trim() + "%";
            sql.append(" AND (pdv.maPhieuDichVu LIKE ? OR kh.hoTen LIKE ? OR kh.soDienThoai LIKE ? OR kh.maKhachHang LIKE ?)");
            params.add(tk);
            params.add(tk);
            params.add(tk);
            params.add(tk);
        }

        if (tuNgay != null) {
            sql.append(" AND pdv.ngayTiepNhan >= ?");
            params.add(Date.valueOf(tuNgay));
        }

        if (denNgay != null) {
            sql.append(" AND pdv.ngayTiepNhan <= ?");
            params.add(Date.valueOf(denNgay));
        }

        if (trangThai != null && !trangThai.trim().isEmpty() && !"Tất cả".equalsIgnoreCase(trangThai.trim())) {
            sql.append(" AND pdv.trangThai = ?");
            params.add(trangThai.trim());
        }

        sql.append(" ORDER BY pdv.ngayTiepNhan DESC, pdv.gioTiepNhan DESC");

        List<PhieuDichVu> danhSach = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(docPhieuDichVu(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi tìm kiếm phiếu dịch vụ: " + e.getMessage(), e);
        }

        for (PhieuDichVu pdv : danhSach) {
            pdv.setDanhSachChiTiet(layChiTietPhieuDichVu(pdv.getMaPhieuDichVu()));
        }

        return danhSach;
    }

    // Lấy danh sách lịch hẹn đang chờ tiếp nhận (trạng thái 'Đã đặt' hoặc 'Đã xác nhận' và chưa lập phiếu)
    public List<LichHen> layDanhSachLichHenChoTiepNhan() {
        String sql = """
                SELECT lh.maLichHen, lh.maKhachHang, kh.hoTen AS tenKhachHang, kh.soDienThoai,
                       lh.maThuCung, tc.tenThuCung, lh.maNhanVien, nv.hoTen AS tenNhanVien,
                       lh.ngayHen, lh.gioHen, lh.trangThai, lh.ghiChu
                FROM LichHen lh
                LEFT JOIN KhachHang kh ON kh.maKhachHang = lh.maKhachHang
                LEFT JOIN ThuCung tc ON tc.maThuCung = lh.maThuCung
                LEFT JOIN NhanVien nv ON nv.maNhanVien = lh.maNhanVien
                WHERE lh.trangThai IN (N'Đã đặt', N'Đã xác nhận')
                  AND NOT EXISTS (
                      SELECT 1 FROM PhieuDichVu pdv
                      WHERE pdv.maLichHen = lh.maLichHen AND pdv.trangThai != N'Đã hủy'
                  )
                ORDER BY lh.ngayHen DESC, lh.gioHen DESC
                """;
        List<LichHen> danhSach = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Date ngay = rs.getDate("ngayHen");
                LocalDate ngayHen = (ngay != null) ? ngay.toLocalDate() : null;

                LocalTime gioHen = null;
                try {
                    Time gio = rs.getTime("gioHen");
                    if (gio != null) gioHen = gio.toLocalTime();
                } catch (Exception ignored) {}

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
                danhSach.add(lh);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi lấy danh sách lịch hẹn chờ tiếp nhận: " + e.getMessage(), e);
        }

        return danhSach;
    }

    // Ánh xạ một dòng ResultSet thành đối tượng PhieuDichVu
    private PhieuDichVu docPhieuDichVu(ResultSet rs) throws SQLException {
        Date ngay = rs.getDate("ngayTiepNhan");
        LocalDate ngayTiepNhan = (ngay != null) ? ngay.toLocalDate() : null;

        LocalTime gioTiepNhan = null;
        try {
            Time gio = rs.getTime("gioTiepNhan");
            if (gio != null) {
                gioTiepNhan = gio.toLocalTime();
            }
        } catch (Exception ignored) {}

        if (gioTiepNhan == null) {
            String gioStr = rs.getString("gioTiepNhan");
            if (gioStr != null && !gioStr.trim().isEmpty()) {
                try {
                    gioTiepNhan = LocalTime.parse(gioStr.split("\\.")[0]);
                } catch (Exception ignored) {}
            }
        }

        PhieuDichVu pdv = new PhieuDichVu(
                rs.getString("maPhieuDichVu"),
                rs.getString("maKhachHang"),
                rs.getString("maNhanVien"),
                rs.getString("maLichHen"),
                ngayTiepNhan,
                gioTiepNhan,
                rs.getString("trangThai"),
                rs.getString("ghiChu")
        );
        String tenKh = rs.getString("tenKhachHang");
        pdv.setTenKhachHang(tenKh != null ? tenKh : rs.getString("maKhachHang"));
        pdv.setSoDienThoai(rs.getString("soDienThoai") != null ? rs.getString("soDienThoai") : "");
        String tenNv = rs.getString("tenNhanVien");
        pdv.setTenNhanVien(tenNv != null ? tenNv : rs.getString("maNhanVien"));
        return pdv;
    }
}

