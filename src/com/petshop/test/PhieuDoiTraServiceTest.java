package com.petshop.test;

import com.petshop.model.ChiTietPhieuDoiTra;
import com.petshop.model.HoaDon;
import com.petshop.model.PhieuDoiTra;
import com.petshop.service.PhieuDoiTraService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Kiểm thử đơn vị (Unit Tests) cho phân hệ Phiếu đổi trả (PhieuDoiTraService).
 * Kiểm tra các quy tắc nghiệp vụ quan trọng:
 * - RET_CREATE / DT_TAO: Ma trận kiểm tra QL5, QL5a, QL5b
 * - QL26: Gợi ý phân loại hướng xử lý hàng nhận lại (Nhập lại kho bán, Trả nhà cung cấp, Tiêu hủy)
 * - Kiểm tra thời hạn 7 ngày và trạng thái thanh toán hóa đơn gốc
 */
public class PhieuDoiTraServiceTest {

    private static int soTestPass = 0;
    private static int soTestFail = 0;

    public static void main(String[] args) {
        System.out.println("=========================================================");
        System.out.println("  BẮT ĐẦU KIỂM THỬ ĐƠN VỊ: PHIEU_DOI_TRA_SERVICE_TEST");
        System.out.println("=========================================================");

        testQL5_LoiSanPhamVaHetHan_MoiTinhTrang();
        testQL5_KhongPhuHopVaKhac_ChiNguyenVen();
        testQL5_KhongPhuHop_DaMo_TuChoi();
        testQL5a_LyDoKhac_BatBuocGhiChu();
        testQL5b_LoiSanPham_DaMo_BatBuocGhiChu();
        testQL26_GoiYHuongXuLy_NhapKho();
        testQL26_GoiYHuongXuLy_TraNhaCungCap();
        testQL26_GoiYHuongXuLy_TieuHuy();
        testDieuKienHoaDon_QuaHan7Ngay_TuChoi();
        testDieuKienHoaDon_ChuaThanhToan_TuChoi();
        testDieuKienHoaDon_HopLe();

        System.out.println("=========================================================");
        System.out.println("  KẾT QUẢ KIỂM THỬ: " + soTestPass + " PASS / " + (soTestPass + soTestFail) + " TESTS");
        if (soTestFail == 0) {
            System.out.println("  ==> TẤT CẢ CÁC TEST CASES ĐÃ ĐẠT CHUẨN 100%! <===");
        } else {
            System.err.println("  ==> CÓ " + soTestFail + " TEST BỊ THẤT BẠI! CẦN KIỂM TRA LẠI.");
        }
        System.out.println("=========================================================");
    }

    private static void ghiNhanKetQua(String tenTest, boolean thanhCong, String thongBao) {
        if (thanhCong) {
            soTestPass++;
            System.out.println("  [PASS] " + tenTest);
        } else {
            soTestFail++;
            System.err.println("  [FAIL] " + tenTest + " -> " + thongBao);
        }
    }

    // 1. QL5: Lỗi sản phẩm / Hết hạn cho phép ở mọi tình trạng
    private static void testQL5_LoiSanPhamVaHetHan_MoiTinhTrang() {
        PhieuDoiTraService service = new PhieuDoiTraService();
        try {
            service.kiemTraQuyTacMaTran("Lỗi sản phẩm", "Nguyên vẹn", null);
            service.kiemTraQuyTacMaTran("Lỗi sản phẩm", "Đã mở", "Mô tả lỗi sản phẩm bị nứt vỏ");
            service.kiemTraQuyTacMaTran("Lỗi sản phẩm", "Hư hỏng", "Rách bao bì do sản xuất");
            service.kiemTraQuyTacMaTran("Hết hạn", "Đã mở", null);
            service.kiemTraQuyTacMaTran("Hết hạn", "Nguyên vẹn", null);
            ghiNhanKetQua("testQL5_LoiSanPhamVaHetHan_MoiTinhTrang", true, null);
        } catch (Exception e) {
            ghiNhanKetQua("testQL5_LoiSanPhamVaHetHan_MoiTinhTrang", false, e.getMessage());
        }
    }

    // 2. QL5: Không phù hợp / Khác chỉ cho phép khi Nguyên vẹn
    private static void testQL5_KhongPhuHopVaKhac_ChiNguyenVen() {
        PhieuDoiTraService service = new PhieuDoiTraService();
        try {
            service.kiemTraQuyTacMaTran("Không phù hợp", "Nguyên vẹn", null);
            service.kiemTraQuyTacMaTran("Khác", "Nguyên vẹn", "Khách đổi kích thước lớn hơn");
            ghiNhanKetQua("testQL5_KhongPhuHopVaKhac_ChiNguyenVen", true, null);
        } catch (Exception e) {
            ghiNhanKetQua("testQL5_KhongPhuHopVaKhac_ChiNguyenVen", false, e.getMessage());
        }
    }

    // 3. QL5: Không phù hợp nhưng hàng Đã mở -> Bắt buộc từ chối
    private static void testQL5_KhongPhuHop_DaMo_TuChoi() {
        PhieuDoiTraService service = new PhieuDoiTraService();
        try {
            service.kiemTraQuyTacMaTran("Không phù hợp", "Đã mở", null);
            ghiNhanKetQua("testQL5_KhongPhuHop_DaMo_TuChoi", false, "Đáng lẽ phải ném ngoại lệ khi chọn Đã mở cho lý do Không phù hợp!");
        } catch (IllegalArgumentException e) {
            ghiNhanKetQua("testQL5_KhongPhuHop_DaMo_TuChoi", true, null);
        }
    }

    // 4. QL5a: Khi lý do là "Khác" -> bắt buộc phải nhập ghi chú
    private static void testQL5a_LyDoKhac_BatBuocGhiChu() {
        PhieuDoiTraService service = new PhieuDoiTraService();
        try {
            service.kiemTraQuyTacMaTran("Khác", "Nguyên vẹn", "");
            ghiNhanKetQua("testQL5a_LyDoKhac_BatBuocGhiChu", false, "Đáng lẽ phải ném ngoại lệ khi lý do 'Khác' để trống ghi chú!");
        } catch (IllegalArgumentException e) {
            ghiNhanKetQua("testQL5a_LyDoKhac_BatBuocGhiChu", true, null);
        }
    }

    // 5. QL5b: Khi "Lỗi sản phẩm" ở tình trạng "Đã mở" -> bắt buộc có ghi chú mô tả lỗi
    private static void testQL5b_LoiSanPham_DaMo_BatBuocGhiChu() {
        PhieuDoiTraService service = new PhieuDoiTraService();
        try {
            service.kiemTraQuyTacMaTran("Lỗi sản phẩm", "Đã mở", null);
            ghiNhanKetQua("testQL5b_LoiSanPham_DaMo_BatBuocGhiChu", false, "Đáng lẽ phải bắt buộc có ghi chú khi sản phẩm lỗi ở tình trạng đã mở!");
        } catch (IllegalArgumentException e) {
            ghiNhanKetQua("testQL5b_LoiSanPham_DaMo_BatBuocGhiChu", true, null);
        }
    }

    // 6. QL26: Nguyên vẹn + Không phù hợp/Khác -> Gợi ý "Nhập lại kho bán"
    private static void testQL26_GoiYHuongXuLy_NhapKho() {
        String huong1 = ChiTietPhieuDoiTra.tuDongGoiYHuongXuLy("Không phù hợp", "Nguyên vẹn");
        String huong2 = ChiTietPhieuDoiTra.tuDongGoiYHuongXuLy("Khác", "Nguyên vẹn");
        boolean ok = "Nhập lại kho bán".equals(huong1) && "Nhập lại kho bán".equals(huong2);
        ghiNhanKetQua("testQL26_GoiYHuongXuLy_NhapKho", ok, "Kết quả không khớp: " + huong1 + ", " + huong2);
    }

    // 7. QL26: Lỗi sản phẩm -> Gợi ý "Trả nhà cung cấp"
    private static void testQL26_GoiYHuongXuLy_TraNhaCungCap() {
        String huong1 = ChiTietPhieuDoiTra.tuDongGoiYHuongXuLy("Lỗi sản phẩm", "Nguyên vẹn");
        String huong2 = ChiTietPhieuDoiTra.tuDongGoiYHuongXuLy("Lỗi sản phẩm", "Đã mở");
        boolean ok = "Trả nhà cung cấp".equals(huong1) && "Trả nhà cung cấp".equals(huong2);
        ghiNhanKetQua("testQL26_GoiYHuongXuLy_TraNhaCungCap", ok, "Kết quả không khớp: " + huong1);
    }

    // 8. QL26: Hết hạn hoặc Hư hỏng -> Gợi ý "Tiêu hủy"
    private static void testQL26_GoiYHuongXuLy_TieuHuy() {
        String huong1 = ChiTietPhieuDoiTra.tuDongGoiYHuongXuLy("Hết hạn", "Nguyên vẹn");
        String huong2 = ChiTietPhieuDoiTra.tuDongGoiYHuongXuLy("Không phù hợp", "Hư hỏng");
        boolean ok = "Tiêu hủy".equals(huong1) && "Tiêu hủy".equals(huong2);
        ghiNhanKetQua("testQL26_GoiYHuongXuLy_TieuHuy", ok, "Kết quả không khớp: " + huong1 + ", " + huong2);
    }

    // 9. Hóa đơn quá hạn 7 ngày -> Từ chối
    private static void testDieuKienHoaDon_QuaHan7Ngay_TuChoi() {
        PhieuDoiTraService service = new PhieuDoiTraService();
        HoaDon hd = new HoaDon();
        hd.setMaHoaDon("HD-20261010-000001");
        hd.setNgayLap(LocalDate.now().minusDays(8));
        hd.setTrangThaiThanhToan("Đã thanh toán");

        try {
            service.kiemTraDieuKienHoaDon(hd);
            ghiNhanKetQua("testDieuKienHoaDon_QuaHan7Ngay_TuChoi", false, "Đáng lẽ phải từ chối hóa đơn quá 7 ngày!");
        } catch (IllegalArgumentException e) {
            ghiNhanKetQua("testDieuKienHoaDon_QuaHan7Ngay_TuChoi", true, null);
        }
    }

    // 10. Hóa đơn chưa thanh toán -> Từ chối
    private static void testDieuKienHoaDon_ChuaThanhToan_TuChoi() {
        PhieuDoiTraService service = new PhieuDoiTraService();
        HoaDon hd = new HoaDon();
        hd.setMaHoaDon("HD-20261101-000001");
        hd.setNgayLap(LocalDate.now());
        hd.setTrangThaiThanhToan("Chưa thanh toán");

        try {
            service.kiemTraDieuKienHoaDon(hd);
            ghiNhanKetQua("testDieuKienHoaDon_ChuaThanhToan_TuChoi", false, "Đáng lẽ phải từ chối hóa đơn chưa thanh toán!");
        } catch (IllegalArgumentException e) {
            ghiNhanKetQua("testDieuKienHoaDon_ChuaThanhToan_TuChoi", true, null);
        }
    }

    // 11. Hóa đơn hợp lệ trong 7 ngày, đã thanh toán và có sản phẩm -> Cho phép
    private static void testDieuKienHoaDon_HopLe() {
        PhieuDoiTraService service = new PhieuDoiTraService();
        HoaDon hd = new HoaDon();
        hd.setMaHoaDon("HD-20261101-000002");
        hd.setNgayLap(LocalDate.now().minusDays(3));
        hd.setTrangThaiThanhToan("Đã thanh toán");

        List<com.petshop.model.ChiTietHoaDonSanPham> dsSp = new ArrayList<>();
        com.petshop.model.ChiTietHoaDonSanPham ct = new com.petshop.model.ChiTietHoaDonSanPham();
        ct.setMaSanPham("SP-20261005-000001");
        ct.setTenSanPham("Thức ăn Royal Canin");
        ct.setSoLuong(2);
        ct.setDonGia(new BigDecimal("150000"));
        dsSp.add(ct);
        hd.setDanhSachSanPham(dsSp);

        try {
            service.kiemTraDieuKienHoaDon(hd);
            ghiNhanKetQua("testDieuKienHoaDon_HopLe", true, null);
        } catch (Exception e) {
            ghiNhanKetQua("testDieuKienHoaDon_HopLe", false, e.getMessage());
        }
    }
}
