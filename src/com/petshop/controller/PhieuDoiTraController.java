package com.petshop.controller;

import com.petshop.model.ChiTietPhieuDoiTra;
import com.petshop.model.HoaDon;
import com.petshop.model.PhieuDoiTra;
import com.petshop.model.SanPham;
import com.petshop.service.HoaDonService;
import com.petshop.service.PhieuDoiTraService;
import com.petshop.service.SanPhamService;

import java.time.LocalDate;
import java.util.List;

/**
 * Lop dieu khien (Controller Layer) cho phan he Phieu doi tra.
 * Ket noi tu cac View doi tra (LapPhieuDoiTraView, XuLyPhieuDoiTraView, TimKiemPhieuDoiTraView)
 * xuong tang nghiep vu PhieuDoiTraService, SanPhamService va HoaDonService.
 */
public class PhieuDoiTraController {

    private final PhieuDoiTraService phieuDoiTraService;
    private final SanPhamService sanPhamService;
    private final HoaDonService hoaDonService;

    public PhieuDoiTraController() {
        this(new PhieuDoiTraService(), new SanPhamService(), new HoaDonService());
    }

    public PhieuDoiTraController(PhieuDoiTraService phieuDoiTraService,
                                SanPhamService sanPhamService,
                                HoaDonService hoaDonService) {
        this.phieuDoiTraService = phieuDoiTraService;
        this.sanPhamService = sanPhamService;
        this.hoaDonService = hoaDonService;
    }

    /**
     * Sinh ma phieu doi tra tu dong theo quy chuan PDT-yyyyMMdd-xxxxxx.
     */
    public String sinhMaPhieu() {
        return phieuDoiTraService.sinhMaPhieu();
    }

    /**
     * Tra cuu hoa don goc theo ma hoa don, so dien thoai hoac ma khach hang.
     */
    public HoaDon traCuuHoaDonDeDoiTra(String tuKhoa) {
        return phieuDoiTraService.traCuuHoaDonDeDoiTra(tuKhoa);
    }

    /**
     * Kiem tra ma tran dieu kien Ly do doi tra va Tinh trang hang hoa (QL5, QL5a, QL5b).
     */
    public void kiemTraHopLeChiTiet(ChiTietPhieuDoiTra ct) {
        phieuDoiTraService.kiemTraHopLeChiTiet(ct);
    }

    /**
     * Lap va luu phieu doi tra moi vao he thong.
     */
    public boolean lapPhieuDoiTra(PhieuDoiTra phieu) {
        return phieuDoiTraService.lapPhieuDoiTra(phieu);
    }

    /**
     * Phe duyet phieu doi tra (chuyen sang Da duyet).
     */
    public boolean duyetPhieuDoiTra(String maPhieuDoiTra) {
        return phieuDoiTraService.duyetPhieuDoiTra(maPhieuDoiTra);
    }

    /**
     * Phe duyet phieu doi tra kem huong xu ly san pham khi hoan ("Thu lại vào kho" hoac "Hủy bỏ").
     */
    public boolean duyetPhieuDoiTra(String maPhieuDoiTra, String huongXuLySanPham) {
        return phieuDoiTraService.duyetPhieuDoiTra(maPhieuDoiTra, huongXuLySanPham);
    }

    /**
     * Tu choi phieu doi tra kem ly do tu choi.
     */
    public boolean tuChoiPhieuDoiTra(String maPhieuDoiTra, String lyDoTuChoi) {
        return phieuDoiTraService.tuChoiPhieuDoiTra(maPhieuDoiTra, lyDoTuChoi);
    }

    /**
     * Xac nhan hoan tat phieu doi tra va dong bo ton kho.
     */
    public boolean hoanTatPhieuDoiTra(String maPhieuDoiTra) {
        return phieuDoiTraService.hoanTatPhieuDoiTra(maPhieuDoiTra);
    }

    /**
     * Lay tat ca phieu doi tra.
     */
    public List<PhieuDoiTra> layTatCaPhieuDoiTra() {
        return phieuDoiTraService.layTatCaPhieuDoiTra();
    }

    /**
     * Tra cuu phieu doi tra theo tieu chi loc nang cao.
     */
    public List<PhieuDoiTra> timKiemPhieuDoiTra(String tuKhoa, LocalDate tuNgay, LocalDate denNgay,
                                                String trangThai, String loaiXuLy) {
        return phieuDoiTraService.timKiemPhieuDoiTra(tuKhoa, tuNgay, denNgay, trangThai, loaiXuLy);
    }

    /**
     * Lay chi tiet phieu doi tra theo ma.
     */
    public PhieuDoiTra layPhieuDoiTraTheoMa(String maPhieuDoiTra) {
        return phieuDoiTraService.layPhieuDoiTraTheoMa(maPhieuDoiTra);
    }

    /**
     * Lay danh sach san pham dang kinh doanh phuc vu chon san pham doi moi.
     */
    public List<SanPham> layDanhSachSanPhamDangKinhDoanh() {
        return sanPhamService.layDanhSachSanPham();
    }

    /**
     * Lay so luong san pham da doi tra truoc do trong hoa don.
     */
    public int laySoLuongDaDoiTra(String maHoaDon, String maSanPham) {
        return phieuDoiTraService.laySoLuongDaDoiTra(maHoaDon, maSanPham);
    }

    /**
     * Cap nhat noi dung phieu doi tra (khi o trang thai Cho xu ly).
     */
    public boolean capNhatPhieuDoiTra(PhieuDoiTra phieu) {
        return phieuDoiTraService.capNhatPhieuDoiTra(phieu);
    }
}

