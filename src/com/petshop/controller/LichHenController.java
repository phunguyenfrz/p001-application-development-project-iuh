package com.petshop.controller;

import com.petshop.dao.DichVuChamSocDAO;
import com.petshop.dao.KhachHangDAO;
import com.petshop.dao.ThuCungDAO;
import com.petshop.model.ChiTietLichHen;
import com.petshop.model.DichVuChamSoc;
import com.petshop.model.KhachHang;
import com.petshop.model.LichHen;
import com.petshop.model.ThuCung;
import com.petshop.service.LichHenService;

import java.time.LocalDate;
import java.util.List;

/**
 * Điều phối thao tác lịch hẹn từ giao diện xuống tầng nghiệp vụ LichHenService.
 */
public class LichHenController {

    private final LichHenService lichHenService;

    // Các DAO hỗ trợ nạp dữ liệu khách hàng, thú cưng, dịch vụ cho form
    // TODO: nối KhachHangService, ThuCungService, DichVuChamSocService khi các phân hệ hoàn thiện
    private final KhachHangDAO khachHangDAO;
    private final ThuCungDAO thuCungDAO;
    private final DichVuChamSocDAO dichVuChamSocDAO;

    public LichHenController() {
        this.lichHenService = new LichHenService();
        this.khachHangDAO = new KhachHangDAO();
        this.thuCungDAO = new ThuCungDAO();
        this.dichVuChamSocDAO = new DichVuChamSocDAO();
    }

    public LichHenController(LichHenService lichHenService) {
        this.lichHenService = lichHenService;
        this.khachHangDAO = new KhachHangDAO();
        this.thuCungDAO = new ThuCungDAO();
        this.dichVuChamSocDAO = new DichVuChamSocDAO();
    }

    // Đặt lịch hẹn mới
    public boolean datLichHen(LichHen lichHen, List<ChiTietLichHen> danhSachChiTiet) {
        return lichHenService.datLichHen(lichHen, danhSachChiTiet);
    }

    // Đổi ngày giờ hoặc dịch vụ của lịch hẹn
    public boolean capNhatLichHen(LichHen lichHen, List<ChiTietLichHen> danhSachChiTiet) {
        return lichHenService.capNhatLichHen(lichHen, danhSachChiTiet);
    }

    // Cập nhật trạng thái duyệt lịch (Đã đặt, Đã xác nhận, Hoàn thành, Đã hủy)
    public boolean capNhatTrangThai(String maLichHen, String trangThaiMoi) {
        return lichHenService.capNhatTrangThai(maLichHen, trangThaiMoi);
    }

    // Hủy lịch hẹn
    public boolean huyLichHen(String maLichHen, String lyDoHuy) {
        return lichHenService.huyLichHen(maLichHen, lyDoHuy);
    }

    // Lấy toàn bộ danh sách lịch hẹn
    public List<LichHen> layDanhSachLichHen() {
        return lichHenService.layDanhSachLichHen();
    }

    // Lấy một lịch hẹn theo mã
    public LichHen layLichHenTheoMa(String maLichHen) {
        return lichHenService.layLichHenTheoMa(maLichHen);
    }

    // Lấy danh sách dịch vụ chi tiết của lịch hẹn
    public List<ChiTietLichHen> layChiTietLichHen(String maLichHen) {
        return lichHenService.layChiTietLichHen(maLichHen);
    }

    // Tìm kiếm và lọc lịch hẹn
    public List<LichHen> timKiemLichHen(String tuKhoaKhach, LocalDate tuNgay, LocalDate denNgay, String trangThai) {
        return lichHenService.timKiemLichHen(tuKhoaKhach, tuNgay, denNgay, trangThai);
    }

    // Sinh mã lịch hẹn mới tiếp theo
    public String taoMaLichHenMoi() {
        return lichHenService.taoMaLichHenMoi();
    }

    // =========================================================================
    // HÀM HỖ TRỢ NẠP DỮ LIỆU LIÊN QUAN LÊN GIAO DIỆN FORM ĐẶT LỊCH
    // =========================================================================

    // Lấy danh sách khách hàng để chọn trong ComboBox
    // TODO: nối KhachHangService
    public List<KhachHang> layDanhSachKhachHang() {
        return khachHangDAO.layDanhSachKhachHang();
    }

    // Lấy danh sách thú cưng của một khách hàng
    // TODO: nối ThuCungService
    public List<ThuCung> layDanhSachThuCungTheoKhachHang(String maKhachHang) {
        return thuCungDAO.layDanhSachTheoKhachHang(maKhachHang);
    }

    // Lấy danh sách các dịch vụ chăm sóc đang kinh doanh
    // TODO: nối DichVuChamSocService
    public List<DichVuChamSoc> layDanhSachDichVu() {
        return dichVuChamSocDAO.layDanhSachDichVuDangKinhDoanh();
    }

    // Tra giá dịch vụ dựa trên cân nặng của thú cưng
    // TODO: nối DichVuChamSocService
    public double traGiaDichVuTheoCanNang(String maDichVu, double canNang) {
        return dichVuChamSocDAO.traGiaTheoCanNang(maDichVu, canNang);
    }

    // Lấy danh sách nhân viên chăm sóc đang làm việc
    public List<String[]> layDanhSachNhanVienChamSoc() {
        return new com.petshop.dao.LichHenDAO().layDanhSachNhanVienChamSoc();
    }
}

