package com.petshop.dao;

import com.petshop.model.ReportData.Result;
import com.petshop.model.ReportData.Row;
import com.petshop.model.ReportData.Type;
import com.petshop.model.ReportData.Period;
import com.petshop.util.DbConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class ReportDao implements ReportRepository {
    @Override
    public Result load(Type type, Period period, LocalDate startDate, LocalDate endDate) {
        return switch (type) {
            case REVENUE -> loadRevenue(period, startDate, endDate);
            case INVENTORY -> loadInventory(period, startDate, endDate);
            case BEST_SELLERS -> loadBestSellers(period, startDate, endDate);
        };
    }

    private Result loadRevenue(Period period, LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT CAST(ngayLap AS date) AS businessDate,
                       COUNT(*) AS invoiceCount, COALESCE(SUM(tongTien), 0) AS revenue
                FROM HoaDon
                WHERE CAST(ngayLap AS date) BETWEEN ? AND ?
                  AND trangThaiThanhToan = N'Đã thanh toán'
                GROUP BY CAST(ngayLap AS date)
                ORDER BY businessDate
                """;
        return query(sql, Type.REVENUE, period, startDate, endDate, statement -> {
        }, result -> new Row("Hóa đơn", result.getString("businessDate"),
                result.getLong("invoiceCount"), result.getBigDecimal("revenue")));
    }

    private Result loadInventory(Period period, LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT tenSanPham, soLuongTon, nguongTonToiThieu, giaBan
                FROM SanPham
                WHERE trangThai = 1
                ORDER BY CASE WHEN soLuongTon < nguongTonToiThieu THEN 0 ELSE 1 END,
                         tenSanPham
                """;
        return query(sql, Type.INVENTORY, period, startDate, endDate, statement -> {
        }, result -> new Row(result.getInt("soLuongTon") < result.getInt("nguongTonToiThieu")
                ? "Dưới ngưỡng" : "Đủ tồn", result.getString("tenSanPham"),
                result.getInt("soLuongTon"), result.getBigDecimal("giaBan")));
    }

    private Result loadBestSellers(Period period, LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT category, itemName, SUM(quantity) AS sold, SUM(amount) AS revenue
                FROM (
                    SELECT N'Sản phẩm' AS category, sp.tenSanPham AS itemName,
                           ct.soLuong AS quantity, ct.soLuong * ct.donGia AS amount
                    FROM ChiTietHoaDon_SanPham ct
                    JOIN HoaDon hd ON hd.maHoaDon = ct.maHoaDon
                    JOIN SanPham sp ON sp.maSanPham = ct.maSanPham
                    WHERE CAST(hd.ngayLap AS date) BETWEEN ? AND ?
                      AND hd.trangThaiThanhToan = N'Đã thanh toán'
                    UNION ALL
                    SELECT N'Dịch vụ', dv.tenDichVu, ct.soLuong, ct.soLuong * ct.donGia
                    FROM ChiTietHoaDon_DichVu ct
                    JOIN HoaDon hd ON hd.maHoaDon = ct.maHoaDon
                    JOIN DichVuChamSoc dv ON dv.maDichVu = ct.maDichVu
                    WHERE CAST(hd.ngayLap AS date) BETWEEN ? AND ?
                      AND hd.trangThaiThanhToan = N'Đã thanh toán'
                ) saleLines
                GROUP BY category, itemName
                ORDER BY sold DESC, revenue DESC
                """;
        return query(sql, Type.BEST_SELLERS, period, startDate, endDate, statement -> {
            statement.setDate(3, java.sql.Date.valueOf(startDate));
            statement.setDate(4, java.sql.Date.valueOf(endDate));
        }, result -> new Row(result.getString("category"), result.getString("itemName"),
                result.getLong("sold"), result.getBigDecimal("revenue")));
    }

    private Result query(
            String sql, Type type, Period period, LocalDate startDate, LocalDate endDate,
            SqlBinder extraBinder, RowMapper mapper) {
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (type != Type.INVENTORY) {
                statement.setDate(1, java.sql.Date.valueOf(startDate));
                statement.setDate(2, java.sql.Date.valueOf(endDate));
            }
            extraBinder.bind(statement);
            try (ResultSet result = statement.executeQuery()) {
                List<Row> rows = new ArrayList<>();
                while (result.next()) {
                    rows.add(mapper.map(result));
                }
                return new Result(type, period, startDate, endDate, rows);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể tải báo cáo " + type.label() + ".", exception);
        }
    }

    @FunctionalInterface
    private interface SqlBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }

    @FunctionalInterface
    private interface RowMapper {
        Row map(ResultSet result) throws SQLException;
    }
}
