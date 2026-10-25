package com.petshop.model;

/**
 * Lớp thực thể Tài khoản hệ thống (khớp bảng TaiKhoan trong CSDL và Class Diagram).
 */
public class TaiKhoan {

    /**
     * Enum vai trò phân quyền người dùng trong hệ thống Happy Tails.
     */
    public enum VaiTro {
        ADMIN("Admin"),
        QUAN_LY("Quản lý"),
        BAN_HANG("Bán hàng"),
        CHAM_SOC("Chăm sóc");

        private final String tenHienThi;

        VaiTro(String tenHienThi) {
            this.tenHienThi = tenHienThi;
        }

        public String getTenHienThi() {
            return tenHienThi;
        }

        public static VaiTro tuTenHienThi(String giaTri) {
            String chuanHoa = giaTri == null ? "" : giaTri.trim();
            for (VaiTro vt : values()) {
                if (vt.tenHienThi.equalsIgnoreCase(chuanHoa) || vt.name().equalsIgnoreCase(chuanHoa)) {
                    return vt;
                }
            }
            throw new IllegalArgumentException("Vai trò không hợp lệ: " + giaTri);
        }
    }

    private String maTaiKhoan;
    private String maNhanVien;
    private String tenDangNhap;
    private String matKhau;
    private VaiTro phanQuyen;
    private boolean trangThai; // true: hoạt động, false: bị khóa
    private String hoTenNhanVien; // Tên nhân viên hiển thị trên giao diện

    public TaiKhoan() {
    }

    public TaiKhoan(String maTaiKhoan, String maNhanVien, String tenDangNhap, String matKhau, VaiTro phanQuyen, boolean trangThai) {
        this.maTaiKhoan = maTaiKhoan;
        this.maNhanVien = maNhanVien;
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.phanQuyen = phanQuyen;
        this.trangThai = trangThai;
    }

    public TaiKhoan(String maNhanVien, String tenDangNhap, String hoTenNhanVien, VaiTro phanQuyen) {
        this.maNhanVien = maNhanVien;
        this.tenDangNhap = tenDangNhap;
        this.hoTenNhanVien = hoTenNhanVien;
        this.phanQuyen = phanQuyen;
        this.trangThai = true;
    }

    public String getMaTaiKhoan() {
        return maTaiKhoan;
    }

    public void setMaTaiKhoan(String maTaiKhoan) {
        this.maTaiKhoan = maTaiKhoan;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public VaiTro getPhanQuyen() {
        return phanQuyen;
    }

    public void setPhanQuyen(VaiTro phanQuyen) {
        this.phanQuyen = phanQuyen;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }

    public String getHoTenNhanVien() {
        return hoTenNhanVien;
    }

    public void setHoTenNhanVien(String hoTenNhanVien) {
        this.hoTenNhanVien = hoTenNhanVien;
    }

    // Các phương thức nghiệp vụ khớp với Class Diagram
    public void doiMatKhau(String matKhauMoi) {
        this.matKhau = matKhauMoi;
    }

    public void khoaTaiKhoan() {
        this.trangThai = false;
    }

    public void moKhoaTaiKhoan() {
        this.trangThai = true;
    }

    public void capNhatQuyen(VaiTro quyenMoi) {
        this.phanQuyen = quyenMoi;
    }

    public void resetMatKhau(String matKhauMacDinh) {
        this.matKhau = matKhauMacDinh;
    }
}

