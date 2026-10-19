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
 * Đảm bảo tuân thủ kiến trúc n-Tier, tái sử dụng kết nối và hỗ trợ xóa mềm (ngừng hoạt động).
 */
public class SupplierDao {

    private static final Object DB_LOCK = new Object();
    private static Connection sharedConnection;
    private static boolean schemaVerified = false;

    /**
     * Lấy kết nối CSDL đang mở (hoặc tạo mới nếu chưa có / đã đóng).
     */
    private Connection getActiveConnection() throws SQLException {
        synchronized (DB_LOCK) {
            if (sharedConnection == null || sharedConnection.isClosed()) {
                sharedConnection = DatabaseManager.getConnection();
            }
            return sharedConnection;
        }
    }

    /**
     * Đóng kết nối hiện tại nếu phát hiện lỗi đường truyền để lần gọi sau tự kết nối lại.
     */
    private void resetConnectionOnError() {
        synchronized (DB_LOCK) {
            if (sharedConnection != null) {
                try {
                    sharedConnection.close();
                } catch (SQLException ignored) {
                }
                sharedConnection = null;
            }
        }
    }

    /**
     * Tự động kiểm tra và bổ sung cột email, matHangCungCap, trangThai (xóa mềm)
     * vào bảng NhaCungCap nếu cơ sở dữ liệu hiện tại chưa có.
     */
    public void ensureSupplierSchema() {
        if (schemaVerified) {
            return;
        }
        synchronized (DB_LOCK) {
            if (schemaVerified) {
                return;
            }
            try (Statement stmt = getActiveConnection().createStatement()) {
                stmt.executeUpdate("""
                    IF COL_LENGTH('NhaCungCap', 'email') IS NULL
                        ALTER TABLE NhaCungCap ADD email VARCHAR(100) NULL;
                    IF COL_LENGTH('NhaCungCap', 'matHangCungCap') IS NULL
                        ALTER TABLE NhaCungCap ADD matHangCungCap NVARCHAR(200) NULL;
                    IF COL_LENGTH('NhaCungCap', 'trangThai') IS NULL
                        ALTER TABLE NhaCungCap ADD trangThai BIT NOT NULL CONSTRAINT DF_NhaCungCap_TrangThai DEFAULT 1;
                """);
                stmt.executeUpdate("""
                    UPDATE NhaCungCap
                    SET email = COALESCE(email, 'contact@royalcanin.vn'),
                        matHangCungCap = COALESCE(matHangCungCap, N'Thức ăn hạt, Pate thú cưng')
                    WHERE maNhaCungCap = 'NCC-20261005-000001';

                    UPDATE NhaCungCap
                    SET email = COALESCE(email, 'sales@petcare.vn'),
                        matHangCungCap = COALESCE(matHangCungCap, N'Phụ kiện, Đồ chơi thú cưng')
                    WHERE maNhaCungCap = 'NCC-20261005-000002';

                    IF NOT EXISTS (SELECT 1 FROM NhaCungCap WHERE maNhaCungCap = 'NCC-20261005-000003')
                        INSERT INTO NhaCungCap (maNhaCungCap, tenNhaCungCap, soDienThoai, email, diaChi, matHangCungCap, trangThai)
                        VALUES ('NCC-20261005-000003', N'Pet Distribution Việt Nam', '0283822889', 'info@petdistribution.vn', N'125 Nguyễn Văn Cừ, Quận 5, TP. HCM', N'Thức ăn dinh dưỡng, Sữa thú cưng', 1);

                    IF NOT EXISTS (SELECT 1 FROM NhaCungCap WHERE maNhaCungCap = 'NCC-20261005-000004')
                        INSERT INTO NhaCungCap (maNhaCungCap, tenNhaCungCap, soDienThoai, email, diaChi, matHangCungCap, trangThai)
                        VALUES ('NCC-20261005-000004', N'Bio-Groom Việt Nam', '0283930112', 'contact@biogroom.vn', N'48 Võ Văn Tần, Quận 3, TP. HCM', N'Sữa tắm, Mỹ phẩm chăm sóc lông', 1);

                    IF NOT EXISTS (SELECT 1 FROM NhaCungCap WHERE maNhaCungCap = 'NCC-20261005-000005')
                        INSERT INTO NhaCungCap (maNhaCungCap, tenNhaCungCap, soDienThoai, email, diaChi, matHangCungCap, trangThai)
                        VALUES ('NCC-20261005-000005', N'Pet City', '0283512334', 'support@petcity.vn', N'89 Điện Biên Phủ, Bình Thạnh, TP. HCM', N'Chuồng nệm, Phụ kiện tổng hợp', 1);
                """);
            } catch (SQLException e) {
                System.err.println("[SupplierDao] Lưu ý kiểm tra schema NhaCungCap: " + e.getMessage());
            } finally {
                schemaVerified = true;
            }
        }
    }

    /**
     * Tìm nhà cung cấp theo mã định danh.
     */
    public Supplier findById(String maNhaCungCap) {
        ensureSupplierSchema();
        String sql = "SELECT maNhaCungCap, tenNhaCungCap, soDienThoai, email, diaChi, matHangCungCap, trangThai " +
                     "FROM NhaCungCap WHERE maNhaCungCap = ?";
        synchronized (DB_LOCK) {
            try (PreparedStatement ps = getActiveConnection().prepareStatement(sql)) {
                ps.setString(1, maNhaCungCap);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? mapRowToSupplier(rs) : null;
                }
            } catch (SQLException e) {
                resetConnectionOnError();
                throw new IllegalStateException("Lỗi khi tra cứu nhà cung cấp: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Kiểm tra số điện thoại đã tồn tại trong bảng NhaCungCap hay chưa.
     */
    public boolean existsByPhone(String soDienThoai, String excludeId) {
        return existsByCondition("soDienThoai = ?", soDienThoai, excludeId);
    }

    /**
     * Kiểm tra địa chỉ email đã tồn tại trong bảng NhaCungCap hay chưa (không phân biệt hoa thường).
     */
    public boolean existsByEmail(String email, String excludeId) {
        return existsByCondition("LOWER(LTRIM(RTRIM(COALESCE(email, '')))) = LOWER(?)", email.trim(), excludeId);
    }

    private boolean existsByCondition(String fieldCondition, String value, String excludeId) {
        ensureSupplierSchema();
        String sql = excludeId == null
                ? "SELECT COUNT(1) FROM NhaCungCap WHERE " + fieldCondition
                : "SELECT COUNT(1) FROM NhaCungCap WHERE " + fieldCondition + " AND maNhaCungCap <> ?";
        synchronized (DB_LOCK) {
            try (PreparedStatement ps = getActiveConnection().prepareStatement(sql)) {
                ps.setString(1, value);
                if (excludeId != null) {
                    ps.setString(2, excludeId);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() && rs.getInt(1) > 0;
                }
            } catch (SQLException e) {
                resetConnectionOnError();
                throw new IllegalStateException("Lỗi khi kiểm tra trùng dữ liệu nhà cung cấp: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Lấy danh sách hoặc tìm kiếm nhà cung cấp theo từ khóa và lọc trạng thái.
     *
     * @param keyword      Từ khóa tìm kiếm (rỗng nếu lấy tất cả)
     * @param activeFilter null: tất cả, true: Đang hợp tác, false: Ngừng hoạt động
     */
    public List<Supplier> searchSuppliers(String keyword, Boolean activeFilter) {
        ensureSupplierSchema();
        List<Supplier> results = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT maNhaCungCap, tenNhaCungCap, soDienThoai, email, diaChi, matHangCungCap, trangThai " +
                "FROM NhaCungCap WHERE 1=1 ");

        boolean hasKeyword = keyword != null && !keyword.isBlank();
        if (hasKeyword) {
            sql.append("AND (maNhaCungCap LIKE ? OR ('NCC-' + RIGHT(maNhaCungCap, 3)) LIKE ? " +
                       "OR tenNhaCungCap LIKE ? OR soDienThoai LIKE ? " +
                       "OR COALESCE(email, '') LIKE ? OR COALESCE(diaChi, '') LIKE ? " +
                       "OR COALESCE(matHangCungCap, '') LIKE ?) ");
        }
        if (activeFilter != null) {
            sql.append("AND trangThai = ? ");
        }
        sql.append("ORDER BY maNhaCungCap ASC");

        synchronized (DB_LOCK) {
            try (PreparedStatement ps = getActiveConnection().prepareStatement(sql.toString())) {
                int paramIdx = 1;
                if (hasKeyword) {
                    String trimmed = keyword.trim();
                    String pattern = "%" + trimmed + "%";
                    String phonePattern = "%" + trimmed.replaceAll("\\s+", "") + "%";
                    ps.setString(paramIdx++, pattern);
                    ps.setString(paramIdx++, pattern);
                    ps.setString(paramIdx++, pattern);
                    ps.setString(paramIdx++, phonePattern);
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
                resetConnectionOnError();
                throw new IllegalStateException("Lỗi khi tìm kiếm nhà cung cấp: " + e.getMessage(), e);
            }
        }
        return results;
    }

    /**
     * Thêm mới một nhà cung cấp vào cơ sở dữ liệu.
     */
    public boolean insertSupplier(Supplier supplier) {
        ensureSupplierSchema();
        String sql = "INSERT INTO NhaCungCap (maNhaCungCap, tenNhaCungCap, soDienThoai, email, diaChi, matHangCungCap, trangThai) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        synchronized (DB_LOCK) {
            try (PreparedStatement ps = getActiveConnection().prepareStatement(sql)) {
                ps.setString(1, supplier.getMaNhaCungCap());
                ps.setString(2, supplier.getTenNhaCungCap());
                ps.setString(3, supplier.getSoDienThoai());
                ps.setString(4, supplier.getEmail());
                ps.setString(5, supplier.getDiaChi());
                ps.setString(6, supplier.getMatHangCungCap());
                ps.setBoolean(7, supplier.isTrangThai());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                resetConnectionOnError();
                throw new IllegalStateException("Không thể thêm nhà cung cấp: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Cập nhật thông tin nhà cung cấp.
     */
    public boolean updateSupplier(Supplier supplier) {
        ensureSupplierSchema();
        String sql = "UPDATE NhaCungCap SET tenNhaCungCap = ?, soDienThoai = ?, email = ?, " +
                     "diaChi = ?, matHangCungCap = ?, trangThai = ? WHERE maNhaCungCap = ?";
        synchronized (DB_LOCK) {
            try (PreparedStatement ps = getActiveConnection().prepareStatement(sql)) {
                ps.setString(1, supplier.getTenNhaCungCap());
                ps.setString(2, supplier.getSoDienThoai());
                ps.setString(3, supplier.getEmail());
                ps.setString(4, supplier.getDiaChi());
                ps.setString(5, supplier.getMatHangCungCap());
                ps.setBoolean(6, supplier.isTrangThai());
                ps.setString(7, supplier.getMaNhaCungCap());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                resetConnectionOnError();
                throw new IllegalStateException("Không thể cập nhật nhà cung cấp: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Ngừng hoạt động nhà cung cấp (Xóa mềm - trangThai = 0) theo tài liệu OOAD.
     */
    public boolean deactivateSupplier(String maNhaCungCap) {
        ensureSupplierSchema();
        String sql = "UPDATE NhaCungCap SET trangThai = 0 WHERE maNhaCungCap = ?";
        synchronized (DB_LOCK) {
            try (PreparedStatement ps = getActiveConnection().prepareStatement(sql)) {
                ps.setString(1, maNhaCungCap);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                resetConnectionOnError();
                throw new IllegalStateException("Không thể ngừng hoạt động nhà cung cấp: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Sinh mã nhà cung cấp tiếp theo theo chuẩn NCC-YYYYMMDD-NNNNNN.
     */
    public String generateNextSupplierId() {
        ensureSupplierSchema();
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sql = "SELECT MAX(RIGHT(maNhaCungCap, 6)) AS maxSeq FROM NhaCungCap";
        int nextSeq = 1;
        synchronized (DB_LOCK) {
            try (PreparedStatement ps = getActiveConnection().prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getString("maxSeq") != null) {
                    nextSeq = Integer.parseInt(rs.getString("maxSeq")) + 1;
                }
            } catch (Exception ignored) {
                resetConnectionOnError();
            }
        }
        return String.format("NCC-%s-%06d", datePart, nextSeq);
    }

    private Supplier mapRowToSupplier(ResultSet rs) throws SQLException {
        return new Supplier(
                rs.getString("maNhaCungCap"),
                rs.getString("tenNhaCungCap"),
                rs.getString("soDienThoai"),
                rs.getString("email") != null ? rs.getString("email") : "",
                rs.getString("diaChi") != null ? rs.getString("diaChi") : "",
                rs.getString("matHangCungCap") != null ? rs.getString("matHangCungCap") : "",
                rs.getBoolean("trangThai")
        );
    }
}
