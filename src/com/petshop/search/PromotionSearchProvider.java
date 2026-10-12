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

public final class PromotionSearchProvider extends AbstractSqlSearchProvider {
    private static final String SQL = """
            SELECT maKhuyenMai, tenChuongTrinh, phanTramGiam, ngayKetThuc
            FROM KhuyenMai
            WHERE {search}
            ORDER BY tenChuongTrinh, maKhuyenMai
            """;

    public PromotionSearchProvider() {
        this(DbConnection::getConnection);
    }

    public PromotionSearchProvider(Supplier<java.sql.Connection> connections) {
        super(connections);
    }

    @Override
    public String getGroupName() {
        return "Khuyến mãi";
    }

    @Override
    public Set<Role> visibleTo() {
        return Set.of(Role.QUAN_LY);
    }

    @Override
    public List<SearchResult> search(String keyword) {
        return execute(keyword, SQL, "maKhuyenMai", "tenChuongTrinh");
    }

    @Override
    protected SearchResult map(ResultSet row) throws SQLException {
        return new SearchResult(getGroupName(), row.getString("maKhuyenMai"),
                row.getString("tenChuongTrinh"), row.getBigDecimal("phanTramGiam") + "% · hết hạn "
                + row.getDate("ngayKetThuc"), Screen.PROMOTIONS);
    }
}
