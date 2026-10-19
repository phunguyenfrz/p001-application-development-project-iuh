package com.petshop.model;

import java.util.Objects;

/**
 * Lớp thực thể ReturnNoteItem đại diện cho một dòng sản phẩm trong chi tiết phiếu đổi trả (bảng ChiTietPhieuDoiTra).
 *
 * Tiêu chuẩn tuân thủ:
 * - Tên lớp là danh từ (ReturnNoteItem), tên phương thức là động từ.
 * - Có bình luận 1 dòng và nhiều dòng đầy đủ.
 */
public class ReturnNoteItem {

    private String maPhieuDoiTra;
    private String maHoaDon;
    private String maSanPham;
    private String tenSanPham; // Tên hiển thị sản phẩm
    private int soLuong;
    private double donGia;

    /**
     * Khởi tạo mặc định.
     */
    public ReturnNoteItem() {
    }

    /**
     * Khởi tạo đầy đủ thông tin cho dòng chi tiết phiếu đổi trả.
     *
     * @param maPhieuDoiTra Mã phiếu đổi trả
     * @param maHoaDon      Mã hóa đơn gốc đã mua
     * @param maSanPham     Mã sản phẩm đổi trả
     * @param tenSanPham    Tên sản phẩm
     * @param soLuong       Số lượng cần đổi trả
     * @param donGia        Đơn giá gốc từ hóa đơn
     */
    public ReturnNoteItem(String maPhieuDoiTra, String maHoaDon, String maSanPham,
                          String tenSanPham, int soLuong, double donGia) {
        this.maPhieuDoiTra = maPhieuDoiTra;
        this.maHoaDon = maHoaDon;
        this.maSanPham = maSanPham;
        this.tenSanPham = tenSanPham;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    /**
     * Tính thành tiền hoàn của dòng sản phẩm này.
     *
     * @return Thành tiền = Đơn giá * Số lượng
     */
    public double tinhThanhTien() {
        return this.donGia * this.soLuong;
    }

    // --- Getter và Setter ---

    public String getMaPhieuDoiTra() {
        return maPhieuDoiTra;
    }

    public void setMaPhieuDoiTra(String maPhieuDoiTra) {
        this.maPhieuDoiTra = maPhieuDoiTra;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public String getMaSanPham() {
        return maSanPham;
    }

    public void setMaSanPham(String maSanPham) {
        this.maSanPham = maSanPham;
    }

    public String getTenSanPham() {
        return tenSanPham;
    }

    public void setTenSanPham(String tenSanPham) {
        this.tenSanPham = tenSanPham;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public double getDonGia() {
        return donGia;
    }

    public void setDonGia(double donGia) {
        this.donGia = donGia;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReturnNoteItem that = (ReturnNoteItem) o;
        return Objects.equals(maPhieuDoiTra, that.maPhieuDoiTra) &&
                Objects.equals(maSanPham, that.maSanPham);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maPhieuDoiTra, maSanPham);
    }

    @Override
    public String toString() {
        return "ReturnNoteItem{" +
                "maSanPham='" + maSanPham + '\'' +
                ", tenSanPham='" + tenSanPham + '\'' +
                ", soLuong=" + soLuong +
                ", donGia=" + donGia +
                ", thanhTien=" + tinhThanhTien() +
                '}';
    }
}

