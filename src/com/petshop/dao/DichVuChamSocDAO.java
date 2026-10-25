package com.petshop.dao;

import com.petshop.model.BangGiaDichVu;
import com.petshop.model.DichVuChamSoc;
import com.petshop.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Truy xuất dữ liệu cho bảng DichVuChamSoc và BangGiaDichVu trong SQL Server.
 */
public class DichVuChamSocDAO {

    private static final DateTimeFormatter DINH_DANG_NGAY_MA = DateTimeFormatter.ofPattern("yyyyMMdd");

    // Thêm dịch vụ mới cùng 3 bậc giá (Nhỏ, Vừa, Lớn) trong một transaction
    public boolean themDichVu(DichVuChamSoc dichVu) {
        String sqlDichVu = """
                INSERT INTO DichVuChamSoc (maDichVu, tenDichVu, moTa, thoiLuong, trangThai)
                VALUES (?, ?, ?, ?, ?)
                """;
        String sqlBangGia = """
                INSERT INTO BangGiaDichVu (maBangGia, maDichVu, gia, phanLoaiKichThuoc, ngayApDung, ngayHetHieuLuc)
                VALUES (?, ?, ?, ?, ?, NULL)
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psDv = conn.prepareStatement(sqlDichVu);
                 PreparedStatement psBg = conn.prepareStatement(sqlBangGia)) {

                psDv.setString(1, dichVu.getMaDichVu());
                psDv.setString(2, dichVu.getTenDichVu());
                psDv.setString(3, dichVu.getMoTa());
                psDv.setInt(4, dichVu.getThoiLuong());
                psDv.setBoolean(5, dichVu.isTrangThai());
                psDv.executeUpdate();

                int soThuTuBg = laySoThuTuCuoi(conn, "BangGiaDichVu", "maBangGia");
                String ngayChuoi = LocalDate.now().format(DINH_DANG_NGAY_MA);
                Date ngayApDung = Date.valueOf(LocalDate.now());

                themDongBangGia(psBg, "BG-" + ngayChuoi + "-" + String.format("%06d", soThuTuBg + 1),
                        dichVu.getMaDichVu(), dichVu.getGiaNho(), "Nhỏ", ngayApDung);
                themDongBangGia(psBg, "BG-" + ngayChuoi + "-" + String.format("%06d", soThuTuBg + 2),
                        dichVu.getMaDichVu(), dichVu.getGiaVua(), "Vừa", ngayApDung);
                themDongBangGia(psBg, "BG-" + ngayChuoi + "-" + String.format("%06d", soThuTuBg + 3),
                        dichVu.getMaDichVu(), dichVu.getGiaLon(), "Lớn", ngayApDung);

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi thêm dịch vụ chăm sóc: " + e.getMessage(), e);
        }
    }

    // Cập nhật thông tin dịch vụ và giá hiện hành của 3 bậc kích thước
    public boolean capNhatDichVu(DichVuChamSoc dichVu) {
        String sqlDichVu = """
                UPDATE DichVuChamSoc
                SET tenDichVu = ?, moTa = ?, thoiLuong = ?, trangThai = ?
                WHERE maDichVu = ?
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psDv = conn.prepareStatement(sqlDichVu)) {
                psDv.setString(1, dichVu.getTenDichVu());
                psDv.setString(2, dichVu.getMoTa());
                psDv.setInt(3, dichVu.getThoiLuong());
                psDv.setBoolean(4, dichVu.isTrangThai());
                psDv.setString(5, dichVu.getMaDichVu());
                int soDong = psDv.executeUpdate();
                if (soDong == 0) {
                    conn.rollback();
                    return false;
                }

                capNhatGiaTheoBac(conn, dichVu.getMaDichVu(), "Nhỏ", dichVu.getGiaNho());
                capNhatGiaTheoBac(conn, dichVu.getMaDichVu(), "Vừa", dichVu.getGiaVua());
                capNhatGiaTheoBac(conn, dichVu.getMaDichVu(), "Lớn", dichVu.getGiaLon());

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi cập nhật dịch vụ chăm sóc: " + e.getMessage(), e);
        }
    }

    // Xóa mềm dịch vụ: chuyển trạng thái sang ngừng kinh doanh (trangThai = 0)
    public boolean xoaDichVu(String maDichVu) {
        String sql = "UPDATE DichVuChamSoc SET trangThai = 0 WHERE maDichVu = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maDichVu);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi ngừng kinh doanh dịch vụ: " + e.getMessage(), e);
        }
    }

    // Lấy toàn bộ danh sách dịch vụ kèm giá 3 bậc hiện hành
    public List<DichVuChamSoc> layDanhSachDichVu() {
        List<DichVuChamSoc> danhSach = new ArrayList<>();
        String sql = "SELECT maDichVu, tenDichVu, moTa, thoiLuong, trangThai FROM DichVuChamSoc ORDER BY maDichVu";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DichVuChamSoc dichVu = docDichVuTuResultSet(rs);
                napGiaBaBac(conn, dichVu);
                danhSach.add(dichVu);
            }
            return danhSach;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi lấy danh sách dịch vụ: " + e.getMessage(), e);
        }
    }

    // Lấy một dịch vụ theo mã
    public DichVuChamSoc layDichVuTheoMa(String maDichVu) {
        String sql = "SELECT maDichVu, tenDichVu, moTa, thoiLuong, trangThai FROM DichVuChamSoc WHERE maDichVu = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maDichVu);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DichVuChamSoc dichVu = docDichVuTuResultSet(rs);
                    napGiaBaBac(conn, dichVu);
                    return dichVu;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi lấy dịch vụ theo mã: " + e.getMessage(), e);
        }
    }

    // Tìm kiếm dịch vụ theo mã hoặc tên
    public List<DichVuChamSoc> timKiemDichVu(String tuKhoa) {
        List<DichVuChamSoc> danhSach = new ArrayList<>();
        String sql = """
                SELECT maDichVu, tenDichVu, moTa, thoiLuong, trangThai
                FROM DichVuChamSoc
                WHERE maDichVu LIKE ? OR tenDichVu LIKE ?
                ORDER BY maDichVu
                """;
        String mauTim = "%" + (tuKhoa == null ? "" : tuKhoa.trim()) + "%";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mauTim);
            ps.setString(2, mauTim);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DichVuChamSoc dichVu = docDichVuTuResultSet(rs);
                    napGiaBaBac(conn, dichVu);
                    danhSach.add(dichVu);
                }
            }
            return danhSach;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi tìm kiếm dịch vụ: " + e.getMessage(), e);
        }
    }

    // Sinh mã dịch vụ mới theo định dạng DV-yyyyMMdd-xxxxxx
    public String taoMaDichVu() {
        try (Connection conn = DatabaseManager.getConnection()) {
            int soTiepTheo = laySoThuTuCuoi(conn, "DichVuChamSoc", "maDichVu") + 1;
            String ngayChuoi = LocalDate.now().format(DINH_DANG_NGAY_MA);
            return "DV-" + ngayChuoi + "-" + String.format("%06d", soTiepTheo);
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi tạo mã dịch vụ: " + e.getMessage(), e);
        }
    }

    // Sinh mã bảng giá mới theo định dạng BG-yyyyMMdd-xxxxxx
    public String taoMaBangGia() {
        try (Connection conn = DatabaseManager.getConnection()) {
            int soTiepTheo = laySoThuTuCuoi(conn, "BangGiaDichVu", "maBangGia") + 1;
            String ngayChuoi = LocalDate.now().format(DINH_DANG_NGAY_MA);
            return "BG-" + ngayChuoi + "-" + String.format("%06d", soTiepTheo);
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi tạo mã bảng giá: " + e.getMessage(), e);
        }
    }

    // Lấy danh sách các dòng bảng giá của một dịch vụ
    public List<BangGiaDichVu> layBangGiaTheoDichVu(String maDichVu) {
        List<BangGiaDichVu> danhSach = new ArrayList<>();
        String sql = """
                SELECT maBangGia, maDichVu, gia, phanLoaiKichThuoc, ngayApDung, ngayHetHieuLuc
                FROM BangGiaDichVu
                WHERE maDichVu = ?
                ORDER BY ngayApDung DESC, maBangGia ASC
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maDichVu);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date ngayHet = rs.getDate("ngayHetHieuLuc");
                    BangGiaDichVu bg = new BangGiaDichVu(
                            rs.getString("maBangGia"),
                            rs.getString("maDichVu"),
                            rs.getDouble("gia"),
                            rs.getString("phanLoaiKichThuoc"),
                            rs.getDate("ngayApDung").toLocalDate(),
                            ngayHet != null ? ngayHet.toLocalDate() : null
                    );
                    danhSach.add(bg);
                }
            }
            return danhSach;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi lấy bảng giá dịch vụ: " + e.getMessage(), e);
        }
    }

    // Tra đơn giá hiện hành của dịch vụ theo cân nặng thú cưng (dùng cho Lịch hẹn, Phiếu dịch vụ, Hóa đơn)
    public double traGiaTheoCanNang(String maDichVu, double canNang) {
        String kichThuoc = BangGiaDichVu.xacDinhKichThuoc(canNang);
        String sql = """
                SELECT TOP 1 gia
                FROM BangGiaDichVu
                WHERE maDichVu = ? AND phanLoaiKichThuoc = ?
                  AND (ngayHetHieuLuc IS NULL OR ngayHetHieuLuc >= CAST(GETDATE() AS DATE))
                ORDER BY ngayApDung DESC
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maDichVu);
            ps.setString(2, kichThuoc);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("gia");
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi tra giá dịch vụ theo cân nặng: " + e.getMessage(), e);
        }
    }

    private DichVuChamSoc docDichVuTuResultSet(ResultSet rs) throws SQLException {
        return new DichVuChamSoc(
                rs.getString("maDichVu"),
                rs.getString("tenDichVu"),
                rs.getString("moTa"),
                rs.getInt("thoiLuong"),
                rs.getBoolean("trangThai")
        );
    }

    private void napGiaBaBac(Connection conn, DichVuChamSoc dichVu) throws SQLException {
        String sql = """
                SELECT phanLoaiKichThuoc, gia
                FROM BangGiaDichVu
                WHERE maDichVu = ?
                  AND (ngayHetHieuLuc IS NULL OR ngayHetHieuLuc >= CAST(GETDATE() AS DATE))
                ORDER BY ngayApDung ASC
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dichVu.getMaDichVu());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String bac = rs.getString("phanLoaiKichThuoc");
                    double gia = rs.getDouble("gia");
                    if ("Nhỏ".equals(bac)) {
                        dichVu.setGiaNho(gia);
                    } else if ("Vừa".equals(bac)) {
                        dichVu.setGiaVua(gia);
                    } else if ("Lớn".equals(bac)) {
                        dichVu.setGiaLon(gia);
                    }
                }
            }
        }
    }

    private void themDongBangGia(PreparedStatement psBg, String maBangGia, String maDichVu,
                                 double gia, String kichThuoc, Date ngayApDung) throws SQLException {
        psBg.setString(1, maBangGia);
        psBg.setString(2, maDichVu);
        psBg.setDouble(3, gia);
        psBg.setString(4, kichThuoc);
        psBg.setDate(5, ngayApDung);
        psBg.executeUpdate();
    }

    private void capNhatGiaTheoBac(Connection conn, String maDichVu, String kichThuoc, double giaMoi) throws SQLException {
        String sqlTim = """
                SELECT TOP 1 maBangGia
                FROM BangGiaDichVu
                WHERE maDichVu = ? AND phanLoaiKichThuoc = ?
                ORDER BY ngayApDung DESC
                """;
        String maBangGiaHienTai = null;
        try (PreparedStatement psTim = conn.prepareStatement(sqlTim)) {
            psTim.setString(1, maDichVu);
            psTim.setString(2, kichThuoc);
            try (ResultSet rs = psTim.executeQuery()) {
                if (rs.next()) {
                    maBangGiaHienTai = rs.getString("maBangGia");
                }
            }
        }

        if (maBangGiaHienTai != null) {
            String sqlCapNhat = "UPDATE BangGiaDichVu SET gia = ? WHERE maBangGia = ?";
            try (PreparedStatement psCapNhat = conn.prepareStatement(sqlCapNhat)) {
                psCapNhat.setDouble(1, giaMoi);
                psCapNhat.setString(2, maBangGiaHienTai);
                psCapNhat.executeUpdate();
            }
        } else {
            int soTiepTheo = laySoThuTuCuoi(conn, "BangGiaDichVu", "maBangGia") + 1;
            String ngayChuoi = LocalDate.now().format(DINH_DANG_NGAY_MA);
            String maMoi = "BG-" + ngayChuoi + "-" + String.format("%06d", soTiepTheo);
            String sqlThem = """
                    INSERT INTO BangGiaDichVu (maBangGia, maDichVu, gia, phanLoaiKichThuoc, ngayApDung, ngayHetHieuLuc)
                    VALUES (?, ?, ?, ?, ?, NULL)
                    """;
            try (PreparedStatement psThem = conn.prepareStatement(sqlThem)) {
                themDongBangGia(psThem, maMoi, maDichVu, giaMoi, kichThuoc, Date.valueOf(LocalDate.now()));
            }
        }
    }

    private int laySoThuTuCuoi(Connection conn, String tenBang, String tenCotMa) throws SQLException {
        String sql = "SELECT MAX(CAST(RIGHT(" + tenCotMa + ", 6) AS INT)) FROM " + tenBang;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }
}

