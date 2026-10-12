package com.petshop.controller;

import com.petshop.model.ReportData.Period;
import com.petshop.model.ReportData.Result;
import com.petshop.model.ReportData.Type;

import java.nio.file.Path;
import java.time.LocalDate;

public interface ReportController {
    Result load(Type type, Period period, LocalDate referenceDate);

    void exportExcel(Result result, Path target);

    void exportPdf(Result result, Path target);
}
