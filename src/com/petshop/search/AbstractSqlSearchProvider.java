package com.petshop.search;

import com.petshop.model.SearchResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

abstract class AbstractSqlSearchProvider implements SearchProvider {
    private final Supplier<Connection> connections;

    AbstractSqlSearchProvider(Supplier<Connection> connections) {
        this.connections = Objects.requireNonNull(connections);
    }

    protected final List<SearchResult> execute(
            String keyword, String sql, int parameterCount) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        String pattern = "%" + keyword.trim() + "%";
        List<SearchResult> results = new ArrayList<>();
        try (Connection connection = connections.get();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 1; index <= parameterCount; index++) {
                statement.setString(index, pattern);
            }
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    results.add(map(rows));
                }
            }
            return List.copyOf(results);
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không thể tìm kiếm nhóm " + getGroupName() + ".", exception);
        }
    }

    protected abstract SearchResult map(ResultSet row) throws SQLException;

    protected final String nullToDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }
}
