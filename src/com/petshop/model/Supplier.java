package com.petshop.model;

/**
 * Lớp thực thể đại diện cho đối tượng Nhà cung cấp (bảng NhaCungCap - mục 2.14 OOAD).
 * Hỗ trợ quản lý thông tin liên hệ và trạng thái hoạt động (xóa mềm).
 */
public class Supplier {

    private String maNhaCungCap;    // Định dạng: NCC-YYYYMMDD-NNNNNN
    private String tenNhaCungCap;
    private String soDienThoai;     // 10 chữ số bắt đầu bằng 0
    private String email;           // Địa chỉ email liên hệ
    private String diaChi;
    private boolean trangThai = true; // true (1): Đang hợp tác, false (0): Ngừng hoạt động (xóa mềm)

    /**
     * Khởi tạo nhà cung cấp mặc định.
     */
    public Supplier() {
    }

    /**
     * Khởi tạo nhà cung cấp với đầy đủ thông tin.
     */
    public Supplier(String maNhaCungCap, String tenNhaCungCap, String soDienThoai,
                    String email, String diaChi, boolean trangThai) {
        this.maNhaCungCap = maNhaCungCap;
        this.tenNhaCungCap = tenNhaCungCap;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.diaChi = diaChi;
        this.trangThai = trangThai;
    }

    /**
     * Khởi tạo nhà cung cấp (tương thích với 4 cột cơ bản).
     */
    public Supplier(String maNhaCungCap, String tenNhaCungCap, String soDienThoai, String diaChi) {
        this(maNhaCungCap, tenNhaCungCap, soDienThoai, "", diaChi, true);
    }

    public String getMaNhaCungCap() {
        return maNhaCungCap;
    }

    public void setMaNhaCungCap(String maNhaCungCap) {
        this.maNhaCungCap = maNhaCungCap;
    }

    public String getTenNhaCungCap() {
        return tenNhaCungCap;
    }

    public void setTenNhaCungCap(String tenNhaCungCap) {
        this.tenNhaCungCap = tenNhaCungCap;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }

    /**
     * Lấy chuỗi hiển thị trạng thái tiếng Việt.
     */
    public String getTenTrangThai() {
        return trangThai ? "Đang hợp tác" : "Ngừng hoạt động";
    }

    @Override
    public String toString() {
        return maNhaCungCap + " - " + tenNhaCungCap;
    }
}

