package com.petshop.bus;

import com.petshop.dao.NhaCungCapDAO;
import com.petshop.entity.NhaCungCap;
import com.petshop.entity.TaiKhoan;
import com.petshop.util.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NhaCungCapBUSTest {

    private NhaCungCapBUS bus;
    private FakeNhaCungCapDAO fakeDAO;

    @BeforeEach
    void setUp() {
        SessionManager.dangNhap(new TaiKhoan("NV-QL01", "quanly", "Quản lý kho", TaiKhoan.VaiTro.QUAN_LY));
        fakeDAO = new FakeNhaCungCapDAO();
        bus = new NhaCungCapBUS(fakeDAO, new XacThucBUS());
    }

    @AfterEach
    void tearDown() {
        SessionManager.dangXuat();
    }

    @Test
    void testThemNhaCungCapHopLe() {
        NhaCungCap ncc = new NhaCungCap(null, "Công ty TNHH PetCare", "0912345678", "123 Lê Lợi, Q1", "info@petcare.vn", true);
        boolean kq = bus.themNhaCungCap(ncc);
        assertTrue(kq);
        assertEquals("NCC-20261105-000001", ncc.getMaNhaCungCap());
    }

    @Test
    void testThemNhaCungCapTrungTenNemNgoaiLe() {
        fakeDAO.danhSach.add(new NhaCungCap("NCC-20261105-000001", "Pet City", "0901234567", "Hà Nội", "petcity@gmail.com", true));

        NhaCungCap nccMoi = new NhaCungCap(null, "Pet City", "0988888888", "TP.HCM", "contact@petcity.vn", true);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> bus.themNhaCungCap(nccMoi));
        assertTrue(ex.getMessage().contains("đã tồn tại"), "Phải báo lỗi khi trùng tên nhà cung cấp");
    }

    @Test
    void testThemNhaCungCapTrungTenKhongPhanBietHoaThuong() {
        fakeDAO.danhSach.add(new NhaCungCap("NCC-20261105-000001", "Bio-Groom Việt Nam", "0901234567", "Hà Nội", "bio@groom.vn", true));

        NhaCungCap nccMoi = new NhaCungCap(null, "bio-groom việt nam", "0988888888", "TP.HCM", "test@groom.vn", true);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> bus.themNhaCungCap(nccMoi));
        assertTrue(ex.getMessage().contains("đã tồn tại"), "Phải báo lỗi khi trùng tên dù viết thường");
    }

    @Test
    void testThemNhaCungCapTrungSoDienThoaiNemNgoaiLe() {
        fakeDAO.danhSach.add(new NhaCungCap("NCC-20261105-000001", "Pet City", "0901234567", "Hà Nội", "petcity@gmail.com", true));

        NhaCungCap nccMoi = new NhaCungCap(null, "Nhà cung cấp mới", "0901234567", "TP.HCM", "new@gmail.com", true);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> bus.themNhaCungCap(nccMoi));
        assertTrue(ex.getMessage().contains("Số điện thoại"), "Phải báo lỗi khi trùng số điện thoại");
    }

    @Test
    void testCapNhatNhaCungCapGiuNguyenTenHopLe() {
        NhaCungCap nccHienTai = new NhaCungCap("NCC-20261105-000001", "Pet City", "0901234567", "Hà Nội", "petcity@gmail.com", true);
        fakeDAO.danhSach.add(nccHienTai);

        NhaCungCap capNhat = new NhaCungCap("NCC-20261105-000001", "Pet City", "0909999999", "Hà Nội mới", "petcity@gmail.com", true);
        assertDoesNotThrow(() -> bus.capNhatNhaCungCap(capNhat));
    }

    @Test
    void testCapNhatNhaCungCapTrungTenCuaNccKhacNemNgoaiLe() {
        fakeDAO.danhSach.add(new NhaCungCap("NCC-20261105-000001", "Pet City", "0901234567", "Hà Nội", "petcity@gmail.com", true));
        fakeDAO.danhSach.add(new NhaCungCap("NCC-20261105-000002", "Royal Canin", "0902222222", "TP.HCM", "royal@canin.vn", true));

        // Cập nhật NCC 2 đổi tên thành tên của NCC 1 ("Pet City")
        NhaCungCap capNhat = new NhaCungCap("NCC-20261105-000002", "Pet City", "0902222222", "TP.HCM", "royal@canin.vn", true);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> bus.capNhatNhaCungCap(capNhat));
        assertTrue(ex.getMessage().contains("đã tồn tại"), "Cập nhật trùng tên NCC khác phải báo lỗi");
    }

    @Test
    void testTimKiemNhaCungCapTheoDiaChi() {
        fakeDAO.danhSach.add(new NhaCungCap("NCC-20261105-000001", "Pet City", "0901234567", "12 Nguyễn Văn Bảo, Gò Vấp", "petcity@gmail.com", true));
        fakeDAO.danhSach.add(new NhaCungCap("NCC-20261105-000002", "Royal Canin", "0902222222", "Quận 7, TP.HCM", "royal@canin.vn", true));

        List<NhaCungCap> ketQua = bus.timKiemNhaCungCap("", "Gò Vấp", true);
        assertEquals(1, ketQua.size());
        assertEquals("NCC-20261105-000001", ketQua.get(0).getMaNhaCungCap());
    }

    private static class FakeNhaCungCapDAO extends NhaCungCapDAO {
        final List<NhaCungCap> danhSach = new ArrayList<>();

        @Override
        public String taoMaNhaCungCap() {
            return "NCC-20261105-000001";
        }

        @Override
        public List<NhaCungCap> timKiemNhaCungCap(String tuKhoa, String diaChi, Boolean trangThai) {
            return danhSach.stream().filter(n -> {
                if (trangThai != null && n.isTrangThai() != trangThai) return false;
                if (tuKhoa != null && !tuKhoa.isBlank()) {
                    String kw = tuKhoa.trim().toLowerCase();
                    boolean matchKw = n.getMaNhaCungCap().toLowerCase().contains(kw)
                            || n.getTenNhaCungCap().toLowerCase().contains(kw)
                            || n.getSoDienThoai().toLowerCase().contains(kw)
                            || (n.getEmail() != null && n.getEmail().toLowerCase().contains(kw))
                            || (n.getDiaChi() != null && n.getDiaChi().toLowerCase().contains(kw));
                    if (!matchKw) return false;
                }
                if (diaChi != null && !diaChi.isBlank()) {
                    String dc = diaChi.trim().toLowerCase();
                    if (n.getDiaChi() == null || !n.getDiaChi().toLowerCase().contains(dc)) return false;
                }
                return true;
            }).toList();
        }

        @Override
        public boolean kiemTraTrungTenNhaCungCap(String tenNhaCungCap, String maLoaiTru) {
            if (tenNhaCungCap == null) return false;
            return danhSach.stream().anyMatch(n ->
                    (maLoaiTru == null || !n.getMaNhaCungCap().equalsIgnoreCase(maLoaiTru))
                            && n.getTenNhaCungCap().trim().equalsIgnoreCase(tenNhaCungCap.trim()));
        }

        @Override
        public boolean kiemTraTrungSoDienThoai(String soDienThoai, String maLoaiTru) {
            if (soDienThoai == null) return false;
            return danhSach.stream().anyMatch(n ->
                    (maLoaiTru == null || !n.getMaNhaCungCap().equalsIgnoreCase(maLoaiTru))
                            && n.getSoDienThoai().trim().equalsIgnoreCase(soDienThoai.trim()));
        }

        @Override
        public boolean themNhaCungCap(NhaCungCap ncc) {
            danhSach.add(ncc);
            return true;
        }

        @Override
        public boolean capNhatNhaCungCap(NhaCungCap ncc) {
            return true;
        }
    }
}

