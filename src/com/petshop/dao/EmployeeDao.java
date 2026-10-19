package com.petshop.dao;

import com.petshop.model.Employee;
import com.petshop.util.DatabaseManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Truy cập database cho hồ sơ nhân viên.
 *
 * Không xóa nhân viên khỏi database.
 * Khi nhân viên nghỉ việc, cập nhật trạng thái và khóa tài khoản
 * trong cùng một transaction.
 */
public class EmployeeDao {

    /**
     * Lấy danh sách nhân viên cùng thông tin tài khoản.
     *
     * Tải ảnh để hiển thị avatar trong danh sách nhân viên.
     *
     * @return Danh sách nhân viên.
     * @throws SQLException Khi truy vấn thất bại.
     */
    public List<Employee> findAll() throws SQLException {
        List<Employee> employees = new ArrayList<>();

        String sql_query = selectSql(true)
                + " ORDER BY nv.maNhanVien DESC";

        try (Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql_query);
                ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                employees.add(mapEmployee(result));
            }
        }

        return employees;
    }

    /**
     * Lấy đầy đủ hồ sơ một nhân viên, bao gồm ảnh.
     *
     * @param employee_id Mã nhân viên.
     * @return Hồ sơ hoặc null nếu không tồn tại.
     * @throws SQLException Khi truy vấn thất bại.
     */
    public Employee findById(String employee_id) throws SQLException {
        String sql_query = selectSql(true)
                + " WHERE nv.maNhanVien = ?";

        try (Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql_query)) {

            statement.setString(1, employee_id);

            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapEmployee(result) : null;
            }
        }
    }

    /**
     * Thêm nhân viên và tự tạo mã theo ngày.
     *
     * Khóa dải mã trong transaction để hạn chế trùng mã khi
     * nhiều người cùng thêm nhân viên.
     *
     * Không tự tạo tài khoản đăng nhập.
     *
     * @param employee Hồ sơ đã được Service kiểm tra.
     * @param actor_id Mã người đang thực hiện thao tác.
     * @param today Ngày hiện tại tại Việt Nam.
     * @return Mã nhân viên mới.
     * @throws SQLException Khi thêm thất bại.
     */
    public String insert(
            Employee employee,
            String actor_id,
            LocalDate today
    ) throws SQLException {

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);

            try {
                checkActor(connection, actor_id);

                String prefix = "NV-"
                        + today.format(DateTimeFormatter.BASIC_ISO_DATE)
                        + "-";

                int next_number;

                String sql_next_number = """
                        SELECT ISNULL(
                            MAX(TRY_CONVERT(INT, RIGHT(maNhanVien, 6))),
                            0
                        ) + 1
                        FROM dbo.NhanVien WITH (UPDLOCK, HOLDLOCK)
                        WHERE maNhanVien LIKE ?
                        """;

                try (PreparedStatement statement =
                        connection.prepareStatement(sql_next_number)) {

                    statement.setString(1, prefix + "%");

                    try (ResultSet result = statement.executeQuery()) {
                        result.next();
                        next_number = result.getInt(1);
                    }
                }

                if (next_number > 999999) {
                    throw new SQLException(
                            "Đã hết dải mã nhân viên trong ngày."
                    );
                }

                String new_id = prefix
                        + String.format(
                                Locale.ROOT,
                                "%06d",
                                next_number
                        );

                String sql_insert = """
                        INSERT INTO dbo.NhanVien (
                            hoTen,
                            soDienThoai,
                            email,
                            diaChi,
                            chucVu,
                            ngaySinh,
                            gioiTinh,
                            ngayBatDau,
                            trangThai,
                            anh,
                            maNhanVien
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """;

                try (PreparedStatement statement =
                        connection.prepareStatement(sql_insert)) {

                    setParameters(statement, employee);
                    statement.setString(11, new_id);

                    statement.executeUpdate();
                }

                connection.commit();
                return new_id;

            } catch (SQLException | RuntimeException error) {
                connection.rollback();
                throw error;
            }
        }
    }

    /**
     * Cập nhật hồ sơ nhân viên đang làm việc.
     *
     * Không cho sửa chính mình hoặc tài khoản Admin.
     * Chức vụ trong hồ sơ không thay đổi quyền đăng nhập.
     *
     * @param employee Hồ sơ đã được Service kiểm tra.
     * @param actor_id Mã người thực hiện thao tác.
     * @throws SQLException Khi cập nhật thất bại.
     */
    public void update(
            Employee employee,
            String actor_id
    ) throws SQLException {

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);

            try {
                checkActor(connection, actor_id);

                String sql_update = """
                        UPDATE nv
                        SET hoTen = ?,
                            soDienThoai = ?,
                            email = ?,
                            diaChi = ?,
                            chucVu = ?,
                            ngaySinh = ?,
                            gioiTinh = ?,
                            ngayBatDau = ?,
                            trangThai = ?,
                            anh = ?
                        FROM dbo.NhanVien nv
                        WHERE nv.maNhanVien = ?
                          AND nv.maNhanVien <> ?
                          AND nv.trangThai = N'Đang làm'
                          AND NOT EXISTS (
                              SELECT 1
                              FROM dbo.TaiKhoan tk
                                  WITH (UPDLOCK, HOLDLOCK)
                              WHERE tk.maNhanVien = nv.maNhanVien
                                AND tk.phanQuyen = N'Admin'
                          )
                        """;

                try (PreparedStatement statement =
                        connection.prepareStatement(sql_update)) {

                    setParameters(statement, employee);

                    statement.setString(
                            11,
                            employee.maNhanVien()
                    );
                    statement.setString(12, actor_id);

                    if (statement.executeUpdate() != 1) {
                        throw new SQLException(
                                "Không thể sửa chính mình, Admin "
                                + "hoặc nhân viên đã nghỉ. "
                                + "Hãy làm mới danh sách."
                        );
                    }
                }

                // Nghỉ việc thì khóa tài khoản trong cùng transaction.
                if ("Đã nghỉ việc".equals(employee.trangThai())) {
                    lockAccount(
                            connection,
                            employee.maNhanVien()
                    );
                }

                connection.commit();

            } catch (SQLException | RuntimeException error) {
                connection.rollback();
                throw error;
            }
        }
    }

    /**
     * Ngừng làm việc bằng cách cập nhật trạng thái.
     *
     * Giữ nguyên nhân viên để bảo toàn liên kết với lịch hẹn,
     * hóa đơn và ca trực.
     *
     * @param employee_id Mã nhân viên cần ngừng làm việc.
     * @param actor_id Mã người thực hiện thao tác.
     * @throws SQLException Khi cập nhật thất bại.
     */
    public void deactivate(
            String employee_id,
            String actor_id
    ) throws SQLException {

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);

            try {
                checkActor(connection, actor_id);

                String sql_update = """
                        UPDATE nv
                        SET trangThai = N'Đã nghỉ việc'
                        FROM dbo.NhanVien nv
                        WHERE nv.maNhanVien = ?
                          AND nv.maNhanVien <> ?
                          AND nv.trangThai = N'Đang làm'
                          AND NOT EXISTS (
                              SELECT 1
                              FROM dbo.TaiKhoan tk
                                  WITH (UPDLOCK, HOLDLOCK)
                              WHERE tk.maNhanVien = nv.maNhanVien
                                AND tk.phanQuyen = N'Admin'
                          )
                        """;

                try (PreparedStatement statement =
                        connection.prepareStatement(sql_update)) {

                    statement.setString(1, employee_id);
                    statement.setString(2, actor_id);

                    if (statement.executeUpdate() != 1) {
                        throw new SQLException(
                                "Không thể ngừng làm việc cho chính mình, "
                                + "Admin hoặc nhân viên đã nghỉ. "
                                + "Hãy làm mới danh sách."
                        );
                    }
                }

                lockAccount(connection, employee_id);

                connection.commit();

            } catch (SQLException | RuntimeException error) {
                connection.rollback();
                throw error;
            }
        }
    }

    /**
     * Tạo phần SELECT dùng chung.
     *
     * LEFT JOIN để nhân viên chưa có tài khoản vẫn xuất hiện.
     *
     * @param with_photo Có tải dữ liệu ảnh hay không.
     * @return Câu SELECT chưa có WHERE hoặc ORDER BY.
     */
    private String selectSql(boolean with_photo) {
        String photo_column = with_photo
                ? "nv.anh"
                : "CAST(NULL AS VARBINARY(MAX)) AS anh";

        return """
                SELECT nv.maNhanVien,
                       nv.hoTen,
                       nv.soDienThoai,
                       nv.email,
                       nv.diaChi,
                       nv.chucVu,
                       nv.ngaySinh,
                       nv.gioiTinh,
                       nv.ngayBatDau,
                       nv.trangThai,
                """
                + photo_column
                + """
                        , tk.tenDangNhap,
                          tk.phanQuyen,
                          tk.trangThai AS taiKhoanHoatDong
                        FROM dbo.NhanVien nv
                        LEFT JOIN dbo.TaiKhoan tk
                            ON tk.maNhanVien = nv.maNhanVien
                        """;
    }

    /**
     * Chuyển một dòng dữ liệu thành model.
     *
     * @param result Dòng kết quả hiện tại.
     * @return Hồ sơ nhân viên.
     * @throws SQLException Khi đọc dữ liệu thất bại.
     */
    private Employee mapEmployee(ResultSet result)
            throws SQLException {

        Date start_date = result.getDate("ngayBatDau");

        return new Employee(
                result.getString("maNhanVien"),
                result.getString("hoTen"),
                result.getString("soDienThoai"),
                result.getString("email"),
                result.getString("diaChi"),
                result.getString("chucVu"),
                result.getDate("ngaySinh").toLocalDate(),
                result.getBoolean("gioiTinh"),
                start_date == null
                        ? null
                        : start_date.toLocalDate(),
                result.getString("trangThai"),
                result.getBytes("anh"),
                result.getString("tenDangNhap"),
                result.getString("phanQuyen"),
                (Boolean) result.getObject("taiKhoanHoatDong")
        );
    }

    /**
     * Gán 10 tham số hồ sơ dùng chung cho INSERT và UPDATE.
     *
     * @param statement Lệnh SQL đang chuẩn bị.
     * @param employee Hồ sơ đã được kiểm tra.
     * @throws SQLException Khi gán tham số thất bại.
     */
    private void setParameters(
            PreparedStatement statement,
            Employee employee
    ) throws SQLException {

        statement.setNString(1, employee.hoTen());
        statement.setString(2, employee.soDienThoai());
        statement.setString(3, employee.email());
        statement.setNString(4, employee.diaChi());
        statement.setNString(5, employee.chucVu());

        statement.setDate(
                6,
                Date.valueOf(employee.ngaySinh())
        );

        statement.setBoolean(7, employee.nam());

        statement.setDate(
                8,
                Date.valueOf(employee.ngayBatDau())
        );

        statement.setNString(9, employee.trangThai());
        statement.setBytes(10, employee.anh());
    }

    /**
     * Khóa tài khoản của nhân viên nghỉ việc.
     *
     * Nhân viên chưa có tài khoản thì không có dòng nào thay đổi.
     *
     * @param connection Connection của transaction hiện tại.
     * @param employee_id Mã nhân viên.
     * @throws SQLException Khi cập nhật thất bại.
     */
    private void lockAccount(
            Connection connection,
            String employee_id
    ) throws SQLException {

        String sql_update = """
                UPDATE dbo.TaiKhoan
                SET trangThai = 0
                WHERE maNhanVien = ?
                """;

        try (PreparedStatement statement =
                connection.prepareStatement(sql_update)) {

            statement.setString(1, employee_id);
            statement.executeUpdate();
        }
    }

    /**
     * Kiểm tra lại quyền người thao tác trong database.
     *
     * Ngăn phiên đăng nhập cũ tiếp tục ghi dữ liệu sau khi
     * tài khoản đã bị khóa, nghỉ việc hoặc đổi quyền.
     *
     * @param connection Connection của transaction hiện tại.
     * @param actor_id Mã nhân viên đang thao tác.
     * @throws SQLException Khi người thao tác không còn quyền.
     */
    private void checkActor(
            Connection connection,
            String actor_id
    ) throws SQLException {

        String sql_query = """
                SELECT 1
                FROM dbo.TaiKhoan tk WITH (UPDLOCK, HOLDLOCK)
                JOIN dbo.NhanVien nv WITH (UPDLOCK, HOLDLOCK)
                    ON nv.maNhanVien = tk.maNhanVien
                WHERE tk.maNhanVien = ?
                  AND tk.trangThai = 1
                  AND nv.trangThai = N'Đang làm'
                  AND tk.phanQuyen IN (N'Admin', N'Quản lý')
                """;

        try (PreparedStatement statement =
                connection.prepareStatement(sql_query)) {

            statement.setString(1, actor_id);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException(
                            "Tài khoản hiện tại không còn quyền "
                            + "cập nhật nhân viên. Hãy đăng nhập lại."
                    );
                }
            }
        }
    }

    /**
     * Lấy danh sách nhân viên đang làm việc (phục vụ chọn nhân viên phân ca hoặc bàn giao).
     */
    public List<Employee> findActiveEmployees() throws SQLException {
        List<Employee> employees = new ArrayList<>();
        String sql_query = selectSql(true)
                + " WHERE nv.trangThai = N'Đang làm' ORDER BY nv.hoTen ASC";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql_query);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                employees.add(mapEmployee(result));
            }
        }
        return employees;
    }

    /**
     * Cấp tài khoản đăng nhập mới cho nhân viên.
     */
    public String insertAccount(
            String maNhanVien,
            String tenDangNhap,
            String matKhau,
            String phanQuyen,
            String actorId,
            LocalDate today
    ) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                checkActor(connection, actorId);

                // Kiểm tra tên đăng nhập đã tồn tại chưa
                String sqlCheckUsername = "SELECT COUNT(*) FROM dbo.TaiKhoan WHERE LOWER(tenDangNhap) = LOWER(?)";
                try (PreparedStatement ps = connection.prepareStatement(sqlCheckUsername)) {
                    ps.setString(1, tenDangNhap.trim());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            throw new SQLException("Tên đăng nhập [" + tenDangNhap.trim() + "] đã được sử dụng. Vui lòng chọn tên khác.");
                        }
                    }
                }

                // Kiểm tra nhân viên đã có tài khoản chưa
                String sqlCheckEmployee = "SELECT COUNT(*) FROM dbo.TaiKhoan WHERE maNhanVien = ?";
                try (PreparedStatement ps = connection.prepareStatement(sqlCheckEmployee)) {
                    ps.setString(1, maNhanVien);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            throw new SQLException("Nhân viên này đã được cấp tài khoản.");
                        }
                    }
                }

                // Sinh mã tài khoản tự động TK-YYYYMMDD-NNNNNN
                String prefix = "TK-" + today.format(DateTimeFormatter.BASIC_ISO_DATE) + "-";
                int nextNumber;
                String sqlNext = """
                        SELECT ISNULL(MAX(TRY_CONVERT(INT, RIGHT(maTaiKhoan, 6))), 0) + 1
                        FROM dbo.TaiKhoan WITH (UPDLOCK, HOLDLOCK)
                        WHERE maTaiKhoan LIKE ?
                        """;
                try (PreparedStatement ps = connection.prepareStatement(sqlNext)) {
                    ps.setString(1, prefix + "%");
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        nextNumber = rs.getInt(1);
                    }
                }

                String newAccountId = prefix + String.format(Locale.ROOT, "%06d", nextNumber);

                String sqlInsert = """
                        INSERT INTO dbo.TaiKhoan (maTaiKhoan, maNhanVien, tenDangNhap, matKhau, phanQuyen, trangThai)
                        VALUES (?, ?, ?, ?, ?, 1)
                        """;
                try (PreparedStatement ps = connection.prepareStatement(sqlInsert)) {
                    ps.setString(1, newAccountId);
                    ps.setString(2, maNhanVien);
                    ps.setString(3, tenDangNhap.trim());
                    ps.setString(4, matKhau);
                    ps.setString(5, phanQuyen);
                    ps.executeUpdate();
                }

                connection.commit();
                return newAccountId;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    /**
     * Cập nhật quyền hạn cho tài khoản nhân viên.
     */
    public void updateRole(
            String maNhanVien,
            String phanQuyen,
            String actorId
    ) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                checkActor(connection, actorId);

                if (maNhanVien.equals(actorId) && !"Admin".equalsIgnoreCase(phanQuyen)) {
                    throw new SQLException("Không thể tự hạ quyền quản trị của chính mình.");
                }

                String sqlUpdate = """
                        UPDATE dbo.TaiKhoan
                        SET phanQuyen = ?
                        WHERE maNhanVien = ?
                        """;
                try (PreparedStatement ps = connection.prepareStatement(sqlUpdate)) {
                    ps.setString(1, phanQuyen);
                    ps.setString(2, maNhanVien);
                    if (ps.executeUpdate() < 1) {
                        throw new SQLException("Nhân viên này chưa có tài khoản để cập nhật phân quyền.");
                    }
                }

                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    /**
     * Bật/tắt trạng thái hoạt động của tài khoản (Khóa / Mở khóa).
     */
    public void toggleAccountStatus(
            String maNhanVien,
            boolean active,
            String actorId
    ) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                checkActor(connection, actorId);

                if (maNhanVien.equals(actorId) && !active) {
                    throw new SQLException("Không thể tự khóa tài khoản của chính mình.");
                }

                String sqlUpdate = """
                        UPDATE dbo.TaiKhoan
                        SET trangThai = ?
                        WHERE maNhanVien = ?
                        """;
                try (PreparedStatement ps = connection.prepareStatement(sqlUpdate)) {
                    ps.setInt(1, active ? 1 : 0);
                    ps.setString(2, maNhanVien);
                    if (ps.executeUpdate() < 1) {
                        throw new SQLException("Không tìm thấy tài khoản tương ứng với nhân viên này.");
                    }
                }

                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    /**
     * Đặt lại mật khẩu tài khoản cho nhân viên.
     */
    public void resetPassword(
            String maNhanVien,
            String newPassword,
            String actorId
    ) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                checkActor(connection, actorId);

                String sqlUpdate = """
                        UPDATE dbo.TaiKhoan
                        SET matKhau = ?
                        WHERE maNhanVien = ?
                        """;
                try (PreparedStatement ps = connection.prepareStatement(sqlUpdate)) {
                    ps.setString(1, newPassword);
                    ps.setString(2, maNhanVien);
                    if (ps.executeUpdate() < 1) {
                        throw new SQLException("Nhân viên này chưa có tài khoản để đặt lại mật khẩu.");
                    }
                }

                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        }
    }
}