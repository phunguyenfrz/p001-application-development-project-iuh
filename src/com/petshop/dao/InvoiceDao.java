package com.petshop.dao;

import com.petshop.model.Invoice;
import com.petshop.model.InvoiceItem;
import com.petshop.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Lớp DAO quản lý truy vấn cơ sở dữ liệu cho Hóa đơn (bảng HoaDon, ChiTietHoaDon_SanPham, ChiTietHoaDon_DichVu).
 * Tuân thủ tiêu chuẩn:
 * - Tên lớp là danh từ (InvoiceDao), tên phương thức là động từ.
 * - Sử dụng 100% PreparedStatement và xử lý transaction an toàn.
 * - Tự động cập nhật tồn kho khi bán hàng và hoàn lại khi hủy hóa đơn (xóa mềm).
 */
public class InvoiceDao {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Tự động sinh mã hóa đơn tiếp theo theo định dạng chuẩn OOAD:
     * HD-YYYYMMDD-NNNNNN (Ví dụ: HD-20261020-000001).
     */
    public synchronized String generateNextInvoiceId() {
        String todayStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = "HD-" + todayStr + "-";
        String sql = "SELECT MAX(maHoaDon) FROM HoaDon WHERE maHoaDon LIKE ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String maxId = rs.getString(1);
                    if (maxId != null && maxId.length() >= prefix.length() + 6) {
                        String seqPart = maxId.substring(prefix.length());
                        try {
                            int seq = Integer.parseInt(seqPart);
                            return prefix + String.format("%06d", seq + 1);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            System.err.println("Lỗi khi sinh mã hóa đơn tiếp theo: " + ex.getMessage());
        }

        return prefix + "000001";
    }

    /**
     * Thêm mới một hóa đơn bán hàng vào CSDL cùng toàn bộ chi tiết sản phẩm / dịch vụ.
     * Tự động trừ số lượng tồn kho sản phẩm nếu hóa đơn đã thanh toán.
     * Sử dụng Transaction để đảm bảo tính toàn vẹn dữ liệu.
     */
    public void addInvoice(Invoice invoice) throws SQLException {
        if (invoice.getMaHoaDon() == null || invoice.getMaHoaDon().trim().isEmpty()) {
            invoice.setMaHoaDon(generateNextInvoiceId());
        }

        String sqlHeader = """
            INSERT INTO HoaDon (maHoaDon, maLichHen, maNhanVien, maKhachHang, ngayLap,
                                hinhThucThanhToan, trangThaiThanhToan, maKhuyenMai, tongTien)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        String sqlProductItem = """
            INSERT INTO ChiTietHoaDon_SanPham (maHoaDon, maSanPham, soLuong, donGia)
            VALUES (?, ?, ?, ?)
        """;

        String sqlServiceItem = """
            INSERT INTO ChiTietHoaDon_DichVu (maHoaDon, maDichVu, soLuong, donGia)
            VALUES (?, ?, ?, ?)
        """;

        String sqlDeductStock = """
            UPDATE SanPham SET soLuongTon = soLuongTon - ?
            WHERE maSanPham = ? AND soLuongTon >= ?
        """;

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            // 1. Thêm bản ghi HoaDon
            try (PreparedStatement psHeader = conn.prepareStatement(sqlHeader)) {
                psHeader.setString(1, invoice.getMaHoaDon());
                if (invoice.getMaLichHen() != null && !invoice.getMaLichHen().trim().isEmpty()) {
                    psHeader.setString(2, invoice.getMaLichHen().trim());
                } else {
                    psHeader.setNull(2, Types.VARCHAR);
                }
                psHeader.setString(3, invoice.getMaNhanVien());
                if (invoice.getMaKhachHang() != null && !invoice.getMaKhachHang().trim().isEmpty()) {
                    psHeader.setString(4, invoice.getMaKhachHang().trim());
                } else {
                    psHeader.setNull(4, Types.VARCHAR);
                }
                psHeader.setDate(5, Date.valueOf(invoice.getNgayLap() != null ? invoice.getNgayLap() : LocalDate.now()));
                psHeader.setString(6, invoice.getHinhThucThanhToan());
                psHeader.setString(7, invoice.getStatus().getDisplayName());
                if (invoice.getMaKhuyenMai() != null && !invoice.getMaKhuyenMai().trim().isEmpty()) {
                    psHeader.setString(8, invoice.getMaKhuyenMai().trim());
                } else {
                    psHeader.setNull(8, Types.VARCHAR);
                }
                psHeader.setBigDecimal(9, java.math.BigDecimal.valueOf(invoice.getTongTien()));
                psHeader.executeUpdate();
            }

            // 2. Thêm chi tiết và trừ kho
            boolean isPaid = (invoice.getStatus() == Invoice.Status.PAID);

            for (InvoiceItem item : invoice.getItems()) {
                if (item.getMaSanPham() != null && !item.getMaSanPham().trim().isEmpty()) {
                    try (PreparedStatement psItem = conn.prepareStatement(sqlProductItem)) {
                        psItem.setString(1, invoice.getMaHoaDon());
                        psItem.setString(2, item.getMaSanPham().trim());
                        psItem.setInt(3, item.getSoLuong());
                        psItem.setBigDecimal(4, java.math.BigDecimal.valueOf(item.getDonGia()));
                        psItem.executeUpdate();
                    }

                    // Trừ tồn kho nếu đã thanh toán
                    if (isPaid) {
                        try (PreparedStatement psStock = conn.prepareStatement(sqlDeductStock)) {
                            psStock.setInt(1, item.getSoLuong());
                            psStock.setString(2, item.getMaSanPham().trim());
                            psStock.setInt(3, item.getSoLuong());
                            int affected = psStock.executeUpdate();
                            if (affected == 0) {
                                throw new SQLException("Sản phẩm [" + item.getTenHang() + "] không đủ số lượng tồn kho để xuất bán.");
                            }
                        }
                    }
                } else if (item.getMaDichVu() != null && !item.getMaDichVu().trim().isEmpty()) {
                    try (PreparedStatement psItem = conn.prepareStatement(sqlServiceItem)) {
                        psItem.setString(1, invoice.getMaHoaDon());
                        psItem.setString(2, item.getMaDichVu().trim());
                        psItem.setInt(3, 1); // Ràng buộc DB dịch vụ soLuong = 1
                        psItem.setBigDecimal(4, java.math.BigDecimal.valueOf(item.getDonGia()));
                        psItem.executeUpdate();
                    }
                }
            }

            conn.commit();
        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Lỗi rollback hóa đơn: " + rollbackEx.getMessage());
                }
            }
            throw ex;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Hủy hóa đơn (Xóa mềm - chuyển trạng thái sang 'Đã hủy').
     * Tự động hoàn lại số lượng tồn kho của các sản phẩm trong hóa đơn.
     */
    public void cancelInvoice(String maHoaDon) throws SQLException {
        String sqlCheck = "SELECT trangThaiThanhToan FROM HoaDon WHERE maHoaDon = ?";
        String sqlUpdateStatus = "UPDATE HoaDon SET trangThaiThanhToan = N'Đã hủy' WHERE maHoaDon = ?";
        String sqlGetProducts = "SELECT maSanPham, soLuong FROM ChiTietHoaDon_SanPham WHERE maHoaDon = ?";
        String sqlRestoreStock = "UPDATE SanPham SET soLuongTon = soLuongTon + ? WHERE maSanPham = ?";

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            String currentStatus = null;
            try (PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {
                psCheck.setString(1, maHoaDon);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        currentStatus = rs.getString(1);
                    }
                }
            }

            if (currentStatus == null) {
                throw new SQLException("Không tìm thấy hóa đơn [" + maHoaDon + "].");
            }

            if ("Đã hủy".equalsIgnoreCase(currentStatus)) {
                throw new SQLException("Hóa đơn [" + maHoaDon + "] đã ở trạng thái Đã hủy trước đó.");
            }

            // Cập nhật trạng thái sang Đã hủy
            try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateStatus)) {
                psUpdate.setString(1, maHoaDon);
                psUpdate.executeUpdate();
            }

            // Hoàn lại số lượng tồn kho nếu hóa đơn đó trước đó đã thanh toán
            if ("Đã thanh toán".equalsIgnoreCase(currentStatus)) {
                List<Object[]> productList = new ArrayList<>();
                try (PreparedStatement psGet = conn.prepareStatement(sqlGetProducts)) {
                    psGet.setString(1, maHoaDon);
                    try (ResultSet rs = psGet.executeQuery()) {
                        while (rs.next()) {
                            productList.add(new Object[]{rs.getString(1), rs.getInt(2)});
                        }
                    }
                }

                for (Object[] row : productList) {
                    String spId = (String) row[0];
                    int qty = (int) row[1];
                    try (PreparedStatement psRestore = conn.prepareStatement(sqlRestoreStock)) {
                        psRestore.setInt(1, qty);
                        psRestore.setString(2, spId);
                        psRestore.executeUpdate();
                    }
                }
            }

            conn.commit();
        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Lỗi rollback hủy hóa đơn: " + rollbackEx.getMessage());
                }
            }
            throw ex;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Tra cứu & Tìm kiếm hóa đơn bán hàng đa tiêu chí.
     */
    public List<Invoice> searchInvoices(String keyword, String statusFilter, LocalDate fromDate, LocalDate toDate) {
        List<Invoice> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT hd.maHoaDon, hd.maLichHen, hd.maNhanVien, nv.hoTen AS tenNhanVien,
                   hd.maKhachHang, kh.hoTen AS tenKhachHang, kh.soDienThoai AS sdtKhachHang,
                   hd.ngayLap, hd.hinhThucThanhToan, hd.trangThaiThanhToan,
                   hd.maKhuyenMai, km.tenChuongTrinh AS tenKhuyenMai, km.phanTramGiam,
                   hd.tongTien
            FROM HoaDon hd
            LEFT JOIN NhanVien nv ON hd.maNhanVien = nv.maNhanVien
            LEFT JOIN KhachHang kh ON hd.maKhachHang = kh.maKhachHang
            LEFT JOIN KhuyenMai km ON hd.maKhuyenMai = km.maKhuyenMai
            WHERE 1=1
        """);
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (hd.maHoaDon LIKE ? OR kh.hoTen LIKE ? OR kh.soDienThoai LIKE ? OR nv.hoTen LIKE ?)");
            String k = "%" + keyword.trim() + "%";
            params.add(k);
            params.add(k);
            params.add(k);
            params.add(k);
        }

        if (statusFilter != null && !statusFilter.trim().isEmpty() && !"Tất cả trạng thái".equalsIgnoreCase(statusFilter) && !"Tất cả".equalsIgnoreCase(statusFilter)) {
            sql.append(" AND hd.trangThaiThanhToan = ?");
            params.add(statusFilter.trim());
        }

        if (fromDate != null) {
            sql.append(" AND hd.ngayLap >= ?");
            params.add(Date.valueOf(fromDate));
        }

        if (toDate != null) {
            sql.append(" AND hd.ngayLap <= ?");
            params.add(Date.valueOf(toDate));
        }

        sql.append(" ORDER BY hd.ngayLap DESC, hd.maHoaDon DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Invoice inv = new Invoice();
                    inv.setMaHoaDon(rs.getString("maHoaDon"));
                    inv.setMaLichHen(rs.getString("maLichHen"));
                    inv.setMaNhanVien(rs.getString("maNhanVien"));
                    inv.setTenNhanVien(rs.getString("tenNhanVien"));
                    inv.setMaKhachHang(rs.getString("maKhachHang"));
                    inv.setTenKhachHang(rs.getString("tenKhachHang"));
                    inv.setSoDienThoaiKhachHang(rs.getString("sdtKhachHang"));

                    Date d = rs.getDate("ngayLap");
                    inv.setNgayLap(d != null ? d.toLocalDate() : null);

                    inv.setHinhThucThanhToan(rs.getString("hinhThucThanhToan"));
                    inv.setStatus(Invoice.Status.fromDisplayName(rs.getString("trangThaiThanhToan")));
                    inv.setMaKhuyenMai(rs.getString("maKhuyenMai"));
                    inv.setTenKhuyenMai(rs.getString("tenKhuyenMai"));
                    inv.setPhanTramGiam(rs.getDouble("phanTramGiam"));
                    inv.setTongTien(rs.getDouble("tongTien"));

                    list.add(inv);
                }
            }
        } catch (SQLException ex) {
            System.err.println("Lỗi khi tìm kiếm hóa đơn: " + ex.getMessage());
        }

        return list;
    }

    /**
     * Tra cứu một hóa đơn theo mã định danh kèm theo danh sách sản phẩm và dịch vụ chi tiết.
     */
    public Optional<Invoice> getInvoiceById(String maHoaDon) {
        String sqlHeader = """
            SELECT hd.maHoaDon, hd.maLichHen, hd.maNhanVien, nv.hoTen AS tenNhanVien,
                   hd.maKhachHang, kh.hoTen AS tenKhachHang, kh.soDienThoai AS sdtKhachHang,
                   hd.ngayLap, hd.hinhThucThanhToan, hd.trangThaiThanhToan,
                   hd.maKhuyenMai, km.tenChuongTrinh AS tenKhuyenMai, km.phanTramGiam,
                   hd.tongTien
            FROM HoaDon hd
            LEFT JOIN NhanVien nv ON hd.maNhanVien = nv.maNhanVien
            LEFT JOIN KhachHang kh ON hd.maKhachHang = kh.maKhachHang
            LEFT JOIN KhuyenMai km ON hd.maKhuyenMai = km.maKhuyenMai
            WHERE hd.maHoaDon = ?
        """;

        String sqlProducts = """
            SELECT ct.maHoaDon, ct.maSanPham, sp.tenSanPham, ct.soLuong, ct.donGia
            FROM ChiTietHoaDon_SanPham ct
            LEFT JOIN SanPham sp ON ct.maSanPham = sp.maSanPham
            WHERE ct.maHoaDon = ?
        """;

        String sqlServices = """
            SELECT ct.maHoaDon, ct.maDichVu, dv.tenDichVu, ct.soLuong, ct.donGia
            FROM ChiTietHoaDon_DichVu ct
            LEFT JOIN DichVuChamSoc dv ON ct.maDichVu = dv.maDichVu
            WHERE ct.maHoaDon = ?
        """;

        try (Connection conn = DatabaseManager.getConnection()) {
            Invoice inv = null;
            try (PreparedStatement psHeader = conn.prepareStatement(sqlHeader)) {
                psHeader.setString(1, maHoaDon);
                try (ResultSet rs = psHeader.executeQuery()) {
                    if (rs.next()) {
                        inv = new Invoice();
                        inv.setMaHoaDon(rs.getString("maHoaDon"));
                        inv.setMaLichHen(rs.getString("maLichHen"));
                        inv.setMaNhanVien(rs.getString("maNhanVien"));
                        inv.setTenNhanVien(rs.getString("tenNhanVien"));
                        inv.setMaKhachHang(rs.getString("maKhachHang"));
                        inv.setTenKhachHang(rs.getString("tenKhachHang"));
                        inv.setSoDienThoaiKhachHang(rs.getString("sdtKhachHang"));

                        Date d = rs.getDate("ngayLap");
                        inv.setNgayLap(d != null ? d.toLocalDate() : null);

                        inv.setHinhThucThanhToan(rs.getString("hinhThucThanhToan"));
                        inv.setStatus(Invoice.Status.fromDisplayName(rs.getString("trangThaiThanhToan")));
                        inv.setMaKhuyenMai(rs.getString("maKhuyenMai"));
                        inv.setTenKhuyenMai(rs.getString("tenKhuyenMai"));
                        inv.setPhanTramGiam(rs.getDouble("phanTramGiam"));
                        inv.setTongTien(rs.getDouble("tongTien"));
                    }
                }
            }

            if (inv == null) {
                return Optional.empty();
            }

            List<InvoiceItem> items = new ArrayList<>();

            // Nạp chi tiết sản phẩm
            try (PreparedStatement psProd = conn.prepareStatement(sqlProducts)) {
                psProd.setString(1, maHoaDon);
                try (ResultSet rs = psProd.executeQuery()) {
                    while (rs.next()) {
                        InvoiceItem item = new InvoiceItem();
                        item.setMaHoaDon(maHoaDon);
                        item.setMaSanPham(rs.getString("maSanPham"));
                        item.setTenHang(rs.getString("tenSanPham"));
                        item.setSoLuong(rs.getInt("soLuong"));
                        item.setDonGia(rs.getDouble("donGia"));
                        items.add(item);
                    }
                }
            }

            // Nạp chi tiết dịch vụ
            try (PreparedStatement psServ = conn.prepareStatement(sqlServices)) {
                psServ.setString(1, maHoaDon);
                try (ResultSet rs = psServ.executeQuery()) {
                    while (rs.next()) {
                        InvoiceItem item = new InvoiceItem();
                        item.setMaHoaDon(maHoaDon);
                        item.setMaDichVu(rs.getString("maDichVu"));
                        item.setTenHang(rs.getString("tenDichVu"));
                        item.setSoLuong(rs.getInt("soLuong"));
                        item.setDonGia(rs.getDouble("donGia"));
                        items.add(item);
                    }
                }
            }

            inv.setItems(items);

            // Tính tiền giảm nếu có
            if (inv.getPhanTramGiam() > 0) {
                double discount = inv.getTongTienHang() * (inv.getPhanTramGiam() / 100.0);
                inv.setTienGiam(discount);
            }

            return Optional.of(inv);
        } catch (SQLException ex) {
            System.err.println("Lỗi khi lấy chi tiết hóa đơn [" + maHoaDon + "]: " + ex.getMessage());
        }

        return Optional.empty();
    }

    /**
     * Lấy danh sách hóa đơn ĐÃ THANH TOÁN (PAID) phục vụ màn hình Phiếu đổi trả của Tuấn Anh.
     */
    public List<Invoice> getPaidInvoices(String keyword) {
        return searchInvoices(keyword, "Đã thanh toán", null, null);
    }
}

