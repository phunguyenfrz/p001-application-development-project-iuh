package com.petshop.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record ShiftAssignment(
        String id,
        String employeeId,
        String employeeName,
        String shiftId,
        String shiftName,
        LocalDate workDate,
        LocalTime startsAt,
        LocalTime endsAt,
        String status) {
    @Override
    public String toString() {
        return shiftName + " (" + startsAt + "–" + endsAt + ")";
    }

    public record Choice(String id, String label) {
        @Override
        public String toString() {
            return label;
        }
    }
}
