package com.petshop.dao;

import com.petshop.model.NhaCungCap;
import com.petshop.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp DAO truy xuất dữ liệu bảng NhaCungCap trong CSDL SQL Server.
 */
public class NhaCungCapDAO {

    /**
     * Lấy toàn bộ danh sách nhà cung cấp.
     */
    public List<NhaCungCap> layDanhSachNhaCungCap() {
        List<NhaCungCap> danhSach = new ArrayList<>();
        String sql = "SELECT maNhaCungCap, tenNhaCungCap, soDienThoai, diaChi FROM NhaCungCap ORDER BY tenNhaCungCap ASC";

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) return danhSach;

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(new NhaCungCap(
                            rs.getString("maNhaCungCap"),
                            rs.getString("tenNhaCungCap"),
                            rs.getString("soDienThoai"),
                            rs.getString("diaChi")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi lấy danh sách nhà cung cấp: " + e.getMessage(), e);
        }

        return danhSach;
    }

    /**
     * Lấy thông tin nhà cung cấp theo mã.
     */
    public NhaCungCap layTheoMa(String maNhaCungCap) {
        String sql = "SELECT maNhaCungCap, tenNhaCungCap, soDienThoai, diaChi FROM NhaCungCap WHERE maNhaCungCap = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) return null;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maNhaCungCap);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new NhaCungCap(
                                rs.getString("maNhaCungCap"),
                                rs.getString("tenNhaCungCap"),
                                rs.getString("soDienThoai"),
                                rs.getString("diaChi")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi lấy thông tin nhà cung cấp: " + e.getMessage(), e);
        }

        return null;
    }
}

