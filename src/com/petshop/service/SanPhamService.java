package com.petshop.service;

import com.petshop.dao.SanPhamDAO;
import com.petshop.model.SanPham;

import java.math.BigDecimal;
import java.util.List;

public class SanPhamService {

    private final SanPhamDAO sanPhamDAO;
    private final XacThucService xacThucService;

    public SanPhamService() {
        this(new SanPhamDAO(), new XacThucService());
    }

    public SanPhamService(SanPhamDAO sanPhamDAO, XacThucService xacThucService) {
        this.sanPhamDAO = sanPhamDAO;
        this.xacThucService = xacThucService;
    }

    public List<SanPham.NhaCungCapSimple> layDanhSachNhaCungCap() {
        return sanPhamDAO.layDanhSachNhaCungCap();
    }

    public String themSanPham(SanPham sp) {
        xacThucService.kiemTraQuyen("SP_CAP_NHAT");
        kiemTraThongTinSanPham(sp, true);
        return sanPhamDAO.themSanPham(sp);
    }

    public boolean capNhatSanPham(SanPham sp) {
        xacThucService.kiemTraQuyen("SP_CAP_NHAT");
        if (sp == null || sp.getMaSanPham() == null || sp.getMaSanPham().isBlank()) {
            throw new IllegalArgumentException("Mã sản phẩm không được để trống khi cập nhật.");
        }
        kiemTraThongTinSanPham(sp, false);
        return sanPhamDAO.capNhatSanPham(sp);
    }

    public boolean xoaMemSanPham(String maSanPham) {
        xacThucService.kiemTraQuyen("SP_TIM_KIEM");
        if (maSanPham == null || maSanPham.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn sản phẩm cần xóa.");
        }
        return sanPhamDAO.xoaMemSanPham(maSanPham.trim());
    }

    public SanPham laySanPhamTheoMa(String maSanPham) {
        if (maSanPham == null || maSanPham.isBlank()) {
            return null;
        }
        return sanPhamDAO.laySanPhamTheoMa(maSanPham.trim());
    }

    public List<SanPham> timKiemSanPham(String tuKhoa, String maNhaCungCap, Boolean chiDangKinhDoanh) {
        xacThucService.kiemTraQuyen("SP_TIM_KIEM");
        return sanPhamDAO.timKiemSanPham(tuKhoa, maNhaCungCap, chiDangKinhDoanh);
    }

    public List<SanPham> layDanhSachTonKho(boolean chiDuoiNguong) {
        xacThucService.kiemTraQuyen("SP_TON_KHO");
        return sanPhamDAO.layDanhSachTonKho(chiDuoiNguong);
    }

    public boolean capNhatSoLuongTon(String maSanPham, int soLuongThayDoi) {
        if (maSanPham == null || maSanPham.isBlank()) {
            throw new IllegalArgumentException("Mã sản phẩm không được để trống.");
        }
        return sanPhamDAO.capNhatSoLuongTon(maSanPham.trim(), soLuongThayDoi);
    }

    private void kiemTraThongTinSanPham(SanPham sp, boolean laThemMoi) {
        if (sp == null) {
            throw new IllegalArgumentException("Thông tin sản phẩm không được rỗng.");
        }
        if (sp.getTenSanPham() == null || sp.getTenSanPham().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống.");
        }
        if (sp.getMaVach() == null || sp.getMaVach().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã vạch không được để trống.");
        }
        if (sanPhamDAO.tonTaiMaVach(sp.getMaVach().trim(), laThemMoi ? null : sp.getMaSanPham())) {
            throw new IllegalArgumentException("Mã vạch đã tồn tại trên hệ thống.");
        }
        if (sp.getGiaBan() == null || sp.getGiaBan().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá bán không hợp lệ (phải >= 0).");
        }
        if (sp.getNguongTonToiThieu() < 0) {
            throw new IllegalArgumentException("Ngưỡng tồn tối thiểu phải >= 0.");
        }
        if (sp.getDonViTinh() == null || sp.getDonViTinh().trim().isEmpty()) {
            throw new IllegalArgumentException("Đơn vị tính không được để trống.");
        }
        if (laThemMoi && sp.getSoLuongTon() < 0) {
            throw new IllegalArgumentException("Số lượng tồn ban đầu phải >= 0.");
        }
    }
}
