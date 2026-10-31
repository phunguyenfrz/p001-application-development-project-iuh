package com.petshop.controller;

import com.petshop.model.ChiTietDonNhap;
import com.petshop.model.DonNhapHang;
import com.petshop.service.DonNhapHangService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

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
     * IMP_CREATE: Lập đơn nhập hàng mới (đơn lẻ).
     */
    public boolean lapDonNhapHang(DonNhapHang donHang, List<ChiTietDonNhap> danhSachChiTiet) {
        return donNhapHangService.lapDonNhapHang(donHang, danhSachChiTiet);
    }

    /**
     * Tự động tách đơn theo nhà cung cấp ở tầng Service và lưu trong một Transaction.
     */
    public List<DonNhapHang> lapDonNhapHangTuDanhSach(List<ChiTietDonNhap> danhSachChiTiet,
                                                      Map<String, String> sanPhamNhaCungCapMap,
                                                      LocalDate ngayLap, LocalTime gioLap) {
        return donNhapHangService.lapDonNhapHangTuDanhSach(danhSachChiTiet, sanPhamNhaCungCapMap, ngayLap, gioLap);
    }

    /**
     * Lưu các đơn nhập đã được tách theo nhà cung cấp.
     */
    public boolean lapNhieuDonNhapHang(List<DonNhapHang> danhSachDonHang) {
        return donNhapHangService.lapNhieuDonNhapHang(danhSachDonHang);
    }

    /**
     * IMP_CONFIRM: Xác nhận nhận hàng với danh sách thực nhận (xử lý thiếu/hỏng).
     */
    public boolean xacNhanNhanHang(String maDonHang, List<ChiTietDonNhap> chiTietCapNhat) {
        return donNhapHangService.xacNhanNhanHang(maDonHang, chiTietCapNhat);
    }

    /**
     * IMP_CONFIRM (Phương án 3): Xác nhận nhận hàng có xử lý giao thiếu:
     * - Tự động tách đơn nhập bù (Backorder) hoặc
     * - Đóng đơn theo thực nhận kèm lý do chênh lệch bắt buộc.
     */
    public String xacNhanNhanHangThieu(String maDonHang, List<ChiTietDonNhap> chiTietCapNhat,
                                       boolean taoDonBu, String lyDoChenhLech) {
        return donNhapHangService.xacNhanNhanHangThieu(maDonHang, chiTietCapNhat, taoDonBu, lyDoChenhLech);
    }

    /**
     * Xác nhận nhận hàng mặc định nhận đủ.
     */
    public boolean xacNhanNhanHang(String maDonHang) {
        return donNhapHangService.xacNhanNhanHang(maDonHang);
    }

    /**
     * IMP_CANCEL: Hủy đơn nhập hàng chưa nhận với lý do hủy bắt buộc.
     */
    public boolean huyDonNhapHang(String maDonHang, String lyDoHuy) {
        return donNhapHangService.huyDonNhapHang(maDonHang, lyDoHuy);
    }

    /**
     * Hủy đơn nhập hàng (lý do mặc định).
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
     * IMP_SEARCH: Tìm kiếm đơn nhập hàng hỗ trợ lọc theo tên hàng.
     */
    public List<DonNhapHang> timKiemDonHang(String keyword, String maNhaCungCap, LocalDate tuNgay, LocalDate denNgay, String trangThai, String tenHang) {
        return donNhapHangService.timKiemDonHang(keyword, maNhaCungCap, tuNgay, denNgay, trangThai, tenHang);
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
