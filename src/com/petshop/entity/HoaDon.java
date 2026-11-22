package com.petshop.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Thực thể ánh xạ bảng HoaDon trong cơ sở dữ liệu SQL Server.
 * Đại diện cho hóa đơn thanh toán bán lẻ sản phẩm và dịch vụ của cửa hàng Happy Tails.
 * Tuân thủ Bộ chỉ thị kỹ thuật IUH:
 * - Điều 1: Tiền tệ dùng 100% kiểu double, không dùng BigDecimal.
 * - Điều 2: Khóa ngoại dùng Object Reference (KhachHang, NhanVien, KhuyenMai, PhieuDichVu, LichHen).
 *           Toàn bộ thuộc tính dẫn xuất (tongTien, tienGiam, tienThoiLai, diemTichLuyCong...) KHÔNG lưu biến thành viên,
 *           KHÔNG có setter/constructor nhận trực tiếp, 100% được tính qua phương thức.
 *           Giữ lại toàn bộ Delegate Getters để không làm vỡ TableView và các giao diện khác.
 */
public class HoaDon {

    private String maHoaDon;
    private LichHen lichHen;
    private PhieuDichVu phieuDichVu;
    private NhanVien nhanVien;
    private KhachHang khachHang;
    private KhuyenMai khuyenMai;

    private LocalDate ngayLap;
    private LocalTime gioLap;
    private String hinhThucThanhToan; // "Tiền mặt", "Chuyển khoản"
    private String trangThaiThanhToan; // "Chưa thanh toán", "Đã thanh toán", "Đã hủy"

    private int diemTichLuySuDung = 0; // Số điểm khách hàng dùng giảm trừ
    private String ghiChu;

    // Danh sách dòng chi tiết
    private List<ChiTietHoaDonSanPham> danhSachSanPham = new ArrayList<>();
    private List<ChiTietHoaDonDichVu> danhSachDichVu = new ArrayList<>();

    public HoaDon() {
        this.ngayLap = LocalDate.now();
        this.gioLap = LocalTime.now();
        this.hinhThucThanhToan = "Tiền mặt";
        this.trangThaiThanhToan = "Đã thanh toán";
    }

    public HoaDon(String maHoaDon, NhanVien nhanVien, KhachHang khachHang, LocalDate ngayLap, String hinhThucThanhToan) {
        this();
        this.maHoaDon = maHoaDon;
        this.setNhanVien(nhanVien);
        this.setKhachHang(khachHang);
        this.ngayLap = ngayLap != null ? ngayLap : LocalDate.now();
        this.hinhThucThanhToan = hinhThucThanhToan != null ? hinhThucThanhToan : "Tiền mặt";
    }

    /**
     * Phương thức tự tính toán: Tính tổng tiền gốc từ danh sách sản phẩm và dịch vụ (Điều 2).
     */
    public double tinhTongTienGoc() {
        double tong = 0.0;
        if (danhSachSanPham != null) {
            for (ChiTietHoaDonSanPham sp : danhSachSanPham) {
                tong += sp.tinhThanhTien();
            }
        }
        if (danhSachDichVu != null) {
            for (ChiTietHoaDonDichVu dv : danhSachDichVu) {
                tong += dv.tinhThanhTien();
            }
        }
        return tong;
    }

    /**
     * Phương thức tự tính toán: Tiền giảm giá từ chương trình khuyến mãi (Điều 2).
     */
    public double tinhTienGiamKhuyenMai() {
        if (khuyenMai == null) {
            return 0.0;
        }
        double tongGoc = tinhTongTienGoc();
        if (tongGoc <= 0.0) {
            return 0.0;
        }
        return Math.min(tongGoc, Math.round(khuyenMai.tinhTienGiam(tongGoc)));
    }

    /**
     * Phương thức tự tính toán: Tiền giảm từ điểm tích lũy (1 điểm = 1.000 VNĐ), tối đa bằng số tiền sau khuyến mãi.
     */
    public double tinhTienGiamTuDiem() {
        if (diemTichLuySuDung <= 0) return 0.0;
        double tienSauKm = Math.max(0.0, tinhTongTienGoc() - tinhTienGiamKhuyenMai());
        return Math.min(tienSauKm, diemTichLuySuDung * 1000.0);
    }

    /**
     * Phương thức tự tính toán: Tổng số tiền được giảm giá (Khuyến mãi + Điểm tích lũy).
     */
    public double tinhTongGiamGia() {
        return tinhTienGiamKhuyenMai() + tinhTienGiamTuDiem();
    }

    /**
     * Phương thức tự tính toán: Tính tổng tiền thực thanh toán sau khi trừ toàn bộ giảm giá (Điều 2).
     */
    public double tinhTongThanhToan() {
        double tongGoc = tinhTongTienGoc();
        double giamGia = tinhTongGiamGia();
        return Math.max(0.0, tongGoc - giamGia);
    }

    /**
     * Phương thức tự tính toán: Số điểm tích lũy mới được cộng từ hóa đơn (Điều 2).
     * - Khách vãng lai: 0 điểm
     * - Khách hàng có tài khoản:
     *   + Hóa đơn < 1.000.000 VNĐ: mỗi 100.000 VNĐ tính 5 điểm
     *   + Hóa đơn >= 1.000.000 VNĐ: mỗi 100.000 VNĐ tính 10 điểm
     */
    public int tinhDiemTichLuyCong() {
        String maKh = getMaKhachHang();
        if (maKh == null || maKh.trim().isEmpty()) {
            return 0;
        }
        long tien = (long) tinhTongThanhToan();
        if (tien <= 0L) {
            return 0;
        }
        long soBlock100k = tien / 100_000L;
        return (int) (tien >= 1_000_000L ? soBlock100k * 10L : soBlock100k * 5L);
    }

    /**
     * Phương thức tự tính toán tiền thối lại từ số tiền khách đưa (không lưu biến thành viên).
     */
    public double tinhTienThoiLai(double tienKhachDua) {
        return Math.max(0.0, tienKhachDua - tinhTongThanhToan());
    }

    /**
     * Phương thức tự tính toán số tiền khách cần thanh toán thêm khi cập nhật hóa đơn.
     */
    public double tinhTienThanhToanThem(HoaDon hoaDonCu) {
        if (hoaDonCu == null) return 0.0;
        return Math.max(0.0, this.tinhTongThanhToan() - hoaDonCu.tinhTongThanhToan());
    }

    /**
     * Phương thức tự tính toán số tiền hoàn lại cho khách khi cập nhật hóa đơn.
     */
    public double tinhTienHoanLai(HoaDon hoaDonCu) {
        if (hoaDonCu == null) return 0.0;
        return Math.max(0.0, hoaDonCu.tinhTongThanhToan() - this.tinhTongThanhToan());
    }

    /**
     * Mô tả chi tiết các khoản ưu đãi (khuyến mãi và điểm tích lũy) áp dụng trên hóa đơn.
     */
    public String getMoTaGiamGia() {
        List<String> ds = new ArrayList<>();
        double tienGiamKm = tinhTienGiamKhuyenMai();
        if (getMaKhuyenMai() != null && !getMaKhuyenMai().isBlank()) {
            if (tienGiamKm > 0.0) {
                ds.add(getMaKhuyenMai() + " (-" + String.format("%,.0f", tienGiamKm) + "đ)");
            } else {
                ds.add(getMaKhuyenMai());
            }
        }
        if (diemTichLuySuDung > 0) {
            double tienDiem = tinhTienGiamTuDiem();
            ds.add("Dùng " + diemTichLuySuDung + " điểm (-" + String.format("%,.0f", tienDiem) + "đ)");
        }
        if (ds.isEmpty()) {
            return "--";
        }
        return String.join(" + ", ds);
    }

    // ==========================================
    // GETTERS & SETTERS / DELEGATE GETTERS (IUH)
    // ==========================================

    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }

    public LichHen getLichHen() { return lichHen; }
    public void setLichHen(LichHen lichHen) { this.lichHen = lichHen; }

    public String getMaLichHen() {
        if (lichHen != null && lichHen.getMaLichHen() != null) {
            return lichHen.getMaLichHen();
        }
        if (phieuDichVu != null && phieuDichVu.getMaLichHen() != null) {
            return phieuDichVu.getMaLichHen();
        }
        return null;
    }

    public void setMaLichHen(String maLichHen) {
        if (maLichHen == null) {
            this.lichHen = null;
            return;
        }
        if (this.lichHen == null) {
            this.lichHen = new LichHen();
        }
        this.lichHen.setMaLichHen(maLichHen);
    }

    public PhieuDichVu getPhieuDichVu() { return phieuDichVu; }
    public void setPhieuDichVu(PhieuDichVu phieuDichVu) {
        this.phieuDichVu = phieuDichVu;
        if (phieuDichVu != null && phieuDichVu.getLichHen() != null) {
            this.lichHen = phieuDichVu.getLichHen();
        }
    }

    public String getMaPhieuDichVu() {
        return phieuDichVu != null ? phieuDichVu.getMaPhieuDichVu() : null;
    }

    public void setMaPhieuDichVu(String maPhieuDichVu) {
        if (maPhieuDichVu == null) {
            this.phieuDichVu = null;
            return;
        }
        if (this.phieuDichVu == null) {
            this.phieuDichVu = new PhieuDichVu();
        }
        this.phieuDichVu.setMaPhieuDichVu(maPhieuDichVu);
    }

    // Object Reference NhanVien & Delegate Getters
    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }

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
        return nhanVien != null ? nhanVien.getHoTen() : null;
    }

    public void setTenNhanVien(String tenNhanVien) {
        if (tenNhanVien == null) {
            if (this.nhanVien != null) {
                this.nhanVien.setHoTen(null);
            }
            return;
        }
        if (this.nhanVien == null) {
            this.nhanVien = new NhanVien();
        }
        this.nhanVien.setHoTen(tenNhanVien);
    }

    // Object Reference KhachHang & Delegate Getters
    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }

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
        if (tenKhachHang == null) {
            if (this.khachHang != null) {
                this.khachHang.setHoTen(null);
            }
            return;
        }
        if (this.khachHang == null) {
            this.khachHang = new KhachHang();
        }
        this.khachHang.setHoTen(tenKhachHang);
    }

    public String getSoDienThoaiKhachHang() {
        return khachHang != null ? khachHang.getSoDienThoai() : null;
    }

    public void setSoDienThoaiKhachHang(String soDienThoaiKhachHang) {
        if (soDienThoaiKhachHang == null) {
            if (this.khachHang != null) {
                this.khachHang.setSoDienThoai(null);
            }
            return;
        }
        if (this.khachHang == null) {
            this.khachHang = new KhachHang();
        }
        this.khachHang.setSoDienThoai(soDienThoaiKhachHang);
    }

    public LocalDate getNgayLap() { return ngayLap; }
    public void setNgayLap(LocalDate ngayLap) { this.ngayLap = ngayLap; }

    public LocalTime getGioLap() { return gioLap; }
    public void setGioLap(LocalTime gioLap) { this.gioLap = gioLap; }

    public String getHinhThucThanhToan() { return hinhThucThanhToan; }
    public void setHinhThucThanhToan(String hinhThucThanhToan) { this.hinhThucThanhToan = hinhThucThanhToan; }

    public String getTrangThaiThanhToan() { return trangThaiThanhToan; }
    public void setTrangThaiThanhToan(String trangThaiThanhToan) { this.trangThaiThanhToan = trangThaiThanhToan; }

    // Object Reference KhuyenMai & Delegate Getters
    public KhuyenMai getKhuyenMai() { return khuyenMai; }
    public void setKhuyenMai(KhuyenMai khuyenMai) { this.khuyenMai = khuyenMai; }

    public String getMaKhuyenMai() {
        return khuyenMai != null ? khuyenMai.getMaKhuyenMai() : null;
    }

    public void setMaKhuyenMai(String maKhuyenMai) {
        if (maKhuyenMai == null) {
            if (this.khuyenMai != null) {
                this.khuyenMai.setMaKhuyenMai(null);
            }
            return;
        }
        if (this.khuyenMai == null) {
            this.khuyenMai = new KhuyenMai();
        }
        this.khuyenMai.setMaKhuyenMai(maKhuyenMai);
    }

    public String getTenKhuyenMai() {
        return khuyenMai != null ? khuyenMai.getTenKhuyenMai() : null;
    }

    public void setTenKhuyenMai(String tenKhuyenMai) {
        if (tenKhuyenMai == null) {
            if (this.khuyenMai != null) {
                this.khuyenMai.setTenKhuyenMai(null);
            }
            return;
        }
        if (this.khuyenMai == null) {
            this.khuyenMai = new KhuyenMai();
        }
        this.khuyenMai.setTenKhuyenMai(tenKhuyenMai);
    }

    public double getTienGiam() {
        return tinhTienGiamKhuyenMai();
    }

    public double getTongTienGiam() {
        return tinhTongGiamGia();
    }

    public double getTongTienGoc() {
        return tinhTongTienGoc();
    }

    /**
     * Delegate getter lấy tổng tiền thực thanh toán dẫn xuất (Điều 2 IUH).
     * 100% tự tính toán từ chi tiết hóa đơn và giảm giá, không lưu trường dẫn xuất.
     */
    public double getTongTien() {
        return tinhTongThanhToan();
    }

    public int getDiemTichLuySuDung() { return diemTichLuySuDung; }
    public void setDiemTichLuySuDung(int diemTichLuySuDung) {
        this.diemTichLuySuDung = Math.max(0, diemTichLuySuDung);
    }

    public int getDiemTichLuyCong() {
        return tinhDiemTichLuyCong();
    }

    public double getTienGiamTuDiem() {
        return tinhTienGiamTuDiem();
    }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public List<ChiTietHoaDonSanPham> getDanhSachSanPham() { return danhSachSanPham; }
    public void setDanhSachSanPham(List<ChiTietHoaDonSanPham> danhSachSanPham) { this.danhSachSanPham = danhSachSanPham; }

    public List<ChiTietHoaDonDichVu> getDanhSachDichVu() { return danhSachDichVu; }
    public void setDanhSachDichVu(List<ChiTietHoaDonDichVu> danhSachDichVu) { this.danhSachDichVu = danhSachDichVu; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HoaDon hoaDon = (HoaDon) o;
        return Objects.equals(maHoaDon, hoaDon.maHoaDon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maHoaDon);
    }
}
