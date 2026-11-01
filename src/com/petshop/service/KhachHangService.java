package com.petshop.service;

import com.petshop.dao.KhachHangDAO;
import com.petshop.model.ChiTietGiaoDich;
import com.petshop.model.KhachHang;
import com.petshop.model.LichSuGiaoDich;

import java.time.LocalDate;
import java.util.List;

/**
 * Lớp dịch vụ (Service Layer) xử lý logic nghiệp vụ cho phân hệ Khách hàng:
 * Kiểm tra tính hợp lệ của dữ liệu (validation), chặn ký tự đặc biệt trong họ tên,
 * kiểm tra định dạng và trùng lặp số điện thoại, tra cứu lịch sử giao dịch và chi tiết hóa đơn.
 * Tuân thủ Quy ước #60 về phân quyền bảo mật nghiệp vụ.
 */
public class KhachHangService {

    /** Biểu thức chính quy kiểm tra họ và tên: Cho phép chữ cái (kể cả tiếng Việt), chữ số và khoảng trắng; chặn ký tự đặc biệt (!@#$%^&*...) */
    public static final String REGEX_HO_TEN = "^[\\p{L}0-9\\s]+$";

    /** Biểu thức chính quy kiểm tra số điện thoại: Bắt đầu bằng 0 và tiếp theo là 9 chữ số */
    public static final String REGEX_SO_DIEN_THOAI = "^0\\d{9}$";

    /** Biểu thức chính quy kiểm tra định dạng email */
    public static final String REGEX_EMAIL = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    private final KhachHangDAO khachHangDAO;
    private final XacThucService xacThucService;

    public KhachHangService() {
        this.khachHangDAO = new KhachHangDAO();
        this.xacThucService = new XacThucService();
    }

    public KhachHangService(KhachHangDAO khachHangDAO, XacThucService xacThucService) {
        this.khachHangDAO = khachHangDAO;
        this.xacThucService = xacThucService;
    }

    /**
     * Lấy toàn bộ danh sách khách hàng.
     */
    public List<KhachHang> layDanhSachKhachHang() {
        kiemTraQuyen("KH_CAP_NHAT");
        return khachHangDAO.layDanhSachKhachHang();
    }

    /**
     * Lấy danh sách khách hàng đang hoạt động.
     */
    public List<KhachHang> layDanhSachKhachHangDangHoatDong() {
        kiemTraQuyen("KH_CAP_NHAT");
        return khachHangDAO.layDanhSachKhachHangDangHoatDong();
    }

    /**
     * Tra cứu khách hàng theo mã.
     */
    public KhachHang layKhachHangTheoMa(String maKhachHang) {
        kiemTraQuyen("KH_CAP_NHAT");
        return khachHangDAO.timTheoMa(maKhachHang);
    }

    /**
     * Tra cứu khách hàng theo số điện thoại.
     */
    public KhachHang layKhachHangTheoSoDienThoai(String soDienThoai) {
        kiemTraQuyen("KH_CAP_NHAT");
        return khachHangDAO.timTheoSoDienThoai(soDienThoai);
    }

    /**
     * Tìm kiếm và lọc danh sách khách hàng linh hoạt theo từ khóa và trạng thái hoạt động.
     */
    public List<KhachHang> timKiemKhachHang(String tuKhoa, String locTrangThai) {
        kiemTraQuyen("KH_TIM_KIEM");
        return khachHangDAO.timKiemKhachHang(tuKhoa, locTrangThai);
    }

    /**
     * Lấy lịch sử giao dịch hóa đơn của khách hàng theo bộ lọc thời gian và trạng thái.
     * // TODO: nối HoaDonService khi phân hệ Hóa đơn merge theo Quy ước #60 (Mục 7.3)
     */
    public List<LichSuGiaoDich> layLichSuGiaoDich(String maKhachHang, LocalDate tuNgay, LocalDate denNgay, String trangThaiThanhToan) {
        kiemTraQuyen("KH_LICH_SU");
        return khachHangDAO.layLichSuGiaoDich(maKhachHang, tuNgay, denNgay, trangThaiThanhToan);
    }

    /**
     * Lấy danh sách sản phẩm trong một hóa đơn của khách hàng.
     * // TODO: nối HoaDonService khi phân hệ Hóa đơn merge theo Quy ước #60 (Mục 7.3)
     */
    public List<ChiTietGiaoDich> laySanPhamHoaDon(String maKhachHang, String maHoaDon) {
        kiemTraQuyen("KH_LICH_SU");
        return khachHangDAO.laySanPhamHoaDon(maKhachHang, maHoaDon);
    }

    /**
     * Lấy danh sách dịch vụ trong một hóa đơn của khách hàng.
     * // TODO: nối HoaDonService khi phân hệ Hóa đơn merge theo Quy ước #60 (Mục 7.3)
     */
    public List<ChiTietGiaoDich> layDichVuHoaDon(String maKhachHang, String maHoaDon) {
        kiemTraQuyen("KH_LICH_SU");
        return khachHangDAO.layDichVuHoaDon(maKhachHang, maHoaDon);
    }

    /**
     * Thêm mới khách hàng vào hệ thống với đầy đủ kiểm tra ràng buộc nghiệp vụ.
     */
    public boolean themKhachHang(KhachHang khachHang) {
        kiemTraQuyen("KH_CAP_NHAT");
        kiemTraDuLieuKhachHang(khachHang, false);

        if (khachHang.getMaKhachHang() == null || khachHang.getMaKhachHang().trim().isEmpty()) {
            khachHang.setMaKhachHang(khachHangDAO.taoMaKhachHang());
        }

        return khachHangDAO.themKhachHang(khachHang);
    }

    /**
     * Cập nhật thông tin khách hàng hiện có.
     */
    public boolean capNhatKhachHang(KhachHang khachHang) {
        kiemTraQuyen("KH_CAP_NHAT");

        if (khachHang == null || khachHang.getMaKhachHang() == null || khachHang.getMaKhachHang().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khách hàng cần cập nhật không được để trống!");
        }

        kiemTraDuLieuKhachHang(khachHang, true);
        return khachHangDAO.capNhatKhachHang(khachHang);
    }

    /**
     * Xóa mềm khách hàng (chuyển trạng thái sang ngừng hoạt động: trangThai = 0).
     */
    public boolean xoaMemKhachHang(String maKhachHang) {
        kiemTraQuyen("KH_CAP_NHAT");

        if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn khách hàng cần ngưng hoạt động!");
        }

        return khachHangDAO.xoaMemKhachHang(maKhachHang.trim());
    }

    /**
     * Kích hoạt lại khách hàng đã ngừng hoạt động (trangThai = 1).
     */
    public boolean kichHoatLaiKhachHang(String maKhachHang) {
        kiemTraQuyen("KH_CAP_NHAT");

        if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn khách hàng cần kích hoạt lại!");
        }

        return khachHangDAO.kichHoatLaiKhachHang(maKhachHang.trim());
    }

    /**
     * Tự động sinh mã khách hàng mới tiếp theo.
     */
    public boolean capNhatDiemTichLuy(String maKhachHang, int diemThayDoi) {
        if (maKhachHang == null || maKhachHang.trim().isEmpty()) return false;
        return khachHangDAO.capNhatDiemTichLuy(maKhachHang.trim(), diemThayDoi);
    }

    public String taoMaKhachHangMoi() {
        return khachHangDAO.taoMaKhachHang();
    }

    /**
     * Kiểm tra toàn diện tính hợp lệ của dữ liệu khách hàng trước khi lưu vào CSDL.
     */
    public void kiemTraDuLieuKhachHang(KhachHang khachHang, boolean laCapNhat) {
        if (khachHang == null) {
            throw new IllegalArgumentException("Dữ liệu khách hàng không được để trống!");
        }

        // 1. Kiểm tra họ tên: Bắt buộc, không rỗng, không chứa ký tự đặc biệt (!@#$%^&*...)
        if (khachHang.getHoTen() == null || khachHang.getHoTen().trim().isEmpty()) {
            throw new IllegalArgumentException("Họ và tên khách hàng không được để trống!");
        }
        String hoTen = khachHang.getHoTen().trim();
        if (!hoTen.matches(REGEX_HO_TEN)) {
            throw new IllegalArgumentException("Họ và tên khách hàng không được chứa ký tự đặc biệt (!@#$%^&*...)!");
        }
        khachHang.setHoTen(hoTen);

        // 2. Kiểm tra số điện thoại: Đúng định dạng 10 số, bắt đầu bằng 0
        if (khachHang.getSoDienThoai() == null || khachHang.getSoDienThoai().trim().isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống!");
        }
        String sdt = khachHang.getSoDienThoai().trim();
        if (!sdt.matches(REGEX_SO_DIEN_THOAI)) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ! Phải gồm đúng 10 chữ số và bắt đầu bằng số 0.");
        }
        khachHang.setSoDienThoai(sdt);

        // 3. Kiểm tra trùng số điện thoại với khách hàng khác trong hệ thống
        boolean trungSDT = khachHangDAO.kiemTraTrungSoDienThoai(sdt, laCapNhat ? khachHang.getMaKhachHang() : null);
        if (trungSDT) {
            throw new IllegalArgumentException("Số điện thoại '" + sdt + "' đã tồn tại cho một khách hàng khác trong hệ thống!");
        }

        // 4. Kiểm tra email (nếu có nhập thì phải đúng cấu trúc)
        if (khachHang.getEmail() != null && !khachHang.getEmail().trim().isEmpty()) {
            String email = khachHang.getEmail().trim();
            if (!email.matches(REGEX_EMAIL)) {
                throw new IllegalArgumentException("Định dạng email không hợp lệ (Ví dụ hợp lệ: ten@gmail.com)!");
            }
            khachHang.setEmail(email);
        } else {
            khachHang.setEmail(null);
        }

        // 5. Chuẩn hóa địa chỉ và ghi chú
        if (khachHang.getDiaChi() != null) {
            khachHang.setDiaChi(khachHang.getDiaChi().trim());
        }
        if (khachHang.getGhiChu() != null) {
            khachHang.setGhiChu(khachHang.getGhiChu().trim());
        }

        // 6. Điểm tích lũy không âm
        if (khachHang.getDiemTichLuy() < 0) {
            khachHang.setDiemTichLuy(0);
        }
    }

    /**
     * Kiểm tra quyền hạn trực tiếp qua XacThucService (sẽ ném SecurityException nếu chưa đăng nhập hoặc không có quyền).
     */
    private void kiemTraQuyen(String maChucNang) {
        xacThucService.kiemTraQuyen(maChucNang);
    }
}
