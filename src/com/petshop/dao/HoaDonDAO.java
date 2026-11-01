package com.petshop.dao;

import com.petshop.model.ChiTietHoaDonDichVu;
import com.petshop.model.ChiTietHoaDonSanPham;
import com.petshop.model.HoaDon;
import com.petshop.model.PhieuDichVu;
import com.petshop.util.DatabaseManager;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lớp DAO phụ trách thao tác cơ sở dữ liệu cho Hóa đơn (HoaDon) và Chi tiết hóa đơn.
 * Áp dụng JDBC Transaction quản trị nhất quán dữ liệu bán hàng, trừ tồn kho và cập nhật phiếu dịch vụ.
 * Tuân thủ Quy ước #60: không nuốt SQLException mà ném IllegalStateException.
 */
public class HoaDonDAO {

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Map<String, HoaDon> boNhoHoaDon = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Tự động sinh mã hóa đơn mới theo ngày hiện tại: HD-yyyyMMdd-xxxxxx.
     */
    public String taoMaHoaDon() {
        String prefix = "HD-" + LocalDate.now().format(DINH_DANG_NGAY) + "-";
        String sql = "SELECT TOP 1 maHoaDon FROM dbo.HoaDon WHERE maHoaDon LIKE ? ORDER BY maHoaDon DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String maCu = rs.getString("maHoaDon");
                    if (maCu != null && maCu.length() >= 17) {
                        try {
                            int soThuTu = Integer.parseInt(maCu.substring(maCu.lastIndexOf('-') + 1));
                            return prefix + String.format("%06d", soThuTu + 1);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi sinh mã hóa đơn mới: " + e.getMessage(), e);
        }

        return prefix + "000001";
    }

    /**
     * Lưu hóa đơn thanh toán vào cơ sở dữ liệu trong một Transaction toàn vẹn:
     * 1. Thêm bản ghi HoaDon
     * 2. Thêm các dòng ChiTietHoaDon_SanPham
     * 3. Trừ số lượng tồn kho tương ứng trong bảng SanPham (kiểm tra tồn kho đủ)
     * 4. Thêm các dòng ChiTietHoaDon_DichVu
     * 5. Nếu hóa đơn thanh toán cho Phiếu dịch vụ: Cập nhật trạng thái phiếu dịch vụ sang 'Hoàn thành'
     *
     * @param hoaDon Đối tượng hóa đơn chứa đầy đủ thông tin và danh sách chi tiết
     * @return true nếu lưu thành công
     */
    public boolean luuHoaDon(HoaDon hoaDon) {
        String sqlHoaDon = """
                INSERT INTO dbo.HoaDon (maHoaDon, maLichHen, maPhieuDichVu, maNhanVien, maKhachHang,
                                        ngayLap, hinhThucThanhToan, trangThaiThanhToan, maKhuyenMai, tongTien)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        String sqlCtSanPham = """
                INSERT INTO dbo.ChiTietHoaDon_SanPham (maHoaDon, maSanPham, soLuong, donGia)
                VALUES (?, ?, ?, ?)
                """;

        String sqlTruTonKho = """
                UPDATE dbo.SanPham
                SET soLuongTon = soLuongTon - ?
                WHERE maSanPham = ? AND soLuongTon >= ?
                """;

        String sqlCtDichVu = """
                INSERT INTO dbo.ChiTietHoaDon_DichVu (maHoaDon, maDichVu, soLuong, donGia)
                VALUES (?, ?, ?, ?)
                """;

        String sqlCapNhatPhieuDichVu = """
                UPDATE dbo.PhieuDichVu
                SET trangThai = N'Hoàn thành'
                WHERE maPhieuDichVu = ?
                """;

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            // 1. Thêm hóa đơn
            try (PreparedStatement psHd = conn.prepareStatement(sqlHoaDon)) {
                psHd.setString(1, hoaDon.getMaHoaDon());
                psHd.setString(2, hoaDon.getMaLichHen());
                psHd.setString(3, hoaDon.getMaPhieuDichVu());
                psHd.setString(4, hoaDon.getMaNhanVien());
                psHd.setString(5, hoaDon.getMaKhachHang());
                psHd.setDate(6, Date.valueOf(hoaDon.getNgayLap() != null ? hoaDon.getNgayLap() : LocalDate.now()));
                psHd.setString(7, hoaDon.getHinhThucThanhToan());
                psHd.setString(8, hoaDon.getTrangThaiThanhToan() != null ? hoaDon.getTrangThaiThanhToan() : "Đã thanh toán");
                psHd.setString(9, hoaDon.getMaKhuyenMai());
                psHd.setBigDecimal(10, hoaDon.getTongTien());
                psHd.executeUpdate();
            }

            // 2. Thêm chi tiết sản phẩm và trừ tồn kho
            if (hoaDon.getDanhSachSanPham() != null && !hoaDon.getDanhSachSanPham().isEmpty()) {
                try (PreparedStatement psCtSp = conn.prepareStatement(sqlCtSanPham);
                     PreparedStatement psTruTon = conn.prepareStatement(sqlTruTonKho)) {

                    for (ChiTietHoaDonSanPham sp : hoaDon.getDanhSachSanPham()) {
                        // Thêm chi tiết
                        psCtSp.setString(1, hoaDon.getMaHoaDon());
                        psCtSp.setString(2, sp.getMaSanPham());
                        psCtSp.setInt(3, sp.getSoLuong());
                        psCtSp.setBigDecimal(4, sp.getDonGia());
                        psCtSp.executeUpdate();

                        // Trừ tồn kho
                        psTruTon.setInt(1, sp.getSoLuong());
                        psTruTon.setString(2, sp.getMaSanPham());
                        psTruTon.setInt(3, sp.getSoLuong());
                        int soDongCapNhat = psTruTon.executeUpdate();
                        if (soDongCapNhat == 0) {
                            conn.rollback();
                            throw new IllegalStateException("Sản phẩm '" + (sp.getTenSanPham() != null ? sp.getTenSanPham() : sp.getMaSanPham())
                                    + "' không đủ số lượng tồn kho để xuất bán!");
                        }
                    }
                }
            }

            // 3. Thêm chi tiết dịch vụ
            if (hoaDon.getDanhSachDichVu() != null && !hoaDon.getDanhSachDichVu().isEmpty()) {
                try (PreparedStatement psCtDv = conn.prepareStatement(sqlCtDichVu)) {
                    for (ChiTietHoaDonDichVu dv : hoaDon.getDanhSachDichVu()) {
                        psCtDv.setString(1, hoaDon.getMaHoaDon());
                        psCtDv.setString(2, dv.getMaDichVu());
                        psCtDv.setInt(3, 1); // Ràng buộc soLuong = 1
                        psCtDv.setBigDecimal(4, dv.getDonGia());
                        psCtDv.executeUpdate();
                    }
                }
            }

            // 4. Nếu gắn với phiếu dịch vụ: chuyển trạng thái phiếu dịch vụ sang Hoàn thành
            if (hoaDon.getMaPhieuDichVu() != null && !hoaDon.getMaPhieuDichVu().trim().isEmpty()) {
                try (PreparedStatement psPdv = conn.prepareStatement(sqlCapNhatPhieuDichVu)) {
                    psPdv.setString(1, hoaDon.getMaPhieuDichVu().trim());
                    psPdv.executeUpdate();
                }
            }

            // 5. Cập nhật điểm tích lũy cho khách hàng (nếu không phải khách vãng lai)
            if (hoaDon.getMaKhachHang() != null && !hoaDon.getMaKhachHang().trim().isEmpty()) {
                int thayDoiDiem = hoaDon.getDiemTichLuyCong() - hoaDon.getDiemTichLuySuDung();
                if (thayDoiDiem != 0) {
                    String sqlDiem = """
                            UPDATE dbo.KhachHang
                            SET diemTichLuy = CASE WHEN diemTichLuy + ? < 0 THEN 0 ELSE diemTichLuy + ? END
                            WHERE maKhachHang = ?
                            """;
                    try (PreparedStatement psDiem = conn.prepareStatement(sqlDiem)) {
                        psDiem.setInt(1, thayDoiDiem);
                        psDiem.setInt(2, thayDoiDiem);
                        psDiem.setString(3, hoaDon.getMaKhachHang().trim());
                        psDiem.executeUpdate();
                    }
                }
            }

            conn.commit();
            boNhoHoaDon.put(hoaDon.getMaHoaDon(), hoaDon);
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new IllegalStateException("Lỗi khi lưu hóa đơn " + hoaDon.getMaHoaDon() + ": " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Hủy hóa đơn bán hàng và hoàn lại tồn kho cho các sản phẩm đã xuất trong Transaction.
     *
     * @param maHoaDon Mã hóa đơn cần hủy
     * @return true nếu hủy thành công
     */
    public boolean huyHoaDon(String maHoaDon) {
        String sqlKiemTra = "SELECT trangThaiThanhToan FROM dbo.HoaDon WHERE maHoaDon = ?";
        String sqlCapNhatHuy = "UPDATE dbo.HoaDon SET trangThaiThanhToan = N'Đã hủy' WHERE maHoaDon = ?";
        String sqlLaySanPham = "SELECT maSanPham, soLuong FROM dbo.ChiTietHoaDon_SanPham WHERE maHoaDon = ?";
        String sqlHoanTonKho = "UPDATE dbo.SanPham SET soLuongTon = soLuongTon + ? WHERE maSanPham = ?";

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            // Kiểm tra trạng thái hiện tại
            try (PreparedStatement psKiemTra = conn.prepareStatement(sqlKiemTra)) {
                psKiemTra.setString(1, maHoaDon);
                try (ResultSet rs = psKiemTra.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("Không tìm thấy hóa đơn mã: " + maHoaDon);
                    }
                    String trangThai = rs.getString("trangThaiThanhToan");
                    if ("Đã hủy".equalsIgnoreCase(trangThai)) {
                        throw new IllegalStateException("Hóa đơn này đã ở trạng thái 'Đã hủy', không thể hủy lại!");
                    }
                }
            }

            // 1. Cập nhật trạng thái hóa đơn sang Đã hủy
            try (PreparedStatement psHuy = conn.prepareStatement(sqlCapNhatHuy)) {
                psHuy.setString(1, maHoaDon);
                psHuy.executeUpdate();
            }

            // 2. Lấy danh sách sản phẩm và hoàn lại số lượng tồn kho
            List<ChiTietHoaDonSanPham> dsSp = new ArrayList<>();
            try (PreparedStatement psLaySp = conn.prepareStatement(sqlLaySanPham)) {
                psLaySp.setString(1, maHoaDon);
                try (ResultSet rsSp = psLaySp.executeQuery()) {
                    while (rsSp.next()) {
                        dsSp.add(new ChiTietHoaDonSanPham(maHoaDon, rsSp.getString("maSanPham"), rsSp.getInt("soLuong"), BigDecimal.ZERO));
                    }
                }
            }

            if (!dsSp.isEmpty()) {
                try (PreparedStatement psHoanTon = conn.prepareStatement(sqlHoanTonKho)) {
                    for (ChiTietHoaDonSanPham sp : dsSp) {
                        psHoanTon.setInt(1, sp.getSoLuong());
                        psHoanTon.setString(2, sp.getMaSanPham());
                        psHoanTon.executeUpdate();
                    }
                }
            }

            // 3. Hoàn lại điểm tích lũy cho khách hàng (nếu hóa đơn có khách hàng)
            String sqlLayKhach = "SELECT maKhachHang, tongTien FROM dbo.HoaDon WHERE maHoaDon = ?";
            try (PreparedStatement psKh = conn.prepareStatement(sqlLayKhach)) {
                psKh.setString(1, maHoaDon);
                try (ResultSet rsKh = psKh.executeQuery()) {
                    if (rsKh.next()) {
                        String maKh = rsKh.getString("maKhachHang");
                        BigDecimal tongTienHd = rsKh.getBigDecimal("tongTien");
                        if (maKh != null && !maKh.isBlank() && tongTienHd != null) {
                            long tien = tongTienHd.longValue();
                            int diemDaCong = (int) ((tien / 100_000L) * (tien >= 1_000_000L ? 10L : 5L));
                            if (diemDaCong > 0) {
                                String sqlTruDiem = """
                                        UPDATE dbo.KhachHang
                                        SET diemTichLuy = CASE WHEN diemTichLuy - ? < 0 THEN 0 ELSE diemTichLuy - ? END
                                        WHERE maKhachHang = ?
                                        """;
                                try (PreparedStatement psTru = conn.prepareStatement(sqlTruDiem)) {
                                    psTru.setInt(1, diemDaCong);
                                    psTru.setInt(2, diemDaCong);
                                    psTru.setString(3, maKh);
                                    psTru.executeUpdate();
                                }
                            }
                        }
                    }
                }
            }

            conn.commit();
            HoaDon cachedHuy = boNhoHoaDon.get(maHoaDon);
            if (cachedHuy != null) {
                cachedHuy.setTrangThaiThanhToan("Đã hủy");
            }
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new IllegalStateException("Lỗi khi hủy hóa đơn " + maHoaDon + ": " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Cập nhật lại thông tin mặt hàng, số lượng và tổng tiền của hóa đơn đã thanh toán.
     * Tự động hoàn lại hoặc trừ thêm số lượng tồn kho tương ứng của sản phẩm.
     * Cập nhật điểm tích lũy của khách hàng nếu tổng tiền thay đổi.
     * Toàn bộ thao tác thực thi trong một JDBC Transaction toàn vẹn.
     */
    public boolean capNhatHoaDon(HoaDon hoaDonCu, HoaDon hoaDonMoi, BigDecimal tienThanhToanThem, BigDecimal tienHoanLai) {
        String sqlXoaSanPham = "DELETE FROM dbo.ChiTietHoaDon_SanPham WHERE maHoaDon = ?";
        String sqlChenSanPham = """
                INSERT INTO dbo.ChiTietHoaDon_SanPham (maHoaDon, maSanPham, soLuong, donGia)
                VALUES (?, ?, ?, ?)
                """;
        String sqlTruTonKho = """
                UPDATE dbo.SanPham
                SET soLuongTon = soLuongTon - ?
                WHERE maSanPham = ? AND soLuongTon >= ?
                """;
        String sqlHoanTonKho = """
                UPDATE dbo.SanPham
                SET soLuongTon = soLuongTon + ?
                WHERE maSanPham = ?
                """;
        String sqlCapNhatHoaDon = """
                UPDATE dbo.HoaDon
                SET tongTien = ?, hinhThucThanhToan = ?
                WHERE maHoaDon = ?
                """;
        String sqlCapNhatDiem = """
                UPDATE dbo.KhachHang
                SET diemTichLuy = CASE WHEN diemTichLuy + ? < 0 THEN 0 ELSE diemTichLuy + ? END
                WHERE maKhachHang = ?
                """;

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            // 1. Tính toán chênh lệch số lượng từng sản phẩm để trừ thêm hoặc hoàn lại tồn kho
            Map<String, Integer> slCu = new HashMap<>();
            if (hoaDonCu.getDanhSachSanPham() != null) {
                for (ChiTietHoaDonSanPham sp : hoaDonCu.getDanhSachSanPham()) {
                    slCu.put(sp.getMaSanPham(), slCu.getOrDefault(sp.getMaSanPham(), 0) + sp.getSoLuong());
                }
            }

            Map<String, Integer> slMoi = new HashMap<>();
            if (hoaDonMoi.getDanhSachSanPham() != null) {
                for (ChiTietHoaDonSanPham sp : hoaDonMoi.getDanhSachSanPham()) {
                    slMoi.put(sp.getMaSanPham(), slMoi.getOrDefault(sp.getMaSanPham(), 0) + sp.getSoLuong());
                }
            }

            Set<String> tatCaMaSp = new HashSet<>();
            tatCaMaSp.addAll(slCu.keySet());
            tatCaMaSp.addAll(slMoi.keySet());

            try (PreparedStatement psTruTon = conn.prepareStatement(sqlTruTonKho);
                 PreparedStatement psHoanTon = conn.prepareStatement(sqlHoanTonKho)) {

                for (String maSp : tatCaMaSp) {
                    int c = slCu.getOrDefault(maSp, 0);
                    int m = slMoi.getOrDefault(maSp, 0);
                    int delta = m - c;

                    if (delta > 0) {
                        // Khách mua thêm mặt hàng này -> trừ thêm tồn kho
                        psTruTon.setInt(1, delta);
                        psTruTon.setString(2, maSp);
                        psTruTon.setInt(3, delta);
                        int capNhat = psTruTon.executeUpdate();
                        if (capNhat == 0) {
                            conn.rollback();
                            throw new IllegalStateException("Sản phẩm '" + maSp + "' không đủ số lượng tồn kho để tăng thêm " + delta + " cái!");
                        }
                    } else if (delta < 0) {
                        // Khách trả lại / bớt mặt hàng này -> hoàn lại tồn kho
                        int soLuongHoan = -delta;
                        psHoanTon.setInt(1, soLuongHoan);
                        psHoanTon.setString(2, maSp);
                        psHoanTon.executeUpdate();
                    }
                }
            }

            // 2. Xóa các dòng chi tiết sản phẩm cũ và chèn lại các dòng mới
            try (PreparedStatement psXoaSp = conn.prepareStatement(sqlXoaSanPham)) {
                psXoaSp.setString(1, hoaDonMoi.getMaHoaDon());
                psXoaSp.executeUpdate();
            }

            if (hoaDonMoi.getDanhSachSanPham() != null && !hoaDonMoi.getDanhSachSanPham().isEmpty()) {
                try (PreparedStatement psChenSp = conn.prepareStatement(sqlChenSanPham)) {
                    for (ChiTietHoaDonSanPham sp : hoaDonMoi.getDanhSachSanPham()) {
                        psChenSp.setString(1, hoaDonMoi.getMaHoaDon());
                        psChenSp.setString(2, sp.getMaSanPham());
                        psChenSp.setInt(3, sp.getSoLuong());
                        psChenSp.setBigDecimal(4, sp.getDonGia());
                        psChenSp.executeUpdate();
                    }
                }
            }

            // 3. Cập nhật tổng tiền hóa đơn trong CSDL
            try (PreparedStatement psHd = conn.prepareStatement(sqlCapNhatHoaDon)) {
                psHd.setBigDecimal(1, hoaDonMoi.getTongTien());
                psHd.setString(2, hoaDonMoi.getHinhThucThanhToan());
                psHd.setString(3, hoaDonMoi.getMaHoaDon());
                psHd.executeUpdate();
            }

            // 4. Cập nhật lại điểm tích lũy khách hàng nếu điểm tích lũy thay đổi
            if (hoaDonMoi.getMaKhachHang() != null && !hoaDonMoi.getMaKhachHang().trim().isEmpty()) {
                int thayDoiDiem = hoaDonMoi.getDiemTichLuyCong() - hoaDonCu.getDiemTichLuyCong();
                if (thayDoiDiem != 0) {
                    try (PreparedStatement psDiem = conn.prepareStatement(sqlCapNhatDiem)) {
                        psDiem.setInt(1, thayDoiDiem);
                        psDiem.setInt(2, thayDoiDiem);
                        psDiem.setString(3, hoaDonMoi.getMaKhachHang().trim());
                        psDiem.executeUpdate();
                    }
                }
            }

            conn.commit();

            // Cập nhật bộ nhớ đệm
            boNhoHoaDon.put(hoaDonMoi.getMaHoaDon(), hoaDonMoi);
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new IllegalStateException("Lỗi khi cập nhật hóa đơn " + hoaDonMoi.getMaHoaDon() + ": " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Lấy thông tin chi tiết một hóa đơn theo mã kèm đầy đủ danh sách mặt hàng và dịch vụ.
     */
    public HoaDon layHoaDonTheoMa(String maHoaDon) {
        String sql = """
                SELECT hd.maHoaDon, hd.maLichHen, hd.maPhieuDichVu, hd.maNhanVien, hd.maKhachHang,
                       hd.ngayLap, hd.hinhThucThanhToan, hd.trangThaiThanhToan, hd.maKhuyenMai, hd.tongTien,
                       nv.hoTen AS tenNhanVien,
                       kh.hoTen AS tenKhachHang, kh.soDienThoai AS soDienThoaiKhachHang,
                       km.tenChuongTrinh AS tenKhuyenMai, km.phanTramGiam,
                       (ISNULL((SELECT SUM(sp.soLuong * sp.donGia) FROM dbo.ChiTietHoaDon_SanPham sp WHERE sp.maHoaDon = hd.maHoaDon), 0)
                        + ISNULL((SELECT SUM(dv.soLuong * dv.donGia) FROM dbo.ChiTietHoaDon_DichVu dv WHERE dv.maHoaDon = hd.maHoaDon), 0)
                       ) AS tongTienGoc
                FROM dbo.HoaDon hd
                LEFT JOIN dbo.NhanVien nv ON hd.maNhanVien = nv.maNhanVien
                LEFT JOIN dbo.KhachHang kh ON hd.maKhachHang = kh.maKhachHang
                LEFT JOIN dbo.KhuyenMai km ON hd.maKhuyenMai = km.maKhuyenMai
                WHERE hd.maHoaDon = ?
                """;

        String sqlSanPham = """
                SELECT ct.maHoaDon, ct.maSanPham, ct.soLuong, ct.donGia,
                       sp.tenSanPham, sp.donViTinh
                FROM dbo.ChiTietHoaDon_SanPham ct
                JOIN dbo.SanPham sp ON ct.maSanPham = sp.maSanPham
                WHERE ct.maHoaDon = ?
                """;

        String sqlDichVu = """
                SELECT ct.maHoaDon, ct.maDichVu, ct.soLuong, ct.donGia,
                       dv.tenDichVu
                FROM dbo.ChiTietHoaDon_DichVu ct
                JOIN dbo.DichVuChamSoc dv ON ct.maDichVu = dv.maDichVu
                WHERE ct.maHoaDon = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    HoaDon hd = anhXaHoaDon(rs);

                    // Nạp danh sách sản phẩm
                    try (PreparedStatement psSp = conn.prepareStatement(sqlSanPham)) {
                        psSp.setString(1, maHoaDon);
                        try (ResultSet rsSp = psSp.executeQuery()) {
                            while (rsSp.next()) {
                                hd.getDanhSachSanPham().add(new ChiTietHoaDonSanPham(
                                        rsSp.getString("maHoaDon"),
                                        rsSp.getString("maSanPham"),
                                        rsSp.getString("tenSanPham"),
                                        rsSp.getString("donViTinh"),
                                        rsSp.getInt("soLuong"),
                                        rsSp.getBigDecimal("donGia")
                                ));
                            }
                        }
                    }

                    // Nạp danh sách dịch vụ
                    try (PreparedStatement psDv = conn.prepareStatement(sqlDichVu)) {
                        psDv.setString(1, maHoaDon);
                        try (ResultSet rsDv = psDv.executeQuery()) {
                            while (rsDv.next()) {
                                hd.getDanhSachDichVu().add(new ChiTietHoaDonDichVu(
                                        rsDv.getString("maHoaDon"),
                                        rsDv.getString("maDichVu"),
                                        rsDv.getString("tenDichVu"),
                                        null,
                                        null,
                                        rsDv.getBigDecimal("donGia")
                                ));
                            }
                        }
                    }

                    // Tính chi tiết tiền giảm KM và điểm tích lũy
                    HoaDon cached = boNhoHoaDon.get(maHoaDon);
                    BigDecimal tongGoc = hd.tinhTongTienGoc();
                    BigDecimal tongChenhLech = tongGoc.subtract(hd.getTongTien()).max(BigDecimal.ZERO);
                    if (tongChenhLech.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal tienGiamKm = BigDecimal.ZERO;
                        if (hd.getMaKhuyenMai() != null) {
                            BigDecimal phanTram = rs.getBigDecimal("phanTramGiam");
                            if (phanTram != null && phanTram.compareTo(BigDecimal.ZERO) > 0) {
                                tienGiamKm = tongGoc.multiply(phanTram).divide(BigDecimal.valueOf(100), 0, java.math.RoundingMode.HALF_UP);
                                if (tienGiamKm.compareTo(tongChenhLech) > 0) {
                                    tienGiamKm = tongChenhLech;
                                }
                            }
                        }
                        BigDecimal tienGiamDiem = tongChenhLech.subtract(tienGiamKm);
                        hd.setTienGiam(tienGiamKm);
                        if (tienGiamDiem.compareTo(BigDecimal.ZERO) > 0) {
                            int diemDung = tienGiamDiem.divide(BigDecimal.valueOf(1000), 0, java.math.RoundingMode.CEILING).intValue();
                            hd.setDiemTichLuySuDung(diemDung);
                        }
                    }

                    if (cached != null) {
                        if (cached.getTienGiam() != null && cached.getTienGiam().compareTo(BigDecimal.ZERO) > 0) {
                            hd.setTienGiam(cached.getTienGiam());
                        }
                        if (cached.getDiemTichLuySuDung() > 0) {
                            hd.setDiemTichLuySuDung(cached.getDiemTichLuySuDung());
                        }
                        hd.setDiemTichLuyCong(cached.getDiemTichLuyCong());
                        hd.setTienKhachDua(cached.getTienKhachDua());
                        hd.setTienThoiLai(cached.getTienThoiLai());
                    }
                    boNhoHoaDon.put(hd.getMaHoaDon(), hd);
                    return hd;
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi lấy hóa đơn " + maHoaDon + ": " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Tra cứu danh sách hóa đơn theo các bộ lọc linh hoạt.
     */
    public List<HoaDon> timKiemHoaDon(String tuKhoa, LocalDate tuNgay, LocalDate denNgay, String hinhThucThanhToan, String trangThai) {
        List<HoaDon> danhSach = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT hd.maHoaDon, hd.maLichHen, hd.maPhieuDichVu, hd.maNhanVien, hd.maKhachHang,
                       hd.ngayLap, hd.hinhThucThanhToan, hd.trangThaiThanhToan, hd.maKhuyenMai, hd.tongTien,
                       nv.hoTen AS tenNhanVien,
                       kh.hoTen AS tenKhachHang, kh.soDienThoai AS soDienThoaiKhachHang,
                       km.tenChuongTrinh AS tenKhuyenMai, km.phanTramGiam,
                       (ISNULL((SELECT SUM(sp.soLuong * sp.donGia) FROM dbo.ChiTietHoaDon_SanPham sp WHERE sp.maHoaDon = hd.maHoaDon), 0)
                        + ISNULL((SELECT SUM(dv.soLuong * dv.donGia) FROM dbo.ChiTietHoaDon_DichVu dv WHERE dv.maHoaDon = hd.maHoaDon), 0)
                       ) AS tongTienGoc
                FROM dbo.HoaDon hd
                LEFT JOIN dbo.NhanVien nv ON hd.maNhanVien = nv.maNhanVien
                LEFT JOIN dbo.KhachHang kh ON hd.maKhachHang = kh.maKhachHang
                LEFT JOIN dbo.KhuyenMai km ON hd.maKhuyenMai = km.maKhuyenMai
                WHERE 1=1
                """);

        List<Object> thamSo = new ArrayList<>();

        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            String pattern = "%" + tuKhoa.trim() + "%";
            sql.append("""
                    AND (hd.maHoaDon LIKE ?
                         OR kh.hoTen LIKE ?
                         OR kh.soDienThoai LIKE ?
                         OR nv.hoTen LIKE ?
                         OR hd.maPhieuDichVu LIKE ?)
                    """);
            for (int i = 0; i < 5; i++) thamSo.add(pattern);
        }

        if (tuNgay != null) {
            sql.append(" AND hd.ngayLap >= ?");
            thamSo.add(Date.valueOf(tuNgay));
        }

        if (denNgay != null) {
            sql.append(" AND hd.ngayLap <= ?");
            thamSo.add(Date.valueOf(denNgay));
        }

        if (hinhThucThanhToan != null && !hinhThucThanhToan.trim().isEmpty()
                && !"Tất cả".equalsIgnoreCase(hinhThucThanhToan.trim())
                && !"Tất cả hình thức".equalsIgnoreCase(hinhThucThanhToan.trim())
                && !hinhThucThanhToan.toLowerCase().contains("tất cả")
                && !hinhThucThanhToan.toLowerCase().contains("tat ca")) {
            sql.append(" AND hd.hinhThucThanhToan = ?");
            thamSo.add(hinhThucThanhToan.trim());
        }

        if (trangThai != null && !trangThai.trim().isEmpty()
                && !"Tất cả".equalsIgnoreCase(trangThai.trim())
                && !"Tất cả trạng thái".equalsIgnoreCase(trangThai.trim())
                && !trangThai.toLowerCase().contains("tất cả")
                && !trangThai.toLowerCase().contains("tat ca")) {
            sql.append(" AND hd.trangThaiThanhToan = ?");
            thamSo.add(trangThai.trim());
        }

        sql.append(" ORDER BY hd.ngayLap DESC, hd.maHoaDon DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < thamSo.size(); i++) {
                ps.setObject(i + 1, thamSo.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(anhXaHoaDon(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi tìm kiếm danh sách hóa đơn: " + e.getMessage(), e);
        }

        return danhSach;
    }

    /**
     * Lấy danh sách các Phiếu dịch vụ chưa thanh toán để nạp nhanh vào giao diện bán lẻ.
     */
    public List<PhieuDichVu> layDanhSachPhieuDichVuChuaThanhToan() {
        List<PhieuDichVu> danhSach = new ArrayList<>();
        String sql = """
                SELECT pdv.maPhieuDichVu, pdv.maKhachHang, pdv.maNhanVien, pdv.maLichHen,
                       pdv.ngayTiepNhan, pdv.gioTiepNhan, pdv.trangThai, pdv.ghiChu,
                       kh.hoTen AS tenKhachHang, kh.soDienThoai
                FROM dbo.PhieuDichVu pdv
                JOIN dbo.KhachHang kh ON pdv.maKhachHang = kh.maKhachHang
                WHERE pdv.trangThai != N'Đã hủy'
                  AND pdv.maPhieuDichVu NOT IN (
                      SELECT maPhieuDichVu FROM dbo.HoaDon
                      WHERE maPhieuDichVu IS NOT NULL AND trangThaiThanhToan != N'Đã hủy'
                  )
                ORDER BY pdv.ngayTiepNhan DESC, pdv.gioTiepNhan DESC
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                PhieuDichVu pdv = new PhieuDichVu();
                pdv.setMaPhieuDichVu(rs.getString("maPhieuDichVu"));
                pdv.setMaKhachHang(rs.getString("maKhachHang"));
                pdv.setMaNhanVien(rs.getString("maNhanVien"));
                pdv.setMaLichHen(rs.getString("maLichHen"));
                Date ngay = rs.getDate("ngayTiepNhan");
                if (ngay != null) pdv.setNgayTiepNhan(ngay.toLocalDate());
                Time gio = rs.getTime("gioTiepNhan");
                if (gio != null) pdv.setGioTiepNhan(gio.toLocalTime());
                pdv.setTrangThai(rs.getString("trangThai"));
                pdv.setGhiChu(rs.getString("ghiChu"));
                pdv.setTenKhachHang(rs.getString("tenKhachHang"));
                pdv.setSoDienThoai(rs.getString("soDienThoai"));
                danhSach.add(pdv);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi lấy danh sách phiếu dịch vụ chưa thanh toán: " + e.getMessage(), e);
        }

        return danhSach;
    }

    /**
     * Lấy danh sách phiếu dịch vụ trong ngày của khách hàng mà chưa được thanh toán.
     * Tự động xuất thông tin phiếu dịch vụ khi nhân viên nhập số điện thoại khách hàng.
     */
    public List<PhieuDichVu> layPhieuDichVuTrongNgayCuaKhachHang(String maKhachHang, LocalDate ngay) {
        List<PhieuDichVu> danhSach = new ArrayList<>();
        String sql = """
                SELECT pdv.maPhieuDichVu, pdv.maKhachHang, pdv.maNhanVien, pdv.maLichHen,
                       pdv.ngayTiepNhan, pdv.gioTiepNhan, pdv.trangThai, pdv.ghiChu,
                       kh.hoTen AS tenKhachHang, kh.soDienThoai
                FROM dbo.PhieuDichVu pdv
                JOIN dbo.KhachHang kh ON pdv.maKhachHang = kh.maKhachHang
                WHERE pdv.maKhachHang = ?
                  AND pdv.trangThai != N'Đã hủy'
                  AND (? IS NULL OR pdv.ngayTiepNhan = ?)
                  AND pdv.maPhieuDichVu NOT IN (
                      SELECT maPhieuDichVu FROM dbo.HoaDon
                      WHERE maPhieuDichVu IS NOT NULL AND trangThaiThanhToan != N'Đã hủy'
                  )
                ORDER BY pdv.ngayTiepNhan DESC, pdv.gioTiepNhan DESC
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maKhachHang);
            if (ngay != null) {
                ps.setDate(2, Date.valueOf(ngay));
                ps.setDate(3, Date.valueOf(ngay));
            } else {
                ps.setNull(2, Types.DATE);
                ps.setNull(3, Types.DATE);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PhieuDichVu pdv = new PhieuDichVu();
                    pdv.setMaPhieuDichVu(rs.getString("maPhieuDichVu"));
                    pdv.setMaKhachHang(rs.getString("maKhachHang"));
                    pdv.setMaNhanVien(rs.getString("maNhanVien"));
                    pdv.setMaLichHen(rs.getString("maLichHen"));
                    Date d = rs.getDate("ngayTiepNhan");
                    if (d != null) pdv.setNgayTiepNhan(d.toLocalDate());
                    Time gio = rs.getTime("gioTiepNhan");
                    if (gio != null) pdv.setGioTiepNhan(gio.toLocalTime());
                    pdv.setTrangThai(rs.getString("trangThai"));
                    pdv.setGhiChu(rs.getString("ghiChu"));
                    pdv.setTenKhachHang(rs.getString("tenKhachHang"));
                    pdv.setSoDienThoai(rs.getString("soDienThoai"));
                    danhSach.add(pdv);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi tìm phiếu dịch vụ của khách hàng: " + e.getMessage(), e);
        }

        return danhSach;
    }

    /**
     * Lấy tất cả hóa đơn hiện có trong cơ sở dữ liệu.
     */
    public List<HoaDon> layTatCaHoaDon() {
        return timKiemHoaDon("", null, null, "Tất cả", "Tất cả");
    }

    /**
     * Lấy báo cáo thống kê doanh thu theo khoảng thời gian.
     */
    public Map<String, Object> layBaoCaoDoanhThu(LocalDate tuNgay, LocalDate denNgay) {
        return layBaoCaoDoanhThu(tuNgay, denNgay, null);
    }

    public Map<String, Object> layBaoCaoDoanhThu(LocalDate tuNgay, LocalDate denNgay, String trangThaiLoc) {
        Map<String, Object> baoCao = new HashMap<>();
        boolean locTheoTrangThai = trangThaiLoc != null
                && !trangThaiLoc.isBlank()
                && !"Tất cả".equalsIgnoreCase(trangThaiLoc.trim())
                && !"Tất cả trạng thái".equalsIgnoreCase(trangThaiLoc.trim())
                && !trangThaiLoc.toLowerCase().contains("tất cả")
                && !trangThaiLoc.toLowerCase().contains("tat ca");
        String dieuKienTrangThai = locTheoTrangThai ? " AND hd.trangThaiThanhToan = ?" : "";
        String sql = """
                SELECT COUNT(*) AS tongSoHoaDon,
                       ISNULL(SUM(CASE WHEN trangThaiThanhToan = N'Đã thanh toán' THEN tongTien ELSE 0 END), 0) AS tongDoanhThu,
                       ISNULL(SUM(CASE WHEN trangThaiThanhToan = N'Đã thanh toán' AND hinhThucThanhToan = N'Tiền mặt' THEN tongTien ELSE 0 END), 0) AS doanhThuTienMat,
                       ISNULL(SUM(CASE WHEN trangThaiThanhToan = N'Đã thanh toán' AND hinhThucThanhToan = N'Chuyển khoản' THEN tongTien ELSE 0 END), 0) AS doanhThuChuyenKhoan,
                       ISNULL(SUM(CASE WHEN trangThaiThanhToan = N'Đã thanh toán' AND hinhThucThanhToan = N'POS' THEN tongTien ELSE 0 END), 0) AS doanhThuPos,
                       COUNT(CASE WHEN trangThaiThanhToan = N'Đã hủy' THEN 1 END) AS soHoaDonHuy,
                       COUNT(CASE WHEN trangThaiThanhToan = N'Đã thanh toán' THEN 1 END) AS soHoaDonThanhCong,
                       (SELECT ISNULL(SUM(sp.soLuong * sp.donGia), 0)
                        FROM dbo.ChiTietHoaDon_SanPham sp
                        JOIN dbo.HoaDon h ON sp.maHoaDon = h.maHoaDon
                        WHERE h.ngayLap >= ? AND h.ngayLap <= ? AND h.trangThaiThanhToan = N'Đã thanh toán'""" + (locTheoTrangThai ? " AND h.trangThaiThanhToan = ?" : "") + """
                        ) AS doanhThuSanPham,
                       (SELECT ISNULL(SUM(dv.soLuong * dv.donGia), 0)
                        FROM dbo.ChiTietHoaDon_DichVu dv
                        JOIN dbo.HoaDon h ON dv.maHoaDon = h.maHoaDon
                        WHERE h.ngayLap >= ? AND h.ngayLap <= ? AND h.trangThaiThanhToan = N'Đã thanh toán'""" + (locTheoTrangThai ? " AND h.trangThaiThanhToan = ?" : "") + """
                        ) AS doanhThuDichVu
                FROM dbo.HoaDon
                WHERE ngayLap >= ? AND ngayLap <= ?
                """ + dieuKienTrangThai.replace("hd.", "");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            Date dTu = Date.valueOf(tuNgay != null ? tuNgay : LocalDate.now().minusMonths(1));
            Date dDen = Date.valueOf(denNgay != null ? denNgay : LocalDate.now());

            int thamSo = 1;
            ps.setDate(thamSo++, dTu);
            ps.setDate(thamSo++, dDen);
            if (locTheoTrangThai) ps.setString(thamSo++, trangThaiLoc);
            ps.setDate(thamSo++, dTu);
            ps.setDate(thamSo++, dDen);
            if (locTheoTrangThai) ps.setString(thamSo++, trangThaiLoc);
            ps.setDate(thamSo++, dTu);
            ps.setDate(thamSo++, dDen);
            if (locTheoTrangThai) ps.setString(thamSo++, trangThaiLoc);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int tongSoHd = rs.getInt("tongSoHoaDon");
                    int soHuy = rs.getInt("soHoaDonHuy");
                    int soTc = rs.getInt("soHoaDonThanhCong");

                    BigDecimal tongDt = rs.getBigDecimal("tongDoanhThu");
                    if (tongDt == null) tongDt = BigDecimal.ZERO;

                    BigDecimal dtTm = rs.getBigDecimal("doanhThuTienMat");
                    if (dtTm == null) dtTm = BigDecimal.ZERO;

                    BigDecimal dtCk = rs.getBigDecimal("doanhThuChuyenKhoan");
                    if (dtCk == null) dtCk = BigDecimal.ZERO;

                    BigDecimal dtPos = rs.getBigDecimal("doanhThuPos");
                    if (dtPos == null) dtPos = BigDecimal.ZERO;

                    BigDecimal dtSp = rs.getBigDecimal("doanhThuSanPham");
                    if (dtSp == null) dtSp = BigDecimal.ZERO;

                    BigDecimal dtDv = rs.getBigDecimal("doanhThuDichVu");
                    if (dtDv == null) dtDv = BigDecimal.ZERO;

                    // Tổng tiền giảm giá = Tổng tiền hàng gốc (Sản phẩm + Dịch vụ) - Tổng thực thu (sau giảm giá)
                    BigDecimal tongGoc = dtSp.add(dtDv);
                    BigDecimal tongGiamGia = tongGoc.subtract(tongDt).max(BigDecimal.ZERO);

                    BigDecimal aov = soTc > 0
                            ? tongDt.divide(BigDecimal.valueOf(soTc), 0, java.math.RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    // Tính tổng tiền hoàn đổi trả và bù đổi trả để đồng bộ doanh thu thực thu
                    BigDecimal tongHoanDt = BigDecimal.ZERO;
                    BigDecimal tongBuDt = BigDecimal.ZERO;
                    try (Statement stDt = conn.createStatement();
                         ResultSet rsDt = stDt.executeQuery("SELECT ISNULL(SUM(tienHoan), 0) AS tongHoan, ISNULL(SUM(tienBu), 0) AS tongBu " +
                                 "FROM dbo.PhieuDoiTra WHERE trangThai IN (N'Đã duyệt', N'Hoàn thành') " +
                                 "AND ngayLap >= '" + dTu + "' AND ngayLap <= '" + dDen + "'")) {
                        if (rsDt.next()) {
                            tongHoanDt = rsDt.getBigDecimal("tongHoan");
                            tongBuDt = rsDt.getBigDecimal("tongBu");
                        }
                    } catch (Exception ignored) {}

                    // Doanh thu thực thu sau khi đã trừ tiền hoàn lại cho khách
                    BigDecimal dtThucThuSauDoiTra = tongDt.subtract(tongHoanDt).add(tongBuDt);
                    if (dtThucThuSauDoiTra.compareTo(BigDecimal.ZERO) < 0) {
                        dtThucThuSauDoiTra = BigDecimal.ZERO;
                    }

                    baoCao.put("tongSoHoaDon", tongSoHd);
                    baoCao.put("tongDoanhThu", dtThucThuSauDoiTra);
                    baoCao.put("tongDoanhThuGoc", tongDt);
                    baoCao.put("tongTienHoanDoiTra", tongHoanDt);
                    baoCao.put("tongTienBuDoiTra", tongBuDt);
                    baoCao.put("doanhThuTienMat", dtTm);
                    baoCao.put("doanhThuChuyenKhoan", dtCk);
                    baoCao.put("doanhThuPos", dtPos);
                    baoCao.put("soHoaDonHuy", soHuy);
                    baoCao.put("soHoaDonThanhCong", soTc);
                    baoCao.put("doanhThuSanPham", dtSp);
                    baoCao.put("doanhThuDichVu", dtDv);
                    baoCao.put("tongGiamGia", tongGiamGia);
                    baoCao.put("giaTriTrungBinhDon", aov);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi tổng hợp báo cáo doanh thu: " + e.getMessage(), e);
        }

        return baoCao;
    }

    private HoaDon anhXaHoaDon(ResultSet rs) throws SQLException {
        HoaDon hd = new HoaDon();
        hd.setMaHoaDon(rs.getString("maHoaDon"));
        hd.setMaLichHen(rs.getString("maLichHen"));
        hd.setMaPhieuDichVu(rs.getString("maPhieuDichVu"));
        hd.setMaNhanVien(rs.getString("maNhanVien"));
        hd.setMaKhachHang(rs.getString("maKhachHang"));

        Date ngay = rs.getDate("ngayLap");
        if (ngay != null) hd.setNgayLap(ngay.toLocalDate());

        hd.setHinhThucThanhToan(rs.getString("hinhThucThanhToan"));
        hd.setTrangThaiThanhToan(rs.getString("trangThaiThanhToan"));
        hd.setMaKhuyenMai(rs.getString("maKhuyenMai"));
        hd.setTongTien(rs.getBigDecimal("tongTien"));

        hd.setTenNhanVien(rs.getString("tenNhanVien"));
        hd.setTenKhachHang(rs.getString("tenKhachHang"));
        hd.setSoDienThoaiKhachHang(rs.getString("soDienThoaiKhachHang"));
        hd.setTenKhuyenMai(rs.getString("tenKhuyenMai"));

        HoaDon cached = boNhoHoaDon.get(hd.getMaHoaDon());
        if (cached != null) {
            hd.setTienGiam(cached.getTienGiam());
            hd.setDiemTichLuySuDung(cached.getDiemTichLuySuDung());
            hd.setDiemTichLuyCong(cached.getDiemTichLuyCong());
            hd.setTienKhachDua(cached.getTienKhachDua());
            hd.setTienThoiLai(cached.getTienThoiLai());
        }

        try {
            BigDecimal tongGoc = rs.getBigDecimal("tongTienGoc");
            if (tongGoc != null) {
                hd.setTongTienGoc(tongGoc);
                // Nếu chưa có tiền giảm (hoặc bằng 0), khôi phục từ tổng tiền gốc và tổng tiền thực tế
                if (hd.getTongTienGiam().compareTo(BigDecimal.ZERO) == 0) {
                    BigDecimal tongTien = hd.getTongTien() != null ? hd.getTongTien() : BigDecimal.ZERO;
                    BigDecimal tongChenhLech = tongGoc.subtract(tongTien).max(BigDecimal.ZERO);
                    if (tongChenhLech.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal tienGiamKm = BigDecimal.ZERO;
                        if (hd.getMaKhuyenMai() != null) {
                            try {
                                BigDecimal phanTram = rs.getBigDecimal("phanTramGiam");
                                if (phanTram != null && phanTram.compareTo(BigDecimal.ZERO) > 0) {
                                    tienGiamKm = tongGoc.multiply(phanTram).divide(BigDecimal.valueOf(100), 0, java.math.RoundingMode.HALF_UP);
                                    if (tienGiamKm.compareTo(tongChenhLech) > 0) {
                                        tienGiamKm = tongChenhLech;
                                    }
                                }
                            } catch (SQLException ignored) {}
                        }
                        BigDecimal tienGiamDiem = tongChenhLech.subtract(tienGiamKm);
                        hd.setTienGiam(tienGiamKm);
                        if (tienGiamDiem.compareTo(BigDecimal.ZERO) > 0) {
                            int diemDung = (int) Math.ceil(tienGiamDiem.doubleValue() / 1000.0);
                            hd.setDiemTichLuySuDung(diemDung);
                            hd.setTienGiamTuDiem(tienGiamDiem);
                        }
                    }
                }
            }
        } catch (SQLException ignored) {
        }

        return hd;
    }

    /**
     * Lấy báo cáo thống kê các sản phẩm đã bán trong khoảng thời gian.
     */
    public List<Map<String, Object>> layBaoCaoSanPhamBanRa(LocalDate tuNgay, LocalDate denNgay) {
        List<Map<String, Object>> danhSach = new ArrayList<>();
        String sql = """
                SELECT ct.maSanPham, sp.tenSanPham, sp.donViTinh,
                       SUM(ct.soLuong) AS tongSoLuong,
                       SUM(ct.soLuong * ct.donGia) AS tongDoanhThu
                FROM dbo.ChiTietHoaDon_SanPham ct
                JOIN dbo.HoaDon hd ON ct.maHoaDon = hd.maHoaDon
                JOIN dbo.SanPham sp ON ct.maSanPham = sp.maSanPham
                WHERE hd.ngayLap >= ? AND hd.ngayLap <= ? AND hd.trangThaiThanhToan = N'Đã thanh toán'
                GROUP BY ct.maSanPham, sp.tenSanPham, sp.donViTinh
                ORDER BY tongSoLuong DESC
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            Date dTu = Date.valueOf(tuNgay != null ? tuNgay : LocalDate.now().minusMonths(1));
            Date dDen = Date.valueOf(denNgay != null ? denNgay : LocalDate.now());
            ps.setDate(1, dTu);
            ps.setDate(2, dDen);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("maSanPham", rs.getString("maSanPham"));
                    map.put("tenSanPham", rs.getString("tenSanPham"));
                    map.put("donViTinh", rs.getString("donViTinh"));
                    map.put("soLuong", rs.getInt("tongSoLuong"));
                    map.put("doanhThu", rs.getBigDecimal("tongDoanhThu"));
                    danhSach.add(map);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi lấy báo cáo sản phẩm bán ra: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Lấy báo cáo thống kê các dịch vụ đã thực hiện trong khoảng thời gian.
     */
    public List<Map<String, Object>> layBaoCaoDichVuThucHien(LocalDate tuNgay, LocalDate denNgay) {
        List<Map<String, Object>> danhSach = new ArrayList<>();
        String sql = """
                SELECT dv.maDichVu, d.tenDichVu,
                       COUNT(dv.maDichVu) AS tongLuot,
                       SUM(dv.soLuong * dv.donGia) AS tongDoanhThu
                FROM dbo.ChiTietHoaDon_DichVu dv
                JOIN dbo.HoaDon hd ON dv.maHoaDon = hd.maHoaDon
                JOIN dbo.DichVuChamSoc d ON dv.maDichVu = d.maDichVu
                WHERE hd.ngayLap >= ? AND hd.ngayLap <= ? AND hd.trangThaiThanhToan = N'Đã thanh toán'
                GROUP BY dv.maDichVu, d.tenDichVu
                ORDER BY tongLuot DESC
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            Date dTu = Date.valueOf(tuNgay != null ? tuNgay : LocalDate.now().minusMonths(1));
            Date dDen = Date.valueOf(denNgay != null ? denNgay : LocalDate.now());
            ps.setDate(1, dTu);
            ps.setDate(2, dDen);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("maDichVu", rs.getString("maDichVu"));
                    map.put("tenDichVu", rs.getString("tenDichVu"));
                    map.put("soLuong", rs.getInt("tongLuot"));
                    map.put("doanhThu", rs.getBigDecimal("tongDoanhThu"));
                    danhSach.add(map);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi lấy báo cáo dịch vụ thực hiện: " + e.getMessage(), e);
        }
        return danhSach;
    }
}
