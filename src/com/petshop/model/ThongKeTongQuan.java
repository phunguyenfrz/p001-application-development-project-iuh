package com.petshop.model;

import java.math.BigDecimal;

/**
 * Lớp chứa số liệu tổng quan thống kê cho Dashboard.
 */
public class ThongKeTongQuan {
    private BigDecimal doanhThuHomNay;
    private int soLichHenHomNay;
    private int soSanPhamSapHet;

    public ThongKeTongQuan() {
        this.doanhThuHomNay = BigDecimal.ZERO;
        this.soLichHenHomNay = 0;
        this.soSanPhamSapHet = 0;
    }

    public ThongKeTongQuan(BigDecimal doanhThuHomNay, int soLichHenHomNay, int soSanPhamSapHet) {
        this.doanhThuHomNay = doanhThuHomNay != null ? doanhThuHomNay : BigDecimal.ZERO;
        this.soLichHenHomNay = soLichHenHomNay;
        this.soSanPhamSapHet = soSanPhamSapHet;
    }

    public BigDecimal getDoanhThuHomNay() {
        return doanhThuHomNay;
    }

    public void setDoanhThuHomNay(BigDecimal doanhThuHomNay) {
        this.doanhThuHomNay = doanhThuHomNay;
    }

    public int getSoLichHenHomNay() {
        return soLichHenHomNay;
    }

    public void setSoLichHenHomNay(int soLichHenHomNay) {
        this.soLichHenHomNay = soLichHenHomNay;
    }

    public int getSoSanPhamSapHet() {
        return soSanPhamSapHet;
    }

    public void setSoSanPhamSapHet(int soSanPhamSapHet) {
        this.soSanPhamSapHet = soSanPhamSapHet;
    }
}

