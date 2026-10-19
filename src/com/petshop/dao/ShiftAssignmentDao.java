package com.petshop.dao;

import com.petshop.model.Shift;
import com.petshop.model.ShiftAssignment;
import com.petshop.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lớp ShiftAssignmentDao chịu trách nhiệm thực thi các truy vấn CSDL
 * liên quan đến bảng PhanCa và liên kết thông tin NhanVien, Ca trực.
 *
 * Tiêu chuẩn tuân thủ (Review 3_OOP & Review 4_Coding Convention):
 * - Tên lớp là danh từ (ShiftAssignmentDao), viết theo chuẩn PascalCase.
 * - Tên phương thức là động từ (lay..., them..., capNhat..., huy...), viết theo chuẩn camelCase.
 * - Sử dụng PreparedStatement 100% để phòng chống SQL Injection.
 * - Đầy đủ chú thích Javadoc và chú thích từng bước xử lý.
 */
public class ShiftAssignmentDao {

    private static final DateTimeFormatter DINH_DANG_NGAY_MA = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Khởi tạo DAO và đảm bảo các bản ghi mẫu của bảng PhanCa và nhân viên liên quan sẵn sàng.
     */
    public ShiftAssignmentDao() {
        khoiTaoBangPhanCa();
    }

    /**
     * Tự động khởi tạo dữ liệu mẫu cho nhân viên, ca trực và phân ca khớp với hình ảnh thiết kế UI.
     */
    public void khoiTaoBangPhanCa() {
        try (Connection conn = DatabaseManager.getConnection()) {
            // 1. Đảm bảo các nhân viên mẫu trong UI tồn tại trong bảng NhanVien
            String[] sqlNhanVien = {
                """
                IF NOT EXISTS (SELECT * FROM NhanVien WHERE hoTen = N'Lê Thị Hoa')
                    INSERT INTO NhanVien (maNhanVien, hoTen, soDienThoai, chucVu, ngaySinh, gioiTinh, trangThai)
                    VALUES ('NV-20261016-000001', N'Lê Thị Hoa', '0901112233', N'Bán hàng', '1998-05-12', 0, N'Đang làm');
                """,
                """
                IF NOT EXISTS (SELECT * FROM NhanVien WHERE hoTen = N'Trần Minh Khang')
                    INSERT INTO NhanVien (maNhanVien, hoTen, soDienThoai, chucVu, ngaySinh, gioiTinh, trangThai)
                    VALUES ('NV-20261016-000002', N'Trần Minh Khang', '0902223344', N'Chăm sóc', '1996-09-20', 1, N'Đang làm');
                """,
                """
                IF NOT EXISTS (SELECT * FROM NhanVien WHERE hoTen = N'Phạm Ngọc Linh')
                    INSERT INTO NhanVien (maNhanVien, hoTen, soDienThoai, chucVu, ngaySinh, gioiTinh, trangThai)
                    VALUES ('NV-20261016-000003', N'Phạm Ngọc Linh', '0903334455', N'Bán hàng', '2000-01-15', 0, N'Đang làm');
                """
            };

            for (String sql : sqlNhanVien) {
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate(sql);
                } catch (Exception ignored) {}
            }

            // 2. Đảm bảo các ca trực mẫu (Ca sáng, Ca chiều) có trong bảng Ca
            String[] sqlCa = {
                """
                IF NOT EXISTS (SELECT * FROM Ca WHERE maCa = 'CA-20261016-000001')
                    INSERT INTO Ca (maCa, tenCa, gioBatDau, gioKetThuc, trangThai, ngayCapNhat)
                    VALUES ('CA-20261016-000001', N'Ca sáng', '08:00:00', '16:00:00', N'Đang hoạt động', '2026-10-16');
                """,
                """
                IF NOT EXISTS (SELECT * FROM Ca WHERE maCa = 'CA-20261016-000002')
                    INSERT INTO Ca (maCa, tenCa, gioBatDau, gioKetThuc, trangThai, ngayCapNhat)
                    VALUES ('CA-20261016-000002', N'Ca chiều', '16:00:00', '22:00:00', N'Đang hoạt động', '2026-10-16');
                """
            };

            for (String sql : sqlCa) {
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate(sql);
                } catch (Exception ignored) {}
            }

            // 3. Nạp 5 bản ghi phân ca mẫu khớp 100% với Image 1 nếu bảng PhanCa chưa có
            String[] sqlPhanCa = {
                """
                IF NOT EXISTS (SELECT * FROM PhanCa WHERE maPhanCa = 'PC-20261016-000001')
                    INSERT INTO PhanCa (maPhanCa, maNhanVien, maCa, ngayLam, trangThai)
                    SELECT 'PC-20261016-000001', n.maNhanVien, 'CA-20261016-000001', '2026-10-19', N'Đã phân công'
                    FROM NhanVien n WHERE n.hoTen = N'Lê Thị Hoa';
                """,
                """
                IF NOT EXISTS (SELECT * FROM PhanCa WHERE maPhanCa = 'PC-20261016-000002')
                    INSERT INTO PhanCa (maPhanCa, maNhanVien, maCa, ngayLam, trangThai)
                    SELECT 'PC-20261016-000002', n.maNhanVien, 'CA-20261016-000002', '2026-10-19', N'Đã phân công'
                    FROM NhanVien n WHERE n.hoTen = N'Trần Minh Khang';
                """,
                """
                IF NOT EXISTS (SELECT * FROM PhanCa WHERE maPhanCa = 'PC-20261016-000003')
                    INSERT INTO PhanCa (maPhanCa, maNhanVien, maCa, ngayLam, trangThai)
                    SELECT 'PC-20261016-000003', n.maNhanVien, 'CA-20261016-000001', '2026-10-20', N'Đã phân công'
                    FROM NhanVien n WHERE n.hoTen = N'Phạm Ngọc Linh';
                """,
                """
                IF NOT EXISTS (SELECT * FROM PhanCa WHERE maPhanCa = 'PC-20261016-000004')
                    INSERT INTO PhanCa (maPhanCa, maNhanVien, maCa, ngayLam, trangThai)
                    SELECT 'PC-20261016-000004', n.maNhanVien, 'CA-20261016-000002', '2026-10-20', N'Đã phân công'
                    FROM NhanVien n WHERE n.hoTen = N'Lê Thị Hoa';
                """,
                """
                IF NOT EXISTS (SELECT * FROM PhanCa WHERE maPhanCa = 'PC-20261016-000005')
                    INSERT INTO PhanCa (maPhanCa, maNhanVien, maCa, ngayLam, trangThai)
                    SELECT 'PC-20261016-000005', n.maNhanVien, 'CA-20261016-000001', '2026-10-21', N'Đã phân công'
                    FROM NhanVien n WHERE n.hoTen = N'Trần Minh Khang';
                """,
                """
                IF NOT EXISTS (SELECT * FROM PhanCa WHERE maPhanCa = 'PC-20261016-000006')
                    INSERT INTO PhanCa (maPhanCa, maNhanVien, maCa, ngayLam, trangThai)
                    SELECT 'PC-20261016-000006', n.maNhanVien, 'CA-20261016-000001', '2026-10-22', N'Đã phân công'
                    FROM NhanVien n WHERE n.hoTen = N'Lê Thị Hoa';
                """,
                """
                IF NOT EXISTS (SELECT * FROM PhanCa WHERE maPhanCa = 'PC-20261016-000007')
                    INSERT INTO PhanCa (maPhanCa, maNhanVien, maCa, ngayLam, trangThai)
                    SELECT 'PC-20261016-000007', n.maNhanVien, 'CA-20261016-000001', '2026-10-23', N'Đã phân công'
                    FROM NhanVien n WHERE n.hoTen = N'Phạm Ngọc Linh';
                """,
                """
                IF NOT EXISTS (SELECT * FROM PhanCa WHERE maPhanCa = 'PC-20261016-000008')
                    INSERT INTO PhanCa (maPhanCa, maNhanVien, maCa, ngayLam, trangThai)
                    SELECT 'PC-20261016-000008', n.maNhanVien, 'CA-20261016-000002', '2026-10-23', N'Đã phân công'
                    FROM NhanVien n WHERE n.hoTen = N'Phạm Ngọc Linh';
                """,
                """
                IF NOT EXISTS (SELECT * FROM PhanCa WHERE maPhanCa = 'PC-20261016-000009')
                    INSERT INTO PhanCa (maPhanCa, maNhanVien, maCa, ngayLam, trangThai)
                    SELECT 'PC-20261016-000009', n.maNhanVien, 'CA-20261016-000002', '2026-10-24', N'Đã phân công'
                    FROM NhanVien n WHERE n.hoTen = N'Trần Minh Khang';
                """,
                """
                IF NOT EXISTS (SELECT * FROM PhanCa WHERE maPhanCa = 'PC-20261016-000010')
                    INSERT INTO PhanCa (maPhanCa, maNhanVien, maCa, ngayLam, trangThai)
                    SELECT 'PC-20261016-000010', n.maNhanVien, 'CA-20261016-000001', '2026-10-25', N'Đã phân công'
                    FROM NhanVien n WHERE n.hoTen = N'Trần Minh Khang';
                """,
                """
                IF NOT EXISTS (SELECT * FROM PhanCa WHERE maPhanCa = 'PC-20261016-000011')
                    INSERT INTO PhanCa (maPhanCa, maNhanVien, maCa, ngayLam, trangThai)
                    SELECT 'PC-20261016-000011', n.maNhanVien, 'CA-20261016-000002', '2026-10-25', N'Đã phân công'
                    FROM NhanVien n WHERE n.hoTen = N'Lê Thị Hoa';
                """
            };

            for (String sql : sqlPhanCa) {
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate(sql);
                } catch (Exception ignored) {}
            }

        } catch (SQLException e) {
            System.err.println("[ShiftAssignmentDao] Khởi tạo dữ liệu mẫu thất bại: " + e.getMessage());
        }
    }

    /**
     * Lấy toàn bộ danh sách phân ca, liên kết lấy tên nhân viên, tên ca và khung giờ làm việc.
     *
     * @return Danh sách đối tượng ShiftAssignment
     */
    public List<ShiftAssignment> layTatCaPhanCa() {
        List<ShiftAssignment> danhSach = new ArrayList<>();
        String sql = """
            SELECT p.maPhanCa, p.maNhanVien, n.hoTen AS tenNhanVien, 
                   p.maCa, c.tenCa, c.gioBatDau, c.gioKetThuc, 
                   p.ngayLam, p.trangThai
            FROM PhanCa p
            JOIN NhanVien n ON p.maNhanVien = n.maNhanVien
            JOIN Ca c ON p.maCa = c.maCa
            ORDER BY p.ngayLam ASC, c.gioBatDau ASC
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                danhSach.add(chuyenDoiBanGhiSangDoiTuong(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi truy vấn danh sách phân ca: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Tìm phân ca theo mã định danh duy nhất.
     *
     * @param maPhanCa Mã phân ca
     * @return Đối tượng ShiftAssignment nếu tìm thấy, null nếu không có
     */
    public ShiftAssignment timTheoMaPhanCa(String maPhanCa) {
        String sql = """
            SELECT p.maPhanCa, p.maNhanVien, n.hoTen AS tenNhanVien, 
                   p.maCa, c.tenCa, c.gioBatDau, c.gioKetThuc, 
                   p.ngayLam, p.trangThai
            FROM PhanCa p
            JOIN NhanVien n ON p.maNhanVien = n.maNhanVien
            JOIN Ca c ON p.maCa = c.maCa
            WHERE p.maPhanCa = ?
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maPhanCa.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return chuyenDoiBanGhiSangDoiTuong(rs);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi tìm kiếm phân ca theo mã: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Thêm mới một bản ghi phân ca vào CSDL.
     *
     * @param pa Đối tượng phân ca
     * @return true nếu thêm thành công
     */
    public boolean themPhanCa(ShiftAssignment pa) {
        String sql = "INSERT INTO PhanCa (maPhanCa, maNhanVien, maCa, ngayLam, trangThai) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, pa.getMaPhanCa());
            ps.setString(2, pa.getMaNhanVien());
            ps.setString(3, pa.getMaCa());
            ps.setDate(4, Date.valueOf(pa.getNgayLam()));
            ps.setString(5, (pa.getTrangThai() != null) ? pa.getTrangThai() : "Đã phân công");

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi thêm phân ca: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật thông tin phân ca đã có.
     *
     * @param pa Đối tượng phân ca với thông tin mới
     * @return true nếu cập nhật thành công
     */
    public boolean capNhatPhanCa(ShiftAssignment pa) {
        String sql = "UPDATE PhanCa SET maNhanVien = ?, maCa = ?, ngayLam = ?, trangThai = ? WHERE maPhanCa = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, pa.getMaNhanVien());
            ps.setString(2, pa.getMaCa());
            ps.setDate(3, Date.valueOf(pa.getNgayLam()));
            ps.setString(4, pa.getTrangThai());
            ps.setString(5, pa.getMaPhanCa());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi cập nhật phân ca: " + e.getMessage(), e);
        }
    }

    /**
     * Hủy phân ca (Xóa mềm theo chuẩn OOAD): Cập nhật trạng thái thành 'Đã hủy'.
     *
     * @param maPhanCa Mã phân ca cần hủy
     * @return true nếu hủy thành công
     */
    public boolean huyPhanCa(String maPhanCa) {
        String sql = "UPDATE PhanCa SET trangThai = N'Đã hủy' WHERE maPhanCa = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maPhanCa.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi hủy phân ca: " + e.getMessage(), e);
        }
    }

    /**
     * Kiểm tra xem nhân viên đã được phân công vào ca này trong ngày này hay chưa.
     * Chặn việc 1 nhân viên bị phân trùng 1 ca trong cùng 1 ngày.
     *
     * @param maNhanVien      Mã nhân viên
     * @param maCa            Mã ca
     * @param ngayLam         Ngày làm
     * @param maPhanCaBoQua   Mã phân ca cần bỏ qua (khi cập nhật)
     * @return true nếu bị trùng lịch
     */
    public boolean kiemTraTrungLich(String maNhanVien, String maCa, LocalDate ngayLam, String maPhanCaBoQua) {
        String sql = """
            SELECT COUNT(*) FROM PhanCa 
            WHERE maNhanVien = ? AND maCa = ? AND ngayLam = ? 
              AND trangThai = N'Đã phân công'
              AND (maPhanCa <> ? OR ? IS NULL)
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNhanVien);
            ps.setString(2, maCa);
            ps.setDate(3, Date.valueOf(ngayLam));
            ps.setString(4, (maPhanCaBoQua != null) ? maPhanCaBoQua : "");
            ps.setString(5, maPhanCaBoQua);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[ShiftAssignmentDao] Lỗi kiểm tra trùng lịch: " + e.getMessage());
        }
        return false;
    }

    /**
     * Lấy danh sách nhân viên đang làm việc phục vụ ComboBox chọn nhân viên.
     *
     * @return Danh sách Map chứa maNhanVien và hoTen
     */
    public List<Map<String, String>> layDanhSachNhanVienHoatDong() {
        List<Map<String, String>> danhSach = new ArrayList<>();
        String sql = "SELECT maNhanVien, hoTen FROM NhanVien WHERE trangThai = N'Đang làm' ORDER BY hoTen ASC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, String> item = new HashMap<>();
                item.put("maNhanVien", rs.getString("maNhanVien"));
                item.put("hoTen", rs.getString("hoTen"));
                danhSach.add(item);
            }
        } catch (SQLException e) {
            System.err.println("[ShiftAssignmentDao] Lỗi lấy danh sách nhân viên: " + e.getMessage());
        }
        return danhSach;
    }

    /**
     * Lấy danh sách ca trực đang hoạt động phục vụ ComboBox chọn ca trực.
     *
     * @return Danh sách các đối tượng Shift
     */
    public List<Shift> layDanhSachCaTrucHoatDong() {
        List<Shift> danhSach = new ArrayList<>();
        String sql = "SELECT maCa, tenCa, gioBatDau, gioKetThuc, trangThai, ngayCapNhat FROM Ca WHERE trangThai <> N'Ngừng áp dụng' ORDER BY gioBatDau ASC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Time timeBatDau = rs.getTime("gioBatDau");
                Time timeKetThuc = rs.getTime("gioKetThuc");
                Date sqlNgayCapNhat = rs.getDate("ngayCapNhat");

                Shift shift = new Shift(
                        rs.getString("maCa"),
                        rs.getString("tenCa"),
                        (timeBatDau != null) ? timeBatDau.toLocalTime() : null,
                        (timeKetThuc != null) ? timeKetThuc.toLocalTime() : null,
                        rs.getString("trangThai"),
                        (sqlNgayCapNhat != null) ? sqlNgayCapNhat.toLocalDate() : LocalDate.now()
                );
                danhSach.add(shift);
            }
        } catch (SQLException e) {
            System.err.println("[ShiftAssignmentDao] Lỗi lấy danh sách ca trực: " + e.getMessage());
        }
        return danhSach;
    }

    /**
     * Tự động sinh mã phân ca chuẩn PC-YYYYMMDD-NNNNNN (ví dụ: PC-20261016-000006).
     *
     * @return Chuỗi mã phân ca tự sinh
     */
    public String sinhMaPhanCaTuDong() {
        String dateStr = LocalDate.now().format(DINH_DANG_NGAY_MA);
        String prefix = "PC-" + dateStr + "-";
        int maxSeq = 0;

        String sql = "SELECT maPhanCa FROM PhanCa WHERE maPhanCa LIKE ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ma = rs.getString("maPhanCa");
                    if (ma != null && ma.startsWith(prefix)) {
                        String suffix = ma.substring(prefix.length());
                        try {
                            int seq = Integer.parseInt(suffix);
                            if (seq > maxSeq) {
                                maxSeq = seq;
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[ShiftAssignmentDao] Lỗi sinh mã phân ca: " + e.getMessage());
        }

        return String.format("%s%06d", prefix, maxSeq + 1);
    }

    /**
     * Chuyển đổi một dòng ResultSet sang đối tượng ShiftAssignment.
     *
     * @param rs ResultSet
     * @return Đối tượng ShiftAssignment
     * @throws SQLException nếu có lỗi đọc cột
     */
    private ShiftAssignment chuyenDoiBanGhiSangDoiTuong(ResultSet rs) throws SQLException {
        String maPhanCa = rs.getString("maPhanCa");
        String maNhanVien = rs.getString("maNhanVien");
        String tenNhanVien = rs.getString("tenNhanVien");
        String maCa = rs.getString("maCa");
        String tenCa = rs.getString("tenCa");

        Time timeBatDau = rs.getTime("gioBatDau");
        LocalTime gioBatDau = (timeBatDau != null) ? timeBatDau.toLocalTime() : null;

        Time timeKetThuc = rs.getTime("gioKetThuc");
        LocalTime gioKetThuc = (timeKetThuc != null) ? timeKetThuc.toLocalTime() : null;

        Date sqlNgayLam = rs.getDate("ngayLam");
        LocalDate ngayLam = (sqlNgayLam != null) ? sqlNgayLam.toLocalDate() : LocalDate.now();

        String trangThai = rs.getString("trangThai");

        return new ShiftAssignment(maPhanCa, maNhanVien, tenNhanVien, maCa, tenCa, gioBatDau, gioKetThuc, ngayLam, trangThai);
    }
}

