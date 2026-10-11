package com.petshop.dao;

import com.petshop.model.Employee;
import com.petshop.model.Role;
import com.petshop.util.DbConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class EmployeeDao implements EmployeeRepository {
    private static final String SELECT = """
            SELECT nv.maNhanVien, nv.hoTen, nv.soDienThoai, nv.chucVu, nv.ngaySinh,
                   nv.gioiTinh, nv.trangThai, tk.tenDangNhap, tk.trangThai AS taiKhoanHoatDong
            FROM NhanVien nv
            LEFT JOIN TaiKhoan tk ON tk.maNhanVien = nv.maNhanVien
            """;

    @Override
    public List<Employee> findAll() {
        return query(SELECT + " ORDER BY nv.hoTen", null);
    }

    @Override
    public List<Employee> search(String keyword) {
        String sql = SELECT + """
                 WHERE nv.maNhanVien LIKE ? OR nv.hoTen LIKE ? OR nv.soDienThoai LIKE ?
                    OR tk.tenDangNhap LIKE ?
                 ORDER BY nv.hoTen
                """;
        String pattern = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        return query(sql, pattern);
    }

    @Override
    public String add(Employee employee, String passwordHash) {
        String insertEmployee = """
                INSERT INTO NhanVien
                    (maNhanVien, hoTen, soDienThoai, chucVu, ngaySinh, gioiTinh, trangThai)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        String insertAccount = """
                INSERT INTO TaiKhoan
                    (maTaiKhoan, maNhanVien, tenDangNhap, matKhau, phanQuyen, trangThai)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        return transaction((connection) -> {
            String employeeId = nextCode(connection, "NV");
            String accountId = nextCode(connection, "TK");
            try (PreparedStatement statement = connection.prepareStatement(insertEmployee)) {
                statement.setString(1, employeeId);
                statement.setString(2, employee.fullName().trim());
                statement.setString(3, employee.phone().trim());
                statement.setString(4, employee.role().getDisplayName());
                statement.setDate(5, Date.valueOf(employee.birthDate()));
                statement.setBoolean(6, employee.male());
                statement.setString(7, employee.working() ? "Đang làm" : "Đã nghỉ việc");
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement(insertAccount)) {
                statement.setString(1, accountId);
                statement.setString(2, employeeId);
                statement.setString(3, employee.username().trim());
                statement.setString(4, passwordHash);
                statement.setString(5, employee.role().getDisplayName());
                statement.setBoolean(6, employee.accountActive() && employee.working());
                statement.executeUpdate();
            }
            return employeeId;
        });
    }

    @Override
    public void update(Employee employee, String passwordHash) {
        String updateEmployee = """
                UPDATE NhanVien
                SET hoTen = ?, soDienThoai = ?, chucVu = ?, ngaySinh = ?, gioiTinh = ?, trangThai = ?
                WHERE maNhanVien = ?
                """;
        String updateAccount = """
                UPDATE TaiKhoan
                SET tenDangNhap = ?, phanQuyen = ?, trangThai = ?,
                    matKhau = COALESCE(?, matKhau)
                WHERE maNhanVien = ?
                """;
        transaction(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(updateEmployee)) {
                statement.setString(1, employee.fullName().trim());
                statement.setString(2, employee.phone().trim());
                statement.setString(3, employee.role().getDisplayName());
                statement.setDate(4, Date.valueOf(employee.birthDate()));
                statement.setBoolean(5, employee.male());
                statement.setString(6, employee.working() ? "Đang làm" : "Đã nghỉ việc");
                statement.setString(7, employee.id());
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement(updateAccount)) {
                statement.setString(1, employee.username().trim());
                statement.setString(2, employee.role().getDisplayName());
                statement.setBoolean(3, employee.accountActive() && employee.working());
                statement.setString(4, passwordHash);
                statement.setString(5, employee.id());
                if (statement.executeUpdate() == 0) {
                    throw new IllegalStateException("Không tìm thấy tài khoản nhân viên cần cập nhật.");
                }
            }
            return null;
        });
    }

    @Override
    public void deactivate(String id) {
        transaction(connection -> {
            try (PreparedStatement employee = connection.prepareStatement(
                    "UPDATE NhanVien SET trangThai = N'Đã nghỉ việc' WHERE maNhanVien = ?")) {
                employee.setString(1, id);
                if (employee.executeUpdate() == 0) {
                    throw new IllegalArgumentException("Không tìm thấy nhân viên cần ngừng.");
                }
            }
            try (PreparedStatement account = connection.prepareStatement(
                    "UPDATE TaiKhoan SET trangThai = 0 WHERE maNhanVien = ?")) {
                account.setString(1, id);
                account.executeUpdate();
            }
            return null;
        });
    }

    private List<Employee> query(String sql, String searchPattern) {
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (searchPattern != null) {
                for (int parameter = 1; parameter <= 4; parameter++) {
                    statement.setString(parameter, searchPattern);
                }
            }
            try (ResultSet rows = statement.executeQuery()) {
                List<Employee> employees = new ArrayList<>();
                while (rows.next()) {
                    employees.add(new Employee(
                            rows.getString("maNhanVien"),
                            rows.getString("hoTen"),
                            rows.getString("soDienThoai"),
                            Role.fromDbValue(rows.getString("chucVu")),
                            rows.getDate("ngaySinh").toLocalDate(),
                            rows.getBoolean("gioiTinh"),
                            "Đang làm".equals(rows.getString("trangThai")),
                            rows.getString("tenDangNhap"),
                            rows.getBoolean("taiKhoanHoatDong")));
                }
                return List.copyOf(employees);
            }
        } catch (SQLException exception) {
            throw databaseFailure("Không thể tải danh sách nhân viên.", exception);
        }
    }

    private String nextCode(Connection connection, String prefix) throws SQLException {
        String day = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String sql = "SELECT ISNULL(MAX(TRY_CONVERT(int, RIGHT(maNhanVien, 6))), 0) "
                + "FROM NhanVien WITH (UPDLOCK, HOLDLOCK) WHERE maNhanVien LIKE ?";
        if ("TK".equals(prefix)) {
            sql = "SELECT ISNULL(MAX(TRY_CONVERT(int, RIGHT(maTaiKhoan, 6))), 0) "
                    + "FROM TaiKhoan WITH (UPDLOCK, HOLDLOCK) WHERE maTaiKhoan LIKE ?";
        }
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, prefix + "-" + day + "-%");
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                int next = result.getInt(1) + 1;
                if (next > 999_999) {
                    throw new IllegalStateException("Đã vượt số mã tối đa trong ngày.");
                }
                return prefix + "-" + day + "-" + String.format("%06d", next);
            }
        }
    }

    private <T> T transaction(SqlWork<T> work) {
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
                    throw databaseFailure("Không thể lưu thông tin nhân viên. "
                            + "Hãy kiểm tra tên đăng nhập và số điện thoại.", sqlException);
                }
                throw exception;
            }
        } catch (SQLException exception) {
            throw databaseFailure("Không thể cập nhật dữ liệu nhân viên.", exception);
        }
    }

    private IllegalStateException databaseFailure(String message, SQLException exception) {
        return new IllegalStateException(message, exception);
    }

    @FunctionalInterface
    private interface SqlWork<T> {
        T run(Connection connection) throws SQLException;
    }
}
