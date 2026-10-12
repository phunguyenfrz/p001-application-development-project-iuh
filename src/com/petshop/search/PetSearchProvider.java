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

public final class PetSearchProvider extends AbstractSqlSearchProvider {
    private static final String SQL = """
            SELECT tc.maThuCung, tc.tenThuCung, tc.loaiThuCung, tc.giongLoai,
                   kh.hoTen, kh.soDienThoai
            FROM ThuCung tc
            JOIN KhachHang kh ON kh.maKhachHang = tc.maKhachHang
            WHERE tc.trangThai = 1 AND {search}
            ORDER BY tc.tenThuCung, tc.maThuCung
            """;

    public PetSearchProvider() {
        this(DbConnection::getConnection);
    }

    public PetSearchProvider(Supplier<java.sql.Connection> connections) {
        super(connections);
    }

    @Override
    public String getGroupName() {
        return "Thú cưng";
    }

    @Override
    public Set<Role> visibleTo() {
        return Set.of(Role.QUAN_LY, Role.BAN_HANG, Role.CHAM_SOC);
    }

    @Override
    public List<SearchResult> search(String keyword) {
        return execute(keyword, SQL, "tc.maThuCung", "tc.tenThuCung",
                "tc.loaiThuCung", "tc.giongLoai", "kh.hoTen", "kh.soDienThoai");
    }

    @Override
    protected SearchResult map(ResultSet row) throws SQLException {
        return new SearchResult(getGroupName(), row.getString("maThuCung"),
                row.getString("tenThuCung"), row.getString("loaiThuCung") + " · "
                + row.getString("giongLoai") + " · Chủ nuôi: " + row.getString("hoTen")
                + " • " + row.getString("soDienThoai"), Screen.PETS);
    }
}
