package com.petshop.controller;

import com.petshop.dao.PromotionDao;
import com.petshop.model.Promotion;

import java.time.LocalDate;
import java.util.List;

/**
 * Lớp PromotionController điều khiển luồng dữ liệu giữa giao diện (View) và tầng dữ liệu (DAO).
 * Thực hiện các quy tắc kiểm tra tính hợp lệ nghiệp vụ trước khi ghi nhận xuống CSDL.
 */
public class PromotionController {

    private final PromotionDao promotionDao;

    /**
     * Khởi tạo PromotionController cùng đối tượng PromotionDao.
     */
    public PromotionController() {
        this.promotionDao = new PromotionDao();
    }

    /**
     * Khởi tạo với DAO tùy biến (hỗ trợ kiểm thử đơn vị hoặc mock).
     *
     * @param promotionDao Đối tượng PromotionDao cần liên kết
     */
    public PromotionController(PromotionDao promotionDao) {
        this.promotionDao = promotionDao;
    }

    /**
     * Lấy danh sách toàn bộ các chương trình khuyến mãi.
     *
     * @return Danh sách khuyến mãi
     */
    public List<Promotion> layDanhSachKhuyenMai() {
        return promotionDao.layTatCaKhuyenMai();
    }

    /**
     * Tìm kiếm các chương trình khuyến mãi theo từ khóa nhập vào.
     *
     * @param tuKhoa Từ khóa tìm kiếm (mã định danh hoặc tên chương trình)
     * @return Danh sách kết quả phù hợp
     */
    public List<Promotion> timKiemKhuyenMai(String tuKhoa) {
        return promotionDao.timKiemTheoTuKhoa(tuKhoa);
    }

    /**
     * Tìm kiếm một khuyến mãi theo mã định danh duy nhất.
     *
     * @param maKhuyenMai Mã khuyến mãi cần tìm
     * @return Đối tượng Promotion nếu có
     */
    public Promotion timKhuyenMaiTheoMa(String maKhuyenMai) {
        if (maKhuyenMai == null || maKhuyenMai.isBlank()) {
            return null;
        }
        return promotionDao.timTheoMaKhuyenMai(maKhuyenMai.trim());
    }

    /**
     * Tự động sinh mã khuyến mãi tiếp theo cho màn hình thêm mới.
     *
     * @return Chuỗi mã khuyến mãi định dạng KM-YYYY-NNN
     */
    public String sinhMaKhuyenMaiMoi() {
        return promotionDao.sinhMaKhuyenMaiTuDong();
    }

    /**
     * Kiểm tra tính hợp lệ dữ liệu và thêm mới chương trình khuyến mãi.
     *
     * @param km Đối tượng Promotion cần thêm
     * @return true nếu thêm thành công vào CSDL
     */
    public boolean themKhuyenMai(Promotion km) {
        kiemTraHopLe(km);

        // Kiểm tra trùng mã
        if (promotionDao.timTheoMaKhuyenMai(km.getMaKhuyenMai()) != null) {
            throw new IllegalArgumentException("Mã khuyến mãi [" + km.getMaKhuyenMai() + "] đã tồn tại trong hệ thống.");
        }

        return promotionDao.themKhuyenMai(km);
    }

    /**
     * Kiểm tra tính hợp lệ dữ liệu và cập nhật thông tin khuyến mãi đã có.
     *
     * @param km Đối tượng Promotion đã chỉnh sửa
     * @return true nếu cập nhật thành công vào CSDL
     */
    public boolean capNhatKhuyenMai(Promotion km) {
        kiemTraHopLe(km);

        // Kiểm tra tồn tại trước khi cập nhật
        if (promotionDao.timTheoMaKhuyenMai(km.getMaKhuyenMai()) == null) {
            throw new IllegalArgumentException("Không tìm thấy chương trình khuyến mãi mã: " + km.getMaKhuyenMai());
        }

        return promotionDao.capNhatKhuyenMai(km);
    }

    /**
     * Thực hiện xóa mềm (ngừng áp dụng) chương trình khuyến mãi theo đúng tài liệu thiết kế OOAD.
     *
     * @param maKhuyenMai Mã khuyến mãi cần xóa mềm
     * @return true nếu thao tác thành công
     */
    public boolean xoaKhuyenMai(String maKhuyenMai) {
        if (maKhuyenMai == null || maKhuyenMai.isBlank()) {
            throw new IllegalArgumentException("Mã khuyến mãi không được để trống.");
        }

        return promotionDao.xoaMemKhuyenMai(maKhuyenMai.trim());
    }

    /**
     * Kiểm tra toàn diện tính hợp lệ của dữ liệu khuyến mãi theo tiêu chuẩn nghiệp vụ:
     * - Tên chương trình không được rỗng
     * - Phần trăm giảm phải từ 1 đến 100
     * - Ngày bắt đầu và ngày kết thúc không được để trống
     * - Ngày kết thúc không được trước ngày bắt đầu
     *
     * @param km Đối tượng Promotion cần thẩm định dữ liệu
     */
    public void kiemTraHopLe(Promotion km) {
        if (km == null) {
            throw new IllegalArgumentException("Thông tin khuyến mãi không được để trống.");
        }

        // Kiểm tra tên chương trình
        if (km.getTenChuongTrinh() == null || km.getTenChuongTrinh().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập tên chương trình khuyến mãi.");
        }

        // Kiểm tra phần trăm giảm
        if (km.getPhanTramGiam() <= 0 || km.getPhanTramGiam() > 100) {
            throw new IllegalArgumentException("Phần trăm giảm phải là số từ 1 đến 100.");
        }

        // Kiểm tra ngày bắt đầu
        if (km.getNgayBatDau() == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày bắt đầu áp dụng.");
        }

        // Kiểm tra ngày kết thúc
        if (km.getNgayKetThuc() == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày kết thúc áp dụng.");
        }

        // Kiểm tra quy tắc ngày kết thúc không được trước ngày bắt đầu
        if (km.getNgayKetThuc().isBefore(km.getNgayBatDau())) {
            throw new IllegalArgumentException("Ngày kết thúc không được trước ngày bắt đầu.");
        }
    }
}

