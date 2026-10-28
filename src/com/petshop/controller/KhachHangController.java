package com.petshop.controller;

import com.petshop.model.ChiTietGiaoDich;
import com.petshop.model.KhachHang;
import com.petshop.model.LichSuGiaoDich;
import com.petshop.service.KhachHangService;

import java.time.LocalDate;
import java.util.List;

/**
 * Lớp điều khiển (Controller Layer) cho phân hệ Khách hàng.
 * Đóng vai trò cầu nối tiếp nhận yêu cầu từ các View (CapNhatKhachHangView, ThemKhachHangView, TimKiemKhachHangView, LichSuGiaoDichKhachHangView)
 * và điều hướng xử lý xuống tầng nghiệp vụ KhachHangService.
 */
public class KhachHangController {

    private final KhachHangService khachHangService;

    public KhachHangController() {
        this.khachHangService = new KhachHangService();
    }

    public KhachHangController(KhachHangService khachHangService) {
        this.khachHangService = khachHangService;
    }

    /**
     * Lấy toàn bộ danh sách khách hàng để hiển thị trên bảng TableView.
     */
    public List<KhachHang> layDanhSachKhachHang() {
        return khachHangService.layDanhSachKhachHang();
    }

    /**
     * Lấy danh sách khách hàng đang hoạt động.
     */
    public List<KhachHang> layDanhSachKhachHangDangHoatDong() {
        return khachHangService.layDanhSachKhachHangDangHoatDong();
    }

    /**
     * Tra cứu thông tin khách hàng theo mã định danh.
     */
    public KhachHang layKhachHangTheoMa(String maKhachHang) {
        return khachHangService.layKhachHangTheoMa(maKhachHang);
    }

    /**
     * Tra cứu thông tin khách hàng theo số điện thoại.
     */
    public KhachHang layKhachHangTheoSoDienThoai(String soDienThoai) {
        return khachHangService.layKhachHangTheoSoDienThoai(soDienThoai);
    }

    /**
     * Tìm kiếm và lọc danh sách khách hàng linh hoạt qua từ khóa và trạng thái hoạt động.
     */
    public List<KhachHang> timKiemKhachHang(String tuKhoa, String locTrangThai) {
        return khachHangService.timKiemKhachHang(tuKhoa, locTrangThai);
    }

    /**
     * Lấy danh sách lịch sử hóa đơn giao dịch của khách hàng.
     */
    public List<LichSuGiaoDich> layLichSuGiaoDich(String maKhachHang, LocalDate tuNgay, LocalDate denNgay, String trangThaiThanhToan) {
        return khachHangService.layLichSuGiaoDich(maKhachHang, tuNgay, denNgay, trangThaiThanhToan);
    }

    /**
     * Lấy danh sách sản phẩm trong hóa đơn của khách hàng.
     */
    public List<ChiTietGiaoDich> laySanPhamHoaDon(String maKhachHang, String maHoaDon) {
        return khachHangService.laySanPhamHoaDon(maKhachHang, maHoaDon);
    }

    /**
     * Lấy danh sách dịch vụ trong hóa đơn của khách hàng.
     */
    public List<ChiTietGiaoDich> layDichVuHoaDon(String maKhachHang, String maHoaDon) {
        return khachHangService.layDichVuHoaDon(maKhachHang, maHoaDon);
    }

    /**
     * Thêm mới khách hàng vào cơ sở dữ liệu.
     */
    public boolean themKhachHang(KhachHang khachHang) {
        return khachHangService.themKhachHang(khachHang);
    }

    /**
     * Cập nhật thông tin khách hàng.
     */
    public boolean capNhatKhachHang(KhachHang khachHang) {
        return khachHangService.capNhatKhachHang(khachHang);
    }

    /**
     * Xóa mềm khách hàng (chuyển trạng thái sang ngừng hoạt động).
     */
    public boolean xoaMemKhachHang(String maKhachHang) {
        return khachHangService.xoaMemKhachHang(maKhachHang);
    }

    /**
     * Kích hoạt lại khách hàng đã ngừng hoạt động.
     */
    public boolean kichHoatLaiKhachHang(String maKhachHang) {
        return khachHangService.kichHoatLaiKhachHang(maKhachHang);
    }

    /**
     * Sinh mã khách hàng mới tiếp theo.
     */
    public String taoMaKhachHangMoi() {
        return khachHangService.taoMaKhachHangMoi();
    }
}
