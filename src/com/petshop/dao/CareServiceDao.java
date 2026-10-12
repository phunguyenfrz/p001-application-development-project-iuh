package com.petshop.dao;

import com.petshop.model.CareServiceData;
import com.petshop.util.DbConnection;
import com.petshop.util.TextSearch;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class CareServiceDao implements CareServiceRepository {
    private static final String QUERY = """
            SELECT dv.maDichVu, dv.tenDichVu, dv.moTa, dv.thoiLuong, dv.trangThai,
              (SELECT TOP (1) gia FROM BangGiaDichVu b WHERE b.maDichVu = dv.maDichVu
                 AND b.phanLoaiKichThuoc = N'Nhỏ' AND b.ngayApDung <= CAST(GETDATE() AS date)
                 AND (b.ngayHetHieuLuc IS NULL OR b.ngayHetHieuLuc >= CAST(GETDATE() AS date))
                 ORDER BY b.ngayApDung DESC) AS giaNho,
              (SELECT TOP (1) gia FROM BangGiaDichVu b WHERE b.maDichVu = dv.maDichVu
                 AND b.phanLoaiKichThuoc = N'Vừa' AND b.ngayApDung <= CAST(GETDATE() AS date)
                 AND (b.ngayHetHieuLuc IS NULL OR b.ngayHetHieuLuc >= CAST(GETDATE() AS date))
                 ORDER BY b.ngayApDung DESC) AS giaVua,
              (SELECT TOP (1) gia FROM BangGiaDichVu b WHERE b.maDichVu = dv.maDichVu
                 AND b.phanLoaiKichThuoc = N'Lớn' AND b.ngayApDung <= CAST(GETDATE() AS date)
                 AND (b.ngayHetHieuLuc IS NULL OR b.ngayHetHieuLuc >= CAST(GETDATE() AS date))
                 ORDER BY b.ngayApDung DESC) AS giaLon
            FROM DichVuChamSoc dv
            """;

    @Override
    public List<CareServiceData> findAll() {
        return query(QUERY + " ORDER BY dv.tenDichVu", null);
    }

    @Override
    public List<CareServiceData> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return query(QUERY + " WHERE "
                + TextSearch.likePredicate(keyword, "dv.maDichVu", "dv.tenDichVu")
                + """
                ORDER BY dv.tenDichVu
                """, keyword);
    }

    @Override
    public void add(CareServiceData service) {
        inTransaction(connection -> {
            String id = service.id();
            try (PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO DichVuChamSoc (maDichVu, tenDichVu, moTa, thoiLuong, trangThai)
                    VALUES (?, ?, ?, ?, ?)
                    """)) {
                statement.setString(1, id);
                statement.setString(2, service.name().trim());
                statement.setString(3, nullable(service.description()));
                statement.setInt(4, service.durationMinutes());
                statement.setBoolean(5, service.active());
                statement.executeUpdate();
            }
            insertPrices(connection, id, service);
            return null;
        });
    }

    @Override
    public void update(CareServiceData service) {
        inTransaction(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("""
                    UPDATE DichVuChamSoc SET tenDichVu = ?, moTa = ?, thoiLuong = ?, trangThai = ?
                    WHERE maDichVu = ?
                    """)) {
                statement.setString(1, service.name().trim());
                statement.setString(2, nullable(service.description()));
                statement.setInt(3, service.durationMinutes());
                statement.setBoolean(4, service.active());
                statement.setString(5, service.id());
                if (statement.executeUpdate() == 0) {
                    throw new IllegalArgumentException("Không tìm thấy dịch vụ cần cập nhật.");
                }
            }
            insertPrices(connection, service.id(), service);
            return null;
        });
    }

    @Override
    public void deactivate(String id) {
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE DichVuChamSoc SET trangThai = 0 WHERE maDichVu = ?")) {
            statement.setString(1, id);
            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException("Không tìm thấy dịch vụ cần ngừng.");
            }
        } catch (SQLException exception) {
            throw failure("Không thể ngừng dịch vụ.", exception);
        }
    }

    private List<CareServiceData> query(String sql, String pattern) {
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (pattern != null) {
                TextSearch.bindLikeParameters(statement, 1, pattern, 2);
            }
            try (ResultSet rows = statement.executeQuery()) {
                List<CareServiceData> result = new ArrayList<>();
                while (rows.next()) {
                    result.add(new CareServiceData(rows.getString("maDichVu"),
                            rows.getString("tenDichVu"), rows.getString("moTa"),
                            rows.getInt("thoiLuong"), rows.getBigDecimal("giaNho"),
                            rows.getBigDecimal("giaVua"), rows.getBigDecimal("giaLon"),
                            LocalDate.now(), rows.getBoolean("trangThai")));
                }
                return List.copyOf(result);
            }
        } catch (SQLException exception) {
            throw failure("Không thể tải dịch vụ chăm sóc.", exception);
        }
    }

    private void insertPrices(Connection connection, String id, CareServiceData service)
            throws SQLException {
        try (PreparedStatement close = connection.prepareStatement("""
                UPDATE BangGiaDichVu SET ngayHetHieuLuc = DATEADD(day, -1, ?)
                WHERE maDichVu = ? AND phanLoaiKichThuoc IN (N'Nhỏ', N'Vừa', N'Lớn')
                  AND ngayApDung < ? AND (ngayHetHieuLuc IS NULL OR ngayHetHieuLuc >= ?)
                """)) {
            close.setDate(1, Date.valueOf(service.effectiveDate()));
            close.setString(2, id);
            close.setDate(3, Date.valueOf(service.effectiveDate()));
            close.setDate(4, Date.valueOf(service.effectiveDate()));
            close.executeUpdate();
        }
        String[] tiers = {"Nhỏ", "Vừa", "Lớn"};
        var prices = List.of(service.smallPrice(), service.mediumPrice(), service.largePrice());
        String sql = """
                MERGE BangGiaDichVu AS target
                USING (SELECT ? AS maDichVu, ? AS phanLoaiKichThuoc, ? AS ngayApDung) AS source
                ON target.maDichVu = source.maDichVu
                   AND target.phanLoaiKichThuoc = source.phanLoaiKichThuoc
                   AND target.ngayApDung = source.ngayApDung
                WHEN MATCHED THEN UPDATE SET gia = ?, ngayHetHieuLuc = NULL
                WHEN NOT MATCHED THEN INSERT
                    (maBangGia, maDichVu, gia, phanLoaiKichThuoc, ngayApDung, ngayHetHieuLuc)
                    VALUES (?, ?, ?, ?, ?, NULL);
                """;
        for (int i = 0; i < tiers.length; i++) {
            String priceId = nextCode(connection, "BG");
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, id);
                statement.setString(2, tiers[i]);
                statement.setDate(3, Date.valueOf(service.effectiveDate()));
                statement.setBigDecimal(4, prices.get(i));
                statement.setString(5, priceId);
                statement.setString(6, id);
                statement.setBigDecimal(7, prices.get(i));
                statement.setString(8, tiers[i]);
                statement.setDate(9, Date.valueOf(service.effectiveDate()));
                statement.executeUpdate();
            }
        }
    }

    private String nextCode(Connection connection, String prefix) throws SQLException {
        String day = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String table = "DichVuChamSoc";
        String column = "maDichVu";
        if ("BG".equals(prefix)) {
            table = "BangGiaDichVu";
            column = "maBangGia";
        }
        String sql = "SELECT ISNULL(MAX(TRY_CONVERT(int, RIGHT(" + column
                + ", 6))), 0) FROM " + table + " WITH (UPDLOCK, HOLDLOCK) WHERE "
                + column + " LIKE ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, prefix + "-" + day + "-%");
            try (ResultSet row = statement.executeQuery()) {
                row.next();
                return prefix + "-" + day + "-" + String.format("%06d", row.getInt(1) + 1);
            }
        }
    }

    private <T> T inTransaction(SqlWork<T> work) {
        try (Connection connection = DbConnection.getConnection()) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            try {
                T result = work.run(connection);
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                if (exception instanceof SQLException sqlException) {
                    throw failure("Không thể lưu dịch vụ và bảng giá.", sqlException);
                }
                throw exception;
            }
        } catch (SQLException exception) {
            throw failure("Không thể lưu dịch vụ chăm sóc.", exception);
        }
    }

    private String nullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private IllegalStateException failure(String message, SQLException cause) {
        return new IllegalStateException(message, cause);
    }

    @FunctionalInterface
    private interface SqlWork<T> {
        T run(Connection connection) throws SQLException;
    }
}
