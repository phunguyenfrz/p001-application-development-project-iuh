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

public final class AppointmentSearchProvider extends AbstractSqlSearchProvider {
    private static final String SQL = """
            SELECT lh.maLichHen, tc.tenThuCung, kh.hoTen, kh.soDienThoai,
                   lh.ngayHen, lh.gioHen, lh.trangThai
            FROM LichHen lh
            JOIN ThuCung tc ON tc.maThuCung = lh.maThuCung
            JOIN KhachHang kh ON kh.maKhachHang = lh.maKhachHang
            WHERE {search}
            ORDER BY lh.ngayHen DESC, lh.gioHen DESC, lh.maLichHen
            """;

    public AppointmentSearchProvider() {
        this(DbConnection::getConnection);
    }

    public AppointmentSearchProvider(Supplier<java.sql.Connection> connections) {
        super(connections);
    }

    @Override
    public String getGroupName() {
        return "Lịch hẹn";
    }

    @Override
    public Set<Role> visibleTo() {
        return Set.of(Role.CHAM_SOC);
    }

    @Override
    public List<SearchResult> search(String keyword) {
        return execute(keyword, SQL, "lh.maLichHen", "tc.tenThuCung", "kh.hoTen");
    }

    @Override
    protected SearchResult map(ResultSet row) throws SQLException {
        String detail = row.getString("hoTen") + " • " + row.getString("soDienThoai")
                + " • " + row.getDate("ngayHen") + " " + row.getTime("gioHen")
                + " • " + row.getString("trangThai");
        return new SearchResult(getGroupName(), row.getString("maLichHen"),
                row.getString("tenThuCung"), detail, Screen.APPOINTMENTS);
    }
}
