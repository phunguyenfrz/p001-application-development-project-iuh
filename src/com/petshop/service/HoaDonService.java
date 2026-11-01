package com.petshop.service;

import com.petshop.dao.HoaDonDAO;
import com.petshop.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Tầng nghiệp vụ (Service Layer) cho phân hệ Quản lý Hóa đơn & Bán lẻ POS.
 * Tuân thủ quy ước bảo mật: kiểm tra phân quyền (HD_TAO, HD_TIM_KIEM, HD_CAP_NHAT, HD_DOANH_THU)
 * và toàn vẹn giao dịch (trừ tồn kho, áp khuyến mãi, cập nhật trạng thái phiếu dịch vụ).
 */
public class HoaDonService {

    private static final String REGEX_MA_HOA_DON = "^HD-\\d{8}-\\d{6}$";

    private final HoaDonDAO hoaDonDAO;
    private final SanPhamService sanPhamService;
    private final PhieuDichVuService phieuDichVuService;
    private final KhuyenMaiService khuyenMaiService;
    private final XacThucService xacThucService;

    public HoaDonService() {
        this(new HoaDonDAO(), new SanPhamService(), new PhieuDichVuService(), new KhuyenMaiService(), new XacThucService());
    }

    public HoaDonService(HoaDonDAO hoaDonDAO, SanPhamService sanPhamService, PhieuDichVuService phieuDichVuService,
                         KhuyenMaiService khuyenMaiService, XacThucService xacThucService) {
        this.hoaDonDAO = hoaDonDAO;
        this.sanPhamService = sanPhamService;
        this.phieuDichVuService = phieuDichVuService;
        this.khuyenMaiService = khuyenMaiService;
        this.xacThucService = xacThucService;
    }

    /**
     * Lập và thanh toán hóa đơn mới (INV_CREATE).
     * Yêu cầu quyền: HD_TAO.
     */
    public boolean lapHoaDon(HoaDon hoaDon) {
        kiemTraQuyenHoaDon("HD_TAO");

        if (hoaDon == null) {
            throw new IllegalArgumentException("Thông tin hóa đơn không được để trống!");
        }

        // Tự động sinh mã hóa đơn nếu chưa có
        if (hoaDon.getMaHoaDon() == null || hoaDon.getMaHoaDon().trim().isEmpty()) {
            hoaDon.setMaHoaDon(hoaDonDAO.taoMaHoaDon());
        }

        // Kiểm tra tính hợp lệ dữ liệu nghiệp vụ
        kiemTraHopLeHoaDon(hoaDon);

        // Áp dụng khuyến mãi nếu có
        BigDecimal tongGoc = hoaDon.tinhTongTienGoc();
        BigDecimal tienSauKm = tongGoc;
        if (hoaDon.getMaKhuyenMai() != null && !hoaDon.getMaKhuyenMai().trim().isEmpty()) {
            String maKm = hoaDon.getMaKhuyenMai().trim();
            if (!khuyenMaiService.kiemTraKhuyenMaiHopLe(maKm)) {
                throw new IllegalArgumentException("Mã khuyến mãi '" + maKm + "' không hợp lệ hoặc đã hết hạn!");
            }
            BigDecimal soTienGiam = khuyenMaiService.tinhSoTienGiam(maKm, tongGoc);
            hoaDon.setTienGiam(soTienGiam);
            tienSauKm = tongGoc.subtract(soTienGiam).max(BigDecimal.ZERO);
        } else {
            hoaDon.setTienGiam(BigDecimal.ZERO);
        }

        // Áp dụng điểm tích lũy nếu có (1 điểm = 1.000 VNĐ)
        boolean laKhachHang = hoaDon.getMaKhachHang() != null && !hoaDon.getMaKhachHang().trim().isEmpty();
        if (hoaDon.getDiemTichLuySuDung() > 0) {
            if (!laKhachHang) {
                throw new IllegalArgumentException("Khách vãng lai không thể sử dụng điểm tích lũy!");
            }
            // Làm tròn lên số điểm để có thể lấn át toàn bộ tiền thanh toán về 0đ
            int diemToiDa = (int) Math.ceil(tienSauKm.doubleValue() / 1000.0);
            if (hoaDon.getDiemTichLuySuDung() > diemToiDa) {
                hoaDon.setDiemTichLuySuDung(diemToiDa);
            }
            BigDecimal giaTriDiem = BigDecimal.valueOf(hoaDon.getDiemTichLuySuDung() * 1000L);
            BigDecimal tienGiamTuDiem = tienSauKm.min(giaTriDiem);
            hoaDon.setTienGiamTuDiem(tienGiamTuDiem);
        } else {
            hoaDon.setTienGiamTuDiem(BigDecimal.ZERO);
        }

        BigDecimal tongThucThu = tienSauKm.subtract(hoaDon.getTienGiamTuDiem()).max(BigDecimal.ZERO);
        hoaDon.setTongTien(tongThucThu);

        // Tính điểm tích lũy mới được cộng (Chỉ tính cho khách hàng chính thức, khách vãng lai = 0)
        int diemCong = tinhDiemTichLuy(tongThucThu, laKhachHang);
        hoaDon.setDiemTichLuyCong(diemCong);

        return hoaDonDAO.luuHoaDon(hoaDon);
    }

    /**
     * Quy tắc tính điểm tích lũy:
     * - Khách vãng lai: 0 điểm
     * - Khách hàng có tài khoản:
     *   + Hóa đơn < 1.000.000 VNĐ: cứ 100k tính 5 điểm
     *   + Hóa đơn >= 1.000.000 VNĐ: cứ 100k tính 10 điểm
     */
    public int tinhDiemTichLuy(BigDecimal soTien, boolean laKhachHang) {
        if (!laKhachHang || soTien == null || soTien.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        long tien = soTien.longValue();
        long soBlock100k = tien / 100_000L;
        if (tien >= 1_000_000L) {
            return (int) (soBlock100k * 10L);
        } else {
            return (int) (soBlock100k * 5L);
        }
    }

    /**
     * Hủy hóa đơn bán hàng và hoàn lại tồn kho sản phẩm (INV_CANCEL).
     * Yêu cầu quyền: HD_CAP_NHAT.
     */
    public boolean huyHoaDon(String maHoaDon, String lyDoHuy) {
        kiemTraQuyenHoaDon("HD_CAP_NHAT");

        if (maHoaDon == null || maHoaDon.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã hóa đơn cần hủy không được để trống!");
        }

        HoaDon hd = hoaDonDAO.layHoaDonTheoMa(maHoaDon.trim());
        if (hd == null) {
            throw new IllegalArgumentException("Không tìm thấy hóa đơn có mã: " + maHoaDon);
        }

        if ("Đã hủy".equalsIgnoreCase(hd.getTrangThaiThanhToan())) {
            throw new IllegalStateException("Hóa đơn '" + maHoaDon + "' đã bị hủy trước đó!");
        }

        return hoaDonDAO.huyHoaDon(maHoaDon.trim());
    }

    /**
     * Cập nhật hóa đơn đã thanh toán khi có thay đổi mặt hàng hoặc số lượng (INV_UPDATE).
     * Yêu cầu quyền: HD_CAP_NHAT.
     */
    public boolean capNhatHoaDon(HoaDon hoaDonCu, HoaDon hoaDonMoi, BigDecimal tienThanhToanThem, BigDecimal tienHoanLai) {
        kiemTraQuyenHoaDon("HD_CAP_NHAT");

        if (hoaDonCu == null || hoaDonMoi == null) {
            throw new IllegalArgumentException("Thông tin hóa đơn cần cập nhật không được để trống!");
        }

        if ("Đã hủy".equalsIgnoreCase(hoaDonCu.getTrangThaiThanhToan())) {
            throw new IllegalStateException("Không thể cập nhật hóa đơn đã bị hủy!");
        }

        // Kiểm tra hợp lệ các dòng mặt hàng mới
        if ((hoaDonMoi.getDanhSachSanPham() == null || hoaDonMoi.getDanhSachSanPham().isEmpty())
                && (hoaDonMoi.getDanhSachDichVu() == null || hoaDonMoi.getDanhSachDichVu().isEmpty())) {
            throw new IllegalArgumentException("Hóa đơn sau cập nhật phải có ít nhất một mặt hàng hoặc dịch vụ!");
        }

        // Tính lại điểm tích lũy mới cho khách hàng nếu có
        boolean laKhachHang = hoaDonMoi.getMaKhachHang() != null && !hoaDonMoi.getMaKhachHang().trim().isEmpty();
        int diemMoi = tinhDiemTichLuy(hoaDonMoi.getTongTien(), laKhachHang);
        hoaDonMoi.setDiemTichLuyCong(diemMoi);

        return hoaDonDAO.capNhatHoaDon(hoaDonCu, hoaDonMoi, tienThanhToanThem, tienHoanLai);
    }

    /**
     * Tra cứu chi tiết một hóa đơn theo mã.
     * Yêu cầu quyền: HD_TIM_KIEM hoặc HD_TAO hoặc HD_CAP_NHAT.
     */
    public HoaDon layHoaDonTheoMa(String maHoaDon) {
        if (maHoaDon == null || maHoaDon.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã hóa đơn không được để trống!");
        }
        return hoaDonDAO.layHoaDonTheoMa(maHoaDon.trim());
    }

    /**
     * Tra cứu danh sách hóa đơn theo các tiêu chí tìm kiếm (INV_SEARCH).
     * Yêu cầu quyền: HD_TIM_KIEM.
     */
    public List<HoaDon> timKiemHoaDon(String tuKhoa, LocalDate tuNgay, LocalDate denNgay, String hinhThucThanhToan, String trangThai) {
        kiemTraQuyenHoaDon("HD_TIM_KIEM");
        if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
            throw new IllegalArgumentException("Từ ngày không được lớn hơn Đến ngày!");
        }
        return hoaDonDAO.timKiemHoaDon(tuKhoa, tuNgay, denNgay, hinhThucThanhToan, trangThai);
    }

    /**
     * Lấy toàn bộ danh sách hóa đơn hiện có trong cơ sở dữ liệu.
     */
    public List<HoaDon> layTatCaHoaDon() {
        kiemTraQuyenHoaDon("HD_TIM_KIEM");
        return hoaDonDAO.layTatCaHoaDon();
    }

    /**
     * Lấy danh sách các Phiếu dịch vụ chưa thanh toán để nạp nhanh vào hóa đơn.
     */
    public List<PhieuDichVu> layDanhSachPhieuDichVuChuaThanhToan() {
        return hoaDonDAO.layDanhSachPhieuDichVuChuaThanhToan();
    }

    /**
     * Lấy danh sách phiếu dịch vụ trong ngày của khách hàng mà chưa được thanh toán.
     * Tự động xuất thông tin phiếu dịch vụ khi nhân viên nhập số điện thoại khách hàng.
     */
    public List<PhieuDichVu> layPhieuDichVuTrongNgayCuaKhachHang(String maKhachHang, LocalDate ngay) {
        return hoaDonDAO.layPhieuDichVuTrongNgayCuaKhachHang(maKhachHang, ngay);
    }

    /**
     * Lấy chi tiết phiếu dịch vụ kèm đầy đủ các dòng dịch vụ để nạp vào hóa đơn.
     */
    public PhieuDichVu layPhieuDichVuDeThanhToan(String maPhieuDichVu) {
        return phieuDichVuService.layPhieuDichVuDeThanhToan(maPhieuDichVu);
    }

    /**
     * Tự động sinh mã hóa đơn mới theo ngày.
     */
    public String taoMaHoaDonMoi() {
        return hoaDonDAO.taoMaHoaDon();
    }

    /**
     * Lấy báo cáo thống kê doanh thu (INV_REVENUE).
     * Yêu cầu quyền: HD_DOANH_THU.
     */
    public Map<String, Object> layBaoCaoDoanhThu(LocalDate tuNgay, LocalDate denNgay) {
        kiemTraQuyenHoaDon("HD_DOANH_THU");
        return hoaDonDAO.layBaoCaoDoanhThu(tuNgay, denNgay);
    }

    public Map<String, Object> layBaoCaoDoanhThu(LocalDate tuNgay, LocalDate denNgay, String trangThai) {
        kiemTraQuyenHoaDon("HD_DOANH_THU");
        return hoaDonDAO.layBaoCaoDoanhThu(tuNgay, denNgay, trangThai);
    }

    public List<Map<String, Object>> layBaoCaoSanPhamBanRa(LocalDate tuNgay, LocalDate denNgay) {
        kiemTraQuyenHoaDon("TK_DOANH_THU");
        return hoaDonDAO.layBaoCaoSanPhamBanRa(tuNgay, denNgay);
    }

    public List<Map<String, Object>> layBaoCaoDichVuThucHien(LocalDate tuNgay, LocalDate denNgay) {
        kiemTraQuyenHoaDon("TK_DOANH_THU");
        return hoaDonDAO.layBaoCaoDichVuThucHien(tuNgay, denNgay);
    }

    // =========================================================================
    // HÀM KIỂM TRA RÀNG BUỘC (VALIDATION)
    // =========================================================================

    private void kiemTraHopLeHoaDon(HoaDon hoaDon) {
        if (!hoaDon.getMaHoaDon().matches(REGEX_MA_HOA_DON)) {
            throw new IllegalArgumentException("Mã hóa đơn không đúng định dạng chuẩn (HD-yyyyMMdd-xxxxxx)!");
        }

        if (hoaDon.getMaNhanVien() == null || hoaDon.getMaNhanVien().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng xác định nhân viên lập hóa đơn!");
        }

        boolean coSanPham = hoaDon.getDanhSachSanPham() != null && !hoaDon.getDanhSachSanPham().isEmpty();
        boolean coDichVu = hoaDon.getDanhSachDichVu() != null && !hoaDon.getDanhSachDichVu().isEmpty();

        if (!coSanPham && !coDichVu) {
            throw new IllegalArgumentException("Hóa đơn phải có ít nhất 1 sản phẩm hoặc 1 dịch vụ chăm sóc!");
        }

        // Kiểm tra ràng buộc phiếu dịch vụ nếu có: Phải ở trạng thái "Hoàn thành" mới được thanh toán
        if (hoaDon.getMaPhieuDichVu() != null && !hoaDon.getMaPhieuDichVu().trim().isEmpty()) {
            PhieuDichVu phieu = phieuDichVuService.layPhieuDichVuTheoMa(hoaDon.getMaPhieuDichVu().trim());
            if (phieu != null && !"Hoàn thành".equalsIgnoreCase(phieu.getTrangThai())) {
                throw new IllegalArgumentException("Phiếu dịch vụ '" + hoaDon.getMaPhieuDichVu()
                        + "' hiện đang ở trạng thái '" + phieu.getTrangThai()
                        + "'. Chỉ khi phiếu dịch vụ ở trạng thái 'Hoàn thành' thì mới được thanh toán hóa đơn!");
            }
        }

        // Kiểm tra từng sản phẩm và tồn kho
        if (coSanPham) {
            for (ChiTietHoaDonSanPham sp : hoaDon.getDanhSachSanPham()) {
                if (sp.getSoLuong() <= 0) {
                    throw new IllegalArgumentException("Số lượng mua cho sản phẩm '" + sp.getMaSanPham() + "' phải lớn hơn 0!");
                }
                if (sp.getDonGia() == null || sp.getDonGia().compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Đơn giá sản phẩm '" + sp.getMaSanPham() + "' không được âm!");
                }

                // Kiểm tra tồn kho thực tế
                int tonKho = sanPhamService.traCuuTonKho(sp.getMaSanPham());
                if (tonKho < sp.getSoLuong()) {
                    throw new IllegalArgumentException("Sản phẩm '" + (sp.getTenSanPham() != null ? sp.getTenSanPham() : sp.getMaSanPham())
                            + "' chỉ còn " + tonKho + " trong kho, không đủ để bán " + sp.getSoLuong() + "!");
                }
            }
        }

        // Kiểm tra từng dịch vụ
        if (coDichVu) {
            for (ChiTietHoaDonDichVu dv : hoaDon.getDanhSachDichVu()) {
                if (dv.getDonGia() == null || dv.getDonGia().compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Đơn giá dịch vụ '" + dv.getMaDichVu() + "' không được âm!");
                }
            }
        }

        // Kiểm tra hình thức thanh toán (chỉ hỗ trợ Tiền mặt và Chuyển khoản)
        String ht = hoaDon.getHinhThucThanhToan();
        if (ht == null || (!"Tiền mặt".equalsIgnoreCase(ht) && !"Chuyển khoản".equalsIgnoreCase(ht))) {
            throw new IllegalArgumentException("Hình thức thanh toán chỉ hỗ trợ 'Tiền mặt' hoặc 'Chuyển khoản'!");
        }
    }

    private void kiemTraQuyenHoaDon(String maQuyenChinh) {
        if (xacThucService == null) return;
        try {
            xacThucService.kiemTraQuyen(maQuyenChinh);
        } catch (SecurityException e) {
            // Hỗ trợ mã quyền alias INV_...
            String alias = switch (maQuyenChinh) {
                case "HD_TAO" -> "INV_CREATE";
                case "HD_TIM_KIEM" -> "INV_SEARCH";
                case "HD_CAP_NHAT" -> "INV_CANCEL";
                case "HD_DOANH_THU", "TK_DOANH_THU" -> "INV_REVENUE";
                default -> null;
            };
            if (alias != null) {
                try {
                    xacThucService.kiemTraQuyen(alias);
                    return;
                } catch (SecurityException ignored) {}
            }
            if ("TK_DOANH_THU".equals(maQuyenChinh)) {
                try {
                    xacThucService.kiemTraQuyen("HD_DOANH_THU");
                    return;
                } catch (SecurityException ignored) {}
            }
            throw e;
        }
    }
}
