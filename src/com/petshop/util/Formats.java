package com.petshop.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

public final class Formats {
    private Formats() {
    }

    public static String money(BigDecimal amount) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"))
                .format(Objects.requireNonNull(amount));
    }

    public static String date(LocalDate value) {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("vi-VN"))
                .format(Objects.requireNonNull(value));
    }

    public static String dateTime(LocalDateTime value) {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("vi-VN"))
                .format(Objects.requireNonNull(value.atZone(ZoneId.of("Asia/Ho_Chi_Minh"))));
    }

    public static String dateTime(Instant value) {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("vi-VN"))
                .format(Objects.requireNonNull(value).atZone(ZoneId.of("Asia/Ho_Chi_Minh")));
    }

    public static String time(LocalTime value) {
        return DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("vi-VN"))
                .format(Objects.requireNonNull(value));
    }
}
