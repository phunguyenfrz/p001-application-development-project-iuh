package com.petshop.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Thực thể Phiếu đổi trả (PhieuDoiTra) - Ánh xạ bảng dbo.PhieuDoiTra.
 * Chuẩn hóa OOP:
 * - Khóa ngoại sử dụng Object Reference: HoaDon hoaDon, NhanVien nhanVien, KhachHang khachHang.
 * - Giữ lại các Delegate Getter/Setter để tương thích TableView.
 * - Thuộc tính dẫn xuất (tienHoan, tienBu, tongTienHangTra, tongTienHangDoi) không lưu biến thành viên,
 *   không đưa vào constructor, không có setter; tự tính toán qua phương thức kiểu double.
 */
public class PhieuDoiTra {

    private String maPhieuDoiTra;
    private HoaDon hoaDon;
    private NhanVien nhanVien;
    private KhachHang khachHang;
    private LocalDate ngayLap;
    private String lyDo;
    private int soLuong;
    private String loaiXuLy;       // "Hoàn tiền" | "Đổi hàng"
    private String hinhThucHoan;   // "Tiền mặt" | "Chuyển khoản" | "Voucher"
    private String trangThai;      // "Chờ xử lý" | "Đã duyệt" | "Từ chối"

    private List<ChiTietPhieuDoiTra> danhSachSanPham = new ArrayList<>();

    public PhieuDoiTra() {
        this.ngayLap = LocalDate.now();
        this.soLuong = 1;
        this.loaiXuLy = "Hoàn tiền";
        this.trangThai = "Chờ xử lý";
    }

    public PhieuDoiTra(String maPhieuDoiTra, HoaDon hoaDon, NhanVien nhanVien, KhachHang khachHang,
                       LocalDate ngayLap, String lyDo, int soLuong, String loaiXuLy, String trangThai) {
        this.maPhieuDoiTra = maPhieuDoiTra;
        this.hoaDon = hoaDon;
        this.nhanVien = nhanVien;
        this.khachHang = khachHang;
        this.ngayLap = ngayLap != null ? ngayLap : LocalDate.now();
        this.lyDo = lyDo;
        this.soLuong = soLuong;
        this.loaiXuLy = loaiXuLy != null ? loaiXuLy : "Hoàn tiền";
        this.trangThai = trangThai != null ? trangThai : "Chờ xử lý";
    }

    // =========================================================================
    // CÁC PHƯƠNG THỨC TÍNH TOÁN DẪN XUẤT (KHÔNG LƯU BIẾN THÀNH VIÊN, KHÔNG SETTER)
    // =========================================================================

    /**
     * Tính tổng giá trị các mặt hàng khách trả lại dựa trên danh sách chi tiết.
     */
    public double tinhTongTienHangTra() {
        double tong = 0.0;
        if (danhSachSanPham != null) {
            for (ChiTietPhieuDoiTra ct : danhSachSanPham) {
                tong += ct.tinhThanhTienTra();
            }
        }
        return tong;
    }

    /**
     * Tính tổng giá trị các mặt hàng khách đổi lấy mới.
     */
    public double tinhTongTienHangDoi() {
        double tong = 0.0;
        if (danhSachSanPham != null) {
            for (ChiTietPhieuDoiTra ct : danhSachSanPham) {
                tong += ct.tinhThanhTienDoi();
            }
        }
        return tong;
    }

    /**
     * Tự động tính số tiền hoàn lại cho khách (double).
     * - Nếu là Hoàn tiền: hoàn 100% tổng tiền hàng trả.
     * - Nếu là Đổi hàng và tổng tiền hàng trả > tổng tiền hàng đổi: hoàn phần chênh lệch.
     */
    public double tinhTienHoan() {
        double tongTra = tinhTongTienHangTra();
        if (laDoiHang()) {
            double tongDoi = tinhTongTienHangDoi();
            if (tongDoi > 0 && tongTra > tongDoi) {
                return tongTra - tongDoi;
            }
            return 0.0;
        }
        return tongTra;
    }

    /**
     * Tự động tính số tiền khách phải bù thêm (double).
     * - Chỉ phát sinh khi Đổi hàng và tổng tiền hàng đổi > tổng tiền hàng trả.
     */
    public double tinhTienBu() {
        if (laDoiHang()) {
            double tongTra = tinhTongTienHangTra();
            double tongDoi = tinhTongTienHangDoi();
            if (tongDoi > tongTra) {
                return tongDoi - tongTra;
            }
        }
        return 0.0;
    }

    private boolean laDoiHang() {
        if (loaiXuLy == null) return false;
        String lower = loaiXuLy.trim().toLowerCase();
        return lower.contains("đổi") || lower.contains("doi");
    }

    // Delegate Getters cho TableView
    public double getTienHoan() {
        return tinhTienHoan();
    }

    public double getTienBu() {
        return tinhTienBu();
    }

    public void themChiTiet(ChiTietPhieuDoiTra chiTiet) {
        if (this.danhSachSanPham == null) {
            this.danhSachSanPham = new ArrayList<>();
        }
        this.danhSachSanPham.add(chiTiet);
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
            this.nhanVien = null;
            return;
        }
        if (this.nhanVien == null) {
            this.nhanVien = new NhanVien();
        }
        this.nhanVien.setMaNhanVien(maNhanVien);
    }

    public String getTenNhanVien() {
        return nhanVien != null ? nhanVien.getHoTen() : null;
    }

    public void setTenNhanVien(String tenNhanVien) {
        if (this.nhanVien == null) {
            this.nhanVien = new NhanVien();
        }
        this.nhanVien.setHoTen(tenNhanVien);
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
        return khachHang != null ? khachHang.getHoTen() : null;
    }

    public void setTenKhachHang(String tenKhachHang) {
        if (this.khachHang == null) {
            this.khachHang = new KhachHang();
        }
        this.khachHang.setHoTen(tenKhachHang);
    }

    public String getSoDienThoaiKhachHang() {
        return khachHang != null ? khachHang.getSoDienThoai() : null;
    }

    public void setSoDienThoaiKhachHang(String soDienThoaiKhachHang) {
        if (this.khachHang == null) {
            this.khachHang = new KhachHang();
        }
        this.khachHang.setSoDienThoai(soDienThoaiKhachHang);
    }

    public LocalDate getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(LocalDate ngayLap) {
        this.ngayLap = ngayLap;
    }

    public String getLyDo() {
        if (lyDo != null && !lyDo.trim().isEmpty()) {
            return lyDo;
        }
        if (danhSachSanPham != null && !danhSachSanPham.isEmpty()) {
            String lyDoDau = danhSachSanPham.get(0).getLyDo();
            if (lyDoDau != null && !lyDoDau.trim().isEmpty()) {
                return lyDoDau;
            }
        }
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public String getLoaiXuLy() {
        return loaiXuLy;
    }

    public void setLoaiXuLy(String loaiXuLy) {
        this.loaiXuLy = loaiXuLy;
    }

    public String getHinhThucHoan() {
        return hinhThucHoan;
    }

    public void setHinhThucHoan(String hinhThucHoan) {
        this.hinhThucHoan = hinhThucHoan;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public List<ChiTietPhieuDoiTra> getDanhSachSanPham() {
        return danhSachSanPham;
    }

    public void setDanhSachSanPham(List<ChiTietPhieuDoiTra> danhSachSanPham) {
        this.danhSachSanPham = danhSachSanPham != null ? danhSachSanPham : new ArrayList<>();
    }
}
