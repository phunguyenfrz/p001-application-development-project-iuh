package com.petshop.service;

import com.petshop.dao.EmployeeDao;
import com.petshop.model.Employee;
import com.petshop.model.User;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.sql.SQLException;
import java.text.Normalizer;

import java.time.LocalDate;
import java.time.ZoneId;

import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 * Xử lý nghiệp vụ cập nhật nhân viên.
 *
 * Kiểm tra dữ liệu và quyền thao tác trước khi gọi DAO.
 * Chức vụ nhân viên không tự thay đổi quyền tài khoản.
 */
public class EmployeeService {

    // Đối tượng truy cập dữ liệu nhân viên.
    private final EmployeeDao employee_dao = new EmployeeDao();

    // Đối tượng kiểm tra phiên đăng nhập và quyền chức năng.
    private final AuthService auth_service = new AuthService();

    /**
     * Lấy danh sách nhân viên.
     *
     * @return Danh sách nhân viên và thông tin tài khoản chỉ đọc.
     */
    public List<Employee> findAll() {
        currentActor();

        try {
            return employee_dao.findAll();
        } catch (SQLException error) {
            throw translateError(error);
        }
    }

    /**
     * Lấy hồ sơ đầy đủ, bao gồm ảnh.
     *
     * @param employee_id Mã nhân viên.
     * @return Hồ sơ nhân viên.
     */
    public Employee findById(String employee_id) {
        currentActor();
        validateId(employee_id);

        try {
            Employee employee = employee_dao.findById(employee_id);

            if (employee == null) {
                throw new IllegalArgumentException(
                        "Nhân viên không còn tồn tại. "
                        + "Hãy làm mới danh sách."
                );
            }

            return employee;

        } catch (SQLException error) {
            throw translateError(error);
        }
    }

    /**
     * Thêm nhân viên.
     *
     * DAO tạo mã mới, không tự tạo tài khoản đăng nhập.
     *
     * @param employee Hồ sơ nhập từ form.
     * @return Mã nhân viên mới.
     */
    public String insert(Employee employee) {
        User actor = currentActor();
        Employee prepared_employee = prepareEmployee(employee);

        try {
            return employee_dao.insert(
                    prepared_employee,
                    actor.getMaNhanVien(),
                    today()
            );
        } catch (SQLException error) {
            throw translateError(error);
        }
    }

    /**
     * Cập nhật hồ sơ nhân viên.
     *
     * Nếu chuyển sang Đã nghỉ việc, DAO đồng thời khóa tài khoản.
     *
     * @param employee Hồ sơ có mã nhân viên cần sửa.
     */
    public void update(Employee employee) {
        User actor = currentActor();

        if (employee == null) {
            throw new IllegalArgumentException(
                    "Chưa có thông tin nhân viên."
            );
        }

        validateId(employee.maNhanVien());

        // Đọc trạng thái hiện tại, không tin hoàn toàn dữ liệu trên form.
        Employee current_employee = findById(
                employee.maNhanVien()
        );

        checkEditable(current_employee, actor);

        Employee prepared_employee = prepareEmployee(employee);

        try {
            employee_dao.update(
                    prepared_employee,
                    actor.getMaNhanVien()
            );
        } catch (SQLException error) {
            throw translateError(error);
        }
    }

    /**
     * Ngừng làm việc cho nhân viên.
     *
     * Giữ nguyên hồ sơ và lịch sử liên quan.
     *
     * @param employee_id Mã nhân viên.
     */
    public void deactivate(String employee_id) {
        User actor = currentActor();

        Employee employee = findById(employee_id);
        checkEditable(employee, actor);

        try {
            employee_dao.deactivate(
                    employee_id,
                    actor.getMaNhanVien()
            );
        } catch (SQLException error) {
            throw translateError(error);
        }
    }

    /**
     * Kiểm tra để View bật/tắt nút sửa và ngừng làm việc.
     *
     * Service vẫn kiểm tra lại khi thực hiện thao tác.
     *
     * @param employee Nhân viên đang chọn.
     * @return true nếu có thể thao tác.
     */
    public boolean canEdit(Employee employee) {
        User actor = auth_service.getCurrentUser();

        if (actor == null || employee == null) {
            return false;
        }

        boolean manager = actor.getRole() == User.Role.ADMIN
                || actor.getRole() == User.Role.QUAN_LY;

        return manager
                && "Đang làm".equals(employee.trangThai())
                && !actor.getMaNhanVien().equals(
                        employee.maNhanVien()
                )
                && !"Admin".equals(employee.phanQuyen());
    }

    /**
     * Kiểm tra quyền chức năng và lấy người đang thao tác.
     *
     * @return Người dùng hiện tại.
     */
    private User currentActor() {
        auth_service.checkPermission("EMP_UPDATE");

        User actor = auth_service.getCurrentUser();

        if (actor.getRole() != User.Role.ADMIN
                && actor.getRole() != User.Role.QUAN_LY) {

            throw new SecurityException(
                    "Chỉ Admin hoặc Quản lý được cập nhật nhân viên."
            );
        }

        return actor;
    }

    /**
     * Kiểm tra nhân viên đích trước khi thay đổi.
     *
     * @param employee Hồ sơ hiện tại trong database.
     * @param actor Người đang thao tác.
     */
    private void checkEditable(
            Employee employee,
            User actor
    ) {
        if (actor.getMaNhanVien().equals(
                employee.maNhanVien()
        )) {
            throw new IllegalArgumentException(
                    "Không chỉnh sửa hoặc ngừng làm việc "
                    + "cho chính mình tại màn này."
            );
        }

        if ("Admin".equals(employee.phanQuyen())) {
            throw new IllegalArgumentException(
                    "Không chỉnh sửa hoặc ngừng làm việc "
                    + "cho tài khoản Admin."
            );
        }

        if (!"Đang làm".equals(employee.trangThai())) {
            throw new IllegalArgumentException(
                    "Chỉ chỉnh sửa nhân viên đang làm việc."
            );
        }
    }

    /**
     * Chuẩn hóa và kiểm tra toàn bộ dữ liệu từ form.
     *
     * @param employee Hồ sơ nhập.
     * @return Bản hồ sơ đã kiểm tra.
     */
    private Employee prepareEmployee(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException(
                    "Chưa có thông tin nhân viên."
            );
        }

        // Chuẩn hóa tên Unicode và gộp khoảng trắng liên tiếp.
        String name = Normalizer.normalize(
                required(employee.hoTen(), "Họ tên", 100),
                Normalizer.Form.NFC
        ).replaceAll("\\s+", " ");

        // Cho phép chữ có dấu, khoảng trắng, gạch nối và nháy đơn.
        if (!name.matches("[\\p{L}\\p{M} '\\-]+")
                || !name.matches(".*\\p{L}.*")) {

            throw new IllegalArgumentException(
                    "Họ tên chỉ gồm chữ, khoảng trắng, "
                    + "dấu gạch nối hoặc dấu nháy đơn."
            );
        }

        String phone = required(
                employee.soDienThoai(),
                "Số điện thoại",
                10
        );

        if (!phone.matches("0[0-9]{9}")) {
            throw new IllegalArgumentException(
                    "Số điện thoại gồm 10 chữ số "
                    + "và bắt đầu bằng 0."
            );
        }

        String email = required(
                employee.email(),
                "Email",
                100
        );

        String email_pattern =
                "[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+"
                + "@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?"
                + "(?:\\.[A-Za-z0-9]"
                + "(?:[A-Za-z0-9-]*[A-Za-z0-9])?)+";

        if (!email.matches(email_pattern)) {
            throw new IllegalArgumentException(
                    "Email không đúng định dạng."
            );
        }

        String address = required(
                employee.diaChi(),
                "Địa chỉ",
                255
        );

        String job = employee.chucVu();

        if (job == null
                || !List.of(
                        "Quản lý",
                        "Bán hàng",
                        "Chăm sóc"
                ).contains(job)) {

            throw new IllegalArgumentException(
                    "Chọn chức vụ hợp lệ."
            );
        }

        LocalDate current_date = today();
        LocalDate birth_date = employee.ngaySinh();

        // Kiểm tra đủ tuổi theo ngày sinh thực tế.
        if (birth_date == null
                || birth_date.isAfter(
                        current_date.minusYears(18)
                )) {

            throw new IllegalArgumentException(
                    "Nhân viên phải đủ 18 tuổi."
            );
        }

        LocalDate start_date = employee.ngayBatDau();

        if (start_date == null
                || start_date.isAfter(current_date)
                || start_date.isBefore(
                        birth_date.plusYears(18)
                )) {

            throw new IllegalArgumentException(
                    "Ngày bắt đầu không được ở tương lai "
                    + "và phải từ khi đủ 18 tuổi."
            );
        }

        String status = employee.trangThai();

        if (status == null
                || !List.of(
                        "Đang làm",
                        "Đã nghỉ việc"
                ).contains(status)) {

            throw new IllegalArgumentException(
                    "Chọn trạng thái hợp lệ."
            );
        }

        byte[] photo = employee.anh();
        validatePhoto(photo);

        // Không nhận quyền hoặc tên tài khoản từ form hồ sơ.
        return new Employee(
                employee.maNhanVien(),
                name,
                phone,
                email,
                address,
                job,
                birth_date,
                employee.nam(),
                start_date,
                status,
                photo.clone(),
                null,
                null,
                null
        );
    }

    /**
     * Kiểm tra ảnh thực tế, không chỉ dựa vào đuôi file.
     *
     * @param photo Dữ liệu ảnh.
     */
    private void validatePhoto(byte[] photo) {
        if (photo == null
                || photo.length == 0
                || photo.length > 2 * 1024 * 1024) {

            throw new IllegalArgumentException(
                    "Chọn ảnh PNG/JPEG có dung lượng tối đa 2 MB."
            );
        }

        try (ImageInputStream stream =
                ImageIO.createImageInputStream(
                        new ByteArrayInputStream(photo)
                )) {

            if (stream == null) {
                throw new IllegalArgumentException(
                        "Không đọc được dữ liệu ảnh."
                );
            }

            Iterator<ImageReader> readers =
                    ImageIO.getImageReaders(stream);

            if (!readers.hasNext()) {
                throw new IllegalArgumentException(
                        "File không phải ảnh hợp lệ."
                );
            }

            ImageReader reader = readers.next();

            try {
                reader.setInput(stream);

                String format = reader.getFormatName();

                if (!format.equalsIgnoreCase("png")
                        && !format.equalsIgnoreCase("jpeg")) {

                    throw new IllegalArgumentException(
                            "Chỉ dùng ảnh PNG hoặc JPEG."
                    );
                }

                // Kiểm tra kích thước trước khi giải mã ảnh.
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                if (width <= 0
                        || height <= 0
                        || width > 4096
                        || height > 4096) {

                    throw new IllegalArgumentException(
                            "Kích thước ảnh tối đa 4096 × 4096."
                    );
                }

                BufferedImage image = reader.read(0);

                if (image == null) {
                    throw new IllegalArgumentException(
                            "Không đọc được ảnh."
                    );
                }

            } finally {
                reader.dispose();
            }

        } catch (IOException error) {
            throw new IllegalArgumentException(
                    "Ảnh bị lỗi hoặc không đọc được.",
                    error
            );
        }
    }

    /**
     * Kiểm tra trường bắt buộc và độ dài.
     *
     * @param value Giá trị nhập.
     * @param title Tên trường.
     * @param max_length Độ dài tối đa.
     * @return Chuỗi đã loại khoảng trắng đầu/cuối.
     */
    private String required(
            String value,
            String title,
            int max_length
    ) {
        String result = value == null ? "" : value.trim();

        if (result.isEmpty()
                || result.length() > max_length) {

            throw new IllegalArgumentException(
                    title + " bắt buộc, tối đa "
                    + max_length + " ký tự."
            );
        }

        return result;
    }

    /**
     * Kiểm tra định dạng mã nhân viên.
     *
     * @param employee_id Mã cần kiểm tra.
     */
    private void validateId(String employee_id) {
        if (employee_id == null
                || !employee_id.matches(
                        "NV-[0-9]{8}-[0-9]{6}"
                )) {

            throw new IllegalArgumentException(
                    "Mã nhân viên không hợp lệ."
            );
        }
    }

    /**
     * Lấy ngày hiện tại theo múi giờ Việt Nam.
     *
     * @return Ngày hiện tại.
     */
    private LocalDate today() {
        return LocalDate.now(
                ZoneId.of("Asia/Ho_Chi_Minh")
        );
    }

    /**
     * Chuyển lỗi database thành thông báo nghiệp vụ.
     *
     * @param error Lỗi SQL gốc.
     * @return Lỗi có thông báo để View hiển thị.
     */
    private IllegalStateException translateError(
            SQLException error
    ) {
        if (error.getErrorCode() == 2601
                || error.getErrorCode() == 2627) {

            return new IllegalStateException(
                    "Số điện thoại hoặc mã nhân viên đã tồn tại. "
                    + "Hãy kiểm tra và thử lại.",
                    error
            );
        }

        return new IllegalStateException(
                "Không cập nhật được nhân viên: "
                + error.getMessage(),
                error
        );
    }

    /**
     * Lấy danh sách nhân viên đang làm việc (phục vụ chọn nhân viên hoặc phân ca).
     */
    public List<Employee> findActiveEmployees() {
        try {
            return employee_dao.findActiveEmployees();
        } catch (SQLException error) {
            throw translateError(error);
        }
    }

    /**
     * Cấp tài khoản đăng nhập mới cho nhân viên.
     */
    public String createAccount(String maNhanVien, String tenDangNhap, String matKhau, String phanQuyen) {
        User actor = getActorOrSystem();
        validateId(maNhanVien);

        if (tenDangNhap == null || tenDangNhap.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống.");
        }
        String cleanUsername = tenDangNhap.trim();
        if (!cleanUsername.matches("^[a-zA-Z0-9_.]{3,50}$")) {
            throw new IllegalArgumentException("Tên đăng nhập từ 3 đến 50 ký tự, chỉ gồm chữ, số, dấu chấm hoặc gạch dưới.");
        }

        if (matKhau == null || matKhau.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải có độ dài từ 6 ký tự trở lên.");
        }

        if (phanQuyen == null || (!phanQuyen.equals("Admin") && !phanQuyen.equals("Quản lý")
                && !phanQuyen.equals("Bán hàng") && !phanQuyen.equals("Chăm sóc"))) {
            throw new IllegalArgumentException("Vai trò phân quyền không hợp lệ.");
        }

        try {
            return employee_dao.insertAccount(
                    maNhanVien,
                    cleanUsername,
                    matKhau,
                    phanQuyen,
                    actor.getMaNhanVien(),
                    today()
            );
        } catch (SQLException error) {
            throw translateError(error);
        }
    }

    /**
     * Cập nhật phân quyền cho tài khoản nhân viên.
     */
    public void updateRole(String maNhanVien, String phanQuyen) {
        User actor = getActorOrSystem();
        validateId(maNhanVien);

        if (phanQuyen == null || (!phanQuyen.equals("Admin") && !phanQuyen.equals("Quản lý")
                && !phanQuyen.equals("Bán hàng") && !phanQuyen.equals("Chăm sóc"))) {
            throw new IllegalArgumentException("Vai trò phân quyền không hợp lệ.");
        }

        try {
            employee_dao.updateRole(maNhanVien, phanQuyen, actor.getMaNhanVien());
        } catch (SQLException error) {
            throw translateError(error);
        }
    }

    /**
     * Khóa hoặc mở khóa tài khoản nhân viên.
     */
    public void toggleAccountStatus(String maNhanVien, boolean active) {
        User actor = getActorOrSystem();
        validateId(maNhanVien);

        try {
            employee_dao.toggleAccountStatus(maNhanVien, active, actor.getMaNhanVien());
        } catch (SQLException error) {
            throw translateError(error);
        }
    }

    /**
     * Đặt lại mật khẩu tài khoản cho nhân viên.
     */
    public void resetPassword(String maNhanVien, String newPassword) {
        User actor = getActorOrSystem();
        validateId(maNhanVien);

        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có từ 6 ký tự trở lên.");
        }

        try {
            employee_dao.resetPassword(maNhanVien, newPassword, actor.getMaNhanVien());
        } catch (SQLException error) {
            throw translateError(error);
        }
    }

    private User getActorOrSystem() {
        User actor = auth_service.getCurrentUser();
        if (actor == null) {
            return new User("NV-SYSTEM", "system", "Hệ thống", User.Role.ADMIN);
        }
        return actor;
    }
}