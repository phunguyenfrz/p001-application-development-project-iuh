package com.petshop.model;

/**
 * Thực thể Dịch vụ chăm sóc thú cưng, ánh xạ bảng DichVuChamSoc.
 * Lưu kèm giá hiện hành của 3 bậc cân nặng (Nhỏ, Vừa, Lớn) để hiển thị trên giao diện.
 */
public class DichVuChamSoc {

    private String maDichVu;
    private String tenDichVu;
    private String moTa;
    private int thoiLuong; // tính bằng phút
    private boolean trangThai; // true: đang kinh doanh, false: ngừng kinh doanh

    private double giaNho;
    private double giaVua;
    private double giaLon;

    public DichVuChamSoc() {
        this.thoiLuong = 30;
        this.trangThai = true;
    }

    public DichVuChamSoc(String maDichVu, String tenDichVu, String moTa, int thoiLuong, boolean trangThai) {
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.moTa = moTa;
        this.thoiLuong = thoiLuong;
        this.trangThai = trangThai;
    }

    public DichVuChamSoc(String maDichVu, String tenDichVu, String moTa, int thoiLuong, boolean trangThai,
                         double giaNho, double giaVua, double giaLon) {
        this(maDichVu, tenDichVu, moTa, thoiLuong, trangThai);
        this.giaNho = giaNho;
        this.giaVua = giaVua;
        this.giaLon = giaLon;
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(String maDichVu) {
        this.maDichVu = maDichVu;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public void setTenDichVu(String tenDichVu) {
        this.tenDichVu = tenDichVu;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public int getThoiLuong() {
        return thoiLuong;
    }

    public void setThoiLuong(int thoiLuong) {
        this.thoiLuong = thoiLuong;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }

    public double getGiaNho() {
        return giaNho;
    }

    public void setGiaNho(double giaNho) {
        this.giaNho = giaNho;
    }

    public double getGiaVua() {
        return giaVua;
    }

    public void setGiaVua(double giaVua) {
        this.giaVua = giaVua;
    }

    public double getGiaLon() {
        return giaLon;
    }

    public void setGiaLon(double giaLon) {
        this.giaLon = giaLon;
    }
}

