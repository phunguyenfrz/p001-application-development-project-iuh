package com.petshop.controller;

import com.petshop.model.ChiTietDonHang;
import com.petshop.model.DonHang;
import com.petshop.service.DonHangService;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller điều phối nghiệp vụ Đơn nhập hàng giữa View và Service.
 * Tuân thủ Quy ước #60 và phân quyền BAN_HANG, QUAN_LY, ADMIN.
 */
public class DonHangController {

    private final DonHangService donHangService;

    public DonHangController() {
        this.donHangService = new DonHangService();
    }

    public DonHangController(DonHangService donHangService) {
        this.donHangService = donHangService;
    }

    /**
     * Tự động sinh mã đơn nhập hàng.
     */
    public String taoMaDonHang() {
        return donHangService.taoMaDonHang();
    }

    /**
     * IMP_CREATE: Lập đơn nhập hàng mới.
     */
    public boolean lapDonNhapHang(DonHang donHang, List<ChiTietDonHang> danhSachChiTiet) {
        return donHangService.lapDonNhapHang(donHang, danhSachChiTiet);
    }

    /** Lưu các đơn nhập đã được tách theo nhà cung cấp. */
    public boolean lapNhieuDonNhapHang(List<DonHang> danhSachDonHang) {
        return donHangService.lapNhieuDonNhapHang(danhSachDonHang);
    }

    /**
     * IMP_CONFIRM: Xác nhận nhận hàng và cộng tồn kho.
     */
    public boolean xacNhanNhanHang(String maDonHang) {
        return donHangService.xacNhanNhanHang(maDonHang);
    }

    /**
     * IMP_CANCEL: Hủy đơn nhập hàng chưa nhận.
     */
    public boolean huyDonNhapHang(String maDonHang) {
        return donHangService.huyDonNhapHang(maDonHang);
    }

    /**
     * IMP_SEARCH: Tìm kiếm đơn nhập hàng.
     */
    public List<DonHang> timKiemDonHang(String keyword, String maNhaCungCap, LocalDate tuNgay, LocalDate denNgay, String trangThai) {
        return donHangService.timKiemDonHang(keyword, maNhaCungCap, tuNgay, denNgay, trangThai);
    }

    /**
     * Lấy toàn bộ danh sách đơn nhập hàng.
     */
    public List<DonHang> layTatCaDonHang() {
        return donHangService.layTatCaDonHang();
    }

    /**
     * Lấy chi tiết đơn hàng theo mã đơn.
     */
    public DonHang timTheoMa(String maDonHang) {
        return donHangService.timTheoMa(maDonHang);
    }

    /**
     * Lấy danh sách sản phẩm của đơn hàng.
     */
    public List<ChiTietDonHang> layChiTietTheoMaDonHang(String maDonHang) {
        return donHangService.layChiTietTheoMaDonHang(maDonHang);
    }
}

