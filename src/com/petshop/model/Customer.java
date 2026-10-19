package com.petshop.model;

/**
 * Model đại diện cho một khách hàng (bảng KhachHang).
 */
public class Customer {
    private String maKhachHang;   // KH-YYYYMMDD-NNNNNN
    private String hoTen;
    private String soDienThoai;
    private String email;
    private int diemTichLuy;
    private String diaChi;
    private String ghiChu;

    // Trạng thái khách hàng: true đang hoạt động, false ngừng hoạt động.
    private boolean trangThai = true;

    public Customer() {}

    public Customer(String maKhachHang, String hoTen, String soDienThoai, String email,
                    int diemTichLuy, String diaChi, String ghiChu) {
        this.maKhachHang = maKhachHang;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.diemTichLuy = diemTichLuy;
        this.diaChi = diaChi;
        this.ghiChu = ghiChu;
    }

    public String getMaKhachHang()                      { return maKhachHang; }
    public void setMaKhachHang(String maKhachHang)      { this.maKhachHang = maKhachHang; }

    public String getHoTen()                { return hoTen; }
    public void setHoTen(String hoTen)      { this.hoTen = hoTen; }

    public String getSoDienThoai()                      { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai)      { this.soDienThoai = soDienThoai; }

    public String getEmail()                { return email; }
    public void setEmail(String email)      { this.email = email; }

    public int getDiemTichLuy()                     { return diemTichLuy; }
    public void setDiemTichLuy(int diemTichLuy)     { this.diemTichLuy = diemTichLuy; }

    public String getDiaChi()               { return diaChi; }
    public void setDiaChi(String diaChi)    { this.diaChi = diaChi; }

    public String getGhiChu()               { return ghiChu; }
    public void setGhiChu(String ghiChu)    { this.ghiChu = ghiChu; }

    /**
     * Lấy trạng thái hoạt động của khách hàng.
     *
     * @return true nếu đang hoạt động; false nếu đã ngừng hoạt động.
     */
    public boolean isTrangThai() {
        return trangThai;
    }

    /**
     * Cập nhật trạng thái đọc từ database.
     *
     * @param trangThai Trạng thái của khách hàng trong bảng KhachHang.
     */
    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return java.util.Objects.equals(maKhachHang, customer.maKhachHang);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(maKhachHang);
    }

    @Override
    public String toString() {
        return "Customer{ma='" + maKhachHang + "', hoTen='" + hoTen + "', sdt='" + soDienThoai + "'}";
    }
}
