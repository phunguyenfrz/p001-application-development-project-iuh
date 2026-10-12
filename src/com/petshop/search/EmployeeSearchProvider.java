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

public final class EmployeeSearchProvider extends AbstractSqlSearchProvider {
    private static final String SQL = """
            SELECT maNhanVien, hoTen, soDienThoai
            FROM NhanVien
            WHERE {search}
            ORDER BY hoTen, maNhanVien
            """;

    public EmployeeSearchProvider() {
        this(DbConnection::getConnection);
    }

    public EmployeeSearchProvider(Supplier<java.sql.Connection> connections) {
        super(connections);
    }

    @Override
    public String getGroupName() {
        return "Nhân viên";
    }

    @Override
    public Set<Role> visibleTo() {
        return Set.of(Role.QUAN_LY);
    }

    @Override
    public List<SearchResult> search(String keyword) {
        return execute(keyword, SQL, "maNhanVien", "hoTen", "soDienThoai");
    }

    @Override
    protected SearchResult map(ResultSet row) throws SQLException {
        return new SearchResult(getGroupName(), row.getString("maNhanVien"),
                row.getString("hoTen"), row.getString("soDienThoai"), Screen.EMPLOYEES);
    }
}
