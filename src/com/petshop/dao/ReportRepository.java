package com.petshop.dao;

import com.petshop.model.ReportData.Result;
import com.petshop.model.ReportData.Type;
import com.petshop.model.ReportData.Period;

import java.time.LocalDate;

public interface ReportRepository {
    Result load(Type type, Period period, LocalDate startDate, LocalDate endDate);
}
