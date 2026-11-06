package com.petshop.bus;

import com.petshop.dao.TaiKhoanDAO;
import com.petshop.entity.TaiKhoan;
import com.petshop.util.HashUtil;
import com.petshop.util.SessionManager;

/**
 * Lớp dịch vụ xử lý nghiệp vụ xác thực đăng nhập, kiểm tra quyền hạn tài khoản và cấp lại mật khẩu.
 * Tuân thủ Điều 3, 5, 6 Bộ chỉ thị kỹ thuật IUH:
 * - Đăng nhập bằng Mã nhân viên hoặc Tên đăng nhập.
 * - Mật khẩu băm SHA-256 qua HashUtil.sha256().
 * - Quên mật khẩu: Validate regex SĐT 10 số, ném IllegalArgumentException nếu sai.
 */
public class XacThucBUS {

    private static final String REGEX_SDT = "^0\\d{9}$";
    private static final String REGEX_EMAIL = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
    public static final String MAT_KHAU_MAC_DINH_RESET = "123456";

    private final TaiKhoanDAO taiKhoanDAO;

    public XacThucBUS() {
        this(new TaiKhoanDAO());
    }

    public XacThucBUS(TaiKhoanDAO taiKhoanDAO) {
        this.taiKhoanDAO = taiKhoanDAO;
    }

    /**
     * Xác thực thông tin đăng nhập của người dùng qua Mã nhân viên hoặc Tên đăng nhập.
     * Mật khẩu được băm SHA-256 khi kiểm tra (Bộ chỉ thị kỹ thuật IUH - Điều 5).
     */
    public TaiKhoan dangNhap(String maNhanVienHoacTenDangNhap, String matKhau) {
        if (maNhanVienHoacTenDangNhap == null || maNhanVienHoacTenDangNhap.isBlank() || matKhau == null || matKhau.isEmpty()) {
            return null;
        }

        TaiKhoan tk = taiKhoanDAO.dangNhap(maNhanVienHoacTenDangNhap.trim(), matKhau);
        if (tk != null) {
            SessionManager.dangNhap(tk);
        }
        return tk;
    }

    public TaiKhoan xacThuc(String maNhanVienHoacTenDangNhap, String matKhau) {
        return dangNhap(maNhanVienHoacTenDangNhap, matKhau);
    }

    /**
     * Xử lý khôi phục mật khẩu (Quên mật khẩu).
     * Yêu cầu nhập Mã NV + SĐT + Email khớp quy chuẩn, sau đó đặt lại mật khẩu về 123456.
     *
     * @param maNhanVien Mã nhân viên (bắt buộc)
     * @param soDienThoai Số điện thoại (10 chữ số, bắt đầu bằng 0)
     * @param email Địa chỉ email hợp lệ
     * @return true nếu cấp lại mật khẩu thành công
     */
    public boolean quenMatKhau(String maNhanVien, String soDienThoai, String email) {
        if (maNhanVien == null || maNhanVien.isBlank()) {
            throw new IllegalArgumentException("Mã nhân viên không được để trống!");
        }

        if (soDienThoai == null || !soDienThoai.trim().matches(REGEX_SDT)) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ! Phải gồm đúng 10 chữ số và bắt đầu bằng số 0.");
        }

        if (email == null || !email.trim().matches(REGEX_EMAIL)) {
            throw new IllegalArgumentException("Địa chỉ email không đúng định dạng (ví dụ: nhanvien@happytails.com)!");
        }

        boolean ketQua = taiKhoanDAO.khoiPhucMatKhau(maNhanVien.trim(), soDienThoai.trim(), MAT_KHAU_MAC_DINH_RESET);
        if (!ketQua) {
            throw new IllegalArgumentException("Thông tin Mã nhân viên hoặc Số điện thoại không khớp với hồ sơ nhân sự đang làm việc!");
        }

        return true;
    }

    /**
     * Xử lý nghiệp vụ cấp lại mật khẩu (Quên mật khẩu).
     * Ràng buộc nghiệp vụ:
     * - Mã nhân viên không được để trống.
     * - Số điện thoại bắt buộc 10 số bắt đầu bằng 0 (^0\\d{9}$).
     * - Phải khớp thông tin nhân viên đang làm việc trong CSDL.
     * - Mật khẩu mới tối thiểu 4 ký tự.
     * - Xác nhận mật khẩu mới phải khớp.
     * - Mật khẩu mới băm SHA-256 qua HashUtil.sha256() trước khi lưu.
     */
    public boolean datLaiMatKhau(String maNhanVien, String soDienThoai, String matKhauMoi, String xacNhanMatKhau) {
        if (maNhanVien == null || maNhanVien.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập mã nhân viên!");
        }
        if (soDienThoai == null || soDienThoai.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập số điện thoại xác minh!");
        }
        String sdt = soDienThoai.trim();
        if (!sdt.matches("^0\\d{9}$")) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ! Phải gồm đúng 10 chữ số và bắt đầu bằng số 0 (VD: 0912345678).");
        }
        if (matKhauMoi == null || matKhauMoi.isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu mới không được để trống!");
        }
        if (matKhauMoi.length() < 4) {
            throw new IllegalArgumentException("Mật khẩu mới phải có tối thiểu 4 ký tự!");
        }
        if (!matKhauMoi.equals(xacNhanMatKhau)) {
            throw new IllegalArgumentException("Xác nhận mật khẩu mới không trùng khớp!");
        }

        String maNv = maNhanVien.trim();
        boolean tonTai = taiKhoanDAO.kiemTraThongTinQuenMatKhau(maNv, sdt);
        if (!tonTai) {
            throw new IllegalArgumentException("Mã nhân viên hoặc Số điện thoại không khớp với hồ sơ nhân sự đang làm việc trong hệ thống!");
        }

        String matKhauHash = HashUtil.sha256(matKhauMoi);
        boolean thanhCong = taiKhoanDAO.datLaiMatKhauTheoMaNhanVien(maNv, matKhauHash);
        if (!thanhCong) {
            throw new IllegalStateException("Không thể cập nhật mật khẩu mới! Vui lòng thử lại sau.");
        }
        return true;
    }

    /**
     * Đăng xuất tài khoản khỏi phiên làm việc.
     */
    public void dangXuat() {
        SessionManager.dangXuat();
    }

    /**
     * Lấy thông tin tài khoản đang đăng nhập hiện tại.
     */
    public TaiKhoan getTaiKhoanHienTai() {
        return SessionManager.getTaiKhoanHienTai();
    }

    /**
     * Kiểm tra tài khoản hiện tại có quyền thực hiện chức năng nghiệp vụ không.
     */
    public boolean coQuyen(String maChucNang) {
        TaiKhoan tk = getTaiKhoanHienTai();
        if (tk == null) {
            return false;
        }
        if (tk.getPhanQuyen() == TaiKhoan.VaiTro.ADMIN) {
            return true;
        }
        return DanhMucMenu.kiemTraQuyen(tk.getPhanQuyen(), maChucNang);
    }

    /**
     * Kiểm tra tài khoản hiện tại có quyền thực hiện chức năng nghiệp vụ không.
     * @throws SecurityException nếu không có quyền hoặc chưa đăng nhập.
     */
    public void kiemTraQuyen(String maChucNang) {
        TaiKhoan tk = getTaiKhoanHienTai();
        if (tk == null) {
            throw new SecurityException("Chưa đăng nhập hệ thống!");
        }

        if (tk.getPhanQuyen() == TaiKhoan.VaiTro.ADMIN) {
            return;
        }

        if (!DanhMucMenu.kiemTraQuyen(tk, maChucNang)) {
            throw new SecurityException("Tài khoản của bạn không có quyền thực hiện chức năng này!");
        }
    }
}
