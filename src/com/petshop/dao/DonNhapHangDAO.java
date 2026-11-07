package com.petshop.dao;

import com.petshop.entity.ChiTietDonNhap;
import com.petshop.entity.DonNhapHang;
import com.petshop.entity.NhaCungCap;
import com.petshop.entity.NhanVien;
import com.petshop.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lớp DAO truy xuất và thao tác dữ liệu bảng DonNhapHang & ChiTietDonNhap.
 */
public class DonNhapHangDAO {

    private static final DateTimeFormatter DINH_DANG_NGAY_MA = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Tự động sinh mã đơn nhập hàng theo định dạng DH-yyyyMMdd-xxxxxx.
     */
    public String taoMaDonNhapHang() {
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                String ngay = LocalDate.now().format(DINH_DANG_NGAY_MA);
                return "DH-" + ngay + "-000001";
            }
            return taoMaDonNhapHang(conn);
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi sinh mã đơn nhập hàng: " + e.getMessage(), e);
        }
    }

    /**
     * Tự động sinh mã đơn nhập hàng trong một connection sẵn có (mã mới luôn mang tiền tố DH-).
     */
    public String taoMaDonNhapHang(Connection conn) throws SQLException {
        String ngay = LocalDate.now().format(DINH_DANG_NGAY_MA);
        String tienTo = "DH-" + ngay + "-";
        String sql = "SELECT maDonHang FROM DonNhapHang WHERE maDonHang LIKE ?";

        int maxStt = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tienTo + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ma = rs.getString("maDonHang");
                    if (ma != null) {
                        String[] phan = ma.split("-");
                        if (phan.length == 3) {
                            try {
                                int stt = Integer.parseInt(phan[2]);
                                if (stt > maxStt) {
                                    maxStt = stt;
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }
            }
        }
        return tienTo + String.format("%06d", maxStt + 1);
    }

    /**
     * Thêm mới một đơn nhập hàng cùng danh sách chi tiết (trong cùng transaction).
     */
    public boolean themDonNhapHang(DonNhapHang donHang, List<ChiTietDonNhap> danhSachChiTiet) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            if (conn == null) {
                throw new IllegalStateException("Lỗi khi thêm đơn nhập: không thể kết nối cơ sở dữ liệu");
            }
            conn.setAutoCommit(false);

            String sqlDonHang = "INSERT INTO DonNhapHang (maDonHang, maNhaCungCap, maNhanVien, ngayLap, gioLap, tongTien, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?)";
            String sqlChiTiet = "INSERT INTO ChiTietDonNhap (maDonHang, maSanPham, soLuong, soLuongThucNhan, donGia) VALUES (?, ?, ?, ?, ?)";

            kiemTraNhaCungCapDangHoatDong(conn, donHang.getMaNhaCungCap());

            try (PreparedStatement psDH = conn.prepareStatement(sqlDonHang)) {
                psDH.setString(1, donHang.getMaDonHang());
                psDH.setString(2, donHang.getMaNhaCungCap());
                psDH.setString(3, donHang.getMaNhanVien());
                psDH.setDate(4, Date.valueOf(donHang.getNgayLap()));
                psDH.setTime(5, Time.valueOf(donHang.getGioLap() != null ? donHang.getGioLap() : LocalTime.now().withNano(0)));
                psDH.setDouble(6, donHang.tinhTongTien());
                psDH.setNString(7, donHang.getTrangThai() != null ? donHang.getTrangThai() : DonNhapHang.TRANG_THAI_CHO_NHAN);

                int rowsDH = psDH.executeUpdate();
                if (rowsDH <= 0) {
                    conn.rollback();
                    throw new IllegalStateException("Lỗi khi thêm đơn nhập: không thể chèn dữ liệu");
                }
            }

            try (PreparedStatement psCT = conn.prepareStatement(sqlChiTiet)) {
                for (ChiTietDonNhap ct : danhSachChiTiet) {
                    psCT.setString(1, donHang.getMaDonHang());
                    psCT.setString(2, ct.getMaSanPham());
                    psCT.setInt(3, ct.getSoLuong());
                    psCT.setInt(4, ct.getSoLuongThucNhan() != null ? ct.getSoLuongThucNhan() : ct.getSoLuong());
                    psCT.setDouble(5, ct.getDonGia());
                    psCT.addBatch();
                }
                psCT.executeBatch();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
            }
            throw new IllegalStateException("Lỗi khi thêm đơn nhập hàng: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Lưu nhiều đơn nhập theo từng nhà cung cấp trong một transaction.
     */
    public boolean themNhieuDonNhapHang(List<DonNhapHang> danhSachDonHang) {
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                throw new IllegalStateException("Không thể kết nối cơ sở dữ liệu.");
            }
            conn.setAutoCommit(false);
            try {
                String sqlDonHang = "INSERT INTO DonNhapHang (maDonHang, maNhaCungCap, maNhanVien, ngayLap, gioLap, tongTien, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?)";
                String sqlChiTiet = "INSERT INTO ChiTietDonNhap (maDonHang, maSanPham, soLuong, soLuongThucNhan, donGia) VALUES (?, ?, ?, ?, ?)";

                for (DonNhapHang donHang : danhSachDonHang) {
                    kiemTraNhaCungCapDangHoatDong(conn, donHang.getMaNhaCungCap());
                    donHang.setMaDonHang(taoMaDonNhapHang(conn));

                    try (PreparedStatement ps = conn.prepareStatement(sqlDonHang)) {
                        ps.setString(1, donHang.getMaDonHang());
                        ps.setString(2, donHang.getMaNhaCungCap());
                        ps.setString(3, donHang.getMaNhanVien());
                        ps.setDate(4, Date.valueOf(donHang.getNgayLap()));
                        ps.setTime(5, Time.valueOf(donHang.getGioLap() != null ? donHang.getGioLap() : LocalTime.now().withNano(0)));
                        ps.setDouble(6, donHang.tinhTongTien());
                        ps.setNString(7, donHang.getTrangThai());
                        ps.executeUpdate();
                    }

                    try (PreparedStatement ps = conn.prepareStatement(sqlChiTiet)) {
                        for (ChiTietDonNhap chiTiet : donHang.getDanhSachChiTiet()) {
                            ps.setString(1, donHang.getMaDonHang());
                            ps.setString(2, chiTiet.getMaSanPham());
                            ps.setInt(3, chiTiet.getSoLuong());
                            ps.setInt(4, chiTiet.getSoLuongThucNhan() != null ? chiTiet.getSoLuongThucNhan() : chiTiet.getSoLuong());
                            ps.setDouble(5, chiTiet.getDonGia());
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }
                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                if (ex instanceof IllegalStateException ise) throw ise;
                throw new IllegalStateException("Không thể lưu các đơn nhập: " + ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Lỗi cơ sở dữ liệu: " + ex.getMessage(), ex);
        }
    }

    /**
     * Tìm đơn nhập hàng theo mã.
     */
    public DonNhapHang timTheoMa(String maDonHang) {
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                throw new IllegalStateException("Lỗi khi tìm đơn nhập: không thể kết nối cơ sở dữ liệu");
            }
            String sql = "SELECT dh.*, ncc.tenNhaCungCap, nv.hoTen AS tenNhanVien " +
                    "FROM DonNhapHang dh " +
                    "LEFT JOIN NhaCungCap ncc ON dh.maNhaCungCap = ncc.maNhaCungCap " +
                    "LEFT JOIN NhanVien nv ON dh.maNhanVien = nv.maNhanVien " +
                    "WHERE dh.maDonHang = ?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maDonHang);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        DonNhapHang dh = anhXaDonNhapHang(rs);
                        if (dh.getMaNhanVienNhan() != null) {
                            dh.setTenNhanVienNhan(layTenNhanVien(conn, dh.getMaNhanVienNhan()));
                        }
                        if (dh.getMaNhanVienHuy() != null) {
                            dh.setTenNhanVienHuy(layTenNhanVien(conn, dh.getMaNhanVienHuy()));
                        }
                        napTenHangChoDanhSach(conn, List.of(dh));
                        return dh;
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi tìm đơn nhập theo mã: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Lấy danh sách chi tiết của một đơn nhập hàng.
     */
    public List<ChiTietDonNhap> layChiTietTheoMaDonHang(String maDonHang) {
        List<ChiTietDonNhap> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                throw new IllegalStateException("Lỗi khi lấy chi tiết đơn nhập: không thể kết nối cơ sở dữ liệu");
            }
            String sql = "SELECT ct.*, sp.tenSanPham, sp.donViTinh " +
                    "FROM ChiTietDonNhap ct " +
                    "LEFT JOIN SanPham sp ON ct.maSanPham = sp.maSanPham " +
                    "WHERE ct.maDonHang = ?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maDonHang);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ChiTietDonNhap ct = new ChiTietDonNhap();
                        ct.setMaDonHang(rs.getString("maDonHang"));
                        ct.setMaSanPham(rs.getString("maSanPham"));
                        int soLuong = rs.getInt("soLuong");
                        ct.setSoLuong(soLuong);
                        int thucNhan = rs.getInt("soLuongThucNhan");
                        ct.setSoLuongThucNhan(rs.wasNull() ? soLuong : thucNhan);
                        ct.setDonGia(rs.getDouble("donGia"));
                        ct.setTenSanPham(rs.getNString("tenSanPham"));
                        ct.setDonViTinh(rs.getNString("donViTinh"));
                        list.add(ct);
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi lấy chi tiết đơn nhập: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Lấy tất cả đơn nhập hàng sắp xếp theo ngày lập giảm dần.
     */
    public List<DonNhapHang> layTatCaDonHang() {
        return timKiemDonHang(null, null, null, null, null);
    }

    /**
     * Tìm kiếm đơn nhập hàng theo bộ lọc đa tiêu chí (hỗ trợ tìm kiếm theo tên hàng).
     */
    public List<DonNhapHang> timKiemDonHang(String keyword, String maNhaCungCap, LocalDate tuNgay, LocalDate denNgay, String trangThai) {
        return timKiemDonHang(keyword, maNhaCungCap, tuNgay, denNgay, trangThai, null);
    }

    /**
     * Tìm kiếm đơn nhập hàng theo bộ lọc đa tiêu chí:
     * - keyword: mã đơn hàng, tên nhà cung cấp, tên nhân viên, hoặc tên mặt hàng trong đơn
     * - tenHang: tìm theo tên sản phẩm có trong chi tiết đơn nhập
     * - maNhaCungCap: mã NCC
     * - tuNgay, denNgay: khoảng ngày lập
     * - trangThai: Chờ nhận hàng, Hoàn tất, Đã hủy
     */
    public List<DonNhapHang> timKiemDonHang(String keyword, String maNhaCungCap, LocalDate tuNgay, LocalDate denNgay, String trangThai, String tenHang) {
        List<DonNhapHang> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                throw new IllegalStateException("Lỗi khi tìm kiếm đơn nhập: không thể kết nối cơ sở dữ liệu");
            }

            StringBuilder sql = new StringBuilder(
                    "SELECT dh.*, ncc.tenNhaCungCap, nv.hoTen AS tenNhanVien " +
                    "FROM DonNhapHang dh " +
                    "LEFT JOIN NhaCungCap ncc ON dh.maNhaCungCap = ncc.maNhaCungCap " +
                    "LEFT JOIN NhanVien nv ON dh.maNhanVien = nv.maNhanVien " +
                    "WHERE 1=1"
            );

            List<Object> params = new ArrayList<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.trim() + "%";
                sql.append(" AND (dh.maDonHang LIKE ? OR ncc.tenNhaCungCap LIKE ? OR nv.hoTen LIKE ? " +
                        "OR EXISTS (SELECT 1 FROM ChiTietDonNhap ct JOIN SanPham sp ON ct.maSanPham = sp.maSanPham " +
                        "WHERE ct.maDonHang = dh.maDonHang AND (sp.tenSanPham LIKE ? OR sp.maSanPham LIKE ?)))");
                params.add(kw);
                params.add(kw);
                params.add(kw);
                params.add(kw);
                params.add(kw);
            }

            if (tenHang != null && !tenHang.trim().isEmpty()) {
                String th = "%" + tenHang.trim() + "%";
                sql.append(" AND EXISTS (SELECT 1 FROM ChiTietDonNhap ct JOIN SanPham sp ON ct.maSanPham = sp.maSanPham " +
                        "WHERE ct.maDonHang = dh.maDonHang AND (sp.tenSanPham LIKE ? OR sp.maSanPham LIKE ?))");
                params.add(th);
                params.add(th);
            }

            if (maNhaCungCap != null && !maNhaCungCap.trim().isEmpty()) {
                sql.append(" AND dh.maNhaCungCap = ?");
                params.add(maNhaCungCap.trim());
            }

            if (tuNgay != null) {
                sql.append(" AND dh.ngayLap >= ?");
                params.add(Date.valueOf(tuNgay));
            }

            if (denNgay != null) {
                sql.append(" AND dh.ngayLap <= ?");
                params.add(Date.valueOf(denNgay));
            }

            if (trangThai != null && !trangThai.trim().isEmpty() && !trangThai.equalsIgnoreCase("Tất cả")) {
                sql.append(" AND dh.trangThai = ?");
                params.add(trangThai.trim());
            }

            sql.append(" ORDER BY dh.ngayLap DESC, dh.gioLap DESC, dh.maDonHang DESC");

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(anhXaDonNhapHang(rs));
                    }
                }
            }
            napTenHangChoDanhSach(conn, list);
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi tìm kiếm đơn nhập hàng: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Cập nhật trạng thái đơn hàng (dùng khi hủy đơn với lý do và người hủy).
     */
    public boolean capNhatTrangThaiHuy(String maDonHang, String maNhanVienHuy, String lyDoHuy) {
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                throw new IllegalStateException("Lỗi khi cập nhật trạng thái đơn nhập: không thể kết nối cơ sở dữ liệu");
            }
            String sql = "UPDATE DonNhapHang SET trangThai = ?, maNhanVienHuy = ?, lyDoHuy = ? WHERE maDonHang = ? AND trangThai = ?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setNString(1, DonNhapHang.TRANG_THAI_DA_HUY);
                ps.setString(2, maNhanVienHuy);
                ps.setNString(3, lyDoHuy != null ? lyDoHuy.trim() : "");
                ps.setString(4, maDonHang);
                ps.setNString(5, DonNhapHang.TRANG_THAI_CHO_NHAN);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi khi hủy đơn nhập hàng: " + e.getMessage(), e);
        }
    }

    /**
     * Xác nhận nhận hàng và cộng tồn kho.
     */
    public boolean xacNhanNhanHangVaCongTonKho(String maDonHang, String maNhanVienNhan,
                                                LocalDateTime ngayGioNhan, double tongTienThucNhan,
                                                List<ChiTietDonNhap> chiTietCapNhat) {
        String ketQua = xacNhanNhanHangThieuVaTaoDonBu(maDonHang, maNhanVienNhan, ngayGioNhan, tongTienThucNhan,
                null, chiTietCapNhat, null);
        return ketQua != null;
    }

    /**
     * Xác nhận nhận hàng có xử lý giao thiếu (Phương án 3):
     * - Cập nhật trạng thái 'Hoàn tất' cho đơn hiện tại với số lượng thực nhận và tổng tiền thực nhận.
     * - Cộng tồn kho chỉ đúng số lượng thực nhận.
     * - Lưu ghi chú/lý do chênh lệch vào lyDoHuy (nếu đóng đơn hoặc tách đơn bù).
     * - Nếu có donNhapBu != null: tạo mới đơn nhập bù cho phần thiếu trong cùng 1 transaction.
     *
     * @return Mã đơn bù (nếu tạo) hoặc mã đơn gốc (nếu không tạo đơn bù)
     */
    public String xacNhanNhanHangThieuVaTaoDonBu(String maDonHang, String maNhanVienNhan,
                                                LocalDateTime ngayGioNhan, double tongTienThucNhan,
                                                String ghiChuChenhLech,
                                                List<ChiTietDonNhap> chiTietCapNhat,
                                                DonNhapHang donNhapBu) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            if (conn == null) {
                throw new IllegalStateException("Lỗi khi xác nhận nhận hàng: không thể kết nối cơ sở dữ liệu");
            }
            conn.setAutoCommit(false);

            // 1. Kiểm tra danh sách chi tiết
            if (chiTietCapNhat == null || chiTietCapNhat.isEmpty()) {
                conn.rollback();
                throw new IllegalStateException("Đơn hàng không có chi tiết sản phẩm để xác nhận nhận hàng.");
            }

            // 2. Chuyển trạng thái có điều kiện (WHERE trangThai = 'Chờ nhận hàng')
            String sqlUpdateDonHang = "UPDATE DonNhapHang SET trangThai = ?, maNhanVienNhan = ?, ngayGioNhan = ?, tongTienThucNhan = ?, lyDoHuy = ? WHERE maDonHang = ? AND trangThai = ?";

            try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateDonHang)) {
                psUpdate.setNString(1, DonNhapHang.TRANG_THAI_HOAN_TAT);
                psUpdate.setString(2, maNhanVienNhan);
                psUpdate.setTimestamp(3, ngayGioNhan != null ? Timestamp.valueOf(ngayGioNhan) : Timestamp.valueOf(LocalDateTime.now()));
                psUpdate.setDouble(4, tongTienThucNhan);
                psUpdate.setNString(5, ghiChuChenhLech);
                psUpdate.setString(6, maDonHang);
                psUpdate.setNString(7, DonNhapHang.TRANG_THAI_CHO_NHAN);

                if (psUpdate.executeUpdate() != 1) {
                    conn.rollback();
                    throw new IllegalStateException("Đơn hàng không ở trạng thái 'Chờ nhận hàng' hoặc đã được xác nhận trước đó.");
                }
            }

            // 3. Cập nhật soLuongThucNhan vào ChiTietDonNhap
            String sqlUpdateCT = "UPDATE ChiTietDonNhap SET soLuongThucNhan = ? WHERE maDonHang = ? AND maSanPham = ?";
            try (PreparedStatement psCT = conn.prepareStatement(sqlUpdateCT)) {
                for (ChiTietDonNhap ct : chiTietCapNhat) {
                    psCT.setInt(1, ct.getSoLuongThucNhan());
                    psCT.setString(2, maDonHang);
                    psCT.setString(3, ct.getMaSanPham());
                    psCT.addBatch();
                }
                psCT.executeBatch();
            }

            // 4. Cộng tồn kho SanPham theo soLuongThucNhan
            // Dòng có soLuongThucNhan = 0 thì BỎ QUA câu UPDATE, KHÔNG rollback
            String sqlUpdateTonKho = "UPDATE SanPham SET soLuongTon = soLuongTon + ? WHERE maSanPham = ?";
            try (PreparedStatement psTonKho = conn.prepareStatement(sqlUpdateTonKho)) {
                for (ChiTietDonNhap ct : chiTietCapNhat) {
                    int thucNhan = ct.getSoLuongThucNhan();
                    if (thucNhan > 0) {
                        psTonKho.setInt(1, thucNhan);
                        psTonKho.setString(2, ct.getMaSanPham());
                        if (psTonKho.executeUpdate() != 1) {
                            conn.rollback();
                            throw new IllegalStateException("Không tìm thấy sản phẩm " + ct.getMaSanPham() + " để cộng tồn kho.");
                        }
                    }
                }
            }

            // 5. Tự động tách đơn nhập bù (Backorder) nếu có yêu cầu
            String maDonBu = null;
            if (donNhapBu != null && donNhapBu.getDanhSachChiTiet() != null && !donNhapBu.getDanhSachChiTiet().isEmpty()) {
                maDonBu = taoMaDonNhapHang(conn);
                donNhapBu.setMaDonHang(maDonBu);

                String sqlInsertBu = "INSERT INTO DonNhapHang (maDonHang, maNhaCungCap, maNhanVien, ngayLap, gioLap, tongTien, trangThai, lyDoHuy) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement psBu = conn.prepareStatement(sqlInsertBu)) {
                    psBu.setString(1, maDonBu);
                    psBu.setString(2, donNhapBu.getMaNhaCungCap());
                    psBu.setString(3, donNhapBu.getMaNhanVien());
                    psBu.setDate(4, Date.valueOf(donNhapBu.getNgayLap() != null ? donNhapBu.getNgayLap() : LocalDate.now()));
                    psBu.setTime(5, Time.valueOf(donNhapBu.getGioLap() != null ? donNhapBu.getGioLap() : LocalTime.now().withNano(0)));
                    psBu.setDouble(6, donNhapBu.tinhTongTien());
                    psBu.setNString(7, DonNhapHang.TRANG_THAI_CHO_NHAN);
                    psBu.setNString(8, donNhapBu.getLyDoHuy());
                    psBu.executeUpdate();
                }

                String sqlCTBu = "INSERT INTO ChiTietDonNhap (maDonHang, maSanPham, soLuong, soLuongThucNhan, donGia) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement psCTBu = conn.prepareStatement(sqlCTBu)) {
                    for (ChiTietDonNhap ct : donNhapBu.getDanhSachChiTiet()) {
                        psCTBu.setString(1, maDonBu);
                        psCTBu.setString(2, ct.getMaSanPham());
                        psCTBu.setInt(3, ct.getSoLuong());
                        psCTBu.setInt(4, ct.getSoLuong());
                        psCTBu.setDouble(5, ct.getDonGia());
                        psCTBu.addBatch();
                    }
                    psCTBu.executeBatch();
                }
            }

            conn.commit();
            return maDonBu != null ? maDonBu : maDonHang;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
            }
            throw new IllegalStateException("Lỗi khi xác nhận nhận hàng: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    private String layTenNhanVien(Connection conn, String maNhanVien) {
        if (maNhanVien == null || maNhanVien.isBlank()) return null;
        String sql = "SELECT hoTen FROM NhanVien WHERE maNhanVien = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNhanVien.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getNString("hoTen");
                }
            }
        } catch (SQLException ignored) {}
        return maNhanVien;
    }

    private void kiemTraNhaCungCapDangHoatDong(Connection conn, String maNhaCungCap) throws SQLException {
        String sql = "SELECT trangThai FROM NhaCungCap WHERE maNhaCungCap = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNhaCungCap);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Nhà cung cấp " + maNhaCungCap + " không tồn tại.");
                }
                if (!rs.getBoolean("trangThai")) {
                    throw new IllegalStateException("Nhà cung cấp " + maNhaCungCap + " đã ngừng hợp tác.");
                }
            }
        }
    }

    private void napTenHangChoDanhSach(Connection conn, List<DonNhapHang> list) {
        if (list == null || list.isEmpty()) return;
        Map<String, List<ChiTietDonNhap>> mapChiTiet = new HashMap<>();
        for (int i = 0; i < list.size(); i += 50) {
            List<DonNhapHang> sub = list.subList(i, Math.min(i + 50, list.size()));
            String placeholders = String.join(",", Collections.nCopies(sub.size(), "?"));
            String sql = "SELECT ct.*, sp.tenSanPham, sp.donViTinh " +
                    "FROM ChiTietDonNhap ct " +
                    "LEFT JOIN SanPham sp ON ct.maSanPham = sp.maSanPham " +
                    "WHERE ct.maDonHang IN (" + placeholders + ")";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int j = 0; j < sub.size(); j++) {
                    ps.setString(j + 1, sub.get(j).getMaDonHang());
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String maDH = rs.getString("maDonHang");
                        ChiTietDonNhap ct = new ChiTietDonNhap();
                        ct.setMaDonHang(maDH);
                        ct.setMaSanPham(rs.getString("maSanPham"));
                        ct.setTenSanPham(rs.getNString("tenSanPham"));
                        ct.setDonViTinh(rs.getNString("donViTinh"));
                        int sl = rs.getInt("soLuong");
                        ct.setSoLuong(sl);
                        int tn = rs.getInt("soLuongThucNhan");
                        ct.setSoLuongThucNhan(rs.wasNull() ? sl : tn);
                        ct.setDonGia(rs.getDouble("donGia"));
                        mapChiTiet.computeIfAbsent(maDH, k -> new ArrayList<>()).add(ct);
                    }
                }
            } catch (SQLException ignored) {}
        }
        for (DonNhapHang dh : list) {
            List<ChiTietDonNhap> cts = mapChiTiet.getOrDefault(dh.getMaDonHang(), new ArrayList<>());
            dh.setDanhSachChiTiet(cts);
            List<String> ds = new ArrayList<>();
            for (ChiTietDonNhap ct : cts) {
                String tenSP = ct.getTenSanPham() != null ? ct.getTenSanPham() : "SP";
                ds.add(tenSP + " (x" + ct.getSoLuong() + ")");
            }
            dh.setTenHang(String.join(", ", ds));
        }
    }

    private DonNhapHang anhXaDonNhapHang(ResultSet rs) throws SQLException {
        DonNhapHang dh = new DonNhapHang();
        dh.setMaDonHang(rs.getString("maDonHang"));

        NhaCungCap ncc = new NhaCungCap();
        ncc.setMaNhaCungCap(rs.getString("maNhaCungCap"));
        try {
            ncc.setTenNhaCungCap(rs.getNString("tenNhaCungCap"));
        } catch (SQLException ignored) {}
        dh.setNhaCungCap(ncc);

        NhanVien nv = new NhanVien();
        nv.setMaNhanVien(rs.getString("maNhanVien"));
        try {
            nv.setHoTen(rs.getNString("tenNhanVien"));
        } catch (SQLException ignored) {}
        dh.setNhanVien(nv);

        Date ngayLap = rs.getDate("ngayLap");
        if (ngayLap != null) {
            dh.setNgayLap(ngayLap.toLocalDate());
        }
        Time gioLap = rs.getTime("gioLap");
        dh.setGioLap(gioLap != null ? gioLap.toLocalTime() : LocalTime.MIDNIGHT);

        dh.setTrangThai(rs.getNString("trangThai"));

        String maNVNhan = rs.getString("maNhanVienNhan");
        if (maNVNhan != null && !maNVNhan.isBlank()) {
            NhanVien nvn = new NhanVien();
            nvn.setMaNhanVien(maNVNhan);
            dh.setNhanVienNhan(nvn);
        }
        Timestamp tsNhan = rs.getTimestamp("ngayGioNhan");
        if (tsNhan != null) {
            dh.setNgayGioNhan(tsNhan.toLocalDateTime());
        }

        String maNVHuy = rs.getString("maNhanVienHuy");
        if (maNVHuy != null && !maNVHuy.isBlank()) {
            NhanVien nvh = new NhanVien();
            nvh.setMaNhanVien(maNVHuy);
            dh.setNhanVienHuy(nvh);
        }
        dh.setLyDoHuy(rs.getNString("lyDoHuy"));

        return dh;
    }
}
