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

public final class CustomerSearchProvider extends AbstractSqlSearchProvider {
    private static final String SQL = """
            SELECT TOP (5) maKhachHang, hoTen, soDienThoai
            FROM KhachHang
            WHERE maKhachHang LIKE ? OR hoTen LIKE ? OR soDienThoai LIKE ?
            ORDER BY hoTen
            """;

    public CustomerSearchProvider() {
        this(DbConnection::getConnection);
    }

    public CustomerSearchProvider(Supplier<java.sql.Connection> connections) {
        super(connections);
    }

    @Override
    public String getGroupName() {
        return "Khách hàng";
    }

    @Override
    public Set<Role> visibleTo() {
        return Set.of(Role.BAN_HANG, Role.CHAM_SOC);
    }

    @Override
    public List<SearchResult> search(String keyword) {
        return execute(keyword, SQL, 3);
    }

    @Override
    protected SearchResult map(ResultSet row) throws SQLException {
        return new SearchResult(getGroupName(), row.getString("maKhachHang"),
                row.getString("hoTen"), row.getString("soDienThoai"), Screen.CUSTOMERS);
    }
}
