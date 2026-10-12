package com.petshop.controller;

import com.petshop.dao.CrudDao;
import com.petshop.model.Customer;

import java.util.Objects;

public final class CustomerController extends CrudControllerImpl<Customer, String> {
    public CustomerController(CrudDao<Customer, String> dao) {
        super(dao);
    }

    @Override
    public void add(Customer customer) {
        validate(customer);
        super.add(customer);
    }

    @Override
    public void update(Customer customer) {
        validate(customer);
        super.update(customer);
    }

    private void validate(Customer customer) {
        Objects.requireNonNull(customer, "Thông tin khách hàng không được để trống.");
        if (customer.getHoTen() == null || customer.getHoTen().isBlank()
                || customer.getHoTen().length() > 100) {
            throw new IllegalArgumentException("Họ tên khách hàng là bắt buộc, tối đa 100 ký tự.");
        }
        if (customer.getSoDienThoai() == null
                || !customer.getSoDienThoai().matches("\\d{10}")) {
            throw new IllegalArgumentException("Số điện thoại phải gồm đúng 10 chữ số.");
        }
        if (customer.getEmail() != null && customer.getEmail().length() > 100) {
            throw new IllegalArgumentException("Email không được vượt quá 100 ký tự.");
        }
        if (customer.getEmail() != null && !customer.getEmail().isBlank()
                && !customer.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Địa chỉ email không hợp lệ.");
        }
        if (customer.getDiaChi() != null && customer.getDiaChi().length() > 255
                || customer.getGhiChu() != null && customer.getGhiChu().length() > 255) {
            throw new IllegalArgumentException("Địa chỉ và ghi chú tối đa 255 ký tự.");
        }
        if (customer.getDiemTichLuy() < 0) {
            throw new IllegalArgumentException("Điểm tích lũy không được âm.");
        }
    }
}
