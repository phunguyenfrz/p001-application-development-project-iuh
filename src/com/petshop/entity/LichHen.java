package com.petshop.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Thực thể lịch hẹn dịch vụ chăm sóc thú cưng của khách hàng.
 * Chuẩn hóa OOP với Object Reference (KhachHang, ThuCung, NhanVien).
 */
public class LichHen {

    private String maLichHen;
    private KhachHang khachHang;
    private ThuCung thuCung;
    private NhanVien nhanVien;
    private LocalDate ngayHen;
    private LocalTime gioHen;
    private String trangThai; // "Đã đặt", "Đã xác nhận", "Đã tiếp nhận", "Đang thực hiện", "Hoàn thành", "Đã hủy"
    private String ghiChu;

    private List<ChiTietLichHen> danhSachChiTiet;

    public LichHen() {
        this.trangThai = "Đã đặt";
        this.danhSachChiTiet = new ArrayList<>();
    }

    public LichHen(String maLichHen, KhachHang khachHang, ThuCung thuCung, NhanVien nhanVien,
                   LocalDate ngayHen, LocalTime gioHen, String trangThai, String ghiChu) {
        this.maLichHen = maLichHen;
        this.khachHang = khachHang;
        this.thuCung = thuCung;
        this.nhanVien = nhanVien;
        this.ngayHen = ngayHen;
        this.gioHen = gioHen;
        this.trangThai = trangThai != null ? trangThai : "Đã đặt";
        this.ghiChu = ghiChu;
        this.danhSachChiTiet = new ArrayList<>();
    }

    // =========================================================================
    // GETTERS & SETTERS (OOP & DELEGATE GETTERS)
    // =========================================================================

    public String getMaLichHen() {
        return maLichHen;
    }

    public void setMaLichHen(String maLichHen) {
        this.maLichHen = maLichHen;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public String getMaKhachHang() {
        return khachHang != null ? khachHang.getMaKhachHang() : null;
    }

    public void setMaKhachHang(String maKhachHang) {
        if (maKhachHang == null) {
            if (this.khachHang != null) {
                this.khachHang.setMaKhachHang(null);
            }
            return;
        }
        if (this.khachHang == null) {
            this.khachHang = new KhachHang();
        }
        this.khachHang.setMaKhachHang(maKhachHang);
    }

    public String getTenKhachHang() {
        if (khachHang != null && khachHang.getHoTen() != null) {
            return khachHang.getHoTen();
        }
        return "Khách vãng lai";
    }

    public void setTenKhachHang(String tenKhachHang) {
        if (this.khachHang == null) {
            this.khachHang = new KhachHang();
        }
        this.khachHang.setHoTen(tenKhachHang);
    }

    public String getSoDienThoai() {
        if (khachHang != null && khachHang.getSoDienThoai() != null) {
            return khachHang.getSoDienThoai();
        }
        return "";
    }

    public void setSoDienThoai(String soDienThoai) {
        if (this.khachHang == null) {
            this.khachHang = new KhachHang();
        }
        this.khachHang.setSoDienThoai(soDienThoai);
    }

    public ThuCung getThuCung() {
        return thuCung;
    }

    public void setThuCung(ThuCung thuCung) {
        this.thuCung = thuCung;
    }

    public String getMaThuCung() {
        return thuCung != null ? thuCung.getMaThuCung() : null;
    }

    public void setMaThuCung(String maThuCung) {
        if (maThuCung == null) {
            if (this.thuCung != null) {
                this.thuCung.setMaThuCung(null);
            }
            return;
        }
        if (this.thuCung == null) {
            this.thuCung = new ThuCung();
        }
        this.thuCung.setMaThuCung(maThuCung);
    }

    public String getTenThuCung() {
        if (thuCung != null && thuCung.getTenThuCung() != null) {
            return thuCung.getTenThuCung();
        }
        return "";
    }

    public void setTenThuCung(String tenThuCung) {
        if (this.thuCung == null) {
            this.thuCung = new ThuCung();
        }
        this.thuCung.setTenThuCung(tenThuCung);
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public String getMaNhanVien() {
        return nhanVien != null ? nhanVien.getMaNhanVien() : null;
    }

    public void setMaNhanVien(String maNhanVien) {
        if (maNhanVien == null) {
            if (this.nhanVien != null) {
                this.nhanVien.setMaNhanVien(null);
            }
            return;
        }
        if (this.nhanVien == null) {
            this.nhanVien = new NhanVien();
        }
        this.nhanVien.setMaNhanVien(maNhanVien);
    }

    public String getTenNhanVien() {
        if (nhanVien != null && nhanVien.getHoTen() != null) {
            return nhanVien.getHoTen();
        }
        return "";
    }

    public void setTenNhanVien(String tenNhanVien) {
        if (this.nhanVien == null) {
            this.nhanVien = new NhanVien();
        }
        this.nhanVien.setHoTen(tenNhanVien);
    }

    public LocalDate getNgayHen() {
        return ngayHen;
    }

    public void setNgayHen(LocalDate ngayHen) {
        this.ngayHen = ngayHen;
    }

    public LocalTime getGioHen() {
        return gioHen;
    }

    public void setGioHen(LocalTime gioHen) {
        this.gioHen = gioHen;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public List<ChiTietLichHen> getDanhSachChiTiet() {
        return danhSachChiTiet;
    }

    public void setDanhSachChiTiet(List<ChiTietLichHen> danhSachChiTiet) {
        this.danhSachChiTiet = danhSachChiTiet != null ? danhSachChiTiet : new ArrayList<>();
    }

    /**
     * Chuỗi danh sách tên dịch vụ để hiển thị lên bảng TableView.
     */
    public String getChuoiDichVu() {
        if (danhSachChiTiet == null || danhSachChiTiet.isEmpty()) {
            return "Chưa có dịch vụ";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < danhSachChiTiet.size(); i++) {
            ChiTietLichHen ct = danhSachChiTiet.get(i);
            if (ct.getTenDichVu() != null && !ct.getTenDichVu().isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(ct.getTenDichVu());
            }
        }
        return sb.toString();
    }
}
