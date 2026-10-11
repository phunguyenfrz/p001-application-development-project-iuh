package com.petshop.dao;

import com.petshop.model.ShiftAssignment;
import com.petshop.util.DbConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class ShiftDao implements ShiftRepository {
    @Override
    public List<ShiftAssignment> findBetween(LocalDate from, LocalDate through) {
        String sql = """
                SELECT pc.maPhanCa, pc.maNhanVien, nv.hoTen, pc.maCa, c.tenCa,
                       pc.ngayLam, c.gioBatDau, c.gioKetThuc, pc.trangThai
                FROM PhanCa pc
                JOIN NhanVien nv ON nv.maNhanVien = pc.maNhanVien
                JOIN Ca c ON c.maCa = pc.maCa
                WHERE pc.ngayLam BETWEEN ? AND ?
                ORDER BY pc.ngayLam, c.gioBatDau, nv.hoTen
                """;
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(from));
            statement.setDate(2, Date.valueOf(through));
            try (ResultSet rows = statement.executeQuery()) {
                List<ShiftAssignment> assignments = new ArrayList<>();
                while (rows.next()) {
                    assignments.add(new ShiftAssignment(rows.getString("maPhanCa"),
                            rows.getString("maNhanVien"), rows.getString("hoTen"),
                            rows.getString("maCa"), rows.getString("tenCa"),
                            rows.getDate("ngayLam").toLocalDate(),
                            rows.getTime("gioBatDau").toLocalTime(),
                            rows.getTime("gioKetThuc").toLocalTime(),
                            rows.getString("trangThai")));
                }
                return List.copyOf(assignments);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể tải lịch phân ca.", exception);
        }
    }

    @Override
    public List<ShiftAssignment.Choice> activeEmployees() {
        return choices("SELECT maNhanVien, hoTen FROM NhanVien WHERE trangThai = N'Đang làm' ORDER BY hoTen");
    }

    @Override
    public List<ShiftAssignment.Choice> activeShifts() {
        return choices("SELECT maCa, tenCa FROM Ca WHERE trangThai = N'Áp dụng' ORDER BY gioBatDau");
    }

    @Override
    public void add(ShiftAssignment value) {
        execute("""
                INSERT INTO PhanCa (maPhanCa, maNhanVien, maCa, ngayLam, trangThai)
                VALUES (?, ?, ?, ?, N'Đã phân công')
                """, value, false);
    }

    @Override
    public void update(ShiftAssignment value) {
        execute("""
                UPDATE PhanCa SET maNhanVien = ?, maCa = ?, ngayLam = ?
                WHERE maPhanCa = ? AND ngayLam >= CAST(GETDATE() AS date)
                    AND trangThai = N'Đã phân công'
                """, value, true);
    }

    @Override
    public void deleteFuture(String id) {
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     DELETE FROM PhanCa WHERE maPhanCa = ?
                       AND ngayLam >= CAST(GETDATE() AS date)
                       AND trangThai = N'Đã phân công'
                     """)) {
            statement.setString(1, id);
            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException("Chỉ được xóa ca chưa diễn ra.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể xóa phân ca.", exception);
        }
    }

    private List<ShiftAssignment.Choice> choices(String sql) {
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            List<ShiftAssignment.Choice> result = new ArrayList<>();
            while (rows.next()) {
                result.add(new ShiftAssignment.Choice(rows.getString(1), rows.getString(2)));
            }
            return List.copyOf(result);
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể tải danh sách lựa chọn ca trực.", exception);
        }
    }

    private void execute(String sql, ShiftAssignment value, boolean updating) {
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (updating) {
                statement.setString(1, value.employeeId());
                statement.setString(2, value.shiftId());
                statement.setDate(3, Date.valueOf(value.workDate()));
                statement.setString(4, value.id());
            } else {
                statement.setString(1, value.id());
                statement.setString(2, value.employeeId());
                statement.setString(3, value.shiftId());
                statement.setDate(4, Date.valueOf(value.workDate()));
            }
            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException("Không tìm thấy phân ca có thể cập nhật.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể lưu phân ca.", exception);
        }
    }
}
