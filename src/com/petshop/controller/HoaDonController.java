package com.petshop.controller;

import com.petshop.model.*;
import com.petshop.service.HoaDonService;
import com.petshop.service.KhachHangService;
import com.petshop.service.KhuyenMaiService;
import com.petshop.service.SanPhamService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Lớp điều khiển (Controller Layer) cho phân hệ Hóa đơn & Bán lẻ POS.
 * Đóng vai trò cầu nối tiếp nhận tương tác từ các View (LapHoaDonThanhToanView, TimKiemHoaDonView, XemChiTietHoaDonView)
 * và chuyển tiếp xuống tầng nghiệp vụ HoaDonService.
 */
public class HoaDonController {

    private final HoaDonService hoaDonService;
    private final SanPhamService sanPhamService;
    private final KhuyenMaiService khuyenMaiService;
    private final KhachHangService khachHangService;

    public HoaDonController() {
        this(new HoaDonService(), new SanPhamService(), new KhuyenMaiService(), new KhachHangService());
    }

    public HoaDonController(HoaDonService hoaDonService, SanPhamService sanPhamService,
                             KhuyenMaiService khuyenMaiService, KhachHangService khachHangService) {
        this.hoaDonService = hoaDonService;
        this.sanPhamService = sanPhamService;
        this.khuyenMaiService = khuyenMaiService;
        this.khachHangService = khachHangService;
    }

    /**
     * Lập và thanh toán hóa đơn.
     */
    public boolean lapHoaDon(HoaDon hoaDon) {
        return hoaDonService.lapHoaDon(hoaDon);
    }

    /**
     * Hủy hóa đơn bán hàng và hoàn lại tồn kho.
     */
    public boolean huyHoaDon(String maHoaDon, String lyDoHuy) {
        return hoaDonService.huyHoaDon(maHoaDon, lyDoHuy);
    }

    /**
     * Cập nhật thông tin hóa đơn và xử lý chênh lệch thanh toán thêm / hoàn tiền.
     */
    public boolean capNhatHoaDon(HoaDon hoaDonCu, HoaDon hoaDonMoi, BigDecimal tienThanhToanThem, BigDecimal tienHoanLai) {
        return hoaDonService.capNhatHoaDon(hoaDonCu, hoaDonMoi, tienThanhToanThem, tienHoanLai);
    }

    /**
     * Tra cứu chi tiết một hóa đơn theo mã.
     */
    public HoaDon layHoaDonTheoMa(String maHoaDon) {
        return hoaDonService.layHoaDonTheoMa(maHoaDon);
    }

    /**
     * Tìm kiếm hóa đơn theo các bộ lọc.
     */
    public List<HoaDon> timKiemHoaDon(String tuKhoa, LocalDate tuNgay, LocalDate denNgay, String hinhThucThanhToan, String trangThai) {
        return hoaDonService.timKiemHoaDon(tuKhoa, tuNgay, denNgay, hinhThucThanhToan, trangThai);
    }

    /**
     * Lấy toàn bộ danh sách hóa đơn hiện có.
     */
    public List<HoaDon> layTatCaHoaDon() {
        return hoaDonService.layTatCaHoaDon();
    }

    /**
     * Sinh mã hóa đơn mới theo ngày.
     */
    public String taoMaHoaDonMoi() {
        return hoaDonService.taoMaHoaDonMoi();
    }

    /**
     * Lấy danh sách phiếu dịch vụ chưa thanh toán để nạp nhanh vào hóa đơn.
     */
    public List<PhieuDichVu> layDanhSachPhieuDichVuChuaThanhToan() {
        return hoaDonService.layDanhSachPhieuDichVuChuaThanhToan();
    }

    /**
     * Lấy danh sách phiếu dịch vụ trong ngày của khách hàng mà chưa được thanh toán.
     * Tự động xuất thông tin phiếu dịch vụ khi nhân viên nhập số điện thoại khách hàng.
     */
    public List<PhieuDichVu> layPhieuDichVuTrongNgayCuaKhachHang(String maKhachHang, LocalDate ngay) {
        return hoaDonService.layPhieuDichVuTrongNgayCuaKhachHang(maKhachHang, ngay);
    }

    /**
     * Lấy chi tiết phiếu dịch vụ để nạp vào hóa đơn.
     */
    public PhieuDichVu layPhieuDichVuDeThanhToan(String maPhieuDichVu) {
        return hoaDonService.layPhieuDichVuDeThanhToan(maPhieuDichVu);
    }

    /**
     * Lấy danh sách sản phẩm còn hoạt động để chọn bán hàng.
     */
    public List<SanPham> layDanhSachSanPham() {
        return sanPhamService.timKiemSanPham("", true);
    }

    /**
     * Tra cứu số lượng tồn kho của một sản phẩm.
     */
    public int traCuuTonKho(String maSanPham) {
        return sanPhamService.traCuuTonKho(maSanPham);
    }

    /**
     * Lấy danh sách khách hàng để chọn/tìm kiếm khi lập hóa đơn.
     */
    public List<KhachHang> layDanhSachKhachHang() {
        return khachHangService.layDanhSachKhachHangDangHoatDong();
    }

    /**
     * Tìm khách hàng theo số điện thoại.
     */
    public KhachHang timKhachHangTheoSoDienThoai(String soDienThoai) {
        return khachHangService.layKhachHangTheoSoDienThoai(soDienThoai);
    }

    /**
     * Lấy danh sách khuyến mãi đang hoạt động.
     */
    public List<KhuyenMai> layDanhSachKhuyenMai() {
        return khuyenMaiService.layDanhSachKhuyenMai();
    }

    /**
     * Kiểm tra khuyến mãi hợp lệ.
     */
    public boolean kiemTraKhuyenMaiHopLe(String maKhuyenMai) {
        return khuyenMaiService.kiemTraKhuyenMaiHopLe(maKhuyenMai);
    }

    /**
     * Tính số tiền giảm giá theo mã khuyến mãi và tổng tiền gốc.
     */
    public BigDecimal tinhSoTienGiam(String maKhuyenMai, BigDecimal tongTienGoc) {
        return khuyenMaiService.tinhSoTienGiam(maKhuyenMai, tongTienGoc);
    }

    public int tinhDiemTichLuy(BigDecimal soTien, boolean laKhachHang) {
        return hoaDonService.tinhDiemTichLuy(soTien, laKhachHang);
    }

    /**
     * Lấy báo cáo thống kê doanh thu.
     */
    public Map<String, Object> layBaoCaoDoanhThu(LocalDate tuNgay, LocalDate denNgay) {
        return hoaDonService.layBaoCaoDoanhThu(tuNgay, denNgay);
    }

    public Map<String, Object> layBaoCaoDoanhThu(LocalDate tuNgay, LocalDate denNgay, String trangThai) {
        return hoaDonService.layBaoCaoDoanhThu(tuNgay, denNgay, trangThai);
    }

    public List<Map<String, Object>> layBaoCaoSanPhamBanRa(LocalDate tuNgay, LocalDate denNgay) {
        return hoaDonService.layBaoCaoSanPhamBanRa(tuNgay, denNgay);
    }

    public List<Map<String, Object>> layBaoCaoDichVuThucHien(LocalDate tuNgay, LocalDate denNgay) {
        return hoaDonService.layBaoCaoDichVuThucHien(tuNgay, denNgay);
    }
}
