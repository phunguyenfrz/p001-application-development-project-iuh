package com.petshop.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Thực thể biểu diễn một lần hẹn chăm sóc dịch vụ của thú cưng (ánh xạ thông qua Object Reference LichHen).
 */
public class LichSuChamSoc {

    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm");

    private final LichHen lichHen;

    public LichSuChamSoc(LichHen lichHen) {
        this.lichHen = lichHen != null ? lichHen : new LichHen();
    }

    public LichSuChamSoc(String maLichHen, String maThuCung, String tenThuCung,
                         String maKhachHang, String tenKhachHang,
                         String maNhanVien, String tenNhanVien,
                         LocalDate ngayHen, String gioHen,
                         String trangThai, String ghiChu) {
        this.lichHen = new LichHen();
        this.lichHen.setMaLichHen(maLichHen);

        ThuCung tc = new ThuCung();
        tc.setMaThuCung(maThuCung);
        tc.setTenThuCung(tenThuCung);
        this.lichHen.setThuCung(tc);

        KhachHang kh = new KhachHang();
        kh.setMaKhachHang(maKhachHang);
        kh.setHoTen(tenKhachHang);
        this.lichHen.setKhachHang(kh);

        NhanVien nv = new NhanVien();
        nv.setMaNhanVien(maNhanVien);
        nv.setHoTen(tenNhanVien);
        this.lichHen.setNhanVien(nv);

        this.lichHen.setNgayHen(ngayHen);
        if (gioHen != null && !gioHen.isBlank() && !"-".equals(gioHen.trim())) {
            try {
                String chuanGio = gioHen.trim().length() >= 5 ? gioHen.trim().substring(0, 5) : gioHen.trim();
                this.lichHen.setGioHen(LocalTime.parse(chuanGio, DINH_DANG_GIO));
            } catch (Exception ignored) {
            }
        }
        this.lichHen.setTrangThai(trangThai != null ? trangThai : "Hoàn thành");
        this.lichHen.setGhiChu(ghiChu != null ? ghiChu : "");
    }

    public LichHen getLichHen() {
        return lichHen;
    }

    public String getMaLichHen() {
        return lichHen != null ? lichHen.getMaLichHen() : null;
    }

    public ThuCung getThuCung() {
        return lichHen != null ? lichHen.getThuCung() : null;
    }

    public String getMaThuCung() {
        return lichHen != null ? lichHen.getMaThuCung() : null;
    }

    public String getTenThuCung() {
        return lichHen != null ? lichHen.getTenThuCung() : null;
    }

    public KhachHang getKhachHang() {
        return lichHen != null ? lichHen.getKhachHang() : null;
    }

    public String getMaKhachHang() {
        return lichHen != null ? lichHen.getMaKhachHang() : null;
    }

    public String getTenKhachHang() {
        return lichHen != null ? lichHen.getTenKhachHang() : null;
    }

    public NhanVien getNhanVien() {
        return lichHen != null ? lichHen.getNhanVien() : null;
    }

    public String getMaNhanVien() {
        return lichHen != null ? lichHen.getMaNhanVien() : null;
    }

    public String getTenNhanVien() {
        return lichHen != null ? lichHen.getTenNhanVien() : null;
    }

    public LocalDate getNgayHen() {
        return lichHen != null ? lichHen.getNgayHen() : null;
    }

    public String getGioHen() {
        if (lichHen != null && lichHen.getGioHen() != null) {
            return lichHen.getGioHen().format(DINH_DANG_GIO);
        }
        return "-";
    }

    public String getTrangThai() {
        return lichHen != null ? lichHen.getTrangThai() : "Hoàn thành";
    }

    public String getGhiChu() {
        return (lichHen != null && lichHen.getGhiChu() != null) ? lichHen.getGhiChu() : "";
    }
}

