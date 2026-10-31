package com.petshop.service;

import com.petshop.dao.DonNhapHangDAO;
import com.petshop.model.ChiTietDonNhap;
import com.petshop.model.DonNhapHang;
import com.petshop.model.TaiKhoan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Lớp dịch vụ nghiệp vụ Đơn nhập hàng.
 * Phụ trách kiểm tra quyền, validation, tự động tách đơn theo NCC,
 * xác nhận nhận hàng thiếu/hỏng và truy vết người nhận/hủy.
 */
public class DonNhapHangService {

    private final DonNhapHangDAO donNhapHangDAO;
    private final XacThucService xacThucService;

    public DonNhapHangService() {
        this.donNhapHangDAO = new DonNhapHangDAO();
        this.xacThucService = new XacThucService();
    }

    public DonNhapHangService(DonNhapHangDAO donNhapHangDAO, XacThucService xacThucService) {
        this.donNhapHangDAO = donNhapHangDAO;
        this.xacThucService = xacThucService;
    }

    /**
     * Tự động sinh mã đơn nhập hàng theo định dạng PN-yyyyMMdd-xxxxxx.
     */
    public String taoMaDonNhapHang() {
        return donNhapHangDAO.taoMaDonNhapHang();
    }

    /**
     * Tự động tách đơn theo từng Nhà cung cấp tại tầng Service và lưu trong một Transaction.
     * View chỉ cần truyền danh sách sản phẩm và map thông tin NCC của từng SP.
     */
    public List<DonNhapHang> lapDonNhapHangTuDanhSach(List<ChiTietDonNhap> danhSachChiTiet,
                                                      Map<String, String> sanPhamNhaCungCapMap,
                                                      LocalDate ngayLap, LocalTime gioLap) {
        xacThucService.kiemTraQuyen("DN_TAO");

        if (danhSachChiTiet == null || danhSachChiTiet.isEmpty()) {
            throw new IllegalArgumentException("Danh sách sản phẩm nhập không được để trống!");
        }

        TaiKhoan tkHienTai = xacThucService.getTaiKhoanHienTai();
        if (tkHienTai == null || tkHienTai.getMaNhanVien() == null || tkHienTai.getMaNhanVien().isBlank()) {
            throw new IllegalArgumentException("Không xác định được nhân viên lập đơn đang đăng nhập!");
        }

        // Nhóm chi tiết theo nhà cung cấp
        Map<String, List<ChiTietDonNhap>> gomNhomTheoNCC = new LinkedHashMap<>();
        for (ChiTietDonNhap ct : danhSachChiTiet) {
            String maNCC = (sanPhamNhaCungCapMap != null) ? sanPhamNhaCungCapMap.get(ct.getMaSanPham()) : null;
            if (maNCC == null || maNCC.isBlank()) {
                throw new IllegalArgumentException("Không xác định được nhà cung cấp của sản phẩm: " + ct.getTenSanPham());
            }
            gomNhomTheoNCC.computeIfAbsent(maNCC.trim(), k -> new ArrayList<>()).add(ct);
        }

        List<DonNhapHang> danhSachDonHang = new ArrayList<>();
        LocalDate ngay = ngayLap != null ? ngayLap : LocalDate.now();
        LocalTime gio = gioLap != null ? gioLap : LocalTime.now().withNano(0);

        for (Map.Entry<String, List<ChiTietDonNhap>> entry : gomNhomTheoNCC.entrySet()) {
            DonNhapHang donHang = new DonNhapHang();
            donHang.setMaNhaCungCap(entry.getKey());
            donHang.setMaNhanVien(tkHienTai.getMaNhanVien());
            donHang.setNgayLap(ngay);
            donHang.setGioLap(gio);

            List<ChiTietDonNhap> dsBanSao = new ArrayList<>();
            for (ChiTietDonNhap ct : entry.getValue()) {
                ChiTietDonNhap banSao = new ChiTietDonNhap(
                        null, ct.getMaSanPham(), ct.getTenSanPham(), ct.getDonViTinh(),
                        ct.getSoLuong(), ct.getSoLuong(), ct.getDonGia()
                );
                dsBanSao.add(banSao);
            }
            donHang.setDanhSachChiTiet(dsBanSao);
            danhSachDonHang.add(donHang);
        }

        // Gọi lưu nhiều đơn trong 1 transaction
        lapNhieuDonNhapHang(danhSachDonHang);
        return danhSachDonHang;
    }

    /**
     * Lập đơn nhập hàng mới đơn lẻ (IMP_CREATE).
     */
    public boolean lapDonNhapHang(DonNhapHang donHang, List<ChiTietDonNhap> danhSachChiTiet) {
        xacThucService.kiemTraQuyen("DN_TAO");

        if (donHang == null) {
            throw new IllegalArgumentException("Thông tin đơn nhập không được để trống!");
        }
        if (donHang.getMaNhaCungCap() == null || donHang.getMaNhaCungCap().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn nhà cung cấp!");
        }
        if (danhSachChiTiet == null || danhSachChiTiet.isEmpty()) {
            throw new IllegalArgumentException("Danh sách sản phẩm nhập không được để trống!");
        }

        if (donHang.getMaDonHang() == null || donHang.getMaDonHang().trim().isEmpty()) {
            donHang.setMaDonHang(donNhapHangDAO.taoMaDonNhapHang());
        }

        TaiKhoan tkHienTai = xacThucService.getTaiKhoanHienTai();
        if (donHang.getMaNhanVien() == null || donHang.getMaNhanVien().trim().isEmpty()) {
            if (tkHienTai != null && tkHienTai.getMaNhanVien() != null) {
                donHang.setMaNhanVien(tkHienTai.getMaNhanVien());
            } else {
                throw new IllegalArgumentException("Không xác định được nhân viên lập đơn!");
            }
        }

        BigDecimal tongTien = BigDecimal.ZERO;
        Set<String> sanPhamDaCo = new HashSet<>();

        for (ChiTietDonNhap ct : danhSachChiTiet) {
            if (ct.getMaSanPham() == null || ct.getMaSanPham().trim().isEmpty()) {
                throw new IllegalArgumentException("Mã sản phẩm trong chi tiết không được để trống!");
            }
            if (!sanPhamDaCo.add(ct.getMaSanPham())) {
                throw new IllegalArgumentException("Sản phẩm " + ct.getMaSanPham() + " bị trùng lặp trong đơn nhập!");
            }
            if (ct.getSoLuong() <= 0) {
                throw new IllegalArgumentException("Số lượng của sản phẩm " + ct.getMaSanPham() + " phải lớn hơn 0!");
            }
            if (ct.getDonGia() == null || ct.getDonGia().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Đơn giá của sản phẩm " + ct.getMaSanPham() + " không được âm!");
            }

            ct.setMaDonHang(donHang.getMaDonHang());
            tongTien = tongTien.add(ct.getThanhTien());
        }

        donHang.setTongTien(tongTien);
        donHang.setTrangThai(DonNhapHang.TRANG_THAI_CHO_NHAN);
        if (donHang.getNgayLap() == null) {
            donHang.setNgayLap(LocalDate.now());
        }
        if (donHang.getGioLap() == null) {
            donHang.setGioLap(LocalTime.now().withNano(0));
        }

        return donNhapHangDAO.themDonNhapHang(donHang, danhSachChiTiet);
    }

    /**
     * Tạo nhiều đơn nhập, mỗi đơn ứng với một nhà cung cấp; lưu tất cả trong một transaction.
     */
    public boolean lapNhieuDonNhapHang(List<DonNhapHang> danhSachDonHang) {
        xacThucService.kiemTraQuyen("DN_TAO");
        if (danhSachDonHang == null || danhSachDonHang.isEmpty()) {
            throw new IllegalArgumentException("Không có đơn nhập nào để lưu.");
        }

        TaiKhoan tk = xacThucService.getTaiKhoanHienTai();
        Set<String> nhaCungCapDaGap = new HashSet<>();
        Set<String> sanPhamDaGap = new HashSet<>();
        for (DonNhapHang donHang : danhSachDonHang) {
            if (donHang == null || donHang.getMaNhaCungCap() == null || donHang.getMaNhaCungCap().isBlank()) {
                throw new IllegalArgumentException("Không xác định được nhà cung cấp của một đơn nhập.");
            }
            donHang.setMaNhaCungCap(donHang.getMaNhaCungCap().trim());
            if (!nhaCungCapDaGap.add(donHang.getMaNhaCungCap())) {
                throw new IllegalArgumentException("Mỗi nhà cung cấp chỉ được xuất hiện một lần trong danh sách đơn.");
            }
            if (donHang.getDanhSachChiTiet() == null || donHang.getDanhSachChiTiet().isEmpty()) {
                throw new IllegalArgumentException("Đơn của nhà cung cấp " + donHang.getMaNhaCungCap() + " không có sản phẩm.");
            }
            if (donHang.getMaNhanVien() == null || donHang.getMaNhanVien().isBlank()) {
                if (tk == null || tk.getMaNhanVien() == null || tk.getMaNhanVien().isBlank()) {
                    throw new IllegalArgumentException("Không xác định được nhân viên lập đơn.");
                }
                donHang.setMaNhanVien(tk.getMaNhanVien());
            }
            if (donHang.getNgayLap() == null) donHang.setNgayLap(LocalDate.now());
            if (donHang.getGioLap() == null) donHang.setGioLap(LocalTime.now().withNano(0));

            BigDecimal tongTien = BigDecimal.ZERO;
            for (ChiTietDonNhap ct : donHang.getDanhSachChiTiet()) {
                if (ct == null || ct.getMaSanPham() == null || ct.getMaSanPham().isBlank()) {
                    throw new IllegalArgumentException("Sản phẩm trong đơn nhập không hợp lệ.");
                }
                if (!sanPhamDaGap.add(ct.getMaSanPham())) {
                    throw new IllegalArgumentException("Sản phẩm " + ct.getMaSanPham() + " bị lặp trong các đơn nhập.");
                }
                if (ct.getSoLuong() <= 0) {
                    throw new IllegalArgumentException("Số lượng sản phẩm " + ct.getMaSanPham() + " phải lớn hơn 0.");
                }
                if (ct.getDonGia() == null || ct.getDonGia().compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Đơn giá sản phẩm " + ct.getMaSanPham() + " không hợp lệ.");
                }
                tongTien = tongTien.add(ct.getThanhTien());
            }
            donHang.setTongTien(tongTien);
            donHang.setTrangThai(DonNhapHang.TRANG_THAI_CHO_NHAN);
        }
        return donNhapHangDAO.themNhieuDonNhapHang(danhSachDonHang);
    }

    /**
     * Xác nhận nhận hàng và cộng tồn kho với danh sách thực nhận (IMP_CONFIRM).
     * Bắt buộc kiểm tra:
     * - soLuongThucNhan: >= 0 và <= soLuong
     * - Nếu toàn bộ dòng đều có soLuongThucNhan = 0 thì chặn, báo lỗi và gợi ý Hủy đơn.
     * - Tính lại tongTienThucNhan.
     */
    /**
     * Xác nhận nhận hàng và cộng tồn kho với danh sách thực nhận (IMP_CONFIRM).
     * Bắt buộc kiểm tra:
     * - soLuongThucNhan: >= 0 và <= soLuong
     * - Nếu toàn bộ dòng đều có soLuongThucNhan = 0 thì chặn, báo lỗi và gợi ý Hủy đơn.
     * - Tính lại tongTienThucNhan.
     */
    public boolean xacNhanNhanHang(String maDonHang, List<ChiTietDonNhap> chiTietCapNhat) {
        String ketQua = xacNhanNhanHangThieu(maDonHang, chiTietCapNhat, false, "Đóng đơn theo thực nhận");
        return ketQua != null;
    }

    /**
     * Xác nhận nhận hàng có xử lý giao thiếu (Phương án 3):
     * - Nếu nhận đủ 100%: hoàn tất đơn bình thường.
     * - Nếu giao thiếu và taoDonBu = true: hoàn tất đơn hiện tại và tự động tách đơn nhập bù trong cùng một transaction.
     * - Nếu giao thiếu và taoDonBu = false: đóng đơn theo số thực nhận và bắt buộc lưu lý do chênh lệch.
     *
     * @param maDonHang Mã đơn hàng cần xác nhận
     * @param chiTietCapNhat Danh sách mặt hàng với số lượng thực nhận
     * @param taoDonBu True để tự động tạo đơn nhập bù cho phần thiếu; False để đóng đơn
     * @param lyDoChenhLech Lý do chênh lệch khi đóng đơn (bắt buộc nếu đóng đơn)
     * @return Mã đơn bù (nếu tạo) hoặc mã đơn gốc
     */
    public String xacNhanNhanHangThieu(String maDonHang, List<ChiTietDonNhap> chiTietCapNhat,
                                       boolean taoDonBu, String lyDoChenhLech) {
        xacThucService.kiemTraQuyen("DN_CAP_NHAT");

        if (maDonHang == null || maDonHang.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã đơn hàng không được để trống!");
        }

        DonNhapHang dh = donNhapHangDAO.timTheoMa(maDonHang);
        if (dh == null) {
            throw new IllegalArgumentException("Không tìm thấy đơn hàng " + maDonHang);
        }

        if (DonNhapHang.TRANG_THAI_HOAN_TAT.equalsIgnoreCase(dh.getTrangThai())) {
            throw new IllegalStateException("Đơn hàng này đã được xác nhận nhận hàng trước đó!");
        }

        if (DonNhapHang.TRANG_THAI_DA_HUY.equalsIgnoreCase(dh.getTrangThai())) {
            throw new IllegalStateException("Đơn hàng này đã bị hủy, không thể nhận hàng!");
        }

        if (!DonNhapHang.TRANG_THAI_CHO_NHAN.equalsIgnoreCase(dh.getTrangThai())) {
            throw new IllegalStateException("Chỉ có thể xác nhận đơn hàng đang ở trạng thái 'Chờ nhận hàng'!");
        }

        if (chiTietCapNhat == null || chiTietCapNhat.isEmpty()) {
            chiTietCapNhat = donNhapHangDAO.layChiTietTheoMaDonHang(maDonHang);
        }

        if (chiTietCapNhat.isEmpty()) {
            throw new IllegalStateException("Đơn hàng không có sản phẩm nào để xác nhận!");
        }

        boolean coItNhatMotSanPhamThucNhan = false;
        BigDecimal tongTienThucNhan = BigDecimal.ZERO;
        List<ChiTietDonNhap> dsHangThieu = new ArrayList<>();
        int tongSoLuongThieu = 0;

        for (ChiTietDonNhap ct : chiTietCapNhat) {
            int thucNhan = ct.getSoLuongThucNhan() != null ? ct.getSoLuongThucNhan() : 0;
            if (thucNhan < 0) {
                throw new IllegalArgumentException("Số lượng thực nhận của sản phẩm " + ct.getTenSanPham() + " không được âm!");
            }
            if (thucNhan > ct.getSoLuong()) {
                throw new IllegalArgumentException("Số lượng thực nhận (" + thucNhan + ") của sản phẩm "
                        + ct.getTenSanPham() + " không được vượt quá số lượng đặt (" + ct.getSoLuong() + ")!");
            }
            if (thucNhan > 0) {
                coItNhatMotSanPhamThucNhan = true;
            }
            tongTienThucNhan = tongTienThucNhan.add(ct.getThanhTienThucNhan());

            int thieu = ct.getSoLuong() - thucNhan;
            if (thieu > 0) {
                tongSoLuongThieu += thieu;
                ChiTietDonNhap ctBu = new ChiTietDonNhap(null, ct.getMaSanPham(), ct.getTenSanPham(),
                        ct.getDonViTinh(), thieu, ct.getDonGia());
                dsHangThieu.add(ctBu);
            }
        }

        // Chặn nếu toàn bộ dòng đều thực nhận = 0
        if (!coItNhatMotSanPhamThucNhan) {
            throw new IllegalArgumentException("Tất cả sản phẩm đều có số lượng thực nhận bằng 0. Vui lòng sử dụng chức năng 'Hủy đơn' nếu không nhận hàng.");
        }

        TaiKhoan tk = xacThucService.getTaiKhoanHienTai();
        String maNhanVienNhan = (tk != null && tk.getMaNhanVien() != null) ? tk.getMaNhanVien() : dh.getMaNhanVien();
        LocalDateTime ngayGioNhan = LocalDateTime.now();

        DonNhapHang donNhapBu = null;
        String ghiChuGhiNhan = null;

        if (tongSoLuongThieu > 0) {
            if (taoDonBu) {
                donNhapBu = new DonNhapHang();
                donNhapBu.setMaNhaCungCap(dh.getMaNhaCungCap());
                String maNV = (dh.getMaNhanVien() != null && !dh.getMaNhanVien().isBlank()) ? dh.getMaNhanVien() : maNhanVienNhan;
                donNhapBu.setMaNhanVien(maNV);
                donNhapBu.setNgayLap(LocalDate.now());
                donNhapBu.setGioLap(LocalTime.now().withNano(0));
                donNhapBu.setTrangThai(DonNhapHang.TRANG_THAI_CHO_NHAN);
                donNhapBu.setLyDoHuy("Đơn nhập bù cho " + maDonHang + " (thiếu " + tongSoLuongThieu + " sp)");
                donNhapBu.setDanhSachChiTiet(dsHangThieu);

                BigDecimal tongTienBu = BigDecimal.ZERO;
                for (ChiTietDonNhap b : dsHangThieu) {
                    tongTienBu = tongTienBu.add(b.getThanhTien());
                }
                donNhapBu.setTongTien(tongTienBu);

                ghiChuGhiNhan = "Giao thiếu " + tongSoLuongThieu + " sp. Đã tách đơn nhập bù.";
            } else {
                if (lyDoChenhLech == null || lyDoChenhLech.trim().isEmpty()) {
                    throw new IllegalArgumentException("Vui lòng cung cấp lý do thiếu hàng khi đóng đơn!");
                }
                ghiChuGhiNhan = "Giao thiếu " + tongSoLuongThieu + " sp. Đóng đơn: " + lyDoChenhLech.trim();
            }
        }

        return donNhapHangDAO.xacNhanNhanHangThieuVaTaoDonBu(maDonHang, maNhanVienNhan, ngayGioNhan,
                tongTienThucNhan, ghiChuGhiNhan, chiTietCapNhat, donNhapBu);
    }

    /**
     * Xác nhận nhận hàng mặc định nhận đủ (tương thích ngược).
     */
    public boolean xacNhanNhanHang(String maDonHang) {
        List<ChiTietDonNhap> dsChiTiet = donNhapHangDAO.layChiTietTheoMaDonHang(maDonHang);
        for (ChiTietDonNhap ct : dsChiTiet) {
            ct.setSoLuongThucNhan(ct.getSoLuong());
        }
        return xacNhanNhanHang(maDonHang, dsChiTiet);
    }

    /**
     * Hủy đơn nhập hàng chưa nhận (IMP_CANCEL) với lý do hủy bắt buộc.
     */
    public boolean huyDonNhapHang(String maDonHang, String lyDoHuy) {
        xacThucService.kiemTraQuyen("DN_CAP_NHAT");

        if (maDonHang == null || maDonHang.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã đơn hàng không được để trống!");
        }

        if (lyDoHuy == null || lyDoHuy.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do hủy đơn nhập hàng!");
        }

        DonNhapHang dh = donNhapHangDAO.timTheoMa(maDonHang);
        if (dh == null) {
            throw new IllegalArgumentException("Không tìm thấy đơn hàng " + maDonHang);
        }

        if (DonNhapHang.TRANG_THAI_HOAN_TAT.equalsIgnoreCase(dh.getTrangThai())) {
            throw new IllegalStateException("Đơn hàng đã hoàn tất nhập kho, không thể hủy!");
        }

        if (DonNhapHang.TRANG_THAI_DA_HUY.equalsIgnoreCase(dh.getTrangThai())) {
            throw new IllegalStateException("Đơn hàng này đã bị hủy từ trước!");
        }

        if (!DonNhapHang.TRANG_THAI_CHO_NHAN.equalsIgnoreCase(dh.getTrangThai())) {
            throw new IllegalStateException("Chỉ có thể hủy đơn hàng đang ở trạng thái 'Chờ nhận hàng'!");
        }

        TaiKhoan tk = xacThucService.getTaiKhoanHienTai();
        String maNhanVienHuy = (tk != null && tk.getMaNhanVien() != null) ? tk.getMaNhanVien() : dh.getMaNhanVien();

        return donNhapHangDAO.capNhatTrangThaiHuy(maDonHang, maNhanVienHuy, lyDoHuy.trim());
    }

    /**
     * Hủy đơn không truyền lý do (tương thích ngược với unit test cũ).
     */
    public boolean huyDonNhapHang(String maDonHang) {
        return huyDonNhapHang(maDonHang, "Hủy đơn theo yêu cầu");
    }

    /**
     * Tìm kiếm đơn nhập hàng đa tiêu chí (IMP_SEARCH) - hỗ trợ tìm theo tên hàng.
     */
    public List<DonNhapHang> timKiemDonHang(String keyword, String maNhaCungCap, LocalDate tuNgay, LocalDate denNgay, String trangThai) {
        return timKiemDonHang(keyword, maNhaCungCap, tuNgay, denNgay, trangThai, null);
    }

    /**
     * Tìm kiếm đơn nhập hàng đa tiêu chí (IMP_SEARCH) bao gồm tìm kiếm theo tên hàng.
     */
    public List<DonNhapHang> timKiemDonHang(String keyword, String maNhaCungCap, LocalDate tuNgay, LocalDate denNgay, String trangThai, String tenHang) {
        xacThucService.kiemTraQuyen("DN_TIM_KIEM");
        if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
            throw new IllegalArgumentException("Từ ngày không được lớn hơn Đến ngày!");
        }
        return donNhapHangDAO.timKiemDonHang(keyword, maNhaCungCap, tuNgay, denNgay, trangThai, tenHang);
    }

    /**
     * Lấy tất cả đơn nhập hàng.
     */
    public List<DonNhapHang> layTatCaDonHang() {
        if (!xacThucService.coQuyen("DN_CAP_NHAT") && !xacThucService.coQuyen("DN_TIM_KIEM")) {
            xacThucService.kiemTraQuyen("DN_CAP_NHAT");
        }
        return donNhapHangDAO.layTatCaDonHang();
    }

    /**
     * Lấy thông tin đơn nhập hàng theo mã.
     */
    public DonNhapHang timTheoMa(String maDonHang) {
        if (maDonHang == null || maDonHang.trim().isEmpty()) {
            return null;
        }
        return donNhapHangDAO.timTheoMa(maDonHang.trim());
    }

    /**
     * Lấy danh sách chi tiết sản phẩm của một đơn nhập.
     */
    public List<ChiTietDonNhap> layChiTietTheoMaDonHang(String maDonHang) {
        if (maDonHang == null || maDonHang.trim().isEmpty()) {
            return List.of();
        }
        return donNhapHangDAO.layChiTietTheoMaDonHang(maDonHang.trim());
    }
}
