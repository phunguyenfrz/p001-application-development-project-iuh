package com.petshop.dao;

import com.petshop.model.ShiftAssignment;

import java.time.LocalDate;
import java.util.List;

public interface ShiftRepository {
    List<ShiftAssignment> findBetween(LocalDate from, LocalDate through);
    List<ShiftAssignment.Choice> activeEmployees();
    List<ShiftAssignment.Choice> activeShifts();
    void add(ShiftAssignment assignment);
    void update(ShiftAssignment assignment);
    void deleteFuture(String id);
}
