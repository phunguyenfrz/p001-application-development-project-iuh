package com.petshop.dao;

import com.petshop.model.ImportOrder;
import com.petshop.model.ImportOrderItem;
import com.petshop.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Lớp DAO quản lý truy vấn cơ sở dữ liệu cho phân hệ Đơn hàng nhập (DonHang & ChiTietDonHang).
 * Tuân thủ tiêu chuẩn:
 * - Tên lớp là danh từ (ImportOrderDao), tên phương thức là động từ.
 * - Sử dụng 100% PreparedStatement và quản lý giao dịch (Transaction) an toàn.
 * - Hỗ trợ các chức năng: Lập đơn (IMP_CREATE), Nhận hàng (IMP_CONFIRM / IMP_RECEIVE),
 *   Cập nhật & Hủy đơn (IMP_CANCEL / IMP_UPDATE), Tìm kiếm (IMP_SEARCH).
 */
public class ImportOrderDao {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public ImportOrderDao() {
        khoiTaoDuLieuMauNeuTrong();
    }

    /**
     * Tự động sinh mã đơn hàng nhập tiếp theo theo định dạng chuẩn OOAD:
     * DH-YYYYMMDD-NNNNNN (Ví dụ: DH-20261020-000001).
     */
    public synchronized String generateNextOrderId() {
        String todayStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = "DH-" + todayStr + "-";
        String sql = "SELECT MAX(maDonHang) FROM DonHang WHERE maDonHang LIKE ?";

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
            System.err.println("Lỗi khi sinh mã đơn nhập tiếp theo: " + ex.getMessage());
        }

        return prefix + "000001";
    }

    /**
     * Lập đơn hàng nhập mới cùng các dòng chi tiết trong 1 giao dịch (Transaction) an toàn.
     * Mặc định lưu với trạng thái 'Chờ nhận hàng'.
     *
     * @param order Đơn hàng nhập cần tạo
     */
    public void createImportOrder(ImportOrder order) throws SQLException {
        if (order.getMaDonHang() == null || order.getMaDonHang().trim().isEmpty()) {
            order.setMaDonHang(generateNextOrderId());
        }

        String sqlOrder = "INSERT INTO DonHang (maDonHang, maNhaCungCap, maNhanVien, ngayLap, tongTien, trangThai) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlItem = "INSERT INTO ChiTietDonHang (maDonHang, maSanPham, soLuong, donGia) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                // 1. Thêm bản ghi DonHang
                try (PreparedStatement psOrder = conn.prepareStatement(sqlOrder)) {
                    psOrder.setString(1, order.getMaDonHang());
                    psOrder.setString(2, order.getMaNhaCungCap());
                    psOrder.setString(3, order.getMaNhanVien());
                    psOrder.setDate(4, Date.valueOf(order.getNgayLap() != null ? order.getNgayLap() : LocalDate.now()));
                    psOrder.setBigDecimal(5, java.math.BigDecimal.valueOf(order.getTongTien()));
                    psOrder.setString(6, order.getTrangThai() != null ? order.getTrangThai() : ImportOrder.TRANG_THAI_CHO_NHAN);
                    psOrder.executeUpdate();
                }

                // 2. Thêm các bản ghi ChiTietDonHang
                if (order.getItems() != null && !order.getItems().isEmpty()) {
                    try (PreparedStatement psItem = conn.prepareStatement(sqlItem)) {
                        for (ImportOrderItem item : order.getItems()) {
                            psItem.setString(1, order.getMaDonHang());
                            psItem.setString(2, item.getMaSanPham());
                            psItem.setInt(3, item.getSoLuong());
                            psItem.setBigDecimal(4, java.math.BigDecimal.valueOf(item.getDonGia()));
                            psItem.addBatch();
                        }
                        psItem.executeBatch();
                    }
                }

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    /**
     * Xác nhận nhận hàng (IMP_CONFIRM / IMP_RECEIVE):
     * 1. Cập nhật trạng thái DonHang thành 'Hoàn tất'.
     * 2. Tự động CỘNG TỒN KHO vào bảng SanPham tương ứng với số lượng trong đơn nhập.
     * Cả 2 thao tác được thực hiện trong 1 Transaction.
     *
     * @param maDonHang Mã đơn hàng cần nhận
     */
    public void confirmReceiveOrder(String maDonHang) throws SQLException {
        String sqlCheck = "SELECT trangThai FROM DonHang WHERE maDonHang = ?";
        String sqlUpdateOrder = "UPDATE DonHang SET trangThai = ? WHERE maDonHang = ?";
        String sqlGetItems = "SELECT maSanPham, soLuong FROM ChiTietDonHang WHERE maDonHang = ?";
        String sqlUpdateStock = "UPDATE SanPham SET soLuongTon = soLuongTon + ? WHERE maSanPham = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                // Kiểm tra trạng thái hiện tại
                String currentStatus = null;
                try (PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {
                    psCheck.setString(1, maDonHang);
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (rs.next()) {
                            currentStatus = rs.getString(1);
                        }
                    }
                }

                if (currentStatus == null) {
                    throw new SQLException("Không tìm thấy đơn hàng nhập với mã: " + maDonHang);
                }

                if (!ImportOrder.TRANG_THAI_CHO_NHAN.equalsIgnoreCase(currentStatus)) {
                    throw new SQLException("Chỉ có thể xác nhận nhận hàng đối với đơn đang ở trạng thái 'Chờ nhận hàng'. (Trạng thái hiện tại: " + currentStatus + ")");
                }

                // 1. Cập nhật trạng thái đơn thành 'Hoàn tất'
                try (PreparedStatement psOrder = conn.prepareStatement(sqlUpdateOrder)) {
                    psOrder.setString(1, ImportOrder.TRANG_THAI_HOAN_TAT);
                    psOrder.setString(2, maDonHang);
                    psOrder.executeUpdate();
                }

                // 2. Lấy danh sách mặt hàng và cộng tồn kho
                List<ImportOrderItem> itemsToUpdate = new ArrayList<>();
                try (PreparedStatement psItems = conn.prepareStatement(sqlGetItems)) {
                    psItems.setString(1, maDonHang);
                    try (ResultSet rs = psItems.executeQuery()) {
                        while (rs.next()) {
                            ImportOrderItem item = new ImportOrderItem();
                            item.setMaSanPham(rs.getString("maSanPham"));
                            item.setSoLuong(rs.getInt("soLuong"));
                            itemsToUpdate.add(item);
                        }
                    }
                }

                try (PreparedStatement psStock = conn.prepareStatement(sqlUpdateStock)) {
                    for (ImportOrderItem item : itemsToUpdate) {
                        psStock.setInt(1, item.getSoLuong());
                        psStock.setString(2, item.getMaSanPham());
                        psStock.addBatch();
                    }
                    psStock.executeBatch();
                }

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    /**
     * Hủy đơn hàng nhập (IMP_CANCEL):
     * Thao tác xóa mềm chuyển trạng thái thành 'Đã hủy'.
     * Chỉ áp dụng cho đơn đang ở trạng thái 'Chờ nhận hàng'.
     *
     * @param maDonHang Mã đơn hàng cần hủy
     */
    public void cancelImportOrder(String maDonHang) throws SQLException {
        String sqlCheck = "SELECT trangThai FROM DonHang WHERE maDonHang = ?";
        String sqlUpdate = "UPDATE DonHang SET trangThai = ? WHERE maDonHang = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            // Kiểm tra trạng thái hiện tại
            String currentStatus = null;
            try (PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {
                psCheck.setString(1, maDonHang);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        currentStatus = rs.getString(1);
                    }
                }
            }

            if (currentStatus == null) {
                throw new SQLException("Không tìm thấy đơn hàng nhập với mã: " + maDonHang);
            }

            if (!ImportOrder.TRANG_THAI_CHO_NHAN.equalsIgnoreCase(currentStatus)) {
                throw new SQLException("Chỉ có thể hủy đơn hàng đang ở trạng thái 'Chờ nhận hàng'. (Trạng thái hiện tại: " + currentStatus + ")");
            }

            try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate)) {
                psUpdate.setString(1, ImportOrder.TRANG_THAI_DA_HUY);
                psUpdate.setString(2, maDonHang);
                psUpdate.executeUpdate();
            }
        }
    }

    /**
     * Cập nhật thông tin chi tiết đơn hàng nhập khi đơn còn ở trạng thái 'Chờ nhận hàng'.
     *
     * @param order Đơn hàng có thông tin và danh sách mặt hàng mới
     */
    public void updateImportOrder(ImportOrder order) throws SQLException {
        String sqlCheck = "SELECT trangThai FROM DonHang WHERE maDonHang = ?";
        String sqlUpdateOrder = "UPDATE DonHang SET maNhaCungCap = ?, ngayLap = ?, tongTien = ? WHERE maDonHang = ?";
        String sqlDeleteItems = "DELETE FROM ChiTietDonHang WHERE maDonHang = ?";
        String sqlInsertItem = "INSERT INTO ChiTietDonHang (maDonHang, maSanPham, soLuong, donGia) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                // Kiểm tra trạng thái
                try (PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {
                    psCheck.setString(1, order.getMaDonHang());
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (rs.next()) {
                            String st = rs.getString(1);
                            if (!ImportOrder.TRANG_THAI_CHO_NHAN.equalsIgnoreCase(st)) {
                                throw new SQLException("Không thể chỉnh sửa đơn hàng đã hoàn tất hoặc đã hủy.");
                            }
                        } else {
                            throw new SQLException("Không tìm thấy đơn hàng: " + order.getMaDonHang());
                        }
                    }
                }

                // Cập nhật thông tin đơn hàng
                try (PreparedStatement psOrder = conn.prepareStatement(sqlUpdateOrder)) {
                    psOrder.setString(1, order.getMaNhaCungCap());
                    psOrder.setDate(2, Date.valueOf(order.getNgayLap() != null ? order.getNgayLap() : LocalDate.now()));
                    psOrder.setBigDecimal(3, java.math.BigDecimal.valueOf(order.getTongTien()));
                    psOrder.setString(4, order.getMaDonHang());
                    psOrder.executeUpdate();
                }

                // Xóa chi tiết cũ và chèn chi tiết mới
                try (PreparedStatement psDel = conn.prepareStatement(sqlDeleteItems)) {
                    psDel.setString(1, order.getMaDonHang());
                    psDel.executeUpdate();
                }

                if (order.getItems() != null && !order.getItems().isEmpty()) {
                    try (PreparedStatement psIns = conn.prepareStatement(sqlInsertItem)) {
                        for (ImportOrderItem item : order.getItems()) {
                            psIns.setString(1, order.getMaDonHang());
                            psIns.setString(2, item.getMaSanPham());
                            psIns.setInt(3, item.getSoLuong());
                            psIns.setBigDecimal(4, java.math.BigDecimal.valueOf(item.getDonGia()));
                            psIns.addBatch();
                        }
                        psIns.executeBatch();
                    }
                }

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    /**
     * Tìm kiếm và lọc danh sách đơn hàng nhập theo nhiều tiêu chí:
     * từ khóa (mã đơn, tên NCC, tên NV), trạng thái, khoảng ngày lập.
     */
    public List<ImportOrder> searchImportOrders(String keyword, String status, LocalDate fromDate, LocalDate toDate) {
        List<ImportOrder> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT d.maDonHang, d.maNhaCungCap, ncc.tenNhaCungCap, ncc.soDienThoai AS sdtNCC, ncc.diaChi AS diaChiNCC,
                   d.maNhanVien, nv.hoTen AS tenNhanVien, d.ngayLap, d.tongTien, d.trangThai
            FROM DonHang d
            LEFT JOIN NhaCungCap ncc ON d.maNhaCungCap = ncc.maNhaCungCap
            LEFT JOIN NhanVien nv ON d.maNhanVien = nv.maNhanVien
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (d.maDonHang LIKE ? OR ncc.tenNhaCungCap LIKE ? OR nv.hoTen LIKE ?)");
            String k = "%" + keyword.trim() + "%";
            params.add(k);
            params.add(k);
            params.add(k);
        }

        if (status != null && !status.trim().isEmpty() && !"Tất cả trạng thái".equalsIgnoreCase(status) && !"Tất cả".equalsIgnoreCase(status)) {
            sql.append(" AND d.trangThai = ?");
            params.add(status.trim());
        }

        if (fromDate != null) {
            sql.append(" AND d.ngayLap >= ?");
            params.add(Date.valueOf(fromDate));
        }

        if (toDate != null) {
            sql.append(" AND d.ngayLap <= ?");
            params.add(Date.valueOf(toDate));
        }

        sql.append(" ORDER BY d.ngayLap DESC, d.maDonHang DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ImportOrder order = mapResultSetToOrder(rs);
                    list.add(order);
                }
            }
        } catch (SQLException ex) {
            System.err.println("Lỗi khi tìm kiếm đơn hàng nhập: " + ex.getMessage());
        }

        return list;
    }

    /**
     * Lấy chi tiết các mặt hàng trong một đơn hàng nhập.
     *
     * @param maDonHang Mã đơn hàng
     * @return Danh sách mặt hàng kèm tên sản phẩm và đơn vị tính
     */
    public List<ImportOrderItem> getOrderItems(String maDonHang) {
        List<ImportOrderItem> items = new ArrayList<>();
        String sql = """
            SELECT ct.maDonHang, ct.maSanPham, sp.tenSanPham, sp.donViTinh, ct.soLuong, ct.donGia
            FROM ChiTietDonHang ct
            LEFT JOIN SanPham sp ON ct.maSanPham = sp.maSanPham
            WHERE ct.maDonHang = ?
            ORDER BY ct.maSanPham
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maDonHang);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ImportOrderItem item = new ImportOrderItem();
                    item.setMaDonHang(rs.getString("maDonHang"));
                    item.setMaSanPham(rs.getString("maSanPham"));
                    item.setTenSanPham(rs.getString("tenSanPham") != null ? rs.getString("tenSanPham") : "Sản phẩm " + item.getMaSanPham());
                    item.setDonViTinh(rs.getString("donViTinh") != null ? rs.getString("donViTinh") : "Cái");
                    item.setSoLuong(rs.getInt("soLuong"));
                    item.setDonGia(rs.getDouble("donGia"));
                    item.setThanhTien(item.getSoLuong() * item.getDonGia());
                    items.add(item);
                }
            }
        } catch (SQLException ex) {
            System.err.println("Lỗi khi lấy chi tiết đơn hàng nhập: " + ex.getMessage());
        }

        return items;
    }

    /**
     * Tra cứu một đơn hàng nhập theo mã kèm danh sách mặt hàng đầy đủ.
     */
    public Optional<ImportOrder> getOrderById(String maDonHang) {
        String sql = """
            SELECT d.maDonHang, d.maNhaCungCap, ncc.tenNhaCungCap, ncc.soDienThoai AS sdtNCC, ncc.diaChi AS diaChiNCC,
                   d.maNhanVien, nv.hoTen AS tenNhanVien, d.ngayLap, d.tongTien, d.trangThai
            FROM DonHang d
            LEFT JOIN NhaCungCap ncc ON d.maNhaCungCap = ncc.maNhaCungCap
            LEFT JOIN NhanVien nv ON d.maNhanVien = nv.maNhanVien
            WHERE d.maDonHang = ?
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maDonHang);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ImportOrder order = mapResultSetToOrder(rs);
                    order.setItems(getOrderItems(maDonHang));
                    return Optional.of(order);
                }
            }
        } catch (SQLException ex) {
            System.err.println("Lỗi khi lấy đơn hàng theo ID: " + ex.getMessage());
        }

        return Optional.empty();
    }

    private ImportOrder mapResultSetToOrder(ResultSet rs) throws SQLException {
        ImportOrder order = new ImportOrder();
        order.setMaDonHang(rs.getString("maDonHang"));
        order.setMaNhaCungCap(rs.getString("maNhaCungCap"));
        order.setTenNhaCungCap(rs.getString("tenNhaCungCap"));
        order.setSoDienThoaiNhaCungCap(rs.getString("sdtNCC"));
        order.setDiaChiNhaCungCap(rs.getString("diaChiNCC"));
        order.setMaNhanVien(rs.getString("maNhanVien"));
        order.setTenNhanVien(rs.getString("tenNhanVien"));

        Date d = rs.getDate("ngayLap");
        order.setNgayLap(d != null ? d.toLocalDate() : LocalDate.now());

        order.setTongTien(rs.getDouble("tongTien"));
        order.setTrangThai(rs.getString("trangThai"));
        return order;
    }

    /**
     * Khởi tạo dữ liệu mẫu cho đơn hàng nhập nếu bảng DonHang đang trống
     * để tiện cho việc kiểm thử và nghiệm thu các màn hình.
     */
    private void khoiTaoDuLieuMauNeuTrong() {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM DonHang")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return; // Đã có dữ liệu
                }
            }

            // Lấy 1 NCC, 1 NV, 2 SP có sẵn trong DB để tạo đơn mẫu
            String nccId = null;
            String nvId = null;
            List<String> spIds = new ArrayList<>();

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT TOP 1 maNhaCungCap FROM NhaCungCap")) {
                if (rs.next()) nccId = rs.getString(1);
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT TOP 1 maNhanVien FROM NhanVien")) {
                if (rs.next()) nvId = rs.getString(1);
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT TOP 2 maSanPham FROM SanPham")) {
                while (rs.next()) spIds.add(rs.getString(1));
            }

            if (nccId != null && nvId != null && !spIds.isEmpty()) {
                String sp1 = spIds.get(0);
                String sp2 = spIds.size() > 1 ? spIds.get(1) : sp1;

                // 1. Đơn Chờ nhận hàng
                ImportOrder o1 = new ImportOrder("DH-20261019-000001", nccId, nvId, LocalDate.of(2026, 10, 19), 3500000, ImportOrder.TRANG_THAI_CHO_NHAN);
                o1.addItem(new ImportOrderItem(o1.getMaDonHang(), sp1, "Sản phẩm 1", "Gói", 20, 100000));
                o1.addItem(new ImportOrderItem(o1.getMaDonHang(), sp2, "Sản phẩm 2", "Túi", 15, 100000));
                o1.tinhTongTien();

                // 2. Đơn Hoàn tất
                ImportOrder o2 = new ImportOrder("DH-20261018-000001", nccId, nvId, LocalDate.of(2026, 10, 18), 2400000, ImportOrder.TRANG_THAI_HOAN_TAT);
                o2.addItem(new ImportOrderItem(o2.getMaDonHang(), sp1, "Sản phẩm 1", "Gói", 30, 80000));
                o2.tinhTongTien();

                // 3. Đơn Đã hủy
                ImportOrder o3 = new ImportOrder("DH-20261017-000001", nccId, nvId, LocalDate.of(2026, 10, 17), 1200000, ImportOrder.TRANG_THAI_DA_HUY);
                o3.addItem(new ImportOrderItem(o3.getMaDonHang(), sp2, "Sản phẩm 2", "Túi", 10, 120000));
                o3.tinhTongTien();

                createImportOrder(o1);
                createImportOrder(o2);
                createImportOrder(o3);
            }
        } catch (Exception ex) {
            // Không làm gián đoạn hệ thống nếu tạo dữ liệu mẫu gặp sự cố
            System.err.println("Khởi tạo dữ liệu mẫu DonHang (bỏ qua): " + ex.getMessage());
        }
    }
}

