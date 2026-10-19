package com.petshop.dao;

import com.petshop.model.ReturnNote;
import com.petshop.model.ReturnNoteItem;
import com.petshop.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp ReturnNoteDao xử lý truy vấn dữ liệu từ cơ sở dữ liệu cho các bảng
 * PhieuDoiTra, ChiTietPhieuDoiTra, HoaDon và SanPham.
 *
 * Tiêu chuẩn tuân thủ:
 * - Tên lớp là danh từ (ReturnNoteDao), tên phương thức là động từ.
 * - Sử dụng 100% PreparedStatement và quản lý giao dịch (Transaction) an toàn.
 * - Có bình luận 1 dòng và nhiều dòng chuẩn Javadoc.
 */
public class ReturnNoteDao {

    private static final DateTimeFormatter DINH_DANG_NGAY_MA = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Khởi tạo DAO và đảm bảo cấu trúc bảng, ràng buộc cũng như dữ liệu mẫu sẵn sàng.
     */
    public ReturnNoteDao() {
        khoiTaoBangPhieuDoiTra();
    }

    /**
     * Tự động kiểm tra và nâng cấp bảng PhieuDoiTra, nới lỏng ràng buộc CHECK nếu cần thiết.
     */
    public void khoiTaoBangPhieuDoiTra() {
        try (Connection conn = DatabaseManager.getConnection()) {
            // 1. Đảm bảo có cột tongTienHoan trong bảng PhieuDoiTra
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("""
                    IF NOT EXISTS (
                        SELECT * FROM sys.columns 
                        WHERE object_id = OBJECT_ID('PhieuDoiTra') AND name = 'tongTienHoan'
                    )
                    BEGIN
                        ALTER TABLE PhieuDoiTra ADD tongTienHoan DECIMAL(18,2) DEFAULT 0;
                    END
                """);
            }

            // 2. Nới lỏng CHECK maHoaDon để nhận cả định dạng 3 số đuôi (HD-YYYYMMDD-NNN)
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("""
                    DECLARE @ckHD NVARCHAR(200);
                    SELECT @ckHD = name FROM sys.check_constraints 
                    WHERE parent_object_id = OBJECT_ID('HoaDon') AND definition LIKE '%maHoaDon%';
                    IF @ckHD IS NOT NULL EXEC('ALTER TABLE HoaDon DROP CONSTRAINT ' + @ckHD);
                    ALTER TABLE HoaDon ADD CONSTRAINT CK_HoaDon_MaHoaDon 
                        CHECK (maHoaDon LIKE 'HD-[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]-[0-9][0-9][0-9]%');
                """);
            } catch (Exception ignored) {}

            // 3. Nới lỏng CHECK maPhieuDoiTra để nhận cả định dạng 3 số đuôi (PDT-YYYYMMDD-NNN)
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("""
                    DECLARE @ckPDT NVARCHAR(200);
                    SELECT @ckPDT = name FROM sys.check_constraints 
                    WHERE parent_object_id = OBJECT_ID('PhieuDoiTra') AND definition LIKE '%maPhieuDoiTra%';
                    IF @ckPDT IS NOT NULL EXEC('ALTER TABLE PhieuDoiTra DROP CONSTRAINT ' + @ckPDT);
                    ALTER TABLE PhieuDoiTra ADD CONSTRAINT CK_PhieuDoiTra_MaPhieu 
                        CHECK (maPhieuDoiTra LIKE 'PDT-[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]-[0-9][0-9][0-9]%');
                """);
            } catch (Exception ignored) {}

            // 4. Mở rộng ràng buộc CHECK trangThai để chấp nhận "Đã duyệt", "Đã từ chối"
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("""
                    DECLARE @ckTT NVARCHAR(200);
                    SELECT @ckTT = name FROM sys.check_constraints 
                    WHERE parent_object_id = OBJECT_ID('PhieuDoiTra') AND definition LIKE '%trangThai%';
                    IF @ckTT IS NOT NULL EXEC('ALTER TABLE PhieuDoiTra DROP CONSTRAINT ' + @ckTT);
                    ALTER TABLE PhieuDoiTra ADD CONSTRAINT CK_PhieuDoiTra_TrangThai 
                        CHECK (trangThai IN (N'Chờ xử lý', N'Đã duyệt', N'Đã từ chối', N'Đã hoàn tiền', N'Từ chối'));
                """);
            } catch (Exception ignored) {}

        } catch (SQLException e) {
            System.err.println("[ReturnNoteDao] Lỗi khởi tạo bảng PhieuDoiTra: " + e.getMessage());
        }
    }

    /**
     * Lấy toàn bộ danh sách phiếu đổi trả kèm thông tin tên khách hàng.
     *
     * @return Danh sách các phiếu đổi trả
     */
    public List<ReturnNote> layTatCaPhieuDoiTra() {
        List<ReturnNote> danhSach = new ArrayList<>();
        String sql = """
                SELECT pdt.maPhieuDoiTra, pdt.maHoaDon, pdt.maNhanVien, pdt.maKhachHang,
                       kh.hoTen AS tenKhachHang, pdt.ngayLap, pdt.lyDo, pdt.soLuong,
                       pdt.trangThai, ISNULL(pdt.tongTienHoan, 0) AS tongTienHoan
                FROM PhieuDoiTra pdt
                LEFT JOIN KhachHang kh ON pdt.maKhachHang = kh.maKhachHang
                ORDER BY pdt.maPhieuDoiTra DESC
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                danhSach.add(chuyenDoiBanGhiSangDoiTuong(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi truy vấn toàn bộ phiếu đổi trả: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Lấy danh sách phiếu đổi trả lọc theo trạng thái ("Chờ xử lý", "Đã duyệt", "Đã từ chối").
     *
     * @param trangThai Trạng thái cần lọc (nếu null hoặc "Tất cả" thì lấy tất cả)
     * @return Danh sách các phiếu đổi trả thỏa điều kiện
     */
    public List<ReturnNote> layPhieuDoiTraTheoTrangThai(String trangThai) {
        if (trangThai == null || "Tất cả".equalsIgnoreCase(trangThai.trim())) {
            return layTatCaPhieuDoiTra();
        }

        List<ReturnNote> danhSach = new ArrayList<>();
        String sql = """
                SELECT pdt.maPhieuDoiTra, pdt.maHoaDon, pdt.maNhanVien, pdt.maKhachHang,
                       kh.hoTen AS tenKhachHang, pdt.ngayLap, pdt.lyDo, pdt.soLuong,
                       pdt.trangThai, ISNULL(pdt.tongTienHoan, 0) AS tongTienHoan
                FROM PhieuDoiTra pdt
                LEFT JOIN KhachHang kh ON pdt.maKhachHang = kh.maKhachHang
                WHERE pdt.trangThai = ?
                ORDER BY pdt.maPhieuDoiTra DESC
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, trangThai.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(chuyenDoiBanGhiSangDoiTuong(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi lọc phiếu đổi trả theo trạng thái: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Đếm số lượng phiếu đổi trả theo trạng thái cụ thể để hiển thị trên các thẻ Tab.
     *
     * @param trangThai Trạng thái cần đếm
     * @return Số lượng bản ghi
     */
    public int demSoLuongTheoTrangThai(String trangThai) {
        String sql = "SELECT COUNT(*) FROM PhieuDoiTra WHERE trangThai = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, trangThai);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ReturnNoteDao] Lỗi đếm số lượng: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Tìm kiếm phiếu đổi trả theo từ khóa (mã phiếu, mã hóa đơn, tên khách hàng hoặc lý do).
     *
     * @param tuKhoa    Từ khóa cần tìm
     * @param trangThai Trạng thái đang chọn để lọc (nếu có)
     * @return Danh sách kết quả phù hợp
     */
    public List<ReturnNote> timKiemTheoTuKhoa(String tuKhoa, String trangThai) {
        List<ReturnNote> ketQua = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT pdt.maPhieuDoiTra, pdt.maHoaDon, pdt.maNhanVien, pdt.maKhachHang,
                       kh.hoTen AS tenKhachHang, pdt.ngayLap, pdt.lyDo, pdt.soLuong,
                       pdt.trangThai, ISNULL(pdt.tongTienHoan, 0) AS tongTienHoan
                FROM PhieuDoiTra pdt
                LEFT JOIN KhachHang kh ON pdt.maKhachHang = kh.maKhachHang
                WHERE 1=1
                """);

        boolean coLocTrangThai = (trangThai != null && !trangThai.isBlank() && !"Tất cả".equalsIgnoreCase(trangThai));
        boolean coLocTuKhoa = (tuKhoa != null && !tuKhoa.trim().isEmpty());

        if (coLocTrangThai) {
            sql.append(" AND pdt.trangThai = ? ");
        }

        if (coLocTuKhoa) {
            sql.append("""
                    AND (LOWER(pdt.maPhieuDoiTra) LIKE LOWER(?) 
                      OR LOWER(pdt.maHoaDon) LIKE LOWER(?)
                      OR LOWER(kh.hoTen) LIKE LOWER(?)
                      OR LOWER(pdt.lyDo) LIKE LOWER(?))
                    """);
        }

        sql.append(" ORDER BY pdt.maPhieuDoiTra DESC ");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (coLocTrangThai) {
                ps.setString(paramIndex++, trangThai.trim());
            }

            if (coLocTuKhoa) {
                String pattern = "%" + tuKhoa.trim() + "%";
                ps.setString(paramIndex++, pattern);
                ps.setString(paramIndex++, pattern);
                ps.setString(paramIndex++, pattern);
                ps.setString(paramIndex++, pattern);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ketQua.add(chuyenDoiBanGhiSangDoiTuong(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi tìm kiếm phiếu đổi trả: " + e.getMessage(), e);
        }
        return ketQua;
    }

    /**
     * Lấy danh sách các dòng sản phẩm chi tiết của một phiếu đổi trả.
     *
     * @param maPhieuDoiTra Mã phiếu đổi trả
     * @return Danh sách ReturnNoteItem
     */
    public List<ReturnNoteItem> layChiTietPhieu(String maPhieuDoiTra) {
        List<ReturnNoteItem> items = new ArrayList<>();
        String sql = """
                SELECT ct.maPhieuDoiTra, ct.maHoaDon, ct.maSanPham,
                       sp.tenSanPham, ct.soLuong, ct.donGia
                FROM ChiTietPhieuDoiTra ct
                LEFT JOIN SanPham sp ON ct.maSanPham = sp.maSanPham
                WHERE ct.maPhieuDoiTra = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maPhieuDoiTra);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReturnNoteItem item = new ReturnNoteItem(
                            rs.getString("maPhieuDoiTra"),
                            rs.getString("maHoaDon"),
                            rs.getString("maSanPham"),
                            rs.getString("tenSanPham"),
                            rs.getInt("soLuong"),
                            rs.getDouble("donGia")
                    );
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi lấy chi tiết phiếu đổi trả: " + e.getMessage(), e);
        }
        return items;
    }

    /**
     * Thêm mới một phiếu đổi trả và danh sách chi tiết vào CSDL trong một Transaction duy nhất.
     *
     * @param phieu            Đối tượng ReturnNote chứa thông tin chung
     * @param danhSachChiTiet  Danh sách các sản phẩm đổi trả
     * @return true nếu thêm thành công
     */
    public boolean themPhieuDoiTra(ReturnNote phieu, List<ReturnNoteItem> danhSachChiTiet) {
        String sqlPhieu = """
                INSERT INTO PhieuDoiTra (maPhieuDoiTra, maHoaDon, maNhanVien, maKhachHang, ngayLap, lyDo, soLuong, trangThai, tongTienHoan)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        String sqlChiTiet = """
                INSERT INTO ChiTietPhieuDoiTra (maPhieuDoiTra, maHoaDon, maSanPham, soLuong, donGia)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false); // Bắt đầu transaction

            try (PreparedStatement psPhieu = conn.prepareStatement(sqlPhieu);
                 PreparedStatement psChiTiet = conn.prepareStatement(sqlChiTiet)) {

                // 1. Thêm bản ghi PhieuDoiTra
                psPhieu.setString(1, phieu.getMaPhieuDoiTra());
                psPhieu.setString(2, phieu.getMaHoaDon());
                psPhieu.setString(3, phieu.getMaNhanVien());
                psPhieu.setString(4, phieu.getMaKhachHang());
                psPhieu.setDate(5, Date.valueOf(phieu.getNgayLap() != null ? phieu.getNgayLap() : LocalDate.now()));
                psPhieu.setString(6, phieu.getLyDo());
                psPhieu.setInt(7, phieu.getSoLuong());
                psPhieu.setString(8, phieu.getTrangThai() != null ? phieu.getTrangThai() : "Chờ xử lý");
                psPhieu.setDouble(9, phieu.getTongTienHoan());
                psPhieu.executeUpdate();

                // 2. Thêm từng bản ghi ChiTietPhieuDoiTra
                if (danhSachChiTiet != null) {
                    for (ReturnNoteItem item : danhSachChiTiet) {
                        psChiTiet.setString(1, phieu.getMaPhieuDoiTra());
                        psChiTiet.setString(2, phieu.getMaHoaDon());
                        psChiTiet.setString(3, item.getMaSanPham());
                        psChiTiet.setInt(4, item.getSoLuong());
                        psChiTiet.setDouble(5, item.getDonGia());
                        psChiTiet.addBatch();
                    }
                    psChiTiet.executeBatch();
                }

                conn.commit(); // Ghi nhận toàn bộ thành công
                return true;
            } catch (SQLException e) {
                conn.rollback(); // Hoàn tác nếu có bất kỳ lỗi nào
                throw new IllegalStateException("Lỗi ghi nhận phiếu đổi trả vào CSDL: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi kết nối khi thêm phiếu đổi trả: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật trạng thái của phiếu đổi trả (ví dụ: "Đã duyệt", "Đã từ chối").
     *
     * @param maPhieuDoiTra Mã phiếu đổi trả
     * @param trangThaiMoi  Trạng thái mới cần cập nhật
     * @return true nếu cập nhật thành công
     */
    public boolean capNhatTrangThai(String maPhieuDoiTra, String trangThaiMoi) {
        String sql = "UPDATE PhieuDoiTra SET trangThai = ? WHERE maPhieuDoiTra = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, trangThaiMoi);
            ps.setString(2, maPhieuDoiTra);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi cập nhật trạng thái phiếu đổi trả: " + e.getMessage(), e);
        }
    }

    /**
     * Tự động sinh mã phiếu đổi trả tiếp theo theo ngày: PDT-YYYYMMDD-NNN.
     *
     * @return Chuỗi mã phiếu mới (ví dụ PDT-20261016-004)
     */
    public String sinhMaPhieuTuDong() {
        String dateStr = LocalDate.now().format(DINH_DANG_NGAY_MA);
        String prefix = "PDT-" + dateStr + "-";
        int maxSeq = 0;

        String sql = "SELECT maPhieuDoiTra FROM PhieuDoiTra WHERE maPhieuDoiTra LIKE ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ma = rs.getString("maPhieuDoiTra");
                    if (ma != null && ma.startsWith(prefix)) {
                        String suffix = ma.substring(prefix.length());
                        try {
                            int seq = Integer.parseInt(suffix);
                            if (seq > maxSeq) {
                                maxSeq = seq;
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[ReturnNoteDao] Lỗi sinh mã: " + e.getMessage());
        }

        return String.format("%s%03d", prefix, maxSeq + 1);
    }

    /**
     * Tra cứu thông tin hóa đơn và khách hàng theo mã hóa đơn.
     *
     * @param maHoaDon Mã hóa đơn cần tìm (HD-...)
     * @return Mảng Object[] gồm: [0]=maHoaDon, [1]=maKhachHang, [2]=tenKhachHang, [3]=ngayLap, [4]=tongTien
     */
    public Object[] timThongTinHoaDon(String maHoaDon) {
        if (maHoaDon == null || maHoaDon.trim().isEmpty()) {
            return null;
        }

        String sql = """
                SELECT hd.maHoaDon, hd.maKhachHang, kh.hoTen AS tenKhachHang,
                       hd.ngayLap, hd.tongTien
                FROM HoaDon hd
                LEFT JOIN KhachHang kh ON hd.maKhachHang = kh.maKhachHang
                WHERE hd.maHoaDon = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maHoaDon.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                            rs.getString("maHoaDon"),
                            rs.getString("maKhachHang"),
                            rs.getString("tenKhachHang"),
                            rs.getDate("ngayLap").toLocalDate(),
                            rs.getDouble("tongTien")
                    };
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi tra cứu thông tin hóa đơn: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Lấy danh sách các sản phẩm đã mua theo mã hóa đơn từ bảng ChiTietHoaDon_SanPham.
     *
     * @param maHoaDon Mã hóa đơn cần lấy danh sách hàng
     * @return Danh sách ReturnNoteItem tương ứng
     */
    public List<ReturnNoteItem> layDanhSachSanPhamTheoHoaDon(String maHoaDon) {
        List<ReturnNoteItem> danhSach = new ArrayList<>();
        if (maHoaDon == null || maHoaDon.trim().isEmpty()) {
            return danhSach;
        }

        String sql = """
                SELECT ct.maHoaDon, ct.maSanPham, sp.tenSanPham, ct.soLuong, ct.donGia
                FROM ChiTietHoaDon_SanPham ct
                LEFT JOIN SanPham sp ON ct.maSanPham = sp.maSanPham
                WHERE ct.maHoaDon = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maHoaDon.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReturnNoteItem item = new ReturnNoteItem(
                            null, // Chưa gắn mã phiếu
                            rs.getString("maHoaDon"),
                            rs.getString("maSanPham"),
                            rs.getString("tenSanPham"),
                            rs.getInt("soLuong"),
                            rs.getDouble("donGia")
                    );
                    danhSach.add(item);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi lấy danh sách sản phẩm từ hóa đơn: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /**
     * Chuyển đổi một dòng ResultSet sang đối tượng ReturnNote.
     *
     * @param rs ResultSet hiện tại
     * @return Đối tượng ReturnNote
     * @throws SQLException nếu có lỗi đọc dữ liệu
     */
    private ReturnNote chuyenDoiBanGhiSangDoiTuong(ResultSet rs) throws SQLException {
        String maPhieu = rs.getString("maPhieuDoiTra");
        String maHoaDon = rs.getString("maHoaDon");
        String maNV = rs.getString("maNhanVien");
        String maKH = rs.getString("maKhachHang");
        String tenKH = rs.getString("tenKhachHang");

        Date sqlNgayLap = rs.getDate("ngayLap");
        LocalDate ngayLap = (sqlNgayLap != null) ? sqlNgayLap.toLocalDate() : LocalDate.now();

        String lyDo = rs.getString("lyDo");
        int soLuong = rs.getInt("soLuong");
        String trangThai = rs.getString("trangThai");
        double tongTienHoan = rs.getDouble("tongTienHoan");

        return new ReturnNote(maPhieu, maHoaDon, maNV, maKH, tenKH, ngayLap, lyDo, soLuong, trangThai, tongTienHoan);
    }
}

