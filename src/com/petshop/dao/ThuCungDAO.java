package com.petshop.dao;

import com.petshop.model.ThuCung;
import com.petshop.util.DatabaseManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp DAO phụ trách truy vấn và thao tác cơ sở dữ liệu cho thực thể Thú cưng (ThuCung).
 * Tuân thủ Quy ước #60: không nuốt ngoại lệ SQLException mà ném IllegalStateException.
 */
public class ThuCungDAO {

    /**
     * Lấy danh sách thú cưng thuộc sở hữu của một khách hàng dựa vào mã khách hàng.
     *
     * @param maKhachHang Mã định danh của khách hàng
     * @return Danh sách các thú cưng của khách hàng đó
     */
    public List<ThuCung> layDanhSachTheoKhachHang(String maKhachHang) {
        List<ThuCung> danhSach = new ArrayList<>();
        String sql = "SELECT maThuCung, maKhachHang, tenThuCung, loaiThuCung, giongLoai, canNang, ngaySinh, diUng, gioiTinh " +
                     "FROM dbo.ThuCung WHERE maKhachHang = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maKhachHang);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date sqlNgaySinh = rs.getDate("ngaySinh");
                    LocalDate ngaySinh = sqlNgaySinh != null ? sqlNgaySinh.toLocalDate() : null;

                    ThuCung tc = new ThuCung(
                            rs.getString("maThuCung"),
                            rs.getString("maKhachHang"),
                            rs.getString("tenThuCung"),
                            rs.getString("loaiThuCung"),
                            rs.getString("giongLoai"),
                            rs.getBigDecimal("canNang"),
                            ngaySinh,
                            rs.getString("diUng"),
                            rs.getString("gioiTinh")
                    );
                    danhSach.add(tc);
                }
            }
        } catch (SQLException e) {
            // Tuân thủ Quy ước #60 (Mục 4): ném ngoại lệ rõ ràng thay vì nuốt lỗi
            throw new IllegalStateException("Lỗi truy vấn danh sách thú cưng của khách hàng " + maKhachHang + ": " + e.getMessage(), e);
        }

        return danhSach;
    }
}
