package com.petshop.dao;

import com.petshop.model.PhanCa;
import com.petshop.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp truy xuất dữ liệu cho bảng PhanCa trong SQL Server.
 */
public class PhanCaDAO {

    private static final DateTimeFormatter DINH_DANG_NGAY_MA = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Tự động sinh mã phân ca dạng PC-yyyyMMdd-XXXXXX.
     */
    public String taoMaPhanCa() {
        String ngay = LocalDate.now().format(DINH_DANG_NGAY_MA);
        String tienTo = "PC-" + ngay + "-";
        String sql = "SELECT TOP 1 maPhanCa FROM PhanCa WHERE maPhanCa LIKE ? ORDER BY maPhanCa DESC";

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                return tienTo + "000001";
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, tienTo + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String maCu = rs.getString("maPhanCa");
                        String[] phan = maCu.split("-");
                        if (phan.length == 3) {
                            int so = Integer.parseInt(phan[2]) + 1;
                            return String.format("%s%06d", tienTo, so);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Fallback khi lỗi
        }
        return tienTo + "000001";
    }

    /**
     * Thêm mới một phân công ca trực.
     */
    public boolean themPhanCa(PhanCa phanCa) {
        String sql = "INSERT INTO PhanCa (maPhanCa, maNhanVien, maCa, ngayLam, trangThai) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                return true;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, phanCa.getMaPhanCa());
                ps.setString(2, phanCa.getMaNhanVien());
                ps.setString(3, phanCa.getMaCa());
                ps.setDate(4, Date.valueOf(phanCa.getNgayLam()));
                ps.setString(5, phanCa.getTrangThai());
                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Lỗi thêm phân ca vào cơ sở dữ liệu: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật phân ca.
     */
    public boolean capNhatPhanCa(PhanCa phanCa) {
        String sql = "UPDATE PhanCa SET maNhanVien = ?, maCa = ?, ngayLam = ?, trangThai = ? WHERE maPhanCa = ?";
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                return true;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, phanCa.getMaNhanVien());
                ps.setString(2, phanCa.getMaCa());
                ps.setDate(3, Date.valueOf(phanCa.getNgayLam()));
                ps.setString(4, phanCa.getTrangThai());
                ps.setString(5, phanCa.getMaPhanCa());
                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Lỗi cập nhật phân ca: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa mềm phân ca (chuyển trạng thái sang Đã hủy).
     */
    public boolean xoaMemPhanCa(String maPhanCa) {
        String sql = "UPDATE PhanCa SET trangThai = ? WHERE maPhanCa = ?";
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                return true;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, PhanCa.TRANG_THAI_DA_HUY);
                ps.setString(2, maPhanCa);
                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Lỗi hủy phân công ca trực: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy danh sách tất cả các phân ca, kèm tên nhân viên và thông tin ca trực.
     */
    public List<PhanCa> layDanhSachPhanCa() {
        List<PhanCa> danhSach = new ArrayList<>();
        String sql = """
                SELECT pc.maPhanCa, pc.maNhanVien, nv.hoTen, pc.maCa, c.tenCa, 
                       c.gioBatDau, c.gioKetThuc, pc.ngayLam, pc.trangThai
                FROM PhanCa pc
                LEFT JOIN NhanVien nv ON pc.maNhanVien = nv.maNhanVien
                JOIN Ca c ON pc.maCa = c.maCa
                ORDER BY pc.ngayLam DESC, c.gioBatDau ASC
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                return taoDuLieuGiaPhanCa();
            }
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(anhXaDongPhanCa(rs));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Lỗi tải danh sách phân ca: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Lấy danh sách phân ca theo một ngày cụ thể.
     */
    public List<PhanCa> layPhanCaTheoNgay(LocalDate ngay) {
        List<PhanCa> danhSach = new ArrayList<>();
        String sql = """
                SELECT pc.maPhanCa, pc.maNhanVien, nv.hoTen, pc.maCa, c.tenCa, 
                       c.gioBatDau, c.gioKetThuc, pc.ngayLam, pc.trangThai
                FROM PhanCa pc
                LEFT JOIN NhanVien nv ON pc.maNhanVien = nv.maNhanVien
                JOIN Ca c ON pc.maCa = c.maCa
                WHERE pc.ngayLam = ?
                ORDER BY c.gioBatDau ASC
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                return taoDuLieuGiaPhanCa().stream()
                        .filter(pc -> ngay.equals(pc.getNgayLam()))
                        .toList();
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDate(1, Date.valueOf(ngay));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        danhSach.add(anhXaDongPhanCa(rs));
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Lỗi tải phân ca theo ngày: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Lấy các phân ca còn hiệu lực (Đã phân công) của một nhân viên trong ngày.
     */
    public List<PhanCa> layPhanCaTheoNhanVienVaNgay(String maNhanVien, LocalDate ngay) {
        List<PhanCa> danhSach = new ArrayList<>();
        String sql = """
                SELECT pc.maPhanCa, pc.maNhanVien, nv.hoTen, pc.maCa, c.tenCa, 
                       c.gioBatDau, c.gioKetThuc, pc.ngayLam, pc.trangThai
                FROM PhanCa pc
                LEFT JOIN NhanVien nv ON pc.maNhanVien = nv.maNhanVien
                JOIN Ca c ON pc.maCa = c.maCa
                WHERE pc.maNhanVien = ? AND pc.ngayLam = ? AND pc.trangThai = ?
                ORDER BY c.gioBatDau ASC
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                return taoDuLieuGiaPhanCa().stream()
                        .filter(pc -> maNhanVien.equals(pc.getMaNhanVien()) && ngay.equals(pc.getNgayLam()) && pc.isDaPhanCong())
                        .toList();
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maNhanVien);
                ps.setDate(2, Date.valueOf(ngay));
                ps.setString(3, PhanCa.TRANG_THAI_DA_PHAN_CONG);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        danhSach.add(anhXaDongPhanCa(rs));
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Lỗi kiểm tra lịch ca của nhân viên: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Tìm kiếm và lọc danh sách phân ca theo các điều kiện.
     */
    public List<PhanCa> timKiemPhanCa(LocalDate ngay, String maNhanVien, String maCa, String trangThai) {
        List<PhanCa> danhSach = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT pc.maPhanCa, pc.maNhanVien, nv.hoTen, pc.maCa, c.tenCa, 
                       c.gioBatDau, c.gioKetThuc, pc.ngayLam, pc.trangThai
                FROM PhanCa pc
                LEFT JOIN NhanVien nv ON pc.maNhanVien = nv.maNhanVien
                JOIN Ca c ON pc.maCa = c.maCa
                WHERE 1 = 1
                """);

        List<Object> thamSo = new ArrayList<>();
        if (ngay != null) {
            sql.append(" AND pc.ngayLam = ?");
            thamSo.add(Date.valueOf(ngay));
        }
        if (maNhanVien != null && !maNhanVien.isBlank() && !"Tất cả".equalsIgnoreCase(maNhanVien)) {
            sql.append(" AND pc.maNhanVien = ?");
            thamSo.add(maNhanVien);
        }
        if (maCa != null && !maCa.isBlank() && !"Tất cả".equalsIgnoreCase(maCa)) {
            sql.append(" AND pc.maCa = ?");
            thamSo.add(maCa);
        }
        if (trangThai != null && !trangThai.isBlank() && !"Tất cả".equalsIgnoreCase(trangThai)) {
            sql.append(" AND pc.trangThai = ?");
            thamSo.add(trangThai);
        }
        sql.append(" ORDER BY pc.ngayLam DESC, c.gioBatDau ASC");

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                return taoDuLieuGiaPhanCa();
            }
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < thamSo.size(); i++) {
                    ps.setObject(i + 1, thamSo.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        danhSach.add(anhXaDongPhanCa(rs));
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Lỗi tìm kiếm phân ca: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Lấy chi tiết phân ca theo mã.
     */
    public PhanCa layTheoMa(String maPhanCa) {
        String sql = """
                SELECT pc.maPhanCa, pc.maNhanVien, nv.hoTen, pc.maCa, c.tenCa, 
                       c.gioBatDau, c.gioKetThuc, pc.ngayLam, pc.trangThai
                FROM PhanCa pc
                LEFT JOIN NhanVien nv ON pc.maNhanVien = nv.maNhanVien
                JOIN Ca c ON pc.maCa = c.maCa
                WHERE pc.maPhanCa = ?
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                return null;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maPhanCa);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return anhXaDongPhanCa(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Lỗi tìm phân ca theo mã: " + e.getMessage(), e);
        }
        return null;
    }

    private PhanCa anhXaDongPhanCa(ResultSet rs) throws SQLException {
        PhanCa pc = new PhanCa();
        pc.setMaPhanCa(rs.getString("maPhanCa"));
        pc.setMaNhanVien(rs.getString("maNhanVien"));
        String hoTen = rs.getString("hoTen");
        pc.setTenNhanVien(hoTen != null ? hoTen : rs.getString("maNhanVien"));
        pc.setMaCa(rs.getString("maCa"));
        pc.setTenCa(rs.getString("tenCa"));

        Time tBatDau = rs.getTime("gioBatDau");
        Time tKetThuc = rs.getTime("gioKetThuc");
        if (tBatDau != null) pc.setGioBatDau(tBatDau.toLocalTime());
        if (tKetThuc != null) pc.setGioKetThuc(tKetThuc.toLocalTime());

        Date dNgayLam = rs.getDate("ngayLam");
        if (dNgayLam != null) pc.setNgayLam(dNgayLam.toLocalDate());

        pc.setTrangThai(rs.getString("trangThai"));
        return pc;
    }

    /**
     * Dữ liệu mẫu hỗ trợ khi không có kết nối cơ sở dữ liệu (tương thích đúng mẫu ảnh lịch tuần).
     */
    private List<PhanCa> taoDuLieuGiaPhanCa() {
        List<PhanCa> ds = new ArrayList<>();
        LocalDate t2 = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

        // Thứ Hai
        ds.add(taoPhanCaGia("PC-20261019-000001", "NV-20261005-000001", "Lê Thị Hoa", "CA-01", "Ca sáng", LocalTime.of(8, 0), LocalTime.of(16, 0), t2));
        ds.add(taoPhanCaGia("PC-20261019-000002", "NV-20261005-000002", "Trần Minh Khang", "CA-02", "Ca chiều", LocalTime.of(16, 0), LocalTime.of(22, 0), t2));

        // Thứ Ba
        ds.add(taoPhanCaGia("PC-20261019-000003", "NV-20261005-000003", "Phạm Ngọc Linh", "CA-01", "Ca sáng", LocalTime.of(8, 0), LocalTime.of(16, 0), t2.plusDays(1)));
        ds.add(taoPhanCaGia("PC-20261019-000004", "NV-20261005-000001", "Lê Thị Hoa", "CA-02", "Ca chiều", LocalTime.of(16, 0), LocalTime.of(22, 0), t2.plusDays(1)));

        // Thứ Tư
        ds.add(taoPhanCaGia("PC-20261019-000005", "NV-20261005-000002", "Trần Minh Khang", "CA-01", "Ca sáng", LocalTime.of(8, 0), LocalTime.of(16, 0), t2.plusDays(2)));

        // Thứ Năm
        ds.add(taoPhanCaGia("PC-20261019-000006", "NV-20261005-000001", "Lê Thị Hoa", "CA-01", "Ca sáng", LocalTime.of(8, 0), LocalTime.of(16, 0), t2.plusDays(3)));
        ds.add(taoPhanCaGia("PC-20261019-000007", "NV-20261005-000003", "Phạm Ngọc Linh", "CA-02", "Ca chiều", LocalTime.of(16, 0), LocalTime.of(22, 0), t2.plusDays(3)));

        // Thứ Sáu
        ds.add(taoPhanCaGia("PC-20261019-000008", "NV-20261005-000003", "Phạm Ngọc Linh", "CA-01", "Ca sáng", LocalTime.of(8, 0), LocalTime.of(16, 0), t2.plusDays(4)));
        ds.add(taoPhanCaGia("PC-20261019-000009", "NV-20261005-000002", "Trần Minh Khang", "CA-02", "Ca chiều", LocalTime.of(16, 0), LocalTime.of(22, 0), t2.plusDays(4)));

        // Thứ Bảy
        ds.add(taoPhanCaGia("PC-20261019-000010", "NV-20261005-000001", "Lê Thị Hoa", "CA-02", "Ca chiều", LocalTime.of(16, 0), LocalTime.of(22, 0), t2.plusDays(5)));

        // Chủ nhật
        ds.add(taoPhanCaGia("PC-20261019-000011", "NV-20261005-000002", "Trần Minh Khang", "CA-01", "Ca sáng", LocalTime.of(8, 0), LocalTime.of(16, 0), t2.plusDays(6)));

        return ds;
    }

    private PhanCa taoPhanCaGia(String ma, String maNV, String tenNV, String maCa, String tenCa, LocalTime batDau, LocalTime ketThuc, LocalDate ngay) {
        PhanCa pc = new PhanCa(ma, maNV, maCa, ngay, PhanCa.TRANG_THAI_DA_PHAN_CONG);
        pc.setTenNhanVien(tenNV);
        pc.setTenCa(tenCa);
        pc.setGioBatDau(batDau);
        pc.setGioKetThuc(ketThuc);
        return pc;
    }
}

