package com.petshop.dao;

import com.petshop.model.Promotion;
import com.petshop.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp PromotionDao xử lý truy vấn dữ liệu từ cơ sở dữ liệu cho bảng KhuyenMai.
 * Tuân thủ mô hình DAO (Data Access Object) và chuẩn hóa truy vấn bằng PreparedStatement.
 */
public class PromotionDao {

    /**
     * Khởi tạo DAO và đảm bảo cấu trúc bảng cũng như dữ liệu mẫu được cập nhật.
     */
    public PromotionDao() {
        khoiTaoBangKhuyenMai();
    }

    /**
     * Tự động kiểm tra và nâng cấp bảng KhuyenMai, nạp dữ liệu mẫu ban đầu nếu cần.
     */
    public void khoiTaoBangKhuyenMai() {
        try (Connection conn = DatabaseManager.getConnection()) {
            // Đảm bảo có cột ngayCapNhat
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("""
                    IF NOT EXISTS (
                        SELECT * FROM sys.columns 
                        WHERE object_id = OBJECT_ID('KhuyenMai') AND name = 'ngayCapNhat'
                    )
                    BEGIN
                        ALTER TABLE KhuyenMai ADD ngayCapNhat DATE DEFAULT GETDATE();
                    END
                """);
            }

            // Nới lỏng ràng buộc CHECK maKhuyenMai để hỗ trợ cả định dạng KM-YYYY-NNN và KM-YYYYMMDD-NNNNNN
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("""
                    DECLARE @constraintName NVARCHAR(200);
                    SELECT @constraintName = name 
                    FROM sys.check_constraints 
                    WHERE parent_object_id = OBJECT_ID('KhuyenMai') 
                      AND definition LIKE '%maKhuyenMai%';

                    IF @constraintName IS NOT NULL
                    BEGIN
                        EXEC('ALTER TABLE KhuyenMai DROP CONSTRAINT ' + @constraintName);
                    END

                    ALTER TABLE KhuyenMai ADD CONSTRAINT CK_KhuyenMai_MaKhuyenMai 
                        CHECK (maKhuyenMai LIKE 'KM-[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9][0-9][0-9]' 
                            OR maKhuyenMai LIKE 'KM-[0-9][0-9][0-9][0-9]-[0-9][0-9][0-9]%');
                """);
            } catch (Exception ignored) {
                // Ràng buộc đã tồn tại hoặc đã được tạo
            }

            // Nạp dữ liệu mẫu từ thiết kế UI nếu bảng đang trống
            String countSql = "SELECT COUNT(*) FROM KhuyenMai";
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(countSql)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String[] sampleSqls = {
                        "INSERT INTO KhuyenMai (maKhuyenMai, tenChuongTrinh, phanTramGiam, ngayBatDau, ngayKetThuc, trangThai, ngayCapNhat) " +
                        "VALUES ('KM-2026-081', N'Tháng 10 - Giảm 12%', 12.00, '2026-10-01', '2026-10-31', 1, '2026-10-12')",

                        "INSERT INTO KhuyenMai (maKhuyenMai, tenChuongTrinh, phanTramGiam, ngayBatDau, ngayKetThuc, trangThai, ngayCapNhat) " +
                        "VALUES ('KM-2026-080', N'Thức ăn hạt - Giảm 10%', 10.00, '2026-11-01', '2026-11-30', 1, '2026-10-05')",

                        "INSERT INTO KhuyenMai (maKhuyenMai, tenChuongTrinh, phanTramGiam, ngayBatDau, ngayKetThuc, trangThai, ngayCapNhat) " +
                        "VALUES ('KM-2026-079', N'Khách hàng mới - Giảm 15%', 15.00, '2026-10-01', '2026-12-31', 0, '2026-09-20')",

                        "INSERT INTO KhuyenMai (maKhuyenMai, tenChuongTrinh, phanTramGiam, ngayBatDau, ngayKetThuc, trangThai, ngayCapNhat) " +
                        "VALUES ('KM-2026-078', N'Combo chăm sóc toàn diện cho chó nhỏ', 8.00, '2026-10-15', '2026-11-15', 1, '2026-10-16')",

                        "INSERT INTO KhuyenMai (maKhuyenMai, tenChuongTrinh, phanTramGiam, ngayBatDau, ngayKetThuc, trangThai, ngayCapNhat) " +
                        "VALUES ('KM-2026-077', N'Cuối tuần - Giảm 5% đơn từ 500k', 5.00, '2026-10-01', '2026-10-31', 1, '2026-10-16')"
                    };
                    for (String sql : sampleSqls) {
                        st.executeUpdate(sql);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[PromotionDao] Lỗi khởi tạo bảng KhuyenMai: " + e.getMessage());
        }
    }

    /**
     * Lấy toàn bộ danh sách khuyến mãi từ CSDL theo thứ tự ngày cập nhật giảm dần.
     *
     * @return Danh sách các đối tượng Promotion
     */
    public List<Promotion> layTatCaKhuyenMai() {
        List<Promotion> danhSach = new ArrayList<>();
        String sql = """
                SELECT maKhuyenMai, tenChuongTrinh, phanTramGiam, ngayBatDau, ngayKetThuc, trangThai, ngayCapNhat
                FROM KhuyenMai
                ORDER BY maKhuyenMai DESC
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Promotion km = chuyenDoiBanGhiSangDoiTuong(rs);
                danhSach.add(km);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi truy vấn danh sách khuyến mãi: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Tìm kiếm danh sách khuyến mãi theo từ khóa (tìm theo mã định danh hoặc tên chương trình).
     *
     * @param tuKhoa Chuỗi từ khóa cần tìm
     * @return Danh sách các khuyến mãi thỏa điều kiện
     */
    public List<Promotion> timKiemTheoTuKhoa(String tuKhoa) {
        List<Promotion> ketQua = new ArrayList<>();
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            return layTatCaKhuyenMai();
        }

        String sql = """
                SELECT maKhuyenMai, tenChuongTrinh, phanTramGiam, ngayBatDau, ngayKetThuc, trangThai, ngayCapNhat
                FROM KhuyenMai
                WHERE LOWER(maKhuyenMai) LIKE LOWER(?) 
                   OR LOWER(tenChuongTrinh) LIKE LOWER(?)
                ORDER BY maKhuyenMai DESC
                """;

        String searchPattern = "%" + tuKhoa.trim() + "%";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ketQua.add(chuyenDoiBanGhiSangDoiTuong(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi tìm kiếm khuyến mãi: " + e.getMessage(), e);
        }
        return ketQua;
    }

    /**
     * Tìm thông tin một khuyến mãi theo mã định danh.
     *
     * @param maKhuyenMai Mã khuyến mãi cần tìm
     * @return Đối tượng Promotion nếu tìm thấy, null nếu không tồn tại
     */
    public Promotion timTheoMaKhuyenMai(String maKhuyenMai) {
        String sql = """
                SELECT maKhuyenMai, tenChuongTrinh, phanTramGiam, ngayBatDau, ngayKetThuc, trangThai, ngayCapNhat
                FROM KhuyenMai
                WHERE maKhuyenMai = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maKhuyenMai);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return chuyenDoiBanGhiSangDoiTuong(rs);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi tìm khuyến mãi theo mã: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Thêm mới một chương trình khuyến mãi vào cơ sở dữ liệu.
     *
     * @param km Đối tượng Promotion chứa thông tin cần lưu
     * @return true nếu thêm thành công
     */
    public boolean themKhuyenMai(Promotion km) {
        String sql = """
                INSERT INTO KhuyenMai (maKhuyenMai, tenChuongTrinh, phanTramGiam, ngayBatDau, ngayKetThuc, trangThai, ngayCapNhat)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, km.getMaKhuyenMai());
            ps.setString(2, km.getTenChuongTrinh());
            ps.setDouble(3, km.getPhanTramGiam());
            ps.setDate(4, Date.valueOf(km.getNgayBatDau()));
            ps.setDate(5, Date.valueOf(km.getNgayKetThuc()));
            ps.setBoolean(6, km.isTrangThai());
            ps.setDate(7, Date.valueOf(km.getNgayCapNhat() != null ? km.getNgayCapNhat() : LocalDate.now()));

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi thêm khuyến mãi: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật thông tin một chương trình khuyến mãi đã tồn tại.
     *
     * @param km Đối tượng Promotion với thông tin đã chỉnh sửa
     * @return true nếu cập nhật thành công
     */
    public boolean capNhatKhuyenMai(Promotion km) {
        String sql = """
                UPDATE KhuyenMai
                SET tenChuongTrinh = ?,
                    phanTramGiam = ?,
                    ngayBatDau = ?,
                    ngayKetThuc = ?,
                    trangThai = ?,
                    ngayCapNhat = ?
                WHERE maKhuyenMai = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, km.getTenChuongTrinh());
            ps.setDouble(2, km.getPhanTramGiam());
            ps.setDate(3, Date.valueOf(km.getNgayBatDau()));
            ps.setDate(4, Date.valueOf(km.getNgayKetThuc()));
            ps.setBoolean(5, km.isTrangThai());
            ps.setDate(6, Date.valueOf(LocalDate.now())); // Ngày cập nhật là hôm nay
            ps.setString(7, km.getMaKhuyenMai());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi cập nhật khuyến mãi: " + e.getMessage(), e);
        }
    }

    /**
     * Thực hiện xóa mềm (ngừng hoạt động) chương trình khuyến mãi theo đúng tài liệu OOAD.
     * Không xóa hẳn dữ liệu khỏi CSDL để bảo đảm toàn vẹn tham chiếu với bảng HoaDon.
     *
     * @param maKhuyenMai Mã khuyến mãi cần ngừng hoạt động
     * @return true nếu ngừng hoạt động thành công
     */
    public boolean xoaMemKhuyenMai(String maKhuyenMai) {
        String sql = """
                UPDATE KhuyenMai
                SET trangThai = 0,
                    ngayCapNhat = ?
                WHERE maKhuyenMai = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(LocalDate.now()));
            ps.setString(2, maKhuyenMai);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi xóa mềm khuyến mãi: " + e.getMessage(), e);
        }
    }

    /**
     * Tự động sinh mã khuyến mãi tiếp theo theo mẫu chuẩn hiển thị: KM-YYYY-NNN (ví dụ: KM-2026-082).
     *
     * @return Chuỗi mã khuyến mãi mới
     */
    public String sinhMaKhuyenMaiTuDong() {
        int currentYear = LocalDate.now().getYear();
        String prefix = "KM-" + currentYear + "-";
        int maxSeq = 81; // Mặc định nếu chưa có

        String sql = "SELECT maKhuyenMai FROM KhuyenMai WHERE maKhuyenMai LIKE ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ma = rs.getString("maKhuyenMai");
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
            System.err.println("[PromotionDao] Lỗi sinh mã: " + e.getMessage());
        }

        return String.format("%s%03d", prefix, maxSeq + 1);
    }

    /**
     * Chuyển đổi một bản ghi ResultSet sang đối tượng Promotion.
     *
     * @param rs ResultSet hiện hành
     * @return Đối tượng Promotion
     * @throws SQLException nếu có lỗi đọc dữ liệu
     */
    private Promotion chuyenDoiBanGhiSangDoiTuong(ResultSet rs) throws SQLException {
        String maKhuyenMai = rs.getString("maKhuyenMai");
        String tenChuongTrinh = rs.getString("tenChuongTrinh");
        double phanTramGiam = rs.getDouble("phanTramGiam");

        Date sqlNgayBatDau = rs.getDate("ngayBatDau");
        LocalDate ngayBatDau = (sqlNgayBatDau != null) ? sqlNgayBatDau.toLocalDate() : null;

        Date sqlNgayKetThuc = rs.getDate("ngayKetThuc");
        LocalDate ngayKetThuc = (sqlNgayKetThuc != null) ? sqlNgayKetThuc.toLocalDate() : null;

        boolean trangThai = rs.getBoolean("trangThai");

        Date sqlNgayCapNhat = rs.getDate("ngayCapNhat");
        LocalDate ngayCapNhat = (sqlNgayCapNhat != null) ? sqlNgayCapNhat.toLocalDate() : LocalDate.now();

        return new Promotion(maKhuyenMai, tenChuongTrinh, phanTramGiam, ngayBatDau, ngayKetThuc, trangThai, ngayCapNhat);
    }
}

