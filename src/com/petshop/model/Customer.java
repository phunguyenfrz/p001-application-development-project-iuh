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

    @Override
    public String toString() {
        return "Customer{ma='" + maKhachHang + "', hoTen='" + hoTen + "', sdt='" + soDienThoai + "'}";
    }
}
