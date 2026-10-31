package com.petshop.service;

import com.petshop.dao.DonHangDAO;
import com.petshop.model.ChiTietDonHang;
import com.petshop.model.DonHang;
import com.petshop.model.TaiKhoan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Xử lý nghiệp vụ, kiểm tra tính hợp lệ dữ liệu và phân quyền cho Đơn nhập hàng.
 * Phục vụ các ca sử dụng: IMP_CREATE, IMP_CONFIRM, IMP_CANCEL, IMP_SEARCH.
 */
public class DonHangService {

    public static final String REGEX_MA_DON_HANG = "^DH-\\d{8}-\\d{6}$";

    private final DonHangDAO donHangDAO;
    private final XacThucService xacThucService;

    public DonHangService() {
        this.donHangDAO = new DonHangDAO();
        this.xacThucService = new XacThucService();
    }

    public DonHangService(DonHangDAO donHangDAO, XacThucService xacThucService) {
        this.donHangDAO = donHangDAO;
        this.xacThucService = xacThucService;
    }

    /**
     * Tự động sinh mã đơn nhập hàng.
     */
    public String taoMaDonHang() {
        return donHangDAO.taoMaDonHang();
    }

    /**
     * Lập đơn nhập hàng mới (IMP_CREATE).
     * Yêu cầu quyền: DN_TAO.
     */
    public boolean lapDonNhapHang(DonHang donHang, List<ChiTietDonHang> danhSachChiTiet) {
        xacThucService.kiemTraQuyen("DN_TAO");

        if (donHang == null) {
            throw new IllegalArgumentException("Thông tin đơn hàng không được để trống!");
        }

        if (donHang.getMaNhaCungCap() == null || donHang.getMaNhaCungCap().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn nhà cung cấp!");
        }

        if (danhSachChiTiet == null || danhSachChiTiet.isEmpty()) {
            throw new IllegalArgumentException("Đơn hàng phải có ít nhất một sản phẩm nhập!");
        }

        // Tự động gán mã nhân viên đang đăng nhập nếu chưa có
        TaiKhoan tk = xacThucService.getTaiKhoanHienTai();
        if (donHang.getMaNhanVien() == null || donHang.getMaNhanVien().trim().isEmpty()) {
            if (tk != null && tk.getMaNhanVien() != null) {
                donHang.setMaNhanVien(tk.getMaNhanVien());
            } else {
                throw new IllegalArgumentException("Không xác định được nhân viên lập đơn!");
            }
        }

        // Sinh mã đơn nếu chưa có hoặc kiểm tra định dạng
        if (donHang.getMaDonHang() == null || donHang.getMaDonHang().trim().isEmpty()) {
            donHang.setMaDonHang(donHangDAO.taoMaDonHang());
        } else {
            String ma = donHang.getMaDonHang().trim();
            if (!ma.matches(REGEX_MA_DON_HANG)) {
                throw new IllegalArgumentException("Mã đơn hàng phải đúng định dạng DH-yyyyMMdd-xxxxxx!");
            }
            if (donHangDAO.timTheoMa(ma) != null) {
                throw new IllegalArgumentException("Mã đơn hàng " + ma + " đã tồn tại trong hệ thống!");
            }
        }

        // Tính tổng tiền và validate chi tiết đơn
        BigDecimal tongTien = BigDecimal.ZERO;
        for (ChiTietDonHang ct : danhSachChiTiet) {
            if (ct.getMaSanPham() == null || ct.getMaSanPham().trim().isEmpty()) {
                throw new IllegalArgumentException("Sản phẩm trong danh sách chi tiết không hợp lệ!");
            }
            if (ct.getSoLuong() <= 0) {
                throw new IllegalArgumentException("Số lượng nhập của sản phẩm " + ct.getMaSanPham() + " phải lớn hơn 0!");
            }
            if (ct.getDonGia() == null || ct.getDonGia().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Đơn giá nhập không được âm!");
            }
            ct.setMaDonHang(donHang.getMaDonHang());
            tongTien = tongTien.add(ct.getThanhTien());
        }

        donHang.setTongTien(tongTien);
        donHang.setTrangThai(DonHang.TRANG_THAI_CHO_NHAN);
        if (donHang.getNgayLap() == null) {
            donHang.setNgayLap(LocalDate.now());
        }

        return donHangDAO.themDonHang(donHang, danhSachChiTiet);
    }

    /**
     * Xác nhận nhận hàng và cộng tồn kho (IMP_CONFIRM).
     * Bắt buộc kiểm tra để không cộng đôi tồn kho.
     */
    public boolean xacNhanNhanHang(String maDonHang) {
        xacThucService.kiemTraQuyen("DN_CAP_NHAT");

        if (maDonHang == null || maDonHang.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã đơn hàng không được để trống!");
        }

        DonHang dh = donHangDAO.timTheoMa(maDonHang);
        if (dh == null) {
            throw new IllegalArgumentException("Không tìm thấy đơn hàng " + maDonHang);
        }

        if (DonHang.TRANG_THAI_HOAN_TAT.equalsIgnoreCase(dh.getTrangThai())) {
            throw new IllegalStateException("Đơn hàng này đã được xác nhận nhận hàng trước đó!");
        }

        if (DonHang.TRANG_THAI_DA_HUY.equalsIgnoreCase(dh.getTrangThai())) {
            throw new IllegalStateException("Đơn hàng này đã bị hủy, không thể nhận hàng!");
        }

        if (!DonHang.TRANG_THAI_CHO_NHAN.equalsIgnoreCase(dh.getTrangThai())) {
            throw new IllegalStateException("Chỉ có thể xác nhận đơn hàng đang ở trạng thái 'Chờ nhận hàng'!");
        }

        return donHangDAO.xacNhanNhanHangVaCongTonKho(maDonHang);
    }

    /**
     * Hủy đơn nhập hàng chưa nhận (IMP_CANCEL).
     * Chỉ được hủy khi đơn đang ở trạng thái 'Chờ nhận hàng'.
     */
    public boolean huyDonNhapHang(String maDonHang) {
        xacThucService.kiemTraQuyen("DN_CAP_NHAT");

        if (maDonHang == null || maDonHang.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã đơn hàng không được để trống!");
        }

        DonHang dh = donHangDAO.timTheoMa(maDonHang);
        if (dh == null) {
            throw new IllegalArgumentException("Không tìm thấy đơn hàng " + maDonHang);
        }

        if (DonHang.TRANG_THAI_HOAN_TAT.equalsIgnoreCase(dh.getTrangThai())) {
            throw new IllegalStateException("Đơn hàng đã hoàn tất nhập kho, không thể hủy!");
        }

        if (DonHang.TRANG_THAI_DA_HUY.equalsIgnoreCase(dh.getTrangThai())) {
            throw new IllegalStateException("Đơn hàng này đã bị hủy từ trước!");
        }

        if (!DonHang.TRANG_THAI_CHO_NHAN.equalsIgnoreCase(dh.getTrangThai())) {
            throw new IllegalStateException("Chỉ có thể hủy đơn hàng đang ở trạng thái 'Chờ nhận hàng'!");
        }

        return donHangDAO.capNhatTrangThai(maDonHang,
                DonHang.TRANG_THAI_CHO_NHAN, DonHang.TRANG_THAI_DA_HUY);
    }

    /**
     * Tìm kiếm đơn hàng đa tiêu chí (IMP_SEARCH).
     */
    public List<DonHang> timKiemDonHang(String keyword, String maNhaCungCap, LocalDate tuNgay, LocalDate denNgay, String trangThai) {
        xacThucService.kiemTraQuyen("DN_TIM_KIEM");
        if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
            throw new IllegalArgumentException("Từ ngày không được lớn hơn Đến ngày!");
        }
        return donHangDAO.timKiemDonHang(keyword, maNhaCungCap, tuNgay, denNgay, trangThai);
    }

    /**
     * Lấy tất cả đơn hàng.
     */
    public List<DonHang> layTatCaDonHang() {
        xacThucService.kiemTraQuyen("DN_CAP_NHAT");
        return donHangDAO.layTatCaDonHang();
    }

    /**
     * Lấy thông tin đơn hàng theo mã.
     */
    public DonHang timTheoMa(String maDonHang) {
        if (maDonHang == null || maDonHang.trim().isEmpty()) {
            return null;
        }
        return donHangDAO.timTheoMa(maDonHang.trim());
    }

    /**
     * Lấy danh sách chi tiết sản phẩm của một đơn hàng.
     */
    public List<ChiTietDonHang> layChiTietTheoMaDonHang(String maDonHang) {
        if (maDonHang == null || maDonHang.trim().isEmpty()) {
            return List.of();
        }
        return donHangDAO.layChiTietTheoMaDonHang(maDonHang.trim());
    }

}

