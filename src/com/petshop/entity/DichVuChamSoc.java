package com.petshop.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Thực thể Dịch vụ chăm sóc thú cưng, ánh xạ bảng DichVuChamSoc.
 * Quản lý danh sách bảng giá theo bậc kích thước thông qua Object Reference List<BangGiaDichVu>.
 */
public class DichVuChamSoc {

    private String maDichVu;
    private String tenDichVu;
    private String moTa;
    private int thoiLuong; // tính bằng phút
    private boolean trangThai; // true: đang kinh doanh, false: ngừng kinh doanh

    private List<BangGiaDichVu> danhSachBangGia = new ArrayList<>();

    public DichVuChamSoc() {
        this.thoiLuong = 30;
        this.trangThai = true;
    }

    public DichVuChamSoc(String maDichVu, String tenDichVu, String moTa, int thoiLuong, boolean trangThai) {
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.moTa = moTa;
        this.thoiLuong = thoiLuong;
        this.trangThai = trangThai;
    }

    public DichVuChamSoc(String maDichVu, String tenDichVu, String moTa, int thoiLuong, boolean trangThai,
                         double giaNho, double giaVua, double giaLon) {
        this(maDichVu, tenDichVu, moTa, thoiLuong, trangThai);
        setGiaNho(giaNho);
        setGiaVua(giaVua);
        setGiaLon(giaLon);
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(String maDichVu) {
        this.maDichVu = maDichVu;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public void setTenDichVu(String tenDichVu) {
        this.tenDichVu = tenDichVu;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public int getThoiLuong() {
        return thoiLuong;
    }

    public void setThoiLuong(int thoiLuong) {
        this.thoiLuong = thoiLuong;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }

    public List<BangGiaDichVu> getDanhSachBangGia() {
        return danhSachBangGia;
    }

    public void setDanhSachBangGia(List<BangGiaDichVu> danhSachBangGia) {
        this.danhSachBangGia = danhSachBangGia != null ? danhSachBangGia : new ArrayList<>();
    }

    public double tinhGiaTheoKichThuoc(String phanLoaiKichThuoc) {
        if (danhSachBangGia == null || phanLoaiKichThuoc == null) {
            return 0.0;
        }
        for (BangGiaDichVu bg : danhSachBangGia) {
            if (phanLoaiKichThuoc.equalsIgnoreCase(bg.getPhanLoaiKichThuoc())) {
                return bg.getGia();
            }
        }
        return 0.0;
    }

    public void capNhatGiaTheoKichThuoc(String phanLoaiKichThuoc, double gia) {
        if (phanLoaiKichThuoc == null) return;
        if (danhSachBangGia == null) {
            danhSachBangGia = new ArrayList<>();
        }
        for (BangGiaDichVu bg : danhSachBangGia) {
            if (phanLoaiKichThuoc.equalsIgnoreCase(bg.getPhanLoaiKichThuoc())) {
                bg.setGia(gia);
                return;
            }
        }
        BangGiaDichVu bgMoi = new BangGiaDichVu(null, this, gia, phanLoaiKichThuoc, null, null);
        danhSachBangGia.add(bgMoi);
    }

    public double getGiaNho() {
        return tinhGiaTheoKichThuoc("Nhỏ");
    }

    public void setGiaNho(double giaNho) {
        capNhatGiaTheoKichThuoc("Nhỏ", giaNho);
    }

    public double getGiaVua() {
        return tinhGiaTheoKichThuoc("Vừa");
    }

    public void setGiaVua(double giaVua) {
        capNhatGiaTheoKichThuoc("Vừa", giaVua);
    }

    public double getGiaLon() {
        return tinhGiaTheoKichThuoc("Lớn");
    }

    public void setGiaLon(double giaLon) {
        capNhatGiaTheoKichThuoc("Lớn", giaLon);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DichVuChamSoc that = (DichVuChamSoc) o;
        return Objects.equals(maDichVu, that.maDichVu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maDichVu);
    }
}

