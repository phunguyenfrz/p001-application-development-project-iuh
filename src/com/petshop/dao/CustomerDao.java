package com.petshop.dao;

import com.petshop.model.Customer;
import com.petshop.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Truy cập dữ liệu khách hàng trong bảng KhachHang.
 * Hỗ trợ đọc, thêm, sửa và xóa hẳn khách hàng.
 */
public class CustomerDao {

    /**
     * Lấy toàn bộ khách hàng, sắp xếp theo họ tên và mã.
     *
     * @return Danh sách khách hàng; rỗng nếu chưa có dữ liệu.
     * @throws SQLException Khi kết nối hoặc truy vấn thất bại.
     */
    public List<Customer> findAll() throws SQLException {
        // Danh sách chứa khách hàng đọc từ database.
        List<Customer> customer_list = new ArrayList<>();

        // Đọc đúng các cột trong cấu trúc SQL ban đầu.
        String sql_query = """
                SELECT maKhachHang, hoTen, soDienThoai, email,
                       diemTichLuy, diaChi, ghiChu
                FROM dbo.KhachHang
                ORDER BY hoTen, maKhachHang
                """;

        /*
         * Tự đóng kết nối, câu lệnh và kết quả sau khi sử dụng.
         * Lỗi SQL được chuyển lên Service để xử lý.
         */
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql_query);
             ResultSet result_set = statement.executeQuery()) {

            // Chuyển từng dòng SQL thành đối tượng Customer.
            while (result_set.next()) {
                customer_list.add(mapCustomer(result_set));
            }
        }

        return customer_list;
    }

    /**
     * Thêm khách hàng mới.
     * Service cung cấp mã khách hàng và đặt điểm tích lũy bằng 0.
     *
     * @param customer Khách hàng cần thêm.
     * @throws SQLException Khi thêm dữ liệu thất bại.
     */
    public void insert(Customer customer) throws SQLException {
        // Dấu ? là vị trí nhận tham số, không nối dữ liệu vào SQL.
        String sql_query = """
                INSERT INTO dbo.KhachHang (
                    maKhachHang, hoTen, soDienThoai, email,
                    diemTichLuy, diaChi, ghiChu
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql_query)) {

            statement.setString(1, customer.getMaKhachHang());
            statement.setString(2, customer.getHoTen());
            statement.setString(3, customer.getSoDienThoai());
            statement.setString(4, customer.getEmail());
            statement.setInt(5, customer.getDiemTichLuy());
            statement.setString(6, customer.getDiaChi());
            statement.setString(7, customer.getGhiChu());

            // Một lần thêm phải tạo đúng một bản ghi.
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Không thêm được khách hàng.");
            }
        }
    }

    /**
     * Sửa thông tin khách hàng theo mã.
     * Không thay đổi mã khách hàng và điểm tích lũy.
     *
     * @param customer Khách hàng chứa thông tin mới.
     * @throws SQLException Khi cập nhật thất bại.
     */
    public void update(Customer customer) throws SQLException {
        // Chỉ sửa các thông tin được phép nhập trên form.
        String sql_query = """
                UPDATE dbo.KhachHang
                SET hoTen = ?,
                    soDienThoai = ?,
                    email = ?,
                    diaChi = ?,
                    ghiChu = ?
                WHERE maKhachHang = ?
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

            // Khách hàng có thể đã bị xóa bởi người dùng khác.
            if (statement.executeUpdate() != 1) {
                throw new SQLException(
                        "Khách hàng không còn tồn tại. Hãy làm mới danh sách."
                );
            }
        }
    }

    /**
     * Xóa hẳn khách hàng theo mã.
     * Khóa ngoại của database chặn xóa nếu có dữ liệu liên quan.
     *
     * @param customer_id Mã khách hàng cần xóa.
     * @throws SQLException Khi xóa thất bại hoặc vi phạm khóa ngoại.
     */
    public void delete(String customer_id) throws SQLException {
        // Chỉ xóa bản ghi có mã trùng tham số được truyền vào.
        String sql_query = """
                DELETE FROM dbo.KhachHang
                WHERE maKhachHang = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql_query)) {

            statement.setString(1, customer_id);

            // Báo lỗi nếu không có khách hàng nào được xóa.
            if (statement.executeUpdate() != 1) {
                throw new SQLException(
                        "Khách hàng không còn tồn tại. Hãy làm mới danh sách."
                );
            }
        }
    }

    /**
     * Chuyển dòng dữ liệu hiện tại thành đối tượng Customer.
     *
     * @param result_set Kết quả truy vấn đang trỏ tới một dòng hợp lệ.
     * @return Khách hàng được tạo từ dòng dữ liệu.
     * @throws SQLException Khi đọc dữ liệu thất bại.
     */
    private Customer mapCustomer(ResultSet result_set)
            throws SQLException {

        return new Customer(
                result_set.getString("maKhachHang"),
                result_set.getString("hoTen"),
                result_set.getString("soDienThoai"),
                result_set.getString("email"),
                result_set.getInt("diemTichLuy"),
                result_set.getString("diaChi"),
                result_set.getString("ghiChu")
        );
    }
}