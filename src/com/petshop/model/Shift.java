package com.petshop.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Lớp thực thể Shift đại diện cho một ca làm việc của cửa hàng (bảng Ca trong CSDL).
 *
 * Tuân thủ quy tắc lập trình (Review 3_OOP và Review 4_Coding Convention):
 * - Tên lớp là danh từ (Shift), viết theo chuẩn PascalCase.
 * - Tên phương thức là động từ (lay..., kiemTra...), viết theo chuẩn camelCase.
 * - Tên hằng số viết hoa UPPER_SNAKE_CASE.
 * - Đầy đủ chú thích Javadoc và chú thích một dòng.
 */
public class Shift {

    private String maCa;              // CA-YYYYMMDD-NNNNNN
    private String tenCa;             // Ca sáng, Ca chiều, Ca sớm...
    private LocalTime gioBatDau;      // Giờ bắt đầu ca
    private LocalTime gioKetThuc;     // Giờ kết thúc ca
    private String trangThai;         // Đang hoạt động, Sắp bắt đầu, Đã kết thúc, Ngừng áp dụng
    private LocalDate ngayCapNhat;    // Ngày cập nhật ca trực gần nhất

    public static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Khởi tạo mặc định.
     */
    public Shift() {
        this.ngayCapNhat = LocalDate.now();
        this.trangThai = "Đang hoạt động";
    }

    /**
     * Khởi tạo đầy đủ thông tin cho ca trực.
     *
     * @param maCa        Mã định danh ca trực (CA-...)
     * @param tenCa       Tên ca trực (ví dụ: Ca sáng)
     * @param gioBatDau   Thời gian bắt đầu làm việc
     * @param gioKetThuc  Thời gian kết thúc ca
     * @param trangThai   Trạng thái ca trực
     * @param ngayCapNhat Ngày cập nhật thông tin
     */
    public Shift(String maCa, String tenCa, LocalTime gioBatDau, LocalTime gioKetThuc,
                 String trangThai, LocalDate ngayCapNhat) {
        this.maCa = maCa;
        this.tenCa = tenCa;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
        this.trangThai = (trangThai != null) ? trangThai : "Đang hoạt động";
        this.ngayCapNhat = (ngayCapNhat != null) ? ngayCapNhat : LocalDate.now();
    }

    /**
     * Xác định trạng thái hiển thị của ca trực dựa trên thời gian thực tế hiện tại
     * hoặc trạng thái xóa mềm / ngừng áp dụng được lưu trong CSDL.
     *
     * @return Chuỗi mô tả trạng thái: "Đang hoạt động", "Sắp bắt đầu", "Đã kết thúc" hoặc "Ngừng áp dụng"
     */
    public String layTrangThaiHienThi() {
        if ("Ngừng áp dụng".equalsIgnoreCase(this.trangThai) || "0".equals(this.trangThai)) {
            return "Ngừng áp dụng";
        }

        if (this.gioBatDau == null || this.gioKetThuc == null) {
            return this.trangThai;
        }

        LocalTime bayGio = LocalTime.now();

        // Nếu giờ kết thúc lớn hơn giờ bắt đầu (ca trong cùng một ngày)
        if (this.gioKetThuc.isAfter(this.gioBatDau)) {
            if (bayGio.isBefore(this.gioBatDau)) {
                return "Sắp bắt đầu";
            } else if (bayGio.isAfter(this.gioKetThuc)) {
                return "Đã kết thúc";
            } else {
                return "Đang hoạt động";
            }
        } else {
            // Ca qua đêm (ví dụ 22:00 -> 06:00 sáng hôm sau)
            if (bayGio.isAfter(this.gioBatDau) || bayGio.isBefore(this.gioKetThuc)) {
                return "Đang hoạt động";
            } else {
                return "Sắp bắt đầu";
            }
        }
    }

    /**
     * Lấy chuỗi định dạng hiển thị giờ bắt đầu (HH:mm).
     *
     * @return Chuỗi giờ định dạng HH:mm
     */
    public String layGioBatDauHienThi() {
        return (this.gioBatDau != null) ? this.gioBatDau.format(DINH_DANG_GIO) : "";
    }

    /**
     * Lấy chuỗi định dạng hiển thị giờ kết thúc (HH:mm).
     *
     * @return Chuỗi giờ định dạng HH:mm
     */
    public String layGioKetThucHienThi() {
        return (this.gioKetThuc != null) ? this.gioKetThuc.format(DINH_DANG_GIO) : "";
    }

    /**
     * Lấy chuỗi định dạng ngày cập nhật (dd/MM/yyyy).
     *
     * @return Chuỗi ngày định dạng dd/MM/yyyy
     */
    public String layNgayCapNhatHienThi() {
        return (this.ngayCapNhat != null) ? this.ngayCapNhat.format(DINH_DANG_NGAY) : "";
    }

    // --- Getter và Setter ---

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

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDate getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(LocalDate ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }

    @Override
    public String toString() {
        return "Shift{" +
                "maCa='" + maCa + '\'' +
                ", tenCa='" + tenCa + '\'' +
                ", gioBatDau=" + layGioBatDauHienThi() +
                ", gioKetThuc=" + layGioKetThucHienThi() +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}

