package com.petshop.controller;

import com.petshop.model.ChiTietDichVuLichHen;
import com.petshop.model.LichSuChamSoc;
import com.petshop.model.ThuCung;
import com.petshop.service.ThuCungService;

import java.time.LocalDate;
import java.util.List;

/**
 * Lớp điều khiển (Controller Layer) cho phân hệ Thú cưng.
 * Đóng vai trò cầu nối tiếp nhận yêu cầu từ các View (CapNhatThuCungView, ThemThuCungView, TimKiemThuCungView, XemChiTietThuCungView)
 * và chuyển tiếp xử lý xuống tầng nghiệp vụ ThuCungService.
 */
public class ThuCungController {

    private final ThuCungService thuCungService;

    public ThuCungController() {
        this(new ThuCungService());
    }

    public ThuCungController(ThuCungService thuCungService) {
        this.thuCungService = thuCungService;
    }

    /**
     * Lấy toàn bộ danh sách thú cưng để hiển thị trên bảng.
     */
    public List<ThuCung> layDanhSachThuCung() {
        return thuCungService.layDanhSachThuCung();
    }

    /**
     * Lấy danh sách thú cưng của một khách hàng cụ thể.
     */
    public List<ThuCung> layDanhSachTheoKhachHang(String maKhachHang) {
        return thuCungService.layDanhSachTheoKhachHang(maKhachHang);
    }

    /**
     * Tra cứu chi tiết thú cưng theo mã.
     */
    public ThuCung layThuCungTheoMa(String maThuCung) {
        return thuCungService.layThuCungTheoMa(maThuCung);
    }

    /**
     * Tìm kiếm và lọc danh sách thú cưng theo từ khóa và loài.
     */
    public List<ThuCung> timKiemThuCung(String tuKhoa, String locLoai) {
        return thuCungService.timKiemThuCung(tuKhoa, locLoai);
    }

    /**
     * Thêm mới một thú cưng.
     */
    public boolean themThuCung(ThuCung thuCung) {
        return thuCungService.themThuCung(thuCung);
    }

    /**
     * Cập nhật thông tin thú cưng.
     */
    public boolean capNhatThuCung(ThuCung thuCung) {
        return thuCungService.capNhatThuCung(thuCung);
    }

    /**
     * Tự động sinh mã thú cưng mới.
     */
    public String taoMaThuCungMoi() {
        return thuCungService.taoMaThuCungMoi();
    }

    /**
     * Lấy lịch sử chăm sóc của thú cưng.
     */
    public List<String[]> layLichSuChamSoc(String maThuCung) {
        return thuCungService.layLichSuChamSoc(maThuCung);
    }

    /**
     * Lấy danh sách lịch hẹn chăm sóc dịch vụ của thú cưng.
     */
    /**
     * Lấy danh sách thú cưng đã có ít nhất một lần hoàn thành dịch vụ chăm sóc.
     */
    public List<ThuCung> layDanhSachThuCungCoLichSuChamSoc() {
        return thuCungService.layDanhSachThuCungCoLichSuChamSoc();
    }

    /**
     * Tìm kiếm thú cưng có lịch sử chăm sóc hoàn thành theo từ khóa.
     */
    public List<ThuCung> timKiemThuCungCoLichSuChamSoc(String tuKhoa) {
        return thuCungService.timKiemThuCungCoLichSuChamSoc(tuKhoa);
    }

    public List<LichSuChamSoc> layDanhSachLichHenChamSoc(String maThuCung, LocalDate tuNgay, LocalDate denNgay, String trangThai) {
        return thuCungService.layDanhSachLichHenChamSoc(maThuCung, tuNgay, denNgay, trangThai);
    }

    /**
     * Lấy danh sách chi tiết các dịch vụ trong một lịch hẹn.
     */
    public List<ChiTietDichVuLichHen> layChiTietDichVuLichHen(String maLichHen) {
        return thuCungService.layChiTietDichVuLichHen(maLichHen);
    }
}

