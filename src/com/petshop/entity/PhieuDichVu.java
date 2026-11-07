package com.petshop.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Thực thể phiếu tiếp nhận và theo dõi dịch vụ thú cưng tại quầy.
 * Chuẩn hóa OOP với Object Reference (KhachHang, NhanVien, LichHen),
 * kiểu nguyên thủy double, thuộc tính dẫn xuất tinhTongTien() (IUH Directive).
 */
public class PhieuDichVu {

    private String maPhieuDichVu;
    private KhachHang khachHang;
    private NhanVien nhanVien;
    private LichHen lichHen;
    private LocalDate ngayTiepNhan;
    private LocalTime gioTiepNhan;
    private String trangThai; // "Đang chờ", "Đang thực hiện", "Hoàn thành", "Đã hủy"
    private String ghiChu;

    // Biến phụ lưu trữ mã tạm khi chưa kịp liên kết đối tượng
    private String maKhachHangTam;
    private String maNhanVienTam;
    private String maLichHenTam;
    private String tenKhachHangTam;
    private String soDienThoaiTam;
    private String tenNhanVienTam;

    private List<ChiTietPhieuDichVu> danhSachChiTiet;

    public PhieuDichVu() {
        this.trangThai = "Đang chờ";
        this.ngayTiepNhan = LocalDate.now();
        this.gioTiepNhan = LocalTime.now();
        this.danhSachChiTiet = new ArrayList<>();
    }

    public PhieuDichVu(String maPhieuDichVu, String maKhachHang, String maNhanVien, String maLichHen,
                       LocalDate ngayTiepNhan, LocalTime gioTiepNhan, String trangThai, String ghiChu) {
        this.maPhieuDichVu = maPhieuDichVu;
        this.maKhachHangTam = maKhachHang;
        this.maNhanVienTam = maNhanVien;
        this.maLichHenTam = maLichHen;
        this.ngayTiepNhan = ngayTiepNhan != null ? ngayTiepNhan : LocalDate.now();
        this.gioTiepNhan = gioTiepNhan != null ? gioTiepNhan : LocalTime.now();
        this.trangThai = trangThai != null ? trangThai : "Đang chờ";
        this.ghiChu = ghiChu;
        this.danhSachChiTiet = new ArrayList<>();
    }

    public PhieuDichVu(String maPhieuDichVu, KhachHang khachHang, NhanVien nhanVien, LichHen lichHen,
                       LocalDate ngayTiepNhan, LocalTime gioTiepNhan, String trangThai, String ghiChu) {
        this.maPhieuDichVu = maPhieuDichVu;
        this.khachHang = khachHang;
        this.nhanVien = nhanVien;
        this.lichHen = lichHen;
        this.ngayTiepNhan = ngayTiepNhan != null ? ngayTiepNhan : LocalDate.now();
        this.gioTiepNhan = gioTiepNhan != null ? gioTiepNhan : LocalTime.now();
        this.trangThai = trangThai != null ? trangThai : "Đang chờ";
        this.ghiChu = ghiChu;
        this.danhSachChiTiet = new ArrayList<>();
    }

    // =========================================================================
    // GETTERS & SETTERS (OOP & DELEGATE GETTERS)
    // =========================================================================

    public String getMaPhieuDichVu() {
        return maPhieuDichVu;
    }

    public void setMaPhieuDichVu(String maPhieuDichVu) {
        this.maPhieuDichVu = maPhieuDichVu;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
        if (khachHang != null) {
            this.maKhachHangTam = khachHang.getMaKhachHang();
            this.tenKhachHangTam = khachHang.getHoTen();
            this.soDienThoaiTam = khachHang.getSoDienThoai();
        }
    }

    public String getMaKhachHang() {
        return khachHang != null ? khachHang.getMaKhachHang() : maKhachHangTam;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHangTam = maKhachHang;
        if (this.khachHang == null) {
            this.khachHang = new KhachHang();
        }
        this.khachHang.setMaKhachHang(maKhachHang);
    }

    public String getTenKhachHang() {
        if (khachHang != null && khachHang.getHoTen() != null) {
            return khachHang.getHoTen();
        }
        return tenKhachHangTam != null ? tenKhachHangTam : "Khách vãng lai";
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHangTam = tenKhachHang;
        if (this.khachHang == null) {
            this.khachHang = new KhachHang();
        }
        this.khachHang.setHoTen(tenKhachHang);
    }

    public String getSoDienThoai() {
        if (khachHang != null && khachHang.getSoDienThoai() != null) {
            return khachHang.getSoDienThoai();
        }
        return soDienThoaiTam != null ? soDienThoaiTam : "";
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoaiTam = soDienThoai;
        if (this.khachHang == null) {
            this.khachHang = new KhachHang();
        }
        this.khachHang.setSoDienThoai(soDienThoai);
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
        if (nhanVien != null) {
            this.maNhanVienTam = nhanVien.getMaNhanVien();
            this.tenNhanVienTam = nhanVien.getHoTen();
        }
    }

    public String getMaNhanVien() {
        return nhanVien != null ? nhanVien.getMaNhanVien() : maNhanVienTam;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVienTam = maNhanVien;
        if (this.nhanVien == null) {
            this.nhanVien = new NhanVien();
        }
        this.nhanVien.setMaNhanVien(maNhanVien);
    }

    public String getTenNhanVien() {
        if (nhanVien != null && nhanVien.getHoTen() != null) {
            return nhanVien.getHoTen();
        }
        return tenNhanVienTam != null ? tenNhanVienTam : "";
    }

    public void setTenNhanVien(String tenNhanVien) {
        this.tenNhanVienTam = tenNhanVien;
        if (this.nhanVien == null) {
            this.nhanVien = new NhanVien();
        }
        this.nhanVien.setHoTen(tenNhanVien);
    }

    public LichHen getLichHen() {
        return lichHen;
    }

    public void setLichHen(LichHen lichHen) {
        this.lichHen = lichHen;
        if (lichHen != null) {
            this.maLichHenTam = lichHen.getMaLichHen();
        }
    }

    public String getMaLichHen() {
        return lichHen != null ? lichHen.getMaLichHen() : maLichHenTam;
    }

    public void setMaLichHen(String maLichHen) {
        this.maLichHenTam = maLichHen;
        if (this.lichHen == null && maLichHen != null) {
            this.lichHen = new LichHen();
            this.lichHen.setMaLichHen(maLichHen);
        }
    }

    public LocalDate getNgayTiepNhan() {
        return ngayTiepNhan;
    }

    public LocalDate getNgayLap() {
        return ngayTiepNhan;
    }

    public void setNgayTiepNhan(LocalDate ngayTiepNhan) {
        this.ngayTiepNhan = ngayTiepNhan;
    }

    public void setNgayLap(LocalDate ngayLap) {
        this.ngayTiepNhan = ngayLap;
    }

    public LocalTime getGioTiepNhan() {
        return gioTiepNhan;
    }

    public void setGioTiepNhan(LocalTime gioTiepNhan) {
        this.gioTiepNhan = gioTiepNhan;
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

    public List<ChiTietPhieuDichVu> getDanhSachChiTiet() {
        return danhSachChiTiet;
    }

    public void setDanhSachChiTiet(List<ChiTietPhieuDichVu> danhSachChiTiet) {
        this.danhSachChiTiet = danhSachChiTiet != null ? danhSachChiTiet : new ArrayList<>();
    }

    // =========================================================================
    // THUỘC TÍNH DẪN XUẤT (DERIVED ATTRIBUTES - ĐIỀU 2)
    // =========================================================================

    /**
     * Phương thức tự tính tổng tiền các dịch vụ trong phiếu.
     */
    public double tinhTongTien() {
        double tong = 0.0;
        if (danhSachChiTiet != null) {
            for (ChiTietPhieuDichVu ct : danhSachChiTiet) {
                tong += ct.getDonGia();
            }
        }
        return tong;
    }

    /**
     * Delegate getter cho TableView PropertyValueFactory nếu cần.
     */
    public double getTongTien() {
        return tinhTongTien();
    }

    /**
     * Chuỗi tóm tắt các dịch vụ thực hiện để hiển thị lên bảng.
     */
    public String getChuoiDichVu() {
        if (danhSachChiTiet == null || danhSachChiTiet.isEmpty()) {
            return "Chưa có dịch vụ";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < danhSachChiTiet.size(); i++) {
            ChiTietPhieuDichVu ct = danhSachChiTiet.get(i);
            String tenTc = ct.getTenThuCung() != null ? ct.getTenThuCung() : "";
            String tenDv = ct.getTenDichVu() != null ? ct.getTenDichVu() : ct.getMaDichVu();
            if (i > 0) sb.append(", ");
            sb.append(tenDv);
            if (!tenTc.isEmpty()) {
                sb.append(" (").append(tenTc).append(")");
            }
        }
        return sb.toString();
    }
}
