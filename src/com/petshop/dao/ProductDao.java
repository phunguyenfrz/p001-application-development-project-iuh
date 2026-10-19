package com.petshop.dao;

import com.petshop.model.Product;
import com.petshop.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Lớp DAO xử lý truy vấn cơ sở dữ liệu cho bảng SanPham.
 * Hỗ trợ các chức năng: Cập nhật (CRUD), Tìm kiếm, Quản lý tồn kho, Cảnh báo hàng sắp hết,
 * tra cứu mã vạch và xóa mềm (ngừng kinh doanh).
 */
public class ProductDao {

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
     * Tự động kiểm tra bảng SanPham trong cơ sở dữ liệu.
     */
    public void ensureProductSchema() {
        if (schemaVerified) {
            return;
        }
        synchronized (DB_LOCK) {
            if (schemaVerified) {
                return;
            }
            try {
                Connection conn = getActiveConnection();
                DatabaseMetaData meta = conn.getMetaData();
                try (ResultSet rs = meta.getColumns(null, null, "SanPham", "nguongTonToiThieu")) {
                    if (!rs.next()) {
                        try (Statement stmt = conn.createStatement()) {
                            stmt.executeUpdate("ALTER TABLE SanPham ADD nguongTonToiThieu INT NOT NULL DEFAULT 10");
                        } catch (SQLException ignored) {
                        }
                    }
                }
                schemaVerified = true;
            } catch (SQLException e) {
                resetConnectionOnError();
            }
        }
    }

    /**
     * Lấy toàn bộ danh sách sản phẩm (kèm tên nhà cung cấp nếu có).
     */
    public List<Product> getAllProducts() {
        ensureProductSchema();
        String sql = """
            SELECT p.maSanPham, p.maNhaCungCap, p.tenSanPham, p.maVach, p.giaBan, 
                   p.soLuongTon, p.nguongTonToiThieu, p.donViTinh, p.trangThai,
                   n.tenNhaCungCap
            FROM SanPham p
            LEFT JOIN NhaCungCap n ON p.maNhaCungCap = n.maNhaCungCap
            ORDER BY p.maSanPham DESC
        """;
        List<Product> list = new ArrayList<>();
        synchronized (DB_LOCK) {
            try (PreparedStatement ps = getActiveConnection().prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToProduct(rs));
                }
            } catch (SQLException e) {
                resetConnectionOnError();
                throw new IllegalStateException("Không thể tải danh sách sản phẩm: " + e.getMessage(), e);
            }
        }
        return list;
    }

    /**
     * Lấy danh sách sản phẩm đang kinh doanh (trangThai = 1).
     */
    public List<Product> getActiveProducts() {
        ensureProductSchema();
        String sql = """
            SELECT p.maSanPham, p.maNhaCungCap, p.tenSanPham, p.maVach, p.giaBan, 
                   p.soLuongTon, p.nguongTonToiThieu, p.donViTinh, p.trangThai,
                   n.tenNhaCungCap
            FROM SanPham p
            LEFT JOIN NhaCungCap n ON p.maNhaCungCap = n.maNhaCungCap
            WHERE p.trangThai = 1
            ORDER BY p.tenSanPham ASC
        """;
        List<Product> list = new ArrayList<>();
        synchronized (DB_LOCK) {
            try (PreparedStatement ps = getActiveConnection().prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToProduct(rs));
                }
            } catch (SQLException e) {
                resetConnectionOnError();
                throw new IllegalStateException("Không thể tải danh sách sản phẩm đang bán: " + e.getMessage(), e);
            }
        }
        return list;
    }

    /**
     * Lấy danh sách sản phẩm sắp hết hàng (soLuongTon <= nguongTonToiThieu và trangThai = 1).
     */
    public List<Product> getLowStockProducts() {
        ensureProductSchema();
        String sql = """
            SELECT p.maSanPham, p.maNhaCungCap, p.tenSanPham, p.maVach, p.giaBan, 
                   p.soLuongTon, p.nguongTonToiThieu, p.donViTinh, p.trangThai,
                   n.tenNhaCungCap
            FROM SanPham p
            LEFT JOIN NhaCungCap n ON p.maNhaCungCap = n.maNhaCungCap
            WHERE p.trangThai = 1 AND p.soLuongTon <= p.nguongTonToiThieu
            ORDER BY p.soLuongTon ASC, p.tenSanPham ASC
        """;
        List<Product> list = new ArrayList<>();
        synchronized (DB_LOCK) {
            try (PreparedStatement ps = getActiveConnection().prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToProduct(rs));
                }
            } catch (SQLException e) {
                resetConnectionOnError();
                throw new IllegalStateException("Không thể tải danh sách sản phẩm sắp hết: " + e.getMessage(), e);
            }
        }
        return list;
    }

    /**
     * Tìm kiếm sản phẩm theo từ khóa (Mã sản phẩm, Tên sản phẩm, Mã vạch hoặc Tên nhà cung cấp).
     */
    public List<Product> searchProducts(String keyword, Boolean onlyActive) {
        ensureProductSchema();
        StringBuilder sql = new StringBuilder("""
            SELECT p.maSanPham, p.maNhaCungCap, p.tenSanPham, p.maVach, p.giaBan, 
                   p.soLuongTon, p.nguongTonToiThieu, p.donViTinh, p.trangThai,
                   n.tenNhaCungCap
            FROM SanPham p
            LEFT JOIN NhaCungCap n ON p.maNhaCungCap = n.maNhaCungCap
            WHERE 1=1
        """);

        if (onlyActive != null) {
            sql.append(" AND p.trangThai = ").append(onlyActive ? "1" : "0");
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("""
                AND (p.maSanPham LIKE ? 
                  OR p.tenSanPham LIKE ? 
                  OR p.maVach LIKE ? 
                  OR p.donViTinh LIKE ?
                  OR n.tenNhaCungCap LIKE ?)
            """);
        }
        sql.append(" ORDER BY p.tenSanPham ASC");

        List<Product> list = new ArrayList<>();
        synchronized (DB_LOCK) {
            try (PreparedStatement ps = getActiveConnection().prepareStatement(sql.toString())) {
                if (keyword != null && !keyword.trim().isEmpty()) {
                    String pattern = "%" + keyword.trim() + "%";
                    ps.setString(1, pattern);
                    ps.setString(2, pattern);
                    ps.setString(3, pattern);
                    ps.setString(4, pattern);
                    ps.setString(5, pattern);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapRowToProduct(rs));
                    }
                }
            } catch (SQLException e) {
                resetConnectionOnError();
                throw new IllegalStateException("Không thể tìm kiếm sản phẩm: " + e.getMessage(), e);
            }
        }
        return list;
    }

    /**
     * Tìm sản phẩm theo mã sản phẩm (SP-YYYYMMDD-NNNNNN).
     */
    public Optional<Product> getProductById(String maSanPham) {
        ensureProductSchema();
        String sql = """
            SELECT p.maSanPham, p.maNhaCungCap, p.tenSanPham, p.maVach, p.giaBan, 
                   p.soLuongTon, p.nguongTonToiThieu, p.donViTinh, p.trangThai,
                   n.tenNhaCungCap
            FROM SanPham p
            LEFT JOIN NhaCungCap n ON p.maNhaCungCap = n.maNhaCungCap
            WHERE p.maSanPham = ?
        """;
        synchronized (DB_LOCK) {
            try (PreparedStatement ps = getActiveConnection().prepareStatement(sql)) {
                ps.setString(1, maSanPham);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapRowToProduct(rs));
                    }
                }
            } catch (SQLException e) {
                resetConnectionOnError();
                throw new IllegalStateException("Lỗi khi tra cứu mã sản phẩm " + maSanPham + ": " + e.getMessage(), e);
            }
        }
        return Optional.empty();
    }

    /**
     * Tìm sản phẩm chính xác theo Mã vạch (Barcode).
     * Dành cho thiết bị quét mã vạch và màn hình Lập hóa đơn.
     */
    public Optional<Product> getProductByBarcode(String maVach) {
        ensureProductSchema();
        if (maVach == null || maVach.trim().isEmpty()) {
            return Optional.empty();
        }
        String sql = """
            SELECT p.maSanPham, p.maNhaCungCap, p.tenSanPham, p.maVach, p.giaBan, 
                   p.soLuongTon, p.nguongTonToiThieu, p.donViTinh, p.trangThai,
                   n.tenNhaCungCap
            FROM SanPham p
            LEFT JOIN NhaCungCap n ON p.maNhaCungCap = n.maNhaCungCap
            WHERE p.maVach = ? AND p.trangThai = 1
        """;
        synchronized (DB_LOCK) {
            try (PreparedStatement ps = getActiveConnection().prepareStatement(sql)) {
                ps.setString(1, maVach.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapRowToProduct(rs));
                    }
                }
            } catch (SQLException e) {
                resetConnectionOnError();
                throw new IllegalStateException("Lỗi khi quét mã vạch " + maVach + ": " + e.getMessage(), e);
            }
        }
        return Optional.empty();
    }

    /**
     * Kiểm tra xem mã vạch đã tồn tại trên sản phẩm khác hay chưa.
     */
    public boolean isBarcodeExists(String maVach, String excludeMaSanPham) {
        ensureProductSchema();
        String sql = "SELECT COUNT(*) FROM SanPham WHERE maVach = ? AND (? IS NULL OR maSanPham <> ?)";
        synchronized (DB_LOCK) {
            try (PreparedStatement ps = getActiveConnection().prepareStatement(sql)) {
                ps.setString(1, maVach.trim());
                ps.setString(2, excludeMaSanPham);
                ps.setString(3, excludeMaSanPham);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            } catch (SQLException e) {
                resetConnectionOnError();
            }
        }
        return false;
    }

    /**
     * Thêm mới một sản phẩm vào cơ sở dữ liệu.
     */
    public boolean addProduct(Product product) {
        ensureProductSchema();
        String sql = """
            INSERT INTO SanPham (maSanPham, maNhaCungCap, tenSanPham, maVach, giaBan, 
                                 soLuongTon, nguongTonToiThieu, donViTinh, trangThai)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        synchronized (DB_LOCK) {
            try (PreparedStatement ps = getActiveConnection().prepareStatement(sql)) {
                ps.setString(1, product.getMaSanPham());
                ps.setString(2, product.getMaNhaCungCap());
                ps.setString(3, product.getTenSanPham());
                ps.setString(4, product.getMaVach());
                ps.setDouble(5, product.getGiaBan());
                ps.setInt(6, product.getSoLuongTon());
                ps.setInt(7, product.getNguongTonToiThieu());
                ps.setString(8, product.getDonViTinh());
                ps.setBoolean(9, product.isTrangThai());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                resetConnectionOnError();
                throw new IllegalStateException("Không thể thêm sản phẩm: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Cập nhật thông tin sản phẩm.
     */
    public boolean updateProduct(Product product) {
        ensureProductSchema();
        String sql = """
            UPDATE SanPham 
            SET maNhaCungCap = ?, tenSanPham = ?, maVach = ?, giaBan = ?, 
                soLuongTon = ?, nguongTonToiThieu = ?, donViTinh = ?, trangThai = ?
            WHERE maSanPham = ?
        """;
        synchronized (DB_LOCK) {
            try (PreparedStatement ps = getActiveConnection().prepareStatement(sql)) {
                ps.setString(1, product.getMaNhaCungCap());
                ps.setString(2, product.getTenSanPham());
                ps.setString(3, product.getMaVach());
                ps.setDouble(4, product.getGiaBan());
                ps.setInt(5, product.getSoLuongTon());
                ps.setInt(6, product.getNguongTonToiThieu());
                ps.setString(7, product.getDonViTinh());
                ps.setBoolean(8, product.isTrangThai());
                ps.setString(9, product.getMaSanPham());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                resetConnectionOnError();
                throw new IllegalStateException("Không thể cập nhật sản phẩm: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Cập nhật số lượng tồn kho (PRD_INVENTORY).
     */
    public boolean updateStock(String maSanPham, int soLuongTon) {
        ensureProductSchema();
        String sql = "UPDATE SanPham SET soLuongTon = ? WHERE maSanPham = ?";
        synchronized (DB_LOCK) {
            try (PreparedStatement ps = getActiveConnection().prepareStatement(sql)) {
                ps.setInt(1, soLuongTon);
                ps.setString(2, maSanPham);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                resetConnectionOnError();
                throw new IllegalStateException("Không thể cập nhật số lượng tồn: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Cập nhật ngưỡng tồn tối thiểu an toàn (PRD_INVENTORY & PRD_LOW_STOCK).
     */
    public boolean updateThreshold(String maSanPham, int nguongTonToiThieu) {
        ensureProductSchema();
        String sql = "UPDATE SanPham SET nguongTonToiThieu = ? WHERE maSanPham = ?";
        synchronized (DB_LOCK) {
            try (PreparedStatement ps = getActiveConnection().prepareStatement(sql)) {
                ps.setInt(1, nguongTonToiThieu);
                ps.setString(2, maSanPham);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                resetConnectionOnError();
                throw new IllegalStateException("Không thể cập nhật ngưỡng tồn tối thiểu: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Ngừng kinh doanh sản phẩm (Xóa mềm - trangThai = 0).
     */
    public boolean deactivateProduct(String maSanPham) {
        ensureProductSchema();
        String sql = "UPDATE SanPham SET trangThai = 0 WHERE maSanPham = ?";
        synchronized (DB_LOCK) {
            try (PreparedStatement ps = getActiveConnection().prepareStatement(sql)) {
                ps.setString(1, maSanPham);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                resetConnectionOnError();
                throw new IllegalStateException("Không thể ngừng kinh doanh sản phẩm: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Kích hoạt lại sản phẩm (trangThai = 1).
     */
    public boolean activateProduct(String maSanPham) {
        ensureProductSchema();
        String sql = "UPDATE SanPham SET trangThai = 1 WHERE maSanPham = ?";
        synchronized (DB_LOCK) {
            try (PreparedStatement ps = getActiveConnection().prepareStatement(sql)) {
                ps.setString(1, maSanPham);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                resetConnectionOnError();
                throw new IllegalStateException("Không thể kích hoạt lại sản phẩm: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Sinh mã sản phẩm tiếp theo theo chuẩn SP-YYYYMMDD-NNNNNN.
     */
    public String generateNextProductId() {
        ensureProductSchema();
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sql = "SELECT MAX(RIGHT(maSanPham, 6)) AS maxSeq FROM SanPham";
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
        return String.format("SP-%s-%06d", datePart, nextSeq);
    }

    private Product mapRowToProduct(ResultSet rs) throws SQLException {
        Product p = new Product(
                rs.getString("maSanPham"),
                rs.getString("maNhaCungCap"),
                rs.getString("tenSanPham"),
                rs.getString("maVach"),
                rs.getDouble("giaBan"),
                rs.getInt("soLuongTon"),
                rs.getInt("nguongTonToiThieu"),
                rs.getString("donViTinh"),
                rs.getBoolean("trangThai")
        );
        try {
            p.setTenNhaCungCap(rs.getString("tenNhaCungCap"));
        } catch (SQLException ignored) {
        }
        return p;
    }
}

