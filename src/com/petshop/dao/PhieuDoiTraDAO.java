package com.petshop.dao;

import com.petshop.model.ChiTietPhieuDoiTra;
import com.petshop.model.PhieuDoiTra;
import com.petshop.util.DatabaseManager;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp truy xuất cơ sở dữ liệu cho Phiếu đổi trả (PhieuDoiTraDAO).
 * Thao tác với các bảng PhieuDoiTra và ChiTietPhieuDoiTra.
 */
public class PhieuDoiTraDAO {

    private static volatile boolean daKiemTraCauTruc = false;

    public PhieuDoiTraDAO() {
        tuDongNangCapCauTrucVaDuLieu();
    }

    private synchronized void tuDongNangCapCauTrucVaDuLieu() {
        if (daKiemTraCauTruc) return;
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) return;
            try (Statement st = conn.createStatement()) {
                // 1. Bổ sung các cột thiếu trong ChiTietPhieuDoiTra nếu chưa có
                st.executeUpdate("""
                    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.ChiTietPhieuDoiTra') AND name = 'lyDo')
                    BEGIN
                        ALTER TABLE dbo.ChiTietPhieuDoiTra ADD lyDo NVARCHAR(100) NULL;
                        ALTER TABLE dbo.ChiTietPhieuDoiTra ADD tinhTrang NVARCHAR(50) NULL;
                        ALTER TABLE dbo.ChiTietPhieuDoiTra ADD huongXuLy NVARCHAR(50) NULL;
                        ALTER TABLE dbo.ChiTietPhieuDoiTra ADD ghiChu NVARCHAR(255) NULL;
                        ALTER TABLE dbo.ChiTietPhieuDoiTra ADD maSanPhamDoi VARCHAR(20) NULL;
                        ALTER TABLE dbo.ChiTietPhieuDoiTra ADD soLuongDoi INT NULL;
                    END
                """);

                // 2. Bổ sung các cột thiếu trong PhieuDoiTra nếu chưa có
                st.executeUpdate("""
                    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.PhieuDoiTra') AND name = 'loaiXuLy')
                    BEGIN
                        ALTER TABLE dbo.PhieuDoiTra ADD loaiXuLy NVARCHAR(30) NOT NULL DEFAULT N'Hoàn tiền';
                        ALTER TABLE dbo.PhieuDoiTra ADD tienHoan DECIMAL(18,2) NOT NULL DEFAULT 0;
                        ALTER TABLE dbo.PhieuDoiTra ADD tienBu DECIMAL(18,2) NOT NULL DEFAULT 0;
                    END
                """);

                // 3. Bổ sung các cột thiếu trong HoaDon nếu chưa có
                st.executeUpdate("""
                    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.HoaDon') AND name = 'tienHoanLai')
                    BEGIN
                        ALTER TABLE dbo.HoaDon ADD tienHoanLai DECIMAL(18,2) NOT NULL DEFAULT 0;
                    END
                    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.HoaDon') AND name = 'tienThanhToanThem')
                    BEGIN
                        ALTER TABLE dbo.HoaDon ADD tienThanhToanThem DECIMAL(18,2) NOT NULL DEFAULT 0;
                    END
                    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.HoaDon') AND name = 'ghiChu')
                    BEGIN
                        ALTER TABLE dbo.HoaDon ADD ghiChu NVARCHAR(255) NULL;
                    END
                """);

                // 4. Tự động liên kết tạo chi tiết mặt hàng cho các phiếu đổi trả chưa có dòng chi tiết
                st.executeUpdate("""
                    INSERT INTO dbo.ChiTietPhieuDoiTra (maPhieuDoiTra, maHoaDon, maSanPham, soLuong, donGia, lyDo, tinhTrang, huongXuLy, ghiChu)
                    SELECT p.maPhieuDoiTra, p.maHoaDon, ct.maSanPham, ISNULL(p.soLuong, 1), ct.donGia,
                           ISNULL(p.lyDo, N'Không phù hợp'), N'Nguyên vẹn', N'Nhập lại kho bán', N'Tự động đồng bộ từ hóa đơn gốc'
                    FROM dbo.PhieuDoiTra p
                    CROSS APPLY (
                        SELECT TOP 1 maSanPham, donGia FROM dbo.ChiTietHoaDon_SanPham WHERE maHoaDon = p.maHoaDon
                    ) ct
                    WHERE NOT EXISTS (
                        SELECT 1 FROM dbo.ChiTietPhieuDoiTra WHERE maPhieuDoiTra = p.maPhieuDoiTra
                    )
                """);
                st.executeUpdate("UPDATE dbo.PhieuDoiTra SET trangThai = N'Đã duyệt' WHERE trangThai = N'Hoàn thành'");
            }
            daKiemTraCauTruc = true;
        } catch (Exception ignored) {
            // Bỏ qua nếu database không hỗ trợ quyền DDL
        }
    }

    /**
     * Sinh mã phiếu đổi trả tự động theo quy tắc: PDT-yyyyMMdd-xxxxxx.
     */
    public String sinhMaPhieu() {
        String tienTo = "PDT-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        String sql = "SELECT TOP 1 maPhieuDoiTra FROM dbo.PhieuDoiTra WHERE maPhieuDoiTra LIKE ? ORDER BY maPhieuDoiTra DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tienTo + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String maCu = rs.getString("maPhieuDoiTra");
                    int soThuTu = Integer.parseInt(maCu.substring(tienTo.length()));
                    return tienTo + String.format("%06d", soThuTu + 1);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi sinh mã phiếu đổi trả: " + e.getMessage(), e);
        }

        return tienTo + "000001";
    }

    /**
     * Lưu phiếu đổi trả mới và danh sách chi tiết mặt hàng vào database.
     */
    public boolean themPhieuDoiTra(PhieuDoiTra phieu) {
        String sqlPhieu = """
                INSERT INTO dbo.PhieuDoiTra (
                    maPhieuDoiTra, maHoaDon, maNhanVien, maKhachHang, ngayLap,
                    lyDo, soLuong, loaiXuLy, tienHoan, tienBu, trangThai
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        String sqlChiTiet = """
                INSERT INTO dbo.ChiTietPhieuDoiTra (
                    maPhieuDoiTra, maHoaDon, maSanPham, soLuong, donGia,
                    lyDo, tinhTrang, huongXuLy, ghiChu, maSanPhamDoi, soLuongDoi
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psPhieu = conn.prepareStatement(sqlPhieu);
                 PreparedStatement psChiTiet = conn.prepareStatement(sqlChiTiet)) {

                // 1. Lưu thông tin phiếu đổi trả
                psPhieu.setString(1, phieu.getMaPhieuDoiTra());
                psPhieu.setString(2, phieu.getMaHoaDon());
                psPhieu.setString(3, phieu.getMaNhanVien());
                psPhieu.setString(4, phieu.getMaKhachHang());
                psPhieu.setDate(5, Date.valueOf(phieu.getNgayLap()));
                psPhieu.setString(6, phieu.getLyDo());
                psPhieu.setInt(7, phieu.getSoLuong());
                psPhieu.setString(8, phieu.getLoaiXuLy());
                psPhieu.setBigDecimal(9, phieu.getTienHoan());
                psPhieu.setBigDecimal(10, phieu.getTienBu());
                psPhieu.setString(11, phieu.getTrangThai());
                psPhieu.executeUpdate();

                // 2. Lưu từng dòng sản phẩm chi tiết
                for (ChiTietPhieuDoiTra ct : phieu.getDanhSachSanPham()) {
                    psChiTiet.setString(1, phieu.getMaPhieuDoiTra());
                    psChiTiet.setString(2, phieu.getMaHoaDon());
                    psChiTiet.setString(3, ct.getMaSanPham());
                    psChiTiet.setInt(4, ct.getSoLuong());
                    psChiTiet.setBigDecimal(5, ct.getDonGia());
                    psChiTiet.setString(6, ct.getLyDo());
                    psChiTiet.setString(7, ct.getTinhTrang() != null ? ct.getTinhTrang() : "Nguyên vẹn");
                    psChiTiet.setString(8, ct.getHuongXuLy());
                    psChiTiet.setString(9, ct.getGhiChu());
                    psChiTiet.setString(10, ct.getMaSanPhamDoi());
                    if (ct.getSoLuongDoi() > 0) {
                        psChiTiet.setInt(11, ct.getSoLuongDoi());
                    } else {
                        psChiTiet.setNull(11, Types.INTEGER);
                    }
                    psChiTiet.addBatch();
                }
                psChiTiet.executeBatch();

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi thêm phiếu đổi trả: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật thông tin phiếu đổi trả và danh sách chi tiết (chỉ khi Chờ xử lý).
     */
    public boolean capNhatPhieuDoiTra(PhieuDoiTra phieu) {
        String sqlPhieu = """
                UPDATE dbo.PhieuDoiTra
                SET lyDo = ?, soLuong = ?, loaiXuLy = ?, tienHoan = ?, tienBu = ?
                WHERE maPhieuDoiTra = ? AND trangThai = N'Chờ xử lý'
                """;

        String sqlXoaChiTiet = "DELETE FROM dbo.ChiTietPhieuDoiTra WHERE maPhieuDoiTra = ?";

        String sqlChiTiet = """
                INSERT INTO dbo.ChiTietPhieuDoiTra (
                    maPhieuDoiTra, maHoaDon, maSanPham, soLuong, donGia,
                    lyDo, tinhTrang, huongXuLy, ghiChu, maSanPhamDoi, soLuongDoi
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psPhieu = conn.prepareStatement(sqlPhieu);
                 PreparedStatement psXoa = conn.prepareStatement(sqlXoaChiTiet);
                 PreparedStatement psChiTiet = conn.prepareStatement(sqlChiTiet)) {

                // 1. Cập nhật phiếu
                psPhieu.setString(1, phieu.getLyDo());
                psPhieu.setInt(2, phieu.getSoLuong());
                psPhieu.setString(3, phieu.getLoaiXuLy());
                psPhieu.setBigDecimal(4, phieu.getTienHoan());
                psPhieu.setBigDecimal(5, phieu.getTienBu());
                psPhieu.setString(6, phieu.getMaPhieuDoiTra());
                int soDong = psPhieu.executeUpdate();
                if (soDong <= 0) {
                    conn.rollback();
                    throw new IllegalStateException("Phiếu đã được duyệt/xác nhận, không thể chỉnh sửa!");
                }

                // 2. Xóa chi tiết cũ
                psXoa.setString(1, phieu.getMaPhieuDoiTra());
                psXoa.executeUpdate();

                // 3. Thêm chi tiết mới
                for (ChiTietPhieuDoiTra ct : phieu.getDanhSachSanPham()) {
                    psChiTiet.setString(1, phieu.getMaPhieuDoiTra());
                    psChiTiet.setString(2, phieu.getMaHoaDon());
                    psChiTiet.setString(3, ct.getMaSanPham());
                    psChiTiet.setInt(4, ct.getSoLuong());
                    psChiTiet.setBigDecimal(5, ct.getDonGia());
                    psChiTiet.setString(6, ct.getLyDo());
                    psChiTiet.setString(7, ct.getTinhTrang() != null ? ct.getTinhTrang() : "Nguyên vẹn");
                    psChiTiet.setString(8, ct.getHuongXuLy());
                    psChiTiet.setString(9, ct.getGhiChu());
                    psChiTiet.setString(10, ct.getMaSanPhamDoi());
                    if (ct.getSoLuongDoi() > 0) {
                        psChiTiet.setInt(11, ct.getSoLuongDoi());
                    } else {
                        psChiTiet.setNull(11, Types.INTEGER);
                    }
                    psChiTiet.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi cập nhật phiếu đổi trả: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật trạng thái phiếu đổi trả (Chờ xử lý, Đã duyệt, Từ chối).
     */
    public boolean capNhatTrangThai(String maPhieuDoiTra, String trangThaiMoi, String lyDoTuChoi) {
        String sql;
        if (lyDoTuChoi != null && !lyDoTuChoi.trim().isEmpty()) {
            sql = "UPDATE dbo.PhieuDoiTra SET trangThai = ?, lyDo = ? WHERE maPhieuDoiTra = ?";
        } else {
            sql = "UPDATE dbo.PhieuDoiTra SET trangThai = ? WHERE maPhieuDoiTra = ?";
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (lyDoTuChoi != null && !lyDoTuChoi.trim().isEmpty()) {
                ps.setString(1, trangThaiMoi);
                ps.setString(2, lyDoTuChoi.trim());
                ps.setString(3, maPhieuDoiTra);
            } else {
                ps.setString(1, trangThaiMoi);
                ps.setString(2, maPhieuDoiTra);
            }

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi cập nhật trạng thái phiếu đổi trả: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật số lượng tồn kho sản phẩm khi hoàn thành phiếu đổi trả:
     * - Sản phẩm trả lại được cộng thêm vào tồn kho nếu hướng xử lý là "Thu lại vào kho" / "Nhập lại kho bán".
     * - Nếu hướng xử lý là "Hủy bỏ" / "Tiêu hủy" / "Trả nhà cung cấp" thì không cộng lại tồn kho.
     * - Sản phẩm đổi mới bị trừ tồn kho tương ứng.
     */
    public boolean capNhatTonKhoKhiHoanTat(PhieuDoiTra phieu) {
        String sqlCongTon = "UPDATE dbo.SanPham SET soLuongTon = soLuongTon + ? WHERE maSanPham = ?";
        String sqlTruTon = "UPDATE dbo.SanPham SET soLuongTon = soLuongTon - ? WHERE maSanPham = ? AND soLuongTon >= ?";
        String sqlCapNhatHuong = "UPDATE dbo.ChiTietPhieuDoiTra SET huongXuLy = ? WHERE maPhieuDoiTra = ? AND maSanPham = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psCong = conn.prepareStatement(sqlCongTon);
                 PreparedStatement psTru = conn.prepareStatement(sqlTruTon);
                 PreparedStatement psHuong = conn.prepareStatement(sqlCapNhatHuong)) {

                for (ChiTietPhieuDoiTra ct : phieu.getDanhSachSanPham()) {
                    String huong = ct.getHuongXuLy() != null ? ct.getHuongXuLy().trim() : "";

                    if (!huong.isEmpty()) {
                        psHuong.setString(1, huong);
                        psHuong.setString(2, phieu.getMaPhieuDoiTra());
                        psHuong.setString(3, ct.getMaSanPham());
                        psHuong.executeUpdate();
                    }

                    boolean thuLaiVaoKho;
                    if (!huong.isEmpty()) {
                        thuLaiVaoKho = "Thu lại vào kho".equalsIgnoreCase(huong)
                                || "Nhập lại kho bán".equalsIgnoreCase(huong);
                    } else {
                        thuLaiVaoKho = "Nguyên vẹn".equalsIgnoreCase(ct.getTinhTrang());
                    }

                    if (thuLaiVaoKho) {
                        psCong.setInt(1, ct.getSoLuong());
                        psCong.setString(2, ct.getMaSanPham());
                        psCong.executeUpdate();
                    }

                    // Trừ tồn cho sản phẩm đổi mới nếu khách chọn đổi hàng
                    if (ct.getMaSanPhamDoi() != null && !ct.getMaSanPhamDoi().trim().isEmpty() && ct.getSoLuongDoi() > 0) {
                        psTru.setInt(1, ct.getSoLuongDoi());
                        psTru.setString(2, ct.getMaSanPhamDoi());
                        psTru.setInt(3, ct.getSoLuongDoi());
                        int soDongCapNhat = psTru.executeUpdate();
                        if (soDongCapNhat <= 0) {
                            conn.rollback();
                            throw new IllegalStateException("Sản phẩm đổi '" + ct.getMaSanPhamDoi() + "' không đủ số lượng tồn kho!");
                        }
                    }
                }

                // Cập nhật tiền hoàn/bù và ghi chú liên kết vào Hóa đơn gốc (nếu có cột)
                BigDecimal tienHoan = phieu.getTienHoan() != null ? phieu.getTienHoan() : BigDecimal.ZERO;
                BigDecimal tienBu = phieu.getTienBu() != null ? phieu.getTienBu() : BigDecimal.ZERO;
                String moTaDoiTra = "Đổi trả " + phieu.getMaPhieuDoiTra() + " (" + phieu.getLoaiXuLy() + "): "
                        + (tienHoan.compareTo(BigDecimal.ZERO) > 0 ? "Hoàn " + tienHoan + "đ" : "")
                        + (tienBu.compareTo(BigDecimal.ZERO) > 0 ? "Bù " + tienBu + "đ" : "");

                boolean coCotTienHoan = false;
                boolean coCotGhiChu = false;
                try (Statement stCheck = conn.createStatement();
                     ResultSet rsCheck = stCheck.executeQuery("SELECT name FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.HoaDon')")) {
                    while (rsCheck.next()) {
                        String tenCot = rsCheck.getString("name");
                        if ("tienHoanLai".equalsIgnoreCase(tenCot)) coCotTienHoan = true;
                        if ("ghiChu".equalsIgnoreCase(tenCot)) coCotGhiChu = true;
                    }
                } catch (Exception ignored) {}

                if (!coCotTienHoan) {
                    try (Statement stAlter = conn.createStatement()) {
                        stAlter.executeUpdate("""
                            IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.HoaDon') AND name = 'tienHoanLai')
                            BEGIN
                                ALTER TABLE dbo.HoaDon ADD tienHoanLai DECIMAL(18,2) NOT NULL DEFAULT 0;
                            END
                            IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.HoaDon') AND name = 'tienThanhToanThem')
                            BEGIN
                                ALTER TABLE dbo.HoaDon ADD tienThanhToanThem DECIMAL(18,2) NOT NULL DEFAULT 0;
                            END
                        """);
                        coCotTienHoan = true;
                    } catch (Exception ignored) {}
                }

                if (coCotTienHoan) {
                    String sqlHD = "UPDATE dbo.HoaDon SET tienHoanLai = ISNULL(tienHoanLai, 0) + ?, tienThanhToanThem = ISNULL(tienThanhToanThem, 0) + ? "
                            + (coCotGhiChu ? ", ghiChu = ISNULL(ghiChu + N'; ', N'') + ? " : " ")
                            + "WHERE maHoaDon = ?";
                    try (PreparedStatement psHD = conn.prepareStatement(sqlHD)) {
                        psHD.setBigDecimal(1, tienHoan);
                        psHD.setBigDecimal(2, tienBu);
                        if (coCotGhiChu) {
                            psHD.setString(3, moTaDoiTra);
                            psHD.setString(4, phieu.getMaHoaDon());
                        } else {
                            psHD.setString(3, phieu.getMaHoaDon());
                        }
                        psHD.executeUpdate();
                    }
                } else if (coCotGhiChu) {
                    String sqlHD = "UPDATE dbo.HoaDon SET ghiChu = ISNULL(ghiChu + N'; ', N'') + ? WHERE maHoaDon = ?";
                    try (PreparedStatement psHD = conn.prepareStatement(sqlHD)) {
                        psHD.setString(1, moTaDoiTra);
                        psHD.setString(2, phieu.getMaHoaDon());
                        psHD.executeUpdate();
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi cập nhật tồn kho khi hoàn tất đổi trả: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy toàn bộ danh sách phiếu đổi trả trong hệ thống.
     */
    public List<PhieuDoiTra> layTatCaPhieuDoiTra() {
        return timKiemPhieuDoiTra(null, null, null, null, null);
    }

    /**
     * Tìm kiếm và lọc danh sách phiếu đổi trả theo nhiều tiêu chí.
     */
    public List<PhieuDoiTra> timKiemPhieuDoiTra(String tuKhoa, LocalDate tuNgay, LocalDate denNgay,
                                                String trangThai, String loaiXuLy) {
        List<PhieuDoiTra> danhSach = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT p.maPhieuDoiTra, p.maHoaDon, p.maNhanVien, p.maKhachHang, p.ngayLap,
                       p.lyDo, p.soLuong, p.loaiXuLy, p.tienHoan, p.tienBu, p.trangThai,
                       nv.hoTen AS tenNhanVien,
                       kh.hoTen AS tenKhachHang, kh.soDienThoai AS soDienThoaiKhachHang
                FROM dbo.PhieuDoiTra p
                LEFT JOIN dbo.NhanVien nv ON p.maNhanVien = nv.maNhanVien
                LEFT JOIN dbo.KhachHang kh ON p.maKhachHang = kh.maKhachHang
                WHERE 1 = 1
                """);

        List<Object> thamSo = new ArrayList<>();

        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            String pattern = "%" + tuKhoa.trim() + "%";
            sql.append("""
                    AND (p.maPhieuDoiTra LIKE ?
                         OR p.maHoaDon LIKE ?
                         OR kh.hoTen LIKE ?
                         OR kh.soDienThoai LIKE ?
                         OR nv.hoTen LIKE ?)
                    """);
            for (int i = 0; i < 5; i++) {
                thamSo.add(pattern);
            }
        }

        if (tuNgay != null) {
            sql.append(" AND p.ngayLap >= ?");
            thamSo.add(Date.valueOf(tuNgay));
        }

        if (denNgay != null) {
            sql.append(" AND p.ngayLap <= ?");
            thamSo.add(Date.valueOf(denNgay));
        }

        if (trangThai != null && !trangThai.trim().isEmpty() && !"Tất cả".equalsIgnoreCase(trangThai.trim())) {
            if ("Đã duyệt".equalsIgnoreCase(trangThai.trim())) {
                sql.append(" AND p.trangThai IN (N'Đã duyệt', N'Hoàn thành')");
            } else {
                sql.append(" AND p.trangThai = ?");
                thamSo.add(trangThai.trim());
            }
        }

        if (loaiXuLy != null && !loaiXuLy.trim().isEmpty() && !"Tất cả".equalsIgnoreCase(loaiXuLy.trim())) {
            sql.append(" AND p.loaiXuLy = ?");
            thamSo.add(loaiXuLy.trim());
        }

        sql.append(" ORDER BY p.ngayLap DESC, p.maPhieuDoiTra DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < thamSo.size(); i++) {
                ps.setObject(i + 1, thamSo.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PhieuDoiTra phieu = new PhieuDoiTra();
                    phieu.setMaPhieuDoiTra(rs.getString("maPhieuDoiTra"));
                    phieu.setMaHoaDon(rs.getString("maHoaDon"));
                    phieu.setMaNhanVien(rs.getString("maNhanVien"));
                    phieu.setMaKhachHang(rs.getString("maKhachHang"));

                    Date ngay = rs.getDate("ngayLap");
                    if (ngay != null) phieu.setNgayLap(ngay.toLocalDate());

                    phieu.setLyDo(rs.getString("lyDo"));
                    phieu.setSoLuong(rs.getInt("soLuong"));
                    phieu.setLoaiXuLy(rs.getString("loaiXuLy"));
                    phieu.setTienHoan(rs.getBigDecimal("tienHoan"));
                    phieu.setTienBu(rs.getBigDecimal("tienBu"));
                    phieu.setTrangThai(rs.getString("trangThai"));

                    phieu.setTenNhanVien(rs.getString("tenNhanVien"));
                    phieu.setTenKhachHang(rs.getString("tenKhachHang"));
                    phieu.setSoDienThoaiKhachHang(rs.getString("soDienThoaiKhachHang"));

                    // Tải luôn danh sách chi tiết mặt hàng cho từng phiếu
                    napChiTietMatHang(conn, phieu);

                    danhSach.add(phieu);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi tìm kiếm phiếu đổi trả: " + e.getMessage(), e);
        }

        return danhSach;
    }

    /**
     * Lấy thông tin một phiếu đổi trả theo mã kèm danh sách mặt hàng chi tiết.
     */
    public PhieuDoiTra layPhieuDoiTraTheoMa(String maPhieuDoiTra) {
        String sql = """
                SELECT p.maPhieuDoiTra, p.maHoaDon, p.maNhanVien, p.maKhachHang, p.ngayLap,
                       p.lyDo, p.soLuong, p.loaiXuLy, p.tienHoan, p.tienBu, p.trangThai,
                       nv.hoTen AS tenNhanVien,
                       kh.hoTen AS tenKhachHang, kh.soDienThoai AS soDienThoaiKhachHang
                FROM dbo.PhieuDoiTra p
                LEFT JOIN dbo.NhanVien nv ON p.maNhanVien = nv.maNhanVien
                LEFT JOIN dbo.KhachHang kh ON p.maKhachHang = kh.maKhachHang
                WHERE p.maPhieuDoiTra = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maPhieuDoiTra);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PhieuDoiTra phieu = new PhieuDoiTra();
                    phieu.setMaPhieuDoiTra(rs.getString("maPhieuDoiTra"));
                    phieu.setMaHoaDon(rs.getString("maHoaDon"));
                    phieu.setMaNhanVien(rs.getString("maNhanVien"));
                    phieu.setMaKhachHang(rs.getString("maKhachHang"));

                    Date ngay = rs.getDate("ngayLap");
                    if (ngay != null) phieu.setNgayLap(ngay.toLocalDate());

                    phieu.setLyDo(rs.getString("lyDo"));
                    phieu.setSoLuong(rs.getInt("soLuong"));
                    phieu.setLoaiXuLy(rs.getString("loaiXuLy"));
                    phieu.setTienHoan(rs.getBigDecimal("tienHoan"));
                    phieu.setTienBu(rs.getBigDecimal("tienBu"));
                    phieu.setTrangThai(rs.getString("trangThai"));

                    phieu.setTenNhanVien(rs.getString("tenNhanVien"));
                    phieu.setTenKhachHang(rs.getString("tenKhachHang"));
                    phieu.setSoDienThoaiKhachHang(rs.getString("soDienThoaiKhachHang"));

                    napChiTietMatHang(conn, phieu);
                    return phieu;
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi lấy thông tin phiếu đổi trả " + maPhieuDoiTra + ": " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Tra cứu số lượng sản phẩm đã được đổi trả trước đó trong một hóa đơn.
     */
    public int laySoLuongDaDoiTra(String maHoaDon, String maSanPham) {
        String sql = """
                SELECT ISNULL(SUM(ct.soLuong), 0) AS tongDaTra
                FROM dbo.ChiTietPhieuDoiTra ct
                JOIN dbo.PhieuDoiTra p ON ct.maPhieuDoiTra = p.maPhieuDoiTra
                WHERE ct.maHoaDon = ? AND ct.maSanPham = ? AND p.trangThai != N'Từ chối'
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maHoaDon);
            ps.setString(2, maSanPham);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("tongDaTra");
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi tra cứu số lượng đã đổi trả: " + e.getMessage(), e);
        }

        return 0;
    }

    private void napChiTietMatHang(Connection conn, PhieuDoiTra phieu) throws SQLException {
        String sqlChiTiet = """
                SELECT ct.maPhieuDoiTra, ct.maHoaDon, ct.maSanPham, ct.soLuong, ct.donGia,
                       ct.lyDo, ct.tinhTrang, ct.huongXuLy, ct.ghiChu,
                       ct.maSanPhamDoi, ct.soLuongDoi,
                       COALESCE(sp.tenSanPham, ct.maSanPham) AS tenSanPham,
                       sp.donViTinh,
                       spDoi.tenSanPham AS tenSanPhamDoi, spDoi.giaBan AS donGiaDoi
                FROM dbo.ChiTietPhieuDoiTra ct
                LEFT JOIN dbo.SanPham sp ON ct.maSanPham = sp.maSanPham
                LEFT JOIN dbo.SanPham spDoi ON ct.maSanPhamDoi = spDoi.maSanPham
                WHERE ct.maPhieuDoiTra = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sqlChiTiet)) {
            ps.setString(1, phieu.getMaPhieuDoiTra());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChiTietPhieuDoiTra ct = new ChiTietPhieuDoiTra();
                    ct.setMaPhieuDoiTra(rs.getString("maPhieuDoiTra"));
                    ct.setMaHoaDon(rs.getString("maHoaDon"));
                    ct.setMaSanPham(rs.getString("maSanPham"));
                    ct.setTenSanPham(rs.getString("tenSanPham"));
                    ct.setDonViTinh(rs.getString("donViTinh"));
                    ct.setSoLuong(rs.getInt("soLuong"));
                    ct.setDonGia(rs.getBigDecimal("donGia"));

                    ct.setLyDo(rs.getString("lyDo"));
                    ct.setTinhTrang(rs.getString("tinhTrang"));
                    ct.setHuongXuLy(rs.getString("huongXuLy"));
                    ct.setGhiChu(rs.getString("ghiChu"));

                    ct.setMaSanPhamDoi(rs.getString("maSanPhamDoi"));
                    ct.setTenSanPhamDoi(rs.getString("tenSanPhamDoi"));
                    ct.setSoLuongDoi(rs.getInt("soLuongDoi"));
                    ct.setDonGiaDoi(rs.getBigDecimal("donGiaDoi"));

                    phieu.themChiTiet(ct);
                }
            }
        }
    }
}

