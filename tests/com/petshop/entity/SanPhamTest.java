package com.petshop.entity;

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

    @Test
    void testGiaNhapVaGiaBanKhoiTaoMacDinh() {
        SanPham sp = new SanPham();
        assertEquals(0.0, sp.getGiaNhap(), 0.001, "Giá nhập mặc định phải bằng 0");
        assertEquals(0.0, sp.getGiaBan(), 0.001, "Giá bán mặc định phải bằng 0");
    }

    @Test
    void testGiaNhapVaGiaBanSetter() {
        SanPham sp = new SanPham();
        sp.setGiaNhap(150000.0);
        sp.setGiaBan(220000.0);
        assertEquals(150000.0, sp.getGiaNhap(), 0.001);
        assertEquals(220000.0, sp.getGiaBan(), 0.001);
    }

    @Test
    void testPhanLoaiTinhTrangTonKho() {
        SanPham sp = new SanPham();
        sp.setNguongTonToiThieu(10);

        // Trường hợp 1: Tồn kho <= 0 -> Hết hàng
        sp.setSoLuongTon(0);
        assertTrue(sp.isHetHang());
        assertFalse(sp.isSapHetHang());
        assertFalse(sp.isConHang());
        assertEquals("Hết hàng", sp.getTinhTrangTonKho());

        // Trường hợp 2: 0 < Tồn kho <= Ngưỡng -> Sắp hết hàng
        sp.setSoLuongTon(10);
        assertFalse(sp.isHetHang());
        assertTrue(sp.isSapHetHang());
        assertFalse(sp.isConHang());
        assertEquals("Sắp hết hàng", sp.getTinhTrangTonKho());

        sp.setSoLuongTon(5);
        assertFalse(sp.isHetHang());
        assertTrue(sp.isSapHetHang());
        assertFalse(sp.isConHang());
        assertEquals("Sắp hết hàng", sp.getTinhTrangTonKho());

        // Trường hợp 3: Tồn kho > Ngưỡng -> Còn hàng
        sp.setSoLuongTon(11);
        assertFalse(sp.isHetHang());
        assertFalse(sp.isSapHetHang());
        assertTrue(sp.isConHang());
        assertEquals("Còn hàng", sp.getTinhTrangTonKho());

        sp.setSoLuongTon(100);
        assertFalse(sp.isHetHang());
        assertFalse(sp.isSapHetHang());
        assertTrue(sp.isConHang());
        assertEquals("Còn hàng", sp.getTinhTrangTonKho());
    }
}
