package com.petshop.service;

import com.petshop.dao.SanPhamDAO;
import com.petshop.model.NhaCungCap;
import com.petshop.model.SanPham;

import java.math.BigDecimal;
import java.util.List;

/**
 * Xử lý nghiệp vụ, kiểm tra dữ liệu hợp lệ và phân quyền cho phân hệ Sản phẩm.
 */
public class SanPhamService {

    private final SanPhamDAO sanPhamDAO;
    private final XacThucService xacThucService;

    public SanPhamService() {
        this.sanPhamDAO = new SanPhamDAO();
        this.xacThucService = new XacThucService();
    }

    public SanPhamService(SanPhamDAO sanPhamDAO, XacThucService xacThucService) {
        this.sanPhamDAO = sanPhamDAO;
        this.xacThucService = xacThucService;
    }

    /**
     * Thêm mới sản phẩm (Quyền SP_CAP_NHAT).
     */
    public boolean themSanPham(SanPham sp) {
        xacThucService.kiemTraQuyen("SP_CAP_NHAT");

        if (sp == null) {
            throw new IllegalArgumentException("Thông tin sản phẩm không được để trống!");
        }

        if (sp.getMaSanPham() == null || sp.getMaSanPham().trim().isEmpty()) {
            sp.setMaSanPham(sanPhamDAO.taoMaSanPham());
        } else {
            String ma = sp.getMaSanPham().trim();
            if (!ma.matches("^SP-\\d{8}-\\d{6}$")) {
                throw new IllegalArgumentException("Mã sản phẩm phải đúng định dạng SP-yyyyMMdd-xxxxxx!");
            }
            sp.setMaSanPham(ma);
        }

        // Tự động phát sinh mã vạch theo mã sản phẩm nếu để trống
        if (sp.getMaVach() == null || sp.getMaVach().trim().isEmpty()) {
            sp.setMaVach("893" + sp.getMaSanPham().replace("SP-", "").replace("-", ""));
        }

        kiemTraDuLieuSanPham(sp);

        if (sanPhamDAO.kiemTraTonTaiMaVach(sp.getMaVach().trim(), null)) {
            throw new IllegalArgumentException("Mã vạch đã tồn tại trên hệ thống!");
        }

        return sanPhamDAO.themSanPham(sp);
    }

    /**
     * Cập nhật thông tin sản phẩm (Quyền SP_CAP_NHAT).
     */
    public boolean capNhatSanPham(SanPham sp) {
        xacThucService.kiemTraQuyen("SP_CAP_NHAT");
        if (sp == null || sp.getMaSanPham() == null || sp.getMaSanPham().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã sản phẩm cần cập nhật không được để trống!");
        }
        kiemTraDuLieuSanPham(sp);

        if (sanPhamDAO.kiemTraTonTaiMaVach(sp.getMaVach().trim(), sp.getMaSanPham().trim())) {
            throw new IllegalArgumentException("Mã vạch đã tồn tại trên hệ thống!");
        }

        return sanPhamDAO.capNhatSanPham(sp);
    }

    /** Xóa sản phẩm nếu không còn tồn kho và chưa phát sinh giao dịch. */
    public boolean xoaSanPham(String maSanPham) {
        xacThucService.kiemTraQuyen("SP_CAP_NHAT");
        if (maSanPham == null || maSanPham.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã sản phẩm cần xóa không được để trống!");
        }
        return sanPhamDAO.xoaSanPham(maSanPham.trim());
    }

    /** Ngừng kinh doanh hoặc kinh doanh lại sản phẩm, giữ nguyên lịch sử. */
    public boolean capNhatTrangThaiSanPham(String maSanPham, boolean trangThai) {
        xacThucService.kiemTraQuyen("SP_CAP_NHAT");
        if (maSanPham == null || maSanPham.isBlank()) {
            throw new IllegalArgumentException("Mã sản phẩm không được để trống!");
        }
        return sanPhamDAO.capNhatTrangThaiSanPham(maSanPham.trim(), trangThai);
    }

    /**
     * Lấy toàn bộ danh sách sản phẩm.
     */
    public List<SanPham> layDanhSachSanPham() {
        return sanPhamDAO.layDanhSachSanPham();
    }

    /**
     * Lấy thông tin sản phẩm theo mã.
     */
    public SanPham layTheoMa(String maSanPham) {
        if (maSanPham == null || maSanPham.trim().isEmpty()) {
            return null;
        }
        return sanPhamDAO.layTheoMa(maSanPham.trim());
    }

    /**
     * Tìm kiếm sản phẩm theo từ khóa và trạng thái (Quyền SP_TIM_KIEM).
     */
    public List<SanPham> timKiemSanPham(String tuKhoa, Boolean trangThai) {
        xacThucService.kiemTraQuyen("SP_TIM_KIEM");
        return sanPhamDAO.timKiemSanPham(tuKhoa, trangThai);
    }

    /**
     * Lấy danh sách sản phẩm tồn kho dưới ngưỡng an toàn (Quyền SP_TON_KHO).
     */
    public List<SanPham> layDanhSachSanPhamSapHet() {
        xacThucService.kiemTraQuyen("SP_TON_KHO");
        return sanPhamDAO.layDanhSachSanPhamSapHet();
    }

    /**
     * Tra cứu số lượng tồn kho theo mã sản phẩm (Cung cấp cho Issue #71 Hóa đơn).
     */
    public int traCuuTonKho(String maSanPham) {
        if (maSanPham == null || maSanPham.trim().isEmpty()) {
            return 0;
        }
        return sanPhamDAO.traCuuTonKho(maSanPham.trim());
    }

    /**
     * Cập nhật số lượng tồn kho (Cung cấp cho Issue #71 Hóa đơn và #67 Đơn nhập hàng).
     */
    public boolean capNhatSoLuongTon(String maSanPham, int soLuongThayDoi) {
        if (maSanPham == null || maSanPham.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã sản phẩm không được để trống!");
        }
        return sanPhamDAO.capNhatSoLuongTon(maSanPham.trim(), soLuongThayDoi);
    }

    /**
     * Tự động sinh mã sản phẩm mới.
     */
    public String taoMaSanPham() {
        return sanPhamDAO.taoMaSanPham();
    }

    /**
     * Lấy danh sách nhà cung cấp để hiển thị lựa chọn.
     */
    public List<NhaCungCap> layDanhSachNhaCungCap() {
        return sanPhamDAO.layDanhSachNhaCungCap();
    }

    private void kiemTraDuLieuSanPham(SanPham sp) {
        if (sp == null) {
            throw new IllegalArgumentException("Thông tin sản phẩm không được để trống!");
        }
        if (sp.getTenSanPham() == null || sp.getTenSanPham().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống!");
        }
        if (sp.getMaVach() == null || sp.getMaVach().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã vạch sản phẩm không được để trống!");
        }
        if (sp.getGiaBan() == null || sp.getGiaBan().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá bán sản phẩm phải lớn hơn hoặc bằng 0!");
        }
        if (sp.getSoLuongTon() < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không được âm!");
        }
        if (sp.getNguongTonToiThieu() < 0) {
            throw new IllegalArgumentException("Ngưỡng tồn tối thiểu không được âm!");
        }
        if (sp.getDonViTinh() == null || sp.getDonViTinh().trim().isEmpty()) {
            throw new IllegalArgumentException("Loại sản phẩm / đơn vị tính không được để trống!");
        }
    }
}
