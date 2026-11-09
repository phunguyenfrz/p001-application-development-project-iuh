package com.petshop.bus;

import com.petshop.dao.DichVuChamSocDAO;
import com.petshop.dao.KhachHangDAO;
import com.petshop.dao.LichHenDAO;
import com.petshop.dao.ThuCungDAO;
import com.petshop.entity.ChiTietLichHen;
import com.petshop.entity.DichVuChamSoc;
import com.petshop.entity.KhachHang;
import com.petshop.entity.LichHen;
import com.petshop.entity.TaiKhoan;
import com.petshop.entity.ThuCung;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Lớp nghiệp vụ (BUS Layer) xử lý nghiệp vụ, kiểm tra ràng buộc và phân quyền cho lịch hẹn.
 * Tuân thủ chuẩn kiến trúc 3 tầng IUH: không import javafx.*, ném IllegalArgumentException khi có lỗi nghiệp vụ.
 */
public class LichHenBUS {

    private final LichHenDAO lichHenDAO;
    private final XacThucBUS xacThucService;
    private final KhachHangBUS khachHangBUS;
    private final ThuCungBUS thuCungBUS;
    private final DichVuChamSocBUS dichVuChamSocBUS;

    public LichHenBUS() {
        this.lichHenDAO = new LichHenDAO();
        this.xacThucService = new XacThucBUS();
        this.khachHangBUS = new KhachHangBUS();
        this.thuCungBUS = new ThuCungBUS();
        this.dichVuChamSocBUS = new DichVuChamSocBUS();
    }

    public LichHenBUS(LichHenDAO lichHenDAO, XacThucBUS xacThucService) {
        this.lichHenDAO = lichHenDAO;
        this.xacThucService = xacThucService;
        this.khachHangBUS = new KhachHangBUS();
        this.thuCungBUS = new ThuCungBUS();
        this.dichVuChamSocBUS = new DichVuChamSocBUS();
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

    // Cập nhật trạng thái duyệt lịch: Đã đặt -> Đã xác nhận -> Đã tiếp nhận / Hoàn thành
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

    // Kiểm tra quyền xem danh sách và chi tiết lịch hẹn
    private void kiemTraQuyenXemLich() {
        TaiKhoan tk = xacThucService.getTaiKhoanHienTai();
        if (tk == null) {
            throw new SecurityException("Chưa đăng nhập hệ thống!");
        }
        if (tk.getPhanQuyen() == TaiKhoan.VaiTro.ADMIN) {
            return;
        }
        if (DanhMucMenu.kiemTraQuyen(tk, "LH_TIM_KIEM")
                || DanhMucMenu.kiemTraQuyen(tk, "LH_CAP_NHAT")
                || DanhMucMenu.kiemTraQuyen(tk, "LH_TAO")) {
            return;
        }
        throw new SecurityException("Tài khoản của bạn không có quyền xem thông tin lịch hẹn!");
    }

    // Lấy toàn bộ danh sách lịch hẹn
    public List<LichHen> layDanhSachLichHen() {
        kiemTraQuyenXemLich();
        return lichHenDAO.layDanhSachLichHen();
    }

    // Tìm một lịch hẹn theo mã duy nhất
    public LichHen layLichHenTheoMa(String maLichHen) {
        kiemTraQuyenXemLich();
        if (maLichHen == null || maLichHen.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã lịch hẹn không được để trống!");
        }
        return lichHenDAO.layLichHenTheoMa(maLichHen.trim());
    }

    // Lấy danh sách dịch vụ chi tiết của lịch hẹn
    public List<ChiTietLichHen> layChiTietLichHen(String maLichHen) {
        kiemTraQuyenXemLich();
        if (maLichHen == null || maLichHen.trim().isEmpty()) {
            return List.of();
        }
        return lichHenDAO.layChiTietLichHen(maLichHen.trim());
    }

    // Tìm kiếm và lọc lịch hẹn theo khách hàng, ngày và trạng thái
    public List<LichHen> timKiemLichHen(String tuKhoaKhach, LocalDate tuNgay, LocalDate denNgay, String trangThai) {
        return timKiemLichHen(tuKhoaKhach, tuNgay, denNgay, trangThai, null);
    }

    // Tìm kiếm và lọc lịch hẹn theo khách hàng, ngày, trạng thái và nhân viên phụ trách
    public List<LichHen> timKiemLichHen(String tuKhoaKhach, LocalDate tuNgay, LocalDate denNgay, String trangThai, String maNhanVien) {
        kiemTraQuyenXemLich();
        if (tuNgay != null && denNgay != null && denNgay.isBefore(tuNgay)) {
            throw new IllegalArgumentException("Đến ngày phải lớn hơn hoặc bằng từ ngày!");
        }
        return lichHenDAO.timKiemLichHen(tuKhoaKhach, tuNgay, denNgay, trangThai, maNhanVien);
    }

    // Sinh mã lịch hẹn mới tiếp theo
    public String taoMaLichHenMoi() {
        return lichHenDAO.taoMaLichHen();
    }

    // Lấy danh sách nhân viên chăm sóc đang làm việc
    public List<String[]> layDanhSachNhanVienChamSoc() {
        return lichHenDAO.layDanhSachNhanVienChamSoc();
    }

    // Tra cứu khách hàng theo số điện thoại (10 chữ số, bắt đầu bằng 0)
    public KhachHang timKhachHangTheoSoDienThoai(String soDienThoai) {
        if (soDienThoai == null || soDienThoai.trim().isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống!");
        }
        String sdt = soDienThoai.trim();
        if (!sdt.matches("^0\\d{9}$")) {
            throw new IllegalArgumentException("Số điện thoại không đúng định dạng (phải có 10 chữ số, bắt đầu bằng số 0)!");
        }
        return khachHangBUS.layKhachHangTheoSoDienThoai(sdt);
    }

    // Lấy danh sách thú cưng của một khách hàng
    public List<ThuCung> layDanhSachThuCungTheoKhachHang(String maKhachHang) {
        if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
            return List.of();
        }
        return thuCungBUS.layDanhSachTheoKhachHang(maKhachHang.trim());
    }

    // Lấy danh sách dịch vụ chăm sóc đang kinh doanh
    public List<DichVuChamSoc> layDanhSachDichVu() {
        return dichVuChamSocBUS.layDanhSachDichVuDangKinhDoanh();
    }

    // Tra đơn giá dịch vụ theo cân nặng
    public double traGiaDichVuTheoCanNang(String maDichVu, double canNang) {
        return dichVuChamSocBUS.traGiaTheoCanNang(maDichVu, canNang);
    }

    // Thêm khách hàng nhanh
    public boolean themKhachHang(KhachHang kh) {
        return khachHangBUS.themKhachHang(kh);
    }

    // Sinh mã khách hàng mới
    public String taoMaKhachHang() {
        return khachHangBUS.taoMaKhachHang();
    }

    // Kiểm tra trùng số điện thoại khách hàng
    public boolean kiemTraTrungSoDienThoai(String soDienThoai) {
        return khachHangBUS.kiemTraTrungSoDienThoai(soDienThoai, null);
    }

    // Thêm thú cưng nhanh
    public boolean themThuCung(ThuCung tc) {
        return thuCungBUS.themThuCung(tc);
    }

    // Sinh mã thú cưng mới
    public String taoMaThuCungMoi() {
        return thuCungBUS.taoMaThuCung();
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

        // 3. Tự động gán nhân viên đang đăng nhập nếu chưa có (lịch hẹn không yêu cầu chọn nhân viên chăm sóc trước)
        if (lichHen.getMaNhanVien() == null || lichHen.getMaNhanVien().trim().isEmpty()) {
            TaiKhoan tk = xacThucService.getTaiKhoanHienTai();
            if (tk != null && tk.getMaNhanVien() != null && !tk.getMaNhanVien().trim().isEmpty()) {
                lichHen.setMaNhanVien(tk.getMaNhanVien().trim());
            } else {
                List<String[]> dsNv = lichHenDAO.layDanhSachNhanVienChamSoc();
                if (!dsNv.isEmpty()) {
                    lichHen.setMaNhanVien(dsNv.get(0)[0]);
                }
            }
        }

        // 4. Kiểm tra ngày hẹn và giờ hẹn
        if (lichHen.getNgayHen() == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày hẹn dịch vụ!");
        }
        if (lichHen.getGioHen() == null) {
            throw new IllegalArgumentException("Vui lòng chọn giờ hẹn dịch vụ!");
        }

        // Chặn chọn ngày/giờ hẹn trong quá khứ
        LocalDate homNay = LocalDate.now();
        if (lichHen.getNgayHen().isBefore(homNay)) {
            throw new IllegalArgumentException("Ngày hẹn không được ở trong quá khứ!");
        }
        if (lichHen.getNgayHen().isEqual(homNay) && lichHen.getGioHen().isBefore(LocalTime.now())) {
            throw new IllegalArgumentException("Giờ hẹn không được ở trong quá khứ!");
        }

        // 5. Kiểm tra danh sách dịch vụ chi tiết
        if (danhSachChiTiet != null) {
            if (danhSachChiTiet.isEmpty()) {
                throw new IllegalArgumentException("Lịch hẹn phải có ít nhất một dịch vụ chăm sóc!");
            }
            for (ChiTietLichHen ct : danhSachChiTiet) {
                if (ct.getMaDichVu() == null || ct.getMaDichVu().trim().isEmpty()) {
                    throw new IllegalArgumentException("Mã dịch vụ trong chi tiết không hợp lệ!");
                }
                if (ct.getCanNangThuCung() < 0) {
                    ct.setCanNangThuCung(0.0);
                }
                if (ct.getDonGia() < 0) {
                    ct.setDonGia(0.0);
                }
            }
        }

        // 6. Kiểm tra trùng lịch hẹn tại khung giờ đó
        String maLichBoQua = laCapNhat ? lichHen.getMaLichHen() : null;
        boolean trungLich = lichHenDAO.kiemTraTrungLich(
                lichHen.getNgayHen(),
                lichHen.getGioHen(),
                maLichBoQua
        );
        if (trungLich) {
            throw new IllegalArgumentException("Khung giờ "
                    + lichHen.getGioHen().toString() + " ngày " + lichHen.getNgayHen().toString()
                    + " đã có lịch hẹn khác được đặt! Vui lòng chọn khung giờ khác.");
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

        // Từ 'Đã đặt' có thể sang 'Đã xác nhận', 'Đã tiếp nhận', 'Đang thực hiện', 'Hoàn thành' hoặc 'Đã hủy'
        if ("Đã đặt".equalsIgnoreCase(trangThaiCu)) {
            if (!"Đã xác nhận".equalsIgnoreCase(trangThaiMoi)
                    && !"Đã tiếp nhận".equalsIgnoreCase(trangThaiMoi)
                    && !"Đang thực hiện".equalsIgnoreCase(trangThaiMoi)
                    && !"Hoàn thành".equalsIgnoreCase(trangThaiMoi)
                    && !"Đã hủy".equalsIgnoreCase(trangThaiMoi)) {
                throw new IllegalArgumentException("Lịch hẹn mới đặt chỉ có thể chuyển sang 'Đã xác nhận', 'Hoàn thành' hoặc 'Đã hủy'!");
            }
        }

        // Từ 'Đã xác nhận' có thể sang 'Đã tiếp nhận', 'Đang thực hiện', 'Hoàn thành' hoặc 'Đã hủy'
        if ("Đã xác nhận".equalsIgnoreCase(trangThaiCu)) {
            if (!"Đã tiếp nhận".equalsIgnoreCase(trangThaiMoi)
                    && !"Đang thực hiện".equalsIgnoreCase(trangThaiMoi)
                    && !"Hoàn thành".equalsIgnoreCase(trangThaiMoi)
                    && !"Đã hủy".equalsIgnoreCase(trangThaiMoi)) {
                throw new IllegalArgumentException("Lịch hẹn đã xác nhận không thể quay lại trạng thái 'Đã đặt'!");
            }
        }
    }
}
