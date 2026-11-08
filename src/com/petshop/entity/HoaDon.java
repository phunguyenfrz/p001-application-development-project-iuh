package com.petshop.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Thực thể ánh xạ bảng HoaDon trong cơ sở dữ liệu SQL Server.
 * Đại diện cho hóa đơn thanh toán bán lẻ sản phẩm và dịch vụ của cửa hàng Happy Tails.
 * Tuân thủ Bộ chỉ thị kỹ thuật IUH:
 * - Điều 1: Tiền tệ dùng 100% kiểu double, không dùng BigDecimal.
 * - Điều 2: Khóa ngoại dùng Object Reference (KhachHang, NhanVien, KhuyenMai, PhieuDichVu).
 *           Thuộc tính dẫn xuất tongTien, tienGiam được tính qua phương thức tinhTongThanhToan(),
 *           không có setter/constructor nhận trực tiếp.
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
    private double tienGiam; // Tiền giảm trực tiếp từ khuyến mãi

    private int diemTichLuySuDung = 0; // Số điểm dùng giảm trừ
    private int diemTichLuyCong = 0;   // Số điểm mới tích lũy được
    private double tienKhachDua = 0.0;
    private double tienThoiLai = 0.0;
    private double tienThanhToanThem = 0.0;
    private double tienHoanLai = 0.0;
    private String ghiChu;

    // Danh sách dòng chi tiết
    private List<ChiTietHoaDonSanPham> danhSachSanPham = new ArrayList<>();
    private List<ChiTietHoaDonDichVu> danhSachDichVu = new ArrayList<>();

    public HoaDon() {
        this.ngayLap = LocalDate.now();
        this.gioLap = LocalTime.now();
        this.hinhThucThanhToan = "Tiền mặt";
        this.trangThaiThanhToan = "Đã thanh toán";
        this.tienGiam = 0.0;
    }

    public HoaDon(String maHoaDon, NhanVien nhanVien, KhachHang khachHang, LocalDate ngayLap, String hinhThucThanhToan) {
        this();
        this.maHoaDon = maHoaDon;
        this.setNhanVien(nhanVien);
        this.setKhachHang(khachHang);
        this.ngayLap = ngayLap != null ? ngayLap : LocalDate.now();
        this.hinhThucThanhToan = hinhThucThanhToan != null ? hinhThucThanhToan : "Tiền mặt";
    }

    // Constructor tương thích các tầng cũ (đã xóa tongTien, tienGiam khỏi tham số theo Điều 2)
    public HoaDon(String maHoaDon, String maLichHen, String maPhieuDichVu, String maNhanVien,
                   String maKhachHang, LocalDate ngayLap, String hinhThucThanhToan,
                   String trangThaiThanhToan, String maKhuyenMai) {
        this();
        this.maHoaDon = maHoaDon;
        setMaLichHen(maLichHen);
        setMaPhieuDichVu(maPhieuDichVu);
        setMaNhanVien(maNhanVien);
        setMaKhachHang(maKhachHang);
        this.ngayLap = ngayLap != null ? ngayLap : LocalDate.now();
        this.hinhThucThanhToan = hinhThucThanhToan;
        this.trangThaiThanhToan = trangThaiThanhToan != null ? trangThaiThanhToan : "Đã thanh toán";
        setMaKhuyenMai(maKhuyenMai);
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
     * Phương thức tự tính toán: Tiền giảm từ điểm tích lũy (1 điểm = 1.000 VNĐ).
     */
    public double tinhTienGiamTuDiem() {
        if (diemTichLuySuDung <= 0) return 0.0;
        return diemTichLuySuDung * 1000.0;
    }

    /**
     * Phương thức tự tính toán: Tổng số tiền được giảm giá (Khuyến mãi + Điểm tích lũy).
     */
    public double tinhTongGiamGia() {
        return this.tienGiam + tinhTienGiamTuDiem();
    }

    /**
     * Phương thức tự tính toán: Tính tổng tiền thực thanh toán sau khi trừ toàn bộ giảm giá (Điều 2).
     */
    public double tinhTongThanhToan() {
        double tongGoc = tinhTongTienGoc();
        double giamGia = tinhTongGiamGia();
        return Math.max(0.0, tongGoc - giamGia);
    }

    public int getTongSoLuongMatHang() {
        int tong = 0;
        if (danhSachSanPham != null) {
            for (ChiTietHoaDonSanPham sp : danhSachSanPham) tong += sp.getSoLuong();
        }
        if (danhSachDichVu != null) {
            tong += danhSachDichVu.size();
        }
        return tong;
    }

    /**
     * Mô tả chi tiết các khoản ưu đãi (khuyến mãi và điểm tích lũy) áp dụng trên hóa đơn.
     */
    public String getMoTaGiamGia() {
        List<String> ds = new ArrayList<>();
        if (getMaKhuyenMai() != null && !getMaKhuyenMai().isBlank()) {
            if (tienGiam > 0.0) {
                ds.add(getMaKhuyenMai() + " (-" + String.format("%,.0f", tienGiam) + "đ)");
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

    public double getTienGiam() { return tienGiam; }
    public void setTienGiam(double tienGiam) { this.tienGiam = Math.max(0.0, tienGiam); }

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

    public int getDiemTichLuyCong() { return diemTichLuyCong; }
    public void setDiemTichLuyCong(int diemTichLuyCong) { this.diemTichLuyCong = Math.max(0, diemTichLuyCong); }

    public double getTienGiamTuDiem() {
        return tinhTienGiamTuDiem();
    }

    public void setTienGiamTuDiem(double tienGiamTuDiem) {
        if (this.diemTichLuySuDung <= 0 && tienGiamTuDiem > 0.0) {
            this.diemTichLuySuDung = (int) Math.round(tienGiamTuDiem / 1000.0);
        }
    }

    public double getTienKhachDua() { return tienKhachDua; }
    public void setTienKhachDua(double tienKhachDua) { this.tienKhachDua = tienKhachDua; }

    public double getTienThoiLai() { return tienThoiLai; }
    public void setTienThoiLai(double tienThoiLai) { this.tienThoiLai = tienThoiLai; }

    public double getTienThanhToanThem() { return tienThanhToanThem; }
    public void setTienThanhToanThem(double tienThanhToanThem) { this.tienThanhToanThem = tienThanhToanThem; }

    public double getTienHoanLai() { return tienHoanLai; }
    public void setTienHoanLai(double tienHoanLai) { this.tienHoanLai = tienHoanLai; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public List<ChiTietHoaDonSanPham> getDanhSachSanPham() { return danhSachSanPham; }
    public void setDanhSachSanPham(List<ChiTietHoaDonSanPham> danhSachSanPham) { this.danhSachSanPham = danhSachSanPham; }

    public List<ChiTietHoaDonDichVu> getDanhSachDichVu() { return danhSachDichVu; }
    public void setDanhSachDichVu(List<ChiTietHoaDonDichVu> danhSachDichVu) { this.danhSachDichVu = danhSachDichVu; }
}
