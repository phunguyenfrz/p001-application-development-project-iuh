package com.petshop.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class ReportData {
    private ReportData() {
    }

    public enum Type {
        REVENUE("Doanh thu"),
        INVENTORY("Tồn kho"),
        BEST_SELLERS("Bán chạy");

        private final String label;

        Type(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public enum Period {
        DAY("Ngày"),
        WEEK("Tuần"),
        MONTH("Tháng");

        private final String label;

        Period(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public record Row(String category, String name, long quantity, BigDecimal amount) {
    }

    public record Result(
            Type type, Period period, LocalDate startDate, LocalDate endDate, List<Row> rows) {
        public Result {
            rows = List.copyOf(rows);
        }
    }
}
