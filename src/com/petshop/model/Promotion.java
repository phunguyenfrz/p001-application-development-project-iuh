package com.petshop.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Lớp thực thể Promotion đại diện cho một chương trình khuyến mãi (bảng KhuyenMai).
 * Áp dụng các nguyên lý lập trình hướng đối tượng (OOP): Đóng gói, Trừu tượng hóa.
 */
public class Promotion {

    // Mã chương trình khuyến mãi (KM-YYYY-NNN hoặc KM-YYYYMMDD-NNNNNN)
    private String maKhuyenMai;

    // Tên chương trình ưu đãi hiển thị trên hóa đơn và giao diện
    private String tenChuongTrinh;

    // Phần trăm chiết khấu giảm giá (giá trị từ 1 đến 100)
    private double phanTramGiam;

    // Ngày bắt đầu có hiệu lực áp dụng
    private LocalDate ngayBatDau;

    // Ngày kết thúc hiệu lực áp dụng
    private LocalDate ngayKetThuc;

    // Trạng thái hoạt động (true: kích hoạt, false: ngừng áp dụng / xóa mềm)
    private boolean trangThai = true;

    // Ngày cập nhật thông tin gần nhất
    private LocalDate ngayCapNhat;

    /**
     * Khởi tạo đối tượng khuyến mãi mặc định.
     */
    public Promotion() {
        this.ngayCapNhat = LocalDate.now();
    }

    /**
     * Khởi tạo đối tượng khuyến mãi đầy đủ thuộc tính.
     *
     * @param maKhuyenMai    Mã khuyến mãi định danh duy nhất
     * @param tenChuongTrinh Tên chương trình khuyến mãi
     * @param phanTramGiam   Phần trăm giảm giá (1 - 100)
     * @param ngayBatDau     Ngày bắt đầu hiệu lực
     * @param ngayKetThuc    Ngày kết thúc hiệu lực
     * @param trangThai      Trạng thái hoạt động
     * @param ngayCapNhat    Ngày cập nhật thông tin
     */
    public Promotion(String maKhuyenMai, String tenChuongTrinh, double phanTramGiam,
                     LocalDate ngayBatDau, LocalDate ngayKetThuc, boolean trangThai, LocalDate ngayCapNhat) {
        this.maKhuyenMai = maKhuyenMai;
        this.tenChuongTrinh = tenChuongTrinh;
        this.phanTramGiam = phanTramGiam;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.trangThai = trangThai;
        this.ngayCapNhat = (ngayCapNhat != null) ? ngayCapNhat : LocalDate.now();
    }

    // =========================================================================
    // CÁC PHƯƠNG THỨC XỬ LÝ NGHIỆP VỤ (BUSINESS METHODS)
    // =========================================================================

    /**
     * Xác định chuỗi trạng thái hiển thị của khuyến mãi:
     * - "Ngừng áp dụng": Khi bị xóa mềm hoặc đã qua ngày kết thúc.
     * - "Sắp áp dụng": Khi còn hoạt động nhưng chưa tới ngày bắt đầu.
     * - "Đang áp dụng": Khi đang trong khoảng thời gian hiệu lực.
     *
     * @return Chuỗi mô tả trạng thái tương ứng
     */
    public String layTrangThaiHienThi() {
        // Trường hợp bị ngưng hoạt động (xóa mềm)
        if (!trangThai) {
            return "Ngừng áp dụng";
        }

        LocalDate homNay = LocalDate.now();

        // Chưa tới ngày bắt đầu
        if (ngayBatDau != null && homNay.isBefore(ngayBatDau)) {
            return "Sắp áp dụng";
        }

        // Đã quá ngày kết thúc
        if (ngayKetThuc != null && homNay.isAfter(ngayKetThuc)) {
            return "Ngừng áp dụng";
        }

        // Đang trong thời gian áp dụng
        return "Đang áp dụng";
    }

    /**
     * Kiểm tra khuyến mãi hiện tại có đang đủ điều kiện áp dụng cho hóa đơn mới hay không.
     *
     * @return true nếu đang trong thời gian hiệu lực và kích hoạt
     */
    public boolean kiemTraDangKichHoat() {
        return "Đang áp dụng".equals(layTrangThaiHienThi());
    }

    // =========================================================================
    // GETTERS VÀ SETTERS
    // =========================================================================

    public String getMaKhuyenMai() {
        return maKhuyenMai;
    }

    public void setMaKhuyenMai(String maKhuyenMai) {
        this.maKhuyenMai = maKhuyenMai;
    }

    public String getTenChuongTrinh() {
        return tenChuongTrinh;
    }

    public void setTenChuongTrinh(String tenChuongTrinh) {
        this.tenChuongTrinh = tenChuongTrinh;
    }

    public double getPhanTramGiam() {
        return phanTramGiam;
    }

    public void setPhanTramGiam(double phanTramGiam) {
        this.phanTramGiam = phanTramGiam;
    }

    public LocalDate getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDate ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDate getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDate ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDate getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(LocalDate ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Promotion promotion = (Promotion) o;
        return Objects.equals(maKhuyenMai, promotion.maKhuyenMai);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maKhuyenMai);
    }

    @Override
    public String toString() {
        return "Promotion{" +
                "maKhuyenMai='" + maKhuyenMai + '\'' +
                ", tenChuongTrinh='" + tenChuongTrinh + '\'' +
                ", phanTramGiam=" + phanTramGiam +
                ", ngayBatDau=" + ngayBatDau +
                ", ngayKetThuc=" + ngayKetThuc +
                ", trangThai=" + trangThai +
                ", ngayCapNhat=" + ngayCapNhat +
                '}';
    }
}

