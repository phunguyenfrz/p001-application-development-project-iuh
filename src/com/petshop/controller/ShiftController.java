package com.petshop.controller;

import com.petshop.dao.ShiftDao;
import com.petshop.model.Shift;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Lớp ShiftController điều khiển luồng dữ liệu và thẩm định các quy tắc nghiệp vụ
 * cho phân hệ Quản lý Ca trực (Shift Management).
 *
 * Tuân thủ quy tắc lập trình (Review 3_OOP và Review 4_Coding Convention):
 * - Tên lớp là danh từ (ShiftController), viết theo chuẩn PascalCase.
 * - Tên phương thức là động từ (lay..., them..., capNhat..., xoa..., kiemTra...), viết theo chuẩn camelCase.
 * - Tên biến, tham số viết theo camelCase.
 * - Đầy đủ chú thích Javadoc và chú thích một dòng.
 */
public class ShiftController {

    private final ShiftDao shiftDao;

    // Biểu thức chính quy kiểm tra định dạng giờ HH:mm hợp lệ (00:00 đến 23:59)
    public static final Pattern REGEX_GIO_HOP_LE = Pattern.compile("^(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$");

    /**
     * Khởi tạo ShiftController với ShiftDao mặc định.
     */
    public ShiftController() {
        this.shiftDao = new ShiftDao();
    }

    /**
     * Khởi tạo với ShiftDao tùy biến (phục vụ unit test / mock).
     *
     * @param shiftDao Đối tượng ShiftDao
     */
    public ShiftController(ShiftDao shiftDao) {
        this.shiftDao = shiftDao;
    }

    /**
     * Lấy toàn bộ danh sách ca trực.
     *
     * @return Danh sách các ca trực
     */
    public List<Shift> layDanhSachCaTruc() {
        return shiftDao.layTatCaCaTruc();
    }

    /**
     * Lấy toàn bộ danh sách ca trực (tên phương thức tương thích theo động từ Review 3).
     *
     * @return Danh sách các ca trực
     */
    public List<Shift> layTatCaCaTruc() {
        return shiftDao.layTatCaCaTruc();
    }

    /**
     * Tìm kiếm ca trực theo mã định danh duy nhất.
     *
     * @param maCa Mã ca trực cần tìm
     * @return Đối tượng Shift nếu tìm thấy, null nếu không có
     */
    public Shift timCaTrucTheoMa(String maCa) {
        if (maCa == null || maCa.isBlank()) {
            return null;
        }
        return shiftDao.timTheoMaCa(maCa.trim());
    }

    /**
     * Tự động sinh mã ca trực tiếp theo.
     *
     * @return Chuỗi mã ca dạng CA-YYYYMMDD-NNNNNN
     */
    public String sinhMaCaMoi() {
        return shiftDao.sinhMaCaTuDong();
    }

    /**
     * Tự động sinh mã ca trực tiếp theo chuẩn CA-YYYYMMDD-NNNNNN.
     *
     * @return Chuỗi mã ca tự sinh
     */
    public String sinhMaCaTuDong() {
        return shiftDao.sinhMaCaTuDong();
    }

    /**
     * Tìm kiếm ca trực theo từ khóa.
     *
     * @param tuKhoa Chuỗi tìm kiếm
     * @return Danh sách kết quả phù hợp
     */
    public List<Shift> timKiemCaTruc(String tuKhoa) {
        return shiftDao.timKiemTheoTuKhoa(tuKhoa);
    }

    /**
     * Kiểm tra tính hợp lệ nghiệp vụ và thêm mới ca trực.
     *
     * @param ca Đối tượng Shift cần thêm
     * @return true nếu thêm thành công vào CSDL
     */
    public boolean themCaTruc(Shift ca) {
        kiemTraHopLe(ca);

        // Kiểm tra trùng mã ca
        if (shiftDao.timTheoMaCa(ca.getMaCa()) != null) {
            throw new IllegalArgumentException("Mã ca trực [" + ca.getMaCa() + "] đã tồn tại trong hệ thống.");
        }

        return shiftDao.themCaTruc(ca);
    }

    /**
     * Kiểm tra tính hợp lệ nghiệp vụ và cập nhật ca trực.
     *
     * @param ca Đối tượng Shift với dữ liệu mới
     * @return true nếu cập nhật thành công
     */
    public boolean capNhatCaTruc(Shift ca) {
        kiemTraHopLe(ca);

        // Kiểm tra tồn tại trước khi cập nhật
        if (shiftDao.timTheoMaCa(ca.getMaCa()) == null) {
            throw new IllegalArgumentException("Không tìm thấy ca trực có mã: " + ca.getMaCa());
        }

        return shiftDao.capNhatCaTruc(ca);
    }

    /**
     * Thực hiện xóa mềm ca trực (chuyển trạng thái sang Ngừng áp dụng).
     *
     * @param maCa Mã ca trực cần xóa mềm
     * @return true nếu thao tác thành công
     */
    public boolean xoaCaTruc(String maCa) {
        if (maCa == null || maCa.isBlank()) {
            throw new IllegalArgumentException("Mã ca trực không được để trống.");
        }
        return shiftDao.xoaMemCaTruc(maCa.trim());
    }

    /**
     * Thẩm định toàn diện tính hợp lệ của ca trực theo tiêu chuẩn nghiệp vụ:
     * - Tên ca không được rỗng
     * - Giờ bắt đầu và giờ kết thúc không được để trống
     * - Giờ kết thúc phải lớn hơn giờ bắt đầu (khớp ràng buộc CSDL CK_Ca_Gio)
     *
     * @param ca Đối tượng Shift cần kiểm tra
     */
    public void kiemTraHopLe(Shift ca) {
        if (ca == null) {
            throw new IllegalArgumentException("Thông tin ca trực không được để trống.");
        }

        // 1. Kiểm tra tên ca
        if (ca.getTenCa() == null || ca.getTenCa().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập tên ca trực.");
        }

        // 2. Kiểm tra giờ bắt đầu
        if (ca.getGioBatDau() == null) {
            throw new IllegalArgumentException("Vui lòng nhập giờ bắt đầu.");
        }

        // 3. Kiểm tra giờ kết thúc
        if (ca.getGioKetThuc() == null) {
            throw new IllegalArgumentException("Vui lòng nhập giờ kết thúc.");
        }

        // 4. Ràng buộc giờ kết thúc phải lớn hơn giờ bắt đầu
        if (!ca.getGioKetThuc().isAfter(ca.getGioBatDau())) {
            throw new IllegalArgumentException("Giờ kết thúc phải lớn hơn giờ bắt đầu.");
        }
    }

    /**
     * Thẩm định hợp lệ từ các chuỗi nhập liệu của giao diện form.
     *
     * @param tenCa         Chuỗi tên ca trực
     * @param gioBatDauStr  Chuỗi giờ bắt đầu
     * @param gioKetThucStr Chuỗi giờ kết thúc
     * @return Chuỗi mô tả lỗi nếu có, null nếu hợp lệ
     */
    public String kiemTraHopLe(String tenCa, String gioBatDauStr, String gioKetThucStr) {
        if (tenCa == null || tenCa.trim().isEmpty()) {
            return "Tên ca không được để trống.";
        }
        if (gioBatDauStr == null || gioBatDauStr.trim().isEmpty()) {
            return "Giờ bắt đầu không được để trống.";
        }
        if (gioKetThucStr == null || gioKetThucStr.trim().isEmpty()) {
            return "Giờ kết thúc không được để trống.";
        }
        try {
            LocalTime batDau = chuyenDoiSangLocalTime(gioBatDauStr);
            LocalTime ketThuc = chuyenDoiSangLocalTime(gioKetThucStr);
            if (!ketThuc.isAfter(batDau)) {
                return "Giờ kết thúc phải lớn hơn giờ bắt đầu (" + gioBatDauStr.trim() + ").";
            }
        } catch (IllegalArgumentException ex) {
            return ex.getMessage();
        }
        return null;
    }

    /**
     * Kiểm tra chuỗi định dạng giờ nhập vào có đúng chuẩn HH:mm (00:00 - 23:59) hay không.
     *
     * @param gioStr Chuỗi giờ cần kiểm tra
     * @return true nếu hợp lệ
     * @throws IllegalArgumentException nếu không đúng định dạng hoặc giá trị giờ/phút ngoài phạm vi
     */
    public boolean kiemTraDinhDangGio(String gioStr) {
        if (gioStr == null || gioStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập thời gian.");
        }

        String trimmed = gioStr.trim();
        if (!REGEX_GIO_HOP_LE.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("\"" + trimmed + "\" không hợp lệ. Nhập HH:mm (00:00–23:59).");
        }

        try {
            LocalTime.parse(trimmed, Shift.DINH_DANG_GIO);
            return true;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("\"" + trimmed + "\" không hợp lệ. Nhập HH:mm (00:00–23:59).");
        }
    }

    /**
     * Chuyển đổi an toàn chuỗi HH:mm sang đối tượng LocalTime.
     *
     * @param gioStr Chuỗi giờ hợp lệ
     * @return Đối tượng LocalTime tương ứng
     */
    public LocalTime chuyenDoiSangLocalTime(String gioStr) {
        kiemTraDinhDangGio(gioStr);
        return LocalTime.parse(gioStr.trim(), Shift.DINH_DANG_GIO);
    }
}

