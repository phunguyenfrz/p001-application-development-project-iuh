package com.petshop.model;

import org.junit.jupiter.api.Test;

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
}
