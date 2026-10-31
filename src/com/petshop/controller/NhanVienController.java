package com.petshop.controller;

import com.petshop.model.NhanVien;
import com.petshop.service.NhanVienService;

import java.util.List;

/**
 * Điều phối các thao tác xử lý nghiệp vụ Nhân viên từ giao diện xuống tầng NhanVienService.
 */
public class NhanVienController {

    private final NhanVienService nhanVienService;

    public NhanVienController() {
        this.nhanVienService = new NhanVienService();
    }

    public NhanVienController(NhanVienService nhanVienService) {
        this.nhanVienService = nhanVienService;
    }

    // Lấy toàn bộ danh sách nhân viên
    public List<NhanVien> layDanhSachNhanVien() {
        return nhanVienService.layDanhSachNhanVien();
    }

    // Tìm kiếm và lọc danh sách nhân viên
    public List<NhanVien> timKiemNhanVien(String tuKhoa, String chucVu, String trangThai) {
        return nhanVienService.timKiemNhanVien(tuKhoa, chucVu, trangThai);
    }

    // Lấy thông tin nhân viên theo mã
    public NhanVien layNhanVienTheoMa(String maNhanVien) {
        return nhanVienService.layNhanVienTheoMa(maNhanVien);
    }

    // Thêm nhân viên mới (hồ sơ nhân sự)
    public boolean themNhanVien(NhanVien nv) {
        return nhanVienService.themNhanVien(nv);
    }

    // Cập nhật thông tin nhân sự
    public boolean capNhatNhanVien(NhanVien nv) {
        return nhanVienService.capNhatNhanVien(nv);
    }

    // Cập nhật thông tin nhân sự và tài khoản liên kết (EMP_CRUD, EMP_ROLES, EMP_ACCOUNT)
    public boolean capNhatNhanVien(NhanVien nv, String vaiTroMoi, Boolean trangThaiTaiKhoanMoi) {
        return nhanVienService.capNhatNhanVien(nv, vaiTroMoi, trangThaiTaiKhoanMoi);
    }

    // Xóa mềm: ngừng làm việc nhân viên và tự động khóa tài khoản
    public boolean ngungLamViec(String maNhanVien) {
        return nhanVienService.ngungLamViec(maNhanVien);
    }

    // Khôi phục nhân viên về trạng thái đang làm
    public boolean khoiPhucLamViec(String maNhanVien, boolean moLaiTaiKhoan) {
        return nhanVienService.khoiPhucLamViec(maNhanVien, moLaiTaiKhoan);
    }

    // Khóa / Mở khóa tài khoản nhân viên
    public boolean doiTrangThaiTaiKhoan(String maNhanVien, boolean moKhoa) {
        return nhanVienService.doiTrangThaiTaiKhoan(maNhanVien, moKhoa);
    }

    // Sinh mã nhân viên mới
    public String taoMaNhanVienMoi() {
        return nhanVienService.taoMaNhanVienMoi();
    }

    // Cấp tài khoản mới cho nhân viên
    public boolean themTaiKhoanChoNhanVien(String maNhanVien, String tenDangNhap, String matKhau, String phanQuyen) {
        return nhanVienService.themTaiKhoanChoNhanVien(maNhanVien, tenDangNhap, matKhau, phanQuyen);
    }

    // Đặt lại mật khẩu tài khoản
    public boolean datLaiMatKhau(String maTaiKhoan, String matKhauMoi) {
        return nhanVienService.datLaiMatKhau(maTaiKhoan, matKhauMoi);
    }

    // Xóa tài khoản đăng nhập
    public boolean xoaTaiKhoan(String maTaiKhoan) {
        return nhanVienService.xoaTaiKhoan(maTaiKhoan);
    }
}

