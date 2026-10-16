package com.petshop.dao;

import com.petshop.model.Supplier;
import com.petshop.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp DAO xử lý truy vấn cơ sở dữ liệu cho bảng NhaCungCap.
 * Đảm bảo tuân thủ kiến trúc n-Tier và hỗ trợ xóa mềm (ngừng hoạt động).
 */
public class SupplierDao {

    private static boolean schemaVerified = false;

    /**
     * Tự động kiểm tra và bổ sung cột email, trangThai (xóa mềm) vào bảng NhaCungCap
     * nếu cơ sở dữ liệu hiện tại chưa có hai cột này.
     */
    public synchronized void ensureSupplierSchema() {
        if (schemaVerified) {
            return;
        }
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("""
                IF COL_LENGTH('NhaCungCap', 'email') IS NULL
                BEGIN
                    ALTER TABLE NhaCungCap ADD email VARCHAR(100) NULL;
                END
                IF COL_LENGTH('NhaCungCap', 'trangThai') IS NULL
                BEGIN
                    ALTER TABLE NhaCungCap ADD trangThai BIT NOT NULL CONSTRAINT DF_NhaCungCap_TrangThai DEFAULT 1;
                END
            """);

            // Cập nhật email mẫu cho dữ liệu khởi tạo nếu đang NULL
            stmt.executeUpdate("""
                UPDATE NhaCungCap SET email = 'contact@royalcanin.vn'
                WHERE maNhaCungCap = 'NCC-20261005-000001' AND email IS NULL;
                UPDATE NhaCungCap SET email = 'sales@petcare.vn'
                WHERE maNhaCungCap = 'NCC-20261005-000002' AND email IS NULL;
            """);

            schemaVerified = true;
        } catch (SQLException e) {
            System.err.println("[SupplierDao] Lưu ý kiểm tra schema NhaCungCap: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách nhà cung cấp từ cơ sở dữ liệu.
     *
     * @param includeInactive true nếu lấy cả nhà cung cấp đã ngừng hoạt động, false nếu chỉ lấy đang hoạt động
     * @return Danh sách Supplier
     */
    public List<Supplier> findAll(boolean includeInactive) {
        ensureSupplierSchema();
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT maNhaCungCap, tenNhaCungCap, soDienThoai, email, diaChi, trangThai " +
                     "FROM NhaCungCap " +
                     (includeInactive ? "" : "WHERE trangThai = 1 ") +
                     "ORDER BY maNhaCungCap DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                suppliers.add(mapRowToSupplier(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh sách nhà cung cấp: " + e.getMessage(), e);
        }
        return suppliers;
    }

    /**
     * Tìm nhà cung cấp theo mã định danh.
     *
     * @param maNhaCungCap Mã nhà cung cấp
     * @return Đối tượng Supplier hoặc null nếu không tìm thấy
     */
    public Supplier findById(String maNhaCungCap) {
        ensureSupplierSchema();
        String sql = "SELECT maNhaCungCap, tenNhaCungCap, soDienThoai, email, diaChi, trangThai " +
                     "FROM NhaCungCap WHERE maNhaCungCap = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNhaCungCap);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToSupplier(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi tra cứu nhà cung cấp: " + e.getMessage(), e);
        }
    }

    /**
     * Tìm kiếm nhà cung cấp theo từ khóa (mã, tên, SĐT, email, địa chỉ) và lọc trạng thái.
     *
     * @param keyword      Từ khóa tìm kiếm (có thể rỗng)
     * @param activeFilter null: tất cả, true: Đang hợp tác, false: Ngừng hoạt động
     * @return Danh sách nhà cung cấp thỏa điều kiện
     */
    public List<Supplier> searchSuppliers(String keyword, Boolean activeFilter) {
        ensureSupplierSchema();
        List<Supplier> results = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT maNhaCungCap, tenNhaCungCap, soDienThoai, email, diaChi, trangThai " +
                "FROM NhaCungCap WHERE 1=1 ");

        boolean hasKeyword = keyword != null && !keyword.isBlank();
        if (hasKeyword) {
            sql.append("AND (maNhaCungCap LIKE ? OR tenNhaCungCap LIKE ? OR soDienThoai LIKE ? " +
                       "OR COALESCE(email, '') LIKE ? OR COALESCE(diaChi, '') LIKE ?) ");
        }
        if (activeFilter != null) {
            sql.append("AND trangThai = ? ");
        }
        sql.append("ORDER BY maNhaCungCap DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIdx = 1;
            if (hasKeyword) {
                String pattern = "%" + keyword.trim() + "%";
                ps.setString(paramIdx++, pattern);
                ps.setString(paramIdx++, pattern);
                ps.setString(paramIdx++, pattern);
                ps.setString(paramIdx++, pattern);
                ps.setString(paramIdx++, pattern);
            }
            if (activeFilter != null) {
                ps.setBoolean(paramIdx, activeFilter);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRowToSupplier(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi tìm kiếm nhà cung cấp: " + e.getMessage(), e);
        }
        return results;
    }

    /**
     * Thêm mới một nhà cung cấp vào cơ sở dữ liệu.
     *
     * @param supplier Thông tin nhà cung cấp cần thêm
     * @return true nếu thêm thành công
     */
    public boolean insertSupplier(Supplier supplier) {
        ensureSupplierSchema();
        String sql = "INSERT INTO NhaCungCap (maNhaCungCap, tenNhaCungCap, soDienThoai, email, diaChi, trangThai) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, supplier.getMaNhaCungCap());
            ps.setString(2, supplier.getTenNhaCungCap());
            ps.setString(3, supplier.getSoDienThoai());
            ps.setString(4, supplier.getEmail());
            ps.setString(5, supplier.getDiaChi());
            ps.setBoolean(6, supplier.isTrangThai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể thêm nhà cung cấp: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật thông tin nhà cung cấp.
     *
     * @param supplier Thông tin nhà cung cấp cần cập nhật
     * @return true nếu cập nhật thành công
     */
    public boolean updateSupplier(Supplier supplier) {
        ensureSupplierSchema();
        String sql = "UPDATE NhaCungCap SET tenNhaCungCap = ?, soDienThoai = ?, email = ?, " +
                     "diaChi = ?, trangThai = ? WHERE maNhaCungCap = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, supplier.getTenNhaCungCap());
            ps.setString(2, supplier.getSoDienThoai());
            ps.setString(3, supplier.getEmail());
            ps.setString(4, supplier.getDiaChi());
            ps.setBoolean(5, supplier.isTrangThai());
            ps.setString(6, supplier.getMaNhaCungCap());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể cập nhật nhà cung cấp: " + e.getMessage(), e);
        }
    }

    /**
     * Ngừng hoạt động nhà cung cấp (Xóa mềm - trangThai = 0) theo tài liệu OOAD.
     *
     * @param maNhaCungCap Mã nhà cung cấp cần ngừng hoạt động
     * @return true nếu cập nhật trạng thái thành công
     */
    public boolean deactivateSupplier(String maNhaCungCap) {
        ensureSupplierSchema();
        String sql = "UPDATE NhaCungCap SET trangThai = 0 WHERE maNhaCungCap = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNhaCungCap);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể ngừng hoạt động nhà cung cấp: " + e.getMessage(), e);
        }
    }

    /**
     * Sinh mã nhà cung cấp tiếp theo theo chuẩn NCC-YYYYMMDD-NNNNNN.
     *
     * @return Mã nhà cung cấp mới
     */
    public String generateNextSupplierId() {
        ensureSupplierSchema();
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sql = "SELECT MAX(RIGHT(maNhaCungCap, 6)) AS maxSeq FROM NhaCungCap";
        int nextSeq = 1;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getString("maxSeq") != null) {
                nextSeq = Integer.parseInt(rs.getString("maxSeq")) + 1;
            }
        } catch (Exception ignored) {
        }
        return String.format("NCC-%s-%06d", datePart, nextSeq);
    }

    /**
     * Ánh xạ một dòng ResultSet thành đối tượng Supplier.
     */
    private Supplier mapRowToSupplier(ResultSet rs) throws SQLException {
        return new Supplier(
                rs.getString("maNhaCungCap"),
                rs.getString("tenNhaCungCap"),
                rs.getString("soDienThoai"),
                rs.getString("email") != null ? rs.getString("email") : "",
                rs.getString("diaChi") != null ? rs.getString("diaChi") : "",
                rs.getBoolean("trangThai")
        );
    }
}

