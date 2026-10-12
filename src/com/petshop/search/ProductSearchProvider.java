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

public final class ProductSearchProvider extends AbstractSqlSearchProvider {
    private static final String SQL = """
            SELECT maSanPham, tenSanPham, maVach
            FROM SanPham
            WHERE {search}
            ORDER BY tenSanPham, maSanPham
            """;

    public ProductSearchProvider() {
        this(DbConnection::getConnection);
    }

    public ProductSearchProvider(Supplier<java.sql.Connection> connections) {
        super(connections);
    }

    @Override
    public String getGroupName() {
        return "Sản phẩm";
    }

    @Override
    public Set<Role> visibleTo() {
        return Set.of(Role.BAN_HANG);
    }

    @Override
    public List<SearchResult> search(String keyword) {
        return execute(keyword, SQL, "maSanPham", "tenSanPham", "maVach");
    }

    @Override
    protected SearchResult map(ResultSet row) throws SQLException {
        return new SearchResult(getGroupName(), row.getString("maSanPham"),
                row.getString("tenSanPham"), "Mã vạch: "
                + nullToDash(row.getString("maVach")), Screen.PRODUCTS);
    }
}
