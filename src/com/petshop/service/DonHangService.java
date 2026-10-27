package com.petshop.service;

import com.petshop.dao.DonHangDAO;
import com.petshop.model.DonNhap;
import com.petshop.model.TaiKhoan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DonHangService {

    private final DonHangDAO donHangDAO;
    private final XacThucService xacThucService;

    public DonHangService() {
        this(new DonHangDAO(), new XacThucService());
    }

    public DonHangService(DonHangDAO donHangDAO, XacThucService xacThucService) {
        this.donHangDAO = donHangDAO;
        this.xacThucService = xacThucService;
    }

    public List<DonNhap.NhaCungCap> layDanhSachNhaCungCap() {
        xacThucService.kiemTraQuyen("DN_TAO");
        return donHangDAO.layDanhSachNhaCungCap();
    }

    public List<DonNhap.SanPham> layDanhSachSanPham(String maNhaCungCap) {
        xacThucService.kiemTraQuyen("DN_TAO");
        if (maNhaCungCap == null || maNhaCungCap.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn nhà cung cấp.");
        }
        return donHangDAO.layDanhSachSanPham(maNhaCungCap.trim());
    }

    public String taoDonNhap(String maNhaCungCap, List<DonNhap.ChiTiet> chiTiet) {
        xacThucService.kiemTraQuyen("DN_TAO");
        if (maNhaCungCap == null || maNhaCungCap.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn nhà cung cấp.");
        }
        if (chiTiet == null || chiTiet.isEmpty()) {
            throw new IllegalArgumentException("Đơn nhập phải có ít nhất một sản phẩm.");
        }

        Set<String> maSanPhamDaThem = new HashSet<>();
        BigDecimal tongTien = BigDecimal.ZERO;
        for (DonNhap.ChiTiet dong : chiTiet) {
            if (dong == null || dong.maSanPham() == null || dong.maSanPham().isBlank()
                    || dong.soLuong() <= 0 || dong.donGia() == null || dong.donGia().signum() < 0) {
                throw new IllegalArgumentException("Thông tin sản phẩm, số lượng hoặc đơn giá không hợp lệ.");
            }
            if (!maSanPhamDaThem.add(dong.maSanPham())) {
                throw new IllegalArgumentException("Mỗi sản phẩm chỉ được xuất hiện một lần trong đơn nhập.");
            }
            tongTien = tongTien.add(dong.donGia().multiply(BigDecimal.valueOf(dong.soLuong())));
        }

        TaiKhoan taiKhoan = xacThucService.getTaiKhoanHienTai();
        if (taiKhoan.getMaNhanVien() == null || taiKhoan.getMaNhanVien().isBlank()) {
            throw new IllegalStateException("Tài khoản hiện tại chưa được liên kết với nhân viên.");
        }
        return donHangDAO.taoDonNhap(maNhaCungCap.trim(), taiKhoan.getMaNhanVien().trim(), chiTiet, tongTien);
    }

    public List<DonNhap> layDonChoNhanHang() {
        xacThucService.kiemTraQuyen("DN_NHAN_HANG");
        return donHangDAO.layDonChoNhanHang();
    }

    public List<DonNhap> timKiemDonNhap(String maDonHang, String tenNhaCungCap,
                                       LocalDate tuNgay, LocalDate denNgay) {
        xacThucService.kiemTraQuyen("DN_TIM_KIEM");
        if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
            throw new IllegalArgumentException("Ngày bắt đầu không được sau ngày kết thúc.");
        }
        return donHangDAO.timKiemDonNhap(maDonHang, tenNhaCungCap, tuNgay, denNgay);
    }

    public List<DonNhap.ChiTiet> layChiTietDonHang(String maDonHang) {
        xacThucService.kiemTraQuyen("DN_TIM_KIEM");
        if (maDonHang == null || maDonHang.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn đơn nhập.");
        }
        return donHangDAO.layChiTietDonHang(maDonHang.trim());
    }

    public boolean xacNhanNhanHang(String maDonHang) {
        xacThucService.kiemTraQuyen("DN_NHAN_HANG");
        if (maDonHang == null || maDonHang.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn đơn nhập cần xác nhận.");
        }
        return donHangDAO.xacNhanNhanHang(maDonHang.trim());
    }

    public boolean huyDonNhap(String maDonHang) {
        xacThucService.kiemTraQuyen("IMP_CANCEL");
        if (maDonHang == null || maDonHang.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn đơn nhập cần hủy.");
        }
        return donHangDAO.huyDonNhap(maDonHang.trim());
    }
}