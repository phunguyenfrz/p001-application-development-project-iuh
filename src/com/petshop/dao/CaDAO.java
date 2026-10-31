package com.petshop.dao;

import com.petshop.model.Ca;
import com.petshop.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp truy xuất dữ liệu cho bảng Ca trong SQL Server.
 */
public class CaDAO {

    private static final DateTimeFormatter DINH_DANG_NGAY_MA = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Tự động sinh mã ca mới dạng CA-yyyyMMdd-XXXXXX.
     */
    public String taoMaCa() {
        String ngay = LocalDate.now().format(DINH_DANG_NGAY_MA);
        String tienTo = "CA-" + ngay + "-";
        String sql = "SELECT TOP 1 maCa FROM Ca WHERE maCa LIKE ? ORDER BY maCa DESC";

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                return tienTo + "000001";
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, tienTo + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String maCu = rs.getString("maCa");
                        String[] phan = maCu.split("-");
                        if (phan.length == 3) {
                            int so = Integer.parseInt(phan[2]) + 1;
                            return String.format("%s%06d", tienTo, so);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Trường hợp lỗi fallback mã đầu tiên
        }
        return tienTo + "000001";
    }

    /**
     * Thêm mới ca trực vào CSDL.
     */
    public boolean themCa(Ca ca) {
        String sql = "INSERT INTO Ca (maCa, tenCa, gioBatDau, gioKetThuc, trangThai) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                return true;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, ca.getMaCa());
                ps.setString(2, ca.getTenCa());
                ps.setTime(3, Time.valueOf(ca.getGioBatDau()));
                ps.setTime(4, Time.valueOf(ca.getGioKetThuc()));
                ps.setString(5, ca.getTrangThai());
                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Lỗi thêm ca trực vào cơ sở dữ liệu: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật thông tin ca trực.
     */
    public boolean capNhatCa(Ca ca) {
        String sql = "UPDATE Ca SET tenCa = ?, gioBatDau = ?, gioKetThuc = ?, trangThai = ? WHERE maCa = ?";
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                return true;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, ca.getTenCa());
                ps.setTime(2, Time.valueOf(ca.getGioBatDau()));
                ps.setTime(3, Time.valueOf(ca.getGioKetThuc()));
                ps.setString(4, ca.getTrangThai());
                ps.setString(5, ca.getMaCa());
                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Lỗi cập nhật ca trực: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa mềm ca trực (chuyển trạng thái sang Ngừng áp dụng).
     */
    public boolean xoaMemCa(String maCa) {
        String sql = "UPDATE Ca SET trangThai = ? WHERE maCa = ?";
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                return true;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, Ca.TRANG_THAI_NGUNG);
                ps.setString(2, maCa);
                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Lỗi ngừng áp dụng ca trực: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy danh sách tất cả các ca trực.
     */
    public List<Ca> layDanhSachCa() {
        List<Ca> danhSach = new ArrayList<>();
        String sql = "SELECT maCa, tenCa, gioBatDau, gioKetThuc, trangThai FROM Ca ORDER BY gioBatDau ASC";
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                return taoDuLieuGiaCa();
            }
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Time tBatDau = rs.getTime("gioBatDau");
                    Time tKetThuc = rs.getTime("gioKetThuc");
                    LocalTime gioBatDau = tBatDau != null ? tBatDau.toLocalTime() : LocalTime.of(8, 0);
                    LocalTime gioKetThuc = tKetThuc != null ? tKetThuc.toLocalTime() : LocalTime.of(12, 0);

                    Ca ca = new Ca(
                            rs.getString("maCa"),
                            rs.getString("tenCa"),
                            gioBatDau,
                            gioKetThuc,
                            rs.getString("trangThai")
                    );
                    danhSach.add(ca);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Lỗi tải danh sách ca trực: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Lấy danh sách ca trực đang áp dụng.
     */
    public List<Ca> layDanhSachCaDangApDung() {
        List<Ca> danhSach = new ArrayList<>();
        String sql = "SELECT maCa, tenCa, gioBatDau, gioKetThuc, trangThai FROM Ca WHERE trangThai = ? ORDER BY gioBatDau ASC";
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                return taoDuLieuGiaCa().stream().filter(Ca::isDangApDung).toList();
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, Ca.TRANG_THAI_AP_DUNG);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Time tBatDau = rs.getTime("gioBatDau");
                        Time tKetThuc = rs.getTime("gioKetThuc");
                        LocalTime gioBatDau = tBatDau != null ? tBatDau.toLocalTime() : LocalTime.of(8, 0);
                        LocalTime gioKetThuc = tKetThuc != null ? tKetThuc.toLocalTime() : LocalTime.of(12, 0);

                        Ca ca = new Ca(
                                rs.getString("maCa"),
                                rs.getString("tenCa"),
                                gioBatDau,
                                gioKetThuc,
                                rs.getString("trangThai")
                        );
                        danhSach.add(ca);
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Lỗi tải danh sách ca trực đang áp dụng: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Lấy thông tin một ca trực theo mã ca.
     */
    public Ca layTheoMa(String maCa) {
        String sql = "SELECT maCa, tenCa, gioBatDau, gioKetThuc, trangThai FROM Ca WHERE maCa = ?";
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                for (Ca c : taoDuLieuGiaCa()) {
                    if (c.getMaCa().equalsIgnoreCase(maCa)) return c;
                }
                return null;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maCa);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Time tBatDau = rs.getTime("gioBatDau");
                        Time tKetThuc = rs.getTime("gioKetThuc");
                        LocalTime gioBatDau = tBatDau != null ? tBatDau.toLocalTime() : LocalTime.of(8, 0);
                        LocalTime gioKetThuc = tKetThuc != null ? tKetThuc.toLocalTime() : LocalTime.of(12, 0);

                        return new Ca(
                                rs.getString("maCa"),
                                rs.getString("tenCa"),
                                gioBatDau,
                                gioKetThuc,
                                rs.getString("trangThai")
                        );
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Lỗi tìm ca trực theo mã: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Dữ liệu mẫu hỗ trợ chạy thử khi chưa có kết nối CSDL.
     */
    private List<Ca> taoDuLieuGiaCa() {
        List<Ca> ds = new ArrayList<>();
        ds.add(new Ca("CA-20261005-000001", "Ca Sáng", LocalTime.of(8, 0), LocalTime.of(16, 0), Ca.TRANG_THAI_AP_DUNG));
        ds.add(new Ca("CA-20261005-000002", "Ca Chiều", LocalTime.of(16, 0), LocalTime.of(22, 0), Ca.TRANG_THAI_AP_DUNG));
        ds.add(new Ca("CA-20261005-000003", "Ca Tối", LocalTime.of(18, 0), LocalTime.of(21, 0), Ca.TRANG_THAI_AP_DUNG));
        return ds;
    }
}

