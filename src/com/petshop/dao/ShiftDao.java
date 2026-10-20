package com.petshop.dao;

import com.petshop.model.Shift;
import com.petshop.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp ShiftDao xử lý truy vấn dữ liệu từ cơ sở dữ liệu cho bảng Ca trực (Ca).
 *
 * Tuân thủ quy tắc lập trình (Review 3_OOP và Review 4_Coding Convention):
 * - Tên lớp là danh từ (ShiftDao), viết theo chuẩn PascalCase.
 * - Tên phương thức là động từ (lay..., them..., capNhat..., xoa...), viết theo chuẩn camelCase.
 * - Chuẩn hóa toàn bộ truy vấn bằng PreparedStatement chống SQL Injection.
 * - Đầy đủ chú thích Javadoc và chú thích từng bước xử lý.
 */
public class ShiftDao {

    private static final DateTimeFormatter DINH_DANG_NGAY_MA = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Khởi tạo DAO và đảm bảo cấu trúc bảng Ca cũng như dữ liệu mẫu sẵn sàng.
     */
    public ShiftDao() {
        khoiTaoBangCa();
    }

    /**
     * Tự động kiểm tra và nâng cấp bảng Ca, thêm cột ngayCapNhat và nạp dữ liệu mẫu ban đầu.
     */
    public void khoiTaoBangCa() {
        try (Connection conn = DatabaseManager.getConnection()) {
            // 1. Bổ sung cột ngayCapNhat vào bảng Ca nếu chưa tồn tại
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("""
                    IF NOT EXISTS (
                        SELECT * FROM sys.columns 
                        WHERE object_id = OBJECT_ID('Ca') AND name = 'ngayCapNhat'
                    )
                    BEGIN
                        ALTER TABLE Ca ADD ngayCapNhat DATE DEFAULT GETDATE();
                    END
                """);
            }

            // 2. Nới lỏng ràng buộc CHECK trangThai để chấp nhận các trạng thái nghiệp vụ phong phú
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("""
                    DECLARE @constraintName NVARCHAR(200);
                    SELECT @constraintName = name 
                    FROM sys.check_constraints 
                    WHERE parent_object_id = OBJECT_ID('Ca') 
                      AND definition LIKE '%trangThai%';

                    IF @constraintName IS NOT NULL
                    BEGIN
                        EXEC('ALTER TABLE Ca DROP CONSTRAINT ' + @constraintName);
                    END

                    ALTER TABLE Ca ADD CONSTRAINT CK_Ca_TrangThai 
                        CHECK (trangThai IN (N'Áp dụng', N'Ngừng áp dụng', N'Đang hoạt động', N'Sắp bắt đầu', N'Đã kết thúc'));
                """);
            } catch (Exception ignored) {}

            // 3. Nạp dữ liệu mẫu từ thiết kế UI nếu các mã ca mẫu chưa có
            String sqlCheck = "SELECT COUNT(*) FROM Ca WHERE maCa IN ('CA-20261016-000001', 'CA-20261016-000002', 'CA-20261016-000003')";
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sqlCheck)) {
                if (rs.next() && rs.getInt(1) < 3) {
                    String[] sampleSqls = {
                        """
                        IF NOT EXISTS (SELECT * FROM Ca WHERE maCa = 'CA-20261016-000001')
                            INSERT INTO Ca (maCa, tenCa, gioBatDau, gioKetThuc, trangThai, ngayCapNhat)
                            VALUES ('CA-20261016-000001', N'Ca sáng', '08:00:00', '16:00:00', N'Đang hoạt động', '2026-10-16');
                        """,
                        """
                        IF NOT EXISTS (SELECT * FROM Ca WHERE maCa = 'CA-20261016-000002')
                            INSERT INTO Ca (maCa, tenCa, gioBatDau, gioKetThuc, trangThai, ngayCapNhat)
                            VALUES ('CA-20261016-000002', N'Ca chiều', '16:00:00', '22:00:00', N'Sắp bắt đầu', '2026-10-16');
                        """,
                        """
                        IF NOT EXISTS (SELECT * FROM Ca WHERE maCa = 'CA-20261016-000003')
                            INSERT INTO Ca (maCa, tenCa, gioBatDau, gioKetThuc, trangThai, ngayCapNhat)
                            VALUES ('CA-20261016-000003', N'Ca sớm', '06:00:00', '08:00:00', N'Đã kết thúc', '2026-10-16');
                        """
                    };
                    for (String sql : sampleSqls) {
                        st.executeUpdate(sql);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("[ShiftDao] Lỗi khởi tạo bảng Ca: " + e.getMessage());
        }
    }

    /**
     * Lấy toàn bộ danh sách ca trực từ CSDL sắp xếp theo mã định danh hoặc giờ bắt đầu.
     *
     * @return Danh sách các đối tượng Shift
     */
    public List<Shift> layTatCaCaTruc() {
        List<Shift> danhSach = new ArrayList<>();
        String sql = """
                SELECT maCa, tenCa, gioBatDau, gioKetThuc, trangThai, ngayCapNhat
                FROM Ca
                ORDER BY maCa ASC
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                danhSach.add(chuyenDoiBanGhiSangDoiTuong(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi truy vấn danh sách ca trực: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Tìm thông tin một ca trực theo mã định danh.
     *
     * @param maCa Mã ca trực cần tìm (CA-...)
     * @return Đối tượng Shift nếu tìm thấy, null nếu không tồn tại
     */
    public Shift timTheoMaCa(String maCa) {
        if (maCa == null || maCa.isBlank()) {
            return null;
        }

        String sql = """
                SELECT maCa, tenCa, gioBatDau, gioKetThuc, trangThai, ngayCapNhat
                FROM Ca
                WHERE maCa = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maCa.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return chuyenDoiBanGhiSangDoiTuong(rs);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi tìm ca trực theo mã: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Thêm mới một ca trực vào cơ sở dữ liệu.
     *
     * @param ca Đối tượng Shift chứa thông tin cần lưu
     * @return true nếu thêm thành công
     */
    public boolean themCaTruc(Shift ca) {
        String sql = """
                INSERT INTO Ca (maCa, tenCa, gioBatDau, gioKetThuc, trangThai, ngayCapNhat)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ca.getMaCa());
            ps.setString(2, ca.getTenCa());
            ps.setTime(3, Time.valueOf(ca.getGioBatDau()));
            ps.setTime(4, Time.valueOf(ca.getGioKetThuc()));
            ps.setString(5, ca.getTrangThai() != null ? ca.getTrangThai() : "Đang hoạt động");
            ps.setDate(6, Date.valueOf(ca.getNgayCapNhat() != null ? ca.getNgayCapNhat() : LocalDate.now()));

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi thêm mới ca trực: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật thông tin một ca trực đã có trong cơ sở dữ liệu.
     *
     * @param ca Đối tượng Shift với các giá trị đã chỉnh sửa
     * @return true nếu cập nhật thành công
     */
    public boolean capNhatCaTruc(Shift ca) {
        String sql = """
                UPDATE Ca
                SET tenCa = ?,
                    gioBatDau = ?,
                    gioKetThuc = ?,
                    trangThai = ?,
                    ngayCapNhat = ?
                WHERE maCa = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ca.getTenCa());
            ps.setTime(2, Time.valueOf(ca.getGioBatDau()));
            ps.setTime(3, Time.valueOf(ca.getGioKetThuc()));
            ps.setString(4, ca.getTrangThai());
            ps.setDate(5, Date.valueOf(LocalDate.now())); // Cập nhật ngày sửa là hôm nay
            ps.setString(6, ca.getMaCa());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi cập nhật ca trực: " + e.getMessage(), e);
        }
    }

    /**
     * Thực hiện xóa mềm (ngừng hoạt động) ca trực theo đúng chuẩn OOAD.
     * Chuyển trạng thái sang "Ngừng áp dụng" để bảo toàn toàn vẹn tham chiếu với bảng PhanCa.
     *
     * @param maCa Mã ca trực cần ngừng hoạt động
     * @return true nếu xóa mềm thành công
     */
    public boolean xoaMemCaTruc(String maCa) {
        String sql = """
                UPDATE Ca
                SET trangThai = N'Ngừng áp dụng',
                    ngayCapNhat = ?
                WHERE maCa = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(LocalDate.now()));
            ps.setString(2, maCa);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi xóa mềm ca trực: " + e.getMessage(), e);
        }
    }

    /**
     * Tìm kiếm ca trực theo từ khóa (tìm theo mã ca hoặc tên ca).
     *
     * @param tuKhoa Chuỗi từ khóa cần tìm
     * @return Danh sách các ca trực phù hợp
     */
    public List<Shift> timKiemTheoTuKhoa(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            return layTatCaCaTruc();
        }

        List<Shift> ketQua = new ArrayList<>();
        String sql = """
                SELECT maCa, tenCa, gioBatDau, gioKetThuc, trangThai, ngayCapNhat
                FROM Ca
                WHERE LOWER(maCa) LIKE LOWER(?) 
                   OR LOWER(tenCa) LIKE LOWER(?)
                   OR LOWER(trangThai) LIKE LOWER(?)
                ORDER BY maCa ASC
                """;

        String pattern = "%" + tuKhoa.trim() + "%";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ketQua.add(chuyenDoiBanGhiSangDoiTuong(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi tìm kiếm ca trực: " + e.getMessage(), e);
        }
        return ketQua;
    }

    /**
     * Tự động sinh mã ca trực tiếp theo theo định dạng chuẩn: CA-YYYYMMDD-NNNNNN (ví dụ: CA-20261016-000004).
     *
     * @return Chuỗi mã ca trực mới
     */
    public String sinhMaCaTuDong() {
        String dateStr = LocalDate.now().format(DINH_DANG_NGAY_MA);
        String prefix = "CA-" + dateStr + "-";
        int maxSeq = 0;

        String sql = "SELECT maCa FROM Ca WHERE maCa LIKE ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ma = rs.getString("maCa");
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
            System.err.println("[ShiftDao] Lỗi sinh mã ca: " + e.getMessage());
        }

        return String.format("%s%06d", prefix, maxSeq + 1);
    }

    /**
     * Chuyển đổi một bản ghi ResultSet sang đối tượng Shift.
     *
     * @param rs ResultSet hiện hành
     * @return Đối tượng Shift
     * @throws SQLException nếu có lỗi đọc dữ liệu
     */
    private Shift chuyenDoiBanGhiSangDoiTuong(ResultSet rs) throws SQLException {
        String maCa = rs.getString("maCa");
        String tenCa = rs.getString("tenCa");

        Time timeBatDau = rs.getTime("gioBatDau");
        LocalTime gioBatDau = (timeBatDau != null) ? timeBatDau.toLocalTime() : null;

        Time timeKetThuc = rs.getTime("gioKetThuc");
        LocalTime gioKetThuc = (timeKetThuc != null) ? timeKetThuc.toLocalTime() : null;

        String trangThai = rs.getString("trangThai");

        Date sqlNgayCapNhat = rs.getDate("ngayCapNhat");
        LocalDate ngayCapNhat = (sqlNgayCapNhat != null) ? sqlNgayCapNhat.toLocalDate() : LocalDate.now();

        return new Shift(maCa, tenCa, gioBatDau, gioKetThuc, trangThai, ngayCapNhat);
    }
}

