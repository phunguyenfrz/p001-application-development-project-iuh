package com.petshop.controller;

import com.petshop.dao.ShiftRepository;
import com.petshop.model.ShiftAssignment;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class ShiftController {
    private final ShiftRepository repository;

    public ShiftController(ShiftRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public List<ShiftAssignment> findBetween(LocalDate from, LocalDate through) {
        if (from == null || through == null || through.isBefore(from)
                || through.isAfter(from.plusMonths(1))) {
            throw new IllegalArgumentException("Chọn khoảng lịch hợp lệ, không quá một tháng.");
        }
        return repository.findBetween(from, through);
    }

    public List<ShiftAssignment.Choice> activeEmployees() {
        return repository.activeEmployees();
    }

    public List<ShiftAssignment.Choice> activeShifts() {
        return repository.activeShifts();
    }

    public void add(ShiftAssignment assignment) {
        validate(assignment);
        repository.add(assignment);
    }

    public void update(ShiftAssignment assignment) {
        validate(assignment);
        if (assignment.workDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Không thể sửa ca đã diễn ra.");
        }
        repository.update(assignment);
    }

    public void delete(ShiftAssignment assignment) {
        if (assignment == null || assignment.workDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Chỉ được xóa ca chưa diễn ra.");
        }
        repository.deleteFuture(assignment.id());
    }

    private void validate(ShiftAssignment assignment) {
        Objects.requireNonNull(assignment, "Thông tin phân ca không được để trống.");
        if (assignment.id() == null || assignment.id().isBlank()
                || assignment.employeeId() == null || assignment.shiftId() == null
                || assignment.workDate() == null) {
            throw new IllegalArgumentException("Vui lòng nhập đủ nhân viên, ca và ngày làm.");
        }
        if (assignment.workDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Không thể phân ca cho ngày đã qua.");
        }
    }
}
