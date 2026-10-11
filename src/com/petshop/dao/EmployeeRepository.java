package com.petshop.dao;

import com.petshop.model.Employee;

import java.util.List;

public interface EmployeeRepository {
    List<Employee> findAll();

    List<Employee> search(String keyword);

    String add(Employee employee, String passwordHash);

    void update(Employee employee, String passwordHash);

    void deactivate(String id);
}
