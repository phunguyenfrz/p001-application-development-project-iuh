package com.petshop.dao;

import com.petshop.entity.ChiTietPhieuDoiTra;
import com.petshop.entity.PhieuDoiTra;
import com.petshop.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp truy xuất cơ sở dữ liệu cho Phiếu đổi trả (PhieuDoiTraDAO).
 * Tuân thủ Bộ chỉ thị kỹ thuật IUH:
 * - 100% tiền tệ dùng kiểu nguyên thủy double (0% BigDecimal).
 * - Không tự ý chạy DDL runtime (ALTER TABLE); toàn bộ cấu trúc bảng nằm trong script SQL.
 * - Các nghiệp vụ tác động nhiều bảng (thêm phiếu, cập nhật phiếu, duyệt phiếu hoàn tồn kho & trừ điểm tích lũy)
 *   BẮT BUỘC sử dụng JDBC Transaction (setAutoCommit(false), commit(), rollback()).
 */
public class PhieuDoiTraDAO {

    public PhieuDoiTraDAO() {
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
     * Lưu phiếu đổi trả mới và danh sách chi tiết mặt hàng vào database (Transaction).
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

                psPhieu.setString(1, phieu.getMaPhieuDoiTra());
                psPhieu.setString(2, phieu.getMaHoaDon());
                psPhieu.setString(3, phieu.getMaNhanVien());
                if (phieu.getMaKhachHang() != null && !phieu.getMaKhachHang().trim().isEmpty()) {
                    psPhieu.setString(4, phieu.getMaKhachHang().trim());
                } else {
                    psPhieu.setNull(4, Types.VARCHAR);
                }
                psPhieu.setDate(5, Date.valueOf(phieu.getNgayLap() != null ? phieu.getNgayLap() : LocalDate.now()));
                String lyDoPhieu = (phieu.getLyDo() != null && !phieu.getLyDo().trim().isEmpty())
                        ? phieu.getLyDo().trim()
                        : "Đổi trả sản phẩm";
                psPhieu.setString(6, lyDoPhieu);
                psPhieu.setInt(7, phieu.getSoLuong());
                psPhieu.setString(8, phieu.getLoaiXuLy() != null ? phieu.getLoaiXuLy() : "Hoàn tiền");
                psPhieu.setDouble(9, phieu.tinhTienHoan());
                psPhieu.setDouble(10, phieu.tinhTienBu());
                psPhieu.setString(11, phieu.getTrangThai() != null ? phieu.getTrangThai() : "Chờ xử lý");
                psPhieu.executeUpdate();

                for (ChiTietPhieuDoiTra ct : phieu.getDanhSachSanPham()) {
                    psChiTiet.setString(1, phieu.getMaPhieuDoiTra());
                    psChiTiet.setString(2, phieu.getMaHoaDon());
                    psChiTiet.setString(3, ct.getMaSanPham());
                    psChiTiet.setInt(4, ct.getSoLuong());
                    psChiTiet.setDouble(5, ct.getDonGia());
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
     * Cập nhật thông tin phiếu đổi trả và danh sách chi tiết (Transaction, chỉ khi Chờ xử lý).
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

                String lyDoPhieu = (phieu.getLyDo() != null && !phieu.getLyDo().trim().isEmpty())
                        ? phieu.getLyDo().trim()
                        : "Đổi trả sản phẩm";
                psPhieu.setString(1, lyDoPhieu);
                psPhieu.setInt(2, phieu.getSoLuong());
                psPhieu.setString(3, phieu.getLoaiXuLy() != null ? phieu.getLoaiXuLy() : "Hoàn tiền");
                psPhieu.setDouble(4, phieu.tinhTienHoan());
                psPhieu.setDouble(5, phieu.tinhTienBu());
                psPhieu.setString(6, phieu.getMaPhieuDoiTra());
                int soDong = psPhieu.executeUpdate();
                if (soDong <= 0) {
                    conn.rollback();
                    throw new IllegalStateException("Phiếu đã được duyệt/xác nhận, không thể chỉnh sửa!");
                }

                psXoa.setString(1, phieu.getMaPhieuDoiTra());
                psXoa.executeUpdate();

                for (ChiTietPhieuDoiTra ct : phieu.getDanhSachSanPham()) {
                    psChiTiet.setString(1, phieu.getMaPhieuDoiTra());
                    psChiTiet.setString(2, phieu.getMaHoaDon());
                    psChiTiet.setString(3, ct.getMaSanPham());
                    psChiTiet.setInt(4, ct.getSoLuong());
                    psChiTiet.setDouble(5, ct.getDonGia());
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
     * Duyệt phiếu đổi trả và đồng bộ dữ liệu trong cùng một Transaction:
     * 1. Tự động hoàn tồn kho (soLuongTon + ?) đối với sản phẩm thu lại vào kho.
     * 2. Trừ tồn kho (soLuongTon - ?) đối với sản phẩm đổi mới.
     * 3. Tự động trừ điểm tích lũy của khách hàng tương ứng với giá trị hoàn trả.
     * 4. Cập nhật trạng thái phiếu sang "Đã duyệt" và đồng bộ tiền hoàn/bù vào hóa đơn gốc.
     */
    public boolean capNhatTonKhoKhiHoanTat(PhieuDoiTra phieu, int soDiemTruKhachHang) {
        String sqlCongTon = "UPDATE dbo.SanPham SET soLuongTon = soLuongTon + ? WHERE maSanPham = ?";
        String sqlTruTon = "UPDATE dbo.SanPham SET soLuongTon = soLuongTon - ? WHERE maSanPham = ? AND soLuongTon >= ?";
        String sqlCapNhatHuong = "UPDATE dbo.ChiTietPhieuDoiTra SET huongXuLy = ? WHERE maPhieuDoiTra = ? AND maSanPham = ?";
        String sqlCapNhatPhieu = "UPDATE dbo.PhieuDoiTra SET tienHoan = ?, tienBu = ?, trangThai = N'Đã duyệt' WHERE maPhieuDoiTra = ?";
        String sqlTruDiemKhach = """
                UPDATE dbo.KhachHang
                SET diemTichLuy = CASE WHEN ISNULL(diemTichLuy, 0) >= ? THEN ISNULL(diemTichLuy, 0) - ? ELSE 0 END
                WHERE maKhachHang = ?
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psCong = conn.prepareStatement(sqlCongTon);
                 PreparedStatement psTru = conn.prepareStatement(sqlTruTon);
                 PreparedStatement psHuong = conn.prepareStatement(sqlCapNhatHuong);
                 PreparedStatement psPhieu = conn.prepareStatement(sqlCapNhatPhieu);
                 PreparedStatement psTruDiem = conn.prepareStatement(sqlTruDiemKhach)) {

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

                    if (ct.getMaSanPhamDoi() != null && !ct.getMaSanPhamDoi().trim().isEmpty() && ct.getSoLuongDoi() > 0) {
                        psTru.setInt(1, ct.getSoLuongDoi());
                        psTru.setString(2, ct.getMaSanPhamDoi());
                        psTru.setInt(3, ct.getSoLuongDoi());
                        int soDongCapNhat = psTru.executeUpdate();
                        if (soDongCapNhat <= 0) {
                            conn.rollback();
                            throw new IllegalArgumentException("Sản phẩm đổi '" + ct.getMaSanPhamDoi() + "' không đủ số lượng tồn kho!");
                        }
                    }
                }

                // Tự động trừ điểm tích lũy của khách hàng khi duyệt đổi trả
                if (soDiemTruKhachHang > 0 && phieu.getMaKhachHang() != null && !phieu.getMaKhachHang().isBlank()) {
                    psTruDiem.setInt(1, soDiemTruKhachHang);
                    psTruDiem.setInt(2, soDiemTruKhachHang);
                    psTruDiem.setString(3, phieu.getMaKhachHang().trim());
                    psTruDiem.executeUpdate();
                }

                double tienHoan = phieu.tinhTienHoan();
                double tienBu = phieu.tinhTienBu();
                psPhieu.setDouble(1, tienHoan);
                psPhieu.setDouble(2, tienBu);
                psPhieu.setString(3, phieu.getMaPhieuDoiTra());
                psPhieu.executeUpdate();

                String moTaDoiTra = "Đổi trả " + phieu.getMaPhieuDoiTra() + " (" + phieu.getLoaiXuLy() + "): "
                        + (tienHoan > 0 ? "Hoàn " + (long) tienHoan + "đ" : "")
                        + (tienBu > 0 ? "Bù " + (long) tienBu + "đ" : "");

                String sqlHD = """
                        UPDATE dbo.HoaDon
                        SET tienHoanLai = ISNULL(tienHoanLai, 0) + ?,
                            tienThanhToanThem = ISNULL(tienThanhToanThem, 0) + ?,
                            ghiChu = ISNULL(ghiChu + N'; ', N'') + ?
                        WHERE maHoaDon = ?
                        """;
                try (PreparedStatement psHD = conn.prepareStatement(sqlHD)) {
                    psHD.setDouble(1, tienHoan);
                    psHD.setDouble(2, tienBu);
                    psHD.setString(3, moTaDoiTra);
                    psHD.setString(4, phieu.getMaHoaDon());
                    psHD.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi cập nhật tồn kho và điểm tích lũy khi duyệt đổi trả: " + e.getMessage(), e);
        }
    }

    public boolean capNhatTonKhoKhiHoanTat(PhieuDoiTra phieu) {
        return capNhatTonKhoKhiHoanTat(phieu, 0);
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
                    phieu.setTrangThai(rs.getString("trangThai"));

                    phieu.setTenNhanVien(rs.getString("tenNhanVien"));
                    phieu.setTenKhachHang(rs.getString("tenKhachHang"));
                    phieu.setSoDienThoaiKhachHang(rs.getString("soDienThoaiKhachHang"));

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
                    ct.setDonGia(rs.getDouble("donGia"));

                    ct.setLyDo(rs.getString("lyDo"));
                    ct.setTinhTrang(rs.getString("tinhTrang"));
                    ct.setHuongXuLy(rs.getString("huongXuLy"));
                    ct.setGhiChu(rs.getString("ghiChu"));

                    ct.setMaSanPhamDoi(rs.getString("maSanPhamDoi"));
                    ct.setTenSanPhamDoi(rs.getString("tenSanPhamDoi"));
                    ct.setSoLuongDoi(rs.getInt("soLuongDoi"));
                    ct.setDonGiaDoi(rs.getDouble("donGiaDoi"));

                    phieu.themChiTiet(ct);
                }
            }
        }
    }
}
