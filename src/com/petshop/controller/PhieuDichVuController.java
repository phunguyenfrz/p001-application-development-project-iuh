package com.petshop.controller;

import com.petshop.dao.DichVuChamSocDAO;
import com.petshop.dao.KhachHangDAO;
import com.petshop.dao.LichHenDAO;
import com.petshop.dao.ThuCungDAO;
import com.petshop.model.*;
import com.petshop.service.PhieuDichVuService;

import java.time.LocalDate;
import java.util.List;

/**
 * Lớp Controller điều phối thao tác phiếu tiếp nhận và theo dõi dịch vụ thú cưng.
 */
public class PhieuDichVuController {

    private final PhieuDichVuService phieuDichVuService;

    // Các DAO hỗ trợ nạp dữ liệu khách hàng, thú cưng, dịch vụ lên giao diện
    private final KhachHangDAO khachHangDAO;
    private final ThuCungDAO thuCungDAO;
    private final DichVuChamSocDAO dichVuChamSocDAO;
    private final LichHenDAO lichHenDAO;

    public PhieuDichVuController() {
        this.phieuDichVuService = new PhieuDichVuService();
        this.khachHangDAO = new KhachHangDAO();
        this.thuCungDAO = new ThuCungDAO();
        this.dichVuChamSocDAO = new DichVuChamSocDAO();
        this.lichHenDAO = new LichHenDAO();
    }

    public PhieuDichVuController(PhieuDichVuService phieuDichVuService) {
        this.phieuDichVuService = phieuDichVuService;
        this.khachHangDAO = new KhachHangDAO();
        this.thuCungDAO = new ThuCungDAO();
        this.dichVuChamSocDAO = new DichVuChamSocDAO();
        this.lichHenDAO = new LichHenDAO();
    }

    // Tạo phiếu tiếp nhận dịch vụ mới
    public boolean taoPhieuDichVu(PhieuDichVu phieu, List<ChiTietPhieuDichVu> danhSachChiTiet) {
        return phieuDichVuService.taoPhieuDichVu(phieu, danhSachChiTiet);
    }

    // Cập nhật thông tin phiếu dịch vụ và chi tiết
    public boolean capNhatPhieuDichVu(PhieuDichVu phieu, List<ChiTietPhieuDichVu> danhSachChiTiet) {
        return phieuDichVuService.capNhatPhieuDichVu(phieu, danhSachChiTiet);
    }

    // Cập nhật trạng thái tiến độ phiếu (Đang chờ, Đang thực hiện, Hoàn thành, Đã hủy)
    public boolean capNhatTrangThai(String maPhieuDichVu, String trangThaiMoi) {
        return phieuDichVuService.capNhatTrangThai(maPhieuDichVu, trangThaiMoi);
    }

    // Hủy phiếu dịch vụ (xóa mềm)
    public boolean huyPhieuDichVu(String maPhieuDichVu, String lyDoHuy) {
        return phieuDichVuService.huyPhieuDichVu(maPhieuDichVu, lyDoHuy);
    }

    // Lấy toàn bộ danh sách phiếu dịch vụ
    public List<PhieuDichVu> layDanhSachPhieuDichVu() {
        return phieuDichVuService.layDanhSachPhieuDichVu();
    }

    // Lấy danh sách dịch vụ chi tiết của một phiếu
    public List<ChiTietPhieuDichVu> layChiTietPhieuDichVu(String maPhieuDichVu) {
        return phieuDichVuService.layChiTietPhieuDichVu(maPhieuDichVu);
    }

    // Tìm kiếm và lọc phiếu dịch vụ
    public List<PhieuDichVu> timKiemPhieuDichVu(String tuKhoa, LocalDate tuNgay, LocalDate denNgay, String trangThai) {
        return phieuDichVuService.timKiemPhieuDichVu(tuKhoa, tuNgay, denNgay, trangThai);
    }

    // Lấy danh sách lịch hẹn đang chờ tiếp nhận để lập phiếu nhanh
    public List<LichHen> layDanhSachLichHenChoTiepNhan() {
        return phieuDichVuService.layDanhSachLichHenChoTiepNhan();
    }

    // Tự sinh mã phiếu mới tiếp theo
    public String taoMaPhieuDichVuMoi() {
        return phieuDichVuService.taoMaPhieuDichVuMoi();
    }

    // =========================================================================
    // HÀM HỖ TRỢ NẠP DỮ LIỆU LIÊN KẾT CHO FORM GIAO DIỆN
    // =========================================================================

    // Lấy danh sách khách hàng để chọn trong ComboBox
    public List<KhachHang> layDanhSachKhachHang() {
        return khachHangDAO.layDanhSachKhachHang();
    }

    // Lấy danh sách thú cưng của một khách hàng
    public List<ThuCung> layDanhSachThuCungTheoKhachHang(String maKhachHang) {
        return thuCungDAO.layDanhSachTheoKhachHang(maKhachHang);
    }

    // Lấy danh sách các dịch vụ chăm sóc đang kinh doanh
    public List<DichVuChamSoc> layDanhSachDichVu() {
        return dichVuChamSocDAO.layDanhSachDichVuDangKinhDoanh();
    }

    // Tra giá dịch vụ theo cân nặng của thú cưng
    public double traGiaDichVuTheoCanNang(String maDichVu, double canNang) {
        return dichVuChamSocDAO.traGiaTheoCanNang(maDichVu, canNang);
    }

    // Lấy chi tiết các dịch vụ đã đăng ký trong một lịch hẹn
    public List<ChiTietLichHen> layChiTietLichHen(String maLichHen) {
        return lichHenDAO.layChiTietLichHen(maLichHen);
    }

    // Lấy danh sách nhân viên chăm sóc đang làm việc
    public List<String[]> layDanhSachNhanVien() {
        return lichHenDAO.layDanhSachNhanVienChamSoc();
    }

    // Cập nhật cân nặng thực tế cho thú cưng khi cân tại quầy tiếp nhận
    public boolean capNhatCanNangThuCung(String maThuCung, java.math.BigDecimal canNang) {
        return thuCungDAO.capNhatCanNang(maThuCung, canNang);
    }

    // Thêm khách hàng nhanh tại quầy
    public boolean themKhachHang(KhachHang kh) {
        return khachHangDAO.themKhachHang(kh);
    }

    // Tự sinh mã khách hàng mới
    public String taoMaKhachHang() {
        return khachHangDAO.taoMaKhachHang();
    }

    // Kiểm tra trùng số điện thoại khách hàng
    public boolean kiemTraTrungSoDienThoai(String soDienThoai) {
        return khachHangDAO.kiemTraTrungSoDienThoai(soDienThoai, null);
    }

    // Thêm nhanh hồ sơ thú cưng tại quầy
    public boolean themThuCung(ThuCung tc) {
        return thuCungDAO.themThuCung(tc);
    }

    // Tự sinh mã thú cưng mới
    public String taoMaThuCungMoi() {
        return thuCungDAO.taoMaThuCung();
    }
}

