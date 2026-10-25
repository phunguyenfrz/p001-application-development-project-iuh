package com.petshop.service;

import com.petshop.dao.DichVuChamSocDAO;
import com.petshop.model.BangGiaDichVu;
import com.petshop.model.DichVuChamSoc;

import java.time.LocalDate;
import java.util.List;

/**
 * Xử lý nghiệp vụ, kiểm tra dữ liệu hợp lệ và phân quyền cho phân hệ Dịch vụ chăm sóc.
 */
public class DichVuChamSocService {

    private final DichVuChamSocDAO dichVuChamSocDAO;
    private final XacThucService xacThucService;

    public DichVuChamSocService() {
        this.dichVuChamSocDAO = new DichVuChamSocDAO();
        this.xacThucService = new XacThucService();
    }

    public DichVuChamSocService(DichVuChamSocDAO dichVuChamSocDAO, XacThucService xacThucService) {
        this.dichVuChamSocDAO = dichVuChamSocDAO;
        this.xacThucService = xacThucService;
    }

    // Thêm dịch vụ mới (yêu cầu quyền DVCS_CAP_NHAT: Admin hoặc Quản lý)
    public boolean themDichVu(DichVuChamSoc dichVu) {
        xacThucService.kiemTraQuyen("DVCS_CAP_NHAT");
        kiemTraDuLieuDichVu(dichVu);

        if (dichVu.getMaDichVu() == null || dichVu.getMaDichVu().trim().isEmpty()) {
            dichVu.setMaDichVu(dichVuChamSocDAO.taoMaDichVu());
        } else {
            String ma = dichVu.getMaDichVu().trim();
            if (!ma.matches("^DV-\\d{8}-\\d{6}$")) {
                throw new IllegalArgumentException("Mã dịch vụ phải đúng định dạng DV-yyyyMMdd-xxxxxx!");
            }
            dichVu.setMaDichVu(ma);
        }

        return dichVuChamSocDAO.themDichVu(dichVu);
    }

    // Cập nhật thông tin và bảng giá 3 bậc của dịch vụ
    public boolean capNhatDichVu(DichVuChamSoc dichVu) {
        xacThucService.kiemTraQuyen("DVCS_CAP_NHAT");
        if (dichVu == null || dichVu.getMaDichVu() == null || dichVu.getMaDichVu().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã dịch vụ cần cập nhật không được để trống!");
        }
        kiemTraDuLieuDichVu(dichVu);
        dichVu.setMaDichVu(dichVu.getMaDichVu().trim());

        return dichVuChamSocDAO.capNhatDichVu(dichVu);
    }

    // Xóa mềm dịch vụ (chuyển sang ngừng kinh doanh)
    public boolean xoaDichVu(String maDichVu) {
        xacThucService.kiemTraQuyen("DVCS_CAP_NHAT");
        if (maDichVu == null || maDichVu.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn dịch vụ cần ngừng kinh doanh!");
        }
        return dichVuChamSocDAO.xoaDichVu(maDichVu.trim());
    }

    // Lấy toàn bộ danh sách dịch vụ (cho phép Admin, Quản lý, Chăm sóc)
    public List<DichVuChamSoc> layDanhSachDichVu() {
        xacThucService.kiemTraQuyen("DVCS_TIM_KIEM");
        return dichVuChamSocDAO.layDanhSachDichVu();
    }

    // Hàm cung cấp cho #68 (Lịch hẹn), #69 (Phiếu dịch vụ), #71 (Hóa đơn): lấy dịch vụ đang kinh doanh
    public List<DichVuChamSoc> layDanhSachDichVuDangKinhDoanh() {
        xacThucService.kiemTraQuyen("DVCS_TIM_KIEM");
        return dichVuChamSocDAO.layDanhSachDichVuDangKinhDoanh();
    }

    // Lấy thông tin một dịch vụ theo mã
    public DichVuChamSoc layDichVuTheoMa(String maDichVu) {
        xacThucService.kiemTraQuyen("DVCS_TIM_KIEM");
        if (maDichVu == null || maDichVu.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã dịch vụ không được để trống!");
        }
        return dichVuChamSocDAO.layDichVuTheoMa(maDichVu.trim());
    }

    // Tìm kiếm dịch vụ theo mã hoặc tên
    public List<DichVuChamSoc> timKiemDichVu(String tuKhoa) {
        xacThucService.kiemTraQuyen("DVCS_TIM_KIEM");
        String tuKhoaChuan = (tuKhoa == null) ? "" : tuKhoa.trim();
        return dichVuChamSocDAO.timKiemDichVu(tuKhoaChuan);
    }

    // Lấy toàn bộ danh sách bảng giá của các dịch vụ (yêu cầu quyền DVCS_BANG_GIA)
    public List<BangGiaDichVu> layTatCaBangGia() {
        xacThucService.kiemTraQuyen("DVCS_BANG_GIA");
        return dichVuChamSocDAO.layTatCaBangGia();
    }

    // Hàm cung cấp cho #68 (Lịch hẹn), #69 (Phiếu dịch vụ), #71 (Hóa đơn): tra đơn giá theo cân nặng
    public double traGiaTheoCanNang(String maDichVu, double canNang) {
        xacThucService.kiemTraQuyen("DVCS_TIM_KIEM");
        if (maDichVu == null || maDichVu.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã dịch vụ không được để trống!");
        }
        if (canNang <= 0) {
            throw new IllegalArgumentException("Cân nặng thú cưng phải lớn hơn 0!");
        }
        return dichVuChamSocDAO.traGiaTheoCanNang(maDichVu.trim(), canNang);
    }

    // Thống kê báo cáo dịch vụ chăm sóc theo khoảng ngày (yêu cầu quyền DVCS_BAO_CAO)
    public List<String[]> thongKeDichVu(LocalDate tuNgay, LocalDate denNgay) {
        xacThucService.kiemTraQuyen("DVCS_BAO_CAO");
        if (tuNgay == null || denNgay == null) {
            throw new IllegalArgumentException("Vui lòng chọn đầy đủ từ ngày và đến ngày!");
        }
        if (denNgay.isBefore(tuNgay)) {
            throw new IllegalArgumentException("Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu!");
        }
        return dichVuChamSocDAO.thongKeDichVu(tuNgay, denNgay);
    }

    // Kiểm tra các quy tắc nghiệp vụ của dịch vụ chăm sóc
    private void kiemTraDuLieuDichVu(DichVuChamSoc dichVu) {
        if (dichVu == null) {
            throw new IllegalArgumentException("Dữ liệu dịch vụ không hợp lệ!");
        }

        String ten = dichVu.getTenDichVu() == null ? "" : dichVu.getTenDichVu().trim();
        if (ten.isEmpty()) {
            throw new IllegalArgumentException("Tên dịch vụ không được để trống!");
        }
        if (ten.length() > 100) {
            throw new IllegalArgumentException("Tên dịch vụ không được vượt quá 100 ký tự!");
        }
        dichVu.setTenDichVu(ten);

        if (dichVu.getMoTa() != null) {
            String moTa = dichVu.getMoTa().trim();
            if (moTa.length() > 255) {
                throw new IllegalArgumentException("Mô tả dịch vụ không được vượt quá 255 ký tự!");
            }
            dichVu.setMoTa(moTa);
        }

        if (dichVu.getThoiLuong() <= 0) {
            throw new IllegalArgumentException("Thời gian thực hiện phải lớn hơn 0 phút!");
        }

        if (dichVu.getGiaNho() <= 0 || dichVu.getGiaVua() <= 0 || dichVu.getGiaLon() <= 0) {
            throw new IllegalArgumentException("Giá dịch vụ của cả 3 bậc (Nhỏ, Vừa, Lớn) phải lớn hơn 0!");
        }
    }
}
