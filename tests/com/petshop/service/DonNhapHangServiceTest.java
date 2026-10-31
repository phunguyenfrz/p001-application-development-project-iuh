package com.petshop.service;

import com.petshop.dao.DonNhapHangDAO;
import com.petshop.model.ChiTietDonNhap;
import com.petshop.model.DonNhapHang;
import com.petshop.model.TaiKhoan;
import com.petshop.util.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DonNhapHangServiceTest {

    private static final String MA_DON = "PN-20261030-000001";
    private FakeDonNhapHangDAO dao;
    private DonNhapHangService service;

    @BeforeEach
    void setUp() {
        SessionManager.dangNhap(new TaiKhoan("NV-01", "banhang", "Nhân viên bán hàng", TaiKhoan.VaiTro.BAN_HANG));
        dao = new FakeDonNhapHangDAO();
        service = new DonNhapHangService(dao, new XacThucService());
    }

    @AfterEach
    void tearDown() {
        SessionManager.dangXuat();
    }

    @Test
    void lapDonNhapTinhTongTienGanNhanVienVaLuuChiTiet() {
        DonNhapHang donHang = new DonNhapHang();
        donHang.setMaNhaCungCap("NCC-01");
        List<ChiTietDonNhap> chiTiet = List.of(
                new ChiTietDonNhap(null, "SP-01", 3, new BigDecimal("100")),
                new ChiTietDonNhap(null, "SP-02", 2, new BigDecimal("50"))
        );

        assertTrue(service.lapDonNhapHang(donHang, chiTiet));

        assertEquals(MA_DON, donHang.getMaDonHang());
        assertEquals("NV-01", donHang.getMaNhanVien());
        assertEquals(new BigDecimal("400"), donHang.getTongTien());
        assertEquals(DonNhapHang.TRANG_THAI_CHO_NHAN, donHang.getTrangThai());
        assertEquals(MA_DON, chiTiet.get(0).getMaDonHang());
        assertTrue(dao.donHangDaLuu);
    }

    @Test
    void xacNhanDonHangChiCongTonKhoMotLan() {
        DonNhapHang donHang = taoDonChoNhan();
        dao.themDonNhapHang(donHang, List.of(new ChiTietDonNhap(MA_DON, "SP-01", 4, BigDecimal.TEN)));

        assertTrue(service.xacNhanNhanHang(MA_DON));
        assertThrows(IllegalStateException.class, () -> service.xacNhanNhanHang(MA_DON));

        assertEquals(DonNhapHang.TRANG_THAI_HOAN_TAT, donHang.getTrangThai());
        assertEquals(4, dao.soLuongTon.get("SP-01"));
    }

    @Test
    void huyDonChiChoPhepKhiDangChoNhan() {
        DonNhapHang donHang = taoDonChoNhan();
        dao.themDonNhapHang(donHang, List.of());

        assertTrue(service.huyDonNhapHang(MA_DON));
        assertEquals(DonNhapHang.TRANG_THAI_DA_HUY, donHang.getTrangThai());
        assertThrows(IllegalStateException.class, () -> service.huyDonNhapHang(MA_DON));
        assertThrows(IllegalStateException.class, () -> service.xacNhanNhanHang(MA_DON));
    }

    @Test
    void timKiemTuChoiKhoangNgayKhongHopLe() {
        LocalDate tuNgay = LocalDate.of(2026, 10, 31);
        LocalDate denNgay = LocalDate.of(2026, 10, 30);

        assertThrows(IllegalArgumentException.class,
                () -> service.timKiemDonHang("PN-", null, tuNgay, denNgay, null));
        assertFalse(dao.daGoiTimKiem);
    }

    @Test
    void timKiemChuyenTieuChiHopLeChoDao() {
        LocalDate tuNgay = LocalDate.of(2026, 10, 1);
        LocalDate denNgay = LocalDate.of(2026, 10, 30);

        service.timKiemDonHang("PN-", "NCC-01", tuNgay, denNgay, DonNhapHang.TRANG_THAI_CHO_NHAN);

        assertTrue(dao.daGoiTimKiem);
        assertEquals("PN-", dao.tuKhoaTimKiem);
        assertEquals("NCC-01", dao.maNhaCungCapTimKiem);
        assertEquals(tuNgay, dao.tuNgayTimKiem);
        assertEquals(denNgay, dao.denNgayTimKiem);
    }

    @Test
    void tuChoiNguoiDungKhongCoQuyenTimKiem() {
        SessionManager.dangNhap(new TaiKhoan("NV-02", "chamsoc", "Nhân viên chăm sóc", TaiKhoan.VaiTro.CHAM_SOC));

        assertThrows(SecurityException.class,
                () -> service.timKiemDonHang(null, null, null, null, null));
        assertFalse(dao.daGoiTimKiem);
    }

    private DonNhapHang taoDonChoNhan() {
        DonNhapHang donHang = new DonNhapHang();
        donHang.setMaDonHang(MA_DON);
        donHang.setTrangThai(DonNhapHang.TRANG_THAI_CHO_NHAN);
        return donHang;
    }

    private static class FakeDonNhapHangDAO extends DonNhapHangDAO {
        private final Map<String, DonNhapHang> donHangs = new HashMap<>();
        private final Map<String, List<ChiTietDonNhap>> chiTietTheoDon = new HashMap<>();
        private final Map<String, Integer> soLuongTon = new HashMap<>();
        private boolean donHangDaLuu;
        private boolean daGoiTimKiem;
        private String tuKhoaTimKiem;
        private String maNhaCungCapTimKiem;
        private LocalDate tuNgayTimKiem;
        private LocalDate denNgayTimKiem;

        @Override
        public String taoMaDonNhapHang() {
            return MA_DON;
        }

        @Override
        public DonNhapHang timTheoMa(String maDonHang) {
            return donHangs.get(maDonHang);
        }

        @Override
        public boolean themDonNhapHang(DonNhapHang donHang, List<ChiTietDonNhap> danhSachChiTiet) {
            donHangDaLuu = true;
            donHangs.put(donHang.getMaDonHang(), donHang);
            chiTietTheoDon.put(donHang.getMaDonHang(), new ArrayList<>(danhSachChiTiet));
            return true;
        }

        @Override
        public boolean xacNhanNhanHangVaCongTonKho(String maDonHang) {
            DonNhapHang donHang = donHangs.get(maDonHang);
            if (donHang == null || !DonNhapHang.TRANG_THAI_CHO_NHAN.equals(donHang.getTrangThai())) {
                return false;
            }
            for (ChiTietDonNhap chiTiet : chiTietTheoDon.getOrDefault(maDonHang, List.of())) {
                soLuongTon.merge(chiTiet.getMaSanPham(), chiTiet.getSoLuong(), Integer::sum);
            }
            donHang.setTrangThai(DonNhapHang.TRANG_THAI_HOAN_TAT);
            return true;
        }

        @Override
        public boolean capNhatTrangThai(String maDonHang, String trangThaiHienTai, String trangThaiMoi) {
            DonNhapHang donHang = donHangs.get(maDonHang);
            if (donHang == null || !trangThaiHienTai.equals(donHang.getTrangThai())) {
                return false;
            }
            donHang.setTrangThai(trangThaiMoi);
            return true;
        }

        @Override
        public List<DonNhapHang> timKiemDonHang(String keyword, String maNhaCungCap,
                                                LocalDate tuNgay, LocalDate denNgay, String trangThai) {
            daGoiTimKiem = true;
            tuKhoaTimKiem = keyword;
            maNhaCungCapTimKiem = maNhaCungCap;
            tuNgayTimKiem = tuNgay;
            denNgayTimKiem = denNgay;
            return List.of();
        }
    }
}

