package com.petshop.model;

import java.time.LocalDate;

/**
 * Model đại diện cho một thú cưng (bảng ThuCung).
 */
public class Pet {
    private String maThuCung;     // TC-YYYYMMDD-NNNNNN
    private String maKhachHang;   // Chủ nhân
    private String tenThuCung;
    private String loaiThuCung;   // Chó, Mèo, v.v.
    private String giongLoai;     // Giống
    private double canNang;       // kg
    private LocalDate ngaySinh;
    private String diUng;
    private String gioiTinh;      // "Đực" / "Cái"
    private String tenChu;
    private String dienThoaiChu;
    private String dacDiemNhanDang;
    private String benhNen;
    private String tinhCach;
    private boolean active = true;

    public Pet() {}

    public Pet(String maThuCung, String maKhachHang, String tenThuCung, String loaiThuCung,
               String giongLoai, double canNang, LocalDate ngaySinh, String diUng, String gioiTinh) {
        this.maThuCung = maThuCung;
        this.maKhachHang = maKhachHang;
        this.tenThuCung = tenThuCung;
        this.loaiThuCung = loaiThuCung;
        this.giongLoai = giongLoai;
        this.canNang = canNang;
        this.ngaySinh = ngaySinh;
        this.diUng = diUng;
        this.gioiTinh = gioiTinh;
    }

    public String getMaThuCung()                    { return maThuCung; }
    public void setMaThuCung(String maThuCung)      { this.maThuCung = maThuCung; }

    public String getMaKhachHang()                      { return maKhachHang; }
    public void setMaKhachHang(String maKhachHang)      { this.maKhachHang = maKhachHang; }

    public String getTenThuCung()                   { return tenThuCung; }
    public void setTenThuCung(String tenThuCung)    { this.tenThuCung = tenThuCung; }

    public String getLoaiThuCung()                      { return loaiThuCung; }
    public void setLoaiThuCung(String loaiThuCung)      { this.loaiThuCung = loaiThuCung; }

    public String getGiongLoai()                { return giongLoai; }
    public void setGiongLoai(String giongLoai)  { this.giongLoai = giongLoai; }

    public double getCanNang()                  { return canNang; }
    public void setCanNang(double canNang)      { this.canNang = canNang; }

    public LocalDate getNgaySinh()                  { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh)     { this.ngaySinh = ngaySinh; }

    public String getDiUng()                { return diUng; }
    public void setDiUng(String diUng)      { this.diUng = diUng; }

    public String getGioiTinh()                 { return gioiTinh; }
    public void setGioiTinh(String gioiTinh)    { this.gioiTinh = gioiTinh; }

    public String getTenChu()                   { return tenChu; }
    public void setTenChu(String tenChu)        { this.tenChu = tenChu; }

    public String getDienThoaiChu()                   { return dienThoaiChu; }
    public void setDienThoaiChu(String dienThoaiChu)  { this.dienThoaiChu = dienThoaiChu; }

    public String getDacDiemNhanDang() { return dacDiemNhanDang; }
    public void setDacDiemNhanDang(String dacDiemNhanDang) {
        this.dacDiemNhanDang = dacDiemNhanDang;
    }

    public String getBenhNen() { return benhNen; }
    public void setBenhNen(String benhNen) { this.benhNen = benhNen; }

    public String getTinhCach() { return tinhCach; }
    public void setTinhCach(String tinhCach) { this.tinhCach = tinhCach; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    /** Phân loại kích thước theo cân nặng: Nhỏ (&lt;5kg), Vừa (5-10kg), Lớn (&gt;10kg). */
    public String getPhanLoaiKichThuoc() {
        if (canNang < 5) return "Nhỏ";
        if (canNang <= 10) return "Vừa";
        return "Lớn";
    }

    @Override
    public String toString() {
        return "Pet{ma='" + maThuCung + "', ten='" + tenThuCung + "', loai='" + loaiThuCung
                + "', giong='" + giongLoai + "'}";
    }
}
