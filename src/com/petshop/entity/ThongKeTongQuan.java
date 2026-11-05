package com.petshop.entity;

/**
 * Đối tượng truyền dữ liệu (DTO) chứa các chỉ số thống kê nhanh cho màn hình Tổng quan (Dashboard).
 * Tuân thủ Bộ chỉ thị kỹ thuật IUH: kiểu tiền tệ doanhThuHomNay dùng double nguyên thủy.
 */
public class ThongKeTongQuan {
    private double doanhThuHomNay;
    private int soHoaDonHomNay;
    private int tongSoLichHenHomNay;
    private int soLichHenChoXuLy;
    private int soSanPhamSapHet;

    public ThongKeTongQuan() {
        this.doanhThuHomNay = 0.0;
    }

    public ThongKeTongQuan(double doanhThuHomNay, int soHoaDonHomNay,
                           int tongSoLichHenHomNay, int soLichHenChoXuLy,
                           int soSanPhamSapHet) {
        this.doanhThuHomNay = doanhThuHomNay;
        this.soHoaDonHomNay = soHoaDonHomNay;
        this.tongSoLichHenHomNay = tongSoLichHenHomNay;
        this.soLichHenChoXuLy = soLichHenChoXuLy;
        this.soSanPhamSapHet = soSanPhamSapHet;
    }

    public double getDoanhThuHomNay() {
        return doanhThuHomNay;
    }

    public void setDoanhThuHomNay(double doanhThuHomNay) {
        this.doanhThuHomNay = doanhThuHomNay;
    }

    public int getSoHoaDonHomNay() {
        return soHoaDonHomNay;
    }

    public void setSoHoaDonHomNay(int soHoaDonHomNay) {
        this.soHoaDonHomNay = soHoaDonHomNay;
    }

    public int getTongSoLichHenHomNay() {
        return tongSoLichHenHomNay;
    }

    public void setTongSoLichHenHomNay(int tongSoLichHenHomNay) {
        this.tongSoLichHenHomNay = tongSoLichHenHomNay;
    }

    public int getSoLichHenChoXuLy() {
        return soLichHenChoXuLy;
    }

    public void setSoLichHenChoXuLy(int soLichHenChoXuLy) {
        this.soLichHenChoXuLy = soLichHenChoXuLy;
    }

    public int getSoSanPhamSapHet() {
        return soSanPhamSapHet;
    }

    public void setSoSanPhamSapHet(int soSanPhamSapHet) {
        this.soSanPhamSapHet = soSanPhamSapHet;
    }
}
