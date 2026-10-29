package com.petshop.service;

import com.petshop.dao.LichHenDAO;
import com.petshop.model.ChiTietLichHen;
import com.petshop.model.LichHen;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Lớp dịch vụ (Service Layer) xử lý nghiệp vụ, kiểm tra ràng buộc và phân quyền cho lịch hẹn.
 */
public class LichHenService {

    private final LichHenDAO lichHenDAO;
    private final XacThucService xacThucService;

    public LichHenService() {
        this.lichHenDAO = new LichHenDAO();
        this.xacThucService = new XacThucService();
    }

    public LichHenService(LichHenDAO lichHenDAO, XacThucService xacThucService) {
        this.lichHenDAO = lichHenDAO;
        this.xacThucService = xacThucService;
    }

    // Đặt lịch hẹn mới kèm danh sách dịch vụ (vai trò CHAM_SOC, ADMIN)
    public boolean datLichHen(LichHen lichHen, List<ChiTietLichHen> danhSachChiTiet) {
        xacThucService.kiemTraQuyen("LH_TAO");
        kiemTraDuLieuLichHen(lichHen, danhSachChiTiet, false);

        if (lichHen.getMaLichHen() == null || lichHen.getMaLichHen().trim().isEmpty()) {
            lichHen.setMaLichHen(lichHenDAO.taoMaLichHen());
        }

        // Mặc định lịch hẹn mới có trạng thái 'Đã đặt'
        if (lichHen.getTrangThai() == null || lichHen.getTrangThai().trim().isEmpty()) {
            lichHen.setTrangThai("Đã đặt");
        }

        return lichHenDAO.themLichHen(lichHen, danhSachChiTiet);
    }

    // Đổi ngày giờ hoặc dịch vụ của lịch hẹn đã có (APT_RESCHEDULE)
    public boolean capNhatLichHen(LichHen lichHen, List<ChiTietLichHen> danhSachChiTiet) {
        xacThucService.kiemTraQuyen("LH_CAP_NHAT");

        if (lichHen == null || lichHen.getMaLichHen() == null || lichHen.getMaLichHen().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã lịch hẹn cần cập nhật không được để trống!");
        }

        LichHen lichCu = lichHenDAO.layLichHenTheoMa(lichHen.getMaLichHen().trim());
        if (lichCu == null) {
            throw new IllegalArgumentException("Không tìm thấy lịch hẹn mã: " + lichHen.getMaLichHen());
        }

        if ("Hoàn thành".equalsIgnoreCase(lichCu.getTrangThai())) {
            throw new IllegalArgumentException("Lịch hẹn đã hoàn thành, không thể sửa đổi thông tin!");
        }
        if ("Đã hủy".equalsIgnoreCase(lichCu.getTrangThai())) {
            throw new IllegalArgumentException("Lịch hẹn đã bị hủy, không thể sửa đổi thông tin!");
        }

        kiemTraDuLieuLichHen(lichHen, danhSachChiTiet, true);
        return lichHenDAO.capNhatLichHen(lichHen, danhSachChiTiet);
    }

    // Cập nhật trạng thái duyệt lịch: Đã đặt -> Đã xác nhận -> Hoàn thành (APT_STATUS)
    public boolean capNhatTrangThai(String maLichHen, String trangThaiMoi) {
        xacThucService.kiemTraQuyen("LH_CAP_NHAT");

        if (maLichHen == null || maLichHen.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã lịch hẹn không được để trống!");
        }
        if (trangThaiMoi == null || trangThaiMoi.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn trạng thái mới!");
        }

        LichHen lichHienTai = lichHenDAO.layLichHenTheoMa(maLichHen.trim());
        if (lichHienTai == null) {
            throw new IllegalArgumentException("Không tìm thấy lịch hẹn mã: " + maLichHen);
        }

        String trangThaiCu = lichHienTai.getTrangThai();
        kiemTraChuyenDoiTrangThai(trangThaiCu, trangThaiMoi.trim());

        return lichHenDAO.capNhatTrangThai(maLichHen.trim(), trangThaiMoi.trim());
    }

    // Hủy lịch hẹn có kèm lý do hủy (xóa mềm)
    public boolean huyLichHen(String maLichHen, String lyDoHuy) {
        xacThucService.kiemTraQuyen("LH_CAP_NHAT");

        if (maLichHen == null || maLichHen.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã lịch hẹn cần hủy không được để trống!");
        }

        LichHen lichHienTai = lichHenDAO.layLichHenTheoMa(maLichHen.trim());
        if (lichHienTai == null) {
            throw new IllegalArgumentException("Không tìm thấy lịch hẹn mã: " + maLichHen);
        }

        if ("Hoàn thành".equalsIgnoreCase(lichHienTai.getTrangThai())) {
            throw new IllegalArgumentException("Lịch hẹn đã hoàn thành, không thể hủy!");
        }
        if ("Đã hủy".equalsIgnoreCase(lichHienTai.getTrangThai())) {
            throw new IllegalArgumentException("Lịch hẹn này đã bị hủy trước đó!");
        }

        // Cập nhật lý do hủy vào ghi chú nếu có
        if (lyDoHuy != null && !lyDoHuy.trim().isEmpty()) {
            String ghiChuCu = lichHienTai.getGhiChu() != null ? lichHienTai.getGhiChu() : "";
            String ghiChuMoi = (ghiChuCu.isEmpty() ? "" : ghiChuCu + " | ") + "Lý do hủy: " + lyDoHuy.trim();
            lichHienTai.setGhiChu(ghiChuMoi);
            lichHenDAO.capNhatLichHen(lichHienTai, null);
        }

        return lichHenDAO.capNhatTrangThai(maLichHen.trim(), "Đã hủy");
    }

    // Lấy toàn bộ danh sách lịch hẹn
    public List<LichHen> layDanhSachLichHen() {
        xacThucService.kiemTraQuyen("LH_TIM_KIEM");
        return lichHenDAO.layDanhSachLichHen();
    }

    // Tìm một lịch hẹn theo mã duy nhất
    public LichHen layLichHenTheoMa(String maLichHen) {
        xacThucService.kiemTraQuyen("LH_TIM_KIEM");
        if (maLichHen == null || maLichHen.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã lịch hẹn không được để trống!");
        }
        return lichHenDAO.layLichHenTheoMa(maLichHen.trim());
    }

    // Lấy danh sách dịch vụ chi tiết của lịch hẹn
    public List<ChiTietLichHen> layChiTietLichHen(String maLichHen) {
        xacThucService.kiemTraQuyen("LH_TIM_KIEM");
        if (maLichHen == null || maLichHen.trim().isEmpty()) {
            return List.of();
        }
        return lichHenDAO.layChiTietLichHen(maLichHen.trim());
    }

    // Tìm kiếm và lọc lịch hẹn theo khách hàng, ngày và trạng thái (APT_SEARCH)
    public List<LichHen> timKiemLichHen(String tuKhoaKhach, LocalDate tuNgay, LocalDate denNgay, String trangThai) {
        xacThucService.kiemTraQuyen("LH_TIM_KIEM");
        if (tuNgay != null && denNgay != null && denNgay.isBefore(tuNgay)) {
            throw new IllegalArgumentException("Đến ngày phải lớn hơn hoặc bằng từ ngày!");
        }
        return lichHenDAO.timKiemLichHen(tuKhoaKhach, tuNgay, denNgay, trangThai);
    }

    // Sinh mã lịch hẹn mới tiếp theo
    public String taoMaLichHenMoi() {
        return lichHenDAO.taoMaLichHen();
    }

    // Kiểm tra tính hợp lệ của dữ liệu lịch hẹn trước khi lưu CSDL
    private void kiemTraDuLieuLichHen(LichHen lichHen, List<ChiTietLichHen> danhSachChiTiet, boolean laCapNhat) {
        if (lichHen == null) {
            throw new IllegalArgumentException("Thông tin lịch hẹn không được để trống!");
        }

        // 1. Kiểm tra khách hàng
        if (lichHen.getMaKhachHang() == null || lichHen.getMaKhachHang().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn khách hàng đặt lịch!");
        }

        // 2. Kiểm tra thú cưng
        if (lichHen.getMaThuCung() == null || lichHen.getMaThuCung().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn thú cưng làm dịch vụ!");
        }

        // 3. Kiểm tra nhân viên phụ trách
        if (lichHen.getMaNhanVien() == null || lichHen.getMaNhanVien().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn nhân viên phụ trách chăm sóc!");
        }

        // 4. Kiểm tra ngày hẹn và giờ hẹn
        if (lichHen.getNgayHen() == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày hẹn dịch vụ!");
        }
        if (lichHen.getGioHen() == null) {
            throw new IllegalArgumentException("Vui lòng chọn giờ hẹn dịch vụ!");
        }

        // Chặn chọn ngày giờ hẹn trong quá khứ
        LocalDate homNay = LocalDate.now();
        LocalTime bayGio = LocalTime.now();
        if (lichHen.getNgayHen().isBefore(homNay)) {
            throw new IllegalArgumentException("Ngày hẹn không được ở trong quá khứ!");
        }
        if (lichHen.getNgayHen().isEqual(homNay) && lichHen.getGioHen().isBefore(bayGio)) {
            throw new IllegalArgumentException("Giờ hẹn hôm nay không được ở trong quá khứ!");
        }

        // 5. Kiểm tra danh sách dịch vụ chi tiết (nếu có cập nhật danh sách dịch vụ)
        if (danhSachChiTiet != null) {
            if (danhSachChiTiet.isEmpty()) {
                throw new IllegalArgumentException("Lịch hẹn phải có ít nhất một dịch vụ chăm sóc!");
            }
            for (ChiTietLichHen ct : danhSachChiTiet) {
                if (ct.getMaDichVu() == null || ct.getMaDichVu().trim().isEmpty()) {
                    throw new IllegalArgumentException("Mã dịch vụ trong chi tiết không hợp lệ!");
                }
                if (ct.getCanNangThuCung() == null || ct.getCanNangThuCung().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Cân nặng thú cưng cho dịch vụ phải lớn hơn 0 kg!");
                }
                if (ct.getDonGia() == null || ct.getDonGia().compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Đơn giá dịch vụ không được âm!");
                }
            }
        }

        // 6. Kiểm tra trùng lịch hẹn theo tiêu chí: cùng thú cưng hoặc cùng nhân viên tại khung giờ đó
        String maLichBoQua = laCapNhat ? lichHen.getMaLichHen() : null;
        boolean trungLich = lichHenDAO.kiemTraTrungLich(
                lichHen.getMaNhanVien(),
                lichHen.getMaThuCung(),
                lichHen.getNgayHen(),
                lichHen.getGioHen(),
                maLichBoQua
        );
        if (trungLich) {
            throw new IllegalArgumentException("Thú cưng hoặc nhân viên này đã có lịch hẹn khác vào thời điểm "
                    + lichHen.getGioHen().toString() + " ngày " + lichHen.getNgayHen().toString() + "!");
        }
    }

    // Kiểm tra quy tắc chuyển đổi trạng thái hợp lệ
    private void kiemTraChuyenDoiTrangThai(String trangThaiCu, String trangThaiMoi) {
        if (trangThaiCu == null) return;
        if (trangThaiCu.equalsIgnoreCase(trangThaiMoi)) return;

        if ("Hoàn thành".equalsIgnoreCase(trangThaiCu)) {
            throw new IllegalArgumentException("Lịch hẹn đã hoàn tất, không thể đổi sang trạng thái khác!");
        }
        if ("Đã hủy".equalsIgnoreCase(trangThaiCu)) {
            throw new IllegalArgumentException("Lịch hẹn đã bị hủy, không thể kích hoạt lại trạng thái!");
        }

        // Từ 'Đã đặt' chỉ được sang 'Đã xác nhận' hoặc 'Đã hủy'
        if ("Đã đặt".equalsIgnoreCase(trangThaiCu)) {
            if (!"Đã xác nhận".equalsIgnoreCase(trangThaiMoi) && !"Đã hủy".equalsIgnoreCase(trangThaiMoi)) {
                throw new IllegalArgumentException("Lịch hẹn mới đặt chỉ có thể chuyển sang 'Đã xác nhận' hoặc 'Đã hủy'!");
            }
        }

        // Từ 'Đã xác nhận' có thể sang 'Đang thực hiện', 'Hoàn thành' hoặc 'Đã hủy'
        if ("Đã xác nhận".equalsIgnoreCase(trangThaiCu)) {
            if (!"Đang thực hiện".equalsIgnoreCase(trangThaiMoi)
                    && !"Hoàn thành".equalsIgnoreCase(trangThaiMoi)
                    && !"Đã hủy".equalsIgnoreCase(trangThaiMoi)) {
                throw new IllegalArgumentException("Lịch hẹn đã xác nhận không thể quay lại trạng thái 'Đã đặt'!");
            }
        }
    }
}

