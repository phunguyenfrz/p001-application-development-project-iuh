package com.petshop.service;

import com.petshop.dao.DonNhapHangDAO;
import com.petshop.model.ChiTietDonNhap;
import com.petshop.model.DonNhapHang;
import com.petshop.model.TaiKhoan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * Lớp dịch vụ nghiệp vụ Đơn nhập hàng.
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
     * Lập đơn nhập hàng mới (IMP_CREATE).
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
     * Xác nhận nhận hàng và cộng tồn kho (IMP_CONFIRM).
     */
    public boolean xacNhanNhanHang(String maDonHang) {
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

        return donNhapHangDAO.xacNhanNhanHangVaCongTonKho(maDonHang);
    }

    /**
     * Hủy đơn nhập hàng chưa nhận (IMP_CANCEL).
     */
    public boolean huyDonNhapHang(String maDonHang) {
        xacThucService.kiemTraQuyen("DN_CAP_NHAT");

        if (maDonHang == null || maDonHang.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã đơn hàng không được để trống!");
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

        return donNhapHangDAO.capNhatTrangThai(maDonHang,
                DonNhapHang.TRANG_THAI_CHO_NHAN, DonNhapHang.TRANG_THAI_DA_HUY);
    }

    /**
     * Tìm kiếm đơn nhập hàng đa tiêu chí (IMP_SEARCH).
     */
    public List<DonNhapHang> timKiemDonHang(String keyword, String maNhaCungCap, LocalDate tuNgay, LocalDate denNgay, String trangThai) {
        xacThucService.kiemTraQuyen("DN_TIM_KIEM");
        if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
            throw new IllegalArgumentException("Từ ngày không được lớn hơn Đến ngày!");
        }
        return donNhapHangDAO.timKiemDonHang(keyword, maNhaCungCap, tuNgay, denNgay, trangThai);
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

