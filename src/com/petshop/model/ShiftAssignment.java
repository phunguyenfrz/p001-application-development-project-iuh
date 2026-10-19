package com.petshop.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Lớp thực thể ShiftAssignment đại diện cho một bản ghi phân công ca làm việc
 * cho nhân viên (bảng PhanCa trong CSDL).
 *
 * Tuân thủ quy tắc lập trình (Review 3_OOP và Review 4_Coding Convention):
 * - Tên lớp là danh từ (ShiftAssignment), viết theo chuẩn PascalCase.
 * - Tên phương thức là động từ (lay..., kiemTra...), viết theo chuẩn camelCase.
 * - Hằng số viết hoa theo chuẩn UPPER_SNAKE_CASE.
 * - Đầy đủ chú thích Javadoc và chú thích một dòng.
 */
public class ShiftAssignment {

    private String maPhanCa;         // PC-YYYYMMDD-NNNNNN
    private String maNhanVien;        // NV-YYYYMMDD-NNNNNN
    private String tenNhanVien;       // Tên hiển thị của nhân viên
    private String maCa;              // CA-YYYYMMDD-NNNNNN
    private String tenCa;             // Tên ca trực (Ca sáng, Ca chiều...)
    private LocalTime gioBatDau;      // Giờ bắt đầu ca
    private LocalTime gioKetThuc;     // Giờ kết thúc ca
    private LocalDate ngayLam;        // Ngày làm việc được phân công
    private String trangThai;         // Đã phân công, Đã hủy

    public static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Khởi tạo mặc định.
     */
    public ShiftAssignment() {
        this.trangThai = "Đã phân công";
        this.ngayLam = LocalDate.now();
    }

    /**
     * Khởi tạo đầy đủ các trường của phân công ca trực.
     *
     * @param maPhanCa    Mã định danh phân ca (PC-...)
     * @param maNhanVien  Mã nhân viên
     * @param tenNhanVien Họ tên nhân viên
     * @param maCa        Mã ca trực
     * @param tenCa       Tên ca trực
     * @param gioBatDau   Giờ bắt đầu làm việc
     * @param gioKetThuc  Giờ kết thúc ca
     * @param ngayLam     Ngày làm việc
     * @param trangThai   Trạng thái phân công
     */
    public ShiftAssignment(String maPhanCa, String maNhanVien, String tenNhanVien,
                           String maCa, String tenCa, LocalTime gioBatDau, LocalTime gioKetThuc,
                           LocalDate ngayLam, String trangThai) {
        this.maPhanCa = maPhanCa;
        this.maNhanVien = maNhanVien;
        this.tenNhanVien = tenNhanVien;
        this.maCa = maCa;
        this.tenCa = tenCa;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
        this.ngayLam = (ngayLam != null) ? ngayLam : LocalDate.now();
        this.trangThai = (trangThai != null) ? trangThai : "Đã phân công";
    }

    /**
     * Lấy chuỗi hiển thị tên ca và khung giờ (dạng 1 dòng ngăn cách bằng dấu chấm giữa).
     * Ví dụ: "Ca sáng · 08:00–16:00"
     *
     * @return Chuỗi mô tả ca và giờ
     */
    public String layCaVaGioHienThiMotDong() {
        String gioBatDauStr = (gioBatDau != null) ? gioBatDau.format(DINH_DANG_GIO) : "";
        String gioKetThucStr = (gioKetThuc != null) ? gioKetThuc.format(DINH_DANG_GIO) : "";
        return (tenCa != null ? tenCa : "") + " · " + gioBatDauStr + "–" + gioKetThucStr;
    }

    /**
     * Lấy chuỗi hiển thị ngày làm việc theo chuẩn dd/MM/yyyy.
     *
     * @return Chuỗi ngày dạng 19/10/2026
     */
    public String layNgayLamHienThi() {
        return (ngayLam != null) ? ngayLam.format(DINH_DANG_NGAY) : "";
    }

    /**
     * Xác định thứ trong tuần bằng tiếng Việt (Thứ Hai, Thứ Ba,... Chủ Nhật).
     *
     * @return Tên thứ trong tuần
     */
    public String layThuTrongTuan() {
        if (ngayLam == null) return "";
        DayOfWeek dow = ngayLam.getDayOfWeek();
        return switch (dow) {
            case MONDAY -> "Thứ Hai";
            case TUESDAY -> "Thứ Ba";
            case WEDNESDAY -> "Thứ Tư";
            case THURSDAY -> "Thứ Năm";
            case FRIDAY -> "Thứ Sáu";
            case SATURDAY -> "Thứ Bảy";
            case SUNDAY -> "Chủ Nhật";
        };
    }

    /**
     * Kiểm tra xem ca làm việc này đã diễn ra hay chưa.
     * Quy tắc nghiệp vụ: Có thể sửa hoặc hủy phân ca trước khi ca bắt đầu.
     *
     * @return true nếu ca chưa diễn ra (có thể sửa/hủy), false nếu đã diễn ra hoặc đang diễn ra
     */
    public boolean kiemTraChuaDienRa() {
        if (ngayLam == null) return true;
        LocalDate homNay = LocalDate.now();
        if (ngayLam.isAfter(homNay)) {
            return true;
        }
        if (ngayLam.isEqual(homNay)) {
            if (gioBatDau == null) return true;
            return LocalTime.now().isBefore(gioBatDau);
        }
        return false;
    }

    /**
     * Lấy dòng thông tin tóm tắt hiển thị ở thanh trạng thái dưới bảng (theo Image 1).
     * Ví dụ: "Lê Thị Hoa • Thứ Hai, 19/10/2026 • Ca sáng 08:00–16:00. Có thể sửa hoặc hủy phân ca trước khi ca bắt đầu."
     *
     * @return Chuỗi tóm tắt phân ca
     */
    public String layDongThongTinChiTiet() {
        String emp = (tenNhanVien != null) ? tenNhanVien : "Chưa xác định";
        String thu = layThuTrongTuan();
        String ngay = layNgayLamHienThi();
        String caGio = (tenCa != null ? tenCa : "") + " "
                + ((gioBatDau != null) ? gioBatDau.format(DINH_DANG_GIO) : "") + "–"
                + ((gioKetThuc != null) ? gioKetThuc.format(DINH_DANG_GIO) : "");

        String ghiChu = kiemTraChuaDienRa()
                ? "Có thể sửa hoặc hủy phân ca trước khi ca bắt đầu."
                : "Ca trực đang hoặc đã diễn ra.";

        return emp + " • " + thu + ", " + ngay + " • " + caGio + ". " + ghiChu;
    }

    // --- Getter và Setter ---

    public String getMaPhanCa() {
        return maPhanCa;
    }

    public void setMaPhanCa(String maPhanCa) {
        this.maPhanCa = maPhanCa;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public String getTenNhanVien() {
        return tenNhanVien;
    }

    public void setTenNhanVien(String tenNhanVien) {
        this.tenNhanVien = tenNhanVien;
    }

    public String getMaCa() {
        return maCa;
    }

    public void setMaCa(String maCa) {
        this.maCa = maCa;
    }

    public String getTenCa() {
        return tenCa;
    }

    public void setTenCa(String tenCa) {
        this.tenCa = tenCa;
    }

    public LocalTime getGioBatDau() {
        return gioBatDau;
    }

    public void setGioBatDau(LocalTime gioBatDau) {
        this.gioBatDau = gioBatDau;
    }

    public LocalTime getGioKetThuc() {
        return gioKetThuc;
    }

    public void setGioKetThuc(LocalTime gioKetThuc) {
        this.gioKetThuc = gioKetThuc;
    }

    public LocalDate getNgayLam() {
        return ngayLam;
    }

    public void setNgayLam(LocalDate ngayLam) {
        this.ngayLam = ngayLam;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShiftAssignment that = (ShiftAssignment) o;
        return Objects.equals(maPhanCa, that.maPhanCa);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maPhanCa);
    }

    @Override
    public String toString() {
        return layCaVaGioHienThiMotDong() + " (" + layNgayLamHienThi() + ")";
    }
}

