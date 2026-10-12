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

public final class InventorySearchProvider extends AbstractSqlSearchProvider {
    private static final String SQL = """
            SELECT sp.maSanPham, sp.tenSanPham, sp.maVach, ncc.tenNhaCungCap
            FROM SanPham sp
            LEFT JOIN NhaCungCap ncc ON ncc.maNhaCungCap = sp.maNhaCungCap
            WHERE {search}
            ORDER BY sp.tenSanPham, sp.maSanPham
            """;

    public InventorySearchProvider() {
        this(DbConnection::getConnection);
    }

    public InventorySearchProvider(Supplier<java.sql.Connection> connections) {
        super(connections);
    }

    @Override
    public String getGroupName() {
        return "Tồn kho";
    }

    @Override
    public Set<Role> visibleTo() {
        return Set.of(Role.QUAN_LY, Role.BAN_HANG);
    }

    @Override
    public List<SearchResult> search(String keyword) {
        return execute(keyword, SQL, "sp.maSanPham", "sp.tenSanPham",
                "sp.maVach", "ncc.tenNhaCungCap");
    }

    @Override
    protected SearchResult map(ResultSet row) throws SQLException {
        return new SearchResult(getGroupName(), row.getString("maSanPham"),
                row.getString("tenSanPham"), "Mã vạch: " + nullToDash(row.getString("maVach"))
                + " · Nhà cung cấp: " + nullToDash(row.getString("tenNhaCungCap")),
                Screen.INVENTORY);
    }
}
