package com.petshop.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class TextSearch {
    private TextSearch() {
    }

    public static String normalize(String value) {
        String decomposed = Normalizer.normalize(value == null ? "" : value.trim(),
                Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT);
    }

    public static boolean matchesAll(String keyword, String... fields) {
        List<String> terms = terms(keyword);
        if (terms.isEmpty()) {
            return false;
        }
        String searchableFields = String.join(" ",
                Arrays.stream(fields).map(TextSearch::normalize).collect(Collectors.toList()));
        return terms.stream().allMatch(searchableFields::contains);
    }

    public static boolean startsWithAny(String keyword, String... fields) {
        String normalizedKeyword = normalize(keyword).trim();
        if (normalizedKeyword.isEmpty()) {
            return false;
        }
        return Arrays.stream(fields)
                .map(TextSearch::normalize)
                .anyMatch(field -> field.startsWith(normalizedKeyword));
    }

    public static String likePredicate(String keyword, String... fields) {
        List<String> terms = terms(keyword);
        if (terms.isEmpty() || fields.length == 0) {
            return "1 = 0";
        }
        return terms.stream()
                .map(term -> "(" + Arrays.stream(fields)
                        .map(field -> field + " COLLATE Vietnamese_CI_AI LIKE ? ESCAPE '\\'")
                        .collect(Collectors.joining(" OR ")) + ")")
                .collect(Collectors.joining(" AND "));
    }

    public static void bindLikeParameters(
            PreparedStatement statement, int firstParameter, String keyword, int fieldCount)
            throws SQLException {
        int parameter = firstParameter;
        for (String term : terms(keyword)) {
            String pattern = "%" + escapeLike(term) + "%";
            for (int field = 0; field < fieldCount; field++) {
                statement.setString(parameter++, pattern);
            }
        }
    }

    public static int termCount(String keyword) {
        return terms(keyword).size();
    }

    private static List<String> terms(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return Arrays.stream(normalize(keyword).split("\\s+"))
                .filter(term -> !term.isBlank())
                .toList();
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_")
                .replace("[", "\\[");
    }
}
