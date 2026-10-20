package com.petshop.dao;

import com.petshop.model.CareService;
import com.petshop.model.ServicePricing;
import com.petshop.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Lớp DAO quản lý truy vấn cơ sở dữ liệu cho Dịch vụ chăm sóc và Bảng giá dịch vụ.
 * Tuân thủ tiêu chuẩn:
 * - Tên lớp là danh từ (CareServiceDao), tên phương thức là động từ.
 * - Sử dụng 100% PreparedStatement và xử lý transaction an toàn.
 * - Hỗ trợ các chức năng: Cập nhật dịch vụ (SRV_CRUD), Tìm kiếm dịch vụ (SRV_SEARCH),
 *   Bảng giá dịch vụ (SRV_PRICING), xóa mềm (ngừng cung cấp).
 */
public class CareServiceDao {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Tự động sinh mã dịch vụ tiếp theo theo định dạng chuẩn OOAD:
     * DV-YYYYMMDD-NNNNNN (Ví dụ: DV-20261020-000001).
     */
    public synchronized String generateNextServiceId() {
        String todayStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = "DV-" + todayStr + "-";
        String sql = "SELECT MAX(maDichVu) FROM DichVuChamSoc WHERE maDichVu LIKE ?";

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
            System.err.println("Lỗi khi sinh mã dịch vụ tiếp theo: " + ex.getMessage());
        }

        return prefix + "000001";
    }

    /**
     * Tự động sinh mã bảng giá dịch vụ tiếp theo theo định dạng:
     * BG-YYYYMMDD-NNNNNN (Ví dụ: BG-20261020-000001).
     */
    public synchronized String generateNextPricingId() {
        String todayStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = "BG-" + todayStr + "-";
        String sql = "SELECT MAX(maBangGia) FROM BangGiaDichVu WHERE maBangGia LIKE ?";

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
            System.err.println("Lỗi khi sinh mã bảng giá tiếp theo: " + ex.getMessage());
        }

        return prefix + "000001";
    }

    // =========================================================================
    // CRUD DỊCH VỤ CHĂM SÓC (DichVuChamSoc)
    // =========================================================================

    /**
     * Thêm mới một dịch vụ chăm sóc vào CSDL.
     */
    public void addCareService(CareService service) throws SQLException {
        if (service.getMaDichVu() == null || service.getMaDichVu().trim().isEmpty()) {
            service.setMaDichVu(generateNextServiceId());
        }

        String sql = "INSERT INTO DichVuChamSoc (maDichVu, tenDichVu, moTa, thoiLuong, trangThai) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, service.getMaDichVu());
            ps.setString(2, service.getTenDichVu());
            ps.setString(3, service.getMoTa());
            ps.setInt(4, service.getThoiLuong());
            ps.setBoolean(5, service.isTrangThai());
            ps.executeUpdate();
        }
    }

    /**
     * Cập nhật thông tin dịch vụ chăm sóc.
     */
    public void updateCareService(CareService service) throws SQLException {
        String sql = "UPDATE DichVuChamSoc SET tenDichVu = ?, moTa = ?, thoiLuong = ?, trangThai = ? WHERE maDichVu = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, service.getTenDichVu());
            ps.setString(2, service.getMoTa());
            ps.setInt(3, service.getThoiLuong());
            ps.setBoolean(4, service.isTrangThai());
            ps.setString(5, service.getMaDichVu());
            ps.executeUpdate();
        }
    }

    /**
     * Ngừng cung cấp dịch vụ (Xóa mềm - trangThai = 0).
     */
    public void deactivateCareService(String maDichVu) throws SQLException {
        String sql = "UPDATE DichVuChamSoc SET trangThai = 0 WHERE maDichVu = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maDichVu);
            ps.executeUpdate();
        }
    }

    /**
     * Kích hoạt lại dịch vụ chăm sóc đã tạm ngừng (trangThai = 1).
     */
    public void activateCareService(String maDichVu) throws SQLException {
        String sql = "UPDATE DichVuChamSoc SET trangThai = 1 WHERE maDichVu = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maDichVu);
            ps.executeUpdate();
        }
    }

    /**
     * Tìm kiếm và lọc danh sách dịch vụ chăm sóc theo từ khóa và trạng thái hoạt động.
     * Tự động truy vấn giá 3 bậc hiện hành để hiển thị trực quan.
     */
    public List<CareService> searchCareServices(String keyword, Boolean activeFilter) {
        List<CareService> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT maDichVu, tenDichVu, moTa, thoiLuong, trangThai FROM DichVuChamSoc WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (maDichVu LIKE ? OR tenDichVu LIKE ? OR moTa LIKE ?)");
            String k = "%" + keyword.trim() + "%";
            params.add(k);
            params.add(k);
            params.add(k);
        }

        if (activeFilter != null) {
            sql.append(" AND trangThai = ?");
            params.add(activeFilter);
        }

        sql.append(" ORDER BY maDichVu ASC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CareService service = new CareService();
                    service.setMaDichVu(rs.getString("maDichVu"));
                    service.setTenDichVu(rs.getString("tenDichVu"));
                    service.setMoTa(rs.getString("moTa"));
                    service.setThoiLuong(rs.getInt("thoiLuong"));
                    service.setTrangThai(rs.getBoolean("trangThai"));
                    list.add(service);
                }
            }
        } catch (SQLException ex) {
            System.err.println("Lỗi khi tìm kiếm dịch vụ chăm sóc: " + ex.getMessage());
        }

        // Tải kèm giá hiện hành 3 bậc
        napGiaHienHanhChoDanhSach(list);
        return list;
    }

    private void napGiaHienHanhChoDanhSach(List<CareService> services) {
        if (services.isEmpty()) return;

        String sql = """
            SELECT maDichVu, phanLoaiKichThuoc, gia
            FROM BangGiaDichVu
            WHERE (ngayHetHieuLuc IS NULL OR ngayHetHieuLuc >= CAST(GETDATE() AS DATE))
              AND ngayApDung <= CAST(GETDATE() AS DATE)
            ORDER BY ngayApDung DESC
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String dvId = rs.getString("maDichVu");
                String size = rs.getString("phanLoaiKichThuoc");
                double price = rs.getDouble("gia");

                for (CareService s : services) {
                    if (s.getMaDichVu().equals(dvId)) {
                        if ("Nhỏ".equalsIgnoreCase(size) && s.getGiaNho() == 0) s.setGiaNho(price);
                        if ("Vừa".equalsIgnoreCase(size) && s.getGiaVua() == 0) s.setGiaVua(price);
                        if ("Lớn".equalsIgnoreCase(size) && s.getGiaLon() == 0) s.setGiaLon(price);
                    }
                }
            }
        } catch (SQLException ex) {
            System.err.println("Lỗi khi nạp giá dịch vụ hiện hành: " + ex.getMessage());
        }
    }

    /**
     * Tra cứu một dịch vụ theo mã.
     */
    public Optional<CareService> getServiceById(String maDichVu) {
        String sql = "SELECT maDichVu, tenDichVu, moTa, thoiLuong, trangThai FROM DichVuChamSoc WHERE maDichVu = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maDichVu);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CareService service = new CareService();
                    service.setMaDichVu(rs.getString("maDichVu"));
                    service.setTenDichVu(rs.getString("tenDichVu"));
                    service.setMoTa(rs.getString("moTa"));
                    service.setThoiLuong(rs.getInt("thoiLuong"));
                    service.setTrangThai(rs.getBoolean("trangThai"));
                    napGiaHienHanhChoDanhSach(List.of(service));
                    return Optional.of(service);
                }
            }
        } catch (SQLException ex) {
            System.err.println("Lỗi khi lấy dịch vụ theo ID: " + ex.getMessage());
        }
        return Optional.empty();
    }

    // =========================================================================
    // QUẢN LÝ BẢNG GIÁ DỊCH VỤ (BangGiaDichVu)
    // =========================================================================

    /**
     * Thêm mới một mức giá dịch vụ theo bậc cân nặng.
     */
    public void addServicePricing(ServicePricing pricing) throws SQLException {
        if (pricing.getMaBangGia() == null || pricing.getMaBangGia().trim().isEmpty()) {
            pricing.setMaBangGia(generateNextPricingId());
        }

        String sql = "INSERT INTO BangGiaDichVu (maBangGia, maDichVu, gia, phanLoaiKichThuoc, ngayApDung, ngayHetHieuLuc) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pricing.getMaBangGia());
            ps.setString(2, pricing.getMaDichVu());
            ps.setBigDecimal(3, java.math.BigDecimal.valueOf(pricing.getGia()));
            ps.setString(4, pricing.getPhanLoaiKichThuoc());
            ps.setDate(5, Date.valueOf(pricing.getNgayApDung() != null ? pricing.getNgayApDung() : LocalDate.now()));
            if (pricing.getNgayHetHieuLuc() != null) {
                ps.setDate(6, Date.valueOf(pricing.getNgayHetHieuLuc()));
            } else {
                ps.setNull(6, Types.DATE);
            }
            ps.executeUpdate();
        }
    }

    /**
     * Cập nhật thông tin mức giá dịch vụ.
     */
    public void updateServicePricing(ServicePricing pricing) throws SQLException {
        String sql = "UPDATE BangGiaDichVu SET gia = ?, phanLoaiKichThuoc = ?, ngayApDung = ?, ngayHetHieuLuc = ? WHERE maBangGia = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, java.math.BigDecimal.valueOf(pricing.getGia()));
            ps.setString(2, pricing.getPhanLoaiKichThuoc());
            ps.setDate(3, Date.valueOf(pricing.getNgayApDung()));
            if (pricing.getNgayHetHieuLuc() != null) {
                ps.setDate(4, Date.valueOf(pricing.getNgayHetHieuLuc()));
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.setString(5, pricing.getMaBangGia());
            ps.executeUpdate();
        }
    }

    /**
     * Tìm kiếm và lọc danh sách bảng giá theo từ khóa và bậc kích thước.
     */
    public List<ServicePricing> searchServicePricings(String keyword, String sizeFilter) {
        List<ServicePricing> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT bg.maBangGia, bg.maDichVu, dv.tenDichVu, bg.gia, bg.phanLoaiKichThuoc, bg.ngayApDung, bg.ngayHetHieuLuc
            FROM BangGiaDichVu bg
            LEFT JOIN DichVuChamSoc dv ON bg.maDichVu = dv.maDichVu
            WHERE 1=1
        """);
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (bg.maBangGia LIKE ? OR dv.tenDichVu LIKE ? OR dv.maDichVu LIKE ?)");
            String k = "%" + keyword.trim() + "%";
            params.add(k);
            params.add(k);
            params.add(k);
        }

        if (sizeFilter != null && !sizeFilter.trim().isEmpty() && !"Tất cả kích thước".equalsIgnoreCase(sizeFilter) && !"Tất cả".equalsIgnoreCase(sizeFilter)) {
            sql.append(" AND bg.phanLoaiKichThuoc = ?");
            params.add(sizeFilter.trim());
        }

        sql.append(" ORDER BY dv.tenDichVu ASC, bg.phanLoaiKichThuoc ASC, bg.ngayApDung DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ServicePricing p = new ServicePricing();
                    p.setMaBangGia(rs.getString("maBangGia"));
                    p.setMaDichVu(rs.getString("maDichVu"));
                    p.setTenDichVu(rs.getString("tenDichVu"));
                    p.setGia(rs.getDouble("gia"));
                    p.setPhanLoaiKichThuoc(rs.getString("phanLoaiKichThuoc"));

                    Date dApp = rs.getDate("ngayApDung");
                    p.setNgayApDung(dApp != null ? dApp.toLocalDate() : null);

                    Date dExp = rs.getDate("ngayHetHieuLuc");
                    p.setNgayHetHieuLuc(dExp != null ? dExp.toLocalDate() : null);

                    list.add(p);
                }
            }
        } catch (SQLException ex) {
            System.err.println("Lỗi khi tìm kiếm bảng giá dịch vụ: " + ex.getMessage());
        }

        return list;
    }

    /**
     * Tra cứu đơn giá dịch vụ theo cân nặng thú cưng và ngày áp dụng.
     * Quy tắc phân loại:
     * - Cân nặng < 5 kg -> Bậc Nhỏ
     * - 5 kg <= Cân nặng <= 10 kg -> Bậc Vừa
     * - Cân nặng > 10 kg -> Bậc Lớn
     */
    public double getPriceForWeight(String maDichVu, double canNang, LocalDate date) {
        String size = canNang < 5.0 ? ServicePricing.BAC_NHO : (canNang <= 10.0 ? ServicePricing.BAC_VUA : ServicePricing.BAC_LON);
        return getPriceForSize(maDichVu, size, date);
    }

    /**
     * Tra cứu đơn giá dịch vụ theo phân loại kích thước ('Nhỏ', 'Vừa', 'Lớn') và ngày áp dụng.
     */
    public double getPriceForSize(String maDichVu, String size, LocalDate date) {
        if (maDichVu == null || maDichVu.trim().isEmpty() || size == null) {
            return 0;
        }
        LocalDate searchDate = date != null ? date : LocalDate.now();
        String sql = """
            SELECT TOP 1 gia FROM BangGiaDichVu
            WHERE maDichVu = ? AND phanLoaiKichThuoc = ? AND ngayApDung <= ?
              AND (ngayHetHieuLuc IS NULL OR ngayHetHieuLuc >= ?)
            ORDER BY ngayApDung DESC
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maDichVu.trim());
            ps.setString(2, size.trim());
            ps.setDate(3, Date.valueOf(searchDate));
            ps.setDate(4, Date.valueOf(searchDate));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("gia");
                }
            }
        } catch (SQLException ex) {
            System.err.println("Lỗi khi tra cứu giá dịch vụ theo kích thước: " + ex.getMessage());
        }
        return 0;
    }
}

