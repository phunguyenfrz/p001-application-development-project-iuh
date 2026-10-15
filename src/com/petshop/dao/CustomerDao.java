package com.petshop.dao;

import com.petshop.model.Customer;
import com.petshop.util.DatabaseManager;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Truy cập dữ liệu bảng KhachHang.
 * Hỗ trợ đọc, thêm, sửa và ngừng hoạt động khách hàng.
 */
public class CustomerDao {

    // Bộ sinh phần số ngẫu nhiên của mã khách hàng.
    private final SecureRandom random = new SecureRandom();

    /**
     * Lấy tất cả khách hàng đang hoạt động.
     *
     * @return Danh sách khách hàng đang hoạt động.
     * @throws SQLException Khi truy vấn database thất bại.
     */
    public List<Customer> findAll() throws SQLException {
        // Danh sách khách hàng trả về cho giao diện.
        List<Customer> customer_list = new ArrayList<>();

        // Chỉ đọc khách hàng chưa bị ngừng hoạt động.
        String sql_query = """
                SELECT maKhachHang, hoTen, soDienThoai, email,
                       diemTichLuy, diaChi, ghiChu
                FROM dbo.KhachHang
                WHERE trangThai = 1
                ORDER BY hoTen, maKhachHang
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql_query);
             ResultSet result_set = statement.executeQuery()) {

            // Chuyển từng dòng dữ liệu thành đối tượng Customer.
            while (result_set.next()) {
                // Thông tin khách hàng tại dòng hiện tại.
                Customer customer = new Customer(
                        result_set.getString("maKhachHang"),
                        result_set.getString("hoTen"),
                        result_set.getString("soDienThoai"),
                        result_set.getString("email"),
                        result_set.getInt("diemTichLuy"),
                        result_set.getString("diaChi"),
                        result_set.getString("ghiChu")
                );

                customer_list.add(customer);
            }
        }

        return customer_list;
    }

    /**
     * Thêm khách hàng mới; điểm tích lũy ban đầu bằng 0.
     *
     * @param customer Thông tin khách hàng cần thêm.
     * @throws SQLException Khi lưu thất bại hoặc không sinh được mã.
     */
    public void insert(Customer customer) throws SQLException {
        // Câu lệnh thêm khách hàng với trạng thái đang hoạt động.
        String sql_query = """
                INSERT INTO dbo.KhachHang (
                    maKhachHang, hoTen, soDienThoai, email,
                    diemTichLuy, diaChi, ghiChu, trangThai
                )
                VALUES (?, ?, ?, ?, 0, ?, ?, 1)
                """;

        try (Connection connection = DatabaseManager.getConnection()) {
            // Thử lại tối đa 5 lần nếu phần số của mã ngẫu nhiên bị trùng.
            for (int attempt = 0; attempt < 5; attempt++) {
                // Mã theo định dạng KH-YYYYMMDD-NNNNNN của database.
                String customer_id = generateCustomerId();

                try (PreparedStatement statement =
                             connection.prepareStatement(sql_query)) {

                    statement.setString(1, customer_id);
                    statement.setString(2, customer.getHoTen());
                    statement.setString(3, customer.getSoDienThoai());
                    statement.setString(4, customer.getEmail());
                    statement.setString(5, customer.getDiaChi());
                    statement.setString(6, customer.getGhiChu());

                    statement.executeUpdate();

                    // Cập nhật mã được sinh sau khi thêm thành công.
                    customer.setMaKhachHang(customer_id);
                    return;
                } catch (SQLException exception) {
                    // Chỉ thử sinh lại mã nếu lỗi trùng khóa đúng mã vừa tạo.
                    if ((exception.getErrorCode() == 2601
                            || exception.getErrorCode() == 2627)
                            && existsById(connection, customer_id)) {
                        continue;
                    }

                    throw exception;
                }
            }
        }

        throw new SQLException(
                "Không tạo được mã khách hàng. Vui lòng thử lại."
        );
    }

    /**
     * Sửa thông tin khách hàng đang hoạt động.
     * Không thay đổi mã và điểm tích lũy.
     *
     * @param customer Thông tin mới của khách hàng.
     * @throws SQLException Khi cập nhật thất bại.
     */
    public void update(Customer customer) throws SQLException {
        // Chỉ cập nhật thông tin liên hệ và ghi chú.
        String sql_query = """
                UPDATE dbo.KhachHang
                SET hoTen = ?, soDienThoai = ?, email = ?,
                    diaChi = ?, ghiChu = ?
                WHERE maKhachHang = ? AND trangThai = 1
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql_query)) {

            statement.setString(1, customer.getHoTen());
            statement.setString(2, customer.getSoDienThoai());
            statement.setString(3, customer.getEmail());
            statement.setString(4, customer.getDiaChi());
            statement.setString(5, customer.getGhiChu());
            statement.setString(6, customer.getMaKhachHang());

            // Báo lỗi nếu khách hàng đã bị thay đổi trạng thái hoặc không còn.
            if (statement.executeUpdate() != 1) {
                throw new SQLException(
                        "Khách hàng không còn hoạt động. Hãy làm mới danh sách."
                );
            }
        }
    }

    /**
     * Ngừng hoạt động khách hàng bằng cách đổi trạng thái.
     *
     * @param customer_id Mã khách hàng cần ngừng hoạt động.
     * @throws SQLException Khi cập nhật thất bại.
     */
    public void deactivate(String customer_id) throws SQLException {
        // Xóa mềm, không xóa bản ghi khỏi database.
        String sql_query = """
                UPDATE dbo.KhachHang
                SET trangThai = 0
                WHERE maKhachHang = ? AND trangThai = 1
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql_query)) {

            statement.setString(1, customer_id);

            // Kiểm tra khách hàng có còn hoạt động tại thời điểm cập nhật.
            if (statement.executeUpdate() != 1) {
                throw new SQLException(
                        "Khách hàng không còn hoạt động. Hãy làm mới danh sách."
                );
            }
        }
    }

    /**
     * Sinh mã theo định dạng database yêu cầu.
     *
     * @return Mã khách hàng có ngày hiện tại và phần số gồm 6 chữ số.
     */
    private String generateCustomerId() {
        // Ngày tạo mã theo định dạng năm, tháng, ngày.
        String date_text = LocalDate.now().format(
                DateTimeFormatter.BASIC_ISO_DATE
        );

        return "KH-" + date_text + "-"
                + String.format(java.util.Locale.ROOT,
                        "%06d", random.nextInt(1_000_000));
    }

    /**
     * Kiểm tra mã đã tồn tại để xử lý va chạm mã ngẫu nhiên.
     *
     * @param connection Kết nối đang sử dụng.
     * @param customer_id Mã khách hàng cần kiểm tra.
     * @return true nếu mã đã tồn tại.
     * @throws SQLException Khi truy vấn thất bại.
     */
    private boolean existsById(
            Connection connection, String customer_id
    ) throws SQLException {
        // Truy vấn kiểm tra khóa chính khách hàng.
        String sql_query = """
                SELECT 1 FROM dbo.KhachHang WHERE maKhachHang = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql_query)) {
            statement.setString(1, customer_id);

            try (ResultSet result_set = statement.executeQuery()) {
                return result_set.next();
            }
        }
    }
}