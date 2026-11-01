package com.petshop.model;

import java.math.BigDecimal;

/**
 * Lớp đại diện cho một dòng hiển thị trên bảng chi tiết hóa đơn (TableView),
 * hỗ trợ gộp linh hoạt cả Sản phẩm và Dịch vụ theo phong cách bán lẻ POS.
 */
public class DongHoaDon {

    public enum LoaiDong {
        SAN_PHAM("Sản phẩm"),
        DICH_VU("Dịch vụ");

        private final String nhan;
        LoaiDong(String nhan) { this.nhan = nhan; }
        public String getNhan() { return nhan; }
    }

    private int stt;
    private LoaiDong loai;
    private String maHang;
    private String tenHang;
    private String donViTinh;
    private int soLuong;
    private BigDecimal donGia;
    private String ghiChu;
    private String maThuCung;
    private String tenThuCung;

    public DongHoaDon() {
        this.soLuong = 1;
        this.donGia = BigDecimal.ZERO;
        this.loai = LoaiDong.SAN_PHAM;
    }

    public DongHoaDon(int stt, LoaiDong loai, String maHang, String tenHang, String donViTinh, int soLuong, BigDecimal donGia, String ghiChu) {
        this.stt = stt;
        this.loai = loai;
        this.maHang = maHang;
        this.tenHang = tenHang;
        this.donViTinh = donViTinh;
        this.soLuong = soLuong;
        this.donGia = donGia != null ? donGia : BigDecimal.ZERO;
        this.ghiChu = ghiChu;
    }

    public static DongHoaDon tuSanPham(int stt, SanPham sp, int soLuong) {
        return new DongHoaDon(
                stt,
                LoaiDong.SAN_PHAM,
                sp.getMaSanPham(),
                sp.getTenSanPham(),
                sp.getDonViTinh() != null ? sp.getDonViTinh() : "Cái",
                soLuong,
                sp.getGiaBan(),
                ""
        );
    }

    public static DongHoaDon tuDichVu(int stt, String maDichVu, String tenDichVu, String maThuCung, String tenThuCung, BigDecimal donGia) {
        DongHoaDon dong = new DongHoaDon(
                stt,
                LoaiDong.DICH_VU,
                maDichVu,
                tenDichVu,
                "Lần",
                1,
                donGia,
                tenThuCung != null && !tenThuCung.isBlank() ? "Thú cưng: " + tenThuCung : ""
        );
        dong.setMaThuCung(maThuCung);
        dong.setTenThuCung(tenThuCung);
        return dong;
    }

    public BigDecimal getThanhTien() {
        if (donGia == null) return BigDecimal.ZERO;
        return donGia.multiply(BigDecimal.valueOf(soLuong));
    }

    public String getTenLoai() {
        return loai != null ? loai.getNhan() : "";
    }

    // Getters and Setters
    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public LoaiDong getLoai() { return loai; }
    public void setLoai(LoaiDong loai) { this.loai = loai; }

    public String getMaHang() { return maHang; }
    public void setMaHang(String maHang) { this.maHang = maHang; }

    public String getTenHang() { return tenHang; }
    public void setTenHang(String tenHang) { this.tenHang = tenHang; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getMaThuCung() { return maThuCung; }
    public void setMaThuCung(String maThuCung) { this.maThuCung = maThuCung; }

    public String getTenThuCung() { return tenThuCung; }
    public void setTenThuCung(String tenThuCung) { this.tenThuCung = tenThuCung; }
}

