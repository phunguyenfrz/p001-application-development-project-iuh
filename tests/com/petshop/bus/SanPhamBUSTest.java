package com.petshop.bus;

import com.petshop.dao.SanPhamDAO;
import com.petshop.entity.SanPham;
import com.petshop.entity.TaiKhoan;
import com.petshop.util.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SanPhamBUSTest {

    private SanPhamBUS bus;

    @BeforeEach
    void setUp() {
        SessionManager.dangNhap(new TaiKhoan("NV-QL01", "quanly", "Quản lý kho", TaiKhoan.VaiTro.QUAN_LY));
        bus = new SanPhamBUS(new FakeSanPhamDAO(), new XacThucBUS());
    }

    @AfterEach
    void tearDown() {
        SessionManager.dangXuat();
    }

    @Test
    void testGiaNhapAmNemNgoaiLe() {
        SanPham sp = taoSanPhamHopLe();
        sp.setGiaNhap(-1000.0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> bus.themSanPham(sp));
        assertTrue(ex.getMessage().contains("Giá nhập"), "Phải báo lỗi giá nhập âm");
    }

    @Test
    void testGiaBanAmNemNgoaiLe() {
        SanPham sp = taoSanPhamHopLe();
        sp.setGiaNhap(10000.0);
        sp.setGiaBan(-5000.0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> bus.themSanPham(sp));
        assertTrue(ex.getMessage().contains("Giá bán"), "Phải báo lỗi giá bán âm");
    }

    @Test
    void testGiaBanNhoHonGiaNhapNemNgoaiLe() {
        SanPham sp = taoSanPhamHopLe();
        sp.setGiaNhap(100000.0);
        sp.setGiaBan(80000.0); // nhỏ hơn giá nhập

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> bus.themSanPham(sp));
        assertEquals("Giá bán phải lớn hơn hoặc bằng giá nhập!", ex.getMessage());
    }

    @Test
    void testGiaBanBangGiaNhapHopLe() {
        SanPham sp = taoSanPhamHopLe();
        sp.setGiaNhap(100000.0);
        sp.setGiaBan(100000.0);

        assertDoesNotThrow(() -> bus.themSanPham(sp));
    }

    @Test
    void testGiaBanLonHonGiaNhapHopLe() {
        SanPham sp = taoSanPhamHopLe();
        sp.setGiaNhap(100000.0);
        sp.setGiaBan(150000.0);

        assertDoesNotThrow(() -> bus.themSanPham(sp));
    }

    private SanPham taoSanPhamHopLe() {
        SanPham sp = new SanPham();
        sp.setMaSanPham("SP-20261105-000001");
        sp.setTenSanPham("Sữa tắm cho chó mèo Joyce & Dolls 400ml");
        sp.setMaVach("893000000001");
        sp.setGiaNhap(120000.0);
        sp.setGiaBan(180000.0);
        sp.setSoLuongTon(20);
        sp.setNguongTonToiThieu(5);
        sp.setDonViTinh("Chai");
        sp.setTrangThai(true);
        return sp;
    }

    private static class FakeSanPhamDAO extends SanPhamDAO {
        @Override
        public String taoMaSanPham() {
            return "SP-20261105-000001";
        }

        @Override
        public boolean kiemTraTonTaiMaVach(String maVach, String maSanPhamHienTai) {
            return false;
        }

        @Override
        public boolean themSanPham(SanPham sp) {
            return true;
        }

        @Override
        public boolean capNhatSanPham(SanPham sp) {
            return true;
        }
    }
}

