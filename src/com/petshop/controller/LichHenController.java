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
 * Dieu phoi thao tac lich hen tu giao dien xuong tang nghiep vu LichHenService.
 */
public class LichHenController {

    private final LichHenService lichHenService;

    // Cac DAO ho tro nap du lieu khach hang, thu cung, dich vu cho form
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

    // Dat lich hen moi
    public boolean datLichHen(LichHen lichHen, List<ChiTietLichHen> danhSachChiTiet) {
        return lichHenService.datLichHen(lichHen, danhSachChiTiet);
    }

    // Doi ngay gio hoac dich vu cua lich hen
    public boolean capNhatLichHen(LichHen lichHen, List<ChiTietLichHen> danhSachChiTiet) {
        return lichHenService.capNhatLichHen(lichHen, danhSachChiTiet);
    }

    // Cap nhat trang thai duyet lich (Da dat, Da xac nhan, Hoan thanh, Da huy)
    public boolean capNhatTrangThai(String maLichHen, String trangThaiMoi) {
        return lichHenService.capNhatTrangThai(maLichHen, trangThaiMoi);
    }

    // Huy lich hen
    public boolean huyLichHen(String maLichHen, String lyDoHuy) {
        return lichHenService.huyLichHen(maLichHen, lyDoHuy);
    }

    // Lay toan bo danh sach lich hen
    public List<LichHen> layDanhSachLichHen() {
        return lichHenService.layDanhSachLichHen();
    }

    // Lay mot lich hen theo ma (cung cap cho #71 Hoa don)
    public LichHen layLichHenTheoMa(String maLichHen) {
        return lichHenService.layLichHenTheoMa(maLichHen);
    }

    // Lay danh sach dich vu chi tiet cua lich hen (cung cap cho #71 Hoa don)
    public List<ChiTietLichHen> layChiTietLichHen(String maLichHen) {
        return lichHenService.layChiTietLichHen(maLichHen);
    }

    // Tim kiem va loc lich hen
    public List<LichHen> timKiemLichHen(String tuKhoaKhach, LocalDate tuNgay, LocalDate denNgay, String trangThai) {
        return lichHenService.timKiemLichHen(tuKhoaKhach, tuNgay, denNgay, trangThai);
    }

    // Tim kiem va loc lich hen theo tu khoa, ngay, trang thai va nhan vien
    public List<LichHen> timKiemLichHen(String tuKhoaKhach, LocalDate tuNgay, LocalDate denNgay, String trangThai, String maNhanVien) {
        return lichHenService.timKiemLichHen(tuKhoaKhach, tuNgay, denNgay, trangThai, maNhanVien);
    }

    // Sinh ma lich hen moi tiep theo
    public String taoMaLichHenMoi() {
        return lichHenService.taoMaLichHenMoi();
    }

    // =========================================================================
    // HAM HO TRO NAP DU LIEU LIEN QUAN LEN GIAO DIEN FORM DAT LICH
    // =========================================================================

    // Lay danh sach khach hang de chon trong ComboBox
    public List<KhachHang> layDanhSachKhachHang() {
        return khachHangDAO.layDanhSachKhachHang();
    }

    // Lay danh sach thu cung cua mot khach hang
    public List<ThuCung> layDanhSachThuCungTheoKhachHang(String maKhachHang) {
        return thuCungDAO.layDanhSachTheoKhachHang(maKhachHang);
    }

    // Lay danh sach cac dich vu cham soc dang kinh doanh
    public List<DichVuChamSoc> layDanhSachDichVu() {
        return dichVuChamSocDAO.layDanhSachDichVuDangKinhDoanh();
    }

    // Tra gia dich vu dua tren can nang cua thu cung
    public double traGiaDichVuTheoCanNang(String maDichVu, double canNang) {
        return dichVuChamSocDAO.traGiaTheoCanNang(maDichVu, canNang);
    }

    // Lay danh sach nhan vien cham soc dang lam viec
    public List<String[]> layDanhSachNhanVienChamSoc() {
        return lichHenService.layDanhSachNhanVienChamSoc();
    }

    // Them nhanh khach hang moi (phuc vu dat lich cho khach la)
    public boolean themKhachHang(KhachHang kh) {
        return khachHangDAO.themKhachHang(kh);
    }

    // Sinh ma khach hang tu dong
    public String taoMaKhachHang() {
        return khachHangDAO.taoMaKhachHang();
    }

    // Kiem tra trung so dien thoai khach hang
    public boolean kiemTraTrungSoDienThoai(String soDienThoai) {
        return khachHangDAO.kiemTraTrungSoDienThoai(soDienThoai, null);
    }
}
