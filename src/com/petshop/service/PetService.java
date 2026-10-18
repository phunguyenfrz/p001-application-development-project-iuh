package com.petshop.service;

import com.petshop.dao.PetDao;
import com.petshop.model.Pet;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Xử lý nghiệp vụ hồ sơ thú cưng.
 * Kiểm tra dữ liệu trước khi gọi PetDao.
 */
public class PetService {

    // Đối tượng truy cập dữ liệu thú cưng.
    private final PetDao pet_dao = new PetDao();

    /**
     * Lấy danh sách thú cưng đang được theo dõi.
     *
     * @return Danh sách hồ sơ đang theo dõi.
     * @throws IllegalStateException Khi không đọc được dữ liệu.
     */
    public List<Pet> findActive() {
        try {
            return pet_dao.findActive();
        } catch (SQLException exception) {
            throw translateSqlError(exception);
        }
    }

    /**
     * Lấy toàn bộ hồ sơ để tra cứu, kể cả hồ sơ ngừng theo dõi.
     *
     * @return Danh sách toàn bộ thú cưng.
     * @throws IllegalStateException Khi không đọc được dữ liệu.
     */
    public List<Pet> findAll() {
        try {
            return pet_dao.findAll();
        } catch (SQLException exception) {
            throw translateSqlError(exception);
        }
    }

    /**
     * Thêm hồ sơ thú cưng mới.
     *
     * @param pet Thông tin thú cưng nhập trên form.
     * @throws IllegalArgumentException Khi dữ liệu không hợp lệ.
     * @throws IllegalStateException Khi lưu dữ liệu thất bại.
     */
    public void insert(Pet pet) {
        // Bản dữ liệu đã kiểm tra, không sửa trực tiếp dữ liệu trên form.
        Pet prepared_pet = preparePet(pet);

        try {
            // Chủ nuôi mới phải tồn tại và đang hoạt động.
            if (!pet_dao.existsActiveOwner(
                    prepared_pet.getMaKhachHang()
            )) {
                throw new IllegalArgumentException(
                        "Chọn một chủ nuôi đang hoạt động từ danh sách."
                );
            }

            /*
             * Khi nhiều máy cùng thêm hồ sơ, hai máy có thể cùng đọc
             * một số thứ tự. Khóa chính chặn mã trùng; thử lấy số mới
             * tối đa 5 lần, không ghi đè hồ sơ đã tồn tại.
             */
            for (int attempt = 0; attempt < 5; attempt++) {
                prepared_pet.setMaThuCung(generatePetId());

                try {
                    pet_dao.insert(prepared_pet);

                    // Chỉ cập nhật mã trên đối tượng gốc khi lưu thành công.
                    pet.setMaThuCung(prepared_pet.getMaThuCung());
                    pet.setTrangThai(true);
                    return;
                } catch (SQLException exception) {
                    // Chỉ thử lại khi SQL báo trùng khóa.
                    if (exception.getErrorCode() != 2601
                            && exception.getErrorCode() != 2627) {
                        throw exception;
                    }
                }
            }

            throw new IllegalStateException(
                    "Chưa cấp được mã thú cưng do có thao tác đồng thời. "
                            + "Vui lòng thử lưu lại."
            );
        } catch (SQLException exception) {
            throw translateSqlError(exception);
        }
    }

    /**
     * Cập nhật thông tin hồ sơ đang được theo dõi.
     *
     * @param pet Thông tin mới và mã thú cưng cần cập nhật.
     * @throws IllegalArgumentException Khi dữ liệu không hợp lệ.
     * @throws IllegalStateException Khi cập nhật thất bại.
     */
    public void update(Pet pet) {
        // Bản sao đã được chuẩn hóa và kiểm tra.
        Pet prepared_pet = preparePet(pet);

        validatePetId(prepared_pet.getMaThuCung());

        try {
            // Đọc hồ sơ hiện tại để kiểm tra trạng thái và chủ nuôi cũ.
            Pet current_pet = pet_dao.findById(
                    prepared_pet.getMaThuCung()
            );

            if (current_pet == null || !current_pet.isTrangThai()) {
                throw new IllegalStateException(
                        "Hồ sơ không tồn tại hoặc đã ngừng theo dõi. "
                                + "Hãy làm mới danh sách."
                );
            }

            // Xác định người dùng có đang đổi chủ nuôi hay không.
            boolean owner_changed = !current_pet.getMaKhachHang()
                    .equals(prepared_pet.getMaKhachHang());

            /*
             * Chủ nuôi mới phải đang hoạt động.
             * Nếu giữ nguyên chủ cũ, vẫn cho sửa thông tin thú cưng
             * dù khách hàng đó đã ngừng hoạt động.
             */
            if (owner_changed
                    && !pet_dao.existsActiveOwner(
                            prepared_pet.getMaKhachHang()
                    )) {
                throw new IllegalArgumentException(
                        "Chủ nuôi mới phải là khách hàng đang hoạt động."
                );
            }

            pet_dao.update(prepared_pet);
        } catch (SQLException exception) {
            throw translateSqlError(exception);
        }
    }

    /**
     * Ngừng theo dõi hồ sơ thú cưng bằng xóa mềm.
     *
     * @param pet_id Mã thú cưng cần ngừng theo dõi.
     * @throws IllegalArgumentException Khi mã không hợp lệ.
     * @throws IllegalStateException Khi cập nhật thất bại.
     */
    public void deactivate(String pet_id) {
        validatePetId(pet_id);

        try {
            pet_dao.deactivate(pet_id.trim());
        } catch (SQLException exception) {
            throw translateSqlError(exception);
        }
    }

    /**
     * Kiểm tra và tạo bản sao dữ liệu thú cưng.
     *
     * @param pet Dữ liệu do Controller truyền vào.
     * @return Hồ sơ có thông tin đã chuẩn hóa.
     * @throws IllegalArgumentException Khi dữ liệu không hợp lệ.
     */
    private Pet preparePet(Pet pet) {
        if (pet == null) {
            throw new IllegalArgumentException(
                    "Chưa có thông tin thú cưng."
            );
        }

        // Các trường văn bản sau khi bỏ khoảng trắng đầu cuối.
        String pet_id = trimText(pet.getMaThuCung());
        String owner_id = trimText(pet.getMaKhachHang());
        String name = trimText(pet.getTenThuCung());
        String species = trimText(pet.getLoaiThuCung());
        String breed = trimText(pet.getGiongLoai());
        String allergy = trimText(pet.getDiUng());
        String gender = trimText(pet.getGioiTinh());

        // Chủ nuôi phải được chọn từ một hồ sơ khách hàng.
        if (!owner_id.matches("KH-[0-9]{8}-[0-9]{6}")) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn chủ nuôi từ danh sách gợi ý."
            );
        }

        // Tên thú cưng bắt buộc và khớp độ dài cột SQL.
        if (name.isEmpty() || name.length() > 100) {
            throw new IllegalArgumentException(
                    "Tên thú cưng phải có từ 1 đến 100 ký tự."
            );
        }

        if (species.length() > 50) {
            throw new IllegalArgumentException(
                    "Loài không được dài quá 50 ký tự."
            );
        }

        if (breed.length() > 100) {
            throw new IllegalArgumentException(
                    "Giống không được dài quá 100 ký tự."
            );
        }

        if (allergy.length() > 255) {
            throw new IllegalArgumentException(
                    "Nội dung dị ứng không được dài quá 255 ký tự."
            );
        }

        // Database chỉ chấp nhận Đực, Cái hoặc giá trị null.
        if (!gender.isEmpty()
                && !"Đực".equals(gender)
                && !"Cái".equals(gender)) {
            throw new IllegalArgumentException(
                    "Giới tính phải là Đực hoặc Cái."
            );
        }

        // Cân nặng trong SQL có kiểu DECIMAL(5,2).
        double weight = pet.getCanNang();

        if (!Double.isFinite(weight)
                || weight <= 0
                || weight > 999.99) {
            throw new IllegalArgumentException(
                    "Cân nặng phải lớn hơn 0 và không vượt quá 999,99 kg."
            );
        }

        // Không tự làm tròn khi người dùng nhập quá hai số thập phân.
        try {
            BigDecimal.valueOf(weight).setScale(
                    2,
                    RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "Cân nặng chỉ được có tối đa 2 chữ số thập phân."
            );
        }

        // Ngày hiện tại theo múi giờ Việt Nam.
        LocalDate today = LocalDate.now(
                ZoneId.of("Asia/Ho_Chi_Minh")
        );

        if (pet.getNgaySinh() != null
                && pet.getNgaySinh().isAfter(today)) {
            throw new IllegalArgumentException(
                    "Ngày sinh không được nằm trong tương lai."
            );
        }

        // Tạo đối tượng riêng để không đổi dữ liệu đang hiển thị.
        Pet prepared_pet = new Pet(
                pet_id,
                owner_id,
                name,
                nullableText(species),
                nullableText(breed),
                weight,
                pet.getNgaySinh(),
                nullableText(allergy),
                nullableText(gender)
        );

        prepared_pet.setTrangThai(pet.isTrangThai());

        return prepared_pet;
    }

    /**
     * Sinh mã thú cưng theo ngày Việt Nam và số thứ tự trong ngày.
     *
     * @return Mã theo định dạng TC-YYYYMMDD-NNNNNN.
     * @throws SQLException Khi không đọc được số thứ tự.
     * @throws IllegalStateException Khi hết số thứ tự trong ngày.
     */
    private String generatePetId() throws SQLException {
        // Ngày tạo hồ sơ theo múi giờ Việt Nam.
        String date_text = LocalDate.now(
                ZoneId.of("Asia/Ho_Chi_Minh")
        ).format(DateTimeFormatter.BASIC_ISO_DATE);

        // Số thứ tự tiếp theo do DAO đọc từ database.
        int next_number = pet_dao.findNextDailyNumber(date_text);

        if (next_number > 999999) {
            throw new IllegalStateException(
                    "Đã hết số thứ tự hồ sơ thú cưng trong ngày."
            );
        }

        return "TC-" + date_text + "-"
                + String.format(
                        Locale.ROOT,
                        "%06d",
                        next_number
                );
    }

    /**
     * Kiểm tra định dạng mã thú cưng trước khi sửa hoặc ngừng theo dõi.
     *
     * @param pet_id Mã theo định dạng TC-YYYYMMDD-NNNNNN.
     * @throws IllegalArgumentException Khi mã không hợp lệ.
     */
    private void validatePetId(String pet_id) {
        if (pet_id == null
                || !pet_id.trim().matches(
                        "TC-[0-9]{8}-[0-9]{6}"
                )) {
            throw new IllegalArgumentException(
                    "Mã thú cưng không hợp lệ."
            );
        }
    }

    /**
     * Bỏ khoảng trắng đầu cuối và xử lý null.
     *
     * @param value Chuỗi cần chuẩn hóa.
     * @return Chuỗi đã chuẩn hóa; rỗng nếu đầu vào null.
     */
    private String trimText(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Chuyển chuỗi trống thành null cho trường không bắt buộc.
     *
     * @param value Chuỗi đã chuẩn hóa.
     * @return Chuỗi ban đầu hoặc null.
     */
    private String nullableText(String value) {
        return value.isEmpty() ? null : value;
    }

    /**
     * Chuyển lỗi SQL thành thông báo nghiệp vụ.
     *
     * @param exception Lỗi database trả về.
     * @return Ngoại lệ có thông báo dành cho người sử dụng.
     */
    private IllegalStateException translateSqlError(
            SQLException exception
    ) {
        // SQL báo trùng khóa chính hoặc ràng buộc duy nhất.
        if (exception.getErrorCode() == 2601
                || exception.getErrorCode() == 2627) {
            return new IllegalStateException(
                    "Mã hồ sơ thú cưng đã tồn tại. Vui lòng thử lại.",
                    exception
            );
        }

        /*
         * Khóa ngoại bảo vệ quan hệ thú cưng, chủ nuôi và lịch hẹn.
         * Không tự đổi hoặc xóa dữ liệu lịch sử để vượt qua ràng buộc.
         */
        if (exception.getErrorCode() == 547) {
            return new IllegalStateException(
                    "Không lưu được do ràng buộc dữ liệu. "
                            + "Kiểm tra chủ nuôi; thú cưng đã có lịch hẹn "
                            + "không thể đổi chủ nuôi theo cấu trúc hiện tại.",
                    exception
            );
        }

        return new IllegalStateException(
                "Không thực hiện được thao tác thú cưng: "
                        + exception.getMessage(),
                exception
        );
    }
}