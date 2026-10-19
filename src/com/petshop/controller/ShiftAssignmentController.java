package com.petshop.controller;

import com.petshop.dao.ShiftAssignmentDao;
import com.petshop.model.Shift;
import com.petshop.model.ShiftAssignment;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Lớp ShiftAssignmentController điều khiển nghiệp vụ phân công ca trực
 * cho nhân viên tại Happy Tails Pet Shop.
 *
 * Tiêu chuẩn tuân thủ (Review 3_OOP và Review 4_Coding Convention):
 * - Tên lớp là danh từ (ShiftAssignmentController), viết theo chuẩn PascalCase.
 * - Tên phương thức là động từ (lay..., them..., capNhat..., huy..., kiemTra...), viết theo chuẩn camelCase.
 * - Đầy đủ chú thích Javadoc và chú thích từng bước thẩm định nghiệp vụ.
 */
public class ShiftAssignmentController {

    private final ShiftAssignmentDao shiftAssignmentDao;

    /**
     * Khởi tạo với DAO mặc định.
     */
    public ShiftAssignmentController() {
        this.shiftAssignmentDao = new ShiftAssignmentDao();
    }

    /**
     * Khởi tạo với DAO tùy biến (hỗ trợ unit test).
     *
     * @param shiftAssignmentDao Đối tượng DAO
     */
    public ShiftAssignmentController(ShiftAssignmentDao shiftAssignmentDao) {
        this.shiftAssignmentDao = shiftAssignmentDao;
    }

    /**
     * Lấy toàn bộ danh sách phân công ca trực.
     *
     * @return Danh sách đối tượng ShiftAssignment
     */
    public List<ShiftAssignment> layTatCaPhanCa() {
        return shiftAssignmentDao.layTatCaPhanCa();
    }

    /**
     * Tìm kiếm phân công ca theo mã.
     *
     * @param maPhanCa Mã phân ca
     * @return Đối tượng ShiftAssignment nếu có
     */
    public ShiftAssignment timPhanCaTheoMa(String maPhanCa) {
        if (maPhanCa == null || maPhanCa.isBlank()) {
            return null;
        }
        return shiftAssignmentDao.timTheoMaPhanCa(maPhanCa.trim());
    }

    /**
     * Tự động sinh mã phân ca mới nhất theo chuẩn PC-YYYYMMDD-NNNNNN.
     *
     * @return Chuỗi mã phân ca tự sinh
     */
    public String sinhMaPhanCaTuDong() {
        return shiftAssignmentDao.sinhMaPhanCaTuDong();
    }

    /**
     * Lấy danh sách nhân viên đang làm việc phục vụ ComboBox.
     *
     * @return Danh sách map chứa thông tin nhân viên
     */
    public List<Map<String, String>> layDanhSachNhanVien() {
        return shiftAssignmentDao.layDanhSachNhanVienHoatDong();
    }

    /**
     * Lấy danh sách ca trực đang áp dụng phục vụ ComboBox.
     *
     * @return Danh sách các đối tượng Shift
     */
    public List<Shift> layDanhSachCaTruc() {
        return shiftAssignmentDao.layDanhSachCaTrucHoatDong();
    }

    /**
     * Thêm mới một phân công ca trực sau khi thẩm định hợp lệ toàn diện.
     *
     * @param pa Đối tượng phân ca cần thêm
     * @return true nếu ghi vào CSDL thành công
     */
    public boolean themPhanCa(ShiftAssignment pa) {
        kiemTraHopLe(pa);

        // Kiểm tra trùng mã phân ca
        if (shiftAssignmentDao.timTheoMaPhanCa(pa.getMaPhanCa()) != null) {
            throw new IllegalArgumentException("Mã phân ca [" + pa.getMaPhanCa() + "] đã tồn tại trong hệ thống.");
        }

        return shiftAssignmentDao.themPhanCa(pa);
    }

    /**
     * Cập nhật thông tin phân công ca trực.
     *
     * @param pa Đối tượng phân ca với thông tin mới
     * @return true nếu cập nhật thành công
     */
    public boolean capNhatPhanCa(ShiftAssignment pa) {
        kiemTraHopLe(pa);

        // Kiểm tra tồn tại
        if (shiftAssignmentDao.timTheoMaPhanCa(pa.getMaPhanCa()) == null) {
            throw new IllegalArgumentException("Không tìm thấy phân ca có mã: " + pa.getMaPhanCa());
        }

        return shiftAssignmentDao.capNhatPhanCa(pa);
    }

    /**
     * Hủy phân công ca trực (Xóa mềm theo chuẩn OOAD).
     *
     * @param maPhanCa Mã phân ca cần hủy
     * @return true nếu hủy thành công
     */
    public boolean huyPhanCa(String maPhanCa) {
        if (maPhanCa == null || maPhanCa.isBlank()) {
            throw new IllegalArgumentException("Mã phân ca không được để trống.");
        }
        return shiftAssignmentDao.huyPhanCa(maPhanCa.trim());
    }

    /**
     * Thẩm định toàn diện tính hợp lệ của phân ca:
     * 1. Mã nhân viên không được để trống.
     * 2. Mã ca trực không được để trống.
     * 3. Ngày làm không được để trống và phải từ hôm nay trở đi (Khớp quy tắc Modal 4.2A: "Chỉ phân công từ hôm nay").
     * 4. Không được phân trùng 1 nhân viên vào cùng 1 ca trong cùng 1 ngày.
     *
     * @param pa Đối tượng ShiftAssignment cần kiểm tra
     */
    public void kiemTraHopLe(ShiftAssignment pa) {
        if (pa == null) {
            throw new IllegalArgumentException("Thông tin phân ca không được để trống.");
        }

        if (pa.getMaNhanVien() == null || pa.getMaNhanVien().isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn nhân viên được phân công.");
        }

        if (pa.getMaCa() == null || pa.getMaCa().isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn ca trực làm việc.");
        }

        if (pa.getNgayLam() == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày làm việc.");
        }

        // Quy tắc theo thiết kế UI Image 2 & Image 4: "Chỉ phân công từ hôm nay"
        if (pa.getNgayLam().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Chỉ phân công từ hôm nay trở đi (" + LocalDate.now().format(ShiftAssignment.DINH_DANG_NGAY) + ").");
        }

        // Kiểm tra trùng lặp lịch phân ca
        boolean biTrung = shiftAssignmentDao.kiemTraTrungLich(
                pa.getMaNhanVien(),
                pa.getMaCa(),
                pa.getNgayLam(),
                pa.getMaPhanCa()
        );

        if (biTrung) {
            throw new IllegalArgumentException("Nhân viên đã được phân công ca này vào ngày " + pa.getNgayLam().format(ShiftAssignment.DINH_DANG_NGAY) + ".");
        }
    }
}

