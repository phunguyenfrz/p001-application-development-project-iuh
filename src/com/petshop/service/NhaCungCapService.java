package com.petshop.service;

import com.petshop.dao.NhaCungCapDAO;
import com.petshop.model.NhaCungCap;
import com.petshop.model.TaiKhoan;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Xử lý nghiệp vụ, kiểm tra hợp lệ dữ liệu và phân quyền cho Nhà cung cấp.
 * Tuân thủ Quy ước #60 và kiến trúc phân lớp chuẩn của hệ thống.
 */
public class NhaCungCapService {

    public static final String REGEX_SDT = "^0\\d{9}$";
    public static final String REGEX_EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    public static final String REGEX_MA_NCC = "^NCC-\\d{8}-\\d{6}$";

    private static final Pattern PATTERN_SDT = Pattern.compile(REGEX_SDT);
    private static final Pattern PATTERN_EMAIL = Pattern.compile(REGEX_EMAIL);

    private final NhaCungCapDAO nhaCungCapDAO;
    private final XacThucService xacThucService;

    public NhaCungCapService() {
        this.nhaCungCapDAO = new NhaCungCapDAO();
        this.xacThucService = new XacThucService();
    }

    public NhaCungCapService(NhaCungCapDAO nhaCungCapDAO, XacThucService xacThucService) {
        this.nhaCungCapDAO = nhaCungCapDAO;
        this.xacThucService = xacThucService;
    }

    /**
     * Thêm nhà cung cấp mới.
     * Kiểm tra quyền NCC_CAP_NHAT, validate dữ liệu, sinh mã nếu chưa có và kiểm tra trùng SĐT.
     */
    public boolean themNhaCungCap(NhaCungCap nhaCungCap) {
        xacThucService.kiemTraQuyen("NCC_CAP_NHAT");
        kiemTraDuLieuNhaCungCap(nhaCungCap, false);

        if (nhaCungCap.getMaNhaCungCap() == null || nhaCungCap.getMaNhaCungCap().trim().isEmpty()) {
            nhaCungCap.setMaNhaCungCap(nhaCungCapDAO.taoMaNhaCungCap());
        } else {
            String ma = nhaCungCap.getMaNhaCungCap().trim();
            if (!ma.matches(REGEX_MA_NCC)) {
                throw new IllegalArgumentException("Mã nhà cung cấp phải đúng định dạng NCC-yyyyMMdd-xxxxxx!");
            }
            nhaCungCap.setMaNhaCungCap(ma);
        }

        return nhaCungCapDAO.themNhaCungCap(nhaCungCap);
    }

    /**
     * Cập nhật thông tin nhà cung cấp.
     * Kiểm tra quyền NCC_CAP_NHAT, mã tồn tại, validate dữ liệu và trùng SĐT loại trừ mã đang cập nhật.
     */
    public boolean capNhatNhaCungCap(NhaCungCap nhaCungCap) {
        xacThucService.kiemTraQuyen("NCC_CAP_NHAT");
        if (nhaCungCap == null || nhaCungCap.getMaNhaCungCap() == null || nhaCungCap.getMaNhaCungCap().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhà cung cấp cần cập nhật không được để trống!");
        }

        kiemTraDuLieuNhaCungCap(nhaCungCap, true);
        nhaCungCap.setMaNhaCungCap(nhaCungCap.getMaNhaCungCap().trim());

        return nhaCungCapDAO.capNhatNhaCungCap(nhaCungCap);
    }

    /** Xóa vật lý nhà cung cấp chưa có sản phẩm liên kết hoặc đơn nhập. */
    public boolean xoaNhaCungCap(String maNhaCungCap) {
        xacThucService.kiemTraQuyen("NCC_CAP_NHAT");
        if (maNhaCungCap == null || maNhaCungCap.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn nhà cung cấp cần xóa!");
        }

        return nhaCungCapDAO.xoaNhaCungCap(maNhaCungCap.trim());
    }

    /** Ngừng hợp tác hoặc hợp tác lại; không tự thay đổi trạng thái sản phẩm. */
    public boolean capNhatTrangThaiNhaCungCap(String maNhaCungCap, boolean trangThai) {
        xacThucService.kiemTraQuyen("NCC_CAP_NHAT");
        if (maNhaCungCap == null || maNhaCungCap.isBlank()) {
            throw new IllegalArgumentException("Mã nhà cung cấp không được để trống!");
        }
        return nhaCungCapDAO.capNhatTrangThaiNhaCungCap(maNhaCungCap.trim(), trangThai);
    }

    /**
     * Lấy danh sách nhà cung cấp đang hoạt động cho phân hệ Quản lý Nhà cung cấp.
     * Kiểm tra quyền NCC_TIM_KIEM.
     */
    public List<NhaCungCap> layDanhSachNhaCungCap() {
        xacThucService.kiemTraQuyen("NCC_TIM_KIEM");
        return nhaCungCapDAO.layDanhSachNhaCungCap();
    }

    /**
     * Lấy toàn bộ danh sách nhà cung cấp (cả hoạt động và ngừng hoạt động).
     * Kiểm tra quyền NCC_TIM_KIEM.
     */
    public List<NhaCungCap> layTatCaNhaCungCap() {
        xacThucService.kiemTraQuyen("NCC_TIM_KIEM");
        return nhaCungCapDAO.layTatCaNhaCungCap();
    }

    /**
     * Tra cứu nhà cung cấp theo mã.
     * Kiểm tra quyền NCC_TIM_KIEM.
     */
    public NhaCungCap layTheoMa(String maNhaCungCap) {
        xacThucService.kiemTraQuyen("NCC_TIM_KIEM");
        if (maNhaCungCap == null || maNhaCungCap.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhà cung cấp không được để trống!");
        }
        return nhaCungCapDAO.layTheoMa(maNhaCungCap.trim());
    }

    /**
     * Tìm kiếm nhà cung cấp theo từ khóa (mã, tên, SĐT hoặc email), chỉ lấy NCC đang hoạt động.
     * Kiểm tra quyền NCC_TIM_KIEM.
     */
    public List<NhaCungCap> timKiemNhaCungCap(String tuKhoa) {
        xacThucService.kiemTraQuyen("NCC_TIM_KIEM");
        return nhaCungCapDAO.timKiemNhaCungCap(tuKhoa);
    }

    // =========================================================================
    // CÁC HÀM RIÊNG CHO DIALOG CHỌN NHÀ CUNG CẤP (Hỗ trợ Lập đơn nhập #67)
    // Cho phép ADMIN, QUAN_LY, BAN_HANG sử dụng (không dùng mã quyền NCC_TIM_KIEM)
    // =========================================================================

    /**
     * Kiểm tra quyền sử dụng dialog chọn nhà cung cấp.
     * Cho phép các vai trò ADMIN, QUAN_LY, BAN_HANG.
     */
    private void kiemTraQuyenChonNhaCungCap() {
        TaiKhoan tk = xacThucService.getTaiKhoanHienTai();
        if (tk == null) {
            throw new SecurityException("Chưa đăng nhập hệ thống!");
        }
        TaiKhoan.VaiTro vaiTro = tk.getPhanQuyen();
        if (vaiTro != TaiKhoan.VaiTro.ADMIN && vaiTro != TaiKhoan.VaiTro.QUAN_LY && vaiTro != TaiKhoan.VaiTro.BAN_HANG) {
            throw new SecurityException("Tài khoản của bạn không có quyền chọn nhà cung cấp!");
        }
    }

    /**
     * Lấy danh sách nhà cung cấp đang hoạt động cho dialog lựa chọn (phục vụ lập đơn nhập #67).
     * Cho phép ADMIN, QUAN_LY, BAN_HANG.
     */
    public List<NhaCungCap> layDanhSachDangHoatDong() {
        kiemTraQuyenChonNhaCungCap();
        return nhaCungCapDAO.layDanhSachNhaCungCap();
    }

    /**
     * Tìm kiếm nhà cung cấp đang hoạt động theo từ khóa cho dialog lựa chọn (phục vụ lập đơn nhập #67).
     * Cho phép ADMIN, QUAN_LY, BAN_HANG.
     */
    public List<NhaCungCap> timKiemChoDialog(String tuKhoa) {
        kiemTraQuyenChonNhaCungCap();
        return nhaCungCapDAO.timKiemNhaCungCap(tuKhoa);
    }

    /**
     * Kiểm tra tính hợp lệ của dữ liệu nhà cung cấp trước khi thêm hoặc cập nhật.
     */
    private void kiemTraDuLieuNhaCungCap(NhaCungCap ncc, boolean laCapNhat) {
        if (ncc == null) {
            throw new IllegalArgumentException("Dữ liệu nhà cung cấp không hợp lệ!");
        }

        // 1. Kiểm tra và chuẩn hóa tên nhà cung cấp
        String ten = ncc.getTenNhaCungCap() == null ? "" : ncc.getTenNhaCungCap().trim();
        if (ten.isEmpty()) {
            throw new IllegalArgumentException("Tên nhà cung cấp không được để trống!");
        }
        if (ten.length() > 150) {
            throw new IllegalArgumentException("Tên nhà cung cấp không được vượt quá 150 ký tự!");
        }
        ncc.setTenNhaCungCap(ten);

        // 2. Kiểm tra và chuẩn hóa số điện thoại
        String sdt = ncc.getSoDienThoai() == null ? "" : ncc.getSoDienThoai().trim();
        if (sdt.isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống!");
        }
        if (!PATTERN_SDT.matcher(sdt).matches()) {
            throw new IllegalArgumentException("Số điện thoại phải gồm đúng 10 chữ số và bắt đầu bằng số 0 (ví dụ: 0912345678)!");
        }
        ncc.setSoDienThoai(sdt);

        // Kiểm tra trùng SĐT
        String maLoaiTru = laCapNhat ? ncc.getMaNhaCungCap() : null;
        if (nhaCungCapDAO.kiemTraTrungSoDienThoai(sdt, maLoaiTru)) {
            throw new IllegalArgumentException("Số điện thoại '" + sdt + "' đã tồn tại ở nhà cung cấp khác!");
        }

        // 3. Kiểm tra và chuẩn hóa email (cho phép để trống)
        String email = ncc.getEmail() == null ? null : ncc.getEmail().trim();
        if (email != null && !email.isEmpty()) {
            if (email.length() > 100) {
                throw new IllegalArgumentException("Email không được vượt quá 100 ký tự!");
            }
            if (!PATTERN_EMAIL.matcher(email).matches()) {
                throw new IllegalArgumentException("Địa chỉ email không đúng định dạng hợp lệ!");
            }
            ncc.setEmail(email);
        } else {
            ncc.setEmail(null);
        }

        // 4. Chuẩn hóa địa chỉ (cho phép để trống)
        String diaChi = ncc.getDiaChi() == null ? null : ncc.getDiaChi().trim();
        if (diaChi != null && !diaChi.isEmpty()) {
            if (diaChi.length() > 255) {
                throw new IllegalArgumentException("Địa chỉ không được vượt quá 255 ký tự!");
            }
            ncc.setDiaChi(diaChi);
        } else {
            ncc.setDiaChi(null);
        }
    }
}
