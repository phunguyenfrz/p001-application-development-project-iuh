package com.petshop.service;

import com.petshop.dao.CustomerTransactionDao;
import com.petshop.model.CustomerTransaction;
import com.petshop.model.CustomerTransactionItem;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Xử lý nghiệp vụ tra cứu lịch sử giao dịch khách hàng.
 * Kiểm tra bộ lọc và chuyển lỗi database thành thông báo dễ hiểu.
 */
public class CustomerTransactionService {

    // Đối tượng đọc lịch sử giao dịch từ database.
    private final CustomerTransactionDao transaction_dao =
            new CustomerTransactionDao();

    /**
     * Lấy lịch sử giao dịch của khách hàng theo bộ lọc.
     *
     * @param customer_id Mã khách hàng cần tra cứu.
     * @param from_date Ngày bắt đầu; null nếu không giới hạn.
     * @param to_date Ngày kết thúc; null nếu không giới hạn.
     * @param payment_status Trạng thái hoặc Tất cả.
     * @return Danh sách hóa đơn phù hợp.
     * @throws IllegalArgumentException Khi mã hoặc bộ lọc không hợp lệ.
     * @throws IllegalStateException Khi không đọc được dữ liệu.
     */
    public List<CustomerTransaction> loadTransactions(
            String customer_id,
            LocalDate from_date,
            LocalDate to_date,
            String payment_status
    ) {
        validateCustomerId(customer_id);

        // Chỉ kiểm tra thứ tự khi người dùng chọn cả hai ngày.
        if (from_date != null
                && to_date != null
                && from_date.isAfter(to_date)) {

            throw new IllegalArgumentException(
                    "Từ ngày phải nhỏ hơn hoặc bằng Đến ngày."
            );
        }

        // Chuyển Tất cả thành null và kiểm tra trạng thái được chọn.
        String normalized_status =
                normalizePaymentStatus(payment_status);

        try {
            return transaction_dao.findByCustomer(
                    customer_id.trim(),
                    from_date,
                    to_date,
                    normalized_status
            );
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không tải được lịch sử giao dịch. "
                            + "Vui lòng kiểm tra kết nối và thử lại.",
                    exception
            );
        }
    }

    /**
     * Lấy sản phẩm trong hóa đơn của khách hàng.
     *
     * @param customer_id Mã khách hàng sở hữu hóa đơn.
     * @param invoice_id Mã hóa đơn cần xem.
     * @return Danh sách sản phẩm trong hóa đơn.
     * @throws IllegalArgumentException Khi mã không hợp lệ.
     * @throws IllegalStateException Khi không đọc được dữ liệu.
     */
    public List<CustomerTransactionItem> loadProducts(
            String customer_id,
            String invoice_id
    ) {
        validateCustomerId(customer_id);
        validateInvoiceId(invoice_id);

        try {
            return transaction_dao.findProducts(
                    customer_id.trim(),
                    invoice_id.trim()
            );
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không tải được sản phẩm trong hóa đơn.",
                    exception
            );
        }
    }

    /**
     * Lấy dịch vụ trong hóa đơn của khách hàng.
     *
     * @param customer_id Mã khách hàng sở hữu hóa đơn.
     * @param invoice_id Mã hóa đơn cần xem.
     * @return Danh sách dịch vụ trong hóa đơn.
     * @throws IllegalArgumentException Khi mã không hợp lệ.
     * @throws IllegalStateException Khi không đọc được dữ liệu.
     */
    public List<CustomerTransactionItem> loadServices(
            String customer_id,
            String invoice_id
    ) {
        validateCustomerId(customer_id);
        validateInvoiceId(invoice_id);

        try {
            return transaction_dao.findServices(
                    customer_id.trim(),
                    invoice_id.trim()
            );
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không tải được dịch vụ trong hóa đơn.",
                    exception
            );
        }
    }

    /**
     * Tính tổng tiền của các hóa đơn đã thanh toán.
     * Chỉ tính trên danh sách kết quả được truyền vào.
     *
     * @param transaction_list Danh sách hóa đơn đang hiển thị.
     * @return Tổng tiền đã thanh toán; bằng 0 nếu không có.
     * @throws IllegalArgumentException Khi danh sách là null.
     */
    public BigDecimal calculatePaidTotal(
            List<CustomerTransaction> transaction_list
    ) {
        // Không xem dữ liệu chưa tải được như danh sách rỗng.
        if (transaction_list == null) {
            throw new IllegalArgumentException(
                    "Chưa có danh sách giao dịch để tính tổng."
            );
        }

        // Tổng tiền dùng BigDecimal để giữ độ chính xác.
        BigDecimal paid_total = BigDecimal.ZERO;

        // Chỉ cộng tổng thực tế của hóa đơn đã thanh toán.
        for (CustomerTransaction transaction : transaction_list) {
            if ("Đã thanh toán".equals(
                    transaction.getTrangThaiThanhToan()
            )) {
                paid_total = paid_total.add(
                        transaction.getTongTien()
                );
            }
        }

        return paid_total;
    }

    /**
     * Đếm hóa đơn đã thanh toán trong kết quả hiện tại.
     *
     * @param transaction_list Danh sách hóa đơn đang hiển thị.
     * @return Số hóa đơn đã thanh toán.
     * @throws IllegalArgumentException Khi danh sách là null.
     */
    public int countPaidTransactions(
            List<CustomerTransaction> transaction_list
    ) {
        if (transaction_list == null) {
            throw new IllegalArgumentException(
                    "Chưa có danh sách giao dịch để thống kê."
            );
        }

        // Số hóa đơn có trạng thái Đã thanh toán.
        int paid_count = 0;

        for (CustomerTransaction transaction : transaction_list) {
            if ("Đã thanh toán".equals(
                    transaction.getTrangThaiThanhToan()
            )) {
                paid_count++;
            }
        }

        return paid_count;
    }

    /**
     * Kiểm tra định dạng mã khách hàng.
     *
     * @param customer_id Mã theo định dạng KH-YYYYMMDD-NNNNNN.
     * @throws IllegalArgumentException Khi chưa chọn hoặc mã không hợp lệ.
     */
    private void validateCustomerId(String customer_id) {
        if (customer_id == null
                || !customer_id.trim().matches(
                        "KH-[0-9]{8}-[0-9]{6}"
                )) {

            throw new IllegalArgumentException(
                    "Vui lòng chọn một khách hàng hợp lệ."
            );
        }
    }

    /**
     * Kiểm tra định dạng mã hóa đơn.
     *
     * @param invoice_id Mã theo định dạng HD-YYYYMMDD-NNNNNN.
     * @throws IllegalArgumentException Khi chưa chọn hoặc mã không hợp lệ.
     */
    private void validateInvoiceId(String invoice_id) {
        if (invoice_id == null
                || !invoice_id.trim().matches(
                        "HD-[0-9]{8}-[0-9]{6}"
                )) {

            throw new IllegalArgumentException(
                    "Vui lòng chọn một hóa đơn hợp lệ."
            );
        }
    }

    /**
     * Chuẩn hóa trạng thái thanh toán trước khi gửi xuống DAO.
     *
     * @param payment_status Trạng thái do giao diện truyền vào.
     * @return Trạng thái hợp lệ; null nếu không lọc.
     * @throws IllegalArgumentException Khi trạng thái không được hỗ trợ.
     */
    private String normalizePaymentStatus(String payment_status) {
        // Không lọc khi chưa chọn trạng thái.
        if (payment_status == null) {
            return null;
        }

        // Giá trị đã bỏ khoảng trắng đầu cuối.
        String normalized_status = payment_status.trim();

        // DAO nhận null khi người dùng chọn Tất cả.
        if (normalized_status.isEmpty()
                || "Tất cả".equals(normalized_status)) {
            return null;
        }

        // Chỉ chấp nhận trạng thái có trong CHECK của bảng HoaDon.
        if (!"Chưa thanh toán".equals(normalized_status)
                && !"Đã thanh toán".equals(normalized_status)
                && !"Đã hủy".equals(normalized_status)) {

            throw new IllegalArgumentException(
                    "Trạng thái thanh toán không hợp lệ."
            );
        }

        return normalized_status;
    }
}