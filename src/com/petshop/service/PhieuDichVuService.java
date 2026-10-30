package com.petshop.service;

import com.petshop.dao.PhieuDichVuDAO;
import com.petshop.model.ChiTietPhieuDichVu;
import com.petshop.model.LichHen;
import com.petshop.model.PhieuDichVu;
import com.petshop.model.TaiKhoan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Lớp dịch vụ (Service Layer) xử lý nghiệp vụ, kiểm tra ràng buộc và phân quyền cho phiếu dịch vụ.
 */
public class PhieuDichVuService {

    private final PhieuDichVuDAO phieuDichVuDAO;
    private final XacThucService xacThucService;

    public PhieuDichVuService() {
        this.phieuDichVuDAO = new PhieuDichVuDAO();
        this.xacThucService = new XacThucService();
    }

    public PhieuDichVuService(PhieuDichVuDAO phieuDichVuDAO, XacThucService xacThucService) {
        this.phieuDichVuDAO = phieuDichVuDAO;
        this.xacThucService = xacThucService;
    }

    // Tạo phiếu tiếp nhận dịch vụ mới kèm các dòng chi tiết (vai trò CHAM_SOC, ADMIN)
    public boolean taoPhieuDichVu(PhieuDichVu phieu, List<ChiTietPhieuDichVu> danhSachChiTiet) {
        xacThucService.kiemTraQuyen("PDV_TAO");
        kiemTraDuLieuPhieu(phieu, danhSachChiTiet);

        if (phieu.getMaPhieuDichVu() == null || phieu.getMaPhieuDichVu().trim().isEmpty()) {
            phieu.setMaPhieuDichVu(phieuDichVuDAO.taoMaPhieuDichVu());
        }

        if (phieu.getTrangThai() == null || phieu.getTrangThai().trim().isEmpty()) {
            phieu.setTrangThai("Đang chờ");
        }

        return phieuDichVuDAO.themPhieuDichVu(phieu, danhSachChiTiet);
    }

    // Cập nhật thông tin phiếu dịch vụ và danh sách dịch vụ chi tiết
    public boolean capNhatPhieuDichVu(PhieuDichVu phieu, List<ChiTietPhieuDichVu> danhSachChiTiet) {
        xacThucService.kiemTraQuyen("PDV_CAP_NHAT");

        if (phieu == null || phieu.getMaPhieuDichVu() == null || phieu.getMaPhieuDichVu().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã phiếu dịch vụ cần cập nhật không được để trống!");
        }

        PhieuDichVu phieuCu = phieuDichVuDAO.layPhieuDichVuTheoMa(phieu.getMaPhieuDichVu().trim());
        if (phieuCu == null) {
            throw new IllegalArgumentException("Không tìm thấy phiếu dịch vụ mã: " + phieu.getMaPhieuDichVu());
        }

        if ("Hoàn thành".equalsIgnoreCase(phieuCu.getTrangThai())) {
            throw new IllegalArgumentException("Phiếu dịch vụ đã hoàn thành, không thể chỉnh sửa chi tiết!");
        }
        if ("Đã hủy".equalsIgnoreCase(phieuCu.getTrangThai())) {
            throw new IllegalArgumentException("Phiếu dịch vụ đã bị hủy, không thể chỉnh sửa thông tin!");
        }

        kiemTraDuLieuPhieu(phieu, danhSachChiTiet);
        kiemTraChuyenDoiTrangThai(phieuCu.getTrangThai(), phieu.getTrangThai());

        return phieuDichVuDAO.capNhatPhieuDichVu(phieu, danhSachChiTiet);
    }

    // Cập nhật trạng thái tiến độ phiếu dịch vụ: Đang chờ -> Đang thực hiện -> Hoàn thành
    public boolean capNhatTrangThai(String maPhieuDichVu, String trangThaiMoi) {
        xacThucService.kiemTraQuyen("PDV_CAP_NHAT");

        if (maPhieuDichVu == null || maPhieuDichVu.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã phiếu dịch vụ không được để trống!");
        }
        if (trangThaiMoi == null || trangThaiMoi.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn trạng thái mới!");
        }

        PhieuDichVu phieuHienTai = phieuDichVuDAO.layPhieuDichVuTheoMa(maPhieuDichVu.trim());
        if (phieuHienTai == null) {
            throw new IllegalArgumentException("Không tìm thấy phiếu dịch vụ mã: " + maPhieuDichVu);
        }

        kiemTraChuyenDoiTrangThai(phieuHienTai.getTrangThai(), trangThaiMoi.trim());
        return phieuDichVuDAO.capNhatTrangThai(maPhieuDichVu.trim(), trangThaiMoi.trim());
    }

    // Xóa mềm phiếu dịch vụ (chuyển sang 'Đã hủy' và lưu lý do)
    public boolean huyPhieuDichVu(String maPhieuDichVu, String lyDoHuy) {
        xacThucService.kiemTraQuyen("PDV_CAP_NHAT");

        if (maPhieuDichVu == null || maPhieuDichVu.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã phiếu dịch vụ cần hủy không được để trống!");
        }

        PhieuDichVu phieuHienTai = phieuDichVuDAO.layPhieuDichVuTheoMa(maPhieuDichVu.trim());
        if (phieuHienTai == null) {
            throw new IllegalArgumentException("Không tìm thấy phiếu dịch vụ mã: " + maPhieuDichVu);
        }

        if ("Hoàn thành".equalsIgnoreCase(phieuHienTai.getTrangThai())) {
            throw new IllegalArgumentException("Phiếu dịch vụ đã hoàn thành, không thể hủy!");
        }
        if ("Đã hủy".equalsIgnoreCase(phieuHienTai.getTrangThai())) {
            throw new IllegalArgumentException("Phiếu dịch vụ này đã bị hủy trước đó!");
        }

        return phieuDichVuDAO.xoaMem(maPhieuDichVu.trim(), lyDoHuy);
    }

    // Kiểm tra quyền xem danh sách và chi tiết phiếu dịch vụ
    private void kiemTraQuyenXemPhieu() {
        TaiKhoan tk = xacThucService.getTaiKhoanHienTai();
        if (tk == null) {
            throw new SecurityException("Chưa đăng nhập hệ thống!");
        }
        if (tk.getPhanQuyen() == TaiKhoan.VaiTro.ADMIN) {
            return;
        }
        if (DanhMucMenu.kiemTraQuyen(tk.getPhanQuyen(), "PDV_TIM_KIEM")
                || DanhMucMenu.kiemTraQuyen(tk.getPhanQuyen(), "PDV_CAP_NHAT")
                || DanhMucMenu.kiemTraQuyen(tk.getPhanQuyen(), "PDV_TAO")) {
            return;
        }
        throw new SecurityException("Tài khoản của bạn không có quyền xem thông tin phiếu dịch vụ!");
    }

    // Lấy toàn bộ danh sách phiếu dịch vụ
    public List<PhieuDichVu> layDanhSachPhieuDichVu() {
        kiemTraQuyenXemPhieu();
        return phieuDichVuDAO.layDanhSachPhieuDichVu();
    }

    // Tìm một phiếu dịch vụ theo mã duy nhất
    public PhieuDichVu layPhieuDichVuTheoMa(String maPhieuDichVu) {
        kiemTraQuyenXemPhieu();
        if (maPhieuDichVu == null || maPhieuDichVu.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã phiếu dịch vụ không được để trống!");
        }
        return phieuDichVuDAO.layPhieuDichVuTheoMa(maPhieuDichVu.trim());
    }

    // Lấy danh sách dịch vụ chi tiết của phiếu
    public List<ChiTietPhieuDichVu> layChiTietPhieuDichVu(String maPhieuDichVu) {
        kiemTraQuyenXemPhieu();
        if (maPhieuDichVu == null || maPhieuDichVu.trim().isEmpty()) {
            return List.of();
        }
        return phieuDichVuDAO.layChiTietPhieuDichVu(maPhieuDichVu.trim());
    }

    // Tìm kiếm và lọc phiếu dịch vụ theo khách hàng/mã phiếu, khoảng ngày và trạng thái
    public List<PhieuDichVu> timKiemPhieuDichVu(String tuKhoa, LocalDate tuNgay, LocalDate denNgay, String trangThai) {
        kiemTraQuyenXemPhieu();
        if (tuNgay != null && denNgay != null && denNgay.isBefore(tuNgay)) {
            throw new IllegalArgumentException("Đến ngày phải lớn hơn hoặc bằng từ ngày!");
        }
        return phieuDichVuDAO.timKiemPhieuDichVu(tuKhoa, tuNgay, denNgay, trangThai);
    }

    // Lấy danh sách các lịch hẹn đang chờ tiếp nhận để lập phiếu
    public List<LichHen> layDanhSachLichHenChoTiepNhan() {
        kiemTraQuyenXemPhieu();
        return phieuDichVuDAO.layDanhSachLichHenChoTiepNhan();
    }

    // Tự sinh mã phiếu dịch vụ mới tiếp theo
    public String taoMaPhieuDichVuMoi() {
        return phieuDichVuDAO.taoMaPhieuDichVu();
    }

    // =========================================================================
    // HÀM CUNG CẤP CHO PHÂN HỆ HÓA ĐƠN (#71 - Hóa đơn)
    // =========================================================================

    /**
     * Lấy phiếu dịch vụ để nạp vào hóa đơn thanh toán tại quầy.
     * @param maPhieuDichVu Mã phiếu dịch vụ cần thanh toán
     * @return Phiếu dịch vụ kèm chi tiết các dòng dịch vụ
     */
    public PhieuDichVu layPhieuDichVuDeThanhToan(String maPhieuDichVu) {
        if (maPhieuDichVu == null || maPhieuDichVu.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã phiếu dịch vụ không được để trống!");
        }
        PhieuDichVu phieu = phieuDichVuDAO.layPhieuDichVuTheoMa(maPhieuDichVu.trim());
        if (phieu == null) {
            throw new IllegalArgumentException("Không tìm thấy phiếu dịch vụ mã: " + maPhieuDichVu);
        }
        if ("Đã hủy".equalsIgnoreCase(phieu.getTrangThai())) {
            throw new IllegalArgumentException("Phiếu dịch vụ này đã bị hủy, không thể lập hóa đơn thanh toán!");
        }
        return phieu;
    }

    // =========================================================================
    // HÀM BỔ TRỢ KIỂM TRA RÀNG BUỘC (VALIDATION)
    // =========================================================================

    // Kiểm tra ràng buộc hợp lệ của thông tin phiếu và danh sách chi tiết
    private void kiemTraDuLieuPhieu(PhieuDichVu phieu, List<ChiTietPhieuDichVu> danhSachChiTiet) {
        if (phieu == null) {
            throw new IllegalArgumentException("Thông tin phiếu dịch vụ không được để trống!");
        }

        // 1. Kiểm tra khách hàng
        if (phieu.getMaKhachHang() == null || phieu.getMaKhachHang().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn khách hàng tiếp nhận!");
        }

        // 2. Kiểm tra nhân viên tiếp nhận/phụ trách
        if (phieu.getMaNhanVien() == null || phieu.getMaNhanVien().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn nhân viên tiếp nhận dịch vụ!");
        }

        // 3. Kiểm tra ngày giờ tiếp nhận
        if (phieu.getNgayTiepNhan() == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày tiếp nhận dịch vụ!");
        }
        if (phieu.getGioTiepNhan() == null) {
            throw new IllegalArgumentException("Vui lòng chọn giờ tiếp nhận dịch vụ!");
        }

        // 4. Kiểm tra danh sách dịch vụ chi tiết
        if (danhSachChiTiet == null || danhSachChiTiet.isEmpty()) {
            throw new IllegalArgumentException("Phiếu tiếp nhận phải có ít nhất một dịch vụ chăm sóc!");
        }

        Set<String> cacDichVuDaThem = new HashSet<>();
        for (ChiTietPhieuDichVu ct : danhSachChiTiet) {
            if (ct.getMaThuCung() == null || ct.getMaThuCung().trim().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng chọn thú cưng cho từng dòng dịch vụ!");
            }
            if (ct.getMaDichVu() == null || ct.getMaDichVu().trim().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng chọn dịch vụ chăm sóc trong chi tiết phiếu!");
            }
            if (ct.getDonGia() == null || ct.getDonGia().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Đơn giá dịch vụ không được nhỏ hơn 0!");
            }

            // Chặn thêm trùng cùng một thú cưng với cùng một dịch vụ trong phiếu
            String khoaChung = ct.getMaThuCung().trim() + "_" + ct.getMaDichVu().trim();
            if (!cacDichVuDaThem.add(khoaChung)) {
                String tenTc = ct.getTenThuCung() != null ? ct.getTenThuCung() : ct.getMaThuCung();
                String tenDv = ct.getTenDichVu() != null ? ct.getTenDichVu() : ct.getMaDichVu();
                throw new IllegalArgumentException("Dịch vụ '" + tenDv + "' đã được áp dụng cho thú cưng '" + tenTc + "' trong phiếu này!");
            }
        }
    }

    // Kiểm tra tính hợp lệ khi chuyển đổi trạng thái tiến độ phiếu
    private void kiemTraChuyenDoiTrangThai(String trangThaiCu, String trangThaiMoi) {
        if (trangThaiCu == null || trangThaiMoi == null) return;
        if (trangThaiCu.equalsIgnoreCase(trangThaiMoi)) return;

        if ("Hoàn thành".equalsIgnoreCase(trangThaiCu)) {
            throw new IllegalArgumentException("Phiếu dịch vụ đã hoàn thành, không thể chuyển sang trạng thái khác!");
        }
        if ("Đã hủy".equalsIgnoreCase(trangThaiCu)) {
            throw new IllegalArgumentException("Phiếu dịch vụ đã bị hủy, không thể kích hoạt lại!");
        }

        // 'Đang chờ' chỉ được chuyển sang 'Đang thực hiện' hoặc 'Đã hủy'
        if ("Đang chờ".equalsIgnoreCase(trangThaiCu)) {
            if (!"Đang thực hiện".equalsIgnoreCase(trangThaiMoi) && !"Đã hủy".equalsIgnoreCase(trangThaiMoi)) {
                throw new IllegalArgumentException("Phiếu đang chờ chỉ có thể chuyển sang 'Đang thực hiện' hoặc 'Đã hủy'!");
            }
        }

        // 'Đang thực hiện' chỉ được chuyển sang 'Hoàn thành' hoặc 'Đã hủy'
        if ("Đang thực hiện".equalsIgnoreCase(trangThaiCu)) {
            if (!"Hoàn thành".equalsIgnoreCase(trangThaiMoi) && !"Đã hủy".equalsIgnoreCase(trangThaiMoi)) {
                throw new IllegalArgumentException("Phiếu đang thực hiện chỉ có thể chuyển sang 'Hoàn thành' hoặc 'Đã hủy'!");
            }
        }
    }
}

