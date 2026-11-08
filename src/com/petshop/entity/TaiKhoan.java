package com.petshop.entity;

/**
 * Lớp thực thể Tài khoản hệ thống (khớp bảng TaiKhoan trong CSDL và Class Diagram).
 * Tuân thủ Điều 2 & Điều 5 IUH: Khóa ngoại dùng Object Reference (NhanVien nhanVien),
 * tự động lấy nhân viên qua SessionManager.
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
    private NhanVien nhanVien; // Object Reference nhân viên sở hữu tài khoản (Điều 2 & 5 IUH)
    private String tenDangNhap;
    private String matKhau;
    private VaiTro phanQuyen;
    private boolean trangThai; // true: hoạt động, false: bị khóa

    public TaiKhoan() {
    }

    public TaiKhoan(String maTaiKhoan, String maNhanVien, String tenDangNhap, String matKhau, VaiTro phanQuyen, boolean trangThai) {
        this.maTaiKhoan = maTaiKhoan;
        setMaNhanVien(maNhanVien);
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.phanQuyen = phanQuyen;
        this.trangThai = trangThai;
    }

    public TaiKhoan(String maNhanVien, String tenDangNhap, String hoTenNhanVien, VaiTro phanQuyen) {
        setMaNhanVien(maNhanVien);
        this.tenDangNhap = tenDangNhap;
        setHoTenNhanVien(hoTenNhanVien);
        this.phanQuyen = phanQuyen;
        this.trangThai = true;
    }

    public TaiKhoan(NhanVien nhanVien, String tenDangNhap, String matKhau, VaiTro phanQuyen) {
        this.setNhanVien(nhanVien);
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.phanQuyen = phanQuyen;
        this.trangThai = true;
    }

    public String getMaTaiKhoan() {
        return maTaiKhoan;
    }

    public void setMaTaiKhoan(String maTaiKhoan) {
        this.maTaiKhoan = maTaiKhoan;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public String getMaNhanVien() {
        return nhanVien != null ? nhanVien.getMaNhanVien() : null;
    }

    public void setMaNhanVien(String maNhanVien) {
        if (maNhanVien == null) {
            if (this.nhanVien != null) {
                this.nhanVien.setMaNhanVien(null);
            }
            return;
        }
        if (this.nhanVien == null) {
            this.nhanVien = new NhanVien();
        }
        this.nhanVien.setMaNhanVien(maNhanVien);
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
        return nhanVien != null ? nhanVien.getHoTen() : null;
    }

    public void setHoTenNhanVien(String hoTenNhanVien) {
        if (hoTenNhanVien == null) {
            if (this.nhanVien != null) {
                this.nhanVien.setHoTen(null);
            }
            return;
        }
        if (this.nhanVien == null) {
            this.nhanVien = new NhanVien();
        }
        this.nhanVien.setHoTen(hoTenNhanVien);
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
