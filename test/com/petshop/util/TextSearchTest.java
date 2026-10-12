package com.petshop.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextSearchTest {
    @Test
    void matchesSubstringAnywhereWithoutCaseOrVietnameseDiacriticDifferences() {
        assertTrue(TextSearch.matchesAll("an", "Lan"));
        assertTrue(TextSearch.matchesAll("an", "Anh"));
        assertTrue(TextSearch.matchesAll("an", "Phan Văn A"));
        assertFalse(TextSearch.matchesAll("an", "Hùng"));
        assertTrue(TextSearch.matchesAll("nguyen", "Nguyễn Văn A"));
        assertTrue(TextSearch.matchesAll("dung", "Dũng"));
        assertTrue(TextSearch.matchesAll("dung", "Đũng"));
    }

    @Test
    void matchesCaseInsensitiveSubstringsConsistently() {
        assertTrue(TextSearch.matchesAll("Hoangl", "Hoangl"));
        assertTrue(TextSearch.matchesAll("hoangl", "Hoangl"));
        assertTrue(TextSearch.matchesAll("HOANGL", "Hoangl"));
        assertTrue(TextSearch.matchesAll("HoAnGl", "hoangl"));
        assertTrue(TextSearch.matchesAll("hoa", "Hoangl"));
        assertTrue(TextSearch.matchesAll("nguyen", "Nguyễn"));
        assertTrue(TextSearch.matchesAll("Nguyễn", "nguyen"));
        assertFalse(TextSearch.matchesAll("xyz", "Hoangl"));
    }

    @Test
    void requiresEveryWhitespaceSeparatedTermInAnyOrder() {
        assertTrue(TextSearch.matchesAll("nguyen lan", "Nguyễn Thị Lan"));
        assertTrue(TextSearch.matchesAll("nguyen lan", "Lan Nguyễn"));
        assertFalse(TextSearch.matchesAll("nguyen hùng", "Nguyễn Thị Lan"));
    }

    @Test
    void blankTermsNeverMatchAndLikePatternEscapesWildcards() {
        assertFalse(TextSearch.matchesAll("   ", "bất kỳ dữ liệu"));
        assertFalse(TextSearch.matchesAll(null, "bất kỳ dữ liệu"));
        assertTrue(TextSearch.likePredicate("%_['", "c.name")
                .contains("c.name COLLATE Vietnamese_CI_AI LIKE ? ESCAPE '\\'"));

        List<String> bound = new ArrayList<>();
        PreparedStatement statement = (PreparedStatement) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{PreparedStatement.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("setString")) {
                        bound.add((String) args[1]);
                        return null;
                    }
                    return null;
                });
        try {
            TextSearch.bindLikeParameters(statement, 1, "%_['", 1);
        } catch (java.sql.SQLException exception) {
            throw new AssertionError(exception);
        }
        assertEquals(List.of("%\\%\\_\\['%"), bound);
    }
}
