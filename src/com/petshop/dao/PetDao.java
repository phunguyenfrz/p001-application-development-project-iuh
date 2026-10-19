package com.petshop.dao;

import com.petshop.model.Pet;
import com.petshop.util.DatabaseManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Truy cập dữ liệu hồ sơ thú cưng.
 * Hỗ trợ đọc, thêm, sửa và ngừng theo dõi bằng xóa mềm.
 */
public class PetDao {

    /**
     * Lấy các thú cưng đang được theo dõi.
     *
     * @return Danh sách hồ sơ có trạng thái bằng 1.
     * @throws SQLException Khi truy vấn thất bại.
     */
    public List<Pet> findActive() throws SQLException {
        return findPets(true, null);
    }

    /**
     * Lấy toàn bộ thú cưng, kể cả hồ sơ đã ngừng theo dõi.
     *
     * @return Danh sách toàn bộ hồ sơ thú cưng.
     * @throws SQLException Khi truy vấn thất bại.
     */
    public List<Pet> findAll() throws SQLException {
        return findPets(false, null);
    }

    /**
     * Tìm hồ sơ theo mã thú cưng.
     *
     * @param pet_id Mã thú cưng cần tìm.
     * @return Hồ sơ tương ứng hoặc null nếu không tồn tại.
     * @throws SQLException Khi truy vấn thất bại.
     */
    public Pet findById(String pet_id) throws SQLException {
        // Danh sách kết quả; mã thú cưng là khóa chính nên tối đa một dòng.
        List<Pet> pet_list = findPets(false, pet_id);

        return pet_list.isEmpty() ? null : pet_list.get(0);
    }

    /**
     * Truy vấn hồ sơ thú cưng kèm thông tin chủ nuôi.
     *
     * @param active_only Chỉ lấy hồ sơ đang theo dõi nếu bằng true.
     * @param pet_id      Mã cần tìm; null nếu không lọc theo mã.
     * @return Danh sách hồ sơ phù hợp.
     * @throws SQLException Khi truy vấn thất bại.
     */
    private List<Pet> findPets(
            boolean active_only,
            String pet_id) throws SQLException {

        // Danh sách thú cưng trả về cho Service.
        List<Pet> pet_list = new ArrayList<>();

        /*
         * Tên và số điện thoại chủ nuôi lấy từ bảng KhachHang.
         * Không lưu trùng hai thông tin này trong bảng ThuCung.
         */
        StringBuilder sql_query = new StringBuilder("""
                SELECT tc.maThuCung,
                       tc.maKhachHang,
                       tc.tenThuCung,
                       tc.loaiThuCung,
                       tc.giongLoai,
                       tc.canNang,
                       tc.ngaySinh,
                       tc.diUng,
                       tc.gioiTinh,
                       tc.trangThai,
                       kh.hoTen AS tenChuNuoi,
                       kh.soDienThoai AS soDienThoaiChuNuoi
                FROM dbo.ThuCung tc
                JOIN dbo.KhachHang kh
                    ON kh.maKhachHang = tc.maKhachHang
                WHERE 1 = 1
                """);

        // Trang Cập nhật chỉ lấy thú cưng đang theo dõi.
        if (active_only) {
            sql_query.append(" AND tc.trangThai = 1");
        }

        // Tìm chính xác theo mã nếu được yêu cầu.
        if (pet_id != null) {
            sql_query.append(" AND tc.maThuCung = ?");
        }

        sql_query.append(
                " ORDER BY tc.tenThuCung, tc.maThuCung");

        try (Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql_query.toString())) {

            if (pet_id != null) {
                statement.setString(1, pet_id);
            }

            try (ResultSet result_set = statement.executeQuery()) {
                // Chuyển từng dòng SQL thành đối tượng Pet.
                while (result_set.next()) {
                    pet_list.add(mapPet(result_set));
                }
            }
        }

        return pet_list;
    }

    /**
     * Thêm hồ sơ thú cưng mới.
     * Mã thú cưng do tầng Service cung cấp.
     *
     * @param pet Thú cưng cần thêm.
     * @throws SQLException Khi thêm dữ liệu thất bại.
     */
    public void insert(Pet pet) throws SQLException {
        // Hồ sơ mới mặc định đang được theo dõi.
        String sql_query = """
                INSERT INTO dbo.ThuCung (
                    maKhachHang, tenThuCung, loaiThuCung,
                    giongLoai, canNang, ngaySinh, diUng,
                    gioiTinh, maThuCung, trangThai
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1)
                """;

        try (Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql_query)) {

            setPetParameters(statement, pet);
            statement.setString(9, pet.getMaThuCung());

            if (statement.executeUpdate() != 1) {
                throw new SQLException(
                        "Không thêm được hồ sơ thú cưng.");
            }
        }
    }

    /**
     * Cập nhật hồ sơ thú cưng đang được theo dõi.
     * Không thay đổi mã thú cưng hoặc trạng thái theo dõi.
     *
     * @param pet Hồ sơ chứa thông tin mới.
     * @throws SQLException Khi cập nhật thất bại.
     */
    public void update(Pet pet) throws SQLException {
        // Chỉ cập nhật hồ sơ vẫn đang được theo dõi.
        String sql_query = """
                UPDATE dbo.ThuCung
                SET maKhachHang = ?,
                    tenThuCung = ?,
                    loaiThuCung = ?,
                    giongLoai = ?,
                    canNang = ?,
                    ngaySinh = ?,
                    diUng = ?,
                    gioiTinh = ?
                WHERE maThuCung = ? AND trangThai = 1
                """;

        try (Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql_query)) {

            setPetParameters(statement, pet);
            statement.setString(9, pet.getMaThuCung());

            // Hồ sơ có thể đã bị ngừng theo dõi bởi người khác.
            if (statement.executeUpdate() != 1) {
                throw new SQLException(
                        "Hồ sơ không tồn tại hoặc đã ngừng theo dõi. "
                                + "Hãy làm mới danh sách.");
            }
        }
    }

    /**
     * Ngừng theo dõi thú cưng bằng xóa mềm.
     * Không xóa hồ sơ hoặc lịch sử chăm sóc liên quan.
     *
     * @param pet_id Mã thú cưng cần ngừng theo dõi.
     * @throws SQLException Khi cập nhật trạng thái thất bại.
     */
    public void deactivate(String pet_id) throws SQLException {
        // Chỉ chuyển trạng thái từ đang theo dõi sang ngừng theo dõi.
        String sql_query = """
                UPDATE dbo.ThuCung
                SET trangThai = 0
                WHERE maThuCung = ? AND trangThai = 1
                """;

        try (Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql_query)) {

            statement.setString(1, pet_id);

            if (statement.executeUpdate() != 1) {
                throw new SQLException(
                        "Hồ sơ không tồn tại hoặc đã ngừng theo dõi. "
                                + "Hãy làm mới danh sách.");
            }
        }
    }

    /**
     * Kiểm tra khách hàng có tồn tại và đang hoạt động.
     *
     * @param customer_id Mã chủ nuôi cần kiểm tra.
     * @return true nếu khách hàng tồn tại và đang hoạt động.
     * @throws SQLException Khi truy vấn thất bại.
     */
    public boolean existsActiveOwner(String customer_id)
            throws SQLException {

        // Chủ nuôi mới phải là khách hàng đang hoạt động.
        String sql_query = """
                SELECT 1
                FROM dbo.KhachHang
                WHERE maKhachHang = ? AND trangThai = 1
                """;

        try (Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql_query)) {

            statement.setString(1, customer_id);

            try (ResultSet result_set = statement.executeQuery()) {
                return result_set.next();
            }
        }
    }

    /**
     * Lấy số thứ tự tiếp theo của hồ sơ thú cưng trong ngày.
     * Tính cả hồ sơ ngừng theo dõi để không tái sử dụng mã cũ.
     *
     * @param date_text Ngày tạo mã theo định dạng yyyyMMdd.
     * @return Số thứ tự tiếp theo, bắt đầu từ 1.
     * @throws SQLException Khi truy vấn thất bại.
     */
    public int findNextDailyNumber(String date_text)
            throws SQLException {

        // Lấy phần số thứ tự lớn nhất của các mã trong ngày.
        String sql_query = """
                SELECT ISNULL(
                    MAX(CAST(RIGHT(maThuCung, 6) AS INT)),
                    0
                ) + 1 AS nextNumber
                FROM dbo.ThuCung
                WHERE maThuCung LIKE ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql_query)) {

            statement.setString(1, "TC-" + date_text + "-%");

            try (ResultSet result_set = statement.executeQuery()) {
                if (result_set.next()) {
                    return result_set.getInt("nextNumber");
                }

                throw new SQLException(
                        "Không xác định được số thứ tự hồ sơ thú cưng.");
            }
        }
    }

    /**
     * Gán tám tham số dùng chung cho thao tác thêm và sửa.
     *
     * @param statement Câu lệnh SQL đang được chuẩn bị.
     * @param pet       Thông tin thú cưng đã được Service kiểm tra.
     * @throws SQLException Khi gán tham số thất bại.
     */
    private void setPetParameters(
            PreparedStatement statement,
            Pet pet) throws SQLException {

        statement.setString(1, pet.getMaKhachHang());
        statement.setString(2, pet.getTenThuCung());
        statement.setString(3, pet.getLoaiThuCung());
        statement.setString(4, pet.getGiongLoai());

        // Cột canNang trong SQL dùng kiểu DECIMAL.
        statement.setBigDecimal(
                5,
                BigDecimal.valueOf(pet.getCanNang()));

        // Ngày sinh không bắt buộc trong database.
        if (pet.getNgaySinh() == null) {
            statement.setNull(6, Types.DATE);
        } else {
            statement.setDate(
                    6,
                    java.sql.Date.valueOf(pet.getNgaySinh()));
        }

        statement.setString(7, pet.getDiUng());
        statement.setString(8, pet.getGioiTinh());
    }

    /**
     * Chuyển dòng SQL hiện tại thành hồ sơ thú cưng.
     *
     * @param result_set Kết quả đang trỏ tới một dòng hợp lệ.
     * @return Thú cưng kèm thông tin chủ nuôi.
     * @throws SQLException Khi đọc dữ liệu thất bại.
     */
    private Pet mapPet(ResultSet result_set) throws SQLException {
        // Ngày sinh có thể null trong database.
        java.sql.Date birth_date = result_set.getDate("ngaySinh");

        // Tạo hồ sơ từ các trường của bảng ThuCung.
        Pet pet = new Pet(
                result_set.getString("maThuCung"),
                result_set.getString("maKhachHang"),
                result_set.getString("tenThuCung"),
                result_set.getString("loaiThuCung"),
                result_set.getString("giongLoai"),
                result_set.getDouble("canNang"),
                birth_date == null ? null : birth_date.toLocalDate(),
                result_set.getString("diUng"),
                result_set.getString("gioiTinh"));

        // Bổ sung trạng thái và thông tin chủ nuôi để hiển thị.
        pet.setTrangThai(result_set.getBoolean("trangThai"));
        pet.setTenChuNuoi(result_set.getString("tenChuNuoi"));
        pet.setSoDienThoaiChuNuoi(
                result_set.getString("soDienThoaiChuNuoi"));

        return pet;
    }
}