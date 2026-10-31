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
import java.time.LocalDateTime;
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
        // Mặc định đăng nhập tài khoản QUẢN LÝ (có toàn quyền lập đơn, xác nhận, hủy đơn, tìm kiếm)
        SessionManager.dangNhap(new TaiKhoan("NV-QL01", "quanly", "Quản lý kho", TaiKhoan.VaiTro.QUAN_LY));
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
        assertEquals("NV-QL01", donHang.getMaNhanVien());
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
    void xacNhanNhanHangThieuCongDungSoLuongThucNhan() {
        DonNhapHang donHang = taoDonChoNhan();
        ChiTietDonNhap ct1 = new ChiTietDonNhap(MA_DON, "SP-01", "Hạt Me-O", "Gói", 10, 7, new BigDecimal("100"));
        ChiTietDonNhap ct2 = new ChiTietDonNhap(MA_DON, "SP-02", "Pate Royal", "Lon", 5, 0, new BigDecimal("50"));
        dao.themDonNhapHang(donHang, List.of(ct1, ct2));

        assertTrue(service.xacNhanNhanHang(MA_DON, List.of(ct1, ct2)));

        assertEquals(DonNhapHang.TRANG_THAI_HOAN_TAT, donHang.getTrangThai());
        // SP-01 thực nhận 7 -> tồn kho tăng 7
        assertEquals(7, dao.soLuongTon.get("SP-01"));
        // SP-02 thực nhận 0 -> không cộng tồn kho
        assertEquals(0, dao.soLuongTon.getOrDefault("SP-02", 0));
        // Tổng tiền thực nhận = 7 * 100 + 0 * 50 = 700
        assertEquals(new BigDecimal("700"), donHang.getTongTienThucNhan());
    }

    @Test
    void xacNhanNhanHangChanKhiTatCaBangKhong() {
        DonNhapHang donHang = taoDonChoNhan();
        ChiTietDonNhap ct1 = new ChiTietDonNhap(MA_DON, "SP-01", "Hạt Me-O", "Gói", 10, 0, new BigDecimal("100"));
        ChiTietDonNhap ct2 = new ChiTietDonNhap(MA_DON, "SP-02", "Pate Royal", "Lon", 5, 0, new BigDecimal("50"));
        dao.themDonNhapHang(donHang, List.of(ct1, ct2));

        // Nếu tất cả thực nhận = 0 thì phải chặn và ném ngoại lệ gợi ý dùng chức năng Hủy đơn
        assertThrows(IllegalArgumentException.class, () -> service.xacNhanNhanHang(MA_DON, List.of(ct1, ct2)));
    }

    @Test
    void huyDonBatBuocNhapLyDo() {
        DonNhapHang donHang = taoDonChoNhan();
        dao.themDonNhapHang(donHang, List.of());

        // Không nhập lý do hoặc nhập khoảng trắng -> ném ngoại lệ
        assertThrows(IllegalArgumentException.class, () -> service.huyDonNhapHang(MA_DON, "   "));

        // Nhập lý do hợp lệ -> hủy thành công và lưu lý do
        assertTrue(service.huyDonNhapHang(MA_DON, "Nhà cung cấp hết hàng"));
        assertEquals(DonNhapHang.TRANG_THAI_DA_HUY, donHang.getTrangThai());
        assertEquals("Nhà cung cấp hết hàng", donHang.getLyDoHuy());
        assertEquals("NV-QL01", donHang.getMaNhanVienHuy());
    }

    @Test
    void huyDonChiChoPhepKhiDangChoNhan() {
        DonNhapHang donHang = taoDonChoNhan();
        dao.themDonNhapHang(donHang, List.of());

        assertTrue(service.huyDonNhapHang(MA_DON, "Lý do hủy"));
        assertEquals(DonNhapHang.TRANG_THAI_DA_HUY, donHang.getTrangThai());
        assertThrows(IllegalStateException.class, () -> service.huyDonNhapHang(MA_DON, "Hủy lại"));
        assertThrows(IllegalStateException.class, () -> service.xacNhanNhanHang(MA_DON));
    }

    @Test
    void banHangBiChanKhiXacNhanHoacHuyDon() {
        // Đăng nhập BÁN HÀNG: được phép lập đơn và tìm kiếm, nhưng BỊ CHẶN nhận hàng & hủy đơn
        SessionManager.dangNhap(new TaiKhoan("NV-BH01", "banhang", "Nhân viên bán hàng", TaiKhoan.VaiTro.BAN_HANG));

        DonNhapHang donHang = taoDonChoNhan();
        dao.themDonNhapHang(donHang, List.of(new ChiTietDonNhap(MA_DON, "SP-01", 5, BigDecimal.TEN)));

        // 1. Chặn xác nhận nhận hàng
        assertThrows(SecurityException.class, () -> service.xacNhanNhanHang(MA_DON));

        // 2. Chặn hủy đơn
        assertThrows(SecurityException.class, () -> service.huyDonNhapHang(MA_DON, "Lý do"));

        // 3. Nhưng vẫn được tìm kiếm
        service.timKiemDonHang("PN-", null, null, null, null);
        assertTrue(dao.daGoiTimKiem);

        // 4. Vẫn được lập đơn nhập mới
        DonNhapHang donMoi = new DonNhapHang();
        donMoi.setMaNhaCungCap("NCC-01");
        List<ChiTietDonNhap> chiTiet = List.of(new ChiTietDonNhap(null, "SP-01", 2, BigDecimal.TEN));
        assertTrue(service.lapDonNhapHang(donMoi, chiTiet));
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
        public List<ChiTietDonNhap> layChiTietTheoMaDonHang(String maDonHang) {
            return new ArrayList<>(chiTietTheoDon.getOrDefault(maDonHang, List.of()));
        }

        @Override
        public boolean themDonNhapHang(DonNhapHang donHang, List<ChiTietDonNhap> danhSachChiTiet) {
            donHangDaLuu = true;
            donHangs.put(donHang.getMaDonHang(), donHang);
            chiTietTheoDon.put(donHang.getMaDonHang(), new ArrayList<>(danhSachChiTiet));
            return true;
        }

        @Override
        public boolean xacNhanNhanHangVaCongTonKho(String maDonHang, String maNhanVienNhan,
                                                    LocalDateTime ngayGioNhan, BigDecimal tongTienThucNhan,
                                                    List<ChiTietDonNhap> chiTietCapNhat) {
            DonNhapHang donHang = donHangs.get(maDonHang);
            if (donHang == null || !DonNhapHang.TRANG_THAI_CHO_NHAN.equals(donHang.getTrangThai())) {
                return false;
            }
            for (ChiTietDonNhap chiTiet : chiTietCapNhat) {
                int tn = chiTiet.getSoLuongThucNhan();
                if (tn > 0) {
                    soLuongTon.merge(chiTiet.getMaSanPham(), tn, Integer::sum);
                }
            }
            donHang.setTrangThai(DonNhapHang.TRANG_THAI_HOAN_TAT);
            donHang.setMaNhanVienNhan(maNhanVienNhan);
            donHang.setNgayGioNhan(ngayGioNhan);
            donHang.setTongTienThucNhan(tongTienThucNhan);
            return true;
        }

        @Override
        public boolean capNhatTrangThaiHuy(String maDonHang, String maNhanVienHuy, String lyDoHuy) {
            DonNhapHang donHang = donHangs.get(maDonHang);
            if (donHang == null || !DonNhapHang.TRANG_THAI_CHO_NHAN.equals(donHang.getTrangThai())) {
                return false;
            }
            donHang.setTrangThai(DonNhapHang.TRANG_THAI_DA_HUY);
            donHang.setMaNhanVienHuy(maNhanVienHuy);
            donHang.setLyDoHuy(lyDoHuy);
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
