package com.petshop.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Thực thể Thú cưng (ánh xạ bảng ThuCung trong cơ sở dữ liệu).
 * Đại diện cho hồ sơ thú cưng của khách hàng trong hệ thống Happy Tails.
 */
public class ThuCung {

    /** Mã định danh thú cưng (định dạng: TC-yyyyMMdd-xxxxxx) */
    private String maThuCung;

    /** Mã khách hàng sở hữu thú cưng (Khóa ngoại tham chiếu KhachHang) */
    private String maKhachHang;

    /** Tên của thú cưng */
    private String tenThuCung;

    /** Loài thú cưng (VD: Chó, Mèo, Chim, Thỏ...) */
    private String loaiThuCung;

    /** Giống loài chi tiết (VD: Poodle, Corgi, Mèo Anh lông ngắn...) */
    private String giongLoai;

    /** Cân nặng (kg) */
    private BigDecimal canNang;

    /** Ngày sinh của thú cưng */
    private LocalDate ngaySinh;

    /** Thông tin dị ứng, tiền sử bệnh án hoặc ghi chú đặc biệt */
    private String diUng;

    /** Giới tính: Đực hoặc Cái */
    private String gioiTinh;

    /** Tên khách hàng sở hữu (thuộc tính hiển thị mở rộng khi JOIN) */
    private String tenKhachHang;

    /** Số điện thoại khách hàng sở hữu (thuộc tính hiển thị mở rộng khi JOIN) */
    private String soDienThoaiKhachHang;

    public ThuCung() {
        this.canNang = BigDecimal.ZERO;
    }

    public ThuCung(String maThuCung, String maKhachHang, String tenThuCung, String loaiThuCung,
                   String giongLoai, BigDecimal canNang, LocalDate ngaySinh, String diUng, String gioiTinh) {
        this.maThuCung = maThuCung;
        this.maKhachHang = maKhachHang;
        this.tenThuCung = tenThuCung;
        this.loaiThuCung = loaiThuCung;
        this.giongLoai = giongLoai;
        this.canNang = canNang != null ? canNang : BigDecimal.ZERO;
        this.ngaySinh = ngaySinh;
        this.diUng = diUng;
        this.gioiTinh = gioiTinh;
    }

    public ThuCung(String maThuCung, String maKhachHang, String tenThuCung, String loaiThuCung,
                   String giongLoai, BigDecimal canNang, LocalDate ngaySinh, String diUng, String gioiTinh,
                   String tenKhachHang, String soDienThoaiKhachHang) {
        this(maThuCung, maKhachHang, tenThuCung, loaiThuCung, giongLoai, canNang, ngaySinh, diUng, gioiTinh);
        this.tenKhachHang = tenKhachHang;
        this.soDienThoaiKhachHang = soDienThoaiKhachHang;
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

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
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

    public BigDecimal getCanNang() {
        return canNang;
    }

    public void setCanNang(BigDecimal canNang) {
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

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getSoDienThoaiKhachHang() {
        return soDienThoaiKhachHang;
    }

    public void setSoDienThoaiKhachHang(String soDienThoaiKhachHang) {
        this.soDienThoaiKhachHang = soDienThoaiKhachHang;
    }

    @Override
    public String toString() {
        return (tenThuCung != null ? tenThuCung : "") + " (" + (loaiThuCung != null ? loaiThuCung : "") + ")";
    }
}
