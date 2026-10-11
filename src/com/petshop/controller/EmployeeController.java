package com.petshop.controller;

import com.petshop.dao.EmployeeRepository;
import com.petshop.model.Employee;
import com.petshop.util.PasswordUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class EmployeeController {
    private final EmployeeRepository repository;

    public EmployeeController(EmployeeRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public List<Employee> findAll() {
        return repository.findAll();
    }

    public List<Employee> search(String keyword) {
        return repository.search(keyword);
    }

    public String add(Employee employee, String password) {
        validate(employee, password, true);
        return repository.add(employee, PasswordUtil.hash(password));
    }

    public void update(Employee employee, String password) {
        validate(employee, password, false);
        repository.update(employee,
                password == null || password.isBlank() ? null : PasswordUtil.hash(password));
    }

    public void deactivate(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Mã nhân viên không hợp lệ.");
        }
        repository.deactivate(id);
    }

    private void validate(Employee employee, String password, boolean adding) {
        Objects.requireNonNull(employee, "Thông tin nhân viên không được để trống.");
        if (employee.fullName() == null || employee.fullName().isBlank()) {
            throw new IllegalArgumentException("Họ tên nhân viên không được để trống.");
        }
        if (employee.phone() == null || !employee.phone().matches("\\d{10}")) {
            throw new IllegalArgumentException("Số điện thoại phải gồm đúng 10 chữ số.");
        }
        if (employee.role() == null || employee.birthDate() == null) {
            throw new IllegalArgumentException("Vai trò và ngày sinh là bắt buộc.");
        }
        if (!employee.birthDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày sinh phải trước ngày hiện tại.");
        }
        if (employee.username() == null || employee.username().isBlank()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống.");
        }
        if (adding || password != null && !password.isBlank()) {
            if (password == null || password.length() < 8
                    || !password.matches(".*[A-Za-z].*")
                    || !password.matches(".*\\d.*")) {
                throw new IllegalArgumentException(
                        "Mật khẩu phải có ít nhất 8 ký tự, gồm chữ và số.");
            }
        }
    }
}
