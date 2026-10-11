package com.petshop.controller;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.petshop.dao.ReportRepository;
import com.petshop.model.ReportData.Period;
import com.petshop.model.ReportData.Result;
import com.petshop.model.ReportData.Type;
import com.petshop.util.Formats;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class ReportControllerImpl implements ReportController {
    private final ReportRepository repository;

    public ReportControllerImpl(ReportRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public Result load(Type type, Period period, LocalDate referenceDate) {
        LocalDate start;
        LocalDate end;
        switch (period) {
            case DAY -> {
                start = referenceDate;
                end = referenceDate;
            }
            case WEEK -> {
                start = referenceDate.with(DayOfWeek.MONDAY);
                end = start.plusDays(6);
            }
            case MONTH -> {
                start = referenceDate.withDayOfMonth(1);
                end = referenceDate.withDayOfMonth(referenceDate.lengthOfMonth());
            }
            default -> throw new IllegalArgumentException("Kỳ báo cáo không hợp lệ.");
        }
        return repository.load(type, period, start, end);
    }

    @Override
    public void exportExcel(Result result, Path target) {
        try {
            createParent(target);
            try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet(result.type().label());
                Row heading = sheet.createRow(0);
                heading.createCell(0).setCellValue("Nhóm");
                heading.createCell(1).setCellValue("Tên / Ngày");
                heading.createCell(2).setCellValue("Số lượng");
                heading.createCell(3).setCellValue("Giá trị");
                int rowIndex = 1;
                for (var item : result.rows()) {
                    Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(item.category());
                    row.createCell(1).setCellValue(item.name());
                    row.createCell(2).setCellValue(item.quantity());
                    row.createCell(3).setCellValue(item.amount().doubleValue());
                }
                for (int column = 0; column < 4; column++) {
                    sheet.autoSizeColumn(column);
                }
                try (var output = Files.newOutputStream(target)) {
                    workbook.write(output);
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Không thể xuất báo cáo Excel.", exception);
        }
    }

    @Override
    public void exportPdf(Result result, Path target) {
        try {
            createParent(target);
            Document document = new Document();
            try (var output = Files.newOutputStream(target)) {
                PdfWriter.getInstance(document, output);
                document.open();
                Paragraph title = new Paragraph("BÁO CÁO " + result.type().label().toUpperCase(),
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16));
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);
                document.add(new Paragraph("Kỳ: " + Formats.date(result.startDate())
                        + " – " + Formats.date(result.endDate())));
                PdfPTable table = new PdfPTable(new float[] {2, 4, 2, 2});
                table.setWidthPercentage(100);
                for (String heading : List.of("Nhóm", "Tên / Ngày", "Số lượng", "Giá trị")) {
                    table.addCell(heading);
                }
                for (var item : result.rows()) {
                    table.addCell(item.category());
                    table.addCell(item.name());
                    table.addCell(Long.toString(item.quantity()));
                    table.addCell(Formats.money(item.amount()));
                }
                document.add(table);
            } finally {
                document.close();
            }
        } catch (IOException | DocumentException exception) {
            throw new IllegalStateException("Không thể xuất báo cáo PDF.", exception);
        }
    }

    private void createParent(Path target) throws IOException {
        Path parent = target.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }
}
