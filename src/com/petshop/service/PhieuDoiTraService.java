package com.petshop.service;

import com.petshop.dao.HoaDonDAO;
import com.petshop.dao.PhieuDoiTraDAO;
import com.petshop.dao.SanPhamDAO;
import com.petshop.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Lớp nghiệp vụ cho Phiếu đổi trả (PhieuDoiTraService).
 * Quản lý quy tắc nghiệp vụ:
 * - QL4, QL6: Hóa đơn hợp lệ (phải thanh toán, trong 7 ngày, chỉ đổi trả sản phẩm).
 * - QL5: Ma trận kiểm tra Lý do & Tình trạng hàng.
 * - QL5a, QL5b: Ràng buộc ghi chú giải thích.
 * - QL17: Tính toán tiền hoàn / tiền bù.
 * - QL26: Gợi ý hướng xử lý hàng nhận lại.
 * - Phân quyền: RET_CREATE, RET_CONFIRM, RET_SEARCH.
 */
public class PhieuDoiTraService {

    public static final int SO_NGAY_DOI_TRA_TOI_DA = 7;
    public static final BigDecimal NGUONG_DUYET_QUAN_LY = new BigDecimal("2000000");

    private final PhieuDoiTraDAO phieuDoiTraDAO;
    private final HoaDonDAO hoaDonDAO;
    private final SanPhamDAO sanPhamDAO;
    private final XacThucService xacThucService;

    public PhieuDoiTraService() {
        this.phieuDoiTraDAO = new PhieuDoiTraDAO();
        this.hoaDonDAO = new HoaDonDAO();
        this.sanPhamDAO = new SanPhamDAO();
        this.xacThucService = new XacThucService();
    }

    public PhieuDoiTraService(PhieuDoiTraDAO phieuDoiTraDAO, HoaDonDAO hoaDonDAO,
                             SanPhamDAO sanPhamDAO, XacThucService xacThucService) {
        this.phieuDoiTraDAO = phieuDoiTraDAO;
        this.hoaDonDAO = hoaDonDAO;
        this.sanPhamDAO = sanPhamDAO;
        this.xacThucService = xacThucService;
    }

    /**
     * Sinh mã phiếu đổi trả tự động.
     */
    public String sinhMaPhieu() {
        return phieuDoiTraDAO.sinhMaPhieu();
    }

    private void kiemTraQuyenDoiTra(String maQuyenChinh) {
        if (xacThucService == null) return;
        try {
            xacThucService.kiemTraQuyen(maQuyenChinh);
        } catch (SecurityException e) {
            String alias = switch (maQuyenChinh) {
                case "RET_CREATE" -> "DT_TAO";
                case "RET_CONFIRM" -> "DT_XU_LY";
                case "RET_SEARCH" -> "DT_TIM_KIEM";
                default -> null;
            };
            if (alias != null) {
                try {
                    xacThucService.kiemTraQuyen(alias);
                    return;
                } catch (SecurityException ignored) {}
            }
            throw e;
        }
    }

    /**
     * Tra cứu và kiểm tra hóa đơn gốc để lập phiếu đổi trả.
     */
    public HoaDon traCuuHoaDonDeDoiTra(String tuKhoa) {
        kiemTraQuyenDoiTra("RET_CREATE");

        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập Mã hóa đơn, Số điện thoại hoặc Mã khách hàng để tìm kiếm!");
        }

        String keyword = tuKhoa.trim();
        HoaDon hd = hoaDonDAO.layHoaDonTheoMa(keyword);

        if (hd == null) {
            List<HoaDon> ds = hoaDonDAO.timKiemHoaDon(keyword, null, null, "Tất cả", "Tất cả");
            if (!ds.isEmpty()) {
                hd = hoaDonDAO.layHoaDonTheoMa(ds.get(0).getMaHoaDon());
            }
        }

        if (hd == null) {
            throw new IllegalArgumentException("Không tìm thấy hóa đơn gốc khớp với thông tin: " + keyword);
        }

        kiemTraDieuKienHoaDon(hd);
        return hd;
    }

    /**
     * Kiểm tra điều kiện hóa đơn có được phép đổi trả hay không.
     */
    public void kiemTraDieuKienHoaDon(HoaDon hd) {
        if (!"Đã thanh toán".equalsIgnoreCase(hd.getTrangThaiThanhToan())) {
            throw new IllegalArgumentException("Hóa đơn '" + hd.getMaHoaDon() + "' chưa hoàn tất thanh toán hoặc đã bị hủy, không thể đổi trả!");
        }

        if (hd.getNgayLap() != null) {
            long soNgay = ChronoUnit.DAYS.between(hd.getNgayLap(), LocalDate.now());
            if (soNgay > SO_NGAY_DOI_TRA_TOI_DA) {
                throw new IllegalArgumentException("Hóa đơn '" + hd.getMaHoaDon() + "' đã mua cách đây " + soNgay
                        + " ngày, đã vượt quá thời hạn đổi trả quy định (" + SO_NGAY_DOI_TRA_TOI_DA + " ngày)!");
            }
        }

        if (hd.getDanhSachSanPham() == null || hd.getDanhSachSanPham().isEmpty()) {
            throw new IllegalArgumentException("Hóa đơn '" + hd.getMaHoaDon() + "' là hóa đơn dịch vụ chăm sóc thú cưng, không có sản phẩm nào để đổi trả!");
        }
    }

    /**
     * Kiểm tra ma trận quy tắc đổi trả QL5 cho từng dòng sản phẩm:
     * - "Lỗi sản phẩm" hoặc "Hết hạn": cho phép ở mọi tình trạng (Nguyên vẹn, Đã mở, Hư hỏng).
     * - "Không phù hợp" hoặc "Khác": chỉ cho phép khi tình trạng là "Nguyên vẹn".
     * - QL5a: Khi lý do là "Khác" -> bắt buộc có ghi chú giải thích.
     * - QL5b: Khi lý do là "Lỗi sản phẩm" mà đã mở/hư hỏng -> bắt buộc có ghi chú giải thích.
     */
    public void kiemTraQuyTacMaTran(String lyDo, String tinhTrang, String ghiChu) {
        if (lyDo == null || lyDo.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn lý do đổi trả!");
        }
        if (tinhTrang == null || tinhTrang.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn tình trạng mặt hàng!");
        }

        boolean laKhongPhuHopHoacKhac = "Không phù hợp".equalsIgnoreCase(lyDo) || "Khác".equalsIgnoreCase(lyDo);
        if (laKhongPhuHopHoacKhac && !"Nguyên vẹn".equalsIgnoreCase(tinhTrang)) {
            throw new IllegalArgumentException("Lý do '" + lyDo + "' chỉ được đổi trả khi sản phẩm còn nguyên vẹn! Không chấp nhận hàng đã mở hoặc hư hỏng.");
        }

        if ("Khác".equalsIgnoreCase(lyDo)) {
            if (ghiChu == null || ghiChu.trim().isEmpty()) {
                throw new IllegalArgumentException("Khi chọn lý do 'Khác', bắt buộc phải nhập ghi chú giải thích chi tiết!");
            }
        }

        if ("Lỗi sản phẩm".equalsIgnoreCase(lyDo) && !"Nguyên vẹn".equalsIgnoreCase(tinhTrang)) {
            if (ghiChu == null || ghiChu.trim().isEmpty()) {
                throw new IllegalArgumentException("Sản phẩm lỗi ở tình trạng '" + tinhTrang + "' bắt buộc phải nhập ghi chú mô tả chi tiết lỗi!");
            }
        }
    }

    /**
     * Kiểm tra hợp lệ chi tiết mặt hàng theo đối tượng ChiTietPhieuDoiTra.
     */
    public void kiemTraHopLeChiTiet(ChiTietPhieuDoiTra ct) {
        if (ct == null) {
            throw new IllegalArgumentException("Mặt hàng đổi trả không được để trống!");
        }
        kiemTraQuyTacMaTran(ct.getLyDo(), ct.getTinhTrang(), ct.getGhiChu());
    }

    /**
     * Lấy số lượng sản phẩm đã đổi trả trước đó trong hóa đơn.
     */
    public int laySoLuongDaDoiTra(String maHoaDon, String maSanPham) {
        return phieuDoiTraDAO.laySoLuongDaDoiTra(maHoaDon, maSanPham);
    }

    /**
     * Lập phiếu đổi trả mới và lưu vào cơ sở dữ liệu (RET_CREATE).
     */
    public boolean lapPhieuDoiTra(PhieuDoiTra phieu) {
        kiemTraQuyenDoiTra("RET_CREATE");

        if (phieu == null) {
            throw new IllegalArgumentException("Dữ liệu phiếu đổi trả không được để trống!");
        }
        if (phieu.getMaHoaDon() == null || phieu.getMaHoaDon().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn hóa đơn gốc cần đổi trả!");
        }

        HoaDon hdGoc = hoaDonDAO.layHoaDonTheoMa(phieu.getMaHoaDon());
        if (hdGoc == null) {
            throw new IllegalArgumentException("Không tìm thấy hóa đơn gốc có mã: " + phieu.getMaHoaDon());
        }
        kiemTraDieuKienHoaDon(hdGoc);

        if (phieu.getDanhSachSanPham() == null || phieu.getDanhSachSanPham().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một sản phẩm cần đổi trả!");
        }

        int tongSoLuongTra = 0;
        BigDecimal tongTienTra = BigDecimal.ZERO;
        BigDecimal tongTienDoi = BigDecimal.ZERO;

        for (ChiTietPhieuDoiTra ct : phieu.getDanhSachSanPham()) {
            if (ct.getSoLuong() <= 0) {
                throw new IllegalArgumentException("Số lượng đổi trả của sản phẩm '" + ct.getTenSanPham() + "' phải lớn hơn 0!");
            }

            // Kiểm tra theo ma trận QL5, QL5a, QL5b
            kiemTraQuyTacMaTran(ct.getLyDo(), ct.getTinhTrang(), ct.getGhiChu());

            // Tự động gán hướng xử lý hàng nhận lại theo QL26 nếu chưa có
            if (ct.getHuongXuLy() == null || ct.getHuongXuLy().trim().isEmpty()) {
                ct.setHuongXuLy(ChiTietPhieuDoiTra.tuDongGoiYHuongXuLy(ct.getLyDo(), ct.getTinhTrang()));
            }

            // Kiểm tra không vượt quá số lượng đã mua trừ đi số lượng đã đổi trả trước đó
            int soLuongDaMua = 0;
            for (ChiTietHoaDonSanPham spMua : hdGoc.getDanhSachSanPham()) {
                if (spMua.getMaSanPham().equalsIgnoreCase(ct.getMaSanPham())) {
                    soLuongDaMua += spMua.getSoLuong();
                }
            }

            int soLuongDaTraTruoc = phieuDoiTraDAO.laySoLuongDaDoiTra(phieu.getMaHoaDon(), ct.getMaSanPham());
            int soLuongToiDaConLai = soLuongDaMua - soLuongDaTraTruoc;

            if (ct.getSoLuong() > soLuongToiDaConLai) {
                throw new IllegalArgumentException("Sản phẩm '" + ct.getTenSanPham() + "' đã mua " + soLuongDaMua
                        + ", đã đổi trả trước đó " + soLuongDaTraTruoc + ". Số lượng trả lần này (" + ct.getSoLuong()
                        + ") vượt quá số lượng tối đa được phép đổi trả (" + soLuongToiDaConLai + ")!");
            }

            // Nếu là đổi hàng, kiểm tra tồn kho sản phẩm đổi mới
            if ("Đổi sang sản phẩm khác".equalsIgnoreCase(phieu.getLoaiXuLy())) {
                if (ct.getMaSanPhamDoi() != null && !ct.getMaSanPhamDoi().trim().isEmpty()) {
                    if (ct.getSoLuongDoi() <= 0) {
                        throw new IllegalArgumentException("Vui lòng nhập số lượng sản phẩm đổi lấy mới cho '" + ct.getTenSanPham() + "'!");
                    }
                    int tonKho = sanPhamDAO.traCuuTonKho(ct.getMaSanPhamDoi());
                    if (tonKho < ct.getSoLuongDoi()) {
                        throw new IllegalArgumentException("Sản phẩm đổi mới '" + ct.getTenSanPhamDoi() + "' chỉ còn tồn kho "
                                + tonKho + ", không đủ số lượng đổi (" + ct.getSoLuongDoi() + ")!");
                    }
                    tongTienDoi = tongTienDoi.add(ct.tinhThanhTienDoi());
                }
            }

            tongSoLuongTra += ct.getSoLuong();
            tongTienTra = tongTienTra.add(ct.tinhThanhTienTra());
        }

        phieu.setSoLuong(tongSoLuongTra);

        // Tính toán tiền hoàn / tiền bù
        if ("Đổi sang sản phẩm khác".equalsIgnoreCase(phieu.getLoaiXuLy())) {
            BigDecimal chenhLech = tongTienDoi.subtract(tongTienTra);
            if (chenhLech.compareTo(BigDecimal.ZERO) > 0) {
                // Khách cần bù thêm tiền
                phieu.setTienBu(chenhLech);
                phieu.setTienHoan(BigDecimal.ZERO);
            } else if (chenhLech.compareTo(BigDecimal.ZERO) < 0) {
                // Cửa hàng hoàn lại tiền cho khách
                phieu.setTienHoan(chenhLech.abs());
                phieu.setTienBu(BigDecimal.ZERO);
            } else {
                phieu.setTienHoan(BigDecimal.ZERO);
                phieu.setTienBu(BigDecimal.ZERO);
            }
        } else {
            // Trả hàng hoàn tiền 100% giá trị hàng trả
            phieu.setTienHoan(tongTienTra);
            phieu.setTienBu(BigDecimal.ZERO);
        }

        if (phieu.getMaPhieuDoiTra() == null || phieu.getMaPhieuDoiTra().trim().isEmpty()) {
            phieu.setMaPhieuDoiTra(sinhMaPhieu());
        }

        return phieuDoiTraDAO.themPhieuDoiTra(phieu);
    }

    /**
     * Cập nhật thông tin phiếu đổi trả khi ở trạng thái Chờ xử lý.
     * Phiếu đã duyệt/từ chối thì không được sửa đổi.
     */
    public boolean capNhatPhieuDoiTra(PhieuDoiTra phieu) {
        kiemTraQuyenDoiTra("RET_CREATE");

        if (phieu == null || phieu.getMaPhieuDoiTra() == null) {
            throw new IllegalArgumentException("Dữ liệu phiếu đổi trả không hợp lệ!");
        }

        PhieuDoiTra phieuCu = phieuDoiTraDAO.layPhieuDoiTraTheoMa(phieu.getMaPhieuDoiTra());
        if (phieuCu == null) {
            throw new IllegalArgumentException("Không tìm thấy phiếu đổi trả: " + phieu.getMaPhieuDoiTra());
        }

        if (!"Chờ xử lý".equalsIgnoreCase(phieuCu.getTrangThai())) {
            throw new IllegalArgumentException("Phiếu '" + phieu.getMaPhieuDoiTra() + "' đã được xác nhận ("
                    + phieuCu.getTrangThai() + "), không thể chỉnh sửa!");
        }

        if (phieu.getDanhSachSanPham() == null || phieu.getDanhSachSanPham().isEmpty()) {
            throw new IllegalArgumentException("Phiếu đổi trả phải có ít nhất một mặt hàng!");
        }

        HoaDon hdGoc = hoaDonDAO.layHoaDonTheoMa(phieu.getMaHoaDon());
        int tongSoLuongTra = 0;
        BigDecimal tongTienTra = BigDecimal.ZERO;
        BigDecimal tongTienDoi = BigDecimal.ZERO;

        for (ChiTietPhieuDoiTra ct : phieu.getDanhSachSanPham()) {
            if (ct.getSoLuong() <= 0) {
                throw new IllegalArgumentException("Số lượng trả của '" + ct.getTenSanPham() + "' phải > 0!");
            }
            kiemTraQuyTacMaTran(ct.getLyDo(), ct.getTinhTrang(), ct.getGhiChu());

            if (ct.getHuongXuLy() == null || ct.getHuongXuLy().trim().isEmpty()) {
                ct.setHuongXuLy(ChiTietPhieuDoiTra.tuDongGoiYHuongXuLy(ct.getLyDo(), ct.getTinhTrang()));
            }

            tongSoLuongTra += ct.getSoLuong();
            tongTienTra = tongTienTra.add(ct.tinhThanhTienTra());
            if ("Đổi sang sản phẩm khác".equalsIgnoreCase(phieu.getLoaiXuLy()) && ct.getMaSanPhamDoi() != null) {
                tongTienDoi = tongTienDoi.add(ct.tinhThanhTienDoi());
            }
        }

        phieu.setSoLuong(tongSoLuongTra);
        if ("Đổi sang sản phẩm khác".equalsIgnoreCase(phieu.getLoaiXuLy())) {
            BigDecimal chenhLech = tongTienDoi.subtract(tongTienTra);
            if (chenhLech.compareTo(BigDecimal.ZERO) > 0) {
                phieu.setTienBu(chenhLech);
                phieu.setTienHoan(BigDecimal.ZERO);
            } else if (chenhLech.compareTo(BigDecimal.ZERO) < 0) {
                phieu.setTienHoan(chenhLech.abs());
                phieu.setTienBu(BigDecimal.ZERO);
            } else {
                phieu.setTienHoan(BigDecimal.ZERO);
                phieu.setTienBu(BigDecimal.ZERO);
            }
        } else {
            phieu.setTienHoan(tongTienTra);
            phieu.setTienBu(BigDecimal.ZERO);
        }

        return phieuDoiTraDAO.capNhatPhieuDoiTra(phieu);
    }

    /**
     * Duyệt phiếu đổi trả (RET_CONFIRM).
     * Phiếu giá trị lớn (> 2.000.000 VNĐ) bắt buộc vai trò Quản lý / ADMIN duyệt.
     */
    public boolean duyetPhieuDoiTra(String maPhieuDoiTra) {
        kiemTraQuyenDoiTra("RET_CONFIRM");

        PhieuDoiTra phieu = phieuDoiTraDAO.layPhieuDoiTraTheoMa(maPhieuDoiTra);
        if (phieu == null) {
            throw new IllegalArgumentException("Không tìm thấy phiếu đổi trả: " + maPhieuDoiTra);
        }

        if (!"Chờ xử lý".equalsIgnoreCase(phieu.getTrangThai())) {
            throw new IllegalArgumentException("Phiếu '" + maPhieuDoiTra + "' hiện đang ở trạng thái '"
                    + phieu.getTrangThai() + "', không thể duyệt lại!");
        }

        // Kiểm tra ngưỡng giá trị lớn
        BigDecimal tongGiaTri = phieu.getTienHoan().add(phieu.getTienBu());
        if (tongGiaTri.compareTo(NGUONG_DUYET_QUAN_LY) > 0) {
            TaiKhoan tk = xacThucService.getTaiKhoanHienTai();
            if (tk != null && tk.getPhanQuyen() == TaiKhoan.VaiTro.BAN_HANG) {
                throw new SecurityException("Phiếu đổi trả có giá trị trên 2.000.000 VNĐ cần Quản lý duyệt!");
            }
        }

        // Đồng bộ tồn kho và cập nhật tài chính hóa đơn ngay khi duyệt
        phieuDoiTraDAO.capNhatTonKhoKhiHoanTat(phieu);

        return phieuDoiTraDAO.capNhatTrangThai(maPhieuDoiTra, "Đã duyệt", null);
    }

    /**
     * Hoàn tất phiếu đổi trả (tương thích ngược với chức năng duyệt phiếu).
     */
    public boolean hoanTatPhieuDoiTra(String maPhieuDoiTra) {
        return duyetPhieuDoiTra(maPhieuDoiTra);
    }

    /**
     * Từ chối phiếu đổi trả (RET_CONFIRM) - bắt buộc nhập lý do từ chối.
     */
    public boolean tuChoiPhieuDoiTra(String maPhieuDoiTra, String lyDoTuChoi) {
        kiemTraQuyenDoiTra("RET_CONFIRM");

        if (lyDoTuChoi == null || lyDoTuChoi.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do từ chối phiếu đổi trả!");
        }

        PhieuDoiTra phieu = phieuDoiTraDAO.layPhieuDoiTraTheoMa(maPhieuDoiTra);
        if (phieu == null) {
            throw new IllegalArgumentException("Không tìm thấy phiếu đổi trả: " + maPhieuDoiTra);
        }

        if (!"Chờ xử lý".equalsIgnoreCase(phieu.getTrangThai())) {
            throw new IllegalArgumentException("Phiếu '" + maPhieuDoiTra + "' đã được xác nhận ("
                    + phieu.getTrangThai() + "), không thể từ chối!");
        }

        return phieuDoiTraDAO.capNhatTrangThai(maPhieuDoiTra, "Từ chối", lyDoTuChoi.trim());
    }

    /**
     * Lấy danh sách tất cả các phiếu đổi trả (RET_SEARCH).
     */
    public List<PhieuDoiTra> layTatCaPhieuDoiTra() {
        kiemTraQuyenDoiTra("RET_SEARCH");
        return phieuDoiTraDAO.layTatCaPhieuDoiTra();
    }

    /**
     * Tra cứu phiếu đổi trả theo bộ lọc nâng cao (RET_SEARCH).
     */
    public List<PhieuDoiTra> timKiemPhieuDoiTra(String tuKhoa, LocalDate tuNgay, LocalDate denNgay,
                                                String trangThai, String loaiXuLy) {
        kiemTraQuyenDoiTra("RET_SEARCH");
        return phieuDoiTraDAO.timKiemPhieuDoiTra(tuKhoa, tuNgay, denNgay, trangThai, loaiXuLy);
    }

    /**
     * Đọc chi tiết một phiếu đổi trả (RET_SEARCH).
     */
    public PhieuDoiTra layPhieuDoiTraTheoMa(String maPhieuDoiTra) {
        kiemTraQuyenDoiTra("RET_SEARCH");
        return phieuDoiTraDAO.layPhieuDoiTraTheoMa(maPhieuDoiTra);
    }
}
