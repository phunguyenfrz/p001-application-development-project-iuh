package com.petshop.model;

/**
 * Thực thể Khách hàng (Customer), ánh xạ bảng KhachHang trong cơ sở dữ liệu SQL Server.
 * Quản lý thông tin cá nhân, liên hệ, điểm tích lũy và trạng thái hoạt động của khách hàng.
 */
public class KhachHang {

    /** Mã định danh duy nhất của khách hàng theo định dạng: KH-yyyyMMdd-xxxxxx */
    private String maKhachHang;

    /** Họ và tên đầy đủ của khách hàng */
    private String hoTen;

    /** Số điện thoại liên hệ (10 chữ số, bắt đầu bằng 0) */
    private String soDienThoai;

    /** Địa chỉ email nhận thông báo / hóa đơn */
    private String email;

    /** Điểm thưởng tích lũy qua các lần mua sắm / sử dụng dịch vụ (>= 0) */
    private int diemTichLuy;

    /** Địa chỉ cư trú / giao hàng của khách */
    private String diaChi;

    /** Ghi chú đặc biệt về khách hàng (sở thích, thói quen, yêu cầu riêng) */
    private String ghiChu;

    /**
     * Trạng thái hoạt động (phục vụ kỹ thuật xóa mềm):
     * true: Đang hoạt động (bình thường)
     * false: Ngừng hoạt động (đã xóa mềm)
     */
    private boolean trangThai;

    /**
     * Hàm khởi tạo mặc định (No-args constructor).
     * Mặc định điểm tích lũy bằng 0 và trạng thái là đang hoạt động (true).
     */
    public KhachHang() {
        this.diemTichLuy = 0;
        this.trangThai = true;
    }

    /**
     * Hàm khởi tạo đầy đủ tham số khi đọc từ cơ sở dữ liệu.
     *
     * @param maKhachHang Mã khách hàng
     * @param hoTen Họ và tên
     * @param soDienThoai Số điện thoại
     * @param email Địa chỉ email
     * @param diemTichLuy Điểm tích lũy
     * @param diaChi Địa chỉ
     * @param ghiChu Ghi chú
     * @param trangThai Trạng thái hoạt động
     */
    public KhachHang(String maKhachHang, String hoTen, String soDienThoai, String email,
                     int diemTichLuy, String diaChi, String ghiChu, boolean trangThai) {
        this.maKhachHang = maKhachHang;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.diemTichLuy = diemTichLuy;
        this.diaChi = diaChi;
        this.ghiChu = ghiChu;
        this.trangThai = trangThai;
    }

    /**
     * Hàm khởi tạo nhanh khi tạo mới khách hàng từ giao diện (chưa có mã và mặc định hoạt động).
     */
    public KhachHang(String hoTen, String soDienThoai, String email, String diaChi, String ghiChu) {
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.diaChi = diaChi;
        this.ghiChu = ghiChu;
        this.diemTichLuy = 0;
        this.trangThai = true;
    }

    // =========================================================================
    // GETTERS VÀ SETTERS
    // =========================================================================

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
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

    public int getDiemTichLuy() {
        return diemTichLuy;
    }

    public void setDiemTichLuy(int diemTichLuy) {
        this.diemTichLuy = Math.max(0, diemTichLuy);
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }

    @Override
    public String toString() {
        return (hoTen != null ? hoTen : "") + " - " + (soDienThoai != null ? soDienThoai : "");
    }
}

