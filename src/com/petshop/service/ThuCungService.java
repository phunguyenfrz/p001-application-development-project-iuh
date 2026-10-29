package com.petshop.service;

import com.petshop.dao.ThuCungDAO;
import com.petshop.model.ChiTietDichVuLichHen;
import com.petshop.model.LichSuChamSoc;
import com.petshop.model.ThuCung;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Lớp Dịch vụ phụ trách xử lý nghiệp vụ, kiểm tra ràng buộc dữ liệu (Validation)
 * và phân quyền người dùng cho phân hệ Quản lý Thú cưng (THU_CUNG).
 * Tuân thủ Quy ước #60.
 */
public class ThuCungService {

    public static final String REGEX_MA_THU_CUNG = "^TC-\\d{8}-\\d{6}$";

    private final ThuCungDAO thuCungDAO;
    private final XacThucService xacThucService;

    public ThuCungService() {
        this(new ThuCungDAO(), new XacThucService());
    }

    public ThuCungService(ThuCungDAO thuCungDAO, XacThucService xacThucService) {
        this.thuCungDAO = thuCungDAO;
        this.xacThucService = xacThucService;
    }

    /**
     * Lấy toàn bộ danh sách thú cưng trong hệ thống.
     * Quyền yêu cầu: TC_CAP_NHAT hoặc TC_TIM_KIEM.
     */
    public List<ThuCung> layDanhSachThuCung() {
        return thuCungDAO.layTatCaThuCung();
    }

    /**
     * Lấy danh sách thú cưng thuộc sở hữu của một khách hàng cụ thể.
     */
    public List<ThuCung> layDanhSachTheoKhachHang(String maKhachHang) {
        if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khách hàng không được để trống!");
        }
        return thuCungDAO.layDanhSachTheoKhachHang(maKhachHang.trim());
    }

    /**
     * Lấy chi tiết thông tin của một thú cưng theo mã.
     */
    public ThuCung layThuCungTheoMa(String maThuCung) {
        if (maThuCung == null || maThuCung.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã thú cưng không được để trống!");
        }
        return thuCungDAO.layThuCungTheoMa(maThuCung.trim());
    }

    /**
     * Tìm kiếm và lọc danh sách thú cưng linh hoạt theo từ khóa và loài.
     * Quyền yêu cầu: TC_TIM_KIEM.
     */
    public List<ThuCung> timKiemThuCung(String tuKhoa, String locLoai) {
        xacThucService.kiemTraQuyen("TC_TIM_KIEM");
        return thuCungDAO.timKiemThuCung(tuKhoa, locLoai);
    }

    /**
     * Thêm mới một hồ sơ thú cưng vào hệ thống.
     * Quyền yêu cầu: TC_CAP_NHAT.
     */
    public boolean themThuCung(ThuCung thuCung) {
        xacThucService.kiemTraQuyen("TC_CAP_NHAT");
        kiemTraDuLieuThuCung(thuCung, false);

        if (thuCung.getMaThuCung() == null || thuCung.getMaThuCung().trim().isEmpty()) {
            thuCung.setMaThuCung(thuCungDAO.taoMaThuCung());
        }

        return thuCungDAO.themThuCung(thuCung);
    }

    /**
     * Cập nhật thông tin hồ sơ thú cưng.
     * Quyền yêu cầu: TC_CAP_NHAT.
     */
    public boolean capNhatThuCung(ThuCung thuCung) {
        xacThucService.kiemTraQuyen("TC_CAP_NHAT");
        kiemTraDuLieuThuCung(thuCung, true);
        return thuCungDAO.capNhatThuCung(thuCung);
    }

    /**
     * Sinh tự động mã thú cưng mới theo mẫu duy nhất.
     */
    public String taoMaThuCungMoi() {
        return thuCungDAO.taoMaThuCung();
    }

    /**
     * Lấy lịch sử chăm sóc dịch vụ của thú cưng.
     * Quyền yêu cầu: TC_CHAM_SOC hoặc TC_CAP_NHAT hoặc TC_TIM_KIEM.
     */
    public List<String[]> layLichSuChamSoc(String maThuCung) {
        if (maThuCung == null || maThuCung.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã thú cưng không được để trống!");
        }
        return thuCungDAO.layLichSuChamSoc(maThuCung.trim());
    }

    /**
     * Lấy danh sách lịch hẹn chăm sóc của thú cưng theo bộ lọc thời gian và trạng thái.
     * Quyền yêu cầu: TC_CHAM_SOC hoặc TC_TIM_KIEM hoặc TC_CAP_NHAT.
     */
    public List<LichSuChamSoc> layDanhSachLichHenChamSoc(String maThuCung, LocalDate tuNgay, LocalDate denNgay, String trangThai) {
        if (maThuCung == null || maThuCung.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã thú cưng không được để trống!");
        }
        xacThucService.kiemTraQuyen("TC_CHAM_SOC");
        return thuCungDAO.layDanhSachLichHenChamSoc(maThuCung.trim(), tuNgay, denNgay, trangThai);
    }

    /**
     * Lấy danh sách chi tiết các dịch vụ đã sử dụng trong một lần hẹn chăm sóc.
     */
    public List<ChiTietDichVuLichHen> layChiTietDichVuLichHen(String maLichHen) {
        if (maLichHen == null || maLichHen.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã lịch hẹn không được để trống!");
        }
        xacThucService.kiemTraQuyen("TC_CHAM_SOC");
        return thuCungDAO.layChiTietDichVuLichHen(maLichHen.trim());
    }

    /**
     * Kiểm tra tính hợp lệ dữ liệu của thực thể Thú cưng theo đúng ràng buộc CSDL và nghiệp vụ.
     *
     * @param thuCung    Đối tượng thú cưng cần kiểm tra
     * @param laCapNhat  true nếu đang ở luồng cập nhật
     */
    public void kiemTraDuLieuThuCung(ThuCung thuCung, boolean laCapNhat) {
        if (thuCung == null) {
            throw new IllegalArgumentException("Thông tin thú cưng không được để trống!");
        }

        // 1. Kiểm tra mã thú cưng
        if (laCapNhat) {
            if (thuCung.getMaThuCung() == null || !thuCung.getMaThuCung().matches(REGEX_MA_THU_CUNG)) {
                throw new IllegalArgumentException("Mã thú cưng không hợp lệ (định dạng chuẩn: TC-yyyyMMdd-xxxxxx)!");
            }
        }

        // 2. Kiểm tra chủ nuôi (Khách hàng sở hữu)
        if (thuCung.getMaKhachHang() == null || thuCung.getMaKhachHang().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn khách hàng chủ nuôi cho thú cưng!");
        }

        // 3. Kiểm tra tên thú cưng (Bắt buộc, không quá 100 ký tự)
        if (thuCung.getTenThuCung() == null || thuCung.getTenThuCung().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên thú cưng không được để trống!");
        }
        if (thuCung.getTenThuCung().trim().length() > 100) {
            throw new IllegalArgumentException("Tên thú cưng không được vượt quá 100 ký tự!");
        }

        // 4. Kiểm tra cân nặng (Bắt buộc > 0)
        BigDecimal canNang = thuCung.getCanNang();
        if (canNang == null || canNang.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Cân nặng thú cưng phải lớn hơn 0 kg!");
        }
        if (canNang.compareTo(new BigDecimal("999.99")) > 0) {
            throw new IllegalArgumentException("Cân nặng thú cưng không được vượt quá 999.99 kg!");
        }

        // 5. Kiểm tra ngày sinh (Không được ở tương lai)
        if (thuCung.getNgaySinh() != null && thuCung.getNgaySinh().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày sinh của thú cưng không được ở tương lai!");
        }

        // 6. Kiểm tra giới tính (Nếu có, phải là Đực hoặc Cái)
        if (thuCung.getGioiTinh() != null && !thuCung.getGioiTinh().trim().isEmpty()) {
            String gt = thuCung.getGioiTinh().trim();
            if (!"Đực".equalsIgnoreCase(gt) && !"Cái".equalsIgnoreCase(gt)) {
                throw new IllegalArgumentException("Giới tính thú cưng chỉ được là 'Đực' hoặc 'Cái'!");
            }
        }
    }
}

