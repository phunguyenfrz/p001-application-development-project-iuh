package com.petshop.dao;

import com.petshop.model.AppointmentData.Draft;
import com.petshop.model.AppointmentData.Lookups;
import com.petshop.model.AppointmentData.Row;
import com.petshop.model.AppointmentData.ServiceOption;
import java.math.BigDecimal;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository {
    List<Row> findByDate(LocalDate date);

    Lookups loadLookups(LocalDate date);

    List<ServiceOption> loadServices(LocalDate date, BigDecimal petWeight);

    String create(Draft draft);

    void updateStatus(String appointmentId, String expectedStatus, String status);
}
