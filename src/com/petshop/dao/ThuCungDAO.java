package com.petshop.dao;

import com.petshop.model.ChiTietDichVuLichHen;
import com.petshop.model.LichSuChamSoc;
import com.petshop.model.ThuCung;
import com.petshop.util.DatabaseManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp DAO phụ trách truy vấn và thao tác cơ sở dữ liệu cho thực thể Thú cưng (ThuCung).
 * Tuân thủ Quy ước #60: không nuốt ngoại lệ SQLException mà ném IllegalStateException.
 */
public class ThuCungDAO {

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Lấy toàn bộ danh sách thú cưng trong hệ thống kèm thông tin chủ nuôi.
     *
     * @return Danh sách thú cưng
     */
    public List<ThuCung> layTatCaThuCung() {
        List<ThuCung> danhSach = new ArrayList<>();
        String sql = """
                SELECT tc.maThuCung, tc.maKhachHang, tc.tenThuCung, tc.loaiThuCung, tc.giongLoai,
                       tc.canNang, tc.ngaySinh, tc.diUng, tc.gioiTinh,
                       kh.hoTen AS tenKhachHang, kh.soDienThoai AS soDienThoaiKhachHang
                FROM dbo.ThuCung tc
                LEFT JOIN dbo.KhachHang kh ON tc.maKhachHang = kh.maKhachHang
                ORDER BY tc.maThuCung DESC
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                danhSach.add(anhXaDongThuCung(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi truy vấn toàn bộ danh sách thú cưng: " + e.getMessage(), e);
        }

        return danhSach;
    }

    /**
     * Lấy danh sách thú cưng thuộc sở hữu của một khách hàng dựa vào mã khách hàng.
     *
     * @param maKhachHang Mã định danh của khách hàng
     * @return Danh sách các thú cưng của khách hàng đó
     */
    public List<ThuCung> layDanhSachTheoKhachHang(String maKhachHang) {
        List<ThuCung> danhSach = new ArrayList<>();
        String sql = """
                SELECT tc.maThuCung, tc.maKhachHang, tc.tenThuCung, tc.loaiThuCung, tc.giongLoai,
                       tc.canNang, tc.ngaySinh, tc.diUng, tc.gioiTinh,
                       kh.hoTen AS tenKhachHang, kh.soDienThoai AS soDienThoaiKhachHang
                FROM dbo.ThuCung tc
                LEFT JOIN dbo.KhachHang kh ON tc.maKhachHang = kh.maKhachHang
                WHERE tc.maKhachHang = ?
                ORDER BY tc.maThuCung ASC
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maKhachHang);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(anhXaDongThuCung(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi truy vấn danh sách thú cưng của khách hàng " + maKhachHang + ": " + e.getMessage(), e);
        }

        return danhSach;
    }

    /**
     * Tìm kiếm thú cưng theo mã thú cưng cụ thể.
     */
    public ThuCung layThuCungTheoMa(String maThuCung) {
        String sql = """
                SELECT tc.maThuCung, tc.maKhachHang, tc.tenThuCung, tc.loaiThuCung, tc.giongLoai,
                       tc.canNang, tc.ngaySinh, tc.diUng, tc.gioiTinh,
                       kh.hoTen AS tenKhachHang, kh.soDienThoai AS soDienThoaiKhachHang
                FROM dbo.ThuCung tc
                LEFT JOIN dbo.KhachHang kh ON tc.maKhachHang = kh.maKhachHang
                WHERE tc.maThuCung = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maThuCung);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return anhXaDongThuCung(rs);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi tìm thú cưng mã " + maThuCung + ": " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Tìm kiếm và lọc thú cưng theo từ khóa và loài.
     * Từ khóa tìm trên: mã thú cưng, tên thú cưng, giống loài, tên chủ nuôi, số điện thoại chủ nuôi.
     *
     * @param tuKhoa  Từ khóa tra cứu
     * @param locLoai Loài thú cưng cần lọc (Chó, Mèo,...) hoặc null / "Tất cả"
     * @return Danh sách thú cưng thỏa điều kiện
     */
    public List<ThuCung> timKiemThuCung(String tuKhoa, String locLoai) {
        List<ThuCung> danhSach = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT tc.maThuCung, tc.maKhachHang, tc.tenThuCung, tc.loaiThuCung, tc.giongLoai,
                       tc.canNang, tc.ngaySinh, tc.diUng, tc.gioiTinh,
                       kh.hoTen AS tenKhachHang, kh.soDienThoai AS soDienThoaiKhachHang
                FROM dbo.ThuCung tc
                LEFT JOIN dbo.KhachHang kh ON tc.maKhachHang = kh.maKhachHang
                WHERE 1=1
                """);

        List<Object> thamSo = new ArrayList<>();

        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            String pattern = "%" + tuKhoa.trim() + "%";
            sql.append("""
                     AND (tc.maThuCung LIKE ?
                          OR tc.tenThuCung LIKE ?
                          OR tc.loaiThuCung LIKE ?
                          OR tc.giongLoai LIKE ?
                          OR kh.hoTen LIKE ?
                          OR kh.soDienThoai LIKE ?)
                    """);
            thamSo.add(pattern);
            thamSo.add(pattern);
            thamSo.add(pattern);
            thamSo.add(pattern);
            thamSo.add(pattern);
            thamSo.add(pattern);
        }

        if (locLoai != null && !locLoai.trim().isEmpty() && !"Tất cả".equalsIgnoreCase(locLoai.trim())) {
            if ("Khác".equalsIgnoreCase(locLoai.trim())) {
                sql.append(" AND tc.loaiThuCung NOT IN (N'Chó', N'Mèo', N'Thỏ', N'Hamster', N'Chim')");
            } else {
                sql.append(" AND tc.loaiThuCung = ?");
                thamSo.add(locLoai.trim());
            }
        }

        sql.append(" ORDER BY tc.maThuCung DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < thamSo.size(); i++) {
                ps.setObject(i + 1, thamSo.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(anhXaDongThuCung(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi tìm kiếm thú cưng: " + e.getMessage(), e);
        }

        return danhSach;
    }

    /**
     * Thêm mới một thú cưng vào cơ sở dữ liệu.
     *
     * @param tc Đối tượng thú cưng cần thêm
     * @return true nếu thêm thành công
     */
    public boolean themThuCung(ThuCung tc) {
        String sql = """
                INSERT INTO dbo.ThuCung (maThuCung, maKhachHang, tenThuCung, loaiThuCung, giongLoai, canNang, ngaySinh, diUng, gioiTinh)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tc.getMaThuCung());
            ps.setString(2, tc.getMaKhachHang());
            ps.setString(3, tc.getTenThuCung());
            ps.setString(4, tc.getLoaiThuCung());
            ps.setString(5, tc.getGiongLoai());
            ps.setBigDecimal(6, tc.getCanNang() != null ? tc.getCanNang() : BigDecimal.ZERO);
            ps.setDate(7, tc.getNgaySinh() != null ? Date.valueOf(tc.getNgaySinh()) : null);
            ps.setString(8, tc.getDiUng());
            ps.setString(9, tc.getGioiTinh());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi thêm mới thú cưng " + tc.getMaThuCung() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật thông tin thú cưng trong cơ sở dữ liệu.
     *
     * @param tc Đối tượng thú cưng với thông tin mới
     * @return true nếu cập nhật thành công
     */
    public boolean capNhatThuCung(ThuCung tc) {
        String sql = """
                UPDATE dbo.ThuCung
                SET maKhachHang = ?,
                    tenThuCung = ?,
                    loaiThuCung = ?,
                    giongLoai = ?,
                    canNang = ?,
                    ngaySinh = ?,
                    diUng = ?,
                    gioiTinh = ?
                WHERE maThuCung = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tc.getMaKhachHang());
            ps.setString(2, tc.getTenThuCung());
            ps.setString(3, tc.getLoaiThuCung());
            ps.setString(4, tc.getGiongLoai());
            ps.setBigDecimal(5, tc.getCanNang() != null ? tc.getCanNang() : BigDecimal.ZERO);
            ps.setDate(6, tc.getNgaySinh() != null ? Date.valueOf(tc.getNgaySinh()) : null);
            ps.setString(7, tc.getDiUng());
            ps.setString(8, tc.getGioiTinh());
            ps.setString(9, tc.getMaThuCung());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi cập nhật thú cưng " + tc.getMaThuCung() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Tự động sinh mã thú cưng mới theo quy tắc: TC-yyyyMMdd-xxxxxx.
     *
     * @return Mã thú cưng mới duy nhất
     */
    public String taoMaThuCung() {
        String ngayChuoi = LocalDate.now().format(DINH_DANG_NGAY);
        String tienTo = "TC-" + ngayChuoi + "-";

        String sql = """
                SELECT MAX(CAST(RIGHT(maThuCung, 6) AS INT)) AS soCuoi
                FROM dbo.ThuCung
                WHERE maThuCung LIKE ?
                """;

        int soThuTu = 1;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tienTo + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int maxSo = rs.getInt("soCuoi");
                    if (!rs.wasNull()) {
                        soThuTu = maxSo + 1;
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi sinh mã thú cưng mới: " + e.getMessage(), e);
        }

        return String.format("%s%06d", tienTo, soThuTu);
    }

    /**
     * Lấy lịch sử chăm sóc dịch vụ của thú cưng từ các phiếu dịch vụ.
     *
     * @param maThuCung Mã thú cưng
     * @return Danh sách lịch sử chăm sóc (mảng dữ liệu hiển thị bảng: Mã phiếu, Ngày lập, Tên dịch vụ, Đơn giá, Tên NV, Ghi chú)
     */
    public List<String[]> layLichSuChamSoc(String maThuCung) {
        List<String[]> lichSu = new ArrayList<>();
        String sql = """
                SELECT ct.maPhieuDichVu,
                       pdv.ngayTao,
                       dv.tenDichVu,
                       ct.donGia,
                       nv.hoTen AS tenNhanVien,
                       pdv.trangThai
                FROM dbo.ChiTietPhieuDichVu ct
                JOIN dbo.PhieuDichVu pdv ON ct.maPhieuDichVu = pdv.maPhieuDichVu
                JOIN dbo.DichVuChamSoc dv ON ct.maDichVu = dv.maDichVu
                LEFT JOIN dbo.NhanVien nv ON pdv.maNhanVien = nv.maNhanVien
                WHERE ct.maThuCung = ?
                ORDER BY pdv.ngayTao DESC
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maThuCung);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date sqlDate = rs.getDate("ngayTao");
                    String ngayTao = sqlDate != null ? sqlDate.toLocalDate().toString() : "-";
                    lichSu.add(new String[]{
                            rs.getString("maPhieuDichVu"),
                            ngayTao,
                            rs.getString("tenDichVu"),
                            String.format("%,.0f đ", rs.getDouble("donGia")),
                            rs.getString("tenNhanVien") != null ? rs.getString("tenNhanVien") : "-",
                            rs.getString("trangThai") != null ? rs.getString("trangThai") : "-"
                    });
                }
            }
        } catch (SQLException e) {
            // Nếu bảng PhieuDichVu chưa có dữ liệu hoặc truy vấn lỗi thì log và trả danh sách rỗng, không crash màn hình
            System.err.println("Ghi nhận: Chưa có dữ liệu phiếu dịch vụ cho thú cưng " + maThuCung + " (" + e.getMessage() + ")");
        }

        return lichSu;
    }

    /**
     * Lấy danh sách lịch hẹn chăm sóc dịch vụ của thú cưng từ bảng LichHen,
     * có thể lọc theo khoảng ngày (Từ ngày, Đến ngày) và trạng thái.
     *
     * @param maThuCung Mã thú cưng
     * @param tuNgay    Từ ngày (hoặc null)
     * @param denNgay   Đến ngày (hoặc null)
     * @param trangThai Trạng thái (hoặc null / "Tất cả")
     * @return Danh sách lịch sử chăm sóc
     */
    public List<LichSuChamSoc> layDanhSachLichHenChamSoc(String maThuCung, LocalDate tuNgay, LocalDate denNgay, String trangThai) {
        List<LichSuChamSoc> danhSach = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT lh.maLichHen, lh.maThuCung, tc.tenThuCung,
                       lh.maKhachHang, kh.hoTen AS tenKhachHang,
                       lh.maNhanVien, nv.hoTen AS tenNhanVien,
                       lh.ngayHen, lh.gioHen, lh.trangThai, lh.ghiChu
                FROM dbo.LichHen lh
                LEFT JOIN dbo.ThuCung tc ON lh.maThuCung = tc.maThuCung AND lh.maKhachHang = tc.maKhachHang
                LEFT JOIN dbo.KhachHang kh ON lh.maKhachHang = kh.maKhachHang
                LEFT JOIN dbo.NhanVien nv ON lh.maNhanVien = nv.maNhanVien
                WHERE lh.maThuCung = ?
                """);

        List<Object> thamSo = new ArrayList<>();
        thamSo.add(maThuCung);

        if (tuNgay != null) {
            sql.append(" AND lh.ngayHen >= ?");
            thamSo.add(Date.valueOf(tuNgay));
        }

        if (denNgay != null) {
            sql.append(" AND lh.ngayHen <= ?");
            thamSo.add(Date.valueOf(denNgay));
        }

        if (trangThai != null && !trangThai.trim().isEmpty() && !"Tất cả".equalsIgnoreCase(trangThai.trim())) {
            sql.append(" AND lh.trangThai = ?");
            thamSo.add(trangThai.trim());
        }

        sql.append(" ORDER BY lh.ngayHen DESC, lh.gioHen DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < thamSo.size(); i++) {
                ps.setObject(i + 1, thamSo.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date sqlDate = rs.getDate("ngayHen");
                    LocalDate ngayHen = sqlDate != null ? sqlDate.toLocalDate() : null;

                    Time sqlTime = rs.getTime("gioHen");
                    String gioHen = "-";
                    if (sqlTime != null) {
                        String t = sqlTime.toString();
                        gioHen = t.length() >= 5 ? t.substring(0, 5) : t;
                    }

                    danhSach.add(new LichSuChamSoc(
                            rs.getString("maLichHen"),
                            rs.getString("maThuCung"),
                            rs.getString("tenThuCung"),
                            rs.getString("maKhachHang"),
                            rs.getString("tenKhachHang"),
                            rs.getString("maNhanVien"),
                            rs.getString("tenNhanVien"),
                            ngayHen,
                            gioHen,
                            rs.getString("trangThai"),
                            rs.getString("ghiChu")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi tải lịch sử chăm sóc của thú cưng " + maThuCung + ": " + e.getMessage(), e);
        }

        return danhSach;
    }

    /**
     * Lấy danh sách các dịch vụ chi tiết được thực hiện trong một lần hẹn từ bảng ChiTietLichHen.
     *
     * @param maLichHen Mã lịch hẹn
     * @return Danh sách chi tiết dịch vụ
     */
    public List<ChiTietDichVuLichHen> layChiTietDichVuLichHen(String maLichHen) {
        List<ChiTietDichVuLichHen> danhSach = new ArrayList<>();
        String sql = """
                SELECT ct.maDichVu, dv.tenDichVu, ct.canNangThuCung, ct.donGia
                FROM dbo.ChiTietLichHen ct
                LEFT JOIN dbo.DichVuChamSoc dv ON ct.maDichVu = dv.maDichVu
                WHERE ct.maLichHen = ?
                ORDER BY ct.maDichVu ASC
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maLichHen);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tenDichVu = rs.getString("tenDichVu");
                    if (tenDichVu == null) {
                        tenDichVu = rs.getString("maDichVu");
                    }
                    danhSach.add(new ChiTietDichVuLichHen(
                            tenDichVu,
                            rs.getBigDecimal("canNangThuCung"),
                            rs.getBigDecimal("donGia")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi tải chi tiết dịch vụ lịch hẹn " + maLichHen + ": " + e.getMessage(), e);
        }

        return danhSach;
    }

    /**
     * Ánh xạ một dòng ResultSet sang đối tượng ThuCung.
     */
    private ThuCung anhXaDongThuCung(ResultSet rs) throws SQLException {
        Date sqlNgaySinh = rs.getDate("ngaySinh");
        LocalDate ngaySinh = sqlNgaySinh != null ? sqlNgaySinh.toLocalDate() : null;

        String tenKh = null;
        String sdtKh = null;
        try {
            tenKh = rs.getString("tenKhachHang");
            sdtKh = rs.getString("soDienThoaiKhachHang");
        } catch (SQLException ignored) {
            // Trường hợp truy vấn không JOIN với KhachHang
        }

        return new ThuCung(
                rs.getString("maThuCung"),
                rs.getString("maKhachHang"),
                rs.getString("tenThuCung"),
                rs.getString("loaiThuCung"),
                rs.getString("giongLoai"),
                rs.getBigDecimal("canNang"),
                ngaySinh,
                rs.getString("diUng"),
                rs.getString("gioiTinh"),
                tenKh,
                sdtKh
        );
    }
}
