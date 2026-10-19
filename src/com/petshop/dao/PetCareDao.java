package com.petshop.dao;

import com.petshop.model.PetCareProfile;
import com.petshop.model.PetCareServiceItem;
import com.petshop.model.PetCareVisit;
import com.petshop.util.DatabaseManager;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Chỉ thực hiện SQL cho lịch sử và thông tin chăm sóc. */
public class PetCareDao {
    /**
     * Đọc lịch hẹn, không loại hồ sơ thú cưng hoặc nhân viên ngừng hoạt động.
     * @param pet_id Mã thú cưng.
     * @param from_date Ngày bắt đầu, null nếu không giới hạn.
     * @param to_date Ngày kết thúc, null nếu không giới hạn.
     * @param status Trạng thái; null là tất cả.
     * @return Lịch hẹn mới nhất trước.
     * @throws SQLException Khi đọc database thất bại.
     */
    public List<PetCareVisit> findVisits(String pet_id, LocalDate from_date,
            LocalDate to_date, String status) throws SQLException {
        StringBuilder sql_query = new StringBuilder("""
                SELECT lh.maLichHen, lh.maThuCung, tc.tenThuCung,
                       kh.hoTen AS tenChuNuoi, lh.ngayHen, lh.gioHen,
                       nv.hoTen AS tenNhanVien, lh.trangThai, lh.ghiChu
                FROM dbo.LichHen lh
                JOIN dbo.ThuCung tc ON tc.maThuCung = lh.maThuCung
                JOIN dbo.KhachHang kh ON kh.maKhachHang = lh.maKhachHang
                JOIN dbo.NhanVien nv ON nv.maNhanVien = lh.maNhanVien
                WHERE lh.maThuCung = ?
                """);
        if (from_date != null) sql_query.append(" AND lh.ngayHen >= ?");
        if (to_date != null) sql_query.append(" AND lh.ngayHen <= ?");
        if (status != null) sql_query.append(" AND lh.trangThai = ?");
        sql_query.append(" ORDER BY lh.ngayHen DESC, lh.gioHen DESC, lh.maLichHen DESC");

        List<PetCareVisit> visits = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql_query.toString())) {
            int index = 1;
            statement.setString(index++, pet_id);
            if (from_date != null) statement.setDate(index++, Date.valueOf(from_date));
            if (to_date != null) statement.setDate(index++, Date.valueOf(to_date));
            if (status != null) statement.setNString(index, status);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    visits.add(new PetCareVisit(
                            result.getString("maLichHen"), result.getString("maThuCung"),
                            result.getString("tenThuCung"), result.getString("tenChuNuoi"),
                            result.getDate("ngayHen").toLocalDate(),
                            result.getTime("gioHen").toLocalTime(),
                            result.getString("tenNhanVien"), result.getString("trangThai"),
                            result.getString("ghiChu")));
                }
            }
        }
        return visits;
    }

    /**
     * Đọc giá và cân nặng đã lưu; không lấy bảng giá hoặc cân nặng hiện tại.
     * @param visit_id Mã lịch hẹn.
     * @param pet_id Mã thú cưng để kiểm tra lịch hẹn tương ứng.
     * @return Các dịch vụ của lịch hẹn.
     * @throws SQLException Khi đọc database thất bại.
     */
    public List<PetCareServiceItem> findServices(String visit_id, String pet_id) throws SQLException {
        String sql_query = """
                SELECT dv.tenDichVu, ct.canNangThuCung, ct.donGia
                FROM dbo.ChiTietLichHen ct
                JOIN dbo.DichVuChamSoc dv ON dv.maDichVu = ct.maDichVu
                JOIN dbo.LichHen lh ON lh.maLichHen = ct.maLichHen
                WHERE ct.maLichHen = ? AND lh.maThuCung = ?
                ORDER BY dv.tenDichVu, dv.maDichVu
                """;
        List<PetCareServiceItem> services = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql_query)) {
            statement.setString(1, visit_id);
            statement.setString(2, pet_id);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    services.add(new PetCareServiceItem(result.getString("tenDichVu"),
                            result.getBigDecimal("canNangThuCung"), result.getBigDecimal("donGia")));
                }
            }
        }
        return services;
    }

    /**
     * @param pet_id Mã thú cưng.
     * @return Thông tin chăm sóc hiện tại, null nếu không còn hồ sơ.
     * @throws SQLException Khi đọc database thất bại.
     */
    public PetCareProfile findProfile(String pet_id) throws SQLException {
        String sql_query = """
                SELECT canNang, diUng, trangThai
                FROM dbo.ThuCung WHERE maThuCung = ?
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql_query)) {
            statement.setString(1, pet_id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? new PetCareProfile(result.getBigDecimal("canNang"),
                        result.getString("diUng"),
                        result.getBoolean("trangThai")) : null;
            }
        }
    }

    /**
     * Chỉ cập nhật hồ sơ hiện tại; giữ nguyên ChiTietLichHen.
     * @param pet_id Mã thú cưng.
     * @param weight Cân nặng đã kiểm tra.
     * @param allergy Dị ứng hoặc null.
     * @throws SQLException Khi cập nhật thất bại hoặc hồ sơ đã ngừng theo dõi.
     */
    public void updateProfile(String pet_id, BigDecimal weight,
            String allergy) throws SQLException {
        String sql_query = """
                UPDATE dbo.ThuCung SET canNang = ?, diUng = ?
                WHERE maThuCung = ? AND trangThai = 1
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql_query)) {
            statement.setBigDecimal(1, weight);
            statement.setNString(2, allergy);
            statement.setString(3, pet_id);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Hồ sơ không còn tồn tại hoặc đã ngừng theo dõi. Hãy tải lại.");
            }
        }
    }
}
