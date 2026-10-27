package com.petshop.dao;

import com.petshop.model.DonNhap;
import com.petshop.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DonHangDAO {

    public List<DonNhap.NhaCungCap> layDanhSachNhaCungCap() {
        String sql = "SELECT maNhaCungCap, tenNhaCungCap FROM NhaCungCap ORDER BY tenNhaCungCap";
        List<DonNhap.NhaCungCap> danhSach = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                danhSach.add(new DonNhap.NhaCungCap(
                        resultSet.getString("maNhaCungCap"), resultSet.getString("tenNhaCungCap")));
            }
            return danhSach;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi tải danh sách nhà cung cấp: " + e.getMessage(), e);
        }
    }

    public List<DonNhap.SanPham> layDanhSachSanPham(String maNhaCungCap) {
        String sql = """
                SELECT maSanPham, tenSanPham, maNhaCungCap
                FROM SanPham
                WHERE maNhaCungCap = ? AND trangThai = 1
                ORDER BY tenSanPham
                """;
        List<DonNhap.SanPham> danhSach = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maNhaCungCap);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    danhSach.add(new DonNhap.SanPham(
                            resultSet.getString("maSanPham"), resultSet.getString("tenSanPham"),
                            resultSet.getString("maNhaCungCap")));
                }
            }
            return danhSach;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi tải danh sách sản phẩm: " + e.getMessage(), e);
        }
    }

    public String taoDonNhap(String maNhaCungCap, String maNhanVien,
                             List<DonNhap.ChiTiet> chiTiet, BigDecimal tongTien) {
        LocalDate ngayLap = LocalDate.now();
        String sqlTaoDon = """
                INSERT INTO DonHang (maDonHang, maNhaCungCap, maNhanVien, ngayLap, tongTien, trangThai)
                VALUES (?, ?, ?, ?, ?, N'Chờ nhận hàng')
                """;
        String sqlTaoChiTiet = """
                INSERT INTO ChiTietDonHang (maDonHang, maSanPham, soLuong, donGia)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            connection.setAutoCommit(false);
            try {
                String maDonHang = taoMaDonHang(connection, ngayLap);
                try (PreparedStatement taoDon = connection.prepareStatement(sqlTaoDon)) {
                    taoDon.setString(1, maDonHang);
                    taoDon.setString(2, maNhaCungCap);
                    taoDon.setString(3, maNhanVien);
                    taoDon.setDate(4, Date.valueOf(ngayLap));
                    taoDon.setBigDecimal(5, tongTien);
                    taoDon.executeUpdate();
                }

                try (PreparedStatement taoDong = connection.prepareStatement(sqlTaoChiTiet)) {
                    for (DonNhap.ChiTiet dong : chiTiet) {
                        taoDong.setString(1, maDonHang);
                        taoDong.setString(2, dong.maSanPham());
                        taoDong.setInt(3, dong.soLuong());
                        taoDong.setBigDecimal(4, dong.donGia());
                        taoDong.addBatch();
                    }
                    taoDong.executeBatch();
                }

                connection.commit();
                return maDonHang;
            } catch (SQLException | RuntimeException e) {
                rollback(connection, e);
                if (e instanceof SQLException sqlException) {
                    throw new IllegalStateException("Lỗi lưu đơn nhập: " + sqlException.getMessage(), sqlException);
                }
                throw (RuntimeException) e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi kết nối khi lưu đơn nhập: " + e.getMessage(), e);
        }
    }

    private String taoMaDonHang(Connection connection, LocalDate ngayLap) throws SQLException {
        String ngay = ngayLap.format(DateTimeFormatter.BASIC_ISO_DATE);
        String tienTo = "DH-" + ngay + "-";
        String sql = "SELECT MAX(maDonHang) FROM DonHang WITH (UPDLOCK, HOLDLOCK) WHERE maDonHang LIKE ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tienTo + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                String maLonNhat = resultSet.getString(1);
                int soThuTu = maLonNhat == null ? 1 : Integer.parseInt(maLonNhat.substring(maLonNhat.length() - 6)) + 1;
                if (soThuTu > 999999) {
                    throw new IllegalStateException("Đã vượt số lượng mã đơn nhập trong ngày.");
                }
                return tienTo + String.format("%06d", soThuTu);
            }
        }
    }

    public List<DonNhap> layDonChoNhanHang() {
        String sql = """
                SELECT dh.maDonHang, ncc.tenNhaCungCap, dh.ngayLap, dh.tongTien
                FROM DonHang dh
                JOIN NhaCungCap ncc ON ncc.maNhaCungCap = dh.maNhaCungCap
                WHERE dh.trangThai = N'Chờ nhận hàng'
                ORDER BY dh.ngayLap DESC, dh.maDonHang DESC
                """;
        List<DonNhap> danhSach = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                danhSach.add(new DonNhap(
                        resultSet.getString("maDonHang"),
                        resultSet.getString("tenNhaCungCap"),
                        resultSet.getDate("ngayLap").toLocalDate(),
                        resultSet.getBigDecimal("tongTien")));
            }
            return danhSach;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi tải danh sách đơn nhập chờ nhận: " + e.getMessage(), e);
        }
    }

    public List<DonNhap> timKiemDonNhap(String maDonHang, String tenNhaCungCap,
                                        LocalDate tuNgay, LocalDate denNgay) {
        StringBuilder sql = new StringBuilder("""
                SELECT dh.maDonHang, ncc.tenNhaCungCap, dh.ngayLap, dh.tongTien
                FROM DonHang dh
                JOIN NhaCungCap ncc ON ncc.maNhaCungCap = dh.maNhaCungCap
                WHERE 1 = 1
                """);
        List<Object> thamSo = new ArrayList<>();
        if (maDonHang != null && !maDonHang.isBlank()) {
            sql.append(" AND dh.maDonHang LIKE ?");
            thamSo.add("%" + maDonHang.trim() + "%");
        }
        if (tenNhaCungCap != null && !tenNhaCungCap.isBlank()) {
            sql.append(" AND ncc.tenNhaCungCap LIKE ?");
            thamSo.add("%" + tenNhaCungCap.trim() + "%");
        }
        if (tuNgay != null) {
            sql.append(" AND dh.ngayLap >= ?");
            thamSo.add(Date.valueOf(tuNgay));
        }
        if (denNgay != null) {
            sql.append(" AND dh.ngayLap <= ?");
            thamSo.add(Date.valueOf(denNgay));
        }
        sql.append(" ORDER BY dh.ngayLap DESC, dh.maDonHang DESC");

        List<DonNhap> danhSach = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < thamSo.size(); i++) {
                statement.setObject(i + 1, thamSo.get(i));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    danhSach.add(new DonNhap(
                            resultSet.getString("maDonHang"),
                            resultSet.getString("tenNhaCungCap"),
                            resultSet.getDate("ngayLap").toLocalDate(),
                            resultSet.getBigDecimal("tongTien")));
                }
            }
            return danhSach;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi tìm kiếm đơn nhập: " + e.getMessage(), e);
        }
    }

    public List<DonNhap.ChiTiet> layChiTietDonHang(String maDonHang) {
        String sql = """
                SELECT ct.maSanPham, sp.tenSanPham, ct.soLuong, ct.donGia
                FROM ChiTietDonHang ct
                JOIN SanPham sp ON sp.maSanPham = ct.maSanPham
                WHERE ct.maDonHang = ?
                ORDER BY sp.tenSanPham
                """;
        List<DonNhap.ChiTiet> danhSach = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maDonHang);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    danhSach.add(new DonNhap.ChiTiet(
                            resultSet.getString("maSanPham"),
                            resultSet.getString("tenSanPham"),
                            resultSet.getInt("soLuong"),
                            resultSet.getBigDecimal("donGia")));
                }
            }
            return danhSach;
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi tải chi tiết đơn nhập: " + e.getMessage(), e);
        }
    }

    public boolean xacNhanNhanHang(String maDonHang) {
        String sqlCapNhatTrangThai = """
                UPDATE DonHang
                SET trangThai = N'Hoàn tất'
                WHERE maDonHang = ? AND trangThai = N'Chờ nhận hàng'
                """;
        String sqlLayChiTiet = "SELECT maSanPham, soLuong FROM ChiTietDonHang WHERE maDonHang = ?";
        String sqlCongTonKho = "UPDATE SanPham SET soLuongTon = soLuongTon + ? WHERE maSanPham = ?";

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement updateStatus = connection.prepareStatement(sqlCapNhatTrangThai)) {
                updateStatus.setString(1, maDonHang);
                if (updateStatus.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }

                int soDongChiTiet = 0;
                try (PreparedStatement selectDetails = connection.prepareStatement(sqlLayChiTiet);
                     ResultSet details = bindAndExecute(selectDetails, maDonHang);
                     PreparedStatement updateStock = connection.prepareStatement(sqlCongTonKho)) {
                    while (details.next()) {
                        soDongChiTiet++;
                        updateStock.setInt(1, details.getInt("soLuong"));
                        updateStock.setString(2, details.getString("maSanPham"));
                        if (updateStock.executeUpdate() != 1) {
                            throw new IllegalStateException("Không tìm thấy sản phẩm " + details.getString("maSanPham"));
                        }
                    }
                }
                if (soDongChiTiet == 0) {
                    throw new IllegalStateException("Đơn nhập không có sản phẩm nên không thể nhận hàng.");
                }

                connection.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                rollback(connection, e);
                if (e instanceof SQLException sqlException) {
                    throw new IllegalStateException("Lỗi xác nhận nhận hàng: " + sqlException.getMessage(), sqlException);
                }
                throw (RuntimeException) e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi kết nối khi xác nhận nhận hàng: " + e.getMessage(), e);
        }
    }

    private ResultSet bindAndExecute(PreparedStatement statement, String maDonHang) throws SQLException {
        statement.setString(1, maDonHang);
        return statement.executeQuery();
    }

    private void rollback(Connection connection, Exception cause) {
        try {
            connection.rollback();
        } catch (SQLException rollbackError) {
            cause.addSuppressed(rollbackError);
        }
    }
}