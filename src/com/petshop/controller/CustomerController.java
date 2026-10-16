package com.petshop.controller;

import com.petshop.model.Customer;
import com.petshop.service.CustomerService;

import java.util.List;

/**
 * Tiếp nhận thao tác khách hàng từ giao diện.
 * Chuyển yêu cầu đến CustomerService để xử lý nghiệp vụ.
 */
public class CustomerController {

    // Đối tượng xử lý nghiệp vụ khách hàng.
    private final CustomerService customer_service =
            new CustomerService();

    /**
     * Yêu cầu lấy danh sách khách hàng.
     *
     * @return Danh sách khách hàng từ Service.
     * @throws IllegalStateException Khi không đọc được dữ liệu.
     */
    public List<Customer> loadCustomers() {
        return customer_service.findAll();
    }

    /**
     * Yêu cầu thêm khách hàng mới.
     * Service tự tạo mã và đặt điểm tích lũy bằng 0.
     *
     * @param customer Thông tin khách hàng nhập trên form.
     * @throws IllegalArgumentException Khi dữ liệu không hợp lệ.
     * @throws IllegalStateException Khi lưu dữ liệu thất bại.
     */
    public void addCustomer(Customer customer) {
        customer_service.insert(customer);
    }

    /**
     * Yêu cầu sửa thông tin khách hàng.
     *
     * @param customer Khách hàng chứa mã và thông tin mới.
     * @throws IllegalArgumentException Khi dữ liệu không hợp lệ.
     * @throws IllegalStateException Khi cập nhật thất bại.
     */
    public void updateCustomer(Customer customer) {
        customer_service.update(customer);
    }

    /**
     * Yêu cầu xóa hẳn khách hàng theo mã.
     *
     * @param customer_id Mã khách hàng cần xóa.
     * @throws IllegalArgumentException Khi mã không hợp lệ.
     * @throws IllegalStateException Khi không được phép xóa hoặc lỗi SQL.
     */
    public void deleteCustomer(String customer_id) {
        customer_service.delete(customer_id);
    }
}