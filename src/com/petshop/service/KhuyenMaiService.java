package com.petshop.service;

import com.petshop.dao.KhuyenMaiDAO;
import com.petshop.model.KhuyenMai;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Xử lý nghiệp vụ, kiểm tra dữ liệu và phân quyền cho khuyến mãi.
 */
public class KhuyenMaiService {

    private final KhuyenMaiDAO khuyenMaiDAO;
    private final XacThucService xacThucService;

    public KhuyenMaiService() {
        this.khuyenMaiDAO = new KhuyenMaiDAO();
        this.xacThucService = new XacThucService();
    }

    public KhuyenMaiService(KhuyenMaiDAO khuyenMaiDAO, XacThucService xacThucService) {
        this.khuyenMaiDAO = khuyenMaiDAO;
        this.xacThucService = xacThucService;
    }

    // Thêm chương trình khuyến mãi mới cho Quản lý hoặc Admin.
    public boolean themKhuyenMai(KhuyenMai khuyenMai) {
        xacThucService.kiemTraQuyen("KM_CAP_NHAT");
        kiemTraDuLieuKhuyenMai(khuyenMai);
        if (khuyenMai.getMaKhuyenMai() == null || khuyenMai.getMaKhuyenMai().trim().isEmpty()) {
            khuyenMai.setMaKhuyenMai(khuyenMaiDAO.taoMaKhuyenMai());
        } else {
            String ma = khuyenMai.getMaKhuyenMai().trim();
            if (!ma.matches("^KM-\\d{8}-\\d{6}$")) {
                throw new IllegalArgumentException("Mã khuyến mãi phải đúng định dạng KM-yyyyMMdd-xxxxxx!");
            }
            khuyenMai.setMaKhuyenMai(ma);
        }
        return khuyenMaiDAO.themKhuyenMai(khuyenMai);
    }

    // Cập nhật thông tin chương trình khuyến mãi.
    public boolean capNhatKhuyenMai(KhuyenMai khuyenMai) {
        xacThucService.kiemTraQuyen("KM_CAP_NHAT");
        if (khuyenMai == null || khuyenMai.getMaKhuyenMai() == null || khuyenMai.getMaKhuyenMai().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khuyến mãi cần cập nhật không được để trống!");
        }
        kiemTraDuLieuKhuyenMai(khuyenMai);
        khuyenMai.setMaKhuyenMai(khuyenMai.getMaKhuyenMai().trim());
        return khuyenMaiDAO.capNhatKhuyenMai(khuyenMai);
    }

    // Ngừng khuyến mãi bằng xóa mềm.
    public boolean xoaKhuyenMai(String maKhuyenMai) {
        xacThucService.kiemTraQuyen("KM_CAP_NHAT");
        if (maKhuyenMai == null || maKhuyenMai.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn khuyến mãi cần ngừng áp dụng!");
        }
        return khuyenMaiDAO.xoaKhuyenMai(maKhuyenMai.trim());
    }

    public List<KhuyenMai> layDanhSachKhuyenMai() {
        xacThucService.kiemTraQuyen("KM_TIM_KIEM");
        return khuyenMaiDAO.layDanhSachKhuyenMai();
    }

    public KhuyenMai layKhuyenMaiTheoMa(String maKhuyenMai) {
        xacThucService.kiemTraQuyen("KM_TIM_KIEM");
        if (maKhuyenMai == null || maKhuyenMai.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khuyến mãi không được để trống!");
        }
        return khuyenMaiDAO.layKhuyenMaiTheoMa(maKhuyenMai.trim());
    }

    public List<KhuyenMai> timKiemKhuyenMai(String maKhuyenMai, String tenChuongTrinh, Boolean trangThai) {
        xacThucService.kiemTraQuyen("KM_TIM_KIEM");
        return khuyenMaiDAO.timKiemKhuyenMai(maKhuyenMai, tenChuongTrinh, trangThai);
    }

    // Hàm cung cấp cho #71 Hóa đơn: mã phải tồn tại, đang hoạt động và nằm trong thời gian áp dụng.
    public boolean kiemTraKhuyenMaiHopLe(String maKhuyenMai) {
        if (maKhuyenMai == null || maKhuyenMai.trim().isEmpty()) {
            return false;
        }
        KhuyenMai khuyenMai = khuyenMaiDAO.layKhuyenMaiTheoMa(maKhuyenMai.trim());
        if (khuyenMai == null || !khuyenMai.isTrangThai()) {
            return false;
        }
        LocalDate homNay = LocalDate.now();
        return !homNay.isBefore(khuyenMai.getNgayBatDau())
                && !homNay.isAfter(khuyenMai.getNgayKetThuc());
    }

    // Hàm hỗ trợ tính giảm giá trên tổng hóa đơn; một hóa đơn chỉ áp dụng tối đa một mã.
    public BigDecimal tinhSoTienGiam(String maKhuyenMai, BigDecimal tongHoaDon) {
        if (tongHoaDon == null || tongHoaDon.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Tổng hóa đơn phải lớn hơn 0!");
        }
        if (!kiemTraKhuyenMaiHopLe(maKhuyenMai)) {
            throw new IllegalArgumentException("Khuyến mãi không hợp lệ hoặc đã hết thời gian áp dụng!");
        }
        KhuyenMai khuyenMai = khuyenMaiDAO.layKhuyenMaiTheoMa(maKhuyenMai.trim());
        return tongHoaDon.multiply(khuyenMai.getPhanTramGiam()).divide(new BigDecimal("100"));
    }

    private void kiemTraDuLieuKhuyenMai(KhuyenMai khuyenMai) {
        if (khuyenMai == null) {
            throw new IllegalArgumentException("Dữ liệu khuyến mãi không hợp lệ!");
        }
        String ten = khuyenMai.getTenChuongTrinh() == null ? "" : khuyenMai.getTenChuongTrinh().trim();
        if (ten.isEmpty()) {
            throw new IllegalArgumentException("Tên chương trình khuyến mãi không được để trống!");
        }
        if (ten.length() > 150) {
            throw new IllegalArgumentException("Tên chương trình không được vượt quá 150 ký tự!");
        }
        khuyenMai.setTenChuongTrinh(ten);

        BigDecimal phanTram = khuyenMai.getPhanTramGiam();
        if (phanTram == null || phanTram.compareTo(BigDecimal.ZERO) <= 0 || phanTram.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Mức giảm phải lớn hơn 0% và không vượt quá 100%!");
        }
        if (khuyenMai.getNgayBatDau() == null || khuyenMai.getNgayKetThuc() == null) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ ngày bắt đầu và ngày kết thúc!");
        }
        if (khuyenMai.getNgayKetThuc().isBefore(khuyenMai.getNgayBatDau())) {
            throw new IllegalArgumentException("Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu!");
        }
    }
}
