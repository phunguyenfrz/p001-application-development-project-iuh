package com.petshop.service;

import com.petshop.dao.PetCareDao;
import com.petshop.model.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Kiểm tra dữ liệu trước khi gọi DAO chăm sóc thú cưng. */
public class PetCareService {
    private final PetCareDao care_dao = new PetCareDao();

    /**
     * @param pet_id Mã thú cưng.
     * @param from_date Ngày bắt đầu hoặc null.
     * @param to_date Ngày kết thúc hoặc null.
     * @param status Trạng thái hoặc Tất cả.
     * @return Các lịch hẹn phù hợp.
     */
    public List<PetCareVisit> findVisits(String pet_id, LocalDate from_date,
            LocalDate to_date, String status) {
        validateId(pet_id, "TC");
        if (from_date != null && to_date != null && from_date.isAfter(to_date)) {
            throw new IllegalArgumentException("Từ ngày phải nhỏ hơn hoặc bằng Đến ngày.");
        }
        if (status == null || !List.of("Tất cả", "Đã đặt", "Đã xác nhận",
                "Đang thực hiện", "Hoàn thành", "Đã hủy").contains(status)) {
            throw new IllegalArgumentException("Trạng thái lịch hẹn không hợp lệ.");
        }
        try {
            return care_dao.findVisits(pet_id, from_date, to_date,
                    "Tất cả".equals(status) ? null : status);
        } catch (SQLException exception) { throw databaseError(exception); }
    }

    /** @param pet_id Mã thú cưng. @return Hồ sơ chăm sóc hiện tại. */
    public PetCareProfile findProfile(String pet_id) {
        validateId(pet_id, "TC");
        try {
            PetCareProfile profile = care_dao.findProfile(pet_id);
            if (profile == null) throw new IllegalStateException("Không còn tìm thấy hồ sơ thú cưng.");
            return profile;
        } catch (SQLException exception) { throw databaseError(exception); }
    }

    /**
     * @param visit_id Mã lịch hẹn.
     * @param pet_id Mã thú cưng.
     * @return Chi tiết dịch vụ đã lưu trong lịch hẹn.
     */
    public List<PetCareServiceItem> findServices(String visit_id, String pet_id) {
        validateId(visit_id, "LH");
        validateId(pet_id, "TC");
        try { return care_dao.findServices(visit_id, pet_id); }
        catch (SQLException exception) { throw databaseError(exception); }
    }

    /**
     * Cân nặng hợp lệ: lớn hơn 0, tối đa 999,99 kg và hai chữ số thập phân.
     * @param pet_id Mã thú cưng.
     * @param weight_text Cân nặng nhập bằng dấu chấm hoặc phẩy.
     * @param allergy Dị ứng, tối đa 255 ký tự.
     */
    public void updateProfile(String pet_id, String weight_text, String allergy) {
        validateId(pet_id, "TC");
        BigDecimal weight;
        try {
            String value = weight_text == null ? "" : weight_text.trim().replace(',', '.');
            if (!value.matches("[0-9]+(?:\\.[0-9]{1,2})?")) throw new NumberFormatException();
            weight = new BigDecimal(value).setScale(2, RoundingMode.UNNECESSARY);
            if (weight.signum() <= 0 || weight.compareTo(new BigDecimal("999.99")) > 0) {
                throw new NumberFormatException();
            }
        } catch (ArithmeticException | NumberFormatException exception) {
            throw new IllegalArgumentException("Cân nặng phải lớn hơn 0, tối đa 999,99 kg và có tối đa 2 số thập phân.");
        }
        String allergy_value = validateNote(allergy, "Dị ứng");
        try { care_dao.updateProfile(pet_id, weight, allergy_value); }
        catch (SQLException exception) { throw databaseError(exception); }
    }

    /** @param value Giá trị nhập. @param title Tên trường. @return Nội dung hoặc null. */
    private String validateNote(String value, String title) {
        String result = value == null ? "" : value.trim();
        if (result.length() > 255) throw new IllegalArgumentException(title + " tối đa 255 ký tự.");
        return result.isEmpty() ? null : result;
    }

    /** @param value Mã hồ sơ. @param prefix Tiền tố TC hoặc LH. */
    private void validateId(String value, String prefix) {
        if (value == null || !value.matches(prefix + "-\\d{8}-\\d{6}")) {
            throw new IllegalArgumentException("Mã " + prefix + " không hợp lệ.");
        }
    }

    /** @param exception Lỗi SQL gốc. @return Lỗi nghiệp vụ có nguyên nhân gốc. */
    private IllegalStateException databaseError(SQLException exception) {
        return new IllegalStateException("Không thực hiện được thao tác chăm sóc: " + exception.getMessage(), exception);
    }
}
