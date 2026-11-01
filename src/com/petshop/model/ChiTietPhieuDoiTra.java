package com.petshop.model;

import java.math.BigDecimal;

/**
 * Thực thể chi tiết mặt hàng trong Phiếu đổi trả (ChiTietPhieuDoiTra).
 */
public class ChiTietPhieuDoiTra {

    private String maPhieuDoiTra;
    private String maHoaDon;
    private String maSanPham; // Mã sản phẩm khách trả lại
    private String tenSanPham;
    private String donViTinh;
    private BigDecimal donGia; // Đơn giá khi mua
    private int soLuongMua; // Số lượng khách đã mua trong hóa đơn gốc
    private int soLuongDaDoiTraTruoc; // Số lượng đã đổi trả trước đó
    private int soLuong; // Số lượng trả lại lần này
    private String lyDo; // "Lỗi sản phẩm", "Hết hạn", "Không phù hợp", "Khác"
    private String tinhTrang; // "Nguyên vẹn", "Đã mở", "Hư hỏng"
    private String ghiChu; // Bắt buộc khi lý do là "Khác" hoặc "Lỗi sản phẩm" bị mở/hư hỏng
    private String huongXuLy; // "Nhập lại kho bán", "Trả nhà cung cấp", "Tiêu hủy"

    // Thông tin sản phẩm đổi lấy mới (nếu có)
    private String maSanPhamDoi;
    private String tenSanPhamDoi;
    private BigDecimal donGiaDoi;
    private int soLuongDoi;

    public ChiTietPhieuDoiTra() {
        this.donGia = BigDecimal.ZERO;
        this.donGiaDoi = BigDecimal.ZERO;
        this.soLuong = 1;
        this.soLuongDoi = 0;
        this.lyDo = "Lỗi sản phẩm";
        this.tinhTrang = "Nguyên vẹn";
        this.huongXuLy = "Trả nhà cung cấp";
    }

    public ChiTietPhieuDoiTra(String maPhieuDoiTra, String maHoaDon, String maSanPham,
                              int soLuong, BigDecimal donGia, String lyDo, String tinhTrang,
                              String maSanPhamDoi, int soLuongDoi) {
        this();
        this.maPhieuDoiTra = maPhieuDoiTra;
        this.maHoaDon = maHoaDon;
        this.maSanPham = maSanPham;
        this.soLuong = soLuong;
        this.donGia = donGia != null ? donGia : BigDecimal.ZERO;
        this.lyDo = lyDo != null ? lyDo : "Lỗi sản phẩm";
        this.tinhTrang = tinhTrang != null ? tinhTrang : "Nguyên vẹn";
        this.maSanPhamDoi = maSanPhamDoi;
        this.soLuongDoi = soLuongDoi;
        this.huongXuLy = tuDongGoiYHuongXuLy(this.lyDo, this.tinhTrang);
    }

    public ChiTietPhieuDoiTra(String maPhieuDoiTra, String maHoaDon, String maSanPham, String tenSanPham,
                              int soLuong, BigDecimal donGia, String lyDo, String tinhTrang,
                              String huongXuLy, String ghiChu) {
        this();
        this.maPhieuDoiTra = maPhieuDoiTra;
        this.maHoaDon = maHoaDon;
        this.maSanPham = maSanPham;
        this.tenSanPham = tenSanPham;
        this.soLuong = soLuong;
        this.donGia = donGia != null ? donGia : BigDecimal.ZERO;
        this.lyDo = lyDo != null ? lyDo : "Lỗi sản phẩm";
        this.tinhTrang = tinhTrang != null ? tinhTrang : "Nguyên vẹn";
        this.huongXuLy = huongXuLy != null ? huongXuLy : tuDongGoiYHuongXuLy(this.lyDo, this.tinhTrang);
        this.ghiChu = ghiChu;
    }

    /**
     * Tự động gợi ý hướng xử lý hàng nhận lại theo Quy tắc QL26:
     * - Nguyên vẹn + Không phù hợp/Khác -> Nhập lại kho bán.
     * - Lỗi sản phẩm -> Trả nhà cung cấp.
     * - Hết hạn, hoặc tình trạng Hư hỏng -> Tiêu hủy.
     */
    public static String tuDongGoiYHuongXuLy(String lyDo, String tinhTrang) {
        if ("Hết hạn".equalsIgnoreCase(lyDo) || "Hư hỏng".equalsIgnoreCase(tinhTrang)) {
            return "Tiêu hủy";
        }
        if ("Lỗi sản phẩm".equalsIgnoreCase(lyDo)) {
            return "Trả nhà cung cấp";
        }
        if ("Nguyên vẹn".equalsIgnoreCase(tinhTrang) &&
                ("Không phù hợp".equalsIgnoreCase(lyDo) || "Khác".equalsIgnoreCase(lyDo))) {
            return "Nhập lại kho bán";
        }
        return "Trả nhà cung cấp";
    }

    // Tính thành tiền của mặt hàng trả lại
    public BigDecimal tinhThanhTienTra() {
        if (donGia == null || soLuong <= 0) return BigDecimal.ZERO;
        return donGia.multiply(BigDecimal.valueOf(soLuong));
    }

    // Tính thành tiền của mặt hàng đổi lấy mới
    public BigDecimal tinhThanhTienDoi() {
        if (donGiaDoi == null || soLuongDoi <= 0) return BigDecimal.ZERO;
        return donGiaDoi.multiply(BigDecimal.valueOf(soLuongDoi));
    }

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

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public BigDecimal getDonGia() {
        return donGia != null ? donGia : BigDecimal.ZERO;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia != null ? donGia : BigDecimal.ZERO;
    }

    public int getSoLuongMua() {
        return soLuongMua;
    }

    public void setSoLuongMua(int soLuongMua) {
        this.soLuongMua = soLuongMua;
    }

    public int getSoLuongDaDoiTraTruoc() {
        return soLuongDaDoiTraTruoc;
    }

    public void setSoLuongDaDoiTraTruoc(int soLuongDaDoiTraTruoc) {
        this.soLuongDaDoiTraTruoc = soLuongDaDoiTraTruoc;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public String getTinhTrang() {
        return tinhTrang;
    }

    public void setTinhTrang(String tinhTrang) {
        this.tinhTrang = tinhTrang;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getHuongXuLy() {
        return huongXuLy;
    }

    public void setHuongXuLy(String huongXuLy) {
        this.huongXuLy = huongXuLy;
    }

    public String getMaSanPhamDoi() {
        return maSanPhamDoi;
    }

    public void setMaSanPhamDoi(String maSanPhamDoi) {
        this.maSanPhamDoi = maSanPhamDoi;
    }

    public String getTenSanPhamDoi() {
        return tenSanPhamDoi;
    }

    public void setTenSanPhamDoi(String tenSanPhamDoi) {
        this.tenSanPhamDoi = tenSanPhamDoi;
    }

    public BigDecimal getDonGiaDoi() {
        return donGiaDoi != null ? donGiaDoi : BigDecimal.ZERO;
    }

    public void setDonGiaDoi(BigDecimal donGiaDoi) {
        this.donGiaDoi = donGiaDoi != null ? donGiaDoi : BigDecimal.ZERO;
    }

    public int getSoLuongDoi() {
        return soLuongDoi;
    }

    public void setSoLuongDoi(int soLuongDoi) {
        this.soLuongDoi = soLuongDoi;
    }
}

