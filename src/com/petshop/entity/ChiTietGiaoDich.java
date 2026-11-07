package com.petshop.entity;

/**
 * Thực thể chi tiết một dòng sản phẩm hoặc dịch vụ trong hóa đơn giao dịch.
 * Dùng để hiển thị trong popup Chi tiết giao dịch.
 * Tuân thủ 100% Điều 1 IUH: Sử dụng kiểu nguyên thủy double cho tiền tệ (0% BigDecimal).
 */
public class ChiTietGiaoDich {

    /** Mã sản phẩm hoặc mã dịch vụ */
    private final String maDoiTuong;

    /** Tên sản phẩm hoặc tên dịch vụ */
    private final String tenDoiTuong;

    /** Số lượng sử dụng / mua */
    private final int soLuong;

    /** Đơn giá tại thời điểm lập hóa đơn (double) */
    private final double donGia;

    public ChiTietGiaoDich(String maDoiTuong, String tenDoiTuong, int soLuong, double donGia) {
        this.maDoiTuong = maDoiTuong;
        this.tenDoiTuong = tenDoiTuong;
        this.soLuong = soLuong;
        this.donGia = Math.max(0.0, donGia);
    }

    public String getMaDoiTuong() {
        return maDoiTuong;
    }

    public String getTenDoiTuong() {
        return tenDoiTuong;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public double getDonGia() {
        return donGia;
    }

    /**
     * Phương thức tự tính toán: Thành tiền của dòng: Số lượng * Đơn giá (Điều 2 IUH).
     */
    public double tinhThanhTien() {
        return donGia * soLuong;
    }

    public double getThanhTien() {
        return tinhThanhTien();
    }
}

