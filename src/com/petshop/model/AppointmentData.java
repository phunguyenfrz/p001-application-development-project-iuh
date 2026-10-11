package com.petshop.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public final class AppointmentData {
    private AppointmentData() {
    }

    public record Row(
            String id,
            String customerId,
            String customerName,
            String petId,
            String petName,
            String employeeId,
            String employeeName,
            LocalDate date,
            LocalTime time,
            String status,
            String note,
            String services) {
    }

    public record CustomerOption(String id, String name, String phone) {
        @Override
        public String toString() {
            return name + " · " + phone + " · " + id;
        }
    }

    public record PetOption(String id, String customerId, String name, BigDecimal weight) {
        @Override
        public String toString() {
            return name + " · " + weight.stripTrailingZeros().toPlainString() + " kg · " + id;
        }
    }

    public record EmployeeOption(String id, String name) {
        @Override
        public String toString() {
            return name + " · " + id;
        }
    }

    public record ServiceOption(String id, String name, int durationMinutes, BigDecimal price) {
        @Override
        public String toString() {
            return name + " · " + price.toPlainString() + " đ";
        }
    }

    public record Lookups(
            List<CustomerOption> customers,
            List<PetOption> pets,
            List<EmployeeOption> employees) {
        public Lookups {
            customers = List.copyOf(customers);
            pets = List.copyOf(pets);
            employees = List.copyOf(employees);
        }
    }

    public record Draft(
            String customerId,
            String petId,
            String employeeId,
            LocalDate date,
            LocalTime time,
            String note,
            List<String> serviceIds) {
        public Draft {
            serviceIds = List.copyOf(serviceIds);
        }
    }

    public record CreationResult(String id, boolean auditLogged) {
    }
}
