package com.petshop.controller;

import com.petshop.model.CustomerTransaction;
import com.petshop.model.CustomerTransactionItem;
import com.petshop.service.CustomerTransactionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Tiếp nhận yêu cầu tra cứu lịch sử giao dịch từ giao diện.
 * Chuyển yêu cầu đến CustomerTransactionService.
 */
public class CustomerTransactionController {

    // Đối tượng xử lý nghiệp vụ lịch sử giao dịch.
    private final CustomerTransactionService transaction_service =
            new CustomerTransactionService();

    /**
     * Yêu cầu lấy lịch sử giao dịch theo bộ lọc.
     *
     * @param customer_id Mã khách hàng được chọn.
     * @param from_date Ngày bắt đầu; null nếu không giới hạn.
     * @param to_date Ngày kết thúc; null nếu không giới hạn.
     * @param payment_status Trạng thái thanh toán hoặc Tất cả.
     * @return Danh sách hóa đơn phù hợp.
     * @throws IllegalArgumentException Khi bộ lọc không hợp lệ.
     * @throws IllegalStateException Khi không đọc được dữ liệu.
     */
    public List<CustomerTransaction> loadTransactions(
            String customer_id,
            LocalDate from_date,
            LocalDate to_date,
            String payment_status
    ) {
        return transaction_service.loadTransactions(
                customer_id,
                from_date,
                to_date,
                payment_status
        );
    }

    /**
     * Yêu cầu lấy các sản phẩm trong hóa đơn.
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
        return transaction_service.loadProducts(
                customer_id,
                invoice_id
        );
    }

    /**
     * Yêu cầu lấy các dịch vụ trong hóa đơn.
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
        return transaction_service.loadServices(
                customer_id,
                invoice_id
        );
    }

    /**
     * Yêu cầu tính tổng tiền các hóa đơn đã thanh toán.
     *
     * @param transaction_list Danh sách hóa đơn đang hiển thị.
     * @return Tổng tiền đã thanh toán trong danh sách.
     * @throws IllegalArgumentException Khi danh sách là null.
     */
    public BigDecimal calculatePaidTotal(
            List<CustomerTransaction> transaction_list
    ) {
        return transaction_service.calculatePaidTotal(
                transaction_list
        );
    }

    /**
     * Yêu cầu đếm các hóa đơn đã thanh toán.
     *
     * @param transaction_list Danh sách hóa đơn đang hiển thị.
     * @return Số hóa đơn đã thanh toán.
     * @throws IllegalArgumentException Khi danh sách là null.
     */
    public int countPaidTransactions(
            List<CustomerTransaction> transaction_list
    ) {
        return transaction_service.countPaidTransactions(
                transaction_list
        );
    }
}