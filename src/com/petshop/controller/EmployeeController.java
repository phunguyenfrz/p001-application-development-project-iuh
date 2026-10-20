package com.petshop.controller;

import com.petshop.model.Employee;
import com.petshop.service.EmployeeService;

import java.util.List;

/**
 * Tiếp nhận thao tác từ giao diện nhân viên.
 *
 * Chuyển yêu cầu đến Service để kiểm tra và xử lý nghiệp vụ.
 * Không chứa SQL hoặc xử lý điều khiển JavaFX.
 */
public class EmployeeController {

    // Service xử lý nghiệp vụ nhân viên.
    private final EmployeeService employee_service =
            new EmployeeService();

    /**
     * Lấy danh sách nhân viên và thông tin tài khoản chỉ đọc.
     *
     * @return Danh sách nhân viên.
     */
    public List<Employee> loadEmployees() {
        return employee_service.findAll();
    }

    /**
     * Lấy hồ sơ đầy đủ, bao gồm ảnh, trước khi mở form sửa.
     *
     * @param employee_id Mã nhân viên.
     * @return Hồ sơ nhân viên.
     */
    public Employee loadEmployee(String employee_id) {
        return employee_service.findById(employee_id);
    }

    /**
     * Thêm nhân viên mới.
     *
     * Không tự tạo tài khoản đăng nhập.
     *
     * @param employee Thông tin nhập từ form.
     * @return Mã nhân viên mới.
     */
    public String addEmployee(Employee employee) {
        return employee_service.insert(employee);
    }

    /**
     * Cập nhật hồ sơ nhân viên.
     *
     * @param employee Hồ sơ có mã nhân viên cần sửa.
     */
    public void updateEmployee(Employee employee) {
        employee_service.update(employee);
    }

    /**
     * Ngừng làm việc và khóa tài khoản liên quan nếu có.
     *
     * @param employee_id Mã nhân viên.
     */
    public void deactivateEmployee(String employee_id) {
        employee_service.deactivate(employee_id);
    }

    /**
     * Kiểm tra để giao diện bật/tắt nút thao tác.
     *
     * Service vẫn kiểm tra lại khi lưu hoặc ngừng làm việc.
     *
     * @param employee Nhân viên đang được chọn.
     * @return true nếu được sửa hoặc ngừng làm việc.
     */
    public boolean canEdit(Employee employee) {
        return employee_service.canEdit(employee);
    }

    /**
     * Lấy danh sách nhân viên đang làm việc.
     */
    public List<Employee> loadActiveEmployees() {
        return employee_service.findActiveEmployees();
    }

    /**
     * Cấp tài khoản mới cho nhân viên.
     */
    public String createAccount(String maNhanVien, String tenDangNhap, String matKhau, String phanQuyen) {
        return employee_service.createAccount(maNhanVien, tenDangNhap, matKhau, phanQuyen);
    }

    /**
     * Cập nhật phân quyền truy cập.
     */
    public void updateRole(String maNhanVien, String phanQuyen) {
        employee_service.updateRole(maNhanVien, phanQuyen);
    }

    /**
     * Khóa hoặc mở khóa tài khoản.
     */
    public void toggleAccountStatus(String maNhanVien, boolean active) {
        employee_service.toggleAccountStatus(maNhanVien, active);
    }

    /**
     * Đặt lại mật khẩu tài khoản.
     */
    public void resetPassword(String maNhanVien, String newPassword) {
        employee_service.resetPassword(maNhanVien, newPassword);
    }
}