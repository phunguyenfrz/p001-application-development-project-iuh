package com.petshop.service;

import com.petshop.dao.CaDAO;
import com.petshop.dao.NhanVienDAO;
import com.petshop.dao.PhanCaDAO;
import com.petshop.model.Ca;
import com.petshop.model.NhanVien;
import com.petshop.model.PhanCa;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Lớp nghiệp vụ quản lý Ca trực và Phân công ca trực cho nhân viên.
 * Đảm bảo phân quyền (QUAN_LY, ADMIN) và các ràng buộc nghiệp vụ.
 */
public class CaTrucService {

    private final CaDAO caDAO;
    private final PhanCaDAO phanCaDAO;
    private final NhanVienDAO nhanVienDAO;
    private final XacThucService xacThucService;

    public CaTrucService() {
        this.caDAO = new CaDAO();
        this.phanCaDAO = new PhanCaDAO();
        this.nhanVienDAO = new NhanVienDAO();
        this.xacThucService = new XacThucService();
    }

    // ==========================================
    // NGHIỆP VỤ CA TRỰC (SHF_CRUD)
    // ==========================================

    public String taoMaCa() {
        return caDAO.taoMaCa();
    }

    /**
     * Thêm ca trực mới. Yêu cầu quyền CT_CAP_NHAT.
     */
    public boolean themCa(Ca ca) {
        xacThucService.kiemTraQuyen("CT_CAP_NHAT");
        kiemTraDuLieuCa(ca);

        if (ca.getMaCa() == null || ca.getMaCa().isBlank()) {
            ca.setMaCa(caDAO.taoMaCa());
        }

        // Kiểm tra không trùng tên ca đang áp dụng
        List<Ca> dsHienCo = caDAO.layDanhSachCaDangApDung();
        for (Ca c : dsHienCo) {
            if (c.getTenCa().trim().equalsIgnoreCase(ca.getTenCa().trim())) {
                throw new IllegalArgumentException("Đã tồn tại ca trực đang áp dụng mang tên '" + ca.getTenCa() + "'!");
            }
        }

        return caDAO.themCa(ca);
    }

    public boolean themCaLamViec(Ca ca) {
        return themCa(ca);
    }

    /**
     * Cập nhật thông tin ca trực. Yêu cầu quyền CT_CAP_NHAT.
     */
    public boolean capNhatCa(Ca ca) {
        xacThucService.kiemTraQuyen("CT_CAP_NHAT");
        if (ca == null || ca.getMaCa() == null || ca.getMaCa().isBlank()) {
            throw new IllegalArgumentException("Mã ca trực cần cập nhật không được để trống!");
        }
        kiemTraDuLieuCa(ca);

        return caDAO.capNhatCa(ca);
    }

    public boolean suaCaLamViec(Ca ca) {
        return capNhatCa(ca);
    }

    /**
     * Xóa mềm ca trực (chuyển sang Ngừng áp dụng). Yêu cầu quyền CT_CAP_NHAT.
     */
    public boolean xoaMemCa(String maCa) {
        xacThucService.kiemTraQuyen("CT_CAP_NHAT");
        if (maCa == null || maCa.isBlank()) {
            throw new IllegalArgumentException("Mã ca trực cần ngừng áp dụng không được để trống!");
        }
        return caDAO.xoaMemCa(maCa.trim());
    }

    public boolean xoaCaLamViec(String maCa) {
        return xoaMemCa(maCa);
    }

    public List<Ca> layDanhSachCa() {
        return caDAO.layDanhSachCa();
    }

    public List<Ca> layDanhSachCaDangApDung() {
        return caDAO.layDanhSachCaDangApDung();
    }

    public Ca layCaTheoMa(String maCa) {
        if (maCa == null || maCa.isBlank()) return null;
        return caDAO.layTheoMa(maCa.trim());
    }

    private void kiemTraDuLieuCa(Ca ca) {
        if (ca == null) {
            throw new IllegalArgumentException("Thông tin ca trực không được để trống!");
        }
        if (ca.getTenCa() == null || ca.getTenCa().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên ca trực không được để trống!");
        }
        if (ca.getGioBatDau() == null) {
            throw new IllegalArgumentException("Giờ bắt đầu ca trực không được để trống!");
        }
        if (ca.getGioKetThuc() == null) {
            throw new IllegalArgumentException("Giờ kết thúc ca trực không được để trống!");
        }
        if (!ca.getGioKetThuc().isAfter(ca.getGioBatDau())) {
            throw new IllegalArgumentException("Giờ kết thúc (" + ca.getGioKetThucDinhDang() 
                    + ") phải sau giờ bắt đầu (" + ca.getGioBatDauDinhDang() + ")!");
        }
    }

    // ==========================================
    // NGHIỆP VỤ PHÂN CA (SHF_ASSIGN, SHF_VIEW)
    // ==========================================

    public String taoMaPhanCa() {
        return phanCaDAO.taoMaPhanCa();
    }

    /**
     * Phân công ca trực cho nhân viên. Yêu cầu quyền CT_PHAN_CONG.
     * Quy tắc nghiệp vụ:
     * - Không phân công một nhân viên 2 ca trùng khung giờ trong cùng ngày.
     * - Không phân ca vào ngày quá khứ (trước ngày hiện tại).
     */
    public boolean phanCa(PhanCa phanCa) {
        xacThucService.kiemTraQuyen("CT_PHAN_CONG");

        if (phanCa == null) {
            throw new IllegalArgumentException("Thông tin phân ca không được để trống!");
        }
        if (phanCa.getMaNhanVien() == null || phanCa.getMaNhanVien().isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn nhân viên được phân ca!");
        }
        if (phanCa.getMaCa() == null || phanCa.getMaCa().isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn ca trực phân công!");
        }
        if (phanCa.getNgayLam() == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày làm việc!");
        }

        // Kiểm tra không cho phân ca vào ngày quá khứ
        if (phanCa.getNgayLam().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Không thể phân ca cho ngày đã qua ("
                    + phanCa.getNgayLamDinhDang() + ")! Chỉ được phân ca từ ngày hôm nay trở đi.");
        }

        // Lấy thông tin chi tiết ca trực mới
        Ca caMoi = caDAO.layTheoMa(phanCa.getMaCa());
        if (caMoi == null) {
            throw new IllegalArgumentException("Ca trực được chọn không tồn tại hoặc đã bị xóa!");
        }
        if (!caMoi.isDangApDung()) {
            throw new IllegalArgumentException("Ca trực '" + caMoi.getTenCa() + "' đã ngừng áp dụng, không thể phân công!");
        }

        // Kiểm tra trùng khung giờ làm việc của nhân viên trong ngày
        List<PhanCa> dsCaTrongNgay = phanCaDAO.layPhanCaTheoNhanVienVaNgay(phanCa.getMaNhanVien(), phanCa.getNgayLam());
        for (PhanCa daPhan : dsCaTrongNgay) {
            // Khung giờ giao nhau khi: batDau1 < ketThuc2 AND batDau2 < ketThuc1
            if (daPhan.getGioBatDau() != null && daPhan.getGioKetThuc() != null) {
                boolean biTrung = caMoi.getGioBatDau().isBefore(daPhan.getGioKetThuc()) 
                        && daPhan.getGioBatDau().isBefore(caMoi.getGioKetThuc());
                if (biTrung) {
                    throw new IllegalArgumentException("Nhân viên đã được phân công ca '" 
                            + daPhan.getTenCa() + "' (" + daPhan.getKhungGio() 
                            + ") trùng khung giờ làm việc trong ngày " + phanCa.getNgayLamDinhDang() + "!");
                }
            }
        }

        if (phanCa.getMaPhanCa() == null || phanCa.getMaPhanCa().isBlank()) {
            phanCa.setMaPhanCa(phanCaDAO.taoMaPhanCa());
        }

        return phanCaDAO.themPhanCa(phanCa);
    }

    /**
     * Phân ca hàng loạt: phân công nhiều ca cho một nhân viên trong một khoảng ngày,
     * chỉ áp dụng cho các thứ trong tuần được chọn.
     *
     * @param maNhanVien  mã nhân viên được phân ca
     * @param tenNhanVien tên nhân viên (hiển thị)
     * @param dsCa        danh sách các ca trực cần phân (cho phép nhiều ca/ngày, miễn không trùng giờ)
     * @param tuNgay      ngày bắt đầu khoảng phân ca
     * @param denNgay     ngày kết thúc khoảng phân ca
     * @param cacThuTrongTuan tập hợp các thứ trong tuần cần phân ca (MONDAY, TUESDAY, ...)
     * @return danh sách kết quả: mỗi phần tử mô tả thành công / lỗi cho từng (ngày, ca)
     */
    public List<String> phanCaHangLoat(String maNhanVien, String tenNhanVien,
                                        List<Ca> dsCa, LocalDate tuNgay, LocalDate denNgay,
                                        Set<DayOfWeek> cacThuTrongTuan) {
        xacThucService.kiemTraQuyen("CT_PHAN_CONG");

        if (maNhanVien == null || maNhanVien.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn nhân viên được phân ca!");
        }
        if (dsCa == null || dsCa.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một ca trực!");
        }
        if (tuNgay == null || denNgay == null) {
            throw new IllegalArgumentException("Vui lòng chọn khoảng ngày phân ca!");
        }
        if (denNgay.isBefore(tuNgay)) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu!");
        }
        if (tuNgay.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày bắt đầu không thể là ngày đã qua! Chỉ được phân ca từ ngày hôm nay trở đi.");
        }
        if (cacThuTrongTuan == null || cacThuTrongTuan.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một thứ trong tuần!");
        }

        List<String> ketQua = new ArrayList<>();
        int thanhCong = 0;
        int thatBai = 0;

        LocalDate ngayHienTai = tuNgay;
        while (!ngayHienTai.isAfter(denNgay)) {
            if (cacThuTrongTuan.contains(ngayHienTai.getDayOfWeek())) {
                for (Ca ca : dsCa) {
                    try {
                        PhanCa phanCaMoi = new PhanCa(
                                phanCaDAO.taoMaPhanCa(),
                                maNhanVien,
                                ca.getMaCa(),
                                ngayHienTai,
                                PhanCa.TRANG_THAI_DA_PHAN_CONG
                        );
                        phanCaMoi.setTenNhanVien(tenNhanVien);
                        phanCaMoi.setTenCa(ca.getTenCa());
                        phanCaMoi.setGioBatDau(ca.getGioBatDau());
                        phanCaMoi.setGioKetThuc(ca.getGioKetThuc());

                        phanCa(phanCaMoi);
                        thanhCong++;
                    } catch (IllegalArgumentException ex) {
                        thatBai++;
                        ketQua.add("⚠ " + ngayHienTai.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                + " - " + ca.getTenCa() + ": " + ex.getMessage());
                    }
                }
            }
            ngayHienTai = ngayHienTai.plusDays(1);
        }

        ketQua.add(0, "✅ Phân ca hàng loạt hoàn tất: " + thanhCong + " thành công, " + thatBai + " bỏ qua.");
        return ketQua;
    }

    public boolean themPhanCa(PhanCa phanCa) {
        return phanCa(phanCa);
    }

    public boolean suaPhanCa(PhanCa phanCa) {
        xacThucService.kiemTraQuyen("CT_PHAN_CONG");
        if (phanCa == null || phanCa.getMaPhanCa() == null || phanCa.getMaPhanCa().isBlank()) {
            throw new IllegalArgumentException("Mã phân ca cần sửa không được để trống!");
        }
        return phanCaDAO.capNhatPhanCa(phanCa);
    }

    public boolean kiemTraTrungCa(String maNhanVien, String maCa, LocalDate ngayLam) {
        Ca caMoi = caDAO.layTheoMa(maCa);
        if (caMoi == null) return false;
        List<PhanCa> dsCaTrongNgay = phanCaDAO.layPhanCaTheoNhanVienVaNgay(maNhanVien, ngayLam);
        for (PhanCa daPhan : dsCaTrongNgay) {
            if (daPhan.getGioBatDau() != null && daPhan.getGioKetThuc() != null) {
                boolean biTrung = caMoi.getGioBatDau().isBefore(daPhan.getGioKetThuc()) 
                        && daPhan.getGioBatDau().isBefore(caMoi.getGioKetThuc());
                if (biTrung) return true;
            }
        }
        return false;
    }

    public List<PhanCa> xemLichTheoNhanVien(String maNhanVien) {
        return timKiemPhanCa(null, maNhanVien, null, null);
    }

    public List<PhanCa> xemLichTheoNgay(LocalDate ngay) {
        return layPhanCaTheoNgay(ngay);
    }

    /**
     * Hủy phân ca (xóa mềm). Yêu cầu quyền CT_PHAN_CONG.
     */
    public boolean huyPhanCa(String maPhanCa) {
        xacThucService.kiemTraQuyen("CT_PHAN_CONG");
        if (maPhanCa == null || maPhanCa.isBlank()) {
            throw new IllegalArgumentException("Mã phân ca cần hủy không được để trống!");
        }
        return phanCaDAO.xoaMemPhanCa(maPhanCa.trim());
    }

    /**
     * Lấy danh sách phân ca. Yêu cầu quyền CT_XEM.
     */
    public List<PhanCa> layDanhSachPhanCa() {
        xacThucService.kiemTraQuyen("CT_XEM");
        return phanCaDAO.layDanhSachPhanCa();
    }

    /**
     * Lấy danh sách phân ca theo ngày. Yêu cầu quyền CT_XEM.
     */
    public List<PhanCa> layPhanCaTheoNgay(LocalDate ngay) {
        xacThucService.kiemTraQuyen("CT_XEM");
        if (ngay == null) {
            return phanCaDAO.layDanhSachPhanCa();
        }
        return phanCaDAO.layPhanCaTheoNgay(ngay);
    }

    /**
     * Tìm kiếm và lọc danh sách phân ca. Yêu cầu quyền CT_XEM.
     */
    public List<PhanCa> timKiemPhanCa(LocalDate ngay, String maNhanVien, String maCa, String trangThai) {
        xacThucService.kiemTraQuyen("CT_XEM");
        return phanCaDAO.timKiemPhanCa(ngay, maNhanVien, maCa, trangThai);
    }

    /**
     * Lấy danh sách nhân viên đang làm việc phục vụ chọn trong phân ca.
     * Cung cấp từ Issue #64 Quản lý Nhân viên.
     */
    public List<NhanVien> layDanhSachNhanVienDangLamViec() {
        // TODO: nối NhanVienService khi hoàn thiện Issue #64
        return nhanVienDAO.layDanhSachNhanVienDangLamViec();
    }
}
