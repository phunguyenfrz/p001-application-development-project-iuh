package com.petshop.bus;

import com.petshop.dao.HoaDonDAO;
import com.petshop.dao.PhieuDoiTraDAO;
import com.petshop.dao.SanPhamDAO;
import com.petshop.entity.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp nghiệp vụ cho Phiếu đổi trả (PhieuDoiTraBUS).
 * Tuân thủ Bộ chỉ thị kỹ thuật IUH:
 * - 100% tiền tệ dùng kiểu nguyên thủy double (0% BigDecimal).
 * - Không import javafx.* trong BUS.
 * - Kiểm tra điều kiện: Hóa đơn trong 7 ngày, trạng thái "Đã thanh toán", chỉ đổi trả sản phẩm (không đổi trả dịch vụ).
 * - Khi duyệt đổi trả: tự động hoàn tồn kho (soLuongTon + ?) và trừ điểm tích lũy của khách hàng.
 */
public class PhieuDoiTraBUS {

    public static final int SO_NGAY_DOI_TRA_TOI_DA = 7;
    public static final double NGUONG_DUYET_QUAN_LY = 2_000_000.0;

    private final PhieuDoiTraDAO phieuDoiTraDAO;
    private final HoaDonDAO hoaDonDAO;
    private final SanPhamDAO sanPhamDAO;
    private final XacThucBUS xacThucService;

    public PhieuDoiTraBUS() {
        this.phieuDoiTraDAO = new PhieuDoiTraDAO();
        this.hoaDonDAO = new HoaDonDAO();
        this.sanPhamDAO = new SanPhamDAO();
        this.xacThucService = new XacThucBUS();
    }

    public PhieuDoiTraBUS(PhieuDoiTraDAO phieuDoiTraDAO, HoaDonDAO hoaDonDAO,
                          SanPhamDAO sanPhamDAO, XacThucBUS xacThucService) {
        this.phieuDoiTraDAO = phieuDoiTraDAO;
        this.hoaDonDAO = hoaDonDAO;
        this.sanPhamDAO = sanPhamDAO;
        this.xacThucService = xacThucService;
    }

    public String sinhMaPhieu() {
        return phieuDoiTraDAO.sinhMaPhieu();
    }

    private boolean laDoiHang(String loaiXuLy) {
        if (loaiXuLy == null) return false;
        String lower = loaiXuLy.trim().toLowerCase();
        return lower.contains("đổi") || lower.contains("doi");
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
     * Lấy danh sách hóa đơn hợp lệ trong 7 ngày gần nhất.
     */
    public List<HoaDon> layDanhSachHoaDonTrong7Ngay() {
        kiemTraQuyenDoiTra("RET_CREATE");
        LocalDate tuNgay = LocalDate.now().minusDays(SO_NGAY_DOI_TRA_TOI_DA);
        LocalDate denNgay = LocalDate.now();
        List<HoaDon> ds = hoaDonDAO.timKiemHoaDon(null, tuNgay, denNgay, "Tất cả", "Đã thanh toán");
        List<HoaDon> ketQua = new ArrayList<>();
        for (HoaDon item : ds) {
            HoaDon full = hoaDonDAO.layHoaDonTheoMa(item.getMaHoaDon());
            if (full != null) {
                try {
                    kiemTraDieuKienHoaDon(full);
                    ketQua.add(full);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return ketQua;
    }

    /**
     * Tra cứu hóa đơn trực tiếp dưới CSDL theo từ khóa (Mã HĐ, SĐT khách hàng ^0\d{9}$, Tên KH).
     */
    public List<HoaDon> traCuuDanhSachHoaDonDeDoiTra(String tuKhoa) {
        kiemTraQuyenDoiTra("RET_CREATE");

        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập Mã hóa đơn hoặc Số điện thoại khách hàng để tra cứu!");
        }

        String keyword = tuKhoa.trim();
        HoaDon exact = hoaDonDAO.layHoaDonTheoMa(keyword);
        if (exact != null) {
            kiemTraDieuKienHoaDon(exact);
            return List.of(exact);
        }

        List<HoaDon> ds = hoaDonDAO.timKiemHoaDon(keyword, null, null, "Tất cả", "Tất cả");
        if (ds.isEmpty()) {
            List<HoaDon> tatCa7Ngay = hoaDonDAO.timKiemHoaDon(null, LocalDate.now().minusDays(SO_NGAY_DOI_TRA_TOI_DA), LocalDate.now(), "Tất cả", "Tất cả");
            for (HoaDon h : tatCa7Ngay) {
                if (h.getMaKhachHang() != null && h.getMaKhachHang().toLowerCase().contains(keyword.toLowerCase())) {
                    ds.add(h);
                }
            }
        }

        if (ds.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy hóa đơn gốc khớp với thông tin: " + keyword);
        }

        List<HoaDon> dsHopLe = new ArrayList<>();
        String loiGanNhat = null;

        for (HoaDon item : ds) {
            HoaDon full = hoaDonDAO.layHoaDonTheoMa(item.getMaHoaDon());
            if (full == null) continue;
            try {
                kiemTraDieuKienHoaDon(full);
                dsHopLe.add(full);
            } catch (IllegalArgumentException ex) {
                if (loiGanNhat == null) {
                    loiGanNhat = ex.getMessage();
                }
            }
        }

        if (dsHopLe.isEmpty()) {
            throw new IllegalArgumentException(loiGanNhat != null
                    ? loiGanNhat
                    : "Không có hóa đơn sản phẩm hợp lệ trong 7 ngày gần nhất khớp với: " + keyword);
        }

        return dsHopLe;
    }

    public HoaDon traCuuHoaDonDeDoiTra(String tuKhoa) {
        List<HoaDon> ds = traCuuDanhSachHoaDonDeDoiTra(tuKhoa);
        return ds.get(0);
    }

    /**
     * Kiểm tra điều kiện hóa đơn có được phép đổi trả hay không:
     * 1. Phải ở trạng thái "Đã thanh toán".
     * 2. Phải nằm trong vòng 7 ngày kể từ ngày lập.
     * 3. Chỉ đổi trả sản phẩm (hóa đơn thuần dịch vụ chăm sóc không được đổi trả).
     */
    public void kiemTraDieuKienHoaDon(HoaDon hd) {
        if (hd == null) {
            throw new IllegalArgumentException("Hóa đơn gốc không tồn tại!");
        }
        if (!"Đã thanh toán".equalsIgnoreCase(hd.getTrangThaiThanhToan())) {
            throw new IllegalArgumentException("Hóa đơn '" + hd.getMaHoaDon() + "' chưa hoàn tất thanh toán hoặc đã bị hủy, không thể đổi trả!");
        }

        if (hd.getNgayLap() != null) {
            long soNgay = ChronoUnit.DAYS.between(hd.getNgayLap(), LocalDate.now());
            if (soNgay < 0 || soNgay > SO_NGAY_DOI_TRA_TOI_DA) {
                throw new IllegalArgumentException("Hóa đơn '" + hd.getMaHoaDon() + "' đã mua cách đây " + soNgay
                        + " ngày, đã vượt quá thời hạn đổi trả quy định (" + SO_NGAY_DOI_TRA_TOI_DA + " ngày)!");
            }
        }

        if (hd.getDanhSachSanPham() == null || hd.getDanhSachSanPham().isEmpty()) {
            throw new IllegalArgumentException("Hóa đơn '" + hd.getMaHoaDon() + "' là hóa đơn dịch vụ chăm sóc thú cưng, chỉ cho phép đổi trả sản phẩm!");
        }
    }

    /**
     * Kiểm tra ma trận quy tắc đổi trả QL5, QL5a, QL5b.
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

    public void kiemTraHopLeChiTiet(ChiTietPhieuDoiTra ct) {
        if (ct == null) {
            throw new IllegalArgumentException("Mặt hàng đổi trả không được để trống!");
        }
        if (ct.getMaSanPham() != null && ct.getMaSanPham().toUpperCase().startsWith("DV")) {
            throw new IllegalArgumentException("Không được phép đổi trả dịch vụ chăm sóc thú cưng!");
        }
        kiemTraQuyTacMaTran(ct.getLyDo(), ct.getTinhTrang(), ct.getGhiChu());
    }

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

        for (ChiTietPhieuDoiTra ct : phieu.getDanhSachSanPham()) {
            if (ct.getMaSanPham() != null && ct.getMaSanPham().toUpperCase().startsWith("DV")) {
                throw new IllegalArgumentException("Chỉ cho phép đổi trả sản phẩm, không đổi trả dịch vụ!");
            }
            if (ct.getSoLuong() <= 0) {
                throw new IllegalArgumentException("Số lượng đổi trả của sản phẩm '" + ct.getTenSanPham() + "' phải lớn hơn 0!");
            }

            kiemTraQuyTacMaTran(ct.getLyDo(), ct.getTinhTrang(), ct.getGhiChu());

            if (ct.getHuongXuLy() == null || ct.getHuongXuLy().trim().isEmpty()) {
                ct.setHuongXuLy(ChiTietPhieuDoiTra.tuDongGoiYHuongXuLy(ct.getLyDo(), ct.getTinhTrang()));
            }

            int soLuongDaMua = 0;
            for (ChiTietHoaDonSanPham spMua : hdGoc.getDanhSachSanPham()) {
                if (spMua.getMaSanPham().equalsIgnoreCase(ct.getMaSanPham())) {
                    soLuongDaMua += spMua.getSoLuong();
                }
            }
            if (soLuongDaMua <= 0) {
                throw new IllegalArgumentException("Sản phẩm '" + ct.getTenSanPham() + "' không thuộc danh sách sản phẩm của hóa đơn gốc!");
            }

            int soLuongDaTraTruoc = phieuDoiTraDAO.laySoLuongDaDoiTra(phieu.getMaHoaDon(), ct.getMaSanPham());
            int soLuongToiDaConLai = soLuongDaMua - soLuongDaTraTruoc;

            if (ct.getSoLuong() > soLuongToiDaConLai) {
                throw new IllegalArgumentException("Sản phẩm '" + ct.getTenSanPham() + "' đã mua " + soLuongDaMua
                        + ", đã đổi trả trước đó " + soLuongDaTraTruoc + ". Số lượng trả lần này (" + ct.getSoLuong()
                        + ") vượt quá số lượng tối đa được phép đổi trả (" + soLuongToiDaConLai + ")!");
            }

            if (laDoiHang(phieu.getLoaiXuLy())) {
                if (ct.getMaSanPhamDoi() != null && !ct.getMaSanPhamDoi().trim().isEmpty()) {
                    if (ct.getSoLuongDoi() <= 0) {
                        throw new IllegalArgumentException("Vui lòng nhập số lượng sản phẩm đổi lấy mới cho '" + ct.getTenSanPham() + "'!");
                    }
                    int tonKho = sanPhamDAO.traCuuTonKho(ct.getMaSanPhamDoi());
                    if (tonKho < ct.getSoLuongDoi()) {
                        throw new IllegalArgumentException("Sản phẩm đổi mới '" + ct.getTenSanPhamDoi() + "' chỉ còn tồn kho "
                                + tonKho + ", không đủ số lượng đổi (" + ct.getSoLuongDoi() + ")!");
                    }
                    if (ct.getDonGiaDoi() <= 0) {
                        SanPham spDoi = sanPhamDAO.layTheoMa(ct.getMaSanPhamDoi());
                        if (spDoi != null) {
                            ct.setDonGiaDoi(spDoi.getGiaBan());
                        }
                    }
                }
            }

            tongSoLuongTra += ct.getSoLuong();
        }

        phieu.setSoLuong(tongSoLuongTra);

        if (phieu.getMaPhieuDoiTra() == null || phieu.getMaPhieuDoiTra().trim().isEmpty()) {
            phieu.setMaPhieuDoiTra(sinhMaPhieu());
        }

        return phieuDoiTraDAO.themPhieuDoiTra(phieu);
    }

    /**
     * Cập nhật thông tin phiếu đổi trả khi ở trạng thái Chờ xử lý.
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

        int tongSoLuongTra = 0;
        for (ChiTietPhieuDoiTra ct : phieu.getDanhSachSanPham()) {
            if (ct.getSoLuong() <= 0) {
                throw new IllegalArgumentException("Số lượng trả của '" + ct.getTenSanPham() + "' phải > 0!");
            }
            kiemTraQuyTacMaTran(ct.getLyDo(), ct.getTinhTrang(), ct.getGhiChu());

            if (ct.getHuongXuLy() == null || ct.getHuongXuLy().trim().isEmpty()) {
                ct.setHuongXuLy(ChiTietPhieuDoiTra.tuDongGoiYHuongXuLy(ct.getLyDo(), ct.getTinhTrang()));
            }

            tongSoLuongTra += ct.getSoLuong();
            if (laDoiHang(phieu.getLoaiXuLy()) && ct.getMaSanPhamDoi() != null && !ct.getMaSanPhamDoi().trim().isEmpty()) {
                if (ct.getDonGiaDoi() <= 0) {
                    SanPham spDoi = sanPhamDAO.layTheoMa(ct.getMaSanPhamDoi());
                    if (spDoi != null) {
                        ct.setDonGiaDoi(spDoi.getGiaBan());
                    }
                }
            }
        }

        phieu.setSoLuong(tongSoLuongTra);
        return phieuDoiTraDAO.capNhatPhieuDoiTra(phieu);
    }

    /**
     * Duyệt phiếu đổi trả (RET_CONFIRM).
     */
    public boolean duyetPhieuDoiTra(String maPhieuDoiTra) {
        return duyetPhieuDoiTra(maPhieuDoiTra, null);
    }

    /**
     * Duyệt phiếu đổi trả kèm tùy chọn xử lý sản phẩm khi hoàn ("Thu lại vào kho" hoặc "Hủy bỏ"):
     * - Tự động hoàn tồn kho (soLuongTon + ?) cho sản phẩm thu lại vào kho.
     * - Tự động trừ điểm tích lũy của khách hàng tương ứng với số tiền hoàn trả.
     */
    public boolean duyetPhieuDoiTra(String maPhieuDoiTra, String huongXuLySanPham) {
        kiemTraQuyenDoiTra("RET_CONFIRM");

        PhieuDoiTra phieu = phieuDoiTraDAO.layPhieuDoiTraTheoMa(maPhieuDoiTra);
        if (phieu == null) {
            throw new IllegalArgumentException("Không tìm thấy phiếu đổi trả: " + maPhieuDoiTra);
        }

        if (!"Chờ xử lý".equalsIgnoreCase(phieu.getTrangThai())) {
            throw new IllegalArgumentException("Phiếu '" + maPhieuDoiTra + "' hiện đang ở trạng thái '"
                    + phieu.getTrangThai() + "', không thể duyệt lại!");
        }

        double tongGiaTri = phieu.tinhTienHoan() + phieu.tinhTienBu();
        if (tongGiaTri > NGUONG_DUYET_QUAN_LY) {
            TaiKhoan tk = xacThucService.getTaiKhoanHienTai();
            if (tk != null && tk.getPhanQuyen() == TaiKhoan.VaiTro.BAN_HANG) {
                throw new SecurityException("Phiếu đổi trả có giá trị trên 2.000.000 VNĐ cần Quản lý duyệt!");
            }
        }

        if (huongXuLySanPham != null && !huongXuLySanPham.trim().isEmpty() && phieu.getDanhSachSanPham() != null) {
            for (ChiTietPhieuDoiTra ct : phieu.getDanhSachSanPham()) {
                ct.setHuongXuLy(huongXuLySanPham.trim());
            }
        }

        int soDiemTru = tinhSoDiemTichLuyCanTru(phieu);
        return phieuDoiTraDAO.capNhatTonKhoKhiHoanTat(phieu, soDiemTru);
    }

    /**
     * Tính số điểm tích lũy cần trừ của khách hàng khi duyệt đổi trả:
     * Dựa trên giá trị tiền hoàn lại cho khách (mỗi 100.000 VNĐ tương ứng 5 điểm, hoặc 10 điểm nếu >= 1.000.000 VNĐ).
     */
    public int tinhSoDiemTichLuyCanTru(PhieuDoiTra phieu) {
        if (phieu == null || phieu.getMaKhachHang() == null || phieu.getMaKhachHang().isBlank()) {
            return 0;
        }
        double giaTriTinhDiem = phieu.tinhTienHoan();
        if (giaTriTinhDiem <= 0) {
            return 0;
        }
        long soBlock100k = (long) (giaTriTinhDiem / 100_000.0);
        if (giaTriTinhDiem >= 1_000_000.0) {
            return (int) (soBlock100k * 10L);
        }
        int diem = (int) (soBlock100k * 5L);
        return (diem == 0 && giaTriTinhDiem >= 20_000.0) ? 1 : diem;
    }

    public boolean hoanTatPhieuDoiTra(String maPhieuDoiTra) {
        return duyetPhieuDoiTra(maPhieuDoiTra);
    }

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

    public List<PhieuDoiTra> layTatCaPhieuDoiTra() {
        return layDanhSachPhieuDoiTra();
    }

    public List<PhieuDoiTra> layDanhSachPhieuDoiTra() {
        kiemTraQuyenDoiTra("RET_SEARCH");
        return phieuDoiTraDAO.layDanhSachPhieuDoiTra();
    }

    public List<PhieuDoiTra> timKiemPhieuDoiTra(String tuKhoa, LocalDate tuNgay, LocalDate denNgay,
                                                String trangThai, String loaiXuLy) {
        kiemTraQuyenDoiTra("RET_SEARCH");
        return phieuDoiTraDAO.timKiemPhieuDoiTra(tuKhoa, tuNgay, denNgay, trangThai, loaiXuLy);
    }

    public PhieuDoiTra layPhieuDoiTraTheoMa(String maPhieuDoiTra) {
        kiemTraQuyenDoiTra("RET_SEARCH");
        return phieuDoiTraDAO.layPhieuDoiTraTheoMa(maPhieuDoiTra);
    }

    /**
     * Tra cứu trực tiếp sản phẩm đổi mới theo mã vạch / mã sản phẩm hoặc từ khóa dưới CSDL (tránh nạp toàn bộ CSDL lên ComboBox).
     */
    public SanPham timSanPhamTheoMaVachHoacTuKhoa(String maHoacTuKhoa) {
        if (maHoacTuKhoa == null || maHoacTuKhoa.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập mã vạch hoặc mã/tên sản phẩm cần đổi!");
        }
        String kw = maHoacTuKhoa.trim();
        SanPham exact = sanPhamDAO.layTheoMa(kw);
        if (exact != null && exact.isTrangThai()) {
            return exact;
        }
        List<SanPham> ds = sanPhamDAO.timKiemSanPham(kw, true);
        if (ds != null && !ds.isEmpty()) {
            return ds.get(0);
        }
        throw new IllegalArgumentException("Không tìm thấy sản phẩm đang kinh doanh khớp với mã/từ khóa: " + kw);
    }

    public List<SanPham> timDanhSachSanPhamDoiTheoTuKhoa(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            return List.of();
        }
        return sanPhamDAO.timKiemSanPham(tuKhoa.trim(), true);
    }

    public List<SanPham> layDanhSachSanPhamDangKinhDoanh() {
        return sanPhamDAO.layDanhSachSanPham();
    }
}
