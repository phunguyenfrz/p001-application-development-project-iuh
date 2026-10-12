package com.petshop.dao;

import com.petshop.model.AppointmentData.Draft;
import com.petshop.model.AppointmentData.Lookups;
import com.petshop.model.AppointmentData.Row;
import com.petshop.model.AppointmentData.ServiceOption;
import java.math.BigDecimal;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository {
    List<Row> findByDate(LocalDate date);

    default Optional<Row> findById(String appointmentId) {
        return Optional.empty();
    }

    Lookups loadLookups(LocalDate date);

    List<ServiceOption> loadServices(LocalDate date, BigDecimal petWeight);

    String create(Draft draft);

    void updateStatus(String appointmentId, String expectedStatus, String status);
}
