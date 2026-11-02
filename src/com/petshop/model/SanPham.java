package com.petshop.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Thực thể Sản phẩm (hàng hóa) trong cửa hàng Happy Tails.
 * Khớp bảng SanPham trong CSDL SQL Server.
 */
public class SanPham {

    private String maSanPham;
    private String maNhaCungCap;
    private String tenSanPham;
    private String maVach;
    private BigDecimal giaNhap;
    private BigDecimal giaBan;
    private int soLuongTon;
    private int nguongTonToiThieu;
    private String donViTinh;
    private boolean trangThai; // true: đang kinh doanh, false: ngừng kinh doanh (xóa mềm)

    // Tên nhà cung cấp hiển thị trên bảng biểu
    private String tenNhaCungCap;

    public SanPham() {
        this.giaNhap = BigDecimal.ZERO;
        this.giaBan = BigDecimal.ZERO;
        this.soLuongTon = 0;
        this.nguongTonToiThieu = 10;
        this.trangThai = true;
    }

    public SanPham(String maSanPham, String maNhaCungCap, String tenSanPham, String maVach,
                   BigDecimal giaBan, int soLuongTon, int nguongTonToiThieu, String donViTinh, boolean trangThai) {
        this(maSanPham, maNhaCungCap, tenSanPham, maVach,
                tinhGiaNhapMacDinh(giaBan), giaBan,
                soLuongTon, nguongTonToiThieu, donViTinh, trangThai);
    }

    public SanPham(String maSanPham, String maNhaCungCap, String tenSanPham, String maVach,
                   BigDecimal giaNhap, BigDecimal giaBan, int soLuongTon, int nguongTonToiThieu,
                   String donViTinh, boolean trangThai) {
        this.maSanPham = maSanPham;
        this.maNhaCungCap = maNhaCungCap;
        this.tenSanPham = tenSanPham;
        this.maVach = maVach;
        this.giaBan = giaBan != null ? giaBan : BigDecimal.ZERO;
        if (giaNhap != null && giaNhap.compareTo(BigDecimal.ZERO) > 0
                && (this.giaBan.compareTo(BigDecimal.ZERO) <= 0 || giaNhap.compareTo(this.giaBan) < 0)) {
            this.giaNhap = giaNhap;
        } else {
            this.giaNhap = tinhGiaNhapMacDinh(this.giaBan);
        }
        this.soLuongTon = soLuongTon;
        this.nguongTonToiThieu = nguongTonToiThieu;
        this.donViTinh = donViTinh;
        this.trangThai = trangThai;
    }

    /**
     * Tính giá nhập mặc định hợp lý (70% giá bán) cho dữ liệu cũ chưa có giá nhập,
     * đảm bảo 0 < giaNhap < giaBan khi giaBan > 1.
     */
    public static BigDecimal tinhGiaNhapMacDinh(BigDecimal giaBan) {
        if (giaBan == null || giaBan.compareTo(BigDecimal.ONE) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal macDinh;
        if (giaBan.compareTo(new BigDecimal("2000")) >= 0) {
            macDinh = giaBan.multiply(new BigDecimal("0.7")).setScale(-2, RoundingMode.HALF_UP).setScale(0, RoundingMode.DOWN);
        } else {
            macDinh = giaBan.multiply(new BigDecimal("0.7")).setScale(0, RoundingMode.DOWN);
        }
        if (macDinh.compareTo(BigDecimal.ZERO) <= 0) {
            macDinh = BigDecimal.ONE;
        }
        if (macDinh.compareTo(giaBan) >= 0) {
            macDinh = giaBan.subtract(BigDecimal.ONE);
        }
        return macDinh;
    }

    public String getMaSanPham() {
        return maSanPham;
    }

    public void setMaSanPham(String maSanPham) {
        this.maSanPham = maSanPham;
    }

    public String getMaNhaCungCap() {
        return maNhaCungCap;
    }

    public void setMaNhaCungCap(String maNhaCungCap) {
        this.maNhaCungCap = maNhaCungCap;
    }

    public String getTenSanPham() {
        return tenSanPham;
    }

    public void setTenSanPham(String tenSanPham) {
        this.tenSanPham = tenSanPham;
    }

    public String getMaVach() {
        return maVach;
    }

    public void setMaVach(String maVach) {
        this.maVach = maVach;
    }

    public BigDecimal getGiaNhap() {
        return giaNhap;
    }

    public void setGiaNhap(BigDecimal giaNhap) {
        this.giaNhap = giaNhap;
    }

    /**
     * Trả về giá nhập hợp lệ (nếu dữ liệu cũ chưa có giá nhập hoặc không hợp lệ thì tự tính giá mặc định hợp lý từ giá bán).
     */
    public BigDecimal getGiaNhapHopLe() {
        if (giaNhap != null && giaNhap.compareTo(BigDecimal.ZERO) > 0
                && (giaBan == null || giaBan.compareTo(BigDecimal.ZERO) <= 0 || giaNhap.compareTo(giaBan) < 0)) {
            return giaNhap;
        }
        return tinhGiaNhapMacDinh(giaBan);
    }

    public BigDecimal getGiaBan() {
        return giaBan;
    }

    public void setGiaBan(BigDecimal giaBan) {
        this.giaBan = giaBan != null ? giaBan : BigDecimal.ZERO;
    }

    public int getSoLuongTon() {
        return soLuongTon;
    }

    public void setSoLuongTon(int soLuongTon) {
        this.soLuongTon = soLuongTon;
    }

    public int getNguongTonToiThieu() {
        return nguongTonToiThieu;
    }

    public void setNguongTonToiThieu(int nguongTonToiThieu) {
        this.nguongTonToiThieu = nguongTonToiThieu;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }

    public String getTenNhaCungCap() {
        return tenNhaCungCap;
    }

    public void setTenNhaCungCap(String tenNhaCungCap) {
        this.tenNhaCungCap = tenNhaCungCap;
    }

    public boolean isSapHetHang() {
        return this.soLuongTon > 0 && this.soLuongTon <= this.nguongTonToiThieu;
    }

    public boolean isHetHang() {
        return this.soLuongTon <= 0;
    }

    /**
     * Trạng thái hiển thị sản phẩm:
     * - Nếu trangThai == false: "Ngừng kinh doanh"
     * - Nếu trangThai == true và soLuongTon <= 0: "Hết hàng"
     * - Nếu trangThai == true và soLuongTon > 0: "Đang kinh doanh"
     */
    public String getTrangThaiHienThi() {
        if (!this.trangThai) {
            return "Ngừng kinh doanh";
        }
        if (this.soLuongTon <= 0) {
            return "Hết hàng";
        }
        if (isSapHetHang()) {
            return "Sắp hết hàng";
        }
        return "Đang kinh doanh";
    }
}
