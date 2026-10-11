package com.petshop.search;

import com.petshop.model.Role;
import com.petshop.model.Screen;
import com.petshop.model.SearchResult;
import com.petshop.util.DbConnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public final class ServiceSearchProvider extends AbstractSqlSearchProvider {
    private static final String SQL = """
            SELECT TOP (5) maDichVu, tenDichVu, moTa
            FROM DichVuChamSoc
            WHERE maDichVu LIKE ? OR tenDichVu LIKE ? OR moTa LIKE ?
            ORDER BY tenDichVu
            """;

    public ServiceSearchProvider() {
        this(DbConnection::getConnection);
    }

    public ServiceSearchProvider(Supplier<java.sql.Connection> connections) {
        super(connections);
    }

    @Override
    public String getGroupName() {
        return "Dịch vụ";
    }

    @Override
    public Set<Role> visibleTo() {
        return Set.of(Role.QUAN_LY);
    }

    @Override
    public List<SearchResult> search(String keyword) {
        return execute(keyword, SQL, 3);
    }

    @Override
    protected SearchResult map(ResultSet row) throws SQLException {
        return new SearchResult(getGroupName(), row.getString("maDichVu"),
                row.getString("tenDichVu"), nullToDash(row.getString("moTa")),
                Screen.CARE_SERVICES);
    }
}
