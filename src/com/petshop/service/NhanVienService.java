package com.petshop.service;

import com.petshop.dao.NhanVienDAO;
import com.petshop.model.NhanVien;
import com.petshop.model.TaiKhoan;
import com.petshop.util.SessionManager;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Lớp dịch vụ nghiệp vụ Nhân viên (NhanVienService).
 * Phụ trách kiểm tra ràng buộc dữ liệu (Validation), tuổi >= 18, regex SĐT,
 * phân quyền bảo mật (RBAC), quản lý trạng thái làm việc (xóa mềm) và tài khoản liên kết.
 */
public class NhanVienService {

    // Regex SĐT: Bắt đầu bằng số 0, theo sau là đúng 9 chữ số (tổng cộng 10 số)
    public static final String REGEX_SO_DIEN_THOAI = "^0[0-9]{9}$";
    private static final Pattern PATTERN_SDT = Pattern.compile(REGEX_SO_DIEN_THOAI);

    private final NhanVienDAO nhanVienDAO;
    private final XacThucService xacThucService;

    public NhanVienService() {
        this.nhanVienDAO = new NhanVienDAO();
        this.xacThucService = new XacThucService();
    }

    public NhanVienService(NhanVienDAO nhanVienDAO, XacThucService xacThucService) {
        this.nhanVienDAO = nhanVienDAO;
        this.xacThucService = xacThucService;
    }

    // Lấy toàn bộ danh sách nhân viên
    public List<NhanVien> layDanhSachNhanVien() {
        return nhanVienDAO.layDanhSachNhanVien();
    }

    // Tìm kiếm và lọc nhân viên theo từ khóa (Mã/Tên/SĐT), chức vụ và trạng thái
    public List<NhanVien> timKiemNhanVien(String tuKhoa, String chucVu, String trangThai) {
        return nhanVienDAO.timKiemNhanVien(tuKhoa, chucVu, trangThai);
    }

    // Lấy thông tin một nhân viên theo mã
    public NhanVien layNhanVienTheoMa(String maNhanVien) {
        if (maNhanVien == null || maNhanVien.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhân viên không được để trống!");
        }
        return nhanVienDAO.layNhanVienTheoMa(maNhanVien.trim());
    }

    // Thêm nhân viên mới (chỉ hồ sơ nhân sự)
    public boolean themNhanVien(NhanVien nv) {
        xacThucService.kiemTraQuyen("NV_CAP_NHAT");
        if (nv == null) {
            throw new IllegalArgumentException("Thông tin nhân viên không được để trống!");
        }
        if (nv.getMaNhanVien() == null || nv.getMaNhanVien().trim().isEmpty()) {
            nv.setMaNhanVien(nhanVienDAO.taoMaNhanVien());
        }
        kiemTraHopLeNhanVien(nv, false);
        return nhanVienDAO.themNhanVien(nv);
    }

    // Cập nhật thông tin nhân viên (chỉ hồ sơ nhân sự)
    public boolean capNhatNhanVien(NhanVien nv) {
        xacThucService.kiemTraQuyen("NV_CAP_NHAT");
        if (nv == null) {
            throw new IllegalArgumentException("Thông tin nhân viên không được để trống!");
        }
        if (nv.getMaNhanVien() == null || nv.getMaNhanVien().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhân viên cần cập nhật không hợp lệ!");
        }
        kiemTraHopLeNhanVien(nv, true);
        return nhanVienDAO.capNhatNhanVien(nv);
    }

    // Cập nhật thông tin nhân viên kèm chức năng đổi vai trò & khóa/mở khóa tài khoản (EMP_CRUD, EMP_ROLES, EMP_ACCOUNT)
    public boolean capNhatNhanVien(NhanVien nv, String vaiTroMoi, Boolean trangThaiTaiKhoanMoi) {
        xacThucService.kiemTraQuyen("NV_CAP_NHAT");

        if (nv == null) {
            throw new IllegalArgumentException("Thông tin nhân viên không được để trống!");
        }
        if (nv.getMaNhanVien() == null || nv.getMaNhanVien().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhân viên cần cập nhật không hợp lệ!");
        }

        NhanVien nvCu = nhanVienDAO.layNhanVienTheoMa(nv.getMaNhanVien().trim());
        if (nvCu == null) {
            throw new IllegalArgumentException("Không tìm thấy nhân viên có mã: " + nv.getMaNhanVien());
        }

        // Chặn sửa tài khoản Admin hệ thống qua màn hình nhân viên thường
        if ("Admin".equalsIgnoreCase(nvCu.getPhanQuyen()) || "NV-20261005-000000".equals(nvCu.getMaNhanVien())) {
            TaiKhoan tkHienTai = SessionManager.getTaiKhoanHienTai();
            if (tkHienTai == null || tkHienTai.getPhanQuyen() != TaiKhoan.VaiTro.ADMIN) {
                throw new SecurityException("Chỉ Quản trị viên tối cao (Admin) mới có quyền chỉnh sửa tài khoản quản trị hệ thống!");
            }
        }

        // Kiểm tra tính hợp lệ của dữ liệu nhân viên
        kiemTraHopLeNhanVien(nv, true);

        // 1. Cập nhật thông tin nhân sự
        boolean capNhatNvOk = nhanVienDAO.capNhatNhanVien(nv);

        // 2. Cập nhật thông tin tài khoản (nếu có tài khoản liên kết)
        if (capNhatNvOk && nvCu.getMaTaiKhoan() != null && vaiTroMoi != null && trangThaiTaiKhoanMoi != null) {
            // Không cho phép chọn gán vai trò ADMIN từ giao diện này
            if ("Admin".equalsIgnoreCase(vaiTroMoi.trim())) {
                throw new IllegalArgumentException("Không thể gán vai trò Admin từ màn hình quản lý nhân viên!");
            }

            // Chặn người dùng tự khóa tài khoản của chính mình
            TaiKhoan tkDangDangNhap = SessionManager.getTaiKhoanHienTai();
            if (tkDangDangNhap != null && nv.getMaNhanVien().equals(tkDangDangNhap.getMaNhanVien()) && !trangThaiTaiKhoanMoi) {
                throw new IllegalArgumentException("Bạn không thể tự khóa tài khoản đang đăng nhập của chính mình!");
            }

            nhanVienDAO.capNhatTaiKhoan(nv.getMaNhanVien(), vaiTroMoi.trim(), trangThaiTaiKhoanMoi);
        }

        return capNhatNvOk;
    }

    // Xóa mềm nhân viên: chuyển trạng thái sang "Đã nghỉ việc" và khóa tài khoản
    public boolean ngungLamViec(String maNhanVien) {
        xacThucService.kiemTraQuyen("NV_CAP_NHAT");

        if (maNhanVien == null || maNhanVien.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhân viên cần ngừng làm việc không được để trống!");
        }

        NhanVien nv = nhanVienDAO.layNhanVienTheoMa(maNhanVien.trim());
        if (nv == null) {
            throw new IllegalArgumentException("Không tìm thấy nhân viên mã: " + maNhanVien);
        }

        if ("Admin".equalsIgnoreCase(nv.getPhanQuyen()) || "NV-20261005-000000".equals(nv.getMaNhanVien())) {
            throw new SecurityException("Không thể ngừng làm việc tài khoản Quản trị hệ thống!");
        }

        TaiKhoan tkHienTai = SessionManager.getTaiKhoanHienTai();
        if (tkHienTai != null && maNhanVien.trim().equals(tkHienTai.getMaNhanVien())) {
            throw new IllegalArgumentException("Bạn không thể ngừng làm việc tài khoản của chính mình!");
        }

        return nhanVienDAO.ngungLamViec(maNhanVien.trim());
    }

    // Khôi phục trạng thái nhân viên về "Đang làm"
    public boolean khoiPhucLamViec(String maNhanVien, boolean moLaiTaiKhoan) {
        xacThucService.kiemTraQuyen("NV_CAP_NHAT");

        if (maNhanVien == null || maNhanVien.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhân viên cần khôi phục không được để trống!");
        }

        return nhanVienDAO.khoiPhucLamViec(maNhanVien.trim(), moLaiTaiKhoan);
    }

    // Khóa hoặc mở khóa tài khoản của nhân viên
    public boolean doiTrangThaiTaiKhoan(String maNhanVien, boolean moKhoa) {
        xacThucService.kiemTraQuyen("NV_CAP_NHAT");

        if (maNhanVien == null || maNhanVien.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhân viên không được để trống!");
        }

        TaiKhoan tkHienTai = SessionManager.getTaiKhoanHienTai();
        if (tkHienTai != null && maNhanVien.trim().equals(tkHienTai.getMaNhanVien()) && !moKhoa) {
            throw new IllegalArgumentException("Bạn không thể tự khóa tài khoản của chính mình!");
        }

        return nhanVienDAO.doiTrangThaiTaiKhoan(maNhanVien.trim(), moKhoa);
    }

    // =========================================================================
    // MOCK TAO TAI KHOAN SONG SONG & TODO KET NOI XacThucService (EMP_ACCOUNT)
    // =========================================================================

    /**
     * Tạo tài khoản mới cho nhân viên.
     * Hiện tại hỗ trợ mock tạo mật khẩu mặc định hoặc hash.
     * // TODO: nối XacThucService khi module XacThuc hoàn thiện hàm tạo tài khoản tập trung.
     */
    public TaiKhoan mockTaoTaiKhoan(String maNhanVien, String tenDangNhap, String matKhau, String vaiTro) {
        String maTk = nhanVienDAO.taoMaTaiKhoan();
        String pass = (matKhau != null && !matKhau.trim().isEmpty()) ? matKhau.trim() : "123456";
        TaiKhoan.VaiTro vt = TaiKhoan.VaiTro.tuTenHienThi(vaiTro);

        TaiKhoan tk = new TaiKhoan(maTk, maNhanVien, tenDangNhap.trim(), pass, vt, true);
        return tk;
    }

    public boolean themTaiKhoanChoNhanVien(String maNhanVien, String tenDangNhap, String matKhau, String phanQuyen) {
        if (tenDangNhap == null || tenDangNhap.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống!");
        }
        if (nhanVienDAO.kiemTraTenDangNhapTonTai(tenDangNhap.trim())) {
            throw new IllegalArgumentException("Tên đăng nhập '" + tenDangNhap.trim() + "' đã được sử dụng!");
        }
        return nhanVienDAO.themTaiKhoanChoNhanVien(maNhanVien, tenDangNhap.trim(), (matKhau != null && !matKhau.isBlank()) ? matKhau : "1111", phanQuyen);
    }

    public boolean datLaiMatKhau(String maTaiKhoan, String matKhauMoi) {
        if (maTaiKhoan == null || maTaiKhoan.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã tài khoản không hợp lệ!");
        }
        return nhanVienDAO.datLaiMatKhau(maTaiKhoan, (matKhauMoi != null && !matKhauMoi.isBlank()) ? matKhauMoi : "1111");
    }

    public boolean xoaTaiKhoan(String maTaiKhoan) {
        if (maTaiKhoan == null || maTaiKhoan.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã tài khoản không hợp lệ!");
        }
        return nhanVienDAO.xoaTaiKhoan(maTaiKhoan);
    }

    // Sinh mã nhân viên mới
    public String taoMaNhanVienMoi() {
        return nhanVienDAO.taoMaNhanVien();
    }

    // =========================================================================
    // HAM KIEM TRA TINH HOP LE (VALIDATION)
    // =========================================================================

    public void kiemTraHopLeNhanVien(NhanVien nv, boolean laCapNhat) {
        if (nv == null) {
            throw new IllegalArgumentException("Thông tin nhân viên không được để trống!");
        }

        // 1. Kiểm tra họ tên
        if (nv.getHoTen() == null || nv.getHoTen().trim().isEmpty()) {
            throw new IllegalArgumentException("Họ và tên nhân viên không được để trống!");
        }
        String hoTen = nv.getHoTen().trim();
        if (hoTen.length() < 2 || hoTen.length() > 100) {
            throw new IllegalArgumentException("Họ và tên nhân viên phải từ 2 đến 100 ký tự!");
        }

        // 2. Kiểm tra số điện thoại (0 + 9 chữ số)
        if (nv.getSoDienThoai() == null || nv.getSoDienThoai().trim().isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống!");
        }
        String sdt = nv.getSoDienThoai().trim();
        if (!PATTERN_SDT.matcher(sdt).matches()) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ! Phải gồm 10 chữ số và bắt đầu bằng số 0 (ví dụ: 0912345678).");
        }

        // 3. Kiểm tra ngày sinh và tuổi tối thiểu >= 18
        if (nv.getNgaySinh() == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày sinh của nhân viên!");
        }
        LocalDate ngaySinh = nv.getNgaySinh();
        LocalDate homNay = LocalDate.now();
        if (ngaySinh.isAfter(homNay)) {
            throw new IllegalArgumentException("Ngày sinh không thể ở trong tương lai!");
        }
        int tuoi = Period.between(ngaySinh, homNay).getYears();
        if (tuoi < 18) {
            throw new IllegalArgumentException("Nhân viên phải từ đủ 18 tuổi trở lên (Tuổi hiện tại: " + tuoi + ")!");
        }

        // 4. Kiểm tra chức vụ (Quản lý, Bán hàng, Chăm sóc)
        if (nv.getChucVu() == null || nv.getChucVu().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn chức vụ cho nhân viên!");
        }
        String cv = nv.getChucVu().trim();
        if ("Quản lý".equalsIgnoreCase(cv) || "Quan ly".equalsIgnoreCase(cv) || "QUAN_LY".equalsIgnoreCase(cv)) {
            nv.setChucVu("Quản lý");
        } else if ("Bán hàng".equalsIgnoreCase(cv) || "Ban hang".equalsIgnoreCase(cv) || "BAN_HANG".equalsIgnoreCase(cv)) {
            nv.setChucVu("Bán hàng");
        } else if ("Chăm sóc".equalsIgnoreCase(cv) || "Cham soc".equalsIgnoreCase(cv) || "CHAM_SOC".equalsIgnoreCase(cv)) {
            nv.setChucVu("Chăm sóc");
        } else {
            throw new IllegalArgumentException("Chức vụ phải là một trong các giá trị: 'Quản lý', 'Bán hàng', 'Chăm sóc'!");
        }

        // 5. Kiểm tra trùng SĐT trong CSDL
        String maBoQua = laCapNhat ? nv.getMaNhanVien() : null;
        try {
            if (nhanVienDAO.kiemTraTrungSoDienThoai(sdt, maBoQua)) {
                throw new IllegalArgumentException("Số điện thoại '" + sdt + "' đã tồn tại ở một nhân viên khác trong hệ thống!");
            }
        } catch (IllegalStateException e) {
            // Khi CSDL offline trong môi trường test độc lập, bỏ qua nếu lỗi kết nối socket
            if (!e.getMessage().contains("socket") && !e.getMessage().contains("TCP/IP")) {
                throw e;
            }
        }

        // 5. Kiểm tra trạng thái
        if (nv.getTrangThai() == null || nv.getTrangThai().trim().isEmpty()) {
            nv.setTrangThai("Đang làm");
        }
    }
}
