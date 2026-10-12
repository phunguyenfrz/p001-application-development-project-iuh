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

public final class PurchaseOrderSearchProvider extends AbstractSqlSearchProvider {
    private static final String SQL = """
            SELECT dh.maDonHang, ncc.tenNhaCungCap, dh.trangThai, dh.ngayLap
            FROM DonHang dh
            JOIN NhaCungCap ncc ON ncc.maNhaCungCap = dh.maNhaCungCap
            WHERE {search}
            ORDER BY dh.ngayLap DESC, dh.maDonHang
            """;

    public PurchaseOrderSearchProvider() {
        this(DbConnection::getConnection);
    }

    public PurchaseOrderSearchProvider(Supplier<java.sql.Connection> connections) {
        super(connections);
    }

    @Override
    public String getGroupName() {
        return "Đơn nhập";
    }

    @Override
    public Set<Role> visibleTo() {
        return Set.of(Role.QUAN_LY, Role.BAN_HANG);
    }

    @Override
    public List<SearchResult> search(String keyword) {
        return execute(keyword, SQL, "dh.maDonHang", "ncc.tenNhaCungCap");
    }

    @Override
    protected SearchResult map(ResultSet row) throws SQLException {
        return new SearchResult(getGroupName(), row.getString("maDonHang"),
                row.getString("tenNhaCungCap"), row.getString("tenNhaCungCap") + " · "
                + row.getString("trangThai") + " · " + row.getDate("ngayLap"),
                Screen.PURCHASE_ORDERS);
    }
}
