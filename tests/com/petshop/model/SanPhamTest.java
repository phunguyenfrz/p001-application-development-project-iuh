package com.petshop.model;

import com.petshop.dao.SanPhamDAO;
import com.petshop.service.SanPhamService;
import com.petshop.service.XacThucService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class SanPhamTest {

    @Test
    void testSoLuongTonBang0LaHetHang() {
        SanPham sp = new SanPham();
        sp.setSoLuongTon(0);
        sp.setNguongTonToiThieu(10);
        sp.setTrangThai(true);

        assertTrue(sp.isHetHang(), "Khi số lượng tồn = 0 thì phải là hết hàng");
        assertFalse(sp.isSapHetHang(), "Hết hàng không xem là sắp hết hàng");
        assertEquals("Hết hàng", sp.getTrangThaiHienThi(), "Trạng thái hiển thị phải là 'Hết hàng'");
    }

    @Test
    void testDuoiNguongAnToanLaSapHetHang() {
        SanPham sp = new SanPham();
        sp.setSoLuongTon(5);
        sp.setNguongTonToiThieu(10);
        sp.setTrangThai(true);

        assertFalse(sp.isHetHang(), "Tồn = 5 chưa phải là hết hàng");
        assertTrue(sp.isSapHetHang(), "Tồn = 5 <= ngưỡng 10 phải là sắp hết hàng");
        assertEquals("Sắp hết hàng", sp.getTrangThaiHienThi(), "Dưới ngưỡng an toàn trạng thái hiển thị phải là 'Sắp hết hàng'");
    }

    @Test
    void testSoLuongTonLonHonNguongLaConHang() {
        SanPham sp = new SanPham();
        sp.setSoLuongTon(50);
        sp.setNguongTonToiThieu(10);
        sp.setTrangThai(true);

        assertFalse(sp.isHetHang());
        assertFalse(sp.isSapHetHang());
        assertEquals("Đang kinh doanh", sp.getTrangThaiHienThi(), "Tồn trên ngưỡng an toàn hiển thị là 'Đang kinh doanh'");
    }

    @Test
    void testNgungKinhDoanh() {
        SanPham sp = new SanPham();
        sp.setSoLuongTon(0);
        sp.setTrangThai(false);

        assertEquals("Ngừng kinh doanh", sp.getTrangThaiHienThi(), "Khi ngừng kinh doanh thì ưu tiên hiển thị 'Ngừng kinh doanh'");
    }

    @Test
    void testDuLieuCuChuaCoGiaNhapSeDatMacDinhHopLy() {
        SanPham sp = new SanPham(
                "SP-20261005-000001", "NCC-20261005-000001",
                "Hạt Royal Canin Indoor 2kg", "893123456701",
                new BigDecimal("350000"), 45, 10, "Gói", true
        );

        assertNotNull(sp.getGiaNhap());
        assertTrue(sp.getGiaNhap().compareTo(BigDecimal.ZERO) > 0, "Giá nhập mặc định phải > 0");
        assertTrue(sp.getGiaBan().compareTo(sp.getGiaNhap()) > 0, "Giá bán phải > giá nhập mặc định");
        assertEquals(0, new BigDecimal("245000").compareTo(sp.getGiaNhap()));
    }

    @Test
    void testRangBuocGiaBanKhongDuocBang0VaPhaiLonHonGiaNhap() {
        XacThucService xacThucChoPhep = new XacThucService() {
            @Override
            public void kiemTraQuyen(String maQuyen) {
                // Bỏ qua kiểm tra quyền trong unit test
            }
        };
        SanPhamDAO daoGiaLap = new SanPhamDAO() {
            @Override
            public boolean kiemTraTonTaiMaVach(String maVach, String maSanPhamHienTai) {
                return false;
            }

            @Override
            public boolean capNhatSanPham(SanPham sp) {
                return true;
            }
        };
        SanPhamService service = new SanPhamService(daoGiaLap, xacThucChoPhep);

        SanPham sp = new SanPham();
        sp.setMaSanPham("SP-20261005-000001");
        sp.setTenSanPham("Pate mèo");
        sp.setMaVach("89320261005000001");
        sp.setDonViTinh("Gói");
        sp.setSoLuongTon(10);
        sp.setNguongTonToiThieu(5);

        // 1. Giá bán = 0 -> báo lỗi
        sp.setGiaNhap(new BigDecimal("15000"));
        sp.setGiaBan(BigDecimal.ZERO);
        IllegalArgumentException exGiaBan0 = assertThrows(
                IllegalArgumentException.class,
                () -> service.capNhatSanPham(sp)
        );
        assertTrue(exGiaBan0.getMessage().contains("Giá bán"));

        // 2. Giá bán <= Giá nhập -> báo lỗi
        sp.setGiaNhap(new BigDecimal("20000"));
        sp.setGiaBan(new BigDecimal("20000"));
        IllegalArgumentException exBangNhau = assertThrows(
                IllegalArgumentException.class,
                () -> service.capNhatSanPham(sp)
        );
        assertTrue(exBangNhau.getMessage().contains("Giá bán phải lớn hơn giá nhập"));

        sp.setGiaNhap(new BigDecimal("25000"));
        sp.setGiaBan(new BigDecimal("20000"));
        IllegalArgumentException exNhoHon = assertThrows(
                IllegalArgumentException.class,
                () -> service.capNhatSanPham(sp)
        );
        assertTrue(exNhoHon.getMessage().contains("Giá bán phải lớn hơn giá nhập"));

        // 3. Hợp lệ: Giá bán > Giá nhập > 0
        sp.setGiaNhap(new BigDecimal("15000"));
        sp.setGiaBan(new BigDecimal("20000"));
        assertTrue(service.capNhatSanPham(sp));
    }
}
