package com.petshop.test;

import com.petshop.dao.HoaDonDAO;
import com.petshop.model.*;
import com.petshop.service.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Kiểm thử đơn vị (Unit Tests) cho HoaDonService kiểm thử các chức năng:
 * - INV_CREATE (HD_TAO): Lập hóa đơn, kiểm tra tồn kho, áp khuyến mãi, tính tiền
 * - INV_SEARCH (HD_TIM_KIEM): Tra cứu, lọc theo tiêu chí
 * - INV_CANCEL (HD_CAP_NHAT): Hủy hóa đơn và hoàn lại tồn kho
 */
public class HoaDonServiceTest {

    private static int soTestPass = 0;
    private static int soTestFail = 0;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  BẮT ĐẦU KIỂM THỬ ĐƠN VỊ: HOADONSERVICE");
        System.out.println("=================================================");

        testTaoHoaDon_ThanhCong();
        testTaoHoaDon_ApMaKhuyenMai();
        testTaoHoaDon_ThatBai_KhongCoMatHang();
        testTaoHoaDon_ThatBai_SoLuongKhongHopLe();
        testTaoHoaDon_ThatBai_VuotQuaTonKho();
        testHuyHoaDon_ThanhCong();
        testHuyHoaDon_ThatBai_DaHuyTruocDo();
        testTimKiemHoaDon_TuNgayLonHonDenNgay();
        testRuleDiemTichLuy();
        testThanhToanKetHopDiemTichLuy();
        testMoTaGiamGiaKetHopKMVaDiem();
        testCapNhatHoaDon_ThanhToanThem();
        testCapNhatHoaDon_HoanTienMat();

        System.out.println("=================================================");
        System.out.printf("  KẾT QUẢ KIỂM THỬ: %d PASSED, %d FAILED%n", soTestPass, soTestFail);
        System.out.println("=================================================");
    }

    private static void testTaoHoaDon_ThanhCong() {
        System.out.print("[TEST 1] Lập hóa đơn bán sản phẩm hợp lệ: ");
        try {
            // Mock DAO
            HoaDonDAO mockDao = new MockHoaDonDAO();
            SanPhamService mockSpService = new MockSanPhamService(50);
            PhieuDichVuService mockPdvService = new PhieuDichVuService();
            KhuyenMaiService mockKmService = new KhuyenMaiService();

            HoaDonService service = new HoaDonService(mockDao, mockSpService, mockPdvService, mockKmService, null);

            HoaDon hd = new HoaDon();
            hd.setMaHoaDon("HD-20261101-000001");
            hd.setMaNhanVien("NV-20261005-000001");
            hd.setMaKhachHang("KH-20261001-000001");
            hd.setHinhThucThanhToan("Tiền mặt");

            hd.getDanhSachSanPham().add(new ChiTietHoaDonSanPham(
                    hd.getMaHoaDon(), "SP-000001", "Pate mèo Royal Canin", "Gói", 3, new BigDecimal("35000")
            ));

            boolean kq = service.lapHoaDon(hd);
            if (kq && hd.getTongTien().compareTo(new BigDecimal("105000")) == 0) {
                pass();
            } else {
                fail("Tổng tiền tính toán không đúng kỳ vọng (105,000 VNĐ)!");
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private static void testTaoHoaDon_ApMaKhuyenMai() {
        System.out.print("[TEST 2] Lập hóa đơn có áp dụng mã khuyến mãi: ");
        try {
            HoaDonDAO mockDao = new MockHoaDonDAO();
            SanPhamService mockSpService = new MockSanPhamService(100);
            PhieuDichVuService mockPdvService = new PhieuDichVuService();
            KhuyenMaiService mockKmService = new MockKhuyenMaiService(new BigDecimal("20000"));

            HoaDonService service = new HoaDonService(mockDao, mockSpService, mockPdvService, mockKmService, null);

            HoaDon hd = new HoaDon();
            hd.setMaHoaDon("HD-20261101-000002");
            hd.setMaNhanVien("NV-20261005-000001");
            hd.setMaKhuyenMai("KM-TEST20K");
            hd.setHinhThucThanhToan("Chuyển khoản");

            hd.getDanhSachSanPham().add(new ChiTietHoaDonSanPham(
                    hd.getMaHoaDon(), "SP-000002", "Dầu tắm chó", "Chai", 1, new BigDecimal("150000")
            ));

            service.lapHoaDon(hd);
            if (hd.getTongTien().compareTo(new BigDecimal("130000")) == 0 && hd.getTienGiam().compareTo(new BigDecimal("20000")) == 0) {
                pass();
            } else {
                fail("Số tiền giảm giá hoặc tổng tiền sau giảm không chính xác!");
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private static void testTaoHoaDon_ThatBai_KhongCoMatHang() {
        System.out.print("[TEST 3] Bắt lỗi khi lập hóa đơn rỗng: ");
        try {
            HoaDonDAO mockDao = new MockHoaDonDAO();
            SanPhamService mockSpService = new MockSanPhamService(50);
            HoaDonService service = new HoaDonService(mockDao, mockSpService, new PhieuDichVuService(), new KhuyenMaiService(), null);

            HoaDon hd = new HoaDon();
            hd.setMaHoaDon("HD-20261101-000003");
            hd.setMaNhanVien("NV-20261005-000001");
            hd.setHinhThucThanhToan("Tiền mặt");

            service.lapHoaDon(hd);
            fail("Không ném ngoại lệ khi hóa đơn không có sản phẩm/dịch vụ nào!");
        } catch (IllegalArgumentException e) {
            pass();
        } catch (Exception e) {
            fail("Ném sai loại ngoại lệ: " + e.getClass().getName());
        }
    }

    private static void testTaoHoaDon_ThatBai_SoLuongKhongHopLe() {
        System.out.print("[TEST 4] Bắt lỗi khi số lượng sản phẩm <= 0: ");
        try {
            HoaDonDAO mockDao = new MockHoaDonDAO();
            SanPhamService mockSpService = new MockSanPhamService(50);
            HoaDonService service = new HoaDonService(mockDao, mockSpService, new PhieuDichVuService(), new KhuyenMaiService(), null);

            HoaDon hd = new HoaDon();
            hd.setMaHoaDon("HD-20261101-000004");
            hd.setMaNhanVien("NV-20261005-000001");
            hd.setHinhThucThanhToan("Tiền mặt");
            hd.getDanhSachSanPham().add(new ChiTietHoaDonSanPham(hd.getMaHoaDon(), "SP-000001", 0, new BigDecimal("35000")));

            service.lapHoaDon(hd);
            fail("Không ném ngoại lệ khi số lượng = 0!");
        } catch (IllegalArgumentException e) {
            pass();
        } catch (Exception e) {
            fail("Ném sai ngoại lệ: " + e.getMessage());
        }
    }

    private static void testTaoHoaDon_ThatBai_VuotQuaTonKho() {
        System.out.print("[TEST 5] Bắt lỗi khi số lượng mua vượt tồn kho (INV_CREATE validation): ");
        try {
            HoaDonDAO mockDao = new MockHoaDonDAO();
            SanPhamService mockSpService = new MockSanPhamService(5); // Chỉ có 5 cái tồn kho
            HoaDonService service = new HoaDonService(mockDao, mockSpService, new PhieuDichVuService(), new KhuyenMaiService(), null);

            HoaDon hd = new HoaDon();
            hd.setMaHoaDon("HD-20261101-000005");
            hd.setMaNhanVien("NV-20261005-000001");
            hd.setHinhThucThanhToan("Tiền mặt");
            hd.getDanhSachSanPham().add(new ChiTietHoaDonSanPham(hd.getMaHoaDon(), "SP-000001", 10, new BigDecimal("35000"))); // Mua 10 cái

            service.lapHoaDon(hd);
            fail("Không chặn khi số lượng bán vượt quá tồn kho khả dụng!");
        } catch (IllegalArgumentException e) {
            pass();
        } catch (Exception e) {
            fail("Ném sai ngoại lệ: " + e.getMessage());
        }
    }

    private static void testHuyHoaDon_ThanhCong() {
        System.out.print("[TEST 6] Hủy hóa đơn đã thanh toán thành công (INV_CANCEL): ");
        try {
            MockHoaDonDAO mockDao = new MockHoaDonDAO();
            HoaDon hd = new HoaDon();
            hd.setMaHoaDon("HD-20261101-000006");
            hd.setTrangThaiThanhToan("Đã thanh toán");
            mockDao.themHoaDonSan(hd);

            HoaDonService service = new HoaDonService(mockDao, new MockSanPhamService(50), new PhieuDichVuService(), new KhuyenMaiService(), null);

            boolean kq = service.huyHoaDon("HD-20261101-000006", "Khách hàng đổi ý");
            if (kq) {
                pass();
            } else {
                fail("Hủy hóa đơn trả về false!");
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private static void testHuyHoaDon_ThatBai_DaHuyTruocDo() {
        System.out.print("[TEST 7] Bắt lỗi khi hủy hóa đơn đã ở trạng thái Đã hủy: ");
        try {
            MockHoaDonDAO mockDao = new MockHoaDonDAO();
            HoaDon hd = new HoaDon();
            hd.setMaHoaDon("HD-20261101-000007");
            hd.setTrangThaiThanhToan("Đã hủy");
            mockDao.themHoaDonSan(hd);

            HoaDonService service = new HoaDonService(mockDao, new MockSanPhamService(50), new PhieuDichVuService(), new KhuyenMaiService(), null);

            service.huyHoaDon("HD-20261101-000007", "Hủy lại lần 2");
            fail("Không ném ngoại lệ khi hóa đơn đã bị hủy trước đó!");
        } catch (IllegalStateException e) {
            pass();
        } catch (Exception e) {
            fail("Ném sai loại ngoại lệ: " + e.getClass().getName());
        }
    }

    private static void testTimKiemHoaDon_TuNgayLonHonDenNgay() {
        System.out.print("[TEST 8] Bắt lỗi khi Từ ngày > Đến ngày (INV_SEARCH validation): ");
        try {
            HoaDonDAO mockDao = new MockHoaDonDAO();
            HoaDonService service = new HoaDonService(mockDao, new MockSanPhamService(50), new PhieuDichVuService(), new KhuyenMaiService(), null);

            service.timKiemHoaDon("", LocalDate.now(), LocalDate.now().minusDays(5), "Tất cả", "Tất cả");
            fail("Không bắt lỗi Từ ngày lớn hơn Đến ngày!");
        } catch (IllegalArgumentException e) {
            pass();
        } catch (Exception e) {
            fail("Ném sai ngoại lệ: " + e.getMessage());
        }
    }

    private static void testRuleDiemTichLuy() {
        System.out.print("[TEST 9] Kiểm tra quy tắc cộng điểm tích lũy (dưới 1tr: 100k=5đ, trên 1tr: 100k=10đ, khách vãng lai=0): ");
        try {
            HoaDonService service = new HoaDonService(new MockHoaDonDAO(), new MockSanPhamService(50), new PhieuDichVuService(), new KhuyenMaiService(), null);

            // < 1tr: 500k -> 5 * 5 = 25đ
            int diem1 = service.tinhDiemTichLuy(new BigDecimal("550000"), true);
            if (diem1 != 25) {
                fail("Hóa đơn 550,000 VNĐ phải được 25 điểm, thực tế: " + diem1);
                return;
            }

            // < 100k -> 0đ
            int diem2 = service.tinhDiemTichLuy(new BigDecimal("99000"), true);
            if (diem2 != 0) {
                fail("Hóa đơn 99,000 VNĐ phải được 0 điểm, thực tế: " + diem2);
                return;
            }

            // >= 1tr: 1.250.000 VNĐ -> 12 * 10 = 120đ
            int diem3 = service.tinhDiemTichLuy(new BigDecimal("1250000"), true);
            if (diem3 != 120) {
                fail("Hóa đơn 1,250,000 VNĐ phải được 120 điểm, thực tế: " + diem3);
                return;
            }

            // Khách vãng lai -> 0đ
            int diem4 = service.tinhDiemTichLuy(new BigDecimal("2000000"), false);
            if (diem4 != 0) {
                fail("Khách vãng lai không được tích điểm, thực tế: " + diem4);
                return;
            }

            pass();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private static void testThanhToanKetHopDiemTichLuy() {
        System.out.print("[TEST 10] Thanh toán kết hợp điểm tích lũy (1 điểm = 1.000 VNĐ giảm trừ vào hóa đơn): ");
        try {
            HoaDonDAO mockDao = new MockHoaDonDAO();
            HoaDonService service = new HoaDonService(mockDao, new MockSanPhamService(50), new PhieuDichVuService(), new KhuyenMaiService(), null);

            HoaDon hd = new HoaDon();
            hd.setMaHoaDon("HD-20261101-000010");
            hd.setMaNhanVien("NV-20261005-000001");
            hd.setMaKhachHang("KH-20261001-000001");
            hd.setHinhThucThanhToan("Tiền mặt");
            // Mua 5 món x 100.000 = 500.000
            hd.getDanhSachSanPham().add(new ChiTietHoaDonSanPham(hd.getMaHoaDon(), "SP-000001", 5, new BigDecimal("100000")));
            // Dùng 50 điểm = 50.000 VNĐ
            hd.setDiemTichLuySuDung(50);

            service.lapHoaDon(hd);

            // Kiểm tra tiền giảm từ điểm = 50.000
            boolean dungTienGiam = hd.getTienGiamTuDiem().compareTo(new BigDecimal("50000")) == 0;
            // Tổng tiền sau giảm = 450.000
            boolean dungTongTien = hd.getTongTien().compareTo(new BigDecimal("450000")) == 0;
            // Điểm mới tích lũy từ 450.000: 4 * 5 = 20 điểm
            boolean dungDiemCong = hd.getDiemTichLuyCong() == 20;

            if (dungTienGiam && dungTongTien && dungDiemCong) {
                // Kiểm tra trường hợp lấn át về 0đ: hóa đơn 72.250 dùng 73 điểm
                HoaDon hdZero = new HoaDon();
                hdZero.setMaHoaDon("HD-20261101-000011");
                hdZero.setMaNhanVien("NV-20261005-000001");
                hdZero.setMaKhachHang("KH-20261001-000001");
                hdZero.setHinhThucThanhToan("Tiền mặt");
                hdZero.getDanhSachSanPham().add(new ChiTietHoaDonSanPham(hdZero.getMaHoaDon(), "SP-000001", 1, new BigDecimal("72250")));
                hdZero.setDiemTichLuySuDung(73);
                service.lapHoaDon(hdZero);

                if (hdZero.getTongTien().compareTo(BigDecimal.ZERO) == 0
                        && hdZero.getTienGiamTuDiem().compareTo(new BigDecimal("72250")) == 0) {
                    pass();
                } else {
                    fail(String.format("Lỗi lấn át về 0đ! Tổng tiền: %s, Tiền giảm điểm: %s",
                            hdZero.getTongTien(), hdZero.getTienGiamTuDiem()));
                }
            } else {
                fail(String.format("Kết quả sai! Tiền giảm điểm: %s, Tổng tiền: %s, Điểm cộng: %d",
                        hd.getTienGiamTuDiem(), hd.getTongTien(), hd.getDiemTichLuyCong()));
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private static void testMoTaGiamGiaKetHopKMVaDiem() {
        System.out.print("[TEST 11] Hiển thị rõ ràng khuyến mãi và điểm tích lũy dùng trong hóa đơn: ");
        try {
            HoaDon hd = new HoaDon();
            hd.setMaKhuyenMai("KM-20261005-000002");
            hd.setTienGiam(BigDecimal.valueOf(18000));
            hd.setDiemTichLuySuDung(5);

            String moTa = hd.getMoTaGiamGia();
            if (moTa.contains("KM-20261005-000002") && moTa.contains("18,000")
                    && moTa.contains("5 điểm") && moTa.contains("5,000")) {
                pass();
            } else {
                fail("Mô tả giảm giá không chính xác: " + moTa);
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private static void testCapNhatHoaDon_ThanhToanThem() {
        System.out.print("[TEST 12] Cập nhật hóa đơn mua thêm hàng -> Khách thanh toán thêm tiền: ");
        try {
            MockHoaDonDAO mockDao = new MockHoaDonDAO();
            HoaDonService service = new HoaDonService(mockDao, new MockSanPhamService(50), new PhieuDichVuService(),
                    new MockKhuyenMaiService(BigDecimal.ZERO), null);

            HoaDon hdCu = new HoaDon();
            hdCu.setMaHoaDon("HD-20261101-000001");
            hdCu.setTongTien(BigDecimal.valueOf(100_000));
            hdCu.getDanhSachSanPham().add(new ChiTietHoaDonSanPham("HD-20261101-000001", "SP01", 1, BigDecimal.valueOf(100_000)));
            mockDao.themHoaDonSan(hdCu);

            HoaDon hdMoi = new HoaDon();
            hdMoi.setMaHoaDon("HD-20261101-000001");
            hdMoi.setTongTien(BigDecimal.valueOf(150_000));
            hdMoi.getDanhSachSanPham().add(new ChiTietHoaDonSanPham("HD-20261101-000001", "SP01", 1, BigDecimal.valueOf(100_000)));
            hdMoi.getDanhSachSanPham().add(new ChiTietHoaDonSanPham("HD-20261101-000001", "SP02", 1, BigDecimal.valueOf(50_000)));

            BigDecimal tienTraThem = BigDecimal.valueOf(50_000);
            boolean ok = service.capNhatHoaDon(hdCu, hdMoi, tienTraThem, BigDecimal.ZERO);
            if (ok && hdMoi.getTongTien().compareTo(BigDecimal.valueOf(150_000)) == 0) {
                pass();
            } else {
                fail("Cập nhật hóa đơn thất bại");
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private static void testCapNhatHoaDon_HoanTienMat() {
        System.out.print("[TEST 13] Cập nhật hóa đơn bớt hàng -> Hoàn lại tiền mặt cho khách: ");
        try {
            MockHoaDonDAO mockDao = new MockHoaDonDAO();
            HoaDonService service = new HoaDonService(mockDao, new MockSanPhamService(50), new PhieuDichVuService(),
                    new MockKhuyenMaiService(BigDecimal.ZERO), null);

            HoaDon hdCu = new HoaDon();
            hdCu.setMaHoaDon("HD-20261101-000002");
            hdCu.setTongTien(BigDecimal.valueOf(200_000));
            hdCu.getDanhSachSanPham().add(new ChiTietHoaDonSanPham("HD-20261101-000002", "SP01", 2, BigDecimal.valueOf(100_000)));
            mockDao.themHoaDonSan(hdCu);

            HoaDon hdMoi = new HoaDon();
            hdMoi.setMaHoaDon("HD-20261101-000002");
            hdMoi.setTongTien(BigDecimal.valueOf(100_000));
            hdMoi.getDanhSachSanPham().add(new ChiTietHoaDonSanPham("HD-20261101-000002", "SP01", 1, BigDecimal.valueOf(100_000)));

            BigDecimal tienHoanLai = BigDecimal.valueOf(100_000);
            boolean ok = service.capNhatHoaDon(hdCu, hdMoi, BigDecimal.ZERO, tienHoanLai);
            if (ok && hdMoi.getTongTien().compareTo(BigDecimal.valueOf(100_000)) == 0) {
                pass();
            } else {
                fail("Hoàn tiền hóa đơn thất bại");
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private static void pass() {
        soTestPass++;
        System.out.println("PASSED ✔");
    }

    private static void fail(String lyDo) {
        soTestFail++;
        System.out.println("FAILED ❌: " + lyDo);
    }

    // =========================================================================
    // MOCK CLASSES
    // =========================================================================

    private static class MockHoaDonDAO extends HoaDonDAO {
        private final List<HoaDon> ds = new ArrayList<>();

        public void themHoaDonSan(HoaDon hd) {
            ds.add(hd);
        }

        @Override
        public String taoMaHoaDon() {
            return "HD-20261101-000001";
        }

        @Override
        public boolean luuHoaDon(HoaDon hoaDon) {
            ds.add(hoaDon);
            return true;
        }

        @Override
        public boolean huyHoaDon(String maHoaDon) {
            for (HoaDon h : ds) {
                if (h.getMaHoaDon().equals(maHoaDon)) {
                    h.setTrangThaiThanhToan("Đã hủy");
                    return true;
                }
            }
            return true;
        }

        @Override
        public boolean capNhatHoaDon(HoaDon hoaDonCu, HoaDon hoaDonMoi, BigDecimal tienThanhToanThem, BigDecimal tienHoanLai) {
            for (int i = 0; i < ds.size(); i++) {
                if (ds.get(i).getMaHoaDon().equals(hoaDonCu.getMaHoaDon())) {
                    ds.set(i, hoaDonMoi);
                    return true;
                }
            }
            ds.add(hoaDonMoi);
            return true;
        }

        @Override
        public HoaDon layHoaDonTheoMa(String maHoaDon) {
            for (HoaDon h : ds) {
                if (h.getMaHoaDon().equals(maHoaDon)) return h;
            }
            return null;
        }
    }

    private static class MockSanPhamService extends SanPhamService {
        private final int tonKho;

        public MockSanPhamService(int tonKho) {
            this.tonKho = tonKho;
        }

        @Override
        public int traCuuTonKho(String maSanPham) {
            return this.tonKho;
        }
    }

    private static class MockKhuyenMaiService extends KhuyenMaiService {
        private final BigDecimal soTienGiam;

        public MockKhuyenMaiService(BigDecimal soTienGiam) {
            this.soTienGiam = soTienGiam;
        }

        @Override
        public boolean kiemTraKhuyenMaiHopLe(String maKhuyenMai) {
            return true;
        }

        @Override
        public BigDecimal tinhSoTienGiam(String maKhuyenMai, BigDecimal tongHoaDon) {
            return this.soTienGiam;
        }
    }
}

