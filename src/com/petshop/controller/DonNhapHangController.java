package com.petshop.controller;

import com.petshop.model.ChiTietDonNhap;
import com.petshop.model.DonNhapHang;
import com.petshop.service.DonNhapHangService;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller điều phối nghiệp vụ Đơn nhập hàng giữa View và Service.
 */
public class DonNhapHangController {

    private final DonNhapHangService donNhapHangService;

    public DonNhapHangController() {
        this.donNhapHangService = new DonNhapHangService();
    }

    public DonNhapHangController(DonNhapHangService donNhapHangService) {
        this.donNhapHangService = donNhapHangService;
    }

    /**
     * Tự động sinh mã đơn nhập hàng.
     */
    public String taoMaDonHang() {
        return donNhapHangService.taoMaDonNhapHang();
    }

    public String taoMaDonNhapHang() {
        return donNhapHangService.taoMaDonNhapHang();
    }

    /**
     * IMP_CREATE: Lập đơn nhập hàng mới.
     */
    public boolean lapDonNhapHang(DonNhapHang donHang, List<ChiTietDonNhap> danhSachChiTiet) {
        return donNhapHangService.lapDonNhapHang(donHang, danhSachChiTiet);
    }

    /**
     * Lưu các đơn nhập đã được tách theo nhà cung cấp.
     */
    public boolean lapNhieuDonNhapHang(List<DonNhapHang> danhSachDonHang) {
        return donNhapHangService.lapNhieuDonNhapHang(danhSachDonHang);
    }

    /**
     * IMP_CONFIRM: Xác nhận nhận hàng và cộng tồn kho.
     */
    public boolean xacNhanNhanHang(String maDonHang) {
        return donNhapHangService.xacNhanNhanHang(maDonHang);
    }

    /**
     * IMP_CANCEL: Hủy đơn nhập hàng chưa nhận.
     */
    public boolean huyDonNhapHang(String maDonHang) {
        return donNhapHangService.huyDonNhapHang(maDonHang);
    }

    /**
     * IMP_SEARCH: Tìm kiếm đơn nhập hàng.
     */
    public List<DonNhapHang> timKiemDonHang(String keyword, String maNhaCungCap, LocalDate tuNgay, LocalDate denNgay, String trangThai) {
        return donNhapHangService.timKiemDonHang(keyword, maNhaCungCap, tuNgay, denNgay, trangThai);
    }

    /**
     * Lấy toàn bộ danh sách đơn nhập hàng.
     */
    public List<DonNhapHang> layTatCaDonHang() {
        return donNhapHangService.layTatCaDonHang();
    }

    /**
     * Lấy chi tiết đơn hàng theo mã đơn.
     */
    public DonNhapHang timTheoMa(String maDonHang) {
        return donNhapHangService.timTheoMa(maDonHang);
    }

    /**
     * Lấy danh sách sản phẩm của đơn hàng.
     */
    public List<ChiTietDonNhap> layChiTietTheoMaDonHang(String maDonHang) {
        return donNhapHangService.layChiTietTheoMaDonHang(maDonHang);
    }
}

