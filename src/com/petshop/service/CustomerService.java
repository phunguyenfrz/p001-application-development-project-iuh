package com.petshop.service;

import com.petshop.dao.CustomerDao;
import com.petshop.model.Customer;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Xử lý nghiệp vụ khách hàng.
 * Kiểm tra dữ liệu trước khi gọi CustomerDao.
 */
public class CustomerService {

    // Đối tượng truy cập dữ liệu khách hàng.
    private final CustomerDao customer_dao = new CustomerDao();

    // Bộ sinh phần số ngẫu nhiên của mã khách hàng.
    private final SecureRandom random = new SecureRandom();

    /**
     * Lấy toàn bộ khách hàng.
     *
     * @return Danh sách khách hàng trong database.
     * @throws IllegalStateException Khi đọc dữ liệu thất bại.
     */
    public List<Customer> findAll() {
        try {
            return customer_dao.findAll();
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không tải được khách hàng. Kiểm tra kết nối database.",
                    exception
            );
        }
    }

    /**
     * Lấy khách hàng đang hoạt động cho trang Cập nhật.
     *
     * @return Danh sách khách hàng đang hoạt động.
     * @throws IllegalStateException Khi không đọc được dữ liệu.
     */
    public List<Customer> findActive() {
        try {
            return customer_dao.findActive();
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Không tải được khách hàng đang hoạt động.", exception);
        }
    }
    /**
     * Thêm khách hàng mới sau khi kiểm tra dữ liệu.
     *
     * @param customer Thông tin khách hàng do Controller truyền vào.
     * @throws IllegalArgumentException Khi dữ liệu nhập không hợp lệ.
     * @throws IllegalStateException Khi lưu database thất bại.
     */
    public void insert(Customer customer) {
        // Bản dữ liệu đã được kiểm tra và chuẩn hóa.
        Customer prepared_customer = prepareCustomer(customer);

        prepared_customer.setMaKhachHang(generateCustomerId());
        prepared_customer.setDiemTichLuy(0);

        try {
            customer_dao.insert(prepared_customer);

            // Chỉ cập nhật mã và điểm cho đối tượng gốc khi lưu thành công.
            customer.setMaKhachHang(
                    prepared_customer.getMaKhachHang()
            );
            customer.setDiemTichLuy(0);
        } catch (SQLException exception) {
            throw translateSqlError(exception);
        }
    }

    /**
     * Cập nhật khách hàng sau khi kiểm tra dữ liệu.
     *
     * @param customer Khách hàng chứa mã và thông tin mới.
     * @throws IllegalArgumentException Khi dữ liệu không hợp lệ.
     * @throws IllegalStateException Khi cập nhật database thất bại.
     */
    public void update(Customer customer) {
        // Bản dữ liệu hợp lệ dùng để cập nhật.
        Customer prepared_customer = prepareCustomer(customer);

        validateCustomerId(prepared_customer.getMaKhachHang());

        try {
            // DAO không cập nhật điểm tích lũy hoặc mã khách hàng.
            customer_dao.update(prepared_customer);
        } catch (SQLException exception) {
            throw translateSqlError(exception);
        }
    }

    /**
     * Ngừng hoạt động khách hàng bằng xóa mềm.
     *
     * @param customer_id Mã khách hàng cần ngừng hoạt động.
     * @throws IllegalArgumentException Khi mã khách hàng không hợp lệ.
     * @throws IllegalStateException Khi cập nhật trạng thái thất bại.
     */
    public void delete(String customer_id) {
        validateCustomerId(customer_id);

        try {
            // DAO cập nhật trạng thái, giữ nguyên dữ liệu liên quan.
            customer_dao.delete(customer_id.trim());
        } catch (SQLException exception) {
            throw translateSqlError(exception);
        }
    }

    /**
     * Kiểm tra dữ liệu và tạo bản sao đã chuẩn hóa.
     * Không thay đổi trực tiếp khách hàng đang hiển thị trên bảng.
     *
     * @param customer Thông tin khách hàng cần kiểm tra.
     * @return Bản sao có dữ liệu hợp lệ.
     * @throws IllegalArgumentException Khi thông tin không hợp lệ.
     */
    private Customer prepareCustomer(Customer customer) {
        // Không xử lý nếu Controller chưa truyền đối tượng.
        if (customer == null) {
            throw new IllegalArgumentException(
                    "Chưa có thông tin khách hàng."
            );
        }

        // Giá trị đã loại bỏ khoảng trắng ở đầu và cuối.
        String customer_id = trimText(customer.getMaKhachHang());
        String name = trimText(customer.getHoTen());
        String phone = trimText(customer.getSoDienThoai());
        String email = trimText(customer.getEmail());
        String address = trimText(customer.getDiaChi());
        String note = trimText(customer.getGhiChu());

        // Họ tên bắt buộc, tối đa 100 ký tự theo SQL.
        if (name.isEmpty() || name.length() > 100) {
            throw new IllegalArgumentException(
                    "Họ tên phải có từ 1 đến 100 ký tự."
            );
        }

        // Số điện thoại khớp CHECK trong bảng KhachHang.
        if (!phone.matches("0[0-9]{9}")) {
            throw new IllegalArgumentException(
                    "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0."
            );
        }

        // Email không bắt buộc; nếu nhập thì phải đúng định dạng.
        if (!email.isEmpty()
                && (email.length() > 100
                || !email.matches(
                        "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
                ))) {
            throw new IllegalArgumentException(
                    "Email không hợp lệ hoặc dài quá 100 ký tự."
            );
        }

        // Giới hạn độ dài địa chỉ theo cấu trúc database.
        if (address.length() > 255) {
            throw new IllegalArgumentException(
                    "Địa chỉ không được dài quá 255 ký tự."
            );
        }

        // Giới hạn độ dài ghi chú theo cấu trúc database.
        if (note.length() > 255) {
            throw new IllegalArgumentException(
                    "Ghi chú không được dài quá 255 ký tự."
            );
        }

        // Tạo bản sao; trường tùy chọn để trống được lưu thành NULL.
        return new Customer(
                customer_id,
                name,
                phone,
                email.isEmpty() ? null : email,
                customer.getDiemTichLuy(),
                address.isEmpty() ? null : address,
                note.isEmpty() ? null : note
        );
    }

    /**
     * Kiểm tra mã khách hàng trước khi sửa hoặc xóa.
     *
     * @param customer_id Mã theo định dạng KH-YYYYMMDD-NNNNNN.
     * @throws IllegalArgumentException Khi mã không đúng định dạng.
     */
    private void validateCustomerId(String customer_id) {
        if (customer_id == null
                || !customer_id.trim().matches(
                        "KH-[0-9]{8}-[0-9]{6}"
                )) {
            throw new IllegalArgumentException(
                    "Mã khách hàng không hợp lệ."
            );
        }
    }

    /**
     * Sinh mã theo định dạng khóa chính của bảng KhachHang.
     *
     * @return Mã gồm tiền tố KH, ngày hiện tại và 6 chữ số.
     */
    private String generateCustomerId() {
        // Ngày tạo mã theo định dạng năm, tháng, ngày.
        String date_text = LocalDate.now().format(
                DateTimeFormatter.BASIC_ISO_DATE
        );

        return "KH-" + date_text + "-"
                + String.format(
                        Locale.ROOT, "%06d", random.nextInt(1_000_000)
                );
    }

    /**
     * Chuẩn hóa chuỗi và xử lý giá trị null.
     *
     * @param value Chuỗi cần chuẩn hóa.
     * @return Chuỗi đã bỏ khoảng trắng đầu cuối; rỗng nếu đầu vào null.
     */
    private String trimText(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Chuyển lỗi SQL thành lỗi nghiệp vụ để Controller xử lý.
     *
     * @param exception Lỗi database trả về.
     * @return Ngoại lệ có thông báo dành cho người sử dụng.
     */
    private IllegalStateException translateSqlError(
            SQLException exception
    ) {
        // SQL Server báo trùng khóa chính hoặc cột UNIQUE.
        if (exception.getErrorCode() == 2601
                || exception.getErrorCode() == 2627) {
            return new IllegalStateException(
                    "Số điện thoại hoặc mã khách hàng đã tồn tại. "
                            + "Số điện thoại của khách đã ngừng hoạt động vẫn được giữ; nếu số chưa trùng, "
                            + "hãy thử thêm lại.",
                    exception
            );
        }

        return new IllegalStateException(
                "Không thực hiện được thao tác: "
                        + exception.getMessage(),
                exception
        );
    }
}