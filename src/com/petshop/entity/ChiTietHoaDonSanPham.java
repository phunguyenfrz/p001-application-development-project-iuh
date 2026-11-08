package com.petshop.entity;

/**
 * Thực thể ánh xạ bảng ChiTietHoaDon_SanPham trong cơ sở dữ liệu SQL Server.
 * Tuân thủ Bộ chỉ thị kỹ thuật IUH:
 * - Điều 1: Tiền tệ dùng kiểu nguyên thủy double, không dùng BigDecimal.
 * - Điều 2: Khóa ngoại dùng Object Reference (SanPham sanPham), giữ Delegate Getters,
 *           thuộc tính dẫn xuất thanhTien được tính qua phương thức tinhThanhTien(), không lưu biến/setter.
 */
public class ChiTietHoaDonSanPham {

    private String maHoaDon;
    private SanPham sanPham;
    private int soLuong;
    private double donGia;

    public ChiTietHoaDonSanPham() {
        this.soLuong = 1;
        this.donGia = 0.0;
    }

    public ChiTietHoaDonSanPham(SanPham sanPham, int soLuong, double donGia) {
        this.sanPham = sanPham;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    public ChiTietHoaDonSanPham(String maHoaDon, String maSanPham, int soLuong, double donGia) {
        this.maHoaDon = maHoaDon;
        setMaSanPham(maSanPham);
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    public ChiTietHoaDonSanPham(String maHoaDon, String maSanPham, String tenSanPham, String donViTinh, int soLuong, double donGia) {
        this(maHoaDon, maSanPham, soLuong, donGia);
        setTenSanPham(tenSanPham);
        setDonViTinh(donViTinh);
    }

    /**
     * Phương thức tự tính thành tiền dẫn xuất: soLuong * donGia (Điều 2 IUH).
     */
    public double tinhThanhTien() {
        return this.soLuong * getDonGia();
    }

    /**
     * Delegate getter trả về kết quả từ phương thức tính toán (không lưu biến thành viên).
     */
    public double getThanhTien() {
        return tinhThanhTien();
    }

    // Getters and Setters
    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public SanPham getSanPham() {
        return sanPham;
    }

    public void setSanPham(SanPham sanPham) {
        this.sanPham = sanPham;
    }

    // Delegate Getters bảo toàn tương thích và binding TableView
    public String getMaSanPham() {
        return sanPham != null ? sanPham.getMaSanPham() : null;
    }

    public void setMaSanPham(String maSanPham) {
        if (maSanPham == null) {
            if (this.sanPham != null) {
                this.sanPham.setMaSanPham(null);
            }
            return;
        }
        if (this.sanPham == null) {
            this.sanPham = new SanPham();
        }
        this.sanPham.setMaSanPham(maSanPham);
    }

    public String getTenSanPham() {
        return sanPham != null ? sanPham.getTenSanPham() : null;
    }

    public void setTenSanPham(String tenSanPham) {
        if (this.sanPham == null) {
            this.sanPham = new SanPham();
        }
        this.sanPham.setTenSanPham(tenSanPham);
    }

    public String getDonViTinh() {
        return sanPham != null ? sanPham.getDonViTinh() : null;
    }

    public void setDonViTinh(String donViTinh) {
        if (this.sanPham == null) {
            this.sanPham = new SanPham();
        }
        this.sanPham.setDonViTinh(donViTinh);
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        if (soLuong <= 0) {
            throw new IllegalArgumentException("Số lượng sản phẩm phải lớn hơn 0!");
        }
        this.soLuong = soLuong;
    }

    public double getDonGia() {
        if (donGia > 0.0) {
            return donGia;
        }
        if (sanPham != null) {
            return sanPham.getGiaBan();
        }
        return 0.0;
    }

    public void setDonGia(double donGia) {
        if (donGia < 0.0) {
            throw new IllegalArgumentException("Đơn giá không được âm!");
        }
        this.donGia = donGia;
    }
}
