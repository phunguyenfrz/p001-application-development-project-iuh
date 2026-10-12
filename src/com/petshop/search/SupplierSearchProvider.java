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

public final class SupplierSearchProvider extends AbstractSqlSearchProvider {
    private static final String SQL = """
            SELECT maNhaCungCap, tenNhaCungCap, soDienThoai
            FROM NhaCungCap
            WHERE {search}
            ORDER BY tenNhaCungCap, maNhaCungCap
            """;

    public SupplierSearchProvider() {
        this(DbConnection::getConnection);
    }

    public SupplierSearchProvider(Supplier<java.sql.Connection> connections) {
        super(connections);
    }

    @Override
    public String getGroupName() {
        return "Nhà cung cấp";
    }

    @Override
    public Set<Role> visibleTo() {
        return Set.of(Role.QUAN_LY);
    }

    @Override
    public List<SearchResult> search(String keyword) {
        return execute(keyword, SQL, "maNhaCungCap", "tenNhaCungCap", "soDienThoai");
    }

    @Override
    protected SearchResult map(ResultSet row) throws SQLException {
        return new SearchResult(getGroupName(), row.getString("maNhaCungCap"),
                row.getString("tenNhaCungCap"), row.getString("soDienThoai"),
                Screen.SUPPLIERS);
    }
}
