package com.petshop.dao;

import com.petshop.model.CustomerTransaction;
import com.petshop.model.CustomerTransactionItem;
import com.petshop.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Truy cập dữ liệu lịch sử giao dịch của khách hàng.
 * Chỉ đọc dữ liệu, không thêm, sửa hoặc xóa hóa đơn.
 */
public class CustomerTransactionDao {

    /**
     * Lấy hóa đơn của khách hàng theo điều kiện lọc.
     * Kết quả được sắp xếp ngày lập mới nhất trước.
     *
     * @param customer_id Mã khách hàng cần xem lịch sử.
     * @param from_date Ngày bắt đầu; null nếu không giới hạn.
     * @param to_date Ngày kết thúc; null nếu không giới hạn.
     * @param payment_status Trạng thái; null nếu lấy tất cả.
     * @return Danh sách hóa đơn phù hợp.
     * @throws SQLException Khi truy vấn database thất bại.
     */
    public List<CustomerTransaction> findByCustomer(
            String customer_id,
            LocalDate from_date,
            LocalDate to_date,
            String payment_status
    ) throws SQLException {

        // Danh sách hóa đơn trả về cho tầng Service.
        List<CustomerTransaction> transaction_list =
                new ArrayList<>();

        // Câu SQL gốc luôn giới hạn theo khách hàng được chọn.
        StringBuilder sql_query = new StringBuilder("""
                SELECT hd.maHoaDon,
                       hd.maKhachHang,
                       kh.hoTen AS tenKhachHang,
                       hd.ngayLap,
                       nv.hoTen AS tenNhanVien,
                       hd.hinhThucThanhToan,
                       hd.trangThaiThanhToan,
                       hd.tongTien
                FROM dbo.HoaDon hd
                JOIN dbo.KhachHang kh
                    ON kh.maKhachHang = hd.maKhachHang
                JOIN dbo.NhanVien nv
                    ON nv.maNhanVien = hd.maNhanVien
                WHERE hd.maKhachHang = ?
                """);

        // Thêm điều kiện ngày bắt đầu nếu người dùng đã chọn.
        if (from_date != null) {
            sql_query.append(" AND hd.ngayLap >= ?");
        }

        // Bao gồm cả các hóa đơn được lập trong ngày kết thúc.
        if (to_date != null) {
            sql_query.append(" AND hd.ngayLap <= ?");
        }

        // Service truyền null khi người dùng chọn Tất cả.
        if (payment_status != null) {
            sql_query.append(" AND hd.trangThaiThanhToan = ?");
        }

        // Mã hóa đơn giúp thứ tự ổn định khi các hóa đơn cùng ngày.
        sql_query.append(
                " ORDER BY hd.ngayLap DESC, hd.maHoaDon DESC"
        );

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql_query.toString())) {

            // Vị trí tham số thay đổi theo các điều kiện được thêm.
            int parameter_index = 1;

            statement.setString(parameter_index++, customer_id);

            if (from_date != null) {
                statement.setDate(
                        parameter_index++,
                        java.sql.Date.valueOf(from_date)
                );
            }

            if (to_date != null) {
                statement.setDate(
                        parameter_index++,
                        java.sql.Date.valueOf(to_date)
                );
            }

            if (payment_status != null) {
                statement.setNString(
                        parameter_index++,
                        payment_status
                );
            }

            try (ResultSet result_set = statement.executeQuery()) {
                // Chuyển từng dòng SQL thành giao dịch khách hàng.
                while (result_set.next()) {
                    transaction_list.add(
                            mapTransaction(result_set)
                    );
                }
            }
        }

        return transaction_list;
    }

    /**
     * Lấy các sản phẩm trong hóa đơn của khách hàng.
     *
     * @param customer_id Mã khách hàng sở hữu hóa đơn.
     * @param invoice_id Mã hóa đơn cần xem chi tiết.
     * @return Danh sách sản phẩm; rỗng nếu không có.
     * @throws SQLException Khi truy vấn database thất bại.
     */
    public List<CustomerTransactionItem> findProducts(
            String customer_id,
            String invoice_id
    ) throws SQLException {

        /*
         * Đơn giá lấy từ chi tiết hóa đơn, không lấy giá bán hiện tại.
         * Điều kiện khách hàng ngăn đọc nhầm hóa đơn của người khác.
         */
        String sql_query = """
                SELECT sp.maSanPham AS maDoiTuong,
                       sp.tenSanPham AS tenDoiTuong,
                       ct.soLuong,
                       ct.donGia
                FROM dbo.ChiTietHoaDon_SanPham ct
                JOIN dbo.SanPham sp
                    ON sp.maSanPham = ct.maSanPham
                JOIN dbo.HoaDon hd
                    ON hd.maHoaDon = ct.maHoaDon
                WHERE hd.maKhachHang = ?
                  AND hd.maHoaDon = ?
                ORDER BY sp.tenSanPham, sp.maSanPham
                """;

        return findItems(sql_query, customer_id, invoice_id);
    }

    /**
     * Lấy các dịch vụ trong hóa đơn của khách hàng.
     *
     * @param customer_id Mã khách hàng sở hữu hóa đơn.
     * @param invoice_id Mã hóa đơn cần xem chi tiết.
     * @return Danh sách dịch vụ; rỗng nếu không có.
     * @throws SQLException Khi truy vấn database thất bại.
     */
    public List<CustomerTransactionItem> findServices(
            String customer_id,
            String invoice_id
    ) throws SQLException {

        // Đọc số lượng và đơn giá đã lưu trong chi tiết hóa đơn.
        String sql_query = """
                SELECT dv.maDichVu AS maDoiTuong,
                       dv.tenDichVu AS tenDoiTuong,
                       ct.soLuong,
                       ct.donGia
                FROM dbo.ChiTietHoaDon_DichVu ct
                JOIN dbo.DichVuChamSoc dv
                    ON dv.maDichVu = ct.maDichVu
                JOIN dbo.HoaDon hd
                    ON hd.maHoaDon = ct.maHoaDon
                WHERE hd.maKhachHang = ?
                  AND hd.maHoaDon = ?
                ORDER BY dv.tenDichVu, dv.maDichVu
                """;

        return findItems(sql_query, customer_id, invoice_id);
    }

    /**
     * Thực hiện truy vấn chi tiết sản phẩm hoặc dịch vụ.
     * Câu SQL chỉ được cung cấp từ các phương thức trong lớp này.
     *
     * @param sql_query Câu SQL có hai tham số khách hàng và hóa đơn.
     * @param customer_id Mã khách hàng sở hữu hóa đơn.
     * @param invoice_id Mã hóa đơn cần truy vấn.
     * @return Danh sách dòng chi tiết.
     * @throws SQLException Khi truy vấn database thất bại.
     */
    private List<CustomerTransactionItem> findItems(
            String sql_query,
            String customer_id,
            String invoice_id
    ) throws SQLException {

        // Danh sách sản phẩm hoặc dịch vụ trả về.
        List<CustomerTransactionItem> item_list =
                new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql_query)) {

            statement.setString(1, customer_id);
            statement.setString(2, invoice_id);

            try (ResultSet result_set = statement.executeQuery()) {
                // Tạo đối tượng chi tiết từ từng dòng kết quả.
                while (result_set.next()) {
                    // Dòng sản phẩm hoặc dịch vụ tại vị trí hiện tại.
                    CustomerTransactionItem item =
                            new CustomerTransactionItem(
                                    result_set.getString("maDoiTuong"),
                                    result_set.getString("tenDoiTuong"),
                                    result_set.getInt("soLuong"),
                                    result_set.getBigDecimal("donGia")
                            );

                    item_list.add(item);
                }
            }
        }

        return item_list;
    }

    /**
     * Chuyển dòng SQL hiện tại thành giao dịch khách hàng.
     *
     * @param result_set Kết quả đang trỏ tới một dòng hợp lệ.
     * @return Giao dịch được tạo từ dữ liệu SQL.
     * @throws SQLException Khi đọc dữ liệu thất bại.
     */
    private CustomerTransaction mapTransaction(
            ResultSet result_set
    ) throws SQLException {

        return new CustomerTransaction(
                result_set.getString("maHoaDon"),
                result_set.getString("maKhachHang"),
                result_set.getString("tenKhachHang"),
                result_set.getDate("ngayLap").toLocalDate(),
                result_set.getString("tenNhanVien"),
                result_set.getString("hinhThucThanhToan"),
                result_set.getString("trangThaiThanhToan"),
                result_set.getBigDecimal("tongTien")
        );
    }
}