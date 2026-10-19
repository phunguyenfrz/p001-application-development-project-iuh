package com.petshop.model;

import java.time.LocalDate;

/**
 * Hồ sơ nhân viên cùng thông tin tài khoản chỉ đọc.
 *
 * Dùng record của Java 17 để tự tạo constructor và các hàm đọc.
 * Ví dụ: employee.hoTen(), employee.maNhanVien().
 *
 * @param maNhanVien Mã nhân viên, định dạng NV-YYYYMMDD-NNNNNN.
 * @param hoTen Họ tên nhân viên.
 * @param soDienThoai Số điện thoại liên hệ.
 * @param email Email liên hệ.
 * @param diaChi Địa chỉ.
 * @param chucVu Chức vụ: Quản lý, Bán hàng hoặc Chăm sóc.
 * @param ngaySinh Ngày sinh.
 * @param nam true là Nam, false là Nữ.
 * @param ngayBatDau Ngày bắt đầu làm việc.
 * @param trangThai Đang làm hoặc Đã nghỉ việc.
 * @param anh Dữ liệu ảnh PNG/JPEG.
 * @param tenDangNhap Tên tài khoản, null nếu chưa được cấp.
 * @param phanQuyen Quyền đăng nhập lấy từ bảng TaiKhoan.
 * @param taiKhoanHoatDong Trạng thái tài khoản, null nếu chưa có.
 */
public record Employee(
        String maNhanVien,
        String hoTen,
        String soDienThoai,
        String email,
        String diaChi,
        String chucVu,
        LocalDate ngaySinh,
        boolean nam,
        LocalDate ngayBatDau,
        String trangThai,
        byte[] anh,
        String tenDangNhap,
        String phanQuyen,
        Boolean taiKhoanHoatDong
) {
    public String getMaNhanVien() { return maNhanVien; }
    public String getHoTen() { return hoTen; }
    public String getSoDienThoai() { return soDienThoai; }
    public String getEmail() { return email; }
    public String getDiaChi() { return diaChi; }
    public String getChucVu() { return chucVu; }
    public LocalDate getNgaySinh() { return ngaySinh; }
    public boolean isNam() { return nam; }
    public LocalDate getNgayBatDau() { return ngayBatDau; }
    public String getTrangThai() { return trangThai; }
    public byte[] getAnh() { return anh; }
    public String getTenDangNhap() { return tenDangNhap; }
    public String getPhanQuyen() { return phanQuyen; }
    public Boolean getTaiKhoanHoatDong() { return taiKhoanHoatDong; }
}