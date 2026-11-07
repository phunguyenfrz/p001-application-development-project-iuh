package com.petshop.bus;

import com.petshop.dao.DichVuChamSocDAO;
import com.petshop.dao.KhachHangDAO;
import com.petshop.dao.LichHenDAO;
import com.petshop.dao.PhieuDichVuDAO;
import com.petshop.dao.ThuCungDAO;
import com.petshop.entity.ChiTietLichHen;
import com.petshop.entity.ChiTietPhieuDichVu;
import com.petshop.entity.DichVuChamSoc;
import com.petshop.entity.KhachHang;
import com.petshop.entity.LichHen;
import com.petshop.entity.PhieuDichVu;
import com.petshop.entity.TaiKhoan;
import com.petshop.entity.ThuCung;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Lớp nghiệp vụ (BUS Layer) xử lý nghiệp vụ, kiểm tra ràng buộc và phân quyền cho phiếu dịch vụ.
 * Tuân thủ chuẩn kiến trúc 3 tầng IUH: không import javafx.*, ném IllegalArgumentException khi có lỗi nghiệp vụ.
 */
public class PhieuDichVuBUS {

    private final PhieuDichVuDAO phieuDichVuDAO;
    private final XacThucBUS xacThucService;

    public PhieuDichVuBUS() {
        this.phieuDichVuDAO = new PhieuDichVuDAO();
        this.xacThucService = new XacThucBUS();
    }

    public PhieuDichVuBUS(PhieuDichVuDAO phieuDichVuDAO, XacThucBUS xacThucService) {
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

        if ("Đang thực hiện".equalsIgnoreCase(phieu.getTrangThai())) {
            kiemTraRangBuocBatDauThucHien(phieu);
        }

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
        // Chặn nhân viên hoặc thú cưng làm 2 phiếu cùng lúc
        if ("Đang thực hiện".equalsIgnoreCase(trangThaiMoi.trim())) {
            kiemTraRangBuocBatDauThucHien(phieuHienTai);
        }

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

    // Quét và tự động hủy tất cả các lịch hẹn đã trễ quá 2 giờ
    public int quetVaHuyLichHenTreQua2Gio() {
        kiemTraQuyenXemPhieu();
        return phieuDichVuDAO.quetVaHuyLichHenTreQua2Gio();
    }

    // Tự động hủy lịch hẹn kèm ghi chú lý do
    public boolean huyLichHenTuDong(String maLichHen, String lyDoHuy) {
        if (maLichHen == null || maLichHen.isBlank()) return false;
        return new LichHenDAO().huyLichHenTuDong(maLichHen.trim(), lyDoHuy);
    }

    // Lấy danh sách các lịch hẹn đang chờ tiếp nhận hôm nay của một khách hàng cụ thể
    public List<LichHen> layDanhSachLichHenChoTheoKhachHang(String maKhachHang) {
        if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
            return List.of();
        }
        kiemTraQuyenXemPhieu();
        return phieuDichVuDAO.layDanhSachLichHenChoTheoKhachHang(maKhachHang.trim());
    }

    // Tự sinh mã phiếu dịch vụ mới tiếp theo
    public String taoMaPhieuDichVuMoi() {
        return phieuDichVuDAO.taoMaPhieuDichVu();
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
        return new KhachHangDAO().layKhachHangTheoSoDienThoai(sdt);
    }

    // Kiểm tra xem phiếu dịch vụ đã có hóa đơn thanh toán hay chưa (chặn trùng 2 hóa đơn)
    public boolean kiemTraPhieuDichVuDaCoHoaDon(String maPhieuDichVu) {
        return phieuDichVuDAO.kiemTraPhieuDichVuDaCoHoaDon(maPhieuDichVu);
    }

    // Tìm kiếm phiếu dịch vụ chờ thanh toán theo từ khóa
    public List<PhieuDichVu> timKiemPhieuDichVuChoThanhToan(String tuKhoa) {
        return phieuDichVuDAO.timKiemPhieuDichVuChoThanhToan(tuKhoa);
    }

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
        if (phieuDichVuDAO.kiemTraPhieuDichVuDaCoHoaDon(phieu.getMaPhieuDichVu())) {
            throw new IllegalArgumentException("Phiếu dịch vụ này đã được thanh toán trong một hóa đơn khác!");
        }
        return phieu;
    }

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
            if (ct.getDonGia() < 0) {
                throw new IllegalArgumentException("Đơn giá dịch vụ không được nhỏ hơn 0!");
            }

            // Đảm bảo thú cưng trong chi tiết thuộc quyền sở hữu của khách hàng trong phiếu
            if (!phieuDichVuDAO.kiemTraThuCungThuocKhachHang(ct.getMaThuCung(), phieu.getMaKhachHang())) {
                String tenTc = ct.getTenThuCung() != null ? ct.getTenThuCung() : ct.getMaThuCung();
                throw new IllegalArgumentException("Thú cưng '" + tenTc + "' không thuộc sở hữu của khách hàng trong phiếu tiếp nhận!");
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

    // Kiểm tra ràng buộc khi bắt đầu thực hiện: nhân viên và thú cưng không được trùng dịch vụ đang chạy
    private void kiemTraRangBuocBatDauThucHien(PhieuDichVu phieu) {
        if (phieu == null) return;

        // 1. Kiểm tra nhân viên phụ trách có đang bận phiếu dịch vụ khác không
        String maPhieuBan = phieuDichVuDAO.layMaPhieuDangThucHienCuaNhanVien(phieu.getMaNhanVien(), phieu.getMaPhieuDichVu());
        if (maPhieuBan != null) {
            String tenNv = phieu.getTenNhanVien() != null ? phieu.getTenNhanVien() : phieu.getMaNhanVien();
            throw new IllegalStateException("Nhân viên " + tenNv + " hiện đang thực hiện phiếu dịch vụ " + maPhieuBan + ". Vui lòng hoàn thành phiếu hiện tại trước khi bắt đầu phiếu mới!");
        }

        // 2. Kiểm tra thú cưng trong phiếu có đang được chăm sóc ở phiếu khác không
        String thongTinTc = phieuDichVuDAO.layThongTinThuCungDangThucHien(phieu.getMaPhieuDichVu(), phieu.getMaPhieuDichVu());
        if (thongTinTc != null) {
            throw new IllegalStateException(thongTinTc + ". Vui lòng hoàn thành phiếu cũ trước khi bắt đầu phiếu này!");
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

    public List<KhachHang> layDanhSachKhachHang() {
        return new KhachHangDAO().layDanhSachKhachHang();
    }

    public List<ThuCung> layDanhSachThuCungTheoKhachHang(String maKhachHang) {
        if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
            return List.of();
        }
        return new ThuCungDAO().layDanhSachTheoKhachHang(maKhachHang.trim());
    }

    public List<DichVuChamSoc> layDanhSachDichVu() {
        return new DichVuChamSocDAO().layDanhSachDichVuDangKinhDoanh();
    }

    public double traGiaDichVuTheoCanNang(String maDichVu, double canNang) {
        return new DichVuChamSocDAO().traGiaTheoCanNang(maDichVu, canNang);
    }

    public List<ChiTietLichHen> layChiTietLichHen(String maLichHen) {
        return new LichHenDAO().layChiTietLichHen(maLichHen);
    }

    public List<String[]> layDanhSachNhanVien() {
        return new LichHenDAO().layDanhSachNhanVienChamSoc();
    }

    public boolean capNhatCanNangThuCung(String maThuCung, double canNang) {
        return new ThuCungDAO().capNhatCanNang(maThuCung, canNang);
    }

    public boolean themKhachHang(KhachHang kh) {
        return new KhachHangDAO().themKhachHang(kh);
    }

    public String taoMaKhachHang() {
        return new KhachHangDAO().taoMaKhachHang();
    }

    public boolean kiemTraTrungSoDienThoai(String soDienThoai) {
        return new KhachHangDAO().kiemTraTrungSoDienThoai(soDienThoai, null);
    }

    public boolean themThuCung(ThuCung tc) {
        return new ThuCungDAO().themThuCung(tc);
    }

    public String taoMaThuCungMoi() {
        return new ThuCungDAO().taoMaThuCung();
    }
}
