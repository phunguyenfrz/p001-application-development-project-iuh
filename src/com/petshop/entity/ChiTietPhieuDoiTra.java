package com.petshop.entity;

/**
 * Thực thể Chi tiết phiếu đổi trả (ChiTietPhieuDoiTra) - Ánh xạ bảng dbo.ChiTietPhieuDoiTra.
 * Chuẩn hóa OOP:
 * - Khóa ngoại sử dụng Object Reference: HoaDon hoaDon, SanPham sanPham, SanPham sanPhamDoi.
 * - Tiền tệ sử dụng kiểu nguyên thủy double (donGia, donGiaDoi).
 * - Thuộc tính dẫn xuất thành tiền tự tính qua phương thức tinhThanhTienTra(), tinhThanhTienDoi().
 */
public class ChiTietPhieuDoiTra {

    private String maPhieuDoiTra;
    private HoaDon hoaDon;
    private SanPham sanPham;
    private int soLuong;
    private double donGia;

    // Các thuộc tính nghiệp vụ kiểm tra hàng hoàn trả
    private String lyDo;           // "Lỗi sản phẩm", "Hết hạn", "Không phù hợp", "Khác"
    private String tinhTrang;      // "Nguyên vẹn", "Đã mở", "Hư hỏng"
    private String huongXuLy;      // "Thu lại vào kho", "Nhập lại kho bán", "Hủy bỏ", "Trả nhà cung cấp", "Tiêu hủy"
    private String ghiChu;

    // Thông tin mặt hàng đổi mới (khi chọn hình thức Đổi sang sản phẩm khác)
    private SanPham sanPhamDoi;
    private int soLuongDoi;
    private double donGiaDoi;

    public ChiTietPhieuDoiTra() {
        this.soLuong = 1;
        this.donGia = 0.0;
        this.tinhTrang = "Nguyên vẹn";
        this.soLuongDoi = 0;
        this.donGiaDoi = 0.0;
    }

    public ChiTietPhieuDoiTra(String maPhieuDoiTra, String maHoaDon, String maSanPham, String tenSanPham,
                              int soLuong, double donGia, String lyDo, String tinhTrang,
                              String huongXuLy, String ghiChu) {
        this();
        this.maPhieuDoiTra = maPhieuDoiTra;
        setMaHoaDon(maHoaDon);
        setMaSanPham(maSanPham);
        setTenSanPham(tenSanPham);
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.lyDo = lyDo;
        this.tinhTrang = tinhTrang != null ? tinhTrang : "Nguyên vẹn";
        this.huongXuLy = huongXuLy != null ? huongXuLy : tuDongGoiYHuongXuLy(lyDo, this.tinhTrang);
        this.ghiChu = ghiChu;
    }



    /**
     * Tính thành tiền của dòng sản phẩm khách trả lại (double).
     */
    public double tinhThanhTienTra() {
        if (soLuong <= 0 || donGia <= 0) return 0.0;
        return donGia * soLuong;
    }

    /**
     * Tính thành tiền của dòng sản phẩm khách đổi mới (double).
     */
    public double tinhThanhTienDoi() {
        if (soLuongDoi <= 0 || donGiaDoi <= 0) return 0.0;
        return donGiaDoi * soLuongDoi;
    }

    /**
     * Tự động gợi ý hướng xử lý hàng nhận lại theo quy tắc QL26:
     * - Hàng Nguyên vẹn + Lý do Không phù hợp/Khác -> "Nhập lại kho bán"
     * - Hàng Lỗi sản phẩm -> "Trả nhà cung cấp"
     * - Hàng Hết hạn hoặc Hư hỏng -> "Tiêu hủy"
     */
    public static String tuDongGoiYHuongXuLy(String lyDo, String tinhTrang) {
        if ("Lỗi sản phẩm".equalsIgnoreCase(lyDo)) {
            return "Trả nhà cung cấp";
        }
        if ("Hết hạn".equalsIgnoreCase(lyDo) || "Hư hỏng".equalsIgnoreCase(tinhTrang)) {
            return "Tiêu hủy";
        }
        if ("Nguyên vẹn".equalsIgnoreCase(tinhTrang)) {
            return "Nhập lại kho bán";
        }
        return "Tiêu hủy";
    }

    // =========================================================================
    // OBJECT REFERENCES & DELEGATE GETTERS / SETTERS
    // =========================================================================

    public String getMaPhieuDoiTra() {
        return maPhieuDoiTra;
    }

    public void setMaPhieuDoiTra(String maPhieuDoiTra) {
        this.maPhieuDoiTra = maPhieuDoiTra;
    }

    public HoaDon getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
    }

    public String getMaHoaDon() {
        return hoaDon != null ? hoaDon.getMaHoaDon() : null;
    }

    public void setMaHoaDon(String maHoaDon) {
        if (maHoaDon == null) {
            this.hoaDon = null;
            return;
        }
        if (this.hoaDon == null) {
            this.hoaDon = new HoaDon();
        }
        this.hoaDon.setMaHoaDon(maHoaDon);
    }

    public SanPham getSanPham() {
        return sanPham;
    }

    public void setSanPham(SanPham sanPham) {
        this.sanPham = sanPham;
    }

    public String getMaSanPham() {
        return sanPham != null ? sanPham.getMaSanPham() : null;
    }

    public void setMaSanPham(String maSanPham) {
        if (maSanPham == null) {
            this.sanPham = null;
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
        this.soLuong = soLuong;
    }

    public double getDonGia() {
        return donGia;
    }

    public void setDonGia(double donGia) {
        this.donGia = donGia;
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

    public String getHuongXuLy() {
        return huongXuLy;
    }

    public void setHuongXuLy(String huongXuLy) {
        this.huongXuLy = huongXuLy;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public SanPham getSanPhamDoi() {
        return sanPhamDoi;
    }

    public void setSanPhamDoi(SanPham sanPhamDoi) {
        this.sanPhamDoi = sanPhamDoi;
    }

    public String getMaSanPhamDoi() {
        return sanPhamDoi != null ? sanPhamDoi.getMaSanPham() : null;
    }

    public void setMaSanPhamDoi(String maSanPhamDoi) {
        if (maSanPhamDoi == null || maSanPhamDoi.isBlank()) {
            this.sanPhamDoi = null;
            return;
        }
        if (this.sanPhamDoi == null) {
            this.sanPhamDoi = new SanPham();
        }
        this.sanPhamDoi.setMaSanPham(maSanPhamDoi);
    }

    public String getTenSanPhamDoi() {
        return sanPhamDoi != null ? sanPhamDoi.getTenSanPham() : null;
    }

    public void setTenSanPhamDoi(String tenSanPhamDoi) {
        if (tenSanPhamDoi == null) return;
        if (this.sanPhamDoi == null) {
            this.sanPhamDoi = new SanPham();
        }
        this.sanPhamDoi.setTenSanPham(tenSanPhamDoi);
    }

    public int getSoLuongDoi() {
        return soLuongDoi;
    }

    public void setSoLuongDoi(int soLuongDoi) {
        this.soLuongDoi = soLuongDoi;
    }

    public double getDonGiaDoi() {
        return donGiaDoi;
    }

    public void setDonGiaDoi(double donGiaDoi) {
        this.donGiaDoi = donGiaDoi;
    }
}
