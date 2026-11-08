package com.petshop.entity;

import java.time.LocalDate;

/**
 * Thực thể Thú cưng (ánh xạ bảng ThuCung trong cơ sở dữ liệu).
 * Đại diện cho hồ sơ thú cưng của khách hàng trong hệ thống Happy Tails.
 * Tuân thủ Chuẩn hóa OOP (Object Reference KhachHang), kiểu nguyên thủy double cho canNang (IUH Directive).
 */
public class ThuCung {

    /** Mã định danh thú cưng (định dạng: TC-yyyyMMdd-xxxxxx) */
    private String maThuCung;

    /** Tham chiếu đối tượng Khách hàng sở hữu thú cưng (OOP Object Reference) */
    private KhachHang khachHang;

    /** Tên của thú cưng */
    private String tenThuCung;

    /** Loài thú cưng (VD: Chó, Mèo, Chim, Thỏ...) */
    private String loaiThuCung;

    /** Giống loài chi tiết (VD: Poodle, Corgi, Mèo Anh lông ngắn...) */
    private String giongLoai;

    /** Cân nặng (kg) - Dùng kiểu nguyên thủy double theo Chuẩn IUH */
    private double canNang;

    /** Ngày sinh của thú cưng */
    private LocalDate ngaySinh;

    /** Thông tin dị ứng, tiền sử bệnh án hoặc ghi chú đặc biệt */
    private String diUng;

    /** Giới tính: Đực hoặc Cái */
    private String gioiTinh;

    public ThuCung() {
        this.canNang = 0.0;
        this.loaiThuCung = "Chó";
        this.gioiTinh = "Đực";
    }

    public ThuCung(String maThuCung, String maKhachHang, String tenThuCung, String loaiThuCung,
                   String giongLoai, double canNang, LocalDate ngaySinh, String diUng, String gioiTinh) {
        this.maThuCung = maThuCung;
        setMaKhachHang(maKhachHang);
        this.tenThuCung = tenThuCung;
        this.loaiThuCung = loaiThuCung;
        this.giongLoai = giongLoai;
        this.canNang = canNang;
        this.ngaySinh = ngaySinh;
        this.diUng = diUng;
        this.gioiTinh = gioiTinh;
    }

    public ThuCung(String maThuCung, KhachHang khachHang, String tenThuCung, String loaiThuCung,
                   String giongLoai, double canNang, LocalDate ngaySinh, String diUng, String gioiTinh) {
        this.maThuCung = maThuCung;
        this.khachHang = khachHang;
        this.tenThuCung = tenThuCung;
        this.loaiThuCung = loaiThuCung;
        this.giongLoai = giongLoai;
        this.canNang = canNang;
        this.ngaySinh = ngaySinh;
        this.diUng = diUng;
        this.gioiTinh = gioiTinh;
    }

    public ThuCung(String maThuCung, String maKhachHang, String tenThuCung, String loaiThuCung,
                   String giongLoai, double canNang, LocalDate ngaySinh, String diUng, String gioiTinh,
                   String tenKhachHang, String soDienThoaiKhachHang) {
        this(maThuCung, maKhachHang, tenThuCung, loaiThuCung, giongLoai, canNang, ngaySinh, diUng, gioiTinh);
        setTenKhachHang(tenKhachHang);
        setSoDienThoaiKhachHang(soDienThoaiKhachHang);
    }

    // =========================================================================
    // GETTERS VÀ SETTERS
    // =========================================================================

    public String getMaThuCung() {
        return maThuCung;
    }

    public void setMaThuCung(String maThuCung) {
        this.maThuCung = maThuCung;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    // Delegate Getters / Setters để không làm gãy TableView và code cũ
    public String getMaKhachHang() {
        return khachHang != null ? khachHang.getMaKhachHang() : null;
    }

    public void setMaKhachHang(String maKhachHang) {
        if (maKhachHang == null) {
            if (this.khachHang != null) {
                this.khachHang.setMaKhachHang(null);
            }
            return;
        }
        if (this.khachHang == null) {
            this.khachHang = new KhachHang();
        }
        this.khachHang.setMaKhachHang(maKhachHang);
    }

    public String getTenKhachHang() {
        return (khachHang != null && khachHang.getHoTen() != null) ? khachHang.getHoTen() : "";
    }

    public void setTenKhachHang(String tenKhachHang) {
        if (this.khachHang == null) {
            this.khachHang = new KhachHang();
        }
        this.khachHang.setHoTen(tenKhachHang);
    }

    public String getSoDienThoaiKhachHang() {
        return (khachHang != null && khachHang.getSoDienThoai() != null) ? khachHang.getSoDienThoai() : "";
    }

    public void setSoDienThoaiKhachHang(String soDienThoai) {
        if (this.khachHang == null) {
            this.khachHang = new KhachHang();
        }
        this.khachHang.setSoDienThoai(soDienThoai);
    }

    public String getTenThuCung() {
        return tenThuCung;
    }

    public void setTenThuCung(String tenThuCung) {
        this.tenThuCung = tenThuCung;
    }

    public String getLoaiThuCung() {
        return loaiThuCung;
    }

    public void setLoaiThuCung(String loaiThuCung) {
        this.loaiThuCung = loaiThuCung;
    }

    public String getGiongLoai() {
        return giongLoai;
    }

    public void setGiongLoai(String giongLoai) {
        this.giongLoai = giongLoai;
    }

    public double getCanNang() {
        return canNang;
    }

    public void setCanNang(double canNang) {
        this.canNang = canNang;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getDiUng() {
        return diUng;
    }

    public void setDiUng(String diUng) {
        this.diUng = diUng;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    @Override
    public String toString() {
        String loai = (loaiThuCung != null && !loaiThuCung.isBlank()) ? loaiThuCung : "Thú cưng";
        String giong = (giongLoai != null && !giongLoai.isBlank()) ? " - " + giongLoai : "";
        String can = canNang > 0 ? String.format(" (%.1f kg)", canNang) : "";
        return (tenThuCung != null ? tenThuCung : "") + " [" + loai + giong + "]" + can;
    }
}
