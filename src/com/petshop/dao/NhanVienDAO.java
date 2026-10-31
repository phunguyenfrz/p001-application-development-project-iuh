package com.petshop.dao;

import com.petshop.model.NhanVien;
import com.petshop.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp truy xuất dữ liệu Nhân viên từ CSDL SQL Server hoặc Mock khi chưa có phân hệ Nhân viên.
 */
public class NhanVienDAO {

    /**
     * Lấy danh sách nhân viên đang làm việc phục vụ phân ca.
     * Tương thích với Issue #64 Quản lý Nhân viên.
     */
    public List<NhanVien> layDanhSachNhanVienDangLamViec() {
        List<NhanVien> danhSach = new ArrayList<>();
        String sql = "SELECT maNhanVien, hoTen, soDienThoai, chucVu, gioiTinh, trangThai FROM NhanVien WHERE trangThai = N'Đang làm' ORDER BY hoTen ASC";

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                return taoDuLieuGiaNhanVien();
            }
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    NhanVien nv = new NhanVien(
                            rs.getString("maNhanVien"),
                            rs.getString("hoTen"),
                            rs.getString("soDienThoai"),
                            rs.getString("chucVu"),
                            rs.getBoolean("gioiTinh"),
                            rs.getString("trangThai")
                    );
                    danhSach.add(nv);
                }
            }
        } catch (Exception e) {
            // Khi bảng chưa có hoặc lỗi kết nối, fallback dữ liệu giả lập
            return taoDuLieuGiaNhanVien();
        }

        if (danhSach.isEmpty()) {
            return taoDuLieuGiaNhanVien();
        }
        return danhSach;
    }

    private List<NhanVien> taoDuLieuGiaNhanVien() {
        List<NhanVien> ds = new ArrayList<>();
        ds.add(new NhanVien("NV-20261005-000001", "Lê Thị Hoa", "0901234567", "Chăm sóc", false, "Đang làm"));
        ds.add(new NhanVien("NV-20261005-000002", "Trần Minh Khang", "0902345678", "Bán hàng", true, "Đang làm"));
        ds.add(new NhanVien("NV-20261005-000003", "Phạm Ngọc Linh", "0903456789", "Chăm sóc", false, "Đang làm"));
        ds.add(new NhanVien("NV-20261005-000000", "Quản trị hệ thống", "0909000000", "Quản lý", true, "Đang làm"));
        return ds;
    }
}

